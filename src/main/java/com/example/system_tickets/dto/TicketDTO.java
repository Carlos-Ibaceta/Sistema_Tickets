package com.example.system_tickets.dto;

public class TicketDTO {

    private String asunto;
    private String descripcion;
    private Long categoriaId;
    private Long subcategoriaId;
    private Long prioridadId;

    // NUEVO CAMPO NECESARIO
    private Long departamentoId;

    public TicketDTO() {
    }

    public TicketDTO(String asunto, String descripcion, Long categoriaId, Long subcategoriaId, Long prioridadId, Long departamentoId) {
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
        this.subcategoriaId = subcategoriaId;
        this.prioridadId = prioridadId;
        this.departamentoId = departamentoId;
    }

    // Getters y Setters
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public Long getSubcategoriaId() { return subcategoriaId; }
    public void setSubcategoriaId(Long subcategoriaId) { this.subcategoriaId = subcategoriaId; }

    public Long getPrioridadId() { return prioridadId; }
    public void setPrioridadId(Long prioridadId) { this.prioridadId = prioridadId; }

    public Long getDepartamentoId() { return departamentoId; }
    public void setDepartamentoId(Long departamentoId) { this.departamentoId = departamentoId; }
}