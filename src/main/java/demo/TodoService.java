package demo;

import io.aura.Validate;
import io.aura.annotation.Desc;
import io.aura.annotation.Get;
import io.aura.annotation.Path;
import io.aura.annotation.Put;
import io.aura.db.Db;
import io.aura.db.Page;
import io.aura.db.Row;

import java.util.List;
import java.util.Map;

@Path("/todo")
@Desc("Todo management API")
public class TodoService {

    private final Db db;

    public TodoService(Db db) { this.db = db; }

    @Desc("Get todo by ID")
    public Row get(int id) {
        Row todo = db.findById("todo", id);
        if (todo == null) throw new NotFoundException("Todo not found: " + id);
        return todo;
    }

    @Desc("List all todos")
    public List<Row> list() {
        return db.table("todo").orderBy("created_at DESC").find();
    }

    @Desc("Create a new todo")
    public Row create(CreateReq req) {
        Validate.notBlank(req.title(), "title is required");
        Validate.maxLength(req.title(), 200, "title too long");
        Row.of("todo").set("title", req.title()).insert(db);
        return db.findOne("SELECT * FROM todo ORDER BY id DESC LIMIT 1");
    }

    @Put("/{id}")
    @Desc("Update a todo")
    public Row update(int id, UpdateReq req) {
        Row todo = get(id);
        if (req.title() != null) {
            Validate.notBlank(req.title(), "title cannot be blank");
            todo.set("title", req.title());
        }
        if (req.done() != null) {
            todo.set("done", req.done());
        }
        db.execute("UPDATE todo SET title = ?, done = ? WHERE id = ?",
                todo.getStr("title"), todo.get("done"), id);
        return get(id);
    }

    @Desc("Delete a todo")
    public void delete(int id) {
        int rows = db.deleteById("todo", id);
        if (rows == 0) throw new NotFoundException("Todo not found: " + id);
    }

    @Get("/search")
    @Desc("Search todos by keyword")
    public List<Row> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return list();
        return db.find("SELECT * FROM todo WHERE title LIKE ? ORDER BY created_at DESC",
                "%" + keyword + "%");
    }

    @Get("/stats")
    @Desc("Get todo statistics")
    public Map<String, Object> stats() {
        int total = db.table("todo").count();
        Row doneRow = db.findOne("SELECT COUNT(*) as cnt FROM todo WHERE done = true");
        int done = doneRow != null ? doneRow.getInt("cnt") : 0;
        return Map.of("total", total, "done", done, "pending", total - done);
    }

    public record CreateReq(String title) {}
    public record UpdateReq(String title, Boolean done) {}

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String msg) { super(msg); }
    }
}
