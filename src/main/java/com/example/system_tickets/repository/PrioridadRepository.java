package com.example.system_tickets.repository;

import com.example.system_tickets.entity.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrioridadRepository extends JpaRepository<Prioridad, Long> {

    /**
     * Busca una prioridad por su nivel (ej: "Alta", "Media", "Baja")
     * @param nivelPrioridad el nivel de prioridad
     * @return Optional conteniendo la prioridad si existe
     */
    Optional<Prioridad> findByNivelPrioridad(String nivelPrioridad);

    /**
     * Verifica si existe una prioridad con el nivel especificado
     * @param nivelPrioridad el nivel a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNivelPrioridad(String nivelPrioridad);
}