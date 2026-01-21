package com.example.system_tickets.controller;

import com.example.system_tickets.entity.Ticket;
import com.example.system_tickets.repository.DepartamentoRepository;
import com.example.system_tickets.repository.PrioridadRepository;
import com.example.system_tickets.repository.TicketRepository;
import com.example.system_tickets.service.ExportService;
import com.example.system_tickets.service.UsuarioService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired private ExportService exportService;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private PrioridadRepository prioridadRepository;

    // --- VISTA PRINCIPAL ---
    @GetMapping
    public String verReportes(Model model, Principal principal,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                              @RequestParam(required = false) Long departamentoId,
                              @RequestParam(required = false) Long prioridadId,
                              @RequestParam(required = false, defaultValue = "DESC") String ordenFecha) {

        if (principal != null) {
            usuarioService.buscarPorEmail(principal.getName())
                    .ifPresent(u -> model.addAttribute("usuarioNombre", u.getNombre()));
        }

        List<Ticket> tickets = filtrarTickets(fechaInicio, fechaFin, departamentoId, prioridadId, ordenFecha);

        long total = tickets.size();
        long resueltos = tickets.stream().filter(t -> t.getEstadoTicket() != null && "RESUELTO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())).count();
        long pendientes = tickets.stream().filter(t -> t.getEstadoTicket() != null && "INGRESADO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())).count();
        long enProceso = tickets.stream().filter(t -> t.getEstadoTicket() != null && "EN_PROCESO".equalsIgnoreCase(t.getEstadoTicket().getNombreEstado())).count();

        model.addAttribute("totalTickets", total);
        model.addAttribute("ticketsAbiertos", pendientes + enProceso);
        model.addAttribute("ticketsResueltos", resueltos);

        double tasa = total > 0 ? ((double) resueltos / total) * 100 : 0;
        model.addAttribute("tasaResolucion", String.format("%.0f", tasa));

        model.addAttribute("departamentos", departamentoRepository.findAll());
        model.addAttribute("prioridades", prioridadRepository.findAll());
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("departamentoId", departamentoId);
        model.addAttribute("prioridadId", prioridadId);
        model.addAttribute("ordenFecha", ordenFecha);

        return "admin/reportes";
    }

    // --- EXCEL ---
    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> descargarExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) Long prioridadId,
            @RequestParam(required = false, defaultValue = "DESC") String ordenFecha) {

        List<Ticket> tickets = filtrarTickets(fechaInicio, fechaFin, departamentoId, prioridadId, ordenFecha);
        ByteArrayInputStream in = exportService.exportarTicketsExcel(tickets);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_tickets.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    // --- PDF ---
    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> descargarPDF(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) Long prioridadId,
            @RequestParam(required = false, defaultValue = "DESC") String ordenFecha) {

        List<Ticket> tickets = filtrarTickets(fechaInicio, fechaFin, departamentoId, prioridadId, ordenFecha);
        ByteArrayInputStream in = exportService.exportarTicketsPDF(tickets);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_tickets.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }

    // --- LOGICA DE FILTRADO Y ORDENAMIENTO (ACTUALIZADA) ---
    private List<Ticket> filtrarTickets(LocalDate inicio, LocalDate fin, Long deptoId, Long prioridadId, String orden) {
        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (inicio != null) predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), inicio.atStartOfDay()));
            if (fin != null) predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), fin.atTime(23, 59, 59)));
            if (deptoId != null) predicates.add(cb.equal(root.get("departamento").get("id"), deptoId));
            if (prioridadId != null) predicates.add(cb.equal(root.get("prioridad").get("id"), prioridadId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Lógica de Ordenamiento Mejorada
        Sort sort = Sort.by("fechaCreacion").descending(); // Valor por defecto

        if ("ASC".equals(orden)) {
            sort = Sort.by("fechaCreacion").ascending();
        } else if ("PRIO_ALTA".equals(orden)) {
            // Asumiendo que ID 1 = Alta, 2 = Media, 3 = Baja.
            // Ordenar ascendente pone el 1 (Alta) primero.
            sort = Sort.by("prioridad.id").ascending();
        } else if ("PRIO_BAJA".equals(orden)) {
            // Pone el ID más alto (Baja) primero.
            sort = Sort.by("prioridad.id").descending();
        }

        return ticketRepository.findAll(spec, sort);
    }
}