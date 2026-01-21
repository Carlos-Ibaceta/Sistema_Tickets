package com.example.system_tickets.controller;

import com.example.system_tickets.entity.Usuario;
import com.example.system_tickets.repository.UsuarioRepository;
import com.example.system_tickets.service.UsuarioService; // <--- IMPORTANTE
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/obligatorio")
public class PasswordFlowController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioService usuarioService; // <--- INYECTAMOS EL SERVICIO

    // Pantalla de bloqueo (GET)
    @GetMapping("/cambiar-password")
    public String mostrarPantallaBloqueo(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElseThrow();

        if (!usuario.isCambioPasswordObligatorio()) {
            return "redirect:/home";
        }

        model.addAttribute("usuario", usuario);
        return "seguridad/cambio-obligatorio";
    }

    // Procesar cambio (POST)
    @PostMapping("/actualizar")
    public String actualizarPassword(@RequestParam("newPassword") String password,
                                     Principal principal,
                                     RedirectAttributes flash) {

        // --- VALIDACIÓN DE SEGURIDAD ---
        if (!usuarioService.esPasswordSegura(password)) {
            flash.addFlashAttribute("error", "Contraseña débil: Mínimo 8 caracteres, 1 mayúscula, 1 número y 1 símbolo.");
            return "redirect:/obligatorio/cambiar-password";
        }
        // -------------------------------

        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElseThrow();

        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setCambioPasswordObligatorio(false);
        usuarioRepository.save(usuario);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), usuario.getPassword(), auth.getAuthorities())
        );

        flash.addFlashAttribute("success", "Contraseña actualizada correctamente.");
        return "redirect:/home";
    }
}