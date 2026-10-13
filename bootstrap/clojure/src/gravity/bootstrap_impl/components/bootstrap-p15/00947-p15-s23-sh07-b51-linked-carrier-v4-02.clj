
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
