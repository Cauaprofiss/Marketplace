package com.br.ifsp.pji.marketplace.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.br.ifsp.pji.marketplace.model.Turma;
import com.br.ifsp.pji.marketplace.repository.TurmaRepository;

@Service
public class TurmaService {

    @Autowired
    private TurmaRepository repository;

    public List<Turma> listarTodas() {
        return repository.findAll();
    }

    public Turma buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
    }

    public Turma cadastrar(Turma turma) {
        return repository.save(turma);
    }
}