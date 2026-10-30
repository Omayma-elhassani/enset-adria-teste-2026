package org.sid.transactionservice.client;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.sid.transactionservice.exception.AccountServiceUnavailableException;
import org.sid.transactionservice.exception.BusinessException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fallback Resilience4J : on distingue une vraie erreur métier (404 / 4xx renvoyée par account-service)
 * d'une panne technique (service arrêté, timeout, circuit ouvert).
 * En cas de panne, on refuse proprement la transaction au lieu de laisser une erreur 500.
 */
@Slf4j
@Component
public class AccountRestClientFallbackFactory implements FallbackFactory<AccountRestClient> {

    @Override
    public AccountRestClient create(Throwable cause) {
        return new AccountRestClient() {
            @Override
            public Account getAccount(UUID id) {
                throw translate(cause, id);
            }

            @Override
            public Account updateBalance(UUID id, BalanceUpdateRequest request) {
                throw translate(cause, id);
            }
        };
    }

    private RuntimeException translate(Throwable cause, UUID id) {
        if (cause instanceof FeignException.NotFound) {
            return new BusinessException("Le compte " + id + " n'existe pas");
        }
        if (cause instanceof FeignException fe && fe.status() >= 400 && fe.status() < 500) {
            return new BusinessException("Opération refusée par account-service : " + fe.contentUTF8());
        }
        log.warn("account-service indisponible (fallback Resilience4J) : {}", cause.toString());
        return new AccountServiceUnavailableException();
    }
}
