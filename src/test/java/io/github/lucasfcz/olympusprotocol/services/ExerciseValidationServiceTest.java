package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.TestFactory;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExerciseValidationService Tests")
class ExerciseValidationServiceTest {

    private final ExerciseValidationService service = new ExerciseValidationService();

    @Test
    @DisplayName("checkLevelCompatibility should return empty when exercise level equals user level")
    void checkLevelCompatibility_equalExperienceLevels_shouldReturnEmpty() {
        // Arrange
        var exercise = TestFactory.makeExercise(ExperienceLevel.BEGINNER);
        var user = TestFactory.makeUser(ExperienceLevel.BEGINNER);

        // Act
        var result = service.checkLevelCompatibility(exercise, user);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("checkLevelCompatibility should return empty when exercise level is lower than user level")
    void checkLevelCompatibility_exerciseLevelLower_shouldReturnEmpty() {
        // Arrange
        var exercise = TestFactory.makeExercise(ExperienceLevel.BEGINNER);
        var user = TestFactory.makeUser(ExperienceLevel.INTERMEDIATE);

        // Act
        var result = service.checkLevelCompatibility(exercise, user);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("checkLevelCompatibility should return warning when exercise level is higher than user level")
    void checkLevelCompatibility_exerciseLevelHigher_shouldReturnWarning() {
        // Arrange
        var exercise = TestFactory.makeExercise(ExperienceLevel.EXPERT);
        var user = TestFactory.makeUser(ExperienceLevel.BEGINNER);

        // Act
        var result = service.checkLevelCompatibility(exercise, user);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("EXPERT"));
        assertTrue(result.get().contains("BEGINNER"));
    }

    @Test
    @DisplayName("checkLevelCompatibility should handle all ExperienceLevel combinations")
    void checkLevelCompatibility_intermediateUserWithAdvancedExercise_shouldReturnWarning() {
        // Arrange
        var exercise = TestFactory.makeExercise(ExperienceLevel.ADVANCED);
        var user = TestFactory.makeUser(ExperienceLevel.INTERMEDIATE);

        // Act
        var result = service.checkLevelCompatibility(exercise, user);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("recommended for"));
    }
}

