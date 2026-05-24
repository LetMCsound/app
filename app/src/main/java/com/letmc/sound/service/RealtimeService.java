package com.letmc.sound.service;

import android.util.Log;
import com.letmc.sound.data.api.SupabaseManager;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

/**
 * Supabase Realtime via WebSocket (OkHttp).
 * Suscribe al canal "messages" para recibir INSERT en tiempo real.
 */
public class RealtimeService {

    private static final String TAG = "RealtimeService";
    private OkHttpClient client;
    private WebSocket webSocket;
    private MessageListener listener;
    private String conversationId;

    public interface MessageListener {
        void onNewMessage(String payload);
        void onConnected();
        void onDisconnected();
    }

    public RealtimeService(MessageListener listener) {
        this.listener = listener;
        client = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();
    }

    public void connect(String conversationId) {
        this.conversationId = conversationId;
        String wsUrl = SupabaseManager.SUPABASE_URL
            .replace("https://", "wss://")
            + "/realtime/v1/websocket?apikey=" + SupabaseManager.SUPABASE_ANON
            + "&vsn=1.0.0";

        Request request = new Request.Builder().url(wsUrl).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "WS connected");
                listener.onConnected();
                subscribeToMessages(ws);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                try {
                    JSONObject msg = new JSONObject(text);
                    String event = msg.optString("event");
                    if ("phx_reply".equals(event) || "INSERT".equals(event)) {
                        String payload = msg.optString("payload");
                        if (payload != null && !payload.isEmpty()) {
                            listener.onNewMessage(payload);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error", e);
                }
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                listener.onDisconnected();
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WS failure", t);
                listener.onDisconnected();
            }
        });
    }

    private void subscribeToMessages(WebSocket ws) {
        try {
            // Heartbeat
            JSONObject heartbeat = new JSONObject();
            heartbeat.put("topic", "phoenix");
            heartbeat.put("event", "heartbeat");
            heartbeat.put("payload", new JSONObject());
            heartbeat.put("ref", "1");
            ws.send(heartbeat.toString());

            // Suscripción al canal messages filtrado por conversation_id
            JSONObject sub = new JSONObject();
            sub.put("topic", "realtime:public:messages:conversation_id=eq." + conversationId);
            sub.put("event", "phx_join");
            sub.put("payload", new JSONObject());
            sub.put("ref", "2");
            ws.send(sub.toString());

            Log.d(TAG, "Subscribed to conversation: " + conversationId);
        } catch (Exception e) {
            Log.e(TAG, "Subscribe error", e);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User left");
            webSocket = null;
        }
    }
}
