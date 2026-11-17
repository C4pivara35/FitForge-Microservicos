package br.fitforge.progresso.sessions.repository;

import br.fitforge.progresso.sessions.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findAllByOwnerOrderByDateDescCreatedAtDesc(String owner);

    List<Session> findByOwnerAndWorkoutIdOrderByDateDescCreatedAtDesc(String owner, String workoutId);

    Optional<Session> findByIdAndOwner(String id, String owner);
}
