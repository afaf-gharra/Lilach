package com.lilach.server.services;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.javalin.websocket.WsContext;

public class WebSocketBroadcaster {
    private static final Set<WsContext> connections = ConcurrentHashMap.newKeySet();
    
    public static void addConnection(WsContext ctx) {
        connections.add(ctx);
    }
    
    public static void removeConnection(WsContext ctx) {
        connections.remove(ctx);
    }
    
    public static void broadcast(String message) {
        connections.removeIf(ctx -> {
            try {
                ctx.send(message);
                return false;
            } catch (Exception e) {
                return true; // Remove dead connections
            }
        });
    }
    
    // Convenience methods for specific refresh types
    public static void broadcastProductUpdate() {
        broadcast("REFRESH_PRODUCTS");
    }
    
    public static void broadcastOrderUpdate() {
        broadcast("REFRESH_ORDERS");
    }
    
    public static void broadcastComplaintUpdate() {
        broadcast("REFRESH_COMPLAINTS");
    }
}
