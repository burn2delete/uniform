(ns gravity.core-ast-lowering
  "Pure hosted Stage0 L2 core lowering.

  This namespace mirrors the small legacy L2 lowering cluster and its core
  artifact projection.  Source acquisition and all authority-bearing stages
  remain outside the leaf; callers provide the already-produced macro source
  artifact through the operation seam."
  (:require [clojure.string :as str]))

;; The operation table is deliberately dynamic.  Bootstrap can bind the seed
;; functions without making this leaf depend on gravity.bootstrap (or on the
;; diagnostic policy namespace).  Active keys prevent a replacement operation
;; from recursively calling itself; bypass keys provide one-shot entry into a
;; captured original Var while allowing recursive calls to see the replacement.
(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})

(def ^:private function-operation-keys
  #{:fail!
    :macro-source-artifact
    :uses-println?
    :form-effect
    :combine-effects
    :core-node
    :lower-sequential-body
    :extract-pattern-guard
    :lower-match-clauses
    :next-node-id
    :assert-recur-target!
    :assert-set-target!
    :assert-throw-legal!
    :assert-core-operator!
    :lower-core-expr
    :flatten-core
    :core-source-artifact})

(def ^:private scalar-operation-keys
  #{:core-forms :lowering-gap-forms :unknown-reserved-core-forms})

(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(def ^:private namespace-contract
  {:namespace 'gravity.core-ast-lowering
   :contract-boundary :hosted-stage0-l2-core-ast-lowering
   :public-api :bootstrap-compatible-l2-vars
   :leaf-only-api ['core-source-artifact]
   :owns [:hosted-stage0-l2-core-lowering-algorithm
          :hosted-stage0-l2-core-artifact-projection]
   :dependency-direction
   {:requires ['clojure.core 'clojure.string]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:source-acquisition
                  :source-reading
                  :macro-source-authority
                  :canonical-l2-authority
                  :canonical-c6-authority
                  :canonical-sh07-authority
                  :target-selection
                  :target-lowering
                  :backend
                  :proof-authority
                  :equivalence
                  :self-hosting
                  :release
                  :diagnostic-authority]
   :compatibility-only? true
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-l2-authority? false
   :canonical-c6-authority? false
   :canonical-sh07-authority? false
   :source-reading? false
   :target-lowering? false
   :backend-authority? false
   :proof-authority? false
   :equivalence-authority? false
   :release-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :scalar-values-must-satisfy
    {:core-forms :non-empty-symbol-set
     :lowering-gap-forms :non-empty-symbol-set
     :unknown-reserved-core-forms :non-empty-symbol-set}
    :single-binding-per-top-level-call? true
    :captured-original-one-shot? true}})

(declare public-api)

(defn- default-fail!
  [id message data]
  ;; Keep the stage0 carrier shape used by the legacy bootstrap diagnostics
  ;; namespace without taking a dependency on that authority-bearing leaf.
  (throw (ex-info message
                  (merge {:id id
                          :message message
                          :bootstrap-stage :stage0}
                         (or data {})))))

(defn- unsupported-host-operation
  [operation]
  (fn [& _]
    (throw (ex-info (str "L2 leaf requires injected operation " operation)
                    {:operation operation}))))

(defn- current-operation
  [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defmacro ^:private definterposable
  [name key arguments & body]
  `(defn ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys*
                 (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(defmacro ^:private definterposable-private
  [name key arguments & body]
  `(defn- ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys*
                 (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(defn- operation-value
  [key default]
  (if (contains? *operations* key)
    (get *operations* key)
    default))

(defn- invoke
  [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "L2 core lowering requires operation " key)
                    {:operation key}))))

(defn- valid-symbol-set?
  [value]
  (and (set? value)
       (seq value)
       (every? symbol? value)))

(defn- validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "L2 core lowering operation map must be a map"
                    {:value operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))
        invalid-functions
        (vec (for [[key value] (select-keys operations function-operation-keys)
                   :when (not (fn? value))]
               key))]
    (when (seq unknown)
      (throw (ex-info "L2 core lowering operation map contains unknown keys"
                      {:unknown-keys unknown
                       :allowed-keys operation-keys})))
    (when (seq invalid-functions)
      (throw (ex-info "L2 core lowering function operation values must be functions"
                      {:non-function-keys invalid-functions}))))
  (doseq [[key value] operations
          :when (and (contains? scalar-operation-keys key)
                     (not (valid-symbol-set? value)))]
    (throw (ex-info "L2 core lowering scalar operation has an invalid shape"
                    {:key key
                     :expected :non-empty-symbol-set
                     :actual value})))
  operations)

(defn with-operations
  [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "L2 core lowering thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* (merge *operations* operations)]
    (thunk)))

(defn call-entrypoint-body
  "Invoke one captured original body with a one-shot bypass.

  Bootstrap wrappers can pass the Var's original function here.  The first
  invocation executes that original body; recursive calls then observe the
  injected operation again, avoiding both infinite self-recursion and loss of
  recursive interposition."
  [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "L2 core lowering entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "L2 core lowering entrypoint must be a function"
                    {:operation operation-key
                     :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "L2 core lowering entrypoint args must be sequential"
                    {:operation operation-key
                     :args args})))
  (binding [*active-operation-keys*
            (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys*
            (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(definterposable-private fail!
  :fail!
  [id message data]
  (default-fail! id message data))

(def core-forms
  '#{quote if do let fn loop recur def var set! try throw match})

(def lowering-gap-forms
  '#{defn when -> cond case with-open with-region defmacro defschema defworkflow
     defagent ui query ai-form})

(def unknown-reserved-core-forms
  '#{core-unknown})

(definterposable-private uses-println?
  :uses-println?
  [form]
  (cond
    (seq? form) (or (= 'println (first form)) (some uses-println? form))
    (coll? form) (some uses-println? form)
    :else false))

(definterposable form-effect
  :form-effect
  [form]
  (cond
    (uses-println? form) #{:io/write}
    (and (seq? form) (= 'throw (first form))) #{:error/throw}
    (and (seq? form) (= 'set! (first form))) #{:state/write}
    :else #{}))

(defn combine-effects
  [& effect-sets]
  (if (contains? *bypass-next-operation-keys* :combine-effects)
    (binding [*bypass-next-operation-keys*
              (disj *bypass-next-operation-keys* :combine-effects)]
      (set (mapcat identity effect-sets)))
    (if-let [operation (current-operation :combine-effects)]
      (binding [*active-operation-keys*
                (conj *active-operation-keys* :combine-effects)]
        (apply operation effect-sets))
      (set (mapcat identity effect-sets)))))

(definterposable core-node
  :core-node
  [node-id kind syntax form data]
  (merge {:node-id (str "stage0-core-" node-id)
          :kind kind
          :form form
          :source-span (:span syntax)
          :generated-origin (:generated-origin syntax)
          :profile (:profile syntax)
          :namespace (:namespace syntax)
          :effects (form-effect form)
          :capabilities #{}}
         data))

(declare lower-core-expr)

(definterposable lower-sequential-body
  :lower-sequential-body
  [counter module syntax forms context]
  (mapv #(lower-core-expr counter module syntax % context) forms))

(definterposable extract-pattern-guard
  :extract-pattern-guard
  [pattern]
  (if (and (map? pattern) (contains? pattern :when))
    {:pattern (dissoc pattern :when)
     :guard (get pattern :when)}
    {:pattern pattern
     :guard nil}))

(definterposable lower-match-clauses
  :lower-match-clauses
  [counter module syntax clauses context]
  (when (odd? (count clauses))
    (fail! "L7-PATTERN-TYPE"
           "match requires pattern/expression clause pairs"
           {:source-span (:span syntax)
            :remediation "Use (match value pattern expr ...)."}))
  (mapv (fn [branch-index [raw-pattern raw-expr]]
          (let [{:keys [pattern guard]}
                (extract-pattern-guard raw-pattern)]
            {:branch-index branch-index
             :raw-pattern raw-pattern
             :pattern pattern
             :guard (when guard
                      (lower-core-expr counter module syntax guard context))
             :body (lower-core-expr counter module syntax raw-expr context)}))
        (range)
        (partition 2 clauses)))

(definterposable next-node-id
  :next-node-id
  [counter]
  (let [id @counter]
    (swap! counter inc)
    id))

(definterposable assert-recur-target!
  :assert-recur-target!
  [module syntax form context]
  (when (= 'recur (first form))
    (let [target-arity (:recur-arity context)
          actual-arity (count (rest form))]
      (when (or (nil? target-arity)
                (not= target-arity actual-arity))
        (fail! "L2-RECUR-TARGET"
               "recur has no compatible loop or function target"
               {:source-span (:span syntax)
                :form form
                :target-arity target-arity
                :actual-arity actual-arity
                :remediation "Use recur only inside a compatible loop or function recur point with matching arity."})))))

(definterposable assert-set-target!
  :assert-set-target!
  [module syntax form]
  (when (= 'set! (first form))
    (let [[_ target] form]
      (when-not (and (symbol? target)
                     (str/starts-with? (name target) "mutable-"))
        (fail! "L2-SET-ILLEGAL"
               "set! targets an immutable or profile-forbidden location"
               {:source-span (:span syntax)
                :target target
                :profile (:profile module)
                :remediation "Use an explicit mutable location accepted by the active profile."})))))

(definterposable assert-throw-legal!
  :assert-throw-legal!
  [module syntax form]
  (when (and (= 'throw (first form))
             (not (contains? (:effects module) :error/throw)))
    (fail! "L2-THROW-ILLEGAL"
           "throw requires an error effect in the namespace"
           {:source-span (:span syntax)
            :declared-effects (:effects module)
            :required-effect :error/throw
            :remediation "Declare :error/throw or lower to an explicit result value."})))

(definterposable assert-core-operator!
  :assert-core-operator!
  [module syntax form]
  (when (seq? form)
    (let [op (first form)]
      (cond
        (contains? (operation-value :unknown-reserved-core-forms
                                    unknown-reserved-core-forms)
                   op)
        (fail! "L2-UNKNOWN-CORE-FORM"
               "analyzer found an unrecognized reserved core form"
               {:source-span (:span syntax)
                :operator op
                :remediation "Use an L2 core form, a call, or a documented domain IR boundary."})

        (contains? (operation-value :lowering-gap-forms lowering-gap-forms)
                   op)
        (fail! "L2-LOWERING-GAP"
               "surface form failed to lower to core or a declared domain IR boundary"
               {:source-span (:span syntax)
                :operator op
                :remediation "Lower the surface form to L2 core before core analysis."})

        (= 'reorder-effects op)
        (fail! "L2-EVAL-ORDER"
               "transformation changed required evaluation order for effectful expressions"
               {:source-span (:span syntax)
                :operator op
                :remediation "Preserve left-to-right order for effectful expressions or prove purity before reordering."})

        (= 'host-exception op)
        (fail! "L2-HOST-SEMANTICS"
               "code depends on host behavior not represented in Gravity semantics"
               {:source-span (:span syntax)
                :operator op
                :remediation "Normalize host behavior into Gravity error, type, effect, and capability contracts."})

        :else nil))))

(definterposable lower-core-expr
  :lower-core-expr
  [counter module syntax form context]
  (let [id (next-node-id counter)]
    (cond
      (seq? form)
      (let [op (first form)]
        (assert-core-operator! module syntax form)
        (assert-recur-target! module syntax form context)
        (assert-set-target! module syntax form)
        (assert-throw-legal! module syntax form)
        (case op
          quote
          (core-node id :quote syntax form
                     {:value (second form)
                      :evaluation-order []})

          if
          (let [[_ test then else] form
                children [(lower-core-expr counter module syntax test context)
                          (lower-core-expr counter module syntax then context)
                          (lower-core-expr counter module syntax else context)]]
            (core-node id :if syntax form
                       {:children children
                        :evaluation-order [:condition :then-or-else]
                        :effects (apply combine-effects
                                        (form-effect form)
                                        (map :effects children))}))

          do
          (let [children (lower-sequential-body counter module syntax
                                                (rest form) context)]
            (core-node id :do syntax form
                       {:children children
                        :evaluation-order
                        (mapv (fn [idx] [:expr idx])
                              (range (count children)))
                        :effects (apply combine-effects
                                        (form-effect form)
                                        (map :effects children))}))

          let
          (let [[_ bindings & body] form
                binding-pairs (partition 2 bindings)
                binding-nodes
                (mapv (fn [[name expr]]
                        {:name name
                         :initializer
                         (lower-core-expr counter module syntax expr context)})
                      binding-pairs)
                body-nodes (lower-sequential-body counter module syntax body context)]
            (core-node id :let syntax form
                       {:bindings binding-nodes
                        :children body-nodes
                        :evaluation-order
                        (concat (mapv (fn [[name _]] [:binding name])
                                     binding-pairs)
                                (mapv (fn [idx] [:body idx])
                                      (range (count body-nodes))))
                        :effects
                        (apply combine-effects
                               (form-effect form)
                               (concat (map (comp :effects :initializer)
                                            binding-nodes)
                                       (map :effects body-nodes)))}))

          fn
          (let [[_ params & body] form
                body-nodes
                (lower-sequential-body
                 counter module syntax body
                 (assoc context :recur-arity (count params)))
                latent-effects (apply combine-effects (map :effects body-nodes))]
            (core-node id :fn syntax form
                       {:params params
                        :children body-nodes
                        :latent-effects latent-effects
                        :evaluation-order [:call-arguments-left-to-right]}))

          loop
          (let [[_ bindings & body] form
                recur-arity (/ (count bindings) 2)
                binding-pairs (partition 2 bindings)
                binding-nodes
                (mapv (fn [[name expr]]
                        {:name name
                         :initializer
                         (lower-core-expr counter module syntax expr context)})
                      binding-pairs)
                body-nodes
                (lower-sequential-body
                 counter module syntax body
                 (assoc context :recur-arity recur-arity))]
            (core-node id :loop syntax form
                       {:bindings binding-nodes
                        :recur-arity recur-arity
                        :children body-nodes
                        :evaluation-order
                        (concat (mapv (fn [[name _]] [:loop-binding name])
                                       binding-pairs)
                                (mapv (fn [idx] [:body idx])
                                      (range (count body-nodes))))}))

          recur
          (core-node id :recur syntax form
                     {:arguments
                      (lower-sequential-body counter module syntax
                                             (rest form) context)
                      :target-arity (:recur-arity context)
                      :evaluation-order [:arguments-left-to-right]})

          def
          (let [[_ name value] form
                value-node (lower-core-expr counter module syntax value context)]
            (core-node id :def syntax form
                       {:name name
                        :value value-node
                        :evaluation-order [:initializer]
                        :effects (:effects value-node)}))

          ;; Kept intentionally: this is a legacy L2 quirk.  defconst is not
          ;; in core-forms, yet the old case lowered it to a compile-time def.
          defconst
          (let [[_ name value] form
                value-node (lower-core-expr counter module syntax value context)]
            (core-node id :def syntax form
                       {:name name
                        :value value-node
                        :compile-time-binding? true
                        :evaluation-order [:compile-time-initializer]
                        :effects (:effects value-node)}))

          var
          (core-node id :var syntax form
                     {:name (second form)
                      :evaluation-order []})

          set!
          (let [[_ target value] form
                value-node (lower-core-expr counter module syntax value context)]
            (core-node id :set! syntax form
                       {:target target
                        :value value-node
                        :evaluation-order [:value]
                        :effects (combine-effects #{:state/write}
                                                  (:effects value-node))}))

          try
          (let [[_ body & handlers] form
                body-node (lower-core-expr counter module syntax body context)]
            (core-node id :try syntax form
                       {:body body-node
                        :handlers handlers
                        :evaluation-order [:body :matching-handler]
                        :effects (:effects body-node)}))

          throw
          (let [[_ value] form
                value-node (lower-core-expr counter module syntax value context)]
            (core-node id :throw syntax form
                       {:value value-node
                        :evaluation-order [:value]
                        :effects (combine-effects #{:error/throw}
                                                  (:effects value-node))}))

          match
          (let [[_ value & clauses] form
                value-node (lower-core-expr counter module syntax value context)
                lowered-clauses
                (lower-match-clauses counter module syntax clauses context)]
            (core-node id :match syntax form
                       {:value value-node
                        :clauses lowered-clauses
                        :evaluation-order [:scrutinee :selected-clause]
                        :effects
                        (apply combine-effects
                               (:effects value-node)
                               (concat (map #(get-in % [:guard :effects] #{})
                                            lowered-clauses)
                                       (map #(get-in % [:body :effects] #{})
                                            lowered-clauses)))}))

          ;; Any non-reserved list is a call.  Preserve the old operator/raw
          ;; child ordering and effect projection exactly.
          (let [children (lower-sequential-body counter module syntax form context)]
            (core-node id :call syntax form
                       {:operator op
                        :arguments (vec (rest children))
                        :evaluation-order [:operator :arguments-left-to-right]
                        :effects (apply combine-effects
                                        (form-effect form)
                                        (map :effects children))}))))

      (symbol? form)
      (core-node id :symbol syntax form
                 {:name form
                  :evaluation-order []})

      :else
      (core-node id :literal syntax form
                 {:value form
                  :evaluation-order []}))))

(definterposable flatten-core
  :flatten-core
  [node]
  (let [core-child? #(and (map? %) (:node-id %))
        children (filter core-child?
                         (concat (:children node)
                                 (keep :initializer (:bindings node))
                                 (when-let [v (:value node)] [v])
                                 (when-let [b (:body node)] [b])
                                 (:arguments node)
                                 (mapcat (fn [clause]
                                           (cond-> [(:body clause)]
                                             (:guard clause)
                                             (conj (:guard clause))))
                                         (:clauses node))))]
    (vec (cons node (mapcat flatten-core children)))))

(definterposable-private macro-source-artifact
  :macro-source-artifact
  [source-path source-text]
  ((unsupported-host-operation :macro-source-artifact)
   source-path source-text))

(definterposable core-source-artifact
  :core-source-artifact
  [source-path source-text]
  (let [macro-artifact (macro-source-artifact source-path source-text)
        module (:module macro-artifact)
        expanded-syntax (:expanded-syntax-object-stream macro-artifact)
        ;; Deliberately mirrors the seed's first-form namespace assumption.
        body-syntax (subvec expanded-syntax 1)
        counter (atom 0)
        roots (mapv #(lower-core-expr counter module % (:form %) {}) body-syntax)
        flat (vec (mapcat flatten-core roots))
        source-map (mapv #(select-keys % [:node-id :kind :source-span
                                          :generated-origin]) flat)
        form-kinds (mapv #(select-keys % [:node-id :kind :profile :namespace
                                          :effects :capabilities]) flat)
        evaluation (mapv #(select-keys % [:node-id :kind :evaluation-order])
                         (filter :evaluation-order flat))
        latent (mapv #(select-keys % [:node-id :params :latent-effects])
                     (filter #(= :fn (:kind %)) flat))
        calls (mapv #(select-keys % [:node-id :operator :arguments :effects])
                    (filter #(= :call (:kind %)) flat))]
    {:kind :gravity/stage0-core-artifact
     :pass {:name :core-lowering
            :input :expanded-syntax
            :output :core-ast
            :requires [:reader :namespace-analyzer]
            :preserves [:source-spans :generated-origin :profile :effects
                        :capabilities]
            :rejects ["L2-UNKNOWN-CORE-FORM" "L2-EVAL-ORDER"
                      "L2-RECUR-TARGET" "L2-SET-ILLEGAL" "L2-THROW-ILLEGAL"
                      "L2-HOST-SEMANTICS" "L2-LOWERING-GAP"]}
     :module (select-keys module [:module :source-path :profile :target :effects
                                  :capabilities :safety :metadata])
     :macro-expansion-trace (:macro-expansion-trace macro-artifact)
     :expanded-syntax-object-stream expanded-syntax
     :expanded-core-ast roots
     :core-node-source-map source-map
     :core-form-kind-records form-kinds
     :evaluation-order-metadata evaluation
     :latent-function-effect-records latent
     :call-records calls
     :diagnostics []}))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'call-entrypoint-body {:arglists '([operation-key operation args])}
   'core-ast-lowering-engine-contract {:arglists '([])}
   'core-forms {:kind :constant}
   'lowering-gap-forms {:kind :constant}
   'unknown-reserved-core-forms {:kind :constant}
   'form-effect {:arglists '([form])}
   'combine-effects {:arglists '([& effect-sets])}
   'core-node {:arglists '([node-id kind syntax form data])}
   'lower-sequential-body {:arglists '([counter module syntax forms context])}
   'extract-pattern-guard {:arglists '([pattern])}
   'lower-match-clauses {:arglists '([counter module syntax clauses context])}
   'next-node-id {:arglists '([counter])}
   'assert-recur-target! {:arglists '([module syntax form context])}
   'assert-set-target! {:arglists '([module syntax form])}
   'assert-throw-legal! {:arglists '([module syntax form])}
   'assert-core-operator! {:arglists '([module syntax form])}
   'lower-core-expr {:arglists '([counter module syntax form context])}
   'flatten-core {:arglists '([node])}
   'core-source-artifact {:arglists '([source-path source-text])}})

(defn core-ast-lowering-engine-contract
  []
  (assoc namespace-contract :public-api public-api))
