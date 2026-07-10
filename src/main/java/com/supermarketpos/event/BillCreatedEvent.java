package com.supermarketpos.event;

import com.supermarketpos.model.Bill;
import java.time.LocalDateTime;

/**
 * Fired after a Bill is completed and persisted.
 * Future consumers: Reports, Google Sheets Sync, Email Receipt, QR Receipt.
 */
public class BillCreatedEvent {

    private final Bill bill;
    private final LocalDateTime occurredAt;

    public BillCreatedEvent(Bill bill) {
        if (bill == null) throw new IllegalArgumentException("bill must not be null");
        this.bill       = bill;
        this.occurredAt = LocalDateTime.now();
    }

    public Bill getBill() { return bill; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}