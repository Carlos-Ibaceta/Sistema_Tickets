package com.example.system_tickets.repository;

import com.example.system_tickets.entity.AuditoriaTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<AuditoriaTicket, Long> {
}