package com.letmc.sound.ui.chat;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.letmc.sound.R;
import com.letmc.sound.data.model.Conversation;
import java.util.*;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.VH> {
    public interface OnClickListener { void onClick(Conversation c); }
    private final List<Conversation> items = new ArrayList<>();
    private final OnClickListener listener;
    public ConversationsAdapter(OnClickListener l) { listener = l; }
    public void setConversations(List<Conversation> list) { items.clear(); items.addAll(list); notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_conversation, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos)); }
    @Override public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView avatar; TextView name, lastMsg, status;
        VH(View v) { super(v); avatar = v.findViewById(R.id.iv_avatar); name = v.findViewById(R.id.tv_name); lastMsg = v.findViewById(R.id.tv_last_message); status = v.findViewById(R.id.tv_status); }
        void bind(Conversation c) {
            if (c.seller != null) { name.setText(c.seller.name); if (c.seller.avatarUrl != null) Glide.with(itemView).load(c.seller.avatarUrl).circleCrop().into(avatar); }
            lastMsg.setText(c.lastMessage != null ? c.lastMessage : "Sin mensajes");
            status.setText(c.status != null ? c.status : "");
            itemView.setOnClickListener(v -> listener.onClick(c));
        }
    }
}
