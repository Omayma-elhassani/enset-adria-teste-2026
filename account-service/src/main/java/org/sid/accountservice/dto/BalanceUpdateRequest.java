package org.sid.accountservice.dto;

/** delta > 0 : crédit, delta < 0 : débit. */
public record BalanceUpdateRequest(double delta) {
}
