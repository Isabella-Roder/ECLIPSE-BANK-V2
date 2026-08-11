# Requisitos — Eclipse Bank V2

## Objetivo

Reconstruir o Eclipse Bank com arquitetura organizada, regras bancárias seguras e qualidade suficiente para ser o projeto principal do portfólio.

## Regras técnicas

- [x] Backend iniciado com Java 26, Spring Boot e Maven
- [x] Separação em `model`, `repository`, `dto`, `service`, `controller` e `exception`
- [x] Banco H2 persistente para desenvolvimento local
- [ ] PostgreSQL para produção
- [ ] Valores monetários representados com `BigDecimal`
- [ ] Senhas armazenadas somente com hash
- [ ] Validação dos dados recebidos pela API
- [ ] Tratamento global e padronizado de erros
- [ ] Testes unitários e de integração das regras críticas
- [ ] Documentação da API com OpenAPI/Swagger
- [ ] Docker para aplicação e banco de produção

## 1. Usuários e autenticação

- [ ] Cadastro de pessoa física
- [ ] E-mail e CPF únicos
- [ ] Login seguro
- [ ] Perfis de cliente e administrador
- [ ] Consulta e atualização do próprio perfil
- [ ] Autenticação e autorização com Spring Security

## 2. Contas bancárias

- [ ] Criação automática de conta para o cliente
- [ ] Agência e número de conta únicos
- [ ] Consulta de saldo
- [ ] Bloqueio e encerramento de conta
- [ ] Impedir saldo negativo fora das regras permitidas

## 3. Movimentações

- [ ] Depósito
- [ ] Saque
- [ ] Transferência entre contas
- [ ] Pix por chave
- [ ] Validação de saldo antes de débitos
- [ ] Operações atômicas com `@Transactional`
- [ ] Proteção contra envio duplicado da mesma operação

## 4. Extrato e comprovantes

- [ ] Registrar toda movimentação em histórico imutável
- [ ] Listar extrato por período
- [ ] Identificar crédito, débito, data, descrição e saldo resultante
- [ ] Gerar comprovante com código único
- [ ] Nunca apagar ou alterar uma movimentação concluída

## 5. Frontend

- [ ] Cadastro e login
- [ ] Painel com saldo e resumo da conta
- [ ] Tela de depósito e saque
- [ ] Tela de transferência e Pix
- [ ] Extrato com filtros
- [ ] Comprovante de operação
- [ ] Layout responsivo e identidade visual própria

## 6. Qualidade e portfólio

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
