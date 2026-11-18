package com.br.ifsp.pji.marketplace.Service;

import com.br.ifsp.pji.marketplace.Dto.ItensModeracao;
import com.br.ifsp.pji.marketplace.model.Denuncia;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusDenuncia;
import com.br.ifsp.pji.marketplace.model.StatusProduto;
import com.br.ifsp.pji.marketplace.repository.DenunciaRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoRepository;
import com.br.ifsp.pji.marketplace.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
// Aplica consultas e regras de moderacao usadas pelo painel administrativo.
public class AdminService {

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DenunciaRepository denunciaRepository;
    private final NotificacaoService notificacaoService;

    public AdminService(
            ProdutoRepository produtoRepository,
            UsuarioRepository usuarioRepository,
            DenunciaRepository denunciaRepository,
            NotificacaoService notificacaoService) {

        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.denunciaRepository = denunciaRepository;
        this.notificacaoService = notificacaoService;
    }
    
    public List<ItensModeracao> listarItensModeracao() {
        return denunciaRepository.findByStatusOrderByDataDenunciaDesc(StatusDenuncia.pendente)
                .stream()
                .map(d -> new ItensModeracao(
                        "Denúncia",
                        "D-" + d.getIdDenuncia(),
                        d.getDescricao(),
                        calcularPrioridade(d), // sua regra de negócio
                        d.getIdDenuncia()
                ))
                .toList();
    }

    private String calcularPrioridade(Denuncia d) {
        // regra provisória — ajuste conforme sua necessidade
        return "Média";
    }

    // ============================================================
    // LISTAGEM DE ANÚNCIOS (COM BUSCA E FILTRO POR STATUS)
    // ============================================================

    public List<Produto> listarAnuncios(String status, String busca) {

        List<Produto> produtos = produtoRepository.findAll();

        if (busca != null && !busca.isBlank()) {

            String termo = busca.trim().toLowerCase();

            produtos = produtos.stream()
                    .filter(p ->
                            (p.getNome() != null
                                    && p.getNome().toLowerCase().contains(termo))
                            || (p.getCodigoProduto() != null
                                    && p.getCodigoProduto().toLowerCase().contains(termo))
                    )
                    .toList();
        }

        if (status != null && !status.isBlank()) {

            StatusProduto statusFiltro = StatusProduto.valueOf(status);

            produtos = produtos.stream()
                    .filter(p -> p.getStatus() == statusFiltro)
                    .toList();
        }

        return produtos;
    }

    // ============================================================
    // VISÃO GERAL (DASHBOARD)
    // ============================================================

    public long contarUsuariosAtivos() {

        return usuarioRepository.findAll()
                .stream()
                .filter(u -> !u.isBloqueado())
                .count();
    }

    public long contarAnunciosAtivos() {

        return produtoRepository
                .findByStatus(StatusProduto.disponivel)
                .size();
    }

    public long contarDenunciasAbertas() {

        return denunciaRepository
                .findByStatus(StatusDenuncia.pendente)
                .size();
    }

    public List<Produto> anunciosRecentesEmRevisao(int limite) {

        return produtoRepository
                .findByStatus(StatusProduto.em_revisao)
                .stream()
                .sorted((a, b) -> {
                    if (a.getDataPublicacao() == null) return 1;
                    if (b.getDataPublicacao() == null) return -1;
                    return b.getDataPublicacao().compareTo(a.getDataPublicacao());
                })
                .limit(limite)
                .toList();
    }

    public List<Denuncia> denunciasRecentesPendentes(int limite) {

        return denunciaRepository
                .findByStatusOrderByDataDenunciaDesc(StatusDenuncia.pendente)
                .stream()
                .limit(limite)
                .toList();
    }

    public List<Denuncia> listarDenuncias(String status) {
        if (status == null || status.isBlank()) {
            return denunciaRepository.findAll().stream()
                    .sorted((a, b) -> b.getDataDenuncia().compareTo(a.getDataDenuncia()))
                    .toList();
        }
        return denunciaRepository.findByStatusOrderByDataDenunciaDesc(StatusDenuncia.valueOf(status));
    }

    @Transactional
    public void revisarDenuncia(Long idDenuncia, boolean aprovada) {
        Denuncia denuncia = denunciaRepository.findById(idDenuncia)
                .orElseThrow(() -> new IllegalArgumentException("Denúncia não encontrada."));
        if (denuncia.getStatus() != StatusDenuncia.pendente) {
            throw new IllegalStateException("Esta denúncia já foi analisada.");
        }

        if (aprovada) {
            Produto produto = denuncia.getProduto();
            produto.setStatus(StatusProduto.reprovado);
            produtoRepository.save(produto);
            denuncia.setStatus(StatusDenuncia.resolvida);
            if (produto.getUsuario() != null) {
                notificacaoService.notificarAnuncioExcluido(produto.getUsuario(), produto.getNome());
            }
        } else {
            denuncia.setStatus(StatusDenuncia.arquivada);
        }
        denunciaRepository.save(denuncia);
    }
    
    public long contarAnunciosEmAnalise() {
        return produtoRepository.findByStatus(StatusProduto.em_revisao).size();
    }
}
