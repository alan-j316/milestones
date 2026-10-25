package com.cs360.weighttracker.components;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cs360.weighttracker.R;
import com.cs360.weighttracker.domain.models.DailyWeight;
import com.cs360.weighttracker.utils.DateFormatter;
import com.cs360.weighttracker.utils.WeightFormatter;

public class WeightItemViewHolder extends RecyclerView.ViewHolder {

    private final TextView weightTextView;
    private final TextView dateTextView;
    private final ImageButton deleteWeightItemButton;

    public WeightItemViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.weight_item, parent, false));
        this.weightTextView = itemView.findViewById(R.id.tvHistoryItemWeight);
        this.dateTextView = itemView.findViewById(R.id.tvHistoryItemDate);
        this.deleteWeightItemButton = itemView.findViewById(R.id.btnHistoryItemDelete);
    }

    public void bind(DailyWeight weight, WeightItemRemoveListener onDeleteListener) {
        dateTextView.setText(DateFormatter.fromMillis(weight.getDateTimeMillis()));

//        weightTextView.setText(String.valueOf(weight.getUserWeight()));
        weightTextView.setText(WeightFormatter.format(weight.userWeight));
        // Attach the on delete method call.
        deleteWeightItemButton.setOnClickListener(view -> onDeleteListener.onRemove(weight.id));
    }
}
