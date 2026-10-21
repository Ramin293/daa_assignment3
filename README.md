# Assignment 3: Smart City Infrastructure and Route Planning

## Project overview

This Java project represents city districts as vertices and roads or service dependencies as weighted edges. The graph uses adjacency lists. Undirected roads are available in both directions, while directed connections have one outgoing arc. Parallel edges and self-loops are preserved. Each logical edge appears once in the edge list.

The project currently implements a disjoint-set union (DSU), Kruskal's algorithm, and Prim's algorithm. Both MST algorithms return the selected roads, their total construction cost, and whether all districts are connected. Later commits will add directed-graph algorithms, shortest paths, experiments, plots, and the remaining analysis.

## How to run

Use Java 17 or newer. Run these commands from the `assignment3` folder:

```powershell
$classes = "target/classes"
New-Item -ItemType Directory -Force $classes | Out-Null
$sources = Get-ChildItem src/main/java -Filter *.java -Recurse | ForEach-Object FullName
javac -encoding UTF-8 -d $classes $sources
java -cp $classes daa.assignment3.Main --test
java -cp $classes daa.assignment3.Main --demo
```

The test command prints `PASS: 4565 checks`. The demonstration runs both MST algorithms on a four-district graph; each reports a total cost of 16.

## Implementation

`WeightedGraph` stores each input edge once and keeps an adjacency list for every vertex. Its edge and neighbor views are read-only. Weights use `long`, and the graph accepts negative weights because minimum spanning tree algorithms can use them. Dijkstra's algorithm will require non-negative weights when it is added.

`DisjointSetUnion` starts with one component per vertex. `find` compresses paths, and `union` attaches the smaller component to the larger one. A repeated union returns `false`; a successful union reduces the component count.

`Kruskal` sorts logical edges by weight and joins their endpoints only when the DSU says they belong to different components. `Prim` grows each component from its lowest-numbered unvisited vertex using a priority queue of candidate roads. Both ignore self-loops and work with parallel edges, duplicate weights, and negative construction costs. On a disconnected graph they return a minimum spanning forest with `connected=false` and the number of components. Summing selected weights uses checked `long` arithmetic.

Each result also records operation counts: examined edges or adjacency arcs, ordering comparisons, priority-queue offers and polls, and DSU find calls, parent traversals, and union calls. These counters will be used alongside execution time in the experiments.

## Complexity analysis

For `V` vertices and `E` logical edges, the graph uses `O(V + E)` space and adds an edge in `O(1)` amortized time. A DSU stores `O(V)` data. With path compression and union by size, a sequence of `m` DSU operations takes `O(m α(V))` time, where `α` is the inverse Ackermann function.

Kruskal sorts `E` edges in `O(E log E)` time and performs at most `E` DSU unions in `O(E α(V))` total time. Its extra space is `O(V + E)` for the sorted edge list, DSU, and result. Prim uses adjacency lists and a binary priority queue. Its lazy queue can hold `O(E)` candidate edges, so time is `O(V + E log E)` and extra space is `O(V + E)`. For a simple graph, `E = O(V²)`, making the logarithmic factor `O(log V)`. The reported edge counts differ: Kruskal examines logical edges, while Prim examines adjacency arcs, normally twice per undirected road.

## Testing

The correctness suite checks directed and undirected adjacency, parallel edges, self-loops, a single-vertex graph, invalid vertex indices, read-only edge access, and DSU behavior. MST cases cover connected and disconnected graphs, multiple valid MSTs, duplicate and negative weights, a single vertex, rejection of directed graphs, and total-weight overflow. On 100 reproducible connected graphs, the suite compares Kruskal's and Prim's weights and verifies that each result is acyclic and spans every input component.

## AI acknowledgement

AI assisted with the initial code and documentation. Benchmark numbers and conclusions will be based on executed experiments in later commits.
