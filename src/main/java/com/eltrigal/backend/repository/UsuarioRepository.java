package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su dirección de correo electrónico.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica la existencia de un usuario por su email.
     */
    boolean existsByEmail(String email);

}
