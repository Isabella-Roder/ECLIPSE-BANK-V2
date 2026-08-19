# Eclipse Bank V2

Banco digital fictício desenvolvido como projeto de portfólio, com backend Spring Boot, frontend web próprio e aplicativo mobile em React Native. Simula operações bancárias reais (conta, extrato, Pix, transferências, investimentos e metas financeiras) com foco em arquitetura em camadas, segurança e regras de negócio consistentes.

> Nova versão do Eclipse Bank, reconstruída do zero. A versão original está disponível em [ECLIPSE-BANK](https://github.com/Isabella-Roder/ECLIPSE-BANK).

## Tecnologias

- **Backend:** Java 26, Spring Boot 4, Spring Data JPA, Spring Security, Maven, H2
- **Frontend web:** HTML, CSS e JavaScript
- **Mobile:** React Native, Expo SDK 54, Expo Router, TypeScript

## Funcionalidades

- Cadastro e login com sessão autenticada (`JSESSIONID` + proteção CSRF)
- Conta corrente com saldo, bloqueio, desbloqueio e encerramento
- Depósito, saque, transferência entre contas e Pix por chave de e-mail
- Extrato com filtros e comprovante de operação
- Carteira de investimentos: catálogo, aplicação, resgate, posição consolidada e rentabilidade
- Metas financeiras com aporte, resgate e barra de progresso
- Aplicativo mobile integrado à mesma API segura do frontend web

## Arquitetura

O backend segue arquitetura em camadas, separando responsabilidades por pacote:

```text
backend/
  src/main/java/com/eclipsebank/backend/
    config/       # Spring Security, CORS, cabeçalhos de segurança
    controller/   # endpoints REST
    dto/          # objetos de entrada e saída da API
    enums/        # tipos fixos do domínio (movimentação, investimento, meta)
    exception/    # tratamento global e padronizado de erros
    models/       # entidades JPA com as regras de negócio
    repository/   # acesso a dados (Spring Data JPA)
    service/      # orquestração das regras de negócio e transações
frontend/
  html/           # telas
  css/            # estilos por módulo
  script/         # integração com a API
mobile/
  app/            # telas do Expo Router
  config/         # URL da API e token CSRF
```

## Segurança

- Sessão opaca no servidor com cookie `HttpOnly` e `SameSite`; nada de token de sessão salvo no `localStorage`
- Proteção CSRF em todas as operações que alteram dados, tanto no frontend web quanto no mobile
- Autorização por proprietário da conta validada no servidor — o `usuarioId` enviado pelo cliente nunca é usado para autorizar sozinho
- Senhas com hash BCrypt e nunca retornadas pela API
- Valores monetários sempre em `BigDecimal`, nunca `double`/`float`, para evitar erro de arredondamento
- Alteração de saldo e registro de movimentação sempre na mesma transação; movimentação concluída nunca é editada ou apagada

## Testes

```bash
cd backend
./mvnw test
```

Cobrem regras de negócio das entidades, orquestração dos services, saldo insuficiente e cenários de erro nas principais operações (contas, investimentos, metas financeiras).

## Como executar

**Backend**

```bash
cd backend
./mvnw spring-boot:run
```

**Frontend web**

Abra a pasta `frontend/` com o Live Server (ou similar). A API é consumida em `http://localhost:8080` ou `http://<seu-ip>:8080`.

**Mobile**

```bash
cd mobile
npx expo start --lan
```

Configure `EXPO_PUBLIC_API_URL` em `mobile/.env.local` com o IPv4 do computador na rede local (não `localhost`).

## Documentação

- [Requisitos.md](Requisitos.md) — requisitos, roadmap e o que já foi entregue
- [AGENTS.md](AGENTS.md) — contexto técnico do projeto
- [mobile/AGENTS.md](mobile/AGENTS.md) — contexto do aplicativo mobile

## Status

Projeto em desenvolvimento ativo. Consulte o roadmap em [Requisitos.md](Requisitos.md) para o que já está pronto e o que vem a seguir.
