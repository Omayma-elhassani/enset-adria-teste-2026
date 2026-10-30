package org.sid.accountservice.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sid.accountservice.domain.AccountStatus;
import org.sid.accountservice.dto.AccountDTO;
import org.sid.accountservice.dto.AccountRequest;
import org.sid.accountservice.dto.BalanceUpdateRequest;
import org.sid.accountservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Gestion des comptes bancaires")
public class AccountRestController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Lister les comptes (filtre optionnel par client)")
    public List<AccountDTO> findAll(@RequestParam(required = false) String customerReference) {
        return customerReference == null ? accountService.findAll() : accountService.findByCustomer(customerReference);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un compte")
    public AccountDTO findById(@PathVariable UUID id) {
        return accountService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un compte")
    public AccountDTO create(@Valid @RequestBody AccountRequest request) {
        return accountService.create(request);
    }

    @PutMapping("/{id}/balance")
    @Operation(summary = "Mettre à jour le solde (delta positif = crédit, négatif = débit)")
    public AccountDTO updateBalance(@PathVariable UUID id, @RequestBody BalanceUpdateRequest request) {
        return accountService.updateBalance(id, request.delta());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Activer ou bloquer un compte")
    public AccountDTO changeStatus(@PathVariable UUID id, @RequestParam AccountStatus status) {
        return accountService.changeStatus(id, status);
    }
}
