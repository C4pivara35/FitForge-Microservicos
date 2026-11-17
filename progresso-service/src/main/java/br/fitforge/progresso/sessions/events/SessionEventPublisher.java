package br.fitforge.progresso.sessions.events;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "app.service-bus.enabled", havingValue = "true")
public class SessionEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(SessionEventPublisher.class);
    private static final String EVENT_TYPE = "fitforge.sessao-registrada.v1";

    @Bean(destroyMethod = "close")
    ServiceBusSenderClient sessionSender(@Value("${app.service-bus.connection-string}") String connection,
                                         @Value("${app.service-bus.queue}") String queue) {
        return new ServiceBusClientBuilder().connectionString(connection)
                .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
                .sender().queueName(queue).buildClient();
    }

    @Bean
    OutboxDispatch outboxDispatch(SessionOutboxRepository outbox, ServiceBusSenderClient sender,
                                 ObjectMapper json, Clock clock) {
        return new OutboxDispatch(outbox, sender::sendMessage, json, clock);
    }

    public record OutboxDispatch(SessionOutboxRepository outbox, Consumer<ServiceBusMessage> sender,
                                 ObjectMapper json, Clock clock) {
        @Scheduled(fixedDelayString = "${SERVICE_BUS_POLL_MS:3000}")
        public void dispatch() {
            for (SessionRegistered event : outbox.findTop20ByNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(clock.instant())) {
                try {
                    ServiceBusMessage message = new ServiceBusMessage(json.writeValueAsString(event));
                    message.setContentType("application/json");
                    message.setSubject(EVENT_TYPE);
                    message.setMessageId(event.getEventId());
                    sender.accept(message);
                    outbox.deleteById(event.getEventId());
                    log.info("Evento enviado id={} ficha={}", event.getEventId(), event.getWorkoutId());
                } catch (Exception error) {
                    outbox.defer(event.getEventId(), clock.instant().plusSeconds(30));
                    log.warn("Envio pendente id={} tipo={}", event.getEventId(), error.getClass().getSimpleName());
                }
            }
        }
    }
}
