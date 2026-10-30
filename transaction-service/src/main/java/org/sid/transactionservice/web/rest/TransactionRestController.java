package org.sid.transactionservice.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sid.transactionservice.dto.TransactionDTO;
import org.sid.transactionservice.dto.TransactionRequest;
import org.sid.transactionservice.dto.TransferRequest;
import org.sid.transactionservice.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Enregistrement et consultation des transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Lister les transactions (filtre optionnel par compte)")
    public List<TransactionDTO> findAll(@RequestParam(required = false) UUID accountId) {
        return accountId == null ? transactionService.findAll() : transactionService.findByAccount(accountId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une transaction")
    public TransactionDTO findById(@PathVariable UUID id) {
        return transactionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enregistrer une transaction (DEBIT par défaut)")
    public TransactionDTO create(@Valid @RequestBody TransactionRequest request) {
        return transactionService.create(request);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Effectuer un virement d'un compte vers un autre")
    public List<TransactionDTO> transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }
}
