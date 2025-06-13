package org.example.dao;



import org.example.model.Pedido;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public Pedido addPedido(Pedido pedido) throws SQLException { // RF006
        String sql = "INSERT INTO PEDIDOS (cliente_id, data_pedido, valor_total, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, pedido.getClienteId());
            pstmt.setDate(2, Date.valueOf(pedido.getDataPedido()));
            pstmt.setBigDecimal(3, pedido.getValorTotal());
            pstmt.setString(4, pedido.getStatus());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pedido.setId(generatedKeys.getInt(1));
                }
            }
            return pedido;
        }
    }

    public List<Pedido> getAllPedidos() throws SQLException { // RF007
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT id, cliente_id, data_pedido, valor_total, status FROM PEDIDOS";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setId(rs.getInt("id"));
                pedido.setClienteId(rs.getInt("cliente_id"));
                pedido.setDataPedido(rs.getDate("data_pedido").toLocalDate());
                pedido.setValorTotal(rs.getBigDecimal("valor_total"));
                pedido.setStatus(rs.getString("status"));
                pedidos.add(pedido);
            }
        }
        return pedidos;
    }

    public Pedido getPedidoById(int id) throws SQLException { // RF008
        String sql = "SELECT id, cliente_id, data_pedido, valor_total, status FROM PEDIDOS WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Pedido pedido = new Pedido();
                    pedido.setId(rs.getInt("id"));
                    pedido.setClienteId(rs.getInt("cliente_id"));
                    pedido.setDataPedido(rs.getDate("data_pedido").toLocalDate());
                    pedido.setValorTotal(rs.getBigDecimal("valor_total"));
                    pedido.setStatus(rs.getString("status"));
                    return pedido;
                }
            }
        }
        return null;
    }

    public boolean updatePedido(Pedido pedido) throws SQLException { // RF009
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

    public boolean deletePedido(int id) throws SQLException { // RF010
        String sql = "DELETE FROM PEDIDOS WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // RF011
    public List<Pedido> getPedidosComNomesClientes() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.id, p.cliente_id, p.data_pedido, p.valor_total, p.status, c.nome AS nome_cliente " +
                "FROM PEDIDOS p JOIN CLIENTES c ON p.cliente_id = c.id";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setId(rs.getInt("id"));
                pedido.setClienteId(rs.getInt("cliente_id"));
                pedido.setDataPedido(rs.getDate("data_pedido").toLocalDate());
                pedido.setValorTotal(rs.getBigDecimal("valor_total"));
                pedido.setStatus(rs.getString("status"));
                pedido.setNomeCliente(rs.getString("nome_cliente")); // RF011
                pedidos.add(pedido);
            }
        }
        return pedidos;
    }
}