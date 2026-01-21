package com.example.system_tickets.service;

import com.example.system_tickets.dto.RegisterRequest;
import com.example.system_tickets.entity.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> obtenerPorId(Long id);
    boolean existeEmail(String email);
    List<Usuario> listarTodos();
    List<Usuario> listarTecnicos();
    Usuario guardarUsuario(Usuario usuario);
    void eliminarUsuario(Long id);
    void toggleKpi(Long usuarioId);
    Usuario crearUsuario(RegisterRequest request);

    // --- NUEVOS MÉTODOS IMPLEMENTADOS ---
    Optional<Usuario> buscarPorRut(String rut);
    boolean existeRut(String rut);

    // VALIDACIÓN DE SEGURIDAD
    // 1. Método que lanza excepción (para lógica interna)
    void validarPasswordSegura(String password);

    // 2. Método booleano (para usar en los 'if' de los controladores)
    boolean esPasswordSegura(String password);

    // --- GESTIÓN DE ESTADO (ACTIVO/INACTIVO) ---
    void toggleEstado(Long usuarioId);
}