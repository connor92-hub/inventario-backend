package com.brian.inventario.controller;

import com.brian.inventario.dto.LoginRequest;
import com.brian.inventario.model.Usuario;
import com.brian.inventario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Usuario usuario = usuarioService.buscarPorUsername(request.getUsername());

        if (usuario != null &&
                passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {

            return ResponseEntity.ok(usuario);
        }

        return ResponseEntity
                .badRequest()
                .body("Usuario o contraseña incorrectos");
    }
}