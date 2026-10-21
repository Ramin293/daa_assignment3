package daa.assignment3;

import daa.assignment3.dsu.DisjointSetUnion;
import daa.assignment3.graph.WeightedGraph;
import daa.assignment3.graph.WeightedEdge;
import daa.assignment3.mst.Kruskal;
import daa.assignment3.mst.MstResult;
import daa.assignment3.mst.Prim;
import java.util.Random;

public final class CorrectnessSuite {
    private static int checks;

    private CorrectnessSuite() {}

    public static String runAll() {
        checks = 0;
        graphRepresentation();
        disjointSets();
        minimumSpanningTrees();
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
        require(sets.metrics().unionCalls() == 4, "DSU counts union attempts");

        DisjointSetUnion compression = new DisjointSetUnion(4);
        compression.union(0, 1);
        compression.union(2, 3);
        compression.union(0, 2);
        long before = compression.metrics().parentTraversals();
        compression.find(3);
        long firstTraversal = compression.metrics().parentTraversals() - before;
        compression.find(3);
        long secondTraversal = compression.metrics().parentTraversals() - before - firstTraversal;
        require(firstTraversal == 2 && secondTraversal == 1, "Path compression shortens later finds");
    }

    private static void minimumSpanningTrees() {
        WeightedGraph roads = new WeightedGraph(4, false);
        roads.addEdge(0, 1, 4);
        roads.addEdge(0, 2, 3);
        roads.addEdge(1, 2, 1);
        roads.addEdge(1, 3, 2);
        roads.addEdge(2, 3, 4);
        checkBoth(roads, 6, true);

        WeightedGraph ties = new WeightedGraph(4, false);
        ties.addEdge(0, 1, 1);
        ties.addEdge(1, 2, 1);
        ties.addEdge(2, 3, 1);
        ties.addEdge(3, 0, 1);
        ties.addEdge(0, 2, 2);
        ties.addEdge(1, 3, 2);
        checkBoth(ties, 3, true);

        WeightedGraph disconnected = new WeightedGraph(5, false);
        disconnected.addEdge(0, 1, 4);
        disconnected.addEdge(1, 2, 3);
        disconnected.addEdge(0, 2, 9);
        disconnected.addEdge(3, 4, 2);
        checkBoth(disconnected, 9, false);
        require(Kruskal.run(disconnected).componentCount() == 2, "Kruskal counts components");
        require(Prim.run(disconnected).componentCount() == 2, "Prim counts components");

        WeightedGraph negative = new WeightedGraph(3, false);
        negative.addEdge(0, 1, -5);
        negative.addEdge(1, 2, 3);
        negative.addEdge(0, 2, 9);
        checkBoth(negative, -2, true);

        WeightedGraph single = new WeightedGraph(1, false);
        single.addEdge(0, 0, -10);
        checkBoth(single, 0, true);

        WeightedGraph directed = new WeightedGraph(2, true);
        directed.addEdge(0, 1, 1);
        expectThrows(IllegalArgumentException.class, () -> Kruskal.run(directed));
        expectThrows(IllegalArgumentException.class, () -> Prim.run(directed));

        WeightedGraph overflow = new WeightedGraph(3, false);
        overflow.addEdge(0, 1, Long.MAX_VALUE);
        overflow.addEdge(1, 2, 1);
        expectThrows(ArithmeticException.class, () -> Kruskal.run(overflow));
        expectThrows(ArithmeticException.class, () -> Prim.run(overflow));

        Random random = new Random(20261021L);
        for (int trial = 0; trial < 100; trial++) {
            int vertices = 2 + random.nextInt(7);
            WeightedGraph generated = new WeightedGraph(vertices, false);
            for (int vertex = 1; vertex < vertices; vertex++) {
                generated.addEdge(vertex, random.nextInt(vertex), random.nextInt(26) - 5);
            }
            for (int extra = 0; extra < vertices; extra++) {
                generated.addEdge(random.nextInt(vertices), random.nextInt(vertices), random.nextInt(26) - 5);
            }
            MstResult kruskal = Kruskal.run(generated);
            MstResult prim = Prim.run(generated);
            require(kruskal.totalWeight() == prim.totalWeight(), "MST weights agree on generated graph");
            require(kruskal.connected() && prim.connected(), "Generated graph stays connected");
            checkForest(generated, kruskal);
            checkForest(generated, prim);
        }
    }

    private static void checkBoth(WeightedGraph graph, long expectedWeight, boolean connected) {
        MstResult kruskal = Kruskal.run(graph);
        MstResult prim = Prim.run(graph);
        require(kruskal.totalWeight() == expectedWeight, "Kruskal weight");
        require(prim.totalWeight() == expectedWeight, "Prim weight");
        require(kruskal.connected() == connected, "Kruskal connectivity");
        require(prim.connected() == connected, "Prim connectivity");
        checkForest(graph, kruskal);
        checkForest(graph, prim);
        require(kruskal.metrics().dsuUnionCalls() == kruskal.metrics().edgeExaminations(),
                "Kruskal attempts one union per examined edge");
        require(kruskal.metrics().dsuFindCalls() == 2 * kruskal.metrics().dsuUnionCalls(),
                "Each union calls find twice");
        require(prim.metrics().queueOffers() == prim.metrics().queuePolls(),
                "Prim drains its priority queue");
    }

    private static void checkForest(WeightedGraph graph, MstResult result) {
        DisjointSetUnion forest = new DisjointSetUnion(graph.vertexCount());
        long sum = 0;
        for (WeightedEdge edge : result.edges()) {
            boolean present = graph.edges().stream().anyMatch(original ->
                    original.weight() == edge.weight()
                            && ((original.from() == edge.from() && original.to() == edge.to())
                            || (original.from() == edge.to() && original.to() == edge.from())));
            require(present, "Selected edge belongs to the input graph");
            require(forest.union(edge.from(), edge.to()), "Selected edges contain no cycle");
            sum = Math.addExact(sum, edge.weight());
        }
        require(sum == result.totalWeight(), "Reported weight matches selected edges");
        require(result.edges().size() == graph.vertexCount() - result.componentCount(),
                "Forest has V minus component-count edges");
        require(forest.componentCount() == result.componentCount(), "Forest component count agrees");
        for (WeightedEdge edge : graph.edges()) {
            require(forest.find(edge.from()) == forest.find(edge.to()),
                    "Selected forest spans each input component");
        }
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
