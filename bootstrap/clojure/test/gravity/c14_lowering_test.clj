(ns gravity.c14-lowering-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c14-lowering :as c14]
            [gravity.c14-lowering.artifact :as artifact]
            [gravity.c14-lowering.operations :as c14-ops]
            [gravity.c14-lowering.projection :as projection]
            [gravity.c14-lowering.validation :as validation]
            [gravity.optimization-lowering :as shared]))

(def module
  {:module 'gravity.c14-test
   :source-path "c14-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{:io/stdout}
   :safety :safe
   :metadata {}})

(def c13-artifact
  {:kind :gravity/stage0-c13-mir-optimization-artifact
   :task "P06-D092"
   :artifact-id "sha256:c13"
   :governing-document "docs/c13.md"
   :optimized-mir-artifact
   {:artifact :gravity/optimized-mir
    :output "sha256:optimized-mir"
    :source-origin-map []
    :status :complete}
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c14-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c13-optimization-source-artifact (fn [_ _] c13-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c14/c14-engine-contract)
        publics (ns-publics 'gravity.c14-lowering)]
    (is (= :hosted-stage0-c14-target-lowering
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c14-authority? contract)))
    (is (false? (:lowering-model-complete? contract)))
    (is (some #{:abi-authority} (:does-not-own contract)))
    (is (some #{:runtime-provider-authority} (:does-not-own contract)))
    (is (= #{'gravity.digest 'gravity.optimization-lowering 'gravity.lowering}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= (set (keys publics)) (set (keys c14/public-api))))
    (doseq [[name spec] c14/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c14-lowering-validate! :keyword-is-invokable}
           {:c14-lowering-governing-document ""}
           {:c14-lowering-diagnostic-ids [:bad]}
           {:optimization-lowering-diagnostic-messages {"C14-OK" :bad}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c14/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)
        artifact
        (c14/with-operations
          (assoc (operations)
                 :c14-lowering-diagnostic-catalog
                 (fn [source-path input-id]
                   (swap! calls inc)
                   {:artifact :gravity/c14-lowering-diagnostic-catalog
                    :status :complete
                    :diagnostics
                    (mapv (fn [id]
                            {:diagnostic id
                             :input-artifact-id input-id
                             :source-span {:source source-path
                                           :form-index 0}})
                          shared/c14-lowering-diagnostic-ids)}))
          #(c14/compiler-c14-lowering-source-artifact
            "c14-test.gravity" "source"))]
    (is (= 1 @calls))
    (is (= (set shared/c14-lowering-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:lowering-diagnostic-stream :diagnostics]))))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c14/with-operations
             {:compiler-c14-lowering-source-artifact (fn [_ _] sentinel)}
             #(c14/compiler-c14-lowering-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity"))))))

(deftest lowering-engine-emits-complete-products
  (let [artifact
        (c14/with-operations
          (operations)
          #(c14/compiler-c14-lowering-source-artifact
            "c14-test.gravity" "source"))]
    (is (= :gravity/stage0-c14-target-lowering-artifact (:kind artifact)))
    (is (= :gravity/stage0-c13-mir-optimization-artifact
           (:optimization-artifact-kind artifact)))
    (is (= :optimized-mir (get-in artifact [:lowering-request :input :kind])))
    (is (= :eligible
           (get-in artifact [:target-eligibility-report :status])))
    (is (= :complete (get-in artifact [:abi-manifest :status])))
    (is (= :complete
           (get-in artifact [:runtime-provider-manifest :status])))
    (is (= 3 (count (:provider-selection-records artifact))))
    (is (= 3 (count (get-in artifact
                             [:proof-to-target-metadata-map :entries]))))
    (is (= :gravity/target-artifact-manifest
           (get-in artifact [:target-artifact-manifest :artifact])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set shared/c14-lowering-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:lowering-diagnostic-stream :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c14-identities
  (doseq [[failure expected-id]
          [[:input "C14-INPUT"]
           [:profile "C14-PROFILE"]
           [:target "C14-TARGET"]
           [:abi "C14-ABI"]
           [:runtime "C14-RUNTIME"]
           [:provider "C14-PROVIDER"]
           [:proof-metadata "C14-PROOF-METADATA"]
           [:capability "C14-CAPABILITY"]
           [:unsupported "C14-UNSUPPORTED"]
           [:manifest "C14-MANIFEST"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module [:metadata :compiler :c14-lowering :fail] failure)
            error
            (try
              (c14/with-operations
                (operations failed-module)
                #(c14/compiler-c14-lowering-source-artifact
                  "c14-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))

(deftest native-file-dispatch-captures-invoke-before-reading-arguments
  (let [marker (Object.) calls (atom [])
        replacement (fn [& _] (throw (AssertionError. "Late invoke root was used")))
        original (fn [key _ & values] (swap! calls conj [key values]) marker)]
    (with-redefs [c14-ops/invoke original
                  clojure.core/slurp
                  (fn [path]
                    (is (= "native-port.qst" path))
                    (alter-var-root #'c14-ops/invoke (constantly replacement))
                    "native source text")]
      (is (identical? marker (artifact/file-artifact {} "native-port.qst"))))
    (is (= [[:compiler-c14-lowering-source-artifact
             ["native-port.qst" "native source text"]]] @calls))))

(deftest native-projection-retains-digest-lookup-before-serialization
  ;; Callback timing only: these strings confer no source identity or proof trust.
  (let [hash-var (ns-resolve 'gravity.c14-lowering.projection 'sha256-hex)
        calls (atom []) old (fn [_] (swap! calls conj :old) "old")
        after (fn [_] (swap! calls conj :after) "after")
        output (proxy [Object] []
                 (toString [] (alter-var-root hash-var (constantly after)) "output"))
        result (with-redefs-fn {hash-var old}
                 #(projection/artifact-base {} "native-port.gravity" "source" {} {}
                                            {:optimized-mir-artifact {:output output}} {}))]
    (is (= [:old :after] @calls))
    (is (= "sha256:old" (get-in result [:target-artifact-manifest :artifacts 0 :hash])))
    (is (= "sha256:after" (get-in result [:target-artifact-manifest :target])))))

(deftest source-assembly-keeps-leaf-roots-live-between-steps
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
          (c14/with-operations
            (assoc (operations) :c4-artifact-id (constantly "native-port-result"))
            #(c14/compiler-c14-lowering-source-artifact "late-source.gravity" "source")))]
    (is (= [:projection :late-validation] @calls))
    (is (= {:native :base :capability-based-proof proof :artifact-id "native-port-result"} result))))

(deftest native-validation-retains-refusal-order-and-exception-identity
  (let [value (c14/with-operations (operations)
                #(c14/compiler-c14-lowering-source-artifact "c14-test.gravity" "source"))
        changed (-> value
                    (assoc-in [:lowering-request :input :kind] :changed)
                    (assoc-in [:target-eligibility-report :status] :changed)
                    (assoc-in [:lowering-request :target :features] #{})
                    (assoc-in [:abi-manifest :status] :changed)
                    (assoc-in [:runtime-provider-manifest :status] :changed)
                    (assoc-in [:capability-preservation-report :status] :changed)
                    (assoc-in [:provider-selection-records 0 :status] :changed)
                    (assoc-in [:proof-to-target-metadata-map :entries 0 :proof] nil)
                    (assoc-in [:unsupported-feature-report 0 :fallback-status] :changed)
                    (assoc-in [:target-artifact-manifest :artifact] :changed)
                    (assoc-in [:lowering-diagnostic-stream :diagnostics] []))
        calls (atom [])]
    ;; A returning failure callback preserves the old marker, without acceptance.
    (is (= :complete
           (c14/with-operations
             {:optimization-lowering-fail!
              (fn [id path actual _ _]
                (is (= "ordered-native.qst" path)) (is (identical? changed actual))
                (swap! calls conj id) :returned)}
             #(c14/c14-lowering-validate! "ordered-native.qst" changed))))
    (is (= ["C14-INPUT" "C14-PROFILE" "C14-TARGET" "C14-ABI" "C14-RUNTIME"
            "C14-CAPABILITY" "C14-PROVIDER" "C14-PROOF-METADATA" "C14-UNSUPPORTED"
            "C14-MANIFEST" "C14-MANIFEST"] @calls))
    (let [error (ex-info "Original failure callback" {})
          observed (try
                     (c14/with-operations {:optimization-lowering-fail! (fn [& _] (throw error))}
                       #(c14/c14-lowering-validate! "throwing-native.gravity" changed))
                     nil (catch clojure.lang.ExceptionInfo observed observed))]
      (is (identical? error observed)))))

(deftest native-empty-collection-predicates-remain-nonauthoritative
  (let [value (c14/with-operations (operations)
                #(c14/compiler-c14-lowering-source-artifact "c14-test.gravity" "source"))
        empty-record (assoc value :provider-selection-records [] :unsupported-feature-report []
                            :proof-to-target-metadata-map {:entries []})
        proof (c14/c14-lowering-capability-proof empty-record)]
    (is (= :complete (c14/c14-lowering-validate! "native-empty.qst" empty-record)))
    (is (true? (:providers-selected? proof)))
    (is (true? (:proof-metadata-linked? proof)))
    (is (true? (:unsupported-fallbacks-recorded? proof)))))
