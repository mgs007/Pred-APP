package com.predapp.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.predapp.model.Prediction
import kotlinx.coroutines.tasks.await

class PredictionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val predictionsCollection = firestore.collection("predictions")
    
    // Get all free predictions
    suspend fun getFreePredictions(): List<Prediction> {
        return try {
            predictionsCollection
                .whereEqualTo("predictionType", Prediction.PredictionType.FREE.name)
                .orderBy("matchDate", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Prediction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get all premium predictions
    suspend fun getPremiumPredictions(): List<Prediction> {
        return try {
            predictionsCollection
                .whereEqualTo("predictionType", Prediction.PredictionType.PREMIUM.name)
                .orderBy("matchDate", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Prediction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get prediction of the day
    suspend fun getPredictionOfTheDay(): Prediction? {
        return try {
            val result = predictionsCollection
                .whereEqualTo("isPOTD", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            if (result.isEmpty) null else result.documents[0].toObject(Prediction::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    // Get predictions by sport category
    suspend fun getPredictionsByCategory(category: String): List<Prediction> {
        return try {
            predictionsCollection
                .whereEqualTo("category", category)
                .orderBy("matchDate", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Prediction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get predictions by date range
    suspend fun getPredictionsByDateRange(startDate: Timestamp, endDate: Timestamp): List<Prediction> {
        return try {
            predictionsCollection
                .whereGreaterThanOrEqualTo("matchDate", startDate)
                .whereLessThanOrEqualTo("matchDate", endDate)
                .orderBy("matchDate", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Prediction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get predictions by result status
    suspend fun getPredictionsByResultStatus(status: Prediction.ResultStatus): List<Prediction> {
        return try {
            predictionsCollection
                .whereEqualTo("resultStatus", status.name)
                .orderBy("matchDate", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Prediction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get prediction by ID
    suspend fun getPredictionById(predictionId: String): Prediction? {
        return try {
            val document = predictionsCollection.document(predictionId).get().await()
            if (document.exists()) {
                document.toObject(Prediction::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Add new prediction
    suspend fun addPrediction(prediction: Prediction): String? {
        return try {
            val documentRef = predictionsCollection.document()
            val predictionWithId = prediction.copy(predictionId = documentRef.id)
            documentRef.set(predictionWithId).await()
            documentRef.id
        } catch (e: Exception) {
            null
        }
    }
    
    // Update prediction
    suspend fun updatePrediction(prediction: Prediction): Boolean {
        return try {
            val updatedPrediction = prediction.copy(updatedAt = Timestamp.now())
            predictionsCollection.document(prediction.predictionId)
                .set(updatedPrediction)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Update prediction result
    suspend fun updatePredictionResult(
        predictionId: String, 
        resultStatus: Prediction.ResultStatus,
        homeScore: Int?,
        awayScore: Int?
    ): Boolean {
        return try {
            val prediction = getPredictionById(predictionId) ?: return false
            
            val updatedMatchDetails = prediction.matchDetails.copy(
                homeScore = homeScore,
                awayScore = awayScore
            )
            
            val updatedPrediction = prediction.copy(
                resultStatus = resultStatus,
                matchDetails = updatedMatchDetails,
                updatedAt = Timestamp.now()
            )
            
            predictionsCollection.document(predictionId)
                .set(updatedPrediction)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Set prediction as Prediction of the Day
    suspend fun setPredictionOfTheDay(predictionId: String): Boolean {
        return try {
            // First, unset any existing POTD
            val existingPOTD = getPredictionOfTheDay()
            if (existingPOTD != null) {
                predictionsCollection.document(existingPOTD.predictionId)
                    .update("isPOTD", false)
                    .await()
            }
            
            // Then set the new POTD
            predictionsCollection.document(predictionId)
                .update(
                    "isPOTD", true,
                    "updatedAt", Timestamp.now()
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Delete prediction
    suspend fun deletePrediction(predictionId: String): Boolean {
        return try {
            predictionsCollection.document(predictionId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Get success rate statistics
    suspend fun getSuccessRateStatistics(): Map<String, Double> {
        val stats = mutableMapOf<String, Double>()
        
        try {
            val allPredictions = predictionsCollection
                .whereIn("resultStatus", listOf(
                    Prediction.ResultStatus.WIN.name, 
                    Prediction.ResultStatus.LOSS.name
                ))
                .get()
                .await()
                .toObjects(Prediction::class.java)
            
            if (allPredictions.isEmpty()) return stats
            
            // Overall success rate
            val totalCompleted = allPredictions.size
            val totalWins = allPredictions.count { it.isWin() }
            stats["overall"] = (totalWins.toDouble() / totalCompleted) * 100
            
            // Success rate by category
            val categories = allPredictions.map { it.category }.distinct()
            categories.forEach { category ->
                val categoryPredictions = allPredictions.filter { it.category == category }
                val categoryWins = categoryPredictions.count { it.isWin() }
                stats[category] = (categoryWins.toDouble() / categoryPredictions.size) * 100
            }
            
            // Success rate by confidence level
            for (i in 1..5) {
                val confidencePredictions = allPredictions.filter { it.confidenceLevel == i }
                if (confidencePredictions.isNotEmpty()) {
                    val confidenceWins = confidencePredictions.count { it.isWin() }
                    stats["confidence_$i"] = (confidenceWins.toDouble() / confidencePredictions.size) * 100
                }
            }
            
        } catch (e: Exception) {
            // Return empty stats on error
        }
        
        return stats
    }
}