package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.TimelineStep;
import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private Context context;
    private List<TimelineStep> timelineList;

    public TimelineAdapter(Context context, List<TimelineStep> timelineList) {
        this.context = context;
        this.timelineList = timelineList;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        TimelineStep item = timelineList.get(position);

        // Si es el último elemento, indicamos al item que oculte la línea inferior
        if (position == timelineList.size() - 1) {
            item.setLast(true);
        }

        holder.bind(item, context);
    }

    @Override
    public int getItemCount() {
        return timelineList != null ? timelineList.size() : 0;
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvDescription;
        View viewLine;
        View viewDot;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asegúrate de que estos IDs existan en tu item_timeline.xml
            tvTitle = itemView.findViewById(R.id.tvTimelineTitle);
            tvDate = itemView.findViewById(R.id.tvTimelineDate);
            tvDescription = itemView.findViewById(R.id.tvTimelineDescription);
            viewLine = itemView.findViewById(R.id.viewTimelineLine);
            viewDot = itemView.findViewById(R.id.viewTimelineDot);
        }

        public void bind(TimelineStep item, Context context) {
            tvTitle.setText(item.getTitle());
            tvDate.setText(item.getDate());
            tvDescription.setText(item.getDescription());

            // Lógica de colores (Completado vs Pendiente)
            if (item.isCompleted()) {
                viewDot.setBackgroundResource(R.drawable.shape_circle_green); // Necesitarás este drawable o un color
                viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.status_available)); // Verde
                tvTitle.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            } else {
                viewDot.setBackgroundResource(R.drawable.shape_circle_gray); // Necesitarás este drawable o un color
                viewLine.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                tvTitle.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            }

            // Ocultar línea si es el último
            if (item.isLast()) {
                viewLine.setVisibility(View.INVISIBLE);
            } else {
                viewLine.setVisibility(View.VISIBLE);
            }
        }
    }
}