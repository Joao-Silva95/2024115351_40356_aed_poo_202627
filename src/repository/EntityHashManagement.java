package repository;

import java.util.List;
import domain.Entity;

public interface EntityHashManagement {

  void insertEntity();

  void removeEntity();

  Entity searchEntityById();

  List<Entity> listAllEntities();

}