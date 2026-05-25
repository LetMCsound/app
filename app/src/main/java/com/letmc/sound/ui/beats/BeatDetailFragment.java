package com.letmc.sound.ui.beats;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
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
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnPlay.setOnClickListener(v -> togglePlay());
        binding.btnBuy.setOnClickListener(v -> purchaseBeat());
    }

    private void loadBeat(String id) {
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getBeatById("*", "eq." + id)
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
        binding.tvPrice.setText(currentBeat.priceStandard > 0
            ? String.format("€%.2f", currentBeat.priceStandard) : "Consultar");
        binding.tvGenre.setText(currentBeat.genre != null ? currentBeat.genre : "");
        binding.tvBpm.setText(currentBeat.bpm != null ? currentBeat.bpm + " BPM" : "");
        binding.tvArtist.setText(currentBeat.sellerName != null ? currentBeat.sellerName : "");
        if (currentBeat.coverUrl != null)
            Glide.with(this).load(currentBeat.coverUrl).into(binding.ivCover);
        if (currentBeat.audioPreviewUrl != null) initPlayer();
    }

    private void initPlayer() {
        if (currentBeat.audioPreviewUrl == null || currentBeat.audioPreviewUrl.isEmpty()) {
            binding.btnPlay.setEnabled(false);
            binding.btnPlay.setText("Sin preview");
            return;
        }
        player = new ExoPlayer.Builder(requireContext()).build();
        player.setMediaItem(MediaItem.fromUri(currentBeat.audioPreviewUrl));
        player.setPlayWhenReady(false);
        player.prepare();

        // Listener para actualizar el botón y la barra de progreso
        player.addListener(new Player.Listener() {
            @Override public void onIsPlayingChanged(boolean isPlaying) {
                if (binding == null || !isAdded()) return;
                binding.btnPlay.setText(isPlaying ? "⏸ Pausar" : "▶ Preview");
                binding.progressAudio.setVisibility(isPlaying ? View.VISIBLE : View.INVISIBLE);
                if (isPlaying) startProgressUpdater();
            }
            @Override public void onPlaybackStateChanged(int state) {
                if (binding == null || !isAdded()) return;
                if (state == Player.STATE_ENDED) {
                    binding.btnPlay.setText("▶ Preview");
                    binding.progressAudio.setProgress(0);
                    binding.progressAudio.setVisibility(View.INVISIBLE);
                    player.seekTo(0);
                }
                if (state == Player.STATE_BUFFERING) {
                    binding.btnPlay.setText("⏳ Cargando...");
                }
            }
        });
    }

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private void startProgressUpdater() {
        progressHandler.post(new Runnable() {
            @Override public void run() {
                if (player != null && player.isPlaying() && binding != null) {
                    long dur = player.getDuration();
                    long pos = player.getCurrentPosition();
                    if (dur > 0) binding.progressAudio.setProgress((int)(pos * 100 / dur));
                    progressHandler.postDelayed(this, 500);
                }
            }
        });
    }

    private void togglePlay() {
        if (player == null) return;
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
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
        sale.put("seller_id", currentBeat.sellerId);
        sale.put("amount",    currentBeat.priceStandard);
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
        cs.generateContract("beat", session.getUserId(), currentBeat.sellerId,
            currentBeat.id, currentBeat.priceStandard,
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
        binding.btnBuy.setText(currentBeat != null ? String.format("Comprar Beat — €%.2f", currentBeat.priceStandard) : "Comprar");
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        progressHandler.removeCallbacksAndMessages(null);
        if (player != null) { player.release(); player = null; }
        binding = null;
    }
}
