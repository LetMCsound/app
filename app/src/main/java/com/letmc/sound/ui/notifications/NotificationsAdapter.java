package com.letmc.sound.ui.notifications;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.letmc.sound.R;
import com.letmc.sound.data.model.Notification;
import java.util.*;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {
    private final List<Notification> items = new ArrayList<>();
    public void setNotifications(List<Notification> list) { items.clear(); items.addAll(list); notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_notification, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos)); }
    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, body;
        VH(View v) { super(v); title = v.findViewById(R.id.tv_title); body = v.findViewById(R.id.tv_body); }
        void bind(Notification n) { title.setText(n.title); body.setText(n.body); itemView.setAlpha(n.isRead ? 0.6f : 1f); }
    }
}
