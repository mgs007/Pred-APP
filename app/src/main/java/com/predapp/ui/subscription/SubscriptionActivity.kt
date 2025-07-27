package com.predapp.ui.subscription

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.predapp.R
import com.predapp.databinding.ActivitySubscriptionBinding
import com.predapp.model.PlanType
import java.text.NumberFormat
import java.util.Locale

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var viewModel: SubscriptionViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this).get(SubscriptionViewModel::class.java)
        
        setupToolbar()
        setupPlanSpinner()
        setupPaymentSubmission()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.upgrade_to_premium)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupPlanSpinner() {
        val planTypes = PlanType.values().map { it.getPlanName() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, planTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.planTypeSpinner.adapter = adapter
        binding.planTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPlan = PlanType.values()[position]
                updatePlanDetails(selectedPlan)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        
        // Set default selection to MONTHLY
        binding.planTypeSpinner.setSelection(PlanType.MONTHLY.ordinal)
    }
    
    private fun updatePlanDetails(planType: PlanType) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val amount = when (planType) {
            PlanType.MONTHLY -> 9.99
            PlanType.QUARTERLY -> 24.99
            PlanType.YEARLY -> 89.99
        }
        
        binding.planAmount.text = currencyFormat.format(amount)
        
        // Update payment reference
        val reference = "PRED-${planType.name}-${System.currentTimeMillis()}"
        binding.paymentReferenceText.text = reference
        viewModel.setPaymentReference(reference)
        viewModel.setSelectedPlan(planType)
        viewModel.setAmount(amount)
    }
    
    private fun setupPaymentSubmission() {
        binding.submitPaymentButton.setOnClickListener {
            val transactionNumber = binding.transactionNumberInput.text.toString().trim()
            if (transactionNumber.isEmpty()) {
                binding.transactionNumberLayout.error = "Transaction number is required"
                return@setOnClickListener
            }
            
            viewModel.submitPayment(transactionNumber)
        }
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
        
        // Observe submission result
        viewModel.submissionResult.observe(this) { result ->
            if (result != null) {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                if (result == getString(R.string.payment_pending)) {
                    // Close activity after successful submission
                    finish()
                }
            }
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }
}