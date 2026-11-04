(ns gravity.c10-safety-analysis-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c10-safety-analysis :as c10]
            [gravity.c10-safety-analysis.verification :as verification]
            [gravity.safety :as safety]))

(def module
  {:module 'gravity.c10-test
   :source-path "c10-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{:io/write}
   :capabilities #{:io/stdout}
   :safety :safe
   :metadata {}})

(def c9-artifact
  {:kind :gravity/stage0-c9-ownership-checker-artifact
   :artifact-id "sha256:c9"
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _]
      [{:form '(ns gravity.c10-test (:profile :hosted) (:target :jvm))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c9-ownership-source-artifact (fn [_ _] c9-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c10/c10-engine-contract)
        publics (ns-publics 'gravity.c10-safety-analysis)]
    (is (= :hosted-stage0-c10-safety-analysis
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c10-authority? contract)))
    (is (false? (:safety-model-complete? contract)))
    (is (some #{:safe1-classification-authority} (:does-not-own contract)))
    (is (some #{:proof-certificate-authority} (:does-not-own contract)))
    (is (true? (get-in contract
                       [:operation-interposition
                        :unknown-keys-rejected?])))
    (is (= #{'clojure.set 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= (set (keys publics)) (set (keys c10/public-api))))
    (doseq [[name spec] c10/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c10-safety-capability-proof :keyword-is-invokable}
           {:c10-safety-diagnostic-ids [:not-a-string]}
           {:c10-safety-governing-document ""}
           {:c10-safety-rejected-designs [:not-a-map]}
           {:c10-safety-override-diagnostics {:unsafe :not-a-string}}
           {:c10-safe-outcomes #{"not-a-keyword"}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c10/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)
        proof {:operation-inventory-complete? true
               :exactly-one-outcome-per-operation? true
               :runtime-checks-emitted? true
               :proof-obligations-discharged? true
               :certificate-references-recorded? true
               :unsafe-island-audits-complete? true
               :taint-and-capability-reports-complete? true
               :generated-provenance-recorded? true
               :optimization-evidence-preserved? true
               :diagnostics-covered? true
               :verifier-passed? true}]
    (is (= :complete
           (c10/with-operations
             {:c10-safety-capability-proof
              (fn [_] (swap! calls inc) proof)}
             #(c10/c10-safety-validate! "c10-test.gravity" {}))))
    (is (= 1 @calls))))

(deftest safety-engine-emits-complete-products
  (let [artifact (c10/with-operations
                   (operations)
                   #(c10/compiler-c10-safety-source-artifact
                     "c10-test.gravity" "ignored"))
        outcomes (get-in artifact [:safety-outcome-records :records])]
    (is (= :gravity/stage0-c10-safety-analysis-artifact (:kind artifact)))
    (is (= 12 (count (get-in artifact
                             [:safety-operation-inventory :records]))))
    (is (= 12 (count outcomes)))
    (is (= #{:proven-safe :runtime-checked :unsafe-island}
           (set (map :outcome outcomes))))
    (is (= 3 (count (get-in artifact [:runtime-check-list :records]))))
    (is (= 7 (count (get-in artifact [:proof-obligation-list :records]))))
    (is (= 3 (count (get-in artifact
                            [:proof-certificate-references :records]))))
    (is (= 2 (count (get-in artifact
                            [:unsafe-island-audit-manifest :records]))))
    (is (= 1 (count (get-in artifact
                            [:taint-capability-safety-report
                             :taint-records]))))
    (is (= 1 (count (get-in artifact
                            [:taint-capability-safety-report
                             :capability-records]))))
    (is (= 1 (count (get-in artifact
                            [:generated-code-safety-provenance :records]))))
    (is (= 2 (count (get-in artifact
                            [:optimization-safety-preservation :records]))))
    (is (= :passed (get-in artifact [:safety-verifier-report :status])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set c10/c10-safety-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact [:safety-diagnostics :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c10-identities
  (doseq [[failure expected-id]
          [[:no-outcome "C10-NO-OUTCOME"]
           [:proof "C10-PROOF"]
           [:check "C10-CHECK"]
           [:unsafe "C10-UNSAFE"]
           [:generated "C10-GENERATED"]
           [:taint "C10-TAINT"]
           [:capability "C10-CAPABILITY"]
           [:ffi "C10-FFI"]
           [:numeric "C10-NUMERIC"]
           [:optimization "C10-OPTIMIZATION"]]]
    (testing expected-id
      (let [failed-module (assoc-in module
                                    [:metadata :compiler
                                     :c10-safety-analysis :fail]
                                    failure)
            error (try
                    (c10/with-operations
                      (operations failed-module)
                      #(c10/compiler-c10-safety-source-artifact
                        "c10-test.gravity" "ignored"))
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))
        (is (= "C10" (:document-id (ex-data error))))))))

(defn- native-artifact []
  (c10/with-operations (operations)
    #(c10/compiler-c10-safety-source-artifact "c10-test.gravity" "ignored")))

(defn- report-inputs [artifact]
  [c10/c10-safe-outcomes c10/c10-safety-diagnostic-ids c9-artifact
   (:safety-operation-inventory artifact) (:safety-outcome-records artifact)
   (:runtime-check-list artifact) (:proof-obligation-list artifact)
   (:proof-certificate-references artifact) (:unsafe-island-audit-manifest artifact)
   (:taint-capability-safety-report artifact) (:generated-code-safety-provenance artifact)
   (:optimization-safety-preservation artifact) (:safety-diagnostics artifact)])

(deftest stage-report-predicates-and-original-projections
  (let [artifact (native-artifact) inputs (report-inputs artifact)
        report (apply safety/native-verifier-report inputs)]
    (is (= (:safety-verifier-report artifact) report))
    (is (= (c10/c10-safety-capability-proof artifact)
           (safety/native-capability-proof artifact)))
    (is (= :passed (:status report)))
    ;; These are intentionally the original sequence/truthy values.
    (doseq [key [:certificate-references-recorded? :generated-provenance-recorded?]]
      (is (seq? (get report key)))
      (is (true? (get (safety/native-capability-proof artifact) key))))
    (doseq [[index path value field]
            [[2 [:capability-based-proof :status] :failed :c9-proof-complete?]
             [3 [:records] [] :operation-inventory-complete?]
             [4 [:records 0 :outcome] :unclassified :exactly-one-outcome-per-operation?]
             [4 [:records 0 :operation] :unknown-operation :exactly-one-outcome-per-operation?]
             [5 [:records] [] :runtime-checks-emitted?]
             [6 [:records 0 :status] :pending :proof-obligations-discharged?]
             [7 [:records] [] :certificate-references-recorded?]
             [8 [:records] [] :unsafe-island-audits-complete?]
             [9 [:taint-records] [] :taint-and-capability-reports-complete?]
             [9 [:capability-records] [] :taint-and-capability-reports-complete?]
             [10 [:records] [] :generated-provenance-recorded?]
             [11 [:records 0 :status] :stale :optimization-evidence-preserved?]
             [12 [:diagnostics] [] :diagnostics-covered?]]]
      (let [changed (update inputs index assoc-in path value)
            result (apply safety/native-verifier-report changed)]
        (is (not (get result field)) (pr-str [index path field]))
        (is (= :failed (:status result)))))
    (is (= :failed (:status (apply safety/native-verifier-report
                                   (assoc inputs 0 #{})))))
    ;; The original overall status omits the inventory-status predicate.
    ;; The false field still causes the subsequent native validator to refuse.
    (let [result (apply safety/native-verifier-report (assoc-in inputs [3 :status] :missing))
          changed (assoc artifact :safety-verifier-report result)
          error (try (c10/c10-safety-validate! "inventory-status.qst" changed)
                     nil (catch clojure.lang.ExceptionInfo error error))]
      (is (= :passed (:status result)))
      (is (false? (:operation-inventory-complete? result)))
      (is (= "C10-NO-OUTCOME" (:id (ex-data error)))))
    ;; Native compatibility does not prove multiplicity or certificate trust.
    (let [duplicated (update-in inputs [4 :records] #(conj % (first %)))
          vacuous (-> inputs (assoc-in [6 :records] []) (assoc-in [11 :records] []))]
      (is (= :passed (:status (apply safety/native-verifier-report duplicated))))
      (is (= :passed (:status (apply safety/native-verifier-report vacuous)))))))

(def native-refusals
  [[:operation-inventory-complete? "C10-NO-OUTCOME"]
   [:exactly-one-outcome-per-operation? "C10-NO-OUTCOME"]
   [:runtime-checks-emitted? "C10-CHECK"]
   [:proof-obligations-discharged? "C10-PROOF"]
   [:certificate-references-recorded? "C10-PROOF"]
   [:unsafe-island-audits-complete? "C10-UNSAFE"]
   [:taint-and-capability-reports-complete? "C10-TAINT"]
   [:generated-provenance-recorded? "C10-GENERATED"]
   [:optimization-evidence-preserved? "C10-OPTIMIZATION"]
   [:diagnostics-covered? "C10-NO-OUTCOME"]
   [:verifier-passed? "C10-NO-OUTCOME"]])

(deftest native-validation-preserves-order-truthiness-and-real-refusals
  (let [artifact (native-artifact) proof (safety/native-capability-proof artifact)]
    (doseq [[field id] native-refusals value [false nil]]
      (let [changed (assoc proof field value)
            error (try
                    (c10/with-operations {:c10-safety-capability-proof (constantly changed)}
                      #(c10/c10-safety-validate! "exact-native-path.qst" artifact))
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= id (:id (ex-data error))))
        (is (= [field] (:missing-fields (ex-data error))))
        (is (= {:source "exact-native-path.qst" :form-index 0}
               (:source-span (ex-data error))))))
    (let [calls (atom []) path (Object.) token (Object.)
          result (safety/validate-native-report!
                  (fn [input] (is (identical? artifact input)) (swap! calls conj :projection) {})
                  (fn [id actual-path subject extra]
                    (is (identical? path actual-path))
                    (is (= {:stage :safety-analysis} subject))
                    (swap! calls conj [id (:missing-fields extra)]) token)
                  path artifact)]
      (is (= :complete result))
      (is (= (into [:projection] (map (fn [[field id]] [id [field]]) native-refusals)) @calls)))
    (is (= :complete (safety/validate-native-report!
                      (constantly (zipmap (map first native-refusals) (repeat :truthy)))
                      (fn [& _] (throw (ex-info "Unexpected refusal" {}))) nil artifact)))
    (doseq [phase [:projection :failure]]
      (let [failure (ex-info "Original callback error" {:phase phase})
            observed (try
                       (safety/validate-native-report!
                        (if (= phase :projection) (fn [_] (throw failure)) (constantly {}))
                        (fn [& _] (throw failure)) "native-error.gravity" artifact)
                       nil (catch clojure.lang.ExceptionInfo error error))]
        (is (identical? failure observed))))))

(deftest compatibility-leaf-forwards-exact-native-arguments-and-results
  (let [artifact (native-artifact) inputs (report-inputs artifact)
        marker (Object.) calls (atom [])]
    (with-redefs [safety/native-verifier-report
                  (fn [& values] (swap! calls conj [:report values]) marker)
                  safety/native-capability-proof
                  (fn [value] (swap! calls conj [:projection value]) marker)
                  safety/validate-native-report!
                  (fn [& values] (swap! calls conj [:validate values]) marker)]
      (is (identical? marker (apply verification/verifier-report inputs)))
      (is (identical? marker (verification/capability-proof artifact)))
      (is (identical? marker (verification/validate! identity identity :path artifact)))
      (is (= [[:report inputs] [:projection artifact]
              [:validate [identity identity :path artifact]]] @calls)))))
