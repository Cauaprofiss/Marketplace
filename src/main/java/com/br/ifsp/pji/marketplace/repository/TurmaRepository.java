package com.br.ifsp.pji.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.Turma;

// Persistencia das turmas usadas no cadastro e na conta administrativa.
public interface TurmaRepository extends JpaRepository<Turma, Long>{

}
