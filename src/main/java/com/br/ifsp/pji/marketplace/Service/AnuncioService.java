package com.br.ifsp.pji.marketplace.Service;

import com.br.ifsp.pji.marketplace.Dto.AnuncioPublicadoResponse;
import com.br.ifsp.pji.marketplace.Dto.AnuncioWizardDTO;
import com.br.ifsp.pji.marketplace.Dto.EditarAnuncioRequest;
import com.br.ifsp.pji.marketplace.Dto.Passo1InicioRequest;
import com.br.ifsp.pji.marketplace.Dto.Passo3CategoriaRequest;
import com.br.ifsp.pji.marketplace.Dto.Passo4DescricaoRequest;
import com.br.ifsp.pji.marketplace.Dto.Passo4NomeRequest;
import com.br.ifsp.pji.marketplace.Dto.Passo5ValorRequest;
import com.br.ifsp.pji.marketplace.model.Categoria;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.ProdutoImagem;
import com.br.ifsp.pji.marketplace.model.StatusProduto;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.CategoriaRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoImagemRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoRepository;
import com.br.ifsp.pji.marketplace.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
// Implementa o wizard de anuncios, validando dados, fotos, estoque e publicacao.
public class AnuncioService {

    private static final int MAX_FOTOS = 4;
    private static final long MAX_TAMANHO_FOTO = 8L * 1024 * 1024;
    private static final int MAX_CARACTERES_NOME = 45;
    private static final int MAX_CARACTERES_DESCRICAO = 250;

    private static final List<String> TIPOS_MIME_PERMITIDOS = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final ProdutoRepository produtoRepository;
    private final ProdutoImagemRepository produtoImagemRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    @Value("${app.upload-dir:uploads/produtos}")
    private String uploadDir;

    @Autowired
    public AnuncioService(
            ProdutoRepository produtoRepository,
            ProdutoImagemRepository produtoImagemRepository,
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService) {

        this.produtoRepository = produtoRepository;
        this.produtoImagemRepository = produtoImagemRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    // ============================================================
    // PASSO 1 - INICIAR ANÚNCIO
    // ============================================================

    public AnuncioWizardDTO iniciar(
            HttpSession session,
            Passo1InicioRequest req) {

        Usuario usuario = obterUsuarioLogado(session);

        if (!"produto".equalsIgnoreCase(req.getTipo())) {

            throw new IllegalArgumentException(
                    "No momento so e possivel anunciar produtos."
            );
        }

        Produto produto = buscarRascunhoDoVendedor(
                usuario.getIdUsuario()
        );

        if (produto == null) {
            produto = new Produto();

            // Agora o próprio Usuario é o responsável pelo anúncio.
            produto.setUsuario(usuario);

            produto.setStatus(StatusProduto.rascunho);

            produto.setQuantidadeDenuncias(0);

            /*
             * codigoProduto é obrigatório no banco.
             * Geramos o código antes do primeiro save.
             */
            produto.setCodigoProduto(
                    "CMP-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 16)
                            .toUpperCase()
            );
        }

        produto.setNome(validarTextoLimitado(req.getNome(), MAX_CARACTERES_NOME, "nome"));

        produto = produtoRepository.save(produto);

        return converterParaDTO(produto);
    }

    // ============================================================
    // PASSO 2 - FOTOS
    // ============================================================

    public AnuncioWizardDTO adicionarFotos(
            Long idProduto,
            List<MultipartFile> fotos) {

        Produto produto = obterRascunho(idProduto);

        if (fotos == null || fotos.isEmpty()) {

            throw new IllegalArgumentException(
                    "Envie ao menos uma foto do produto."
            );
        }

        List<ProdutoImagem> imagensExistentes =
                produtoImagemRepository.findByProdutoIdProduto(idProduto);

        if (imagensExistentes.size() + fotos.size() > MAX_FOTOS) {

            throw new IllegalArgumentException(
                    "Maximo de " + MAX_FOTOS + " fotos por anuncio."
            );
        }

        for (MultipartFile foto : fotos) {

            validarImagem(foto);

            String url = salvarArquivo(foto);

            ProdutoImagem imagem =
                    new ProdutoImagem(produto, url);

            produtoImagemRepository.save(imagem);

            /*
             * A primeira foto também fica registrada em fotoUrl
             * para facilitar a exibição como foto principal.
             */
            if (produto.getFotoUrl() == null ||
                    produto.getFotoUrl().isBlank()) {

                produto.setFotoUrl(url);
            }
        }

        produtoRepository.save(produto);

        return converterParaDTO(produto);
    }

    // ============================================================
    // REMOVER FOTO
    // ============================================================

    public AnuncioWizardDTO removerFoto(
            Long idProduto,
            String urlFoto) {

        Produto produto = obterRascunho(idProduto);

        List<ProdutoImagem> imagens =
                produtoImagemRepository
                        .findByProdutoIdProduto(idProduto);

        ProdutoImagem imagemRemover = null;

        for (ProdutoImagem imagem : imagens) {

            if (urlFoto.equals(imagem.getUrlImagem())) {

                imagemRemover = imagem;
                break;
            }
        }

        if (imagemRemover != null) {

            produtoImagemRepository.delete(imagemRemover);

            /*
             * Se a foto removida era a principal,
             * escolhe outra como principal.
             */
            if (urlFoto.equals(produto.getFotoUrl())) {

                List<ProdutoImagem> restantes =
                        produtoImagemRepository
                                .findByProdutoIdProduto(idProduto);

                if (!restantes.isEmpty()) {

                    produto.setFotoUrl(
                            restantes.get(0).getUrlImagem()
                    );

                } else {

                    produto.setFotoUrl(null);
                }

                produtoRepository.save(produto);
            }
        }

        return converterParaDTO(produto);
    }

    // ============================================================
    // PASSO 3 - CATEGORIA
    // ============================================================

    public AnuncioWizardDTO definirCategoria(
            Long idProduto,
            Passo3CategoriaRequest req) {

        Produto produto = obterRascunho(idProduto);

        Categoria categoria =
                categoriaRepository.findById(req.getIdCategoria())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Categoria nao encontrada."
                                )
                        );

        produto.setCategoria(categoria);

        produtoRepository.save(produto);

        return converterParaDTO(produto);
    }

    // ============================================================
    // PASSO 4 - INFORMAÇÕES
    // ============================================================

    public AnuncioWizardDTO definirNome(
            Long idProduto,
            Passo4NomeRequest req) {

        Produto produto = obterRascunho(idProduto);

        if (req.getNome() == null ||
                req.getNome().isBlank()) {

            throw new IllegalArgumentException(
                    "Informe o nome do produto."
            );
        }

        produto.setNome(validarTextoLimitado(req.getNome(), MAX_CARACTERES_NOME, "nome"));

        produtoRepository.save(produto);

        return converterParaDTO(produto);
    }

    public AnuncioWizardDTO definirDescricaoEDetalhes(
            Long idProduto,
            Passo4DescricaoRequest req) {

        Produto produto = obterRascunho(idProduto);

        if (req.getDescricao() == null ||
                req.getDescricao().isBlank()) {

            throw new IllegalArgumentException(
                    "Informe a descricao do produto."
            );
        }

        if (req.getQuantidade() == null ||
                req.getQuantidade() <= 0) {

            throw new IllegalArgumentException(
                    "Informe uma quantidade valida."
            );
        }

        if (req.getLocalRetirada() == null) {

            throw new IllegalArgumentException(
                    "Selecione o local de retirada."
            );
        }

        produto.setDescricao(validarTextoLimitado(req.getDescricao(), MAX_CARACTERES_DESCRICAO, "descricao"));
        produto.setQuantidade(req.getQuantidade());
        produto.setLocalRetirada(req.getLocalRetirada());

        produtoRepository.save(produto);

        return converterParaDTO(produto);
    }

    // ============================================================
    // PASSO 5 - VALOR
    // ============================================================

    @Transactional
    public AnuncioWizardDTO definirValor(
            Long idProduto,
            Passo5ValorRequest req) {

        Produto produto = obterRascunho(idProduto);

        if (req.getPreco() == null) {
            throw new IllegalArgumentException("Informe o preco.");
        }

        if (req.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preco deve ser maior que zero.");
        }

        if (req.getQuantidade() == null || req.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Informe uma quantidade valida.");
        }

        if (req.getLocalRetirada() == null) {
            throw new IllegalArgumentException("Selecione o local de retirada.");
        }

        if (req.getDisponibilidadeRetirada() == null) {
            throw new IllegalArgumentException("Selecione a disponibilidade para retirada.");
        }

        if (!"formatura".equals(req.getFinalidade()) && !"renda_extra".equals(req.getFinalidade())) {
            throw new IllegalArgumentException("Selecione a finalidade da arrecadacao.");
        }

        produto.setPreco(req.getPreco());
        produto.setQuantidade(req.getQuantidade());
        produto.setLocalRetirada(req.getLocalRetirada());
        produto.setDisponibilidadeRetirada(req.getDisponibilidadeRetirada());
        produto.setFinalidade(req.getFinalidade());

        produtoRepository.save(produto);

        return converterParaDTO(produto);
    }

    // ============================================================
    // BUSCAR ANÚNCIO
    // ============================================================

    public AnuncioWizardDTO buscarPorId(Long idProduto) {

        Produto produto =
                produtoRepository.findById(idProduto)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Anuncio nao encontrado."
                                )
                        );

        return converterParaDTO(produto);
    }

    // ============================================================
    // PASSO 6 - REVISÃO
    // ============================================================

    public AnuncioWizardDTO obterParaRevisao(Long idProduto) {

        Produto produto = obterRascunho(idProduto);

        AnuncioWizardDTO dto =
                converterParaDTO(produto);

        validarWizardCompleto(dto);
        
        return dto;
    }

    // ============================================================
    // PUBLICAR DIRETAMENTE NA VITRINE
    // ============================================================

    public AnuncioPublicadoResponse publicar(
            Long idProduto,
            Long idUsuarioLogado) {

        Produto produto =
                produtoRepository.findById(idProduto)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Anuncio nao encontrado."
                                )
                        );

        /*
         * Segurança:
         * o anúncio precisa pertencer ao usuário logado.
         */
        if (produto.getUsuario() == null ||
                produto.getUsuario().getIdUsuario() == null ||
                !produto.getUsuario()
                        .getIdUsuario()
                        .equals(idUsuarioLogado)) {

            throw new IllegalStateException(
                    "Voce nao tem permissao para publicar este anuncio."
            );
        }

        AnuncioWizardDTO wizard =
                converterParaDTO(produto);

        validarWizardCompleto(wizard);

        produto.setStatus(StatusProduto.disponivel);

        produto.setDataPublicacao(LocalDateTime.now());

        if (produto.getQuantidadeDenuncias() == null) {
            produto.setQuantidadeDenuncias(0);
        }

        produto = produtoRepository.save(produto);

        return AnuncioPublicadoResponse.of(produto);
    }

    // ============================================================
    // CANCELAR ANÚNCIO
    // ============================================================

    public void cancelar(
            Long idProduto,
            Long idUsuarioLogado) {

        Produto produto =
                produtoRepository.findById(idProduto)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Anuncio nao encontrado."
                                )
                        );

        if (produto.getUsuario() == null ||
                produto.getUsuario().getIdUsuario() == null ||
                !produto.getUsuario()
                        .getIdUsuario()
                        .equals(idUsuarioLogado)) {

            throw new IllegalStateException(
                    "Voce nao tem permissao para cancelar este anuncio."
            );
        }

        if (produto.getStatus() != StatusProduto.rascunho) {

            throw new IllegalStateException(
                    "Somente anuncios em rascunho podem ser cancelados."
            );
        }

        /*
         * Primeiro removemos as imagens relacionadas.
         */
        List<ProdutoImagem> imagens =
                produtoImagemRepository
                        .findByProdutoIdProduto(idProduto);

        if (!imagens.isEmpty()) {
            produtoImagemRepository.deleteAll(imagens);
        }

        /*
         * Depois removemos o Produto.
         */
        produtoRepository.delete(produto);
    }
    
    @Transactional
    public AnuncioPublicadoResponse revisar(
            Long idProduto,
            StatusProduto novoStatus) {

        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new IllegalArgumentException("Anuncio nao encontrado."));

        if (produto.getStatus() != StatusProduto.em_revisao) {
            throw new IllegalStateException("Este anuncio nao esta em revisao.");
        }

        if (novoStatus != StatusProduto.disponivel
                && novoStatus != StatusProduto.reprovado) {

            throw new IllegalArgumentException(
                    "Status invalido para revisao. Use disponivel ou reprovado."
            );
        }

        produto.setStatus(novoStatus);

        produto = produtoRepository.save(produto);

        if (novoStatus == StatusProduto.disponivel) {
            notificacaoService.notificarAnuncioAprovado(
                    produto.getUsuario(), produto.getNome(), produto.getIdProduto());
        } else {
            notificacaoService.notificarAnuncioReprovado(
                    produto.getUsuario(), produto.getNome());
        }

        return AnuncioPublicadoResponse.of(produto);
    }
    
 // ============================================================
 // MEUS ANÚNCIOS (LISTAGEM DO VENDEDOR)
 // ============================================================

 public List<Produto> listarMeusAnuncios(Long idUsuario) {

     return produtoRepository.findByUsuarioIdUsuario(idUsuario)
             .stream()
             .filter(p -> p.getStatus() != StatusProduto.rascunho)
             .toList();
 }

 // ============================================================
 // EDITAR ANÚNCIO JÁ PUBLICADO
 // ============================================================

 @Transactional
 public Produto editarPublicado(
         Long idProduto,
         Long idUsuarioLogado,
         EditarAnuncioRequest req,
         MultipartFile novaImagem) {
     return editarPublicado(idProduto, idUsuarioLogado, req, novaImagem, List.of(), List.of());
 }

 @Transactional
 public Produto editarPublicado(Long idProduto, Long idUsuarioLogado, EditarAnuncioRequest req,
         MultipartFile novaImagem, List<MultipartFile> secundarias, List<String> urlsOriginais) {

     Produto produto = obterDoDono(idProduto, idUsuarioLogado);
     List<ProdutoImagem> imagens = new ArrayList<>(produtoImagemRepository.findByProdutoIdProduto(idProduto));
     if (secundarias.size() != urlsOriginais.size()) throw new IllegalArgumentException("Fotos invalidas.");
     int novas = 0;
     java.util.Set<String> substituidas = new java.util.HashSet<>();
     if (novaImagem != null && !novaImagem.isEmpty()) validarImagem(novaImagem);
     for (int i = 0; i < secundarias.size(); i++) {
         MultipartFile foto = secundarias.get(i);
         if (foto == null || foto.isEmpty()) continue;
         validarImagem(foto);
         String original = urlsOriginais.get(i);
         if (original == null || original.isBlank()) { novas++; continue; }
         if (original.equals(produto.getFotoUrl()) || !substituidas.add(original)
                 || imagens.stream().noneMatch(imagem -> original.equals(imagem.getUrlImagem()))) {
             throw new IllegalArgumentException("Foto adicional nao encontrada neste anuncio. Recarregue a pagina.");
         }
     }
     boolean principalSemRegistro = produto.getFotoUrl() != null && !produto.getFotoUrl().isBlank()
             && imagens.stream().noneMatch(imagem -> produto.getFotoUrl().equals(imagem.getUrlImagem()));
     int total = imagens.size() + (principalSemRegistro ? 1 : 0) + novas;
     if (imagens.isEmpty() && !principalSemRegistro && novaImagem != null && !novaImagem.isEmpty()) total++;
     if (total > MAX_FOTOS) throw new IllegalArgumentException("Maximo de " + MAX_FOTOS + " fotos por anuncio.");

     if (req.getNome() != null && !req.getNome().isBlank()) {
         produto.setNome(validarTextoLimitado(req.getNome(), MAX_CARACTERES_NOME, "nome"));
     }

     if (req.getDescricao() != null && !req.getDescricao().isBlank()) {
         produto.setDescricao(validarTextoLimitado(req.getDescricao(), MAX_CARACTERES_DESCRICAO, "descricao"));
     }

     if (req.getValor() != null) {

         if (req.getValor().compareTo(BigDecimal.ZERO) <= 0) {
             throw new IllegalArgumentException("O preco deve ser maior que zero.");
         }

         produto.setPreco(req.getValor());
     }

     if (req.getQuantidade() != null && req.getQuantidade() >= 0) {
         produto.setQuantidade(req.getQuantidade());
     }

     if (req.getCategoria() != null) {

         Categoria categoria = categoriaRepository.findById(req.getCategoria())
                 .orElseThrow(() -> new IllegalArgumentException("Categoria nao encontrada."));

         produto.setCategoria(categoria);
     }

     if (req.getDisponibilidadeRetirada() != null) {
         produto.setDisponibilidadeRetirada(req.getDisponibilidadeRetirada());
     }
     if (req.getLocalRetirada() != null) {
         produto.setLocalRetirada(req.getLocalRetirada());
     }
     if (req.getFinalidade() != null) {
         if (!"formatura".equals(req.getFinalidade()) && !"renda_extra".equals(req.getFinalidade())) {
             throw new IllegalArgumentException("Finalidade invalida.");
         }
         produto.setFinalidade(req.getFinalidade());
     }

     if (novaImagem != null && !novaImagem.isEmpty()) {

         validarImagem(novaImagem);

         ProdutoImagem principal = imagens.stream()
                 .filter(imagem -> imagem.getUrlImagem().equals(produto.getFotoUrl())).findFirst()
                 .orElse(null);
         String url = salvarArquivo(novaImagem);
         if (principal == null) principal = new ProdutoImagem(produto, url);
         else principal.setUrlImagem(url);
         produtoImagemRepository.save(principal);
         produto.setFotoUrl(url);
     }
     for (int i = 0; i < secundarias.size(); i++) {
         MultipartFile foto = secundarias.get(i);
         if (foto == null || foto.isEmpty()) continue;
         String original = urlsOriginais.get(i);
         ProdutoImagem imagem = imagens.stream().filter(item -> item.getUrlImagem().equals(original))
                 .findFirst().orElse(null);
         String url = salvarArquivo(foto);
         if (imagem == null) imagem = new ProdutoImagem(produto, url);
         else imagem.setUrlImagem(url);
         produtoImagemRepository.save(imagem);
     }

     return produtoRepository.save(produto);
 }

 // ============================================================
 // EXCLUIR ARQUIVO FÍSICO DE UMA IMAGEM
 // ============================================================

 private void excluirArquivoFisico(String urlImagem) {

     if (urlImagem == null || urlImagem.isBlank()) {
         return;
     }

     try {

         String nomeArquivo = urlImagem.substring(urlImagem.lastIndexOf('/') + 1);

         Path caminho = Paths.get(uploadDir).resolve(nomeArquivo);

         Files.deleteIfExists(caminho);

     } catch (IOException ex) {

         /*
          * Nao interrompe a edicao se o arquivo fisico ja
          * nao existir ou nao puder ser removido. So loga.
          */
         System.err.println(
                 "Falha ao excluir arquivo de imagem antigo: "
                         + urlImagem + " - " + ex.getMessage()
         );
     }
 }

 // ============================================================
 // ALTERAR STATUS DE ANÚNCIO PUBLICADO (DONO)
 // ============================================================

 @Transactional
 public void alterarStatusPublicado(
         Long idProduto,
         Long idUsuarioLogado,
         StatusProduto novoStatus) {

     Produto produto = obterDoDono(idProduto, idUsuarioLogado);

     if (produto.getStatus() != StatusProduto.disponivel
             && produto.getStatus() != StatusProduto.indisponivel) {

         throw new IllegalStateException(
                 "Este anuncio nao pode ter o status alterado neste momento."
         );
     }

     if (novoStatus != StatusProduto.disponivel
             && novoStatus != StatusProduto.indisponivel) {

         throw new IllegalArgumentException("Status invalido.");
     }

     produto.setStatus(novoStatus);

     produtoRepository.save(produto);
 }

 // ============================================================
 // EXCLUIR ANÚNCIO PUBLICADO (DONO)
 // ============================================================

 @Transactional
 public void excluirPublicado(Long idProduto, Long idUsuarioLogado) {

     Produto produto = obterDoDono(idProduto, idUsuarioLogado);

     List<ProdutoImagem> imagens =
             produtoImagemRepository.findByProdutoIdProduto(idProduto);

     for (ProdutoImagem imagem : imagens) {
         excluirArquivoFisico(imagem.getUrlImagem());
     }

     if (!imagens.isEmpty()) {
         produtoImagemRepository.deleteAll(imagens);
     }

     produtoRepository.delete(produto);
 }

 // ============================================================
 // HELPER: CONFIRMA QUE O PRODUTO PERTENCE AO USUÁRIO
 // ============================================================

 private Produto obterDoDono(Long idProduto, Long idUsuarioLogado) {

     Produto produto = produtoRepository.findById(idProduto)
             .orElseThrow(() -> new IllegalArgumentException("Anuncio nao encontrado."));

     if (produto.getUsuario() == null
             || !produto.getUsuario().getIdUsuario().equals(idUsuarioLogado)) {

         throw new IllegalStateException("Voce nao tem permissao sobre este anuncio.");
     }

     return produto;
 }

    // ============================================================
    // CATEGORIA
    // ============================================================

    public Categoria buscarCategoria(Long idCategoria) {

        if (idCategoria == null) {
            return null;
        }

        return categoriaRepository.findById(idCategoria)
                .orElse(null);
    }

    // ============================================================
    // BUSCAR RASCUNHO DO USUÁRIO
    // ============================================================

    public Produto buscarRascunhoDoVendedor(
            Long idUsuario) {

        List<Produto> produtos =
                produtoRepository.findByUsuarioIdUsuario(idUsuario);

        /*
         * Como o projeto atualmente trabalha com um wizard
         * por vez, pegamos o primeiro rascunho encontrado.
         */
        for (Produto produto : produtos) {

            if (produto.getStatus() == StatusProduto.rascunho) {
                return produto;
            }
        }

        return null;
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private Usuario obterUsuarioLogado(
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {

            throw new IllegalStateException(
                    "Usuario nao autenticado."
            );
        }

        return usuario;
    }

    private Produto obterRascunho(
            Long idProduto) {

        if (idProduto == null) {

            throw new IllegalStateException(
                    "ID do anuncio nao informado."
            );
        }

        Produto produto =
                produtoRepository.findById(idProduto)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Anuncio nao encontrado."
                                )
                        );

        if (produto.getStatus() != StatusProduto.rascunho) {

            throw new IllegalStateException(
                    "Este anuncio nao esta mais em rascunho."
            );
        }

        return produto;
    }

    // ============================================================
    // CONVERTER PRODUTO → DTO
    // ============================================================

    private AnuncioWizardDTO converterParaDTO(Produto produto) {

        AnuncioWizardDTO dto = new AnuncioWizardDTO();

        dto.setIdProduto(produto.getIdProduto());

        if (produto.getUsuario() != null) {
            dto.setIdUsuario(produto.getUsuario().getIdUsuario());
        }

        dto.setNome(produto.getNome());
        dto.setTipo("PRODUTO");
        dto.setDescricao(produto.getDescricao());
        dto.setQuantidade(produto.getQuantidade());
        dto.setPreco(produto.getPreco());
        dto.setLocalRetirada(produto.getLocalRetirada()); // <-- ADICIONE ESTA LINHA
        dto.setDisponibilidadeRetirada(produto.getDisponibilidadeRetirada());
        dto.setFinalidade(produto.getFinalidade());

        if (produto.getCategoria() != null) {
            dto.setIdCategoria(produto.getCategoria().getIdCategoria());
        }

        /*
         * Busca todas as fotos relacionadas ao Produto.
         */
        List<ProdutoImagem> imagens =
                produtoImagemRepository
                        .findByProdutoIdProduto(
                                produto.getIdProduto()
                        );

        List<String> urls =
                new ArrayList<>();

        for (ProdutoImagem imagem : imagens) {

            if (imagem.getUrlImagem() != null) {
                urls.add(imagem.getUrlImagem());
            }
        }

        /*
         * Caso exista fotoUrl mas ainda não exista
         * registro em ProdutoImagem.
         */
        if (urls.isEmpty() &&
                produto.getFotoUrl() != null &&
                !produto.getFotoUrl().isBlank()) {

            urls.add(produto.getFotoUrl());
        }

        if (produto.getFotoUrl() != null && !produto.getFotoUrl().isBlank()) {
            urls.remove(produto.getFotoUrl());
            urls.add(0, produto.getFotoUrl());
        }
        dto.setUrlsFotos(urls);

        return dto;
    }

    // ============================================================
    // VALIDAÇÃO FINAL
    // ============================================================

    private void validarWizardCompleto(
            AnuncioWizardDTO wizard) {

        List<String> pendencias =
                new ArrayList<>();

        if (!wizard.passo1Completo()) {
            pendencias.add("inicio");
        }

        if (!wizard.passo2Completo()) {
            pendencias.add("fotos");
        }

        if (!wizard.passo3Completo()) {
            pendencias.add("categoria");
        }

        if (!wizard.passo4Completo()) {
            pendencias.add("informacoes");
        }

        if (!wizard.passo5Completo()) {
            pendencias.add("valor");
        }

        if (!pendencias.isEmpty()) {

            throw new IllegalStateException(
                    "Complete os passos antes de publicar: "
                            + String.join(", ", pendencias)
            );
        }
    }

    // ============================================================
    // IMAGENS
    // ============================================================

    private String validarTextoLimitado(
            String texto,
            int limite,
            String campo) {

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe " + ("nome".equals(campo) ? "o nome" : "a descricao") + " do produto."
            );
        }

        String textoTratado = texto.trim();

        if (textoTratado.length() > limite) {
            throw new IllegalArgumentException(
                    "O campo " + campo + " deve ter no maximo " + limite + " caracteres."
            );
        }

        return textoTratado;
    }

    private void validarImagem(
            MultipartFile foto) {

        if (foto == null ||
                foto.isEmpty()) {

            throw new IllegalArgumentException(
                    "Arquivo de imagem vazio."
            );
        }

        if (!TIPOS_MIME_PERMITIDOS
                .contains(foto.getContentType())) {

            throw new IllegalArgumentException(
                    "Formato de imagem invalido. Use JPG, PNG ou WEBP."
            );
        }

        if (foto.getSize() > MAX_TAMANHO_FOTO) {

            throw new IllegalArgumentException(
                    "A imagem deve ter no maximo 8 MB."
            );
        }
    }

    private String salvarArquivo(
            MultipartFile foto) {

        try {

            Path pastaDestino =
                    Paths.get(uploadDir);

            Files.createDirectories(
                    pastaDestino
            );

            String extensao =
                    StringUtils.getFilenameExtension(
                            foto.getOriginalFilename()
                    );

            String nomeArquivo =
                    UUID.randomUUID()
                            + (extensao != null
                            ? "." + extensao
                            : "");

            Path caminhoArquivo =
                    pastaDestino.resolve(nomeArquivo);

            try (InputStream in =
                         foto.getInputStream()) {

                Files.copy(
                        in,
                        caminhoArquivo,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return "/imagens/produtos/"
                    + nomeArquivo;

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Nao foi possivel salvar a imagem. Tente novamente.",
                    ex
            );
        }
    }
}
