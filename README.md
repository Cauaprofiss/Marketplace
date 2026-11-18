# 🎓 Campus Marketplace

Plataforma web de marketplace voltada para o ambiente universitário, permitindo que estudantes publiquem, busquem e negociem produtos e serviços dentro do campus.

---

## 🛠️ Tecnologias Utilizadas

* **Back-end:** Java com Spring Boot
* **Persistência de Dados:** Spring Data JPA / Hibernate
* **Banco de Dados:** MySQL
* **Front-end / Templates:** Thymeleaf, HTML5, CSS3 e JavaScript
* **Serviço de E-mail:** Spring Boot Starter Mail (SMTP)

---

## ⚙️ Funcionalidades Principais (Baseadas na Configuração)

* **Gestão de Anúncios com Imagens:** Suporte para upload de até **4 imagens por anúncio** (limite de até **8 MB** por arquivo e **34 MB** por requisição).
* **Autenticação e Sessão:** Controle de sessão via cookies com expiração automática após **60 minutos** de inatividade.
* **Notificações por E-mail:** Integração SMTP configurada para envio de e-mails transacionais (como confirmação de conta, recuperação de senha ou alertas de contato).
* **Criação Automática de Esquema:** O banco de dados e as tabelas são criados e atualizados automaticamente na inicialização da aplicação.

---

## 🚀 Pré-requisitos

Antes de rodar o projeto, certifique-se de ter instalado em sua máquina:

1. **Java JDK** (versão 17 ou superior recomendada)
2. **Maven** ou **Gradle** (ou utilize o wrapper `./mvnw` do projeto)
3. **MySQL Server** rodando localmente na porta `3306`

---

## 🔧 Configuração do Ambiente

> ⚠️ **Aviso de Segurança:** Nunca suba senhas reais de banco de dados ou senhas de aplicativo de e-mail diretamente no repositório público (GitHub/GitLab). Utilize variáveis de ambiente ou um arquivo `application-local.properties` ignorado pelo `.gitignore`.

1. Clone o repositório:
   ```bash
   git clone <url-do-seu-repositorio>
   cd campusmarketplace
   ```

2. Configure o arquivo `src/main/resources/application.properties` com suas credenciais locais:
   ```properties
   spring.application.name=campusmarketplace

   # Banco de Dados MySQL
   spring.datasource.url=jdbc:mysql://localhost:3306/campusmarketplace?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Sao_Paulo
   spring.datasource.username=SEU_USUARIO_MYSQL
   spring.datasource.password=SUA_SENHA_MYSQL
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

   # JPA / Hibernate
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true

   # Upload de Arquivos (até 4 imagens de 8MB)
   spring.servlet.multipart.max-file-size=8MB
   spring.servlet.multipart.max-request-size=34MB

   # Sessão
   server.servlet.session.tracking-modes=cookie
   server.servlet.session.timeout=60m

   # Desenvolvimento (Hot Reload de templates e estáticos)
   spring.thymeleaf.cache=false
   spring.web.resources.cache.period=0
   spring.web.resources.chain.cache=false

   # Configuração de E-mail (SMTP Gmail)
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=seu-email@gmail.com
   spring.mail.password=sua-senha-de-app
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true

   # URL Base
   app.base-url=http://localhost:8080
   ```

---

## ▶️ Como Executar

1. Inicie o serviço do **MySQL** na sua máquina.
2. Execute a aplicação via terminal na raiz do projeto:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(No Windows, utilize `mvnw.cmd spring-boot:run`)*
3. Acesse no navegador:
   ```text
   http://localhost:8080
   ```

---

## 🔒 Dica de Segurança Importante

Como você compartilhou a senha do banco e a **Senha de App do Gmail** (`spring.mail.password`) na mensagem anterior, recomenda-se **revogar e gerar uma nova Senha de App** na sua conta Google caso este projeto seja enviado para um repositório público.
