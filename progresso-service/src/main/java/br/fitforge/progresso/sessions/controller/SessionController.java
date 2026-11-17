package br.fitforge.progresso.sessions.controller;

import br.fitforge.progresso.sessions.dto.EstatisticasDTO;
import br.fitforge.progresso.sessions.dto.SessionRequest;
import br.fitforge.progresso.sessions.model.Session;
import br.fitforge.progresso.sessions.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** CRUD de sessões de treino. */
@RestController
@RequestMapping("/api/sessoes")
@Tag(name = "Sessões de treino", description = "CRUD do histórico de treinos e indicadores de progresso")
public class SessionController {
    private static final String OWNER = "X-FitForge-Owner";

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar sessões", description = "Mais recentes primeiro. Filtros: ?workoutId=...&periodo=semana|mes|tudo")
    public List<Session> listar(
                                @RequestHeader(name = OWNER, required = false) String owner,
                                @RequestParam(required = false) String workoutId,
                                @Parameter(description = "semana, mes ou tudo") @RequestParam(required = false) String periodo) {
        return service.listar(owner, workoutId, periodo);
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Indicadores de progresso", description = "Totais, semana atual (seg–dom), esforço médio, último peso e sequência")
    public EstatisticasDTO estatisticas(@RequestHeader(name = OWNER, required = false) String owner) {
        return service.estatisticas(owner);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão por id")
    public Session buscar(@RequestHeader(name = OWNER, required = false) String owner, @PathVariable String id) {
        return service.buscar(owner, id);
    }

    @PostMapping
    @Operation(summary = "Registrar sessão")
    public ResponseEntity<Session> criar(@RequestHeader(name = OWNER, required = false) String owner, @Valid @RequestBody SessionRequest req) {
        Session criada = service.criar(owner, req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar sessão")
    public Session atualizar(@RequestHeader(name = OWNER, required = false) String owner, @PathVariable String id, @Valid @RequestBody SessionRequest req) {
        return service.atualizar(owner, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir sessão")
    public void excluir(@RequestHeader(name = OWNER, required = false) String owner, @PathVariable String id) {
        service.excluir(owner, id);
    }
}
