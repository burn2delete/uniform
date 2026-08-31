

(defn sh07-b13-module-assembly-manifest
  [lineage fragment-manifest]
  (let [content
        {:ordered-fragment-ids (mapv :fragment-id fragment-manifest)
         :root-form-ids (vec (mapcat :root-form-ids fragment-manifest))
         :source-revision-id (:source-revision-id lineage)
         :sh06-semantic-projection-id
         (:sh06-semantic-projection-id lineage)
         :alias-table-id (:alias-table-id lineage)}
        content-id (:source-revision-id lineage)]
    (assoc content
           :content-id content-id
           :module-id
           (:sh06-semantic-projection-id lineage))))

(defn sh07-core-authenticated-request
  [resolution-artifact]
  (let [source-path
        (sh07-core-source-path-from-resolution resolution-artifact)
        upstream-verification
        (sh06-resolution-artifact-verification resolution-artifact)]
    (when-not
     (and (= :gravity/sh06-resolution-artifact
             (:kind resolution-artifact))
          (= :passed (:status upstream-verification))
          (= :complete
             (get-in resolution-artifact
                     [:capability-based-proof :status])))
      (throw
       (ex-info "SH-07 requires a fresh verified SH-06 artifact"
                {:id "C6-VERIFY" :stage :core-lowering
                 :source-path source-path
                 :missing-fields [:fresh-authenticated-sh06-resolution]})))
    (let [sh05 (:sh05-macro-artifact resolution-artifact)
          semantic-macro-trace
          (sh07-core-semantic-macro-trace
           (:macro-expansion-trace sh05))
          lineage
          (sh07-core-lineage-with-semantic-trace
           resolution-artifact semantic-macro-trace)
          module-contract
          (get-in resolution-artifact
                  [:gravity-resolution-boundary
                   :resolved-analysis :module-contract])
          module
          {:namespace (:namespace module-contract)
           :profile (:profile module-contract)
           :target (:target module-contract)
           :safety (:safety module-contract)
           :effects (vec (:effects module-contract))
           :capabilities (vec (:capabilities module-contract))
           :exports (vec (:exports module-contract))
           :source-revision-id (:source-revision-id lineage)}
          decimal-evidence (sh07-core-decimal-evidence resolution-artifact)
          executable-syntax
          (->> (:expanded-syntax-stream sh05)
               (remove #(and (seq? (:form %))
                             (= 'ns (first (:form %)))))
               (map-indexed
                (fn [ordinal syntax]
                  (assoc syntax :sh07/root-syntax-id
                         (sh07-core-root-syntax-id
                          (:source-revision-id lineage)
                          ordinal (:form syntax)))))
               vec)
          root-id-by-upstream-id
          (into {}
                (map (fn [syntax]
                       [(:syntax/id syntax)
                        (:sh07/root-syntax-id syntax)]))
                executable-syntax)
          raw-traces
          (filterv #(= 'defn (:macro %))
                   (:macro-expansion-trace sh05))
          traces
          (mapv
           (fn [ordinal step]
             (sh07-core-macro-trace
              lineage
              (assoc step
                     :input-syntax-id
                     (reader-canonical-hash
                      {:domain :gravity/sh07-macro-input-syntax-v1
                       :source-revision-id
                       (:source-revision-id lineage)
                       :ordinal ordinal
                       :macro (:macro step)})
                     :output-syntax-id
                     (get root-id-by-upstream-id
                          (:output-syntax-id step)))))
           (range)
           raw-traces)
          trace-by-output
          (into {} (map (juxt :output-def-syntax-id identity)) traces)
          trees
          (mapv
           #(sh07-core-build-form-tree
             source-path (:source-revision-id lineage) decimal-evidence %
             (get trace-by-output (:sh07/root-syntax-id %)))
           executable-syntax)
          _ (sh07-core-decimal-evidence-complete!
             source-path decimal-evidence)
          authenticated-sh06-request
          (get-in resolution-artifact
                  [:gravity-resolution-boundary
                   :authenticated-resolution-request])
          resolved-analysis
          (get-in resolution-artifact
                  [:gravity-resolution-boundary :resolved-analysis])
          projection
          (sh07-core-resolution-projection!
           source-path (:source-revision-id lineage)
           executable-syntax trees traces
           authenticated-sh06-request resolved-analysis)
          forms (:forms projection)
          roots (mapv (comp :form-id :root) trees)
          bindings
          (sh07-b13-binding-order
           module trees (:binding-table projection))
          aliases (:alias-table projection)
          resolutions (:resolution-table projection)
          fragment-manifest
          (sh07-b13-fragment-manifest
           lineage module trees bindings resolutions aliases)
          fragment-coverage
          (sh07-b13-fragment-coverage
           module forms bindings resolutions fragment-manifest)
          module-assembly-manifest
          (sh07-b13-module-assembly-manifest
           lineage fragment-manifest)
          expectation
          {:source-revision-id (:source-revision-id lineage)
           :sh05-artifact-id (:sh05-artifact-id lineage)
           :macro-expansion-trace-id (:macro-expansion-trace-id lineage)
           :expanded-defn-count (count traces)
           :expected-input-syntax-ids (mapv :input-syntax-id traces)
           :expected-output-def-syntax-ids
           (mapv :output-def-syntax-id traces)
           :expected-introduced-fn-syntax-ids
           (mapv :introduced-fn-syntax-id traces)}
          request
          {:artifact :gravity/sh07-authenticated-sh06-core-request
           :schema-version 15
           :lineage lineage
           :module module
           :forms forms
           :top-level-form-ids roots
           :binding-table bindings
           :alias-table aliases
           :resolution-table resolutions
           :fragment-manifest fragment-manifest
           :fragment-coverage fragment-coverage
           :module-assembly-manifest module-assembly-manifest
           :macro-expansion-trace semantic-macro-trace
           :macro-origin-traces traces
           :macro-origin-expectation expectation
           :projection-binding nil
           :provenance {:actual-source-path source-path}
           :scope :sh07-b15-keyword-map-lookup}
          binding
          (reader-canonical-hash
           (sh07-core-projection-binding-input request))]
      (assoc request :projection-binding binding))))

(defn sh07-core-digest-reference-ordinal
  [value]
  (when (and (map? value)
             (= #{:artifact :schema-version :ordinal :authority}
                (set (keys value)))
             (= :gravity/sh07-internal-digest-reference
                (:artifact value))
             (= 1 (:schema-version value))
             (= :sh07-digest-resolver (:authority value))
             (integer? (:ordinal value))
             (not (neg? (:ordinal value))))
    (:ordinal value)))

(defn sh07-core-resolve-reference!
  [source-path value resolved-digests]
  (let [ordinal (sh07-core-digest-reference-ordinal value)
        resolved (when (some? ordinal)
                   (get resolved-digests ordinal))]
    (when-not (and (some? ordinal)
                   (p15-s23-sh02-sha256-id? resolved))
      (throw
       (ex-info "SH-07 digest reference is unresolved"
                {:id "C6-VERIFY" :stage :core-lowering
                 :source-path source-path
                 :digest-reference value
                 :resolved-count (count resolved-digests)})))
    resolved))

(defn sh07-core-resolve-reference-vector!
  [source-path values resolved-digests]
  (mapv #(sh07-core-resolve-reference!
          source-path % resolved-digests)
        values))

(defn sh07-core-semantic-projection-id
  [value]
  (or (get-in value [:lineage :sh06-semantic-projection-id])
      (get value :sh06-semantic-projection-id)
      (get-in value [:attributes :sh06-semantic-projection-id])))

(defn sh07-core-semantic-binding
  [semantic-projection-id binding]
  (cond-> binding
    (:upstream-binding-id binding)
    (assoc :upstream-binding-id (:binding-id binding))

    (and semantic-projection-id
         (:definition-artifact-id binding))
    (assoc :definition-artifact-id semantic-projection-id)))

(defn sh07-core-semantic-resolution
  [resolution]
  (cond-> resolution
    (:upstream-binding-id resolution)
    (assoc :upstream-binding-id (:binding-id resolution))))

(defn sh07-core-semantic-var-reference
  [semantic-projection-id reference]
  (let [semantic-projection-id
        (or semantic-projection-id
            (sh07-core-semantic-projection-id reference))]
    (cond-> reference
      (:upstream-binding-id reference)
      (assoc :upstream-binding-id (:binding-id reference))

      (and semantic-projection-id
           (:definition-artifact-id reference))
      (assoc :definition-artifact-id semantic-projection-id)

      (and semantic-projection-id
           (:authenticated-sh06-artifact-id reference))
      (assoc :authenticated-sh06-artifact-id semantic-projection-id))))