package daa.assignment3.connectivity;

import java.util.List;

public record SccResult(List<List<Integer>> components, TraversalMetrics metrics) {
    public SccResult {
        components = components.stream().map(List::copyOf).toList();
    }
}
