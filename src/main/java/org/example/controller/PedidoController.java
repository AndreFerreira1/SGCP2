package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dao.PedidoDAO;
import org.example.model.Pedido;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PedidoController extends BaseController implements HttpHandler {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath().replace("/api/pedidos", "");
            String method = exchange.getRequestMethod();

            if (path.equals("/com-clientes") && "GET".equals(method)) {
                List<Pedido> pedidos = pedidoDAO.getPedidosComNomesClientes();
                sendResponse(exchange, 200, gson.toJson(pedidos));
            } else if (path.equals("/") || path.isEmpty()) {
                if ("GET".equals(method)) {
                    List<Pedido> pedidos = pedidoDAO.getAllPedidos();
                    sendResponse(exchange, 200, gson.toJson(pedidos));
                } else if ("POST".equals(method)) {
                    String requestBody = readRequestBody(exchange.getRequestBody());
                    Pedido pedido = gson.fromJson(requestBody, Pedido.class);
                    Pedido novoPedido = pedidoDAO.addPedido(pedido);
                    sendResponse(exchange, 201, gson.toJson(novoPedido));
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Método não permitido para /api/pedidos/\"}");
                }
            } else if (path.matches("^/\\d+$")) {
                int pedidoId = Integer.parseInt(path.substring(1));

                if ("GET".equals(method)) {
                    Pedido pedido = pedidoDAO.getPedidoById(pedidoId);
                    if (pedido != null) {
                        sendResponse(exchange, 200, gson.toJson(pedido));
                    } else {
                        sendResponse(exchange, 404, "{\"error\":\"Pedido não encontrado\"}");
                    }
                } else if ("PUT".equals(method)) {
                    String requestBody = readRequestBody(exchange.getRequestBody());
                    Pedido pedido = gson.fromJson(requestBody, Pedido.class);
                    pedido.setId(pedidoId);
                    boolean success = pedidoDAO.updatePedido(pedido);
                    if (success) {
                        sendResponse(exchange, 200, gson.toJson(pedido));
                    } else {
                        sendResponse(exchange, 404, "{\"error\":\"Pedido não encontrado para atualização\"}");
                    }
                } else if ("DELETE".equals(method)) {
                    boolean success = pedidoDAO.deletePedido(pedidoId);
                    if (success) {
                        sendResponse(exchange, 200, "{\"message\":\"Pedido deletado com sucesso\"}");
                    } else {
                        sendResponse(exchange, 404, "{\"error\":\"Pedido não encontrado para deleção\"}");
                    }
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Método não permitido para /api/pedidos/{id}\"}");
                }
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Endpoint de pedido não encontrado\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"ID do pedido inválido na URL.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Erro interno do servidor ao acessar o banco de dados.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Ocorreu um erro inesperado.\"}");
        }
    }
}