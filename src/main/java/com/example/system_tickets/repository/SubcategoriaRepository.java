package com.example.system_tickets.repository;

import com.example.system_tickets.entity.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {
    // Métodos útiles (opcionales, pero buenos tener)
    List<Subcategoria> findByCategoria_Id(Long categoriaId);
}