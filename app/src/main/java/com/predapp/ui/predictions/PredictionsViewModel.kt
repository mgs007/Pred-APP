package com.predapp.ui.predictions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.predapp.model.Prediction
import com.predapp.repository.PredictionRepository
import com.predapp.repository.UserRepository
import kotlinx.coroutines.launch

class PredictionsViewModel : ViewModel() {

    private val predictionRepository = PredictionRepository()
    private val userRepository = UserRepository()
    
    // LiveData for predictions
    private val _predictions = MutableLiveData<List<Prediction>>()
    val predictions: LiveData<List<Prediction>> = _predictions
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // User premium status
    private val _isPremiumUser = MutableLiveData<Boolean>(false)
    val isPremiumUser: LiveData<Boolean> = _isPremiumUser
    
    // Filter states
    private var selectedCategory: String? = null
    private var selectedResultStatus: Prediction.ResultStatus? = null
    private var showPremiumOnly: Boolean = false
    
    init {
        checkUserPremiumStatus()
    }
    
    fun loadPredictions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val predictions = if (showPremiumOnly && _isPremiumUser.value == true) {
                    predictionRepository.getPremiumPredictions()
                } else {
                    predictionRepository.getFreePredictions(limit = 50)
                }
                
                // Apply filters
                val filteredPredictions = predictions.filter { prediction ->
                    (selectedCategory == null || prediction.category == selectedCategory) &&
                    (selectedResultStatus == null || prediction.resultStatus == selectedResultStatus)
                }
                
                _predictions.value = filteredPredictions
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load predictions: ${e.message}"
                _predictions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setCategory(category: String?) {
        if (selectedCategory != category) {
            selectedCategory = category
            loadPredictions()
        }
    }
    
    fun setResultStatus(status: Prediction.ResultStatus?) {
        if (selectedResultStatus != status) {
            selectedResultStatus = status
            loadPredictions()
        }
    }
    
    fun setPremiumFilter(showPremium: Boolean) {
        if (showPremiumOnly != showPremium) {
            showPremiumOnly = showPremium
            loadPredictions()
        }
    }
    
    private fun checkUserPremiumStatus() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentFirebaseUser()
                if (currentUser != null) {
                    val user = userRepository.getUserById(currentUser.uid)
                    _isPremiumUser.value = user?.isPremium ?: false
                } else {
                    _isPremiumUser.value = false
                }
            } catch (e: Exception) {
                _isPremiumUser.value = false
            }
        }
    }
}