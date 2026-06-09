package demo;

import io.aura.Aura;
import io.aura.Validate;
import io.aura.db.Db;

import java.util.Map;

public class App {
    public static void main(String[] args) {
        Db db = Db.create("jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1", "sa", "");

        db.execute("""
            CREATE TABLE todo (
                id INT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                done BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""");

        Aura.create()
            .port(7291)
            .cors(true)
            .accessLog(true)
            .onStart(a -> a.register(db))
            .onStop(a -> db.close())
            .service(new TodoService(db))
            .routes(r -> {
                r.exception(TodoService.NotFoundException.class,
                    (e, ctx) -> ctx.status(404).json(Map.of("error", e.getMessage())));
                r.exception(Validate.ValidationException.class,
                    (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
            })
            .start(args);
    }
}
