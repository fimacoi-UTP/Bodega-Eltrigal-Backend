package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad Rol.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Byte> {
}
