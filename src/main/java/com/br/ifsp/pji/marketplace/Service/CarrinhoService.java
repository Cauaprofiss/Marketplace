package com.br.ifsp.pji.marketplace.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.ifsp.pji.marketplace.model.Carrinho;
import com.br.ifsp.pji.marketplace.model.ItemCarrinho;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusProduto;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.CarrinhoRepository;
import com.br.ifsp.pji.marketplace.repository.ItemCarrinhoRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoRepository;

@Service
// Gerencia o carrinho do usuario e valida quantidade e disponibilidade de estoque.
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ProdutoRepository produtoRepository;

    public CarrinhoService(
            CarrinhoRepository carrinhoRepository,
            ItemCarrinhoRepository itemCarrinhoRepository,
            ProdutoRepository produtoRepository) {

        this.carrinhoRepository = carrinhoRepository;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Carrinho buscarOuCriarCarrinho(Usuario usuario) {
        return carrinhoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseGet(() -> {
                    Carrinho novo = new Carrinho();
                    novo.setUsuario(usuario);
                    return carrinhoRepository.save(novo);
                });
    }

    public List<ItemCarrinho> listarItens(Usuario usuario) {
        Carrinho carrinho = buscarOuCriarCarrinho(usuario);
        return itemCarrinhoRepository.findByCarrinhoIdCarrinho(carrinho.getIdCarrinho());
    }

    public int contarItens(Usuario usuario) {
        return listarItens(usuario)
                .stream()
                .mapToInt(ItemCarrinho::getQuantidade)
                .sum();
    }

    /*
     * Adiciona 1 unidade ao carrinho e RESERVA esse estoque:
     * a quantidade disponível do produto é debitada. Quando
     * chega a zero, o produto passa automaticamente para
     * indisponivel, sem precisar de nenhuma ação do admin.
     */
    @Transactional
    public void adicionarProduto(Usuario usuario, Long idProduto) {
        adicionarProduto(usuario, idProduto, 1);
    }

    @Transactional
    public void adicionarProduto(Usuario usuario, Long idProduto, int quantidade) {

        if (quantidade <= 0) {
            throw new RuntimeException("Escolha uma quantidade válida.");
        }

        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() ->
                        new RuntimeException("Produto não encontrado."));

        /*
         * O próprio vendedor não pode comprar o produto que ele
         * mesmo anunciou.
         */
        if (produto.getUsuario() != null
                && produto.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException(
                    "Você não pode adicionar ao carrinho um produto anunciado por você mesmo."
            );
        }

        if (produto.getStatus() != StatusProduto.disponivel) {
            throw new RuntimeException(
                    "Este produto não está disponível para compra."
            );
        }

        if (produto.getQuantidade() == null || produto.getQuantidade() < quantidade) {
            throw new RuntimeException(
                    "Não há estoque suficiente para a quantidade escolhida."
            );
        }

        produto.setQuantidade(produto.getQuantidade() - quantidade);

        if (produto.getQuantidade() == 0) {
            produto.setStatus(StatusProduto.indisponivel);
        }

        produtoRepository.save(produto);

        Carrinho carrinho = buscarOuCriarCarrinho(usuario);

        Optional<ItemCarrinho> existente =
                itemCarrinhoRepository.findByCarrinhoIdCarrinhoAndProdutoIdProduto(
                        carrinho.getIdCarrinho(), idProduto
                );

        if (existente.isPresent()) {
            ItemCarrinho item = existente.get();
            item.setQuantidade(item.getQuantidade() + quantidade);
            itemCarrinhoRepository.save(item);
        } else {
            ItemCarrinho novo = new ItemCarrinho();
            novo.setCarrinho(carrinho);
            novo.setProduto(produto);
            novo.setQuantidade(quantidade);
            itemCarrinhoRepository.save(novo);
        }
    }

    /*
     * Se a quantidade no carrinho aumentar, consome mais estoque
     * (validando se existe o suficiente). Se diminuir, devolve
     * o estoque correspondente e, se o produto estava
     * indisponivel só por falta de estoque, ele volta a
     * disponivel automaticamente.
     */
    @Transactional
    public void atualizarQuantidade(
            Usuario usuario, Long idItemCarrinho, int novaQuantidade) {

        ItemCarrinho item = buscarItemDoUsuario(usuario, idItemCarrinho);
        Produto produto = item.getProduto();

        int diferenca = novaQuantidade - item.getQuantidade();

        if (diferenca > 0) {
            if (produto.getQuantidade() == null || produto.getQuantidade() < diferenca) {
                throw new RuntimeException(
                        "Não há estoque suficiente deste produto."
                );
            }
            produto.setQuantidade(produto.getQuantidade() - diferenca);

            if (produto.getQuantidade() == 0) {
                produto.setStatus(StatusProduto.indisponivel);
            }

        } else if (diferenca < 0) {
            int devolver = Math.abs(diferenca);
            int quantidadeAtual = produto.getQuantidade() == null ? 0 : produto.getQuantidade();

            produto.setQuantidade(quantidadeAtual + devolver);

            if (produto.getStatus() == StatusProduto.indisponivel
                    && produto.getQuantidade() > 0) {
                produto.setStatus(StatusProduto.disponivel);
            }
        }

        produtoRepository.save(produto);

        if (novaQuantidade <= 0) {
            itemCarrinhoRepository.delete(item);
            return;
        }

        item.setQuantidade(novaQuantidade);
        itemCarrinhoRepository.save(item);
    }

    /*
     * Remover o item do carrinho devolve todo o estoque
     * reservado por ele.
     */
    @Transactional
    public void removerItem(Usuario usuario, Long idItemCarrinho) {

        ItemCarrinho item = buscarItemDoUsuario(usuario, idItemCarrinho);
        Produto produto = item.getProduto();

        int quantidadeAtual = produto.getQuantidade() == null ? 0 : produto.getQuantidade();
        produto.setQuantidade(quantidadeAtual + item.getQuantidade());

        if (produto.getStatus() == StatusProduto.indisponivel
                && produto.getQuantidade() > 0) {
            produto.setStatus(StatusProduto.disponivel);
        }

        produtoRepository.save(produto);
        itemCarrinhoRepository.delete(item);
    }

    @Transactional
    public void limparCarrinho(Usuario usuario) {
        List<ItemCarrinho> itens = listarItens(usuario);
        for (ItemCarrinho item : itens) {
            Produto produto = item.getProduto();
            int quantidadeAtual = produto.getQuantidade() == null ? 0 : produto.getQuantidade();
            produto.setQuantidade(quantidadeAtual + item.getQuantidade());
            if (produto.getStatus() == StatusProduto.indisponivel && produto.getQuantidade() > 0) {
                produto.setStatus(StatusProduto.disponivel);
            }
            produtoRepository.save(produto);
        }
        itemCarrinhoRepository.deleteAll(itens);
    }

    private ItemCarrinho buscarItemDoUsuario(
            Usuario usuario, Long idItemCarrinho) {

        ItemCarrinho item = itemCarrinhoRepository.findById(idItemCarrinho)
                .orElseThrow(() ->
                        new RuntimeException("Item não encontrado."));

        Carrinho carrinhoDoUsuario = buscarOuCriarCarrinho(usuario);

        if (!item.getCarrinho().getIdCarrinho().equals(carrinhoDoUsuario.getIdCarrinho())) {
            throw new RuntimeException(
                    "Este item não pertence ao seu carrinho."
            );
        }

        return item;
    }
}
