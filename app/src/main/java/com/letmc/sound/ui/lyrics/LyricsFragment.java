package com.letmc.sound.ui.lyrics;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Lyric;
import com.letmc.sound.databinding.FragmentLyricsBinding;
import java.util.List;
import retrofit2.*;

public class LyricsFragment extends Fragment {

    private FragmentLyricsBinding binding;
    private LyricsAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentLyricsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        adapter = new LyricsAdapter(lyric -> {
            Bundle args = new Bundle();
            args.putString("lyricId", lyric.id);
            Navigation.findNavController(view).navigate(R.id.action_lyrics_to_detail, args);
        });
        binding.recyclerLyrics.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerLyrics.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(this::loadLyrics);
        loadLyrics();
    }

    private void loadLyrics() {
        binding.swipeRefresh.setRefreshing(true);
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getLyrics("*", "created_at.desc", "eq.true", 20)
            .enqueue(new Callback<List<Lyric>>() {
                @Override public void onResponse(Call<List<Lyric>> c, Response<List<Lyric>> r) {
                    if (binding == null || !isAdded()) return;
                    binding.swipeRefresh.setRefreshing(false);
                    if (r.isSuccessful() && r.body() != null) {
                        adapter.setLyrics(r.body());
                    } else {
                        Toast.makeText(requireContext(), "Error cargando letras", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<List<Lyric>> c, Throwable t) {
                    if (binding == null || !isAdded()) return;
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
