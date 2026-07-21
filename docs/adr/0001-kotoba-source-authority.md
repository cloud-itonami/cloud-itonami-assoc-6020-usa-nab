# ADR 0001: Kotoba is the NAB catalog source authority

- Status: Accepted
- Date: 2026-07-21

## Decision

`src/association_facts.kotoba` is the sole production source. The Catechism
retains year-only revision `2014` and no fabricated establishment date; the
profile retains year-only founding `1923` and no revision. Indexed access
preserves political-advertising, disclosure, and governance. Unknown values and
indexes return zero or typed option-none; no effects are declared.

CI executes reference semantics, restricted JavaScript, instantiated typed
WebAssembly, and production source-authority checks. Clojure and the JVM are
compiler/test hosts only.

## Consequences

- Establishment, revision, and absent date states remain distinct.
- Multi-topic entries remain complete without host sets.
