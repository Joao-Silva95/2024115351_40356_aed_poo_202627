# 2024115351_40356_aed_poo_202627

# RoboLabyrinth: Exploration and Collection

Projeto desenvolvido no âmbito das Unidades Curriculares de **Algoritmos e Estruturas de Dados (AED)** e **Programação Orientada aos Objetos (POO)** — Ano Letivo 2026/2027, Universidade Fernando Pessoa (Faculdade de Ciência e Tecnologia).

## Autores
* **João Miguel Moreira Silva** — Nº 2024115351 (2024115351@ufp.edu.pt)
* **Tiago Gonçalves Martins** — Nº 40356 (40356@ufp.edu.pt)

---

## 1. Arquitetura do Projeto

O código-fonte está modularizado em pacotes que separam o modelo de domínio, a camada de persistência/indexação (Fase 1), a representação em grafo e algoritmos (Fase 2) e os casos de teste:

```text
src/
├── domain/                  # Modelo de Entidades (Fase 1)
│   ├── Artifacts/           # Artifact, ArtifactType, TechRelic
│   ├── Common/              # Entity (classe base com ID único)
│   ├──Environment/          # Room
│   ├── Resources/           # EnergyCrystal, RadioactiveMaterial
│   └── Robots/              # CarrierRobot, ExplorerRobot, Robot
├── graph/                   # Grafo Heterogéneo Multi-relacional (R7, R8)
│   ├── Enums/               # RelationType, WeightType
│   ├── Interfaces/          # GraphManagement
│   ├── Model/               # LabyrinthGraph, MultiEdge
│   └── Results/             # CollectionStats, PathResult, RobotPair (Java Records)
├── repository/              # Base de Dados e Gestão de Estruturas (R2, R3, R4)
│   ├── DatabaseManager.java
│   ├── EntityBSTManagement.java
│   ├── EntityConsistencyManagement.java
│   └──  EntityHashManagement.java
└── test/                    # Unidades de Teste (R5)
    ├── Common/              # TestUtil
    ├── R2/                  # DatabaseManagerTest
    ├── R3/                  # SearchTest
    ├── R4/                  # ConsistencyTest
    ├── R7/                  # TestR7
    ├── R8/                  # GraphAlgorithmsTest, LargeScaleGraphTest
    └── AllTests.java        # Runner global com exportação de relatório
```
---
## 2. Estruturas de Dados e Algoritmos Implementados

### Fase 1: Base de Dados e Indexação (`repository`)
* **Tabelas de Símbolos Não Ordenadas (`R2` - `EntityHashManagement`):** Armazenamento primário das entidades (`Robot`, `Artifact`, `Room`) indexadas por ID único, garantindo operações de inserção, remoção, edição e pesquisa direta em tempo constante médio.
* **Árvores Binárias de Pesquisa Balanceadas (`R3` - `EntityBSTManagement`):** Utilização de `RedBlackBST` (biblioteca `algs4`) para indexar atributos ordenáveis e suportar pesquisas por intervalos inclusivos (nível de bateria dos robôs, valor científico dos artefactos e índice de perigo das salas), bem como filtragem por substring.
* **Consistência e Arquivo (`R4` - `EntityConsistencyManagement`):** Sincronização automática entre as STs e as BSTs durante edições (`editEntity`) e remoções (`removeEntity`). As entidades removidas são transferidas para uma ST auxiliar de arquivo e persistidas em ficheiro de texto (`ARCHIVED;...`), impedindo a reutilização do seu ID. Inclui ainda validação de integridade (`validateConsistency`) e reconstrução de índices (`rebuildIndexes`).

### Fase 2: Grafo Direcionado Ponderado (`graph`)
* **Representação em Grafo (`R7` - `LabyrinthGraph` & `MultiEdge`):** Extensão/composição sobre estruturas da biblioteca `algs4` (`EdgeWeightedDigraph` / `DirectedEdge`) para suportar vértices heterogéneos (Salas, Robôs e Artefactos) e arestas multi-relacionais. Distingue corredores navegáveis (pesos de distância, energia, radiação e tempo de trânsito) de arestas de eventos (`LOCATION`, `TRAVERSAL`, `COLLECTS`, `COMMUNICATION`) com *timestamps* associados.
* **Caminhos Mais Curtos (`R8a`):** Implementação baseada no algoritmo de **Dijkstra** (`DijkstraSP` da `algs4`) parametrizado por `WeightType` (`DISTANCE`, `ENERGY`, `TIME`, `RADIATION`), ignorando arestas de eventos históricos para garantir que apenas rotas físicas válidas são consideradas.
* **Extração de Subgrafos (`R8b`):** Filtragem funcional de vértices e arestas (`extractSubgraph`, `extractHighHazardSubgraph`, `extractRobotSubgraph`) preservando as referências originais das entidades na `DatabaseManager`.
* **Análise de Conectividade (`R8c`):** Verificação de conectividade fraca (`isConnected`, via travessia BFS/DFS não direcionada), deteção de vértices isolados (`findIsolatedVertices`) e conectividade forte (`isStronglyConnected`, via componentes fortemente conexas `KosarajuSharirSCC` / `TarjanSCC`).
* **Recomendação de Rotas Ótimas (`R8d`):** Avaliação dos artefactos ainda não recolhidos que cumprem o valor científico mínimo, selecionando o caminho de menor custo energético que respeita a bateria atual do robô e o limite máximo de radiação permitido (`recommendOptimalRoute`).
* **Análise Temporal e Estatísticas (`R8e`, `R8f`, `R8g`):** Filtragem de arestas relacionais por janelas temporais (`[t_inicio, t_fim]`) para calcular estatísticas de recolha (`CollectionStats`), identificar robôs em setores perigosos e detetar pares de robôs que operaram no mesmo setor (`RobotPair`).

---

## 3. Análise de Desempenho (Complexidade)

Considerando $N$ o número de entidades na base de dados, $V$ o número de vértices no grafo e $E$ o número de arestas/relações:

| Operação / Requisito | Estrutura / Algoritmo | Complexidade Temporal                 | Complexidade Espacial |
| :--- | :--- |:--------------------------------------| :--- |
| Inserção / Pesquisa por ID (`R2`) | Hash Table (`SeparateChainingHashST` / `HashMap`) | $O(1)$ médio                          | $O(N)$ |
| Pesquisa por intervalos (`R3`) | `RedBlackBST` (`algs4`) | $O(\log N + K)$ *(K = resultados)*    | $O(N)$ |
| Pesquisa por substring (`R3`) | Iteração linear sobre ST | $O(N \cdot L)$ *(L = tamanho string)* | $O(K)$ |
| Validação de consistência (`R4`) | Cruzamento ST $\leftrightarrow$ BST | $O(N \log N)$                         | $O(N)$ |
| Caminho mais curto (`R8a`) | Dijkstra (`DijkstraSP` - `algs4`) | $O(E \log V)$                         | $O(V)$ |
| Extração de subgrafos (`R8b`) | Filtragem de vértices e arestas | $O(V + E)$                            | $O(V' + E')$ |
| Conectividade fraca e forte (`R8c`) | BFS/DFS e Kosaraju-Sharir / Tarjan SCC | $O(V + E)$                            | $O(V)$ |
| Recomendação de rota (`R8d`) | Dijkstra + filtragem de artefactos | $O(E \log V + A)$ *(A = artefactos)* | $O(V)$ |
| Consultas temporais (`R8e-g`) | Filtragem sobre lista/saco de arestas | $O(E)$                                | $O(K)$ |

---

## 4. Testes Efetuados (`R5`)

Todos os requisitos funcionais dispõem de unidades de teste estáticas e isoladas no pacote `test`:

* **`test.R2.DatabaseManagerTest`:** Valida inserção (ID manual e automático), rejeição de duplicados/valores inválidos, pesquisas por ID, edições polimórficas e remoção.
* **`test.R3.SearchTest`:** Testa pesquisas por intervalos inclusivos (incluindo chaves repetidas na BST) e pesquisas *case-insensitive* por substring antes e após alterações na BD.
* **`test.R4.ConsistencyTest`:** Valida sincronização ST/BST, arquivo em ficheiro de texto, bloqueio de reutilização de IDs arquivados, *rollback* em caso de falha de I/O e deteção/reparação de dessincronizações (`validateConsistency` / `rebuildIndexes`).
* **`test.R7.TestR7`:** Valida a construção do `LabyrinthGraph` com vértices heterogéneos, arestas multi-ponderadas (`MultiEdge`) e rejeição de ligações a nós inexistentes.
* **`test.R8.GraphAlgorithmsTest`:** Testa exaustivamente todas as alíneas (`R8a` a `R8g`) e casos limite (*edge cases* como pesos negativos, grafos desconexos, robôs sem bateria e janelas temporais vazias).
* **`test.R8.LargeScaleGraphTest`:** Teste de volume/escalabilidade parametrizável (testado até **600 vértices**: 500 salas em anel com atalhos, 50 robôs e 50 artefactos), validando desempenho, caminhos ótimos em lote e provas positivas/negativas de conectividade forte.

### Execução Global e Relatórios
A classe `test.AllTests` executa sequencialmente todas as suites (`R2`, `R3`, `R4`, `R7`, `R8`), escrevendo os ficheiros auxiliares em `test_output/` e exportando o relatório completo da consola para `test_results/AllTests_output.txt`.

---

## 5. Como Compilar e Executar

1. **Pré-requisitos:**
   * Java JDK `[ex: 17 ou 21]`
   * Biblioteca `algs4.jar` (Princeton University) incluída no *classpath*.
2. **Executar a Suite Completa de Testes:**
   * Correr o método `main` da classe `test.AllTests`.
3. **Executar a Aplicação Principal / GUI (`R9`):**
   * *[Indicar aqui a classe principal da GUI ou menu consola quando estiver concluída]*