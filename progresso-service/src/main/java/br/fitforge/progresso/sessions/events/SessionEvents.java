package br.fitforge.progresso.sessions.events;

import br.fitforge.progresso.sessions.model.Session;
import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SessionEvents {
    private final SessionOutboxRepository outbox;
    private final Clock clock;
    private final boolean enabled;

    public SessionEvents(SessionOutboxRepository outbox, Clock clock,
                         @Value("${app.service-bus.enabled}") boolean enabled) {
        this.outbox = outbox;
        this.clock = clock;
        this.enabled = enabled;
    }

    // Chamado dentro da mesma transação que registra a sessão.
    public void registered(Session session) {
        if (enabled) {
            outbox.save(new SessionRegistered(UUID.randomUUID().toString(),
                    session.getId(), session.getWorkoutId(), session.getOwner(), clock.instant()));
        }
    }
}
