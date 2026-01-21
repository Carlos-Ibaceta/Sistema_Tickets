package com.example.system_tickets.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "prioridades")
public class Prioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nivel_prioridad", nullable = false, length = 50)
    private String nivelPrioridad;

    @OneToMany(mappedBy = "prioridad", cascade = CascadeType.ALL)
    private Set<Ticket> tickets = new HashSet<>();

    // Constructores
    public Prioridad() {
    }

    public Prioridad(String nivelPrioridad) {
        this.nivelPrioridad = nivelPrioridad;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNivelPrioridad() {
        return nivelPrioridad;
    }

    public void setNivelPrioridad(String nivelPrioridad) {
        this.nivelPrioridad = nivelPrioridad;
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }
    public String getNombre() {
        return this.nivelPrioridad;
    }
}