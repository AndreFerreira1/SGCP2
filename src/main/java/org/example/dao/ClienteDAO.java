package org.example.dao;

import org.example.model.Cliente;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    
    public Cliente addCliente(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO CLIENTES (nome, email, telefone, data_cadastro) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, cliente.getNome());
            pstmt.setString(2, cliente.getEmail());
            pstmt.setString(3, cliente.getTelefone());
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) cliente.setId(keys.getInt(1));
            }
            return cliente;
        }
    }

    public long countNovosClientesNoMes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM CLIENTES WHERE MONTH(data_cadastro) = MONTH(CURRENT_DATE()) AND YEAR(data_cadastro) = YEAR(CURRENT_DATE())";
        try (Connection conn = DbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    public List<Cliente> getAllClientes() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, nome, email, telefone FROM CLIENTES";
        try (Connection conn = DbUtil.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) clientes.add(new Cliente(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), rs.getString("telefone")));
        }
        return clientes;
    }

    public Cliente getClienteById(int id) throws SQLException {
        String sql = "SELECT id, nome, email, telefone FROM CLIENTES WHERE id = ?";
        try (Connection conn = DbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return new Cliente(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), rs.getString("telefone"));
            }
        }
        return null;
    }

    public boolean updateCliente(Cliente cliente) throws SQLException {
        String sql = "UPDATE CLIENTES SET nome = ?, email = ?, telefone = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cliente.getNome());
            pstmt.setString(2, cliente.getEmail());
            pstmt.setString(3, cliente.getTelefone());
            pstmt.setInt(4, cliente.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCliente(int id) throws SQLException {
        String sql = "DELETE FROM CLIENTES WHERE id = ?";
        try (Connection conn = DbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}
