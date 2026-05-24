package com.letmc.sound.ui.chat;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Conversation;
import com.letmc.sound.databinding.FragmentChatListBinding;
import com.letmc.sound.utils.SessionManager;
import java.util.List;
import retrofit2.*;

public class ChatListFragment extends Fragment {

    private FragmentChatListBinding binding;
    private ConversationsAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentChatListBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        adapter = new ConversationsAdapter(conv -> {
            Bundle args = new Bundle();
            args.putString("conversationId", conv.id);
            Navigation.findNavController(view).navigate(R.id.action_chatList_to_chatDetail, args);
        });
        binding.recyclerConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerConversations.setAdapter(adapter);
        loadConversations();
    }

    private void loadConversations() {
        SessionManager session = new SessionManager(requireContext());
        if (!session.isLoggedIn()) return;
        String userId = session.getUserId();
        SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
        String select = "*,buyer:musicians!buyer_id(name,avatar_url),seller:musicians!seller_id(name,avatar_url)";
        String orFilter = "buyer_id.eq." + userId + ",seller_id.eq." + userId;
        api.getConversations(select, orFilter, "updated_at.desc")
            .enqueue(new Callback<List<Conversation>>() {
                @Override public void onResponse(Call<List<Conversation>> c, Response<List<Conversation>> r) {
                    if (binding == null || !isAdded()) return;
                    if (r.isSuccessful() && r.body() != null) adapter.setConversations(r.body());
                    else Toast.makeText(requireContext(), "Error cargando chats", Toast.LENGTH_SHORT).show();
                }
                @Override public void onFailure(Call<List<Conversation>> c, Throwable t) {
                    if (binding == null || !isAdded()) return;
                    Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
