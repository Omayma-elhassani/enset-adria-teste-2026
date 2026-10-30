package org.sid.accountservice.service;

import lombok.RequiredArgsConstructor;
import org.sid.accountservice.domain.Account;
import org.sid.accountservice.domain.AccountStatus;
import org.sid.accountservice.dto.AccountDTO;
import org.sid.accountservice.dto.AccountRequest;
import org.sid.accountservice.exception.AccountNotFoundException;
import org.sid.accountservice.exception.BusinessException;
import org.sid.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> findAll() {
        return accountRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO findById(UUID id) {
        return toDTO(getAccount(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> findByCustomer(String customerReference) {
        return accountRepository.findByCustomerReference(customerReference).stream().map(this::toDTO).toList();
    }

    @Override
    public AccountDTO create(AccountRequest request) {
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .customerReference(request.customerReference())
                .currency(request.currency().toUpperCase())
                .balance(request.initialBalance())
                .status(AccountStatus.ACTIVE)
                .build();
        return toDTO(accountRepository.save(account));
    }

    @Override
    public AccountDTO updateBalance(UUID id, double delta) {
        Account account = getAccount(id);
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Le compte " + id + " n'est pas actif");
        }
        double newBalance = account.getBalance() + delta;
        if (newBalance < 0) {
            throw new BusinessException("Solde insuffisant sur le compte " + id);
        }
        account.setBalance(newBalance);
        return toDTO(account);
    }

    @Override
    public AccountDTO changeStatus(UUID id, AccountStatus status) {
        Account account = getAccount(id);
        account.setStatus(status);
        return toDTO(account);
    }

    private Account getAccount(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    private AccountDTO toDTO(Account a) {
        return new AccountDTO(a.getId(), a.getCustomerReference(), a.getStatus(), a.getCurrency(), a.getBalance());
    }
}
