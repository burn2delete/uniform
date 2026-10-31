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
    sh07-core-resolve-digest-preimage! sh07-core-digest-requests sh07-core-resolve-result])

(deftest namespace-contract-and-operation-inventories-agree
  (let [publics (ns-publics 'gravity.checked-core)
        contract (:gravity/namespace-contract (meta #'core/execute!))]
    (is (= 46 (count publics)))
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
  (doseq [name '[checked-core-stage-defaults sh07-core-public-function-shapes sh07-core-source-relative-path
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
                                              sh07-core-source-relative-path sh07-core-public-function-shapes
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
