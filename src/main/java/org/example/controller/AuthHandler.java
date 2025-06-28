package org.example.controller;

import org.example.dao.UsuarioDAO;
import org.example.model.Usuario;
import org.example.security.SecurityUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class AuthHandler extends BaseController implements HttpHandler {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
            return;
        }

        try {
            String requestBody = readRequestBody(exchange.getRequestBody());
            Usuario loginAttempt = gson.fromJson(requestBody, Usuario.class);

            Usuario usuarioDB = usuarioDAO.findByUsername(loginAttempt.getUsername());

            if (usuarioDB != null && SecurityUtil.checkPassword(loginAttempt.getPassword(), usuarioDB.getPassword())) {

                String token = SecurityUtil.generateToken(usuarioDB);
                Map<String, String> response = Collections.singletonMap("token", token);
                sendResponse(exchange, 200, gson.toJson(response));
            } else {

                sendResponse(exchange, 401, "{\"error\":\"Credenciais inválidas\"}");
            }

        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\":\"Erro no banco de dados\"}");
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"Requisição inválida\"}");
        }
    }
}