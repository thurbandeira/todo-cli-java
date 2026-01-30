# todo-cli-java

Um gerenciador de tarefas em linha de comando (CLI) feito em Java para aprendizado e portfólio.

## Requisitos
- Java 21+
- Maven 3.9+

## Build e execução
```bash
mvn package
java -jar target/todo-cli.jar
```

## API REST (backend)
Para iniciar a API REST (Spring Boot):
```bash
mvn spring-boot:run
```

Endpoints principais:
- `GET /api/tasks` (todas)
- `GET /api/tasks?status=pending|completed`
- `GET /api/tasks/search?keyword=...`
- `GET /api/tasks/summary`
- `POST /api/tasks` (body: `{"title":"..."}`)
- `PATCH /api/tasks/{id}` (body: `{"title":"...","completed":true}`)
- `DELETE /api/tasks/{id}`
- `POST /api/tasks/clear-completed`

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

## Testes
```bash
mvn test
```

## Roadmap (curto)
- CLI mais rica (flags)
- Tests de integracao para a CLI
- Migração automática CSV -> JSON
