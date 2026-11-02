# Bootstrap module literal boundary

## Contract and scope

This increment follows D1/D2/D3/D8/D9, L1/L2/L3, C1, BOOT1/BOOT2/BOOT8 and
TEST2. The current development-verification policy removes node, depth and
scalar quotas whose sole purpose is refusing otherwise valid input.

`gravity.bootstrap.module-literals/materialize!` owns the existing
temporary seed literal-data traversal and materialization. Its inputs are a
supplied diagnostic Var and an already parsed native form. Its output is
literal data, or the caller's unchanged BOOT2006 diagnostic for unsupported
literal shape. The helper imports no compiler stage. The Bootstrap wrapper
retains its existing one-argument API and diagnostic ownership.

Only the visited-node, nesting-depth and string-length quota checks are
removed. Unquoted symbols/calls, malformed quote shapes, unsupported literal
objects, declaration legality, duplicate declarations and authentic C5/source
admission remain governed by their existing checks. Quote returns inert data.
The helper does not confer source, Core, profile or execution authority.

## Development evidence

The clean control at scope commit
`f972898465065edb761e41a096ded3b84ada7ed3` uses actual main
`d84f2837fe4747ece773a47176f499c27e62f0b7`. Before and after the extraction,
the focused existing `compiler-module-literal-data-development-kernel` passes
one test and 21 assertions. Its three quota-only rejection assertions now
assert preserved valid data. Shape and duplicate-definition refusals remain.

The new standalone `gravity.bootstrap.module-literals-test` passes four tests
and 43 assertions. It covers scalar/collection values, inert quote identity,
the three former quota cases, real malformed literal shapes and unsupported
objects even beyond the former quotas, late diagnostic Var reads and complete
paired source compilation.
Standard Library regression passes six tests and 111 assertions against its
complete 90-function source kernel. Registration passes 36/1060 and language
boundary 6/11. Documentation, roadmap and governance checks pass.

The ordinary complete-source control captures seven records under
`/tmp/uniform-bootstrap-literals-evidence/`. Five unchanged records compare
equal in full and serialized form: native data, shape refusals, duplicate
declarations and two complete ordinary source plans/results/refusals. The two
long-string records intentionally change from quota refusal to compiled plan
and executed value. Their actual source paths, text hashes and contents stay
fixed. This is generated test source in retained temporary files, not an
authenticated source artifact.

Both co-canonical source files contain a complete module, literal declaration
and `main`. The unchanged small plans have source SHA256
`7d9868cf02029e3fa59efd8b6983edac53689f96fecfb824f9ea8154d8bf64d0` and plan
id `sha256:ab7f76f4675e1b60974c9fe5b567e1ed854888c635fb6f9360c48fee68fb7e6e`.
They return the complete nested literal including inert call data. Malformed
literal declarations reject BOOT2006 with `:unsupported-literal-data`. Both
65,537-character literal modules now compile and execute; each complete plan
id and source hash recomputes. This does not establish arbitrary host depth
or memory support.

The verifier compares the entire component against actual main after replacing
only the old literal helper with its compatibility wrapper. This preserves all
unmoved forms, declaration checks, authentic C5/source gates and map-order
handling. The complete Bootstrap file differs only by the helper require.
Primary catalog/count formulas, authority pins and CI policy do not change.
The helper is explicitly registered as master-coordinator/SH-01 standalone
support; this is no SH-01 advancement claim.

Execution uses the fixed `bootstrap/gravity/p15_s23/compiler.gravity` seed
whose SHA256 is
`57c13e44b46726a136ecd237dde5e85d86cd55b82bd8ac6033afce2fb87bf527`, plus
native Clojure runtime/component 00593. The current Gravity emitter/runtime
sources are not executed by these checks. Inherited compiler stamps remain
seed metadata and do not renew migration or source readiness.

The initial focused commands mistakenly selected `:test`, which owns only its
declared namespace set. The three selector attempts stopped before execution;
their logs are retained as `focused.log`, `focused-registered.log` and
`stdlib.log` under `/tmp/uniform-bootstrap-literals-`. The documented
development runner executes these standalone namespaces and passes. No runner
ownership or admission guard was changed.

Control scripts are `/tmp/uniform-bootstrap-literals-control.clj` and
`/tmp/uniform-bootstrap-literals-verify.clj`. Logs use
`/tmp/uniform-bootstrap-literals-`. Integration still requires contextual lint,
independent exact-source reviews, own CI and clean current-main preflight.

## Unsupported boundaries

This remains a Clojure seed helper, not a Gravity-authored replacement or a
complete Bootstrap namespace migration. It accepts already parsed native data;
it is not a public Reader or an authenticated source decoder. Host heap and
stack limits remain actual host restrictions, not compiler accounting. No
general profile, effect, capability, ownership or safety legality follows from
literal construction. No new native/backend, compiler admission, migration,
self-hosting, release, seed retirement or namespace-completion claim follows.

Development results do not confer integration or migration acceptance.

## Tooling main composition

The normal composition after source
`4464840f1a5256a7635cd24db798d147f1861694` uses actual Tooling main
`fd159988ab06ee0397505718e9bd96bbf8df8228`. Three genuine registration
conflicts are resolved by reconstructing five complete metadata files from
main and appending the existing literal helper association/test and require.
The helper, wrapper component, focused tests and original literal regression
remain byte-identical to446. All incoming Tooling source, wrapper, tests, three
Gravity repair files and evidence remain exact main. Catalog/count formulas,
authority pins and CI policy remain unchanged.

Fresh combined literal, Standard Library, Tooling and complete W5 regressions
pass 28/913. Ownership inventory passes 37/1076 and language boundary 6/11.
Five full/serialized record pairs remain exact against the clean f972 control;
both long-string modules still compile/execute, retain their actual source
hashes and recompute their full plan ids. The complete component and exact
single literal-helper require conserve against fd159. Current source review,
contextual lint, own CI and a clean integration preflight remain required;
earlier-head results confer no new-base integration credit.
