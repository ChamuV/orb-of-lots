# Design Decisions

This document explains the key conceptual decisions made during the development of the exploration strategy — why certain algorithmic directions were pursued, how the frontier family evolved through successive refinements, and what engineering choices shaped the final architecture.

This is not a results document. Benchmark data is in [`benchmarking.md`](benchmarking.md). Per-algorithm findings are in [`algorithm-families.md`](algorithm-families.md). The final algorithm is described in [`final-selection.md`](final-selection.md).

---

## The Core Design Challenge

The exploration problem is an instance of **online search**: the agent must make movement decisions without prior knowledge of the graph it is navigating. This is fundamentally different from the classical search problems that most well-known algorithms are designed for.

In offline search, the full graph G = (V, E) is available before the first move. The agent can plan globally, compute shortest paths, and reason about the entire search space. In online search, none of this is available. The agent sees only its current location, its immediate neighbours, and a heuristic estimate of distance to the goal. Every decision must be made under incomplete information, and the graph is revealed only as the agent physically moves through it.

This constraint immediately rules out a large class of algorithms — not because they are slow, but because they are structurally incompatible with the problem. Three design principles followed from this:

**1. Any algorithm must work incrementally.** It cannot require the full graph. It must build a model of the cavern as it goes and make decisions using only what has been discovered so far.

**2. Physical movement cost matters.** In an offline setting, the cost of a search is typically measured in node expansions. Here, the cost is the number of physical moves made in the cavern. An algorithm that selects sensible targets but requires extensive backtracking to reach them will perform poorly even if its search logic is sound. Travel cost must be part of the decision.

**3. The heuristic is an estimate, not a guarantee.** The game API provides a distance estimate to the Orb, but this is not a true graph distance. It reflects geometric proximity, not structural proximity. Algorithms that treat it as exact will be misled on graphs where the Orb is geometrically close but structurally distant.

These three principles shaped the entire algorithm investigation.

---

## Why Each Family Was Investigated

### Baselines — establishing the lower bound

Before investigating anything sophisticated, it was necessary to establish what naive strategies produce. Random Walk and DFS provide this: they make no use of the heuristic and no global reasoning, so their performance represents the minimum bar any informed strategy must clear.

Greedy DFS was included as a transitional case — the simplest possible use of the heuristic. Its improvement over plain DFS (80.97 vs 177.93 mean moves) quantified the value of incorporating even a local heuristic signal, and its high variance (CV 1.155) identified the key weakness of purely local heuristic strategies: they have no recovery mechanism when the heuristic is misleading.

BFS was included as a principled blind baseline — systematic rather than random, but without heuristic guidance. Its implementation revealed an important subtlety of the online setting, discussed below.

### Heuristic online search — trying established algorithms

RTA* and IDA* are both established algorithms designed for online or memory-constrained settings. They were investigated because they represent the state of existing work in this space and provide a natural comparison point.

Both performed poorly. RTA* (CV 1.378) oscillates in local minima when the heuristic is misleading. IDA* (mean 3,019,573 moves) catastrophically re-explores the known graph on each threshold iteration, a behaviour that is bounded in offline settings but unbounded here. The conclusion was that adapting algorithms designed for offline or memory-constrained settings to the online cavern problem introduces more problems than it solves. A purpose-built approach was needed.

### Frontier utility search — the purpose-built direction

The frontier family was developed in response to the failures of both local heuristic search and adapted offline algorithms. The key insight motivating it: rather than deciding where to go next based on local information at the current node, maintain a global view of all discovered-but-unvisited nodes and reason over all of them simultaneously.

This family is described in detail in the next section.

---

## The Evolution of the Frontier Family

The frontier family was developed incrementally, with each variant addressing a specific weakness identified in the previous one.

### Step 1: The base insight — score the frontier globally

`FrontierUtilitySearch` introduced the core idea. At each decision point, rather than choosing the best immediate neighbour, the agent considers every node in the frontier — the set of all nodes that have been observed but not yet visited — and selects the one with the best score under a utility function:

```
score(n) = h(n) / d(p, n)
```

where h(n) is the heuristic distance to the Orb and d(p, n) is the BFS travel distance from the current position p. This single change — from local to global decision-making — produced a mean of 57.98 moves, compared to 80.97 for Greedy DFS. The improvement came from two sources: the algorithm no longer committed to a locally attractive path that might be globally poor, and travel cost was explicitly factored into the decision rather than ignored.

### Step 2: Replanning — responding to new information

`ReplanningFrontierUtilitySearch` kept the same scoring function but added a mid-path replanning trigger. As the agent navigates toward its chosen target, new nodes are continuously discovered and added to the frontier. If a newly discovered node scores better than the current target, the agent abandons its path and replans.

This improved mean slightly (56.83 vs 57.98) and reduced CV (0.817 vs 0.861). However, the improvement was smaller than expected — in most cases, the target chosen at the start of a path remains the best choice throughout. The replanning overhead (564 µs vs 421 µs) is real, and the benefit is marginal. This suggested that the scoring function itself was the more productive direction for improvement, not the replanning mechanism.

### Step 3: Gradient penalty — tuning the travel cost term

`GradientFrontierUtilitySearch` replaced the linear travel cost denominator with an exponentiated penalty:

```
score(n) = h(n) / d(p, n)^λ
```

Higher λ penalises distant frontier nodes more aggressively, biasing the agent toward nearby unexplored locations. A parameter sweep over λ ∈ {0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 3.0, 5.0, 10.0} found that λ=1.0 and λ=1.5 produced the best results (means of 55.62 and 55.84). The ratio formulation has a structural limitation though: as d(p, n) grows, the penalty becomes multiplicative and difficult to interpret. Small changes in travel cost produce large changes in score, making the function sensitive to graph topology in ways that are hard to reason about.

### Step 4: Additive formulation — interpretability and the coverage bonus

`CoverageBiasedFrontierUtilitySearch` made two changes simultaneously. First, it switched from a ratio to an additive scoring function:

```
score(n) = h(n) + d(p, n) - μ · δ(n)
```

The additive form is more interpretable: each term contributes independently and the weight of each term relative to the others does not depend on the magnitude of the others. Second, it introduced the coverage bonus term μ · δ(n), where δ(n) is the number of n's known neighbours that are also in the frontier.

The coverage bonus addresses the remaining weakness of pure utility search: a tendency to exploit locally convenient frontier nodes while neglecting less accessible regions where the Orb might be. By rewarding frontier nodes that sit on the boundary of unexplored clusters, the algorithm biases exploration toward potentially information-rich regions.

The full mathematical development of this scoring function, including the formal definitions and term-by-term analysis, is in [`final-selection.md`](final-selection.md).

---

## Key Engineering Decisions

### Separating physical visit from search expansion (BFS bug)

The BFS implementation initially conflated two distinct concepts into a single `visited` set: nodes that had been physically visited by the agent (needed to build the graph model) and nodes that had been expanded by the BFS search order (needed to manage the frontier). In an offline implementation this conflation is often harmless. In the online setting it caused immediate total failure: the start node was physically visited before the BFS loop began, so it was immediately treated as BFS-expanded and skipped, causing the algorithm to return null on every run.

The fix — separating `physicallyVisited` and `bfsExpanded` into two distinct sets with clearly named responsibilities — resolved the issue. This incident reinforced a general principle: in online search, the agent's physical state and the algorithm's logical state must be kept conceptually and implementationally separate.

### Single BFS pass for travel costs

An early version of `BaseFrontierSearch` computed travel cost for each frontier candidate by running a separate BFS per candidate — O(F × N) per decision step. This was refactored to a single BFS from the current position that populates a distance map for the entire known graph in one O(N) pass, with all frontier candidates reading from the shared map. On graphs with large frontiers this is a substantial reduction. The distance map is passed as a parameter to `score()`, making the computation explicit and testable.

### Template method pattern for the scoring function

`BaseFrontierSearch` implements the complete exploration loop — model updating, frontier management, target selection, navigation, and replanning hook — as a template method. Subclasses implement only `score()` and optionally override `shouldReplan()`. This design keeps each subclass small and focused: `CoverageBiasedFrontierUtilitySearch` is 40 lines including comments. It also means that improvements to shared infrastructure (such as the BFS optimisation above) automatically benefit all variants without requiring changes to each subclass.

### Protected accessors over inheritance of state

Rather than making `frontier`, `visited`, and `knownGraph` protected fields that subclasses could modify directly, `BaseFrontierSearch` exposes read-only accessors (`frontier()`, `visited()`, `knownNeighbours(node)`). Subclasses can inspect shared state but cannot corrupt it. This was important given that several variants were developed in parallel — a subclass that accidentally modified the frontier set would have produced subtle, hard-to-diagnose bugs in the main exploration loop.

### Parameter sweep before selection

All parameterised variants (GradientFrontierUtility λ values, CoverageBiased μ values) were swept across a fixed range before any cross-algorithm comparison was made. The selection criteria — best mean, then best worst-case, then CV — were defined before examining results. This prevented the selection from being influenced by knowing which algorithm won, which would have made the justification circular.