(defn sh07-core-request-preflight!
  [request]
  ;; Validate native carriers before any field lookup or structural hashing.
  (p15-s23-c6c10-carrier-validation "<sh07-core-request>" request)
  (let [value-shape (fn [value]
                      (cond
                        (nil? value) :nil
                        (map? value) :map
                        (vector? value) :vector
                        (set? value) :set
                        (sequential? value) :sequential
                        (coll? value) :collection
                        :else :scalar))
        require-shape (fn [predicate key reason]
                        (when-not (predicate (get request key))
                          (sh07-core-request-diagnostic!
                           request {:reason reason
                                    :observed-shape (value-shape (get request key))})))]
    (when-not (map? request)
      (sh07-core-request-diagnostic!
       {} {:reason :request-map-required :observed-shape (value-shape request)}))
    (doseq [[key reason] [[:lineage :request-lineage-map-required]
                          [:module :request-module-map-required]
                          [:provenance :request-provenance-map-required]]]
      (require-shape map? key reason))
    (doseq [[key reason] [[:forms :request-forms-vector-required]
                          [:binding-table :request-binding-table-vector-required]
                          [:alias-table :request-alias-table-vector-required]
                          [:resolution-table :request-resolution-table-vector-required]
                          [:fragment-manifest :request-fragment-manifest-vector-required]
                          [:macro-expansion-trace :request-macro-trace-vector-required]]]
      (require-shape vector? key reason))
    (when-not (= (:projection-binding request)
                 (reader-canonical-hash (sh07-core-projection-binding-input request)))
      (sh07-core-request-diagnostic! request {:reason :request-projection-binding-mismatch}))
    (when-not (= (get-in request [:lineage :alias-table-id])
                 (reader-canonical-hash {:domain :gravity/sh07-sh06-alias-table-v1
                                         :aliases (:alias-table request)}))
      (sh07-core-request-diagnostic! request {:reason :request-alias-table-lineage-mismatch}))
    (when-not (= 15 (:schema-version request))
      (sh07-core-request-diagnostic! request {:reason :request-schema-version :expected 15
                                             :observed (:schema-version request)}))
    (when-not (= :sh07-b15-keyword-map-lookup (:scope request))
      (sh07-core-request-diagnostic! request {:reason :request-scope
                                             :expected :sh07-b15-keyword-map-lookup
                                             :observed (:scope request)}))
    :passed))
