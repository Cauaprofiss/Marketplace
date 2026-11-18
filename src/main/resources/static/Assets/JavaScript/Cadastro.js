document.addEventListener('DOMContentLoaded', function () {

  const form = document.getElementById('formCadastro') || document.querySelector('.Formulario__login');
  const AreaInputSenha = document.getElementById('areaSenha');
  const AreaInputConfirmSenha = document.getElementById('areaConfirmSenha');
  const areaEmail = document.getElementById('areaEmail');
  
  const inputNome = document.getElementById('Nome');
  const selectTurma = document.getElementById('Turma');
  const inputEmail = document.getElementById('Cadastro-email');
  const senha = document.querySelector('.Cadastro-senha') || document.getElementById('Cadastro-senha');
  const confirmSenha = document.getElementById('ConfirmSenha');
  
  const erroSpan = document.getElementById('Mensagem__erroSenhas');

  const balaoNome = document.getElementById('balaoNome');
  const balaoTurma = document.getElementById('balaoTurma');
  const balaoEmail = document.getElementById('balaoEmail');
  const balaoSenha = document.getElementById('balaoSenha');
  const balaoConfirmSenha = document.getElementById('balaoConfirmSenha');

  const ImgOlhoAberto = "../Assets/Icones/view.png";
  const ImgOlhoFechado = "../Assets/Icones/hide.png";

  const imgOlhoSenha = document.getElementById('imgOlhoSenha');
  const imgOlhoConfirmSenha = document.getElementById('imgOlhoConfirmSenha');

  // Mesma regra utilizada no login, incluindo letras acentuadas.
  const regexEmailValido = /^[\p{L}\p{N}._%+-]+@(?:gmail|hotmail|outlook)\.com$|^[\p{L}\p{N}._%+-]+@aluno\.ifsp\.edu\.br$/iu;

  // Função para alternar entre texto e senha (Olhinho)
  function alternarVisibilidade(inputCampo, imgElemento) {
    if (!inputCampo || !imgElemento) return;

    if (inputCampo.type === 'password') {
      inputCampo.type = 'text';
      imgElemento.src = ImgOlhoFechado;
    } else {
      inputCampo.type = 'password';
      imgElemento.src = ImgOlhoAberto;
    }
  }

  // Eventos de clique nos olhinhos isolados da validação de submit
  if (imgOlhoSenha) {
    imgOlhoSenha.addEventListener('click', function () {
      alternarVisibilidade(senha, imgOlhoSenha);
    });
  }

  if (imgOlhoConfirmSenha) {
    imgOlhoConfirmSenha.addEventListener('click', function () {
      alternarVisibilidade(confirmSenha, imgOlhoConfirmSenha);
    });
  }

  // Lógica de manipulação de erros (Balao e Campos vazios)
  function ocultarErroVazio(balao, elementoAlvo) {
    if (balao) balao.style.display = 'none';
    if (elementoAlvo) elementoAlvo.classList.remove('input-erro');
  }

  function exibirErroVazio(balao, elementoAlvo, textoErro) {
    if (balao) {
      balao.style.display = 'flex';
      if (textoErro) balao.textContent = textoErro;
    }
    if (elementoAlvo) elementoAlvo.classList.add('input-erro');
  }

  function limparErroSenhas() {
     if (erroSpan) {
       erroSpan.style.display = 'none';
       erroSpan.textContent = '';
     }
     if (AreaInputSenha) AreaInputSenha.classList.remove('input-erro');
     if (AreaInputConfirmSenha) AreaInputConfirmSenha.classList.remove('input-erro');
  }

  // Valida regras de complexidade das senhas no Span tradicional do Thymeleaf
  function validarSenhas() {
    if (senha.value.length < 8) {
      if (erroSpan) erroSpan.textContent = 'A senha deve ter no mínimo 8 caracteres.';
      return false;
    }

    if (senha.value !== confirmSenha.value) {
      if (erroSpan) erroSpan.textContent = 'As senhas não coincidem.';
      return false;
    }

    return true;
  }

  if (form) {
    form.addEventListener('submit', function (event) {
      let temVazio = false;

      // Verifica Nome Vazio
      if (!inputNome || inputNome.value.trim() === '') {
        exibirErroVazio(balaoNome, inputNome);
        temVazio = true;
      } else {
        ocultarErroVazio(balaoNome, inputNome);
      }

      // Verifica Turma Vazia
      if (!selectTurma || selectTurma.value === '') {
        exibirErroVazio(balaoTurma, selectTurma);
        temVazio = true;
      } else {
        ocultarErroVazio(balaoTurma, selectTurma);
      }

      // Mesma validação de e-mail da tela de login.
      if (!inputEmail || inputEmail.value.trim() === '') {
        exibirErroVazio(balaoEmail, areaEmail || inputEmail, 'Preencha seu e-mail!');
        temVazio = true;
      } else if (!regexEmailValido.test(inputEmail.value.trim())) {
        exibirErroVazio(balaoEmail, areaEmail || inputEmail, 'Use um e-mail válido (ex: @gmail.com ou @aluno.ifsp.edu.br)');
        temVazio = true;
      } else {
        ocultarErroVazio(balaoEmail, areaEmail || inputEmail);
      }

      // Verifica Senha Vazia
      if (!senha || senha.value.trim() === '') {
        exibirErroVazio(balaoSenha, AreaInputSenha || senha);
        temVazio = true;
      } else {
        ocultarErroVazio(balaoSenha, AreaInputSenha || senha);
      }

      // Verifica Confirmar Senha Vazia
      if (!confirmSenha || confirmSenha.value.trim() === '') {
        exibirErroVazio(balaoConfirmSenha, AreaInputConfirmSenha || confirmSenha);
        temVazio = true;
      } else {
        ocultarErroVazio(balaoConfirmSenha, AreaInputConfirmSenha || confirmSenha);
      }

      // Se houver qualquer campo vazio, para a execução e prioriza os balões
      if (temVazio) {
        event.preventDefault();
        limparErroSenhas();
        return;
      }

      // Se nenhum estiver vazio, valida as regras de senha do Span tradicional do Thymeleaf
      if (!validarSenhas()) {
        event.preventDefault();

        if (erroSpan) erroSpan.style.display = 'block';
        if (AreaInputSenha) AreaInputSenha.classList.add('input-erro');
        if (AreaInputConfirmSenha) AreaInputConfirmSenha.classList.add('input-erro');

        if (senha.value.length < 8) {
          senha.focus();
        } else {
          confirmSenha.focus();
        }
      } else {
        limparErroSenhas();
      }
    });
  }

  function verificarEmTempoReal() {
    if (senha.value.length >= 8 && senha.value === confirmSenha.value) {
      limparErroSenhas();
    }
  }

  // Limpadores em tempo real para os balões de erro de campo vazio
  if (inputNome) inputNome.addEventListener('input', () => ocultarErroVazio(balaoNome, inputNome));
  if (selectTurma) selectTurma.addEventListener('change', () => ocultarErroVazio(balaoTurma, selectTurma));
  if (inputEmail) {
    inputEmail.addEventListener('input', function () {
      if (inputEmail.value.trim() !== '' && regexEmailValido.test(inputEmail.value.trim())) {
        ocultarErroVazio(balaoEmail, areaEmail || inputEmail);
      }
    });
  }
  
  if (senha) {
    senha.addEventListener('input', function() {
      ocultarErroVazio(balaoSenha, AreaInputSenha || senha);
      verificarEmTempoReal();
    });
  }
  
  if (confirmSenha) {
    confirmSenha.addEventListener('input', function() {
      ocultarErroVazio(balaoConfirmSenha, AreaInputConfirmSenha || confirmSenha);
      verificarEmTempoReal();
    });
  }
});
