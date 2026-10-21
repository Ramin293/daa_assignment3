package daa.assignment3.shortestpath;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

public final class ShortestPathResult {
    private final int source;
    private final long[] distances;
    private final boolean[] reachable;
    private final int[] predecessor;
    private final ShortestPathMetrics metrics;

    ShortestPathResult(int source, long[] distances, boolean[] reachable,
            int[] predecessor, ShortestPathMetrics metrics) {
        this.source = source;
        this.distances = distances.clone();
        this.reachable = reachable.clone();
        this.predecessor = predecessor.clone();
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public int source() {
        return source;
    }

    public int vertexCount() {
        return distances.length;
    }

    public boolean isReachable(int vertex) {
        Objects.checkIndex(vertex, vertexCount());
        return reachable[vertex];
    }

    public OptionalLong distanceTo(int vertex) {
        return isReachable(vertex) ? OptionalLong.of(distances[vertex]) : OptionalLong.empty();
    }

    public List<Integer> pathTo(int vertex) {
        if (!isReachable(vertex)) {
            return List.of();
        }
        ArrayDeque<Integer> path = new ArrayDeque<>();
        for (int current = vertex; current != -1; current = predecessor[current]) {
            path.addFirst(current);
        }
        return List.copyOf(path);
    }

    public ShortestPathMetrics metrics() {
        return metrics;
    }
}
