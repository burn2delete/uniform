
(defn- p15-s23-sh07-b51-definition-by-name [descriptor function]
  (first (filter #(= function (:name %)) (:definitions descriptor))))

(defn- p15-s23-sh07-b51-link-manifest-valid*
  [descriptors manifest]
  (let [by-id (into {} (map (juxt :module-source-id identity)) descriptors)
        ordinal-by-id (into {} (map (juxt :module-source-id :module-ordinal))
                            descriptors)
        edges (:edges manifest)
        ordered
        (vec (sort-by (fn [row]
                        [(get ordinal-by-id (:caller-module-source-id row))
                         (str (:caller-function row))
                         (count (:callsite-path row))
                         (:callsite-path row)])
                      edges))
        root8 (when (and (vector? descriptors) (= 7 (count descriptors)))
                (nth descriptors 6))
        root8-edges
        (filter #(= (:module-source-id root8)
                    (:caller-module-source-id %)) edges)
        root8-root1-edges
        (filter #(and (= 'sh07-b51-finalize-rejection
                         (:caller-function %))
                      (= 'sh07-b51-build-template
                         (:callee-function %))) root8-edges)
        root6-source-ids
        (if (and (vector? descriptors) (= 7 (count descriptors)))
          (set (map :module-source-id (subvec descriptors 4 6)))
          #{})
        root6-cross-edges
        (filter #(and (contains? root6-source-ids
                                 (:caller-module-source-id %))
                      (not (contains? root6-source-ids
                                      (:callee-module-source-id %))))
                edges)
        source-census (p15-s23-sh07-b51-derive-callsite-census)
        projected-census
        (mapv (fn [edge]
                (let [caller (get by-id (:caller-module-source-id edge))
                      callee (get by-id (:callee-module-source-id edge))]
                  {:ordinal (:ordinal edge)
                   :caller-module-ordinal (:module-ordinal caller)
                   :caller-module (:module caller)
                   :caller-function (:caller-function edge)
                   :callsite-path (:callsite-path edge)
                   :callee-module-ordinal (:module-ordinal callee)
                   :callee-module (:module callee)
                   :callee-function (:callee-function edge)
                   :arity (:arity edge)
                   :owner-edge (:owner-edge edge)}))
              edges)]
    (and (= 7 (count descriptors))
         (= (range 7) (map :module-ordinal descriptors))
         (every? p15-s23-sh07-b51-module-source-valid? descriptors)
         (p15-s23-sh07-b51-exact-keys? manifest p15-s23-sh07-b51-m1-keys)
         (= :gravity/sh07-b51-link-manifest-v1 (:artifact manifest))
         (= 1 (:schema-version manifest))
         (= (mapv :module-source-id descriptors)
            (:module-source-ids manifest))
         (vector? edges)
         (= ordered edges)
         (= (vec (range (count edges))) (mapv :ordinal edges))
         (= source-census projected-census)
         (= (count edges)
            (count (set (map (juxt :caller-module-source-id
                                   :caller-function :callsite-path)
                             edges))))
         (= p15-s23-sh07-b51-root8-external-callees
            (mapv :symbol (:external-bindings root8)))
         (= 1 (count root8-root1-edges))
         (empty? root6-cross-edges)
         (every? (fn [descriptor]
                   (= (set (map :symbol (:external-bindings descriptor)))
                      (set (map :callee-function
                                (filter #(= (:module-source-id descriptor)
                                            (:caller-module-source-id %))
                                        edges)))))
                 descriptors)
         (every?
          (fn [row]
            (let [caller (get by-id (:caller-module-source-id row))
                  callee (get by-id (:callee-module-source-id row))
                  caller-definition
                  (p15-s23-sh07-b51-definition-by-name
                   caller (:caller-function row))
                  callee-definition
                  (p15-s23-sh07-b51-definition-by-name
                   callee (:callee-function row))
                  external (first (filter
                                   #(= (:callee-function row) (:symbol %))
                                   (:external-bindings caller)))
                  expected-owner-edge
                  (p15-s23-sh07-b51-owner-edge-kind
                   (:owner caller) (:owner callee))]
              (and (p15-s23-sh07-b51-edge-row-valid? row)
                   caller callee caller-definition callee-definition external
                   (= (:caller-function-source-hash row)
                      (:source-hash caller-definition))
                   (= (:callee-function-source-hash row)
                      (:source-hash callee-definition))
                   (= (:arity row) (:arity callee-definition) (:arity external))
                   (= (:module-ordinal callee)
                      (:callee-module-ordinal external))
                   (= (:module callee) (:callee-module external))
                   (= (:source-sha256 callee)
                      (:callee-source-sha256 external))
                   (= (:source-hash callee-definition)
                      (:callee-definition-source-hash external))
                   (= expected-owner-edge
                      (:owner-edge row) (:owner-edge external)))))
          edges)
         (= (:manifest-id manifest)
            (p15-s23-sh07-b51-manifest-id manifest)))))

(defn p15-s23-sh07-b51-link-manifest-valid? [descriptors manifest]
  (try
    (boolean (p15-s23-sh07-b51-link-manifest-valid* descriptors manifest))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-seal-link-manifest [descriptors edges]
  (let [ordinal-by-id
        (into {} (map (juxt :module-source-id :module-ordinal)) descriptors)
        ordered
        (vec (sort-by (fn [row]
                        [(get ordinal-by-id (:caller-module-source-id row))
                         (str (:caller-function row))
                         (count (:callsite-path row))
                         (:callsite-path row)])
                      edges))
        base {:artifact :gravity/sh07-b51-link-manifest-v1
              :schema-version 1
              :module-source-ids (mapv :module-source-id descriptors)
              :edges (mapv #(assoc %1 :ordinal %2) ordered (range))}
        manifest (assoc base :manifest-id
                        (p15-s23-sh07-b51-manifest-id base))]
    (when-not (p15-s23-sh07-b51-link-manifest-valid?
               descriptors manifest)
      (let [by-id (into {} (map (juxt :module-source-id identity)) descriptors)
            projected
            (mapv (fn [edge]
                    (let [caller (get by-id (:caller-module-source-id edge))
                          callee (get by-id (:callee-module-source-id edge))]
                      {:ordinal (:ordinal edge)
                       :caller-module-ordinal (:module-ordinal caller)
                       :caller-module (:module caller)
                       :caller-function (:caller-function edge)
                       :callsite-path (:callsite-path edge)
                       :callee-module-ordinal (:module-ordinal callee)
                       :callee-module (:module callee)
                       :callee-function (:callee-function edge)
                       :arity (:arity edge)
                       :owner-edge (:owner-edge edge)}))
                  (:edges manifest))
            source-census (p15-s23-sh07-b51-derive-callsite-census)]
        (throw (ex-info "invalid SH-07 B51 M1 manifest"
                        {:id "L2-UNKNOWN-CORE-FORM"
                         :reason :unauthenticated-b51-external-link
                         :descriptor-valid?
                         (mapv p15-s23-sh07-b51-module-source-valid?
                               descriptors)
                         :source-census-match? (= source-census projected)
                         :source-census-count (count source-census)
                         :projected-count (count projected)
                         :root8-external-symbols
                         (mapv :symbol
                               (:external-bindings (nth descriptors 6)))}))))
    manifest))

(defn p15-s23-sh07-b51-enrich-census-edges
  [descriptors census-edges]
  (let [by-ordinal (into {} (map (juxt :module-ordinal identity)) descriptors)]
    (when-not (and (vector? census-edges)
                   (= (vec (range (count census-edges)))
                      (mapv :ordinal census-edges))
                   (every? #(p15-s23-sh07-b51-exact-keys?
                             % p15-s23-sh07-b51-census-edge-keys)
                           census-edges))
      (throw (ex-info "invalid SH-07 B51 callsite census"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    (mapv
     (fn [row]
       (let [caller (get by-ordinal (:caller-module-ordinal row))
             callee (get by-ordinal (:callee-module-ordinal row))
             caller-definition
             (p15-s23-sh07-b51-definition-by-name caller (:caller-function row))
             callee-definition
             (p15-s23-sh07-b51-definition-by-name callee (:callee-function row))]
         (when-not (and caller callee caller-definition callee-definition
                        (= (:caller-module row) (:module caller))
                        (= (:callee-module row) (:module callee)))
           (throw (ex-info "callsite census does not join module sources"
                           {:id "L2-UNKNOWN-CORE-FORM"
                            :reason :unauthenticated-b51-external-link
                            :ordinal (:ordinal row)})))
         {:ordinal (:ordinal row)
          :caller-module-source-id (:module-source-id caller)
          :caller-function (:caller-function row)
          :caller-function-source-hash (:source-hash caller-definition)
          :callsite-path (:callsite-path row)
          :callee-module-source-id (:module-source-id callee)
          :callee-function (:callee-function row)
          :callee-function-source-hash (:source-hash callee-definition)
          :arity (:arity row)
          :owner-edge (:owner-edge row)
          :authorization :authenticated-b51-namespace-external}))
     census-edges)))
