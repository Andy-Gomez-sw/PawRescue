package com.refugio.pawrescue.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.TimeSlot;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    private List<TimeSlot> timeSlots;
    private OnTimeSlotClickListener listener;
    private TimeSlot selectedSlot;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlot slot);
    }

    public TimeSlotAdapter(List<TimeSlot> timeSlots, OnTimeSlotClickListener listener) {
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    public void setSelectedSlot(TimeSlot slot) {
        if (selectedSlot != null) {
            selectedSlot.setSelected(false);
        }
        selectedSlot = slot;
        if (selectedSlot != null) {
            selectedSlot.setSelected(true);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot slot = timeSlots.get(position);
        holder.bind(slot);
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardSlot;
        TextView tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            cardSlot = itemView.findViewById(R.id.cardSlot);
            tvTime = itemView.findViewById(R.id.tvTime);

            cardSlot.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    TimeSlot slot = timeSlots.get(position);
                    if (slot.isAvailable() && listener != null) {
                        listener.onTimeSlotClick(slot);
                    }
                }
            });
        }

        void bind(TimeSlot slot) {
            tvTime.setText(slot.getDisplayTime());

            if (!slot.isAvailable()) {
                // Ocupado
                cardSlot.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.gray_light));
                tvTime.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.gray_dark));
                cardSlot.setEnabled(false);
            } else if (slot.isSelected()) {
                // Seleccionado
                cardSlot.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.primary_orange));
                tvTime.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                cardSlot.setEnabled(true);
            } else {
                // Disponible
                cardSlot.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                tvTime.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                cardSlot.setEnabled(true);
            }
        }
    }
}