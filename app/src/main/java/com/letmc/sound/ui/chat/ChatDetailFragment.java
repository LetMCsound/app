package com.letmc.sound.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Message;
import com.letmc.sound.databinding.FragmentChatDetailBinding;
import com.letmc.sound.service.RealtimeService;
import com.letmc.sound.utils.SessionManager;
import java.util.*;
import retrofit2.*;

public class ChatDetailFragment extends Fragment implements RealtimeService.MessageListener {

    private FragmentChatDetailBinding binding;
    private MessagesAdapter adapter;
    private String conversationId;
    private RealtimeService realtimeService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentChatDetailBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        conversationId = getArguments() != null ? getArguments().getString("conversationId") : null;
        SessionManager session = new SessionManager(requireContext());
        adapter = new MessagesAdapter(session.getUserId());

        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(llm);
        binding.recyclerMessages.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.btnOffer.setOnClickListener(v -> showOfferDialog());

        if (conversationId != null) {
            loadMessages();
            startRealtime();
        }
    }

    private void startRealtime() {
        realtimeService = new RealtimeService(this);
        realtimeService.connect(conversationId);
    }

    @Override public void onNewMessage(String payload) {
        // Recibimos un nuevo mensaje por WebSocket → recargamos
        mainHandler.post(this::loadMessages);
    }

    @Override public void onConnected() {
        mainHandler.post(() -> {
            if (binding != null) binding.tvRealtimeStatus.setText("● En línea");
        });
    }

    @Override public void onDisconnected() {
        mainHandler.post(() -> {
            if (binding != null) binding.tvRealtimeStatus.setText("○ Reconectando...");
        });
    }

    private void loadMessages() {
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.getMessages("*,sender:musicians!sender_id(name,avatar_url)",
            "eq." + conversationId, "created_at.asc")
            .enqueue(new Callback<List<Message>>() {
                @Override public void onResponse(Call<List<Message>> c, Response<List<Message>> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        adapter.setMessages(r.body());
                        binding.recyclerMessages.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }
                @Override public void onFailure(Call<List<Message>> c, Throwable t) {}
            });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        SessionManager session = new SessionManager(requireContext());
        Map<String, Object> msg = new HashMap<>();
        msg.put("conversation_id", conversationId);
        msg.put("sender_id", session.getUserId());
        msg.put("content", text);
        msg.put("type", "text");
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        api.sendMessage(msg).enqueue(new Callback<Message>() {
            @Override public void onResponse(Call<Message> c, Response<Message> r) {
                if (r.isSuccessful()) { binding.etMessage.setText(""); loadMessages(); }
            }
            @Override public void onFailure(Call<Message> c, Throwable t) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOfferDialog() {
        OfferDialogFragment dialog = new OfferDialogFragment();
        Bundle args = new Bundle();
        args.putString("conversationId", conversationId);
        dialog.setArguments(args);
        dialog.show(getChildFragmentManager(), "offer");
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (realtimeService != null) realtimeService.disconnect();
        binding = null;
    }
}
