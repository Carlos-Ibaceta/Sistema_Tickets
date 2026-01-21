package com.example.system_tickets.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "estados_ticket")
public class EstadoTicket { // Agregado public

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_estado", nullable = false)
    private String nombreEstado;

    public EstadoTicket() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // Este es el método que buscaba el AdminController
    public String getNombre() {
        return nombreEstado;
    }

    public String getNombreEstado() { return nombreEstado; }
    public void setNombreEstado(String nombreEstado) { this.nombreEstado = nombreEstado; }
}