package org.sid.transactionservice.mcp;

import lombok.RequiredArgsConstructor;
import org.sid.transactionservice.domain.TransactionType;
import org.sid.transactionservice.dto.TransactionDTO;
import org.sid.transactionservice.dto.TransactionRequest;
import org.sid.transactionservice.dto.TransferRequest;
import org.sid.transactionservice.service.TransactionService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Tools MCP exposés à l'agent IA (endpoint POST /mcp). */
@Component
@RequiredArgsConstructor
public class TransactionMcpTools {

    private final TransactionService transactionService;

    @McpTool(name = "getAccountTransactions", description = "Lister l'historique des transactions d'un compte (les plus récentes en premier)")
    public List<TransactionDTO> getAccountTransactions(@McpToolParam(description = "Identifiant UUID du compte") String accountId) {
        return transactionService.findByAccount(UUID.fromString(accountId));
    }

    @McpTool(name = "getTransaction", description = "Consulter le détail d'une transaction à partir de son identifiant UUID")
    public TransactionDTO getTransaction(@McpToolParam(description = "Identifiant UUID de la transaction") String transactionId) {
        return transactionService.findById(UUID.fromString(transactionId));
    }

    @McpTool(name = "createTransaction", description = "Enregistrer une transaction (paiement DEBIT ou dépôt CREDIT) sur un compte. "
            + "Le montant doit être positif, ne pas dépasser le plafond et la devise doit être celle du compte.")
    public TransactionDTO createTransaction(
            @McpToolParam(description = "Identifiant UUID du compte") String accountId,
            @McpToolParam(description = "Montant strictement positif") double amount,
            @McpToolParam(description = "Devise ISO sur 3 lettres, identique à celle du compte") String currency,
            @McpToolParam(description = "Type : DEBIT ou CREDIT", required = false) String type,
            @McpToolParam(description = "Catégorie du commerçant, ex: GROCERY, TRAVEL", required = false) String merchantCategory,
            @McpToolParam(description = "Code pays, ex: MA, FR", required = false) String country) {
        TransactionType txType = type == null || type.isBlank() ? TransactionType.DEBIT : TransactionType.valueOf(type.toUpperCase());
        return transactionService.create(new TransactionRequest(UUID.fromString(accountId), BigDecimal.valueOf(amount),
                currency, txType, merchantCategory, country));
    }

    @McpTool(name = "transfer", description = "Effectuer un virement d'un compte source vers un compte destination de même devise")
    public List<TransactionDTO> transfer(
            @McpToolParam(description = "UUID du compte source") String fromAccountId,
            @McpToolParam(description = "UUID du compte destination") String toAccountId,
            @McpToolParam(description = "Montant strictement positif") double amount,
            @McpToolParam(description = "Devise ISO sur 3 lettres") String currency) {
        return transactionService.transfer(new TransferRequest(UUID.fromString(fromAccountId), UUID.fromString(toAccountId),
                BigDecimal.valueOf(amount), currency));
    }
}
