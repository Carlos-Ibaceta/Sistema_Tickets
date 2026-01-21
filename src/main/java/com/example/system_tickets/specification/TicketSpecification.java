package com.example.system_tickets.specification;

import com.example.system_tickets.entity.Ticket;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> filtrar(Long departamentoId, String estado, String prioridad, Long usuarioId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (departamentoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("departamento").get("id"), departamentoId));
            }
            if (estado != null && !estado.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("estadoTicket").get("nombreEstado"), estado));
            }
            if (prioridad != null && !prioridad.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("prioridad").get("nivelPrioridad"), prioridad));
            }
            if (usuarioId != null) {
                predicates.add(criteriaBuilder.equal(root.get("usuario").get("id"), usuarioId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}