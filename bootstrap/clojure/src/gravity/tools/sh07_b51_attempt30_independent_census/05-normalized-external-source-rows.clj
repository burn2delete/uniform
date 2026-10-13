
(defn- normalized-external-source-rows
  [source-scan m0s]
  (let [definition-by-name
        (into {} (for [m0 m0s, definition (:definitions m0)]
                   [(:name definition) [m0 definition]]))
        rows
        (for [call (:calls source-scan) :when (= :external (:kind call))]
          (let [[caller-m0 caller-definition]
                (get definition-by-name (:caller call))
                [callee-m0 callee-definition]
                (get definition-by-name (:callee call))
                binding
                (first (filter #(= (:callee call) (:symbol %))
                               (:external-bindings caller-m0)))]
            (ensure! (and caller-m0 callee-m0 binding)
                     :source-external-binding-missing)
            (let [expected-owner-edge
                  (cond
                    (= (:owner caller-m0) (:owner callee-m0))
                    (case (:owner caller-m0)
                      :r1-producer :producer-internal
                      :r6-independent-verifier :root6-internal
                      nil)
                    (and (= :r8-finalizer (:owner caller-m0))
                         (= :r1-producer (:owner callee-m0)))
                    :root8-to-producer-interface
                    :else nil)]
              (ensure! (and expected-owner-edge
                            (= expected-owner-edge (:owner-edge binding))
                            (= (:module-ordinal callee-m0)
                             (:callee-module-ordinal binding))
                          (= (:module callee-m0) (:callee-module binding))
                          (= (:source-sha256 callee-m0)
                             (:callee-source-sha256 binding))
                          (= (:source-hash callee-definition)
                             (:callee-definition-source-hash binding))
                          (= (:arity call) (:arity binding)
                             (:arity callee-definition)))
                       :source-external-binding-mismatch))
            {:caller-module-source-id (:module-source-id caller-m0)
             :caller-function (:caller call)
             :caller-function-source-hash (:source-hash caller-definition)
             :callsite-path (:callsite-path call)
             :callee-module-source-id (:module-source-id callee-m0)
             :callee-function (:callee call)
             :callee-function-source-hash (:source-hash callee-definition)
             :arity (:arity call) :owner-edge (:owner-edge binding)
             :authorization :authenticated-b51-namespace-external}))
        ordered
        (vec
         (sort (fn [left right]
                 (let [module-order
                       (compare (:module-ordinal
                                 (first (get definition-by-name
                                             (:caller-function left))))
                                (:module-ordinal
                                 (first (get definition-by-name
                                             (:caller-function right)))))
                       caller-order (compare (str (:caller-function left))
                                             (str (:caller-function right)))]
                   (cond
                     (not (zero? module-order)) module-order
                     (not (zero? caller-order)) caller-order
                     :else (compare-path (:callsite-path left)
                                         (:callsite-path right)))))
               rows))]
    (doseq [m0 m0s]
      (let [module-id (:module-source-id m0)
            called (mapv :callee-function
                         (filter #(= module-id (:caller-module-source-id %))
                                 ordered))
            bound (mapv :symbol (:external-bindings m0))]
        (ensure! (= (set called) (set bound)) :external-binding-coverage)
        (ensure! (= (count bound) (count (set bound)))
                 :duplicate-external-binding)))
    (mapv #(assoc %1 :ordinal %2) ordered (range))))

(defn- verify-external-bijection!
  [source-scan m0s m1 m2-scan]
  (let [source-rows (normalized-external-source-rows source-scan m0s)
        m1-rows (:edges m1)
        m2-external (filterv #(= :external (:kind %)) (:calls m2-scan))
        m1-by-ordinal (into {} (map (juxt :ordinal identity) m1-rows))]
    (ensure! (= source-rows m1-rows) :source-m1-external-bijection)
    (ensure! (= (count m1-rows) (count m2-external))
             :m1-m2-external-count)
    (doseq [call m2-external]
      (let [link (:external-link call)
            edge (get m1-by-ordinal (:edge-ordinal link))]
        (ensure! (and (= #{:artifact :manifest-id :edge-ordinal}
                          (set (keys link)))
                      (= :gravity/sh07-b51-external-call-v1 (:artifact link))
                      (= (:manifest-id m1) (:manifest-id link))
                      (= (:caller call) (:caller-function edge))
                      (= (:callee call) (:callee-function edge))
                      (= (:arity call) (:arity edge)))
                 :m1-m2-external-link)))
    (ensure! (= (count m2-external)
                (count (set (map #(get-in % [:external-link :edge-ordinal])
                                 m2-external))))
             :duplicate-m2-external-edge)
    {:source-rows source-rows :m1-rows m1-rows}))

(defn- scalar-sort-key [row]
  (mapv (comp locally-ordered-key row)
        [:caller :caller-function-source-hash :builtin :arity
         :compiler-internal?]))

(defn- add-duplicate-ordinals [occurrences]
  (loop [remaining (sort-by scalar-sort-key occurrences)
         previous nil duplicate-ordinal 0
         result []]
    (if (empty? remaining)
      result
      (let [row (first remaining)
            coordinates (scalar-sort-key row)
            duplicate (if (= previous coordinates) (inc duplicate-ordinal) 0)]
        (recur (rest remaining) coordinates duplicate
               (conj result (assoc row :duplicate-ordinal duplicate)))))))

(defn- normalized-m2-builtin-occurrences [m2-scan m0s module-ordinal]
  (let [definition-hash
        (into {} (map (juxt :name :source-hash)
                      (:definitions (nth m0s module-ordinal))))]
    (add-duplicate-ordinals
     (mapv (fn [call]
             {:caller (:caller call)
              :caller-function-source-hash
              (get definition-hash (:caller call))
              :builtin (:callee call) :arity (:arity call)
              :compiler-internal? (= :internal-builtin (:kind call))})
           (filter #(and (= module-ordinal (:module-ordinal %))
                         (contains? #{:builtin :internal-builtin} (:kind %)))
                   (:calls m2-scan))))))

(defn- normalized-m0-builtin-occurrences [m0]
  (add-duplicate-ordinals (mapv identity (:builtin-calls m0))))

(defn- verify-builtin-parity!
  [m2-scan m0s inputs]
  (mapv
   (fn [module-ordinal]
     (let [m0 (nth m0s module-ordinal)
           m2-occurrences
           (normalized-m2-builtin-occurrences m2-scan m0s module-ordinal)
           m0-occurrences (normalized-m0-builtin-occurrences m0)]
       (ensure! (= m0-occurrences m2-occurrences) :m0-m2-builtin-parity)
       (let [root
             (h {:domain :gravity/sh07-b51-ordered-builtin-multiset-root-v1
                 :schema-version 1 :module-ordinal module-ordinal
                 :occurrences m2-occurrences})]
         (h {:domain :gravity/sh07-b51-builtin-parity-commitment-v1
             :schema-version 1 :module-ordinal module-ordinal
             :m0-id (nth (:m0-ids inputs) module-ordinal)
             :m2-id (nth (:m2-ids inputs) module-ordinal)
             :occurrence-count (count m2-occurrences)
             :ordered-multiset-root root :parity-complete true}))))
   (range 7)))

(defn- provenance-commitment!
  [m2-scan m0s m1 inputs]
  (let [internal-set (set internal-builtins)
        occurrences
        (mapv
         (fn [call]
           (let [module-ordinal (:module-ordinal call)
                 m0 (nth m0s module-ordinal)
                 definition
                 (first (filter #(= (:caller call) (:name %))
                                (:definitions m0)))
                 provenance (:provenance call)]
             (ensure! (and (= provenance-map-keys (set (keys provenance)))
                           (= :gravity/sh07-compiler-internal-call-provenance-v2
                              (:artifact provenance))
                           (= (:manifest-id m1) (:manifest-id provenance))
                           (= (:module-source-id m0)
                              (:module-source-id provenance))
                           (= (:source-sha256 m0) (:source-sha256 provenance))
                           (= (:caller call) (:function provenance))
                           (= (:source-hash definition)
                              (:function-source-hash provenance))
                           (= (:callee call) (:callee provenance)))
                      :compiler-internal-provenance)
             {:module-ordinal module-ordinal
              :instruction-ordinal (:instruction-ordinal call)
              :provenance provenance}))
         (filterv #(= :internal-builtin (:kind %)) (:calls m2-scan)))
        observed (set (map #(get-in % [:provenance :callee]) occurrences))]
    (ensure! (= internal-set observed) :internal-builtin-set)
    (ensure! (= occurrences
                (vec (sort-by (juxt :module-ordinal :instruction-ordinal)
                              occurrences))) :provenance-order)
    (let [root
          (h {:domain :gravity/sh07-b51-provenance-occurrence-root-v1
              :schema-version 1 :occurrences occurrences})]
      (h {:domain :gravity/sh07-b51-provenance-completeness-commitment-v1
          :schema-version 1 :m0-ids (:m0-ids inputs) :m1-id (:m1-id inputs)
          :m2-ids (:m2-ids inputs) :internal-builtins internal-builtins
          :occurrence-count (count occurrences) :occurrence-root root
          :provenance-complete true}))))

(defn- reachable-functions! [bundle-core m2-scan]
  (let [known (set (keys (:owner-by-function m2-scan)))
        starts (mapv :function (:public-roots bundle-core))
        by-caller (group-by :caller
                            (filter #(contains? #{:local :external} (:kind %))
                                    (:calls m2-scan)))]
    (ensure! (every? known starts) :unknown-public-root)
    (loop [queue (into clojure.lang.PersistentQueue/EMPTY starts)
           reached #{}]
      (if (empty? queue)
        reached
        (let [function (peek queue)
              remaining (pop queue)]
          (if (contains? reached function)
            (recur remaining reached)
            (let [callees (map :callee (get by-caller function []))]
              (ensure! (every? known callees) :unresolved-reachable-call)
              (recur (into remaining callees) (conj reached function)))))))))

(defn- qualified-coordinate [m0s m2s module-ordinal function]
  {:module-ordinal module-ordinal
   :module-source-id (:module-source-id (nth m0s module-ordinal))
   :plan-id (:plan-id (nth m2s module-ordinal))
   :function function})
