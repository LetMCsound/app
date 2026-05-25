package com.letmc.sound.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.*;
import com.letmc.sound.databinding.FragmentProfileBinding;
import com.letmc.sound.ui.auth.AuthActivity;
import com.letmc.sound.ui.beats.BeatsAdapter;
import com.letmc.sound.ui.lyrics.LyricsAdapter;
import com.letmc.sound.utils.SessionManager;
import java.util.List;
import retrofit2.*;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private BeatsAdapter beatsAdapter;
    private LyricsAdapter lyricsAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentProfileBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        SessionManager session = new SessionManager(requireContext());
        if (!session.isLoggedIn()) {
            Toast.makeText(requireContext(), "Inicia sesión para ver tu perfil", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configurar RecyclerViews para contenido propio
        beatsAdapter = new BeatsAdapter(beat -> {
            Bundle args = new Bundle();
            args.putString("beatId", beat.id);
            Navigation.findNavController(view).navigate(R.id.action_profile_to_beatDetail, args);
        });
        binding.rvMyBeats.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyBeats.setAdapter(beatsAdapter);

        lyricsAdapter = new LyricsAdapter(lyric -> {
            Bundle args = new Bundle();
            args.putString("lyricId", lyric.id);
            try {
                Navigation.findNavController(view).navigate(R.id.action_profile_to_lyricDetail, args);
            } catch (Exception ignored) {}
        });
        binding.rvMyLyrics.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyLyrics.setAdapter(lyricsAdapter);

        loadProfile(session.getUserId());
        loadUserContent(session.getUserId());

        binding.btnEditProfile.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.action_profile_to_editProfile));

        binding.btnUploadBeat.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.action_profile_to_uploadBeat));

        binding.btnSalesHistory.setOnClickListener(v -> {
            try {
                Navigation.findNavController(view).navigate(R.id.action_profile_to_salesHistory);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Historial de compras próximamente", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            session.clearSession();
            startActivity(new Intent(requireActivity(), AuthActivity.class));
            requireActivity().finish();
        });
    }

    private void loadProfile(String userId) {
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getMusicianById("*", "eq." + userId).enqueue(new Callback<List<Musician>>() {
            @Override public void onResponse(Call<List<Musician>> c, Response<List<Musician>> r) {
                if (binding == null || !isAdded()) return;
                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty())
                    populateUI(r.body().get(0));
            }
            @Override public void onFailure(Call<List<Musician>> c, Throwable t) {}
        });
    }

    private void loadUserContent(String userId) {
        if (binding == null) return;
        binding.progressContent.setVisibility(View.VISIBLE);

        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);

        // Cargar beats del usuario
        api.getBeatsBySeller("*", "eq." + userId, "created_at.desc")
            .enqueue(new Callback<List<Beat>>() {
                @Override public void onResponse(Call<List<Beat>> c, Response<List<Beat>> r) {
                    if (binding == null || !isAdded()) return;
                    if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                        beatsAdapter.setBeats(r.body());
                        binding.tvBeatsLabel.setVisibility(View.VISIBLE);
                        binding.rvMyBeats.setVisibility(View.VISIBLE);
                    }
                    checkContentDone();
                }
                @Override public void onFailure(Call<List<Beat>> c, Throwable t) {
                    if (binding == null || !isAdded()) return;
                    checkContentDone();
                }
            });

        // Cargar letras del usuario
        api.getLyricsBySeller("*", "eq." + userId)
            .enqueue(new Callback<List<Lyric>>() {
                @Override public void onResponse(Call<List<Lyric>> c, Response<List<Lyric>> r) {
                    if (binding == null || !isAdded()) return;
                    if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                        lyricsAdapter.setLyrics(r.body());
                        binding.tvLyricsLabel.setVisibility(View.VISIBLE);
                        binding.rvMyLyrics.setVisibility(View.VISIBLE);
                    }
                    checkContentDone();
                }
                @Override public void onFailure(Call<List<Lyric>> c, Throwable t) {
                    if (binding == null || !isAdded()) return;
                    checkContentDone();
                }
            });
    }

    private int contentCallsFinished = 0;

    private void checkContentDone() {
        if (binding == null) return;
        contentCallsFinished++;
        if (contentCallsFinished >= 2) {
            binding.progressContent.setVisibility(View.GONE);
            boolean hasContent = binding.rvMyBeats.getVisibility() == View.VISIBLE
                    || binding.rvMyLyrics.getVisibility() == View.VISIBLE;
            if (!hasContent) binding.tvNoContent.setVisibility(View.VISIBLE);
        }
    }

    private void populateUI(Musician m) {
        binding.tvName.setText(m.name != null ? m.name : "Sin nombre");
        binding.tvBio.setText(m.bio != null ? m.bio : "");
        binding.tvLocation.setText(m.location != null ? "📍 " + m.location : "");
        if (m.avatarUrl != null) Glide.with(this).load(m.avatarUrl).circleCrop().into(binding.ivAvatar);
        if (m.coverUrl  != null) Glide.with(this).load(m.coverUrl).centerCrop().into(binding.ivCover);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
