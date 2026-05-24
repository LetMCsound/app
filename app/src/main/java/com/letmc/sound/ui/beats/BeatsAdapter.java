package com.letmc.sound.ui.beats;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.letmc.sound.R;
import com.letmc.sound.data.model.Beat;
import java.util.ArrayList;
import java.util.List;

public class BeatsAdapter extends RecyclerView.Adapter<BeatsAdapter.BeatViewHolder> {

    public interface OnBeatClickListener { void onClick(Beat beat); }

    private final List<Beat> beats = new ArrayList<>();
    private final OnBeatClickListener listener;

    public BeatsAdapter(OnBeatClickListener listener) { this.listener = listener; }

    public void setBeats(List<Beat> list) { beats.clear(); beats.addAll(list); notifyDataSetChanged(); }

    @NonNull @Override
    public BeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beat, parent, false);
        return new BeatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BeatViewHolder h, int pos) { h.bind(beats.get(pos)); }

    @Override public int getItemCount() { return beats.size(); }

    class BeatViewHolder extends RecyclerView.ViewHolder {
        ImageView cover; TextView title, artist, price, genre, bpm;
        BeatViewHolder(View v) {
            super(v);
            cover  = v.findViewById(R.id.iv_cover);
            title  = v.findViewById(R.id.tv_title);
            artist = v.findViewById(R.id.tv_artist);
            price  = v.findViewById(R.id.tv_price);
            genre  = v.findViewById(R.id.tv_genre);
            bpm    = v.findViewById(R.id.tv_bpm);
        }
        void bind(Beat beat) {
            title.setText(beat.title);
            price.setText(beat.priceStandard > 0 ? String.format("€%.2f", beat.priceStandard) : "Consultar");
            genre.setText(beat.genre != null ? beat.genre : "");
            bpm.setText(beat.bpm != null ? beat.bpm + " BPM" : "");
            artist.setText(beat.sellerName != null ? beat.sellerName : "");
            if (beat.coverUrl != null) {
                Glide.with(itemView).load(beat.coverUrl)
                    .placeholder(R.drawable.placeholder_cover).into(cover);
            }
            itemView.setOnClickListener(v -> listener.onClick(beat));
        }
    }
}
