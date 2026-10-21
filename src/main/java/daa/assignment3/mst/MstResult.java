package daa.assignment3.mst;

import daa.assignment3.graph.WeightedEdge;
import java.util.List;

/** A minimum spanning forest; it is an MST when componentCount is one. */
public record MstResult(long totalWeight, List<WeightedEdge> edges, int componentCount, MstMetrics metrics) {
    public MstResult {
        edges = List.copyOf(edges);
    }

    public boolean connected() {
        return componentCount == 1;
    }
}
