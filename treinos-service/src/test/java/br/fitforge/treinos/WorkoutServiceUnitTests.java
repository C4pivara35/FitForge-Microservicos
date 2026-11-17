package br.fitforge.treinos;

import br.fitforge.treinos.exception.ConflitoException;
import br.fitforge.treinos.workouts.dto.ExerciseRequest;
import br.fitforge.treinos.workouts.dto.WorkoutRequest;
import br.fitforge.treinos.workouts.model.Workout;
import br.fitforge.treinos.workouts.repository.WorkoutRepository;
import br.fitforge.treinos.workouts.service.WorkoutService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkoutServiceUnitTests {
    private final WorkoutRepository repository = mock(WorkoutRepository.class);
    private final WorkoutService service = new WorkoutService(repository);
    private final WorkoutRequest entry = new WorkoutRequest("ficha", "  Treino A  ", "", List.of("Sex", "Seg"),
            List.of(new ExerciseRequest("supino", "Supino", "Peito", 3, 10, new BigDecimal("20.25"), 90)));

    @Test
    void normalizaDiasEConservaCargaDecimal() {
        when(repository.insert(any(Workout.class))).thenAnswer(call -> call.getArgument(0));

        Workout workout = service.criar(null, entry);

        assertThat(workout.getName()).isEqualTo("Treino A");
        assertThat(workout.getDays()).containsExactly("Seg", "Sex");
        assertThat(workout.getVolume()).isEqualByComparingTo("607.50");
    }

    @Test
    void traduzColisaoDeIdConcorrenteEmConflito() {
        when(repository.insert(any(Workout.class))).thenThrow(new DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> service.criar(null, entry)).isInstanceOf(ConflitoException.class);
    }
}
