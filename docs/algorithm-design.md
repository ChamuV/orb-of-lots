# Algorithm Design

## Introduction

This document describes the design process that led to the development of the exploration algorithms implemented in this repository, with particular emphasis on the progression that ultimately resulted in Coverage-Biased Frontier Utility Search.

Rather than presenting the algorithms as independent implementations, the discussion follows the sequence of questions that motivated each design decision. Instead of attempting to design an entirely new search algorithm from first principles, the investigation began by examining well-established strategies from both classical graph search and online search, adapting the underlying ideas that remained applicable to exploration in an initially unknown environment.

At each stage, the strengths and limitations of the current approach motivated the next refinement. The central question throughout the investigation was therefore not “Which algorithm should be implemented?”, but rather “Which ideas from existing search algorithms remain effective when the graph is initially unknown?” This resulted in a sequence of progressively more informed decision-making strategies,

$$
\text{Systematic Exploration}
\rightarrow
f(n)=g(n)+h(n)
\rightarrow
f(n)=g(n)+h(n)+u(n)
\rightarrow
f(n)=g(n)+h(n)+u(n)+\lambda c(n),
$$

where each additional term addresses a limitation identified in the previous formulation. The remainder of this document follows this progression, motivating each refinement before introducing the next.

## 1. Systematic Exploration

The first question considered during the investigation was deliberately simple:

> **Can the Orb be found reliably without attempting to optimise the search?**

Before considering heuristics or more sophisticated decision-making, it was first necessary to understand how far purely exploratory behaviour could succeed. The initial investigation therefore focused solely on **reaching the Orb**, temporarily setting aside the question of how efficiently it could be found. This provided a natural baseline against which progressively more informed search strategies could later be evaluated.

This naturally led to the investigation of two fundamental families of exploration algorithms: **Random Walk**, representing stochastic exploration, and **Depth-First Search (DFS)**, representing systematic exploration.

### 1.1 Random Walk

Random Walk is an exploration strategy in which each neighbouring node has an equal probability of being selected as the agent's next move. Consequently, the agent performs a purely stochastic traversal of the environment, making no use of heuristic information or any explicit notion of planning.

Although such a strategy is not expected to be efficient, it will eventually locate the Orb provided the graph is connected. Random Walk therefore serves as a natural baseline for the investigation, demonstrating that the exploration problem can be solved through exploration alone, albeit typically at the cost of a large number of unnecessary movements.

### 1.2 Depth-First Search

The randomness introduced by Random Walk can be removed by adopting a more systematic exploration strategy. **Depth-First Search (DFS)** is a widely used graph traversal algorithm that explores as far as possible along one branch before backtracking to the nearest unexplored decision point. In an unknown environment, this ensures that every newly discovered branch is eventually explored while avoiding much of the repeated wandering characteristic of purely random exploration.

Although DFS makes no explicit attempt to move towards the Orb, it provides a deterministic exploration policy that substantially reduces unnecessary movement while requiring only local information available at each step.

#### 1.2.1 Greedy Depth-First Search

A natural refinement to DFS is to exploit the heuristic information already provided by the environment, namely the estimated distance to the Orb. Rather than exploring neighbouring nodes in an arbitrary order, **Greedy Depth-First Search** prioritises those with the smallest estimated distance to the target. This preserves the systematic exploration behaviour of DFS while introducing a lightweight notion of heuristic guidance, biasing the search towards regions expected to be closer to the Orb.

#### 1.2.2 Adaptive Heuristic Search

While Greedy DFS uses the heuristic estimate provided by the environment, those estimates remain fixed throughout the search. However, as the agent explores, it acquires additional information about which paths prove less promising than originally expected. This motivated the implementation of **Adaptive Heuristic Search**, which progressively updates the heuristic estimates of visited nodes based on the experience gained during exploration. By increasing the estimated cost of nodes that repeatedly lead to unproductive searches, the algorithm gradually learns to deprioritise them in future decisions. This represents the first stage of the investigation in which the search policy adapts as new information is acquired, rather than relying solely on the heuristic available at the start of the search.

A simplified view of this update is

$$
h_{\text{learned}}(v)
\leftarrow
\max\left(
h_{\text{learned}}(v),
1 + \min_{n \in N(v)} h_{\text{learned}}(n)
\right),
$$

where $N(v)$ is the set of visible neighbours of node $(v)$. If all neighbouring options appear worse than expected, the current node's learned heuristic is raised, making it less attractive in future decisions.

## 2. Classical Heuristic Search

With the knowledge gained from incorporating heuristic guidance into systematic exploration, the next question became whether there existed established search algorithms for related problems whose underlying principles could be adapted to the online exploration setting.

A natural family of candidates was classical heuristic search, where movement decisions are guided by an evaluation function

$$
f(n)=g(n)+h(n),
$$

combining the accumulated path cost $g(n)$ with a heuristic estimate $h(n)$ of the remaining cost to the goal.

The following investigation therefore considers several heuristic search algorithms built around this evaluation function, identifying which of their underlying principles remain applicable to online exploration and how they influenced the subsequent development of the final exploration strategy.

### 2.1 A*

The first algorithm considered was A*, owing to its widespread use in heuristic graph search. By combining the accumulated path cost with an estimate of the remaining distance to the goal,

$$
f(n)=g(n)+h(n),
$$

A* naturally favours paths that appear most promising.

A weighted variant was also considered, increasing the influence of the heuristic estimate to encourage more direct movement towards the Orb. While A* itself assumes complete knowledge of the search graph and therefore cannot be applied directly to online exploration, it demonstrated that combining path cost with heuristic evaluation provides a powerful framework for guiding search decisions.

### 2.2 Iterative Deepening A*

A plausible extension of A* was Iterative Deepening A* (IDA*), which applies the same evaluation function while performing a sequence of increasingly deeper heuristic-guided searches. This initially appeared attractive for the Orb exploration problem, as it combined heuristic guidance with an iterative search process that seemed naturally suited to an online environment.

In practice, however, the repeated iterations proved to be a poor match for exploration. Since every iteration required revisiting previously explored regions, the movement cost increased rapidly as the search progressed. Rather than reducing exploration effort, the repeated searches compounded it, making IDA* considerably less effective in an online setting. This ultimately demonstrated that iterative search strategies are fundamentally ill-suited to problems where every search action corresponds to physical movement.

### 2.3 Real-Time A*

Another variant considered was Real-Time A* (RTA*), which is specifically designed for settings where decisions must be made incrementally. Rather than planning an entire path before moving, RTA* repeatedly evaluates the immediate neighbours of the current node and commits to the action

$$
a^*=\arg\min_{n\in N(v)} f(n),
$$

where $N(v)$ denotes the set of neighbouring nodes and $f(n)=g(n)+h(n)$ is the heuristic evaluation function.

This made RTA* a natural candidate for the Orb exploration problem, as it aligned well with the online nature of exploration. However, its forward-only behaviour also proved to be a limitation. Once the agent committed to a direction, it was reluctant to backtrack and reconsider earlier decisions, even when subsequent exploration revealed a more promising alternative. This highlighted an important design requirement for the final algorithm: an effective online search strategy should be curious enough to explore forward, yet willing to backtrack when doing so is likely to improve future exploration.

## 3. Frontier-Based Exploration

Whilst the classical heuristic search algorithms proved highly effective at guiding the agent towards the Orb, this strong goal-oriented behaviour often came at the expense of exploration. The evaluation function rewarded progress towards the target, leaving little incentive to investigate regions that did not appear to provide an immediate benefit.

This naturally suggested a refinement to the evaluation function. Rather than assigning value solely to reaching the Orb, the evaluation function should also assign value to exploring the unknown environment, allowing the agent to deviate from the most direct route whenever doing so was expected to reveal useful new information. In other words, exploration itself should become part of the search heuristic.

One promising family of algorithms that embodies this idea is frontier-based exploration, where movement decisions are guided not only by progress towards the goal, but also by the expected utility of exploring the boundary between the known and unknown graph.

### 3.1 Frontier Utility Search

The most basic form of frontier-based exploration is Frontier Utility Search, in which the agent assigns value to frontier nodes—the boundary between the explored and unexplored graph—as candidate exploration targets. Since every newly discovered region of the graph must first be reached through a frontier, these nodes provide a natural representation of future exploration opportunities.

This extends the classical heuristic evaluation function to

$$
f(n)=g(n)+h(n)+u(n),
$$

where $u(n)$ represents the utility associated with travelling towards a frontier. The search therefore retains the overall objective of reaching the Orb while introducing the intermediate objective of selecting the frontier that is most worthwhile to explore next.

### 3.2 Replanning Frontier Search

While Frontier Utility Search showed considerable promise during benchmarking, it was observed to become overly committed to the frontier initially selected for exploration. As the agent uncovered new regions of the graph, more attractive frontier targets frequently emerged, yet the original decision was often retained for longer than was desirable.

This motivated Replanning Frontier Search, in which the frontier selection process is repeated after each exploration step rather than being fixed in advance. By continually re-evaluating the available frontier nodes as new information becomes available, the agent remains responsive to changes in the explored graph and can redirect its exploration whenever a more promising opportunity arises.

### 3.3 Gradient Frontier Utility Search

Even with the increased responsiveness introduced by Replanning Frontier Search, a further question remained: when should exploration outweigh direct progress towards the Orb? Since every movement carries a cost, the agent must decide whether travelling to a frontier is justified by the progress it is expected to make towards the Orb.

One variant considered during the investigation was Gradient Frontier Utility Search, a modified frontier algorithm that biases frontier selection towards candidates producing the greatest reduction in the estimated distance to the Orb. This can be viewed as redefining the frontier utility term as

$$
u(n)
=
-\lambda\left(h(v)-h(n)\right),
$$

which is substituted into the frontier evaluation function introduced previously.

where v is the current node, n is the frontier candidate, h(v) and h(n) denote the estimated Orb distances from the current node and candidate frontier respectively, and \lambda>0 is a tunable parameter controlling how strongly the search rewards movement down the Orb-distance gradient. This modified utility is then incorporated into the frontier evaluation function, thereby assigning higher priority to frontiers that offer a large reduction in estimated Orb distance.

### 3.4 Coverage-Biased Frontier Utility Search

Another interpretation of the replanning frontier framework was that the utility term itself should be redefined. Rather than rewarding frontiers that produced the greatest reduction in estimated Orb distance, as in the Gradient variant, the utility should instead reward frontiers expected to reveal the greatest amount of previously unseen environment. This led to the development of Coverage-Biased Frontier Utility Search, whose evaluation function is

$$
f(n)=g(n)+h(n)+u(n)+\lambda c(n),
$$

where $c(n)$ is a lightweight estimate of the expected coverage associated with a frontier and $\lambda$ is a tunable parameter controlling the influence of this additional term.

Both the Gradient and Coverage variants produced competitive benchmark results over a range of values of $\lambda$. However, the coverage-based formulation consistently achieved the strongest overall performance, with the best results obtained for $\lambda=2$. Beyond its empirical performance, the coverage heuristic was ultimately preferred because it measures the quantity most closely aligned with the objective of efficient online graph exploration. Rather than indirectly encouraging exploration through progress towards the Orb, it directly estimates the expected amount of new information gained by exploring a frontier, thereby enabling more informed frontier selection throughout the exploration process.

## Summary

The investigation illustrates a progressive evolution of the search policy, beginning with systematic exploration, incorporating heuristic guidance from classical search, extending the evaluation function to explicitly value exploration, and finally introducing a coverage-based measure of expected information gain. Each refinement addressed a limitation identified in the preceding algorithm, ultimately leading to Coverage-Biased Frontier Utility Search, which provided the strongest balance between goal-directed search and efficient exploration, and was therefore selected as the final exploration algorithm.