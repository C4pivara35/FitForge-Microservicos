package br.fitforge.progresso.accounts;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Hidden
@RestController
@RequestMapping("/api/internal/accounts")
public class AccountController {
    private final AccountService accounts;

    public AccountController(AccountService accounts) {
        this.accounts = accounts;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody Credentials credentials) {
        accounts.register(credentials.username(), credentials.password());
    }

    @PostMapping("/authenticate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void authenticate(@RequestBody Credentials credentials) {
        if (!accounts.authenticate(credentials.username(), credentials.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    record Credentials(String username, String password) {}
}
