package daa.assignment3.shortestpath;

import daa.assignment3.graph.WeightedEdge;
import daa.assignment3.graph.WeightedGraph;
import java.util.Arrays;
import java.util.Objects;
import java.util.PriorityQueue;

public final class Dijkstra {
    private record State(int vertex, long distance) {}

    private Dijkstra() {}

    public static ShortestPathResult run(WeightedGraph graph, int source) {
        Objects.requireNonNull(graph, "graph");
        Objects.checkIndex(source, graph.vertexCount());
        for (WeightedEdge edge : graph.edges()) {
            if (edge.weight() < 0) {
                throw new IllegalArgumentException("Dijkstra requires non-negative edge weights");
            }
        }

        long[] distance = new long[graph.vertexCount()];
        boolean[] reachable = new boolean[graph.vertexCount()];
        int[] predecessor = new int[graph.vertexCount()];
        Arrays.fill(predecessor, -1);
        long[] comparisons = {0};
        PriorityQueue<State> queue = new PriorityQueue<>((first, second) -> {
            comparisons[0]++;
            int order = Long.compare(first.distance(), second.distance());
            return order != 0 ? order : Integer.compare(first.vertex(), second.vertex());
        });

        reachable[source] = true;
        queue.add(new State(source, 0));
        long examined = 0;
        long relaxations = 0;
        long offers = 1;
        long polls = 0;
        while (!queue.isEmpty()) {
            State current = queue.remove();
            polls++;
            if (current.distance() != distance[current.vertex()]) {
                continue;
            }
            for (var arc : graph.neighbors(current.vertex())) {
                examined++;
                long candidate = Math.addExact(current.distance(), arc.weight());
                if (!reachable[arc.to()] || candidate < distance[arc.to()]) {
                    distance[arc.to()] = candidate;
                    reachable[arc.to()] = true;
                    predecessor[arc.to()] = current.vertex();
                    queue.add(new State(arc.to(), candidate));
                    relaxations++;
                    offers++;
                }
            }
        }
        var metrics = new ShortestPathMetrics(examined, relaxations, offers, polls, comparisons[0]);
        return new ShortestPathResult(source, distance, reachable, predecessor, metrics);
    }
}
