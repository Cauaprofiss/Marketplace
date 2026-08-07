package com.br.ifsp.pji.marketplace.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
    private UsuarioRepository repository;

    public Usuario cadastrar(Usuario usuario){

        if(repository.findByEmail(usuario.getEmail()).isPresent()){
            throw new RuntimeException("Email já cadastrado");
        }

        return repository.save(usuario);
    }

    public Usuario buscarPorId(Long id){

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

    }
}
