
(defn- sh06-resolution-b51-request-carriers
  [source-path caller manifest]
  (let [descriptors (:module-source-descriptors caller)
        caller-row (:caller caller)
        authority-rows (:authority-rows caller)
        groups (->> authority-rows
                    (group-by #(get-in % [:callee :module-ordinal]))
                    (sort-by key) (mapv second))
        imports
        (mapv
         (fn [rows]
           (let [callee (:callee (first rows))
                 edge-ordinals (vec (mapcat #(map :ordinal (:edges %)) rows))
                 join {:manifest-id (:manifest-id manifest)
                       :caller-module-source-id (:module-source-id caller-row)
                       :callee-module-source-id (:module-source-id callee)
                       :bindings
                       (mapv (fn [{:keys [external edges]}]
                               {:external-binding external
                                :edge-ordinals (mapv :ordinal edges)}) rows)}]
             {:namespace (:module callee) :alias nil :kind :namespace
              :profile (:profile callee) :targets [(:target callee)]
              :visibility :public
              :refer (mapv #(get-in % [:external :symbol]) rows)
              :effects (vec (sort (:effects callee)))
              :capabilities (vec (sort (:capabilities callee)))
              :safety (:safety callee)
              :boundary (get-in rows [0 :external :owner-edge])
              :semantic-span
              {:manifest-id (:manifest-id manifest)
               :caller-module-source-id (:module-source-id caller-row)
               :callee-module-source-id (:module-source-id callee)
               :authorized-edge-ordinals edge-ordinals}
              :source-span {:source (:logical-source-path caller-row)}
              :dependency-artifact-id
              (reader-canonical-hash
               [:gravity/sh07-b51-authenticated-sh06-import-v1 join])
              :foreign-record-complete? true})) groups)
        bindings
        (mapv
         (fn [{:keys [external callee definition edges]}]
           (let [coordinate
                 {:manifest-id (:manifest-id manifest)
                  :caller-module-source-id (:module-source-id caller-row)
                  :callee-module-source-id (:module-source-id callee)
                  :callee-module (:module callee)
                  :callee-source-sha256 (:source-sha256 callee)
                  :callee-definition-source-hash (:source-hash definition)
                  :symbol (:symbol external) :arity (:arity external)
                  :owner-edge (:owner-edge external)
                  :authorized-edge-ordinals (mapv :ordinal edges)}]
             {:name (:symbol external) :kind :function
              :namespace (:module callee)
              :package (sh06-resolution-package (:module callee))
              :binding-class :import :visibility :public
              :profile-set [(:profile callee)] :target-set [(:target callee)]
              :type-ref :gravity.type/function
              :effects (vec (sort (:effects callee)))
              :capabilities (vec (sort (:capabilities callee)))
              :safety (:safety callee) :semantic-span coordinate
              :source-span {:source :external-binding}
              :definition-syntax-id
              (reader-canonical-hash
               [:gravity/sh07-b51-authenticated-sh06-binding-v1 coordinate])
              :definition-artifact-id (:module-source-id callee)}))
         authority-rows)
        edges (mapv (fn [dependency]
                      {:from (:module caller-row)
                       :to (:namespace dependency) :kind :namespace
                       :boundary (:boundary dependency)
                       :semantic-span (:semantic-span dependency)
                       :source-span (:source-span dependency)}) imports)]
    {:imports imports :import-bindings bindings :dependency-edges edges}))

(defn- sh06-resolution-c5-expected-projection!
  [source-path module-ordinal m0s manifest]
  (let [caller (nth m0s module-ordinal)
        caller-edges (filterv #(= (:module-source-id caller)
                                  (:caller-module-source-id %))
                              (:edges manifest))
        rows
        (mapv
         (fn [external]
           (let [symbol (:symbol external)
                 callee-ordinal (:callee-module-ordinal external)
                 callee (when (and (integer? callee-ordinal)
                                   (<= 0 callee-ordinal 6))
                          (nth m0s callee-ordinal nil))
                 definitions (filterv #(= symbol (:name %))
                                      (:definitions callee))
                 definition (first definitions)
                 edges (filterv #(= symbol (:callee-function %)) caller-edges)
                 expected-owner (when callee
                                  (sh06-resolution-b51-owner-edge
                                   (:owner caller) (:owner callee)))]
             (when-not
              (and (= 1 (count definitions)) (seq edges) expected-owner
                   (= expected-owner (:owner-edge external))
                   (= (:module callee) (:callee-module external))
                   (= (:source-sha256 callee)
                      (:callee-source-sha256 external))
                   (= (:source-hash definition)
                      (:callee-definition-source-hash external))
                   (= (:arity definition) (:arity external))
                   (every? #(and (= :authenticated-b51-namespace-external
                                     (:authorization %))
                                  (= expected-owner (:owner-edge %))
                                  (= (:module-source-id callee)
                                     (:callee-module-source-id %))
                                  (= (:source-hash definition)
                                     (:callee-function-source-hash %))
                                  (= (:arity external) (:arity %))) edges))
               (sh06-resolution-c5-fail!
                source-path :unauthorized-or-stale-edge {:symbol symbol}))
             {:external external :callee callee
              :definition definition :edges edges}))
         (:external-bindings caller))
        covered (vec (mapcat #(map :ordinal (:edges %)) rows))]
    (when-not (and (= (set covered) (set (map :ordinal caller-edges)))
                   (= (count covered) (count (set covered))))
      (sh06-resolution-c5-fail!
       source-path :incomplete-edge-coverage {:covered covered}))
    (sh06-resolution-b51-request-carriers
     source-path {:module-source-descriptors m0s :caller caller
                  :authority-rows rows} manifest)))

(defn- sh06-resolution-validate-c5-authority!
  [source-path sh05-artifact base context]
  (when-not
   (and (sh06-resolution-exact-keys?
         context #{:external-binding-authority :resolution-plan-authority})
        (map? (:external-binding-authority context)))
    (sh06-resolution-c5-fail! source-path :missing-or-malformed-context {}))
  (let [authority (:external-binding-authority context)
        preimage (:preimage authority)
        content (:content authority)]
    (when-not
     (and (sh06-resolution-exact-keys?
           authority
           #{:schema :schema-version :content :module-ordinal :source-path
             :sh05-artifact-id :base-request-id :imports :import-bindings
             :dependency-edges :preimage :self-id-omits :authority-id})
          (= :gravity/sh07-b51-c5-external-binding-authority-v1
             (:schema authority))
          (= 1 (:schema-version authority))
          (= [[:authority-id]] (:self-id-omits authority))
          (sh06-resolution-sha256-id? (:authority-id authority))
          (= (:authority-id authority)
             (reader-canonical-hash
              [:gravity/sh07-b51-c5-external-binding-authority-v1
               (dissoc authority :authority-id)]))
          (sh06-resolution-exact-keys?
           preimage #{:schema :content :content-object-proof
                      :source-descriptors :module-source-inputs
                      :module-source-descriptors :link-manifest})
          (= :gravity/sh07-b51-c5-authority-preimage-v1 (:schema preimage))
          (= content (:content preimage))
          (= source-path (:source-path authority))
          (= (:artifact-id sh05-artifact) (:sh05-artifact-id authority))
          (= (reader-canonical-hash
              [:gravity/sh07-b51-sh06-base-request-v1 base])
             (:base-request-id authority)))
      (sh06-resolution-c5-fail! source-path :authority-shape-or-id {}))
    (sh06-resolution-content-proof-tree!
     source-path content (:content-object-proof preimage))
    (let [modules
          (sh06-resolution-b51-module-inputs!
           source-path content (:source-descriptors preimage)
           (:module-source-inputs preimage))
          derived (sh06-resolution-b51-derive-m0-m1! source-path modules)
          m0s (:module-source-descriptors derived)
          manifest (:link-manifest derived)
          ordinal (:module-ordinal authority)
          plan (:resolution-plan-authority context)]
      (when-not
       (and (= m0s (:module-source-descriptors preimage))
            (= manifest (:link-manifest preimage))
            (integer? ordinal) (<= 0 ordinal 6)
            (= source-path (:logical-source-path (nth m0s ordinal)))
            (= content (:content plan)))
        (sh06-resolution-c5-fail!
         source-path :m0-m1-or-context-content-mismatch {}))
      (let [projection
            (sh06-resolution-c5-expected-projection!
             source-path ordinal m0s manifest)]
        (when-not
         (= projection
            (select-keys authority
                         [:imports :import-bindings :dependency-edges]))
          (sh06-resolution-c5-fail!
           source-path :fabricated-external-binding-projection {}))
        authority))))

(defn sh06-resolution-c5-authority-request!
  [source-path sh05-artifact base context]
  (let [authority
        (sh06-resolution-validate-c5-authority!
         source-path sh05-artifact base context)
        external-symbols (set (map :name (:import-bindings authority)))
        base-symbols (set (concat (map :name (:definitions base))
                                  (map :name (:import-bindings base))))]
    (when-not (empty? (clojure.set/intersection external-symbols base-symbols))
      (sh06-resolution-c5-fail!
       source-path :external-binding-intersects-base
       {:symbols (vec (sort-by str
                              (clojure.set/intersection
                               external-symbols base-symbols)))}))
    (-> base
        (update :imports into (:imports authority))
        (update :import-bindings into (:import-bindings authority))
        (update :dependency-edges into (:dependency-edges authority)))))
