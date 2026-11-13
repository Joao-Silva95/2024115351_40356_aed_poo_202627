package graph;

/**
 * Aresta do grafo com vários pesos (distância, energia, radiação, tempo), tipo de relação e timestamp.
 */

public class MultiEdge {

    /** Índice do vértice de origem. */
    private final int origem;

    /** Índice do vértice de destino. */
    private final int destino;

    /** Tipo de relação. */
    private final RelationType type;

    /** Distância. */
    private final double distance;

    /** Consumo de energia. */
    private final double energyCost;

    /** Nível de perigo de radiação. */
    private final double radiationDangerLevel;

    /** Tempo de trânsito. */
    private final double transitTime;

    /** Instante do evento ou registo, em ms. */
    private final long edgeTimestamp;

    public MultiEdge(int origem, int destino, RelationType type, double distance, double energyCost, double radiationDangerLevel, double transitTime, long edgeTimestamp) {
    this.origem = origem;
    this.destino = destino;
    this.type=type;
    this.distance = distance;
    this.energyCost = energyCost;
    this.radiationDangerLevel = radiationDangerLevel;
    this.transitTime = transitTime;
    this.edgeTimestamp = edgeTimestamp;
}
    /** @return índice de origem */
    public int from() { return origem; }

    /** @return índice de destino */
    public int to() { return destino; }

    /** @return tipo de relação */
    public RelationType getType() {return type;}

    /** @return distância */
    public double getDistance() { return distance; }

    /** @return consumo de energia */
    public double getEnergyCost() { return energyCost; }

    /** @return perigo de radiação */
    public double getRadiationDangerLevel() { return radiationDangerLevel; }

    /** @return tempo de trânsito */
    public double getTransitTime() { return transitTime; }

    /** @return timestamp (ms) */
    public long getEdgeTimestamp() { return edgeTimestamp; }
}