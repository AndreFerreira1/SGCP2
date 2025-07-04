package org.example.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DbUtil {
    private static final String JDBC_URL = "jdbc:h2:mem:sgcp_db;DB_CLOSE_DELAY=-1";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");
            initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Falha ao inicializar o banco de dados", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            InputStream is = DbUtil.class.getResourceAsStream("/db/schema.sql");
            if (is == null) {
                System.err.println("Arquivo schema.sql não encontrado.");
                return;
            }
            String schemaSql = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            stmt.execute(schemaSql);
            System.out.println("Banco de dados inicializado com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }
}