
(defn- run-bounded-process!
  [{:keys [argv directory allowed-exit-codes stdout-role deadline-ms id]}]
  (when-not (and (vector? argv) (seq argv)
                 (every? #(and (string? %) (not (str/blank? %))) argv)
                 (str/starts-with? (first argv) "/"))
    (fail! id :argv {:argv argv}))
  (let [root (canonical-directory! directory :working-directory id)
        builder (doto (ProcessBuilder. ^java.util.List argv)
                  (.directory (.toFile root)))
        _ (doto (.environment builder)
            (.clear)
            (.putAll closed-environment))
        process (.start builder)
        stdout (.getInputStream process)
        stderr (.getErrorStream process)
        stdin (.getOutputStream process)
        executor (Executors/newFixedThreadPool 3)
        completions (ExecutorCompletionService. executor)
        stdout-future
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     [:stdout ((:read-bounded-bytes transport-api)
                               stdout-role stdout)])))
        stderr-future
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     [:stderr ((:read-bounded-bytes transport-api)
                               :stderr stderr)])))
        _ (.close stdin)
        wait-future
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     [:exit (.waitFor process)])))
        futures [stdout-future stderr-future wait-future]
        deadline (+ (System/nanoTime) (* 1000000 (long deadline-ms)))]
    (try
      (let [parts
            (loop [remaining 3
                   result {}]
              (if (zero? remaining)
                result
                (let [millis (remaining-millis deadline)
                      completed (when (pos? millis)
                                  (.poll completions millis
                                         TimeUnit/MILLISECONDS))]
                  (when (nil? completed)
                    (throw (TimeoutException. "bounded process deadline")))
                  (let [[kind value] (completed-value! completed)]
                    (recur (dec remaining) (assoc result kind value))))))
            exit (:exit parts)
            stdout-bytes (:stdout parts)
            stderr-bytes (:stderr parts)]
        (when-not (contains? allowed-exit-codes exit)
          (fail! id :process-exit
                 {:argv argv :exit-code exit
                  :stderr-byte-count (alength ^bytes stderr-bytes)
                  :stderr-raw-sha256 (codec/R stderr-bytes)}))
        {:argv argv
         :exit-code exit
         :stdout-bytes stdout-bytes
         :stderr-bytes stderr-bytes
         :transport
         {:stdin-byte-count 0
          :stdin-raw-sha256 empty-raw-sha256
          :stdin-closed-before-wait true
          :stdout-cap (get transport-limits
                           (if (= :stderr stdout-role)
                             :stderr-max-bytes
                             :response-max-bytes))
          :stderr-cap (:stderr-max-bytes transport-limits)
          :deadline-ms deadline-ms
          :stdout-drain-started-before-wait true
          :stderr-drain-started-before-wait true
          :stdout-drain-complete true
          :stderr-drain-complete true
          :timed-out false
          :destroy-called false
          :destroy-forcibly-called false
          :tasks-joined true}})
      (catch Throwable error
        (doseq [future futures] (.cancel ^Future future true))
        (let [termination (terminate-process! process)]
          (fail! id
                 (if (instance? TimeoutException error)
                   :process-deadline
                   :process-transport)
                 {:argv argv
                  :cause (.getName (class error))
                  :destroy-called (:destroy-called termination)
                  :destroy-forcibly-called
                  (:destroy-forcibly-called termination)})))
      (finally
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (.shutdownNow executor)
        (.awaitTermination executor 1000 TimeUnit/MILLISECONDS)))))

(defn- git-argv
  [^Path repository arguments]
  (into [git-executable "-C" (str repository)] arguments))

(defn- git-operation!
  [repository operation arguments allowed-exit-codes parser]
  (let [result
        (run-bounded-process!
         {:argv (git-argv repository arguments)
          :directory repository
          :allowed-exit-codes allowed-exit-codes
          :stdout-role :response
          :deadline-ms (:deadline-ms transport-limits)
          :id "SH07-B51-GIT-SNAPSHOT"})
        stdout (:stdout-bytes result)
        stderr (:stderr-bytes result)]
    (when-not (zero? (alength ^bytes stderr))
      (fail! "SH07-B51-GIT-SNAPSHOT" :git-stderr
             {:operation operation
              :stderr-byte-count (alength ^bytes stderr)
              :stderr-raw-sha256 (codec/R stderr)}))
    {:operation operation
     :argv (:argv result)
     :stdin-byte-count 0
     :stdin-raw-sha256 empty-raw-sha256
     :exit-code (:exit-code result)
     :stdout-byte-count (alength ^bytes stdout)
     :stdout-raw-sha256 (codec/R stdout)
     :stderr-byte-count 0
     :stderr-raw-sha256 empty-raw-sha256
     :parsed-result (parser stdout (:exit-code result))}))

(defn- parse-single-line!
  [bytes reason]
  (let [text (decode-utf8! bytes "SH07-B51-GIT-SNAPSHOT" reason)]
    (when-not (and (str/ends-with? text "\n")
                   (= 1 (count (filter #(= \newline %) text))))
      (fail! "SH07-B51-GIT-SNAPSHOT" reason))
    (subs text 0 (dec (count text)))))

(defn- parse-show!
  [bytes _]
  (let [text (decode-utf8! bytes "SH07-B51-GIT-SNAPSHOT" :show-utf8)
        match (re-matches
               #"([0-9a-f]{40})\n([0-9a-f]{40})\n((?:[0-9a-f]{40}(?: [0-9a-f]{40})*)?)\n"
               text)]
    (when-not match
      (fail! "SH07-B51-GIT-SNAPSHOT" :show-grammar))
    {:commit (nth match 1)
     :tree (nth match 2)
     :parents (let [parents (nth match 3)]
                (if (str/blank? parents) [] (str/split parents #" ")))}))

(defn- nul-records!
  [bytes reason]
  (let [text (decode-utf8! bytes "SH07-B51-GIT-SNAPSHOT" reason)]
    (cond
      (empty? text) []
      (not (str/ends-with? text "\u0000"))
      (fail! "SH07-B51-GIT-SNAPSHOT" reason {:missing-final-nul true})
      :else (vec (butlast (str/split text #"\u0000" -1))))))

(defn- parse-name-status!
  [parent commit bytes _]
  (let [tokens (nul-records! bytes :diff-name-status-grammar)]
    (when-not (even? (count tokens))
      (fail! "SH07-B51-GIT-SNAPSHOT" :diff-name-status-grammar))
    {:parent parent
     :commit commit
     :entries
     (mapv
      (fn [[status path]]
        (when-not (and (= "A" status) (safe-relative-path? path))
          (fail! "SH07-B51-GIT-SNAPSHOT" :diff-name-status-entry
                 {:status status :path path}))
        {:status :added :path path})
      (partition 2 tokens))}))

(defn- parse-ls-tree!
  [commit requested-paths bytes _]
  (when-not (and (vector? requested-paths)
                 (= (count requested-paths) (count (distinct requested-paths)))
                 (every? safe-relative-path? requested-paths))
    (fail! "SH07-B51-GIT-SNAPSHOT" :requested-paths))
  (let [records (nul-records! bytes :ls-tree-grammar)
        rows
        (mapv
         (fn [record]
           (let [match (re-matches
                        #"([0-7]{6}) ([a-z]+) ([0-9a-f]{40})\t(.+)"
                        record)]
             (when-not match
               (fail! "SH07-B51-GIT-SNAPSHOT" :ls-tree-record
                      {:record record}))
             (let [path (nth match 4)]
               (when-not (some #{path} requested-paths)
                 (fail! "SH07-B51-GIT-SNAPSHOT" :unrequested-tree-path
                        {:path path}))
               {:path path :mode (nth match 1) :type (nth match 2)
                :oid (nth match 3)})))
         records)
        by-path (into {} (map (juxt :path identity) rows))]
    (when-not (= (count rows) (count by-path))
      (fail! "SH07-B51-GIT-SNAPSHOT" :duplicate-tree-path))
    {:commit commit
     :requested-paths requested-paths
     :rows
     (mapv
      (fn [path]
        (if-let [row (get by-path path)]
          (assoc row :present true)
          {:path path :present false :mode nil :type nil :oid nil}))
      requested-paths)}))

(defn- show-operation!
  [repository commit]
  (git-operation!
   repository :show-tree
   ["show" "-s" "--format=%H%n%T%n%P" commit]
   #{0} parse-show!))

(defn- diff-name-status-operation!
  [repository parent commit]
  (git-operation!
   repository :diff-tree-name-status-z
   ["diff-tree" "--no-commit-id" "--name-status" "-r" "-z"
    parent commit]
   #{0} (partial parse-name-status! parent commit)))

(defn- ls-tree-operation!
  [repository commit paths]
  (git-operation!
   repository :ls-tree-z-requested
   (into ["ls-tree" "-z" commit "--"] paths)
   #{0} (partial parse-ls-tree! commit paths)))
