package org.sid.accountservice.web.graphql;

import lombok.RequiredArgsConstructor;
import org.sid.accountservice.domain.AccountStatus;
import org.sid.accountservice.dto.AccountDTO;
import org.sid.accountservice.dto.AccountRequest;
import org.sid.accountservice.service.AccountService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountGraphQLController {

    private final AccountService accountService;

    @QueryMapping
    public List<AccountDTO> accounts() {
        return accountService.findAll();
    }

    @QueryMapping
    public AccountDTO accountById(@Argument UUID id) {
        return accountService.findById(id);
    }

    @QueryMapping
    public List<AccountDTO> accountsByCustomer(@Argument String customerReference) {
        return accountService.findByCustomer(customerReference);
    }

    @MutationMapping
    public AccountDTO createAccount(@Argument AccountRequest input) {
        return accountService.create(input);
    }

    @MutationMapping
    public AccountDTO changeAccountStatus(@Argument UUID id, @Argument AccountStatus status) {
        return accountService.changeStatus(id, status);
    }
}
