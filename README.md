# FitForge — Microsserviços

Backend Java 21/Spring Boot, Maven multimódulo. Treinos e Progresso têm domínios e persistências separados. Cada módulo possui CRUD, Swagger, testes unitários e testes de arquitetura.

**Integrantes:** Andryus Zolet, Nicolas Medeiros, Valentin Rojas e Leonardo Henrique.

| Módulo | Porta local | Banco na Azure | Domínio |
|---|---|---|---|
| `treinos-service` | 8081 | MongoDB/CosmosDB compatível | Fichas e exercícios |
| `progresso-service` | 8082 | Azure SQL Database | Sessões e histórico |

## Acesso e repositórios

O frontend usa o Gateway. Dados dos serviços diretos devem exigir a chave interna.

- [Frontend Azure](https://c4pivara35.z21.web.core.windows.net/).
- [Gateway Azure](https://fitforge-gateway-hvaqcubhf8d0a2hc.chilecentral-01.azurewebsites.net).
- [Swagger Treinos](https://fitforge-treinos-cchfd7b9dyf7cjgu.mexicocentral-01.azurewebsites.net/swagger-ui.html).
- [Swagger Progresso](https://fitforge-progresso-gvfgfua7b0dggrht.mexicocentral-01.azurewebsites.net/swagger-ui.html).
- [Repositório frontend](https://github.com/C4pivara35/FitForge).
- [Repositório Gateway](https://github.com/C4pivara35/FitForge-API_Gateway).

## Execução local

JDK 21 e Maven. Suba MongoDB na porta 27017, por exemplo com Docker Desktop iniciado:

```powershell
docker run --name fitforge-mongo-local -d -p 127.0.0.1:27017:27017 -v fitforge-mongo-local:/data/db mongo:7.0
```

Se esse container já existir, use `docker start fitforge-mongo-local`. Em dois terminais separados, a partir da raiz:

```powershell
mvn -pl treinos-service spring-boot:run
```

```powershell
mvn -pl progresso-service spring-boot:run
```

Treinos usa Mongo local por padrão e aceita `MONGODB_URI`/`MONGODB_DATABASE`. Progresso usa H2 local. Service Bus fica desativado no modo local por padrão. Swagger local: `http://localhost:8081/swagger-ui.html` e `http://localhost:8082/swagger-ui.html`.

## Testes

```powershell
mvn -B clean verify
```

Treinos usa Testcontainers com Docker ou um Mongo de teste indicado em `FITFORGE_TEST_MONGODB_URI`, criando bancos temporários por contexto. Progresso usa H2 para testes rápidos. Há testes de serviços isolados, outbox, CRUD, contas, arquitetura e projeção idempotente. Os testes não usam o perfil Azure e não exigem credenciais de produção. H2 aprovado em teste não comprova SQL na nuvem.

## Azure SQL e perfil Azure

Configure `SPRING_PROFILES_ACTIVE=azure` nos dois apps. No Progresso, defina `DB_URL` JDBC SQL Server com TLS, `DB_USER`, `DB_PASSWORD` e `DB_DDL_AUTO=update` para bootstrap. O perfil exige URL e driver SQL Server, evitando operar silenciosamente com H2. Pool máximo: três conexões. Depois de criar o esquema, `validate` e migrações versionadas são evoluções recomendadas.

Em Treinos, mantenha `MONGODB_URI` real e `MONGODB_DATABASE=fitforge-treinos`. Em ambos, `SEED_DATA=false`. Libere os IPs de saída de Progresso no servidor SQL e os de Treinos no cluster Mongo. Ative `FITFORGE_INTERNAL_KEY` com o mesmo segredo do Gateway.

[SQL de inspeção somente leitura](docs/sql/VERIFICAR-AZURE-SQL.sql) para executar no Query editor do recurso Azure SQL real. Não existem senhas de banco neste repositório. Dados do H2 anterior não migram automaticamente.

## Service Bus e regra de negócio

Ao criar uma sessão, Progresso salva sessão e outbox na mesma transação SQL. O publicador envia `fitforge.sessao-registrada.v1` à fila `fitforge-sessoes`. Treinos consome e atualiza `atividades_fichas.lastSessionRecordedAt` no Mongo para a ficha/proprietário correspondente. A consulta `/api/treinos/{id}/atividade` expõe esse efeito pelo Gateway.

Nos dois apps: `SERVICE_BUS_ENABLED=true`, `SERVICE_BUS_QUEUE=fitforge-sessoes` e `SERVICE_BUS_CONNECTION_STRING`. Use política **Send** em Progresso e **Listen** em Treinos. O transporte usa AMQP sobre WebSockets/443. A fila não deve exigir Service Bus Sessions.

Há retry da outbox e confirmação manual no consumidor. Entrega é at-least-once; o maior timestamp torna a projeção atual idempotente. Atualização/exclusão de sessão não desfaz a projeção de registro. Não se afirma exactly-once nem contagem exata de sessões pelo evento.

## Publicação

Os dois workflows existentes compilam, testam e publicam `app.jar` por módulo. Conexões reais e perfil ativo são configurados nos App Services. Documentação na pasta raiz `docs/` não dispara esses deploys. O perfil Azure exige recursos preparados; siga o guia do pacote antes de ativá-lo.

## Documentação e entrega

- [Arc42 completa: 12 seções](docs/ARC42.md).
- [Checklist de validação e evidências](docs/VALIDACAO-ENTREGA.md).
- [Guia detalhado de Azure SQL, Service Bus e publicação](docs/DEPLOY-AZURE.md).
- [Integrantes](GRUPO.md).

A arquitetura adotada é **Vertical Slice Architecture**, com separação interna de responsabilidades e dependências verificadas por testes. A Arc42 detalha a aplicação de SOLID e os limites do desenho. A conclusão de SQL/mensageria depende da configuração e das evidências na Azure; código e CI aprovados não comprovam essa ativação.

**Vídeo:** pendente de gravação/publicação não listada no YouTube. Inserir aqui o link após validar o fluxo em nuvem, com todos os integrantes falando e com câmera aberta.
