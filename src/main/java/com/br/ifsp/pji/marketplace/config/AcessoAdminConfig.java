package com.br.ifsp.pji.marketplace.config;

import com.br.ifsp.pji.marketplace.model.TipoUsuario;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;

// Intercepta as rotas administrativas e permite acesso somente a usuarios admin autenticados.
public class AcessoAdminConfig implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        Usuario usuario = session != null
                ? (Usuario) session.getAttribute("usuarioLogado")
                : null;

        if (usuario == null) {
            response.sendRedirect("/login");
            return false;
        }

        if (usuario.getTipoUsuario() != TipoUsuario.admin) {
            response.sendRedirect("/home");
            return false;
        }

        return true;
    }
}