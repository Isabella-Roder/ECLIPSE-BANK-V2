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

No Docker, `frontend/` é copiado para `src/main/resources/static` durante o build e servido pelo próprio Spring Boot. A aplicação completa fica em `http://localhost:8080/`, e os scripts usam `/api` no mesmo domínio.

```bash
docker compose up --build
docker compose down
```

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
- Cadastro e login são públicos; as demais rotas exigem sessão autenticada. CSRF reativado em todas as operações mutáveis, e a autorização por proprietário da conta já está validada nas rotas bancárias e de investimentos.
- Erros padronizados: 400, 401, 404, 409, 422 e 500.
- Conta individual por usuário, saldo em `BigDecimal`, status e `@Version`.
- Criação e consulta de conta, com bloqueio, desbloqueio e encerramento condicionado ao saldo zerado.
- Depósito, saque, transferência e Pix por chave de e-mail transacionais.
- Extrato imutável e comprovante por UUID.
- Catálogo de investimentos com produtos iniciais de renda fixa, fundo, ação, ETF e criptomoeda, além de endpoints de cadastro e listagem de produtos ativos.
- Carteira de investimentos persistente com aplicação e resgate parcial ou total; débito/crédito da conta e registro no extrato ocorrem na mesma transação.
- Tela web de investimentos integrada ao catálogo, aplicação, carteira e resgate. Os dados são simulados e exibem aviso educacional.
- O catálogo possui testes unitários de cadastro válido, código duplicado e listagem de produtos ativos.
- Testes de saldo insuficiente na aplicação e resgate inválido (`AplicacaoInvestimentoTest`, `AplicacaoInvestimentoServiceTest`); falta apenas teste de rollback (requer integração com transação real).
- Metas financeiras: entidade com aporte, resgate e conclusão automática ao atingir o valor alvo; repository, DTOs, service e controller com autorização por proprietário da conta; débito/crédito da conta e registro no extrato na mesma transação.
- Metas financeiras possuem testes de regra de negócio (entidade), orquestração do service e saldo insuficiente no aporte.
- Tela web de metas financeiras integrada à API: criação, listagem, aporte e resgate, com barra de progresso.
- Telas: cadastro, login, painel, depósito, saque, transferência, Pix, extrato, comprovante, gerenciamento da conta, investimentos e metas financeiras.
- O frontend web é empacotado no JAR pelo Docker, servido no mesmo domínio da API e possui entrada em `/`.
- O Compose com aplicação e PostgreSQL foi construído e executado localmente com sucesso.
- O README apresenta funcionalidades, arquitetura, segurança, Docker, desenvolvimento local, mobile, testes, variáveis de ambiente e roadmap; faltam screenshots e a futura URL pública.
- CSS consolidado em `frontend/css/eclipse-bank.css`.
- Aplicativo Expo iniciado e validado em aparelho Android real com Expo Go.
- Login mobile integrado ao endpoint existente e com tratamento de carregamento e erro.
- Painel mobile consulta ou cria a conta e exibe titular, saldo, agência e número.
- Extrato, Pix e transferência mobile integrados à API.
- Investimentos mobile integrados à mesma API: catálogo, carteira, aplicação e resgate parcial ou total.
- O extrato mobile reconhece aplicação como débito e resgate de investimento como crédito.
- Posição consolidada da carteira de investimentos (quantidade, valor aplicado, valor atual e preço médio por produto) e cálculo de rentabilidade nominal/percentual, exibidos na tela web de investimentos.
- Mobile: como o React Native não expõe o cabeçalho `Set-Cookie` da resposta, o token CSRF é obtido via `GET /api/csrf` (retorna `{ token }` no corpo) antes de cada operação que muda dado, em vez de ser lido do cookie como no frontend web.
- Cartões: entidade com número gerado por `SecureRandom`, tipo (débito/crédito, limite fixo de R$ 1.000 para crédito) e status (ativo/bloqueado/cancelado); service e controller validam que o cartão pertence à conta autenticada, além da autorização por proprietário da conta. Tela web (`cartoes.html`/`cartoes.js`) integrada: listagem, criação, bloqueio, desbloqueio e cancelamento.
- Fatura de cartão de crédito: `Fatura` agrupa por mês (`lançarCompra`, `fechar`, `pagar`), débito da conta e registro no extrato (`PAGAMENTO_FATURA`) ocorrem na mesma transação; `Compra` guarda cada lançamento individual (valor, descrição, categoria) vinculado à fatura. Autorização valida dono da conta e que o cartão pertence à conta. Tela web (`faturas.html`/`faturas.js`), acessada pelo botão "Ver faturas" nos cartões de crédito: lançar compra, fechar e pagar fatura.
- Faturas: entidade com lançamento de compra, fechamento e pagamento; repository, DTOs, service e controller protegidos por autorização da conta e do cartão, além de testes de entidade e service. A tela web permite visualizar e pagar a fatura; ainda falta lançar compras e fechar a fatura pela interface.
- Empréstimos: `Emprestimo` (valor solicitado, taxa de juros simples, quantidade de parcelas, valor total calculado no service) e `Parcela` (número, valor, vencimento, status, data de pagamento); ciclo `SOLICITADO → APROVADO → EM_ANDAMENTO/QUITADO`, com `INADIMPLENTE` reservado para uso futuro. Aprovar credita o valor na conta; pagar parcela debita e registra movimentação (`EMPRESTIMO_LIBERADO`, `PAGAMENTO_PARCELA_EMPRESTIMO`); o empréstimo é quitado automaticamente quando a última parcela é paga. Service e controller protegidos por autorização da conta e por checagem de posse do empréstimo/parcela. Testes de entidade (`EmprestimoTest`, `ParcelaTest`) e service (`EmprestimoServiceTest`), incluindo casos de acesso indevido. Tela web (`emprestimos.html`/`emprestimos.js`) integrada: simulação, solicitação, aprovação, listagem e pagamento de parcelas.
- Contas empresariais: `Empresa` (CNPJ único, razão social, nome fantasia opcional, vinculada a um `usuarioResponsavel`) não conhece `Conta` — o relacionamento é de mão única (só `Conta` aponta pra `Usuario`/`Empresa`), evitando dependência circular na criação. `Conta` ganhou o campo `tipo` (`PESSOA_FISICA`/`PESSOA_JURIDICA`, default PF) e passou a aceitar `usuario` **ou** `empresa`, nunca os dois. A resolução do dono da conta (`ContaRepository.buscarUsuarioIdPelaContaId`) usa `LEFT JOIN` explícito nos dois caminhos (usuário direto ou responsável da empresa) — **cuidado**: navegação encadeada implícita (`c.usuario.id`, `c.empresa.usuarioResponsavel.id`) sem `left join` explícito gera `INNER JOIN` por padrão no Hibernate e elimina a linha quando o lado oposto é nulo. Isso torna toda autorização por conta (Cartões, Faturas, Empréstimos, etc.) automaticamente compatível com contas PJ. Cadastro de empresa (`POST /api/empresas`) exige usuário autenticado, que vira o responsável; criação de conta PJ exige ser esse responsável. Testes de service (`EmpresaServiceTest`, `ContaServiceTest`). Telas web: `cadastro-empresa.html`/`.js` (cadastro + abertura de conta em um fluxo só) e dashboards (`dashboard-pessoa-fisica.html`/`.js`, `dashboard-empresarial.html`/`.js`).
- Painel administrativo: `AdminController` sob `/api/admin/**`, protegido inteiramente por uma única regra `hasRole("ADMIN")` no `SecurityConfig` (não por checagem manual nos métodos, diferente do resto da API). Endpoints atuais: listar todos os usuários, listar todas as contas, bloquear/desbloquear qualquer conta, listar auditoria. Reaproveita `usuarioService.listar()` e o novo `contaService.listarTodas()`. Para promover alguém a `ADMIN` localmente: `UPDATE USUARIOS SET PERFIL='ADMIN' WHERE ID=...` no H2 local (backend parado) — o perfil vira autoridade da sessão só no login seguinte, então é preciso deslogar e logar de novo depois de promover. Ainda sem tela web dedicada (só as rotas de API); detecção de movimentações atípicas continua pendente. **Pendência de segurança relacionada**: `GET /api/usuarios`, `GET /api/usuarios/{id}`, `PATCH /api/usuarios/{id}` e `DELETE /api/usuarios/{id}` ainda não estão restritas a admin — qualquer usuário autenticado consegue listar/ver/editar/desativar qualquer outra conta (ver seção 5 do Requisitos.md).
- Auditoria imutável: `RegistroAuditoria` (`usuarioId` nullable, `acao` enum `AcaoAuditoria`, `descricao`, `criadoEm`) só tem construtor e getters — nenhum setter além da criação, garantindo que um registro nunca é editado depois de salvo; o repository também não expõe nenhuma operação de update. `AuditoriaService.registrar(...)` é chamado manualmente nos pontos sensíveis: `UsuarioService.login` (sucesso e falha, com `usuarioId` nulo se o e-mail nem existir) e `ContaService.bloquear`/`desbloquear` (usando `buscarUsuarioIdPelaContaId` pra resolver o dono em contas PF e PJ). Consulta só pelo admin, em `GET /api/admin/auditoria` — **atenção**: o endpoint de consulta precisa morar dentro do `AdminController` (ou de outro controller coberto pela regra `/api/admin/**`), nunca num controller próprio com rota fora desse prefixo, senão fica exposto a qualquer usuário autenticado.
- Detecção de login suspeito: em `UsuarioService.login`, antes de checar a senha, conta quantos `LOGIN_FALHA` o usuário teve nos últimos 15 minutos (`RegistroAuditoriaRepository.countByUsuarioIdAndAcaoAndCriadoEmAfter`); com 5 ou mais, registra `LOGIN_BLOQUEADO_SUSPEITA` e recusa o login mesmo com senha correta, sempre pela mesma `CredenciaisInvalidasException` genérica (nunca revela ao atacante que foi bloqueado por suspeita, e não por senha errada).

## Rotas existentes

```text
POST   /api/usuarios
POST   /api/usuarios/login
GET    /api/usuarios/sessao
GET    /api/csrf
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

POST   /api/investimentos/produtos
GET    /api/investimentos/produtos
POST   /api/contas/{contaId}/investimentos/aplicacoes
GET    /api/contas/{contaId}/investimentos/carteira
POST   /api/contas/{contaId}/investimentos/{aplicacaoId}/resgates
GET    /api/contas/{contaId}/investimentos/posicao

POST   /api/contas/{contaId}/metas-financeiras/criar
GET    /api/contas/{contaId}/metas-financeiras/minhas-metas
POST   /api/contas/{contaId}/metas-financeiras/{metaId}/aportar
POST   /api/contas/{contaId}/metas-financeiras/{metaId}/resgatar

POST   /api/contas/{contaId}/cartoes/criar
GET    /api/contas/{contaId}/cartoes/meus-cartoes
PATCH  /api/contas/{contaId}/cartoes/{id}/bloquear
PATCH  /api/contas/{contaId}/cartoes/{id}/desbloquear
PATCH  /api/contas/{contaId}/cartoes/{id}/cancelar

POST   /api/contas/{contaId}/cartoes/{cartaoId}/faturas/compras
GET    /api/contas/{contaId}/cartoes/{cartaoId}/faturas/minha-fatura
PATCH  /api/contas/{contaId}/cartoes/{cartaoId}/faturas/{faturaId}/fechar
POST   /api/contas/{contaId}/cartoes/{cartaoId}/faturas/{faturaId}/pagar

POST   /api/contas/{contaId}/emprestimos
GET    /api/contas/{contaId}/emprestimos
PATCH  /api/contas/{contaId}/emprestimos/{emprestimoId}/aprovar
GET    /api/contas/{contaId}/emprestimos/{emprestimoId}/parcelas
POST   /api/contas/{contaId}/emprestimos/{emprestimoId}/parcelas/{parcelaId}/pagar

POST   /api/empresas
GET    /api/empresas/minhas-empresas
POST   /api/contas/empresa/{empresaId}
GET    /api/contas/empresa/{empresaId}

GET    /api/admin/usuarios
GET    /api/admin/contas
PATCH  /api/admin/contas/{id}/bloqueio
PATCH  /api/admin/contas/{id}/desbloqueio
GET    /api/admin/auditoria
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

A sessão já é representada no `SecurityContext`, as rotas não públicas exigem autenticação, CSRF está reativado em todas as operações mutáveis (web e mobile) e a autorização por proprietário da conta está validada nas rotas bancárias e de investimentos.

Não confiar em `usuarioId` vindo do navegador (ou do app mobile) para autorizar contas ou movimentações. Toda rota bancária identifica o usuário autenticado no backend.

## Próximos passos recomendados

1. Criar dados fictícios com `@Profile("demo")`, senha recebida por variável de ambiente e nenhuma informação pessoal real.
2. Adicionar screenshots web/mobile e as futuras credenciais da demonstração ao README.
3. Escolher uma plataforma e publicar a demonstração com HTTPS, aplicação e PostgreSQL.
4. Testar cadastro, login e operações bancárias na URL pública antes de marcar a Fase 4 como concluída.

## Atenções conhecidas

- Não execute duas instâncias do Spring na porta 8080.
- O banco H2 em arquivo aceita uma instância por vez; os testes já usam banco em memória separado.
- O enum persistente de `movimentacoes.tipo` no H2 local foi atualizado manualmente para aceitar `APLICACAO_INVESTIMENTO`, `RESGATE_INVESTIMENTO`, `PROVENTO_FII`, `APORTE_META_FINANCEIRA`, `RESGATE_META_FINANCEIRA`, `PAGAMENTO_FATURA`, `EMPRESTIMO_LIBERADO` e `PAGAMENTO_PARCELA_EMPRESTIMO`; futuras mudanças de esquema devem usar migrations. Cada novo valor de `TipoMovimentacao` exige rodar `ALTER TABLE MOVIMENTACOES ALTER COLUMN TIPO ENUM(...)` no `backend/data/eclipsebank` local (com o backend parado) via `org.h2.tools.RunScript`, senão o pagamento/registro falha com `22030` (H2 `JdbcSQLDataException`).
- A chave Pix atual é o e-mail cadastrado; outros tipos de chave ainda não foram implementados.
- Após cadastro, direcionar para login; somente o endpoint de login cria a sessão autenticada.
- CORS com portas locais é configuração de desenvolvimento e deve ser restrito em produção.
- Em produção, frontend e API devem permanecer no mesmo domínio para preservar o fluxo atual de sessão e CSRF.
- O `.env` é ignorado pelo Git; nunca colocar `DB_PASSWORD` ou a futura senha demo em arquivos rastreados.
- O login mobile ainda encaminha `usuarioId` como parâmetro de navegação; isso organiza o protótipo, mas não autoriza acesso. A proteção real ainda precisa ser integrada ao aplicativo.
- `mobile/app/conta.tsx` carrega o saldo apenas na montagem; ao voltar de aplicação ou resgate, o valor pode ficar visualmente desatualizado até a tela ser recarregada. Usar `useFocusEffect` como próximo ajuste.
- O React Native não expõe o cabeçalho `Set-Cookie` da resposta (diferente do navegador), então o mobile não pode ler o token CSRF do cookie como o frontend web faz. A solução foi criar `GET /api/csrf`, que devolve o token no corpo da resposta; o mobile busca um token novo antes de cada operação que muda dado.
- A URL mobile é centralizada em `mobile/config/api.ts`; cada computador usa `mobile/.env.local`, que não deve entrar no Git.
- Antes de escrever código dentro de `mobile/`, leia também `mobile/AGENTS.md` e consulte a documentação correspondente ao Expo SDK 54.

## Critério antes de commit

```bash
cd backend
./mvnw test
git status --short
```

Confirme que `backend/data/`, `backend/target/`, segredos e arquivos de IDE não estão rastreados.
