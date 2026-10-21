package daa.assignment3.dsu;

import java.util.Objects;

/** Disjoint sets with path compression and union by component size. */
public final class DisjointSetUnion {
    public record Metrics(long findCalls, long parentTraversals, long unionCalls) {}

    private final int[] parent;
    private final int[] size;
    private int componentCount;
    private long findCalls;
    private long parentTraversals;
    private long unionCalls;

    public DisjointSetUnion(int elementCount) {
        if (elementCount < 1) {
            throw new IllegalArgumentException("A DSU must contain at least one element");
        }
        parent = new int[elementCount];
        size = new int[elementCount];
        componentCount = elementCount;
        for (int element = 0; element < elementCount; element++) {
            parent[element] = element;
            size[element] = 1;
        }
    }

    public int find(int element) {
        Objects.checkIndex(element, parent.length);
        findCalls++;
        int root = element;
        while (root != parent[root]) {
            parentTraversals++;
            root = parent[root];
        }
        while (element != root) {
            int next = parent[element];
            parent[element] = root;
            element = next;
        }
        return root;
    }

    /** Returns true exactly when two previously separate components merge. */
    public boolean union(int first, int second) {
        unionCalls++;
        int firstRoot = find(first);
        int secondRoot = find(second);
        if (firstRoot == secondRoot) {
            return false;
        }
        if (size[firstRoot] < size[secondRoot]) {
            int temporary = firstRoot;
            firstRoot = secondRoot;
            secondRoot = temporary;
        }
        parent[secondRoot] = firstRoot;
        size[firstRoot] += size[secondRoot];
        componentCount--;
        return true;
    }

    public int componentCount() {
        return componentCount;
    }

    public int sizeOf(int element) {
        return size[find(element)];
    }

    public Metrics metrics() {
        return new Metrics(findCalls, parentTraversals, unionCalls);
    }
}
