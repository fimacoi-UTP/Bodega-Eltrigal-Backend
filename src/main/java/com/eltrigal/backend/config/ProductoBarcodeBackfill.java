package com.eltrigal.backend.config;

import com.eltrigal.backend.entity.Producto;
import com.eltrigal.backend.repository.ProductoRepository;
import com.eltrigal.backend.util.Ean13Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Backfill idempotente para asignar código de barras EAN-13 válido (prefijo 775 Perú)
 * a todos los productos existentes que tengan su código de barras en NULL o en blanco.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductoBarcodeBackfill implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        log.info("Iniciando backfill de códigos de barras EAN-13 para productos...");

        List<Producto> productos = productoRepository.findAll();
        int asignados = 0;

        for (Producto producto : productos) {
            if (producto.getCodigoBarras() == null || producto.getCodigoBarras().isBlank()) {
                String barcode = Ean13Util.generar(producto.getId());
                producto.setCodigoBarras(barcode);
                productoRepository.save(producto);
                asignados++;
                log.info("Producto ID {}: '{}' asignado con EAN-13 -> {}", producto.getId(), producto.getNombre(), barcode);
            }
        }

        if (asignados > 0) {
            log.info("Backfill finalizado: se asignaron códigos de barras EAN-13 a {} producto(s).", asignados);
        } else {
            log.info("Backfill finalizado: todos los productos ya contaban con código de barras EAN-13 válido.");
        }
    }

}
