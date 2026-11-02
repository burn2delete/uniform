(clojure.core/require 'gravity.macro)

(declare
 sh05-defn-form?
 sh05-expanded-graph
 sh05-expanded-graph-valid?
 sh05-expanded-syntax-object
 sh05-expanded-syntax-stream-id
 sh05-macro-artifact-id
 sh05-macro-artifact-keys
 sh05-macro-current-binding!
 sh05-macro-envelope-stage
 sh05-macro-envelope-summary
 sh05-macro-expected-source-byte-count
 sh05-macro-gravity-verifier-report
 sh05-macro-sealed-artifact-kind
 sh05-macro-sh02-descriptor
 sh05-macro-source-relative-path
 sh05-macro-trace-id
 sh05-macro-version
 sh05-package-trace
 sh05-source-syntax-stream)

(declare
 compiler-c4-stage0-legacy-source-artifact
 sh05-authenticated-c3-parent!
 sh05-authenticated-source-snapshot!
 sh05-authoritative-compiler-source-path?
 sh05-bounded-authoritative-source?
 sh05-c4-compatibility-artifact
 sh05-c4-compatibility-artifact-id
 sh05-c4-compatibility-identity-input
 sh05-deep-merge
 sh05-default-macro-request
 sh05-defn-form?
 sh05-expanded-form
 sh05-expanded-graph
 sh05-expanded-syntax-object
 sh05-expanded-syntax-stream-id
 sh05-expanded-syntax-stream-identity-input
 sh05-form-contains-legacy-macro-position?
 sh05-identity-chunk-width
 sh05-macro-adapter-contract
 sh05-macro-artifact-id
 sh05-macro-artifact-identity-input
 sh05-macro-artifact-semantic-payload
 sh05-macro-artifact-verification
 sh05-macro-boundary-fail!
 sh05-macro-capability-based-proof-for-construction
 sh05-macro-current-binding!
 sh05-macro-envelope-stage
 sh05-macro-envelope-summary
 sh05-macro-execute!
 sh05-macro-expected-source-byte-count
 sh05-macro-public-function-hashes
 sh05-macro-public-function-shapes
 sh05-macro-resolve-source-path
 sh05-macro-run-semantic-summary
 sh05-macro-sealed-artifact-kind
 sh05-macro-sh02-descriptor
 sh05-macro-source-artifact
 sh05-macro-source-relative-path
 sh05-macro-trace-id
 sh05-macro-trace-identity-input
 sh05-macro-version
 sh05-ordered-identity-chunks
 sh05-package-trace
 sh05-path-neutral-semantic-value
 sh05-run-macro-request!
 sh05-semantic-component-id
 sh05-source-snapshot-stable!
 sh05-source-syntax-stream)

(declare
 sh05-contain-coordinator-operation!
 sh05-macro-boundary-fail!
 sh05-macro-cached-binding
 sh05-macro-compute-plan-identities
 sh05-macro-execute!
 sh05-macro-expected-function-count
 sh05-macro-expected-function-names-hash
 sh05-macro-expected-function-shapes-hash
 sh05-macro-expected-functions-semantic-hash
 sh05-macro-expected-plan-semantic-hash
 sh05-macro-expected-source-byte-count
 sh05-macro-expected-source-content-hash
 sh05-macro-pinned-plan-identities
 sh05-macro-plan-identities
 sh05-macro-public-function-hashes
 sh05-macro-public-function-shapes
 sh05-macro-raise-rejection!
 sh05-macro-read-pinned-source!
 sh05-macro-resolve-digests!
 sh05-macro-resolve-source-path
 sh05-macro-source-relative-path
 sh05-macro-version
 sh05-resolve-expansion-template!
 sh05-resolve-request-preimage!)

(declare ^{:private true} macro-stage-input)

(def
 ^{:private true} macro-stage-defaults
 {:c3-syntax-stream-reader-products-authentic?
  #'c3-syntax-stream-reader-products-authentic?,
  :p15-s23-stage2-sh02-descriptor-envelope
  #'p15-s23-stage2-sh02-descriptor-envelope,
  :p15-s23-stage2-compiler-artifact-plan
  #'p15-s23-stage2-compiler-artifact-plan,
  :compiler-c4-stage0-legacy-source-artifact
  #'compiler-c4-stage0-legacy-source-artifact,
  :sh05-macro-execute! #'sh05-macro-execute!,
  :sh05-macro-artifact-id #'sh05-macro-artifact-id,
  :sh05-source-syntax-stream #'sh05-source-syntax-stream,
  :sh05-resolve-expansion-template! #'sh05-resolve-expansion-template!,
  :source-span #'source-span,
  :sh05-macro-sh02-descriptor #'sh05-macro-sh02-descriptor,
  :parse-module #'parse-module,
  :p15-s23-stage2-runtime-execute-function
  #'p15-s23-stage2-runtime-execute-function,
  :sh05-expanded-form #'sh05-expanded-form,
  :sh05-macro-trace-id #'sh05-macro-trace-id,
  :sh05-macro-current-binding! #'sh05-macro-current-binding!,
  :p15-s23-stage2-compiler-artifact-semantic-input
  #'p15-s23-stage2-compiler-artifact-semantic-input,
  :sh05-macro-resolve-digests! #'sh05-macro-resolve-digests!,
  :sh05-macro-gravity-verifier-report
  #'sh05-macro-gravity-verifier-report,
  :sha256-hex #'sha256-hex,
  :sh05-macro-compute-plan-identities
  #'sh05-macro-compute-plan-identities,
  :sh05-c4-compatibility-artifact-id
  #'sh05-c4-compatibility-artifact-id,
  :sh05-authoritative-compiler-source-path?
  #'sh05-authoritative-compiler-source-path?,
  :sh05-macro-resolve-source-path #'sh05-macro-resolve-source-path,
  :sh05-expanded-graph #'sh05-expanded-graph,
  :sh05-contain-coordinator-operation!
  #'sh05-contain-coordinator-operation!,
  :sh05-defn-form? #'sh05-defn-form?,
  :c-backend-canonical-value #'c-backend-canonical-value,
  :sh05-expanded-syntax-stream-id #'sh05-expanded-syntax-stream-id,
  :sh05-macro-envelope-summary #'sh05-macro-envelope-summary,
  :compiler-c2-reader-source-artifact
  #'compiler-c2-reader-source-artifact,
  :reader-canonical-hash #'reader-canonical-hash,
  :c4-macro-fail! #'c4-macro-fail!,
  :sh05-macro-plan-identities #'sh05-macro-plan-identities,
  :c3-syntax-capability-proof #'c3-syntax-capability-proof,
  :sh05-ordered-identity-chunks #'sh05-ordered-identity-chunks,
  :sh05-resolve-request-preimage! #'sh05-resolve-request-preimage!,
  :sh05-source-snapshot-stable! #'sh05-source-snapshot-stable!,
  :sh05-macro-run-semantic-summary #'sh05-macro-run-semantic-summary,
  :sh05-macro-artifact-verification #'sh05-macro-artifact-verification,
  :sh05-authenticated-source-snapshot!
  #'sh05-authenticated-source-snapshot!,
  :sh05-bounded-authoritative-source?
  #'sh05-bounded-authoritative-source?,
  :sh05-deep-merge #'sh05-deep-merge,
  :sh05-macro-artifact-semantic-payload
  #'sh05-macro-artifact-semantic-payload,
  :sh05-c4-compatibility-identity-input
  #'sh05-c4-compatibility-identity-input,
  :sh05-default-macro-request #'sh05-default-macro-request,
  :sh05-authenticated-c3-parent! #'sh05-authenticated-c3-parent!,
  :sh05-expanded-syntax-object #'sh05-expanded-syntax-object,
  :p15-s23-stage2-sh02-descriptor-envelope-verify!
  #'p15-s23-stage2-sh02-descriptor-envelope-verify!,
  :sh05-macro-trace-identity-input #'sh05-macro-trace-identity-input,
  :sh05-form-contains-legacy-macro-position?
  #'sh05-form-contains-legacy-macro-position?,
  :sh05-macro-capability-based-proof-for-construction
  #'sh05-macro-capability-based-proof-for-construction,
  :sh05-c4-compatibility-artifact #'sh05-c4-compatibility-artifact,
  :sh05-macro-artifact-identity-input
  #'sh05-macro-artifact-identity-input,
  :sh05-expanded-syntax-stream-identity-input
  #'sh05-expanded-syntax-stream-identity-input,
  :c3-artifact-id #'c3-artifact-id,
  :sha256-bytes-hex #'sha256-bytes-hex,
  :sh05-macro-boundary-fail! #'sh05-macro-boundary-fail!,
  :input #'macro-stage-input,
  :c3-syntax-validate! #'c3-syntax-validate!,
  :sh05-macro-raise-rejection! #'sh05-macro-raise-rejection!,
  :sh05-macro-source-artifact #'sh05-macro-source-artifact,
  :sh05-path-neutral-semantic-value #'sh05-path-neutral-semantic-value,
  :c-backend-stage2-plan-emitter-source-rule!
  #'c-backend-stage2-plan-emitter-source-rule!,
  :sh05-semantic-component-id #'sh05-semantic-component-id,
  :sh05-macro-read-pinned-source! #'sh05-macro-read-pinned-source!,
  :sh05-expanded-graph-valid? #'sh05-expanded-graph-valid?,
  :sh05-run-macro-request! #'sh05-run-macro-request!,
  :p15-s23-stage2-compiler-artifact-source-path
  #'p15-s23-stage2-compiler-artifact-source-path,
  :p15-s23-c11-mir-digest #'p15-s23-c11-mir-digest,
  :sh03-reader-authenticated-envelope-options
  #'sh03-reader-authenticated-envelope-options,
  :p15-s23-c6c10-canonical-digest #'p15-s23-c6c10-canonical-digest,
  :sh05-package-trace #'sh05-package-trace,
  :sh04-syntax-strip-host-metadata #'sh04-syntax-strip-host-metadata})

(defn- macro-stage-operations [] macro-stage-defaults)

(defn-
 macro-stage-input
 [key]
 (case
  key
  :c4-macro-diagnostic-ids
  c4-macro-diagnostic-ids
  :c4-macro-governing-document
  c4-macro-governing-document
  :sh05-identity-chunk-width
  sh05-identity-chunk-width
  :sh05-macro-adapter-contract
  sh05-macro-adapter-contract
  :sh05-macro-artifact-keys
  sh05-macro-artifact-keys
  :sh05-macro-cached-binding
  sh05-macro-cached-binding
  :sh05-macro-envelope-stage
  sh05-macro-envelope-stage
  :sh05-macro-expected-function-count
  sh05-macro-expected-function-count
  :sh05-macro-expected-function-names-hash
  sh05-macro-expected-function-names-hash
  :sh05-macro-expected-function-shapes-hash
  sh05-macro-expected-function-shapes-hash
  :sh05-macro-expected-functions-semantic-hash
  sh05-macro-expected-functions-semantic-hash
  :sh05-macro-expected-plan-semantic-hash
  sh05-macro-expected-plan-semantic-hash
  :sh05-macro-expected-source-byte-count
  sh05-macro-expected-source-byte-count
  :sh05-macro-expected-source-content-hash
  sh05-macro-expected-source-content-hash
  :sh05-macro-pinned-plan-identities
  sh05-macro-pinned-plan-identities
  :sh05-macro-public-function-hashes
  sh05-macro-public-function-hashes
  :sh05-macro-public-function-shapes
  sh05-macro-public-function-shapes
  :sh05-macro-sealed-artifact-kind
  sh05-macro-sealed-artifact-kind
  :sh05-macro-source-relative-path
  sh05-macro-source-relative-path
  :sh05-macro-version
  sh05-macro-version))

(def
 sh05-macro-source-relative-path
 "bootstrap/gravity/src/gravity/macro.gravity")

(def
 sh05-macro-facade-relative-path
 "bootstrap/gravity/src/gravity/compiler/c4_macro_expansion_engine.gravity")

(def sh05-macro-expected-source-byte-count 56488)

(def
 sh05-macro-expected-source-content-hash
 "sha256:19fe589efb27228b8788347439381b61c907a7b6a562a2a3ac3f7256ae77e549")

(def
 sh05-macro-expected-plan-semantic-hash
 "sha256:fa8d1c27377204ab134d47ab217b39638acd6eb5a1580276d899850f3fd3d1da")

(def
 sh05-macro-expected-plan-id
 "sha256:382fd0afe5e271a96d006f69cc76d40a67a68f0ef8abd091c133271b1f7c3e93")

(def
 sh05-macro-expected-functions-semantic-hash
 "sha256:50d131f8146232fc09929e5fbcfab4f9db011f720b785d05fe40029bd01ce407")

(def sh05-macro-expected-function-count 46)

(def
 sh05-macro-expected-function-names-hash
 "sha256:42e599bb9b50599135695c0eb6bf39c4ceb0c79884ad6fcd7b08537871b15dbe")

(def
 sh05-macro-expected-function-shapes-hash
 "sha256:4a49b16738264948c46c15674b9a96fbc32cb473054bebe3893b5a871d156c1a")

(def
 sh05-macro-public-function-hashes
 {'sh05-expand-macro-template
  "sha256:7bdb4f2ffebbb9519065e005210f5f2fca2efe90017996d82db9fdd807030b04",
  'sh05-verify-macro-template
  "sha256:599304043c490982bb4c58b919cac16d3e18172f348d4359d62a0e3d9630d241",
  'sh05-verify-macro-resolved
  "sha256:0d3f61ce19a0093d674f5411189dcfd3aebf4ae20a7151c227dbdff15a79b432"})

(def
 sh05-macro-public-function-shapes
 {'sh05-expand-macro-template {:arity 1, :params ['request]},
  'sh05-verify-macro-template
  {:arity 2, :params ['expansion-template 'digest-requests]},
  'sh05-verify-macro-resolved
  {:arity 3,
   :params ['resolved-expansion 'digest-requests 'resolved-digests]}})

(def sh05-macro-adapter-contract :gravity/sh05-to-c4-macro-products-v1)

(def
 sh05-macro-version
 (get sh05-macro-public-function-hashes 'sh05-expand-macro-template))

(def sh05-macro-envelope-stage :c4-macro)

(def sh05-macro-sealed-artifact-kind :gravity/sh05-macro-products)

(defn
 sh05-macro-boundary-fail!
 [rule source-path missing-field observed facts]
 (gravity.macro/boundary-fail!
  rule
  source-path
  missing-field
  observed
  facts
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-macro-resolve-source-path
 []
 (gravity.macro/resolve-plan-source-path
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-macro-read-pinned-source!
 [request-source]
 (gravity.macro/read-pinned-plan-source!
  request-source
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn-
 sh05-macro-compute-plan-identities
 [plan]
 (gravity.macro/compute-plan-identities
  plan
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(def ^{:private true} sh05-macro-pinned-plan-identities (atom nil))

(defn
 sh05-macro-plan-identities
 [plan]
 (gravity.macro/plan-identities
  plan
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-macro-build-binding!
 [request-source]
 (gravity.macro/build-plan-binding!
  request-source
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(def
 ^{:private true} sh05-macro-cached-binding
 (delay (sh05-macro-build-binding! "<sh05-macro-bootstrap>")))

(defn
 sh05-macro-current-binding!
 [request-source]
 (gravity.macro/current-plan-binding!
  request-source
  ^{:line 112, :column 65}
  (macro-stage-operations)))
