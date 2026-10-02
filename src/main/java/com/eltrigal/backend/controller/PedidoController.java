package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.PedidoRequest;
import com.eltrigal.backend.dto.PedidoResponse;
import com.eltrigal.backend.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el módulo de pedidos web.
 * Expone endpoints bajo /api/pedidos.
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * Registra un nuevo pedido reservando inventario.
     * POST /api/pedidos -> 201 Created
     */
    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(@Valid @RequestBody PedidoRequest req) {
        PedidoResponse response = pedidoService.crearPedido(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Consulta el detalle de un pedido por su identificador.
     * GET /api/pedidos/{id} -> 200 OK / 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    /**
     * Lista pedidos con filtro opcional por estado.
     * GET /api/pedidos?estado=PENDIENTE -> 200 OK
     */
    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPorEstado(
            @RequestParam(required = false) String estado
    ) {
        return ResponseEntity.ok(pedidoService.listarPorEstado(estado));
    }

    /**
     * Cambia el estado del pedido aplicando la máquina de estados.
     * PUT /api/pedidos/{id}/estado?nuevoEstado=CONFIRMADO -> 200 OK
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado
    ) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, nuevoEstado));
    }

}
