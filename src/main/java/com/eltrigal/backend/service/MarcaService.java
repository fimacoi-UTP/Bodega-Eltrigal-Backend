package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.MarcaResponse;
import com.eltrigal.backend.entity.Marca;
import com.eltrigal.backend.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de solo lectura para las marcas de productos.
 */
@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;

    /**
     * Lista las marcas activas ordenadas por nombre.
     *
     * @return Lista de MarcaResponse
     */
    @Transactional(readOnly = true)
    public List<MarcaResponse> listarActivas() {
        return marcaRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Devuelve la marca con el nombre indicado (ignorando mayúsculas);
     * si no existe, la crea como activa.
     *
     * @param nombre Nombre de la marca
     * @return MarcaResponse de la marca existente o recién creada
     */
    @Transactional
    public MarcaResponse crearOObtener(String nombre) {
        String nombreLimpio = nombre.trim();
        Marca marca = marcaRepository.findByNombreIgnoreCase(nombreLimpio)
                .orElseGet(() -> {
                    Marca nueva = new Marca();
                    nueva.setNombre(nombreLimpio);
                    nueva.setActivo(true);
                    return marcaRepository.save(nueva);
                });
        return toResponse(marca);
    }

    /**
     * Mapea una entidad Marca a su DTO MarcaResponse.
     */
    private MarcaResponse toResponse(Marca marca) {
        return MarcaResponse.builder()
                .id(marca.getId())
                .nombre(marca.getNombre())
                .build();
    }

}
