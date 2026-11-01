package com.cs360.weighttracker.presentation.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs360.weighttracker.R
import com.cs360.weighttracker.domain.models.DailyWeight
import com.cs360.weighttracker.presentation.utils.DateFormatter.fromMillis
import com.cs360.weighttracker.presentation.utils.WeightFormatter.format

class WeightItemViewHolder(inflater: LayoutInflater, parent: ViewGroup?) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.weight_item, parent, false)) {
    private var weightTextView: TextView = itemView.findViewById(R.id.tvHistoryItemWeight)
    private val dateTextView: TextView = itemView.findViewById(R.id.tvHistoryItemDate)
    private val deleteWeightItemButton: ImageButton =
        itemView.findViewById(R.id.btnHistoryItemDelete)


    fun bind(weight: DailyWeight, onDeleteListener: WeightItemRemoveListener) {
        dateTextView.text = fromMillis(weight.dateTimeMillis)

        //        weightTextView.setText(String.valueOf(weight.getUserWeight()));
        weightTextView.text = format(weight.userWeight)
        // Attach the on delete method call.
        deleteWeightItemButton.setOnClickListener { _: View? ->
            onDeleteListener.onRemove(
                weight.id
            )
        }
    }
}
