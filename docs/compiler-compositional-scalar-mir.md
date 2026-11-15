# Compositional scalar source to verified MIR

## Scope and finish line

This increment extends the production
`gravity.compiler.application/current-source-to-verified-mir` entrypoint to
ordinary hosted, safe JVM modules containing first-order, nonrecursive scalar
functions, direct calls, eager computed `let` bindings, checked I64 addition,
and Boolean conditionals. The input remains an actual `.gravity` or `.qst`
source path and explicit policy. The existing Uniform C6-C10 and C11 owners
must construct and freshly verify the actual source-derived products.

Completion requires reference execution of the returned multi-function MIR,
including checked overflow before a false conditional can return another
value. Source artifacts remain genuinely `:jvm`-targeted. JavaScript admission
and emission, public target dispatch, self-hosting, seed retirement, broader
arithmetic, and release work remain separate.

Governing contracts are D1/D2/D3/D8/D9, L2/L3/L5/L6, C5-C11/C15 and SAFE9.
Host code transports current source, authenticated bindings and identities;
Uniform owns semantic checks, Core construction, MIR construction and
verification. Historical source-interface pins are not renewed by this work.

## Starting state and ownership

- Latest main observed before implementation:
  `d8a296e3d8c3befe6c5a735995fde8cac89047e5`.
- Prerequisite PR #1429 was open, no longer draft, at
  `7209382b0282ec423ff102bcc67ad4859fd5a7b0`.
- That prerequisite candidate already composes the observed main.
- This isolated successor branch is `codex/compositional-scalar-mir` and
  starts from that exact prerequisite candidate. It does not claim that
  #1429 was already on main.

Connected changed-file checks found no overlapping owned implementation
paths in #1419, #916, #911, #1369, #1372, #1197, #1207, #1420 or #1438.
The six active workstream records also have no intersecting owned paths.
#1429 is the explicit prerequisite. #1420 owns a SAFE9 documentation change;
this increment reads that governing contract without editing it.

## Reproduced first rejection

The following exact ordinary source was written to
`/workspace/scratch/f97e4bae66e6/evidence/scalar-functions.gravity`:

```clojure
(ns acceptance.scalar
  (:profile :hosted)
  (:target :jvm)
  (:effects #{:error/raise})
  (:capabilities #{})
  (:safety :safe)
  (:exports [score]))

(defn add [left :- I64 right :- I64]
  (+ left right))

(defn score [choose-sum :- Bool left :- I64 right :- I64]
  (let [sum (add left right)]
    (if choose-sum
      sum
      left)))
```

On base `7209382b0282ec423ff102bcc67ad4859fd5a7b0`, the production API
was invoked with that absolute path and this policy:

```clojure
{:profile :hosted :target :jvm :effects #{:error/raise}
 :capabilities #{} :safety :safe}
```

Command, run from the untouched baseline checkout:

```sh
clojure -J-Xmx2g -M /workspace/scratch/f97e4bae66e6/evidence/probe-scalar.clj
```

The probe invokes only the production API and catches its owned diagnostic
for recording. It returned `L3-UNKNOWN-ALIAS`, message
`stage2 executable requires a main function`, with `:entrypoint main` and
the exact input path in `:source-span`. Its remediation was
`Add (defn main [] ...).` This identifies the executable-only source-plan
handoff as the first observed blocker, before compositional Core and MIR.
The probe process exited 0 because it recorded the exception; this is a
compiler rejection, not an accepted compilation.

Evidence log: `evidence/baseline-scalar.log` in the development workspace.

## Verification checkpoints

The required original application selector was started on the same untouched
base before implementation:

```sh
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.compiler.application-test \
  --exact current-source-to-verified-mir --progress
```

At this initial checkpoint it is still running. Its pending result is not
passing evidence. Implementation takes place in a separate checkout so it
cannot change the selected source while that baseline executes.

The new acceptance selector will be:

```sh
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.compiler.application-test \
  --exact scalar-functions-and-control-flow --progress
```

No implementation acceptance or completed target compilation is claimed by
this initial checkpoint. Later checkpoints record actual terminal results,
candidate identities, remaining failed handoffs, and the corresponding
source and owner identities.
