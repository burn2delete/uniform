
(defn- validate-builtin-parity! [raw m2 m0s]
  (let [raw-by-module (group-by :module-ordinal
                                (filter #(= :builtin-call (:classification %))
                                        (:records raw)))
        m2-by-module (group-by :module-ordinal
                               (filter #(= :builtin-call
                                           (:call-classification %))
                                       (:records m2)))]
    (mapv
     (fn [ordinal descriptor]
       (let [m0-occurrences
             (normalized-builtin-occurrences (:builtin-calls descriptor))
             raw-occurrences
             (normalized-builtin-occurrences
              (mapv (fn [record]
                      {:caller (:caller record)
                       :caller-function-source-hash
                       (:caller-function-source-hash record)
                       :builtin (:callee record) :arity (:arity record)
                       :compiler-internal?
                       (contains? (set internal-builtins) (:callee record))})
                    (get raw-by-module ordinal [])))
             m2-occurrences
             (normalized-builtin-occurrences
              (mapv (fn [record]
                      {:caller (:caller record)
                       :caller-function-source-hash
                       (:caller-function-source-hash record)
                       :builtin (:callee record) :arity (:arity record)
                       :compiler-internal?
                       (contains? (set internal-builtins) (:callee record))})
                    (get m2-by-module ordinal [])))]
         (require! (= m0-occurrences raw-occurrences)
                   :source-m0-builtin-parity {:module-ordinal ordinal})
         (require! (= m0-occurrences m2-occurrences)
                   :m0-m2-builtin-parity {:module-ordinal ordinal})
         (let [source-order
               (mapv (fn [record]
                       {:caller (:caller record)
                        :caller-function-source-hash
                        (:caller-function-source-hash record)
                        :builtin (:callee record)
                        :arity (:arity record)
                        :compiler-internal?
                        (contains? (set internal-builtins)
                                   (:callee record))})
                     (sort-by (juxt (comp str :caller)
                                    :physical-ordinal)
                              (get raw-by-module ordinal [])))]
           (require! (= source-order (:builtin-calls descriptor))
                     :source-m0-builtin-order {:module-ordinal ordinal}))
         m0-occurrences))
     (range) m0s)))

(defn- reachable-functions [bundle m2-records]
  (let [calls-by-caller
        (group-by :caller
                  (filter #(contains? #{:local-runtime-call
                                        :external-runtime-call}
                                      (:call-classification %))
                          m2-records))
        roots (mapv :function (:public-roots bundle))]
    (loop [pending roots reached #{}]
      (if (empty? pending)
        reached
        (let [function (peek pending)
              remaining (pop pending)]
          (if (contains? reached function)
            (recur remaining reached)
            (recur (into remaining (map :callee
                                        (get calls-by-caller function [])))
                   (conj reached function))))))))

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
