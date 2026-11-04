(ns gravity.c13-optimization-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c13-optimization :as c13]
            [gravity.optimization-lowering :as shared]
            [gravity.c13-optimization.artifact :as artifact]
            [gravity.c13-optimization.operations :as c13-ops]
            [gravity.c13-optimization.projection :as projection]
            [gravity.c13-optimization.validation :as validation]))

(def module
  {:module 'gravity.c13-test
   :source-path "c13-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c12-artifact
  {:kind :gravity/stage0-c12-domain-ir-architecture-artifact
   :task "P06-D091"
   :artifact-id "sha256:c12"
   :governing-document "docs/c12.md"
   :domain-verifier-report {:status :passed}
   :semantic-anchor-map [{:domain :efir :syntax-id "syntax-0"}]
   :capability-based-proof {:status :complete}
   :domain-ir-artifacts
   [{:source {:syntax-id "syntax-0"
              :span {:source "c13-test.gravity" :form-index 0}}}]})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c13-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c12-domain-ir-source-artifact (fn [_ _] c12-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c13/c13-engine-contract)
        publics (ns-publics 'gravity.c13-optimization)]
    (is (= :hosted-stage0-c13-optimization (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c13-authority? contract)))
    (is (false? (:optimization-model-complete? contract)))
    (is (some #{:check-elision-authority} (:does-not-own contract)))
    (is (some #{:target-lowering-authority} (:does-not-own contract)))
    (is (= #{'gravity.digest 'gravity.optimization-lowering 'gravity.optimization}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= (set (keys publics)) (set (keys c13/public-api))))
    (doseq [[name spec] c13/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c13-optimization-validate! :keyword-is-invokable}
           {:c13-optimization-governing-document ""}
           {:c13-optimization-diagnostic-ids [:bad]}
           {:optimization-lowering-diagnostic-messages {"C13-OK" :bad}}
           {:optimization-pass-contract-seed []}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c13/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)
        artifact
        (c13/with-operations
          (assoc (operations)
                 :optimization-decision-record
                 (fn [domain input index contract]
                   (swap! calls inc)
                   (shared/optimization-decision-record
                    domain input index contract)))
          #(c13/compiler-c13-optimization-source-artifact
            "c13-test.gravity" "source"))]
    (is (= 6 @calls))
    (is (= 6 (count (:optimization-decision-log artifact)))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c13/with-operations
             {:compiler-c13-optimization-source-artifact
              (fn [_ _] sentinel)}
             #(c13/compiler-c13-optimization-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity"))))))

(deftest optimization-engine-emits-complete-products
  (let [artifact
        (c13/with-operations
          (operations)
          #(c13/compiler-c13-optimization-source-artifact
            "c13-test.gravity" "source"))
        decisions (:optimization-decision-log artifact)]
    (is (= :gravity/stage0-c13-mir-optimization-artifact (:kind artifact)))
    (is (= :gravity/stage0-c12-domain-ir-architecture-artifact
           (:domain-ir-artifact-kind artifact)))
    (is (= 6 (count (:optimization-pass-registry artifact))))
    (is (= 6 (count decisions)))
    (is (= 6 (count (:invalidated-fact-ledger artifact))))
    (is (= 6 (count (:analysis-cache-records artifact))))
    (is (= 6 (count (:proof-and-certificate-usage artifact))))
    (is (= 6 (count (:post-pass-verifier-reports artifact))))
    (is (some seq (map :changed-ops decisions)))
    (is (some empty? (map :changed-ops decisions)))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (not (contains? artifact :residual-cost-report)))
    (is (not-any? #{:residual-cost-report} (get-in artifact [:pass :emits])))
    (is (not (contains? (:capability-based-proof artifact) :residual-cost-visible?)))
    (is (not (contains? (:c13-optimization-results artifact) :residual-cost-status)))
    (doseq [check [:proof-evidence-present? :check-elision-proof?
                   :effect-order-preserved? :safety-outcomes-current?
                   :post-pass-verifiers-passed?]]
      (is (true? (get-in artifact [:capability-based-proof check]))))
    (is (= (set shared/c13-optimization-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:optimization-diagnostic-stream
                              :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c13-identities
  (doseq [[failure expected-id]
          [[:contract "C13-CONTRACT"]
           [:preserve "C13-PRESERVE"]
           [:invalidate "C13-INVALIDATE"]
           [:proof "C13-PROOF"]
           [:check-elision "C13-CHECK-ELISION"]
           [:effect "C13-EFFECT"]
           [:safety "C13-SAFETY"]
           [:domain "C13-DOMAIN"]
           [:nondeterminism "C13-NONDETERMINISM"]
           [:verify "C13-VERIFY"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module
                      [:metadata :compiler :c13-optimization :fail]
                      failure)
            error
            (try
              (c13/with-operations
                (operations failed-module)
                #(c13/compiler-c13-optimization-source-artifact
                  "c13-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))

(deftest native-file-dispatch-captures-invoke-before-reading-arguments
  (let [marker (Object.) calls (atom [])
        replacement (fn [& _] (throw (AssertionError. "Late invoke root was used")))
        original (fn [key _ & values] (swap! calls conj [key values]) marker)]
    (with-redefs [c13-ops/invoke original
                  clojure.core/slurp
                  (fn [path]
                    (is (= "native-port.qst" path))
                    (alter-var-root #'c13-ops/invoke (constantly replacement))
                    "native source text")]
      (is (identical? marker (artifact/file-artifact {} "native-port.qst"))))
    (is (= [[:compiler-c13-optimization-source-artifact
             ["native-port.qst" "native source text"]]] @calls))))

(deftest private-digest-root-is-captured-before-serialization-callback
  ;; Trusted native callback sequencing only; these supplied digest strings
  ;; are not accepted source identities or proof/certificate authority.
  (let [hash-var (ns-resolve 'gravity.c13-optimization.projection 'sha256-hex)
        calls (atom [])
        old-hash (fn [_] (swap! calls conj :old) "old")
        new-hash (fn [_] (swap! calls conj :new) "new")
        pass (proxy [Object] []
               (toString [] (alter-var-root hash-var (constantly new-hash)) "pass"))
        decision {:pass pass :output-mir :output :decision-id :decision :proofs-used []}
        result (with-redefs-fn {hash-var old-hash}
                 #(projection/artifact-base {} "source" {} {} {} :input
                                            [] [decision] {}))]
    (is (= [:old :old] @calls))
    (is (= "sha256:old" (get-in result [:analysis-cache-records 0 :cache-key])))))

(deftest source-assembly-keeps-public-leaf-roots-live-between-steps
  (let [calls (atom []) base {:native :base} proof {:native :projection}
        after (fn [configuration path value]
                (is (map? configuration)) (is (= "late-source.gravity" path))
                (is (identical? base value)) (swap! calls conj :late-validation) :complete)
        result
        (with-redefs [projection/artifact-base
                      (fn [& _]
                        (alter-var-root #'validation/validate! (constantly after))
                        (swap! calls conj :projection) base)
                      validation/validate!
                      (fn [& _] (throw (AssertionError. "Early validation root was retained")))
                      validation/capability-proof
                      (fn [_ value] (is (identical? base value)) proof)]
          (c13/with-operations
            (assoc (operations) :c4-artifact-id (constantly "native-port-result"))
            #(c13/compiler-c13-optimization-source-artifact "late-source.gravity" "source")))]
    (is (= [:projection :late-validation] @calls))
    (is (= {:native :base :capability-based-proof proof :artifact-id "native-port-result"} result))))

(deftest native-validation-retains-all-refusal-order-and-callback-errors
  (let [value (c13/with-operations (operations)
                #(c13/compiler-c13-optimization-source-artifact "c13-test.gravity" "source"))
        changed (-> value
                    (update-in [:optimization-pass-registry 0] dissoc :pass)
                    (assoc-in [:optimization-pipeline-manifest :ordering] :changed)
                    (assoc-in [:optimization-decision-log 0 :preserved] [])
                    (assoc :invalidated-fact-ledger [] :analysis-cache-records []
                           :proof-and-certificate-usage [])
                    (assoc-in [:optimization-decision-log 0 :proofs-used] [])
                    (assoc-in [:check-elision-record :status] :changed)
                    (assoc-in [:effect-reordering-record :status] :changed)
                    (assoc-in [:safety-outcome-refresh-report :status] :changed)
                    (assoc-in [:domain-anchor-transform-report :status] :changed)
                    (assoc-in [:optimization-replay-record :status] :changed)
                    (assoc-in [:post-pass-verifier-reports 0 :status] :failed)
                    (assoc-in [:optimization-diagnostic-stream :diagnostics] []))
        calls (atom []) marker (Object.)]
    ;; A returning failure callback retains the old complete marker, not a
    ;; certificate. Actual throwing refusals are also preserved separately.
    (is (= :complete
           (c13/with-operations
             {:optimization-lowering-fail!
              (fn [id path actual _ _]
                (is (= "ordered-native.qst" path)) (is (identical? changed actual))
                (swap! calls conj id) marker)}
             #(c13/c13-optimization-validate! "ordered-native.qst" changed))))
    (is (= ["C13-CONTRACT" "C13-CONTRACT" "C13-NONDETERMINISM" "C13-PRESERVE"
            "C13-INVALIDATE" "C13-INVALIDATE" "C13-PROOF" "C13-PROOF"
            "C13-CHECK-ELISION" "C13-EFFECT" "C13-SAFETY" "C13-DOMAIN"
            "C13-NONDETERMINISM" "C13-VERIFY" "C13-CONTRACT"] @calls))
    (let [error (ex-info "Original failure callback" {})
          observed (try
                     (c13/with-operations {:optimization-lowering-fail! (fn [& _] (throw error))}
                       #(c13/c13-optimization-validate! "throwing-native.gravity" changed))
                     nil (catch clojure.lang.ExceptionInfo observed observed))]
      (is (identical? error observed)))))
