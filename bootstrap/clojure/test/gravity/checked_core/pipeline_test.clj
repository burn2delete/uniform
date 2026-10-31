(ns gravity.checked-core.pipeline-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.checked-core :as core]))

(defn- seed-var [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Missing checked-core seed seam" {:name name}))))
(defn- operations [] ((seed-var 'checked-core-stage-operations)))
(defn- input [field] ((:input (operations)) field))
(defn- caught [f] (try (f) nil (catch clojure.lang.ExceptionInfo error error)))
(def ^:private source "<checked-core-runtime-unit>")
(def ^:private projection-compatibility-names
  '[sh07-core-source-path-from-resolution sh07-core-semantic-span sh07-core-value-kind
    sh07-core-children sh07-core-decimal-descriptor sh07-core-decimal-evidence
    sh07-core-projected-form-id sh07-core-projected-syntax-id sh07-core-projected-source-mismatch
    sh07-core-neutral-value sh07-core-macro-trace sh07-core-root-syntax-id
    sh07-core-decimal-evidence-complete! sh07-core-semantic-macro-trace
    sh07-core-source-occurrence-context sh07-core-build-form-tree sh07-core-source-map-order
    sh07-core-parameter-binding-paths sh07-core-lineage-with-semantic-trace sh07-core-lineage
    sh07-core-projection-binding-input sh07-b13-alias-name sh07-b13-fragment-record
    sh07-b13-fragment-manifest sh07-b13-binding-order sh07-b13-fragment-coverage
    sh07-b13-module-assembly-manifest sh07-core-request-from-verified-upstream sh07-core-authenticated-request
    sh07-core-digest-reference-ordinal sh07-core-resolve-reference! sh07-core-resolve-reference-vector!
    sh07-core-semantic-projection-id sh07-core-semantic-binding sh07-core-semantic-resolution
    sh07-core-semantic-var-reference sh07-core-semantic-mutation sh07-core-semantic-error-transfer
    sh07-core-semantic-error-handler sh07-core-semantic-node sh07-core-semantic-identity-preimage
    sh07-core-resolve-digest-preimage! sh07-core-digest-requests sh07-core-resolve-result
    sh07-core-public-decimal-presentation sh07-core-canonical-artifact sh07-core-raise-diagnostic!
    sh07-core-request-diagnostic! sh07-core-request-preflight!
    sh07-core-run-structural-request-for-test sh07-core-run-request-for-test
    sh07-core-projection-diagnostic! sh07-core-error-transfers-coherent?
    sh07-core-descendant-node-ids sh07-b9-handler-group-coherent? sh07-core-error-handlers-coherent?
    sh07-b11-guard-source sh07-b11-guard-source-valid? sh07-b11-guard-valid? sh07-b11-branch-tree sh07-b11-clause
    sh07-b11-match-pattern-graph-complete? sh07-b11-match-pattern-record-coherent? sh07-b11-match-group-coherent?
    sh07-core-match-products-coherent?])

(deftest namespace-contract-and-operation-inventories-agree
  (let [publics (ns-publics 'gravity.checked-core)
        contract (:gravity/namespace-contract (meta #'core/execute!))]
    (is (= 67 (count publics)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (is (= (set (keys (operations))) (set (:operation-inputs contract))))
    (doseq [field (:seed-inputs contract)]
      (is (identical? @(seed-var (symbol (name field))) (input field))))
    (doseq [[name api] (:public-api contract)]
      (is (= (:arglists api) (:arglists (meta (publics name)))))
      (is (= :none (:admission api)))
      (is (= contract (:gravity/namespace-contract (meta (publics name))))))
    (is (= '#{source-occurrence-context projected-source-mismatch lineage-with-semantic-trace request-from-verified-upstream}
           (set (keys (:private-implementation contract)))))
    (doseq [[name api] (:private-implementation contract)
            :let [v (ns-resolve 'gravity.checked-core name)]]
      (is (true? (:private (meta v))))
      (is (= (:arglists api) (:arglists (meta v))))
      (is (= contract (:gravity/namespace-contract (meta v)))))
    (is (true? (.isDynamic ^clojure.lang.Var (seed-var '*sh07-core-execution-binding*))))
    (is (not (contains? (ns-aliases 'gravity.checked-core) 'b)))
    (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :forbids])))))

(deftest actual-current-source-plan-and-execution-retain-development-status
  (let [ops (operations) binding (b/sh07-core-current-binding!)
        identities (core/plan-identities (:plan binding) ops)
        empty-state (core/execute! source 'sh07-empty-state [] ops)]
    (is (= :development-unadmitted (:revision-status binding)))
    (is (false? (:self-hosted? binding)))
    (is (= identities (select-keys binding (keys identities))))
    (is (= (:source-content-hash binding) (get-in binding [:plan :source :sha256])))
    (is (= [] (:nodes empty-state) (:var-references empty-state)))
    (is (= (core/source-key ops) ((seed-var 'sh07-core-current-source-key))))
    (is (= (b/sh07-core-source-path) (core/source-path ops)))))

(deftest projected-identities-distinguish-occurrences-and-preserve-generated-origins
  (let [ops (operations) revision :unit-revision root :unit-root
        first-id (core/projected-form-id revision root [0] :integer ops)
        trace (core/macro-trace {:source-revision-id revision :sh05-artifact-id :macro-artifact
                                :macro-expansion-trace-id :macro-trace}
                               {:input-syntax-id :input :output-syntax-id root} ops)]
    (is (= first-id (b/sh07-core-projected-form-id revision root [0] :integer)))
    (is (= first-id (core/projected-form-id revision root [0] :integer ops)))
    (doseq [[r s path kind] [[:other root [0] :integer] [revision :other [0] :integer]
                             [revision root [1] :integer] [revision root [0] :decimal]]]
      (is (not= first-id (core/projected-form-id r s path kind ops))))
    (is (= root (core/projected-syntax-id revision root [] :list trace ops)))
    (is (= (:introduced-fn-syntax-id trace)
           (core/projected-syntax-id revision root [2] :list trace ops)))
    (is (= revision (:source-revision-id trace)))
    (is (not= (:def-generated-origin-id trace) (:fn-generated-origin-id trace)))
    (is (= (core/semantic-macro-trace trace ops) (b/sh07-core-semantic-macro-trace trace)))
    (is (= (core/root-syntax-id revision 0 '(def value 1) ops)
           (b/sh07-core-root-syntax-id revision 0 '(def value 1))))))

(deftest decimal-occurrence-state-preserves-scale-and-rejects-unconsumed-evidence
  (let [ops (operations) value (java.math.BigDecimal. "1.250")
        descriptor (b/p15-s23-c6c10-literal-scalar-descriptor value)
        records (mapv #(hash-map :kind :decimal :decoded value :form-id % :literal-id % :token-id %) [:a :b])
        evidence {:records {descriptor records} :next (atom {}) :cache (atom {})}
        first-value (core/neutral-value source :revision :root [0] evidence value ops)
        again (core/neutral-value source :revision :root [0] evidence value ops)]
    (is (= 3 (:scale first-value)))
    (is (= "1250" (:unscaled-value first-value)))
    (is (identical? first-value again))
    (is (= 1 (get @(:next evidence) descriptor)))
    (let [error (caught #(core/decimal-evidence-complete! source evidence ops))]
      (is (= ["C6-ORIGIN" :decimal-c2-occurrence-bijection]
             ((juxt :id :reason) (ex-data error)))))
    (let [second-value (core/neutral-value source :revision :root [1] evidence value ops)]
      (is (not= (:literal-id first-value) (:literal-id second-value)))
      (is (not= (:projection-form-id first-value) (:projection-form-id second-value)))
      (is (= :passed (core/decimal-evidence-complete! source evidence ops))))
    (doseq [bad [(assoc (first records) :kind :integer)
                 (assoc (first records) :decoded (java.math.BigDecimal. "1.251")) nil]]
      (is (= ["C6-ORIGIN" :exact-decimal-c2-evidence]
             ((juxt :id :reason) (ex-data (caught #(core/decimal-descriptor source value bad :form ops)))))))))

(deftest projection-classification-and-parameter-paths-retain-their-boundary
  (let [ops (operations)]
    (doseq [value [nil true 1 (java.math.BigDecimal. "1.0") 1/2 \a "text" :key 'name [] {} #{} '(value) (Object.)]]
      (is (= (b/sh07-core-value-kind value) (core/value-kind value ops)))
      (is (= (b/sh07-core-children value) (core/children value ops))))
    (is (= [{:name 'a :path [2 0]} {:name 'more :path [2 4]}]
           (core/parameter-binding-paths '[a :- Integer & more] [2] ops)))
    (is (= {:byte-start 1 :byte-end 4}
           (core/semantic-span {:primary {:byte-start 1 :byte-end 4 :source "ignored"}} ops)))
    (is (= "actual.gravity" (core/source-path-from-resolution
                              {:gravity-resolution-boundary {:authenticated-resolution-request
                                                             {:provenance {:actual-source-path "actual.gravity"}}}} ops)))))

(def ^:private lineage-input
  {:artifact-id :authenticated-owner-id
   :gravity-resolution-boundary
   {:resolved-analysis {:module-contract {:namespace 'unit :profile :meta :target :jvm :effects [] :capabilities [] :safety :safe :exports ['value]}
                        :binding-table [{:binding-id :path-specific-id :name 'value :kind :var :namespace 'unit :visibility :public}]
                        :resolution-table [{:reference-syntax-id :syntax :symbol 'value :position :value :resolution-order :local-binding}]
                        :alias-table [{:alias 'dep :namespace 'dependency}]
                        :lexical-scope-graph {:scope-count 1 :edge-count 0 :status :accepted}}}
   :sh05-macro-artifact
   {:expanded-syntax-stream [{:form '(def value 1)}] :macro-expansion-trace []
    :gravity-macro-boundary {:authenticated-sh04-artifact {:c2-reader-artifact
                                                        {:source-unit-record {:bytes-hash :source-byte-hash :path "/unit.gravity"}}}}}})

(deftest lineage-separates-authenticated-owner-identity-from-semantic-projection
  (let [ops (operations) result (core/lineage lineage-input ops)
        changed-owner (core/lineage (assoc lineage-input :artifact-id :different-authenticated-id) ops)]
    (is (= result (b/sh07-core-lineage lineage-input)))
    (is (= :source-byte-hash (:source-revision-id result)))
    (is (= :authenticated-owner-id (:authenticated-sh06-artifact-id result)))
    (is (= (:sh06-semantic-projection-id result) (:sh06-artifact-id result)))
    (is (= (dissoc result :authenticated-sh06-artifact-id)
           (dissoc changed-owner :authenticated-sh06-artifact-id)))
    (doseq [changed [(assoc-in lineage-input [:sh05-macro-artifact :expanded-syntax-stream 0 :form] '(def value 2))
                     (assoc-in lineage-input [:gravity-resolution-boundary :resolved-analysis :binding-table 0 :name] 'other)
                     (assoc-in lineage-input [:gravity-resolution-boundary :resolved-analysis :module-contract :profile] :hosted)
                     (assoc-in lineage-input [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact
                                              :c2-reader-artifact :source-unit-record :bytes-hash] :changed-source)]]
      (is (not= (:sh06-semantic-projection-id result) (:sh06-semantic-projection-id (core/lineage changed ops)))))))

(deftest fragments-preserve-binding-and-reference-order-and-reject-undeclared-aliases
  (let [ops (operations) lineage {:source-revision-id :source :sh06-semantic-projection-id :semantic :alias-table-id :aliases}
        trees [{:root {:form-id :root-a} :records [{:form-id :root-a :syntax-id :syntax-a}]}
               {:root {:form-id :root-b} :records [{:form-id :root-b :syntax-id :syntax-b}]}]
        bindings [{:binding-id :external} {:binding-id :b :definition-syntax-id :syntax-b}
                  {:binding-id :a :definition-syntax-id :syntax-a}]
        resolutions [{:reference-syntax-id :syntax-a :binding-id :external :resolution-order :alias-qualified-required-binding :symbol 'dep/value}
                     {:reference-syntax-id :syntax-a :binding-id :a :resolution-order :local-binding :symbol 'value}]
        aliases [{:alias 'dep}]
        ordered (core/binding-order :module trees bindings ops)
        fragments (core/fragment-manifest lineage :module trees ordered resolutions aliases ops)
        coverage (core/fragment-coverage :module (mapcat :records trees) ordered resolutions fragments ops)
        assembly (core/module-assembly-manifest lineage fragments ops)]
    (is (= [:a :b :external] (mapv :binding-id ordered)))
    (is (= [0 1] (mapv :ordinal fragments)))
    (is (= [:a] (:local-binding-ids (first fragments))))
    (is (= [:external] (:external-binding-ids (first fragments))))
    (is (= ['dep] (:alias-names (first fragments))))
    (is (= [:syntax-a :syntax-a] (:resolution-reference-syntax-ids (first fragments))))
    (is (= [:root-a :root-b] (:covered-root-form-ids coverage) (:ordered-fragment-ids assembly)))
    (is (= [:a :b] (:covered-local-binding-ids coverage)))
    (is (= :source (:content-id assembly) (:source-revision-id assembly)))
    (is (= :semantic (:module-id assembly) (:sh06-semantic-projection-id assembly)))
    (is (= fragments (b/sh07-b13-fragment-manifest lineage :module trees ordered resolutions aliases)))
    (is (= ["C6-VERIFY" :fragment-undeclared-alias]
           ((juxt :id :reason) (ex-data (caught #(core/fragment-record lineage :module 0 (first trees) ordered resolutions [] ops))))))))

(deftest projection-binding-input-excludes-only-the-existing-nonsemantic-envelope-fields
  (let [ops (operations) request {:forms [{:form-id :form :value 1}] :projection-binding :old :provenance {:source-path "/a"}}
        first-input (core/projection-binding-input request ops)]
    (is (= first-input (b/sh07-core-projection-binding-input request)))
    (is (= first-input (core/projection-binding-input (assoc request :projection-binding :new :provenance {:source-path "/b"}) ops)))
    (is (not= first-input (core/projection-binding-input (assoc-in request [:forms 0 :value] 2) ops)))))

(deftest request-gateway-preserves-context-and-invocation-dispatch
  ;; Dispatch model only; real paired source evidence exercises the owner.
  (let [calls (atom []) request :unit-c5-artifact report :unit-owner-report
        ops (assoc (operations)
                   :sh07-core-authenticated-request (fn [& args] [:recursive (vec args)])
                   :sh07-core-source-path-from-resolution (constantly source)
                   :sh03-reader-authenticated-envelope-options (fn [& args] (swap! calls conj [:reader (vec args)]))
                   :sh06-resolution-artifact-verification (fn [& args] (swap! calls conj [:owner (vec args)]) report)
                   :sh07-core-request-from-verified-upstream (fn [& args] [:constructed (vec args)]))]
    (is (= [:recursive [request :gravity.bootstrap/legacy]] (core/authenticated-request request ops)))
    (is (= [:recursive [request :context nil]] (core/authenticated-request request :context ops)))
    (doseq [[context invocation expected] [[:gravity.bootstrap/legacy nil [request]]
                                           [:context nil [request :context]]
                                           [:context {:reader :unit} [request :context {:reader :unit}]]]]
      (reset! calls [])
      (is (= [:constructed [request report]] (core/authenticated-request request context invocation ops)))
      (is (= (if invocation [[:reader [source invocation]] [:owner expected]] [[:owner expected]]) @calls)))
    (reset! calls [])
    (let [error (caught #(core/authenticated-request request nil {:reader :unit} ops))]
      (is (= ["C6-VERIFY" [:sh06-context]] ((juxt :id :missing-fields) (ex-data error))))
      (is (= [[:reader [source {:reader :unit}]]] @calls)))
    (let [error (ex-info "owner failed" {:id "UNIT-OWNER"})]
      (is (identical? error (caught #(core/authenticated-request request :context nil
                                                    (assoc ops :sh06-resolution-artifact-verification (fn [& _] (throw error))))))))))

(deftest private-request-constructor-retains-all-upstream-premises
  (let [construct (ns-resolve 'gravity.checked-core 'request-from-verified-upstream)
        valid {:kind :gravity/sh06-resolution-artifact :capability-based-proof {:status :complete}}
        ops (assoc (operations) :sh07-core-source-path-from-resolution (constantly source))]
    (is (true? (:private (meta construct))))
    (doseq [[artifact report] [[(assoc valid :kind :forged) {:status :passed}]
                               [valid {:status :rejected}]
                               [(dissoc valid :capability-based-proof) {:status :passed}]
                               [(assoc-in valid [:capability-based-proof :status] :incomplete) {:status :passed}]]]
      (is (= ["C6-VERIFY" [:fresh-authenticated-sh06-resolution]]
             ((juxt :id :missing-fields) (ex-data (caught #(construct artifact report ops)))))))))

(deftest digest-reference-schema-and-index-legality-retain-their-existing-meaning
  (let [ops (operations) digest (str "sha256:" (apply str (repeat 64 "a")))
        reference {:artifact :gravity/sh07-internal-digest-reference :schema-version 1 :authority :sh07-digest-resolver :ordinal 0}]
    (is (= 0 (core/digest-reference-ordinal reference ops)))
    (is (= 123456789012345678901234567890N
           (core/digest-reference-ordinal (assoc reference :ordinal 123456789012345678901234567890N) ops)))
    (is (= [digest digest] (core/resolve-reference-vector! source [reference reference] [digest] ops)))
    (doseq [bad [(assoc reference :extra true) (assoc reference :authority :forged)
                 (assoc reference :schema-version 2) (assoc reference :ordinal -1)
                 (assoc reference :ordinal 0.0) nil]]
      (is (nil? (core/digest-reference-ordinal bad ops)))
      (is (= "C6-VERIFY" (:id (ex-data (caught #(core/resolve-reference! source bad [digest] ops)))))))
    (doseq [[ref values] [[(assoc reference :ordinal 1) [digest]] [reference ["not-a-sha256"]] [reference []]]]
      (is (= "C6-VERIFY" (:id (ex-data (caught #(core/resolve-reference! source ref values ops)))))))))

(deftest semantic-reference-normalization-preserves-the-existing-identity-boundary
  (let [ops (operations) input {:binding-id :binding :upstream-binding-id :upstream
                                :definition-artifact-id :owner :authenticated-sh06-artifact-id :authenticated
                                :attributes {:sh06-semantic-projection-id :semantic} :unrelated :preserved}
        result (core/semantic-var-reference nil input ops)]
    (is (= :semantic (core/semantic-projection-id input ops)))
    (is (= (b/sh07-core-semantic-var-reference nil input) result))
    (is (= :binding (:upstream-binding-id result)))
    (is (= :semantic (:definition-artifact-id result) (:authenticated-sh06-artifact-id result)))
    (is (= :preserved (:unrelated result)))
    (is (= :explicit (:definition-artifact-id (core/semantic-binding :explicit input ops))))
    (is (= :binding (:upstream-binding-id (core/semantic-resolution input ops))))
    (is (= {:binding-id :binding} (core/semantic-binding :semantic {:binding-id :binding} ops)))))

(deftest semantic-products-normalize-each-existing-reference-family
  (let [ops (operations) reference {:binding-id :binding :upstream-binding-id :upstream
                                    :definition-artifact-id :authenticated :authenticated-sh06-artifact-id :authenticated
                                    :unrelated :preserved}
        mutation {:target-binding-id :binding :target-upstream-binding-id :upstream
                  :target-definition-artifact-id :authenticated :authenticated-sh06-artifact-id :authenticated}
        handler {:authenticated-sh06-artifact-id :authenticated :unrelated :preserved}
        preimage {:projection-binding :authenticated
                  :lineage {:sh06-semantic-projection-id :semantic :authenticated-sh06-artifact-id :authenticated}
                  :binding-table [reference] :resolution-table [reference] :var-references [reference]
                  :mutations [mutation] :error-transfers [handler] :error-handlers [handler]
                  :nodes [{:core-form :var :attributes reference} {:core-form :set! :attributes mutation}
                          {:core-form :throw :attributes handler} {:core-form :try :attributes handler}]
                  :unrelated :preserved}
        result (core/semantic-identity-preimage preimage ops)]
    (is (= result (b/sh07-core-semantic-identity-preimage preimage)))
    (is (= :semantic (:projection-binding result) (get-in result [:lineage :authenticated-sh06-artifact-id])))
    (is (= :binding (get-in result [:mutations 0 :target-upstream-binding-id])))
    (is (= :semantic (get-in result [:mutations 0 :target-definition-artifact-id])))
    (doseq [node (:nodes result)]
      (is (= :semantic (get-in node [:attributes :authenticated-sh06-artifact-id]))))
    (doseq [family [:error-transfers :error-handlers]]
      (is (= :preserved (get-in result [family 0 :unrelated])))
      (is (= :semantic (get-in result [family 0 :authenticated-sh06-artifact-id]))))
    (is (= :preserved (:unrelated result)))
    (is (= {:core-form :literal :attributes {:value :unchanged}}
           (core/semantic-node :semantic {:core-form :literal :attributes {:value :unchanged}} ops)))
    (is (= {:unrelated :preserved} (core/semantic-mutation nil {:unrelated :preserved} ops)))
    (is (= {:unrelated :preserved} (core/semantic-error-transfer nil {:unrelated :preserved} ops)))
    (is (= {:unrelated :preserved} (core/semantic-error-handler nil {:unrelated :preserved} ops)))))

(deftest digest-purpose-dispatch-preserves-the-existing-gravity-calls-and-refusals
  (let [ops (operations) calls (atom [])
        fixture-ops (assoc ops :sh07-core-execute! (fn [& args] (swap! calls conj (vec args)) {:status :accepted :value {:unit :value}}))]
    (doseq [[purpose function] [[:sh07-core-artifact-id 'sh07-resolve-identity-preimage]
                                [:sh07-core-provenance-binding-id 'sh07-resolve-provenance-preimage]
                                [:sh07-source-core-map-id 'sh07-resolve-source-core-map-preimage]]]
      (reset! calls [])
      (is (= {:unit :value} (core/resolve-digest-preimage! source purpose {:unit :preimage} [] fixture-ops)))
      (is (= [[source function [{:unit :preimage} []]]] @calls))
      (is (= "C6-VERIFY" (:id (ex-data (caught #(core/resolve-digest-preimage!
                                                 source purpose {} []
                                                 (assoc ops :sh07-core-execute! (fn [& _] {:status :rejected :reason :unit-rejection})))))))))
    (let [preimage {:unit :diagnostic}]
      (is (identical? preimage (core/resolve-digest-preimage! source :sh07-core-diagnostic-id preimage [] ops))))
    (is (= :unknown-digest-purpose
           (:reason (ex-data (caught #(core/resolve-digest-preimage! source :unknown {} [] ops))))))))

(deftest declared-digests-reject-schema-ordinal-and-forward-reference-drift
  (let [ops (operations) diagnostic {:ordinal 0 :purpose :sh07-core-diagnostic-id :preimage {:rule "C6-VERIFY"}}
        digest (first (core/digest-requests source [diagnostic] ops))
        reference {:artifact :gravity/sh07-internal-digest-reference :schema-version 1 :ordinal 0 :authority :sh07-digest-resolver}
        node {:ordinal 1 :purpose :sh07-core-node-id :preimage {:children [reference] :evaluated-children [reference]}}
        result (core/digest-requests source [diagnostic node] ops)]
    (is (= [digest] (core/digest-requests source [diagnostic] ops)))
    (is (= result (b/sh07-core-digest-requests source [diagnostic node])))
    (is (= 2 (count result)))
    (is (not= (first result) (second result)))
    (doseq [bad [[(assoc diagnostic :ordinal 1)] [(assoc diagnostic :extra true)]
                 [diagnostic diagnostic] [(assoc diagnostic :purpose :unknown)]
                 [diagnostic (assoc-in node [:preimage :children 0 :ordinal] 1)]]]
      (is (= "C6-VERIFY" (:id (ex-data (caught #(core/digest-requests source bad ops)))))))
    (let [calls (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! calls inc) 7)
                   (equals [_] (swap! calls inc) false)
                   (toString [] (swap! calls inc) "opaque"))]
      (is (= "C6-VERIFY" (:id (ex-data (caught #(core/digest-requests source [opaque] ops))))))
      (is (zero? @calls)))))

(deftest materialized-result-retains-the-exact-runtime-result-boundary
  (let [ops (operations) calls (atom []) value {:nodes []}
        fixture-ops (assoc ops :sh07-core-execute! (fn [& args] (swap! calls conj (vec args)) {:status :accepted :value value}))]
    (is (identical? value (core/resolve-result source :template :unused-declarations [:digest] fixture-ops)))
    (is (= [[source 'sh07-resolve-core-template [:template [:digest]]]] @calls))
    (let [error (caught #(core/resolve-result source :template [] []
                                            (assoc ops :sh07-core-execute! (fn [& _] {:status :rejected :reason :missing-digest}))))]
      (is (= ["C6-VERIFY" :missing-digest] ((juxt :id :reason) (ex-data error)))))))

(deftest canonical-candidate-assembly-preserves-data-without-minting-proof
  (let [ops (operations)
        template {:artifact-id-request :artifact-id :provenance-binding-id-request :provenance-id
                  :identity-preimage {:projection-binding :authenticated
                                      :lineage {:authenticated-sh06-artifact-id :authenticated
                                                :sh06-semantic-projection-id :semantic}}
                  :nodes [{:node-id :node}] :provenance {:actual-source-path "actual.gravity"}
                  :metadata {:unsafe-island :preserved}}
        result (core/canonical-artifact template ops)]
    (is (= result (b/sh07-core-canonical-artifact template)))
    (is (= :gravity/sh07-canonical-core-artifact (:artifact result)))
    (is (= :artifact-id (:artifact-id result)))
    (is (= :provenance-id (:provenance-binding-id result)))
    (is (not (contains? result :artifact-id-request)))
    (is (not (contains? result :provenance-binding-id-request)))
    (is (= :semantic (get-in result [:identity-preimage :projection-binding])
           (get-in result [:identity-preimage :lineage :authenticated-sh06-artifact-id])))
    (is (= (:provenance template) (:provenance result)))
    (is (= (:metadata template) (:metadata result)))
    (is (identical? (:nodes template) (:nodes result)))
    (is (not (contains? result :capability-based-proof)))))

(deftest decimal-presentation-is-an-explicit-view-over-unchanged-identity-data
  (let [ops (operations) descriptor {:kind :gravity/arbitrary-decimal-literal :unscaled-value "1250" :scale 3}
        decimal-node {:attributes {:literal-kind :decimal :value descriptor}}
        ordinary-node {:attributes {:literal-kind :integer :value 7}}
        candidate {:artifact-id :identity :identity-preimage {:nodes [decimal-node ordinary-node]}
                   :nodes [decimal-node ordinary-node]}
        result (core/public-decimal-presentation candidate ops)
        value (get-in result [:nodes 0 :attributes :value])]
    (is (instance? java.math.BigDecimal value))
    (is (= 3 (.scale ^java.math.BigDecimal value)))
    (is (= "1250" (str (.unscaledValue ^java.math.BigDecimal value))))
    (is (= ordinary-node (get-in result [:nodes 1])))
    (is (= descriptor (get-in candidate [:nodes 0 :attributes :value])))
    (is (identical? (:identity-preimage candidate) (:identity-preimage result)))
    (is (= :identity (:artifact-id result)))
    (is (= result (b/sh07-core-public-decimal-presentation candidate)))
    (is (.contains ^String (:doc (meta #'core/public-decimal-presentation)) "Do not use this view as an"))))

(deftest lowering-diagnostic-carrier-preserves-source-facts-and-declared-id
  (let [ops (operations) diagnostic {:rule "C6-VERIFY" :source-span {:source "actual.gravity" :byte-start 3}
                                    :profile :meta :target :jvm :facts {:reason :unit}}
        request {:purpose :sh07-core-diagnostic-id :preimage {:rule "C6-VERIFY" :facts {:reason :unit}}}
        template {:diagnostics [diagnostic] :digest-requests [request]}
        expected (assoc diagnostic :diagnostic-id-request
                        (b/reader-canonical-hash {:domain :gravity/sh07-declared-digest-v1
                                                  :purpose (:purpose request) :preimage (:preimage request)}))
        failure (ex-data (caught #(core/raise-diagnostic! "ignored.gravity" template ops)))]
    (is (= expected failure))
    (is (= failure (ex-data (caught #(b/sh07-core-raise-diagnostic! "ignored.gravity" template)))))
    (is (= "actual.gravity" (get-in failure [:source-span :source])))))

(deftest request-diagnostic-carrier-retains-upstream-and-semantic-identities
  (let [ops (operations) request {:provenance {:actual-source-path "actual.gravity"}
                                 :lineage {:source-revision-id :source :sh06-artifact-id :semantic-alias
                                           :authenticated-sh06-artifact-id :authenticated :sh06-semantic-projection-id :semantic}
                                 :module {:namespace 'unit :profile :meta :target :jvm}}
        facts {:reason :request-shape :input-schema 999}
        failure (ex-data (caught #(core/request-diagnostic! request facts ops)))]
    (is (= failure (ex-data (caught #(b/sh07-core-request-diagnostic! request facts)))))
    (is (= ["C6-VERIFY" :error :core-lowering] ((juxt :rule :severity :stage) failure)))
    (is (= {:source "actual.gravity"} (:source-span failure)))
    (is (= {:syntax-id nil :form-id nil :core-node-id nil}
           (select-keys failure [:syntax-id :form-id :core-node-id])))
    (is (= [] (:generated-origin-chain failure)))
    (is (= :authenticated (get-in failure [:facts :authenticated-sh06-artifact-id])))
    (is (= :semantic (get-in failure [:facts :sh06-semantic-projection-id])))
    (is (= facts (get-in failure [:facts :rule-specific])))
    (is (true? (get-in failure [:facts :fail-closed])))
    (is (= (b/reader-canonical-hash {:domain :gravity/sh07-request-diagnostic-v16
                                    :source-revision-id :source :rule-specific facts})
           (:diagnostic-id-request failure)))
    (let [missing (ex-data (caught #(core/request-diagnostic! {} {} ops)))]
      (is (= {:namespace nil :profile nil :target nil} (select-keys missing [:namespace :profile :target])))
      (is (= :authenticated-core-request (get-in missing [:facts :reason]))))))

(defn- bind-request [request]
  (assoc request :projection-binding (b/reader-canonical-hash (b/sh07-core-projection-binding-input request))))

(defn- preflight-request []
  (bind-request
   {:schema-version 15 :scope :sh07-b15-keyword-map-lookup
    :lineage {:alias-table-id (b/reader-canonical-hash {:domain :gravity/sh07-sh06-alias-table-v1 :aliases []})}
    :module {} :provenance {:actual-source-path source} :forms [] :binding-table []
    :alias-table [] :resolution-table [] :fragment-manifest [] :macro-expansion-trace []}))

(deftest request-preflight-retains-shapes-identities-and-native-first-order
  (let [ops (operations) request (preflight-request)]
    (is (= :passed (core/request-preflight! request ops) (b/sh07-core-request-preflight! request)))
    (doseq [[bad reason] (concat
                         [[nil :request-map-required]
                          [(assoc request :projection-binding :forged) :request-projection-binding-mismatch]
                          [(bind-request (assoc-in request [:lineage :alias-table-id] :forged)) :request-alias-table-lineage-mismatch]
                          [(bind-request (assoc request :schema-version 16)) :request-schema-version]
                          [(bind-request (assoc request :scope :other)) :request-scope]]
                         (map (fn [[field reason]] [(assoc request field nil) reason])
                              [[:lineage :request-lineage-map-required] [:module :request-module-map-required]
                               [:provenance :request-provenance-map-required] [:forms :request-forms-vector-required]
                               [:binding-table :request-binding-table-vector-required] [:alias-table :request-alias-table-vector-required]
                               [:resolution-table :request-resolution-table-vector-required] [:fragment-manifest :request-fragment-manifest-vector-required]
                               [:macro-expansion-trace :request-macro-trace-vector-required]]))]
      (let [failure (ex-data (caught #(core/request-preflight! bad ops)))]
        (is (= "C6-VERIFY" (:rule failure)))
        (is (= reason (get-in failure [:facts :reason])))
        (is (= failure (ex-data (caught #(b/sh07-core-request-preflight! bad)))))))
    (let [calls (atom []) opaque (proxy [Object] []
                                  (hashCode [] (swap! calls conj :hash) 7)
                                  (equals [_] (swap! calls conj :equals) false)
                                  (toString [] (swap! calls conj :string) "opaque"))
          guarded (assoc ops :reader-canonical-hash (fn [& _] (swap! calls conj :canonical-hash))
                         :sh07-core-request-diagnostic! (fn [& _] (swap! calls conj :diagnostic)))
          failure (caught #(core/request-preflight! (assoc request :forms [opaque]) guarded))]
      (is (= "C6-VERIFY" (:id (ex-data failure))))
      (is (= [] @calls)))))

(deftest executable-test-runner-verifies-owner-and-membership-before-execution
  ;; Dispatch model; paired current Reader/C5 source evidence exercises the owner.
  (let [calls (atom []) request {:unit :request} parent :parent
        ops (assoc (operations)
                   :sh07-core-run-request-for-test (fn [& args] [:recursive (vec args)])
                   :sh07-core-source-path-from-resolution (constantly source)
                   :sh03-reader-authenticated-envelope-options (fn [& args] (swap! calls conj [:reader (vec args)]))
                   :sh06-resolution-artifact-verification (fn [& args] (swap! calls conj [:owner (vec args)]) {:status :passed})
                   :sh07-core-authenticated-request (fn [& args] (swap! calls conj [:request (vec args)]) request)
                   :sh07-core-run-structural-request-for-test (fn [value] (swap! calls conj [:execute value]) :unit-result))]
    (is (= [:recursive [parent request nil]] (core/run-request-for-test parent request ops)))
    (doseq [invocation [nil {:reader :current}]]
      (reset! calls [])
      (is (= :unit-result (core/run-request-for-test parent request invocation ops)))
      (is (= (vec (concat (when invocation [[:reader [source invocation]]])
                          [[:owner (if invocation [parent :gravity.bootstrap/legacy invocation] [parent])]
                           [:request [parent :gravity.bootstrap/legacy invocation]] [:execute request]])) @calls)))
    (doseq [[changes reason expected-calls]
            [[{:sh06-resolution-artifact-verification (fn [& args] (swap! calls conj [:owner (vec args)])
                                                       {:status :rejected :failed-checks [:tampered]})}
              :authenticated-sh06-artifact-required [[:owner [parent]]]]
             [{} :authenticated-sh06-request-membership-mismatch
              [[:owner [parent]] [:request [parent :gravity.bootstrap/legacy nil]]]]]]
      (reset! calls [])
      (let [failure (caught #(core/run-request-for-test parent {:unit :forged} nil (merge ops changes)))]
        (is (= reason (get-in (ex-data failure) [:facts :reason])))
        (is (= expected-calls @calls))))
    (let [contract (:gravity/namespace-contract (meta #'core/run-request-for-test))]
      (is (= :with-execution-binding (:scoped-binding-operation contract)))
      (doseq [name '[run-request-for-test run-structural-request-for-test]]
        (is (true? (get-in contract [:public-api name :test-only?])))
        (is (= :none (get-in contract [:public-api name :admission])))))))

(defn- structural-fixture [calls]
  (let [binding-var (seed-var '*sh07-core-execution-binding*)
        core-binding {:source-content-hash :unit-source :plan :unit-plan}
        input-key {:source [:unit :unit-source]}
        raw {:status :accepted :core-template :template :digest-requests [:digest-request]}
        ops (assoc (operations)
                   :input (fn [field] (case field :*sh07-core-execution-binding* @binding-var
                                           :sh07-core-cached-binding (delay core-binding)))
                   :sh07-core-current-source-key (constantly input-key)
                   :sh07-core-request-preflight! (fn [request] (swap! calls conj [:preflight request @binding-var]) :passed)
                   :sh07-core-execute! (fn [_ function args]
                                         (swap! calls conj [:execute function args @binding-var])
                                         (case function sh07-build-core-template raw
                                               sh07-verify-core-template {:status :passed}
                                               sh07-verify-core-resolved {:status :passed}))
                   :sh07-core-digest-requests (fn [_ _] [:resolved-digest])
                   :sh07-core-resolve-result (fn [& _] :resolved-template)
                   :sh07-core-canonical-artifact (fn [value] {:unit-candidate value}))]
    {:binding core-binding :key input-key :raw raw :ops ops}))

(deftest structural-test-execution-scopes-the-selected-binding-and-replays-in-order
  (let [calls (atom []) {:keys [binding raw ops]} (structural-fixture calls)
        binding-var (seed-var '*sh07-core-execution-binding*) original @binding-var
        request {:provenance {:actual-source-path source}}
        result (core/run-structural-request-for-test request ops)]
    (is (= {:raw-template-result raw :canonical-core-artifact {:unit-candidate :resolved-template}
            :digest-requests [:digest-request] :resolved-digests [:resolved-digest]
            :template-verification {:status :passed} :resolved-verification {:status :passed}} result))
    (is (= [[:preflight request binding]
            [:execute 'sh07-build-core-template [request] binding]
            [:execute 'sh07-verify-core-template [request :template [:digest-request]] binding]
            [:execute 'sh07-verify-core-resolved [request :resolved-template [:digest-request] [:resolved-digest]] binding]] @calls))
    (is (identical? original @binding-var))
    (let [outer (assoc binding :plan :outer-plan) error (ex-info "unit preflight" {:id "UNIT"})]
      (with-bindings {binding-var outer}
        (reset! calls [])
        (core/run-structural-request-for-test request ops)
        (is (every? #(identical? outer (last %)) @calls))
        (is (identical? error (caught #(core/run-structural-request-for-test
                                        request (assoc ops :sh07-core-request-preflight! (fn [_] (throw error)))))))
        (is (identical? error (caught #((:with-execution-binding ops) binding
                                      (fn [] (is (identical? binding @binding-var)) (throw error))))))
        (is (identical? outer @binding-var))))
    (is (identical? original @binding-var))))

(deftest structural-test-execution-rejects-source-drift-replay-failure-and-invalid-status
  (let [calls (atom []) {:keys [key ops]} (structural-fixture calls)
        request {:provenance {:actual-source-path source}}]
    (doseq [[keys expected-calls] [[[{:source [:unit :stale]}] 0] [[key {:source [:unit :changed]}] 4]]]
      (reset! calls [])
      (let [remaining (atom keys)
            drift-ops (assoc ops :sh07-core-current-source-key
                             (fn [] (let [value (first @remaining)] (swap! remaining next) value)))
            failure (caught #(core/run-structural-request-for-test request drift-ops))]
        (is (= :checked-core-operation-revision-drift (:reason (ex-data failure))))
        (is (= expected-calls (count @calls)))))
    (doseq [[failure-at expected-field expected-functions]
            [['sh07-verify-core-template :template-verification ['sh07-build-core-template 'sh07-verify-core-template]]
             ['sh07-verify-core-resolved :resolved-verification ['sh07-build-core-template 'sh07-verify-core-template 'sh07-verify-core-resolved]]]]
      (reset! calls [])
      (let [execute (:sh07-core-execute! ops)
            failure (caught #(core/run-structural-request-for-test
                              request (assoc ops :sh07-core-execute!
                                             (fn [path function args]
                                               (if (= function failure-at)
                                                 (do (swap! calls conj [:execute function]) {:status :rejected})
                                                 (execute path function args))))))]
        (is (= "C6-VERIFY" (:id (ex-data failure))))
        (is (= {:status :rejected} (get (ex-data failure) expected-field)))
        (is (= expected-functions (mapv second (filter #(= :execute (first %)) @calls))))))
    (let [rejected {:status :rejected :rule "UNIT-REJECT"}
          expected (ex-info "unit Gravity rejection" {:id "UNIT-REJECT"})
          raised (atom nil)
          failure (caught #(core/run-structural-request-for-test
                            request (assoc ops :sh07-core-execute! (fn [& _] rejected)
                                           :sh07-core-raise-diagnostic! (fn [& args] (reset! raised args) (throw expected)))))]
      (is (identical? expected failure))
      (is (= [source rejected] @raised)))
    (let [failure (caught #(core/run-structural-request-for-test
                            request (assoc ops :sh07-core-execute! (fn [& _] {:status :invalid}))))]
      (is (= ["C6-VERIFY" :invalid] ((juxt :id :status) (ex-data failure)))))))

(deftest module-and-source-contract-drift-reject-before-binding-publication
  ;; Real current source bytes with isolated compiler result ports. No admission.
  (let [binding (b/sh07-core-current-binding!)
        ops (assoc (operations)
                   :c-backend-stage2-plan-emitter-source-rule! (fn [& _] {:emitter :unit})
                   :sh07-core-plan-identities (fn [_] (select-keys binding [:public-function-shapes])))]
    (doseq [plan [(assoc-in (:plan binding) [:module :profile] :hosted)
                  (assoc-in (:plan binding) [:module :effects] #{:io/read})
                  (assoc-in (:plan binding) [:source :sha256] "sha256:changed")]]
      (let [error (caught #(core/build-plan-binding!
                           (assoc ops :p15-s23-stage2-compiler-artifact-plan (fn [& _] plan))))]
        (is (= "C6-VERIFY" (:id (ex-data error))))
        (is (= [:sh07-core-module-contract] (:missing-fields (ex-data error))))))))

(deftest native-carriers-reject-before-runtime-callbacks
  (let [calls (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! calls inc) 7)
                 (equals [_] (swap! calls inc) false)
                 (toString [] (swap! calls inc) "opaque"))
        ops (assoc (operations) :p15-s23-stage2-runtime-execute-function (fn [& _] (swap! calls inc)))]
    (doseq [arguments [[opaque] {:opaque opaque}]]
      (let [error (caught #(core/execute! source 'unit arguments ops))]
        (is (= "C6-VERIFY" (:id (ex-data error))))))
    (is (zero? @calls))))

(deftest host-errors-and-interruption-retain-the-existing-boundary
  (let [ops (operations)
        fixture-ops (assoc ops :input (fn [field]
                                       (if (= field :*sh07-core-execution-binding*)
                                         {:plan :unit} ((:input ops) field))))]
    (doseq [error [(StackOverflowError. "unit") (OutOfMemoryError. "unit")
                   (AssertionError. "unit") (LinkageError. "unit") (Exception. "unit")]]
      (let [failure (caught #(core/execute! source 'unit []
                                            (assoc fixture-ops :p15-s23-stage2-runtime-execute-function
                                                   (fn [& _] (throw error)))))]
        (is (= "C6-VERIFY" (:id (ex-data failure))))
        (is (= (.getName (class error)) (:contained-host-error (ex-data failure))))))
    (let [error (ex-info "unit" {:id "UNIT"})]
      (is (identical? error (caught #(core/execute! source 'unit []
                                                   (assoc fixture-ops :p15-s23-stage2-runtime-execute-function
                                                          (fn [& _] (throw error))))))))
    (let [error (InterruptedException. "unit")
          observed (try
                     (core/execute! source 'unit []
                                    (assoc fixture-ops :p15-s23-stage2-runtime-execute-function
                                           (fn [& _] (throw error))))
                     (catch InterruptedException observed [observed (.isInterrupted (Thread/currentThread))])
                     (finally (Thread/interrupted)))]
      (is (identical? error (first observed)))
      (is (true? (second observed))))))

(def ^:private unit-artifact {:artifact-id "unit-core"})
(def ^:private unit-report {:status :passed :checks {:unit? true} :failed-checks []})
(defn- with-unit-verifier [f]
  ;; Real receipt qualification with an inert verifier; no Core admission claim.
  (with-redefs-fn {(seed-var 'sh07-core-artifact-verification*) (fn [& _] unit-report)} f))
(defn- with-context [f]
  (let [ctx (atom {:open? true :owner-thread-id (.getId (Thread/currentThread)) :phase :construction :epoch 0
                   :receipts [] :executions {} :reuses {} :check-catalogs {} :failed-report-executions 0})]
    (try (with-bindings {(seed-var '*sh07-proof-transaction-context*) ctx} (f ctx))
         (finally (swap! ctx assoc :open? false :receipts [])))))
(defn- verify-unit [] (b/sh07-core-artifact-verification unit-artifact))
(defn- assert-interposition-misses [interpose]
  (with-unit-verifier
    #(with-context
       (fn [ctx]
         (is (= unit-report (verify-unit)))
         (is (= unit-report (verify-unit)))
         (is (= 1 (get-in @ctx [:executions :sh07])))
         (is (= 1 (get-in @ctx [:reuses :sh07])))
         (interpose (fn [] (dotimes [_ 2] (is (= unit-report (verify-unit))))))
         (is (= 3 (get-in @ctx [:executions :sh07])))
         (is (= 1 (get-in @ctx [:reuses :sh07])))
         (is (= unit-report (verify-unit)))
         (is (= 2 (get-in @ctx [:reuses :sh07])))))))

(deftest every-stage-operation-and-compatibility-root-invalidates-receipts
  (let [private-stages (map #(ns-resolve 'gravity.checked-core %)
                           '[source-occurrence-context projected-source-mismatch lineage-with-semantic-trace request-from-verified-upstream])
        vars (set (concat (vals (ns-publics 'gravity.checked-core)) private-stages
                         (map seed-var projection-compatibility-names) (filter var? (vals (operations)))
                         (map seed-var '[checked-core-stage-operations sh07-core-source-path sh07-core-plan-identities
                                         sh07-core-build-binding! sh07-core-current-source-key sh07-core-current-binding!
                                         sh07-core-execute!])))]
    (doseq [v vars]
      (let [original @v]
        (assert-interposition-misses #(with-redefs-fn {v (fn [& args] (apply original args))} %))))))

(deftest configuration-cache-and-thread-binding-roots-invalidate-receipts
  (doseq [name '[checked-core-stage-defaults sh07-b9-handler-descriptor-keys sh07-b11-match-clause-keys sh07-b11-match-pattern-record-keys sh07-core-public-function-shapes sh07-core-source-relative-path
                sh07-core-development-binding sh07-core-cached-binding]]
    (let [v (seed-var name) original @v
          replacement (cond (map? original) (reduce-kv assoc {} original)
                            (string? original) (String. ^String original)
                            (instance? clojure.lang.Atom original) (atom @original)
                            :else (reify clojure.lang.IDeref (deref [_] @original)))]
      (is (not (identical? original replacement)))
      (assert-interposition-misses #(with-redefs-fn {v replacement} %))))
  (let [v #'core/execute! original @v dynamic? (.isDynamic v)]
    (try (.setDynamic v true)
         (assert-interposition-misses #(with-bindings {v original} (%)))
         (finally (.setDynamic v dynamic?)))))

(deftest scoped-execution-binding-is-an-explicit-value-epoch
  (let [v (seed-var '*sh07-core-execution-binding*)
        original-root (.getRawRoot ^clojure.lang.Var v)
        binding {:plan {:body :original} :source-content-hash "same" :plan-semantic-hash "same"}]
    (is (not (identical? binding (reduce-kv assoc {} binding))))
    (with-unit-verifier
      #(with-context
         (fn [ctx]
           (with-bindings {v binding}
             (is (= unit-report (verify-unit)))
             (is (= unit-report (verify-unit))))
           (with-bindings {v (reduce-kv assoc {} binding)} (is (= unit-report (verify-unit))))
           (is (= 1 (get-in @ctx [:executions :sh07])))
           (is (= 2 (get-in @ctx [:reuses :sh07])))
           (with-bindings {v (assoc-in binding [:plan :body] :changed)}
             (is (= unit-report (verify-unit))))
           (is (= 2 (get-in @ctx [:executions :sh07])))
           (with-bindings {v binding} (is (= unit-report (verify-unit))))
           (is (= 3 (get-in @ctx [:reuses :sh07]))))))
    (is (identical? original-root (.getRawRoot ^clojure.lang.Var v)))))

(deftest construction-roots-detect-the-new-runtime-inputs-before-owner-work
  (let [expected (set (concat (vals (ns-publics 'gravity.checked-core))
                              (map #(ns-resolve 'gravity.checked-core %) '[source-occurrence-context projected-source-mismatch lineage-with-semantic-trace request-from-verified-upstream])
                              (map seed-var projection-compatibility-names)
                              (filter var? (vals (operations)))
                              (map seed-var '[checked-core-stage-operations checked-core-stage-defaults checked-core-stage-input
                                              sh07-core-source-path sh07-core-plan-identities sh07-core-build-binding!
                                              sh07-core-current-source-key sh07-core-current-binding! sh07-core-execute!
                                              sh07-core-source-relative-path sh07-core-public-function-shapes sh07-b9-handler-descriptor-keys sh07-b11-match-clause-keys sh07-b11-match-pattern-record-keys
                                              sh07-core-development-binding sh07-core-cached-binding *sh07-core-execution-binding*])))
        downstream (atom 0)]
    ;; Isolated bookend only; the owning admission operations are not executed.
    (with-redefs-fn
      {(seed-var 'sh07-core-construction-phase) (constantly nil)
       (seed-var 'sh07-core-construction-catalog) (fn [& _] (swap! downstream inc) nil)}
      (fn []
        (let [roots ((seed-var 'sh07-core-construction-roots))
              facts {:source-path source :thread (.getId (Thread/currentThread)) :phase nil :roots roots}]
          (is (every? (set (map first roots)) expected))
          (doseq [v expected]
            (let [original @v
                  replacement (cond (map? original) (reduce-kv assoc {} original)
                                    (string? original) (String. ^String original)
                                    (instance? clojure.lang.Atom original) (atom @original)
                                    (instance? clojure.lang.IDeref original) (reify clojure.lang.IDeref (deref [_] @original))
                                    (nil? original) :unit-binding
                                    :else (fn [& args] (apply original args)))
                  error (caught #(with-redefs-fn {v replacement}
                                    (fn [] ((seed-var 'sh07-core-construction-check!) facts))))]
              (is (= :construction-dependency-drift (:reason (ex-data error))) (str (:name (meta v))))))
          (is (zero? @downstream)))))))
(def ^:private transfer-shared
  {:construction-order 0 :runtime-reachability :executed :transfer-policy :typed-error
   :result-policy :no-return :required-effect :error/throw
   :authenticated-sh06-artifact-id :owner :sh06-semantic-projection-id :semantic
   :type-legality :pending :effect-registry-legality :pending
   :effect-profile-capability-legality :pending :profile-error-lowering-legality :pending
   :ownership-legality :pending :safety-classification :pending})

(def ^:private transfer-core
  {:nodes [{:node-id :value :core-form :literal}
           {:node-id :throw :core-form :throw :children [:value] :resolved-binding-ids []
            :evaluation {:kind :value-then-transfer :order [{:index 0 :core-node-id :value}]}
            :attributes (assoc transfer-shared :value-child-index 0 :evaluation-order :value-then-transfer)}]
   :error-transfers [(assoc transfer-shared :ordinal 0 :core-node-id :throw :value-core-node-id :value
                           :evaluated-children [:value] :ordered-steps [:evaluate-value :transfer-error])]})

(deftest transfer-coherence-preserves-bijection-order-and-shared-facts
  (let [ops (operations)]
    (is (true? (core/error-transfers-coherent? transfer-core ops)))
    (is (true? (core/error-transfers-coherent? {:nodes [] :error-transfers []} ops)))
    (doseq [bad (concat
                [(assoc transfer-core :nodes nil) (update transfer-core :nodes seq)
                 (assoc transfer-core :error-transfers []) (update transfer-core :error-transfers seq)
                 (update transfer-core :nodes conj (second (:nodes transfer-core)))
                 (update transfer-core :error-transfers conj (first (:error-transfers transfer-core)))
                 (assoc-in transfer-core [:error-transfers 0 :ordinal] 1)
                 (assoc-in transfer-core [:error-transfers 0 :core-node-id] :missing)
                 (assoc-in transfer-core [:error-transfers 0 :value-core-node-id] :other)
                 (assoc-in transfer-core [:error-transfers 0 :evaluated-children] [])
                 (assoc-in transfer-core [:error-transfers 0 :ordered-steps] [:transfer-error :evaluate-value])
                 (assoc-in transfer-core [:nodes 1 :children] [])
                 (assoc-in transfer-core [:nodes 1 :resolved-binding-ids] [:unexpected])
                 (assoc-in transfer-core [:nodes 1 :evaluation :kind] :transfer-before-value)
                 (assoc-in transfer-core [:nodes 1 :evaluation :order] [])
                 (assoc-in transfer-core [:nodes 1 :attributes :value-child-index] 1)
                 (assoc-in transfer-core [:nodes 1 :attributes :evaluation-order] :transfer-before-value)]
                (map #(assoc-in transfer-core [:error-transfers 0 %] :tampered) (keys transfer-shared)))]
      (is (false? (core/error-transfers-coherent? bad ops)))
      (is (= (b/sh07-core-error-transfers-coherent? bad) (core/error-transfers-coherent? bad ops))))
    ;; Equality is the retained predicate boundary, not presence/legality proof.
    (let [without-facts (-> transfer-core
                            (update-in [:nodes 1 :attributes] #(apply dissoc % (keys transfer-shared)))
                            (update-in [:error-transfers 0] #(apply dissoc % (keys transfer-shared))))
          contract (:gravity/namespace-contract (meta #'core/error-transfers-coherent?))]
      (is (true? (core/error-transfers-coherent? without-facts ops)))
      (is (= :owner-validated-native-core-data (get-in contract [:public-api 'error-transfers-coherent? :input-boundary])))
      (is (false? (get-in contract [:public-api 'error-transfers-coherent? :checks-legality-fact-presence?]))))))

(deftest projection-diagnostic-retains-source-facts-and-unresolved-identity
  (let [ops (operations)
        parent {:artifact-id :authenticated-owner
                :namespace-analysis {:namespace 'unit :profile :meta :target :jvm}
                :gravity-resolution-boundary {:authenticated-resolution-request
                                              {:module {:source-revision-id :source-revision}
                                               :provenance {:actual-source-path "unit.gravity"}}}}
        reason :transfer-order
        failure (ex-data (caught #(core/projection-diagnostic! parent reason ops)))]
    (is (= failure (ex-data (caught #(b/sh07-core-projection-diagnostic! parent reason)))))
    (is (= ["C6-VERIFY" :error :core-lowering] ((juxt :rule :severity :stage) failure)))
    (is (= {:source "unit.gravity"} (:source-span failure)))
    (is (= [nil nil nil []] ((juxt :syntax-id :form-id :core-node-id :generated-origin-chain) failure)))
    (is (= ['unit :meta :jvm] ((juxt :namespace :profile :target) failure)))
    (is (= {:reason reason :rule-specific {:reason reason} :source-revision-id :source-revision
            :sh06-artifact-id :authenticated-owner :authenticated-sh06-artifact-id :authenticated-owner
            :fail-closed true} (:facts failure)))
    (is (= (b/reader-canonical-hash {:domain :gravity/sh07-projection-diagnostic-v16 :reason reason :sh06-artifact-id :authenticated-owner})
           (:diagnostic-id-request failure)))
    (is (not (contains? failure :diagnostic-id)))
    (is (not= (:diagnostic-id-request failure)
              (:diagnostic-id-request (ex-data (caught #(core/projection-diagnostic! parent :other ops))))))
    (is (not= (:diagnostic-id-request failure)
              (:diagnostic-id-request (ex-data (caught #(core/projection-diagnostic! (assoc parent :artifact-id :other) reason ops))))))
    (let [missing (ex-data (caught #(core/projection-diagnostic! {} reason ops)))]
      (is (= [nil nil nil] ((juxt :namespace :profile :target) missing)))
      (is (= {:source "<sh07-core-input>"} (:source-span missing))))))

(def ^:private handler-shared
  {:runtime-reachability :conditional :selection-policy :source-order
   :result-policy :handler-value :authenticated-sh06-artifact-id :owner
   :sh06-semantic-projection-id :semantic :type-coverage-legality :pending
   :result-type-join-legality :pending :effect-registry-legality :pending
   :effect-profile-capability-legality :pending :profile-error-lowering-legality :pending
   :ownership-legality :pending :safety-classification :pending})

(defn- handler-core []
  (let [handlers
        (mapv (fn [i]
                (merge handler-shared
                       {:ordinal i :clause-ordinal i :clause-count 2 :handler-child-index (inc i)
                        :core-node-id :try :protected-core-node-id :protected
                        :handler-core-node-id (keyword (str "handler" i))
                        :evaluation-owner-function-syntax-id :function
                        :catch-clause-form-id [:catch-form i] :catch-clause-syntax-id [:catch-syntax i]
                        :error-type-form-id [:type-form i] :error-type-syntax-id [:type-syntax i]
                        :error-type-binding-id [:type i] :catch-binding-form-id [:binding-form i]
                        :catch-binding-syntax-id [:binding-syntax i] :catch-binding-id [:catch i]
                        :catch-binding-scope-id [:scope i]
                        :candidate-error-transfers [{:ordinal 0 :core-node-id :throw}]
                        :catch-binding-use-syntax-ids [[:use-syntax i]]})) (range 2))
        descriptors (mapv #(select-keys % (input :sh07-b9-handler-descriptor-keys)) handlers)
        try-node {:node-id :try :core-form :try :children [:protected :handler0 :handler1]
                  :resolved-binding-ids [[:type 0] [:catch 0] [:type 1] [:catch 1]]
                  :evaluation {:kind :protected-then-ordered-typed-handler-candidates
                               :order [{:index 0 :core-node-id :protected}]}
                  :attributes (merge handler-shared
                                     {:protected-child-index 0 :handler-count 2 :handler-child-indexes [1 2]
                                      :handler-clauses descriptors
                                      :evaluation-order :protected-then-ordered-typed-handler-candidates})}]
    {:nodes [try-node {:node-id :protected :children [:throw :foreign-throw]}
             {:node-id :throw :children []} {:node-id :foreign-throw :children []}
             {:node-id :outside :children []}
             {:node-id :handler0 :children [:use0]} {:node-id :handler1 :children [:use1]}
             {:node-id :use0 :children []} {:node-id :use1 :children []}]
     :error-handlers handlers
     :error-transfers [{:ordinal 0 :core-node-id :throw :evaluation-owner-function-syntax-id :function}
                      {:ordinal 1 :core-node-id :foreign-throw :evaluation-owner-function-syntax-id :other-function}
                      {:ordinal 2 :core-node-id :outside :evaluation-owner-function-syntax-id :function}]
     :reference-uses [{:core-node-id :use0 :binding-id [:catch 0] :syntax-id [:use-syntax 0]}
                      {:core-node-id :use1 :binding-id [:catch 1] :syntax-id [:use-syntax 1]}
                      {:core-node-id :outside :binding-id [:catch 0] :syntax-id :outside-syntax}]}))

(deftest descendant-traversal-preserves-cycles-and-missing-node-refusal
  (let [ops (operations) nodes {:root {:children [:child :child]} :child {:children [:root]}}]
    (is (= #{:root :child} (core/descendant-node-ids nodes :root ops)))
    (is (= (core/descendant-node-ids nodes :root ops) (b/sh07-core-descendant-node-ids nodes :root)))
    (is (nil? (core/descendant-node-ids nodes :absent ops)))
    (is (nil? (core/descendant-node-ids (assoc nodes :child {:children [:missing]}) :root ops)))))

(deftest typed-handler-coherence-preserves-order-region-bindings-and-facts
  (let [ops (operations) core-data (handler-core)
        node-by-id (into {} (map (juxt :node-id identity)) (:nodes core-data))
        try-node (first (:nodes core-data))]
    (is (true? (core/error-handlers-coherent? core-data ops)))
    (is (= (core/error-handlers-coherent? core-data ops) (b/sh07-core-error-handlers-coherent? core-data)))
    (is (true? (core/handler-group-coherent? node-by-id (:error-transfers core-data)
                                            (:reference-uses core-data) try-node
                                            (vec (reverse (:error-handlers core-data))) ops)))
    (is (true? (core/error-handlers-coherent? {:nodes [] :error-handlers [] :error-transfers [] :reference-uses []} ops)))
    (doseq [bad (concat
                 [(assoc core-data :nodes nil) (update core-data :nodes seq)
                  (assoc core-data :error-handlers []) (update core-data :error-handlers seq)
                  (assoc core-data :error-transfers nil) (assoc core-data :reference-uses nil)
                  (update core-data :nodes conj try-node)
                  (update core-data :error-handlers #(vec (reverse %)))
                  (assoc-in core-data [:nodes 0 :children] [:protected :handler1 :handler0])
                  (assoc-in core-data [:nodes 0 :resolved-binding-ids] [])
                  (assoc-in core-data [:nodes 0 :evaluation :kind] :handlers-before-protected)
                  (assoc-in core-data [:nodes 0 :evaluation :order] [])
                  (assoc-in core-data [:nodes 0 :attributes :protected-child-index] 1)
                  (assoc-in core-data [:nodes 0 :attributes :handler-count] 1)
                  (assoc-in core-data [:nodes 0 :attributes :handler-child-indexes] [2 1])
                  (assoc-in core-data [:nodes 0 :attributes :handler-clauses] [])
                  (assoc-in core-data [:nodes 0 :attributes :evaluation-order] :unordered)
                  (assoc-in core-data [:error-handlers 0 :clause-count] 1)
                  (assoc-in core-data [:error-handlers 0 :clause-ordinal] 1)
                  (assoc-in core-data [:error-handlers 0 :handler-child-index] 2)
                  (assoc-in core-data [:error-handlers 0 :core-node-id] :missing)
                  (assoc-in core-data [:error-handlers 0 :protected-core-node-id] :outside)
                  (assoc-in core-data [:error-handlers 0 :candidate-error-transfers] [])
                  (assoc-in core-data [:error-handlers 0 :catch-binding-use-syntax-ids] [:outside-syntax])
                  (assoc-in core-data [:error-handlers 1 :handler-core-node-id] :handler0)
                  (assoc-in core-data [:error-handlers 1 :catch-binding-id] [:catch 0])
                  (assoc-in core-data [:error-handlers 1 :catch-binding-scope-id] [:scope 0])
                  (assoc-in core-data [:error-transfers 1 :evaluation-owner-function-syntax-id] :function)
                  (assoc-in core-data [:reference-uses 0 :binding-id] [:catch 1])]
                 (for [key (keys handler-shared)] (assoc-in core-data [:error-handlers 0 key] :changed)))]
      (is (false? (core/error-handlers-coherent? bad ops)))
      (is (= (core/error-handlers-coherent? bad ops) (b/sh07-core-error-handlers-coherent? bad))))
    ;; Shared absence and pending facts are agreement, not proof of legality.
    (let [without-facts (-> core-data (update-in [:nodes 0 :attributes] #(apply dissoc % (keys handler-shared)))
                            (update :error-handlers #(mapv (fn [handler] (apply dissoc handler (keys handler-shared))) %)))]
      (is (true? (core/error-handlers-coherent? without-facts ops))))
    (let [contract (:gravity/namespace-contract (meta #'core/error-handlers-coherent?))]
      (doseq [name '[descendant-node-ids handler-group-coherent? error-handlers-coherent?]]
        (is (= :owner-validated-native-core-data (get-in contract [:public-api name :input-boundary])))
        (is (false? (get-in contract [:public-api name :checks-legality-fact-presence?])))))))

(deftest handler-operations-and-descriptor-configuration-remain-explicit
  (let [ops (operations) core-data (handler-core) calls (atom [])
        node-by-id (into {} (map (juxt :node-id identity)) (:nodes core-data))
        group (:sh07-b9-handler-group-coherent? ops)
        descendant (:sh07-core-descendant-node-ids ops)]
    (is (true? (core/error-handlers-coherent?
                 core-data (assoc ops :sh07-b9-handler-group-coherent?
                                  (fn [& args] (swap! calls conj :group) (apply group args))))))
    (is (= [:group] @calls))
    (reset! calls [])
    (is (true? (core/handler-group-coherent?
                 node-by-id (:error-transfers core-data) (:reference-uses core-data)
                 (first (:nodes core-data)) (:error-handlers core-data)
                 (assoc ops :sh07-core-descendant-node-ids
                        (fn [& args] (swap! calls conj (second args)) (apply descendant args))))))
    (is (= [:protected :handler0 :handler1] @calls))
    (is (false? (core/handler-group-coherent?
                  node-by-id (:error-transfers core-data) (:reference-uses core-data)
                  (first (:nodes core-data)) (:error-handlers core-data)
                  (assoc ops :input (fn [field]
                                      (if (= field :sh07-b9-handler-descriptor-keys)
                                        (vec (rest ((:input ops) field))) ((:input ops) field)))))))))

(defn- match-unit-id [value] (b/reader-canonical-hash {:match-structure-unit value}))
(defn- match-unit-guard []
  {:rule-version :sh07-root-map-guard-v1 :source-child-index 3
   :form-id (match-unit-id :guard-form) :syntax-id (match-unit-id :guard-syntax)
   :child-index 3 :conditional-region {:kind :match-guard :match-syntax-id (match-unit-id :match-syntax) :clause-ordinal 0}
   :evaluation :once-after-structural-success :on-pattern-failure :next-candidate
   :on-true :branch-body :on-false :next-candidate :non-boolean-type-effect-legality :pending-later-legality})

(defn- match-unit-core []
  (let [id match-unit-id owner (id :owner) semantic (id :semantic) guard (match-unit-guard)
        common {:authenticated-sh06-artifact-id owner :sh06-semantic-projection-id semantic}
        pattern-base (merge common {:core-node-id (id :match) :local-ordinal 0 :root-local-ordinal 0
                                    :parent-local-ordinal nil :parent-ordinal nil :depth 0 :path []
                                    :vector-width nil :pattern-binding-id nil :pattern-binding-scope-id nil
                                    :pattern-binding-use-syntax-ids []})
        patterns [(merge pattern-base {:ordinal 0 :clause-ordinal 0 :root-ordinal 0 :pattern-kind :map
                                       :pattern-form-id (id :map-form) :pattern-syntax-id (id :map-syntax)
                                       :pattern-value {:value 'item :when true} :guard (core/match-guard-source guard (operations))})
                  (merge pattern-base {:ordinal 1 :clause-ordinal 0 :local-ordinal 1 :root-ordinal 0
                                       :parent-ordinal 0 :parent-local-ordinal 0 :depth 1 :path [1]
                                       :pattern-kind :binding :pattern-value 'item
                                       :pattern-form-id (id :binding-form) :pattern-syntax-id (id :binding-syntax)
                                       :pattern-binding-id (id :binding) :pattern-binding-scope-id (id :binding-scope)
                                       :pattern-binding-use-syntax-ids [(id :body-use-syntax) (id :guard-use-syntax)]})
                  (merge pattern-base {:ordinal 2 :clause-ordinal 1 :root-ordinal 2 :pattern-kind :wildcard
                                       :pattern-value '_ :pattern-form-id (id :wildcard-form) :pattern-syntax-id (id :wildcard-syntax)})]
        records (mapv (fn [i pattern]
                        (merge common (select-keys pattern [:pattern-kind :pattern-value :pattern-form-id :pattern-syntax-id
                                                           :pattern-binding-id :pattern-binding-scope-id])
                               {:ordinal i :clause-ordinal i :clause-count 2 :branch-child-index (inc i)
                                :core-node-id (id :match) :scrutinee-core-node-id (id :scrutinee)
                                :branch-core-node-id (id [:branch i]) :branch-form-id (id [:branch-form i])
                                :branch-syntax-id (id [:branch-syntax i])
                                :pattern-binding-ids (if (zero? i) [(id :binding)] []) :pattern-binding-use-syntax-ids []
                                :scrutinee-evaluation :evaluate-scrutinee-once
                                :branch-evaluation :conditionally-evaluate-selected-branch
                                :conditional-region {:kind :match-branch :match-syntax-id (id :match-syntax) :clause-ordinal i}}
                               (when (zero? i) {:guard (assoc guard :node-id (id :guard))})))
                      (range 2) [(first patterns) (last patterns)])
        attrs (merge common {:scrutinee-child-index 0 :branch-count 2 :branch-child-indexes [1 2]
                             :branch-clauses (mapv #(core/match-clause % (operations)) records)
                             :guard-child-indexes [3] :guard-rule-version :sh07-root-map-guard-v1
                             :evaluation-order :scrutinee-then-source-ordered-pattern-candidates
                             :runtime-reachability :not-asserted-by-sh07-b11 :selection-policy :source-ordered-pattern-candidates
                             :result-type-join :pending-sh08 :exhaustiveness :pending-sh08})
        leaf (fn [name children] {:node-id (id name) :core-form :literal :children (mapv id children)})]
    {:nodes [{:node-id (id :match) :core-form :match :children (mapv id [:scrutinee [:branch 0] [:branch 1] :guard])
              :resolved-binding-ids [(id :binding)] :attributes attrs :lowering-rule-version :sh07-root-map-guard-v1
              :evaluation {:kind :scrutinee-then-source-ordered-pattern-candidates
                           :order [{:index 0 :core-node-id (id :scrutinee)}]}}
             (leaf :scrutinee []) (leaf [:branch 0] [:body-use]) (leaf [:branch 1] [])
             (assoc (leaf :guard [:guard-use]) :source (select-keys guard [:form-id :syntax-id])
                    :evaluation {:region (:conditional-region guard)})
             (leaf :body-use []) (leaf :guard-use [])]
     :match-branch-records records :match-pattern-records patterns
     :match-decision-skeletons [(merge common {:core-node-id (id :match) :scrutinee-core-node-id (id :scrutinee)
                                             :branch-core-node-ids (mapv #(id [:branch %]) (range 2))
                                             :selection-policy :source-ordered-pattern-candidates
                                             :runtime-reachability :not-asserted-by-sh07-b11
                                             :result-type-join :pending-sh08 :exhaustiveness :pending-sh08})]
     :reference-uses [{:core-node-id (id :body-use) :binding-id (id :binding) :syntax-id (id :body-use-syntax)}
                      {:core-node-id (id :guard-use) :binding-id (id :binding) :syntax-id (id :guard-use-syntax)}]}))

(deftest match-guard-projections-preserve-exact-source-and-control-fields
  (let [ops (operations) guard (match-unit-guard) source (core/match-guard-source guard ops)]
    (is (= source (b/sh07-b11-guard-source guard)))
    (is (= #{:rule-version :source-child-index :form-id :syntax-id} (set (keys source))))
    (is (nil? (core/match-guard-source nil ops)))
    (is (true? (core/match-guard-source-valid? source ops)))
    (is (true? (core/match-guard-valid? guard 0 ops)))
    (doseq [bad [(assoc source :extra true) (dissoc source :form-id)
                 (assoc source :source-child-index 0) (assoc source :source-child-index 2)
                 (assoc source :source-child-index -1) (assoc source :source-child-index 1.5)
                 (assoc source :rule-version :other) (assoc source :syntax-id :not-a-sha256)]]
      (is (false? (core/match-guard-source-valid? bad ops)))
      (is (= (core/match-guard-source-valid? bad ops) (b/sh07-b11-guard-source-valid? bad))))
    (doseq [bad [(assoc guard :child-index 0) (assoc guard :on-false :branch-body)
                 (assoc guard :on-true :next-candidate) (assoc guard :evaluation :before-pattern)
                 (assoc guard :on-pattern-failure :branch-body) (assoc guard :extra true)
                 (assoc guard :non-boolean-type-effect-legality :proven)
                 (assoc-in guard [:conditional-region :clause-ordinal] 1)
                 (assoc-in guard [:conditional-region :match-syntax-id] :bad)]]
      (is (false? (core/match-guard-valid? bad 0 ops)))
      (is (= (core/match-guard-valid? bad 0 ops) (b/sh07-b11-guard-valid? bad 0))))
    (is (false? (core/match-guard-source-valid? source (assoc ops :p15-s23-sh02-sha256-id? (constantly false)))))))

(deftest match-branch-clause-and-pattern-graph-preserve-guard-slot-boundaries
  (let [ops (operations) data (match-unit-core) record (first (:match-branch-records data))
        nodes (into {} (map (juxt :node-id identity)) (:nodes data))
        patterns (:match-pattern-records data) clause (core/match-clause record ops)]
    (is (= (set (map match-unit-id [[:branch 0] :body-use :guard :guard-use])) (core/match-branch-tree nodes record ops)))
    (is (= (core/match-branch-tree nodes record ops) (b/sh07-b11-branch-tree nodes record)))
    (is (= (set (map match-unit-id [[:branch 0] :body-use])) (core/match-branch-tree nodes (dissoc record :guard) ops)))
    (is (= clause (b/sh07-b11-clause record)))
    (is (= (dissoc (:guard record) :node-id) (:guard clause)))
    (is (nil? (:guard (core/match-clause (assoc record :guard nil) ops))))
    (is (true? (core/match-pattern-graph-complete? patterns ops)))
    (is (true? (core/match-pattern-graph-complete? [] ops)))
    (doseq [bad [(vec (remove #(= 1 (:ordinal %)) patterns))
                 (conj patterns (second patterns))
                 (assoc-in patterns [1 :path] [0]) (assoc-in patterns [1 :path] [3])
                 (assoc-in patterns [1 :path] [5]) (assoc-in patterns [0 :pattern-value] nil)
                 (conj patterns (assoc (last patterns) :ordinal 3))]]
      (is (false? (core/match-pattern-graph-complete? bad ops)))
      (is (= (core/match-pattern-graph-complete? bad ops) (b/sh07-b11-match-pattern-graph-complete? bad))))
    ;; Child-slot coverage alone does not establish record shape or graph legality.
    (is (true? (core/match-pattern-graph-complete? [{:pattern-kind :literal}] ops)))
    (is (not= clause (core/match-clause record
                         (assoc ops :input (fn [field] (if (= field :sh07-b11-match-clause-keys)
                                                         [] ((:input ops) field)))))))))

(deftest match-structural-coherence-preserves-guard-region-bindings-and-order
  (let [ops (operations) data (match-unit-core)]
    (is (true? (core/match-products-coherent? data ops)))
    (is (= (core/match-products-coherent? data ops) (b/sh07-core-match-products-coherent? data)))
    (doseq [bad (concat
                 [(assoc data :nodes nil) (update data :nodes seq)
                  (update data :nodes conj (first (:nodes data)))
                  (assoc data :match-branch-records []) (update data :match-branch-records seq)
                  (assoc data :match-pattern-records []) (assoc data :match-decision-skeletons [])
                  (assoc data :reference-uses nil)
                  (assoc-in data [:match-branch-records 0 :ordinal] 1)
                  (assoc-in data [:match-branch-records 0 :clause-count] 1)
                  (assoc-in data [:match-branch-records 0 :guard :on-false] :branch-body)
                  (assoc-in data [:match-branch-records 0 :guard :child-index] 2)
                  (assoc-in data [:match-branch-records 0 :guard :node-id] (match-unit-id :absent))
                  (assoc-in data [:match-branch-records 0 :pattern-binding-ids] [])
                  (assoc-in data [:match-branch-records 0 :branch-evaluation] :always)
                  (assoc-in data [:match-branch-records 0 :scrutinee-evaluation] :twice)
                  (assoc-in data [:match-branch-records 0 :authenticated-sh06-artifact-id] (match-unit-id :other))
                  (assoc-in data [:match-branch-records 0 :conditional-region :clause-ordinal] 1)
                  (assoc-in data [:nodes 0 :children] [])
                  (assoc-in data [:nodes 0 :resolved-binding-ids] [])
                  (assoc-in data [:nodes 0 :attributes :guard-child-indexes] [2])
                  (assoc-in data [:nodes 0 :attributes :exhaustiveness] :proven)
                  (assoc-in data [:nodes 0 :attributes :runtime-reachability] :executed)
                  (assoc-in data [:nodes 0 :evaluation :order] [])
                  (assoc-in data [:nodes 4 :evaluation :region :clause-ordinal] 1)
                  (assoc-in data [:nodes 4 :source :form-id] (match-unit-id :different))
                  (assoc-in data [:reference-uses 1 :binding-id] (match-unit-id :different))
                  (assoc-in data [:match-decision-skeletons 0 :branch-core-node-ids] [])]
                 (for [[field value] [[:ordinal 2] [:local-ordinal 0] [:root-ordinal 1] [:depth 2]
                                      [:path [3]] [:pattern-form-id :bad] [:pattern-kind :unknown]
                                      [:pattern-binding-id nil] [:pattern-binding-scope-id nil]
                                      [:pattern-binding-use-syntax-ids []] [:extra :forbidden]]]
                   (assoc-in data [:match-pattern-records 1 field] value)))]
      (is (false? (core/match-products-coherent? bad ops)))
      (is (= (core/match-products-coherent? bad ops) (b/sh07-core-match-products-coherent? bad))))
    (let [calls (atom 0)]
      (is (false? (core/match-products-coherent? data
                         (assoc ops :sh07-b11-match-group-coherent? (fn [& _] (swap! calls inc) false)))))
      (is (= 1 @calls)))
    (let [nodes (into {} (map (juxt :node-id identity)) (:nodes data))
          args [nodes (:reference-uses data) (first (:nodes data)) (:match-branch-records data)
                (first (:match-decision-skeletons data)) (:match-pattern-records data)]]
      (is (true? (apply core/match-group-coherent? (conj args ops))))
      (doseq [operation [:sh07-b11-guard-valid? :sh07-b11-match-pattern-graph-complete? :sh07-b11-match-pattern-record-coherent?]]
        (is (false? (apply core/match-group-coherent? (conj args (assoc ops operation (constantly false))))))))))
