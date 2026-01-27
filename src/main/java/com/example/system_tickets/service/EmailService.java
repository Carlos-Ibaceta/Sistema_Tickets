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

    // --- MÉTODOS PÚBLICOS (ASÍNCRONOS) ---

    @Async
    public void enviarCorreoAsync(String destinatario, String asunto, String cuerpo) {
        enviarEmailReal(destinatario, asunto, cuerpo);
    }

    @Async
    public void notificarCreacionTicketUsuario(Ticket ticket) {
        String asunto = "Ticket Creado - #" + ticket.getId();
        String cuerpo = "Hola " + ticket.getUsuario().getNombre() + ",\n\n" +
                "Tu ticket ha sido ingresado correctamente.\n" +
                "Asunto: " + ticket.getAsunto() + "\n\n" +
                "Atte, Soporte Municipalidad.";
        enviarEmailReal(ticket.getUsuario().getEmail(), asunto, cuerpo);
    }

    @Async
    public void notificarNuevoTicketSoporte(List<Usuario> equipoSoporte, Ticket ticket) {
        String depto = (ticket.getDepartamento() != null) ? ticket.getDepartamento().getNombre() : "General";
        String asunto = "[NUEVO TICKET] #" + ticket.getId();
        String cuerpo = "Se ha creado un ticket nuevo.\nDepartamento: " + depto + "\nAsunto: " + ticket.getAsunto();

        for (Usuario u : equipoSoporte) {
            enviarEmailReal(u.getEmail(), asunto, cuerpo);
        }
    }

    @Async
    public void notificarAsignacionAlUsuario(Ticket ticket) {
        enviarEmailReal(ticket.getUsuario().getEmail(),
                "Ticket en Proceso",
                "Tu ticket #" + ticket.getId() + " ha sido asignado a un técnico y está en revisión.");
    }

    @Async
    public void notificarEscalamientoAdmin(List<Usuario> admins, Ticket ticket, String motivo) {
        for (Usuario a : admins) {
            enviarEmailReal(a.getEmail(),
                    "Ticket Escalado #" + ticket.getId(),
                    "El ticket requiere atención superior.\nMotivo: " + motivo);
        }
    }

    @Async
    public void notificarResultadoSolicitante(Ticket ticket) {
        enviarEmailReal(ticket.getUsuario().getEmail(),
                "Ticket Finalizado",
                "Tu ticket #" + ticket.getId() + " ha sido marcado como: " + ticket.getEstadoTicket().getNombreEstado());
    }

    @Async
    public void notificarReaperturaAdmin(List<Usuario> soporteTeam, Ticket ticket, String nombreAdmin, String estadoAnterior) {
        String asunto = "🔄 URGENTE: Ticket #" + ticket.getId() + " Reabierto";
        String cuerpo = "El Administrador " + nombreAdmin + " ha reactivado el ticket #" + ticket.getId() + ".\n" +
                "Estado previo: " + estadoAnterior;

        for (Usuario s : soporteTeam) {
            enviarEmailReal(s.getEmail(), asunto, cuerpo);
        }
    }

    // --- NUEVO MÉTODO MOVIDO DESDE EL CONTROLADOR (OPTIMIZADO CON ASYNC) ---
    @Async
    public void notificarCambioEstado(Ticket ticket, String nombreNuevoEstado, String notas) {
        if (ticket.getUsuario() == null || ticket.getUsuario().getEmail() == null) return;

        String estadoLegible = nombreNuevoEstado.replace("_", " ");
        String asunto = "Actualización Ticket #" + ticket.getId() + ": " + estadoLegible;

        StringBuilder sb = new StringBuilder();
        sb.append("Hola ").append(ticket.getUsuario().getNombre()).append(",\n\n");

        if ("ESCALADO".equals(nombreNuevoEstado)) {
            sb.append("Tu caso ha sido ESCALADO a un nivel superior para su revisión.\n");
        } else if ("CANCELADO".equals(nombreNuevoEstado)) {
            sb.append("Tu ticket ha sido CANCELADO.\n");
        } else if ("NO_RESUELTO".equals(nombreNuevoEstado)) {
            sb.append("Tu ticket ha sido cerrado como NO RESUELTO.\n");
        } else {
            sb.append("El estado de tu solicitud ha cambiado a: ").append(estadoLegible).append(".\n");
        }

        if (notas != null && !notas.trim().isEmpty()) {
            sb.append("\n------------------------------------------------\n");
            sb.append("📝 COMENTARIO DEL TÉCNICO:\n");
            sb.append(notas);
            sb.append("\n------------------------------------------------\n");
        }

        sb.append("\nSaludos,\nSoporte Informático Municipalidad de Cabildo");

        enviarEmailReal(ticket.getUsuario().getEmail(), asunto, sb.toString());
    }

    // --- MÉTODO PRIVADO (SINCRÓNICO) ---
    private void enviarEmailReal(String destino, String asunto, String texto) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destino);
            mensaje.setSubject(asunto);
            mensaje.setText(texto);
            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("❌ Error enviando email a " + destino + ": " + e.getMessage());
        }
    }
}