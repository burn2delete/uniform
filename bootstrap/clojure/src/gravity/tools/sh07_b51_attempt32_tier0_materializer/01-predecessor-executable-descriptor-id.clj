
(def predecessor-executable-descriptor-id
  "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234")

(def materializer-source-paths
  ["bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"])

(def binding-replay-architecture-report-path
  "docs/artifacts/phase-15/reports/sh07-b51-binding-replay-index-architecture-v1.md")

(def output-file-names
  {:producer-identity "checker-a-identity.edn"
   :independent-identity "checker-b-identity.edn"
   :producer-output "checker-a-output.edn"
   :independent-output "checker-b-output.edn"
   :certificate-pair "certificate-pair.edn"
   :carrier "linked-carrier-v4-candidate.edn"
   :receipt "identity-receipt.edn"})

(defn- require! [condition reason & [context]]
  (when-not condition
    (throw (ex-info "SH-07 Attempt32 Tier-0 materialization rejected"
                    {:reason reason :context (or context {})}))))

(defn- git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- safe-relative-path? [value]
  (and (string? value) (not (str/blank? value))
       (not (str/starts-with? value "/"))
       (not (str/includes? value "\\"))
       (not-any? #{"" "." ".."} (str/split value #"/"))))

(defn- source-file [^Path root relative]
  (let [path (.normalize (.resolve root relative))]
    (require! (and (safe-relative-path? relative) (.startsWith path root)
                   (Files/isRegularFile
                    path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (identity/symlink-free-path? path))
              :source-path {:path relative})
    path))

(defn- source-record [root relative]
  (let [bytes (Files/readAllBytes (source-file root relative))]
    {:relative-path relative :byte-count (alength bytes)
     :source-sha256 (str "sha256:" (digest/sha256-bytes-hex bytes))}))

(defn- source-forms [root products]
  (let [descriptors (:module-source-descriptors products)
        specs bootstrap/p15-s23-sh07-b51-module-specs]
    (require! (= 7 (count descriptors) (count specs)) :module-count)
    (mapv
     (fn [ordinal descriptor spec]
       (let [relative (:logical-source-path spec)
             path (source-file root relative)
             bytes (Files/readAllBytes path)
             text (String. bytes StandardCharsets/UTF_8)
             forms (bootstrap/read-forms relative text)
             source-sha (str "sha256:" (digest/sha256-bytes-hex bytes))]
         (require! (= ordinal (:module-ordinal descriptor)
                      (:module-ordinal spec)) :module-order {:ordinal ordinal})
         (require! (= relative (:logical-source-path descriptor))
                   :module-source-path {:ordinal ordinal})
         (require! (= [(alength bytes) source-sha]
                      [(:source-byte-count descriptor)
                       (:source-sha256 descriptor)])
                   :module-source-identity {:ordinal ordinal})
         (vec forms)))
     (range 7) descriptors specs)))

(defn- common-envelope [snapshot products forms]
  (let [descriptors (:module-source-descriptors products)]
    {:architecture-report-sha256
     bootstrap/p15-s23-sh07-b51-attempt32-architecture-report-sha256
     :snapshot-commit (:commit snapshot)
     :snapshot-tree (:tree snapshot)
     :predecessor-executable-descriptor-id predecessor-executable-descriptor-id
     :module-source-identities (mapv :source-sha256 descriptors)
     :module-source-byte-counts (mapv :source-byte-count descriptors)
     :emitter-source-id (:emitter-source-id products)
     :emitter-semantic-id (:emitter-semantic-id products)
     :m0s descriptors :m1 (:link-manifest products)
     :m2s (:module-plans products)
     :bundle-core (:execution-bundle-core products)
     :source-forms forms}))

(defn- evidence-path [root key]
  (str root "/" (get output-file-names key)))

(defn- materializer-dependencies [root]
  (identity/source-closure-manifest root materializer-source-paths))

(defn materializer-dependency-commitments
  [materializer-dependency-closure]
  {:local-source-record-ids
   (mapv #(identity/canonical-id
           {:domain :gravity/sh07-b51-materializer-local-source-v1
            :schema-version 1 :record %})
         (:local-source-records materializer-dependency-closure))
   :external-runtime-record-ids
   (mapv #(identity/canonical-id
           {:domain :gravity/sh07-b51-materializer-external-runtime-v1
            :schema-version 1 :record %})
         (:external-runtime-records materializer-dependency-closure))})

(defn materializer-tool-closure-id-from-commitments
  [role checker-manifest commitments]
  (let [domain
        (case role
          :producer-census
          :gravity/sh07-b51-producer-census-materializer-tool-closure-id-v2
          :independent-verifier-census
          :gravity/sh07-b51-independent-census-materializer-tool-closure-id-v2)]
    (identity/canonical-id
     {:domain domain :schema-version 2 :role role
      :checker-identity-manifest-id
      (:identity-manifest-id checker-manifest)
      :materializer-dependency-commitments commitments})))

(defn materializer-tool-closure-id
  [role checker-manifest materializer-dependency-closure]
  (materializer-tool-closure-id-from-commitments
   role checker-manifest
   (materializer-dependency-commitments materializer-dependency-closure)))

(defn- provenance
  [role snapshot completed-at evidence-root checker-manifest
   materializer-dependency-closure]
  (let [role-name (case role :producer-census :producer
                              :independent-verifier-census :independent)
        checker (:checker checker-manifest)
        output-key (case role :producer-census :producer-output
                              :independent-verifier-census :independent-output)
        tool-closure-id
        (materializer-tool-closure-id
         role checker-manifest materializer-dependency-closure)
        reproduction-command-id
        (identity/canonical-id
         {:domain :gravity/sh07-b51-attempt32-tier0-reproduction-command-id-v1
          :schema-version 1 :snapshot snapshot :completed-at completed-at
          :evidence-relative-root evidence-root
          :main 'gravity.tools.sh07-b51-attempt32-materialize})]
    {:invocation-id
     (identity/canonical-id
      {:domain :gravity/sh07-b51-attempt32-checker-invocation-id-v1
       :schema-version 1 :role role-name :snapshot snapshot
       :checker-executable-id (:executable-id checker)
       :completed-at completed-at :evidence-relative-root evidence-root})
     :tool-closure-id tool-closure-id
     :reproduction-command-id reproduction-command-id
     :evidence-relative-path (evidence-path evidence-root output-key)
     :completed-at completed-at}))

(defn- checker-envelope
  [common role manifest snapshot completed-at evidence-root
   materializer-dependency-closure]
  (assoc common :checker (:checker manifest)
         :provenance (provenance role snapshot completed-at evidence-root
                                 manifest materializer-dependency-closure)))

(defn validate-certificate-pair!
  "Validate independent accepted outputs and their exact common census joins."
  [envelope-a output-a envelope-b output-b]
  (require! (= :accepted (:status output-a)) :checker-a-rejected output-a)
  (require! (= :accepted (:status output-b)) :checker-b-rejected output-b)
  (require! (checker-a/checker-output-valid? output-a)
            :checker-a-output-invalid)
  (require! (checker-b/valid-output? envelope-b output-b)
            :checker-b-output-invalid)
  (let [a (:certificate output-a) b (:certificate output-b)
        ca (:checker a) cb (:checker b)
        distinct-coordinates
        [:source-sha256 :semantic-id :executable-id :dependency-closure-id]]
    (require! (= (:checker envelope-a) ca) :checker-a-envelope-join)
    (require! (= (:checker envelope-b) cb) :checker-b-envelope-join)
    (require! (= [:producer-census :source-first-exhaustive]
                 [(:role a) (:algorithm-role ca)]) :checker-a-role)
    (require! (= [:independent-verifier-census
                  :m2-first-independent-exhaustive]
                 [(:role b) (:algorithm-role cb)]) :checker-b-role)
    (require! (every? #(not= (get ca %) (get cb %)) distinct-coordinates)
              :checker-identities-not-independent)
    (require! (= (select-keys a [:claim-id :claim :profile-bounds])
                 (select-keys b [:claim-id :claim :profile-bounds]))
              :checker-common-claim-mismatch)
    (require! (= (get-in a [:result :normalized-census-id])
                 (get-in b [:result :normalized-census-id])
                 (get-in a [:claim :normalized-census-id]))
              :checker-normalized-census-mismatch)
    (require! (= (get-in a [:claim :physical-census])
                 (get-in b [:claim :physical-census]))
              :checker-physical-census-mismatch)
    (require! (= (get-in a [:claim :category-schedule])
                 (get-in b [:claim :category-schedule]))
              :checker-category-schedule-mismatch)
    (require! (= (get-in a [:claim :category-commitments])
                 (get-in b [:claim :category-commitments]))
              :checker-category-commitments-mismatch)
    (let [pair [a b]]
      (require!
       (bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
        pair) :runtime-certificate-pair-invalid)
      pair)))
