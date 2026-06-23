# C6 - AST and Core Lowering Design

Sequence: 85
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Core lowering converts expanded, resolved syntax into Gravity's small core AST.
Surface conveniences, macro output, facet syntax, declarations, and sugar either
lower to core forms, produce declared primitives, or cross a documented domain
IR boundary.

Lowering preserves evaluation order, source provenance, generated origins,
metadata, namespace context, profile context, and facts needed by type, effect,
safety, MIR, and diagnostic stages.

## Requirements

- Every executable surface form must lower to an `L2` core form, declared
  primitive, or documented domain IR boundary.
- Core forms are `quote`, `if`, `do`, `let`, `fn`, `loop`, `recur`, `def`,
  `var`, `set!`, `try`, `throw`, and `match`, plus literal/data constructors
  owned by the core language.
- Lowering must preserve left-to-right evaluation where `L2` requires it.
- Introduced core forms must have generated-origin links to the source or macro
  form that caused them.
- Lowering must not erase declared effects, capabilities, unsafe metadata,
  profile clauses, allocation behavior, ownership hints, or proof obligations.
- A lowering that enters a domain IR must emit a semantic anchor back to source
  syntax and later typed core or MIR.
- Core AST artifacts must be verified before type checking.

## Dependencies

- `L2` defines core forms and evaluation semantics.
- `L7` defines `match` semantics and decision-tree obligations.
- `L9` defines `try`, `throw`, panic, and error effects.
- `C3` provides syntax object origin and metadata.
- `C4` produces expanded syntax.
- `C5` provides resolved bindings and namespace artifacts.
- `C7`, `C8`, `C9`, and `C10` consume core AST for type, effect, ownership, and
  safety analysis.
- `C12` defines domain IR boundaries.

## Outputs and Artifacts

- Core AST module.
- Surface-to-core map.
- Desugaring trace.
- Evaluation-order record.
- Domain-boundary records.
- Core verifier report.
- Core lowering diagnostics.

## Core Node Shape

```clojure
{:artifact :gravity/core-node
 :node-id core-node-hash
 :form :if
 :children {:condition cond-node
            :then then-node
            :else else-node}
 :source {:syntax-id syntax-id
          :span source-span
          :origin-chain origin-chain}
 :binding-context scope-id
 :profile :native
 :metadata {}
 :facts {:resolved-bindings binding-map-hash}
 :generated? true}
```

Typed, effect, ownership, and safety facts are attached by later stages. Core
lowering preserves seed facts and leaves later facts empty unless the source form
declares them syntactically.

## Desugaring Trace

```clojure
{:surface-syntax syntax-id
 :surface-kind :when
 :core-root core-node-id
 :introduced-forms [:if :do :quote]
 :preserved #{:source-spans :metadata :profile :capabilities}
 :introduced-origin [{:core-node core-node-id
                      :reason :surface-desugar
                      :from syntax-id}]
 :evaluation-order [:condition :body]
 :diagnostics []}
```

Trace records are required for diagnostics and for macro-expanded code. A user
should be able to see whether an error belongs to their source, macro output, or
an introduced core primitive.

## Lowering Rules

Representative rules:

- `defn` lowers to `def` of an `fn` value with declared type, effects,
  metadata, and export facts preserved.
- `when` lowers to `if` with `nil` else branch.
- `cond` lowers to nested `if` forms preserving test order.
- `case` lowers to `match` or a profile-specific decision primitive only after
  equivalence facts are recorded.
- `with-open` lowers to `try` plus resource cleanup with ownership and effect
  facts preserved.
- `Result` and `Option` helpers lower to constructors plus `match` where
  appropriate.
- Facet forms lower either to core forms or to a declared domain boundary.

Rules are versioned. Changing a lowering rule invalidates downstream typed core,
MIR, diagnostics, and conformance fixtures for affected syntax.

## Evaluation Order

The lowering pass emits evaluation-order records for:

- function arguments,
- binding initializers,
- `do` bodies,
- conditionals,
- match scrutinee, guards, and branches,
- resource cleanup,
- error handlers,
- effectful macro-generated forms.

Optimizers may later reorder pure expressions only after effect checking and
proof that diagnostics and semantics are preserved.

## Domain IR Boundaries

Some source forms do not lower immediately to ordinary core expressions:

- elementary math subgraphs may later enter EFIR,
- schemas may enter schema IR,
- workflows may enter workflow graph IR,
- hardware forms may enter HDL state-machine IR,
- AI agents may enter agent/tool IR.

The boundary record includes source syntax, owner document, required checker,
semantic anchor, profile, target, effects, capabilities, and fallback behavior.

## Core Verification

The core verifier checks:

- every node has a valid form kind,
- every child reference exists,
- source and generated origins are valid,
- binding references point to resolver outputs,
- evaluation-order metadata is present for effect-sensitive forms,
- profile and target annotations are structurally valid,
- domain boundary records have an owner and semantic anchor,
- no surface-only form remains unless accepted as a domain boundary.

Verifier failures stop type checking.

## Diagnostics

Core lowering diagnostics use `C6` identifiers:

- `C6-LOWERING-GAP` for surface forms that cannot lower.
- `C6-CORE-SHAPE` for malformed core nodes.
- `C6-EVAL-ORDER` for missing or changed evaluation-order facts.
- `C6-ORIGIN` for introduced forms without origin links.
- `C6-EFFECT-DROP` for lost effect or capability declarations.
- `C6-UNSAFE-DROP` for unsafe metadata lost during lowering.
- `C6-DOMAIN-BOUNDARY` for malformed domain IR boundary records.
- `C6-VERIFY` for core verifier failures.

Diagnostics must include syntax id, core node id when available, source span,
generated-origin chain, lowering rule, profile, target, and remediation.

## Rejected Designs

Gravity rejects lowering by textual rewrite.

Gravity rejects surface forms that silently bypass core or a declared domain IR.

Gravity rejects introduced core forms without source provenance.

Gravity rejects lowering that erases effects, capabilities, profile facts, or
unsafe metadata.

Gravity rejects backend-specific core nodes outside documented domain
boundaries.

## Conformance Criteria

A conforming lowering implementation must demonstrate:

- lowering for every standard surface form,
- source-to-core maps for handwritten and macro-generated forms,
- evaluation-order preservation for effectful examples,
- `match`, `try`, `throw`, `loop`, and `recur` core node verification,
- rejection of unknown surface forms,
- domain boundary artifacts for representative facets,
- diagnostics for lost metadata and malformed core,
- versioned lowering rule invalidation.
