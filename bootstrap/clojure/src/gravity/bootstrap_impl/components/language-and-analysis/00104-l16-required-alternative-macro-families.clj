

(def l16-required-alternative-macro-families
  [:provider :expansion-trace :syntax-object :hygiene :explicit-capture
   :build-effect :cache :equivalence :facet-aware :generated-validation
   :phase-boundary :source-span :generated-origin :reference-l4])

(defn alternative-macro-conformance-fixture
  [checker-state]
  (let [providers (:alternative-macro-provider-declarations checker-state)
        traces (:alternative-macro-expansion-traces checker-state)
        syntax (:alternative-macro-syntax-serializations checker-state)
        hygiene (:alternative-macro-hygiene-records checker-state)
        captures (:alternative-macro-explicit-capture-records checker-state)
        build-effects (:alternative-macro-build-effect-traces checker-state)
        cache (:alternative-macro-cache-decisions checker-state)
        equivalence (:alternative-macro-equivalence-reports checker-state)
        facets (:alternative-macro-facet-dispatch-records checker-state)
        generated (:alternative-macro-generated-validation-records checker-state)
        covered (cond-> #{}
                  (seq providers) (conj :provider)
                  (seq traces) (conj :expansion-trace)
                  (seq syntax) (conj :syntax-object)
                  (seq hygiene) (conj :hygiene)
                  (seq captures) (conj :explicit-capture)
                  (seq build-effects) (conj :build-effect)
                  (seq cache) (conj :cache)
                  (seq equivalence) (conj :equivalence)
                  (seq facets) (conj :facet-aware)
                  (seq generated) (conj :generated-validation)
                  (some #(= :macro-invocation (:phase %)) traces)
                  (conj :phase-boundary)
                  (or (some :source-span-preserved? traces)
                      (some :source-span-preserved? syntax))
                  (conj :source-span)
                  (or (some :generated-origin-preserved? traces)
                      (some :generated-origin-preserved? syntax)
                      (some #(= :recorded (:generated-origin %)) captures))
                  (conj :generated-origin)
                  (some #(= :passed (:status %)) equivalence)
                  (conj :reference-l4))
        missing (vec (remove covered l16-required-alternative-macro-families))]
    {:required-families l16-required-alternative-macro-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))