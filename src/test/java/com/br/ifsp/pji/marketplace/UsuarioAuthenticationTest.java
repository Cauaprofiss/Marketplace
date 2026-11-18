package com.br.ifsp.pji.marketplace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.text.Normalizer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.br.ifsp.pji.marketplace.Service.EmailService;
import com.br.ifsp.pji.marketplace.Service.UsuarioService;
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

class UsuarioAuthenticationTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final ProdutoRepository produtos = mock(ProdutoRepository.class);
    private final TokenRedefinicaoSenhaRepository tokens = mock(TokenRedefinicaoSenhaRepository.class);
    private final EmailService emails = mock(EmailService.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AvaliacaoRepository avaliacoes = mock(AvaliacaoRepository.class);
    private final DenunciaRepository denuncias = mock(DenunciaRepository.class);
    private final NotificacaoRepository notificacoes = mock(NotificacaoRepository.class);
    private final FavoritoRepository favoritos = mock(FavoritoRepository.class);
    private final ConversaRepository conversas = mock(ConversaRepository.class);
    private final MensagemRepository mensagens = mock(MensagemRepository.class);
    private final CarrinhoRepository carrinhos = mock(CarrinhoRepository.class);
    private final CompraVendedorRepository comprasVendedor = mock(CompraVendedorRepository.class);
    private final ItemCompraRepository itensCompra = mock(ItemCompraRepository.class);
    private final AgendamentoRepository agendamentos = mock(AgendamentoRepository.class);
    private final PagamentoRepository pagamentos = mock(PagamentoRepository.class);
    private final CompraRepository compras = mock(CompraRepository.class);

    private final UsuarioService service = new UsuarioService(
            usuarios, produtos, tokens, emails, passwordEncoder,
            avaliacoes, denuncias, notificacoes, favoritos,
            conversas, mensagens, carrinhos, comprasVendedor,
            itensCompra, agendamentos, pagamentos, compras
    );

    private final AtomicReference<Usuario> salvo = new AtomicReference<>();

    @BeforeEach
    void prepararRepositorio() {
        salvo.set(null);
        when(usuarios.findByEmail(any())).thenAnswer(call -> {
            Usuario atual = salvo.get();
            return atual != null && atual.getEmail().equals(call.getArgument(0))
                    ? Optional.of(atual)
                    : Optional.empty();
        });
        when(usuarios.save(any())).thenAnswer(call -> {
            Usuario usuario = call.getArgument(0);
            salvo.set(usuario);
            return usuario;
        });
    }

    @Test
    void cadastraEAutenticaEmailComCaracterAcentuado() {
        Usuario usuario = new Usuario();
        usuario.setEmail("  Ã@GMAIL.COM ");
        usuario.setSenha("senha-segura");

        service.cadastrar(usuario, "senha-segura");

        assertEquals("ã@gmail.com", salvo.get().getEmail());
        assertSame(usuario, service.login("ã@gmail.com", "senha-segura"));
    }

    @Test
    void trataFormasUnicodeEquivalentesComoOMesmoEmail() {
        Usuario usuario = new Usuario();
        usuario.setEmail(Normalizer.normalize("ã@gmail.com", Normalizer.Form.NFD));
        usuario.setSenha("senha-segura");

        service.cadastrar(usuario, "senha-segura");

        assertTrue(Normalizer.isNormalized(salvo.get().getEmail(), Normalizer.Form.NFC));
        assertSame(usuario, service.login("ã@gmail.com", "senha-segura"));
    }

    @Test
    void rejeitaNoServidorOMesmoFormatoInvalidoRejeitadoNasTelas() {
        Usuario usuario = new Usuario();
        usuario.setEmail("aluno@exemplo.com");
        usuario.setSenha("senha-segura");

        RuntimeException erro = assertThrows(RuntimeException.class,
                () -> service.cadastrar(usuario, "senha-segura"));

        assertTrue(erro.getMessage().contains("e-mail válido"));
        verify(usuarios, never()).save(any());
    }
}