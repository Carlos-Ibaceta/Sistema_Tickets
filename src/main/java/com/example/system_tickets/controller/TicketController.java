package com.example.system_tickets.controller;

import com.example.system_tickets.entity.*;
import com.example.system_tickets.repository.*;
import com.example.system_tickets.service.DropdownService;
import com.example.system_tickets.service.EmailService;
import com.example.system_tickets.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DropdownService dropdownService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SubcategoriaRepository subcategoriaRepository;
    @Autowired private EstadoTicketRepository estadoTicketRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    // LISTA BLANCA DE EXTENSIONES PERMITIDAS (SEGURIDAD)
    private static final List<String> EXTENSIONES_PERMITIDAS = Arrays.asList("jpg", "jpeg", "png", "pdf", "doc", "docx");

    @GetMapping("/mis-tickets")
    public String misTickets(Model model, Authentication auth, HttpSession session, @RequestParam(defaultValue = "0") int page) {
        Usuario usuario = cargarDatosSesion(auth, session);
        if (usuario.isCambioPasswordObligatorio()) return "redirect:/obligatorio/cambiar-password";

        // Paginación configurada a 15 tickets por página
        Pageable pageable = PageRequest.of(page, 15, Sort.by("fechaCreacion").descending());

        Page<Ticket> ticketsPage = ticketRepository.findByUsuario(usuario, pageable);
        model.addAttribute("tickets", ticketsPage);
        return "tickets/mis-tickets";
    }

    @GetMapping("/nuevo")
    public String formularioNuevoTicket(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = cargarDatosSesion(auth, session);
        if (usuario.isCambioPasswordObligatorio()) return "redirect:/obligatorio/cambiar-password";

        Ticket ticket = new Ticket();
        ticket.setFechaIncidente(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        model.addAttribute("ticket", ticket);
        model.addAttribute("departamentos", dropdownService.listarDepartamentos());

        return "tickets/nuevo";
    }

    @PostMapping("/guardar")
    public String guardarTicket(@ModelAttribute Ticket ticket,
                                BindingResult result,
                                @RequestParam(value = "archivoImagen", required = false) MultipartFile imagen,
                                @RequestParam("departamento") Long departamentoId,
                                Authentication auth,
                                RedirectAttributes flash) {
        try {
            // 1. Asignar Usuario
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
            ticket.setUsuario(usuario);
            ticket.setFechaCreacion(LocalDateTime.now());

            // 2. Asignar Departamento Manualmente
            Departamento depto = departamentoRepository.findById(departamentoId)
                    .orElseThrow(() -> new RuntimeException("Error: El departamento seleccionado no existe."));
            ticket.setDepartamento(depto);

            // 3. Asignar Estado Inicial "INGRESADO"
            EstadoTicket estadoInicial = estadoTicketRepository.findByNombreEstado("INGRESADO")
                    .orElseThrow(() -> new RuntimeException("Error: Estado INGRESADO no configurado en BD."));
            ticket.setEstadoTicket(estadoInicial);

            // 4. Prioridad: SE DEJA NULL (La asignará Soporte después)
            ticket.setPrioridad(null);

            // 5. BLINDAJE DE SEGURIDAD PARA ARCHIVOS
            if (imagen != null && !imagen.isEmpty()) {
                String nombreOriginal = imagen.getOriginalFilename();
                String extension = "";

                if (nombreOriginal != null && nombreOriginal.contains(".")) {
                    extension = nombreOriginal.substring(nombreOriginal.lastIndexOf(".") + 1).toLowerCase();
                }

                // VALIDACIÓN: ¿Está en la lista blanca?
                if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
                    flash.addFlashAttribute("error", "⛔ Error de seguridad: Tipo de archivo no permitido. Solo se aceptan: JPG, PNG, PDF, DOC, DOCX.");
                    return "redirect:/tickets/nuevo";
                }

                // Si pasa la validación, procedemos a guardar
                String rutaBase = "uploads";
                try {
                    // Limpiamos el nombre para evitar caracteres raros
                    String nombreSeguro = System.currentTimeMillis() + "_" + nombreOriginal.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

                    Path rutaCompleta = Paths.get(rutaBase);
                    if (!Files.exists(rutaCompleta)) Files.createDirectories(rutaCompleta);
                    Files.copy(imagen.getInputStream(), rutaCompleta.resolve(nombreSeguro));
                    ticket.setEvidencia(nombreSeguro);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 6. Guardar Ticket
            Ticket guardado = ticketRepository.save(ticket);

            // 7. Notificaciones (BLINDADO Y FILTRADO POR ACTIVOS)
            try {
                emailService.notificarCreacionTicketUsuario(guardado);

                // --- CAMBIO AQUÍ: Usamos el método que filtra solo usuarios ACTIVOS ---
                List<Usuario> soportes = usuarioRepository.findByRol_NombreRolAndActivoTrue("SOPORTE");

                if (!soportes.isEmpty()) {
                    emailService.notificarNuevoTicketSoporte(soportes, guardado);
                }
            } catch (Exception e) {
                System.err.println("⚠️ ADVERTENCIA: Ticket creado pero falló el envío de correo: " + e.getMessage());
            }

            flash.addFlashAttribute("mensajeExito", "Solicitud creada exitosamente. Ticket #" + guardado.getId());
            return "redirect:/tickets/mis-tickets";

        } catch (Exception e) {
            e.printStackTrace();
            flash.addFlashAttribute("error", "Error al crear ticket: " + e.getMessage());
            return "redirect:/tickets/nuevo";
        }
    }

    // --- PERFIL DEL FUNCIONARIO ---
    @GetMapping("/perfil")
    public String verPerfil(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = cargarDatosSesion(auth, session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("departamentos", dropdownService.listarDepartamentos());
        return "tickets/perfil";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfil(@ModelAttribute Usuario usuario,
                                @RequestParam String passwordActual,
                                RedirectAttributes flash) {
        Usuario original = usuarioService.obtenerPorId(usuario.getId()).orElseThrow();

        if (!passwordEncoder.matches(passwordActual, original.getPassword())) {
            flash.addFlashAttribute("error", "⛔ ERROR DE SEGURIDAD: La contraseña ingresada no es correcta.");
            return "redirect:/tickets/perfil";
        }

        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!usuario.getEmail().matches(emailRegex)) {
            flash.addFlashAttribute("error", "Formato de correo inválido.");
            return "redirect:/tickets/perfil";
        }

        String dominio = usuario.getEmail().substring(usuario.getEmail().indexOf("@") + 1).toLowerCase();
        List<String> dominiosPermitidos = Arrays.asList("municabildo.cl", "gmail.com", "outlook.com", "hotmail.com", "yahoo.com", "live.com");

        if (!dominiosPermitidos.contains(dominio)) {
            flash.addFlashAttribute("error", "Dominio no permitido. Use correo institucional o proveedores reales.");
            return "redirect:/tickets/perfil";
        }

        original.setNombre(usuario.getNombre());
        original.setEmail(usuario.getEmail());
        original.setDepartamento(usuario.getDepartamento());

        usuarioService.guardarUsuario(original);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), auth.getCredentials(), auth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        flash.addFlashAttribute("mensajeExito", "Datos actualizados correctamente.");
        return "redirect:/tickets/mis-tickets";
    }

    @PostMapping("/perfil/cambiar-password")
    public String solicitarCambioPassword(Authentication auth, HttpServletRequest request, RedirectAttributes flash) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setTokenExpiration(LocalDateTime.now().plusHours(1));
            usuarioRepository.save(usuario);

            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) baseUrl += ":" + request.getServerPort();

            String link = baseUrl + request.getContextPath() + "/restablecer?token=" + token;

            String cuerpo = "Hola " + usuario.getNombre() + ",\n\n" +
                    "Has solicitado restablecer tu contraseña. Haz clic en el siguiente enlace:\n" +
                    link + "\n\n" +
                    "Este enlace expira en 1 hora.\n\n" +
                    "Saludos,\nSoporte Municipalidad de Cabildo";

            emailService.enviarCorreoAsync(usuario.getEmail(), "Solicitud de Cambio de Contraseña", cuerpo);

            flash.addFlashAttribute("mensajeExito", "Correo de recuperación enviado a " + usuario.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
            flash.addFlashAttribute("error", "Error al enviar correo: " + e.getMessage());
        }
        return "redirect:/tickets/perfil";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model, Authentication auth, HttpSession session) {
        Usuario u = cargarDatosSesion(auth, session);
        if (u.isCambioPasswordObligatorio()) return "redirect:/obligatorio/cambiar-password";
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        model.addAttribute("ticket", ticket);
        return "tickets/detalle";
    }

    private Usuario cargarDatosSesion(Authentication auth, HttpSession session) {
        Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        session.setAttribute("usuarioNombre", usuario.getNombre());
        return usuario;
    }
}