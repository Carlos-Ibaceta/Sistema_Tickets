package com.example.system_tickets.entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_incidente")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaIncidente;

    // --- RELOJ SLA ---
    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion; // Cuando el técnico lo toma

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;     // Cuando se resuelve

    @Column(name = "evidencia")
    private String evidencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prioridad_id")
    private Prioridad prioridad;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_ticket_id", nullable = false)
    private EstadoTicket estadoTicket;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tecnico_asignado_id")
    private Usuario tecnicoAsignado;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditoriaTicket> auditorias;

    public Ticket() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // --- 1. TIEMPO DE RESOLUCIÓN (Desde Asignación hasta Cierre/Ahora) ---
    public String getTiempoTranscurrido() {
        if (this.fechaAsignacion == null) {
            return "Esperando técnico...";
        }
        LocalDateTime fin = (this.fechaCierre != null) ? this.fechaCierre : LocalDateTime.now();
        return formatearDuracion(Duration.between(this.fechaAsignacion, fin));
    }

    // --- 2. TIEMPO DE ESPERA TOTAL (Desde Creación hasta Ahora/Cierre) ---
    public String getTiempoDesdeCreacion() {
        if (this.fechaCreacion == null) return "-";

        LocalDateTime fin = (this.fechaCierre != null) ? this.fechaCierre : LocalDateTime.now();
        return formatearDuracion(Duration.between(this.fechaCreacion, fin));
    }

    // --- MÉTODO MATEMÁTICO PARA TEXTO BONITO ---
    private String formatearDuracion(Duration duracion) {
        long dias = duracion.toDays();
        long horas = duracion.toHours() % 24;
        long minutos = duracion.toMinutes() % 60;

        if (dias > 0) {
            return String.format("%d días, %d hrs", dias, horas);
        } else if (horas > 0) {
            return String.format("%d hrs, %d min", horas, minutos);
        } else {
            return String.format("%d min", minutos);
        }
    }

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaIncidente() { return fechaIncidente; }
    public void setFechaIncidente(LocalDateTime fechaIncidente) { this.fechaIncidente = fechaIncidente; }
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public String getEvidencia() { return evidencia; }
    public void setEvidencia(String evidencia) { this.evidencia = evidencia; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Subcategoria getSubcategoria() { return subcategoria; }
    public void setSubcategoria(Subcategoria subcategoria) { this.subcategoria = subcategoria; }
    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }
    public EstadoTicket getEstadoTicket() { return estadoTicket; }
    public void setEstadoTicket(EstadoTicket estadoTicket) { this.estadoTicket = estadoTicket; }
    public Usuario getTecnicoAsignado() { return tecnicoAsignado; }
    public void setTecnicoAsignado(Usuario tecnicoAsignado) { this.tecnicoAsignado = tecnicoAsignado; }
    public List<AuditoriaTicket> getAuditorias() { return auditorias; }
    public void setAuditorias(List<AuditoriaTicket> auditorias) { this.auditorias = auditorias; }

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}