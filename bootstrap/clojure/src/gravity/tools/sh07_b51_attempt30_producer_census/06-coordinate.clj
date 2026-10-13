
(defn- coordinate [m0s m2s module-ordinal function]
  {:module-ordinal module-ordinal
   :module-source-id (:module-source-id (nth m0s module-ordinal))
   :plan-id (:plan-id (nth m2s module-ordinal))
   :function function})

(defn- semantic-category-rows [m0s m1 m2s bundle m2-records reachable]
  (let [owner (into {}
                    (for [descriptor m0s, definition (:definitions descriptor)]
                      [(:name definition) (:module-ordinal descriptor)]))
        ordered-members (sort-by (fn [function]
                                   [(get owner function) (str function)])
                                 reachable)
        definitions (definition-map m0s)
        members
        (mapv (fn [ordinal function]
                {:member-ordinal ordinal
                 :coordinate (coordinate m0s m2s (get owner function) function)})
              (range) ordered-members)
        functions
        (mapv (fn [ordinal function]
                (let [module-ordinal (get owner function)]
                  {:member-ordinal ordinal
                   :coordinate (coordinate m0s m2s module-ordinal function)
                   :definition-source-hash
                   (:source-hash (get definitions [module-ordinal function]))}))
              (range) ordered-members)
        reachable-calls (filter #(contains? reachable (:caller %)) m2-records)
        local-calls (filterv #(= :local-runtime-call
                                 (:call-classification %)) reachable-calls)
        local-rows
        (mapv (fn [ordinal row]
                {:edge-ordinal ordinal
                 :caller (coordinate m0s m2s (:module-ordinal row)
                                     (:caller row))
                 :callee (coordinate m0s m2s (get owner (:callee row))
                                     (:callee row))
                 :arity (:arity row)
                 :source-callsite-ordinal ordinal})
              (range) local-calls)
        reachable-external
        (filterv #(contains? reachable (:caller-function %)) (:edges m1))
        route-by-ordinal (into {} (map (juxt :edge-ordinal identity))
                               (:routes bundle))
        descriptor-by-id (into {} (map (juxt :module-source-id identity)) m0s)
        external-rows
        (mapv
         (fn [semantic-ordinal edge]
           (let [caller (get descriptor-by-id (:caller-module-source-id edge))
                 callee (get descriptor-by-id (:callee-module-source-id edge))]
             {:semantic-edge-ordinal semantic-ordinal
              :m1-edge-ordinal (:ordinal edge)
              :manifest-id (:manifest-id m1)
              :caller (coordinate m0s m2s (:module-ordinal caller)
                                  (:caller-function edge))
              :callee (coordinate m0s m2s (:module-ordinal callee)
                                  (:callee-function edge))
              :arity (:arity edge)
              :owner-edge (:owner-edge edge)
              :route (get route-by-ordinal (:ordinal edge))}))
         (range) reachable-external)
        builtin-rows-source
        (mapcat (fn [descriptor]
                  (filter #(contains? reachable (:caller %))
                          (:builtin-calls descriptor)))
                m0s)
        builtin-rows
        (mapv (fn [ordinal row]
                {:occurrence-ordinal ordinal
                 :caller (coordinate m0s m2s (get owner (:caller row))
                                     (:caller row))
                 :builtin (:builtin row)
                 :arity (:arity row)
                 :compiler-internal? (:compiler-internal? row)
                 :source-callsite-ordinal ordinal})
              (range) builtin-rows-source)
        plan-ordinal (into {} (map-indexed (fn [ordinal plan]
                                            [(:plan-id plan) ordinal]) m2s))
        root-rows
        (mapv (fn [root]
                {:root-ordinal (:root-ordinal root)
                 :coordinate (coordinate m0s m2s
                                         (get plan-ordinal (:plan-id root))
                                         (:function root))
                 :arity (:arity root)})
              (:public-roots bundle))]
    (array-map
     :roots root-rows
     :members members
     :functions functions
     :local-runtime-call-edges local-rows
     :external-runtime-call-edges external-rows
     :referenced-builtins builtin-rows
     :module-contracts
     (mapv (fn [descriptor]
             {:module-ordinal (:module-ordinal descriptor)
              :module-source-id (:module-source-id descriptor)})
           m0s))))

(defn- retained-profile-within? [value class-name]
  (let [profile (value-profile value)
        key-for (fn [suffix]
                  (keyword (str "maximum-" class-name "-" suffix)))
        c11-bytes
        (alength (.getBytes
                  (binding [*print-length* nil *print-level* nil
                            *print-meta* true]
                    (pr-str value))
                  java.nio.charset.StandardCharsets/UTF_8))]
    (and (<= (:nodes profile) (get category-schedule (key-for "nodes")))
         (<= (:depth profile) (get category-schedule (key-for "depth")))
         (<= (:width profile) (get category-schedule (key-for "width")))
         (<= (:scalar-utf8 profile)
             (get category-schedule (key-for "scalar-utf8")))
         (<= c11-bytes (get category-schedule (key-for "c11-bytes")))
         (<= (:canonical-reader-bytes profile)
             (get category-schedule (key-for "canonical-reader-bytes"))))))

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
