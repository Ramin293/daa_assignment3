package daa.assignment3.mst;

import daa.assignment3.dsu.DisjointSetUnion;
import daa.assignment3.graph.WeightedEdge;
import daa.assignment3.graph.WeightedGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Kruskal {
    private Kruskal() {}

    public static MstResult run(WeightedGraph graph) {
        Objects.requireNonNull(graph, "graph");
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Kruskal requires an undirected graph");
        }

        List<WeightedEdge> sorted = new ArrayList<>(graph.edges());
        long[] comparisons = {0};
        sorted.sort((first, second) -> {
            comparisons[0]++;
            int order = Long.compare(first.weight(), second.weight());
            if (order == 0) order = Integer.compare(first.from(), second.from());
            if (order == 0) order = Integer.compare(first.to(), second.to());
            return order;
        });

        DisjointSetUnion sets = new DisjointSetUnion(graph.vertexCount());
        List<WeightedEdge> chosen = new ArrayList<>();
        long totalWeight = 0;
        long examined = 0;
        for (WeightedEdge edge : sorted) {
            if (chosen.size() == graph.vertexCount() - 1) {
                break;
            }
            examined++;
            if (sets.union(edge.from(), edge.to())) {
                chosen.add(edge);
                totalWeight = Math.addExact(totalWeight, edge.weight());
            }
        }
        var dsu = sets.metrics();
        var metrics = new MstMetrics(examined, comparisons[0], 0, 0,
                dsu.findCalls(), dsu.parentTraversals(), dsu.unionCalls());
        return new MstResult(totalWeight, chosen, sets.componentCount(), metrics);
    }
}
