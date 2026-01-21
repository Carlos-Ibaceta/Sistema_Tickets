package com.example.system_tickets.service.impl;

import com.example.system_tickets.entity.*;
import com.example.system_tickets.repository.*;
import com.example.system_tickets.service.DropdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DropdownServiceImpl implements DropdownService {

    @Autowired private CategoriaRepository cr;
    @Autowired private DepartamentoRepository dr;
    @Autowired private PrioridadRepository pr;
    @Autowired private RolRepository rr;
    @Autowired private EstadoTicketRepository er;
    @Autowired private SubcategoriaRepository sr;

    @Override public List<Categoria> listarCategorias() { return cr.findAll(); }
    @Override public List<Departamento> listarDepartamentos() { return dr.findAll(); }
    @Override public List<Prioridad> listarPrioridades() { return pr.findAll(); }
    @Override public List<Rol> listarRoles() { return rr.findAll(); }
    @Override public List<EstadoTicket> listarEstadosTicket() { return er.findAll(); }
    @Override public List<Subcategoria> listarSubcategorias() { return sr.findAll(); }
    @Override public List<Subcategoria> buscarSubcategoriasPorCategoriaId(Long id) { return sr.findByCategoria_Id(id); }

    @Override
    public List<EstadoTicket> listarEstadosCierre() {
        return List.of(
                er.findByNombreEstado("RESUELTO").get(),
                er.findByNombreEstado("CANCELADO").get()
        );
    }
}