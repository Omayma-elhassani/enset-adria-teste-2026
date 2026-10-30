package org.sid.transactionservice.exception;

public class AccountServiceUnavailableException extends RuntimeException {
    public AccountServiceUnavailableException() {
        super("Le service des comptes est momentanément indisponible, veuillez réessayer plus tard");
    }
}
