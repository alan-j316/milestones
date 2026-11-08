package com.cs360.weighttracker.presentation.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.cs360.weighttracker.domain.models.DailyWeight

class WeightItemAdapter(
    private val onWeightDelete: (id: Long) -> Unit
) : ListAdapter<DailyWeight, WeightItemViewHolder>(WeightItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WeightItemViewHolder(layoutInflater, parent)
    }

    override fun onBindViewHolder(holder: WeightItemViewHolder, position: Int) {
        // Get the at the position index and bind it to the view holder
        val weight = getItem(position)
        holder.bind(weight, onWeightDelete)
    }



    class WeightItemDiffCallback : DiffUtil.ItemCallback<DailyWeight>() {
        override fun areItemsTheSame(
            oldItem: DailyWeight,
            newItem: DailyWeight
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DailyWeight,
            newItem: DailyWeight
        ): Boolean {
            return oldItem == newItem
        }

    }
}
