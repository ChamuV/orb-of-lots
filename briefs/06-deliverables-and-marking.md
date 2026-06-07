# Deliverables and Marking

## Version Control Requirements

Students are expected to use Git and GitHub throughout development.

Recommended workflow:

1. Clone starter code.
2. Create personal GitHub repository.
3. Commit changes regularly.
4. Push work to GitHub.
5. Pull final version back into Codio before submission.

Development should follow the standard Git workflow:

```bash
git add .
git commit -m "Meaningful commit message"
git push
```

Regular commits are encouraged throughout the project.

---

# Assessment Components

## 1. Explore Phase Implementation (60%)

Weighting:

```text
60%
```

Assessment focuses on:

- Correctness
- Reliability
- Optimisation
- Quality of exploration strategy

The implementation must:

- Always find the Orb.
- Correctly satisfy all exploration requirements.
- Minimise unnecessary movement where possible.

This is the largest component of the coursework.

---

## 2. Documentation (15%)

Weighting:

```text
15%
```

Documentation should clearly explain:

- Design decisions
- Algorithm choice
- Important implementation details
- Assumptions made
- Usage instructions where appropriate

Possible documentation locations:

- README.md
- JavaDoc comments
- Design notes

---

## 3. Testing (15%)

Weighting:

```text
15%
```

Testing should demonstrate that the solution behaves correctly.

Areas to test include:

- Graph traversal behaviour
- Edge cases
- Correct Orb detection
- Backtracking behaviour
- Reliability across multiple seeds

Both unit and integration testing should be considered where possible.

---

## 4. Development Style (10%)

Weighting:

```text
10%
```

Assessment likely includes:

- Code readability
- Naming conventions
- Modularity
- Maintainability
- Use of appropriate abstractions
- Consistent formatting

The solution should follow good software engineering practices.

---

# Penalty

## Java Reflection

Penalty:

```text
-10%
```

Use of Java Reflection is explicitly penalised.

Avoid:

- Reflection APIs
- Runtime inspection of hidden classes
- Attempts to bypass the intended interfaces

The coursework should be solved using only the provided public APIs.

---

# Assessment Summary

| Component | Weight |
|------------|---------:|
| Explore Phase Implementation | 60% |
| Documentation | 15% |
| Testing | 15% |
| Development Style | 10% |
| Reflection Penalty | -10% |

---

# Strategic Notes

The assessment is heavily weighted toward the exploration algorithm.

Priority order:

1. Correct exploration solution
2. Optimised exploration strategy
3. Comprehensive testing
4. Clear documentation
5. Clean software engineering practices

Since the individual coursework excludes the escape phase, nearly all technical effort should focus on the quality of the exploration implementation.