package graph.Interfaces;

import domain.Artifacts.Artifact;
import domain.Common.Entity;
import domain.Robots.Robot;
import graph.Enums.WeightType;
import graph.Model.LabyrinthGraph;
import graph.Results.CollectionStats;
import graph.Results.PathResult;
import graph.Results.RobotPair;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * API de gestão e análise do grafo do labirinto (R7 e R8).
 */
public interface GraphManagement {

    /**
     * R8a: caminho mais curto segundo um critério.
     *
     * @param from ID do vértice de origem
     * @param to   ID do vértice de destino
     * @param type critério de peso
     * @return o caminho, ou vazio se algum ID não existir ou não houver caminho
     */
    Optional<PathResult> getShortestPath(String from, String to, WeightType type);

    /** @return true se o grafo é (fracamente) conexo, ignorando as direções e os eventos */
    boolean isConnected();

    /** @return true se todos os vértices se alcançam uns aos outros respeitando as direções */
    boolean isStronglyConnected();

    /** @return IDs dos vértices sem nenhuma ligação de rota (setores isolados) */
    List<String> findIsolatedVertices();

    /**
     * R8b: subgrafo com os vértices que passam no filtro e as arestas entre eles.
     *
     * @param vertexFilter filtro sobre as entidades
     * @return novo grafo, que partilha as mesmas entidades
     */
    LabyrinthGraph extractSubgraph(Predicate<Entity> vertexFilter);

    /**
     * @param minHazard índice de perigo mínimo
     * @return subgrafo só com as salas com índice de perigo igual ou superior
     */
    LabyrinthGraph extractHighHazardSubgraph(double minHazard);

    /** @return subgrafo só com os robôs (e as ligações entre eles) */
    LabyrinthGraph extractRobotSubgraph();

    /**
     * R8d: artefacto mais próximo (em energia) que o robô consegue recolher.
     *
     * @param robotId           ID do robô
     * @param minScientificValue valor científico mínimo
     * @param maxRadiation      radiação acumulada máxima no caminho
     * @return o caminho até ao artefacto recomendado, ou vazio se não houver nenhum viável
     */
    Optional<PathResult> recommendOptimalRoute(String robotId, double minScientificValue, double maxRadiation);

    /**
     * R8e: estatísticas de recolha entre dois timestamps.
     *
     * @param sectorId ID do setor (sala), ou null para todos
     * @param category subtipo de artefacto, ou null para todos
     * @param from     timestamp mínimo (inclusive)
     * @param to       timestamp máximo (inclusive)
     * @return as estatísticas
     */
    CollectionStats getArtifactStatsBetweenTimestamps(String sectorId, Class<? extends Artifact> category, long from, long to);

    /**
     * R8f: robôs que entraram em setores perigosos num período.
     *
     * @param minHazard índice de perigo mínimo do setor
     * @param from      início (inclusive)
     * @param to        fim (inclusive)
     * @return robôs, sem repetições
     */
    List<Robot> findRobotsThroughHazardousSectors(double minHazard, long from, long to);

    /**
     * R8g: pares de robôs que operaram no mesmo setor num período.
     *
     * @param from início (inclusive)
     * @param to   fim (inclusive)
     * @return pares, um por setor
     */
    List<RobotPair> findRobotsInSameSector(long from, long to);
}