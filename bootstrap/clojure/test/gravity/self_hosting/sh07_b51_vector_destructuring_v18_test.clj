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

(defn- g13-authenticated-form
  "Return the one raw form carrying form-id, or nil at the integrity boundary.

  This intentionally uses a fresh scan of the authenticated form vector.  The
  pending oracle must not borrow Root 1's indexed selector or its pattern
  enumerator when deriving the expected diagnostic.
  "
  [request form-id]
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
    (let [{:keys [root] _chain :chain} (g13-local-parent-chain request enclosing)
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
  (letfn [(walk [form path expected-parent _descendant? visited first-by-name]
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

(defn- g13-local-resource-priority
  "Apply module-wide priority only after every slot has been collected."
  [slots]
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
  [request _indexes]
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

(defn- g13-owner-for-pattern [request _indexes pattern-form]
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

(defn- g13-recur-owner [request _indexes recur-form]
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

(defn- g13-target-selection-oracle [_request indexes fragment recur-form]
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
            (into
             (if (and (symbol? (first value))
                      (contains? definition-names (first value)))
               #{(first value)} #{})
             (mapcat direct-callees (rest value)))
            (coll? value) (into #{} (mapcat direct-callees value))
            :else #{}))
        call-count
        (fn call-count [value target]
          (cond
            (and (seq? value) (= 'quote (first value))) 0
            (seq? value)
            (+ (if (= target (first value)) 1 0)
               (reduce + 0 (map #(call-count % target) (rest value))))
            (coll? value) (reduce + 0 (map #(call-count % target) value))
            :else 0))
        closure
        (loop [pending [root8] members #{} edges #{}]
          (if (empty? pending)
            {:members members :edges edges}
            (let [function (peek pending)
                  pending (pop pending)]
              (if (contains? members function)
                (recur pending members edges)
                (let [form (get definitions function)
                      callees (direct-callees (nth form 3 nil))]
                  (recur (into pending callees)
                         (conj members function)
                         (into edges (map #(vector function %) callees))))))))
        ;; Start at the raw Root-8 replay seam, below the one permitted Root-1
        ;; admission call.  This closure is the Root-8 replay itself, so a
        ;; forbidden producer selector cannot hide behind Root-1's path.
        r8-root 'sh07-b51-r8-reconstruct-pending-semantic
        independent-closure
        (loop [pending [r8-root] members #{} edges #{}]
          (if (empty? pending)
            {:members members :edges edges}
            (let [function (peek pending)
                  pending (pop pending)]
              (if (contains? members function)
                (recur pending members edges)
                (let [form (get definitions function)
                      callees
                      (if (= function root1)
                        #{}
                        (direct-callees (nth form 3 nil)))]
                  (recur (into pending callees)
                         (conj members function)
                         (into edges (map #(vector function %) callees))))))))
        root8-form (get definitions root8)
        root8-body (nth root8-form 3)
        root8-direct (direct-callees root8-body)
        forbidden-edges (filterv #(contains? forbidden (second %))
                                 (:edges closure))
        forbidden-independent
        #{'sh07-b51-g10-authenticated-resource-census
          'sh07-b51-collect-module-owner-slots
          'sh07-b51-g10-authenticated-root-owner-coordinate
          'sh07-b51-enumerate-slot-pattern
          'sh07-b51-reciprocal-parent-chain
          'sh07-b51-select-owner-fragment
          'sh07-b51-root-macro-traces
          'sh07-b51-expanded-defn-selection
          'sh07-b51-authenticated-form-preorder
          'sh07-b51-loop-owner-skeleton
          'sh07-b51-loop-recur-tail-authenticated?
          'sh07-b51-let-owner-skeleton
          'sh07-b51-owner-coordinate
          'sh07-b51-owner-fragment-by-coordinate
          'sh07-b51-authenticated-direct-child
          'sh07-b51-authenticated-list-operator?
          'sh07-b51-g13-recur-target-selection
          'sh07-b51-g13-select-nearest-compatible-candidates
          'sh07-b51-g13-recur-related-semantic-ids
          'sh07-b51-g13-duplicate-coordinate-priority}
        forbidden-independent-members
        (vec (sort
              (filter forbidden-independent
                      (:members independent-closure))))]
    (is (contains? (:members closure) root8))
    (is (= #{root1} (set (filter (set public-roots) root8-direct)))
        root8-direct)
    (is (= 1 (call-count root8-body root1)) root8-form)
    ;; Root 8 has its own raw-stage/semantic reconstruction closure.  It may
    ;; call Root 1 once for admission, but it must not reuse Root 1's pending
    ;; selector or the G13 producer selector.
    (is (contains? (:members closure)
                   'sh07-b51-r8-reconstruct-pending-semantic)
        (:members closure))
    (is (contains? (:members closure)
                   'sh07-b51-r8-stage-from-raw)
        (:members closure))
    (is (contains? (:members closure)
                   'sh07-b51-r8-select-nearest-compatible-candidates)
        (:members closure))
    (is (empty? forbidden-independent-members)
        [forbidden-independent-members (:members independent-closure)])
    (is (empty? forbidden-edges) forbidden-edges)
    (is (not (contains? (:edges closure) [root8 root8])))
    (is (not (contains? (:edges closure)
                        [root8 'sh07-b51-resolve-digest-preimage])))))

(deftest sh07-b51-v18-g13-recur-selection-uses-greatest-depth-and-tie-boundary
  (let [definitions (definitions-by-name @source-forms)
        selector? (contains? definitions
                             'sh07-b51-g13-recur-target-selection)
        tie-candidates
        [{:target-kind :loop :target-form-id (sha \a)
          :target-id (sha \b) :distance 2}
         {:target-kind :function :target-form-id (sha \c)
          :target-id (sha \d) :distance 2}
         ;; A farther compatible ancestor must not change the tie at depth 2.
         {:target-kind :function :target-form-id (sha \e)
          :target-id (sha \f) :distance 4}]]
    (if-not selector?
      (is true "G13 selector seam is not defined yet")
      (let [g13-tie
            (invoke 'sh07-b51-g13-select-nearest-compatible-candidates
                    [tie-candidates])
            r8-tie
            (invoke 'sh07-b51-r8-select-nearest-compatible-candidates
                    [tie-candidates])]
        (is (= {:status :rejected
                :reason :recur-target-selection-tie}
               g13-tie)
            g13-tie)
        (is (= g13-tie r8-tie) [g13-tie r8-tie])))
    (when selector?
      (let [request (:request
                     (fixture-projection
                      "accepted" "nested-nearest-recur-targets"))
            indexes (fixture-indexes {:request request})
            recur-forms (operator-enclosing-forms request indexes 'recur)
            cases
            (mapv
             (fn [recur-form]
               (let [fragment (g13-recur-fragment request recur-form)
                     oracle (g13-target-selection-oracle
                             request indexes fragment recur-form)
                     selected (first (:selection oracle))
                     actual
                     (invoke 'sh07-b51-g13-recur-target-selection
                             [request indexes fragment recur-form])]
                 {:recur-form recur-form
                  :oracle oracle
                  :selected selected
                  :actual actual}))
             recur-forms)
            farther
            (first (filter #(> (count (get-in % [:oracle :catalog])) 1)
                           cases))]
        (is (map? farther) cases)
        (let [{:keys [oracle selected actual recur-form]} farther
              actual-selected (first (:selection actual))]
          (is (= :accepted (:status actual)) actual)
          (is (= 1 (:nearest-compatible-selection-cardinality actual))
              actual)
          (is (= selected actual-selected) [oracle actual])
          (is (= (:distance selected)
                 (apply min (map :distance (:catalog oracle))))
              [oracle selected])
          (is (> (count (:catalog oracle)) 1) oracle)
          (is (= (:target-id selected)
                 (:syntax-id (:target-form selected)))
              selected)
          (is (not= (:target-id selected) (:target-form-id selected))
              selected)
          (is (not= (:form-id recur-form) (:target-form-id selected))
              [recur-form selected]))))))

(deftest sh07-b51-v18-g13-duplicate-coordinate-priority-is-authenticated-earlier-first
  ;; The duplicate stage retains both authenticated occurrences, but the
  ;; semantic coordinate must be sourced from the earlier occurrence and the
  ;; related ids must remain in [earlier later] order.
  (is (= [[:authenticated-pattern-preorder :selected-duplicate-event :earlier]
          [:authenticated-pattern-preorder :selected-duplicate-event :later]]
         (invoke 'sh07-b51-g13-duplicate-coordinate-priority []))))

(deftest sh07-b51-v18-combined-recur-arity-precedes-non-tail
  (let [projection
        (fixture-projection "rejected" "recur-arity-mismatch-non-tail")
        request (:request projection)
        indexes (fixture-indexes projection)
        recur-form (first (operator-enclosing-forms request indexes 'recur))
        fragment (g13-recur-fragment request recur-form)
        selection
        (invoke 'sh07-b51-g13-recur-target-selection
                [request indexes fragment recur-form])
        selected (first (:selection selection))
        target (:target-form selected)
        tail?
        (invoke 'sh07-b51-loop-recur-tail-authenticated?
                [request indexes fragment recur-form target])
        carrier (raw-carrier
                 "rejected" "recur-arity-mismatch-non-tail" ".gravity")
        root1 (invoke 'sh07-b51-build-template [carrier])
        root8 (invoke 'sh07-b51-finalize-rejection [carrier])
        r8-stage
        (invoke 'sh07-b51-r8-recur-stage-for-form [request recur-form])]
    (is (= :accepted (:status selection)) selection)
    (is (= 1 (:nearest-compatible-selection-cardinality selection))
        selection)
    (is (= 2
           (/ (count
               (:child-form-ids
                (get-in indexes
                        [:forms :by-primary
                         (second (:child-form-ids target))])))
              2))
        target)
    (is (= 1 (- (count (:child-form-ids recur-form)) 1))
        recur-form)
    (is (false? tail?) [recur-form target])
    (is (= (:target-id selected) (:syntax-id target)) selected)
    (is (not= (:target-id selected) (:target-form-id selected)) selected)
    (is (= :recur-arity-mismatch
           (get-in root1
                   [:value :pending-result
                    :diagnostic-semantic-value :reason]))
        root1)
    (is (= :recur-arity-mismatch (:reason r8-stage)) r8-stage)
    (is (= :recur-arity-mismatch
           (get-in root8
                   [:value :envelope :result :diagnostic :reason]))
        root8)
    (is (= [(:target-id selected)]
           (get-in root1
                   [:value :pending-result
                    :diagnostic-semantic-value :related-semantic-ids]))
        root1)
    (is (= [(:target-id selected)]
           (get-in root8
                   [:value :envelope :result :diagnostic
                    :related-semantic-ids]))
        root8)))

(deftest sh07-b51-v18-public-root4-contract-schema-is-closed
  (is (= 51 (count resolved-core-key-order)))
  (is (= 51 (count (set resolved-core-key-order))))
  (is (= product-key-order (subvec resolved-core-key-order 9 50)))
  (is (= :diagnostics (peek resolved-core-key-order)))
  (is (= #{:resolved-core :predecessor-observation :provenance-binding}
         (get-in public-abi-contract
                 ['sh07-b51-resolve-template :success-value-keys])))
  (is (= #{:artifact :schema-version :observation-id :preimage}
         (:observation tier4-keysets)))
  (is (= #{:domain :schema-version :semantic-identity-id
           :raw-adapter-request}
         (:observation-preimage tier4-keysets)))
  (is (= #{:artifact :schema-version :provenance-binding-id :preimage}
         (:provenance tier4-keysets)))
  (is (= #{:domain :schema-version :semantic-identity-id :observation-id}
         (:provenance-preimage tier4-keysets))))

(deftest sh07-b51-v18-private-and-public-root1-build-all-four-authentic-branches
  (let [cases
        [[:legacy-v16-accepted "simple-symbol-parity"
          :legacy-v16-accepted]
         [:vector-frontier "nested-empty-wildcard-let"
          :b51-vector-frontier-rejected]
         [:legacy-after-no-form "ordered-no-form-simple-symbol-waiver"
          :legacy-v16-accepted-after-no-form-waiver]
         [:vector-after-no-form "ordered-no-form-waiver"
          :b51-vector-frontier-rejected-after-no-form-waiver]]
        tier3-through-tier6
        [:sh07-b51-core-identity-id
         :sh07-b51-predecessor-observation-id
         :sh07-b51-provenance-binding-id
         :sh07-b51-independent-verifier-binding-id
         :sh07-b51-final-artifact-binding-id]]
    (is (= 51 (count template-key-order)))
    (is (= 51 (count (set template-key-order))))
    (is (= 19 (count purpose-order)))
    (is (= 58 (reduce + (vals dependency-counts))))
    (doseq [[label basename expected-outcome] cases]
      (let [carrier
            (private-foundation-raw-carrier "accepted" basename)
            private (invoke 'sh07-b51-build-template-private [carrier])
            public (invoke 'sh07-b51-build-template [carrier])]
        (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier]))
            [label :carrier])
        (is (true?
             (invoke 'sh07-b51-fresh-sh06-binding?
                     [(:authenticated-sh06-request carrier)
                      (:authenticated-sh06-verification-report carrier)
                      (:sh06-resolution-artifact carrier)]))
            [label :fresh-sh06])
        (is (= private public) [label private public])
        (is (exact-abi? public :accepted :template-built) [label public])
        (let [template (get-in public [:value :template])
              requests (get-in public [:value :digest-requests])
              products (select-keys template product-key-order)
              plan (invoke 'sh07-b51-success-plan-exact? [requests])
              outcome (invoke 'sh07-b51-success-outcome-kind [requests])
              active-edge-count
              (reduce
               +
               (for [purpose purpose-order]
                 (count
                  (filterv
                   #(invoke 'sh07-b51-dependency-entry-active?
                            [% outcome])
                   (invoke 'sh07-b51-purpose-dependencies [purpose])))))]
          (is (= #{:template :digest-requests}
                 (set (keys (:value public)))) label)
          (is (= (set template-key-order) (set (keys template))) label)
          (is (= 51 (count template)) label)
          (is (= :gravity/sh07-b51-canonical-template-v18
                 (:artifact template)))
          (is (= 18 (:schema-version template)))
          (is (= [] (:diagnostics template)))
          (is (= requests (:digest-requests template)))
          (is (= (vec (range (count requests))) (mapv :ordinal requests)))
          (is (= expected-outcome (:predecessor-outcome-kind template)) label)
          (is (= expected-outcome outcome) label)
          (is (= :accepted (:status plan)) [label plan])
          (is (= 58 (:dependency-edge-count plan)))
          (is (= (if (= expected-outcome :legacy-v16-accepted) 94 174)
                 (:reference-path-census plan))
              [label plan])
          (is (= (if (= expected-outcome :legacy-v16-accepted) 57 56)
                 active-edge-count)
              [label active-edge-count])
          (is (true?
               (invoke 'sh07-b51-template-shape-for-plan?
                       [template requests]))
              label)
          (is (true?
               (invoke 'sh07-b51-tier3-record-request-joins?
                       [requests products expected-outcome]))
              label)
          ;; The producer's second pass must publish only final request
          ;; ordinals.  Replaying every typed edge here excludes provisional
          ;; owner/core/recur sentinels even when their map shape is valid.
          (doseq [request requests]
            (let [registered
                  (invoke 'sh07-b51-request-controlled-references
                          [request expected-outcome])]
              (is (= :accepted (:status registered))
                  [label (:ordinal request) registered])
              (doseq [{:keys [reference target-purpose]}
                      (:references registered)]
                (let [target-ordinal (:ordinal reference)
                      target (get requests target-ordinal)]
                  (is (< target-ordinal (count requests))
                      [label request reference])
                  (is (= target-ordinal (:ordinal target))
                      [label request reference target])
                  (is (= target-purpose (:purpose target))
                      [label request reference target])
                  (is (true?
                       (invoke 'sh07-b51-strict-coordinate-earlier?
                               [target request]))
                      [label request reference target])))))
          (is (= (set (map :purpose requests))
                 (set
                  (filter
                   #(pos? (invoke 'sh07-b51-purpose-count
                                  [requests %]))
                   purpose-order))))
          (doseq [purpose purpose-order]
            (is (= (invoke 'sh07-b51-expected-purpose-count
                           [purpose expected-outcome products])
                   (invoke 'sh07-b51-purpose-count [requests purpose]))
                [label purpose]))
          (doseq [purpose tier3-through-tier6]
            (let [selected (invoke 'sh07-b51-purpose-request
                                   [requests purpose])]
              (is (= 1 (:count selected)) [label purpose selected])
              (is (true?
                   (invoke 'sh07-b51-success-purpose-preimage-shape?
                           [purpose (get-in selected [:request :preimage])]))
                  [label purpose selected])))
          (if (= expected-outcome :legacy-v16-accepted)
            (do
              (is (= 0 (invoke 'sh07-b51-purpose-count
                               [requests :sh07-b51-core-node-id])))
              (is (map? (:legacy-v16-equivalence template)))
              (is (true?
                   (invoke 'sh07-b51-legacy-inherited-products-for-carrier?
                           [carrier products]))))
            (do
              (is (pos? (invoke 'sh07-b51-purpose-count
                                [requests :sh07-b51-core-node-id])))
              (is (nil? (:legacy-v16-equivalence template)))
              (is (seq (:nodes template)))
              (is (every? internal-reference-value?
                          (:root-core-node-ids template)))))
          (is (not (contains? (:value public) :partial-products)))
          (is (not (contains? template :partial-products))))))))

(deftest sh07-b51-v18-root1-pending-is-whitelisted-and-finalized
  (let [pending-cases
        [["duplicate-vector-binding" :duplicate-vector-binding-name]
         ["unsupported-vector-rest" :unsupported-vector-rest]
         ["unsupported-map-pattern" :unsupported-nested-pattern]
         ["missing-recur-target" :missing-recur-target]
         ["recur-arity-mismatch" :recur-arity-mismatch]
         ["recur-arity-mismatch-non-tail" :recur-arity-mismatch]
         ["recur-not-tail" :recur-not-tail]
         ["ordered-no-form-unsupported-vector-rest"
          :unsupported-vector-rest]
         ["module-slot-limit-exceeded" :module-slot-limit-exceeded]
         ["module-binding-leaf-limit-exceeded"
          :module-binding-leaf-limit-exceeded]
         ["module-slot-priority-over-pattern" :module-slot-limit-exceeded]
         ["module-leaf-priority-over-pattern"
          :module-binding-leaf-limit-exceeded]
         ["module-slot-after-leaf-priority" :module-slot-limit-exceeded]]]
    (doseq [[basename expected-reason] pending-cases]
      (let [carrier (raw-carrier "rejected" basename ".gravity")
            private (invoke 'sh07-b51-build-template-private [carrier])
            public (invoke 'sh07-b51-build-template [carrier])
            root8 (invoke 'sh07-b51-finalize-rejection [carrier])
            semantic
            (get-in public
                    [:value :pending-result :diagnostic-semantic-value])]
        (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier])) basename)
        (is (= private public) [basename private public])
        (is (exact-abi? public :pending-rejection :template-rejected)
            [basename public])
        (is (= expected-reason (:reason semantic)) [basename semantic])
        (is (contains? (set (mapcat val root1-pending-whitelist))
                       (:reason semantic))
            [basename semantic])
        (is (true? (invoke 'sh07-b51-diagnostic-semantic-shape? [semantic]))
            [basename semantic])
        (is (exact-abi? root8 :accepted :rejection-finalized)
            [basename root8])))
    (let [carrier
          (private-foundation-raw-carrier
           "rejected" "unauthorized-cross-fragment")
          raw-authentic?
          (invoke 'sh07-b51-raw-carrier-shape? [carrier])
          public (invoke 'sh07-b51-build-template [carrier])]
      (is (true? raw-authentic?) carrier)
      (is (exact-boundary?
           public :template-boundary-rejected
           :sh07-b51-build-template :source-integrity-mismatch)
          public))))

(deftest sh07-b51-v18-private-root1-preserves-two-pass-rejection-reasons
  (let [definitions (definitions-by-name @source-forms)
        root1-private? (contains? definitions 'sh07-b51-build-template-private)
        semantic-cases
        [["duplicate-vector-binding" '[value [value _] tail tail]
          :duplicate-vector-binding-name]
         ["unsupported-vector-rest" '[head & tail]
          :unsupported-vector-rest]
         ["unsupported-map-pattern" '{:value value}
          :unsupported-nested-pattern]]
        pending-for-stage
        (fn [stage request]
          (invoke
           'sh07-b51-entrypoint-envelope
           [:pending-rejection :template-rejected
            (invoke 'sh07-b51-producer-stage-pending-envelope
                    [stage request])]))]
    (if root1-private?
      (do
        (doseq [[basename pattern expected-reason] semantic-cases]
          (let [projection (fixture-projection "rejected" basename)
                request (:request projection)
                indexes (fixture-indexes projection)
                pattern-form (unique-pattern-form request pattern)
                fragment (unique-form-fragment request pattern-form)
                stage
                (invoke 'sh07-b51-enumerate-slot-pattern
                        [request indexes fragment pattern-form])
                carrier
                (raw-carrier "rejected" basename ".gravity")
                root1 (invoke 'sh07-b51-build-template-private [carrier])
                selected (pending-for-stage stage request)]
            (is (= :rejected (:status stage)) [basename stage])
            (is (= expected-reason (:reason stage)) [basename stage])
            (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier]))
                [basename carrier])
            (is (= expected-reason
                   (get-in root1
                           [:value :pending-result
                            :diagnostic-semantic-value :reason]))
                [basename root1])
            ;; The closed stage selector is independently runnable before the
            ;; final-H carrier pin and must preserve the exact semantic cause.
            (is (map? selected) [basename selected])
            (is (exact-abi? selected :pending-rejection :template-rejected)
                [basename selected])
            (is (= expected-reason
                   (get-in selected
                           [:value :pending-result
                            :diagnostic-semantic-value :reason]))
                [basename selected])
            (is (true?
                 (invoke 'sh07-b51-pending-rejected-envelope-shape?
                         [(:value selected)]))
                [basename selected])))
        (let [context
              (loop-recur-context "rejected" "recur-arity-mismatch")
              request (:request context)
              stage (:authenticated-recur context)
              carrier
              (raw-carrier "rejected" "recur-arity-mismatch" ".gravity")
              root1 (invoke 'sh07-b51-build-template-private [carrier])
              selected (pending-for-stage stage request)]
          (is (= :rejected (:status stage)) stage)
          (is (= :recur-arity-mismatch (:reason stage)) stage)
          (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier])) carrier)
          (is (= :recur-arity-mismatch
                 (get-in root1
                         [:value :pending-result
                          :diagnostic-semantic-value :reason]))
              root1)
          (is (map? selected) selected)
          (is (exact-abi? selected :pending-rejection :template-rejected)
              selected)
          (is (= :recur-arity-mismatch
                 (get-in selected
                         [:value :pending-result
                          :diagnostic-semantic-value :reason]))
              selected)
          (is (true?
               (invoke 'sh07-b51-pending-rejected-envelope-shape?
                       [(:value selected)]))
              selected))
        (let [let-context
              (authenticated-preflight-slot "nested-empty-wildcard-let" 'let)
              loop-context
              (authenticated-preflight-slot
               "nested-empty-wildcard-loop-recur" 'loop)
              request (:request let-context)
              let-slot (:preflight-slot let-context)
              loop-slot (:preflight-slot loop-context)
              slot-stage
              (invoke 'sh07-b51-module-resource-preflight
                      [(into (vec (repeat 512 let-slot))
                             (repeat 513 loop-slot))])
              binding-descriptor
              (first
               (filter #(= :binding-leaf (:kind %))
                       (get-in let-slot [:pattern :descriptors])))
              augmented-pattern
              (-> (:pattern let-slot)
                  (update :descriptors conj
                          (assoc binding-descriptor
                                 :pattern-node-ordinal
                                 (get-in let-slot [:pattern :node-count])
                                 :terminal-ordinal
                                 (get-in let-slot [:pattern :terminal-count])
                                 :leaf-ordinal
                                 (get-in let-slot [:pattern :leaf-count])
                                 :path [999] :binding-id (sha \e)))
                  (update :introduced-binding-ids conj (sha \e))
                  (update :node-count inc)
                  (update :extraction-count inc)
                  (update :terminal-count inc)
                  (update :leaf-count inc))
              leaf-slots
              (into
               (vec (cons (assoc let-slot :pattern augmented-pattern)
                          (repeat 511 let-slot)))
               (repeat 512 loop-slot))
              leaf-stage
              (invoke 'sh07-b51-module-resource-preflight [leaf-slots])
              resource-stages
              [["module-slot-limit-exceeded" slot-stage]
               ["module-binding-leaf-limit-exceeded" leaf-stage]]]
          (doseq [[basename stage] resource-stages]
            (let [expected-reason (keyword basename)
                  carrier (raw-carrier "rejected" basename ".gravity")
                  selected (pending-for-stage stage request)]
              (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier]))
                  [basename carrier])
              (is (= :rejected (:status stage)) [basename stage])
              (is (= expected-reason (:reason stage)) [basename stage])
              (is (= [] (:partial-products stage)) [basename stage])
              (is (exact-abi? selected :pending-rejection
                              :template-rejected)
                  [basename selected])
              (is (= expected-reason
                     (get-in selected
                             [:value :pending-result
                              :diagnostic-semantic-value :reason]))
                  [basename selected])
              (let [root1
                    (invoke 'sh07-b51-build-template-private [carrier])]
                (is (exact-abi? root1 :pending-rejection :template-rejected)
                    [basename root1])
                (is (= expected-reason
                       (get-in root1
                               [:value :pending-result
                                :diagnostic-semantic-value :reason]))
                    [basename root1]))))))
      (is true "private Root1 two-pass rejection guard is dormant"))))

(deftest sh07-b51-v18-root1-eight-pending-coordinates-and-related-ids-are-exact
  (let [cases
        [["duplicate-vector-binding" :duplicate-vector-binding-name 2
          :duplicate-binding :unique-vector-leaf-name 'value]
         ["unsupported-vector-rest" :unsupported-vector-rest 0
          :lowering-gap :legacy-symbol-or-fixed-vector-pattern :rest-marker]
         ["unsupported-map-pattern" :unsupported-nested-pattern 0
          :lowering-gap :legacy-symbol-or-fixed-vector-pattern
          :unsupported-form]
         ["missing-recur-target" :missing-recur-target 1
          :recur-target :nearest-compatible-function-or-loop-target :missing]
         ["recur-arity-mismatch" :recur-arity-mismatch 1
          :recur-target 2 1]
         ;; The same authenticated recur is both wrong-arity and non-tail;
         ;; G13 fixes arity as the earlier diagnostic priority.
         ["recur-arity-mismatch-non-tail" :recur-arity-mismatch 1
          :recur-target 2 1]
         ["recur-not-tail" :recur-not-tail 1
          :recur-target :nearest-compatible-function-or-loop-target :non-tail]
         ["module-slot-limit-exceeded" :module-slot-limit-exceeded 1
          :verify {:unit :module-slots :maximum 1024}
          {:unit :module-slots :saturating-count 1025}]
         ["module-binding-leaf-limit-exceeded"
          :module-binding-leaf-limit-exceeded 1
          :verify {:unit :module-binding-leaves :maximum 2048}
          {:unit :module-binding-leaves :saturating-count 2049}]
         ["module-slot-priority-over-pattern" :module-slot-limit-exceeded 1
          :verify {:unit :module-slots :maximum 1024}
          {:unit :module-slots :saturating-count 1025}]
         ["module-leaf-priority-over-pattern"
          :module-binding-leaf-limit-exceeded 1
          :verify {:unit :module-binding-leaves :maximum 2048}
          {:unit :module-binding-leaves :saturating-count 2049}]
         ;; The raw census sees 2049 leaves in the first three slots before
         ;; the later 1025th slot.  G13's module-slot priority therefore wins
         ;; even though the leaf bound was crossed earlier in source order.
         ["module-slot-after-leaf-priority" :module-slot-limit-exceeded 1
          :verify {:unit :module-slots :maximum 1024}
          {:unit :module-slots :saturating-count 1025}]]
        recur-reasons
        #{:missing-recur-target :recur-arity-mismatch :recur-not-tail}
        resource-reasons
        #{:module-slot-limit-exceeded
          :module-binding-leaf-limit-exceeded}
        kind-rules
        {:lowering-gap "C6-LOWERING-GAP"
         :duplicate-binding "L7-DUP-BINDING"
         :recur-target "L2-RECUR-TARGET" :verify "C6-VERIFY"}
        remediation
        {:lowering-gap
         {:action :fix-source :owner :source-author
          :required-evidence [:offending-form :supported-grammar]}
         :duplicate-binding
         {:action :fix-source :owner :source-author
          :required-evidence [:binding-paths]}
         :recur-target
         {:action :fix-source :owner :source-author
          :required-evidence [:recur-coordinate :target-slots]}}
        resource-remediation
        {:action :fix-source :owner :source-author
         :required-evidence [:resource-bound :first-offending-coordinate]}]
    (is (= (set (mapcat val root1-pending-whitelist))
           (set (map second cases))))
    (doseq [[basename reason related-count kind expected observed] cases]
      (let [carrier (raw-carrier "rejected" basename ".gravity")
            request (:authenticated-sh06-request carrier)
            result (invoke 'sh07-b51-build-template [carrier])
            root8 (invoke 'sh07-b51-finalize-rejection [carrier])]
        (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier])) basename)
        (is (exact-abi? result :pending-rejection :template-rejected)
            [basename result])
        (let [semantic
                    (get-in result
                            [:value :pending-result
                             :diagnostic-semantic-value])
                    duplicate-stage
                    (when (= reason :duplicate-vector-binding-name)
                      (let [indexes (fixture-indexes {:request request})
                            pattern
                            (unique-pattern-form
                             request '[value [value _] tail tail])
                            fragment (unique-form-fragment request pattern)]
                        (invoke 'sh07-b51-enumerate-slot-pattern
                                [request indexes fragment pattern])))
                    duplicate-earlier
                    (get duplicate-stage :first-duplicate)
                    duplicate-later
                    (get duplicate-stage :later-duplicate)
                    form-id (:form-id semantic)
                    indexed
                    (keep-indexed
                     (fn [ordinal form]
                       (when (= form-id (:form-id form)) [ordinal form]))
                     (:forms request))
                    [form-ordinal form] (first indexed)
                    expected-related
                    (cond
                      (= reason :duplicate-vector-binding-name)
                      [(:form-id duplicate-earlier)
                       (:form-id duplicate-later)]

                      (= reason :missing-recur-target)
                      (let [indexes (fixture-indexes {:request request})
                            recur-form
                            (first (operator-enclosing-forms
                                    request indexes 'recur'))
                            owner (g13-recur-owner request indexes recur-form)]
                        [(:root-form-id owner)])

                      (contains? #{:recur-arity-mismatch :recur-not-tail}
                                 reason)
                      (let [indexes (fixture-indexes {:request request})
                            recur-form
                            (first (operator-enclosing-forms
                                    request indexes 'recur))
                            fragment
                            (unique-form-fragment request recur-form)
                            selection
                            (g13-target-selection-oracle
                             request indexes fragment recur-form)
                            target (first (:selection selection))]
                        ;; Related ids are authenticated target-record ids
                        ;; (syntax ids), never source form ids.
                        [(:target-id target)])

                      (contains? resource-reasons reason)
                      [(get-in semantic
                               [:owner-coordinate :root-form-id])]

                      :else [])
                    coordinate-keys
                    [:form-id :form-ordinal :fragment-ordinal :syntax-id
                     :source-span :generated-origin-chain :core-node-id
                     :owner-coordinate :slot-id :extraction-id
                     :recur-form-id :recur-syntax-id :path]
                    expected-semantic
                    (merge
                     {:artifact :gravity/sh07-b51-c6-diagnostic-v18
                      :schema-version 18 :status :rejected
                      :diagnostic-kind kind :rule (kind-rules kind)
                      :severity :error :stage :core-lowering
                      :reason reason :expected expected :observed observed
                      :related-semantic-ids expected-related
                      :lowering-rule :sh07-b51-vector-destructuring-v18
                      :profile :meta :target :jvm
                      :remediation
                      (if (contains? resource-reasons reason)
                        resource-remediation (remediation kind))
                      :fail-closed true}
                     (select-keys semantic coordinate-keys))]
                (is (= reason (:reason semantic)) [basename semantic])
                (is (= related-count
                       (count (:related-semantic-ids semantic)))
                    [basename semantic])
                (is (every? #(invoke 'sh07-b51-sha256-id? [%])
                            (:related-semantic-ids semantic))
                    [basename semantic])
                (is (= expected-related (:related-semantic-ids semantic))
                    [basename semantic])
                (is (= expected-semantic semantic) [basename semantic])
                (is (= 29 (count semantic)) [basename semantic])
                (is (= (bootstrap/reader-canonical-hash expected-semantic)
                       (bootstrap/reader-canonical-hash semantic))
                    [basename semantic])
                (is (= 1 (count indexed)) [basename semantic])
                (is (= form-ordinal (:form-ordinal semantic))
                    [basename semantic])
                (is (= (:syntax-id form) (:syntax-id semantic))
                    [basename semantic])
                (is (= (or (:semantic-span form) (:source-span form))
                       (:source-span semantic))
                    [basename semantic])
                (is (= (:generated-origin form)
                       (:generated-origin-chain semantic))
                    [basename semantic])
                (is (some? (:fragment-ordinal semantic))
                    [basename semantic])
                (is (map? (:owner-coordinate semantic))
                    [basename semantic])
                (cond
                  (= reason :duplicate-vector-binding-name)
                  (do
                    (is (map? duplicate-earlier) [basename duplicate-stage])
                    (is (map? duplicate-later) [basename duplicate-stage])
                    (is (= (:form-id duplicate-earlier) (:form-id semantic))
                        [basename semantic])
                    (is (= (:syntax-id duplicate-earlier)
                           (:syntax-id semantic))
                        [basename semantic])
                    (is (= (:source-span duplicate-earlier)
                           (:source-span semantic))
                        [basename semantic])
                    (is (= (:generated-origin duplicate-earlier)
                           (:generated-origin-chain semantic))
                        [basename semantic])
                    (is (= (:path duplicate-earlier) (:path semantic))
                        [basename semantic])
                    (is (= [(:form-id duplicate-earlier)
                            (:form-id duplicate-later)]
                           (:related-semantic-ids semantic))
                        [basename semantic])
                    (is (not (contains? (:value result) :template))
                        [basename result])
                    (is (not (contains? (:value result) :digest-requests))
                        [basename result])
                    (is (not (contains? (:value result) :products))
                        [basename result])
                    (is (not (contains? (:value result) :hashes))
                        [basename result])
                    (is (not (contains? (:value result) :partial-products))
                        [basename result])
                    ;; Duplicate diagnostics stop before any B51 product
                    ;; allocation.  Their coordinate is the authenticated
                    ;; earlier occurrence, with generated ids absent.
                    (is (nil? (:core-node-id semantic)) basename)
                    (is (nil? (:slot-id semantic)) basename)
                    (is (nil? (:extraction-id semantic)) basename)
                    (is (nil? (:recur-form-id semantic)) basename)
                    (is (nil? (:recur-syntax-id semantic)) basename)
                    (is (vector? (:path semantic)) basename))

                  (contains? recur-reasons reason)
                  (do
                    (is (= form-id (:recur-form-id semantic)) basename)
                    (is (= (:syntax-id semantic)
                           (:recur-syntax-id semantic)) basename)
                    (when (= reason :missing-recur-target)
                      (let [indexes (fixture-indexes {:request request})
                            recur-form
                            (first (operator-enclosing-forms
                                    request indexes 'recur))
                            owner (g13-recur-owner request indexes recur-form)]
                        (is (= (:root-form-id owner)
                               (first (:related-semantic-ids semantic)))
                            [basename semantic owner])
                        (is (not= (:projection-binding request)
                                  (first (:related-semantic-ids semantic)))
                            [basename semantic owner])))
                    (when (contains? #{:recur-arity-mismatch :recur-not-tail}
                                     reason)
                      (let [indexes (fixture-indexes {:request request})
                            recur-form
                            (first (operator-enclosing-forms
                                    request indexes 'recur))
                            fragment
                            (unique-form-fragment request recur-form)
                            selection
                            (g13-target-selection-oracle
                             request indexes fragment recur-form)
                            target (first (:selection selection))]
                        (is (= (:target-id target)
                               (first (:related-semantic-ids semantic)))
                            [basename semantic target])
                        (is (= (:target-id target)
                               (:syntax-id (:target-form target)))
                            [basename target])
                        (is (not= (:target-id target)
                                  (:target-form-id target))
                            [basename target])))
                    (is (nil? (:core-node-id semantic)) basename)
                    (is (nil? (:slot-id semantic)) basename)
                    (is (nil? (:extraction-id semantic)) basename)
                    (is (nil? (:path semantic)) basename))

                  (contains? resource-reasons reason)
                  (do
                    (is (= [(get-in semantic
                                    [:owner-coordinate :root-form-id])]
                           (:related-semantic-ids semantic)) basename)
                    (is (nil? (:core-node-id semantic)) basename)
                    (is (nil? (:slot-id semantic)) basename)
                    (is (nil? (:extraction-id semantic)) basename)
                    (is (vector? (:path semantic)) basename))

                  :else
                  (do
                    (is (nil? (:core-node-id semantic)) basename)
                    (is (nil? (:slot-id semantic)) basename)
                    (is (nil? (:extraction-id semantic)) basename)
                    (is (nil? (:path semantic)) basename)))
                (is (exact-abi? root8 :accepted :rejection-finalized)
                    [basename root8])
                (is (= semantic
                       (dissoc
                        (get-in root8 [:value :envelope :result :diagnostic])
                        :diagnostic-id))
                    [basename root8])
                (is (= (bootstrap/reader-canonical-hash expected-semantic)
                       (bootstrap/reader-canonical-hash
                        (dissoc
                         (get-in root8
                                 [:value :envelope :result :diagnostic])
                         :diagnostic-id)))
                    [basename root8])
                (is (= (get-in root8 [:value :digest-id])
                       (get-in root8
                               [:value :envelope :result :diagnostic
                                :diagnostic-id]))
                    [basename root8]))))))

(deftest sh07-b51-v18-catalog-only-reasons-are-injection-only-and-never-finalized
  (let [pattern-projection
        (fixture-projection "rejected" "unsupported-map-pattern")
        pattern-request (:request pattern-projection)
        pattern-indexes (fixture-indexes pattern-projection)
        pattern-form
        (unique-pattern-form pattern-request '{:value value})
        pattern-fragment (unique-form-fragment pattern-request pattern-form)
        pattern-enclosing
        (first (operator-enclosing-forms
                pattern-request pattern-indexes 'let))
        pattern-owner
        (invoke 'sh07-b51-owner-coordinate
                [pattern-request pattern-indexes pattern-enclosing])
        recur-context
        (loop-recur-context "rejected" "recur-arity-mismatch")
        recur-request (:request recur-context)
        recur-form (:recur-form recur-context)
        recur-fragment (unique-form-fragment recur-request recur-form)
        coordinate-for
        (fn [request form fragment owner recur?]
          {:form-id (:form-id form)
           :form-ordinal
           (first (keep-indexed
                   (fn [ordinal candidate]
                     (when (= (:form-id form) (:form-id candidate)) ordinal))
                   (:forms request)))
           :fragment-ordinal (:ordinal fragment)
           :syntax-id (:syntax-id form)
           :source-span (or (:semantic-span form) (:source-span form))
           :generated-origin-chain (:generated-origin form)
           :core-node-id nil
           :owner-coordinate owner
           :slot-id nil :extraction-id nil
           :recur-form-id (when recur? (:form-id form))
           :recur-syntax-id (when recur? (:syntax-id form))
           :path nil})
        pattern-coordinate
        (coordinate-for pattern-request pattern-form pattern-fragment
                        (:coordinate pattern-owner) false)
        recur-coordinate
        (coordinate-for recur-request recur-form recur-fragment
                        (get-in recur-context
                                [:foundation :owner-coordinate]) true)
        rows
        [[:pattern-type :malformed-authenticated-pattern-shape
          pattern-coordinate
          :authenticated-fixed-vector-pattern :malformed-pattern []]
         [:recur-target :ambiguous-recur-target
          recur-coordinate
          :nearest-compatible-function-or-loop-target :multiple [(sha \a)]]]]
    (is (= root1-catalog-only-unreachable
           (set (map (fn [[kind reason & _]] [kind reason]) rows))))
    (doseq [[kind reason coordinate expected observed related] rows]
      (is (false? (invoke 'sh07-b51-root1-pending-reason? [reason])) reason)
      (let [injected
            (invoke 'sh07-b51-pending-rejection
                    [kind reason coordinate expected observed related])
            stage-result
            (invoke 'sh07-b51-root1-stage-result
                    [{:status :rejected :reason reason :partial-products []}
                     {} :source-integrity-mismatch])]
        (is (true? (invoke 'sh07-b51-pending-rejected-envelope-shape?
                           [injected]))
            [reason injected])
        (is (exact-boundary?
             stage-result :template-boundary-rejected
             :sh07-b51-build-template :source-integrity-mismatch)
            [reason stage-result])))))

(deftest sh07-b51-v18-root1-competing-failure-priority-is-literal
  (let [priority
        [:malformed-invocation :source-integrity-mismatch
         :template-construction-mismatch :success-plan-mismatch]
        carrier
        (private-foundation-raw-carrier "accepted" "simple-symbol-parity")
        malformed-and-corrupt
        (-> carrier
            (dissoc :authenticated-sh06-verification-report)
            (assoc-in [:authenticated-sh06-request :module :profile] :core))
        source-pair
        (-> carrier
            (assoc-in [:authenticated-sh06-request :module :profile] :core)
            (assoc-in [:predecessor-raw-outcome
                       :direct-template-observation :request] {}))]
    (is (= priority
           (get-in detector-boundary-contract
                   ['sh07-b51-build-template :reasons])))
    (is (exact-boundary?
         (invoke 'sh07-b51-build-template [malformed-and-corrupt])
         :template-boundary-rejected :sh07-b51-build-template
         :malformed-invocation))
    (is (exact-boundary?
         (invoke 'sh07-b51-build-template [source-pair])
         :template-boundary-rejected :sh07-b51-build-template
         :source-integrity-mismatch))
    (doseq [available
            [#{:success-plan-mismatch :template-construction-mismatch}
             #{:success-plan-mismatch :template-construction-mismatch
               :source-integrity-mismatch}
             (set priority)]]
      (let [selected (first (filter available priority))
            boundary (invoke 'sh07-b51-root1-boundary-envelope [selected])]
        (is (= selected (first (filter available priority))) available)
        (is (exact-boundary?
             boundary :template-boundary-rejected
             :sh07-b51-build-template selected)
            [available boundary])))))

(deftest sh07-b51-v18-catalog-only-literals-have-zero-root1-emission-paths
  (let [definitions (definitions-by-name @source-forms)
        reasons (set (map second root1-catalog-only-unreachable))
        contains-value?
        (fn contains-value? [value target]
          (cond
            (= value target) true
            (map? value) (or (some #(contains-value? % target) (keys value))
                             (some #(contains-value? % target) (vals value)))
            (coll? value) (boolean (some #(contains-value? % target) value))
            :else false))
        literal-owners
        (fn [reason]
          (set (for [[name form] definitions
                     :when (contains-value? form reason)]
                 name)))
        direct-callees
        (fn direct-callees [value]
          (cond
            (and (seq? value) (= 'quote (first value))) #{}
            (seq? value)
            (into
             (if (and (symbol? (first value))
                      (contains? definitions (first value)))
               #{(first value)} #{})
             (mapcat direct-callees (rest value)))
            (coll? value) (into #{} (mapcat direct-callees value))
            :else #{}))
        root1-closure
        (loop [pending ['sh07-b51-build-template] members #{}]
          (if (empty? pending)
            members
            (let [name (peek pending) pending (pop pending)]
              (if (contains? members name)
                (recur pending members)
                (recur
                 (into pending
                       (direct-callees (nthnext (get definitions name) 2)))
                 (conj members name))))))
        catalog-owners
        #{'sh07-b51-diagnostic-kind-reason?
          'sh07-b51-diagnostic-catalog-values-shape?}]
    (doseq [reason reasons]
      (is (= catalog-owners (literal-owners reason)) reason)
      (is (empty? (set/intersection
                   root1-closure (literal-owners reason)))
          [reason root1-closure]))
    (doseq [family ["accepted" "rejected"]
            basename (sort (fixture-catalog family))]
      (let [carrier (raw-carrier family basename ".gravity")
            root1 (invoke 'sh07-b51-build-template [carrier])
            root8 (invoke 'sh07-b51-finalize-rejection [carrier])
            root1-reason
            (get-in root1
                    [:value :pending-result :diagnostic-semantic-value
                     :reason])
            finalized-reason
            (get-in root8
                    [:value :envelope :result :diagnostic :reason])]
        (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier]))
            [family basename])
        (is (not (contains? reasons root1-reason))
            [family basename root1])
        (is (not (contains? reasons finalized-reason))
            [family basename root8])
        (when (exact-abi? root8 :accepted :rejection-finalized)
          (is (= root1-reason finalized-reason)
              [family basename root1 root8]))))))

(deftest sh07-b51-v18-incomplete-whitelisted-stages-never-create-pending
  (let [request
        (:request (fixture-projection "rejected" "unsupported-vector-rest"))
        reasons (vec (mapcat val root1-pending-whitelist))]
    (doseq [reason reasons
            stage [{:status :rejected :reason reason}
                   {:status :rejected :reason reason :partial-products nil}
                   {:status :accepted :reason reason :partial-products []}
                   {:reason reason :partial-products []}
                   {:status :rejected :reason reason :partial-products []
                    :forged true}]]
      (let [result
            (invoke 'sh07-b51-root1-stage-result
                    [stage request :source-integrity-mismatch])]
        (is (exact-boundary?
             result :template-boundary-rejected
             :sh07-b51-build-template :source-integrity-mismatch)
            [reason stage result])))))

(deftest sh07-b51-v18-root1-carrier-outcome-cardinality-and-ordinal-mutations-fail-closed
  (let [carrier
        (private-foundation-raw-carrier "accepted" "simple-symbol-parity")
        observation-path [:predecessor-raw-outcome]
        direct-path
        [:predecessor-raw-outcome :direct-template-observation :returned]
        structural-path
        [:predecessor-raw-outcome :structural-runner-observation :returned]
        built (invoke 'sh07-b51-build-template-private [carrier])
        template (get-in built [:value :template])
        requests (get-in built [:value :digest-requests])
        carrier-mutations
        [[:missing-required-key :malformed-invocation
          (dissoc carrier :authenticated-sh06-verification-report)]
         [:verification-status :source-integrity-mismatch
          (assoc-in carrier
                   [:authenticated-sh06-verification-report :status]
                   :failed)]
         [:proof-shape :source-integrity-mismatch
          (assoc-in carrier
                   [:sh06-resolution-artifact :capability-based-proof]
                   {})]
         [:request-profile :source-integrity-mismatch
          (assoc-in carrier
                   [:authenticated-sh06-request :module :profile]
                   :core)]
         [:direct-status :source-integrity-mismatch
          (assoc-in carrier
                   (into direct-path [:status])
                   :rejected)]
         [:structural-artifact :source-integrity-mismatch
          (assoc-in carrier
                   (into structural-path
                         [:canonical-core-artifact :artifact-id])
                   (sha \f))]
         [:observation-request :source-integrity-mismatch
          (assoc-in carrier
                   (into observation-path
                         [:direct-template-observation :request])
                   {})]]
        plan-mutations
        [[:missing-request (vec (butlast requests))]
         [:duplicate-request (conj requests (last requests))]
         [:reordered-request (vec (reverse requests))]
         [:wrong-ordinal (assoc-in requests [0 :ordinal] 1)]
         [:wrong-batch-ordinal (assoc-in requests [0 :batch-ordinal] 1)]
         [:wrong-purpose
          (assoc-in requests [0 :purpose]
                    :sh07-b51-contract-semantic-id)]]]
    (is (exact-abi? built :accepted :template-built) built)
    (doseq [[label reason mutated] carrier-mutations]
      (let [private (invoke 'sh07-b51-build-template-private [mutated])
            public (invoke 'sh07-b51-build-template [mutated])]
        (is (= private public) [label private public])
        (is (exact-boundary?
             public :template-boundary-rejected
             :sh07-b51-build-template reason)
            [label public])
        (is (not (contains? (:value public) :partial-products)) [label])))
    (doseq [[label mutated] plan-mutations]
      (is (not= requests mutated) label)
      (is (false? (invoke 'sh07-b51-template-shape-for-plan?
                          [(assoc template :digest-requests mutated)
                           mutated]))
          label)
      (is (not= :accepted
                (:status (invoke 'sh07-b51-success-plan-exact? [mutated])))
          label))))

(deftest sh07-b51-v18-root1-two-pass-owner-and-recur-references-are-final
  (let [carrier
        (private-foundation-raw-carrier
         "accepted" "nested-empty-wildcard-loop-recur")
        built (invoke 'sh07-b51-build-template-private [carrier])
        template (get-in built [:value :template])
        requests (get-in built [:value :digest-requests])
        first-index
        (fn [purpose]
          (first
           (keep-indexed
            (fn [ordinal request]
              (when (= purpose (:purpose request)) ordinal))
            requests)))
        core-index (first-index :sh07-b51-core-node-id)
        mapping-index (first-index :sh07-b51-recur-slot-mapping-id)
        provisional (internal-reference (+ 100 (count requests)))
        mutations
        [[:core-owner-wrong-purpose
          (assoc-in requests
                    [core-index :preimage :predecessor-authority-id]
                    (internal-reference 4))]
         [:core-owner-provisional
          (assoc-in requests
                    [core-index :preimage :predecessor-authority-id]
                    provisional)]
         [:recur-owner-wrong-purpose
          (assoc-in requests
                    [mapping-index :preimage :predecessor-authority-id]
                    (internal-reference 4))]
         [:recur-owner-provisional
          (assoc-in requests
                    [mapping-index :preimage :predecessor-authority-id]
                    provisional)]
         [:recur-slot-wrong-purpose
          (assoc-in requests
                    [mapping-index :preimage :slot-ids 0]
                    (internal-reference 5))]
         [:recur-core-provisional
          (assoc-in requests
                    [mapping-index :preimage :target-loop-core-node-id]
                    provisional)]
         [:recur-publication-wrong-purpose
          (assoc-in requests
                    [mapping-index :preimage :commit-publication-id]
                    (internal-reference 5))]]]
    (is (exact-abi? built :accepted :template-built) built)
    (is (integer? core-index))
    (is (integer? mapping-index))
    (is (true? (invoke 'sh07-b51-template-shape-for-plan?
                       [template requests])))
    (doseq [[label mutated] mutations]
      (is (not= requests mutated) label)
      (is (not= :accepted
                (:status (invoke 'sh07-b51-success-plan-exact? [mutated])))
          label)
      (is (false?
           (invoke 'sh07-b51-template-shape-for-plan?
                   [(assoc template :digest-requests mutated) mutated]))
          label))))

(deftest sh07-b51-v18-provisional-allocator-is-injective-at-old-collision-points
  (let [empty-counters
        {:slot 0 :extraction 0 :check 0 :publication 0 :mapping 0}
        minimal-slot
        {:pattern {:extraction-count 1 :vector-node-count 1
                   :terminal-count 1}}
        layouts
        [[:initial-owner-0
          (:layout
           (invoke 'sh07-b51-producer-initialization-layout
                   [[minimal-slot] 0 empty-counters]))]
         [:initial-owner-1023
          (:layout
           (invoke 'sh07-b51-producer-initialization-layout
                   [[minimal-slot] 1023 empty-counters]))]
         [:recur-owner-0-site-0
          (:layout
           (invoke 'sh07-b51-producer-recur-layout
                   [[] 0 0 empty-counters]))]
         ;; Under the old linear formula these two bases were equal:
         ;; owner 0/recur 10000 == owner 1/recur 0.
         [:recur-owner-0-site-10000
          (:layout
           (invoke 'sh07-b51-producer-recur-layout
                   [[] 0 10000 empty-counters]))]
         [:recur-owner-1-site-0
          (:layout
           (invoke 'sh07-b51-producer-recur-layout
                   [[] 1 0 empty-counters]))]
         [:recur-owner-1023-site-10000
          (:layout
           (invoke 'sh07-b51-producer-recur-layout
                   [[] 1023 10000 empty-counters]))]]
        generated
        (vec
         (mapcat
          (fn [[label layout]]
                 (map #(vector label %)
                      (remove #{2 5}
                         (invoke
                          'sh07-b51-producer-allocation-reference-ordinals
                          [layout]))))
          layouts))
        ordinals (mapv second generated)]
    (is (pos? (count ordinals)))
    (is (= (count ordinals) (count (set ordinals))) generated)
    (is (not=
         (get-in (into {} layouts)
                 [:recur-owner-0-site-10000 :recur-core-node-id])
         (get-in (into {} layouts)
                 [:recur-owner-1-site-0 :recur-core-node-id])))
    (is (true? (invoke 'sh07-b51-producer-allocation-regions-valid? [])))
    (is (true?
         (invoke 'sh07-b51-producer-planner-references-unique?
                 [(mapv second layouts)])))))

(deftest sh07-b51-v18-provisional-map-is-total-and-legacy-bypasses-final-owner-rewrite
  (let [nonlegacy-carrier
        (private-foundation-raw-carrier
         "accepted" "nested-empty-wildcard-loop-recur")
        request (:authenticated-sh06-request nonlegacy-carrier)
        provisional (invoke 'sh07-b51-producer-provisional-numbered [request])
        final (invoke 'sh07-b51-producer-final-owner-plans
                      [provisional false])
        provisional-ordinals
        (conj
         (set
          (invoke 'sh07-b51-producer-planner-reference-ordinals
                  [(:owner-plans provisional)]))
         2 5)
        mapping (:reference-mapping final)
        final-ordinals
        (set
         (invoke 'sh07-b51-producer-planner-reference-ordinals
                 [(:owner-plans final)]))
        legacy-carrier
        (private-foundation-raw-carrier "accepted" "simple-symbol-parity")
        legacy-request (:authenticated-sh06-request legacy-carrier)
        legacy-provisional
        (invoke 'sh07-b51-producer-provisional-numbered [legacy-request])
        forbidden-legacy-final
        (invoke 'sh07-b51-producer-final-owner-plans
                [legacy-provisional true])
        root1-defined?
        (contains? (definitions-by-name @source-forms)
                   'sh07-b51-build-template-private)]
    (is (= :accepted (:status provisional)) provisional)
    (is (= :accepted (:status final)) final)
    (is (= (count provisional-ordinals) (count mapping))
        {:provisional provisional-ordinals :mapping mapping})
    (is (= provisional-ordinals (set (keys mapping))))
    (is (every? integer? (vals mapping)))
    (is (empty?
         (set/intersection
          final-ordinals
          (set/difference provisional-ordinals #{2 5})))
        {:final final-ordinals :provisional provisional-ordinals})
    (is (= :rejected (:status forbidden-legacy-final)))
    (is (= :producer-legacy-concrete-core-path-required
           (:reason forbidden-legacy-final)))
    (is (= [] (:partial-products forbidden-legacy-final)))
    ;; Since the legacy-only final-owner seam rejects unconditionally, this
    ;; accepted result proves the producer bypasses it instead of calling it
    ;; with an empty core mapping.
    (if root1-defined?
      (let [legacy-built
            (invoke 'sh07-b51-build-template-private [legacy-carrier])]
        (is (exact-abi? legacy-built :accepted :template-built) legacy-built)
        (is (= :legacy-v16-accepted
               (get-in legacy-built
                       [:value :template :predecessor-outcome-kind]))))
      (is true "legacy producer bypass proof awaits private root 1"))))

(deftest sh07-b51-v18-four-outcome-constructors-are-closed-and-carrier-authenticated
  (let [cases
        [["simple-symbol-parity" :legacy-v16-accepted]
         ["nested-empty-wildcard-let" :b51-vector-frontier-rejected]
         ["ordered-no-form-simple-symbol-waiver"
          :legacy-v16-accepted-after-no-form-waiver]
         ["ordered-no-form-waiver"
          :b51-vector-frontier-rejected-after-no-form-waiver]]]
    (doseq [[basename expected-kind] cases]
      (let [carrier (private-foundation-raw-carrier "accepted" basename)
            request (:authenticated-sh06-request carrier)
            derived (invoke 'sh07-b51-producer-outcome-kind [carrier])
            provisional
            (invoke 'sh07-b51-producer-provisional-numbered [request])
            counts
            (invoke 'sh07-b51-producer-purpose-counts-from-numbered
                    [provisional])
            legacy? (= expected-kind :legacy-v16-accepted)
            bases (invoke 'sh07-b51-producer-purpose-bases
                          [legacy? counts])
            outcome (invoke 'sh07-b51-producer-outcome-preimage
                            [carrier derived bases])
            mutations
            [(dissoc outcome :schema-version)
             (assoc outcome :extra :forbidden)
             (assoc outcome :outcome-kind :forged-outcome)
             (assoc outcome :status
                    (if (= :accepted (:status outcome))
                      :rejected :accepted))
             (assoc outcome :same-request-semantic-id (sha \f))
             (assoc outcome :implementation-semantic-id
                    (internal-reference (:contract bases)))
             (assoc outcome :authorization :none)]]
        (if (and (= expected-kind :legacy-v16-accepted)
                 (= :rejected (:status derived)))
          (do
            ;; The authentic structural return is intentionally unchanged;
            ;; until final-H pins admit it, the legacy constructor must stop
            ;; rather than fabricate the missing canonical artifact fields.
            (is (= :producer-legacy-structural-artifact-missing
                   (:reason derived))
                [basename derived])
            (is (= :rejected (:status outcome)) [basename outcome])
            (is (= :producer-legacy-structural-artifact-missing
                   (:reason outcome))
                [basename outcome]))
          (do
            (is (= :accepted (:status derived)) [basename derived])
            (is (= expected-kind (:outcome-kind derived)) [basename derived])
            (is (= expected-kind (:outcome-kind outcome)) [basename outcome])
            (is (true?
                 (invoke 'sh07-b51-success-outcome-preimage-shape?
                         [outcome]))
                [basename outcome])
            (is (true?
                 (invoke 'sh07-b51-outcome-preimage-for-carrier?
                         [carrier outcome]))
                [basename outcome])
            (doseq [mutated mutations]
              (is (or
                   (false?
                    (invoke 'sh07-b51-success-outcome-preimage-shape?
                            [mutated]))
                   (false?
                    (invoke 'sh07-b51-outcome-preimage-for-carrier?
                            [carrier mutated])))
                  [basename mutated]))))))))

(deftest sh07-b51-v18-private-resolution-is-registry-directed-not-shape-directed
  (let [registered (internal-reference 0)
        opaque (internal-reference 1)
        resolved [(sha \1) (sha \2)]
        carrier-value
        {:registered registered
         :opaque-metadata {:reference-shaped-value opaque}
         :b47-observation {:transported-reference opaque}}
        one-path
        (invoke 'sh07-b51-resolve-controlled-path
                [carrier-value [:registered] resolved])
        observation-request
        {:purpose :sh07-b51-predecessor-observation-id
         :preimage
         {:domain :gravity/sh07-b51-predecessor-observation-preimage-v18
          :schema-version 18
          :semantic-identity-id registered
          :raw-adapter-request
          {:opaque-metadata {:reference-shaped-value opaque}
           :b47-observation {:transported-reference opaque}}}}
        observation
        (invoke 'sh07-b51-resolve-controlled-preimage
                [observation-request :b51-vector-frontier-rejected resolved])
        provenance-request
        {:purpose :sh07-b51-provenance-binding-id
         :preimage
         {:domain :gravity/sh07-b51-provenance-binding-preimage-v18
          :schema-version 18
          :semantic-identity-id registered
          :observation-id opaque}}
        provenance
        (invoke 'sh07-b51-resolve-controlled-preimage
                [provenance-request :b51-vector-frontier-rejected resolved])]
    (is (= :accepted (:status one-path)) one-path)
    (is (= (sha \1) (get-in one-path [:value :registered])))
    (is (= opaque
           (get-in one-path
                   [:value :opaque-metadata :reference-shaped-value])))
    (is (= opaque
           (get-in one-path
                   [:value :b47-observation :transported-reference])))
    (is (= :accepted (:status observation)) observation)
    (is (= (sha \1)
           (get-in observation [:value :semantic-identity-id])))
    (is (= (:raw-adapter-request (:preimage observation-request))
           (get-in observation [:value :raw-adapter-request])))
    (is (= {opaque 2}
           (frequencies
            (internal-reference-occurrences
             (get-in observation [:value :raw-adapter-request])))))
    (is (= :accepted (:status provenance)) provenance)
    (is (= (sha \1)
           (get-in provenance [:value :semantic-identity-id])))
    (is (= (sha \2) (get-in provenance [:value :observation-id])))
    (doseq [[label value path digests reason]
            [[:path-not-vector carrier-value :registered resolved
              :path-not-vector]
             [:path-key-missing carrier-value [:missing] resolved
              :path-key-missing]
             [:path-map-required [] [:registered] resolved
              :path-map-required]
             [:generated-reference-required
              (assoc carrier-value :registered (sha \1))
              [:registered] resolved :generated-reference-representation]
             [:reference-ordinal-range
              (assoc carrier-value :registered (internal-reference 2))
              [:registered] resolved :reference-ordinal-range]
             [:resolved-id-shape carrier-value [:registered]
              [{} (sha \2)] :resolved-id-shape]]]
      (let [rejected
            (invoke 'sh07-b51-resolve-controlled-path
                    [value path digests])]
        (is (= {:status :rejected :reason reason} rejected)
            [label rejected])))
    (doseq [[label request digests reason]
            [[:missing-registered-path
              (update provenance-request :preimage dissoc :observation-id)
              resolved :path-key-missing]
             [:unregistered-extra-reference
              (assoc-in provenance-request
                        [:preimage :opaque-reference]
                        (internal-reference 0))
              resolved :unregistered-internal-reference]
             [:wrong-resolved-id provenance-request
              [(sha \1) {}] :resolved-id-shape]
             [:ordinal-range
              (assoc-in provenance-request
                        [:preimage :observation-id]
                        (internal-reference 2))
              resolved :reference-ordinal-range]]]
      (let [rejected
            (invoke 'sh07-b51-resolve-controlled-preimage
                    [request :b51-vector-frontier-rejected digests])]
        (is (= :rejected (:status rejected)) [label rejected])
        (is (= reason (:reason rejected)) [label rejected])))))

(deftest sh07-b51-v18-public-root4-parity-activates-with-authentic-root1-plan
  ;; Root 1 alone may construct a complete authentic plan.  This test becomes
  ;; executable when roots 1 and 4 are both exposed; until then it records the
  ;; exact contract without inventing a test-only producer.
  (let [definitions (definitions-by-name @source-forms)
        ready?
        (every? #(contains? definitions %)
                '[sh07-b51-build-template sh07-b51-resolve-template])]
    (if (not ready?)
      (is true "root-1/root-4 authentic public pipeline is not exposed yet")
      (let [carrier @nested-let-carrier
            built (invoke 'sh07-b51-build-template [carrier])]
        (is (contains? #{[:accepted :template-built]
                         [:pending-rejection :template-rejected]}
                       [(:status built) (:tag built)])
            built)
        (when (= :accepted (:status built))
          (let [template (get-in built [:value :template])
                requests (get-in built [:value :digest-requests])
                resolved (resolve-digest-plan requests)
                private
                (invoke 'sh07-b51-resolve-template-private
                        [template requests resolved])
                public
                (invoke 'sh07-b51-resolve-template
                        [template requests resolved])
                bundle (:value public)
                core (:resolved-core bundle)
                observation (:predecessor-observation bundle)
                observation-preimage (:preimage observation)
                provenance (:provenance-binding bundle)
                provenance-preimage (:preimage provenance)
                same-request
                (resolved-id-for-purpose
                 requests resolved :sh07-b51-same-request-semantic-id)
                authority
                (resolved-id-for-purpose
                 requests resolved :sh07-b51-predecessor-authority-id)
                identity
                (resolved-id-for-purpose
                 requests resolved :sh07-b51-core-identity-id)
                observation-id
                (resolved-id-for-purpose
                 requests resolved :sh07-b51-predecessor-observation-id)
                provenance-id
                (resolved-id-for-purpose
                 requests resolved :sh07-b51-provenance-binding-id)]
            (is (= (count requests) (count resolved)))
            (is (every? #(invoke 'sh07-b51-sha256-id? [%]) resolved))
            (is (= private public) [private public])
            (is (= (bootstrap/reader-canonical-hash private)
                   (bootstrap/reader-canonical-hash public)))
            (is (exact-abi? public :accepted :template-resolved) public)
            (is (= #{:resolved-core :predecessor-observation
                     :provenance-binding}
                   (set (keys bundle))))
            (is (= (set resolved-core-key-order) (set (keys core))))
            (is (= 51 (count core)))
            (is (= :gravity/sh07-b51-resolved-core-v18 (:artifact core)))
            (is (= 18 (:schema-version core)))
            (is (= [] (:diagnostics core)))
            (is (empty? (internal-reference-occurrences core)))
            (is (true? (invoke 'sh07-b51-resolved-core-shape? [core])))
            (doseq [joined [same-request authority identity observation-id
                            provenance-id]]
              (is (map? joined) joined)
              (is (= (:ordinal joined)
                     (:ordinal (:request joined))) joined)
              (is (= (:purpose (:request joined))
                     (get-in requests [(:ordinal joined) :purpose]))
                  joined)
              (is (invoke 'sh07-b51-sha256-id? [(:resolved-id joined)])))
            (is (= (:resolved-id same-request)
                   (:same-request-semantic-id core)))
            (is (= (:resolved-id authority)
                   (:predecessor-authority-id core)))
            (is (= (:resolved-id identity) (:semantic-identity-id core)))
            (is (= (:observation tier4-keysets)
                   (set (keys observation))))
            (is (= :gravity/sh07-b51-predecessor-observation-v18
                   (:artifact observation)))
            (is (= (:resolved-id observation-id)
                   (:observation-id observation)))
            (is (= (:observation-preimage tier4-keysets)
                   (set (keys observation-preimage))))
            (is (= (:resolved-id identity)
                   (:semantic-identity-id observation-preimage)))
            (is (= carrier (:raw-adapter-request observation-preimage)))
            (is (= (bootstrap/reader-canonical-hash carrier)
                   (bootstrap/reader-canonical-hash
                    (:raw-adapter-request observation-preimage))))
            (is (= (internal-reference-occurrences carrier)
                   (internal-reference-occurrences
                    (:raw-adapter-request observation-preimage))))
            (is (= (:provenance tier4-keysets)
                   (set (keys provenance))))
            (is (= :gravity/sh07-b51-provenance-binding-v18
                   (:artifact provenance)))
            (is (= (:resolved-id provenance-id)
                   (:provenance-binding-id provenance)))
            (is (= (:provenance-preimage tier4-keysets)
                   (set (keys provenance-preimage))))
            (is (= (:resolved-id identity)
                   (:semantic-identity-id provenance-preimage)))
            (is (= (:resolved-id observation-id)
                   (:observation-id provenance-preimage)))
            (doseq [[label reason mutated-template mutated-requests
                     mutated-resolved]
                    [[:missing-digest :digest-mismatch template requests
                      (vec (butlast resolved))]
                     [:reordered-digests :digest-mismatch template requests
                      (vec (reverse resolved))]
                     [:wrong-digest :digest-mismatch template requests
                      (assoc resolved 0 (sha \f))]
                     [:malformed-digest :digest-mismatch template requests
                      (assoc resolved 0 {})]
                     [:internal-reference-residue :digest-mismatch
                      template requests
                      (assoc resolved 0 (internal-reference 0))]
                     [:missing-request :invocation-mismatch template
                      (vec (butlast requests))
                      resolved]
                     [:reordered-requests :invocation-mismatch template
                      (vec (reverse requests))
                      resolved]
                     [:wrong-request-purpose :invocation-mismatch template
                      (assoc-in requests [0 :purpose]
                                :sh07-b51-contract-semantic-id)
                      resolved]
                     [:wrong-request-ordinal :invocation-mismatch template
                      (assoc-in requests [0 :ordinal] 1) resolved]
                     [:template-plan-mismatch :invocation-mismatch
                      (assoc template :digest-requests
                             (vec (butlast requests)))
                      requests resolved]]]
              (let [rejected
                    (invoke 'sh07-b51-resolve-template
                            [mutated-template mutated-requests
                             mutated-resolved])]
                (is (exact-boundary?
                     rejected :template-resolution-boundary-rejected
                     :sh07-b51-resolve-template reason)
                    [label rejected])))))))))

(deftest sh07-b51-v18-permissive-foundation-cannot-establish-root3-success
  (let [specs
        (mapv #(invoke 'sh07-b51-success-purpose-spec [%]) purpose-order)
        nonlegacy-purposes
        (vec (remove
              (set [:sh07-b51-v16-verification-semantic-id
                    :sh07-b51-product-node-id
                    :sh07-b51-binding-slot-id
                    :sh07-b51-binding-extraction-id
                    :sh07-b51-runtime-check-id
                    :sh07-b51-publication-event-id
                    :sh07-b51-recur-slot-mapping-id])
              purpose-order))
        requests
        (mapv
         (fn [ordinal purpose]
           (let [spec (invoke 'sh07-b51-success-purpose-spec [purpose])
                 provisional
                 {:ordinal ordinal
                  :tier (:tier spec)
                  :subtier (:subtier spec)
                  :subtier-ordinal nil
                  :batch (:batch spec)
                  :batch-rank (:batch-rank spec)
                  :batch-ordinal 0
                  :purpose purpose
                  :preimage
                  (cond-> {:domain (:domain spec)}
                    (= purpose :sh07-b51-predecessor-outcome-semantic-id)
                    (assoc :outcome-kind :b51-vector-frontier-rejected))}]
             (assoc provisional :subtier-ordinal
                    (invoke 'sh07-b51-success-request-subtier-ordinal
                            [provisional 1]))))
         (range) nonlegacy-purposes)
        replay (invoke 'sh07-b51-success-request-plan-replay [requests])
        mutate-first
        (fn [f]
          (let [request (f (first requests))
                plan (assoc requests 0 request)]
            (invoke 'sh07-b51-resolve-digest-preimage-foundation
                    [request plan []])))
        reversed-plan (vec (reverse requests))
        rejected-envelopes
        [(mutate-first #(assoc % :purpose :unknown-purpose))
         (mutate-first #(assoc-in % [:preimage :domain] :wrong-domain))
         (mutate-first #(update % :tier inc))
         (mutate-first #(assoc % :subtier :0d))
         (mutate-first #(assoc % :batch :t6-final))
         (mutate-first #(assoc % :batch-rank 18))
         (mutate-first #(assoc % :ordinal 1))
         (invoke 'sh07-b51-resolve-digest-preimage-foundation
                 [(first reversed-plan) reversed-plan []])
         (invoke 'sh07-b51-resolve-digest-preimage-foundation
                 [(nth requests 2) requests []])
         (invoke 'sh07-b51-resolve-digest-preimage-foundation
                 [(nth requests 2) requests [(sha \e) {}]])
         (invoke 'sh07-b51-resolve-digest-preimage-foundation
                 [(first requests) (vec (butlast requests)) []])]]
    (is (= 19 (count specs)))
    (is (= (vec (range 19)) (mapv :batch-rank specs)))
    (is (= 19 (count (set (map :preimage-domain specs)))))
    (is (= 58 (reduce + (vals dependency-counts))))
    (is (= dependency-counts
           (into {}
                 (map (fn [purpose]
                        [purpose
                         (count
                          (invoke 'sh07-b51-purpose-dependencies
                                  [purpose]))]))
                 purpose-order)))
    (is (= 58 (invoke 'sh07-b51-dependency-catalog-edge-count [])))
    (is (= 12 (count requests)))
    (is (= {:status :rejected :reason :malformed-request-plan}
           replay))
    (is (some false?
              (mapv #(invoke 'sh07-b51-success-request-exact-shape? [%])
                    requests)))
    (is (= {:status :rejected :reason :malformed-request-plan}
           (invoke 'sh07-b51-success-plan-exact? [requests])))
    (doseq [envelope rejected-envelopes]
      (is (exact-abi? envelope :boundary-rejected
                      :digest-preimage-boundary-rejected)
          envelope)
      (is (= #{:status :boundary :reason
               :recursive-diagnostic-forbidden}
             (set (keys (:value envelope)))))
      (is (true? (get-in envelope
                         [:value :recursive-diagnostic-forbidden]))))))

(deftest sh07-b51-v18-digest-failure-stream-is-singleton-and-branch-exact
  (let [semantic
        (invoke 'sh07-b51-diagnostic-semantic
                [:core-shape :request-shape
                 {:form-id nil :form-ordinal nil :fragment-ordinal nil
                  :syntax-id nil :source-span nil
                  :generated-origin-chain [] :core-node-id nil
                  :owner-coordinate nil :slot-id nil :extraction-id nil
                  :recur-form-id nil :recur-syntax-id nil :path nil}
                 {:request-shape :authenticated}
                 {:request-shape :malformed}
                 []])
        request
        {:purpose :sh07-b51-c6-diagnostic-id
         :preimage
         {:domain :gravity/sh07-b51-c6-diagnostic-id-preimage-v18
          :schema-version 18
          :diagnostic-semantic-value semantic}}
        success-like-request
        {:ordinal 0 :tier 0 :subtier :0a :subtier-ordinal 0
         :batch :t0-implementation :batch-rank 0 :batch-ordinal 0
         :purpose :sh07-b51-implementation-semantic-id
         :preimage
         {:domain :gravity/sh07-b51-implementation-semantic-preimage-v18}}
        forged-outcome-request
        {:ordinal 0 :tier 0 :subtier :0b :subtier-ordinal 0
         :batch :t0-predecessor-outcome :batch-rank 4 :batch-ordinal 0
         :purpose :sh07-b51-predecessor-outcome-semantic-id
         :preimage
         {:domain
          :gravity/sh07-b51-predecessor-outcome-semantic-preimage-v18
          :outcome-kind :b51-diagnostic-failure}}
        accepted
        (invoke 'sh07-b51-resolve-digest-preimage-private
                [request [request] []])
        malformed-requests
        [(assoc request :extra :forbidden)
         (assoc request :purpose :sh07-b51-core-node-id)
         (dissoc request :preimage)
         (assoc-in request [:preimage :domain] :wrong-domain)
         (assoc-in request [:preimage :schema-version] 17)
         (assoc-in request [:preimage :outcome-kind]
                   :b51-vector-frontier-rejected)
         (assoc-in request [:preimage :diagnostic-semantic-value]
                   (assoc semantic :extra :forbidden))]
        rejected-calls
        (concat
         (map (fn [malformed]
                [malformed [malformed] []])
              malformed-requests)
         [[request [] []]
          [request [request request] []]
          [request [request success-like-request] []]
          [request [success-like-request request] []]
          [request [request] [(sha \e)]]
          [success-like-request [request] []]
          [forged-outcome-request [forged-outcome-request] []]])]
    (is (true? (invoke 'sh07-b51-failure-digest-request-shape?
                       [request])))
    (is (exact-abi? accepted :accepted :digest-preimage-resolved)
        accepted)
    (is (= #{:status :request :digest-requests :resolved-prefix :hash-input}
           (set (keys (:value accepted)))))
    (is (= request (get-in accepted [:value :request])))
    (is (= [request] (get-in accepted [:value :digest-requests])))
    (is (= [] (get-in accepted [:value :resolved-prefix])))
    (is (= {:domain :gravity/sh07-declared-digest-v1
            :purpose (:purpose request)
            :preimage (:preimage request)}
           (get-in accepted [:value :hash-input])))
    (doseq [malformed malformed-requests]
      (is (false? (invoke 'sh07-b51-failure-digest-request-shape?
                          [malformed]))
          malformed))
    (doseq [[candidate plan prefix] rejected-calls]
      (let [rejected
            (invoke 'sh07-b51-resolve-digest-preimage-private
                    [candidate plan prefix])]
        (is (exact-abi? rejected :boundary-rejected
                        :digest-preimage-boundary-rejected)
            [candidate plan prefix rejected])
        (is (true? (get-in rejected
                           [:value :recursive-diagnostic-forbidden]))
            rejected)))))

(deftest sh07-b51-v18-public-root3-is-an-exact-private-mechanics-wrapper
  (let [semantic
        (invoke 'sh07-b51-diagnostic-semantic
                [:core-shape :request-shape
                 {:form-id nil :form-ordinal nil :fragment-ordinal nil
                  :syntax-id nil :source-span nil
                  :generated-origin-chain [] :core-node-id nil
                  :owner-coordinate nil :slot-id nil :extraction-id nil
                  :recur-form-id nil :recur-syntax-id nil :path nil}
                 {:request-shape :authenticated}
                 {:request-shape :malformed}
                 []])
        failure-request
        {:purpose :sh07-b51-c6-diagnostic-id
         :preimage
         {:domain :gravity/sh07-b51-c6-diagnostic-id-preimage-v18
          :schema-version 18
          :diagnostic-semantic-value semantic}}
        calls
        [[failure-request [failure-request] []]
         [{} [] []]
         [failure-request [] []]
         [failure-request [failure-request failure-request] []]
         [failure-request [failure-request] [(sha \7)]]
         [(assoc failure-request :extra :forbidden)
          [(assoc failure-request :extra :forbidden)] []]]]
    (doseq [[request plan prefix] calls]
      (let [private
            (invoke 'sh07-b51-resolve-digest-preimage-private
                    [request plan prefix])
            public
            (invoke 'sh07-b51-resolve-digest-preimage
                    [request plan prefix])]
        (is (= private public) [request plan prefix private public])
        (is (= (bootstrap/reader-canonical-hash private)
               (bootstrap/reader-canonical-hash public)))
        (is (= #{:artifact :domain :schema-version :status :tag :value}
               (set (keys public))))
        (if (= request failure-request)
          (if (and (= plan [failure-request]) (= prefix []))
            (do
              (is (exact-abi? public :accepted
                              :digest-preimage-resolved))
              (is (= #{:status :request :digest-requests
                       :resolved-prefix :hash-input}
                     (set (keys (:value public))))))
            (is (exact-abi? public :boundary-rejected
                            :digest-preimage-boundary-rejected)))
          (is (exact-abi? public :boundary-rejected
                          :digest-preimage-boundary-rejected)))))))

(deftest sh07-b51-v18-public-root3-authentic-success-parity-activates-with-root1
  ;; Root 1 is the sole public producer of an authentic complete success plan.
  ;; Until that definition and an accepted (final-H-pinned) carrier are live,
  ;; this remains a visible dormant contract rather than a synthetic builder.
  (let [definitions (definitions-by-name @source-forms)
        root1-defined?
        (contains? definitions 'sh07-b51-build-template)]
    (if (not root1-defined?)
      (is true "root-1 success-plan producer is not exposed yet")
      (let [built
            (invoke 'sh07-b51-build-template [@nested-let-carrier])]
        (is (contains? #{[:accepted :template-built]
                         [:pending-rejection :template-rejected]}
                       [(:status built) (:tag built)])
            built)
        (is (= #{:artifact :domain :schema-version :status :tag :value}
               (set (keys built))))
        (when (= :accepted (:status built))
          (let [requests (get-in built [:value :digest-requests])
                exact-plan (invoke 'sh07-b51-success-plan-exact? [requests])
                outcome (invoke 'sh07-b51-success-outcome-kind [requests])
                active-edge-count
                (reduce
                 +
                 (for [purpose purpose-order]
                   (count
                    (filterv
                     #(invoke 'sh07-b51-dependency-entry-active?
                              [% outcome])
                     (invoke 'sh07-b51-purpose-dependencies [purpose])))))]
            (is (= :accepted (:status exact-plan)) exact-plan)
            (is (= 58 (:dependency-edge-count exact-plan)))
            (is (= (if (= outcome :legacy-v16-accepted) 57 56)
                   active-edge-count)
                [outcome active-edge-count])
            (loop [remaining requests resolved []]
              (when (seq remaining)
                (let [request (first remaining)
                      private
                      (invoke 'sh07-b51-resolve-digest-preimage-private
                              [request requests resolved])
                      public
                      (invoke 'sh07-b51-resolve-digest-preimage
                              [request requests resolved])]
                  (is (= private public) [(:ordinal request) private public])
                  (is (exact-abi? public :accepted
                                  :digest-preimage-resolved)
                      [(:ordinal request) public])
                  (is (= #{:status :request :digest-requests
                           :resolved-prefix :hash-input}
                         (set (keys (:value public)))))
                  (recur
                   (rest remaining)
                   (conj resolved
                         (bootstrap/reader-canonical-hash
                          (get-in public [:value :hash-input])))))))))))))

(deftest sh07-b51-v18-root3-purpose-and-typed-reference-registries-are-closed
  (let [catalog (invoke 'sh07-b51-purpose-catalog [])
        specs (mapv #(invoke 'sh07-b51-success-purpose-spec [%]) catalog)
        expected-tiers [0 0 0 0 0 0 0 0 1 1 2 2 2 2 3 4 4 5 6]
        expected-subtiers
        [:0a :0a :0a :0a :0b :0c :0d :0d
         nil nil nil nil nil nil nil nil nil nil nil]
        expected-batches
        [:0a-implementation :0a-contract :0a-same-request
         :0a-v16-verification :0b-outcome :0c-authority :0d-core-node
         :0d-product-node :t1-slot :t1-extraction :t2-runtime-check
         :t2-publication :t2-recur-mapping :t2-transcript
         :t3-core-identity :t4-observation :t4-provenance
         :t5-verifier :t6-final]
        expected-branches
        [:both :both :both :accepted :both :both :rejected :both :both
         :both :both :both :both :both :both :both :both :both :both]
        expected-cardinalities
        [:one-per-C :one :one :one :one :one :per-rejected-core-node
         :per-supplemental-product :per-slot :per-extraction
         :per-vector-node-use :per-event :per-recur :one :one :one :one
         :one :one]]
    (is (= purpose-order catalog))
    (is (= 19 (count catalog)))
    (is (= 19 (count (set catalog))))
    (is (= (vec (range 19)) (mapv :batch-rank specs)))
    (is (= expected-tiers (mapv :tier specs)))
    (is (= expected-subtiers (mapv :subtier specs)))
    (is (= expected-batches (mapv :batch specs)))
    (is (= expected-branches (mapv :branch specs)))
    (is (= expected-cardinalities (mapv :cardinality specs)))
    (is (= 19 (count (set (map :preimage-domain specs)))))
    (is (nil? (invoke 'sh07-b51-success-purpose-spec
                      [:sh07-b51-not-a-purpose])))
    (is (nil? (invoke 'sh07-b51-purpose-dependencies
                      [:sh07-b51-not-a-purpose])))
    (doseq [[purpose spec] (map vector catalog specs)]
      (is (= #{:preimage-domain :tier :subtier :batch :batch-rank
               :branch :cardinality}
             (set (keys spec)))
          [purpose spec])
      (is (contains? #{:both :accepted :rejected} (:branch spec)) purpose)
      (is (keyword? (:cardinality spec)) purpose)
      (is (= (root3-dependency-targets purpose)
             (invoke 'sh07-b51-purpose-dependencies [purpose]))
          purpose)
      (is (= (dependency-counts purpose)
             (count (invoke 'sh07-b51-purpose-dependencies [purpose])))
          purpose))
    (is (= 58 (reduce + (map count (vals root3-dependency-targets)))))
    (is (= 58 (invoke 'sh07-b51-dependency-catalog-edge-count [])))
    (is (= [0 0 0 0 4 4 2 1 2 2 4 5 7 7 12 1 2 2 3]
           (invoke 'sh07-b51-dependency-catalog-cardinalities [])))
    (is (true? (invoke 'sh07-b51-dependency-catalog-exact? [])))
    (let [core-row
          (invoke 'sh07-b51-purpose-dependencies
                  [:sh07-b51-core-identity-id])
          legacy-only-targets
          #{:sh07-b51-predecessor-outcome-semantic-id
            :sh07-b51-v16-verification-semantic-id}
          active-targets
          (fn [outcome]
            (vec
             (keep
              (fn [entry]
                (if (keyword? entry)
                  entry
                  (if (contains? entry :accepted-only)
                    (when (= outcome :legacy-v16-accepted)
                      (:accepted-only entry))
                    (when (not= outcome :legacy-v16-accepted)
                      (:non-v16-only entry)))))
              core-row)))
          legacy-active (active-targets :legacy-v16-accepted)
          expected-core-row
          (root3-dependency-targets :sh07-b51-core-identity-id)]
      (is (= 12 (count core-row)))
      (is (= expected-core-row core-row))
      (is (= [{:accepted-only
               :sh07-b51-v16-verification-semantic-id}
              {:accepted-only
               :sh07-b51-predecessor-outcome-semantic-id}
              {:non-v16-only :sh07-b51-core-node-id}]
             (filterv map? core-row)))
      (is (= legacy-only-targets
             (set/intersection legacy-only-targets (set legacy-active))))
      (is (not (some #{:sh07-b51-core-node-id} legacy-active)))
      (is (= 11 (count legacy-active)))
      (doseq [target legacy-only-targets]
        (is (true?
             (invoke 'sh07-b51-purpose-dependency-active?
                     [:sh07-b51-core-identity-id target
                      :legacy-v16-accepted]))
            target)
        (doseq [outcome (rest root3-outcome-kinds)]
          (is (false?
               (invoke 'sh07-b51-purpose-dependency-active?
                       [:sh07-b51-core-identity-id target outcome]))
              [target outcome])))
      (is (false?
           (invoke 'sh07-b51-purpose-dependency-active?
                   [:sh07-b51-core-identity-id :sh07-b51-core-node-id
                    :legacy-v16-accepted])))
      (doseq [outcome (rest root3-outcome-kinds)]
        (let [nonlegacy-active (active-targets outcome)]
          (is (empty? (set/intersection legacy-only-targets
                                        (set nonlegacy-active)))
              outcome)
          (is (some #{:sh07-b51-core-node-id} nonlegacy-active) outcome)
          (is (true?
               (invoke 'sh07-b51-purpose-dependency-active?
                       [:sh07-b51-core-identity-id
                        :sh07-b51-core-node-id outcome]))
              outcome)
          (is (= 10 (count nonlegacy-active)) outcome)))
      (doseq [malformed
              [{:both :sh07-b51-v16-verification-semantic-id}
               {:accepted-only
                :sh07-b51-v16-verification-semantic-id
                :extra :forbidden}
               {}]]
        (is (nil? (invoke 'sh07-b51-dependency-entry-target [malformed]))
            malformed)
        (is (false?
             (invoke 'sh07-b51-dependency-entry-active?
                     [malformed :legacy-v16-accepted]))
            malformed))
      (is (false?
           (invoke 'sh07-b51-dependency-row-exact?
                   [:sh07-b51-core-identity-id
                    [{:non-v16-only :unknown-purpose}]])))
      (doseq [mutated
              [(vec (butlast core-row))
               (conj core-row :sh07-b51-predecessor-outcome-semantic-id)
               (assoc core-row 1
                      {:both :sh07-b51-v16-verification-semantic-id})
               (assoc core-row 2
                      {:non-v16-only
                       :sh07-b51-predecessor-outcome-semantic-id})
               (assoc core-row 4
                      {:accepted-only :sh07-b51-core-node-id})
               (assoc core-row 1 (get core-row 2)
                      2 (get core-row 1))]]
        (is (not= expected-core-row mutated) mutated)))
    (doseq [outcome root3-outcome-kinds]
      (let [expected (root3-reference-census outcome)
            tier3 (invoke 'sh07-b51-tier3-reference-descriptors [outcome])
            non-tier3
            (vec
             (mapcat
              #(invoke 'sh07-b51-purpose-nontier3-reference-descriptors
                       [% outcome])
              purpose-order))
            tier3-generated
            (filterv #(= :generated (:representation %)) tier3)
            non-tier3-generated
            (filterv #(= :generated (:representation %)) non-tier3)
            top-level-paths
            #{[:same-request-semantic-id] [:predecessor-authority-id]}
            tier3-product-paths
            (remove #(contains? top-level-paths (:path %)) tier3)
            legacy-only-descriptors
            (filterv
             #(contains?
               #{:sh07-b51-predecessor-outcome-semantic-id
                 :sh07-b51-v16-verification-semantic-id}
               (:target-purpose %))
             tier3)]
        (is (= (:non-tier3 expected) (count non-tier3-generated)) outcome)
        (is (= (:tier3 expected) (count tier3-generated)) outcome)
        (is (= (:controlled expected)
               (invoke 'sh07-b51-reference-registry-generated-count
                       [outcome]))
            outcome)
        (is (true?
             (invoke 'sh07-b51-reference-registry-static-shape?
                     [outcome]))
            outcome)
        (is (true?
             (invoke 'sh07-b51-reference-registry-static-census?
                     [outcome]))
            outcome)
        (is (= (if (= outcome :legacy-v16-accepted) 2 0)
               (count legacy-only-descriptors))
            [outcome legacy-only-descriptors])
        (is (= 2 (count (filter #(contains? top-level-paths (:path %))
                               tier3)))
            outcome)
        ;; Tier-3 product paths are relative to :products only after the
        ;; closed outcome row is selected.  A top-level-looking product path
        ;; would collide with the two semantic identity authority fields.
        (is (every? #(= :products (first (:path %))) tier3-product-paths)
            outcome)
        (doseq [purpose purpose-order]
          (let [descriptors
                (invoke 'sh07-b51-purpose-reference-descriptors
                        [purpose outcome])]
            (is (true?
                 (invoke 'sh07-b51-reference-paths-unique? [descriptors]))
                [outcome purpose])
            (doseq [descriptor descriptors]
              (is (= #{:path :target-purpose :representation}
                     (set (keys descriptor)))
                  [outcome purpose descriptor])
              (is (vector? (:path descriptor)) descriptor)
              (is (contains? #{:generated :concrete}
                             (:representation descriptor))
                  descriptor)
              (if (= :generated (:representation descriptor))
                (is (contains? (set purpose-order)
                               (:target-purpose descriptor))
                    descriptor)
                (is (nil? (:target-purpose descriptor)) descriptor)))))))
    (is (false?
         (invoke 'sh07-b51-reference-registry-static-census?
                 [:forged-outcome])))))

(deftest sh07-b51-v18-root3-branch-and-cardinality-are-replay-derived
  (let [products
        (-> (zipmap product-key-order (repeat []))
            (assoc :nodes [:node-0 :node-1]
                   :destructuring-product-nodes [:product-0 :product-1
                                                 :product-2]
                   :binding-slots [:slot-0 :slot-1]
                   :binding-extractions [:extraction-0 :extraction-1]
                   :runtime-checks [:check-0 :check-1]
                   :publication-events [:publication-0]
                   :recur-slot-mappings [:mapping-0]))
        build-count-plan
        (fn [outcome]
          (vec
           (mapcat
            (fn [purpose]
              (repeat
               (invoke 'sh07-b51-expected-purpose-count
                       [purpose outcome products])
               (cond-> {:purpose purpose}
                 (= purpose :sh07-b51-predecessor-outcome-semantic-id)
                 (assoc :preimage {:outcome-kind outcome})
                 (= purpose :sh07-b51-core-identity-id)
                 (assoc :preimage {:products products}))))
            purpose-order)))]
    (doseq [outcome root3-outcome-kinds]
      (let [legacy? (= outcome :legacy-v16-accepted)
            plan (build-count-plan outcome)]
        (is (= (if legacy? 1 0)
               (invoke 'sh07-b51-expected-purpose-count
                       [:sh07-b51-v16-verification-semantic-id
                        outcome products])))
        (is (= (if legacy? 0 2)
               (invoke 'sh07-b51-expected-purpose-count
                       [:sh07-b51-core-node-id outcome products])))
        (is (= 3
               (invoke 'sh07-b51-expected-purpose-count
                       [:sh07-b51-product-node-id outcome products])))
        (is (= 2
               (invoke 'sh07-b51-expected-purpose-count
                       [:sh07-b51-binding-slot-id outcome products])))
        (doseq [purpose purpose-order]
          (is (= (invoke 'sh07-b51-expected-purpose-count
                         [purpose outcome products])
                 (invoke 'sh07-b51-purpose-count [plan purpose]))
              [outcome purpose]))
        ;; Counts alone cannot admit: the strengthened exact gate also binds
        ;; every Tier-3 record to its scheduled request and authentic base.
        (is (false? (invoke 'sh07-b51-success-plan-exact-cardinality?
                            [plan]))
            [outcome :count-only-plan])
        (is (false? (invoke 'sh07-b51-success-plan-exact-cardinality?
                            [(vec (rest plan))]))
            outcome)
        (is (false? (invoke 'sh07-b51-success-plan-exact-cardinality?
                            [(conj plan (first plan))]))
            outcome)))
    (doseq [purpose purpose-order
            outcome root3-outcome-kinds]
      (let [branch (:branch (invoke 'sh07-b51-success-purpose-spec [purpose]))
            expected
            (case branch
              :both true
              :accepted (= outcome :legacy-v16-accepted)
              :rejected (not= outcome :legacy-v16-accepted)
              false)]
        (is (= expected
               (invoke 'sh07-b51-purpose-branch-allows?
                       [purpose outcome]))
            [purpose outcome])))
    (is (= -1 (invoke 'sh07-b51-expected-purpose-count
                      [:unknown-purpose :legacy-v16-accepted products])))
    (is (false? (invoke 'sh07-b51-success-plan-exact-cardinality?
                        [(build-count-plan :forged-outcome)])))
    (is (every?
         false?
         (mapv #(invoke 'sh07-b51-purpose-branch-allows?
                        [% :forged-outcome])
               purpose-order)))
    (is (false? (invoke 'sh07-b51-purpose-branch-allows?
                        [:unknown-purpose :legacy-v16-accepted])))))

(deftest sh07-b51-v18-root3-reference-joins-require-exact-earlier-targets
  (let [outcome :b51-vector-frontier-rejected
        ref #(invoke 'sh07-b51-internal-reference [%])
        outcome-request
        {:ordinal 0 :batch-rank 4 :batch-ordinal 0
         :purpose :sh07-b51-predecessor-outcome-semantic-id
         :preimage {:outcome-kind outcome}}
        identity-request
        {:ordinal 1 :batch-rank 14 :batch-ordinal 0
         :purpose :sh07-b51-core-identity-id :preimage {}}
        observation-request
        {:ordinal 2 :batch-rank 15 :batch-ordinal 0
         :purpose :sh07-b51-predecessor-observation-id
         :preimage {:semantic-identity-id (ref 1)}}
        observation-plan
        [outcome-request identity-request observation-request]
        observation-prefix [(sha \1) (sha \2)]
        controlled
        (invoke 'sh07-b51-request-controlled-references
                [observation-request outcome])
        missing-reference
        (assoc observation-request :preimage {})
        extra-reference
        {:purpose :sh07-b51-provenance-binding-id
         :preimage {:semantic-identity-id (ref 1)
                    :observation-id (ref 2)
                    :unregistered (ref 1)}}
        wrong-target-plan
        (assoc observation-plan 1
               (assoc identity-request
                      :purpose :sh07-b51-final-artifact-binding-id))
        forward-observation
        (assoc observation-request
               :ordinal 1
               :preimage {:semantic-identity-id (ref 2)})
        forward-plan
        [outcome-request forward-observation
         (assoc identity-request :ordinal 2)]
        authority-request
        {:ordinal 1 :batch-rank 5 :batch-ordinal 0
         :purpose :sh07-b51-predecessor-authority-id :preimage {}}
        child-request
        {:ordinal 2 :batch-rank 6 :batch-ordinal 0
         :purpose :sh07-b51-core-node-id
         :preimage {:predecessor-authority-id (ref 1)
                    :children [] :evaluated-children []}}
        parent-request
        {:ordinal 3 :batch-rank 6 :batch-ordinal 1
         :purpose :sh07-b51-core-node-id
         :preimage {:predecessor-authority-id (ref 1)
                    :children [(ref 2)]
                    :evaluated-children [(ref 2)]}}
        core-plan
        [outcome-request authority-request child-request parent-request]
        core-prefix [(sha \3) (sha \4) (sha \5)]
        cycle-left
        (assoc child-request
               :preimage {:predecessor-authority-id (ref 1)
                          :children [(ref 3)] :evaluated-children []})
        cycle-right
        (assoc parent-request
               :preimage {:predecessor-authority-id (ref 1)
                          :children [(ref 2)] :evaluated-children []})
        cycle-plan
        [outcome-request authority-request cycle-left cycle-right]]
    (is (= :accepted (:status controlled)) controlled)
    (is (= [{:reference (ref 1)
             :path [:semantic-identity-id]
             :target-purpose :sh07-b51-core-identity-id}]
           (:references controlled)))
    (is (true? (invoke 'sh07-b51-request-reference-dag-valid?
                       [observation-request observation-plan outcome])))
    (is (true? (invoke 'sh07-b51-request-reference-prefix-valid?
                       [observation-request outcome observation-prefix])))
    (is (false? (invoke 'sh07-b51-request-reference-prefix-valid?
                        [observation-request outcome []])))
    (is (false? (invoke 'sh07-b51-request-reference-prefix-valid?
                        [observation-request outcome [(sha \1) {}]])))
    (is (false? (invoke 'sh07-b51-request-reference-dag-valid?
                        [observation-request wrong-target-plan
                         outcome])))
    (is (false? (invoke 'sh07-b51-request-reference-dag-valid?
                        [forward-observation forward-plan
                         outcome])))
    (is (= :path-key-missing
           (:reason
            (invoke 'sh07-b51-request-controlled-references
                    [missing-reference outcome]))))
    (is (= :unregistered-internal-reference
           (:reason
            (invoke 'sh07-b51-request-controlled-references
                    [extra-reference outcome]))))
    (is (true? (invoke 'sh07-b51-request-reference-dag-valid?
                       [parent-request core-plan outcome])))
    (is (true? (invoke 'sh07-b51-request-reference-prefix-valid?
                       [parent-request outcome core-prefix])))
    (is (false? (invoke 'sh07-b51-request-reference-dag-valid?
                        [(assoc parent-request :batch-ordinal 0)
                         (assoc core-plan 3
                                (assoc parent-request :batch-ordinal 0))
                         outcome])))
    (is (false? (invoke 'sh07-b51-request-reference-dag-valid?
                        [cycle-left cycle-plan outcome])))
    (is (true? (invoke 'sh07-b51-request-reference-dag-valid?
                       [cycle-right cycle-plan outcome])))
    (is (false? (invoke 'sh07-b51-success-plan-reference-dag-valid?
                        [cycle-plan outcome])))
    (is (false? (invoke 'sh07-b51-strict-coordinate-earlier?
                        [identity-request identity-request])))
    (is (false? (invoke 'sh07-b51-resolved-prefix-shape?
                        [observation-prefix 1])))
    (is (true? (invoke 'sh07-b51-resolved-prefix-shape?
                       [observation-prefix 2])))))

(deftest sh07-b51-v18-root3-reference-descriptor-representation-is-exact
  (let [reference (invoke 'sh07-b51-internal-reference [0])
        generated
        {:path [:value]
         :target-purpose :sh07-b51-implementation-semantic-id
         :representation :generated}
        concrete
        {:path [:value] :target-purpose nil :representation :concrete}
        valid-generated
        (invoke 'sh07-b51-validate-reference-descriptors
                [{:value reference} [generated]])
        valid-concrete
        (invoke 'sh07-b51-validate-reference-descriptors
                [{:value (sha \6)} [concrete]])]
    (is (= :accepted (:status valid-generated)) valid-generated)
    (is (= :accepted (:status valid-concrete)) valid-concrete)
    (is (= :generated-reference-representation
           (:reason
            (invoke 'sh07-b51-validate-reference-descriptors
                    [{:value (sha \6)} [generated]]))))
    (is (= :concrete-reference-representation
           (:reason
            (invoke 'sh07-b51-validate-reference-descriptors
                    [{:value reference} [concrete]]))))
    (is (= :reference-path-duplicate
           (:reason
            (invoke 'sh07-b51-validate-reference-descriptors
                    [{:value reference} [generated generated]]))))
    (is (= :path-key-missing
           (:reason
            (invoke 'sh07-b51-validate-reference-descriptors
                    [{} [generated]]))))))

(deftest sh07-b51-v18-root3-boundary-reasons-are-the-five-closed-values
  (is (= 5 (count root3-boundary-reasons)))
  (doseq [reason root3-boundary-reasons]
    (is (= reason (invoke 'sh07-b51-digest-boundary-reason [reason])))
    (let [envelope (invoke 'sh07-b51-digest-boundary-envelope [reason])]
      (is (exact-abi? envelope :boundary-rejected
                      :digest-preimage-boundary-rejected))
      (is (= #{:status :boundary :reason :recursive-diagnostic-forbidden}
             (set (keys (:value envelope)))))
      (is (= reason (get-in envelope [:value :reason])))
      (is (= :sh07-b51-resolve-digest-preimage
             (get-in envelope [:value :boundary])))
      (is (true? (get-in envelope
                         [:value :recursive-diagnostic-forbidden])))))
  (doseq [forged [nil :unknown-boundary "malformed-request" {}]]
    (is (= :malformed-request-plan
           (invoke 'sh07-b51-digest-boundary-reason [forged])))))

(deftest sh07-b51-v18-tier3-full-product-schema-is-closed-for-all-outcomes
  (let [ref #(invoke 'sh07-b51-internal-reference [%])
        transcript-preimage
        {:domain
         :gravity/sh07-b51-slot-extraction-transcript-id-preimage-v18
         :schema-version 18
         :predecessor-authority-id (ref 5)
         :same-request-semantic-id (ref 2)
         :slot-count 0 :extraction-count 0 :runtime-check-count 0
         :publication-event-count 0 :recur-slot-mapping-count 0
         :slot-ids [] :extraction-ids [] :runtime-check-ids []
         :publication-event-ids [] :recur-slot-mapping-ids []
         :global-slot-range {:first 0 :count 0}
         :global-extraction-range {:first 0 :count 0}
         :global-runtime-check-range {:first 0 :count 0}
         :global-publication-range {:first 0 :count 0}
         :global-recur-range {:first 0 :count 0}}
        transcript
        (-> transcript-preimage
            (dissoc :domain :schema-version)
            (assoc :schema
                   :gravity/sh07-b51-slot-extraction-transcript-v18
                   :transcript-id (ref 13)))
        v16-projection
        (zipmap (invoke 'sh07-b51-v16-semantic-key-order []) (repeat nil))
        legacy-equivalence
        {:domain :gravity/sh07-b51-legacy-v16-equivalence-v18
         :schema-version 18
         :legacy-artifact-kind :gravity/sh07-canonical-core-artifact
         :legacy-schema-version 16
         :legacy-wrapper-kind nil
         :legacy-identity-preimage-domain
         :gravity/sh07-b47-canonical-core-v16
         :same-request-semantic-id (ref 2)
         :predecessor-outcome-semantic-id (ref 4)
         :inherited-v16-semantic-artifact-id (sha \7)
         :inherited-v16-artifact-purpose :sh07-core-artifact-id
         :v16-semantic-projection v16-projection
         :v18-compatibility-projection v16-projection
         :v16-semantic-key-order
         (invoke 'sh07-b51-v16-semantic-key-order [])
         :field-mapping []
         :normalization-policy :v16-resolved-semantic-canonical-v1
         :missing-fields [] :unexpected-fields [] :delta [] :equal? true
         :v16-verification-semantic-id (ref 3)}
        products-for
        (fn [outcome]
          (-> (zipmap product-key-order (repeat []))
              (assoc :fragment-coverage {}
                     :module-assembly-manifest {}
                     :macro-origin-expectation {}
                     :slot-extraction-transcript transcript
                     :legacy-v16-equivalence
                     (if (= outcome :legacy-v16-accepted)
                       legacy-equivalence nil))))
        preimage-for
        (fn [outcome]
          {:domain :gravity/sh07-b51-core-identity-preimage-v18
           :schema-version 18
           :artifact-kind :gravity/sh07-b51-canonical-core-artifact-v18
           :task "SH-07-B51"
           :scope :sh07-b51-vector-destructuring
           :predecessor-outcome-kind outcome
           :same-request-semantic-id (ref 2)
           :predecessor-authority-id (ref 5)
           :module {}
           :lineage {}
           :projection-binding (sha \8)
           :product-key-order product-key-order
           :products (products-for outcome)
           :diagnostics []})]
    (is (= product-key-order (invoke 'sh07-b51-product-key-order [])))
    (is (= 37 (count (invoke 'sh07-b51-v16-semantic-key-order []))))
    (is (= 37 (count v16-projection)))
    (doseq [outcome root3-outcome-kinds]
      (let [preimage (preimage-for outcome)
            request {:purpose :sh07-b51-core-identity-id
                     :preimage preimage}
            controlled
            (invoke 'sh07-b51-request-controlled-references
                    [request outcome])]
        (is (true? (invoke 'sh07-b51-core-identity-preimage-shape?
                           [preimage]))
            outcome)
        (is (= :accepted (:status controlled)) [outcome controlled])
        (is (every?
             #(contains? (set purpose-order) (:target-purpose %))
             (:references controlled))
            outcome)
        (is (= product-key-order (:product-key-order preimage)))
        (is (= (set product-key-order)
               (set (keys (:products preimage)))))
        (doseq [mutated
                [(dissoc preimage :products)
                 (assoc preimage :extra :forbidden)
                 (assoc preimage :diagnostics [{}])
                 (assoc preimage :product-key-order
                        (vec (reverse product-key-order)))
                 (assoc preimage :predecessor-outcome-kind :forged-outcome)
                 (assoc-in preimage [:products :forged-product] [])
                 (update preimage :products dissoc :nodes)]]
          (is (false? (invoke 'sh07-b51-core-identity-preimage-shape?
                              [mutated]))
              [outcome mutated]))
        (if (= outcome :legacy-v16-accepted)
          (is (= :concrete-reference-representation
                 (:reason
                  (invoke
                   'sh07-b51-request-controlled-references
                   [(assoc-in
                     request
                     [:preimage :products :legacy-v16-equivalence
                      :inherited-v16-semantic-artifact-id]
                     (ref 3))
                    outcome]))))
          (is (= :generated-reference-representation
                 (:reason
                  (invoke
                   'sh07-b51-request-controlled-references
                   [(assoc-in request
                              [:preimage :same-request-semantic-id]
                              (sha \9))
                    outcome])))))))))

(deftest sh07-b51-v18-composition-independently-determines-root3-cardinality
  (let [let-context (let-owner-plan-context)
        loop-context (loop-recur-context)
        cases
        [[:let (:request let-context) (:owner-plans let-context)]
         [:loop (:request loop-context) (loop-owner-plans loop-context)]]
        purpose-by-record-kind
        {:ordinary-core-node :sh07-b51-core-node-id
         :supplemental-product :sh07-b51-product-node-id
         :binding-slot :sh07-b51-binding-slot-id
         :binding-extraction :sh07-b51-binding-extraction-id
         :runtime-check :sh07-b51-runtime-check-id
         :publication-event :sh07-b51-publication-event-id
         :recur-slot-mapping :sh07-b51-recur-slot-mapping-id}
        output-field-by-record-kind
        {:supplemental-product :product-node-preimages
         :binding-slot :binding-slot-preimages
         :binding-extraction :binding-extraction-preimages
         :runtime-check :runtime-check-preimages
         :publication-event :publication-event-preimages
         :recur-slot-mapping :recur-slot-mapping-preimages}
        replay-count-key-by-record-kind
        {:ordinary-core-node :global-core-node-ordinal
         :supplemental-product :global-product-ordinal
         :binding-slot :global-slot-ordinal
         :binding-extraction :global-extraction-ordinal
         :runtime-check :global-runtime-check-ordinal
         :publication-event :global-publication-ordinal
         :recur-slot-mapping :global-recur-mapping-ordinal}]
    (doseq [[kind request owner-plans] cases]
      (let [expected
            (invoke 'sh07-b51-compose-expected-occurrence-specs
                    [request owner-plans])
            specs (:specs expected)
            independent-counts (frequencies (map :record-kind specs))
            composed
            (invoke 'sh07-b51-compose-numbered-preimages
                    [request owner-plans specs
                     (internal-reference 1) (internal-reference 2)])
            replay-counts
            (get-in composed [:occurrence-replay :category-counts])
            products
            (-> (zipmap product-key-order (repeat []))
                (assoc :nodes
                       (vec (repeat (get independent-counts
                                         :ordinary-core-node 0) {}))
                       :destructuring-product-nodes
                       (:product-node-preimages composed)
                       :binding-slots (:binding-slot-preimages composed)
                       :binding-extractions
                       (:binding-extraction-preimages composed)
                       :runtime-checks (:runtime-check-preimages composed)
                       :publication-events
                       (:publication-event-preimages composed)
                       :recur-slot-mappings
                       (:recur-slot-mapping-preimages composed)
                       :slot-extraction-transcript
                       (:slot-extraction-transcript-preimage composed)))
            independent-purpose-counts
            (merge
             (zipmap
              [:sh07-b51-implementation-semantic-id
               :sh07-b51-contract-semantic-id
               :sh07-b51-same-request-semantic-id
               :sh07-b51-predecessor-outcome-semantic-id
               :sh07-b51-predecessor-authority-id
               :sh07-b51-slot-extraction-transcript-id
               :sh07-b51-core-identity-id
               :sh07-b51-predecessor-observation-id
               :sh07-b51-provenance-binding-id
               :sh07-b51-independent-verifier-binding-id
               :sh07-b51-final-artifact-binding-id]
              (repeat 1))
             {:sh07-b51-v16-verification-semantic-id 0}
             (into {}
                   (map (fn [[record-kind purpose]]
                          [purpose (get independent-counts record-kind 0)]))
                   purpose-by-record-kind))
            count-plan
            (vec
             (mapcat
              (fn [purpose]
                (repeat
                 (get independent-purpose-counts purpose)
                 (cond-> {:purpose purpose}
                   (= purpose :sh07-b51-predecessor-outcome-semantic-id)
                   (assoc :preimage
                          {:outcome-kind :b51-vector-frontier-rejected})
                   (= purpose :sh07-b51-core-identity-id)
                   (assoc :preimage {:products products}))))
              purpose-order))]
        (is (= :accepted (:status expected)) [kind expected])
        (is (= :accepted (:status composed)) [kind composed])
        (is (= 1 (get independent-counts :module-transcript)) kind)
        (doseq [[record-kind purpose] purpose-by-record-kind]
          (let [independent (get independent-counts record-kind 0)]
            (is (= independent
                   (get replay-counts
                        (replay-count-key-by-record-kind record-kind)))
                [kind record-kind])
            (is (= independent
                   (invoke 'sh07-b51-expected-purpose-count
                           [purpose :b51-vector-frontier-rejected products]))
                [kind purpose])))
        (doseq [[record-kind output-field]
                output-field-by-record-kind]
          (is (= (get independent-counts record-kind 0)
                 (count (get composed output-field)))
              [kind record-kind output-field]))
        (is (= 1
               (invoke 'sh07-b51-expected-purpose-count
                       [:sh07-b51-slot-extraction-transcript-id
                        :b51-vector-frontier-rejected products])))
        (is (= (reduce + (vals independent-purpose-counts))
               (count count-plan))
            kind)
        ;; This independent census proves each count, but deliberately lacks
        ;; the exact Tier-3 record/request joins and authenticated base rows.
        (is (false? (invoke 'sh07-b51-success-plan-exact-cardinality?
                            [count-plan]))
            [kind :count-only-plan])
        (is (false? (invoke 'sh07-b51-success-plan-exact-cardinality?
                            [(vec (rest count-plan))]))
            kind)
        (is (= (set product-key-order) (set (keys products))))))))

(deftest sh07-b51-v18-static-product-batch-and-edge-contract-is-exact
  (is (exact-contract-oracle?
       product-key-order purpose-order dependency-counts))
  (is (= 33 (.indexOf product-key-order :destructuring-product-nodes)))
  (is (= 8 (- (count product-key-order) 33)))
  (is (= (range 19) (mapv #(get (zipmap purpose-order (range)) %) purpose-order)))
  (doseq [value (concat product-key-order purpose-order)]
    (is (some #{value} (tree-seq coll? seq @source-forms)) value))
  (doseq [[products purposes dependencies]
          [[(vec (butlast product-key-order)) purpose-order dependency-counts]
           [(conj product-key-order :unowned-product) purpose-order dependency-counts]
           [product-key-order (vec (reverse purpose-order)) dependency-counts]
           [product-key-order purpose-order
            (update dependency-counts
                    :sh07-b51-final-artifact-binding-id inc)]]]
    (is (false? (exact-contract-oracle? products purposes dependencies)))))

(deftest sh07-b51-v18-diagnostics-and-bounds-are-closed
  (is (= 8 (count bound-catalog)))
  (is (= 6 (count (filter #(= :predecessor-b47 (:owner %)) bound-catalog))))
  (is (= 2 (count (filter #(= :v18 (:owner %)) bound-catalog))))
  (is (= #{:module-slot-limit :module-binding-leaf-limit}
         (set (map :check (filter #(= :v18 (:owner %)) bound-catalog)))))
  (doseq [rule diagnostic-rules]
    (is (some #{rule} (tree-seq coll? seq @source-forms)) rule))
  (let [coordinate
        {:form-id nil :form-ordinal nil :fragment-ordinal nil :syntax-id nil
         :source-span nil :generated-origin-chain [] :core-node-id nil
         :owner-coordinate nil :slot-id nil :extraction-id nil
         :recur-form-id nil :recur-syntax-id nil :path nil}
        diagnostic
        (invoke 'sh07-b51-diagnostic-semantic
                [:duplicate-binding :duplicate-vector-binding-name coordinate
                 {:binding-name 'value} {:occurrences 2} []])]
    (is (= 29 (count diagnostic)))
    (is (= :gravity/sh07-b51-c6-diagnostic-v18 (:artifact diagnostic)))
    (is (= "L7-DUP-BINDING" (:rule diagnostic)))
    (is (= :sh07-b51-vector-destructuring-v18 (:lowering-rule diagnostic)))
    (is (true? (:fail-closed diagnostic)))
    (is (nil? (:form-id diagnostic)))
    (is (= [] (:generated-origin-chain diagnostic)))
    (is (nil? (:owner-coordinate diagnostic)))
    (is (not (contains? diagnostic :facts)))
    (is (not (contains? diagnostic :details)))
    (is (not (contains? diagnostic :source-path)))))

(deftest sh07-b51-v18-source-carries-authentic-branch-and-atomicity-vocabulary
  (let [scalars (set (tree-seq coll? seq @source-forms))]
    (doseq [token required-semantic-tokens]
      (is (contains? scalars token) token))
    (is (= required-semantic-tokens
           (set (filter required-semantic-tokens scalars))))
    (is (not= required-semantic-tokens
              (disj required-semantic-tokens :authenticated-external)))
    (is (not= required-semantic-tokens
              (conj required-semantic-tokens :host-authorized-external)))))

(deftest sh07-b51-v18-fresh-sh06-binding-rejects-forged-report-proof-analysis-and-module
  (let [{:keys [resolution report request] :as projection}
        (fixture-projection "nested-empty-wildcard-let")
        carrier {:authenticated-sh06-request request
                 :authenticated-sh06-verification-report report
                 :sh06-resolution-artifact resolution}
        forged-report
        (assoc-in carrier [:authenticated-sh06-verification-report :status]
                  :failed)
        forged-proof
        (assoc-in carrier [:sh06-resolution-artifact :capability-based-proof]
                  {})
        forged-analysis
        (assoc-in carrier
                  [:sh06-resolution-artifact :gravity-resolution-boundary
                   :resolved-analysis :binding-table]
                  [])
        forged-module
        (assoc-in carrier [:authenticated-sh06-request :module :profile]
                  :forged-profile)]
    (is (= :passed (:status report)))
    (is (= :accepted (:status resolution)))
    (is (= 15 (:schema-version request)))
    (is (true? (invoke 'sh07-b51-fresh-sh06-binding? [carrier])))
    (doseq [[label forged]
            [[:report forged-report]
             [:proof forged-proof]
             [:analysis forged-analysis]
             [:module forged-module]]]
      (testing (name label)
        (is (false? (invoke 'sh07-b51-fresh-sh06-binding? [forged])))))
    (is (= projection (fixture-projection "nested-empty-wildcard-let")))))

(deftest sh07-b51-v18-authorizes-local-sibling-and-import-relations-authentically
  (doseq [[basename expected-class expected-symbol]
          [["nested-empty-wildcard-let" :lexical 'head]
           ["sibling-namespace-external" :namespace 'sibling-pair]
           ["declared-import-external" :import 'shared/checksum]]]
    (testing basename
      (let [projection (fixture-projection basename)
            indexes (fixture-indexes projection)
            references
            (invoke 'sh07-b51-reference-occurrence-relations
                    [(:resolution projection) (:request projection) indexes])
            definitions
            (invoke 'sh07-b51-binding-definition-relations
                    [(:resolution projection) (:request projection) indexes])
            {:keys [authorization rows]}
            (resolution-classification-rows projection)
            matching
            (filterv #(and (= expected-class
                              (get-in % [:binding :binding-class]))
                           (= expected-symbol
                              (get-in % [:resolution :symbol])))
                     rows)]
        (is (= :accepted (:status references)) references)
        (is (= :accepted (:status definitions)) definitions)
        (is (= :accepted (:status authorization)) authorization)
        (is (seq matching) rows)
        (if (= expected-class :lexical)
          (is (every? #(= :owning-fragment-local (:classification %))
                      matching))
          (is (every? #(= :authenticated-external (:classification %))
                      matching)))))))

(deftest sh07-b51-v18-resolution-permutation-definition-and-fragment-forgeries-reject
  (let [{:keys [resolution request] :as projection}
        (fixture-projection "sibling-namespace-external")
        permuted-request
        (update request :resolution-table #(vec (reverse %)))
        permuted-indexes
        (invoke 'sh07-b51-index-request [permuted-request])
        permuted
        (invoke 'sh07-b51-reference-occurrence-relations
                [resolution permuted-request permuted-indexes])
        first-binding (first (:binding-table request))
        forged-definition-request
        (update request :binding-table
                (fn [bindings]
                  (assoc bindings 0
                         (assoc first-binding :definition-syntax-id
                                (sha \0)))))
        forged-definition-indexes
        (invoke 'sh07-b51-index-request [forged-definition-request])
        forged-definition
        (invoke 'sh07-b51-binding-definition-relations
                [resolution forged-definition-request
                 forged-definition-indexes])
        first-resolution (first (:resolution-table request))
        reference-id (:reference-syntax-id first-resolution)
        binding-id (:binding-id first-resolution)
        forged-membership-request
        (update request :fragment-manifest
                (fn [fragments]
                  (mapv
                   (fn [fragment]
                     (if (some #{reference-id}
                               (:resolution-reference-syntax-ids fragment))
                       (-> fragment
                           (update :local-binding-ids
                                   #(vec (remove #{binding-id} %)))
                           (update :external-binding-ids
                                   #(vec (remove #{binding-id} %))))
                       fragment))
                   fragments)))
        forged-membership-indexes
        (invoke 'sh07-b51-index-request [forged-membership-request])
        forged-membership
        (invoke 'sh07-b51-reference-occurrence-relations
                [resolution forged-membership-request
                 forged-membership-indexes])]
    (is (= :rejected (:status permuted)) permuted)
    (is (contains? #{:fragment-resolution-order
                     :raw-reference-resolution-join
                     :reference-symbol-substitution}
                   (:reason permuted)))
    (is (= :rejected (:status forged-definition)) forged-definition)
    (is (= :definition-syntax-projection (:reason forged-definition)))
    (is (= :rejected (:status forged-membership)) forged-membership)
    (is (= :fragment-binding-membership (:reason forged-membership)))
    (is (= :accepted
           (:status (:authorization
                     (resolution-classification-rows projection)))))))

(deftest sh07-b51-v18-owner-selection-requires-a-strict-definition-descendant
  (doseq [[basename expected-branch definition?]
          [["nested-empty-wildcard-let" :expanded-defn true]
           ["source-def-owner" :non-definition false]
           ["strict-nested-source-def-owner" :source-def true]
           ["non-definition-owner" :non-definition false]]]
    (testing basename
      (let [projection (fixture-projection basename)
            request (:request projection)
            indexes (fixture-indexes projection)
            enclosings (let-enclosing-forms request indexes)
            owner
            (when (= 1 (count enclosings))
              (invoke 'sh07-b51-owner-coordinate
                      [request indexes (first enclosings)]))
            coordinate (:coordinate owner)
            different-form
            (first (remove #(contains?
                            (set [(:root-form-id coordinate)
                                  (:def-form-id coordinate)
                                  (:fn-form-id coordinate)])
                            (:form-id %))
                           (:forms request)))
            different-form-id (:form-id different-form)
            different-syntax-id (:syntax-id different-form)]
        (is (= 1 (count enclosings)) enclosings)
        (is (= :accepted (:status owner)) owner)
        (is (= expected-branch (:branch owner)) owner)
        (is (= definition?
               (some? (:owning-definition-form-id coordinate))))
        (is (= definition?
               (some? (:owning-definition-syntax-id coordinate))))
        (is (= (:owning-definition-form-id coordinate)
               (:def-form-id coordinate)))
        (is (= (:owning-definition-syntax-id coordinate)
               (:def-syntax-id coordinate)))
        (is (true?
             (invoke 'sh07-b51-owner-coordinate-shape? [coordinate])))
        (when definition?
          (doseq [mutated
                  [(assoc coordinate :root-form-id different-form-id)
                   (assoc coordinate :root-syntax-id different-syntax-id)
                   (-> coordinate
                       (assoc :owning-definition-form-id different-form-id)
                       (assoc :def-form-id different-form-id))
                   (-> coordinate
                       (assoc :owning-definition-syntax-id
                              different-syntax-id)
                       (assoc :def-syntax-id different-syntax-id))]]
            (is (false?
                 (invoke 'sh07-b51-owner-coordinate-shape? [mutated]))
                mutated)))
        (when (= expected-branch :non-definition)
          (doseq [field [:owning-definition-form-id
                         :owning-definition-syntax-id
                         :def-form-id :def-syntax-id
                         :defn-input-syntax-id :fn-form-id :fn-syntax-id]]
            (is (false?
                 (invoke 'sh07-b51-owner-coordinate-shape?
                         [(assoc coordinate field
                                 (if (contains?
                                      #{:owning-definition-form-id
                                        :def-form-id :fn-form-id}
                                      field)
                                   different-form-id
                                   different-syntax-id))]))
                field)))
        (when (= expected-branch :source-def)
          (doseq [field [:defn-input-syntax-id :fn-form-id :fn-syntax-id]]
            (is (false?
                 (invoke 'sh07-b51-owner-coordinate-shape?
                         [(assoc coordinate field
                                 (if (= field :fn-form-id)
                                   different-form-id
                                   different-syntax-id))]))
                field)))
        (when (= expected-branch :expanded-defn)
          (is (some? (:defn-input-syntax-id coordinate)))
          (is (some? (:fn-form-id coordinate)))
          (is (some? (:fn-syntax-id coordinate)))
          (doseq [mutated
                  [(assoc coordinate :fn-form-id nil)
                   (assoc coordinate :fn-syntax-id nil)]]
            (is (false?
                 (invoke 'sh07-b51-owner-coordinate-shape? [mutated]))
                mutated)))
        (when (not= expected-branch :expanded-defn)
          (is (nil? (:defn-input-syntax-id coordinate)))
          (is (nil? (:fn-form-id coordinate)))
          (is (nil? (:fn-syntax-id coordinate))))))))

(deftest sh07-b51-v18-pattern-enumerator-admits-nested-empty-and-wildcard-slots
  (let [projection (fixture-projection "nested-empty-wildcard-let")
        request (:request projection)
        indexes (fixture-indexes projection)
        nested-form (unique-pattern-form request '[head [_ [tail]] []])
        enclosing (first (let-enclosing-forms request indexes))
        binding-vector
        (get-in indexes [:forms :by-primary
                         (second (:child-form-ids enclosing))])
        empty-form
        (get-in indexes [:forms :by-primary
                         (nth (:child-form-ids binding-vector) 2)])
        nested
        (invoke 'sh07-b51-enumerate-slot-pattern
                [request indexes
                 (unique-form-fragment request nested-form) nested-form])
        empty-pattern
        (invoke 'sh07-b51-enumerate-slot-pattern
                [request indexes
                 (unique-form-fragment request empty-form) empty-form])]
    (is (= :accepted (:status nested)) nested)
    (is (= :vector-pattern (:pattern-kind nested)))
    (is (= 7 (:node-count nested) (:extraction-count nested)))
    (is (= 3 (:terminal-count nested)))
    (is (= 2 (:leaf-count nested)))
    (is (= 4 (:vector-node-count nested)))
    (is (= 2 (:maximum-vector-depth nested)))
    (is (= 3 (:maximum-vector-width nested)))
    (is (= 3 (:maximum-path-length nested)))
    (is (= [:vector-node :binding-leaf :vector-node :wildcard-leaf
            :vector-node :binding-leaf :vector-node]
           (mapv :kind (:descriptors nested))))
    (is (= [[] [0] [1] [1 0] [1 1] [1 1 0] [2]]
           (mapv :path (:descriptors nested))))
    (is (= :accepted (:status empty-pattern)) empty-pattern)
    (is (= 1 (:node-count empty-pattern)))
    (is (zero? (:terminal-count empty-pattern)))
    (is (zero? (:leaf-count empty-pattern)))
    (is (= 1 (:vector-node-count empty-pattern)))
    (is (= 0 (:maximum-vector-width empty-pattern)))
    (is (= [] (:introduced-binding-ids empty-pattern)))))

(deftest sh07-b51-v18-simple-symbol-underscore-and-ampersand-remain-bindings
  (let [projection (fixture-projection "simple-symbol-parity")
        request (:request projection)
        indexes (fixture-indexes projection)]
    (doseq [name ['_ '&]]
      (let [binding
            (first (filter #(and (= name (:name %))
                                 (= :lexical (:binding-class %)))
                           (:binding-table request)))
            form
            (get-in indexes
                    [:forms :by-secondary (:definition-syntax-id binding)])
            fragment (unique-form-fragment request form)
            pattern
            (invoke 'sh07-b51-enumerate-slot-pattern
                    [request indexes fragment form])]
        (is binding name)
        (is form name)
        (is fragment name)
        (is (= :accepted (:status pattern)) pattern)
        (is (= :legacy-simple-symbol (:pattern-kind pattern)))
        (is (= 1 (:leaf-count pattern)))
        (is (= 1 (:terminal-count pattern)))
        (is (= [:binding-leaf] (mapv :kind (:descriptors pattern))))))))

(deftest sh07-b51-v18-pattern-enumerator-rejects-duplicate-rest-map-and-substitution
  (doseq [[basename pattern expected-reason]
          [["duplicate-vector-binding" '[value [value _] tail tail]
            :duplicate-vector-binding-name]
           ["unsupported-vector-rest" '[head & tail]
            :unsupported-vector-rest]
           ["unsupported-map-pattern" '{:value value}
            :unsupported-nested-pattern]]]
    (testing basename
      (let [projection (fixture-projection "rejected" basename)
            request (:request projection)
            indexes (fixture-indexes projection)
            pattern-form (unique-pattern-form request pattern)
            fragment (unique-form-fragment request pattern-form)
            local-pattern
            (g13-local-pattern-leaves request fragment pattern-form)
            result
            (invoke 'sh07-b51-enumerate-slot-pattern
                    [request indexes fragment pattern-form])]
        (is pattern-form)
        (is fragment)
        (is (= :rejected (:status result)) result)
        (is (= expected-reason (:reason result)) result)
        (is (= [] (:partial-extractions result)))
        (when (= expected-reason :duplicate-vector-binding-name)
          (is (map? (:first-duplicate result)))
          (is (map? (:later-duplicate result)))
          (is (not= (get-in result [:first-duplicate :path])
                    (get-in result [:later-duplicate :path])))
          ;; The nested `_` wildcard is authenticated but contributes no
          ;; binding leaf to the module-global resource budget.
          (is (= :accepted (:status local-pattern)) local-pattern)
          (is (not-any? #(= '_ (:binding-name %))
                        (:leaves local-pattern))
              local-pattern)))))
  (let [projection (fixture-projection "nested-empty-wildcard-let")
        request (:request projection)
        indexes (fixture-indexes projection)
        pattern-form (unique-pattern-form request '[head [_ [tail]] []])
        fragment (unique-form-fragment request pattern-form)
        forged (assoc pattern-form :syntax-id (sha \f))
        result
        (invoke 'sh07-b51-enumerate-slot-pattern
                [request indexes fragment forged])]
    (is (= :rejected (:status result)))
    (is (= :pattern-form-substitution (:reason result)))
    (is (= [] (:partial-extractions result)))))

(deftest sh07-b51-v18-module-resource-preflight-is-saturating-and-atomic
  (let [let-context
        (authenticated-preflight-slot "nested-empty-wildcard-let" 'let)
        loop-context
        (authenticated-preflight-slot
         "nested-empty-wildcard-loop-recur" 'loop)
        pattern (:pattern let-context)
        let-slot (:preflight-slot let-context)
        loop-slot (:preflight-slot loop-context)
        accepted
        (invoke 'sh07-b51-module-resource-preflight [[let-slot]])
        owner-masked
        (invoke 'sh07-b51-module-resource-preflight
                [(vec (repeat 1025 let-slot))])
        distributed
        (into (vec (repeat 512 let-slot)) (repeat 513 loop-slot))
        private-let-slots
        (invoke 'sh07-b51-module-resource-preflight
                [(vec (repeat 512 let-slot))])
        private-loop-slots
        (invoke 'sh07-b51-module-resource-preflight
                [(vec (repeat 513 loop-slot))])
        module-slot-overflow
        (invoke 'sh07-b51-module-resource-preflight [distributed])
        binding-descriptor
        (first (filter #(= :binding-leaf (:kind %))
                       (:descriptors pattern)))
        augmented-pattern
        (-> pattern
            (update :descriptors conj
                    (assoc binding-descriptor
                           :pattern-node-ordinal (:node-count pattern)
                           :terminal-ordinal (:terminal-count pattern)
                           :leaf-ordinal (:leaf-count pattern)
                           :path [999]
                           :binding-id (sha \e)))
            (update :introduced-binding-ids conj (sha \e))
            (update :node-count inc)
            (update :extraction-count inc)
            (update :terminal-count inc)
            (update :leaf-count inc))
        leaf-slots
        (into
         (vec (cons (assoc let-slot :pattern augmented-pattern)
                    (repeat 511 let-slot)))
         (repeat 512 loop-slot))
        private-let-leaves
        (invoke 'sh07-b51-module-resource-preflight
                [(subvec leaf-slots 0 512)])
        private-loop-leaves
        (invoke 'sh07-b51-module-resource-preflight
                [(subvec leaf-slots 512 1024)])
        module-leaf-overflow
        (invoke 'sh07-b51-module-resource-preflight [leaf-slots])
        slot-offender (nth distributed 1024)
        leaf-offender-slot (nth leaf-slots 1023)
        leaf-offender-descriptor
        (last (filter #(= :binding-leaf (:kind %))
                      (get-in leaf-offender-slot [:pattern :descriptors])))]
    (is (= :accepted (get-in let-context [:owner-result :status])))
    (is (= :accepted (get-in loop-context [:owner-result :status])))
    (is (= :accepted (get-in let-context [:pattern :status])))
    (is (= :accepted (get-in loop-context [:pattern :status])))
    (is (= :accepted (:status accepted)) accepted)
    (is (= 1 (:slot-count accepted)))
    (is (= 2 (:binding-leaf-count accepted)))
    (is (= :rejected (:status owner-masked)) owner-masked)
    (is (= :predecessor-bound-mask (:reason owner-masked)))
    (is (= :owner-slot-limit (:check owner-masked)))
    (is (= [] (:partial-products owner-masked)))
    ;; A per-owner/private preflight is not module admission: both chunks pass,
    ;; while their exact ordered concatenation crosses the module-global bound.
    (is (= :accepted (:status private-let-slots)) private-let-slots)
    (is (= :accepted (:status private-loop-slots)) private-loop-slots)
    (is (= :rejected (:status module-slot-overflow)) module-slot-overflow)
    (is (= :module-slot-limit-exceeded (:reason module-slot-overflow)))
    (is (= {:unit :module-slots :saturating-count 1025}
           (:observed module-slot-overflow)))
    (is (= {:slot-ordinal 1025
            :pattern-root (get-in slot-offender [:pattern :pattern-root])
            :path []
            :fragment (:fragment slot-offender)
            :owner-coordinate (:owner-coordinate slot-offender)}
           (:offender module-slot-overflow)))
    (is (= [] (:partial-products module-slot-overflow)))
    (is (= :accepted (:status private-let-leaves)) private-let-leaves)
    (is (= :accepted (:status private-loop-leaves)) private-loop-leaves)
    (is (= :rejected (:status module-leaf-overflow)) module-leaf-overflow)
    (is (= :module-binding-leaf-limit-exceeded
           (:reason module-leaf-overflow)))
    (is (= {:unit :module-binding-leaves :saturating-count 2049}
           (:observed module-leaf-overflow)))
    (is (= {:leaf-ordinal 2049
            :descriptor leaf-offender-descriptor
            :pattern-root
            (get-in leaf-offender-slot [:pattern :pattern-root])
            :path (:path leaf-offender-descriptor)
            :fragment (:fragment leaf-offender-slot)
            :owner-coordinate (:owner-coordinate leaf-offender-slot)}
           (:offender module-leaf-overflow)))
    (is (= [] (:partial-products module-leaf-overflow)))))

(deftest sh07-b51-v18-module-collector-requires-authenticated-canonical-form-preorder
  (let [{:keys [resolution report request] :as let-projection}
        (fixture-projection "nested-empty-wildcard-let")
        loop-projection
        (fixture-projection "nested-empty-wildcard-loop-recur")
        indexes (fixture-indexes let-projection)
        preorder
        (invoke 'sh07-b51-authenticated-form-preorder [request indexes])
        collected-let
        (invoke 'sh07-b51-collect-module-owner-slots [request])
        reordered-request (update request :forms #(vec (reverse %)))
        reordered-indexes
        (invoke 'sh07-b51-index-request [reordered-request])
        reordered-carrier
        {:authenticated-sh06-request reordered-request
         :authenticated-sh06-verification-report report
         :sh06-resolution-artifact resolution}
        reordered-preorder
        (invoke 'sh07-b51-authenticated-form-preorder
                [reordered-request reordered-indexes])
        reordered-let
        (invoke 'sh07-b51-collect-module-owner-slots [reordered-request])
        loop-request (:request loop-projection)
        collected-loop
        (invoke 'sh07-b51-collect-module-owner-slots [loop-request])
        reordered-loop
        (invoke 'sh07-b51-collect-module-owner-slots
                [(update loop-request :forms #(vec (reverse %)))])
        let-slot
        (first (invoke 'sh07-b51-owner-slot-preflight-records
                       [(:slots collected-let)]))
        loop-slot
        (first (invoke 'sh07-b51-owner-slot-preflight-records
                       [(:slots collected-loop)]))
        reordered-let-slot
        (first (invoke 'sh07-b51-owner-slot-preflight-records
                       [(:slots reordered-let)]))
        reordered-loop-slot
        (first (invoke 'sh07-b51-owner-slot-preflight-records
                       [(:slots reordered-loop)]))
        slot-overflow
        (into (vec (repeat 512 let-slot)) (repeat 513 loop-slot))
        reordered-slot-overflow
        (into (vec (repeat 512 reordered-let-slot))
              (repeat 513 reordered-loop-slot))
        canonical-slot-rejection
        (invoke 'sh07-b51-module-resource-preflight [slot-overflow])
        reordered-slot-rejection
        (invoke 'sh07-b51-module-resource-preflight
                [reordered-slot-overflow])
        binding-descriptor
        (first (filter #(= :binding-leaf (:kind %))
                       (get-in let-slot [:pattern :descriptors])))
        augmented-pattern
        (-> (:pattern let-slot)
            (update :descriptors conj
                    (assoc binding-descriptor
                           :pattern-node-ordinal
                           (get-in let-slot [:pattern :node-count])
                           :terminal-ordinal
                           (get-in let-slot [:pattern :terminal-count])
                           :leaf-ordinal
                           (get-in let-slot [:pattern :leaf-count])
                           :path [999]
                           :binding-id (sha \e)))
            (update :introduced-binding-ids conj (sha \e))
            (update :node-count inc)
            (update :extraction-count inc)
            (update :terminal-count inc)
            (update :leaf-count inc))
        leaf-overflow
        (into
         (vec (cons (assoc let-slot :pattern augmented-pattern)
                    (repeat 511 let-slot)))
         (repeat 512 loop-slot))
        reordered-leaf-overflow
        (into
         (vec (cons (assoc reordered-let-slot :pattern augmented-pattern)
                    (repeat 511 reordered-let-slot)))
         (repeat 512 reordered-loop-slot))
        canonical-leaf-rejection
        (invoke 'sh07-b51-module-resource-preflight [leaf-overflow])
        reordered-leaf-rejection
        (invoke 'sh07-b51-module-resource-preflight
                [reordered-leaf-overflow])]
    (is (= :accepted (:status preorder)) preorder)
    (is (= :accepted (:status collected-let)) collected-let)
    (is (= (:forms preorder) (:authenticated-form-order collected-let)))
    ;; Form order is not covered by the SH06 freshness predicate. The module
    ;; collector independently derives root/assembly/child preorder.
    (is (true? (invoke 'sh07-b51-fresh-sh06-binding?
                       [reordered-carrier])))
    (is (= :accepted (:status reordered-preorder)) reordered-preorder)
    (is (= (:forms preorder) (:forms reordered-preorder)))
    (is (= collected-let reordered-let))
    (is (= collected-loop reordered-loop))
    (is (= canonical-slot-rejection reordered-slot-rejection))
    (is (= 1025 (get-in canonical-slot-rejection
                         [:offender :slot-ordinal])))
    (is (= canonical-leaf-rejection reordered-leaf-rejection))
    (is (= 2049 (get-in canonical-leaf-rejection
                         [:offender :leaf-ordinal])))))

(deftest sh07-b51-v18-slot-coordinate-and-preimage-bind-authentic-owner-pairs
  (let [projection (fixture-projection "nested-empty-wildcard-let")
        request (:request projection)
        indexes (fixture-indexes projection)
        enclosing (first (let-enclosing-forms request indexes))
        owner-result
        (invoke 'sh07-b51-owner-coordinate [request indexes enclosing])
        owner (:coordinate owner-result)
        binding-vector
        (get-in indexes [:forms :by-primary
                         (second (:child-form-ids enclosing))])
        pattern
        (get-in indexes [:forms :by-primary
                         (first (:child-form-ids binding-vector))])
        initializer
        (get-in indexes [:forms :by-primary
                         (second (:child-form-ids binding-vector))])
        different-form
        (first (remove #(contains? #{(:form-id enclosing)
                                     (:form-id pattern)
                                     (:form-id initializer)}
                                   (:form-id %))
                       (:forms request)))
        coordinate-result
        (invoke 'sh07-b51-build-slot-coordinate
                [owner 0 pattern initializer])
        coordinate (:slot-coordinate coordinate-result)
        preimage
        {:domain :gravity/sh07-b51-binding-slot-id-preimage-v18
         :schema-version 18
         :predecessor-authority-id
         {:artifact :gravity/sh07-internal-digest-reference
          :schema-version 1 :ordinal 5 :authority :sh07-digest-resolver}
         :global-slot-ordinal 0
         :owner-kind :let
         :owner-form-id (:form-id enclosing)
         :owner-syntax-id (:syntax-id enclosing)
         :owner-core-node-id
         {:artifact :gravity/sh07-internal-digest-reference
          :schema-version 1 :ordinal 6 :authority :sh07-digest-resolver}
         :owner-coordinate owner
         :owning-definition-form-id (:owning-definition-form-id owner)
         :owning-definition-syntax-id (:owning-definition-syntax-id owner)
         :slot-coordinate coordinate
         :slot-ordinal 0
         :pattern-form-id (:form-id pattern)
         :pattern-syntax-id (:syntax-id pattern)
         :pattern-scope-id (:scope-id pattern)
         :initializer-form-id (:form-id initializer)
         :initializer-syntax-id (:syntax-id initializer)
         :initializer-scope-id (:scope-id initializer)
         :initializer-core-node-id
         {:artifact :gravity/sh07-internal-digest-reference
          :schema-version 1 :ordinal 7 :authority :sh07-digest-resolver}
         :source-origin-chain (:generated-origin enclosing)
         :source-generated-origin (:generated-origin pattern)}
        built (invoke 'sh07-b51-build-binding-slot-preimage [preimage])]
    (is (= :accepted (:status owner-result)) owner-result)
    (is (= :accepted (:status coordinate-result)) coordinate-result)
    (is (= owner (:owner-coordinate coordinate)))
    (is (= (:owning-definition-form-id owner)
           (:owning-definition-form-id preimage)))
    (is (= (:owning-definition-syntax-id owner)
           (:owning-definition-syntax-id preimage)))
    (is (= :accepted (:status built)) built)
    (is (= preimage (:preimage built)))
    (doseq [mutated
            [(assoc preimage :global-slot-ordinal -1)
             (assoc preimage :owning-definition-form-id nil)
             (assoc preimage :owning-definition-form-id
                    (:form-id different-form))
             (assoc preimage :slot-ordinal 1)
             (assoc-in preimage [:slot-coordinate :pattern-form-id]
                       (sha \d))
             (assoc-in preimage [:owner-coordinate :root-form-id]
                       (:form-id different-form))
             (-> preimage
                 (assoc :owning-definition-form-id
                        (:form-id different-form))
                 (assoc-in [:owner-coordinate :owning-definition-form-id]
                           (:form-id different-form))
                 (assoc-in [:owner-coordinate :def-form-id]
                           (:form-id different-form)))
             (assoc preimage :extra :forbidden)]]
      (let [result
            (invoke 'sh07-b51-build-binding-slot-preimage [mutated])]
        (is (= :rejected (:status result)) result)
        (is (= :binding-slot-preimage-shape (:reason result)))
        (is (= [] (:partial-products result)))))))

(deftest sh07-b51-v18-extraction-variants-preserve-path-width-binding-and-owner
  (let [{:keys [indexes enclosing owner pattern-form initializer fragment
                pattern slot-coordinate]}
        (nested-slot-context)
        descriptors
        [(first (:descriptors pattern))
         (first (filter #(= :binding-leaf (:kind %))
                        (:descriptors pattern)))
         (first (filter #(= :wildcard-leaf (:kind %))
                        (:descriptors pattern)))]]
    (doseq [descriptor descriptors]
      (let [form (get-in indexes [:forms :by-primary (:form-id descriptor)])
            preimage
            {:domain :gravity/sh07-b51-binding-extraction-id-preimage-v18
             :schema-version 18
             :predecessor-authority-id
             {:artifact :gravity/sh07-internal-digest-reference
              :schema-version 1 :ordinal 5
              :authority :sh07-digest-resolver}
             :slot-id
             {:artifact :gravity/sh07-internal-digest-reference
              :schema-version 1 :ordinal 8
              :authority :sh07-digest-resolver}
             :slot-coordinate slot-coordinate
             :global-ordinal (:pattern-node-ordinal descriptor)
             :global-slot-ordinal 0
             :slot-ordinal 0
             :pattern-node-ordinal (:pattern-node-ordinal descriptor)
             :parent-pattern-node-ordinal
             (:parent-pattern-node-ordinal descriptor)
             :terminal-ordinal (:terminal-ordinal descriptor)
             :leaf-ordinal (:leaf-ordinal descriptor)
             :kind (:kind descriptor)
             :path (:path descriptor)
             :form-id (:form-id descriptor)
             :syntax-id (:syntax-id descriptor)
             :scope-id (:scope-id descriptor)
             :enclosing-form-id (:form-id enclosing)
             :enclosing-syntax-id (:syntax-id enclosing)
             :pattern-form-id (:form-id pattern-form)
             :pattern-syntax-id (:syntax-id pattern-form)
             :initializer-form-id (:form-id initializer)
             :initializer-syntax-id (:syntax-id initializer)
             :expected-width (:expected-width descriptor)
             :binding-id (:binding-id descriptor)
             :binding-name (:binding-name descriptor)
             :definition-form-id (:definition-form-id descriptor)
             :definition-syntax-id (:definition-syntax-id descriptor)
             :binding-scope-id (:binding-scope-id descriptor)
             :fragment-id (:fragment-id fragment)
             :fragment-ordinal (:ordinal fragment)
             :owning-root-form-id (:root-form-id owner)
             :owning-root-syntax-id (:root-syntax-id owner)
             :owning-definition-form-id
             (:owning-definition-form-id owner)
             :owning-definition-syntax-id
             (:owning-definition-syntax-id owner)
             :owner-coordinate owner
             :defn-input-syntax-id (:defn-input-syntax-id owner)
             :fn-form-id (:fn-form-id owner)
             :fn-syntax-id (:fn-syntax-id owner)
             :source-origin-chain (:generated-origin enclosing)
             :source-generated-origin (:generated-origin form)}
            built
            (invoke 'sh07-b51-build-binding-extraction-preimage [preimage])]
        (is (= :accepted (:status built)) [(:kind descriptor) built])
        (is (= preimage (:preimage built)))
        (doseq [mutated
                (case (:kind descriptor)
                  :vector-node
                  [(assoc preimage :terminal-ordinal 0)
                   (assoc preimage :binding-id (sha \a))]
                  :binding-leaf
                  [(assoc preimage :expected-width 0)
                   (assoc preimage :binding-name nil)
                   (assoc preimage :leaf-ordinal nil)]
                  :wildcard-leaf
                  [(assoc preimage :leaf-ordinal 0)
                   (assoc preimage :binding-id (sha \b))
                   (assoc preimage :parent-pattern-node-ordinal nil)])]
          (let [rejected
                (invoke 'sh07-b51-build-binding-extraction-preimage
                        [mutated])]
            (is (= :rejected (:status rejected)) rejected)
            (is (= :binding-extraction-preimage-shape
                   (:reason rejected)))
            (is (= [] (:partial-products rejected)))))))))

(deftest sh07-b51-v18-runtime-vector-check-preimage-is-exact-and-fail-closed
  (let [{:keys [indexes enclosing owner fragment pattern slot-coordinate]
         _pattern-form :pattern-form _initializer :initializer}
        (nested-slot-context)
        descriptor (first (:descriptors pattern))
        form (get-in indexes [:forms :by-primary (:form-id descriptor)])
        preimage
        {:domain :gravity/sh07-b51-runtime-check-id-preimage-v18
         :schema-version 18
         :global-runtime-check-ordinal 0
         :predecessor-authority-id
         {:artifact :gravity/sh07-internal-digest-reference
          :schema-version 1 :ordinal 5 :authority :sh07-digest-resolver}
         :fragment-id (:fragment-id fragment)
         :fragment-ordinal (:ordinal fragment)
         :root-form-id (:root-form-id owner)
         :root-syntax-id (:root-syntax-id owner)
         :owner-kind :let
         :owner-form-id (:form-id enclosing)
         :owner-syntax-id (:syntax-id enclosing)
         :owner-coordinate owner
         :slot-coordinate slot-coordinate
         :site-kind :init
         :recur-coordinate nil
         :slot-id
         {:artifact :gravity/sh07-internal-digest-reference
          :schema-version 1 :ordinal 8 :authority :sh07-digest-resolver}
         :extraction-id
         {:artifact :gravity/sh07-internal-digest-reference
          :schema-version 1 :ordinal 9 :authority :sh07-digest-resolver}
         :path (:path descriptor)
         :check-kind :exact-vector-shape
         :expected-kind :vector
         :expected-width (:expected-width descriptor)
         :check-order 0
         :guarded-publication-ordinal 0
         :guarded-event-kind :slot-atomic-publication
         :failure-rule "L7-PATTERN-TYPE"
         :reasons [:vector-required :vector-width-mismatch]
         :guard-policy :publish-after-all-checks
         :fail-closed true
         :source-origin-chain (:generated-origin enclosing)
         :source-generated-origin (:generated-origin form)}
        built (invoke 'sh07-b51-build-runtime-check-preimage [preimage])]
    (is (= :accepted (:status built)) built)
    (is (= preimage (:preimage built)))
    (is (= 3 (:expected-width preimage)))
    (doseq [mutated
            [(assoc preimage :expected-kind :sequential)
             (assoc preimage :expected-width -1)
             (assoc preimage :reasons
                    [:vector-width-mismatch :vector-required])
             (assoc preimage :guarded-event-kind :loop-atomic-transfer)
             (assoc preimage :guard-policy :publish-incrementally)
             (assoc preimage :fail-closed false)
             (assoc preimage :path [-1])
             (assoc preimage :recur-coordinate {})]]
      (let [rejected (invoke 'sh07-b51-build-runtime-check-preimage
                             [mutated])]
        (is (= :rejected (:status rejected)) rejected)
        (is (= :runtime-check-preimage-shape (:reason rejected)))
        (is (= [] (:partial-products rejected)))))))

(deftest sh07-b51-v18-recur-coordinate-arity-checks-and-atomic-slot-mapping-are-exact
  (let [context (loop-recur-context)
        foundation (:foundation context)
        authenticated-recur (:authenticated-recur context)
        recur-layout (mechanical-recur-reference-layout foundation)
        mapping-preimage
        (mechanical-recur-mapping-preimage context recur-layout)
        built
        (invoke 'sh07-b51-build-recur-slot-mapping-preimage
                [mapping-preimage])
        slot-ids (:slot-ids mapping-preimage)
        swapped-slot-ids
        (assoc slot-ids 0 (get slot-ids 1) 1 (get slot-ids 0))
        mismatched-recur
        (assoc-in mapping-preimage [:recur-coordinate :recur-syntax-id]
                  (:syntax-id (:loop-form context)))]
    (is (= :accepted (:status foundation)) foundation)
    (is (= :loop (:owner-kind foundation)))
    (is (= 3 (:slot-count foundation)))
    (is (= :accepted (:status authenticated-recur)) authenticated-recur)
    (is (= {:recur-form-id (get-in context [:recur-form :form-id])
            :recur-syntax-id (get-in context [:recur-form :syntax-id])
            :recur-core-node-id (internal-reference 700)}
           (:recur-coordinate authenticated-recur)))
    (is (= 3 (count (:argument-forms authenticated-recur))))
    (is (= :accepted (:status built)) built)
    (is (= mapping-preimage (:preimage built)))
    (is (= [0 1 2] (:slot-ordinals mapping-preimage)))
    (is (= [0 1 2] (:argument-ordinals mapping-preimage)))
    (is (= [0 1 2] (:evaluation-order mapping-preimage)))
    (is (= (mapv :slot-coordinate (:slot-plans foundation))
           (:slot-coordinates mapping-preimage)))
    (is (= :simultaneous-after-all-checks
           (:commit-policy mapping-preimage)))
    (is (true? (:all-or-none mapping-preimage)))
    (is (true? (:tail-only mapping-preimage)))
    (doseq [mutated
            [(assoc mapping-preimage :recur-coordinate nil)
             mismatched-recur
             (assoc mapping-preimage :slot-ids (pop slot-ids))
             (assoc mapping-preimage :slot-ids
                    (assoc slot-ids 1 (first slot-ids)))
             ;; Authentic slot references and coordinates in the wrong order
             ;; must fail through their ordinal alignment, not uniqueness alone.
             (assoc mapping-preimage :slot-ids swapped-slot-ids)
             (assoc mapping-preimage :slot-coordinates
                    (vec (reverse (:slot-coordinates mapping-preimage))))
             (assoc mapping-preimage :slot-ordinals [0 2 1])
             (assoc mapping-preimage :argument-ordinals [0 2 1])
             (assoc mapping-preimage :evaluation-order [1 0 2])
             (assoc mapping-preimage :argument-count 2)
             (assoc mapping-preimage :commit-policy :incremental)
             (assoc mapping-preimage :all-or-none false)
             (assoc mapping-preimage :tail-only false)
             (dissoc mapping-preimage :slot-ids)
             (assoc mapping-preimage :extra :forbidden)]]
      (let [rejected
            (invoke 'sh07-b51-build-recur-slot-mapping-preimage [mutated])]
        (is (= :rejected (:status rejected)) rejected)
        (is (= :recur-slot-mapping-preimage-shape (:reason rejected)))
        (is (= [] (:partial-products rejected)))))))

(deftest sh07-b51-v18-ascending-foreign-digest-references-do-not-bind-slot-mappings
  (let [context (loop-recur-context)
        recur-layout
        (mechanical-recur-reference-layout (:foundation context))
        mapping-preimage
        (mechanical-recur-mapping-preimage context recur-layout)
        publication-preimage
        (mechanical-loop-publication-preimage
         context recur-layout mapping-preimage)
        request
        (fn [ordinal purpose preimage batch batch-rank]
          {:ordinal ordinal
           :tier 2
           :subtier nil
           :subtier-ordinal nil
           :batch batch
           :batch-rank batch-rank
           :batch-ordinal 0
           :purpose purpose
           :preimage preimage})
        genuine-request
        (request 0 :sh07-b51-recur-slot-mapping-id mapping-preimage
                 :t2-recur-mapping 12)
        genuine
        (invoke 'sh07-b51-authenticate-internal-digest-reference
                [(internal-reference 0) [genuine-request]
                 :sh07-b51-recur-slot-mapping-id mapping-preimage])
        foreign-requests
        [(request 0 :sh07-b51-recur-slot-mapping-id
                  (assoc mapping-preimage
                         :global-recur-mapping-ordinal 1)
                  :t2-recur-mapping 12)
         (request 1 :sh07-b51-publication-event-id publication-preimage
                  :t2-publication 11)]
        wrong-preimage
        (invoke 'sh07-b51-authenticate-internal-digest-reference
                [(internal-reference 0) foreign-requests
                 :sh07-b51-recur-slot-mapping-id mapping-preimage])
        wrong-purpose
        (invoke 'sh07-b51-authenticate-internal-digest-reference
                [(internal-reference 1) foreign-requests
                 :sh07-b51-recur-slot-mapping-id mapping-preimage])
        resolved-digest-instead-of-reference
        (invoke 'sh07-b51-authenticate-internal-digest-reference
                [(sha \f) [genuine-request]
                 :sh07-b51-recur-slot-mapping-id mapping-preimage])]
    (is (= :accepted (:status genuine)) genuine)
    (is (= 0 (:ordinal genuine)))
    (is (= [(internal-reference 0) (internal-reference 1)]
           (sort-by :ordinal
                    [(internal-reference 1) (internal-reference 0)])))
    (is (= :rejected (:status wrong-preimage)) wrong-preimage)
    (is (= :digest-reference-preimage-substitution
           (:reason wrong-preimage)))
    (is (= [] (:partial-products wrong-preimage)))
    (is (= :rejected (:status wrong-purpose)) wrong-purpose)
    (is (= :digest-reference-purpose-substitution
           (:reason wrong-purpose)))
    (is (= [] (:partial-products wrong-purpose)))
    (is (= :rejected (:status resolved-digest-instead-of-reference))
        resolved-digest-instead-of-reference)
    (is (= :internal-digest-reference
           (:reason resolved-digest-instead-of-reference)))
    (is (= [] (:partial-products resolved-digest-instead-of-reference)))))

(deftest sh07-b51-v18-recur-checks-guard-one-loop-transfer-before-publication
  (let [context (loop-recur-context)
        foundation (:foundation context)
        recur-layout (mechanical-recur-reference-layout foundation)
        mapping-preimage
        (mechanical-recur-mapping-preimage context recur-layout)
        initial-check
        (first (mapcat :runtime-check-preimages
                       (:slot-plans foundation)))
        recur-check
        (-> initial-check
            (assoc :site-kind :recur)
            (assoc :recur-coordinate (:recur-coordinate mapping-preimage))
            (assoc :guarded-publication-ordinal
                   (:global-publication-ordinal recur-layout))
            (assoc :guarded-event-kind :loop-atomic-transfer))
        built (invoke 'sh07-b51-build-runtime-check-preimage [recur-check])]
    (is initial-check)
    (is (= :accepted (:status built)) built)
    (is (= :recur (get-in built [:preimage :site-kind])))
    (is (= :loop-atomic-transfer
           (get-in built [:preimage :guarded-event-kind])))
    (is (= (:recur-coordinate mapping-preimage)
           (get-in built [:preimage :recur-coordinate])))
    (doseq [mutated
            [(assoc recur-check :recur-coordinate nil)
             (assoc recur-check :guarded-event-kind
                    :slot-atomic-publication)
             (assoc recur-check :site-kind :init)
             (assoc recur-check :guard-policy :publish-incrementally)
             (assoc recur-check :fail-closed false)]]
      (let [rejected
            (invoke 'sh07-b51-build-runtime-check-preimage [mutated])]
        (is (= :rejected (:status rejected)) rejected)
        (is (= :runtime-check-preimage-shape (:reason rejected)))
        (is (= [] (:partial-products rejected)))))))

(deftest sh07-b51-v18-loop-publication-is-one-atomic-event-and-zero-slots-is-a-vector
  (let [context (loop-recur-context)
        foundation (:foundation context)
        recur-layout (mechanical-recur-reference-layout foundation)
        mapping-preimage
        (mechanical-recur-mapping-preimage context recur-layout)
        publication
        (mechanical-loop-publication-preimage
         context recur-layout mapping-preimage)
        built
        (invoke 'sh07-b51-build-publication-event-preimage [publication])
        zero-slot
        (-> publication
            (assoc :slot-coordinates [])
            (assoc :slot-ids [])
            (assoc :introduced-binding-id-vectors [])
            (assoc :projection-id-vectors [])
            (assoc :required-check-id-vectors [])
            (assoc :product-node-id-vectors [])
            (assoc :visible-before-binding-id-vectors [])
            (assoc :visible-after-binding-id-vectors [])
            (assoc :publish-none true))
        built-zero
        (invoke 'sh07-b51-build-publication-event-preimage [zero-slot])]
    (is (= :accepted (:status built)) built)
    (is (= :loop-atomic-transfer (get-in built [:preimage :event-kind])))
    (is (nil? (get-in built [:preimage :slot-id])))
    (is (nil? (get-in built [:preimage :slot-coordinate])))
    (is (= (:recur-coordinate mapping-preimage)
           (get-in built [:preimage :recur-coordinate])))
    (is (true? (get-in built [:preimage :all-or-none])))
    (is (false? (get-in built [:preimage :publish-none])))
    (is (= :accepted (:status built-zero)) built-zero)
    (is (= [] (get-in built-zero [:preimage :slot-coordinates])))
    (doseq [mutated
            [(assoc zero-slot :slot-coordinates nil)
             (assoc publication :all-or-none false)
             (assoc publication :commit-order -1)
             (assoc publication :slot-id (first (:slot-ids publication)))
             (assoc publication :slot-coordinate
                    (first (:slot-coordinates publication)))
             (assoc publication :publish-none true)
             (assoc publication :event-kind :slot-atomic-publication)
             (dissoc publication :recur-coordinate)]]
      (let [rejected
            (invoke 'sh07-b51-build-publication-event-preimage [mutated])]
        (is (= :rejected (:status rejected)) rejected)
        (is (= :publication-event-preimage-shape (:reason rejected)))
        (is (= [] (:partial-products rejected)))))))

(deftest sh07-b51-v18-recur-arity-mismatch-rejects-without-partial-products
  (let [context
        (loop-recur-context "rejected" "recur-arity-mismatch")
        rejected (:authenticated-recur context)]
    (is (= :accepted (get-in context [:foundation :status]))
        (:foundation context))
    (is (= :rejected (:status rejected)) rejected)
    (is (= :recur-arity-mismatch (:reason rejected)))
    (is (= 2 (:expected rejected)))
    (is (= 1 (:observed rejected)))
    (is (= [] (:partial-products rejected)))))

(deftest sh07-b51-v18-new-recur-and-no-form-negative-fixtures-hit-lower-seams
  (let [missing-projection
        (fixture-projection "rejected" "missing-recur-target")
        missing-request (:request missing-projection)
        missing-indexes (fixture-indexes missing-projection)
        missing-recur
        (first (operator-enclosing-forms
                missing-request missing-indexes 'recur))
        missing-fragment
        (first (filter #(some #{(:form-id missing-recur)} (:form-ids %))
                       (:fragment-manifest missing-request)))
        missing-target
        (invoke 'sh07-b51-nearest-recur-target
                [missing-request missing-indexes missing-fragment
                 missing-recur])
        non-tail-context
        (loop-recur-context "rejected" "recur-not-tail")
        non-tail (:authenticated-recur non-tail-context)
        no-form-projection
        (fixture-projection
         "rejected" "ordered-no-form-unsupported-vector-rest")
        no-form-request (:request no-form-projection)
        no-form-indexes (fixture-indexes no-form-projection)
        rest-pattern
        (unique-pattern-form no-form-request '[head & tail])
        rest-fragment (unique-form-fragment no-form-request rest-pattern)
        rest-stage
        (invoke 'sh07-b51-enumerate-slot-pattern
                [no-form-request no-form-indexes rest-fragment rest-pattern])
        form-syntax-ids (set (map :syntax-id (:forms no-form-request)))
        no-form-bindings
        (filterv #(contains? #{'external-first 'external-second}
                             (:name %))
                 (:binding-table no-form-request))]
    (is (= :rejected (:status missing-target)) missing-target)
    (is (= :missing-recur-target (:reason missing-target)) missing-target)
    (is (= [] (:partial-products missing-target)) missing-target)
    (is (= :rejected (:status non-tail)) non-tail)
    (is (= :recur-not-tail (:reason non-tail)) non-tail)
    (is (= [] (:partial-products non-tail)) non-tail)
    (is (= :rejected (:status rest-stage)) rest-stage)
    (is (= :unsupported-vector-rest (:reason rest-stage)) rest-stage)
    (is (= [] (:partial-products rest-stage)) rest-stage)
    ;; This proves only authentic SH06 request composition and ordered no-form
    ;; candidates.  It deliberately does not construct or claim an authentic
    ;; B47 raw observation for the public Root1/Root8 boundary.
    (is (= ['external-first 'external-second]
           (mapv :name no-form-bindings))
        no-form-bindings)
    (is (every? #(= :namespace (:binding-class %)) no-form-bindings)
        no-form-bindings)
    (is (every? #(not (contains? form-syntax-ids
                                 (:definition-syntax-id %)))
                no-form-bindings)
        no-form-bindings)))

(deftest sh07-b51-v18-occurrence-applicability-closes-product-event-matrix
  (let [spec
        (fn [product-kind event-kind]
          {:record-kind :supplemental-product
           :product-kind product-kind
           :event-kind event-kind
           :form-id (sha \a)
           :event-form-id (sha \c)
           :owner-form-id (sha \b)
           :form-occurrence-path nil
           :event-occurrence-path nil
           :owner-occurrence-path nil
           :core-postorder-index nil
           :slot-ordinal
           (if (contains? #{:recur-argument :recur-commit} product-kind)
             nil 0)
           :pattern-preorder-index
           (if (contains? #{:vector-check :terminal-projection}
                          product-kind)
             0 nil)
           :local-use-index 0})]
    (doseq [[product-kind event-kind]
            [[:slot-initializer :initializer]
             [:vector-check :initializer]
             [:vector-check :recur]
             [:terminal-projection :initializer]
             [:terminal-projection :recur]
             [:slot-publication :initializer]
             [:recur-argument :recur]
             [:recur-commit :recur]]]
      (let [candidate (spec product-kind event-kind)]
        (is (true? (invoke 'sh07-b51-occurrence-spec-applicable?
                           [candidate]))
            candidate)))
    (doseq [[product-kind event-kind]
            [[:slot-initializer :recur]
             [:slot-publication :recur]
             [:recur-argument :initializer]
             [:recur-commit :initializer]]]
      (let [candidate (spec product-kind event-kind)]
        (is (false? (invoke 'sh07-b51-occurrence-spec-applicable?
                            [candidate]))
            candidate)))))

(deftest sh07-b51-v18-owner-core-body-records-join-form-child-and-postorder
  (let [context (loop-recur-context)
        request (:request context)
        foundation (:foundation context)
        body-form (first (:body-forms foundation))
        catalog (invoke 'sh07-b51-form-position-catalog [request])
        independent-postorder
        (invoke 'sh07-b51-independent-core-postorder [request])
        postorder-record
        (first (filter #(= (:form-id body-form) (:form-id %))
                       (:records independent-postorder)))
        traversal-index
        (first (keep-indexed
                (fn [index candidate]
                  (when (= candidate postorder-record) index))
                (:records independent-postorder)))
        spec (:spec postorder-record)
        key-result
        (invoke 'sh07-b51-occurrence-key-from-spec
                [(:positions catalog) spec traversal-index])
        evaluation-context
        {:evaluation-kind :value
         :evaluation-region :function-body
         :evaluation-owner-function-syntax-id nil
         :binding-context nil}
        owner-preimage
        (invoke 'sh07-b51-build-owner-core-node-preimage
                [request foundation [(internal-reference 0)]
                 (internal-reference 1201) (internal-reference 1202)
                 evaluation-context])
        core-node-preimage
        (-> (:preimage owner-preimage)
            (assoc-in [:source :form-id] (:form-id body-form))
            (assoc-in [:source :syntax-id] (:syntax-id body-form))
            (assoc-in [:source :semantic-span] (:source-span body-form))
            (assoc-in [:source :origin-chain] (:origin-chain body-form))
            (assoc-in [:source :generated-origin]
                      (:generated-origin body-form)))
        digest-request
        {:ordinal 0 :tier 0 :subtier :0d
         :subtier-ordinal (:core-postorder-index postorder-record)
         :batch :0d-core-node :batch-rank 6
         :batch-ordinal (:core-postorder-index postorder-record)
         :purpose :sh07-b51-core-node-id
         :preimage core-node-preimage}
        record {:node-id (internal-reference 0)
                :form-id (:form-id body-form)
                :syntax-id (:syntax-id body-form)
                :owner-child-index (:slot-count foundation)
                :core-postorder-index (:core-postorder-index postorder-record)
                :occurrence-key (:key key-result)
                :core-node-preimage core-node-preimage}
        authenticated
        (invoke 'sh07-b51-authenticate-owner-body-node-records
                [request foundation [record]])
        built
        (invoke 'sh07-b51-build-owner-core-node-authenticated-requests
                [request foundation [record] (internal-reference 1201)
                 (internal-reference 1202) evaluation-context
                 [digest-request]])]
    (is (= :accepted (:status catalog)) catalog)
    (is (= :accepted (:status independent-postorder)) independent-postorder)
    (is postorder-record)
    (is (= :accepted (:status key-result)) key-result)
    (is (= :accepted (:status authenticated)) authenticated)
    (is (= :accepted (:status owner-preimage)) owner-preimage)
    (is (= [(internal-reference 0)] (:node-ids authenticated)))
    (is (= :accepted (:status built)) built)
    (doseq [[reason mutated]
            [[:body-node-record-shape
              (assoc record :node-id (sha \f))]
             [:body-node-record-shape
              (assoc record :node-id {})]
             [:body-node-form-substitution
              (assoc record :form-id (:form-id (:loop-form context)))]
             [:body-node-syntax-substitution
              (assoc record :syntax-id (:syntax-id (:loop-form context)))]
             [:body-node-child-index
              (update record :owner-child-index inc)]
             [:body-node-occurrence-join
              (assoc-in record [:occurrence-key :form-preorder-index] 0)]
             [:body-node-occurrence-join
              (update-in record [:occurrence-key :core-postorder-index]
                         inc)]]]
      (let [rejected
            (invoke 'sh07-b51-authenticate-owner-body-node-records
                    [request foundation [mutated]])]
        (is (= :rejected (:status rejected)) [reason rejected])
        (is (= reason (:reason rejected)) rejected)
        (is (= [] (:partial-products rejected)))))
    (doseq [[reason mutated-record mutated-requests]
            [[:digest-reference-out-of-range
              (assoc record :node-id (internal-reference 1))
              [digest-request]]
             [:digest-reference-preimage-substitution
              (assoc-in record [:core-node-preimage :evaluation-region]
                        :mutated-region)
              [digest-request]]
             [:body-node-core-subtier-postorder-substitution
              record
              [(update digest-request :subtier-ordinal inc)]]]]
      (let [rejected
            (invoke 'sh07-b51-build-owner-core-node-authenticated-requests
                    [request foundation [mutated-record]
                     (internal-reference 1201) (internal-reference 1202)
                     evaluation-context mutated-requests])]
        (is (= :rejected (:status rejected)) [reason rejected])
        (is (= reason (:reason rejected)) rejected)
        (is (= [] (:partial-products rejected)))))
    (let [missing
          (invoke 'sh07-b51-authenticate-owner-body-node-records
                  [request foundation []])]
      (is (= :rejected (:status missing)) missing)
      (is (= :owner-body-node-count (:reason missing)))
      (is (= [] (:partial-products missing))))))

(deftest sh07-b51-v18-occurrence-replay-derives-dense-category-ordinals
  (let [context (loop-recur-context)
        request (:request context)
        owner-form-id (get-in context [:loop-form :form-id])
        recur-form-id (get-in context [:recur-form :form-id])
        base (fn [record-kind]
               {:record-kind record-kind
                :product-kind nil
                :event-kind nil
                :form-id nil
                :event-form-id nil
                :owner-form-id nil
                :form-occurrence-path nil
                :event-occurrence-path nil
                :owner-occurrence-path nil
                :core-postorder-index nil
                :slot-ordinal nil
                :pattern-preorder-index nil
                :local-use-index nil})
        specs
        [(assoc (base :ordinary-core-node)
                :form-id owner-form-id :core-postorder-index 0)
         (assoc (base :runtime-check)
                :event-kind :recur :form-id recur-form-id
                :event-form-id recur-form-id
                :owner-form-id owner-form-id :slot-ordinal 0
                :pattern-preorder-index 0 :local-use-index 0)
         (assoc (base :publication-event)
                :event-kind :recur :form-id recur-form-id
                :event-form-id recur-form-id
                :owner-form-id owner-form-id :local-use-index 0)
         (assoc (base :recur-slot-mapping)
                :event-kind :recur :form-id recur-form-id
                :event-form-id recur-form-id
                :owner-form-id owner-form-id :local-use-index 0)
         (base :module-transcript)]
        replay
        (invoke 'sh07-b51-replay-occurrence-specs-foundation
                [request specs])
        reordered-fragment-request
        (update request :fragment-manifest
                (fn [fragments]
                  (mapv #(update % :form-ids (fn [ids]
                                               (vec (reverse ids))))
                        fragments)))
        reordered-catalog
        (invoke 'sh07-b51-form-position-catalog
                [reordered-fragment-request])
        reordered-replay
        (invoke 'sh07-b51-replay-occurrence-specs-foundation
                [reordered-fragment-request specs])
        initializer-form-id
        (get-in context [:slots 0 :initializer-form :form-id])]
    (is (= :accepted (:status replay)) replay)
    (is (= 5 (:record-count replay)))
    (is (= (vec (range 5))
           (mapv #(get-in % [:occurrence-key :traversal-emission-index])
                 (:entries replay))))
    (is (every? zero? (mapv :global-ordinal (:entries replay))))
    (is (= 1 (get-in replay [:category-counts
                             :global-core-node-ordinal])))
    (is (= 1 (get-in replay [:category-counts
                             :global-runtime-check-ordinal])))
    (is (= 1 (get-in replay [:category-counts
                             :global-publication-ordinal])))
    (is (= 1 (get-in replay [:category-counts
                             :global-recur-mapping-ordinal])))
    (is (= 1 (get-in replay [:category-counts
                             :global-transcript-ordinal])))
    (is (= :accepted (:status reordered-catalog)) reordered-catalog)
    (is (= (invoke 'sh07-b51-form-position-catalog [request])
           reordered-catalog))
    (is (= replay reordered-replay))
    (doseq [mutated [(pop specs)
                     (conj specs (base :module-transcript))
                     (conj (vec (butlast specs))
                           (base :module-transcript)
                           (last (butlast specs)))
                     (assoc-in specs [1 :event-form-id]
                               initializer-form-id)
                     (assoc-in specs [2 :event-form-id]
                               initializer-form-id)
                     (assoc-in specs [3 :event-form-id]
                               initializer-form-id)]]
      (let [rejected
            (invoke 'sh07-b51-replay-occurrence-specs-foundation
                    [request mutated])]
        (is (= :rejected (:status rejected)) rejected)
        (is (= [] (:partial-products rejected)))))))

(deftest sh07-b51-v18-event-forms-are-joined-to-the-exact-owner-and-event
  (let [context (nested-loop-attribution-context)
        request (:request context)
        outer-loop-id (get-in context [:outer-loop :form-id])
        inner-loop-id (get-in context [:inner-loop :form-id])
        recur-form-id (get-in context [:recur-form :form-id])
        outer-initializer-id (get-in context [:outer-initializer :form-id])
        inner-initializer-id (get-in context [:inner-initializer :form-id])
        catalog (invoke 'sh07-b51-form-position-catalog [request])
        occurrence-path
        (fn [form-id]
          (:occurrence-path
           (first (filter #(= form-id (:form-id %))
                          (:positions catalog)))))
        outer-loop-path (occurrence-path outer-loop-id)
        inner-loop-path (occurrence-path inner-loop-id)
        recur-form-path (occurrence-path recur-form-id)
        outer-initializer-path (occurrence-path outer-initializer-id)
        inner-initializer-path (occurrence-path inner-initializer-id)
        base
        {:record-kind :supplemental-product
         :product-kind :slot-initializer
         :event-kind :initializer
         :form-id outer-initializer-id
         :event-form-id outer-initializer-id
         :owner-form-id outer-loop-id
         :form-occurrence-path outer-initializer-path
         :event-occurrence-path outer-initializer-path
         :owner-occurrence-path outer-loop-path
         :core-postorder-index nil
         :slot-ordinal 0
         :pattern-preorder-index nil
         :local-use-index 0}
        recur-spec
        {:record-kind :recur-slot-mapping
         :product-kind nil
         :event-kind :recur
         :form-id recur-form-id
         :event-form-id recur-form-id
         :owner-form-id inner-loop-id
         :form-occurrence-path recur-form-path
         :event-occurrence-path recur-form-path
         :owner-occurrence-path inner-loop-path
         :core-postorder-index nil
         :slot-ordinal nil
         :pattern-preorder-index nil
         :local-use-index 0}
        transcript
        {:record-kind :module-transcript
         :product-kind nil :event-kind nil :form-id nil
         :event-form-id nil :owner-form-id nil
         :form-occurrence-path nil :event-occurrence-path nil
         :owner-occurrence-path nil
         :core-postorder-index nil :slot-ordinal nil
         :pattern-preorder-index nil :local-use-index nil}
        replay-one
        (fn [spec]
          (invoke 'sh07-b51-replay-occurrence-specs-foundation
                  [request [spec transcript]]))]
    (is (= :accepted (:status (replay-one base)))
        (replay-one base))
    (is (= :accepted (:status (replay-one recur-spec)))
        (replay-one recur-spec))
    (doseq [mutated
            [(assoc recur-spec
                    :owner-form-id outer-loop-id
                    :owner-occurrence-path outer-loop-path)
             (assoc base
                    :event-form-id inner-initializer-id
                    :event-occurrence-path inner-initializer-path)
             (assoc recur-spec
                    :form-id outer-initializer-id
                    :form-occurrence-path outer-initializer-path)]]
      (let [rejected (replay-one mutated)]
        (is (= :rejected (:status rejected)) rejected)
        (is (= [] (:partial-products rejected)))))))

(deftest sh07-b51-v18-independent-composer-is-the-only-admitting-replay
  (let [let-context (let-owner-plan-context)
        loop-context (loop-recur-context)
        cases [[:let (:request let-context) (:owner-plans let-context)]
               [:loop (:request loop-context)
                (loop-owner-plans loop-context)]]]
    (doseq [[kind request owner-plans] cases]
      (let [expected
            (invoke 'sh07-b51-compose-expected-occurrence-specs
                    [request owner-plans])
            specs (:specs expected)
            verified
            (invoke 'sh07-b51-verify-and-replay-occurrence-specs
                    [request owner-plans specs])]
        (is (= :accepted (:status expected)) [kind expected])
        (is (true? (:specs-independently-regenerated expected)) expected)
        (is (= :accepted (:status verified)) [kind verified])
        (is (= :independent-exact (:source-spec-regeneration verified)))
        (is (true? (:admitting-success verified)))
        (doseq [supplied
                [(vec (rest specs))
                 (vec (reverse specs))
                 [(last specs)]]]
          (let [rejected
                (invoke 'sh07-b51-verify-and-replay-occurrence-specs
                        [request owner-plans supplied])]
            (is (= :rejected (:status rejected)) [kind rejected])
            (is (= :occurrence-spec-replay-mismatch (:reason rejected))
                rejected)
            (is (= [] (:partial-products rejected)))))))))

(deftest sh07-b51-v18-numbered-preimages-must-equal-replay-category-ordinals
  (let [let-context (let-owner-plan-context)
        let-request (:request let-context)
        let-expected
        (invoke 'sh07-b51-compose-expected-occurrence-specs
                [let-request (:owner-plans let-context)])
        let-replay
        (invoke 'sh07-b51-verify-and-replay-occurrence-specs
                [let-request (:owner-plans let-context)
                 (:specs let-expected)])
        let-entries (:entries let-replay)
        let-foundation
        (get-in let-expected [:materialized-owners 0 :foundation])
        slot (first (:slot-plans let-foundation))
        products
        (invoke 'sh07-b51-owner-product-node-preimages
                [let-request let-foundation (internal-reference 1400) 0])
        entry-for
        (fn [entries record-kind]
          (first (filter #(= record-kind
                             (get-in % [:spec :record-kind])) entries)))
        loop-context (loop-recur-context)
        loop-request (:request loop-context)
        loop-plans (loop-owner-plans loop-context)
        loop-expected
        (invoke 'sh07-b51-compose-expected-occurrence-specs
                [loop-request loop-plans])
        loop-replay
        (invoke 'sh07-b51-verify-and-replay-occurrence-specs
                [loop-request loop-plans (:specs loop-expected)])
        let-numbered
        (invoke 'sh07-b51-compose-numbered-preimages
                [let-request (:owner-plans let-context)
                 (:specs let-expected)
                 (get-in let-context [:layout :predecessor-authority-id])
                 (internal-reference 1402)])
        loop-numbered
        (invoke 'sh07-b51-compose-numbered-preimages
                [loop-request loop-plans (:specs loop-expected)
                 (get-in loop-context [:layout :predecessor-authority-id])
                 (internal-reference 1402)])
        mapping-preimage
        (get-in loop-expected
                [:materialized-owners 0 :loop-recur-sites 0
                 :recur-slot-mapping-preimage])
        cases
        [[:supplemental-product :sh07-b51-product-node-id
          (entry-for let-entries :supplemental-product)
          (first (:preimages products))]
         [:binding-slot :sh07-b51-binding-slot-id
          (entry-for let-entries :binding-slot)
          (:binding-slot-preimage slot)]
         [:binding-extraction :sh07-b51-binding-extraction-id
          (entry-for let-entries :binding-extraction)
          (first (:binding-extraction-preimages slot))]
         [:runtime-check :sh07-b51-runtime-check-id
          (entry-for let-entries :runtime-check)
          (first (:runtime-check-preimages slot))]
         [:publication-event :sh07-b51-publication-event-id
          (entry-for let-entries :publication-event)
          (:publication-event-preimage slot)]
         [:recur-slot-mapping :sh07-b51-recur-slot-mapping-id
          (entry-for (:entries loop-replay) :recur-slot-mapping)
          mapping-preimage]]]
    (is (= :accepted (:status let-expected)) let-expected)
    (is (= :accepted (:status let-replay)) let-replay)
    (is (= :accepted (:status products)) products)
    (is (= :accepted (:status loop-expected)) loop-expected)
    (is (= :accepted (:status loop-replay)) loop-replay)
    (is (= :accepted (:status let-numbered)) let-numbered)
    (is (= :accepted (:status loop-numbered)) loop-numbered)
    (doseq [[record-kind purpose entry preimage] cases]
      (let [field (invoke 'sh07-b51-numbered-row-field [record-kind])
            accepted
            (invoke 'sh07-b51-numbered-preimage-row
                    [(if (= record-kind :recur-slot-mapping)
                       (:entries loop-replay) let-entries)
                     (:spec entry) purpose (internal-reference 1401)
                     preimage])
            mutated
            (invoke 'sh07-b51-numbered-preimage-row
                    [(if (= record-kind :recur-slot-mapping)
                       (:entries loop-replay) let-entries)
                     (:spec entry) purpose (internal-reference 1401)
                     (update preimage field #(+ % 17))])]
        (is (keyword? field) record-kind)
        (is (= (:global-ordinal entry) (get preimage field))
            [record-kind entry preimage])
        (is (= :accepted (:status accepted)) [record-kind accepted])
        (is (= :rejected (:status mutated)) [record-kind mutated])
        (is (= :numbered-preimage-global-ordinal (:reason mutated))
            mutated)
        (is (= [] (:partial-products mutated)))))
    (doseq [replay [let-replay loop-replay]]
      (let [transcripts
            (filterv #(= :module-transcript
                         (get-in % [:spec :record-kind]))
                     (:entries replay))]
        (is (= 1 (count transcripts)))
        (is (= 0 (:global-ordinal (first transcripts))))
        (is (= 1 (get-in replay [:category-counts
                                 :global-transcript-ordinal])))
        (is (= (dec (:record-count replay))
               (get-in (last (:entries replay))
                       [:occurrence-key :traversal-emission-index])))))
    (doseq [[replay numbered]
            [[let-replay let-numbered] [loop-replay loop-numbered]]]
      (let [transcript (:slot-extraction-transcript-preimage numbered)
            fields
            [[:global-slot-ordinal :slot-count :global-slot-range]
             [:global-extraction-ordinal :extraction-count
              :global-extraction-range]
             [:global-runtime-check-ordinal :runtime-check-count
              :global-runtime-check-range]
             [:global-publication-ordinal :publication-event-count
              :global-publication-range]
             [:global-recur-mapping-ordinal :recur-slot-mapping-count
              :global-recur-range]]]
        (doseq [[category count-field range-field] fields]
          (let [expected-count (get-in replay [:category-counts category])]
            (is (= expected-count (get transcript count-field)))
            (is (= {:first 0 :count expected-count}
                   (get transcript range-field)))))
        (doseq [mutated
                [(update transcript :slot-count inc)
                 (update-in transcript [:global-slot-range :count] inc)
                 (assoc-in transcript [:global-recur-range :first] 1)]]
          (let [rejected
                (invoke 'sh07-b51-build-slot-extraction-transcript-preimage
                        [mutated])]
            (is (= :rejected (:status rejected)) rejected)
            (is (= :slot-extraction-transcript-preimage-shape
                   (:reason rejected)))
            (is (= [] (:partial-products rejected)))))))))

;; These positive raw observations deliberately retain the actual B47
;; structural return unchanged.  They remain final-H pin-gated until the
;; checked-core source identity is frozen; do not normalize or fabricate it.
(deftest sh07-b51-v18-raw-observation-cross-run-branches-are-coherent
  (let [rejected-carrier @nested-let-foundation-carrier
        rejected-request (:authenticated-sh06-request rejected-carrier)
        rejected-observation (:predecessor-raw-outcome rejected-carrier)
        accepted-carrier
        (private-foundation-raw-carrier "accepted" "simple-symbol-parity")
        accepted-request (:authenticated-sh06-request accepted-carrier)
        accepted-observation (:predecessor-raw-outcome accepted-carrier)
        accepted-direct-path
        [:direct-template-observation :returned]
        accepted-structural-path
        [:structural-runner-observation :returned]
        accepted-direct (get-in accepted-observation accepted-direct-path)
        accepted-structural
        (get-in accepted-observation accepted-structural-path)
        request-count (count (:digest-requests accepted-direct))
        change-both
        (fn [observation path value]
          (-> observation
              (assoc-in (into accepted-direct-path path) value)
              (assoc-in (into accepted-structural-path
                              (into [:raw-template-result] path))
                        value)))
        out-of-range-reference (internal-reference (+ request-count 17))
        rejected-mutations
        [(assoc-in rejected-observation
                   [:direct-template-observation :returned :diagnostics 0
                    :rule]
                   "C6-FABRICATED")
         (assoc-in rejected-observation
                   [:structural-runner-observation :thrown] nil)
         (assoc-in rejected-observation
                   [:structural-runner-observation :thrown] {})
         (assoc-in rejected-observation
                   [:direct-template-observation :returned :status]
                   :accepted)
         (assoc-in rejected-observation
                   [:direct-template-observation :returned
                    :digest-requests 0 :purpose]
                   :sh07-core-artifact-id)
         (assoc-in rejected-observation
                   [:structural-runner-observation :thrown :form-id]
                   (sha \f))]
        accepted-mutations
        [(assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :digest-requests]
                   [])
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :resolved-digests]
                   [])
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :canonical-core-artifact :artifact-id]
                   (sha \f))
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :canonical-core-artifact :artifact-id]
                   nil)
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :canonical-core-artifact :provenance-binding-id]
                   nil)
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :canonical-core-artifact :provenance-binding-preimage]
                   {:forged true})
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :canonical-core-artifact :provenance]
                   {:forged true})
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :canonical-core-artifact :diagnostics]
                   [{:forged true}])
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :canonical-core-artifact :nodes]
                   [])
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :canonical-core-artifact]
                   (select-keys
                    (:canonical-core-artifact accepted-structural)
                    [:artifact :schema-version :artifact-id
                     :provenance-binding-id]))
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :template-verification :status]
                   :failed)
         (assoc-in accepted-observation
                   [:structural-runner-observation :returned
                    :resolved-verification :rule]
                   "C6-VERIFY")
         (-> accepted-observation
             (assoc-in [:structural-runner-observation :returned] nil)
             (assoc-in [:structural-runner-observation :thrown]
                       {:rule "C6-VERIFY"}))
         (change-both accepted-observation [:bounds] {})
         (change-both accepted-observation [:containment] {})
         (change-both accepted-observation [:execution-boundary]
                      :forged-execution-boundary)
         (change-both accepted-observation
                      [:core-template :artifact-id-request]
                      out-of-range-reference)
         (change-both accepted-observation
                      [:core-template :provenance-binding-id-request]
                      out-of-range-reference)
         (let [changed
               (assoc-in accepted-observation
                         [:direct-template-observation :returned
                          :digest-requests 0 :purpose]
                         :sh07-core-provenance-binding-id)]
           (-> changed
               (assoc-in [:structural-runner-observation :returned
                          :raw-template-result :digest-requests 0 :purpose]
                         :sh07-core-provenance-binding-id)
               (assoc-in [:structural-runner-observation :returned
                          :digest-requests 0 :purpose]
                         :sh07-core-provenance-binding-id)))]]
    (is (= :rejected
           (get-in rejected-observation
                   [:direct-template-observation :returned :status])))
    (is (= :accepted (:status accepted-direct)))
    (doseq [[carrier request observation]
            [[rejected-carrier rejected-request rejected-observation]
             [accepted-carrier accepted-request accepted-observation]]]
      (is (true? (invoke 'sh07-b51-raw-observation-shape?
                         [observation request])))
      (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier]))))
    (doseq [observation rejected-mutations]
      (is (false? (invoke 'sh07-b51-raw-observation-shape?
                          [observation rejected-request])) observation)
      (is (false? (invoke 'sh07-b51-raw-carrier-shape?
                          [(assoc rejected-carrier
                                  :predecessor-raw-outcome observation)]))
          observation))
    (doseq [observation accepted-mutations]
      (is (false? (invoke 'sh07-b51-raw-observation-shape?
                          [observation accepted-request])) observation)
      (is (false? (invoke 'sh07-b51-raw-carrier-shape?
                          [(assoc accepted-carrier
                                  :predecessor-raw-outcome observation)]))
          observation))))

(deftest sh07-b51-v18-no-form-waiver-is-exactly-replay-derived-and-ordered
  (let [carrier (private-no-form-raw-carrier "ordered-no-form-waiver")
        request (:authenticated-sh06-request carrier)
        artifact (:sh06-resolution-artifact carrier)
        indexes (invoke 'sh07-b51-index-request [request])
        derived
        (invoke 'sh07-b51-derived-no-form-failing-binding-ids
                [request indexes])
        expected
        (invoke 'sh07-b51-authenticated-no-form-projections
                [artifact request indexes (:binding-ids derived)])
        form-syntax-ids (set (map :syntax-id (:forms request)))
        independently-derived-ids
        (->> (:binding-table request)
             (filter #(and (= :namespace (:binding-class %))
                           (not (contains? form-syntax-ids
                                           (:definition-syntax-id %)))))
             (mapv :binding-id))
        frontier (invoke 'sh07-b51-first-vector-frontier [request])
        outcome
        {:domain :gravity/sh07-b51-predecessor-outcome-semantic-v18
         :schema-version 18
         :outcome-kind
         :b51-vector-frontier-rejected-after-no-form-waiver
         :status :rejected
         :same-request-semantic-id (internal-reference 2)
         :implementation-semantic-id (internal-reference 0)
         :contract-semantic-id (internal-reference 1)
         :identity-preimage-domain nil
         :actual-core-artifact-kind nil
         :actual-wrapper-artifact-kind nil
         :waived-rule "C6-VERIFY"
         :waived-reason :authoritative-record-shape
         :failing-binding-ids (:binding-ids derived)
         :failing-declarations (:projections expected)
         :replay-status
         :all-invariants-before-expected-vector-stop-passed
         :frontier-owner (:owner-kind frontier)
         :frontier-rule "C6-LOWERING-GAP"
         :frontier-reason (:reason frontier)
         :frontier-binding-kind :vector
         :frontier-form-id (get-in frontier [:form :form-id])
         :frontier-coordinate (:coordinate frontier)
         :semantic-artifact-id nil
         :authorization :v18-no-form-namespace-and-vector-lowering}
        record-replay
        (invoke 'sh07-b51-v16-record-shape-replay
                [artifact request indexes])
        terminal-replay
        (invoke 'sh07-b51-v16-post-waiver-terminal-replay
                [carrier outcome])
        malformed-request
        (assoc-in request [:forms 0 :child-form-ids]
                  [{}])
        malformed-indexes
        (invoke 'sh07-b51-index-request [malformed-request])
        malformed-replay
        (invoke 'sh07-b51-v16-record-shape-replay
                [artifact malformed-request malformed-indexes])
        malformed-carrier
        (-> carrier
            (assoc :authenticated-sh06-request malformed-request)
            (assoc-in [:predecessor-raw-outcome
                       :direct-template-observation :request]
                      malformed-request)
            (assoc-in [:predecessor-raw-outcome
                       :structural-runner-observation :request]
                      malformed-request))
        later-resolution
        (first (filter #(= 'helper (:symbol %))
                       (:resolution-table request)))
        later-context
        (invoke 'sh07-b51-raw-resolution-context
                [artifact request indexes later-resolution])
        later-projection
        (invoke 'sh07-b51-no-form-projection-from-context
                [later-context later-resolution])
        projection-mutations
        [(dissoc (first (:projections expected)) :visibility)
         (assoc (first (:projections expected)) :extra :forbidden)
         (assoc (first (:projections expected)) :binding-class :import)
         (assoc (first (:projections expected)) :visibility :lexical)
         (assoc (first (:projections expected)) :profile-set [:core])
         (assoc (first (:projections expected)) :target-set [:native])
         (assoc (first (:projections expected)) :definition-syntax-id
                (sha \f))
         (assoc (first (:projections expected)) :definition-artifact-id
                (sha \f))
         (assoc (first (:projections expected))
                :explicit-candidate-ordinal 999)
         (assoc (first (:projections expected)) :resolution-order
                :alias-qualified-required-binding)
         {}]
        mutations
        [(update outcome :failing-binding-ids #(vec (reverse %)))
         (update outcome :failing-declarations #(vec (reverse %)))
         (update outcome :failing-binding-ids #(vec (drop 1 %)))
         (update outcome :failing-declarations #(vec (drop 1 %)))
         (assoc outcome :failing-binding-ids [])
         (assoc outcome :failing-declarations [])
         (update outcome :failing-binding-ids conj
                 (:binding-id later-projection))
         (update outcome :failing-declarations conj later-projection)
         (assoc outcome :waived-rule "C6-LOWERING-GAP")
         (assoc outcome :waived-reason :second-failed-predicate)
         (assoc outcome :replay-status :second-predicate-also-waived)
         (assoc outcome :authorization :fabricated-waiver)]]
    (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier])))
    (is (= :accepted (:status derived)) derived)
    (is (= :accepted (:status expected)) expected)
    (is (= 2 (count independently-derived-ids)) independently-derived-ids)
    (is (= independently-derived-ids (:binding-ids derived)))
    (is (= :accepted (:status frontier)) frontier)
    (is (= :accepted (:status record-replay)) record-replay)
    (is (= :authoritative-record-shape
           (:only-waived-predicate record-replay)))
    (is (true? (:all-other-record-shapes-passed record-replay)))
    (is (= :accepted (:status terminal-replay)) terminal-replay)
    (is (= :all-invariants-before-expected-vector-stop-passed
           (:terminal-status terminal-replay)))
    (is (= :rejected (:status malformed-replay)) malformed-replay)
    (is (= :form-occurrence-child-missing
           (:reason malformed-replay)))
    (is (= [] (:partial-products malformed-replay)))
    (is (false? (invoke 'sh07-b51-no-form-outcome-authenticated?
                        [malformed-carrier outcome])))
    ;; The generic raw observation remains internally coherent; only the
    ;; independent authenticated request replay exposes this second cause.
    (is (true? (invoke 'sh07-b51-raw-carrier-shape?
                       [malformed-carrier])))
    (is (true? (invoke 'sh07-b51-vector-outcome-preimage-shape?
                       [outcome])))
    (is (true? (invoke 'sh07-b51-no-form-outcome-authenticated?
                       [carrier outcome])))
    (is (true? (invoke 'sh07-b51-vector-outcome-frontier-authenticated?
                       [carrier outcome])))
    (is (map? later-context))
    (is (false? (invoke 'sh07-b51-no-form-projection-shape?
                        [later-projection])))
    (doseq [projection projection-mutations]
      (let [mutated
            (assoc outcome :failing-declarations
                   (assoc (:projections expected) 0 projection))]
        (is (false? (invoke 'sh07-b51-no-form-outcome-authenticated?
                            [carrier mutated])) projection)))
    (doseq [mutated
            (concat
             mutations
             [(dissoc outcome :failing-binding-ids)
              (assoc outcome :extra :forbidden)
              (assoc outcome :failing-declarations [{}])])]
      (is (or (false? (invoke 'sh07-b51-vector-outcome-preimage-shape?
                              [mutated]))
              (false? (invoke 'sh07-b51-no-form-outcome-authenticated?
                              [carrier mutated])))
          mutated))))

(deftest sh07-b51-v18-numbered-authority-references-join-exact-0a-0c-schedule
  (let [context (let-owner-plan-context "non-definition-owner")
        raw-carrier
        (private-foundation-raw-carrier "accepted" "non-definition-owner")
        request (:authenticated-sh06-request raw-carrier)
        authority-reference (internal-reference 5)
        same-request-reference (internal-reference 2)
        owner-plans
        (mapv #(assoc-in % [:initialization-layout
                            :predecessor-authority-id]
                         authority-reference)
              (:owner-plans context))
        expected
        (invoke 'sh07-b51-compose-expected-occurrence-specs
                [request owner-plans])
        authority-preimage
        {:domain :gravity/sh07-b51-predecessor-authority-v18
         :schema-version 18
         :implementation-semantic-id (internal-reference 0)
         :contract-semantic-id (internal-reference 1)
         :same-request-semantic-id same-request-reference
         :outcome-semantic-id (internal-reference 4)
         :outcome-kind :b51-vector-frontier-rejected
         :semantic-artifact-id nil
         :authorization :v18-vector-lowering}
        same-request-preimage
        {:domain :gravity/sh07-b51-same-request-binding-v18
         :schema-version 18
         :request-artifact :gravity/sh07-authenticated-sh06-core-request
         :request-schema-version 15
         :request-semantic-id (:projection-binding request)
         :source-revision-id (get-in request [:lineage :source-revision-id])
         :top-level-root-semantic-ids (:top-level-form-ids request)}
        implementation-preimage
        {:domain
         :gravity/sh07-b51-implementation-semantic-id-preimage-v18
         :schema-version 18
         :predecessor-executable-descriptor
         (:predecessor-executable-descriptor raw-carrier)
         :v18-semantic-closure
         (get-in raw-carrier
                 [:v18-executable-descriptor :semantic-closure])
         :v18-semantic-closure-hash
         (get-in raw-carrier
                 [:v18-executable-descriptor :semantic-closure-hash])}
        contract-preimage
        {:domain :gravity/sh07-b51-contract-semantic-id-preimage-v18
         :schema-version 18
         :predecessor-contract-carrier
         (:predecessor-contract-carrier raw-carrier)
         :retained-source-contract
         (get-in raw-carrier
                 [:predecessor-contract-carrier :source-contract-forms])}
        let-form (:let-form context)
        frontier-form (get-in context [:slots 0 :pattern-form])
        frontier-fragment
        (unique-form-fragment request frontier-form)
        outcome-preimage
        {:domain :gravity/sh07-b51-predecessor-outcome-semantic-v18
         :schema-version 18
         :outcome-kind :b51-vector-frontier-rejected
         :status :rejected
         :same-request-semantic-id same-request-reference
         :implementation-semantic-id (internal-reference 0)
         :contract-semantic-id (internal-reference 1)
         :identity-preimage-domain nil
         :actual-core-artifact-kind nil
         :actual-wrapper-artifact-kind nil
         :frontier-owner :let
         :frontier-rule "C6-LOWERING-GAP"
         :frontier-reason :let-destructuring-deferred
         :frontier-binding-kind :vector
         :frontier-form-id (:form-id frontier-form)
         :frontier-coordinate
         {:fragment-id (:fragment-id frontier-fragment)
          :form-ordinal (.indexOf (:form-ids frontier-fragment)
                                  (:form-id frontier-form))}
         :semantic-artifact-id nil
         :authorization :v18-vector-lowering}
        numbered
        (invoke 'sh07-b51-compose-numbered-preimages
                [request owner-plans (:specs expected)
                 authority-reference same-request-reference])
        transcript-reference (internal-reference 1500)
        scheduled-request
        (fn [ordinal purpose preimage tier subtier subtier-ordinal batch
             batch-rank batch-ordinal]
          {:ordinal ordinal :tier tier :subtier subtier
           :subtier-ordinal subtier-ordinal :batch batch
           :batch-rank batch-rank :batch-ordinal batch-ordinal
           :purpose purpose :preimage preimage})
        row-request
        (fn [row]
          (let [record-kind (:record-kind row)
                ordinal (get-in row [:reference :ordinal])
                global-ordinal (:global-ordinal row)
                preimage (:preimage row)]
            (cond
              (= record-kind :supplemental-product)
              (scheduled-request
               ordinal :sh07-b51-product-node-id preimage 0 :0d
               (+ (get-in numbered [:occurrence-replay :category-counts
                                    :global-core-node-ordinal])
                  global-ordinal)
               :0d-product-node 7 global-ordinal)

              (= record-kind :binding-slot)
              (scheduled-request ordinal :sh07-b51-binding-slot-id preimage
                                 1 nil nil :t1-slot 8 global-ordinal)

              (= record-kind :binding-extraction)
              (scheduled-request
               ordinal :sh07-b51-binding-extraction-id preimage
               1 nil nil :t1-extraction 9 global-ordinal)

              (= record-kind :runtime-check)
              (scheduled-request ordinal :sh07-b51-runtime-check-id preimage
                                 2 nil nil :t2-runtime-check 10 global-ordinal)

              (= record-kind :publication-event)
              (scheduled-request
               ordinal :sh07-b51-publication-event-id preimage
               2 nil nil :t2-publication 11 global-ordinal)

              (= record-kind :recur-slot-mapping)
              (scheduled-request
               ordinal :sh07-b51-recur-slot-mapping-id preimage
               2 nil nil :t2-recur-mapping 12 global-ordinal))))
        digest-plan
        (fn [authority-ordinal authority-value
             same-request-ordinal same-request-value]
          (let [with-rows
                (reduce
                 (fn [plan row]
                   (assoc plan (get-in row [:reference :ordinal])
                          (row-request row)))
                 (vec (repeat 1501 {}))
                 (:numbered-preimage-rows numbered))]
            (-> with-rows
                (assoc 0
                       (scheduled-request
                        0 :sh07-b51-implementation-semantic-id
                        implementation-preimage
                        0 :0a 0 :0a-implementation 0 0))
                (assoc 1
                       (scheduled-request
                        1 :sh07-b51-contract-semantic-id contract-preimage
                        0 :0a 1 :0a-contract 1 0))
                (assoc same-request-ordinal
                       (scheduled-request
                        same-request-ordinal
                        :sh07-b51-same-request-semantic-id
                        same-request-value 0 :0a 2 :0a-same-request 2 0))
                (assoc authority-ordinal
                       (scheduled-request
                        authority-ordinal
                        :sh07-b51-predecessor-authority-id
                        authority-value 0 :0c 0 :0c-authority 5 0))
                (assoc 4
                       (scheduled-request
                        4 :sh07-b51-predecessor-outcome-semantic-id
                        outcome-preimage 0 :0b 0 :0b-outcome 4 0))
                (assoc (:ordinal transcript-reference)
                       (scheduled-request
                        (:ordinal transcript-reference)
                        :sh07-b51-slot-extraction-transcript-id
                        (:slot-extraction-transcript-preimage numbered)
                        2 nil nil :t2-transcript 13 0)))))
        good-digests
        (digest-plan 5 authority-preimage 2 same-request-preimage)
        authenticate
        (fn [plans digests authority-ref authority-value
             same-request-ref same-request-value]
          (invoke 'sh07-b51-compose-numbered-preimages-authenticated
                  [raw-carrier plans (:specs expected) digests
                   implementation-preimage contract-preimage outcome-preimage
                   authority-ref authority-value
                   same-request-ref same-request-value
                   transcript-reference]))
        accepted
        (authenticate owner-plans good-digests
                      authority-reference authority-preimage
                      same-request-reference same-request-preimage)]
    (is (= :accepted (:status expected)) expected)
    (is (= :accepted (:status numbered)) numbered)
    (is (true? (invoke 'sh07-b51-vector-outcome-preimage-shape?
                       [outcome-preimage])))
    (is (= :accepted (:status accepted)) accepted)
    (is (= (get good-digests 5)
           (:authenticated-authority-request accepted)))
    (is (= (get good-digests 2)
           (:authenticated-same-request-request accepted)))
    (let [mutated-outcome
          (assoc outcome-preimage :frontier-form-id (internal-reference 6))
          mutated-digests
          (assoc-in good-digests [4 :preimage] mutated-outcome)
          rejected
          (invoke 'sh07-b51-compose-numbered-preimages-authenticated
                  [raw-carrier owner-plans (:specs expected) mutated-digests
                   implementation-preimage contract-preimage mutated-outcome
                   authority-reference authority-preimage
                   same-request-reference same-request-preimage
                   transcript-reference])]
      (is (= :rejected (:status rejected)) rejected)
      (is (= :outcome-preimage-shape (:reason rejected)))
      (is (= [] (:partial-products rejected))))
    (let [owner-fragment (unique-form-fragment request let-form)
          mutated-outcome
          (assoc outcome-preimage
                 :frontier-form-id (:form-id let-form)
                 :frontier-coordinate
                 {:fragment-id (:fragment-id owner-fragment)
                  :form-ordinal
                  (.indexOf (:form-ids owner-fragment)
                            (:form-id let-form))})
          mutated-authority
          (assoc authority-preimage
                 :outcome-kind (:outcome-kind mutated-outcome))
          mutated-digests
          (-> good-digests
              (assoc-in [4 :preimage] mutated-outcome)
              (assoc-in [5 :preimage] mutated-authority))
          rejected
          (invoke 'sh07-b51-compose-numbered-preimages-authenticated
                  [raw-carrier owner-plans (:specs expected)
                   mutated-digests implementation-preimage
                   contract-preimage mutated-outcome authority-reference
                   mutated-authority same-request-reference
                   same-request-preimage transcript-reference])]
      (is (= :rejected (:status rejected)) rejected)
      (is (= :outcome-frontier-authentication (:reason rejected)))
      (is (= [] (:partial-products rejected))))
    (doseq [[kind malformed]
            [[:authority (dissoc authority-preimage :schema-version)]
             [:authority (assoc authority-preimage :extra :forbidden)]
             [:authority (assoc authority-preimage :schema-version "18")]
             [:authority (assoc authority-preimage
                                :implementation-semantic-id {})]
             [:authority (assoc authority-preimage
                                :outcome-kind :invented-outcome)]
             [:same-request (dissoc same-request-preimage :schema-version)]
             [:same-request (assoc same-request-preimage :extra :forbidden)]
             [:same-request (assoc same-request-preimage
                                   :schema-version "18")]
             [:same-request (assoc same-request-preimage
                                   :request-artifact :wrong-request)]
             [:same-request (assoc same-request-preimage
                                   :request-semantic-id (sha \f))]
             [:same-request (assoc same-request-preimage
                                   :source-revision-id (sha \f))]
             [:same-request (assoc same-request-preimage
                                   :top-level-root-semantic-ids
                                   [(sha \f)])]]]
      (let [authority-value
            (if (= kind :authority) malformed authority-preimage)
            same-request-value
            (if (= kind :same-request) malformed same-request-preimage)
            rejected
            (authenticate
             owner-plans
             (digest-plan 5 authority-value 2 same-request-value)
             authority-reference authority-value
             same-request-reference same-request-value)]
        (is (= :rejected (:status rejected)) [kind malformed rejected])
        (is (= [] (:partial-products rejected)))
        (is (not (contains? rejected :authenticated-authority-request)))
        (is (not (contains? rejected :authenticated-same-request-request)))))
    (doseq [digests
            [(assoc-in good-digests [5 :purpose]
                       :sh07-b51-product-node-id)
             (assoc-in good-digests [5 :preimage :authorization] :none)
             (assoc-in good-digests [2 :purpose]
                       :sh07-b51-contract-semantic-id)
             (assoc-in good-digests [2 :preimage :request-semantic-id]
                       (sha \f))]]
      (let [rejected
            (authenticate owner-plans digests
                          authority-reference authority-preimage
                          same-request-reference same-request-preimage)]
        (is (= :rejected (:status rejected)) rejected)
        (is (= [] (:partial-products rejected)))))
    (doseq [[authority-ordinal same-request-ordinal]
            [[4 2] [5 3]]]
      (let [authority-ref (internal-reference authority-ordinal)
            same-request-ref (internal-reference same-request-ordinal)
            plans
            (mapv #(assoc-in % [:initialization-layout
                                :predecessor-authority-id]
                             authority-ref)
                  owner-plans)
            rejected
            (authenticate
             plans
             (digest-plan authority-ordinal authority-preimage
                          same-request-ordinal same-request-preimage)
             authority-ref authority-preimage
             same-request-ref same-request-preimage)]
        (is (= :rejected (:status rejected)) rejected)
        (is (= [] (:partial-products rejected)))))
    (doseq [[mutated-authority mutated-outcome mutated-digests]
            (let [implementation-to-contract-authority
                  (assoc authority-preimage
                         :implementation-semantic-id (internal-reference 1))
                  implementation-to-contract-outcome
                  (assoc outcome-preimage
                         :implementation-semantic-id (internal-reference 1))
                  contract-to-implementation-authority
                  (assoc authority-preimage
                         :contract-semantic-id (internal-reference 0))
                  contract-to-implementation-outcome
                  (assoc outcome-preimage
                         :contract-semantic-id (internal-reference 0))]
              [[implementation-to-contract-authority
                implementation-to-contract-outcome
                (-> good-digests
                    (assoc-in [5 :preimage]
                              implementation-to-contract-authority)
                    (assoc-in [4 :preimage]
                              implementation-to-contract-outcome))]
               [contract-to-implementation-authority
                contract-to-implementation-outcome
                (-> good-digests
                    (assoc-in [5 :preimage]
                              contract-to-implementation-authority)
                    (assoc-in [4 :preimage]
                              contract-to-implementation-outcome))]
               [authority-preimage outcome-preimage
                (assoc-in good-digests [4 :purpose]
                          :sh07-b51-contract-semantic-id)]
               [authority-preimage outcome-preimage
                (assoc-in good-digests [4 :preimage]
                          contract-preimage)]])]
      (let [rejected
            (invoke 'sh07-b51-compose-numbered-preimages-authenticated
                    [raw-carrier owner-plans (:specs expected)
                     mutated-digests implementation-preimage
                     contract-preimage mutated-outcome
                     authority-reference mutated-authority
                     same-request-reference same-request-preimage
                     transcript-reference])]
        (is (= :rejected (:status rejected)) rejected)
        (is (= [] (:partial-products rejected)))))))

(deftest sh07-b51-v18-canonical-semantic-child-edges-are-occurrence-exact
  (let [projection
        (fixture-projection "repeated-sibling-core-occurrences")
        request (:request projection)
        indexes (fixture-indexes projection)
        do-form (first (operator-enclosing-forms request indexes 'do))
        sibling-ids (vec (drop 1 (:child-form-ids do-form)))
        sibling-values
        (mapv #(get-in indexes [:forms :by-primary % :value]) sibling-ids)
        postorder (invoke 'sh07-b51-independent-core-postorder [request])
        records (:records postorder)
        records-by-index (into {} (map (juxt :core-postorder-index identity)
                                       records))
        semantic-rows
        (mapv
         (fn [record]
           (let [form (get-in indexes [:forms :by-primary (:form-id record)])
                 selected
                 (invoke 'sh07-b51-core-expression-child-ids [indexes form])
                 child-records
                 (mapv records-by-index
                       (:child-core-postorder-indexes record))]
             {:record record :selected selected :child-records child-records}))
         records)
        edges
        (vec (mapcat
              (fn [{:keys [record selected]}]
                (map-indexed
                 (fn [relative-index child-id]
                   [(:form-id record) [relative-index] child-id])
                 (:child-form-ids selected)))
              semantic-rows))
        expected
        (invoke 'sh07-b51-compose-expected-occurrence-specs [request []])
        replay
        (invoke 'sh07-b51-verify-and-replay-occurrence-specs
                [request [] (:specs expected)])
        sibling-entries
        (filterv #(contains? (set sibling-ids)
                             (get-in % [:spec :form-id]))
                 (:entries replay))
        repeated-form (get-in indexes [:forms :by-primary (first sibling-ids)])
        self-cycle
        (invoke 'sh07-b51-core-postorder-form
                [request indexes repeated-form 0 0 [0]
                 [(first sibling-ids)]])
        synthetic-duplicate-request
        (update request :forms
                (fn [forms]
                  (mapv #(if (= (:form-id do-form) (:form-id %))
                           (assoc % :child-form-ids
                                  [(first (:child-form-ids %))
                                   (first sibling-ids) (first sibling-ids)])
                           %)
                        forms)))
        duplicate-catalog
        (invoke 'sh07-b51-form-position-catalog [synthetic-duplicate-request])]
    (is (= 2 (count sibling-ids)))
    (is (= 2 (count (set sibling-ids))))
    (is (= ['(+ input 1) '(+ input 1)] sibling-values))
    (is (= :accepted (:status postorder)) postorder)
    (is (every? #(= :accepted (get-in % [:selected :status]))
                semantic-rows))
    (doseq [{:keys [record selected child-records]} semantic-rows]
      (is (= (:child-form-ids selected) (mapv :form-id child-records))
          [record selected child-records])
      (is (every? #(< (:core-postorder-index %)
                      (:core-postorder-index record))
                  child-records)
          record))
    (is (= (count edges)
           (count (set (map #(subvec % 0 2) edges))))
        edges)
    (is (= :accepted (:status expected)) expected)
    (is (= :accepted (:status replay)) replay)
    (is (= 2 (count sibling-entries)))
    (doseq [field [:form-preorder-index :core-postorder-index
                   :traversal-emission-index]]
      (is (= 2 (count (set (map #(get-in % [:occurrence-key field])
                                 sibling-entries))))
          field))
    (is (= :rejected (:status self-cycle)) self-cycle)
    (is (= :core-postorder-cycle (:reason self-cycle)))
    (is (= [] (:partial-products self-cycle)))
    (is (= :rejected (:status duplicate-catalog)) duplicate-catalog)
    (is (= [] (:partial-products duplicate-catalog)))))

(deftest sh07-b51-v18-closure-is-disjoint-from-the-frozen-predecessor-roots
  (let [definitions (definitions-by-name @source-forms)
        predecessor-forms (mapv definitions (drop 3 predecessor-exports))
        v18-forms (mapv definitions public-roots)
        symbols-of #(set (filter symbol? (tree-seq coll? seq %)))]
    (doseq [form predecessor-forms]
      (is (not-any? #(str/starts-with? (name %) "sh07-b51-")
                    (symbols-of form))))
    (doseq [form v18-forms]
      (is (not-any? (set (drop 3 predecessor-exports))
                    (symbols-of form))))))

(deftest sh07-b51-v18-mechanical-closure-retains-frozen-b47-boundary
  (let [plan @checked-core-plan
        predecessor (measured-closure plan (vec (drop 3 predecessor-exports)))
        v18 (measured-closure plan public-roots)]
    (is (= 298 (count (:members predecessor))))
    (is (= 849 (count (:edge-set predecessor))))
    (is (= 32 (count (:builtins predecessor))))
    (is (= 1290 (:call-site-count predecessor)))
    (is (= (set public-roots)
           (set (filter (set public-roots) (:members v18)))))
    (is (empty? (set/intersection
                 (set (:members predecessor))
                 (set (:members v18)))))
    (is (every? #(and (vector? %) (= 2 (count %))
                      (every? symbol? %))
                (:edge-set predecessor)))
    (is (= (:edge-set predecessor)
           (:edge-carrier (predecessor-descriptor predecessor))))))

(deftest sh07-b51-v18-authenticated-vector-let-builds-an-ordered-template
  (let [carrier @nested-let-carrier]
    (is (true? (invoke 'sh07-b51-raw-carrier-shape? [carrier])))
    (doseq [mutated [(dissoc carrier :predecessor-raw-outcome)
                     (assoc carrier :schema-version 17)
                     (assoc carrier :scope :wrong-scope)
                     (assoc-in carrier
                               [:predecessor-executable-descriptor
                                :edge-carrier-kind]
                               :canonical-sorted-vector-of-pair-vectors)]]
      (is (false? (invoke 'sh07-b51-raw-carrier-shape? [mutated]))))
    (let [built (invoke 'sh07-b51-build-template [carrier])]
      (is (exact-abi? built :accepted :template-built) built)
      (let [template (get-in built [:value :template])
            requests (get-in built [:value :digest-requests])]
        (is (= :gravity/sh07-b51-canonical-template-v18
               (:artifact template)))
        (is (= 18 (count (set (map :batch requests)))))
        (is (= (vec (range (count requests))) (mapv :ordinal requests)))
        (is (= (vec (range 19))
               (vec (sort (conj (set (map :batch-rank requests)) 3)))))
        (is (= 41 (count (select-keys template product-key-order))))
        (is (= (set product-key-order)
               (set (keys (select-keys template product-key-order)))))
        (is (seq (:binding-slots template)))
        (is (seq (:binding-extractions template)))
        (is (seq (:runtime-checks template)))
        (is (seq (:publication-events template)))
        (is (= :b51-vector-frontier-rejected
               (:predecessor-outcome-kind template)))))))

(deftest sh07-b51-v18-fixture-surface-covers-owner-pattern-runtime-and-recur-cases
  (let [texts
        (into {}
              (for [[family basenames] fixture-catalog
                    basename basenames]
                [[family basename]
                 (slurp (fixture-path family basename ".gravity"))]))]
    (is (str/includes? (get texts ["accepted" "nested-empty-wildcard-let"])
                       "[head [_ [tail]] []]"))
    (is (str/includes?
         (get texts ["accepted" "nested-empty-wildcard-loop-recur"])
         "(recur [head [:ignored [tail]] []] [] (+ total 1))"))
    (is (str/includes? (get texts ["accepted" "simple-symbol-parity"])
                       "(let [_ input"))
    (is (str/includes? (get texts ["accepted" "source-def-owner"])
                       "(def exercise-value"))
    (is (str/includes? (get texts ["accepted" "non-definition-owner"])
                       "\n(let [[left right]"))
    (is (str/includes?
         (get texts ["accepted" "sibling-namespace-external"])
         "sibling-pair"))
    (is (str/includes?
         (get texts ["accepted" "declared-import-external"])
         "shared/checksum"))
    (is (str/includes?
         (get texts ["rejected" "duplicate-vector-binding"])
         "[value [value _] tail tail]"))
    (is (str/includes?
         (get texts ["rejected" "unsupported-vector-rest"])
         "[head & tail]"))
    (is (str/includes?
         (get texts ["rejected" "unsupported-map-pattern"])
         "{:value value}"))
    (is (str/includes?
         (get texts ["rejected" "recur-arity-mismatch"])
         "(recur [left right])"))
    (is (str/includes?
         (get texts ["rejected" "unauthorized-cross-fragment"])
         "[private-value]"))))

(deftest sh07-b51-v18-ordinary-core-form-and-attribute-matrix-is-closed
  (let [definitions (definitions-by-name @source-forms)
        declared (set (mapcat second ordinary-core-lowerer-matrix))]
    (is (= ordinary-core-form-set declared))
    (is (= (set (keys ordinary-core-attribute-keysets))
           (set/difference ordinary-core-form-set #{:let :loop :recur})))
    (doseq [[lowerer expected] ordinary-core-lowerer-matrix]
      (let [form (get definitions lowerer)
            tokens (set (filter keyword? (tree-seq coll? seq form)))]
        (is form lowerer)
        (is (set/subset? expected tokens) [lowerer expected tokens])))
    (doseq [[core-form expected-keys] ordinary-core-attribute-keysets]
      (is (= expected-keys
             (invoke 'sh07-b51-ordinary-core-attribute-keys [core-form]))
          core-form))))

(deftest sh07-b51-v18-ordinary-core-matrix-has-exact-authenticated-postorder
  (let [projection (fixture-projection "ordinary-core-form-matrix")
        request (:request projection)
        indexes (fixture-indexes projection)
        walked (invoke 'sh07-b51-independent-core-postorder [request])
        records (:records walked)
        by-index (into {} (map (juxt :core-postorder-index identity) records))
        observed
        (set
         (map (fn [record]
                (ordinary-core-form-for-authenticated-form
                 indexes (get-in indexes
                                 [:forms :by-primary (:form-id record)])))
              records))]
    (is (= :accepted (:status walked)) walked)
    (is (= ordinary-core-form-set observed) observed)
    (is (= (vec (range (count records)))
           (mapv :core-postorder-index records)))
    (doseq [record records]
      (let [form (get-in indexes [:forms :by-primary (:form-id record)])
            selected
            (invoke 'sh07-b51-core-expression-child-ids [indexes form])
            children (mapv by-index (:child-core-postorder-indexes record))]
        (is (= :accepted (:status selected)) [record selected])
        (is (= (:child-form-ids selected) (mapv :form-id children)) record)
        (is (every? #(< (:core-postorder-index %)
                        (:core-postorder-index record))
                    children)
            record)
        (is (= (:syntax-id form) (:syntax-id record)) record)))
    (let [record (first records)
          form (get-in indexes [:forms :by-primary (:form-id record)])
          forged (assoc form :kind :forged-kind)
          rejected
          (invoke 'sh07-b51-core-postorder-form
                  [request indexes forged 0 0 [0] []])]
      (is (= :rejected (:status rejected)) rejected)
      (is (= :core-postorder-form-substitution (:reason rejected)))
      (is (= [] (:partial-products rejected))))))

(deftest sh07-b51-v18-authentic-ordinary-producer-is-exact-and-mutation-closed
  (let [definitions (definitions-by-name @source-forms)
        producer? (contains? definitions 'sh07-b51-producer-ordinary-core-records)
        authenticator?
        (contains? definitions 'sh07-b51-authenticate-ordinary-core-records)]
    (if (and producer? authenticator?)
      (let [projection (fixture-projection "ordinary-core-form-matrix")
            request (:request projection)
            provisional
            (invoke 'sh07-b51-producer-provisional-numbered [request])
            two-pass
            (invoke 'sh07-b51-producer-two-pass-numbered [request false])
            produced
            (invoke 'sh07-b51-producer-ordinary-core-records
                    [request two-pass])
            preimages (:preimages produced)
            records (:records produced)
            recur-targets (:recur-targets produced)
            recur-transfers (:recur-transfers produced)
            by-form (group-by :core-form records)
            node-ordinals (mapv #(get-in % [:node-id :ordinal]) records)
            authentic
            (invoke 'sh07-b51-authenticate-ordinary-core-records
                    [request two-pass produced])
            first-record (fn [core-form] (first (get by-form core-form)))
            replace-record
            (fn [record mutated]
              (mapv #(if (= (:node-id %) (:node-id record)) mutated %) records))
            authenticate-records
            (fn [candidate-records]
              (invoke 'sh07-b51-authenticate-ordinary-core-records
                      [request two-pass
                       (assoc produced :records candidate-records)]))
            reject-mutation
            (fn [record mutated]
              (authenticate-records (replace-record record mutated)))]
        (is (= :accepted (:status provisional)) provisional)
        (is (= :accepted (:status two-pass)) two-pass)
        (is (= :accepted (:status produced)) produced)
        (is (= :accepted (:status authentic)) authentic)
        (is (= ordinary-core-form-set (set (keys by-form))) (keys by-form))
        (is (= (count preimages) (count records)))
        (is (seq recur-targets) produced)
        (is (seq recur-transfers) produced)
        (is (= (count recur-transfers)
               (count (set (map :recur-form-id recur-transfers))))
            recur-transfers)
        (doseq [transfer recur-transfers]
          (let [target
                (first (filter #(= (:target-id transfer) (:target-id %))
                               recur-targets))]
            (is target transfer)
            (is (= (:target-kind target) (:target-kind transfer)) transfer)
            (is (= (if (= :function (:target-kind target))
                     (:arity target)
                     (:slot-count target))
                   (:arity transfer))
                transfer)
            (is (= (:arity transfer)
                   (count (:argument-node-ids transfer)))
                transfer)))
        (is (= (count node-ordinals) (count (set node-ordinals))))
        (is (= (vec (range (first node-ordinals)
                           (+ (first node-ordinals) (count node-ordinals))))
               node-ordinals))
        (doseq [[preimage record] (map vector preimages records)]
          (is (= (:core-form preimage) (:core-form record)))
          (is (= (get ordinary-core-evaluation-kinds (:core-form record))
                 (:evaluation-kind preimage)))
          (is (= (:evaluation-kind preimage)
                 (get-in record [:evaluation :kind])))
          (is (= (:evaluated-children preimage)
                 (invoke 'sh07-b51-v18-evaluated-children
                         [(:evaluation-kind preimage) (:children preimage)])))
          (is (true?
               (invoke 'sh07-b51-core-node-record-preimage-agrees?
                       [record preimage]))
              [record preimage]))
        (let [ratio-record
              (first (filter #(and (= :literal (:core-form %))
                                   (= :ratio
                                      (get-in % [:attributes :literal-kind])))
                             records))
              map-record
              (first (filter #(and (= :collection-literal (:core-form %))
                                   (= :map
                                      (get-in % [:attributes :literal-kind])))
                             records))
              reference (first-record :reference)
              quote-record (first-record :quote)
              var-record (first-record :var)
              set-record (first-record :set!)
              fn-record (first-record :fn)
              def-record (first-record :def)
              try-record (first-record :try)
              match-record (first-record :match)
              literal-record (first-record :literal)
              child-bearing
              (first (filter #(seq (:children %)) records))
              child-bearing-index
              (first (keep-indexed
                      (fn [index record]
                        (when (= (:node-id record) (:node-id child-bearing))
                          index))
                      records))
              earlier-wrong-node-id
              (first
               (remove (set (:children child-bearing))
                       (mapv :node-id
                             (subvec records 0 child-bearing-index))))
              forged-evaluation
              (-> reference
                  (assoc-in [:evaluation :kind] :forged-evaluation)
                  (assoc-in [:evaluation :order]
                            (invoke 'sh07-b51-v18-evaluation-order
                                    [(:children reference)])))
              odd-map
              (let [children (vec (butlast (:children map-record)))]
                (-> map-record
                    (assoc :children children)
                    (assoc-in [:attributes :element-count] (count children))
                    (assoc-in [:evaluation :order]
                              (invoke 'sh07-b51-v18-evaluation-order
                                      [children]))))
              wrong-earlier-child
              (let [children
                    (assoc (:children child-bearing) 0 earlier-wrong-node-id)]
                (-> child-bearing
                    (assoc :children children)
                    (assoc-in [:evaluation :order]
                              (invoke 'sh07-b51-v18-evaluation-order
                                      [(invoke 'sh07-b51-v18-evaluated-children
                                               [(get-in child-bearing
                                                        [:evaluation :kind])
                                                children])]))))
              symbol-as-literal
              (-> reference
                  (assoc :core-form :literal)
                  (assoc :attributes (:attributes literal-record))
                  (assoc :resolved-binding-ids []))
              accepted-ratio-descriptor
              {:artifact :gravity/ratio-literal
               :kind :ratio :raw "1/2"
               :numerator-spelling "1" :denominator-spelling "2"
               :numerator 1N :denominator 2N
               :semantic-validation :accepted}
              deferred-ratio-descriptor
              {:artifact :gravity/deferred-ratio-literal
               :kind :ratio :raw "9/0"
               :numerator-spelling "9" :denominator-spelling "0"
               :numerator 9N :denominator 0N
               :semantic-validation :deferred
               :reason :zero-denominator}
              ratio-impostors
              [{}
               1
               2/3
               accepted-ratio-descriptor
               deferred-ratio-descriptor
               (dissoc deferred-ratio-descriptor :reason)
               (assoc deferred-ratio-descriptor :extra true)
               (assoc deferred-ratio-descriptor
                      :artifact :gravity/ratio-literal)
               (assoc deferred-ratio-descriptor :kind :integer)
               (assoc deferred-ratio-descriptor :raw "09/0")
               (assoc deferred-ratio-descriptor :numerator-spelling "09")
               (assoc deferred-ratio-descriptor :denominator-spelling "+0")
               (assoc deferred-ratio-descriptor :numerator 8N)
               (assoc deferred-ratio-descriptor :denominator 1N)
               (assoc deferred-ratio-descriptor
                      :semantic-validation :accepted)
               (assoc deferred-ratio-descriptor :reason :forged-reason)]
              mutations
              (concat
               [[reference forged-evaluation]]
               (mapv (fn [value]
                       [ratio-record
                        (assoc-in ratio-record [:attributes :value] value)])
                     ratio-impostors)
               [[map-record odd-map]
               [reference
                (-> reference
                    (assoc-in [:attributes :position] :type)
                    (assoc-in [:attributes :binding-class] :core))]
               [quote-record
                (-> quote-record
                    (assoc-in [:attributes :quoted-kind] :symbol)
                    (assoc-in [:attributes :quoted-value] 'forged))]
               [var-record
                (-> var-record
                    (assoc :children [(:node-id reference)])
                    (assoc-in [:evaluation :order] []))]
               [set-record
                (-> set-record
                    (assoc-in [:attributes :target-symbol] 'forged)
                    (assoc-in [:attributes :target-definition-kind] :type)
                    (assoc-in [:attributes :target-namespace]
                              'forged.namespace))]
               [fn-record (assoc-in fn-record [:attributes :parameters] {})]
               [def-record
                (assoc-in def-record [:attributes :visibility] :forged)]
               [try-record
                (assoc-in try-record [:attributes :handler-clauses 0
                                      :error-type-form-id]
                          (get-in try-record
                                  [:attributes :handler-clauses 0
                                   :catch-binding-form-id]))]
               [match-record
                (assoc-in match-record [:attributes :branch-clauses 0
                                        :pattern-value]
                          :forged-pattern)]
               [child-bearing wrong-earlier-child]
               [reference
                (assoc-in reference [:evaluation :region] :forged-region)]
               [reference
                (assoc-in reference [:evaluation
                                     :owner-function-syntax-id]
                          (sha \e))]
               [reference symbol-as-literal]
               [reference
                (assoc reference :resolved-binding-ids [(sha \f)])]
               [reference
                (assoc reference :core-form :unknown-ordinary-core-form)]])]
          (is ratio-record)
          (is map-record)
          (doseq [[original mutated] mutations]
            (let [rejected (reject-mutation original mutated)]
              (is (= :rejected (:status rejected)) [original rejected])
              (is (= [] (:partial-products rejected)) rejected)))
          (let [first-record-value (first records)
                second-record-value (second records)
                swapped-node-ids
                (-> records
                    (assoc 0 (assoc first-record-value :node-id
                                    (:node-id second-record-value)))
                    (assoc 1 (assoc second-record-value :node-id
                                    (:node-id first-record-value))))
                ;; The repeated form/source identity is placed at a different
                ;; replay ordinal. Its implicit occurrence path is therefore
                ;; wrong even though every retained digest has valid shape.
                duplicate-wrong-occurrence
                (assoc records 1
                       (assoc first-record-value :node-id
                              (:node-id second-record-value)))]
            (doseq [candidate [swapped-node-ids
                               duplicate-wrong-occurrence]]
              (let [rejected (authenticate-records candidate)]
                (is (= :rejected (:status rejected)) rejected)
                (is (= [] (:partial-products rejected)) rejected)))))
        (let [target (first recur-targets)
              transfer (first recur-transfers)
              bundle-mutations
              [(assoc-in produced [:recur-targets 0 :target-id] (sha \f))
               (assoc-in produced [:recur-targets 0 :owner-core-node-id]
                         (:target-id target))
               (update-in produced [:recur-targets 0 :arity] inc)
               (assoc-in produced [:recur-targets 0 :body-scope-id] (sha \g))
               (assoc-in produced [:recur-targets 0 :parent-target-id]
                         (:owner-core-node-id target))
               (assoc-in produced [:recur-transfers 0 :target-id] (sha \h))
               (assoc-in produced [:recur-transfers 0 :target-kind]
                         (if (= :loop (:target-kind transfer))
                           :function :loop))
               (update-in produced [:recur-transfers 0 :arity] inc)
               (assoc-in produced [:recur-transfers 0 :argument-node-ids]
                         [])
               (assoc-in produced [:recur-transfers 0 :tail-position] false)
               (assoc-in produced [:recur-transfers 0 :transfer-policy]
                         :forged-transfer)]]
          (doseq [candidate bundle-mutations]
            (let [rejected
                  (invoke 'sh07-b51-authenticate-ordinary-core-records
                          [request two-pass candidate])]
              (is (= :rejected (:status rejected)) rejected)
              (is (= [] (:partial-products rejected)) rejected)))))
      (is true "authentic ordinary producer assertions await producer seam"))))

(deftest sh07-b51-v18-context-catalog-is-shape-reciprocal-and-path-bounded
  (let [projection (fixture-projection "ordinary-core-form-matrix")
        request (:request projection)
        indexes (fixture-indexes projection)
        context (invoke 'sh07-b51-v18-context-catalog [request])
        postorder (invoke 'sh07-b51-independent-core-postorder [request])
        occurrence-keys
        (fn [records]
          (set (map (juxt :form-id :occurrence-path) records)))
        form-for (fn [operator]
                   (first (operator-enclosing-forms request indexes operator)))
        if-form (form-for 'if)
        fn-form (form-for 'fn)
        try-form (form-for 'try)
        match-form (form-for 'match)
        let-form (form-for 'let)
        loop-form (form-for 'loop)
        do-form (form-for 'do)
        literal-form
        (first (filter #(contains? #{:nil :boolean :integer :ratio
                                     :decimal :character :string :keyword}
                                   (:kind %))
                       (:forms request)))
        mutate-form
        (fn [candidate form-id update-form]
          (update candidate :forms
                  (fn [forms]
                    (mapv #(if (= form-id (:form-id %))
                             (update-form %)
                             %)
                          forms))))
        append-child
        (fn [candidate form]
          (mutate-form candidate (:form-id form)
                       #(update % :child-form-ids conj
                                (:form-id literal-form))))
        binding-vector
        (fn [owner]
          (get-in indexes [:forms :by-primary
                           (second (:child-form-ids owner))]))
        first-do-child-id (second (:child-form-ids do-form))
        second-do-child-id (nth (:child-form-ids do-form) 2)
        reused-sibling
        (mutate-form request (:form-id do-form)
                     #(assoc-in % [:child-form-ids 2] first-do-child-id))
        reciprocal-parent-mismatch
        (mutate-form request first-do-child-id
                     #(assoc % :parent-form-id (:form-id match-form)))
        malformed
        [(append-child request if-form)
         (append-child request fn-form)
         (append-child request try-form)
         (mutate-form request (:form-id match-form)
                      #(update % :child-form-ids pop))
         (append-child request (binding-vector let-form))
         (append-child request (binding-vector loop-form))
         reused-sibling
         reciprocal-parent-mismatch]
        deep-count 258
        deep-form-ids
        (mapv #(bootstrap/reader-canonical-hash
                {:domain :b51-context-depth-form :ordinal %})
              (range deep-count))
        deep-syntax-ids
        (mapv #(bootstrap/reader-canonical-hash
                {:domain :b51-context-depth-syntax :ordinal %})
              (range deep-count))
        deep-forms
        (mapv
         (fn [index]
           {:kind :vector
            :form-id (get deep-form-ids index)
            :syntax-id (get deep-syntax-ids index)
            :parent-form-id (when (pos? index)
                              (get deep-form-ids (dec index)))
            :scope-id (:scope-id literal-form)
            :value [] :metadata {}
            :source-span (:source-span literal-form)
            :origin-chain [] :generated-origin []
            :child-form-ids
            (if (< (inc index) deep-count)
              [(get deep-form-ids (inc index))]
              [])})
         (range deep-count))
        deep-request
        (assoc request :forms deep-forms
               :top-level-form-ids [(first deep-form-ids)])
        too-deep (invoke 'sh07-b51-v18-context-catalog [deep-request])]
    (is (= :accepted (:status context)) context)
    (is (= :accepted (:status postorder)) postorder)
    (is (= (occurrence-keys (:records postorder))
           (occurrence-keys (:records context))))
    (is (= (count (:records context))
           (count (occurrence-keys (:records context)))))
    (is (not= first-do-child-id second-do-child-id))
    (doseq [candidate malformed]
      (let [rejected (invoke 'sh07-b51-v18-context-catalog [candidate])]
        (is (= :rejected (:status rejected)) rejected)
        (is (= [] (:partial-products rejected)) rejected)))
    (is (= :rejected (:status too-deep)) too-deep)
    ;; The independently regenerated postorder is the first path consumer, so
    ;; it must reject the 257th descent before context records are constructed.
    (is (= :core-postorder-depth (:reason too-deep)))
    (is (= [] (:partial-products too-deep)))))

(deftest sh07-b51-v18-classifier-and-simple-semantics-are-closed
  (let [matrix (:request (fixture-projection "ordinary-core-form-matrix"))
        import-request
        (:request (fixture-projection "declared-import-external"))
        mutate-form
        (fn [request form-id update-form]
          (update request :forms
                  (fn [forms]
                    (mapv #(if (= form-id (:form-id %))
                             (update-form %)
                             %)
                          forms))))
        form-for
        (fn [request operator]
          (first
           (operator-enclosing-forms
            request (fixture-indexes {:request request}) operator)))
        form-by-id
        (fn [request form-id]
          (get-in (fixture-indexes {:request request})
                  [:forms :by-primary form-id]))
        context-for
        (fn [request form-id]
          (first
           (filter #(= form-id (:form-id %))
                   (:records (invoke 'sh07-b51-v18-context-catalog
                                     [request])))))
        simple
        (fn [request form core-form children context]
          (invoke 'sh07-b51-v18-simple-semantics
                  [request (fixture-indexes {:request request}) form
                   core-form children context]))
        literal
        (first (filter #(= :integer (:kind %)) (:forms matrix)))
        quote-form (form-for matrix 'quote)
        var-form (form-for matrix 'var)
        set-form (form-for matrix 'set!)
        def-form (form-for matrix 'def)
        fn-form (form-for matrix 'fn)
        call-form (form-for matrix 'helper)
        try-form (form-for matrix 'try)
        params (form-by-id matrix (second (:child-form-ids fn-form)))
        parameter (form-by-id matrix (first (:child-form-ids params)))
        append-literal
        (fn [request owner]
          (mutate-form request (:form-id owner)
                       #(update % :child-form-ids conj (:form-id literal))))
        quote-extra (append-literal matrix quote-form)
        var-extra (append-literal matrix var-form)
        nested-def-context
        (assoc (context-for matrix (:form-id def-form))
               :evaluation-region :function-body
               :evaluation-owner-function-syntax-id (:syntax-id fn-form))
        mutate-parameter
        (fn [update-parameter]
          (mutate-form matrix (:form-id parameter) update-parameter))
        parameter-binding
        (first (filter #(= (:syntax-id parameter)
                           (:definition-syntax-id %))
                       (:binding-table matrix)))
        nonlexical-fn
        (update matrix :binding-table
                (fn [bindings]
                  (mapv #(if (= (:binding-id parameter-binding)
                                (:binding-id %))
                           (assoc % :binding-class :namespace)
                           %)
                        bindings)))
        substituted-fn-body
        (mutate-form matrix (:form-id fn-form)
                     #(assoc-in % [:child-form-ids 2] (:form-id literal)))
        binding-for-operand
        (fn [request owner]
          (let [indexes (fixture-indexes {:request request})
                operand (form-by-id request
                                    (second (:child-form-ids owner)))
                resolution
                (get-in indexes [:resolutions :by-primary
                                 (:syntax-id operand)])]
            (get-in indexes [:bindings :by-primary
                             (:binding-id resolution)])))
        upstream-mismatch
        (fn [request owner]
          (let [binding (binding-for-operand request owner)]
            (update request :binding-table
                    (fn [bindings]
                      (mapv #(if (= (:binding-id binding) (:binding-id %))
                               (assoc % :upstream-binding-id (sha \a))
                               %)
                            bindings)))))
        import-symbol
        (first (filter #(= 'shared/checksum (:value %))
                       (:forms import-request)))
        import-context (context-for import-request (:form-id import-symbol))
        import-semantics
        (simple import-request import-symbol :reference [] import-context)
        try-indexes (fixture-indexes {:request matrix})
        handlers
        (invoke 'sh07-b51-v18-try-handler-descriptors
                [matrix try-indexes try-form])
        clause (first (:descriptors handlers))
        _catch-type
        (form-by-id matrix (:error-type-form-id clause))
        catch-binding
        (get-in try-indexes [:bindings :by-primary
                             (:catch-binding-id clause)])
        catch-binding-form
        (form-by-id matrix (:catch-binding-form-id clause))
        catch-type-resolution
        (get-in try-indexes [:resolutions :by-primary
                             (:error-type-syntax-id clause)])
        mutate-record
        (fn [request table id-key id update-record]
          (update request table
                  (fn [records]
                    (mapv #(if (= id (get % id-key))
                             (update-record %)
                             %)
                          records))))
        catch-mutations
        [(mutate-record matrix :resolution-table :reference-syntax-id
                        (:error-type-syntax-id clause)
                        #(assoc % :position :expression))
         (mutate-record matrix :binding-table :binding-id
                        (:error-type-binding-id clause)
                        #(assoc % :binding-class :namespace))
         (mutate-record matrix :binding-table :binding-id
                        (:catch-binding-id clause)
                        #(assoc % :scope-id (sha \c)))]
        unsupported
        '#{defconst syntax-quote unquote splice-unquote unsafe}
        unsupported-results
        (for [operator unsupported]
          (let [request
                (mutate-form matrix
                             (first (:child-form-ids call-form))
                             #(assoc % :value operator))
                indexes (fixture-indexes {:request request})
                form (form-by-id request (:form-id call-form))]
            [operator (invoke 'sh07-b51-v18-classify-form [indexes form])]))
        fn-children [(invoke 'sh07-b51-internal-reference [100])]
        reject-cases
        [(simple quote-extra
                 (form-by-id quote-extra (:form-id quote-form))
                 :quote [] (context-for quote-extra (:form-id quote-form)))
         (simple var-extra
                 (form-by-id var-extra (:form-id var-form))
                 :var [] (context-for var-extra (:form-id var-form)))
         (simple matrix def-form :def
                 [(invoke 'sh07-b51-internal-reference [101])]
                 nested-def-context)
         (let [request (mutate-parameter #(assoc % :kind :integer :value 0))]
           (simple request (form-by-id request (:form-id fn-form))
                   :fn fn-children
                   (context-for request (:form-id fn-form))))
         (let [request (mutate-parameter #(assoc % :value '&))]
           (simple request (form-by-id request (:form-id fn-form))
                   :fn fn-children
                   (context-for request (:form-id fn-form))))
         (simple nonlexical-fn
                 (form-by-id nonlexical-fn (:form-id fn-form))
                 :fn fn-children
                 (context-for nonlexical-fn (:form-id fn-form)))
         (simple substituted-fn-body
                 (form-by-id substituted-fn-body (:form-id fn-form))
                 :fn fn-children
                 (context-for substituted-fn-body (:form-id fn-form)))
         (let [request (upstream-mismatch matrix var-form)]
           (simple request (form-by-id request (:form-id var-form))
                   :var [] (context-for request (:form-id var-form))))
         (let [request (upstream-mismatch matrix set-form)]
           (simple request (form-by-id request (:form-id set-form))
                   :set! [(invoke 'sh07-b51-internal-reference [102])]
                   (context-for request (:form-id set-form))))]]
    (is (= :accepted (:status import-semantics)) import-semantics)
    (is (= 'shared/checksum (get-in import-semantics
                                    [:attributes :symbol])))
    (is (= :import (get-in import-semantics
                           [:attributes :binding-class])))
    (is (= :accepted (:status handlers)) handlers)
    (is (= :type (:position catch-type-resolution)) catch-type-resolution)
    (is (= :core (get-in try-indexes [:bindings :by-primary
                                      (:binding-id catch-type-resolution)
                                      :binding-class])))
    (is (= :lexical (:binding-class catch-binding)) catch-binding)
    (is (= :local (:kind catch-binding)) catch-binding)
    (is (= (:scope-id catch-binding-form) (:scope-id catch-binding))
        catch-binding)
    (doseq [request catch-mutations]
      (let [result
            (invoke 'sh07-b51-v18-try-handler-descriptors
                    [request (fixture-indexes {:request request})
                     (form-by-id request (:form-id try-form))])]
        (is (= :rejected (:status result)) result)
        (is (= [] (:partial-products result)) result)))
    (doseq [[operator result] unsupported-results]
      (is (= :rejected (:status result)) [operator result])
      (is (= :producer-v18-non-expression-form (:reason result))
          [operator result])
      (is (= [] (:partial-products result)) result))
    (doseq [result reject-cases]
      (is (= :rejected (:status result)) result)
      (is (= [] (:partial-products result)) result))
    ;; This direct seam used to call vector-range with a negative count and
    ;; could fail to terminate. A call without even an operator is closed.
    (let [empty-call
          (simple matrix call-form :call []
                  (context-for matrix (:form-id call-form)))]
      (is (= :rejected (:status empty-call)) empty-call)
      (is (= :producer-v18-call-arity (:reason empty-call)))
      (is (= [] (:partial-products empty-call))))))

(deftest sh07-b51-v18-simple-node-builder-authenticates-replay-and-context
  (let [request (:request (fixture-projection "ordinary-core-form-matrix"))
        indexes (fixture-indexes {:request request})
        postorder (:records (invoke 'sh07-b51-independent-core-postorder
                                    [request]))
        contexts (:records (invoke 'sh07-b51-v18-context-catalog [request]))
        form-core
        (fn [record]
          (ordinary-core-form-for-authenticated-form
           indexes (get-in indexes [:forms :by-primary (:form-id record)])))
        reference-record (first (filter #(= :reference (form-core %)) postorder))
        call-record (first (filter #(= :call (form-core %)) postorder))
        context-for
        (fn [record]
          (first (filter #(and (= (:form-id record) (:form-id %))
                               (= (:occurrence-path record)
                                  (:occurrence-path %)))
                         contexts)))
        authority (invoke 'sh07-b51-internal-reference [0])
        core-base 10000
        build
        (fn [record context]
          (invoke 'sh07-b51-v18-build-simple-postorder-node
                  [request record context core-base authority]))
        authentic-reference (build reference-record
                                   (context-for reference-record))
        authentic-call (build call-record (context-for call-record))
        wrong-syntax (assoc reference-record :syntax-id (sha \b))
        wrong-index
        (first
         (remove (set (:child-core-postorder-indexes call-record))
                 (range (:core-postorder-index call-record))))
        wrong-child-index
        (assoc-in call-record [:child-core-postorder-indexes 0] wrong-index)
        wrong-context
        (first (remove #(= [(:form-id reference-record)
                            (:occurrence-path reference-record)]
                           [(:form-id %) (:occurrence-path %)])
                       contexts))]
    (is (= :accepted (:status authentic-reference)) authentic-reference)
    (is (= :accepted (:status authentic-call)) authentic-call)
    (is (integer? wrong-index) call-record)
    (doseq [result [(build wrong-syntax (context-for reference-record))
                    (build wrong-child-index (context-for call-record))
                    (build reference-record wrong-context)]]
      (is (= :rejected (:status result)) result)
      (is (= [] (:partial-products result)) result))))

(deftest sh07-b51-v18-owner-and-recur-node-builders-use-final-two-pass-joins
  (let [definitions (definitions-by-name @source-forms)
        owner-seam? (contains? definitions
                               'sh07-b51-v18-build-owner-postorder-node)
        recur-seam? (contains? definitions
                               'sh07-b51-v18-build-recur-postorder-node)]
    (if (and owner-seam? recur-seam?)
      (let [request
            (:request (fixture-projection "nested-loop-owner-attribution"))
            two-pass (invoke 'sh07-b51-producer-two-pass-numbered
                             [request false])
            postorder (:postorder-records two-pass)
            contexts
            (:records (invoke 'sh07-b51-v18-context-catalog [request]))
            owner-plans (:owner-plans two-pass)
            materialized (:materialized-owners two-pass)
            plan-by-owner (into {} (map (juxt :owner-form-id identity)
                                        owner-plans))
            postorder-for
            (fn [form-id]
              (first (filter #(= form-id (:form-id %)) postorder)))
            context-for
            (fn [record]
              (first
               (filter #(= [(:form-id record) (:occurrence-path record)]
                           [(:form-id %) (:occurrence-path %)])
                       contexts)))
            owner-records
            (mapv #(postorder-for (:owner-form-id %)) materialized)
            owner-results
            (mapv #(invoke 'sh07-b51-v18-build-owner-postorder-node
                           [request two-pass % (context-for %)])
                  owner-records)
            recur-sites
            (vec
             (mapcat
              (fn [owner]
                (mapv #(assoc % :owner-form-id (:owner-form-id owner))
                      (:loop-recur-sites owner)))
              materialized))
            recur-records
            (mapv #(postorder-for
                    (get-in % [:recur-coordinate :recur-form-id]))
                  recur-sites)
            recur-results
            (mapv #(invoke 'sh07-b51-v18-build-recur-postorder-node
                           [request two-pass % (context-for %)])
                  recur-records)
            first-owner-record (first owner-records)
            first-recur-record (first recur-records)
            wrong-context
            (first
             (remove #(= [(:form-id first-owner-record)
                          (:occurrence-path first-owner-record)]
                         [(:form-id %) (:occurrence-path %)])
                     contexts))
            owner-ref-for
            (fn [owner-result]
              (get-in plan-by-owner
                      [(:form-id owner-result) :initialization-layout
                       :owner-core-node-id]))
            recur-layouts
            (vec (mapcat :recur-layout-entries owner-plans))
            recur-ref-for-form
            (fn [form-id]
              (get-in
               (first (filter #(= form-id (:recur-form-id %))
                              recur-layouts))
               [:layout :recur-core-node-id]))
            owner-id-substitution
            (assoc-in two-pass
                      [:materialized-owners 0 :initialization-layout
                       :owner-core-node-id]
                      (get-in materialized
                              [1 :initialization-layout
                               :owner-core-node-id]))
            owner-plan-divergence
            (assoc-in two-pass
                      [:owner-plans 0 :initialization-layout
                       :owner-core-node-id]
                      (get-in owner-plans
                              [1 :initialization-layout
                               :owner-core-node-id]))
            owner-foundation-substitution
            (assoc-in two-pass [:materialized-owners 0 :foundation]
                      (get-in materialized [1 :foundation]))
            recur-entry-index
            (first
             (keep-indexed
              (fn [index entry]
                (when (= (:form-id first-recur-record)
                         (:recur-form-id entry))
                  index))
              (mapcat :recur-layout-entries owner-plans)))
            recur-owner-index
            (first
             (keep-indexed
              (fn [index owner]
                (when (some #(= (:form-id first-recur-record)
                                (:recur-form-id %))
                            (:recur-layout-entries owner))
                  index))
              owner-plans))
            owner-recur-index
            (first
             (keep-indexed
              (fn [index entry]
                (when (= (:form-id first-recur-record)
                         (:recur-form-id entry))
                  index))
              (get-in owner-plans [recur-owner-index
                                   :recur-layout-entries])))
            recur-layout
            (get-in owner-plans [recur-owner-index :recur-layout-entries
                                 owner-recur-index :layout])
            recur-id-substitution
            (assoc-in two-pass
                      [:materialized-owners recur-owner-index
                       :recur-layout-entries owner-recur-index :layout
                      :recur-core-node-id]
                      (first (:argument-node-ids recur-layout)))
            mutate-recur-site
            (fn [path value]
              (assoc-in two-pass
                        (vec
                         (concat
                          [:materialized-owners recur-owner-index
                           :loop-recur-sites owner-recur-index]
                          path))
                        value))
            recur-target-substitution
            (mutate-recur-site
             [:target-loop-form-id]
             (get-in materialized
                     [(if (= recur-owner-index 0) 1 0) :owner-form-id]))
            recur-target-kind-substitution
            (mutate-recur-site [:target-kind] :function)
            recur-all-or-none-substitution
            (mutate-recur-site [:all-or-none] false)
            recur-commit-policy-substitution
            (mutate-recur-site [:commit-policy] :arguments-left-to-right)
            wrong-recur-child-index
            (first
             (remove (set (:child-core-postorder-indexes first-recur-record))
                     (range (:core-postorder-index first-recur-record))))
            wrong-recur-children
            (assoc-in first-recur-record [:child-core-postorder-indexes 0]
                      wrong-recur-child-index)
            build-owner
            (fn [candidate record context]
              (invoke 'sh07-b51-v18-build-owner-postorder-node
                      [request candidate record context]))
            build-recur
            (fn [candidate record context]
              (invoke 'sh07-b51-v18-build-recur-postorder-node
                      [request candidate record context]))
            first-recur-site (first recur-sites)
            first-recur-form
            (get-in (fixture-indexes {:request request})
                    [:forms :by-primary (:form-id first-recur-record)])
            core-target-substitution
            (invoke 'sh07-b51-build-recur-core-node-preimage
                    [request first-recur-site
                     (get-in plan-by-owner
                             [(:owner-form-id first-recur-site)
                              :initialization-layout :owner-core-node-id])
                     (internal-reference (get-in two-pass
                                                 [:bases :authority]))
                     (invoke 'sh07-b51-v18-recur-builder-context
                             [first-recur-form
                              (context-for first-recur-record)])])]
        (is (= :accepted (:status two-pass)) two-pass)
        (is (= 2 (count owner-results)))
        (is (= 1 (count recur-results)))
        (is (integer? wrong-recur-child-index) first-recur-record)
        (is (= :rejected (:status core-target-substitution))
            core-target-substitution)
        (is (= [] (:partial-products core-target-substitution))
            core-target-substitution)
        (doseq [result owner-results]
          (is (= :accepted (:status result)) result)
          (is (contains? #{:let :loop} (get-in result [:record :core-form]))
              result)
          (is (= (owner-ref-for result) (:node-id result)) result)
          (is (true?
               (invoke 'sh07-b51-core-node-record-preimage-agrees?
                       [(:record result) (:preimage result)]))
              result))
        (doseq [result recur-results]
          (is (= :accepted (:status result)) result)
          (is (= :recur (get-in result [:record :core-form])) result)
          (is (= (recur-ref-for-form (:form-id result)) (:node-id result))
              result)
          (let [site
                (first
                 (filter #(= (:form-id result)
                             (get-in % [:recur-coordinate :recur-form-id]))
                         recur-sites))
                owner
                (first (filter #(= (:owner-form-id site) (:owner-form-id %))
                               materialized))]
            (is (= (get-in owner [:foundation :owner-coordinate
                                  :enclosing-syntax-id])
                   (get-in result [:record :attributes :target-id]))
                result))
          (is (true?
               (invoke 'sh07-b51-core-node-record-preimage-agrees?
                       [(:record result) (:preimage result)]))
              result))
        (is (integer? recur-entry-index))
        (doseq [result
                [(build-owner owner-id-substitution first-owner-record
                              (context-for first-owner-record))
                 (build-owner owner-plan-divergence first-owner-record
                              (context-for first-owner-record))
                 (build-owner owner-foundation-substitution
                              first-owner-record
                              (context-for first-owner-record))
                 (build-owner two-pass
                              (assoc first-owner-record :syntax-id (sha \d))
                              (context-for first-owner-record))
                 (build-owner two-pass first-owner-record wrong-context)
                 (build-recur recur-id-substitution first-recur-record
                              (context-for first-recur-record))
                 (build-recur recur-target-substitution first-recur-record
                              (context-for first-recur-record))
                 (build-recur recur-target-kind-substitution
                              first-recur-record
                              (context-for first-recur-record))
                 (build-recur recur-all-or-none-substitution
                              first-recur-record
                              (context-for first-recur-record))
                 (build-recur recur-commit-policy-substitution
                              first-recur-record
                              (context-for first-recur-record))
                 (build-recur two-pass wrong-recur-children
                              (context-for first-recur-record))
                 (build-recur two-pass first-recur-record wrong-context)]]
          (is (= :rejected (:status result)) result)
          (is (= [] (:partial-products result)) result)))
      (is true "owner/recur node assertions await concrete producer seams"))))

(deftest sh07-b51-v18-function-and-loop-recurs-bind-the-nearest-target
  (let [definitions (definitions-by-name @source-forms)
        function-seam?
        (contains? definitions
                   'sh07-b51-v18-build-function-recur-postorder-node)]
    (if function-seam?
      (let [cases
            [["plain-function-recur" [:function]]
             ["function-recur-inside-loop" [:function]]
             ["loop-recur-inside-function" [:loop]]
             ["nested-nearest-recur-targets" [:function :loop]]]
            results
            (mapv
             (fn [[basename expected-kinds]]
               (let [request (:request (fixture-projection basename))
                     indexes (fixture-indexes {:request request})
                     recur-forms
                     (operator-enclosing-forms request indexes 'recur)
                     fragment-for
                     (fn [form]
                       (first
                        (filter
                         #(= 1 (:count
                                (invoke 'sh07-b51-index-of
                                        [(:form-ids %) (:form-id form)])))
                         (:fragment-manifest request))))
                     targets
                     (mapv
                      (fn [recur-form]
                        (invoke 'sh07-b51-nearest-recur-target
                                [request indexes (fragment-for recur-form)
                                 recur-form]))
                      recur-forms)
                     function-targets
                     (filterv #(= :function (:target-kind (second %)))
                              (mapv vector recur-forms targets))
                     two-pass
                     (invoke 'sh07-b51-producer-two-pass-numbered
                             [request false])
                     contexts
                     (:records (invoke 'sh07-b51-v18-context-catalog
                                       [request]))
                     postorder (:postorder-records two-pass)
                     record-for
                     (fn [form-id]
                       (first (filter #(= form-id (:form-id %)) postorder)))
                     context-for
                     (fn [record]
                       (first
                        (filter
                         #(= [(:form-id record) (:occurrence-path record)]
                             [(:form-id %) (:occurrence-path %)])
                         contexts)))
                     function-results
                     (mapv
                      (fn [[recur-form _]]
                        (let [record (record-for (:form-id recur-form))]
                          (invoke
                           'sh07-b51-v18-build-function-recur-postorder-node
                           [request two-pass record (context-for record)])))
                      function-targets)]
                 {:basename basename :request request :indexes indexes
                  :expected-kinds expected-kinds :recur-forms recur-forms
                  :targets targets :function-targets function-targets
                  :two-pass two-pass :function-results function-results}))
             cases)]
        (doseq [{:keys [basename request indexes expected-kinds recur-forms
                        targets function-targets two-pass function-results]}
                results]
          (is (= :accepted (:status two-pass)) [basename two-pass])
          (is (= (count expected-kinds) (count recur-forms)) basename)
          (is (= expected-kinds (mapv :target-kind targets))
              [basename targets])
          (doseq [target targets]
            (is (= :accepted (:status target)) [basename target]))
          (doseq [[recur-form target] function-targets]
            (let [parity
                  (invoke 'sh07-b51-function-target-recur-parity
                          [request indexes
                           (first
                            (filter
                             #(some #{(:form-id recur-form)} (:form-ids %))
                             (:fragment-manifest request)))
                           recur-form (:target-form target)])]
              (is (= :accepted (:status parity)) [basename parity])
              (is (= (:arity parity) (:argument-count parity)) parity)
              (is (= [] (:b51-products parity)) parity)
              (is (true? (:tail-only parity)) parity)
              (is (false? (:calls-b47-lowerer parity)) parity)))
          (doseq [result function-results]
            (is (= :accepted (:status result)) [basename result])
            (is (= :recur (get-in result [:record :core-form])) result)
            (is (= :function
                   (get-in result [:record :attributes :target-kind]))
                result)
            (let [target
                  (second
                   (first
                    (filter #(= (:form-id result) (:form-id (first %)))
                            function-targets)))]
              (is (= (get-in target [:target-form :syntax-id])
                     (get-in result [:record :attributes :target-id]))
                  result))
            (is (= (count (get-in result [:record :children]))
                   (get-in result [:record :attributes :arity]))
                result)
            (is (true?
                 (invoke 'sh07-b51-v18-function-recur-zero-products?
                         [two-pass (:form-id result)]))
                result)))
        (let [{:keys [request indexes recur-forms targets]}
              (last results)
              function-pair
              (first (filter #(= :function (:target-kind (second %)))
                             (map vector recur-forms targets)))
              loop-pair
              (first (filter #(= :loop (:target-kind (second %)))
                             (map vector recur-forms targets)))
              function-recur (first function-pair)
              loop-recur (first loop-pair)
              function-target (:target-form (second function-pair))
              loop-target (:target-form (second loop-pair))
              fragment
              (first (filter #(some #{(:form-id function-recur)}
                                    (:form-ids %))
                             (:fragment-manifest request)))
              swapped-function
              (invoke 'sh07-b51-function-target-recur-parity
                      [request indexes fragment function-recur loop-target])
              swapped-loop
              (invoke 'sh07-b51-function-target-recur-parity
                      [request indexes fragment loop-recur function-target])]
          (is (= :rejected (:status swapped-function)) swapped-function)
          (is (= [] (:partial-products swapped-function)) swapped-function)
          (is (= :rejected (:status swapped-loop)) swapped-loop)
          (is (= [] (:partial-products swapped-loop)) swapped-loop)))
      (is true "function-target recur assertions await producer seam"))))

(deftest sh07-b51-v18-match-allows-only-tail-branch-recurs
  (let [projection (fixture-projection "function-recur-match-tail")
        request (:request projection)
        indexes (fixture-indexes projection)
        recur-form (first (operator-enclosing-forms request indexes 'recur))
        match-form (first (operator-enclosing-forms request indexes 'match))
        fragment
        (first (filter #(some #{(:form-id recur-form)} (:form-ids %))
                       (:fragment-manifest request)))
        target (invoke 'sh07-b51-nearest-recur-target
                       [request indexes fragment recur-form])
        parity (invoke 'sh07-b51-function-target-recur-parity
                       [request indexes fragment recur-form
                        (:target-form target)])
        recur-index
        (first (keep-indexed (fn [index form-id]
                               (when (= form-id (:form-id recur-form)) index))
                             (:child-form-ids match-form)))
        mutate-match
        (fn [target-index]
          (let [children (:child-form-ids match-form)
                displaced (get children target-index)
                swapped (-> children
                            (assoc target-index (:form-id recur-form))
                            (assoc recur-index displaced))]
            (update request :forms
                    (fn [forms]
                      (mapv #(if (= (:form-id match-form) (:form-id %))
                               (assoc % :child-form-ids swapped)
                               %)
                            forms)))))
        non-tail-results
        (mapv
         (fn [target-index]
           (let [candidate (mutate-match target-index)
                 candidate-indexes (fixture-indexes {:request candidate})
                 candidate-recur
                 (get-in candidate-indexes [:forms :by-primary
                                            (:form-id recur-form)])
                 candidate-target
                 (get-in candidate-indexes [:forms :by-primary
                                            (get-in target
                                                    [:target-form :form-id])])]
             (invoke 'sh07-b51-function-target-recur-parity
                     [candidate candidate-indexes fragment candidate-recur
                      candidate-target])))
         [1 2])]
    (is (= :accepted (:status target)) target)
    (is (= :function (:target-kind target)) target)
    (is (= :accepted (:status parity)) parity)
    (is (odd? recur-index) {:match match-form :recur-index recur-index})
    (is (> recur-index 2) recur-index)
    (doseq [result non-tail-results]
      (is (= :rejected (:status result)) result)
      (is (= :recur-not-tail (:reason result)) result)
      (is (= [] (:partial-products result)) result))))

(deftest sh07-b51-v18-recur-target-and-transfer-records-join-semantically
  (let [definitions (definitions-by-name @source-forms)
        target-seam?
        (contains? definitions 'sh07-b51-v18-build-recur-target-record)
        transfer-seam?
        (contains? definitions 'sh07-b51-v18-build-recur-transfer-record)]
    (if (and target-seam? transfer-seam?)
      (doseq [basename ["plain-function-recur"
                        "nested-nearest-recur-targets"]]
        (let [request (:request (fixture-projection basename))
              indexes (fixture-indexes {:request request})
              two-pass (invoke 'sh07-b51-producer-two-pass-numbered
                               [request false])
              postorder (:postorder-records two-pass)
              contexts
              (:records (invoke 'sh07-b51-v18-context-catalog [request]))
              recur-forms (operator-enclosing-forms request indexes 'recur)
              fragment-for
              (fn [form]
                (first (filter #(some #{(:form-id form)} (:form-ids %))
                               (:fragment-manifest request))))
              targets
              (mapv #(invoke 'sh07-b51-nearest-recur-target
                             [request indexes (fragment-for %) %])
                    recur-forms)
              target-forms
              (vec
               (vals
                (into {} (map (fn [target]
                                [(get-in target [:target-form :form-id])
                                 (:target-form target)])
                              targets))))
              postorder-for
              (fn [form-id]
                (first (filter #(= form-id (:form-id %)) postorder)))
              context-for
              (fn [record]
                (first
                 (filter #(= [(:form-id record) (:occurrence-path record)]
                             [(:form-id %) (:occurrence-path %)])
                         contexts)))
              target-results
              (mapv
               (fn [target-form]
                 (let [record (postorder-for (:form-id target-form))]
                   (invoke 'sh07-b51-v18-build-recur-target-record
                           [request two-pass record (context-for record)])))
               target-forms)
              target-records (mapv :record target-results)
              transfer-results
              (mapv
               (fn [recur-form]
                 (let [record (postorder-for (:form-id recur-form))]
                   (invoke 'sh07-b51-v18-build-recur-transfer-record
                           [request two-pass record (context-for record)
                            target-records])))
               recur-forms)
              target-by-id (into {} (map (juxt :target-id identity)
                                         target-records))]
          (is (= :accepted (:status two-pass)) [basename two-pass])
          (is (= (count target-forms) (count target-records)) basename)
          (is (= (count recur-forms) (count transfer-results)) basename)
          (is (= (count target-records)
                 (count (set (map :target-id target-records))))
              [basename target-records])
          (doseq [result target-results]
            (let [record (:record result)
                  function-target? (= :function (:target-kind record))
                  target-arity (if function-target?
                                 (:arity record)
                                 (:slot-count record))]
              (is (= :accepted (:status result)) [basename result])
              (is (= (if function-target?
                       #{:target-id :target-kind :owner-core-node-id
                         :owner-form-id :owner-syntax-id :arity :binding-ids
                         :body-scope-id :parent-target-id :type-compatibility}
                       #{:target-id :target-kind :owner-core-node-id
                         :owner-form-id :owner-syntax-id :slot-count :slot-ids
                         :slot-ordinals :binding-id-vectors :body-scope-id
                         :parent-target-id :type-compatibility})
                     (set (keys record)))
                  record)
              (is (= (:target-id record) (:owner-syntax-id record)) record)
              (is (= target-arity
                     (count (if function-target?
                              (:binding-ids record)
                              (:binding-id-vectors record))))
                  record)
              (when-not function-target?
                (is (= target-arity (count (:slot-ids record))) record)
                (is (= (vec (range target-arity)) (:slot-ordinals record))
                    record))
              (is (boolean
                   (re-matches #"sha256:[0-9a-f]{64}" (:target-id record)))
                  record)
              (is (invoke 'sh07-b51-internal-reference?
                          [(:owner-core-node-id record)])
                  record)
              (is (not= (:target-id record) (:owner-core-node-id record))
                  record)
              (when (some? (:parent-target-id record))
                (is (boolean
                     (re-matches #"sha256:[0-9a-f]{64}"
                                 (:parent-target-id record)))
                    record)
                (is (not= (:parent-target-id record)
                          (:owner-core-node-id record))
                    record))))
          (doseq [result transfer-results]
            (let [record (:record result)
                  target (get target-by-id (:target-id record))
                  target-arity (if (= :function (:target-kind target))
                                 (:arity target)
                                 (:slot-count target))]
              (is (= :accepted (:status result)) [basename result])
              (is (= #{:recur-core-node-id :recur-form-id
                       :recur-syntax-id :target-id :target-kind :arity
                       :argument-node-ids :tail-position :evaluation-order
                       :transfer-policy :type-compatibility}
                     (set (keys record)))
                  record)
              (is target record)
              (is (= (:target-kind target) (:target-kind record)) record)
              (is (= target-arity (:arity record)) record)
              (is (= (:arity record) (count (:argument-node-ids record)))
                  record)
              (is (= true (:tail-position record)) record)
              (is (= :arguments-left-to-right
                     (:evaluation-order record))
                  record)
              (is (= :nearest-lexical-recur-target
                     (:transfer-policy record))
                  record)
              (is (= :pending-sh08 (:type-compatibility record)) record)))
          (let [first-recur (first recur-forms)
                recur-postorder (postorder-for (:form-id first-recur))
                build
                (fn [records]
                  (invoke 'sh07-b51-v18-build-recur-transfer-record
                          [request two-pass recur-postorder
                           (context-for recur-postorder) records]))
                first-target (first target-records)
                other-target (second target-records)
                loop-target-index
                (first
                 (keep-indexed
                  (fn [index record]
                    (when (= :loop (:target-kind record)) index))
                  target-records))
                replace-target
                (fn [index record]
                  (assoc target-records index record))
                mutations
                (cond->
                 [[:duplicate-target
                   (conj target-records first-target)]
                  [:owner-core-reference
                   (replace-target
                    0 (update-in first-target [:owner-core-node-id :ordinal]
                                 inc))]
                  [:arity
                   (replace-target 0 (update first-target :arity inc))]
                  [:binding-ids
                   (replace-target
                    0 (assoc-in first-target [:binding-ids 0] (sha \f)))]
                  [:body-scope
                   (replace-target
                    0 (assoc first-target :body-scope-id (sha \e)))]
                  [:parent-target
                   (replace-target
                    0 (assoc first-target :parent-target-id (sha \d)))]]
                  other-target
                  (conj
                   [:cross-target-id
                    (replace-target
                     0 (assoc first-target :target-id
                              (:target-id other-target)))])
                  (some? loop-target-index)
                  (into
                   (let [loop-target (nth target-records loop-target-index)]
                     [[:loop-slot-count
                       (replace-target
                        loop-target-index (update loop-target :slot-count inc))]
                      [:loop-slot-id
                       (replace-target
                        loop-target-index
                        (update-in loop-target [:slot-ids 0 :ordinal] inc))]
                      [:loop-slot-ordinal
                       (replace-target
                        loop-target-index
                        (assoc-in loop-target [:slot-ordinals 0] 1))]
                      [:loop-binding-id
                       (replace-target
                        loop-target-index
                        (assoc-in loop-target [:binding-id-vectors 0 0]
                                  (sha \c)))]])))]
            (doseq [[mutation records] mutations]
              (let [rejected (build (vec records))]
                (is (= :rejected (:status rejected))
                    [basename mutation rejected])
                (is (= [] (:partial-products rejected))
                    [basename mutation rejected]))))))
      (is true "recur target/transfer records await producer seams"))))

(deftest sh07-b51-v18-slot-records-join-their-owner-plan-not-owner-position
  (let [projection (fixture-projection "nested-loop-owner-attribution")
        request (:request projection)
        provisional (invoke 'sh07-b51-producer-provisional-numbered [request])
        owners (:materialized-owners provisional)
        postorder (:postorder-records provisional)
        postorder-by-form
        (into {} (map (juxt :form-id :core-postorder-index) postorder))
        owner-preorder (mapv :owner-form-id owners)
        owner-postorder
        (mapv first
              (sort-by second
                       (mapv (fn [form-id]
                               [form-id (get postorder-by-form form-id)])
                             owner-preorder)))
        slot-plans
        (vec
         (mapcat #(get-in % [:foundation :slot-plans]) owners))
        slot-preimages (mapv :binding-slot-preimage slot-plans)
        slot-references (mapv :slot-id slot-plans)
        extraction-reference-vectors
        (mapv :extraction-ids slot-plans)
        built
        (invoke 'sh07-b51-producer-slot-records-for-owners
                [owners slot-preimages slot-references
                 extraction-reference-vectors])
        plan-by-coordinate
        (into
         {}
         (map (fn [slot-plan]
                (let [preimage (:binding-slot-preimage slot-plan)]
                  [[(:owner-coordinate preimage)
                    (:slot-coordinate preimage)]
                   slot-plan])))
         slot-plans)]
    (is (= :accepted (:status provisional)) provisional)
    (is (= 2 (count owners)))
    (is (not= owner-preorder owner-postorder)
        {:preorder owner-preorder :postorder owner-postorder})
    (is (= 1
           (count
            (set
             (map (fn [slot-plan]
                    [(count (:binding-extraction-preimages slot-plan))
                     (count (:terminal-projections slot-plan))
                     (count (filter #(= :binding-leaf (:kind %))
                                    (:binding-extraction-preimages slot-plan)))
                     (count (filter #(= :vector-node (:kind %))
                                    (:binding-extraction-preimages slot-plan)))
                     (:runtime-policy slot-plan)])
                  slot-plans))))
        "the two slots deliberately have the same pattern shape")
    (is (= :accepted (:status built)) built)
    (is (= (count slot-plans) (count (:records built))))
    (doseq [record (:records built)]
      (let [slot-plan
            (get plan-by-coordinate
                 [(:owner-coordinate record) (:slot-coordinate record)])
            preimage (:binding-slot-preimage slot-plan)
            extractions (:binding-extraction-preimages slot-plan)
            evaluation (:initializer-evaluation slot-plan)
            publication (:publication-event-preimage slot-plan)]
        (is slot-plan record)
        (is (= (:owner-coordinate preimage) (:owner-coordinate record)))
        (is (= (:pattern-form-id preimage) (:pattern-form-id record)))
        (is (= (:pattern-syntax-id preimage) (:pattern-syntax-id record)))
        (is (= (:pattern-scope-id preimage) (:pattern-scope-id record)))
        (is (= (:visible-prior-binding-ids evaluation)
               (:visible-prior-binding-ids record)))
        (is (= (:introduced-binding-ids publication)
               (:introduced-binding-ids record)))
        (is (= (count extractions) (:extraction-count record)))
        (is (= (count (:terminal-projections slot-plan))
               (:terminal-count record)))
        (is (= (count (filter #(= :binding-leaf (:kind %)) extractions))
               (:leaf-count record)))
        (is (= (count (filter #(= :vector-node (:kind %)) extractions))
               (:vector-node-count record)))
        (is (= (:runtime-policy slot-plan) (:runtime-policy record)))))
    (let [swapped
          (invoke 'sh07-b51-producer-slot-records-for-owners
                  [owners (vec (reverse slot-preimages)) slot-references
                   extraction-reference-vectors])]
      (is (= :rejected (:status swapped)) swapped)
      (is (= [] (:partial-products swapped))))))

(deftest sh07-b51-v18-product-bundles-are-branch-exact-and-regenerated
  (let [definitions (definitions-by-name @source-forms)
        inherited-keys (subvec product-key-order 0 33)
        supplemental-keys (subvec product-key-order 33 40)
        inherited-paths
        (invoke 'sh07-b51-inherited-product-reference-paths [])
        nonlegacy-seams?
        (every? #(contains? definitions %)
                ['sh07-b51-producer-nonlegacy-product-bundle
                 'sh07-b51-authenticate-nonlegacy-product-bundle])
        legacy-seams?
        (every? #(contains? definitions %)
                ['sh07-b51-producer-legacy-product-bundle
                 'sh07-b51-authenticate-legacy-product-bundle])
        root1-private?
        (contains? definitions 'sh07-b51-build-template-private)]
    (is (= 41 (count product-key-order)))
    (is (= 33 (count inherited-keys)))
    (is (= [:destructuring-product-nodes :binding-slots
            :binding-extractions :slot-extraction-transcript
            :runtime-checks :publication-events :recur-slot-mappings]
           supplemental-keys))
    (is (= 58 (count inherited-paths)))
    (is (= 58 (count (set inherited-paths))))
    (doseq [outcome root3-outcome-kinds]
      (let [descriptors
            (invoke 'sh07-b51-tier3-reference-descriptors [outcome])
            by-path (into {} (map (juxt :path identity) descriptors))
            selected
            (mapv #(get by-path (into [:products] %)) inherited-paths)
            legacy? (= outcome :legacy-v16-accepted)]
        (is (= 58 (count selected)) outcome)
        (is (every? some? selected) outcome)
        (is (every? #(= (if legacy? :concrete :generated)
                        (:representation %))
                    selected)
            outcome)
        (is (every? #(= (if legacy? nil :sh07-b51-core-node-id)
                        (:target-purpose %))
                    selected)
            outcome)))
    (if nonlegacy-seams?
      (let [carrier
            (private-foundation-raw-carrier
             "accepted" "ordinary-core-form-matrix")
            request (:authenticated-sh06-request carrier)
            two-pass (invoke 'sh07-b51-producer-two-pass-numbered
                             [request false])
            ordinary
            (invoke 'sh07-b51-producer-ordinary-core-records
                    [request two-pass])
            produced
            (invoke 'sh07-b51-producer-nonlegacy-product-bundle
                    [request two-pass ordinary])
            products (:products produced)
            authenticate
            (fn [supplied]
              (invoke 'sh07-b51-authenticate-nonlegacy-product-bundle
                      [request two-pass ordinary supplied]))
            first-slot (first (:binding-slots products))
            first-target (first (:recur-targets products))
            first-transfer (first (:recur-transfers products))
            foreign-node-id
            (first
             (remove #{(:owner-core-node-id first-slot)}
                     (map :node-id (:nodes products))))
            mutations
            (cond->
             [[:missing-category
               (update produced :products dissoc (first inherited-keys))]
              [:extra-category
               (assoc-in produced [:products :forged-category] [])]
              [:reordered-inherited-family
               (update-in produced [:products :nodes]
                          #(vec (reverse %)))]
              [:reordered-supplemental-family
               (update-in produced [:products :binding-slots]
                          #(vec (reverse %)))]
              [:cross-family-node-id
               (assoc-in produced [:products :nodes 0 :node-id]
                         (:slot-id first-slot))]
              [:cross-family-target-owner
               (assoc-in produced
                         [:products :recur-targets 0 :owner-core-node-id]
                         (:recur-core-node-id first-transfer))]
              [:nonlegacy-equivalence
               (assoc-in produced [:products :legacy-v16-equivalence] {})]]
              foreign-node-id
              (conj
               [:slot-owner-substitution
                (assoc-in produced
                          [:products :binding-slots 0 :owner-core-node-id]
                          foreign-node-id)])
              (and first-target first-transfer)
              (conj
               [:transfer-target-substitution
                (assoc-in produced
                          [:products :recur-transfers 0 :target-id]
                          (:owner-form-id first-target))]))]
        (is (= :accepted (:status two-pass)) two-pass)
        (is (= :accepted (:status ordinary)) ordinary)
        (is (= #{:status :products :partial-products}
               (set (keys produced))) produced)
        (is (= :accepted (:status produced)) produced)
        (is (= [] (:partial-products produced)) produced)
        (is (= (set product-key-order) (set (keys products))) products)
        (is (= (:records ordinary) (:nodes products)))
        (is (= (:recur-targets ordinary) (:recur-targets products)))
        (is (= (:recur-transfers ordinary) (:recur-transfers products)))
        (is (nil? (:legacy-v16-equivalence products)))
        (is (true?
             (invoke 'sh07-b51-producer-inherited-reference-closure?
                     [products])))
        (is (true?
             (invoke 'sh07-b51-producer-inherited-family-counts?
                     [products])))
        (is (false?
             (invoke 'sh07-b51-producer-inherited-reference-closure?
                     [(assoc-in products [:nodes 0 :node-id] (sha \a))])))
        (is (false?
             (invoke 'sh07-b51-producer-inherited-reference-closure?
                     [(assoc-in products [:nodes 0 :node-id]
                                (internal-reference 999999))])))
        (is (false?
             (invoke 'sh07-b51-producer-inherited-reference-closure?
                     [(assoc-in products [:nodes 1 :node-id]
                                (get-in products [:nodes 0 :node-id]))])))
        (is (false?
             (invoke 'sh07-b51-producer-inherited-family-counts?
                     [(update products :source-map pop)])))
        (is (false?
             (invoke 'sh07-b51-producer-inherited-family-counts?
                     [(update products :evaluation-order pop)])))
        (is (false?
             (invoke 'sh07-b51-producer-inherited-family-counts?
                     [(update products :reference-uses conj
                              (first (:reference-uses products)))])))
        (is (every? seq
                    (map products
                         [:destructuring-product-nodes :binding-slots
                          :binding-extractions :runtime-checks
                          :publication-events :recur-slot-mappings])))
        (is (map? (:slot-extraction-transcript products)))
        (let [authenticated (authenticate produced)]
          (is (= #{:status :products :independently-regenerated
                   :partial-products}
                 (set (keys authenticated))) authenticated)
          (is (= :accepted (:status authenticated)) authenticated)
          (is (= products (:products authenticated)))
          (is (true? (:independently-regenerated authenticated)))
          (is (= [] (:partial-products authenticated))))
        (if root1-private?
          ;; The lower product seam deliberately uses the fixed private
          ;; mechanics carrier.  Root1 requires an independently admitted raw
          ;; observation, exercised by the dedicated no-form Root1 test below.
          (is true "product/root1 join is covered with an admitted carrier")
          (is true "nonlegacy bundle/root1 integration is dormant"))
        (doseq [[label mutation] mutations]
          (let [rejected (authenticate mutation)]
            (is (= :rejected (:status rejected)) [label rejected])
            (is (= [] (:partial-products rejected)) [label rejected]))))
      (is true "nonlegacy product bundle seams are dormant"))
    (if legacy-seams?
      (let [carrier
            (private-foundation-raw-carrier
             "accepted" "simple-symbol-parity")
            request (:authenticated-sh06-request carrier)
            derived (invoke 'sh07-b51-producer-outcome-kind [carrier])
            outcome-kind (:outcome-kind derived)
            provisional
            (invoke 'sh07-b51-producer-provisional-numbered [request])
            counts
            (invoke 'sh07-b51-producer-purpose-counts-from-numbered
                    [provisional])
            bases (invoke 'sh07-b51-producer-purpose-bases [true counts])
            outcome
            (invoke 'sh07-b51-producer-outcome-preimage
                    [carrier derived bases])
            produced
            (invoke 'sh07-b51-producer-legacy-product-bundle
                    [carrier outcome bases])
            products (:products produced)
            artifact
            (get-in carrier
                    [:predecessor-raw-outcome :structural-runner-observation
                     :returned :canonical-core-artifact])
            preserved (:preserved-resolution artifact)
            expected-inherited
            (-> (select-keys artifact inherited-keys)
                (assoc :binding-table (:binding-table preserved)
                       :resolution-table (:resolution-table preserved)))
            semantic-key-order
            (invoke 'sh07-b51-v16-semantic-key-order [])
            authenticate
            (fn [supplied]
              (invoke 'sh07-b51-authenticate-legacy-product-bundle
                      [carrier outcome bases supplied]))
            transcript (:slot-extraction-transcript products)
            mutations
            [[:missing-inherited-category
              (update produced :products dissoc (first inherited-keys))]
             [:extra-category
              (assoc-in produced [:products :forged-category] [])]
             [:reordered-inherited-nodes
              (update-in produced [:products :nodes]
                         #(vec (reverse %)))]
             [:legacy-v18-node
              (assoc-in produced [:products :nodes 0 :artifact]
                        :gravity/sh07-b51-core-node-v18)]
             [:legacy-concrete-node-made-generated
              (assoc-in produced [:products :nodes 0 :node-id]
                        (internal-reference 999999))]
             [:legacy-binding-table-not-preserved
              (update-in produced [:products :binding-table]
                         conj {:forged true})]
             [:legacy-resolution-table-not-preserved
              (update-in produced [:products :resolution-table]
                         conj {:forged true})]
             [:legacy-supplemental-product
              (assoc-in produced [:products :binding-slots]
                        [{:forged true}])]
             [:legacy-transcript-slot
              (assoc-in produced
                        [:products :slot-extraction-transcript
                         :preimage :slot-ids]
                        [(internal-reference 0)])]
             [:missing-equivalence
              (assoc-in produced [:products :legacy-v16-equivalence] nil)]
             [:missing-v16-projection-field
              (update-in produced
                         [:products :legacy-v16-equivalence
                          :v16-semantic-projection]
                         dissoc :nodes)]
             [:extra-v18-projection-field
              (assoc-in produced
                        [:products :legacy-v16-equivalence
                         :v18-compatibility-projection :b51-forged]
                        [])]
             [:reordered-field-mapping
              (update-in produced
                         [:products :legacy-v16-equivalence :field-mapping]
                         #(vec (reverse %)))]
             [:wrong-field-mapping-normalization
              (assoc-in produced
                        [:products :legacy-v16-equivalence
                         :field-mapping 0 :normalization]
                        :direct-resolved-semantic)]
             [:generated-v16-artifact-id
              (assoc-in produced
                        [:products :legacy-v16-equivalence
                         :inherited-v16-semantic-artifact-id]
                        (internal-reference 999999))]
             [:concrete-same-request-id
              (assoc-in produced
                        [:products :legacy-v16-equivalence
                         :same-request-semantic-id]
                        (sha \f))]]]
        (if (= :accepted (:status derived))
          (do
        (is (= :legacy-v16-accepted outcome-kind))
        (is (= :accepted (:status produced)) produced)
        (is (= #{:status :products :partial-products}
               (set (keys produced))) produced)
        (is (= (set product-key-order) (set (keys products))))
        (is (= 37 (count semantic-key-order)))
        (is (= 37 (count (set semantic-key-order))))
        (is (= expected-inherited (select-keys products inherited-keys)))
        (is (= (:binding-table preserved) (:binding-table products)))
        (is (= (:resolution-table preserved) (:resolution-table products)))
        (is (true?
             (invoke 'sh07-b51-legacy-inherited-products-for-carrier?
                     [carrier products])))
        (is (map? (:legacy-v16-equivalence products)))
        (let [equivalence (:legacy-v16-equivalence products)
              v16-view (:v16-semantic-projection equivalence)
              v18-view (:v18-compatibility-projection equivalence)
              mapping (:field-mapping equivalence)]
          (is (= semantic-key-order (:v16-semantic-key-order equivalence)))
          (is (= (set semantic-key-order) (set (keys v16-view))))
          (is (= (set semantic-key-order) (set (keys v18-view))))
          (is (= 37 (count v16-view)))
          (is (= 37 (count v18-view)))
          (is (= v16-view v18-view))
          (is (vector? mapping))
          (let [top-level-mapping
                (into
                 [{:v16-path [:domain] :v18-path [:domain]
                   :normalization :constant-source
                   :constant :gravity/sh07-b47-canonical-core-v16}]
                 (map (fn [key]
                        {:v16-path [key] :v18-path [key]
                         :normalization :direct-resolved-semantic})
                      (rest semantic-key-order)))]
            (is (every? (set mapping) top-level-mapping))
            (is (= top-level-mapping
                   (filterv #(= 1 (count (:v16-path %))) mapping)))))
        ;; Legacy nodes and roots remain concrete B47 IDs: the v18 core-node
        ;; request family is bypassed rather than run with an empty mapping.
        (is (empty? (internal-reference-occurrences (:nodes products))))
        (is (empty?
             (internal-reference-occurrences (:root-core-node-ids products))))
        (is (empty?
             (internal-reference-occurrences
              (select-keys products inherited-keys))))
        (is (every? empty?
                    (map products
                         [:destructuring-product-nodes :binding-slots
                          :binding-extractions :runtime-checks
                          :publication-events :recur-slot-mappings])))
        (is (map? transcript))
        (is (= [0 0 0 0 0]
               (mapv #(get-in transcript [:preimage %])
                     [:slot-count :extraction-count :runtime-check-count
                      :publication-event-count :recur-slot-mapping-count])))
        (is (every? #{ {:first 0 :count 0} }
                    (map #(get-in transcript [:preimage %])
                         [:global-slot-range :global-extraction-range
                          :global-runtime-check-range
                          :global-publication-range :global-recur-range])))
        (is (every? empty?
                    (map #(get-in transcript [:preimage %])
                         [:product-node-ids :slot-ids :extraction-ids
                          :runtime-check-ids :publication-event-ids
                          :recur-slot-mapping-ids])))
        (let [authenticated (authenticate produced)]
          (is (= :accepted (:status authenticated)) authenticated)
          (is (= products (:products authenticated)))
          (is (true? (:independently-regenerated authenticated)))
          (is (= [] (:partial-products authenticated))))
        (if root1-private?
          (let [root1 (invoke 'sh07-b51-build-template-private [carrier])
                template (get-in root1 [:value :template])]
            (is (exact-abi? root1 :accepted :template-built) root1)
            (is (= products (select-keys template product-key-order))
                root1)
            (is (= 0
                   (invoke 'sh07-b51-purpose-count
                           [(get-in root1 [:value :digest-requests])
                            :sh07-b51-core-node-id]))
                root1))
          (is true "legacy bundle/root1 integration is dormant"))
        (doseq [[label mutation] mutations]
          (let [rejected (authenticate mutation)]
            (is (= :rejected (:status rejected)) [label rejected])
            (is (= [] (:partial-products rejected)) [label rejected])))
        (let [nonlegacy-outcome
              (assoc outcome :outcome-kind :b51-vector-frontier-rejected)
              rejected
              (invoke 'sh07-b51-producer-legacy-product-bundle
                      [carrier nonlegacy-outcome bases])]
          (is (= :rejected (:status rejected)) rejected)
          (is (= [] (:partial-products rejected)) rejected)))
          (do
            ;; The authentic B47 structural return is unchanged.  Until the
            ;; final-H identity pins admit it, legacy product construction
            ;; must remain closed rather than fabricate a canonical artifact.
            (is (= :rejected (:status derived)) derived)
            (is (= :producer-legacy-structural-artifact-missing
                   (:reason derived))
                derived)
            (is (= :rejected (:status produced)) produced)
            (is (= :producer-legacy-outcome-kind (:reason produced))
                produced)
            (is (= [] (:partial-products produced)) produced)))
      (is true "legacy product bundle seams are dormant"))
      nil)))

(deftest sh07-b51-v18-private-root1-builds-nonlegacy-and-no-form-plans
  (let [definitions (definitions-by-name @source-forms)
        root1-private? (contains? definitions 'sh07-b51-build-template-private)
        supported-walker-function 'sh07-b51-dependency-entry-active?
        mechanical-operations
        (function-operation-records @checked-core-plan
                                    supported-walker-function)
        reference-operations
        (bootstrap/p15-s23-reference-runtime-function-operation-records
         (path source-relative-path) :jvm supported-walker-function
         (get-in @checked-core-plan
                 [:functions supported-walker-function]))
        call-signature
        (fn [operation]
          (select-keys operation [:op :callee :builtin]))
        measured-final-h-carrier
        (raw-carrier "accepted" "nested-empty-wildcard-let" ".gravity")
        success-cases
        [["ordered-no-form-waiver"
          :b51-vector-frontier-rejected-after-no-form-waiver]]
        tier3-through-tier6
        [:sh07-b51-core-identity-id
         :sh07-b51-predecessor-observation-id
         :sh07-b51-provenance-binding-id
         :sh07-b51-independent-verifier-binding-id
         :sh07-b51-final-artifact-binding-id]]
    (is (= 19 (count purpose-order)))
    (is (= 58 (reduce + (vals dependency-counts))))
    (is (= (frequencies
            (map call-signature
                 (filter #(contains? #{:function-call :builtin-call}
                                     (:op %))
                         reference-operations)))
           (frequencies (map call-signature mechanical-operations))))
    (is (every? #(contains? #{:function-call :builtin-call} (:op %))
                mechanical-operations))
    (is (false?
         (invoke 'sh07-b51-raw-observation-shape?
                 [(:predecessor-raw-outcome measured-final-h-carrier)
                  (:authenticated-sh06-request measured-final-h-carrier)])))
    (is (false?
         (invoke 'sh07-b51-raw-carrier-shape? [measured-final-h-carrier])))
    (let [gated
          (invoke 'sh07-b51-build-template-private
                  [measured-final-h-carrier])]
      (is (exact-boundary?
           gated :template-boundary-rejected
           :sh07-b51-build-template :malformed-invocation)
          gated))
    (if root1-private?
      (do
        (doseq [[basename expected-outcome] success-cases]
          (let [carrier
                (private-no-form-raw-carrier basename)
                built (invoke 'sh07-b51-build-template-private [carrier])
                template (get-in built [:value :template])
                requests (get-in built [:value :digest-requests])
                products (select-keys template product-key-order)
                plan (invoke 'sh07-b51-success-plan-exact? [requests])
                outcome (invoke 'sh07-b51-success-outcome-kind [requests])
                active-edge-count
                (reduce
                 +
                 (for [purpose purpose-order]
                   (count
                    (filterv
                     #(invoke 'sh07-b51-dependency-entry-active?
                              [% outcome])
                     (invoke 'sh07-b51-purpose-dependencies [purpose])))))]
            (is (exact-abi? built :accepted :template-built)
                [basename built])
            (is (= #{:template :digest-requests}
                   (set (keys (:value built))))
                basename)
            (is (= 51 (count template)) basename)
            (is (= (set template-key-order) (set (keys template))) basename)
            (is (= 41 (count products)) basename)
            (is (= (set product-key-order) (set (keys products))) basename)
            (is (= expected-outcome (:predecessor-outcome-kind template))
                basename)
            (is (= expected-outcome outcome) basename)
            (is (= requests (:digest-requests template)) basename)
            (is (= 40 (count requests)) basename)
            (is (= (vec (range (count requests))) (mapv :ordinal requests))
                basename)
            (is (= :accepted (:status plan)) [basename plan])
            (is (= 58 (:dependency-edge-count plan)) [basename plan])
            (is (= 174 (:reference-path-census plan)) [basename plan])
            (is (= 56 active-edge-count) [basename active-edge-count])
            (is (true?
                 (invoke 'sh07-b51-success-plan-reference-dag-valid?
                         [requests expected-outcome]))
                basename)
            (is (true?
                 (invoke 'sh07-b51-template-shape-for-plan?
                         [template requests]))
                basename)
            (is (true?
                 (invoke 'sh07-b51-tier3-record-request-joins?
                         [requests products expected-outcome]))
                basename)
            (is (nil? (:legacy-v16-equivalence template)) basename)
            (is (pos? (invoke 'sh07-b51-purpose-count
                              [requests :sh07-b51-core-node-id]))
                basename)
            (is (seq (:nodes template)) basename)
            (doseq [purpose tier3-through-tier6]
              (let [selected
                    (invoke 'sh07-b51-purpose-request [requests purpose])]
                (is (= 1 (:count selected)) [basename purpose selected])
                (is (true?
                     (invoke 'sh07-b51-success-purpose-preimage-shape?
                             [purpose (get-in selected
                                              [:request :preimage])]))
                    [basename purpose selected])))))
        (let [carrier
              (dissoc
               (private-no-form-raw-carrier "ordered-no-form-waiver")
               :authenticated-sh06-verification-report)
              rejected
              (invoke 'sh07-b51-build-template-private [carrier])
              boundary (:value rejected)]
          (is (exact-boundary?
               rejected :template-boundary-rejected
               :sh07-b51-build-template :malformed-invocation)
              rejected)
          (is (not (contains? (:value rejected) :template)) rejected)
          (is (not (contains? (:value rejected) :digest-requests)) rejected)
          (is (not (contains? (:value rejected) :partial-products)) rejected)
          (is (= :malformed-invocation (:reason boundary)) boundary)))
      (is true "private root1 integration is dormant"))))

(deftest sh07-b51-v18-public-root1-root2-rebuild-governed-template-exactly
  (let [definitions (definitions-by-name @source-forms)
        public-ready?
        (every? #(contains? definitions %)
                '[sh07-b51-build-template sh07-b51-verify-template])]
    (if public-ready?
      (let [carrier
            (private-no-form-raw-carrier "ordered-no-form-waiver")
            private (invoke 'sh07-b51-build-template-private [carrier])
            root1 (invoke 'sh07-b51-build-template [carrier])
            template (get-in root1 [:value :template])
            requests (get-in root1 [:value :digest-requests])
            root2
            (invoke 'sh07-b51-verify-template [carrier template requests])
            private-root2
            (invoke 'sh07-b51-verify-template-private
                    [carrier template requests])
            passed-verification
            {:artifact :gravity/sh07-b51-template-verification-v18
             :schema-version 18 :status :passed :rule nil :reason nil
             :details {}}
            measured-final-h-carrier
            (raw-carrier "accepted" "nested-empty-wildcard-let" ".gravity")
            mutated-carrier
            (dissoc carrier :authenticated-sh06-verification-report)
            wrong-ordinal
            (assoc-in requests [0 :ordinal] 1)
            reordered (vec (reverse requests))
            family-substitution
            (assoc template :calls (:nodes template))
            mutations
            [[:template-missing-family (dissoc template :nodes) requests]
             [:template-extra-family (assoc template :forged-family [])
              requests]
             [:request-presentation template reordered]
             [:family-substitution family-substitution requests]
             ;; These two mutations are coordinated across both arguments;
             ;; Root2 must reconstruct the authenticated plan rather than
             ;; accept byte-consistent attacker-supplied template/request data.
             [:coordinated-reorder
              (assoc template :digest-requests reordered) reordered]
             [:coordinated-wrong-ordinal
              (assoc template :digest-requests wrong-ordinal) wrong-ordinal]]]
        (is (= private root1) [private root1])
        (is (exact-abi? root1 :accepted :template-built) root1)
        (is (= #{:template :digest-requests}
               (set (keys (:value root1))))
            root1)
        (is (= 51 (count template)))
        (is (= (set template-key-order) (set (keys template))))
        (is (= 40 (count requests)))
        (is (= requests (:digest-requests template)))
        (is (= (vec (range 40)) (mapv :ordinal requests)))
        (is (exact-abi? root2 :accepted :template-verified) root2)
        (is (= private-root2 root2) [private-root2 root2])
        (is (= passed-verification (:value root2)) root2)
        ;; Public Root2's fixed passed record is evidence of an exact rebuild,
        ;; while byte parity to the independently constructed private Root1
        ;; pins the supplied successful template and request stream.
        (is (= (get-in private [:value :template]) template))
        (is (= (get-in private [:value :digest-requests]) requests))
        (doseq [[label mutated-template mutated-requests] mutations]
          (let [rejected
                (invoke 'sh07-b51-verify-template
                        [carrier mutated-template mutated-requests])]
            (is (exact-boundary?
                 rejected :template-verification-boundary-rejected
                 :sh07-b51-verify-template :invocation-mismatch)
                [label rejected])
            (is (not (contains? (:value rejected) :partial-products))
                [label rejected])))
        (doseq [[label rejected-carrier]
                [[:measured-final-h measured-final-h-carrier]
                 [:mutated-governed mutated-carrier]]]
          (let [rejected-root1
                (invoke 'sh07-b51-build-template [rejected-carrier])
                rejected-root2
                (invoke 'sh07-b51-verify-template
                        [rejected-carrier template requests])]
            (if (invoke 'sh07-b51-raw-carrier-shape? [rejected-carrier])
              (is (contains? #{:accepted :pending-rejection
                               :boundary-rejected}
                             (:status rejected-root1))
                  [label :post-h rejected-root1])
              (is (exact-boundary?
                   rejected-root1 :template-boundary-rejected
                   :sh07-b51-build-template :malformed-invocation)
                  [label :h-gated rejected-root1]))
            (is (exact-boundary?
                 rejected-root2 :template-verification-boundary-rejected
                 :sh07-b51-verify-template
                 (if (invoke 'sh07-b51-raw-carrier-shape?
                             [rejected-carrier])
                   :invocation-mismatch
                   :malformed-invocation))
                [label rejected-root2]))))
      (is true "public Root1/Root2 exact rebuild gate is dormant"))))

(deftest sh07-b51-v18-public-root4-root5-resolve-and-reverify-governed-plan
  (let [definitions (definitions-by-name @source-forms)
        ready?
        (every? #(contains? definitions %)
                '[sh07-b51-build-template
                  sh07-b51-resolve-digest-preimage
                  sh07-b51-resolve-template-private
                  sh07-b51-resolve-template
                  sh07-b51-verify-resolved-private
                  sh07-b51-verify-resolved])]
    (if ready?
      (let [carrier
            (private-no-form-raw-carrier "ordered-no-form-waiver")
            root1 (invoke 'sh07-b51-build-template [carrier])
            template (get-in root1 [:value :template])
            requests (get-in root1 [:value :digest-requests])
            outcome (invoke 'sh07-b51-success-outcome-kind [requests])
            plan (invoke 'sh07-b51-success-plan-exact? [requests])
            controlled-count
            (invoke 'sh07-b51-reference-registry-generated-count [outcome])
            resolved (resolve-digest-plan requests)
            private-root4
            (invoke 'sh07-b51-resolve-template-private
                    [template requests resolved])
            root4
            (invoke 'sh07-b51-resolve-template
                    [template requests resolved])
            bundle (:value root4)
            core (:resolved-core bundle)
            observation (:predecessor-observation bundle)
            provenance (:provenance-binding bundle)
            private-root5
            (invoke 'sh07-b51-verify-resolved-private
                    [carrier core requests resolved])
            root5
            (invoke 'sh07-b51-verify-resolved
                    [carrier core requests resolved])
            passed
            {:artifact :gravity/sh07-b51-resolved-verification-v18
             :schema-version 18 :status :passed :rule nil}
            identity
            (resolved-id-for-purpose
             requests resolved :sh07-b51-core-identity-id)
            changed-resolved
            (assoc resolved (:ordinal identity) (sha \f))
            changed-core
            (assoc core :semantic-identity-id (sha \f))
            request-rank-mutation
            (assoc-in requests [0 :batch-rank]
                      (inc (get-in requests [0 :batch-rank])))
            root4-mutations
            [[:resolved-count :digest-mismatch (vec (butlast resolved))]
             [:resolved-order :digest-mismatch (vec (reverse resolved))]
             [:resolved-id :digest-mismatch (assoc resolved 0 (sha \f))]
             [:resolved-vector :malformed-invocation {}]
             [:resolved-internal-reference :digest-mismatch
              (assoc resolved 0 (internal-reference 0))]]
            root5-mutations
            [[:core-missing-field :invocation-mismatch
              (dissoc core :nodes) requests resolved]
             [:core-extra-field :invocation-mismatch
              (assoc core :forged true) requests resolved]
             [:core-family-substitution :invocation-mismatch
              (assoc core :calls (:nodes core)) requests resolved]
             [:request-count :invocation-mismatch
              core (vec (butlast requests)) resolved]
             [:request-order :invocation-mismatch
              core (vec (reverse requests)) resolved]
             [:request-rank :invocation-mismatch
              core request-rank-mutation resolved]
             [:resolved-count :digest-mismatch
              core requests (vec (butlast resolved))]
             [:resolved-order :digest-mismatch
              core requests (vec (reverse resolved))]
             [:resolved-id :digest-mismatch
              core requests (assoc resolved 0 (sha \f))]
             [:resolved-vector :malformed-invocation core requests {}]
             ;; Even a coordinated identity substitution must fail because
             ;; Root5 independently rebuilds Root1 and Root4.
             [:coordinated-core-and-digest :digest-mismatch
              changed-core requests changed-resolved]]]
        (is (exact-abi? root1 :accepted :template-built) root1)
        (is (= 40 (count requests)))
        (is (= :accepted (:status plan)) plan)
        (is (= 174 (:reference-path-census plan)))
        (is (= 174 controlled-count))
        (is (= 40 (count resolved)))
        (is (every? #(invoke 'sh07-b51-sha256-id? [%]) resolved))
        (is (= private-root4 root4) [private-root4 root4])
        (is (exact-abi? root4 :accepted :template-resolved) root4)
        (is (= #{:resolved-core :predecessor-observation
                 :provenance-binding}
               (set (keys bundle))))
        (is (= 51 (count core)))
        (is (= (set resolved-core-key-order) (set (keys core))))
        (is (true? (invoke 'sh07-b51-resolved-core-shape? [core])))
        (is (empty? (internal-reference-occurrences core)))
        (is (= (:observation tier4-keysets) (set (keys observation))))
        (is (= (:provenance tier4-keysets) (set (keys provenance))))
        ;; Registered paths materialize, while the raw authenticated carrier
        ;; and all opaque reference-shaped B47 metadata remain byte-identical.
        (is (= carrier (get-in observation [:preimage :raw-adapter-request])))
        (is (= (bootstrap/reader-canonical-hash carrier)
               (bootstrap/reader-canonical-hash
                (get-in observation [:preimage :raw-adapter-request]))))
        (is (= (internal-reference-occurrences carrier)
               (internal-reference-occurrences
                (get-in observation [:preimage :raw-adapter-request]))))
        (is (= private-root5 root5) [private-root5 root5])
        (is (exact-abi? root5 :accepted :resolved-verified) root5)
        (is (= passed (:value root5)) root5)
        (doseq [[label reason mutated-resolved] root4-mutations]
          (let [rejected
                (invoke 'sh07-b51-resolve-template
                        [template requests mutated-resolved])]
            (is (exact-boundary?
                 rejected :template-resolution-boundary-rejected
                 :sh07-b51-resolve-template reason)
                [label rejected])))
        (doseq [[label reason mutated-core mutated-requests mutated-resolved]
                root5-mutations]
          (let [rejected
                (invoke 'sh07-b51-verify-resolved
                        [carrier mutated-core mutated-requests
                         mutated-resolved])]
            (is (exact-boundary?
                 rejected :resolved-verification-boundary-rejected
                 :sh07-b51-verify-resolved reason)
                [label rejected])))
        ;; Root4 proves only self-consistency of its three supplied values.
        ;; When the second governed carrier clears its current H gate, its
        ;; independently canonical tuple may pass Root4, but Root5 must reject
        ;; that tuple against the fixed first raw carrier.
        (let [alternate-carrier
              (private-no-form-raw-carrier
               "ordered-no-form-simple-symbol-waiver")
              alternate-root1
              (invoke 'sh07-b51-build-template [alternate-carrier])]
          (if (exact-abi? alternate-root1 :accepted :template-built)
            (let [alternate-template
                  (get-in alternate-root1 [:value :template])
                  alternate-requests
                  (get-in alternate-root1 [:value :digest-requests])
                  alternate-resolved
                  (resolve-digest-plan alternate-requests)
                  alternate-root4
                  (invoke 'sh07-b51-resolve-template
                          [alternate-template alternate-requests
                           alternate-resolved])
                  cross-root5
                  (invoke 'sh07-b51-verify-resolved
                          [carrier
                           (get-in alternate-root4
                                   [:value :resolved-core])
                           alternate-requests alternate-resolved])]
              (is (exact-abi? alternate-root4 :accepted
                              :template-resolved)
                  alternate-root4)
              (is (exact-boundary?
                   cross-root5 :resolved-verification-boundary-rejected
                   :sh07-b51-verify-resolved :invocation-mismatch)
                  cross-root5))
            (is (contains? #{:pending-rejection :boundary-rejected}
                           (:status alternate-root1))
                [:alternate-tuple-h-gated alternate-root1]))))
      (is true "public Root4/Root5 governed pipeline is dormant"))))

(deftest sh07-b51-v18-root7-assembly-and-boundary-seams-are-exact
  (let [definitions (definitions-by-name @source-forms)
        root7 (get definitions 'sh07-b51-build-final-artifact)
        closure
        (source-definition-closure
         definitions 'sh07-b51-build-final-artifact)
        closure-members (set (:members closure))
        closure-values
        (set (tree-seq coll? seq (mapv definitions closure-members)))
        runtime (direct-source-runtime 'sh07-b51-build-final-artifact)
        invoke-direct #((:invoke runtime) %1 %2)
        core
        (merge
         {:artifact :gravity/sh07-b51-resolved-core-v18
          :schema-version 18
          :predecessor-outcome-kind
          :b51-vector-frontier-rejected-after-no-form-waiver
          :same-request-semantic-id (sha \1)
          :predecessor-authority-id (sha \2)
          :module {} :lineage {} :projection-binding (sha \3)
          :semantic-identity-id (sha \4) :diagnostics []}
         (zipmap product-key-order (repeat [])))
        provenance-id (sha \5)
        verifier-id (sha \6)
        final-id (sha \7)
        artifact
        (invoke-direct 'sh07-b51-r7-canonical-artifact
                       [core provenance-id verifier-id final-id])
        artifact-keys
        (invoke-direct 'sh07-b51-r7-canonical-artifact-keys [])]
    (is (= {:arity 7
            :params '[raw-carrier resolved-core predecessor-observation
                      provenance-binding verifier-binding digest-requests
                      resolved-digests]}
           (function-shape root7)))
    (is (empty?
         (filter
          #(or (str/includes? (name %) "finalize-rejection")
               (str/includes? (name %) "build-independent-verifier")
               (str/includes? (name %) "canonical-text-compare")
               (str/includes? (name %) "observe-utf8")
               (str/includes? (name %) "producer-decimal")
               (str/includes? (name %) "r6-decimal"))
          closure-members))
        closure-members)
    (is (empty?
         (set/intersection
          closure-values
          #{:pending-rejection :template-rejected
            :independent-verifier-rejected :final-artifact-rejected
            :gravity/sh07-b51-rejected-envelope-v18})))
    (is (= 57 (count artifact) (count artifact-keys)))
    (is (= artifact-keys (set (keys artifact))))
    (is (= [:gravity/sh07-b51-canonical-core-artifact-v18
            :gravity/sh07-b51-canonical-core-artifact-v18
            18 :accepted "SH-07-B51" nil]
           [(:artifact artifact) (:artifact-kind artifact)
            (:schema-version artifact) (:status artifact) (:task artifact)
            nil]))
    (is (= [(:semantic-identity-id core) provenance-id verifier-id final-id]
           [(:semantic-identity-id artifact)
            (:provenance-binding-id artifact)
            (:independent-verifier-binding-id artifact)
            (:final-artifact-binding-id artifact)]))
    (doseq [key product-key-order]
      (is (= (get core key) (get artifact key)) key))
    (is (true?
         (invoke-direct 'sh07-b51-r7-canonical-artifact-exact?
                        [artifact core provenance-id verifier-id final-id])))
    (doseq [mutated [(dissoc artifact :nodes)
                     (assoc artifact :forged true)
                     (assoc artifact :task "forged")
                     (assoc artifact :final-artifact-binding-id (sha \f))]]
      (is (false?
           (invoke-direct 'sh07-b51-r7-canonical-artifact-exact?
                          [mutated core provenance-id verifier-id final-id]))))
    (is (exact-boundary?
         (invoke-direct 'sh07-b51-build-final-artifact
                        [{} {} {} {} {} [] []])
         :final-artifact-boundary-rejected
         :sh07-b51-build-final-artifact :malformed-invocation))
    (doseq [reason [:malformed-invocation
                    :invocation-mismatch :digest-mismatch]]
      (let [boundary
            (invoke-direct 'sh07-b51-r7-boundary [reason])]
        (is (exact-boundary?
             boundary :final-artifact-boundary-rejected
             :sh07-b51-build-final-artifact reason)
            [reason boundary])))))

(def root6-full-sh06-check-keys
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
    :binding-table-current? :alias-table-current? :import-export-current?
    :lexical-scope-current? :dependency-graph-current?
    :cross-profile-current? :invalidation-current?
    :resolution-table-current? :artifact-identity-current?
    :fresh-sh02-envelope-verified? :exact-artifact-shape?
    :exact-boundary-shape? :fixed-boundary-contract?
    :pass-contract-current? :execution-boundary-current?
    :component-transport-bounds-current?
    :aggregate-artifact-bounds-current? :provenance-current?
    :accepted-diagnostics-empty? :embedded-capability-proof-current?})

(defn with-root6-full-sh06-evidence
  "Replace only SH06 evidence in a synthetic Root6 carrier with the literal
  full schema accepted by the current Root6 freshness predicate."
  [carrier]
  (let [request (:authenticated-sh06-request carrier)
        source-path (get-in request [:provenance :actual-source-path])
        artifact-id (get-in request
                            [:lineage :authenticated-sh06-artifact-id])
        revision-id (get-in request [:lineage :source-revision-id])
        sh05-id (or (get-in request [:lineage :sh05-artifact-id]) (sha \4))
        checks (zipmap root6-full-sh06-check-keys (repeat true))
        raw-module (assoc (:module request)
                          :package (get-in request [:module :namespace])
                          :c4-artifact-id sh05-id)
        raw-request
        {:artifact :gravity/sh06-authenticated-c4-resolution-request
         :schema-version 1 :module raw-module :definitions [] :imports []
         :import-bindings [] :lexical-scopes {} :references []
         :core-bindings [] :dependency-edges []
         :macro-expansion-binding {}
         :provenance {:actual-source-path source-path}}
        analysis
        {:module-contract raw-module :resolution-table [] :binding-table []
         :alias-table [] :dependency-records [] :exports []
         :lexical-scope-graph {} :macro-expansion-binding {}
         :dependency-graph {:edges []} :cross-profile-edge-report []
         :incremental-invalidation-inputs []}
        boundary
        {:slice :SH-06 :owner :gravity-source
         :adapter-contract :gravity/sh06-resolution-adapter-contract
         :plan-binding {} :authenticated-resolution-request raw-request
         :raw-template-result {} :raw-analysis {}
         :resolved-analysis analysis :digest-requests []
         :resolved-digests [] :template-verification {:status :passed}
         :resolved-verification {:status :passed}
         :authenticated-envelope-descriptor {} :authenticated-envelope {}
         :target-source-reread? false :clojure-adapter-residual? true
         :self-hosted? false}
        proof (assoc checks
                     :artifact :gravity/sh06-resolution-capability-proof
                     :status :complete :failed-checks [])
        report
        {:artifact :gravity/sh06-resolution-artifact-verification
         :status :passed :checks checks :failed-checks []
         :source-path source-path :gravity-verification {:status :passed}
         :upstream-verification {:status :passed}}
        c2-reader-result-id (sha \1)
        c2-semantic-table-id (sha \2)
        projection-base
        {:artifact :gravity/sh03-c2-decimal-occurrence-projection
         :schema-version 1 :reader-result-id c2-reader-result-id
         :semantic-value-table-id c2-semantic-table-id
         :adapted-source-unit-id revision-id
         :adapted-token-stream-id (sha \3)
         :adapted-form-tree-id (sha \4)
         :token-id-projection-id (sha \5)
         :form-id-projection-id (sha \6)
         :occurrence-count 0 :occurrences []}
        projection
        (assoc projection-base :projection-id
               (bootstrap/reader-canonical-hash
                (assoc (dissoc projection-base :artifact :schema-version)
                       :domain
                       :gravity/sh03-c2-decimal-occurrence-projection-v1)))
        summary
        {:slice :SH-03 :status :accepted
         :adapter-contract :gravity/sh03-to-c2-reader-products-v3
         :source-unit-id (sha \7) :token-stream-id (sha \8)
         :form-tree-id (sha \9) :extension-invocation-set-id (sha \a)
         :reader-result-id c2-reader-result-id
         :semantic-value-table-id c2-semantic-table-id
         :adapted-source-unit-id revision-id
         :adapted-token-stream-id (:adapted-token-stream-id projection)
         :adapted-form-tree-id (:adapted-form-tree-id projection)
         :adapted-extension-invocation-set-id (sha \b)
         :token-id-projection-id (:token-id-projection-id projection)
         :form-id-projection-id (:form-id-projection-id projection)
         :decimal-occurrence-projection-id (:projection-id projection)}
        c2
        {:source-unit-record {:source-id revision-id :bytes-hash revision-id}
         :literal-decoding-records [] :form-tree []
         :semantic-error-deferment-record {:deferred-literal-records []}
         :sh07-decimal-occurrence-projection projection
         :gravity-reader-boundary
         {:adapter-contract :gravity/sh03-to-c2-reader-products-v3
          :decimal-occurrence-projection-id (:projection-id projection)
          :semantic-value-table-id c2-semantic-table-id
          :resolved-reader-result
          {:incremental-reader-hashes {:reader-result c2-reader-result-id}}
          :authenticated-envelope-descriptor
          {:semantic-projections
           [{:name :reader-product-identities :value summary}]}}}
        artifact
        {:kind :gravity/sh06-resolution-artifact :status :accepted
         :slice :SH-06 :task "SH-06" :document-set ["L3" "C5"]
         :governing-document "C5" :artifact-id artifact-id
         :sh05-macro-artifact
         {:artifact-id sh05-id :expanded-syntax-stream []
          :macro-expansion-trace []
          :gravity-macro-boundary
          {:authenticated-sh04-artifact
           {:c2-reader-artifact c2}}}
         :gravity-resolution-boundary boundary
         :provenance {:source-path source-path
                      :source-revision-id revision-id}
         :pass {} :execution-boundary {} :capability-based-proof proof
         :diagnostics []
         :namespace-analysis
         (assoc analysis :artifact :gravity/namespace-analysis)
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
         :resolution-table []}]
    (assoc carrier :authenticated-sh06-verification-report report
           :sh06-resolution-artifact artifact)))

(deftest sh07-b51-v18-root6-contract-and-check-order-are-exact
  (is (= 21 (count root6-check-order)))
  (is (= 21 (count (set root6-check-order))))
  (is (= (vec (range 21))
         (mapv :ordinal
               (map-indexed (fn [ordinal check]
                              {:ordinal ordinal :check check :status :passed})
                            root6-check-order))))
  (is (= root6-check-order
         (mapv :check
               (map-indexed (fn [ordinal check]
                              {:ordinal ordinal :check check :status :passed})
                            root6-check-order))))
  (is (= {:arity 6
          :params '[raw-carrier resolved-core predecessor-observation
                    provenance-binding digest-requests resolved-digests]}
         (get public-shapes
              'sh07-b51-build-independent-verifier-binding)))
  (is (= [:accepted :independent-verifier-bound]
         (get-in public-abi-contract
                 ['sh07-b51-build-independent-verifier-binding :success])))
  (is (= [:boundary-rejected :independent-verifier-boundary-rejected]
         (get-in public-abi-contract
                 ['sh07-b51-build-independent-verifier-binding :boundary])))
  (is (= #{:malformed-invocation :invocation-mismatch :digest-mismatch}
         (set (get-in detector-boundary-contract
                      ['sh07-b51-build-independent-verifier-binding
                       :reasons]))))
  (is (= 9 (count root6-preimage-keys)))
  (is (= 4 (count root6-record-keys))))

(deftest sh07-b51-v18-root6-source-is-authored-definitions-only
  (let [forms @source-forms
        predecessor-index
        (first
         (keep-indexed
          (fn [index form]
            (when (and (seq? form)
                       (= 'sh07-b51-verify-resolved (second form)))
              index))
          forms))
        root6-index (when (integer? predecessor-index)
                      (inc predecessor-index))
        root6-forms (if (integer? root6-index) (subvec forms root6-index) [])]
    (is (integer? predecessor-index))
    (is (every? #(and (seq? %) (= 'defn (first %))) root6-forms)
        {:invalid
         (mapv (fn [[index form]] [(+ root6-index index) form])
               (filter (fn [[_ form]]
                         (not (and (seq? form) (= 'defn (first form)))))
                       (map-indexed vector root6-forms)))})))

(deftest sh07-b51-v18-root6-cannot-be-a-prefix-renamed-producer-copy
  (let [definitions (definitions-by-name @source-forms)
        prefixed-copies
        (->> (keys definitions)
             (filter #(.startsWith (name %) "sh07-b51-r6-own-"))
             sort vec)
        collisions
        (->> definitions
             (keep
              (fn [[function-name definition]]
                (when (.startsWith (name function-name) "sh07-b51-r6-own-")
                  (let [normalized (rename-r6-copy-symbols definition)
                        producer-name (second normalized)]
                    (when (= normalized (get definitions producer-name))
                      [function-name producer-name])))))
             vec)]
    (is (empty? prefixed-copies)
        {:prefixed-copy-count (count prefixed-copies)
         :sample (vec (take 20 prefixed-copies))})
    (is (empty? collisions)
        {:copy-count (count collisions)
         :sample (vec (take 20 collisions))})))

(deftest sh07-b51-v18-root6-pin-independent-abi-and-real-check-evidence
  (let [definitions (definitions-by-name @source-forms)
        root6 'sh07-b51-build-independent-verifier-binding
        definition (get definitions root6)
        closure (source-definition-closure definitions root6)
        closure-forms (mapv definitions (:members closure))
        closure-values (tree-seq coll? seq closure-forms)
        closure-scalars (set closure-values)
        check-occurrences
        (frequencies (filter (set root6-check-order) closure-values))
        success-runtime (direct-source-runtime 'sh07-b51-r6-success)
        boundary-runtime (direct-source-runtime 'sh07-b51-r6-boundary)
        record
        {:artifact :gravity/sh07-b51-independent-verifier-binding-v18
         :schema-version 18
         :independent-verifier-binding-id (sha \a)
         :preimage
         {:domain :gravity/sh07-b51-independent-verifier-binding-preimage-v18
          :schema-version 18
          :verifier :sh07-b51-independent-verifier-v18
          :verifier-entrypoint root6
          :semantic-identity-id (sha \b)
          :provenance-binding-id (sha \c)
          :check-order root6-check-order
          :checks [] :status :passed}}
        accepted ((:invoke success-runtime) 'sh07-b51-r6-success [record])]
    (is (= {:arity 6
            :params '[raw-carrier resolved-core predecessor-observation
                      provenance-binding digest-requests resolved-digests]}
           (function-shape definition)))
    (is (= #{:artifact :domain :schema-version :status :tag :value}
           (set (keys accepted))))
    (is (= [:accepted :independent-verifier-bound record]
           [(:status accepted) (:tag accepted) (:value accepted)]))
    (doseq [reason [:malformed-invocation
                    :invocation-mismatch :digest-mismatch]]
      (let [boundary
            ((:invoke boundary-runtime) 'sh07-b51-r6-boundary [reason])]
        (is (exact-boundary?
             boundary :independent-verifier-boundary-rejected
             :sh07-b51-build-independent-verifier-binding reason)
            [reason boundary])))
    (is (empty?
         (set/intersection
          #{:pending-rejection :template-rejected
            :independent-verifier-rejected
            :gravity/sh07-b51-pending-envelope-v18}
          closure-scalars))
        closure-scalars)
    ;; A zero-argument row generator can only stamp :passed onto the public
    ;; order.  Governed Root6 must instead construct every row from evidence
    ;; evaluated for the supplied raw/digest tuple.
    (is (not (contains? definitions 'sh07-b51-r6-checks)))
    (doseq [check root6-check-order]
      (is (<= 2 (get check-occurrences check 0))
          [check (get check-occurrences check 0)]))))

(deftest sh07-b51-v18-root6-pin-independent-counts-outcomes-and-tier5-shapes
  (let [definitions (definitions-by-name @source-forms)
        root6 'sh07-b51-build-independent-verifier-binding
        closure (source-definition-closure definitions root6)
        closure-values
        (tree-seq coll? seq (mapv definitions (:members closure)))
        closure-scalars (set closure-values)
        purpose-runtime (direct-source-runtime 'sh07-b51-r6-purpose-order)
        dependency-runtime
        (direct-source-runtime 'sh07-b51-r6-dependency-catalog-exact?)
        outcome-runtime (direct-source-runtime 'sh07-b51-r6-outcome-kind?)
        path-runtime
        (direct-source-runtime 'sh07-b51-r6-controlled-path-count)
        reference-runtime
        (direct-source-runtime 'sh07-b51-r6-internal-reference?)
        purposes
        ((:invoke purpose-runtime) 'sh07-b51-r6-purpose-order [])
        dependency-count
        ((:invoke dependency-runtime) 'sh07-b51-r6-dependency-edge-count [])
        reference? #((:invoke reference-runtime)
                     'sh07-b51-r6-internal-reference? [%])]
    (is (= purpose-order purposes))
    (is (= 19 (count purposes) (count (set purposes))))
    (is (= 58 dependency-count))
    (is (true?
         ((:invoke dependency-runtime)
          'sh07-b51-r6-dependency-catalog-exact? [])))
    (doseq [outcome root3-outcome-kinds]
      (is (true? ((:invoke outcome-runtime)
                  'sh07-b51-r6-outcome-kind? [outcome]))
          outcome)
      (is (= (get-in root3-reference-census [outcome :controlled])
             ((:invoke path-runtime)
              'sh07-b51-r6-controlled-path-count [outcome]))
          outcome))
    (is (false? ((:invoke outcome-runtime)
                 'sh07-b51-r6-outcome-kind? [:forged-outcome])))
    (is (nil? ((:invoke path-runtime)
               'sh07-b51-r6-controlled-path-count [:forged-outcome])))
    (is (contains? closure-scalars 94))
    (is (contains? closure-scalars 174))
    (is (contains? closure-scalars 65540)
        :aggregate-digest-request-cap-must-be-in-root6-closure)
    (is (true? (reference? (internal-reference 0))))
    (doseq [forged [(assoc (internal-reference 0) :opaque true)
                    (assoc (internal-reference 0) :ordinal -1)
                    (assoc (internal-reference 0) :authority :caller)
                    (assoc (internal-reference 0) :artifact :opaque)]]
      (is (false? (reference? forged)) forged))
    ;; Tier5 authority is an authenticated earlier request occurrence, not a
    ;; caller-supplied hash-shaped scalar or opaque recursive substitution.
    (is (contains? closure-scalars
                   :sh07-b51-independent-verifier-binding-id))
    (is (contains? closure-scalars
                   :gravity/sh07-internal-digest-reference))))

(deftest sh07-b51-v18-root6-typed-path-registry-is-exact-and-mutation-sensitive
  (let [runtime (direct-source-runtime 'sh07-b51-r6-path-registry-exact?)
        invoke #((:invoke runtime) %1 %2)
        registry #(invoke 'sh07-b51-r6-path-registry [%])
        exact? #(invoke 'sh07-b51-r6-path-registry-exact? [%])
        dependency-target
        (fn [entry] (if (map? entry) (first (vals entry)) entry))
        active-edge?
        (fn [outcome source target]
          (and (contains?
                (set (map dependency-target
                          (get root3-dependency-targets source)))
                target)
               (if (= target :sh07-b51-v16-verification-semantic-id)
                 (= outcome :legacy-v16-accepted)
                 (if (= target :sh07-b51-core-node-id)
                   (not= outcome :legacy-v16-accepted)
                   true))))
        independently-exact?
        (fn [outcome rows]
          (and (vector? rows)
               (= (get-in root3-reference-census [outcome :controlled])
                  (count rows))
               (= (count rows)
                  (count (set (map (juxt :source-purpose :path) rows))))
               (every?
                (fn [row]
                  (and (= #{:source-purpose :path :target-purpose}
                          (set (keys row)))
                       (contains? (set purpose-order) (:source-purpose row))
                       (contains? (set purpose-order) (:target-purpose row))
                       (vector? (:path row))
                       (seq (:path row))
                       (active-edge? outcome (:source-purpose row)
                                     (:target-purpose row))))
                rows)))]
    (doseq [outcome root3-outcome-kinds]
      (let [rows (registry outcome)
            first-row (first rows)]
        (is (true? (exact? outcome)) [outcome :production-exact])
        (is (true? (independently-exact? outcome rows))
            [outcome :independent-census])
        ;; These mutations exercise every component of the typed registry key
        ;; [outcome source-purpose path target-purpose], plus cardinality and
        ;; uniqueness.  The oracle is test-local and does not call the source
        ;; registry validator.
        (doseq [[label changed]
                [[:missing-row (vec (rest rows))]
                 [:duplicate-row (conj rows first-row)]
                 [:row-extra-key
                  (assoc rows 0 (assoc first-row :forged true))]
                 [:empty-path (assoc-in rows [0 :path] [])]
                 [:wrong-source
                  (assoc-in rows [0 :source-purpose]
                            :sh07-b51-implementation-semantic-id)]
                 [:wrong-target
                  (assoc-in rows [0 :target-purpose]
                            :sh07-b51-final-artifact-binding-id)]]]
          (is (false? (independently-exact? outcome changed))
              [outcome label]))))))

(deftest sh07-b51-v18-root6-direct-prefix-and-digest-replay-mutations
  (let [runtime (direct-source-runtime 'sh07-b51-r6-replay-digests)
        invoke-direct #((:invoke runtime) %1 %2)
        request
        (fn [ordinal purpose domain subtier batch rank preimage]
          {:ordinal ordinal :tier 0 :subtier subtier
           :subtier-ordinal ordinal :batch batch :batch-rank rank
           :batch-ordinal 0 :purpose purpose
           :preimage (assoc preimage :domain domain)})
        requests
        [(request 0 :sh07-b51-implementation-semantic-id
                  :gravity/sh07-b51-implementation-semantic-id-preimage-v18
                  :0a :0a-implementation 0 {})
         (request 1 :sh07-b51-contract-semantic-id
                  :gravity/sh07-b51-contract-semantic-id-preimage-v18
                  :0a :0a-contract 1 {})
         (request 2 :sh07-b51-same-request-semantic-id
                  :gravity/sh07-b51-same-request-binding-v18
                  :0a :0a-same-request 2 {})
         (request 3 :sh07-b51-v16-verification-semantic-id
                  :gravity/sh07-b51-v16-verification-semantic-v18
                  :0a :0a-v16-verification 3 {})
         (request
          4 :sh07-b51-predecessor-outcome-semantic-id
          :gravity/sh07-b51-predecessor-outcome-semantic-v18
          :0b :0b-outcome 4
          {:outcome-kind :legacy-v16-accepted
           :implementation-semantic-id (internal-reference 0)
           :contract-semantic-id (internal-reference 1)
           :same-request-semantic-id (internal-reference 2)
           :v16-verification-semantic-id (internal-reference 3)})]
        resolved
        (mapv
         (fn [item]
           (bootstrap/reader-canonical-hash
            {:domain :gravity/sh07-declared-digest-v1
             :purpose (:purpose item) :preimage (:preimage item)}))
         requests)
        replay #(invoke-direct 'sh07-b51-r6-replay-digests [%1 %2])
        prefix? #(invoke-direct 'sh07-b51-r6-request-prefix-authentic? %)]
    (is (true? (replay requests resolved)))
    (is (true? (prefix? [(last requests)
                          :legacy-v16-accepted (vec (butlast resolved))])))
    (is (false? (prefix? [(last requests)
                           :legacy-v16-accepted (vec (drop-last 2 resolved))])))
    (is (false? (prefix? [(assoc-in (last requests)
                                    [:preimage :same-request-semantic-id
                                     :ordinal] 4)
                           :legacy-v16-accepted (vec (butlast resolved))])))
    ;; The governed DAG edge is implementation -> contract.  A contract
    ;; preimage that points backward to implementation is not licensed merely
    ;; because ordinal zero is already in the resolved prefix.
    (is (false?
         (prefix?
          [(assoc-in (second requests)
                     [:preimage :implementation-semantic-id]
                     (internal-reference 0))
           :legacy-v16-accepted [(first resolved)]]))
        :contract-to-implementation-counterexample)
    (is (false? (replay requests (assoc resolved 2 (sha \f)))))
    (is (false? (replay requests (vec (reverse resolved)))))
    (is (false? (replay (assoc-in requests [4 :preimage :opaque]
                                  (internal-reference 0))
                        resolved)))))

(deftest sh07-b51-v18-root6-record-request-join-is-exact-sans-own-id
  (let [runtime
        (direct-source-runtime 'sh07-b51-r6-record-request-authentic?)
        vector-runtime
        (direct-source-runtime 'sh07-b51-r6-record-vector-authentic?)
        authentic?
        #((:invoke runtime) 'sh07-b51-r6-record-request-authentic? %)
        vector-authentic?
        #((:invoke vector-runtime) 'sh07-b51-r6-record-vector-authentic? %)
        purpose :sh07-b51-binding-slot-id
        domain :gravity/sh07-b51-binding-slot-id-preimage-v18
        reference (internal-reference 0)
        requests
        [{:purpose purpose
          :preimage {:domain domain :schema-version 18
                     :slot-ordinal 0 :binding-name 'left}}]
        record {:slot-id reference :slot-ordinal 0 :binding-name 'left}]
    (is (true? (authentic?
                [requests purpose :slot-id domain record])))
    (is (false? (authentic?
                 [requests purpose :slot-id domain
                  (assoc record :binding-name 'right)])))
    (is (false? (authentic?
                 [requests purpose :slot-id domain
                  (dissoc record :slot-ordinal)])))
    (is (false? (authentic?
                 [requests purpose :slot-id domain
                  (assoc record :uncommitted-extra true)])))
    ;; Neither a caller-authored request preimage nor the vector projection
    ;; layer may expand the governed per-purpose record schema.  Otherwise the
    ;; same forged key on both sides becomes circular authority.
    (is (false?
         (authentic?
          [[(update (first requests) :preimage assoc
                    :uncommitted-extra true)]
           purpose :slot-id domain
           (assoc record :uncommitted-extra true)]))
        :request-and-record-coherent-extra-key)
    (is (false?
         (vector-authentic?
          [requests purpose :slot-id domain
           [(assoc record :uncommitted-extra true)]]))
        :vector-layer-must-not-project-away-record-extras)
    (is (false? (authentic?
                 [requests purpose :slot-id domain
                  (assoc record :slot-id (internal-reference 1))])))))

(deftest sh07-b51-v18-root6-derives-all-four-predecessor-outcomes-from-raw
  (let [runtime
        (direct-source-runtime 'sh07-b51-r6-derive-predecessor-outcome)
        derive #((:invoke runtime) 'sh07-b51-r6-derive-predecessor-outcome %)
        fixed
        [{:purpose :sh07-b51-implementation-semantic-id :ordinal 0}
         {:purpose :sh07-b51-contract-semantic-id :ordinal 1}
         {:purpose :sh07-b51-same-request-semantic-id :ordinal 2}
         {:purpose :sh07-b51-v16-verification-semantic-id :ordinal 3}]
        common
        {:implementation-semantic-id (internal-reference 0)
         :contract-semantic-id (internal-reference 1)
         :same-request-semantic-id (internal-reference 2)}
        cases
        [{:kind :legacy-v16-accepted
          :outcome (merge common
                          {:outcome-kind :legacy-v16-accepted
                           :status :accepted
                           :v16-verification-semantic-id
                           (internal-reference 3)
                           :v16-semantic-artifact-id (sha \a)})
          :direct {:status :accepted}
          :structural {:canonical-core-artifact {:artifact-id (sha \a)}}}
         {:kind :legacy-v16-accepted-after-no-form-waiver
          :outcome (merge common
                          {:outcome-kind
                           :legacy-v16-accepted-after-no-form-waiver
                           :replay-status :all-other-b47-invariants-passed})
          :direct {:diagnostics
                   [{:facts {:rule-specific
                             {:reason :authoritative-record-shape}}}]}}
         {:kind :b51-vector-frontier-rejected
          :outcome (merge common
                          {:outcome-kind :b51-vector-frontier-rejected
                           :frontier-binding-kind :vector
                           :frontier-rule "C6-LOWERING-GAP"})
          :direct {:status :rejected}}
         {:kind :b51-vector-frontier-rejected-after-no-form-waiver
          :outcome
          (merge common
                 {:outcome-kind
                  :b51-vector-frontier-rejected-after-no-form-waiver
                  :frontier-binding-kind :vector
                  :frontier-rule "C6-LOWERING-GAP"})
          :direct {:status :rejected}}]]
    (doseq [{:keys [kind outcome direct structural]} cases]
      (let [requests
            (conj fixed {:purpose :sh07-b51-predecessor-outcome-semantic-id
                         :ordinal 4 :preimage outcome})
            carrier
            {:predecessor-raw-outcome
             {:direct-template-observation {:returned direct}
              :structural-runner-observation {:returned structural}}}
            accepted (derive [carrier requests])]
        (is (= {:status :accepted :outcome outcome} accepted) kind)
        (is (= {:status :rejected}
               (derive [carrier
                        (assoc-in requests [4 :preimage
                                            :contract-semantic-id :ordinal]
                                  0)]))
            kind)
        (is (= {:status :rejected}
               (derive [(assoc-in carrier
                                  [:predecessor-raw-outcome
                                   :direct-template-observation :returned]
                                  {})
                        requests]))
            kind)))))

(deftest sh07-b51-v18-root6-rejects-coherent-nonlegacy-product-forgery
  (let [runtime (direct-source-runtime 'sh07-b51-r6-reconstruct-products)
        reconstruct
        #((:invoke runtime) 'sh07-b51-r6-reconstruct-products %)
        artifact-id (sha \a) revision-id (sha \b)
        projection-id (sha \c) alias-id (sha \d)
        _form-id (sha \e) _syntax-id (sha \f) fragment-id (sha \9)
        source-path "literal-empty-root6.gravity"
        coverage
        {:root-form-count 0 :form-count 0 :local-binding-count 0
         :resolution-count 0 :fragment-count 1
         :covered-root-form-ids [] :covered-form-ids []
         :covered-local-binding-ids []
         :covered-resolution-reference-syntax-ids []}
        assembly
        {:ordered-fragment-ids [fragment-id] :root-form-ids []
         :source-revision-id revision-id
         :sh06-semantic-projection-id projection-id
         :alias-table-id alias-id :content-id revision-id
         :module-id projection-id}
        request
        {:artifact :gravity/sh07-authenticated-sh06-core-request
         :schema-version 15
         :lineage {:authenticated-sh06-artifact-id artifact-id
                   :source-revision-id revision-id
                   :sh06-semantic-projection-id projection-id
                   :alias-table-id alias-id :sh05-artifact-id (sha \4)
                   :macro-expansion-trace-id (sha \5)}
         :module {:namespace 'root6.fixture :profile :safe :target :jvm
                  :safety :safe :effects #{} :capabilities #{} :exports []
                  :source-revision-id revision-id}
         :forms []
         :top-level-form-ids []
         :binding-table [] :resolution-table []
         :fragment-manifest
         [{:fragment-id fragment-id :root-form-ids []
           :form-ids [] :local-binding-ids []
           :external-binding-ids []
           :resolution-reference-syntax-ids []}]
         :fragment-coverage coverage
         :module-assembly-manifest assembly :alias-table []
         :macro-expansion-trace [] :macro-origin-traces []
         :macro-origin-expectation
         {:source-revision-id revision-id :sh05-artifact-id (sha \4)
          :macro-expansion-trace-id (sha \5) :expanded-defn-count 0
          :expected-input-syntax-ids []
          :expected-output-def-syntax-ids []
          :expected-introduced-fn-syntax-ids []}
         :projection-binding {}
         :provenance {:actual-source-path source-path}
         :scope :sh07-b15-keyword-map-lookup}
        transcript-preimage
        {:domain :gravity/sh07-b51-slot-extraction-transcript-id-preimage-v18
         :schema-version 18 :predecessor-authority-id (internal-reference 50)
         :same-request-semantic-id (internal-reference 51)
         :slot-count 0 :extraction-count 0 :runtime-check-count 0
         :publication-event-count 0 :recur-slot-mapping-count 0
         :slot-ids [] :extraction-ids [] :runtime-check-ids []
         :publication-event-ids [] :recur-slot-mapping-ids []
         :global-slot-range {:first 0 :count 0}
         :global-extraction-range {:first 0 :count 0}
         :global-runtime-check-range {:first 0 :count 0}
         :global-publication-range {:first 0 :count 0}
         :global-recur-range {:first 0 :count 0}}
        transcript
        (assoc (dissoc transcript-preimage :domain :schema-version)
               :schema :gravity/sh07-b51-slot-extraction-transcript-v18
               :transcript-id (internal-reference 0))
        base-products
        (-> (zipmap product-key-order (repeat []))
            (assoc :fragment-manifest
                   [{:fragment-id fragment-id :root-form-ids []
                     :form-ids [] :local-binding-ids []
                     :external-binding-ids []
                     :resolution-reference-syntax-ids []
                     :root-node-ids []}]
                   :fragment-coverage coverage
                   :module-assembly-manifest assembly :binding-table []
                   :declared-alias-table [] :resolution-table []
                   :macro-expansion-trace [] :macro-origin-traces []
                   :macro-origin-expectation
                   {:source-revision-id revision-id
                    :sh05-artifact-id (sha \4)
                    :macro-expansion-trace-id (sha \5)
                    :expanded-defn-count 0
                    :expected-input-syntax-ids []
                    :expected-output-def-syntax-ids []
                    :expected-introduced-fn-syntax-ids []}
                   :pending-fact-families
                   [:types :effects :ownership :safety]
                   :legacy-v16-equivalence nil
                   :slot-extraction-transcript transcript))
        plan
        [{:purpose :sh07-b51-slot-extraction-transcript-id
          :preimage transcript-preimage}
         {:purpose :sh07-b51-core-identity-id
          :preimage
          {:domain :gravity/sh07-b51-core-identity-preimage-v18
           :schema-version 18
           :artifact-kind :gravity/sh07-b51-canonical-core-artifact-v18
           :task "SH-07-B51" :scope :sh07-b51-vector-destructuring
           :predecessor-outcome-kind :b51-vector-frontier-rejected
           :same-request-semantic-id nil :predecessor-authority-id nil
           :module (:module request) :lineage (:lineage request)
           :projection-binding (:projection-binding request)
           :product-key-order product-key-order :products base-products
           :diagnostics []}}
         {:purpose :sh07-b51-predecessor-outcome-semantic-id
          :preimage {:outcome-kind :b51-vector-frontier-rejected}}]
        carrier
        (with-root6-full-sh06-evidence
        {:artifact :gravity/sh07-b51-raw-adapter-request-v18
         :schema-version 18 :authenticated-sh06-request request
         :authenticated-sh06-verification-report
         {:artifact :gravity/sh06-resolution-artifact-verification
          :status :passed :failed-checks [] :source-path source-path
          :checks {:shape true :lineage true}}
         :sh06-resolution-artifact
         {:kind :gravity/sh06-resolution-artifact :status :accepted
          :artifact-id artifact-id :resolution-table []
          :provenance {:source-path source-path}
          :capability-based-proof
          {:artifact :gravity/sh06-resolution-capability-proof
           :status :complete :failed-checks []}
          :gravity-resolution-boundary
          {:authenticated-resolution-request
           {:provenance {:actual-source-path source-path}
            :module (:module request)}
           :resolved-analysis
           {:module-contract (:module request) :resolution-table []}}}
         :predecessor-executable-descriptor {}
         :v18-executable-descriptor {} :predecessor-contract-carrier {}
         :predecessor-raw-outcome {} :physical-invocation {}
         :scope :sh07-b51-vector-destructuring})]
    (is (true? ((:invoke runtime) 'sh07-b51-r6-node-products-authentic?
                [base-products])) :empty-node-products)
    (is (true? ((:invoke runtime) 'sh07-b51-r6-sh06-evidence-shape?
                [carrier])) :empty-full-sh06-evidence-shape)
    (is (true? ((:invoke runtime) 'sh07-b51-r6-sh06-module-bound?
                [(get-in carrier [:sh06-resolution-artifact
                                   :gravity-resolution-boundary
                                   :authenticated-resolution-request])
                 (get-in carrier [:sh06-resolution-artifact
                                   :gravity-resolution-boundary
                                   :resolved-analysis])
                 request])) :empty-full-sh06-module-bound)
    (is (true? ((:invoke runtime) 'sh07-b51-r6-sh06-products-bound?
                [(:sh06-resolution-artifact carrier)
                 (get-in carrier [:sh06-resolution-artifact
                                   :gravity-resolution-boundary
                                   :resolved-analysis])]))
        :empty-full-sh06-products-bound)
    (is (true? ((:invoke runtime) 'sh07-b51-r6-fresh-sh06? [carrier]))
        :empty-fresh-sh06)
    (is (true? ((:invoke runtime) 'sh07-b51-r6-request-products-authentic?
                [carrier base-products])) :empty-request-products)
    (let [relational
          ((:invoke runtime) 'sh07-b51-r6-derived-relational-products
           [carrier plan
            {:nodes [] :definitions [] :reference-uses [] :calls []
             :function-records [] :call-edges [] :error-transfers []}])]
      (is (= :accepted (:status relational)) relational))
    (let [baseline (reconstruct [carrier plan])]
      (is (= :accepted (:status baseline)) baseline))
    ;; A coherent supplied Tier3 rewrite must not become authoritative merely
    ;; because every request/preimage join remains self-consistent.  Exercise
    ;; every governed product family, including request-projected families
    ;; whose carrier view is changed coherently with the forged identity.
    (doseq [key product-key-order]
      (let [prior (get base-products key)
            forged
            (cond
              (vector? prior) (conj prior {:forged key})
              (map? prior) (assoc prior :forged key)
              (nil? prior) {:forged key}
              :else [:forged key])
            changed-plan (assoc-in plan [1 :preimage :products key] forged)
            request-key
            (get {:fragment-manifest :fragment-manifest
                  :fragment-coverage :fragment-coverage
                  :module-assembly-manifest :module-assembly-manifest
                  :binding-table :binding-table
                  :declared-alias-table :alias-table
                  :resolution-table :resolution-table
                  :macro-expansion-trace :macro-expansion-trace
                  :macro-origin-traces :macro-origin-traces
                  :macro-origin-expectation :macro-origin-expectation}
                 key)
            changed-carrier
            (if request-key
              (assoc-in carrier [:authenticated-sh06-request request-key]
                        forged)
              carrier)]
        (is (= :rejected
               (:status (reconstruct [changed-carrier changed-plan])))
            key)))))

(deftest sh07-b51-v18-root6-executes-local-external-and-cross-fragment-policy
  (let [runtime (direct-source-runtime 'sh07-b51-r6-authorize-resolution)
        authorize
        #((:invoke runtime) 'sh07-b51-r6-authorize-resolution %)
        fragment-a (sha \1) fragment-b (sha \2)
        root-a (sha \3) root-b (sha \4)
        root-a-syntax (sha \5) root-b-syntax (sha \6)
        reference-a (sha \7) reference-b (sha \8)
        local-id (sha \a) import-id (sha \b) alias-id (sha \c)
        local-upstream (sha \d) import-upstream (sha \e)
        alias-upstream (sha \f)
        binding-table
        [{:binding-id local-id :upstream-binding-id local-upstream
          :binding-class :lexical}
         {:binding-id import-id :upstream-binding-id import-upstream
          :binding-class :import}
         {:binding-id alias-id :upstream-binding-id alias-upstream
          :binding-class :namespace}]
        forms
        [{:form-id root-a :syntax-id root-a-syntax :parent-form-id nil
          :child-form-ids []}
         {:form-id root-b :syntax-id root-b-syntax :parent-form-id nil
          :child-form-ids []}]
        fragments
        [{:fragment-id fragment-a :root-form-ids [root-a]
          :form-ids [root-a] :local-binding-ids [local-id]
          :external-binding-ids []
          :resolution-reference-syntax-ids [reference-a]}
         {:fragment-id fragment-b :root-form-ids [root-b]
          :form-ids [root-b] :local-binding-ids []
          :external-binding-ids [import-id alias-id]
          :resolution-reference-syntax-ids [reference-b]}]
        request
        {:forms forms :top-level-form-ids [root-a root-b]
         :binding-table binding-table :fragment-manifest fragments
         :alias-table [{:alias 'dep :binding-id alias-id
                        :fragment-id fragment-b}]
         :resolution-table []}
        indexes {:bindings-by-id (zipmap (map :binding-id binding-table)
                                         binding-table)}
        carrier
        {:sh06-resolution-artifact
         {:gravity-resolution-boundary
          {:resolved-analysis
           {:binding-table [{:binding-id local-upstream}
                            {:binding-id import-upstream}
                            {:binding-id alias-upstream}]
            :module-contract {:declared-import-binding-ids [import-upstream]
                              :declared-alias-binding-ids [alias-upstream]}}}}}
        resolution
        (fn [binding-id syntax-id]
          {:binding-id binding-id :reference-syntax-id syntax-id})]
    (is (= :owning-fragment-local
           (authorize [carrier request indexes
                       (resolution local-id reference-a)]))
        :local-reference-in-owning-fragment)
    (is (nil? (authorize [carrier request indexes
                          (resolution local-id reference-b)]))
        :local-binding-referenced-from-other-fragment)
    (is (= :authenticated-external
           (authorize [carrier request indexes
                       (resolution import-id reference-b)]))
        :declared-import-in-reference-fragment)
    (is (= :authenticated-external
           (authorize [carrier request indexes
                       (resolution alias-id reference-b)]))
        :declared-alias-in-reference-fragment)
    (is (nil?
         (authorize
          [carrier
           (update request :alias-table empty)
           indexes (resolution alias-id reference-b)]))
        :undeclared-alias)
    (is (nil?
         (authorize
          [(assoc-in carrier
                     [:sh06-resolution-artifact :gravity-resolution-boundary
                      :resolved-analysis :binding-table] [])
           request indexes (resolution local-id reference-a)]))
        :missing-upstream-authority)))

(deftest sh07-b51-v18-root6-owner-coordinate-is-authentic-top-level-defn-fn
  (let [runtime
        (direct-source-runtime 'sh07-b51-r6-owner-coordinate-authentic?)
        authentic?
        #((:invoke runtime) 'sh07-b51-r6-owner-coordinate-authentic? %)
        fragment-id (sha \1)
        def-form-id (sha \2) def-syntax-id (sha \3)
        fn-form-id (sha \4) fn-syntax-id (sha \5)
        body-form-id (sha \6) body-syntax-id (sha \7)
        defn-input-syntax-id (sha \8)
        forms
        [{:form-id def-form-id :syntax-id def-syntax-id :head-symbol 'def
          :parent-form-id nil :child-form-ids [fn-form-id]}
         {:form-id fn-form-id :syntax-id fn-syntax-id :head-symbol 'fn*
          :parent-form-id def-form-id :child-form-ids [body-form-id]}
         {:form-id body-form-id :syntax-id body-syntax-id
          :parent-form-id fn-form-id :child-form-ids []}]
        fragment
        {:fragment-id fragment-id :root-form-ids [def-form-id]
         :form-ids [def-form-id fn-form-id body-form-id]
         :local-binding-ids [] :external-binding-ids []
         :resolution-reference-syntax-ids []}
        request {:forms forms :top-level-form-ids [def-form-id]
                 :fragment-manifest [fragment]
                 :macro-origin-traces
                 [{:macro 'defn :input-syntax-id defn-input-syntax-id
                   :output-def-syntax-id def-syntax-id
                   :introduced-fn-syntax-id fn-syntax-id}]}
        indexes {:forms-by-id (zipmap (map :form-id forms) forms)}
        coordinate
        {:fragment-id fragment-id :fragment-ordinal 0
         :root-form-id def-form-id :root-syntax-id def-syntax-id
         :owning-definition-form-id def-form-id
         :owning-definition-syntax-id def-syntax-id
         :def-form-id def-form-id :def-syntax-id def-syntax-id
         :defn-input-syntax-id defn-input-syntax-id
         :fn-form-id fn-form-id :fn-syntax-id fn-syntax-id
         :enclosing-form-id body-form-id
         :enclosing-syntax-id body-syntax-id}]
    (is (true? (authentic? [request indexes coordinate]))
        :authentic-top-level-defn-fn-coordinate)
    (doseq [[label changed-request changed-indexes changed-coordinate]
            [[:not-top-level (assoc request :top-level-form-ids [])
              indexes coordinate]
             [:not-fragment-root
              (assoc-in request [:fragment-manifest 0 :root-form-ids] [])
              indexes coordinate]
             [:broken-fn-ancestry request
              (assoc-in indexes [:forms-by-id fn-form-id :parent-form-id] nil)
              coordinate]
             [:wrong-fn-identity request indexes
              (assoc coordinate :fn-form-id body-form-id
                     :fn-syntax-id body-syntax-id)]]]
      (is (false?
           (authentic? [changed-request changed-indexes changed-coordinate]))
          label))))

(deftest sh07-b51-v18-root6-lowers-authentic-raw-basic-forms-exactly
  (let [runtime (direct-source-runtime 'sh07-b51-r6-lower-form)
        lower #((:invoke runtime) 'sh07-b51-r6-lower-form %)
        node-auth-runtime
        (direct-source-runtime 'sh07-b51-r6-core-nodes-raw-authentic?)
        nodes-authentic?
        #((:invoke node-auth-runtime)
          'sh07-b51-r6-core-nodes-raw-authentic? %)
        id (fn [family ordinal]
             (bootstrap/reader-canonical-hash
              {:domain :root6-independent-lowering-fixture
               :family family :ordinal ordinal}))
        authority-ref (internal-reference 0)
        binding-id (id :binding 0)
        definition-syntax-id (id :definition 0)
        base-form
        (fn [ordinal kind value children]
          {:form-id (id :form ordinal) :syntax-id (id :syntax ordinal)
           :kind kind :value value :parent-form-id nil :scope-id :expression
           :metadata {} :source-span nil :origin-chain []
           :generated-origin nil :child-form-ids children})
        scalar (base-form 1 :integer 7 [])
        reference (base-form 2 :symbol 'value [])
        vector-form (base-form 3 :vector [7] [(:form-id scalar)])
        map-form
        (base-form 4 :map {:value 7}
                   [(:form-id reference) (:form-id scalar)])
        set-form (base-form 5 :set #{7} [(:form-id scalar)])
        call-form
        (base-form 6 :list nil [(:form-id reference) (:form-id scalar)])
        quote-operator (base-form 20 :symbol 'quote [])
        quoted-form (base-form 21 :symbol 'unevaluated [])
        quote-form
        (base-form 7 :list nil
                   [(:form-id quote-operator) (:form-id quoted-form)])
        throw-operator (base-form 22 :symbol 'throw [])
        throw-form
        (base-form 8 :list nil
                   [(:form-id throw-operator) (:form-id scalar)])
        if-operator (base-form 23 :symbol 'if [])
        if-form
        (base-form 9 :list nil
                   [(:form-id if-operator) (:form-id scalar)
                    (:form-id scalar) (:form-id scalar)])
        do-operator (base-form 24 :symbol 'do [])
        do-form
        (base-form 10 :list nil
                   [(:form-id do-operator) (:form-id scalar)
                    (:form-id scalar)])
        keyword-operator (base-form 25 :keyword :value [])
        keyword-lookup-form
        (base-form 11 :list nil
                   [(:form-id keyword-operator) (:form-id map-form)])
        forms [scalar reference vector-form map-form set-form call-form
               quote-operator quoted-form quote-form throw-operator throw-form
               if-operator if-form do-operator do-form keyword-operator
               keyword-lookup-form]
        indexes
        {:forms-by-id (zipmap (map :form-id forms) forms)
         :forms-by-syntax (zipmap (map :syntax-id forms) forms)
         :bindings-by-id {}
         :resolutions-by-syntax {}}
        resolution
        {:reference-syntax-id (:syntax-id reference) :symbol 'value
         :binding-id binding-id :binding-class :lexical
         :definition-syntax-id definition-syntax-id}
        request
        {:module {:namespace 'root6.fixture :profile :meta :target :jvm
                  :preserved-declarations []}
         :resolution-table [resolution]}
        source
        (fn [form]
          {:form-id (:form-id form) :syntax-id (:syntax-id form)
           :semantic-span nil :origin-chain [] :generated-origin nil})
        expected-preimage
        (fn [form core-form children attributes resolved-binding-ids
             evaluation-kind]
          {:domain :gravity/sh07-b51-core-node-id-preimage-v18
           :schema-version 18 :predecessor-authority-id authority-ref
           :core-form core-form :children children :attributes attributes
           :source (source form) :resolved-binding-ids resolved-binding-ids
           :evaluation-kind evaluation-kind
           :evaluation-region :module-initialization
           :evaluation-owner-function-syntax-id nil
           :evaluated-children children :namespace 'root6.fixture
           :profile :meta :target :jvm :metadata {}
           :binding-context :expression :generated? false
           :preserved-declarations []
           :pending-fact-families [:types :effects :ownership :safety]})
        scalar-ref (internal-reference 1)
        reference-ref (internal-reference 2)
        map-ref (internal-reference 4)
        keyword-ref (internal-reference 25)
        cases
        [{:label :scalar :form scalar :ordinal 1 :children []
          :preimage
          (expected-preimage scalar :literal []
                             {:literal-kind :integer :value 7} [] :value)}
         {:label :reference :form reference :ordinal 2 :children []
          :preimage
          (expected-preimage
           reference :reference []
           {:symbol 'value :position :expression :binding-id binding-id
            :binding-class :lexical
            :definition-syntax-id definition-syntax-id}
           [binding-id] :value)}
         {:label :vector :form vector-form :ordinal 3
          :children [scalar-ref]
          :preimage
          (expected-preimage vector-form :collection-literal [scalar-ref]
                             {:literal-kind :vector :element-count 1
                              :source-value [7]}
                             [] :left-to-right)}
         {:label :map :form map-form :ordinal 4
          :children [reference-ref scalar-ref]
          :preimage
          (expected-preimage map-form :collection-literal
                             [reference-ref scalar-ref]
                             {:literal-kind :map :element-count 2
                              :source-value {:value 7}}
                             [] :left-to-right)}
         {:label :set :form set-form :ordinal 5 :children [scalar-ref]
          :preimage
          (expected-preimage set-form :collection-literal [scalar-ref]
                             {:literal-kind :set :element-count 1
                              :source-value #{7}}
                             [] :left-to-right)}
         {:label :call :form call-form :ordinal 6
          :children [reference-ref scalar-ref]
          :preimage
          (expected-preimage
           call-form :call [reference-ref scalar-ref]
           {:operator-child-index 0 :argument-count 1
            :argument-child-indexes [1]
            :evaluation-order :operator-then-arguments
            :dispatch :resolved-symbol-call}
           [binding-id] :operator-then-arguments)}
         {:label :quote :form quote-form :ordinal 7 :children []
          :preimage
          (expected-preimage
           quote-form :quote []
           {:quoted-form-id (:form-id quoted-form)
            :quoted-syntax-id (:syntax-id quoted-form)
            :quoted-kind :symbol :quoted-value 'unevaluated}
           [] :no-evaluation)}
         {:label :throw :form throw-form :ordinal 8
          :children [scalar-ref]
          :preimage
          (expected-preimage
           throw-form :throw [scalar-ref]
           {:value-child-index 0 :evaluation-order :value-then-transfer
            :construction-order :postorder-after-value
            :runtime-reachability :not-asserted-by-sh07-b7
            :transfer-policy :error-transfer :result-policy :never
            :required-effect :error/throw
            :authenticated-sh06-artifact-id nil
            :sh06-semantic-projection-id nil
            :type-legality :pending-sh08
            :effect-registry-legality :pending-sh09
            :effect-profile-capability-legality :pending-sh09
            :profile-error-lowering-legality :pending-sh09
            :ownership-legality :pending-sh10
            :safety-classification :pending-sh11}
           [] :value-then-transfer)}
         {:label :if :form if-form :ordinal 9
          :children [scalar-ref scalar-ref scalar-ref]
          :preimage
          (expected-preimage
           if-form :if [scalar-ref scalar-ref scalar-ref]
           {:condition-child-index 0 :then-child-index 1 :else-child-index 2
            :condition-evaluation :always-first
            :branch-evaluation :exactly-one
            :truthiness {:false-values [:nil :false] :other-values :true}}
           [] :condition-then-exclusive-branch)}
         {:label :do :form do-form :ordinal 10
          :children [scalar-ref scalar-ref]
          :preimage
          (expected-preimage
           do-form :do [scalar-ref scalar-ref]
           {:body-count 2 :evaluation-order :left-to-right
            :result :last-child :result-child-index 1}
           [] :left-to-right)}
         {:label :keyword-lookup :form keyword-lookup-form :ordinal 11
          :children [keyword-ref map-ref]
          :preimage
          (expected-preimage
           keyword-lookup-form :keyword-map-lookup [keyword-ref map-ref]
           {:keyword-child-index 0 :map-child-index 1 :keyword :value
            :evaluation-order :keyword-then-map
            :dispatch :keyword-map-lookup :missing-key-policy :nil
            :result-policy :map-value-or-nil
            :target-type-legality :pending-sh08}
           [] :keyword-then-map)}]
        authority-request
        {:ordinal 0 :purpose :sh07-b51-predecessor-authority-id
         :preimage {:domain :gravity/sh07-b51-predecessor-authority-v18}}
        requests
        (into [authority-request]
              (map (fn [{:keys [ordinal preimage]}]
                     {:ordinal ordinal :purpose :sh07-b51-core-node-id
                      :preimage preimage})
                   cases))
        expected-record
        (fn [preimage ordinal]
          (assoc
           (dissoc preimage :domain :schema-version :evaluation-kind
                   :evaluation-region
                   :evaluation-owner-function-syntax-id
                   :evaluated-children)
           :node-id (internal-reference ordinal)
           :evaluation
           {:kind (:evaluation-kind preimage)
            :region :module-initialization
            :owner-function-syntax-id nil
            :order
            (mapv (fn [index node-id]
                    {:index index :core-node-id node-id})
                  (range) (:children preimage))}))]
    (doseq [{:keys [label form ordinal children preimage]} cases]
      (let [args [request requests indexes form children
                  :module-initialization nil]
            lowered (lower args)]
        (is (= :accepted (:status lowered)) label)
        (is (= preimage (:preimage lowered)) [label :preimage])
        (is (= (internal-reference ordinal) (:node-id lowered))
            [label :node-id])
        (is (= (expected-record preimage ordinal) (:record lowered))
            [label :record])
        (is (= :rejected
               (:status
                (lower
                 [request
                  (update-in requests [ordinal :preimage :attributes]
                             assoc :forged true)
                  indexes form children :module-initialization nil])))
            [label :coherent-request-preimage-forgery])
        (is (= :rejected
               (:status
                (lower
                 [request requests indexes
                  (assoc form :metadata {:forged true}) children
                  :module-initialization nil])))
            [label :raw-form-mutation])))
    (let [tree-call-id (:form-id call-form)
          tree-reference (assoc reference :parent-form-id tree-call-id)
          tree-scalar (assoc scalar :parent-form-id tree-call-id)
          tree-call call-form
          tree-forms [tree-call tree-reference tree-scalar]
          tree-request
          (merge
           request
           {:artifact :gravity/sh07-authenticated-sh06-core-request
            :schema-version 15 :lineage {} :forms tree-forms
            :top-level-form-ids [tree-call-id] :binding-table []
            :alias-table [] :fragment-manifest [] :fragment-coverage {}
            :module-assembly-manifest {} :macro-expansion-trace []
            :macro-origin-traces [] :macro-origin-expectation {}
            :projection-binding {} :provenance {}
            :scope :sh07-b15-keyword-map-lookup})
          tree-indexes
          {:forms-by-id (zipmap (map :form-id tree-forms) tree-forms)
           :forms-by-syntax (zipmap (map :syntax-id tree-forms) tree-forms)
           :bindings-by-id {}
           :resolutions-by-syntax {(:syntax-id tree-reference) resolution}}
          selected (fn [label] (first (filter #(= label (:label %)) cases)))
          expected-nodes
          (mapv (fn [label]
                  (let [{:keys [ordinal preimage]} (selected label)]
                    (expected-record preimage ordinal)))
                [:reference :scalar :call])
          ]
      (is (true? (nodes-authentic?
                  [tree-request tree-indexes expected-nodes]))
          :raw-derived-node-baseline)
      (doseq [[label changed]
              [[:core-form (assoc-in expected-nodes [0 :core-form] :literal)]
               [:attributes
                (assoc-in expected-nodes [0 :attributes :symbol] 'forged)]
               [:children (assoc-in expected-nodes [2 :children] [])]
               [:evaluation
                (assoc-in expected-nodes [2 :evaluation :order] [])]
               [:source-semantics
                (assoc-in expected-nodes [1 :source :semantic-span]
                          {:forged true})]]]
        (is (false?
             (nodes-authentic? [tree-request tree-indexes changed]))
            [label :coherent-node-forgery])))))

(deftest sh07-b51-v18-root6-lowers-authentic-stateful-and-structured-forms
  (let [request (:request (fixture-projection "ordinary-core-form-matrix"))
        runtime (direct-source-runtime 'sh07-b51-r6-lower-form)
        call (fn [name args] ((:invoke runtime) name args))
        indexes (call 'sh07-b51-r6-index-request [request])
        form-for
        (fn [operator]
          (first (operator-enclosing-forms
                  request (fixture-indexes {:request request}) operator)))
        children-for
        (fn [form base]
          (mapv (fn [ordinal] (internal-reference (+ base ordinal)))
                (range (count (call 'sh07-b51-r6-expression-child-form-ids
                                    [indexes form])))))
        semantics
        (fn [operator function-name base]
          (let [form (form-for operator)
                children (children-for form base)]
            {:form form :children children
             :result (call function-name [request indexes form children])}))
        var-form (form-for 'var)
        var-result (call 'sh07-b51-r6-var-semantics
                         [request indexes var-form])
        set-case (semantics 'set! 'sh07-b51-r6-set-semantics 200)
        def-case (semantics 'def 'sh07-b51-r6-def-semantics 210)
        try-case (semantics 'try 'sh07-b51-r6-try-semantics 220)
        match-case (semantics 'match 'sh07-b51-r6-match-semantics 230)
        fn-case (semantics 'fn 'sh07-b51-r6-fn-semantics 240)
        let-case
        (let [form (form-for 'let) children (children-for form 250)]
          {:form form :children children
           :result (call 'sh07-b51-r6-let-semantics
                         [request indexes form children :let])})
        loop-case
        (let [form (form-for 'loop) children (children-for form 260)]
          {:form form :children children
           :result (call 'sh07-b51-r6-let-semantics
                         [request indexes form children :loop])})
        recur-form (form-for 'recur)
        recur-children (children-for recur-form 270)
        recur-target (call 'sh07-b51-r6-nearest-recur-target
                           [indexes recur-form 0 #{}])
        recur-result
        (call 'sh07-b51-r6-recur-semantics
              [request indexes recur-form recur-children
               (get-in recur-target [:target :syntax-id])])]
    (is (= :accepted (:status var-result)) var-result)
    (is (= {:object-kind :top-level-var
            :object-policy :reference-without-dereference-or-execution
            :resolution-order :current-namespace-binding}
           (select-keys (:attributes var-result)
                        [:object-kind :object-policy :resolution-order])))
    (is (= :no-evaluation (:evaluation-kind var-result)))
    (doseq [[label {:keys [result]} expected-kind]
            [[:set! set-case :value-then-write]
             [:def def-case :initialize-once]
             [:try try-case :protected-then-ordered-typed-handler-candidates]
             [:match match-case
              :scrutinee-then-source-ordered-pattern-candidates]
             [:fn fn-case :function-creation]
             [:let let-case :initializers-then-body-left-to-right]
             [:loop loop-case :initializers-then-body-left-to-right]]]
      (is (= :accepted (:status result)) [label result])
      (is (= expected-kind (:evaluation-kind result)) [label result])
      (is (vector? (:binding-ids result)) [label result]))
    (is (= :accepted (:status recur-target)) recur-target)
    (is (= :accepted (:status recur-result)) recur-result)
    (is (= :loop (get-in recur-result [:attributes :target-kind]))
        recur-result)
    (is (= (count recur-children)
           (get-in recur-result [:attributes :arity]))
        recur-result)
    (is (= (get-in recur-target [:target :syntax-id])
           (get-in recur-result [:attributes :target-id]))
        recur-result)
    ;; Every structured branch closes on its independently derived child
    ;; cardinality.  Supplying one fewer value must not be repairable by a
    ;; self-consistent request-side attribute mutation.
    (doseq [[label {:keys [form children]} function-name extra]
            [[:set! set-case 'sh07-b51-r6-set-semantics []]
             [:def def-case 'sh07-b51-r6-def-semantics []]
             [:try try-case 'sh07-b51-r6-try-semantics []]
             [:match match-case 'sh07-b51-r6-match-semantics []]
             [:fn fn-case 'sh07-b51-r6-fn-semantics []]
             [:let let-case 'sh07-b51-r6-let-semantics [:let]]
             [:loop loop-case 'sh07-b51-r6-let-semantics [:loop]]]]
      (let [reduced (vec (drop-last children))
            rejected (call function-name
                           (into [request indexes form reduced] extra))]
        (is (= :rejected (:status rejected)) [label rejected])))))

(deftest sh07-b51-v18-root6-rejects-nonempty-raw-product-forgery-matrix
  (let [runtime (direct-source-runtime 'sh07-b51-r6-reconstruct-products)
        reconstruct
        #((:invoke runtime) 'sh07-b51-r6-reconstruct-products %)
        form-id (sha \c)
        syntax-id (sha \d)
        source {:form-id form-id :syntax-id syntax-id
                :semantic-span nil :origin-chain [] :generated-origin nil}
        evaluation {:kind :literal :region nil
                    :owner-function-syntax-id nil :order []}
        node-preimage
        {:domain :gravity/sh07-b51-core-node-id-preimage-v18
         :schema-version 18
         :predecessor-authority-id (internal-reference 50)
         :core-form :literal :children [] :attributes {} :source source
         :resolved-binding-ids [] :evaluation-kind :literal
         :evaluation-region nil :evaluation-owner-function-syntax-id nil
         :evaluated-children [] :namespace 'fixture :profile :meta
         :target :jvm :metadata {} :binding-context :expression
         :generated? false :preserved-declarations []
         :pending-fact-families [:types :effects :ownership :safety]}
        node (assoc (dissoc node-preimage :domain :schema-version)
                    :node-id (internal-reference 0)
                    :evaluation evaluation)
        artifact-id (sha \5)
        revision-id (sha \6)
        projection-id (sha \7)
        alias-id (sha \8)
        fragment-id (sha \9)
        source-path "literal-nonempty-root6.gravity"
        coverage
        {:root-form-count 0 :form-count 1 :local-binding-count 0
         :resolution-count 0 :fragment-count 1
         :covered-root-form-ids [] :covered-form-ids [form-id]
         :covered-local-binding-ids []
         :covered-resolution-reference-syntax-ids []}
        assembly
        {:ordered-fragment-ids [fragment-id] :root-form-ids []
         :source-revision-id revision-id
         :sh06-semantic-projection-id projection-id
         :alias-table-id alias-id :content-id revision-id
         :module-id projection-id}
        request
        {:artifact :gravity/sh07-authenticated-sh06-core-request
         :schema-version 15
         :lineage {:authenticated-sh06-artifact-id artifact-id
                   :source-revision-id revision-id
                   :sh06-semantic-projection-id projection-id
                   :alias-table-id alias-id :sh05-artifact-id (sha \4)
                   :macro-expansion-trace-id (sha \5)}
         :module {:profile :safe :source-revision-id revision-id}
         :forms [{:form-id form-id :syntax-id syntax-id}]
         :top-level-form-ids []
         :binding-table [] :resolution-table []
         :fragment-manifest
         [{:fragment-id fragment-id :root-form-ids []
           :form-ids [form-id] :local-binding-ids []
           :external-binding-ids []
           :resolution-reference-syntax-ids []}]
         :fragment-coverage coverage
         :module-assembly-manifest assembly :alias-table []
         :macro-expansion-trace [] :macro-origin-traces []
         :macro-origin-expectation
         {:source-revision-id revision-id :sh05-artifact-id (sha \4)
          :macro-expansion-trace-id (sha \5) :expanded-defn-count 0
          :expected-input-syntax-ids []
          :expected-output-def-syntax-ids []
          :expected-introduced-fn-syntax-ids []}
         :projection-binding {}
         :provenance {:actual-source-path source-path}
         :scope :sh07-b15-keyword-map-lookup}
        transcript-preimage
        {:domain :gravity/sh07-b51-slot-extraction-transcript-id-preimage-v18
         :schema-version 18 :predecessor-authority-id (internal-reference 50)
         :same-request-semantic-id (internal-reference 51)
         :slot-count 0 :extraction-count 0 :runtime-check-count 0
         :publication-event-count 0 :recur-slot-mapping-count 0
         :slot-ids [] :extraction-ids [] :runtime-check-ids []
         :publication-event-ids [] :recur-slot-mapping-ids []
         :global-slot-range {:first 0 :count 0}
         :global-extraction-range {:first 0 :count 0}
         :global-runtime-check-range {:first 0 :count 0}
         :global-publication-range {:first 0 :count 0}
         :global-recur-range {:first 0 :count 0}}
        transcript
        (assoc (dissoc transcript-preimage :domain :schema-version)
               :schema :gravity/sh07-b51-slot-extraction-transcript-v18
               :transcript-id (internal-reference 1))
        base-products
        (-> (zipmap product-key-order (repeat []))
            (assoc :fragment-manifest
                   [{:fragment-id fragment-id :root-form-ids []
                     :form-ids [form-id] :local-binding-ids []
                     :external-binding-ids []
                     :resolution-reference-syntax-ids []
                     :root-node-ids []}]
                   :fragment-coverage coverage
                   :module-assembly-manifest assembly :binding-table []
                   :declared-alias-table [] :resolution-table []
                   :macro-expansion-trace [] :macro-origin-traces []
                   :macro-origin-expectation
                   {:source-revision-id revision-id
                    :sh05-artifact-id (sha \4)
                    :macro-expansion-trace-id (sha \5)
                    :expanded-defn-count 0
                    :expected-input-syntax-ids []
                    :expected-output-def-syntax-ids []
                    :expected-introduced-fn-syntax-ids []}
                   :nodes [node]
                   :pending-fact-families
                   [:types :effects :ownership :safety]
                   :legacy-v16-equivalence nil
                   :evaluation-order
                   [{:core-node-id (internal-reference 0)
                     :kind :literal :region nil
                     :owner-function-syntax-id nil :children []}]
                   :source-map
                   [{:core-node-id (internal-reference 0)
                     :syntax-id syntax-id :form-id form-id
                     :source-span nil :origin-chain []
                     :generated-origin nil}]
                   :slot-extraction-transcript transcript))
        plan
        [{:purpose :sh07-b51-core-node-id :preimage node-preimage}
         {:purpose :sh07-b51-slot-extraction-transcript-id
          :preimage transcript-preimage}
         {:purpose :sh07-b51-core-identity-id
          :preimage
          {:domain :gravity/sh07-b51-core-identity-preimage-v18
           :schema-version 18
           :artifact-kind :gravity/sh07-b51-canonical-core-artifact-v18
           :task "SH-07-B51" :scope :sh07-b51-vector-destructuring
           :predecessor-outcome-kind :b51-vector-frontier-rejected
           :same-request-semantic-id nil :predecessor-authority-id nil
           :module (:module request) :lineage (:lineage request)
           :projection-binding (:projection-binding request)
           :product-key-order product-key-order :products base-products
           :diagnostics []}}
         {:purpose :sh07-b51-predecessor-outcome-semantic-id
          :preimage {:outcome-kind :b51-vector-frontier-rejected}}]
        carrier
        (with-root6-full-sh06-evidence
        {:artifact :gravity/sh07-b51-raw-adapter-request-v18
         :schema-version 18 :authenticated-sh06-request request
         :authenticated-sh06-verification-report
         {:artifact :gravity/sh06-resolution-artifact-verification
          :status :passed :failed-checks [] :source-path source-path
          :checks {:shape true :lineage true}}
         :sh06-resolution-artifact
         {:kind :gravity/sh06-resolution-artifact :status :accepted
          :artifact-id artifact-id :resolution-table []
          :provenance {:source-path source-path}
          :capability-based-proof
          {:artifact :gravity/sh06-resolution-capability-proof
           :status :complete :failed-checks []}
          :gravity-resolution-boundary
          {:authenticated-resolution-request
           {:provenance {:actual-source-path source-path}
            :module (:module request)}
           :resolved-analysis
           {:module-contract (:module request) :resolution-table []}}}
         :predecessor-executable-descriptor {}
         :v18-executable-descriptor {} :predecessor-contract-carrier {}
         :predecessor-raw-outcome {} :physical-invocation {}
         :scope :sh07-b51-vector-destructuring})]
    (is (true? ((:invoke runtime) 'sh07-b51-r6-node-products-authentic?
                [base-products]))
        :nonempty-node-products)
    (is (true? ((:invoke runtime) 'sh07-b51-r6-fresh-sh06? [carrier]))
        :nonempty-fresh-sh06)
    (is (true? ((:invoke runtime)
                'sh07-b51-r6-sh06-metadata-source-authentic? [carrier]))
        :nonempty-sh06-metadata)
    (let [fragments ((:invoke runtime)
                     'sh07-b51-r6-derived-fragment-products
                     [request (:nodes base-products)])]
      (is (= {:status :accepted
              :fragment-manifest (:fragment-manifest base-products)
              :root-core-node-ids []}
             fragments)
          fragments))
    (is (= [(:fragment-coverage base-products)
            (:module-assembly-manifest base-products)
            (:binding-table base-products)
            (:declared-alias-table base-products)
            (:resolution-table base-products)
            (:definitions base-products)
            (:macro-expansion-trace base-products)
            (:macro-origin-traces base-products)
            (:macro-origin-expectation base-products)]
           [(:fragment-coverage request)
            (:module-assembly-manifest request)
            (:binding-table request) (:alias-table request)
            (:resolution-table request) []
            (:macro-expansion-trace request)
            (:macro-origin-traces request)
            (:macro-origin-expectation request)]))
    (is (true? ((:invoke runtime) 'sh07-b51-r6-request-products-authentic?
                [carrier base-products]))
        :nonempty-request-products)
    (let [baseline (reconstruct [carrier plan])]
      (is (= :accepted (:status baseline)) baseline))
    (doseq [key product-key-order]
      (let [prior (get base-products key)
            forged
            (cond
              (vector? prior) (conj prior {:forged key})
              (map? prior) (assoc prior :forged key)
              :else {:forged key})
            changed-plan (assoc-in plan [2 :preimage :products key] forged)
            request-key
            (get {:fragment-manifest :fragment-manifest
                  :fragment-coverage :fragment-coverage
                  :module-assembly-manifest :module-assembly-manifest
                  :binding-table :binding-table
                  :declared-alias-table :alias-table
                  :resolution-table :resolution-table
                  :macro-expansion-trace :macro-expansion-trace
                  :macro-origin-traces :macro-origin-traces
                  :macro-origin-expectation :macro-origin-expectation}
                 key)
            changed-carrier
            (if request-key
              (assoc-in carrier [:authenticated-sh06-request request-key]
                        forged)
              carrier)]
        (is (= :rejected
               (:status (reconstruct [changed-carrier changed-plan])))
            key)))))

(deftest sh07-b51-v18-root6-present-core-families-have-nonempty-raw-derivations
  (let [runtime (direct-source-runtime 'sh07-b51-r6-node-products-authentic?)
        definitions (definitions-by-name @source-forms)
        derive #((:invoke runtime) 'sh07-b51-r6-derived-node-products %)
        helper-for
        {:var-references 'sh07-b51-r6-derived-var-references
         :mutations 'sh07-b51-r6-derived-mutations
         :error-transfers 'sh07-b51-r6-derived-error-transfers
         :function-records 'sh07-b51-r6-derived-functions
         :call-edges 'sh07-b51-r6-derived-call-edges}
        derive-canonical
        (fn [key nodes]
          (let [helper (get helper-for key)]
            (if (contains? definitions helper)
              ((:invoke runtime) helper [nodes])
              (derive [key nodes]))))
        authentic?
        #((:invoke runtime) 'sh07-b51-r6-node-products-authentic? [%])
        cases
        [[:var-references :var]
         [:mutations :set!]
         [:error-transfers :throw]
         [:function-records :fn]
         [:call-edges :call]]
        node
        (fn [core-form marker]
          {:node-id (internal-reference 0) :core-form core-form
           :source {:form-id (sha \c) :syntax-id (sha \d)
                    :semantic-span nil :origin-chain []
                    :generated-origin nil}
           :attributes {:marker marker}
           :children [(internal-reference 1)]
           :evaluation {:kind core-form :region nil
                        :owner-function-syntax-id nil :order []}
           :resolved-binding-ids [(sha \e)]})
        literal-expected
        (fn [key n]
          (let [a (:attributes n) s (:source n) e (:evaluation n)
                child (first (:children n))]
            (get
             {:var-references
              {:ordinal 0 :core-node-id (:node-id n)
               :form-id (:form-id s) :syntax-id (:syntax-id s)
               :operand-form-id (:operand-form-id a)
               :operand-syntax-id (:operand-syntax-id a)
               :symbol (:symbol a) :binding-id (:binding-id a)
               :binding-class (:binding-class a)
               :definition-kind (:definition-kind a)
               :object-kind (:object-kind a)
               :upstream-binding-id (:upstream-binding-id a)
               :namespace (:namespace a)
               :definition-syntax-id (:definition-syntax-id a)
               :definition-artifact-id (:definition-artifact-id a)
               :resolution-order (:resolution-order a)
               :authenticated-sh06-artifact-id
               (:authenticated-sh06-artifact-id a)
               :sh06-semantic-projection-id
               (:sh06-semantic-projection-id a)
               :object-policy (:object-policy a)
               :evaluated-children [] :evaluation-order []
               :profile-legality (:profile-legality a)}
              :mutations
              {:target-symbol (:target-symbol a)
               :target-binding-id (:target-binding-id a)
               :target-binding-class (:target-binding-class a)
               :target-definition-kind (:target-definition-kind a)
               :target-location (:target-location a)
               :target-upstream-binding-id (:target-upstream-binding-id a)
               :target-namespace (:target-namespace a)
               :target-definition-syntax-id (:target-definition-syntax-id a)
               :target-definition-artifact-id
               (:target-definition-artifact-id a)
               :target-form-id (:target-form-id a)
               :target-syntax-id (:target-syntax-id a)
               :resolution-order (:resolution-order a)
               :authenticated-sh06-artifact-id
               (:authenticated-sh06-artifact-id a)
               :sh06-semantic-projection-id
               (:sh06-semantic-projection-id a)
               :ordinal 0 :core-node-id (:node-id n)
               :form-id (:form-id s) :syntax-id (:syntax-id s)
               :value-core-node-id child
               :target-evaluated-children [] :evaluated-children [child]
               :evaluation-region (:region e)
               :evaluation-owner-function-syntax-id
               (:owner-function-syntax-id e)
               :ordered-steps [:evaluate-value :write-target :yield-unit]
               :result-policy :unit :required-effect :state/write
               :type-legality (:type-legality a)
               :effect-registry-legality (:effect-registry-legality a)
               :effect-profile-capability-legality
               (:effect-profile-capability-legality a)
               :ownership-legality (:ownership-legality a)
               :safety-classification (:safety-classification a)}
              :error-transfers
              {:ordinal 0 :core-node-id (:node-id n)
               :form-id (:form-id s) :syntax-id (:syntax-id s)
               :value-core-node-id child :evaluated-children [child]
               :evaluation-region (:region e)
               :evaluation-owner-function-syntax-id
               (:owner-function-syntax-id e)
               :ordered-steps [:evaluate-value :transfer-error]
               :construction-order (:construction-order a)
               :runtime-reachability (:runtime-reachability a)
               :transfer-policy (:transfer-policy a)
               :result-policy (:result-policy a)
               :required-effect (:required-effect a)
               :authenticated-sh06-artifact-id
               (:authenticated-sh06-artifact-id a)
               :sh06-semantic-projection-id
               (:sh06-semantic-projection-id a)
               :type-legality (:type-legality a)
               :effect-registry-legality (:effect-registry-legality a)
               :effect-profile-capability-legality
               (:effect-profile-capability-legality a)
               :profile-error-lowering-legality
               (:profile-error-lowering-legality a)
               :ownership-legality (:ownership-legality a)
               :safety-classification (:safety-classification a)}
              :function-records
              {:ordinal 0 :function-core-node-id (:node-id n)
               :function-form-id (:form-id s)
               :function-syntax-id (:syntax-id s)
               :body-core-node-id child
               :parameter-binding-ids (:resolved-binding-ids n)
               :fixed-arity (:fixed-arity a)
               :definition-kind :anonymous
               :definition-binding-id nil :definition-core-node-id nil
               :definition-name nil}
              :call-edges
              {:ordinal 0 :call-core-node-id (:node-id n)
               :caller-function-syntax-id nil
               :caller-function-core-node-id nil
               :callee-binding-id (first (:resolved-binding-ids n))
               :callee-definition-syntax-id nil
               :callee-function-syntax-id nil
               :callee-function-core-node-id nil
               :argument-core-node-ids (vec (rest (:children n)))
               :ordered-evaluation-node-ids (:children n)
               :evaluation-order :operator-then-arguments
               :classification :nonlocal-or-nonfunction}}
             key))) ]
    (doseq [[key core-form] cases]
      (let [nodes [(node core-form :base)]
            base (derive-canonical key nodes)
            expected (literal-expected key (first nodes))
            products
            (into
             {:nodes nodes
              :var-references (derive-canonical :var-references nodes)
              :mutations (derive-canonical :mutations nodes)
              :error-transfers (derive-canonical :error-transfers nodes)}
             (map (fn [derived-key]
                    [derived-key (derive [derived-key nodes])]))
             [:evaluation-order :source-map :reference-uses
              :control-flow :calls :keyword-lookups])]
        (is (seq base) [key core-form :missing-raw-derivation])
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
        _slot (assoc (dissoc let-slot-preimage :domain :schema-version)
                    :slot-id (id 40))
        _loop-slot
        (assoc (dissoc loop-slot-preimage :domain :schema-version)
               :slot-id (id 42))
        _extraction
        (assoc (dissoc let-extraction-preimage :domain :schema-version)
               :extraction-id (id 41))
        _loop-extraction
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
        _macro-input-id (sha \6)
        _macro-output-id (sha \7)
        _macro-fn-id (sha \8)
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
