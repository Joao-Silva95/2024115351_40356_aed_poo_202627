package domain;


import java.time.LocalDate;

/**
 * Robô de transporte de carga
 */
public class CarrierRobot extends Robot {

  /** Capacidade máxima de carga, em kg */
  private double cargoCapacity;

  /** Peso da carga atual, em kg */
  private double currentCargoWeight;

  /**
   * @param id ID do utilizador ou {@link Entity#UNASSIGNED_ID}
   * @param name nome do robô
   * @param batteryLevel bateria inicial (0 a 100)
   * @param systemVersionDate data da versão do sistema
   * @param operationalState estado operacional
   * @param cargoCapacity capacidade máxima de carga (kg)
   */
  public CarrierRobot(int id, String name, double batteryLevel, LocalDate systemVersionDate, String operationalState, double cargoCapacity) {
    super(id, name, batteryLevel, systemVersionDate, operationalState);
    this.cargoCapacity = cargoCapacity;
    this.currentCargoWeight = 0;
  }

  /** @return capacidade máxima de carga (kg) */
  public double getCargoCapacity() { return cargoCapacity; }

  /** @param cargoCapacity nova capacidade (kg) */
  public void setCargoCapacity(double cargoCapacity) { this.cargoCapacity = cargoCapacity; }

  /** @return peso da carga atual (kg) */
  public double getCurrentCargoWeight() { return currentCargoWeight; }

  /**
   * @param w novo peso da carga (kg)
   * @throws IllegalArgumentException se for negativo ou exceder a capacidade
   */
  public void setCurrentCargoWeight(double w) {
    if (w < 0 || w > cargoCapacity)
      throw new IllegalArgumentException("Carga inválida: " + w);
    this.currentCargoWeight = w;
  }

  public void collectArtifact() {
  }

  public void dropArtifact() {
  }

}