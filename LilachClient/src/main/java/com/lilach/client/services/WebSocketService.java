package com.lilach.client.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javafx.application.Platform;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketService {
    private static WebSocket webSocket;
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();
    
    private static final Map<String, Consumer<String>> messageHandlers = new HashMap<>();
    private static boolean isConnecting = false;
    
    public static void connect(String serverUrl) {
        if (webSocket != null || isConnecting) {
            return; // Already connected or connecting
        }
        
        isConnecting = true;
        String wsUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://") + "/ws";
        
        Request request = new Request.Builder()
                .url(wsUrl)
                .build();
        
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnecting = false;
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Platform.runLater(() -> {
                    for (Consumer<String> handler : messageHandlers.values()) {
                        handler.accept(text);
                    }
                });
            }
            
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                isConnecting = false;
                WebSocketService.webSocket = null;
                
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        connect(serverUrl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                isConnecting = false;
                WebSocketService.webSocket = null;
                
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        connect(serverUrl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }
    
    public static void registerHandler(String handlerId, Consumer<String> handler) {
        messageHandlers.put(handlerId, handler);
    }
    
    public static void unregisterHandler(String handlerId) {
        messageHandlers.remove(handlerId);
    }
    
    public static void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnecting");
            webSocket = null;
        }
    }
    
    public static boolean isConnected() {
        return webSocket != null;
    }
}
