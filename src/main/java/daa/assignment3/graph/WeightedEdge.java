package daa.assignment3.graph;

/** One logical road or dependency; undirected edges are stored only once. */
public record WeightedEdge(int from, int to, long weight) {
    public WeightedEdge {
        if (from < 0 || to < 0) {
            throw new IllegalArgumentException("Vertex identifiers must be non-negative");
        }
    }
}
