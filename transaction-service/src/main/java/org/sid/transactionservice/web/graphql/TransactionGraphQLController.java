package org.sid.transactionservice.web.graphql;

import lombok.RequiredArgsConstructor;
import org.sid.transactionservice.dto.TransactionDTO;
import org.sid.transactionservice.dto.TransactionRequest;
import org.sid.transactionservice.dto.TransferRequest;
import org.sid.transactionservice.service.TransactionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionGraphQLController {

    private final TransactionService transactionService;

    @QueryMapping
    public List<TransactionDTO> transactions() {
        return transactionService.findAll();
    }

    @QueryMapping
    public TransactionDTO transactionById(@Argument UUID id) {
        return transactionService.findById(id);
    }

    @QueryMapping
    public List<TransactionDTO> transactionsByAccount(@Argument UUID accountId) {
        return transactionService.findByAccount(accountId);
    }

    @MutationMapping
    public TransactionDTO createTransaction(@Argument TransactionRequest input) {
        return transactionService.create(input);
    }

    @MutationMapping
    public List<TransactionDTO> transfer(@Argument TransferRequest input) {
        return transactionService.transfer(input);
    }
}
