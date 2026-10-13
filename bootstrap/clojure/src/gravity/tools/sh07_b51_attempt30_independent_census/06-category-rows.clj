
(defn- category-rows
  [m0s m1 m2s bundle-core m2-scan reachable]
  (let [owner (:owner-by-function m2-scan)
        coordinate #(qualified-coordinate m0s m2s %1 %2)
        members (sort-by (fn [function] [(get owner function) (str function)])
                         reachable)
        member-rows
        (mapv (fn [ordinal function]
                {:member-ordinal ordinal
                 :coordinate (coordinate (get owner function) function)})
              (range) members)
        definition-hash
        (into {} (for [m0 m0s, definition (:definitions m0)]
                   [[(:module-ordinal m0) (:name definition)]
                    (:source-hash definition)]))
        function-rows
        (mapv (fn [ordinal function]
                (let [module-ordinal (get owner function)]
                  {:member-ordinal ordinal
                   :coordinate (coordinate module-ordinal function)
                   :definition-source-hash
                   (get definition-hash [module-ordinal function])}))
              (range) members)
        reachable-calls (filterv #(contains? reachable (:caller %))
                                 (:calls m2-scan))
        local-calls (filterv #(= :local (:kind %)) reachable-calls)
        local-rows
        (mapv (fn [ordinal call]
                {:edge-ordinal ordinal
                 :caller (coordinate (:module-ordinal call) (:caller call))
                 :callee (coordinate (get owner (:callee call)) (:callee call))
                 :arity (:arity call) :source-callsite-ordinal ordinal})
              (range) local-calls)
        reachable-external
        (filterv #(contains? reachable (:caller-function %)) (:edges m1))
        routes (into {} (map (juxt :edge-ordinal identity)
                             (:routes bundle-core)))
        m0-by-id (into {} (map (juxt :module-source-id identity)) m0s)
        external-rows
        (mapv
         (fn [ordinal edge]
           (let [caller (get m0-by-id (:caller-module-source-id edge))
                 callee (get m0-by-id (:callee-module-source-id edge))]
             {:semantic-edge-ordinal ordinal
              :m1-edge-ordinal (:ordinal edge) :manifest-id (:manifest-id m1)
              :caller (coordinate (:module-ordinal caller)
                                  (:caller-function edge))
              :callee (coordinate (:module-ordinal callee)
                                  (:callee-function edge))
              :arity (:arity edge) :owner-edge (:owner-edge edge)
              :route (get routes (:ordinal edge))}))
         (range) reachable-external)
        builtin-calls
        (filterv #(contains? #{:builtin :internal-builtin} (:kind %))
                 reachable-calls)
        builtin-rows
        (mapv (fn [ordinal call]
                {:occurrence-ordinal ordinal
                 :caller (coordinate (:module-ordinal call) (:caller call))
                 :builtin (:callee call) :arity (:arity call)
                 :compiler-internal? (= :internal-builtin (:kind call))
                 :source-callsite-ordinal ordinal})
              (range) builtin-calls)
        root-rows
        (mapv (fn [root]
                (let [module-ordinal (get owner (:function root))]
                  {:root-ordinal (:root-ordinal root)
                   :coordinate (coordinate module-ordinal (:function root))
                   :arity (:arity root)})) (:public-roots bundle-core))]
    (array-map
     :roots root-rows :members member-rows :functions function-rows
     :local-runtime-call-edges local-rows
     :external-runtime-call-edges external-rows
     :referenced-builtins builtin-rows
     :module-contracts
     (mapv (fn [m0] {:module-ordinal (:module-ordinal m0)
                     :module-source-id (:module-source-id m0)}) m0s))))

(defn- class-profile-within? [value prefix]
  (let [profile (value-profile value)
        key-for (fn [suffix] (keyword (str "maximum-" prefix "-" suffix)))]
    (and (<= (:nodes profile) (get category-schedule (key-for "nodes")))
         (<= (:depth profile) (get category-schedule (key-for "depth")))
         (<= (:width profile) (get category-schedule (key-for "width")))
         (<= (:scalar-utf8 profile)
             (get category-schedule (key-for "scalar-utf8")))
         (<= (utf8-count (pr-str value))
             (get category-schedule (key-for "c11-bytes")))
         (<= (:canonical-reader-bytes profile)
             (get category-schedule (key-for "canonical-reader-bytes"))))))

(defn- category-commitment [category rows]
  (doseq [row rows]
    (ensure! (class-profile-within? row "row") :category-row-bound))
  (let [leaf-groups (if (empty? rows) [[]] (partition-all 16 rows))
        leaves
        (mapv (fn [ordinal group]
                {:domain :gravity/sh07-b51-linked-closure-leaf-v2
                 :schema-version 2 :category category :shard-ordinal ordinal
                 :first-row-ordinal (* ordinal 16) :row-count (count group)
                 :category-row-count (count rows) :rows (vec group)})
              (range) leaf-groups)]
    (doseq [leaf leaves]
      (ensure! (class-profile-within? leaf "leaf") :category-leaf-bound))
    (loop [level 1
           children
           (mapv (fn [ordinal leaf]
                   {:child-ordinal ordinal
                    :first-row-ordinal (:first-row-ordinal leaf)
                    :row-count (:row-count leaf) :digest (h leaf)})
                 (range) leaves)]
      (ensure! (<= level 3) :category-fold-level-bound)
      (let [groups (partition-all 16 children)
            folds
            (mapv
             (fn [group-ordinal group]
               {:domain :gravity/sh07-b51-linked-closure-fold-v2
                :schema-version 2 :category category :level level
                :group-ordinal group-ordinal
                :first-child-ordinal (* group-ordinal 16)
                :child-count (count group)
                :row-count (reduce + (map :row-count group))
                :children (vec group)})
             (range) groups)]
        (doseq [fold folds]
          (ensure! (class-profile-within? fold "fold") :category-fold-bound))
        (if (= 1 (count folds))
          (let [commitment
                {:category category :row-count (count rows)
                 :leaf-count (count leaves) :fold-level-count level
                 :root-digest (h (first folds))}]
            (ensure! (class-profile-within? commitment "commitment")
                     :category-commitment-bound)
            commitment)
          (recur
           (inc level)
           (mapv (fn [ordinal fold]
                   {:child-ordinal ordinal
                    :first-row-ordinal
                    (:first-row-ordinal (first (:children fold)))
                    :row-count (:row-count fold) :digest (h fold)})
                 (range) folds)))))))

(defn- build-category-commitments [rows]
  (mapv #(category-commitment % (get rows %)) categories))

(def physical-census-keys
  #{:module-count :module-censuses :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :semantic-row-count
    :module-binding-occupancies :maximum-binding-occupancy :binding-ceiling})

(defn- physical-census
  [m0s m2s m2-scan reachable rows]
  (let [calls (:calls m2-scan)
        owner (:owner-by-function m2-scan)
        module-censuses
        (mapv
         (fn [module-ordinal]
           (let [m0 (nth m0s module-ordinal)
                 plan (nth m2s module-ordinal)
                 module-calls (filter #(= module-ordinal (:module-ordinal %))
                                      calls)
                 reached-calls (filter #(contains? reachable (:caller %))
                                       module-calls)
                 count-kind (fn [kind xs] (count (filter #(= kind (:kind %)) xs)))
                 builtin-count (fn [xs] (count (filter
                                                #(contains? #{:builtin
                                                              :internal-builtin}
                                                            (:kind %)) xs)))]
             {:module-ordinal module-ordinal :module (:module m0)
              :logical-source-path (:logical-source-path m0) :owner (:owner m0)
              :source-byte-count (:source-byte-count m0)
              :source-sha256 (:source-sha256 m0)
              :module-source-id (:module-source-id m0) :plan-id (:plan-id plan)
              :physical-definition-count (count (:definitions m0))
              :reachable-definition-count
              (count (filter #(and (= module-ordinal (get owner %))
                                   (contains? reachable %)) (keys owner)))
              :physical-local-runtime-callsite-count
              (count-kind :local module-calls)
              :reachable-local-runtime-callsite-count
              (count-kind :local reached-calls)
              :physical-external-runtime-callsite-count
              (count-kind :external module-calls)
              :reachable-external-runtime-callsite-count
              (count-kind :external reached-calls)
              :physical-referenced-builtin-occurrence-count
              (builtin-count module-calls)
              :reachable-referenced-builtin-occurrence-count
              (builtin-count reached-calls)
              :core-binding-count (:core-binding-count m0)
              :lexical-binding-count (:lexical-binding-count m0)
              :external-binding-count (count (:external-bindings m0))
              :aggregate-binding-count (:aggregate-binding-count m0)}))
         (range 7))
        total (fn [key] (reduce + (map key module-censuses)))
        occupancies (mapv :aggregate-binding-count module-censuses)]
    (ensure! (every? #(<= % 2440) occupancies) :binding-ceiling)
    {:module-count 7 :module-censuses module-censuses
     :physical-definition-count (total :physical-definition-count)
     :reachable-definition-count (total :reachable-definition-count)
     :physical-local-runtime-callsite-count
     (total :physical-local-runtime-callsite-count)
     :reachable-local-runtime-callsite-count
     (total :reachable-local-runtime-callsite-count)
     :physical-external-runtime-callsite-count
     (total :physical-external-runtime-callsite-count)
     :reachable-external-runtime-callsite-count
     (total :reachable-external-runtime-callsite-count)
     :physical-referenced-builtin-occurrence-count
     (total :physical-referenced-builtin-occurrence-count)
     :reachable-referenced-builtin-occurrence-count
     (total :reachable-referenced-builtin-occurrence-count)
     :semantic-row-count (reduce + (map count (vals rows)))
     :module-binding-occupancies occupancies
     :maximum-binding-occupancy (apply max occupancies)
     :binding-ceiling 2440}))

(defn- transcript-artifact
  [artifact record-domain checker inputs records row-count]
  (let [record-root
        (h {:domain record-domain :schema-version 1
            :checker-algorithm-role (:algorithm-role checker)
            :records records})
        base {:artifact artifact :schema-version 1 :checker checker
              :inputs inputs :node-count (count records) :row-count row-count
              :record-root record-root}
        artifact-id
        (h {:domain :gravity/sh07-b51-census-transcript-artifact-id-v1
            :schema-version 1 :artifact artifact :checker checker
            :inputs inputs :node-count (count records) :row-count row-count
            :record-root record-root})]
    (assoc base :records records :artifact-id artifact-id)))

(defn transcript-descriptor [artifact]
  (select-keys artifact [:artifact :schema-version :node-count :row-count
                         :artifact-id :record-root]))
