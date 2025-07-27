package com.predapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.predapp.R
import com.predapp.databinding.FragmentProfileBinding
import com.predapp.ui.auth.AuthActivity
import com.predapp.ui.subscription.SubscriptionActivity
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProfileViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupButtons()
        observeViewModel()
        
        // Load user data
        viewModel.loadUserData()
    }
    
    private fun setupButtons() {
        // Upgrade to premium button
        binding.upgradeToPremiumButton.setOnClickListener {
            // Navigate to subscription screen
            val intent = Intent(requireContext(), SubscriptionActivity::class.java)
            startActivity(intent)
        }
        
        // Edit profile button
        binding.editProfileButton.setOnClickListener {
            // Show edit profile dialog
            showEditProfileDialog()
        }
        
        // Sign out button
        binding.signOutButton.setOnClickListener {
            viewModel.signOut()
        }
    }
    
    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_profile, null)
        
        val displayNameInput = dialogView.findViewById<TextInputEditText>(R.id.displayNameInput)
        
        // Pre-fill with current display name if available
        viewModel.userData.value?.let { user ->
            if (user.displayName.isNotEmpty()) {
                displayNameInput.setText(user.displayName)
            }
        }
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()
        
        // Set up button click listeners
        dialogView.findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<MaterialButton>(R.id.saveButton).setOnClickListener {
            val newDisplayName = displayNameInput.text.toString().trim()
            if (newDisplayName.isNotEmpty()) {
                viewModel.updateUserProfile(newDisplayName)
                dialog.dismiss()
            } else {
                displayNameInput.error = "Display name cannot be empty"
            }
        }
        
        dialog.show()
    }
    
    private fun observeViewModel() {
        // Observe user data
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                // Set user email and display name
                binding.userEmail.text = if (it.displayName.isNotEmpty()) {
                    it.displayName
                } else {
                    it.email
                }
                
                // Set premium status
                val isPremium = it.isPremium
                binding.premiumStatusText.text = if (isPremium) {
                    getString(R.string.premium_member)
                } else {
                    getString(R.string.free_user)
                }
                
                // Set premium badge visibility
                binding.premiumBadge.visibility = if (isPremium) View.VISIBLE else View.GONE
                
                // Set subscription info
                if (isPremium && it.subscriptionExpiry != null) {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val expiryDate = dateFormat.format(it.subscriptionExpiry?.toDate())
                    val daysRemaining = it.daysRemainingInSubscription()
                    
                    binding.subscriptionInfoCard.visibility = View.VISIBLE
                    binding.subscriptionExpiryDate.text = expiryDate
                    binding.daysRemainingText.text = resources.getQuantityString(
                        R.plurals.days_remaining,
                        daysRemaining,
                        daysRemaining
                    )
                    
                    // Hide upgrade button if user is already premium
                    binding.upgradeToPremiumButton.visibility = View.GONE
                } else {
                    binding.subscriptionInfoCard.visibility = View.GONE
                    binding.upgradeToPremiumButton.visibility = View.VISIBLE
                }
            }
        }
        
        // Observe authentication state
        viewModel.isAuthenticated.observe(viewLifecycleOwner) { isAuthenticated ->
            if (!isAuthenticated) {
                // Navigate to auth screen
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.error)
                    .setMessage(it)
                    .setPositiveButton(R.string.ok, null)
                    .show()
                
                viewModel.clearErrorMessage()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}