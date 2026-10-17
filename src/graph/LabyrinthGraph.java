package graph;

import java.util.Vector;
import java.util.List;

public class LabyrinthGraph implements GraphManagement {

    /**
   *
   * @element-type MultiEdge
   */
  public Vector  hasEdges;
    /**
   *
   * @element-type Entity
   */
  public Vector  hasVertices;

  @Override
  public List<String> getShortestPath() {
    return null;
  }

  @Override
  public void extractSubgraph() {
  }

  @Override
  public boolean isConnected() {
    return false;
  }


  @Override
  public List<String> recommendOptimalRoute() {
    return null;
  }

  public void getArtifactStatsBetweenTimestamps() {
  }

}