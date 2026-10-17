package domain;

import java.util.Vector;
import java.util.Date;

abstract class Robot extends Entity {

  private double batteryLevel;

  private Date systemVersionDate;

  private String operationalState;

    /**
   * 
   * @element-type Artifact
   */
  public Vector  carries;

}