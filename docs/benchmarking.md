# Benchmarking

## Introduction

This document describes the benchmarking methodology used to evaluate the exploration algorithms developed throughout this project. Whilst the primary objective of the investigation was to design an effective online search strategy for locating the Orb, it was equally important to compare the resulting algorithms in a fair, reproducible and systematic manner.

Rather than evaluating algorithms solely on whether they successfully located the Orb, the benchmarking framework measures how efficiently each strategy performs under identical experimental conditions. This document therefore describes the benchmarking methodology, the parameter selection process used for the final algorithm, and the comparative evaluation that ultimately led to the selection of Coverage-Biased Frontier Utility Search.

## Experiment
### 2.1 Setup

To enable a fair comparison between the implemented search algorithms by ensuring that every algorithm explored the same collection of caverns, benchmark execution was automated through the `BulkBenchmarkRunner`, which iterates over the complete set of search algorithms and executes each one using the same sequence of benchmark seeds.

The benchmark seed set was generated once prior to experimentation using Java’s pseudorandom number generator. A total of 1000 random seeds were initially generated and stored in `benchmark-data/seeds.txt`, from which 500 seeds were selected for the experiments reported in this document. Using a large collection of randomly generated caverns reduces the influence of unusually easy or difficult layouts, providing a more representative assessment of algorithm performance. Since the Temple framework deterministically generates a cavern from its seed, the same benchmark suite can be recreated exactly, ensuring that the experiments are fully reproducible.

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
Each algorithm was executed once on every benchmark seed, giving 500 benchmark runs per algorithm. In total, the comparative evaluation therefore consisted of 5,000 benchmark runs across the ten final exploration algorithms.

### 2.2 Evaluation Metrics

Using the benchmark seed set described previously, each search algorithm was primarily evaluated using the number of moves required to locate the Orb, since a lower movement count corresponds directly to a more efficient exploration strategy. Execution time was also recorded by the benchmarking framework to provide an indication of computational overhead, although it was not used as the primary criterion for algorithm selection.

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
Although the benchmarking framework records additional descriptive statistics, including standard deviation, coefficient of variation and selected percentiles, these were retained primarily for completeness. The final algorithm selection was based on the mean, median and worst-case movement counts, which most directly reflect exploration efficiency and robustness.

### 2.3 Parameter Selection

To determine an appropriate value for the proposed algorithm's weighting parameter, **$λ$**, a parameter sweep was performed. Values ranging from **0.5** to **10.0** were evaluated using the benchmarking methodology described previously. Increasing values of $λ$ place progressively greater emphasis on the coverage component of the utility function, encouraging the algorithm to favour frontier nodes expected to reveal larger unexplored regions.

#### Parameter Sweep Results

| $λ$ | Mean | Median | Best | Worst |
|---:|----:|------:|----:|-----:|
| **1.5** | **55.86** | **42.0** | 10 | **274** |
| 0.5 | 55.87 | 43.5 | 10 | 286 |
| 5.0 | 55.95 | 44.0 | 10 | 356 |
| 4.5 | 56.86 | 43.0 | 10 | 446 |
| 10.0 | 56.98 | 45.0 | 10 | 429 |
| 3.0 | 57.08 | 42.0 | 10 | 378 |
| 2.0 | 57.34 | 45.0 | 10 | 304 |
| 1.0 | 57.36 | 44.0 | 10 | 364 |
| 4.0 | 57.52 | 44.0 | 10 | 561 |
| 6.0 | 57.62 | 44.0 | 10 | 509 |
| 5.5 | 57.72 | 45.0 | 10 | 479 |

The parameter sweep indicates that **$λ = 1.5$** provides the most effective overall weighting. It achieved the **lowest mean movement count** across the benchmark suite while also producing the **lowest median**, indicating that the improvement reflects typical performance rather than being driven by a small number of favourable benchmark runs. Furthermore, its **worst-case performance** was substantially better than almost all other parameter values, suggesting that the chosen weighting remains robust even on the most challenging cavern layouts.

#### Individual Benchmark Wins

| $λ$ | Number of Wins |
|---:|--------------:|
| 0.5 | **61** |
| 10.0 | 51 |
| 1.0 | 25 |
| 2.0 | 19 |
| **1.5** | 18 |
| 3.0 | 17 |
| 4.0 | 15 |
| 5.0 | 14 |
| 4.5 | 12 |
| 6.0 | 10 |
| 5.5 | 4 |

Although **$λ = 0.5$** achieved the greatest number of individual benchmark wins, these victories came at the cost of poorer worst-case behaviour. In contrast, **$λ = 1.5$** consistently produced strong results across the entire benchmark suite, combining the lowest mean and median movement counts with the strongest worst-case performance amongst the leading algorithms. Since the objective was to identify a parameter that performs reliably across a wide range of caverns rather than one that occasionally achieves the best result, **$λ = 1.5$** was selected for the final evaluation.

## 2.4 Comparative Evaluation

Following parameter selection, the chosen **Coverage-Biased Frontier Utility Search $(λ = 1.5)$** was benchmarked against the remaining exploration algorithms. Each algorithm was evaluated using the same benchmark methodology and fixed benchmark seed set, allowing their performance to be compared directly under identical experimental conditions.

| Algorithm | Mean Moves | Median | Best | Worst |
|-----------|-----------:|-------:|-----:|------:|
| **Coverage-Biased Frontier Utility Search $(λ = 1.5)$** | **55.86** | **42.0** | **10** | **274** |
| Gradient Frontier Utility Search $(λ = 2.0)$ | 56.06 | 42.0 | 10 | 385 |
| Replanning Frontier Utility Search | 57.98 | 43.0 | 10 | 533 |
| Frontier Utility Search | 58.50 | 43.0 | 10 | 567 |
| Adaptive Heuristic Search | 80.54 | 51.0 | 10 | 780 |
| Greedy DFS | 80.97 | 50.5 | 10 | 882 |
| Real-Time A* Search | 123.98 | 69.0 | 10 | 1462 |
| DFS | 177.93 | 137.0 | 10 | 826 |
| Random Walk | 178.72 | 139.5 | 10 | 718 |
| Iterative Deepening A* | 3,042,191.08 | 1209.0 | 10 | 508,981,707 |

The results demonstrate that the frontier-based search strategies consistently achieved the strongest overall performance. The proposed Coverage-Biased Frontier Utility Search obtained the lowest average movement count of all evaluated algorithms, requiring an average of **55.86** moves to locate the Orb. It also produced the lowest median movement count together with one of the strongest worst-case performances amongst the leading algorithms, indicating that its behaviour remained consistently reliable across the benchmark suite rather than depending on a small number of particularly favourable caverns.

Although average performance formed the primary basis for algorithm selection, an additional comparison was made by recording the number of benchmark runs won by each algorithm, where a *win* corresponds to achieving the lowest movement count on an individual cavern.

| Algorithm | Benchmark Wins |
|-----------|---------------:|
| Adaptive Heuristic Search | **139** |
| Frontier Utility Search | 26 |
| **Coverage-Biased Frontier Utility Search $(λ = 1.5)$** | **18** |
| Greedy DFS | 13 |
| Replanning Frontier Utility Search | 9 |
| DFS | 9 |
| Random Walk | 4 |
| Gradient Frontier Utility Search $(λ = 2.0)$ | 2 |
| Real-Time A* Search | 0 |
| Iterative Deepening A* | 0 |

Interestingly, the proposed algorithm did not achieve the largest number of individual benchmark wins. Instead, **Adaptive Heuristic Search** recorded the highest number of wins despite exhibiting substantially poorer average and worst-case performance than the frontier-based approaches. This behaviour suggests that Adaptive Heuristic Search can perform exceptionally well on certain cavern layouts, but is considerably less consistent across the benchmark suite as a whole. This also demonstrates that winning individual benchmark runs is not necessarily indicative of consistent overall performance. Rather than occasionally producing the best result, the selected **Coverage-Biased Frontier Utility Search $(λ = 1.5)$** maintained a consistently low movement count across the complete benchmark suite, making it the most reliable overall strategy.

These results suggest that maintaining an explicit model of the explored frontier is an effective strategy for online graph exploration. Every frontier-based algorithm ranked amongst the strongest-performing approaches, consistently outperforming the DFS, real-time and random search families. By reasoning about the explored frontier rather than making purely local decisions, these algorithms make more informed decisions about where new information is likely to be discovered, avoiding much of the unnecessary backtracking and local decision making observed in the alternative approaches. The original Frontier Utility Search already performed competitively, while incorporating the proposed coverage term yielded a modest but measurable improvement in both average performance and robustness.

The heuristic DFS algorithms also demonstrated the benefit of exploiting the heuristic distance estimate provided by the game. Both **Greedy DFS** and **Adaptive Heuristic Search** substantially improved upon standard DFS by making use of the heuristic information available at each step. However, because their decisions remain fundamentally local, they were unable to match the consistently strong performance of the frontier-based approaches, which reason over the explored frontier as a whole rather than only the current neighbourhood.

The remaining algorithms further highlight the importance of selecting an exploration strategy suited to online graph search. **Random Walk** and standard **DFS** required approximately three times as many moves as the proposed approach, whilst **Real-Time A\*** also produced considerably higher movement counts despite making heuristic-guided decisions. Finally, **Iterative Deepening A\*** proved unsuitable for this environment, exhibiting movement counts several orders of magnitude larger than every other algorithm due to repeatedly re-exploring and physically retraversing large sections of the cavern.

Overall, these results demonstrate that the proposed **Coverage-Biased Frontier Utility Search** provides the strongest balance between efficiency, robustness and consistency, and was therefore selected as the final exploration strategy used throughout the remainder of the project. Whilst several algorithms occasionally achieved better individual benchmark runs, none matched its combination of low average movement count, strong worst-case behaviour and consistently competitive performance across the complete benchmark suite.

## Summary

A parameter sweep was first performed to determine an appropriate value of **$λ$** for the proposed Coverage-Biased Frontier Utility Search, identifying **$λ = 1.5$** as the most effective weighting. The selected algorithm was then benchmarked against the remaining exploration strategies using a common set of benchmark caverns, demonstrating that it achieved the strongest overall balance of efficiency, robustness and consistency. These results therefore justified its selection as the final exploration algorithm used throughout the remainder of the project.