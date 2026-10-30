package org.sid.accountservice.dto;

import org.sid.accountservice.domain.AccountStatus;

import java.util.UUID;

public record AccountDTO(UUID id, String customerReference, AccountStatus status, String currency, double balance) {
}
