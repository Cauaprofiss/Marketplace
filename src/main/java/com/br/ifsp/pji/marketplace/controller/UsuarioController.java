package com.br.ifsp.pji.marketplace.controller;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.br.ifsp.pji.marketplace.Service.NotificacaoService;
import com.br.ifsp.pji.marketplace.Service.AvaliacaoService;
import com.br.ifsp.pji.marketplace.Service.UsuarioService;
import com.br.ifsp.pji.marketplace.Service.ProdutoService;
import com.br.ifsp.pji.marketplace.model.Categoria;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.TipoUsuario;
import com.br.ifsp.pji.marketplace.model.Turma;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.TurmaRepository;
import com.br.ifsp.pji.marketplace.repository.FavoritoRepository;
import jakarta.servlet.http.HttpSession;

// Controla cadastro, login, sessao, pagina inicial e edicao do perfil dos usuarios.
@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TurmaRepository turmaRepository;
    private final ProdutoService produtoService;
    private final NotificacaoService notificacaoService;
    private final FavoritoRepository favoritoRepository;
    private final AvaliacaoService avaliacaoService;

    public UsuarioController(
            UsuarioService usuarioService,
            TurmaRepository turmaRepository,
            ProdutoService produtoService,
            NotificacaoService notificacaoService,
            FavoritoRepository favoritoRepository,
            AvaliacaoService avaliacaoService) {
        this.usuarioService = usuarioService;
        this.turmaRepository = turmaRepository;
        this.produtoService = produtoService;
        this.notificacaoService = notificacaoService;
        this.favoritoRepository = favoritoRepository;
        this.avaliacaoService = avaliacaoService;
    }

    /*
     * Turma "ADM" é reservada para o admin criado pelo ContaAdminConfig.
     * Ela nunca deve aparecer nos formularios de cadastro/edicao de usuarios comuns.
     */
    private List<Turma> turmasParaCadastro() {
        return turmaRepository.findAll()
                .stream()
                .filter(t -> !"ADM".equalsIgnoreCase(t.getNomeTurma()))
                .toList();
    }
    
    @GetMapping("/")
    public String inicio() {
    	return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("turmas", turmasParaCadastro());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(Usuario usuario, String confSenha, Model model) {
        try {
            usuarioService.cadastrar(usuario, confSenha);
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("erroEmail", e.getMessage());
            model.addAttribute("turmas", turmasParaCadastro());
            return "cadastro";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String fazerLogin(@RequestParam String email,
                              @RequestParam String senha,
                              HttpSession session,
                              Model model) {
        try {
            Usuario usuario = usuarioService.login(email, senha);
            if (usuario != null) {
                session.setAttribute("usuarioLogado", usuario);

                if (usuario.getTipoUsuario() == TipoUsuario.admin) {
                    return "redirect:/admin/dashboard";
                }

                return "redirect:/home";
            }
            model.addAttribute("erro", "E-mail ou senha incorretos.");
            model.addAttribute("email", email);
            return "login";

        } catch (RuntimeException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("email", email);
            return "login";
        }
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);

        List<Produto> produtosHome = produtoService.listarProdutosVisiveis()
                .stream()
                .limit(10)
                .toList();

        List<Produto> produtosMini = produtosHome
                .stream()
                .limit(6)
                .toList();

        List<Categoria> categoriasHome = produtosHome
                .stream()
                .map(Produto::getCategoria)
                .filter(categoria -> categoria != null)
                .distinct()
                .toList();

        model.addAttribute("produtosHome", produtosHome);
        model.addAttribute("produtosMini", produtosMini);
        model.addAttribute("categoriasHome", categoriasHome);
        model.addAttribute("mediasAvaliacoesProduto", avaliacaoService.calcularMediasDosProdutos(produtosHome));
        List<Produto> anunciosDestaque = produtosHome.stream().limit(5).toList();
        Map<Long, List<String>> fotosAnunciosDestaque = new LinkedHashMap<>();
        anunciosDestaque.forEach(anuncio -> fotosAnunciosDestaque.put(
                anuncio.getIdProduto(),
                produtoService.listarImagensDoProduto(anuncio)
        ));

        model.addAttribute("anunciosDestaque", anunciosDestaque);
        model.addAttribute("fotosAnunciosDestaque", fotosAnunciosDestaque);
        model.addAttribute("favoritosIds", favoritoRepository
                .findByUsuarioIdUsuarioOrderByProdutoDataPublicacaoDesc(usuario.getIdUsuario())
                .stream().map(favorito -> favorito.getProduto().getIdProduto())
                .collect(java.util.stream.Collectors.toSet()));

        return "home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ===================== PERFIL =====================

    @GetMapping("/perfil")
    public String verPerfil(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "usuario",
                usuarioService.buscarPorId(usuario.getIdUsuario())
        );

        model.addAttribute("turmas", turmasParaCadastro());

        return "perfil";
    }

    @GetMapping("/perfil/editar")
    public String editarCadastro(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "usuario",
                usuarioService.buscarPorId(usuario.getIdUsuario())
        );

        model.addAttribute("turmas", turmasParaCadastro());

        return "editar-perfil";
    }

    @PostMapping("/perfil")
    public String editarPerfil(
            Usuario dadosNovos,
            @RequestParam(name = "turma.idTurma", required = false) Long turmaId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        try {

            if (turmaId != null) {
                Turma turmaSelecionada = turmaRepository.findById(turmaId)
                        .orElseThrow(() -> new RuntimeException("A turma selecionada não foi encontrada."));
                dadosNovos.setTurma(turmaSelecionada);
            }

            Usuario atualizado = usuarioService.editarPerfil(
                    usuarioLogado.getIdUsuario(),
                    dadosNovos
            );

            session.setAttribute("usuarioLogado", atualizado);
            redirectAttributes.addFlashAttribute("sucesso", "Dados atualizados com sucesso!");
            return "redirect:/perfil";

        } catch (RuntimeException e) {

            model.addAttribute("usuario", dadosNovos);
            model.addAttribute("turmas", turmasParaCadastro());
            model.addAttribute("erro", e.getMessage());

            return "perfil";
        }
    }

    @PostMapping("/perfil/senha")
    public String trocarSenha(
            @RequestParam String senhaAtual,
            @RequestParam String novaSenha,
            HttpSession session,
            Model model) {

        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        try {

            Usuario atualizado = usuarioService.trocarSenha(
                    usuarioLogado.getIdUsuario(),
                    senhaAtual,
                    novaSenha
            );

            session.setAttribute("usuarioLogado", atualizado);

            model.addAttribute("usuario", atualizado);
            model.addAttribute("turmas", turmasParaCadastro());

            model.addAttribute(
                    "sucesso",
                    "Senha atualizada com sucesso!"
            );

        } catch (RuntimeException e) {

            model.addAttribute(
                    "usuario",
                    usuarioService.buscarPorId(
                            usuarioLogado.getIdUsuario()
                    )
            );

            model.addAttribute("turmas", turmasParaCadastro());
            model.addAttribute("erro", e.getMessage());
        }

        return "perfil";
    }

    @PostMapping("/perfil/deletar")
    public String deletarPerfil(
            HttpSession session,
            Model model) {

        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        try {

            usuarioService.deletarPerfil(
                    usuarioLogado.getIdUsuario()
            );

            session.invalidate();

            return "redirect:/login";

        } catch (RuntimeException e) {

            model.addAttribute("usuario", usuarioLogado);
            model.addAttribute("turmas", turmasParaCadastro());
            model.addAttribute("erro", e.getMessage());

            return "perfil";
        }
    }
}
