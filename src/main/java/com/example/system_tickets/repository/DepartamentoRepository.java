package com.example.system_tickets.repository;

import com.example.system_tickets.entity.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    /**
     * Busca un departamento por su nombre
     * @param nombreDepartamento el nombre del departamento
     * @return Optional conteniendo el departamento si existe
     */
    Optional<Departamento> findByNombreDepartamento(String nombreDepartamento);

    /**
     * Verifica si existe un departamento con el nombre especificado
     * @param nombreDepartamento el nombre a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreDepartamento(String nombreDepartamento);
}