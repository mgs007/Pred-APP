package com.predapp.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.predapp.model.PlanType
import com.predapp.repository.SubscriptionRepository
import com.predapp.repository.UserRepository
import kotlinx.coroutines.launch

class SubscriptionViewModel : ViewModel() {

    private val subscriptionRepository = SubscriptionRepository()
    private val userRepository = UserRepository()
    
    // Selected plan and payment details
    private var selectedPlan: PlanType = PlanType.MONTHLY
    private var paymentReference: String = ""
    private var amount: Double = 0.0
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Submission result
    private val _submissionResult = MutableLiveData<String?>()
    val submissionResult: LiveData<String?> = _submissionResult
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun setSelectedPlan(planType: PlanType) {
        selectedPlan = planType
    }
    
    fun setPaymentReference(reference: String) {
        paymentReference = reference
    }
    
    fun setAmount(value: Double) {
        amount = value
    }
    
    fun submitPayment(transactionNumber: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentUser = userRepository.getCurrentFirebaseUser()
                if (currentUser != null) {
                    // Create subscription request
                    subscriptionRepository.createSubscriptionRequest(
                        userId = currentUser.uid,
                        planType = selectedPlan,
                        paymentReference = paymentReference,
                        amount = amount,
                        transactionNumber = transactionNumber
                    )
                    
                    _submissionResult.value = "Payment submitted successfully. Your subscription will be activated once payment is verified."
                } else {
                    _errorMessage.value = "User not authenticated. Please log in again."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to submit payment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}