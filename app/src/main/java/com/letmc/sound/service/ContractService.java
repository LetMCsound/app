package com.letmc.sound.service;

import com.letmc.sound.data.api.SupabaseManager;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

/**
 * Llama a la Edge Function "generar-contrato" de Supabase.
 * Tipos: beat | lyric | film | graphic
 * Devuelve la URL del PDF generado.
 */
public class ContractService {

    public interface ContractCallback { void onSuccess(String pdfUrl); }
    public interface ErrorCallback    { void onError(String message); }

    private final OkHttpClient client = new OkHttpClient();

    public void generateContract(String type, String buyerId, String sellerId,
                                 String itemId, double amount,
                                 ContractCallback onSuccess, ErrorCallback onError) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("type",      type);
                body.put("buyer_id",  buyerId);
                body.put("seller_id", sellerId);
                body.put("item_id",   itemId);
                body.put("amount",    amount);

                String url = SupabaseManager.SUPABASE_URL + "/functions/v1/generar-contrato";
                String token = SupabaseManager.getAccessToken();

                Request req = new Request.Builder()
                    .url(url)
                    .addHeader("apikey",        SupabaseManager.SUPABASE_ANON)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type",  "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

                android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
                try (Response resp = client.newCall(req).execute()) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        String raw = resp.body().string();
                        JSONObject result = new JSONObject(raw);
                        String pdfUrl = result.optString("pdf_url", "");
                        main.post(() -> onSuccess.onSuccess(pdfUrl));
                    } else {
                        String err = resp.body() != null ? resp.body().string() : "Error " + resp.code();
                        main.post(() -> onError.onError(err));
                    }
                }
            } catch (Exception e) {
                android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
                main.post(() -> onError.onError(e.getMessage()));
            }
        }).start();
    }
}
