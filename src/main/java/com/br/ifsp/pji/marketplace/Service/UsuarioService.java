package com.br.ifsp.pji.marketplace.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.ifsp.pji.marketplace.model.Compra;
import com.br.ifsp.pji.marketplace.model.CompraVendedor;
import com.br.ifsp.pji.marketplace.model.Conversa;
import com.br.ifsp.pji.marketplace.model.ItemCompra;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusProduto;
import com.br.ifsp.pji.marketplace.model.TipoUsuario;
import com.br.ifsp.pji.marketplace.model.TokenRedefinicaoSenha;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.AgendamentoRepository;
import com.br.ifsp.pji.marketplace.repository.AvaliacaoRepository;
import com.br.ifsp.pji.marketplace.repository.CarrinhoRepository;
import com.br.ifsp.pji.marketplace.repository.CompraRepository;
import com.br.ifsp.pji.marketplace.repository.CompraVendedorRepository;
import com.br.ifsp.pji.marketplace.repository.ConversaRepository;
import com.br.ifsp.pji.marketplace.repository.DenunciaRepository;
import com.br.ifsp.pji.marketplace.repository.FavoritoRepository;
import com.br.ifsp.pji.marketplace.repository.ItemCompraRepository;
import com.br.ifsp.pji.marketplace.repository.MensagemRepository;
import com.br.ifsp.pji.marketplace.repository.NotificacaoRepository;
import com.br.ifsp.pji.marketplace.repository.PagamentoRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoRepository;
import com.br.ifsp.pji.marketplace.repository.TokenRedefinicaoSenhaRepository;
import com.br.ifsp.pji.marketplace.repository.UsuarioRepository;

@Service
// Implementa cadastro, login, perfil, recuperacao de senha e exclusao de usuarios.
public class UsuarioService {

    private static final int TOKEN_VALIDADE_MINUTOS = 30;
    private static final Pattern EMAIL_PERMITIDO = Pattern.compile(
            "^[\\p{L}\\p{N}._%+-]+@(gmail|hotmail|outlook)\\.com$|^[\\p{L}\\p{N}._%+-]+@aluno\\.ifsp\\.edu\\.br$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final TokenRedefinicaoSenhaRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AvaliacaoRepository avaliacaoRepository;
    private final DenunciaRepository denunciaRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final FavoritoRepository favoritoRepository;
    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final CompraVendedorRepository compraVendedorRepository;
    private final ItemCompraRepository itemCompraRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final CompraRepository compraRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ProdutoRepository produtoRepository,
            TokenRedefinicaoSenhaRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            AvaliacaoRepository avaliacaoRepository,
            DenunciaRepository denunciaRepository,
            NotificacaoRepository notificacaoRepository,
            FavoritoRepository favoritoRepository,
            ConversaRepository conversaRepository,
            MensagemRepository mensagemRepository,
            CarrinhoRepository carrinhoRepository,
            CompraVendedorRepository compraVendedorRepository,
            ItemCompraRepository itemCompraRepository,
            AgendamentoRepository agendamentoRepository,
            PagamentoRepository pagamentoRepository,
            CompraRepository compraRepository) {

        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.avaliacaoRepository = avaliacaoRepository;
        this.denunciaRepository = denunciaRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.favoritoRepository = favoritoRepository;
        this.conversaRepository = conversaRepository;
        this.mensagemRepository = mensagemRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.compraVendedorRepository = compraVendedorRepository;
        this.itemCompraRepository = itemCompraRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.compraRepository = compraRepository;
    }

    @Transactional
    public Usuario cadastrar(Usuario usuario, String confSenha) {

        /*
         * Cadastro publico NUNCA cria admin, independente do que
         * vier no formulario/request. O unico admin do sistema
         * e' criado pelo ContaAdminConfig na inicializacao.
         */
        usuario.setTipoUsuario(TipoUsuario.usuario);

        String emailNormalizado = normalizarEmail(usuario.getEmail());

        if (!emailValido(emailNormalizado)) {
            throw new RuntimeException("Use um e-mail válido (ex: @gmail.com ou @aluno.ifsp.edu.br).");
        }

        usuario.setEmail(emailNormalizado);

        Optional<Usuario> existente =
                usuarioRepository.findByEmail(emailNormalizado);

        if (existente.isPresent()) {
            throw new RuntimeException("E-mail já cadastrado.");
        }

        if (confSenha == null ||
                !usuario.getSenha().equals(confSenha)) {

            throw new RuntimeException("As senhas não são iguais.");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setDataCadastro(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String senha) {

        if (email == null || email.isBlank()) {
            return null;
        }

        String emailNormalizado = normalizarEmail(email);

        if (!emailValido(emailNormalizado)) {
            return null;
        }

        Optional<Usuario> resultado =
                usuarioRepository.findByEmail(emailNormalizado);

        if (resultado.isPresent()) {

            Usuario usuario = resultado.get();

            if (usuario.isBloqueado()) {
                throw new RuntimeException(
                        "Esta conta está bloqueada."
                );
            }

            if (passwordEncoder.matches(senha, usuario.getSenha())) {
                return usuario;
            }
        }

        return null;
    }

    public Usuario buscarPorId(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuário não encontrado."
                        ));
    }

    @Transactional
    public Usuario editarPerfil(
            Long id,
            Usuario dadosNovos) {

        Usuario usuario = buscarPorId(id);

        String emailNormalizado = normalizarEmail(dadosNovos.getEmail());

        if (!emailValido(emailNormalizado)) {
            throw new RuntimeException("Use um e-mail válido (ex: @gmail.com ou @aluno.ifsp.edu.br).");
        }

        if (!usuario.getEmail()
                .equalsIgnoreCase(emailNormalizado)) {

            Optional<Usuario> existente =
                    usuarioRepository.findByEmail(emailNormalizado);

            if (existente.isPresent()
                    && !existente.get()
                            .getIdUsuario()
                            .equals(id)) {

                throw new RuntimeException(
                        "E-mail já está em uso por outro usuário."
                );
            }
        }

        usuario.setNome(dadosNovos.getNome());
        usuario.setSobrenome(dadosNovos.getSobrenome());
        usuario.setTelefone(dadosNovos.getTelefone());
        usuario.setEmail(emailNormalizado);
        usuario.setTurma(dadosNovos.getTurma());

        // NOVO: chave Pix e disponibilidade
        usuario.setChavePix(dadosNovos.getChavePix());
        usuario.setDisponibilidade(dadosNovos.getDisponibilidade());
        usuario.setDescricao(dadosNovos.getDescricao());   
        usuario.setImagem(dadosNovos.getImagem());   

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario trocarSenha(
            Long id,
            String senhaAtual,
            String novaSenha) {

        Usuario usuario = buscarPorId(id);

        if (senhaAtual == null ||
                senhaAtual.isBlank()) {

            throw new RuntimeException(
                    "Informe sua senha atual."
            );
        }

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {

            throw new RuntimeException(
                    "Senha atual incorreta."
            );
        }

        if (novaSenha == null ||
                novaSenha.isBlank()) {

            throw new RuntimeException(
                    "Informe a nova senha."
            );
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));

        return usuarioRepository.save(usuario);
    }
    
    @Transactional
    public void deletarPerfil(Long id) {

        Usuario usuario = buscarPorId(id);

        // ============================================================
        // 1) PEDIDOS ONDE ESTE USUÁRIO É VENDEDOR
        //    (remove itens, pagamento, agendamento e o próprio grupo;
        //     se a Compra do comprador ficar sem nenhum grupo, remove-a)
        // ============================================================

        List<CompraVendedor> gruposComoVendedor =
                compraVendedorRepository.findByVendedorIdUsuario(id);

        for (CompraVendedor grupo : gruposComoVendedor) {

            Long idCompra = grupo.getCompra().getIdCompra();

            removerGrupoDeCompra(grupo);

            boolean compraFicouVazia =
                    compraVendedorRepository.findByCompraIdCompra(idCompra).isEmpty();

            if (compraFicouVazia) {
                compraRepository.deleteById(idCompra);
            }
        }

        // ============================================================
        // 2) COMPRAS ONDE ESTE USUÁRIO É COMPRADOR
        //    (remove todos os grupos de cada compra e a compra inteira)
        // ============================================================

        List<Compra> comprasComoComprador =
                compraRepository.findByCompradorIdUsuario(id);

        for (Compra compra : comprasComoComprador) {

            List<CompraVendedor> grupos =
                    compraVendedorRepository.findByCompraIdCompra(compra.getIdCompra());

            for (CompraVendedor grupo : grupos) {
                removerGrupoDeCompra(grupo);
            }

            compraRepository.delete(compra);
        }

        // ============================================================
        // 3) DENÚNCIAS LIGADAS AOS PRODUTOS DESTE USUÁRIO
        //    (precisa remover antes de apagar os produtos, pois
        //     Denuncia.id_produto não tem cascade)
        // ============================================================

        List<Produto> produtosDoUsuario =
                produtoRepository.findByUsuarioIdUsuario(id);

        for (Produto produto : produtosDoUsuario) {
            denunciaRepository.deleteAll(
                    denunciaRepository.findByProdutoIdProduto(produto.getIdProduto())
            );
        }

        // ============================================================
        // 4) PRODUTOS DO USUÁRIO
        //    (produto_imagem, Favorito por produto e item_carrinho por
        //     produto têm ON DELETE CASCADE no banco, então somem sozinhos)
        // ============================================================

        produtoRepository.deleteAll(produtosDoUsuario);

        // ============================================================
        // 5) AVALIAÇÕES RESTANTES (rede de segurança)
        // ============================================================

        avaliacaoRepository.deleteByAvaliadorIdUsuario(id);
        avaliacaoRepository.deleteByAvaliadoIdUsuario(id);

        // ============================================================
        // 6) DENÚNCIAS FEITAS OU RECEBIDAS PELO USUÁRIO
        // ============================================================

        denunciaRepository.deleteAll(
                denunciaRepository.findByDenuncianteIdUsuarioOrUsuarioDenunciadoIdUsuario(id, id)
        );

        // Se o usuário foi moderador de alguma denúncia alheia, apenas
        // desvincula (não apaga a denúncia de outra pessoa)
        denunciaRepository.findByModeradorIdUsuario(id)
                .forEach(denuncia -> {
                    denuncia.setModerador(null);
                    denunciaRepository.save(denuncia);
                });

        // ============================================================
        // 7) NOTIFICAÇÕES
        // ============================================================

        notificacaoRepository.deleteByUsuarioIdUsuario(id);

        // ============================================================
        // 8) FAVORITOS FEITOS PELO USUÁRIO
        // ============================================================

        favoritoRepository.deleteByUsuarioIdUsuario(id);

        // ============================================================
        // 9) CONVERSAS E MENSAGENS
        // ============================================================

        List<Conversa> conversas =
                conversaRepository.findByCompradorIdUsuarioOrVendedorIdUsuario(id, id);

        for (Conversa conversa : conversas) {
            mensagemRepository.deleteByConversaIdConversa(conversa.getIdConversa());
            conversaRepository.delete(conversa);
        }

        mensagemRepository.deleteByRemetenteIdUsuario(id); // rede de segurança

        // ============================================================
        // 10) TOKEN DE REDEFINIÇÃO DE SENHA
        // ============================================================

        tokenRepository.deleteByUsuarioIdUsuario(id);

        // ============================================================
        // 11) CARRINHO (item_carrinho tem cascade a partir do carrinho)
        // ============================================================

        carrinhoRepository.findByUsuarioIdUsuario(id)
                .ifPresent(carrinhoRepository::delete);

        // ============================================================
        // 12) POR FIM, O USUÁRIO
        // ============================================================

        usuarioRepository.delete(usuario);
    }

    private void removerGrupoDeCompra(CompraVendedor grupo) {

        List<ItemCompra> itens =
                itemCompraRepository.findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor());

        for (ItemCompra item : itens) {
            avaliacaoRepository.deleteByItemCompraIdItemCompra(item.getIdItemCompra());
        }

        itemCompraRepository.deleteAll(itens);

        pagamentoRepository.findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor())
                .ifPresent(pagamentoRepository::delete);

        agendamentoRepository.findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor())
                .ifPresent(agendamentoRepository::delete);

        compraVendedorRepository.delete(grupo);
    }

    // ===================== RECUPERACAO DE SENHA =====================

    private static final String CARACTERES_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sem O/0/I/1 (confundem)
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public String solicitarRecuperacaoSenha(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Informe o e-mail.");
        }

        String emailNormalizado = normalizarEmail(email);
        Optional<Usuario> resultado = usuarioRepository.findByEmail(emailNormalizado);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Você não possui cadastro na plataforma.");
        }

        Usuario usuario = resultado.get();
        if (usuario.isBloqueado()) {
            throw new RuntimeException("Esta conta está bloqueada.");
        }

        LocalDateTime agora = LocalDateTime.now();
        String codigo = gerarCodigoRecuperacao();

        TokenRedefinicaoSenha novoToken = new TokenRedefinicaoSenha();
        novoToken.setUsuario(usuario);
        novoToken.setToken(codigo);
        novoToken.setDataCriacao(agora);
        novoToken.setDataTermino(agora.plusMinutes(TOKEN_VALIDADE_MINUTOS));
        tokenRepository.save(novoToken);

        return codigo;
    }

    public void enviarCodigoRecuperacao(String emailDestino, String codigo) {
        try {
            emailService.enviarEmailCodigoRecuperacao(emailDestino, codigo, TOKEN_VALIDADE_MINUTOS);
        } catch (RuntimeException e) {
            System.err.println("Falha ao enviar e-mail de recuperação: " + e.getMessage());
        }
    }

    private String gerarCodigoRecuperacao() {
        StringBuilder bruto = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            bruto.append(CARACTERES_CODIGO.charAt(RANDOM.nextInt(CARACTERES_CODIGO.length())));
        }
        return bruto.substring(0, 4) + "-" + bruto.substring(4, 8) + "-" + bruto.substring(8, 10);
    }

    @Transactional
    public void redefinirSenhaComCodigo(String email, String codigo, String novaSenha, String confirmacaoSenha) {
        TokenRedefinicaoSenha tokenRedefinicao = buscarTokenRecuperacaoValido(email, codigo);

        validarForcaSenha(novaSenha);
        if (!novaSenha.equals(confirmacaoSenha)) {
            throw new RuntimeException("As senhas não coincidem.");
        }

        Usuario usuario = tokenRedefinicao.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        tokenRedefinicao.setDataUsado(LocalDateTime.now());
        tokenRepository.save(tokenRedefinicao);
    }

    @Transactional(readOnly = true)
    public void validarCodigoRecuperacao(String email, String codigo) {
        buscarTokenRecuperacaoValido(email, codigo);
    }

    private TokenRedefinicaoSenha buscarTokenRecuperacaoValido(String email, String codigo) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Informe o e-mail.");
        }
        if (codigo == null || codigo.isBlank()) {
            throw new RuntimeException("Informe o código de recuperação.");
        }

        String emailNormalizado = normalizarEmail(email);
        String codigoNormalizado = codigo.trim().toUpperCase();

        TokenRedefinicaoSenha tokenRedefinicao = tokenRepository.findByToken(codigoNormalizado)
                .orElseThrow(() -> new RuntimeException("Código de recuperação inválido."));

        if (!tokenRedefinicao.getUsuario().getEmail().equalsIgnoreCase(emailNormalizado)) {
            throw new RuntimeException("Código de recuperação inválido.");
        }
        if (tokenRedefinicao.getDataUsado() != null) {
            throw new RuntimeException("Este código já foi utilizado.");
        }
        if (LocalDateTime.now().isAfter(tokenRedefinicao.getDataTermino())) {
            throw new RuntimeException("Este código expirou. Solicite a recuperação novamente.");
        }
        return tokenRedefinicao;
    }

    private void validarForcaSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new RuntimeException("Informe a nova senha.");
        }

        if (senha.length() < 8) {
            throw new RuntimeException("A senha deve ter no mínimo 8 caracteres.");
        }
    }

    static String normalizarEmail(String email) {
        if (email == null) {
            return "";
        }
        return Normalizer.normalize(email.trim(), Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);
    }

    static boolean emailValido(String email) {
        return email != null && EMAIL_PERMITIDO.matcher(email).matches();
    }
}
