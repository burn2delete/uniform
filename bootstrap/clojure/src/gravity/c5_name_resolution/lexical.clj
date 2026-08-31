(ns gravity.c5-name-resolution.lexical
  (:require [gravity.c5-name-resolution.bindings :as bindings]
            [gravity.c5-name-resolution.operations :as ops]))

(defn c5-param-symbols [params]
  (loop [items (seq params) symbols []]
    (cond
      (nil? items) symbols
      (= ':- (first items)) (recur (nnext items) symbols)
      (and (symbol? (first items)) (= ':- (second items)))
      (recur (nnext (next items)) (conj symbols (first items)))
      (symbol? (first items)) (recur (next items) (conj symbols (first items)))
      :else (recur (next items) symbols))))

(defn c5-local-bindings-from-params [module form syntax-id]
  (when (and (seq? form) (= 'defn (first form)))
    (let [fn-name (second form) params (nth form 2 [])
          param-symbols ((ops/op-fn :c5-param-symbols c5-param-symbols) params)]
      (mapv (fn [idx sym]
              ((ops/op-fn :c5-binding-identity bindings/c5-binding-identity)
               {:name sym :kind :local :namespace (:module module)
                :package ((ops/op-fn :c5-package-record bindings/c5-package-record) module)
                :visibility :lexical :profile-set #{(:profile module)} :target-set #{(:target module)}
                :type-ref :gravity.type/local :effects #{} :capabilities #{} :safety (:safety module)
                :source-span {:source (:source-path module) :function fn-name :param-index idx}
                :artifact syntax-id}))
            (range) param-symbols))))

(defn c5-let-binding-symbols [form]
  (letfn [(walk [value]
            (cond
              (and (seq? value) (= 'let (first value)) (vector? (second value)))
              (let [names (->> (partition 2 (second value)) (map first) (filter symbol?))]
                (concat names (mapcat walk (drop 2 value))))
              (seq? value) (mapcat walk value)
              (coll? value) (mapcat walk value)
              :else []))]
    (vec (walk form))))

(defn- let-binding [module syntax-id span idx sym]
  ((ops/op-fn :c5-binding-identity bindings/c5-binding-identity)
   {:name sym :kind :local :namespace (:module module)
    :package ((ops/op-fn :c5-package-record bindings/c5-package-record) module)
    :visibility :lexical :profile-set #{(:profile module)} :target-set #{(:target module)}
    :type-ref :gravity.type/local :effects #{} :capabilities #{} :safety (:safety module)
    :source-span span :artifact syntax-id}))

(defn c5-local-scope-graph [module expanded-stream]
  (let [scopes (vec (mapcat (fn [syntax]
                              (let [form (:form syntax) syntax-id (:syntax-id syntax)
                                    params (or ((ops/op-fn :c5-local-bindings-from-params
                                                            c5-local-bindings-from-params)
                                               module form syntax-id) [])
                                    lets (mapv #(let-binding module syntax-id (:span syntax) %1 %2)
                                               (range)
                                               ((ops/op-fn :c5-let-binding-symbols c5-let-binding-symbols) form))]
                                (when (seq (concat params lets))
                                  [{:scope-id (str "scope/" syntax-id) :owner-syntax-id syntax-id
                                    :namespace (:module module) :bindings (vec (concat params lets))
                                    :parent :namespace-root}]))) expanded-stream))]
    {:artifact :gravity/c5-lexical-scope-graph
     :root {:scope-id :namespace-root :namespace (:module module)}
     :scopes scopes :status :complete}))
