package com.letmc.sound.ui.lyrics;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.letmc.sound.R;
import com.letmc.sound.data.model.Lyric;
import java.util.*;

public class LyricsAdapter extends RecyclerView.Adapter<LyricsAdapter.VH> {

    public interface OnClickListener { void onClick(Lyric l); }
    private final List<Lyric> items = new ArrayList<>();
    private final OnClickListener listener;
    public LyricsAdapter(OnClickListener l) { listener = l; }
    public void setLyrics(List<Lyric> list) { items.clear(); items.addAll(list); notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_lyric, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos)); }
    @Override public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView avatar; TextView title, artist, price, genre, snippet;
        VH(View v) {
            super(v);
            avatar  = v.findViewById(R.id.iv_avatar);
            title   = v.findViewById(R.id.tv_title);
            artist  = v.findViewById(R.id.tv_artist);
            price   = v.findViewById(R.id.tv_price);
            genre   = v.findViewById(R.id.tv_genre);
            snippet = v.findViewById(R.id.tv_snippet);
        }
        void bind(Lyric l) {
            title.setText(l.title);
            price.setText(l.priceStandard > 0 ? String.format("€%.2f", l.priceStandard) : "Negociable");
            genre.setText(l.genre != null ? l.genre : "");
            snippet.setText(l.description != null ? l.description : "");
            artist.setText(l.sellerName != null ? l.sellerName : "");
            itemView.setOnClickListener(v -> listener.onClick(l));
        }
    }
}
