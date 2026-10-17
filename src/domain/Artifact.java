package domain;

abstract class Artifact extends Entity {

  private double weight;

  private double scientificValue;

  private int dangerLevel;

  private long collectedTimestamp;

    public ArtifactType hasType;
    public Robot carries;

}