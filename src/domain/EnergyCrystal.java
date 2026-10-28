package domain;

/**
 * Cristal energético, fonte de energia para os robôs.
 */
public class EnergyCrystal extends Artifact {

    /**
     * Energia fornecida pelo cristal (ex.: em kWh).
     */
    private double energyYield;

    /**
     * @param id              ID do utilizador ou {@link Entity#UNASSIGNED_ID}
     * @param name            descrição
     * @param weight          peso (kg)
     * @param scientificValue valor científico
     * @param dangerLevel     nível de perigo
     * @param energyYield     energia fornecida
     */
    public EnergyCrystal(int id, String name, double weight, int scientificValue, int dangerLevel, double energyYield) {
        super(id, name, weight, scientificValue, dangerLevel);
        this.energyYield = energyYield;
    }

    /**
     * @return energia fornecida
     */
    public double getEnergyYield() {
        return energyYield;
    }

    /**
     * @param energyYield nova energia fornecida
     */
    public void setEnergyYield(double energyYield) {
        this.energyYield = energyYield;
    }
    @Override
    protected String extraFields() {
        return super.extraFields() + ";" + energyYield;
    }
}