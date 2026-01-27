package com.example.system_tickets.controller;

import com.example.system_tickets.entity.Usuario;
import com.example.system_tickets.repository.UsuarioRepository;
import com.example.system_tickets.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JavaMailSender mailSender;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioService usuarioService;

    // --- LOGIN MODIFICADO PARA DETECTAR INACTIVOS ---
    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model,
                               HttpSession session) {

        if (error != null) {
            String mensajeError = "Credenciales incorrectas.";

            // 1. Recuperamos la excepción exacta que lanzó Spring Security
            Object excepcion = session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

            // 2. Verificamos si es del tipo "Usuario Deshabilitado"
            if (excepcion instanceof DisabledException) {
                mensajeError = " TU CUENTA ESTÁ INACTIVA. Contacta al administrador.";
            }
            else if (excepcion instanceof AuthenticationException) {
                String msg = ((AuthenticationException) excepcion).getMessage();
                if (msg != null && (msg.contains("disabled") || msg.contains("Inactivo"))) {
                    mensajeError = " TU CUENTA ESTÁ INACTIVA. Contacta al administrador.";
                }
            }

            model.addAttribute("error", mensajeError);
        }

        if (logout != null) model.addAttribute("mensaje", "Sesión cerrada.");
        return "login";
    }

    // --- OLVIDÉ MI CONTRASEÑA ---
    @GetMapping("/olvide-password")
    public String formOlvidePassword() {
        return "auth/olvide-password";
    }

    @PostMapping("/enviar-recuperacion")
    public String procesarOlvidePassword(@RequestParam String email,
                                         HttpServletRequest request,
                                         RedirectAttributes flash) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setTokenExpiration(LocalDateTime.now().plusHours(1));
            usuarioRepository.save(usuario);

            String esquema = request.getScheme();
            String servidor = request.getServerName();
            int puerto = request.getServerPort();

            String baseUrl = esquema + "://" + servidor;
            if (puerto != 80 && puerto != 443) { baseUrl += ":" + puerto; }

            // CORRECCIÓN: Se agrega request.getContextPath() para soportar despliegue en subcarpetas
            String link = baseUrl + request.getContextPath() + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Restablecer Contraseña - Municipalidad de Cabildo");
            message.setText("Para restablecer su clave, haga clic aquí:\n" + link);

            try {
                mailSender.send(message);
                flash.addFlashAttribute("success", "Correo enviado con instrucciones.");
            } catch (Exception e) {
                flash.addFlashAttribute("error", "Error al enviar el correo.");
            }
        } else {
            flash.addFlashAttribute("success", "Si el correo existe, se enviaron instrucciones.");
        }
        return "redirect:/olvide-password";
    }

    // --- RESTABLECER CONTRASEÑA ---
    @GetMapping({"/restablecer", "/reset-password"})
    public String formRestablecer(@RequestParam("token") String token, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token);
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getTokenExpiration().isBefore(LocalDateTime.now())) {
            return "auth/error-token";
        }
        model.addAttribute("token", token);
        return "auth/restablecer-password";
    }

    @PostMapping("/guardar-nueva-password")
    public String guardarNuevaPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       HttpServletRequest request,
                                       RedirectAttributes flash) {

        if (!usuarioService.esPasswordSegura(password)) {
            flash.addFlashAttribute("error", "Contraseña débil: Mínimo 8 caracteres, 1 mayúscula, 1 número y 1 símbolo.");
            return "redirect:/restablecer?token=" + token;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setResetToken(null);
            usuario.setTokenExpiration(null);
            usuario.setCambioPasswordObligatorio(false);
            usuarioRepository.save(usuario);

            try { request.logout(); } catch (ServletException e) { }

            flash.addFlashAttribute("success", "Contraseña cambiada exitosamente. Por favor inicie sesión.");
            return "redirect:/login";
        }
        return "redirect:/login?error=true";
    }
}