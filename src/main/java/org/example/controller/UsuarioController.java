package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dao.UsuarioDAO;
import org.example.model.CadastroRequest;
import org.example.model.LoginRequest;
import org.example.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.Collections;

public class UsuarioController extends BaseController implements HttpHandler {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (path.equals("/api/auth/login") && "POST".equals(exchange.getRequestMethod())) {
            handleLogin(exchange);
        } else if (path.equals("/api/auth/cadastro") && "POST".equals(exchange.getRequestMethod())) {
            handleCadastro(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Endpoint de autenticação não encontrado\"}");
        }
    }

    private void handleCadastro(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange.getRequestBody());
            CadastroRequest req = gson.fromJson(requestBody, CadastroRequest.class);

            if (req.getNomeCompleto() == null || req.getEmail() == null || req.getSenha() == null ||
                    req.getNomeCompleto().trim().isEmpty() || req.getEmail().trim().isEmpty() || req.getSenha().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Nome, e-mail e senha são obrigatórios\"}");
                return;
            }

            if (usuarioDAO.findByEmail(req.getEmail()) != null) {
                sendResponse(exchange, 409, "{\"error\":\"Este e-mail já está cadastrado\"}");
                return;
            }

            String senhaHash = BCrypt.hashpw(req.getSenha(), BCrypt.gensalt());
            Usuario novoUsuario = new Usuario();
            novoUsuario.setNomeCompleto(req.getNomeCompleto());
            novoUsuario.setEmail(req.getEmail());
            novoUsuario.setSenhaHash(senhaHash);
            usuarioDAO.save(novoUsuario);

            sendResponse(exchange, 201, "{\"message\":\"Usuário cadastrado com sucesso!\"}");

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Ocorreu um erro interno no servidor\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange.getRequestBody());
            LoginRequest req = gson.fromJson(requestBody, LoginRequest.class);

            Usuario usuario = usuarioDAO.findByEmail(req.getUsername());

            if (usuario != null && BCrypt.checkpw(req.getPassword(), usuario.getSenhaHash())) {
                String response = gson.toJson(Collections.singletonMap("message", "Login bem-sucedido"));
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 401, "{\"error\":\"E-mail ou senha inválidos\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Ocorreu um erro interno no servidor\"}");
        }
    }
}