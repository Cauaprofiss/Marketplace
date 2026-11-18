package com.br.ifsp.pji.marketplace.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.Agendamento;
import com.br.ifsp.pji.marketplace.model.StatusAgendamento;

// Consultas persistentes de agendamentos por pedido, comprador, vendedor e horario.
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    Optional<Agendamento> findByCompraVendedorIdCompraVendedor(Long idCompraVendedor);

    List<Agendamento> findByCompradorIdUsuarioOrderByDataAgendadaAscHorarioAsc(Long idUsuario);

    List<Agendamento> findByVendedorIdUsuarioOrderByDataAgendadaAscHorarioAsc(Long idUsuario);

    List<Agendamento> findByVendedorIdUsuarioAndDataAgendadaAndHorarioAndStatusIn(
            Long idVendedor,
            LocalDate data,
            LocalTime horario,
            Collection<StatusAgendamento> status);
}
