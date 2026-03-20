package hr.lknezevic.brassbirmingham.model.game;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.graph.SimpleGraph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CityNetwork implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient Graph<CityId, BoardEdge> graph;

    public CityNetwork() {
        buildGraph();
    }

    private void buildGraph() {
        graph = new SimpleGraph<>(BoardEdge.class);
        for (CityId city : CityId.values()) {
            graph.addVertex(city);
        }
        for (BoardEdge edge : BoardDefinition.EDGES) {
            graph.addEdge(edge.getCityA(), edge.getCityB(), edge);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        buildGraph();
    }

    public Set<CityId> getAllCities() {
        return graph.vertexSet();
    }

    public Set<BoardEdge> getAllEdges() {
        return graph.edgeSet();
    }

    public Set<BoardEdge> edgesOf(CityId city) {
        return graph.edgesOf(city);
    }

    public boolean areAdjacent(CityId a, CityId b) {
        return graph.containsEdge(a, b);
    }

    public boolean isConnected(CityId source, CityId target, Set<BoardEdge> activeLinks) {
        if (source == target) return true;
        Graph<CityId, BoardEdge> subgraph = new SimpleGraph<>(BoardEdge.class);
        for (CityId city : CityId.values()) {
            subgraph.addVertex(city);
        }
        for (BoardEdge edge : activeLinks) {
            subgraph.addEdge(edge.getCityA(), edge.getCityB(), edge);
        }
        var inspector = new ConnectivityInspector<>(subgraph);
        return inspector.pathExists(source, target);
    }

    public int shortestDistance(CityId source, CityId target, Set<BoardEdge> activeLinks) {
        if (source == target) return 0;
        Graph<CityId, BoardEdge> subgraph = new SimpleGraph<>(BoardEdge.class);
        for (CityId city : CityId.values()) {
            subgraph.addVertex(city);
        }
        for (BoardEdge edge : activeLinks) {
            subgraph.addEdge(edge.getCityA(), edge.getCityB(), edge);
        }
        var bfs = new BFSShortestPath<>(subgraph);
        var path = bfs.getPath(source, target);
        return path == null ? Integer.MAX_VALUE : path.getLength();
    }

    public Set<CityId> playerNetwork(int playerId, Board board) {
        Set<CityId> network = new HashSet<>();
        for (var placed : board.getPlacedIndustries()) {
            if (placed.getOwnerId() == playerId) {
                network.add(placed.getCity());
            }
        }
        for (var link : board.getPlacedLinks()) {
            if (link.getOwnerId() == playerId) {
                network.add(link.getEdge().getCityA());
                network.add(link.getEdge().getCityB());
            }
        }
        return network;
    }

    public Set<BoardEdge> activeLinks(Board board) {
        Set<BoardEdge> links = new HashSet<>();
        for (var link : board.getPlacedLinks()) {
            links.add(link.getEdge());
        }
        return links;
    }

    public List<CityId> getNeighbors(CityId city) {
        return graph.edgesOf(city).stream()
                .map(e -> e.getCityA() == city ? e.getCityB() : e.getCityA())
                .toList();
    }
}
