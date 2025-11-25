package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Message;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Si te marca error en R.layout.item_chat_message, avísame para dártelo
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message, context);
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerLayout;
        LinearLayout messageBubble;
        TextView tvMessageContent;
        TextView tvMessageTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // IDs del layout item_chat_message.xml
            containerLayout = itemView.findViewById(R.id.messageContainer);
            messageBubble = itemView.findViewById(R.id.messageBubble);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        public void bind(Message message, Context context) {
            tvMessageContent.setText(message.getContenido());

            // Formatear hora
            if (message.getFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                tvMessageTime.setText(sdf.format(message.getFecha()));
            }

            // Cambiar alineación y color según si el mensaje es mío o del admin
            if (message.isEsMio()) {
                containerLayout.setGravity(Gravity.END); // Derecha
                // Color azul o primario para mis mensajes
                messageBubble.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.purple_200));
            } else {
                containerLayout.setGravity(Gravity.START); // Izquierda
                // Color gris para mensajes recibidos
                messageBubble.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
            }
        }
    }
}
