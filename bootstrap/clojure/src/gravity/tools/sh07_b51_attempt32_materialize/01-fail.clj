
(defn- fail! [reason & [context]]
  (throw (ex-info "SH-07 Attempt32 evidence publication rejected"
                  {:reason reason :context (or context {})})))

(defn- command-output [^Path directory arguments]
  (let [process (-> (ProcessBuilder. ^java.util.List (vec arguments))
                    (.directory (.toFile directory))
                    (.redirectErrorStream true)
                    (.start))
        output (with-open [stream (.getInputStream process)]
                 (String. (.readAllBytes stream) StandardCharsets/UTF_8))
        exit (.waitFor process)]
    (when-not (zero? exit)
      (fail! :git-command-failed {:arguments arguments :exit exit}))
    (str/trim output)))

(defn- repository-root []
  (let [cwd (.normalize (.toAbsolutePath (Paths/get "." (make-array String 0))))
        output (command-output cwd ["git" "rev-parse" "--show-toplevel"])
        root (.normalize (.toAbsolutePath (Paths/get output (make-array String 0))))]
    (when-not (and (Files/isDirectory root (make-array LinkOption 0))
                   (identity/symlink-free-path? root))
      (fail! :repository-root))
    root))

(defn validate-snapshot!
  "Require a clean checkout at the exact explicit snapshot commit and tree."
  ([root snapshot] (validate-snapshot! root snapshot nil))
  ([^Path root {:keys [commit tree] :as snapshot} ^Path excluded-stage]
   (when-not (identity/symlink-free-path? root)
     (fail! :snapshot-symlink-ancestor))
   (when-not (and (= #{:commit :tree} (set (keys snapshot)))
                  (string? commit) (re-matches #"[0-9a-f]{40}" commit)
                  (string? tree) (re-matches #"[0-9a-f]{40}" tree))
     (fail! :snapshot-shape))
   (let [excluded-relative
         (when excluded-stage
           (let [stage (.normalize (.toAbsolutePath excluded-stage))]
             (when-not (and (.startsWith stage root)
                            (identity/symlink-free-path? stage))
               (fail! :snapshot-stage-path))
             (str (.relativize root stage))))
         head (command-output root ["git" "rev-parse" "HEAD"])
         head-tree (command-output root ["git" "rev-parse" "HEAD^{tree}"])
         snapshot-tree
         (command-output root ["git" "rev-parse" (str commit "^{tree}")])
         status-arguments
         (cond-> ["git" "status" "--porcelain=v1" "--untracked-files=all"]
           excluded-relative
           (into ["--" "." (str ":(exclude)" excluded-relative)]))
         status (command-output root status-arguments)]
     (when-not (= commit head)
       (fail! :snapshot-commit-mismatch {:actual head}))
     (when-not (= tree head-tree snapshot-tree)
       (fail! :snapshot-tree-mismatch
              {:head-tree head-tree :snapshot-tree snapshot-tree}))
     (when-not (str/blank? status)
       (fail! :dirty-worktree {:status status}))
     snapshot)))

(defn- option-map [arguments]
  (when-not (and (even? (count arguments)) (= 8 (count arguments)))
    (fail! :arguments))
  (let [options (apply hash-map arguments)
        expected #{"--snapshot-commit" "--snapshot-tree" "--completed-at"
                   "--evidence-relative-root"}]
    (when-not (= expected (set (keys options))) (fail! :arguments))
    {:snapshot {:commit (get options "--snapshot-commit")
                :tree (get options "--snapshot-tree")}
     :completed-at (get options "--completed-at")
     :evidence-relative-root (get options "--evidence-relative-root")}))

(defn- ordered-value [value]
  (let [sort-key #(pr-str (identity/canonical-value %))
        comparator #(compare (sort-key %1) (sort-key %2))]
    (cond
      (map? value) (into (sorted-map-by comparator)
                         (map (fn [[key item]]
                                [(ordered-value key) (ordered-value item)]))
                         value)
      (set? value) (into (sorted-set-by comparator) (map ordered-value) value)
      (vector? value) (mapv ordered-value value)
      (seq? value) (apply list (map ordered-value value))
      :else value)))

(defn deterministic-edn-bytes [value]
  (.getBytes (str (binding [*print-length* nil *print-level* nil
                            *print-meta* true]
                    (pr-str (ordered-value value)))
                  "\n")
             StandardCharsets/UTF_8))

(defn- byte-id [bytes]
  (str "sha256:" (digest/sha256-bytes-hex bytes)))

(defn- artifact-descriptor [root name bytes]
  {:relative-path (str root "/" name)
   :byte-count (alength bytes) :sha256 (byte-id bytes)})

(defn- product-identities [products]
  {:module-source-identities
   (mapv :source-sha256 (:module-source-descriptors products))
   :module-source-byte-counts
   (mapv :source-byte-count (:module-source-descriptors products))
   :m0-ids (mapv :module-source-id (:module-source-descriptors products))
   :m1-id (get-in products [:link-manifest :manifest-id])
   :m2-ids (mapv :plan-id (:module-plans products))
   :emitter-source-id (:emitter-source-id products)
   :emitter-semantic-id (:emitter-semantic-id products)
   :bundle-core-id (:execution-bundle-core-id products)})

(defn- identity-receipt [materialized descriptors]
  (let [certificates (:certificates materialized)
        a (first certificates) b (second certificates)
        carrier (:carrier materialized)
        base
        {:artifact :gravity/sh07-b51-binding-replay-identity-receipt-v1
         :schema-version 2 :status :materialized-candidate
         :attempt32-architecture-report-sha256
         (:architecture-report-sha256 (:inputs a))
         :binding-replay-architecture-report
         (:binding-replay-architecture-report materialized)
         :snapshot (:snapshot materialized)
         :completed-at (:completed-at materialized)
         :evidence-relative-root (:evidence-relative-root materialized)
         :product-identities (product-identities (:products materialized))
         :checker-identities
         {:producer-census (:checker a)
          :independent-verifier-census (:checker b)}
         :checker-identity-manifest-ids
         {:producer-census
          (get-in materialized
                  [:identities :producer-census :identity-manifest-id])
          :independent-verifier-census
          (get-in materialized
                  [:identities :independent-verifier-census
                   :identity-manifest-id])}
         :census-identities
         {:claim-id (:claim-id a)
          :normalized-census-id (get-in a [:claim :normalized-census-id])
          :physical-census (get-in a [:claim :physical-census])
          :category-schedule (get-in a [:claim :category-schedule])
          :category-commitments (get-in a [:claim :category-commitments])
          :certificate-ids [(:certificate-id a) (:certificate-id b)]}
         :candidate-semantic-coordinates (:pin-map materialized)
         :candidate-carrier-observation
         {:artifact-descriptor (:carrier descriptors)
          :location-sensitive-physical-fields true
          :carrier-descriptor-in-identity-receipt-id true
          :semantic-root-hash (:semantic-root-hash carrier)}
         :materializer-dependency-closure
         (:materializer-dependency-closure materialized)
         :identity-artifacts descriptors
         :authority false
         :nonclaims [:implementation-acceptance :integration-eligibility
                     :sh07-completion :self-hosting :release
                     :seed-retirement]}]
    (assoc base :identity-receipt-id (identity/canonical-id base))))

(defn- output-values [materialized]
  (let [identities (:identities materialized)
        outputs (:outputs materialized)]
    {:producer-identity (:producer-census identities)
     :independent-identity (:independent-verifier-census identities)
     :producer-output (:producer-census outputs)
     :independent-output (:independent-verifier-census outputs)
     :certificate-pair (:certificates materialized)
     :carrier (:carrier materialized)}))

(defn- read-evidence [bytes]
  (with-open [reader
              (PushbackReader.
               (StringReader. (String. bytes StandardCharsets/UTF_8)))]
    (binding [*read-eval* false]
      (let [eof (Object.)
            options {:eof eof :readers {}
                     :default (fn [tag _]
                                (fail! :tagged-evidence-value {:tag tag}))}
            value (edn/read options reader)
            trailing (edn/read options reader)]
        (when (or (identical? eof value) (not (identical? eof trailing)))
          (fail! :evidence-edn-cardinality))
        value))))

(defn- existing-file-names [^Path directory]
  (with-open [stream (Files/list directory)]
    (set (map #(str (.getFileName ^Path %)) (iterator-seq (.iterator stream))))))

(defn- existing-output-identical? [^Path target files]
  (and (Files/isDirectory target (make-array LinkOption 0))
       (identity/symlink-free-path? target)
       (= (set (keys files)) (existing-file-names target))
       (every? (fn [[name bytes]]
                 (let [path (.resolve target name)]
                   (and (Files/isRegularFile
                         path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                        (identity/symlink-free-path? path)
                        (java.util.Arrays/equals bytes (Files/readAllBytes path)))))
               files)))

(def ^:private receipt-keys
  #{:artifact :schema-version :status
    :attempt32-architecture-report-sha256
    :binding-replay-architecture-report :snapshot :completed-at
    :evidence-relative-root :product-identities :checker-identities
    :checker-identity-manifest-ids :census-identities
    :candidate-semantic-coordinates :candidate-carrier-observation
    :materializer-dependency-closure :identity-artifacts :authority
    :nonclaims :identity-receipt-id})
