package daa.assignment3;

import daa.assignment3.dsu.DisjointSetUnion;
import daa.assignment3.connectivity.Kosaraju;
import daa.assignment3.connectivity.SccResult;
import daa.assignment3.connectivity.TopologicalResult;
import daa.assignment3.connectivity.TopologicalSort;
import daa.assignment3.experiment.ExperimentGraphs;
import daa.assignment3.graph.WeightedGraph;
import daa.assignment3.graph.WeightedEdge;
import daa.assignment3.mst.Kruskal;
import daa.assignment3.mst.MstResult;
import daa.assignment3.mst.Prim;
import daa.assignment3.shortestpath.Dijkstra;
import daa.assignment3.shortestpath.ShortestPathResult;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class CorrectnessSuite {
    private static int checks;

    private CorrectnessSuite() {}

    public static String runAll() {
        checks = 0;
        graphRepresentation();
        disjointSets();
        minimumSpanningTrees();
        directedConnectivity();
        shortestPaths();
        generatedExperimentGraphs();
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

    private static void directedConnectivity() {
        WeightedGraph dag = new WeightedGraph(6, true);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 2, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(2, 4, 1);
        dag.addEdge(3, 5, 1);
        dag.addEdge(4, 5, 1);
        checkTopologicalOrder(dag);
        checkComponents(dag, Kosaraju.run(dag));

        WeightedGraph cycle = new WeightedGraph(6, true);
        cycle.addEdge(0, 1, 1);
        cycle.addEdge(1, 2, 1);
        cycle.addEdge(2, 0, 1);
        cycle.addEdge(2, 3, 1);
        cycle.addEdge(3, 4, 1);
        cycle.addEdge(4, 3, 1);
        cycle.addEdge(4, 5, 1);
        require(TopologicalSort.run(cycle).hasCycle(), "A cycle prevents topological ordering");
        require(TopologicalSort.run(cycle).order().isEmpty(), "Cyclic graph has no partial order");
        SccResult components = Kosaraju.run(cycle);
        checkComponents(cycle, components);
        require(components.components().size() == 3, "Graph has three SCCs");

        WeightedGraph selfLoop = new WeightedGraph(1, true);
        selfLoop.addEdge(0, 0, 1);
        require(TopologicalSort.run(selfLoop).hasCycle(), "A self-loop is a cycle");
        checkComponents(selfLoop, Kosaraju.run(selfLoop));
        checkTopologicalOrder(new WeightedGraph(1, true));

        WeightedGraph undirected = new WeightedGraph(2, false);
        expectThrows(IllegalArgumentException.class, () -> TopologicalSort.run(undirected));
        expectThrows(IllegalArgumentException.class, () -> Kosaraju.run(undirected));

        Random random = new Random(20261022L);
        for (int trial = 0; trial < 50; trial++) {
            int vertices = 2 + random.nextInt(7);
            WeightedGraph generated = new WeightedGraph(vertices, true);
            for (int vertex = 0; vertex + 1 < vertices; vertex++) {
                generated.addEdge(vertex, vertex + 1, 1);
            }
            for (int extra = 0; extra < vertices; extra++) {
                int from = random.nextInt(vertices - 1);
                int to = from + 1 + random.nextInt(vertices - from - 1);
                generated.addEdge(from, to, 1);
            }
            checkTopologicalOrder(generated);
            checkComponents(generated, Kosaraju.run(generated));
            generated.addEdge(vertices - 1, 0, 1);
            require(TopologicalSort.run(generated).hasCycle(), "Added back edge creates a cycle");
            require(Kosaraju.run(generated).components().size() == 1,
                    "Chain plus back edge is strongly connected");
        }

        for (int trial = 0; trial < 60; trial++) {
            int vertices = 1 + random.nextInt(8);
            WeightedGraph generated = new WeightedGraph(vertices, true);
            for (int edge = 0; edge < 2 * vertices; edge++) {
                generated.addEdge(random.nextInt(vertices), random.nextInt(vertices), 1);
            }
            checkComponents(generated, Kosaraju.run(generated));
        }
    }

    private static void checkTopologicalOrder(WeightedGraph graph) {
        TopologicalResult result = TopologicalSort.run(graph);
        require(!result.hasCycle(), "DAG has a topological ordering");
        require(result.order().size() == graph.vertexCount(), "Order contains every vertex");
        int[] position = new int[graph.vertexCount()];
        Arrays.fill(position, -1);
        for (int index = 0; index < result.order().size(); index++) {
            int vertex = result.order().get(index);
            require(position[vertex] == -1, "Order contains each vertex once");
            position[vertex] = index;
        }
        for (WeightedEdge edge : graph.edges()) {
            require(position[edge.from()] < position[edge.to()], "Every edge points forward");
        }
        require(result.metrics().edgeExaminations() == 2L * graph.edgeCount(),
                "Kahn examines each edge in both phases");
    }

    private static void checkComponents(WeightedGraph graph, SccResult result) {
        int vertices = graph.vertexCount();
        int[] membership = new int[vertices];
        Arrays.fill(membership, -1);
        for (int index = 0; index < result.components().size(); index++) {
            List<Integer> component = result.components().get(index);
            require(!component.isEmpty(), "SCC is nonempty");
            for (int vertex : component) {
                require(membership[vertex] == -1, "Vertex belongs to one SCC");
                membership[vertex] = index;
            }
        }
        for (int vertex = 0; vertex < vertices; vertex++) {
            require(membership[vertex] != -1, "Every vertex belongs to an SCC");
        }

        boolean[][] reachable = new boolean[vertices][vertices];
        for (int vertex = 0; vertex < vertices; vertex++) {
            reachable[vertex][vertex] = true;
        }
        for (WeightedEdge edge : graph.edges()) {
            reachable[edge.from()][edge.to()] = true;
        }
        for (int via = 0; via < vertices; via++) {
            for (int from = 0; from < vertices; from++) {
                for (int to = 0; to < vertices; to++) {
                    reachable[from][to] |= reachable[from][via] && reachable[via][to];
                }
            }
        }
        for (int from = 0; from < vertices; from++) {
            for (int to = 0; to < vertices; to++) {
                boolean mutuallyReachable = reachable[from][to] && reachable[to][from];
                require((membership[from] == membership[to]) == mutuallyReachable,
                        "SCC matches mutual reachability");
            }
        }
        require(result.metrics().vertexVisits() == 2L * vertices,
                "Kosaraju visits every vertex in both passes");
        require(result.metrics().edgeExaminations() == 3L * graph.edgeCount(),
                "Kosaraju examines each edge in three phases");
    }

    private static void shortestPaths() {
        WeightedGraph routes = new WeightedGraph(7, true);
        routes.addEdge(0, 1, 7);
        routes.addEdge(0, 2, 9);
        routes.addEdge(0, 5, 14);
        routes.addEdge(1, 2, 10);
        routes.addEdge(1, 3, 15);
        routes.addEdge(2, 3, 11);
        routes.addEdge(2, 5, 2);
        routes.addEdge(3, 4, 6);
        routes.addEdge(5, 4, 9);
        ShortestPathResult known = Dijkstra.run(routes, 0);
        require(known.distanceTo(0).orElseThrow() == 0, "Source distance is zero");
        require(known.distanceTo(1).orElseThrow() == 7, "Distance to vertex 1");
        require(known.distanceTo(2).orElseThrow() == 9, "Distance to vertex 2");
        require(known.distanceTo(3).orElseThrow() == 20, "Distance to vertex 3");
        require(known.distanceTo(4).orElseThrow() == 20, "Distance to vertex 4");
        require(known.distanceTo(5).orElseThrow() == 11, "Distance to vertex 5");
        require(known.pathTo(4).equals(List.of(0, 2, 5, 4)), "Shortest route is reconstructed");
        require(known.pathTo(0).equals(List.of(0)), "Source path contains only source");
        require(known.distanceTo(6).isEmpty(), "Unreachable distance is absent");
        require(known.pathTo(6).isEmpty(), "Unreachable path is empty");
        require(known.metrics().queueOffers() == known.metrics().queuePolls(),
                "Dijkstra drains its priority queue");
        expectThrows(IndexOutOfBoundsException.class, () -> known.pathTo(7));
        expectThrows(IndexOutOfBoundsException.class, () -> Dijkstra.run(routes, -1));

        WeightedGraph roads = new WeightedGraph(3, false);
        roads.addEdge(0, 1, 4);
        roads.addEdge(1, 2, 1);
        roads.addEdge(0, 2, 10);
        require(Dijkstra.run(roads, 2).distanceTo(0).orElseThrow() == 5,
                "Dijkstra handles undirected roads");

        WeightedGraph single = new WeightedGraph(1, true);
        require(Dijkstra.run(single, 0).pathTo(0).equals(List.of(0)),
                "Single-vertex path is valid");

        WeightedGraph negative = new WeightedGraph(3, true);
        negative.addEdge(1, 2, -1);
        expectThrows(IllegalArgumentException.class, () -> Dijkstra.run(negative, 0));

        WeightedGraph maximum = new WeightedGraph(2, true);
        maximum.addEdge(0, 1, Long.MAX_VALUE);
        require(Dijkstra.run(maximum, 0).distanceTo(1).orElseThrow() == Long.MAX_VALUE,
                "Maximum long distance remains reachable");

        WeightedGraph overflow = new WeightedGraph(3, true);
        overflow.addEdge(0, 1, Long.MAX_VALUE);
        overflow.addEdge(1, 2, 1);
        expectThrows(ArithmeticException.class, () -> Dijkstra.run(overflow, 0));

        Random random = new Random(20261023L);
        for (int trial = 0; trial < 100; trial++) {
            int vertices = 1 + random.nextInt(9);
            WeightedGraph generated = new WeightedGraph(vertices, trial % 2 == 0);
            for (int edge = 0; edge < 2 * vertices; edge++) {
                generated.addEdge(random.nextInt(vertices), random.nextInt(vertices),
                        random.nextInt(21));
            }
            for (int source = 0; source < vertices; source++) {
                checkAgainstBellmanFord(generated, source);
            }
        }
    }

    private static void checkAgainstBellmanFord(WeightedGraph graph, int source) {
        long[] expected = new long[graph.vertexCount()];
        boolean[] reached = new boolean[graph.vertexCount()];
        reached[source] = true;
        for (int pass = 1; pass < graph.vertexCount(); pass++) {
            boolean changed = false;
            for (WeightedEdge edge : graph.edges()) {
                changed |= relaxReference(edge.from(), edge.to(), edge.weight(), expected, reached);
                if (!graph.isDirected()) {
                    changed |= relaxReference(edge.to(), edge.from(), edge.weight(), expected, reached);
                }
            }
            if (!changed) {
                break;
            }
        }

        ShortestPathResult actual = Dijkstra.run(graph, source);
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            require(actual.isReachable(vertex) == reached[vertex], "Reachability matches Bellman-Ford");
            if (!reached[vertex]) {
                require(actual.distanceTo(vertex).isEmpty(), "Unreachable distance is absent");
                require(actual.pathTo(vertex).isEmpty(), "Unreachable route is empty");
                continue;
            }
            require(actual.distanceTo(vertex).orElseThrow() == expected[vertex],
                    "Distance matches Bellman-Ford");
            List<Integer> path = actual.pathTo(vertex);
            require(path.get(0) == source && path.get(path.size() - 1) == vertex,
                    "Route has correct endpoints");
            require(path.size() <= graph.vertexCount(), "Route has no repeated vertex");
            for (int index = 1; index < path.size(); index++) {
                int from = path.get(index - 1);
                int to = path.get(index);
                long difference = actual.distanceTo(to).orElseThrow()
                        - actual.distanceTo(from).orElseThrow();
                boolean edgeFound = graph.neighbors(from).stream().anyMatch(arc ->
                        arc.to() == to && arc.weight() == difference);
                require(edgeFound, "Route follows a matching weighted edge");
            }
        }
        require(actual.metrics().queueOffers() == actual.metrics().queuePolls(),
                "Dijkstra drains its priority queue");
    }

    private static boolean relaxReference(int from, int to, long weight,
            long[] distance, boolean[] reached) {
        if (!reached[from]) {
            return false;
        }
        long candidate = distance[from] + weight;
        if (reached[to] && candidate >= distance[to]) {
            return false;
        }
        reached[to] = true;
        distance[to] = candidate;
        return true;
    }

    private static void generatedExperimentGraphs() {
        WeightedGraph roads = ExperimentGraphs.connectedUndirected(10, 30, 42);
        WeightedGraph repeatedRoads = ExperimentGraphs.connectedUndirected(10, 30, 42);
        require(roads.edgeCount() == 30, "Undirected generator makes the requested edge count");
        require(roads.edges().equals(repeatedRoads.edges()), "Undirected generation is reproducible");
        require(Kruskal.run(roads).connected(), "Generated road graph is connected");

        WeightedGraph dag = ExperimentGraphs.directed(10, 30, 42, false);
        WeightedGraph repeatedDag = ExperimentGraphs.directed(10, 30, 42, false);
        require(dag.edgeCount() == 30, "DAG generator makes the requested edge count");
        require(dag.edges().equals(repeatedDag.edges()), "DAG generation is reproducible");
        checkTopologicalOrder(dag);
        require(Kosaraju.run(dag).components().size() == 10, "Generated DAG has singleton SCCs");

        WeightedGraph cyclic = ExperimentGraphs.directed(10, 30, 42, true);
        WeightedGraph repeatedCyclic = ExperimentGraphs.directed(10, 30, 42, true);
        require(cyclic.edgeCount() == 30, "Cyclic generator makes the requested edge count");
        require(cyclic.edges().equals(repeatedCyclic.edges()), "Cyclic generation is reproducible");
        require(TopologicalSort.run(cyclic).hasCycle(), "Generated back edge creates a cycle");
        require(Kosaraju.run(cyclic).components().size() == 1,
                "Generated cyclic graph is strongly connected");
        require(Dijkstra.run(cyclic, 0).isReachable(9),
                "Generated chain reaches the last vertex");

        expectThrows(IllegalArgumentException.class,
                () -> ExperimentGraphs.connectedUndirected(10, 46, 42));
        expectThrows(IllegalArgumentException.class,
                () -> ExperimentGraphs.directed(3, 2, 42, true));
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
