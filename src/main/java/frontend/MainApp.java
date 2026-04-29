package com.brian.inventario.frontend;

import com.brian.inventario.model.Usuario;
import com.brian.inventario.security.SesionUsuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainApp extends JFrame {

    private static final String BASE_URL = "https://inventario-backend-rshu.onrender.com";

    private CardLayout cardLayout;
    private JPanel panelPrincipal;
    private DefaultTableModel modeloProductos;

    public MainApp() {
        if (SesionUsuario.getUsuarioActual() == null) {
            JOptionPane.showMessageDialog(this, "No se ha iniciado sesión correctamente.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }

        setTitle("Sistema de Inventario - " + SesionUsuario.getUsuarioActual().getUsername());
        setSize(1150, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        panelPrincipal = new JPanel(cardLayout);

        panelPrincipal.add(menuPrincipal(), "menu");
        panelPrincipal.add(pantallaProductos(), "productos");
        panelPrincipal.add(menuMovimientos(), "movimientos");
        panelPrincipal.add(pantallaEntrada(), "entrada");
        panelPrincipal.add(pantallaSalida(), "salida");
        panelPrincipal.add(pantallaDevolucion(), "devolucion");
        panelPrincipal.add(pantallaHistorial(), "historial");
        panelPrincipal.add(pantallaKardex(), "kardex");

        add(panelPrincipal);
        cardLayout.show(panelPrincipal, "menu");
    }

    class ItemProducto {
        Long id;
        String nombre;

        public ItemProducto(Long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre + " (ID: " + id + ")";
        }
    }

    private JComboBox<ItemProducto> cargarComboProductos() {
        JComboBox<ItemProducto> combo = new JComboBox<>();

        try {
            URL url = new URL(BASE_URL + "/productos");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder res = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) res.append(line);

                JSONArray arr = new JSONArray(res.toString());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    combo.addItem(new ItemProducto(
                            o.getLong("id"),
                            o.optString("nombre", "Sin nombre")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los productos");
        }
        return combo;
    }

    private JPanel menuPrincipal() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 20, 20));

        JButton btnProductos = new JButton("GESTIÓN DE PRODUCTOS");
        JButton btnMovimientos = new JButton("MOVIMIENTOS DE INVENTARIO");

        btnProductos.addActionListener(e -> {
            cargarProductos(modeloProductos);
            cardLayout.show(panelPrincipal, "productos");
        });

        btnMovimientos.addActionListener(e -> cardLayout.show(panelPrincipal, "movimientos"));

        panel.add(btnProductos);
        panel.add(btnMovimientos);
        return panel;
    }

    private JPanel pantallaProductos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JTextField txtNombre = new JTextField(25);
        JButton btnGuardar = new JButton("Guardar Producto");
        JButton btnVolver = new JButton("Volver al Menú");

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formPanel.add(new JLabel("Nombre del Producto:"));
        formPanel.add(txtNombre);

        JPanel botones = new JPanel();
        botones.add(btnGuardar);
        botones.add(btnVolver);

        modeloProductos = new DefaultTableModel(new String[]{"ID", "Nombre", "Stock Actual"}, 0);
        JTable tabla = new JTable(modeloProductos);

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío");
                return;
            }

            try {
                URL url = new URL(BASE_URL + "/productos");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{\"nombre\": \"" + nombre + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("UTF-8"));
                }

                int code = conn.getResponseCode();
                if (code == 200 || code == 201) {
                    JOptionPane.showMessageDialog(this, "✅ Producto guardado correctamente");
                    txtNombre.setText("");
                    cargarProductos(modeloProductos);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar (Código: " + code + ")");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error de conexión");
            }
        });

        btnVolver.addActionListener(e -> cardLayout.show(panelPrincipal, "menu"));
        cargarProductos(modeloProductos);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarProductos(DefaultTableModel modelo) {
        try {
            URL url = new URL(BASE_URL + "/inventario");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder res = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) res.append(line);

                modelo.setRowCount(0);
                JSONArray arr = new JSONArray(res.toString());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    modelo.addRow(new Object[]{
                            o.getLong("id"),
                            o.optString("nombre"),
                            o.optInt("stock", 0)
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel menuMovimientos() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));

        JButton entrada = new JButton("ENTRADA DE PRODUCTOS");
        JButton salida = new JButton("SALIDA DE PRODUCTOS");
        JButton devolucion = new JButton("DEVOLUCIÓN");
        JButton historial = new JButton("HISTORIAL");
        JButton kardex = new JButton("KARDEX");
        JButton volver = new JButton("VOLVER AL MENÚ");

        entrada.addActionListener(e -> cardLayout.show(panelPrincipal, "entrada"));
        salida.addActionListener(e -> cardLayout.show(panelPrincipal, "salida"));
        devolucion.addActionListener(e -> cardLayout.show(panelPrincipal, "devolucion"));
        historial.addActionListener(e -> cardLayout.show(panelPrincipal, "historial"));
        kardex.addActionListener(e -> cardLayout.show(panelPrincipal, "kardex"));
        volver.addActionListener(e -> cardLayout.show(panelPrincipal, "menu"));

        panel.add(entrada);
        panel.add(salida);
        panel.add(devolucion);
        panel.add(historial);
        panel.add(kardex);
        panel.add(volver);

        return panel;
    }

    private JPanel crearPanel(String tipo) {
        JPanel panel = new JPanel(new GridLayout(6, 2, 12, 12));

        JComboBox<ItemProducto> combo = cargarComboProductos();
        JButton btnRefresh = new JButton("🔄");

        JTextField txtCantidad = new JTextField(10);
        JTextField txtCampo = new JTextField(15);
        JTextField txtComentario = new JTextField(15);

        JButton btnGuardar = new JButton("Guardar " + tipo);
        JButton btnVolver = new JButton("Volver");

        JLabel lblCampo = new JLabel("Proveedor:");
        if (tipo.equals("SALIDA")) lblCampo.setText("Quién recibió:");
        else if (tipo.equals("DEVOLUCION")) lblCampo.setText("Quién devolvió:");

        panel.add(new JLabel("Producto:"));

        JPanel panelCombo = new JPanel(new BorderLayout());
        panelCombo.add(combo, BorderLayout.CENTER);
        panelCombo.add(btnRefresh, BorderLayout.EAST);

        panel.add(panelCombo);

        panel.add(new JLabel("Cantidad:"));
        panel.add(txtCantidad);
        panel.add(lblCampo);
        panel.add(txtCampo);
        panel.add(new JLabel("Comentario:"));
        panel.add(txtComentario);

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botonesPanel.add(btnGuardar);
        botonesPanel.add(btnVolver);

        btnRefresh.addActionListener(e -> {
            combo.removeAllItems();
            try {
                URL url = new URL(BASE_URL + "/productos");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder res = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) res.append(line);

                JSONArray arr = new JSONArray(res.toString());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    combo.addItem(new ItemProducto(
                            o.getLong("id"),
                            o.optString("nombre", "Sin nombre")
                    ));
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al refrescar productos");
            }
        });

        btnGuardar.addActionListener(e -> {
            ItemProducto item = (ItemProducto) combo.getSelectedItem();
            if (item == null) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto");
                return;
            }

            String cantStr = txtCantidad.getText().trim();
            if (cantStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese la cantidad");
                return;
            }

            try {
                int cant = Integer.parseInt(cantStr);

                int cantidadFinal = cant;
                if (tipo.equals("SALIDA")) {
                    cantidadFinal = -Math.abs(cant);
                }

                String json = "{"
                        + "\"cantidad\": " + cantidadFinal + ","
                        + "\"tipo\": \"" + tipo + "\","
                        + "\"comentario\": \"" + txtComentario.getText().trim() + "\","
                        + "\"proveedor\": \"" + (tipo.equals("ENTRADA") ? txtCampo.getText().trim() : "") + "\","
                        + "\"recibidoPor\": \"" + (!tipo.equals("ENTRADA") ? txtCampo.getText().trim() : "") + "\","
                        + "\"producto\": {\"id\": " + item.id + "}"
                        + "}";

                URL url = new URL(BASE_URL + "/movimientos");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("UTF-8"));
                }

                int code = conn.getResponseCode();

                if (code >= 200 && code < 300) {
                    JOptionPane.showMessageDialog(this, "✅ " + tipo + " registrada correctamente");

                    txtCantidad.setText("");
                    txtCampo.setText("");
                    txtComentario.setText("");

                    cargarProductos(modeloProductos);
                    btnRefresh.doClick();

                } else {
                    JOptionPane.showMessageDialog(this, "Error al registrar (Código: " + code + ")");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser un número válido");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error de conexión");
            }
        });

        btnVolver.addActionListener(e -> cardLayout.show(panelPrincipal, "movimientos"));

        panel.add(botonesPanel);
        panel.add(new JLabel(""));

        return panel;
    }

    private JPanel pantallaEntrada()  { return crearPanel("ENTRADA"); }
    private JPanel pantallaSalida()   { return crearPanel("SALIDA"); }
    private JPanel pantallaDevolucion(){ return crearPanel("DEVOLUCION"); }

    // ====================== HISTORIAL CON ELIMINAR ======================
    private JPanel pantallaHistorial() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID", "Fecha", "Producto", "Tipo", "Cantidad", "Comentario", "Detalle"}, 0);

        JTable tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);

        JButton btnEliminar = new JButton("🗑 Eliminar Movimiento");
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        JButton btnVolver = new JButton("Volver al Menú");

        btnEliminar.setEnabled(SesionUsuario.esAdmin());

        JPanel south = new JPanel(new FlowLayout());
        south.add(btnEliminar);
        south.add(btnRefrescar);
        south.add(btnVolver);

        btnRefrescar.addActionListener(e -> cargarHistorial(modelo));

        // === ELIMINAR MOVIMIENTO ===
        btnEliminar.addActionListener(e -> {
            int filaSeleccionada = tabla.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un movimiento para eliminar");
                return;
            }

            Long idMovimiento = (Long) modelo.getValueAt(filaSeleccionada, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de eliminar este movimiento?\nEsta acción no se puede deshacer.",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                eliminarMovimiento(idMovimiento, modelo);
            }
        });

        btnVolver.addActionListener(e -> cardLayout.show(panelPrincipal, "movimientos"));

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);

        JLabel lbl = new JLabel("Selecciona un movimiento y presiona 'Eliminar' (solo Admin)", SwingConstants.CENTER);
        panel.add(lbl, BorderLayout.NORTH);

        return panel;
    }

    private void eliminarMovimiento(Long id, DefaultTableModel modelo) {
        try {
            URL url = new URL(BASE_URL + "/movimientos/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");

            int code = conn.getResponseCode();

            if (code >= 200 && code < 300) {
                JOptionPane.showMessageDialog(this, "✅ Movimiento eliminado correctamente");
                cargarHistorial(modelo);   // refrescar tabla
                cargarProductos(modeloProductos); // refrescar stock
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar (Código: " + code + ")");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de conexión al eliminar");
        }
    }

    private void cargarHistorial(DefaultTableModel modelo) {
        try {
            URL url = new URL(BASE_URL + "/movimientos");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder res = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) res.append(line);

                modelo.setRowCount(0);
                JSONArray arr = new JSONArray(res.toString());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);

                    String detalle = "";
                    String tipo = o.optString("tipo", "");
                    if ("ENTRADA".equals(tipo)) detalle = "Proveedor: " + o.optString("proveedor", "");
                    else if ("SALIDA".equals(tipo)) detalle = "Recibió: " + o.optString("recibidoPor", "");
                    else if ("DEVOLUCION".equals(tipo)) detalle = "Devolvió: " + o.optString("recibidoPor", "");

                    modelo.addRow(new Object[]{
                            o.optLong("id"),
                            o.optString("fecha", "").replace("T", " ").substring(0, 19),
                            o.optString("nombreProducto"),
                            tipo,
                            o.optInt("cantidad"),
                            o.optString("comentario", ""),
                            detalle
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo cargar el historial");
        }
    }

    // ====================== KARDEX (Ahora funcional) ======================
    private JPanel pantallaKardex() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JComboBox<ItemProducto> comboProductos = cargarComboProductos();
        JButton btnMostrar = new JButton("Mostrar Kardex");
        JButton btnVolver = new JButton("Volver al Menú");

        DefaultTableModel modeloKardex = new DefaultTableModel(
                new String[]{"Fecha", "Entrada", "Salida", "Saldo", "Detalle"}, 0);

        JTable tablaKardex = new JTable(modeloKardex);
        JScrollPane scroll = new JScrollPane(tablaKardex);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Seleccionar Producto:"));
        topPanel.add(comboProductos);
        topPanel.add(btnMostrar);

        btnMostrar.addActionListener(e -> {
            ItemProducto item = (ItemProducto) comboProductos.getSelectedItem();
            if (item == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto");
                return;
            }
            cargarKardex(item.id, modeloKardex);
        });

        btnVolver.addActionListener(e -> cardLayout.show(panelPrincipal, "movimientos"));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnVolver, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarKardex(Long productoId, DefaultTableModel modelo) {
        try {
            URL url = new URL(BASE_URL + "/movimientos/kardex/" + productoId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder res = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) res.append(line);

                modelo.setRowCount(0);
                JSONArray arr = new JSONArray(res.toString());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    modelo.addRow(new Object[]{
                            o.optString("fecha", "").replace("T", " ").substring(0, 19),
                            o.optInt("entrada", 0),
                            o.optInt("salida", 0),
                            o.optInt("saldo", 0),
                            o.optString("detalle", "")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el Kardex");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}