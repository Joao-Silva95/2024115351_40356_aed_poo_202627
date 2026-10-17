package repository;

import java.util.List;
import domain.Entity;

class DatabaseManager implements EntityHashManagement, EntityBSTManagement {

  @Override
  public void insertEntity() {
  }

  @Override
  public void removeEntity() {
  }

  @Override
  public Entity searchEntityById() {
    return null;
  }

  @Override
  public List<Entity> listAllEntities() {
    return null;
  }

  @Override
  public List<Entity> searchRobotsByBatteryRange() {
    return null;
  }

  @Override
  public List<Entity> searchArtifactsByScientificValue() {
    return null;
  }

  @Override
  public List<Entity> searchRoomsByHazardIndex() {
    return null;
  }

  @Override
  public List<Entity> searchBySubstring() {
    return null;
  }

  public void searchBySymbolTable() {
  }

  public void searchByRedBlackBST() {
  }

  public void validateConsistencyAndArchive() {
  }

}