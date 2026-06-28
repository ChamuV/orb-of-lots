# Software Architecture

## Introduction

This document describes the software architecture adopted throughout the project. Unlike the accompanying *Algorithm Design* document, which focuses on the evolution of the search strategies themselves, this document explains how the codebase was organised to support the implementation, comparison and benchmarking of multiple exploration algorithms. The emphasis is therefore on the software engineering decisions that shaped the project structure, the rationale behind the chosen architecture, and the design choices that enabled new algorithms to be developed and evaluated with minimal code duplication.

## 1. Architectural Principles

The project builds upon the existing Temple of Lots framework, which provides the game environment, exploration API and supporting infrastructure. Rather than modifying the underlying framework, this project extends it through three major software components:

* Search Algorithms, containing the exploration strategies developed throughout this investigation.
* Benchmarking, providing a reusable framework for evaluating algorithm performance and exporting experimental results.
* Testing, containing the unit and integration tests used to validate the correctness of the implementation.

The guiding principle throughout the design was to keep these responsibilities separate. Search algorithms should contain only the exploration logic, benchmarking should remain independent of algorithm behaviour, and testing should verify correctness without influencing the implementation. This separation reduces coupling, improves maintainability and makes new algorithms straightforward to develop, evaluate and compare.

The architecture was therefore designed around established object-oriented software engineering principles, particularly the SOLID principles of Single Responsibility, Open-Closed Design and Dependency Inversion, together with an emphasis on reuse through inheritance and abstraction.

## 2. Search Algorithm Framework

The `student.searchalg` package contains the framework used by every exploration algorithm implemented throughout this project. Rather than allowing each algorithm to define its own execution flow, the package separates the responsibilities of exploration, execution and benchmarking into a small set of reusable abstractions.

The common exploration interface is defined by `SearchAlgorithm`, which exposes a single `findOrb(ExplorationState state)` method. This provides a consistent interface to the Temple framework regardless of the underlying search strategy. Benchmark-related responsibilities are separated through the `BenchmarkableAlgorithm` interface, which exposes the algorithm name together with its associated `BenchmarkSession` without introducing benchmark-specific behaviour into the exploration interface.

These two interfaces are brought together by the abstract `Algorithm` base class, which implements the common execution workflow shared by every search strategy. The `findOrb()` method is implemented once as a final template method that starts the benchmark session, delegates the search to the abstract `runSearch()` method, records the outcome, and exports the benchmark results. Individual algorithms therefore implement only the search behaviour itself, while execution management and benchmark recording are inherited automatically.

```text
SearchAlgorithm
        │
        ▼
    Algorithm
        │
        ▼
    findOrb()
        │
        ├── Start benchmark session
        ├── Call runSearch()
        ├── Stop benchmark session
        ├── Record result
        └── Export benchmark data
        │
        ▼
Implemented Search Algorithm
    (overrides runSearch())
```

### 2.1 The Algorithm Base Class

The Algorithm class serves as the common foundation for every exploration strategy implemented in the project. Rather than allowing each algorithm to evolve independently, it establishes a single abstraction through which all exploration strategies integrate with the surrounding framework. This provides a consistent structure for every implementation while ensuring that algorithm-specific code remains focused solely on the exploration policy.

The use of an abstract base class also promotes extensibility. New search strategies inherit the existing framework and only provide the behaviour unique to that algorithm, allowing new implementations to be introduced without modifying the surrounding infrastructure. This follows the Open-Closed Principle, whereby the framework is open to extension through inheritance while remaining closed to modification.

By centralising the common responsibilities of all search algorithms within a single abstraction, the overall architecture becomes easier to maintain, more consistent across implementations, and significantly less susceptible to duplicated functionality.

```text

                  Responsibilities of Algorithm
                +------------------------------+
                |    Algorithm (abstract)      |
                +------------------------------+
                           │
      ┌────────────────────┼────────────────────┐
      │                    │                    │
      ▼                    ▼                    ▼
  Execution          Benchmarking          Shared Utilities
      │                    │                    │
  • findOrb()        • Benchmark timing   • recordMove()
  • Template method  • Success/failure    • getAlgorithmName()
  • Calls runSearch()• CSV export         • getBenchmarkSession()
                     • Move counting
```

### 2.2 Family-Level Base Classes

The implemented algorithms naturally fall into several families. The DFS-based and frontier-based algorithms share the same underlying traversal mechanics, differing mainly in their decision rules rather than the overall structure of the search. To avoid repeating this common logic, the project introduces family-level base classes that capture the shared behaviour for each group.

`BaseDFS` contains the common traversal logic for depth-first exploration. It manages the visited set, maintains an explicit stack for backtracking, and implements the main DFS traversal loop. Individual DFS algorithms only define how neighbouring nodes should be ordered through `orderedNeighbours`. This allows Depth-First Search, Greedy Depth-First Search and Adaptive Heuristic Search to share the same traversal structure while differing only in how the next neighbour is selected.

`BaseFrontierSearch` contains the common traversal logic for frontier-based exploration. It maintains the discovered graph, tracks visited nodes, stores the current frontier, records known distances to the Orb, computes shortest-path travel costs, and handles navigation towards the selected frontier. Individual frontier algorithms only define how frontier candidates should be scored through `score`, and may optionally override `shouldReplan` when the search strategy requires the frontier to be re-evaluated during navigation.

```text
Algorithm
   │
   ├── BaseDFS
   │     ├── DepthFirstSearch
   │     ├── GreedyDepthFirstSearch
   │     └── AdaptiveHeuristicSearch
   │
   └── BaseFrontierSearch
         ├── FrontierUtilitySearch
         ├── ReplanningFrontierUtilitySearch
         ├── GradientFrontierUtilitySearch
         └── CoverageBiasedFrontierUtilitySearch
```

### 2.3 Implementing New Algorithms

The architecture supports two approaches for implementing new exploration algorithms. Algorithms that belong to an existing search family inherit from the corresponding family-level base class, while algorithms with fundamentally different exploration behaviour inherit directly from `Algorithm`.

This distinction reflects the degree to which behaviour can be reused. DFS-based and frontier-based algorithms share substantial amounts of traversal logic, making inheritance from `BaseDFS` and `BaseFrontierSearch` a natural choice. New algorithms within these families therefore only implement the small amount of logic that differentiates them, such as neighbour ordering or frontier scoring.

Algorithms whose exploration strategy does not naturally fit an existing family instead inherit directly from `Algorithm` and provide their own implementation of `runSearch()`. Examples include `RandomWalkSearch`, `IterativeDeepeningAStarSearch` and `RealTimeAStarSearch`, each of which follows a fundamentally different exploration process and therefore does not benefit from the specialised family abstractions.

```text
                           Algorithm
                                │
        ┌───────────────────────┼────────────────────────┐
        │                       │                        │
        ▼                       ▼                        ▼
    BaseDFS            BaseFrontierSearch        Independent Algorithms
        │                       │                        │
        │                       │        ┌───────────────┼───────────────┐
        ▼                       ▼        ▼               ▼               ▼
      DFS                 Frontier       Random Walk    IDA*          Real-Time A*
   Greedy DFS         Replanning
   Adaptive DFS       Gradient
                      Coverage
```

This layered structure allows related algorithms to reuse existing functionality where appropriate, while still supporting entirely new search strategies that require their own implementation.

## 3. Benchmarking Framework

Whilst developing an effective exploration algorithm was the primary objective of the project, an equally important goal was to compare the different search strategies in a fair and reproducible manner. This motivated the development of a dedicated benchmarking framework that remained independent of the search algorithms themselves.

As described in the previous section, every exploration strategy implements the `BenchmarkableAlgorithm` interface through the shared `Algorithm` base class. This allows the benchmarking framework to interact with every algorithm through a common abstraction, regardless of the underlying search strategy. Consequently, new algorithms automatically participate in the benchmarking framework without requiring any additional benchmarking code, ensuring that every implementation is evaluated under identical conditions.

### 3.1 Benchmark Session

The `BenchmarkSession` class represents a single execution of an exploration algorithm and is responsible for collecting all benchmark measurements throughout that run. During execution it records the runtime, movement count and search outcome, before producing a `BenchmarkResult` containing the complete set of measurements for that execution.

Centralising benchmark collection within a dedicated class ensures that every algorithm is evaluated using the same measurement process while keeping performance recording separate from the exploration logic. This provides a consistent representation of benchmark data that can be exported, analysed and compared across all search algorithms.

```text
                 BenchmarkSession
                         │
      ┌──────────────────┼──────────────────┐
      │                  │                  │
      ▼                  ▼                  ▼
  Runtime           Move Count          Success / Failure
      │                  │                  │
      └──────────────────┼──────────────────┘
                         ▼
                 BenchmarkResult
```

### 3.2 Benchmark Writers

Rather than writing benchmark results directly to disk, the framework separates result generation from result storage through the `BenchmarkWriter` interface. This follows the Dependency Inversion Principle by allowing algorithms to depend on an abstraction rather than a specific output format.

The default implementation, `CsvRunWriter`, records each `BenchmarkResult` as a CSV file within the benchmark data directory. However, because the output mechanism is abstracted behind a common interface, alternative writers can easily be introduced without modifying the benchmarking framework. This also simplifies testing, where a mock writer can be substituted to verify benchmark behaviour without creating output files.

```text
                 BenchmarkResult
                         │
                         ▼
               BenchmarkWriter
                         │
              +----------+----------+
              │                     │
              ▼                     ▼
        CsvRunWriter         Test/Mock Writer
              │
              ▼
        benchmark-data/*.csv
```

### 3.3 Bulk Benchmark Runner

With the infrastructure for benchmarking a single algorithm now in place, the next requirement was a mechanism for evaluating multiple search strategies under identical conditions. This led to the development of the `BulkBenchmarkRunner`, which automates the benchmarking process across the complete collection of implemented algorithms.

Rather than embedding benchmark orchestration within individual algorithms, the runner is responsible for selecting the algorithms to evaluate, executing each one in turn, and collecting the resulting benchmark data. This architecture makes the benchmarking framework extensible. As new search algorithms are added, they can be included in the bulk benchmark suite with minimal additional code. The `BulkBenchmarkRunner` is therefore able to execute an entire collection of algorithms within a single run, automatically collecting a separate benchmark for each implementation.

```text
                BulkBenchmarkRunner
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
   Algorithm A     Algorithm B     Algorithm C
        │                │                │
        ▼                ▼                ▼
 BenchmarkResult  BenchmarkResult  BenchmarkResult
        │                │                │
        └────────────────┼────────────────┘
                         ▼
                Benchmark Data
```

### 3.4 Benchmark Analysis

Following execution of the bulk benchmark suite, a final stage of the framework is responsible for analysing the collected results and producing summary statistics. Rather than embedding statistical analysis within the benchmark runner itself, the post-processing stage is implemented as a separate component of the architecture. This separation allows new statistical measures to be added, modified or removed without affecting either the benchmark execution framework or the search algorithms.

The analysis process is coordinated by `BenchmarkAnalyzer`, which reads the benchmark data and constructs the statistical summaries. The computed statistics are represented by `BenchmarkStatistics`, while `BenchmarkReportPrinter` is responsible for presenting them in a human-readable format. By separating data analysis, statistical representation and report generation into independent classes, the framework follows the Single Responsibility Principle and allows each component to evolve independently.

```text
               Benchmark CSV Files
                        │
                        ▼
              BenchmarkAnalyzer
                        │
            +-----------+-----------+
            │                       │
            ▼                       ▼
  BenchmarkStatistics       BenchmarkRun
            │
            ▼
  BenchmarkReportPrinter
            │
            ▼
     Console Summary Report
```

## 4. Test Organisation

The testing infrastructure follows the same separation of responsibilities as the main codebase. Rather than maintaining a single monolithic test suite, the project organises tests according to the component being validated, allowing each subsystem to be verified independently.

Search algorithm tests are organised by search family, with dedicated test packages for the DFS, frontier, random, real-time and heuristic search implementations. These include both unit tests for individual algorithms and integration tests that validate behaviour within representative exploration environments.

The benchmarking framework is tested independently from the search algorithms. Separate test suites validate the benchmark pipeline, session management, timers, move counters and statistical analysis components. By isolating these tests from the exploration algorithms, changes to the benchmarking infrastructure can be verified without affecting the search implementations.

```text
test/
│
├── searchalg/
│     ├── dfs/
│     ├── frontier/
│     ├── bfs/
│     ├── random/
│     ├── rta/
│     ├── idastar/
│     ├── GraphExplorationState.java
│     └── StubExplorationState.java
│
└── benchmark/
      ├── analysis/
      ├── counter/
      ├── timer/
      ├── BenchmarkLoaderEdgeCasesTest.java
      ├── BenchmarkResultTest.java
      ├── BenchmarkSessionTest.java
      └── BenchmarkPipelineTest.java
```

## Summary

The software architecture was designed around three primary objectives: modularity, extensibility and separation of concerns. Reusable abstractions minimise duplication across related algorithms, while the independent benchmarking and testing frameworks ensure that new search strategies can be evaluated and validated without modifying the surrounding infrastructure. This architecture provided a maintainable foundation that supported the progressive development and comparison of multiple exploration algorithms throughout the project.