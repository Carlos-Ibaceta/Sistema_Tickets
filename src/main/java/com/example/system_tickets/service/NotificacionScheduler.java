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
import java.util.Arrays; // <--- IMPORTANTE: Agregado para usar listas fáciles

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
        List<Usuario> soportes = usuarioRepository.findByRol_NombreRol("SOPORTE");

        if (soportes.isEmpty()) {
            return;
        }

        int contadorRecordatorios = 0;

        // --- LISTA NEGRA: Estados que NO deben recibir correo ---
        List<String> estadosIgnorados = Arrays.asList(
                "RESUELTO",
                "CERRADO",
                "CANCELADO",
                "ESCALADO",     // <--- AGREGADO: Ya no molestará
                "NO RESUELTO",  // <--- AGREGADO: Ya no molestará
                "NO_RESUELTO"   // <--- AGREGADO: Por si acaso en la BD está con guion bajo
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
                        System.out.println("❌ Error: " + e.getMessage());
                    }
                }
            }
        }

        if (contadorRecordatorios > 0) {
            System.out.println("--- 📨 Se enviaron " + contadorRecordatorios + " recordatorios ---");
        } else {
            System.out.println("--- ✅ Todo al día. No hay recordatorios pendientes. ---");
        }
    }
}