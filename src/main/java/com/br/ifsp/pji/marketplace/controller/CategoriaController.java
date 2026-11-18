package com.br.ifsp.pji.marketplace.controller;

import com.br.ifsp.pji.marketplace.Service.CategoriaService;
import com.br.ifsp.pji.marketplace.model.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Fornece a lista de categorias consumida pelo formulario de criacao de anuncios.
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Autowired
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // usado no passo 3 (Selecionar categoria) para popular as opcoes
    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }
}