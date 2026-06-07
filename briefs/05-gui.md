# GUI

## Overview

When the program is run in GUI mode, a graphical interface is displayed that allows the explorer's actions to be visualised in real time.

This can be useful for:

- debugging,
- understanding algorithm behaviour,
- observing pathfinding decisions,
- reproducing issues on specific maps.

---

## Animation Behaviour

When running with the GUI enabled, calls to:

```java
moveTo(...)
```

are blocking operations.

This means:

1. The explorer begins moving.
2. The GUI animation plays.
3. The animation completes.
4. Control returns to the program.

Consequently, program execution pauses until the movement animation finishes.

---

## Performance Implications

GUI execution is generally slower than headless execution.

This slowdown is caused entirely by animation rendering and visualisation.

Algorithm performance is unaffected.

For large-scale testing and benchmarking, headless mode is preferred.

```bash
java main.TXTmain
```

For debugging and visual inspection, GUI mode is preferred.

```bash
java main.GUImain
```

---

# Speed Control

A speed slider is available on the right-hand side of the interface.

### Increasing Speed

- Animations complete more quickly.
- Runs finish faster.
- Useful for routine testing.

### Decreasing Speed

- Animations play more slowly.
- Easier to follow explorer decisions.
- Useful for debugging and understanding algorithm behaviour.

---

# Escape Phase Information

During the escape phase, the GUI displays a countdown timer.

The timer is shown as:

- a numerical value,
- a percentage of remaining time.

This indicates how many movement steps remain before the cavern collapses.

---

# Seed Management

A **Print Seed** button is available.

Pressing this button prints the current map seed to the console.

This is useful for:

- reproducing bugs,
- testing difficult maps,
- comparing algorithm changes on identical maps.

Example workflow:

1. Observe unexpected behaviour.
2. Click **Print Seed**.
3. Copy the seed.
4. Re-run the program using:

```bash
java main.GUImain -s <seed>
```

---

# Score Display

The GUI displays:

- bonus multiplier,
- coins collected,
- final score.

---

## Bonus Multiplier

The bonus multiplier begins at:

```text
1.3
```

and gradually decreases as additional steps are taken during the exploration phase.

Once exploration ends and the Orb is found, the multiplier becomes fixed.

---

## Coins

The coin count increases whenever gold is collected during the escape phase.

---

## Final Score

The final score is computed as:

```text
Final Score = Bonus Multiplier × Coins Collected
```

---

# Tile Inspection

Individual map tiles can be inspected by clicking on them.

Selecting a tile displays detailed information in the right-hand panel.

Available information includes:

- row coordinate,
- column coordinate,
- tile type,
- amount of gold present.

This feature is useful for:

- understanding map structure,
- verifying movement behaviour,
- debugging pathfinding logic.

---

# Recommended Usage

## During Development

Use GUI mode:

```bash
java main.GUImain
```

Purpose:

- visual debugging,
- understanding search behaviour,
- inspecting explorer decisions.

---

## During Benchmarking

Use headless mode:

```bash
java main.TXTmain -n 100
```

Purpose:

- evaluate average performance,
- compare algorithms,
- avoid animation overhead.

---

# Development Notes

The GUI is primarily a debugging and visualisation tool.

The most useful features during development are:

- speed slider,
- tile inspection,
- timer display,
- score display,
- print seed functionality.

For final algorithm evaluation, headless mode is generally preferable.