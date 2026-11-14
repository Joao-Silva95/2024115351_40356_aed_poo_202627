package graph.Model;

import domain.Artifacts.Artifact;
import domain.Common.Entity;
import domain.Robots.Robot;
import domain.Environment.Room;
import edu.princeton.cs.algs4.CC;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DijkstraSP;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.KosarajuSharirSCC;
import graph.Results.CollectionStats;
import graph.Enums.RelationType;
import graph.Enums.WeightType;
import graph.Interfaces.GraphManagement;
import graph.Results.PathResult;
import graph.Results.RobotPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.function.Predicate;

/**
 * Grafo dirigido, ponderado, heterogéneo e multi-relacional do labirinto.
 * Os vértices são entidades da BD; as arestas são {@link MultiEdge}. As estruturas do algs4
 * (EdgeWeightedDigraph, Graph, Digraph) são construídas a partir das arestas em cada consulta.
 */
public class LabyrinthGraph implements GraphManagement {

    /** Todas as arestas do grafo. */
    public Vector<MultiEdge> hasEdges = new Vector<>();

    /** Todas as entidades-vértice; a posição é o índice do vértice. */
    public Vector<Entity> hasVertices = new Vector<>();

    /** ID textual de cada vértice, pela mesma ordem de {@link #hasVertices}. */
    private final List<String> vertexIds = new ArrayList<>();

    /** ID textual -> índice do vértice. */
    private final Map<String, Integer> entityIdToIndex = new HashMap<>();

    /** Cria um grafo vazio. */
    public LabyrinthGraph() {
    }

    /**
     * Mantido por compatibilidade: o grafo cresce sozinho, a capacidade é ignorada.
     *
     * @param expectedVertices número esperado de vértices (ignorado)
     */
    public LabyrinthGraph(int expectedVertices) {
    }

    // ---------------------------------------------------------------- R7: construção

    /**
     * Acrescenta um vértice (ignora IDs repetidos).
     *
     * @param id       ID textual do vértice
     * @param entidade entidade associada
     */
    public void adicionarVertice(String id, Entity entidade) {
        if (id == null || entidade == null) throw new IllegalArgumentException("ID ou entidade nulos");
        if (!entityIdToIndex.containsKey(id)) {
            entityIdToIndex.put(id, hasVertices.size());
            hasVertices.add(entidade);
            vertexIds.add(id);
        }
    }

    /** Acrescenta uma aresta do tipo {@link RelationType#CORRIDOR}. */
    public void adicionarAresta(String idOrigem, String idDestino, double dist, double energy, double rad, double time, long timestamp) {
        adicionarAresta(idOrigem, idDestino, RelationType.CORRIDOR, dist, energy, rad, time, timestamp);
    }

    /**
     * Acrescenta uma aresta. Se algum vértice não existir, é ignorada.
     *
     * @throws IllegalArgumentException se algum peso for negativo (o Dijkstra não os aceita)
     */
    public void adicionarAresta(String idOrigem, String idDestino, RelationType type, double dist, double energy, double rad, double time, long timestamp) {
        Integer u = entityIdToIndex.get(idOrigem);
        Integer v = entityIdToIndex.get(idDestino);
        if (u == null || v == null) return;
        if (dist < 0 || energy < 0 || rad < 0 || time < 0)
            throw new IllegalArgumentException("Pesos negativos não são permitidos");
        hasEdges.add(new MultiEdge(u, v, type, dist, energy, rad, time, timestamp));
    }

    /** Acrescenta uma relação sem pesos (evento ou localização). */
    public void adicionarRelacao(String idOrigem, String idDestino, RelationType type, long timestamp) {
        adicionarAresta(idOrigem, idDestino, type, 0, 0, 0, 0, timestamp);
    }

    /** @return número de vértices */
    public int getVertexCount() { return hasVertices.size(); }

    /** @return número de arestas */
    public int getEdgeCount() { return hasEdges.size(); }

    /** @return true se o vértice existe */
    public boolean contains(String id) { return entityIdToIndex.containsKey(id); }

    /** @return a entidade do vértice, ou null se não existir */
    public Entity getEntity(String id) {
        Integer i = entityIdToIndex.get(id);
        return i == null ? null : hasVertices.get(i);
    }

    // ---------------------------------------------------------------- R8a: caminhos

    /** Aresta do algs4 que guarda a MultiEdge original. */
    private static final class EdgeRef extends DirectedEdge {
        final MultiEdge edge;

        EdgeRef(MultiEdge e, double weight) {
            super(e.from(), e.to(), weight);
            this.edge = e;
        }
    }

    /** Constrói o digrafo do algs4 só com as rotas, pesado pelo critério pedido. */
    private EdgeWeightedDigraph buildDigraph(WeightType type) {
        EdgeWeightedDigraph g = new EdgeWeightedDigraph(hasVertices.size());
        for (MultiEdge e : hasEdges)
            if (e.getType().isRoutable()) g.addEdge(new EdgeRef(e, type.of(e)));
        return g;
    }

    /** Converte um caminho do Dijkstra num PathResult. */
    private Optional<PathResult> toResult(DijkstraSP sp, int s, int t) {
        if (!sp.hasPathTo(t)) return Optional.empty();
        List<String> vs = new ArrayList<>();
        List<MultiEdge> es = new ArrayList<>();
        vs.add(vertexIds.get(s));
        for (DirectedEdge e : sp.pathTo(t)) {
            es.add(((EdgeRef) e).edge);
            vs.add(vertexIds.get(e.to()));
        }
        return Optional.of(new PathResult(vs, es, sp.distTo(t)));
    }

    @Override
    public Optional<PathResult> getShortestPath(String from, String to, WeightType type) {
        Integer s = entityIdToIndex.get(from);
        Integer t = entityIdToIndex.get(to);
        if (s == null || t == null) return Optional.empty();
        return toResult(new DijkstraSP(buildDigraph(type), s), s, t);
    }

    // ---------------------------------------------------------------- R8c: conectividade

    @Override
    public boolean isConnected() {
        int n = hasVertices.size();
        if (n <= 1) return true;
        Graph g = new Graph(n);
        for (MultiEdge e : hasEdges)
            if (e.getType().isRoutable()) g.addEdge(e.from(), e.to());
        return new CC(g).count() == 1;
    }

    @Override
    public boolean isStronglyConnected() {
        int n = hasVertices.size();
        if (n <= 1) return true;
        Digraph g = new Digraph(n);
        for (MultiEdge e : hasEdges)
            if (e.getType().isRoutable()) g.addEdge(e.from(), e.to());
        return new KosarajuSharirSCC(g).count() == 1;
    }

    @Override
    public List<String> findIsolatedVertices() {
        boolean[] touched = new boolean[hasVertices.size()];
        for (MultiEdge e : hasEdges)
            if (e.getType().isRoutable()) {
                touched[e.from()] = true;
                touched[e.to()] = true;
            }
        List<String> isolated = new ArrayList<>();
        for (int i = 0; i < touched.length; i++)
            if (!touched[i]) isolated.add(vertexIds.get(i));
        return isolated;
    }

    // ---------------------------------------------------------------- R8b: subgrafos

    @Override
    public LabyrinthGraph extractSubgraph(Predicate<Entity> vertexFilter) {
        LabyrinthGraph sub = new LabyrinthGraph();
        for (int i = 0; i < hasVertices.size(); i++)
            if (vertexFilter.test(hasVertices.get(i))) sub.adicionarVertice(vertexIds.get(i), hasVertices.get(i));
        for (MultiEdge e : hasEdges) {
            String a = vertexIds.get(e.from());
            String b = vertexIds.get(e.to());
            if (sub.contains(a) && sub.contains(b))
                sub.adicionarAresta(a, b, e.getType(), e.getDistance(), e.getEnergyCost(),
                        e.getRadiationDangerLevel(), e.getTransitTime(), e.getEdgeTimestamp());
        }
        return sub;
    }

    @Override
    public LabyrinthGraph extractHighHazardSubgraph(double minHazard) {
        return extractSubgraph(e -> e instanceof Room r && r.getHazardIndex() >= minHazard);
    }

    @Override
    public LabyrinthGraph extractRobotSubgraph() {
        return extractSubgraph(e -> e instanceof Robot);
    }

    // ---------------------------------------------------------------- R8d: recomendação

    @Override
    public Optional<PathResult> recommendOptimalRoute(String robotId, double minScientificValue,
                                                      double maxRadiation) {
        Integer s = entityIdToIndex.get(robotId);
        if (s == null) return Optional.empty();
        Entity origin = hasVertices.get(s);
        if (!(origin instanceof Robot)) return Optional.empty();
        Robot robot = (Robot) origin;

        DijkstraSP sp = new DijkstraSP(buildDigraph(WeightType.ENERGY), s);
        PathResult best = null;
        for (int v = 0; v < hasVertices.size(); v++) {
            Entity ent = hasVertices.get(v);
            if (!(ent instanceof Artifact)) continue;
            Artifact a = (Artifact) ent;
            if (a.getCollectedTimestamp() > 0 || a.getScientificValue() < minScientificValue) continue;
            Optional<PathResult> path = toResult(sp, s, v);
            if (path.isEmpty()) continue;
            PathResult p = path.get();
            if (p.totalEnergy() > robot.getBatteryLevel()) continue;
            if (p.totalRadiation() > maxRadiation) continue;
            if (best == null || p.cost() < best.cost()) best = p;
        }
        return Optional.ofNullable(best);
    }

    // ---------------------------------------------------------------- R8e-g: pesquisas temporais

    @Override
    public CollectionStats getArtifactStatsBetweenTimestamps(String sectorId,
                                                             Class<? extends Artifact> category,
                                                             long from, long to) {
        Set<Integer> inSector = null;
        if (sectorId != null) {
            Integer sec = entityIdToIndex.get(sectorId);
            if (sec == null) return new CollectionStats(0, 0, 0, 0);
            inSector = new HashSet<>();
            for (MultiEdge e : hasEdges)
                if (e.getType() == RelationType.LOCATION && e.from() == sec) inSector.add(e.to());
        }

        Set<Integer> counted = new HashSet<>();
        int count = 0;
        double weight = 0;
        int science = 0;
        for (MultiEdge e : hasEdges) {
            if (e.getType() != RelationType.COLLECTS) continue;
            long ts = e.getEdgeTimestamp();
            if (ts < from || ts > to) continue;
            Entity ent = hasVertices.get(e.to());
            if (!(ent instanceof Artifact)) continue;
            if (category != null && !category.isInstance(ent)) continue;
            if (inSector != null && !inSector.contains(e.to())) continue;
            if (!counted.add(e.to())) continue;
            Artifact a = (Artifact) ent;
            count++;
            weight += a.getWeight();
            science += a.getScientificValue();
        }
        return new CollectionStats(count, weight, science, count == 0 ? 0 : (double) science / count);
    }

    @Override
    public List<Robot> findRobotsThroughHazardousSectors(double minHazard, long from, long to) {
        Set<Robot> result = new LinkedHashSet<>();
        for (MultiEdge e : hasEdges) {
            if (e.getType() != RelationType.TRAVERSAL) continue;
            long ts = e.getEdgeTimestamp();
            if (ts < from || ts > to) continue;
            Entity r = hasVertices.get(e.from());
            Entity s = hasVertices.get(e.to());
            if (r instanceof Robot && s instanceof Room && ((Room) s).getHazardIndex() >= minHazard)
                result.add((Robot) r);
        }
        return new ArrayList<>(result);
    }

    @Override
    public List<RobotPair> findRobotsInSameSector(long from, long to) {
        Map<Integer, Map<Integer, Robot>> bySector = new TreeMap<>();
        for (MultiEdge e : hasEdges) {
            if (e.getType() != RelationType.TRAVERSAL) continue;
            long ts = e.getEdgeTimestamp();
            if (ts < from || ts > to) continue;
            Entity r = hasVertices.get(e.from());
            if (!(r instanceof Robot) || !(hasVertices.get(e.to()) instanceof Room)) continue;
            bySector.computeIfAbsent(e.to(), k -> new TreeMap<>()).put(r.getId(), (Robot) r);
        }
        List<RobotPair> pairs = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, Robot>> entry : bySector.entrySet()) {
            List<Robot> robots = new ArrayList<>(entry.getValue().values());
            for (int i = 0; i < robots.size(); i++)
                for (int j = i + 1; j < robots.size(); j++)
                    pairs.add(new RobotPair(robots.get(i), robots.get(j), vertexIds.get(entry.getKey())));
        }
        return pairs;
    }
}