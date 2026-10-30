package org.sid.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

/**
 * Client OpenFeign vers account-service (résolu par Eureka / load balancer).
 * Chaque appel passe par un circuit breaker Resilience4J ; en cas d'échec, le fallback est utilisé.
 */
@FeignClient(name = "account-service", fallbackFactory = AccountRestClientFallbackFactory.class)
public interface AccountRestClient {

    @GetMapping("/api/accounts/{id}")
    Account getAccount(@PathVariable("id") UUID id);

    @PutMapping("/api/accounts/{id}/balance")
    Account updateBalance(@PathVariable("id") UUID id, @RequestBody BalanceUpdateRequest request);
}
