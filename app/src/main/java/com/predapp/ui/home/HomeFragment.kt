package com.predapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.predapp.databinding.FragmentHomeBinding
import com.predapp.model.Prediction
import com.predapp.ui.predictions.PredictionAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var potdAdapter: PredictionAdapter
    private lateinit var freePredictionsAdapter: PredictionAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupSwipeRefresh()
        observeViewModel()
        
        // Initial data load
        viewModel.loadPredictionOfTheDay()
        viewModel.loadFreePredictions()
    }
    
    private fun setupRecyclerViews() {
        // Setup POTD adapter
        potdAdapter = PredictionAdapter(listOf(), true)
        binding.potdRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = potdAdapter
        }
        
        // Setup free predictions adapter
        freePredictionsAdapter = PredictionAdapter(listOf(), false)
        binding.freePredictionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = freePredictionsAdapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadPredictionOfTheDay()
            viewModel.loadFreePredictions()
        }
    }
    
    private fun observeViewModel() {
        // Observe Prediction of the Day
        viewModel.predictionOfTheDay.observe(viewLifecycleOwner) { potd ->
            potd?.let {
                potdAdapter.updatePredictions(listOf(it))
                binding.potdCard.visibility = View.VISIBLE
            } ?: run {
                binding.potdCard.visibility = View.GONE
            }
        }
        
        // Observe free predictions
        viewModel.freePredictions.observe(viewLifecycleOwner) { predictions ->
            freePredictionsAdapter.updatePredictions(predictions)
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