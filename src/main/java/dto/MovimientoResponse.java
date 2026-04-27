package com.brian.inventario.dto;

import java.time.LocalDateTime;

public class MovimientoResponse {

    private Long id;
    private Integer cantidad;
    private String comentario;
    private LocalDateTime fecha;
    private String tipo;
    private String proveedor;
    private String recibidoPor;
    private String nombreProducto;

    public MovimientoResponse(Long id, Integer cantidad, String comentario, LocalDateTime fecha,
                              String tipo, String proveedor, String recibidoPor, String nombreProducto) {
        this.id = id;
        this.cantidad = cantidad;
        this.comentario = comentario;
        this.fecha = fecha;
        this.tipo = tipo;
        this.proveedor = proveedor;
        this.recibidoPor = recibidoPor;
        this.nombreProducto = nombreProducto;
    }

    // Getters (necesarios para Jackson)
    public Long getId() { return id; }
    public Integer getCantidad() { return cantidad; }
    public String getComentario() { return comentario; }
    public LocalDateTime getFecha() { return fecha; }
    public String getTipo() { return tipo; }
    public String getProveedor() { return proveedor; }
    public String getRecibidoPor() { return recibidoPor; }
    public String getNombreProducto() { return nombreProducto; }
}