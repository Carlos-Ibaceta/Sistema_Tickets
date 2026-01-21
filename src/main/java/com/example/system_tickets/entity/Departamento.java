package com.example.system_tickets.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departamentos")
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_departamento", nullable = false, length = 100)
    private String nombreDepartamento;

    @OneToMany(mappedBy = "departamento")
    private Set<Ticket> tickets = new HashSet<>();

    // --- RELACIÓN CON USUARIOS ---
    @OneToMany(mappedBy = "departamento")
    private Set<Usuario> usuarios = new HashSet<>();

    public Departamento() {}

    public Departamento(String nombre) {
        this.nombreDepartamento = nombre;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreDepartamento() { return nombreDepartamento; }
    public void setNombreDepartamento(String nombreDepartamento) { this.nombreDepartamento = nombreDepartamento; }

    // MÉTODOS DE COMPATIBILIDAD (Estos son los que usa tu controlador)
    public String getNombre() {
        return this.nombreDepartamento;
    }

    public void setNombre(String nombre) {
        this.nombreDepartamento = nombre;
    }

    public Set<Ticket> getTickets() { return tickets; }
    public void setTickets(Set<Ticket> tickets) { this.tickets = tickets; }

    public Set<Usuario> getUsuarios() { return usuarios; }
    public void setUsuarios(Set<Usuario> usuarios) { this.usuarios = usuarios; }
}