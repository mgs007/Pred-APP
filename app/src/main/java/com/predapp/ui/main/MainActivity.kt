package com.predapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.predapp.R
import com.predapp.databinding.ActivityMainBinding
import com.predapp.ui.auth.AuthActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        observeViewModel()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Setup bottom navigation with nav controller
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun observeViewModel() {
        // Observe authentication state
        viewModel.isAuthenticated.observe(this) { isAuthenticated ->
            if (!isAuthenticated) {
                // User is not authenticated, navigate to auth screen
                navigateToAuth()
            }
        }
        
        // Observe user data
        viewModel.currentUser.observe(this) { user ->
            // Update UI based on user data if needed
        }
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}