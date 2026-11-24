# Cabe no Bolso — Planejador de Despesas Compartilhadas

Aplicação desktop em Java (Swing) para gerenciar planejamentos de atividades compartilhadas, cadastrar participantes com orçamento individual, registrar despesas com diferentes regras de rateio e acompanhar o resumo financeiro de cada pessoa.

---

## Sumário

- [Ambiente, JDK e Dependências](#ambiente-jdk-e-dependências)
- [Instalação](#instalação)
- [Organização das Pastas](#organização-das-pastas)
- [Compilação e Execução](#compilação-e-execução)
- [Arquitetura](#arquitetura)
- [Como Reproduzir os Testes](#como-reproduzir-os-testes)

---

## Ambiente, JDK e Dependências

- **Versão do JDK:** OpenJDK 27 (`openjdk-27`, configurado em [`.idea/misc.xml`](.idea/misc.xml)).
  - *Observação de compatibilidade:* os painéis Swing utilizam `Locale.of("pt", "BR")` (Java 19+) e o construtor de [`DespesaPorPessoa`](src/model/DespesaPorPessoa.java) possui uma instrução antes de `super(...)` (recurso finalizado no Java 25).
- **Dependências:** **nenhuma biblioteca externa**. O projeto utiliza exclusivamente APIs padrão do Java (`javax.swing`, `java.awt`, `java.math.BigDecimal`, `java.util` e serialização nativa `java.io`).

---

## Instalação

1. Certifique-se de ter o **JDK 25 ou superior** (recomendado: **OpenJDK 27**) instalado e, opcionalmente, o **IntelliJ IDEA**.
2. Clone o repositório e acesse a pasta do projeto:
   ```bash
   git clone <url-do-repositorio>
   cd cabeNoBolso
   ```
3. Caso utilize o terminal diretamente, verifique se os executáveis `javac` e `java` do JDK estão configurados no `PATH` do sistema (ou informe o caminho completo da pasta `bin` do JDK).

---

## Organização das Pastas

```text
cabeNoBolso/
├── .idea/                            # Configurações do projeto no IntelliJ IDEA (JDK, módulos)
├── src/                              # Código-fonte Java (padrão MVC)
│   ├── Main.java                     # Ponto de entrada; injeta Repositorio via Persistencia no controller
│   ├── controller/
│   │   └── PlanejadorController.java # Camada de integração entre View e o contrato Persistencia
│   ├── model/
│   │   ├── Persistencia.java         # Interface de persistência e acesso aos dados
│   │   ├── Repositorio.java          # Persistência binária em arquivo (.dat) e validações de integridade
│   │   ├── Participante.java         # Participante, código único, orçamento e orcamentoOk(...)
│   │   ├── Planejamento.java         # Atividade, lista polimórfica de Despesa e getResumo()
│   │   ├── Despesa.java              # Classe abstrata base com validação de valor (> 0)
│   │   ├── DespesaFixa.java          # Rateio fixo com distribuição de centavos pelo menor código
│   │   └── DespesaPorPessoa.java     # Rateio por valor individual multiplicado pelos participantes
│   └── view/
│       ├── TelaPrincipal.java        # Janela principal, menu Arquivo e navegação por abas
│       ├── PainelParticipantes.java  # Aba de cadastro, edição e remoção de participantes
│       ├── PainelPlanejamentos.java  # Aba de planejamentos e despesas (Fixa / Por pessoa)
│       └── PainelResumo.java         # Aba de resumo, matriz de rateio e orçamento excedido
├── out/                              # Diretório de saída dos arquivos compilados (.class)
├── dados.dat                         # Arquivo padrão de dados serializados
├── diagrama_uml.png                  # Diagrama de classes UML atualizado
├── Planejador de Despesas.iml        # Descritor de módulo do IntelliJ IDEA
└── README.md                         # Documentação do projeto
```

---

## Compilação e Execução

### Via IntelliJ IDEA
1. Abra a pasta do projeto no IntelliJ IDEA.
2. Confirme que o SDK do projeto está apontando para o **JDK 27** (`File > Project Structure > Project`).
3. Execute a classe [`Main`](src/Main.java).

### Via Terminal (Windows / Linux / macOS)
A partir da raiz do repositório:

```bash
javac -encoding UTF-8 -sourcepath src -d out src/Main.java
java -cp out Main
```

*(No Windows, caso o `javac` não esteja no `PATH`, utilize o caminho completo do JDK, por exemplo `"%USERPROFILE%\.jdks\openjdk-27.0.1.0.3\bin\javac.exe"` e `java.exe`.)*

Ao iniciar, a aplicação tenta carregar `dados.dat` no diretório de execução. Se o arquivo não existir ou estiver inválido, exibe a opção de **Iniciar vazio** ou **Carregar outro arquivo...**.

---

## Arquitetura

- **Padrão arquitetural:** MVC com inversão de dependência na persistência.
  - [`Persistencia`](src/model/Persistencia.java) define o contrato de leitura, gravação (`salvar`, `carregar`) e manipulação dos dados.
  - [`Repositorio`](src/model/Repositorio.java) implementa `Persistencia` gravando e validando arquivos `.dat`.
  - [`PlanejadorController`](src/controller/PlanejadorController.java) depende exclusivamente da interface `Persistencia`.
- **Polimorfismo:** [`Planejamento`](src/model/Planejamento.java) mantém `ArrayList<Despesa>` e invoca `getValorDoParticipante(...)`, `getValorInformado()`, `setValorInformado(...)` e `getTipo()` sobre referências da classe abstrata [`Despesa`](src/model/Despesa.java), delegando para [`DespesaFixa`](src/model/DespesaFixa.java) ou [`DespesaPorPessoa`](src/model/DespesaPorPessoa.java).

---

## Como Reproduzir os Testes

Para reproduzir manualmente pela interface gráfica:

1. **Preparar arquivo limpo:** execute o programa (`Main`), vá em **Arquivo > Novo arquivo de dados...** e salve um novo arquivo `.dat` (ou remova/renomeie `dados.dat` antes de iniciar e clique em **Iniciar vazio**).
2. **Caso de referência:**
   - Na aba **Participantes**, cadastre `Ana` (`70,00`), `Bruno` (`60,00`) e `Carla` (`70,00`).
   - Na aba **Planejamentos e Despesas**, crie o planejamento `Passeio da turma` e adicione as 4 despesas conforme a tabela do caso de referência.
   - Abra a aba **Resumo** e confira o total de `R$ 210,00`, `Bruno = OK` e `Ana` / `Carla = Excedido em R$ 5,00`.
3. **Centavos no rateio:** em um planejamento de teste (ou iniciando um novo arquivo para começar com códigos `1, 2, 3`), crie uma despesa **Fixa** de `100,00` marcando os 3 participantes e confira os valores individuais na aba **Resumo** (`R$ 33,34`, `R$ 33,33`, `R$ 33,33`).
4. **Entradas e vínculos inválidos:**
   - Tente cadastrar despesa sem marcar participantes, com valor `0` ou `-10,00`, ou sem descrição; valide o diálogo de erro e que nada foi inserido na tabela.
   - Na aba **Participantes**, selecione `Ana` (vinculada a despesas) e clique em **Remover participante selecionado** > **Sim**; valide o aviso **Remoção bloqueada**.
5. **Alteração de orçamento:** na aba **Participantes**, edite `Ana` para `80,00` e verifique na aba **Resumo** que seu total continua `R$ 75,00` e o status muda para `OK`. (Volte para `70,00` se quiser continuar a partir do caso original.)
6. **Exclusão de despesa:** na aba **Planejamentos e Despesas**, selecione `Atividade recreativa` e clique em **Remover despesa selecionada**; na aba **Resumo**, confira `R$ 60,00` por pessoa e total `R$ 180,00`.
7. **Salvar e reiniciar:** após montar o caso principal, feche a janela, abra o programa novamente (ou use **Arquivo > Carregar arquivo...**) e confira na aba **Resumo** que os 3 participantes, as 4 despesas e o total `R$ 210,00` retornaram sem duplicação.
8. **Problema no arquivo:**
   - Crie um arquivo de texto qualquer renomeado para `invalido.dat` e tente abri-lo via **Arquivo > Carregar arquivo...**: a mensagem de arquivo inválido será exibida e o planejamento atual permanecerá intacto (e o arquivo `invalido.dat` não ficará bloqueado no Windows).
   - Remova `dados.dat` da pasta e abra o programa: será exibido o diálogo **Problema no arquivo** oferecendo **Iniciar vazio** ou **Carregar outro arquivo...**.
