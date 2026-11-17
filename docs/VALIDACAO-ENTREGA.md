# Validação da entrega — FitForge

Registrar a data, o commit publicado e as evidências após executar cada item. Os campos estão desmarcados intencionalmente; este documento não é prova de uma execução que ainda não ocorreu. O vídeo será tratado em etapa posterior.

## Identificação da execução

- Data/hora:
- Commit frontend:
- Commit Gateway:
- Commit backend:
- Nome do servidor/banco Azure SQL (sem senha):
- Produto Mongo utilizado (MongoDB/Atlas/CosmosDB compatível):
- Namespace/fila Service Bus (sem chave):
- Responsável pela validação:

## Código e documentação

- [ ] Três repositórios públicos com integrantes e execução local.
- [ ] Arc42 completa em `docs/ARC42.md` de cada repositório.
- [ ] Padrão vertical slice e aplicação de SOLID explicados com referências ao código.
- [ ] Testes unitários, integração e arquitetura aprovados nos workflows.
- [ ] Nenhuma senha real, connection string de produção ou publish profile publicado.

## Ambiente publicado

- [ ] Frontend abre na Azure e remotes carregam.
- [ ] Network mostra APIs somente pelo domínio do Gateway.
- [ ] Gateway sem credencial rejeita CRUD com 401.
- [ ] Serviços diretos sem chave interna rejeitam dados com 403.
- [ ] CORS permite a origem exata do Storage sem cabeçalho duplicado.
- [ ] Swagger do Gateway e de ambos os serviços acessível.
- [ ] Perfil azure ativo nos três aplicativos Java.
- [ ] Health UP e dashboard com ambos os domínios ONLINE.

## Persistência e mensageria

- [ ] Treinos usa Mongo/Cosmos e não armazenamento local do navegador.
- [ ] Criar/consultar/editar/excluir ficha refletido no banco Mongo.
- [ ] Progresso usa Azure SQL Database real; logs não mostram H2 em memória.
- [ ] Criar/consultar/editar/excluir sessão refletido em dbo.sessoes no Azure SQL.
- [ ] Exclusão de ficha preserva uma sessão histórica usada no teste.
- [ ] Namespace e fila fitforge-sessoes ativos.
- [ ] SERVICE_BUS_ENABLED=true nos dois microsserviços, com Send e Listen corretos.
- [ ] Log de envio e processamento do mesmo eventId observado.
- [ ] Coleção atividades_fichas mostra lastSessionRecordedAt para a ficha do teste.
- [ ] Dead-letter e eventos pendentes conferidos, sem apagar falhas para simular sucesso.

## Evidências a anexar ou referenciar

| Evidência | Link/caminho ou descrição conferida |
|---|---|
| Frontend Azure e Network do dashboard | |
| CRUD Mongo, documento antes/depois | |
| CRUD SQL, banco/servidor e linha antes/depois | |
| Mesmo eventId nos logs de envio/consumo | |
| Projeção Mongo e fila/métricas | |
| Actions aprovados | |
| Quotas verificadas antes da apresentação | |

Se ainda houver item pendente, registrar motivo e correção. Não escrever “validado em Azure SQL” apenas porque o health está UP ou o CI passou. Uma outbox vazia pode significar envio rápido; comprovar também o consumidor e a projeção.
