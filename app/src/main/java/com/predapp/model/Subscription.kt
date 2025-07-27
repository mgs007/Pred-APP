package com.predapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Subscription(
    @DocumentId
    val subscriptionId: String = "",
    val userId: String = "",
    val paymentReference: String = "",
    val transactionNumber: String = "",
    val status: SubscriptionStatus = SubscriptionStatus.PENDING,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val planType: PlanType = PlanType.MONTHLY,
    val amount: Double = 0.0,
    val currency: String = "USD",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val verifiedBy: String = "",
    val verifiedAt: Timestamp? = null,
    val notes: String = ""
) {
    // Enum classes for subscription properties
    enum class SubscriptionStatus {
        PENDING, ACTIVE, EXPIRED, CANCELLED, REJECTED
    }
    
    enum class PlanType {
        MONTHLY, QUARTERLY, YEARLY
    }
    
    // Helper methods
    fun isActive(): Boolean {
        return status == SubscriptionStatus.ACTIVE && 
               endDate != null && 
               endDate.toDate().time > System.currentTimeMillis()
    }
    
    fun isPending(): Boolean {
        return status == SubscriptionStatus.PENDING
    }
    
    fun isRejected(): Boolean {
        return status == SubscriptionStatus.REJECTED
    }
    
    fun daysRemaining(): Int {
        if (status != SubscriptionStatus.ACTIVE || endDate == null) return 0
        
        val currentTimeMillis = System.currentTimeMillis()
        val expiryTimeMillis = endDate.toDate().time
        
        if (expiryTimeMillis <= currentTimeMillis) return 0
        
        val millisRemaining = expiryTimeMillis - currentTimeMillis
        return (millisRemaining / (1000 * 60 * 60 * 24)).toInt()
    }
    
    fun getDurationInDays(): Int {
        if (startDate == null || endDate == null) return 0
        
        val startTimeMillis = startDate.toDate().time
        val endTimeMillis = endDate.toDate().time
        
        val millisDuration = endTimeMillis - startTimeMillis
        return (millisDuration / (1000 * 60 * 60 * 24)).toInt()
    }
    
    fun getPlanName(): String {
        return when (planType) {
            PlanType.MONTHLY -> "Monthly Plan"
            PlanType.QUARTERLY -> "Quarterly Plan"
            PlanType.YEARLY -> "Yearly Plan"
        }
    }
}