package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dao.UsuarioDAO;
import org.example.model.CadastroRequest;
import org.example.model.LoginRequest;
import org.example.model.Usuario;
import org.example.security.SecurityUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class UsuarioController extends BaseController implements HttpHandler {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if ("/api/login".equals(path)) {
            handleLogin(exchange);
        } else if ("/api/register".equals(path)) {
            handleCadastro(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Endpoint não encontrado\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
            return;
        }

        try {
            String requestBody = readRequestBody(exchange.getRequestBody());
            LoginRequest req = gson.fromJson(requestBody, LoginRequest.class);
            Usuario usuarioDoBanco = usuarioDAO.findByUsername(req.getUsername());

            if (usuarioDoBanco == null) {
                sendResponse(exchange, 401, "{\"error\":\"Usuário não encontrado.\"}");
            } else if (SecurityUtil.checkPassword(req.getPassword(), usuarioDoBanco.getPassword())) {
                String token = SecurityUtil.generateToken(usuarioDoBanco);
                Map<String, String> response = Collections.singletonMap("token", token);
                sendResponse(exchange, 200, gson.toJson(response));
            } else {
                sendResponse(exchange, 401, "{\"error\":\"Senha incorreta.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Ocorreu um erro interno no servidor\"}");
        }
    }

    private void handleCadastro(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
            return;
        }

        try {
            String requestBody = readRequestBody(exchange.getRequestBody());
            CadastroRequest req = gson.fromJson(requestBody, CadastroRequest.class);

            if (req.getEmail() == null || req.getSenha() == null || req.getEmail().trim().isEmpty() || req.getSenha().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"E-mail e senha são obrigatórios\"}");
                return;
            }

            if (usuarioDAO.findByUsername(req.getEmail()) != null) {
                sendResponse(exchange, 409, "{\"error\":\"Este e-mail já está cadastrado\"}");
                return;
            }

            Usuario usuarioParaSalvar = new Usuario();
            usuarioParaSalvar.setUsername(req.getEmail());
            String senhaHash = SecurityUtil.hashPassword(req.getSenha());
            usuarioParaSalvar.setPassword(senhaHash);
            usuarioParaSalvar.setPerfil("USUARIO");

            usuarioDAO.save(usuarioParaSalvar);

            sendResponse(exchange, 201, "{\"message\":\"Usuário cadastrado com sucesso!\"}");

        } catch (SQLException e) {
            sendResponse(exchange, 409, "{\"error\":\"Erro de SQL, possível usuário duplicado.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Ocorreu um erro interno no servidor\"}");
        }
    }
}
