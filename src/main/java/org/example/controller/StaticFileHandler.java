package org.example.controller;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handler para servir arquivos estáticos (HTML, CSS, JS) de um diretório.
 */
public class StaticFileHandler implements HttpHandler {


    private static final String STATIC_DIRECTORY = "src/main/resources/static";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Lida com requisições CORS preflight (comuns no desenvolvimento frontend)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        String requestedFile = exchange.getRequestURI().getPath();


        if (requestedFile.equals("/")) {
            requestedFile = "/index.html";
        }


        Path filePath = Paths.get(STATIC_DIRECTORY, requestedFile).toAbsolutePath();


        if (!filePath.startsWith(Paths.get(STATIC_DIRECTORY).toAbsolutePath())) {
            sendError(exchange, 403, "Acesso Proibido");
            return;
        }

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            try {

                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {

                    if (requestedFile.endsWith(".js")) contentType = "application/javascript";
                    else if (requestedFile.endsWith(".css")) contentType = "text/css";
                    else contentType = "application/octet-stream";
                }


                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, Files.size(filePath));


                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(filePath, os);
                }
            } catch (IOException e) {
                sendError(exchange, 500, "Erro ao ler o arquivo");
            }
        } else {

            sendError(exchange, 404, "Arquivo não encontrado");
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void handleOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.sendResponseHeaders(204, -1);
    }
}