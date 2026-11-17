package br.fitforge.treinos.workouts.activity;

import java.time.Instant;
import java.util.Date;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class WorkoutActivities {
    private final MongoTemplate mongo;

    public WorkoutActivities(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public void record(String workoutId, Instant occurredAt) {
        Query query = Query.query(Criteria.where("workoutId").is(workoutId));
        Update update = new Update().max("lastSessionRecordedAt", Date.from(occurredAt));
        mongo.upsert(query, update, WorkoutActivity.class);
    }

    public WorkoutActivity find(String workoutId) {
        WorkoutActivity activity = mongo.findById(workoutId, WorkoutActivity.class);
        return activity == null ? new WorkoutActivity(workoutId, null) : activity;
    }
}
