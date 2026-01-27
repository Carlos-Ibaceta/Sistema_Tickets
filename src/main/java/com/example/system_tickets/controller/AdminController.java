package com.example.system_tickets.controller;

import com.example.system_tickets.entity.*;
import com.example.system_tickets.repository.*;
import com.example.system_tickets.service.DropdownService;
import com.example.system_tickets.service.EmailService;
import com.example.system_tickets.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JavaMailSender mailSender;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private DropdownService dropdownService;
    @Autowired private EmailService emailService;

    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private PrioridadRepository prioridadRepository;
    @Autowired private EstadoTicketRepository estadoTicketRepository;
    @Autowired private SubcategoriaRepository subcategoriaRepository;

    // =========================================================================
    // 1. MÉTODOS AUXILIARES PARA NAVEGACIÓN INTELIGENTE
    // =========================================================================

    private boolean esDesdeHistorial(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return referer != null && referer.contains("/admin/historial-global");
    }

    private String obtenerUrlRetorno(HttpServletRequest request) {
        return esDesdeHistorial(request) ? "redirect:/admin/historial-global" : "redirect:/admin/tickets-globales";
    }

    // =========================================================================
    // 2. DASHBOARD Y LISTAS
    // =========================================================================

    @GetMapping("/dashboard")
    public String adminDashboard(
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Long usuarioId,
            Model model, Principal principal) {

        if (principal != null) {
            usuarioService.buscarPorEmail(principal.getName()).ifPresent(admin -> {
                model.addAttribute("usuario", admin);
            });
        }

        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (departamentoId != null) predicates.add(cb.equal(root.get("departamento").get("id"), departamentoId));
            if (estado != null && !estado.isEmpty()) predicates.add(cb.equal(root.get("estadoTicket").get("nombreEstado"), estado));
            if (prioridad != null && !prioridad.isEmpty()) predicates.add(cb.equal(root.get("prioridad").get("nivelPrioridad"), prioridad));
            if (usuarioId != null) predicates.add(cb.equal(root.get("usuario").get("id"), usuarioId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Ticket> ticketsFiltrados = ticketRepository.findAll(spec);
        model.addAttribute("tickets", ticketsFiltrados);
        model.addAttribute("departamentos", dropdownService.listarDepartamentos());
        model.addAttribute("estados", dropdownService.listarEstadosTicket());
        model.addAttribute("prioridades", dropdownService.listarPrioridades());
        model.addAttribute("usuarios", usuarioService.listarTodos());

        List<Ticket> todos = ticketRepository.findAll();
        long enProceso = todos.stream().filter(t -> t.getEstadoTicket() != null && "EN_PROCESO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())).count();
        long resueltos = todos.stream().filter(t -> t.getEstadoTicket() != null && "RESUELTO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())).count();
        long pendientes = todos.stream().filter(t -> t.getEstadoTicket() != null && "INGRESADO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())).count();

        model.addAttribute("totalTickets", todos.size());
        model.addAttribute("ticketsEnProceso", enProceso);
        model.addAttribute("ticketsResueltos", resueltos);
        model.addAttribute("ticketsPendientes", pendientes);

        List<Ticket> recientes = ticketsFiltrados.stream()
                .sorted((t1, t2) -> t2.getFechaCreacion().compareTo(t1.getFechaCreacion()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("ticketsRecientes", recientes);

        return "admin/dashboard";
    }

    // --- MODIFICADO: ORDEN POR DEFECTO ANTIGUOS PRIMERO (ROJOS ARRIBA) ---
    @GetMapping("/tickets-globales")
    public String verTicketsGlobales(Model model,
                                     @RequestParam(required = false) Long departamentoId,
                                     @RequestParam(required = false) Long prioridadId,
                                     @RequestParam(required = false, defaultValue = "antiguos") String orden, // Default cambiado
                                     @RequestParam(defaultValue = "0") int page) {

        // Lógica SLA: "antiguos" (default) -> Ascending (Viejos/Rojos primero)
        Sort sort = "recientes".equals(orden) ? Sort.by("fechaCreacion").descending() : Sort.by("fechaCreacion").ascending();

        Pageable pageable = PageRequest.of(page, 10, sort);

        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (departamentoId != null) predicates.add(cb.equal(root.get("departamento").get("id"), departamentoId));
            if (prioridadId != null) predicates.add(cb.equal(root.get("prioridad").get("id"), prioridadId));

            // Ocultar finalizados en la vista "Sala de Guerra"
            List<String> estadosFinales = Arrays.asList("RESUELTO", "CANCELADO", "ESCALADO", "NO_RESUELTO", "CERRADO");
            predicates.add(cb.not(root.get("estadoTicket").get("nombreEstado").in(estadosFinales)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Ticket> ticketsPage = ticketRepository.findAll(spec, pageable);
        model.addAttribute("tickets", ticketsPage);
        model.addAttribute("departamentos", dropdownService.listarDepartamentos());
        model.addAttribute("prioridades", dropdownService.listarPrioridades());
        model.addAttribute("selectedDepto", departamentoId);
        model.addAttribute("selectedPrio", prioridadId);
        model.addAttribute("selectedOrden", orden);

        return "admin/tickets-globales";
    }

    @GetMapping("/historial-global")
    public String verHistorialGlobal(Model model,
                                     @RequestParam(required = false) Long departamentoId,
                                     @RequestParam(required = false) Long prioridadId,
                                     @RequestParam(required = false, defaultValue = "recientes") String orden,
                                     @RequestParam(defaultValue = "0") int page) {

        Sort sort = "antiguos".equals(orden) ? Sort.by("fechaCreacion").ascending() : Sort.by("fechaCreacion").descending();
        Pageable pageable = PageRequest.of(page, 10, sort);

        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (departamentoId != null) predicates.add(cb.equal(root.get("departamento").get("id"), departamentoId));
            if (prioridadId != null) predicates.add(cb.equal(root.get("prioridad").get("id"), prioridadId));

            // Mostrar SOLO finalizados
            List<String> estadosFinales = Arrays.asList("RESUELTO", "CANCELADO", "ESCALADO", "NO_RESUELTO", "CERRADO");
            predicates.add(root.get("estadoTicket").get("nombreEstado").in(estadosFinales));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Ticket> ticketsPage = ticketRepository.findAll(spec, pageable);
        model.addAttribute("tickets", ticketsPage);
        model.addAttribute("departamentos", dropdownService.listarDepartamentos());
        model.addAttribute("prioridades", dropdownService.listarPrioridades());
        model.addAttribute("selectedDepto", departamentoId);
        model.addAttribute("selectedPrio", prioridadId);
        model.addAttribute("selectedOrden", orden);

        return "admin/historial-global";
    }

    // =========================================================================
    // 3. DETALLE Y GESTIÓN UNIFICADA (SUPERPODERES)
    // =========================================================================

    @GetMapping("/tickets/{id}")
    public String verDetalleTicket(@PathVariable Long id, Model model, HttpServletRequest request) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        model.addAttribute("ticket", ticket);
        model.addAttribute("tecnicos", usuarioRepository.findByRol_NombreRol("SOPORTE"));
        model.addAttribute("estados", estadoTicketRepository.findAll());

        // Enviamos la bandera para saber a dónde debe volver el botón
        model.addAttribute("esDesdeHistorial", esDesdeHistorial(request));

        return "admin/detalle-ticket";
    }

    @PostMapping("/tickets/reasignar")
    public String reasignarTicket(@RequestParam Long ticketId,
                                  @RequestParam(required = false) Long tecnicoId,
                                  @RequestParam(required = false) Long estadoId,
                                  RedirectAttributes flash) {

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        String nombreEstadoAnterior = ticket.getEstadoTicket().getNombreEstado();
        boolean cambioRealizado = false;

        // --- Lógica de Cambio de Estado ---
        if (estadoId != null) {
            EstadoTicket nuevoEstado = estadoTicketRepository.findById(estadoId).orElseThrow();

            if (!nuevoEstado.getId().equals(ticket.getEstadoTicket().getId())) {
                ticket.setEstadoTicket(nuevoEstado);

                // SUPERPODER: Si vuelve a INGRESADO, reseteamos todo
                if ("INGRESADO".equalsIgnoreCase(nuevoEstado.getNombreEstado())) {
                    ticket.setFechaCierre(null);
                    ticket.setTecnicoAsignado(null);
                    tecnicoId = 0L;

                    flash.addFlashAttribute("mensajeExito", "✅ Ticket rescatado: Vuelve a INGRESADO y a la bolsa de activos.");

                    // === DETECTOR DE RESURRECCIÓN (Notificar a Soporte) ===
                    List<String> estadosMuertos = Arrays.asList("RESUELTO", "CANCELADO", "CERRADO", "NO_RESUELTO", "NO RESUELTO", "ESCALADO");

                    if (estadosMuertos.contains(nombreEstadoAnterior.toUpperCase())) {
                        String nombreAdmin = usuarioService.buscarPorEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                                .map(Usuario::getNombre).orElse("Administrador");

                        List<Usuario> equipoSoporte = usuarioRepository.findByRol_NombreRol("SOPORTE");
                        emailService.notificarReaperturaAdmin(equipoSoporte, ticket, nombreAdmin, nombreEstadoAnterior);
                    }
                }
                cambioRealizado = true;
            }
        }

        // --- Lógica de Cambio de Técnico ---
        if (tecnicoId != null) {
            if (tecnicoId > 0) {
                Usuario tecnico = usuarioRepository.findById(tecnicoId).orElseThrow();
                ticket.setTecnicoAsignado(tecnico);
                if (!cambioRealizado) flash.addFlashAttribute("mensajeExito", "Ticket asignado a: " + tecnico.getNombre());
            } else if (tecnicoId == 0 || tecnicoId == -1) {
                ticket.setTecnicoAsignado(null);
                if (!cambioRealizado) flash.addFlashAttribute("mensajeExito", "Ticket liberado a la bolsa.");
            }
        }

        ticketRepository.save(ticket);
        return "redirect:/admin/tickets/" + ticketId;
    }

    // =========================================================================
    // 4. ELIMINACIÓN CON RETORNO INTELIGENTE
    // =========================================================================

    @PostMapping("/tickets/eliminar")
    public String eliminarTicketPost(@RequestParam Long ticketId, HttpServletRequest request, RedirectAttributes flash) {
        try {
            ticketRepository.deleteById(ticketId);
            flash.addFlashAttribute("mensajeExito", "Ticket eliminado permanentemente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al eliminar el ticket.");
        }
        return obtenerUrlRetorno(request);
    }

    @GetMapping("/tickets/eliminar/{id}")
    public String eliminarTicketGet(@PathVariable Long id, HttpServletRequest request, RedirectAttributes flash) {
        try {
            ticketRepository.deleteById(id);
            flash.addFlashAttribute("mensajeExito", "Ticket eliminado.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al eliminar.");
        }
        return obtenerUrlRetorno(request);
    }

    // =========================================================================
    // 5. GESTIÓN DE USUARIOS, PERFIL Y CONFIGURACIÓN
    // =========================================================================

    @GetMapping("/usuarios")
    public String gestionarUsuarios(Model model, Principal principal, @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("nombre").ascending());
        Page<Usuario> usuariosPage = usuarioRepository.findByEmailNot(principal.getName(), pageable);
        model.addAttribute("tecnicos", usuariosPage);
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("titulo", "Crear Nuevo Usuario");
        model.addAttribute("roles", dropdownService.listarRoles());
        model.addAttribute("departamentos", dropdownService.listarDepartamentos());
        return "admin/formulario-usuario";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id).orElseThrow();
        usuario.setPassword("");
        model.addAttribute("usuario", usuario);
        model.addAttribute("titulo", "Editar Usuario");
        model.addAttribute("roles", dropdownService.listarRoles());
        model.addAttribute("departamentos", dropdownService.listarDepartamentos());
        return "admin/formulario-usuario";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes flash) {
        if (usuario.getRut() != null) {
            usuario.setRut(usuario.getRut().replace(".", ""));
        }

        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!usuario.getEmail().matches(emailRegex)) {
            flash.addFlashAttribute("error", "Formato de correo inválido.");
            return (usuario.getId() != null) ? "redirect:/admin/usuarios/editar/" + usuario.getId() : "redirect:/admin/usuarios/crear";
        }

        if (usuario.getId() == null && usuarioRepository.existsByEmail(usuario.getEmail())) {
            flash.addFlashAttribute("error", "Correo ya registrado.");
            return "redirect:/admin/usuarios/crear";
        }

        if (usuario.getId() == null || (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty())) {
            if (!usuarioService.esPasswordSegura(usuario.getPassword())) {
                flash.addFlashAttribute("error", "Contraseña débil.");
                return (usuario.getId() != null) ? "redirect:/admin/usuarios/editar/" + usuario.getId() : "redirect:/admin/usuarios/crear";
            }
        }

        if (usuario.getId() != null) {
            Usuario original = usuarioService.obtenerPorId(usuario.getId()).orElse(null);
            if (original != null) {
                if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                    usuario.setPassword(original.getPassword());
                }
                usuario.setCambioPasswordObligatorio(original.isCambioPasswordObligatorio());
                usuario.setActivo(original.isActivo());
            }
        } else {
            usuario.setCambioPasswordObligatorio(true);
            usuario.setActivo(true);
        }

        usuarioService.guardarUsuario(usuario);
        flash.addFlashAttribute("mensajeExito", "Usuario guardado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/toggle-kpi/{id}")
    public String toggleKpi(@PathVariable Long id, RedirectAttributes flash) {
        Usuario u = usuarioService.obtenerPorId(id).orElse(null);
        if (u != null && "SOPORTE".equalsIgnoreCase(u.getRol().getNombreRol())) {
            usuarioService.toggleKpi(id);
        } else {
            flash.addFlashAttribute("error", "Solo el rol SOPORTE puede tener KPIs.");
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/toggle-estado/{id}")
    public String toggleEstadoUsuario(@PathVariable Long id, RedirectAttributes flash) {
        usuarioService.toggleEstado(id);
        flash.addFlashAttribute("mensajeExito", "Estado del usuario actualizado.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes flash) {
        try {
            usuarioRepository.deleteById(id);
            flash.addFlashAttribute("mensajeExito", "Usuario eliminado.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se puede eliminar (tiene tickets asociados).");
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/perfil")
    public String verMiPerfil(Model model, Principal principal) {
        Usuario admin = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
        model.addAttribute("usuario", admin);
        model.addAttribute("departamentos", dropdownService.listarDepartamentos());
        return "admin/perfil";
    }

    @PostMapping("/perfil/guardar")
    public String guardarMiPerfil(@ModelAttribute Usuario usuario, RedirectAttributes flash) {
        Usuario original = usuarioService.obtenerPorId(usuario.getId()).orElseThrow();
        original.setNombre(usuario.getNombre());
        original.setEmail(usuario.getEmail());
        original.setDepartamento(usuario.getDepartamento());
        usuarioService.guardarUsuario(original);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), auth.getCredentials(), auth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        flash.addFlashAttribute("mensajeExito", "Datos actualizados.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/perfil/cambiar-password")
    public String solicitarCambioPassword(Principal principal, HttpServletRequest request, RedirectAttributes flash) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setTokenExpiration(LocalDateTime.now().plusHours(1));
            usuarioRepository.save(usuario);

            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) baseUrl += ":" + request.getServerPort();

            // CORRECCIÓN: Agregado request.getContextPath() para soportar subcarpetas
            String link = baseUrl + request.getContextPath() + "/restablecer?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(usuario.getEmail());
            message.setSubject("Cambio de Contraseña");
            message.setText("Clic aquí: " + link);
            mailSender.send(message);
            flash.addFlashAttribute("mensajeExito", "Correo enviado.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al procesar.");
        }
        return "redirect:/admin/perfil";
    }

    @GetMapping("/configuracion")
    public String verConfiguracion(Model model) {
        model.addAttribute("departamentos", dropdownService.listarDepartamentos());
        model.addAttribute("categorias", dropdownService.listarCategorias());
        model.addAttribute("prioridades", dropdownService.listarPrioridades());
        model.addAttribute("subcategorias", dropdownService.listarSubcategorias());
        return "admin/configuracion";
    }

    @PostMapping("/configuracion/departamentos/guardar")
    public String guardarDepartamento(@RequestParam String nombre, RedirectAttributes flash) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            Departamento depto = new Departamento(nombre);
            departamentoRepository.save(depto);
            flash.addFlashAttribute("mensajeExito", "Departamento guardado.");
        }
        flash.addFlashAttribute("activeTab", "deptos");
        return "redirect:/admin/configuracion";
    }

    @GetMapping("/configuracion/eliminar-depto/{id}")
    public String eliminarDepartamento(@PathVariable Long id, RedirectAttributes flash) {
        try { departamentoRepository.deleteById(id); flash.addFlashAttribute("mensajeExito", "Departamento eliminado."); }
        catch (Exception e) { flash.addFlashAttribute("error", "No se puede eliminar, está en uso."); }
        flash.addFlashAttribute("activeTab", "deptos");
        return "redirect:/admin/configuracion";
    }

    @PostMapping("/configuracion/categorias/guardar")
    public String guardarCategoria(@RequestParam String nombreCategoria, RedirectAttributes flash) {
        if (nombreCategoria != null && !nombreCategoria.trim().isEmpty()) {
            Categoria c = new Categoria();
            c.setNombreCategoria(nombreCategoria);
            categoriaRepository.save(c);
            flash.addFlashAttribute("mensajeExito", "Categoría guardada.");
        }
        flash.addFlashAttribute("activeTab", "cats");
        return "redirect:/admin/configuracion";
    }

    @GetMapping("/configuracion/eliminar-cat/{id}")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes flash) {
        try { categoriaRepository.deleteById(id); flash.addFlashAttribute("mensajeExito", "Categoría eliminada."); }
        catch (Exception e) { flash.addFlashAttribute("error", "No se puede eliminar, está en uso."); }
        flash.addFlashAttribute("activeTab", "cats");
        return "redirect:/admin/configuracion";
    }

    // --- GUARDAR SUBCATEGORÍA (SIN PRIORIDAD) ---
    @PostMapping("/configuracion/subcategorias/guardar")
    public String guardarSubcategoria(@RequestParam String nombreSubcategoria,
                                      @RequestParam Long categoriaId,
                                      RedirectAttributes flash) {
        if (nombreSubcategoria != null && !nombreSubcategoria.trim().isEmpty()) {
            Subcategoria sub = new Subcategoria(nombreSubcategoria);

            // Asignar Padre
            Categoria cat = categoriaRepository.findById(categoriaId).orElseThrow();
            sub.setCategoria(cat);

            // Se elimina la asignación de prioridad automática.
            // sub.setPrioridadDefecto(null);

            subcategoriaRepository.save(sub);
            flash.addFlashAttribute("mensajeExito", "Subcategoría guardada.");
        }
        flash.addFlashAttribute("activeTab", "subcats");
        return "redirect:/admin/configuracion";
    }

    // --- ELIMINAR SUBCATEGORÍA ---
    @GetMapping("/configuracion/eliminar-subcat/{id}")
    public String eliminarSubcategoria(@PathVariable Long id, RedirectAttributes flash) {
        try {
            subcategoriaRepository.deleteById(id);
            flash.addFlashAttribute("mensajeExito", "Subcategoría eliminada.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se puede eliminar, está en uso en tickets antiguos.");
        }
        flash.addFlashAttribute("activeTab", "subcats");
        return "redirect:/admin/configuracion";
    }

    @GetMapping("/cambiar-clave-obligatoria")
    public String mostrarCambioObligatorio(Model model, Principal principal) {
        if (principal != null) { usuarioService.buscarPorEmail(principal.getName()).ifPresent(u -> model.addAttribute("usuario", u)); }
        return "admin/cambiar-clave-obligatoria";
    }

    @PostMapping("/cambiar-clave-obligatoria/guardar")
    public String procesarCambioObligatorio(@RequestParam String nuevaPassword, Principal principal, RedirectAttributes flash) {
        if (!usuarioService.esPasswordSegura(nuevaPassword)) {
            flash.addFlashAttribute("error", "Contraseña débil.");
            return "redirect:/admin/cambiar-clave-obligatoria";
        }
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setCambioPasswordObligatorio(false);
        usuarioRepository.save(usuario);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), usuario.getPassword(), auth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        flash.addFlashAttribute("mensajeExito", "Contraseña actualizada.");
        return "redirect:/admin/dashboard";
    }
}