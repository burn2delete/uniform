
(defn- validate-envelope! [input]
  (require! (exact-keys? input input-keys) :input-schema)
  (require! (and (sha256-id? (:architecture-report-sha256 input))
                 (git-id? (:snapshot-commit input))
                 (git-id? (:snapshot-tree input))
                 (sha256-id? (:predecessor-executable-descriptor-id input))
                 (digest-vector? (:module-source-identities input) 7)
                 (and (vector? (:module-source-byte-counts input))
                      (= 7 (count (:module-source-byte-counts input)))
                      (every? nonnegative-integer?
                              (:module-source-byte-counts input)))
                 (sha256-id? (:emitter-source-id input))
                 (sha256-id? (:emitter-semantic-id input))
                 (exact-keys? (:provenance input) input-provenance-keys))
            :input-scalar-envelope)
  (validate-checker! (:checker input))
  (let [indexes (source-indexes (:source-forms input) (:m0s input))]
    (validate-m0s! (:m0s input) (:source-forms input) indexes)
    (validate-m1! (:m0s input) (:m1 input))
    (validate-plan-joins! (:m0s input) (:m1 input) (:m2s input)
                          (:emitter-source-id input)
                          (:emitter-semantic-id input))
    (validate-bundle-core! (:m0s input) (:m1 input) (:m2s input)
                           (:bundle-core input))
    (let [derived (derive-certificate-inputs input)]
      (require! (= (:module-source-identities input)
                   (:module-source-identities derived))
                :module-source-identity-join)
      (require! (= (:module-source-byte-counts input)
                   (:module-source-byte-counts derived))
                :module-source-byte-count-join)
      {:indexes indexes :certificate-inputs derived})))

(defn- definition-map [m0s]
  (into {}
        (for [descriptor m0s, row (:definitions descriptor)]
          [[(:module-ordinal descriptor) (:name row)] row])))

(defn- raw-call-records [raw]
  (filterv #(contains? #{:local-runtime-call :external-runtime-call
                         :builtin-call} (:classification %))
           (:records raw)))

(defn- m2-call-records [m2]
  (filterv :semantic-row (:records m2)))

(defn- normalized-call-frequencies [records classification-key classification]
  (frequencies
   (map #(select-keys % [:module-ordinal :caller
                         :caller-function-source-hash :callee :arity])
        (filter #(= classification (classification-key %)) records))))

(defn- owner-edge [caller callee]
  (cond
    (= (:owner caller) (:owner callee))
    (case (:owner caller)
      :r1-producer :producer-internal
      :r6-independent-verifier :root6-internal
      :shared-owner
      (fail! :unknown-same-owner-edge {:owner (:owner caller)}))

    (and (= :r8-finalizer (:owner caller))
         (= :r1-producer (:owner callee)))
    :root8-to-producer-interface

    :else (fail! :forbidden-owner-edge
                 {:caller (:module-ordinal caller)
                  :callee (:module-ordinal callee)})))

(defn- external-source-records [raw m0s]
  (let [owner-by-function (get-in raw [:indexes :owner-by-function])
        m0-by-ordinal (vec m0s)
        definitions (definition-map m0s)
        raw-rows
        (mapv
         (fn [record]
           (let [caller-ordinal (:module-ordinal record)
                 callee-ordinal (get owner-by-function (:callee record))
                 caller (nth m0-by-ordinal caller-ordinal)
                 callee (nth m0-by-ordinal callee-ordinal)
                 caller-definition
                 (get definitions [caller-ordinal (:caller record)])
                 callee-definition
                 (get definitions [callee-ordinal (:callee record)])
                 bindings (filter #(= (:callee record) (:symbol %))
                                  (:external-bindings caller))
                 binding (first bindings)
                 edge (owner-edge caller callee)]
             (require! (= 1 (count bindings)) :external-binding-cardinality
                       {:module-ordinal caller-ordinal
                        :callee (:callee record)})
             (require!
              (= {:symbol (:callee record)
                  :callee-module-ordinal callee-ordinal
                  :callee-module (:module callee)
                  :callee-source-sha256 (:source-sha256 callee)
                  :callee-definition-source-hash (:source-hash callee-definition)
                  :arity (:arity record)
                  :owner-edge edge}
                 binding)
              :external-binding-join {:module-ordinal caller-ordinal
                                      :callee (:callee record)})
             {:caller-module-source-id (:module-source-id caller)
              :caller-function (:caller record)
              :caller-function-source-hash (:source-hash caller-definition)
              :callsite-path (:callsite-path record)
              :callee-module-source-id (:module-source-id callee)
              :callee-function (:callee record)
              :callee-function-source-hash (:source-hash callee-definition)
              :arity (:arity record)
              :owner-edge edge
              :authorization :authenticated-b51-namespace-external}))
         (filter #(= :external-runtime-call (:classification %))
                 (:records raw)))
        by-source (into {} (map (juxt :module-source-id identity)) m0s)
        ordered (sort #(neg? (external-row-compare by-source %1 %2)) raw-rows)]
    (mapv #(assoc %1 :ordinal %2) ordered (range))))

(defn- validate-call-parity! [raw m2 m0s m1]
  (let [raw-calls (raw-call-records raw)
        m2-calls (m2-call-records m2)
        local-raw (normalized-call-frequencies raw-calls :classification
                                                :local-runtime-call)
        local-m2 (normalized-call-frequencies m2-calls :call-classification
                                               :local-runtime-call)
        source-external (external-source-records raw m0s)
        m2-external (filterv #(= :external-runtime-call
                                 (:call-classification %)) m2-calls)]
    (require! (= local-raw local-m2) :source-m2-local-parity)
    (require! (= source-external (:edges m1)) :source-m1-external-bijection)
    (require! (= (count (:edges m1)) (count m2-external))
              :m1-m2-external-count)
    (doseq [record m2-external]
      (let [link (:external-link record)]
        (require! (and (exact-keys? link
                                   #{:artifact :manifest-id :edge-ordinal})
                       (= :gravity/sh07-b51-external-call-v1 (:artifact link))
                       (= (:manifest-id m1) (:manifest-id link))
                       (nonnegative-integer? (:edge-ordinal link))
                       (< (:edge-ordinal link) (count (:edges m1))))
                  :m2-external-link)
        (let [edge (nth (:edges m1) (:edge-ordinal link))]
          (require! (and (= (:caller record) (:caller-function edge))
                         (= (:callee record) (:callee-function edge))
                         (= (:arity record) (:arity edge))
                         (= (:caller-function-source-hash record)
                            (:caller-function-source-hash edge))
                         (= (:module-source-id
                             (nth m0s (:module-ordinal record)))
                            (:caller-module-source-id edge)))
                    :m2-external-edge-join
                    {:edge-ordinal (:edge-ordinal link)}))))
    (require! (= (vec (range (count (:edges m1))))
                 (sort (mapv #(get-in % [:external-link :edge-ordinal])
                             m2-external)))
              :m2-external-link-bijection)
    {:source-external source-external
     :m1-external (:edges m1)}))

(defn- builtin-base [row]
  (select-keys row [:caller :caller-function-source-hash :builtin :arity
                    :compiler-internal?]))

(defn- builtin-record-compare [left right]
  (loop [fields [:caller :caller-function-source-hash :builtin :arity
                 :compiler-internal?]]
    (if (empty? fields)
      0
      (let [field (first fields)
            order (compare (canonical-sort-key (get left field))
                           (canonical-sort-key (get right field)))]
        (if (zero? order) (recur (rest fields)) order)))))

(defn- normalized-builtin-occurrences [rows]
  (let [ordered (sort #(neg? (builtin-record-compare %1 %2))
                      (map builtin-base rows))]
    (loop [remaining ordered previous nil duplicate -1 result []]
      (if (empty? remaining)
        result
        (let [row (first remaining)
              ordinal (if (= row previous) (inc duplicate) 0)]
          (recur (rest remaining) row ordinal
                 (conj result (assoc row :duplicate-ordinal ordinal))))))))

(defn- validate-provenance! [m2 m0s m1]
  (let [internal-set (set internal-builtins)
        occurrences
        (mapv
         (fn [record]
           (let [descriptor (nth m0s (:module-ordinal record))
                 provenance (:provenance record)
                 expected
                 {:artifact :gravity/sh07-compiler-internal-call-provenance-v2
                  :manifest-id (:manifest-id m1)
                  :module-source-id (:module-source-id descriptor)
                  :source-sha256 (:source-sha256 descriptor)
                  :function (:caller record)
                  :function-source-hash (:caller-function-source-hash record)
                  :callee (:callee record)}]
             (require! (= expected provenance) :m2-internal-provenance
                       {:module-ordinal (:module-ordinal record)
                        :instruction-ordinal (:instruction-ordinal record)})
             {:module-ordinal (:module-ordinal record)
              :instruction-ordinal (:instruction-ordinal record)
              :provenance provenance}))
         (filter #(and (= :builtin-call (:call-classification %))
                       (contains? internal-set (:callee %)))
                 (:records m2)))]
    (doseq [record (filter #(= :builtin-call (:call-classification %))
                           (:records m2))]
      (if (contains? internal-set (:callee record))
        (require! (:provenance-present record) :m2-internal-provenance-missing)
        (require! (and (not (:provenance-present record))
                       (nil? (:provenance record)))
                  :m2-ordinary-provenance-present)))
    (require! (= internal-set (set (map #(get-in % [:provenance :callee])
                                        occurrences)))
              :internal-builtin-set)
    occurrences))
