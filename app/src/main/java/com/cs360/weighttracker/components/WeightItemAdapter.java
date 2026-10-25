package com.cs360.weighttracker.components;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cs360.weighttracker.domain.models.DailyWeight;

import java.util.List;

public class WeightItemAdapter extends RecyclerView.Adapter<WeightItemViewHolder> {
    private final List<DailyWeight> dailyWeights;
    private final WeightItemRemoveListener onDeleteWeightListener;

    public WeightItemAdapter(List<DailyWeight> dailyWeights, WeightItemRemoveListener onDeleteWeightListener) {
        this.dailyWeights = dailyWeights;
        this.onDeleteWeightListener = onDeleteWeightListener;
    }

    @NonNull
    @Override
    public WeightItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new WeightItemViewHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightItemViewHolder holder, int position) {
        // Get the at the position index and bind it to the view holder
        DailyWeight weight = dailyWeights.get(position);
        holder.bind(weight, this.onDeleteWeightListener);
        holder.itemView.setTag(weight.id);
    }

    /**
     * Update the recyclerview's data with new data and refresh the view.
     *
     * @param newWeights The weights to update with.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<DailyWeight> newWeights) {
        this.dailyWeights.clear();
        this.dailyWeights.addAll(newWeights);
        // Since we are removing and reintroducing all the item we need to call this to refresh the dataset.
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dailyWeights.size();
    }
}
