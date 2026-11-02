(ns hooks.module-analysis
  "Static node model of the reviewed module-analysis interposition macros."
  (:require [clj-kondo.hooks-api :as api]))

(defn- plain-symbol?
  [value]
  (and (symbol? value) (nil? (namespace value)) (not= '& value)))

(defn- model-definterposable
  [{:keys [node]} private?]
  (let [[head name key arguments & body] (:children node)
        name-value (when name (api/sexpr name))
        argument-values (when (api/vector-node? arguments)
                          (mapv api/sexpr (:children arguments)))]
    (if-not (and (plain-symbol? name-value)
                 key
                 (keyword? (api/sexpr key))
                 (api/vector-node? arguments)
                 (every? plain-symbol? argument-values)
                 (= (count argument-values) (count (distinct argument-values))))
      (do
        (api/reg-finding!
         (assoc (meta node)
                :type :ci/unsupported-interposable
                :message "Unsupported module-analysis definterposable invocation; update the reviewed model"))
        {:node node})
      (let [token #(with-meta (api/token-node %) (meta head))
            list #(with-meta (api/list-node %) (meta node))
            call (fn [function & args] (list (cons (token function) args)))
            vector #(with-meta (api/vector-node %) (meta node))
            operation (token (gensym "operation__ci__"))
            bypass (token 'gravity.module-analysis/*bypass-next-operation-keys*)
            active (token 'gravity.module-analysis/*active-operation-keys*)
            body-node (list (cons (token 'do) body))
            definition (if private? 'clojure.core/defn- 'clojure.core/defn)]
        {:node
         (call definition name arguments
               (call 'if
                     (call 'clojure.core/contains? bypass key)
                     (call 'clojure.core/binding
                           (vector [bypass (call 'clojure.core/disj bypass key)]) body-node)
                     (call 'clojure.core/if-let
                           (vector [operation (call 'gravity.module-analysis/current-operation key)])
                           (call 'clojure.core/binding
                                 (vector [active (call 'clojure.core/conj active key)])
                                 (list (cons operation (:children arguments))))
                           body-node)))}))))

(defn definterposable
  [context]
  (model-definterposable context false))

(defn definterposable-private
  [context]
  (model-definterposable context true))
