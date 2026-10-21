package daa.assignment3.mst;

import daa.assignment3.graph.WeightedEdge;
import daa.assignment3.graph.WeightedGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public final class Prim {
    private Prim() {}

    public static MstResult run(WeightedGraph graph) {
        Objects.requireNonNull(graph, "graph");
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Prim requires an undirected graph");
        }

        long[] comparisons = {0};
        PriorityQueue<WeightedEdge> queue = new PriorityQueue<>((first, second) -> {
            comparisons[0]++;
            int order = Long.compare(first.weight(), second.weight());
            if (order == 0) order = Integer.compare(first.from(), second.from());
            if (order == 0) order = Integer.compare(first.to(), second.to());
            return order;
        });
        boolean[] visited = new boolean[graph.vertexCount()];
        List<WeightedEdge> chosen = new ArrayList<>();
        long totalWeight = 0;
        long examined = 0;
        long offers = 0;
        long polls = 0;
        int components = 0;

        for (int start = 0; start < graph.vertexCount(); start++) {
            if (visited[start]) {
                continue;
            }
            components++;
            visited[start] = true;
            for (var arc : graph.neighbors(start)) {
                examined++;
                if (!visited[arc.to()]) {
                    queue.add(new WeightedEdge(start, arc.to(), arc.weight()));
                    offers++;
                }
            }
            while (!queue.isEmpty()) {
                WeightedEdge edge = queue.remove();
                polls++;
                if (visited[edge.to()]) {
                    continue;
                }
                chosen.add(edge);
                totalWeight = Math.addExact(totalWeight, edge.weight());
                visited[edge.to()] = true;
                for (var arc : graph.neighbors(edge.to())) {
                    examined++;
                    if (!visited[arc.to()]) {
                        queue.add(new WeightedEdge(edge.to(), arc.to(), arc.weight()));
                        offers++;
                    }
                }
            }
        }
        var metrics = new MstMetrics(examined, comparisons[0], offers, polls, 0, 0, 0);
        return new MstResult(totalWeight, chosen, components, metrics);
    }
}
