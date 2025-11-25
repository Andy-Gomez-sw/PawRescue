package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Transaccion;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para el RecyclerView que muestra las transacciones financieras.
 */
public class FinanzasAdapter extends RecyclerView.Adapter<FinanzasAdapter.TransaccionViewHolder> {

    private final Context context;
    private List<Transaccion> transaccionesList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'de' MMMM, yyyy", new Locale("es", "ES"));
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public FinanzasAdapter(Context context) {
        this.context = context;
        this.transaccionesList = new ArrayList<>();
    }

    public void setTransaccionesList(List<Transaccion> transaccionesList) {
        this.transaccionesList = transaccionesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransaccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_finance_transaction, parent, false);
        return new TransaccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaccionViewHolder holder, int position) {
        Transaccion transaccion = transaccionesList.get(position);

        String tipo = transaccion.getTipo();
        String montoFormateado = currencyFormat.format(transaccion.getMonto());
        int color;
        int icon;
        String prefix;

        if ("Donacion".equalsIgnoreCase(tipo)) {
            // Ingreso (Verde)
            color = ContextCompat.getColor(context, R.color.primary_green);
            icon = R.drawable.ic_add;
            prefix = "+";
            holder.tvTitle.setText(String.format("Donación (%s)", transaccion.getCategoria()));
        } else {
            // Gasto (Rojo)
            color = ContextCompat.getColor(context, R.color.status_error);
            icon = R.drawable.ic_remove;
            prefix = "-";
            holder.tvTitle.setText(String.format("Gasto (%s)", transaccion.getCategoria()));
        }

        // Monto y Color
        holder.tvAmount.setText(prefix + montoFormateado);
        holder.tvAmount.setTextColor(color);

        // Icono
        holder.ivIcon.setImageResource(icon);
        holder.ivIcon.setColorFilter(color);

        // Fecha
        if (transaccion.getFecha() != null) {
            holder.tvDate.setText(dateFormat.format(transaccion.getFecha().toDate()));
        } else {
            holder.tvDate.setText("Fecha no disponible");
        }
    }

    @Override
    public int getItemCount() {
        return transaccionesList.size();
    }

    static class TransaccionViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvDate;
        final TextView tvAmount;

        public TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_type_icon);
            tvTitle = itemView.findViewById(R.id.tv_transaction_title);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
        }
    }
}