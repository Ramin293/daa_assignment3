package daa.assignment3.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A weighted adjacency-list graph with vertices numbered 0 through V - 1. */
public final class WeightedGraph {
    public record Arc(int to, long weight) {}

    private final boolean directed;
    private final List<WeightedEdge> edges = new ArrayList<>();
    private final List<WeightedEdge> edgeView = Collections.unmodifiableList(edges);
    private final List<List<Arc>> adjacency;
    private final List<List<Arc>> adjacencyViews;

    public WeightedGraph(int vertexCount, boolean directed) {
        if (vertexCount < 1) {
            throw new IllegalArgumentException("A graph must contain at least one vertex");
        }
        this.directed = directed;
        adjacency = new ArrayList<>(vertexCount);
        adjacencyViews = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            List<Arc> arcs = new ArrayList<>();
            adjacency.add(arcs);
            adjacencyViews.add(Collections.unmodifiableList(arcs));
        }
    }

    public int vertexCount() {
        return adjacency.size();
    }

    public int edgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public void addEdge(int from, int to, long weight) {
        checkVertex(from);
        checkVertex(to);
        edges.add(new WeightedEdge(from, to, weight));
        adjacency.get(from).add(new Arc(to, weight));
        if (!directed && from != to) {
            adjacency.get(to).add(new Arc(from, weight));
        }
    }

    public List<WeightedEdge> edges() {
        return edgeView;
    }

    public List<Arc> neighbors(int vertex) {
        checkVertex(vertex);
        return adjacencyViews.get(vertex);
    }

    private void checkVertex(int vertex) {
        Objects.checkIndex(vertex, vertexCount());
    }
}
