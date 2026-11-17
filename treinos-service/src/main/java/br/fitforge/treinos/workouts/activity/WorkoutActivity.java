package br.fitforge.treinos.workouts.activity;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("atividades_fichas")
public record WorkoutActivity(@MongoId(FieldType.STRING) String workoutId, @JsonIgnore String owner, Instant lastSessionRecordedAt) {}
