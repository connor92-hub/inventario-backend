package com.brian.inventario.frontend;

import com.brian.inventario.model.Role;
import com.brian.inventario.model.Usuario;
import com.brian.inventario.security.SesionUsuario;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class LoginFrame extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;

    public LoginFrame() {
        setTitle("Login Inventario");
        setSize(340, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Usuario:"));
        txtUser = new JTextField();
        panel.add(txtUser);

        panel.add(new JLabel("Contraseña:"));
        txtPass = new JPasswordField();
        panel.add(txtPass);

        JButton btnLogin = new JButton("Ingresar");
        JButton btnSalir = new JButton("Salir");

        panel.add(new JLabel("")); // espacio
        panel.add(btnLogin);

        add(panel);

        // Acción del botón Ingresar
        btnLogin.addActionListener(e -> login());

        // Acción del botón Salir
        btnSalir.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void login() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuario y contraseña son obligatorios", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/auth/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                // Leer respuesta
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONObject obj = new JSONObject(response.toString());

                Usuario usuario = new Usuario();
                usuario.setId(obj.getLong("id"));
                usuario.setUsername(obj.getString("username"));
                usuario.setRole(Role.valueOf(obj.getString("role")));

                // Iniciar sesión
                SesionUsuario.iniciarSesion(usuario);

                JOptionPane.showMessageDialog(this,
                        "¡Bienvenido " + usuario.getUsername() + "!\nRol: " + usuario.getRole(),
                        "Acceso Correcto", JOptionPane.INFORMATION_MESSAGE);

                // Cerrar login y abrir MainApp correctamente
                this.dispose();

                // Importante: Ejecutar en el Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    new MainApp().setVisible(true);
                });

            } else {
                String errorMsg = "Usuario o contraseña incorrectos";
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                    StringBuilder err = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) err.append(line);
                    if (err.length() > 0) errorMsg = err.toString();
                } catch (Exception ignored) {}

                JOptionPane.showMessageDialog(this, errorMsg, "Error de Login", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error de conexión con el servidor.\n\n" + ex.getMessage() +
                            "\n\nAsegúrate que el backend esté corriendo.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}