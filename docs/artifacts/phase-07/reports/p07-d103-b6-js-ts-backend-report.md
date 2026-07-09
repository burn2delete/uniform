# P07-D103 B6 JavaScript / TypeScript Backend Proof Report

Date: 2026-06-29
Task: `P07-D103`
Status: complete (stage0 B6 JavaScript / TypeScript backend document coverage)

## Governing Document Read

- `docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b6-*.gravity`
- `docs/artifacts/phase-07/backend/stage0-p07-d103-b6-js-ts-backend-proof.edn`

The `backend-b6-js-ts-document` command emits
`:gravity/stage0-b6-js-ts-backend-document-artifact` from the current P07-T03
hosted lowering artifact. It records B6 runtime and module target pinning,
JavaScript ESM output, TypeScript declarations, source maps, package metadata,
value/type representations, host-global and package capability manifests,
async effect boundaries, nullish and exception translation, numeric
representations, dynamic-code and prototype rejection policy, UI/component
metadata, B6 diagnostics, document-specific results, and capability-based
proof.

## Validation

```text
clojure -M:gravity backend-b6-js-ts-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-b6-js-ts-backend-document-artifact,
 :task "P07-D103",
 :artifact-id "sha256:7e92739c88869742e3ab926322236eeb154a9263489d05e3f91d5e42fbf2c0fb",
 :document-set ["B6"],
 :diagnostics 11,
 :rejected-designs 5,
 :conformance-criteria 11,
 :javascript-structural true,
 :typescript-structural true,
 :source-map-structural true,
 :package-json-structural true,
 :node-proof :requires-proof-command,
 :tsc-proof :requires-proof-command,
 :proof :complete}
```

JavaScript module hash:

```text
sha256:f1756942748d60c03be8162234433db6410b6406904ba1779850a796be7f1722
```

TypeScript declaration hash:

```text
sha256:cfee7a641df17bebfae23e63be5274d10c1e619eb6086e4088667e1779a4ddfc
```

Source map hash:

```text
sha256:412a7fe7d49915abb9bff5c2819aabc33fc391ce694c9d36fa9bf9750007afbd
```

Package metadata hash:

```text
sha256:83e85c844a757e1e281d5b44381d370ee2cbd864b0dbdf46a5fafd9ccf7b332b
```

```text
clojure -M -e <extract B6 JS/TS/package/source-map files>
{:dir "/tmp/gravity-p07-b6-js-ts",
 :files ("gravity-p07-b6-js-ts" "gravity-stage0.d.ts" "gravity-stage0.mjs" "gravity-stage0.mjs.map" "package.json"),
 :js-structural true,
 :ts-structural true}
```

```text
node --check /tmp/gravity-p07-b6-js-ts/gravity-stage0.mjs
passed
```

```text
node -e <dynamic import B6 module and execute boundary functions>
{"entry":"7","option":"none","promise":"ok","number":1.5,"packed":"1,2,3"}
```

```text
node -e <parse B6 package.json and source map JSON>
package.json:module
gravity-stage0.mjs.map:gravity-stage0.mjs
```

```text
tsc --version
zsh:1: command not found: tsc
```

The TypeScript declaration artifact is structurally validated by the Clojure
proof and recorded for an external TypeScript compiler proof when `tsc` is
available.

```text
clojure -M:test
Ran 82 tests containing 4705 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-07 backend proof EDN parse>
{:parsed 12,
 :tasks [:P07-D098 :P07-D099 :P07-D100 :P07-D101 :P07-D102 :P07-D103 :P07-T01 :P07-T02 :P07-T03 :P07-T04 :P07-T05 :P07-T06],
 :statuses [:complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete]}
```

```text
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

```text
git diff --check
passed
```

## Rejected Diagnostics

The rejected fixture suite covers all B6 JavaScript / TypeScript backend
diagnostic IDs:

- `B6-TARGET`
- `B6-GLOBAL`
- `B6-IMPORT`
- `B6-NULLISH`
- `B6-EXCEPTION`
- `B6-NUMERIC`
- `B6-EVAL`
- `B6-PROTOTYPE`
- `B6-ASYNC`
- `B6-UI`
- `B6-MANIFEST`

## Proof Records

- `docs/artifacts/phase-07/backend/stage0-p07-d103-b6-js-ts-backend-proof.edn`

## Remaining Limits

This completes `P07-D103` for deterministic Clojure stage0 coverage of the B6
JavaScript / TypeScript backend design contract. The emitted JavaScript module
passes Node syntax and dynamic import execution, and package/source-map JSON
parse successfully. The current environment does not provide `tsc`, so this
does not claim TypeScript compiler validation, browser bundler execution, edge
runtime execution, React Native/mobile JS execution, package publication, or
full Phase 07 completion.
