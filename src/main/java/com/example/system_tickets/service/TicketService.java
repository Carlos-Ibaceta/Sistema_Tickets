package com.example.system_tickets.service;

import com.example.system_tickets.entity.Ticket;
import java.util.List;
import java.util.Optional;

public interface TicketService {
    Ticket crearTicket(Ticket ticket, String emailUsuario);
    List<Ticket> listarTicketsPorUsuario(Long usuarioId);
    List<Ticket> listarTodos();
    Optional<Ticket> obtenerTicketPorId(Long id);
    Ticket asignarTecnico(Long ticketId, Long tecnicoId, String prioridad);
    Ticket autoasignarTicket(Long ticketId, Long tecnicoId);
    void avisarIncapacidadAdmin(Long ticketId, String motivo);
    Ticket actualizarEstado(Long ticketId, String nuevoEstado);
    List<Ticket> listarPorEstado(String nombreEstado);
    List<Ticket> listarPorDepartamento(Long departamentoId);
    void eliminarTicket(Long id);
}