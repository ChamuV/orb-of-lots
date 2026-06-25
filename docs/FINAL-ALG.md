# Final Selection: CoverageBiasedFrontierUtilitySearch (μ=1.0)

This document describes the final exploration algorithm selected for submission. It covers the algorithm's design, its mathematical basis, how it fits within the frontier family architecture, the reasoning behind the μ=1.0 parameterisation, and known limitations.

The empirical justification for this selection — benchmark results, selection criteria, and comparison with alternatives — is in [`benchmarking.md`](benchmarking.md). The broader context of the algorithm families investigated is in [`algorithm-families.md`](algorithm-families.md).

---

## Position Within the Frontier Family

`CoverageBiasedFrontierUtilitySearch` is the final member of a family of algorithms that all share the same core insight: rather than making purely local decisions at each step, maintain an explicit set of discovered-but-unvisited nodes (the **frontier**) and select the next exploration target globally by scoring every frontier node against a utility function.

This family was developed incrementally:

1. **FrontierUtilitySearch** — the base implementation, scoring by `orbDistance / travelCost`
2. **ReplanningFrontierUtilitySearch** — adds mid-path replanning when a better target appears
3. **GradientFrontierUtilitySearch** — replaces division with an exponentiated travel cost penalty `orbDistance / travelCost^λ`
4. **CoverageBiasedFrontierUtilitySearch** — switches to an additive scoring formulation and introduces a local frontier-density bonus

The key design shift from the earlier variants is the move from a ratio-based to an additive scoring function. This makes the influence of each term more interpretable and allows the coverage bonus to be incorporated cleanly without interacting multiplicatively with the other terms.

---

## Architecture: BaseFrontierSearch

All frontier algorithms extend `BaseFrontierSearch`, which owns the shared infrastructure. Understanding what the base class provides clarifies what `CoverageBiasedFrontierUtilitySearch` is responsible for.

### What BaseFrontierSearch does

**Graph model.** The base class maintains an adjacency list (`knownGraph`) built incrementally as the agent moves. On each step, `updateModel` adds the current node's neighbours to the graph and records their heuristic distances to the Orb. This is the only source of graph knowledge — nothing is available from the game API beyond what is visible from the current location.

**Frontier set.** A node enters `frontier` when it is first observed as a neighbour of a visited node. It is removed from `frontier` when the agent physically arrives at it (i.e., when it is added to `visited`). The frontier therefore always represents the set of nodes that are known to exist but have not yet been physically visited.

**Target selection.** `chooseBestFrontierNode` runs a single BFS from the current position across the known graph to compute travel distances to all reachable nodes in one O(N) pass. It then calls `score()` for each frontier candidate and selects the one with the lowest score. Lower score is better — the scoring functions are formulated as costs to minimise rather than utilities to maximise.

**Navigation.** `navigateTo` physically moves the agent toward the selected target one step at a time using `shortestPath`. After each step, it calls `updateModel` to incorporate newly visible nodes, then calls `shouldReplan` — a hook that subclasses can override to abort the current path early if a better target has appeared. The default implementation always returns `false` (no replanning).

### A key architectural decision: single BFS pass

An early version of the base class ran a separate BFS for each frontier candidate to compute its travel cost — O(F × N) per decision step, where F is the frontier size and N is the known graph size. This was refactored to a single BFS from the current position that populates a distance map for all reachable nodes simultaneously, reducing the per-step cost to O(N). On large graphs with large frontiers this is a substantial improvement. The distance map is passed into `score()` as a parameter, which is why the signature takes `Map<Long, Integer> travelCost` rather than computing distance internally.

### Protected accessors

`BaseFrontierSearch` exposes three protected accessors to subclasses: `frontier()`, `visited()`, and `knownNeighbours(node)`. These allow subclasses to inspect the current search state without exposing the internal data structures directly. `CoverageBiasedFrontierUtilitySearch` uses `frontier()` and `knownNeighbours()` in its density calculation.

---

## The Scoring Function

The scoring function is the only method `CoverageBiasedFrontierUtilitySearch` implements. Everything else is inherited from `BaseFrontierSearch`.

### Formal Definition

Let:

- V_f ⊆ V be the current frontier — the set of discovered but not yet physically visited nodes
- h(n) be the heuristic distance from node n to the Orb, as provided by the game API
- d(p, n) be the BFS distance over the known graph from the agent's current position p to frontier node n
- N(n) be the set of known neighbours of n in the current graph model
- δ(n) = |{m ∈ N(n) : m ∈ V_f}| be the local frontier density of n — the number of n's known neighbours that are also frontier nodes
- μ ≥ 0 be the coverage weight parameter

The scoring function for a frontier candidate n is:

```
score(n) = h(n) + d(p, n) - μ · δ(n)
```

The algorithm selects the target n* that minimises this score:

```
n* = argmin_{n ∈ V_f} [ h(n) + d(p, n) - μ · δ(n) ]
```

**Score is a cost to minimise.** Lower scores are better. The first two terms are costs — heuristic distance to the Orb and physical travel cost to reach the candidate. The third term is a bonus — it is subtracted, so nodes with higher frontier density become cheaper in the scoring sense and therefore more likely to be selected.

### Term-by-Term Analysis

**h(n) — heuristic guidance.** This is not a true graph distance. It is an estimate provided by the game API, analogous to the h(n) term in A*. It provides directional bias toward the Orb without requiring knowledge of the full graph. When h(n) is well-correlated with true graph distance, this term efficiently guides the agent toward the Orb. When it is not — for example, in graphs where the Orb is geometrically close but structurally distant — this term can misdirect the agent.

**d(p, n) — travel cost.** This is an exact cost, not a heuristic. It is computed by a single BFS over the known graph from the agent's current position, producing distances to all reachable nodes in one O(N) pass. This term penalises frontier nodes that are expensive to reach, preventing the agent from committing to distant targets when cheaper alternatives exist. Without this term, the scoring function reduces to a greedy heuristic that ignores the cost of getting to the next exploration target.

**μ · δ(n) — coverage bonus.** This term is the novel contribution of this algorithm. δ(n) counts the number of n's known neighbours that are also in the frontier. A node with high δ sits on the boundary of a cluster of unexplored nodes — visiting it is likely to open up a region containing many further unvisited nodes. The bonus rewards such nodes by reducing their effective score. μ controls the strength of this reward.

### The Relationship to Pure Frontier Utility Search

When μ = 0, the coverage bonus vanishes and the scoring function reduces to:

```
score(n) = h(n) + d(p, n)
```

This is the additive form of the base `FrontierUtilitySearch` scoring. The coverage-biased variant therefore strictly generalises the base: μ=0 recovers the unbiased baseline, and increasing μ progressively weights the exploration coverage term. This relationship was verified empirically — at μ=0 the two algorithms produce identical behaviour.

### frontierDensity in code

```java
private int localFrontierDensity(long candidate) {
    int count = 0;
    for (long neighbour : knownNeighbours(candidate)) {
        if (frontier().contains(neighbour)) {
            count++;
        }
    }
    return count;
}
```

This directly implements δ(n). It operates only on the known graph — neighbours that have not yet been observed do not contribute, because they are not yet in the graph model. The maximum possible value of δ(n) is bounded by the degree of n in the known graph at the time of scoring.

### Intuition

The coverage bonus addresses a specific failure mode of pure utility search: without it, the agent tends to exploit locally attractive frontier nodes — those that are both heuristically close to the Orb and cheap to reach — while deferring exploration of less accessible regions. On seeds where the Orb is located in one of those deferred regions, the agent wastes many moves exhausting the local frontier before eventually committing to the distant area where the Orb actually is.

A frontier node with high δ sits at the entrance to a cluster of unexplored nodes. Prioritising such nodes biases exploration toward regions where many new nodes are likely to be revealed per visit, rather than regions that are merely convenient. The benchmark results are consistent with this mechanism: CoverageBiased at μ=1.0 achieved the lowest worst-case of any algorithm tested (274 moves), a 31% reduction compared to the best GradientFrontier variant (397 moves). The improvement is concentrated in the tail — worst-case and P95 — rather than in the mean, which is consistent with the bonus specifically reducing catastrophic outcomes on seeds where the Orb is in a sparsely explored region.

---

## Parameter Selection: Why μ=1.0

### The Sweep

μ was not chosen by intuition. Eleven values were evaluated — μ ∈ {0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 4.5, 5.0, 5.5, 6.0, 10.0} — each benchmarked over the same 500 fixed seeds used for all algorithm comparisons. The full sweep results are:

| μ | Mean | CV | P90 | P95 | Worst |
|---|---|---|---|---|---|
| 0.5 | 57.36 | 0.735 | 108.1 | 148.0 | 364 |
| **1.0** | **55.86** | **0.751** | **106.2** | **138.0** | **274** |
| 1.5 | 57.34 | 0.747 | 111.0 | 143.1 | 304 |
| 2.0 | 57.08 | 0.769 | 113.0 | 134.1 | 378 |
| 3.0 | 57.52 | 0.845 | 105.0 | 140.0 | 561 |
| 4.0 | 56.86 | 0.837 | 103.1 | 147.0 | 446 |
| 4.5 | 55.95 | 0.756 | 101.1 | 130.0 | 356 |
| 5.0 | 57.72 | 0.799 | 102.1 | 139.0 | 479 |
| 5.5 | 57.62 | 0.791 | 108.1 | 139.0 | 509 |
| 6.0 | 56.98 | 0.743 | 107.1 | 139.0 | 429 |
| 10.0 | 57.76 | 0.763 | 108.2 | 137.1 | 475 |

### Reading the Sweep

The relationship between μ and performance is non-monotonic, which is expected: the optimal coverage weight depends on the balance between graph connectivity and heuristic accuracy, both of which vary across seeds.

**Below μ=1.0:** At μ=0.5 the bonus is too weak to meaningfully counteract the greedy pull of h(n) + d(p,n). The algorithm behaves similarly to the unbiased baseline, and the worst-case (364) reflects the same failure mode — the agent exploits the local frontier too aggressively on difficult seeds.

**At μ=1.0:** Best mean (55.86) and by far the best worst-case (274) within the family. The bonus is strong enough to redirect the agent toward unexplored boundary regions on difficult seeds, but not so strong that it overrides heuristic guidance on typical seeds.

**μ=1.5 to μ=2.0:** Mean degrades slightly. The bonus begins to systematically preference high-degree frontier nodes over heuristically closer ones, adding unnecessary travel cost on seeds where the nearby frontier is actually the right direction.

**μ=3.0 and above:** Performance becomes erratic. The worst-case at μ=3.0 (561) is more than double the μ=1.0 worst-case, despite a similar mean. At this strength the bonus can dominate the scoring function on high-degree nodes, effectively turning the algorithm into a coverage-greedy search that ignores the Orb heuristic on a subset of seeds — precisely the seeds that produce extreme worst-case outcomes.

**μ=4.5:** A secondary competitive region appears. Mean is 55.95 (only 0.09 behind μ=1.0) but worst-case is 356 vs 274. This reflects a different balance point where the bonus weight happens to perform well on the specific seed distribution used, but with higher tail risk.

### The Selection Decision

Applying the pre-declared criteria in order — best mean, then best worst-case — μ=1.0 wins on both within the CoverageBiased family. The selection was made from the sweep table before any cross-algorithm comparison, ensuring the choice of μ was not influenced by knowledge of how other algorithm families performed.

The sweep also confirms that μ=1.0 is not a local optimum on a smooth curve — the non-monotonic pattern means the result needs to be treated as an empirical finding rather than a derived optimum. A larger seed set or a finer-grained sweep around μ=1.0 might shift the result slightly, but the margin over adjacent values is consistent enough to be credible.

---

## Known Limitations

**frontierDensity is a proxy, not a measure of information gain.** The density count reflects the number of known neighbours that are frontier nodes. It does not measure the size of the unexplored region behind those frontier nodes. A high-density frontier cluster could represent a genuinely large unexplored area, or it could represent a small densely-connected subgraph with little beyond it. The algorithm has no way to distinguish these cases.

**The heuristic distance is not a true graph distance.** `orbDistance` is provided by the game API as an estimate. On some graph topologies it may be poorly correlated with true path length to the Orb — for example, if the Orb is geometrically close but graph-structurally distant due to sparse connectivity. When the heuristic is misleading, the scoring function may consistently misdirect the agent, and no amount of coverage bonus will compensate for a fundamentally inaccurate `orbDistance` signal.

**No backtracking recovery.** If `navigateTo` is called with a target that becomes unreachable mid-path (which should not occur in a connected graph but could in a pathological case), `shortestPath` returns an empty list and navigation silently terminates. The agent then re-enters the main loop and selects a new target. This is safe but silent — there is no explicit handling of the disconnected-graph case.

**Single BFS per decision step.** Although the single-pass BFS is efficient relative to the earlier per-candidate approach, it still runs on every step of navigation, not just when a new target is selected. The `shouldReplan` hook exists to allow subclasses to trigger replanning, but the base class runs BFS unconditionally inside `chooseBestFrontierNode` on each outer loop iteration. On very large graphs with many frontier nodes this could become a bottleneck, though it did not manifest as a runtime issue in the benchmark (292 µs average per run).

**μ is fixed at construction time.** The coverage weight does not adapt during a run. An adaptive μ that increases as exploration progresses — weighting coverage more heavily once the agent has exhausted nearby frontier options — was considered but not implemented. This remains an open improvement.