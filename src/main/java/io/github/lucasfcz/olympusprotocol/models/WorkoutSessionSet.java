package io.github.lucasfcz.olympusprotocol.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workout_session_sets")
public class WorkoutSessionSet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_session_exercise_id", nullable = false)
    private WorkoutSessionExercise workoutSessionExercise;

    // Ordering the number of series for exercise
    @Column(nullable = false)
    private Integer setOrder;

    @Column(nullable = false)
    private Integer reps;

    @Column // can be null, because some exercises uses the bodyweight
    private Double weight;

    @Column
    private Integer restTime;

    public WorkoutSessionSet(WorkoutSessionExercise workoutSessionExercise, Integer setOrder, Integer reps, Double weight, Integer restTime) {
        this.workoutSessionExercise = workoutSessionExercise;
        this.setOrder = setOrder;
        this.reps = reps;
        this.weight = weight;
        this.restTime = restTime;
    }

    public void updateSet(Integer setOrder, Integer reps, Double weight, Integer restTime) {
        this.setOrder = setOrder;
        this.reps = reps;
        this.weight = weight;
        this.restTime = restTime;
    }

    public void updateOrder(Integer newOrder) {
        this.setOrder = newOrder;
    }

    public Double setVolume() {
        if (weight == null || reps == null) {
            return 0.0;
        }
        return weight * reps;
    }
}
