package graph;

import java.util.List;

public interface GraphManagement {

    List<String> getShortestPath();

    boolean isConnected();

    void extractSubgraph();

    List<String> recommendOptimalRoute();

}