package com.brian.inventario.dto;

import java.time.LocalDateTime;

public class KardexResponse {

    private LocalDateTime fecha;
    private Integer entrada;
    private Integer salida;
    private Integer saldo;
    private String detalle;

    public KardexResponse(LocalDateTime fecha, Integer entrada, Integer salida, Integer saldo, String detalle) {
        this.fecha = fecha;
        this.entrada = entrada;
        this.salida = salida;
        this.saldo = saldo;
        this.detalle = detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Integer getEntrada() {
        return entrada;
    }

    public Integer getSalida() {
        return salida;
    }

    public Integer getSaldo() {
        return saldo;
    }

    public String getDetalle() {
        return detalle;
    }
}