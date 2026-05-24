package com.letmc.sound.ui.lyrics;

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
import com.letmc.sound.databinding.FragmentLyricDetailBinding;
import com.letmc.sound.utils.SessionManager;
import java.util.*;
import retrofit2.*;

public class LyricDetailFragment extends Fragment {

    private FragmentLyricDetailBinding binding;
    private Lyric currentLyric;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentLyricDetailBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        String lyricId = getArguments() != null ? getArguments().getString("lyricId") : null;
        if (lyricId != null) loadLyric(lyricId);
        binding.btnNegotiate.setOnClickListener(v -> startNegotiation(view));
    }

    private void loadLyric(String id) {
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getLyricById("*,musicians(name,avatar_url,slug,user_id)", "eq." + id)
            .enqueue(new Callback<List<Lyric>>() {
                @Override public void onResponse(Call<List<Lyric>> c, Response<List<Lyric>> r) {
                    if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                        currentLyric = r.body().get(0);
                        populateUI();
                    }
                }
                @Override public void onFailure(Call<List<Lyric>> c, Throwable t) {
                    Toast.makeText(requireContext(), "Error cargando letra", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void populateUI() {
        binding.tvTitle.setText(currentLyric.title);
        binding.tvGenre.setText(currentLyric.genre != null ? currentLyric.genre : "");
        binding.tvPrice.setText(currentLyric.price > 0 ? String.format("€%.2f", currentLyric.price) : "Precio negociable");
        binding.tvSnippet.setText(currentLyric.snippet != null ? currentLyric.snippet : "Sin fragmento");
        if (currentLyric.musician != null) {
            binding.tvArtist.setText(currentLyric.musician.name);
            if (currentLyric.musician.avatarUrl != null)
                Glide.with(this).load(currentLyric.musician.avatarUrl).circleCrop().into(binding.ivAvatar);
        }
    }

    private void startNegotiation(View view) {
        SessionManager session = new SessionManager(requireContext());
        if (!session.isLoggedIn()) {
            Toast.makeText(requireContext(), "Inicia sesión para negociar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentLyric == null || currentLyric.musician == null) return;

        String sellerId = currentLyric.musician.userId;
        String buyerId  = session.getUserId();
        if (sellerId.equals(buyerId)) {
            Toast.makeText(requireContext(), "No puedes contactar tu propia letra", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnNegotiate.setEnabled(false);
        binding.btnNegotiate.setText("Iniciando chat...");

        // Buscar o crear conversación
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        Map<String, Object> conv = new HashMap<>();
        conv.put("buyer_id",  buyerId);
        conv.put("seller_id", sellerId);
        conv.put("type",      "lyric");
        conv.put("item_id",   currentLyric.id);
        conv.put("status",    "negotiating");

        api.createConversation(conv).enqueue(new Callback<Conversation>() {
            @Override public void onResponse(Call<Conversation> c, Response<Conversation> r) {
                binding.btnNegotiate.setEnabled(true);
                binding.btnNegotiate.setText("Negociar precio");
                if (r.isSuccessful() && r.body() != null) {
                    Bundle args = new Bundle();
                    args.putString("conversationId", r.body().id);
                    Navigation.findNavController(view).navigate(R.id.action_lyricDetail_to_chat, args);
                } else {
                    Toast.makeText(requireContext(), "Error iniciando la negociación", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Conversation> c, Throwable t) {
                binding.btnNegotiate.setEnabled(true);
                binding.btnNegotiate.setText("Negociar precio");
                Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
