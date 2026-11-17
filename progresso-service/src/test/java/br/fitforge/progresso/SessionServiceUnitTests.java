package br.fitforge.progresso;

import br.fitforge.progresso.exception.RegraNegocioException;
import br.fitforge.progresso.sessions.dto.SessionRequest;
import br.fitforge.progresso.sessions.events.SessionEvents;
import br.fitforge.progresso.sessions.model.Session;
import br.fitforge.progresso.sessions.repository.SessionRepository;
import br.fitforge.progresso.sessions.service.SessionService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SessionServiceUnitTests {
    private final SessionRepository repository = mock(SessionRepository.class);
    private final SessionEvents events = mock(SessionEvents.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-10T13:00:00Z"), ZoneOffset.UTC);
    private final SessionService service = new SessionService(repository, clock, events);

    @Test
    void registraEventoDaSessaoPersistida() {
        when(repository.save(any(Session.class))).thenAnswer(call -> call.getArgument(0));

        Session result = service.criar(request(LocalDate.now(clock)));

        var order = inOrder(repository, events);
        order.verify(repository).save(result);
        order.verify(events).registered(result);
    }

    @Test
    void rejeitaDataFuturaAntesDeGravarOuEmitir() {
        assertThatThrownBy(() -> service.criar(request(LocalDate.now(clock).plusDays(1))))
                .isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    private static SessionRequest request(LocalDate date) {
        return new SessionRequest("sessao", "treino", "Treino A", date, 45, 7, null, "");
    }
}
