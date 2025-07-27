package com.predapp.ui.predictions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.predapp.R
import com.predapp.model.Prediction
import java.text.SimpleDateFormat
import java.util.*

class PredictionAdapter(
    private var predictions: List<Prediction>,
    private val isPotd: Boolean = false
) : RecyclerView.Adapter<PredictionAdapter.PredictionViewHolder>() {

    private var onItemClickListener: ((Prediction) -> Unit)? = null

    fun setOnItemClickListener(listener: (Prediction) -> Unit) {
        onItemClickListener = listener
    }

    fun updatePredictions(newPredictions: List<Prediction>) {
        predictions = newPredictions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prediction, parent, false)
        return PredictionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        val prediction = predictions[position]
        holder.bind(prediction, isPotd)
    }

    override fun getItemCount(): Int = predictions.size

    inner class PredictionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.predictionCard)
        private val matchTeams: TextView = itemView.findViewById(R.id.matchTeams)
        private val matchDate: TextView = itemView.findViewById(R.id.matchDate)
        private val predictionType: TextView = itemView.findViewById(R.id.predictionType)
        private val confidenceLevel: TextView = itemView.findViewById(R.id.confidenceLevel)
        private val resultStatus: TextView = itemView.findViewById(R.id.resultStatus)
        private val odds: TextView = itemView.findViewById(R.id.odds)
        private val potdBadge: View = itemView.findViewById(R.id.potdBadge)

        fun bind(prediction: Prediction, isPotd: Boolean) {
            // Set match details
            matchTeams.text = "${prediction.matchDetails.homeTeam} vs ${prediction.matchDetails.awayTeam}"
            
            // Format and set match date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            matchDate.text = dateFormat.format(prediction.matchDate)
            
            // Set prediction type
            predictionType.text = prediction.predictionType.toString()
            
            // Set confidence level
            confidenceLevel.text = "${prediction.confidenceLevel}/10"
            
            // Set odds if available
            odds.text = if (prediction.odds > 0) "Odds: ${prediction.odds}" else ""
            odds.visibility = if (prediction.odds > 0) View.VISIBLE else View.GONE
            
            // Set result status with appropriate color
            resultStatus.text = prediction.resultStatus.toString()
            val context = itemView.context
            when (prediction.resultStatus) {
                Prediction.ResultStatus.WIN -> {
                    resultStatus.setTextColor(ContextCompat.getColor(context, R.color.win))
                }
                Prediction.ResultStatus.LOSS -> {
                    resultStatus.setTextColor(ContextCompat.getColor(context, R.color.loss))
                }
                Prediction.ResultStatus.PENDING -> {
                    resultStatus.setTextColor(ContextCompat.getColor(context, R.color.pending))
                }
            }
            
            // Show POTD badge if applicable
            potdBadge.visibility = if (prediction.isPOTD || isPotd) View.VISIBLE else View.GONE
            
            // Set card stroke color for premium predictions
            if (prediction.isPremium) {
                cardView.strokeColor = ContextCompat.getColor(context, R.color.premium)
                cardView.strokeWidth = context.resources.getDimensionPixelSize(R.dimen.premium_stroke_width)
            } else {
                cardView.strokeWidth = 0
            }
            
            // Set click listener
            itemView.setOnClickListener {
                onItemClickListener?.invoke(prediction)
            }
        }
    }
}