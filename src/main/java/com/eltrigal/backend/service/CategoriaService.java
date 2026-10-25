package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.CategoriaResponse;
import com.eltrigal.backend.entity.Categoria;
import com.eltrigal.backend.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de solo lectura para las categorías de productos.
 */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    /**
     * Lista las categorías activas ordenadas por nombre.
     *
     * @return Lista de CategoriaResponse
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Devuelve la categoría con el nombre indicado (ignorando mayúsculas);
     * si no existe, la crea como activa.
     *
     * @param nombre Nombre de la categoría
     * @return CategoriaResponse de la categoría existente o recién creada
     */
    @Transactional
    public CategoriaResponse crearOObtener(String nombre) {
        String nombreLimpio = nombre.trim();
        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(nombreLimpio)
                .orElseGet(() -> {
                    Categoria nueva = new Categoria();
                    nueva.setNombre(nombreLimpio);
                    nueva.setActivo(true);
                    return categoriaRepository.save(nueva);
                });
        return toResponse(categoria);
    }

    /**
     * Mapea una entidad Categoria a su DTO CategoriaResponse.
     */
    private CategoriaResponse toResponse(Categoria categoria) {
        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .build();
    }

}
