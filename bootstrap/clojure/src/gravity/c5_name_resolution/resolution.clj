(ns gravity.c5-name-resolution.resolution
  (:require [clojure.string :as str]
            [gravity.c5-name-resolution.bindings :as bindings]
            [gravity.c5-name-resolution.config :as config]
            [gravity.c5-name-resolution.operations :as ops]))

(defn c5-bindings-by-name [bindings]
  (reduce (fn [acc binding] (update acc (:name binding) (fnil conj []) binding)) {} bindings))

(defn c5-resolve-qualified-symbol [module alias-map dependency-map sym]
  (let [ns-part (namespace sym) local-name (symbol (name sym)) alias-sym (symbol ns-part)]
    (cond
      (contains? alias-map alias-sym)
      {:resolution-kind :alias-qualified
       :binding ((ops/op-fn :c5-import-binding bindings/c5-import-binding)
                 module (get dependency-map alias-sym) local-name (:module module))}

      (= ns-part (str (:module module)))
      {:resolution-kind :fully-qualified
       :binding ((ops/op-fn :c5-binding-identity bindings/c5-binding-identity)
                 {:name local-name :kind :var :namespace (:module module)
                  :package ((ops/op-fn :c5-package-record bindings/c5-package-record) module)
                  :visibility :public :profile-set #{(:profile module)} :target-set #{(:target module)}
                  :type-ref :gravity.type/value :effects #{} :capabilities #{} :safety (:safety module)
                  :source-span (ops/source-span (:source-path module) 0) :artifact (:module module)})}

      (str/includes? ns-part ".")
      {:resolution-kind :fully-qualified
       :binding ((ops/op-fn :c5-binding-identity bindings/c5-binding-identity)
                 {:name local-name :kind :var :namespace (symbol ns-part)
                  :package {:name (symbol ns-part) :version "stage0"} :visibility :public
                  :profile-set (ops/op-value :known-source-profiles config/known-source-profiles)
                  :target-set #{(:target module)} :type-ref :gravity.type/qualified-var
                  :effects #{} :capabilities #{} :safety :safe
                  :source-span (ops/source-span (:source-path module) 0) :artifact (symbol ns-part)})}
      :else nil)))

(defn c5-resolution-record [module bindings-by-name alias-map dependency-map local-bindings syntax idx sym]
  (let [local-by-name ((ops/op-fn :c5-bindings-by-name c5-bindings-by-name) local-bindings)
        qualified? (namespace sym)
        resolved (if qualified?
                   ((ops/op-fn :c5-resolve-qualified-symbol c5-resolve-qualified-symbol)
                    module alias-map dependency-map sym)
                   (cond
                     (contains? local-by-name sym) {:resolution-kind :local :binding (first (get local-by-name sym))}
                     (contains? bindings-by-name sym) {:resolution-kind :namespace :binding (first (get bindings-by-name sym))}
                     (contains? (ops/op-value :c5-special-form-symbols config/c5-special-form-symbols) sym)
                     {:resolution-kind :special-form :binding ((ops/op-fn :c5-special-form-binding bindings/c5-special-form-binding) sym module)}
                     (contains? (ops/op-value :c5-core-auto-imports config/c5-core-auto-imports) sym)
                     {:resolution-kind :core-auto-import :binding ((ops/op-fn :c5-core-binding bindings/c5-core-binding) sym module)}
                     (contains? (ops/op-value :c5-type-auto-imports config/c5-type-auto-imports) sym)
                     {:resolution-kind :type-position :binding ((ops/op-fn :c5-type-binding bindings/c5-type-binding) sym module)}
                     :else nil))]
    (when resolved
      {:syntax-id (:syntax-id syntax) :symbol-index idx :symbol sym
       :position (cond
                   (contains? (ops/op-value :c5-special-form-symbols config/c5-special-form-symbols) sym) :special-form
                   (contains? (ops/op-value :c5-type-auto-imports config/c5-type-auto-imports) sym) :type
                   qualified? :expression :else :expression)
       :resolution-order (:resolution-kind resolved) :binding-id (get-in resolved [:binding :binding-id])
       :binding (select-keys (:binding resolved) [:binding-id :name :kind :namespace :visibility
                                                  :profile-set :target-set :effects :capabilities :safety])})))

(defn c5-binding-table [module definition-bindings macro-bindings lexical-scope-graph expanded-stream]
  (let [namespace-bindings (vec (concat definition-bindings macro-bindings))
        bindings-by-name ((ops/op-fn :c5-bindings-by-name c5-bindings-by-name) namespace-bindings)
        dependencies (concat (:requires module) (:imports module))
        alias-map (into {} (map (juxt :alias identity) (filter :alias dependencies)))
        locals (vec (mapcat :bindings (:scopes lexical-scope-graph)))]
    {:artifact :gravity/c5-binding-table
     :bindings (vec (keep-indexed
                     (fn [idx [syntax sym]]
                       ((ops/op-fn :c5-resolution-record c5-resolution-record)
                        module bindings-by-name alias-map alias-map locals syntax idx sym))
                     (mapcat (fn [syntax]
                               (map (fn [sym] [syntax sym])
                                    (ops/collect-code-symbols (:form syntax))))
                             (remove #(ops/ns-form? (:form %)) expanded-stream))))
     :namespace-bindings namespace-bindings :local-bindings locals :status :complete}))
