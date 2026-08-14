# Requisitos — Eclipse Bank V2

## Objetivo

Reconstruir o Eclipse Bank com arquitetura organizada, regras bancárias seguras e qualidade suficiente para ser o projeto principal do portfólio.

## Regras técnicas

- [x] Backend iniciado com Java 26, Spring Boot e Maven
- [x] Separação em `model`, `repository`, `dto`, `service`, `controller` e `exception`
- [x] Banco H2 persistente para desenvolvimento local
- [ ] PostgreSQL para produção
- [x] Valores monetários representados com `BigDecimal`
- [x] Senhas armazenadas somente com hash BCrypt
- [x] Validação dos dados recebidos pela API
- [x] Tratamento global e padronizado de erros
- [ ] Testes unitários e de integração das regras críticas
- [ ] Documentação da API com OpenAPI/Swagger
- [ ] Docker para aplicação e banco de produção

## 1. Usuários e autenticação

- [x] Cadastro de pessoa física
- [x] E-mail e CPF únicos
- [x] Login básico com comparação segura de senha BCrypt
- [x] Bloqueio de login para usuário desativado
- [x] Perfis de cliente e administrador definidos
- [x] Consulta, atualização e desativação de usuário
- [ ] Autenticação e autorização com Spring Security

## 2. Contas bancárias

- [ ] Criação automática de conta para o cliente
- [x] Agência e número de conta únicos
- [x] Consulta de saldo
- [ ] Bloqueio e encerramento de conta
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
- [ ] Listar extrato por período
- [x] Identificar crédito, débito, data, descrição e saldo resultante
- [x] Gerar comprovante com código único
- [x] Não disponibilizar exclusão ou alteração de movimentação concluída pela API

## 5. Segurança

- [x] Senhas nunca retornadas pela API
- [x] Mensagem genérica para e-mail ou senha inválidos
- [x] Respostas `400`, `401`, `404`, `409` e `500` padronizadas
- [x] CORS separado em configuração própria para desenvolvimento
- [ ] Autenticação com Spring Security
- [ ] Sessão autenticada por identificador opaco no servidor
- [ ] Cookie de autenticação com `HttpOnly`, `Secure` e `SameSite`
- [ ] Não armazenar token de autenticação no `localStorage`
- [ ] Proteção CSRF nas operações que alteram dados
- [ ] Logout com invalidação da sessão e expiração do cookie
- [ ] Expiração por inatividade e limite de duração da sessão
- [ ] Renovação segura do identificador após o login
- [ ] Rate limiting para login, Pix, transferências e recuperação de senha
- [ ] Proteção contra tentativa de acesso a recursos de outro cliente
- [ ] Cabeçalhos CSP, HSTS, `X-Content-Type-Options` e política de referência
- [ ] HTTPS obrigatório em produção
- [ ] Segredos e credenciais somente em variáveis de ambiente
- [ ] Logs de auditoria sem senha, token, CPF completo ou dados bancários sensíveis
- [ ] Testes de autorização, CSRF, sessão expirada e força bruta
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
- [x] Layout responsivo e identidade visual própria

## 7. Qualidade e portfólio

- [ ] README com apresentação, imagens e instruções de execução
- [ ] Commits pequenos e descritivos
- [ ] Testes para saldo insuficiente, duplicidade e concorrência
- [ ] Coleção de requisições para demonstração da API
- [ ] Dados de demonstração sem informações pessoais reais
- [ ] Pipeline de integração contínua no GitHub

## Funcionalidades futuras

- [ ] Cartões e faturas
- [ ] Metas financeiras
- [ ] Empréstimos
- [ ] Investimentos
- [ ] Contas empresariais
- [ ] Conversão de moedas

## 8. Investimentos e dados de mercado

- [ ] Catálogo de ativos por tipo: renda fixa, fundos, ações, ETFs e criptomoedas
- [ ] Carteira de investimentos separada da conta corrente
- [ ] Aplicação e resgate simulados, sem executar ordens financeiras reais
- [ ] Posição consolidada com quantidade, preço médio e valor atual
- [ ] Cálculo de rentabilidade nominal e percentual
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
- [ ] Aviso claro de caráter educacional e ausência de recomendação financeira
- [ ] Verificação dos termos de uso e licença dos dados antes da publicação

## 9. Aplicativo móvel

- [x] Tecnologia definida: React Native com Expo SDK 54 e Expo Router
- [x] Estrutura inicial do aplicativo criada em `mobile/`
- [x] Login mobile integrado à API de desenvolvimento
- [x] Painel mobile com titular, saldo, agência e número da conta
- [x] Carregamento, mensagens de erro e nova tentativa no painel mobile
- [ ] Consumir a mesma API segura utilizada pelo frontend web
- [ ] Cadastro mobile
- [ ] Pix, transferências, extrato, cartões e investimentos
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
- [ ] Empréstimos com simulação de parcelas e juros
- [ ] Notificações e central de atividades da conta
- [ ] Painel administrativo com autorização própria
- [ ] Detecção de movimentações suspeitas por regras
- [ ] Auditoria imutável de ações críticas
- [ ] Métricas, monitoramento de saúde e alertas da aplicação
- [ ] Open Finance somente após análise de segurança e conformidade

## Ordem de entrega

- [ ] Fase 1 — autenticação segura, conta, saldo e extrato
- [ ] Fase 2 — depósito, saque, transferência e Pix
- [ ] Fase 3 — testes, documentação, Docker e PostgreSQL
- [ ] Fase 4 — frontend completo e publicação da demonstração
- [ ] Fase 5 — carteira e simulador de investimentos
- [ ] Fase 6 — integração com dados externos de mercado
- [ ] Fase 7 — aplicativo móvel
- [ ] Fase 8 — cartões, crédito, antifraude e módulos avançados
