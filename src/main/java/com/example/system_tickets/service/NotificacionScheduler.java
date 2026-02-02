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
import java.util.Arrays;

@Component
public class NotificacionScheduler {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JavaMailSender mailSender;

    // --- MODO PRODUCCIÓN: CADA 4 HORAS (14.400.000 ms) ---
    @Scheduled(fixedRate = 14400000)
    public void recordarTicketsPendientes() {
        System.out.println("--- ⏰ EJECUTANDO SCHEDULER: Buscando tickets pendientes... ---");

        List<Ticket> todos = ticketRepository.findAll();

        // --- CORRECCIÓN AQUÍ: Usamos el método que filtra solo activos ---
        // Antes: findByRol_NombreRol("SOPORTE")
        List<Usuario> soportes = usuarioRepository.findByRol_NombreRolAndActivoTrue("SOPORTE");

        if (soportes.isEmpty()) {
            System.out.println("ℹ️ Info: No hay soportes activos para notificar.");
            return;
        }

        int contadorRecordatorios = 0;

        // --- LISTA NEGRA: Estados que NO deben recibir correo ---
        List<String> estadosIgnorados = Arrays.asList(
                "RESUELTO",
                "CERRADO",
                "CANCELADO",
                "ESCALADO",
                "NO RESUELTO",
                "NO_RESUELTO"
        );

        for (Ticket t : todos) {
            // Obtenemos el nombre y quitamos espacios en blanco por si acaso
            String estado = t.getEstadoTicket().getNombreEstado().trim().toUpperCase();

            // Si el estado actual NO está en la lista negra, enviamos correo
            if (!estadosIgnorados.contains(estado)) {

                contadorRecordatorios++;

                for (Usuario s : soportes) {
                    try {
                        SimpleMailMessage msg = new SimpleMailMessage();
                        msg.setTo(s.getEmail());
                        msg.setSubject("⏰ RECORDATORIO: Ticket #" + t.getId() + " requiere atención");

                        StringBuilder sb = new StringBuilder();
                        sb.append("Hola ").append(s.getNombre()).append(",\n\n");
                        sb.append("Recordatorio automático (4 horas).\n");
                        sb.append("El ticket #").append(t.getId()).append(" ('").append(t.getAsunto()).append("') sigue pendiente.\n\n");
                        sb.append("Estado Actual: ").append(estado).append("\n");
                        sb.append("Prioridad: ").append(t.getPrioridad() != null ? t.getPrioridad().getNivelPrioridad() : "N/A").append("\n");
                        sb.append("Fecha: ").append(t.getFechaCreacion()).append("\n\n");

                        msg.setText(sb.toString());
                        mailSender.send(msg);
                    } catch (Exception e) {
                        System.out.println("❌ Error enviando a " + s.getEmail() + ": " + e.getMessage());
                    }
                }
            }
        }

        if (contadorRecordatorios > 0) {
            System.out.println("--- 📨 Se enviaron " + contadorRecordatorios + " recordatorios a soportes activos ---");
        } else {
            System.out.println("--- ✅ Todo al día. No hay recordatorios pendientes. ---");
        }
    }
}
