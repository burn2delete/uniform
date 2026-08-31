

(defn parse-typed-params
  [params]
  (loop [items (seq params)
         parsed []]
    (cond
      (nil? items) parsed

      (= '& (first items))
      (let [rest-name (second items)]
        (when-not (symbol? rest-name)
          (fail! "L5-ANNOTATION-REQUIRED"
                 "rest parameter requires a symbolic name"
                 {:params params
                  :remediation "Use a symbol after & in parameter vectors."}))
        (recur (nnext items)
               (conj parsed {:name rest-name :type "Dynamic" :rest? true})))

      (symbol? (first items))
      (let [name (first items)
            maybe-marker (second items)
            maybe-type (nth items 2 nil)]
        (if (= ':- maybe-marker)
          (recur (nnext (next items))
                 (conj parsed {:name name :type (type-form-name maybe-type) :declared? true}))
          (recur (next items)
                 (conj parsed {:name name :type "Dynamic"}))))

      :else
      (fail! "L5-ANNOTATION-REQUIRED"
             "function parameter requires a symbol and optional :- type annotation"
             {:params params
              :remediation "Use parameters such as [value] or [value :- String]."}))))