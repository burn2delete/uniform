(ns gravity.macro.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.macro :as macro]))

(defn- operations [] ((ns-resolve 'gravity.bootstrap 'macro-stage-operations)))
(defn- caught [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error error)))
(defn- fixture-path [extension]
  (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/self-hosting/sh-05/accepted/defn-basic" extension))))
(defn- context [extension]
  (let [source (fixture-path extension)
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
     :binding (bootstrap/sh05-macro-current-binding! source)}))
(def ^:private gravity-context (delay (context ".gravity")))
(def ^:private qst-context (delay (context ".qst")))

(deftest macro-stage-contract-and-seed-operation-inventories-agree
  (let [contract (:gravity/namespace-contract (meta #'macro/expand))
        ops (operations)]
    (is (= 'gravity.macro (:namespace contract)))
    (is (= 14 (count (:public-api contract))))
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
                  artifact))
              [gravity-context qst-context])]
    (is (= (mapv :artifact-id artifacts)
           (repeat 2 "sha256:f47b8ed8b4f008970ba41a8e06e37f6cfc6f2899179ba8676f3c53dd7836788a")))
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
