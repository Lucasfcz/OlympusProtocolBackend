package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.TestFactory;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutDayExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutDayRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutPlanRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutPlanResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.ForbiddenException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.WorkoutPlanMapper;
import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutPlan;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.repositories.ExerciseRepository;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutDayRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutPlanService Tests")
class WorkoutPlanServiceTest {

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID OTHER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final UUID PLAN_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
    private static final UUID DAY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
    private static final UUID EXERCISE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutPlanMapper workoutPlanMapper;

    @Mock
    private ExerciseValidationService exerciseValidationService;

    @InjectMocks
    private WorkoutPlanService workoutPlanService;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = TestFactory.makeUser(USER_ID, ExperienceLevel.INTERMEDIATE);
        otherUser = TestFactory.makeUser(OTHER_USER_ID, ExperienceLevel.BEGINNER);
    }

    @Test
    @DisplayName("create should save plan successfully when user exists")
    void create_validRequest_shouldSaveAndReturnResponse() {
        // Arrange
        var request = TestFactory.samplePlanRequest();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        var plan = TestFactory.makePlan(user);
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(plan);
        var response = new WorkoutPlanResponse(PLAN_ID, plan.getName(), request.goal(), true, null, List.of(), List.of());
        when(workoutPlanMapper.toResponse(plan)).thenReturn(response);

        // Act
        var result = workoutPlanService.create(USER_ID, request);

        // Assert
        assertNotNull(result);
        assertEquals(request.name(), result.name());
        verify(workoutPlanRepository).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("create should throw ResourceNotFoundException when user not found")
    void create_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> workoutPlanService.create(USER_ID, TestFactory.samplePlanRequest()));
    }

    @Test
    @DisplayName("findAllByUser should return only active plans for user")
    void findAllByUser_withActivePlans_shouldReturnOnlyActiveList() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        var plan = TestFactory.makePlan(user);
        when(workoutPlanRepository.findByUserAndActiveTrue(user)).thenReturn(List.of(plan));
        when(workoutPlanMapper.toResponse(plan)).thenReturn(
            new WorkoutPlanResponse(PLAN_ID, plan.getName(), null, true, null, List.of(), List.of())
        );

        // Act
        var result = workoutPlanService.findAllByUser(USER_ID);

        // Assert
        assertEquals(1, result.size());
        verify(workoutPlanRepository).findByUserAndActiveTrue(user);
    }

    @Test
    @DisplayName("findById should return plan when user is owner")
    void findById_userOwnsplan_shouldReturnResponse() {
        // Arrange
        var plan = TestFactory.makePlan(user);
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(workoutPlanMapper.toResponse(plan)).thenReturn(
            new WorkoutPlanResponse(PLAN_ID, plan.getName(), null, true, null, List.of(), List.of())
        );

        // Act
        var result = workoutPlanService.findById(USER_ID, PLAN_ID);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("findById should throw ResourceNotFoundException when plan not found")
    void findById_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> workoutPlanService.findById(USER_ID, PLAN_ID));
    }

    @Test
    @DisplayName("findById should throw ForbiddenException when user does not own plan")
    void findById_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var plan = new WorkoutPlan(otherUser, "Other Plan");
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        // Act & Assert
        assertThrows(ForbiddenException.class,
            () -> workoutPlanService.findById(USER_ID, PLAN_ID));
    }

    @Test
    @DisplayName("addDay should successfully add day to plan when user is owner")
    void addDay_validRequest_shouldAddDayAndReturnResponse() {
        // Arrange
        var plan = TestFactory.makePlan(user);
        var request = TestFactory.sampleDayRequest();
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(workoutPlanRepository.save(plan)).thenReturn(plan);
        when(workoutPlanMapper.toResponse(plan)).thenReturn(
            new WorkoutPlanResponse(PLAN_ID, plan.getName(), null, true, null, List.of(), List.of())
        );

        // Act
        var result = workoutPlanService.addDay(USER_ID, PLAN_ID, request);

        // Assert
        assertNotNull(result);
        verify(workoutPlanRepository).save(plan);
    }

    @Test
    @DisplayName("addDay should throw ForbiddenException when user does not own plan")
    void addDay_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var plan = new WorkoutPlan(otherUser, "Other Plan");
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        // Act & Assert
        assertThrows(ForbiddenException.class,
            () -> workoutPlanService.addDay(USER_ID, PLAN_ID, TestFactory.sampleDayRequest()));
    }

    @Test
    @DisplayName("addExerciseToDay should successfully add exercise without warning when levels match")
    void addExerciseToDay_exerciseLevelCompatible_shouldAddWithoutWarning() {
        // Arrange
        var plan = TestFactory.makePlan(user);
        var day = new WorkoutDay(plan, "Upper Day", 1);
        var exercise = TestFactory.makeExercise(ExperienceLevel.BEGINNER);
        var request = TestFactory.sampleDayExerciseRequest(EXERCISE_ID);

        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(workoutDayRepository.findById(DAY_ID)).thenReturn(Optional.of(day));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exercise));
        when(exerciseValidationService.checkLevelCompatibility(exercise, plan.getUser())).thenReturn(Optional.empty());
        when(workoutDayRepository.save(day)).thenReturn(day);
        when(workoutPlanMapper.toResponse(plan)).thenReturn(
            new WorkoutPlanResponse(PLAN_ID, plan.getName(), null, true, null, List.of(), List.of())
        );

        // Act
        var result = workoutPlanService.addExerciseToDay(USER_ID, PLAN_ID, DAY_ID, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    @DisplayName("addExerciseToDay should successfully add exercise with warning when exercise level is higher")
    void addExerciseToDay_exerciseLevelHigher_shouldAddWithWarning() {
        // Arrange
        var plan = TestFactory.makePlan(user);
        var day = new WorkoutDay(plan, "Upper Day", 1);
        var exercise = TestFactory.makeExercise(ExperienceLevel.EXPERT);
        var request = TestFactory.sampleDayExerciseRequest(EXERCISE_ID);
        var warningMessage = "This exercise is recommended for EXPERT and your level is INTERMEDIATE. Be careful.";

        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(workoutDayRepository.findById(DAY_ID)).thenReturn(Optional.of(day));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exercise));
        when(exerciseValidationService.checkLevelCompatibility(exercise, plan.getUser()))
            .thenReturn(Optional.of(warningMessage));
        when(workoutDayRepository.save(day)).thenReturn(day);
        when(workoutPlanMapper.toResponse(plan, List.of(warningMessage))).thenReturn(
            new WorkoutPlanResponse(PLAN_ID, plan.getName(), null, true, null, List.of(), List.of(warningMessage))
        );

        // Act
        var result = workoutPlanService.addExerciseToDay(USER_ID, PLAN_ID, DAY_ID, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.warnings().size());
        assertTrue(result.warnings().get(0).contains("EXPERT"));
    }

    @Test
    @DisplayName("addExerciseToDay should throw ResourceNotFoundException when plan not found")
    void addExerciseToDay_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> workoutPlanService.addExerciseToDay(USER_ID, PLAN_ID, DAY_ID,
                TestFactory.sampleDayExerciseRequest(EXERCISE_ID)));
    }

    @Test
    @DisplayName("deactivate should successfully deactivate plan when user is owner")
    void deactivate_validRequest_shouldDeactivateAndSave() {
        // Arrange
        var plan = TestFactory.makePlan(user);
        assertTrue(plan.isActive());
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        // Act
        workoutPlanService.deactivate(USER_ID, PLAN_ID);

        // Assert
        assertFalse(plan.isActive());
        verify(workoutPlanRepository).save(plan);
    }

    @Test
    @DisplayName("deactivate should throw ForbiddenException when user does not own plan")
    void deactivate_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var plan = new WorkoutPlan(otherUser, "Other Plan");
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        // Act & Assert
        assertThrows(ForbiddenException.class,
            () -> workoutPlanService.deactivate(USER_ID, PLAN_ID));
    }

    @Test
    @DisplayName("reactivate should successfully reactivate plan when user is owner")
    void reactivate_inactivePlan_shouldReactivateAndSave() {
        // Arrange
        var plan = TestFactory.makePlan(user);
        plan.deactivate();
        assertFalse(plan.isActive());
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        // Act
        workoutPlanService.reactivate(USER_ID, PLAN_ID);

        // Assert
        assertTrue(plan.isActive());
        verify(workoutPlanRepository).save(plan);
    }

    @Test
    @DisplayName("reactivate should throw ForbiddenException when user does not own plan")
    void reactivate_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var plan = new WorkoutPlan(otherUser, "Other Plan");
        when(workoutPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        // Act & Assert
        assertThrows(ForbiddenException.class,
            () -> workoutPlanService.reactivate(USER_ID, PLAN_ID));
    }
}


