
(defn- runtime-fingerprint []
  {:clojure-version (clojure-version)
   :java-version (System/getProperty "java.version")
   :java-vendor (System/getProperty "java.vendor")
   :java-vm-name (System/getProperty "java.vm.name")
   :java-vm-version (System/getProperty "java.vm.version")
   :os-name (System/getProperty "os.name")
   :os-arch (System/getProperty "os.arch")})

(defn checker-identity-manifest
  "Derive one role identity from bytes already authenticated against C/T.

  The caller must pass the internal source-authority returned by the child
  classpath preflight.  A checkout path, classpath resource lookup, or missing
  C object is deliberately not accepted as a fallback."
  [source-authority role]
  (let [{:keys [algorithm-role namespace entrypoint source-relative-path domains]}
        (or (get checker-specs role)
            (throw (ex-info "unknown SH-07 checker role" {:role role})))
        _ (when-not
           (and (map? source-authority)
                (= #{:commit :tree} (set (keys (:content source-authority))))
                (map? (:source-objects source-authority))
                (map? (:external-source-records source-authority)))
            (throw (ex-info "missing authenticated C-derived source input"
                            {:role role})))
        bytes (:bytes (source-object! source-authority source-relative-path))
        product (source-product source-authority source-relative-path)
        forms (:forms product)
        source-sha (sha256-bytes bytes)
        semantic-id (canonical-id {:domain (:semantic domains) :schema-version 1
                                   :role role :namespace namespace
                                   :entrypoint entrypoint
                                   :reader-normalization
                                   :ranked-gensym-and-regex-v1
                                   :forms (semantic-forms forms)})
        artifact-id (canonical-id {:domain (:artifact domains) :schema-version 1
                                   :role role :algorithm-role algorithm-role
                                   :source-relative-path source-relative-path
                                   :source-byte-count (alength bytes)
                                   :source-sha256 source-sha})
        dependencies (dependency-manifest source-authority product)
        runtime (runtime-fingerprint)
        closure-id (canonical-id {:domain (:dependency domains) :schema-version 1
                                  :role role :dependencies dependencies
                                  :runtime runtime})
        executable-id
        (canonical-id {:domain (:executable domains) :schema-version 1 :role role
                       :entrypoint entrypoint :artifact-id artifact-id
                       :semantic-id semantic-id :dependency-closure-id closure-id
                       :runtime runtime})
        checker {:artifact-id artifact-id :source-sha256 source-sha
                 :semantic-id semantic-id :executable-id executable-id
                 :dependency-closure-id closure-id
                 :algorithm-role algorithm-role}
        base {:artifact (:manifest domains) :schema-version 1 :role role
              :source-relative-path source-relative-path
              :source-byte-count (alength bytes) :checker checker
              :dependencies dependencies :runtime runtime}]
    (when-not (= 5 (count (set ((juxt :artifact-id :source-sha256 :semantic-id
                                           :executable-id :dependency-closure-id)
                                checker))))
      (throw (ex-info "checker identity coordinates are not distinct"
                      {:role role})))
    (assoc base :identity-manifest-id (canonical-id base))))
