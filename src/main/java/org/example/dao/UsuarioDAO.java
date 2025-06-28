package org.example.dao;

import org.example.model.Usuario;
import java.sql.*;

public class UsuarioDAO {
    public void save(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO USUARIOS (username, password_hash, perfil) VALUES (?, ?, ?)";
        try (Connection conn = DbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getUsername());
            pstmt.setString(2, usuario.getPassword());
            pstmt.setString(3, usuario.getPerfil());
            pstmt.executeUpdate();
        }
    }

    public Usuario findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, perfil FROM USUARIOS WHERE username = ?";
        try (Connection conn = DbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Usuario user = new Usuario();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setPerfil(rs.getString("perfil"));
                    return user;
                }
            }
        }
        return null;
    }
}