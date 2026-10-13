;; Attempt 32 host scaffold.  Production pins and certificate values remain
;; deliberately unavailable until one immutable implementation snapshot is
;; independently reviewed.  Candidate constructors establish schemas and
;; joins; they do not confer authority.
(def p15-s23-sh07-b51-attempt32-architecture-report-sha256
  "sha256:34ee9b302f78d88743c3c8ba91cd9bd47c31bc1b4d814afc9e0a892f70e3b0dd")

;; Compatibility names retained for the Attempt-30 evidence harness.  Their
;; values are the superseding Attempt-32 authorities, never stale Attempt-30
;; identities.
(def p15-s23-sh07-b51-attempt30-architecture-report-sha256
  p15-s23-sh07-b51-attempt32-architecture-report-sha256)

(def p15-s23-sh07-b51-v4-schedule
  (array-map
   :rows-per-leaf 16
   :fold-fanout 16
   :maximum-fold-levels 3
   :category-count 7
   :maximum-total-rows 65536
   :maximum-row-nodes 48
   :maximum-row-depth 6
   :maximum-row-width 8
   :maximum-row-scalar-utf8 4096
   :maximum-row-c11-bytes 8192
   :maximum-row-canonical-reader-bytes 9216
   :maximum-leaf-nodes 1024
   :maximum-leaf-depth 12
   :maximum-leaf-width 16
   :maximum-leaf-scalar-utf8 4096
   :maximum-leaf-c11-bytes 196608
   :maximum-leaf-canonical-reader-bytes 212992
   :maximum-fold-nodes 512
   :maximum-fold-depth 8
   :maximum-fold-width 16
   :maximum-fold-scalar-utf8 4096
   :maximum-fold-c11-bytes 65536
   :maximum-fold-canonical-reader-bytes 73728
   :maximum-commitment-nodes 64
   :maximum-commitment-depth 6
   :maximum-commitment-width 16
   :maximum-commitment-scalar-utf8 4096
   :maximum-commitment-c11-bytes 16384
   :maximum-commitment-canonical-reader-bytes 18432
   :maximum-root-nodes 512
   :maximum-root-depth 8
   :maximum-root-width 64
   :maximum-root-scalar-utf8 4096
   :maximum-root-c11-bytes 65536
   :maximum-root-canonical-reader-bytes 73728))

(def p15-s23-sh07-b51-attempt32-profile-bounds
  {:certificate-maximum-nodes 2048
   :certificate-maximum-depth 12
   :certificate-maximum-width 35
   :certificate-maximum-collection-count 64
   :certificate-maximum-scalar-utf8 4096
   :certificate-maximum-canonical-reader-bytes 262144
   :outer-request-maximum-collection-count 65536
   :carrier-maximum-nodes 8388608
   :carrier-maximum-depth 256
   :carrier-maximum-width 65536
   :carrier-maximum-collection-count 524288
   :carrier-partition-maximum-collection-count 65536
   :carrier-maximum-scalar-utf8 65536
   :offline-transcript-maximum-nodes 262144
   :offline-transcript-maximum-records 262144
   :offline-transcript-maximum-call-rows 65536
   :offline-transcript-maximum-bytes 268435456})

(def p15-s23-sh07-b51-attempt30-profile-bounds
  p15-s23-sh07-b51-attempt32-profile-bounds)

(def p15-s23-sh07-b51-v4-categories
  [:roots :members :functions :local-runtime-call-edges
   :external-runtime-call-edges :referenced-builtins :module-contracts])

(def ^:private p15-s23-sh07-b51-v4-predecessor-id
  "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234")

(def ^:private p15-s23-sh07-b51-v4-inherited-contract-hashes
  (array-map
   :sh07-core-bounds
   "sha256:293bb59806e517d29988dd28a78f0005f0f5b8665ab02f70a82c2b8d91ce77b9"
   :sh07-core-contract
   "sha256:f20c2e57d820edef6461f83de84fe1973a58c02a2a2e9dd702b0a235364e607f"
   :sh07-core-diagnostic-catalog
   "sha256:4a3b03150d3f7e016b5c1e5a17fbea22ae2d73d63b71b1b6c6738630767562c3"))

(def ^:private p15-s23-sh07-b51-v4-logical-public-surface
  '[sh07-core-contract
    sh07-core-bounds
    sh07-core-diagnostic-catalog
    sh07-build-core-template
    sh07-verify-core-template
    sh07-verify-core-resolved
    sh07-b51-build-template
    sh07-b51-verify-template
    sh07-b51-resolve-digest-preimage
    sh07-b51-resolve-template
    sh07-b51-verify-resolved
    sh07-b51-build-independent-verifier-binding
    sh07-b51-build-final-artifact
    sh07-b51-finalize-rejection])

(def ^:private p15-s23-sh07-b51-v4-carrier-keys
  #{:domain :schema-version :predecessor-executable-descriptor-id
    :semantic-manifest :semantic-root-hash :module-source-descriptors
    :link-manifest :module-plan-carriers :execution-bundle :physical-census
    :compile-census-certificates})

(def ^:private p15-s23-sh07-b51-v4-raw-keys
  #{:artifact :schema-version :authenticated-sh06-request
    :authenticated-sh06-verification-report :sh06-resolution-artifact
    :predecessor-executable-descriptor :v18-executable-descriptor
    :predecessor-contract-carrier :predecessor-raw-outcome
    :physical-invocation :scope})

(def ^:private p15-s23-sh07-b51-v4-semantic-keys
  #{:domain :schema-version :predecessor-executable-descriptor-id
    :module-source-ids :link-manifest-id :module-plan-ids
    :execution-bundle-id :compile-census-certificate-ids :schedule
    :category-commitments :logical-public-surface
    :inherited-contract-value-hashes :total-row-count})

(def ^:private p15-s23-sh07-b51-v4-certificate-keys
  #{:artifact :domain :schema-version :status :role
    :architecture-report-sha256 :claim-id :claim :assumptions :checker
    :inputs :result :provenance :raw-source-transcript :m2-transcript
    :profile-bounds :evidence-root :certificate-id})

(def ^:private p15-s23-sh07-b51-v4-claim-keys
  #{:domain :normalized-census-id :external-bijection-commitment
    :builtin-parity-commitments :provenance-completeness-commitment
    :physical-census :category-schedule :category-commitments})

(def ^:private p15-s23-sh07-b51-v4-assumption-keys
  #{:sha256-collision-resistance :canonical-reader-hash-id
    :source-emitter-bytes-immutable :checker-closures-independent
    :host-pin-boundary-id :runtime-token-boundary-id})

(def ^:private p15-s23-sh07-b51-v4-checker-keys
  #{:artifact-id :source-sha256 :semantic-id :executable-id
    :dependency-closure-id :algorithm-role})

(def ^:private p15-s23-sh07-b51-v4-input-keys
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0-ids :m1-id :m2-ids :bundle-core-id})

(def ^:private p15-s23-sh07-b51-v4-result-keys
  #{:status :normalized-census-id :definition-count :local-count
    :external-count :builtin-count :semantic-row-count
    :external-bijection-complete :builtin-parity-complete
    :provenance-complete :ordering-complete :reachability-complete})

(def ^:private p15-s23-sh07-b51-v4-provenance-keys
  #{:invocation-id :tool-closure-id :raw-transcript-artifact-id
    :m2-transcript-artifact-id :reproduction-command-id
    :evidence-relative-path :completed-at})

(def ^:private p15-s23-sh07-b51-v4-transcript-keys
  #{:artifact :schema-version :node-count :row-count
    :artifact-id :record-root})

(def ^:private p15-s23-sh07-b51-v4-category-commitment-keys
  #{:category :row-count :leaf-count :fold-level-count :root-digest})

(def ^:private p15-s23-sh07-b51-v4-physical-census-keys
  #{:module-count :module-censuses :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :semantic-row-count
    :module-binding-occupancies :maximum-binding-occupancy :binding-ceiling})

(def ^:private p15-s23-sh07-b51-v4-module-census-keys
  #{:module-ordinal :module :logical-source-path :owner :source-byte-count
    :source-sha256 :module-source-id :plan-id :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :core-binding-count
    :lexical-binding-count :external-binding-count :aggregate-binding-count})

(defn- p15-s23-sh07-b51-v4-bounded-utf8-count
  [^String value limit]
  (loop [index 0 bytes 0]
    (if (or (> bytes limit) (>= index (.length value)))
      bytes
      (let [code-point (.codePointAt value index)
            width (cond
                    (<= code-point 0x7f) 1
                    (<= code-point 0x7ff) 2
                    (<= code-point 0xffff) 3
                    :else 4)]
        (recur (+ index (Character/charCount code-point))
               (+ bytes width))))))

(defn- p15-s23-sh07-b51-v4-scalar-text [value limit]
  (when (or (nil? value) (boolean? value) (char? value) (string? value)
            (keyword? value) (symbol? value) (number? value))
    (let [raw (cond
                (string? value) value
                (or (keyword? value) (symbol? value)) (str value)
                :else nil)]
      (when (or (nil? raw)
                (<= (p15-s23-sh07-b51-v4-bounded-utf8-count raw limit)
                    limit))
        (pr-str value)))))

(defn- p15-s23-sh07-b51-v4-bounded-collection-children
  [value maximum-width]
  ;; Some authenticated plan syntax is a PersistentList/Cons and therefore
  ;; does not implement Counted.  Walk at most width+1 elements instead of
  ;; calling count or rejecting a finite canonical list by representation.
  (loop [remaining (seq value) width 0 children []]
    (if (nil? remaining)
      {:width width :children children}
      (do
        (when (>= width maximum-width)
          (throw (ex-info "supplied collection exceeds width"
                          {:reason :maximum-width
                           :maximum maximum-width
                           :observed (inc width)})))
        (let [item (first remaining)]
          (if (map? value)
            (recur (next remaining) (inc width)
                   (conj children (key item) (val item)))
            (recur (next remaining) (inc width) (conj children item))))))))

(defn p15-s23-sh07-b51-v4-bounded-value-profile
  [root {:keys [maximum-nodes maximum-depth maximum-width
                maximum-collection-count maximum-scalar-utf8]}]
  (try
    (loop [pending [[root 0]] nodes 0 collections 0 maximum-seen-depth 0
           maximum-seen-width 0 maximum-seen-scalar 0]
      (if (empty? pending)
        {:nodes nodes :depth maximum-seen-depth :width maximum-seen-width
         :collection-count collections
         :scalar-utf8 maximum-seen-scalar}
        (let [[value depth] (peek pending)
              remaining (pop pending)
              collection? (coll? value)
              collection-profile
              (when collection?
                (p15-s23-sh07-b51-v4-bounded-collection-children
                 value maximum-width))
              width (if collection? (:width collection-profile) 0)
              collection-count (if collection? (inc collections) collections)
              scalar-text (when-not collection?
                            (p15-s23-sh07-b51-v4-scalar-text
                             value maximum-scalar-utf8))
              scalar-bytes
              (if collection?
                0
                (when scalar-text
                  (p15-s23-sh07-b51-v4-bounded-utf8-count
                   scalar-text maximum-scalar-utf8)))
              next-nodes (inc nodes)]
          (when-not (and (<= next-nodes maximum-nodes)
                         (<= depth maximum-depth)
                         (<= width maximum-width)
                         (<= collection-count maximum-collection-count)
                         (or collection?
                             (and scalar-text
                                  (<= scalar-bytes maximum-scalar-utf8))))
            (throw
             (ex-info
              "supplied value exceeds preflight bounds"
              {:reason
               (cond
                 (> next-nodes maximum-nodes) :maximum-nodes
                 (> depth maximum-depth) :maximum-depth
                 (> width maximum-width) :maximum-width
                 (> collection-count maximum-collection-count)
                 :maximum-collection-count
                 :else :maximum-scalar-utf8)
               :observed
               {:nodes next-nodes :depth depth :width width
                :collection-count collection-count
                :scalar-utf8 scalar-bytes}})))
          (let [children (if collection?
                           (:children collection-profile)
                           [])]
            (recur (reduce (fn [stack child]
                             (conj stack [child (inc depth)]))
                           remaining children)
                   next-nodes collection-count
                   (max maximum-seen-depth depth)
                   (max maximum-seen-width width)
                   (max maximum-seen-scalar scalar-bytes))))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable failure
      (when (or (= "true" (System/getProperty
                            "gravity.attempt32.preflight-debug"))
                (= "true" (System/getProperty
                            "gravity.attempt30.preflight-debug")))
        (binding [*out* *err*]
          (prn :attempt32-preflight-rejection (ex-data failure))))
      nil)))

(defn- p15-s23-sh07-b51-v4-carrier-profile-limits
  [maximum-collection-count]
  {:maximum-nodes
   (:carrier-maximum-nodes p15-s23-sh07-b51-attempt32-profile-bounds)
   :maximum-depth
   (:carrier-maximum-depth p15-s23-sh07-b51-attempt32-profile-bounds)
   :maximum-width
   (:carrier-maximum-width p15-s23-sh07-b51-attempt32-profile-bounds)
   :maximum-collection-count maximum-collection-count
   :maximum-scalar-utf8
   (:carrier-maximum-scalar-utf8
    p15-s23-sh07-b51-attempt32-profile-bounds)})

(defn- p15-s23-sh07-b51-v4-profile-carrier-partition
  [value]
  ;; This complete iterative pass is intentionally first.  Nothing above this
  ;; point reads the plan coordinate, asks for plan cardinality, or constructs
  ;; a residual projection.
  (when-let
   [whole-profile
    (p15-s23-sh07-b51-v4-bounded-value-profile
     value
     (p15-s23-sh07-b51-v4-carrier-profile-limits
      (:carrier-maximum-collection-count
       p15-s23-sh07-b51-attempt32-profile-bounds)))]
    (let [plans (:module-plan-carriers value)]
      (when (and (vector? plans) (= 7 (count plans)))
        ;; The seven vector elements are disjoint logical-occurrence buckets.
        ;; Profiling each position independently preserves repeated/shared
        ;; occurrences: object identity never deducts from a budget.
        (loop [ordinal 0 buckets [] partition-collections 0]
          (if (< ordinal 7)
            (when-let
             [profile
              (p15-s23-sh07-b51-v4-bounded-value-profile
               (nth plans ordinal)
               (p15-s23-sh07-b51-v4-carrier-profile-limits
                (:carrier-partition-maximum-collection-count
                 p15-s23-sh07-b51-attempt32-profile-bounds)))]
              (recur (inc ordinal)
                     (conj buckets
                           {:bucket [:module-plan-carriers ordinal]
                            :profile profile})
                     (+ partition-collections
                        (:collection-count profile))))
            ;; Construct the residual only after the complete carrier and all
            ;; seven plan elements have passed.  The empty vector occupies the
            ;; original plan-vector root, so the eight counts form an exact
            ;; partition of all complete-carrier collection occurrences.
            (let [residual (assoc value :module-plan-carriers [])
                  residual-profile
                  (p15-s23-sh07-b51-v4-bounded-value-profile
                   residual
                   (p15-s23-sh07-b51-v4-carrier-profile-limits
                    (:carrier-partition-maximum-collection-count
                     p15-s23-sh07-b51-attempt32-profile-bounds)))
                  total
                  (when residual-profile
                    (+ partition-collections
                       (:collection-count residual-profile)))]
              (when (and residual-profile
                         (= total (:collection-count whole-profile))
                         (<= total
                             (:carrier-maximum-collection-count
                              p15-s23-sh07-b51-attempt32-profile-bounds)))
                {:whole-profile whole-profile
                 :buckets
                 (conj buckets {:bucket :residual
                                :profile residual-profile})
                 :partition-collection-count total}))))))))

(defn p15-s23-sh07-b51-v4-carrier-preflight-profile [value]
  (try
    (p15-s23-sh07-b51-v4-profile-carrier-partition value)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ nil)))

(defn p15-s23-sh07-b51-v4-carrier-preflight? [value]
  (boolean (p15-s23-sh07-b51-v4-carrier-preflight-profile value)))

(defn p15-s23-sh07-b51-v4-certificate-preflight? [certificate]
  (try
    (let [v9? (contains? certificate :semantic-witness)
          compact (if v9? (dissoc certificate :semantic-witness) certificate)
          profile
          (p15-s23-sh07-b51-v4-bounded-value-profile
           compact
           {:maximum-nodes 2048 :maximum-depth 12 :maximum-width 35
            :maximum-collection-count 64 :maximum-scalar-utf8 4096})
          witness-profile
          (when v9?
            (p15-s23-sh07-b51-v4-bounded-value-profile
             (:semantic-witness certificate)
             (p15-s23-sh07-b51-v4-carrier-profile-limits
              (:carrier-maximum-collection-count
               p15-s23-sh07-b51-attempt32-profile-bounds))))]
      (and profile
           (or (not v9?) witness-profile)
           (<= (p15-s23-sh07-b51-v4-bounded-utf8-count
                (pr-str (reader-canonical-value compact)) 262144)
               262144)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-b51-v4-nonnegative-integer? [value]
  (and (integer? value) (<= 0 value) (<= value Long/MAX_VALUE)))

(defn- p15-s23-sh07-b51-v4-digest-vector? [value size]
  (and (vector? value) (= size (count value))
       (every? p15-s23-sh07-b51-digest-id? value)))

(defn- p15-s23-sh07-b51-v4-git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- p15-s23-sh07-b51-v4-category-commitments-valid? [commitments]
  (let [leaf-count (fn [row-count]
                     (max 1 (quot (+ row-count 15) 16)))
        fold-level-count
        (fn [leaves]
          (loop [children leaves level 1]
            (if (<= children 16)
              level
              (recur (quot (+ children 15) 16) (inc level)))))]
    (and (vector? commitments) (= 7 (count commitments))
       (= p15-s23-sh07-b51-v4-categories (mapv :category commitments))
       (<= (reduce + 0 (map :row-count commitments)) 65536)
       (every?
        (fn [commitment]
          (and (p15-s23-sh07-b51-exact-keys?
                commitment p15-s23-sh07-b51-v4-category-commitment-keys)
               (every? p15-s23-sh07-b51-v4-nonnegative-integer?
                       ((juxt :row-count :leaf-count :fold-level-count)
                        commitment))
               (<= (:row-count commitment) 65536)
               (<= (:fold-level-count commitment) 3)
               (= (:leaf-count commitment)
                  (leaf-count (:row-count commitment)))
               (= (:fold-level-count commitment)
                  (fold-level-count (:leaf-count commitment)))
               (p15-s23-sh07-b51-digest-id? (:root-digest commitment))))
        commitments))))

(defn- p15-s23-sh07-b51-v4-category-census-joins?
  [commitments physical]
  (let [row-counts (into {} (map (juxt :category :row-count)) commitments)]
    (and (= 8 (get row-counts :roots))
         (= (:reachable-definition-count physical)
            (get row-counts :members))
         (= (:reachable-definition-count physical)
            (get row-counts :functions))
         (= (:reachable-local-runtime-callsite-count physical)
            (get row-counts :local-runtime-call-edges))
         (= (:reachable-external-runtime-callsite-count physical)
            (get row-counts :external-runtime-call-edges))
         (= (:reachable-referenced-builtin-occurrence-count physical)
            (get row-counts :referenced-builtins))
         (= 7 (get row-counts :module-contracts)))))

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
  (let [v9? (contains? inputs :lowering-product-id)]
    (and (p15-s23-sh07-b51-exact-keys?
          inputs (cond-> p15-s23-sh07-b51-v4-input-keys
                   v9? (conj :lowering-product-id)))
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
       (p15-s23-sh07-b51-digest-id? (:bundle-core-id inputs))
       (or (not v9?)
           (p15-s23-sh07-b51-digest-id? (:lowering-product-id inputs))))))

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

(defn p15-s23-sh07-b51-compile-census-certificate-v1-valid?
  [certificate]
  (try
    (let [role (:role certificate)
          role-domain
          ({:producer-census
            :gravity/sh07-b51-producer-census-certificate-v1
            :independent-verifier-census
            :gravity/sh07-b51-independent-verifier-census-certificate-v1}
           role)
          algorithm-role
          ({:producer-census :source-first-exhaustive
            :independent-verifier-census
            :m2-first-independent-exhaustive}
           role)
          evidence-domain
          ({:producer-census
            :gravity/sh07-b51-producer-census-evidence-root-v1
            :independent-verifier-census
            :gravity/sh07-b51-independent-verifier-census-evidence-root-v1}
           role)
          claim (:claim certificate)
          assumptions (:assumptions certificate)
          checker (:checker certificate)
          inputs (:inputs certificate)
          result (:result certificate)
          provenance (:provenance certificate)
          raw (:raw-source-transcript certificate)
          m2 (:m2-transcript certificate)
          v9? (contains? certificate :semantic-witness)
          witness (:semantic-witness certificate)
          commitments (:category-commitments claim)
          normalized-preimage
          {:domain :gravity/sh07-b51-normalized-census-id-v1
           :schema-version 1
           :physical-census (:physical-census claim)
           :ordered-category-row-roots (mapv :root-digest commitments)
           :category-row-counts (mapv :row-count commitments)}
          claim-preimage
          {:domain :gravity/sh07-b51-common-census-claim-id-v1
           :schema-version 1
           :architecture-report-sha256
           (:architecture-report-sha256 certificate)
           :inputs inputs :assumptions assumptions :claim claim
           :profile-bounds (:profile-bounds certificate)}
          evidence-preimage
          {:domain evidence-domain :schema-version 1 :checker checker
           :inputs inputs :raw-source-transcript raw :m2-transcript m2
           :result result
           :reproduction-command-id (:reproduction-command-id provenance)
           :profile-bounds (:profile-bounds certificate)}]
      (and (p15-s23-sh07-b51-v4-certificate-preflight? certificate)
           (p15-s23-sh07-b51-exact-keys?
            certificate (cond-> p15-s23-sh07-b51-v4-certificate-keys
                          v9? (conj :semantic-witness)))
           (= :gravity/sh07-b51-compile-census-certificate
              (:artifact certificate))
           (= role-domain (:domain certificate))
           (= 1 (:schema-version certificate))
           (= :accepted (:status certificate))
           (= p15-s23-sh07-b51-attempt32-architecture-report-sha256
              (:architecture-report-sha256 certificate)
              (:architecture-report-sha256 inputs))
           (p15-s23-sh07-b51-exact-keys?
            claim p15-s23-sh07-b51-v4-claim-keys)
           (= :gravity/sh07-b51-common-census-claim-v1 (:domain claim))
           (p15-s23-sh07-b51-digest-id?
            (:external-bijection-commitment claim))
           (p15-s23-sh07-b51-v4-digest-vector?
            (:builtin-parity-commitments claim) 7)
           (p15-s23-sh07-b51-digest-id?
            (:provenance-completeness-commitment claim))
           (= p15-s23-sh07-b51-v4-schedule (:category-schedule claim))
           (p15-s23-sh07-b51-v4-category-commitments-valid? commitments)
           (p15-s23-sh07-b51-v4-physical-census-valid?
            (:physical-census claim))
           (p15-s23-sh07-b51-v4-category-census-joins?
            commitments (:physical-census claim))
           (= (:normalized-census-id claim)
              (p15-s23-sh07-b51-canonical-id normalized-preimage))
           (p15-s23-sh07-b51-exact-keys?
            assumptions p15-s23-sh07-b51-v4-assumption-keys)
           (= {:sha256-collision-resistance :assumed
               :canonical-reader-hash-id
               :gravity/sh07-declared-digest-hash-reader-canonical-hash-v1
               :source-emitter-bytes-immutable true
               :checker-closures-independent true
               :host-pin-boundary-id p15-s23-sh07-b51-host-pin-boundary-id
               :runtime-token-boundary-id
               p15-s23-sh07-b51-runtime-token-boundary-id}
              assumptions)
           (p15-s23-sh07-b51-v4-checker-valid? checker algorithm-role)
           (p15-s23-sh07-b51-v4-inputs-valid? inputs)
           (= v9? (contains? inputs :lowering-product-id))
           (or
            (not v9?)
            (and
             (p15-s23-sh07-b51-exact-keys?
              witness
              #{:schema :content :operation-manifest :operation-schedule-id
                :fuel-receipts :l1 :l2 :c2 :c6 :boot2 :boot3 :boot4
                :witness-id})
             (= :gravity/sh07-b51-authentic-lowering-witness-v1
                (:schema witness))
             (= {:commit (:snapshot-commit inputs)
                 :tree (:snapshot-tree inputs)}
                (:content witness))
             (p15-s23-sh07-b51-digest-id?
              (:operation-schedule-id witness))
             (p15-s23-sh07-b51-digest-id? (:witness-id witness))
             (vector? (:operation-manifest witness))
             (= 7 (count (:operation-manifest witness)))
             (= (vec (range 7)) (mapv :ordinal (:operation-manifest witness)))
             (= (vec (repeat 7 (:lowering-product-id inputs)))
                (mapv :lowering-product-id (:operation-manifest witness)))
             (vector? (:fuel-receipts witness))
             (= 7 (count (:fuel-receipts witness)))
             (= (mapv :operation-id (:operation-manifest witness))
                (mapv :operation-id (:fuel-receipts witness)))
             (every? true? (map :within-bounds (:fuel-receipts witness)))
             (= (:witness-id witness)
                (p15-s23-sh07-b51-canonical-id
                 [:gravity/sh07-b51-authentic-lowering-witness-v1
                  (dissoc witness :witness-id)]))))
           (p15-s23-sh07-b51-v4-result-valid? result claim)
           (p15-s23-sh07-b51-exact-keys?
            provenance p15-s23-sh07-b51-v4-provenance-keys)
           (every? p15-s23-sh07-b51-digest-id?
                   ((juxt :tool-closure-id :raw-transcript-artifact-id
                          :m2-transcript-artifact-id
                          :reproduction-command-id) provenance))
           (every? #(and (string? %) (not (empty? %)))
                   ((juxt :invocation-id :evidence-relative-path
                          :completed-at) provenance))
           (p15-s23-sh07-b51-v4-transcript-valid?
            raw :gravity/sh07-b51-raw-source-census-transcript-v1
            checker inputs)
           (p15-s23-sh07-b51-v4-transcript-valid?
            m2 :gravity/sh07-b51-m2-census-transcript-v1
            checker inputs)
           (= (:artifact-id raw) (:raw-transcript-artifact-id provenance))
           (= (:artifact-id m2) (:m2-transcript-artifact-id provenance))
           (= p15-s23-sh07-b51-attempt32-profile-bounds
              (:profile-bounds certificate))
           (= (:claim-id certificate)
              (p15-s23-sh07-b51-canonical-id claim-preimage))
           (= (:evidence-root certificate)
              (p15-s23-sh07-b51-canonical-id evidence-preimage))
           (= (:certificate-id certificate)
              (p15-s23-sh07-b51-canonical-id
               (dissoc certificate :certificate-id)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
  [certificates]
  (try
    (let [[certificate-a certificate-b] certificates
          checker-a (:checker certificate-a)
          checker-b (:checker certificate-b)
          checker-identity-keys
          [:source-sha256 :semantic-id :executable-id :dependency-closure-id]
          v9? (contains? certificate-a :semantic-witness)
          common-keys
          (cond-> [:architecture-report-sha256 :claim-id :claim :assumptions
                   :inputs :profile-bounds]
            v9? (conj :semantic-witness))]
      (and (vector? certificates) (= 2 (count certificates))
           (= [:producer-census :independent-verifier-census]
              (mapv :role certificates))
           (every? p15-s23-sh07-b51-compile-census-certificate-v1-valid?
                   certificates)
           (= v9? (contains? certificate-b :semantic-witness))
           (= (select-keys certificate-a common-keys)
              (select-keys certificate-b common-keys))
           (not= (:certificate-id certificate-a)
                 (:certificate-id certificate-b))
           (every? #(not= (get checker-a %) (get checker-b %))
                   checker-identity-keys)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-v4-bundle-core-preimage [products]
  {:domain :gravity/sh07-b51-execution-bundle-core-id-v1
   :schema-version 1
   :manifest-id (get-in products [:link-manifest :manifest-id])
   :module-plans
   (mapv (fn [descriptor plan]
           {:module-ordinal (:module-ordinal descriptor)
            :module-source-id (:module-source-id descriptor)
            :plan-id (:plan-id plan)})
         (:module-source-descriptors products) (:module-plans products))
   :routes
   (let [plan-by-source
         (into {} (map (juxt :sh07-b51-module-source-id identity)
                       (:module-plans products)))]
     (mapv (fn [edge]
             {:edge-ordinal (:ordinal edge)
              :caller-plan-id
              (:plan-id (get plan-by-source (:caller-module-source-id edge)))
              :callee-plan-id
              (:plan-id (get plan-by-source (:callee-module-source-id edge)))})
           (get-in products [:link-manifest :edges])))
   :public-roots
   (mapv (fn [root]
           {:root-ordinal (:root-ordinal root)
            :function (:function root) :arity (:arity root)
            :plan-id (:plan-id
                      (nth (:module-plans products) (:module-ordinal root)))})
         p15-s23-sh07-b51-public-root-specs)})

(defn- p15-s23-sh07-b51-v4-declared-edge-valid?
  [descriptors edge]
  (let [by-id (into {} (map (juxt :module-source-id identity)) descriptors)
        caller (get by-id (:caller-module-source-id edge))
        callee (get by-id (:callee-module-source-id edge))
        external
        (first (filter #(= (:callee-function edge) (:symbol %))
                       (:external-bindings caller)))
        caller-definition
        (first (filter #(= (:caller-function edge) (:name %))
                       (:definitions caller)))
        callee-definition
        (first (filter #(= (:callee-function edge) (:name %))
                       (:definitions callee)))]
    (and caller callee external caller-definition callee-definition
         (p15-s23-sh07-b51-edge-row-valid? edge)
         (= (p15-s23-sh07-b51-owner-edge-kind
             (:owner caller) (:owner callee))
            (:owner-edge edge) (:owner-edge external))
         (= (:callee-module-ordinal external) (:module-ordinal callee))
         (= (:callee-module external) (:module callee))
         (= (:callee-source-sha256 external) (:source-sha256 callee))
         (= (:caller-function-source-hash edge)
            (:source-hash caller-definition))
         (= (:callee-function-source-hash edge)
            (:source-hash callee-definition))
         (= (:callee-definition-source-hash external)
            (:source-hash callee-definition))
         (= (:arity external) (:arity edge) (:arity callee-definition)))))

(defn- p15-s23-sh07-b51-v4-products-closed-joins? [products]
  (let [descriptors (:module-source-descriptors products)
        manifest (:link-manifest products)
        plans (:module-plans products)
        core (p15-s23-sh07-b51-v4-bundle-core-preimage products)]
    (and (vector? descriptors) (= 7 (count descriptors))
         (= (vec (range 7)) (mapv :module-ordinal descriptors))
         (every? p15-s23-sh07-b51-digest-id?
                 (map :module-source-id descriptors))
         (vector? plans) (= 7 (count plans))
         (= (mapv :module-source-id descriptors)
            (mapv :sh07-b51-module-source-id plans))
         (= (mapv :plan-id plans) (mapv :plan-id (:module-plans core)))
         (= #{:artifact :schema-version :manifest-id :module-source-ids :edges}
            (set (keys manifest)))
         (= :gravity/sh07-b51-link-manifest-v1 (:artifact manifest))
         (= 1 (:schema-version manifest))
         (= (mapv :module-source-id descriptors)
            (:module-source-ids manifest))
         (= (:manifest-id manifest)
            (p15-s23-sh07-b51-manifest-id manifest))
         (vector? (:edges manifest))
         (= (vec (range (count (:edges manifest))))
            (mapv :ordinal (:edges manifest)))
         (every? #(p15-s23-sh07-b51-v4-declared-edge-valid?
                   descriptors %)
                 (:edges manifest))
         (= 8 (count (:public-roots core)))
         (= (:execution-bundle-core products) core)
         (= (:execution-bundle-core-id products)
            (p15-s23-sh07-b51-canonical-id core))
         (p15-s23-sh07-b51-execution-bundle-core-valid-structure?
          descriptors manifest plans core
          (:execution-bundle-core-id products)))))

(defn- p15-s23-sh07-b51-v4-certificate-inputs-match-products?
  [certificate products]
  (let [inputs (:inputs certificate)
        descriptors (:module-source-descriptors products)
        plans (:module-plans products)]
    (= {:predecessor-executable-descriptor-id
        p15-s23-sh07-b51-v4-predecessor-id
        :module-source-identities (mapv :source-sha256 descriptors)
        :module-source-byte-counts (mapv :source-byte-count descriptors)
        :emitter-source-id (:emitter-source-id products)
        :emitter-semantic-id (:emitter-semantic-id products)
        :m0-ids (mapv :module-source-id descriptors)
        :m1-id (get-in products [:link-manifest :manifest-id])
        :m2-ids (mapv :plan-id plans)
        :bundle-core-id (:execution-bundle-core-id products)}
       (select-keys
        inputs
        [:predecessor-executable-descriptor-id :module-source-identities
         :module-source-byte-counts :emitter-source-id :emitter-semantic-id
         :m0-ids :m1-id :m2-ids :bundle-core-id]))))

(defn- p15-s23-sh07-b51-v4-physical-census-match-products?
  [certificate products]
  (let [rows (get-in certificate [:claim :physical-census :module-censuses])
        descriptors (:module-source-descriptors products)
        plans (:module-plans products)]
    (and (= 7 (count rows) (count descriptors) (count plans))
         (every?
          true?
          (map
           (fn [row descriptor plan]
             (= {:module-ordinal (:module-ordinal descriptor)
                 :module (:module descriptor)
                 :logical-source-path (:logical-source-path descriptor)
                 :owner (:owner descriptor)
                 :source-byte-count (:source-byte-count descriptor)
                 :source-sha256 (:source-sha256 descriptor)
                 :module-source-id (:module-source-id descriptor)
                 :plan-id (:plan-id plan)
                 :core-binding-count (:core-binding-count descriptor)
                 :lexical-binding-count (:lexical-binding-count descriptor)
                 :external-binding-count (count (:external-bindings descriptor))
                 :aggregate-binding-count (:aggregate-binding-count descriptor)}
                (select-keys
                 row
                 [:module-ordinal :module :logical-source-path :owner
                  :source-byte-count :source-sha256 :module-source-id :plan-id
                  :core-binding-count :lexical-binding-count
                  :external-binding-count :aggregate-binding-count])))
           rows descriptors plans)))))

(defn p15-s23-sh07-b51-build-semantic-manifest-v4
  [products execution-bundle certificates]
  (let [claim (:claim (first certificates))
        commitments (:category-commitments claim)]
    {:domain :gravity/sh07-b51-v18-linked-semantic-manifest-v4
     :schema-version 4
     :predecessor-executable-descriptor-id p15-s23-sh07-b51-v4-predecessor-id
     :module-source-ids
     (mapv :module-source-id (:module-source-descriptors products))
     :link-manifest-id (get-in products [:link-manifest :manifest-id])
     :module-plan-ids (mapv :plan-id (:module-plans products))
     :execution-bundle-id (:bundle-id execution-bundle)
     :compile-census-certificate-ids (mapv :certificate-id certificates)
     :schedule (:category-schedule claim)
     :category-commitments commitments
     :logical-public-surface p15-s23-sh07-b51-v4-logical-public-surface
     :inherited-contract-value-hashes
     p15-s23-sh07-b51-v4-inherited-contract-hashes
     :total-row-count (reduce + 0 (map :row-count commitments))}))

(defn- p15-s23-sh07-b51-v4-build-execution-bundle
  [products certificates]
  (p15-s23-sh07-b51-build-execution-bundle-v2
   products (mapv :certificate-id certificates)))

(declare p15-s23-sh07-b51-build-linked-carrier-v4-candidate)

(defn- p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?
  [carrier]
  (let [certificates (:compile-census-certificates carrier)
          certificate-a (first certificates)
          execution-bundle (:execution-bundle carrier)
          plans (mapv :plan (:module-plan-carriers carrier))
          products
          {:artifact :gravity/sh07-b51-linked-plan-products-v2
           :module-source-descriptors (:module-source-descriptors carrier)
           :link-manifest (:link-manifest carrier)
           :module-plans plans
           :emitter-source-id (get-in certificate-a [:inputs :emitter-source-id])
           :emitter-semantic-id
           (get-in certificate-a [:inputs :emitter-semantic-id])
           :execution-bundle-core
           (assoc (select-keys execution-bundle
                               [:manifest-id :module-plans :routes
                                :public-roots])
                  :domain :gravity/sh07-b51-execution-bundle-core-id-v1
                  :schema-version 1)
           :execution-bundle-core-id (:bundle-core-id execution-bundle)}
          expected-bundle
          (p15-s23-sh07-b51-v4-build-execution-bundle products certificates)
          expected-semantic
          (p15-s23-sh07-b51-build-semantic-manifest-v4
           products expected-bundle certificates)
          expected-root
          (p15-s23-sh07-b51-canonical-id
           {:domain :gravity/sh07-b51-v18-linked-semantic-root-v4
            :schema-version 4 :semantic-manifest expected-semantic})]
        (and (p15-s23-sh07-b51-exact-keys?
            carrier p15-s23-sh07-b51-v4-carrier-keys)
           (= :gravity/sh07-b51-v18-linked-executable-closure-carrier-v4
              (:domain carrier))
           (= 4 (:schema-version carrier))
           (= p15-s23-sh07-b51-v4-predecessor-id
              (:predecessor-executable-descriptor-id carrier))
           (p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
            certificates)
           (p15-s23-sh07-b51-v4-products-closed-joins? products)
           (every? #(p15-s23-sh07-b51-v4-certificate-inputs-match-products?
                      % products)
                   certificates)
           (every? #(p15-s23-sh07-b51-v4-physical-census-match-products?
                      % products)
                   certificates)
           (= (:physical-census carrier)
              (get-in certificate-a [:claim :physical-census]))
           (= expected-bundle execution-bundle)
           (= expected-semantic (:semantic-manifest carrier))
           (= p15-s23-sh07-b51-v4-semantic-keys
              (set (keys (:semantic-manifest carrier))))
           (= expected-root (:semantic-root-hash carrier))
           (= (mapv (fn [descriptor plan]
                      {:module-ordinal (:module-ordinal descriptor)
                       :module-source-id (:module-source-id descriptor)
                       :plan-id (:plan-id plan) :plan plan})
                    (:module-source-descriptors products) plans)
              (:module-plan-carriers carrier)))))

(defn p15-s23-sh07-b51-linked-carrier-v4-valid? [carrier]
  (try
    (and (p15-s23-sh07-b51-v4-carrier-preflight? carrier)
         (p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?
          carrier))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-v4-candidate-pin-map [carrier]
  {:expected-m3-id (get-in carrier [:execution-bundle :bundle-id])
   :expected-certificate-a-id
   (get-in carrier [:compile-census-certificates 0 :certificate-id])
   :expected-certificate-b-id
   (get-in carrier [:compile-census-certificates 1 :certificate-id])
   :expected-semantic-root (:semantic-root-hash carrier)})

(defn p15-s23-sh07-b51-v4-candidate-matches-pin-map?
  [carrier pin-map]
  (and (= #{:expected-m3-id :expected-certificate-a-id
            :expected-certificate-b-id :expected-semantic-root}
          (set (keys pin-map)))
       (every? p15-s23-sh07-b51-digest-id? (vals pin-map))
       (= pin-map (p15-s23-sh07-b51-v4-candidate-pin-map carrier))))

(defn p15-s23-sh07-b51-attempt32-authority-status []
  {:status :provisional-no-authority
   :host-pin-boundary-id p15-s23-sh07-b51-host-pin-boundary-id
   :runtime-token-boundary-id p15-s23-sh07-b51-runtime-token-boundary-id
   :pins-frozen? false
   :certificates-frozen? false})

(defn p15-s23-sh07-b51-attempt30-authority-status []
  (p15-s23-sh07-b51-attempt32-authority-status))

(def ^:private p15-s23-sh07-b51-authoritative-linked-carrier-value-v4
  :gravity/provisional-no-authority)

(def ^:private p15-s23-sh07-b51-v4-linked-product-keys
  #{:artifact :module-source-descriptors :link-manifest :module-plans
    :emitter-source-id :emitter-semantic-id :execution-bundle-core
    :execution-bundle-core-id})

(def ^:private p15-s23-sh07-b51-v9-receipted-product-keys
  #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
    :module-transactions :sh06-resolution-plan-authority
    :source-semantic-carrier :source-read-receipt})

(def ^:private p15-s23-sh07-b51-v9-source-receipt-keys
  #{:schema :role :commit :tree :source-descriptors
    :source-descriptors-root :events :event-root :counters :finalized})

(defn- p15-s23-sh07-b51-v4-receipted-products->linked-products
  [products]
  (let [receipt (:source-read-receipt products)
        source-descriptors (:source-descriptors receipt)
        events (:events receipt)
        plans (:m2s products)
        source-ids (mapv #(get-in % [:compiler :source-content-hash]) plans)
        semantic-ids (mapv #(get-in % [:compiler :semantic-hash]) plans)
        emitter-source-id (first source-ids)
        emitter-semantic-id (first semantic-ids)
        plan-authority (:sh06-resolution-plan-authority products)
        source-semantic-carrier (:source-semantic-carrier products)
        content (select-keys receipt [:commit :tree])
        linked-products
        {:artifact :gravity/sh07-b51-linked-plan-products-v2
         :module-source-descriptors (:m0s products)
         :link-manifest (:m1 products)
         :module-plans plans
         :emitter-source-id emitter-source-id
         :emitter-semantic-id emitter-semantic-id
         :execution-bundle-core (:bundle-core products)
         :execution-bundle-core-id (:execution-bundle-core-id products)}]
    (when-not
     (and
      (p15-s23-sh07-b51-exact-keys?
       products p15-s23-sh07-b51-v9-receipted-product-keys)
      (p15-s23-sh07-b51-exact-keys?
       receipt p15-s23-sh07-b51-v9-source-receipt-keys)
      (= :gravity/sh07-b51-source-read-receipt-v3 (:schema receipt))
      (contains? #{:producer-census :independent-verifier-census :m}
                 (:role receipt))
      (vector? source-descriptors) (= 13 (count source-descriptors))
      (= (:source-descriptors-root receipt)
         (p15-s23-sh07-b51-canonical-id
          [:gravity/sh07-b51-source-descriptors-root-v1 source-descriptors]))
      (vector? events) (= 40 (count events))
      (= (:event-root receipt)
         (p15-s23-sh07-b51-canonical-id
          [:gravity/sh07-b51-source-event-root-v1 events]))
      (= {:acquire-count 13 :decode-count 13
          :event-count 40 :finalize-count 1}
         (:counters receipt))
      (true? (:finalized receipt))
      (vector? (:source-forms products))
      (= 7 (count (:source-forms products)))
      (vector? (:module-transactions products))
      (= 7 (count (:module-transactions products)))
      (vector? plans) (= 7 (count plans))
      (p15-s23-sh07-b51-digest-id? emitter-source-id)
      (p15-s23-sh07-b51-digest-id? emitter-semantic-id)
      (= (vec (repeat 7 emitter-source-id)) source-ids)
      (= (vec (repeat 7 emitter-semantic-id)) semantic-ids)
      (p15-s23-sh07-b51-resolution-plan-authority-valid? plan-authority)
      (= content (:content plan-authority))
      (= (nth source-descriptors 12 nil)
         (:source-descriptor plan-authority))
      (p15-s23-sh07-b51-source-semantic-carrier-valid?
       content receipt (:module-transactions products)
       source-semantic-carrier)
      (p15-s23-sh07-b51-replay-returned-source-authority-events!
       (:m0s products) (:m1 products) plan-authority receipt)
      (p15-s23-sh07-b51-exact-keys?
       linked-products p15-s23-sh07-b51-v4-linked-product-keys))
      (throw (ex-info "invalid Attempt32 receipted product boundary"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-linked-carrier})))
    linked-products))

(defn- p15-s23-sh07-b51-v4-normalize-products [products]
  (cond
    (p15-s23-sh07-b51-exact-keys?
     products p15-s23-sh07-b51-v4-linked-product-keys)
    products

    (p15-s23-sh07-b51-exact-keys?
     products p15-s23-sh07-b51-v9-receipted-product-keys)
    (p15-s23-sh07-b51-v4-receipted-products->linked-products products)

    :else
    (throw (ex-info "invalid Attempt32 linked product shape"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :unauthenticated-b51-linked-carrier}))))

(defn- p15-s23-sh07-b51-v9-certificate-shell? [certificate]
  (and (contains? certificate :semantic-witness)
       (contains? (:inputs certificate) :lowering-product-id)
       (= :gravity/sh07-b51-authentic-lowering-witness-v1
          (get-in certificate [:semantic-witness :schema]))
       (p15-s23-sh07-b51-digest-id?
        (get-in certificate [:semantic-witness :witness-id]))
       (p15-s23-sh07-b51-digest-id?
        (get-in certificate [:inputs :lowering-product-id]))))

(defn p15-s23-sh07-b51-build-linked-carrier-v4-candidate
  [products certificates]
  (let [receipted-e3-products?
        (p15-s23-sh07-b51-exact-keys?
         products p15-s23-sh07-b51-v9-receipted-product-keys)
        products (p15-s23-sh07-b51-v4-normalize-products products)]
    (when-not
     (and (p15-s23-sh07-b51-v4-products-closed-joins? products)
          (or (not receipted-e3-products?)
              (every? p15-s23-sh07-b51-v9-certificate-shell? certificates))
          (p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
           certificates)
          (every? #(p15-s23-sh07-b51-v4-certificate-inputs-match-products?
                     % products)
                  certificates)
          (every? #(p15-s23-sh07-b51-v4-physical-census-match-products?
                     % products)
                  certificates))
      (throw (ex-info "invalid Attempt32 carrier candidate inputs"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-linked-carrier})))
    (let [execution-bundle
          (p15-s23-sh07-b51-v4-build-execution-bundle products certificates)
          semantic
          (p15-s23-sh07-b51-build-semantic-manifest-v4
           products execution-bundle certificates)
          root-preimage
          {:domain :gravity/sh07-b51-v18-linked-semantic-root-v4
           :schema-version 4 :semantic-manifest semantic}
          physical (get-in certificates [0 :claim :physical-census])]
      (array-map
       :domain :gravity/sh07-b51-v18-linked-executable-closure-carrier-v4
       :schema-version 4
       :predecessor-executable-descriptor-id
       p15-s23-sh07-b51-v4-predecessor-id
       :semantic-manifest semantic
       :semantic-root-hash (p15-s23-sh07-b51-canonical-id root-preimage)
       :module-source-descriptors (:module-source-descriptors products)
       :link-manifest (:link-manifest products)
       :module-plan-carriers
       (mapv (fn [descriptor plan]
               {:module-ordinal (:module-ordinal descriptor)
                :module-source-id (:module-source-id descriptor)
                :plan-id (:plan-id plan) :plan plan})
             (:module-source-descriptors products) (:module-plans products))
       :execution-bundle execution-bundle
       :physical-census physical
       :compile-census-certificates certificates))))

(defn- p15-s23-sh07-b51-v4-host-pin-map []
  {:expected-m3-id p15-s23-sh07-b51-expected-m3-id
   :expected-certificate-a-id p15-s23-sh07-b51-expected-certificate-a-id
   :expected-certificate-b-id p15-s23-sh07-b51-expected-certificate-b-id
   :expected-semantic-root p15-s23-sh07-b51-expected-semantic-root})

(defn- p15-s23-sh07-b51-load-local-authority-v4 []
  (let [pin-map (p15-s23-sh07-b51-v4-host-pin-map)
        carrier p15-s23-sh07-b51-authoritative-linked-carrier-value-v4]
    (when (and (every? p15-s23-sh07-b51-digest-id? (vals pin-map))
               (map? carrier)
               (every? p15-s23-sh07-b51-v9-certificate-shell?
                       (:compile-census-certificates carrier)))
      (let [base-products (p15-s23-sh07-b51-current-bundle-core-products)
            reconstructed-carrier
            (p15-s23-sh07-b51-build-linked-carrier-v4-candidate
             base-products
             (:compile-census-certificates carrier))
            products
            (assoc base-products :execution-bundle (:execution-bundle carrier))]
        (when (and (= carrier reconstructed-carrier)
                   (p15-s23-sh07-b51-linked-carrier-v4-valid? carrier)
                   (p15-s23-sh07-b51-v4-candidate-matches-pin-map?
                    carrier pin-map))
          {:carrier carrier :products products})))))

(defn p15-s23-sh07-b51-current-linked-carrier-v4 []
  (if-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
    (:carrier authority)
    (throw (ex-info "Attempt32 authority is not frozen"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :provisional-no-authority
                     :result-committed? false
                     :output-committed? false}))))

(defn p15-s23-sh07-b51-current-authoritative-linked-products-v4 []
  (if-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
    (:products authority)
    (throw (ex-info "Attempt32 authority is not frozen"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :provisional-no-authority
                     :result-committed? false
                     :output-committed? false}))))

(defn- p15-s23-sh07-b51-v4-bounded-carrier-equal? [supplied current]
  (= supplied current))

(defn p15-s23-sh07-b51-linked-carrier-v4-authorized? [carrier]
  (try
    (boolean
     (and (p15-s23-sh07-b51-v4-carrier-preflight? carrier)
          (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
            (and (p15-s23-sh07-b51-v4-bounded-carrier-equal?
                  carrier (:carrier authority))
                 (p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?
                  carrier)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-linked-carrier-v4-runtime-products-match?
  [carrier descriptors manifest plans bundle]
  (try
    (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
      (let [current (:carrier authority)]
        (and (identical? carrier current)
             (= descriptors (:module-source-descriptors current))
             (= manifest (:link-manifest current))
             (= bundle (:execution-bundle current))
             (= plans (mapv :plan (:module-plan-carriers current))))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

;; Compatibility for the unchanged Stage2 internal-builtin component.  The
;; value accepted here is carrier-v4; carrier-v3 domains always reject.
(defn p15-s23-sh07-b51-linked-carrier-v3-runtime-products-match?
  [carrier descriptors manifest plans bundle]
  (p15-s23-sh07-b51-linked-carrier-v4-runtime-products-match?
   carrier descriptors manifest plans bundle))

(def ^:private p15-s23-sh07-b51-v4-preflight-placeholder
  :gravity/bounded-carrier-preflight-placeholder)

(defn- p15-s23-sh07-b51-v4-shallow-raw-carrier-coordinate
  [raw]
  ;; This pass examines only root entries.  It never descends into a value and
  ;; never calls count on the supplied map; the 65,537th entry rejects before
  ;; any unbounded root traversal can occur.
  (when (map? raw)
    (let [maximum-width
          (:carrier-maximum-width p15-s23-sh07-b51-attempt32-profile-bounds)
          maximum-scalar
          (:carrier-maximum-scalar-utf8
           p15-s23-sh07-b51-attempt32-profile-bounds)]
      (loop [remaining (seq raw) width 0 found? false carrier nil]
        (if (nil? remaining)
          (when found? {:carrier carrier :root-width width})
          (when (< width maximum-width)
            (let [entry (first remaining)
                  root-key (key entry)
                  key-text
                  (p15-s23-sh07-b51-v4-scalar-text root-key maximum-scalar)
                  key-bytes
                  (when key-text
                    (p15-s23-sh07-b51-v4-bounded-utf8-count
                     key-text maximum-scalar))
                  coordinate?
                  (= :v18-executable-descriptor root-key)]
              (when (and key-text (<= key-bytes maximum-scalar)
                         (not (and found? coordinate?)))
                (recur (next remaining) (inc width)
                       (or found? coordinate?)
                       (if coordinate? (val entry) carrier))))))))))

(defn p15-s23-sh07-b51-supplied-raw-v4-preflight-profile
  [raw]
  (try
    (when-let
     [coordinate
      (p15-s23-sh07-b51-v4-shallow-raw-carrier-coordinate raw)]
      (let [outer
            (assoc raw :v18-executable-descriptor
                   p15-s23-sh07-b51-v4-preflight-placeholder)
            outer-profile
            (p15-s23-sh07-b51-v4-bounded-value-profile
             outer
             (p15-s23-sh07-b51-v4-carrier-profile-limits
              (:outer-request-maximum-collection-count
               p15-s23-sh07-b51-attempt32-profile-bounds)))]
        ;; The nested carrier profile starts only after the shallow pass and
        ;; independent placeholder outer profile have completed.  Its first
        ;; operation is the complete 524,288-collection iterative pass; the
        ;; plan key is not read until that pass succeeds.
        (when outer-profile
          (when-let
           [carrier-profile
            (p15-s23-sh07-b51-v4-carrier-preflight-profile
             (:carrier coordinate))]
            {:outer-profile outer-profile
             :carrier-profile carrier-profile}))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ nil)))

(defn p15-s23-sh07-b51-supplied-raw-v4-preflight? [raw]
  (boolean (p15-s23-sh07-b51-supplied-raw-v4-preflight-profile raw)))

(defn- p15-s23-sh07-b51-v4-raw-joins-valid? [raw]
  (and (p15-s23-sh07-b51-exact-keys? raw p15-s23-sh07-b51-v4-raw-keys)
       (= :gravity/sh07-b51-raw-adapter-request-linked-v4 (:artifact raw))
       (= 4 (:schema-version raw))
       (= :sh07-b51-vector-destructuring (:scope raw))
       (= p15-s23-sh07-b51-v4-predecessor-id
          (:executable-closure-descriptor-id
           (:predecessor-executable-descriptor raw)))
       (every? map?
               ((juxt :authenticated-sh06-request
                      :authenticated-sh06-verification-report
                      :sh06-resolution-artifact
                      :predecessor-executable-descriptor
                      :predecessor-contract-carrier
                      :predecessor-raw-outcome
                      :physical-invocation) raw))))

(defn- p15-s23-sh07-b51-v4-step3-admit
  [raw authority]
  (let [supplied-carrier (:v18-executable-descriptor raw)
        current-carrier (:carrier authority)]
    (and (p15-s23-sh07-b51-v4-bounded-carrier-equal?
          supplied-carrier current-carrier)
         (p15-s23-sh07-b51-v4-raw-joins-valid? raw)
         (p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?
          supplied-carrier))))

(defn p15-s23-sh07-b51-linked-raw-v4-authorized? [raw]
  (try
    (boolean
     (and (p15-s23-sh07-b51-supplied-raw-v4-preflight? raw)
          (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
            (p15-s23-sh07-b51-v4-step3-admit raw authority))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-b51-v4-route-and-arity
  [products root-ordinal args]
  (let [bundle (:execution-bundle products)
        roots (:public-roots bundle)
        root (when (and (integer? root-ordinal) (<= 1 root-ordinal 8)
                        (vector? roots) (= 8 (count roots)))
               (nth roots (dec root-ordinal) nil))
        specification
        (when root (nth p15-s23-sh07-b51-public-root-specs
                        (dec root-ordinal) nil))
        expected-owner
        (when root
          (case root-ordinal
            6 :r6-independent-verifier
            8 :r8-finalizer
            :r1-producer))
        plan (when root
               (first (filter #(= (:plan-id root) (:plan-id %))
                              (:module-plans products))))
        descriptor
        (when specification
          (nth (:module-source-descriptors products)
               (:module-ordinal specification) nil))]
    (when (and (vector? args) root specification plan descriptor
               (= expected-owner (:owner descriptor))
               (= (select-keys specification [:root-ordinal :function :arity])
                  (select-keys root [:root-ordinal :function :arity]))
               (= (:plan-id plan) (:plan-id root))
               (= (:module-source-id descriptor)
                  (:sh07-b51-module-source-id plan))
               (= (:arity root) (count args))
               (= (:arity root)
                  (get-in plan [:functions (:function root) :arity])))
      {:root root :plan plan})))

(defn- p15-s23-sh07-b51-v4-attach-runtime-token
  [authority raw route args runtime]
  (let [products (:products authority)
        carrier (:carrier authority)]
    (p15-s23-sh07-b51-register-admitted-runtime-context
     products carrier raw route args runtime)))

(defn- p15-s23-sh07-b51-v4-admission-proof
  [raw root-ordinal args]
  (try
    (when (p15-s23-sh07-b51-supplied-raw-v4-preflight? raw)
      (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
        (when (p15-s23-sh07-b51-v4-step3-admit raw authority)
          (when-let [route
                     (p15-s23-sh07-b51-v4-route-and-arity
                      (:products authority) root-ordinal args)]
            {:authority authority :route route}))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ nil)))

(defn p15-s23-sh07-b51-admit-linked-raw-v4
  [raw root-ordinal args runtime]
  (when-let [{:keys [authority route]}
             (p15-s23-sh07-b51-v4-admission-proof raw root-ordinal args)]
    ;; Compatibility admission reports no live token authority. Receipt and
    ;; context lifetime exist only inside the synchronous execution boundary.
    (assoc route
           :products (:products authority)
           :runtime (p15-s23-sh07-b51-unadmitted-runtime-context runtime))))

(defn p15-s23-sh07-b51-execute-linked-raw-v4-public-root
  [raw root-ordinal args runtime]
  (if-let [{:keys [authority route]}
           (p15-s23-sh07-b51-v4-admission-proof raw root-ordinal args)]
    (let [{:keys [runtime context-lease]}
          (p15-s23-sh07-b51-v4-attach-runtime-token
           authority raw route args runtime)
          admitted-runtime runtime]
      (try
        ;; Retain the exact private context strongly for this synchronous
        ;; execution; the identity registry itself holds only weak references.
        (when-not context-lease
          (throw (ex-info "missing admitted SH-07 context lease"
                          {:id "L2-UNKNOWN-CORE-FORM"
                           :reason :unauthenticated-b51-linked-carrier})))
        (let [public-invocation-scope
              (when (contains? #{1 6} root-ordinal)
                (p15-s23-sh07-b51-top-level-public-invocation-scope
                 admitted-runtime))]
          (p15-s23-stage2-runtime-execute-function-scoped
           admitted-runtime (:plan route)
           (get-in route [:root :function]) args public-invocation-scope))
        (finally
          (p15-s23-sh07-b51-unregister-admitted-runtime-context!
           admitted-runtime)
          (java.lang.ref.Reference/reachabilityFence context-lease))))
    (throw (ex-info "unauthenticated Attempt32 public-root invocation"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :unauthenticated-b51-linked-carrier
                     :result-committed? false
                     :output-committed? false}))))
