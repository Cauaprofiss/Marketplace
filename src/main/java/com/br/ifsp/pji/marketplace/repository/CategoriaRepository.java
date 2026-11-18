package com.br.ifsp.pji.marketplace.repository;

import com.br.ifsp.pji.marketplace.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

// Operacoes de persistencia e consulta das categorias dos anuncios.
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
