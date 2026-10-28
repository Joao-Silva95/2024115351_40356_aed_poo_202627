package domain;

/**
 * Artefacto recuperável no labirinto
 */
public abstract class Artifact extends Entity {

    /**
     * Peso, em kg
     */
    private double weight;

    /**
     * Valor científico (0 a 100)
     */
    private int scientificValue;

    /**
     * Nível de perigo (0 a 10)
     */
    private int dangerLevel;

    /**
     * Instante da recolha, em milissegundos desde 1970 (0 se ainda não recolhido)
     */
    private long collectedTimestamp;

    /**
     * @param id              ID do utilizador ou {@link Entity#UNASSIGNED_ID}
     * @param name            descrição do artefacto
     * @param weight          peso em kg
     * @param scientificValue valor científico
     * @param dangerLevel     nível de perigo
     */
    protected Artifact(int id, String name, double weight, int scientificValue, int dangerLevel) {
        super(id, name);
        this.weight = weight;
        this.scientificValue = scientificValue;
        this.dangerLevel = dangerLevel;
    }

    /**
     * @return peso (kg)
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight novo peso (kg)
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * @return valor científico
     */
    public int getScientificValue() {
        return scientificValue;
    }

    /**
     * @param scientificValue novo valor científico
     */
    public void setScientificValue(int scientificValue) {
        this.scientificValue = scientificValue;
    }

    /**
     * @return nível de perigo
     */
    public int getDangerLevel() {
        return dangerLevel;
    }

    /**
     * @param dangerLevel novo nível de perigo
     */
    public void setDangerLevel(int dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    /**
     * @return timestamp da recolha (ms), ou 0 se não recolhido
     */
    public long getCollectedTimestamp() {
        return collectedTimestamp;
    }

    /**
     * @param collectedTimestamp novo timestamp de recolha (ms)
     */
    public void setCollectedTimestamp(long collectedTimestamp) {
        this.collectedTimestamp = collectedTimestamp;
    }

    public ArtifactType hasType;
    public Robot carries;

    @Override
    public String toString() {
        return super.toString() + "{weight=" + weight + ", science=" + scientificValue
                + ", danger=" + dangerLevel + "}";
    }
    // Retorna a descrição do artefacto

    /**
     * @return alias para a descrição do artefacto (R3)
     */
    public String getDescription() {
        return getName(); // Usa o atributo name herdado de Entity
    }
    @Override
    protected String extraFields() {
        return ";" + weight + ";" + scientificValue + ";" + dangerLevel + ";" + collectedTimestamp;
    }
}