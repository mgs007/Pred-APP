package com.predapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Prediction(
    @DocumentId
    val predictionId: String = "",
    val expertId: String = "",
    val expertName: String = "",
    val matchDetails: MatchDetails = MatchDetails(),
    val predictionType: PredictionType = PredictionType.FREE,
    val confidenceLevel: Int = 1, // 1-5 stars
    val analysisText: String = "",
    val category: String = "", // Sport category: football, basketball, etc.
    val isPOTD: Boolean = false,
    val resultStatus: ResultStatus = ResultStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val matchDate: Timestamp = Timestamp.now(),
    val odds: Double = 0.0
) {
    // Enum classes for prediction properties
    enum class PredictionType {
        FREE, PREMIUM
    }
    
    enum class ResultStatus {
        PENDING, WIN, LOSS
    }
    
    // Match details data class
    data class MatchDetails(
        val homeTeam: String = "",
        val awayTeam: String = "",
        val league: String = "",
        val predictionText: String = "",
        val homeScore: Int? = null,
        val awayScore: Int? = null,
        val matchTime: String = ""
    ) {
        // Format match as "Team A vs Team B"
        fun formatMatchTitle(): String {
            return "$homeTeam vs $awayTeam"
        }
        
        // Format score if available
        fun formatScore(): String {
            return if (homeScore != null && awayScore != null) {
                "$homeScore - $awayScore"
            } else {
                ""
            }
        }
    }
    
    // Helper methods
    fun isMatchCompleted(): Boolean {
        return resultStatus != ResultStatus.PENDING
    }
    
    fun isWin(): Boolean {
        return resultStatus == ResultStatus.WIN
    }
    
    fun isLoss(): Boolean {
        return resultStatus == ResultStatus.LOSS
    }
    
    fun isPending(): Boolean {
        return resultStatus == ResultStatus.PENDING
    }
    
    fun isFree(): Boolean {
        return predictionType == PredictionType.FREE
    }
    
    fun isPremium(): Boolean {
        return predictionType == PredictionType.PREMIUM
    }
}