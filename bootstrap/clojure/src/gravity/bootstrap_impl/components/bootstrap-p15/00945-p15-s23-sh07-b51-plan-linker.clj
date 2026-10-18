(declare c-backend-canonical-value c4-artifact-id reader-canonical-value
         p15-s23-compiler-source-form-record-from-text
         p15-s23-compiler-def-value
         p15-s23-stage2-compiler-artifact-augmented-emitter
         sh06-resolution-definition-records sh06-resolution-core-records
         sh06-resolution-analysis-inputs sh06-resolution-package
         sh06-resolution-base-request
         sh06-resolution-artifact-from-sh05-artifact
         sh06-resolution-artifact-verification
         sh06-resolution-plan-identities
         sh06-resolution-source-relative-path
         sh06-resolution-expected-source-byte-count
         sh06-resolution-expected-source-content-hash
         sh06-resolution-expected-plan-semantic-hash
         sh06-resolution-expected-plan-id
         sh06-resolution-expected-functions-semantic-hash
         sh06-resolution-expected-function-count
         sh06-resolution-expected-function-names-hash
         sh06-resolution-expected-function-shapes-hash
         sh06-resolution-public-function-hashes
         sh06-resolution-public-function-shapes
         sh07-core-authenticated-request
         p15-s23-sh07-b51-derive-callsite-census
         p15-s23-stage2-construct-emitted-core-plan
         p15-s23-sh07-b51-resolution-plan-authority-valid?
         p15-s23-sh07-b51-plan-valid-structure?
         p15-s23-sh07-b51-plan-links-valid-structure?
         p15-s23-sh07-b51-execution-bundle-core-valid-structure?
         p15-s23-sh07-b51-execution-bundle-valid-structure?
         p15-s23-sh07-b51-execution-bundle-authorized?
         p15-s23-sh07-b51-current-authoritative-linked-products-v4
         ^:private p15-s23-sh07-b51-issue-top-level-validation-receipt!
         ^:private p15-s23-sh07-b51-retire-admitted-runtime-context!
         p15-s23-stage2-runtime-execute-function
         ^:private p15-s23-stage2-runtime-execute-function-scoped)

(def p15-s23-sh07-b51-module-specs
  [{:module-ordinal 0
    :module 'gravity.sh07-b51.producer-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
    :owner :r1-producer
    :exports []}
   {:module-ordinal 1
    :module 'gravity.sh07-b51.producer-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
    :owner :r1-producer
    :exports []}
   {:module-ordinal 2
    :module 'gravity.sh07-b51.producer-c
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
    :owner :r1-producer
    :exports '[sh07-b51-resolve-digest-preimage]}
   {:module-ordinal 3
    :module 'gravity.sh07-b51.producer-d
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
    :owner :r1-producer
    :exports '[sh07-b51-build-template
               sh07-b51-verify-template
               sh07-b51-resolve-template
               sh07-b51-verify-resolved
               sh07-b51-build-final-artifact]}
   {:module-ordinal 4
    :module 'gravity.sh07-b51.root6-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
    :owner :r6-independent-verifier
    :exports []}
   {:module-ordinal 5
    :module 'gravity.sh07-b51.root6-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
    :owner :r6-independent-verifier
    :exports '[sh07-b51-build-independent-verifier-binding]}
   {:module-ordinal 6
    :module 'gravity.sh07-b51.root8
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"
    :owner :r8-finalizer
    :exports '[sh07-b51-finalize-rejection]}])

(def p15-s23-sh07-b51-public-root-specs
  [{:root-ordinal 1 :function 'sh07-b51-build-template :arity 1
    :module-ordinal 3}
   {:root-ordinal 2 :function 'sh07-b51-verify-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 3 :function 'sh07-b51-resolve-digest-preimage :arity 3
    :module-ordinal 2}
   {:root-ordinal 4 :function 'sh07-b51-resolve-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 5 :function 'sh07-b51-verify-resolved :arity 4
    :module-ordinal 3}
   {:root-ordinal 6
    :function 'sh07-b51-build-independent-verifier-binding :arity 6
    :module-ordinal 5}
   {:root-ordinal 7 :function 'sh07-b51-build-final-artifact :arity 7
    :module-ordinal 3}
   {:root-ordinal 8 :function 'sh07-b51-finalize-rejection :arity 1
    :module-ordinal 6}])

(def ^:private p15-s23-sh07-b51-m0-keys
  #{:artifact :schema-version :module-source-id :module-ordinal :module
    :logical-source-path :owner :source-byte-count :source-sha256 :profile
    :target :effects :capabilities :safety :definitions :exports
    :core-binding-count :lexical-binding-count :external-bindings
    :aggregate-binding-count :builtin-calls})

(def ^:private p15-s23-sh07-b51-definition-keys
  #{:name :arity :visibility :source-hash})

(def ^:private p15-s23-sh07-b51-external-binding-keys
  #{:symbol :callee-module-ordinal :callee-module :callee-source-sha256
    :callee-definition-source-hash :arity :owner-edge})

(def ^:private p15-s23-sh07-b51-builtin-call-keys
  #{:caller :caller-function-source-hash :builtin :arity
    :compiler-internal?})

(def ^:private p15-s23-sh07-b51-edge-keys
  #{:ordinal :caller-module-source-id :caller-function
    :caller-function-source-hash :callsite-path :callee-module-source-id
    :callee-function :callee-function-source-hash :arity :owner-edge
    :authorization})

(def ^:private p15-s23-sh07-b51-census-edge-keys
  #{:ordinal :caller-module-ordinal :caller-module :caller-function
    :callsite-path :callee-module-ordinal :callee-module :callee-function
    :arity :owner-edge})

(def ^:private p15-s23-sh07-b51-m1-keys
  #{:artifact :schema-version :manifest-id :module-source-ids :edges})

(def ^:private p15-s23-sh07-b51-m3-keys
  #{:artifact :schema-version :manifest-id :module-plans :routes
    :public-roots :bundle-core-id :compile-census-certificate-ids
    :bundle-id})

(def ^:private p15-s23-sh07-b51-bundle-core-keys
  #{:domain :schema-version :manifest-id :module-plans :routes
    :public-roots})

(def ^:private p15-s23-sh07-b51-root8-external-callees
  '[sh07-b51-build-template
    sh07-b51-entrypoint-envelope
    sh07-b51-exact-keys?
    sh07-b51-kind-remediation
    sh07-b51-kind-rule
    sh07-b51-nonnegative-integer?
    sh07-b51-pending-rejected-envelope-shape?
    sh07-b51-pending-rejection
    sh07-b51-raw-carrier-shape?
    sh07-b51-semantic-coordinate
    sh07-b51-sha256-id?
    sh07-b51-template-shape-for-plan?])

;; Attempt 30 cannot acquire implementation authority until both independent
;; census certificates and the resulting M3/root snapshot have been frozen.
;; These deliberately non-digest values fail every authority check and must be
;; replaced only by a separately reviewed immutable implementation snapshot.
(def ^:private p15-s23-sh07-b51-expected-m3-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-certificate-a-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-certificate-b-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-semantic-root
  :gravity/provisional-no-authority)

(def ^:private p15-s23-sh07-b51-source-specs
  (vec
   (concat
    [{:ordinal 0 :kind :l1-contract :module-ordinal nil
      :path "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"}
     {:ordinal 1 :kind :l2-contract :module-ordinal nil
      :path "docs/phase-01-core-language/012-l2-core-language-semantics.md"}
     {:ordinal 2 :kind :checked-core :module-ordinal nil
      :path "bootstrap/gravity/src/gravity/checked_core.gravity"}]
    (mapv (fn [{:keys [module-ordinal logical-source-path]}]
            {:ordinal (+ 3 module-ordinal)
             :kind :module
             :module-ordinal module-ordinal
             :path logical-source-path})
          p15-s23-sh07-b51-module-specs)
    [{:ordinal 10 :kind :compiler :module-ordinal nil
      :path "bootstrap/gravity/p15_s23/compiler.gravity"}
     {:ordinal 11 :kind :emitter :module-ordinal nil
      :path "bootstrap/gravity/p15_s23/emitter.gravity"}
     {:ordinal 12 :kind :sh06-resolution :module-ordinal nil
      :path "bootstrap/gravity/src/gravity/resolution.gravity"}])))

(def ^:private p15-s23-sh07-b51-source-event-plan
  (vec
   (concat
    [{:ordinal 0 :domain :l1-contract :source-ordinal 0
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l1-contract}
     {:ordinal 1 :domain :l2-contract :source-ordinal 1
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l2-contract}
     {:ordinal 2 :domain :checked-core-revision :source-ordinal 2
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 3 module-ordinal)
       :domain :module-validation
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 10 module-ordinal)
       :domain :callsite-census
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 17 module-ordinal)
       :domain :link-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census})
    [{:ordinal 24 :domain :compiler-emitter :source-ordinal 10
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-current-emitter}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 25 module-ordinal)
       :domain :source-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-current-module-source-authority})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 32 module-ordinal)
       :domain :compiler-artifact-binding
       :source-ordinal 11
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-stage2-compiler-artifact-binding})
    [{:ordinal 39 :domain :sh06-resolution-binding :source-ordinal 12
      :module-ordinal nil
      :consumer
      'gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding}])))
