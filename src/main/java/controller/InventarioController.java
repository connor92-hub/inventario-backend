package com.brian.inventario.controller;

import com.brian.inventario.model.Producto;
import com.brian.inventario.repository.MovimientoRepository;
import com.brian.inventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/inventario")
@CrossOrigin(origins = "*")
public class InventarioController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @GetMapping
    public List<Map<String, Object>> obtenerInventario() {
        List<Producto> productos = productoRepository.findAll();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Producto p : productos) {
            Integer stock = movimientoRepository.calcularStock(p.getId());

            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("nombre", p.getNombre());
            item.put("stock", stock != null ? stock : 0);

            resultado.add(item);
        }
        return resultado;
    }
}