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

    @Desc("List all todos, optionally filtered")
    public List<Row> list() {
        return db.table("todo").orderBy("created_at DESC").find();
    }

    @Get("/page")
    @Desc("Paginate todos")
    public Page<Row> page(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 10;
        return db.paginate("SELECT * FROM todo ORDER BY created_at DESC",
                new Object[0], pageNum, pageSize);
    }

    @Desc("Create a new todo")
    public Row create(CreateReq req) {
        Validate.notBlank(req.title(), "title is required");
        Validate.maxLength(req.title(), 200, "title too long");
        int count = db.table("todo").count();
        if (count >= 100) {
            db.execute("DELETE FROM todo WHERE id = (SELECT MIN(id) FROM todo)");
        }
        Row row = Row.of("todo").set("title", req.title()).insert(db);
        return db.findById("todo", row.id());
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
        db.table("todo").where("id", id).update(todo);
        return get(id);
    }

    @Desc("Delete a todo")
    public void delete(int id) {
        int rows = db.deleteById("todo", id);
        if (rows == 0) throw new NotFoundException("Todo not found: " + id);
    }

    @Get("/search")
    @Desc("Search todos by keyword and done status")
    public List<Row> search(String keyword, Boolean done) {
        String sql = "SELECT * FROM todo #where(title, 'LIKE', keyword) #and(done, '=', done) #orderBy(created_at DESC)";
        Map<String, Object> params = new java.util.HashMap<>();
        if (keyword != null && !keyword.isBlank()) params.put("keyword", "%" + keyword + "%");
        if (done != null) params.put("done", done);
        return db.findDynamic(sql, params);
    }

    @Get("/stats")
    @Desc("Get todo statistics")
    public Map<String, Object> stats() {
        int total = db.table("todo").count();
        Row doneRow = db.findOne("SELECT COUNT(*) as cnt FROM todo WHERE done = true");
        int done = doneRow != null && doneRow.get("cnt") != null
                ? ((Number) doneRow.get("cnt")).intValue() : 0;
        return Map.of("total", total, "done", done, "pending", total - done);
    }

    public record CreateReq(String title) {}
    public record UpdateReq(String title, Boolean done) {}

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String msg) { super(msg); }
    }
}
