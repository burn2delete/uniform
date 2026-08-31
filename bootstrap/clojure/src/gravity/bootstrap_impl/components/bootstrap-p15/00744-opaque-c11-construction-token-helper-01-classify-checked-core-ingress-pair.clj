(def ^:private __gravity_bootstrap_lexical_119416_classify-checked-core-ingress-pair
  (fn classify-checked-core-ingress-pair
      [checked-core context]
      (let [checked-core-map?
            (p15-s23-c11-exact-bounded-map? checked-core 128)
            context-map?
            (p15-s23-c11-exact-bounded-map? context 5)
            artifact-kind
            (when checked-core-map? (:kind checked-core))
            context-kind (when context-map? (:kind context))
            source-core-input
            (when checked-core-map? (:source-core-input checked-core))
            legacy-artifact-mode
            (when (p15-s23-c11-exact-bounded-map?
                   source-core-input 32)
              (:mode source-core-input))]
        (cond
          (and (= :gravity/p15-s23-stage2-gravity-checked-core-artifact
                  artifact-kind)
               (= :gravity/p15-s23-stage2-gravity-checked-core-context
                  context-kind)
               (= 5 (count context)))
          :gravity-source-pure

          (and (= :gravity/p15-s23-stage2-closed-checked-core-artifact
                  artifact-kind)
               (nil? context-kind)
               (= 5 (count context))
               (= :effectful-reference legacy-artifact-mode))
          :effectful-reference

          :else :invalid))))
