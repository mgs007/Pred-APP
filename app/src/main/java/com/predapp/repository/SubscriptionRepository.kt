package com.predapp.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.predapp.model.PlanType
import com.predapp.model.Subscription
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class SubscriptionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val subscriptionsCollection = firestore.collection("subscriptions")
    private val userRepository = UserRepository()
    
    // Get user's active subscription
    suspend fun getUserActiveSubscription(userId: String): Subscription? {
        return try {
            val result = subscriptionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", Subscription.SubscriptionStatus.ACTIVE.name)
                .whereGreaterThan("endDate", Timestamp.now())
                .orderBy("endDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            if (result.isEmpty) null else result.documents[0].toObject(Subscription::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    // Get user's pending subscription
    suspend fun getUserPendingSubscription(userId: String): Subscription? {
        return try {
            val result = subscriptionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", Subscription.SubscriptionStatus.PENDING.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            if (result.isEmpty) null else result.documents[0].toObject(Subscription::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    // Get user's subscription history
    suspend fun getUserSubscriptionHistory(userId: String): List<Subscription> {
        return try {
            subscriptionsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Subscription::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Create new subscription request
    suspend fun createSubscriptionRequest(
        userId: String,
        paymentReference: String,
        planType: PlanType,
        amount: Double,
        currency: String = "USD",
        transactionNumber: String = ""
    ): String? {
        return try {
            // Check if user already has a pending subscription
            val pendingSubscription = getUserPendingSubscription(userId)
            if (pendingSubscription != null) {
                // Update existing pending subscription
                val updatedSubscription = pendingSubscription.copy(
                    paymentReference = paymentReference,
                    planType = planType,
                    amount = amount,
                    currency = currency,
                    transactionNumber = if (transactionNumber.isNotEmpty()) transactionNumber else pendingSubscription.transactionNumber,
                    updatedAt = Timestamp.now()
                )
                
                subscriptionsCollection.document(pendingSubscription.subscriptionId)
                    .set(updatedSubscription)
                    .await()
                
                return pendingSubscription.subscriptionId
            }
            
            // Create new subscription
            val documentRef = subscriptionsCollection.document()
            val subscription = Subscription(
                subscriptionId = documentRef.id,
                userId = userId,
                paymentReference = paymentReference,
                transactionNumber = transactionNumber,
                planType = planType,
                amount = amount,
                currency = currency,
                status = Subscription.SubscriptionStatus.PENDING,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            documentRef.set(subscription).await()
            documentRef.id
        } catch (e: Exception) {
            null
        }
    }
    
    // Submit transaction number for verification
    suspend fun submitTransactionNumber(subscriptionId: String, transactionNumber: String): Boolean {
        return try {
            subscriptionsCollection.document(subscriptionId)
                .update(
                    "transactionNumber", transactionNumber,
                    "updatedAt", Timestamp.now()
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Verify subscription payment (by expert/admin)
    suspend fun verifySubscriptionPayment(
        subscriptionId: String,
        verifiedBy: String,
        approved: Boolean,
        notes: String = ""
    ): Boolean {
        return try {
            val subscription = getSubscriptionById(subscriptionId) ?: return false
            
            if (approved) {
                // Calculate subscription dates based on plan type
                val startDate = Timestamp.now()
                val endDate = calculateEndDate(startDate.toDate(), subscription.planType)
                
                // Update subscription status to active
                val updates = hashMapOf<String, Any>(
                    "status" to Subscription.SubscriptionStatus.ACTIVE.name,
                    "startDate" to startDate,
                    "endDate" to Timestamp(endDate),
                    "verifiedBy" to verifiedBy,
                    "verifiedAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now(),
                    "notes" to notes
                )
                
                subscriptionsCollection.document(subscriptionId)
                    .update(updates)
                    .await()
                
                // Update user's premium status
                userRepository.updatePremiumStatus(
                    subscription.userId,
                    true,
                    Timestamp(endDate)
                )
            } else {
                // Update subscription status to rejected
                subscriptionsCollection.document(subscriptionId)
                    .update(
                        "status", Subscription.SubscriptionStatus.REJECTED.name,
                        "verifiedBy", verifiedBy,
                        "verifiedAt", Timestamp.now(),
                        "updatedAt", Timestamp.now(),
                        "notes", notes
                    )
                    .await()
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Get subscription by ID
    suspend fun getSubscriptionById(subscriptionId: String): Subscription? {
        return try {
            val document = subscriptionsCollection.document(subscriptionId).get().await()
            if (document.exists()) {
                document.toObject(Subscription::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Get all pending subscriptions (for admin/expert verification)
    suspend fun getAllPendingSubscriptions(): List<Subscription> {
        return try {
            subscriptionsCollection
                .whereEqualTo("status", Subscription.SubscriptionStatus.PENDING.name)
                .whereNotEqualTo("transactionNumber", "")
                .orderBy("transactionNumber", Query.Direction.ASCENDING)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(Subscription::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Calculate subscription end date based on plan type
    private fun calculateEndDate(startDate: Date, planType: PlanType): Date {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        when (planType) {
            PlanType.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            PlanType.QUARTERLY -> calendar.add(Calendar.MONTH, 3)
            PlanType.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }
        
        return calendar.time
    }
}