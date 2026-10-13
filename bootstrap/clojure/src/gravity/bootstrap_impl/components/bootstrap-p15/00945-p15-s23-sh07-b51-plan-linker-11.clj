
(defn- p15-s23-sh07-b51-c5-authority-rows*
  [module-ordinal source descriptors manifest]
  (when-not (and (integer? module-ordinal) (<= 0 module-ordinal 6)
                 (vector? descriptors) (= 7 (count descriptors))
                 (= (vec (range 7)) (mapv :module-ordinal descriptors))
                 (= 7 (count (set (map :module-source-id descriptors))))
                 (map? manifest) (vector? (:edges manifest)))
    (p15-s23-sh07-b51-c5-authority-fail!
     :authority-shape
     {:module-ordinal module-ordinal
      :descriptor-count (when (vector? descriptors) (count descriptors))}))
  (let [caller (nth descriptors module-ordinal)
        source-descriptor (:source-descriptor source)
        externals (:external-bindings caller)
        caller-edges
        (filterv #(= (:module-source-id caller)
                     (:caller-module-source-id %))
                 (:edges manifest))]
    (when-not
     (and (= caller (nth descriptors (:module-ordinal caller) nil))
          (= (:module caller) (get-in source [:module :module]))
          (= (:logical-source-path caller) (:path source)
             (:path source-descriptor))
          (= (:source-sha256 caller) (:source-sha256 source)
             (:raw-sha256 source-descriptor))
          (= (:source-byte-count caller) (alength ^bytes (:bytes source)))
          (vector? externals))
      (p15-s23-sh07-b51-c5-authority-fail!
       :caller-descriptor
       {:module-ordinal module-ordinal
        :logical-source-path (:logical-source-path caller)}))
    (when-not (= (count externals)
                 (count (set (map :symbol externals))))
      (p15-s23-sh07-b51-c5-authority-fail!
       :ambiguous-external-binding
       {:module-ordinal module-ordinal
        :symbols (mapv :symbol externals)}))
    (let [rows
          (mapv
           (fn [external]
             (let [symbol (:symbol external)
                   callee-ordinal (:callee-module-ordinal external)
                   callee (when (and (integer? callee-ordinal)
                                     (<= 0 callee-ordinal 6))
                            (nth descriptors callee-ordinal nil))
                   definitions
                   (filterv #(= symbol (:name %)) (:definitions callee))
                   definition (first definitions)
                   edges (filterv #(= symbol (:callee-function %))
                                  caller-edges)
                   expected-owner-edge
                   (when callee
                     (p15-s23-sh07-b51-owner-edge-kind
                      (:owner caller) (:owner callee)))]
               (when-not (= 1 (count definitions))
                 (p15-s23-sh07-b51-c5-authority-fail!
                  :ambiguous-external-binding
                  {:module-ordinal module-ordinal :symbol symbol
                   :callee-module-ordinal callee-ordinal
                   :definition-count (count definitions)}))
               (when (empty? edges)
                 (p15-s23-sh07-b51-c5-authority-fail!
                  :missing-authorized-edge
                  {:module-ordinal module-ordinal :symbol symbol}))
               (when-not
                (and expected-owner-edge
                     (= expected-owner-edge (:owner-edge external))
                     (every?
                      #(and (= :authenticated-b51-namespace-external
                               (:authorization %))
                            (= expected-owner-edge (:owner-edge %)))
                      edges))
                 (p15-s23-sh07-b51-c5-authority-fail!
                  :unauthorized-owner-edge
                  {:module-ordinal module-ordinal :symbol symbol
                   :expected-owner-edge expected-owner-edge
                   :external-owner-edge (:owner-edge external)
                   :edge-owner-edges (mapv :owner-edge edges)
                   :authorizations (mapv :authorization edges)}))
               (when-not
                (and callee
                     (= callee-ordinal (:module-ordinal callee))
                     (= (:callee-module external) (:module callee))
                     (= (:callee-source-sha256 external)
                        (:source-sha256 callee))
                     (= (:callee-definition-source-hash external)
                        (:source-hash definition))
                     (= (:arity external) (:arity definition))
                     (every?
                      (fn [edge]
                        (let [caller-definition
                              (p15-s23-sh07-b51-definition-by-name
                               caller (:caller-function edge))]
                          (and caller-definition
                               (= (:caller-function-source-hash edge)
                                  (:source-hash caller-definition))
                               (= (:callee-module-source-id edge)
                                  (:module-source-id callee))
                               (= (:callee-function-source-hash edge)
                                  (:source-hash definition))
                               (= (:arity edge) (:arity external)))))
                      edges))
                 (p15-s23-sh07-b51-c5-authority-fail!
                  :stale-external-binding
                  {:module-ordinal module-ordinal :symbol symbol
                   :callee-module-ordinal callee-ordinal}))
               {:external external
                :callee callee
                :definition definition
                :edges edges}))
           externals)
          covered-edge-ordinals
          (mapcat #(map :ordinal (:edges %)) rows)]
      (when-not
       (and (= (count caller-edges) (count covered-edge-ordinals))
            (= (set (map :ordinal caller-edges))
               (set covered-edge-ordinals))
            (= (count covered-edge-ordinals)
               (count (set covered-edge-ordinals))))
        (p15-s23-sh07-b51-c5-authority-fail!
         :extra-or-ambiguous-authorized-edge
         {:module-ordinal module-ordinal
          :caller-edge-ordinals (mapv :ordinal caller-edges)
          :covered-edge-ordinals (vec covered-edge-ordinals)}))
      rows)))

(defn- p15-s23-sh07-b51-c5-request-carriers
  [caller manifest authority-rows]
  (let [groups
        (->> authority-rows
             (group-by #(get-in % [:callee :module-ordinal]))
             (sort-by key)
             (mapv second))
        imports
        (mapv
         (fn [rows]
           (let [callee (:callee (first rows))
                 edge-ordinals (vec (mapcat #(map :ordinal (:edges %)) rows))
                 dependency-id
                 (p15-s23-sh07-b51-canonical-id
                  [:gravity/sh07-b51-authenticated-sh06-import-v1
                   {:manifest-id (:manifest-id manifest)
                    :caller-module-source-id (:module-source-id caller)
                    :callee-module-source-id (:module-source-id callee)
                    :bindings
                    (mapv (fn [{:keys [external edges]}]
                            {:external-binding external
                             :edge-ordinals (mapv :ordinal edges)})
                          rows)}])]
             {:namespace (:module callee)
              :alias nil
              :kind :namespace
              :profile (:profile callee)
              :targets [(:target callee)]
              :visibility :public
              :refer (mapv #(get-in % [:external :symbol]) rows)
              :effects (vec (sort (:effects callee)))
              :capabilities (vec (sort (:capabilities callee)))
              :safety (:safety callee)
              :boundary (get-in rows [0 :external :owner-edge])
              :semantic-span
              {:manifest-id (:manifest-id manifest)
               :caller-module-source-id (:module-source-id caller)
               :callee-module-source-id (:module-source-id callee)
               :authorized-edge-ordinals edge-ordinals}
              :source-span {:source (:logical-source-path caller)}
              :dependency-artifact-id dependency-id
              :foreign-record-complete? true}))
         groups)
        import-bindings
        (mapv
         (fn [{:keys [external callee definition edges]}]
           (let [edge-ordinals (mapv :ordinal edges)
                 coordinate
                 {:manifest-id (:manifest-id manifest)
                  :caller-module-source-id (:module-source-id caller)
                  :callee-module-source-id (:module-source-id callee)
                  :callee-module (:module callee)
                  :callee-source-sha256 (:source-sha256 callee)
                  :callee-definition-source-hash (:source-hash definition)
                  :symbol (:symbol external)
                  :arity (:arity external)
                  :owner-edge (:owner-edge external)
                  :authorized-edge-ordinals edge-ordinals}]
             {:name (:symbol external)
              :kind :function
              :namespace (:module callee)
              :package (sh06-resolution-package (:module callee))
              :binding-class :import
              ;; M1 authorization is the visibility grant.  The originating
              ;; M0 definition may remain module-private.
              :visibility :public
              :profile-set [(:profile callee)]
              :target-set [(:target callee)]
              :type-ref :gravity.type/function
              :effects (vec (sort (:effects callee)))
              :capabilities (vec (sort (:capabilities callee)))
              :safety (:safety callee)
              :semantic-span coordinate
              :source-span {:source :external-binding}
              :definition-syntax-id
              (p15-s23-sh07-b51-canonical-id
               [:gravity/sh07-b51-authenticated-sh06-binding-v1 coordinate])
              :definition-artifact-id (:module-source-id callee)}))
         authority-rows)
        dependency-edges
        (mapv
         (fn [dependency]
           {:from (:module caller)
            :to (:namespace dependency)
            :kind :namespace
            :boundary (:boundary dependency)
            :semantic-span (:semantic-span dependency)
            :source-span (:source-span dependency)})
         imports)]
    (when-not
     (and (every? #(p15-s23-sh07-b51-exact-keys?
                    % p15-s23-sh07-b51-c5-import-keys)
                  imports)
          (every? #(p15-s23-sh07-b51-exact-keys?
                    % p15-s23-sh07-b51-c5-import-binding-keys)
                  import-bindings)
          (every? #(p15-s23-sh07-b51-exact-keys?
                    % p15-s23-sh07-b51-c5-dependency-edge-keys)
                  dependency-edges))
      (p15-s23-sh07-b51-c5-authority-fail!
       :projection-schema {:module-ordinal (:module-ordinal caller)}))
    {:imports imports
     :import-bindings import-bindings
     :dependency-edges dependency-edges}))
