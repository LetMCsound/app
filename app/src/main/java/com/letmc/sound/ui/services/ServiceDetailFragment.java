package com.letmc.sound.ui.services;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.*;
import com.letmc.sound.databinding.FragmentServiceDetailBinding;
import com.letmc.sound.utils.SessionManager;
import java.util.*;
import retrofit2.*;

public class ServiceDetailFragment extends Fragment {

    private FragmentServiceDetailBinding binding;
    private Musician musician;
    private String serviceType;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentServiceDetailBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        String musicianId = getArguments() != null ? getArguments().getString("musicianId") : null;
        serviceType = getArguments() != null ? getArguments().getString("serviceType", "filmmaker") : "filmmaker";
        if (musicianId != null) loadMusician(musicianId);
        binding.btnContact.setOnClickListener(v -> startChat(view));

        String label = "filmmaker".equals(serviceType) ? "Film Maker" : "Diseñador Gráfico";
        binding.tvServiceType.setText(label);
    }

    private void loadMusician(String userId) {
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getMusicianById("*", "eq." + userId)
            .enqueue(new Callback<List<Musician>>() {
                @Override public void onResponse(Call<List<Musician>> c, Response<List<Musician>> r) {
                    if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                        musician = r.body().get(0);
                        populateUI();
                    }
                }
                @Override public void onFailure(Call<List<Musician>> c, Throwable t) {}
            });
    }

    private void populateUI() {
        binding.tvName.setText(musician.name != null ? musician.name : "Sin nombre");
        binding.tvBio.setText(musician.bio != null ? musician.bio : "");
        binding.tvLocation.setText(musician.location != null ? "📍 " + musician.location : "");
        if (musician.avatarUrl != null) Glide.with(this).load(musician.avatarUrl).circleCrop().into(binding.ivAvatar);
        if (musician.coverUrl != null)  Glide.with(this).load(musician.coverUrl).centerCrop().into(binding.ivCover);
    }

    private void startChat(View view) {
        SessionManager session = new SessionManager(requireContext());
        if (!session.isLoggedIn()) {
            Toast.makeText(requireContext(), "Inicia sesión para contactar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (musician == null) return;
        if (musician.userId.equals(session.getUserId())) {
            Toast.makeText(requireContext(), "No puedes contactarte a ti mismo", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnContact.setEnabled(false);
        Map<String, Object> conv = new HashMap<>();
        conv.put("buyer_id",  session.getUserId());
        conv.put("seller_id", musician.userId);
        conv.put("type",      serviceType);
        conv.put("status",    "negotiating");

        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.createConversation(conv).enqueue(new Callback<Conversation>() {
            @Override public void onResponse(Call<Conversation> c, Response<Conversation> r) {
                binding.btnContact.setEnabled(true);
                if (r.isSuccessful() && r.body() != null) {
                    Bundle args = new Bundle();
                    args.putString("conversationId", r.body().id);
                    Navigation.findNavController(view).navigate(R.id.action_serviceDetail_to_chat, args);
                } else {
                    Toast.makeText(requireContext(), "Error iniciando conversación", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Conversation> c, Throwable t) {
                binding.btnContact.setEnabled(true);
                Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
