(ns gravity.self-hosting.sh01-host-resource-broker
  "Cooperative host-wide admission for non-authoritative SH-01 development work.

  Callers supply one trusted, existing host-local coordination root shared by
  every participating worktree.  The broker owns only direct children of that
  root, executes no tests, and persists no test or proof evidence."
  (:require [clojure.edn :as edn]
            [clojure.string :as str])
  (:import (java.io PushbackReader StringReader)
           (java.nio.charset StandardCharsets)
           (java.nio.channels FileChannel FileLock OverlappingFileLockException)
           (java.nio.file Files LinkOption OpenOption Path StandardOpenOption)
           (java.nio.file.attribute FileAttribute PosixFilePermission
                                     PosixFilePermissions)
           (java.util.concurrent TimeUnit)
           (java.util.concurrent.locks ReentrantLock)))

(def ^:private reviewed-capacities
  (array-map :normal 2 :memory-heavy 1 :exclusive 1))

(def ^:private policy-record
  (array-map
   :schema :gravity/sh01-host-resource-policy-v1
   :capacities reviewed-capacities))

(def ^:private supported-classes (set (keys reviewed-capacities)))
(def ^:private default-timeout-ms (* 60 60 1000))
(def ^:private maximum-timeout-ms (* 24 60 60 1000))
(def ^:private poll-ms 10)
(def ^:private maximum-ticket-count 1024)
(def ^:private ticket-pattern
  #"ticket-([0-9]{20})-(normal|memory-heavy|exclusive)\.lock")
(def ^:private local-admission-locks (atom {}))
(def ^:private local-slot-locks (atom {}))
(def ^:private no-follow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private read-write-options
  (into-array OpenOption [StandardOpenOption/READ
                          StandardOpenOption/WRITE
                          LinkOption/NOFOLLOW_LINKS]))
(def ^:private read-only-options
  (into-array OpenOption [StandardOpenOption/READ
                          LinkOption/NOFOLLOW_LINKS]))
(def ^:private file-permissions
  (PosixFilePermissions/asFileAttribute
   (PosixFilePermissions/fromString "rw-------")))

(defn- fail!
  [id message data]
  (throw (ex-info message (assoc data :id id))))

(defn- positive-timeout
  [value]
  (let [value (or value default-timeout-ms)]
    (when-not (and (integer? value)
                   (pos? value)
                   (<= value maximum-timeout-ms))
      (fail! "SH01-BROKER-TIMEOUT-OPTION"
             "SH-01 broker timeout must be a positive bounded integer"
             {:timeout-ms value :maximum-timeout-ms maximum-timeout-ms}))
    (long value)))

(defn- trusted-root
  [value]
  (when (nil? value)
    (fail! "SH01-BROKER-ROOT-REQUIRED"
           "SH-01 broker requires an explicit coordination root"
           {}))
  (let [^Path supplied (.toPath (java.io.File. (str value)))
        ^Path root (.normalize (.toAbsolutePath supplied))]
    (when-not (.isAbsolute supplied)
      (fail! "SH01-BROKER-ROOT-ABSOLUTE"
             "SH-01 broker coordination root must be absolute"
             {:coordination-root (str value)}))
    (when (or (Files/isSymbolicLink root)
              (not (Files/isDirectory root no-follow-links)))
      (fail! "SH01-BROKER-ROOT-INVALID"
             "SH-01 broker coordination root must be an existing non-symlink directory"
             {:coordination-root (str root)}))
    (let [home (Path/of (System/getProperty "user.home")
                        (make-array String 0))]
      (when-not (= (Files/getOwner root no-follow-links)
                   (Files/getOwner home no-follow-links))
        (fail! "SH01-BROKER-ROOT-OWNER"
               "SH-01 broker coordination root must be owned by the current user"
               {:coordination-root (str root)})))
    (try
      (let [permissions (Files/getPosixFilePermissions root no-follow-links)
            required #{PosixFilePermission/OWNER_READ
                       PosixFilePermission/OWNER_WRITE
                       PosixFilePermission/OWNER_EXECUTE}
            forbidden #{PosixFilePermission/GROUP_WRITE
                        PosixFilePermission/OTHERS_WRITE
                        PosixFilePermission/GROUP_READ
                        PosixFilePermission/OTHERS_READ
                        PosixFilePermission/GROUP_EXECUTE
                        PosixFilePermission/OTHERS_EXECUTE}]
        (when (or (not-every? #(contains? permissions %) required)
                  (some forbidden permissions))
          (fail! "SH01-BROKER-ROOT-PERMISSIONS"
                 "SH-01 broker coordination root must be private to its owner"
                 {:coordination-root (str root)
                  :permissions (str (PosixFilePermissions/toString permissions))})))
      (catch UnsupportedOperationException _
        (fail! "SH01-BROKER-ROOT-PERMISSIONS"
               "SH-01 broker coordination root requires POSIX permission checks"
               {:coordination-root (str root)})))
    (try
      (let [mode (Files/getAttribute root "unix:mode" no-follow-links)]
        (when-not (and (number? mode)
                       (= 448 (bit-and (long mode) 4095)))
          (fail! "SH01-BROKER-ROOT-PERMISSIONS"
                 "SH-01 broker coordination root mode must be exactly 0700"
                 {:coordination-root (str root)
                  :mode (when (number? mode)
                          (format "%04o" (bit-and (long mode) 4095)))})))
      (catch UnsupportedOperationException _
        (fail! "SH01-BROKER-ROOT-PERMISSIONS"
               "SH-01 broker coordination root requires Unix mode checks"
               {:coordination-root (str root)})))
    root))

(defn- regular-direct-child!
  [^Path root file-name]
  (let [path (.resolve root file-name)]
    (when (or (Files/isSymbolicLink path)
              (and (Files/exists path no-follow-links)
                   (not (Files/isRegularFile path no-follow-links))))
      (fail! "SH01-BROKER-STATE-CORRUPT"
             "SH-01 broker state contains an unsafe direct child"
             {:coordination-root (str root) :child file-name}))
    path))

(defn- ensure-empty-lock-file!
  [^Path root file-name]
  (let [path (regular-direct-child! root file-name)]
    (when-not (Files/exists path no-follow-links)
      (try
        (Files/createFile path (into-array FileAttribute [file-permissions]))
        (catch java.nio.file.FileAlreadyExistsException _ nil)))
    (when (or (not (Files/isRegularFile path no-follow-links))
              (pos? (Files/size path)))
      (fail! "SH01-BROKER-STATE-CORRUPT"
             "SH-01 broker lock state is not an empty regular file"
             {:coordination-root (str root) :child file-name}))
    path))

(defn- ensure-policy!
  [^Path root]
  (let [path (regular-direct-child! root "policy.edn")
        bytes (.getBytes (str (pr-str policy-record) "\n")
                         StandardCharsets/UTF_8)]
    (when-not (Files/exists path no-follow-links)
      (try
        (Files/write path bytes
                     (into-array OpenOption
                                 [StandardOpenOption/CREATE_NEW
                                  StandardOpenOption/WRITE]))
        (catch java.nio.file.FileAlreadyExistsException _ nil)))
    (when (or (not (Files/isRegularFile path no-follow-links))
              (> (Files/size path) 4096))
      (fail! "SH01-BROKER-POLICY-MISMATCH"
             "SH-01 broker policy file is invalid"
             {:coordination-root (str root)}))
    (let [observed
          (try
            (let [eof (Object.)]
              (with-open [reader
                          (PushbackReader.
                           (StringReader.
                            (String. (Files/readAllBytes path)
                                     StandardCharsets/UTF_8)))]
                (let [value (edn/read {:eof eof} reader)
                      trailing (edn/read {:eof eof} reader)]
                  (if (and (not (identical? eof value))
                           (identical? eof trailing))
                    value
                    ::invalid))))
            (catch Throwable _ ::invalid))]
      (when-not (= policy-record observed)
        (fail! "SH01-BROKER-POLICY-MISMATCH"
               "SH-01 broker policy differs from reviewed capacities"
               {:coordination-root (str root)
                :expected policy-record
                :observed observed})))
    path))

(defn- slot-file-names
  []
  (vec
   (concat
    (for [index (range (:normal reviewed-capacities))]
      (format "slot-normal-%02d.lock" index))
    (for [index (range (:memory-heavy reviewed-capacities))]
      (format "slot-memory-heavy-%02d.lock" index))
    (for [index (range (:exclusive reviewed-capacities))]
      (format "slot-exclusive-%02d.lock" index)))))

(defn- class-slot-file-names
  [resource-class]
  (filterv #(str/starts-with? % (str "slot-" (name resource-class) "-"))
           (slot-file-names)))

(declare with-admission-lock remaining-nanos)

(defn- prepare-root!
  [^Path root deadline]
  (ensure-empty-lock-file! root "admission.lock")
  (loop []
    (if-let [prepared
             (with-admission-lock
              root deadline
              #(do
                 (ensure-policy! root)
                 (doseq [file-name (slot-file-names)]
                   (ensure-empty-lock-file! root file-name))
                 root))]
      prepared
      (if (pos? (remaining-nanos deadline))
        (recur)
        (fail! "SH01-BROKER-TIMEOUT"
               "SH-01 broker timed out preparing shared policy"
               {:coordination-root (str root)})))))

(defn- local-admission-lock
  [^Path root]
  (let [key (str root)]
    (get
     (swap! local-admission-locks
            (fn [locks]
              (if (contains? locks key)
                locks
                (assoc locks key (ReentrantLock. true)))))
     key)))

(defn- local-slot-lock
  [^Path root file-name]
  (let [key (str root "/" file-name)]
    (get
     (swap! local-slot-locks
            (fn [locks]
              (if (contains? locks key)
                locks
                (assoc locks key (ReentrantLock. true)))))
     key)))

(defn- remaining-nanos
  [deadline]
  (max 0 (- deadline (System/nanoTime))))

(defn- try-file-lock
  [^FileChannel channel]
  (try
    (.tryLock channel)
    (catch OverlappingFileLockException _ nil)))

(defn- with-admission-lock
  [^Path root deadline thunk]
  (let [^ReentrantLock local (local-admission-lock root)
        wait-nanos (min (remaining-nanos deadline)
                        (.toNanos TimeUnit/MILLISECONDS poll-ms))]
    (when (and (pos? wait-nanos)
               (.tryLock local wait-nanos TimeUnit/NANOSECONDS))
      (try
        (with-open [channel
                    (FileChannel/open (.resolve root "admission.lock")
                                      read-write-options)]
          (when-let [^FileLock lock (try-file-lock channel)]
            (try
              (thunk)
              (finally (.release lock)))))
        (finally (.unlock local))))))

(defn- ticket-record
  [^Path path]
  (let [file-name (str (.getFileName path))]
    (when (str/starts-with? file-name "ticket-")
      (if-let [[_ sequence resource-class] (re-matches ticket-pattern file-name)]
        (let [sequence (parse-long sequence)]
          (when-not (and (integer? sequence)
                         (< (long sequence) Long/MAX_VALUE))
            (fail! "SH01-BROKER-STATE-CORRUPT"
                   "SH-01 broker ticket sequence is outside its bounded range"
                   {:child file-name}))
          {:path path
           :file-name file-name
           :sequence sequence
           :resource-class (keyword resource-class)})
        (fail! "SH01-BROKER-STATE-CORRUPT"
               "SH-01 broker contains a malformed ticket"
               {:child file-name})))))

(defn- ticket-records
  [^Path root]
  (with-open [stream (Files/newDirectoryStream root)]
    (let [records (->> (iterator-seq (.iterator stream))
                       (keep ticket-record)
                       (sort-by :sequence)
                       vec)]
      (when (> (count records) maximum-ticket-count)
        (fail! "SH01-BROKER-STATE-CORRUPT"
               "SH-01 broker ticket count exceeds its reviewed bound"
               {:ticket-count (count records)
                :maximum maximum-ticket-count}))
      records)))

(defn- live-tickets!
  ([^Path root]
   (live-tickets! root nil))
  ([^Path root own-path]
  (reduce
   (fn [{:keys [live recovered]} ticket]
     (let [path (:path ticket)]
       (when (or (Files/isSymbolicLink path)
                 (not (Files/isRegularFile path no-follow-links))
                 (pos? (Files/size path)))
         (fail! "SH01-BROKER-STATE-CORRUPT"
                "SH-01 broker ticket is not a regular direct child"
                {:child (:file-name ticket)}))
       ;; Never open a second descriptor for our own locked ticket.  POSIX
       ;; record locks are process-associated on some hosts, where closing a
       ;; probe descriptor can release a lock held through another channel.
       (if (= path own-path)
         {:live (conj live ticket) :recovered recovered}
         (let [stale?
               (with-open [channel (FileChannel/open path read-write-options)]
                 (when-let [^FileLock stale-lock (try-file-lock channel)]
                   (.release stale-lock)
                   true))]
           (if stale?
             (do
               (Files/delete path)
               {:live live :recovered (inc recovered)})
             {:live (conj live ticket) :recovered recovered})))))
   {:live [] :recovered 0}
   (ticket-records root))))

(defn- create-ticket!
  [^Path root resource-class]
  (let [{:keys [live recovered]} (live-tickets! root)
        sequence (inc (reduce max -1 (map :sequence live)))
        file-name (format "ticket-%020d-%s.lock"
                          sequence (name resource-class))
        path (.resolve root file-name)]
    (Files/createFile path (into-array FileAttribute [file-permissions]))
    (let [channel (FileChannel/open path read-write-options)
          lock (try-file-lock channel)]
      (when-not lock
        (.close channel)
        (fail! "SH01-BROKER-LOCK"
               "SH-01 broker could not lock its new ticket"
               {:child file-name}))
      {:path path
       :file-name file-name
       :sequence sequence
       :resource-class resource-class
       :channel channel
       :lock lock
       :recovered-stale-tickets recovered})))

(defn- close-ticket!
  [ticket delete?]
  (when-let [^FileLock lock (:lock ticket)]
    (when (.isValid lock) (.release lock)))
  (when-let [^FileChannel channel (:channel ticket)]
    (when (.isOpen channel) (.close channel)))
  (when delete?
    (Files/deleteIfExists ^Path (:path ticket))))

(defn- try-slot
  [^Path root file-name]
  (let [^ReentrantLock local (local-slot-lock root file-name)]
    (when (.tryLock local)
      (let [channel (atom nil)]
        (try
          (let [opened (FileChannel/open (.resolve root file-name)
                                         read-write-options)
                _ (reset! channel opened)
                lock (try-file-lock opened)]
            (if lock
              {:file-name file-name
               :channel opened
               :lock lock
               :local-lock local}
              (do
                (.close opened)
                (.unlock local)
                nil)))
          (catch Throwable throwable
            (try
              (when-let [^FileChannel opened @channel]
                (when (.isOpen opened) (.close opened)))
              (finally (.unlock local)))
            (throw throwable)))))))

(defn- release-slots!
  [slots]
  (let [errors (atom [])]
    (doseq [{:keys [^FileLock lock ^FileChannel channel
                    ^ReentrantLock local-lock]}
            (reverse slots)]
      (try
        (when (and lock (.isValid lock)) (.release lock))
        (catch Throwable throwable
          (swap! errors conj throwable))
        (finally
          (try
            (when (and channel (.isOpen channel)) (.close channel))
            (catch Throwable throwable
              (swap! errors conj throwable))
            (finally
              (try
                (when (and local-lock
                           (.isHeldByCurrentThread local-lock))
                  (.unlock local-lock))
                (catch Throwable throwable
                  (swap! errors conj throwable))))))))
    (when (seq @errors)
      (fail! "SH01-BROKER-RELEASE"
             "SH-01 broker could not completely release its slot set"
             {:release-errors (mapv #(or (.getMessage ^Throwable %) (str %))
                                    @errors)}))))

(defn- try-all-slots
  [^Path root file-names]
  (loop [remaining file-names
         acquired []]
    (if-let [file-name (first remaining)]
      (let [slot
            (try
              (try-slot root file-name)
              (catch Throwable throwable
                (release-slots! acquired)
                (throw throwable)))]
        (if slot
          (recur (next remaining) (conj acquired slot))
          (do (release-slots! acquired) nil)))
      acquired)))

(defn- try-first-slot
  [^Path root file-names]
  (loop [remaining file-names]
    (when-let [file-name (first remaining)]
      (if-let [slot (try-slot root file-name)]
        [slot]
        (recur (next remaining))))))

(defn- admissible?
  [live ticket]
  (let [exclusive-index
        (first (keep-indexed
                (fn [index value]
                  (when (= :exclusive (:resource-class value)) index))
                live))
        prefix (if exclusive-index (subvec (vec live) 0 exclusive-index)
                   (vec live))]
    (if (= :exclusive (:resource-class ticket))
      (= (:sequence ticket) (:sequence (first live)))
      (and (some #(= (:sequence ticket) (:sequence %)) prefix)
           (= (:sequence ticket)
              (:sequence
               (first (filter #(= (:resource-class ticket)
                                  (:resource-class %))
                              prefix))))))))

(defn- queue-position
  [live ticket]
  (inc (or (first (keep-indexed
                   (fn [index value]
                     (when (= (:sequence ticket) (:sequence value)) index))
                   live))
           (count live))))

(defn- try-admit!
  [^Path root ticket]
  (ensure-policy! root)
  (doseq [file-name (slot-file-names)]
    (ensure-empty-lock-file! root file-name))
  (let [{:keys [live recovered]} (live-tickets! root (:path ticket))
        own (some #(when (= (:sequence ticket) (:sequence %)) %) live)]
    (when-not own
      (fail! "SH01-BROKER-STALE"
             "SH-01 broker ticket disappeared before admission"
             {:ticket (:file-name ticket)}))
    (when (admissible? live ticket)
      (let [file-names
            (if (= :exclusive (:resource-class ticket))
              (slot-file-names)
              (class-slot-file-names (:resource-class ticket)))]
        (when-let [slots (if (= :exclusive (:resource-class ticket))
                           (try-all-slots root file-names)
                           (try-first-slot root file-names))]
          (try
            (close-ticket! ticket true)
            {:slots slots
             :queue-position (queue-position live ticket)
             :queue-length (count live)
             :recovered-stale-tickets
             (+ (:recovered-stale-tickets ticket) recovered)}
            (catch Throwable throwable
              (try
                (release-slots! slots)
                (catch Throwable release-error
                  (.addSuppressed throwable release-error)))
              (throw throwable))))))))

(defn- event
  [resource-class outcome data]
  (merge
   (array-map
    :schema :gravity/sh01-host-resource-telemetry-v1
    :authority :non-authoritative
    :authoritative? false
    :resource-class resource-class
    :capacity (get reviewed-capacities resource-class)
    :outcome outcome)
   data))

(defn- semantic-receipt
  "Returns the deterministic, normalized result of one broker operation.

  Observational root, ticket, queue, timing, and stale-recovery fields never
  enter this receipt. The schema itself declares its non-authoritative scope."
  [resource-class outcome diagnostic-id]
  (array-map
   :schema :gravity/sh01-host-resource-non-authoritative-receipt-v1
   :resource-class resource-class
   :capacity (get reviewed-capacities resource-class)
   :outcome outcome
   :diagnostic-id diagnostic-id))

(defn- emit!
  [options value]
  (when-let [callback (:on-event options)]
    (try (callback value) (catch Throwable _ nil)))
  value)

(defn- acquire-lease!
  [options resource-class]
  (when-not (contains? supported-classes resource-class)
    (fail! "SH01-BROKER-RESOURCE-CLASS"
           "SH-01 broker received an unknown resource class"
           {:resource-class resource-class
            :supported (vec (sort supported-classes))}))
  (let [timeout-ms (positive-timeout (:timeout-ms options))
        started (System/nanoTime)
        deadline (+ started (.toNanos TimeUnit/MILLISECONDS timeout-ms))
        root (prepare-root! (trusted-root (:coordination-root options)) deadline)
        ticket
        (loop []
          (if-let [created
                   (with-admission-lock
                    root deadline #(create-ticket! root resource-class))]
            created
            (if (pos? (remaining-nanos deadline))
              (recur)
              (fail! "SH01-BROKER-TIMEOUT"
                     "SH-01 broker timed out before queue admission"
                     {:resource-class resource-class
                      :timeout-ms timeout-ms
                      :coordination-root (str root)}))))]
    (emit! options
           (event resource-class :queued
                  {:ticket (:file-name ticket)
                   :timeout-ms timeout-ms
                   :coordination-root (str root)}))
    (try
      (loop []
        (if-let [admission (with-admission-lock
                            root deadline #(try-admit! root ticket))]
          (if-not (pos? (remaining-nanos deadline))
            (do
              (release-slots! (:slots admission))
              (emit! options
                     (event resource-class :timeout
                            {:ticket (:file-name ticket)
                             :timeout-ms timeout-ms
                             :coordination-root (str root)}))
              (fail! "SH01-BROKER-TIMEOUT"
                     "SH-01 broker admission completed after its deadline"
                     {:resource-class resource-class
                      :timeout-ms timeout-ms
                      :ticket (:file-name ticket)
                      :coordination-root (str root)}))
            (let [recovered (:recovered-stale-tickets admission)
                  _ (when (pos? recovered)
                      (emit!
                       options
                       (event resource-class :stale-recovered
                              {:ticket (:file-name ticket)
                               :recovered-stale-tickets recovered
                               :coordination-root (str root)})))
                  wait-ms (long (/ (- (System/nanoTime) started) 1000000.0))
                telemetry
                (emit!
                 options
                 (event resource-class :admitted
                        (merge
                         (select-keys admission
                                      [:queue-position :queue-length
                                       :recovered-stale-tickets])
                         {:ticket (:file-name ticket)
                          :wait-ms wait-ms
                          :coordination-root (str root)})))]
            {:schema :gravity/sh01-host-resource-lease-v1
             :resource-class resource-class
             :coordination-root (str root)
             :owner-thread-id (.getId (Thread/currentThread))
             :slots (:slots admission)
             :receipt (semantic-receipt resource-class :admitted nil)
             :telemetry telemetry
             :released? (atom false)
             :options options}))
          (if (pos? (remaining-nanos deadline))
            (do (Thread/sleep poll-ms) (recur))
            (do
              (let [cleanup-deadline
                    (+ (System/nanoTime) (.toNanos TimeUnit/SECONDS 1))]
                (loop []
                  (when (and (pos? (remaining-nanos cleanup-deadline))
                             (not
                              (with-admission-lock
                               root cleanup-deadline
                               #(do (close-ticket! ticket true) true))))
                    (recur))))
              (emit! options
                     (event resource-class :timeout
                            {:ticket (:file-name ticket)
                             :timeout-ms timeout-ms
                             :coordination-root (str root)}))
              (fail! "SH01-BROKER-TIMEOUT"
                     "SH-01 broker timed out waiting for host-wide capacity"
                     {:resource-class resource-class
                      :timeout-ms timeout-ms
                      :ticket (:file-name ticket)
                      :coordination-root (str root)})))))
      (catch Throwable throwable
        ;; Leave an unlocked stale name when admission cleanup itself fails.
        ;; The next admission reclaims it under the admission lock, avoiding
        ;; an out-of-lock delete race with a directory scan.
        (try (close-ticket! ticket false) (catch Throwable _))
        (throw throwable)))))

(defn acquire!
  "Acquires one host-wide lease before the bounded timeout.

  The returned value is a linear local handle and must be passed exactly once
  to `release!`. Ticket and admission telemetry is explicitly
  non-authoritative."
  [options resource-class]
  (try
    (acquire-lease! options resource-class)
    (catch InterruptedException error
      (.interrupt (Thread/currentThread))
      (throw
       (ex-info "SH-01 broker admission was interrupted"
                {:id "SH01-BROKER-INTERRUPTED"
                 :resource-class resource-class
                 :coordination-root (some-> (:coordination-root options) str)
                 :receipt
                 (semantic-receipt resource-class :rejected
                                   "SH01-BROKER-INTERRUPTED")}
                error)))
    (catch clojure.lang.ExceptionInfo error
      (let [data (ex-data error)
            diagnostic-id (:id data)]
        (throw
         (if (:receipt data)
           error
           (ex-info (.getMessage error)
                    (assoc data :receipt
                           (semantic-receipt resource-class :rejected
                                             diagnostic-id))
                    error)))))
    (catch Throwable error
      (throw
       (ex-info "SH-01 broker host coordination failed"
                {:id "SH01-BROKER-IO"
                 :resource-class resource-class
                 :coordination-root (some-> (:coordination-root options) str)
                 :receipt
                 (semantic-receipt resource-class :rejected
                                   "SH01-BROKER-IO")}
                error)))))

(def ^:private lease-keys
  #{:schema :resource-class :coordination-root :owner-thread-id :slots
    :receipt :telemetry :released? :options})

(def ^:private receipt-keys
  #{:schema :resource-class :capacity :outcome :diagnostic-id})

(def ^:private admitted-telemetry-keys
  #{:schema :authority :authoritative? :resource-class :capacity :outcome
    :queue-position :queue-length :recovered-stale-tickets :ticket :wait-ms
    :coordination-root})

(def ^:private slot-keys
  #{:file-name :channel :lock :local-lock})

(def ^:private accepted-option-keys
  #{:coordination-root :timeout-ms :on-event})

(defn- invalid-lease!
  ([reason] (invalid-lease! reason {}))
  ([reason data]
   (fail! "SH01-BROKER-LEASE-INVALID"
          "SH-01 broker live lease failed validation"
          (assoc data :reason reason))))

(defn- lease-ensure!
  [value reason data]
  (when-not value
    (invalid-lease! reason data))
  true)

(defn- bounded-read-once
  [^Path path maximum-bytes]
  (with-open [channel (FileChannel/open path read-only-options)]
    (let [buffer (java.nio.ByteBuffer/allocate 1024)
          output (java.io.ByteArrayOutputStream.)]
      (loop [total 0]
        (.clear buffer)
        (let [read (.read channel buffer)]
          (cond
            (= -1 read)
            (.toByteArray output)

            (> (+ total read) maximum-bytes)
            ::too-large

            :else
            (do
              (.flip buffer)
              (let [chunk (byte-array (.remaining buffer))]
                (.get buffer chunk)
                (.write output chunk 0 (alength chunk)))
              (recur (+ total read)))))))))

(defn- read-reviewed-policy
  [^Path root]
  (let [path (regular-direct-child! root "policy.edn")]
    (lease-ensure! (Files/exists path no-follow-links)
                   :policy-missing
                   {:path "policy.edn"})
    (lease-ensure! (Files/isRegularFile path no-follow-links)
                   :policy-shape
                   {:path "policy.edn"})
    (let [size-before
          (.size ^java.nio.file.attribute.BasicFileAttributes
                 (Files/readAttributes path
                                       java.nio.file.attribute.BasicFileAttributes
                                       no-follow-links))]
      (lease-ensure! (<= size-before 4096)
                     :policy-bound
                     {:path "policy.edn" :size size-before})
      (let [bytes (bounded-read-once path 4096)
            bytes-again (bounded-read-once path 4096)
            size-after
            (.size ^java.nio.file.attribute.BasicFileAttributes
                   (Files/readAttributes path
                                         java.nio.file.attribute.BasicFileAttributes
                                         no-follow-links))]
        (lease-ensure! (and (not= ::too-large bytes)
                            (not= ::too-large bytes-again)
                            (= size-before size-after)
                            (java.util.Arrays/equals ^bytes bytes
                                                      ^bytes bytes-again))
                       :policy-mutated
                       {:path "policy.edn"})
        (let [eof (Object.)
              observed
              (try
                (with-open [reader
                            (PushbackReader.
                             (StringReader.
                              (String. ^bytes bytes StandardCharsets/UTF_8)))]
                  (let [value (edn/read {:eof eof} reader)
                        trailing (edn/read {:eof eof} reader)]
                    (when (and (not (identical? eof value))
                               (identical? eof trailing))
                      value)))
                (catch Throwable _ nil))]
          (lease-ensure! (= policy-record observed)
                         :policy-mismatch
                         {:path "policy.edn"})
          observed)))))

(defn- validate-slot!
  [^Path root resource-class slot]
  (lease-ensure! (and (map? slot)
                      (= slot-keys (set (keys slot))))
                 :slot-shape
                 {:resource-class resource-class})
  (let [file-name (:file-name slot)
        allowed (set (if (= :exclusive resource-class)
                       (slot-file-names)
                       (class-slot-file-names resource-class)))]
    (lease-ensure! (and (string? file-name)
                        (contains? allowed file-name))
                   :slot-name
                   {:resource-class resource-class :file-name file-name})
    (let [path (regular-direct-child! root file-name)
          attributes
          (when (Files/exists path no-follow-links)
            (Files/readAttributes path
                                  java.nio.file.attribute.BasicFileAttributes
                                  no-follow-links))]
      (lease-ensure! (and attributes
                          (.isRegularFile ^java.nio.file.attribute.BasicFileAttributes
                                          attributes)
                          (zero? (.size ^java.nio.file.attribute.BasicFileAttributes
                                        attributes)))
                     :slot-file
                     {:resource-class resource-class :file-name file-name})
      (let [channel (:channel slot)
            lock (:lock slot)
            local-lock (:local-lock slot)]
        (lease-ensure! (instance? FileChannel channel)
                       :slot-channel
                       {:file-name file-name})
        (lease-ensure! (and (.isOpen ^FileChannel channel)
                            (instance? FileLock lock)
                            (.isValid ^FileLock lock)
                            (identical? channel (.channel ^FileLock lock))
                            (not (.isShared ^FileLock lock)))
                       :slot-lock
                       {:file-name file-name})
        (lease-ensure! (and (instance? ReentrantLock local-lock)
                            (.isFair ^ReentrantLock local-lock)
                            (.isLocked ^ReentrantLock local-lock)
                            (.isHeldByCurrentThread ^ReentrantLock local-lock)
                            (= 1 (.getHoldCount ^ReentrantLock local-lock)))
                       :slot-local-lock
                       {:file-name file-name})
        slot))))

(defn validate-live-lease!
  "Validates one live broker lease without granting or mutating authority.

  Validation is deliberately stricter than the public release transition:
  every schema and observation is checked, every slot must still be locked by
  the current thread, and the expected root and resource class are supplied by
  the caller.  A validated lease is returned unchanged; every invalid value
  fails with the stable `SH01-BROKER-LEASE-INVALID` diagnostic."
  [lease expected-root expected-class]
  (try
    (lease-ensure! (map? lease) :lease-shape {})
    (lease-ensure! (= lease-keys (set (keys lease)))
                   :lease-keys
                   {:keys (set (keys lease))})
    (lease-ensure! (= :gravity/sh01-host-resource-lease-v1 (:schema lease))
                   :lease-schema
                   {:schema (:schema lease)})
    (lease-ensure! (contains? supported-classes expected-class)
                   :expected-resource-class
                   {:resource-class expected-class})
    (lease-ensure! (= expected-class (:resource-class lease))
                   :resource-class
                   {:expected expected-class
                    :observed (:resource-class lease)})
    (let [root (trusted-root expected-root)
          root-string (str root)
          lease-root (:coordination-root lease)
          options (:options lease)]
      (lease-ensure! (and (string? lease-root)
                          (= root-string lease-root))
                     :coordination-root
                     {:expected root-string :observed lease-root})
      (lease-ensure! (map? options) :options-shape {})
      (lease-ensure! (every? accepted-option-keys (keys options))
                     :options-keys
                     {:keys (set (keys options))})
      (let [option-root (:coordination-root options)
            timeout-ms (positive-timeout (:timeout-ms options))]
        (lease-ensure! (= root-string (str (trusted-root option-root)))
                       :options-root
                       {:expected root-string :observed option-root})
        (lease-ensure! (or (not (contains? options :on-event))
                           (nil? (:on-event options))
                           (ifn? (:on-event options)))
                       :options-callback
                       {})
        (lease-ensure! (= timeout-ms (positive-timeout (:timeout-ms options)))
                       :options-timeout
                       {:timeout-ms (:timeout-ms options)}))
      (lease-ensure! (= (.getId (Thread/currentThread))
                        (:owner-thread-id lease))
                     :owner-thread
                     {:owner-thread-id (:owner-thread-id lease)})
      (lease-ensure! (and (instance? clojure.lang.Atom (:released? lease))
                          (false? @(:released? lease)))
                     :released-state
                     {})
      (read-reviewed-policy root)
      (let [slots (:slots lease)
            expected-names (if (= :exclusive expected-class)
                             (set (slot-file-names))
                             nil)
            expected-count (if (= :exclusive expected-class)
                            (count (slot-file-names))
                            1)
            slot-names (when (vector? slots) (mapv :file-name slots))]
        (lease-ensure! (and (vector? slots)
                            (= expected-count (count slots))
                            (= (count slot-names) (count (distinct slot-names)))
                            (if expected-names
                              (= expected-names (set slot-names))
                              (every? #(contains? (set (class-slot-file-names expected-class)) %)
                                      slot-names)))
                       :slots
                       {:resource-class expected-class
                        :slot-names slot-names})
        (doseq [slot slots]
          (validate-slot! root expected-class slot)))
      (let [receipt (:receipt lease)
            expected-capacity (get reviewed-capacities expected-class)]
        (lease-ensure! (and (map? receipt)
                            (= receipt-keys (set (keys receipt)))
                            (= :gravity/sh01-host-resource-non-authoritative-receipt-v1
                               (:schema receipt))
                            (= expected-class (:resource-class receipt))
                            (= expected-capacity (:capacity receipt))
                            (= :admitted (:outcome receipt))
                            (nil? (:diagnostic-id receipt)))
                       :receipt
                       {:resource-class expected-class}))
      (let [telemetry (:telemetry lease)
            expected-capacity (get reviewed-capacities expected-class)
            timeout-ms (positive-timeout (get-in lease [:options :timeout-ms]))]
        (lease-ensure! (and (map? telemetry)
                            (= admitted-telemetry-keys (set (keys telemetry)))
                            (= :gravity/sh01-host-resource-telemetry-v1
                               (:schema telemetry))
                            (= :non-authoritative (:authority telemetry))
                            (false? (:authoritative? telemetry))
                            (= expected-class (:resource-class telemetry))
                            (= expected-capacity (:capacity telemetry))
                            (= :admitted (:outcome telemetry))
                            (integer? (:queue-position telemetry))
                            (pos? (long (:queue-position telemetry)))
                            (<= (long (:queue-position telemetry)) maximum-ticket-count)
                            (integer? (:queue-length telemetry))
                            (pos? (long (:queue-length telemetry)))
                            (<= (long (:queue-length telemetry)) maximum-ticket-count)
                            (<= (long (:queue-position telemetry))
                                (long (:queue-length telemetry)))
                            (integer? (:recovered-stale-tickets telemetry))
                            (<= 0 (long (:recovered-stale-tickets telemetry)))
                            (<= (long (:recovered-stale-tickets telemetry))
                                maximum-ticket-count)
                            (string? (:ticket telemetry))
                            (let [[_ _ class-name]
                                  (re-matches ticket-pattern (:ticket telemetry))]
                              (= (name expected-class) class-name))
                            (integer? (:wait-ms telemetry))
                            (<= 0 (long (:wait-ms telemetry)))
                            (<= (long (:wait-ms telemetry)) timeout-ms)
                            (= (str (trusted-root expected-root))
                               (:coordination-root telemetry)))
                       :telemetry
                       {:resource-class expected-class}))
      lease)
    (catch clojure.lang.ExceptionInfo error
      (if (= "SH01-BROKER-LEASE-INVALID" (:id (ex-data error)))
        (throw error)
        (invalid-lease! :validation-error
                        {:cause-id (:id (ex-data error))})))
    (catch Throwable _
      (invalid-lease! :validation-error {}))))

(defn release!
  "Releases a lease exactly once."
  [lease]
  (when-not (and (map? lease)
                 (= :gravity/sh01-host-resource-lease-v1 (:schema lease))
                 (instance? clojure.lang.Atom (:released? lease))
                 (vector? (:slots lease)))
    (fail! "SH01-BROKER-RELEASE"
           "SH-01 broker release requires a broker lease"
           {}))
  (when-not (= (:owner-thread-id lease) (.getId (Thread/currentThread)))
    (fail! "SH01-BROKER-RELEASE"
           "SH-01 broker lease must be released by its acquiring thread"
           {:resource-class (:resource-class lease)}))
  (when-not (compare-and-set! (:released? lease) false :releasing)
    (fail! "SH01-BROKER-RELEASE"
           "SH-01 broker lease was released more than once"
           {:resource-class (:resource-class lease)}))
  (try
    (release-slots! (:slots lease))
    (reset! (:released? lease) true)
    (emit! (:options lease)
           (event (:resource-class lease) :released
                  {:coordination-root (:coordination-root lease)}))
    (semantic-receipt (:resource-class lease) :released nil)
    (catch Throwable throwable
      ;; `release-slots!` is deliberately retry-safe: it skips invalid locks,
      ;; closed channels, and a local lock no longer held by this thread. A
      ;; partial cleanup failure therefore restores the linear handle so its
      ;; owner can retry instead of permanently leaking host capacity.
      (reset! (:released? lease) false)
      (throw throwable))))

(def ^:private ^:dynamic *process-custody* nil)

(def ^:private quarantined-leases (atom {}))

(defn retain-process-start-custody!
  "Register BEFORE native start. A pending or thrown start has unknown creation.
  This is cooperative in-JVM custody, not a startup watchdog or OS containment."
  []
  (when *process-custody*
    (let [custody (atom {:launch-state :pending :creation-coverage :unknown
                         :owner-thread-id (.getId (Thread/currentThread))
                         :process nil :owned nil :result nil})]
      (swap! *process-custody* conj custody)
      custody)))

(defn- require-start-owner! [custody]
  (when-not (= (:owner-thread-id @custody) (.getId (Thread/currentThread)))
    (fail! "SH01-PROCESS-OWNERSHIP" "Process startup belongs to its retaining thread" {})))

(defn record-started-process!
  "Keep the actual returned Process even if token retention subsequently throws."
  [custody process]
  (when custody
    (require-start-owner! custody)
    (swap! custody assoc :launch-state :returned :process process))
  process)

(defn record-started-ownership! [custody owned]
  (when custody
    (require-start-owner! custody)
    (when-not (and (= :returned (:launch-state @custody))
                   (identical? (:process @custody) (:process owned))
                   (= (:owner-thread-id @custody) (:owner-thread-id owned))
                   (nil? (:owned @custody)))
      (fail! "SH01-PROCESS-OWNERSHIP" "Startup token must retain its returned Process once" {}))
    (swap! custody assoc :owned owned))
  owned)

(defn record-process-start-error! [custody error]
  (when custody
    (require-start-owner! custody)
    (swap! custody #(assoc % :launch-state (if (:process %) :retention-threw :start-threw)
                           :start-error error)))
  error)

(defn retained-start-token
  "Reuse a token only for this exact returned Process in the current lease scope."
  [process]
  (when *process-custody*
    (when-let [custody (some #(when (identical? process (:process @%)) %) @*process-custody*)]
      (require-start-owner! custody)
      (:owned @custody))))

(defn retain-process-custody!
  "Retains the normal runner's ownership token within an active with-lease.
  Outside that scope the normal process API is unchanged."
  [owned]
  (when *process-custody*
    (if-let [custody (some #(when (identical? (:process owned) (:process @%)) %)
                           @*process-custody*)]
      (do
        (require-start-owner! custody)
        (when-not (identical? owned (:owned @custody))
          (fail! "SH01-PROCESS-OWNERSHIP" "Process wait must use its startup token" {}))
        custody)
      (let [custody (atom {:owned owned :result nil})]
        (swap! *process-custody* conj custody)
        custody))))

(defn record-process-result!
  "Records actual terminal observations without inferring descendant coverage."
  [custody result]
  (when custody (swap! custody assoc :result result))
  result)

(defn- terminal-process-custody?
  [custody]
  (let [{:keys [result launch-state process owned start-error] :as record} @custody]
    (and (or (not (contains? record :launch-state))
             (and (= :returned launch-state) (instance? Process process)
                  (some? owned) (nil? start-error)))
         (true? (:root-exited? result))
         (false? (:cleanup-root-alive? result))
         (true? (:tracked-owned-processes-terminal? result))
         (false? (:capture-failed? result))
         (every? (fn [stream]
                   (and (#{:complete :truncated}
                         (get result (keyword (str stream "-capture-status"))))
                        (nil? (get result (keyword (str stream "-capture-error"))))
                        (false? (get result (keyword (str stream "-capture-forced-close?"))))))
                 ["stdout" "stderr"]))))

(defn- throw-disposition-errors!
  [errors observation]
  (let [cause-with (fn [predicate error]
                     (loop [error error seen #{}]
                       (when (and error (not (contains? seen error)))
                         (if (predicate error) error
                             (recur (.getCause ^Throwable error) (conj seen error))))))
        fatal? #(or (instance? VirtualMachineError %) (instance? ThreadDeath %)
                    (instance? LinkageError %))
        primary (or (some #(cause-with fatal? %) errors) (first errors))]
    (when (some #(cause-with (fn [error] (instance? InterruptedException error)) %) errors)
      (.interrupt (Thread/currentThread)))
    (when primary
      (doseq [error errors :when (and (not (identical? primary error))
                                      (not (cause-with #(identical? primary %) error)))]
        (.addSuppressed ^Throwable primary error))
      ;; Preserve actual objects locally on the thrown error; never encode
      ;; these linear handles as a published receipt or auto-release them.
      (.addSuppressed ^Throwable primary
                      (ex-info "Resource disposition observation"
                               {:id "SH01-BROKER-DISPOSITION" :observation observation}))
      (throw primary))))


(defn with-lease
  "Runs `thunk` while holding exactly one reviewed host-wide resource lease.

  The optional fourth argument observes actual result/error and resource
  disposition after the release attempt or quarantine decision. Raw lease and
  process registry objects remain local. This is not telemetry, durable custody,
  or an exit guard for a one-shot parent. The legacy three-argument path is
  unchanged."
  ([options resource-class thunk]
   (let [lease (acquire! options resource-class)]
     (try
       (thunk)
       (finally
         (release! lease)))))
  ([options resource-class thunk observe!]
   (when-not (ifn? observe!)
     (fail! "SH01-BROKER-DISPOSITION" "Disposition observer must be callable" {}))
   (let [processes (atom [])
         lease-result (try {:lease (acquire! options resource-class)}
                           (catch Throwable error {:error error}))
         lease (:lease lease-result)
         operation (if lease
                     (binding [*process-custody* processes]
                       (try {:returned? true :value (thunk) :error nil}
                            (catch Throwable error {:returned? false :value nil :error error})))
                     {:returned? false :value nil :error (:error lease-result)})
         terminal (when lease
                    (try {:complete? (every? terminal-process-custody? @processes)}
                         (catch Throwable error {:error error})))
         disposition
         (cond
           (nil? lease)
           {:state :acquire-failed :release-attempted? false :receipt nil
            :error (:error lease-result) :parent-exit-custody :not-established}

           (:complete? terminal)
           (try
             {:state :released :release-attempted? true :receipt (release! lease)
              :error nil :parent-exit-custody :not-required-for-this-released-lease}
             (catch Throwable error
               (swap! quarantined-leases assoc (:released? lease)
                      {:lease lease :processes processes :release-error error})
               {:state :release-failed :release-attempted? true :receipt nil
                :error error :parent-exit-custody :unsupported-in-JVM-only}))

           :else
           (do
             (swap! quarantined-leases assoc (:released? lease)
                    {:lease lease :processes processes :assessment-error (:error terminal)})
             {:state :quarantined :release-attempted? false :receipt nil :error (:error terminal)
              :parent-exit-custody :unsupported-in-JVM-only}))
         observation {:resource-class resource-class :lease lease :processes processes
                      :operation operation :disposition disposition}
         observer-error (try (observe! observation) nil (catch Throwable error error))
         errors (vec (distinct (remove nil? [(:error operation) (:error disposition) observer-error])))]
     (throw-disposition-errors! errors observation)
     (:value operation)))
  ([options resource-class thunk finalize-owned-resources! observe!]
   (when-not (and (ifn? finalize-owned-resources!) (ifn? observe!))
     (fail! "SH01-BROKER-OWNED-RESOURCE" "Resource finalizer and observer must be callable" {}))
   (let [processes (atom [])
         interrupted? (volatile! false)
         remember-interrupt! #(when (.isInterrupted (Thread/currentThread)) (vreset! interrupted? true))
         lease-result (try {:lease (acquire! options resource-class)} (catch Throwable error {:error error}))
         lease (:lease lease-result)
         operation (if lease
                     (binding [*process-custody* processes]
                       (try {:returned? true :value (thunk) :error nil}
                            (catch Throwable error {:returned? false :value nil :error error})))
                     {:returned? false :value nil :error (:error lease-result)})
         _ (remember-interrupt!)
         terminal (when lease
                    (try {:complete? (every? terminal-process-custody? @processes)}
                         (catch Throwable error {:error error})))
         resources (if lease
                     (try
                       (let [value (finalize-owned-resources!
                                    {:lease lease :processes processes :operation operation :terminal terminal})]
                         (when-not (and (map? value)
                                        (contains? #{:not-acquired :released :quarantined :release-failed} (:state value))
                                        (boolean? (:release-attempted? value))
                                        (contains? value :custody)
                                        (or (nil? (:error value)) (instance? Throwable (:error value)))
                                        (case (:state value)
                                          :released (and (true? (:release-attempted? value)) (nil? (:error value)))
                                          :not-acquired (nil? (:error value))
                                          :release-failed (instance? Throwable (:error value))
                                          :quarantined true false))
                           (fail! "SH01-BROKER-OWNED-RESOURCE" "Invalid owned resource disposition" {:value value}))
                         value)
                       (catch Throwable error
                         {:state :quarantined :release-attempted? :unknown :error error
                          :custody finalize-owned-resources! :receipt nil}))
                     {:state :not-acquired :release-attempted? false :error nil :custody nil :receipt nil})
         _ (remember-interrupt!)
         reusable? (and (:complete? terminal) (nil? (:error resources))
                        (contains? #{:not-acquired :released} (:state resources)))
         disposition
         (cond
           (nil? lease) {:state :acquire-failed :release-attempted? false :receipt nil
                         :error (:error lease-result) :parent-exit-custody :not-established}
           reusable? (try
                       {:state :released :release-attempted? true :receipt (release! lease)
                        :error nil :parent-exit-custody :not-required-for-this-released-lease}
                       (catch Throwable error
                         {:state :release-failed :release-attempted? true :receipt nil
                          :error error :parent-exit-custody :unsupported-in-JVM-only}))
           :else {:state :quarantined :release-attempted? false :receipt nil
                  :error (or (:error terminal) (:error resources)) :parent-exit-custody :unsupported-in-JVM-only})
         _ (remember-interrupt!)
         observation {:resource-class resource-class :lease lease :processes processes
                      :operation operation :terminal terminal :owned-resources resources :disposition disposition}
         _ (when (and lease (not= :released (:state disposition)))
             (swap! quarantined-leases assoc (:released? lease)
                    {:lease lease :processes processes :owned-resources resources
                     :finalize-owned-resources! finalize-owned-resources! :observation observation}))
         observer-error (try (observe! observation) nil (catch Throwable error error))
         errors (vec (distinct (remove nil? [(:error operation) (:error terminal) (:error resources)
                                             (:error disposition) observer-error])))]
     (try
       (throw-disposition-errors! errors observation)
       (:value operation)
       (finally
         (when @interrupted? (.interrupt (Thread/currentThread))))))))
