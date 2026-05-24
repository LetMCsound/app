package com.letmc.sound.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.letmc.sound.MainActivity;
import com.letmc.sound.R;
import com.letmc.sound.data.api.SupabaseAuthApi;
import com.letmc.sound.data.api.SupabaseManager;
import com.letmc.sound.data.model.AuthResponse;
import com.letmc.sound.data.model.LoginRequest;
import com.letmc.sound.databinding.FragmentLoginBinding;
import com.letmc.sound.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        binding.btnLogin.setOnClickListener(v -> doLogin());
        binding.tvGoRegister.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new RegisterFragment())
                .addToBackStack(null).commit()
        );
    }

    private void doLogin() {
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.btnLogin.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        SupabaseAuthApi api = SupabaseManager.createService(SupabaseAuthApi.class);
        api.login(new LoginRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override public void onResponse(Call<AuthResponse> c, Response<AuthResponse> r) {
                if (binding == null || !isAdded()) return;
                binding.btnLogin.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    AuthResponse auth = r.body();
                    new SessionManager(requireContext()).saveSession(
                        auth.accessToken, auth.user.id, auth.user.email);
                    startActivity(new Intent(requireActivity(), MainActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(requireContext(), "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<AuthResponse> c, Throwable t) {
                if (binding == null || !isAdded()) return;
                binding.btnLogin.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
