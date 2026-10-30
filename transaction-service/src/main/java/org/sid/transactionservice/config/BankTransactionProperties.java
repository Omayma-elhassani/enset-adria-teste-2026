package org.sid.transactionservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Paramètre partagé par le Config Server : bank.transaction.max-amount (défaut 1000).
 * Un bean @ConfigurationProperties est automatiquement mis à jour après un POST /actuator/refresh.
 */
@Getter @Setter
@Component
@ConfigurationProperties(prefix = "bank.transaction")
public class BankTransactionProperties {
    private BigDecimal maxAmount = new BigDecimal("1000");
}
