# SH-05 Macroexpander Fixtures

Every source case has a byte-identical co-canonical `.gravity` / `.qst` peer.
The physical path and extension are provenance; neither participates in the
semantic identity of the expanded syntax or expansion trace.

## Accepted pair

- `defn-basic`: the smallest bootstrap macro slice. The source form
  `(defn add-one [x] (+ x 1))` must expand to the ordinary Gravity form
  `(def add-one (fn [x] (+ x 1)))`. The expansion must preserve the input
  SH-04 syntax lineage, attach macro call, definition, and generated origins,
  retain metadata, and emit a deterministic trace.
- `defn-multiple`: two independent `defn` forms. The adapter must preserve
  order while producing two independently authenticated expansion runs.
- `legacy-external-macro`: a macro call outside the bounded SH-05 `defn`
  subset. Compatibility C4 must retain legacy routing without SH-05 credit.
- `legacy-local-defmacro`: a local `defmacro` and invocation. Compatibility C4
  must retain legacy routing without treating the local macro as Gravity-owned
  SH-05 behavior.

## Rejected scenario pairs

Each rejected fixture is executable scenario data read through the Gravity
reader. The dedicated SH-05 test applies the scenario to the macroexpander and
requires the listed structured C4 diagnostic with the actual fixture path.

| Base name | Expected rule | Rejection family |
| --- | --- | --- |
| `not-macro` | `C4-NOT-MACRO` | macro-position symbol is unavailable |
| `phase-mismatch` | `C4-NOT-MACRO` | runtime binding is requested during macro expansion |
| `return-non-syntax` | `C4-RETURN` | macro result is not syntax |
| `depth-limit` | `C4-DEPTH` | expansion exceeds configured depth |
| `size-limit` | `C4-SIZE` | expanded graph exceeds configured size |
| `build-effect-ungranted` | `C4-BUILD-EFFECT` | build authority is undeclared or ungranted |
| `hygiene-hidden-binding` | `C4-HYGIENE` | introduced identifier loses its hygiene mark |
| `capture-authority` | `C4-CAPTURE` | capture of an authority-bearing binding is denied |
| `generated-unsafe-missing-audit` | `C4-GENERATED-UNSAFE` | generated unsafe form lacks required metadata |
| `profile-illegal-output` | `C4-PROFILE` | generated form is illegal for the caller profile |
| `trace-replay-substitution` | `C4-TRACE` | replay input no longer matches the trace |
| `override-scalar` | `C4-RETURN` | request override is a scalar |
| `override-vector` | `C4-RETURN` | request override is a vector |
| `override-nil` | `C4-RETURN` | request override is nil-like |

The fixtures and dedicated namespace prove only the bounded `defn` expansion
surface. They do not claim general macro evaluation, target legality beyond the
early C4 checks, seed retirement, or release readiness.

The C4 catalog has no separate phase or capability diagnostic. Phase mismatch
is therefore contained by `C4-NOT-MACRO` with `L12-PHASE-CAPTURE` and
`SAFE12-PHASE` references. Missing compile-time authority is contained by
`C4-BUILD-EFFECT` with the missing declaration, capability, grant, and
`L15`/`SAFE10` references in structured facts.
