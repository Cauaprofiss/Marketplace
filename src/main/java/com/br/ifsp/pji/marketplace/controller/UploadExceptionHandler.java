package com.br.ifsp.pji.marketplace.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Converte falhas de upload em mensagens compreensiveis para as telas de anuncios.
@ControllerAdvice(assignableTypes = AnuncioController.class)
public class UploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tratarImagemMuitoGrande(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "erro",
                "Cada imagem deve ter no máximo 8 MB. Escolha uma imagem menor e tente novamente."
        );

        String idProduto = null;
        String queryString = request.getQueryString();

        if (queryString != null) {
            for (String parametro : queryString.split("&")) {
                if (parametro.matches("idProduto=\\d+")) {
                    idProduto = parametro.substring("idProduto=".length());
                    break;
                }
            }
        }

        if (idProduto != null && idProduto.matches("\\d+")) {
            return "redirect:/anuncios/novo/fotos?idProduto=" + idProduto;
        }

        return "redirect:/anuncios/novo";
    }
}
