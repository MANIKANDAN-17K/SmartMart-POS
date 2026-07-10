package com.supermarketpos.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockUpdateEvent {

    private final int productId;
    private final String productName;
    private final int previousStock;
    private final int newStock;
    private final String movementType;
    private final LocalDateTime timestamp;

    public StockUpdateEvent(int productId, String productName, int previousStock,
                            int newStock, String movementType) {
        this.productId = productId;
        this.productName = productName;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.movementType = movementType;
        this.timestamp = LocalDateTime.now();
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getPreviousStock() {
        return previousStock;
    }

    public int getNewStock() {
        return newStock;
    }

    public String getMovementType() {
        return movementType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // --- Simple in-process publish/subscribe so other modules (e.g. Dashboard) can listen ---

    public interface Listener {
        void onStockUpdated(StockUpdateEvent event);
    }

    private static final List<Listener> listeners = new ArrayList<>();

    public static synchronized void subscribe(Listener listener) {
        listeners.add(listener);
    }

    public static synchronized void unsubscribe(Listener listener) {
        listeners.remove(listener);
    }

    public static synchronized void publish(StockUpdateEvent event) {
        for (Listener listener : new ArrayList<>(listeners)) {
            listener.onStockUpdated(event);
        }
    }
}