package org.sid.accountservice.service;

import org.sid.accountservice.domain.AccountStatus;
import org.sid.accountservice.dto.AccountDTO;
import org.sid.accountservice.dto.AccountRequest;

import java.util.List;
import java.util.UUID;

/** Couche service unique, appelée par les contrôleurs REST, GraphQL et MCP. */
public interface AccountService {
    List<AccountDTO> findAll();
    AccountDTO findById(UUID id);
    List<AccountDTO> findByCustomer(String customerReference);
    AccountDTO create(AccountRequest request);
    AccountDTO updateBalance(UUID id, double delta);
    AccountDTO changeStatus(UUID id, AccountStatus status);
}
