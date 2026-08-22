# Requisitos — Eclipse Bank V2

## Objetivo

Reconstruir o Eclipse Bank com arquitetura organizada, regras bancárias seguras e qualidade suficiente para ser o projeto principal do portfólio.

## Regras técnicas

- [x] Backend iniciado com Java 26, Spring Boot e Maven
- [x] Separação em `model`, `repository`, `dto`, `service`, `controller` e `exception`
- [x] Banco H2 persistente para desenvolvimento local
- [x] PostgreSQL para produção
- [x] Valores monetários representados com `BigDecimal`
- [x] Senhas armazenadas somente com hash BCrypt
- [x] Validação dos dados recebidos pela API
- [x] Tratamento global e padronizado de erros
- [ ] Testes unitários e de integração das regras críticas
- [x] Documentação da API com OpenAPI/Swagger
- [x] Docker para aplicação e banco de produção

## 1. Usuários e autenticação

- [x] Cadastro de pessoa física
- [x] E-mail e CPF únicos
- [x] Login básico com comparação segura de senha BCrypt
- [x] Bloqueio de login para usuário desativado
- [x] Perfis de cliente e administrador definidos
- [x] Consulta, atualização e desativação de usuário
- [x] Autenticação e autorização com Spring Security

## 2. Contas bancárias

- [x] Criação automática de conta para o cliente no primeiro acesso ao painel
- [x] Agência e número de conta únicos
- [x] Consulta de saldo
- [x] Bloqueio, desbloqueio e encerramento de conta com validação de saldo
- [x] Impedir saldo negativo fora das regras permitidas

## 3. Movimentações

- [x] Depósito
- [x] Saque
- [x] Transferência entre contas
- [x] Pix por chave de e-mail
- [x] Validação de saldo antes de débitos
- [x] Operações atômicas com `@Transactional`
- [ ] Proteção contra envio duplicado da mesma operação

## 4. Extrato e comprovantes

- [x] Registrar toda movimentação em histórico
- [x] Listar extrato por período
- [x] Identificar crédito, débito, data, descrição e saldo resultante
- [x] Gerar comprovante com código único
- [x] Não disponibilizar exclusão ou alteração de movimentação concluída pela API

## 5. Segurança

- [x] Senhas nunca retornadas pela API
- [x] Mensagem genérica para e-mail ou senha inválidos
- [x] Respostas `400`, `401`, `404`, `409` e `500` padronizadas
- [x] CORS separado em configuração própria para desenvolvimento
- [x] Renderização segura de dados dinâmicos do extrato sem interpolação em `innerHTML`
- [x] Spring Security integrado ao backend com `SecurityFilterChain`
- [x] Autenticação com Spring Security
- [x] Sessão autenticada por identificador opaco no servidor
- [x] Cookie de sessão com `HttpOnly`, `SameSite` e `Secure` configurável para produção
- [x] Não armazenar usuário ou token de autenticação no `localStorage` ou `sessionStorage`
- [x] Proteção CSRF nas operações que alteram dados
- [x] Logout com invalidação da sessão no servidor
- [x] Expiração da sessão após 30 minutos de inatividade
- [ ] Limite absoluto de duração da sessão
- [x] Renovação do identificador de sessão após o login
- [ ] Rate limiting para login, Pix, transferências e recuperação de senha
- [x] Proteção contra tentativa de acesso a recursos de outro cliente
- [x] Cabeçalhos CSP, HSTS, `X-Content-Type-Options` e política de referência
- [ ] HTTPS obrigatório em produção
- [ ] Segredos e credenciais somente em variáveis de ambiente
- [ ] Logs de auditoria sem senha, token, CPF completo ou dados bancários sensíveis
- [x] Testes de autorização, CSRF, sessão expirada e força bruta (linha de base, sem rate limiting ainda)
- [ ] Verificação de dependências vulneráveis no pipeline
- [ ] Autenticação em dois fatores como evolução futura

## 6. Frontend

- [x] Tela responsiva de cadastro
- [x] Cadastro integrado à API
- [x] Tela responsiva de login
- [x] Login validado de ponta a ponta no navegador
- [x] Painel com saldo e resumo da conta
- [x] Tela de depósito e saque
- [x] Tela de transferência e Pix
- [x] Extrato com filtros
- [x] Comprovante de operação
- [x] Lista de movimentações recentes no painel
- [x] Ocultar e restaurar a visualização do saldo no painel
- [x] Máscaras de CPF, telefone, dados bancários e valores monetários
- [x] Tela de gerenciamento para bloquear, desbloquear e encerrar conta
- [x] Tela de investimentos com catálogo, aplicação, carteira e resgate
- [x] Tela de empréstimos com simulação, solicitação e aprovação
- [x] Layout responsivo e identidade visual própria

## 7. Qualidade e portfólio

- [x] README com apresentação, imagens e instruções de execução
- [ ] Commits pequenos e descritivos
- [ ] Testes para saldo insuficiente, duplicidade e concorrência
- [ ] Coleção de requisições para demonstração da API
- [x] Dados de demonstração sem informações pessoais reais
- [x] Pipeline de integração contínua no GitHub

## Funcionalidades futuras

- [x] Cartões (débito e crédito)
  - [x] Enums `TipoCartao` (débito/crédito) e `StatusCartao` (ativo/bloqueado/cancelado)
  - [x] Entidade com regras de bloqueio, desbloqueio e cancelamento
  - [x] Repository e DTOs de cadastro e resposta
  - [x] Service e controller com autorização por proprietário da conta e do cartão
  - [x] Testes de regra de negócio e orquestração do service
  - [x] Tela web de listagem, criação, bloqueio, desbloqueio e cancelamento
  - [x] Fatura: entidade com lançamento de compra, fechamento e pagamento
  - [x] Repository, DTOs, service e controller de fatura com autorização por conta e cartão
  - [x] Testes de fatura (entidade e service)
  - [x] Tela web de fatura completa: lançar compra, fechar e pagar
  - [x] Entidade `Compra` com valor, descrição e categoria individuais por lançamento
- [x] Metas financeiras (criação, aporte e resgate manuais)
  - [x] Entidade com regras de aporte/resgate e conclusão automática ao atingir o valor alvo
  - [x] DTOs validados de cadastro, requisição e resposta
  - [x] Repository, service e controller com autorização por proprietário da conta
  - [x] Débito/crédito da conta e registro no extrato na mesma transação
  - [x] Testes de regra de negócio, orquestração do service e saldo insuficiente
  - [x] Tela web de criação, listagem, aporte e resgate com barra de progresso
  - [ ] Aportes automáticos simulados (fora do escopo desta etapa)
- [x] Empréstimos
  - [x] Entidades `Emprestimo` e `Parcela` com estados e regras de negócio
  - [x] DTOs validados, repositories, service e controller com autorização por proprietário da conta
  - [x] Cálculo do valor total, juros e geração automática das parcelas
  - [x] Aprovação com crédito na conta e registro no extrato na mesma transação
  - [x] Pagamento de parcela com débito da conta e quitação automática do empréstimo
  - [x] Testes das entidades e da orquestração do service
  - [x] Tela web responsiva com simulação, solicitação e aprovação
- [ ] Investimentos
- [ ] Contas empresariais
- [ ] Dashboard separado para conta pessoa física e conta empresarial
- [ ] Módulo de analytics em Python para relatórios financeiros
- [ ] Conversão de moedas

## 8. Investimentos e dados de mercado

- [x] Catálogo de ativos por tipo: renda fixa, fundos, ações, ETFs e criptomoedas
  - [x] Enum com os tipos de investimento
  - [x] Entidade persistente e repository de produtos
  - [x] DTOs validados de cadastro e resposta
  - [x] Service para cadastrar e listar produtos ativos em ordem alfabética
  - [x] Controller e endpoints do catálogo
  - [x] Testes específicos do catálogo
- [x] Carteira de investimentos separada da conta corrente
- [x] Aplicação e resgate simulados, sem executar ordens financeiras reais
  - [x] Débito da conta e registro da aplicação na mesma transação
  - [x] Resgate parcial ou total com crédito na conta
  - [x] Registro de aplicação e resgate no extrato bancário
  - [x] Testes de saldo insuficiente e resgate inválido
  - [x] Teste de rollback (integração com transação real, `AplicacaoInvestimentoRollbackTest`)
- [x] Posição consolidada com quantidade, preço médio e valor atual
- [x] Cálculo de rentabilidade nominal e percentual
- [x] Atualização simulada do saldo investido por taxa e período, sem duplicar rendimentos
- [x] Fundos imobiliários com preço da cota e quantidade de cotas adquiridas
- [ ] Cálculo e manutenção do preço médio dos fundos imobiliários
- [x] Proventos simulados de fundos imobiliários com histórico próprio
  - [x] Cálculo mensal do provento pela quantidade de cotas
  - [x] Crédito do provento na conta e registro no extrato
  - [x] Proteção contra pagamento duplicado para a mesma aplicação no mesmo mês
  - [x] Testes do cálculo, pagamento e duplicidade mensal
  - [x] Recebimento de provento pela carteira no frontend web
- [ ] Histórico de aportes, resgates, proventos e rendimentos
- [ ] Lista de ativos favoritos
- [ ] Simulador de investimento por prazo e rentabilidade
- [ ] Gráficos de evolução da carteira e distribuição por categoria
- [ ] Integração com API externa para cotações e histórico de preços
- [ ] Integração com API de câmbio para ativos internacionais
- [ ] Exibir fonte, moeda e horário da última atualização de cada cotação
- [ ] Cache de cotações para reduzir chamadas, latência e custo
- [ ] Tratamento de indisponibilidade e limite de requisições da API externa
- [ ] Camada própria de integração para permitir a troca do provedor de dados
- [ ] Chaves das APIs externas armazenadas somente no backend
- [ ] Dados simulados como alternativa quando o provedor estiver indisponível
- [x] Aviso claro de caráter educacional e ausência de recomendação financeira
- [ ] Verificação dos termos de uso e licença dos dados antes da publicação

## 9. Aplicativo móvel

- [x] Tecnologia definida: React Native com Expo SDK 54 e Expo Router
- [x] Estrutura inicial do aplicativo criada em `mobile/`
- [x] Login mobile integrado à API de desenvolvimento
- [x] Painel mobile com titular, saldo, agência e número da conta
- [x] Carregamento, mensagens de erro e nova tentativa no painel mobile
- [x] Consumir a mesma API segura utilizada pelo frontend web (sessão + CSRF via `GET /api/csrf`)
- [ ] Cadastro mobile
- [x] Pix e transferências mobile
- [x] Extrato mobile integrado à API
- [ ] Cartões mobile
- [x] Investimentos mobile com catálogo, carteira, aplicação e resgate
- [ ] Interface adaptada para diferentes tamanhos de tela
- [ ] Tema claro e escuro mantendo a identidade do Eclipse Bank
- [ ] Armazenamento seguro de credenciais no Keychain/Keystore
- [ ] Nunca armazenar senha ou token em armazenamento comum do aplicativo
- [ ] Desbloqueio local com biometria
- [ ] Encerramento e expiração de sessão
- [ ] Notificações de login e movimentações
- [ ] Ocultar valores quando o aplicativo entrar em segundo plano
- [ ] Impedir informações sensíveis em logs e mensagens de erro
- [x] Protótipo executado e testado em Android real com Expo Go
- [ ] Testes em emulador Android
- [ ] Pipeline de build separado para desenvolvimento e produção

## 10. Evolução do ecossistema bancário

- [ ] Chaves Pix e gerenciamento de favorecidos
- [ ] Agendamento e recorrência de pagamentos
- [ ] Cartão virtual, limites, bloqueio e fatura
- [ ] Metas com aportes automáticos simulados
- [x] Empréstimos com simulação de parcelas e juros
- [ ] Notificações e central de atividades da conta
- [ ] Painel administrativo com autorização própria
- [ ] Detecção de movimentações suspeitas por regras
- [ ] Auditoria imutável de ações críticas
- [ ] Métricas, monitoramento de saúde e alertas da aplicação
- [ ] Open Finance somente após análise de segurança e conformidade
- [x] Exportar extrato e comprovante em PDF ou CSV
- [ ] Categorização de gastos com resumo mensal
- [ ] Notificação por e-mail em operações sensíveis (login novo, Pix enviado)
- [x] Página "sobre o projeto" explicando decisões técnicas e arquiteturais
- [ ] Conta conjunta ou múltiplos titulares
- [ ] Histórico de dispositivos/sessões ativas com opção de encerrar todas

## Ordem de entrega (roadmap)

- [x] Fase 1 — autenticação segura, conta, saldo e extrato
- [x] Fase 2 — depósito, saque, transferência e Pix
- [x] Fase 5 — carteira de investimentos, metas financeiras, posição consolidada e rentabilidade
- [x] Fase 7 — aplicativo móvel (login, painel, extrato, Pix, transferência, investimentos, sessão + CSRF)
  - [ ] Cadastro mobile e demais itens da seção 9 ainda pendentes
- [x] Fase 3 — testes, documentação, Docker e PostgreSQL
  - [x] Teste de rollback de investimentos
  - [x] Testes de segurança (CSRF, sessão expirada, força bruta)
  - [x] Documentação OpenAPI/Swagger
  - [x] Docker (aplicação + banco) e migração para PostgreSQL
- [x] Fase 4 — frontend completo e publicação da demonstração
  - [x] Frontend web empacotado no JAR e servido pelo Spring Boot no mesmo domínio da API
  - [x] Entrada da aplicação disponível em `/` com redirecionamento para o login
  - [x] Scripts web compatíveis com Live Server no desenvolvimento e `/api` no Docker/produção
  - [x] Execução local da aplicação completa com Docker e PostgreSQL validada
  - [x] README com apresentação, arquitetura, segurança e instruções de execução via Docker
  - [x] Screenshots do frontend web e aplicativo mobile no README
  - [x] Dados de demonstração sem informações pessoais reais
  - [x] Publicação/deploy da demonstração (Render, `https://eclipse-bank-v2.onrender.com/`)
- [ ] Fase 6 — integração com dados externos de mercado
  - [ ] Selic/CDI/IPCA via API do Banco Central
  - [ ] Cotações de ações e FIIs via provedor terceiro
  - [ ] Câmbio para ativos internacionais
- [ ] Fase 8 — cartões, crédito, antifraude e módulos avançados
  - [x] Cartões e faturas
  - [x] Empréstimos
  - [ ] Contas empresariais
  - [ ] Dashboard PF/PJ, analytics em Python
  - [ ] Painel administrativo, auditoria imutável, detecção de fraude
