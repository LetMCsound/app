package com.letmc.sound.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Musician;
import com.letmc.sound.databinding.FragmentEditProfileBinding;
import com.letmc.sound.service.StorageService;
import com.letmc.sound.utils.SessionManager;
import java.util.*;
import retrofit2.*;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private Uri avatarUri = null;
    private Uri coverUri  = null;

    private final ActivityResultLauncher<Intent> avatarLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            if (r.getResultCode() == Activity.RESULT_OK && r.getData() != null) {
                avatarUri = r.getData().getData();
                Glide.with(this).load(avatarUri).circleCrop().into(binding.ivAvatar);
            }
        });

    private final ActivityResultLauncher<Intent> coverLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            if (r.getResultCode() == Activity.RESULT_OK && r.getData() != null) {
                coverUri = r.getData().getData();
                Glide.with(this).load(coverUri).centerCrop().into(binding.ivCover);
            }
        });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentEditProfileBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        SessionManager session = new SessionManager(requireContext());
        loadCurrentProfile(session.getUserId());

        binding.ivAvatar.setOnClickListener(v -> pickImage(avatarLauncher));
        binding.ivCover.setOnClickListener(v -> pickImage(coverLauncher));
        binding.btnSave.setOnClickListener(v -> saveProfile(session));
    }

    private void pickImage(ActivityResultLauncher<Intent> launcher) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        launcher.launch(i);
    }

    private void loadCurrentProfile(String userId) {
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getMusicianById("*", "eq." + userId).enqueue(new Callback<List<Musician>>() {
            @Override public void onResponse(Call<List<Musician>> c, Response<List<Musician>> r) {
                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                    Musician m = r.body().get(0);
                    binding.etName.setText(m.name != null ? m.name : "");
                    binding.etBio.setText(m.bio != null ? m.bio : "");
                    binding.etLocation.setText(m.location != null ? m.location : "");
                    if (m.avatarUrl != null) Glide.with(requireView()).load(m.avatarUrl).circleCrop().into(binding.ivAvatar);
                    if (m.coverUrl  != null) Glide.with(requireView()).load(m.coverUrl).centerCrop().into(binding.ivCover);
                }
            }
            @Override public void onFailure(Call<List<Musician>> c, Throwable t) {}
        });
    }

    private void saveProfile(SessionManager session) {
        String name     = binding.etName.getText().toString().trim();
        String bio      = binding.etBio.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();

        if (name.isEmpty()) { binding.etName.setError("El nombre es obligatorio"); return; }

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Guardando...");

        StorageService storage = new StorageService(requireContext());

        if (avatarUri != null) {
            storage.uploadFile("beats-media", "avatars/" + session.getUserId(),
                avatarUri, "image/jpeg",
                avatarUrl -> updateProfile(session.getUserId(), name, bio, location, avatarUrl, null),
                err -> updateProfile(session.getUserId(), name, bio, location, null, null));
        } else {
            updateProfile(session.getUserId(), name, bio, location, null, null);
        }
    }

    private void updateProfile(String userId, String name, String bio, String location,
                                @Nullable String avatarUrl, @Nullable String coverUrl) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("bio", bio);
        data.put("location", location);
        if (avatarUrl != null) data.put("avatar_url", avatarUrl);
        if (coverUrl  != null) data.put("cover_url",  coverUrl);

        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.updateMusician("eq." + userId, data).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                binding.btnSave.setEnabled(true);
                binding.btnSave.setText("Guardar cambios");
                if (r.isSuccessful()) {
                    Toast.makeText(requireContext(), "Perfil actualizado ✅", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                binding.btnSave.setEnabled(true);
                binding.btnSave.setText("Guardar cambios");
                Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
