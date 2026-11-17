package br.fitforge.progresso.accounts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fitforge_accounts")
public class Account {
    @Id
    @Column(length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    protected Account() {}

    Account(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    String passwordHash() {
        return passwordHash;
    }
}
