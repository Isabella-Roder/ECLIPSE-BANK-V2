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
Requisitos.md
```

Pacote Java oficial: `com.eclipsebank.backend`. Não recriar o pacote antigo `com.ECLIPSE_BANK_V2`.

## Tecnologias e comandos

- Java 26
- Spring Boot 4.1.0
- Maven Wrapper
- H2 persistente no desenvolvimento
- H2 em memória isolado nos testes

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

O frontend é aberto pelo Live Server. Em desenvolvimento, a API fica em `http://localhost:8080/api`.

## Estado funcional atual

- Usuários: cadastro, listagem, consulta, atualização e desativação.
- E-mail e CPF únicos; senha armazenada com BCrypt.
- Login básico validado no navegador, ainda sem Spring Security.
- Erros padronizados: 400, 401, 404, 409, 422 e 500.
- Conta individual por usuário, saldo em `BigDecimal`, status e `@Version`.
- Criação e consulta de conta.
- Depósito e saque transacionais.
- Extrato imutável e comprovante por UUID.
- Telas: cadastro, login, painel, depósito e saque.
- CSS consolidado em `frontend/css/eclipse-bank.css`.

## Rotas existentes

```text
POST   /api/usuarios
POST   /api/usuarios/login
GET    /api/usuarios
GET    /api/usuarios/{id}
PATCH  /api/usuarios/{id}
DELETE /api/usuarios/{id}

POST   /api/contas/usuario/{usuarioId}
GET    /api/contas/{id}
GET    /api/contas/usuario/{usuarioId}
GET    /api/contas/buscar?agencia=0001&numero=...

POST   /api/contas/{contaId}/depositos
POST   /api/contas/{contaId}/saques
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

O armazenamento atual em `localStorage`/`sessionStorage` é provisório e serve apenas à interface. Ele não autentica requisições. A próxima evolução de segurança deve usar Spring Security, sessão no servidor e cookie com `HttpOnly`, `Secure` e `SameSite`, além de CSRF e autorização por proprietário do recurso.

Não confiar em `usuarioId` vindo do navegador para autorizar contas ou movimentações. Antes de publicar, todas as rotas bancárias devem identificar o usuário autenticado no backend.

## Próximos passos recomendados

1. Implementar e testar `deposito.js` e `saque.js`.
2. Carregar atividade recente no painel a partir do extrato.
3. Criar testes automatizados de depósito, saque, rollback e saldo insuficiente.
4. Implementar transferência atômica entre contas.
5. Adicionar Spring Security e substituir armazenamento provisório por cookie seguro.
6. Implementar Pix, documentação OpenAPI, PostgreSQL e Docker conforme `Requisitos.md`.

## Atenções conhecidas

- Não execute duas instâncias do Spring na porta 8080.
- O banco H2 em arquivo aceita uma instância por vez; os testes já usam banco em memória separado.
- `frontend/script/deposito.js` e `frontend/script/saque.js` ainda precisam ser implementados.
- Após cadastro, o fluxo ideal é direcionar para login ou criar uma sessão real; não tratar o objeto no storage como prova de autenticação.
- CORS com portas locais é configuração de desenvolvimento e deve ser restrito em produção.

## Critério antes de commit

```bash
cd backend
./mvnw test
git status --short
```

Confirme que `backend/data/`, `backend/target/`, segredos e arquivos de IDE não estão rastreados.
