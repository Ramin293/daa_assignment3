package daa.assignment3.connectivity;

import java.util.List;

public record TopologicalResult(List<Integer> order, boolean hasCycle, TraversalMetrics metrics) {
    public TopologicalResult {
        order = List.copyOf(order);
    }
}
