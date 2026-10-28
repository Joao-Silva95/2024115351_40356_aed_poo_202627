package domain;

import java.util.Vector;
import java.time.LocalDate;

/**
 * Robô autónomo genérico
 */
public abstract class Robot extends Entity {

    /**
     * Nível de bateria, em percentagem (0 a 100)
     */
    private double batteryLevel;

    /**
     * Data da versão do sistema instalada
     */
    private LocalDate systemVersionDate;

    /**
     * Estado operacional (ex.: "ACTIVE", "CHARGING")
     */
    private String operationalState;

    /**
     * @param id                ID do utilizador ou {@link Entity#UNASSIGNED_ID}
     * @param name              nome do robô
     * @param batteryLevel      bateria inicial (0 a 100)
     * @param systemVersionDate data da versão do sistema
     * @param operationalState  estado operacional
     */
    protected Robot(int id, String name, double batteryLevel, LocalDate systemVersionDate, String operationalState) {
        super(id, name);
        setBatteryLevel(batteryLevel);
        this.systemVersionDate = systemVersionDate;
        this.operationalState = operationalState;
    }

    /**
     * @return bateria em percentagem
     */
    public double getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * @param batteryLevel nova bateria (0 a 100)
     * @throws IllegalArgumentException se fora do intervalo
     */
    public void setBatteryLevel(double batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100)
            throw new IllegalArgumentException("Bateria fora de 0-100: " + batteryLevel);
        this.batteryLevel = batteryLevel;
    }

    /**
     * @return data da versão do sistema
     */
    public LocalDate getSystemVersionDate() {
        return systemVersionDate;
    }

    /**
     * @param systemVersionDate nova data de versão
     */
    public void setSystemVersionDate(LocalDate systemVersionDate) {
        this.systemVersionDate = systemVersionDate;
    }

    /**
     * @return estado operacional
     */
    public String getOperationalState() {
        return operationalState;
    }

    /**
     * @param operationalState novo estado operacional
     */
    public void setOperationalState(String operationalState) {
        this.operationalState = operationalState;
    }

    /**
     *
     * @element-type Artifact
     */
    public Vector carries;

    @Override
    public String toString() {
        return super.toString() + "{battery=" + batteryLevel + ", state=" + operationalState
                + ", version=" + systemVersionDate + "}";
    }

    @Override
    protected String extraFields() {
        return ";" + batteryLevel + ";" + systemVersionDate + ";" + operationalState;
    }
}