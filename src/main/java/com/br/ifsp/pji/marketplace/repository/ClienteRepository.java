package com.br.ifsp.pji.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

}
