package br.fitforge.treinos.workouts.controller;

import br.fitforge.treinos.workouts.dto.EstatisticasDTO;
import br.fitforge.treinos.workouts.activity.WorkoutActivity;
import br.fitforge.treinos.workouts.activity.WorkoutActivities;
import br.fitforge.treinos.workouts.dto.WorkoutRequest;
import br.fitforge.treinos.workouts.model.Workout;
import br.fitforge.treinos.workouts.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** CRUD de fichas de treino. */
@RestController
@RequestMapping("/api/treinos")
@Tag(name = "Fichas de treino", description = "CRUD das fichas e exercícios do FitForge")
public class WorkoutController {

    private final WorkoutService service;
    private final WorkoutActivities activities;

    public WorkoutController(WorkoutService service, WorkoutActivities activities) {
        this.service = service;
        this.activities = activities;
    }

    @GetMapping
    @Operation(summary = "Listar fichas", description = "Filtros opcionais: ?dia=Seg e ?busca=texto")
    public List<Workout> listar(@Parameter(description = "Seg, Ter, Qua, Qui, Sex, Sáb ou Dom") @RequestParam(required = false) String dia,
                                @RequestParam(required = false) String busca) {
        return service.listar(dia, busca);
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Indicadores das fichas", description = "Totais, volume e distribuição por dia e grupo muscular")
    public EstatisticasDTO estatisticas() {
        return service.estatisticas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ficha por id")
    public Workout buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/atividade")
    @Operation(summary = "Consultar o último registro de sessão recebido pelo Service Bus")
    public WorkoutActivity atividade(@PathVariable String id) {
        service.buscar(id);
        return activities.find(id);
    }

    @PostMapping
    @Operation(summary = "Criar ficha")
    public ResponseEntity<Workout> criar(@Valid @RequestBody WorkoutRequest req) {
        Workout criado = service.criar(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ficha")
    public Workout atualizar(@PathVariable String id, @Valid @RequestBody WorkoutRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir ficha", description = "As sessões já registradas no progresso-service são preservadas")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }
}
