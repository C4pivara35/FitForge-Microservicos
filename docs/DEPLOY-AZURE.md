# FitForge — concluir Azure SQL, Service Bus e arquitetura

Data: 17/11/2026. Este pacote contém somente arquivos novos/modificados, separados pelo nome dos três repositórios. Nenhum segredo real deve ser colocado nos arquivos. O vídeo fica para outra etapa.

## 1. O que o pacote entrega e o que você fará no portal

| Item | Nos arquivos | Na sua Azure |
|---|---|---|
| Arc42 | 12 seções específicas em cada `docs/ARC42.md` | Nada para criar |
| Arquitetura/SOLID | Descrição por fatias verticais, classes, decisões, testes e limites | Validar o fluxo publicado |
| Azure SQL | Perfil `azure` exige DB_URL e driver SQL Server | Configurar banco, usuário, firewall e app settings |
| Service Bus | Publicador/consumidor existentes usam WebSockets/443 | Criar/reutilizar namespace e fila, políticas Send/Listen e app settings |
| Segurança interna | Perfis exigem a variável da chave interna | Mesmo segredo nos três App Services |
| Confirmação | SQL de inspeção e checklist | Executar CRUD e conferir bancos/evento |

Não basta fazer o commit para conectar os recursos. O pacote não contém sua senha SQL, sua chave do broker nem o nome confirmado do seu servidor SQL. Copie esses valores dos seus próprios recursos conforme as etapas abaixo.

Na auditoria anterior a este pacote, o SQL não estava conectado ao aplicativo e o Service Bus estava desativado. Não considere esses requisitos concluídos até a etapa 9. Não apague nem recrie o cluster Mongo que já está funcionando.

## 2. Recursos e endereços usados

Faça as etapas de infraestrutura em [portal.azure.com](https://portal.azure.com/). Faça os commits nos repositórios da conta C4pivara35.

| Componente | Nome/endereço conhecido |
|---|---|
| Frontend | `https://c4pivara35.z21.web.core.windows.net/` |
| Gateway | `fitforge-gateway` — `https://fitforge-gateway-hvaqcubhf8d0a2hc.chilecentral-01.azurewebsites.net` |
| Treinos | `fitforge-treinos` — `https://fitforge-treinos-cchfd7b9dyf7cjgu.mexicocentral-01.azurewebsites.net` |
| Progresso | `fitforge-progresso` — `https://fitforge-progresso-gvfgfua7b0dggrht.mexicocentral-01.azurewebsites.net` |
| Mongo | Reutilizar o cluster que já atende Treinos e a variável `MONGODB_URI` existente |
| Azure SQL | Abrir o seu banco existente e copiar nome e servidor reais |
| Fila | Nome adotado pelo código: `fitforge-sessoes` |

Você não precisa de outro App Service nem de mais um país. SQL, broker e apps podem estar em regiões diferentes, embora proximidade geralmente ajude na latência. Use uma região permitida pela sua assinatura e disponível para o produto. Grupos de recursos não precisam ser todos iguais.

## 3. Ordem segura para realizar tudo

1. Preparar Azure SQL, usuário e firewall (etapa 4).
2. Preparar Service Bus, fila e duas políticas (etapa 5).
3. Preparar os valores das configurações em um local privado (etapa 6).
4. Copiar os arquivos e fazer um commit por repositório (etapa 7).
5. Aguardar workflows terminarem. Como `SPRING_PROFILES_ACTIVE` ainda não foi alterado, o novo JAR continua aceitando o modo existente.
6. Aplicar todas as app settings de Treinos de uma vez, depois Progresso e por último Gateway, incluindo `SPRING_PROFILES_ACTIVE=azure` (etapa 8). A transição pode causar indisponibilidade breve até os três usarem a mesma chave.
7. Validar SQL, eventos, CRUD e fronteira de segurança (etapa 9).

Não altere uma variável por vez salvando entre cada uma. Preencha a lista completa e salve o conjunto, reduzindo reinicializações. Reiniciar/publicar e utilizar a aplicação consome recursos; não existe garantia de que uma cota F1 não se esgotará. Conferir cotas antes da gravação continua necessário.

## 4. Azure SQL — conectar Progresso ao banco real

### 4.1 Localize os dois recursos corretos

1. No portal, pesquise **SQL databases / Bancos de dados SQL**.
2. Abra o banco que você criou para Progresso. Não abra um banco Mongo/Cosmos e não crie outro se o SQL já existe.
3. Em **Overview / Visão geral**, anote **Database name / Nome do banco**.
4. Copie **Server name / Nome do servidor**, no formato `SEU-SERVIDOR.database.windows.net`.
5. Clique no nome do servidor. Esse é o **servidor lógico SQL**, onde ficam autenticação e firewall.
6. Anote a assinatura e o grupo do servidor para não alterar o recurso de outra pessoa.

O texto “Free 1 DTU” da atividade não corresponde ao nome da oferta gratuita atual documentada pela Microsoft, que usa serverless/vCore. Registre o tier real no vídeo/documentação e confirme a equivalência com o professor. O requisito principal ainda exige **Azure SQL Database**; H2 local e Mongo não o substituem.

### 4.2 Confira a autenticação

O caminho deste guia usa usuário e senha SQL, já suportados pelo projeto. Se você criou o servidor com **Microsoft Entra-only authentication**, essa modalidade bloqueia login SQL.

1. No **servidor lógico**, procure **Microsoft Entra ID** ou **Authentication / Autenticação**.
2. Confira se “Support only Microsoft Entra authentication” / “Somente autenticação Microsoft Entra” está habilitado.
3. Para seguir com usuário/senha, desabilite o modo exclusivo, preservando o administrador Entra existente. Se sua política institucional bloquear a opção, será necessário configurar identidade gerenciada e adaptar a autenticação JDBC; não tente contornar a política.
4. Em **Overview**, procure **Reset password / Redefinir senha** se precisar estabelecer a senha do administrador SQL. Anote o login SQL exibido pelo seu servidor; seu e-mail acadêmico não se torna automaticamente um login SQL.

A alteração permite autenticação SQL; ela não publica o banco sem firewall. Uma evolução futura pode usar identidade gerenciada e dispensar senha. Este pacote não afirma que essa evolução foi implementada.

### 4.3 Firewall para Progresso

1. Abra **App Services → fitforge-progresso**.
2. Abra **Networking / Rede** e localize **Outbound traffic configuration / Configuração de tráfego de saída**, ou **Properties / Propriedades** conforme a versão do portal.
3. Copie todos os **Outbound IPv4 addresses**. Se houver **Additional/Possible outbound IP addresses**, inclua os possíveis IPs ou registre a necessidade de atualizar após mudar o plano.
4. Volte ao **servidor lógico SQL → Networking / Rede**.
5. Habilite acesso público pelas redes/IPs selecionados se esse for o modelo que já está usando. Não crie endpoint privado para este procedimento.
6. Crie uma regra por IPv4: nome `progresso-01`, IP inicial igual ao IP e IP final igual ao mesmo IP. Repita até cobrir a lista completa.
7. Adicione seu IP público atual se for usar uma ferramenta SQL no seu computador ou o Query editor exigir esse acesso.
8. Salve e aguarde a aplicação das regras.

Use IPs de **Progresso** para o SQL. Os IPs de Treinos usados no firewall Mongo não são necessariamente os mesmos. O inbound IP do App Service não é o IP correto para liberar sua conexão de saída. Não use o intervalo `0.0.0.0–255.255.255.255`.

### 4.4 Usuário de aplicação restrito ao banco

Abra o **banco SQL → Query editor / Editor de consultas**, entre como administrador autorizado e confira se está no banco certo. Você também pode usar SSMS/DBeaver com esse servidor.

Crie um usuário contido no banco para a aplicação, substituindo a senha de exemplo apenas no editor SQL privado:

```sql
CREATE USER [fitforge_app] WITH PASSWORD = 'SUBSTITUA_POR_UMA_SENHA_FORTE_EXCLUSIVA', DEFAULT_SCHEMA = [dbo];
ALTER ROLE db_datareader ADD MEMBER [fitforge_app];
ALTER ROLE db_datawriter ADD MEMBER [fitforge_app];
ALTER ROLE db_ddladmin ADD MEMBER [fitforge_app];
```

Execute a criação uma vez. Se o usuário já existir, reutilize-o ou ajuste sua senha com os controles apropriados; não apague usuários ou tabelas existentes para resolver um erro. Guarde o usuário/senha em local privado. **Não salve uma versão desse SQL com sua senha no GitHub.**

`db_ddladmin` permite ao Hibernate criar o esquema no primeiro início. Depois do bootstrap, você pode usar `DB_DDL_AUTO=validate` e remover esse papel com `ALTER ROLE db_ddladmin DROP MEMBER [fitforge_app]`, mantendo leitura/escrita. Isso é uma segunda etapa opcional, após comprovar o esquema. Para a entrega inicial, `update` cria as tabelas sem solicitar que você as monte manualmente.

### 4.5 Monte a URL JDBC

Use exatamente o servidor e banco copiados em 4.1. Exemplo sem credenciais:

```text
jdbc:sqlserver://SEU-SERVIDOR.database.windows.net:1433;databaseName=SEU-BANCO;encrypt=true;trustServerCertificate=false;loginTimeout=30;
```

A URL não começa com `https://`. Não deixe `SEU-SERVIDOR` ou `SEU-BANCO` literais. Não inclua senha nela: use `DB_PASSWORD` separado. Não marque `trustServerCertificate=true` para resolver certificado; confira hostname e conexão.

Prepare estes valores para `fitforge-progresso`:

| Nome | Valor |
|---|---|
| `DB_URL` | URL JDBC montada acima |
| `DB_USER` | `fitforge_app`, se criou esse usuário |
| `DB_PASSWORD` | Senha real desse usuário |
| `DB_DDL_AUTO` | `update` no primeiro início |

O pacote H2 continua no POM para execução local/testes. Com o perfil `azure`, o driver é SQL Server e DB_URL é obrigatório. Não é necessário baixar o banco da Azure para o Git. Dados reais do H2 anterior, se houver, não são migrados automaticamente: exporte/reimporte seus registros necessários antes da troca. Os quatro registros `demo-*` são exemplos, e não evidência de persistência SQL.

## 5. Service Bus — criar a comunicação entre os serviços

### 5.1 Namespace

1. Pesquise **Service Bus** no portal e confira se já existe um namespace do seu projeto.
2. Se existir, reutilize-o. Se não, clique **Create / Criar**.
3. Escolha sua assinatura e grupo. Um nome possível é `fitforge-bus-andryus`, se disponível.
4. Escolha uma região da lista permitida pela sua assinatura e oferecida para Service Bus.
5. Selecione **Basic**, que suporta a fila usada pelo projeto. Ele pode gerar custos; confira o preço exibido antes de criar. Standard/Premium não são necessários para este fluxo simples.
6. Conclua **Review + create** e aguarde o recurso ficar pronto.
7. Abra o namespace. Registre seu nome, sem publicar chaves.

### 5.2 Fila

1. Dentro do namespace, vá a **Entities / Entidades → Queues / Filas → + Queue**.
2. Nome: **`fitforge-sessoes`**.
3. Mantenha os padrões compatíveis do plano, sem exigir sessões de mensagens. Se a opção **Enable sessions** estiver disponível, deixe desabilitada. Sessão de treino do domínio e Service Bus Sessions são conceitos distintos.
4. Não habilite duplicate detection como pré-requisito; o fluxo tolera reentrega pelo efeito idempotente no consumidor.
5. Crie a fila. Se já existir, confirme o nome e se ela está ativa.

### 5.3 Permissão de publicar — Progresso

1. Abra a fila `fitforge-sessoes`.
2. Procure **Shared access policies / Políticas de acesso compartilhado**.
3. Crie a política **`progresso-send`** com somente **Send / Enviar**.
4. Abra a política e copie **Primary Connection String / Cadeia de conexão primária** inteira.
5. Guarde para `SERVICE_BUS_CONNECTION_STRING` do app **fitforge-progresso**.

### 5.4 Permissão de consumir — Treinos

1. Na mesma fila, crie **`treinos-listen`** com somente **Listen / Escutar**.
2. Copie a **Primary Connection String** dessa política.
3. Guarde para `SERVICE_BUS_CONNECTION_STRING` do app **fitforge-treinos**.

Se o portal não mostrar políticas na fila, crie as políticas no nível do namespace, ainda com Send e Listen separados. O escopo fica mais amplo, mas a função de cada serviço permanece separada. Para políticas da fila, a connection string pode terminar em `EntityPath=fitforge-sessoes`; para políticas do namespace, o código usa `SERVICE_BUS_QUEUE` para selecionar a fila. Não coloque ambas as filas diferentes numa mesma configuração.

Não use a política Listen em Progresso nem Send em Treinos. Não use RootManageSharedAccessKey como primeira escolha. O aplicativo atual usa SAS; se **Local authentication** do namespace estiver desabilitada por política, esse caminho não funcionará e será necessária uma integração por Entra/identidade gerenciada.

### 5.5 Rede do broker

Use o endpoint público do namespace neste procedimento. Os clientes Java deste pacote usam AMQP sobre WebSockets, porta 443. Isso reduz a dependência de porta AMQP direta liberada. Não abra o banco para toda a internet para resolver problemas do broker; são firewalls distintos.

## 6. Prepare a chave e as configurações completas

A chave interna é um segredo criado por você, não é a senha de login nem uma chave do Storage. Gere uma única chave e use a mesma nos três aplicativos. Exemplo no PowerShell 5.1:

```powershell
$bytesFitForge = New-Object byte[] 32
$geradorFitForge = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$geradorFitForge.GetBytes($bytesFitForge)
$chaveFitForge = [Convert]::ToBase64String($bytesFitForge)
$geradorFitForge.Dispose()
$chaveFitForge
```

Copie o valor para seu gerenciador/local privado. Não mande print com o valor. As configurações abaixo ficam em **Azure → App Services → aplicativo → Settings / Configurações → Environment variables / Variáveis de ambiente → App settings / Configurações do aplicativo**. Não coloque essas credenciais em `application.yml`, arquivos do GitHub nem variáveis públicas de build.

### 6.1 fitforge-treinos

| Nome | Valor a preencher |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `azure` |
| `MONGODB_URI` | Manter sua URI real já funcionando, com `mongodb+srv://` e senha correta |
| `MONGODB_DATABASE` | `fitforge-treinos` |
| `FITFORGE_INTERNAL_KEY` | Chave interna gerada, idêntica nos três apps |
| `SERVICE_BUS_ENABLED` | `true` |
| `SERVICE_BUS_QUEUE` | `fitforge-sessoes` |
| `SERVICE_BUS_CONNECTION_STRING` | Connection string da política **treinos-listen** |
| `SEED_DATA` | `false` |

Mantenha as regras de saída de Treinos no firewall Mongo. O perfil não troca o cluster nem exige recriar coleções.

### 6.2 fitforge-progresso

| Nome | Valor a preencher |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `azure` |
| `DB_URL` | Sua URL JDBC do Azure SQL (4.5) |
| `DB_USER` | Usuário SQL criado (ex.: `fitforge_app`) |
| `DB_PASSWORD` | Senha real do usuário SQL |
| `DB_DDL_AUTO` | `update` |
| `FITFORGE_INTERNAL_KEY` | Mesma chave de Treinos e Gateway |
| `SERVICE_BUS_ENABLED` | `true` |
| `SERVICE_BUS_QUEUE` | `fitforge-sessoes` |
| `SERVICE_BUS_CONNECTION_STRING` | Connection string da política **progresso-send** |
| `SEED_DATA` | `false` |
| `APP_TIMEZONE` | `America/Sao_Paulo` |

### 6.3 fitforge-gateway

| Nome | Valor a preencher |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `azure` |
| `TREINOS_SERVICE_URL` | `https://fitforge-treinos-cchfd7b9dyf7cjgu.mexicocentral-01.azurewebsites.net` |
| `PROGRESSO_SERVICE_URL` | `https://fitforge-progresso-gvfgfua7b0dggrht.mexicocentral-01.azurewebsites.net` |
| `CORS_ORIGINS` | `https://c4pivara35.z21.web.core.windows.net` (sem barra final) |
| `FITFORGE_AUTH_REQUIRED` | `true` |
| `FITFORGE_USERNAME` | Manter seu usuário de demonstração atual |
| `FITFORGE_PASSWORD` | Manter sua senha de demonstração atual |
| `FITFORGE_INTERNAL_KEY` | Mesma chave dos dois serviços |
| `APP_TIMEZONE` | `America/Sao_Paulo` |

Gateway não recebe conexão de SQL, Mongo ou Service Bus. Frontend não recebe a chave interna. No GitHub do frontend, mantenha `FITFORGE_API_URL` com essa URL HTTPS do Gateway.

## 7. Copiar arquivos e fazer os commits

### 7.1 Onde cada pasta vai

| Pasta dentro do ZIP | Repositório de destino |
|---|---|
| `FitForge/` | `https://github.com/C4pivara35/FitForge` |
| `FitForge-API_Gateway/` | `https://github.com/C4pivara35/FitForge-API_Gateway` |
| `FitForge-Microservicos/` | `https://github.com/C4pivara35/FitForge-Microservicos` |

As pastas contêm apenas os arquivos alterados. Copie **o conteúdo** da pasta para a raiz do clone correspondente, preservando os caminhos. Não substitua seu projeto inteiro por essa pasta parcial e não crie uma pasta `FitForge-Microservicos` dentro de outra com o mesmo nome. `ARQUIVOS-ALTERADOS.csv` lista todos os caminhos e hashes.

### 7.2 Atualizar antes de copiar

No PowerShell, abra a raiz de cada clone, não uma subpasta Java. Exemplo do backend:

```powershell
cd D:\Codes\FitForge-Microservicos
git status --short
git switch main
git pull --ff-only
```

Se `git status` mostrar trabalho local, preserve-o antes de copiar. Este pacote parte dos commits `c4cd8c3` (front), `0b739f0` (Gateway) e `224680b` (backend). Se houve alteração posterior nos mesmos arquivos, compare em vez de sobrescrever sua nova versão.

Depois copie a pasta correspondente. Confira o diff, especialmente README, perfis e as duas classes do broker. Não deve aparecer senha real nem arquivo `.PublishSettings`.

### 7.3 Commit do backend

```powershell
cd D:\Codes\FitForge-Microservicos
git diff --check
git status --short
git add README.md GRUPO.md docs treinos-service/src/main/resources/application-azure.yml progresso-service/src/main/resources/application-azure.yml treinos-service/src/main/java/br/fitforge/treinos/workouts/activity/SessionEventConsumer.java progresso-service/src/main/java/br/fitforge/progresso/sessions/events/SessionEventPublisher.java
git diff --cached --stat
git commit -m "Documenta Arc42 e prepara Azure SQL e Service Bus"
git push origin main
```

Essas alterações em ambos os módulos disparam seus dois workflows. Não crie novos pipelines. Quem não possui Java/Maven local pode usar os workflows existentes para validar; a confirmação é o job build passar antes do deploy.

### 7.4 Commit do Gateway

```powershell
cd D:\Codes\FitForge-API_Gateway
git diff --check
git status --short
git add README.md GRUPO.md docs src/main/resources/application-azure.yml .github/workflows/deploy-azure-webapp.yml
git diff --cached --stat
git commit -m "Documenta Arc42 e adiciona perfil Azure do Gateway"
git push origin main
```

### 7.5 Commit do frontend

```powershell
cd D:\Codes\FitForge
git diff --check
git status --short
git add README.md GRUPO.md docs .github/workflows/deploy-azure-storage.yml
git diff --cached --stat
git commit -m "Completa Arc42 e documenta validacao da entrega"
git push origin main
```

O frontend não recebe alterações funcionais. Depois deste ajuste de workflow, mudanças exclusivamente no README/GRUPO/docs deixam de publicar novamente. Assim, inserir o link do vídeo posteriormente não exige novo deploy por esses workflows. Como este commit também altera o workflow, ele ainda dispara a execução atual.

Abra Actions em cada repositório e aguarde conclusão. Não clique em re-run enquanto o job ainda estiver executando. Um deploy verde comprova envio do JAR, não conexão correta com banco/broker.

## 8. Ativar as configurações na Azure

Depois de os novos JARs serem publicados:

1. Abra **fitforge-treinos → Environment variables → App settings**.
2. Preencha/edite todo o conjunto da tabela 6.1 e salve uma vez. A aplicação pode reiniciar.
3. Aguarde o início no **Log stream / Fluxo de logs**. Abra `/actuator/health` até responder `UP`, sem ficar recarregando continuamente.
4. Faça o mesmo em **fitforge-progresso** com toda a tabela 6.2.
5. Nos logs de Progresso, confirme perfil `azure`, pool JDBC inicializado e ausência de `jdbc:h2:mem:progressodb`. Se houver erro, corrija usuário/URL/firewall antes de insistir no CRUD.
6. Faça o mesmo no **Gateway** com 6.3. Enquanto as chaves não coincidirem nos três apps, o fluxo pode dar `403`; conclua o conjunto.
7. Se a opção de logs exigir ativação, use **App Service logs → Application logging (Filesystem)** e limite a retenção conforme o necessário para a demonstração.

O perfil `azure` deve aparecer nos logs de startup. Um campo “Running / Em execução” do portal não comprova que o JAR iniciou nem que o SQL está conectado.

## 9. Validar e produzir as evidências

### 9.1 Saúde e segurança

1. Consulte `/actuator/health` de cada app. Esperado `{"status":"UP"}`.
2. Abra `/swagger-ui.html` de Gateway e serviços. O documento `/v3/api-docs` deve carregar.
3. No frontend, faça login com a credencial do Gateway que você manteve em 6.3.
4. No Network, confira `/api/bff/dashboard` usando **o domínio do Gateway**, com `200`, ambos os serviços `ONLINE` e JSON agregado.
5. Uma chamada ao Gateway `/api/treinos` sem Authorization deve retornar `401`.
6. Uma chamada direta aos serviços `/api/treinos` e `/api/sessoes`, sem chave, deve retornar `403`. O Swagger público continua disponível; proteger dados não significa esconder o contrato.
7. O CORS deve retornar a origem do Storage uma única vez.

### 9.2 CRUD de Treinos com prova no MongoDB

1. Crie uma ficha pela interface com nome identificável, como `Entrega - Treino demonstracao`.
2. Adicione ao menos um dia e exercício válido. Salve e confirme sucesso no Network.
3. Na ferramenta que já conecta ao Mongo (mongosh/Compass), selecione o banco real `fitforge-treinos`.
4. Consulte:

```javascript
use('fitforge-treinos')
db.treinos.find({ name: 'Entrega - Treino demonstracao' })
```

5. Copie o `_id` **real** retornado. Edite a ficha na interface e consulte pelo mesmo ID para conferir o novo valor.
6. Mantenha essa ficha para o teste de sessão/broker. Faça a exclusão apenas depois de demonstrar o evento.

### 9.3 Sessão, SQL e Service Bus

1. No frontend, registre uma sessão vinculada à ficha de demonstração. Use data de hoje ou anterior.
2. Copie o ID real da sessão pela resposta `POST /api/sessoes` no Network, sem mostrar Authorization.
3. No **banco Azure SQL → Query editor**, execute o arquivo `docs/sql/VERIFICAR-AZURE-SQL.sql` do backend. Ele é somente leitura e mostra banco, engine, tabelas e registros recentes, sem hashes de senha.
4. Confirme uma linha em `dbo.sessoes` com o ID retornado. Essa consulta, feita dentro do recurso Azure SQL, é a prova de persistência exigida.
5. Em **Progresso → Log stream**, procure `Evento enviado id=... ficha=...`.
6. Em **Treinos → Log stream**, procure `Evento processado id=... ficha=...` com o mesmo evento e ficha.
7. No MongoDB, consulte:

```javascript
use('fitforge-treinos')
db.atividades_fichas.find({ _id: 'COLE_O_ID_REAL_DA_FICHA' })
```

8. Deve aparecer `lastSessionRecordedAt`. Também pode consultar, pelo Gateway autenticado, `/api/treinos/ID_REAL/atividade`.
9. No Service Bus → fila → métricas, confira mensagens recebidas/enviadas e dead-letter. As métricas podem atrasar. Se a mensagem é consumida rapidamente, a fila ficar vazia é esperado.
10. A outbox SQL pode estar vazia após envio bem-sucedido. Isso é esperado; use logs e projeção como evidência conjunta. Não remova manualmente mensagens ou registros para forçar sucesso.

`lastSessionRecordedAt` é a data do evento de **registro** da sessão, não a data escolhida para a prática. Uma alteração/exclusão de sessão não publica novo evento no contrato atual.

### 9.4 Completar os CRUDs

1. Edite duração/observações da sessão no frontend. Consulte o mesmo ID no SQL e confira os valores.
2. Exclua essa sessão e confirme ausência no SQL.
3. Exclua a ficha e confirme ausência em `treinos` no MongoDB.
4. Para provar preservação de histórico, use outra sessão válida: exclua sua ficha e mostre que a sessão SQL permanece. Não destrua registros de outras pessoas.
5. Atualize o dashboard e confira totais coerentes.
6. Registre as evidências e marque o checklist em cada repositório. Não marque itens que não conferiu.

### 9.5 Validação local e limites dos testes

Com Java 21/Maven, Docker para Mongo ou um Mongo de teste:

```powershell
# raiz de FitForge-Microservicos
mvn -B clean verify
# raiz de FitForge-API_Gateway
mvn -B clean verify
# raiz de FitForge
npm ci
npm run check
```

Para Mongo externo de **teste**, pode definir `FITFORGE_TEST_MONGODB_URI`. A suíte cria/apaga bancos temporários; não aponte para credencial que deva ser restrita à produção. Os testes Java usam configurações locais/teste, sem perfil azure. O CI não cria sua infraestrutura Azure.

## 10. Erros frequentes e diagnóstico

| Erro/evidência | Conferir |
|---|---|
| `Could not resolve placeholder DB_URL` | App setting está no **fitforge-progresso** e foi salva? |
| Logs mostram H2 | Perfil `azure` ativo e JAR novo realmente publicado? |
| SQL `Login failed for user` | SQL authentication habilitada, usuário contido no banco certo, senha correta e databaseName correto |
| SQL timeout/conexão recusada | Servidor, porta 1433, firewall com saída de Progresso e banco pausado/iniciando |
| `Invalid object name` / permissão DDL | App abriu banco correto e usuário pode criar tabelas com `update`? |
| Bus `Unauthorized` | Send em Progresso, Listen em Treinos, SAS habilitado e EntityPath/fila coincidentes |
| Outbox cresce | Publicador está ativo? Conexão Send correta? Logs de retry? |
| Mensagens ativas não diminuem | Treinos ativo, conexão Listen correta e fila sem sessões obrigatórias? |
| Dead-letter aumenta | Contrato/assunto JSON ou falha recorrente no consumidor; inspecionar sem apagar evidências |
| Evento enviado mas sem projeção | Mesmo ID e proprietário? Ficha ainda existe? Consumidor logou processamento? |
| Gateway `403` após ativar chave | Chaves iguais nos três apps e sem espaços extras? |
| Browser NetworkError | Network/Console, status real, CORS único e domínio do Gateway |
| Portal Running, API fora | Log stream e health da aplicação, não apenas estado do recurso |
| Quota exceeded | Quotas do App Service; novo commit/restart não zera cota |

## 11. O que entra no Git e o que fica privado

GitHub: código, perfis com placeholders, Arc42, checklist, SQL de inspeção e README com links. Azure: senhas, chaves, connection strings reais e seleção de recursos. Banco: os dados. Não enviar `.PublishSettings`, `.env` real, passwords, credenciais do broker ou exportações de usuários.

O pacote mantém três repositórios separados. Não altera links para repositórios de Valentin e não depende da Azure de outra conta.

## 12. Critério para considerar esta etapa concluída

- Arc42 de 12 seções publicada nos três repositórios, acessível pelos READMEs.
- Ambos os CRUDs demonstrados pelo frontend/Gateway.
- Sessões novas verificadas no Azure SQL, sem H2 no aplicativo publicado.
- Evento enviado e processado com efeito observável em `atividades_fichas`.
- Serviços protegidos por chave interna e Gateway autenticado.
- Workflows dos projetos passam e os testes de arquitetura permanecem ativos.

Isso deixa o vídeo como a etapa seguinte, depois de comprovar os itens acima. Não representa atribuição automática de nota máxima: a avaliação depende da execução e das evidências.

Referências oficiais consultadas: [Arc42](https://arc42.org/overview/), [Java/JDBC Azure SQL](https://learn.microsoft.com/en-us/azure/azure-sql/database/connect-query-java?view=azuresql), [oferta gratuita SQL](https://learn.microsoft.com/en-us/azure/azure-sql/database/free-tier?view=azuresql), [Service Bus no portal](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal), [permissões SAS](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas), [configuração Spring Boot](https://docs.spring.io/spring-boot/3.3/reference/features/external-config.html).
