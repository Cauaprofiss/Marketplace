package com.br.ifsp.pji.marketplace.controller;

import com.br.ifsp.pji.marketplace.Dto.AnuncioWizardDTO;
import com.br.ifsp.pji.marketplace.Dto.Passo1InicioRequest;
import com.br.ifsp.pji.marketplace.Dto.Passo3CategoriaRequest;
import com.br.ifsp.pji.marketplace.Dto.Passo4DescricaoRequest;
import com.br.ifsp.pji.marketplace.Dto.Passo4NomeRequest;
import com.br.ifsp.pji.marketplace.Dto.Passo5ValorRequest;
import com.br.ifsp.pji.marketplace.Service.AnuncioService;
import com.br.ifsp.pji.marketplace.Service.CategoriaService;
import com.br.ifsp.pji.marketplace.model.Categoria;
import com.br.ifsp.pji.marketplace.model.LocalRetirada;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

// Conduz o wizard de criacao de anuncios, incluindo fotos, dados, valor e publicacao.
@Controller
@RequestMapping("/anuncios/novo")
public class AnuncioController {

    // Categorias exibidas no passo 3, na ordem do Figma.
    private static final List<String> CATEGORIAS_EXIBIDAS = List.of(
            "Doces e alimentos",
            "Artesanato",
            "Eletrônicos",
            "Livros e apostilas",
            "Bebidas",
            "Outros"
    );

    private final AnuncioService anuncioService;
    private final CategoriaService categoriaService;

    @Autowired
    public AnuncioController(
            AnuncioService anuncioService,
            CategoriaService categoriaService) {

        this.anuncioService = anuncioService;
        this.categoriaService = categoriaService;
    }


    // ============================================================
    // PASSO 1
    // ============================================================

    @GetMapping
    public String exibirPasso1(
            @RequestParam(value = "voltar", required = false, defaultValue = "false") boolean voltar,
            HttpSession session,
            Model model) {

        Usuario usuario = obterUsuarioLogado(session);
        Produto rascunho = anuncioService.buscarRascunhoDoVendedor(
                usuario.getIdUsuario()
        );

        if (!model.containsAttribute("passo1")) {
            Passo1InicioRequest passo1 = new Passo1InicioRequest();

            if (rascunho != null && voltar) {
                passo1.setNome(rascunho.getNome());
                passo1.setTipo("PRODUTO");
            }

            model.addAttribute("passo1", passo1);
        }

        return "01-anuncio-novo";
    }
    
    @PostMapping
    public String processarPasso1(
            @Valid @ModelAttribute("passo1")
            Passo1InicioRequest req,

            BindingResult bindingResult,

            HttpSession session,

            Model model) {

        if (bindingResult.hasErrors()) {
            return "01-anuncio-novo";
        }

        try {

            AnuncioWizardDTO anuncio =
                    anuncioService.iniciar(
                            session,
                            req
                    );

            return "redirect:/anuncios/novo/fotos?idProduto="
                    + anuncio.getIdProduto();

        } catch (IllegalArgumentException |
                 IllegalStateException ex) {

            model.addAttribute(
                    "erro",
                    ex.getMessage()
            );

            return "01-anuncio-novo";
        }
    }

    // ============================================================
    // PASSO 2 - FOTOS
    // ============================================================

    @GetMapping("/fotos")
    public String exibirPasso2(
            @RequestParam Long idProduto,
            HttpSession session,
            Model model) {

        try {

            verificarDono(idProduto, session);

            AnuncioWizardDTO anuncio =
                    anuncioService.buscarPorId(idProduto);

            model.addAttribute(
                    "anuncio",
                    anuncio
            );

            List<String> fotos =
                    anuncio.getUrlsFotos();

            List<String> extras =
                    fotos.size() > 1
                            ? fotos.subList(
                                    1,
                                    Math.min(fotos.size(), 4)
                            )
                            : List.of();

            model.addAttribute(
                    "extras",
                    extras
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            return "02-anuncio-fotos";

        } catch (IllegalStateException |
                 IllegalArgumentException ex) {

            return "redirect:/anuncios/novo";
        }
    }

    @PostMapping("/fotos/remover")
    public org.springframework.http.ResponseEntity<Void> removerFoto(
            @RequestParam Long idProduto,
            @RequestParam String urlFoto,
            HttpSession session) {
        try {
            verificarDono(idProduto, session);
            anuncioService.removerFoto(idProduto, urlFoto);
            return org.springframework.http.ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/fotos")
    public String processarPasso2(
            @RequestParam Long idProduto,

            @RequestParam(
                    value = "fotoPrincipal",
                    required = false
            )
            MultipartFile fotoPrincipal,

            @RequestParam(
                    value = "fotosAdicionais",
                    required = false
            )
            List<MultipartFile> fotosAdicionais,

            HttpSession session,

            Model model) {

        try {

            verificarDono(idProduto, session);

            List<MultipartFile> todasFotos =
                    new ArrayList<>();

            if (fotoPrincipal != null &&
                    !fotoPrincipal.isEmpty()) {

                todasFotos.add(fotoPrincipal);
            }

            if (fotosAdicionais != null) {

                fotosAdicionais.stream()
                        .filter(f -> !f.isEmpty())
                        .forEach(
                                todasFotos::add
                        );
            }

            if (!todasFotos.isEmpty()) {

                anuncioService.adicionarFotos(
                        idProduto,
                        todasFotos
                );
            }

            AnuncioWizardDTO anuncio =
                    anuncioService.buscarPorId(
                            idProduto
                    );

            if (anuncio.getUrlsFotos().isEmpty()) {

                model.addAttribute(
                        "erro",
                        "Envie ao menos uma foto do produto."
                );

                model.addAttribute(
                        "anuncio",
                        anuncio
                );

                model.addAttribute(
                        "extras",
                        List.of()
                );

                model.addAttribute(
                        "idProduto",
                        idProduto
                );

                return "02-anuncio-fotos";
            }

            return "redirect:/anuncios/novo/categoria?idProduto="
                    + idProduto;

        } catch (IllegalArgumentException |
                 IllegalStateException ex) {

            AnuncioWizardDTO anuncio =
                    anuncioService.buscarPorId(
                            idProduto
                    );

            model.addAttribute(
                    "erro",
                    ex.getMessage()
            );

            model.addAttribute(
                    "anuncio",
                    anuncio
            );

            model.addAttribute(
                    "extras",
                    anuncio.getUrlsFotos().size() > 1
                            ? anuncio.getUrlsFotos()
                                    .subList(
                                            1,
                                            Math.min(
                                                    anuncio.getUrlsFotos().size(),
                                                    4
                                            )
                                    )
                            : List.of()
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            return "02-anuncio-fotos";
        }
    }

    // ============================================================
    // PASSO 3 - CATEGORIA
    // ============================================================

    @GetMapping("/categoria")
    public String exibirPasso3(
            @RequestParam(required = false) Long idProduto,
            HttpSession session,
            Model model) {

        try {

            idProduto = resolverIdProduto(idProduto, session);

            verificarDono(idProduto, session);

            AnuncioWizardDTO anuncio =
                    anuncioService.buscarPorId(
                            idProduto
                    );

            model.addAttribute(
                    "anuncio",
                    anuncio
            );

            model.addAttribute(
                    "categorias",
                    categoriasVisiveis()
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            return "03-anuncio-categoria";

        } catch (IllegalStateException |
                 IllegalArgumentException ex) {

            return "redirect:/anuncios/novo";
        }
    }

    @PostMapping("/categoria")
    public String processarPasso3(
            @Valid @ModelAttribute("passo3")
            Passo3CategoriaRequest req,

            BindingResult bindingResult,

            @RequestParam Long idProduto,

            HttpSession session,

            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "anuncio",
                    anuncioService.buscarPorId(
                            idProduto
                    )
            );

            model.addAttribute(
                    "categorias",
                    categoriasVisiveis()
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            model.addAttribute(
                    "erro",
                    "Selecione uma categoria."
            );

            return "03-anuncio-categoria";
        }

        try {

            verificarDono(idProduto, session);

            anuncioService.definirCategoria(
                    idProduto,
                    req
            );

            return "redirect:/anuncios/novo/informacoes?idProduto="
                    + idProduto;

        } catch (IllegalArgumentException |
                 IllegalStateException ex) {

            model.addAttribute(
                    "anuncio",
                    anuncioService.buscarPorId(
                            idProduto
                    )
            );

            model.addAttribute(
                    "categorias",
                    categoriasVisiveis()
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            model.addAttribute(
                    "erro",
                    ex.getMessage()
            );

            return "03-anuncio-categoria";
        }
    }

    // ============================================================
    // PASSO 4 - INFORMAÇÕES
    // ============================================================

    @GetMapping("/informacoes")
    public String exibirPasso4(
            @RequestParam Long idProduto,
            HttpSession session,
            Model model) {

        try {

            verificarDono(idProduto, session);

            AnuncioWizardDTO anuncio =
                    anuncioService.buscarPorId(
                            idProduto
                    );

            model.addAttribute(
                    "anuncio",
                    anuncio
            );

            model.addAttribute(
                    "categoriaNome",
                    nomeCategoria(anuncio)
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            return "04-anuncio-informacoes";

        } catch (IllegalStateException |
                 IllegalArgumentException ex) {

            return "redirect:/anuncios/novo";
        }
    }

    @PostMapping("/informacoes")
    public String processarPasso4(
            @RequestParam String nome,
            @RequestParam String descricao,
            @RequestParam Integer quantidade,
            @RequestParam LocalRetirada localRetirada,
            @RequestParam Long idProduto,
            HttpSession session,
            Model model) {

        try {
            verificarDono(idProduto, session);

            Passo4NomeRequest nomeRequest = new Passo4NomeRequest();
            nomeRequest.setNome(nome);
            anuncioService.definirNome(idProduto, nomeRequest);

            Passo4DescricaoRequest detalhesRequest = new Passo4DescricaoRequest();
            detalhesRequest.setDescricao(descricao);
            detalhesRequest.setQuantidade(quantidade);
            detalhesRequest.setLocalRetirada(localRetirada);
            anuncioService.definirDescricaoEDetalhes(idProduto, detalhesRequest);

            return "redirect:/anuncios/novo/valor?idProduto=" + idProduto;

        } catch (IllegalArgumentException | IllegalStateException ex) {
            AnuncioWizardDTO anuncio = anuncioService.buscarPorId(idProduto);
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("anuncio", anuncio);
            model.addAttribute("categoriaNome", nomeCategoria(anuncio));
            model.addAttribute("idProduto", idProduto);
            return "04-anuncio-informacoes";
        }
    }

    @GetMapping("/descricao")
    public String exibirDescricao(
            @RequestParam Long idProduto,
            HttpSession session,
            Model model) {

        try {

            verificarDono(idProduto, session);

            return "redirect:/anuncios/novo/informacoes?idProduto=" + idProduto;

        } catch (IllegalArgumentException |
                 IllegalStateException ex) {

            return "redirect:/anuncios/novo";
        }
    }

    @PostMapping("/descricao")
    public String processarDescricao(
            @Valid @ModelAttribute("passoDescricao")
            Passo4DescricaoRequest req,

            BindingResult bindingResult,

            @RequestParam Long idProduto,

            HttpSession session,

            Model model) {

        AnuncioWizardDTO anuncio =
                anuncioService.buscarPorId(idProduto);

        if (bindingResult.hasErrors()) {

            model.addAttribute("anuncio", anuncio);
            model.addAttribute("idProduto", idProduto);
            model.addAttribute(
                    "erro",
                    "Preencha a descricao, a quantidade e o local de retirada."
            );

            return "04b-anuncio-descricao";
        }

        try {

            verificarDono(idProduto, session);

            anuncioService.definirDescricaoEDetalhes(
                    idProduto,
                    req
            );

            return "redirect:/anuncios/novo/valor?idProduto="
                    + idProduto;

        } catch (IllegalArgumentException |
                 IllegalStateException ex) {

            model.addAttribute("anuncio", anuncio);
            model.addAttribute("idProduto", idProduto);
            model.addAttribute("erro", ex.getMessage());

            return "04b-anuncio-descricao";
        }
    }

    // ============================================================
    // PASSO 5 - VALOR
    // ============================================================

    @GetMapping("/valor")
    public String exibirPasso5(
            @RequestParam Long idProduto,
            HttpSession session,
            Model model) {

        try {

            verificarDono(idProduto, session);

            AnuncioWizardDTO anuncio =
                    anuncioService.buscarPorId(
                            idProduto
                    );

            model.addAttribute(
                    "anuncio",
                    anuncio
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            return "05-anuncio-valor";

        } catch (IllegalStateException |
                 IllegalArgumentException ex) {

            return "redirect:/anuncios/novo";
        }
    }

    @PostMapping("/valor")
    public String processarPasso5(
            @Valid @ModelAttribute("passo5")
            Passo5ValorRequest req,

            BindingResult bindingResult,

            @RequestParam Long idProduto,

            HttpSession session,

            Model model) {

        if (bindingResult.hasErrors()) {

            AnuncioWizardDTO anuncio =
                    anuncioService.buscarPorId(
                            idProduto
                    );

            model.addAttribute(
                    "anuncio",
                    anuncio
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            model.addAttribute(
                    "erro",
                    "Informe um valor valido (ex: 12,00)."
            );

            return "05-anuncio-valor";
        }

        try {

            verificarDono(idProduto, session);

            anuncioService.definirValor(
                    idProduto,
                    req
            );

            Usuario usuario =
                    obterUsuarioLogado(session);

            anuncioService.publicar(
                    idProduto,
                    usuario.getIdUsuario()
            );

            model.addAttribute(
                    "anuncio",
                    anuncioService.buscarPorId(idProduto)
            );

            return "07-anuncio-publicado";

        } catch (IllegalArgumentException |
                 IllegalStateException ex) {

            AnuncioWizardDTO anuncio =
                    anuncioService.buscarPorId(
                            idProduto
                    );

            model.addAttribute(
                    "anuncio",
                    anuncio
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            model.addAttribute(
                    "erro",
                    ex.getMessage()
            );

            return "05-anuncio-valor";
        }
    }

    // ============================================================
    // PASSO 6 - REVISÃO
    // ============================================================

    @GetMapping("/revisao")
    public String exibirPasso6(
            @RequestParam Long idProduto,
            HttpSession session,
            Model model) {

        try {

            verificarDono(idProduto, session);

            AnuncioWizardDTO anuncio =
                    anuncioService.obterParaRevisao(
                            idProduto
                    );

            model.addAttribute(
                    "anuncio",
                    anuncio
            );

            model.addAttribute(
                    "categoriaNome",
                    nomeCategoria(anuncio)
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            return "06-anuncio-revisao";

        } catch (IllegalArgumentException |
                 IllegalStateException ex) {

            model.addAttribute(
                    "erro",
                    ex.getMessage()
            );

            return "redirect:/anuncios/novo";
        }
    }

    // ============================================================
    // PUBLICAR
    // ============================================================

    @PostMapping("/publicar")
    public String publicar(
            @RequestParam Long idProduto,

            HttpSession session,

            Model model,

            RedirectAttributes redirectAttributes) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {

            model.addAttribute(
                    "erro",
                    "Voce precisa estar logado para publicar um anuncio."
            );

            return "redirect:/login";
        }

        try {

            Long idVendedor =
                    usuario.getIdUsuario();

            anuncioService.publicar(
                    idProduto,
                    idVendedor
            );

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Anúncio publicado e disponível na vitrine."
            );

            return "redirect:/perfil";

        } catch (IllegalArgumentException |
                 IllegalStateException ex) {

            AnuncioWizardDTO anuncio =
                    anuncioService.buscarPorId(
                            idProduto
                    );

            model.addAttribute(
                    "erro",
                    ex.getMessage()
            );

            model.addAttribute(
                    "anuncio",
                    anuncio
            );

            model.addAttribute(
                    "categoriaNome",
                    nomeCategoria(anuncio)
            );

            model.addAttribute(
                    "idProduto",
                    idProduto
            );

            return "06-anuncio-revisao";
        }
    }

    // ============================================================
    // CANCELAR
    // ============================================================

    @PostMapping("/cancelar")
    public String cancelar(
            @RequestParam Long idProduto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario =
                obterUsuarioLogado(session);

        try {

            anuncioService.cancelar(
                    idProduto,
                    usuario.getIdUsuario()
            );

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Anuncio cancelado."
            );

            return "redirect:/home";

        } catch (IllegalArgumentException |
                 IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    ex.getMessage()
            );

            return "redirect:/anuncios/novo/revisao?idProduto="
                    + idProduto;
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private List<Categoria> categoriasVisiveis() {
        return categoriaService.listarTodas()
                .stream()
                .filter(c -> CATEGORIAS_EXIBIDAS.contains(c.getNome()))
                .sorted(java.util.Comparator.comparingInt(
                        c -> CATEGORIAS_EXIBIDAS.indexOf(c.getNome())))
                .toList();
    }

    private Long resolverIdProduto(
            Long idProduto,
            HttpSession session) {

        if (idProduto != null) {
            return idProduto;
        }

        Usuario usuario = obterUsuarioLogado(session);
        Produto rascunho = anuncioService.buscarRascunhoDoVendedor(
                usuario.getIdUsuario()
        );

        if (rascunho == null) {
            throw new IllegalStateException("Nenhum anúncio em criação foi encontrado.");
        }

        return rascunho.getIdProduto();
    }

    private Usuario obterUsuarioLogado(
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {

            throw new IllegalStateException(
                    "Usuario nao autenticado."
            );
        }

        return usuario;
    }

    private void verificarDono(
            Long idProduto,
            HttpSession session) {

        Usuario usuario =
                obterUsuarioLogado(session);

        Produto produto =
                anuncioService.buscarRascunhoDoVendedor(
                        usuario.getIdUsuario()
                );

        if (produto == null ||
                !produto.getIdProduto()
                        .equals(idProduto)) {

            throw new IllegalStateException(
                    "Anuncio nao encontrado ou sem permissao."
            );
        }
    }

    private String nomeCategoria(
            AnuncioWizardDTO anuncio) {

        if (anuncio == null ||
                anuncio.getIdCategoria() == null) {

            return null;
        }

        Categoria categoria =
                anuncioService.buscarCategoria(
                        anuncio.getIdCategoria()
                );

        return categoria != null
                ? categoria.getNome()
                : null;
    }
}
