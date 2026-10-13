
(defn- p15-s23-sh07-b51-validate-content-object-proof!
  [content proof]
  (when-not (and (= #{:commit :tree} (set (keys content)))
                 (string? (:commit content))
                 (boolean (re-matches #"[0-9a-f]{40}" (:commit content)))
                 (string? (:tree content))
                 (boolean (re-matches #"[0-9a-f]{40}" (:tree content)))
                 (= #{:oid :raw-bytes} (set (keys proof))))
    (p15-s23-sh07-b51-governed-read-fail!
     :content-object-proof-shape {:content content}))
  (let [bytes (p15-s23-sh07-b51-unsigned-byte-array
               (:raw-bytes proof) :commit-object-proof)
        computed (p15-s23-sh07-b51-git-object-id "commit" bytes)
        tree (p15-s23-sh07-b51-parse-commit-tree!
              bytes :commit-object-proof)]
    (when-not (and (= (:commit content) (:oid proof) computed)
                   (= (:tree content) tree))
      (p15-s23-sh07-b51-governed-read-fail!
       :content-object-proof
       {:content content :proof-oid (:oid proof)
        :computed-oid computed :computed-tree tree}))
    true))

(defn- p15-s23-sh07-b51-validate-source-object-proof!
  [content descriptor proof]
  (when-not
   (and (= #{:schema :path :root-tree :tree-chain :blob-bytes}
           (set (keys proof)))
        (= :gravity/sh07-b51-git-tree-inclusion-proof-v1 (:schema proof))
        (= (:path descriptor) (:path proof))
        (= (:tree content) (:root-tree proof))
        (vector? (:tree-chain proof)))
    (p15-s23-sh07-b51-governed-read-fail!
     :source-object-proof-shape {:path (:path descriptor)}))
  (let [segments (p15-s23-sh07-b51-safe-proof-path-segments (:path proof))
        chain (:tree-chain proof)
        blob-bytes (p15-s23-sh07-b51-unsigned-byte-array
                    (:blob-bytes proof) [(:path proof) :blob])]
    (when-not (= (count segments) (count chain))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-object-proof-depth {:path (:path proof)}))
    (doseq [index (range (count chain))]
      (let [row (nth chain index)
            component (nth segments index)
            final? (= index (dec (count chain)))
            tree-bytes (when (map? row)
                         (p15-s23-sh07-b51-unsigned-byte-array
                          (:tree-bytes row) [(:path proof) index :tree]))
            entries (when tree-bytes
                      (p15-s23-sh07-b51-parse-tree-object!
                       tree-bytes [(:path proof) index]))
            matches (filterv #(= component (:name %)) entries)
            selected (first matches)
            expected-tree-oid
            (if (zero? index) (:tree content)
                (:selected-oid (nth chain (dec index))))]
        (when-not
         (and (= #{:tree-oid :tree-bytes :component
                   :selected-mode :selected-oid}
                 (set (keys row)))
              (= expected-tree-oid (:tree-oid row))
              (= (:tree-oid row)
                 (p15-s23-sh07-b51-git-object-id "tree" tree-bytes))
              (= component (:component row))
              (= 1 (count matches))
              (= (:mode selected) (:selected-mode row))
              (= (:oid selected) (:selected-oid row))
              (= (if final? "100644" "40000") (:selected-mode row)))
          (p15-s23-sh07-b51-governed-read-fail!
           :source-object-proof-chain
           {:path (:path proof) :index index}))))
    (let [leaf (peek chain)
          blob-oid (p15-s23-sh07-b51-git-object-id "blob" blob-bytes)]
      (when-not
       (and (= "100644" (:mode descriptor) (:selected-mode leaf))
            (= (:blob-oid descriptor) (:selected-oid leaf) blob-oid)
            (= (:byte-count descriptor) (alength ^bytes blob-bytes))
            (= (:raw-sha256 descriptor)
               (p15-s23-sh07-b51-raw-sha256 blob-bytes)))
        (p15-s23-sh07-b51-governed-read-fail!
         :source-object-proof-leaf {:path (:path proof)})))
    blob-bytes))

(defn- p15-s23-sh07-b51-git-arguments-valid?
  [arguments]
  (let [git-id? #(and (string? %)
                      (boolean (re-matches #"[0-9a-f]{40}" %)))
        source-paths (set (map :path p15-s23-sh07-b51-source-specs))]
    (and
     (vector? arguments)
     (or
      (and (= 3 (count arguments))
           (= ["rev-parse" "--verify"] (subvec arguments 0 2))
           (boolean
            (re-matches #"[0-9a-f]{40}\^\{(?:commit|tree)\}"
                        (nth arguments 2))))
      (and (= 5 (count arguments))
           (= ["ls-tree" "-z"] (subvec arguments 0 2))
           (git-id? (nth arguments 2))
           (= "--" (nth arguments 3))
           (contains? source-paths (nth arguments 4)))
      (and (= 3 (count arguments))
           (= "cat-file" (first arguments))
           (contains? #{"blob" "tree" "commit"} (second arguments))
           (git-id? (nth arguments 2)))))))

(defn- p15-s23-sh07-b51-remaining-millis
  [deadline-nanos]
  (max 0
       (long
        (Math/ceil
         (/ (double (- deadline-nanos (System/nanoTime))) 1000000.0)))))

(defn- p15-s23-sh07-b51-close-quietly!
  [value]
  (when value
    (try
      (.close ^java.io.Closeable value)
      (catch Throwable _ nil))))

(defn- p15-s23-sh07-b51-stop-git!
  [^Process process futures executor]
  (when (.isAlive process)
    (.destroy process)
    (when-not (.waitFor process 250 java.util.concurrent.TimeUnit/MILLISECONDS)
      (.destroyForcibly process)
      (.waitFor process 250 java.util.concurrent.TimeUnit/MILLISECONDS)))
  (doseq [stream [(.getOutputStream process)
                  (.getInputStream process)
                  (.getErrorStream process)]]
    (p15-s23-sh07-b51-close-quietly! stream))
  (doseq [future futures]
    (.cancel ^java.util.concurrent.Future future true))
  (.shutdownNow ^java.util.concurrent.ExecutorService executor))

(defn- p15-s23-sh07-b51-completed-git-task!
  [completion deadline-nanos]
  (let [remaining (p15-s23-sh07-b51-remaining-millis deadline-nanos)
        future (.poll ^java.util.concurrent.ExecutorCompletionService
                      completion remaining
                      java.util.concurrent.TimeUnit/MILLISECONDS)]
    (when-not future
      (p15-s23-sh07-b51-governed-read-fail!
       :git-timeout {:deadline-ms (:deadline-ms p15-s23-sh07-b51-git-limits)}))
    (try
      (.get ^java.util.concurrent.Future future)
      (catch java.util.concurrent.ExecutionException error
        (throw (.getCause error))))))

(defn- p15-s23-sh07-b51-git-bytes
  [arguments]
  (when-not (and (vector? arguments)
                 (every? string? arguments)
                 (p15-s23-sh07-b51-git-arguments-valid? arguments))
    (p15-s23-sh07-b51-governed-read-fail!
     :git-argv {:arguments arguments}))
  (let [argv (into ["/usr/bin/git" "-C" (System/getProperty "user.dir")]
                   arguments)
        process (-> (ProcessBuilder. ^java.util.List argv) .start)
        executor (java.util.concurrent.Executors/newFixedThreadPool 3)
        completion (java.util.concurrent.ExecutorCompletionService. executor)
        stdout-future
        (.submit completion
                 ^java.util.concurrent.Callable
                 (reify java.util.concurrent.Callable
                   (call [_]
                     {:kind :stdout
                      :bytes
                      (p15-s23-sh07-b51-read-stream-bytes
                       (.getInputStream process)
                       (:stdout-max-bytes p15-s23-sh07-b51-git-limits)
                       :stdout)})))
        stderr-future
        (.submit completion
                 ^java.util.concurrent.Callable
                 (reify java.util.concurrent.Callable
                   (call [_]
                     {:kind :stderr
                      :bytes
                      (p15-s23-sh07-b51-read-stream-bytes
                       (.getErrorStream process)
                       (:stderr-max-bytes p15-s23-sh07-b51-git-limits)
                       :stderr)})))
        exit-future
        (.submit completion
                 ^java.util.concurrent.Callable
                 (reify java.util.concurrent.Callable
                   (call [_] {:kind :exit :value (.waitFor process)})))
        futures [stdout-future stderr-future exit-future]
        deadline-nanos
        (+ (System/nanoTime)
           (* 1000000 (:deadline-ms p15-s23-sh07-b51-git-limits)))
        completed? (atom false)]
    (.close (.getOutputStream process))
    (try
      (let [results
            (loop [remaining 3 values {}]
              (if (zero? remaining)
                values
                (let [{:keys [kind] :as result}
                      (p15-s23-sh07-b51-completed-git-task!
                       completion deadline-nanos)]
                  (when (contains? values kind)
                    (p15-s23-sh07-b51-governed-read-fail!
                     :git-task-duplicate {:kind kind}))
                  (recur (dec remaining) (assoc values kind result)))))
            stdout (get-in results [:stdout :bytes])
            stderr (get-in results [:stderr :bytes])
            exit (get-in results [:exit :value])]
        (when-not (and (zero? exit) (zero? (alength ^bytes stderr)))
          (p15-s23-sh07-b51-governed-read-fail!
           :git-object-read
           {:arguments arguments
            :exit exit
            :stdout-byte-count (alength ^bytes stdout)
            :stderr-byte-count (alength ^bytes stderr)
            :stderr (p15-s23-sh07-b51-strict-utf8 stderr :git-stderr)}))
        (reset! completed? true)
        stdout)
      (finally
        (if @completed?
          (do
            (doseq [stream [(.getInputStream process) (.getErrorStream process)]]
              (p15-s23-sh07-b51-close-quietly! stream))
            (.shutdownNow executor))
          (p15-s23-sh07-b51-stop-git! process futures executor))))))

(defn- p15-s23-sh07-b51-single-lf-line
  [bytes coordinate]
  (let [text (p15-s23-sh07-b51-strict-utf8 bytes coordinate)]
    (when-not (boolean (re-matches #"[^\r\n]+\n" text))
      (p15-s23-sh07-b51-governed-read-fail!
       :git-line-grammar {:coordinate coordinate}))
    (subs text 0 (dec (count text)))))
