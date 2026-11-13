package graph;

import java.util.List;

/**
 * Caminho no grafo: vértices, arestas percorridas e custo no critério pedido.
 *
 * @param vertices IDs dos vértices, da origem ao destino
 * @param edges    arestas percorridas
 * @param cost     custo total no critério usado na pesquisa
 */
public record PathResult(List<String> vertices, List<MultiEdge> edges, double cost) {

    /** @return distância total do caminho */
    public double totalDistance() {
        return edges.stream().mapToDouble(MultiEdge::getDistance).sum();
    }

    /** @return energia total do caminho */
    public double totalEnergy() {
        return edges.stream().mapToDouble(MultiEdge::getEnergyCost).sum();
    }

    /** @return radiação acumulada do caminho */
    public double totalRadiation() {
        return edges.stream().mapToDouble(MultiEdge::getRadiationDangerLevel).sum();
    }

    /** @return tempo total de trânsito do caminho */
    public double totalTime() {
        return edges.stream().mapToDouble(MultiEdge::getTransitTime).sum();
    }
}