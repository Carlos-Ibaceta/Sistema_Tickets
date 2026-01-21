package com.example.system_tickets.controller;

import com.example.system_tickets.entity.Usuario;
import com.example.system_tickets.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping({"/", "/home"})
    public String redirectHome(Authentication authentication) {
        // Si no está logueado, al login
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // --- TRAMPA DE SEGURIDAD GLOBAL ---
        // Buscamos al usuario para ver si tiene el candado puesto
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);

        if (usuario != null && usuario.isCambioPasswordObligatorio()) {
            return "redirect:/obligatorio/cambiar-password";
        }
        // ----------------------------------

        // Redirección según ROL
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("SOPORTE"))) {
            return "redirect:/soporte/dashboard";
        }
        else {
            // Funcionario
            return "redirect:/tickets/mis-tickets";
        }
    }
}