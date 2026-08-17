# AGENTS.md

## Objetivo

Este arquivo define as regras obrigatórias para qualquer agente de IA que trabalhe neste repositório.

A prioridade é **preservar o projeto**, evitar alterações desnecessárias e manter Isabella no controle das decisões técnicas.

---

## Regra principal

**Não faça alterações amplas, destrutivas ou não solicitadas.**

Antes de modificar qualquer arquivo:

1. Entenda exatamente a tarefa.
2. Leia apenas os arquivos necessários.
3. Explique resumidamente o que pretende fazer.
4. Só altere o que estiver diretamente relacionado à tarefa.

Nunca aproveite uma tarefa pequena para refatorar outras partes do projeto.

---

## Escopo

- Trabalhe somente dentro do escopo solicitado.
- Não analise o repositório inteiro sem necessidade.
- Não altere arquivos que não sejam relevantes para a tarefa.
- Não reorganize pastas sem autorização.
- Não renomeie classes, arquivos, métodos ou variáveis sem necessidade.
- Não substitua tecnologias, bibliotecas ou padrões existentes por preferência própria.
- Não faça "melhorias gerais" não solicitadas.

Se houver dúvida sobre o escopo, **pergunte antes de alterar**.

---

## Segurança do projeto

É proibido sem autorização explícita:

- excluir arquivos;
- apagar diretórios;
- sobrescrever arquivos grandes;
- alterar configurações globais;
- alterar `.gitignore`;
- alterar dependências;
- alterar versões de Java, Spring Boot, Node, React Native ou outras ferramentas;
- modificar configuração de banco de dados;
- mudar estrutura de banco;
- alterar migrations;
- executar comandos destrutivos;
- executar `git reset --hard`;
- executar `git clean`;
- executar `git push --force`;
- executar `git rebase`;
- executar comandos com `sudo`;
- apagar branches;
- modificar histórico do Git.

Nunca execute comandos destrutivos ou irreversíveis.

---

## Git

- Nunca faça `git push` sem pedido explícito.
- Nunca faça merge automaticamente.
- Nunca faça rebase automaticamente.
- Nunca apague commits.
- Nunca altere histórico remoto.
- Antes de alterações relevantes, verifique o estado com `git status`.
- Depois de alterações, mostre resumidamente quais arquivos foram modificados.

Se houver arquivos modificados pela Isabella, **não sobrescreva o trabalho dela**.

---

## Forma de trabalho

Faça mudanças pequenas e incrementais.

Para cada tarefa:

1. Identifique os arquivos necessários.
2. Explique o plano em poucas linhas.
3. Faça a menor alteração possível.
4. Verifique se o projeto continua compilando.
5. Execute apenas os testes relacionados à mudança.
6. Informe resumidamente o resultado.

Não continue para outra tarefa automaticamente.

---

## Modo de aprendizado

Quando Isabella pedir explicação, orientação ou disser que quer implementar:

- Não escreva a solução completa.
- Não modifique arquivos.
- Explique somente o próximo passo.
- Use exemplos pequenos quando necessário.
- Espere Isabella implementar.
- Depois revise o que ela fez.

Nunca responda apenas:

> "Essa parte você implementa."

Se Isabella precisar implementar algo, explique claramente **o que fazer, onde fazer e por quê**.

---

## Modo de execução

Quando Isabella pedir explicitamente para implementar:

- Faça apenas a tarefa solicitada.
- Não altere arquitetura sem necessidade.
- Não refatore arquivos não relacionados.
- Não gere funcionalidades extras.
- Não adicione dependências sem justificar e pedir autorização.
- Não reproduza arquivos completos no chat sem necessidade.

Depois da alteração, responda com um resumo curto.

---

## Backend

O backend é uma área importante do projeto e deve receber atenção especial.

Regras gerais:

- Preserve a arquitetura existente.
- Regras de negócio devem ficar na camada apropriada.
- Controllers não devem concentrar regra de negócio.
- Services devem concentrar lógica de negócio.
- Repositories devem cuidar da persistência.
- Use DTOs nas fronteiras da API quando o projeto já seguir esse padrão.
- Não exponha entidades diretamente sem necessidade.
- Para valores monetários, use `BigDecimal`.
- Não use `double` ou `float` para dinheiro.
- Validações importantes devem existir no backend.
- Nunca confie apenas em validação de frontend.

---

## Segurança

Nunca:

- exponha senha;
- salve senha em texto puro;
- registre tokens em logs;
- registre credenciais;
- coloque segredos no código;
- coloque chaves de API no repositório;
- coloque dados pessoais reais em exemplos;
- desative autenticação ou autorização para "facilitar";
- remova validações de segurança sem autorização.

Sempre trate entrada do usuário como não confiável.

Autorização deve ser validada no servidor.

---

## Frontend

Frontend é secundário ao foco principal do projeto.

Para tarefas puramente visuais:

- pode implementar diretamente quando solicitado;
- reutilize componentes e estilos existentes;
- não altere backend para resolver problema visual;
- não introduza bibliotecas novas sem necessidade;
- não recrie páginas inteiras se bastar alterar pequenos componentes;
- mantenha respostas curtas após a implementação.

---

## Testes

- Não remova testes existentes para fazer a build passar.
- Não altere expectativa de teste apenas para esconder bug.
- Quando possível, adicione ou atualize testes relacionados à mudança.
- Execute apenas os testes necessários, salvo quando Isabella pedir suíte completa.

---

## Erros e bugs

Quando encontrar um erro:

1. Identifique a causa.
2. Explique resumidamente.
3. Proponha a menor correção possível.
4. Não faça refatorações grandes enquanto corrige o bug.

Se não tiver certeza da causa, diga claramente que ainda está investigando.

Não invente explicações.

---

## Dependências

Antes de adicionar qualquer dependência:

1. Verifique se a funcionalidade já pode ser implementada com o que existe.
2. Explique por que a nova dependência é necessária.
3. Peça autorização.

Não atualize versões automaticamente.

---

## Arquivos de configuração

Tenha cuidado especial com:

- `pom.xml`
- `build.gradle`
- `package.json`
- arquivos `.env`
- Docker
- banco de dados
- CI/CD
- arquivos de configuração de segurança
- configurações de produção

Não altere esses arquivos sem necessidade clara.

---

## Respostas

Depois de uma implementação, responda de forma curta.

Formato preferido:

**Alterado**
- arquivo X
- arquivo Y

**O que mudou**
- resumo objetivo

**Verificação**
- teste executado
- resultado

Não reproduza centenas de linhas de código no chat.

---

## Em caso de dúvida

Se uma ação puder:

- quebrar funcionalidade existente;
- afetar vários módulos;
- alterar arquitetura;
- apagar dados;
- alterar banco;
- alterar dependências;
- alterar Git;
- comprometer segurança;

**pare e peça autorização antes de continuar.**

---

## Princípio final

O objetivo do agente é ajudar Isabella a desenvolver o projeto com segurança.

**Não tente "melhorar tudo".  
Não assuma controle do projeto.  
Não faça alterações fora do pedido.  
Não destrua trabalho existente.  
Quando estiver em dúvida, pergunte.**