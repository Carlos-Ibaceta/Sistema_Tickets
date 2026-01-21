package com.example.system_tickets.dto;

public class AsignacionDTO {

    private Long ticketId;
    private Long tecnicoId;
    private String prioridad;

    // Constructores
    public AsignacionDTO() {
    }

    public AsignacionDTO(Long ticketId, Long tecnicoId, String prioridad) {
        this.ticketId = ticketId;
        this.tecnicoId = tecnicoId;
        this.prioridad = prioridad;
    }

    // Getters y Setters
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getTecnicoId() {
        return tecnicoId;
    }

    public void setTecnicoId(Long tecnicoId) {
        this.tecnicoId = tecnicoId;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
}