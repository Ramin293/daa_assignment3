package daa.assignment3.connectivity;

import daa.assignment3.graph.WeightedEdge;
import daa.assignment3.graph.WeightedGraph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Kosaraju {
    private Kosaraju() {}

    private static final class Frame {
        final int vertex;
        int nextArc;

        Frame(int vertex) {
            this.vertex = vertex;
        }
    }

    public static SccResult run(WeightedGraph graph) {
        Objects.requireNonNull(graph, "graph");
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC requires a directed graph");
        }

        boolean[] visited = new boolean[graph.vertexCount()];
        List<Integer> finishOrder = new ArrayList<>(graph.vertexCount());
        ArrayDeque<Frame> depthFirst = new ArrayDeque<>();
        long vertexVisits = 0;
        long examined = 0;

        for (int start = 0; start < graph.vertexCount(); start++) {
            if (visited[start]) {
                continue;
            }
            visited[start] = true;
            vertexVisits++;
            depthFirst.push(new Frame(start));
            while (!depthFirst.isEmpty()) {
                Frame frame = depthFirst.peek();
                var arcs = graph.neighbors(frame.vertex);
                if (frame.nextArc == arcs.size()) {
                    finishOrder.add(frame.vertex);
                    depthFirst.pop();
                    continue;
                }
                int next = arcs.get(frame.nextArc++).to();
                examined++;
                if (!visited[next]) {
                    visited[next] = true;
                    vertexVisits++;
                    depthFirst.push(new Frame(next));
                }
            }
        }

        List<List<Integer>> reversed = new ArrayList<>(graph.vertexCount());
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            reversed.add(new ArrayList<>());
        }
        for (WeightedEdge edge : graph.edges()) {
            reversed.get(edge.to()).add(edge.from());
            examined++;
        }

        boolean[] assigned = new boolean[graph.vertexCount()];
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        List<List<Integer>> components = new ArrayList<>();
        for (int index = finishOrder.size() - 1; index >= 0; index--) {
            int start = finishOrder.get(index);
            if (assigned[start]) {
                continue;
            }
            List<Integer> component = new ArrayList<>();
            assigned[start] = true;
            stack.push(start);
            while (!stack.isEmpty()) {
                int vertex = stack.pop();
                vertexVisits++;
                component.add(vertex);
                for (int predecessor : reversed.get(vertex)) {
                    examined++;
                    if (!assigned[predecessor]) {
                        assigned[predecessor] = true;
                        stack.push(predecessor);
                    }
                }
            }
            components.add(component);
        }
        return new SccResult(components, new TraversalMetrics(vertexVisits, examined));
    }
}
