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
- O protótipo foi executado com sucesso em um aparelho Android real usando Expo Go.
- Os botões de Pix, transferência e extrato ainda são apenas visuais.

## Forma de colaboração

- Isabella está aprendendo React Native e escreve o código TypeScript/JavaScript com orientação.
- Revise o que ela escreveu antes de propor a próxima etapa.
- Explique `useState`, propriedades, funções assíncronas, navegação e estilos em linguagem simples.
- Preserve as escolhas visuais e personalizações feitas por ela.
- Ao avançar, prefira uma funcionalidade completa e testável por vez.

## Atenções

- No aparelho físico, `localhost` aponta para o próprio celular. Use o IPv4 do computador na rede local durante o desenvolvimento.
- O IP da API está temporariamente repetido em `index.tsx` e `conta.tsx`; o próximo refactor deve centralizá-lo.
- O `usuarioId` recebido pela navegação não é autenticação nem autorização.
- Não persista senha em AsyncStorage ou outro armazenamento comum.
- A autenticação atual é provisória até o backend possuir Spring Security e sessão segura.
