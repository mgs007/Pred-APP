package com.predapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.predapp.R
import com.predapp.databinding.ActivityAuthBinding
import com.predapp.ui.main.MainActivity
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()
    
    // Phone verification callbacks
    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-verification completed
            hideLoading()
            // Sign in with the credential
            // viewModel.signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            hideLoading()
            Toast.makeText(this@AuthActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // Save verification ID and resending token for later use
            viewModel.setVerificationId(verificationId)
            viewModel.setResendToken(token)
            
            // Show verification code input
            showVerificationCodeInput()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Toggle between login and register views
        binding.tvToggleAuth.setOnClickListener {
            toggleAuthMode()
        }
        
        // Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (validateLoginInputs(email, password)) {
                viewModel.loginWithEmail(email, password)
            }
        }
        
        // Register button click
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            
            if (validateRegisterInputs(email, password, confirmPassword, phone)) {
                // Start phone verification
                viewModel.startPhoneVerification(phone, phoneAuthCallbacks)
            }
        }
        
        // Verify code button click
        binding.btnVerify.setOnClickListener {
            val code = binding.etVerificationCode.text.toString().trim()
            if (code.length == 6) {
                viewModel.verifyPhoneCode(code)
            } else {
                Toast.makeText(this, "Please enter a valid verification code", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Resend code button click
        binding.btnResendCode.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (phone.isNotEmpty()) {
                viewModel.startPhoneVerification(phone, phoneAuthCallbacks)
            }
        }
        
        // Forgot password click
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
        
        // Input validation
        binding.etEmail.doOnTextChanged { _, _, _, _ -> clearErrors() }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> clearErrors() }
        binding.etConfirmPassword.doOnTextChanged { _, _, _, _ -> clearErrors() }
        binding.etPhone.doOnTextChanged { _, _, _, _ -> clearErrors() }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> showLoading()
                is AuthViewModel.AuthState.Authenticated -> {
                    hideLoading()
                    navigateToMain()
                }
                is AuthViewModel.AuthState.Unauthenticated -> {
                    hideLoading()
                    // Already on auth screen
                }
                is AuthViewModel.AuthState.Error -> {
                    hideLoading()
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is AuthViewModel.AuthState.PasswordResetSent -> {
                    hideLoading()
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show()
                }
                is AuthViewModel.AuthState.PhoneVerificationSent -> {
                    hideLoading()
                    showVerificationCodeInput()
                }
            }
        }
    }

    private fun toggleAuthMode() {
        val isLoginMode = binding.loginContainer.visibility == View.VISIBLE
        
        if (isLoginMode) {
            // Switch to register mode
            binding.loginContainer.visibility = View.GONE
            binding.registerContainer.visibility = View.VISIBLE
            binding.tvToggleAuth.text = getString(R.string.already_have_account)
            binding.tvAuthTitle.text = getString(R.string.register)
        } else {
            // Switch to login mode
            binding.loginContainer.visibility = View.VISIBLE
            binding.registerContainer.visibility = View.GONE
            binding.tvToggleAuth.text = getString(R.string.dont_have_account)
            binding.tvAuthTitle.text = getString(R.string.login)
        }
        
        // Hide verification code input
        binding.verificationContainer.visibility = View.GONE
        
        // Clear inputs
        binding.etEmail.text?.clear()
        binding.etPassword.text?.clear()
        binding.etConfirmPassword.text?.clear()
        binding.etPhone.text?.clear()
        binding.etVerificationCode.text?.clear()
        
        clearErrors()
    }

    private fun validateLoginInputs(email: String, password: String): Boolean {
        var isValid = true
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            isValid = false
        }
        
        if (password.isEmpty() || password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }
        
        return isValid
    }

    private fun validateRegisterInputs(email: String, password: String, confirmPassword: String, phone: String): Boolean {
        var isValid = true
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            isValid = false
        }
        
        if (password.isEmpty() || password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }
        
        if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }
        
        if (phone.isEmpty() || phone.length < 10) {
            binding.tilPhone.error = "Please enter a valid phone number"
            isValid = false
        }
        
        return isValid
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        binding.tilPhone.error = null
    }

    private fun showVerificationCodeInput() {
        binding.loginContainer.visibility = View.GONE
        binding.registerContainer.visibility = View.GONE
        binding.verificationContainer.visibility = View.VISIBLE
        binding.tvAuthTitle.text = getString(R.string.verification_code)
        binding.tvToggleAuth.visibility = View.GONE
    }

    private fun showForgotPasswordDialog() {
        val email = binding.etEmail.text.toString().trim()
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            return
        }
        
        // Send password reset email
        viewModel.resetPassword(email)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        binding.btnRegister.isEnabled = false
        binding.btnVerify.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.btnRegister.isEnabled = true
        binding.btnVerify.isEnabled = true
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}