; Semantic decomposition of committed HEAD reader line 151848.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh06-resolution-analysis-inputs-parameter-names
 [parameters]
 (loop
  [remaining (seq (if (sequential? parameters) parameters [])) names []]
  (if
   (empty? remaining)
   names
   (let
    [item (first remaining)]
    (cond
     (= item ':-)
     (recur (nnext remaining) names)
     (= item '&)
     (recur (next remaining) names)
     (symbol? item)
     (recur (next remaining) (conj names item))
     :else
     (recur (next remaining) names))))))
