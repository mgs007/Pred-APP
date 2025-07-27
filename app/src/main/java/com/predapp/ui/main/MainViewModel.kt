package com.predapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.predapp.model.User
import com.predapp.repository.UserRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    
    // LiveData for authentication state
    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated
    
    // LiveData for current user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    init {
        // Check if user is authenticated
        checkAuthState()
        
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            checkAuthState()
        }
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        _isAuthenticated.value = currentUser != null
        
        if (currentUser != null) {
            loadUserData(currentUser.uid)
        } else {
            _currentUser.value = null
        }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            _currentUser.value = user
        }
    }

    fun signOut() {
        userRepository.signOut()
        _isAuthenticated.value = false
        _currentUser.value = null
    }
}