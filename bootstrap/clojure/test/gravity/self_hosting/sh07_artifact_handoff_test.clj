(ns gravity.self-hosting.sh07-artifact-handoff-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.authenticated-artifact-pipeline :as pipeline]
            [gravity.bootstrap :as bootstrap]))

(def ^:private source-path
  "bootstrap/gravity/src/gravity/bootstrap/diagnostics.gravity")

(defn- private-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "SH-07 transaction seam var is absent"
                      {:id "SH07-PROOF-TRANSACTION-IMPLEMENTATION"
                       :symbol symbol}))))

(defn- fake-transaction-bindings
  [artifact-builder calls]
  (let [context-var (private-var '*sh07-proof-transaction-context*)
        source-var (private-var 'sh07-proof-transaction-source-snapshot)
        core-var (private-var 'sh07-proof-transaction-core-snapshot)
        transition-var (private-var 'sh07-proof-transaction-transition!)
        verification-var (private-var 'sh07-core-artifact-verification)
        capability-var (private-var 'sh07-core-proof-from-verification-report)
        final-check-var (private-var 'sh07-proof-transaction-final-snapshot-check!)
        hash-var (private-var 'reader-canonical-hash)
        close-var (private-var 'sh07-proof-transaction-close!)]
    {source-var (fn [path]
                  (swap! calls update :snapshots inc)
                  {:source-path path
                   :source-byte-count 1
                   :source-content-hash "sha256:source"})
     core-var (fn [] {:source-content-hash "sha256:core"})
     transition-var (fn [_ _ _] nil)
     verification-var (fn [_]
                        {:artifact :gravity/sh07-core-artifact-verification
                         :status :passed
                         :failed-checks []})
     capability-var (fn [_]
                      {:artifact :gravity/sh07-core-capability-proof
                       :status :complete
                       :failed-checks []})
     final-check-var (fn [_ _ _] nil)
     hash-var (fn [_] "sha256:verification")
     close-var (fn [context status]
                 {:artifact :gravity/sh07-proof-transaction-receipt
                  :status status
                  :artifact-id (:artifact-id @context)
                  :verification-report-id "sha256:verification"})
     (private-var '*sh07-proof-transaction-cleanup-observer*) nil
     (private-var 'sh07-core-file-artifact) artifact-builder}))

(defn- run-fake-transaction
  [arity artifact-builder]
  (let [calls (atom {:builder 0 :snapshots 0 :context nil})
        builder (fn [path]
                  (swap! calls update :builder inc)
                  (swap! calls assoc :context
                         (let [context-atom (deref (private-var
                                                    '*sh07-proof-transaction-context*))]
                           @context-atom))
                  (artifact-builder path))
        bindings (fake-transaction-bindings builder calls)
        result (with-redefs-fn
                 bindings
                 #(if (= 1 arity)
                    (bootstrap/sh07-core-file-proof-transaction source-path)
                    (bootstrap/sh07-core-file-proof-transaction
                     source-path builder)))]
    {:calls @calls :result result}))

(deftest artifact-builder-runs-once-inside-construction-context
  (let [{:keys [calls result]}
        (run-fake-transaction 2
                              (fn [path]
                                {:artifact-id "sha256:builder"
                                 :source-path path}))]
    (is (= 1 (:builder calls)))
    (is (= 1 (:snapshots calls)))
    (is (= :construction (get-in calls [:context :phase])))
    (is (true? (get-in calls [:context :open?])))
    (is (= "sha256:builder" (get-in result [:artifact :artifact-id])))
    (is (= :passed (get-in result [:proof-transaction :status])))))

(deftest one-arity-retains-the-legacy-file-builder
  (let [{:keys [calls result]}
        (run-fake-transaction 1
                              (fn [_] {:artifact-id "sha256:legacy"}))]
    (is (= 1 (:builder calls)))
    (is (= "sha256:legacy" (get-in result [:artifact :artifact-id])))))

(deftest non-callable-builder-fails-before-opening-a-proof-epoch
  (let [calls (atom 0)
        source-var (private-var 'sh07-proof-transaction-source-snapshot)
        data (try
               (with-redefs-fn
                 {source-var (fn [_] (swap! calls inc))}
                 #(bootstrap/sh07-core-file-proof-transaction source-path nil))
               nil
               (catch clojure.lang.ExceptionInfo error
                 (ex-data error)))]
    (is (= "C6-VERIFY" (:id data)))
    (is (= :artifact-builder-required (:reason data)))
    (is (zero? @calls))))

(def ^:private fake-authority
  {:authoritative? false
   :ceiling :development-only
   :nonclaims [:integration :proof :release :self-hosting :seed-retirement
               :safety :performance]})

(def ^:private fake-boundary
  {:fresh-cache-bypassed? true
   :isolation :same-jvm
   :isolated? false
   :acceptance-authority? false})

(defn- fake-pipeline-result
  []
  (let [actual-path "/tmp/fresh-c6.gravity"
        source-hash "sha256:source"
        payload {:artifact :gravity/checked-core :artifact-id "sha256:core"}
        stages
        (mapv (fn [stage]
                {:stage stage
                 :mode :fresh
                 :authority fake-authority
                 :cache-policy {:eligible? false
                                :observed-hit? false
                                :authoritative? false}
                 :provenance {:actual-path actual-path}
                 :source-identity {:bytes-hash source-hash}
                 :payload-artifact-id (when (= :checked-core stage)
                                        (:artifact-id payload))
                 :payload (when (= :checked-core stage) payload)
                 :receipt {:cache {:eligible? false
                                   :hit? false
                                   :authoritative? false}}})
              pipeline/stages)
        stage-map (into (array-map) (map (juxt :stage identity) stages))
        selected (get stage-map :checked-core)]
    {:kind :gravity/authenticated-artifact-pipeline-result
     :schema-version pipeline/pipeline-schema-version
     :pipeline pipeline/pipeline-id
     :requested-stage :checked-core
     :mode :fresh
     :actual-path actual-path
     :source-content-hash source-hash
     :stage-artifacts stage-map
     :artifact selected
     :payload payload
     :authority fake-authority
     :execution-boundary fake-boundary}))

(deftest fresh-projection-requests-closed-cache-free-checked-core
  (let [requests (atom [])
        result (fake-pipeline-result)
        run-var (ns-resolve 'gravity.authenticated-artifact-pipeline 'run-pipeline)
        snapshot-var (ns-resolve 'gravity.authenticated-artifact-pipeline
                                 'source-snapshot!)]
    (with-redefs-fn {run-var (fn [request]
                               (swap! requests conj request)
                               result)
                     snapshot-var (fn [_]
                                    {:canonical-path "/tmp/fresh-c6.gravity"
                                     :bytes-hash "sha256:source"})}
      #(is (= {:artifact :gravity/checked-core :artifact-id "sha256:core"}
              (pipeline/fresh-checked-core-artifact source-path))))
    (is (= [{:source-path source-path
             :target-stage :checked-core
             :mode :fresh}]
           @requests))))

(deftest fresh-projection-rejects-result-tampering
  (let [cases [{:label :wrong-mode
                :edit #(assoc % :mode :fast)}
               {:label :cache-reuse
                :edit #(-> %
                            (assoc-in [:stage-artifacts :checked-core :cache-policy]
                                      {:eligible? true :observed-hit? true
                                       :authoritative? true})
                            (assoc-in [:stage-artifacts :checked-core :receipt :cache]
                                      {:eligible? true :hit? true
                                       :authoritative? true}))}
               {:label :path-rebinding
                :edit #(let [path "/tmp/rebound.gravity"
                             selected (assoc-in
                                       (get-in % [:stage-artifacts :checked-core])
                                       [:provenance :actual-path] path)]
                         (assoc % :actual-path path
                                  :stage-artifacts
                                  (assoc-in (:stage-artifacts %) [:checked-core]
                                            selected)
                                  :artifact selected))}
               {:label :source-hash
                :edit #(let [hash "sha256:tampered"
                             selected (assoc-in
                                       (get-in % [:stage-artifacts :checked-core])
                                       [:source-identity :bytes-hash] hash)]
                         (assoc % :source-content-hash hash
                                  :stage-artifacts
                                  (assoc-in (:stage-artifacts %) [:checked-core]
                                            selected)
                                  :artifact selected))}
               {:label :payload-id
                :edit #(let [selected (assoc (get-in % [:stage-artifacts :checked-core])
                                             :payload-artifact-id "sha256:tampered")]
                         (assoc % :stage-artifacts
                                  (assoc-in (:stage-artifacts %) [:checked-core]
                                            selected)
                                  :artifact selected))}
               {:label :authority
                :edit #(assoc-in % [:authority :authoritative?] true)}]]
    (doseq [{:keys [label edit]} cases]
      (testing (name label)
        (with-redefs-fn
          {(ns-resolve 'gravity.authenticated-artifact-pipeline 'run-pipeline)
           (fn [_] (edit (fake-pipeline-result)))
           (ns-resolve 'gravity.authenticated-artifact-pipeline
                       'source-snapshot!)
           (fn [_]
             {:canonical-path "/tmp/fresh-c6.gravity"
              :bytes-hash "sha256:source"})}
          #(let [data (try
                        (pipeline/fresh-checked-core-artifact source-path)
                        nil
                        (catch clojure.lang.ExceptionInfo error
                          (ex-data error)))]
             (is (= "ARTIFACT-RESULT" (:id data)))))))))
