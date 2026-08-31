(ns gravity.core-ast-lowering.contract
  "Stable facade contract and operation policy for hosted Stage0 L2.")

(def function-operation-keys
  #{:fail! :macro-source-artifact :uses-println? :form-effect
    :combine-effects :core-node :lower-sequential-body
    :extract-pattern-guard :lower-match-clauses :next-node-id
    :assert-recur-target! :assert-set-target! :assert-throw-legal!
    :assert-core-operator! :lower-core-expr :flatten-core
    :core-source-artifact})

(def scalar-operation-keys
  #{:core-forms :lowering-gap-forms :unknown-reserved-core-forms})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

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
   'lower-match-clauses
   {:arglists '([counter module syntax clauses context])}
   'next-node-id {:arglists '([counter])}
   'assert-recur-target! {:arglists '([module syntax form context])}
   'assert-set-target! {:arglists '([module syntax form])}
   'assert-throw-legal! {:arglists '([module syntax form])}
   'assert-core-operator! {:arglists '([module syntax form])}
   'lower-core-expr {:arglists '([counter module syntax form context])}
   'flatten-core {:arglists '([node])}
   'core-source-artifact {:arglists '([source-path source-text])}})

(def namespace-contract
  {:namespace 'gravity.core-ast-lowering
   :contract-boundary :hosted-stage0-l2-core-ast-lowering
   :public-api :bootstrap-compatible-l2-vars
   :leaf-only-api ['core-source-artifact]
   :owns [:hosted-stage0-l2-core-lowering-algorithm
          :hosted-stage0-l2-core-artifact-projection]
   :dependency-direction
   {:requires ['clojure.core 'clojure.string]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:source-acquisition :source-reading
                  :macro-source-authority :canonical-l2-authority
                  :canonical-c6-authority :canonical-sh07-authority
                  :target-selection :target-lowering :backend
                  :proof-authority :equivalence :self-hosting :release
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

(defn valid-symbol-set?
  [value]
  (and (set? value) (seq value) (every? symbol? value)))

(defn validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "L2 core lowering operation map must be a map"
                    {:value operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))
        invalid-functions
        (vec (for [[key value]
                   (select-keys operations function-operation-keys)
                   :when (not (fn? value))]
               key))]
    (when (seq unknown)
      (throw (ex-info "L2 core lowering operation map contains unknown keys"
                      {:unknown-keys unknown :allowed-keys operation-keys})))
    (when (seq invalid-functions)
      (throw
       (ex-info "L2 core lowering function operation values must be functions"
                {:non-function-keys invalid-functions}))))
  (doseq [[key value] operations
          :when (and (contains? scalar-operation-keys key)
                     (not (valid-symbol-set? value)))]
    (throw (ex-info "L2 core lowering scalar operation has an invalid shape"
                    {:key key :expected :non-empty-symbol-set :actual value})))
  operations)
