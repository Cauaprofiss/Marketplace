document.addEventListener('DOMContentLoaded', function () {

  // Seleção dos elementos da Tela de Login
  const form = document.getElementById('formLogin') || document.querySelector('.Formulario__login');
  const areaEmail = document.getElementById('areaEmail');
  const AreaInputSenha = document.getElementById('areaSenha');
  
  const inputEmail = document.getElementById('Login-email') || document.getElementById('Cadastro-email');
  const senha = document.getElementById('Login-senha') || document.getElementById('Cadastro-senha') || document.querySelector('.Cadastro-senha');
  
  const balaoEmail = document.getElementById('balaoEmail');
  const balaoSenha = document.getElementById('balaoSenha');

  const ImgOlhoAberto = "../Assets/Icones/view.png";
  const ImgOlhoFechado = "../Assets/Icones/hide.png";
  const imgOlhoSenha = document.getElementById('ImgSenha');

  // Aceita letras Unicode no nome do e-mail (por exemplo, ã@gmail.com).
  const regexEmailValido = /^[\p{L}\p{N}._%+-]+@(?:gmail|hotmail|outlook)\.com$|^[\p{L}\p{N}._%+-]+@aluno\.ifsp\.edu\.br$/iu;

  // Função para alternar entre texto e senha
  if (imgOlhoSenha && senha) {
    imgOlhoSenha.addEventListener('click', function () {
      if (senha.type === 'password') {
        senha.type = 'text';
        imgOlhoSenha.src = ImgOlhoFechado;
      } else {
        senha.type = 'password';
        imgOlhoSenha.src = ImgOlhoAberto;
      }
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

  // Validação no envio do formulário de Login
  if (form) {
    form.addEventListener('submit', function (event) {
      let temErro = false;

      // VALIDAÇÃO AVANÇADA DE E-MAIL (Vazio ou Formato Inválido)
      if (!inputEmail || inputEmail.value.trim() === '') {
        exibirErroVazio(balaoEmail, areaEmail || inputEmail, "Preencha seu e-mail!");
        temErro = true;
      } else if (!regexEmailValido.test(inputEmail.value.trim())) {
        exibirErroVazio(balaoEmail, areaEmail || inputEmail, "Use um e-mail válido (ex: @gmail.com ou @aluno.ifsp.edu.br)");
        temErro = true;
      } else {
        ocultarErroVazio(balaoEmail, areaEmail || inputEmail);
      }

      // Verifica Senha Vazia
      if (!senha || senha.value.trim() === '') {
        exibirErroVazio(balaoSenha, AreaInputSenha || senha, "Digite sua senha!");
        temErro = true;
      } else {
        ocultarErroVazio(balaoSenha, AreaInputSenha || senha);
      }

      // Se houver qualquer erro, impede o envio para o servidor
      if (temErro) {
        event.preventDefault();
      }
    });
  }

  // REQUISITO: O balão de e-mail inválido só some quando o formato inserido for válido
  if (inputEmail) {
    inputEmail.addEventListener('input', function() {
      if (inputEmail.value.trim() !== '' && regexEmailValido.test(inputEmail.value.trim())) {
        ocultarErroVazio(balaoEmail, areaEmail || inputEmail);
      }
    });
  }
  
  if (senha) {
    senha.addEventListener('input', function() {
      ocultarErroVazio(balaoSenha, AreaInputSenha || senha);
    });
  }
});
