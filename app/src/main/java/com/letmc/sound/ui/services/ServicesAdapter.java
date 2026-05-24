package com.letmc.sound.ui.services;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.letmc.sound.R;
import com.letmc.sound.data.model.Musician;
import java.util.*;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.VH> {
    public interface OnClickListener { void onClick(Musician m); }
    private final List<Musician> items = new ArrayList<>();
    private final OnClickListener listener;
    public ServicesAdapter(OnClickListener l) { listener = l; }
    public void setMusicians(List<Musician> list) { items.clear(); items.addAll(list); notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_service, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos)); }
    @Override public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView avatar, cover; TextView name, bio, location, tags;
        VH(View v) {
            super(v);
            avatar   = v.findViewById(R.id.iv_avatar);
            cover    = v.findViewById(R.id.iv_cover);
            name     = v.findViewById(R.id.tv_name);
            bio      = v.findViewById(R.id.tv_bio);
            location = v.findViewById(R.id.tv_location);
            tags     = v.findViewById(R.id.tv_tags);
        }
        void bind(Musician m) {
            name.setText(m.name != null ? m.name : "Sin nombre");
            bio.setText(m.bio != null ? m.bio : "");
            location.setText(m.location != null ? "📍 " + m.location : "");
            tags.setText(m.tags != null ? m.tags : "");
            if (m.avatarUrl != null) Glide.with(itemView).load(m.avatarUrl).circleCrop().into(avatar);
            if (m.coverUrl != null)  Glide.with(itemView).load(m.coverUrl).centerCrop().into(cover);
            itemView.setOnClickListener(v -> listener.onClick(m));
        }
    }
}
