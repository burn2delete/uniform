(ns gravity.tools.migrate-workstream-ledger
  "Migrate the v1 aggregate lifecycle ledger into immutable shards.

  Migration is a one-shot, fail-closed operation.  It validates and snapshots
  the complete source before creating anything, refuses every existing output
  (including byte-identical outputs), and writes each new artifact through a
  same-directory atomic move while holding the repository ledger lock."
  (:require [clojure.string :as str])
  (:import (java.nio ByteBuffer)
           (java.nio.charset CharacterCodingException CodingErrorAction
                                StandardCharsets)
           (java.nio.channels FileChannel OverlappingFileLockException)
           (java.nio.file Files LinkOption Path Paths StandardCopyOption
                          StandardOpenOption)
           (java.nio.file.attribute FileAttribute)
           (java.security MessageDigest)
           (java.util Arrays)))

(System/setProperty "gravity.workstream-governance.library" "true")
(load-file "tools/validate_workstream_governance.clj")
(alias 'governance 'gravity.workstream-governance)

(def ^:private maximum-json-bytes (* 2 1024 1024))
(def ^:private active-states
  #{"draft" "frozen" "review-pending" "accepted" "integration-eligible"})
(def ^:private repository-root
  (.normalize (.toAbsolutePath (Paths/get "." (make-array String 0)))))
(def ^:private repository-lock
  (.resolve repository-root ".workstream-ledger.lock"))

(defn- path-for [value]
  (.normalize (.toAbsolutePath (Paths/get (str value) (make-array String 0)))))

(defn- nofollow []
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- sha256-bytes [bytes]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") bytes)]
    (apply str (map #(format "%02x" (bit-and (int %) 0xff)) digest))))

(defn- decode-utf8 [bytes]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (str (.decode decoder (ByteBuffer/wrap bytes)))
      (catch CharacterCodingException exception
        (throw (ex-info "JSON document is not UTF-8"
                        {:cause (.getMessage exception)} exception))))))

(defn- symlink-component? [^Path path]
  (loop [current (.normalize path)]
    (if current
      (if (Files/isSymbolicLink current)
        true
        (recur (.getParent current)))
      false)))

(defn- read-json-file [^Path path]
  (when (symlink-component? path)
    (throw (ex-info "JSON path contains a symbolic link" {:path (str path)})))
  (when-not (Files/isRegularFile path (nofollow))
    (throw (ex-info "JSON path is not a regular file" {:path (str path)})))
  (let [bytes (Files/readAllBytes path)]
    (when (> (alength bytes) maximum-json-bytes)
      (throw (ex-info "JSON document exceeds byte limit"
                      {:path (str path) :maximum maximum-json-bytes})))
    {:path path :bytes bytes
     :value (governance/read-strict-json (decode-utf8 bytes))}))

(defn- write-bytes! [^Path path bytes]
  (Files/write path bytes (make-array java.nio.file.OpenOption 0)))

(defn- atomic-create-new! [^Path target bytes]
  "Create TARGET atomically without replacing an existing destination."
  (when (Files/exists target (nofollow))
    (throw (ex-info "migration destination already exists"
                    {:path (str target)})))
  (let [parent (.getParent target)]
    (when (symlink-component? parent)
      (throw (ex-info "migration destination parent contains a symbolic link"
                      {:path (str parent)})))
    (Files/createDirectories parent (make-array FileAttribute 0))
    (let [temporary (Files/createTempFile parent ".workstream-migrate-" ".tmp"
                                          (make-array FileAttribute 0))]
      (try
        (write-bytes! temporary bytes)
        (try
          (Files/move temporary target
                      (into-array StandardCopyOption
                                  [StandardCopyOption/ATOMIC_MOVE]))
          (catch java.nio.file.AtomicMoveNotSupportedException _
            (Files/move temporary target (make-array StandardCopyOption 0))))
        (finally (Files/deleteIfExists temporary))))))

(defn- json-bytes [value]
  (.getBytes (str (governance/canonical-json value) "\n") StandardCharsets/UTF_8))

(defn- safe-id [value]
  (when-not (and (string? value)
                 (re-matches #"[a-z0-9][a-z0-9./-]*" value))
    (throw (ex-info "workstream id is not a stable path component" {:id value})))
  value)

(defn- regular-json-files-under [^Path root]
  (if-not (Files/exists root (nofollow))
    []
    (let [stream (Files/walk root (into-array java.nio.file.FileVisitOption []))]
      (try
        (vec
         (filter (fn [^Path path]
                   (and (Files/isRegularFile path (nofollow))
                        (str/ends-with? (str/lower-case (str (.getFileName path)))
                                        ".json")))
                 (iterator-seq (.iterator stream))))
        (finally (.close stream))))))

(defn- transaction-output [repo-root item]
  (let [id (safe-id (get item "id"))
        state (get item "state")
        terminal? (not (contains? active-states state))
        bytes (json-bytes item)
        digest (sha256-bytes bytes)
        relative-path (if terminal?
                        (str "contracts/workstream-records/terminal/"
                             id "-" digest ".json")
                        (str "contracts/workstream-active/" id ".json"))]
    {"id" id
     "path" relative-path
     :absolute-path (path-for relative-path)
     :bytes bytes
     "sha256" digest
     "state" state
     "invariant_family" (get item "invariant_family")
     "dependencies" (get item "dependencies")
     "terminal" terminal?}))

(defn- parse-options [args]
  (loop [remaining args options {:source "contracts/workstream-ledger-v1.json"
                                  :manifest "contracts/workstream-ledger.json"}]
    (if-let [argument (first remaining)]
      (case argument
        "--source" (recur (nnext remaining)
                           (assoc options :source (or (second remaining)
                                                      (throw (ex-info "--source requires a path" {})))))
        "--manifest" (recur (nnext remaining)
                             (assoc options :manifest (or (second remaining)
                                                          (throw (ex-info "--manifest requires a path" {})))))
        (throw (ex-info (str "unknown argument: " argument) {})))
      options)))

(defn- with-repository-lock [thunk]
  (Files/createDirectories repository-root (make-array FileAttribute 0))
  (with-open [channel (FileChannel/open
                       repository-lock
                       (into-array StandardOpenOption
                                   [StandardOpenOption/CREATE
                                    StandardOpenOption/WRITE]))]
    (let [held (try
                 (.tryLock channel)
                 (catch OverlappingFileLockException _ nil))]
      (when-not held
        (throw (ex-info "another workstream ledger operation holds the repository lock"
                        {:lock (str repository-lock)})))
      (try
        (thunk)
        (finally (.release held))))))

(defn migrate! [{:keys [source manifest]}]
  (with-repository-lock
    (fn []
      ;; Everything through manifest construction is read-only.  In particular,
      ;; malformed or semantically invalid source data cannot create one shard.
      (let [source-path (path-for source)
            source-snapshot (read-json-file source-path)
            legacy (:value source-snapshot)
            items (get legacy "workstreams")
            source-errors (governance/validate-ledger legacy)]
        (when (seq source-errors)
          (throw (ex-info "source ledger fails v1 governance"
                          {:diagnostics source-errors})))
        (when-not (and (map? legacy) (vector? items))
          (throw (ex-info "source ledger must contain a workstreams vector" {})))
        (let [repo-root repository-root
              manifest-path (path-for manifest)
              source-relative (str (.relativize repo-root source-path))
              aggregate-digest
              (sha256-bytes (.getBytes
                             (governance/canonical-json items)
                             StandardCharsets/UTF_8))
              outputs (mapv #(transaction-output repo-root %) items)
              entries (mapv (fn [[ordinal output]]
                              (dissoc (assoc output "ordinal" ordinal)
                                      :absolute-path :bytes))
                            (map-indexed vector outputs))
              sorted-entries (vec (sort-by #(get % "id") entries))
              terminal-count (count (filter #(true? (get % "terminal")) entries))
              active-count (- (count items) terminal-count)
              manifest-value
              {"schema_version" 2
               "contract_id" "gravity/workstream-ledger-v2"
               "governance_contract" "contracts/workstream-governance.json"
               "record_root" "contracts/workstream-records"
               "active_root" "contracts/workstream-active"
               "record_count" (count items)
               "terminal_count" terminal-count
               "active_count" active-count
               "aggregate_sha256" aggregate-digest
               "migration" {"source_path" source-relative
                             "source_sha256" (sha256-bytes (:bytes source-snapshot))
                             "source_schema_version" 1
                             "source_contract_id" "gravity/workstream-ledger-v1"
                             "decoded_aggregate_sha256" aggregate-digest
                             "decoded_record_count" (count items)
                             "parity" "exact"}
               "records" sorted-entries}
              manifest-bytes (json-bytes manifest-value)
              output-targets (conj (mapv :absolute-path outputs) manifest-path)
              target-collisions (for [[path count] (frequencies output-targets)
                                      :when (> count 1)] path)
              existing-targets (filter #(Files/exists % (nofollow)) output-targets)
              root-json (concat
                         (regular-json-files-under
                          (.resolve repo-root "contracts/workstream-records"))
                         (regular-json-files-under
                          (.resolve repo-root "contracts/workstream-active")))
              record-root (.resolve repo-root "contracts/workstream-records")
              active-root (.resolve repo-root "contracts/workstream-active")]
          (when (seq target-collisions)
            (throw (ex-info "migration output paths collide"
                            {:paths (map str target-collisions)})))
          ;; Existing outputs are never adopted, even if their bytes match.
          (when (seq existing-targets)
            (throw (ex-info "migration refuses existing destinations"
                            {:paths (map str existing-targets)})))
          (when (seq root-json)
            (throw (ex-info "migration refuses a non-empty shard root"
                            {:paths (map str root-json)})))
          (when (or (symlink-component? record-root)
                    (symlink-component? active-root))
            (throw (ex-info "migration shard root contains a symbolic link"
                            {:record-root (str record-root)
                             :active-root (str active-root)})))
          (when (symlink-component? manifest-path)
            (throw (ex-info "migration manifest path contains a symbolic link"
                            {:path (str manifest-path)})))
          (when-not (= source-relative "contracts/workstream-ledger-v1.json")
            (throw (ex-info "migration source must be the retained v1 snapshot"
                            {:source source-relative})))
          ;; Close the source TOCTOU window before the first output write.
          (when-not (Arrays/equals ^bytes (:bytes source-snapshot)
                                   ^bytes (:bytes (read-json-file source-path)))
            (throw (ex-info "source ledger changed during migration"
                            {:path (str source-path)})))
          (let [created (atom [])]
            (try
              (doseq [output outputs]
                (atomic-create-new! (:absolute-path output) (:bytes output))
                (swap! created conj (:absolute-path output)))
              (atomic-create-new! manifest-path manifest-bytes)
              (swap! created conj manifest-path)
              (println (str "migrated " (count items) " records; terminal="
                            terminal-count ", active=" active-count
                            ", aggregate_sha256=" aggregate-digest))
              manifest-value
              (catch Throwable exception
                ;; No partially migrated tree is left behind after a failed
                ;; create or concurrent destination race.
                (doseq [path (reverse @created)]
                  (Files/deleteIfExists path))
                (throw exception)))))))))

(when-not (= "true" (System/getProperty "gravity.workstream.ledger.library"))
  (migrate! (parse-options *command-line-args*)))
