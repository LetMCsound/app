package com.letmc.sound.ui.beats;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.*;
import retrofit2.*;

public class BeatsFragment extends Fragment {

    private FragmentBeatsBinding binding;
    private BeatsAdapter adapter;
    private List<Beat> allBeats = new ArrayList<>();

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
        binding.swipeRefresh.setColorSchemeResources(android.R.color.holo_purple);
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF18181f);
        binding.swipeRefresh.setOnRefreshListener(this::loadBeats);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable e) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterBeats(s.toString());
            }
        });

        loadBeats();
    }

    private void loadBeats() {
        binding.swipeRefresh.setRefreshing(true);
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getBeats("*", "created_at.desc", "eq.true", 50)
            .enqueue(new Callback<List<Beat>>() {
                @Override public void onResponse(Call<List<Beat>> c, Response<List<Beat>> r) {
                    if (binding == null || !isAdded()) return;
                    binding.swipeRefresh.setRefreshing(false);
                    if (r.isSuccessful() && r.body() != null) {
                        allBeats = r.body();
                        adapter.setBeats(allBeats);
                    } else {
                        Toast.makeText(requireContext(), "Error cargando beats", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<List<Beat>> c, Throwable t) {
                    if (binding == null || !isAdded()) return;
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void filterBeats(String query) {
        if (query.isEmpty()) { adapter.setBeats(allBeats); return; }
        List<Beat> filtered = new ArrayList<>();
        String q = query.toLowerCase();
        for (Beat b : allBeats) {
            if ((b.title != null && b.title.toLowerCase().contains(q)) ||
                (b.genre != null && b.genre.toLowerCase().contains(q))) {
                filtered.add(b);
            }
        }
        adapter.setBeats(filtered);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
