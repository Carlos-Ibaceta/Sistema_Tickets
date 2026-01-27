package com.example.system_tickets.service.impl;

import com.example.system_tickets.entity.Usuario;
import com.example.system_tickets.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // SOLUCIÓN DE SEGURIDAD:
        // El tercer parámetro 'usuario.isActivo()' es la clave.
        // Si es false, Spring bloqueará el login automáticamente diciendo "Cuenta deshabilitada".

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.isActivo(), // <--- AQUÍ ESTÁ EL CANDADO (enabled)
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRol().getNombreRol()))
        );
    }
}