package com.predapp.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.predapp.R
import com.predapp.ui.auth.AuthActivity
import com.predapp.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2000L // 2 seconds
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        auth = FirebaseAuth.getInstance()
        
        // Delay for splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthState()
        }, SPLASH_DELAY)
    }
    
    private fun checkAuthState() {
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            // User is signed in, navigate to main activity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // User is not signed in, navigate to auth activity
            startActivity(Intent(this, AuthActivity::class.java))
        }
        
        // Close splash activity
        finish()
    }
}