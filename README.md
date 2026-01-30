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

## Funcionalidades atuais
- Adicionar tarefa
- Listar tarefas (com resumo)
- Marcar como concluída
- Remover tarefa
- Editar tarefa
- Buscar por palavra-chave
- Filtrar pendentes e concluídas
- Persistência em JSON (`data/tasks.json`)

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
- Migracao automatica CSV -> JSON
- Backup automatico do JSON
- Migração automática CSV -> JSON
