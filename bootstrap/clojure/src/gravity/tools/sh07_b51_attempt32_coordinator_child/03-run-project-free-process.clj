
(defn- run-project-free-process!
  [working-directory argv stdout-maximum operation]
  (let [builder (doto (ProcessBuilder. ^java.util.List argv)
                  (.directory (.toFile
                               (Paths/get working-directory
                                          (make-array String 0)))))
        _ (doto (.environment builder)
            (.clear)
            (.putAll closed-environment))
        process (.start builder)
        stdout (.getInputStream process)
        stderr (.getErrorStream process)
        stdin (.getOutputStream process)
        executor (Executors/newFixedThreadPool 3)
        completions (ExecutorCompletionService. executor)
        stdout-task
        (.submit completions ^Callable
                 (reify Callable
                   (call [_]
                     [:stdout (read-bounded-core! stdout stdout-maximum
                                                  :git-stdout-overflow)])))
        stderr-task
        (.submit completions ^Callable
                 (reify Callable
                   (call [_]
                     [:stderr (read-bounded-core!
                               stderr (:stderr-max-bytes transport-limits)
                               :git-stderr-overflow)])))
        _ (.close ^OutputStream stdin)
        process-task
        (.submit completions ^Callable
                 (reify Callable
                   (call [_] [:exit (.waitFor process)])))
        tasks [stdout-task stderr-task process-task]
        deadline (+ (System/nanoTime)
                    (* 1000000 (:deadline-ms transport-limits)))]
    (try
      (let [parts
            (loop [remaining 3 result {}]
              (if (zero? remaining)
                result
                (let [millis (remaining-millis deadline)
                      completed (when (pos? millis)
                                  (.poll completions millis
                                         TimeUnit/MILLISECONDS))]
                  (when-not completed
                    (throw (TimeoutException. "project-free process")))
                  (let [[kind value] (completed-future-value! completed)]
                    (recur (dec remaining) (assoc result kind value))))))
            stdout-bytes (:stdout parts)
            stderr-bytes (:stderr parts)]
        (when-not (and (zero? (:exit parts))
                       (zero? (alength ^bytes stderr-bytes)))
          (reject! :git-object-operation
                   {:operation operation :exit-code (:exit parts)
                    :stderr-byte-count (alength ^bytes stderr-bytes)}))
        {:operation operation :argv argv
         :stdin-byte-count 0 :stdin-raw-sha256 empty-raw-sha256
         :exit-code 0
         :stdout-bytes stdout-bytes
         :stdout-byte-count (alength ^bytes stdout-bytes)
         :stdout-raw-sha256 (sha256-bytes-core stdout-bytes)
         :stderr-byte-count 0 :stderr-raw-sha256 empty-raw-sha256})
      (catch Throwable error
        (doseq [task tasks] (.cancel ^Future task true))
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (terminate-process! process)
        (if (= "SH07-B51-CHECKER-PROCESS" (:id (ex-data error)))
          (throw error)
          (reject! :git-object-operation
                   {:operation operation
                    :cause (.getName (class error))})))
      (finally
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (.shutdownNow executor)
        (.awaitTermination executor 1000 TimeUnit/MILLISECONDS)))))

(defn- git-operation-core!
  [options operation arguments stdout-maximum]
  (run-project-free-process!
   (:working-directory options)
   (into ["/usr/bin/git" "-C" (:working-directory options)] arguments)
   stdout-maximum operation))

(defn- nul-records-core! [bytes reason]
  (let [text (strict-utf8-core! bytes reason)]
    (cond
      (empty? text) []
      (not (str/ends-with? text "\u0000")) (reject! reason)
      :else (vec (butlast (str/split text #"\u0000" -1))))))

(defn- parse-show-core! [bytes]
  (let [text (strict-utf8-core! bytes :git-show-grammar)
        match (re-matches #"([0-9a-f]{40})\n([0-9a-f]{40})\n([0-9a-f ]*)\n"
                          text)]
    (when-not match (reject! :git-show-grammar))
    {:commit (nth match 1) :tree (nth match 2)
     :parents (let [parents (nth match 3)]
                (if (str/blank? parents) [] (str/split parents #" ")))}))

(defn- parse-ls-tree-core! [bytes requested-paths]
  (let [records (nul-records-core! bytes :git-ls-tree-grammar)
        rows
        (mapv
         (fn [record]
           (let [match
                 (re-matches #"([0-7]{6}) ([a-z]+) ([0-9a-f]{40})\t(.+)"
                             record)]
             (when-not match (reject! :git-ls-tree-grammar))
             {:mode (nth match 1) :type (nth match 2)
              :oid (nth match 3) :path (nth match 4)}))
         records)
        by-path (into {} (map (juxt :path identity) rows))]
    (when-not (and (= (count rows) (count by-path))
                   (every? (set requested-paths) (keys by-path)))
      (reject! :git-ls-tree-grammar))
    (mapv #(or (get by-path %)
               {:path % :mode nil :type nil :oid nil})
          requested-paths)))

(defn- show-content-core! [options]
  (let [operation
        (git-operation-core!
         options :show-tree
         ["show" "-s" "--format=%H%n%T%n%P"
          (get-in options [:content :commit])]
         4096)
        parsed (parse-show-core! (:stdout-bytes operation))]
    (when-not (= (:content options) (select-keys parsed [:commit :tree]))
      (reject! :content-coordinate))
    (assoc (dissoc operation :stdout-bytes) :parsed-result parsed)))

(defn- git-path-object-core! [options path]
  (let [commit (get-in options [:content :commit])
        tree-operation
        (git-operation-core! options :ls-tree-z-requested
                             ["ls-tree" "-z" commit "--" path]
                             1048576)
        row (first (parse-ls-tree-core! (:stdout-bytes tree-operation)
                                        [path]))]
    (when-not (and (= "100644" (:mode row)) (= "blob" (:type row)))
      (reject! :c-source-object {:path path :row row}))
    (let [blob-operation
          (git-operation-core! options :cat-file-blob
                               ["cat-file" "blob" (:oid row)]
                               (:response-max-bytes transport-limits))
          bytes (:stdout-bytes blob-operation)]
      {:path path :mode (:mode row) :blob-oid (:oid row)
       :byte-count (alength ^bytes bytes)
       :raw-sha256 (sha256-bytes-core bytes) :bytes bytes})))

(defn- read-source-forms-core! [bytes path]
  (let [text (strict-utf8-core! bytes :trusted-tcb-utf8)]
    (with-open [reader
                (LineNumberingPushbackReader.
                 (InputStreamReader.
                  (ByteArrayInputStream.
                   (.getBytes text StandardCharsets/UTF_8))
                  StandardCharsets/UTF_8))]
      (binding [*read-eval* false]
        (let [eof (Object.)]
          (loop [forms []]
            (let [form (read {:eof eof :read-cond :allow :features #{:clj}}
                             reader)]
              (if (identical? eof form)
                forms
                (recur (conj forms form))))))))))

(defn- require-spec-namespace-core [spec]
  (cond
    (symbol? spec) spec
    (and (vector? spec) (symbol? (first spec))) (first spec)
    :else nil))

(defn- source-namespace-core! [bytes path]
  (let [forms (read-source-forms-core! bytes path)
        ns-forms (filter #(and (seq? %) (= 'ns (first %))) forms)
        first-form (first forms)]
    (when (and (seq ns-forms)
               (or (not= 1 (count ns-forms))
                   (not= first-form (first ns-forms))))
      (reject! :trusted-tcb-namespace {:path path}))
    (when (and first-form (not (seq ns-forms))
               (some #(and (seq? %) (= 'ns (first %))) (rest forms)))
      (reject! :trusted-tcb-namespace {:path path}))
    (when (seq ns-forms)
      (let [ns-form (first ns-forms)
            namespace (second ns-form)
            project-requires
            (->> (drop 2 ns-form)
                 (filter #(and (seq? %) (= :require (first %))))
                 (mapcat rest)
                 (keep require-spec-namespace-core)
                 (filter #(str/starts-with? (str %) "gravity."))
                 vec)]
        {:namespace namespace
         :requires-project-namespaces project-requires}))))

(defn- symlink-free-core? [^Path path]
  (loop [current (.normalize (.toAbsolutePath path))]
    (cond
      (nil? current) true
      (Files/isSymbolicLink current) false
      :else (recur (.getParent current)))))

(defn- materialized-source-bytes-core! [options path maximum]
  (when-not (str/starts-with? path classpath-source-prefix)
    (reject! :materialized-source-path {:path path}))
  (let [root (Paths/get (first (:classpath options)) (make-array String 0))
        relative (subs path (count classpath-source-prefix))
        resolved (.normalize (.resolve root relative))]
    (when-not (and (.startsWith resolved root)
                   (Files/isRegularFile
                    resolved
                    (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (symlink-free-core? resolved))
      (reject! :materialized-source-path {:path path}))
    (with-open [input (Files/newInputStream resolved (make-array java.nio.file.OpenOption 0))]
      (read-bounded-core! input maximum :materialized-source-overflow))))
