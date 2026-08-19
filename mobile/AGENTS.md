# AGENTS.md — aplicativo móvel

## Regra de documentação

Expo mudou. Antes de escrever código, leia a documentação exata da versão utilizada:
https://docs.expo.dev/versions/v54.0.0/

## Tecnologias

- React Native
- Expo SDK 54
- Expo Router
- TypeScript
- Node.js 20.19.x

## Estado atual

- `app/index.tsx`: login com e-mail e senha, validação, estado de carregamento e integração com `POST /api/usuarios/login`.
- `app/conta.tsx`: consulta a conta por usuário, cria quando a API devolve 404 e exibe titular, saldo, agência e número.
- `app/investimentos.tsx`: lista catálogo e carteira, permite aplicação e resgate parcial ou total usando a API do backend.
- `app/extrato.tsx`: exibe aplicação como débito e resgate de investimento como crédito.
- `app/pix.tsx` e `app/transferencia.tsx`: totalmente integrados à API (`POST /pix` e `POST /transferencias`), com validação e comprovante.
- O protótipo foi executado com sucesso em um aparelho Android real usando Expo Go.
- `config/csrf.ts`: como o React Native não expõe o cabeçalho `Set-Cookie`, o token CSRF é obtido via `GET /api/csrf` (`buscarTokenCsrf()`) e enviado no cabeçalho `X-XSRF-TOKEN`. Chame `buscarTokenCsrf()` antes de qualquer `POST`/`PATCH`/`DELETE` — o token muda após o login (troca de sessão), então um token obtido antes de logar não serve depois.

## Forma de colaboração

- Isabella está aprendendo React Native e escreve o código TypeScript/JavaScript com orientação.
- Revise o que ela escreveu antes de propor a próxima etapa.
- Explique `useState`, propriedades, funções assíncronas, navegação e estilos em linguagem simples.
- Preserve as escolhas visuais e personalizações feitas por ela.
- Ao avançar, prefira uma funcionalidade completa e testável por vez.

## Atenções

- No aparelho físico, `localhost` aponta para o próprio celular. Use o IPv4 do computador na rede local durante o desenvolvimento.
- A URL da API está centralizada em `config/api.ts` e vem de `EXPO_PUBLIC_API_URL` no `.env.local`.
- O `usuarioId` recebido pela navegação não é autenticação nem autorização.
- Não persista senha em AsyncStorage ou outro armazenamento comum.
- A autenticação usa sessão (`JSESSIONID`, `HttpOnly`) e CSRF (`X-XSRF-TOKEN` obtido via `GET /api/csrf`), mas o `usuarioId` ainda vem por parâmetro de navegação em vez de derivado da sessão — ver nota acima.
- A tela `conta.tsx` ainda usa `useEffect` somente na montagem; ao voltar de investimentos, o saldo pode ficar desatualizado. O próximo ajuste é recarregar a conta com `useFocusEffect`.
