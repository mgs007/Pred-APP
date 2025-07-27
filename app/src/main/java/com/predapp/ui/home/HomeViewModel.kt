package com.predapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.predapp.model.Prediction
import com.predapp.repository.PredictionRepository
import com.predapp.repository.UserRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val predictionRepository = PredictionRepository()
    private val userRepository = UserRepository()
    
    // LiveData for Prediction of the Day
    private val _predictionOfTheDay = MutableLiveData<Prediction?>()
    val predictionOfTheDay: LiveData<Prediction?> = _predictionOfTheDay
    
    // LiveData for free predictions
    private val _freePredictions = MutableLiveData<List<Prediction>>()
    val freePredictions: LiveData<List<Prediction>> = _freePredictions
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // User premium status
    private val _isPremium = MutableLiveData<Boolean>(false)
    val isPremium: LiveData<Boolean> = _isPremium
    
    init {
        checkUserPremiumStatus()
    }
    
    fun loadPredictionOfTheDay() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val potd = predictionRepository.getPredictionOfTheDay()
                _predictionOfTheDay.value = potd
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load Prediction of the Day: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadFreePredictions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val predictions = predictionRepository.getFreePredictions(limit = 5)
                _freePredictions.value = predictions
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load free predictions: ${e.message}"
                _freePredictions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun checkUserPremiumStatus() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentFirebaseUser()
                if (currentUser != null) {
                    val user = userRepository.getUserById(currentUser.uid)
                    _isPremium.value = user?.isPremium ?: false
                } else {
                    _isPremium.value = false
                }
            } catch (e: Exception) {
                _isPremium.value = false
            }
        }
    }
}