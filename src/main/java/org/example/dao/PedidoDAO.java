package org.example.dao;

import org.example.model.Pedido;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public BigDecimal getFaturamentoNoMes() throws SQLException {
        String sql = "SELECT SUM(valor_total) FROM PEDIDOS WHERE status = 'Concluído' AND MONTH(data_pedido) = MONTH(CURRENT_DATE()) AND YEAR(data_pedido) = YEAR(CURRENT_DATE())";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                return total == null ? BigDecimal.ZERO : total;
            }
        }
        return BigDecimal.ZERO;
    }

    public long countPedidosPorStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PEDIDOS WHERE status = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    public Pedido addPedido(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO PEDIDOS (cliente_id, data_pedido, valor_total, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, pedido.getClienteId());
            pstmt.setDate(2, Date.valueOf(pedido.getDataPedido()));
            pstmt.setBigDecimal(3, pedido.getValorTotal());
            pstmt.setString(4, pedido.getStatus());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    pedido.setId(keys.getInt(1));
                }
            }
            return pedido;
        }
    }

    public List<Pedido> getAllPedidos() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT id, cliente_id, data_pedido, valor_total, status FROM PEDIDOS";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Pedido p = new Pedido();
                p.setId(rs.getInt("id"));
                p.setClienteId(rs.getInt("cliente_id"));
                p.setDataPedido(rs.getDate("data_pedido").toLocalDate());
                p.setValorTotal(rs.getBigDecimal("valor_total"));
                p.setStatus(rs.getString("status"));
                pedidos.add(p);
            }
        }
        return pedidos;
    }

    public Pedido getPedidoById(int id) throws SQLException {
        String sql = "SELECT id, cliente_id, data_pedido, valor_total, status FROM PEDIDOS WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Pedido p = new Pedido();
                    p.setId(rs.getInt("id"));
                    p.setClienteId(rs.getInt("cliente_id"));
                    p.setDataPedido(rs.getDate("data_pedido").toLocalDate());
                    p.setValorTotal(rs.getBigDecimal("valor_total"));
                    p.setStatus(rs.getString("status"));
                    return p;
                }
            }
        }
        return null;
    }

    public boolean updatePedido(Pedido pedido) throws SQLException {
        String sql = "UPDATE PEDIDOS SET cliente_id = ?, data_pedido = ?, valor_total = ?, status = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pedido.getClienteId());
            pstmt.setDate(2, Date.valueOf(pedido.getDataPedido()));
            pstmt.setBigDecimal(3, pedido.getValorTotal());
            pstmt.setString(4, pedido.getStatus());
            pstmt.setInt(5, pedido.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deletePedido(int id) throws SQLException {
        String sql = "DELETE FROM PEDIDOS WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Pedido> getPedidosComNomesClientes() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.id, p.cliente_id, p.data_pedido, p.valor_total, p.status, c.nome AS nome_cliente FROM PEDIDOS p JOIN CLIENTES c ON p.cliente_id = c.id";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Pedido p = new Pedido();
                p.setId(rs.getInt("id"));
                p.setClienteId(rs.getInt("cliente_id"));
                p.setDataPedido(rs.getDate("data_pedido").toLocalDate());
                p.setValorTotal(rs.getBigDecimal("valor_total"));
                p.setStatus(rs.getString("status"));
                p.setNomeCliente(rs.getString("nome_cliente"));
                pedidos.add(p);
            }
        }
        return pedidos;
    }
}
