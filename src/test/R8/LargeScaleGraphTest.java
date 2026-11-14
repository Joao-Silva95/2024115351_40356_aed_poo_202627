package test.R8;

import domain.Artifacts.Artifact;
import domain.Artifacts.TechRelic;
import domain.Environment.Room;
import domain.Robots.ExplorerRobot;
import domain.Robots.Robot;
import graph.Enums.RelationType;
import graph.Enums.WeightType;
import graph.Model.LabyrinthGraph;
import graph.Results.PathResult;
import graph.Results.RobotPair;
import repository.DatabaseManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static test.Common.TestUtil.*;

/**
 * Testes em escala (stress/volume) do R8: criação dinâmica de salas,
 * robôs e artefactos totalmente parametrizável pelas constantes no topo da classe.
 */
public class LargeScaleGraphTest {

    // Podes alterar livremente estes 3 valores (desde que NUM_ROOMS >= NUM_ROBOTS + NUM_ARTIFACTS)
    private static final int NUM_ROOMS = 500;
    private static final int NUM_ROBOTS = 50;
    private static final int NUM_ARTIFACTS = 50;

    private static final int BASE_ROOM_ID = 1000;
    private static final int BASE_ROBOT_ID = 2000;
    private static final int BASE_ARTIFACT_ID = 3000;

    // Índices derivados automaticamente para evitar números fixos no código
    private static final int HAZARD_START_INDEX = NUM_ROOMS - NUM_ARTIFACTS;
    private static final int ROBOTS_START_INDEX = Math.max(0, HAZARD_START_INDEX - NUM_ROBOTS);

    /** @return true se os dois valores diferem menos de 1e-9 */
    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }

    public static LabyrinthGraph buildLargeScenario(DatabaseManager db) {
        if (NUM_ROOMS < NUM_ROBOTS + NUM_ARTIFACTS) {
            throw new IllegalArgumentException("NUM_ROOMS deve ser >= NUM_ROBOTS + NUM_ARTIFACTS para evitar sobreposição.");
        }

        LabyrinthGraph g = new LabyrinthGraph();
        LocalDate baseDate = LocalDate.of(2026, 9, 1);

        // 1. Criar Salas
        for (int i = 0; i < NUM_ROOMS; i++) {
            int roomId = BASE_ROOM_ID + i;
            double hazardLevel = (i >= HAZARD_START_INDEX) ? 8.5 : 2.0;
            boolean isMaintenance = (i % 10 == 0);

            Room room = new Room(roomId, "Sector_" + roomId, "ZONE_ALPHA", 1, hazardLevel, baseDate, isMaintenance);
            db.insertEntity(room);
            g.adicionarVertice(String.valueOf(roomId), room);
        }

        // Ligar as salas em anel seguro (custo baixo de energia e radiação)
        for (int i = 0; i < NUM_ROOMS; i++) {
            int currentRoom = BASE_ROOM_ID + i;
            int nextRoom = BASE_ROOM_ID + ((i + 1) % NUM_ROOMS);
            g.adicionarAresta(String.valueOf(currentRoom), String.valueOf(nextRoom), 10, 2, 1, 5, 0);
        }

        // Criar atalhos rápidos a cada 5 salas (alto consumo de energia e alta radiação)
        for (int i = 0; i < NUM_ROOMS - 5; i += 5) {
            int from = BASE_ROOM_ID + i;
            int to = BASE_ROOM_ID + i + 5;
            g.adicionarAresta(String.valueOf(from), String.valueOf(to), 20, 40, 9, 4, 0);
        }

        // 2. Criar Robôs (posicionados imediatamente antes da zona perigosa)
        for (int i = 0; i < NUM_ROBOTS; i++) {
            int robotId = BASE_ROBOT_ID + i;
            int startRoomId = BASE_ROOM_ID + ROBOTS_START_INDEX + i;

            Robot robot = new ExplorerRobot(robotId, "Bot_" + robotId, 100.0, baseDate, "ACTIVE", 15.0);
            db.insertEntity(robot);

            String rStr = String.valueOf(robotId);
            String roomStr = String.valueOf(startRoomId);
            g.adicionarVertice(rStr, robot);
            g.adicionarRelacao(rStr, roomStr, RelationType.LOCATION, 0);
        }

        // 3. Criar Artefactos (posicionados nas últimas NUM_ARTIFACTS salas)
        for (int i = 0; i < NUM_ARTIFACTS; i++) {
            int artifactId = BASE_ARTIFACT_ID + i;
            int targetRoomId = BASE_ROOM_ID + HAZARD_START_INDEX + i;

            Artifact artifact = new TechRelic(artifactId, "Relic_" + artifactId, 2.5, 90 + (i % 10), 5, "ANCIENT");
            db.insertEntity(artifact);

            String aStr = String.valueOf(artifactId);
            String roomStr = String.valueOf(targetRoomId);
            g.adicionarVertice(aStr, artifact);
            g.adicionarRelacao(roomStr, aStr, RelationType.LOCATION, 0);
        }

        // 4. Simular eventos na primeira sala perigosa
        String firstHazardRoom = String.valueOf(BASE_ROOM_ID + HAZARD_START_INDEX);
        String firstRobot = String.valueOf(BASE_ROBOT_ID);
        String secondRobot = String.valueOf(BASE_ROBOT_ID + 1);
        String firstArtifact = String.valueOf(BASE_ARTIFACT_ID);

        g.adicionarRelacao(firstRobot, firstHazardRoom, RelationType.TRAVERSAL, 1200);
        if (NUM_ROBOTS > 1) {
            g.adicionarRelacao(secondRobot, firstHazardRoom, RelationType.TRAVERSAL, 1500);
        }
        g.adicionarRelacao(firstRobot, firstArtifact, RelationType.COLLECTS, 1600);

        return g;
    }

    /** Valida a estrutura e conectividade do grafo em escala. */
    public static void testLargeGraphConnectivity() {
        int totalEntities = NUM_ROOMS + NUM_ROBOTS + NUM_ARTIFACTS;
        section("R8c (Escala) - Conectividade com " + NUM_ROOMS + " salas, " + NUM_ROBOTS + " robôs e " + NUM_ARTIFACTS + " artefactos");
        DatabaseManager db = freshDb("r8_large_conn.txt");
        LabyrinthGraph g = buildLargeScenario(db);

        check("Total de vértices é " + totalEntities, g.getVertexCount() == totalEntities);
        check("O grafo completo em escala é conexo", g.isConnected());
        check("Nenhum vértice ficou isolado", g.findIsolatedVertices().isEmpty());

        LabyrinthGraph roomsSubgraph = g.extractSubgraph(e -> e instanceof Room);
        check("Subgrafo das " + NUM_ROOMS + " salas é fortemente conexo (anel)", roomsSubgraph.isStronglyConnected());

        // Prova negativa: linha aberta sem fechar anel
        LabyrinthGraph lineGraph = new LabyrinthGraph();
        int sampleSize = Math.min(10, NUM_ROOMS);
        for (int i = 0; i < sampleSize; i++) {
            int id = BASE_ROOM_ID + i;
            lineGraph.adicionarVertice(String.valueOf(id), db.searchEntityById(id));
        }
        for (int i = 0; i < sampleSize - 1; i++) {
            lineGraph.adicionarAresta(String.valueOf(BASE_ROOM_ID + i), String.valueOf(BASE_ROOM_ID + i + 1), 10, 2, 1, 5, 0);
        }
        check("Linha aberta (sem fechar anel) ainda é conexa (fracamente)", lineGraph.isConnected());
        check("Linha aberta NÃO é fortemente conexa", !lineGraph.isStronglyConnected());
    }

    /** Valida caminhos mais curtos para todos os robôs simultaneamente. */
    public static void testMultiRobotPaths() {
        section("R8a (Escala) - Caminhos mais curtos para os " + NUM_ROBOTS + " robôs");
        DatabaseManager db = freshDb("r8_large_paths.txt");
        LabyrinthGraph g = buildLargeScenario(db);

        String targetArtifact = String.valueOf(BASE_ARTIFACT_ID);

        for (int i = 0; i < NUM_ROBOTS; i++) {
            String robotId = String.valueOf(BASE_ROBOT_ID + i);
            int stepsToTargetRoom = NUM_ROBOTS - i;
            int expectedHops = stepsToTargetRoom + 3;
            double expectedEnergy = stepsToTargetRoom * 2.0;

            Optional<PathResult> energyPath = g.getShortestPath(robotId, targetArtifact, WeightType.ENERGY);
            check("Robô " + robotId + " -> artefacto " + targetArtifact + " (Energia): custo = " + expectedEnergy,
                    energyPath.isPresent()
                            && energyPath.get().vertices().size() == expectedHops
                            && near(energyPath.get().cost(), expectedEnergy));
        }

        if (NUM_ROBOTS >= 5) {
            String firstRobot = String.valueOf(BASE_ROBOT_ID);
            Optional<PathResult> fastDist = g.getShortestPath(firstRobot, targetArtifact, WeightType.DISTANCE);
            Optional<PathResult> lowEnergy = g.getShortestPath(firstRobot, targetArtifact, WeightType.ENERGY);

            check("Robô " + firstRobot + ": caminho por distância usa menos saltos (atalhos) que o de energia",
                    fastDist.isPresent() && lowEnergy.isPresent()
                            && fastDist.get().vertices().size() < lowEnergy.get().vertices().size());
        }
    }

    /** Valida subgrafos de perigo e pesquisas temporais na rede alargada. */
    public static void testLargeSubgraphsAndQueries() {
        section("R8b/R8f/R8g (Escala) - Subgrafos de perigo e atividade dos robôs");
        DatabaseManager db = freshDb("r8_large_queries.txt");
        LabyrinthGraph g = buildLargeScenario(db);

        LabyrinthGraph highHazard = g.extractHighHazardSubgraph(8.0);
        check("Subgrafo de alto perigo (>= 8.0) contém exatamente as " + NUM_ARTIFACTS + " salas finais",
                highHazard.getVertexCount() == NUM_ARTIFACTS);

        Set<Integer> expectedRobots = (NUM_ROBOTS > 1)
                ? Set.of(BASE_ROBOT_ID, BASE_ROBOT_ID + 1)
                : Set.of(BASE_ROBOT_ID);

        check("Robôs detetados em setores perigosos (>= 8.0) batem certo",
                ids(g.findRobotsThroughHazardousSectors(8.0, 1000, 2000)).equals(expectedRobots));

        if (NUM_ROBOTS > 1) {
            String firstHazardRoom = String.valueOf(BASE_ROOM_ID + HAZARD_START_INDEX);
            List<RobotPair> pairs = g.findRobotsInSameSector(1000, 2000);
            check("Par de robôs detetado no setor perigoso " + firstHazardRoom,
                    pairs.size() == 1 && pairs.get(0).sector().equals(firstHazardRoom));
        }
    }

    /** Executa todos os testes em escala do R8. */
    public static void runAll() {
        testLargeGraphConnectivity();
        testMultiRobotPaths();
        testLargeSubgraphsAndQueries();
    }

    /** @param args não utilizado */
    public static void main(String[] args) {
        runAll();
        summary();
    }
}