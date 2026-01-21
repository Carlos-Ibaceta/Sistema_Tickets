package com.example.system_tickets.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- NUEVO CAMPO RUT ---
    @Column(unique = true, length = 12)
    private String rut;

    private String nombre;
    private String email;
    private String password;

    // --- NUEVO CAMPO DE ESTADO (ACTIVO/INACTIVO) ---
    // Usamos Boolean wrapper y por defecto true para compatibilidad
    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "token_expiration")
    private LocalDateTime tokenExpiration;

    // Usamos Boolean (Objeto) en lugar de boolean (primitivo) para evitar errores si la BD tiene NULL
    @Column(name = "kpis_habilitados")
    private Boolean kpisHabilitados = false;

    @Column(name = "crear_usuarios_habilitado")
    private Boolean crearUsuariosHabilitado = false;

    // --- SEGURIDAD NIVEL 2 ---
    @Column(name = "cambio_password_obligatorio")
    private Boolean cambioPasswordObligatorio = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();

    public Usuario() {}

    // GETTERS Y SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // --- GETTER Y SETTER DEL RUT ---
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // --- GETTER Y SETTER DE ESTADO ACTIVO ---
    public boolean isActivo() {
        return activo != null ? activo : true;
    }
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }

    public List<Ticket> getTickets() { return tickets != null ? tickets : new ArrayList<>(); }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getTokenExpiration() { return tokenExpiration; }
    public void setTokenExpiration(LocalDateTime tokenExpiration) { this.tokenExpiration = tokenExpiration; }

    // --- GETTERS BLINDADOS (Si es null, devuelve false) ---

    public boolean isKpisHabilitados() {
        return kpisHabilitados != null ? kpisHabilitados : false;
    }
    public void setKpisHabilitados(Boolean kpisHabilitados) {
        this.kpisHabilitados = kpisHabilitados;
    }

    public boolean isCrearUsuariosHabilitado() {
        return crearUsuariosHabilitado != null ? crearUsuariosHabilitado : false;
    }
    public void setCrearUsuariosHabilitado(Boolean crearUsuariosHabilitado) {
        this.crearUsuariosHabilitado = crearUsuariosHabilitado;
    }

    public boolean isCambioPasswordObligatorio() {
        return cambioPasswordObligatorio != null ? cambioPasswordObligatorio : false;
    }
    public void setCambioPasswordObligatorio(Boolean cambioPasswordObligatorio) {
        this.cambioPasswordObligatorio = cambioPasswordObligatorio;
    }
}