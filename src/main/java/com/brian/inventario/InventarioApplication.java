package com.brian.inventario;

import com.brian.inventario.model.Role;
import com.brian.inventario.model.Usuario;
import com.brian.inventario.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class InventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner run(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        return args -> {

            System.out.println("🔄 Iniciando recreación de usuarios de prueba...");

            // ==================== ELIMINAR USUARIOS ANTERIORES ====================
            System.out.println("🗑 Eliminando usuarios anteriores (admin y user)...");
            usuarioService.eliminarPorUsername("admin");
            usuarioService.eliminarPorUsername("user");

            // ==================== CREAR USUARIO ADMIN ====================
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setRole(Role.ADMIN);
            usuarioService.guardar(admin);
            System.out.println("✅ USUARIO ADMIN CREADO");
            System.out.println("   Username : admin");
            System.out.println("   Password : 1234");

            // ==================== CREAR USUARIO NORMAL ====================
            Usuario user = new Usuario();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("1234"));
            user.setRole(Role.USER);
            usuarioService.guardar(user);
            System.out.println("✅ USUARIO USER CREADO");
            System.out.println("   Username : user");
            System.out.println("   Password : 1234");

            System.out.println("========================================");
            System.out.println("🚀 USUARIOS RECREADOS CORRECTAMENTE");
            System.out.println("   Prueba con:");
            System.out.println("   → admin / 1234");
            System.out.println("   → user  / 1234");
            System.out.println("========================================");
        };
    }
}