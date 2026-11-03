(ns gravity.self-hosting.sh01-operation-fence
  "Closed source-owned declarations and immutable journal I/O. No admission,
  process, lock acquisition or caller-supplied authority installation API."
  (:require [acc08-fence :as protocol] [clojure.walk :as walk])
  (:import [java.nio ByteBuffer]
           [java.nio.channels FileChannel]
           [java.nio.file Files LinkOption OpenOption Path StandardOpenOption]
           [java.nio.file.attribute BasicFileAttributes FileAttribute PosixFilePermissions]))

(def protocol-sha256 "c45311b3d8b00010d3a7d6fc6694a34bc714c551fc20f733711a16595c3acfe4")
(def ^:private nofollow (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private declarations
  ;; Only independently accepted SOURCE configuration can add an entry. Neither
  ;; an on-disk marker nor caller data populates this map. Empty means no real
  ;; root is activated. The adjacent proposal is not installed configuration.
  {})
(defn- refuse! [reason]
  (throw (ex-info "Operation fence authority/I/O unavailable or invalid"
                  {:id "SH01-OPERATION-FENCE-AUTHORITY" :reason reason})))
(defn- need! [yes reason] (when-not yes (refuse! reason)))
(defn- closed? [value fields] (and (map? value) (= (set fields) (set (keys value)))))
(defn- hex? [value n] (and (string? value) (boolean (re-matches (re-pattern (str "[0-9a-f]{" n "}")) value))))
(defn- absolute? [s]
  (and (string? s) (let [p (Path/of s (make-array String 0))]
                    (and (.isAbsolute p) (= s (str (.normalize p)))))))
(defn- reference! [r]
  (need! (and (closed? r [:path :sha256]) (absolute? (:path r)) (hex? (:sha256 r) 64)) :provenance-reference))
(defn- provenance! [p]
  (need! (and (closed? p [:owner-thread :accepted-record])
              (string? (:owner-thread p)) (not (empty? (:owner-thread p)))) :provenance)
  (reference! (:accepted-record p)))
(defn declaration-domain [d]
  (let [digest (fn [value]
                 (protocol/sha256
                  (.getBytes (pr-str (walk/postwalk #(if (map? %) (into (sorted-map) %) %) value))
                             java.nio.charset.StandardCharsets/UTF_8)))]
    {:domain-sha256 (digest (select-keys d [:project :root :canonical-L]))
     :participants-sha256 (digest (:participants d))
     :protocol-config-sha256 protocol-sha256}))

(defn validate-declaration!
  "Validate DATA shape/continuity only; this function does NOT grant trust or
  install a domain. Only the private source registry selects trusted entries."
  [d]
  (need! (closed? d [:schema :project :root :canonical-L :domain :protocol-source-sha256
                     :participants :operations :provenance]) :declaration-shape)
  (need! (= :gravity/operation-fence-domain-v1 (:schema d)) :declaration-schema)
  (let [p (:project d)]
    (need! (and (closed? p [:repository :source-commit :source-tree])
                (= "burn2delete/uniform" (:repository p))
                (hex? (:source-commit p) 40) (hex? (:source-tree p) 40)) :project))
  (need! (and (absolute? (:root d)) (absolute? (:canonical-L d))
              (= protocol-sha256 (:protocol-source-sha256 d))) :root-or-protocol)
  (protocol/validate-domain! (:domain d))
  (need! (= (:domain d) (declaration-domain d)) :domain-continuity)
  (provenance! (:provenance d))
  (need! (and (vector? (:participants d)) (seq (:participants d))
              (= (count (:participants d)) (count (set (map :id (:participants d)))))) :participants)
  (doseq [p (:participants d)]
    (need! (and (closed? p [:id :kind :root :classes :entry :source-sha256 :provenance])
                (keyword? (:id p)) (#{:C :L-only :excluded} (:kind p))
                (absolute? (:root p)) (vector? (:classes p))
                (symbol? (:entry p)) (hex? (:source-sha256 p) 64)) :participant)
    (case (:kind p)
      :C (need! (and (= (:root d) (:root p))
                     (= [:normal :memory-heavy :exclusive] (:classes p))) :C-participant)
      :L-only (need! (and (= (:canonical-L d) (:root p)) (empty? (:classes p))) :L-participant)
      :excluded (need! (empty? (:classes p)) :excluded-participant))
    (provenance! (:provenance p)))
  (need! (some #(= :C (:kind %)) (:participants d)) :missing-C-participant)
  (need! (vector? (:operations d)) :operations)
  (need! (= (count (:operations d)) (count (set (map #(get-in % [:identity :operation-id]) (:operations d))))) :operation-reuse)
  (doseq [o (:operations d)]
    (need! (closed? o [:identity :provenance :request :source]) :operation)
    (protocol/validate-identity! (:identity o))
    (need! (= (:domain d) (protocol/domain-of (:identity o))) :operation-domain)
    (need! (= (get-in o [:identity :request-sha256]) (get-in o [:request :sha256])) :request-pin)
    (need! (= (get-in o [:identity :source-sha256]) (get-in o [:source :sha256])) :source-pin)
    (provenance! (:provenance o)) (reference! (:request o)) (reference! (:source o)))
  d)

(defn- attrs [^Path p] (Files/readAttributes p BasicFileAttributes nofollow))
(defn- root-key! [^Path root]
  (let [a (attrs root)]
    (need! (and (.isAbsolute root) (= root (.normalize root))
                (not (Files/isSymbolicLink root)) (.isDirectory a) (.fileKey a)
                (= root (.toRealPath root (make-array LinkOption 0)))) :canonical-root)
    (.fileKey a)))
(defn trusted-declaration
  "No marker/option/boolean registers a root. A source-registry hit must match
  canonical root and closed declaration; all real roots are currently absent."
  [^Path root]
  (when-let [d (get declarations (str root))]
    (validate-declaration! d)
    (need! (= (str root) (:root d)) :registry-root)
    (root-key! root)
    d))
(defn- restore-input-interrupt! [error]
  (try
    (let [seen (java.util.IdentityHashMap.)]
      (loop [cause error]
        (when (and cause (not (.containsKey seen cause)))
          (.put seen cause true)
          (if (instance? InterruptedException cause)
            (.interrupt (Thread/currentThread))
            (recur (.getCause ^Throwable cause))))))
    (catch Throwable _ nil)))

(defn- open-fence-input! [path]
  (Files/newInputStream path
    (into-array OpenOption [StandardOpenOption/READ LinkOption/NOFOLLOW_LINKS])))

(defn- with-fence-input! [path read!]
  ;; Reader closure/primary cell are constructed before opening. Once returned,
  ;; this actual input is owned by the following try/finally, closed once.
  (let [primary (volatile! nil) input (open-fence-input! path)]
    (try
      (read! input)
      (catch Throwable error (vreset! primary error) (throw error))
      (finally
        (let [cleanup (try (.close ^java.io.InputStream input) nil
                           (catch Throwable error error))]
          (restore-input-interrupt! @primary)
          (restore-input-interrupt! cleanup)
          (when cleanup
            (if-let [original @primary]
              (when-not (identical? original cleanup)
                (try (.addSuppressed ^Throwable original cleanup) (catch Throwable _ nil)))
              (throw cleanup))))))))

(defn- read-pinned-reference! [reference read! exact-byte-length]
  (reference! reference)
  (let [path (Path/of (:path reference) (make-array String 0)) a (attrs path)]
    (need! (and (.isRegularFile a) (not (Files/isSymbolicLink path))) :reference-file)
    (when (some? exact-byte-length)
      (need! (some? (.fileKey a)) :reference-identity)
      (need! (= exact-byte-length (.size a)) :reference-length))
    (let [bytes (with-fence-input! path read!)
          after (attrs path)]
      (need! (and (.isRegularFile after) (= (.fileKey a) (.fileKey after))
                  (= (.size a) (.size after))
                  (= (:sha256 reference) (protocol/sha256 bytes))) :reference-continuity)
      bytes)))
(defn- pinned-reference! [reference]
  (read-pinned-reference! reference #(.readAllBytes ^java.io.InputStream %) nil)
  nil)
(defn read-exact-child-DATA!
  "Read integrity for an ALREADY SOURCE-selected reference, not authentication
  of its caller, operation, current export record or invocation. Expected bytes
  come from the existing canonical request/identity product. The only numeric
  boundary is readNBytes/byte-array representability; no new workload allowance.
  Reuses the original NOFOLLOW/stable-reference/read-close error mechanics."
  [reference exact-byte-length]
  (need! (and (integer? exact-byte-length) (<= 0 exact-byte-length Integer/MAX_VALUE)) :reference-length)
  (read-pinned-reference!
   reference
   (fn [^java.io.InputStream input]
     (let [bytes (.readNBytes input (int exact-byte-length))]
       (need! (= exact-byte-length (alength ^bytes bytes)) :reference-length)
       (need! (= -1 (.read input)) :reference-extra)
       bytes))
   exact-byte-length))
(defn authenticate-operation!
  "Continuity of records selected by accepted SOURCE provenance, not a new
  cryptographic attestation. Never called on a caller-installed declaration."
  [d identity]
  (let [o (some #(when (= identity (:identity %)) %) (:operations d))]
    (need! o :operation-not-declared)
    (doseq [p (cons (:provenance d) (cons (:provenance o) (map :provenance (:participants d))))]
      (pinned-reference! (:accepted-record p)))
    (pinned-reference! (:request o)) (pinned-reference! (:source o))
    {:status :authorized-operation :identity identity}))

(def ^:private maximum-record-bytes
  ;; Exact canonical v7 terminal representation is the largest writable frame.
  ;; This is a representation-derived bound, not a new workload allowance.
  (alength ^bytes (protocol/encode-record :terminal
                   (zipmap [:operation-id :request-sha256 :source-sha256 :domain-sha256 :participants-sha256 :protocol-config-sha256]
                           (cons "ffffffff-ffff-ffff-ffff-ffffffffffff" (repeat 5 (apply str (repeat 64 "f")))))
                   (apply str (repeat 64 "f")))))
(defn- close-new-record! [^FileChannel channel] (.close channel))
(defn- preserve-close! [channel primary]
  (try (close-new-record! channel)
       (catch Throwable error
         ;; A close failure can be suppressed behind the original write error.
         ;; Restore its causal interruption before it becomes only diagnostic.
         (try
           (let [seen (java.util.IdentityHashMap.)]
             (loop [cause error]
               (when (and cause (not (.containsKey seen cause)))
                 (.put seen cause true)
                 (if (instance? InterruptedException cause)
                   (.interrupt (Thread/currentThread))
                   (recur (.getCause ^Throwable cause))))))
           (catch Throwable _ nil))
         (if-let [original @primary]
           (when-not (identical? original error)
             (try (.addSuppressed ^Throwable original error) (catch Throwable _ nil)))
           (throw error)))))
(defn- force-new-record! [^FileChannel channel] (.force channel true))
(defn journal-ports
  "Private DATA or trusted production store mechanics only, no authentication.
  Caller supplies the common owner's strict reserved namespace enumerator and
  transaction-owner check. No directory/root is created, reset or repaired."
  [^Path root names! assert-transaction!]
  (let [key (root-key! root)
        check! (fn [] (assert-transaction!) (need! (= key (root-key! root)) :root-replaced))
        path! (fn [name]
                (need! (and (string? name)
                            (re-matches #"acc08-fence-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\.(claim|terminal)\.edn" name)) :record-name)
                (.resolve root name))
        read! (fn [name]
                (check!)
                (let [path (path! name) before (attrs path)]
                  (need! (and (.isRegularFile before) (.fileKey before)
                              (= 1 (long (Files/getAttribute path "unix:nlink" nofollow)))
                              (<= (.size before) maximum-record-bytes)) :record-file)
                  (let [bytes (with-fence-input! path
                                (fn [^java.io.InputStream in]
                                  (let [b (.readNBytes in maximum-record-bytes)]
                                    (need! (= -1 (.read in)) :record-size) b)))
                        after (attrs path)]
                    (need! (and (= (.fileKey before) (.fileKey after))
                                (.isRegularFile after) (= (.size before) (.size after))
                                (= (.size after) (alength ^bytes bytes))
                                (= 1 (long (Files/getAttribute path "unix:nlink" nofollow)))) :record-replaced)
                    (check!) {:name name :kind :regular :bytes bytes})))]
    {:read-all! (fn []
                  (check!)
                  (let [names (vec (sort (names! root))) entries (mapv read! names)]
                    (need! (= names (vec (sort (names! root)))) :namespace-changed)
                    (check!) entries))
     :publish-new! (fn [name bytes]
                     (check!)
                     (need! (and (instance? (Class/forName "[B") bytes)
                                 (<= (alength ^bytes bytes) maximum-record-bytes)) :record-size)
                     (let [path (path! name)
                           channel (FileChannel/open path
                                     #{StandardOpenOption/CREATE_NEW StandardOpenOption/WRITE LinkOption/NOFOLLOW_LINKS}
                                     (into-array FileAttribute [(PosixFilePermissions/asFileAttribute (PosixFilePermissions/fromString "rw-------"))]))
                           primary (volatile! nil)]
                       (try
                         (let [buffer (ByteBuffer/wrap ^bytes bytes)]
                           (while (.hasRemaining buffer) (.write channel buffer)))
                         (force-new-record! channel)
                         (need! (java.util.Arrays/equals ^bytes bytes ^bytes (:bytes (read! name))) :publication-readback)
                         (check!)
                         :published-new
                         (catch Throwable error (vreset! primary error) (throw error))
                         (finally (preserve-close! channel primary)))))}))
