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

(defn with-lease
  "Runs `thunk` while holding exactly one reviewed host-wide resource lease."
  [options resource-class thunk]
  (let [lease (acquire! options resource-class)]
    (try
      (thunk)
      (finally
        (release! lease)))))
