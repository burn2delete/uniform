(ns gravity.c17-plugin-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c17-plugin :as c17]))

(def module
  {:module 'gravity.c17-test
   :source-path "c17-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c16-artifact
  {:kind :gravity/stage0-c16-incremental-compilation-artifact
   :task "P06-D095"
   :artifact-id "sha256:c16"
   :governing-document "docs/c16.md"
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c17-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c16-incremental-source-artifact (fn [_ _] c16-artifact)}))

(deftest contract-is-hosted-evidence-and-not-a-plugin-runtime
  (let [contract (c17/c17-engine-contract)
        publics (ns-publics 'gravity.c17-plugin)]
    (is (= :hosted-stage0-c17-plugin-evidence (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c17-authority? contract)))
    (is (false? (:plugin-runtime-implementation? contract)))
    (is (false? (:plugin-model-complete? contract)))
    (is (some #{:plugin-loading} (:does-not-own contract)))
    (is (some #{:sandbox-enforcement} (:does-not-own contract)))
    (is (some #{:compiler-capability-grants} (:does-not-own contract)))
    (is (some #{:signature-verification} (:does-not-own contract)))
    (is (= #{'clojure.set 'clojure.string
             'gravity.compiler-verification-shared 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= (set (keys publics)) (set (keys c17/public-api))))
    (doseq [[name spec] c17/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c17-plugin-diagnostic-stream :keyword-is-invokable}
           {:c17-plugin-governing-document ""}
           {:c17-plugin-diagnostic-ids [:bad]}
           {:c17-plugin-manifest-required-fields [:artifact "bad"]}
           {:c17-plugin-pass-contract-required-fields []}
           {:c17-plugin-cache-key-required-fields [:artifact :plugin "bad"]}
           {:compiler-verification-diagnostic-messages {"C17-OK" :bad}}
           {:compiler-verification-override-diagnostics
            {:c17-api ["C17-API" "bad"]}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c17/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)
        original c17/c17-plugin-diagnostic-stream
        artifact
        (c17/with-operations
          (assoc (operations)
                 :c17-plugin-diagnostic-stream
                 (fn [path manifest input]
                   (swap! calls inc)
                   (assoc (original path manifest input) :interposed? true)))
          #(c17/compiler-c17-plugin-source-artifact
            "c17-test.gravity" "source"))]
    (is (= 1 @calls))
    (is (true? (get-in artifact [:plugin-diagnostic-stream :interposed?]))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c17/with-operations
             {:compiler-c17-plugin-source-artifact (fn [_ _] sentinel)}
             #(c17/compiler-c17-plugin-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity"))))))

(deftest plugin-evidence-engine-emits-complete-products
  (let [artifact
        (c17/with-operations
          (operations)
          #(c17/compiler-c17-plugin-source-artifact
            "c17-test.gravity" "source"))]
    (is (= :gravity/stage0-c17-compiler-plugin-artifact (:kind artifact)))
    (is (= :gravity/stage0-c16-incremental-compilation-artifact
           (:incremental-artifact-kind artifact)))
    (is (= :gravity/compiler-plugin
           (get-in artifact [:plugin-manifest :artifact])))
    (is (= :compatible
           (get-in artifact [:api-compatibility-report :status])))
    (is (= 2 (count (:trust-grants artifact))))
    (is (= :denied-ungranted-effects
           (get-in artifact [:hermetic-build-effect-report :status])))
    (is (= 2 (count (:plugin-pass-registration-records artifact))))
    (is (= 1 (count (:domain-registration-records artifact))))
    (is (= 1 (count (:facet-registration-records artifact))))
    (is (= 2 (count (:plugin-cache-keys artifact))))
    (is (= 2 (count (:plugin-output-artifacts artifact))))
    (is (= 2 (count (:plugin-execution-traces artifact))))
    (is (= :passed (get-in artifact [:plugin-conformance-results :status])))
    (is (= (set c17/c17-plugin-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:plugin-diagnostic-stream :diagnostics])))))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c17-identities
  (doseq [[failure expected-id]
          [[:c17-manifest "C17-MANIFEST"]
           [:c17-api "C17-API"]
           [:c17-capability "C17-CAPABILITY"]
           [:c17-build-effect "C17-BUILD-EFFECT"]
           [:c17-sandbox "C17-SANDBOX"]
           [:c17-pass-contract "C17-PASS-CONTRACT"]
           [:c17-output "C17-OUTPUT"]
           [:c17-domain "C17-DOMAIN"]
           [:c17-facet "C17-FACET"]
           [:c17-trust "C17-TRUST"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module [:metadata :compiler :c17-plugin :fail] failure)
            error
            (try
              (c17/with-operations
                (operations failed-module)
                #(c17/compiler-c17-plugin-source-artifact
                  "c17-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))
