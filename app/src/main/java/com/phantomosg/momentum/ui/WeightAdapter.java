package com.phantomosg.momentum.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.phantomosg.momentum.R;
import com.phantomosg.momentum.databinding.ItemWeightRecordBinding;
import com.phantomosg.momentum.model.WeightRecord;

public final class WeightAdapter extends ListAdapter<WeightRecord, WeightAdapter.ViewHolder> {
    public interface Listener {
        void onEdit(WeightRecord record);
        void onDelete(WeightRecord record);
    }

    private final Listener listener;

    public WeightAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(
                    @NonNull WeightRecord oldItem,
                    @NonNull WeightRecord newItem
            ) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(
                    @NonNull WeightRecord oldItem,
                    @NonNull WeightRecord newItem
            ) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeightRecordBinding binding = ItemWeightRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemWeightRecordBinding binding;

        ViewHolder(ItemWeightRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WeightRecord record) {
            binding.dateText.setText(record.getDate());
            binding.weightText.setText(
                    binding.getRoot().getContext().getString(
                            R.string.pounds_value, record.getWeight()
                    )
            );
            binding.editButton.setOnClickListener(view -> listener.onEdit(record));
            binding.deleteButton.setOnClickListener(view -> listener.onDelete(record));
        }
    }
}
