package br.fitforge.progresso;

import br.fitforge.progresso.sessions.events.SessionEventPublisher.OutboxDispatch;
import br.fitforge.progresso.sessions.events.SessionOutboxRepository;
import br.fitforge.progresso.sessions.events.SessionRegistered;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxUnitTests {
    private final Instant now = Instant.parse("2026-01-10T13:00:00Z");
    private final SessionOutboxRepository outbox = mock(SessionOutboxRepository.class);
    @SuppressWarnings("unchecked")
    private final Consumer<ServiceBusMessage> sender = mock(Consumer.class);
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();
    private final OutboxDispatch dispatch = new OutboxDispatch(outbox, sender, json, Clock.fixed(now, ZoneOffset.UTC));

    @Test
    void confirmaEventoComIdentificadorEstavelDepoisDoEnvio() throws Exception {
        when(outbox.findTop20ByNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(now))
                .thenReturn(List.of(new SessionRegistered("evento-1", "sessao-1", "treino-1", null, now)));

        dispatch.dispatch();

        var message = ArgumentCaptor.forClass(ServiceBusMessage.class);
        var order = inOrder(sender, outbox);
        order.verify(sender).accept(message.capture());
        order.verify(outbox).deleteById("evento-1");
        assertThat(message.getValue().getMessageId()).isEqualTo("evento-1");
        assertThat(message.getValue().getSubject()).isEqualTo("fitforge.sessao-registrada.v1");
        var body = json.readTree(message.getValue().getBody().toString());
        assertThat(body.path("workoutId").asText()).isEqualTo("treino-1");
    }

    @Test
    void reagendaQuandoBrokerFalha() {
        when(outbox.findTop20ByNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(now))
                .thenReturn(List.of(new SessionRegistered("evento-1", "sessao-1", "treino-1", null, now)));
        doThrow(new IllegalStateException("broker indisponível")).when(sender).accept(any());

        dispatch.dispatch();

        verify(outbox, never()).deleteById("evento-1");
        verify(outbox).defer("evento-1", now.plusSeconds(30));
    }
}
