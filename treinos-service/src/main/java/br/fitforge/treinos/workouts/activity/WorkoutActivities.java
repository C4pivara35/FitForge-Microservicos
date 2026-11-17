package br.fitforge.treinos.workouts.activity;

import br.fitforge.treinos.workouts.model.Workout;
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

    public void record(String workoutId, String owner, Instant occurredAt) {
        Query workout = Query.query(Criteria.where("id").is(workoutId).and("owner").is(owner));
        if (!mongo.exists(workout, Workout.class)) return;
        Query query = Query.query(Criteria.where("workoutId").is(workoutId));
        Update update = new Update().max("lastSessionRecordedAt", Date.from(occurredAt)).set("owner", owner);
        mongo.upsert(query, update, WorkoutActivity.class);
    }

    public WorkoutActivity find(String workoutId, String owner) {
        Query query = Query.query(Criteria.where("workoutId").is(workoutId).and("owner").is(owner));
        WorkoutActivity activity = mongo.findOne(query, WorkoutActivity.class);
        return activity == null ? new WorkoutActivity(workoutId, owner, null) : activity;
    }
}
