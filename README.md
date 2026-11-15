# HomeShare (back-end)

API do HomeShare, o app que a gente fez pro Projeto Integrador pra organizar a vida numa casa compartilhada: quem faz o quê, quem pagou o aluguel, quando a lavanderia está livre, essas coisas.

Feito com Java 26, Spring Boot 4.1.2, PostgreSQL, Redis e Maven. O e-mail de confirmação sai pelo Resend.

## O que você precisa ter instalado

- JDK 26 (se o Lombok reclamar, instala o 25 e troca o `java.version` no `pom.xml`)
- Docker Desktop, aberto antes de rodar. É ele que sobe o Postgres e o Redis
- IntelliJ

Não precisa instalar banco nenhum na mão. O `compose.yaml` cuida disso e o Spring já se conecta sozinho.

## Configurando o .env

Copia o `.env.example` e renomeia pra `.env`, na mesma pasta do `pom.xml`. Esse arquivo não vai pro GitHub, então pode colocar suas chaves nele sem medo.

O exemplo já vem pronto pra testar. Ele usa `MAIL_PROVIDER=log`, que não manda e-mail nenhum: o código de confirmação do cadastro aparece no console do IntelliJ, numa linha com "Token:". Dá pra criar conta e usar o sistema todo sem ter conta no Resend.

Se quiser receber o e-mail de verdade, muda pra `MAIL_PROVIDER=resend` e preenche a `RESEND_API_KEY`. Enquanto você não tiver um domínio verificado no Resend, ele só entrega pro e-mail dono da conta, então coloca esse e-mail em `MAIL_OVERRIDE_TO` e todo e-mail do sistema vai pra ele.

As variáveis, rapidinho:

- `JWT_SECRET`: segredo que assina o login. Precisa de pelo menos 32 caracteres. Pra gerar um: `openssl rand -hex 48`
- `MAIL_PROVIDER`: `log` ou `resend`
- `RESEND_API_KEY`, `MAIL_FROM`, `MAIL_OVERRIDE_TO`: só importam no modo `resend`
- `FRONTEND_URL`: endereço do front, usado nos links de convite e de confirmação (padrão `http://localhost:3000`)
- `CORS_ALLOWED_ORIGINS`: de onde o front pode chamar a API

Formato é `CHAVE=valor`, sem aspas e sem espaço em volta do `=`.

Se o Spring não achar o `.env`, quase sempre é um destes: o arquivo ficou como `.env.txt` (no Windows, liga a opção de mostrar extensões pra ver), ele está dentro de `src` em vez de ao lado do `pom.xml`, ou o `application.yaml` não tem a linha `config.import: optional:file:.env[.properties]`. Também dá pra preencher tudo em *Run > Edit Configurations > Environment variables* no IntelliJ. No IntelliJ o `.env` aparece como texto puro, sem cor. Isso é normal.

## Rodando

1. Abre o Docker Desktop.
2. Abre o projeto no IntelliJ e deixa o Maven carregar.
3. Roda a classe `HomeShareApplication`.
4. A API sobe em http://localhost:8080. Na primeira vez o Flyway cria as tabelas sozinho.

Pra testar sem front, o arquivo `http/homeshare.http` tem uma requisição pronta pra cada rota. Escolhe o ambiente `dev` e vai apertando o play.

Quando você para a aplicação, o Docker só pausa os containers e os dados continuam lá. Só não usa `docker compose down`, porque isso apaga o banco. Se quiser zerar de propósito, é `docker compose down -v`.

Os testes rodam com `mvn test` e não precisam do Docker.

## Como o cadastro funciona

A pessoa preenche nome, e-mail e senha em `POST /api/auth/register`. Nada vai pro banco ainda: os dados ficam guardados no Redis por 15 minutos (a senha já criptografada) e um código é enviado por e-mail. Quando ela manda esse código em `POST /api/auth/confirm`, aí sim o usuário é gravado no PostgreSQL. O código só funciona uma vez. Depois é só fazer login em `POST /api/auth/login` e usar o token que volta.

## Convidando gente pra casa

O líder gera um convite em `POST /api/casas/{id}/convites`. A resposta já traz o `link` pronto pra mandar no WhatsApp. Quem recebe abre o link, entra (ou cria a conta) e aceita. Cada convite serve pra uma pessoa só e vale 48 horas por padrão, mas o líder pode cancelar antes.

## Algumas regras que vale conhecer

- Cada morador só enxerga a casa em que mora.
- Só existe um líder por casa. Se ele sair, o morador mais antigo assume. Se não sobrar ninguém, a casa é encerrada.
- O líder não consegue expulsar a si mesmo.
- Duas reservas do mesmo espaço não podem se sobrepor.
- Num gasto fixo, a soma das parcelas tem que bater com o valor total.

A lista completa, com a rota de cada coisa, está no `ROADMAP.md`.

## Onde fica cada coisa

Dentro de `src/main/java/com/homeshare`:

- `controller`: as rotas
- `service`: as regras de negócio
- `repository`: o acesso ao banco
- `entity` e `enums`: as 14 tabelas e os status
- `dto`: o formato do que entra e sai da API
- `security` e `config`: login com JWT, CORS e configurações
- `email`: envio do e-mail
- `exception`: tratamento dos erros

As tabelas são criadas por `src/main/resources/db/migration/V1__criar_schema_inicial.sql`.

## Se der erro

- **`Could not resolve placeholder 'JWT_SECRET'`**: o `.env` não foi encontrado. Veja a parte do `.env` lá em cima.
- **`missing table [alguma coisa]`**: sobrou uma `@Entity` velha no projeto. Só as 14 da pasta `entity` devem existir.
- **`Could not find or load main class`**: confere se a classe é `HomeShareApplication` e roda `clean` no Maven.
- **Erro de conexão com Postgres ou Redis**: o Docker Desktop está fechado.
- **`RESEND_API_KEY não configurada`**: preenche a chave ou volta pro `MAIL_PROVIDER=log`.
- **Nenhum `EmailService` encontrado**: falta o `EmailConfig.java` na pasta `email`, ou o `target` está velho. Apaga ele e reconstrói.
- **401 em tudo**: o token venceu (dura 2 horas) ou faltou o `Bearer` na frente.

## Subindo pra produção

Ativa o perfil com `SPRING_PROFILES_ACTIVE=prod`, que desliga o Docker Compose, e passa a conexão com o banco e o Redis por variáveis de ambiente (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`). Troca o `JWT_SECRET`, usa um domínio verificado no Resend e deixa o `MAIL_OVERRIDE_TO` vazio.

E nunca sobe chave de API pro GitHub. Se uma vazar, apaga e gera outra.
