package domain;

import java.time.LocalDate;

/**
 * Sala do labirinto. Pode funcionar como estação de manutenção.
 */
public class Room extends Entity {

    /**
     * Tipo de zona (ex.: "STORAGE", "REACTOR").
     */
    private String zoneType;

    /**
     * Nível de segurança (0 a 5).
     */
    private int securityLevel;

    /**
     * Índice de perigo ambiental.
     */
    private double hazardIndex;

    /**
     * Data em que a sala foi detetada.
     */
    private LocalDate detectionDate;

    /**
     * Indica se a sala é uma estação de manutenção.
     */
    private boolean maintenanceRoom;

    /**
     * @param id              ID do utilizador ou {@link Entity#UNASSIGNED_ID}
     * @param name            código da sala
     * @param zoneType        tipo de zona
     * @param securityLevel   nível de segurança
     * @param hazardIndex     índice de perigo
     * @param detectionDate   data de deteção
     * @param maintenanceRoom true se a sala for uma estação de manutenção
     */
    public Room(int id, String name, String zoneType, int securityLevel,
                double hazardIndex, LocalDate detectionDate, boolean maintenanceRoom) {
        super(id, name);
        this.zoneType = zoneType;
        this.securityLevel = securityLevel;
        this.hazardIndex = hazardIndex;
        this.detectionDate = detectionDate;
        this.maintenanceRoom = maintenanceRoom;
    }

    /**
     * @return true se a sala é uma estação de manutenção
     */
    public boolean isMaintenanceRoom() {
        return maintenanceRoom;
    }

    /**
     * @param maintenanceRoom true se a sala passa a ser estação de manutenção
     */
    public void setMaintenanceRoom(boolean maintenanceRoom) {
        this.maintenanceRoom = maintenanceRoom;
    }

    /**
     * @return tipo de zona
     */
    public String getZoneType() {
        return zoneType;
    }

    /**
     * @param zoneType novo tipo de zona
     */
    public void setZoneType(String zoneType) {
        this.zoneType = zoneType;
    }

    /**
     * @return nível de segurança
     */
    public int getSecurityLevel() {
        return securityLevel;
    }

    /**
     * @param securityLevel novo nível de segurança
     */
    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    /**
     * @return índice de perigo
     */
    public double getHazardIndex() {
        return hazardIndex;
    }

    /**
     * @param hazardIndex novo índice de perigo
     */
    public void setHazardIndex(double hazardIndex) {
        this.hazardIndex = hazardIndex;
    }

    /**
     * @return data de deteção
     */
    public LocalDate getDetectionDate() {
        return detectionDate;
    }

    /**
     * @param detectionDate nova data de deteção
     */
    public void setDetectionDate(LocalDate detectionDate) {
        this.detectionDate = detectionDate;
    }

    @Override
    public String toString() {
        return super.toString() + "{zone=" + zoneType + ", security=" + securityLevel
                + ", hazard=" + hazardIndex + ", maintenance=" + maintenanceRoom + "}";
    }

    /**
     * @return alias para o índice de perigo ambiental (R3)
     */
    public double getEnvironmentalHazardIndex() {
        return getHazardIndex();
    }

    /**
     * @return alias para o código da sala, herdado de Entity (R3)
     */
    public String getRoomCode() {
        return getName();
    }
}