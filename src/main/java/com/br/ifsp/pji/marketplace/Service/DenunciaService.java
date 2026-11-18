package com.br.ifsp.pji.marketplace.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.ifsp.pji.marketplace.Dto.DenunciaRequest;
import com.br.ifsp.pji.marketplace.model.Denuncia;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusDenuncia;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.DenunciaRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoRepository;

@Service
// Registra denuncias, impede duplicidades e fornece dados para a moderacao.
public class DenunciaService {

    private static final Map<String, String> MOTIVOS = Map.of(
            "PRODUTO_PROIBIDO", "Produto proibido ou perigoso",
            "INFORMACOES_ENGANOSAS", "Informações enganosas",
            "PRECO_ABUSIVO", "Preço abusivo",
            "IMAGEM_INADEQUADA", "Imagem inadequada",
            "OUTRO", "Outro motivo"
    );

    private final DenunciaRepository denunciaRepository;
    private final ProdutoRepository produtoRepository;
    private final NotificacaoService notificacaoService;

    public DenunciaService(
            DenunciaRepository denunciaRepository,
            ProdutoRepository produtoRepository,
            NotificacaoService notificacaoService) {

        this.denunciaRepository = denunciaRepository;
        this.produtoRepository = produtoRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public Denuncia registrar(DenunciaRequest req, Usuario denunciante) {

        if (req.getIdProduto() == null) {
            throw new RuntimeException("Produto não informado.");
        }

        if (req.getMotivo() == null || req.getMotivo().isBlank()) {
            throw new RuntimeException("Selecione o motivo da denúncia.");
        }

        if (denunciante == null) {
            throw new RuntimeException("Você precisa estar logado para denunciar um anúncio.");
        }

        Produto produto = produtoRepository.findById(req.getIdProduto())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        if (produto.getUsuario() != null
                && produto.getUsuario().getIdUsuario().equals(denunciante.getIdUsuario())) {
            throw new RuntimeException("Você não pode denunciar o seu próprio anúncio.");
        }

        /*
         * Regra: um mesmo usuário não pode denunciar o mesmo
         * produto mais de uma vez, mesmo que a denúncia anterior
         * já tenha sido resolvida ou arquivada.
         */
        boolean jaDenunciou = denunciaRepository.existsByDenuncianteIdUsuarioAndProdutoIdProduto(
                denunciante.getIdUsuario(), produto.getIdProduto()
        );

        if (jaDenunciou) {
            throw new RuntimeException("Você já denunciou este anúncio anteriormente.");
        }

        String motivoLegivel = MOTIVOS.getOrDefault(req.getMotivo(), req.getMotivo());

        Denuncia denuncia = new Denuncia();
        denuncia.setDenunciante(denunciante);
        denuncia.setProduto(produto);
        denuncia.setUsuarioDenunciado(produto.getUsuario());
        denuncia.setMotivo(motivoLegivel);
        denuncia.setDescricao(req.getDetalhes());
        denuncia.setStatus(StatusDenuncia.pendente);
        denuncia.setDataDenuncia(LocalDateTime.now());

        int atual = produto.getQuantidadeDenuncias() != null ? produto.getQuantidadeDenuncias() : 0;
        produto.setQuantidadeDenuncias(atual + 1);
        produtoRepository.save(produto);

        Denuncia salva = denunciaRepository.save(denuncia);
        if (produto.getUsuario() != null) {
            notificacaoService.notificarDenunciaRecebida(produto.getUsuario(), produto.getNome());
        }
        return salva;
    }

    public List<Denuncia> listarPendentes() {
        return denunciaRepository.findByStatusOrderByDataDenunciaDesc(StatusDenuncia.pendente);
    }

    public Optional<Denuncia> buscarPorId(Long id) {
        return denunciaRepository.findById(id);
    }
}
