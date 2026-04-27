package com.brian.inventario.service;

import com.brian.inventario.model.Usuario;
import com.brian.inventario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Guardar usuario
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Listar todos los usuarios
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    // Buscar por username (para login)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElse(null);
    }

    // ==================== NUEVO MÉTODO ====================
    // Eliminar usuario por username (útil para recrear usuarios en desarrollo)
    public void eliminarPorUsername(String username) {
        usuarioRepository.findByUsername(username)
                .ifPresent(usuarioRepository::delete);
    }

    // Eliminar usuario por ID
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    // Verificar si existe un usuario por username
    public boolean existePorUsername(String username) {
        return usuarioRepository.findByUsername(username).isPresent();
    }
}