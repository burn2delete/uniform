
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

(defn- builtin-commitments [m0s m2s occurrences-by-module]
  (mapv
   (fn [ordinal descriptor plan occurrences]
     (let [root-preimage
           {:domain :gravity/sh07-b51-ordered-builtin-multiset-root-v1
            :schema-version 1
            :module-ordinal ordinal
            :occurrences occurrences}
           preimage
           {:domain :gravity/sh07-b51-builtin-parity-commitment-v1
            :schema-version 1
            :module-ordinal ordinal
            :m0-id (:module-source-id descriptor)
            :m2-id (:plan-id plan)
            :occurrence-count (count occurrences)
            :ordered-multiset-root (canonical-hash root-preimage)
            :parity-complete true}]
       {:id (canonical-hash preimage)
        :preimage preimage
        :root-preimage root-preimage}))
   (range) m0s m2s occurrences-by-module))

(defn- provenance-commitment [m0s m1 m2s occurrences]
  (let [root-preimage
        {:domain :gravity/sh07-b51-provenance-occurrence-root-v1
         :schema-version 1
         :occurrences occurrences}
        preimage
        {:domain :gravity/sh07-b51-provenance-completeness-commitment-v1
         :schema-version 1
         :m0-ids (mapv :module-source-id m0s)
         :m1-id (:manifest-id m1)
         :m2-ids (mapv :plan-id m2s)
         :internal-builtins internal-builtins
         :occurrence-count (count occurrences)
         :occurrence-root (canonical-hash root-preimage)
         :provenance-complete true}]
    {:id (canonical-hash preimage)
     :preimage preimage
     :root-preimage root-preimage}))

(defn- normalized-census-id [physical commitments]
  (canonical-hash
   {:domain :gravity/sh07-b51-normalized-census-id-v1
    :schema-version 1
    :physical-census physical
    :ordered-category-row-roots (mapv :root-digest commitments)
    :category-row-counts (mapv :row-count commitments)}))

(defn- host-pin-boundary-id []
  (canonical-hash
   {:domain :gravity/sh07-b51-host-pin-boundary-id-v1
    :schema-version 1
    :component :p15-s23-sh07-b51-plan-linker
    :pin-key-names [:expected-m3-id :expected-certificate-a-id
                    :expected-certificate-b-id :expected-semantic-root]
    :caller-supplied false}))

(defn- runtime-token-boundary-id []
  (canonical-hash
   {:domain :gravity/sh07-b51-runtime-token-boundary-id-v1
    :schema-version 1
    :component :p15-s23-sh07-b51-runtime-context
    :token-kind :private-object-identity
    :caller-supplied false}))

(defn- accepted-assumptions []
  {:sha256-collision-resistance :assumed
   :canonical-reader-hash-id
   :gravity/sh07-declared-digest-hash-reader-canonical-hash-v1
   :source-emitter-bytes-immutable true
   :checker-closures-independent true
   :host-pin-boundary-id (host-pin-boundary-id)
   :runtime-token-boundary-id (runtime-token-boundary-id)})

(defn- result-map [physical normalized-id]
  {:status :accepted
   :normalized-census-id normalized-id
   :definition-count (:physical-definition-count physical)
   :local-count (:physical-local-runtime-callsite-count physical)
   :external-count (:physical-external-runtime-callsite-count physical)
   :builtin-count (:physical-referenced-builtin-occurrence-count physical)
   :semantic-row-count (:semantic-row-count physical)
   :external-bijection-complete true
   :builtin-parity-complete true
   :provenance-complete true
   :ordering-complete true
   :reachability-complete true})
