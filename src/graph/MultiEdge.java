package graph;

class MultiEdge {

    private double distance;

    private double energyCost;

    private double radiationDangerLevel;

    private double transitTime;

    private long edgeTimestamp;

    public LabyrinthGraph hasEdges;

  private int origem; // Adiciona os índices para saberes de onde para onde vai
  private int destino;

  public MultiEdge(int origem, int destino, double distance, double energyCost, double radiationDangerLevel, double transitTime, long edgeTimestamp) {
    this.origem = origem;
    this.destino = destino;
    this.distance = distance;
    this.energyCost = energyCost;
    this.radiationDangerLevel = radiationDangerLevel;
    this.transitTime = transitTime;
    this.edgeTimestamp = edgeTimestamp;
}
  public int from() { return origem; }
  public int to() { return destino; }

  public double getDistance() { return distance; }
  public double getEnergyCost() { return energyCost; }
  public double getRadiationDangerLevel() { return radiationDangerLevel; }
  public double getTransitTime() { return transitTime; }
  public long getEdgeTimestamp() { return edgeTimestamp; }
}