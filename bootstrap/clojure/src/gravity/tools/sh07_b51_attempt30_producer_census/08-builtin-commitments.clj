
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

(defn- common-claim-id [architecture-report-sha256 inputs assumptions claim]
  (canonical-hash
   {:domain :gravity/sh07-b51-common-census-claim-id-v1
    :schema-version 1
    :architecture-report-sha256 architecture-report-sha256
    :inputs inputs
    :assumptions assumptions
    :claim claim
    :profile-bounds profile-bounds}))

(defn- evidence-root [checker inputs raw-descriptor m2-descriptor result
                      reproduction-command-id]
  (canonical-hash
   {:domain :gravity/sh07-b51-producer-census-evidence-root-v1
    :schema-version 1
    :checker checker
    :inputs inputs
    :raw-source-transcript raw-descriptor
    :m2-transcript m2-descriptor
    :result result
    :reproduction-command-id reproduction-command-id
    :profile-bounds profile-bounds}))

(defn- rejection [reason context]
  {:artifact :gravity/sh07-b51-census-checker-rejection-evidence-v1
   :schema-version 1
   :status :rejected
   :role :producer-census
   :checker-algorithm-role :source-first-exhaustive
   :reason (if (keyword? reason) reason :checker-failure)
   :context (if (map? context)
              (select-keys context
                           [:module-ordinal :caller :callee :ordinal
                            :edge-ordinal :root-ordinal :artifact :metrics])
              {})})

(defn- certificate-inputs-valid? [inputs]
  (and (exact-keys? inputs certificate-input-keys)
       (sha256-id? (:architecture-report-sha256 inputs))
       (git-id? (:snapshot-commit inputs))
       (git-id? (:snapshot-tree inputs))
       (sha256-id? (:predecessor-executable-descriptor-id inputs))
       (digest-vector? (:module-source-identities inputs) 7)
       (vector? (:module-source-byte-counts inputs))
       (= 7 (count (:module-source-byte-counts inputs)))
       (every? nonnegative-integer? (:module-source-byte-counts inputs))
       (sha256-id? (:emitter-source-id inputs))
       (sha256-id? (:emitter-semantic-id inputs))
       (digest-vector? (:m0-ids inputs) 7)
       (sha256-id? (:m1-id inputs))
       (digest-vector? (:m2-ids inputs) 7)
       (sha256-id? (:bundle-core-id inputs))
       (sha256-id? (:lowering-product-id inputs))))

(defn- checker-valid? [checker]
  (and (exact-keys? checker checker-keys)
       (= :source-first-exhaustive (:algorithm-role checker))
       (every? sha256-id?
               ((juxt :artifact-id :source-sha256 :semantic-id :executable-id
                      :dependency-closure-id) checker))))

(defn- transcript-descriptor-valid? [descriptor artifact]
  (and (exact-keys? descriptor transcript-descriptor-keys)
       (= artifact (:artifact descriptor))
       (= 1 (:schema-version descriptor))
       (nonnegative-integer? (:node-count descriptor))
       (nonnegative-integer? (:row-count descriptor))
       (<= (:row-count descriptor) (:node-count descriptor))
       (sha256-id? (:artifact-id descriptor))
       (sha256-id? (:record-root descriptor))))

(defn- category-commitments-valid? [commitments]
  (and (vector? commitments) (= 7 (count commitments))
       (= categories (mapv :category commitments))
       (every?
        #(and (exact-keys? %
                           #{:category :row-count :leaf-count
                             :fold-level-count :root-digest})
              (nonnegative-integer? (:row-count %))
              (pos-int? (:leaf-count %))
              (and (pos-int? (:fold-level-count %))
                   (<= (:fold-level-count %) 3))
              (sha256-id? (:root-digest %)))
        commitments)))

(defn- physical-census-valid? [physical]
  (and (exact-keys? physical physical-census-keys)
       (= 7 (:module-count physical))
       (vector? (:module-censuses physical))
       (= 7 (count (:module-censuses physical)))
       (= (vec (range 7))
          (mapv :module-ordinal (:module-censuses physical)))
       (every? #(exact-keys? % module-census-keys)
               (:module-censuses physical))
       (every? nonnegative-integer?
               ((juxt :physical-definition-count :reachable-definition-count
                      :physical-local-runtime-callsite-count
                      :reachable-local-runtime-callsite-count
                      :physical-external-runtime-callsite-count
                      :reachable-external-runtime-callsite-count
                      :physical-referenced-builtin-occurrence-count
                      :reachable-referenced-builtin-occurrence-count
                      :semantic-row-count :maximum-binding-occupancy
                      :binding-ceiling) physical))
       (= 2440 (:binding-ceiling physical))
       (<= (:maximum-binding-occupancy physical) 2440)
       (vector? (:module-binding-occupancies physical))
       (= 7 (count (:module-binding-occupancies physical)))
       (every? nonnegative-integer? (:module-binding-occupancies physical))))
