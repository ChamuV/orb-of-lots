# Escape Phase (Group Version Only)

## Overview

After picking up the Orb, the walls of the cavern shift and a new layout is generated.

In addition, piles of gold fall onto the ground.

Underneath the Orb is a map revealing the entire cavern, so the full graph is now known to the explorer.

However, the shifting walls destabilise the cavern and begin a countdown. Once time expires, the ceiling collapses and Jeremy is crushed.

Picking up the Orb also activates the cavern's traps and puzzles, causing different edges of the graph to have different traversal costs.

---

## Objective

The primary objective is to reach the cavern entrance before time runs out.

A secondary objective is to collect gold while escaping.

---

## Scoring

The final score depends on:

1. The amount of gold collected during the escape phase.
2. The score achieved during the exploration phase.

The final score is calculated as:

```text
Final Score = Exploration Score × Gold Collected
```

---

## Implementation

Implement:

```java
public void escape(EscapeState state)
```

inside:

```java
student.Explorer
```

---

## Success Condition

To escape successfully, return from the method while standing on the entrance tile.

Returning while standing on any other tile causes the game to end immediately.

Failing to return before time runs out also results in failure and a score of zero.

---

## Time

Time is not measured using CPU execution time.

Instead, time corresponds to movement through the graph.

The amount of time remaining decreases whenever the explorer moves.

You may spend as long as you wish computing a route.

The specification guarantees that there is always enough time to escape if the shortest path to the exit is followed.

---

## Gold Collection

Different tiles may contain different amounts of gold.

Collecting gold from the tile currently occupied by the explorer takes no time.

Gold can be collected using:

```java
void pickUpGold()
```

This method fails if:

- no gold is present on the tile, or
- the gold has already been collected.

---

## EscapeState API

### Current Node

```java
Node getCurrentNode()
```

Returns the node corresponding to the explorer's current location.

---

### Exit Node

```java
Node getExit()
```

Returns the node corresponding to the cavern exit.

---

### All Vertices

```java
Collection<Node> getVertices()
```

Returns all traversable nodes in the graph.

---

### Remaining Time

```java
int getTimeRemaining()
```

Returns the amount of time remaining before the cavern collapses.

---

### Movement

```java
void moveTo(Node n)
```

Moves the explorer to an adjacent node.

This operation decreases the remaining time.

The move fails if the supplied node is not adjacent to the current location.

---

### Collect Gold

```java
void pickUpGold()
```

Collects all gold on the current tile.

---

## Additional Classes

The classes:

- `Node`
- `Edge`

contain additional methods that may be useful when implementing a solution.

Consult the API documentation for details.

---

## Suggested Development Strategy

A sensible first implementation is:

1. Compute a path that always reaches the exit.
2. Ensure the explorer never runs out of time.
3. Once correctness is guaranteed, introduce gold collection.
4. Optimise the route while maintaining a guaranteed escape.

---

## Important Note

The most important requirement is:

> Always escape successfully.

Any optimisation that risks failing to reach the exit before time expires should be avoided.