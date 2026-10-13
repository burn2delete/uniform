
(defn- validate-checker! [checker]
  (require! (exact-keys? checker checker-keys) :checker-schema)
  (require! (= :source-first-exhaustive (:algorithm-role checker))
            :checker-algorithm-role)
  (require! (every? sha256-id?
                    ((juxt :artifact-id :source-sha256 :semantic-id
                           :executable-id :dependency-closure-id) checker))
            :checker-identities))

(defn- validate-m0s! [m0s source-forms indexes]
  (require! (and (vector? m0s) (= 7 (count m0s))) :m0-count)
  (doseq [[ordinal descriptor] (map-indexed vector m0s)]
    (require! (exact-keys? descriptor m0-keys) :m0-schema
              {:module-ordinal ordinal})
    (require! (and (= :gravity/sh07-b51-module-source-v1
                      (:artifact descriptor))
                   (= 1 (:schema-version descriptor))
                   (= ordinal (:module-ordinal descriptor))
                   (symbol? (:module descriptor))
                   (string? (:logical-source-path descriptor))
                   (nonnegative-integer? (:source-byte-count descriptor))
                   (sha256-id? (:source-sha256 descriptor))
                   (contains? #{:meta} (:profile descriptor))
                   (contains? #{:jvm} (:target descriptor))
                   (set? (:effects descriptor))
                   (set? (:capabilities descriptor))
                   (= :safe (:safety descriptor))
                   (vector? (:definitions descriptor))
                   (vector? (:exports descriptor))
                   (vector? (:external-bindings descriptor))
                   (vector? (:builtin-calls descriptor))
                   (every? nonnegative-integer?
                           ((juxt :core-binding-count :lexical-binding-count
                                  :aggregate-binding-count) descriptor))
                   (<= (:aggregate-binding-count descriptor) 2440)
                   (= (:aggregate-binding-count descriptor)
                      (+ (:core-binding-count descriptor)
                         (:lexical-binding-count descriptor)
                         (count (:definitions descriptor))
                         (count (:external-bindings descriptor))))
                   (= (:module-source-id descriptor)
                      (canonical-hash (dissoc descriptor :module-source-id))))
              :m0-value {:module-ordinal ordinal})
    (let [source-definitions
          (get-in indexes [:definitions-by-module ordinal])
          expected-definitions
          (mapv (fn [form]
                  (let [definition (definition-from-form form)
                        name (:name definition)]
                    {:name name
                     :arity (:arity definition)
                     :visibility (if (some #{name} (:exports descriptor))
                                   :public :private)
                     :source-hash
                     (get-in indexes [:definition-hash [ordinal name]])}))
                (filter definition-from-form (nth source-forms ordinal)))]
      (require! (= expected-definitions (:definitions descriptor))
                :m0-source-definitions {:module-ordinal ordinal})
      (require! (= (set (:exports descriptor))
                   (set (map :name (filter #(= :public (:visibility %))
                                          (:definitions descriptor)))))
                :m0-export-visibility {:module-ordinal ordinal})
      (require! (= (set (keys source-definitions))
                   (set (map :name (:definitions descriptor))))
                :m0-definition-set {:module-ordinal ordinal}))
    (doseq [row (:definitions descriptor)]
      (require! (and (exact-keys? row definition-keys)
                     (symbol? (:name row))
                     (nonnegative-integer? (:arity row))
                     (contains? #{:public :private} (:visibility row))
                     (sha256-id? (:source-hash row)))
                :m0-definition-row {:module-ordinal ordinal}))
    (doseq [row (:external-bindings descriptor)]
      (require! (and (exact-keys? row external-binding-keys)
                     (symbol? (:symbol row))
                     (nonnegative-integer? (:callee-module-ordinal row))
                     (< (:callee-module-ordinal row) 7)
                     (symbol? (:callee-module row))
                     (sha256-id? (:callee-source-sha256 row))
                     (sha256-id? (:callee-definition-source-hash row))
                     (nonnegative-integer? (:arity row))
                     (keyword? (:owner-edge row)))
                :m0-external-binding {:module-ordinal ordinal}))
    (doseq [row (:builtin-calls descriptor)]
      (require! (and (exact-keys? row builtin-call-keys)
                     (symbol? (:caller row))
                     (sha256-id? (:caller-function-source-hash row))
                     (symbol? (:builtin row))
                     (nonnegative-integer? (:arity row))
                     (boolean? (:compiler-internal? row))
                     (= (contains? internal-builtin-arities (:builtin row))
                        (:compiler-internal? row))
                     (or (not (:compiler-internal? row))
                         (= (get internal-builtin-arities (:builtin row))
                            (:arity row))))
                :m0-builtin-row {:module-ordinal ordinal}))))

(defn- validate-m1! [m0s m1]
  (require! (exact-keys? m1 m1-keys) :m1-schema)
  (require! (and (= :gravity/sh07-b51-link-manifest-v1 (:artifact m1))
                 (= 1 (:schema-version m1))
                 (= (mapv :module-source-id m0s) (:module-source-ids m1))
                 (vector? (:edges m1))
                 (= (:manifest-id m1)
                    (canonical-hash (dissoc m1 :manifest-id))))
            :m1-value)
  (require! (= (vec (range (count (:edges m1))))
               (mapv :ordinal (:edges m1)))
            :m1-dense-ordinals)
  (doseq [row (:edges m1)]
    (require! (and (exact-keys? row external-row-keys)
                   (nonnegative-integer? (:ordinal row))
                   (sha256-id? (:caller-module-source-id row))
                   (symbol? (:caller-function row))
                   (sha256-id? (:caller-function-source-hash row))
                   (vector? (:callsite-path row))
                   (every? nonnegative-integer? (:callsite-path row))
                   (sha256-id? (:callee-module-source-id row))
                   (symbol? (:callee-function row))
                   (sha256-id? (:callee-function-source-hash row))
                   (nonnegative-integer? (:arity row))
                   (keyword? (:owner-edge row))
                   (= :authenticated-b51-namespace-external
                      (:authorization row)))
              :m1-edge-row {:ordinal (:ordinal row)})))

(defn- validate-plan-joins! [m0s m1 m2s emitter-source-id emitter-semantic-id]
  (doseq [[ordinal [descriptor plan]]
          (map-indexed vector (map vector m0s m2s))]
    (require! (and (map? plan) (sha256-id? (:plan-id plan))
                   (= (:module-source-id descriptor)
                      (:sh07-b51-module-source-id plan))
                   (= (:manifest-id m1) (:sh07-b51-link-manifest-id plan))
                   (= (:source-sha256 descriptor) (get-in plan [:source :sha256]))
                   (= (:module descriptor) (get-in plan [:module :module]))
                   (= emitter-source-id (get-in plan [:compiler :source-content-hash]))
                   (= emitter-semantic-id (get-in plan [:compiler :semantic-hash])))
              :m2-tier0-join {:module-ordinal ordinal})))

(defn- validate-bundle-core! [m0s m1 m2s bundle]
  (require! (exact-keys? bundle bundle-core-keys) :bundle-core-schema)
  (require! (and (= :gravity/sh07-b51-execution-bundle-core-id-v1
                    (:domain bundle))
                 (= 1 (:schema-version bundle))
                 (= (:manifest-id m1) (:manifest-id bundle)))
            :bundle-core-value)
  (let [coordinates (:module-plans bundle)
        routes (:routes bundle)
        roots (:public-roots bundle)
        plan-by-id (into {} (map (juxt :plan-id identity)) m2s)
        m0-by-id (into {} (map (juxt :module-source-id identity)) m0s)]
    (require! (and (vector? coordinates) (= 7 (count coordinates))
                   (every? #(exact-keys? % module-plan-coordinate-keys)
                           coordinates)
                   (= (vec (range 7)) (mapv :module-ordinal coordinates))
                   (= coordinates
                      (mapv (fn [ordinal descriptor plan]
                              {:module-ordinal ordinal
                               :module-source-id (:module-source-id descriptor)
                               :plan-id (:plan-id plan)})
                            (range) m0s m2s)))
              :bundle-core-module-plans)
    (require! (and (vector? routes)
                   (= (count (:edges m1)) (count routes))
                   (= (vec (range (count routes)))
                      (mapv :edge-ordinal routes))
                   (every? #(exact-keys? % route-keys) routes))
              :bundle-core-routes)
    (doseq [[edge route] (map vector (:edges m1) routes)]
      (require! (and (= (:ordinal edge) (:edge-ordinal route))
                     (= (:caller-module-source-id edge)
                        (:sh07-b51-module-source-id
                         (get plan-by-id (:caller-plan-id route))))
                     (= (:callee-module-source-id edge)
                        (:sh07-b51-module-source-id
                         (get plan-by-id (:callee-plan-id route)))))
                :bundle-core-route {:edge-ordinal (:ordinal edge)}))
    (require! (and (vector? roots) (= 8 (count roots))
                   (= (vec (range 1 9)) (mapv :root-ordinal roots))
                   (every? #(exact-keys? % public-root-keys) roots))
              :bundle-core-public-roots)
    (doseq [root roots]
      (let [plan (get plan-by-id (:plan-id root))
            descriptor (get m0-by-id (:sh07-b51-module-source-id plan))
            definition (get-in plan [:functions (:function root)])]
        (require! (and plan descriptor definition
                       (= (:arity root) (:arity definition)))
                  :bundle-core-public-root
                  {:root-ordinal (:root-ordinal root)})))))

(defn- derive-certificate-inputs [input]
  (let [{:keys [m0s m1 m2s bundle-core]} input]
    {:architecture-report-sha256 (:architecture-report-sha256 input)
     :snapshot-commit (:snapshot-commit input)
     :snapshot-tree (:snapshot-tree input)
     :predecessor-executable-descriptor-id
     (:predecessor-executable-descriptor-id input)
     :module-source-identities (mapv :source-sha256 m0s)
     :module-source-byte-counts (mapv :source-byte-count m0s)
     :emitter-source-id (:emitter-source-id input)
     :emitter-semantic-id (:emitter-semantic-id input)
     :m0-ids (mapv :module-source-id m0s)
     :m1-id (:manifest-id m1)
     :m2-ids (mapv :plan-id m2s)
     :bundle-core-id (canonical-hash bundle-core)}))

(defn- validate-envelope! [input]
  (require! (exact-keys? input input-keys) :input-schema)
  (require! (and (sha256-id? (:architecture-report-sha256 input))
                 (git-id? (:snapshot-commit input))
                 (git-id? (:snapshot-tree input))
                 (sha256-id? (:predecessor-executable-descriptor-id input))
                 (digest-vector? (:module-source-identities input) 7)
                 (and (vector? (:module-source-byte-counts input))
                      (= 7 (count (:module-source-byte-counts input)))
                      (every? nonnegative-integer?
                              (:module-source-byte-counts input)))
                 (sha256-id? (:emitter-source-id input))
                 (sha256-id? (:emitter-semantic-id input))
                 (exact-keys? (:provenance input) input-provenance-keys))
            :input-scalar-envelope)
  (validate-checker! (:checker input))
  (let [indexes (source-indexes (:source-forms input) (:m0s input))]
    (validate-m0s! (:m0s input) (:source-forms input) indexes)
    (validate-m1! (:m0s input) (:m1 input))
    (validate-plan-joins! (:m0s input) (:m1 input) (:m2s input)
                          (:emitter-source-id input)
                          (:emitter-semantic-id input))
    (validate-bundle-core! (:m0s input) (:m1 input) (:m2s input)
                           (:bundle-core input))
    (let [derived (derive-certificate-inputs input)]
      (require! (= (:module-source-identities input)
                   (:module-source-identities derived))
                :module-source-identity-join)
      (require! (= (:module-source-byte-counts input)
                   (:module-source-byte-counts derived))
                :module-source-byte-count-join)
      {:indexes indexes :certificate-inputs derived})))

(defn- definition-map [m0s]
  (into {}
        (for [descriptor m0s, row (:definitions descriptor)]
          [[(:module-ordinal descriptor) (:name row)] row])))

(defn- raw-call-records [raw]
  (filterv #(contains? #{:local-runtime-call :external-runtime-call
                         :builtin-call} (:classification %))
           (:records raw)))
