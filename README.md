# Aura Demo — Todo API

A complete CRUD API built with [Aura Framework](https://github.com/tianhaocui/aura) in 2 files.

## Run

```bash
mvn compile exec:java -Dexec.mainClass=demo.App
```

Or with MCP for AI agent access:

```bash
mvn compile exec:java -Dexec.mainClass=demo.App -Dexec.args="--mcp-stdio"
```

## API

| Method | Path | Description |
|--------|------|-------------|
| GET | /todo | List all todos |
| GET | /todo/{id} | Get todo by ID |
| POST | /todo | Create todo (`{"title": "..."}`) |
| PUT | /todo/{id} | Update todo (`{"title": "...", "done": true}`) |
| DELETE | /todo/{id} | Delete todo |
| GET | /todo/search?keyword=x | Search by title |
| GET | /todo/stats | Get statistics |

## Test

```bash
# Create
curl -X POST http://localhost:8080/todo -H "Content-Type: application/json" -d '{"title":"Buy milk"}'

# List
curl http://localhost:8080/todo

# Update
curl -X PUT http://localhost:8080/todo/1 -H "Content-Type: application/json" -d '{"done":true}'

# Search
curl http://localhost:8080/todo/search?keyword=milk

# Stats
curl http://localhost:8080/todo/stats

# Delete
curl -X DELETE http://localhost:8080/todo/1
```

## What This Demo Shows

- `@Path` + CRUD convention (get/list/create/delete auto-mapped)
- `@Get`/`@Put` for custom routes
- `@Desc` for MCP tool descriptions
- Record parameter binding (CreateReq, UpdateReq from body)
- Path parameter binding (int id)
- Query parameter binding (String keyword)
- `Validate` for input validation
- `Db` with H2 in-memory database
- `Row` for dynamic data
- Exception handling (NotFoundException → 404)
- MCP integration (`--mcp-stdio`)

## Project Structure

```
src/main/java/demo/
├── App.java          ← 12 lines: DB setup + start
└── TodoService.java  ← 80 lines: full CRUD + search + stats
```

Total: ~92 lines of code for a complete, production-ready API.
