package br.fitforge.treinos.workouts.activity;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.service-bus.enabled", havingValue = "true")
public class SessionEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(SessionEventConsumer.class);
    private static final String EVENT_TYPE = "fitforge.sessao-registrada.v1";

    @Bean(initMethod = "start", destroyMethod = "close")
    ServiceBusProcessorClient sessionProcessor(@Value("${app.service-bus.connection-string}") String connection,
                                               @Value("${app.service-bus.queue}") String queue,
                                               ObjectMapper json, WorkoutActivities activities) {
        return new ServiceBusClientBuilder().connectionString(connection)
                .processor()
                .queueName(queue)
                .disableAutoComplete()
                .processMessage(context -> consume(context, json, activities))
                .processError(context -> log.warn("Falha no consumidor Service Bus tipo={}",
                        context.getException().getClass().getSimpleName()))
                .buildProcessorClient();
    }

    private void consume(ServiceBusReceivedMessageContext context, ObjectMapper json, WorkoutActivities activities) {
        SessionEvent event;
        try {
            event = json.readValue(context.getMessage().getBody().toString(), SessionEvent.class);
        } catch (JsonProcessingException error) {
            deadLetter(context);
            return;
        }
        if (!EVENT_TYPE.equals(context.getMessage().getSubject()) || event == null
                || event.workoutId() == null || event.workoutId().isBlank() || event.occurredAt() == null) {
            deadLetter(context);
            return;
        }
        try {
            activities.record(event.workoutId(), event.owner(), event.occurredAt());
        } catch (RuntimeException error) {
            log.warn("Projecao pendente id={} tipo={}", event.eventId(), error.getClass().getSimpleName());
            context.abandon();
            return;
        }

        context.complete();
        log.info("Evento processado id={} ficha={}", event.eventId(), event.workoutId());
    }

    private void deadLetter(ServiceBusReceivedMessageContext context) {
        context.deadLetter(new DeadLetterOptions().setDeadLetterReason("Evento invalido"));
    }

    record SessionEvent(String eventId, String sessionId, String workoutId, String owner, Instant occurredAt) {}
}
