package com.letmc.sound.ui.services;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.tabs.TabLayout;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Musician;
import com.letmc.sound.databinding.FragmentServicesBinding;
import java.util.ArrayList;
import java.util.List;
import retrofit2.*;

public class ServicesFragment extends Fragment {

    private FragmentServicesBinding binding;
    private ServicesAdapter adapter;
    private String currentType = "filmmaker";
    private final List<Musician> allMusicians = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentServicesBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        adapter = new ServicesAdapter(musician -> {
            Bundle args = new Bundle();
            args.putString("musicianId",  musician.userId);
            args.putString("serviceType", currentType);
            Navigation.findNavController(view).navigate(R.id.action_services_to_detail, args);
        });
        binding.recyclerServices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerServices.setAdapter(adapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentType = tab.getPosition() == 0 ? "filmmaker" : "graphic";
                filterAndShow();
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });
        loadAllMusicians();
    }

    /** Carga todos los músicos y luego filtra por tab en cliente */
    private void loadAllMusicians() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);

        // Primero intentar con filtro de categoría
        api.getMusiciansByTag("*", "cs.{" + currentType + "}", "name.asc")
            .enqueue(new Callback<List<Musician>>() {
                @Override public void onResponse(Call<List<Musician>> c, Response<List<Musician>> r) {
                    if (binding == null || !isAdded()) return;
                    if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                        binding.progressBar.setVisibility(View.GONE);
                        allMusicians.clear();
                        allMusicians.addAll(r.body());
                        filterAndShow();
                    } else {
                        // Fallback: cargar todos sin filtro
                        loadAllWithoutFilter(api);
                    }
                }
                @Override public void onFailure(Call<List<Musician>> c, Throwable t) {
                    if (binding == null || !isAdded()) return;
                    loadAllWithoutFilter(api);
                }
            });
    }

    private void loadAllWithoutFilter(SupabaseApi api) {
        api.getAllMusicians("*", "name.asc")
            .enqueue(new Callback<List<Musician>>() {
                @Override public void onResponse(Call<List<Musician>> c, Response<List<Musician>> r) {
                    if (binding == null || !isAdded()) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (r.isSuccessful() && r.body() != null) {
                        allMusicians.clear();
                        allMusicians.addAll(r.body());
                        filterAndShow();
                    } else {
                        Toast.makeText(requireContext(), "Error cargando servicios", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<List<Musician>> c, Throwable t) {
                    if (binding == null || !isAdded()) return;
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
                }
            });
    }

    /** Filtra la lista en memoria según el tab activo */
    private void filterAndShow() {
        if (allMusicians.isEmpty()) { adapter.setMusicians(allMusicians); return; }
        List<Musician> filtered = new ArrayList<>();
        for (Musician m : allMusicians) {
            if (m.categories != null && !m.categories.isEmpty()) {
                for (String cat : m.categories) {
                    if (cat != null && cat.toLowerCase().contains(
                            currentType.equals("filmmaker") ? "film" : "graph")) {
                        filtered.add(m); break;
                    }
                }
            }
        }
        // Si ninguno tiene categoría coincidente, mostrar todos
        adapter.setMusicians(filtered.isEmpty() ? allMusicians : filtered);
    }
}
