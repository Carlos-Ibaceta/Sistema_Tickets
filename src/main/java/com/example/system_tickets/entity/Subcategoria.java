package com.example.system_tickets.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subcategorias")
public class Subcategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_subcategoria", nullable = false, length = 100)
    private String nombreSubcategoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // --- NUEVO: PRIORIDAD AUTOMÁTICA ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prioridad_defecto_id")
    private Prioridad prioridadDefecto;
    // -----------------------------------

    @OneToMany(mappedBy = "subcategoria", cascade = CascadeType.ALL)
    private Set<Ticket> tickets = new HashSet<>();

    public Subcategoria() {}

    public Subcategoria(String nombreSubcategoria) {
        this.nombreSubcategoria = nombreSubcategoria;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreSubcategoria() { return nombreSubcategoria; }
    public void setNombreSubcategoria(String nombreSubcategoria) { this.nombreSubcategoria = nombreSubcategoria; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Set<Ticket> getTickets() { return tickets; }
    public void setTickets(Set<Ticket> tickets) { this.tickets = tickets; }

    // Getter/Setter Prioridad
    public Prioridad getPrioridadDefecto() { return prioridadDefecto; }
    public void setPrioridadDefecto(Prioridad prioridadDefecto) { this.prioridadDefecto = prioridadDefecto; }
}