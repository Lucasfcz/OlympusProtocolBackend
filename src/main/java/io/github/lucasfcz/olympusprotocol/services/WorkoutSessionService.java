package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.requests.AddExerciseToSessionRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutSessionResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.BusinessException;
import io.github.lucasfcz.olympusprotocol.exceptions.ForbiddenException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.WorkoutSessionMapper;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSession;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionExercise;
import io.github.lucasfcz.olympusprotocol.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor                                       // AINDA FALTAM OS METODOS: addSet, updateSet, finish
public class WorkoutSessionService {

    private final UserRepository userRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseValidationService exerciseValidationService;
    private final WorkoutSessionMapper workoutSessionMapper;

    /*
    public WorkoutSessionResponse startFromPlan(UUID userId, UUID workoutId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserId", userId));
        validateNoActiveSession(user);
        var workoutDay = workoutDayRepository.findById(workoutId)
                        .orElseThrow(() -> new ResourceNotFoundException("WorkoutId", workoutId ));
        checkDayOwnership(workoutDay, userId);
        var session = new WorkoutSession(user, workoutDay);

    }
    */
    public WorkoutSessionResponse startFreeSession(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserId", userId));
        validateNoActiveSession(user);
        var session = new WorkoutSession(user, null);

        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    public WorkoutSessionResponse findById(UUID userId, UUID sessionId) {
        var session = getSessionOrThrow(sessionId);
        checkSessionOwnership(session, userId);

        return workoutSessionMapper.toResponse(session);
    }

    public List<WorkoutSessionResponse> findAllByUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserId", userId));

        return workoutSessionRepository.findByUser(user)
                .stream()
                .map(workoutSessionMapper::toResponse)
                .toList();
    }

    public WorkoutSessionResponse addExercise(UUID userId, UUID sessionId, AddExerciseToSessionRequest request) {
        var session = getSessionOrThrow(sessionId);
        checkSessionOwnership(session, userId);
        validateSessionNotFinished(session);
        var exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("ExerciseId", request.exerciseId()));
        var sessionExercise = new WorkoutSessionExercise(session, exercise, request.exerciseOrder());
        session.addExercise(sessionExercise);

        var warning = exerciseValidationService.checkLevelCompatibility(exercise, session.getUser());

        return warning.isPresent()
                ? workoutSessionMapper.toResponse(session, List.of(warning.get()))
                : workoutSessionMapper.toResponse(session);
    }

    public WorkoutSessionResponse deleteExercise(UUID userId, UUID sessionId, UUID exerciseId) {
        var session = getSessionOrThrow(sessionId);
        checkSessionOwnership(session, userId);
        validateSessionNotFinished(session);
        var sessionExercise = session.getExercises()
                        .stream()
                                .filter(se -> se.getExercise().getId().equals(exerciseId))
                                        .findFirst()
                                                .orElseThrow(() -> new ResourceNotFoundException("ExerciseId", exerciseId));

        session.removeExercise(sessionExercise);

        return workoutSessionMapper.toResponse(session);
    }

    public void validateSessionNotFinished(WorkoutSession session) {
        if(session.isFinished()) {
            throw new BusinessException("This Session already ends");
        }
    }

    public void validateNoActiveSession(User user) {
        if(workoutSessionRepository.findByUserAndFinishedAtIsNull(user).isPresent()) {
            throw new BusinessException("You are in a session, finish this session to start another one");
        }
    }

    public WorkoutSession getSessionOrThrow(UUID sessionId) {
        var session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSessionId", sessionId));
        return session;
    }

    private void checkSessionOwnership(WorkoutSession session, UUID userId) {
        if (!session.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this workout plan.");
        }
    }

    private void checkDayOwnership(WorkoutDay day, UUID userId) {
        if (!day.getWorkoutPlan().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this workout plan.");
        }
    }
}
