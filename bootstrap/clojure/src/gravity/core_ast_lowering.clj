(ns gravity.core-ast-lowering
  "Pure hosted Stage0 L2 core lowering compatibility facade."
  (:require [clojure.string :as str]
            [gravity.core-ast-lowering.artifact :as artifact]
            [gravity.core-ast-lowering.assertions :as assertions]
            [gravity.core-ast-lowering.contract :as contract]
            [gravity.core-ast-lowering.effects :as effects]
            [gravity.core-ast-lowering.expression :as expression]
            [gravity.core-ast-lowering.traversal :as traversal]))

;; Keep interposition state on the compatibility namespace. Captured facade
;; Vars and recursive calls therefore share the same dynamic operation scope.
(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})
(def ^:private function-operation-keys contract/function-operation-keys)
(def ^:private scalar-operation-keys contract/scalar-operation-keys)
(def ^:private operation-keys contract/operation-keys)
(def ^:private namespace-contract contract/namespace-contract)

(defn- default-fail! [id message data]
  (throw (ex-info message
                  (merge {:id id :message message :bootstrap-stage :stage0}
                         (or data {})))))
(defn- unsupported-host-operation [operation]
  (fn [& _]
    (throw (ex-info (str "L2 leaf requires injected operation " operation)
                    {:operation operation}))))
(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defmacro ^:private definterposable [name key arguments & body]
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
(defmacro ^:private definterposable-private [name key arguments & body]
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

(defn- operation-value [key default]
  (if (contains? *operations* key) (get *operations* key) default))
(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "L2 core lowering requires operation " key)
                    {:operation key}))))
(defn- valid-symbol-set? [value] (contract/valid-symbol-set? value))
(defn- validate-operations! [operations]
  (contract/validate-operations! operations))

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "L2 core lowering thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* (merge *operations* operations)] (thunk)))

(defn call-entrypoint-body
  "Invoke one captured original body with a one-shot bypass."
  [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "L2 core lowering entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "L2 core lowering entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "L2 core lowering entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys*
            (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys*
            (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(definterposable-private fail! :fail! [id message data]
  (default-fail! id message data))
(def core-forms '#{quote if do let fn loop recur def var set! try throw match})
(def lowering-gap-forms
  '#{defn when -> cond case with-open with-region defmacro defschema defworkflow
     defagent ui query ai-form})
(def unknown-reserved-core-forms '#{core-unknown})
(definterposable-private uses-println? :uses-println? [form]
  (effects/uses-println? uses-println? form))
(definterposable form-effect :form-effect [form]
  (effects/form-effect uses-println? form))

(defn combine-effects [& effect-sets]
  (if (contains? *bypass-next-operation-keys* :combine-effects)
    (binding [*bypass-next-operation-keys*
              (disj *bypass-next-operation-keys* :combine-effects)]
      (effects/combine-effects effect-sets))
    (if-let [operation (current-operation :combine-effects)]
      (binding [*active-operation-keys*
                (conj *active-operation-keys* :combine-effects)]
        (apply operation effect-sets))
      (effects/combine-effects effect-sets))))

(definterposable core-node :core-node [node-id kind syntax form data]
  (effects/core-node form-effect node-id kind syntax form data))
(declare lower-core-expr)
(definterposable lower-sequential-body :lower-sequential-body
  [counter module syntax forms context]
  (traversal/lower-sequential-body
   lower-core-expr counter module syntax forms context))
(definterposable extract-pattern-guard :extract-pattern-guard [pattern]
  (traversal/extract-pattern-guard pattern))
(definterposable lower-match-clauses :lower-match-clauses
  [counter module syntax clauses context]
  (traversal/lower-match-clauses
   fail! extract-pattern-guard lower-core-expr
   counter module syntax clauses context))
(definterposable next-node-id :next-node-id [counter]
  (traversal/next-node-id counter))
(definterposable assert-recur-target! :assert-recur-target!
  [module syntax form context]
  (assertions/assert-recur-target! fail! module syntax form context))
(definterposable assert-set-target! :assert-set-target! [module syntax form]
  (assertions/assert-set-target! fail! module syntax form))
(definterposable assert-throw-legal! :assert-throw-legal! [module syntax form]
  (assertions/assert-throw-legal! fail! module syntax form))
(definterposable assert-core-operator! :assert-core-operator!
  [module syntax form]
  (assertions/assert-core-operator!
   fail! operation-value lowering-gap-forms unknown-reserved-core-forms
   module syntax form))

(definterposable lower-core-expr :lower-core-expr
  [counter module syntax form context]
  (expression/lower-core-expr
   {:next-node-id next-node-id
    :assert-core-operator! assert-core-operator!
    :assert-recur-target! assert-recur-target!
    :assert-set-target! assert-set-target!
    :assert-throw-legal! assert-throw-legal!
    :core-node core-node
    :lower-core-expr lower-core-expr
    :lower-sequential-body lower-sequential-body
    :lower-match-clauses lower-match-clauses
    :combine-effects combine-effects
    :form-effect form-effect}
   counter module syntax form context))
(definterposable flatten-core :flatten-core [node]
  (traversal/flatten-core flatten-core node))
(definterposable-private macro-source-artifact :macro-source-artifact
  [source-path source-text]
  ((unsupported-host-operation :macro-source-artifact) source-path source-text))
(definterposable core-source-artifact :core-source-artifact
  [source-path source-text]
  (artifact/core-source-artifact
   {:macro-source-artifact macro-source-artifact
    :lower-core-expr lower-core-expr
    :flatten-core flatten-core}
   source-path source-text))

(def public-api contract/public-api)
(defn core-ast-lowering-engine-contract []
  (assoc namespace-contract :public-api public-api))
