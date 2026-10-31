(ns gravity.macro.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.macro-run-storage :as macro-run-storage]
            [gravity.reader.identity-expectations :as expected]
            [gravity.macro :as macro]))

(defn- operations [] ((ns-resolve 'gravity.bootstrap 'macro-stage-operations)))
(defn- caught [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error error)))
(defn- fixture-path
  ([extension] (fixture-path extension "defn-basic"))
  ([extension basename]
   (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/self-hosting/sh-05/accepted/" basename extension)))))
(defn- context
  ([extension] (context extension "defn-basic"))
  ([extension basename]
  (let [source (fixture-path extension basename)
        reader (.getCanonicalPath (io/file bootstrap/sh03-reader-source-relative-path))
        invocation {:reader-source-path reader :authenticated-envelope-options {:source :development-current}}
        c3 (bootstrap/compiler-c3-syntax-source-artifact
             source (slurp source) (bootstrap/reader-project-context-for-source source)
             nil nil reader invocation)
        forms (get-in c3 [:c2-reader-artifact :parsed-semantic-values])
        module (bootstrap/parse-module source forms)
        syntax (second (bootstrap/sh05-source-syntax-stream c3))
        request (bootstrap/sh05-default-macro-request source c3 module syntax (second forms))]
    {:source source :invocation invocation :c3 c3 :request request
     :binding (bootstrap/sh05-macro-current-binding! source)})))
(def ^:private gravity-context (delay (context ".gravity")))
(def ^:private qst-context (delay (context ".qst")))

(defn- assert-parent-lineage [artifact c3 binding]
  ;; The explicit current Reader implementation is an input, so a whole-artifact
  ;; digest is not a golden for fixture text alone. Keep the actual parent joins
  ;; and prove that neither upstream identity can disappear from the digest.
  (is (= (expected/macro-preimage artifact)
         (macro/artifact-identity-input artifact (operations))))
  (is (= (expected/macro-id artifact) (:artifact-id artifact)))
  (is (= {:kind :gravity/sh05-macro-expansion-artifact :slice :SH-05 :task "SH-05"
          :status :accepted :document-set ["L4" "C4"] :diagnostics []}
         (select-keys artifact [:kind :slice :task :status :document-set :diagnostics])))
  (is (= (select-keys binding expected/macro-plan-fields)
         (:plan-binding (expected/macro-preimage artifact))))
  (is (= ((:input (operations)) :c4-macro-governing-document) (:governing-document artifact)))
  (is (= {:artifact :gravity/sh05-macro-environment
          :macros [{:name 'defn :version ((:input (operations)) :sh05-macro-version) :phase :macro-expansion}]
          :status :complete}
         (:macro-environment artifact)))
  (is (= (:artifact-id c3)
         (get-in artifact [:sh04-syntax-artifact :artifact-id])))
  (is (= (get-in c3 [:gravity-syntax-boundary :resolved-syntax-result :artifact-id])
         (get-in artifact [:sh04-syntax-artifact :syntax-stream-id])))
  (doseq [field [:artifact-id :syntax-stream-id]]
    (let [changed (assoc-in artifact [:sh04-syntax-artifact field] "changed-parent")]
      (is (not= (:artifact-id artifact) (macro/artifact-id changed (operations)))
          (str "upstream " field " remains identity-bearing")))))

(deftest macro-stage-contract-and-seed-operation-inventories-agree
  (let [contract (:gravity/namespace-contract (meta #'macro/expand))
        ops (operations)]
    (is (= 'gravity.macro (:namespace contract)))
    (is (= 50 (count (:public-api contract))))
    (is (= (set (keys (:public-api contract))) (set (keys (ns-publics 'gravity.macro)))))
    (is (= (set (:operation-inputs contract)) (set (keys ops))))
    (doseq [[name api] (:public-api contract)]
      (is (= (:arglists api) (:arglists (meta (ns-resolve 'gravity.macro name))))))
    (is (= 46 ((:input ops) :sh05-macro-expected-function-count)))))

(deftest real-paired-macro-expansion-retains-identities-provenance-and-output
  (let [artifacts
        (mapv (fn [ctx]
                (let [{:keys [source c3 request binding invocation]} @ctx
                      run (macro/expand source binding request (operations))
                      artifact (bootstrap/sh05-macro-artifact-from-c3-artifact source c3 invocation)]
                  (is (= run (bootstrap/sh05-run-macro-request! source binding request)))
                  (is (= :passed (get-in run [:template-verification :status])))
                  (is (= :passed (get-in run [:resolved-verification :status])))
                  (is (= '[(def add-one (fn [x] (+ x 1)))] (:expanded-forms artifact)))
                  (is (= source (get-in artifact [:provenance :source-path])))
                  (is (seq (get-in artifact [:macro-expansion-trace 0 :generated-origin])))
                  (is (= :passed (:status (bootstrap/sh05-macro-artifact-verification artifact invocation))))
                  (assert-parent-lineage artifact c3 binding)
                  artifact))
              [gravity-context qst-context])]
    (is (= (select-keys (first artifacts) [:artifact-id :expanded-syntax-stream-id :macro-expansion-trace-id :expanded-forms])
           (select-keys (second artifacts) [:artifact-id :expanded-syntax-stream-id :macro-expansion-trace-id :expanded-forms])))))

(deftest real-macro-requests-reject-illegal-return-and-build-authority
  (let [{:keys [source binding request]} @gravity-context]
    (doseq [[rule changed] [["C4-RETURN" (assoc-in request [:policy :macro-return-kind] :value)]
                           ["C4-BUILD-EFFECT" (assoc-in request [:policy :requested-build-effects] [:build/read-file])]]]
      (let [data (ex-data (caught #(macro/expand source binding changed (operations))))]
        (is (= rule (:id data)))
        (is (= source (get-in data [:source-span :source])))))))

(deftest plan-pins-and-identity-cache-do-not-admit-substituted-plans
  (let [{:keys [source binding]} @gravity-context
        ops (operations)
        input (:input ops)
        changed-binding (assoc binding :function-count -1)]
    (is (= "C4-TRACE" (:id (ex-data (caught #(macro/current-plan-binding!
                                               source (assoc ops :input
                                                             (fn [key]
                                                               (if (= key :sh05-macro-cached-binding)
                                                                 (delay changed-binding) (input key))))))))))
    (is (= "C4-TRACE" (:id (ex-data (caught #(macro/read-pinned-plan-source!
                                               source (assoc ops :input
                                                             (fn [key]
                                                               (if (= key :sh05-macro-expected-source-byte-count)
                                                                 (inc (input key)) (input key))))))))))
    (is (= (macro/compute-plan-identities (:plan binding) ops)
           (macro/plan-identities (:plan binding) ops)))))

(deftest digest-resolution-only-replaces-declared-control-slots
  (let [{:keys [source request binding]} @gravity-context
        ops (operations)
        run (macro/expand source binding request ops)
        template (assoc (get-in run [:raw-template-result :expansion-template])
                        :ordinary-semantic-data {:digest-ref 0})
        resolved (macro/resolve-expansion-template! source template (:resolved-digests run) ops)]
    (is (= {:digest-ref 0} (:ordinary-semantic-data resolved)))
    (is (= (first (:resolved-digests run)) (:output-syntax-id resolved)))
    (is (= "C4-TRACE" (:id (ex-data (caught #(macro/resolve-expansion-template!
                                               source (assoc template :output-syntax-id "substitute")
                                               (:resolved-digests run) ops))))))
    (is (= "C4-TRACE" (:id (ex-data (caught #(macro/resolve-digest-preimage!
                                               source {:ordinal 1 :purpose :sh05-expanded-syntax-id :preimage :wrong}
                                               [] ops))))))))

(deftest execution-interposition-and-host-containment-remain-live
  (let [ops (operations)
        failure (ex-info "injected" {:id "TEST-INJECTED"})
        changed (assoc ops :p15-s23-stage2-runtime-execute-function (fn [& _] (throw failure)))
        data (ex-data (caught #(macro/execute-plan! "source.gravity" {:plan {}} 'entry [] changed)))]
    (is (= "C4-RETURN" (:id data)))
    (is (= "TEST-INJECTED" (get-in data [:facts :contained-diagnostic])))
    (is (= {:retained :semantic-value}
           (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                         (fn [& _] (with-meta {:retained :semantic-value} {:host-only true}))]
             (bootstrap/sh05-macro-execute! "source.gravity" {:plan {}} 'entry []))))
    (let [interrupt (InterruptedException. "test")
          observed (try
                     (macro/execute-plan! "source.gravity" {:plan {}} 'entry []
                                                  (assoc ops :p15-s23-stage2-runtime-execute-function
                                                         (fn [& _] (throw interrupt))))
                     (catch InterruptedException error [error (.isInterrupted (Thread/currentThread))])
                     (finally (Thread/interrupted)))]
      (is (identical? interrupt (first observed)))
      (is (true? (second observed))))
    (let [data (ex-data (caught #(macro/contain-operation! "source.gravity" :real-host-stack
                                                          (fn [] (throw (StackOverflowError.))) ops)))]
      (is (= "C4-DEPTH" (:id data)))
      (is (= "java.lang.StackOverflowError" (get-in data [:facts :contained-host-error]))))))

(defn- artifact-context [extension]
  (let [{:keys [source c3 invocation] :as ctx} (context extension "defn-multiple")]
    (assoc ctx :artifact (macro/assemble source c3 false invocation (operations)))))
(def ^:private multi-gravity (delay (artifact-context ".gravity")))
(def ^:private multi-qst (delay (artifact-context ".qst")))
(defn- private-var [name] (or (ns-resolve 'gravity.bootstrap name)
                            (throw (ex-info "Required seed seam is absent" {:name name}))))
(defn- with-report-context [operation]
  (let [ctx (atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
                   :phase :construction :epoch 0 :receipts [] :executions {}
                   :reuses {} :check-catalogs {} :failed-report-executions 0})]
    (try
      (with-bindings {(private-var '*sh07-proof-transaction-context*) ctx}
        (operation ctx))
      (finally (swap! ctx assoc :open? false :receipts [])))))

(deftest multi-artifact-construction-and-verification-are-co-canonical
  (doseq [ctx [multi-gravity multi-qst]]
    (let [{:keys [source c3 invocation artifact binding]} @ctx
          ops (operations)
          report (macro/verify artifact false invocation ops)]
      (is (= :passed (:status report)))
      (is (= report (bootstrap/sh05-macro-artifact-verification artifact invocation)))
      (is (= artifact (bootstrap/sh05-macro-artifact-from-c3-artifact source c3 invocation)))
      (is (= 2 (:expanded-defn-count artifact)))
      (is (= '[(def add-one (fn [x] (+ x 1))) (def double-value (fn [x] (+ x x)))]
             (:expanded-forms artifact)))
      (assert-parent-lineage artifact c3 binding)
      (is (= (:artifact-id artifact) (macro/artifact-id artifact ops)))
      (is (= source (get-in artifact [:provenance :source-path])))
      (is (= (mapv :syntax/id (subvec (bootstrap/sh05-source-syntax-stream c3) 1))
             (mapv :input-syntax-id (:macro-expansion-trace artifact)))))))

(deftest invalid-parent-rejects-before-macro-work
  (let [{:keys [source c3 invocation]} @multi-gravity
        calls (atom 0)
        ops (assoc (operations) :sh05-macro-current-binding! (fn [& _] (swap! calls inc)))
        error (caught #(macro/assemble source (assoc c3 :artifact-id "forged") false invocation ops))]
    (is (= "C4-RETURN" (:id (ex-data error))))
    (is (= [:authenticated-c3-syntax-parent] (:missing-fields (ex-data error))))
    (is (zero? @calls))))

(deftest artifact-forgeries-and-physical-provenance-drift-fail-verification
  (let [{:keys [artifact invocation]} @multi-gravity
        runs (get-in artifact [:gravity-macro-boundary :expansion-runs])
        mutations [(assoc artifact :expanded-defn-count 99)
                   (assoc-in artifact [:gravity-macro-boundary :expansion-runs] (vec (reverse runs)))
                   (assoc-in artifact [:gravity-macro-boundary :expansion-runs] (vec (rest runs)))
                   (assoc-in artifact [:expanded-syntax-stream 1 :origin] [])
                   (assoc-in artifact [:macro-environment :macros 0 :version] "forged")
                   (assoc-in artifact [:gravity-macro-boundary :plan-binding :self-hosted?] true)
                   (assoc-in artifact [:capability-based-proof :status] :forged)]
        path-drift (assoc-in artifact [:provenance :source-path] "different-source.gravity")]
    (with-report-context
      (fn [_]
        (doseq [changed (conj mutations path-drift)]
          (let [report (bootstrap/sh05-macro-artifact-verification changed invocation)]
            (is (= :failed (:status report)))
            (is (seq (:failed-checks report)))))))
    (is (= (:artifact-id artifact) (macro/artifact-id path-drift (operations))))
    (is (= {:ordinary {:digest-ref 0} :metadata {:doc "kept"}}
           (macro/path-neutral-value {:ordinary {:digest-ref 0} :metadata {:doc "kept"}
                                      :actual-source-path "/physical"} (operations))))))

(defn- assert-interposition-misses [interpose]
  (let [{:keys [artifact invocation]} @multi-gravity]
    (with-report-context
      (fn [ctx]
        (is (= :passed (:status (bootstrap/sh05-macro-artifact-verification artifact invocation))))
        (is (= :passed (:status (bootstrap/sh05-macro-artifact-verification artifact invocation))))
        (is (= 1 (get-in @ctx [:executions :sh05])))
        (is (= 1 (get-in @ctx [:reuses :sh05])))
        (interpose #(dotimes [_ 2]
                      (is (= :passed (:status (bootstrap/sh05-macro-artifact-verification artifact invocation))))))
        (is (= 3 (get-in @ctx [:executions :sh05])))
        (is (= 1 (get-in @ctx [:reuses :sh05])))
        (is (= :passed (:status (bootstrap/sh05-macro-artifact-verification artifact invocation))))
        (is (= 2 (get-in @ctx [:reuses :sh05])))))))

(deftest receipt-reuse-requires-current-stage-and-seed-operations
  (doseq [v [#'macro/verify #'macro/expanded-syntax-object
             #'bootstrap/sh05-macro-gravity-verifier-report
             #'macro-run-storage/exact?]]
    (let [original @v calls (atom 0)]
      (assert-interposition-misses
       (fn [operation]
         (with-redefs-fn {v (fn [& args] (swap! calls inc) (apply original args))} operation)))
      (is (pos? @calls))))
  (let [defaults (private-var 'macro-stage-defaults)
        original @defaults
        copied (reduce-kv assoc {} original)]
    (is (= original copied))
    (is (not (identical? original copied)))
    (assert-interposition-misses
     #(with-redefs-fn {defaults (assoc copied :verifier-root (fn [] :not-a-guard))} %)))
  (let [cache (private-var 'sh05-macro-pinned-plan-identities)
        replacement (atom @@cache)]
    (assert-interposition-misses #(with-redefs-fn {cache replacement} %)))
  (let [version (private-var 'sh05-macro-version)
        replacement (String. ^String @version)]
    (is (= @version replacement))
    (is (not (identical? @version replacement)))
    (assert-interposition-misses #(with-redefs-fn {version replacement} %))))

(deftest identical-value-thread-binding-disables-receipt-reuse
  (let [v #'macro/verify dynamic? (.isDynamic v) original @v]
    (try
      (.setDynamic v true)
      (assert-interposition-misses #(with-bindings {v original} (%)))
      (finally (.setDynamic v dynamic?)))))

(deftest invocation-rejection-precedes-the-stage-verifier
  (let [{:keys [artifact]} @multi-gravity calls (atom 0)
        options (private-var 'sh03-reader-authenticated-envelope-options)]
    (with-redefs-fn
      {options (fn [& _] (throw (ex-info "invalid invocation" {:id "TEST-INVOCATION"})))
       #'macro/verify (fn [& _] (swap! calls inc))}
      #(is (= "TEST-INVOCATION"
              (:id (ex-data (caught (fn [] (bootstrap/sh05-macro-artifact-verification artifact {:invalid true}))))))))
    (is (zero? @calls))))

(deftest interrupted-macro-verification-unwinds-production-receipt-lifetime
  (let [{:keys [artifact source invocation]} @multi-gravity
        interrupt (InterruptedException. "macro verification")
        cleanup (atom nil)
        observer (private-var '*sh07-proof-transaction-cleanup-observer*)
        ;; Isolate lifetime control flow; no checked-core admission runs.
        prerequisites {(private-var 'sh07-core-assert-pinned-revision!) (fn [& _] {})
                       (private-var 'sh07-proof-transaction-source-snapshot) (fn [_] {})
                       (private-var 'sh07-proof-transaction-core-snapshot) (fn [] {})}
        observed
        (try
          (with-redefs-fn prerequisites
            #(with-bindings {observer (fn [value] (reset! cleanup value))}
               (bootstrap/sh07-core-file-proof-transaction
                source
                (fn [_]
                  (bootstrap/sh05-macro-artifact-verification artifact invocation)
                  (with-redefs [bootstrap/sh05-macro-gravity-verifier-report (fn [& _] (throw interrupt))]
                    (bootstrap/sh05-macro-artifact-verification artifact invocation))))))
          (catch InterruptedException error [error (.isInterrupted (Thread/currentThread))])
          (finally (Thread/interrupted)))]
    (is (identical? interrupt (first observed)))
    (is (true? (second observed)))
    (is (= {:open? false :cleanup-complete? true :retained-receipt-count 0}
           (select-keys @cleanup [:open? :cleanup-complete? :retained-receipt-count])))))

(deftest old-helper-vars-stay-unmapped
  (doseq [suffix ["carrier" "expansion-replay" "provenance" "proof"]]
    (is (nil? (ns-resolve 'gravity.bootstrap
                         (symbol (str "semantic-late-sh05-macro-artifact-verification*-" suffix)))))))
