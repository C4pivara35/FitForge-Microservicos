package br.fitforge.treinos.workouts.repository;

import br.fitforge.treinos.workouts.model.Workout;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkoutRepository extends MongoRepository<Workout, String> {
    List<Workout> findAllByOwner(String owner);

    Optional<Workout> findByIdAndOwner(String id, String owner);

}