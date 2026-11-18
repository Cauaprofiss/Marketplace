package com.br.ifsp.pji.marketplace.repository;

import java.util.List;
import com.br.ifsp.pji.marketplace.model.Avaliacao;
import com.br.ifsp.pji.marketplace.model.TipoAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Acesso a avaliacoes de produtos, vendedores e itens de compra.
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByAvaliadoIdUsuario(Long idUsuario);
    boolean existsByItemCompraIdItemCompra(Long idItemCompra);
    List<Avaliacao> findByItemCompra_Produto_IdProdutoOrderByDataAvaliacaoDesc(Long idProduto);

    boolean existsByItemCompraIdItemCompraAndTipo(Long idItemCompra, TipoAvaliacao tipo);
    boolean existsByCompraVendedorIdCompraVendedorAndTipo(Long idCompraVendedor, TipoAvaliacao tipo);

    @Query("select a from Avaliacao a where a.itemCompra.produto.idProduto = :idProduto "
            + "and (a.tipo = :tipo or a.tipo is null) order by a.dataAvaliacao desc")
    List<Avaliacao> listarAvaliacoesProduto(
            @Param("idProduto") Long idProduto,
            @Param("tipo") TipoAvaliacao tipo);

    @Query("select (count(a) > 0) from Avaliacao a where a.itemCompra.idItemCompra = :idItemCompra "
            + "and (a.tipo = :tipo or a.tipo is null)")
    boolean existeAvaliacaoProdutoOuLegada(
            @Param("idItemCompra") Long idItemCompra,
            @Param("tipo") TipoAvaliacao tipo);

    List<Avaliacao> findByAvaliadoIdUsuarioAndTipoOrderByDataAvaliacaoDesc(
            Long idUsuario,
            TipoAvaliacao tipo);
    
    void deleteByItemCompraIdItemCompra(Long idItemCompra);
    void deleteByAvaliadorIdUsuario(Long idUsuario);
    void deleteByAvaliadoIdUsuario(Long idUsuario);
}
