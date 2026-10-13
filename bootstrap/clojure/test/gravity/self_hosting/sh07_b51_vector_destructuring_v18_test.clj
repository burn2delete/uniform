Warning: truncated output (original token count: 170760)
Total output lines: 14222

(ns gravity.self-hosting.sh07-b51-vector-destructuring-v18-test
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_b51_vector_destructuring_v18_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-07-B51-v18 test source is not on the classpath"
                {:id "SH07-B51-V18-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "SH-07-B51-v18 repository root was not found"
                  {:id "SH07-B51-V18-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        (.toAbsolutePath (.normalize candidate))

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/checked_core.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07/b51-vector-destructuring-v18")
(def ^:private extensions [".gravity" ".qst"])

(def ^:private fixture-catalog
  {"accepted"
   #{"nested-empty-wildcard-let"
     "nested-empty-wildcard-loop-recur"
     "nested-loop-owner-attribution"
     "plain-function-recur"
     "function-recur-inside-loop"
     "loop-recur-inside-function"
     "nested-nearest-recur-targets"
     "function-recur-match-tail"
     "ordered-no-form-waiver"
     "ordered-no-form-simple-symbol-waiver"
     "ordinary-core-form-matrix"
     "repeated-sibling-core-occurrences"
     "simple-symbol-parity"
     "source-def-owner"
     "strict-nested-source-def-owner"
     "non-definition-owner"
     "sibling-namespace-external"
     "declared-import-external"}
   "runtime"
   #{"runtime-vector-kind" "runtime-vector-width"}
   "rejected"
   #{"duplicate-vector-binding"
     "unsupported-vector-rest"
     "unsupported-map-pattern"
     "missing-recur-target"
     "recur-arity-mismatch"
     "recur-arity-mismatch-non-tail"
     "recur-not-tail"
     "ordered-no-form-unsupported-vector-rest"
     "module-slot-limit-exceeded"
     "module-binding-leaf-limit-exceeded"
     "module-slot-priority-over-pattern"
     "module-leaf-priority-over-pattern"
     "module-slot-after-leaf-priority"
     "unauthorized-cross-fragment"}})

(def ^:private predecessor-exports
  '[sh07-core-contract
    sh07-core-bounds
    sh07-core-diagnostic-catalog
    sh07-build-core-template
    sh07-verify-core-template
    sh07-verify-core-resolved])

(def ^:private public-roots
  '[sh07-b51-build-template
    sh07-b51-verify-template
    sh07-b51-resolve-digest-preimage
    sh07-b51-resolve-template
    sh07-b51-verify-resolved
    sh07-b51-build-independent-verifier-binding
    sh07-b51-build-final-artifact
    sh07-b51-finalize-rejection])

(def ^:private public-shapes
  {'sh07-b51-build-template
   {:arity 1 :params '[raw-carrier]}
   'sh07-b51-verify-template
   {:arity 3 :params '[raw-carrier template digest-requests]}
   'sh07-b51-resolve-digest-preimage
   {:arity 3 :params '[request digest-requests resolved-prefix]}
   'sh07-b51-resolve-template
   {:arity 3 :params '[template digest-requests resolved-digests]}
   'sh07-b51-verify-resolved
   {:arity 4
    :params '[raw-carrier resolved-core digest-requests resolved-digests]}
   'sh07-b51-build-independent-verifier-binding
   {:arity 6
    :params '[raw-carrier resolved-core predecessor-observation
              provenance-binding digest-requests resolved-digests]}
   'sh07-b51-build-final-artifact
   {:arity 7
    :params '[raw-carrier resolved-core predecessor-observation
              provenance-binding verifier-binding digest-requests
              resolved-digests]}
   'sh07-b51-finalize-rejection
   {:arity 1 :params '[raw-carrier]}})

(def ^:private root1-pending-whitelist
  {:lowering-gap
   [:unsupported-vector-rest :unsupported-nested-pattern]
   :duplicate-binding
   [:duplicate-vector-binding-name]
   :recur-target
   [:missing-recur-target :recur-arity-mismatch :recur-not-tail]
   :verify
   [:module-slot-limit-exceeded
    :module-binding-leaf-limit-exceeded]})

(def ^:private root1-catalog-only-unreachable
  #{[:pattern-type :malformed-authenticated-pattern-shape]
    [:recur-target :ambiguous-recur-target]})

(def ^:private detector-boundary-contract
  {'sh07-b51-build-template
   {:tag :template-boundary-rejected
    :reasons [:malformed-invocation :source-integrity-mismatch
              :template-construction-mismatch :success-plan-mismatch]}
   'sh07-b51-verify-template
   {:tag :template-verification-boundary-rejected
    :reasons [:malformed-invocation :invocation-mismatch]}
   'sh07-b51-resolve-template
   {:tag :template-resolution-boundary-rejected
    :reasons [:malformed-invocation :invocation-mismatch :digest-mismatch]}
   'sh07-b51-verify-resolved
   {:tag :resolved-verification-boundary-rejected
    :reasons [:malformed-invocation :invocation-mismatch :digest-mismatch]}
   'sh07-b51-build-independent-verifier-binding
   {:tag :independent-verifier-boundary-rejected
    :reasons [:malformed-invocation :invocation-mismatch :digest-mismatch]}
   'sh07-b51-build-final-artifact
   {:tag :final-artifact-boundary-rejected
    :reasons [:malformed-invocation :invocation-mismatch :digest-mismatch]}})

(def ^:private root6-check-order
  [:raw-carrier-exact-shape :authenticated-sh06-membership
   :fresh-sh06-verification :predecessor-closure :predecessor-contract
   :same-request :predecessor-outcome :predecessor-authority
   :owning-root-fragment :external-binding-authorization
   :vector-pattern-grammar :slot-extraction-reconstruction
   :empty-vector-width :visibility-and-publication :runtime-check-order
   :loop-recur-atomicity :semantic-product-closure
   :legacy-v16-equivalence :digest-dag :tier3-identity
   :tier4-observation-provenance])

(def ^:private root6-preimage-keys
  #{:domain :schema-version :verifier :verifier-entrypoint
    :semantic-identity-id :provenance-binding-id
    :check-order :checks :status})

(def ^:private root6-record-keys
  #{:artifact :schema-version :independent-verifier-binding-id :preimage})

(def ^:private public-abi-contract
  {'sh07-b51-build-template
   {:success [:accepted :template-built]
    :pending [:pending-rejection :template-rejected]
    :boundary [:boundary-rejected :template-boundary-rejected]
    :success-value-keys #{:template :digest-requests}}
   'sh07-b51-verify-template
   {:success [:accepted :template-verified]
    :boundary [:boundary-rejected :template-verification-boundary-rejected]
    :success-value-keys
    #{:artifact :schema-version :status :rule :reason :details}}
   'sh07-b51-resolve-digest-preimage
   {:success [:accepted :digest-preimage-resolved]
    :boundary [:boundary-rejected :digest-preimage-boundary-rejected]
    :success-value-keys
    #{:status :request :digest-requests :resolved-prefix :hash-input}}
   'sh07-b51-resolve-template
   {:success [:accepted :template-resolved]
    :boundary [:boundary-rejected :template-resolution-boundary-rejected]
    :success-value-keys
    #{:resolved-core :predecessor-observation :provenance-binding}}
   'sh07-b51-verify-resolved
   {:success [:accepted :resolved-verified]
    :boundary [:boundary-rejected :resolved-verification-boundary-rejected]
    :success-value-keys #{:artifact :schema-version :status :rule}}
   'sh07-b51-build-independent-verifier-binding
   {:success [:accepted :independent-verifier-bound]
    :boundary [:boundary-rejected :independent-verifier-boundary-rejected]}
   'sh07-b51-build-final-artifact
   {:success [:accepted :final-artifact-built]
    :boundary [:boundary-rejected :final-artifact-boundary-rejected]
    :success-value-keys #{:canonical-artifact :outer-wrapper}}
   'sh07-b51-finalize-rejection
   {:success [:accepted :rejection-finalized]
    :boundary
    [:boundary-rejected :rejection-finalizer-boundary-rejected]}})

(def ^:private product-key-order
  [:fragment-manifest :fragment-coverage :module-assembly-manifest
   :root-core-node-ids :definitions :nodes :evaluation-order :control-flow
   :reference-uses :var-references :mutations :error-transfers :error-handlers
   :match-branch-records :match-decision-skeletons :match-pattern-records
   :calls :function-records :call-edges :recursion-components :keyword-lookups
   :lexical-bindings :loop-bindings :recur-targets :recur-transfers :source-map
   :binding-table :declared-alias-table :resolution-table
   :macro-expansion-trace :macro-origin-traces :macro-origin-expectation
   :pending-fact-families :destructuring-product-nodes :binding-slots
   :binding-extractions :slot-extraction-transcript :runtime-checks
   :publication-events :recur-slot-mappings :legacy-v16-equivalence])

(def ^:private ordinary-core-form-set
  #{:reference :literal :collection-literal :keyword-map-lookup :call
    :quote :fn :def :if :do :let :loop :recur :var :set! :throw :try
    :match})

(def ^:private ordinary-core-lowerer-matrix
  {'sh07-lower-reference #{:reference}
   'sh07-lower-literal #{:literal :collection-literal}
   'sh07-lower-keyword-map-lookup #{:keyword-map-lookup}
   'sh07-lower-call #{:call}
   'sh07-lower-quote #{:quote}
   'sh07-lower-fn #{:fn}
   'sh07-lower-def #{:def}
   'sh07-lower-if #{:if}
   'sh07-lower-do #{:do}
   'sh07-lower-let #{:let}
   'sh07-lower-loop #{:loop}
   'sh07-lower-recur #{:recur}
   'sh07-lower-var #{:var}
   'sh07-lower-set #{:set!}
   'sh07-lower-throw #{:throw}
   'sh07-lower-try #{:try}
   'sh07-complete-lowered-match #{:match}})

(def ^:private ordinary-core-attribute-keysets
  {:reference
   #{:symbol :position :binding-id :binding-class :definition-syntax-id}
   :literal #{:literal-kind :value}
   :collection-literal #{:literal-kind :element-count :source-value}
   :keyword-map-lookup
   #{:keyword-child-index :map-child-index :keyword :evaluation-order
     :dispatch :missing-key-policy :result-policy :target-type-legality}
   :call
   #{:operator-child-index :argument-count :argument-child-indexes
     :evaluation-order :dispatch}
   :quote #{:quoted-form-id :quoted-syntax-id :quoted-kind :quoted-value}
   :fn
   #{:fixed-arity :parameter-form-id :parameter-syntax-id :parameters
     :recur-target-id}
   :def #{:name :name-form-id :name-syntax-id :visibility}
   :if
   #{:condition-child-index :then-child-index :else-child-index
     :condition-evaluation :branch-evaluation :truthiness}
   :do #{:body-count :evaluation-order :result :result-child-index}
   :var
   #{:symbol :binding-id :binding-class :definition-kind :object-kind
     :upstream-binding-id :namespace :definition-syntax-id
     :definition-artifact-id :operand-form-id :operand-syntax-id
     :object-policy :resolution-order :authenticated-sh06-artifact-id
     :sh06-semantic-projection-id :profile-legality}
   :set!
   #{:target-symbol :target-binding-id :target-binding-class
     :target-definition-kind :target-location :target-upstream-binding-id
     :target-namespace :target-definition-syntax-id
     :target-definition-artifact-id :target-form-id :target-syntax-id
     :resolution-order :target-evaluation :value-child-index
     :evaluation-order :result-policy :required-effect
     :authenticated-sh06-artifact-id :sh06-semantic-projection-id
     :type-legality :effect-registry-legality
     :effect-profile-capability-legality :ownership-legality
     :safety-classification}
   :throw
   #{:value-child-index :evaluation-order :construction-order
     :runtime-reachability :transfer-policy :result-policy :required-effect
     :authenticated-sh06-artifact-id :sh06-semantic-projection-id
     :type-legality :effect-registry-legality
     :effect-profile-capability-legality :profile-error-lowering-legality
     :ownership-legality :safety-classification}
   :try
   #{:protected-child-index :handler-count :handler-child-indexes
     :handler-clauses :evaluation-order :runtime-reachability
     :selection-policy :result-policy :authenticated-sh06-artifact-id
     :sh06-semantic-projection-id :type-coverage-legality
     :result-type-join-legality :effect-registry-legality
     :effect-profile-capability-legality :profile-error-lowering-legality
     :ownership-legality :safety-classification}
   :match
   #{:scrutinee-child-index :branch-count :branch-child-indexes
     :branch-clauses :evaluation-order :runtime-reachability
     :selection-policy :result-type-join :exhaustiveness
     :authenticated-sh06-artifact-id :sh06-semantic-projection-id}})

(def ^:private ordinary-core-evaluation-kinds
  {:reference :value
   :literal :value
   :collection-literal :left-to-right
   :keyword-map-lookup :keyword-then-map
   :call :operator-then-arguments
   :quote :no-evaluation
   :fn :function-creation
   :def :initialize-once
   :if :condition-then-exclusive-branch
   :do :left-to-right
   :let :initializers-then-body-left-to-right
   :loop :initializers-then-body-left-to-right
   :recur :arguments-left-to-right-then-transfer
   :var :no-evaluation
   :set! :value-then-write
   :throw :value-then-transfer
   :try :protected-then-ordered-typed-handler-candidates
   :match :scrutinee-then-source-ordered-pattern-candidates})

(def ^:private resolved-core-key-order
  (vec
   (concat
    [:artifact :schema-version :predecessor-outcome-kind
     :same-request-semantic-id :predecessor-authority-id
     :module :lineage :projection-binding :semantic-identity-id]
    product-key-order
    [:diagnostics])))

(def ^:private template-key-order
  (vec
   (concat
    [:artifact :schema-version :predecessor-outcome-kind
     :same-request-semantic-id :predecessor-authority-id
     :module :lineage :projection-binding]
    product-key-order
    [:digest-requests :diagnostics])))

(def ^:private tier4-keysets
  {:observation
   #{:artifact :schema-version :observation-id :preimage}
   :observation-preimage
   #{:domain :schema-version :semantic-identity-id :raw-adapter-request}
   :provenance
   #{:artifact :schema-version :provenance-binding-id :preimage}
   :provenance-preimage
   #{:domain :schema-version :semantic-identity-id :observation-id}})

(def ^:private purpose-order
  [:sh07-b51-implementation-semantic-id
   :sh07-b51-contract-semantic-id
   :sh07-b51-same-request-semantic-id
   :sh07-b51-v16-verification-semantic-id
   :sh07-b51-predecessor-outcome-semantic-id
   :sh07-b51-predecessor-authority-id
   :sh07-b51-core-node-id
   :sh07-b51-product-node-id
   :sh07-b51-binding-slot-id
   :sh07-b51-binding-extraction-id
   :sh07-b51-runtime-check-id
   :sh07-b51-publication-event-id
   :sh07-b51-recur-slot-mapping-id
   :sh07-b51-slot-extraction-transcript-id
   :sh07-b51-core-identity-id
   :sh07-b51-predecessor-observation-id
   :sh07-b51-provenance-binding-id
   :sh07-b51-independent-verifier-binding-id
   :sh07-b51-final-artifact-binding-id])

(def ^:private dependency-counts
  {:sh07-b51-implementation-semantic-id 0
   :sh07-b51-contract-semantic-id 0
   :sh07-b51-same-request-semantic-id 0
   :sh07-b51-v16-verification-semantic-id 0
   :sh07-b51-predecessor-outcome-semantic-id 4
   :sh07-b51-predecessor-authority-id 4
   :sh07-b51-core-node-id 2
   :sh07-b51-product-node-id 1
   :sh07-b51-binding-slot-id 2
   :sh07-b51-binding-extraction-id 2
   :sh07-b51-runtime-check-id 4
   :sh07-b51-publication-event-id 5
   :sh07-b51-recur-slot-mapping-id 7
   :sh07-b51-slot-extraction-transcript-id 7
   :sh07-b51-core-identity-id 12
   :sh07-b51-predecessor-observation-id 1
   :sh07-b51-provenance-binding-id 2
   :sh07-b51-independent-verifier-binding-id 2
   :sh07-b51-final-artifact-binding-id 3})

(def ^:private root3-dependency-targets
  {:sh07-b51-implementation-semantic-id []
   :sh07-b51-contract-semantic-id []
   :sh07-b51-same-request-semantic-id []
   :sh07-b51-v16-verification-semantic-id []
   :sh07-b51-predecessor-outcome-semantic-id
   [:sh07-b51-implementation-semantic-id
    :sh07-b51-contract-semantic-id
    :sh07-b51-same-request-semantic-id
    :sh07-b51-v16-verification-semantic-id]
   :sh07-b51-predecessor-authority-id
   [:sh07-b51-implementation-semantic-id
    :sh07-b51-contract-semantic-id
    :sh07-b51-same-request-semantic-id
    :sh07-b51-predecessor-outcome-semantic-id]
   :sh07-b51-core-node-id
   [:sh07-b51-predecessor-authority-id :sh07-b51-core-node-id]
   :sh07-b51-product-node-id [:sh07-b51-predecessor-authority-id]
   :sh07-b51-binding-slot-id
   [:sh07-b51-predecessor-authority-id :sh07-b51-core-node-id]
   :sh07-b51-binding-extraction-id
   [:sh07-b51-predecessor-authority-id :sh07-b51-binding-slot-id]
   :sh07-b51-runtime-check-id
   [:sh07-b51-predecessor-authority-id :sh07-b51-core-node-id
    :sh07-b51-binding-slot-id :sh07-b51-binding-extraction-id]
   :sh07-b51-publication-event-id
   [:sh07-b51-predecessor-authority-id :sh07-b51-core-node-id
    :sh07-b51-product-node-id :sh07-b51-binding-slot-id
    :sh07-b51-runtime-check-id]
   :sh07-b51-recur-slot-mapping-id
   [:sh07-b51-predecessor-authority-id :sh07-b51-core-node-id
    :sh07-b51-product-node-id :sh07-b51-binding-slot-id
    :sh07-b51-binding-extraction-id :sh07-b51-runtime-check-id
    :sh07-b51-publication-event-id]
   :sh07-b51-slot-extraction-transcript-id
   [:sh07-b51-predecessor-authority-id :sh07-b51-same-request-semantic-id
    :sh07-b51-binding-slot-id :sh07-b51-binding-extraction-id
    :sh07-b51-runtime-check-id :sh07-b51-publication-event-id
    :sh07-b51-recur-slot-mapping-id]
   :sh07-b51-core-identity-id
   [:sh07-b51-same-request-semantic-id
    {:accepted-only :sh07-b51-v16-verification-semantic-id}
    {:accepted-only :sh07-b51-predecessor-outcome-semantic-id}
    :sh07-b51-predecessor-authority-id
    {:non-v16-only :sh07-b51-core-node-id}
    :sh07-b51-product-node-id
    :sh07-b51-binding-slot-id :sh07-b51-binding-extraction-id
    :sh07-b51-runtime-check-id :sh07-b51-publication-event-id
    :sh07-b51-recur-slot-mapping-id
    :sh07-b51-slot-extraction-transcript-id]
   :sh07-b51-predecessor-observation-id [:sh07-b51-core-identity-id]
   :sh07-b51-provenance-binding-id
   [:sh07-b51-core-identity-id :sh07-b51-predecessor-observation-id]
   :sh07-b51-independent-verifier-binding-id
   [:sh07-b51-core-identity-id :sh07-b51-provenance-binding-id]
   :sh07-b51-final-artifact-binding-id
   [:sh07-b51-core-identity-id :sh07-b51-provenance-binding-id
    :sh07-b51-independent-verifier-binding-id]})

(def ^:private root3-outcome-kinds
  [:legacy-v16-accepted
   :b51-vector-frontier-rejected
   :legacy-v16-accepted-after-no-form-waiver
   :b51-vector-frontier-rejected-after-no-form-waiver])

(def ^:private root3-reference-census
  {:legacy-v16-accepted
   {:controlled 94 :non-tier3 46 :tier3 48}
   :b51-vector-frontier-rejected
   {:controlled 174 :non-tier3 55 :tier3 119}
   :legacy-v16-accepted-after-no-form-waiver
   {:controlled 174 :non-tier3 55 :tier3 119}
   :b51-vector-frontier-rejected-after-no-form-waiver
   {:controlled 174 :non-tier3 55 :tier3 119}})

(def ^:private root3-boundary-reasons
  #{:malformed-request :malformed-request-plan :prefix-mismatch
    :schedule-mismatch :cardinality-mismatch})

(def ^:private diagnostic-rules
  #{"C6-CORE-SHAPE" "C6-LOWERING-GAP" "L7-DUP-BINDING"
    "L7-PATTERN-TYPE" "C6-EVAL-ORDER" "C6-ORIGIN"
    "L2-RECUR-TARGET" "C6-EFFECT-DROP" "C6-UNSAFE-DROP" "C6-VERIFY"})

(def ^:private required-semantic-tokens
  ;; Attempts 5, 11, and 13 name the third owner branch :non-definition.
  #{:legacy-v16-accepted
    :b51-vector-frontier-rejected
    :legacy-v16-accepted-after-no-form-waiver
    :b51-vector-frontier-rejected-after-no-form-waiver
    :expanded-defn :source-def :non-definition
    :owning-fragment-local :authenticated-external
    :current-namespace-binding :fully-qualified-namespace-binding
    :profile-allowed-core-binding :alias-qualified-required-binding
    :explicit-foreign-import-binding
    :vector-node :binding-leaf :wildcard-leaf
    :vector-required :vector-width-mismatch :publish-none
    :slot-atomic-publication :loop-atomic-transfer
    :duplicate-vector-binding-name :unsupported-vector-rest
    :unsupported-nested-pattern :missing-recur-target
    :recur-arity-mismatch :recur-not-tail
    :legacy-v16-equivalence})

(def ^:private bound-catalog
  [{:check :owner-slot-limit :maximum 1024 :crossing 1025
    :owner :predecessor-b47}
   {:check :module-slot-limit :maximum 1024 :crossing 1025 :owner :v18}
   {:check :vector-width-limit :maximum 1024 :crossing 1025
    :owner :predecessor-b47}
   {:check :slot-pattern-node-limit :maximum 1024 :crossing 1025
    :owner :predecessor-b47}
   {:check :module-extraction-limit :maximum 65536 :crossing 65537
    :owner :predecessor-b47}
   {:check :module-binding-leaf-limit :maximum 2048 :crossing 2049 :owner :v18}
   {:check :vector-depth-limit :maximum 256 :crossing 257
    :owner :predecessor-b47}
   {:check :extraction-path-length-limit :maximum 256 :crossing 257
    :owner :predecessor-b47}])

(defn- path [relative]
  (str (.normalize (.toAbsolutePath (.resolve @root relative)))))

(defn- fixture-path [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- source-bytes [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- fixture-basenames [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(str/ends-with? % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

(defn- read-gravity-source-forms [relative]
  (with-open [reader
              (java.io.PushbackReader. (io/reader (path relative)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form)
          forms
          (recur (conj forms form)))))))

(def ^:private source-forms
  (delay (read-gravity-source-forms source-relative-path)))

(defn- namespace-clauses [ns-form]
  (into {} (map (fn [clause] [(first clause) (second clause)]))
        (drop 2 ns-form)))

(defn- definitions-by-name [forms]
  (into {}
        (map (juxt second identity))
        (filter #(and (seq? %) (contains? #{'def 'defn 'defn-} (first %)))
                forms)))

(defn- source-direct-callees [definition-names value]
  (cond
    (and (seq? value) (= 'quote (first value))) #{}
    (seq? value)
    (into
     (if (and (symbol? (first value))
              (contains? definition-names (first value)))
       #{(first value)} #{})
     (mapcat #(source-direct-callees definition-names %) (rest value)))
    (coll? value)
    (into #{} (mapcat #(source-direct-callees definition-names %) value))
    :else #{}))

(defn- source-direct-project-callees [value]
  (cond
    (and (seq? value) (= 'quote (first value))) #{}
    (seq? value)
    (into
     (if (and (symbol? (first value))
              (.startsWith (name (first value)) "sh07-"))
       #{(first value)} #{})
     (mapcat source-direct-project-callees (rest value)))
    (coll? value)
    (into #{} (mapcat source-direct-project-callees value))
    :else #{}))

(defn- source-definition-closure [definitions root-symbol]
  (let [definition-names (set (keys definitions))]
    (loop [pending [root-symbol] members #{} edges #{} undefined #{}]
      (if (empty? pending)
        {:members members :edges edges :undefined-callees undefined}
        (let [function (peek pending)
              pending (pop pending)]
          (if (contains? members function)
            (recur pending members edges undefined)
            (let [form (get definitions function)
                  callees
                  (source-direct-callees definition-names
                                         (nthnext form 2))
                  project-callees
                  (source-direct-project-callees (nthnext form 2))
                  missing
                  (set/difference
                   project-callees definition-names
                   #{'sh07-declared-digest-hash})]
              (recur (into pending callees)
                     (conj members function)
                     (into edges (map #(vector function %) callees))
                     (into undefined missing)))))))))

(defn- rename-r6-copy-symbols [value]
  (cond
    (symbol? value)
    (let [text (name value)
          prefix "sh07-b51-r6-own-"]
      (if (.startsWith text prefix)
        (symbol (str "sh07-b51-" (subs text (count prefix))))
        value))

    (list? value) (apply list (map rename-r6-copy-symbols value))
    (vector? value) (mapv rename-r6-copy-symbols value)
    (map? value)
    (into (empty value)
          (map (fn [[key item]]
                 [(rename-r6-copy-symbols key)
                  (rename-r6-copy-symbols item)]))
          value)
    (set? value) (set (map rename-r6-copy-symbols value))
    :else value))

(defn- direct-source-runtime
  "Evaluate one closed Gravity helper family without the pin-gated compiler.

  Checked-core's authored subset is Clojure-readable.  Predeclaring every
  function in the mechanically measured closure permits mutually recursive
  definitions while the only injected operations are the governed declared
  digest, canonical UTF-8 text comparator, and exact UTF-8 census builtins.
  No producer or host materializer is supplied.
  "
  [root-symbol]
  (let [definitions (definitions-by-name @source-forms)
        closure (source-definition-closure definitions root-symbol)
        runtime-symbol (gensym "gravity.sh07.b51.r6-direct-")
        runtime-ns (create-ns runtime-symbol)]
    (binding [*ns* runtime-ns]
      (clojure.core/refer 'clojure.core)
      (intern runtime-ns 'sh07-declared-digest-hash
              (fn [hash-input]
                (bootstrap/reader-canonical-hash hash-input)))
      (intern runtime-ns 'sh07-canonical-text-compare
              bootstrap/p15-s23-sh07-canonical-text-compare)
      (intern runtime-ns 'sh07-observe-utf8-byte-count
              bootstrap/p15-s23-sh07-observe-utf8-byte-count)
      (doseq [function (:members closure)]
        (intern runtime-ns function))
      (doseq [function (:members closure)]
        (eval (get definitions function))))
    {:namespace runtime-ns
     :closure closure
     :invoke
     (fn [function arguments]
       (apply (deref (ns-resolve runtime-ns function)) arguments))}))

(defn- function-shape [form]
  (let [params (nth form 2 nil)]
    {:arity (count params) :params params}))

(defn- compile-plan []
  (let [source-path (path source-relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private checked-core-plan (delay (compile-plan)))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b51-v18-test
    :compiler-artifact-plan? true}
   @checked-core-plan function arguments))

(defn- ordinary-core-form-for-authenticated-form [indexes form]
  (let [kind (:kind form)
        operator
        (when (= kind :list)
          (invoke 'sh07-b51-authenticated-operator-symbol [indexes form]))]
    (cond
      (= kind :symbol) :reference
      (contains? #{:vector :map :set} kind) :collection-literal
      (not= kind :list) :literal
      (= operator 'quote) :quote
      (= operator 'fn) :fn
      (= operator 'def) :def
      (= operator 'if) :if
      (= operator 'do) :do
      (= operator 'let) :let
      (= operator 'loop) :loop
      (= operator 'recur) :recur
      (= operator 'var) :var
      (= operator 'set!) :set!
      (= operator 'throw) :throw
      (= operator 'try) :try
      (= operator 'match) :match
      (= (:kind (get-in indexes [:forms :by-primary
                                 (first (:child-form-ids form))]))
         :keyword)
      :keyword-map-lookup
      :else :call)))

(defn- exact-abi? [value status tag]
  (and (= #{:artifact :domain :schema-version :status :tag :value}
          (set (keys value)))
       (= :gravity/sh07-b51-entrypoint-result-v18 (:artifact value))
       (= :gravity/sh07-b51-entrypoint-abi-v18 (:domain value))
       (= 18 (:schema-version value))
       (= status (:status value))
       (= tag (:tag value))))

(defn- exact-boundary? [value tag boundary reason]
  (and (exact-abi? value :boundary-rejected tag)
       (= #{:status :boundary :reason :recursive-diagnostic-forbidden}
          (set (keys (:value value))))
       (= :boundary-rejected (get-in value [:value :status]))
       (= boundary (get-in value [:value :boundary]))
       (= reason (get-in value [:value :reason]))
       (true? (get-in value [:value :recursive-diagnostic-forbidden]))))

(defn- resolve-digest-plan
  [digest-requests]
  ;; Test-local mechanical stage-2 executor: Gravity authenticates and exposes
  ;; each hash input; the host only computes its opaque canonical digest.
  (loop [remaining digest-requests resolved []]
    (if (empty? remaining)
      resolved
      (let [request (first remaining)
            envelope
            (invoke 'sh07-b51-resolve-digest-preimage
                    [request digest-requests resolved])]
        (when-not (exact-abi? envelope :accepted :digest-preimage-resolved)
          (throw
           (ex-info "B51-v18 digest request was rejected at the host boundary"
                    {:id "SH07-B51-V18-DIGEST-BOUNDARY"
                     :request request
                     :envelope envelope})))
        (let [resolution (:value envelope)
              hash-input (:hash-input resolution)
              digest (bootstrap/reader-canonical-hash hash-input)]
          (recur (rest remaining) (conj resolved digest)))))))

(defn- resolve-digest-plan-direct
  [runtime digest-requests]
  (loop [remaining digest-requests resolved []]
    (if (empty? remaining)
      resolved
      (let [request (first remaining)
            envelope
            ((:invoke runtime) 'sh07-b51-resolve-digest-preimage
             [request digest-requests resolved])]
        (when-not (exact-abi? envelope :accepted :digest-preimage-resolved)
          (throw
           (ex-info "B51-v18 direct digest request was rejected"
                    {:id "SH07-B51-V18-DIRECT-DIGEST-BOUNDARY"
                     :request request :envelope envelope})))
        (recur (rest remaining)
               (conj resolved
                     (bootstrap/reader-canonical-hash
                      (get-in envelope [:value :hash-input]))))))))

(defn- c11-raw-pr-str-digest [value]
  (str "sha256:" (bootstrap/sha256-hex (pr-str value))))

(defn- authenticated-internal-call-fixture
  [enclosing-function builtin]
  (let [compiler-definitions
        (definitions-by-name
         (read-gravity-source-forms "bootstrap/gravity/p15_s23/compiler.gravity"))
        emitter (nth (get compiler-definitions
                          'p15-s23-stage2-plan-emitter) 2)
        checked-source (slurp source-relative-path)
        macro-artifact
        (bootstrap/macro-source-artifact source-relative-path checked-source)
        source-module
        (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))
        function-table (bootstrap/stage0-function-table source-module)
        source-authority
        (bootstrap/p15-s23-sh07-compiler-source-authority
         source-relative-path checked-source source-module function-table)
        module
        (assoc source-module
               :function-table function-table
               :p15-s23-sh07-source-authority source-authority)
        source-definition (get function-table enclosing-function)
        source-call
        (first
         (filter #(and (seq? %) (= builtin (first %)))
                 (tree-seq coll? seq (:body source-definition))))
        _ (when-not source-call
            (throw (ex-info "declared internal builtin occurrence is absent"
                            {:function enclosing-function :builtin builtin})))
        instruction
        (bootstrap/p15-s23-stage2-seed-compile-expr
         emitter
         (assoc module
                :p15-s23-sh07-enclosing-function enclosing-function
                :p15-s23-sh07-enclosing-definition source-definition)
         (set (filter symbol? (rest source-call)))
         source-call)
        definition (assoc source-definition :instructions [instruction])
        source-sha256 (str "sha256:" (bootstrap/sha256-hex checked-source))
        plan-base
        {:kind :gravity/stage2-compiler-artifact-plan
         :compiler-artifact-plan? true
         :entrypoint 'main
         :source {:sha256 source-sha256}
         :compiler
         {:owner :gravity-source
          :stage :p15-s23-stage2-expression-lowering
          :compiled-by :clojure-stage0-seed
          :generic-bridge-residual? true
          :artifact-hash
          (bootstrap/p15-s23-stage2-compiler-artifact-expected-artifact-hash)
          :source-content-hash
          bootstrap/p15-s23-stage2-compiler-artifact-expected-source-content-hash
          :semantic-hash
          bootstrap/p15-s23-stage2-compiler-artifact-expected-semantic-hash}
         :module (select-keys module
                              [:module :profile :target :effects
                               :capabilities :exports :safety])
         :functions {enclosing-function definition}
         :instruction-summary {}
         :effect-summary {:declared #{} :inferred #{} :capabilities #{}}}
        plan-id
        ((var-get (ns-resolve 'gravity.bootstrap
                              'p15-s23-sh07-runtime-plan-id)) plan-base)
        plan (assoc plan-base :plan-id plan-id)
        binding {:plan plan :source-content-hash source-sha256}]
    {:plan plan :instruction instruction :binding binding}))

(def ^:private declared-digest-call-fixture
  (delay
    (authenticated-internal-call-fixture
     'sh07-b51-success-digest-replay-exact?
     'sh07-declared-digest-hash)))

(defn- gravity-declared-digest [handler value]
  (let [{:keys [plan instruction binding]} @declared-digest-call-fixture
        cached-binding-var
        (ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding)]
    (with-redefs-fn
      {cached-binding-var (delay binding)}
      #(handler {:sh07-core-binding binding}
                plan instruction [value]))))

(defn- function-operation-records [plan function]
  ;; Closure measurement needs only the instruction kinds and named callees.
  ;; Walk the already-authenticated Stage-2 instruction tree mechanically so
  ;; newly admitted loop arities do not depend on the older host executor's
  ;; runtime rule vocabulary.  Literal payloads are opaque and never walked.
  (letfn [(operations [value]
            (cond
              (vector? value) (vec (mapcat operations value))
              (and (map? value) (= :literal (:op value))) []
              (map? value)
              (let [operation
                    (case (:op value)
                      :function-call
                      [{:op :function-call :callee (:function value)}]
                      :builtin-call
                      [{:op :builtin-call :builtin (:function value)}]
                      [])]
                (into operation (mapcat operations (vals value))))
              :else []))]
    (operations (get-in plan [:functions function]))))

(defn- measured-closure [plan roots]
  (loop [pending (vec (reverse roots)) members #{} edges #{} builtins #{}
         call-site-count 0]
    (if (empty? pending)
      (let [ordered-members (vec (sort-by str members))]
        {:members ordered-members
         :functions (into (sorted-map)
                          (map (fn [name]
                                 [name (get-in plan [:functions name])]))
                          ordered-members)
         :edges (vec (sort-by pr-str edges))
         :edge-set (into (sorted-set) edges)
         :builtins (vec (sort-by str builtins))
         :call-site-count call-site-count})
      (let [function (peek pending)
            pending (pop pending)]
        (if (contains? members function)
          (recur pending members edges builtins call-site-count)
          (let [operations (function-operation-records plan function)
                callees (keep #(when (= :function-call (:op %)) (:callee %))
                              operations)
                called-builtins
                (keep #(when (= :builtin-call (:op %)) (:builtin %))
                      operations)]
            (when-not (every? #(contains? (:functions plan) %) callees)
              (throw
               (ex-info "B51-v18 executable closure contains an unresolved call"
                        {:id "SH07-B51-V18-CLOSURE-CALL"
                         :function function})))
            (recur (into pending (reverse callees))
                   (conj members function)
                   (into edges (map #(vector function %) callees))
                   (into builtins called-builtins)
                   (+ call-site-count (count callees)))))))))

(defn- sha [digit]
  (str "sha256:" (apply str (repeat 64 digit))))

(defn- internal-reference [ordinal]
  {:artifact :gravity/sh07-internal-digest-reference
   :schema-version 1
   :ordinal ordinal
   :authority :sh07-digest-resolver})

(defn- internal-reference-value? [value]
  (and (map? value)
       (= #{:artifact :schema-version :ordinal :authority}
          (set (keys value)))
       (= :gravity/sh07-internal-digest-reference (:artifact value))
       (= 1 (:schema-version value))
       (integer? (:ordinal value))
       (<= 0 (:ordinal value))
       (= :sh07-digest-resolver (:authority value))))

(defn- internal-reference-occurrences [value]
  (filterv internal-reference-value? (tree-seq coll? seq value)))

(defn- resolved-id-for-purpose [requests resolved purpose]
  (let [matches
        (keep-indexed
         (fn [ordinal request]
           (when (= purpose (:purpose request))
             {:ordinal ordinal :request request :resolved-id
              (get resolved ordinal)}))
         requests)]
    (when (= 1 (count matches)) (first matches))))

(defn- predecessor-descriptor [closure]
  {:domain :gravity/sh07-b51-predecessor-executable-closure-v18
   :schema-version 18
   :base-commit "3d7d4b532176841a45db71fa3ca37f7300d2d2c8"
   :source-relative-path source-relative-path
   :roots (vec (drop 3 predecessor-exports))
   :root-symbols-hash
   "sha256:d92f6a8991c28e6514e2df9f7b4ca6aeb0baccf9f75cdce47779fd0337c9667b"
   :plan-function-count 305
   :function-member-count 298
   :function-names-hash
   "sha256:aba0868e849b735529ded667ba6aadda30451c8af67dd9686abfdb2d7a97b1fc"
   :function-shapes-hash
   "sha256:79509cc6a17101d22220b3f60278d77547612ab49f6b57d719d463f5cefa5500"
   :selected-functions-hash
   "sha256:34bacc392d81d22757dd7d8f75d82a4ff9ddc55dbe8ececc79daa294496283b1"
   :edge-count 849
   :edge-carrier (:edge-set closure)
   :edge-carrier-kind :canonical-sorted-set-of-pair-vectors
   :edge-hash
   "sha256:5f7a87d2a3fc9b43469c5a46109f7b1d8edd1860631d20508d6222a2c2f465ef"
   :roots-members-functions-descriptor-hash
   "sha256:13a9131f49c09fd8c62c365fe68442cfee82cc34cdf73679dcf5c90fe46cdb1c"
   :referenced-builtin-name-count 32
   :referenced-builtin-names-hash
   "sha256:c8769b21181d28b6129f6c4f8e2c42bf0860381d0005f67c63f1ef30e844a28e"
   :contract-form-names-hash
   "sha256:20169a279466cae667cb1e41783b862c9f6dd9fe067574e170282846868f5e4a"
   :contract-form-reader-hashes
   ["sha256:d936967af4da29e0fd7f0193d470c0ca17315a48230dcf3b052e0d9ae35cfd26"
    "sha256:f60ca55802342b02f742eb0a7cc2f9fff233b50f2b64cbbb2340ed56e86135ea"
    "sha256:815a4611e6c950a579d10d4c375b06bbdde5cd3f321987a53ca7c950f4935d8b"]
   :contract-form-reader-vector-hash
   "sha256:79995e16228c5217df02171e5833eff49c5e3f03118566f54b36ea02b5887079"
   :contract-core-hash
   "sha256:f20c2e57d820edef6461f83de84fe1973a58c02a2a2e9dd702b0a235364e607f"
   :contract-bounds-hash
   "sha256:293bb59806e517d29988dd28a78f0005f0f5b8665ab02f70a82c2b8d91ce77b9"
   :contract-diagnostics-hash
   "sha256:4a3b03150d3f7e016b5c1e5a17fbea22ae2d73d63b71b1b6c6738630767562c3"
   :combined-contracts-hash
   "sha256:f8ac391550f7205c2ad0b56689e9109e71927ff948fa7913da80710aaf51e7ff"
   :filtered-module-header-export-hash
   "sha256:e12f3dde6a0afa798e034b9e60311847535832ec9adffc10e221784c3665c58f"
   :executable-closure-descriptor-id
   "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234"
   :excluded-plan-members
   '[sh07-find-alias sh07-lower-top-level-forms
     sh07-resolve-match-branch-vector
     sh07-resolve-match-decision-skeleton-vector
     sh07-resolve-match-pattern-vector sh07-scope-binding-ids
     sh07-utf8-byte-width]})

(defn- predecessor-contract []
  {:domain :gravity/sh07-b51-predecessor-contract-v18
   :schema-version 18
   :adapter-contract :gravity/sh07-to-c6-core-products-v16
   :identity-preimage-domain :gravity/sh07-b47-canonical-core-v16
   :actual-core-artifact-kind :gravity/sh07-canonical-core-artifact
   :actual-wrapper-artifact-kind nil
   :request-artifact :gravity/sh07-authenticated-sh06-core-request
   :request-schema-version 15
   :lowering-rule :sh07-b47-function-call-recursion-products
   :entrypoints (vec (drop 3 predecessor-exports))
   :source-contract-forms
   {:contract-form-count 3
    :contract-form-names
    '[sh07-core-contract sh07-core-bounds sh07-core-diagnostic-catalog]
    :contract-form-reader-hashes
    ["sha256:d936967af4da29e0fd7f0193d470c0ca17315a48230dcf3b052e0d9ae35cfd26"
     "sha256:f60ca55802342b02f742eb0a7cc2f9fff233b50f2b64cbbb2340ed56e86135ea"
     "sha256:815a4611e6c950a579d10d4c375b06bbdde5cd3f321987a53ca7c950f4935d8b"]
    :contract-form-reader-vector-hash
    "sha256:79995e16228c5217df02171e5833eff49c5e3f03118566f54b36ea02b5887079"
    :contract-value-hashes-by-name
    {'sh07-core-bounds
     "sha256:293bb59806e517d29988dd28a78f0005f0f5b8665ab02f70a82c2b8d91ce77b9"
     'sh07-core-contract
     "sha256:f20c2e57d820edef6461f83de84fe1973a58c02a2a2e9dd702b0a235364e607f"
     'sh07-core-diagnostic-catalog
     "sha256:4a3b03150d3f7e016b5c1e5a17fbea22ae2d73d63b71b1b6c6738630767562c3"}
    :combined-contracts-hash
    "sha256:f8ac391550f7205c2ad0b56689e9109e71927ff948fa7913da80710aaf51e7ff"}
   :executable-closure-descriptor-id
   "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234"})

(defn- semantic-contract-values []
  (let [definitions (definitions-by-name @source-forms)]
    (into (sorted-map)
          (for [name '[sh07-core-bounds sh07-core-contract
                       sh07-core-diagnostic-catalog]]
            [name (nth (get definitions name) 2)]))))

(defn- v18-executable-carrier [plan closure]
  (let [module-contract
        {:module 'gravity.checked-core
         :profile :meta :target :jvm :effects #{} :capabilities #{}
         :safety :safe
         :exports (vec (concat predecessor-exports public-roots))}
        semantic-closure
        {:domain :gravity/sh07-b51-v18-executable-semantic-closure-v18
         :schema-version 18
         :roots public-roots
         :root-symbols-hash (bootstrap/reader-canonical-hash public-roots)
         :members (:members closure)
         :functions (:functions closure)
         :runtime-call-edges (:edges closure)
         :referenced-builtins (:builtins closure)
         :module-contract module-contract
         :semantic-contracts (semantic-contract-values)}
        semantic-hash
        (bootstrap/reader-canonical-hash
         {:domain :gravity/sh07-b51-v18-semantic-closure-hash-v18
          :schema-version 18
          :semantic-closure semantic-closure})
        source-path (path source-relative-path)
        bytes (source-bytes source-path)]
    {:domain :gravity/sh07-b51-v18-executable-closure-carrier-v18
     :schema-version 18
     :semantic-closure semantic-closure
     :semantic-closure-hash semantic-hash
     :source-relative-path source-relative-path
     :semantic-base-commit "3d7d4b532176841a45db71fa3ca37f7300d2d2c8"
     :governance-parent-commit
     "4062d23eadf7d8d013e1163006423db416d88ee2"
     :physical-census
     {:source-byte-count (alength bytes)
      :source-content-hash
      (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
      :source-revision-id
      (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
      :whole-file-plan-hash (bootstrap/reader-canonical-hash plan)
      :whole-file-function-hash
      (bootstrap/reader-canonical-hash (:functions plan))
      :plan-function-count (count (:functions plan))
      :plan-member-count (count (:functions plan))
      :runtime-call-site-count (:call-site-count closure)
      :runtime-edge-count (count (:edges closure))}}))

(defn- predecessor-observation [request]
  (let [direct (invoke 'sh07-build-core-template [request])
        structural
        (try
          {:returned
           (bootstrap/sh07-core-run-structural-request-for-test request)
           :thrown nil}
          (catch clojure.lang.ExceptionInfo exception
            {:returned nil :thrown (ex-data exception)}))]
    {:direct-template-observation
     {:entrypoint 'sh07-build-core-template
      :request request :returned direct :thrown nil}
     :structural-runner-observation
     {:entrypoint 'sh07-core-run-structural-request-for-test
      :request request
      :returned (:returned structural)
      :thrown (:thrown structural)}}))

(defn- raw-carrier [family basename extension]
  (let [source-path (fixture-path family basename extension)
        resolution (bootstrap/sh06-resolution-file-artifact source-path)
        report (bootstrap/sh06-resolution-artifact-verification resolution)
        request (bootstrap/sh07-core-authenticated-request resolution)
        plan @checked-core-plan
        predecessor-closure
        (measured-closure plan (vec (drop 3 predecessor-exports)))
        v18-closure (measured-closure plan public-roots)
        bytes (source-bytes source-path)]
    {:artifact :gravity/sh07-b51-raw-adapter-request-v18
     :schema-version 18
     :authenticated-sh06-request request
     :authenticated-sh06-verification-report report
     :sh06-resolution-artifact resolution
     :predecessor-executable-descriptor
     (predecessor-descriptor predecessor-closure)
     :v18-executable-descriptor (v18-executable-carrier plan v18-closure)
     :predecessor-contract-carrier (predecessor-contract)
     :predecessor-raw-outcome (predecessor-observation request)
     :physical-invocation
     {:source-path source-path
      :source-byte-count (alength bytes)
      :source-content-hash
      (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
      :session-id "checkpoint-c-test-session"
      :attempt-id "checkpoint-c-test-attempt"
      :invocation-id (str "checkpoint-c-test-" basename extension)}
     :scope :sh07-b51-vector-destructuring}))

(defn- private-foundation-raw-carrier [family basename]
  (let [source-path (fixture-path family basename ".gravity")
        resolution (bootstrap/sh06-resolution-file-artifact source-path)
        report (bootstrap/sh06-resolution-artifact-verification resolution)
        request (bootstrap/sh07-core-authenticated-request resolution)
        ;; This private seam deliberately exercises the authenticated numbered
        ;; composer without depending on the unfinished public v18 closure.
        ;; The descriptor validator pins the predecessor census/hash tuple; a
        ;; fixed 849-edge carrier is sufficient here because public end-to-end
        ;; tests separately require the mechanically measured B47 closure.
        predecessor-closure
        {:edge-set
         (set
          (map (fn [ordinal]
                 [(symbol (str "fixed-predecessor-parent-" ordinal))
                  (symbol (str "fixed-predecessor-child-" ordinal))])
               (range 849)))}
        bytes (source-bytes source-path)
        module-contract
        {:module 'gravity.checked-core
         :profile :meta :target :jvm :effects #{} :capabilities #{}
         :safety :safe
         :exports (vec (concat predecessor-exports public-roots))}
        semantic-closure
        {:domain :gravity/sh07-b51-v18-executable-semantic-closure-v18
         :schema-version 18
         :roots public-roots
         :root-symbols-hash (bootstrap/reader-canonical-hash public-roots)
         :members [] :functions {} :runtime-call-edges []
         :referenced-builtins [] :module-contract module-contract
         :semantic-contracts (semantic-contract-values)}
        v18-executable-descriptor
        {:domain :gravity/sh07-b51-v18-executable-closure-carrier-v18
         :schema-version 18 :semantic-closure semantic-closure
         :semantic-closure-hash
         (bootstrap/reader-canonical-hash semantic-closure)
         :source-relative-path source-relative-path
         :semantic-base-commit
         "3d7d4b532176841a45db71fa3ca37f7300d2d2c8"
         :governance-parent-commit
         "4062d23eadf7d8d013e1163006423db416d88ee2"
         :physical-census
         {:source-byte-count 0 :source-content-hash (sha \a)
          :source-revision-id (sha \b) :whole-file-plan-hash (sha \c)
          :whole-file-function-hash (sha \d) :plan-function-count 0
          :plan-member-count 0 :runtime-call-site-count 0
          :runtime-edge-count 0}}]
    {:artifact :gravity/sh07-b51-raw-adapter-request-v18
     :schema-version 18
     :authenticated-sh06-request request
     :authenticated-sh06-verification-report report
     :sh06-resolution-artifact resolution
     :predecessor-executable-descriptor
     (predecessor-descriptor predecessor-closure)
     :v18-executable-descriptor v18-executable-descriptor
     :predecessor-contract-carrier (predecessor-contract)
     :predecessor-raw-outcome
     (predecessor-observation request)
     :physical-invocation
     {:source-path source-path :source-byte-count (alength bytes)
      :source-content-hash
      (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
      :session-id "checkpoint-c-private-session"
      :attempt-id "checkpoint-c-private-attempt"
      :invocation-id (str "checkpoint-c-private-" basename)}
     :scope :sh07-b51-vector-destructuring}))

(def ^:private nested-let-carrier
  (delay (raw-carrier "accepted" "nested-empty-wildcard-let" ".gravity")))

(def ^:private nested-let-foundation-carrier
  (delay (private-foundation-raw-carrier
          "accepted" "nested-empty-wildcard-let")))

(defn- private-no-form-raw-carrier [basename]
  (let [carrier (private-foundation-raw-carrier "accepted" basename)
        observation (:predecessor-raw-outcome carrier)
        direct-path [:direct-template-observation :returned]
        direct (get-in observation direct-path)
        request-preimage (get-in direct [:digest-requests 0 :preimage])
        preimage-facts {:reason :authoritative-record-shape}
        preimage
        (assoc request-preimage :rule "C6-VERIFY" :facts preimage-facts)
        diagnostic (first (:diagnostics direct))
        diagnostic-facts
        {:reason :authoritative-record-shape
         :semantic-rule nil
         :rule-specific preimage-facts
         :source-revision-id (:source-revision-id preimage)
         :sh06-artifact-id (:sh06-artifact-id preimage)
         :fail-closed true}
        unresolved-diagnostic
        (assoc diagnostic :rule "C6-VERIFY" :facts diagnostic-facts)
        resolved-diagnostic
        (assoc unresolved-diagnostic :diagnostic-id-request (sha \e))
        coherent-observation
        (-> observation
            (assoc-in (into direct-path [:digest-requests 0 :preimage])
                      preimage)
            (assoc-in (into direct-path [:diagnostics])
                      [unresolved-diagnostic])
            (assoc-in [:structural-runner-observation :returned] nil)
            (assoc-in [:structural-runner-observation :thrown]
                      resolved-diagnostic))]
    (assoc carrier :predecessor-raw-outcome coherent-observation)))

(defn- pin-independent-predecessor-observation
  [runtime request]
  (let [direct ((:invoke runtime) 'sh07-build-core-template [request])
        structural
        (try
          {:returned
           (bootstrap/sh07-core-run-structural-request-for-test request)
           :thrown nil}
          (catch clojure.lang.ExceptionInfo exception
            {:returned nil :thrown (ex-data exception)}))]
    {:direct-template-observation
     {:entrypoint 'sh07-build-core-template
      :request request :returned direct :thrown nil}
     :structural-runner-observation
     {:entrypoint 'sh07-core-run-structural-request-for-test
      :request request :returned (:returned structural)
      :thrown (:thrown structural)}}))

(defn- pin-independent-private-no-form-raw-carrier [basename]
  (let [runtime (direct-source-runtime 'sh07-build-core-template)]
    (with-redefs
      [predecessor-observation
       (fn [request]
         (pin-independent-predecessor-observation runtime request))]
      (private-no-form-raw-carrier basename))))

(def ^:private projection-cache (atom {}))

(defn- fixture-projection
  ([basename] (fixture-projection "accepted" basename))
  ([family basename]
   (let [cache-key [family basename]]
    (or (get @projection-cache cache-key)
      (let [source-path (fixture-path family basename ".gravity")
            resolution (bootstrap/sh06-resolution-file-artifact source-path)
            report (bootstrap/sh06-resolution-artifact-verification resolution)
            request (bootstrap/sh07-core-authenticated-request resolution)
            value {:resolution resolution
                   :report report
                   :request request}]
        (swap! projection-cache assoc cache-key value)
        value)))))

(defn- fixture-indexes [projection]
  (invoke 'sh07-b51-index-request [(:request projection)]))

(defn- operator-enclosing-forms [request indexes operator-name]
  (filterv
   (fn [form]
     (let [operator-id (first (:child-form-ids form))
           operator
           (get-in indexes [:forms :by-primary operator-id])]
       (and (= :list (:kind form))
            (= :symbol (:kind operator))
            (= operator-name (:value operator)))))
   (:forms request)))

(defn- let-enclosing-forms [request indexes]
  (operator-enclosing-forms request indexes 'let))

(defn- unique-pattern-form [request pattern]
  (let [matches (filterv #(= pattern (:value %)) (:forms request))]
    (when (= 1 (count matches)) (first matches))))

(defn- unique-form-fragment [request form]
  (let [matches
        (filterv #(some #{(:form-id form)} (:form-ids %))
                 (:fragment-manifest request))]
    (when (= 1 (count matches)) (first matches))))

(defn- authenticated-preflight-slot [basename operator-name]
  (let [projection (fixture-projection basename)
        request (:request projection)
        indexes (fixture-indexes projection)
        enclosing
        (first (operator-enclosing-forms request indexes operator-name))
        owner-result
        (invoke 'sh07-b51-owner-coordinate [request indexes enclosing])
        binding-vector
        (get-in indexes [:forms :by-primary
                         (second (:child-form-ids enclosing))])
        pattern-form
        (get-in indexes [:forms :by-primary
                         (first (:child-form-ids binding-vector))])
        fragment (unique-form-fragment request pattern-form)
        pattern
        (invoke 'sh07-b51-enumerate-slot-pattern
                [request indexes fragment pattern-form])]
    {:request request
     :indexes indexes
     :enclosing enclosing
     :owner-result owner-result
     :pattern-form pattern-form
     :fragment fragment
     :pattern pattern
     :preflight-slot
     {:owner-coordinate (:coordinate owner-result)
      :fragment fragment
      :pattern pattern}}))

(defn- resolution-classification-rows [projection]
  (let [request (:request projection)
        indexes (fixture-indexes projection)
        authorization
        (invoke 'sh07-b51-authorize-all-resolutions
                [(:resolution projection) request indexes])]
    {:authorization authorization
     :rows
     (mapv (fn [resolution classification]
             {:resolution resolution
              :binding (get-in indexes
                               [:bindings :by-primary
                                (:binding-id resolution)])
              :classification classification})
           (:resolution-table request)
           (:classifications authorization))}))

(defn- nested-slot-context []
  (let [projection (fixture-projection "nested-empty-wildcard-let")
        request (:request projection)
        indexes (fixture-indexes projection)
        enclosing (first (let-enclosing-forms request indexes))
        owner-result
        (invoke 'sh07-b51-owner-coordinate [request indexes enclosing])
        binding-vector
        (get-in indexes [:forms :by-primary
                         (second (:child-form-ids enclosing))])
        pattern-form
        (get-in indexes [:forms :by-primary
                         (first (:child-form-ids binding-vector))])
        initializer
        (get-in indexes [:forms :by-primary
                         (second (:child-form-ids binding-vector))])
        fragment (unique-form-fragment request pattern-form)
        pattern
        (invoke 'sh07-b51-enumerate-slot-pattern
                [request indexes fragment pattern-form])
        coordinate-result
        (invoke 'sh07-b51-build-slot-coordinate
                [(:coordinate owner-result) 0 pattern-form initializer])]
    {:projection projection :request request :indexes indexes
     :enclosing enclosing :owner-result owner-result
     :owner (:coordinate owner-result) :binding-vector binding-vector
     :pattern-form pattern-form :initializer initializer
     :fragment fragment :pattern pattern
     :slot-coordinate (:slot-coordinate coordinate-result)}))

(defn- mechanical-foundation-reference-layout [slots]
  (let [product-id-vectors
        (mapv
         (fn [slot-index slot]
           (mapv internal-reference
                 (range (+ 500 (* slot-index 32))
                        (+ 500 (* slot-index 32)
                           2
                           (get-in slot [:pattern :vector-node-count])
                           (get-in slot [:pattern :terminal-count])))))
         (range) slots)]
    {:predecessor-authority-id (internal-reference 1)
     :owner-core-node-id (internal-reference 2)
     :initializer-core-node-ids
     (mapv #(internal-reference (+ 10 %)) (range (count slots)))
     :slot-ids
     (mapv #(internal-reference (+ 20 %)) (range (count slots)))
     :extraction-id-vectors
     (mapv (fn [slot-index slot]
             (mapv internal-reference
                   (range (+ 100 (* slot-index 32))
                          (+ 100 (* slot-index 32)
                             (get-in slot [:pattern :extraction-count])))))
           (range) slots)
     :runtime-check-id-vectors
     (mapv (fn [slot-index slot]
             (mapv internal-reference
                   (range (+ 200 (* slot-index 32))
                          (+ 200 (* slot-index 32)
                             (get-in slot [:pattern :vector-node-count])))))
           (range) slots)
     :publication-event-ids
     (mapv #(internal-reference (+ 300 %)) (range (count slots)))
     :projection-id-vectors
     (mapv (fn [slot products]
             (let [first-projection
                   (+ 1 (get-in slot [:pattern :vector-node-count]))
                   terminal-count
                   (get-in slot [:pattern :terminal-count])]
               (subvec products first-projection
                       (+ first-projection terminal-count))))
           slots product-id-vectors)
     :product-node-id-vectors product-id-vectors
     :first-global-slot-ordinal 0
     :first-global-extraction-ordinal 0
     :first-global-runtime-check-ordinal 0
     :first-global-publication-ordinal 0}))

(defn- loop-recur-context
  ([] (loop-recur-context "accepted" "nested-empty-wildcard-loop-recur"))
  ([family basename]
  (let [projection
        (fixture-projection family basename)
        request (:request projection)
        indexes (fixture-indexes projection)
        loop-form
        (first (operator-enclosing-forms request indexes 'loop))
        skeleton
        (invoke 'sh07-b51-loop-owner-skeleton [request indexes loop-form])
        slot-result
        (invoke 'sh07-b51-let-slot-patterns [request indexes skeleton])
        layout (mechanical-foundation-reference-layout (:slots slot-result))
        foundation
        (invoke 'sh07-b51-lower-loop-slots-foundation
                [request loop-form layout])
        recur-form
        (first (operator-enclosing-forms request indexes 'recur))
        recur-core-node-id (internal-reference 700)
        authenticated-recur
        (invoke 'sh07-b51-authenticated-loop-recur
                [request indexes foundation recur-form recur-core-node-id])]
    {:projection projection
     :request request
     :indexes indexes
     :loop-form loop-form
     :skeleton skeleton
     :slots (:slots slot-result)
     :layout layout
     :foundation foundation
     :recur-form recur-form
     :authenticated-recur authenticated-recur})))

(defn- nested-loop-attribution-context []
  (let [projection
        (fixture-projection "nested-loop-owner-attribution")
        request (:request projection)
        indexes (fixture-indexes projection)
        loops (operator-enclosing-forms request indexes 'loop)
        recur-form (first (operator-enclosing-forms request indexes 'recur))
        outer-loop
        (first (filter (fn [candidate]
                         (some (set (map :form-id
                                         (remove #{candidate} loops)))
                               (:child-form-ids candidate)))
                       loops))
        inner-loop (first (remove #{outer-loop} loops))
        binding-vector
        (fn [loop-form]
          (get-in indexes [:forms :by-primary
                           (second (:child-form-ids loop-form))]))
        initializer
        (fn [loop-form]
          (get-in indexes [:forms :by-primary
                           (second (:child-form-ids
                                    (binding-vector loop-form)))]))]
    {:projection projection :request request :indexes indexes
     :outer-loop outer-loop :inner-loop inner-loop :recur-form recur-form
     :outer-initializer (initializer outer-loop)
     :inner-initializer (initializer inner-loop)}))

(defn- let-owner-plan-context
  ([] (let-owner-plan-context "nested-empty-wildcard-let"))
  ([basename]
  (let [projection (fixture-projection basename)
        request (:request projection)
        indexes (fixture-indexes projection)
        let-form (first (operator-enclosing-forms request indexes 'let))
        skeleton
        (invoke 'sh07-b51-let-owner-skeleton [request indexes let-form])
        slot-result
        (invoke 'sh07-b51-let-slot-patterns [request indexes skeleton])
        layout (mechanical-foundation-reference-layout (:slots slot-result))]
    {:projection projection :request request :indexes indexes
     :let-form let-form :skeleton skeleton :slots (:slots slot-result)
     :layout layout
     :owner-plans
     [{:owner-form-id (:form-id let-form)
       :initialization-layout layout
       :recur-layout-entries []}]})))

(defn- mechanical-recur-reference-layout [foundation]
  (let [slot-plans (:slot-plans foundation)]
    {:predecessor-authority-id (internal-reference 701)
     :recur-core-node-id (internal-reference 700)
     :argument-node-ids
     (mapv #(internal-reference (+ 710 %)) (range (count slot-plans)))
     :runtime-check-id-vectors
     (mapv (fn [slot-index slot-plan]
             (mapv internal-reference
                   (range (+ 800 (* slot-index 32))
                          (+ 800 (* slot-index 32)
                             (count (:runtime-check-preimages slot-plan))))))
           (range) slot-plans)
     :projection-id-vectors
     (mapv (fn [slot-index slot-plan]
             (mapv internal-reference
                   (range (+ 900 (* slot-index 32))
                          (+ 900 (* slot-index 32)
                             (count (:terminal-projections slot-plan))))))
           (range) slot-plans)
     :product-node-id-vectors
     (mapv (fn [slot-index slot-plan]
             (mapv internal-reference
                   (range (+ 1000 (* slot-index 32))
                          (+ 1000 (* slot-index 32)
                             (count (:runtime-check-preimages slot-plan))
                             (count (:terminal-projections slot-plan))))))
           (range) slot-plans)
     :argument-product-node-ids
     (mapv #(internal-reference (+ 1100 %)) (range (count slot-plans)))
     :commit-product-node-id (internal-reference 1150)
     :publication-event-id (internal-reference 1151)
     :recur-slot-mapping-id (internal-reference 1152)
     :first-global-runtime-check-ordinal 10
     :global-publication-ordinal 10
     :global-recur-mapping-ordinal 0}))

(defn- loop-owner-plans [context]
  [{:owner-form-id (get-in context [:loop-form :form-id])
    :initialization-layout (:layout context)
    :recur-layout-entries
    [{:recur-form-id (get-in context [:recur-form :form-id])
      :layout
      (assoc (mechanical-recur-reference-layout (:foundation context))
             :predecessor-authority-id
             (get-in context [:layout :predecessor-authority-id]))}]}])

(defn- mechanical-recur-mapping-preimage [context recur-layout]
  (let [foundation (:foundation context)
        owner (:owner-coordinate foundation)
        recur-coordinate
        (get-in context [:authenticated-recur :recur-coordinate])
        slot-plans (:slot-plans foundation)
        slot-count (count slot-plans)
        loop-core-node-id (get-in context [:layout :owner-core-node-id])]
    {:domain :gravity/sh07-b51-recur-slot-mapping-id-preimage-v18
     :schema-version 18
     :global-recur-mapping-ordinal
     (:global-recur-mapping-ordinal recur-layout)
     :predecessor-authority-id (:predecessor-authority-id recur-layout)
     :fragment-id (:fragment-id owner)
     :fragment-ordinal (:fragment-ordinal owner)
     :root-form-id (:root-form-id owner)
     :root-syntax-id (:root-syntax-id owner)
     :loop-form-id (:form-id (:loop-form context))
     :loop-syntax-id (:syntax-id (:loop-form context))
     :loop-core-node-id loop-core-node-id
     :recur-form-id (:recur-form-id recur-coordinate)
     :recur-syntax-id (:recur-syntax-id recur-coordinate)
     :recur-core-node-id (:recur-core-node-id recur-coordinate)
     :target-loop-form-id (:form-id (:loop-form context))
     :target-loop-syntax-id (:syntax-id (:loop-form context))
     :target-loop-core-node-id loop-core-node-id
     :commit-publication-id (:publication-event-id recur-layout)
     :owner-coordinate owner
     :recur-coordinate recur-coordinate
     :slot-count slot-count
     :argument-count slot-count
     :slot-ordinals (vec (range slot-count))
     :argument-ordinals (vec (range slot-count))
     :slot-ids (:slot-ids (:layout context))
     :slot-coordinates (mapv :slot-coordinate slot-plans)
     :argument-node-ids (:argument-node-ids recur-layout)
     :extraction-id-vectors (:extraction-id-vectors (:layout context))
     :required-check-id-vectors (:runtime-check-id-vectors recur-layout)
     :projection-id-vectors (:projection-id-vectors recur-layout)
     :binding-id-vectors
     (mapv #(get-in % [:pattern :introduced-binding-ids]) (:slots context))
     :evaluation-order (vec (range slot-count))
     :commit-policy :simultaneous-after-all-checks
     :all-or-none true
     :tail-only true
     :source-origin-chain (get-in context [:recur-form :origin-chain])
     :source-generated-origin
     (get-in context [:recur-form :generated-origin])}))

(defn- mechanical-loop-publication-preimage
  [context recur-layout mapping-preimage]
  (let [owner (:owner-coordinate (:foundation context))
        coordinates (:slot-coordinates mapping-preimage)
        slot-ids (:slot-ids mapping-preimage)
        binding-vectors (:binding-id-vectors mapping-preimage)]
    {:domain :gravity/sh07-b51-publication-event-id-preimage-v18
     :schema-version 18
     :global-publication-ordinal (:global-publication-ordinal recur-layout)
     :predecessor-authority-id (:predecessor-authority-id recur-layout)
     :fragment-id (:fragment-id owner)
     :fragment-ordinal (:fragment-ordinal owner)
     :root-form-id (:root-form-id owner)
     :root-syntax-id (:root-syntax-id owner)
     :owner-kind :loop
     :owner-form-id (:enclosing-form-id owner)
     :owner-syntax-id (:enclosing-syntax-id owner)
     :owner-coordinate owner
     :slot-coordinate nil
     :slot-coordinates coordinates
     :recur-coordinate (:recur-coordinate mapping-preimage)
     :slot-id nil
     :slot-ids slot-ids
     :event-kind :loop-atomic-transfer
     :introduced-binding-ids []
     :introduced-binding-id-vectors binding-vectors
     :projection-ids []
     :projection-id-vectors (:projection-id-vectors recur-layout)
     :required-check-ids []
     :required-check-id-vectors (:runtime-check-id-vectors recur-layout)
     :product-node-ids []
     :product-node-id-vectors (:product-node-id-vectors recur-layout)
     :visible-before-binding-ids []
     :visible-before-binding-id-vectors
     (vec (repeat (count slot-ids) []))
     :visible-after-binding-ids []
     :visible-after-binding-id-vectors
     (vec (repeat (count slot-ids) []))
     :commit-order (:global-publication-ordinal recur-layout)
     :all-or-none true
     :publish-none (every? empty? binding-vectors)
     :source-origin-chain (get-in context [:recur-form :origin-chain])
     :source-generated-origin
     (get-in context [:recur-form :generated-origin])}))

(defn- exact-contract-oracle? [products purposes dependencies]
  (and (= product-key-order products)
       (= purpose-order purposes)
       (= dependency-counts dependencies)
       (= 41 (count products))
       (= 19 (count purposes))
       (= 58 (reduce + (vals dependencies)))))

;; Root 8's acceptance evidence must have a raw-derived oracle that is not
;; obtained by projecting Root 1's pending envelope.  These small test-local
;; helpers derive only authenticated request coordinates and the governed
;; reason-specific related id from the raw SH06 request.
(def ^:private g13-pending-cases
  {"duplicate-vector-binding"
   {:reason :duplicate-vector-binding-name
    :kind :duplicate-binding
    :expected :unique-vector-leaf-name
    :observed 'value}
   "unsupported-vector-rest"
   {:reason :unsupported-vector-rest
    :kind :lowering-gap
    :expected :legacy-symbol-or-fixed-vector-pattern
    :observed :rest-marker}
   "unsupported-map-pattern"
   {:reason :unsupported-nested-pattern
    :kind :lowering-gap
    :expected :legacy-symbol-or-fixed-vector-pattern
    :observed :unsupported-form}
   "missing-recur-target"
   {:reason :missing-recur-target
    :kind :recur-target
    :expected :nearest-compatible-function-or-loop-target
    :observed :missing}
   "recur-arity-mismatch"
   {:reason :recur-arity-mismatch
    :kind :recur-target
    :expected 2
    :observed 1}
   "recur-arity-mismatch-non-tail"
   {:reason :recur-arity-mismatch
    :kind :recur-target
    :expected 2
    :observed 1}
   "recur-not-tail"
   {:reason :recur-not-tail
    :kind :recur-target
    :expected :nearest-compatible-function-or-loop-target
    :observed :non-tail}
   "module-slot-limit-exceeded"
   {:reason :module-slot-limit-exceeded
    :kind :verify
    :expected {:unit :module-slots :maximum 1024}
    :observed {:unit :module-slots :saturating-count 1025}}
   "module-binding-leaf-limit-exceeded"
   {:reason :module-binding-leaf-limit-exceeded
    :kind :verify
    :expected {:unit :module-binding-leaves :maximum 2048}
    :observed {:unit :module-binding-leaves :saturating-count 2049}}
   "module-slot-priority-over-pattern"
   {:reason :module-slot-limit-exceeded
    :kind :verify
    :expected {:unit :module-slots :maximum 1024}
    :observed {:unit :module-slots :saturating-count 1025}}
   "module-leaf-priority-over-pattern"
   {:reason :module-binding-leaf-limit-exceeded
    :kind :verify
    :expected {:unit :module-binding-leaves :maximum 2048}
    :observed {:unit :module-binding-leaves :saturating-count 2049}}
   "module-slot-after-leaf-priority"
   {:reason :module-slot-limit-exceeded
    :kind :verify
    :expected {:unit :module-slots :maximum 1024}
    :observed {:unit :module-slots :saturating-count 1025}}})

(defn- g13-form-ordinal [request form-id]
  (first
   (keep-indexed
    (fn [ordinal form]
      (when (= form-id (:form-id form)) ordinal))
    (:forms request))))

(defn- g13-authenticated-form [request form-id]
  "Return the one raw form carrying form-id, or nil at the integrity boundary.

  This intentionally uses a fresh scan of the authenticated form vector.  The
  pending oracle must not borrow Root 1's indexed selector or its pattern
  enumerator when deriving the expected diagnostic.
  "
  (let [matches (filterv #(= form-id (:form-id %)) (:forms request))]
    (when (= 1 (count matches))
      (first matches))))

(defn- g13-authenticated-fragment [request form-id]
  (let [matches
        (filterv #(some #{form-id} (:form-ids %))
                 (:fragment-manifest request))]
    (when (= 1 (count matches))
      (first matches))))

(declare g13-owner-for-pattern g13-authenticated-target-operator)

(defn- g13-local-parent-chain
  "Authenticate the raw reciprocal parent chain independently of production.

  Every owner coordinate in the pending oracle is rooted by this walk.  It
  intentionally rechecks canonical form identity, unique child membership,
  and exactly-one top-level root membership instead of consulting a compiler
  owner selector.
  "
  [request form]
  (loop [current form chain [] visited #{}]
    (let [form-id (:form-id current)
          canonical (g13-authenticated-form request form-id)]
      (cond
        (nil? canonical) nil
        (not= current canonical) nil
        (contains? visited form-id) nil
        :else
        (let [next-chain (conj chain current)
              parent-id (:parent-form-id current)]
          (if (nil? parent-id)
            (when (= 1 (count (filter #(= form-id %) (:top-level-form-ids request))))
              {:root current :chain next-chain})
            (let [parent (g13-authenticated-form request parent-id)
                  parents
                  (filterv
                   #(some #{form-id} (:child-form-ids %))
                   (:forms request))]
              (when (and (map? parent)
                         (= 1 (count parents))
                         (= parent-id (:form-id (first parents)))
                         (= 1 (count (filter #(= form-id %)
                                             (:child-form-ids parent)))))
                (recur parent next-chain (conj visited form-id))))))))))

(defn- g13-local-owner-fragment
  [request root enclosing]
  (let [root-id (:form-id root)
        enclosing-id (:form-id enclosing)
        matches
        (filterv
         (fn [fragment]
           (and (= (:fragment-id fragment) root-id)
                (= (:content-id fragment) root-id)
                (= (:root-form-ids fragment) [root-id])
                (= (:root-node-ids fragment) [root-id])
                (= 1 (count (filter #(= enclosing-id %) (:form-ids fragment))))))
         (:fragment-manifest request))]
    (when (= 1 (count matches)) (first matches))))

(defn- g13-local-owner-coordinate
  "Return the authenticated root/fragment coordinate for an enclosing form."
  [request enclosing]
  (when (map? enclosing)
    (let [{:keys [root chain]} (g13-local-parent-chain request enclosing)
          fragment (when root (g13-local-owner-fragment request root enclosing))]
      (when (and root fragment)
        {:fragment-id (:fragment-id fragment)
         :fragment-ordinal (:ordinal fragment)
         :root-form-id (:form-id root)
         :root-syntax-id (:syntax-id root)
         :owning-definition-form-id nil
         :owning-definition-syntax-id nil
         :def-form-id nil
         :def-syntax-id nil
         :defn-input-syntax-id nil
         :fn-form-id nil
         :fn-syntax-id nil
         :enclosing-form-id (:form-id enclosing)
         :enclosing-syntax-id (:syntax-id enclosing)}))))

(defn- g13-local-pattern-leaves
  "Authenticate and enumerate a raw pattern in preorder.

  The implementation is deliberately test-local: it checks canonical form
  identity, reciprocal child edges, and exactly-one fragment membership while
  walking the raw form graph.  Wildcards are terminals but not binding leaves;
  a descendant ampersand is the governed rest-pattern rejection.
  "
  [request fragment pattern-form]
  (letfn [(walk [form path expected-parent visited]
            (let [form-id (:form-id form)
                  canonical (g13-authenticated-form request form-id)
                  membership
                  (count (filter #(= form-id %) (:form-ids fragment)))]
              (cond
                (nil? canonical)
                {:status :rejected :reason :pattern-form-substitution}

                (not= form canonical)
                {:status :rejected :reason :pattern-form-substitution}

                (not= 1 membership)
                {:status :rejected :reason :pattern-cross-fragment}

                (contains? visited form-id)
                {:status :rejected :reason :pattern-cycle}

                (and expected-parent
                     (not= expected-parent (:parent-form-id form)))
                {:status :rejected :reason :pattern-parent-not-reciprocal}

                (= :symbol (:kind form))
                (cond
                  (= '& (:value form))
                  {:status :rejected :reason :unsupported-vector-rest}

                  (= '_ (:value form))
                  ;; `_` is a wildcard only as a descendant of a vector
                  ;; pattern.  A bare legacy symbol named `_` remains a
                  ;; binding, matching the public simple-symbol contract.
                  (if expected-parent
                    {:status :accepted :leaves []}
                    {:status :accepted
                     :leaves [{:binding-name (:value form)
                               :form-id form-id
                               :syntax-id (:syntax-id form)
                               :source-span (:source-span form)
                               :generated-origin (:generated-origin form)
                               :path path}]})

                  :else
                  {:status :accepted
                   :leaves [{:binding-name (:value form)
                             :form-id form-id
                             :syntax-id (:syntax-id form)
                             :source-span (:source-span form)
                             :generated-origin (:generated-origin form)
                             :path path}]})

                (= :vector (:kind form))
                (loop [remaining (:child-form-ids form)
                       child-index 0
                       seen (conj visited form-id)
                       leaves []]
                  (if (empty? remaining)
                    {:status :accepted :leaves leaves}
                    (let [child
                          (g13-authenticated-form request (first remaining))
                          result
                          (if (map? child)
                            (walk child (conj path child-index) form-id seen)
                            {:status :rejected
                             :reason :pattern-child-missing})]
                      (if (= :accepted (:status result))
                        (recur (rest remaining)
                               (inc child-index)
                               (conj seen (:form-id child))
                               (into leaves (:leaves result)))
                        result))))

                :else
                {:status :rejected :reason :unsupported-nested-pattern})))]
    (if (and (map? fragment) (map? pattern-form))
      (walk pattern-form [] nil #{})
      {:status :rejected :reason :pattern-authentication})))

(defn- g13-local-duplicate-stage [request fragment pattern-form owner]
  (let [enumerated (g13-local-pattern-leaves request fragment pattern-form)]
    (if (= :accepted (:status enumerated))
      (let [duplicate
            (loop [remaining (:leaves enumerated) seen {}]
              (if (empty? remaining)
                nil
                (let [leaf (first remaining)
                      name (:binding-name leaf)]
                  (if (contains? seen name)
                    {:earlier (get seen name) :later leaf}
                    (recur (rest remaining) (assoc seen name leaf))))))]
        (if duplicate
          (let [earlier (:earlier duplicate)
                later (:later duplicate)
                occurrence
                (fn [leaf ordinal]
                  (assoc leaf
                         :scope-id (:scope-id
                                    (g13-authenticated-form
                                     request (:form-id leaf)))
                         :pattern-node-ordinal ordinal
                         :terminal-ordinal ordinal
                         :leaf-ordinal ordinal))]
            {:status :rejected
             :reason :duplicate-vector-binding-name
             :check nil
             :offending-form
             (g13-authenticated-form request (:form-id later))
             :path (:path later)
             :first-duplicate (occurrence earlier 0)
             :later-duplicate (occurrence later 1)
             :partial-extractions []
             :owner-coordinate owner
             :slot-coordinate nil})
          {:status :accepted :leaves (:leaves enumerated)}))
      enumerated)))

(defn- g13-local-pattern-binding
  "Authenticate one lexical binding leaf against the raw binding table."
  [request fragment form]
  (let [matches
        (filterv
         (fn [binding]
           (and (= :lexical (:binding-class binding))
                (= (:name binding) (:value form))
                (= (:definition-syntax-id binding) (:syntax-id form))
                (= (:scope-id binding) (:scope-id form))
                (= 1 (count (filter #(= (:binding-id binding) %)
                                    (:local-binding-ids fragment))))))
         (:binding-table request))]
    (when (= 1 (count matches)) (first matches))))

(defn- g13-local-resource-pattern-scan
  "Count authenticated raw binding leaves while deferring pattern defects."
  [request fragment pattern-form]
  (letfn [(walk [form path expected-parent descendant? visited first-by-name]
            (let [form-id (:form-id form)
                  canonical (g13-authenticated-form request form-id)
                  membership (count (filter #(= form-id %) (:form-ids fragment)))]
              (cond
                (nil? canonical)
                {:leaves [] :deferred {:reason :pattern-form-substitution}}

                (not= form canonical)
                {:leaves [] :deferred {:reason :pattern-form-substitution}}

                (not= 1 membership)
                {:leaves [] :deferred {:reason :pattern-cross-fragment}}

                (contains? visited form-id)
                {:leaves [] :deferred {:reason :pattern-cycle}}

                (and expected-parent
                     (not= expected-parent (:parent-form-id form)))
                {:leaves [] :deferred {:reason :pattern-parent-not-reciprocal}}

                (= :symbol (:kind form))
                (cond
                  (= '& (:value form))
                  {:leaves [] :deferred {:reason :unsupported-vector-rest}}

                  (= '_ (:value form))
                  {:leaves [] :deferred nil}

                  :else
                  (if (g13-local-pattern-binding request fragment form)
                    (let [leaf {:binding-name (:value form)
                                :form-id form-id
                                :syntax-id (:syntax-id form)
                                :scope-id (:scope-id form)
                                :source-span (:source-span form)
                                :generated-origin (:generated-origin form)
                                :path path}
                          prior (get first-by-name (:value form))]
                      {:leaves [leaf]
                       :deferred
                       (when prior {:reason :duplicate-vector-binding-name
                                    :first-duplicate prior
                                    :later-duplicate leaf})
                       :first-by-name
                       (if prior first-by-name
                           (assoc first-by-name (:value form) leaf))})
                    {:leaves []
                     :deferred {:reason :binding-leaf-unauthenticated}}))

                (= :vector (:kind form))
                (loop [remaining (:child-form-ids form)
                       child-index 0
                       seen (conj visited form-id)
                       leaves []
                       deferred nil
                       names first-by-name]
                  (if (empty? remaining)
                    {:leaves leaves :deferred deferred
                     :first-by-name names}
                    (let [child (g13-authenticated-form request (first remaining))
                          result
                          (if (map? child)
                            (walk child (conj path child-index) form-id true
                                  seen names)
                            {:leaves []
                             :deferred {:reason :pattern-child-missing}})]
                      (recur (rest remaining) (inc child-index)
                             (conj seen (when (map? child) (:form-id child)))
                             (into leaves (:leaves result))
                             (or deferred (:deferred result))
                             (or (:first-by-name result) names)))))

                :else
                {:leaves [] :deferred {:reason :unsupported-nested-pattern}})))]
    (if (and (map? fragment) (map? pattern-form))
      (assoc (walk pattern-form [] nil false #{} {}) :status :accepted)
      {:status :accepted :leaves []
       :deferred {:reason :pattern-authentication}})))

(defn- g13-local-first-governed-deferred [slots]
  (loop [remaining slots]
    (if (empty? remaining)
      nil
      (let [slot (first remaining)
            deferred (:deferred slot)
            reason (:reason deferred)]
        (if (and (map? deferred)
                 (contains? #{:unsupported-vector-rest
                              :unsupported-nested-pattern
                              :duplicate-vector-binding-name}
                            reason))
          (assoc deferred
                 :pattern-root (:pattern-root slot)
                 :fragment (:fragment slot)
                 :owner-coordinate (:owner-coordinate slot)
                 :slot-coordinate nil
                 :partial-extractions [])
          (recur (rest remaining)))))))

(defn- g13-local-resource-priority [slots]
  "Apply module-wide priority only after every slot has been collected."
  (if (> (count slots) 1024)
    {:status :rejected :reason :module-slot-limit-exceeded
     :expected {:unit :module-slots :maximum 1024}
     :observed {:unit :module-slots :saturating-count 1025}
     :offender (nth slots 1024)}
    (loop [remaining slots leaf-count 0]
      (if (empty? remaining)
        (let [deferred (g13-local-first-governed-deferred slots)]
          (if (map? deferred)
            deferred
            {:status :accepted :slot-count (count slots)
             :binding-leaf-count leaf-count :slots slots}))
        (let [slot (first remaining)
              leaves (:leaves slot)
              next-count (+ leaf-count (count leaves))]
          (if (> next-count 2048)
            (let [offending
                  (first (drop (- 2048 leaf-count) leaves))]
              {:status :rejected
               :reason :module-binding-leaf-limit-exceeded
               :expected {:unit :module-binding-leaves :maximum 2048}
               :observed {:unit :module-binding-leaves
                          :saturating-count 2049}
               :offender
               (assoc offending
                      :pattern-root (:pattern-root slot)
                      :fragment (:fragment slot)
                      :owner-coordinate (:owner-coordinate slot))})
            (recur (rest remaining) next-count)))))))

(defn- g13-local-resource-census
  "Rebuild the module owner/slot census from authenticated raw forms.

  This is intentionally separate from the production collector.  It only
  emits the first saturating resource offender needed by the pending oracle.
  "
  [request indexes]
  (let [forms (:forms request)
        authenticated?
        (and (= (count forms)
                (count (set (map :form-id forms))))
             (every? #(= % (g13-authenticated-form request (:form-id %)))
                     forms))
        owners
        (filterv
         (fn [form]
           (contains? #{'let 'loop}
                      (let [operator
                            (g13-authenticated-form
                             request (first (:child-form-ids form)))]
                        (when (and (= :list (:kind form))
                                   (= :symbol (:kind operator)))
                          (:value operator)))))
         forms)]
    (if-not authenticated?
      {:status :rejected :reason :resource-census-authentication}
      (loop [remaining owners slots [] leaf-count 0]
        (if (empty? remaining)
          (g13-local-resource-priority slots)
          (let [owner-form (first remaining)
                child-ids (:child-form-ids owner-form)
                binding-vector
                (g13-authenticated-form request (second child-ids))
                owner-coordinate (g13-local-owner-coordinate request owner-form)
                fragment (g13-authenticated-fragment
                          request (:form-id owner-form))
                pairs (:child-form-ids binding-vector)]
            (if (and (map? binding-vector)
                     (= :vector (:kind binding-vector))
                     (even? (count pairs)))
              (let [owner-slots
                    (loop [remaining-pairs pairs
                           slot-ordinal (count slots)
                           built-slots slots
                           current-leaf-count leaf-count]
                      (if (empty? remaining-pairs)
                        {:status :accepted
                         :slots built-slots
                         :leaf-count current-leaf-count}
                        (let [pattern-form
                              (g13-authenticated-form
                               request (first remaining-pairs))
                              initializer-form
                              (g13-authenticated-form
                               request (second remaining-pairs))
                              pattern-result
                              (g13-local-resource-pattern-scan
                               request
                               (or (g13-authenticated-fragment
                                   request (:form-id pattern-form))
                                   fragment)
                               pattern-form)
                              slot
                              {:slot-ordinal (inc slot-ordinal)
                               :pattern-form pattern-form
                               :initializer-form initializer-form
                               :pattern-root pattern-form
                               :path []
                               :fragment fragment
                               :owner-coordinate owner-coordinate
                               :leaves (:leaves pattern-result)
                               :deferred (:deferred pattern-result)}]
                          ;; Preserve every authenticated slot and deferred
                          ;; observation.  Resource bounds are deliberately
                          ;; applied only after the complete module census so
                          ;; slot overflow wins over an earlier leaf crossing,
                          ;; and either bound wins over deferred pattern
                          ;; diagnostics (matching Root 1's preflight order).
                          (recur (drop 2 remaining-pairs)
                                 (inc slot-ordinal)
                                 (conj built-slots slot)
                                 (+ current-leaf-count
                                    (count (:leaves pattern-result)))))))]
                (if (= :accepted (:status owner-slots))
                  (recur (rest remaining)
                         (:slots owner-slots)
                         (:leaf-count owner-slots))
                  owner-slots))
              {:status :rejected :reason :resource-census-owner-shape})))))))

(defn- g13-owner-for-pattern [request indexes pattern-form]
  (loop [current pattern-form visited #{}]
    (when (map? current)
      (let [form-id (:form-id current)
            operator
            (g13-authenticated-form
             request (first (:child-form-ids current)))]
        (if (contains? visited form-id)
          nil
          (if (contains? #{'let 'loop}
                         (when (and (= :list (:kind current))
                                    (= :symbol (:kind operator)))
                           (:value operator)))
            (g13-local-owner-coordinate request current)
            (let [parent-id (:parent-form-id current)]
              (when parent-id
                (recur (g13-authenticated-form request parent-id)
                       (conj visited form-id))))))))))

(defn- g13-recur-fragment [request recur-form]
  (unique-form-fragment request recur-form))

(defn- g13-recur-owner [request indexes recur-form]
  ;; The source-owned recur coordinate intentionally names the recur form as
  ;; its enclosing source occurrence.  Derive it through the same independent
  ;; reciprocal parent walk used by the raw pending oracle.
  (g13-local-owner-coordinate request recur-form))

(defn- g13-authenticated-target-operator [indexes form]
  (let [operator-id (first (:child-form-ids form))
        operator (get-in indexes [:forms :by-primary operator-id])]
    (when (and (= :list (:kind form))
               (= :symbol (:kind operator)))
      (:value operator))))

(defn- g13-target-selection-oracle [request indexes fragment recur-form]
  ;; Independently walk the authenticated reciprocal parent chain.  A target
  ;; is admitted only when the parent edge and fragment membership are unique;
  ;; loop/fn parents become catalog entries.  Selection is then the unique
  ;; entry at minimum distance (greatest lexical depth), allowing farther
  ;; compatible ancestors to remain in the catalog.
  (loop [current recur-form
         distance 0
         visited #{}
         candidates []]
    (let [current-id (:form-id current)
          parent-id (:parent-form-id current)]
      (cond
        (> distance 256)
        {:status :rejected :reason :recur-target-depth}

        (contains? visited current-id)
        {:status :rejected :reason :recur-target-cycle}

        (nil? parent-id)
        (if (empty? candidates)
          {:status :accepted :selection []
           :nearest-compatible-selection-cardinality 0
           :catalog candidates}
          (let [nearest (apply min (map :distance candidates))
                selection (vec (filter #(= nearest (:distance %)) candidates))]
            {:status (if (= 1 (count selection)) :accepted :boundary)
             :selection selection
             :nearest-compatible-selection-cardinality (count selection)
             :catalog candidates
             :reason (when (> (count selection) 1)
                       :recur-target-selection-tie)}))

        :else
        (let [parent (get-in indexes [:forms :by-primary parent-id])
              child-ids (:child-form-ids parent)
              edge-count (count (filter #(= current-id %) child-ids))
              fragment-count (count (filter #(= parent-id %) (:form-ids fragment)))
              operator (when (map? parent)
                         (g13-authenticated-target-operator indexes parent))]
          (if (and (map? parent)
                   (= 1 edge-count)
                   (= (:parent-form-id current) parent-id)
                   (= 1 fragment-count))
            (recur parent
                   (inc distance)
                   (conj visited current-id)
                   (if (contains? #{'loop 'fn} operator)
                     (conj candidates
                           {:target-kind (if (= operator 'loop) :loop :function)
                            :target-form parent
                            :target-form-id (:form-id parent)
                            ;; Target record ids are syntax-authenticated,
                            ;; not source form ids.
                            :target-id (:syntax-id parent)
                            :distance (inc distance)})
                     candidates))
            {:status :rejected :reason :recur-target-parent-edge}))))))

(defn- g13-semantic-coordinate
  [request form owner-coordinate recur? path]
  {:form-id (:form-id form)
   :form-ordinal (g13-form-ordinal request (:form-id form))
   :fragment-ordinal (:fragment-ordinal owner-coordinate)
   :syntax-id (:syntax-id form)
   :source-span (or (:semantic-span form) (:source-span form))
   :generated-origin-chain (:generated-origin form)
   :core-node-id nil
   :owner-coordinate owner-coordinate
   :slot-id nil
   :extraction-id nil
   :recur-form-id (when recur? (:form-id form))
   :recur-syntax-id (when recur? (:syntax-id form))
   :path path})

(defn- g13-raw-pending-oracle [basename]
  (let [{:keys [reason kind expected observed]} (get g13-pending-cases basename)
        projection (fixture-projection "rejected" basename)
        request (:request projection)
        indexes (fixture-indexes projection)
        recur? (contains? #{:missing-recur-target :recur-arity-mismatch
                            :recur-not-tail}
                          reason)
        duplicate? (= reason :duplicate-vector-binding-name)
        resource? (contains? #{:module-slot-limit-exceeded
                               :module-binding-leaf-limit-exceeded}
                             reason)
        pattern-form
        (when (or duplicate?
                  (contains? #{:unsupported-vector-rest
                               :unsupported-nested-pattern}
                             reason))
          (unique-pattern-form
           request
           (case reason
             :duplicate-vector-binding-name '[value [value _] tail tail]
             :unsupported-vector-rest '[head & tail]
             '{:value value})))
        duplicate-stage
        (when duplicate?
          (g13-local-duplicate-stage
           request (unique-form-fragment request pattern-form)
           pattern-form (g13-owner-for-pattern request indexes pattern-form)))
        duplicate-earlier (:first-duplicate duplicate-stage)
        duplicate-later (:later-duplicate duplicate-stage)
        recur-form
        (when recur?
          (first (operator-enclosing-forms request indexes 'recur)))
        recur-fragment (when recur? (g13-recur-fragment request recur-form))
        recur-owner (when recur? (g13-recur-owner request indexes recur-form))
        resource-stage
        (when resource?
          (g13-local-resource-census request indexes))
        resource-offender (:offender resource-stage)
        resource-form-value
        (or (:form resource-offender)
            (:pattern-form resource-offender)
            (:form-id resource-offender)
            (:pattern-root resource-offender)
            (get-in resource-offender [:descriptor :form-id]))
        resource-form
        (if (map? resource-form-value)
          resource-form-value
          (get-in indexes [:forms :by-primary resource-form-value]))
        resource-owner (:owner-coordinate resource-offender)
        source-form (cond
                      duplicate? duplicate-earlier
                      recur? recur-form
                      resource? resource-form
                      :else pattern-form)
        owner-coordinate (cond
                           recur? recur-owner
                           resource? resource-owner
                           :else (g13-owner-for-pattern
                                  request indexes source-form))
        path (cond
               duplicate? (:path duplicate-earlier)
               resource? (:path resource-offender)
               :else nil)
        related
        (cond
          duplicate? [(:form-id duplicate-earlier)
                      (:form-id duplicate-later)]
          (= reason :missing-recur-target)
          [(:root-form-id recur-owner)]
          recur?
          (let [selection
                (g13-target-selection-oracle
                 request indexes recur-fragment recur-form)
                selected (first (:selection selection))]
            [(:target-id selected)])
          resource? [(:root-form-id owner-coordinate)]
          :else [])]
    {:reason reason
     :kind kind
     :expected expected
     :observed observed
     :related-semantic-ids related
     :coordinate
     (g13-semantic-coordinate request source-form owner-coordinate recur? path)}))

(defn- g13-oracle-semantic [oracle]
  (let [kind (:kind oracle)
        reason (:reason oracle)
        remediation
        (if (contains? #{:module-slot-limit-exceeded
                         :module-binding-leaf-limit-exceeded}
                       reason)
          {:action :fix-source :owner :source-author
           :required-evidence
           [:resource-bound :first-offending-coordinate]}
          (if (= kind :lowering-gap)
            {:action :fix-source :owner :source-author
             :required-evidence [:offending-form :supported-grammar]}
            (if (= kind :duplicate-binding)
              {:action :fix-source :owner :source-author
               :required-evidence [:binding-paths]}
              (if (= kind :recur-target)
                {:action :fix-source :owner :source-author
                 :required-evidence [:recur-coordinate :target-slots]}
                {:action :repair-carrier :owner :master-coordinator
                 :required-evidence [:failed-invariant]}))))]
    (merge
     {:artifact :gravity/sh07-b51-c6-diagnostic-v18
      :schema-version 18
      :status :rejected
      :diagnostic-kind kind
      :rule (case kind
              :lowering-gap "C6-LOWERING-GAP"
              :duplicate-binding "L7-DUP-BINDING"
              :recur-target "L2-RECUR-TARGET"
              "C6-VERIFY")
      :severity :error
      :stage :core-lowering
      :reason reason
      :expected (:expected oracle)
      :observed (:observed oracle)
      :related-semantic-ids (:related-semantic-ids oracle)
      :lowering-rule :sh07-b51-vector-destructuring-v18
      :profile :meta
      :target :jvm
      :remediation remediation
      :fail-closed true}
     (:coordinate oracle))))

(deftest sh07-b51-v18-fixtures-are-fresh-byte-identical-pairs
  (doseq [[family basenames] fixture-catalog
          extension extensions]
    (is (= basenames (fixture-basenames family extension))
        [family extension]))
  (doseq [[family basenames] fixture-catalog
          basename basenames]
    (let [gravity (fixture-path family basename ".gravity")
          qst (fixture-path family basename ".qst")]
      (is (.isFile (io/file gravity)))
      (is (.isFile (io/file qst)))
      (is (pos? (alength (source-bytes gravity))))
      (is (java.util.Arrays/equals
           (source-bytes gravity) (source-bytes qst))
          [family basename]))))

(deftest sh07-b51-v18-mixed-resource-priority-fixtures-preserve-raw-evidence
  ;; These fixtures keep an early pattern defect in the same authenticated
  ;; carrier as the later global resource crossing.  The source-level evidence
  ;; remains useful while the stage2 emitter pin is unavailable to execute the
  ;; full projection: resource census must report the bound, not the deferred
  ;; pattern diagnostic.
  (let [slot-gravity
        (slurp (fixture-path "rejected"
                            "module-slot-priority-over-pattern" ".gravity"))
        leaf-gravity
        (slurp (fixture-path "rejected"
                            "module-leaf-priority-over-pattern" ".gravity"))
        combined-gravity
        (slurp (fixture-path "rejected"
                            "module-slot-after-leaf-priority" ".gravity"))
        slot-symbols (set (re-seq #"\bslot-[0-9]+\b" slot-gravity))
        leaf-symbols (set (re-seq #"\bleaf-[0-9]+\b" leaf-gravity))
        combined-slot-symbols
        (set (re-seq #"\bslot-[0-9]+\b" combined-gravity))
        combined-leaf-symbols
        (set (re-seq #"\bleaf-[0-9]+\b" combined-gravity))
        ;; This independently derived shape lets the test-local priority
        ;; function run without the pin-gated stage2 reader.  The first three
        ;; slots carry all 2049 leaves; the remaining 1022 are empty slots.
        combined-slots
        (vec (concat (map (fn [ordinal]
                            {:slot-ordinal ordinal
                             :leaves (vec (repeat 683 ordinal))})
                          (range 1 4))
                     (map (fn [ordinal]
                            {:slot-ordinal ordinal :leaves []})
                          (range 4 1026))))
        combined-priority (g13-local-resource-priority combined-slots)]
    (is (= 1024 (count slot-symbols)))
    (is (= 2047 (count leaf-symbols)))
    (is (< (str/index-of slot-gravity "[head & tail]")
           (str/index-of slot-gravity "slot-0")))
    (is (< (str/index-of leaf-gravity "[dup dup]")
           (str/index-of leaf-gravity "leaf-0")))
    ;; The combined fixture crosses the 2049-leaf bound in its first three
    ;; patterns, then reaches slot 1025 only in the trailing simple slots.
    ;; It is therefore a direct resource-priority witness: slot overflow must
    ;; win over the earlier leaf overflow, with no deferred pattern involved.
    (is (= 1022 (count combined-slot-symbols)))
    (is (= 2049 (count combined-leaf-symbols)))
    (is (< (str/index-of combined-gravity "leaf-2048")
           (str/index-of combined-gravity "slot-0")))
    (is (= :module-slot-limit-exceeded
           (get-in g13-pending-cases
                   ["module-slot-after-leaf-priority" :reason])))
    (is (= 2049 (reduce + (map #(count (:leaves %)) combined-slots))))
    (is (= 1025 (count combined-slots)))
    (is (= :rejected (:status combined-priority)) combined-priority)
    (is (= :module-slot-limit-exceeded (:reason combined-priority))
        combined-priority)
    (is (= 1025 (get-in combined-priority [:offender :slot-ordinal]))
        combined-priority)
    (is (= :module-slot-limit-exceeded
           (get-in g13-pending-cases
                   ["module-slot-priority-over-pattern" :reason])))
    (is (= :module-binding-leaf-limit-exceeded
           (get-in g13-pending-cases
                   ["module-leaf-priority-over-pattern" :reason])))
    (doseq [basename ["module-slot-priority-over-pattern"
                      "module-leaf-priority-over-pattern"
                      "module-slot-after-leaf-priority"]]
      (is (java.util.Arrays/equals
           (source-bytes (fixture-path "rejected" basename ".gravity"))
           (source-bytes (fixture-path "rejected" basename ".qst")))
          basename))))

(deftest sh07-b51-v18-root8-deferred-pattern-replay-is-explicit-and-ordered
  ;; Keep direct evidence for ordinary lowering/duplicate branches even when
  ;; the stage2 runtime pin prevents executing a full raw-carrier projection.
  (let [definitions (definitions-by-name @source-forms)
        stage-text (pr-str (get definitions 'sh07-b51-r8-stage-from-raw))
        census-text (pr-str (get definitions 'sh07-b51-r8-resource-census))
        scan-text
        (pr-str (get definitions 'sh07-b51-r8-resource-pattern-scan))
        reconstruct-text
        (pr-str (get definitions
                     'sh07-b51-r8-reconstruct-pending-semantic))
        first-deferred
        (g13-local-first-governed-deferred
         [{:deferred {:reason :resource-census-cycle}}
          {:deferred {:reason :unsupported-vector-rest}}
          {:deferred {:reason :duplicate-vector-binding-name}}])
        duplicate-deferred
        (g13-local-first-governed-deferred
         [{:deferred {:reason :duplicate-vector-binding-name}}])]
    (is (neg? (compare (str/index-of stage-text
                                     "sh07-b51-r8-resource-census")
                       (str/index-of stage-text
                                     "sh07-b51-r8-first-recur-stage"))))
    (is (str/includes? census-text "sh07-b51-r8-first-governed-deferred"))
    (is (str/includes? scan-text ":deferred"))
    (doseq [reason [:unsupported-vector-rest
                    :unsupported-nested-pattern
                    :duplicate-vector-binding-name]]
      (is (str/includes? scan-text (name reason)) reason))
    (is (str/includes? reconstruct-text
                       "sh07-b51-r8-stage-from-raw"))
    (doseq [reason [:unsupported-vector-rest
                    :unsupported-nested-pattern
                    :duplicate-vector-binding-name]]
      (is (str/includes? reconstruct-text (name reason)) reason))
    (is (= :unsupported-vector-rest (:reason first-deferred)))
    (is (= :duplicate-vector-binding-name (:reason duplicate-deferred)))
    (is (= :module-slot-limit-exceeded
           (get-in g13-pending-cases
                   ["module-slot-priority-over-pattern" :reason])))
    (is (= :module-binding-leaf-limit-exceeded
           (get-in g13-pending-cases
                   ["module-leaf-priority-over-pattern" :reason])))))

(deftest sh07-b51-v18-public-eight-root-abi-and-export-arities-are-exact
  (let [forms @source-forms
        clauses (namespace-clauses (first forms))
        definitions (definitions-by-name forms)]
    (is (= 'gravity.checked-core (second (first forms))))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= (vec (concat predecessor-exports public-roots))
           (:exports clauses)))
    (is (= 8 (count public-roots) (count (set public-roots))))
    (doseq [[function expected] public-shapes]
      (if-let [definition (get definitions function)]
        (is (= expected (function-shape definition)) function)
        (is false [:public-root-not-defined function])))))

(deftest sh07-b51-v18-public-root-contract-rows-are-closed-and-disjoint
  ;; Attempt 10 makes Root 1 the sole semantic pending detector.  Every other
  ;; public root is success-or-boundary only; Root 8 consumes raw input rather
  ;; than a caller-supplied pending envelope.
  (is (= (set public-roots) (set (keys public-abi-contract))))
  (is (= 8 (count public-abi-contract)))
  (let [success-rows (mapv :success (vals public-abi-contract))
        boundary-rows (mapv :boundary (vals public-abi-contract))]
    (is (= 8 (count (set success-rows))))
    (is (= 8 (count (set boundary-rows))))
    (is (every? #(= 2 (count %)) (concat success-rows boundary-rows)))
    (is (every? #(= :accepted (first %)) success-rows))
    (is (every? #(= :boundary-rejected (first %)) boundary-rows)))
  (is (= [:pending-rejection :template-rejected]
         (get-in public-abi-contract ['sh07-b51-build-template :pending])))
  (is (= 1 (count (filter :pending (vals public-abi-contract)))))
  (is (= 6 (count detector-boundary-contract)))
  (is (= 8 (reduce + (map count (vals root1-pending-whitelist)))))
  (is (= 4 (count root1-pending-whitelist)))
  (is (= 2 (count (:verify root1-pending-whitelist))))
  (is (= 2 (count root1-catalog-only-unreachable)))
  (doseq [[root {:keys [tag reasons]}] detector-boundary-contract]
    (is (= [:boundary-rejected tag]
           (get-in public-abi-contract [root :boundary])) root)
    (is (= reasons (vec (distinct reasons))) root)
    (is (every? keyword? reasons) root))
  (doseq [stale-tag [:template-verification-rejected
                     :template-resolution-rejected
                     :resolved-verification-rejected
                     :independent-verifier-rejected
                     :final-artifact-rejected]]
    (is (not (.contains (pr-str public-abi-contract) (str stale-tag)))
        stale-tag))
  (is (= #{:template :digest-requests}
         (get-in public-abi-contract
                 ['sh07-b51-build-template :success-value-keys])))
  (is (= #{:resolved-core :predecessor-observation :provenance-binding}
         (get-in public-abi-contract
                 ['sh07-b51-resolve-template :success-value-keys])))
  (is (= #{:canonical-artifact :outer-wrapper}
         (get-in public-abi-contract
                 ['sh07-b51-build-final-artifact :success-value-keys]))))

(deftest sh07-b51-v18-abi-helper-is-closed-and-mutation-sensitive
  (let [value (invoke 'sh07-b51-abi [:accepted :template-built {:ok true}])]
    (is (exact-abi? value :accepted :template-built))
    (doseq [mutated [(dissoc value :domain)
                     (assoc value :extra :forbidden)
                     (assoc value :schema-version 17)
                     (assoc value :status :rejected)
                     (assoc value :tag :resolved-verified)]]
      (is (false? (exact-abi? mutated :accepted :template-built))))))

(deftest sh07-b51-v18-declared-digest-builtin-matches-host-not-c11
  (let [witness
        {:domain :gravity/sh07-declared-digest-v1
         :purpose :witness
         :preimage {:b 2 :a 1}}
        values
        [nil true 0 1/2 "gravity" 'shared/checksum :keyword
         [] [1 {:b 2 :a 1}] #{} #{:b :a}
         witness]]
    (is (= "sha256:6ec3ea0e883bb82651d808899aefc25c3bff00eed7f4b05102759a448efac14e"
           (c11-raw-pr-str-digest witness)))
    (is (= "sha256:19b214381d3ae2f7c84107865aa5ceab1bb271a7cc159e534e66fa8c127020a3"
           (bootstrap/reader-canonical-hash witness)))
    (is (not= (c11-raw-pr-str-digest witness)
              (bootstrap/reader-canonical-hash witness)))
    (doseq [value values]
      (is (= (bootstrap/reader-canonical-hash value)
             (gravity-declared-digest
              #'bootstrap/p15-s23-sh07-runtime-invoke-compiler-internal-builtin
              value))
          value))
    (is (thrown? clojure.lang.ExceptionInfo
                 (bootstrap/p15-s23-stage2-runtime-invoke-builtin
                  {:source {:path source-relative-path}}
                  'sh07-declared-digest-hash [])))
    (is (thrown? clojure.lang.ExceptionInfo
                 (bootstrap/p15-s23-stage2-runtime-invoke-builtin
                  {:source {:path source-relative-path}}
                  'sh07-declared-digest-hash
                  [witness witness])))))

(deftest sh07-b51-v18-root3-and-unary-root8-have-closed-boundaries
  (let [resolver (invoke 'sh07-b51-resolve-digest-preimage [{} [] []])
        finalizer (invoke 'sh07-b51-finalize-rejection [{}])]
    (is (exact-abi? resolver :boundary-rejected
                    :digest-preimage-boundary-rejected))
    (is (= :sh07-b51-resolve-digest-preimage
           (get-in resolver [:value :boundary])))
    (is (true? (get-in resolver [:value :recursive-diagnostic-forbidden])))
    (is (exact-abi? finalizer :boundary-rejected
                    :rejection-finalizer-boundary-rejected))
    (is (= #{:status :boundary :reason :recursive-diagnostic-forbidden}
           (set (keys (:value finalizer)))))
    (is (= :sh07-b51-finalize-rejection
           (get-in finalizer [:value :boundary])))
    (is (= :raw-carrier-shape (get-in finalizer [:value :reason])))
    (is (true? (get-in finalizer
                       [:value :recursive-diagnostic-forbidden])))))

(deftest sh07-b51-v18-unary-root8-reconstructs-an-independent-raw-pending-oracle
  ;; Every producer-reachable reason is exercised from a raw carrier.  The
  ;; expected semantic is derived from authenticated forms/targets here,
  ;; before Root 1 is invoked, so Root 8 cannot pass by merely replaying a
  ;; caller-supplied or Root-1-projected semantic value.
  (doseq [basename (sort (keys g13-pending-cases))]
    (let [oracle (g13-raw-pending-oracle basename)
          expected-semantic (g13-oracle-semantic oracle)
          carrier (raw-carrier "rejected" basename ".gravity")
          root1 (invoke 'sh07-b51-build-template [carrier])
          root8 (invoke 'sh07-b51-finalize-rejection [carrier])
          root1-semantic
          (get-in root1 [:value :pending-result
                         :diagnostic-semantic-value])
          accepted (:value root8)
          materialized
          (get-in accepted [:envelope :result :diagnostic])
          forged-root1
          (assoc-in root1
                    [:value :pending-result :diagnostic-semantic-value]
                    (assoc root1-semantic :reason :recur-not-tail))
          root8-after-forged-root1
          (invoke 'sh07-b51-finalize-rejection [carrier])]
      (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier])) basename)
      (is (= [:pending-rejection :template-rejected]
             [(:status root1) (:tag root1)])
          [basename root1])
      (is (= expected-semantic root1-semantic)
          [basename :root1-oracle root1-semantic expected-semantic])
      (is (exact-abi? root8 :accepted :rejection-finalized)
          [basename root8])
      (is (= expected-semantic (dissoc materialized :diagnostic-id))
          [basename :root8-oracle materialized expected-semantic])
      (is (= (:digest-id accepted) (:diagnostic-id materialized))
          [basename root8])
      (is (= (gravity-declared-digest
              #'bootstrap/p15-s23-sh07-runtime-invoke-compiler-internal-builtin
              {:domain :gravity/sh07-declared-digest-v1
               :purpose :sh07-b51-c6-diagnostic-id
               :preimage
               {:domain :gravity/sh07-b51-c6-diagnostic-id-preimage-v18
                :schema-version 18
                :diagnostic-semantic-value expected-semantic}})
             (:digest-id accepted))
          [basename root8])
      ;; Mutating the separately obtained Root-1 result cannot alter Root 8's
      ;; raw replay; Root 8 is invoked again with the same authenticated raw
      ;; carrier and must still match the independent oracle exactly.
      (is (not= forged-root1 root1) basename)
      (is (= expected-semantic
             (dissoc (get-in (:value root8-after-forged-root1)
                             [:envelope :result :diagnostic])
                    :diagnostic-id))
          [basename :root8-independent-replay root8-after-forged-root1]))))

(deftest sh07-b51-v18-unary-root8-calls-only-root1-once
  (let [definitions (definitions-by-name @source-forms)
        definition-names (set (keys definitions))
        root8 'sh07-b51-finalize-rejection
        root1 'sh07-b51-build-template
        forbidden (disj (set public-roots) root1 root8)
        direct-callees
        (fn direct-callees [value]
          (cond
            (and (seq? value) (= 'quote (first value))) #{}
            (seq? value)
       …110760 tokens truncated…])
        (is (map? expected)
            [key core-form :missing-independent-literal-schema])
        (when expected
          (is (= [expected] base)
              [key core-form :noncanonical-literal-record]))
        (is (not-any? #(contains? % :product-family) base)
            [key core-form :generic-witness-is-not-canonical])
        (is (true? (authentic? (assoc products key base)))
            [key core-form :canonical-record-not-accepted])
        (doseq [[label changed]
                [[:extra-field (assoc (first base) :forged true)]
                 [:ordinal (assoc (first base) :ordinal 99)]
                 [:owner (assoc (first base) :owner-coordinate {:forged true})]
                 [:reference (assoc (first base) :core-node-id
                                    (internal-reference 9))]
                 [:source (assoc (first base) :form-id (sha \f))]]]
          (is (false?
               (authentic? (assoc products key (assoc base 0 changed))))
              [key core-form label :record-mutation-not-rejected]))))))

(deftest sh07-b51-v18-root6-relational-families-require-governed-joins
  (let [runtime (direct-source-runtime
                 'sh07-b51-r6-derived-relational-products)
        derive #((:invoke runtime)
                 'sh07-b51-r6-derived-relational-products %)
        id (fn [ordinal] (internal-reference ordinal))
        form-id (sha \a)
        syntax-id (sha \b)
        artifact-id (sha \c)
        projection-id (sha \d)
        binding-id (sha \e)
        scope-id (sha \f)
        target-id (sha \1)
        make-node
        (fn [ordinal core-form children bindings attributes]
          {:node-id (id ordinal) :core-form core-form
           :source {:form-id (sha (char (+ 65 ordinal)))
                    :syntax-id (sha (char (+ 75 ordinal)))
                    :semantic-span nil :origin-chain []
                    :generated-origin nil}
           :attributes attributes :children children
           :evaluation {:kind core-form :region nil
                        :owner-function-syntax-id nil :order []}
           :resolved-binding-ids bindings})
        nodes
        [(make-node 0 :try [(id 20) (id 21)] [] {})
         (make-node 1 :match [(id 22) (id 23)] [binding-id] {})
         (make-node 2 :fn [(id 24)] [binding-id]
                    {:fixed-arity 1})
         (assoc-in
          (make-node 3 :call [(id 25) (id 26)] [binding-id] {})
          [:evaluation :owner-function-syntax-id]
          (sha \M))
         (make-node 4 :let [(id 27) (id 28)] [binding-id] {})
         (make-node 5 :loop [(id 29) (id 30)] [binding-id]
                    {:target-id target-id})
         (make-node 6 :recur [(id 31)] [binding-id]
                    {:target-id target-id :target-kind :loop :arity 1})
         (make-node 7 :def [(id 2)] [binding-id] {:name 'self})]
        function-record
        {:ordinal 0 :function-core-node-id (id 2)
         :function-form-id (get-in nodes [2 :source :form-id])
         :function-syntax-id (get-in nodes [2 :source :syntax-id])
         :body-core-node-id (id 24) :parameter-binding-ids [binding-id]
         :fixed-arity 1 :definition-kind :named-top-level
         :definition-binding-id binding-id :definition-core-node-id (id 7)
         :definition-name 'self}
        definition-record
        {:name 'self :binding-id binding-id :core-node-id (id 7)
         :value-node-id (id 2) :syntax-id (get-in nodes [7 :source :syntax-id])}
        call-record
        {:core-node-id (id 3) :operator-node-id (id 25)
         :operator-binding-id binding-id :argument-node-ids [(id 26)]
         :ordered-evaluation-node-ids [(id 25) (id 26)]
         :evaluation-order :operator-then-arguments :result-policy :call-result}
        call-edge
        {:ordinal 0 :call-core-node-id (id 3)
         :caller-function-syntax-id (get-in nodes [2 :source :syntax-id])
         :caller-function-core-node-id (id 2)
         :callee-binding-id binding-id :callee-definition-syntax-id nil
         :callee-function-syntax-id (get-in nodes [2 :source :syntax-id])
         :callee-function-core-node-id (id 2)
         :argument-core-node-ids [(id 26)]
         :ordered-evaluation-node-ids [(id 25) (id 26)]
         :evaluation-order :operator-then-arguments
         :classification :local-function}
        node-products
        {:nodes nodes :definitions [definition-record] :reference-uses []
         :calls [call-record] :function-records [function-record]
         :call-edges [call-edge] :error-transfers []}
        owner-coordinate
        (fn [owner-index]
          {:fragment-id (sha \7) :fragment-ordinal 0
           :root-form-id (get-in nodes [7 :source :form-id])
           :root-syntax-id (get-in nodes [7 :source :syntax-id])
           :owning-definition-form-id (get-in nodes [7 :source :form-id])
           :owning-definition-syntax-id
           (get-in nodes [7 :source :syntax-id])
           :def-form-id (get-in nodes [7 :source :form-id])
           :def-syntax-id (get-in nodes [7 :source :syntax-id])
           :defn-input-syntax-id nil :fn-form-id nil :fn-syntax-id nil
           :enclosing-form-id (get-in nodes [owner-index :source :form-id])
           :enclosing-syntax-id
           (get-in nodes [owner-index :source :syntax-id])})
        slot-preimage
        (fn [global owner-index initializer-id owner-kind]
          (let [owner (owner-coordinate owner-index)
                coordinate
                {:owner-coordinate owner :slot-ordinal 0
                 :pattern-form-id form-id :pattern-syntax-id syntax-id
                 :initializer-form-id form-id
                 :initializer-syntax-id syntax-id}]
            {:domain :gravity/sh07-b51-binding-slot-id-preimage-v18
             :schema-version 18 :predecessor-authority-id (id 50)
             :global-slot-ordinal global :owner-kind owner-kind
             :owner-form-id (:enclosing-form-id owner)
             :owner-syntax-id (:enclosing-syntax-id owner)
             :owner-core-node-id (id owner-index) :owner-coordinate owner
             :owning-definition-form-id (:owning-definition-form-id owner)
             :owning-definition-syntax-id
             (:owning-definition-syntax-id owner)
             :slot-coordinate coordinate :slot-ordinal 0
             :pattern-form-id form-id :pattern-syntax-id syntax-id
             :pattern-scope-id scope-id :initializer-form-id form-id
             :initializer-syntax-id syntax-id
             :initializer-scope-id scope-id
             :initializer-core-node-id initializer-id
             :source-origin-chain [] :source-generated-origin []}))
        extraction-preimage
        (fn [global owner-index slot-id initializer-id]
          (let [slot (slot-preimage global owner-index initializer-id
                                    (if (= owner-index 4) :let :loop))
                owner (:owner-coordinate slot)]
            {:domain :gravity/sh07-b51-binding-extraction-id-preimage-v18
             :schema-version 18 :predecessor-authority-id (id 50)
             :slot-id slot-id :slot-coordinate (:slot-coordinate slot)
             :global-ordinal global :global-slot-ordinal global
             :slot-ordinal 0 :pattern-node-ordinal 0
             :parent-pattern-node-ordinal nil :terminal-ordinal 0
             :leaf-ordinal 0 :kind :binding-leaf :path []
             :form-id form-id :syntax-id syntax-id :scope-id scope-id
             :enclosing-form-id (:enclosing-form-id owner)
             :enclosing-syntax-id (:enclosing-syntax-id owner)
             :pattern-form-id form-id :pattern-syntax-id syntax-id
             :initializer-form-id form-id
             :initializer-syntax-id syntax-id :expected-width nil
             :binding-id binding-id :binding-name 'x
             :definition-form-id form-id :definition-syntax-id syntax-id
             :binding-scope-id scope-id :fragment-id (:fragment-id owner)
             :fragment-ordinal 0
             :owning-root-form-id (:root-form-id owner)
             :owning-root-syntax-id (:root-syntax-id owner)
             :owning-definition-form-id
             (:owning-definition-form-id owner)
             :owning-definition-syntax-id
             (:owning-definition-syntax-id owner)
             :owner-coordinate owner :defn-input-syntax-id nil
             :fn-form-id nil :fn-syntax-id nil
             :source-origin-chain [] :source-generated-origin []}))
        let-slot-preimage (slot-preimage 0 4 (id 27) :let)
        loop-slot-preimage (slot-preimage 1 5 (id 29) :loop)
        let-extraction-preimage
        (extraction-preimage 0 4 (id 40) (id 27))
        loop-extraction-preimage
        (extraction-preimage 1 5 (id 42) (id 29))
        slot (assoc (dissoc let-slot-preimage :domain :schema-version)
                    :slot-id (id 40))
        loop-slot
        (assoc (dissoc loop-slot-preimage :domain :schema-version)
               :slot-id (id 42))
        extraction
        (assoc (dissoc let-extraction-preimage :domain :schema-version)
               :extraction-id (id 41))
        loop-extraction
        (assoc (dissoc loop-extraction-preimage :domain :schema-version)
               :extraction-id (id 43))
        recur-mapping-preimage
        (let [owner (owner-coordinate 5)]
          {:domain :gravity/sh07-b51-recur-slot-mapping-id-preimage-v18
           :schema-version 18 :global-recur-mapping-ordinal 0
           :predecessor-authority-id (id 50)
           :fragment-id (:fragment-id owner) :fragment-ordinal 0
           :root-form-id (:root-form-id owner)
           :root-syntax-id (:root-syntax-id owner)
           :loop-form-id (:enclosing-form-id owner)
           :loop-syntax-id (:enclosing-syntax-id owner)
           :loop-core-node-id (id 5)
           :recur-form-id (get-in nodes [6 :source :form-id])
           :recur-syntax-id (get-in nodes [6 :source :syntax-id])
           :recur-core-node-id (id 6)
           :target-loop-form-id (:enclosing-form-id owner)
           :target-loop-syntax-id (:enclosing-syntax-id owner)
           :target-loop-core-node-id (id 5)
           :commit-publication-id (id 60) :owner-coordinate owner
           :recur-coordinate
           {:recur-form-id (get-in nodes [6 :source :form-id])
            :recur-syntax-id (get-in nodes [6 :source :syntax-id])
            :recur-core-node-id (id 6)}
           :slot-count 1 :argument-count 1 :slot-ordinals [0]
           :argument-ordinals [0] :slot-ids [(id 42)]
           :slot-coordinates [(:slot-coordinate loop-slot-preimage)]
           :argument-node-ids [(id 31)]
           :extraction-id-vectors [[(id 43)]]
           :required-check-id-vectors [[]]
           :projection-id-vectors [[]]
           :binding-id-vectors [[binding-id]] :evaluation-order [0]
           :commit-policy :atomic-loop-transfer :all-or-none true
           :tail-only true :source-origin-chain []
           :source-generated-origin []})
        target
        {:target-id target-id :target-kind :loop
         :owner-core-node-id (id 5)
         :owner-form-id (get-in nodes [5 :source :form-id])
         :owner-syntax-id (get-in nodes [5 :source :syntax-id])
         :arity 1 :binding-ids [binding-id]
         :body-scope-id scope-id :parent-target-id nil
         :type-compatibility :pending-sh08}
        transfer
        {:recur-core-node-id (id 6)
         :recur-form-id (get-in nodes [6 :source :form-id])
         :recur-syntax-id (get-in nodes [6 :source :syntax-id])
         :target-id target-id
         :target-kind :loop :argument-node-ids [(id 31)] :arity 1
         :tail-position true :evaluation-order :arguments-left-to-right
         :transfer-policy :nearest-lexical-recur-target
         :type-compatibility :pending-sh08}
        relational-preimage
        {:lineage {:authenticated-sh06-artifact-id artifact-id
                   :sh06-semantic-projection-id projection-id}
         :handler-clauses
         [{:try-form-id (get-in nodes [0 :source :form-id])
           :try-syntax-id (get-in nodes [0 :source :syntax-id])
           :clause-ordinal 0 :catch-clause-form-id form-id
           :catch-clause-syntax-id syntax-id
           :error-type-form-id form-id :error-type-syntax-id syntax-id
           :error-type-binding-id binding-id
           :catch-binding-form-id form-id
           :catch-binding-syntax-id syntax-id
           :catch-binding-id binding-id :catch-binding-scope-id scope-id
           :catch-binding-use-syntax-ids []
           :type-coverage-legality :pending-sh08
           :result-type-join-legality :pending-sh08
           :effect-registry-legality :pending-sh09
           :effect-profile-capability-legality :pending-sh09
           :profile-error-lowering-legality :pending-sh09
           :ownership-legality :pending-sh10
           :safety-classification :pending-sh11}]
         :branch-clauses
         [{:match-form-id (get-in nodes [1 :source :form-id])
           :match-syntax-id (get-in nodes [1 :source :syntax-id])
           :clause-ordinal 0 :pattern-kind :binding
           :pattern-form-id form-id :pattern-syntax-id syntax-id
           :pattern-value 'x :pattern-scope-id scope-id
           :pattern-binding-id binding-id :pattern-binding-ids [binding-id]
           :pattern-binding-use-syntax-ids []
           :branch-form-id form-id :branch-syntax-id syntax-id}]
         }
        governed-nodes
        (-> nodes
            (assoc-in
             [0 :attributes]
             {:protected-child-index 0 :handler-count 1
              :handler-child-indexes [1]
              :handler-clauses
              (mapv #(select-keys
                      % [:clause-ordinal :handler-child-index
                         :catch-clause-form-id :catch-clause-syntax-id
                         :error-type-form-id :error-type-syntax-id
                         :error-type-binding-id :catch-binding-form-id
                         :catch-binding-syntax-id :catch-binding-id
                         :catch-binding-scope-id])
                    (:handler-clauses relational-preimage))
              :evaluation-order
              :protected-then-ordered-typed-handler-candidates
              :runtime-reachability :not-asserted-by-sh07-b9
              :selection-policy :source-ordered-typed-handler-candidates
              :result-policy :protected-or-selected-handler-last
              :authenticated-sh06-artifact-id artifact-id
              :sh06-semantic-projection-id projection-id
              :type-coverage-legality :pending-sh08
              :result-type-join-legality :pending-sh08
              :effect-registry-legality :pending-sh09
              :effect-profile-capability-legality :pending-sh09
              :profile-error-lowering-legality :pending-sh09
              :ownership-legality :pending-sh10
              :safety-classification :pending-sh11})
            (assoc-in
             [1 :attributes]
             {:scrutinee-child-index 0 :branch-count 1
              :branch-child-indexes [1]
              :branch-clauses (:branch-clauses relational-preimage)
              :evaluation-order
              :scrutinee-then-source-ordered-pattern-candidates
              :runtime-reachability :not-asserted-by-sh07-b11
              :selection-policy :source-ordered-pattern-candidates
              :result-type-join :pending-sh08 :exhaustiveness :pending-sh08
              :authenticated-sh06-artifact-id artifact-id
              :sh06-semantic-projection-id projection-id})
            (assoc-in
             [5 :attributes]
             {:target-id target-id :binding-vector-form-id form-id
              :binding-vector-syntax-id syntax-id :binding-count 1
              :body-count 1 :initializer-child-indexes [0]
              :body-child-indexes [1]
              :evaluation-order :initializers-then-body-left-to-right
              :result-policy {:kind :last-body :child-index 1}}))
        governed-node-products (assoc node-products :nodes governed-nodes)
        core-node-requests
        (mapv
         (fn [n]
           {:purpose :sh07-b51-core-node-id
            :preimage
            {:domain :gravity/sh07-b51-core-node-id-preimage-v18
             :schema-version 18 :predecessor-authority-id (id 50)
             :core-form (:core-form n) :children (:children n)
             :attributes (:attributes n) :source (:source n)
             :resolved-binding-ids (:resolved-binding-ids n)
             :evaluation-kind (get-in n [:evaluation :kind])
             :evaluation-region (get-in n [:evaluation :region])
             :evaluation-owner-function-syntax-id
             (get-in n [:evaluation :owner-function-syntax-id])
             :evaluated-children (:children n) :namespace 'fixture
             :profile :meta :target :jvm :metadata {}
             :binding-context :expression :generated? false
             :preserved-declarations []
             :pending-fact-families [:types :effects :ownership :safety]}})
         governed-nodes)
        authenticated-request
        {:artifact :gravity/sh07-authenticated-sh06-core-request
         :schema-version 15
         :lineage {:authenticated-sh06-artifact-id artifact-id
                   :sh06-semantic-projection-id projection-id}
         :module {:profile :safe} :forms
         (conj (mapv :source nodes)
               {:form-id form-id :syntax-id syntax-id :scope-id scope-id
                :kind :symbol :value 'x :child-form-ids []
                :parent-form-id (get-in nodes [1 :source :form-id])})
         :top-level-form-ids [(get-in nodes [7 :source :form-id])]
         :binding-table
         [{:binding-id binding-id :name 'x :binding-class :lexical
           :definition-form-id form-id :definition-syntax-id syntax-id
           :scope-id scope-id}]
         :alias-table [] :resolution-table []
         :fragment-manifest
         [{:fragment-id (sha \7) :fragment-ordinal 0
           :root-form-ids [(get-in nodes [7 :source :form-id])]
           :form-ids (conj (mapv #(get-in % [:source :form-id]) nodes)
                           form-id)
           :local-binding-ids [binding-id] :external-binding-ids []
           :resolution-reference-syntax-ids []}]
         :fragment-coverage {} :module-assembly-manifest {}
         :macro-expansion-trace [] :macro-origin-traces []
         :macro-origin-expectation {} :projection-binding {}
         :provenance {:actual-source-path "literal-relational.gravity"}
         :scope :sh07-b15-keyword-map-lookup}
        carrier
        {:artifact :gravity/sh07-b51-raw-adapter-request-v18
         :schema-version 18 :authenticated-sh06-request authenticated-request
         :authenticated-sh06-verification-report {}
         :sh06-resolution-artifact {}
         :predecessor-executable-descriptor {}
         :v18-executable-descriptor {} :predecessor-contract-carrier {}
         :predecessor-raw-outcome {} :physical-invocation {}
         :scope :sh07-b51-vector-destructuring}
        requests
        (into
         core-node-requests
         [{:purpose :sh07-b51-binding-slot-id
           :preimage let-slot-preimage}
          {:purpose :sh07-b51-binding-slot-id
           :preimage loop-slot-preimage}
          {:purpose :sh07-b51-binding-extraction-id
           :preimage let-extraction-preimage}
          {:purpose :sh07-b51-binding-extraction-id
           :preimage loop-extraction-preimage}
          {:purpose :sh07-b51-recur-slot-mapping-id
           :preimage recur-mapping-preimage}])
        handler
        {:ordinal 0 :clause-ordinal 0 :clause-count 1
         :handler-child-index 1 :core-node-id (id 0)
         :form-id (get-in nodes [0 :source :form-id])
         :syntax-id (get-in nodes [0 :source :syntax-id])
         :protected-core-node-id (id 20) :handler-core-node-id (id 21)
         :catch-clause-form-id form-id :catch-clause-syntax-id syntax-id
         :error-type-form-id form-id :error-type-syntax-id syntax-id
         :error-type-binding-id binding-id :catch-binding-form-id form-id
         :catch-binding-syntax-id syntax-id :catch-binding-id binding-id
         :catch-binding-scope-id scope-id :catch-binding-use-syntax-ids []
         :candidate-error-transfers [] :evaluation-region nil
         :evaluation-owner-function-syntax-id nil
         :ordered-steps
         [:evaluate-protected
          :conditionally-select-source-ordered-typed-handler]
         :construction-order :protected-then-handlers-source-order-then-try
         :runtime-evaluation :protected-then-conditional-handler-chain
         :runtime-reachability :not-asserted-by-sh07-b9
         :selection-policy :source-ordered-typed-handler-candidates
         :result-policy :protected-or-selected-handler-last
         :authenticated-sh06-artifact-id artifact-id
         :sh06-semantic-projection-id projection-id
         :type-coverage-legality :pending-sh08
         :result-type-join-legality :pending-sh08
         :effect-registry-legality :pending-sh09
         :effect-profile-capability-legality :pending-sh09
         :profile-error-lowering-legality :pending-sh09
         :ownership-legality :pending-sh10
         :safety-classification :pending-sh11}
        branch
        {:ordinal 0 :clause-ordinal 0 :clause-count 1
         :core-node-id (id 1) :scrutinee-core-node-id (id 22)
         :branch-core-node-id (id 23) :branch-child-index 1
         :pattern-kind :binding :pattern-form-id form-id
         :pattern-syntax-id syntax-id :pattern-value 'x
         :branch-form-id form-id :branch-syntax-id syntax-id
         :pattern-binding-id binding-id :pattern-binding-scope-id scope-id
         :pattern-binding-ids [binding-id]
         :pattern-binding-use-syntax-ids []
         :conditional-region {:kind :match-branch
                              :match-syntax-id
                              (get-in nodes [1 :source :syntax-id])
                              :clause-ordinal 0}
         :scrutinee-evaluation :evaluate-scrutinee-once
         :branch-evaluation :conditionally-evaluate-selected-branch
         :authenticated-sh06-artifact-id artifact-id
         :sh06-semantic-projection-id projection-id}
        pattern
        {:ordinal 0 :clause-ordinal 0 :local-ordinal 0
         :parent-local-ordinal nil :root-local-ordinal 0
         :parent-ordinal nil :root-ordinal 0 :core-node-id (id 1)
         :depth 0 :path [] :pattern-kind :binding
         :pattern-form-id form-id :pattern-syntax-id syntax-id
         :pattern-value 'x :vector-width nil
         :pattern-binding-id binding-id :pattern-binding-scope-id scope-id
         :pattern-binding-use-syntax-ids []
         :authenticated-sh06-artifact-id artifact-id
         :sh06-semantic-projection-id projection-id}
        skeleton
        {:core-node-id (id 1) :scrutinee-core-node-id (id 22)
         :branch-core-node-ids [(id 23)]
         :selection-policy :source-ordered-pattern-candidates
         :runtime-reachability :not-asserted-by-sh07-b11
         :result-type-join :pending-sh08 :exhaustiveness :pending-sh08
         :authenticated-sh06-artifact-id artifact-id
         :sh06-semantic-projection-id projection-id}
        lexical
        {:let-core-node-id (id 4) :ordinal 0 :name 'x
         :binding-id binding-id :definition-form-id form-id
         :definition-syntax-id syntax-id :binding-scope-id scope-id
         :initializer-form-id form-id :initializer-syntax-id syntax-id
         :initializer-scope-id scope-id :initializer-node-id (id 27)
         :visible-prior-binding-ids [] :mutability :immutable}
        loop-binding
        (-> lexical (dissoc :let-core-node-id)
            (assoc :loop-core-node-id (id 5)
                   :initializer-node-id (id 29)))
        recursion
        {:ordinal 0 :kind :self-recursive
         :function-syntax-ids [(get-in nodes [2 :source :syntax-id])]
         :function-core-node-ids [(id 2)]
         :internal-call-edge-ordinals [0]}
        expected
        {:error-handlers [handler]
         :match-branch-records [branch]
         :match-decision-skeletons [skeleton]
         :match-pattern-records [pattern]
         :recursion-components [recursion]
         :lexical-bindings [lexical]
         :loop-bindings [loop-binding]
         :recur-targets [target]
         :recur-transfers [transfer]}
        result (derive [carrier requests governed-node-products])]
    (is (= :accepted (:status result)) :relational-seam-rejected-literal-base)
    (doseq [[key records] expected]
      (is (= records (get-in result [:products key]))
          [key :noncanonical-relational-product])
      (is (every? map? records) [key :records-required])
      (is (not-any? #(contains? % :attributes) records)
          [key :compact-node-witness-forbidden]))
    (doseq [[label changed]
            [[:carrier
              (assoc-in carrier
                        [:authenticated-sh06-request :lineage
                         :authenticated-sh06-artifact-id]
                        (sha \0))]
             [:slot (assoc-in requests [8 :preimage :owner-core-node-id]
                              (id 99))]
             [:extraction
              (assoc-in requests
                        [10 :preimage :binding-id]
                        (sha \9))]
             [:recur-map
              (assoc-in requests
                        [12 :preimage :target-loop-form-id]
                        (sha \8))]
             [:node-base
              (assoc-in governed-node-products
                        [:function-records 0 :fixed-arity] 2)]]]
      (let [[changed-carrier changed-requests changed-products]
            (case label
              :carrier [changed requests governed-node-products]
              (:slot :extraction :recur-map)
              [carrier changed governed-node-products]
              :node-base [carrier requests changed])]
        (is (= :rejected
               (:status (derive [changed-carrier changed-requests
                                 changed-products])))
            [label :relational-authentication-mutation-not-rejected])))))

(deftest sh07-b51-v18-root6-request-metadata-cannot-self-authorize
  (let [runtime (direct-source-runtime
                 'sh07-b51-r6-request-products-authentic?)
        projection-runtime
        (direct-source-runtime 'sh07-b51-r6-project-sh07-core-request)
        projected-lowering-runtime
        (direct-source-runtime 'sh07-b51-r6-lower-authenticated-forms)
        authentic?
        #((:invoke runtime) 'sh07-b51-r6-request-products-authentic? %)
        artifact-id (sha \a)
        revision-id (sha \b)
        projection-id (sha \c)
        alias-id (sha \d)
        sh05-id (sha \4)
        macro-trace-id (sha \5)
        macro-input-id (sha \6)
        macro-output-id (sha \7)
        macro-fn-id (sha \8)
        source-path "literal-sh06-authority.gravity"
        coverage
        {:root-form-count 0 :form-count 0 :local-binding-count 0
         :resolution-count 0 :fragment-count 0
         :covered-root-form-ids [] :covered-form-ids []
         :covered-local-binding-ids []
         :covered-resolution-reference-syntax-ids []}
        assembly
        {:ordered-fragment-ids [] :root-form-ids []
         :source-revision-id revision-id
         :sh06-semantic-projection-id projection-id
         :alias-table-id alias-id :content-id revision-id
         :module-id projection-id}
        request-base
        {:artifact :gravity/sh07-authenticated-sh06-core-request
         :schema-version 15
         :lineage {:authenticated-sh06-artifact-id artifact-id
                   :source-revision-id revision-id
                   :sh06-semantic-projection-id projection-id
                   :alias-table-id alias-id :sh05-artifact-id sh05-id
                   :macro-expansion-trace-id macro-trace-id}
         :module {:namespace 'root6.fixture :profile :safe :target :jvm
                  :safety :safe :effects [] :capabilities [] :exports []
                  :source-revision-id revision-id}
         :forms [] :top-level-form-ids []
         :fragment-manifest [] :fragment-coverage coverage
         :module-assembly-manifest assembly :binding-table [] :alias-table []
         :resolution-table [] :macro-expansion-trace []
         :macro-origin-traces []
         :macro-origin-expectation
         {:source-revision-id revision-id :sh05-artifact-id sh05-id
          :macro-expansion-trace-id macro-trace-id :expanded-defn-count 0
          :expected-input-syntax-ids []
          :expected-output-def-syntax-ids []
          :expected-introduced-fn-syntax-ids []}
         :projection-binding {}
         :provenance {:actual-source-path source-path}
         :scope :sh07-b15-keyword-map-lookup}
        semantic-module
        (select-keys (:module request-base)
                     [:namespace :profile :target :safety :effects
                      :capabilities :exports])
        semantic-sh05-id
        (bootstrap/reader-canonical-hash
         {:domain :gravity/sh07-semantic-sh05-artifact-v1
          :source-revision-id revision-id :expanded-forms []
          :macro-trace []})
        semantic-sh06-id
        (bootstrap/reader-canonical-hash
         {:domain :gravity/sh07-semantic-sh06-artifact-v1
          :source-revision-id revision-id :module semantic-module
          :expanded-forms [] :bindings [] :aliases [] :resolutions []})
        semantic-analysis-id
        (bootstrap/reader-canonical-hash
         {:domain :gravity/sh07-semantic-sh06-analysis-v1
          :source-revision-id revision-id :module semantic-module
          :bindings [] :aliases [] :resolutions []})
        semantic-alias-id
        (bootstrap/reader-canonical-hash
         {:domain :gravity/sh07-sh06-alias-table-v1 :aliases []})
        semantic-lineage
        {:authenticated-sh06-artifact-id artifact-id
         :sh06-semantic-projection-id semantic-sh06-id
         :sh06-artifact-id semantic-sh06-id
         :sh06-analysis-artifact-id semantic-analysis-id
         :source-revision-id revision-id :sh05-artifact-id semantic-sh05-id
         :expanded-syntax-stream-id
         (bootstrap/reader-canonical-hash
          {:domain :gravity/sh07-semantic-expanded-stream-v1
           :source-revision-id revision-id :expanded-forms []})
         :macro-expansion-trace-id
         (bootstrap/reader-canonical-hash
          {:domain :gravity/sh07-semantic-macro-trace-v1
           :source-revision-id revision-id :trace []})
         :binding-table-id
         (bootstrap/reader-canonical-hash
          {:domain :gravity/sh07-sh06-binding-table-v1 :bindings []})
         :resolution-table-id
         (bootstrap/reader-canonical-hash
          {:domain :gravity/sh07-sh06-resolution-table-v1 :resolutions []})
         :alias-table-id semantic-alias-id
         :lexical-scope-graph-id
         (bootstrap/reader-canonical-hash
          {:domain :gravity/sh07-sh06-lexical-scope-graph-v1
           :source-revision-id revision-id :graph {}})
         :authenticated-envelope-id
         (bootstrap/reader-canonical-hash
          {:domain :gravity/sh07-semantic-upstream-envelope-v1
           :source-revision-id revision-id
           :sh05-artifact-id semantic-sh05-id})}
        request
        (-> request-base
            (assoc :lineage semantic-lineage)
            (assoc-in [:macro-origin-expectation :sh05-artifact-id]
                      semantic-sh05-id)
            (assoc-in [:macro-origin-expectation :macro-expansion-trace-id]
                      (:macro-expansion-trace-id semantic-lineage))
            (assoc :module-assembly-manifest
                   (assoc assembly
                          :sh06-semantic-projection-id semantic-sh06-id
                          :module-id semantic-sh06-id
                          :alias-table-id semantic-alias-id)))
        sh06-check-keys
        #{:bounded-artifact-carrier? :bounded-semantic-components?
          :exact-kind? :accepted-status? :document-set-current?
          :governing-document-current? :fresh-upstream-sh05-verification?
          :plan-binding-current? :fresh-gravity-request-replay?
          :authenticated-resolution-request-current?
          :raw-template-result-current? :raw-analysis-current?
          :resolved-analysis-current? :digest-requests-current?
          :resolved-digests-current? :template-verification-current?
          :resolved-verification-current? :upstream-request-binding?
          :fresh-gravity-verification? :namespace-analysis-current?
          :binding-table-current? :alias-table-current?
          :import-export-current? :lexical-scope-current?
          :dependency-graph-current? :cross-profile-current?
          :invalidation-current? :resolution-table-current?
          :artifact-identity-current? :fresh-sh02-envelope-verified?
          :exact-artifact-shape? :exact-boundary-shape?
          :fixed-boundary-contract? :pass-contract-current?
          :execution-boundary-current?
          :component-transport-bounds-current?
          :aggregate-artifact-bounds-current? :provenance-current?
          :accepted-diagnostics-empty? :embedded-capability-proof-current?}
        sh06-checks (zipmap sh06-check-keys (repeat true))
        report
        {:artifact :gravity/sh06-resolution-artifact-verification
         :status :passed :failed-checks [] :source-path source-path
         :checks sh06-checks
         :gravity-verification {:status :passed}
         :upstream-verification {:status :passed}}
        raw-resolution-request
        {:artifact :gravity/sh06-authenticated-c4-resolution-request
         :schema-version 1
         :module (assoc (:module request) :package 'root6.fixture
                        :c4-artifact-id sh05-id)
         :definitions [] :imports [] :import-bindings []
         :lexical-scopes {} :references [] :core-bindings []
         :dependency-edges [] :macro-expansion-binding {}
         :provenance {:actual-source-path source-path}}
        resolved-analysis
        {:module-contract (:module raw-resolution-request) :resolution-table []
         :binding-table [] :alias-table [] :dependency-records []
         :exports [] :lexical-scope-graph {} :macro-expansion-binding {}
         :dependency-graph {:edges []} :cross-profile-edge-report []
         :incremental-invalidation-inputs []}
        boundary
        {:slice :SH-06 :owner :gravity-source
         :adapter-contract :gravity/sh06-resolution-adapter-contract
         :plan-binding {} :authenticated-resolution-request
         raw-resolution-request
         :raw-template-result {} :raw-analysis {}
         :resolved-analysis resolved-analysis :digest-requests []
         :resolved-digests [] :template-verification {:status :passed}
         :resolved-verification {:status :passed}
         :authenticated-envelope-descriptor {}
         :authenticated-envelope {} :target-source-reread? false
         :clojure-adapter-residual? true :self-hosted? false}
        proof
        (assoc sh06-checks
               :artifact :gravity/sh06-resolution-capability-proof
               :status :complete :failed-checks [])
        sh06-component-paths
        [[:gravity-resolution-boundary]
         [:document-set] [:provenance] [:pass] [:execution-boundary]
         [:capability-based-proof] [:diagnostics] [:sh05-macro-artifact]
         [:gravity-resolution-boundary :plan-binding]
         [:gravity-resolution-boundary :authenticated-resolution-request]
         [:gravity-resolution-boundary :raw-template-result]
         [:gravity-resolution-boundary :raw-analysis]
         [:gravity-resolution-boundary :resolved-analysis]
         [:gravity-resolution-boundary :digest-requests]
         [:gravity-resolution-boundary :resolved-digests]
         [:gravity-resolution-boundary :template-verification]
         [:gravity-resolution-boundary :resolved-verification]
         [:gravity-resolution-boundary :authenticated-envelope-descriptor]
         [:gravity-resolution-boundary :authenticated-envelope]
         [:namespace-analysis] [:binding-table] [:alias-table]
         [:import-export-table] [:lexical-scope-graph] [:dependency-graph]
         [:cross-profile-edge-report] [:incremental-invalidation-keys]
         [:resolution-table]]
        c2-reader-result-id (sha \1)
        c2-semantic-table-id (sha \2)
        c2-adapted-token-id (sha \3)
        c2-adapted-form-id (sha \4)
        c2-token-projection-id (sha \5)
        c2-form-projection-id (sha \6)
        decimal-projection-base
        {:artifact :gravity/sh03-c2-decimal-occurrence-projection
         :schema-version 1 :reader-result-id c2-reader-result-id
         :semantic-value-table-id c2-semantic-table-id
         :adapted-source-unit-id revision-id
         :adapted-token-stream-id c2-adapted-token-id
         :adapted-form-tree-id c2-adapted-form-id
         :token-id-projection-id c2-token-projection-id
         :form-id-projection-id c2-form-projection-id
         :occurrence-count 0 :occurrences []}
        decimal-projection
        (assoc decimal-projection-base :projection-id
               (bootstrap/reader-canonical-hash
                (assoc (dissoc decimal-projection-base
                               :artifact :schema-version)
                       :domain
                       :gravity/sh03-c2-decimal-occurrence-projection-v1)))
        c2-summary
        {:slice :SH-03 :status :accepted
         :adapter-contract :gravity/sh03-to-c2-reader-products-v3
         :source-unit-id (sha \7) :token-stream-id (sha \8)
         :form-tree-id (sha \9) :extension-invocation-set-id (sha \a)
         :reader-result-id c2-reader-result-id
         :semantic-value-table-id c2-semantic-table-id
         :adapted-source-unit-id revision-id
         :adapted-token-stream-id c2-adapted-token-id
         :adapted-form-tree-id c2-adapted-form-id
         :adapted-extension-invocation-set-id (sha \b)
         :token-id-projection-id c2-token-projection-id
         :form-id-projection-id c2-form-projection-id
         :decimal-occurrence-projection-id
         (:projection-id decimal-projection)}
        c2-artifact
        {:source-unit-record {:source-id revision-id :bytes-hash revision-id}
         :literal-decoding-records [] :form-tree []
         :semantic-error-deferment-record {:deferred-literal-records []}
         :sh07-decimal-occurrence-projection decimal-projection
         :gravity-reader-boundary
         {:adapter-contract :gravity/sh03-to-c2-reader-products-v3
          :decimal-occurrence-projection-id
          (:projection-id decimal-projection)
          :semantic-value-table-id c2-semantic-table-id
          :resolved-reader-result
          {:incremental-reader-hashes {:reader-result c2-reader-result-id}}
          :authenticated-envelope-descriptor
          {:semantic-projections
           [{:name :reader-product-identities :value c2-summary}]}}}
        resolution-artifact
        {:kind :gravity/sh06-resolution-artifact :status :accepted
         :slice :SH-06 :task "SH-06" :document-set ["L3" "C5"]
         :governing-document "C5" :artifact-id artifact-id
         :sh05-macro-artifact
         {:artifact-id sh05-id :expanded-syntax-stream []
          :macro-expansion-trace []
          :gravity-macro-boundary
          {:authenticated-sh04-artifact
           {:c2-reader-artifact c2-artifact}}}
         :gravity-resolution-boundary boundary
         :provenance {:source-path source-path
                      :source-revision-id revision-id}
         :pass {} :execution-boundary {}
         :capability-based-proof proof :diagnostics []
         :namespace-analysis
         (assoc resolved-analysis :artifact :gravity/namespace-analysis)
         :binding-table
         {:artifact :gravity/c5-binding-table :status :complete
          :bindings [] :resolution-table []}
         :alias-table
         {:artifact :gravity/c5-alias-table :status :complete :aliases []}
         :import-export-table
         {:artifact :gravity/c5-import-export-table :status :complete
          :imports [] :exports []}
         :lexical-scope-graph
         {:artifact :gravity/c5-lexical-scope-graph :status :complete
          :scopes {}}
         :dependency-graph
         {:artifact :gravity/c5-module-dependency-graph :status :complete
          :edges []}
         :cross-profile-edge-report
         {:artifact :gravity/c5-cross-profile-edge-report :status :complete
          :records []}
         :incremental-invalidation-keys
         {:artifact :gravity/c5-incremental-invalidation-keys :status :stable
          :inputs [] :invalidation-id (sha \9)}
         :resolution-table []}
        carrier
        {:artifact :gravity/sh07-b51-raw-adapter-request-v18
         :schema-version 18 :authenticated-sh06-request request
         :authenticated-sh06-verification-report report
         :sh06-resolution-artifact resolution-artifact
         :predecessor-executable-descriptor {}
         :v18-executable-descriptor {} :predecessor-contract-carrier {}
         :predecessor-raw-outcome {} :physical-invocation {}
         :scope :sh07-b51-vector-destructuring}
        products
        {:nodes [] :fragment-manifest [] :root-core-node-ids []
         :fragment-coverage coverage
         :module-assembly-manifest (:module-assembly-manifest request)
         :binding-table [] :declared-alias-table [] :resolution-table []
         :definitions [] :macro-expansion-trace []
         :macro-origin-traces (:macro-origin-traces request)
         :macro-origin-expectation (:macro-origin-expectation request)}
        extra-form {:form-id (sha \e) :syntax-id (sha \f)}
        coverage-with-form (assoc coverage :form-count 1)
        next-revision (sha \1)
        assembly-with-revision
        (assoc assembly :source-revision-id next-revision
               :content-id next-revision)
        forged-resolution [{:reference-syntax-id (sha \2)}]
        coverage-with-resolution (assoc coverage :resolution-count 1)
        coherent-mutations
        [[:fragment-coverage
          (-> request (assoc :forms [extra-form])
              (assoc :fragment-coverage coverage-with-form))
          (assoc products :fragment-coverage coverage-with-form)]
         [:module-assembly-manifest
          (-> request
              (assoc-in [:lineage :source-revision-id] next-revision)
              (assoc :module-assembly-manifest assembly-with-revision))
          (assoc products :module-assembly-manifest assembly-with-revision)]
         [:binding-table (assoc request :binding-table [{:forged true}])
          (assoc products :binding-table [{:forged true}])]
         [:declared-alias-table (assoc request :alias-table [{:forged true}])
          (assoc products :declared-alias-table [{:forged true}])]
         [:resolution-table
          (-> request (assoc :resolution-table forged-resolution)
              (assoc :fragment-coverage coverage-with-resolution))
          (-> products (assoc :resolution-table forged-resolution)
              (assoc :fragment-coverage coverage-with-resolution))]
         [:macro-expansion-trace
          (assoc request :macro-expansion-trace [{:forged true}])
          (assoc products :macro-expansion-trace [{:forged true}])]
         [:macro-origin-traces
          (assoc request :macro-origin-traces [{:forged true}])
          (assoc products :macro-origin-traces [{:forged true}])]
         [:macro-origin-expectation
          (assoc request :macro-origin-expectation {:forged true})
          (assoc products :macro-origin-expectation {:forged true})]]]
    (is (= 40 (count sh06-check-keys)) :full-sh06-check-catalog)
    (is (= 7 (count report)) :full-sh06-report-shape)
    (is (= 23 (count resolution-artifact)) :full-sh06-artifact-key-shape)
    (is (= 28 (count sh06-component-paths))
        :full-sh06-authenticated-component-catalog)
    (is (= 17 (count boundary)) :full-sh06-boundary-shape)
    (is (= (+ 3 (count sh06-check-keys)) (count proof))
        :full-sh06-proof-shape)
    (is (true?
         ((:invoke runtime) 'sh07-b51-r6-sh06-evidence-shape? [carrier]))
        :full-sh06-evidence-shape)
    (is (true? ((:invoke runtime) 'sh07-b51-r6-fresh-sh06? [carrier]))
        :full-sh06-fresh-evidence)
    (is (true? (authentic? [carrier products])) :literal-baseline)
    (let [projection-binding
          (fn [candidate]
            (bootstrap/reader-canonical-hash
             {:domain :gravity/sh07-authenticated-sh06-core-projection-v15
              :request (dissoc candidate :projection-binding :provenance)}))
          bind-projection
          (fn [candidate]
            (assoc candidate :projection-binding
                   (projection-binding candidate)))
          project
          #((:invoke projection-runtime)
            'sh07-b51-r6-project-sh07-core-request [%])
          projected-request (bind-projection request)
          projection-carrier
          (assoc carrier :authenticated-sh06-request projected-request)
          executable-form
          {:kind :integer :value 7 :form-id (sha \a)
           :syntax-id (sha \b) :parent-form-id nil :scope-id :expression
           :metadata {} :source-span nil :origin-chain []
           :generated-origin nil :child-form-ids []}
          projected-only
          (bind-projection (assoc projected-request :forms [executable-form]))
          raw-only
          (assoc-in projection-carrier
                    [:sh06-resolution-artifact :sh05-macro-artifact
                     :expanded-syntax-stream]
                    [{:form executable-form}])
          table-forgery
          (bind-projection
           (assoc projected-request :binding-table [{:forged true}]))
          fragment-forgery
          (bind-projection
           (assoc projected-request :fragment-manifest [{:forged true}]))
          coverage-forgery
          (bind-projection
           (assoc projected-request :fragment-coverage
                  (assoc coverage :fragment-count 1)))
          assembly-forgery
          (bind-projection
           (assoc projected-request :module-assembly-manifest
                  (assoc (:module-assembly-manifest projected-request)
                         :content-id (sha \0))))
          ;; A set's child topology is governed by canonical `pr-str` order,
          ;; not by the order asserted by the supplied projection.  Both the
          ;; honest and forged projections below have fresh declared bindings.
          raw-set #{1 2}
          namespace-root-scope-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-projected-namespace-root-scope-v1
            :source-revision-id revision-id
            :namespace 'root6.fixture})
          ordered-set-values (vec (sort-by pr-str raw-set))
          root-syntax-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-root-syntax-v1
            :source-revision-id revision-id :ordinal 0 :form raw-set})
          root-form-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-form-occurrence-v1
            :source-revision-id revision-id :root-syntax-id root-syntax-id
            :path [] :kind :set})
          child-form-id
          (fn [index]
            (bootstrap/reader-canonical-hash
             {:domain :gravity/sh07-form-occurrence-v1
              :source-revision-id revision-id
              :root-syntax-id root-syntax-id :path [index]
              :kind :integer}))
          child-syntax-id
          (fn [index]
            (bootstrap/reader-canonical-hash
             {:domain :gravity/sh07-form-syntax-v1
              :source-revision-id revision-id
              :root-syntax-id root-syntax-id :path [index]
              :kind :integer}))
          child-record
          (fn [index value]
            {:form-id (child-form-id index)
             :syntax-id (child-syntax-id index) :kind :integer
             :value value :parent-form-id root-form-id :child-form-ids []
             :source-span {} :origin-chain [] :generated-origin []
             :metadata {} :scope-id namespace-root-scope-id})
          root-record
          {:form-id root-form-id :syntax-id root-syntax-id :kind :set
           :value raw-set :parent-form-id nil
           :child-form-ids [(child-form-id 0) (child-form-id 1)]
           :source-span {} :origin-chain [] :generated-origin []
           :metadata {} :scope-id namespace-root-scope-id}
          canonical-set-forms
          [(child-record 0 (get ordered-set-values 0))
           (child-record 1 (get ordered-set-values 1)) root-record]
          swapped-set-forms
          [(child-record 0 (get ordered-set-values 1))
           (child-record 1 (get ordered-set-values 0)) root-record]
          set-sh05-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-semantic-sh05-artifact-v1
            :source-revision-id revision-id :expanded-forms [raw-set]
            :macro-trace []})
          set-sh06-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-semantic-sh06-artifact-v1
            :source-revision-id revision-id :module semantic-module
            :expanded-forms [raw-set] :bindings [] :aliases []
            :resolutions []})
          set-lineage
          (assoc semantic-lineage
                 :sh05-artifact-id set-sh05-id
                 :sh06-semantic-projection-id set-sh06-id
                 :sh06-artifact-id set-sh06-id
                 :expanded-syntax-stream-id
                 (bootstrap/reader-canonical-hash
                  {:domain :gravity/sh07-semantic-expanded-stream-v1
                   :source-revision-id revision-id
                   :expanded-forms [raw-set]})
                 :authenticated-envelope-id
                 (bootstrap/reader-canonical-hash
                  {:domain :gravity/sh07-semantic-upstream-envelope-v1
                   :source-revision-id revision-id
                   :sh05-artifact-id set-sh05-id}))
          set-fragment
          {:ordinal 0 :root-form-ids [root-form-id]
           :form-ids (mapv :form-id canonical-set-forms)
           :local-binding-ids [] :external-binding-ids []
           :resolution-reference-syntax-ids [] :alias-names []
           :root-node-ids [root-form-id] :fragment-id root-form-id
           :content-id root-form-id}
          set-coverage
          {:root-form-count 1 :form-count 3 :local-binding-count 0
           :resolution-count 0 :fragment-count 1
           :covered-root-form-ids [root-form-id]
           :covered-form-ids (mapv :form-id canonical-set-forms)
           :covered-local-binding-ids []
           :covered-resolution-reference-syntax-ids []}
          set-assembly
          {:ordered-fragment-ids [root-form-id]
           :root-form-ids [root-form-id] :source-revision-id revision-id
           :sh06-semantic-projection-id set-sh06-id
           :alias-table-id semantic-alias-id :content-id revision-id
           :module-id set-sh06-id}
          set-request-base
          (-> request
              (assoc :lineage set-lineage :forms canonical-set-forms
                     :top-level-form-ids [root-form-id]
                     :fragment-manifest [set-fragment]
                     :fragment-coverage set-coverage
                     :module-assembly-manifest set-assembly)
              (assoc-in [:macro-origin-expectation :sh05-artifact-id]
                        set-sh05-id))
          set-request (bind-projection set-request-base)
          swapped-set-request
          (bind-projection (assoc set-request-base :forms swapped-set-forms))
          set-carrier
          (-> carrier
              (assoc :authenticated-sh06-request set-request)
              (assoc-in [:sh06-resolution-artifact :sh05-macro-artifact
                         :expanded-syntax-stream]
                        [{:form raw-set :span {}}]))
          core-preimage
          (fn [form core-form children attributes evaluation-kind]
            {:domain :gravity/sh07-b51-core-node-id-preimage-v18
             :schema-version 18
             :predecessor-authority-id (internal-reference 0)
             :core-form core-form :children children
             :attributes attributes
             :source {:form-id (:form-id form) :syntax-id (:syntax-id form)
                      :semantic-span {} :origin-chain []
                      :generated-origin []}
             :resolved-binding-ids [] :evaluation-kind evaluation-kind
             :evaluation-region :module-initialization
             :evaluation-owner-function-syntax-id nil
             :evaluated-children children :namespace 'root6.fixture
             :profile :safe :target :jvm :metadata {}
             :binding-context namespace-root-scope-id :generated? false
             :preserved-declarations []
             :pending-fact-families [:types :effects :ownership :safety]})
          core-requests-for
          (fn [forms]
            (let [left (get forms 0) right (get forms 1) root (get forms 2)]
              [{:ordinal 0 :purpose :sh07-b51-predecessor-authority-id
                :preimage
                {:domain :gravity/sh07-b51-predecessor-authority-v18}}
               {:ordinal 1 :purpose :sh07-b51-core-node-id
                :preimage
                (core-preimage left :literal []
                               {:literal-kind :integer :value (:value left)}
                               :value)}
               {:ordinal 2 :purpose :sh07-b51-core-node-id
                :preimage
                (core-preimage right :literal []
                               {:literal-kind :integer :value (:value right)}
                               :value)}
               {:ordinal 3 :purpose :sh07-b51-core-node-id
                :preimage
                (core-preimage
                 root :collection-literal
                 [(internal-reference 1) (internal-reference 2)]
                 {:literal-kind :set :element-count 2
                  :source-value raw-set}
                 :left-to-right)}]))
          canonical-core-requests (core-requests-for canonical-set-forms)
          swapped-core-requests (core-requests-for swapped-set-forms)
          raw-decimal 1.20M
          decimal-evidence
          {:decoded raw-decimal :raw "1.20" :literal-id (sha \4)
           :form-id (sha \5) :token-id (sha \6)}
          decimal-root-syntax-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-root-syntax-v1
            :source-revision-id revision-id :ordinal 0
            :form raw-decimal})
          decimal-form-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-form-occurrence-v1
            :source-revision-id revision-id
            :root-syntax-id decimal-root-syntax-id :path []
            :kind :decimal})
          decimal-descriptor
          {:kind :gravity/arbitrary-decimal-literal
           :unscaled-value "120" :scale 2
           :literal-id
           (bootstrap/reader-canonical-hash
            {:domain :gravity/sh07-c2-decimal-literal-evidence-v1
             :literal-id (:literal-id decimal-evidence)
             :form-id (:form-id decimal-evidence)})
           :form-id
           (bootstrap/reader-canonical-hash
            {:domain :gravity/sh07-c2-decimal-form-evidence-v1
             :form-id (:form-id decimal-evidence)
             :literal-id (:literal-id decimal-evidence)})
           :token-id
           (bootstrap/reader-canonical-hash
            {:domain :gravity/sh07-c2-decimal-token-evidence-v1
             :token-id (:token-id decimal-evidence)
             :literal-id (:literal-id decimal-evidence)
             :form-id (:form-id decimal-evidence)})
           :projection-form-id decimal-form-id}
          decimal-form
          {:form-id decimal-form-id :syntax-id decimal-root-syntax-id
           :kind :decimal :value decimal-descriptor :parent-form-id nil
           :child-form-ids [] :source-span {} :origin-chain []
           :generated-origin [] :metadata {}
           :scope-id namespace-root-scope-id}
          decimal-sh05-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-semantic-sh05-artifact-v1
            :source-revision-id revision-id :expanded-forms [raw-decimal]
            :macro-trace []})
          decimal-sh06-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-semantic-sh06-artifact-v1
            :source-revision-id revision-id :module semantic-module
            :expanded-forms [raw-decimal] :bindings [] :aliases []
            :resolutions []})
          decimal-lineage
          (assoc semantic-lineage
                 :sh05-artifact-id decimal-sh05-id
                 :sh06-semantic-projection-id decimal-sh06-id
                 :sh06-artifact-id decimal-sh06-id
                 :expanded-syntax-stream-id
                 (bootstrap/reader-canonical-hash
                  {:domain :gravity/sh07-semantic-expanded-stream-v1
                   :source-revision-id revision-id
                   :expanded-forms [raw-decimal]})
                 :authenticated-envelope-id
                 (bootstrap/reader-canonical-hash
                  {:domain :gravity/sh07-semantic-upstream-envelope-v1
                   :source-revision-id revision-id
                   :sh05-artifact-id decimal-sh05-id}))
          decimal-fragment
          {:ordinal 0 :root-form-ids [decimal-form-id]
           :form-ids [decimal-form-id] :local-binding-ids []
           :external-binding-ids [] :resolution-reference-syntax-ids []
           :alias-names [] :root-node-ids [decimal-form-id]
           :fragment-id decimal-form-id :content-id decimal-form-id}
          decimal-coverage
          {:root-form-count 1 :form-count 1 :local-binding-count 0
           :resolution-count 0 :fragment-count 1
           :covered-root-form-ids [decimal-form-id]
           :covered-form-ids [decimal-form-id]
           :covered-local-binding-ids []
           :covered-resolution-reference-syntax-ids []}
          decimal-assembly
          {:ordered-fragment-ids [decimal-form-id]
           :root-form-ids [decimal-form-id]
           :source-revision-id revision-id
           :sh06-semantic-projection-id decimal-sh06-id
           :alias-table-id semantic-alias-id :content-id revision-id
           :module-id decimal-sh06-id}
          decimal-request
          (bind-projection
           (-> request
               (assoc :lineage decimal-lineage :forms [decimal-form]
                      :top-level-form-ids [decimal-form-id]
                      :fragment-manifest [decimal-fragment]
                      :fragment-coverage decimal-coverage
                      :module-assembly-manifest decimal-assembly)
               (assoc-in [:macro-origin-expectation :sh05-artifact-id]
                         decimal-sh05-id)))
          decimal-semantic-key
          {:kind :decimal :sign 1 :coefficient [49 50]
           :decimal-power -1}
          decimal-occurrence
          {:ordinal 0
           :semantic-value-entry
           {:artifact :gravity/semantic-value :schema-version 1
            :value-id (sha \a) :form-id (sha \b) :token-id (sha \c)
            :kind :decimal
            :descriptor
            {:artifact :gravity/decimal-literal :kind :decimal
             :raw "1.20" :integer-spelling "1"
             :fraction-spelling "20" :exponent-spelling nil
             :semantic-key decimal-semantic-key
             :semantic-validation :accepted
             :normalization-reason nil :reason nil}
            :semantic-key decimal-semantic-key}
           :adapted-form-id (:form-id decimal-evidence)
           :adapted-token-id (:token-id decimal-evidence)}
          decimal-occurrence-base
          (assoc decimal-projection-base
                 :occurrence-count 1 :occurrences [decimal-occurrence])
          decimal-occurrence-projection
          (assoc decimal-occurrence-base :projection-id
                 (bootstrap/reader-canonical-hash
                  (assoc (dissoc decimal-occurrence-base
                                 :artifact :schema-version)
                         :domain
                         :gravity/sh03-c2-decimal-occurrence-projection-v1)))
          decimal-summary
          (assoc c2-summary
                 :decimal-occurrence-projection-id
                 (:projection-id decimal-occurrence-projection))
          decimal-c2-artifact
          (-> c2-artifact
              (assoc :literal-decoding-records
                     [(assoc decimal-evidence
                             :kind :decimal :span {}
                             :facts {:exponent-spelling nil :exact? false})])
              (assoc :form-tree
                     [{:form-id (:form-id decimal-evidence) :form-index 0
                       :kind :decimal :collection-kind nil
                       :parent-form-id nil :children []
                       :open-token (:token-id decimal-evidence)
                       :close-token (:token-id decimal-evidence)
                       :open-token-index 0 :close-token-index 0
                       :token-range [0 0] :span {} :raw "1.20"
                       :value raw-decimal :semantic-key decimal-semantic-key
                       :subtree-form-count 1 :metadata {}
                       :surface-span {} :abbreviation nil :abbrev nil
                       :tag-codepoints nil :generated-origin nil :origin nil}])
              (assoc :sh07-decimal-occurrence-projection
                     decimal-occurrence-projection)
              (assoc-in [:gravity-reader-boundary
                         :decimal-occurrence-projection-id]
                        (:projection-id decimal-occurrence-projection))
              (assoc-in [:gravity-reader-boundary
                         :authenticated-envelope-descriptor
                         :semantic-projections]
                        [{:name :reader-product-identities
                          :value decimal-summary}]))
          decimal-carrier
          (-> carrier
              (assoc :authenticated-sh06-request decimal-request)
              (assoc-in [:sh06-resolution-artifact :sh05-macro-artifact
                         :expanded-syntax-stream]
                        [{:form raw-decimal :span {}}])
              (assoc-in [:sh06-resolution-artifact :sh05-macro-artifact
                         :gravity-macro-boundary
                         :authenticated-sh04-artifact :c2-reader-artifact]
                        decimal-c2-artifact))
          forged-decimal-request
          (bind-projection
           (assoc decimal-request :forms
                  [(assoc-in decimal-form [:value :unscaled-value] "12")]))
          lower-projected
          #((:invoke projected-lowering-runtime)
            'sh07-b51-r6-lower-authenticated-forms %)]
      (is (= (projection-binding projected-request)
             (:projection-binding projected-request))
          :independent-declared-projection-binding)
      (is (= {:status :accepted :request projected-request
              :forms {:status :accepted :forms [] :roots [] :traces []}
              :bindings {:status :accepted :bindings []}
              :resolutions {:status :accepted :resolutions []}
              :fragments
              {:status :accepted :manifest [] :coverage coverage
               :assembly (:module-assembly-manifest projected-request)}}
             (project projection-carrier))
          :actual-sh06-to-sh07-projection-baseline)
      ;; Recomputing the declared binding cannot authorize projected forms or
      ;; tables absent from the immutable SH05/SH06 evidence.
      (is (= :rejected
             (:status
              (project (assoc projection-carrier
                              :authenticated-sh06-request projected-only))))
          :coherent-projected-form-forgery)
      (is (= :rejected
             (:status
              (project (assoc projection-carrier
                              :authenticated-sh06-request table-forgery))))
          :coherent-projected-table-forgery)
      (is (= :rejected
             (:status
              (project (assoc projection-carrier
                              :authenticated-sh06-request
                              fragment-forgery))))
          :coherent-projected-fragment-forgery)
      (is (= :rejected
             (:status
              (project (assoc projection-carrier
                              :authenticated-sh06-request
                              coverage-forgery))))
          :coherent-projected-coverage-forgery)
      (is (= :rejected
             (:status
              (project (assoc projection-carrier
                              :authenticated-sh06-request
                              assembly-forgery))))
          :coherent-projected-assembly-forgery)
      (is (= :accepted (:status (project set-carrier)))
          :nonempty-set-uses-governed-canonical-order)
      (is (= :accepted
             (:status (lower-projected
                       [set-carrier canonical-core-requests])))
          :set-lowering-uses-canonical-order)
      (is (= :rejected
             (:status
              (project (assoc set-carrier :authenticated-sh06-request
                              swapped-set-request))))
          :supplied-set-order-cannot-authorize-itself)
      (is (= :rejected
             (:status
              (lower-projected
               [(assoc set-carrier :authenticated-sh06-request
                       swapped-set-request)
                swapped-core-requests])))
          :coherent-set-order-and-core-requests-cannot-self-authorize)
      (is (= :accepted (:status (project decimal-carrier)))
          :decimal-replays-authenticated-c2-literal-evidence)
      (is (= :rejected
             (:status
              (project
               (assoc decimal-carrier :authenticated-sh06-request
                      forged-decimal-request))))
          :decimal-descriptor-cannot-self-authorize)
      (is (= :rejected
             (:status
              (project
               (update-in decimal-carrier
                          [:sh06-resolution-artifact :sh05-macro-artifact
                           :gravity-macro-boundary
                           :authenticated-sh04-artifact :c2-reader-artifact
                           :literal-decoding-records]
                          conj decimal-evidence))))
          :duplicate-decimal-evidence-rejects)
      ;; Conversely, changing executable syntax while retaining the old
      ;; projected request must require a corresponding exact projection.
      (is (= :rejected (:status (project raw-only)))
          :executable-syntax-change-requires-projection-change))
    (let [reduced-report
          {:artifact :gravity/sh06-resolution-artifact-verification
           :status :passed :failed-checks [] :source-path source-path
           :checks {:shape true :lineage true}}
          reduced-artifact
          {:kind :gravity/sh06-resolution-artifact :status :accepted
           :artifact-id artifact-id :resolution-table []
           :provenance {:source-path source-path}
           :capability-based-proof
           {:artifact :gravity/sh06-resolution-capability-proof
            :status :complete :failed-checks []}
           :gravity-resolution-boundary
           {:authenticated-resolution-request raw-resolution-request
            :resolved-analysis resolved-analysis}}]
      (is (false?
           (authentic?
            [(assoc carrier
                    :authenticated-sh06-verification-report reduced-report
                    :sh06-resolution-artifact reduced-artifact)
             products]))
          :prior-reduced-sh06-schema-must-reject))
    (doseq [key (keys report)]
      (is (false?
           (authentic?
            [(update carrier :authenticated-sh06-verification-report
                     dissoc key)
             products]))
          [:report-key-deletion key]))
    (doseq [key (keys resolution-artifact)]
      (is (false?
           (authentic?
            [(update carrier :sh06-resolution-artifact dissoc key)
             products]))
          [:artifact-key-deletion key]))
    (doseq [key (keys boundary)]
      (is (false?
           (authentic?
            [(update-in carrier
                        [:sh06-resolution-artifact
                         :gravity-resolution-boundary]
                        dissoc key)
             products]))
          [:boundary-key-deletion key]))
    (doseq [key (keys proof)]
      (is (false?
           (authentic?
            [(update-in carrier
                        [:sh06-resolution-artifact :capability-based-proof]
                        dissoc key)
             products]))
          [:proof-key-deletion key]))
    (doseq [path sh06-component-paths]
      (let [parent-path (vec (butlast path))
            key (last path)]
        (is (false?
             (authentic?
              [(update carrier :sh06-resolution-artifact
                       #(update-in % parent-path dissoc key))
               products]))
            [:authenticated-component-deletion path])))
    (is (= 8 (count coherent-mutations)) :exact-fallback-family-count)
    (doseq [[family changed-request changed-products] coherent-mutations]
      (is (false?
           (authentic? [(assoc carrier :authenticated-sh06-request
                               changed-request)
                        changed-products]))
          [family :coherent-self-authorization]))
    (doseq [[label changed]
            [[:artifact
              (assoc-in carrier [:sh06-resolution-artifact :artifact-id]
                        (sha \3))]
             [:proof
              (assoc-in carrier
                        [:sh06-resolution-artifact
                         :capability-based-proof :status]
                        :partial)]
             [:report
              (assoc-in carrier
                        [:authenticated-sh06-verification-report :checks]
                        {:shape false})]]]
      (is (false? (authentic? [changed products]))
          [label :artifact-authority-mutation-not-rejected]))
    (doseq [[label changed]
            [[:empty-checks
              (assoc-in carrier
                        [:authenticated-sh06-verification-report :checks] {})]
             [:report-extra
              (assoc-in carrier
                        [:authenticated-sh06-verification-report :forged]
                        true)]
             [:proof-extra
              (assoc-in carrier
                        [:sh06-resolution-artifact
                         :capability-based-proof :forged]
                        true)]
             [:boundary-extra
              (assoc-in carrier
                        [:sh06-resolution-artifact
                         :gravity-resolution-boundary :forged]
                        true)]]]
      (is (false? (authentic? [changed products]))
          [label :exact-sh06-evidence-shape]))
    (doseq [[label changed]
            [[:report-missing
              (update carrier :authenticated-sh06-verification-report
                      dissoc :gravity-verification)]
             [:artifact-missing
              (update carrier :sh06-resolution-artifact
                      dissoc :namespace-analysis)]
             [:boundary-missing
              (update-in carrier
                         [:sh06-resolution-artifact
                          :gravity-resolution-boundary]
                         dissoc :raw-analysis)]
             [:proof-missing-check
              (update-in carrier
                         [:sh06-resolution-artifact
                          :capability-based-proof]
                         dissoc :raw-analysis-current?)]
             [:proof-check-mismatch
              (assoc-in carrier
                        [:sh06-resolution-artifact
                         :capability-based-proof :raw-analysis-current?]
                        false)]
             [:report-proof-coherent-false
              (-> carrier
                  (assoc-in [:authenticated-sh06-verification-report
                             :checks :raw-analysis-current?] false)
                  (assoc-in [:sh06-resolution-artifact
                             :capability-based-proof
                             :raw-analysis-current?] false))]
             [:failed-checks-mismatch
              (assoc-in carrier
                        [:authenticated-sh06-verification-report
                         :failed-checks]
                        [:raw-analysis-current?])]]]
      (is (false? (authentic? [changed products]))
          [label :full-sh06-evidence-family]))))

(deftest sh07-b51-v18-root6-exclusive-closure-cannot-call-producer-or-other-roots
  (let [definitions (definitions-by-name @source-forms)
        root6 'sh07-b51-build-independent-verifier-binding]
    (if-not (contains? definitions root6)
      (is true "Root6 is not defined yet; exact closure gate is dormant")
      (let [definition-names (set (keys definitions))
            direct-callees
            (fn direct-callees [value]
              (cond
                (and (seq? value) (= 'quote (first value))) #{}
                (seq? value)
                (into
                 (if (and (symbol? (first value))
                          (contains? definition-names (first value)))
                   #{(first value)} #{})
                 (mapcat direct-callees (rest value)))
                (coll? value) (into #{} (mapcat direct-callees value))
                :else #{}))
            closure
            (loop [pending [root6] members #{} edges #{}]
              (if (empty? pending)
                {:members members :edges edges}
                (let [function (peek pending)
                      pending (pop pending)]
                  (if (contains? members function)
                    (recur pending members edges)
                    (let [callees
                          (direct-callees
                           (nthnext (get definitions function) 2))]
                      (recur (into pending callees)
                             (conj members function)
                             (into edges
                                   (map #(vector function %) callees))))))))
            forbidden-exact
            (set
             (concat
              (remove #{root6} public-roots)
              '[sh07-b51-build-template-private
                sh07-b51-verify-template-private
                sh07-b51-resolve-digest-preimage-private
                sh07-b51-resolve-template-private
                sh07-b51-verify-resolved-private
                sh07-b51-r6-resolve-value
                sh07-b51-resolve-controlled-path
                sh07-b51-resolve-controlled-preimage
                sh07-b51-resolve-controlled-descriptors]))
            forbidden-prefixes
            ["sh07-b51-producer-"
             "sh07-b51-resolve-controlled-"
             "sh07-b51-materialize-"
             "sh07-b51-materialized-"
             "sh07-b51-template-"
             "sh07-b51-success-"
             "sh07-b51-outcome-"
             "sh07-b51-purpose-"
             "sh07-b51-reference-registry-"
             "sh07-b51-resolved-"]
            forbidden-members
            (filterv
             (fn [member]
               (or (contains? forbidden-exact member)
                   (some #(.startsWith (name member) %)
                         forbidden-prefixes)))
             (:members closure))]
        (is (contains? (:members closure) root6))
        (is (empty? (:undefined-callees closure))
            (:undefined-callees closure))
        (let [non-r6-members
              (filterv
               (fn [member]
                 (and (not= member root6)
                      (not (.startsWith (name member) "sh07-b51-r6-"))))
               (:members closure))]
          (is (empty? non-r6-members)
              {:closure-size (count (:members closure))
               :non-r6-count (count non-r6-members)
               :non-r6-sample (vec (take 20 non-r6-members))}))
        (is (empty? forbidden-members) forbidden-members)
        (is (not-any? #(contains? forbidden-exact (second %))
                      (:edges closure))
            (:edges closure))))))
(deftest sh07-b51-v18-attempt17-text-order-is-unsigned-utf8
  (let [compare-text bootstrap/p15-s23-sh07-canonical-text-compare
        bmp-private "\uE000"
        supplementary "\uD800\uDC00"]
    (is (= -1 (compare-text "a" "b")))
    (is (= 0 (compare-text "\u03B2" "\u03B2")))
    (is (= 1 (compare-text "\u00E9" "z")))
    ;; UTF-8 orders EE... before F0..., while UTF-16 orders the leading
    ;; surrogate D800 before E000. This detects accidental host comparison.
    (is (= -1 (compare-text bmp-private supplementary)))
    (is (neg? (compare supplementary bmp-private)))
    (is (= (str "#{\"" bmp-private "\" \"" supplementary "\"}")
           (:text
            (bootstrap/p15-s23-sh07-canonical-decoration
             "attempt17-unicode" #{supplementary bmp-private}))))
    (doseq [[left right expected]
            [["" "a" -1] ["same" "same" 0] ["z" "\u00E9" -1]
             [supplementary bmp-private 1]]]
      (is (= expected (compare-text left right)) [left right]))
    ;; Comparator is ordering-only. Resource admission belongs solely to G20.
    (is (= 1 (compare-text (apply str (repeat 262145 "x")) "x")))
    (doseq [malformed [(String. (char-array [(char 0xd800)]))
                       (String. (char-array [(char 0xdc00)]))]]
      (is (thrown? IllegalArgumentException
                   (compare-text malformed "x"))))))

(deftest sh07-b51-v18-attempt17-decoration-order-and-limits-are-exact
  (let [decorate bootstrap/p15-s23-sh07-canonical-decoration
        combine (deref (ns-resolve 'gravity.bootstrap
                                   'p15-s23-sh07-combine-decorations))
        stable-order (deref (ns-resolve 'gravity.bootstrap
                                        'p15-s23-sh07-stable-order-by))
        comparator bootstrap/p15-s23-sh07-canonical-text-compare
        nested-map {'z #{'beta 'alpha} 'a [2 1]}
        shared [7]
        exact-node-tree
        (vec (concat (repeat 511 (vec (repeat 7 0)))
                     [(vec (repeat 6 0))]))
        over-node-tree
        (vec (concat (repeat 511 (vec (repeat 7 0)))
                     [(vec (repeat 7 0))]))
        depth96 (nth (iterate vector 0) 96)
        depth97 (vector depth96)
        exact-output (apply str (repeat 262144 "a"))
        over-output (str exact-output "a")]
    (is (= {:maximum-nodes 4096 :maximum-depth 96
            :maximum-collection-width 512 :maximum-scalar-bytes 32768
            :maximum-output-bytes 262144}
           bootstrap/p15-s23-sh07-canonical-decoration-limits))
    (is (= "{a [2 1], z #{alpha beta}}"
           (:text (decorate "attempt17-nested" nested-map))))
    (is (= "#{alpha beta}"
           (:text (decorate "attempt17-set" #{'beta 'alpha}))))
    (is (= "{a [2 1], z #{alpha beta}}"
           (:text (decorate "attempt17-map" nested-map))))
    (is (= "[z a]" (:text (decorate "attempt17-vector" ['z 'a]))))
    (is (= "(z a)" (:text (decorate "attempt17-list" '(z a)))))
    (let [records [{:key "same" :value "z"}
                   {:key "same" :value "a"}]
          ordered
          (stable-order
           (fn [left right]
             (let [key-order (comparator (:key left) (:key right))]
               (if (zero? key-order)
                 (comparator (:value left) (:value right)) key-order)))
           records)]
      (is (= ["a" "z"] (mapv :value ordered))))
    (is (= [:decorated 4096]
           ((juxt :status :node-occurrences)
            (decorate "attempt17-node-exact" exact-node-tree))))
    (is (= :node-count-limit
           (:reason (decorate "attempt17-node-over" over-node-tree))))
    (is (= :decorated (:status (decorate "attempt17-depth-exact" depth96))))
    (is (= :depth-limit (:reason (decorate "attempt17-depth-over" depth97))))
    (is (= :decorated
           (:status (decorate "attempt17-width-exact" (vec (range 512))))))
    (is (= :collection-width-limit
           (:reason (decorate "attempt17-width-over" (vec (range 513))))))
    (is (= :decorated
           (:status (decorate "attempt17-scalar-exact"
                              (apply str (repeat 32766 "a"))))))
    (is (= :scalar-byte-limit
           (:reason (decorate "attempt17-scalar-over"
                              (apply str (repeat 32767 "a"))))))
    (let [multibyte-at-limit
          (str (apply str (repeat 8191 "\uD83D\uDE00")) "aa")
          multibyte-over (str multibyte-at-limit "a")]
      (is (= :decorated
             (:status (decorate "attempt17-multibyte-at-limit"
                                multibyte-at-limit))))
      (is (= :scalar-byte-limit
             (:reason (decorate "attempt17-multibyte-over"
                                multibyte-over)))))
    (let [integer-at-limit
          (java.math.BigInteger.
           (str "1" (apply str (repeat 32767 "0"))))
          integer-over
          (java.math.BigInteger.
           (str "1" (apply str (repeat 32768 "0"))))]
      (is (= :decorated
             (:status (decorate "attempt17-integer-at-limit"
                                integer-at-limit))))
      (is (= :scalar-byte-limit
             (:reason (decorate "attempt17-integer-over"
                                integer-over)))))
    (is (= :decorated
           (:status (combine 0 4096 "" "" ""
                             [{:text exact-output :node-occurrences 0
                               :maximum-depth 0}]))))
    (is (= :output-byte-limit
           (:reason (combine 0 4096 "" "" ""
                             [{:text over-output :node-occurrences 0
                               :maximum-depth 0}]))))
    (is (= 5 (:node-occurrences
              (decorate "attempt17-shared-dag" [shared shared]))))
    (let [active (doto (java.util.IdentityHashMap.)
                   (.put shared Boolean/TRUE))]
      (is (= :cyclic-value
             (:reason (decorate "attempt17-direct-cycle" shared 0 active))))
      (is (= :cyclic-value
             (:reason (decorate "attempt17-indirect-cycle" [shared]
                                0 active)))))
    (let [scalar-var
          (ns-resolve 'gravity.bootstrap 'p15-s23-sh07-o-scalar-text)
          scalar-text (var-get scalar-var)]
      (with-redefs-fn
        {scalar-var
         (fn [value]
           (if (contains? #{:collision-a :collision-b} value)
             {:text ":collision" :output-utf8-bytes 10}
             (scalar-text value)))}
        #(is (= :source-integrity-mismatch
                (:reason (decorate "attempt17-collision"
                                   {:collision-a 1
                                    :collision-b 2}))))))))

(deftest sh07-b51-v18-attempt17-unordered-failures-are-commutative-and-wrapped
  (let [bad-prefix (apply str (repeat 32767 "x"))
        bad-left (str bad-prefix "a") bad-right (str bad-prefix "b")
        permutations [[bad-left bad-right] [bad-right bad-left]]
        host-results
        (mapv
         (fn [values]
           (select-keys
            (bootstrap/p15-s23-sh07-canonical-decoration
             "attempt17-generic" (set values))
            [:reason]))
         permutations)
        runtime (direct-source-runtime 'sh07-b51-r6-order-set-children)
        decoration-runtime
        (direct-source-runtime 'sh07-b51-r6-canonical-decoration)
        gravity-decoration
        #((:invoke decoration-runtime) 'sh07-b51-r6-canonical-decoration %)
        gravity-order
        #((:invoke runtime) 'sh07-b51-r6-order-set-children %)
        parity-values ["\uE000" "\uD800\uDC00" ['z 'a] '(z a)
                       #{'beta 'alpha}
                       {'z #{'beta 'alpha} 'a [2 1]}]
        direct-scalar-exact
        (gravity-decoration [(apply str (repeat 32766 "x")) 0 1 #{}])
        direct-scalar-over
        (gravity-decoration [(apply str (repeat 32767 "x")) 0 1 #{}])
        multibyte-scalar-exact
        (str (apply str (repeat 8191 "\uD83D\uDE00")) "aa")
        multibyte-scalar-over (str multibyte-scalar-exact "a")
        direct-multibyte-exact
        (gravity-decoration [multibyte-scalar-exact 0 1 #{}])
        direct-multibyte-over
        (gravity-decoration [multibyte-scalar-over 0 1 #{}])
        exact-output-value
        (vec (concat (repeat 511 (apply str (repeat 509 "x")))
                     [(apply str (repeat 508 "x"))]))
        over-output-value
        (assoc exact-output-value 511 (apply str (repeat 509 "x")))
        direct-output-exact
        (gravity-decoration [exact-output-value 0 1 #{}])
        direct-output-over
        (gravity-decoration [over-output-value 0 1 #{}])
        gravity-failed (gravity-decoration [#{bad-left bad-right} 0 1 #{}])
        caller-failed
        (gravity-order [{} #{bad-left bad-right} [(sha \a) (sha \b)]
                        (sha \c) (sha \d) []])]
    (is (= [{:reason :unordered-decoration-failure}
            {:reason :unordered-decoration-failure}]
           host-results))
    (doseq [value parity-values]
      (let [host (bootstrap/p15-s23-sh07-canonical-decoration
                  "attempt17-parity" value)
            gravity (gravity-decoration [value 0 1 #{}])]
        (is (= (select-keys host
                            [:status :text :node-occurrences :maximum-depth])
               gravity)
            value)))
    (is (= :decorated (:status direct-scalar-exact)))
    (is (= {:status :failed :reason :scalar-byte-limit}
           direct-scalar-over))
    (is (= :decorated (:status direct-multibyte-exact)))
    (is (= {:status :failed :reason :scalar-byte-limit}
           direct-multibyte-over))
    (is (= :decorated (:status direct-output-exact)))
    (is (= {:status :failed :reason :output-byte-limit}
           direct-output-over))
    (is (= {:status :failed :reason :unordered-decoration-failure}
           gravity-failed))
    (is (= {:status :rejected :reason :unordered-decoration-failure}
           caller-failed))
    (is (not= (:status gravity-failed) (:status caller-failed)))
    (is (= (:reason gravity-failed) (:reason caller-failed)))))

(def ^:private g19-zero-key
  {:kind :decimal :sign 1 :coefficient [48] :decimal-power 0})

(defn- g19-key-with-coefficient-count [coefficient-count]
  {:kind :decimal
   :sign 1
   :coefficient (vec (repeat coefficient-count 49))
   :decimal-power (- 1 coefficient-count)})

(defn- g19-authentic-c2-evidence [text]
  (let [source-path (str "target/sh07-g19-" (hash text) ".gravity")
        artifact
        (bootstrap/compiler-c2-reader-source-artifact
         source-path (str text "\n"))
        projection (:sh07-decimal-occurrence-projection artifact)
        occurrence (first (:occurrences projection))]
    (when-not (and (= 1 (:occurrence-count projection)) occurrence)
      (throw (ex-info "expected one authentic decimal occurrence"
                      {:text text :projection projection})))
    {:c2-reader-artifact artifact
     :adapted-form-id (:adapted-form-id occurrence)
     :adapted-token-id (:adapted-token-id occurrence)}))

(defn- g19-mutated-c2-evidence [evidence path value]
  ;; Deliberately retain the original artifact/projection ids.  A well-shaped
  ;; local row is never authority once any authenticated product is changed.
  (assoc evidence :c2-reader-artifact
         (assoc-in (:c2-reader-artifact evidence) path value)))

(defn- exception-reason [thunk]
  (try
    (thunk)
    :no-exception
    (catch clojure.lang.ExceptionInfo error
      (:reason (ex-data error)))
    (catch IllegalArgumentException _
      :illegal-argument)))

(deftest sh07-b51-v18-g19-decimal-scientific-spelling-and-inverse-are-exact
  (let [producer-spell bootstrap/p15-s23-sh07-p-decimal-spell
        producer-inverse bootstrap/p15-s23-sh07-p-decimal-inverse
        producer-branch bootstrap/p15-s23-sh07-p-decimal-c2-branch
        oracle-spell bootstrap/p15-s23-sh07-o-decimal-spell
        oracle-inverse bootstrap/p15-s23-sh07-o-decimal-inverse
        oracle-branch bootstrap/p15-s23-sh07-o-decimal-c2-branch
        ordinary {:kind :decimal :sign 1
                  :coefficient [49 50] :decimal-power -1}
        negative (assoc ordinary :sign -1)
        power18 999999999999999999
        power19 {:artifact :gravity/signed-decimal-power
                 :sign :positive
                 :digit-codepoints (vec (repeat 19 57))}
        exact256 (g19-key-with-coefficient-count 252)
        exact257 (g19-key-with-coefficient-count 253)
        exact261 (g19-key-with-coefficient-count 257)
        over261 (g19-key-with-coefficient-count 258)]
    (doseq [[key expected]
            [[g19-zero-key "0E+0"]
             [ordinary "1.2E+0"]
             [negative "-1.2E+0"]
             [{:kind :decimal :sign 1 :coefficient [49]
               :decimal-power power18}
              "1E+999999999999999999"]
             [{:kind :decimal :sign 1 :coefficient [49]
               :decimal-power power19}
              "1E+9999999999999999999"]]]
      (is (= expected (producer-spell key)) [key :producer-spell])
      (is (= expected (oracle-spell key)) [key :oracle-spell])
      (is (= key (producer-inverse expected)) [expected :producer-inverse])
      (is (= key (oracle-inverse expected)) [expected :oracle-inverse]))
    ;; Raw-equivalent spellings do not survive as distinct ordered values.
    ;; Only their normalized four-key C2 value has spelling authority.
    (doseq [noncanonical ["1.20E+0" "12E-1" "+1.2E+0" "-0E+0"]]
      (is (= :unsupported-decimal-readable-spelling
             (exception-reason #(producer-inverse noncanonical))))
      (is (= :unsupported-decimal-readable-spelling
             (exception-reason #(oracle-inverse noncanonical)))))
    (doseq [negative-zero [(assoc g19-zero-key :sign -1)
                           (assoc g19-zero-key :decimal-power -1)]]
        (is (= :unsupported-decimal-readable-spelling
             (exception-reason #(producer-spell negative-zero))))
      (is (= :unsupported-decimal-readable-spelling
             (exception-reason #(oracle-spell negative-zero)))))
    (doseq [[key length branch-status]
            [[exact256 256 :exact-normalized]
             [exact257 257 :normalization-deferred]
             [exact261 261 :normalization-deferred]]]
      (let [producer-text (producer-spell key)
            oracle-text (oracle-spell key)
            evidence (g19-authentic-c2-evidence producer-text)]
        (is (= length (count producer-text)))
        (is (= producer-text oracle-text))
        (is (= key (producer-inverse producer-text)))
        (is (= key (oracle-inverse oracle-text)))
        (is (= branch-status
               (:status (producer-branch producer-text key evidence))))
        (is (= branch-status
               (:status (oracle-branch oracle-text key evidence))))
        (is (= key
               (:semantic-key
                (producer-branch producer-text key evidence))))
        (is (= key
               (:semantic-key
                (oracle-branch oracle-text key evidence))))
        (let [form-id (:adapted-form-id evidence)
              artifact (:c2-reader-artifact evidence)
              literal-index
              (first (keep-indexed
                      #(when (= form-id (:form-id %2)) %1)
                      (:literal-decoding-records artifact)))
              occurrence-index
              (first (keep-indexed
                      #(when (= form-id (:adapted-form-id %2)) %1)
                      (get-in artifact
                              [:sh07-decimal-occurrence-projection
                               :occurrences])))]
          (doseq [[label mutation]
                  [[:missing-literal
                    (assoc-in evidence
                              [:c2-reader-artifact
                               :literal-decoding-records] [])]
                   [:wrong-literal-raw
                    (g19-mutated-c2-evidence
                     evidence [:literal-decoding-records literal-index :raw]
                     (str producer-text "0"))]
                   [:wrong-semantic-key
                    (g19-mutated-c2-evidence
                     evidence
                     [:sh07-decimal-occurrence-projection :occurrences
                      occurrence-index :semantic-value-entry :semantic-key]
                     g19-zero-key)]
                   [:fabricated-evidence (assoc evidence :fabricated true)]
                   [:wrong-projection-id
                    (g19-mutated-c2-evidence
                     evidence [:sh07-decimal-occurrence-projection
                               :projection-id] (sha \f))]]]
          (is (= :unsupported-decimal-readable-spelling
                 (exception-reason
                  #(producer-branch producer-text key mutation)))
              [:producer key label])
          (is (= :unsupported-decimal-readable-spelling
                 (exception-reason
                  #(oracle-branch oracle-text key mutation)))
              [:oracle key label])))))
    (is (= :unsupported-decimal-readable-spelling
           (exception-reason #(producer-spell over261))))
    (is (= :unsupported-decimal-readable-spelling
           (exception-reason #(oracle-spell over261))))
    (doseq [malformed ["" "0" "0E-0" "01E+0" "1.E+0" "1.0E+0"
                       "1e+0" "1E0" "1E++0" "1E+00" "1E+0x"
                       (apply str (repeat 262 "1"))]]
      (is (= :unsupported-decimal-readable-spelling
             (exception-reason #(producer-inverse malformed))) malformed)
      (is (= :unsupported-decimal-readable-spelling
             (exception-reason #(oracle-inverse malformed))) malformed))))

(deftest sh07-b51-v18-g19-authentic-projection-keeps-a-b-c-and-equal-occurrences-distinct
  (let [state-b-text "1E+9999999999999999999"
        state-c-key (g19-key-with-coefficient-count 253)
        state-c-text (bootstrap/p15-s23-sh07-p-decimal-spell state-c-key)
        artifact
        (bootstrap/compiler-c2-reader-source-artifact
         "target/sh07-g19-three-states.gravity"
         (str "[1.2E+0 1.20E+0 " state-b-text " " state-c-text "]\n"))
        occurrences
        (get-in artifact [:sh07-decimal-occurrence-projection :occurrences])
        evidence
        (fn [occurrence]
          {:c2-reader-artifact artifact
           :adapted-form-id (:adapted-form-id occurrence)
           :adapted-token-id (:adapted-token-id occurrence)})
        entries (mapv :semantic-value-entry occurrences)
        descriptors (mapv :descriptor entries)]
    (is (= 4 (count occurrences)))
    (is (= (:semantic-key (nth entries 0))
           (:semantic-key (nth entries 1))))
    (is (not= (:adapted-form-id (nth occurrences 0))
              (:adapted-form-id (nth occurrences 1))))
    (is (not= (:adapted-token-id (nth occurrences 0))
              (:adapted-token-id (nth occurrences 1))))
    (is (= [:accepted :accepted :deferred :deferred]
           (mapv :semantic-validation descriptors)))
    (is (= [nil nil :host-independent-decimal-range nil]
           (mapv :reason descriptors)))
    (is (= [nil nil nil :reader-semantic-work-boundary]
           (mapv :normalization-reason descriptors)))
    (doseq [occurrence (subvec occurrences 0 3)]
      (let [key (get-in occurrence [:semantic-value-entry :semantic-key])
            text (bootstrap/p15-s23-sh07-p-decimal-spell key)
            replay (g19-authentic-c2-evidence text)]
        (is (= text
               (bootstrap/p15-s23-sh07-p-canonical-decimal-decoration
                (evidence occurrence) replay)))
        (is (= text
               (bootstrap/p15-s23-sh07-o-decimal-decoration key replay)))))
    (is (= :unsupported-decimal-readable-spelling
           (exception-reason
            #(bootstrap/p15-s23-sh07-p-canonical-decimal-decoration
              (evidence (nth occurrences 3))
              (g19-authentic-c2-evidence state-c-text)))))
    (is (= :unsupported-decimal-readable-spelling
           (exception-reason
            #(bootstrap/p15-s23-sh07-p-canonical-decimal-decoration
              (assoc (evidence (nth occurrences 0))
                     :adapted-token-id
                     (:adapted-token-id (nth occurrences 1)))
              (g19-authentic-c2-evidence "1.2E+0")))))))

(deftest sh07-b51-v18-g19-authentic-c2-carrier-traverses-nested-occurrences
  (let [source "[[1.2E+0] (2.3E+0) #{3.4E+0} {4.5E+0 5.6E+0}]\n"
        artifact
        (bootstrap/compiler-c2-reader-source-artifact
         "target/sh07-g19-nested-carrier.gravity" source)
        occurrences
        (get-in artifact [:sh07-decimal-occurrence-projection :occurrences])
        replays
        (into {}
              (map
               (fn [occurrence]
                 (let [key (get-in occurrence
                                   [:semantic-value-entry :semantic-key])]
                   [[(:adapted-form-id occurrence)
                     (:adapted-token-id occurrence)]
                    (g19-authentic-c2-evidence
                     (bootstrap/p15-s23-sh07-o-decimal-spell key))]))
               occurrences))
        carrier
        {:artifact :gravity/sh07-oracle-c2-carrier-v1
         :c2-reader-artifact artifact
         :root-adapted-form-id (first (:top-level-form-ids artifact))
         :decimal-replay-evidence replays}
        state-c-text
        (bootstrap/p15-s23-sh07-o-decimal-spell
         (g19-key-with-coefficient-count 253))
        state-c-artifact
        (bootstrap/compiler-c2-reader-source-artifact
         "target/sh07-g19-state-c-carrier.gravity"
         (str "[" state-c-text "]\n"))
        state-c-carrier
        {:artifact :gravity/sh07-oracle-c2-carrier-v1
         :c2-reader-artifact state-c-artifact
         :root-adapted-form-id (first (:top-level-form-ids state-c-artifact))
         :decimal-replay-evidence {}}]
    (is (= 5 (count occurrences)))
    (is (= "[[1.2E+0] (2.3E+0) #{3.4E+0} {4.5E+0 5.6E+0}]"
           (:text
            (bootstrap/p15-s23-sh07-canonical-decoration
             "attempt23-authentic-nested" nil carrier))))
    (is (= :unsupported-decimal-readable-spelling
           (:reason
            (bootstrap/p15-s23-sh07-canonical-decoration
             "attempt23-authentic-state-c" nil state-c-carrier))))))

(deftest sh07-b51-v18-g19-root6-decimal-parity-is-definition-gated
  (let [definitions (definitions-by-name @source-forms)
        required '[sh07-b51-r6-decimal-spell
                   sh07-b51-r6-decimal-inverse
                   sh07-b51-r6-decimal-c2-branch]]
    (if-not (every? #(contains? definitions %) required)
      (is true
          {:g19-root6 :not-yet-defined
           :missing (filterv #(not (contains? definitions %)) required)})
      (let [runtime (direct-source-runtime 'sh07-b51-r6-decimal-c2-branch)
            invoke #((:invoke runtime) %1 %2)
            keys [g19-zero-key
                  {:kind :decimal :sign 1
                   :coefficient [49 50] :decimal-power -1}
                  (g19-key-with-coefficient-count 252)
                  (g19-key-with-coefficient-count 253)
                  (g19-key-with-coefficient-count 257)]]
        (doseq [key keys]
          (let [expected (bootstrap/p15-s23-sh07-p-decimal-spell key)
                actual (invoke 'sh07-b51-r6-decimal-spell [key])
                evidence (g19-authentic-c2-evidence actual)
                c2-artifact (:c2-reader-artifact evidence)
                form-id (:adapted-form-id evidence)
                expected-status
                (if (<= (count actual) 256)
                  :exact-normalized :normalization-deferred)
                branch #(invoke 'sh07-b51-r6-decimal-c2-branch
                                [% form-id actual])]
            (is (= expected actual) [key :root6-spell])
            (is (= key (invoke 'sh07-b51-r6-decimal-inverse [actual])))
            (is (= expected-status (:status (branch c2-artifact))))
            (is (= key (:semantic-key (branch c2-artifact))))
            ;; Only authentic replay products authorize this operational C2
            ;; branch. Stale ids make even well-shaped local mutations fail.
            (doseq [[label mutation]
                    [[:missing-literal
                      (assoc c2-artifact :literal-decoding-records [])]
                     [:duplicate-literal
                      (update c2-artifact :literal-decoding-records
                              #(conj % (first %)))]
                     [:missing-form (assoc c2-artifact :form-tree [])]
                     [:duplicate-form
                      (update c2-artifact :form-tree #(conj % (first %)))]
                     [:wrong-decoded
                      (assoc-in c2-artifact
                                [:literal-decoding-records 0 :decoded]
                                :fabricated)]
                     [:wrong-raw
                      (assoc-in c2-artifact
                                [:literal-decoding-records 0 :raw]
                                (str actual "0"))]
                     [:wrong-kind
                      (assoc-in c2-artifact
                                [:literal-decoding-records 0 :kind] :ratio)]
                     [:wrong-semantic-key
                      (assoc-in c2-artifact
                                [:form-tree 0 :semantic-key] :fabricated)]
                     [:wrong-form-value
                      (assoc-in c2-artifact
                                [:form-tree 0 :value] :fabricated)]
                     [:wrong-branch-records
                      (if (<= (count actual) 256)
                        (assoc-in c2-artifact
                                  [:semantic-error-deferment-record
                                   :deferred-literal-records]
                                  [(first (:literal-decoding-records
                                           c2-artifact))])
                        (assoc-in c2-artifact
                                  [:semantic-error-deferment-record
                                   :deferred-literal-records] []))]
                     [:wrong-branch-status
                      (if (<= (count actual) 256)
                        (assoc-in c2-artifact
                                  [:literal-decoding-records 0 :facts
                                   :semantic-validation] :deferred)
                        (assoc-in c2-artifact
                                  [:semantic-error-deferment-record
                                   :deferred-literal-records 0 :reason]
                                  :forged-reader-boundary))]]]
              (is (= :unsupported-decimal-readable-spelling
                     (exception-reason #(branch mutation)))
                  [key label]))))))))

(def ^:private g20-observation-keys
  #{:artifact :status :reason :maximum :observed-before :observed-after
    :prospective-after :utf16-units-scanned :unicode-scalars-scanned})

(deftest sh07-b51-v18-g20-utf8-census-records-widths-and-limits-are-exact
  (let [observe bootstrap/p15-s23-sh07-observe-utf8-byte-count
        cases [["\u0000" 1 1] ["\u007f" 1 1]
               ["\u0080" 2 1] ["\u07ff" 2 1]
               ["\u0800" 3 1] ["\ud7ff" 3 1] ["\ue000" 3 1]
               ["\uffff" 3 1] ["\ud800\udc00" 4 1]
               ["\udbff\udfff" 4 1]]]
    (doseq [[text bytes scalars] cases]
      (let [record (observe text 0 bytes)]
        (is (= g20-observation-keys (set (keys record))) text)
        (is (= [:gravity/sh07-utf8-byte-observation-v1 :accepted nil
                bytes bytes (.length ^String text) scalars]
               [(:artifact record) (:status record) (:reason record)
                (:observed-after record) (:prospective-after record)
                (:utf16-units-scanned record)
                (:unicode-scalars-scanned record)]) text)
        (is (= {:status :rejected :reason :utf8-byte-limit
                :observed-after 0 :prospective-after bytes
                :utf16-units-scanned 0 :unicode-scalars-scanned 0}
               (select-keys (observe text 0 (dec bytes))
                            [:status :reason :observed-after
                             :prospective-after :utf16-units-scanned
                             :unicode-scalars-scanned])) text)))
    (doseq [maximum [268435456 1073741824]
            [text bytes] [["a" 1] ["\u0080" 2] ["\u0800" 3]
                          ["\ud800\udc00" 4]]]
      (let [prior (- maximum bytes)
            exact (observe text prior maximum)
            over (observe text (inc prior) maximum)]
        (is (= [:accepted maximum maximum]
               [(:status exact) (:observed-after exact)
                (:prospective-after exact)]))
        (is (= [:rejected :utf8-byte-limit (inc prior)
                (inc maximum)]
               [(:status over) (:reason over) (:observed-after over)
                (:prospective-after over)]))))
    (let [left "a\u0080" right "\u0800\ud800\udc00"
          first-record (observe left 7 100)
          second-record (observe right (:observed-after first-record) 100)
          combined (observe (str left right) 7 100)]
      (is (= :accepted (:status first-record)))
      (is (= :accepted (:status second-record)))
      (is (= (:observed-after combined) (:observed-after second-record)))
      (is (= (:utf16-units-scanned combined)
             (+ (:utf16-units-scanned first-record)
                (:utf16-units-scanned second-record))))
      (is (= (:unicode-scalars-scanned combined)
             (+ (:unicode-scalars-scanned first-record)
                (:unicode-scalars-scanned second-record)))))))

(deftest sh07-b51-v18-g20-utf8-census-malformed-and-contract-closure-fail-closed
  (let [observe bootstrap/p15-s23-sh07-observe-utf8-byte-count
        malformed [(String. (char-array [(char 0xd800)]))
                   (String. (char-array [(char 0xdc00)]))
                   (String. (char-array [(char 0xd800) \a]))
                   (String. (char-array [(char 0xdc00) (char 0xd800)]))
                   (String. (char-array [(char 0xd800) (char 0xd800)]))]
        callers bootstrap/p15-s23-sh07-observe-utf8-byte-count-direct-callers
        contract (get bootstrap/p15-s23-sh07-declared-builtin-contracts
                      'sh07-observe-utf8-byte-count)]
    (doseq [text malformed]
      (is (= {:status :rejected
              :reason :invalid-unicode-scalar-sequence
              :prospective-after nil}
             (select-keys (observe text 0 16)
                          [:status :reason :prospective-after]))))
    (is (= 3 (get bootstrap/p15-s23-sh07-declared-builtin-exact-arities
                  'sh07-observe-utf8-byte-count)))
    (is (= g20-observation-keys (get-in contract [:result :keys])))
    (is (= #{nil :utf8-byte-limit :invalid-unicode-scalar-sequence}
           (get-in contract [:result :reasons])))
    (is (= 1073741824 (:maximum contract)))
    (is (= 1073741828 (:maximum-prospective-after contract)))
    (is (= #{} (:effects contract)))
    (is (= #{} (:capabilities contract)))
    (is (= :forbidden (:dynamic-resolution contract)))
    (is (= :forbidden (:public-runtime-call contract)))
    (is (not (contains? callers 'sh07-b51-finalize-rejection)))
    (is (not (contains? callers 'sh07-b51-build-independent-verifier-binding)))
    (doseq [arguments [["a" -1 1] ["a" 2 1] ["a" 0 1073741825]
                       [1 0 1] ["a" 0.0 1] ["a" 0 1.0]]]
      (is (= :illegal-argument
             (exception-reason #(apply observe arguments))) arguments))
    (doseq [mutation [(assoc contract :arity 2)
                      (assoc contract :maximum 1073741825)
                      (assoc contract :effects #{:io})
                      (assoc contract :capabilities #{:encoding})
                      (assoc-in contract [:result :keys]
                                (disj g20-observation-keys :prospective-after))]]
      (is (not= contract mutation)))))

(deftest sh07-b51-v18-internal-builtin-caller-authority-is-compiler-derived
  (let [compiler-definitions
        (definitions-by-name
         (read-gravity-source-forms "bootstrap/gravity/p15_s23/compiler.gravity"))
        emitter (nth (get compiler-definitions
                          'p15-s23-stage2-plan-emitter) 2)
        checked-source (slurp source-relative-path)
        macro-artifact
        (bootstrap/macro-source-artifact source-relative-path checked-source)
        source-module
        (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))
        function-table (bootstrap/stage0-function-table source-module)
        source-authority
        (bootstrap/p15-s23-sh07-compiler-source-authority
         source-relative-path checked-source source-module function-table)
        module
        (assoc source-module
               :function-table function-table
               :p15-s23-sh07-source-authority source-authority)
        source-definition
        (get function-table 'sh07-b51-producer-insert-decoration)
        source-call
        (first
         (filter #(and (seq? %)
                       (= 'sh07-canonical-text-compare (first %)))
                 (tree-seq coll? seq (:body source-definition))))
        instruction
        (bootstrap/p15-s23-stage2-seed-compile-expr
         emitter
         (assoc module
                :p15-s23-sh07-enclosing-function
                'sh07-b51-producer-insert-decoration
                :p15-s23-sh07-enclosing-definition source-definition)
         (set (filter symbol? (rest source-call)))
         source-call)
        definition
        (assoc source-definition :instructions [instruction])
        source-sha256
        (str "sha256:" (bootstrap/sha256-hex checked-source))
        plan-base
        {:kind :gravity/stage2-compiler-artifact-plan
         :compiler-artifact-plan? true
         :entrypoint 'main
         :source {:sha256 source-sha256}
         :compiler
         {:owner :gravity-source
          :stage :p15-s23-stage2-expression-lowering
          :compiled-by :clojure-stage0-seed
          :generic-bridge-residual? true
          :artifact-hash
          (bootstrap/p15-s23-stage2-compiler-artifact-expected-artifact-hash)
          :source-content-hash
          bootstrap/p15-s23-stage2-compiler-artifact-expected-source-content-hash
          :semantic-hash
          bootstrap/p15-s23-stage2-compiler-artifact-expected-semantic-hash}
         :module (select-keys module
                              [:module :profile :target :effects
                               :capabilities :exports :safety])
         :functions
         {'sh07-b51-producer-insert-decoration definition}
         :instruction-summary {}
         :effect-summary {:declared #{} :inferred #{} :capabilities #{}}}
        plan-id
        ((var-get
          (ns-resolve 'gravity.bootstrap 'p15-s23-sh07-runtime-plan-id))
         plan-base)
        plan (assoc plan-base :plan-id plan-id)
        provenance-authentic?
        (var-get
         (ns-resolve 'gravity.bootstrap
                     'p15-s23-sh07-runtime-provenance-authentic?))
        invoke
        (fn [candidate-plan candidate-instruction arguments]
          (try
            (bootstrap/p15-s23-sh07-runtime-invoke-compiler-internal-builtin
             {} candidate-plan candidate-instruction arguments)
            :accepted
            (catch clojure.lang.ExceptionInfo error
              (:id (ex-data error)))))
        cached-binding-var
        (ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding)
        authenticated-binding
        {:plan plan :source-content-hash source-sha256}
        authenticated-invoke
        (fn [arguments]
          (with-redefs-fn
            {cached-binding-var (delay authenticated-binding)}
            #(try
               (bootstrap/p15-s23-sh07-runtime-invoke-compiler-internal-builtin
                {:sh07-core-binding authenticated-binding}
                plan instruction arguments)
               {:id :accepted}
               (catch clojure.lang.ExceptionInfo error
                 (select-keys (ex-data error) [:id :reason])))))
        mutations
        [[:plan-id (assoc plan :plan-id (sha \f)) instruction]
         [:source
          (assoc-in plan [:source :sha256] (sha \e)) instruction]
         [:function
          (assoc-in plan
                    [:functions 'sh07-b51-producer-insert-decoration :body]
                    '[(sh07-canonical-text-compare right left)])
          instruction]
         [:instruction plan
          (assoc-in instruction
                    [:compiler-internal-provenance :function]
                    'fabricated-caller)]
         [:compiler-artifact
          (assoc-in plan [:compiler :artifact-hash] (sha \d)) instruction]]]
    (is (= {:op :builtin-call
            :function 'sh07-canonical-text-compare
            :compiler-internal-caller
            'sh07-b51-producer-insert-decoration}
           (select-keys instruction
                        [:op :function :compiler-internal-caller])))
    (is (true? (provenance-authentic? plan instruction)))
    ;; Runtime success remains pinned to sh07-core-cached-binding; an otherwise
    ;; complete hand-built public plan is not authority.
    (is (= "L2-BUILTIN-ERROR" (invoke plan instruction ["a" "b"])))
    (doseq [[label candidate-plan candidate-instruction] mutations]
      (is (= "L2-BUILTIN-ERROR"
             (invoke candidate-plan candidate-instruction ["a" "b"]))
          label))
    (is (= "L2-BUILTIN-ERROR" (invoke plan instruction [1 "b"])))
    (is (= "L2-BUILTIN-ERROR" (invoke plan instruction ["a"])))
    ;; These reach the handler through valid plan/function/instruction
    ;; provenance. Wrong types are contained as builtin errors; wrong arity
    ;; retains its distinct closed diagnostic.
    (is (= {:id "L2-BUILTIN-ERROR" :reason :builtin-contract-rejection}
           (authenticated-invoke [1 "b"])))
    (is (= {:id "L2-BUILTIN-ARITY"}
           (authenticated-invoke ["a"])))
    (let [forged-module
          {:module 'gravity.checked-core :profile :meta :target :jvm
           :effects #{} :capabilities #{} :safety :safe
           :source-path source-relative-path :function-table {}}
          forged-definition
          {:name 'sh07-b51-producer-insert-decoration
           :params '[left right]
           :body '[(sh07-canonical-text-compare left right)]}]
      (is (= "L2-UNKNOWN-CORE-FORM"
             (try
               (bootstrap/p15-s23-stage2-seed-compile-function
                emitter forged-module forged-definition)
               :accepted
               (catch clojure.lang.ExceptionInfo error
                 (:id (ex-data error))))))
      (is (= "L2-BUILTIN-ARITY"
             (try
               (bootstrap/p15-s23-stage2-seed-compile-function
                emitter forged-module
                (assoc forged-definition
                       :body '[(sh07-canonical-text-compare left)]))
               :accepted
               (catch clojure.lang.ExceptionInfo error
                 (:id (ex-data error)))))))
    (is (= '#{sh07-b51-producer-insert-decoration
              sh07-b51-r6-decoration-insert
              p15-s23-sh07-canonical-decoration}
           bootstrap/p15-s23-sh07-canonical-text-compare-direct-callers))
    (is (= '#{sh07-b51-producer-observe-decoration-bytes
              sh07-b51-r6-observe-scalar-bytes
              p15-s23-sh07-o-append-scalar-fragment!
              p15-s23-sh07-combine-decorations
             p15-s23-sh07-o-c2-decoration}
           bootstrap/p15-s23-sh07-observe-utf8-byte-count-direct-callers))))

(deftest sh07-b51-v18-root6-central-check-matrix-is-field-specific
  (let [definitions (definitions-by-name @source-forms)
        definition (get definitions 'sh07-b51-r6-evaluate-check-booleans)
        direct (source-direct-callees (set (keys definitions))
                                      (nthnext definition 2))
        field-specific
        {:owning-root-fragment 'sh07-b51-r6-record-owners-authentic?
         :external-binding-authorization 'sh07-b51-r6-all-resolutions-authorized?
         :vector-pattern-grammar 'sh07-b51-r6-binding-resource-bounds?
         :slot-extraction-reconstruction 'sh07-b51-r6-record-vector-authentic?
         :empty-vector-width 'sh07-b51-r6-vector-check-coverage-authentic?
         :visibility-and-publication 'sh07-b51-r6-publications-authentic?
         :runtime-check-order 'sh07-b51-r6-runtime-checks-authentic?
         :loop-recur-atomicity 'sh07-b51-r6-recur-mappings-authentic?
         :semantic-product-closure 'sh07-b51-r6-reconstruct-products
         :digest-dag 'sh07-b51-r6-plan-dag-exact?
         :tier3-identity 'sh07-b51-r6-plan-product-cardinality?
         :tier4-observation-provenance
         'sh07-b51-r6-tier4-preimages-authentic?}
        runtime (direct-source-runtime 'sh07-b51-r6-check-proof-kind)
        proof-kind #((:invoke runtime) 'sh07-b51-r6-check-proof-kind [%])
        vector-runtime
        (direct-source-runtime 'sh07-b51-r6-vector-check-coverage-authentic?)
        publication-runtime
        (direct-source-runtime 'sh07-b51-r6-publications-authentic?)
        runtime-check-runtime
        (direct-source-runtime 'sh07-b51-r6-runtime-checks-authentic?)
        recur-runtime
        (direct-source-runtime 'sh07-b51-r6-recur-mappings-authentic?)
        empty-products
        {:binding-extractions [] :runtime-checks []
         :publication-events [] :recur-slot-mappings []}
        invoke-vector
        #((:invoke vector-runtime)
          'sh07-b51-r6-vector-check-coverage-authentic? [%])
        invoke-publications
        #((:invoke publication-runtime)
          'sh07-b51-r6-publications-authentic? [%1 %2])
        invoke-runtime-checks
        #((:invoke runtime-check-runtime)
          'sh07-b51-r6-runtime-checks-authentic? [%1 %2])
        invoke-recur
        #((:invoke recur-runtime)
          'sh07-b51-r6-recur-mappings-authentic? [%1 %2])]
    (is (= 21 (count root6-check-order)))
    (is (= root6-check-order
           ((:invoke (direct-source-runtime 'sh07-b51-r6-check-order))
            'sh07-b51-r6-check-order [])))
    (is (= 21 (count (set (map proof-kind root6-check-order)))))
    (doseq [[check predicate] field-specific]
      (is (some #{check} root6-check-order) check)
      (is (contains? direct predicate) [check predicate]))
    ;; Checks 13-16 accept the exact governed empty families, but reject an
    ;; injected row or non-vector family.  This is executable evidence that
    ;; these rows call their field-specific validators rather than trusting a
    ;; supplied boolean/check transcript.
    (is (true? (invoke-vector empty-products)))
    (is (true? (invoke-publications [] empty-products)))
    (is (true? (invoke-runtime-checks [] empty-products)))
    (is (true? (invoke-recur [] empty-products)))
    (is (false?
         (invoke-vector
          (assoc empty-products :binding-extractions
                 [{:kind :vector-node :expected-width 0
                   :extraction-id (internal-reference 0)}]))))
    (is (false?
         (invoke-publications
          [] (assoc empty-products :publication-events [{}]))))
    (is (false?
         (invoke-runtime-checks
          [] (assoc empty-products :runtime-checks [{}]))))
    (is (false?
         (invoke-recur
          [] (assoc empty-products :recur-slot-mappings [{}]))))
    (is (false? (invoke-vector
                 (assoc empty-products :runtime-checks nil))))
    (is (false? (invoke-publications
                 [] (assoc empty-products :publication-events nil))))
    (is (false? (invoke-runtime-checks
                 [] (assoc empty-products :runtime-checks nil))))
    (is (false? (invoke-recur
                 [] (assoc empty-products :recur-slot-mappings nil))))
    (is (not (contains? definitions 'sh07-b51-r6-checks)))))
