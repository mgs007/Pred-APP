package com.predapp.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.predapp.model.Prediction
import com.predapp.repository.PredictionRepository
import kotlinx.coroutines.launch

class ResultsViewModel : ViewModel() {

    private val predictionRepository = PredictionRepository()
    
    // LiveData for results
    private val _results = MutableLiveData<List<Prediction>>()
    val results: LiveData<List<Prediction>> = _results
    
    // Statistics
    private val _statistics = MutableLiveData<PredictionStatistics>()
    val statistics: LiveData<PredictionStatistics> = _statistics
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Filter state
    private var selectedCategory: String? = null
    
    fun loadResults() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Get completed predictions (both wins and losses)
                val completedPredictions = predictionRepository.getPredictionsByResultStatus(
                    listOf(Prediction.ResultStatus.WIN, Prediction.ResultStatus.LOSS)
                )
                
                // Apply category filter if selected
                val filteredResults = if (selectedCategory != null) {
                    completedPredictions.filter { it.category == selectedCategory }
                } else {
                    completedPredictions
                }
                
                // Sort by date (newest first)
                val sortedResults = filteredResults.sortedByDescending { it.matchDate }
                
                _results.value = sortedResults
                
                // Calculate statistics
                calculateStatistics(filteredResults)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load results: ${e.message}"
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setCategory(category: String?) {
        if (selectedCategory != category) {
            selectedCategory = category
            loadResults()
        }
    }
    
    private fun calculateStatistics(predictions: List<Prediction>) {
        val totalPredictions = predictions.size
        val wins = predictions.count { it.resultStatus == Prediction.ResultStatus.WIN }
        val losses = predictions.count { it.resultStatus == Prediction.ResultStatus.LOSS }
        
        val winRate = if (totalPredictions > 0) {
            (wins.toFloat() / totalPredictions * 100).toInt()
        } else {
            0
        }
        
        _statistics.value = PredictionStatistics(
            totalPredictions = totalPredictions,
            wins = wins,
            losses = losses,
            winRate = winRate
        )
    }
    
    data class PredictionStatistics(
        val totalPredictions: Int = 0,
        val wins: Int = 0,
        val losses: Int = 0,
        val winRate: Int = 0
    )
}