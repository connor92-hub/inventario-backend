package com.brian.inventario.security;

import com.brian.inventario.model.Usuario;

public class SesionUsuario {

    private static Usuario usuarioActual;

    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static boolean esAdmin() {
        return usuarioActual != null &&
                usuarioActual.getRole().name().equals("ADMIN");
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}