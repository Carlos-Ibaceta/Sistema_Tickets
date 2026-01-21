package com.example.system_tickets.repository;

import com.example.system_tickets.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoría por su nombre
     * @param nombreCategoria el nombre de la categoría
     * @return Optional conteniendo la categoría si existe
     */
    Optional<Categoria> findByNombreCategoria(String nombreCategoria);

    /**
     * Verifica si existe una categoría con el nombre especificado
     * @param nombreCategoria el nombre a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreCategoria(String nombreCategoria);
}