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
            String path = exchange.getRequestURI().getPath().replace("/api/clientes", "");
            String method = exchange.getRequestMethod();

            if (path.equals("/") || path.isEmpty()) {
                if ("GET".equals(method)) {
                    List<Cliente> clientes = clienteDAO.getAllClientes();
                    sendResponse(exchange, 200, gson.toJson(clientes));
                } else if ("POST".equals(method)) {
                    String requestBody = readRequestBody(exchange.getRequestBody());
                    Cliente cliente = gson.fromJson(requestBody, Cliente.class);
                    Cliente novoCliente = clienteDAO.addCliente(cliente);
                    sendResponse(exchange, 201, gson.toJson(novoCliente));
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Método não permitido para /api/clientes/\"}");
                }
            } else if (path.matches("^/\\d+$")) {
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
                    if (success) {
                        sendResponse(exchange, 200, gson.toJson(cliente));
                    } else {
                        sendResponse(exchange, 404, "{\"error\":\"Cliente não encontrado para atualização\"}");
                    }
                } else if ("DELETE".equals(method)) {
                    boolean success = clienteDAO.deleteCliente(clienteId);
                    if (success) {
                        sendResponse(exchange, 200, "{\"message\":\"Cliente deletado com sucesso\"}");
                    } else {
                        sendResponse(exchange, 404, "{\"error\":\"Cliente não encontrado para deleção\"}");
                    }
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Método não permitido para /api/clientes/{id}\"}");
                }
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Endpoint de cliente não encontrado\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"ID do cliente inválido na URL.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Erro interno do servidor ao acessar o banco de dados.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Ocorreu um erro inesperado.\"}");
        }
    }
}