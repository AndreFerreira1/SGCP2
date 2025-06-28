package org.example.security;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class SecurityFilter extends Filter {
    private final List<String> perfisPermitidos;

    public SecurityFilter(String... perfis) {
        this.perfisPermitidos = Arrays.asList(perfis);
    }

    @Override
    public String description() { return "Security Filter"; }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(exchange, 401, "Token de autenticação ausente ou malformado.");
                return;
            }
            String token = authHeader.substring(7);
            Claims claims = SecurityUtil.validateToken(token);
            if (claims == null) {
                sendError(exchange, 401, "Token inválido ou expirado.");
                return;
            }
            String perfil = claims.get("perfil", String.class);
            if (!perfisPermitidos.contains(perfil)) {
                sendError(exchange, 403, "Acesso negado.");
                return;
            }
            exchange.setAttribute("userId", claims.get("userId", Number.class).intValue());
            exchange.setAttribute("perfil", perfil);
            chain.doFilter(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Erro interno no filtro de segurança.");
        }
    }

    private void sendError(HttpExchange e, int code, String msg) throws IOException {
        e.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = String.format("{\"error\":\"%s\"}", msg).getBytes("UTF-8");
        e.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = e.getResponseBody()) { os.write(bytes); }
    }
}
