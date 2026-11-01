(ns gravity.self-hosting.sh07-core-construction-verification-work-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]))

;; C6, D6 and D9: orchestration reuse must preserve actual owner execution,
;; fresh public admission, request membership and dependency invalidation.
(defn- implementation [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Missing construction implementation" {:name name}))))

(defn- thrown [f]
  (try (f) nil (catch Throwable error error)))

(def ^:private invocation
  {:reader-source-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
   :authenticated-envelope-options {:source :development-current}})

(def ^:private resolution
  (delay
    (let [path "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-reader-quote.gravity"]
      (b/compiler-c5-resolution-source-artifact path (slurp path :encoding "UTF-8")
        {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))))

(defn- observe [names f]
  (let [calls (atom {}) arities (atom {})
        originals (into {} (map #(let [v (implementation %)] [v (var-get v)])) names)
        result (with-redefs-fn
                 (into {} (map (fn [[v original]]
                                 [v (fn [& args]
                                      (swap! calls update (:name (meta v)) (fnil inc 0))
                                      (swap! arities update [(:name (meta v)) (count args)] (fnil inc 0))
                                      (apply original args))])) originals)
                 f)]
    {:result result :calls @calls :arities @arities
     :restored? (every? (fn [[v original]] (identical? original (var-get v))) originals)}))

(deftest genuine-construction-retains-products-with-one-fresh-upstream-report
  (let [input @resolution
        names '[sh06-resolution-artifact-verification sh07-core-request-from-verified-upstream
                sh06-resolution-artifact-verification-contained
                sh06-resolution-artifact-verification-bounded* sh06-resolution-run-request!]
        first-run (observe names #(b/sh07-core-from-resolution-artifact input invocation))
        artifact (:result first-run)
        request (get-in artifact [:gravity-core-boundary :authenticated-core-request])
        second-run (observe names #(b/sh07-core-from-authenticated-request input request invocation))]
    (is (= :accepted (:status input) (:status artifact)))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= input (:sh06-resolution-artifact artifact)))
    (is (= (:artifact-id input) (b/sh06-resolution-artifact-id input)))
    (is (= (:artifact-id artifact)
           (b/reader-canonical-hash
             {:domain :gravity/sh07-declared-digest-v1 :purpose :sh07-core-artifact-id
              :preimage (b/sh07-core-artifact-identity-input artifact)})))
    (is (= artifact (:result second-run)))
    (doseq [run [first-run second-run]]
      (is (= 1 (get-in run [:arities ['sh06-resolution-artifact-verification 3]])))
      (is (= 1 (get-in run [:calls 'sh07-core-request-from-verified-upstream])))
      (is (= 1 (get-in run [:arities ['sh06-resolution-artifact-verification-contained 3]])))
      (is (= 1 (get-in run [:arities ['sh06-resolution-artifact-verification-bounded* 3]])))
      (is (= 1 (get-in run [:calls 'sh06-resolution-run-request!])))
      (is (:restored? run)))
    (is (false? (get-in artifact [:gravity-core-boundary :target-source-reread?])))
    (is (= :passed (get-in artifact [:gravity-core-boundary :template-verification :status])
                   (get-in artifact [:gravity-core-boundary :resolved-verification :status])))))

(deftest standalone-public-boundaries-and-independent-audit-remain-fresh
  (let [input @resolution names '[sh06-resolution-artifact-verification]
        requests (observe names #(vector (b/sh07-core-authenticated-request input :gravity.bootstrap/legacy invocation)
                                        (b/sh07-core-authenticated-request input :gravity.bootstrap/legacy invocation)))
        request (first (:result requests))
        _ (is (= 2 (get-in requests [:arities ['sh06-resolution-artifact-verification 3]])))
        _ (is (apply = (:result requests)))
        run (observe names #(b/sh07-core-run-request-for-test input request invocation))
        artifact (b/sh07-core-from-resolution-artifact input invocation)
        audit (observe names #(b/sh07-core-artifact-verification artifact invocation))
        altered (assoc-in artifact [:gravity-core-boundary :canonical-core-artifact
                                     :fragment-coverage :form-count] 999999)]
    (is (= :passed (get-in run [:result :template-verification :status])))
    (is (pos? (get-in run [:calls 'sh06-resolution-artifact-verification])))
    (is (= :passed (get-in audit [:result :status])))
    ;; Independent audit performs its own work; its total is not a producer count.
    (is (pos? (get-in audit [:calls 'sh06-resolution-artifact-verification])))
    (is (= :failed (:status (b/sh07-core-artifact-verification altered invocation))))
    (is (every? :restored? [requests run audit]))))

(deftest coherent-identifier-tampering-and-request-mismatch-remain-rejected
  (let [input @resolution
        parent (update (:sh05-macro-artifact input) :expanded-defn-count inc)
        parent (assoc parent :artifact-id (b/sh05-macro-artifact-id parent))
        changed (assoc input :sh05-macro-artifact parent)
        changed (assoc changed :artifact-id (b/sh06-resolution-artifact-id changed))
        report (b/sh06-resolution-artifact-verification changed :gravity.bootstrap/legacy invocation)
        error (thrown #(b/sh07-core-from-resolution-artifact changed invocation))
        request (b/sh07-core-authenticated-request input :gravity.bootstrap/legacy invocation)
        mismatch (thrown #(b/sh07-core-from-authenticated-request input
                           (assoc request :projection-binding "changed") invocation))
        nil-context (thrown #(b/sh07-core-authenticated-request input nil))]
    (is (= (:artifact-id parent) (b/sh05-macro-artifact-id parent)))
    (is (= (:artifact-id changed) (b/sh06-resolution-artifact-id changed)))
    (is (not= (:artifact-id input) (:artifact-id changed)))
    (is (= :failed (:status report)))
    (is (= "C6-VERIFY" (:id (ex-data error))))
    (is (= [:fresh-authenticated-sh06-resolution] (:missing-fields (ex-data error))))
    (is (= :authenticated-sh06-request-membership-mismatch (get-in (ex-data mismatch) [:facts :reason])))
    (is (= [:sh06-context] (:missing-fields (ex-data nil-context))))))

(deftest actual-owner-reports-remain-separated-by-transaction-phase
  ;; Exercise real owners in the existing receipt kernel. This is not the
  ;; historically pinned file-transaction admission or its snapshot evidence.
  (let [input @resolution
        context-var (implementation '*sh07-proof-transaction-context*)
        context (atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
                       :phase :construction :epoch 0
                       :receipts [] :check-catalogs {} :executions {} :reuses {}
                       :failed-report-executions 0})
        verifier (implementation 'sh06-resolution-artifact-verification-contained)
        original (var-get verifier) events (atom [])]
    (try
      (with-bindings {context-var context}
        (with-redefs-fn
          {verifier (fn [& args]
                      (when (= 3 (count args)) (swap! events conj (:phase @context)))
                      (apply original args))}
          (fn []
            (let [artifact (b/sh07-core-from-resolution-artifact input invocation)
                  construction @context]
              (is (= :complete (get-in artifact [:capability-based-proof :status])))
              (is (= 1 (get-in construction [:executions :sh06])))
              (is (seq (:receipts construction)))
              ;; Match the receipt boundary: discard construction facts before
              ;; asking the independent generic verifier to execute.
              (swap! context assoc :phase :independent-audit :epoch 1
                     :receipts [] :executions {} :reuses {} :check-catalogs {})
              (is (empty? (:receipts @context)))
              (let [report (b/sh07-core-artifact-verification artifact invocation)]
                (is (= :passed (:status report)))
                (is (= 1 (get-in @context [:executions :sh06])))
                (is (= 1 (get-in @context [:executions :sh07])))
                (is (= [:construction :independent-audit] @events))
                (is (every? #(= :independent-audit (:phase %)) (:receipts @context))))))))
      (finally (swap! context assoc :open? false :receipts [])))
    (is (false? (:open? @context)))
    (is (empty? (:receipts @context)))))

(defn- actual-upstream-change [kind]
  (let [input @resolution verified? (atom false) reports (atom []) core-calls (atom 0)
        verifier (implementation 'sh06-resolution-artifact-verification)
        current (implementation 'sh06-resolution-current-binding!)
        execute (implementation 'sh07-core-execute!)
        reader (implementation 'sh03-reader-verifier)
        context (when (= :catalog kind)
                  (atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
                         :phase :construction :epoch 0
                         :receipts [] :check-catalogs {} :executions {} :reuses {}}))
        original-verifier (var-get verifier) original-current (var-get current)
        original-execute (var-get execute)
        error (with-bindings {(implementation '*sh07-proof-transaction-context*) context}
                (with-redefs-fn
                {reader (var-get reader)
                 verifier (fn [& args]
                            (let [report (apply original-verifier args)]
                              (when (= 3 (count args))
                               (swap! reports conj (:status report))
                               (when (= :catalog kind)
                                (swap! context assoc-in [:check-catalogs [:sh06 :final]]
                                       {:check-catalog #{} :check-catalog-hash "changed"}))
                              (when (= :reader kind)
                                (alter-var-root reader (constantly (var-get (implementation 'sh03-reader-entrypoint)))))
                               (reset! verified? true)) report))
                 current (fn [& args]
                           (let [binding (apply original-current args)]
                             (if (and (= :binding kind) @verified?)
                               (assoc-in binding [:plan :functions 'test-replaced] {:arity 0})
                               binding)))
                 execute (fn [& args] (swap! core-calls inc) (apply original-execute args))}
                #(thrown (fn [] (b/sh07-core-from-resolution-artifact input invocation)))))]
    {:reports @reports :error (ex-data error) :core-calls @core-calls}))

(deftest actual-upstream-dependency-replacements-after-verification-are-rejected
  (doseq [[kind diagnostic] [[:binding ["C6-VERIFY" :construction-dependency-drift]]
                            [:catalog ["C6-VERIFY" :construction-check-catalog-drift]]
                            [:reader ["C2-HASH" nil]]]]
    (let [{:keys [reports error core-calls]} (actual-upstream-change kind)]
      (is (= [:passed] reports) (name kind))
      (is (= diagnostic ((juxt :id :reason) error)) (name kind))
      (is (zero? core-calls) (name kind)))))

(defn- with-guard-model [f]
  ;; These are guard-unit models, not fabricated owner acceptance evidence.
  (let [state (atom {:dependencies :stable :bindings :stable :phase nil})
        overrides {'sh07-core-construction-carrier! (constantly :trusted)
                   'sh07-core-source-path-from-resolution (constantly "guard.gravity")
                   'sh07-core-construction-dependencies (fn [& _] (:dependencies @state))
                   'sh07-core-construction-bindings (fn [_] (:bindings @state))
                   'sh07-core-construction-phase (fn [] (:phase @state))
                   'sh07-core-construction-reader (constantly :selected-reader)
                   'sh07-core-construction-envelope! (fn [& _] :passed)
                   'sh06-resolution-artifact-verification
                   (constantly {:status :passed :checks {:owner-executed? true}})
                   'sh07-core-request-from-verified-upstream (fn [_ _] {:request :expected})}]
    (with-redefs-fn (into {} (map (fn [[name value]] [(implementation name) value])) overrides)
      #(f state))))

(deftest whole-construction-guards-reject-drift-at-every-use
  (with-guard-model
    (fn [state]
      (doseq [field [:dependencies :bindings :phase]
              boundary [:run :exit]]
        (reset! state {:dependencies :stable :bindings :stable :phase nil})
        (let [error
              (thrown #((implementation 'sh07-core-with-fresh-construction) {}
                        (fn [facts]
                          (swap! state assoc field :changed)
                          (when (= :run boundary)
                            ((implementation 'sh07-core-run-with-construction)
                             (:artifact facts) (:request facts) facts))
                          :would-have-returned)))]
          (is (= ["C6-VERIFY" :construction-dependency-drift]
                 ((juxt :id :reason) (ex-data error))))))
      (reset! state {:dependencies :stable :bindings :stable :phase nil})
      (let [v (implementation 'sh07-core-execute!)
            error (thrown #((implementation 'sh07-core-with-fresh-construction) {}
                            (fn [facts]
                              (with-redefs-fn {v (fn [& _] :changed)}
                                (fn [] ((implementation 'sh07-core-construction-check!) facts))))))]
        (is (= :construction-dependency-drift (:reason (ex-data error)))))
      (let [error (thrown
                    (fn []
                      ((implementation 'sh07-core-with-fresh-construction) {}
                       (fn [facts]
                         (let [result (promise)
                               check (fn [] ((implementation 'sh07-core-construction-check!) facts))
                               thread (Thread. (fn [] (deliver result (thrown check))))]
                           (.start thread) (.join thread) (throw @result))))))]
        (is (= :construction-dependency-drift (:reason (ex-data error))))))))

(deftest carrier-guard-dominates-new-dependency-walks
  (let [walked? (atom false)
        dependencies (implementation 'sh07-core-construction-dependencies)
        error (with-redefs-fn {dependencies (fn [& _] (reset! walked? true))}
                #(thrown (fn [] ((implementation 'sh07-core-with-fresh-construction)
                                 (java.util.HashMap.) identity))))]
    (is (= "C6-VERIFY" (:id (ex-data error))))
    (is (false? @walked?))))

(deftest fresh-verifier-failures-and-interrupts-propagate-unchanged
  (with-guard-model
    (fn [_]
      (doseq [error [(ex-info "owner diagnostic" {:id "OWNER-FAILURE"})
                     (InterruptedException. "owner interrupted")]]
        (let [v (implementation 'sh06-resolution-artifact-verification)
              [caught interrupted?]
              (with-redefs-fn {v (fn [& _] (throw error))}
                (fn []
                  (let [caught (thrown (fn [] ((implementation 'sh07-core-with-fresh-construction) {} identity)))
                        interrupted? (.isInterrupted (Thread/currentThread))]
                    (Thread/interrupted)
                    [caught interrupted?])))]
          (is (identical? error caught))
          (when (instance? InterruptedException error)
            (is interrupted?)))))))

(deftest operation-roots-distinguish-local-execution-bindings
  ;; Native Var observations only; no owner/admission evidence is constructed.
  (let [roots (implementation 'sh07-core-construction-roots)
        execution (implementation '*sh07-core-execution-binding*)
        before (get (into {} (roots)) execution)]
    (with-bindings {execution ::captured-plan}
      (is (identical? before (get (into {} (roots)) execution)))
      (with-redefs-fn {execution ::replaced-root}
        #(is (= ::replaced-root (get (into {} (roots)) execution)))))
    (is (identical? before (get (into {} (roots)) execution)))))

(deftest construction-root-guard-preserves-local-binding-and-root-refusals
  ;; Existing explicit guard-unit models, never authenticated owner acceptance.
  (let [execution (implementation '*sh07-core-execution-binding*)
        check (implementation 'sh07-core-construction-check!)
        construct (implementation 'sh07-core-with-fresh-construction)]
    (with-guard-model
      (fn [_]
        (is (= ::constructed
               (construct {} (fn [facts]
                               (with-bindings {execution ::captured-plan}
                                 (check facts))
                               ::constructed))))
        (with-bindings {execution ::captured-plan}
          (let [error
                (thrown #(construct {}
                            (fn [facts]
                              (with-redefs-fn {execution ::replaced-root}
                                (fn [] (check facts))))))]
            (is (= ["C6-VERIFY" :construction-dependency-drift]
                   ((juxt :id :reason) (ex-data error))))))))))
