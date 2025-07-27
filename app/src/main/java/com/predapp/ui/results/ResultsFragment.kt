package com.predapp.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.predapp.R
import com.predapp.databinding.FragmentResultsBinding
import com.predapp.model.Prediction
import com.predapp.ui.predictions.PredictionAdapter

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ResultsViewModel by viewModels()
    private lateinit var resultsAdapter: PredictionAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFilterSpinner()
        setupSwipeRefresh()
        setupChart()
        observeViewModel()
        
        // Initial data load
        viewModel.loadResults()
    }
    
    private fun setupRecyclerView() {
        resultsAdapter = PredictionAdapter(emptyList())
        resultsAdapter.setOnItemClickListener { prediction ->
            // Navigate to prediction detail
            // TODO: Implement navigation to prediction detail
        }
        
        binding.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = resultsAdapter
        }
    }
    
    private fun setupFilterSpinner() {
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
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadResults()
        }
    }
    
    private fun setupChart() {
        val chart = binding.resultsChart
        
        // Configure chart appearance
        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setScaleEnabled(false)
        
        // Configure X axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)
        
        // Configure left Y axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f
        
        // Configure right Y axis
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
    }
    
    private fun updateChart(winCount: Int, lossCount: Int) {
        val chart = binding.resultsChart
        
        // Create bar entries
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, winCount.toFloat()))
        entries.add(BarEntry(1f, lossCount.toFloat()))
        
        // Create dataset
        val dataSet = BarDataSet(entries, "Results")
        dataSet.colors = listOf(
            resources.getColor(R.color.win, null),
            resources.getColor(R.color.loss, null)
        )
        
        // Create bar data
        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        
        // Set labels for X axis
        val labels = arrayOf("Wins", "Losses")
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        // Set data to chart
        chart.data = barData
        chart.invalidate()
    }
    
    private fun observeViewModel() {
        // Observe results
        viewModel.results.observe(viewLifecycleOwner) { results ->
            resultsAdapter.updatePredictions(results)
            binding.emptyStateLayout.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        }
        
        // Observe statistics
        viewModel.statistics.observe(viewLifecycleOwner) { stats ->
            binding.winRateText.text = "${stats.winRate}%"
            binding.totalPredictionsText.text = stats.totalPredictions.toString()
            binding.winsText.text = stats.wins.toString()
            binding.lossesText.text = stats.losses.toString()
            
            // Update chart
            updateChart(stats.wins, stats.losses)
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