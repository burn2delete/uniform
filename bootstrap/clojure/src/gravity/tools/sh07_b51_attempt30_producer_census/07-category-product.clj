
(defn- category-product [category rows]
  (doseq [row rows]
    (require! (retained-profile-within? row "row")
              :category-row-profile {:category category}))
  (let [groups (if (empty? rows) [[]] (partition-all 16 rows))
        leaves
        (mapv (fn [ordinal group]
                (let [value
                      (array-map
                       :domain :gravity/sh07-b51-linked-closure-leaf-v2
                       :schema-version 2
                       :category category
                       :shard-ordinal ordinal
                       :first-row-ordinal (* 16 ordinal)
                       :row-count (count group)
                       :category-row-count (count rows)
                       :rows (vec group))]
                  (require! (retained-profile-within? value "leaf")
                            :category-leaf-profile {:category category})
                  value))
              (range) groups)]
    (loop [level 1
           children (mapv (fn [ordinal leaf]
                            {:child-ordinal ordinal
                             :first-row-ordinal (:first-row-ordinal leaf)
                             :row-count (:row-count leaf)
                             :digest (canonical-hash leaf)})
                          (range) leaves)
           folds []]
      (require! (<= level 3) :category-fold-level {:category category})
      (let [current
            (mapv
             (fn [group-ordinal group]
               (let [value
                     (array-map
                      :domain :gravity/sh07-b51-linked-closure-fold-v2
                      :schema-version 2
                      :category category
                      :level level
                      :group-ordinal group-ordinal
                      :first-child-ordinal (* 16 group-ordinal)
                      :child-count (count group)
                      :row-count (reduce + (map :row-count group))
                      :children (vec group))]
                 (require! (retained-profile-within? value "fold")
                           :category-fold-profile {:category category})
                 value))
             (range) (partition-all 16 children))
            all-folds (into folds current)]
        (if (= 1 (count current))
          {:leaves leaves
           :folds all-folds
           :commitment
           (let [commitment
                 (array-map
                  :category category
                  :row-count (count rows)
                  :leaf-count (count leaves)
                  :fold-level-count level
                  :root-digest (canonical-hash (first current)))]
             (require! (retained-profile-within? commitment "commitment")
                       :category-commitment-profile {:category category})
             commitment)}
          (recur
           (inc level)
           (mapv (fn [ordinal value]
                   {:child-ordinal ordinal
                    :first-row-ordinal
                    (:first-row-ordinal (first (:children value)))
                    :row-count (:row-count value)
                    :digest (canonical-hash value)})
                 (range) current)
           all-folds))))))

(defn- category-products [rows]
  (require! (= categories (vec (keys rows))) :category-order)
  (require! (<= (reduce + (map count (vals rows)))
                (:maximum-total-rows category-schedule))
            :category-row-total)
  (mapv #(category-product % (get rows %)) categories))

(defn- physical-census [m0s m1 m2s raw m2 reachable rows]
  (let [raw-calls (raw-call-records raw)
        m2-calls (m2-call-records m2)
        module-rows
        (mapv
         (fn [ordinal descriptor plan]
           (let [raw-module (filter #(= ordinal (:module-ordinal %)) raw-calls)
                 m2-module (filter #(= ordinal (:module-ordinal %)) m2-calls)
                 reachable-calls (filter #(contains? reachable (:caller %))
                                         m2-module)
                 external-source (filter #(= (:module-source-id descriptor)
                                             (:caller-module-source-id %))
                                         (:edges m1))
                 reachable-external
                 (filter #(contains? reachable (:caller-function %))
                         external-source)]
             (array-map
              :module-ordinal ordinal
              :module (:module descriptor)
              :logical-source-path (:logical-source-path descriptor)
              :owner (:owner descriptor)
              :source-byte-count (:source-byte-count descriptor)
              :source-sha256 (:source-sha256 descriptor)
              :module-source-id (:module-source-id descriptor)
              :plan-id (:plan-id plan)
              :physical-definition-count (count (:definitions descriptor))
              :reachable-definition-count
              (count (filter #(contains? reachable (:name %))
                             (:definitions descriptor)))
              :physical-local-runtime-callsite-count
              (count (filter #(= :local-runtime-call (:classification %))
                             raw-module))
              :reachable-local-runtime-callsite-count
              (count (filter #(= :local-runtime-call
                                 (:call-classification %)) reachable-calls))
              :physical-external-runtime-callsite-count
              (count external-source)
              :reachable-external-runtime-callsite-count
              (count reachable-external)
              :physical-referenced-builtin-occurrence-count
              (count (:builtin-calls descriptor))
              :reachable-referenced-builtin-occurrence-count
              (count (filter #(and (= :builtin-call
                                      (:call-classification %))
                                   (contains? reachable (:caller %)))
                             m2-module))
              :core-binding-count (:core-binding-count descriptor)
              :lexical-binding-count (:lexical-binding-count descriptor)
              :external-binding-count (count (:external-bindings descriptor))
              :aggregate-binding-count (:aggregate-binding-count descriptor))))
         (range) m0s m2s)
        sum-field #(reduce + (map % module-rows))
        occupancies (mapv :aggregate-binding-count module-rows)
        semantic-count (reduce + (map count (vals rows)))
        census
        (array-map
         :module-count 7
         :module-censuses module-rows
         :physical-definition-count (sum-field :physical-definition-count)
         :reachable-definition-count (sum-field :reachable-definition-count)
         :physical-local-runtime-callsite-count
         (sum-field :physical-local-runtime-callsite-count)
         :reachable-local-runtime-callsite-count
         (sum-field :reachable-local-runtime-callsite-count)
         :physical-external-runtime-callsite-count
         (sum-field :physical-external-runtime-callsite-count)
         :reachable-external-runtime-callsite-count
         (sum-field :reachable-external-runtime-callsite-count)
         :physical-referenced-builtin-occurrence-count
         (sum-field :physical-referenced-builtin-occurrence-count)
         :reachable-referenced-builtin-occurrence-count
         (sum-field :reachable-referenced-builtin-occurrence-count)
         :semantic-row-count semantic-count
         :module-binding-occupancies occupancies
         :maximum-binding-occupancy (apply max occupancies)
         :binding-ceiling 2440)]
    (require! (and (exact-keys? census physical-census-keys)
                   (every? #(exact-keys? % module-census-keys) module-rows)
                   (<= (:maximum-binding-occupancy census) 2440))
              :physical-census-schema)
    census))

(defn- transcript-evidence [artifact records row-count checker inputs]
  (let [raw? (= artifact :gravity/sh07-b51-raw-source-census-transcript-v1)
        root-domain (if raw?
                      :gravity/sh07-b51-raw-source-census-record-root-v1
                      :gravity/sh07-b51-m2-census-record-root-v1)
        record-keys (if raw? raw-record-keys m2-record-keys)]
    (require! (every? #(exact-keys? % record-keys) records)
              :transcript-record-schema {:artifact artifact})
    (require! (= (vec (range (count records))) (mapv :node-ordinal records))
              :transcript-node-ordinals {:artifact artifact})
    (let [record-root
          (canonical-hash
           {:domain root-domain
            :schema-version 1
            :checker-algorithm-role :source-first-exhaustive
            :records records})
          descriptor-base {:artifact artifact
                           :schema-version 1
                           :node-count (count records)
                           :row-count row-count
                           :record-root record-root}
          artifact-id
          (canonical-hash
           {:domain :gravity/sh07-b51-census-transcript-artifact-id-v1
            :schema-version 1
            :artifact artifact
            :checker checker
            :inputs inputs
            :node-count (count records)
            :row-count row-count
            :record-root record-root})
          descriptor (assoc descriptor-base :artifact-id artifact-id)
          evidence
          {:artifact (if raw?
                       :gravity/sh07-b51-raw-source-census-transcript-evidence-v1
                       :gravity/sh07-b51-m2-census-transcript-evidence-v1)
           :schema-version 1
           :checker-algorithm-role :source-first-exhaustive
           :descriptor descriptor
           :records records}
          metrics {:node-count (count records)
                   :record-count (count records)
                   :call-row-count row-count
                   :byte-count
                   (alength (.getBytes
                             (binding [*print-length* nil *print-level* nil
                                       *print-meta* true]
                               (pr-str evidence))
                             java.nio.charset.StandardCharsets/UTF_8))}]
      (require! (offline-transcript-within-bounds? metrics)
                :offline-transcript-bounds
                {:artifact artifact :metrics metrics})
      evidence)))

(defn- external-commitment [m1-id source-records m1-records]
  (let [source-root-preimage
        {:domain :gravity/sh07-b51-ordered-source-external-root-v1
         :schema-version 1
         :records source-records}
        m1-root-preimage
        {:domain :gravity/sh07-b51-ordered-m1-external-root-v1
         :schema-version 1
         :records m1-records}
        preimage
        {:domain :gravity/sh07-b51-external-call-bijection-commitment-v1
         :schema-version 1
         :m1-id m1-id
         :physical-external-count (count source-records)
         :ordered-source-external-root (canonical-hash source-root-preimage)
         :ordered-m1-root (canonical-hash m1-root-preimage)
         :bijection-complete true}]
    {:id (canonical-hash preimage)
     :preimage preimage
     :source-root-preimage source-root-preimage
     :m1-root-preimage m1-root-preimage}))
