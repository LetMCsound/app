package com.letmc.sound.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.letmc.sound.MainActivity;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.*;
import com.letmc.sound.databinding.FragmentRegisterBinding;
import com.letmc.sound.utils.SessionManager;
import retrofit2.*;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentRegisterBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        binding.btnRegister.setOnClickListener(v -> doRegister());
        binding.tvGoLogin.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void doRegister() {
        String email = binding.etEmail.getText().toString().trim();
        String pass  = binding.etPassword.getText().toString().trim();
        String pass2 = binding.etPasswordConfirm.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show(); return;
        }
        if (!pass.equals(pass2)) {
            Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show(); return;
        }
        binding.btnRegister.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        SupabaseAuthApi api = SupabaseManager.createService(SupabaseAuthApi.class);
        api.register(new RegisterRequest(email, pass)).enqueue(new Callback<AuthResponse>() {
            @Override public void onResponse(Call<AuthResponse> c, Response<AuthResponse> r) {
                if (binding == null || !isAdded()) return;
                binding.btnRegister.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null && r.body().accessToken != null) {
                    AuthResponse auth = r.body();
                    new SessionManager(requireContext()).saveSession(auth.accessToken, auth.user.id, auth.user.email);
                    startActivity(new Intent(requireActivity(), MainActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(requireContext(), "Error al registrar. El email ya existe.", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<AuthResponse> c, Throwable t) {
                if (binding == null || !isAdded()) return;
                binding.btnRegister.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
