package domain;

import java.time.LocalDate;

/**
 * Robô explorador, que analisa o labirinto dentro de um raio de varrimento
 */
public class ExplorerRobot extends Robot {

    /**
     * Raio de varrimento, em metros
     */
    private double scanRadius;

    /**
     * @param id                ID do utilizador ou {@link Entity#UNASSIGNED_ID}
     * @param name              nome do robô
     * @param batteryLevel      bateria inicial (0 a 100)
     * @param systemVersionDate data da versão do sistema
     * @param operationalState  estado operacional
     * @param scanRadius        raio de varrimento (m)
     */
    public ExplorerRobot(int id, String name, double batteryLevel, LocalDate systemVersionDate, String operationalState, double scanRadius) {
        super(id, name, batteryLevel, systemVersionDate, operationalState);
        this.scanRadius = scanRadius;
    }

    /**
     * @return raio de varrimento (m)
     */
    public double getScanRadius() {
        return scanRadius;
    }

    /**
     * @param scanRadius novo raio de varrimento (m)
     */
    public void setScanRadius(double scanRadius) {
        this.scanRadius = scanRadius;
    }

    public void scanSector() {
    }
}