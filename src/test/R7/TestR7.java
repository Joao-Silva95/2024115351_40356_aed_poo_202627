package test.R7;

import domain.Artifacts.Artifact;
import domain.Environment.Room;
import domain.Resources.EnergyCrystal;
import domain.Robots.ExplorerRobot;
import domain.Robots.Robot;
import graph.Model.LabyrinthGraph;
import graph.Model.MultiEdge;
import repository.DatabaseManager;
import java.io.IOException;
import java.time.LocalDate;

import static test.Common.TestUtil.*;

/**
 * Testes do R7: Adaptação e combinação de entidades (Fase 1) num grafo
 * direcionado e ponderado (algs4), suportando vértices heterogéneos e arestas multi-relacionais.
 */
public class TestR7 {

    /** Valida a inserção correta de vértices heterogéneos (Salas, Robôs, Artefactos) no LabyrinthGraph. */
    public static void testAddHeterogeneousVertices() {
        section("R7 - Inserção de Vértices Heterogéneos");
        LabyrinthGraph graph = new LabyrinthGraph(10);

        Room room = new Room(20, "R-01", "STORAGE", 2, 1.5, LocalDate.of(2026, 2, 10), true);
        ExplorerRobot robot = new ExplorerRobot(10, "Explorer-A", 90, LocalDate.of(2026, 1, 15), "ACTIVE", 25);
        EnergyCrystal artifact = new EnergyCrystal(32, "Cristal azul", 1.5, 85, 4, 120.0);

        graph.adicionarVertice("R-01", room);
        graph.adicionarVertice("Explorer-A", robot);
        graph.adicionarVertice("Cristal azul", artifact);

        check("Grafo possui 3 vértices registados", graph.hasVertices.size() == 3);
        check("Suporta tipos heterogéneos de entidades (Room, Robot, Artifact)",
                graph.hasVertices.stream().anyMatch(e -> e instanceof Room) &&
                        graph.hasVertices.stream().anyMatch(e -> e instanceof Robot) &&
                        graph.hasVertices.stream().anyMatch(e -> e instanceof Artifact));
    }

    /** Valida a adição de arestas direcionadas e ponderadas (MultiEdge) com múltiplos atributos. */
    public static void testAddMultiEdgesWithMultipleWeights() {
        section("R7 - Adição de Arestas Ponderadas Multi-relacionais");
        LabyrinthGraph graph = new LabyrinthGraph(10);

        Room room1 = new Room(20, "R-01", "STORAGE", 2, 1.5, LocalDate.of(2026, 2, 10), true);
        Room room2 = new Room(21, "R-02", "REACTOR", 5, 9.0, LocalDate.of(2026, 2, 12), false);
        graph.adicionarVertice("R-01", room1);
        graph.adicionarVertice("R-02", room2);

        long timestamp = System.currentTimeMillis();
        graph.adicionarAresta("R-01", "R-02", 15.0, 5.0, 2.0, 10.0, timestamp);

        check("Aresta registada no vector hasEdges", graph.hasEdges.size() == 1);

        MultiEdge edge = graph.hasEdges.get(0);
        check("Peso Distância correto (15.0)", edge.getDistance() == 15.0);
        check("Peso Custo de Energia correto (5.0)", edge.getEnergyCost() == 5.0);
        check("Peso Nível de Radiação correto (2.0)", edge.getRadiationDangerLevel() == 2.0);
        check("Peso Tempo de Trânsito correto (10.0)", edge.getTransitTime() == 10.0);
        check("Timestamp de registo associado correto", edge.getEdgeTimestamp() == timestamp);
    }

    /** Valida que adicionar uma aresta com identificadores de vértices inexistentes é ignorado/recusado. */
    public static void testAddEdgeWithMissingVertices() {
        section("R7 - Adicionar Aresta com Vértice Inexistente");
        LabyrinthGraph graph = new LabyrinthGraph(10);

        Room room = new Room(20, "R-01", "STORAGE", 2, 1.5, LocalDate.of(2026, 2, 10), true);
        graph.adicionarVertice("R-01", room);

        graph.adicionarAresta("R-01", "R-INEXISTENTE", 10.0, 2.0, 0.0, 5.0, System.currentTimeMillis());

        check("Aresta inválida não é inserida (hasEdges vazio)", graph.hasEdges.isEmpty());
    }

    /** Valida o povoamento completo do grafo utilizando a base de dados populada (integração Fase 1 + Fase 2). */
    public static void testPopulateGraphFromDatabase() {
        section("R7 - Povoamento do Grafo via DatabaseManager");
        DatabaseManager db = freshDb("r7_db_graph.txt");
        populate(db);

        LabyrinthGraph graph = new LabyrinthGraph(20);

        db.listAllEntities().forEach(entity -> graph.adicionarVertice(String.valueOf(entity.getId()), entity));

        check("Todas as 11 entidades da BD foram inseridas como vértices", graph.hasVertices.size() == 11);

        graph.adicionarAresta("20", "21", 12.0, 4.0, 1.0, 8.0, System.currentTimeMillis());
        graph.adicionarAresta("21", "22", 8.0, 3.0, 0.5, 5.0, System.currentTimeMillis());

        check("Conexões de percurso adicionadas com sucesso (2 arestas)", graph.hasEdges.size() == 2);
    }
    /** Grafo e relações após adições e validações estruturais. */
    public static void testGraphAfterModifications() {
        section("R7 - Grafo após adições e modificações");
        DatabaseManager db = freshDb("r7_after.txt");
        populate(db);

        LabyrinthGraph graph = new LabyrinthGraph(20);
        db.listAllEntities().forEach(entity -> graph.adicionarVertice(String.valueOf(entity.getId()), entity));

        check("Grafo populado contém 11 vértices correspondentes à BD", graph.hasVertices.size() == 11);
        // Adicionar aresta inicial entre salas (ID 20 e 21)
        graph.adicionarAresta("20", "21", 15.0, 5.0, 2.0, 10.0, System.currentTimeMillis());
        check("Grafo possui 1 aresta após inserção", graph.hasEdges.size() == 1);
        check("Peso da aresta (distância) está correto", graph.hasEdges.get(0).getDistance() == 15.0);

        // Adicionar nova aresta e verificar incremento
        graph.adicionarAresta("21", "22", 8.0, 3.0, 1.0, 4.0, System.currentTimeMillis());
        check("Grafo possui 2 arestas após nova inserção", graph.hasEdges.size() == 2);

        // Tentar adicionar aresta com nó inexistente não deve alterar o número de arestas
        graph.adicionarAresta("20", "999", 10.0, 2.0, 0.0, 5.0, System.currentTimeMillis());
        check("Tentativa de ligar a vértice inexistente mantém o nº de arestas", graph.hasEdges.size() == 2);
    }

    /** Grafo vazio. */
    public static void testEmptyGraph() {
        section("R7 - Grafo vazio");
        LabyrinthGraph graph = new LabyrinthGraph(10);

        check("Vértices do grafo vazio estão vazios", graph.hasVertices.isEmpty());
        check("Arestas do grafo vazio estão vazias", graph.hasEdges.isEmpty());
    }

    /** Executa todos os testes do Requisito R7. */
    public static void runAll() throws IOException {
        testAddHeterogeneousVertices();
        testAddMultiEdgesWithMultipleWeights();
        testAddEdgeWithMissingVertices();
        testPopulateGraphFromDatabase();
        testEmptyGraph();
        testGraphAfterModifications();
    }

    /** @param args não utilizado */
    public static void main(String[] args) throws IOException {
        runAll();
        summary();
    }
}