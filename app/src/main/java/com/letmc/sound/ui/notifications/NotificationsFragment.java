package com.letmc.sound.ui.notifications;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Notification;
import com.letmc.sound.databinding.FragmentNotificationsBinding;
import com.letmc.sound.utils.SessionManager;
import java.util.List;
import retrofit2.*;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentNotificationsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        adapter = new NotificationsAdapter();
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerNotifications.setAdapter(adapter);
        loadNotifications();
    }

    private void loadNotifications() {
        SessionManager session = new SessionManager(requireContext());
        if (!session.isLoggedIn()) return;
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getNotifications("*", "eq." + session.getUserId(), "created_at.desc")
            .enqueue(new Callback<List<Notification>>() {
                @Override public void onResponse(Call<List<Notification>> c, Response<List<Notification>> r) {
                    if (binding == null || !isAdded()) return;
                    if (r.isSuccessful() && r.body() != null) adapter.setNotifications(r.body());
                }
                @Override public void onFailure(Call<List<Notification>> c, Throwable t) {}
            });
    }
}
