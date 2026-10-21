# Assignment 3: Smart City Infrastructure and Route Planning

## Project overview

This Java project represents city districts as vertices and roads or service dependencies as weighted edges. The graph uses adjacency lists. Undirected roads are available in both directions, while directed connections have one outgoing arc. Parallel edges and self-loops are preserved. Each logical edge appears once in the edge list.

The first development step implements the graph representation and a disjoint-set union (DSU). The DSU will support Kruskal's minimum spanning tree algorithm. Later commits will add the remaining algorithms, experiments, plots, and analysis required by the assignment.

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

The test command prints `PASS: 18 checks`. The demonstration prints the district, road, and connected-component counts for a four-district graph.

## Implementation

`WeightedGraph` stores each input edge once and keeps an adjacency list for every vertex. Its edge and neighbor views are read-only. Weights use `long`, and the graph accepts negative weights because minimum spanning tree algorithms can use them. Dijkstra's algorithm will require non-negative weights when it is added.

`DisjointSetUnion` starts with one component per vertex. `find` compresses paths, and `union` attaches the smaller component to the larger one. A repeated union returns `false`; a successful union reduces the component count.

## Complexity analysis

For `V` vertices and `E` logical edges, the graph uses `O(V + E)` space and adds an edge in `O(1)` amortized time. A DSU stores `O(V)` data. With path compression and union by size, a sequence of `m` DSU operations takes `O(m α(V))` time, where `α` is the inverse Ackermann function.

## Testing

The correctness suite checks directed and undirected adjacency, parallel edges, self-loops, a single-vertex graph, invalid vertex indices, read-only edge access, DSU merging, repeated unions, component sizes, and component counts.

## AI acknowledgement

AI assisted with the initial code and documentation. Benchmark numbers and conclusions will be based on executed experiments in later commits.
