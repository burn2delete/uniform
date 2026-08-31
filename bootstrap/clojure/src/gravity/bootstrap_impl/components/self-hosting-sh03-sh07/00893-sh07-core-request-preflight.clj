

(defn sh07-core-request-preflight!
  [request]
  (let [forms-value (:forms request)
        bindings-value (:binding-table request)
        aliases-value (:alias-table request)
        resolutions-value (:resolution-table request)
        fragments-value (:fragment-manifest request)
        trace-value (:macro-expansion-trace request)
        origin-traces-value (:macro-origin-traces request)
        value-shape
        (fn [value]
          (cond
            (nil? value) :nil
            (map? value) :map
            (vector? value) :vector
            (set? value) :set
            (sequential? value) :sequential
            (coll? value) :collection
            :else :scalar))
        request-depth
        (when (map? request)
          (sh07-core-nested-depth request))]
    (cond
      (not (map? request))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-map-required
        :observed-shape (value-shape request)})

      (not (map? (:lineage request)))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-lineage-map-required
        :observed-shape (value-shape (:lineage request))})

      (not (map? (:module request)))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-module-map-required
        :observed-shape (value-shape (:module request))})

      (not (map? (:provenance request)))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-provenance-map-required
        :observed-shape (value-shape (:provenance request))})

      (not (vector? forms-value))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-forms-vector-required
        :observed-shape (value-shape forms-value)})

      (not (vector? bindings-value))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-binding-table-vector-required
        :observed-shape (value-shape bindings-value)})

      (not (vector? aliases-value))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-alias-table-vector-required
        :observed-shape (value-shape aliases-value)})

      (not (vector? resolutions-value))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-resolution-table-vector-required
        :observed-shape (value-shape resolutions-value)})

      (not (vector? fragments-value))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-fragment-manifest-vector-required
        :observed-shape (value-shape fragments-value)})

      (not (vector? trace-value))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-macro-trace-vector-required
        :observed-shape (value-shape trace-value)})

      (> request-depth 256)
      (sh07-core-request-diagnostic!
       request
       {:bound :maximum-carrier-depth
        :maximum 256
        :observed request-depth})

      (not=
       (:projection-binding request)
       (reader-canonical-hash
        (sh07-core-projection-binding-input request)))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-projection-binding-mismatch})

      (not=
       (get-in request [:lineage :alias-table-id])
       (reader-canonical-hash
        {:domain :gravity/sh07-sh06-alias-table-v1
         :aliases aliases-value}))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-alias-table-lineage-mismatch})

      :else
      (let [forms (count forms-value)
            bindings (count bindings-value)
            aliases (count aliases-value)
            resolutions (count resolutions-value)
            fragments (count fragments-value)
            trace-count (count trace-value)
            origin-trace-count
            (if (vector? origin-traces-value)
              (count origin-traces-value)
              0)]
        (cond
      (not= 15 (:schema-version request))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-schema-version
        :expected 15
        :observed (:schema-version request)})

      (not= :sh07-b15-keyword-map-lookup (:scope request))
      (sh07-core-request-diagnostic!
       request
       {:reason :request-scope
        :expected :sh07-b15-keyword-map-lookup
        :observed (:scope request)})

      (> forms 65536)
      (sh07-core-request-diagnostic!
       request
       {:bound :maximum-module-forms
        :maximum 65536
        :observed forms
        :projected-core-node-count forms
         :projected-digest-request-count (+ forms 2)})

      (> fragments 1024)
      (sh07-core-request-diagnostic!
       request
       {:bound :maximum-top-level-fragments
        :maximum 1024
        :observed fragments})

      (some #(> (count (:form-ids %)) 1024) fragments-value)
      (let [fragment
            (first
             (filter #(> (count (:form-ids %)) 1024)
                     fragments-value))]
        (sh07-core-request-diagnostic!
         request
         {:reason :fragment-root-form-bound
          :bound :maximum-fragment-forms
          :maximum 1024
          :ordinal (:ordinal fragment)
          :observed (count (:form-ids fragment))}))

      (> bindings 2440)
      (sh07-core-request-diagnostic!
       request
       {:bound :maximum-module-bindings
        :maximum 2440
        :observed bindings})

      (> aliases 256)
      (sh07-core-request-diagnostic!
       request
       {:bound :maximum-alias-records
        :maximum 256
        :observed aliases})

      (> resolutions 65536)
      (sh07-core-request-diagnostic!
       request
       {:bound :maximum-resolutions
        :maximum 65536
        :observed resolutions})

      (> trace-count 1024)
      (sh07-core-request-diagnostic!
       request
       {:bound :maximum-forms
        :maximum 1024
        :observed trace-count})

      (> origin-trace-count 2048)
      (sh07-core-request-diagnostic!
       request
       {:bound :maximum-carrier-origin-traces
        :maximum 2048
        :observed origin-trace-count})

          :else :passed)))))