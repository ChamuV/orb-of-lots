# Exploration Phase

## Objective

Navigate Jeremy Hunt through the cavern and locate the Orb of Lots.

The cavern layout is initially unknown. At any point, only the current tile and adjacent tiles are visible.

The goal is to reach the Orb in as few steps as possible.

---

## ExplorationState API

### Current Location

```java
long getCurrentLocation()
```

Returns a unique identifier for the current tile.

### Distance To Target

```java
int getDistanceToTarget()
```

Returns the distance from the current location to the Orb, assuming there are no walls or obstacles.

### Neighbours

```java
Collection<NodeStatus> getNeighbours()
```

Returns information about all adjacent reachable tiles.

### Movement

```java
void moveTo(long id)
```

Moves the explorer to an adjacent tile.

---

## NodeStatus

Each NodeStatus contains:

- Node ID
- Distance from the Orb

---

## Requirements

- Implement `explore()` in `student.Explorer`.
- Return only when standing on the Orb.
- Returning elsewhere causes an exception.
- No step limit exists.
- Fewer steps receive a higher score.

---

## Suggested Approach

The brief suggests:

- Depth First Search (DFS)

Possible improvement:

- Use Orb distance as a heuristic.
- Explore neighbours with smaller distance values first.