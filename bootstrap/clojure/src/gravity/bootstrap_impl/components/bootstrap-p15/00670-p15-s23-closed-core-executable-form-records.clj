

(defn p15-s23-closed-core-executable-form-records
  "Return the single checked-core C2 executable preorder.

  Function bodies and each operation's evaluated children are visited
  left-to-right.  Quote itself is executable but its payload is data, so quote
  records are terminal."
  [form-tree top-level-form-ids]
  (let [limit (inc p15-s23-closed-core-max-plan-nodes)
        form-by-id (into {} (map (juxt :form-id identity)) form-tree)
        function-root (get form-by-id (second top-level-form-ids))
        body-form-ids
        (if (and (seq? (:value function-root))
                 (= 'defn (first (:value function-root))))
          (vec (take limit (drop 3 (:children function-root))))
          [])]
    (loop [pending (vec (reverse body-form-ids))
           ordered []
           seen #{}]
      (if-let [form-id (peek pending)]
        (let [pending (pop pending)
              record (get form-by-id form-id)]
          (if (or (nil? record) (contains? seen form-id))
            (recur pending ordered seen)
            (let [value (:value record)
                  operator (when (seq? value) (first value))
                  child-ids
                  (if (seq? value)
                    (case operator
                      quote []
                      (do if str println) (rest (:children record))
                      let
                      (let [binding-record
                            (get form-by-id (second (:children record)))]
                        (if (vector? (:value binding-record))
                          (concat
                           (take-nth 2 (rest (:children binding-record)))
                           (drop 2 (:children record)))
                          []))
                      [])
                    [])
                  next-ordered (conj ordered record)
                  remaining-capacity (- limit (count next-ordered))]
              (if (zero? remaining-capacity)
                next-ordered
                (recur
                 (into pending
                       (reverse (vec (take remaining-capacity child-ids))))
                 next-ordered
                 (conj seen form-id))))))
        ordered))))

(defn p15-s23-closed-core-source-surface-validation
  [forms executable-form-records]
  (let [namespace-form (first forms)
        function-form (second forms)
        top-level-valid?
        (and (= 2 (count forms))
             (seq? namespace-form)
             (= 'ns (first namespace-form))
             (seq? function-form)
             (= 'defn (first function-form))
             (= 'main (second function-form))
             (vector? (nth function-form 2 nil))
             (empty? (nth function-form 2 nil)))]
    (if-not top-level-valid?
      {:status :unsupported
       :missing-fact :exact-ns-and-single-zero-arity-main-source-shape}
      (loop [records executable-form-records
             visited 0]
        (if-let [record (first records)]
          (let [form (:value record)
                operator (when (seq? form) (first form))
                visited (inc visited)
                base {:operator operator
                      :observed-form form
                      :offending-form-id (:form-id record)}]
            (cond
              (> visited p15-s23-closed-core-max-plan-nodes)
              {:status :over-limit
               :missing-fact :bounded-pure-source-form-surface
               :observed-source-forms visited
               :offending-form-id (:form-id record)}

              (or (nil? form) (string? form) (boolean? form)
                  (number? form) (char? form) (keyword? form)
                  (symbol? form)
                  (and (map? form)
                       (= :gravity/deferred-ratio-literal
                          (:artifact form))))
              (recur (next records) visited)

              (seq? form)
              (cond
                (= 'quote operator)
                (if (= 2 (count form))
                  (recur (next records) visited)
                  (merge base
                         {:status :unsupported
                          :missing-fact :pure-quote-source-arity
                          :observed-arity (dec (count form))}))

                (= 'let operator)
                (let [bindings (second form)]
                  (if (and (vector? bindings)
                           (even? (count bindings))
                           (every? symbol? (take-nth 2 bindings))
                           (= (count (take-nth 2 bindings))
                              (count (set (take-nth 2 bindings)))))
                    (recur (next records) visited)
                    (merge base
                           {:status :unsupported
                            :missing-fact :pure-let-binding-source-shape})))

                (contains? #{'do 'if 'str 'println} operator)
                (recur (next records) visited)

                :else
                (merge base
                       {:status :unsupported
                        :missing-fact :pure-closed-source-operation}))

              :else
              (merge base
                     {:status :unsupported
                      :missing-fact :pure-closed-source-expression-kind
                      :observed-class (some-> form class .getName)})))
          {:status :passed :observed-source-forms visited})))))

(def p15-s23-closed-core-node-keys
  #{:node-id :path :kind :source-operation :plan-node? :plan-depth
    :operands :attributes :type :effects :capabilities :ownership :safety
    :profile :source})

(def p15-s23-closed-core-artifact-keys
  #{:kind :artifact-id :mapping-id :status :scope :source-content-hash
    :source-core-input :entrypoint :profile :source-target
    :target-request-metadata :core-nodes :root-node-ids :type-facts
    :effect-facts :capability-facts :ownership-facts :safety-facts
    :profile-facts :typed-core :effect-graph :capability-proof-records
    :pure-capability-closure :ownership-analysis
    :dependency-order-graph :lexical-binding-records
    :source-origin-table :origin-closure :pass-history
    :authenticated-input :bounds :provenance :diagnostics
    :provenance-binding-id :actual-path-binding-id
    :instruction-origin-sidecar
    :mir-derived? :whole-language? :clojure-seed-boundary? :self-hosted?})

(def p15-s23-closed-core-origin-closure-keys
  #{:origin-id :actual-source-path :c2-source-id :c2-form-id
    :c2-open-token-id :c2-close-token-id :c2-span :c2-surface-span
    :c2-form-kind :c2-abbrev :c2-reader-generated-origin
    :c3-syntax-id :c3-source :c3-origin :expanded-generated-origin
    :generated-role :input-origin-id :provenance-binding-hash
    :actual-path-binding-hash})