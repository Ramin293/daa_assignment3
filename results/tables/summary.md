# Benchmark results

Fixed seed 42; three unmeasured warm-up runs and five measured runs per row. The mean uses `System.nanoTime()` around the algorithm call only. Graphs are generated before timing. Times depend on the machine and JVM.

## Minimum spanning tree

| V | E | Algorithm | Mean ms | Edge examinations | Comparisons | DSU finds | DSU unions | Queue offers | Queue polls | Result |
|---:|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|
| 10 | 30 | Kruskal | 0.0335 | 10 | 110 | 20 | 10 | 0 | 0 | weight=871;components=1 |
| 10 | 30 | Prim | 0.0558 | 60 | 200 | 0 | 0 | 30 | 30 | weight=871;components=1 |
| 100 | 500 | Kruskal | 0.2623 | 190 | 3852 | 380 | 190 | 0 | 0 | weight=11390;components=1 |
| 100 | 500 | Prim | 0.2678 | 1000 | 7707 | 0 | 0 | 500 | 500 | weight=11390;components=1 |
| 1000 | 5000 | Kruskal | 1.5564 | 3337 | 55151 | 6674 | 3337 | 0 | 0 | weight=113553;components=1 |
| 1000 | 5000 | Prim | 1.3573 | 10000 | 111794 | 0 | 0 | 5000 | 5000 | weight=113553;components=1 |

## Directed connectivity

| V | E | Graph | Algorithm | Mean ms | Vertex visits | Edge examinations | Outcome |
|---:|---:|---|---|---:|---:|---:|---|
| 10 | 30 | dag | Topological | 0.0157 | 10 | 60 | cycle=false |
| 10 | 30 | cyclic | Topological | 0.0045 | 0 | 30 | cycle=true |
| 10 | 30 | dag | SCC | 0.0564 | 20 | 90 | components=10 |
| 10 | 30 | cyclic | SCC | 0.0363 | 20 | 90 | components=1 |
| 100 | 500 | dag | Topological | 0.0938 | 100 | 1000 | cycle=false |
| 100 | 500 | cyclic | Topological | 0.0368 | 0 | 500 | cycle=true |
| 100 | 500 | dag | SCC | 0.2731 | 200 | 1500 | components=100 |
| 100 | 500 | cyclic | SCC | 0.2513 | 200 | 1500 | components=1 |
| 1000 | 5000 | dag | Topological | 0.3746 | 1000 | 10000 | cycle=false |
| 1000 | 5000 | cyclic | Topological | 0.0751 | 0 | 5000 | cycle=true |
| 1000 | 5000 | dag | SCC | 0.7520 | 2000 | 15000 | components=1000 |
| 1000 | 5000 | cyclic | SCC | 0.4830 | 2000 | 15000 | components=1 |

## Shortest paths

| V | E | Mean ms | Edge examinations | Relaxations | Queue offers | Queue polls | Comparisons | Result |
|---:|---:|---:|---:|---:|---:|---:|---:|---|
| 10 | 30 | 0.0268 | 30 | 11 | 12 | 12 | 38 | lastDistance=276 |
| 100 | 500 | 0.1555 | 500 | 115 | 116 | 116 | 869 | lastDistance=452 |
| 1000 | 5000 | 0.9397 | 5000 | 1145 | 1146 | 1146 | 13393 | lastDistance=680 |

Kruskal counts logical edges considered after sorting; Prim counts adjacency arcs. SCC counts each arc in the first search, reversal, and second search. Topological ordering counts the in-degree scan plus arcs from dequeued vertices. A zero in an operation column means that no such operation occurred in that run or the operation does not apply.
