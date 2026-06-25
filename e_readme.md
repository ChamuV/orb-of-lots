# Orb of Lots

An autonomous search agent for the **Orb of Lots** Software Design & Programming coursework.

---

## Overview

This project implements an autonomous agent that locates a hidden Orb inside an initially unknown cavern graph. The agent cannot see the full map — it discovers the graph incrementally, deciding at each step where to move using only its current location, visible neighbours, and a heuristic distance estimate to the Orb.

The core challenge is an **online graph search problem**: unlike classical search, there is no complete graph to reason over upfront. Every algorithm must be evaluated on how efficiently it explores an unknown space, not just how well it routes through a known one.

The project covers algorithm design across several families, rigorous benchmarking over fixed random seeds, and a documented rationale for the final strategy selected.

---

## The Search Problem

The cavern is modelled as an undirected graph G = (V, E), where vertices are locations and edges are traversable connections. The agent starts at an unknown vertex s and must find the Orb at some unknown target vertex t.

At each step, the agent has access to:

- its **current location** and the locations of its **immediate neighbours**
- a **heuristic distance estimate** to the Orb (not a true distance — just an indication of relative proximity)

Global graph structure is **not available in advance**. This rules out classical offline algorithms like Dijkstra's or standard A* and motivates the algorithm families investigated below.

---

## Algorithm Investigation

Algorithms were investigated across four families, with each family motivated by a different approach to the explore/exploit trade-off inherent in online search.

| Family | Algorithms |
|---|---|
| Baseline | DFS, Greedy DFS, BFS, Random Walk |
| Heuristic online search | Real-Time A* (RTA*), IDA* |
| Frontier utility search | FrontierUtilitySearch, ReplanningFrontierUtilitySearch |
| Frontier utility variants | GradientFrontierUtilitySearch (λ = 1.5, 2.0, 3.0), CoverageBiasedFrontierSearch (μ = 0, 1.0, 1.5) |

Several candidate algorithms were designed, implemented, and subsequently **eliminated** during development — either because they were structurally unsound for the online setting (e.g. bidirectional search requires a known goal), or because their performance did not justify their complexity. The full design history is documented in [`docs/algorithm-families.md`](docs/algorithm-families.md).

---

## Final Exploration Strategy

**Selected algorithm:** _To be completed after benchmarking._

The selection criteria, established prior to running benchmarks to avoid post-hoc rationalisation, are:

1. **Average move count** — primary metric; lower is better
2. **Worst-case move count** — robustness across difficult seeds
3. **Coefficient of variation** — consistency across the seed set
4. **Runtime reliability** — must complete within the 10-second limit across all seeds

The final algorithm selection, benchmarking results, and decision rationale are documented in [`docs/final-selection.md`](docs/final-selection.md).

---

## Benchmarking

Benchmarks are run over a fixed set of random seeds to ensure fair, reproducible comparisons. All algorithms are evaluated in a single JVM invocation to eliminate warm-up variance.

Results are recorded per-algorithm as CSV files containing move count and runtime per seed. A summary across algorithms is written to `benchmark-data/summary.csv`.

Full methodology, seed lists, and results tables are in [`docs/benchmarking.md`](docs/benchmarking.md).

---

## Build and Run

**Prerequisites:** Java 11+, Gradle.

```bash
# Run the test suite
./gradlew clean test

# Run with GUI (graphical cavern visualisation)
./gradlew :temple:run -PchooseMain=main.GUImain

# Run with text output
./gradlew :temple:run -PchooseMain=main.TXTmain

# Run the full benchmark suite (all algorithms, fixed seed set)
# No recompilation needed after first build
java -cp build/libs/temple-*.jar main.BulkBenchmarkMain
```

> Run the benchmark from the `temple/` directory after an initial `./gradlew build`.

---

## Project Structure

```
.
├── temple/
│   └── src/
│       ├── main/java/
│       │   └── student/
│       │       ├── searchalg/        # All exploration algorithm implementations
│       │       └── benchmark/        # Benchmarking infrastructure
│       └── test/java/                # Unit and integration tests
│
├── docs/
│   ├── algorithm-families.md         # Motivation, design, and elimination of candidate algorithms
│   ├── benchmarking.md               # Methodology, seed lists, and full results
│   ├── final-selection.md            # Rationale for final algorithm choice
│   └── design-decisions.md           # Key implementation and architectural decisions
│
├── benchmark-data/                   # Raw CSV output from benchmark runs
├── README.md
└── LICENSE
```

---

## Testing

Tests are in `temple/src/test/java` and cover:

- **Unit tests** for core helper methods and utility functions
- **Component tests** for individual algorithmic decisions (frontier scoring, replanning triggers)
- **Reproducible benchmarks** using fixed random seeds with CSV output for analysis

---

## Documentation

| Document | Contents |
|---|---|
| [`algorithm-families.md`](docs/algorithm-families.md) | Motivation and analysis of each algorithm family investigated |
| [`benchmarking.md`](docs/benchmarking.md) | Benchmark methodology, metrics, seed set, and full results |
| [`final-selection.md`](docs/final-selection.md) | Final algorithm selection with experimental justification |
| [`design-decisions.md`](docs/design-decisions.md) | Architectural choices, refactoring history, and engineering trade-offs |

---

## Acknowledgements

The Orb of Lots framework was originally developed by Eric Perdew, Ryan Pindulic, and Ethan Cecchetti at Cornell University's Department of Computer Science. All exploration algorithm implementations in this repository are my own work, developed for the Software Design & Programming module.

---

## Author

Chamundeshwari Rajputri Vadamalai