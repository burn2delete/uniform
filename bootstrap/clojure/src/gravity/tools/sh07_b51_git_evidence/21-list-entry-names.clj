
(defn- list-entry-names!
  [^Path directory id]
  (with-open [^DirectoryStream stream (Files/newDirectoryStream directory)]
    (->> (iterator-seq (.iterator stream))
         (mapv #(.toString (.getFileName ^Path %)))
         sort
         vec)))

(defn- read-bounded-file!
  [^Path path]
  (when-not (and (Files/isRegularFile path no-link-options)
                 (not (Files/isSymbolicLink path)))
    (fail! "SH07-B51-GIT-INPUT-BLOB" :staged-payload-file
           {:path (str path)}))
  (with-open [input (Files/newInputStream path no-open-options)]
    ((:read-bounded-bytes transport-api) :response input)))

(defn validate-staged-payloads!
  "Strictly validate the six canonical M payloads and their descriptors."
  [{:keys [staging-root payload-descriptors]}]
  (when-not (and (vector? payload-descriptors)
                 (= 6 (count payload-descriptors)))
    (fail! "SH07-B51-GIT-INPUT-BLOB" :payload-descriptor-count))
  (let [root (canonical-directory! staging-root :staging-root
                                   "SH07-B51-GIT-INPUT-BLOB")
        names (list-entry-names! root "SH07-B51-GIT-INPUT-BLOB")]
    (when-not (= (vec (sort payload-names)) names)
      (fail! "SH07-B51-GIT-INPUT-BLOB" :staging-topology
             {:expected (vec (sort payload-names)) :actual names}))
    (mapv
     (fn [[ordinal name kind] descriptor]
       (exact-keys! descriptor payload-descriptor-keys
                    "SH07-B51-GIT-INPUT-BLOB" :payload-descriptor-keys)
       (let [path (.resolve root name)
             bytes (read-bounded-file! path)
             value ((:strict-read-wire transport-api) :response bytes)
             expected
             {:ordinal ordinal
              :name name
              :kind kind
              :relative-path (str evidence-relative-root "/" name)
              :wire-codec :gravity/sh07-b51-edn-wire-v1
              :byte-count (alength ^bytes bytes)
              :raw-sha256 (codec/R bytes)
              :value-id
              (codec/H
               [:gravity/sh07-b51-staged-payload-value-v1
                {:ordinal ordinal :name name :kind kind :value value}])}]
         (when-not (= expected descriptor)
           (fail! "SH07-B51-GIT-INPUT-BLOB" :payload-descriptor
                  {:ordinal ordinal :expected expected :actual descriptor}))
         {:ordinal ordinal :name name :kind kind :path path
          :bytes bytes :value value :descriptor descriptor
          :file-key (file-key path "SH07-B51-GIT-INPUT-BLOB")}))
     payload-specs payload-descriptors)))

(defn validate-m-response!
  "Strictly load the closed M response and join all six staged payloads.

  Coordinator/contract semantic validation is intentionally not inferred here;
  callers receive `:authority :none` until that separately owned seam exists."
  [{:keys [content staging-root response-bytes]}]
  (let [response ((:strict-read-wire transport-api)
                  :response response-bytes)]
    (exact-keys! response
                 #{:schema :status :content :payload-descriptors
                   :staging-receipt :coordinator-receipt
                   :source-read-receipt :counters :transport}
                 "SH07-B51-CHECKER-PROCESS" :m-response-keys)
    (exact-keys! (:staging-receipt response)
                 #{:staging-path :evidence-relative-root :directory-mode
                   :symlink-free :payload-names :payload-descriptors}
                 "SH07-B51-CHECKER-PROCESS" :m-staging-receipt-keys)
    (exact-keys! (:transport response)
                 #{:response-preimage-id :self-id-omits}
                 "SH07-B51-CHECKER-PROCESS" :m-transport-keys)
    (let [response-id
          (codec/H
           [:gravity/sh07-b51-response-preimage-id-v1
            (update response :transport dissoc :response-preimage-id)])
          staged (validate-staged-payloads!
                  {:staging-root staging-root
                   :payload-descriptors (:payload-descriptors response)})]
      (when-not
       (and (= :gravity/sh07-b51-m-response-v1 (:schema response))
            (= :accepted (:status response))
            (= content (:content response))
            (= (str (canonical-directory!
                     staging-root :staging-root
                     "SH07-B51-CHECKER-PROCESS"))
               (:staging-path (:staging-receipt response)))
            (= evidence-relative-root
               (:evidence-relative-root (:staging-receipt response)))
            (= "0700" (:directory-mode (:staging-receipt response)))
            (true? (:symlink-free (:staging-receipt response)))
            (= payload-names (:payload-names (:staging-receipt response)))
            (= (:payload-descriptors response)
               (:payload-descriptors (:staging-receipt response)))
            (= [[:transport :response-preimage-id]]
               (:self-id-omits (:transport response)))
            (= response-id (get-in response
                                   [:transport :response-preimage-id])))
        (fail! "SH07-B51-CHECKER-PROCESS" :m-response-join))
      {:response response :staged-payloads staged
       :response-byte-count (alength ^bytes response-bytes)
       :response-raw-sha256 (codec/R response-bytes)
       :response-preimage-id response-id
       :authority :none})))

(defn coordinator-argv
  "Construct the exact 39-token M argv.  The four-entry classpath is accepted
  only as transport substrate; this function does not claim its missing C/T
  manifest validation."
  [{:keys [classpath content staging-root eligibility-coordinate]}]
  (exact-keys! content #{:commit :tree}
               "SH07-B51-CHECKER-PROCESS" :content)
  (exact-keys! eligibility-coordinate eligibility-coordinate-keys
               "SH07-B51-CHECKER-PROCESS" :eligibility-coordinate)
  (doseq [[field value] [[:content-commit (:commit content)]
                         [:content-tree (:tree content)]
                         [:eligibility-commit (:commit eligibility-coordinate)]
                         [:eligibility-tree (:tree eligibility-coordinate)]
                         [:eligibility-blob (:blob-oid eligibility-coordinate)]]]
    (require-git-id! value field "SH07-B51-CHECKER-PROCESS"))
  (when-not (and (= :gravity/sh07-b51-external-review-object-v1
                    (:schema eligibility-coordinate))
                 (= [(:commit content)] (:parents eligibility-coordinate))
                 (= eligibility-receipt-path
                    (:path eligibility-coordinate))
                 (= "100644" (:mode eligibility-coordinate))
                 (= "blob" (:type eligibility-coordinate))
                 (integer? (:byte-count eligibility-coordinate))
                 (not (neg? (:byte-count eligibility-coordinate)))
                 (sha256-id? (:raw-sha256 eligibility-coordinate))
                 (sha256-id? (:receipt-id eligibility-coordinate)))
    (fail! "SH07-B51-CHECKER-PROCESS" :eligibility-coordinate))
  (let [entries (if (vector? classpath) classpath
                    (str/split (or classpath "")
                               (re-pattern
                                (java.util.regex.Pattern/quote
                                 java.io.File/pathSeparator)) -1))
        _ (when-not
           (and (= 4 (count entries)) (= 4 (count (distinct entries)))
                (every? #(and (string? %) (not (str/blank? %))
                              (.isAbsolute
                               (Paths/get % (make-array String 0))))
                        entries))
            (fail! "SH07-B51-TRUSTED-TCB" :classpath))
        classpath-string (str/join java.io.File/pathSeparator entries)
        java-path
        (str (.normalize
              (.toAbsolutePath
               (Paths/get (System/getProperty "java.home")
                          (into-array String ["bin" "java"])))))
        stage (str (canonical-directory! staging-root :staging-root
                                         "SH07-B51-CHECKER-PROCESS"))]
    [java-path "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
     "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
     "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
     "-Duser.home=/nonexistent" "-cp" classpath-string "clojure.main" "-m"
     "gravity.tools.sh07-b51-attempt32-coordinator-child"
     "--snapshot-commit" (:commit content)
     "--snapshot-tree" (:tree content)
     "--completed-at" "1970-01-01T00:00:00Z"
     "--evidence-relative-root" evidence-relative-root
     "--staging-root" stage
     "--eligibility-review-commit" (:commit eligibility-coordinate)
     "--eligibility-review-tree" (:tree eligibility-coordinate)
     "--eligibility-receipt-path" (:path eligibility-coordinate)
     "--eligibility-receipt-blob" (:blob-oid eligibility-coordinate)
     "--eligibility-receipt-byte-count" (str (:byte-count eligibility-coordinate))
     "--eligibility-receipt-raw-sha256" (:raw-sha256 eligibility-coordinate)
     "--eligibility-receipt-id" (:receipt-id eligibility-coordinate)]))

(defn launch-coordinator-transport!
  "Launch one fresh M process with bounded concurrent stdout/stderr drains.

  This is an explicitly non-authoritative integration seam until the C-derived
  classpath materializer and full launcher-receipt validator are joined."
  [{:keys [repository-root content staging-root] :as options}]
  (let [repository (canonical-directory! repository-root :repository-root
                                         "SH07-B51-CHECKER-PROCESS")
        argv (coordinator-argv options)
        process-result
        (run-bounded-process!
         {:argv argv :directory repository :allowed-exit-codes #{0}
          :stdout-role :response
          :deadline-ms (:deadline-ms transport-limits)
          :id "SH07-B51-CHECKER-PROCESS"})
        stderr (:stderr-bytes process-result)]
    (when-not (zero? (alength ^bytes stderr))
      (fail! "SH07-B51-CHECKER-PROCESS" :m-stderr
             {:stderr-byte-count (alength ^bytes stderr)
              :stderr-raw-sha256 (codec/R stderr)}))
    (merge
     (validate-m-response!
      {:content content :staging-root staging-root
       :response-bytes (:stdout-bytes process-result)})
     {:argv argv
      :argv-id (codec/H [:gravity/sh07-b51-process-argv-id-v1 argv])
      :transport (:transport process-result)
      :authority :none})))

(def ^:private identity-receipt-keys
  #{:artifact :schema-version :content :semantic-ordinals
    :coordinator-events :coordinator-event-root :child-process-receipts
    :launcher-receipt :coordinator-receipt :payload-joins
    :evidence-eligibility :pre-discharge-id :runtime-contract-discharge
    :receipt-id-normalization :self-id-omits :receipt-id :nonclaims})

(declare validate-runtime-identity-receipt!)

(defn- validate-identity-receipt-envelope!
  ([content receipt]
   (validate-identity-receipt-envelope! content receipt false))
  ([content receipt strict?]
   (exact-keys! receipt identity-receipt-keys
                "SH07-B51-RECEIPT-JOIN" :identity-receipt-keys)
   (when-not
    (and (= :gravity/sh07-b51-checker-process-identity-receipt-v1
            (:artifact receipt))
         (= 1 (:schema-version receipt))
         (= content (:content receipt))
         (= (vec (range 11)) (:semantic-ordinals receipt))
         (= [[:receipt-id]] (:self-id-omits receipt))
         (sha256-id? (:coordinator-event-root receipt))
         (sha256-id? (:pre-discharge-id receipt))
         (sha256-id? (:receipt-id receipt))
         (= nonclaims (:nonclaims receipt)))
     (fail! "SH07-B51-RECEIPT-JOIN" :identity-receipt-envelope))
   (when strict?
     (validate-runtime-identity-receipt! content receipt))
   receipt))
