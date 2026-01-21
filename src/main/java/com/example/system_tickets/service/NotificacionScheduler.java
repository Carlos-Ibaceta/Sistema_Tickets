package com.example.system_tickets.service;

import com.example.system_tickets.entity.Ticket;
import com.example.system_tickets.entity.Usuario;
import com.example.system_tickets.repository.TicketRepository;
import com.example.system_tickets.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificacionScheduler {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JavaMailSender mailSender;

    // --- MODO PRODUCCIÓN: CADA 4 HORAS (14.400.000 ms), CADA 1 MINUTO (60.000) SOLO PRUEBAS ---
    @Scheduled(fixedRate = 14400000)
    public void recordarTicketsPendientes() {
        System.out.println("--- ⏰ EJECUTANDO SCHEDULER (4 HORAS): Buscando tickets pendientes... ---");

        // 1. Buscar todos los tickets
        List<Ticket> todos = ticketRepository.findAll();

        // 2. Buscar técnicos de soporte
        List<Usuario> soportes = usuarioRepository.findByRol_NombreRol("SOPORTE");

        if (soportes.isEmpty()) {
            System.out.println("⚠️ No hay usuarios de soporte para notificar.");
            return;
        }

        int contadorRecordatorios = 0;

        for (Ticket t : todos) {
            String estado = t.getEstadoTicket().getNombreEstado();

            // 3. Filtrar solo los que NO están resueltos ni cerrados
            if (!"RESUELTO".equalsIgnoreCase(estado) &&
                    !"CERRADO".equalsIgnoreCase(estado) &&
                    !"CANCELADO".equalsIgnoreCase(estado)) {

                contadorRecordatorios++;

                // 4. Enviar correo a cada técnico
                for (Usuario s : soportes) {
                    try {
                        SimpleMailMessage msg = new SimpleMailMessage();
                        msg.setTo(s.getEmail());
                        msg.setSubject("⏰ RECORDATORIO: Ticket #" + t.getId() + " sigue pendiente");

                        StringBuilder sb = new StringBuilder();
                        sb.append("Hola ").append(s.getNombre()).append(",\n\n");
                        sb.append("Este es un recordatorio automático del sistema (cada 4 horas).\n");
                        sb.append("El ticket #").append(t.getId()).append(" ('").append(t.getAsunto()).append("') aún no ha sido resuelto.\n\n");
                        sb.append("--- ESTADO ACTUAL ---\n");
                        sb.append("Estado: ").append(estado).append("\n");
                        sb.append("Prioridad: ").append(t.getPrioridad() != null ? t.getPrioridad().getNivelPrioridad() : "N/A").append("\n");
                        sb.append("Fecha Creación: ").append(t.getFechaCreacion()).append("\n\n");
                        sb.append("Por favor, revisar a la brevedad.");

                        msg.setText(sb.toString());
                        mailSender.send(msg);
                    } catch (Exception e) {
                        System.out.println("❌ Error enviando recordatorio a " + s.getEmail() + ": " + e.getMessage());
                    }
                }
            }
        }

        if (contadorRecordatorios > 0) {
            System.out.println("--- 📨 Se enviaron recordatorios para " + contadorRecordatorios + " tickets ---");
        }
    }
}