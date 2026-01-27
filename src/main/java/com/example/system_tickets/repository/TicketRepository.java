package com.example.system_tickets.repository;

import com.example.system_tickets.entity.Ticket;
import com.example.system_tickets.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    // --- MÉTODOS PARA PAGINACIÓN ---

    // Para Funcionarios: Mis Tickets paginados
    Page<Ticket> findByUsuario(Usuario usuario, Pageable pageable);

    // Para Soporte: Mis Tickets Asignados paginados
    Page<Ticket> findByTecnicoAsignado(Usuario tecnico, Pageable pageable);

    // Para Soporte: Bolsa de Tickets paginada
    Page<Ticket> findByTecnicoAsignadoIsNull(Pageable pageable);

    // Para Soporte: Historial antiguo
    Page<Ticket> findByTecnicoAsignadoAndEstadoTicket_NombreEstadoIn(Usuario tecnico, Collection<String> estados, Pageable pageable);

    // --- NUEVO MÉTODO BLINDADO PARA HISTORIAL (SOLUCIÓN) ---
    // Busca tickets donde el ID del técnico sea EXACTAMENTE el tuyo
    @Query("SELECT t FROM Ticket t WHERE t.tecnicoAsignado.id = :tecnicoId AND t.estadoTicket.nombreEstado IN :estados")
    Page<Ticket> findHistorialExactoPorTecnico(@Param("tecnicoId") Long tecnicoId,
                                               @Param("estados") List<String> estados,
                                               Pageable pageable);

    // --- MÉTODOS ANTIGUOS (Mantenidos) ---
    List<Ticket> findByUsuario_Id(Long usuarioId);
    List<Ticket> findByDepartamento_Id(Long departamentoId);
    List<Ticket> findByEstadoTicket_NombreEstado(String nombreEstado);
    List<Ticket> findByUsuario(Usuario usuario);
    List<Ticket> findByTecnicoAsignado(Usuario tecnico);
    List<Ticket> findByTecnicoAsignadoIsNull();
    List<Ticket> findAllByOrderByFechaCreacionDesc();
    List<Ticket> findAllByOrderByFechaCreacionAsc();

    // --- NUEVO: ESTADÍSTICAS POR CATEGORÍA ---
    // Esta es la consulta que faltaba para arreglar el gráfico
    @Query("SELECT c.nombreCategoria, COUNT(t) FROM Ticket t JOIN t.categoria c GROUP BY c.nombreCategoria")
    List<Object[]> contarTicketsPorCategoria();
}