(ns gravity.resolution.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.resolution :as resolution]))

(defn- seed-var [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Required resolution seed seam is absent" {:name name}))))
(defn- operations [] ((seed-var 'resolution-stage-operations)))
(defn- input [field] ((:input (operations)) field))
(defn- caught [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error error)))
(def ^:private source
  (.getCanonicalPath (io/file "bootstrap/clojure/fixtures/self-hosting/sh-06/current-cohort-unmetered.gravity")))
(def ^:private invocation
  {:reader-source-path b/sh03-reader-source-relative-path
   :authenticated-envelope-options {:source :development-current}})
(def ^:private real-artifact
  (delay
    (b/compiler-c5-resolution-source-artifact
     source (slurp source :encoding "UTF-8")
     {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
     (.getCanonicalPath (io/file b/sh03-reader-source-relative-path)) invocation)))
(defn- real-binding [] (b/sh06-resolution-current-binding! source))

(deftest contract-and-operation-inventories-agree
  (let [publics (ns-publics 'gravity.resolution)
        contract (:gravity/namespace-contract (meta #'resolution/execute!))]
    (is (= 20 (count publics)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (is (= (set (keys (operations))) (set (:operation-inputs contract))))
    (doseq [[name api] (:public-api contract)]
      (is (= (:arglists api) (:arglists (meta (publics name)))))
      (is (= contract (:gravity/namespace-contract (meta (publics name))))))
    (is (true? (.isDynamic ^clojure.lang.Var (seed-var '*sh06-resolution-operation-binding*))))
    (is (not (contains? (ns-aliases 'gravity.resolution) 'b)))
    (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :forbids])))))

(deftest declaration-records-preserve-qualified-identity-and-origin
  ;; These supplied syntax records test normalization, not C4 authentication.
  (let [ops (operations)
        span {:byte-start 7 :byte-end 20 :line-start 2 :column-start 3 :ignored true}
        module {:module 'example.records :exports ['public-value 'other/qualified]
                :profile :meta :target :jvm :effects #{:io/write}
                :capabilities #{:io/stdout} :safety :safe}
        parent {:artifact-id "synthetic-expanded-parent"
                :expanded-syntax-stream
                [{:form '(def public-value 1) :span span :syntax/id "syntax-public"}
                 {:form '(defn private-value [x] x) :span span :syntax/id "syntax-private"}
                 {:form '(def other/qualified (fn [x] x)) :span span :syntax/id "syntax-qualified"}
                 {:form '(defconst answer 42) :span span :syntax/id "syntax-constant"}
                 {:form '(defn 17 [] nil) :span span :syntax/id "syntax-invalid-name"}
                 {:form '(println public-value) :span span :syntax/id "syntax-expression"}]}
        records (resolution/definition-records module parent ops)
        [_public _private qualified constant] records]
    (is (= ['public-value 'private-value 'qualified 'answer] (mapv :name records)))
    (is (= [:public :private :public :private] (mapv :visibility records)))
    (is (= [:var :function :function :compile-time-constant] (mapv :kind records)))
    (is (= 'other (:namespace qualified)))
    (is (= {:name 'other :version "workspace"} (:package qualified)))
    (is (= [:gravity.type/value :gravity.type/function :gravity.type/function :gravity.type/value]
           (mapv :type-ref records)))
    (is (= [0 1 2 3] (mapv #(get-in % [:semantic-span :ordinal]) records)))
    (is (= "syntax-constant" (:definition-syntax-id constant)))
    (doseq [record records]
      (is (= span (:source-span record)))
      (is (= {:ordinal (get-in record [:semantic-span :ordinal])
              :byte-start 7 :byte-end 20 :line-start 2 :column-start 3} (:semantic-span record)))
      (is (= "synthetic-expanded-parent" (:definition-artifact-id record)))
      (is (= [[:meta] [:jvm] [:io/write] [:io/stdout] :safe :namespace]
             ((juxt :profile-set :target-set :effects :capabilities :safety :binding-class) record))))
    (doseq [[form expected] [['(defmacro m [x] x) :macro]
                            ['(defschema S {}) :schema]
                            ['(defprotocol P) :protocol]
                            ['(let [x 1] x) nil]
                            ['plain-symbol nil]]]
      (is (= expected (resolution/definition-kind form))))
    (is (= {:ordinal 9} (resolution/semantic-span nil 9)))
    (is (= {:name 'example.records :version "workspace"}
           (resolution/package-record 'example.records)))))

(deftest upstream-identities-preserve-precedence-and-deterministic-fallbacks
  (let [ops (operations)
        parent {:artifact-id "parent" :expanded-syntax-stream-id "stream"
                :macro-expansion-trace-id "trace"}
        with-c2 (assoc-in parent [:gravity-macro-boundary :authenticated-sh04-artifact
                                  :c2-reader-artifact :source-unit-record :source-id] "c2-source")
        with-revision (assoc-in with-c2 [:gravity-macro-boundary :authenticated-sh04-artifact
                                         :gravity-syntax-boundary :reader-source-revision :revision-id] "revision")
        with-envelope (assoc-in parent [:gravity-macro-boundary :authenticated-envelope
                                         :semantic-envelope-id] "envelope")]
    (is (= "revision" (resolution/source-revision-id with-revision ops)))
    (is (= "c2-source" (resolution/source-revision-id with-c2 ops)))
    (is (= "envelope" (resolution/envelope-id with-envelope ops)))
    (doseq [operation [resolution/source-revision-id resolution/envelope-id]]
      (let [id (operation parent ops)]
        (is (re-matches #"sha256:[0-9a-f]{64}" id))
        (is (= id (operation (into (array-map) (reverse (seq parent))) ops)))
        (is (not= id (operation (assoc parent :macro-expansion-trace-id "changed") ops)))))
    (is (not= (resolution/source-revision-id parent ops) (resolution/envelope-id parent ops)))
    (is (not= (resolution/envelope-id parent ops)
              (resolution/envelope-id (assoc parent :artifact-id "changed") ops)))))

(deftest core-catalog-and-explicit-overrides-retain-their-boundaries
  (let [ops (operations) records (resolution/core-records ops)
        by-name (into {} (map (juxt :name identity)) records)
        candidates [{:namespace 'one :name 'x :identity :first}
                    {:namespace 'two :name 'x :identity :other}
                    {:namespace 'one :name 'x :identity :later}]
        overrides {:candidate-bindings candidates}]
    (is (= (sort (keys by-name)) (map :name records)))
    (is (= (range (count records)) (map #(get-in % [:semantic-span :ordinal]) records)))
    (is (= (count records) (count by-name)))
    (is (= :special-form (:kind (by-name 'let))))
    (is (= :type (:kind (by-name 'I64))))
    (is (= :var (:kind (by-name 'count))))
    (is (= [[:io/write] [:io/stdout]] ((juxt :effects :capabilities) (by-name 'println))))
    (is (= [[] []] ((juxt :effects :capabilities) (by-name 'count))))
    (is (= [:all] (:target-set (by-name 'I64))))
    (is (= (vec (sort (input :known-source-profiles))) (:profile-set (by-name 'let))))
    (is (every? #(= :safe (:safety %)) records))
    (is (= 1 (count (set (map :definition-artifact-id records)))))
    (is (= (count records) (count (set (map :definition-syntax-id records)))))
    (is (= overrides (resolution/request-overrides {:metadata {:compiler {:sh06-request overrides}}})))
    (doseq [value [nil [] "not-a-record"]]
      (is (= {} (resolution/request-overrides {:metadata {:compiler {:sh06-request value}}}))))
    (is (= :first (:identity (resolution/candidate-for overrides 'one 'x))))
    (is (= :other (:identity (resolution/candidate-for overrides 'two 'x))))
    (is (nil? (resolution/candidate-for overrides 'one 'missing)))))

(deftest actual-source-emits-bindings-and-rejects-an-unresolved-reference
  (let [artifact @real-artifact binding (real-binding) ops (operations)
        request (get-in artifact [:gravity-resolution-boundary :authenticated-resolution-request])
        accepted (resolution/execute! source binding 'sh06-build-resolution-template [request] ops)
        last-reference (dec (count (:references request)))
        invalid (-> request
                    (assoc-in [:references last-reference :symbol] 'missing-value)
                    (assoc-in [:references last-reference :name] 'missing-value))
        rejected (resolution/execute! source binding 'sh06-build-resolution-template [invalid] ops)]
    (is (= :accepted (:status artifact)))
    (is (= :accepted (:status accepted)))
    (is (= "sha256:49d37a2f0d79fb8902e3be9928cab495e734997f5e3e15bfee186ae5b9ed8bc1"
           (:artifact-id artifact)))
    (is (some #(and (= 'value (:symbol %)) (= :local (:resolution-kind %)))
              (:resolution-table artifact)))
    (is (some #(and (= 'identity-value (:name %)) (= :public (:visibility %)))
              (get-in artifact [:binding-table :bindings])))
    (is (= source (get-in artifact [:provenance :source-path])))
    (is (= :development-unadmitted (:revision-status binding)))
    (is (= (:source-content-hash binding) (get-in binding [:plan :source :sha256])))
    (is (= :rejected (:status rejected)))
    (is (some #(= "C5-UNRESOLVED" (:rule %)) (:diagnostics rejected)))
    ;; Both spellings also execute in the before/after artifact parity evidence.
    (is (= (slurp source :encoding "UTF-8")
           (slurp (.getCanonicalPath (io/file "bootstrap/clojure/fixtures/self-hosting/sh-06/current-cohort-unmetered.qst"))
                  :encoding "UTF-8")))))

(deftest source-identities-and-pinned-admission-remain-distinct
  (let [ops (operations) current (resolution/read-current-source! source ops)
        original-input (:input ops)
        pinned-ops (assoc ops :input (fn [field]
                                      (case field
                                        :sh06-resolution-expected-source-byte-count (:source-byte-count current)
                                        :sh06-resolution-expected-source-content-hash (:source-content-hash current)
                                        (original-input field))))]
    (is (= current (b/sh06-resolution-read-current-source! source)))
    (is (= current (resolution/read-pinned-source! source pinned-ops)))
    (is (= [:exact-pinned-resolution-source]
           (:missing-fields (ex-data (caught #(b/sh06-resolution-read-pinned-source! source))))))
    (doseq [[field bad] [[:sh06-resolution-expected-source-byte-count (inc (:source-byte-count current))]
                         [:sh06-resolution-expected-source-content-hash "sha256:forged"]]]
      (let [changed (assoc pinned-ops :input (fn [key] (if (= key field) bad ((:input pinned-ops) key))))]
        (is (= "C5-UNRESOLVED" (:id (ex-data (caught #(resolution/read-pinned-source! source changed))))))))))

(deftest plan-substitutions-and-cache-drift-fail-closed
  (let [binding (real-binding) plan (:plan binding) ops (operations)
        identities (resolution/plan-identities plan ops)
        build-ops (assoc ops
                         :c-backend-stage2-plan-emitter-source-rule! (fn [& _] {:emitter :unit-emitter})
                         :sh06-resolution-plan-identities (constantly identities))]
    (is (= identities (b/sh06-resolution-plan-identities plan)))
    (doseq [bad [(assoc plan :kind :forged)
                 (assoc-in plan [:module :module] 'forged.module)
                 (assoc-in plan [:module :profile] :kernel)
                 (assoc-in plan [:module :effects] #{:io/write})
                 (assoc-in plan [:source :sha256] "sha256:forged")]]
      (is (= [:pinned-resolution-plan-and-functions]
             (:missing-fields (ex-data
                               (caught #(resolution/build-plan-binding!
                                         source (assoc build-ops :p15-s23-stage2-compiler-artifact-plan
                                                       (fn [& _] bad)))))))))
    (let [key (:input-key (b/sh06-resolution-current-binding! source))
          cache (atom nil) builds (atom 0) original-input (:input ops)
          cached-ops (assoc ops
                            :input (fn [field]
                                     (case field
                                       :sh06-resolution-cached-binding cache
                                       :*sh06-resolution-operation-binding* nil
                                       (original-input field)))
                            :sh06-resolution-current-source-key (constantly key)
                            :sh06-resolution-build-binding! (fn [_] (swap! builds inc) binding))
          first-binding (resolution/current-plan-binding! source cached-ops)]
      (is (identical? first-binding (resolution/current-plan-binding! source cached-ops)))
      (is (= 1 @builds))
      (is (= [:resolution-toolchain-drift]
             (:missing-fields (ex-data
                               (caught #(resolution/current-plan-binding!
                                         source (assoc cached-ops :sh06-resolution-current-source-key
                                                       (constantly (assoc key :toolchain :changed)))))))))
      (is (= [:resolution-source-or-toolchain-drift]
             (:missing-fields (ex-data
                               (caught #(resolution/binding-current!
                                         source first-binding
                                         (assoc cached-ops :sh06-resolution-current-source-key
                                                (constantly (assoc key :source :changed))))))))))))

(deftest scoped-operation-binding-and-host-containment-are-preserved
  (let [v (seed-var '*sh06-resolution-operation-binding*)
        binding (b/sh06-resolution-current-binding! source)
        scoped (assoc binding :reader-invocation invocation)
        ops (operations)]
    (with-bindings {v scoped}
      (is (identical? scoped (input :*sh06-resolution-operation-binding*)))
      (is (identical? scoped (resolution/current-plan-binding! source ops))))
    (doseq [error [(StackOverflowError. "unit") (AssertionError. "unit")
                   (LinkageError. "unit") (ex-info "unit" {:id "UNIT"})
                   (Exception. "unit")]]
      (let [data (ex-data (caught #(resolution/execute!
                                   source binding 'unit []
                                   (assoc ops :p15-s23-stage2-runtime-execute-function (fn [& _] (throw error))))))]
        (is (= "C5-UNRESOLVED" (:id data)))
        (is (seq (:missing-fields data)))))
    (let [interrupted (InterruptedException. "unit")
          observed (try
                     (resolution/execute! source binding 'unit []
                                          (assoc ops :p15-s23-stage2-runtime-execute-function
                                                 (fn [& _] (throw interrupted))))
                     (catch InterruptedException error [error (.isInterrupted (Thread/currentThread))])
                     (finally (Thread/interrupted)))]
      (is (identical? interrupted (first observed)))
      (is (true? (second observed))))))

(defn- with-report-context [operation]
  (let [ctx (atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
                   :phase :construction :epoch 0 :receipts [] :executions {}
                   :reuses {} :check-catalogs {} :failed-report-executions 0})]
    (try
      (with-bindings {(seed-var '*sh07-proof-transaction-context*) ctx} (operation ctx))
      (finally (swap! ctx assoc :open? false :receipts [])))))
(def ^:private unit-artifact {:artifact-id "unit" :semantic-projection-id "projection" :alias-table-id "aliases"})
(def ^:private unit-report {:status :passed :checks {:receipt-unit? true} :failed-checks []})
(defn- with-unit-verifier [operation]
  ;; Test actual receipt qualification/retention with isolated rule/admission ports.
  ;; This unit supplies no semantic, source-authentication or migration evidence.
  (with-redefs-fn
    {(seed-var 'sh06-resolution-context-validation) (fn [& _] {:status :passed})
     (seed-var 'sh06-resolution-artifact-verification-contained) (fn [& _] unit-report)}
    operation))
(defn- unit-verify [] (b/sh06-resolution-artifact-verification unit-artifact {}))
(defn- assert-interposition-misses [interpose]
  (with-unit-verifier
    #(with-report-context
       (fn [ctx]
         (is (= unit-report (unit-verify)))
         (is (= unit-report (unit-verify)))
         (is (= 1 (get-in @ctx [:executions :sh06])))
         (is (= 1 (get-in @ctx [:reuses :sh06])))
         (interpose (fn [] (dotimes [_ 2] (is (= unit-report (unit-verify))))))
         (is (= 3 (get-in @ctx [:executions :sh06])))
         (is (= 1 (get-in @ctx [:reuses :sh06])))
         (is (= unit-report (unit-verify)))
         (is (= 2 (get-in @ctx [:reuses :sh06])))))))

(deftest every-stage-and-operation-var-invalidates-old-receipts
  (let [stage-vars (set (vals (ns-publics 'gravity.resolution)))
        vars (set (concat stage-vars
                         (filter var? (vals (operations)))
                         [(seed-var 'resolution-stage-operations)]))]
    (doseq [v vars]
      (let [original @v
            replacement (if (stage-vars v)
                          (fn [& _] (throw (ex-info "Qualification invoked stage code" {})))
                          ;; Shared SHA ports are legitimately used later to hash the
                          ;; receipt catalog. Preserve them while changing root identity.
                          (fn [& args] (apply original args)))]
        (assert-interposition-misses #(with-redefs-fn {v replacement} %)))))
  (let [v (seed-var 'resolution-stage-defaults) copied (reduce-kv assoc {} @v)]
    (is (= @v copied))
    (is (not (identical? @v copied)))
    (assert-interposition-misses #(with-redefs-fn {v copied} %))))

(deftest equal-valued-configuration-cache-and-thread-bindings-invalidate-receipts
  (doseq [name ['sh06-resolution-source-relative-path 'sh06-resolution-expected-source-content-hash
                'sh06-resolution-public-function-shapes 'sh06-resolution-cached-binding
                'known-source-profiles 'sh06-resolution-all-core-symbols
                'sh06-resolution-special-symbols 'sh06-resolution-type-symbols]]
    (let [v (seed-var name) original @v
          replacement (cond (string? original) (String. ^String original)
                            (map? original) (reduce-kv assoc {} original)
                            (set? original) (into #{} (map identity) original)
                            :else (atom @original))]
      (is (not (identical? original replacement)))
      (assert-interposition-misses #(with-redefs-fn {v replacement} %))))
  (let [v #'resolution/execute! original @v dynamic? (.isDynamic v)]
    (try
      (.setDynamic v true)
      (assert-interposition-misses #(with-bindings {v original} (%)))
      (finally (.setDynamic v dynamic?)))))

(deftest legitimate-scoped-bindings-reuse-but-the-actual-plan-is-an-epoch-input
  (let [v (seed-var '*sh06-resolution-operation-binding*)
        original-root (.getRawRoot ^clojure.lang.Var v)
        binding {:plan {:body :original} :source-content-hash "same" :plan-semantic-hash "same"}]
    (with-unit-verifier
      #(with-report-context
         (fn [ctx]
           (with-bindings {v binding}
             (is (= unit-report (unit-verify)))
             (is (= unit-report (unit-verify))))
           (is (= 1 (get-in @ctx [:executions :sh06])))
           (is (= 1 (get-in @ctx [:reuses :sh06])))
           (with-bindings {v (assoc-in binding [:plan :body] :changed)}
             (is (= unit-report (unit-verify))))
           (is (= 2 (get-in @ctx [:executions :sh06])))
           (with-bindings {v binding} (is (= unit-report (unit-verify))))
           (is (= 2 (get-in @ctx [:reuses :sh06]))))))
    (is (identical? original-root (.getRawRoot ^clojure.lang.Var v)))))

(deftest actual-public-verification-retains-normal-scoped-reuse
  (let [artifact @real-artifact]
    (with-report-context
      (fn [ctx]
        (is (= :passed (:status (b/sh06-resolution-artifact-verification artifact :gravity.bootstrap/legacy invocation))))
        (is (= :passed (:status (b/sh06-resolution-artifact-verification artifact :gravity.bootstrap/legacy invocation))))
        (is (= 1 (get-in @ctx [:executions :sh06])))
        (is (= 1 (get-in @ctx [:reuses :sh06])))))))

(deftest construction-short-seam-tracks-the-new-stage-dependencies
  (let [environment (seed-var 'resolution-construction-environment)
        roots-fn (seed-var 'resolution-construction-roots)
        check! (seed-var 'resolution-construction-check!)
        reads (atom 0)
        expected (set (concat (vals (ns-publics 'gravity.resolution))
                              [(seed-var 'resolution-stage-operations)
                               (seed-var 'resolution-stage-defaults)
                               (seed-var 'resolution-stage-input)]))]
    ;; Isolate the existing readonly drift seam; no parent/owner admission runs.
    (with-redefs-fn
      {environment (fn [] (swap! reads inc) {})}
      #(let [roots (roots-fn)]
         (is (every? (set (map first roots)) expected))
         (doseq [v expected]
           (let [original @v
                 replacement (if (map? original) (reduce-kv assoc {} original)
                                 (fn [& args] (apply original args)))
                 error (caught (fn []
                                 (with-redefs-fn {v replacement}
                                   (fn [] (check! source {} {} roots :gravity.bootstrap/unavailable)))))]
             (is (= [:construction-parent-dependency-drift] (:missing-fields (ex-data error))))))
         (is (zero? @reads))))))

(deftest invocation-rejection-precedes-binding-and-stage-work
  (let [calls (atom 0) with-invocation (seed-var 'sh06-resolution-with-reader-invocation)]
    (with-redefs-fn
      {(seed-var 'sh03-reader-authenticated-envelope-options)
       (fn [& _] (throw (ex-info "invalid invocation" {:id "UNIT-INVOCATION"})))
       #'resolution/current-plan-binding! (fn [& _] (swap! calls inc))}
      #(is (= "UNIT-INVOCATION"
              (:id (ex-data (caught (fn [] (with-invocation source {:invalid true} identity))))))))
    (is (zero? @calls))))

(deftest interrupted-execution-unwinds-production-receipt-lifetime
  (let [interrupt (InterruptedException. "resolution execution") cleanup (atom nil)
        fail? (atom false) observer (seed-var '*sh07-proof-transaction-cleanup-observer*)
        prerequisites {(seed-var 'sh07-core-assert-pinned-revision!) (fn [& _] {})
                       (seed-var 'sh07-proof-transaction-source-snapshot) (fn [_] {})
                       (seed-var 'sh07-proof-transaction-core-snapshot) (fn [] {})
                       (seed-var 'sh06-resolution-context-validation) (fn [& _] {:status :passed})
                       (seed-var 'sh06-resolution-artifact-verification-contained)
                       (fn [& _]
                         (if @fail?
                           (resolution/execute! source {} 'unit [] (operations)) unit-report))}
        observed
        (try
          ;; Production lifetime, isolated Core snapshot preludes: no C6 admission.
          (with-redefs-fn prerequisites
            #(with-bindings {observer (fn [value] (reset! cleanup value))}
               (b/sh07-core-file-proof-transaction
                source
                (fn [_]
                  (unit-verify)
                  (reset! fail? true)
                  (with-redefs-fn {(seed-var 'p15-s23-stage2-runtime-execute-function)
                                  (fn [& _] (throw interrupt))} unit-verify)))))
          (catch InterruptedException error [error (.isInterrupted (Thread/currentThread))])
          (finally (Thread/interrupted)))]
    (is (identical? interrupt (first observed)))
    (is (true? (second observed)))
    (is (= {:open? false :cleanup-complete? true :retained-receipt-count 0}
           (select-keys @cleanup [:open? :cleanup-complete? :retained-receipt-count])))))
