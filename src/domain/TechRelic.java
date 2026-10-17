package domain;

/**
 * Relíquia tecnológica de uma civilização anterior.
 */
public class TechRelic extends Artifact {

  /** Época histórica a que a relíquia pertence (ex.: "Pre-Collapse"). */
  private String historicalEra;

  /**
   * @param id ID do utilizador ou {@link Entity#UNASSIGNED_ID}
   * @param name descrição
   * @param weight peso (kg)
   * @param scientificValue valor científico
   * @param dangerLevel nível de perigo
   * @param historicalEra época histórica
   */
  public TechRelic(int id, String name, double weight, int scientificValue,
                   int dangerLevel, String historicalEra) {
    super(id, name, weight, scientificValue, dangerLevel);
    this.historicalEra = historicalEra;
  }

  /** @return época histórica */
  public String getHistoricalEra() { return historicalEra; }

  /** @param historicalEra nova época histórica */
  public void setHistoricalEra(String historicalEra) { this.historicalEra = historicalEra; }
}