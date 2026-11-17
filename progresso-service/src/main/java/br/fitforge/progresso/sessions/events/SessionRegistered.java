package br.fitforge.progresso.sessions.events;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "session_event_outbox")
public class SessionRegistered {
    @Id
    private String eventId;
    private String sessionId;
    private String workoutId;
    private Instant occurredAt;
    private Instant nextAttemptAt;

    protected SessionRegistered() {}

    public SessionRegistered(String eventId, String sessionId, String workoutId, Instant occurredAt) {
        this.eventId = eventId;
        this.sessionId = sessionId;
        this.workoutId = workoutId;
        this.occurredAt = occurredAt;
        this.nextAttemptAt = occurredAt;
    }

    public String getEventId() { return eventId; }
    public String getSessionId() { return sessionId; }
    public String getWorkoutId() { return workoutId; }
    public Instant getOccurredAt() { return occurredAt; }
}
