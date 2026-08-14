# AGENTS.md — Eclipse Bank V2

## Finalidade deste arquivo

Este documento transmite contexto entre instâncias do Codex que trabalham neste repositório. Ele não é um canal de comunicação em tempo real. Para continuar em outro computador, faça `git pull`, abra a raiz do projeto no Codex e peça que ele leia este arquivo e `Requisitos.md` antes de agir.

## Objetivo do projeto

Reconstruir o Eclipse Bank como projeto principal de portfólio, com backend Spring Boot, frontend web próprio, regras bancárias consistentes, segurança, testes, documentação, investimentos simulados e futuro aplicativo móvel.

## Forma de colaboração com Isabella

- Converse em português e explique o motivo das decisões enquanto ensina.
- Isabella escreve e aprende JavaScript; revise e oriente antes de alterar JS diretamente.
- O Codex pode criar e alterar HTML e CSS quando solicitado.
- No backend, prefira orientar em etapas; altere diretamente quando Isabella pedir.
- Preserve personalizações feitas por ela, como `nomeSocial`.
- Use mudanças pequenas, compile e teste antes de recomendar commit.
- Não faça `git push` sem pedido explícito.
- Nunca inclua banco local, senha, token, chave privada ou dados pessoais reais no Git.

## Estrutura

```text
backend/
  src/main/java/com/eclipsebank/backend/
    config/ controller/ dto/ enums/ exception/
    models/ repository/ service/
  src/main/resources/application.properties
  src/test/resources/application.properties
frontend/
  html/
  css/eclipse-bank.css
  script/
mobile/
  app/
    _layout.tsx
    index.tsx
    conta.tsx
    extrato.tsx
Requisitos.md
```

Pacote Java oficial: `com.eclipsebank.backend`. Não recriar o pacote antigo `com.ECLIPSE_BANK_V2`.

## Tecnologias e comandos

- Java 26
- Spring Boot 4.1.0
- Maven Wrapper
- H2 persistente no desenvolvimento
- H2 em memória isolado nos testes
- React Native com Expo SDK 54 e Expo Router
- Node.js 20.19.x para compatibilidade com o Expo SDK 54

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

O frontend é aberto pelo Live Server. Em desenvolvimento, os scripts montam a API com o host atual e a porta 8080 para evitar misturar `localhost` e `127.0.0.1`.

O aplicativo móvel fica em `mobile/` e é iniciado com:

```bash
cd mobile
npx expo start --lan
```

No celular, a URL da API deve usar o IPv4 do computador na rede local, não `localhost`. O backend precisa estar ligado e o celular deve estar na mesma rede Wi-Fi.

## Estado funcional atual

- Usuários: cadastro, listagem, consulta, atualização e desativação.
- E-mail e CPF únicos; senha armazenada com BCrypt.
- Spring Security instalado e configurado com `SecurityFilterChain`.
- Login web cria sessão no servidor e envia cookie `JSESSIONID` com `HttpOnly` e `SameSite=Lax`.
- O frontend web consulta `/api/usuarios/sessao`, envia `credentials: "include"` e não armazena mais o usuário no `localStorage` ou `sessionStorage`.
- Logout web invalida a sessão no servidor; a sessão expira após 30 minutos de inatividade.
- As rotas ainda usam `permitAll()` e o CSRF permanece temporariamente desativado durante a migração. A autorização ainda não está concluída.
- Erros padronizados: 400, 401, 404, 409, 422 e 500.
- Conta individual por usuário, saldo em `BigDecimal`, status e `@Version`.
- Criação e consulta de conta, com bloqueio, desbloqueio e encerramento condicionado ao saldo zerado.
- Depósito, saque, transferência e Pix por chave de e-mail transacionais.
- Extrato imutável e comprovante por UUID.
- Investimentos iniciados no backend: enum de tipos, entidade persistente de produto, repository, DTOs de cadastro/resposta e service com cadastro e listagem de produtos ativos.
- O catálogo de investimentos ainda não possui controller/endpoints nem testes específicos; carteira, aplicação e resgate ainda não foram iniciados.
- Telas: cadastro, login, painel, depósito, saque, transferência, Pix, extrato, comprovante e gerenciamento da conta.
- CSS consolidado em `frontend/css/eclipse-bank.css`.
- Aplicativo Expo iniciado e validado em aparelho Android real com Expo Go.
- Login mobile integrado ao endpoint existente e com tratamento de carregamento e erro.
- Painel mobile consulta ou cria a conta e exibe titular, saldo, agência e número.
- Extrato mobile integrado à API; Pix e transferência mobile ainda não foram implementados.

## Rotas existentes

```text
POST   /api/usuarios
POST   /api/usuarios/login
GET    /api/usuarios/sessao
POST   /api/usuarios/logout
GET    /api/usuarios
GET    /api/usuarios/{id}
PATCH  /api/usuarios/{id}
DELETE /api/usuarios/{id}

POST   /api/contas/usuario/{usuarioId}
GET    /api/contas/{id}
GET    /api/contas/usuario/{usuarioId}
GET    /api/contas/buscar?agencia=0001&numero=...
PATCH  /api/contas/{id}/bloqueio
PATCH  /api/contas/{id}/desbloqueio
PATCH  /api/contas/{id}/encerramento

POST   /api/contas/{contaId}/depositos
POST   /api/contas/{contaId}/saques
POST   /api/contas/{contaId}/transferencias
POST   /api/contas/{contaId}/pix
GET    /api/contas/{contaId}/extrato
GET    /api/movimentacoes/{codigo}
```

## Regras arquiteturais

- Dinheiro sempre em `BigDecimal`; nunca `double` ou `float`.
- O saldo não possui setter público. Use `Conta.creditar` e `Conta.debitar`.
- Alteração de saldo e registro de movimentação devem ocorrer na mesma transação.
- Movimentação concluída não deve ser editada nem apagada.
- DTOs de resposta nunca expõem senha, CPF ou entidades completas.
- Mensagem de login inválido deve continuar genérica.
- A restrição do banco é a defesa final para unicidade.
- Dados externos de investimentos serão informativos/simulados, não ordens reais.

## Segurança — situação e direção

O frontend web já usa sessão opaca mantida no servidor. O cookie é `HttpOnly`, usa `SameSite=Lax` e aceita `Secure=true` pela variável `SESSION_COOKIE_SECURE` em produção. O usuário não é mais salvo no armazenamento web.

A migração ainda não terminou: `SecurityConfig` usa `permitAll()` e desativa CSRF temporariamente. O próximo passo obrigatório é representar a sessão no `SecurityContext`, exigir autenticação nas rotas bancárias, validar o proprietário da conta e reativar CSRF. Não marcar “Autenticação com Spring Security” como concluída antes disso.

Não confiar em `usuarioId` vindo do navegador para autorizar contas ou movimentações. Antes de publicar, todas as rotas bancárias devem identificar o usuário autenticado no backend.

## Próximos passos recomendados

1. Criar o controller e os testes do catálogo de produtos de investimento.
2. Modelar a carteira e as aplicações, com débito transacional da conta corrente.
3. Integrar a sessão ao `SecurityContext`, proteger recursos por proprietário e reativar CSRF.
4. Implementar documentação OpenAPI, PostgreSQL e Docker conforme `Requisitos.md`.

## Atenções conhecidas

- Não execute duas instâncias do Spring na porta 8080.
- O banco H2 em arquivo aceita uma instância por vez; os testes já usam banco em memória separado.
- A chave Pix atual é o e-mail cadastrado; outros tipos de chave ainda não foram implementados.
- Após cadastro, direcionar para login; somente o endpoint de login cria a sessão autenticada.
- CORS com portas locais é configuração de desenvolvimento e deve ser restrito em produção.
- O login mobile ainda encaminha `usuarioId` como parâmetro de navegação; isso organiza o protótipo, mas não autoriza acesso. A proteção real ainda precisa ser integrada ao aplicativo.
- A URL mobile é centralizada em `mobile/config/api.ts`; cada computador usa `mobile/.env.local`, que não deve entrar no Git.
- Antes de escrever código dentro de `mobile/`, leia também `mobile/AGENTS.md` e consulte a documentação correspondente ao Expo SDK 54.

## Critério antes de commit

```bash
cd backend
./mvnw test
git status --short
```

Confirme que `backend/data/`, `backend/target/`, segredos e arquivos de IDE não estão rastreados.
