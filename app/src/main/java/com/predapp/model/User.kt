package com.predapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val userId: String = "",
    val email: String = "",
    val phone: String = "",
    val isPremium: Boolean = false,
    val subscriptionExpiry: Timestamp? = null,
    val favoriteSports: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val lastLoginAt: Timestamp = Timestamp.now(),
    val isExpert: Boolean = false,
    val displayName: String = "",
    val photoUrl: String = ""
) {
    // Check if user has an active premium subscription
    fun hasActivePremium(): Boolean {
        return isPremium && subscriptionExpiry != null && 
               subscriptionExpiry.toDate().time > System.currentTimeMillis()
    }
    
    // Calculate days remaining in subscription
    fun daysRemainingInSubscription(): Int {
        if (!isPremium || subscriptionExpiry == null) return 0
        
        val currentTimeMillis = System.currentTimeMillis()
        val expiryTimeMillis = subscriptionExpiry.toDate().time
        
        if (expiryTimeMillis <= currentTimeMillis) return 0
        
        val millisRemaining = expiryTimeMillis - currentTimeMillis
        return (millisRemaining / (1000 * 60 * 60 * 24)).toInt()
    }
}