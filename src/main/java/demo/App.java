package demo;

import io.aura.Aura;
import io.aura.db.Db;

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
            .port(8080)
            .cors(true)
            .onStart(a -> a.register(db))
            .onStop(a -> db.close())
            .service(new TodoService(db))
            .start(args);
    }
}
