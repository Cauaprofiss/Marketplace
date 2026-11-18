package com.br.ifsp.pji.marketplace.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.br.ifsp.pji.marketplace.Service.CarrinhoService;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;

// Disponibiliza a quantidade de itens do carrinho em todas as paginas renderizadas.
@ControllerAdvice
public class CarrinhoModelAdvice {

    private final CarrinhoService carrinhoService;

    public CarrinhoModelAdvice(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @ModelAttribute
    public void adicionarQuantidadeCarrinhoAoCabecalho(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return;
        }

        model.addAttribute("quantidadeCarrinho", carrinhoService.contarItens(usuario));
    }
}
