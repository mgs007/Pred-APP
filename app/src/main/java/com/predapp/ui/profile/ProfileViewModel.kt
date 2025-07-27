package com.predapp.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.predapp.model.User
import com.predapp.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()
    
    // LiveData for user data
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData
    
    // Authentication state
    private val _isAuthenticated = MutableLiveData<Boolean>(true)
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun loadUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentUser = userRepository.getCurrentFirebaseUser()
                if (currentUser != null) {
                    val user = userRepository.getUserById(currentUser.uid)
                    _userData.value = user
                    _isAuthenticated.value = true
                } else {
                    _isAuthenticated.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load user data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateUserProfile(displayName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentUser = userRepository.getCurrentFirebaseUser()
                if (currentUser != null) {
                    userRepository.updateUserProfile(currentUser.uid, displayName)
                    loadUserData() // Reload user data
                } else {
                    _isAuthenticated.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                _isAuthenticated.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to sign out: ${e.message}"
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}