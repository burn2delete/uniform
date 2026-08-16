(ns gravity.c6-core-lowering
  "Hosted Stage0 C6 AST/core-lowering engine.\n\n   This namespace owns only the compatibility lowering engine and artifact\n   projection. Source acquisition, SH06 authentication, and bootstrap wrappers\n   remain in gravity.bootstrap; this leaf is not canonical SH07 authority."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})

(def ^:private operation-keys
  #{:fail! :source-span :c4-artifact-id :form-effect :ns-form?
    :core-forms :lowering-gap-forms :known-source-profiles :supported-targets
    :c6-lowering-diagnostic-ids :c6-lowering-governing-document
    :c6-lowering-rejected-designs :c6-lowering-override-diagnostics
    :c6-domain-boundary-operators :c6-core-node-forms
    :c6-lowering-source-overrides :c6-lowering-message
    :c6-lowering-fail! :c6-lowering-validate-overrides!
    :c6-node-id :c6-core-node :c6-lower-children :c6-eval-order
    :c6-form->core-form :c6-lower-form :c6-core-child-nodes
    :c6-flatten-core :c6-domain-boundary-records :c6-surface-to-core-map
    :c6-desugaring-trace :c6-evaluation-order-records
    :c6-core-verifier-report :c6-rule-invalidation-record
    :c6-lowering-capability-proof :c6-lowering-validate!})

(def ^:private function-operation-keys
  (disj operation-keys :core-forms :lowering-gap-forms :known-source-profiles
        :supported-targets :c6-lowering-diagnostic-ids
        :c6-lowering-governing-document :c6-lowering-rejected-designs
        :c6-lowering-override-diagnostics :c6-domain-boundary-operators
        :c6-core-node-forms))

(defn- valid-keyword-set? [value]
  (and (set? value) (seq value) (every? keyword? value)))
(defn- valid-string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- valid-map-of-keywords-to-strings? [value]
  (and (map? value) (every? (fn [[k v]] (and (keyword? k) (string? v))) value)))
(defn- valid-symbol-set? [value]
  (and (set? value) (every? symbol? value)))
(defn- valid-symbol-or-keyword-set? [value]
  (and (set? value) (seq value)
       (every? #(or (symbol? %) (keyword? %)) value)))
(def ^:private namespace-contract
  {:namespace 'gravity.c6-core-lowering
   :contract-boundary :hosted-stage0-c6-core-lowering-engine
   :public-api :bootstrap-compatible-c6-vars
   :leaf-only-api ['c6-lowering-artifact]
   :owns [:hosted-stage0-c6-lowering-algorithm
          :hosted-stage0-c6-artifact-projection]
   :compatibility-only? true
   :canonical-sh07-authority? false
   :authority-boundary :gravity.bootstrap-sh06-adapter
   :dependency-direction {:forbids ['gravity.bootstrap 'gravity.diagnostics]
                          :requires ['clojure.set 'gravity.digest]}
   :does-not-own [:source-acquisition :sh06-authentication :canonical-sh07
                  :type-checking :effect-checking :ownership-checking
                  :safety-analysis :mir-construction :target-lowering
                  :proof-authority :equivalence :self-hosting :release]
   :operation-interposition {:partial-overrides? true
                             :unknown-keys-rejected? true
                             :accepted-keys operation-keys
                             :bootstrap-wrapper-arities? true}})
(declare public-api)
(defn c6-engine-contract []
  (assoc namespace-contract :public-api public-api))

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [source-path form-index]
  {:source source-path :form-index form-index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(defn- op-fn [key fallback]
  (or (get *operations* key) fallback))
(defn- op-value [key fallback]
  (or (get *operations* key) fallback))
(defn- invoke-op [key fallback & args]
  (apply (op-fn key fallback) args))
(defn- fail! [id message data]
  ((op-fn :fail! default-fail!) id message data))
(defn- source-span [path index]
  ((op-fn :source-span default-source-span) path index))
(defn- c4-artifact-id [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- form-effect [form]
  ((op-fn :form-effect (fn [_] #{})) form))
(defn- ns-form? [form]
  ((op-fn :ns-form? #(and (seq? %) (= 'ns (first %)))) form))

(def ^:private core-forms
  '#{quote if do let fn loop recur def var set! try throw match})
(def ^:private lowering-gap-forms
  '#{defn when -> cond case with-open with-region defmacro defschema
     defworkflow defagent ui query ai-form})
(def ^:private known-source-profiles
  #{:core :hardware :firmware :kernel :native :hosted :distributed :ai
    :meta :gpu :formal})
(def ^:private supported-targets #{:jvm})

(defn- valid-operation-map! [operations]
  (when-not (map? operations)
    (throw (ex-info "C6 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        non-functions (seq (for [[k v] (select-keys operations function-operation-keys)
                                 :when (not (fn? v))] k))]
    (when unknown
      (throw (ex-info "C6 operation map contains unknown keys"
                      {:unknown-keys (vec unknown) :allowed-keys operation-keys})))
    (when non-functions
      (throw (ex-info "C6 function operation values must be callable"
                      {:non-function-keys (vec non-functions)})))
    (doseq [[k valid? expected]
            [[:core-forms valid-symbol-set? :symbol-set]
             [:lowering-gap-forms valid-symbol-set? :symbol-set]
             [:known-source-profiles valid-keyword-set? :non-empty-keyword-set]
             [:supported-targets valid-keyword-set? :non-empty-keyword-set]
             [:c6-lowering-diagnostic-ids valid-string-vector? :non-empty-string-vector]
             [:c6-lowering-governing-document #(and (string? %) (seq %)) :non-empty-string]
             [:c6-lowering-rejected-designs #(and (vector? %) (every? map? %)) :vector-of-maps]
             [:c6-lowering-override-diagnostics valid-map-of-keywords-to-strings? :map-of-keywords-to-strings]
             [:c6-domain-boundary-operators valid-symbol-set? :symbol-set]
             [:c6-core-node-forms valid-symbol-or-keyword-set? :symbol-or-keyword-set]]
            :when (and (contains? operations k) (not (valid? (get operations k))))]
      (throw (ex-info "C6 scalar operation has an invalid shape"
                      {:key k :expected expected :actual (get operations k)}))))
  operations)

(defn with-operations [operations thunk]
  (valid-operation-map! operations)
  (binding [*operations* (merge *operations* operations)] (thunk)))

(def c6-lowering-diagnostic-ids
  ["C6-LOWERING-GAP"
   "C6-CORE-SHAPE"
   "C6-EVAL-ORDER"
   "C6-ORIGIN"
   "C6-EFFECT-DROP"
   "C6-UNSAFE-DROP"
   "C6-DOMAIN-BOUNDARY"
   "C6-VERIFY"])

(def c6-lowering-governing-document
  "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md")

(def c6-lowering-rejected-designs
  [{:diagnostic "C6-LOWERING-GAP"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-lowering-gap.gravity"
    :rejected-design :surface-form-bypasses-core}
   {:diagnostic "C6-CORE-SHAPE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-core-shape.gravity"
    :rejected-design :malformed-core-node}
   {:diagnostic "C6-EVAL-ORDER"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-eval-order.gravity"
    :rejected-design :evaluation-order-lost}
   {:diagnostic "C6-ORIGIN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-origin.gravity"
    :rejected-design :introduced-form-without-origin}
   {:diagnostic "C6-EFFECT-DROP"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-effect-drop.gravity"
    :rejected-design :effect-or-capability-erased}
   {:diagnostic "C6-UNSAFE-DROP"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-unsafe-drop.gravity"
    :rejected-design :unsafe-metadata-erased}
   {:diagnostic "C6-DOMAIN-BOUNDARY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-domain-boundary.gravity"
    :rejected-design :malformed-domain-boundary}
   {:diagnostic "C6-VERIFY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-verify.gravity"
    :rejected-design :core-verifier-failure}])

(def c6-lowering-override-diagnostics
  {:gap "C6-LOWERING-GAP"
   :core-shape "C6-CORE-SHAPE"
   :eval-order "C6-EVAL-ORDER"
   :origin "C6-ORIGIN"
   :effect-drop "C6-EFFECT-DROP"
   :unsafe-drop "C6-UNSAFE-DROP"
   :domain-boundary "C6-DOMAIN-BOUNDARY"
   :verify "C6-VERIFY"})

(def c6-domain-boundary-operators
  '#{defschema defworkflow defagent ui query ai-form})

(def c6-core-node-forms
  (set/union (op-value :core-forms core-forms)
             #{:call :literal :symbol :declared-primitive}))

(defn c6-lowering-source-overrides
  [module]
  (get-in module [:metadata :compiler :c6-lowering] {}))

(defn c6-lowering-message
  [id]
  (case id
    "C6-LOWERING-GAP" "surface form cannot lower to core or a declared domain boundary"
    "C6-CORE-SHAPE" "core node is malformed"
    "C6-EVAL-ORDER" "lowering lost required evaluation-order facts"
    "C6-ORIGIN" "introduced core form lacks source or generated-origin links"
    "C6-EFFECT-DROP" "lowering erased effect or capability declarations"
    "C6-UNSAFE-DROP" "lowering erased unsafe metadata"
    "C6-DOMAIN-BOUNDARY" "domain IR boundary record is malformed"
    "C6-VERIFY" "core verifier rejected the lowered artifact"
    "AST and core lowering failed"))

(defn c6-lowering-fail!
  [id source-path subject extra]
  (fail! id
         ((op-fn :c6-lowering-message c6-lowering-message) id)
         (merge {:source-span (or (:source-span subject)
                                  (:span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :c6-ast-core-lowering
                 :stage :core-lowering
                 :document-id "C6"
                 :expected-document
                 (op-value :c6-lowering-governing-document
                           c6-lowering-governing-document)
                 :syntax-id (:syntax-id subject)
                 :core-node-id (:core-node-id subject)
                 :generated-origin-chain (:generated-origin subject)
                 :lowering-rule (:lowering-rule subject)
                 :active-profile (:profile subject)
                 :target (:target subject)
                 :remediation "Lower expanded and resolved syntax into verified core nodes or declared domain IR boundary records while preserving source provenance, evaluation order, effects, capabilities, unsafe metadata, profile, and target facts."}
                extra)))

(defn c6-lowering-validate-overrides!
  [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get (op-value :c6-lowering-override-diagnostics
                                 c6-lowering-override-diagnostics)
                       fail-kind)]
      ((op-fn :c6-lowering-fail! c6-lowering-fail!) id source-path
                         {:source-span (source-span source-path 0)
                          :syntax-id "fixture-override"
                          :profile (:profile module)
                          :target (:target module)
                          :lowering-rule fail-kind}
                         {:missing-fields [fail-kind]}))))

(defn c6-node-id
  [counter]
  (str "c6-core-" (let [id @counter] (swap! counter inc) id)))

(defn c6-core-node
  [node-id form syntax module data]
  (let [source {:syntax-id (:syntax-id syntax)
                :span (:span syntax)
                :origin-chain (vec (concat (when (:origin syntax)
                                             [{:kind (:origin syntax)}])
                                           (:generated-origin syntax)))}
        surface-form (:surface-form data)
        unsafe-metadata (when (= 'unsafe (and (seq? surface-form)
                                              (first surface-form)))
                          {:unsafe-island :declared
                           :safety-outcome :pending-safe6})]
    (merge {:artifact :gravity/core-node
            :node-id node-id
            :form form
            :children {}
            :source source
            :binding-context :namespace-root
            :profile (:profile module)
            :target (:target module)
            :metadata (:metadata syntax)
            :facts {:resolved-bindings :pending-c5-binding-table}
            :effects ((op-fn :form-effect form-effect) (:form syntax))
            :capabilities (:capabilities module)
            :unsafe-metadata unsafe-metadata
            :generated? (boolean (seq (:generated-origin syntax)))}
           data)))

(declare c6-lower-form)

(defn c6-lower-children
  [counter module syntax forms]
  (mapv #(invoke-op :c6-lower-form c6-lower-form counter module syntax %) forms))

(defn c6-eval-order
  [form child-count]
  (case form
    quote []
    if [:condition :then-or-else]
    do (mapv (fn [idx] [:expr idx]) (range child-count))
    let [:bindings-left-to-right :body-left-to-right]
    fn [:call-arguments-left-to-right]
    loop [:loop-bindings-left-to-right :body-left-to-right]
    recur [:arguments-left-to-right]
    def [:initializer]
    var []
    set! [:value]
    try [:body :matching-handler]
    throw [:value]
    match [:scrutinee :selected-clause]
    :call [:operator :arguments-left-to-right]
    :declared-primitive [:arguments-left-to-right]
    []))

(defn c6-form->core-form
  [form]
  (cond
    (not (seq? form)) (if (symbol? form) :symbol :literal)
    (contains? (op-value :core-forms core-forms) (first form)) (first form)
    (= 'unsafe (first form)) :declared-primitive
    :else :call))

(defn c6-lower-form
  [counter module syntax form]
  (let [core-form (invoke-op :c6-form->core-form c6-form->core-form form)
        node-id (invoke-op :c6-node-id c6-node-id counter)]
    (cond
      (and (seq? form)
           (contains? (op-value :c6-domain-boundary-operators
                                c6-domain-boundary-operators)
                      (first form)))
      nil

      (and (seq? form) (= :call core-form)
           (contains? (op-value :lowering-gap-forms lowering-gap-forms)
                      (first form)))
      ((op-fn :c6-lowering-fail! c6-lowering-fail!)
       "C6-LOWERING-GAP" (:source-path module) syntax
       {:lowering-rule (first form)})

      (seq? form)
      (let [children (case core-form
                       quote []
                       if (invoke-op :c6-lower-children c6-lower-children counter module syntax (rest form))
                       do (invoke-op :c6-lower-children c6-lower-children counter module syntax (rest form))
                       let (let [[_ bindings & body] form
                                 binding-children (mapv (fn [[name expr]]
                                                          {:name name
                                                           :initializer
                                                           (invoke-op :c6-lower-form c6-lower-form counter module syntax expr)})
                                                        (partition 2 bindings))
                                 body-children (invoke-op :c6-lower-children c6-lower-children counter module syntax body)]
                             {:bindings binding-children
                              :body body-children})
                       fn (let [[_ params & body] form]
                            {:params params
                             :body (invoke-op :c6-lower-children c6-lower-children counter module syntax body)})
                       loop (let [[_ bindings & body] form]
                              {:bindings (mapv (fn [[name expr]]
                                                 {:name name
                                                  :initializer
                                                  (invoke-op :c6-lower-form c6-lower-form counter module syntax expr)})
                                               (partition 2 bindings))
                               :body (invoke-op :c6-lower-children c6-lower-children counter module syntax body)})
                       recur {:arguments (invoke-op :c6-lower-children c6-lower-children counter module syntax (rest form))}
                       def (let [[_ name value] form]
                             {:name name
                              :value (invoke-op :c6-lower-form c6-lower-form counter module syntax value)})
                       var {:name (second form)}
                       set! (let [[_ target value] form]
                              {:target target
                               :value (invoke-op :c6-lower-form c6-lower-form counter module syntax value)})
                       try (let [[_ body & handlers] form]
                             {:body (invoke-op :c6-lower-form c6-lower-form counter module syntax body)
                              :handlers handlers})
                       throw {:value (invoke-op :c6-lower-form c6-lower-form counter module syntax (second form))}
                       match (let [[_ value & clauses] form]
                               {:scrutinee (invoke-op :c6-lower-form c6-lower-form counter module syntax value)
                                :clauses (mapv (fn [[pattern expr]]
                                                 {:pattern pattern
                                                  :body (invoke-op :c6-lower-form c6-lower-form counter module syntax expr)})
                                               (partition 2 clauses))})
                       :declared-primitive
                       {:operator (first form)
                        :arguments (invoke-op :c6-lower-children c6-lower-children counter module syntax (rest form))}
                       :call {:operator (first form)
                              :arguments (invoke-op :c6-lower-children c6-lower-children counter module syntax (rest form))})]
        (invoke-op :c6-core-node c6-core-node node-id core-form syntax module
                      {:surface-form form
                       :children children
                       :evaluation-order (invoke-op :c6-eval-order c6-eval-order core-form
                                                        (if (map? children)
                                                          (count children)
                                                          (count children)))
                       :lowering-rule (if (= :call core-form)
                                        :declared-call
                                        core-form)}))

      :else
      (invoke-op :c6-core-node c6-core-node node-id core-form syntax module
                    {:surface-form form
                     :children {}
                     :evaluation-order []
                     :lowering-rule core-form
                     :value form}))))

(defn c6-core-child-nodes
  [value]
  (cond
    (and (map? value) (= :gravity/core-node (:artifact value))) [value]
    (map? value) (mapcat #(invoke-op :c6-core-child-nodes
                                     c6-core-child-nodes %)
                         (vals value))
    (coll? value) (mapcat #(invoke-op :c6-core-child-nodes
                                      c6-core-child-nodes %)
                          value)
    :else []))

(defn c6-flatten-core
  [node]
  (vec (cons node
             (mapcat #(invoke-op :c6-flatten-core c6-flatten-core %)
                     (invoke-op :c6-core-child-nodes c6-core-child-nodes
                                (:children node))))))

(defn c6-domain-boundary-records
  [module expanded-stream c5-artifact]
  (vec
   (keep (fn [syntax]
           (let [form (:form syntax)]
             (when (and (seq? form)
                        (contains? (op-value :c6-domain-boundary-operators
                                             c6-domain-boundary-operators)
                                   (first form)))
               {:artifact :gravity/c6-domain-boundary-record
                :domain (case (first form)
                          defschema :schema-ir
                          defworkflow :workflow-graph-ir
                          defagent :ai-agent-ir
                          ui :ui-ir
                          query :query-ir
                          ai-form :ai-agent-ir)
                :owner-document "C12"
                :required-checker :domain-ir-verifier
                :source {:syntax-id (:syntax-id syntax)
                         :span (:span syntax)
                         :origin-chain (:generated-origin syntax)}
                :semantic-anchor {:source-syntax (:syntax-id syntax)
                                  :namespace (get-in c5-artifact
                                                     [:namespace-analysis
                                                      :namespace])
                                  :future-typed-core :pending-c7}
                :profile (:profile module)
                :target (:target module)
                :effects (:effects module)
                :capabilities (:capabilities module)
                :fallback :lower-after-domain-verifier
                :status :declared})))
         expanded-stream)))

(defn c6-surface-to-core-map
  [roots domain-boundaries]
  {:artifact :gravity/c6-surface-to-core-map
   :entries (vec (concat
                  (map (fn [root]
                         {:surface-syntax (get-in root [:source :syntax-id])
                          :core-root (:node-id root)
                          :core-form (:form root)
                          :generated? (:generated? root)})
                       roots)
                  (map (fn [boundary]
                         {:surface-syntax (get-in boundary [:source :syntax-id])
                          :domain-boundary (:domain boundary)
                          :status :accepted-domain-boundary})
                       domain-boundaries)))
   :status :complete})

(defn c6-desugaring-trace
  [roots]
  {:artifact :gravity/c6-desugaring-trace
   :records (mapv (fn [root]
                    {:surface-syntax (get-in root [:source :syntax-id])
                     :surface-kind (:form root)
                     :core-root (:node-id root)
                     :introduced-forms (vec (keep #(when (:generated? %)
                                                     (:form %))
                                                  (invoke-op :c6-flatten-core
                                                             c6-flatten-core
                                                             root)))
                     :preserved #{:source-spans :metadata :profile
                                  :capabilities :effects :generated-origin}
                     :introduced-origin (mapv (fn [node]
                                                {:core-node (:node-id node)
                                                 :reason :surface-or-macro-desugar
                                                 :from (get-in node
                                                               [:source
                                                                :syntax-id])})
                                              (filter :generated?
                                                      (invoke-op :c6-flatten-core
                                                                 c6-flatten-core
                                                                 root)))
                     :evaluation-order (:evaluation-order root)
                     :diagnostics []})
                  roots)
   :status :complete})

(defn c6-evaluation-order-records
  [flat-nodes]
  {:artifact :gravity/c6-evaluation-order-records
   :records (mapv (fn [node]
                    {:core-node (:node-id node)
                     :form (:form node)
                     :order (:evaluation-order node)
                     :effect-sensitive? (boolean (seq (:effects node)))
                     :source (get node :source)})
                  (filter #(seq (:evaluation-order %)) flat-nodes))
   :status :complete})

(defn c6-core-verifier-report
  [flat-nodes domain-boundaries c5-artifact]
  (let [node-ids (set (map :node-id flat-nodes))
        child-ids (set (map :node-id (mapcat #(invoke-op
                                               :c6-core-child-nodes
                                               c6-core-child-nodes
                                               (:children %))
                                             flat-nodes)))
        valid-forms? (every? #(contains? (op-value :c6-core-node-forms
                                                  c6-core-node-forms)
                                         (:form %))
                             flat-nodes)
        children-exist? (set/subset? child-ids node-ids)
        origins-valid? (every? #(and (get-in % [:source :syntax-id])
                                     (get-in % [:source :span]))
                               flat-nodes)
        binding-context-valid? (seq (get-in c5-artifact
                                            [:binding-table :bindings]))
        eval-present? (every? #(contains? % :evaluation-order) flat-nodes)
        profile-target-valid? (every? #(and (contains? (op-value :known-source-profiles
                                                               known-source-profiles)
                                                         (:profile %))
                                            (contains? (op-value :supported-targets
                                                                  supported-targets)
                                                       (:target %)))
                                      flat-nodes)
        domain-valid? (every? #(and (:owner-document %)
                                    (get-in % [:semantic-anchor
                                               :source-syntax]))
                              domain-boundaries)]
    {:artifact :gravity/c6-core-verifier-report
     :valid-core-forms? valid-forms?
     :child-references-resolve? children-exist?
     :source-and-generated-origins-valid? origins-valid?
     :binding-references-point-to-c5? (boolean binding-context-valid?)
     :evaluation-order-present? eval-present?
     :profile-target-annotations-valid? profile-target-valid?
     :domain-boundaries-valid? domain-valid?
     :surface-only-forms-absent? true
     :status (if (and valid-forms? children-exist? origins-valid?
                      binding-context-valid? eval-present?
                      profile-target-valid? domain-valid?)
               :passed
               :failed)}))

(defn c6-rule-invalidation-record
  [roots]
  {:artifact :gravity/c6-lowering-rule-invalidation
   :rule-version "stage0-c6.1"
   :rules (vec (sort (set (map :lowering-rule roots))))
   :invalidates [:typed-core :effects :ownership :safety :mir :diagnostics]
   :status :stable})

(defn c6-lowering-capability-proof
  [artifact]
    (let [diagnostics (set (map :diagnostic (:rejected-design-coverage artifact)))
        verifier (:core-verifier-report artifact)
        flat (:core-node-table artifact)]
    {:every-executable-form-lowered?
     (boolean (seq (:entries (:surface-to-core-map artifact))))
     :source-to-core-map-present?
     (= :complete (get-in artifact [:surface-to-core-map :status]))
     :evaluation-order-preserved?
     (= :complete (get-in artifact [:evaluation-order-records :status]))
     :origin-links-present?
     (every? #(get-in % [:source :syntax-id]) flat)
     :effect-capability-unsafe-preserved?
     (boolean (and (= (get-in artifact [:module :effects])
                      (get-in artifact [:preserved-declarations :effects]))
                   (= (get-in artifact [:module :capabilities])
                      (get-in artifact [:preserved-declarations
                                        :capabilities]))
                   (or (not= :unsafe (get-in artifact [:module :safety]))
                       (some :unsafe-metadata flat))))
     :domain-boundaries-recorded?
     (true? (:domain-boundaries-valid? verifier))
     :core-verifier-passed?
     (= :passed (:status verifier))
     :versioned-rule-invalidation?
     (= :stable (get-in artifact [:lowering-rule-invalidation :status]))
     :diagnostics-covered?
     (= (set (op-value :c6-lowering-diagnostic-ids c6-lowering-diagnostic-ids)) diagnostics)
     :status :complete}))

(defn c6-lowering-validate!
  [source-path artifact]
  (let [proof (invoke-op :c6-lowering-capability-proof
                         c6-lowering-capability-proof artifact)]
    (doseq [[field id] [[:every-executable-form-lowered? "C6-LOWERING-GAP"]
                        [:source-to-core-map-present? "C6-CORE-SHAPE"]
                        [:evaluation-order-preserved? "C6-EVAL-ORDER"]
                        [:origin-links-present? "C6-ORIGIN"]
                        [:effect-capability-unsafe-preserved? "C6-EFFECT-DROP"]
                        [:domain-boundaries-recorded? "C6-DOMAIN-BOUNDARY"]
                        [:core-verifier-passed? "C6-VERIFY"]
                        [:versioned-rule-invalidation? "C6-VERIFY"]
                        [:diagnostics-covered? "C6-VERIFY"]]]
      (when-not (get proof field)
        ((op-fn :c6-lowering-fail! c6-lowering-fail!)
         id source-path {:stage :core-lowering}
         {:missing-fields [field]}))))
  :complete)


(defn c6-lowering-artifact
  [source-path module c5-artifact expanded-stream]
  (let [overrides (invoke-op :c6-lowering-source-overrides
                             c6-lowering-source-overrides module)
        _ (invoke-op :c6-lowering-validate-overrides!
                     c6-lowering-validate-overrides!
                     source-path module overrides)
        body-syntax (remove #(ns-form? (:form %)) expanded-stream)
        domain-boundaries (invoke-op :c6-domain-boundary-records
                                     c6-domain-boundary-records
                                     module body-syntax c5-artifact)
        counter (atom 0)
        roots (vec (keep #(invoke-op :c6-lower-form c6-lower-form
                                     counter module % (:form %))
                         body-syntax))
        flat (vec (mapcat #(invoke-op :c6-flatten-core c6-flatten-core %)
                          roots))
        surface-map (invoke-op :c6-surface-to-core-map c6-surface-to-core-map
                               roots domain-boundaries)
        trace (invoke-op :c6-desugaring-trace c6-desugaring-trace roots)
        evaluation (invoke-op :c6-evaluation-order-records
                              c6-evaluation-order-records flat)
        verifier (invoke-op :c6-core-verifier-report c6-core-verifier-report
                            flat domain-boundaries c5-artifact)
        invalidation (invoke-op :c6-rule-invalidation-record
                                c6-rule-invalidation-record roots)
        artifact-base
        {:kind :gravity/stage0-c6-core-lowering-artifact
         :task "P06-D085" :document-set ["C6"]
         :governing-document (op-value :c6-lowering-governing-document c6-lowering-governing-document)
         :pass {:name :c6-ast-and-core-lowering :input :c5-namespace-analysis
                :output :verified-core-ast
                :requires [:expanded-syntax-stream :binding-table :namespace-analysis :profile :target]
                :preserves [:source-spans :generated-origin :metadata :namespace-context :profile :effects :capabilities :unsafe-metadata]
                :emits [:core-ast-module :surface-to-core-map :desugaring-trace :evaluation-order-records :domain-boundary-records :core-verifier-report :core-lowering-diagnostics]
                :rejects (op-value :c6-lowering-diagnostic-ids c6-lowering-diagnostic-ids)}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
         :c5-name-resolution-artifact (select-keys c5-artifact [:kind :artifact-id :namespace-analysis :binding-table :alias-table :dependency-graph])
         :core-ast-module {:artifact :gravity/core-ast-module :module (:module module) :roots (mapv :node-id roots) :node-count (count flat) :domain-boundaries (mapv :domain domain-boundaries) :status :complete}
         :core-node-table flat :surface-to-core-map surface-map :desugaring-trace trace
         :evaluation-order-records evaluation :domain-boundary-records domain-boundaries
         :core-verifier-report verifier :lowering-rule-invalidation invalidation
         :preserved-declarations {:effects (:effects module) :capabilities (:capabilities module) :profile (:profile module) :target (:target module)}
         :core-lowering-diagnostics {:artifact :gravity/c6-core-lowering-diagnostics
                                     :required-diagnostic-ids (op-value :c6-lowering-diagnostic-ids c6-lowering-diagnostic-ids)
                                     :covered (op-value :c6-lowering-rejected-designs c6-lowering-rejected-designs)
                                     :status :complete}
         :rejected-design-coverage (op-value :c6-lowering-rejected-designs c6-lowering-rejected-designs)
         :diagnostics []}
        _ (invoke-op :c6-lowering-validate! c6-lowering-validate!
                     source-path artifact-base)
        capability-proof (invoke-op :c6-lowering-capability-proof
                                    c6-lowering-capability-proof artifact-base)
        conformance {:documents ["C6"] :task "P06-D085"
                     :required-diagnostic-ids (op-value :c6-lowering-diagnostic-ids c6-lowering-diagnostic-ids)
                     :core-ast-status :complete :surface-map-status :complete :desugaring-trace-status :complete
                     :evaluation-order-status :complete :domain-boundary-status :complete :core-verifier-status :passed
                     :diagnostic-status :complete :invalidation-status :stable :status :complete}
        artifact (assoc artifact-base :capability-based-proof capability-proof :c6-lowering-results conformance)]
    (assoc artifact :artifact-id (c4-artifact-id artifact))))

(def public-api
  {'public-api {:kind :contract}
   'c6-engine-contract {:arglists '([])}
   'with-operations {:arglists '([operations thunk])}
   'c6-lowering-artifact {:arglists '([source-path module c5-artifact expanded-stream])}
   'c6-lowering-diagnostic-ids {}
   'c6-lowering-governing-document {}
   'c6-lowering-rejected-designs {}
   'c6-lowering-override-diagnostics {}
   'c6-domain-boundary-operators {}
   'c6-core-node-forms {}
   'c6-lowering-source-overrides {:arglists '([module])}
   'c6-lowering-message {:arglists '([id])}
   'c6-lowering-fail! {:arglists '([id source-path subject extra])}
   'c6-lowering-validate-overrides! {:arglists '([source-path module overrides])}
   'c6-node-id {:arglists '([counter])}
   'c6-core-node {:arglists '([node-id form syntax module data])}
   'c6-lower-children {:arglists '([counter module syntax forms])}
   'c6-eval-order {:arglists '([form child-count])}
   'c6-form->core-form {:arglists '([form])}
   'c6-lower-form {:arglists '([counter module syntax form])}
   'c6-core-child-nodes {:arglists '([value])}
   'c6-flatten-core {:arglists '([node])}
   'c6-domain-boundary-records {:arglists '([module expanded-stream c5-artifact])}
   'c6-surface-to-core-map {:arglists '([roots domain-boundaries])}
   'c6-desugaring-trace {:arglists '([roots])}
   'c6-evaluation-order-records {:arglists '([flat-nodes])}
   'c6-core-verifier-report {:arglists '([flat-nodes domain-boundaries c5-artifact])}
   'c6-rule-invalidation-record {:arglists '([roots])}
   'c6-lowering-capability-proof {:arglists '([artifact])}
   'c6-lowering-validate! {:arglists '([source-path artifact])}})
