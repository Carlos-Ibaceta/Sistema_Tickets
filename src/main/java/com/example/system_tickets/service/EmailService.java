package com.example.system_tickets.service;

import com.example.system_tickets.entity.Ticket;
import com.example.system_tickets.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // --- NUEVO: Método Genérico Rápido (Úsalo para lo que quieras) ---
    @Async
    public void enviarCorreoAsync(String destinatario, String asunto, String cuerpo) {
        enviarEmail(destinatario, asunto, cuerpo);
    }

    // --- NUEVO: Confirmación de Creación de Ticket ---
    @Async
    public void notificarCreacionTicketUsuario(Ticket ticket) {
        String asunto = "Ticket Creado Exitosamente - #" + ticket.getId();
        String cuerpo = "Hola " + ticket.getUsuario().getNombre() + ",\n\n" +
                "Tu ticket ha sido ingresado correctamente en nuestro sistema.\n" +
                "Asunto: " + ticket.getAsunto() + "\n\n" +
                "Un técnico lo revisará a la brevedad.\n\n" +
                "Atte, Soporte Municipalidad.";
        enviarEmail(ticket.getUsuario().getEmail(), asunto, cuerpo);
    }

    // --- TUS MÉTODOS EXISTENTES (Ya eran Async, ¡Super!) ---

    @Async
    public void notificarNuevoTicketSoporte(List<Usuario> equipoSoporte, Ticket ticket) {
        // Validación extra por si el departamento es null
        String depto = (ticket.getDepartamento() != null) ? ticket.getDepartamento().getNombre() : "General";
        String asunto = "[NUEVO TICKET] #" + ticket.getId();
        String cuerpo = "Se ha creado un ticket. Departamento: " + depto;

        for (Usuario u : equipoSoporte) {
            enviarEmail(u.getEmail(), asunto, cuerpo);
        }
    }

    @Async
    public void notificarAsignacionAlUsuario(Ticket ticket) {
        enviarEmail(ticket.getUsuario().getEmail(), "Ticket en Proceso", "Tu ticket #" + ticket.getId() + " ha sido asignado.");
    }

    @Async
    public void notificarEscalamientoAdmin(List<Usuario> admins, Ticket ticket, String motivo) {
        for (Usuario a : admins) {
            enviarEmail(a.getEmail(), "Ticket Escalado", "Ticket #" + ticket.getId() + " requiere atención. Motivo: " + motivo);
        }
    }

    @Async
    public void notificarResultadoSolicitante(Ticket ticket) {
        enviarEmail(ticket.getUsuario().getEmail(), "Ticket Finalizado", "Tu ticket ha sido: " + ticket.getEstadoTicket().getNombre());
    }

    // Método privado sincrónico (realiza el envío real)
    private void enviarEmail(String destino, String asunto, String texto) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destino);
            mensaje.setSubject(asunto);
            mensaje.setText(texto);
            mailSender.send(mensaje);
            System.out.println("✅ Correo enviado a: " + destino);
        } catch (Exception e) {
            System.err.println("❌ Error enviando email a " + destino + ": " + e.getMessage());
        }
    }
}