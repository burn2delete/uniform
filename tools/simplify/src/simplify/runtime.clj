(ns simplify.runtime
  "Local execution, atomic state, and verbose, credential-redacted event logs."
  (:refer-clojure :exclude [run!])
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.nio.channels FileChannel OverlappingFileLockException]
           [java.nio.file Files StandardCopyOption StandardOpenOption]
           [java.security MessageDigest]
           [java.time Instant]))

(defn fail! [message data] (throw (ex-info message data)))
(defn canonical [path] (.getCanonicalPath (io/file path)))
(defn digest [value]
  (format "%064x" (BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256")
                                        (.getBytes (str value) "UTF-8")))))

(defn redact [value]
  (cond
    (map? value) (into {} (map (fn [[k v]]
                                [k (if (re-find #"(?i)(authorization|token|secret|password|api.?key)" (name k))
                                     "[REDACTED]" (redact v))]) value))
    (sequential? value) (mapv redact value)
    (string? value) (-> value
                        (str/replace #"(?:gh[pousr]_[A-Za-z0-9_]+|github_pat_[A-Za-z0-9_]+|sk-[A-Za-z0-9_-]+)" "[REDACTED]")
                        (str/replace #"(?i)(Bearer\s+)[^\s\"']+" "$1[REDACTED]"))
    :else value))

(defn event! [ctx kind data]
  (let [entry (redact (merge {:time (str (Instant/now)) :event (name kind)} data))
        line (json/write-str entry)
        lock (:log-lock ctx)]
    (locking lock
      (binding [*out* *err*] (println line) (flush))
      (when-let [path (:log-file ctx)] (spit path (str line "\n") :append true)))
    entry))

(defn read-edn [path] (edn/read-string {:readers {} :default (fn [tag _] (fail! "Unsupported EDN tag" {:tag tag}))} (slurp path)))

(defn write-edn! [destination value]
    (let [path (.toPath (io/file destination))
          tmp (Files/createTempFile (.getParent path) "state-" ".edn" (make-array java.nio.file.attribute.FileAttribute 0))]
      (try
        (with-open [out (FileChannel/open tmp (into-array StandardOpenOption [StandardOpenOption/WRITE]))]
          (let [buffer (java.nio.ByteBuffer/wrap (.getBytes (pr-str value) "UTF-8"))]
            (while (.hasRemaining buffer) (.write out buffer)))
          (.force out true))
        (Files/move tmp path (into-array StandardCopyOption [StandardCopyOption/ATOMIC_MOVE StandardCopyOption/REPLACE_EXISTING]))
        (finally (Files/deleteIfExists tmp)))))

(defn save! [ctx]
  (let [state (:state ctx)]
    (locking state (write-edn! (:state-file ctx) @state))))

(defn update! [ctx f & args]
  (when (and (:coordinator-owner ctx) (not @(:coordinator-owner ctx)))
    (fail! "Only the locked coordinator may update campaign state" {}))
  (let [state (:state ctx)]
    (locking state
      (apply swap! state f args)
      (save! ctx)
      @state)))

(defn command
  "Argument vectors only. Both output streams are drained concurrently. No shell."
  ([ctx cwd argv] (command ctx cwd argv nil))
  ([ctx cwd argv input]
   (when-not (and (seq argv) (every? string? argv)) (fail! "Invalid command arguments" {:argv argv}))
   (event! ctx :command-start {:cwd (str cwd) :argv argv})
   (let [started (System/nanoTime)
         process (.start (doto (ProcessBuilder. ^java.util.List (vec argv)) (.directory (io/file cwd))))
         drain (fn [stream label]
                 (future
                   (with-open [reader (io/reader stream :encoding "UTF-8")]
                     (let [out (StringBuilder.) pending (StringBuilder.) buffer (char-array 4096)]
                       (loop []
                         (let [n (.read reader buffer) start (.length pending)]
                           (if (pos? n)
                             (do
                               (.append out buffer 0 n)
                               (.append pending buffer 0 n)
                               ;; Redact complete lines, never isolated chunks that
                               ;; could divide a credential across event records.
                               (loop [start start]
                                 (let [end (.indexOf pending "\n" start)]
                                   (when (not (neg? end))
                                     (event! ctx :command-output {:stream label :line (.substring pending 0 end)})
                                     (.delete pending 0 (inc end))
                                     (recur 0))))
                               (recur))
                             (when (pos? (.length pending))
                               (event! ctx :command-output {:stream label :line (str pending)})))))
                       (str out)))))
         stdout (drain (.getInputStream process) "stdout")
         stderr (drain (.getErrorStream process) "stderr")]
     (with-open [writer (io/writer (.getOutputStream process) :encoding "UTF-8")]
       (when input (.write writer (str input))))
     (let [exit (.waitFor process)
           result {:exit exit :out @stdout :err @stderr}]
       (event! ctx :command-end {:argv argv :exit exit :elapsed-ms (quot (- (System/nanoTime) started) 1000000)})
       result))))

(defn authorization-denied? [message]
(boolean (re-find #"(?i)(HTTP (401|403|429)|requested URL returned error: (401|403|429)|permission denied|operation not permitted|read-only file system|sandbox[^\n]*(denied|deny|forbidden)|permission to .+ denied|write access .+ not granted|authentication failed|resource not accessible|requires authentication|bad credentials|rate limit exceeded|secondary rate limit|not authorized)"
                    (or message ""))))

(defn denials!
  "Denied controller operations survive campaign changes and cannot transfer between harnesses."
  [ctx f]
  (if-let [directory (:coordination-dir ctx)]
    (do (.mkdirs (io/file directory))
        (loop []
          (let [result (with-open [channel (FileChannel/open (.toPath (io/file directory "denials.lock"))
                                                            (into-array StandardOpenOption [StandardOpenOption/CREATE StandardOpenOption/WRITE]))]
                         (when-let [lock (try (.tryLock channel) (catch OverlappingFileLockException _ nil))]
                           (try
                             (let [file (io/file directory "denials.edn")
                                   [updated result] (f (if (.exists file) (read-edn file) {}))]
                               (write-edn! file updated) {:value result})
                             (finally (.release lock)))))]
            (if result (:value result) (do (Thread/sleep 25) (recur))))))
    (second (f {}))))

(defn assert-authorized! [ctx kind key]
  (when (denials! ctx (fn [denials] [denials (get denials [kind key])]))
    (fail! "Operation denied in a coordinated harness; cannot retry or transfer it" {:restricted true :kind kind :key key})))

(defn record-denial! [ctx kind key details]
  (denials! ctx (fn [denials] [(assoc denials [kind key] details) true])))

(defn remember-denial! [ctx kind key details]
  (record-denial! ctx kind key details)
  (when (or (nil? (:coordinator-owner ctx)) @(:coordinator-owner ctx))
    (update! ctx assoc-in [(case kind :command :restricted-commands :github :restricted-actions) key] details)))

(defn run! [ctx cwd argv]
  (let [action (cond
                 (= ["git" "push"] (vec (take 2 argv))) ["git-push" (get-in ctx [:config :repository])]
                 (= ["gh" "pr" "merge"] (vec (take 3 argv))) ["gh-pr-merge" (get-in ctx [:config :repository])]
                 :else argv)
        key (digest (pr-str action))
        _ (assert-authorized! ctx :command key)
        _ (when (get-in @(:state ctx) [:restricted-commands key])
            (fail! "Restricted controller command cannot be retried" {:argv argv :restricted true}))
        result (command ctx cwd argv)]
    (when-not (zero? (:exit result))
      (let [restricted (authorization-denied? (str (:err result) "\n" (:out result)))]
        (when restricted
          (remember-denial! ctx :command key {:argv argv :error (:err result)}))
        (fail! "Command failed" (assoc result :argv argv :restricted restricted))))
    (str/trim (:out result))))

(defn git [ctx cwd & args] (run! ctx cwd (into ["git"] args)))

(defn effect!
  "Record an intent before a remote mutation. An uncertain effect is never replayed automatically."
  [ctx id operation]
  (let [prior (get-in @(:state ctx) [:effects id])]
    (case (:status prior)
      :done (:result prior)
      :restricted (fail! "Restricted effect cannot be retried" {:effect id :restricted true})
      (:pending :uncertain) (fail! "External effect needs reconciliation; not replaying" {:effect id :prior prior})
      (do
        (update! ctx assoc-in [:effects id]
                 (cond-> {:status :pending} (= :not-applied (:status prior)) (assoc :previous prior)))
        (try
          (let [result (operation)]
            (update! ctx update-in [:effects id] assoc :status :done :result result)
            result)
          (catch Exception e
            (update! ctx update-in [:effects id] assoc :status (if (:restricted (ex-data e)) :restricted :uncertain)
                     :message (.getMessage e))
            (throw e)))))))

(defn tracking-effect!
  "Status may legitimately cycle. An unresolved earlier write must be inspected first."
  [ctx key operation]
  (let [state (:state ctx)
        id (locking state
             (when-let [prior (get-in @(:state ctx) [:tracking-intents key])]
               (let [status (get-in @(:state ctx) [:effects prior :status])]
                 (when-not (#{:done :not-applied} status)
                   (fail! "Tracking effect needs reconciliation; not issuing another write"
                          {:effect prior :restricted (= :restricted status)}))))
             (let [n (inc (or (:tracking-sequence @(:state ctx)) 0))
                   id (str key "/" n)]
               (update! ctx #(-> % (assoc :tracking-sequence n) (assoc-in [:tracking-intents key] id)))
               id))]
    (effect! ctx id operation)))

(defn with-lock [ctx f]
  (with-open [channel (FileChannel/open (.toPath (io/file (:lock-file ctx)))
                                       (into-array StandardOpenOption [StandardOpenOption/CREATE StandardOpenOption/WRITE]))]
    (let [lock (try (.tryLock channel) (catch OverlappingFileLockException _ nil))]
      (when-not lock (fail! "Another harness owns this repository" {}))
      (try
        ;; Contexts can be loaded while a coordinator is writing. Mutable
        ;; commands use only the state observed after acquiring its lock.
        (when (.exists (io/file (:state-file ctx)))
          (let [state (read-edn (:state-file ctx)) config (:config ctx)]
            (when-not (and (= 1 (:schema state))
                           (= (:repository config) (:repository state)) (= (:campaign config) (:campaign state)))
              (fail! "State belongs to another campaign or schema" {}))
            (reset! (:state ctx) state)))
        (when-let [owner (:coordinator-owner ctx)] (reset! owner true))
        (try (f)
             (catch Exception e
               (when (#{"run" "resume"} (:command ctx)) (update! ctx assoc :status :attention))
               (throw e)))
        (finally
          (when-let [owner (:coordinator-owner ctx)] (reset! owner false))
          (.release lock))))))
