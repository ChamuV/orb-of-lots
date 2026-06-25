# Algorithm Families

This document describes the algorithm families investigated during development of the exploration strategy. The narrative follows the order in which families were evaluated — from naive baselines through to the frontier-based approaches that ultimately dominated the benchmark results. Each section explains why a family was investigated, what was found, and whether it was retained or eliminated.

---

## The Online Search Constraint

Before discussing individual algorithms, it is worth being precise about what makes this problem different from classical graph search.

In a standard offline search problem, the complete graph G = (V, E) is available before the first move. Algorithms like Dijkstra's shortest path or standard A* exploit this: they reason globally about the graph, maintain a priority queue over all reachable nodes, and produce an optimal path before any movement occurs.

This problem removes that assumption entirely. The cavern graph is unknown at the start. The explorer only ever sees its current location, the IDs and distances of immediate neighbours, and a heuristic estimate of distance to the Orb. The graph is revealed incrementally as the agent moves through it.

This has two important consequences. First, any algorithm that requires the full graph upfront is immediately disqualified — this rules out Dijkstra's, standard A*, and bidirectional search (which additionally requires a known goal location). Second, the cost of exploration is not just the number of edges traversed in the search tree, but the number of physical moves made in the cavern. An algorithm that selects a sensible next node to explore but requires extensive backtracking to reach it will perform poorly in practice, even if its search logic is sound.

Every algorithm described below was evaluated against this constraint.

---

## Algorithms Eliminated Before Benchmarking

Several candidate algorithms were designed and partially implemented before being eliminated on structural grounds — not because of benchmark performance, but because they were fundamentally incompatible with the online setting.

### Bidirectional Search

Bidirectional search expands from both the start and the goal simultaneously, meeting in the middle. It can reduce search time substantially on known graphs. It was considered briefly and rejected immediately: the goal location (the Orb) is unknown during exploration. Without a known target to expand backwards from, bidirectional search cannot be applied. Including it would have required inventing a target heuristic that effectively replicated the distance estimate already provided by the game API, adding complexity with no benefit.

### Standard A\*

A* requires a complete graph to reason over. Several early implementations attempted to adapt it to the online setting by treating the known subgraph as the search space and replanning on each new discovery. These were eliminated because they reduced to a variant of the frontier-based approach described later, but with more complex bookkeeping and without the explicit utility scoring that makes frontier search interpretable. The cleaner approach was to implement frontier utility search directly rather than disguise it as A*.

### WeightedFrontierUtilitySearch and FringeSearch

These were removed during a naming and correctness review. WeightedFrontierUtilitySearch was a mislabelled duplicate of an existing variant with no meaningful behavioural difference. FringeSearch had structural issues in its frontier management that caused incorrect expansion ordering. Neither was defensible as a distinct, correct algorithm and both were dropped.

---

## Family 1 — Baselines

The baseline family provides reference points. Their purpose is not to perform well but to establish a lower bound and demonstrate why more sophisticated strategies are necessary.

### Random Walk

The simplest possible strategy: at each step, move to a randomly selected unvisited neighbour, or any neighbour if all neighbours have been visited. No memory, no planning.

**What was found:** Mean 170.82 moves, CV 0.782. Surprisingly not the worst performer — its randomness means it occasionally stumbles onto the Orb quickly, which inflates the win count (4 wins across 500 seeds) and keeps the mean lower than a purely systematic bad strategy would produce. The high move count and lack of any directional bias make it unsuitable as anything other than a lower bound reference.

### Depth-First Search

DFS explores as deep as possible before backtracking. In an online setting this translates to: always move to an unvisited neighbour if one exists, otherwise backtrack along the known path to the nearest node with an unvisited neighbour.

**What was found:** Mean 177.93 moves, CV 0.798. Slightly worse than random walk on average. DFS performs well on graphs where the Orb happens to be in the direction of the initial deep traversal, and very poorly when the Orb is in a part of the graph that DFS reaches last. The 9 wins reflect those lucky seeds. The fundamental problem is that DFS ignores the distance heuristic entirely — it has no mechanism for preferring moves that bring it closer to the Orb.

### Greedy DFS

A heuristic-aware variant of DFS: when choosing which unvisited neighbour to expand next, select the one with the smallest heuristic distance to the Orb rather than an arbitrary one.

**What was found:** Mean 80.97 moves, CV 1.155, 12 wins. Immediately a substantial improvement over plain DFS — roughly halving the average move count simply by incorporating the distance heuristic into neighbour selection. However, the high CV (1.155) reveals a serious weakness: greedy selection is easily misled by the heuristic on graphs where the shortest heuristic path leads into a dead end. The algorithm has no recovery mechanism and can waste many moves backtracking out of locally attractive but globally poor branches.

### Breadth-First Search

BFS explores in order of increasing graph distance from the start, ensuring all nodes at distance d are visited before any node at distance d+1.

**What was found:** The initial implementation contained a defect specific to the online setting — the physically-visited set and the BFS-expansion set were conflated into a single structure. Because the start node is physically visited before the BFS loop begins, it was immediately treated as already expanded, causing the algorithm to terminate at the start on every run (0% success across 500 seeds). After separating the two sets, BFS was re-benchmarked. Results are documented in [`benchmarking.md`](benchmarking.md).

This bug is a useful illustration of the online constraint: the distinction between "nodes I have physically been to" and "nodes I have expanded in search order" matters in offline search too, but conflating them in an online implementation causes immediate, total failure rather than a subtle correctness issue.

---

## Family 2 — Heuristic Online Search

The heuristic family attempts to incorporate the distance estimate more systematically than Greedy DFS, using it to guide search rather than just neighbour selection.

### Real-Time A\* (RTA\*)

RTA* is a well-known online search algorithm designed for agents that must move at each step. At each node, it evaluates neighbours using f(n) = h(n) (heuristic distance to goal). It moves to the most promising neighbour, updates a local memory of the node just left, and continues. Unlike standard A*, it commits to a move at each step rather than computing a full path first.

**What was found:** Mean 121.13 moves, CV 1.378, 2 wins. Worse than Greedy DFS on average, with significantly higher variance. The CV of 1.378 is the highest of any algorithm that successfully completes all runs. RTA* suffers from local minima: when the heuristic guides the agent into a region with no good escape, it oscillates between a small set of nodes updating local costs in a loop. On graphs where this occurs the move count explodes, which drives both the mean and variance upward.

### Iterative Deepening A\* (IDA\*)

IDA* performs a depth-first search bounded by a cost threshold, iteratively increasing the threshold. It is memory-efficient and complete. Adapting it to the online setting means replanning from the current position on each iteration.

**What was found:** Mean 3,019,573 moves, CV 10.657, worst case 477,476,149 moves. IDA* is catastrophically unsuitable for this problem. The core issue is that IDA* was designed for offline search with a known goal. In the online setting, the threshold iterations have no meaningful bound — the algorithm re-explores vast portions of the known graph on each iteration, with exponential re-expansion. A CV of 10.657 means the variance is more than ten times the mean, indicating that on some seeds the algorithm enters what is effectively an infinite loop within the time budget. IDA* was retained in the benchmark as an illustration of what happens when an algorithm designed for offline search is forced into an online setting without appropriate adaptation, but it is not a serious candidate.

---

## Family 3 — Frontier Utility Search

The frontier family represents the most significant algorithmic development in this project. Rather than selecting the next node to visit based on local information alone, these algorithms maintain an explicit set of discovered-but-unvisited nodes (the frontier) and score each one using a utility function that balances heuristic distance against travel cost.

### FrontierUtilitySearch (Base)

The base implementation scores each frontier node as:

```
utility(n) = heuristic(n) / travelCost(n)
```

where `heuristic(n)` is the game's distance estimate to the Orb and `travelCost(n)` is the BFS distance across the known graph from the current position to n. The agent always moves to the frontier node with the highest utility.

A key architectural decision was the BFS strategy for computing travel costs. An early version ran a separate BFS for each candidate frontier node — O(F × N) per decision step, where F is the frontier size and N is the known graph size. This was replaced with a single O(N) BFS from the current position that computes distances to all frontier nodes simultaneously, reducing per-step cost substantially on large graphs.

**What was found:** Mean 57.98 moves, CV 0.861, 22 wins. A substantial improvement over all baseline and heuristic algorithms. The utility ratio naturally directs the agent toward nodes that are close to the Orb and cheap to reach, avoiding the heuristic-only myopia of Greedy DFS and the oscillation problems of RTA*.

### ReplanningFrontierUtilitySearch

Extends the base by adding a replanning trigger: after each physical move, the agent checks whether the current best frontier target has changed. If a better-scoring node has appeared (due to newly discovered graph structure), the agent abandons its current path and replans toward the new target.

**What was found:** Mean 56.83 moves, CV 0.817, 6 wins. A marginal improvement in mean over the base, with slightly lower CV — the replanning mechanism prevents the agent from committing to a suboptimal target after new information arrives. However, the benefit is smaller than expected, which suggests that in most cases the initial frontier selection remains valid for the duration of the path to it. The higher runtime (564.9 μs vs 421.1 μs for the base) reflects the overhead of re-evaluating the frontier on every step.

### GradientFrontierUtilitySearch (λ variants)

Modifies the utility function to penalise travel cost more aggressively using a tunable exponent λ:

```
utility(n) = heuristic(n) / travelCost(n)^λ
```

Higher λ strongly penalises distant frontier nodes, biasing the agent toward nearby unexplored locations even if they are less heuristically promising.

**What was found across λ values:**

| λ | Mean | CV | Worst |
|---|---|---|---|
| 0.5 | 62.92 | 0.893 | 602 |
| 0.75 | 60.60 | 0.852 | 469 |
| 1.0 | 55.62 | 0.775 | 397 |
| 1.25 | 57.61 | 0.776 | 360 |
| 1.5 | 55.84 | 0.737 | 322 |
| 2.0 | 56.30 | 0.766 | 324 |
| 3.0 | 57.12 | 0.873 | 507 |
| 5.0 | 58.60 | 0.828 | 380 |
| 10.0 | 61.95 | 0.950 | 500 |

λ = 1.0 and λ = 1.5 produce the best mean scores (55.62 and 55.84 respectively). Below λ = 1.0 the algorithm underweights travel cost and makes too many expensive long-range moves. Above λ = 2.0 it overweights proximity and begins to behave like a local greedy search, missing heuristically attractive nodes that are moderately far away. The sweet spot is a narrow band around λ = 1.0–1.5.

### CoverageBiasedFrontierSearch (μ variants)

Adds a coverage bias term to the utility function: frontier nodes in less-explored regions of the graph receive a bonus, controlled by parameter μ. The intent is to prevent the agent from clustering its exploration around a locally dense region while leaving large unexplored areas unvisited.

**What was found across μ values:**

| μ | Mean | CV | Worst |
|---|---|---|---|
| 0.5 | 57.36 | 0.735 | 364 |
| 1.0 | 55.86 | 0.751 | 274 |
| 1.5 | 57.34 | 0.747 | 304 |
| 2.0 | 57.08 | 0.769 | 378 |
| 3.0 | 57.52 | 0.845 | 561 |
| 4.0 | 56.86 | 0.837 | 446 |
| 4.5 | 55.95 | 0.756 | 356 |
| 5.0 | 57.72 | 0.799 | 479 |
| 5.5 | 57.62 | 0.791 | 509 |
| 6.0 | 56.98 | 0.743 | 429 |
| 10.0 | 57.76 | 0.763 | 475 |

μ = 1.0 achieves the best mean (55.86) and notably the lowest worst-case across all algorithms tested (274 moves). The coverage bias appears to help most in reducing catastrophic cases — seeds where the Orb is in a sparsely explored region that a pure utility search would reach last. However, the benefit over GradientFrontierUtility at λ = 1.0 is marginal on mean, and the parameter is less interpretable: the coverage bonus depends on local neighbourhood density, which is a proxy for unexplored area rather than a direct measure of it.

---

## Summary

The table below compares all algorithms across the primary evaluation metrics, ordered by mean move count.

| Algorithm | Mean | Median | CV | Worst | Wins |
|---|---|---|---|---|---|
| GradientFrontierUtility (λ=1.0) | 55.62 | 42.5 | 0.775 | 397 | 8 |
| GradientFrontierUtility (λ=1.5) | 55.84 | 42.0 | 0.737 | 322 | 3 |
| CoverageBiasedFrontier (μ=1.0) | 55.86 | 42.0 | 0.751 | 274 | 50 |
| CoverageBiasedFrontier (μ=4.5) | 55.95 | 44.0 | 0.756 | 356 | 17 |
| GradientFrontierUtility (λ=2.0) | 56.30 | 43.0 | 0.766 | 324 | 1 |
| ReplanningFrontierUtility | 56.83 | 43.0 | 0.817 | 391 | 6 |
| FrontierUtilitySearch | 57.98 | 43.0 | 0.861 | 533 | 22 |
| AdaptiveHeuristicSearch | 80.54 | 51.0 | 1.158 | 780 | 140 |
| GreedyDFS | 80.97 | 50.5 | 1.155 | 882 | 12 |
| RealTimeAStarSearch | 121.13 | 65.5 | 1.378 | 1556 | 2 |
| RandomWalkSearch | 170.82 | 132.5 | 0.782 | 728 | 4 |
| DFS | 177.93 | 137.0 | 0.798 | 826 | 9 |
| BreadthFirstSearch | — | — | — | — | — |
| IDA* | 3,019,573 | 1,194.5 | 10.657 | 477,476,149 | 0 |

**Notable observation — AdaptiveHeuristicSearch win count:** AdaptiveHeuristicSearch won 140 out of 500 seeds — more than double any other single algorithm — despite having a mean of 80.54, far worse than the frontier family. This is not a contradiction. AdaptiveHeuristicSearch is highly aggressive on seeds where the heuristic is well-aligned with the true graph topology; on those seeds it finds the Orb extremely quickly. On seeds where the heuristic is misleading it performs poorly, which the CV of 1.158 reflects. The frontier algorithms win on mean by being more robust across all seed types, not by being fastest on any individual seed.

Final algorithm selection and rationale are documented in [`final-selection.md`](final-selection.md).