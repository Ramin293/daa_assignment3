package daa.assignment3;

import daa.assignment3.dsu.DisjointSetUnion;
import daa.assignment3.graph.WeightedGraph;

public final class CorrectnessSuite {
    private static int checks;

    private CorrectnessSuite() {}

    public static String runAll() {
        checks = 0;
        graphRepresentation();
        disjointSets();
        return "PASS: " + checks + " checks";
    }

    private static void graphRepresentation() {
        WeightedGraph roads = new WeightedGraph(3, false);
        roads.addEdge(0, 1, 7);
        roads.addEdge(0, 1, 7);
        roads.addEdge(2, 2, -3);
        require(roads.edgeCount() == 3, "Each road is stored once");
        require(roads.neighbors(0).size() == 2, "Parallel roads are retained");
        require(roads.neighbors(1).size() == 2, "Undirected roads have reverse arcs");
        require(roads.neighbors(2).size() == 1, "A self-loop has one arc");
        require(roads.edges().get(2).weight() == -3, "The graph permits MST weights below zero");
        expectThrows(UnsupportedOperationException.class, () -> roads.edges().clear());
        expectThrows(IndexOutOfBoundsException.class, () -> roads.addEdge(0, 3, 1));

        WeightedGraph oneWay = new WeightedGraph(2, true);
        oneWay.addEdge(0, 1, 5);
        require(oneWay.neighbors(1).isEmpty(), "Directed edges have no reverse arc");
        require(new WeightedGraph(1, false).edgeCount() == 0, "Single-vertex graph works");
    }

    private static void disjointSets() {
        DisjointSetUnion sets = new DisjointSetUnion(5);
        require(sets.union(0, 1), "First merge succeeds");
        require(sets.union(2, 3), "Second merge succeeds");
        require(sets.union(1, 2), "Components merge transitively");
        require(!sets.union(0, 3), "Redundant merge reports false");
        require(sets.find(3) == sets.find(0), "Merged vertices share a root");
        require(sets.sizeOf(2) == 4, "Component size is updated");
        require(sets.componentCount() == 2, "Component count is updated");
        require(sets.find(4) != sets.find(0), "Unmerged vertex remains separate");
        expectThrows(IndexOutOfBoundsException.class, () -> sets.find(5));
    }

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expectThrows(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable error) {
            if (type.isInstance(error)) {
                checks++;
                return;
            }
            throw new AssertionError("Expected " + type.getSimpleName(), error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
