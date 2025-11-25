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
import com.refugio.pawrescue.model.DocumentItem;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private Context context;
    private List<DocumentItem> documentList;
    private OnDocumentClickListener listener;

    public interface OnDocumentClickListener {
        void onDocumentClick(DocumentItem document);
    }

    public DocumentAdapter(Context context, List<DocumentItem> documentList, OnDocumentClickListener listener) {
        this.context = context;
        this.documentList = documentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el diseño de cada fila de documento
        // Si te sale error en R.layout.item_document, avísame para pasarte el XML
        View view = LayoutInflater.from(context).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        DocumentItem document = documentList.get(position);
        holder.bind(document, listener, context);
    }

    @Override
    public int getItemCount() {
        return documentList != null ? documentList.size() : 0;
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView tvDocName, tvDocStatus;
        ImageView ivDocIcon, ivStatusIcon;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asegúrate de tener estos IDs en tu item_document.xml
            tvDocName = itemView.findViewById(R.id.tvDocName);
            tvDocStatus = itemView.findViewById(R.id.tvDocStatus);
            ivDocIcon = itemView.findViewById(R.id.ivDocIcon);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
        }

        public void bind(DocumentItem document, OnDocumentClickListener listener, Context context) {
            tvDocName.setText(document.getNombre());
            tvDocStatus.setText(document.getEstado());

            // Cambiar iconos/colores según estado
            if (document.getEstado().equals("Subido") || document.getEstado().equals("Aprobado")) {
                ivStatusIcon.setImageResource(R.drawable.ic_check_circle); // Icono que ya tienes
                ivStatusIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                tvDocStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else {
                // Estado pendiente o vacío
                ivStatusIcon.setImageResource(R.drawable.ic_info); // Icono que ya tienes (o ic_upload)
                ivStatusIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
                tvDocStatus.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            }

            itemView.setOnClickListener(v -> listener.onDocumentClick(document));
        }
    }
}
