package com.letmc.sound.ui.upload;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.*;
import android.widget.Toast;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.databinding.FragmentUploadBeatBinding;
import com.letmc.sound.service.StorageService;
import com.letmc.sound.utils.SessionManager;
import java.util.*;

public class UploadBeatFragment extends Fragment {

    private FragmentUploadBeatBinding binding;
    private Uri audioUri = null;
    private Uri coverUri = null;

    private final ActivityResultLauncher<Intent> audioPickerLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                audioUri = result.getData().getData();
                binding.tvAudioName.setText(getFileName(audioUri));
                binding.btnPickAudio.setAlpha(0.6f);
            }
        });

    private final ActivityResultLauncher<Intent> coverPickerLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                coverUri = result.getData().getData();
                binding.imgCoverPreview.setImageURI(coverUri);
            }
        });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentUploadBeatBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        binding.btnPickAudio.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            audioPickerLauncher.launch(intent);
        });

        binding.btnPickCover.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            coverPickerLauncher.launch(intent);
        });

        binding.btnPublish.setOnClickListener(v -> publishBeat());
    }

    private void publishBeat() {
        String title = binding.etTitle.getText().toString().trim();
        String price = binding.etPrice.getText().toString().trim();
        String genre = binding.etGenre.getText().toString().trim();
        String bpm   = binding.etBpm.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (title.isEmpty() || price.isEmpty()) {
            Toast.makeText(requireContext(), "Título y precio son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }
        if (audioUri == null) {
            Toast.makeText(requireContext(), "Selecciona un archivo de audio", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnPublish.setEnabled(false);
        binding.progressUpload.setVisibility(View.VISIBLE);
        binding.tvUploadStatus.setText("Subiendo audio...");

        SessionManager session = new SessionManager(requireContext());
        StorageService storage = new StorageService(requireContext());

        // 1. Subir audio
        storage.uploadFile("beats-media", "audio/" + session.getUserId() + "_" + System.currentTimeMillis(), audioUri, "audio/mpeg", audioUrl -> {
            if (binding == null || !isAdded()) return;
            binding.tvUploadStatus.setText("Subiendo portada...");

            // 2. Subir cover (si hay)
            if (coverUri != null) {
                storage.uploadFile("beats-media", "covers/" + session.getUserId() + "_" + System.currentTimeMillis(), coverUri, "image/jpeg", coverUrl -> {
                    if (binding == null || !isAdded()) return;
                    savebeat(title, price, genre, bpm, description, audioUrl, coverUrl, session.getUserId());
                }, err -> {
                    if (binding == null || !isAdded()) return;
                    savebeat(title, price, genre, bpm, description, audioUrl, null, session.getUserId());
                });
            } else {
                savebeat(title, price, genre, bpm, description, audioUrl, null, session.getUserId());
            }
        }, error -> {
            if (binding == null || !isAdded()) return;
            binding.btnPublish.setEnabled(true);
            binding.progressUpload.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Error subiendo audio: " + error, Toast.LENGTH_LONG).show();
        });
    }

    private void savebeat(String title, String price, String genre, String bpm, String description, String audioUrl, String coverUrl, String userId) {
        binding.tvUploadStatus.setText("Guardando beat...");
        double priceVal = (price != null && !price.isEmpty()) ? Double.parseDouble(price) : 0;
        Map<String, Object> beat = new HashMap<>();
        beat.put("title",             title);
        beat.put("price_standard",    priceVal);
        beat.put("price_premium",     Math.round(priceVal * 2.5 * 100.0) / 100.0);
        beat.put("price_exclusive",   Math.round(priceVal * 6.0 * 100.0) / 100.0);
        beat.put("genre",             genre);
        beat.put("bpm",               bpm);
        beat.put("description",       description);
        beat.put("audio_preview_url", audioUrl);
        beat.put("cover_url",         coverUrl);
        beat.put("seller_id",         userId);
        beat.put("is_published",      true);
        beat.put("plays",             0);
        beat.put("likes",             0);

        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.insertBeat(beat).enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(retrofit2.Call<Void> c, retrofit2.Response<Void> r) {
                if (binding == null || !isAdded()) return;
                binding.btnPublish.setEnabled(true);
                binding.progressUpload.setVisibility(View.GONE);
                if (r.isSuccessful()) {
                    Toast.makeText(requireContext(), "¡Beat publicado con éxito! 🎵", Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Error al guardar el beat", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(retrofit2.Call<Void> c, Throwable t) {
                if (binding == null || !isAdded()) return;
                binding.btnPublish.setEnabled(true);
                binding.progressUpload.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileName(Uri uri) {
        try (android.database.Cursor cursor = requireContext().getContentResolver()
            .query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        return uri.getLastPathSegment();
    }
}
