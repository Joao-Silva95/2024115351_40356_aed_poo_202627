package domain;

/**
 * Material radioativo (perigoso)
 */
public class RadioactiveMaterial extends Artifact {

    /**
     * Nível de radiação, em mSv/h
     */
    private double radiationLevel;

    /**
     * @param id              ID do utilizador ou {@link Entity#UNASSIGNED_ID}
     * @param name            descrição
     * @param weight          peso (kg)
     * @param scientificValue valor científico
     * @param dangerLevel     nível de perigo
     * @param radiationLevel  radiação (mSv/h)
     */
    public RadioactiveMaterial(int id, String name, double weight, int scientificValue, int dangerLevel, double radiationLevel) {
        super(id, name, weight, scientificValue, dangerLevel);
        this.radiationLevel = radiationLevel;
    }

    /**
     * @return radiação (mSv/h)
     */
    public double getRadiationLevel() {
        return radiationLevel;
    }

    /**
     * @param radiationLevel nova radiação (mSv/h)
     */
    public void setRadiationLevel(double radiationLevel) {
        this.radiationLevel = radiationLevel;
    }
    @Override
    protected String extraFields() {
        return super.extraFields() + ";" + radiationLevel;
    }
}