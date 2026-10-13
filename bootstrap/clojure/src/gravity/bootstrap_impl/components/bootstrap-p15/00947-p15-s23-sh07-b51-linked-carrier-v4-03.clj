
(defn- p15-s23-sh07-b51-v4-module-census-valid? [row ordinal]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-v4-module-census-keys)
       (= ordinal (:module-ordinal row))
       (symbol? (:module row))
       (string? (:logical-source-path row))
       (contains? #{:r1-producer :r6-independent-verifier :r8-finalizer}
                  (:owner row))
       (p15-s23-sh07-b51-digest-id? (:source-sha256 row))
       (p15-s23-sh07-b51-digest-id? (:module-source-id row))
       (p15-s23-sh07-b51-digest-id? (:plan-id row))
       (every? p15-s23-sh07-b51-v4-nonnegative-integer?
               ((juxt :source-byte-count :physical-definition-count
                      :reachable-definition-count
                      :physical-local-runtime-callsite-count
                      :reachable-local-runtime-callsite-count
                      :physical-external-runtime-callsite-count
                      :reachable-external-runtime-callsite-count
                      :physical-referenced-builtin-occurrence-count
                      :reachable-referenced-builtin-occurrence-count
                      :core-binding-count :lexical-binding-count
                      :external-binding-count :aggregate-binding-count)
                row))
       (<= (:reachable-definition-count row)
           (:physical-definition-count row))
       (<= (:reachable-local-runtime-callsite-count row)
           (:physical-local-runtime-callsite-count row))
       (<= (:reachable-external-runtime-callsite-count row)
           (:physical-external-runtime-callsite-count row))
       (<= (:reachable-referenced-builtin-occurrence-count row)
           (:physical-referenced-builtin-occurrence-count row))
       (<= (:aggregate-binding-count row) 2440)))

(defn- p15-s23-sh07-b51-v4-physical-census-valid? [census]
  (let [modules (:module-censuses census)
        occupancies (:module-binding-occupancies census)
        sum-field (fn [field] (reduce + 0 (map field modules)))]
    (and (p15-s23-sh07-b51-exact-keys?
          census p15-s23-sh07-b51-v4-physical-census-keys)
         (= 7 (:module-count census))
         (vector? modules) (= 7 (count modules))
         (every? true? (map p15-s23-sh07-b51-v4-module-census-valid?
                            modules (range 7)))
         (vector? occupancies) (= 7 (count occupancies))
         (every? p15-s23-sh07-b51-v4-nonnegative-integer? occupancies)
         (= occupancies (mapv :aggregate-binding-count modules))
         (= 2440 (:binding-ceiling census))
         (= (:maximum-binding-occupancy census) (apply max occupancies))
         (<= (:maximum-binding-occupancy census) 2440)
         (every? p15-s23-sh07-b51-v4-nonnegative-integer?
                 ((juxt :physical-definition-count
                        :reachable-definition-count
                        :physical-local-runtime-callsite-count
                        :reachable-local-runtime-callsite-count
                        :physical-external-runtime-callsite-count
                        :reachable-external-runtime-callsite-count
                        :physical-referenced-builtin-occurrence-count
                        :reachable-referenced-builtin-occurrence-count
                        :semantic-row-count) census))
         (= (:physical-definition-count census)
            (sum-field :physical-definition-count))
         (= (:reachable-definition-count census)
            (sum-field :reachable-definition-count))
         (= (:physical-local-runtime-callsite-count census)
            (sum-field :physical-local-runtime-callsite-count))
         (= (:reachable-local-runtime-callsite-count census)
            (sum-field :reachable-local-runtime-callsite-count))
         (= (:physical-external-runtime-callsite-count census)
            (sum-field :physical-external-runtime-callsite-count))
         (= (:reachable-external-runtime-callsite-count census)
            (sum-field :reachable-external-runtime-callsite-count))
         (= (:physical-referenced-builtin-occurrence-count census)
            (sum-field :physical-referenced-builtin-occurrence-count))
         (= (:reachable-referenced-builtin-occurrence-count census)
            (sum-field :reachable-referenced-builtin-occurrence-count)))))

(defn- p15-s23-sh07-b51-v4-transcript-valid?
  [descriptor artifact checker inputs]
  (and (p15-s23-sh07-b51-exact-keys?
        descriptor p15-s23-sh07-b51-v4-transcript-keys)
       (= artifact (:artifact descriptor))
       (= 1 (:schema-version descriptor))
       (p15-s23-sh07-b51-v4-nonnegative-integer? (:node-count descriptor))
       (p15-s23-sh07-b51-v4-nonnegative-integer? (:row-count descriptor))
       (<= (:row-count descriptor) (:node-count descriptor))
       (<= (:node-count descriptor) 262144)
       (<= (:row-count descriptor) 65536)
       (p15-s23-sh07-b51-digest-id? (:artifact-id descriptor))
       (p15-s23-sh07-b51-digest-id? (:record-root descriptor))
       (= (:artifact-id descriptor)
          (p15-s23-sh07-b51-canonical-id
           {:domain :gravity/sh07-b51-census-transcript-artifact-id-v1
            :schema-version 1 :artifact artifact :checker checker
            :inputs inputs :node-count (:node-count descriptor)
            :row-count (:row-count descriptor)
            :record-root (:record-root descriptor)}))))

(defn- p15-s23-sh07-b51-v4-inputs-valid? [inputs]
  (and (p15-s23-sh07-b51-exact-keys?
        inputs p15-s23-sh07-b51-v4-input-keys)
       (= p15-s23-sh07-b51-attempt32-architecture-report-sha256
          (:architecture-report-sha256 inputs))
       (p15-s23-sh07-b51-v4-git-id? (:snapshot-commit inputs))
       (p15-s23-sh07-b51-v4-git-id? (:snapshot-tree inputs))
       (= p15-s23-sh07-b51-v4-predecessor-id
          (:predecessor-executable-descriptor-id inputs))
       (p15-s23-sh07-b51-v4-digest-vector?
        (:module-source-identities inputs) 7)
       (and (vector? (:module-source-byte-counts inputs))
            (= 7 (count (:module-source-byte-counts inputs)))
            (every? p15-s23-sh07-b51-v4-nonnegative-integer?
                    (:module-source-byte-counts inputs)))
       (p15-s23-sh07-b51-digest-id? (:emitter-source-id inputs))
       (p15-s23-sh07-b51-digest-id? (:emitter-semantic-id inputs))
       (p15-s23-sh07-b51-v4-digest-vector? (:m0-ids inputs) 7)
       (p15-s23-sh07-b51-digest-id? (:m1-id inputs))
       (p15-s23-sh07-b51-v4-digest-vector? (:m2-ids inputs) 7)
       (p15-s23-sh07-b51-digest-id? (:bundle-core-id inputs))))

(defn- p15-s23-sh07-b51-v4-checker-valid? [checker algorithm-role]
  (and (p15-s23-sh07-b51-exact-keys?
        checker p15-s23-sh07-b51-v4-checker-keys)
       (= algorithm-role (:algorithm-role checker))
       (every? p15-s23-sh07-b51-digest-id?
               ((juxt :artifact-id :source-sha256 :semantic-id
                      :executable-id :dependency-closure-id) checker))))

(defn- p15-s23-sh07-b51-v4-result-valid? [result claim]
  (let [physical (:physical-census claim)
        commitments (:category-commitments claim)]
    (and (p15-s23-sh07-b51-exact-keys?
          result p15-s23-sh07-b51-v4-result-keys)
         (= :accepted (:status result))
         (= (:normalized-census-id claim) (:normalized-census-id result))
         (= (:definition-count result)
            (:physical-definition-count physical))
         (= (:local-count result)
            (:physical-local-runtime-callsite-count physical))
         (= (:external-count result)
            (:physical-external-runtime-callsite-count physical))
         (= (:builtin-count result)
            (:physical-referenced-builtin-occurrence-count physical))
         (= (:semantic-row-count result) (:semantic-row-count physical)
            (reduce + 0 (map :row-count commitments)))
         (every? true?
                 ((juxt :external-bijection-complete
                        :builtin-parity-complete :provenance-complete
                        :ordering-complete :reachability-complete) result)))))
