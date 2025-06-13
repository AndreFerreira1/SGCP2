package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.controller.ClienteController;
import org.example.controller.PedidoController;
import org.example.controller.UsuarioController;
import org.example.dao.DbUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class MainApp {

    public static void main(String[] args) throws IOException {

        try {
            Class.forName(DbUtil.class.getName());
        } catch (ClassNotFoundException e) {
            System.err.println("Falha crítica ao carregar a classe do banco de dados: " + e.getMessage());
            return;
        }

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/api/auth/", new UsuarioController());
        server.createContext("/api/clientes/", new ClienteController());
        server.createContext("/api/pedidos/", new PedidoController());


        server.createContext("/", MainApp::handleStaticFiles);

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("======================================================");
        System.out.println(" Servidor SGCP iniciado na porta " + port);
        System.out.println(" Frontend: http://localhost:" + port + "/");
        System.out.println(" API de Autenticação: http://localhost:" + port + "/api/auth/");
        System.out.println(" API de Clientes: http://localhost:" + port + "/api/clientes/");
        System.out.println(" API de Pedidos: http://localhost:" + port + "/api/pedidos/");
        System.out.println("======================================================");
    }

    private static void handleStaticFiles(HttpExchange exchange) throws IOException {
        String requestedFile = exchange.getRequestURI().getPath();
        if (requestedFile.equals("/")) {
            requestedFile = "/index.html";
        }

        Path filePath = Paths.get("src/main/resources/static" + requestedFile).toAbsolutePath();

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                if (requestedFile.endsWith(".js")) contentType = "application/javascript; charset=utf-8";
                else if (requestedFile.endsWith(".css")) contentType = "text/css; charset=utf-8";
                else contentType = "application/octet-stream; charset=utf-8";
            }
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, Files.size(filePath));
            try (OutputStream os = exchange.getResponseBody()) {
                Files.copy(filePath, os);
            }
        } else {
            String response = "404 Not Found";
            System.err.println("Arquivo não encontrado: " + filePath);
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}