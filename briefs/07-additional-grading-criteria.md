# Additional Grading Criteria

## Core Assessment Principles

In addition to the formal marking scheme, the coursework will be assessed according to:

- Ability to fulfil the requirements.
- Simplicity of the solution.
- Clarity of the implementation.
- Generality of the design.
- Appropriate comments and JavaDoc.
- Compliance with software engineering and version control best practices.

---

# Code Quality Expectations

The implementation should demonstrate:

## Simplicity

Solutions should avoid unnecessary complexity.

Prefer:

- clear logic,
- understandable algorithms,
- maintainable code,

over unnecessarily clever implementations.

---

## Clarity

Code should be easy to read and understand.

This includes:

- meaningful variable names,
- descriptive method names,
- sensible class structure,
- consistent formatting.

---

## Generality

Solutions should not be tailored to specific maps.

Algorithms should operate correctly regardless of:

- graph structure,
- map size,
- random seed.

---

## Documentation

Documentation should include:

- concise comments,
- useful JavaDoc,
- explanation of non-obvious design decisions.

Comments should explain:

```text
Why
```

rather than merely repeating

```text
What
```

the code already says.

---

# Version Control Expectations

Good version control practices are part of the assessment.

Recommended practices include:

## Commit Frequently

Prefer:

```text
Small, focused commits
```

rather than:

```text
One large commit at the end
```

---

## Descriptive Commit Messages

Good examples:

```text
Implement DFS exploration

Add visited-node tracking

Add heuristic neighbour ordering

Create exploration integration tests
```

Poor examples:

```text
Update

Fix stuff

Changes
```

---

## Repository Contents

Commit:

- source code,
- documentation,
- tests.

Do not commit:

- compiled binaries,
- generated files,
- `.class` files.

---

# Algorithm Priorities

The specification explicitly states:

> The vast majority of marks come from a solution that always finds the Orb and always escapes successfully.

Therefore the priority order is:

## 1. Correctness

The solution must always:

- find the Orb,
- escape successfully (group version).

A correct but unoptimised solution will generally outperform an optimised solution that occasionally fails.

---

## 2. Optimisation

Once correctness is guaranteed:

### Exploration Phase

Optimise:

- number of steps taken,
- bonus multiplier retained.

### Escape Phase

Optimise:

- gold collected,
- final score.

---

# Runtime Constraints

CPU execution time does not affect the game score.

However, grading infrastructure imposes a timeout.

## Requirement

When running in headless mode:

```text
A single map should complete within approximately 10 seconds.
```

---

## Consequences

Solutions that significantly exceed this limit may be treated as incomplete.

Such solutions are likely to receive low marks regardless of score quality.

---

# Forbidden Techniques

## Java Reflection

Use of Java Reflection is strictly prohibited.

Examples include:

- runtime inspection of hidden classes,
- bypassing intended APIs,
- accessing internal implementation details.

Reflection usage will result in significant penalties.

---

# Practical Development Strategy

## Phase 1

Guarantee correctness.

Goal:

```text
Always find the Orb.
```

---

## Phase 2

Improve exploration efficiency.

Goal:

```text
Reduce unnecessary movement.
```

---

## Phase 3

Improve documentation and testing.

Goal:

```text
Maximise documentation, testing, and style marks.
```

---

## Phase 4 (Optional Extension)

Implement escape-phase optimisation.

Goal:

```text
Collect additional gold while maintaining guaranteed escape.
```

---

# Key Takeaway

The specification strongly prioritises:

```text
Correctness > Optimisation
```

A solution that always succeeds is more valuable than a highly optimised solution that occasionally fails.