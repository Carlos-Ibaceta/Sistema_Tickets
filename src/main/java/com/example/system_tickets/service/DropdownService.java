package com.example.system_tickets.service;

import com.example.system_tickets.entity.*;
import java.util.List;

public interface DropdownService {
    List<Categoria> listarCategorias();
    List<Departamento> listarDepartamentos();
    List<Prioridad> listarPrioridades();
    List<Rol> listarRoles();
    List<EstadoTicket> listarEstadosTicket();
    List<Subcategoria> listarSubcategorias();
    List<Subcategoria> buscarSubcategoriasPorCategoriaId(Long categoriaId);
    List<EstadoTicket> listarEstadosCierre(); // AGREGADO
}