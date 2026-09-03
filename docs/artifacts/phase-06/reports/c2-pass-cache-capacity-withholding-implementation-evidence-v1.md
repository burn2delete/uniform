# C2 Pass-Cache Capacity Withholding Implementation Evidence V1

Status: candidate evidence checkpoint, not review or acceptance.

## Exact implementation identity

- Workstream: `c2-pass-cache-capacity-withholding-v1`
- Invariant family: `compiler/c2-pass-cache-capacity-withholding`
- Base: `e4b85b7a4f7bbf1e2f8287ec8052240ffa106f50`
- Implementation/source commit:
  `4ed05b21b590a93f64da6b00cca6ee6102b868cd`
- Implementation/source tree:
  `7242f96dae0259131a38656e0db6a7a0590e6c30`
- Integrated architecture terminal record:
  `contracts/workstream-records/terminal/c2-pass-cache-capacity-withholding-architecture-v1-b456d9b500402afca7fa11dd17ae9b3bab9a91604dced767be0ee85f950a3691.json`
- Terminal-record SHA-256:
  `b456d9b500402afca7fa11dd17ae9b3bab9a91604dced767be0ee85f950a3691`
- Architecture report SHA-256:
  `70c99c3b207ef8654b3cbfb752f1b2d88aec7c8b0249d5ca3f6c6349c09a2f42`
- Historical provenance SHA-256:
  `d5e639dc40d6a5a2555298606657b74b71bf3b9addf259ccb39021bc2e196006`

The later governance record owns the exact candidate commit and tree. This
report intentionally binds the source implementation it tested, so it has no
self-referential commit claim.

## Clean validation receipts

All successful implementation commands below ran on the named branch with
clean HEAD `4ed05b21b590a93f64da6b00cca6ee6102b868cd` and tree
`7242f96dae0259131a38656e0db6a7a0590e6c30`. The source checkpoint contains
the requested-hit identity repair and its tests; this report is a later
evidence-only commit and therefore names the exact source checkpoint rather
than making a self-referential claim.

1. Focused repair and capacity matrix:

   ```text
   /usr/bin/time -p clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.c2-pass-cache-test)) (let [c (ref clojure.test/*initial-report-counters*) vars [(var gravity.c2-pass-cache-test/generic-v2-adapter-same-key-concurrency-executes-one-c2-producer) (var gravity.c2-pass-cache-test/c2-semantic-validation-trace-separates-requested-and-recovery-roles) (var gravity.c2-pass-cache-test/requested-hit-binding-rejects-replacement-and-post-validation-mutation) (var gravity.c2-pass-cache-test/c2-envelope-capacity-is-closed-at-node-depth-and-byte-boundaries) (var gravity.c2-pass-cache-test/c2-capacity-retains-rejected-entry-and-direct-store-still-rejects) (var gravity.c2-pass-cache-test/c2-capacity-catches-propagate-unrelated-failures-identically)]] (binding [clojure.test/*report-counters* c] (clojure.test/test-vars vars)) (prn @c) (shutdown-agents) (System/exit (if (zero? (+ (:fail @c) (:error @c))) 0 1)))'
   {:test 6, :pass 133, :fail 0, :error 0}
   real 7.22
   user 9.43
   sys 0.51
   ```

2. Complete C2 cache namespace:

   ```text
   /usr/bin/time -p clojure -M:dev-test --namespace gravity.c2-pass-cache-test
   Ran 16 tests containing 231 assertions.
   0 failures, 0 errors.
   real 95.64
   user 111.15
   sys 2.34
   ```

3. Complete generic pass-cache namespace:

   ```text
   /usr/bin/time -p clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.pass-cache-test)) (let [r (clojure.test/run-tests (quote gravity.pass-cache-test))] (prn r) (shutdown-agents) (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
   Ran 32 tests containing 192 assertions.
   0 failures, 0 errors.
   real 14.61
   user 37.50
   sys 1.82
   ```

4. Authentic SH-03 own-source smoke:

   ```text
   /usr/bin/time -p clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.self-hosting.sh03-reader-adapter-test)) (let [c (ref clojure.test/*initial-report-counters*) vars [(var gravity.self-hosting.sh03-reader-adapter-test/sh03-public-reader-accepts-its-own-gravity-source)]] (binding [clojure.test/*report-counters* c] (clojure.test/test-vars vars)) (prn @c) (shutdown-agents) (System/exit (if (zero? (+ (:fail @c) (:error @c))) 0 1)))'
   {:test 1, :pass 8, :fail 0, :error 0}
   real 324.98
   user 393.24
   sys 6.47
   ```

5. Authentic paired route:

   ```text
   /usr/bin/time -p clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '<paired-route-form below>'
   {:paired-source-byte-identical true,
    :source-byte-count 257161,
    :source-sha256 "sha256:bd262c746ad88d9213e4b3160943fecde948c5ceee12a65dfe67ffb29d4e5b77",
    :complete-census {:canonical-nodes 8256942, :canonical-depth 24},
    :extensions [".gravity" ".qst"]}
   real 1308.12
   user 1468.60
   sys 116.46
   ```

6. Architecture, language-boundary, document, roadmap, governance, and diff
   gates:

   ```text
   clojure -M tools/validate_architecture_authority.clj --report docs/artifacts/phase-06/reports/c2-pass-cache-capacity-withholding-architecture-v1.md
   architecture authority validation passed

   clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
   Ran 6 tests containing 11 assertions.
   0 failures, 0 errors.

   clojure -M tools/validate_gravity_docs.clj
   validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders

   clojure -M tools/validate_full_language_roadmap.clj
   full-language roadmap validation passed

   clojure -M tools/validate_workstream_governance.clj
   validation passed: 147 workstreams

   git diff --check e4b85b7a4f7bbf1e2f8287ec8052240ffa106f50..4ed05b21b590a93f64da6b00cca6ee6102b868cd
   exit 0, no output
   ```

Read-only ancestry inspection found current authoritative `main` at
`f88c8094d4150bc5cd3488f598a3d8617e1b7018`, with the repaired source
checkpoint 10 commits ahead and 17 commits behind. The proportional preflight
is rerun after this report is committed. Any
`resolve_divergence_before_integration` result is an explicit later integration
blocker, not a reason to rewrite this candidate's exact base during freeze or
review.

The paired form copied `bootstrap/gravity/src/gravity/bootstrap/reader.gravity`
byte-for-byte to temporary `reader.gravity` and `reader.qst` paths. For each
extension it:

- invoked the private ordinary uncached C2 producer under the checked-in
  `with-target-reader-tripwires` boundary;
- measured the complete canonical node/depth census iteratively, following the
  encoder's metadata, map, set, vector, and sequence child rules without
  encoding a carrier or invocation token;
- invoked `compiler-c2-reader-file-artifact-cached` against a pre-created,
  extension-isolated temporary cache base under the same tripwires;
- asserted exact direct/withheld artifact equality and artifact-id equality;
- asserted C2 kind, `P06-D081`, `C2`, accepted SH-03 and authenticated-envelope
  status, Gravity owner, adapter v2, no target reread, and residual-host flags;
- asserted clean-miss capacity evidence, exact node maximum/observation,
  sanitized token absence, withheld/no-disk publication, reader execution, and
  no artifact reuse; and
- recursively counted regular files under the actual
  `.cpcache/compiler-pass/v2/{blobs,receipts,entries,staging}` root, requiring
  zero in every directory before deleting the temporary tree in `finally`.

This rerun deliberately does not rely on the earlier candidate's inventory
receipt. It corrects the review-time concern about a duplicated inventory path
by measuring the actual generic pass-cache v2 root on the repaired source
checkpoint. The source bytes, direct artifact identities, complete census, and
capacity threshold match the earlier run; the cache-withholding inventory
claim is newly reproduced for this candidate.

The normalized executable body of `<paired-route-form>` was:

```clojure
(require 'clojure.java.io 'gravity.bootstrap 'gravity.c2-pass-cache
         'gravity.digest 'gravity.self-hosting.sh03-reader-adapter-test)
(import '[java.nio.file Files LinkOption OpenOption StandardOpenOption])
(let [root (.toPath (clojure.java.io/file "."))
      relative (var-get (ns-resolve 'gravity.bootstrap
                                    'sh03-reader-source-relative-path))
      bytes (Files/readAllBytes
             (.normalize (.toAbsolutePath (.resolve root relative))))
      source-hash (str "sha256:" (gravity.digest/sha256-bytes-hex bytes))
      uncached (var-get (ns-resolve
                         'gravity.bootstrap
                         'compiler-c2-reader-file-artifact-uncached))
      cached gravity.bootstrap/compiler-c2-reader-file-artifact-cached
      tripwire (var-get
                (ns-resolve
                 'gravity.self-hosting.sh03-reader-adapter-test
                 'with-target-reader-tripwires))
      temporary (Files/createTempDirectory
                 "gravity-c2-capacity-paired-"
                 (make-array java.nio.file.attribute.FileAttribute 0))
      file-count (fn [path]
                   (if (Files/exists path (make-array LinkOption 0))
                     (with-open [walk (Files/walk
                                       path
                                       (make-array
                                        java.nio.file.FileVisitOption 0))]
                       (count
                        (filter #(Files/isRegularFile
                                  % (make-array LinkOption 0))
                                (vec (.toArray walk)))))
                     0))
      inventory (fn [base]
                  (let [store-root (-> base
                                       (.resolve ".cpcache")
                                       (.resolve "compiler-pass")
                                       (.resolve "v2"))]
                    (into (sorted-map)
                          (for [name ["blobs" "receipts" "entries"
                                      "staging"]]
                            [(keyword name)
                             (file-count (.resolve store-root name))]))))
      census (fn [artifact]
               (loop [stack [[artifact 0]], nodes 0, maximum-depth 0]
                 (if (empty? stack)
                   {:canonical-nodes nodes
                    :canonical-depth maximum-depth}
                   (let [[value depth] (peek stack)
                         stack (pop stack)
                         metadata? (and (instance? clojure.lang.IMeta value)
                                        (seq (meta value)))
                         children (cond
                                    metadata?
                                    [(meta value) (with-meta value nil)]
                                    (map? value) (mapcat identity value)
                                    (or (set? value) (vector? value)
                                        (seq? value)) value
                                    :else nil)]
                     (recur
                      (into stack
                            (map #(vector % (inc depth)) children))
                      (inc nodes) (max maximum-depth depth))))))
      delete-tree! (fn [path]
                     (when (Files/exists path (make-array LinkOption 0))
                       (with-open [walk (Files/walk
                                         path
                                         (make-array
                                          java.nio.file.FileVisitOption 0))]
                         (doseq [entry (reverse (vec (.toArray walk)))]
                           (Files/deleteIfExists entry)))))]
  (try
    (let [results
          (mapv
           (fn [extension]
             (let [source (.resolve temporary (str "reader" extension))
                   cache-base (.resolve temporary
                                        (str "cache-" (subs extension 1)))
                   _ (Files/write
                      source bytes
                      (into-array OpenOption
                                  [StandardOpenOption/CREATE_NEW
                                   StandardOpenOption/WRITE]))
                   _ (Files/createDirectory
                      cache-base
                      (make-array
                       java.nio.file.attribute.FileAttribute 0))
                   source-path (str source)
                   direct (tripwire source-path #(uncached source-path))
                   complete-census (census direct)
                   wrapped (tripwire
                            source-path
                            #(cached source-path (str cache-base)))
                   returned (:c2-reader-artifact wrapped)
                   evidence (:cache-evidence wrapped)
                   boundary (:gravity-reader-boundary direct)
                   observed
                   {:extension extension
                    :source-byte-count (alength bytes)
                    :source-sha256 source-hash
                    :direct-artifact-id (:artifact-id direct)
                    :withheld-artifact-id (:artifact-id returned)
                    :direct-equals-withheld (= direct returned)
                    :complete-census complete-census
                    :boundary
                    (select-keys
                     boundary
                     [:slice :owner :adapter-contract
                      :target-source-reread? :clojure-adapter-residual?
                      :self-hosted?])
                    :resolved-status
                    (get-in boundary [:resolved-reader-result :status])
                    :authenticated-envelope-status
                    (get-in boundary [:authenticated-envelope :status])
                    :task (:task direct)
                    :document-set (:document-set direct)
                    :kind (:kind direct)
                    :cache-status (:status evidence)
                    :cache-reason (:reason evidence)
                    :capacity (:contained-capacity evidence)
                    :cache-publication (:cache-publication evidence)
                    :disk-candidate-publication
                    (:disk-candidate-publication evidence)
                    :reader-executed? (:reader-executed? evidence)
                    :artifact-reused? (:artifact-reused? evidence)
                    :inventory (inventory cache-base)}]
               (assert (= direct returned))
               (assert (= (:artifact-id direct) (:artifact-id returned)))
               (assert (= :gravity/stage0-c2-reader-document-artifact
                          (:kind observed)))
               (assert (= "P06-D081" (:task observed)))
               (assert (= ["C2"] (:document-set observed)))
               (assert (= :accepted (:resolved-status observed)))
               (assert (= :accepted
                          (:authenticated-envelope-status observed)))
               (assert (= :fresh-artifact-envelope-capacity
                          (:cache-reason observed)))
               (assert (= [:canonical-nodes 524288 524289]
                          ((juxt :resource :maximum :observed)
                           (:capacity observed))))
               (assert (not (contains? (:capacity observed)
                                       :invocation-token)))
               (assert (= [:withheld :none true false]
                          ((juxt :cache-publication
                                 :disk-candidate-publication
                                 :reader-executed? :artifact-reused?)
                           observed)))
               (assert (= {:blobs 0 :entries 0 :receipts 0 :staging 0}
                          (:inventory observed)))
               (prn observed)
               observed))
           [".gravity" ".qst"])]
      (assert (apply = (map :source-sha256 results)))
      (assert (apply = (map :complete-census results)))
      (prn {:paired-source-byte-identical true
            :source-byte-count (alength bytes)
            :source-sha256 source-hash
            :complete-census (:complete-census (first results))
            :extensions (mapv :extension results)}))
    (finally (delete-tree! temporary))))
```

The emitted extension receipts were:

| Extension | Direct and returned artifact ID | Complete nodes/depth | Capacity | Candidate files |
| --- | --- | --- | --- | --- |
| `.gravity` | `sha256:055f36d44e2a9ac18dba4938d07350413c38a0cb732e28fb3aab70c8ae6a6a66` | `8256942` / `24` | nodes `524289 > 524288` | `0/0/0/0` |
| `.qst` | `sha256:5164726c5c283402580bea19e0415dd346586a85a6912881b374283a53747af3` | `8256942` / `24` | nodes `524289 > 524288` | `0/0/0/0` |

The artifact and source-unit IDs include actual-path provenance, so the two
extensions correctly have distinct IDs despite byte-identical contents.

One earlier harness invocation exited 1 after 413.21 seconds with `explicit
cache base must already exist`. It had completed only the direct parse and
reached no capacity assertion or cache mutation. It is excluded from positive
evidence; the successful command above created each explicit cache base first.

## Accepted and rejected behavior

### Requested-hit object identity repair

The private C2 adapter now closes one requested-hit binding inside each public
invocation. The binding retains the exact decoded artifact object presented to
the semantic validator and returns that same object after the generic cache
result has passed projection. It binds and rechecks the invocation token,
operation, exact generic key, entry, envelope, artifact, payload-byte objects,
requested cache key, semantic/storage key identifiers, entry/receipt/producer
identifiers, validation/requested/C2 artifact identifiers, and the authenticated
boundary, carrier, and payload identifiers. No object reference or invocation
token is emitted or persisted.

The focused repair tests observe semantic validation through a private test
seam. Every observation carries a role, requested cache-key id, validated
cache-key id, artifact id, and a test-memory-only object tag:

- a clean miss has exactly one `:fresh-requested-artifact` validation;
- a requested hit has exactly one `:requested-retained-artifact` validation,
  and the public result is `identical?` to that validated object;
- direct store has exactly one `:direct-store-artifact` validation;
- an invalid semantic key performs zero semantic validations;
- corrupt retained data followed by fresh production records only the fresh
  role with the requested key and new artifact identifiers;
- recovery uses `:recovery-artifact`, remains distinct from the requested
  artifact, and cannot replace its object binding;
- two concurrent same-key requests execute one producer and record one fresh
  plus one requested-hit validation with the same requested key/artifact ids,
  distinct object tags, invocation-local bindings, and an identity-preserved
  hit result;
- an equal-but-distinct value returned by the semantic validator is rejected
  as `C16-STALE`;
- a successful requested binding followed by generic rejection clears the
  binding before fresh production, so no stale object can escape; and
- mutation of a retained payload byte after validation is rejected as
  `C16-STALE` with `requested C2 hit binding changed after validation`.

The semantic validator still exposes the same generic API. The role-tagged
trace and object tags exist only at the private C2 testing boundary.

The focused tests establish all three canonical resources at the exact limit
and first-plus-one. Exact-limit node, depth, and byte artifacts publish one
blob, one receipt, and one entry with zero staging files. First-plus-one returns
the identical prevalidated artifact twice, performs semantic validation once
per requested production, never hits, and leaves all four inventories at zero.

The clean-miss public result has status `:miss`, reason
`:fresh-artifact-envelope-capacity`, publication `:withheld`, disk candidate
publication `:none`, and no rejected-entry evidence. The rejected-entry result
uses reason `:rejected-entry-fresh-artifact-envelope-capacity`, retains the
complete generic rejected-entry evidence and original corrupt entry bytes, and
does not replace or add a file. Direct `store!` propagates the exact registered
capacity exception with operation `:store` and phase
`:direct-store-envelope-serialization`; it does not return withholding success.

The sanitized contained-capacity record has exactly these keys:

```clojure
#{:id :artifact :schema-version :stage :operation :phase :carrier-id
  :resource :maximum :observed :semantic-key-id :storage-key-id
  :c2-artifact-id}
```

Its fixed facts are `C16-POLICY`, schema 1, stage `:c2-reader`, artifact
`:gravity/c2-pass-cache-envelope-capacity-exception`, operation
`:lookup-or-compute`, phase `:fresh-envelope-serialization`, resource one of
`:canonical-nodes`, `:canonical-depth`, or `:canonical-bytes`, and strictly
`observed > maximum`. The invocation-local `Object` token and exact exception
and handoff wrapper identities never appear in evidence or serialized data.

Invalid semantic keys stay `C16-STALE`/`C16-KEY` before production. Unsupported
fresh values stay `C16-KEY`, rather than becoming capacity. Validator errors,
the exact producer `IOException`, the exact `InterruptedException`, and the
exact injected `ClosedByInterruptException` propagate without translation;
the two interruption cases preserve interrupt state. Existing retained-data
corruption remains `C16-ENTRY`. The complete generic suite covers unchanged
ordinary miss/hit, recovery, locking, atomic publication, and non-C2 operation
schema behavior.

## Source digests and live-seam audit

| File | SHA-256 |
| --- | --- |
| `gravity/c2_pass_cache.clj` | `1d772b5446ae1500944d18a85044f63ada8f9ec6065d526592dcc65cd0605ea9` |
| `gravity/c2_pass_cache/adapter_projection.clj` | `67d95a4af8e386c6639c8f4580d58e6923a0ab53d01265a9ee57909bae4cfe7a` |
| `gravity/c2_pass_cache/adapter_request.clj` | `d79602e7a3150c97142b77e62a45fe105fe698efea11ef745d43ff431a30e588` |
| `gravity/c2_pass_cache/canonical_decode.clj` | `e86007c14fab1fccdd729fd9afbd5a7567077d9f36aca37584194a6ee82ca96d` |
| `gravity/c2_pass_cache/canonical_encode.clj` | `591396e29c46c29e442045eea136e6503429666c9fc8c9acd06c5e40addfe0a5` |
| `gravity/c2_pass_cache/capacity_handoff.clj` | `e26a7467fbb2375296902df60969ca8d061cf4d1afb753ffb52366c2dd9a037d` |
| `gravity/c2_pass_cache/requested_hit_binding.clj` | `33c47d524399602df3313a5c480558ade4712a2edc1515e0554de4c194f73b32` |
| `gravity/pass_cache/execution.clj` | `6a29b4f61f0d526003465813f95db550766cb7da591a117dfe10441a4f1e2e7d` |
| `gravity/pass_cache/publication.clj` | `747e75fe5c86bbd718eca9cc0f70fc536e7c63378f95e5fe9bf8e176c98c12b5` |
| `gravity/pass_cache/receipt_validation.clj` | `ba382018a9c581a4b7017ea3cfe95f14ed922bd04dfbeea97ef159a32483609f` |
| `gravity/c2_pass_cache_test.clj` | `a0c9b7ee9a92e63375aacba908b272e35d8f402b81213fde59447d85426de0cb` |
| `gravity/pass_cache_test.clj` | `ae37573fe4447599ad763d473bf6c203c163f7aa1d448cbc5d36feacab64c2fb` |

Paths in the table are relative to `bootstrap/clojure/src` or
`bootstrap/clojure/test`. Definition/call-site inspection confirms the facade's
later adapter definitions own public C2 `lookup!`, `store!`, and
`lookup-or-compute!`; the new requested-hit binding is private to that adapter;
generic execution owns only the producer-local handoff; receipt validation and
publication retain their separate roles. No generic cache API, lookup,
recovery, retention-scan, path-policy, SH-03, SH-07, release, or roadmap source
was changed.

`clojure -M tools/validate_clojure_loc.clj` exits 1 only for these pre-existing,
untouched files: `authenticated_artifact_pipeline.clj` at 1069 lines,
`00866-sh05-macro-artifact-verification.clj` at 373, and
`00880-sh06-resolution-artifact-verification.clj` at 372. Every source file
owned by this candidate is at or below the 250-line limit. The failed global
LOC result is recorded as an exclusion, not a passing validation.

Environment: Clojure CLI `1.12.5.1654`, OpenJDK `26.0.1`, Darwin arm64
`25.5.0`.

The earlier independent review report remains the authoritative REJECT for the
old exact tuple `c8f373fb59509c4ea03339b51bdbb960c82b64cd`, tree
`ca080ee42557232f3a8a938ddb5dac92bb9a512a`, evidence
`0fc613afacd85a4e9e22339ec18aaa53effd25f8ca4c28b36c6bd24ed7a32e77`.
It is historical evidence, not a review of this repair. This repair requires a
new independent review after its exact candidate is frozen.

## Authority ceiling

This evidence supports an exact implementation candidate entering independent
review only. It does not itself review, accept, integrate, publish, or grant C2
or C16 conformance, SH-03 or SH-07 completion, proof, equivalence, release,
self-hosting, safety, performance, roadmap, or seed-retirement authority.
Clojure and the JVM remain the adapter, validator, canonical traversal,
exception-identity, filesystem, lock, and observer boundary. The cache remains
a local speculative development optimization with its existing provider and
same-user mutation exclusions.
