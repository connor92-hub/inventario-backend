package com.brian.inventario.service;

import com.brian.inventario.model.Producto;
import com.brian.inventario.model.Movimiento;
import com.brian.inventario.repository.ProductoRepository;
import com.brian.inventario.repository.MovimientoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    // ===== GUARDAR PRODUCTO =====
    public Producto guardar(Producto producto) {

        // 1. Guardar producto
        Producto nuevo = productoRepository.save(producto);

        // 2. Crear movimiento inicial (IMPORTANTE 🔥)
        Movimiento mov = new Movimiento();
        mov.setProducto(nuevo);
        mov.setCantidad(0); // puedes cambiar a 10 si quieres stock inicial
        mov.setTipo("ENTRADA");
        mov.setComentario("Creación de producto");
        mov.setFecha(LocalDateTime.now());

        movimientoRepository.save(mov);

        return nuevo;
    }

    // ===== LISTAR PRODUCTOS =====
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    // ===== BUSCAR POR ID =====
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    // ===== ELIMINAR =====
    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }
}