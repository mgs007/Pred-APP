package com.predapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.predapp.model.User
import com.predapp.repository.UserRepository
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()
    
    // LiveData for authentication state
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    // LiveData for current user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    // Phone verification
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    init {
        // Check if user is already signed in
        auth.currentUser?.let { firebaseUser ->
            _authState.value = AuthState.Authenticated
            loadUserData(firebaseUser.uid)
        } ?: run {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    // Register with email and password
    fun registerWithEmail(email: String, password: String, phone: String) {
        _authState.value = AuthState.Loading
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        createUserInFirestore(firebaseUser, email, phone)
                    } else {
                        _authState.value = AuthState.Error("User creation failed")
                    }
                } else {
                    handleAuthError(task.exception)
                }
            }
    }
    
    // Login with email and password
    fun loginWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        _authState.value = AuthState.Authenticated
                        loadUserData(firebaseUser.uid)
                    } else {
                        _authState.value = AuthState.Error("Login failed")
                    }
                } else {
                    handleAuthError(task.exception)
                }
            }
    }
    
    // Start phone verification
    fun startPhoneVerification(phoneNumber: String, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        _authState.value = AuthState.Loading
        
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    
    // Verify phone code
    fun verifyPhoneCode(code: String) {
        val verificationId = storedVerificationId
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            _authState.value = AuthState.Error("Verification ID not found")
        }
    }
    
    // Sign in with phone credential
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        _authState.value = AuthState.Loading
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        _authState.value = AuthState.Authenticated
                        loadUserData(firebaseUser.uid)
                    } else {
                        _authState.value = AuthState.Error("Login failed")
                    }
                } else {
                    handleAuthError(task.exception)
                }
            }
    }
    
    // Reset password
    fun resetPassword(email: String) {
        _authState.value = AuthState.Loading
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.PasswordResetSent
                } else {
                    handleAuthError(task.exception)
                }
            }
    }
    
    // Sign out
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _currentUser.value = null
    }
    
    // Create user in Firestore
    private fun createUserInFirestore(firebaseUser: FirebaseUser, email: String, phone: String) {
        viewModelScope.launch {
            val user = User(
                userId = firebaseUser.uid,
                email = email,
                phone = phone,
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
            
            val success = userRepository.createOrUpdateUser(user)
            if (success) {
                _authState.value = AuthState.Authenticated
                _currentUser.value = user
            } else {
                _authState.value = AuthState.Error("Failed to create user profile")
            }
        }
    }
    
    // Load user data from Firestore
    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            _currentUser.value = user
        }
    }
    
    // Handle authentication errors
    private fun handleAuthError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid credentials"
            is FirebaseAuthUserCollisionException -> "Email already in use"
            else -> exception?.message ?: "Authentication failed"
        }
        _authState.value = AuthState.Error(errorMessage)
    }
    
    // Store verification ID for phone auth
    fun setVerificationId(verificationId: String) {
        storedVerificationId = verificationId
    }
    
    // Store resend token for phone auth
    fun setResendToken(token: PhoneAuthProvider.ForceResendingToken) {
        resendToken = token
    }
    
    // Authentication states
    sealed class AuthState {
        object Unauthenticated : AuthState()
        object Authenticated : AuthState()
        object Loading : AuthState()
        object PasswordResetSent : AuthState()
        data class Error(val message: String) : AuthState()
        data class PhoneVerificationSent(val verificationId: String) : AuthState()
    }
}