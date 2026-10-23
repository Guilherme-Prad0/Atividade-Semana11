# Testes de integração com Spring Boot e JUnit 5

Solução da lista prática usando o projeto-base, com Java 17, Maven, Spring Boot 3.3.13, Spring Data JPA, H2, JUnit 5 e MockMvc. Os cinco casos estão implementados, incluindo o desafio CT03. Service e Repository são componentes reais: não são substituídos por mocks.

## Executar

Configure um JDK 17 (JAVA_HOME) e Maven no PATH. Na pasta que contém pom.xml, execute:

~~~sh
mvn test
~~~

O banco H2 é criado em memória automaticamente. Não é necessário instalar um banco nem iniciar a aplicação separadamente.

## Casos implementados

| Caso | Método | Verificações |
| --- | --- | --- |
| CT01 | deveCadastrarERecuperarProduto | Cadastra Teclado por 150.0; verifica ID não nulo; consulta pelo repositório e confere ID, nome e preço. |
| CT02 | naoDevePersistirProdutoComNomeEmBranco | Guarda a contagem inicial; tenta cadastrar nome com espaços por 80.0; verifica IllegalArgumentException e contagem inalterada. |
| CT03 | deveAtualizarPrecoPersistido | Cadastra Mouse por 80.0; atualiza para 95.0; consulta novamente pelo repositório e verifica nome e preço. |
| CT04 | deveCadastrarProdutoPelaApi | Envia POST /produtos com Mouse e 80.0; verifica HTTP 200, conteúdo JSON, ID numérico, nome e preço. |
| CT05 | deveBuscarProdutoPelaApi | Cadastra por POST; extrai o ID com ObjectMapper/JsonNode; consulta GET /produtos/{id}; verifica HTTP 200, conteúdo JSON e os mesmos ID, nome e preço. |

Os testes estão em src/test/java/br/edu/exemplo/ProdutoServiceIntegrationTest.java e ProdutoControllerIntegrationTest.java.

## Persistência e isolamento

Verificar apenas o objeto devolvido pelo serviço confirma seu estado em memória, mas não garante que uma leitura posterior recupere esses dados. Consultar repository.findById() verifica também o caminho de recuperação pela persistência.

Como o JPA mantém entidades no contexto de persistência, uma consulta dentro da mesma transação pode reutilizar o objeto já carregado. Por isso, CT01, CT03 e CT05 executam EntityManager.flush() e clear() antes da leitura: flush envia as alterações pendentes ao H2 e clear remove as entidades do contexto. A consulta seguinte precisa recuperar o registro do banco. Flush não equivale a commit.

@Transactional faz o Spring abrir uma transação para cada método de teste e, por padrão, desfazê-la ao término, mesmo quando há falha. Os dados de um teste não ficam disponíveis para o seguinte. Os testes não dependem da ordem de execução ou de IDs fixos; sequências de IDs podem avançar mesmo com rollback. Estes testes verificam operações dentro da transação, sem comprovar comportamento após commit.

## Integração HTTP

@SpringBootTest carrega a aplicação e injeta componentes reais com @Autowired. @AutoConfigureMockMvc disponibiliza o MockMvc, que exercita o processamento do Spring MVC (roteamento, leitura do JSON e escrita da resposta), ProdutoController, ProdutoService, ProdutoRepository, JPA/Hibernate e H2.

MockMvc envia requisições simuladas diretamente à infraestrutura Servlet da aplicação. Não precisa abrir uma porta nem iniciar um servidor HTTP real. Como as chamadas destes testes são síncronas e executadas na mesma thread, participam da transação do teste e do rollback.

Em um teste unitário do controller com dependências simuladas, o comportamento do serviço seria programado pelo teste. Aqui as regras do serviço e a persistência são executadas de fato, permitindo detectar problemas na integração entre as camadas. A suíte não verifica conexões de rede ou a configuração de um servidor HTTP externo.

## Entrega

Repositório da atividade: [Guilherme-Prad0/Atividade-Semana11](https://github.com/Guilherme-Prad0/Atividade-Semana11). O projeto inclui pom.xml, src/, README.md e evidências dos testes. A pasta target/ contém arquivos gerados e está ignorada pelo Git.

## Resultado da execução

Suíte executada em 22/10/2026, às 22h20 (America/Sao_Paulo), com Java 17.0.18 e Maven 3.9.12, pelo objetivo test do Maven:

~~~text
ProdutoControllerIntegrationTest: Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
ProdutoServiceIntegrationTest:    Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
~~~

Relatórios originais do Surefire preservados para a entrega:

- [Testes do Service](evidencias/br.edu.exemplo.ProdutoServiceIntegrationTest.txt)
- [Testes do Controller](evidencias/br.edu.exemplo.ProdutoControllerIntegrationTest.txt)

Após nova execução, os relatórios completos são gerados em target/surefire-reports/.
