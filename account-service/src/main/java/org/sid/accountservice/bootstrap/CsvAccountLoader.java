package org.sid.accountservice.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.accountservice.domain.Account;
import org.sid.accountservice.domain.AccountStatus;
import org.sid.accountservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Import des comptes du jeu de données data/bank_transactions.csv (988 comptes ACC0001...).
 * - customerReference = account_id du CSV (ex : ACC0090)
 * - id = UUID dérivé de account_id (UUID.nameUUIDFromBytes) : transaction-service calcule le même UUID
 * - devise EUR (les pays du CSV sont FR, BE, DE, ES, IT), statut ACTIVE
 * - solde initial = 1000 + 2 x total des montants du compte dans le CSV
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class CsvAccountLoader implements CommandLineRunner {

    private static final String CSV = "data/bank_transactions.csv";

    private final AccountRepository accountRepository;

    @Value("${bank.seed.csv.enabled:true}")
    private boolean enabled;

    public static UUID accountUuid(String csvAccountId) {
        return UUID.nameUUIDFromBytes(csvAccountId.trim().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void run(String... args) throws Exception {
        if (!enabled) return;

        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(CSV).getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // en-tête
            while ((line = reader.readLine()) != null) {
                String[] c = line.split(",", -1);
                if (c.length < 3 || c[1].isBlank() || c[2].isBlank()) continue;
                totals.merge(c[1].trim(), new BigDecimal(c[2].trim()), BigDecimal::add);
            }
        }

        if (totals.isEmpty() || accountRepository.existsById(accountUuid(totals.keySet().iterator().next()))) return;

        List<Account> accounts = totals.entrySet().stream().map(e -> Account.builder()
                .id(accountUuid(e.getKey()))
                .customerReference(e.getKey())
                .status(AccountStatus.ACTIVE)
                .currency("EUR")
                .balance(e.getValue().multiply(BigDecimal.valueOf(2)).add(BigDecimal.valueOf(1000))
                        .setScale(2, RoundingMode.HALF_UP).doubleValue())
                .build()).toList();
        accountRepository.saveAll(accounts);
        log.info("{} comptes importés depuis {}", accounts.size(), CSV);
    }
}
