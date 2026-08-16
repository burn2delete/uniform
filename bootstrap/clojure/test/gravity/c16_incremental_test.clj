(ns gravity.c16-incremental-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c16-incremental :as c16]))

(def module
  {:module 'gravity.c16-test
   :source-path "c16-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c15-artifact
  {:kind :gravity/stage0-c15-compiler-diagnostics-artifact
   :task "P06-D094"
   :artifact-id "sha256:c15"
   :governing-document "docs/c15.md"
   :diagnostic-stream {:artifact :gravity/diagnostic-stream}
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c16-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c15-diagnostics-source-artifact (fn [_ _] c15-artifact)}))

(deftest contract-is-hosted-evidence-and-not-a-cache-implementation
  (let [contract (c16/c16-engine-contract)
        publics (ns-publics 'gravity.c16-incremental)]
    (is (= :hosted-stage0-c16-incremental-evidence
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c16-authority? contract)))
    (is (false? (:cache-implementation? contract)))
    (is (false? (:incremental-model-complete? contract)))
    (is (some #{:content-addressed-pass-cache} (:does-not-own contract)))
    (is (some #{:actual-artifact-reuse} (:does-not-own contract)))
    (is (some #{:release-reproducibility-proof} (:does-not-own contract)))
    (is (= #{'clojure.set 'clojure.string
             'gravity.compiler-verification-shared 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (some #{'gravity.c2-pass-cache}
              (get-in contract [:dependency-direction :forbids])))
    (is (= (set (keys publics)) (set (keys c16/public-api))))
    (doseq [[name spec] c16/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c16-stage-cache-key :keyword-is-invokable}
           {:c16-incremental-governing-document ""}
           {:c16-incremental-diagnostic-ids [:bad]}
           {:c16-cache-key-required-fields [:artifact "bad"]}
           {:c16-invalidation-causes []}
           {:compiler-verification-diagnostic-messages {"C16-OK" :bad}}
           {:compiler-verification-override-diagnostics
            {:c16-key ["C16-KEY" "bad"]}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c16/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom [])
        artifact
        (c16/with-operations
          (assoc (operations)
                 :c16-stage-cache-key
                 (fn [stage source dependency]
                   (swap! calls conj stage)
                   (assoc (c16/c16-stage-cache-key stage source dependency)
                          :interposed? true)))
          #(c16/compiler-c16-incremental-source-artifact
            "c16-test.gravity" "source"))]
    (is (= 8 (count @calls)))
    (is (every? :interposed? (:stage-cache-keys artifact))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c16/with-operations
             {:compiler-c16-incremental-source-artifact (fn [_ _] sentinel)}
             #(c16/compiler-c16-incremental-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity"))))))

(deftest incremental-evidence-engine-emits-complete-products
  (let [artifact
        (c16/with-operations
          (operations)
          #(c16/compiler-c16-incremental-source-artifact
            "c16-test.gravity" "source"))]
    (is (= :gravity/stage0-c16-incremental-compilation-artifact
           (:kind artifact)))
    (is (= :gravity/stage0-c15-compiler-diagnostics-artifact
           (:diagnostics-artifact-kind artifact)))
    (is (= :consistent
           (get-in artifact [:incremental-dependency-graph :status])))
    (is (= 8 (count (:stage-cache-keys artifact))))
    (is (= 8 (count (:cache-entry-manifest artifact))))
    (is (= 19 (count (:invalidation-trace artifact))))
    (is (every? #(= :required-before-release (:revalidation %))
                (:cache-entry-manifest artifact)))
    (is (= :rejected
           (get-in artifact [:stale-proof-rejection-report :status])))
    (is (= :rejected
           (get-in artifact [:stale-diagnostic-rejection-report :status])))
    (is (= :blocked-from-release
           (get-in artifact [:speculative-reuse-record :publish-status])))
    (is (= :complete
           (get-in artifact [:build-effect-replay-record :status])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set c16/c16-incremental-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:incremental-diagnostic-stream
                              :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c16-identities
  (doseq [[failure expected-id]
          [[:c16-key "C16-KEY"]
           [:c16-entry "C16-ENTRY"]
           [:c16-stale "C16-STALE"]
           [:c16-proof "C16-PROOF"]
           [:c16-speculative "C16-SPECULATIVE"]
           [:c16-replay "C16-REPLAY"]
           [:c16-policy "C16-POLICY"]
           [:c16-diagnostic "C16-DIAGNOSTIC"]
           [:c16-graph "C16-GRAPH"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module [:metadata :compiler :c16-incremental :fail]
                      failure)
            error
            (try
              (c16/with-operations
                (operations failed-module)
                #(c16/compiler-c16-incremental-source-artifact
                  "c16-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))
