
(defn reload-validate-output!
  "Reload all seven output artifacts and recheck their physical/semantic joins."
  [^Path directory materialized values files external-descriptors]
  (when-not (and (= 7 (count values) (count files))
                 (= 7 (count external-descriptors))
                 (identity/symlink-free-path? directory)
                 (= (set (keys files)) (existing-file-names directory)))
    (fail! :evidence-reload-shape))
  (doseq [[key value] values]
    (let [name (get tier0/output-file-names key)
          path (.resolve directory name)
          expected-bytes (get files name)]
      (when-not (and name expected-bytes
                     (Files/isRegularFile
                      path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                     (identity/symlink-free-path? path)
                     (java.util.Arrays/equals expected-bytes
                                              (Files/readAllBytes path))
                     (= value (read-evidence (Files/readAllBytes path))))
        (fail! :evidence-reload-mismatch {:artifact key}))))
  (let [receipt (:receipt values)
        carrier (:carrier values)
        carrier-bytes (get files (:carrier tier0/output-file-names))
        carrier-descriptor
        (artifact-descriptor
         (:evidence-relative-root receipt)
         (:carrier tier0/output-file-names) carrier-bytes)]
    (when-not (and (= receipt-keys (set (keys receipt)))
                   (= :gravity/sh07-b51-binding-replay-identity-receipt-v1
                      (:artifact receipt))
                   (= 2 (:schema-version receipt))
                   (= :materialized-candidate (:status receipt))
                   (false? (:authority receipt))
                   (= (set (keys (:identity-artifacts receipt)))
                      #{:producer-identity :independent-identity
                        :producer-output :independent-output
                        :certificate-pair :carrier})
                   (= (:identity-receipt-id receipt)
                      (identity/canonical-id
                       (dissoc receipt :identity-receipt-id)))
                   (= carrier-descriptor
                      (get-in receipt
                              [:candidate-carrier-observation
                               :artifact-descriptor]))
                   (= (:semantic-root-hash carrier)
                      (get-in receipt
                              [:candidate-carrier-observation
                               :semantic-root-hash])))
        (fail! :evidence-reload-receipt))
    (when-not
     (= receipt
        (identity-receipt materialized (dissoc external-descriptors :receipt)))
      (fail! :evidence-receipt-materialized-join)))
  (doseq [[key descriptor] external-descriptors]
    (let [name (get tier0/output-file-names key)
          bytes (get files name)]
      (when-not (and (= #{:relative-path :byte-count :sha256}
                         (set (keys descriptor)))
                     (= descriptor
                        (artifact-descriptor
                         (get-in values [:receipt :evidence-relative-root])
                         name bytes))
                     (or (= key :receipt)
                         (= descriptor
                            (get-in values [:receipt :identity-artifacts key]))))
        (fail! :evidence-artifact-descriptor {:artifact key}))))
  (tier0/validate-reloaded-evidence! materialized (dissoc values :receipt))
  true)

(defn- publish-files!
  [^Path root relative snapshot materialized values files descriptors]
  (let [target (.normalize (.resolve root relative)) parent (.getParent target)]
    (when-not (and (.startsWith target root) parent
                   (Files/isDirectory parent (make-array LinkOption 0))
                   (identity/symlink-free-path? parent))
      (fail! :evidence-output-parent {:relative relative}))
    (if (Files/exists target (make-array LinkOption 0))
      (if (existing-output-identical? target files)
        (do (reload-validate-output! target materialized values files
                                     descriptors)
            (validate-snapshot! root snapshot)
            :already-identical)
        (fail! :evidence-output-exists {:relative relative}))
      (let [staged (Files/createTempDirectory
                    parent ".sh07-attempt32-tier0-"
                    (make-array FileAttribute 0))
            cleanup-stage!
            (fn []
              (doseq [name (keys files)]
                (Files/deleteIfExists (.resolve staged name)))
              (Files/deleteIfExists staged))]
        (try
          (doseq [[name bytes] files]
            (Files/write (.resolve staged name) bytes
                         (into-array OpenOption
                                      [StandardOpenOption/CREATE_NEW
                                       StandardOpenOption/WRITE])))
          (reload-validate-output! staged materialized values files descriptors)
          (validate-snapshot! root snapshot staged)
          (try
            (Files/move staged target
                        (into-array StandardCopyOption
                                    [StandardCopyOption/ATOMIC_MOVE]))
            (catch java.io.IOException error
              (cleanup-stage!)
              (if (Files/exists
                   target
                   (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                (fail! :evidence-output-race {:relative relative})
                (throw error))))
          (reload-validate-output! target materialized values files descriptors)
          :published
          (catch Throwable error
            (cleanup-stage!)
            (throw error)))))))

(defn publication-bundle [materialized]
  (let [values (output-values materialized)
        initial-bytes
        (into {} (map (fn [[key value]]
                        [(get tier0/output-file-names key)
                         (deterministic-edn-bytes value)])) values)
        identity-keys [:producer-identity :independent-identity
                       :producer-output :independent-output :certificate-pair
                       :carrier]
        descriptors
        (into {}
              (map (fn [key]
                     [key (artifact-descriptor
                           (:evidence-relative-root materialized)
                           (get tier0/output-file-names key)
                           (get initial-bytes
                                (get tier0/output-file-names key)))]))
              identity-keys)
        receipt (identity-receipt materialized descriptors)
        receipt-bytes (deterministic-edn-bytes receipt)
        files (assoc initial-bytes (:receipt tier0/output-file-names)
                     receipt-bytes)
        descriptors
        (assoc descriptors :receipt
               (artifact-descriptor (:evidence-relative-root materialized)
                                    (:receipt tier0/output-file-names)
                                    receipt-bytes))
        values (assoc values :receipt receipt)]
    {:values values :files files :descriptors descriptors
     :receipt receipt}))

(defn materialize-and-publish! [options]
  (let [root (repository-root)
        snapshot (validate-snapshot! root (:snapshot options))
        materialized
        (tier0/materialize-current
         (assoc options :repository-root root :snapshot snapshot))
        {:keys [values files descriptors receipt]}
        (publication-bundle materialized)
        publication
        (publish-files! root (:evidence-relative-root options) snapshot
                        materialized values files descriptors)]
    {:status publication :identity-receipt-id (:identity-receipt-id receipt)
     :receipt-artifact (:receipt descriptors)
     :snapshot snapshot :evidence-relative-root
     (:evidence-relative-root options)}))

(defn -main [& arguments]
  (try
    (prn (materialize-and-publish! (option-map arguments)))
    (catch Throwable error
      (binding [*out* *err*]
        (prn {:status :rejected
              :reason (or (:reason (ex-data error)) :materializer-failure)}))
      (System/exit 1))))
