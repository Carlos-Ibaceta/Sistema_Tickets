package com.example.system_tickets.repository;

import com.example.system_tickets.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    // Método antiguo (trae todos, activos e inactivos) - SE MANTIENE por compatibilidad
    List<Usuario> findByRol_NombreRol(String nombreRol);

    // --- NUEVO MÉTODO CRÍTICO (FILTRA SOLO ACTIVOS) ---
    // Este es el que usaremos para enviar correos
    List<Usuario> findByRol_NombreRolAndActivoTrue(String nombreRol);

    boolean existsByEmail(String email);

    Optional<Usuario> findByResetToken(String resetToken);

    // Paginación excluyendo al usuario actual
    Page<Usuario> findByEmailNot(String email, Pageable pageable);

    // Métodos para RUT
    Optional<Usuario> findByRut(String rut);
    boolean existsByRut(String rut);
}
