# Benchmarking

This document describes the benchmarking methodology used to evaluate candidate exploration algorithms and justifies the final algorithm selection on empirical grounds.

The algorithms considered are described in [`algorithm-families.md`](algorithm-families.md). This document does not repeat those descriptions — it focuses on the experimental process, the results, and the reasoning behind the final choice.

---

## Motivation

Algorithm selection was not based on theoretical analysis alone. Several of the candidate algorithms are close variants of one another — particularly within the frontier family — and their relative performance depends on graph topology in ways that are difficult to predict analytically. A controlled empirical benchmark was therefore essential to make a defensible selection.

The benchmark was designed before results were examined, with selection criteria defined upfront to avoid post-hoc rationalisation.

---

## Methodology

### Infrastructure

A bulk benchmarking framework was implemented in `student.benchmark.BulkBenchmarkMain`. All algorithms are executed within a single JVM invocation to eliminate between-run warm-up variance. Algorithms are dispatched via JVM system properties (`benchmark.algorithm`, `benchmark.seed`), a design required by the constraint that `GameState` constructs the `Explorer` internally and cannot be modified.

Per-run results (move count, runtime) are written to per-algorithm CSV files via `BenchmarkSession` → `BenchmarkResult` → `CsvRunWriter`. Summary statistics are computed by `BenchmarkAnalysisRunner` across the full result set.

### Seed Set

Each algorithm was evaluated on 500 fixed random seeds. The same seed set was used across all algorithms, ensuring that every comparison is made on identical cavern instances. Seeds were generated once and persisted before any algorithm was benchmarked.

### Metrics Recorded

For each algorithm across all 500 seeds, the following were recorded:

- **Success rate** — whether the Orb was located within the time limit
- **Move count** — number of physical moves made during exploration (primary metric)
- **Runtime** — wall-clock time per run in microseconds

From these, the following summary statistics were derived:

- Mean and median move count
- Standard deviation and coefficient of variation (CV)
- 90th and 95th percentile move counts (P90, P95)
- Best and worst case move counts

### Selection Criteria

The following criteria were established before examining results, in priority order:

1. **Mean move count** — lower is better; primary efficiency metric
2. **Worst-case move count** — robustness; penalises algorithms that occasionally fail catastrophically
3. **Coefficient of variation** — consistency; lower CV means more predictable behaviour across seed types
4. **Runtime** — must complete reliably within the 10-second limit; a secondary consideration given all frontier algorithms completed in under 1ms on average

Win count (number of seeds on which an algorithm achieved the lowest move count) was recorded but explicitly excluded as a primary selection criterion, for reasons discussed below.

---

## Results

### Summary Table

| Algorithm | Runs | Success | Mean | Median | Std | CV | P90 | P95 | Best | Worst | Time (µs) |
|---|---|---|---|---|---|---|---|---|---|---|---|
| GradientFrontierUtility (λ=1.0) | 500 | 100% | 55.62 | 42.5 | 43.11 | 0.775 | 102.1 | 127.0 | 10 | 397 | 335 |
| GradientFrontierUtility (λ=1.5) | 500 | 100% | 55.84 | 42.0 | 41.13 | 0.737 | 105.1 | 133.0 | 10 | 322 | 355 |
| **CoverageBiasedFrontier (μ=1.0)** | **500** | **100%** | **55.86** | **42.0** | **41.97** | **0.751** | **106.2** | **138.0** | **10** | **274** | **292** |
| CoverageBiasedFrontier (μ=4.5) | 500 | 100% | 55.95 | 44.0 | 42.33 | 0.756 | 101.1 | 130.0 | 10 | 356 | 299 |
| GradientFrontierUtility (λ=2.0) | 500 | 100% | 56.30 | 43.0 | 43.11 | 0.766 | 101.2 | 135.0 | 10 | 324 | 334 |
| ReplanningFrontierUtility | 500 | 100% | 56.83 | 43.0 | 46.45 | 0.817 | 108.3 | 138.3 | 10 | 391 | 565 |
| FrontierUtilitySearch | 500 | 100% | 57.98 | 43.0 | 49.95 | 0.861 | 107.3 | 149.1 | 10 | 533 | 421 |
| AdaptiveHeuristicSearch | 500 | 100% | 80.54 | 51.0 | 93.26 | 1.158 | 170.0 | 232.1 | 10 | 780 | 99 |
| GreedyDFS | 500 | 100% | 80.97 | 50.5 | 93.55 | 1.155 | 174.1 | 241.1 | 10 | 882 | 32 |
| RealTimeAStarSearch | 500 | 100% | 121.13 | 65.5 | 166.88 | 1.378 | 250.5 | 395.5 | 10 | 1556 | 47 |
| RandomWalkSearch | 500 | 100% | 170.82 | 132.5 | 133.60 | 0.782 | 356.0 | 459.6 | 10 | 728 | 171 |
| DFS | 500 | 100% | 177.93 | 137.0 | 142.05 | 0.798 | 383.5 | 482.0 | 10 | 826 | 132 |
| BreadthFirstSearch | 500 | — | — | — | — | — | — | — | — | — | — |
| IDA* | 500 | 100% | 3,019,573 | 1,194.5 | 32,178,224 | 10.657 | 111,482 | 650,007 | 10 | 477,476,149 | 2,087,774 |

The selected algorithm is highlighted in bold. Only a representative subset of frontier variants is shown; full results for all parameterisations are available in `benchmark-data/`.

### A Note on BreadthFirstSearch

BFS is excluded from the analysis. During benchmarking it recorded 0% success across all 500 seeds, which was traced to an implementation defect specific to the online setting: the physically-visited set and the BFS-expansion set were conflated, causing the algorithm to terminate at the start node on every run. A corrected implementation separating the two sets has been produced. Results will be added once re-benchmarking is complete. The defect and its fix are documented in [`algorithm-families.md`](algorithm-families.md).

---

## Analysis

### Frontier Algorithms Dominate

The clearest finding is the separation between the frontier family and everything else. All frontier variants achieved a mean below 63 moves. The next best non-frontier algorithm is AdaptiveHeuristicSearch at 80.54. The gap is not marginal — it reflects a structural advantage: frontier utility search reasons globally over all discovered-but-unvisited nodes, whereas greedy and heuristic algorithms make purely local decisions at each step.

### Why the Top Four Are Close

The four best-performing algorithms (GradientFrontier λ=1.0, λ=1.5, CoverageBiased μ=1.0, μ=4.5) differ in mean by less than 0.4 moves across 500 seeds. This is not a statistically meaningful difference in mean. The selection therefore had to turn on secondary criteria — worst-case and CV — where the differences are more pronounced.

### The Win Count Anomaly

AdaptiveHeuristicSearch achieved 140 individual seed wins — more than double any other algorithm — despite a mean of 80.54, far worse than the frontier family. This is not a contradiction. On seeds where the heuristic is well-aligned with the true graph topology, AdaptiveHeuristicSearch reaches the Orb very quickly via aggressive heuristic-guided moves. On seeds where the heuristic is misleading, it performs poorly, producing the CV of 1.158 and worst case of 780. Win count measures only the number of first-place finishes; it does not account for consistency, variance, or tail behaviour. An algorithm that wins 140 races but performs badly on the remaining 360 is not a good general-purpose explorer. This is why win count was excluded as a selection criterion.

---

## Final Selection: CoverageBiasedFrontierUtility (μ=1.0)

### The Decision

Applying the pre-declared criteria in order:

**Mean move count:** GradientFrontier λ=1.0 leads at 55.62. CoverageBiased μ=1.0 is 55.86 — a difference of 0.24 moves. This is negligible across 500 seeds and does not constitute a meaningful advantage.

**Worst-case move count:** CoverageBiased μ=1.0 achieves a worst case of 274 moves — the lowest of any algorithm in the benchmark by a substantial margin. The next best is GradientFrontier λ=1.5 at 322, and GradientFrontier λ=1.0 at 397. The 274 vs 397 comparison represents a 31% reduction in the worst observed outcome.

**CV:** GradientFrontier λ=1.5 leads at 0.737. CoverageBiased μ=1.0 is 0.751 — competitive, and substantially below the heuristic algorithms.

**Runtime:** CoverageBiased μ=1.0 is the fastest of the top four at 292 µs per run, well within the 10-second limit on all seeds.

On the primary criterion (mean), no algorithm is meaningfully better than another within the top four. The decision therefore rests on worst-case robustness, where CoverageBiased μ=1.0 has a clear and material advantage.

### Why Worst-Case Robustness Matters Here

The exploration phase score depends on how efficiently the Orb is located across all cavern instances, not just typical ones. A worst case of 274 vs 397 means that on the most difficult seeds — those where graph topology and heuristic alignment are poor — CoverageBiased μ=1.0 loses significantly fewer moves. The coverage bias term appears to help most precisely in those cases, by directing the agent toward frontier regions that are more likely to contain large unexplored areas, rather than committing greedily to the nearest heuristically-attractive node.

### What the Coverage Bias Does and Does Not Do

The coverage bias incorporates a density proxy for information gain: frontier nodes surrounded by other frontier nodes receive a bonus, on the assumption that dense frontier regions are more likely to reveal large unseen portions of the cavern.

This is an approximation, not a guarantee. The density of nearby frontier nodes is a proxy for potential information gain, not a direct measure of it. A dense frontier region might reflect a genuinely large unexplored area, or it might reflect a region of the graph with high branching factor that has already been substantially explored. The μ=1.0 parameterisation weights this bonus conservatively, which appears to avoid over-committing to density at the expense of heuristic guidance.

---

## Conclusion

Benchmarking substantially influenced the final design. Early in development, greedy and heuristic approaches appeared promising — Greedy DFS at 80.97 moves was a substantial improvement over plain DFS at 177.93. The frontier family, once developed, improved on this by another factor of roughly 1.5x in mean, and the systematic parameter sweep across λ and μ values narrowed the field to four competitive candidates within a mean range of less than 0.4 moves.

The final selection of CoverageBiased Frontier Utility Search at μ=1.0 is not claimed to be globally optimal. It is the algorithm that best satisfies the pre-declared selection criteria across 500 fixed seeds, with particular strength in worst-case robustness. That robustness is supported by a plausible mechanism — the coverage bias reducing catastrophic outcomes on difficult seeds — rather than being a statistical artefact of the seed set.

Full per-algorithm CSV results are available in `benchmark-data/`.