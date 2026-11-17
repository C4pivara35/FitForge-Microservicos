-- Executar no Query editor do recurso Azure SQL Database de Progresso.
-- Somente leitura. Nao mostra hashes de senha ou credenciais.
SELECT DB_NAME() AS banco_atual,
       SERVERPROPERTY('ServerName') AS servidor,
       SERVERPROPERTY('Edition') AS edicao,
       SERVERPROPERTY('EngineEdition') AS engine_edition;
-- No Azure SQL Database, EngineEdition normalmente e 5.

SELECT TABLE_SCHEMA, TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
  AND TABLE_NAME IN ('sessoes', 'session_event_outbox', 'fitforge_accounts')
ORDER BY TABLE_NAME;

IF OBJECT_ID(N'dbo.sessoes', N'U') IS NOT NULL
BEGIN
    SELECT TOP (20) id, owner_username, workout_id, workout_name,
           data_sessao, duration, effort, body_weight, notes
    FROM dbo.sessoes
    ORDER BY created_at DESC;
END
ELSE
    SELECT N'Tabela dbo.sessoes ausente: confira startup, banco e DB_DDL_AUTO.' AS diagnostico;

IF OBJECT_ID(N'dbo.session_event_outbox', N'U') IS NOT NULL
BEGIN
    SELECT COUNT_BIG(*) AS eventos_pendentes FROM dbo.session_event_outbox;
    SELECT TOP (20) event_id, session_id, workout_id, occurred_at, next_attempt_at
    FROM dbo.session_event_outbox
    ORDER BY occurred_at DESC;
END
ELSE
    SELECT N'Tabela de outbox ausente: confira startup e permissoes de DDL.' AS diagnostico;

-- Para conferir uma criacao/edicao/exclusao especifica, use o ID real:
-- SELECT * FROM dbo.sessoes WHERE id = 'ID_REAL_DA_RESPOSTA_DA_API';
-- Outbox vazia depois de envio bem-sucedido e esperado.
