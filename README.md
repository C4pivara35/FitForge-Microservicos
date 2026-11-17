# FitForge — Microsserviços

Projeto Maven multimódulo, com JDK 21 e Spring Boot. As fatias verticais `workouts` e `sessions` reúnem modelos, API e persistência do respectivo domínio. Cada serviço tem seu próprio armazenamento.

| Módulo | Porta local | Banco da Azure | Rota |
|---|---|---|---|
| `treinos-service` | 8081 | Azure DocumentDB compatível com MongoDB ou MongoDB, banco `fitforge-treinos` | `/api/treinos` |
| `progresso-service` | 8082 | Azure SQL Database | `/api/sessoes` |

Os dois serviços expõem CRUD, indicadores e Swagger em `/swagger-ui.html`. Na nuvem, o Gateway envia a chave interna aos caminhos `/api/**`.

Ao criar sessão com o broker habilitado, Progresso salva a sessão e seu evento em uma transação. A outbox reenvia mensagens pendentes ao Azure Service Bus. Treinos recebe a mensagem `fitforge.sessao-registrada.v1` e projeta a data do último registro recebido na coleção `atividades_fichas`. Consulte em `/api/treinos/{id}/atividade`. A atualização utiliza o maior timestamp, preservando a data em caso de eventos atrasados ou repetidos.

## Rodar e testar

Suba um MongoDB local na porta 27017. Os comandos abaixo devem rodar em terminais separados, a partir desta pasta.

```powershell
mvn -pl treinos-service spring-boot:run
mvn -pl progresso-service spring-boot:run
```

Treinos aceita `MONGODB_URI` e `MONGODB_DATABASE`. Progresso usa H2 local por padrão e aceita `DB_URL`, `DB_USER`, `DB_PASSWORD` e `DB_DDL_AUTO` para Azure SQL. `SEED_DATA=false` evita inserir fichas e sessões de exemplo.

Em nuvem, configure `FITFORGE_INTERNAL_KEY` nos dois serviços e no Gateway. Configure `SERVICE_BUS_ENABLED=true`, `SERVICE_BUS_QUEUE=fitforge-sessoes` e `SERVICE_BUS_CONNECTION_STRING` com conexão Listen para Treinos e Send para Progresso.

Rode `mvn -B clean verify` com o Docker ativo. A suíte de Treinos usa um MongoDB temporário, ou uma instância de teste indicada em `FITFORGE_TEST_MONGODB_URI`. Cada contexto tem seu próprio banco de teste. A suíte de Progresso usa H2 local. Os testes exercitam o CRUD, regras de negócio, arquitetura e projeção idempotente.

Integrantes em [GRUPO.md](GRUPO.md).
