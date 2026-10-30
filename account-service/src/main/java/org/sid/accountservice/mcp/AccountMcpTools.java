package org.sid.accountservice.mcp;

import lombok.RequiredArgsConstructor;
import org.sid.accountservice.domain.AccountStatus;
import org.sid.accountservice.dto.AccountDTO;
import org.sid.accountservice.dto.AccountRequest;
import org.sid.accountservice.service.AccountService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Tools MCP exposés à l'agent IA (endpoint POST /mcp). */
@Component
@RequiredArgsConstructor
public class AccountMcpTools {

    private final AccountService accountService;

    @McpTool(name = "listAccounts", description = "Lister tous les comptes bancaires avec leur solde, devise et statut")
    public List<AccountDTO> listAccounts() {
        return accountService.findAll();
    }

    @McpTool(name = "getAccount", description = "Consulter un compte bancaire (solde, devise, statut) à partir de son identifiant UUID")
    public AccountDTO getAccount(@McpToolParam(description = "Identifiant UUID du compte") String accountId) {
        return accountService.findById(UUID.fromString(accountId));
    }

    @McpTool(name = "getCustomerAccounts", description = "Lister les comptes d'un client à partir de sa référence client (ex: CUST-001 ou ACC0090)")
    public List<AccountDTO> getCustomerAccounts(@McpToolParam(description = "Référence du client") String customerReference) {
        return accountService.findByCustomer(customerReference);
    }

    @McpTool(name = "createAccount", description = "Créer un nouveau compte bancaire actif")
    public AccountDTO createAccount(
            @McpToolParam(description = "Référence du client") String customerReference,
            @McpToolParam(description = "Devise ISO sur 3 lettres, ex: MAD, EUR") String currency,
            @McpToolParam(description = "Solde initial (>= 0)") double initialBalance) {
        return accountService.create(new AccountRequest(customerReference, currency, initialBalance));
    }

    @McpTool(name = "changeAccountStatus", description = "Bloquer ou activer un compte bancaire")
    public AccountDTO changeAccountStatus(
            @McpToolParam(description = "Identifiant UUID du compte") String accountId,
            @McpToolParam(description = "Nouveau statut : ACTIVE ou BLOCKED") String status) {
        return accountService.changeStatus(UUID.fromString(accountId), AccountStatus.valueOf(status.toUpperCase()));
    }
}
