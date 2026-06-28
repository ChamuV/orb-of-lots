# Testing

## Introduction

This document describes the testing strategy adopted throughout the project, with particular emphasis on the principles that guided how correctness was verified at each level of the implementation.

Rather than treating testing as a retrospective activity, the approach was to design the test suite alongside the implementation itself, ensuring that every component — from individual utility classes to end-to-end algorithm runs — could be validated in isolation. This produced a suite organised around the same separation of concerns that shaped the codebase: search algorithm tests are kept independent of benchmarking tests, and correctness of individual components is established before validating the behaviour of the full system.

At each stage, the structure of the tests reflects a deliberate choice about what is being verified. The central question throughout was therefore not "Which tests pass?", but rather "What behaviour does each test actually establish?" This distinction motivated a progression from narrow unit tests that target a single class in isolation, through targeted behaviour tests on configurable graph structures, to integration tests that validate the complete execution pipeline end to end.

## 1. Test Infrastructure

Before any algorithm could be tested meaningfully, two questions had to be resolved:

> **How can an exploration algorithm be tested without running the real game environment?**
> **How can different graph topologies be expressed concisely enough to test specific behaviours?**

Both questions pointed to the same conclusion: the test suite required its own lightweight representation of the exploration environment, decoupled entirely from the Temple of Lots framework.

### 1.1 StubExplorationState

The simplest possible exploration environment is a two-node graph: one starting node with a single neighbour at distance zero, representing the Orb. This is sufficient to verify that an algorithm terminates correctly, records the right number of moves, and reports success through the benchmark pipeline — without exercising any backtracking, frontier expansion, or graph-level decision-making.

`StubExplorationState` implements `ExplorationState` directly and encodes this two-node graph as a fixed-state object. Because it requires no configuration, it can be constructed inline within a single test, making it the default choice for verifying the basic correctness of any algorithm that implements `findOrb()`.

### 1.2 GraphExplorationState

The two-node stub is deliberately minimal, which means it cannot exercise the behaviours that distinguish one algorithm from another: backtracking through dead ends, frontier expansion across multiple hops, or neighbour ordering under competing heuristic values. Testing these behaviours required a more expressive representation.

`GraphExplorationState` models an arbitrary undirected graph, built through a fluent `Builder` API that allows nodes, edges and orb-distance estimates to be declared concisely within each test. The explorer always begins at node 0, and `moveTo()` enforces adjacency at runtime, catching any algorithm that attempts an invalid traversal. The builder requires `orbAt()` to be called before `build()`, making it impossible to construct a state without specifying where the Orb is located.

A representative construction is shown below:

```java
// Linear graph: 0 -- 1 -- 2 -- 3(orb)
GraphExplorationState state = new GraphExplorationState.Builder()
    .orbAt(3)
    .edge(0, 1).edge(1, 2).edge(2, 3)
    .distance(0, 3).distance(1, 2).distance(2, 1).distance(3, 0)
    .build();
```

This makes it straightforward to construct targeted scenarios — branching graphs, dead ends, misleading heuristic values — without duplicating graph-building logic across test classes.

## 2. Search Algorithm Tests

With the test infrastructure in place, the next question was how to structure the tests for the search algorithms themselves. Two requirements shaped the answer.

The first was isolation: the correctness of each algorithm should be verifiable without depending on the correctness of any other. The second was coverage: the behaviours that actually distinguish one algorithm from another — backtracking logic, neighbour ordering, frontier selection, replanning, heuristic updates — should each be directly targeted by at least one test, rather than being verified only indirectly through end-to-end runs.

This motivated a two-level structure for each algorithm family. Unit tests use `StubExplorationState` to establish basic correctness. Behaviour tests use `GraphExplorationState` to verify the properties that are specific to each family.

### 2.1 DFS Family

The three DFS-based algorithms — `DepthFirstSearch`, `GreedyDepthFirstSearch` and `AdaptiveHeuristicSearch` — share the same underlying traversal structure and differ only in how they order neighbours at each step. This relationship shaped the test organisation directly.

**Unit tests** (`DFSTest`, `GreedyDFSTest`, `AdaptiveHeuristicSearchTest`) verify the same three properties for each algorithm in isolation: that the Orb is found on a two-node graph, that exactly one move is recorded, and that success is reported through the benchmark result. These tests confirm that the basic execution pipeline works correctly for each concrete class without exercising any shared traversal logic.

**Backtracking tests** (`BaseDFSBacktrackingTest`) target the traversal logic shared by all DFS variants, using `GraphExplorationState` to construct graphs where backtracking is unavoidable. Three scenarios are used: a branching graph where a dead-end branch must be fully explored and abandoned before the Orb is reached; a linear chain where no backtracking is required; and a two-branch graph where the Orb sits on the branch not explored first. Together these confirm that the return stack correctly unwinds dead ends in all relevant configurations.

**Ordering tests** (`GreedyDFSOrderingTest`) verify the specific property that distinguishes `GreedyDFS` from plain `DFS`: that neighbours are explored in order of ascending orb-distance. On a branching graph where the Orb sits on the closer branch, the algorithm should reach it in exactly one move, never touching the further branch. Additional tests confirm that equal distances do not cause infinite loops, and that the Orb is still found even when it lies on the further branch.

**Heuristic update tests** (`AdaptiveHeuristicSearchBehaviourTest`) target the behaviour unique to `AdaptiveHeuristicSearch`: that learned heuristic values are updated after visiting dead ends, and that these updates do not prevent the algorithm from eventually finding the Orb. Three scenarios are used: a graph with a direct dead-end branch; a linear chain where updates are monotone and cycles are therefore impossible; and a graph with a misleadingly close branch that leads nowhere, requiring the algorithm to backtrack and reconsider.

### 2.2 Frontier Family

The four frontier-based algorithms share the traversal infrastructure provided by `BaseFrontierSearch` and differ in how they score frontier candidates. This again shaped the organisation directly: the shared infrastructure is tested once, and each concrete algorithm is tested only for the behaviour it adds.

**Unit tests** (`FrontierUtilitySearchTest`, `ReplanningFrontierUtilitySearchTest`, `CoverageBiasedFrontierUtilitySearchTest`, `GradientFrontierUtilitySearchTest`) call `score()` directly with controlled inputs, verifying that each algorithm's scoring function produces the expected values without requiring any graph traversal.

For `GradientFrontierUtilitySearch`, the unit tests cover parameter validation, exact arithmetic across all three gradient cases — positive gain, zero gain and negative gain — and lambda amplification. The zero-gain case confirms that when a candidate sits at the same orb distance as the current node the score equals travel cost exactly, and the negative-gain case confirms that moving further from the orb incurs a correctly computed penalty. A final unit test verifies that, when travel costs are equal, the candidate offering the steepest descent receives the lower score.

For `CoverageBiasedFrontierUtilitySearch`, the unit tests cover parameter validation at both the strict boundary ($\mu = 0$ accepted, $\mu < 0$ rejected), the degenerate case where $\mu = 0$ recovers the base utility score exactly, and the monotone relationship between $\mu$ and the resulting score verified at three distinct values. Because `score()` has no visibility into the discovered graph, density-driven candidate ranking is tested in the behaviour file rather than here.

**Core behaviour tests** (`BaseFrontierSearchBehaviourTest`) verify the traversal logic shared by all frontier algorithms using `FrontierUtilitySearch` as a minimal concrete stand-in. Four scenarios are used: a linear chain requiring multi-hop navigation through intermediate nodes; a star graph where the best frontier node must be selected from several simultaneously available candidates; a two-path graph where travel cost influences which path is preferred when orb-distance estimates are equal; and a disconnected graph where the Orb is unreachable, confirming that the algorithm terminates gracefully rather than throwing.

**Replanning tests** (`ReplanningFrontierBehaviourTest`) target the specific property that distinguishes `ReplanningFrontierUtilitySearch`: that the frontier target can be abandoned mid-navigation when a better alternative becomes available. Three scenarios are used: a graph where a closer target appears after the initial navigation has begun, requiring the algorithm to redirect; a linear chain where replanning never fires but move counting must still be correct; and a graph where no superior target ever emerges, confirming that the algorithm produces the same result as the base variant in that case.

**Coverage bias tests** (`CoverageBiasedFrontierBehaviourTest`) verify end-to-end behaviour for `CoverageBiasedFrontierUtilitySearch` on graphs where frontier density actually varies between candidates. Nine scenarios are used. Two baseline cases confirm degenerate behaviour: that $\mu = 0$ is accepted without throwing and that the Orb is found, and that a linear graph — where only one frontier node exists at a time so density is always zero — produces a correct run even under a high value of $\mu$. Three tests use graphs with non-zero frontier density: a star graph confirms that a high $\mu$ still produces a valid run; the same star graph with $\mu = 0$ confirms that the base utility score drives selection and reaches the Orb in exactly one move, providing direct verification that density ranking actually influences candidate choice when $\mu > 0$; and a grid-like graph with two converging paths confirms termination under genuinely varying density. Two tests use adversarial graphs: a chain with a dead-end branch confirms that a high $\mu$ does not cause the algorithm to fixate on the dead end indefinitely, and a two-branch graph confirms that the Orb is found even when it lies on the branch not initially preferred. Finally, a disconnected graph confirms graceful termination and additionally asserts that the Orb was not reported as found, and a move-count test confirms that the benchmark result is populated correctly after a successful run.

**Gradient tests** (`GradientFrontierUtilitySearchTest`) verify end-to-end behaviour for `GradientFrontierUtilitySearch` on graphs where the gradient term actively influences frontier selection. Eight scenarios are used. A linear chain confirms that the algorithm correctly navigates all hops when every step is a descent. A branching graph where the steeper branch leads directly to the Orb confirms that the gradient term influences the initial frontier choice. A deeper two-branch graph with a high lambda confirms correct behaviour when the gradient signal must overcome a larger travel cost difference. A fourth test places the Orb on the shallower branch, confirming that the algorithm backtracks and finds it even after exploring the more attractive gradient direction first. A disconnected graph confirms graceful termination without throwing, and a final test confirms that move count and success are recorded correctly after a successful run.

### 2.3 Independent Algorithms

`RandomWalkSearch`, `RealTimeAStarSearch` and `IterativeDeepeningAStarSearch` each follow a fundamentally different exploration process and do not share a family-level base class. Their tests therefore follow the same pattern as the DFS unit tests: three properties are verified for each algorithm on the two-node stub — that the Orb is found, that the move count is correct, and that success is reported.

These tests are deliberately minimal. `RandomWalkSearch` is nondeterministic by design, making precise move-count assertions impossible on larger graphs. `IDA*` is included in the codebase primarily to demonstrate that iterative search strategies are ill-suited to online exploration, which is a design-level observation rather than a correctness property that can be directly tested. Keeping the tests focused on the properties that can be reliably verified is therefore both more honest and more useful than attempting to test behaviour that is either nondeterministic or deliberately suboptimal.

### 2.4 Integration Tests

With unit and behaviour tests confirming the correctness of each algorithm in isolation, a final set of integration tests verify that the complete `Algorithm` template-method pipeline works correctly end to end.

`DFSIntegrationTest` and `FrontierSearchIntegrationTest` each confirm three properties: that the benchmark writer is invoked after the search completes; that move count and success status are recorded together in a single consistent result; and, for DFS, that the algorithm name stored in the benchmark result matches the expected string. These tests are valuable not because they exercise new algorithm behaviour, but because they confirm that the three responsibilities handled by the `Algorithm` base class — delegating the search, recording the result, and invoking the writer — interact correctly with concrete implementations.

## 3. Benchmarking Tests

The benchmarking framework is tested independently from the search algorithms. This follows the same principle that motivated the separation of concerns in the implementation itself: changes to the benchmarking infrastructure should be verifiable without running any search algorithm, and vice versa.

### 3.1 Component Tests

`MoveCounterTest` and `NanoTimerTest` verify the two measurement components in isolation. For `MoveCounter`, three properties are established: that it starts at zero, that each call to `increment()` adds exactly one, and that ten consecutive increments produce a value of ten. For `NanoTimer`, three properties are verified: that elapsed time is zero before any timing has occurred; that elapsed time is positive after a timed interval; and that stopping before starting returns a non-negative value. Comparisons between two separately timed intervals were deliberately excluded from the suite, as they depend on JVM scheduling and produce results that are sensitive to machine load. The `BenchmarkSession` tests, which use a stub `Timer` returning a fixed value, provide more reliable coverage of the timing integration than wall-clock comparisons between two `NanoTimer` instances could offer.

`BenchmarkResultTest` verifies the default values and mutation behaviour of `BenchmarkResult` independently of `BenchmarkSession`: that the algorithm name is set at construction and cannot be changed, that `success` defaults to `true`, and that `moves`, `runtimeUs` and `seed` all default to zero and can be set explicitly. The default of `true` for `success` is worth noting: a run that throws before `markFailure()` is called should still appear in the analysis as a data point, rather than being silently omitted.

`BenchmarkSessionTest` verifies that the session correctly coordinates its timer and counter, using stub implementations of both interfaces. Four properties are established: that move count is accumulated correctly across multiple calls to `recordMove()`; that runtime is read from the timer and stored in the result; that `markSuccess()` and `markFailure()` are reflected correctly in the result; and that the seed is recorded without modification.

### 3.2 Pipeline Tests

`BenchmarkPipelineTest` verifies the full write-then-read cycle: that a result written by `CsvRunWriter` can be read back correctly by `BenchmarkLoader`, and that the round-trip preserves algorithm name, seed, move count, success status and runtime. Four additional properties are verified: that a failed run is recorded as such; that multiple runs in a single session are all read back; that the CSV header is written exactly once regardless of how many runs are appended to the same file; and that the column order in each data row matches the expected schema.

`BenchmarkLoaderEdgeCasesTest` verifies six edge conditions that the pipeline test does not cover: that an empty list is returned when the benchmark directory does not exist; that a header-only file produces an empty list rather than throwing; that non-CSV files in the directory are silently ignored; that runs are loaded correctly when they are spread across multiple CSV files; that all five fields are parsed with the correct types; and that a malformed row — one with fewer columns than expected — causes an `IllegalArgumentException` to be thrown rather than producing a silently incorrect result.

### 3.3 Analysis Tests

`BenchmarkAnalyzerTest` verifies the three core statistical properties of `BenchmarkAnalyzer`: that mean move count is computed correctly; that failed runs are excluded from both the success count and the mean; and that the best and worst move counts are correctly identified.

`BenchmarkAnalyzerStatisticsTest` extends this coverage to the remaining statistics not exercised by the core test: percentile computation, standard deviation, coefficient of variation, runtime mean and output sort order. The percentile tests use linear interpolation and verify that the implementation handles both even-sized lists (where the median falls between two elements) and degenerate cases (where a single run produces a p95 equal to its own value). The coefficient-of-variation test explicitly verifies that the result is zero when the mean is zero, confirming that a divide-by-zero case is handled gracefully rather than producing `NaN`. The sort-order test confirms that the output of `analyze()` is ordered by ascending mean move count, which is the order expected by `BenchmarkReportPrinter`.

`BenchmarkComparisonAnalyzerTest` and `BenchmarkComparisonAnalyzerEdgeCasesTest` verify the per-seed win-counting logic. The core tests confirm that the algorithm with the fewest moves on a given seed is awarded the win, that failed runs are excluded from consideration, and that wins are correctly accumulated across multiple seeds. The edge-case tests cover four additional scenarios: that a tie counts as exactly one win rather than zero or two; that a single algorithm wins every seed it enters; that an empty list is returned when all runs failed; and that an algorithm which loses every seed does not appear in the output.

## Summary

The test suite was designed around three primary objectives: isolation, coverage and honesty. Each component can be verified independently of the others, the behaviours that distinguish one algorithm from another are directly targeted rather than verified only through end-to-end runs, and tests are limited to properties that can be reliably established — avoiding assertions that would be meaningless for nondeterministic or deliberately suboptimal algorithms. Together, the unit, behaviour, integration and benchmarking tests provide a consistent foundation for validating both the correctness of the implementation and the reliability of the experimental results on which the final algorithm selection was based.