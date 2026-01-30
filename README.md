# todo-cli-java

Um gerenciador de tarefas em linha de comando (CLI) feito em Java para aprendizado e portfólio.

## Requisitos
- Java 21+
- Maven 3.9+

## Build e execução (multi-módulo)
```bash
mvn package
```

### CLI
```bash
java -jar todo-cli/target/todo-cli.jar
```

### API
```bash
mvn -pl todo-api spring-boot:run
```

### Swagger UI
```
http://localhost:8080/swagger-ui
```

### H2 Console (dev)
```
http://localhost:8080/h2-console
```

## Funcionalidades atuais
- Adicionar tarefa
- Listar tarefas (com resumo)
- Marcar como concluída
- Remover tarefa
- Editar tarefa
- Buscar por palavra-chave
- Filtrar pendentes e concluídas
- Persistência em JSON (`data/tasks.json`)
- Migracao automatica do CSV antigo (se existir)
- Backup automatico do JSON (`.bak`)
- API REST (Spring Boot) com validacao e endpoints de busca/limpeza

## Exemplos (modo interativo)
```
=== GERENCIADOR DE TAREFAS ===
1 - Adicionar tarefa
2 - Listar tarefas
3 - Marcar como concluida
4 - Remover tarefa
5 - Editar tarefa
6 - Buscar por palavra-chave
7 - Listar pendentes
8 - Listar concluidas
0 - Sair
```

## Exemplos (comandos)
```bash
java -jar target/todo-cli.jar add "Comprar leite"
java -jar target/todo-cli.jar list
java -jar target/todo-cli.jar pending
java -jar target/todo-cli.jar completed
java -jar target/todo-cli.jar search "leite"
java -jar target/todo-cli.jar edit 1 "Comprar leite e pao"
java -jar target/todo-cli.jar done 1
java -jar target/todo-cli.jar remove 1
```

## Estrutura
```
src/main/java/com/thurbandeira/todocli
  cli/        -> entrada/saida do usuário
  model/      -> entidades do domínio
  service/    -> regras de negócio
  storage/    -> persistência
```

## API (exemplos)
```
POST /api/auth/register
POST /api/auth/login

Todos os endpoints abaixo exigem Authorization: Bearer <TOKEN>
GET  /api/tasks
GET  /api/tasks?status=pending|completed|all
GET  /api/tasks/summary
GET  /api/tasks/search?keyword=...
POST /api/tasks
PUT  /api/tasks/{id}
POST /api/tasks/{id}/complete
DELETE /api/tasks/{id}
POST /api/tasks/clear-completed
```

## Autenticacao (JWT)
1) Registrar:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"user\",\"password\":\"secret123\"}"
```

2) Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"user\",\"password\":\"secret123\"}"
```

3) Usar o token:
```bash
curl http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <TOKEN>"
```

## Banco de dados
Por padrao, a API usa H2 (arquivo local em `./data/todo-db`).

Para PostgreSQL, use o profile:
```bash
mvn -pl todo-api spring-boot:run -Dspring-boot.run.profiles=postgres
```

E configure as variaveis de ambiente:
- `JDBC_DATABASE_URL`
- `JDBC_DATABASE_USERNAME`
- `JDBC_DATABASE_PASSWORD`
- `JWT_SECRET`

## Testes
```bash
mvn test
```

## Script automatico de teste (Windows/PowerShell)
Com a API rodando, execute:
```powershell
.\scripts\test-api.ps1
```

Opcional:
```powershell
.\scripts\test-api.ps1 -BaseUrl http://localhost:8080
```

## Front-end basico (sem build)
1) Suba a API:
```bash
mvn -pl todo-api spring-boot:run
```

2) Em outro terminal, suba um servidor estatico:
```bash
cd todo-web
python -m http.server 5173
```

3) Abra no navegador:
```
http://localhost:5173
```

4) Use o formulario para registrar/login e testar as rotas.

## Roadmap (curto)
- CLI mais rica (flags)
- Tests de integracao para a CLI
- Migração automática CSV -> JSON
