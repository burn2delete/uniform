(ns hooks.c2
  "Static node model of the separately pinned C2 wrapper; never evaluates source."
  (:require [clj-kondo.hooks-api :as api]))

(defn definterposable [{:keys [node]}]
  (let [[head name key arguments & body] (:children node)
        name-value (when name (api/sexpr name))
        argument-values (when (api/vector-node? arguments) (mapv api/sexpr (:children arguments)))
        plain-symbol? #(and (symbol? %) (nil? (namespace %)) (not= '& %))]
    (if-not (and (plain-symbol? name-value) key (keyword? (api/sexpr key))
                 (api/vector-node? arguments) (every? plain-symbol? argument-values)
                 (= (count argument-values) (count (distinct argument-values))))
      (do (api/reg-finding! (assoc (meta node) :type :ci/unsupported-interposable
                                   :message "Unsupported definterposable invocation; update the reviewed model"))
          {:node node})
      (let [token #(with-meta (api/token-node %) (meta head))
            list #(with-meta (api/list-node %) (meta node))
            call (fn [function & args] (list (cons (token function) args)))
            vector #(with-meta (api/vector-node %) (meta node))
            operation (token (gensym "operation__ci__"))
            bypass (token 'gravity.c2-artifact-identity/*bypass-next-operation-keys*)
            active (token 'gravity.c2-artifact-identity/*active-operation-keys*)
            body-node (list (cons (token 'do) body))]
        {:node
         (call 'clojure.core/defn name arguments
               (call 'if
                     (call 'clojure.core/contains? bypass key)
                     (call 'clojure.core/binding
                           (vector [bypass (call 'clojure.core/disj bypass key)]) body-node)
                     (call 'clojure.core/if-let
                           (vector [operation (call 'gravity.c2-artifact-identity/current-operation key)])
                           (call 'clojure.core/binding
                                 (vector [active (call 'clojure.core/conj active key)])
                                 (list (cons operation (:children arguments))))
                           body-node)))}))))
