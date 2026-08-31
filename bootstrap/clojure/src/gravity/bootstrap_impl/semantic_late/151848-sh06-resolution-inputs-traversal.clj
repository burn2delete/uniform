; Semantic decomposition of committed HEAD reader line 151848.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh06-resolution-analysis-inputs-make-traversal
 [parameter-names add-reference! add-scope!]
 (clojure.core/letfn
  [(walk-form
    [value scope-chain syntax position]
    (cond
     (symbol? value)
     (add-reference! value scope-chain syntax position (:span syntax))
     (seq? value)
     (let
      [operator (first value)]
      (cond
       (= operator 'quote)
       (add-reference! 'quote scope-chain syntax :operator (:span syntax))
       (contains? '#{syntax-quote} operator)
       (add-reference! operator scope-chain syntax :operator (:span syntax))
       (contains? '#{defconst def} operator)
       (do
        (add-reference! operator scope-chain syntax :operator (:span syntax))
        (doseq [item (drop 2 value)] (walk-form item scope-chain syntax :expression)))
       (= operator 'defn)
       (let
        [parameters
         (nth value 2 [])
         names
         (parameter-names parameters)
         scope-id
         (add-scope! (:syntax/id syntax) (first scope-chain) names (:span syntax))]
        (add-reference! 'defn scope-chain syntax :operator (:span syntax))
        (doseq
         [item (drop 3 value)]
         (walk-form item (cons scope-id scope-chain) syntax :expression)))
       (= operator 'fn)
       (let
        [named?
         (symbol? (second value))
         parameters
         (if named? (nth value 2 []) (second value))
         body
         (if named? (drop 3 value) (drop 2 value))
         names
         (cond-> (parameter-names parameters) named? (conj (second value)))
         scope-id
         (add-scope! (:syntax/id syntax) (first scope-chain) names (:span syntax))]
        (add-reference! 'fn scope-chain syntax :operator (:span syntax))
        (doseq [item body] (walk-form item (cons scope-id scope-chain) syntax :expression)))
       (= operator 'match)
       (do
        (add-reference! 'match scope-chain syntax :operator (:span syntax))
        (when-let [scrutinee (second value)] (walk-form scrutinee scope-chain syntax :expression))
        (doseq
         [[pattern branch] (partition 2 (drop 2 value))]
         (cond
          (and (symbol? pattern) (not= '_ pattern))
          (let
           [scope-id (add-scope! (:syntax/id syntax) (first scope-chain) [pattern] (:span syntax))]
           (walk-form branch (cons scope-id scope-chain) syntax :expression))
          (and (vector? pattern) (sh06-fixed-vector-pattern? pattern))
          (when-let
           [binding-paths (sh06-unique-match-binding-paths pattern [])]
           (if
            (empty? binding-paths)
            (walk-form branch scope-chain syntax :expression)
            (let
             [scope-id
              (add-scope!
               (:syntax/id syntax)
               (first scope-chain)
               (mapv :name binding-paths)
               (:span syntax))]
             (walk-form branch (cons scope-id scope-chain) syntax :expression))))
          (or
           (= '_ pattern)
           (nil? pattern)
           (true? pattern)
           (false? pattern)
           (number? pattern)
           (char? pattern)
           (string? pattern)
           (keyword? pattern))
          (walk-form branch scope-chain syntax :expression)
          :else
          nil)))
       (= operator 'try)
       (do
        (add-reference! 'try scope-chain syntax :operator (:span syntax))
        (doseq
         [item (rest value)]
         (cond
          (and (seq? item) (= 'catch (first item)))
          (do
           (add-reference! 'catch scope-chain syntax :operator (:span syntax))
           (when-let [error-type (second item)] (walk-form error-type scope-chain syntax :type))
           (let
            [binding-name (nth item 2 nil)]
            (when
             (symbol? binding-name)
             (let
              [scope-id
               (add-scope! (:syntax/id syntax) (first scope-chain) [binding-name] (:span syntax))
               nested
               (cons scope-id scope-chain)]
              (doseq
               [handler-form (drop 3 item)]
               (walk-form handler-form nested syntax :expression))))))
          (and (seq? item) (= 'finally (first item)))
          (do
           (add-reference! 'finally scope-chain syntax :operator (:span syntax))
           (doseq
            [cleanup-form (rest item)]
            (walk-form cleanup-form scope-chain syntax :expression)))
          :else
          (walk-form item scope-chain syntax :expression))))
       (contains? '#{let loop} operator)
       (let
        [binding-vector
         (second value)
         pairs
         (if (vector? binding-vector) (partition 2 binding-vector) [])
         _
         (add-reference! operator scope-chain syntax :operator (:span syntax))
         nested
         (loop
          [remaining pairs active-scope-chain scope-chain]
          (if
           (empty? remaining)
           active-scope-chain
           (let
            [[binding-name initializer]
             (first remaining)
             _
             (walk-form initializer active-scope-chain syntax :expression)
             next-scope-chain
             (if
              (symbol? binding-name)
              (let
               [scope-id
                (add-scope!
                 (:syntax/id syntax)
                 (first active-scope-chain)
                 [binding-name]
                 (:span syntax))]
               (cons scope-id active-scope-chain))
              active-scope-chain)]
            (recur (next remaining) next-scope-chain))))]
        (doseq [item (drop 2 value)] (walk-form item nested syntax :expression)))
       :else
       (doseq
        [[index item] (map-indexed vector value)]
        (walk-form item scope-chain syntax (if (zero? index) :operator position)))))
     (map? value)
     (doseq
      [[key item] value]
      (walk-form key scope-chain syntax :expression)
      (walk-form item scope-chain syntax :expression))
     (coll? value)
     (doseq [item value] (walk-form item scope-chain syntax position))
     :else
     nil))]
  walk-form))
