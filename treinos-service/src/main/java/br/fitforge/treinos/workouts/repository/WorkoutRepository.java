package br.fitforge.treinos.workouts.repository;

import br.fitforge.treinos.workouts.model.Workout;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkoutRepository extends MongoRepository<Workout, String> {
}