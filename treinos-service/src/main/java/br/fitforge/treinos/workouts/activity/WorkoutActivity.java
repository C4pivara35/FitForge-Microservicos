package br.fitforge.treinos.workouts.activity;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("atividades_fichas")
public record WorkoutActivity(@MongoId(FieldType.STRING) String workoutId, Instant lastSessionRecordedAt) {}
