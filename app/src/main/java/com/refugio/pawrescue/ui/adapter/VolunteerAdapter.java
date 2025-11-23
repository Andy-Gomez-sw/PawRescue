package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Importado para manejar colores
import androidx.recyclerview.widget.RecyclerView;

import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {

    private final Context context;
    private List<Usuario> volunteersList;
    private final OnVolunteerClickListener listener; // <<-- CAMPO AÑADIDO

    // 1. Interfaz de Listener para manejar el clic fuera del Adapter
    public interface OnVolunteerClickListener {
        void onVolunteerClick(Usuario usuario);
    }

    // Constructor modificado para aceptar el listener
    public VolunteerAdapter(Context context, OnVolunteerClickListener listener) {
        this.context = context;
        this.volunteersList = new ArrayList<>();
        this.listener = listener; // <<-- ASIGNACIÓN DEL LISTENER
    }

    public void setVolunteersList(List<Usuario> volunteersList) {
        this.volunteersList = volunteersList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que este layout (item_volunteer) sea correcto
        View view = LayoutInflater.from(context).inflate(R.layout.item_volunteer, parent, false);
        return new VolunteerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolunteerViewHolder holder, int position) {
        Usuario volunteer = volunteersList.get(position);

        holder.tvNombre.setText(volunteer.getNombre());
        holder.tvCorreo.setText(volunteer.getCorreo());
        holder.tvRol.setText(volunteer.getRol());

        // Formateo del ID Numérico
        holder.tvIdNumerico.setText("ID: #" + String.format(Locale.US, "%04d", volunteer.getIdNumerico()));

        // Uso de ContextCompat para colores
        boolean esActivo = volunteer.isEstadoActivo();
        holder.tvEstado.setText(esActivo ? "✓ Activo" : "✗ Inactivo");
        holder.tvEstado.setTextColor(ContextCompat.getColor(
                context,
                esActivo ? R.color.status_success : R.color.status_error
        ));

        // 2. Lógica del Clic en el Item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVolunteerClick(volunteer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return volunteersList.size();
    }

    static class VolunteerViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvCorreo, tvRol, tvIdNumerico, tvEstado;

        public VolunteerViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asumiendo que estos IDs existen en tu layout item_volunteer.xml
            tvNombre = itemView.findViewById(R.id.tv_volunteer_name);
            tvCorreo = itemView.findViewById(R.id.tv_volunteer_email);
            tvRol = itemView.findViewById(R.id.tv_volunteer_role);
            tvIdNumerico = itemView.findViewById(R.id.tv_volunteer_id);
            tvEstado = itemView.findViewById(R.id.tv_volunteer_status);
        }
    }
}