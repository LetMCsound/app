package com.letmc.sound.ui.beats;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Beat;
import com.letmc.sound.databinding.FragmentBeatsBinding;
import java.util.List;
import retrofit2.*;

public class BeatsFragment extends Fragment {

    private FragmentBeatsBinding binding;
    private BeatsAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentBeatsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        adapter = new BeatsAdapter(beat -> {
            Bundle args = new Bundle();
            args.putString("beatId", beat.id);
            Navigation.findNavController(view).navigate(R.id.action_beats_to_detail, args);
        });
        binding.recyclerBeats.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerBeats.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(this::loadBeats);
        loadBeats();
    }

    private void loadBeats() {
        binding.swipeRefresh.setRefreshing(true);
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getBeats("*,musicians(name,avatar_url,slug)", "created_at.desc", "0-19")
            .enqueue(new Callback<List<Beat>>() {
                @Override public void onResponse(Call<List<Beat>> c, Response<List<Beat>> r) {
                    if (binding == null || !isAdded()) return;
                    binding.swipeRefresh.setRefreshing(false);
                    if (r.isSuccessful() && r.body() != null) adapter.setBeats(r.body());
                    else Toast.makeText(requireContext(), "Error cargando beats", Toast.LENGTH_SHORT).show();
                }
                @Override public void onFailure(Call<List<Beat>> c, Throwable t) {
                    if (binding == null || !isAdded()) return;
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
