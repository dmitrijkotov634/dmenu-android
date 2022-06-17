package com.dm.dmenu.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dm.dmenu.databinding.ItemBinding;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private final List<Item> items;

    private final Context context;
    private final Callback callback;

    private int currentPosition = 0;

    public Adapter(Context context, List<Item> items, Callback callback) {
        this.context = context;

        this.items = items;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBinding rowItem = ItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getBinding().title.setText(items.get(position).getTitle());
        holder.getBinding().getRoot().setBackgroundColor(position == currentPosition ? 0xFF005577 : 0xFF222222);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void callOnClick(int index) {
        callback.onItemSelected(items.get(index));
    }

    public void left() {
        if (currentPosition == 0)
            return;

        notifyItemChanged(currentPosition--);
        notifyItemChanged(currentPosition);
        callback.scrollTo(currentPosition);
    }

    public void right() {
        if (currentPosition == getItemCount() - 1)
            return;

        notifyItemChanged(currentPosition++);
        notifyItemChanged(currentPosition);
        callback.scrollTo(currentPosition);
    }

    public int getPosition() {
        return currentPosition;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ItemBinding binding;

        public ViewHolder(ItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            binding.getRoot().setOnClickListener(this);
        }

        public ItemBinding getBinding() {
            return binding;
        }

        @Override
        public void onClick(View view) {
            callback.onItemSelected(items.get(getLayoutPosition()));
        }
    }
}
