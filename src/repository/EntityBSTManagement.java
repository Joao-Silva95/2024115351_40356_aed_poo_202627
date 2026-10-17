package repository;

import java.util.List;
import domain.Entity;

public interface EntityBSTManagement {

  List<Entity> searchRobotsByBatteryRange();

  List<Entity> searchArtifactsByScientificValue();

  List<Entity> searchRoomsByHazardIndex();

  List<Entity> searchBySubstring();

}