;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- validate-key-request!
  [request]
  (when-not (and (map? request) (= key-request-fields (set (keys request))))
    (cache-fail! "C16-KEY" "C2 cache key request fields are incomplete"
                 {:required-fields (vec (sort key-request-fields))
                  :observed-fields (when (map? request)
                                     (vec (sort (keys request))))}))
  (let [{:keys [source-unit source-snapshot reader-policy project-binding
                compiler-binding pass-binding dependency-binding
                build-effect-binding capability-binding facet-binding
                profile-binding target-binding boundary-binding
                path-provenance]} request]
    (doseq [[field value]
            [[:source-unit source-unit]
             [:source-snapshot source-snapshot]
             [:reader-policy reader-policy]
             [:project-binding project-binding]
             [:compiler-binding compiler-binding]
             [:pass-binding pass-binding]
             [:dependency-binding dependency-binding]
             [:build-effect-binding build-effect-binding]
             [:capability-binding capability-binding]
             [:facet-binding facet-binding]
             [:profile-binding profile-binding]
             [:target-binding target-binding]
             [:boundary-binding boundary-binding]
             [:path-provenance path-provenance]]]
      (when-not (map? value)
        (cache-fail! "C16-KEY" "C2 cache key binding must be a map"
                     {:field field})))
    (ensure-sha256-id! :source-id (:source-id source-unit))
    (ensure-sha256-id! :source-unit-bytes-hash (:bytes-hash source-unit))
    (ensure-sha256-id! :snapshot-bytes-hash (:bytes-hash source-snapshot))
    (ensure-sha256-id! :extension-policy (:extension-policy reader-policy))
    (ensure-sha256-id! :project-root-id (:project-root-id project-binding))
    (ensure-sha256-id! :compiler-id (:compiler-id compiler-binding))
    (ensure-sha256-id! :sh03-binding-id (:sh03-binding-id compiler-binding))
    (ensure-sha256-id! :pass-contract-id (:pass-contract-id pass-binding))
    (doseq [[field binding]
            [[:dependency-binding dependency-binding]
             [:build-effect-binding build-effect-binding]
             [:capability-binding capability-binding]
             [:facet-binding facet-binding]
             [:boundary-binding boundary-binding]]]
      (ensure-sha256-id! field (:identity binding)))
    (when-not (and (= (:bytes-hash source-unit)
                      (:bytes-hash source-snapshot))
                   (= (:bytes-hash source-unit)
                      (get-in source-unit [:identity-inputs :bytes-hash]))
                   (= (:reader-options source-unit)
                      (:reader-options reader-policy))
                   (= (:extension-policy reader-policy)
                      (get-in source-unit
                              [:identity-inputs :extension-policy]))
                   (= (:project-root-id project-binding)
                      (get-in source-unit
                              [:identity-inputs :project-root-id]))
                   (= (:project-relative-path project-binding)
                      (get-in source-unit
                              [:identity-inputs :project-relative-path]))
                   (integer? (:byte-count source-snapshot))
                   (<= 0 (:byte-count source-snapshot) maximum-source-bytes))
      (cache-fail! "C16-KEY" "source snapshot and C2 source-unit bindings disagree"
                   {}))
    (when-not (= {:applicability :not-applicable-at-c2} profile-binding)
      (cache-fail! "C16-KEY" "C2 profile binding must be explicitly inapplicable"
                   {:observed profile-binding}))
    (when-not (= {:applicability :not-applicable-at-c2} target-binding)
      (cache-fail! "C16-KEY" "C2 target binding must be explicitly inapplicable"
                   {:observed target-binding}))
    (when-not (and (= :SH-03 (:slice boundary-binding))
                   (= :gravity-source (:owner boundary-binding))
                   (= :gravity/sh03-to-c2-reader-products-v2
                      (:adapter-contract boundary-binding))
                   (sha256-id? (:plan-binding-id boundary-binding))
                   (sha256-id?
                    (:semantic-value-table-contract-id boundary-binding))
                   (sha256-id?
                    (:authenticated-envelope-contract-id boundary-binding))
                   (false? (:target-source-reread? boundary-binding))
                   (map? (:uncredited-source-models boundary-binding))
                   (true? (:clojure-adapter-residual? boundary-binding))
                   (false? (:self-hosted? boundary-binding))
                   (= (:identity boundary-binding)
                      (canonical-content-id
                       {:domain :gravity/c2-pass-cache-boundary-binding-v1
                        :binding (dissoc boundary-binding :identity)})))
      (cache-fail! "C16-KEY"
                   "C2 cache boundary binding is incomplete or unsafe"
                   {:observed boundary-binding}))
    (let [canonical-path (:canonical-path path-provenance)
          normalized (when (string? canonical-path)
                       (normalized-absolute-path! canonical-path))]
      (when-not (and normalized (= canonical-path (str normalized)))
        (cache-fail! "C16-KEY" "path provenance must be normalized and absolute"
                     {:observed canonical-path}))))
  request)

(defn cache-key
  "Build the canonical semantic and path-scoped storage ids for one C2 input.

  The absolute supplied path is deliberately excluded from `:semantic-key-id`
  and included in `:storage-key-id`.  This preserves the C2 semantic identity
  rule while conservatively preventing an artifact carrying one checkout's path
  provenance from being returned for another checkout."
  [request]
  (let [validated (validate-key-request! request)
        path-provenance (:path-provenance validated)
        semantic-preimage
        {:artifact :gravity/compiler-pass-cache-key-preimage
         :schema-version cache-schema-version
         :canonicalizer-version canonicalizer-version
         :stage cache-stage
         :source-unit (:source-unit validated)
         :source-snapshot (:source-snapshot validated)
         :reader-policy (:reader-policy validated)
         :project-binding (:project-binding validated)
         :compiler-binding (:compiler-binding validated)
         :pass-binding (:pass-binding validated)
         :dependency-binding (:dependency-binding validated)
         :build-effect-binding (:build-effect-binding validated)
         :capability-binding (:capability-binding validated)
         :facet-binding (:facet-binding validated)
         :profile-binding (:profile-binding validated)
         :target-binding (:target-binding validated)
         :boundary-binding (:boundary-binding validated)}
        semantic-key-id (canonical-content-id semantic-preimage)
        storage-key-id
        (canonical-content-id
         {:domain :gravity/c2-pass-cache-storage-key-v1
          :semantic-key-id semantic-key-id
          :path-provenance path-provenance})]
    {:artifact :gravity/compiler-pass-cache-key
     :schema-version cache-schema-version
     :canonicalizer-version canonicalizer-version
     :stage cache-stage
     :semantic-preimage semantic-preimage
     :semantic-key-id semantic-key-id
     :path-provenance path-provenance
     :storage-key-id storage-key-id}))
