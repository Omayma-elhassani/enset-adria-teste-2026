package org.sid.accountservice;

import org.sid.accountservice.domain.Account;
import org.sid.accountservice.domain.AccountStatus;
import org.sid.accountservice.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    /** Jeu de données de test avec des UUID fixes pour faciliter les tests (Postman, chatbot...). */
    @Bean
    CommandLineRunner initAccounts(AccountRepository accountRepository) {
        return args -> {
            if (accountRepository.existsById(UUID.fromString("11111111-1111-1111-1111-111111111111"))) return;
            accountRepository.saveAll(List.of(
                    Account.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                            .customerReference("CUST-001").status(AccountStatus.ACTIVE).currency("MAD").balance(15000).build(),
                    Account.builder().id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                            .customerReference("CUST-002").status(AccountStatus.ACTIVE).currency("MAD").balance(3000).build(),
                    Account.builder().id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                            .customerReference("CUST-003").status(AccountStatus.ACTIVE).currency("EUR").balance(2500).build(),
                    Account.builder().id(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                            .customerReference("CUST-004").status(AccountStatus.BLOCKED).currency("MAD").balance(800).build()
            ));
        };
    }
}
