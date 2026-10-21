package daa.assignment3.connectivity;

import daa.assignment3.graph.WeightedEdge;
import daa.assignment3.graph.WeightedGraph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TopologicalSort {
    private TopologicalSort() {}

    public static TopologicalResult run(WeightedGraph graph) {
        Objects.requireNonNull(graph, "graph");
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological ordering requires a directed graph");
        }

        int[] indegree = new int[graph.vertexCount()];
        long examined = 0;
        for (WeightedEdge edge : graph.edges()) {
            indegree[edge.to()]++;
            examined++;
        }

        ArrayDeque<Integer> ready = new ArrayDeque<>();
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            if (indegree[vertex] == 0) {
                ready.add(vertex);
            }
        }

        List<Integer> order = new ArrayList<>(graph.vertexCount());
        while (!ready.isEmpty()) {
            int vertex = ready.remove();
            order.add(vertex);
            for (var arc : graph.neighbors(vertex)) {
                examined++;
                if (--indegree[arc.to()] == 0) {
                    ready.add(arc.to());
                }
            }
        }

        boolean hasCycle = order.size() != graph.vertexCount();
        var metrics = new TraversalMetrics(order.size(), examined);
        return new TopologicalResult(hasCycle ? List.of() : order, hasCycle, metrics);
    }
}
