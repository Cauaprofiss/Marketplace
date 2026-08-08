package com.br.ifsp.pji.marketplace.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrar(Usuario usuario) {

        usuario.setDataCadastro(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String senha) {

    	Optional<Usuario> resultado = usuarioRepository.findByEmail(email);

    	if (resultado.isPresent()) {
    	    Usuario usuario = resultado.get();

    	    if (usuario.getSenha().equals(senha)) {
    	        return usuario;
    	    }
    	}

        return null;
    }
}