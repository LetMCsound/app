package com.letmc.sound.service;

import android.content.Context;
import android.net.Uri;
import com.letmc.sound.data.api.SupabaseManager;
import java.io.InputStream;
import okhttp3.*;

/** Sube archivos a Supabase Storage (bucket beats-media). */
public class StorageService {

    public interface UploadCallback  { void onSuccess(String publicUrl); }
    public interface ErrorCallback   { void onError(String message); }

    private final Context context;
    private final OkHttpClient client = new OkHttpClient();

    public StorageService(Context context) { this.context = context.getApplicationContext(); }

    public void uploadFile(String bucket, String path, Uri fileUri,
                           String mimeType, UploadCallback onSuccess, ErrorCallback onError) {
        new Thread(() -> {
            try {
                InputStream is = context.getContentResolver().openInputStream(fileUri);
                if (is == null) { onError.onError("No se pudo leer el archivo"); return; }
                // readAllBytes() requiere API 26+; usamos ByteArrayOutputStream para compatibilidad con minSdk 24
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int read;
                while ((read = is.read(chunk)) != -1) buffer.write(chunk, 0, read);
                byte[] bytes = buffer.toByteArray();
                is.close();

                String url = SupabaseManager.SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + path;
                Request req = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseManager.SUPABASE_ANON)
                    .addHeader("Authorization", "Bearer " + SupabaseManager.getAccessToken())
                    .post(RequestBody.create(bytes, MediaType.parse(mimeType)))
                    .build();

                try (Response resp = client.newCall(req).execute()) {
                    if (resp.isSuccessful()) {
                        String publicUrl = SupabaseManager.SUPABASE_URL
                            + "/storage/v1/object/public/" + bucket + "/" + path;
                        android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
                        main.post(() -> onSuccess.onSuccess(publicUrl));
                    } else {
                        String err = resp.body() != null ? resp.body().string() : "Error desconocido";
                        android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
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
