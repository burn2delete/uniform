
(defn- check-and-derive-inputs [envelope]
  (ensure! (exact-keys? envelope input-keys) :input-schema)
  (let [{:keys [m0s m1 m2s bundle-core source-forms checker provenance]}
        envelope]
    (ensure! (and (vector? m0s) (= 7 (count m0s))
                  (vector? m2s) (= 7 (count m2s))
                  (vector? source-forms) (= 7 (count source-forms))
                  (every? vector? source-forms)) :seven-module-cardinality)
    (ensure! (and (exact-keys? checker checker-keys)
                  (= :m2-first-independent-exhaustive
                     (:algorithm-role checker))
                  (every? digest-id?
                          ((juxt :artifact-id :source-sha256 :semantic-id
                                 :executable-id :dependency-closure-id)
                           checker))
                  (= 5 (count (set ((juxt :artifact-id :source-sha256
                                           :semantic-id :executable-id
                                           :dependency-closure-id)
                                    checker))))) :checker-identity)
    (ensure! (exact-keys? provenance provenance-input-keys)
             :provenance-input-schema)
    (ensure! (and (digest-id? (:architecture-report-sha256 envelope))
                  (raw-git-id? (:snapshot-commit envelope))
                  (raw-git-id? (:snapshot-tree envelope))
                  (digest-id? (:predecessor-executable-descriptor-id envelope)))
             :snapshot-identity)
    (ensure! (every? #(exact-keys? % m0-keys) m0s) :m0-schema)
    (ensure! (= (vec (range 7)) (mapv :module-ordinal m0s))
             :m0-module-order)
    (ensure! (every? #(and (= :gravity/sh07-b51-module-source-v1
                               (:artifact %))
                            (= 1 (:schema-version %))
                            (= (:module-source-id %)
                               (h (dissoc % :module-source-id)))) m0s)
             :m0-own-id)
    (ensure!
     (every?
      (fn [m0]
        (and (vector? (:definitions m0))
             (every? #(and (exact-keys? % definition-row-keys)
                           (symbol? (:name %))
                           (integer? (:arity %)) (<= 0 (:arity %))
                           (contains? #{:public :private} (:visibility %))
                           (digest-id? (:source-hash %))) (:definitions m0))
             (vector? (:external-bindings m0))
             (every? #(exact-keys? % external-binding-keys)
                     (:external-bindings m0))
             (vector? (:builtin-calls m0))
             (every? #(and (exact-keys? % builtin-row-keys)
                           (symbol? (:builtin %))
                           (integer? (:arity %)) (<= 0 (:arity %))
                           (boolean? (:compiler-internal? %))
                           (= (contains? internal-builtin-arities (:builtin %))
                              (:compiler-internal? %))
                           (or (not (:compiler-internal? %))
                               (= (get internal-builtin-arities (:builtin %))
                                  (:arity %))))
                     (:builtin-calls m0))
             (= (:aggregate-binding-count m0)
                (+ (:core-binding-count m0) (count (:definitions m0))
                   (:lexical-binding-count m0)
                   (count (:external-bindings m0)))))) m0s)
     :m0-row-schema)
    (ensure! (and (exact-keys? m1 m1-keys)
                  (= :gravity/sh07-b51-link-manifest-v1 (:artifact m1))
                  (= 1 (:schema-version m1))
                  (= (:manifest-id m1) (h (dissoc m1 :manifest-id))))
             :m1-own-id)
    (ensure! (and (vector? (:edges m1))
                  (every? #(exact-keys? % m1-edge-keys) (:edges m1))
                  (= (vec (range (count (:edges m1))))
                     (mapv :ordinal (:edges m1)))) :m1-edges)
    (ensure! (= (mapv :module-source-id m0s) (:module-source-ids m1))
             :m0-m1-join)
    (ensure! (every? #(and (map? %) (map? (:functions %))
                           (digest-id? (:plan-id %))
                           (= (:plan-id %) (m2-plan-id %))) m2s)
             :m2-own-id)
    (ensure! (every? true?
                     (map (fn [m0 m2]
                            (and (= (:module-source-id m0)
                                    (:sh07-b51-module-source-id m2))
                                 (= (:manifest-id m1)
                                    (:sh07-b51-link-manifest-id m2))
                                 (= (:module m0) (get-in m2 [:module :module]))
                                 (= (:source-sha256 m0)
                                    (get-in m2 [:source :sha256]))))
                          m0s m2s)) :m0-m1-m2-join)
    (ensure! (and (exact-keys? bundle-core bundle-core-keys)
                  (= :gravity/sh07-b51-execution-bundle-core-id-v1
                     (:domain bundle-core))
                  (= 1 (:schema-version bundle-core))
                  (= (:manifest-id m1) (:manifest-id bundle-core))
                  (= 7 (count (:module-plans bundle-core)))
                  (= 8 (count (:public-roots bundle-core))))
             :bundle-core-schema)
    (let [derived-source-identities (mapv :source-sha256 m0s)
          derived-source-bytes (mapv :source-byte-count m0s)
          derived-m0-ids (mapv :module-source-id m0s)
          derived-m2-ids (mapv :plan-id m2s)
          compiler-source-ids
          (set (map #(get-in % [:compiler :source-content-hash]) m2s))
          compiler-semantic-ids
          (set (map #(get-in % [:compiler :semantic-hash]) m2s))]
      (ensure! (= derived-source-identities
                  (:module-source-identities envelope))
               :source-identity-join)
      (ensure! (= derived-source-bytes (:module-source-byte-counts envelope))
               :source-byte-count-join)
      (ensure! (= #{(:emitter-source-id envelope)} compiler-source-ids)
               :emitter-source-join)
      (ensure! (= #{(:emitter-semantic-id envelope)} compiler-semantic-ids)
               :emitter-semantic-join)
      (ensure! (every? digest-id? derived-source-identities)
               :source-sha256-shape)
      (ensure! (every? #(and (integer? %) (<= 0 %)) derived-source-bytes)
               :source-byte-count-shape)
      (ensure! (= (mapv (fn [m0 m2]
                           {:module-ordinal (:module-ordinal m0)
                            :module-source-id (:module-source-id m0)
                            :plan-id (:plan-id m2)}) m0s m2s)
                  (:module-plans bundle-core)) :bundle-module-plan-join)
      (let [m0-by-id (into {} (map (juxt :module-source-id identity)) m0s)
            m2-by-id (into {} (map (juxt :plan-id identity)) m2s)
            edge-by-ordinal (into {} (map (juxt :ordinal identity))
                                  (:edges m1))]
        (ensure!
         (and (vector? (:routes bundle-core))
              (= (count (:edges m1)) (count (:routes bundle-core)))
              (= (vec (range (count (:routes bundle-core))))
                 (mapv :edge-ordinal (:routes bundle-core)))
              (every?
               (fn [route]
                 (let [edge (get edge-by-ordinal (:edge-ordinal route))
                       caller-plan (get m2-by-id (:caller-plan-id route))
                       callee-plan (get m2-by-id (:callee-plan-id route))]
                   (and (= #{:edge-ordinal :caller-plan-id :callee-plan-id}
                           (set (keys route))) edge caller-plan callee-plan
                        (= (:caller-module-source-id edge)
                           (:sh07-b51-module-source-id caller-plan))
                        (= (:callee-module-source-id edge)
                           (:sh07-b51-module-source-id callee-plan)))))
               (:routes bundle-core))) :bundle-route-join)
        (ensure!
         (and (vector? (:public-roots bundle-core))
              (= (vec (range 1 9))
                 (mapv :root-ordinal (:public-roots bundle-core)))
              (every?
               (fn [root]
                 (let [plan (get m2-by-id (:plan-id root))
                       m0 (when plan (get m0-by-id
                                          (:sh07-b51-module-source-id plan)))
                       definition (when plan
                                    (get-in plan [:functions (:function root)]))]
                   (and (= #{:root-ordinal :function :arity :plan-id}
                           (set (keys root))) plan m0 definition
                        (= (:arity root) (:arity definition))
                        (contains? (set (:exports m0)) (:function root)))))
               (:public-roots bundle-core))) :bundle-root-join))
      {:architecture-report-sha256 (:architecture-report-sha256 envelope)
       :snapshot-commit (:snapshot-commit envelope)
       :snapshot-tree (:snapshot-tree envelope)
       :predecessor-executable-descriptor-id
       (:predecessor-executable-descriptor-id envelope)
       :module-source-identities derived-source-identities
       :module-source-byte-counts derived-source-bytes
       :emitter-source-id (:emitter-source-id envelope)
       :emitter-semantic-id (:emitter-semantic-id envelope)
       :m0-ids derived-m0-ids
       :m1-id (:manifest-id m1)
       :m2-ids derived-m2-ids
       :bundle-core-id (h bundle-core)})))

(defn- verify-source-m2-call-parity! [source-scan m2-scan]
  (let [project-source
        (fn [call]
          [(:module-ordinal call) (:caller call) (:kind call)
           (:callee call) (:arity call)])
        project-m2
        (fn [call]
          [(:module-ordinal call) (:caller call)
           (if (= :internal-builtin (:kind call)) :builtin (:kind call))
           (:callee call) (:arity call)])]
    (ensure! (= (frequencies (map project-source (:calls source-scan)))
                (frequencies (map project-m2 (:calls m2-scan))))
             :source-m2-call-parity)))

(defn- verify-definitions!
  [source-scan m0s m2s]
  (doseq [module-ordinal (range 7)]
    (let [source-defs (nth (:definitions-by-module source-scan) module-ordinal)
          m0 (nth m0s module-ordinal)
          m2 (nth m2s module-ordinal)
          expected
          (mapv (fn [definition]
                  {:name (:name definition) :arity (:arity definition)
                   :visibility (if (contains? (set (:exports m0))
                                              (:name definition))
                                 :public :private)
                   :source-hash (definition-id definition)}) source-defs)]
      (ensure! (= expected (:definitions m0)) :source-m0-definition-join)
      (ensure! (= (set (map :name source-defs))
                  (set (keys (:functions m2)))) :source-m2-definition-set)
      (doseq [definition source-defs]
        (let [compiled (get-in m2 [:functions (:name definition)])]
          (ensure! (= (select-keys definition [:name :arity :params :body])
                      (select-keys compiled [:name :arity :params :body]))
                   :source-m2-definition-join))))))
