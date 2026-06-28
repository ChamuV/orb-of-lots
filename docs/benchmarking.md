# Benchmarking

## Introduction

This document describes the benchmarking methodology used to evaluate the exploration algorithms developed throughout this project. Whilst the primary objective of the investigation was to design an effective online search strategy for locating the Orb, it was equally important to compare the resulting algorithms in a fair, reproducible and systematic manner.

Rather than evaluating algorithms solely on whether they successfully located the Orb, the benchmarking framework measures how efficiently each strategy performs under identical experimental conditions. This document therefore describes the benchmarking methodology, the parameter selection process used for the final algorithm, and the comparative evaluation that ultimately led to the selection of Coverage-Biased Frontier Utility Search.

## Experiment
### 2.1 Setup

To enable a fair comparison between the implemented search algorithms by ensuring that every algorithm explored the same collection of caverns, benchmark execution was automated through the `BulkBenchmarkRunner`, which iterates over the complete set of search algorithms and executes each one using the same sequence of benchmark seeds.

The benchmark seed set was generated once prior to experimentation using Java's pseudorandom number generator. The generated seeds were stored in `benchmark-data/seeds.txt` and reused throughout the investigation. Since the Temple framework deterministically generates a cavern from its seed, the same environments can be recreated exactly for future verification of the benchmark results.

```text
          Fixed Seed Set
                │
     ┌──────────┼──────────┐
     │          │          │
   Seed 1     Seed 2     Seed n
     │          │          │
     └──────────┼──────────┘
                │
                ▼
      BulkBenchmarkRunner
                │
      ┌─────────┼─────────┐
      │         │         │
      ▼         ▼         ▼
  Algorithm A Algorithm B Algorithm C
      │         │         │
      └─────────┼─────────┘
                │
                ▼
        Benchmark Results
```

### 2.2 Evaluation Metrics

Using the benchmark seed set described previously, each search algorithm was evaluated using two performance metrics. The primary metric was the number of moves required to locate the Orb, since a lower movement count corresponds directly to a more efficient exploration strategy. A secondary metric of execution time was also recorded to provide an indication of the computational overhead associated with each algorithm. Although runtime can vary depending on hardware and implementation details, algorithms with substantially higher execution times require greater computational resources and therefore incur a higher computational cost.

To summarise performance across the benchmark seed set, the following statistics were computed for both movement count and execution time. Reporting multiple summary statistics ensures that conclusions are not based solely on average performance, allowing both typical behaviour and unusually favourable or challenging benchmark runs to be considered.

* Mean – the average performance across all benchmark runs, providing an overall measure of algorithm efficiency.
* Median – the middle result when all runs are ordered, reducing the influence of unusually easy or difficult caverns.
* Best – the lowest value achieved, representing the algorithm’s strongest performance on the benchmark set.
* Worst – the highest value achieved, capturing an algorithm’s behaviour under the most challenging benchmark conditions rather than considering only its average performance.

```text
                 Benchmark Metrics
                        │
        ┌───────────────┼───────────────┐
        │                               │
        ▼                               ▼
 Primary Metric                  Secondary Metric
 Number of Moves                 Execution Time
        │                               │
        └───────────────┬───────────────┘
                        ▼
        Mean • Median • Best • Worst
```