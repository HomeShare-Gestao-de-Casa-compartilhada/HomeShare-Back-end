# HomeShare - Back-end (API)

API REST para gestão de casas compartilhadas: moradores, tarefas, calendário, reservas de espaços, gastos fixos, avisos, regras, contatos e lista de compras.

**Stack:** Java 26 · Spring Boot 4.1.2 · Spring Security (JWT) · Spring Data JPA/Hibernate · PostgreSQL · Flyway · Redis · Resend (e-mail) · Lombok · Maven.

## Requisitos
- JDK 26 (se o Lombok reclamar, use 25 e troque `java.version` no `pom.xml`)
- Docker Desktop (aberto) - sobe Postgres e Redis pelo `compose.yaml`
- IntelliJ IDEA (Maven já vem incluído)
- Conta no [Resend](https://resend.com) (opcional para testes: veja `MAIL_PROVIDER=log`)

## Configuração (.env)
1. Copie `.env.example` para `.env` na raiz do projeto (o `.env` **nunca** vai para o GitHub).
2. Preencha as variáveis. O Spring lê o `.env` sozinho (`spring.config.import`), sem plugin.

| Variável | Para quê | Exemplo |
|---|---|---|
| `JWT_SECRET` | Assina os tokens (mín. 32 caracteres). Gere: `openssl rand -hex 48` | `a1b2...` |
| `MAIL_PROVIDER` | `resend` envia de verdade; `log` só imprime o token no console | `resend` |
| `RESEND_API_KEY` | Chave da API do Resend | `re_...` |
| `MAIL_FROM` | Remetente | `HomeShare <onboarding@resend.dev>` |
| `MAIL_OVERRIDE_TO` | Todo e-mail vai para este endereço (modo teste do Resend). Vazio = envia ao destinatário real | `seu@gmail.com` |
| `FRONTEND_URL` | Base dos links dos e-mails e dos convites | `http://localhost:3000` |
| `CORS_ALLOWED_ORIGINS` | Origens do front, separadas por vírgula | `http://localhost:3000` |

Formato do `.env`: `CHAVE=valor`, sem aspas e sem espaços ao redor do `=`. O nome do arquivo é só `.env`.

## Como rodar
1. Abra o Docker Desktop.
2. Abra o projeto no IntelliJ e carregue o Maven (*Load Maven Project*).
3. Rode a classe `com.homeshare.HomeShareApplication`.
4. A API sobe em http://localhost:8080. Na primeira vez o Flyway cria as tabelas.
5. Teste pelo arquivo `http/homeshare.http` (ambiente `dev`) ou pelo front-end.

Dica: ao parar a aplicação, o Docker é só pausado e seus dados ficam. **Não use `docker compose down`** (apaga o banco). Para zerar de propósito: `docker compose down -v`.

Testes: `mvn test` (unitários, não precisam de Docker).

## Estrutura
```
src/main/java/com/homeshare/
  config/       propriedades, JWT, segurança/CORS, relógio
  controller/   rotas REST
  dto/          records de entrada/saída (com validação)
  email/        envio (Resend ou log)
  entity/       entidades JPA (14, espelham o ER v4)
  enums/        status e tipos
  exception/    BusinessException + tratamento de erros (ProblemDetail)
  repository/   Spring Data JPA
  security/     JwtService, handlers 401/403, SecurityUtils
  service/      regras de negócio
  util/         utilitários
src/main/resources/
  application.yaml, application-prod.yaml
  db/migration/V1__criar_schema_inicial.sql
http/           requisições de teste do IntelliJ
```

## Como funciona
**Cadastro (Redis):** `POST /api/auth/register` guarda o cadastro (senha já em hash) no Redis por 15 min e envia um token por e-mail; `POST /api/auth/confirm` com o token grava o usuário no PostgreSQL (token de uso único). `POST /api/auth/login` devolve o JWT (2 h).

**Convites (entrar numa casa):** o líder gera um convite em `POST /api/casas/{id}/convites`. A resposta já traz `link` pronto (`FRONTEND_URL/?convite=CODIGO`) e `codigo`. Quem recebe abre o link, faz login/cadastro e entra com `POST /api/convites/{codigo}/entrar`. O convite é de uso único, tem validade (padrão 48 h, máx. 30 dias) e pode ser cancelado.

**Regras principais:** o morador só acessa a casa em que mora; o líder é único; se o líder sai, o morador mais antigo assume (sem ninguém, a casa é encerrada); reservas não podem se sobrepor; a soma das parcelas de um gasto é igual ao total. Lista completa em `ROADMAP.md`.

## Rotas
Todas exigem `Authorization: Bearer <token>`, exceto `/api/auth/**`. Tabela completa no `ROADMAP.md` (seção 4) e exemplos prontos em `http/homeshare.http`.

Erros seguem o formato `ProblemDetail` (`title`, `status`, `detail`; validação traz também `erros` por campo).

## Produção
- Ative o perfil: `SPRING_PROFILES_ACTIVE=prod` (desliga o Docker Compose).
- Defina `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` e `SPRING_DATA_REDIS_HOST/PORT/PASSWORD`.
- Use `MAIL_PROVIDER=resend` com domínio verificado e deixe `MAIL_OVERRIDE_TO` vazio.
- `JWT_SECRET` forte e `CORS_ALLOWED_ORIGINS` com o domínio real do front.

## Problemas comuns
| Erro | Causa |
|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` | `.env` ausente ou rodando fora da raiz do projeto |
| `missing table [xxx]` | sobrou uma `@Entity` antiga no projeto: só as 14 de `entity/` devem existir |
| `Could not find or load main class` | confira a classe `HomeShareApplication` e rode `Maven > clean` |
| Falha de conexão (Postgres/Redis) | Docker Desktop fechado |
| `RESEND_API_KEY não configurada` | preencha a chave ou use `MAIL_PROVIDER=log` |
| 403 do Resend | remetente/domínio não verificado ou chave inválida |
| 401 em tudo | token expirado ou sem `Bearer` |

## Segurança
Chaves e senhas só no `.env` (ignorado pelo Git). Se alguma chave vazar, apague e gere outra. Senhas ficam em BCrypt; tokens de cadastro ficam no Redis apenas como hash.
