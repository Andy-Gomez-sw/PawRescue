package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Usuario;
import java.util.ArrayList;
import java.util.List;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {

    private final Context context;
    private List<Usuario> volunteersList;

    public VolunteerAdapter(Context context) {
        this.context = context;
        this.volunteersList = new ArrayList<>();
    }

    public void setVolunteersList(List<Usuario> volunteersList) {
        this.volunteersList = volunteersList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_volunteer, parent, false);
        return new VolunteerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolunteerViewHolder holder, int position) {
        Usuario volunteer = volunteersList.get(position);

        holder.tvNombre.setText(volunteer.getNombre());
        holder.tvCorreo.setText(volunteer.getCorreo());
        holder.tvRol.setText(volunteer.getRol());
        holder.tvIdNumerico.setText("ID: #" + String.format("%04d", volunteer.getIdNumerico()));

        holder.tvEstado.setText(volunteer.isEstadoActivo() ? "✓ Activo" : "✗ Inactivo");
        holder.tvEstado.setTextColor(context.getResources().getColor(
                volunteer.isEstadoActivo() ? R.color.status_success : R.color.status_error
        ));
    }

    @Override
    public int getItemCount() {
        return volunteersList.size();
    }

    static class VolunteerViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvCorreo, tvRol, tvIdNumerico, tvEstado;

        public VolunteerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_volunteer_name);
            tvCorreo = itemView.findViewById(R.id.tv_volunteer_email);
            tvRol = itemView.findViewById(R.id.tv_volunteer_role);
            tvIdNumerico = itemView.findViewById(R.id.tv_volunteer_id);
            tvEstado = itemView.findViewById(R.id.tv_volunteer_status);
        }
    }
}