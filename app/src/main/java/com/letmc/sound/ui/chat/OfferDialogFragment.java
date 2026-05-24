package com.letmc.sound.ui.chat;

import android.app.Dialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.letmc.sound.R;
import com.letmc.sound.data.api.*;
import com.letmc.sound.data.model.Message;
import com.letmc.sound.databinding.DialogOfferBinding;
import com.letmc.sound.utils.SessionManager;
import java.util.HashMap;
import java.util.Map;
import retrofit2.*;

public class OfferDialogFragment extends BottomSheetDialogFragment {

    private DialogOfferBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = DialogOfferBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        String conversationId = getArguments() != null ? getArguments().getString("conversationId") : null;

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSendOffer.setOnClickListener(v -> {
            String amount = binding.etAmount.getText().toString().trim();
            String note   = binding.etNote.getText().toString().trim();
            if (amount.isEmpty()) { binding.etAmount.setError("Introduce un precio"); return; }

            String content = "💜 OFERTA: €" + amount + (note.isEmpty() ? "" : " — " + note);
            SessionManager session = new SessionManager(requireContext());
            Map<String, Object> msg = new HashMap<>();
            msg.put("conversation_id", conversationId);
            msg.put("sender_id", session.getUserId());
            msg.put("content", content);
            msg.put("type", "offer");

            SupabaseApi api = SupabaseManager.createService(SupabaseApi.class);
            api.sendMessage(msg).enqueue(new Callback<Message>() {
                @Override public void onResponse(Call<Message> c, Response<Message> r) {
                    if (r.isSuccessful()) dismiss();
                    else Toast.makeText(requireContext(), "Error enviando oferta", Toast.LENGTH_SHORT).show();
                }
                @Override public void onFailure(Call<Message> c, Throwable t) {
                    Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
