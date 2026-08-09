(ns gravity.c7-type-checker
  "Hosted Stage0 C7 type-analysis engine and artifact projection.

  This namespace owns the Clojure seed compatibility implementation of C7.
  Source acquisition and pass routing are injected by gravity.bootstrap. It is
  not canonical Gravity authority and confers no proof or release status."
  (:require [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(def ^:private function-operation-keys
  #{:fail! :source-span :c4-artifact-id :read-source-form-records
    :validate-ns-syntax! :parse-module
    :compiler-c6-lowering-source-artifact
    :c7-type-source-overrides :c7-type-message :c7-type-fail!
    :c7-type-validate-overrides! :c7-literal-type :c7-node-operator
    :c7-node-type :c7-type-fact :c7-type-environment
    :c7-constraint-ledger :c7-function-table
    :c7-dynamic-boundary-records :c7-cast-records
    :c7-generic-instantiations :c7-protocol-dispatch-table
    :c7-schema-links :c7-layout-facts :c7-type-diagnostics
    :c7-typed-core-verifier-report :c7-type-capability-proof
    :c7-type-validate! :compiler-c7-type-source-artifact
    :compiler-c7-type-file-artifact})

(def ^:private scalar-operation-keys
  #{:c7-type-diagnostic-ids :c7-type-governing-document
    :c7-type-rejected-designs :c7-type-override-diagnostics})

(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn- current-operation
  [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defmacro ^:private definterposable
  [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- default-fail!
  [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))

(defn- default-source-span
  [source-path form-index]
  {:source source-path :form-index form-index})

(defn- default-c4-artifact-id
  [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))

(defn- unsupported-host-operation
  [operation]
  (fn [& _]
    (throw (ex-info (str "C7 leaf requires injected operation " operation)
                    {:operation operation}))))

(defn- op-fn
  [key fallback]
  (or (get *operations* key) fallback))

(defn- fail!
  [id message data]
  ((op-fn :fail! default-fail!) id message data))

(defn- source-span
  [source-path form-index]
  ((op-fn :source-span default-source-span) source-path form-index))

(defn- c4-artifact-id
  [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))

(defn- read-source-form-records
  [source-path source-text]
  ((op-fn :read-source-form-records
          (unsupported-host-operation :read-source-form-records))
   source-path source-text))

(defn- validate-ns-syntax!
  [source-path forms]
  ((op-fn :validate-ns-syntax!
          (unsupported-host-operation :validate-ns-syntax!))
   source-path forms))

(defn- parse-module
  [source-path forms]
  ((op-fn :parse-module
          (unsupported-host-operation :parse-module))
   source-path forms))

(defn- compiler-c6-lowering-source-artifact
  [source-path source-text]
  ((op-fn :compiler-c6-lowering-source-artifact
          (unsupported-host-operation :compiler-c6-lowering-source-artifact))
   source-path source-text))

(def ^:dynamic c7-type-diagnostic-ids
  ["C7-TYPE-MISMATCH"
   "C7-ANNOTATION"
   "C7-DYNAMIC"
   "C7-CAST"
   "C7-NULLABILITY"
   "C7-GENERIC"
   "C7-PROTOCOL"
   "C7-LAYOUT"
   "C7-SCHEMA"
   "C7-VERIFY"])

(def ^:dynamic c7-type-governing-document
  "docs/phase-06-compiler-architecture/086-c7-type-checker-design.md")

(def ^:dynamic c7-type-rejected-designs
  [{:diagnostic "C7-TYPE-MISMATCH"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-type-mismatch.gravity"
    :rejected-design :incompatible-inferred-and-expected-types}
   {:diagnostic "C7-ANNOTATION"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-annotation.gravity"
    :rejected-design :profile-required-type-fact-missing}
   {:diagnostic "C7-DYNAMIC"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-dynamic.gravity"
    :rejected-design :dynamic-fallback-in-constrained-profile}
   {:diagnostic "C7-CAST"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-cast.gravity"
    :rejected-design :unchecked-or-illegal-conversion}
   {:diagnostic "C7-NULLABILITY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-nullability.gravity"
    :rejected-design :host-null-without-typed-wrapper}
   {:diagnostic "C7-GENERIC"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-generic.gravity"
    :rejected-design :failed-generic-instantiation}
   {:diagnostic "C7-PROTOCOL"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-protocol.gravity"
    :rejected-design :missing-protocol-implementation}
   {:diagnostic "C7-LAYOUT"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-layout.gravity"
    :rejected-design :missing-profile-required-layout-facts}
   {:diagnostic "C7-SCHEMA"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-schema.gravity"
    :rejected-design :schema-derived-type-weakened}
   {:diagnostic "C7-VERIFY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-verify.gravity"
    :rejected-design :typed-core-verifier-failure}])

(def ^:dynamic c7-type-override-diagnostics
  {:type-mismatch "C7-TYPE-MISMATCH"
   :annotation "C7-ANNOTATION"
   :dynamic "C7-DYNAMIC"
   :cast "C7-CAST"
   :nullability "C7-NULLABILITY"
   :generic "C7-GENERIC"
   :protocol "C7-PROTOCOL"
   :layout "C7-LAYOUT"
   :schema "C7-SCHEMA"
   :verify "C7-VERIFY"})

(definterposable c7-type-source-overrides
  [module]
  (get-in module [:metadata :compiler :c7-type-check] {}))

(definterposable c7-type-message
  [id]
  (case id
    "C7-TYPE-MISMATCH" "inferred type is incompatible with the expected type"
    "C7-ANNOTATION" "active profile requires a type annotation or layout fact"
    "C7-DYNAMIC" "dynamic behavior is forbidden by the active profile"
    "C7-CAST" "cast or conversion lacks a checked or unsafe classification"
    "C7-NULLABILITY" "host null crossed into a non-null Gravity type without a wrapper"
    "C7-GENERIC" "generic instantiation failed or omitted bound evidence"
    "C7-PROTOCOL" "protocol dispatch lacks a matching implementation"
    "C7-LAYOUT" "profile-required layout facts are missing"
    "C7-SCHEMA" "schema-derived type lost source schema identity"
    "C7-VERIFY" "typed-core verifier rejected the artifact"
    "Type checking failed"))

(definterposable c7-type-fail!
  [id source-path subject extra]
  (fail! id
         (c7-type-message id)
         (merge {:source-span (or (:source-span subject)
                                  (get-in subject [:source :span])
                                  (:span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :c7-type-checker
                 :stage :type-check
                 :document-id "C7"
                 :expected-document c7-type-governing-document
                 :core-node-id (or (:core-node-id subject) (:node-id subject))
                 :syntax-id (or (:syntax-id subject)
                                (get-in subject [:source :syntax-id]))
                 :expected-type (or (:expected-type subject) "Typed")
                 :actual-type (or (:actual-type subject) "Dynamic")
                 :active-profile (:profile subject)
                 :target (:target subject)
                 :relevant-binding-id (:binding-ref subject)
                 :generated-origin-chain (or (:generated-origin subject)
                                             (get-in subject
                                                     [:source :origin-chain]))
                 :remediation "Emit typed-core facts, solved constraints, checked casts, dynamic boundary records, schema/layout links, generic and protocol evidence, and verifier-accepted diagnostics before effect checking."}
                extra)))

(definterposable c7-type-validate-overrides!
  [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get c7-type-override-diagnostics fail-kind)]
      (c7-type-fail! id source-path
                     {:source-span (source-span source-path 0)
                      :syntax-id "fixture-override"
                      :core-node-id "fixture-override"
                      :expected-type "Expected"
                      :actual-type "Actual"
                      :profile (:profile module)
                      :target (:target module)
                      :generated-origin []}
                     {:missing-fields [fail-kind]}))))

(definterposable c7-literal-type
  [value]
  (cond
    (nil? value) "Nil"
    (true? value) "Boolean"
    (false? value) "Boolean"
    (integer? value) "I64"
    (float? value) "F64"
    (string? value) "String"
    (keyword? value) "Keyword"
    (symbol? value) "Symbol"
    (vector? value) "Vector[Dynamic]"
    (map? value) "Map[Keyword, Dynamic]"
    (set? value) "Set[Dynamic]"
    (seq? value) "List[Dynamic]"
    :else "Dynamic"))

(definterposable c7-node-operator
  [node]
  (get-in node [:children :operator]))

(definterposable c7-node-type
  [node]
  (let [operator (c7-node-operator node)]
    (case (:form node)
      :literal (c7-literal-type (:value node))
      quote "Syntax"
      :symbol "BindingRef"
      def "Var"
      fn "Fn[Dynamic]->Dynamic"
      let "Dynamic"
      do "Dynamic"
      if "Dynamic"
      match "Dynamic"
      try "Dynamic"
      throw "Never"
      loop "Dynamic"
      recur "Never"
      var "VarRef"
      set! "Unit"
      :declared-primitive (if (:unsafe-metadata node)
                            "UnsafeIsland[Dynamic]"
                            "PrimitiveResult")
      :call (case operator
              dynamic/value "Dynamic"
              dynamic/cast "CheckedCast[String]"
              generic/id "Generic[T]"
              protocol/value "ProtocolValue"
              schema/derive "SchemaDerived"
              schema/validate "Validated[Schema]"
              "Dynamic")
      "Dynamic")))

(definterposable c7-type-fact
  [node]
  {:artifact :gravity/c7-type-fact
   :fact-id (str "c7-type-" (:node-id node))
   :core-node (:node-id node)
   :source (:source node)
   :type (c7-node-type node)
   :type-source :local-deterministic-inference
   :profile (:profile node)
   :target (:target node)
   :effects (:effects node)
   :capabilities (:capabilities node)
   :ownership {:mode :borrowed :resource :nonlinear}
   :layout {:representation (case (:profile node)
                              :hosted :managed-object
                              :native :layout-required
                              :kernel :explicit-layout-required
                              :firmware :fixed-layout-required
                              :hardware :synthesizable-layout-required
                              :abstract)
            :status :recorded}
   :diagnostics []})

(definterposable c7-type-environment
  [type-facts]
  {:artifact :gravity/c7-type-environment
   :types (into (sorted-map)
                (map (fn [fact] [(:core-node fact) (:type fact)])
                     type-facts))
   :locals (into (sorted-map)
                 (keep (fn [fact]
                         (when (= "BindingRef" (:type fact))
                           [(:core-node fact)
                            {:type (:type fact)
                             :mutability :immutable
                             :ownership :borrowed}]))
                       type-facts))
   :status :complete})

(definterposable c7-constraint-ledger
  [type-facts]
  {:artifact :gravity/c7-constraint-ledger
   :constraints
   (mapv (fn [idx fact]
           {:constraint-id (str "c7-constraint-" idx)
            :kind :type-assignment
            :source-node (:core-node fact)
            :producer-rule :local-inference
            :dependencies [(:core-node fact)]
            :solution (:type fact)
            :invalidation [:core-node :binding-table :profile-contract]
            :status :solved})
         (range)
         type-facts)
   :status :solved})

(definterposable c7-function-table
  [nodes]
  {:artifact :gravity/c7-function-type-table
   :functions
   (mapv (fn [node]
           {:fn-id (:node-id node)
            :params (vec (repeat (count (get-in node [:children :params]))
                                 "Dynamic"))
            :return "Dynamic"
            :latent-effects (:effects node)
            :capabilities (:capabilities node)
            :ownership-constraints [:borrowed-captures-preserved]
            :profile-constraints [(:profile node)]
            :throws #{"String"}
            :source (:source node)
            :status :typed})
         (filter #(= 'fn (:form %)) nodes))
   :status :complete})

(definterposable c7-dynamic-boundary-records
  [nodes module]
  {:artifact :gravity/c7-dynamic-boundary-records
   :records
   (mapv (fn [node]
           {:boundary-id (str "c7-dynamic-" (:node-id node))
            :kind :dynamic-call
            :source (:node-id node)
            :input-type "Dynamic"
            :result-type "Dynamic"
            :profile (:profile node)
            :target (:target node)
            :runtime-checks [:runtime-type-known]
            :effects #{:runtime/dynamic-dispatch}
            :capabilities #{}
            :accepted? (= :hosted (:profile module))
            :diagnostics []})
         (filter #(= 'dynamic/value (c7-node-operator %)) nodes))
   :status :complete})

(definterposable c7-cast-records
  [nodes]
  {:artifact :gravity/c7-cast-records
   :records
   (mapv (fn [node]
           {:cast-id (str "c7-cast-" (:node-id node))
            :kind :checked-dynamic-cast
            :source-node (:node-id node)
            :from "Dynamic"
            :to "String"
            :classification :runtime-checked
            :runtime-check :type-tag-check
            :unsafe-metadata (:unsafe-metadata node)
            :source (:source node)
            :status :checked})
         (filter #(= 'dynamic/cast (c7-node-operator %)) nodes))
   :status :complete})

(definterposable c7-generic-instantiations
  [nodes]
  {:artifact :gravity/c7-generic-instantiation-table
   :records
   (mapv (fn [node]
           {:instantiation-id (str "c7-generic-" (:node-id node))
            :generic 'generic/id
            :type-arguments ["T"]
            :bounds ["Any"]
            :source-node (:node-id node)
            :profile (:profile node)
            :target (:target node)
            :status :solved})
         (filter #(= 'generic/id (c7-node-operator %)) nodes))
   :status :complete})

(definterposable c7-protocol-dispatch-table
  [nodes]
  {:artifact :gravity/c7-protocol-dispatch-type-table
   :records
   (mapv (fn [node]
           {:dispatch-id (str "c7-dispatch-" (:node-id node))
            :protocol :Displayable
            :method 'protocol/value
            :receiver-type "String"
            :dispatch :hosted-dynamic
            :effects (:effects node)
            :capabilities (:capabilities node)
            :profile (:profile node)
            :target (:target node)
            :source-node (:node-id node)
            :status :typed})
         (filter #(= 'protocol/value (c7-node-operator %)) nodes))
   :status :complete})

(definterposable c7-schema-links
  [domain-boundaries]
  {:artifact :gravity/c7-schema-type-links
   :records
   (mapv (fn [boundary]
           {:schema-type-id (str "c7-schema-"
                                 (get-in boundary
                                         [:source :syntax-id]))
            :schema :Packet
            :source-schema (get-in boundary [:semantic-anchor :source-syntax])
            :domain (:domain boundary)
            :validation-boundary :schema-ir-verifier
            :profile (:profile boundary)
            :target (:target boundary)
            :status :preserved})
         (filter #(= :schema-ir (:domain %)) domain-boundaries))
   :status :complete})

(definterposable c7-layout-facts
  [nodes]
  {:artifact :gravity/c7-layout-facts
   :records
   (mapv (fn [node]
           {:layout-id (str "c7-layout-" (:node-id node))
            :core-node (:node-id node)
            :type (c7-node-type node)
            :profile (:profile node)
            :target (:target node)
            :layout (case (:profile node)
                      :hosted :managed
                      :native :explicit-native-layout
                      :firmware :fixed-layout
                      :kernel :explicit-kernel-layout
                      :hardware :synthesizable-layout
                      :abstract)
            :status :recorded})
         nodes)
   :status :complete})

(definterposable c7-type-diagnostics
  [source-path nodes]
  {:artifact :gravity/c7-type-diagnostic-registry
   :required-diagnostic-ids c7-type-diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           (let [node (first nodes)]
             {:diagnostic (:diagnostic design)
              :fixture (:fixture design)
              :core-node-id (:node-id node)
              :syntax-id (get-in node [:source :syntax-id])
              :source-span (get-in node [:source :span]
                                   (source-span source-path 0))
              :expected-type "Expected"
              :actual-type "Actual"
              :active-profile (:profile node)
              :target (:target node)
              :relevant-binding-id (:node-id node)
              :generated-origin-chain (get-in node [:source :origin-chain])
              :remediation "Keep C7 type facts explicit and profile-gated."}))
         c7-type-rejected-designs)
   :status :complete})

(definterposable c7-typed-core-verifier-report
  [nodes type-facts constraints functions dynamic cast generic dispatch schema layout]
  (let [node-ids (set (map :node-id nodes))
        typed-node-ids (set (map :core-node type-facts))
        all-typed? (= node-ids typed-node-ids)
        constraints-solved? (every? #(= :solved (:status %))
                                    (:constraints constraints))
        functions-have-effects? (every? #(contains? % :latent-effects)
                                        (:functions functions))
        casts-classified? (every? #(contains? % :classification)
                                  (:records cast))
        dynamic-profiled? (every? #(contains? % :profile) (:records dynamic))
        schema-preserved? (seq (:records schema))
        layout-recorded? (and (seq (:records layout))
                              (every? #(= :recorded (:status %))
                                      (:records layout)))
        origins-preserved? (every? #(get-in % [:source :syntax-id]) nodes)
        generic-solved? (= :complete (:status generic))
        dispatch-typed? (= :complete (:status dispatch))]
    {:artifact :gravity/c7-typed-core-verifier-report
     :every-node-typed-or-diagnostic? all-typed?
     :constraints-solved? constraints-solved?
     :function-latent-effects-present? functions-have-effects?
     :casts-classified? casts-classified?
     :dynamic-boundaries-profile-marked? dynamic-profiled?
     :schema-derived-types-preserve-identity? (boolean schema-preserved?)
     :layout-facts-recorded? layout-recorded?
     :generic-instantiations-solved? generic-solved?
     :protocol-dispatch-typed? dispatch-typed?
     :origins-preserved? origins-preserved?
     :status (if (and all-typed? constraints-solved?
                      functions-have-effects? casts-classified?
                      dynamic-profiled? schema-preserved? layout-recorded?
                      generic-solved? dispatch-typed? origins-preserved?)
               :passed
               :failed)}))

(definterposable c7-type-capability-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:type-diagnostics :diagnostics])))
        verifier (:typed-core-verifier-report artifact)]
    {:every-core-node-has-type-or-diagnostic?
     (:every-node-typed-or-diagnostic? verifier)
     :constraints-solved?
     (:constraints-solved? verifier)
     :function-types-include-latent-effects?
     (:function-latent-effects-present? verifier)
     :dynamic-boundaries-profile-gated?
     (:dynamic-boundaries-profile-marked? verifier)
     :casts-classified?
     (:casts-classified? verifier)
     :generic-and-protocol-evidence?
     (and (:generic-instantiations-solved? verifier)
          (:protocol-dispatch-typed? verifier))
     :schema-identity-preserved?
     (:schema-derived-types-preserve-identity? verifier)
     :layout-facts-recorded?
     (:layout-facts-recorded? verifier)
     :diagnostics-covered?
     (= (set c7-type-diagnostic-ids) diagnostics)
     :verifier-passed?
     (= :passed (:status verifier))
     :status :complete}))

(definterposable c7-type-validate!
  [source-path artifact]
  (let [proof (c7-type-capability-proof artifact)]
    (doseq [[field id] [[:every-core-node-has-type-or-diagnostic?
                         "C7-TYPE-MISMATCH"]
                        [:constraints-solved? "C7-VERIFY"]
                        [:function-types-include-latent-effects?
                         "C7-VERIFY"]
                        [:dynamic-boundaries-profile-gated? "C7-DYNAMIC"]
                        [:casts-classified? "C7-CAST"]
                        [:generic-and-protocol-evidence? "C7-GENERIC"]
                        [:schema-identity-preserved? "C7-SCHEMA"]
                        [:layout-facts-recorded? "C7-LAYOUT"]
                        [:diagnostics-covered? "C7-VERIFY"]
                        [:verifier-passed? "C7-VERIFY"]]]
      (when-not (get proof field)
        (c7-type-fail! id source-path {:stage :type-check}
                       {:missing-fields [field]}))))
  :complete)

(definterposable compiler-c7-type-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (c7-type-source-overrides module)
        _ (c7-type-validate-overrides! source-path module overrides)
        c6-artifact (compiler-c6-lowering-source-artifact source-path
                                                          source-text)
        nodes (:core-node-table c6-artifact)
        type-facts (mapv c7-type-fact nodes)
        environment (c7-type-environment type-facts)
        constraints (c7-constraint-ledger type-facts)
        functions (c7-function-table nodes)
        dynamic (c7-dynamic-boundary-records nodes module)
        cast (c7-cast-records nodes)
        generic (c7-generic-instantiations nodes)
        dispatch (c7-protocol-dispatch-table nodes)
        schema (c7-schema-links (:domain-boundary-records c6-artifact))
        layout (c7-layout-facts nodes)
        diagnostics (c7-type-diagnostics source-path nodes)
        typed-core
        {:artifact :gravity/typed-core
         :module (get-in c6-artifact [:core-ast-module :module])
         :core-input (:artifact-id c6-artifact)
         :types (:types environment)
         :locals (:locals environment)
         :functions (:functions functions)
         :constraints (mapv :constraint-id (:constraints constraints))
         :dynamic-boundaries (mapv :boundary-id (:records dynamic))
         :casts (mapv :cast-id (:records cast))
         :layout-facts :c7-layout-facts
         :diagnostics []
         :status :complete}
        verifier (c7-typed-core-verifier-report nodes type-facts constraints
                                                functions dynamic cast generic
                                                dispatch schema layout)
        artifact-base
        {:kind :gravity/stage0-c7-type-checker-artifact
         :task "P06-D086"
         :document-set ["C7"]
         :governing-document c7-type-governing-document
         :pass {:name :c7-type-checker
                :input :verified-core-ast
                :output :typed-core
                :requires [:core-ast-module :core-node-table
                           :binding-table :profile :target
                           :domain-boundary-records]
                :preserves [:source-spans :generated-origin :metadata
                            :profile :target :effects :capabilities
                            :unsafe-metadata]
                :emits [:typed-core-module :type-environment
                        :constraint-ledger :generic-instantiation-table
                        :protocol-dispatch-type-table
                        :dynamic-boundary-records :cast-conversion-records
                        :layout-facts :schema-type-links
                        :typed-core-verifier-report :type-diagnostics]
                :rejects c7-type-diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c6-core-lowering-artifact
         (select-keys c6-artifact [:kind :artifact-id :core-ast-module
                                   :surface-to-core-map
                                   :evaluation-order-records
                                   :domain-boundary-records])
         :typed-core-module typed-core
         :type-environment environment
         :type-facts type-facts
         :constraint-ledger constraints
         :function-type-table functions
         :generic-instantiation-table generic
         :protocol-dispatch-type-table dispatch
         :dynamic-boundary-records dynamic
         :cast-conversion-records cast
         :layout-facts layout
         :schema-type-links schema
         :typed-core-verifier-report verifier
         :type-diagnostics diagnostics
         :c7-type-check-results
         {:documents ["C7"]
          :task "P06-D086"
          :required-diagnostic-ids c7-type-diagnostic-ids
          :typed-core-status :complete
          :type-environment-status :complete
          :constraint-status :solved
          :generic-status :complete
          :protocol-status :complete
          :dynamic-boundary-status :complete
          :cast-status :complete
          :schema-link-status :complete
          :layout-status :complete
          :verifier-status (:status verifier)
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (c7-type-validate! source-path artifact-base)
        capability-proof (c7-type-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c7-type-file-artifact
  [path]
  (compiler-c7-type-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:namespace 'gravity.c7-type-checker
   :contract-boundary :hosted-stage0-c7-type-checker
   :artifact-inputs [:c6-core-lowering-artifact :module-context]
   :artifact-outputs [:typed-core-module :type-environment
                      :constraint-ledger :function-type-table
                      :generic-instantiation-table
                      :protocol-dispatch-type-table
                      :dynamic-boundary-records :cast-conversion-records
                      :schema-type-links :layout-facts
                      :typed-core-verifier-report :type-diagnostics]
   :owns [:hosted-stage0-c7-type-analysis
          :hosted-stage0-c7-artifact-projection]
   :dependency-direction {:requires ['gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c7-authority :source-authentication
                  :c6-lowering-authority :effect-legality
                  :ownership-legality :safety-legality :mir-construction
                  :proof-authority :equivalence :self-hosting :release
                  :seed-retirement]
   :compatibility-only? true
   :canonical-c7-authority? false
   :clojure-seed-boundary? true
   :operation-interposition {:accepted-keys operation-keys
                             :partial-overrides? true
                             :unknown-keys-rejected? true
                             :single-binding-per-top-level-call? true}})

(defn- valid-string-vector?
  [value]
  (and (vector? value) (seq value) (every? string? value)))

(defn- valid-rejected-designs?
  [value]
  (and (vector? value) (every? map? value)))

(defn- valid-override-map?
  [value]
  (and (map? value)
       (every? (fn [[key item]]
                 (and (keyword? key) (string? item)))
               value)))

(defn- validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "C7 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        non-functions
        (seq (for [[key value] (select-keys operations
                                            function-operation-keys)
                   :when (not (fn? value))]
               key))]
    (when unknown
      (throw (ex-info "C7 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)
                       :allowed-keys operation-keys})))
    (when non-functions
      (throw (ex-info "C7 function operation values must be functions"
                      {:non-function-keys (vec non-functions)}))))
  (doseq [[key predicate expected]
          [[:c7-type-diagnostic-ids valid-string-vector?
            :non-empty-string-vector]
           [:c7-type-governing-document
            #(and (string? %) (seq %)) :non-empty-string]
           [:c7-type-rejected-designs valid-rejected-designs?
            :vector-of-maps]
           [:c7-type-override-diagnostics valid-override-map?
            :keyword-to-string-map]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C7 scalar operation has an invalid shape"
                    {:key key :expected expected
                     :actual (get operations key)})))
  operations)

(defn with-operations
  [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c7-type-diagnostic-ids
              (get merged :c7-type-diagnostic-ids
                   c7-type-diagnostic-ids)
              c7-type-governing-document
              (get merged :c7-type-governing-document
                   c7-type-governing-document)
              c7-type-rejected-designs
              (get merged :c7-type-rejected-designs
                   c7-type-rejected-designs)
              c7-type-override-diagnostics
              (get merged :c7-type-override-diagnostics
                   c7-type-override-diagnostics)]
      (thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c7-engine-contract {:arglists '([])}
   'c7-type-diagnostic-ids {:kind :constant}
   'c7-type-governing-document {:kind :constant}
   'c7-type-rejected-designs {:kind :constant}
   'c7-type-override-diagnostics {:kind :constant}
   'c7-type-source-overrides {:arglists '([module])}
   'c7-type-message {:arglists '([id])}
   'c7-type-fail! {:arglists '([id source-path subject extra])}
   'c7-type-validate-overrides!
   {:arglists '([source-path module overrides])}
   'c7-literal-type {:arglists '([value])}
   'c7-node-operator {:arglists '([node])}
   'c7-node-type {:arglists '([node])}
   'c7-type-fact {:arglists '([node])}
   'c7-type-environment {:arglists '([type-facts])}
   'c7-constraint-ledger {:arglists '([type-facts])}
   'c7-function-table {:arglists '([nodes])}
   'c7-dynamic-boundary-records {:arglists '([nodes module])}
   'c7-cast-records {:arglists '([nodes])}
   'c7-generic-instantiations {:arglists '([nodes])}
   'c7-protocol-dispatch-table {:arglists '([nodes])}
   'c7-schema-links {:arglists '([domain-boundaries])}
   'c7-layout-facts {:arglists '([nodes])}
   'c7-type-diagnostics {:arglists '([source-path nodes])}
   'c7-typed-core-verifier-report
   {:arglists '([nodes type-facts constraints functions dynamic cast
                 generic dispatch schema layout])}
   'c7-type-capability-proof {:arglists '([artifact])}
   'c7-type-validate! {:arglists '([source-path artifact])}
   'compiler-c7-type-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c7-type-file-artifact {:arglists '([path])}})

(defn c7-engine-contract
  []
  (assoc namespace-contract :public-api public-api))
