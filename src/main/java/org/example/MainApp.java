package org.example;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import org.example.controller.ClienteController;
import org.example.controller.DashboardController;
import org.example.controller.PedidoController;
import org.example.controller.StaticFileHandler;
import org.example.controller.UsuarioController;
import org.example.security.SecurityFilter;
import java.io.IOException;
import java.net.InetSocketAddress;

public class MainApp {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/api/login", new UsuarioController());
        server.createContext("/api/register", new UsuarioController());


        HttpContext clientesContext = server.createContext("/api/clientes", new ClienteController());
        clientesContext.getFilters().add(new SecurityFilter("ADMIN", "USUARIO"));

        HttpContext pedidosContext = server.createContext("/api/pedidos", new PedidoController());
        pedidosContext.getFilters().add(new SecurityFilter("ADMIN", "USUARIO"));

        HttpContext dashboardContext = server.createContext("/api/dashboard", new DashboardController());
        dashboardContext.getFilters().add(new SecurityFilter("ADMIN", "USUARIO"));

        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor SGCP (versão avançada) iniciado na porta " + port);
    }
}
