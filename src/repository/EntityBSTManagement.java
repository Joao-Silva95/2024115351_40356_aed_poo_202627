package repository;

import java.util.List;

import domain.Entity;

public interface EntityBSTManagement {

    List<Entity> searchRobotsByBatteryRange(double minBattery, double maxBattery);

    List<Entity> searchArtifactsByScientificValue(double minVal, double maxVal);

    List<Entity> searchRoomsByHazardIndex(double minHazard, double maxHazard);

    List<Entity> searchBySubstring(String query);

}