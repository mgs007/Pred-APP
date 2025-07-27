package com.predapp.ui.predictions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.predapp.R
import com.predapp.databinding.FragmentPredictionsBinding
import com.predapp.model.Prediction

class PredictionsFragment : Fragment() {

    private var _binding: FragmentPredictionsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PredictionsViewModel by viewModels()
    private lateinit var predictionsAdapter: PredictionAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFilterSpinners()
        setupSwipeRefresh()
        observeViewModel()
        setupPremiumToggle()
        
        // Initial data load
        viewModel.loadPredictions()
    }
    
    private fun setupRecyclerView() {
        predictionsAdapter = PredictionAdapter(emptyList())
        predictionsAdapter.setOnItemClickListener { prediction ->
            // Navigate to prediction detail
            // TODO: Implement navigation to prediction detail
        }
        
        binding.predictionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = predictionsAdapter
        }
    }
    
    private fun setupFilterSpinners() {
        // Setup category filter
        val categories = arrayOf("All Categories", "Football", "Basketball", "Tennis", "Hockey", "Baseball")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = if (position == 0) null else categories[position]
                viewModel.setCategory(selectedCategory)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Setup status filter
        val statuses = arrayOf("All Statuses", "Pending", "Win", "Loss")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.statusSpinner.adapter = statusAdapter
        binding.statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = when (position) {
                    0 -> null
                    1 -> Prediction.ResultStatus.PENDING
                    2 -> Prediction.ResultStatus.WIN
                    3 -> Prediction.ResultStatus.LOSS
                    else -> null
                }
                viewModel.setResultStatus(selectedStatus)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadPredictions()
        }
    }
    
    private fun setupPremiumToggle() {
        binding.premiumSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPremiumFilter(isChecked)
        }
        
        // Observe premium status to enable/disable the switch
        viewModel.isPremiumUser.observe(viewLifecycleOwner) { isPremium ->
            binding.premiumSwitch.isEnabled = isPremium
            if (!isPremium) {
                binding.premiumSwitch.isChecked = false
                binding.premiumUnavailableText.visibility = View.VISIBLE
            } else {
                binding.premiumUnavailableText.visibility = View.GONE
            }
        }
    }
    
    private fun observeViewModel() {
        // Observe predictions
        viewModel.predictions.observe(viewLifecycleOwner) { predictions ->
            predictionsAdapter.updatePredictions(predictions)
            binding.emptyStateLayout.visibility = if (predictions.isEmpty()) View.VISIBLE else View.GONE
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            // Show error message if needed
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}