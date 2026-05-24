package com.letmc.sound.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Musician;
import com.letmc.sound.databinding.FragmentProfileBinding;
import com.letmc.sound.ui.auth.AuthActivity;
import com.letmc.sound.utils.SessionManager;
import java.util.List;
import retrofit2.*;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

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
        loadProfile(session.getUserId());

        binding.btnEditProfile.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.action_profile_to_editProfile));

        binding.btnUploadBeat.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.action_profile_to_uploadBeat));

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
                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) populateUI(r.body().get(0));
            }
            @Override public void onFailure(Call<List<Musician>> c, Throwable t) {}
        });
    }

    private void populateUI(Musician m) {
        binding.tvName.setText(m.name != null ? m.name : "Sin nombre");
        binding.tvBio.setText(m.bio != null ? m.bio : "");
        binding.tvLocation.setText(m.location != null ? "📍 " + m.location : "");
        if (m.avatarUrl != null) Glide.with(this).load(m.avatarUrl).circleCrop().into(binding.ivAvatar);
        if (m.coverUrl  != null) Glide.with(this).load(m.coverUrl).centerCrop().into(binding.ivCover);
    }
}
