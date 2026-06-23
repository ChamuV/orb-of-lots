# *Orb of Lots*

Autonomous search agent for the *Orb of Lots* Software Design & Programming coursework.

## Project Overview

This repository contains my individual solution to the *Orb of Lots* coursework. The challenge is to design an autonomous agent capable of navigating an initially unknown cavern to locate a hidden Orb and then escaping the cavern while managing limited resources.

The current stage of the project focuses on the exploration phase of the challenge: efficiently searching an unknown graph to locate the Orb using custom search heuristics and graph algorithms. As development progresses, the repository will also include the escape phase together with benchmarking, testing, and documentation of the design decisions behind the final implementation.

The project emphasises clean software engineering practices, maintainable Java code, reproducible testing, and well-documented algorithm design.

---

## Build and Run

Run the tests:

```bash
./gradlew clean test
```

Run the graphical version:

```bash
./gradlew :temple:run -PchooseMain=main.GUImain
```

Run the text version:

```bash
./gradlew :temple:run -PchooseMain=main.TXTmain
```

---

## Repository Structure

```text
src/
├── main/
│   └── java/
│       └── student/       # Main student implementation
├── test/
│   └── java/              # Unit tests

docs/                      # Additional project documentation
README.md
LICENSE
```

---

## Documentation

```md
Additional documentation is available in the `docs/` directory.

- `algorithms.md` — exploration and escape algorithms
- `design.md` — key design decisions and implementation rationale
- `testing.md` — testing strategy and unit tests
- `benchmarks.md` — performance comparisons between candidate algorithms
```

---

## Testing

Unit tests are located in:

```text
src/test/java
```

The project includes unit tests for the core helper methods and algorithmic components, together with reproducible benchmark testing using fixed random seeds.

---

## Acknowledgements

This coursework is based on the *Orb of Lots* framework originally developed by Eric Perdew, Ryan Pindulic, and Ethan Cecchetti of the Department of Computer Science at Cornell University. This repository contains my individual implementation developed for the Software Design & Programming coursework.

---

## License

This project is released under the MIT License. See the LICENSE file for details.

---

## Author

- Chamundeshwari Rajputri Vadamalai