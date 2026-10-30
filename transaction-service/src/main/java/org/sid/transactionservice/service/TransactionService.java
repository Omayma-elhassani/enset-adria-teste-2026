package org.sid.transactionservice.service;

import org.sid.transactionservice.dto.TransactionDTO;
import org.sid.transactionservice.dto.TransactionRequest;
import org.sid.transactionservice.dto.TransferRequest;

import java.util.List;
import java.util.UUID;

/** Couche service unique, appelée par les contrôleurs REST, GraphQL et MCP. */
public interface TransactionService {
    TransactionDTO create(TransactionRequest request);
    List<TransactionDTO> transfer(TransferRequest request);
    List<TransactionDTO> findAll();
    List<TransactionDTO> findByAccount(UUID accountId);
    TransactionDTO findById(UUID id);
}
