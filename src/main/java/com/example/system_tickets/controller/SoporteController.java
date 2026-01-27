package com.example.system_tickets.controller;

import com.example.system_tickets.entity.*;
import com.example.system_tickets.repository.*;
import com.example.system_tickets.service.EmailService;
import com.example.system_tickets.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/soporte")
public class SoporteController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JavaMailSender mailSender;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private PrioridadRepository prioridadRepository;
    @Autowired private EstadoTicketRepository estadoTicketRepository;
    @Autowired private SubcategoriaRepository subcategoriaRepository;

    // --- MIS TICKETS (MODIFICADO: ORDEN POR DEFECTO ANTIGUOS PRIMERO) ---
    @GetMapping("/mis-tickets")
    public String verMisTickets(Model model,
                                Authentication auth,
                                HttpSession session,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) Long categoriaId,
                                @RequestParam(required = false) Long prioridadId,
                                @RequestParam(required = false, defaultValue = "antiguo") String orden) { // Default cambiado a "antiguo"

        Usuario tecnico = cargarDatosSesion(auth, session);

        // Lógica de ordenamiento para SLA (Semáforo):
        // Por defecto ("antiguo") -> Ascendente (Viejos primero -> Rojos/Amarillos arriba)
        // Si elige "reciente" -> Descendente (Nuevos primero -> Verdes arriba)
        Sort sort = Sort.by("fechaCreacion").ascending();

        if ("reciente".equals(orden)) {
            sort = Sort.by("fechaCreacion").descending();
        } else if ("prioridad".equals(orden)) {
            sort = Sort.by("prioridad.id").descending();
        }

        Pageable pageable = PageRequest.of(page, 10, sort);

        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tecnicoAsignado"), tecnico));

            // EXCLUIR TODOS LOS ESTADOS FINALES
            predicates.add(cb.notEqual(root.get("estadoTicket").get("nombreEstado"), "RESUELTO"));
            predicates.add(cb.notEqual(root.get("estadoTicket").get("nombreEstado"), "CANCELADO"));
            predicates.add(cb.notEqual(root.get("estadoTicket").get("nombreEstado"), "CERRADO"));
            predicates.add(cb.notEqual(root.get("estadoTicket").get("nombreEstado"), "ESCALADO"));
            predicates.add(cb.notEqual(root.get("estadoTicket").get("nombreEstado"), "NO_RESUELTO"));

            if (categoriaId != null) predicates.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            if (prioridadId != null) predicates.add(cb.equal(root.get("prioridad").get("id"), prioridadId));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Ticket> ticketsPage = ticketRepository.findAll(spec, pageable);

        model.addAttribute("tickets", ticketsPage);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("prioridades", prioridadRepository.findAll());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("prioridadSeleccionada", prioridadId);
        model.addAttribute("ordenSeleccionado", orden);

        return "soporte/mis-tickets";
    }

    // --- DASHBOARD ---
    @GetMapping("/dashboard")
    public String dashboardSoporte(Model model, Authentication auth, HttpSession session) {
        Usuario tecnico = cargarDatosSesion(auth, session);
        boolean mostrarKpis = tecnico.isKpisHabilitados();
        model.addAttribute("mostrarKpis", mostrarKpis);

        List<Ticket> asignadosTotal = ticketRepository.findByTecnicoAsignado(tecnico);

        // SOLO MOSTRAR ACTIVOS EN LA LISTA RAPIDA
        List<Ticket> activos = asignadosTotal.stream()
                .filter(t -> !t.getEstadoTicket().getNombreEstado().equals("RESUELTO") &&
                        !t.getEstadoTicket().getNombreEstado().equals("CANCELADO") &&
                        !t.getEstadoTicket().getNombreEstado().equals("ESCALADO") &&
                        !t.getEstadoTicket().getNombreEstado().equals("NO_RESUELTO"))
                .limit(5)
                .toList();

        List<Ticket> disponibles = ticketRepository.findByTecnicoAsignadoIsNull();

        if (mostrarKpis) {
            long pendientes = asignadosTotal.stream().filter(t ->
                    !"RESUELTO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado()) &&
                            !"ESCALADO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado()) &&
                            !"CANCELADO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado()) &&
                            !"NO_RESUELTO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())
            ).count();

            long resueltosCount = asignadosTotal.stream().filter(t -> "RESUELTO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())).count();
            double tasaResolucion = (asignadosTotal.isEmpty()) ? 0 : ((double) resueltosCount / asignadosTotal.size()) * 100;

            model.addAttribute("ticketsAsignados", pendientes);
            model.addAttribute("ticketsPendientes", disponibles.size());
            model.addAttribute("ticketsResueltos", resueltosCount);

            long altaPrioridad = asignadosTotal.stream().filter(t ->
                    t.getPrioridad() != null && "ALTA".equalsIgnoreCase(t.getPrioridad().getNivelPrioridad()) &&
                            !"RESUELTO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado()) &&
                            !"ESCALADO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado()) &&
                            !"NO_RESUELTO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado()) &&
                            !"CANCELADO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())
            ).count();

            model.addAttribute("prioridadAlta", altaPrioridad);
            model.addAttribute("tasaResolucion", Math.round(tasaResolucion));
        }

        model.addAttribute("misTickets", activos);
        model.addAttribute("ticketsDisponibles", disponibles);
        return "soporte/dashboard";
    }

    // --- BOLSA DE TICKETS (MODIFICADO: ORDEN POR DEFECTO ANTIGUOS PRIMERO) ---
    @GetMapping("/tickets-disponibles")
    public String verTicketsDisponibles(Model model,
                                        Authentication auth,
                                        HttpSession session,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(required = false) String busqueda,
                                        @RequestParam(required = false, defaultValue = "antiguo") String orden) { // Default cambiado a "antiguo"

        cargarDatosSesion(auth, session);

        // Lógica de ordenamiento para SLA (Semáforo) en Bolsa:
        Sort sort = Sort.by("fechaCreacion").ascending(); // Por defecto: Viejos arriba

        if ("reciente".equals(orden)) {
            sort = Sort.by("fechaCreacion").descending();
        } else if ("prioridad".equals(orden)) {
            sort = Sort.by("prioridad.id").descending();
        }

        Pageable pageable = PageRequest.of(page, 10, sort);

        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("tecnicoAsignado")));

            if (busqueda != null && !busqueda.isEmpty()) {
                String likePattern = "%" + busqueda.toLowerCase() + "%";
                Predicate porUsuario = cb.like(cb.lower(root.get("usuario").get("nombre")), likePattern);
                Predicate porAsunto = cb.like(cb.lower(root.get("asunto")), likePattern);
                predicates.add(cb.or(porUsuario, porAsunto));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Ticket> ticketsPage = ticketRepository.findAll(spec, pageable);

        model.addAttribute("tickets", ticketsPage);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("prioridades", prioridadRepository.findAll());
        model.addAttribute("subcategorias", subcategoriaRepository.findAll());
        model.addAttribute("ordenSeleccionado", orden);
        model.addAttribute("busqueda", busqueda);

        return "soporte/tickets-disponibles";
    }

    // --- DETALLE ---
    @GetMapping("/detalle/{id}")
    public String verDetalleTicket(@PathVariable Long id, Model model, Authentication auth, HttpSession session) {
        cargarDatosSesion(auth, session);
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        String tiempoTranscurrido = "Pendiente de Asignación";

        if (ticket.getFechaAsignacion() != null) {
            LocalDateTime fin = (ticket.getFechaCierre() != null) ? ticket.getFechaCierre() : LocalDateTime.now();
            Duration duracion = Duration.between(ticket.getFechaAsignacion(), fin);

            long dias = duracion.toDays();
            long horas = duracion.toHours() % 24;
            long minutos = duracion.toMinutes() % 60;

            if (dias > 0) {
                tiempoTranscurrido = String.format("%d días, %d hrs", dias, horas);
            } else if (horas > 0) {
                tiempoTranscurrido = String.format("%d hrs, %d min", horas, minutos);
            } else {
                tiempoTranscurrido = String.format("%d min", minutos);
            }
        }

        model.addAttribute("tiempoTranscurrido", tiempoTranscurrido);
        model.addAttribute("ticket", ticket);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("prioridades", prioridadRepository.findAll());
        model.addAttribute("subcategorias", subcategoriaRepository.findAll());
        return "soporte/detalle";
    }

    // --- ACCIONES ---
    @PostMapping("/actualizar-clasificacion")
    public String actualizarClasificacion(@RequestParam Long ticketId,
                                          @RequestParam Long categoriaId,
                                          @RequestParam Long prioridadId,
                                          @RequestParam(required = false) Long subcategoriaId,
                                          RedirectAttributes flash) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if(categoriaId != null) ticket.setCategoria(categoriaRepository.findById(categoriaId).orElse(null));
        if(prioridadId != null) ticket.setPrioridad(prioridadRepository.findById(prioridadId).orElse(null));
        if (subcategoriaId != null) ticket.setSubcategoria(subcategoriaRepository.findById(subcategoriaId).orElse(null));
        else ticket.setSubcategoria(null);

        ticketRepository.save(ticket);
        flash.addFlashAttribute("mensajeExito", "Clasificación actualizada correctamente.");
        return "redirect:/soporte/detalle/" + ticketId;
    }

    @PostMapping("/asignar")
    public String asignarTicketConClasificacion(@RequestParam Long ticketId,
                                                @RequestParam Long categoriaId,
                                                @RequestParam Long prioridadId,
                                                @RequestParam(required = false) Long subcategoriaId,
                                                Authentication auth,
                                                RedirectAttributes redirectAttributes) {
        Usuario tecnico = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();

        ticket.setTecnicoAsignado(tecnico);
        ticket.setCategoria(categoriaRepository.findById(categoriaId).orElse(null));
        ticket.setPrioridad(prioridadRepository.findById(prioridadId).orElse(null));
        if (subcategoriaId != null) ticket.setSubcategoria(subcategoriaRepository.findById(subcategoriaId).orElse(null));
        else ticket.setSubcategoria(null);

        ticket.setFechaAsignacion(LocalDateTime.now());
        EstadoTicket enProceso = estadoTicketRepository.findByNombreEstado("EN_PROCESO").orElse(null);
        if (enProceso != null) ticket.setEstadoTicket(enProceso);

        ticketRepository.save(ticket);

        emailService.notificarCambioEstado(ticket, "EN_PROCESO", null);

        redirectAttributes.addFlashAttribute("mensajeExito", "Ticket #" + ticket.getId() + " tomado correctamente. ¡Buen trabajo!");
        return "redirect:/soporte/tickets-disponibles";
    }

    @GetMapping("/gestion/{id}")
    public String formGestionarTicket(@PathVariable Long id, Model model, Authentication auth, HttpSession session) {
        cargarDatosSesion(auth, session);
        model.addAttribute("ticket", ticketRepository.findById(id).orElseThrow());
        return "soporte/gestion";
    }

    // --- GUARDAR GESTIÓN ---
    @PostMapping("/guardar-gestion")
    public String guardarGestionTicket(@RequestParam Long id,
                                       @RequestParam String nuevoEstado,
                                       @RequestParam(required = false) String notas,
                                       RedirectAttributes flash) {
        Ticket t = ticketRepository.findById(id).orElseThrow();

        EstadoTicket estado = estadoTicketRepository.findByNombreEstado(nuevoEstado).orElse(null);

        if(estado != null) {
            t.setEstadoTicket(estado);
            emailService.notificarCambioEstado(t, nuevoEstado, notas);
        } else {
            System.err.println("❌ ERROR: El estado '" + nuevoEstado + "' no existe en BD.");
        }

        if("RESUELTO".equals(nuevoEstado) ||
                "ESCALADO".equals(nuevoEstado) ||
                "NO_RESUELTO".equals(nuevoEstado) ||
                "CANCELADO".equals(nuevoEstado)) {
            t.setFechaCierre(LocalDateTime.now());
        }

        ticketRepository.save(t);

        if ("RESUELTO".equals(nuevoEstado) ||
                "CANCELADO".equals(nuevoEstado) ||
                "ESCALADO".equals(nuevoEstado) ||
                "NO_RESUELTO".equals(nuevoEstado)) {

            flash.addFlashAttribute("mensajeExito", "Ticket #" + t.getId() + " movido al historial (" + nuevoEstado + ").");
        } else {
            flash.addFlashAttribute("mensajeExito", "Estado del ticket actualizado.");
        }
        return "redirect:/soporte/mis-tickets";
    }

    // --- HISTORIAL ---
    @GetMapping("/historial")
    public String verHistorial(Model model, Authentication auth, HttpSession session, @RequestParam(defaultValue = "0") int page) {
        Usuario tecnico = cargarDatosSesion(auth, session);

        List<String> estadosHistorial = Arrays.asList("RESUELTO", "CANCELADO", "CERRADO", "ESCALADO", "NO_RESUELTO");

        Pageable pageable = PageRequest.of(page, 10, Sort.by("fechaCreacion").descending());

        Page<Ticket> historialPage = ticketRepository.findHistorialExactoPorTecnico(tecnico.getId(), estadosHistorial, pageable);

        model.addAttribute("tickets", historialPage);
        return "soporte/historial";
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model, Authentication auth, HttpSession session) {
        Usuario tecnico = cargarDatosSesion(auth, session);
        model.addAttribute("usuario", tecnico);
        return "soporte/perfil";
    }

    @PostMapping("/perfil/guardar")
    public String guardarMiPerfil(@ModelAttribute Usuario usuario,
                                  @RequestParam String passwordActual,
                                  RedirectAttributes flash) {
        Usuario original = usuarioService.obtenerPorId(usuario.getId()).orElseThrow();

        if (!passwordEncoder.matches(passwordActual, original.getPassword())) {
            flash.addFlashAttribute("error", "⛔ ERROR DE SEGURIDAD: La contraseña ingresada no es correcta.");
            return "redirect:/soporte/perfil";
        }

        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!usuario.getEmail().matches(emailRegex)) {
            flash.addFlashAttribute("error", "Formato de correo inválido.");
            return "redirect:/soporte/perfil";
        }

        String dominio = usuario.getEmail().substring(usuario.getEmail().indexOf("@") + 1).toLowerCase();
        List<String> dominiosPermitidos = Arrays.asList("municabildo.cl", "gmail.com", "outlook.com", "hotmail.com", "yahoo.com", "live.com");
        if (!dominiosPermitidos.contains(dominio)) {
            flash.addFlashAttribute("error", "Dominio no permitido.");
            return "redirect:/soporte/perfil";
        }

        original.setNombre(usuario.getNombre());
        original.setEmail(usuario.getEmail());
        usuarioService.guardarUsuario(original);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), auth.getCredentials(), auth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        flash.addFlashAttribute("mensajeExito", "Perfil actualizado correctamente.");
        return "redirect:/soporte/perfil";
    }

    @PostMapping("/perfil/cambiar-password")
    public String enviarLinkCambioPassword(Authentication auth, HttpServletRequest request, RedirectAttributes flash) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setTokenExpiration(LocalDateTime.now().plusHours(1));
            usuarioRepository.save(usuario);

            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) baseUrl += ":" + request.getServerPort();

            // CORRECCIÓN: Se agrega request.getContextPath() para soportar subcarpetas
            String link = baseUrl + request.getContextPath() + "/restablecer?token=" + token;

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(usuario.getEmail());
            msg.setSubject("Solicitud de Cambio de Contraseña - Municipalidad");
            msg.setText("Hola " + usuario.getNombre() + ",\nClic aquí para cambiar tu clave:\n" + link);

            mailSender.send(msg);
            flash.addFlashAttribute("mensajeExito", "Correo de recuperación enviado a " + usuario.getEmail());
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al enviar correo: " + e.getMessage());
        }
        return "redirect:/soporte/perfil";
    }

    // --- ESTADÍSTICAS ---
    @GetMapping("/estadisticas")
    public String verEstadisticas(Model model, Authentication auth, HttpSession session) {
        Usuario tecnico = cargarDatosSesion(auth, session);
        if (!tecnico.isKpisHabilitados()) return "redirect:/soporte/dashboard";

        List<Ticket> misTickets = ticketRepository.findByTecnicoAsignado(tecnico);
        long total = misTickets.size();

        long resueltos = misTickets.stream().filter(t -> "RESUELTO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())).count();
        long enProceso = misTickets.stream().filter(t -> "EN_PROCESO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())).count();
        int porcentajeExito = (total > 0) ? (int) ((resueltos * 100) / total) : 0;

        long alta = misTickets.stream().filter(t -> t.getPrioridad() != null && "ALTA".equalsIgnoreCase(t.getPrioridad().getNivelPrioridad())).count();
        long media = misTickets.stream().filter(t -> t.getPrioridad() != null && "MEDIA".equalsIgnoreCase(t.getPrioridad().getNivelPrioridad())).count();
        long baja = misTickets.stream().filter(t -> t.getPrioridad() != null && "BAJA".equalsIgnoreCase(t.getPrioridad().getNivelPrioridad())).count();

        List<Object[]> categoriasStats = ticketRepository.contarTicketsPorCategoria();

        model.addAttribute("total", total);
        model.addAttribute("resueltos", resueltos);
        model.addAttribute("enProceso", enProceso);
        model.addAttribute("porcentajeExito", porcentajeExito);

        model.addAttribute("alta", alta);
        model.addAttribute("media", media);
        model.addAttribute("baja", baja);

        model.addAttribute("categoriasStats", categoriasStats);
        model.addAttribute("totalCategorias", misTickets.stream().filter(t -> t.getCategoria() != null).count());

        return "soporte/estadisticas";
    }

    private Usuario cargarDatosSesion(Authentication auth, HttpSession session) {
        Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        session.setAttribute("usuarioNombre", usuario.getNombre());
        session.setAttribute("usuarioKpisHabilitados", usuario.isKpisHabilitados());
        return usuario;
    }
}