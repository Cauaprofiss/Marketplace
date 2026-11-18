package com.br.ifsp.pji.marketplace.config;

import com.br.ifsp.pji.marketplace.model.TipoUsuario;
import com.br.ifsp.pji.marketplace.model.Turma;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.TurmaRepository;
import com.br.ifsp.pji.marketplace.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Cria a conta administrativa padrao na inicializacao quando ainda nao existe um admin.
@Component
@Order(4)
public class ContaAdminConfig implements CommandLineRunner {

    private static final String EMAIL_ADMIN = "campus.marketplacepji@gmail.com";
    private static final String SENHA_ADMIN = "IFSP@PJICAM123";

    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaAdminConfig(
            UsuarioRepository usuarioRepository,
            TurmaRepository turmaRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        boolean existeAdmin = usuarioRepository
                .findAll()
                .stream()
                .anyMatch(u -> u.getTipoUsuario() == TipoUsuario.admin);

        if (existeAdmin) {
            return;
        }

        // Cria a turma ADM caso ela ainda não exista
        Turma turmaAdmin = turmaRepository
                .findAll()
                .stream()
                .filter(t -> "ADM".equalsIgnoreCase(t.getNomeTurma()))
                .findFirst()
                .orElseGet(() -> {

                    Turma nova = new Turma();

                    nova.setNomeTurma("ADM");
                    nova.setDescricao("Administrador Central do Sistema");

                    return turmaRepository.save(nova);
                });

        // Cria o administrador
        Usuario admin = new Usuario();

        admin.setNome("Administrador");
        admin.setSobrenome("Sistema");
        admin.setEmail(EMAIL_ADMIN);
        admin.setSenha(passwordEncoder.encode(SENHA_ADMIN));
        admin.setTurma(turmaAdmin);
        admin.setTipoUsuario(TipoUsuario.admin);
        admin.setDataCadastro(LocalDateTime.now());
        admin.setBloqueado(false);

        usuarioRepository.save(admin);

        System.out.println(
                "Admin padrao criado -> email: " + EMAIL_ADMIN
                        + " | senha: " + SENHA_ADMIN
        );
    }
}