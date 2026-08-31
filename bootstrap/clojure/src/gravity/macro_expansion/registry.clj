(ns gravity.macro-expansion.registry
  (:require [gravity.macro-expansion.builtins :as builtins]
            [gravity.macro-expansion.operations :as operations]
            [gravity.macro-expansion.templates :as templates]))

(defn parse-defmacro-form
  [module syntax ops]
  (let [form (:form syntax)
        [_ name & tail] form
        [metadata tail] (if (map? (first tail))
                          [(first tail) (rest tail)]
                          [{} tail])
        params (first tail)
        body (vec (rest tail))]
    (when-not (symbol? name)
      (operations/fail!
       ops "L4-MACRO-NOT-SYNTAX" "defmacro requires a symbolic name"
       {:source-span (:span syntax)
        :form form
        :remediation "Use (defmacro name [args] (syntax-quote ...))."}))
    (when-not (vector? params)
      (operations/fail!
       ops "L4-MACRO-NOT-SYNTAX" "defmacro requires a parameter vector"
       {:source-span (:span syntax)
        :macro name
        :remediation "Use a vector parameter list."}))
    (let [identity ((operations/operation ops :local-macro-symbol
                                          operations/local-macro-symbol)
                    module name)
          parse-params (operations/operation
                        ops :parse-param-list
                        (fn [values] (templates/parse-param-list values ops)))
          macro {:name name
                 :identity identity
                 :kind :source
                 :version (or (:version metadata) "stage0-source")
                 :macro-namespace (:module module)
                 :params (parse-params params)
                 :source-span (:span syntax)
                 :metadata metadata
                 :body body
                 :build-effects (or (:build-effects metadata) #{})
                 :uses-build-effects (or (:uses-build-effects metadata) #{})
                 :required-build-capabilities
                 (or (:required-build-capabilities metadata) #{})
                 :hygiene-policy (or (:hygiene-policy metadata) :hygienic)
                 :output-contract (or (:output-contract metadata)
                                      :gravity-syntax)
                 :allow-unsafe? (true? (:allow-unsafe metadata))
                 :omit-generated-origin?
                 (true? (:omit-generated-origin metadata))}]
      [name identity macro])))

(defn macro-registry
  [module syntax builtin-macros ops]
  (let [form-op (operations/operation ops :form-op? operations/form-op?)
        parse-defmacro (operations/operation
                        ops :parse-defmacro-form
                        (fn [m s] (parse-defmacro-form m s ops)))
        built-ins (operations/operation
                   ops :built-in-registry
                   (fn [] (builtins/registry builtin-macros ops)))]
    (reduce (fn [registry syntax-object]
              (if (form-op 'defmacro (:form syntax-object))
                (let [[name identity macro]
                      (parse-defmacro module syntax-object)]
                  (assoc registry name macro identity macro))
                registry))
            (built-ins)
            syntax)))

(defn namespace-entry
  [macro]
  (select-keys macro [:name :identity :kind :version :macro-namespace :params
                      :build-effects :required-build-capabilities
                      :hygiene-policy :output-contract :source-span]))

(defn build-effect-record
  [macro]
  {:macro (:identity macro)
   :declared-build-effects (:build-effects macro)
   :used-build-effects (or (:uses-build-effects macro)
                           (:build-effects macro)
                           #{})
   :required-build-capabilities (:required-build-capabilities macro)})

(defn build-grants
  [module]
  (or (get-in module [:metadata :build-grants]) #{}))
