(ns gravity.backend.workflow.native-manifest-test
  "Cold workflow document data; no source admission or workflow execution."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.workflow :as workflow]
            [gravity.digest :as digest]))

(def ^:private diagnostic-values
  ["B10-SCHEMA" "B10-REPLAY" "B10-IDEMPOTENCY" "B10-RETRY" "B10-COMPENSATION"
   "B10-CAPABILITY" "B10-POLICY" "B10-TAINT" "B10-GRAPH" "B10-MANIFEST"])
(defn- hash-text [text] (digest/sha256-hex text))
(defn- publish-refusal [id path subject extra]
  (throw (ex-info "Caller-owned unverified workflow document refusal"
                  (merge {:id id :source-path path :subject subject} extra))))
(defn- ports []
  {:artifact-id #'hash-text :workflow-graph #'workflow/workflow-graph :replay-fixture #'workflow/replay-fixture
   :workflow-graph-structurally-valid? #'workflow/workflow-graph-structurally-valid?
   :replay-fixture-structurally-valid? #'workflow/replay-fixture-structurally-valid?
   :diagnostic-ids #'diagnostic-values :fail! #'publish-refusal})
(defn- unverified-data []
  {:fixture :forged-unverified-document-data
   :specialized-lowering-artifact {:kind :gravity/stage0-specialized-lowering-artifact
                                   :capability-based-proof {:status :complete}}
   :workflow-backend-manifest (workflow/native-manifest (ports) "unverified-source.gravity" {:fixture :unverified-input})
   :b10-diagnostic-stream {:diagnostics (mapv #(hash-map :diagnostic %) diagnostic-values)}})
(defn- refusal [data]
  (try (workflow/validate-native-manifest! (ports) "workflow-fixture.gravity" data)
       nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest emits-fixed-graph-and-replay-with-deterministic-hashes-and-borrowed-input
  (let [input {:fixture :unverified-input} path "unverified-workflow.gravity"
        manifest (workflow/native-manifest (ports) path input)]
    (is (= manifest (workflow/native-manifest (ports) path input)))
    (is (identical? input (:input-artifact manifest)))
    (is (identical? input (get-in manifest [:workflow-ir-handoff-record :source-artifact])))
    (is (identical? input (get-in manifest [:source-debug-map :source])))
    (is (= (mapv #(str path %) [":workflow" ":steps" ":policy" ":replay"])
           (get-in manifest [:source-debug-map :locations])))
    (is (= [:mir :c11-mir :c12-workflow-domain-ir :c14-target-lowering :b1-interface :b10-workflow-graph-backend]
           (get-in manifest [:source-debug-map :generated-origin-chain])))
    (is (= :not-available-in-current-environment (get-in manifest [:external-runtime-validation-record :status])))
    (doseq [[field text filename sha]
            [[[:workflow-graph-artifact] workflow/workflow-graph "gravity_stage0_workflow.edn"
              "5072e27d66d95833a640ec6acea56c3e992976d4ee9a35e98800fc5413ecb127"]
             [[:replay-fixtures 0] workflow/replay-fixture "gravity_stage0_workflow_replay.edn"
              "de4413358afb6b74806bc28cecfb777e1539d60e8371f983e08f2238e42ba8fa"]]]
      (let [record (get-in manifest field) file (io/file "target/validation/workflow-native-manifest" filename)]
        (is (= text (:content record)))
        (is (= sha (digest/sha256-hex text) (:hash record)))
        (io/make-parents file) (spit file text)
        (is (= text (slurp file)))))))

(deftest rejects-the-existing-record-conditions
  (let [data (unverified-data) m :workflow-backend-manifest]
    (is (= :complete (workflow/validate-native-manifest! (ports) "unverified.gravity" data)))
    (doseq [[bad id missing]
            [[(dissoc data :specialized-lowering-artifact) "B10-MANIFEST" :specialized-lowering-artifact]
             [(assoc-in data [:specialized-lowering-artifact :capability-based-proof :status] :forged) "B10-MANIFEST" :specialized-lowering-proof]
             [(assoc-in data [m :target :runtime] :forged) "B10-MANIFEST" :target-runtime]
             [(assoc-in data [m :workflow-ir-handoff-record :status] :missing) "B10-MANIFEST" :workflow-ir-handoff-record]
             [(assoc-in data [m :workflow-graph-artifact :status] :missing) "B10-GRAPH" :workflow-graph-artifact]
             [(assoc-in data [m :step-schema-bundle :status] :missing) "B10-SCHEMA" :step-schema-bundle]
             [(assoc-in data [m :event-log-schema :status] :missing) "B10-REPLAY" :event-log-schema]
             [(assoc-in data [m :replay-policy :status] :missing) "B10-REPLAY" :replay-policy]
             [(assoc-in data [m :idempotency-key-map :status] :missing) "B10-IDEMPOTENCY" :idempotency-key-map]
             [(assoc-in data [m :retry-timeout-cancellation-compensation-table :status] :missing) "B10-RETRY" :retry-timeout-cancellation]
             [(update-in data [m :retry-timeout-cancellation-compensation-table :steps :call-tool] dissoc :compensation) "B10-COMPENSATION" :compensation-handlers]
             [(assoc-in data [m :external-capability-manifest :status] :missing) "B10-CAPABILITY" :external-capability-manifest]
             [(assoc-in data [m :external-capability-manifest :ambient-authority] :forged) "B10-CAPABILITY" :ambient-authority-rejection]
             [(assoc-in data [m :policy-graph :status] :missing) "B10-POLICY" :policy-graph]
             [(assoc-in data [m :human-review-policy-graph :status] :missing) "B10-POLICY" :human-review-policy-graph]
             [(assoc-in data [m :taint-validation-report :status] :missing) "B10-TAINT" :taint-validation-report]
             [(assoc-in data [m :taint-validation-report :unvalidated-trusted-sinks] [:unvalidated]) "B10-TAINT" :validated-trusted-sinks]
             [(assoc-in data [m :graph-validation-report :status] :missing) "B10-GRAPH" :graph-validation-report]
             [(assoc-in data [m :differential-replay-record :status] :missing) "B10-REPLAY" :differential-replay-record]
             [(assoc-in data [m :audit-provenance-record :status] :missing) "B10-MANIFEST" :audit-provenance-record]
             [(assoc-in data [m :source-debug-map :status] :missing) "B10-MANIFEST" :source-debug-map]
             [(update data m dissoc :tool-model-provider-manifest) "B10-MANIFEST" :workflow-artifact-manifest]
             [(assoc-in data [:b10-diagnostic-stream :diagnostics] []) "B10-MANIFEST" :b10-diagnostics]]]
      (let [result (refusal bad)]
        (is (= id (:id result)))
        (is (= [missing] (:missing-fields result)))
        (is (= "workflow-fixture.gravity" (:source-path result)))))))

(deftest removing-required-reference-tokens-causes-structural-refusals
  (let [data (unverified-data)]
    (doseq [[operation text tokens id missing]
            [[#'workflow/workflow-graph workflow/workflow-graph
              [":workflow :gravity-stage0-workflow" ":kind :model-call" ":kind :tool-call"
               ":kind :human-review-gate" ":kind :compensation" ":replay-barriers"] "B10-GRAPH" :workflow-graph-structure]
             [#'workflow/replay-fixture workflow/replay-fixture
              [":model-output-recorded" ":tool-output-recorded" ":human-reviewed" ":side-effects-reissued false"]
              "B10-REPLAY" :replay-fixture-structure]]]
      (doseq [token tokens]
        (with-redefs-fn {operation (str/replace text token "removed")}
          (fn [] (let [result (refusal data)]
                   (is (= id (:id result)))
                   (is (= [missing] (:missing-fields result))))))))))

(deftest retains-all-twenty-five-refusal-steps-and-subjects
  (let [calls (atom []) manifest {:retry-timeout-cancellation-compensation-table {:steps {:bad {}}}
                                 :taint-validation-report {:unvalidated-trusted-sinks [:bad]}}
        supplied (assoc (ports) :workflow-graph "invalid" :replay-fixture "invalid"
                        :fail! (fn [& args] (swap! calls conj args) false))]
    (is (= :complete (workflow/validate-native-manifest! supplied "ordered.gravity" {:workflow-backend-manifest manifest})))
    (is (= 25 (count @calls)))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof] [:target-runtime]
            [:workflow-ir-handoff-record] [:workflow-graph-artifact] [:workflow-graph-structure]
            [:step-schema-bundle] [:event-log-schema] [:replay-policy] [:replay-fixture-structure]
            [:idempotency-key-map] [:retry-timeout-cancellation] [:compensation-handlers]
            [:external-capability-manifest] [:ambient-authority-rejection] [:policy-graph]
            [:human-review-policy-graph] [:taint-validation-report] [:validated-trusted-sinks]
            [:graph-validation-report] [:differential-replay-record] [:audit-provenance-record]
            [:source-debug-map] [:workflow-artifact-manifest] [:b10-diagnostics]]
           (mapv #(:missing-fields (nth % 3)) @calls)))
    (is (every? nil? (map #(nth % 2) (take 2 @calls))))
    (is (every? #(identical? manifest (nth % 2)) (take 22 (drop 2 @calls))))
    (is (nil? (nth (last @calls) 2)))))

(deftest first-failure-preserves-the-caller-exception-object
  (let [error (Error. "Caller-owned workflow failure") calls (atom [])]
    (is (identical? error
                    (try (workflow/validate-native-manifest!
                          (assoc (ports) :fail! (fn [& args] (swap! calls conj args) (throw error))) "identity.gravity" {})
                         nil (catch Throwable actual actual))))
    (is (= [["B10-MANIFEST" "identity.gravity" nil {:missing-fields [:specialized-lowering-artifact]}]] @calls))))

(deftest failure-var-is-resolved-at-every-original-use
  (let [error (Error. "Late caller failure") calls (atom [])]
    (with-redefs [publish-refusal (fn [& args]
                                  (swap! calls conj args)
                                  (alter-var-root #'publish-refusal
                                                  (constantly (fn [& later]
                                                                (swap! calls conj later) (throw error)))))]
      (is (identical? error
                      (try (workflow/validate-native-manifest! (ports) "late.gravity" {})
                           nil (catch Throwable actual actual)))))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof]]
           (mapv #(:missing-fields (nth % 3)) @calls)))))

(deftest hash-calls-retain-distinct-late-function-and-text-reads
  (let [graph workflow/workflow-graph replay workflow/replay-fixture calls (atom [])]
    (with-redefs [workflow/workflow-graph graph workflow/replay-fixture replay
                  hash-text (fn [text]
                              (swap! calls conj [:graph text])
                              (alter-var-root #'workflow/workflow-graph (constantly (str graph "\n:first")))
                              (alter-var-root #'workflow/replay-fixture (constantly (str replay "\n:first")))
                              (alter-var-root #'hash-text
                                              (constantly (fn [later]
                                                            (swap! calls conj [:replay later])
                                                            (alter-var-root #'workflow/workflow-graph (constantly (str graph "\n:second")))
                                                            (alter-var-root #'workflow/replay-fixture (constantly (str replay "\n:second")))
                                                            (digest/sha256-hex later))))
                              (digest/sha256-hex text))]
      (let [manifest (workflow/native-manifest (ports) "late.gravity" :unverified-input)]
        (is (= [[:graph graph] [:replay (str replay "\n:first")]] @calls))
        (is (= (str graph "\n:second") (get-in manifest [:workflow-graph-artifact :content])))
        (is (= (str replay "\n:second") (get-in manifest [:replay-fixtures 0 :content])))
        (is (= (digest/sha256-hex graph) (get-in manifest [:workflow-graph-artifact :hash])))
        (is (= (digest/sha256-hex (str replay "\n:first")) (get-in manifest [:replay-fixtures 0 :hash])))))))

(deftest validation-and-coverage-preserve-different-predicate-orders
  (let [data (unverified-data) calls (atom [])
        supplied (assoc (ports)
                        :workflow-graph-structurally-valid? (fn [_] (swap! calls conj :graph) true)
                        :replay-fixture-structurally-valid? (fn [_] (swap! calls conj :replay) true))]
    (is (= :complete (workflow/validate-native-manifest! supplied "order.gravity" data)))
    (is (= [:graph :replay] @calls))
    (reset! calls [])
    (is (= :complete (:status (workflow/native-coverage-report supplied data))))
    (is (= [:replay :graph] @calls))
    (with-redefs [workflow/replay-fixture-structurally-valid? workflow/replay-fixture-structurally-valid?
                  workflow/workflow-graph-structurally-valid?
                  (fn [_] (alter-var-root #'workflow/replay-fixture-structurally-valid? (constantly (fn [_] false))) true)]
      (is (= [:replay-fixture-structure] (:missing-fields (refusal data)))))))

(deftest failure-operation-is-selected-before-final-subject-evaluation
  (let [events (atom []) armed (atom false) error (ex-info "Original caller" {:identity :original})
        comparator (fn [left right]
                     (when (and @armed (= :b10-diagnostic-stream left))
                       (reset! armed false) (swap! events conj :argument)
                       (alter-var-root #'publish-refusal (constantly (fn [& _] (throw (Error. "Late caller"))))))
                     (compare left right))
        data (into (sorted-map-by comparator) (unverified-data))]
    (with-redefs [diagnostic-values (lazy-seq (reset! armed true) nil)
                  publish-refusal (fn [& _] (swap! events conj :original) (throw error))]
      (is (identical? error (try (workflow/validate-native-manifest! (ports) "selection.gravity" data)
                                 nil (catch Throwable actual actual)))))
    (is (= [:argument :original] @events))))

(deftest forged-data-and-empty-compensation-do-not-establish-workflow-semantics
  (let [data (-> (unverified-data)
                 (assoc-in [:workflow-backend-manifest :workflow-graph-artifact :content] "not a workflow")
                 (assoc-in [:workflow-backend-manifest :replay-fixtures] [])
                 (assoc-in [:workflow-backend-manifest :retry-timeout-cancellation-compensation-table :steps] {})
                 (assoc-in [:workflow-backend-manifest :step-schema-bundle :schemas] [])
                 (assoc-in [:workflow-backend-manifest :external-capability-manifest :capabilities] #{}))
        report (workflow/native-coverage-report (ports) {})]
    (is (= :complete (workflow/validate-native-manifest! (ports) "forged.gravity" data)))
    (is (= :complete (:status report)))
    (is (true? (:compensation-covered? report)))
    (is (false? (:manifest-complete? report)))
    (is (false? (:backend-interface-input-verified? report)))
    (is (= :not-available-in-current-environment
           (:external-runtime-validation? (workflow/native-coverage-report (ports) data))))))

(deftest new-apis-have-explicit-non-admitting-contracts
  (is (= 13 (count (ns-publics 'gravity.backend.workflow))))
  (doseq [operation [#'workflow/native-manifest #'workflow/validate-native-manifest! #'workflow/native-coverage-report]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= 'gravity.backend.workflow (:namespace contract)))
      (is (= (:arglists (meta operation)) (:arglists entry)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-inputs? entry)))
      (is (false? (:grants-authority? entry)))
      (is (false? (:issues-certificates? entry)))
      (is (seq (:required-operations entry)))
      (is (= '[gravity.bootstrap gravity.cli gravity.release]
             (get-in contract [:dependency-direction :must-not-require])))
      (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :consumed-by]))))))
