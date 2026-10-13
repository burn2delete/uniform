
(defn validate-complete-classpath!
  "Re-read every entry-0 blob from C, compare every materialized byte, bind
  the exact three external JAR versions, and derive the classpath manifest.
  This must run immediately after the admitted codec loads and before any
  other project namespace is loaded."
  [options codec]
  (let [tree-operation
        (git-operation-core!
         options :ls-tree-z-recursive
         ["ls-tree" "-r" "-z" (get-in options [:content :commit]) "--"
          "bootstrap/clojure/src"]
         (:response-max-bytes transport-limits))
        tree-rows (parse-recursive-tree-core! (:stdout-bytes tree-operation))
        authenticated
        (mapv
         (fn [{:keys [path oid mode]}]
           (let [object-bytes (git-blob-core! options oid)
                 materialized
                 (materialized-source-bytes-core!
                  options path (:response-max-bytes transport-limits))
                 raw ((:R codec) object-bytes)]
             (when-not (Arrays/equals ^bytes object-bytes
                                      ^bytes materialized)
               (reject! :classpath-materialized-byte-drift {:path path}))
             {:path path :mode mode :blob-oid oid
              :byte-count (alength ^bytes object-bytes)
              :raw-sha256 raw :bytes object-bytes}))
         tree-rows)
        manifest-rows
        (mapv (fn [{:keys [path blob-oid byte-count raw-sha256]}]
                [(subs path (count classpath-source-prefix))
                 blob-oid byte-count raw-sha256])
              authenticated)
        manifest-root
        ((:H codec)
         [:gravity/sh07-b51-classpath-manifest-root-v1 manifest-rows])
        project-namespaces
        (->> authenticated
             (filter #(or (str/ends-with? (:path %) ".clj")
                          (str/ends-with? (:path %) ".cljc")))
             (keep #(some-> (source-namespace-core! (:bytes %) (:path %))
                            :namespace))
             vec)
        _ (when-not (= (count project-namespaces)
                       (count (distinct project-namespaces)))
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
         :sha256 manifest-root
         :provided-namespaces (vec (sort-by str project-namespaces))}
        external
        (mapv (fn [path spec]
                (jar-classpath-entry-core! path spec codec))
              (subvec (vec (:classpath options)) 1)
              external-classpath-specs)
        classpath (into [project-entry] (map :entry external))
        namespace-groups (map :provided-namespaces classpath)
        all-namespaces (vec (mapcat identity namespace-groups))
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

(defn checker-request
  "Construct the only child IPC value.  It contains no semantic envelope."
  [options role]
  (when-not (contains? child-role-specs role)
    (reject! :role {:actual role}))
  (when-not (sha256-id? (:architecture-report-sha256 options))
    (reject! :eligibility-check-required
             {:field :architecture-report-sha256}))
  {:schema :gravity/sh07-b51-checker-child-request-v4
   :role role
   :content (:content options)
   :eligibility (:eligibility options)
   :evidence
   {:architecture-report-sha256 (:architecture-report-sha256 options)
    :profile-bounds profile-bounds}
   :limits transport-limits})

(defn child-argv
  "Return exact A/B argv.  The checker-child class name remains opaque text."
  [options role]
  (let [spec (or (get child-role-specs role)
                 (reject! :role {:actual role}))
        java-path
        (str (.normalize
              (.toAbsolutePath
               (Paths/get (System/getProperty "java.home")
                          (into-array String ["bin" "java"]))))) ]
    [java-path "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
     "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
     "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
     "-Duser.home=/nonexistent" "-cp" (:classpath-string options)
     "clojure.main" "-m" child-main-token "--role" (:cli spec)]))

(defn build-child
  "Construct, but do not start, one closed-environment checker child."
  [options codec role]
  (let [request (checker-request options role)
        request-bytes ((:bounded-wire-bytes codec) :request request)
        argv (child-argv options role)
        H (:H codec)
        request-id
        (H [:gravity/sh07-b51-checker-request-id-v1 request])
        argv-id (H [:gravity/sh07-b51-process-argv-id-v1 argv])
        builder (doto (ProcessBuilder. ^java.util.List argv)
                  (.directory (.toFile
                               (Paths/get (:working-directory options)
                                          (make-array String 0)))))]
    (doto (.environment builder)
      (.clear)
      (.putAll closed-environment))
    {:role role :sequence (:sequence (get child-role-specs role))
     :builder builder :argv argv :argv-id argv-id
     :request request :request-bytes request-bytes :request-id request-id}))

(defn- optional-value [^Optional value]
  (when (.isPresent value) (.get value)))

(defn- process-start-observation [built ^Process process codec]
  (let [handle (.toHandle process)
        info (.info handle)
        parent (optional-value (.parent handle))
        command (optional-value (.command info))
        arguments (some-> (optional-value (.arguments info)) vec)
        started (optional-value (.startInstant info))
        expected-argv (:argv built)]
    (when-not (and parent command arguments started
                   (= expected-argv (into [command] arguments)))
      (reject! :retained-process-start {:role (:role built)}))
    (let [coordinate
          {:role (:role built)
           :child-sequence (:sequence built)
           :normalized-argv-id (:argv-id built)
           :request-id (:request-id built)}]
      {:role (:role built)
       :process-handle-id
       ((:H codec)
        [:gravity/sh07-b51-process-handle-coordinate-id-v1 coordinate])
       :pid (.pid handle)
       :parent-pid (.pid ^ProcessHandle parent)
       :command command
       :arguments arguments
       :start-instant (str ^Instant started)})))

(defn start-child!
  "Start a previously constructed child and retain its process and pipes."
  [built codec]
  (let [process (.start ^ProcessBuilder (:builder built))]
    (assoc built
           :process process
           :process-start (process-start-observation built process codec))))

(defn- remaining-millis [deadline-nanos]
  (max 0 (long (Math/ceil
                (/ (double (- deadline-nanos (System/nanoTime)))
                   1000000.0)))))

(defn- close-quietly! [value]
  (when (instance? Closeable value)
    (try (.close ^Closeable value) (catch Throwable _ nil))))

(defn- terminate-process! [^Process process]
  (when (.isAlive process)
    (.destroy process)
    (when-not (.waitFor process 250 TimeUnit/MILLISECONDS)
      (.destroyForcibly process)
      (.waitFor process 1000 TimeUnit/MILLISECONDS)))
  (when (.isAlive process)
    (reject! :process-cleanup-timeout)))
