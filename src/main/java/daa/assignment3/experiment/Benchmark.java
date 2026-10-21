package daa.assignment3.experiment;

import daa.assignment3.connectivity.Kosaraju;
import daa.assignment3.connectivity.SccResult;
import daa.assignment3.connectivity.TopologicalResult;
import daa.assignment3.connectivity.TopologicalSort;
import daa.assignment3.graph.WeightedGraph;
import daa.assignment3.mst.Kruskal;
import daa.assignment3.mst.MstResult;
import daa.assignment3.mst.Prim;
import daa.assignment3.shortestpath.Dijkstra;
import daa.assignment3.shortestpath.ShortestPathResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Benchmark {
    private static final long SEED = 42L;
    private static final int WARMUPS = 3;
    private static final int REPETITIONS = 5;
    private static volatile int sink;

    private record Size(String name, int vertices, int edges) {}

    private record Observation(long edgeExaminations, long orderingComparisons,
            long queueOffers, long queuePolls, long dsuFindCalls,
            long dsuParentTraversals, long dsuUnionCalls, long vertexVisits,
            long successfulRelaxations, String outcome) {}

    private record Row(String algorithm, String graphType, Size size,
            double meanNanos, Observation observation) {}

    private Benchmark() {}

    public static void run(Path csvPath) throws IOException {
        Objects.requireNonNull(csvPath, "csvPath");
        List<Row> rows = new ArrayList<>();
        List<Size> sizes = List.of(
                new Size("small", 10, 30),
                new Size("medium", 100, 500),
                new Size("large", 1_000, 5_000));

        for (Size size : sizes) {
            WeightedGraph roads = ExperimentGraphs.connectedUndirected(
                    size.vertices(), size.edges(), SEED);
            WeightedGraph dag = ExperimentGraphs.directed(
                    size.vertices(), size.edges(), SEED, false);
            WeightedGraph cyclic = ExperimentGraphs.directed(
                    size.vertices(), size.edges(), SEED, true);
            verifyInputs(roads, dag, cyclic, size);

            rows.add(measure("Kruskal", "undirected", size,
                    () -> Kruskal.run(roads), Benchmark::observe));
            rows.add(measure("Prim", "undirected", size,
                    () -> Prim.run(roads), Benchmark::observe));
            rows.add(measure("Topological", "dag", size,
                    () -> TopologicalSort.run(dag), Benchmark::observe));
            rows.add(measure("Topological", "cyclic", size,
                    () -> TopologicalSort.run(cyclic), Benchmark::observe));
            rows.add(measure("SCC", "dag", size,
                    () -> Kosaraju.run(dag), Benchmark::observe));
            rows.add(measure("SCC", "cyclic", size,
                    () -> Kosaraju.run(cyclic), Benchmark::observe));
            rows.add(measure("Dijkstra", "cyclic", size,
                    () -> Dijkstra.run(cyclic, 0), Benchmark::observe));
        }

        Path parent = csvPath.toAbsolutePath().getParent();
        Path tablePath = parent.resolve("tables/summary.md");
        Files.createDirectories(parent);
        Files.createDirectories(tablePath.getParent());
        Files.writeString(csvPath, csv(rows), StandardCharsets.UTF_8);
        Files.writeString(tablePath, tables(rows), StandardCharsets.UTF_8);
        System.out.println("Wrote " + rows.size() + " benchmark rows to " + csvPath);
        System.out.println("Wrote summary tables to " + tablePath);
    }

    private static void verifyInputs(WeightedGraph roads, WeightedGraph dag,
            WeightedGraph cyclic, Size size) {
        if (roads.edgeCount() != size.edges() || dag.edgeCount() != size.edges()
                || cyclic.edgeCount() != size.edges()) {
            throw new IllegalStateException("Generated graph has the wrong number of edges");
        }
        MstResult kruskal = Kruskal.run(roads);
        MstResult prim = Prim.run(roads);
        if (!kruskal.connected() || !prim.connected()
                || kruskal.totalWeight() != prim.totalWeight()) {
            throw new IllegalStateException("MST inputs or results are inconsistent");
        }
        if (TopologicalSort.run(dag).hasCycle() || !TopologicalSort.run(cyclic).hasCycle()
                || Kosaraju.run(dag).components().size() != size.vertices()
                || Kosaraju.run(cyclic).components().size() != 1
                || !Dijkstra.run(cyclic, 0).isReachable(size.vertices() - 1)) {
            throw new IllegalStateException("Directed graph input does not match its description");
        }
    }

    private static <T> Row measure(String algorithm, String graphType, Size size,
            Supplier<T> action, Function<T, Observation> observe) {
        for (int run = 0; run < WARMUPS; run++) {
            sink ^= observe.apply(action.get()).hashCode();
        }
        long totalNanos = 0;
        Observation first = null;
        for (int run = 0; run < REPETITIONS; run++) {
            long start = System.nanoTime();
            T result = action.get();
            totalNanos += System.nanoTime() - start;
            Observation current = observe.apply(result);
            if (first != null && !first.equals(current)) {
                throw new IllegalStateException("Algorithm produced inconsistent results");
            }
            first = current;
            sink ^= current.hashCode();
        }
        return new Row(algorithm, graphType, size, totalNanos / (double) REPETITIONS, first);
    }

    private static Observation observe(MstResult result) {
        var metrics = result.metrics();
        return new Observation(metrics.edgeExaminations(), metrics.orderingComparisons(),
                metrics.queueOffers(), metrics.queuePolls(), metrics.dsuFindCalls(),
                metrics.dsuParentTraversals(), metrics.dsuUnionCalls(), 0, 0,
                "weight=" + result.totalWeight() + ";components=" + result.componentCount());
    }

    private static Observation observe(TopologicalResult result) {
        var metrics = result.metrics();
        return new Observation(metrics.edgeExaminations(), 0, 0, 0, 0, 0, 0,
                metrics.vertexVisits(), 0, "cycle=" + result.hasCycle());
    }

    private static Observation observe(SccResult result) {
        var metrics = result.metrics();
        return new Observation(metrics.edgeExaminations(), 0, 0, 0, 0, 0, 0,
                metrics.vertexVisits(), 0, "components=" + result.components().size());
    }

    private static Observation observe(ShortestPathResult result) {
        var metrics = result.metrics();
        return new Observation(metrics.edgeExaminations(), metrics.orderingComparisons(),
                metrics.queueOffers(), metrics.queuePolls(), 0, 0, 0, 0,
                metrics.successfulRelaxations(),
                "lastDistance=" + result.distanceTo(result.vertexCount() - 1).orElseThrow());
    }

    private static String csv(List<Row> rows) {
        StringBuilder text = new StringBuilder();
        text.append("algorithm,graph_type,size,vertices,edges,seed,repetitions,mean_time_ns,")
                .append("edge_examinations,ordering_comparisons,queue_offers,queue_polls,")
                .append("dsu_find_calls,dsu_parent_traversals,dsu_union_calls,vertex_visits,")
                .append("successful_relaxations,outcome\n");
        for (Row row : rows) {
            Observation o = row.observation();
            text.append(row.algorithm()).append(',').append(row.graphType()).append(',')
                    .append(row.size().name()).append(',').append(row.size().vertices()).append(',')
                    .append(row.size().edges()).append(',').append(SEED).append(',')
                    .append(REPETITIONS).append(',')
                    .append(String.format(Locale.ROOT, "%.1f", row.meanNanos())).append(',')
                    .append(o.edgeExaminations()).append(',').append(o.orderingComparisons()).append(',')
                    .append(o.queueOffers()).append(',').append(o.queuePolls()).append(',')
                    .append(o.dsuFindCalls()).append(',').append(o.dsuParentTraversals()).append(',')
                    .append(o.dsuUnionCalls()).append(',').append(o.vertexVisits()).append(',')
                    .append(o.successfulRelaxations()).append(',').append(o.outcome()).append('\n');
        }
        return text.toString();
    }

    private static String tables(List<Row> rows) {
        StringBuilder text = new StringBuilder();
        text.append("# Benchmark results\n\n")
                .append("Fixed seed 42; three unmeasured warm-up runs and five measured runs per row. ")
                .append("The mean uses `System.nanoTime()` around the algorithm call only. ")
                .append("Graphs are generated before timing. Times depend on the machine and JVM.\n\n")
                .append("## Minimum spanning tree\n\n")
                .append("| V | E | Algorithm | Mean ms | Edge examinations | Comparisons | ")
                .append("DSU finds | DSU unions | Queue offers | Queue polls | Result |\n")
                .append("|---:|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|\n");
        for (Row row : rows) {
            if (!row.algorithm().equals("Kruskal") && !row.algorithm().equals("Prim")) continue;
            Observation o = row.observation();
            text.append("| ").append(row.size().vertices()).append(" | ")
                    .append(row.size().edges()).append(" | ").append(row.algorithm()).append(" | ")
                    .append(milliseconds(row)).append(" | ").append(o.edgeExaminations())
                    .append(" | ").append(o.orderingComparisons()).append(" | ")
                    .append(o.dsuFindCalls()).append(" | ").append(o.dsuUnionCalls())
                    .append(" | ").append(o.queueOffers()).append(" | ")
                    .append(o.queuePolls()).append(" | ").append(o.outcome()).append(" |\n");
        }
        text.append("\n## Directed connectivity\n\n")
                .append("| V | E | Graph | Algorithm | Mean ms | Vertex visits | Edge examinations | Outcome |\n")
                .append("|---:|---:|---|---|---:|---:|---:|---|\n");
        for (Row row : rows) {
            if (!row.algorithm().equals("Topological") && !row.algorithm().equals("SCC")) continue;
            Observation o = row.observation();
            text.append("| ").append(row.size().vertices()).append(" | ")
                    .append(row.size().edges()).append(" | ").append(row.graphType())
                    .append(" | ").append(row.algorithm()).append(" | ")
                    .append(milliseconds(row)).append(" | ").append(o.vertexVisits())
                    .append(" | ").append(o.edgeExaminations()).append(" | ")
                    .append(o.outcome()).append(" |\n");
        }
        text.append("\n## Shortest paths\n\n")
                .append("| V | E | Mean ms | Edge examinations | Relaxations | Queue offers | ")
                .append("Queue polls | Comparisons | Result |\n")
                .append("|---:|---:|---:|---:|---:|---:|---:|---:|---|\n");
        for (Row row : rows) {
            if (!row.algorithm().equals("Dijkstra")) continue;
            Observation o = row.observation();
            text.append("| ").append(row.size().vertices()).append(" | ")
                    .append(row.size().edges()).append(" | ").append(milliseconds(row))
                    .append(" | ").append(o.edgeExaminations()).append(" | ")
                    .append(o.successfulRelaxations()).append(" | ").append(o.queueOffers())
                    .append(" | ").append(o.queuePolls()).append(" | ")
                    .append(o.orderingComparisons()).append(" | ").append(o.outcome())
                    .append(" |\n");
        }
        text.append("\nKruskal counts logical edges considered after sorting; Prim counts adjacency arcs. ")
                .append("SCC counts each arc in the first search, reversal, and second search. ")
                .append("Topological ordering counts the in-degree scan plus arcs from dequeued vertices. ")
                .append("A zero in an operation column means that no such operation occurred ")
                .append("in that run or the operation does not apply.\n");
        return text.toString();
    }

    private static String milliseconds(Row row) {
        return String.format(Locale.ROOT, "%.4f", row.meanNanos() / 1_000_000);
    }
}
