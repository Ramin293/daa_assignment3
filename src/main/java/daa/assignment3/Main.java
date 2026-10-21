package daa.assignment3;

import daa.assignment3.connectivity.Kosaraju;
import daa.assignment3.connectivity.TopologicalSort;
import daa.assignment3.graph.WeightedGraph;
import daa.assignment3.mst.Kruskal;
import daa.assignment3.mst.Prim;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        String command = args.length == 0 ? "--help" : args[0];
        switch (command) {
            case "--test" -> System.out.println(CorrectnessSuite.runAll());
            case "--demo" -> demo();
            case "--help" -> printHelp();
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        }
    }

    private static void demo() {
        WeightedGraph roads = new WeightedGraph(4, false);
        roads.addEdge(0, 1, 8);
        roads.addEdge(1, 2, 3);
        roads.addEdge(2, 3, 5);
        roads.addEdge(0, 3, 20);
        var kruskal = Kruskal.run(roads);
        var prim = Prim.run(roads);
        System.out.println("Districts: " + roads.vertexCount());
        System.out.println("Roads: " + roads.edgeCount());
        System.out.println("Kruskal MST weight: " + kruskal.totalWeight()
                + " | connected=" + kruskal.connected());
        System.out.println("Prim MST weight: " + prim.totalWeight()
                + " | connected=" + prim.connected());

        WeightedGraph dependencies = new WeightedGraph(4, true);
        dependencies.addEdge(0, 1, 1);
        dependencies.addEdge(1, 2, 1);
        dependencies.addEdge(2, 3, 1);
        System.out.println("Topological order: " + TopologicalSort.run(dependencies).order());
        dependencies.addEdge(2, 1, 1);
        System.out.println("Cycle detected: " + TopologicalSort.run(dependencies).hasCycle());
        System.out.println("Strongly connected components: " + Kosaraju.run(dependencies).components());
    }

    private static void printHelp() {
        System.out.println("Usage: java -cp target/classes daa.assignment3.Main --test | --demo");
    }
}
