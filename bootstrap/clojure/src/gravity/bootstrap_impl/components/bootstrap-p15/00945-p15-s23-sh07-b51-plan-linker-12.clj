
(defn- p15-s23-sh07-b51-validate-c5-authority-preimage!
  [preimage]
  (when-not
   (and (p15-s23-sh07-b51-exact-keys?
         preimage p15-s23-sh07-b51-c5-preimage-keys)
        (= :gravity/sh07-b51-c5-authority-preimage-v1
           (:schema preimage))
        (= #{:commit :tree} (set (keys (:content preimage))))
        (vector? (:source-descriptors preimage))
        (= 7 (count (:source-descriptors preimage)))
        (vector? (:module-source-inputs preimage))
        (= 7 (count (:module-source-inputs preimage)))
        (vector? (:module-source-descriptors preimage))
        (= 7 (count (:module-source-descriptors preimage))))
    (p15-s23-sh07-b51-c5-authority-fail!
     :preimage-shape {}))
  (p15-s23-sh07-b51-validate-content-object-proof!
   (:content preimage) (:content-object-proof preimage))
  (let [content (:content preimage)
        source-descriptors (:source-descriptors preimage)
        inputs (:module-source-inputs preimage)
        descriptors (:module-source-descriptors preimage)
        manifest (:link-manifest preimage)
        sources
        (mapv
         (fn [module-ordinal input]
           (when-not
            (and (p15-s23-sh07-b51-exact-keys?
                  input #{:descriptor :source-object-proof})
                 (= (nth source-descriptors module-ordinal nil)
                    (:descriptor input)))
             (p15-s23-sh07-b51-c5-authority-fail!
              :module-source-input-shape
              {:module-ordinal module-ordinal}))
           (let [descriptor (:descriptor input)
                 expected-spec (nth p15-s23-sh07-b51-source-specs
                                    (+ 3 module-ordinal))
                 bytes
                 (p15-s23-sh07-b51-validate-source-object-proof!
                  content descriptor (:source-object-proof input))
                 text
                 (p15-s23-sh07-b51-strict-utf8
                  bytes [:c5-authority module-ordinal :source])]
             (when-not
              (= (select-keys descriptor
                              [:ordinal :kind :module-ordinal :path])
                 expected-spec)
              (p15-s23-sh07-b51-c5-authority-fail!
               :module-source-descriptor
               {:module-ordinal module-ordinal}))
             (p15-s23-sh07-b51-module-input-record
              (p15-s23-sh07-b51-module-spec module-ordinal)
              {:descriptor descriptor :bytes bytes :text text})))
         (range 7) inputs)
        _
        (doseq [module-ordinal (range 7)]
          (when-not
           (p15-s23-sh07-b51-module-source-valid-with-input?
            (nth descriptors module-ordinal)
            (nth sources module-ordinal))
            (p15-s23-sh07-b51-c5-authority-fail!
             :module-source-rederivation
             {:module-ordinal module-ordinal})))
        census
        (p15-s23-sh07-b51-derive-callsite-census-from-modules sources)
        expected-edges
        (p15-s23-sh07-b51-enrich-census-edges descriptors census)
        expected-manifest-base
        {:artifact :gravity/sh07-b51-link-manifest-v1
         :schema-version 1
         :module-source-ids (mapv :module-source-id descriptors)
         :edges expected-edges}
        expected-manifest
        (assoc expected-manifest-base :manifest-id
               (p15-s23-sh07-b51-manifest-id expected-manifest-base))
        root8 (nth descriptors 6)
        root6-source-ids
        (set (map :module-source-id (subvec descriptors 4 6)))
        root6-cross-edges
        (filter #(and (contains? root6-source-ids
                                 (:caller-module-source-id %))
                      (not (contains? root6-source-ids
                                      (:callee-module-source-id %))))
                expected-edges)]
    (when-not
     (and (= expected-manifest manifest)
          (= 1480 (count expected-edges))
          (= p15-s23-sh07-b51-root8-external-callees
             (mapv :symbol (:external-bindings root8)))
          (empty? root6-cross-edges))
      (p15-s23-sh07-b51-c5-authority-fail!
       :m0-m1-rederivation
       {:expected-manifest-id (:manifest-id expected-manifest)
        :actual-manifest-id (:manifest-id manifest)
        :edge-count (count expected-edges)}))
    ;; Every caller is checked, including modules with no external symbols.
    ;; This closes visibility, source/definition/arity/owner joins and proves
    ;; complete edge coverage rather than trusting a self-consistent M0/M1.
    (doseq [module-ordinal (range 7)]
      (p15-s23-sh07-b51-c5-authority-rows*
       module-ordinal (nth sources module-ordinal) descriptors manifest))
    {:sources sources :descriptors descriptors :manifest manifest}))

(defn p15-s23-sh07-b51-c5-external-binding-authority
  [module-ordinal source sh05-artifact base-request preimage]
  (let [{:keys [sources descriptors manifest]}
        (p15-s23-sh07-b51-validate-c5-authority-preimage! preimage)
        expected-source (nth sources module-ordinal nil)
        caller (nth descriptors module-ordinal nil)
        source-path (:path expected-source)
        recomputed-base
        (when (and caller expected-source)
          (sh06-resolution-base-request source-path sh05-artifact))]
    (when-not
     (and caller expected-source
          (= source-path (:path source))
          (= (:source-descriptor expected-source)
             (:source-descriptor source))
          (= (:source-sha256 expected-source) (:source-sha256 source))
          (p15-s23-sh07-b51-byte-array-equal?
           (:bytes expected-source) (:bytes source))
          (= recomputed-base base-request)
          (= (:module caller) (get-in base-request [:module :namespace]))
          (= (:artifact-id sh05-artifact)
             (get-in base-request [:module :c4-artifact-id])))
      (p15-s23-sh07-b51-c5-authority-fail!
       :caller-sh05-base-join {:module-ordinal module-ordinal}))
    (let [rows
          (p15-s23-sh07-b51-c5-authority-rows*
           module-ordinal expected-source descriptors manifest)
          carriers
          (p15-s23-sh07-b51-c5-request-carriers caller manifest rows)
          external-symbols
          (set (map #(get-in % [:external :symbol]) rows))
          base-binding-symbols
          (set (concat (map :name (:definitions base-request))
                       (map :name (:import-bindings base-request))))]
      (when-not (empty? (clojure.set/intersection external-symbols
                                                   base-binding-symbols))
        (p15-s23-sh07-b51-c5-authority-fail!
         :base-binding-collision
         {:module-ordinal module-ordinal
          :symbols (vec (sort-by str external-symbols))}))
      (let [base
            (merge
             {:schema :gravity/sh07-b51-c5-external-binding-authority-v1
              :schema-version 1
              :content (:content preimage)
              :module-ordinal module-ordinal
              :source-path source-path
              :sh05-artifact-id (:artifact-id sh05-artifact)
              :base-request-id
              (p15-s23-sh07-b51-canonical-id
               [:gravity/sh07-b51-sh06-base-request-v1 base-request])
              :preimage preimage
              :self-id-omits [[:authority-id]]}
             carriers)
            authority
            (assoc base :authority-id
                   (p15-s23-sh07-b51-canonical-id
                    [:gravity/sh07-b51-c5-external-binding-authority-v1
                     base]))]
        (when-not
         (and (p15-s23-sh07-b51-exact-keys?
               authority p15-s23-sh07-b51-c5-authority-keys)
              (p15-s23-sh07-b51-digest-id? (:authority-id authority)))
          (p15-s23-sh07-b51-c5-authority-fail!
           :authority-schema {:module-ordinal module-ordinal}))
        authority))))

(defn p15-s23-sh07-b51-c5-external-binding-authority-valid?
  [authority]
  (try
    (let [{:keys [sources descriptors manifest]}
          (p15-s23-sh07-b51-validate-c5-authority-preimage!
           (:preimage authority))
          module-ordinal (:module-ordinal authority)
          source (nth sources module-ordinal nil)
          caller (nth descriptors module-ordinal nil)
          rows
          (when (and source caller)
            (p15-s23-sh07-b51-c5-authority-rows*
             module-ordinal source descriptors manifest))
          carriers
          (when rows
            (p15-s23-sh07-b51-c5-request-carriers
             caller manifest rows))
          expected-id
          (p15-s23-sh07-b51-canonical-id
           [:gravity/sh07-b51-c5-external-binding-authority-v1
            (dissoc authority :authority-id)])]
      (boolean
       (and (p15-s23-sh07-b51-exact-keys?
             authority p15-s23-sh07-b51-c5-authority-keys)
            (= :gravity/sh07-b51-c5-external-binding-authority-v1
               (:schema authority))
            (= 1 (:schema-version authority))
            (= (:content (:preimage authority)) (:content authority))
            (= (:path source) (:source-path authority))
            (p15-s23-sh07-b51-digest-id? (:sh05-artifact-id authority))
            (p15-s23-sh07-b51-digest-id? (:base-request-id authority))
            (= carriers
               (select-keys authority
                            [:imports :import-bindings :dependency-edges]))
            (= [[:authority-id]] (:self-id-omits authority))
            (= expected-id (:authority-id authority)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-sh06-context
  [c5-authority resolution-plan-authority]
  (let [context {:external-binding-authority c5-authority
                 :resolution-plan-authority resolution-plan-authority}]
    (when-not
     (and (p15-s23-sh07-b51-exact-keys?
           context p15-s23-sh07-b51-sh06-context-keys)
          (p15-s23-sh07-b51-exact-keys?
           c5-authority p15-s23-sh07-b51-c5-authority-keys)
          (p15-s23-sh07-b51-exact-keys?
           resolution-plan-authority
           p15-s23-sh07-b51-resolution-plan-authority-keys)
          (p15-s23-sh07-b51-c5-external-binding-authority-valid?
           c5-authority)
          (p15-s23-sh07-b51-resolution-plan-authority-valid?
           resolution-plan-authority)
          (= (:content c5-authority)
             (:content resolution-plan-authority)))
      (p15-s23-sh07-b51-c5-authority-fail!
       :context-join {}))
    context))
