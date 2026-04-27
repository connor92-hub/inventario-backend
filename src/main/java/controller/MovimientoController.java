package com.brian.inventario.controller;

import com.brian.inventario.dto.MovimientoResponse;
import com.brian.inventario.model.Movimiento;
import com.brian.inventario.model.Producto;
import com.brian.inventario.repository.MovimientoRepository;
import com.brian.inventario.repository.ProductoRepository;
import com.brian.inventario.dto.KardexResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoController {

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public MovimientoController(MovimientoRepository movimientoRepository,
                                ProductoRepository productoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    // LISTAR
    @GetMapping
    public List<MovimientoResponse> listar() {
        List<Movimiento> movimientos = movimientoRepository.findAll();
        return movimientos.stream().map(m ->
                new MovimientoResponse(
                        m.getId(),
                        m.getCantidad(),
                        m.getComentario(),
                        m.getFecha(),
                        m.getTipo(),
                        m.getProveedor(),
                        m.getRecibidoPor(),
                        (m.getProducto() != null) ? m.getProducto().getNombre() : "Producto eliminado"
                )
        ).collect(Collectors.toList());
    }

    // CREAR MOVIMIENTO
    @PostMapping
    public ResponseEntity<?> crearMovimiento(@RequestBody Movimiento movimiento) {
        try {
            if (movimiento.getProducto() == null || movimiento.getProducto().getId() == null) {
                return ResponseEntity.badRequest().body("Producto es obligatorio");
            }

            Producto producto = productoRepository.findById(movimiento.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Integer stockActual = movimientoRepository.calcularStock(producto.getId());
            if (stockActual == null) stockActual = 0;

            if (stockActual + movimiento.getCantidad() < 0) {
                return ResponseEntity.badRequest().body("Stock insuficiente");
            }

            // El tipo lo manda el frontend, no lo sobreescribimos aquí
            movimiento.setProducto(producto);
            movimiento.setFecha(LocalDateTime.now());
            if (movimiento.getComentario() == null) movimiento.setComentario("");
            if (movimiento.getProveedor() == null) movimiento.setProveedor("");
            if (movimiento.getRecibidoPor() == null) movimiento.setRecibidoPor("");

            Movimiento guardado = movimientoRepository.save(movimiento);
            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al guardar movimiento");
        }
    }

    // KARDEX
    @GetMapping("/kardex/{productoId}")
    public List<KardexResponse> kardex(@PathVariable Long productoId) {
        List<Movimiento> movimientos = movimientoRepository.findByProductoIdOrderByFechaAsc(productoId);
        return construirKardex(movimientos);
    }

    private List<KardexResponse> construirKardex(List<Movimiento> movimientos) {
        List<KardexResponse> resultado = new java.util.ArrayList<>();
        int saldo = 0;
        for (Movimiento m : movimientos) {
            int entrada = 0, salida = 0;
            if ("SALIDA".equals(m.getTipo())) {
                salida = Math.abs(m.getCantidad());
                saldo -= salida;
            } else {
                entrada = m.getCantidad();
                saldo += entrada;
            }

            String detalle = switch (m.getTipo()) {
                case "ENTRADA" -> "Proveedor: " + m.getProveedor();
                case "SALIDA" -> "Recibió: " + m.getRecibidoPor();
                case "DEVOLUCION" -> "Devolvió: " + m.getRecibidoPor();
                default -> "";
            };

            resultado.add(new KardexResponse(m.getFecha(), entrada, salida, saldo, detalle));
        }
        return resultado;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            movimientoRepository.deleteById(id);
            return ResponseEntity.ok("Movimiento eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar movimiento");
        }
    }
}
