package com.predapp.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.predapp.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    // Get current Firebase user
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    // Get current user data from Firestore
    suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null
        return getUserById(currentUser.uid)
    }
    
    // Get user by ID
    suspend fun getUserById(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Create or update user
    suspend fun createOrUpdateUser(user: User): Boolean {
        return try {
            usersCollection.document(user.userId)
                .set(user, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Update user premium status
    suspend fun updatePremiumStatus(userId: String, isPremium: Boolean, expiryDate: Timestamp?): Boolean {
        return try {
            val updates = hashMapOf<String, Any?>(
                "isPremium" to isPremium,
                "subscriptionExpiry" to expiryDate,
                "updatedAt" to Timestamp.now()
            )
            
            usersCollection.document(userId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Update user favorite sports
    suspend fun updateFavoriteSports(userId: String, favoriteSports: List<String>): Boolean {
        return try {
            usersCollection.document(userId)
                .update(
                    "favoriteSports", favoriteSports,
                    "updatedAt", Timestamp.now()
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Update user profile
    suspend fun updateUserProfile(userId: String, displayName: String, photoUrl: String): Boolean {
        return try {
            val updates = hashMapOf<String, Any>(
                "displayName" to displayName,
                "updatedAt" to Timestamp.now()
            )
            
            if (photoUrl.isNotEmpty()) {
                updates["photoUrl"] = photoUrl
            }
            
            usersCollection.document(userId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Check if user is an expert
    suspend fun isUserExpert(userId: String): Boolean {
        val user = getUserById(userId) ?: return false
        return user.isExpert
    }
    
    // Sign out
    fun signOut() {
        auth.signOut()
    }
}