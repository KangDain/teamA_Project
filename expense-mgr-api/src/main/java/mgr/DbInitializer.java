package mgr;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbInitializer {

    public static void main(String[] args) {
        System.out.println("[DB Initializer] Connecting to remote MySQL server to create 'expense_manager' DB and seed data...");
        
        String driver = "com.mysql.cj.jdbc.Driver";
        String serverUrl = "jdbc:mysql://cdn.ditanet.duckdns.org:8306/?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "dita2414";
        String sqlFilePath = "init_sample_data.sql";

        try {
            Class.forName(driver);
            String fullSql = Files.readString(Paths.get(sqlFilePath), StandardCharsets.UTF_8);

            // Remove SQL comment lines
            StringBuilder cleanedSql = new StringBuilder();
            for (String line : fullSql.split("\n")) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("--") && !trimmed.isEmpty()) {
                    cleanedSql.append(line).append("\n");
                }
            }

            Connection con = DriverManager.getConnection(serverUrl, user, pass);
            Statement stmt = con.createStatement();

            String[] statements = cleanedSql.toString().split(";");
            int count = 0;
            for (String statementSql : statements) {
                String sql = statementSql.trim();
                if (!sql.isEmpty()) {
                    try {
                        stmt.execute(sql);
                        count++;
                    } catch (Exception ex) {
                        System.err.println("Statement warning: " + ex.getMessage());
                    }
                }
            }

            stmt.close();
            con.close();
            System.out.println("[DB Initializer SUCCESS] Successfully executed " + count + " SQL statements on remote MySQL database!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
