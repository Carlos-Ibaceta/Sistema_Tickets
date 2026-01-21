package com.example.system_tickets.service.impl;

import com.example.system_tickets.dto.RegisterRequest;
import com.example.system_tickets.entity.Rol;
import com.example.system_tickets.entity.Usuario;
import com.example.system_tickets.repository.RolRepository;
import com.example.system_tickets.repository.UsuarioRepository;
import com.example.system_tickets.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        // Encriptar solo si la contraseña ha cambiado (no está ya encriptada)
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario crearUsuario(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // VALIDACIÓN DE SEGURIDAD AL CREAR
        if (!esPasswordSegura(request.getPassword())) {
            throw new IllegalArgumentException("La contraseña es muy débil (Requiere: 8 caracteres, Mayúscula, Número y Símbolo).");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setKpisHabilitados(false);
        usuario.setCrearUsuariosHabilitado(false);

        Rol rol = rolRepository.findByNombreRol("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Error: Rol CLIENTE no encontrado"));
        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public List<Usuario> listarTecnicos() {
        return usuarioRepository.findByRol_NombreRol("SOPORTE");
    }

    @Override
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public void toggleKpi(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setKpisHabilitados(!usuario.isKpisHabilitados());
        usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorRut(String rut) {
        return usuarioRepository.findByRut(rut);
    }

    @Override
    public boolean existeRut(String rut) {
        return usuarioRepository.existsByRut(rut);
    }

    // --- IMPLEMENTACIÓN DE SEGURIDAD (TIPO RIOT) ---
    @Override
    public boolean esPasswordSegura(String password) {
        if (password == null) return false;
        // Regex Explicado:
        // (?=.*[0-9])       -> Al menos 1 número
        // (?=.*[a-z])       -> Al menos 1 minúscula
        // (?=.*[A-Z])       -> Al menos 1 mayúscula
        // (?=.*[@#$%^&+=!._\-]) -> Al menos 1 símbolo
        // .{8,}             -> Mínimo 8 caracteres de largo
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._\\-*]).{8,}$";
        return password.matches(regex);
    }

    @Override
    public void validarPasswordSegura(String password) {
        // Reutilizamos el booleano para no duplicar el regex
        if (!esPasswordSegura(password)) {
            throw new IllegalArgumentException("La contraseña es débil. Debe tener al menos 8 caracteres, una mayúscula, un número y un símbolo.");
        }
    }

    @Override
    public void toggleEstado(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(!usuario.isActivo());
        usuarioRepository.save(usuario);
    }
}