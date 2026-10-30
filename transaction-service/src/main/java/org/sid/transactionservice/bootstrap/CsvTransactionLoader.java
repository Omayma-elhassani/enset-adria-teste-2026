package org.sid.transactionservice.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.transactionservice.domain.Transaction;
import org.sid.transactionservice.domain.TransactionType;
import org.sid.transactionservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Import de l'historique des transactions depuis data/bank_transactions.csv (5000 lignes).
 * C'est un historique : les lignes sont enregistrées directement en base, sans passer par les règles
 * métier (plafond, solde) ni publier d'événement Kafka. Les nouvelles transactions, elles, passent par
 * TransactionService.
 * - id = UUID dérivé de transaction_id, accountId = UUID dérivé de account_id (même calcul qu'account-service)
 * - type DEBIT, devise EUR, createdAt = janvier 2026 à l'heure (colonne hour) indiquée dans le CSV
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsvTransactionLoader implements CommandLineRunner {

    private static final String CSV = "data/bank_transactions.csv";
    private static final Instant BASE_DATE = Instant.parse("2026-01-01T00:00:00Z");

    private final TransactionRepository transactionRepository;

    @Value("${bank.seed.csv.enabled:true}")
    private boolean enabled;

    public static UUID uuidOf(String csvId) {
        return UUID.nameUUIDFromBytes(csvId.trim().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void run(String... args) throws Exception {
        if (!enabled || transactionRepository.count() > 0) return;

        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(CSV).getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // en-tête
            int index = 0;
            while ((line = reader.readLine()) != null) {
                // transaction_id,account_id,amount,hour,country,merchant_category,...
                String[] c = line.split(",", -1);
                if (c.length < 6 || c[1].isBlank() || c[2].isBlank()) continue;
                BigDecimal amount = new BigDecimal(c[2].trim());
                if (amount.signum() <= 0) continue;
                int hour = c[3].isBlank() ? 12 : Integer.parseInt(c[3].trim());
                Instant createdAt = BASE_DATE
                        .plus(Duration.ofDays(index % 31))
                        .plus(Duration.ofHours(hour))
                        .plus(Duration.ofMinutes(index % 60));
                transactions.add(Transaction.builder()
                        .id(uuidOf(c[0]))
                        .accountId(uuidOf(c[1]))
                        .amount(amount)
                        .currency("EUR")
                        .type(TransactionType.DEBIT)
                        .country(c[4].isBlank() ? null : c[4].trim())
                        .merchantCategory(c[5].isBlank() ? null : c[5].trim())
                        .createdAt(createdAt)
                        .build());
                index++;
            }
        }
        transactionRepository.saveAll(transactions);
        log.info("{} transactions importées depuis {}", transactions.size(), CSV);
    }
}
