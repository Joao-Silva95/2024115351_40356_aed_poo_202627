package graph;

import domain.Entity;
import java.util.Vector;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.DirectedEdge;

public class LabyrinthGraph implements GraphManagement {

    public Vector<MultiEdge> hasEdges = new Vector<>();
    public Vector<Entity> hasVertices = new Vector<>();

    private EdgeWeightedDigraph algs4Graph;
    private Map<String, Integer> entityIdToIndex = new HashMap<>();

    public LabyrinthGraph(int numVertices) {
      this.algs4Graph = new EdgeWeightedDigraph(numVertices);
    }

    public void adicionarVertice(String id, Entity entidade) {
      if (!entityIdToIndex.containsKey(id)) {
        int index = hasVertices.size();
        entityIdToIndex.put(id, index);
        hasVertices.add(entidade);
      }
    }

    public void adicionarAresta(String idOrigem, String idDestino, double dist, double energy, double rad, double time, long timestamp) {
      Integer u = entityIdToIndex.get(idOrigem);
      Integer v = entityIdToIndex.get(idDestino);

      if (u != null && v != null) {
        // Adiciona ao algs4 usando a distância como peso principal de referência
        algs4Graph.addEdge(new DirectedEdge(u, v, dist));

        // Guarda também a aresta multi-peso
        hasEdges.add(new MultiEdge(u, v, dist, energy, rad, time, timestamp));
      }
    }
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