package com.predapp.model

enum class PlanType {
    MONTHLY, QUARTERLY, YEARLY;
    
    fun getPlanName(): String {
        return when (this) {
            MONTHLY -> "Monthly Plan"
            QUARTERLY -> "Quarterly Plan"
            YEARLY -> "Yearly Plan"
        }
    }
}