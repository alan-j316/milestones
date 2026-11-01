package com.cs360.weighttracker.presentation.components

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cs360.weighttracker.domain.models.DailyWeight

class WeightItemAdapter(
    private val dailyWeights: MutableList<DailyWeight>,
    private val onDeleteWeightListener: WeightItemRemoveListener?
) : RecyclerView.Adapter<WeightItemViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WeightItemViewHolder(layoutInflater, parent)
    }

    override fun onBindViewHolder(holder: WeightItemViewHolder, position: Int) {
        // Get the at the position index and bind it to the view holder
        val weight = dailyWeights[position]
        holder.bind(weight, this.onDeleteWeightListener)
        holder.itemView.tag = weight.id
    }

    /**
     * Update the recyclerview's data with new data and refresh the view.
     * 
     * @param newWeights The weights to update with.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newWeights: MutableList<DailyWeight?>) {
        // TODO: Update
        this.dailyWeights.clear()
        this.dailyWeights.addAll(newWeights)
        // Since we are removing and reintroducing all the item we need to call this to refresh the dataset.
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dailyWeights.size
    }
}
