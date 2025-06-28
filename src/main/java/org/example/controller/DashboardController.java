package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dao.ClienteDAO;
import org.example.dao.PedidoDAO;
import org.example.model.DashboardData;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;

public class DashboardController extends BaseController implements HttpHandler {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
            return;
        }

        try {
            long pedidosPendentes = pedidoDAO.countPedidosPorStatus("Pendente");
            long concluidos = pedidoDAO.countPedidosPorStatus("Concluído");
            long cancelados = pedidoDAO.countPedidosPorStatus("Cancelado");
            long novosClientes = clienteDAO.countNovosClientesNoMes();
            BigDecimal faturamentoMes = pedidoDAO.getFaturamentoNoMes();

            DashboardData.KpiData kpis = new DashboardData.KpiData(faturamentoMes, novosClientes, pedidosPendentes);

            DashboardData.ChartData graficoStatus = new DashboardData.ChartData(
                    Arrays.asList("Pendente", "Concluído", "Cancelado"),
                    Arrays.asList(pedidosPendentes, concluidos, cancelados)
            );

            DashboardData.ChartData graficoFaturamento = new DashboardData.ChartData(
                    Arrays.asList("Jan", "Fev", "Mar", "Abr", "Mai", "Jun"),
                    Arrays.asList(new BigDecimal("12000"), new BigDecimal("19000"), new BigDecimal("13000"), new BigDecimal("17000"), new BigDecimal("15000"), new BigDecimal("18500"))
            );

            DashboardData responseData = new DashboardData();
            responseData.setKpis(kpis);
            responseData.setGraficoStatus(graficoStatus);
            responseData.setGraficoFaturamento(graficoFaturamento);

            sendResponse(exchange, 200, gson.toJson(responseData));

        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Erro de banco de dados ao buscar dados do dashboard\"}");
        } catch (Throwable t) {
            t.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Ocorreu um erro fatal e inesperado no servidor.\"}");
        }
    }
}