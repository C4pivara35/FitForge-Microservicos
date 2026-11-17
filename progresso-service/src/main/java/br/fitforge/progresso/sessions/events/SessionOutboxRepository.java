package br.fitforge.progresso.sessions.events;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SessionOutboxRepository extends JpaRepository<SessionRegistered, String> {
    List<SessionRegistered> findTop20ByNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(Instant now);

    @Transactional
    @Modifying
    @Query("update SessionRegistered e set e.nextAttemptAt = :time where e.eventId = :id")
    int defer(@Param("id") String eventId, @Param("time") Instant time);
}
