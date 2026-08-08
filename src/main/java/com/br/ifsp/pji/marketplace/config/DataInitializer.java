package com.br.ifsp.pji.marketplace.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.br.ifsp.pji.marketplace.model.Turma;
import com.br.ifsp.pji.marketplace.repository.TurmaRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner iniciarDados(TurmaRepository turmaRepository) {

        return args -> {

            if (turmaRepository.count() == 0) {

                Turma turma1 = new Turma();
                turma1.setNomeTurma("INI1");
                turma1.setDescricao("Turma do primeiro ano");
                turmaRepository.save(turma1);

                Turma turma2 = new Turma();
                turma2.setNomeTurma("INI2");
                turma2.setDescricao("Turma do segundo ano");
                turmaRepository.save(turma2);

                Turma turma3 = new Turma();
                turma3.setNomeTurma("INI3");
                turma3.setDescricao("Turma do terceiro ano");
                turmaRepository.save(turma3);
            }
        };
    }
}