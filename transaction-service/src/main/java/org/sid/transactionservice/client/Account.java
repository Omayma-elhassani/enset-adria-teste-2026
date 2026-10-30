package org.sid.transactionservice.client;

import java.util.UUID;

/** Vue d'un compte telle que renvoyée par account-service. */
public record Account(UUID id, String customerReference, String status, String currency, double balance) {
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
