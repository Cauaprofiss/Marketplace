package com.br.ifsp.pji.marketplace.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.br.ifsp.pji.marketplace.model.Turma;
import com.br.ifsp.pji.marketplace.repository.TurmaRepository;
import com.br.ifsp.pji.marketplace.model.Categoria;
import com.br.ifsp.pji.marketplace.model.Disponibilidade;
import com.br.ifsp.pji.marketplace.model.DisponibilidadeRetirada;
import com.br.ifsp.pji.marketplace.model.LocalRetirada;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusProduto;
import com.br.ifsp.pji.marketplace.model.TipoUsuario;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.CategoriaRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoRepository;
import com.br.ifsp.pji.marketplace.repository.UsuarioRepository;

// Popula dados basicos, como turmas, categorias e registros iniciais do marketplace.
@Configuration
public class DataInitializer {

	@Bean
	@Order(1)
	CommandLineRunner iniciarDados(TurmaRepository turmaRepository) {
	    return args -> {

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "INI1",
	                "Informática para Internet, primeiro ano"
	        );

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "INI2",
	                "Informática para Internet, segundo ano"
	        );

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "INI3",
	                "Informática para Internet, terceiro ano"
	        );

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "INI4",
	                "Informática para Internet, quarto ano"
	        );

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "PAV1",
	                "Produção de Áudio e Vídeo, primeiro ano"
	        );

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "PAV2",
	                "Produção de Áudio e Vídeo, segundo ano"
	        );

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "PAV3",
	                "Produção de Áudio e Vídeo, terceiro ano"
	        );

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "PAV4",
	                "Produção de Áudio e Vídeo, quarto ano"
	        );

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "DEG1",
	                "Design Gráfico, primeiro ano"
	        );

	        adicionarTurmaSeNaoExistir(
	                turmaRepository,
	                "DEG2",
	                "Design Gráfico, segundo ano"
	        );
	    };
	}

	private void adicionarTurmaSeNaoExistir(
	        TurmaRepository turmaRepository,
	        String nome,
	        String descricao) {

	    boolean existe = turmaRepository
	            .findAll()
	            .stream()
	            .anyMatch(t -> nome.equalsIgnoreCase(t.getNomeTurma()));

	    if (!existe) {

	        Turma turma = new Turma();

	        turma.setNomeTurma(nome);
	        turma.setDescricao(descricao);

	        turmaRepository.save(turma);
	    }
	}

    // ---------- NOVO: categorias do marketplace ----------
    @Bean
    @Order(2)
    CommandLineRunner iniciarCategorias(CategoriaRepository categoriaRepository) {
        return args -> {
            adicionarCategoriaSeNaoExistir(categoriaRepository, "Doces e alimentos");
            adicionarCategoriaSeNaoExistir(categoriaRepository, "Materiais escolares");
            adicionarCategoriaSeNaoExistir(categoriaRepository, "Artesanato");
            adicionarCategoriaSeNaoExistir(categoriaRepository, "Eletrônicos");
            adicionarCategoriaSeNaoExistir(categoriaRepository, "Roupas e acessórios");
            adicionarCategoriaSeNaoExistir(categoriaRepository, "Livros e apostilas");
            adicionarCategoriaSeNaoExistir(categoriaRepository, "Bebidas");
            adicionarCategoriaSeNaoExistir(categoriaRepository, "Outros");
        };
    }

    private void adicionarCategoriaSeNaoExistir(
            CategoriaRepository categoriaRepository,
            String nome) {

        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            return;
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoriaRepository.save(categoria);
    }

    @Bean
    @Order(3)
    CommandLineRunner iniciarVendedorDemonstracao(
            UsuarioRepository usuarioRepository,
            ProdutoRepository produtoRepository,
            TurmaRepository turmaRepository,
            CategoriaRepository categoriaRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            produtoRepository.findByStatus(StatusProduto.em_revisao).forEach(produto -> {
                produto.setStatus(StatusProduto.disponivel);
                produtoRepository.save(produto);
            });
            final String email = "vendedor.teste@gmail.com";
            Usuario vendedor = usuarioRepository.findByEmail(email).orElseGet(() -> {
                Usuario novo = new Usuario();
                novo.setNome("Ana");
                novo.setSobrenome("Souza");
                novo.setEmail(email);
                novo.setSenha(passwordEncoder.encode("Teste123"));
                novo.setDataCadastro(LocalDateTime.now());
                novo.setTipoUsuario(TipoUsuario.usuario);
                novo.setBloqueado(false);
                novo.setDisponibilidade(Disponibilidade.intervalo);
                novo.setDescricao("Vende doces e produtos para apoiar a formatura");
                novo.setTurma(turmaRepository.findAll().stream()
                        .filter(t -> "INI3".equalsIgnoreCase(t.getNomeTurma()))
                        .findFirst().orElseThrow());
                return usuarioRepository.save(novo);
            });

            Categoria alimentos = categoriaRepository.findAll().stream()
                    .filter(c -> "Doces e alimentos".equalsIgnoreCase(c.getNome())).findFirst().orElseThrow();
            Categoria eletronicos = categoriaRepository.findAll().stream()
                    .filter(c -> "Eletrônicos".equalsIgnoreCase(c.getNome())).findFirst().orElseThrow();
            Categoria materiais = categoriaRepository.findAll().stream()
                    .filter(c -> "Materiais escolares".equalsIgnoreCase(c.getNome())).findFirst().orElseThrow();

            criarProdutoDemonstracao(produtoRepository, vendedor, alimentos, "DEMO-001",
                    "Caixa de mini donuts", "Dois mini donuts de chocolate com granulado colorido, preparados no dia e embalados para retirada no campus.",
                    "18.00", 12, "/Assets/Imagens/demo-vendedor/donuts-chocolate.png", LocalRetirada.patio_interno, DisponibilidadeRetirada.intervalo_manha, 5);
            criarProdutoDemonstracao(produtoRepository, vendedor, alimentos, "DEMO-002",
                    "Brownie artesanal", "Brownie de chocolate com textura macia por dentro e casquinha crocante. Unidade embalada individualmente.",
                    "8.00", 20, "/Assets/Imagens/demo-vendedor/brownie-brookie.jpg", LocalRetirada.patio_externo, DisponibilidadeRetirada.intervalo_tarde, 4);
            criarProdutoDemonstracao(produtoRepository, vendedor, eletronicos, "DEMO-003",
                    "Fone bluetooth vermelho", "Fone sem fio na cor vermelha, com estojo de carregamento. Produto conservado e funcionando normalmente.",
                    "45.00", 3, "/Assets/Imagens/demo-vendedor/fone-vermelho.webp", LocalRetirada.quadra, DisponibilidadeRetirada.intervalo_tarde, 3);
            criarProdutoDemonstracao(produtoRepository, vendedor, alimentos, "DEMO-004",
                    "Bolo de chocolate", "Fatia generosa de bolo de chocolate com recheio e cobertura cremosa. Produção artesanal para retirada no mesmo dia.",
                    "12.00", 15, "/Assets/Imagens/demo-vendedor/bolo-de-chocolate.jpg", LocalRetirada.patio_interno, DisponibilidadeRetirada.intervalo_manha, 2);
            criarProdutoDemonstracao(produtoRepository, vendedor, materiais, "DEMO-005",
                    "Kit de cadernos coloridos", "Conjunto de cadernos com capas ilustradas, ideais para anotações e organização das matérias.",
                    "25.00", 8, "/Assets/Imagens/demo-vendedor/cadernos.webp", LocalRetirada.patio_externo, DisponibilidadeRetirada.intervalo_tarde, 1);

            System.out.println("Conta de demonstração disponível -> email: " + email
                    + " | senha: Teste123 | anúncios: 5");
        };
    }

    private void criarProdutoDemonstracao(ProdutoRepository repository, Usuario vendedor,
            Categoria categoria, String codigo, String nome, String descricao, String preco,
            int quantidade, String fotoUrl, LocalRetirada local,
            DisponibilidadeRetirada disponibilidade, int minutosAtras) {
        Produto produtoExistente = repository.findByCodigoProduto(codigo).orElse(null);
        if (produtoExistente != null) {
            if (!fotoUrl.equals(produtoExistente.getFotoUrl())) {
                produtoExistente.setFotoUrl(fotoUrl);
                repository.save(produtoExistente);
            }
            return;
        }
        Produto produto = new Produto();
        produto.setCodigoProduto(codigo);
        produto.setChaveProduto("demo-" + codigo.toLowerCase());
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(new BigDecimal(preco));
        produto.setQuantidade(quantidade);
        produto.setFotoUrl(fotoUrl);
        produto.setCategoria(categoria);
        produto.setUsuario(vendedor);
        produto.setLocalRetirada(local);
        produto.setDisponibilidadeRetirada(disponibilidade);
        produto.setQuantidadeDenuncias(0);
        produto.setStatus(StatusProduto.disponivel);
        produto.setDataPublicacao(LocalDateTime.now().minusMinutes(minutosAtras));
        repository.save(produto);
    }
}
