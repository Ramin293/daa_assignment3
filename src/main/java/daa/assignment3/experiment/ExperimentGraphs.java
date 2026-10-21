package daa.assignment3.experiment;

import daa.assignment3.graph.WeightedGraph;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class ExperimentGraphs {
    private ExperimentGraphs() {}

    public static WeightedGraph connectedUndirected(int vertices, int edges, long seed) {
        validateSize(vertices, edges);
        WeightedGraph graph = new WeightedGraph(vertices, false);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();
        for (int vertex = 1; vertex < vertices; vertex++) {
            addUndirected(graph, used, vertex, random.nextInt(vertex), random);
        }
        while (graph.edgeCount() < edges) {
            int first = random.nextInt(vertices);
            int second = random.nextInt(vertices);
            if (first != second) {
                addUndirected(graph, used, first, second, random);
            }
        }
        return graph;
    }

    public static WeightedGraph directed(int vertices, int edges, long seed, boolean cyclic) {
        validateSize(vertices, edges);
        if (cyclic && edges < vertices) {
            throw new IllegalArgumentException("A chain and its back edge need at least V edges");
        }
        WeightedGraph graph = new WeightedGraph(vertices, true);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();
        for (int vertex = 0; vertex + 1 < vertices; vertex++) {
            addDirected(graph, used, vertex, vertex + 1, random);
        }
        int forwardTarget = cyclic ? edges - 1 : edges;
        while (graph.edgeCount() < forwardTarget) {
            int first = random.nextInt(vertices - 1);
            int second = first + 1 + random.nextInt(vertices - first - 1);
            addDirected(graph, used, first, second, random);
        }
        if (cyclic) {
            graph.addEdge(vertices - 1, 0, 1 + random.nextInt(1_000));
        }
        return graph;
    }

    private static void validateSize(int vertices, int edges) {
        if (vertices < 2 || edges < vertices - 1
                || edges > (long) vertices * (vertices - 1) / 2) {
            throw new IllegalArgumentException("Expected V >= 2 and V-1 <= E <= V(V-1)/2");
        }
    }

    private static void addUndirected(WeightedGraph graph, Set<Long> used,
            int first, int second, Random random) {
        int from = Math.min(first, second);
        int to = Math.max(first, second);
        if (used.add(key(from, to))) {
            graph.addEdge(from, to, 1 + random.nextInt(1_000));
        }
    }

    private static void addDirected(WeightedGraph graph, Set<Long> used,
            int from, int to, Random random) {
        if (used.add(key(from, to))) {
            graph.addEdge(from, to, 1 + random.nextInt(1_000));
        }
    }

    private static long key(int from, int to) {
        return ((long) from << 32) | (to & 0xffffffffL);
    }
}
