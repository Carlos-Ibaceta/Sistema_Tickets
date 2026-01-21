package com.example.system_tickets.repository;

import com.example.system_tickets.entity.EstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoTicketRepository extends JpaRepository<EstadoTicket, Long> {
    // Busca el estado por su nombre exacto (ej: "RESUELTO")
    Optional<EstadoTicket> findByNombreEstado(String nombreEstado);
}