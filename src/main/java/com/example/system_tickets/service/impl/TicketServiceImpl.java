package com.example.system_tickets.service.impl;

import com.example.system_tickets.entity.*;
import com.example.system_tickets.repository.*;
import com.example.system_tickets.service.TicketService;
import com.example.system_tickets.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EstadoTicketRepository estadoTicketRepository; // IMPORTANTE: Nuevo Repo
    @Autowired private EmailService emailService;
    @Autowired private AuditoriaRepository auditoriaRepository;

    @Override
    public Ticket crearTicket(Ticket ticket, String emailUsuario) {
        Usuario solicitante = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ticket.setUsuario(solicitante);
        ticket.setFechaCreacion(LocalDateTime.now());

        // Asigna estado inicial INGRESADO
        EstadoTicket estado = estadoTicketRepository.findByNombreEstado("INGRESADO")
                .orElseThrow(() -> new RuntimeException("Error: El estado 'INGRESADO' no existe en la base de datos."));
        ticket.setEstadoTicket(estado);

        Ticket guardado = ticketRepository.save(ticket);
        registrarAuditoria(guardado, null, "INGRESADO", solicitante.getNombre());

        // Notificar soporte
        List<Usuario> soporte = usuarioRepository.findByRol_NombreRol("SOPORTE");
        if (!soporte.isEmpty()) {
            emailService.notificarNuevoTicketSoporte(soporte, guardado);
        }

        return guardado;
    }

    @Override
    public Ticket autoasignarTicket(Long ticketId, Long tecnicoId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        Usuario tecnico = usuarioRepository.findById(tecnicoId).orElseThrow();
        String estadoAnterior = ticket.getEstadoTicket().getNombreEstado();

        ticket.setTecnicoAsignado(tecnico);

        // Al asignar, pasa a EN_PROCESO automáticamente
        EstadoTicket enProceso = estadoTicketRepository.findByNombreEstado("EN_PROCESO")
                .orElseThrow(() -> new RuntimeException("Estado EN_PROCESO no encontrado"));
        ticket.setEstadoTicket(enProceso);

        Ticket actualizado = ticketRepository.save(ticket);
        registrarAuditoria(actualizado, estadoAnterior, "EN_PROCESO", tecnico.getNombre());
        emailService.notificarAsignacionAlUsuario(actualizado);

        return actualizado;
    }

    @Override
    public Ticket actualizarEstado(Long ticketId, String nuevoEstado) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        String estadoAnterior = ticket.getEstadoTicket().getNombreEstado();

        // Buscamos el nuevo estado en la BD (RESUELTO, ESCALADO, etc.)
        EstadoTicket estadoDb = estadoTicketRepository.findByNombreEstado(nuevoEstado)
                .orElseThrow(() -> new RuntimeException("El estado '" + nuevoEstado + "' no existe en la BD"));

        ticket.setEstadoTicket(estadoDb);
        Ticket actualizado = ticketRepository.save(ticket);

        registrarAuditoria(actualizado, estadoAnterior, nuevoEstado, "Soporte Técnico");

        // Notificar al usuario del cambio
        emailService.notificarResultadoSolicitante(actualizado);

        return actualizado;
    }

    @Override
    public void avisarIncapacidadAdmin(Long ticketId, String motivo) {
        Ticket t = ticketRepository.findById(ticketId).orElseThrow();
        String ant = t.getEstadoTicket().getNombreEstado();

        EstadoTicket escalado = estadoTicketRepository.findByNombreEstado("ESCALADO")
                .orElseThrow(() -> new RuntimeException("Estado ESCALADO no existe"));

        t.setEstadoTicket(escalado);
        ticketRepository.save(t);

        registrarAuditoria(t, ant, "ESCALADO", "Soporte Técnico");

        List<Usuario> admins = usuarioRepository.findByRol_NombreRol("ADMIN");
        emailService.notificarEscalamientoAdmin(admins, t, motivo);
    }

    private void registrarAuditoria(Ticket t, String ant, String nue, String usu) {
        AuditoriaTicket a = new AuditoriaTicket();
        a.setTicket(t);
        a.setEstadoAnterior(ant);
        a.setEstadoNuevo(nue);
        a.setUsuarioAccion(usu);
        a.setFechaCambio(LocalDateTime.now());
        auditoriaRepository.save(a);
    }

    // Métodos de lectura y eliminación
    @Override public List<Ticket> listarTodos() { return ticketRepository.findAll(); }
    @Override public Optional<Ticket> obtenerTicketPorId(Long id) { return ticketRepository.findById(id); }
    @Override public List<Ticket> listarTicketsPorUsuario(Long id) { return ticketRepository.findByUsuario_Id(id); }
    @Override public List<Ticket> listarPorEstado(String n) { return ticketRepository.findByEstadoTicket_NombreEstado(n); }
    @Override public List<Ticket> listarPorDepartamento(Long id) { return ticketRepository.findByDepartamento_Id(id); }
    @Override public void eliminarTicket(Long id) { ticketRepository.deleteById(id); }

    // Método pendiente de implementación (si no lo usas, déjalo así o lanza excepción)
    @Override public Ticket asignarTecnico(Long tId, Long teId, String p) { return null; }
}