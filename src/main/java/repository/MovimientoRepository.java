package com.brian.inventario.repository;

import com.brian.inventario.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    // ====================== LISTAR MOVIMIENTOS ======================
    List<Movimiento> findByProductoIdOrderByFechaAsc(Long productoId);

    // ====================== CÁLCULO DE STOCK (VERSIÓN SIMPLE Y FUNCIONAL) ======================
    @Query("""
        SELECT COALESCE(SUM(m.cantidad), 0)
        FROM Movimiento m
        WHERE m.producto.id = :productoId
    """)
    Integer calcularStock(@Param("productoId") Long productoId);

    // Sumar solo las salidas (para posibles usos futuros)
    @Query("""
        SELECT COALESCE(SUM(m.cantidad), 0)
        FROM Movimiento m
        WHERE m.producto.id = :productoId 
        AND m.tipo = 'SALIDA'
    """)
    Integer sumarSalidas(@Param("productoId") Long productoId);

    // ====================== KARDEX ======================
    @Query("""
        SELECT m FROM Movimiento m
        WHERE m.producto.id = :productoId
        AND m.fecha BETWEEN :desde AND :hasta
        ORDER BY m.fecha ASC
    """)
    List<Movimiento> findByProductoAndFecha(
            @Param("productoId") Long productoId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    // Métodos adicionales útiles
    @Query("SELECT COUNT(m) FROM Movimiento m WHERE m.producto.id = :productoId")
    Long countByProductoId(@Param("productoId") Long productoId);

    Movimiento findTopByProductoIdOrderByFechaDesc(Long productoId);
}