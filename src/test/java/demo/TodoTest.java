package demo;

import io.aura.Aura;
import io.aura.Validate;
import io.aura.db.Db;
import io.aura.web.TestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoTest {

    static TestClient test;

    @BeforeAll
    static void setup() {
        Db db = Db.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
        db.execute("""
            CREATE TABLE todo (
                id INT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                done BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""");

        Aura app = Aura.create()
            .onStart(a -> a.register(db))
            .onStop(a -> db.close())
            .service(new TodoService(db))
            .routes(r -> {
                r.exception(TodoService.NotFoundException.class,
                    (e, ctx) -> ctx.status(404).json(Map.of("error", e.getMessage())));
                r.exception(Validate.ValidationException.class,
                    (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
            });

        test = TestClient.of(app);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void createTodo() {
        test.post("/todo").body(new TodoService.CreateReq("Buy milk")).expect(200)
            .bodyContains("Buy milk");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void listTodos() {
        test.get("/todo").expect(200).bodyContains("Buy milk");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void getTodo() {
        test.get("/todo/1").expect(200).bodyContains("Buy milk");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void updateTodo() {
        test.put("/todo/1").body(new TodoService.UpdateReq(null, true)).expect(200)
            .bodyContains("true");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    void searchByKeyword() {
        test.get("/todo/search?keyword=milk").expect(200).bodyContains("Buy milk");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    void searchByDone() {
        test.get("/todo/search?done=true").expect(200).bodyContains("Buy milk");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    void pagination() {
        test.get("/todo/page?pageNum=1&pageSize=10").expect(200).bodyContains("total");
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    void stats() {
        test.get("/todo/stats").expect(200).bodyContains("total").bodyContains("done");
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    void notFound() {
        test.get("/todo/999").expect(404).bodyContains("error");
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    void validationError() {
        test.post("/todo").body(new TodoService.CreateReq("")).expect(400).bodyContains("error");
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    void deleteTodo() {
        test.delete("/todo/1").expect(200);
        test.get("/todo/1").expect(404);
    }
}
