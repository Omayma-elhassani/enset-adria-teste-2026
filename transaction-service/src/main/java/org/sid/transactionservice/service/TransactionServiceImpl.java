package org.sid.transactionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.transactionservice.client.Account;
import org.sid.transactionservice.client.AccountRestClient;
import org.sid.transactionservice.client.BalanceUpdateRequest;
import org.sid.transactionservice.config.BankTransactionProperties;
import org.sid.transactionservice.domain.Transaction;
import org.sid.transactionservice.domain.TransactionType;
import org.sid.transactionservice.dto.TransactionDTO;
import org.sid.transactionservice.dto.TransactionRequest;
import org.sid.transactionservice.dto.TransferRequest;
import org.sid.transactionservice.exception.BusinessException;
import org.sid.transactionservice.exception.TransactionNotFoundException;
import org.sid.transactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRestClient accountRestClient;
    private final BankTransactionProperties properties;

    /**
     * Règles métier :
     * 1. montant strictement positif et inférieur ou égal au plafond configuré ;
     * 2. le compte doit exister et être actif ;
     * 3. la devise de la transaction doit correspondre à celle du compte ;
     * 4. consultation du solde avant l'opération, puis mise à jour du solde après l'enregistrement.
     */
    @Override
    public TransactionDTO create(TransactionRequest request) {
        TransactionType type = request.type() == null ? TransactionType.DEBIT : request.type();
        validateAmount(request.amount());
        Account account = loadActiveAccount(request.accountId());
        checkCurrency(account, request.currency());
        checkBalance(account, type, request.amount());

        Transaction saved = save(request.accountId(), request.amount(), request.currency(), type,
                request.merchantCategory(), request.country());
        accountRestClient.updateBalance(account.id(), new BalanceUpdateRequest(signedDelta(type, request.amount())));
        // Partie F : publication de l'événement TransactionCreated dans Kafka
        return toDTO(saved);
    }

    /** Virement = DEBIT sur le compte source + CREDIT sur le compte destination. */
    @Override
    public List<TransactionDTO> transfer(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new BusinessException("Le compte source et le compte destination doivent être différents");
        }
        validateAmount(request.amount());
        Account from = loadActiveAccount(request.fromAccountId());
        Account to = loadActiveAccount(request.toAccountId());
        checkCurrency(from, request.currency());
        checkCurrency(to, request.currency());
        checkBalance(from, TransactionType.DEBIT, request.amount());

        Transaction debit = save(from.id(), request.amount(), request.currency(), TransactionType.DEBIT, "TRANSFER", null);
        Transaction credit = save(to.id(), request.amount(), request.currency(), TransactionType.CREDIT, "TRANSFER", null);

        double amount = request.amount().doubleValue();
        accountRestClient.updateBalance(from.id(), new BalanceUpdateRequest(-amount));
        try {
            accountRestClient.updateBalance(to.id(), new BalanceUpdateRequest(amount));
        } catch (RuntimeException e) {
            // Compensation : on recrédite le compte source puis on annule la transaction locale (rollback)
            log.error("Échec du crédit du compte {}, compensation du débit", to.id());
            accountRestClient.updateBalance(from.id(), new BalanceUpdateRequest(amount));
            throw e;
        }
        return List.of(toDTO(debit), toDTO(credit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> findAll() {
        return transactionRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> findByAccount(UUID accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO findById(UUID id) {
        return transactionRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    // ---------------------------------------------------------------- règles métier

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("Le montant doit être strictement positif");
        }
        if (amount.compareTo(properties.getMaxAmount()) > 0) {
            throw new BusinessException("Le montant " + amount + " dépasse le plafond autorisé de " + properties.getMaxAmount());
        }
    }

    private Account loadActiveAccount(UUID accountId) {
        Account account = accountRestClient.getAccount(accountId);
        if (account == null) {
            throw new BusinessException("Le compte " + accountId + " n'existe pas");
        }
        if (!account.isActive()) {
            throw new BusinessException("Le compte " + accountId + " n'est pas actif (statut " + account.status() + ")");
        }
        return account;
    }

    private void checkCurrency(Account account, String currency) {
        if (!account.currency().equalsIgnoreCase(currency)) {
            throw new BusinessException("La devise " + currency + " ne correspond pas à celle du compte (" + account.currency() + ")");
        }
    }

    private void checkBalance(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.DEBIT && BigDecimal.valueOf(account.balance()).compareTo(amount) < 0) {
            throw new BusinessException("Solde insuffisant : solde actuel " + account.balance() + " " + account.currency());
        }
    }

    // ---------------------------------------------------------------- utilitaires

    private Transaction save(UUID accountId, BigDecimal amount, String currency, TransactionType type,
                             String merchantCategory, String country) {
        return transactionRepository.save(Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(accountId)
                .amount(amount)
                .currency(currency.toUpperCase())
                .type(type)
                .merchantCategory(merchantCategory)
                .country(country)
                .createdAt(Instant.now())
                .build());
    }

    private double signedDelta(TransactionType type, BigDecimal amount) {
        return type == TransactionType.DEBIT ? -amount.doubleValue() : amount.doubleValue();
    }

    private TransactionDTO toDTO(Transaction t) {
        return new TransactionDTO(t.getId(), t.getAccountId(), t.getAmount(), t.getCurrency(), t.getType(),
                t.getMerchantCategory(), t.getCountry(), t.getCreatedAt());
    }
}
