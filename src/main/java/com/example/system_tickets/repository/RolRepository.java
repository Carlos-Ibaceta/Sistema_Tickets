package com.example.system_tickets.repository;

import com.example.system_tickets.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre
     * @param nombreRol el nombre del rol a buscar
     * @return Optional conteniendo el rol si existe
     */
    Optional<Rol> findByNombreRol(String nombreRol);

    /**
     * Verifica si existe un rol con el nombre especificado
     * @param nombreRol el nombre del rol a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreRol(String nombreRol);
}