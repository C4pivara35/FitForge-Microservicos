package br.fitforge.progresso.accounts;

import br.fitforge.progresso.exception.ConflitoException;
import br.fitforge.progresso.exception.RegraNegocioException;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private static final Pattern USERNAME = Pattern.compile("[a-zA-Z0-9._@-]{3,64}");
    private final EntityManager entityManager;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    private final String unknownPassword = passwords.encode(java.util.UUID.randomUUID().toString());

    public AccountService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void register(String username, String password) {
        if (username == null || !USERNAME.matcher(username).matches()) {
            throw new RegraNegocioException("Use de 3 a 64 caracteres no usuário: letras, números, ponto, _, @ ou hífen.");
        }
        if (!validPassword(password)) {
            throw new RegraNegocioException("A senha deve ter pelo menos 8 caracteres e no máximo 72 bytes.");
        }
        try {
            entityManager.persist(new Account(username.toLowerCase(Locale.ROOT), passwords.encode(password)));
            entityManager.flush();
        } catch (ConstraintViolationException exception) {
            throw new ConflitoException("Esse usuário já está cadastrado.");
        }
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        if (username == null || !USERNAME.matcher(username).matches() || !validPassword(password)) {
            return false;
        }
        Account account = entityManager.find(Account.class, username.toLowerCase(Locale.ROOT));
        boolean matches = passwords.matches(password, account == null ? unknownPassword : account.passwordHash());
        return matches && account != null;
    }

    private boolean validPassword(String password) {
        return password != null && password.length() >= 8
                && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
