package org.example.dao;


import org.example.model.Usuario;
import org.example.dao.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario findByEmail(String email) {

        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id"));
                usuario.setNomeCompleto(rs.getString("nome_completo"));
                usuario.setEmail(rs.getString("email"));
                usuario.setSenhaHash(rs.getString("senha_hash"));
                return usuario;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void save(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome_completo, email, senha_hash) VALUES (?, ?, ?)";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNomeCompleto());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getSenhaHash());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(); // Ou um tratamento de exceção mais robusto
        }
    }
}