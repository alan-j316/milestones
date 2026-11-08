package com.cs360.weighttracker.presentation.components

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cs360.weighttracker.domain.models.DailyWeight

class WeightItemAdapter(
//    private val dailyWeights: List<DailyWeight>,
//    private val onDeleteWeightListener: WeightItemRemoveListener
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
        // TODO: REMOVE
//        holder.itemView.tag = weight.id
    }

    // TODO: REMOVE
//    /**
//     * Update the recyclerview's data with new data and refresh the view.
//     *
//     * @param newWeights The weights to update with.
//     */
//    // TODO: Rework and remove warning
//    @SuppressLint("NotifyDataSetChanged")
//    fun updateData(newWeights: List<DailyWeight>) {
//        // TODO: Update
//        this.dailyWeights.clear()
//        this.dailyWeights.addAll(newWeights)
//        // Since we are removing and reintroducing all the item we need to call this to refresh the dataset.
//        notifyDataSetChanged()
//    }


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
