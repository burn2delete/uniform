
(defn validate-complete-classpath!
  "Validate all C source objects/materialized bytes and the three exact JARs."
  [options codec]
  (let [tree
        (git-operation-core!
         options :ls-tree-z-recursive
         ["ls-tree" "-r" "-z" (get-in options [:content :commit]) "--"
          "bootstrap/clojure/src"]
         (:response-max-bytes transport-limits))
        tree-rows (parse-tree-rows-core! (:stdout-bytes tree))
        _ (when-not
           (and (seq tree-rows)
                (= (count tree-rows) (count (set (map :path tree-rows))))
                (every? #(and (= "100644" (:mode %))
                              (= "blob" (:type %))
                              (str/starts-with? (:path %)
                                                classpath-source-prefix))
                        tree-rows))
            (reject! :classpath-tree))
        authenticated
        (mapv
         (fn [{:keys [path oid mode]}]
           (let [blob (git-operation-core!
                       options :cat-file-blob ["cat-file" "blob" oid]
                       (:response-max-bytes transport-limits))
                 object-bytes (:stdout-bytes blob)
                 materialized (materialized-source-core! options path)]
             (when-not (Arrays/equals ^bytes object-bytes ^bytes materialized)
               (reject! :classpath-materialized-byte-drift {:path path}))
             {:path path :mode mode :blob-oid oid
              :byte-count (alength ^bytes object-bytes)
              :raw-sha256 ((:R codec) object-bytes) :bytes object-bytes}))
         tree-rows)
        manifest-rows
        (mapv (fn [{:keys [path blob-oid byte-count raw-sha256]}]
                [(subs path (count classpath-source-prefix))
                 blob-oid byte-count raw-sha256])
              authenticated)
        manifest-root
        ((:H codec)
         [:gravity/sh07-b51-classpath-manifest-root-v1 manifest-rows])
        namespaces
        (->> authenticated
             (filter #(or (str/ends-with? (:path %) ".clj")
                          (str/ends-with? (:path %) ".cljc")))
             (keep #(some-> (source-namespace-core! (:bytes %) (:path %))
                            :namespace)) vec)
        _ (when-not (= (count namespaces) (count (distinct namespaces)))
            (reject! :classpath-project-namespace-duplicate))
        root (Paths/get (first (:classpath options)) (make-array String 0))
        project-entry
        {:ordinal 0 :kind :directory
         :logical-coordinate
         {:artifact 'project/bootstrap-clojure-src
          :commit (get-in options [:content :commit])
          :tree (get-in options [:content :tree])}
         :canonical-path (str (.toRealPath root (make-array LinkOption 0)))
         :file-key (file-key-core! root)
         :byte-count (reduce + 0 (map :byte-count authenticated))
         :sha256 manifest-root :provided-namespaces (vec (sort-by str namespaces))}
        external
        (mapv (fn [path spec]
                (jar-classpath-entry-core! path spec codec))
              (subvec (vec (:classpath options)) 1) external-classpath-specs)
        classpath (into [project-entry] (map :entry external))
        all-namespaces (vec (mapcat :provided-namespaces classpath))
        external-records
        (reduce
         (fn [result item]
           (reduce-kv
            (fn [result namespace record]
              (when (contains? result namespace)
                (reject! :classpath-namespace-shadowing
                         {:namespace namespace}))
              (assoc result namespace record))
            result (:external-source-records item)))
         {} external)]
    (when-not (and (= 4 (count classpath))
                   (= (vec (range 4)) (mapv :ordinal classpath))
                   (= (count all-namespaces) (count (distinct all-namespaces))))
      (reject! :classpath-namespace-shadowing))
    {:classpath-receipt classpath
     :classpath-manifest-rows manifest-rows
     :classpath-manifest-root manifest-root
     :source-authority
     {:content (:content options)
      :source-objects (into {} (map (juxt :path identity) authenticated))
      :external-source-records external-records}}))

(defn validate-checker-request!
  "Validate the closed V9 request and require its role to equal the CLI role."
  [cli-role request]
  (let [content (:content request)
        evidence (:evidence request)
        eligibility (:eligibility request)]
    (when-not
     (and (exact-keys? request request-keys)
          (= :gravity/sh07-b51-checker-child-request-v4 (:schema request))
          (= cli-role (:role request))
          (exact-keys? content content-keys)
          (git-id? (:commit content))
          (git-id? (:tree content))
          (exact-keys? evidence evidence-keys)
          (sha256-id? (:architecture-report-sha256 evidence))
          (= profile-bounds (:profile-bounds evidence))
          (= transport-limits (:limits request))
          (exact-keys? eligibility eligibility-keys)
          (= :gravity/sh07-b51-external-review-object-v1
             (:schema eligibility))
          (git-id? (:commit eligibility))
          (git-id? (:tree eligibility))
          (= [(:commit content)] (:parents eligibility))
          (= eligibility-path (:path eligibility))
          (= "100644" (:mode eligibility))
          (= "blob" (:type eligibility))
          (git-id? (:blob-oid eligibility))
          (and (integer? (:byte-count eligibility))
               (<= 0 (:byte-count eligibility)))
          (sha256-id? (:raw-sha256 eligibility))
          (sha256-id? (:receipt-id eligibility)))
      (reject! :request {:role cli-role}))
    request))

(defn- load-git-evidence! []
  (require (symbol git-evidence-namespace))
  {:validate-eligibility-object!
   (resolve-public! git-evidence-namespace "validate-eligibility-object!")})

(defn- dense-observations? [expected-count observations]
  (and (vector? observations)
       (= expected-count (count observations))
       (= (vec (range expected-count)) (mapv :ordinal observations))))

(defn validate-eligibility-five-operations!
  "Validate Q with five operations and independently reconstruct all 209 PRE
  operations through the authenticated shared Git-evidence implementation.
  The shared Q sequence retains its show, diff-tree, ls-tree, blob, ls-tree
  topology; this entrypoint does not duplicate those Git operations."
  [preflight codec request]
  (let [coordinate (:eligibility request)
        validate! (:validate-eligibility-object! (load-git-evidence!))
        result (validate! {:repository-root (:working-directory preflight)
                           :content (:content request)
                           :eligibility-coordinate coordinate})
        receipt (:receipt result)
        check (:eligibility-check result)
        observations (:observations result)
        pre-observations (:pre-discharge-observations result)
        transition (get-in receipt [:freeze :active-record-ref :transition])]
    (when (seq (:remaining-gaps result))
      (reject! :pre-discharge-reconstruction-unavailable))
    (when-not
     (and (exact-keys? result eligibility-result-keys)
          (exact-keys? receipt eligibility-receipt-keys)
          (not (contains? receipt :masked-root))
          (exact-keys? check eligibility-check-keys)
          (= coordinate (:coordinate result) (:coordinate check))
          (= 5 (:operation-count result))
          (dense-observations? 5 observations)
          (= observations (:observations check))
          (= 209 (:pre-discharge-git-operation-count result))
          (dense-observations? 209 pre-observations)
          (= pre-observations (:pre-discharge-observations check))
          (= eligibility-check-counts
             (select-keys check (keys eligibility-check-counts)))
          (every? true? (map check eligibility-check-matches))
          (= :immutable-q-coordinate-and-pre-discharge (:scope result))
          (= [] (:remaining-gaps result))
          (= :none (:authority result))
          (= (:eligibility-check-root result)
             ((:H codec)
              [:gravity/sh07-b51-eligibility-check-root-v1 check]))
          (sha256-id? (:architecture-report-sha256 transition))
          (= (get-in request [:evidence :architecture-report-sha256])
             (:architecture-report-sha256 transition)))
      (reject! :eligibility-object-invalid))
    result))

(defn- read-request-before-codec! [^InputStream input]
  (let [bytes (read-bounded-core!
               input (:request-max-bytes transport-limits)
               :request-overflow)
        text (strict-utf8-core! bytes :request-utf8)]
    (with-open [reader (java.io.PushbackReader. (java.io.StringReader. text))]
      (let [eof (Object.)
            value (edn/read {:eof eof :readers {} :default nil} reader)
            trailing (edn/read {:eof eof :readers {} :default nil} reader)]
        (when (or (identical? eof value) (not (identical? eof trailing)))
          (reject! :request-frame))
        {:bytes bytes :value value}))))

(defn- recomputable-event-product
  [event]
  (when (<= 3 (:ordinal event) 23) true))
