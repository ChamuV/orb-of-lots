# Orb of Lots

### Designing an Online Graph Search Algorithm for Unknown Environment Exploration

> Exploring how classical search principles can be adapted to unknown environments.

---

## Project Overview

The Orb of Lots is an online graph exploration problem in which an autonomous agent must locate a hidden Orb within an initially unknown cavern before successfully escaping. Although the complete challenge consists of both an exploration phase and an escape phase, this project focuses exclusively on the exploration phase, where the objective is to locate the Orb as efficiently as possible while gradually discovering the structure of the environment.

During exploration, the agent has access only to information available from its current position. At each step, it knows its current location, the neighbouring nodes that can be reached next, and the straight-line distance to the Orb. The remainder of the graph is revealed only through physical exploration, meaning every movement decision must be made using incomplete information. This places the problem within the domain of online graph search, where planning and exploration occur simultaneously.

The aim of this project was to investigate how principles from classical graph search can be adapted to this online setting. Multiple search strategies were implemented, benchmarked, and evaluated to understand which ideas remain effective when the search graph is initially unknown. The findings from this investigation ultimately led to the development of Coverage-Biased Frontier Utility Search, a new frontier-based exploration algorithm that extends frontier search by introducing a lightweight coverage heuristic to bias exploration towards frontier regions expected to maximise the amount of newly discovered environment.

---

## The Search Problem

The cavern is modelled as an undirected graph G = (V, E), where vertices are locations and edges are traversable connections. The agent starts at an unknown vertex s and must find the Orb at some unknown target vertex t.

At each step, the agent has access to:

- its **current location** and the locations of its **immediate neighbours**
- a **heuristic distance estimate** to the Orb (not a true distance — just an indication of relative proximity)

Global graph structure is **not available in advance**. This rules out classical offline algorithms like Dijkstra's or standard A* and motivates the algorithm families investigated below.

---

## Selected Algorithm

The selected strategy is **Coverage-Biased Frontier Utility Search**, a novel algorithm developed from the frontier utility search family. Rather than choosing the next move based on local information alone, it maintains an explicit frontier — the set of all discovered but not yet visited nodes — and selects the best target globally at each decision point.

The scoring function for each frontier candidate n is:

```
score(n) = h(n) + d(p, n) - μ · δ(n)
```

Where:
- **h(n)** — heuristic distance from n to the Orb
- **d(p, n)** — exact BFS travel cost from the agent's current position p to n
- **δ(n)** — local frontier density: the number of n's known neighbours also in the frontier
- **μ** — coverage weight; controls how strongly unexplored boundary regions are preferred

The agent selects the frontier node n\* that minimises this score:

```
n* = argmin_{n ∈ V_f} [ h(n) + d(p, n) - μ · δ(n) ]
```

The coverage bonus μ · δ(n) rewards nodes that sit at the boundary of unexplored clusters, reducing catastrophic outcomes on seeds where the Orb is in a region the agent would otherwise reach last. The parameter μ was selected by systematic sweep across eleven values over 500 fixed seeds. μ = 1.0 produced the best mean move count (55.86) and the lowest worst-case of any algorithm tested (274 moves).

Full mathematical treatment, architecture, and parameter selection rationale are in [`docs/final-selection.md`](docs/final-selection.md).

---

## Algorithm Investigation

Algorithms were investigated across four families, with each family motivated by a different approach to the explore/exploit trade-off inherent in online search.

| Family | Algorithms |
|---|---|
| Baseline | DFS, Greedy DFS, BFS, Random Walk |
| Heuristic online search | Real-Time A* (RTA*), IDA* |
| Frontier utility search | FrontierUtilitySearch, ReplanningFrontierUtilitySearch |
| Frontier utility variants | GradientFrontierUtilitySearch (λ variants), CoverageBiasedFrontierSearch (μ variants) |

Several candidates were eliminated during development — either structurally incompatible with the online setting (bidirectional search requires a known goal) or outperformed without sufficient justification for their complexity. Full design history is in [`docs/algorithm-families.md`](docs/algorithm-families.md).

---

## Benchmarking

Each algorithm was evaluated over 500 fixed random seeds in a single JVM invocation to eliminate warm-up variance. The selection criteria, defined before benchmarking began:

1. **Mean move count** — primary metric
2. **Worst-case move count** — robustness across difficult seeds
3. **Coefficient of variation** — consistency across the seed set
4. **Runtime reliability** — must complete within the 10-second limit

Coverage-Biased Frontier Utility Search (μ=1.0) was selected: best mean within the frontier family and the lowest worst-case of any algorithm tested (274 moves — a 31% reduction over the nearest competitor). Full results and methodology are in [`docs/benchmarking.md`](docs/benchmarking.md).

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