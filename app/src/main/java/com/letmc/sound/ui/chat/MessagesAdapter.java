package com.letmc.sound.ui.chat;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.letmc.sound.R;
import com.letmc.sound.data.model.Message;
import java.util.*;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.VH> {
    private final List<Message> items = new ArrayList<>();
    private final String myUserId;
    private static final int VIEW_MINE = 1, VIEW_OTHER = 2;

    public MessagesAdapter(String myUserId) { this.myUserId = myUserId; }
    public void setMessages(List<Message> list) { items.clear(); items.addAll(list); notifyDataSetChanged(); }

    @Override public int getItemViewType(int pos) {
        return myUserId != null && myUserId.equals(items.get(pos).senderId) ? VIEW_MINE : VIEW_OTHER;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int type) {
        int layout = type == VIEW_MINE ? R.layout.item_message_mine : R.layout.item_message_other;
        return new VH(LayoutInflater.from(p.getContext()).inflate(layout, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos)); }
    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView content;
        VH(View v) { super(v); content = v.findViewById(R.id.tv_content); }
        void bind(Message m) { content.setText(m.content); }
    }
}
