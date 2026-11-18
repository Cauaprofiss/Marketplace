package com.br.ifsp.pji.marketplace.Service;

import com.br.ifsp.pji.marketplace.model.Categoria;
import com.br.ifsp.pji.marketplace.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
// Encapsula a consulta e manutencao das categorias disponiveis para anuncios.
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Autowired
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }
}