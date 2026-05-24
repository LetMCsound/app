package com.letmc.sound.ui.beats;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import com.bumptech.glide.Glide;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Beat;
import com.letmc.sound.databinding.FragmentBeatDetailBinding;
import com.letmc.sound.service.ContractService;
import com.letmc.sound.utils.SessionManager;
import java.util.*;
import retrofit2.*;

public class BeatDetailFragment extends Fragment {

    private FragmentBeatDetailBinding binding;
    private ExoPlayer player;
    private Beat currentBeat;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentBeatDetailBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        String beatId = getArguments() != null ? getArguments().getString("beatId") : null;
        if (beatId != null) loadBeat(beatId);
        binding.btnPlay.setOnClickListener(v -> togglePlay());
        binding.btnBuy.setOnClickListener(v -> purchaseBeat());
    }

    private void loadBeat(String id) {
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getBeatById("*,musicians(name,avatar_url,slug)", "eq." + id)
            .enqueue(new Callback<List<Beat>>() {
                @Override public void onResponse(Call<List<Beat>> c, Response<List<Beat>> r) {
                    if (binding == null || !isAdded()) return;
                    if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                        currentBeat = r.body().get(0);
                        populateUI();
                    }
                }
                @Override public void onFailure(Call<List<Beat>> c, Throwable t) {
                    if (binding == null || !isAdded()) return;
                    Toast.makeText(requireContext(), "Error cargando beat", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void populateUI() {
        binding.tvTitle.setText(currentBeat.title);
        binding.tvPrice.setText(String.format("€%.2f", currentBeat.price));
        binding.tvGenre.setText(currentBeat.genre != null ? currentBeat.genre : "");
        binding.tvBpm.setText(currentBeat.bpm != null ? currentBeat.bpm + " BPM" : "");
        if (currentBeat.musician != null) binding.tvArtist.setText(currentBeat.musician.name);
        if (currentBeat.coverUrl != null)
            Glide.with(this).load(currentBeat.coverUrl).into(binding.ivCover);
        if (currentBeat.audioUrl != null) initPlayer();
    }

    private void initPlayer() {
        player = new ExoPlayer.Builder(requireContext()).build();
        player.setMediaItem(MediaItem.fromUri(currentBeat.audioUrl));
        player.prepare();
    }

    private void togglePlay() {
        if (player == null) return;
        if (player.isPlaying()) { player.pause(); binding.btnPlay.setText("▶ Play Preview"); }
        else { player.play(); binding.btnPlay.setText("⏸ Pausar"); }
    }

    private void purchaseBeat() {
        SessionManager session = new SessionManager(requireContext());
        if (!session.isLoggedIn()) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para comprar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentBeat == null) return;

        binding.btnBuy.setEnabled(false);
        binding.btnBuy.setText("Procesando...");

        // 1. Registrar venta en Supabase
        Map<String, Object> sale = new HashMap<>();
        sale.put("beat_id",   currentBeat.id);
        sale.put("buyer_id",  session.getUserId());
        sale.put("seller_id", currentBeat.musicianId);
        sale.put("amount",    currentBeat.price);
        sale.put("status",    "completed");

        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.createSale(sale).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (binding == null || !isAdded()) return;
                if (r.isSuccessful()) {
                    // 2. Generar contrato PDF
                    generateContract(session);
                } else {
                    resetBuyButton();
                    Toast.makeText(requireContext(), "Error al procesar la compra", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                if (binding == null || !isAdded()) return;
                resetBuyButton();
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateContract(SessionManager session) {
        binding.btnBuy.setText("Generando contrato...");
        ContractService cs = new ContractService();
        cs.generateContract("beat", session.getUserId(), currentBeat.musicianId,
            currentBeat.id, currentBeat.price,
            pdfUrl -> {
                if (binding == null || !isAdded()) return;
                binding.btnBuy.setText("✅ ¡Compra exitosa!");
                Toast.makeText(requireContext(), "Contrato PDF generado correctamente", Toast.LENGTH_LONG).show();
            },
            error -> {
                if (binding == null || !isAdded()) return;
                // Venta OK aunque falle el PDF
                binding.btnBuy.setText("✅ Compra realizada");
                Toast.makeText(requireContext(), "Venta registrada. El contrato se generará pronto.", Toast.LENGTH_LONG).show();
            });
    }

    private void resetBuyButton() {
        binding.btnBuy.setEnabled(true);
        binding.btnBuy.setText(currentBeat != null ? String.format("Comprar Beat — €%.2f", currentBeat.price) : "Comprar");
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (player != null) { player.release(); player = null; }
        binding = null;
    }
}
