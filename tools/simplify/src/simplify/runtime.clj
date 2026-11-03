(ns simplify.runtime
  "Local execution, atomic state, and verbose, credential-redacted event logs."
  (:refer-clojure :exclude [run!])
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.nio.channels FileChannel]
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
                     (let [out (StringBuilder.)]
                       (doseq [line (line-seq reader)]
                         (.append out (str line "\n"))
                         (event! ctx :command-output {:stream label :line line}))
                       (str out)))))
         stdout (drain (.getInputStream process) "stdout")
         stderr (drain (.getErrorStream process) "stderr")]
     (with-open [writer (io/writer (.getOutputStream process) :encoding "UTF-8")]
       (when input (.write writer (str input))))
     (let [exit (.waitFor process)
           result {:exit exit :out @stdout :err @stderr}]
       (event! ctx :command-end {:argv argv :exit exit :elapsed-ms (quot (- (System/nanoTime) started) 1000000)})
       result))))

(defn run! [ctx cwd argv]
  (let [result (command ctx cwd argv)]
    (when-not (zero? (:exit result)) (fail! "Command failed" (assoc result :argv argv)))
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
        (update! ctx assoc-in [:effects id] {:status :pending})
        (try
          (let [result (operation)]
            (update! ctx assoc-in [:effects id] {:status :done :result result})
            result)
          (catch Exception e
            (update! ctx assoc-in [:effects id] {:status (if (:restricted (ex-data e)) :restricted :uncertain)
                                               :message (.getMessage e)})
            (throw e)))))))

(defn tracking-effect!
  "Status may legitimately cycle. An unresolved earlier write must be inspected first."
  [ctx key operation]
  (let [state (:state ctx)
        id (locking state
             (when-let [prior (get-in @(:state ctx) [:tracking-intents key])]
               (let [status (get-in @(:state ctx) [:effects prior :status])]
                 (when-not (= :done status)
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
    (let [lock (.tryLock channel)]
      (when-not lock (fail! "Another harness owns this repository" {}))
      (try (f) (finally (.release lock))))))
