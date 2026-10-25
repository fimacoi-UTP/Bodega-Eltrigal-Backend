package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un producto favorito del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritoResponse {

    private Long idFavorito;
    private Long productoId;
    private String productoNombre;
    private LocalDateTime creadoEn;

}
