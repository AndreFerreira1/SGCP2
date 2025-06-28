package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dao.ClienteDAO;
import org.example.model.Cliente;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ClienteController extends BaseController implements HttpHandler {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath().substring(exchange.getHttpContext().getPath().length());
            String method = exchange.getRequestMethod();

            if (path.isEmpty() || path.equals("/")) {
                if ("GET".equals(method)) {
                    List<Cliente> clientes = clienteDAO.getAllClientes();
                    sendResponse(exchange, 200, gson.toJson(clientes));
                } else if ("POST".equals(method)) {
                    String requestBody = readRequestBody(exchange.getRequestBody());
                    Cliente cliente = gson.fromJson(requestBody, Cliente.class);
                    Cliente novoCliente = clienteDAO.addCliente(cliente);
                    sendResponse(exchange, 201, gson.toJson(novoCliente));
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
                }
            } else {
                int clienteId = Integer.parseInt(path.substring(1));

                if ("GET".equals(method)) {
                    Cliente cliente = clienteDAO.getClienteById(clienteId);
                    if (cliente != null) {
                        sendResponse(exchange, 200, gson.toJson(cliente));
                    } else {
                        sendResponse(exchange, 404, "{\"error\":\"Cliente não encontrado\"}");
                    }
                } else if ("PUT".equals(method)) {
                    String requestBody = readRequestBody(exchange.getRequestBody());
                    Cliente cliente = gson.fromJson(requestBody, Cliente.class);
                    cliente.setId(clienteId);
                    boolean success = clienteDAO.updateCliente(cliente);
                    sendResponse(exchange, success ? 200 : 404, success ? gson.toJson(cliente) : "{\"error\":\"Cliente não encontrado para atualização\"}");
                } else if ("DELETE".equals(method)) {
                    String perfil = (String) exchange.getAttribute("perfil");
                    if (!"ADMIN".equals(perfil)) {
                        sendResponse(exchange, 403, "{\"error\":\"Acesso negado. Permissão de administrador necessária.\"}");
                        return;
                    }
                    boolean success = clienteDAO.deleteCliente(clienteId);
                    sendResponse(exchange, success ? 200 : 404, success ? "{\"message\":\"Cliente deletado com sucesso\"}" : "{\"error\":\"Cliente não encontrado\"}");
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
                }
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"ID de cliente inválido\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Erro de banco de dados\"}");
        } catch (Throwable t) {
            t.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Ocorreu um erro fatal e inesperado no servidor.\"}");
        }
    }
}