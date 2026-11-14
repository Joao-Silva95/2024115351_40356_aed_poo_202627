package test.R8;

import domain.Artifacts.Artifact;
import domain.Artifacts.TechRelic;
import domain.Environment.Room;
import domain.Resources.EnergyCrystal;
import domain.Robots.Robot;
import graph.Enums.RelationType;
import graph.Enums.WeightType;
import graph.Model.LabyrinthGraph;
import graph.Results.CollectionStats;
import graph.Results.PathResult;
import graph.Results.RobotPair;
import repository.DatabaseManager;

import java.util.Optional;
import java.util.Set;

import static test.Common.TestUtil.*;

/**
 * Testes do R8: caminhos mais curtos, subgrafos, conectividade, recomendação e pesquisas temporais.
 */
public class GraphAlgorithmsTest {

    /** @return true se os dois valores diferem menos de 1e-9 */
    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }

    private static void corridor(LabyrinthGraph g, int a, int b, double dist, double energy, double rad, double time) {
        g.adicionarAresta(String.valueOf(a), String.valueOf(b), dist, energy, rad, time, 0);
    }

    private static void relation(LabyrinthGraph g, int a, int b, RelationType t, long ts) {
        g.adicionarRelacao(String.valueOf(a), String.valueOf(b), t, ts);
    }

    /**
     * Constrói o grafo de teste a partir da BD populada (mesmas referências das entidades).
     * Corredores: 20->21 (direto: perto, caro, radioativo), 20->22->21 (barato, seguro), 21->20.
     * Robôs 10 e 13 na sala 20; 11 e 12 na sala 22. Salas 21 e 22 contêm os artefactos.
     */
    public static LabyrinthGraph buildScenario(DatabaseManager db) {
        LabyrinthGraph g = new LabyrinthGraph();
        db.listAllEntities().forEach(e -> g.adicionarVertice(String.valueOf(e.getId()), e));

        corridor(g, 20, 21, 10, 20, 8, 5);
        corridor(g, 20, 22, 6, 5, 1, 4);
        corridor(g, 22, 21, 6, 5, 1, 4);
        corridor(g, 21, 20, 10, 20, 8, 5);

        relation(g, 10, 20, RelationType.LOCATION, 0);
        relation(g, 13, 20, RelationType.LOCATION, 0);
        relation(g, 11, 22, RelationType.LOCATION, 0);
        relation(g, 12, 22, RelationType.LOCATION, 0);
        relation(g, 21, 30, RelationType.LOCATION, 0);
        relation(g, 21, 32, RelationType.LOCATION, 0);
        relation(g, 22, 31, RelationType.LOCATION, 0);
        relation(g, 22, 33, RelationType.LOCATION, 0);

        relation(g, 10, 32, RelationType.COLLECTS, 1000);
        relation(g, 11, 31, RelationType.COLLECTS, 2000);
        relation(g, 11, 33, RelationType.COLLECTS, 3000);

        relation(g, 10, 20, RelationType.TRAVERSAL, 1000);
        relation(g, 10, 21, RelationType.TRAVERSAL, 1500);
        relation(g, 11, 22, RelationType.TRAVERSAL, 2000);
        relation(g, 11, 21, RelationType.TRAVERSAL, 2500);
        relation(g, 12, 22, RelationType.TRAVERSAL, 4000);

        relation(g, 10, 11, RelationType.COMMUNICATION, 3500);
        relation(g, 11, 12, RelationType.COMMUNICATION, 3600);
        return g;
    }

    private static LabyrinthGraph scenario(String file, DatabaseManager[] out) {
        DatabaseManager db = freshDb(file);
        populate(db);
        if (out != null) out[0] = db;
        return buildScenario(db);
    }

    /** R8a: caminhos mais curtos por distância, energia, tempo e radiação. */
    public static void testShortestPaths() {
        section("R8a - Caminhos mais curtos por critério");
        LabyrinthGraph g = scenario("r8_paths.txt", null);

        Optional<PathResult> dist = g.getShortestPath("20", "21", WeightType.DISTANCE);
        check("Distância: caminho direto [20, 21] com custo 10",
                dist.isPresent() && dist.get().vertices().equals(java.util.List.of("20", "21")) && near(dist.get().cost(), 10));

        Optional<PathResult> energy = g.getShortestPath("20", "21", WeightType.ENERGY);
        check("Energia: passa pela sala 22 [20, 22, 21] com custo 10",
                energy.isPresent() && energy.get().vertices().equals(java.util.List.of("20", "22", "21")) && near(energy.get().cost(), 10));
        check("No caminho de energia, a distância total é 12 e a radiação 2",
                energy.isPresent() && near(energy.get().totalDistance(), 12) && near(energy.get().totalRadiation(), 2));

        Optional<PathResult> time = g.getShortestPath("20", "21", WeightType.TIME);
        check("Tempo: caminho direto com custo 5",
                time.isPresent() && time.get().vertices().size() == 2 && near(time.get().cost(), 5));

        Optional<PathResult> rad = g.getShortestPath("20", "21", WeightType.RADIATION);
        check("Radiação: passa pela sala 22 com custo 2",
                rad.isPresent() && rad.get().vertices().size() == 3 && near(rad.get().cost(), 2));

        Optional<PathResult> robotToArtifact = g.getShortestPath("10", "33", WeightType.ENERGY);
        check("Robô 10 -> artefacto 33: [10, 20, 22, 33] com energia 5",
                robotToArtifact.isPresent()
                        && robotToArtifact.get().vertices().equals(java.util.List.of("10", "20", "22", "33"))
                        && near(robotToArtifact.get().cost(), 5));

        Optional<PathResult> same = g.getShortestPath("20", "20", WeightType.ENERGY);
        check("Origem igual ao destino: caminho [20] com custo 0",
                same.isPresent() && same.get().vertices().size() == 1 && near(same.get().cost(), 0));
        check("Sem caminho (sala 20 -> robô 10) devolve vazio", g.getShortestPath("20", "10", WeightType.ENERGY).isEmpty());
        check("ID inexistente devolve vazio", g.getShortestPath("20", "999", WeightType.ENERGY).isEmpty());
        check("Peso negativo é recusado", throwsA(IllegalArgumentException.class,
                () -> g.adicionarAresta("20", "21", -1, 1, 1, 1, 0)));
    }

    /** Eventos (TRAVERSAL, COLLECTS...) não contam como rotas. */
    public static void testEventsAreNotRoutes() {
        section("R8a - Eventos não são rotas");
        DatabaseManager db = freshDb("r8_events.txt");
        populate(db);
        LabyrinthGraph g = new LabyrinthGraph();
        g.adicionarVertice("10", db.searchEntityById(10));
        g.adicionarVertice("20", db.searchEntityById(20));
        g.adicionarRelacao("10", "20", RelationType.TRAVERSAL, 1000);

        check("Aresta de evento não cria caminho", g.getShortestPath("10", "20", WeightType.DISTANCE).isEmpty());
        check("Aresta de evento não liga o grafo", !g.isConnected());
        check("Os 2 vértices ficam isolados", g.findIsolatedVertices().size() == 2);
    }

    /** R8b: subgrafos. */
    public static void testSubgraphs() {
        section("R8b - Subgrafos");
        DatabaseManager[] holder = new DatabaseManager[1];
        LabyrinthGraph g = scenario("r8_sub.txt", holder);

        LabyrinthGraph hazard = g.extractHighHazardSubgraph(5.0);
        check("Subgrafo perigoso (>= 5.0) tem 2 vértices (salas 21 e 22)", hazard.getVertexCount() == 2);
        check("Só contém as salas 21 e 22", hazard.contains("21") && hazard.contains("22") && !hazard.contains("20"));
        check("Só mantém a aresta 22->21", hazard.getEdgeCount() == 1);
        check("O subgrafo partilha as mesmas entidades da BD", hazard.getEntity("21") == holder[0].searchEntityById(21));

        LabyrinthGraph robots = g.extractRobotSubgraph();
        check("Subgrafo de robôs tem 4 vértices", robots.getVertexCount() == 4);
        check("Subgrafo de robôs tem as 2 ligações de comunicação", robots.getEdgeCount() == 2);

        LabyrinthGraph none = g.extractSubgraph(e -> false);
        check("Filtro vazio: 0 vértices e 0 arestas", none.getVertexCount() == 0 && none.getEdgeCount() == 0);
        check("O grafo original não foi alterado", g.getVertexCount() == 11);
    }

    /** R8c: conectividade. */
    public static void testConnectivity() {
        section("R8c - Conectividade");
        DatabaseManager[] holder = new DatabaseManager[1];
        LabyrinthGraph g = scenario("r8_conn.txt", holder);

        check("Grafo completo é conexo", g.isConnected());
        check("Nenhum vértice isolado", g.findIsolatedVertices().isEmpty());
        check("Grafo completo não é fortemente conexo (robôs só têm saídas)", !g.isStronglyConnected());

        LabyrinthGraph rooms = g.extractSubgraph(e -> e instanceof Room);
        check("Subgrafo das salas é conexo", rooms.isConnected());
        check("Subgrafo das salas é fortemente conexo", rooms.isStronglyConnected());

        LabyrinthGraph broken = new LabyrinthGraph();
        for (int id : new int[]{20, 21, 22}) broken.adicionarVertice(String.valueOf(id), holder[0].searchEntityById(id));
        broken.adicionarAresta("20", "21", 10, 1, 1, 1, 0);
        check("Com a sala 22 sem ligações o grafo não é conexo", !broken.isConnected());
        check("A sala 22 é detetada como isolada", broken.findIsolatedVertices().equals(java.util.List.of("22")));

        check("Grafo vazio é conexo", new LabyrinthGraph().isConnected());
    }

    /** R8d: recomendação do artefacto mais próximo. */
    public static void testRecommendation() {
        section("R8d - Recomendação de rota");
        DatabaseManager[] holder = new DatabaseManager[1];
        LabyrinthGraph g = scenario("r8_reco.txt", holder);
        DatabaseManager db = holder[0];

        Optional<PathResult> rec = g.recommendOptimalRoute("10", 80, 10);
        check("Robô 10, valor >= 80: recomenda o artefacto 33 (energia 5)",
                rec.isPresent() && rec.get().vertices().equals(java.util.List.of("10", "20", "22", "33")) && near(rec.get().cost(), 5));
        check("Valor mínimo 90: nenhum artefacto serve", g.recommendOptimalRoute("10", 90, 10).isEmpty());
        check("Limite de radiação 0.5: nenhum caminho é seguro", g.recommendOptimalRoute("10", 80, 0.5).isEmpty());
        check("ID que não é robô devolve vazio", g.recommendOptimalRoute("20", 80, 10).isEmpty());
        check("ID inexistente devolve vazio", g.recommendOptimalRoute("999", 80, 10).isEmpty());

        db.editEntity(33, Artifact.class, a -> a.setCollectedTimestamp(3000));
        Optional<PathResult> afterCollected = g.recommendOptimalRoute("10", 80, 10);
        check("Artefacto já recolhido (33) deixa de ser recomendado",
                afterCollected.isPresent() && !afterCollected.get().vertices().contains("33") && near(afterCollected.get().cost(), 10));

        check("Robô 13 com bateria 20 consegue chegar ao artefacto", g.recommendOptimalRoute("13", 85, 10).isPresent()
                || g.recommendOptimalRoute("13", 80, 10).isPresent());
        db.editEntity(13, Robot.class, r -> r.setBatteryLevel(3));
        check("Robô 13 com bateria 3 não tem energia para nenhum artefacto", g.recommendOptimalRoute("13", 80, 10).isEmpty());
    }

    /** R8e: estatísticas de recolha. */
    public static void testCollectionStats() {
        section("R8e - Estatísticas de recolha");
        LabyrinthGraph g = scenario("r8_stats.txt", null);

        CollectionStats all = g.getArtifactStatsBetweenTimestamps(null, null, 0, 5000);
        check("Todas as recolhas: 3 artefactos", all.count() == 3);
        check("Peso total 6.7 kg", near(all.totalWeight(), 6.7));
        check("Valor científico total 240, média 80", all.totalScientificValue() == 240 && near(all.averageScientificValue(), 80));

        check("Setor 21: 1 recolha", g.getArtifactStatsBetweenTimestamps("21", null, 0, 5000).count() == 1);
        check("Setor 22: 2 recolhas com valor total 155",
                g.getArtifactStatsBetweenTimestamps("22", null, 0, 5000).totalScientificValue() == 155);
        check("Setor 22 só EnergyCrystal: 1 recolha", g.getArtifactStatsBetweenTimestamps("22", EnergyCrystal.class, 0, 5000).count() == 1);
        check("EnergyCrystal em todo o labirinto: 2 recolhas", g.getArtifactStatsBetweenTimestamps(null, EnergyCrystal.class, 0, 5000).count() == 2);
        check("Janela 1500-2500: 1 recolha (TechRelic)", g.getArtifactStatsBetweenTimestamps(null, TechRelic.class, 1500, 2500).count() == 1);
        check("Limites são inclusivos (1000-1000)", g.getArtifactStatsBetweenTimestamps(null, null, 1000, 1000).count() == 1);

        CollectionStats empty = g.getArtifactStatsBetweenTimestamps(null, null, 5000, 6000);
        check("Janela sem recolhas: 0 e média 0", empty.count() == 0 && near(empty.averageScientificValue(), 0));
        check("Setor inexistente: 0 recolhas", g.getArtifactStatsBetweenTimestamps("999", null, 0, 5000).count() == 0);
    }

    /** R8f: robôs que atravessaram setores perigosos. */
    public static void testHazardousTraversals() {
        section("R8f - Robôs em setores perigosos");
        LabyrinthGraph g = scenario("r8_hazard.txt", null);

        check("Perigo >= 8, 0-5000: robôs 10 e 11", ids(g.findRobotsThroughHazardousSectors(8.0, 0, 5000)).equals(Set.of(10, 11)));
        check("Perigo >= 5, 0-5000: robôs 10, 11 e 12", ids(g.findRobotsThroughHazardousSectors(5.0, 0, 5000)).equals(Set.of(10, 11, 12)));
        check("Perigo >= 5, 0-1999: só o robô 10", ids(g.findRobotsThroughHazardousSectors(5.0, 0, 1999)).equals(Set.of(10)));
        check("Perigo >= 5, 3000-5000: só o robô 12", ids(g.findRobotsThroughHazardousSectors(5.0, 3000, 5000)).equals(Set.of(12)));
        check("Perigo >= 10: ninguém", g.findRobotsThroughHazardousSectors(10.0, 0, 5000).isEmpty());
        check("Sem repetições (robô 11 passou em 2 setores)", g.findRobotsThroughHazardousSectors(5.0, 0, 5000).size() == 3);
    }

    /** R8g: robôs que operaram no mesmo setor. */
    public static void testSameSector() {
        section("R8g - Robôs no mesmo setor");
        LabyrinthGraph g = scenario("r8_same.txt", null);

        java.util.List<RobotPair> early = g.findRobotsInSameSector(0, 3000);
        check("Janela 0-3000: 1 par", early.size() == 1);
        check("O par é (10, 11) no setor 21", !early.isEmpty() && early.get(0).first().getId() == 10
                && early.get(0).second().getId() == 11 && early.get(0).sector().equals("21"));
        check("Janela 0-5000: 2 pares (setores 21 e 22)", g.findRobotsInSameSector(0, 5000).size() == 2);
        check("Janela 0-500: nenhum par", g.findRobotsInSameSector(0, 500).isEmpty());
    }

    /** Executa todos os testes do R8. */
    public static void runAll() {
        testShortestPaths();
        testEventsAreNotRoutes();
        testSubgraphs();
        testConnectivity();
        testRecommendation();
        testCollectionStats();
        testHazardousTraversals();
        testSameSector();
    }

    /** @param args não utilizado */
    public static void main(String[] args) {
        runAll();
        summary();
    }
}