(ns gravity.tools.publish-workstream-terminal-record
  "Publish one terminal lifecycle record and close its active reservation.

  Publication is a guarded repository transaction.  The candidate must be the
  exact active reservation named by the manifest (including its bytes), the
  repository lock serializes publishers, and an unfinished transaction is
  rolled back before the next operation.  Terminal records are immutable and
  content-addressed."
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
(def ^:private terminal-states
  #{"integrated" "rejected" "superseded" "abandoned"})
(def ^:private repository-root
  (.normalize (.toAbsolutePath (Paths/get "." (make-array String 0)))))
(def ^:private repository-lock
  (.resolve repository-root ".workstream-ledger.lock"))
(def ^:private transaction-prefix ".workstream-ledger-txn-")

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
    (throw (ex-info "JSON path contains a symbolic link"
                    {:path (str path)})))
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

(defn- atomic-replace! [^Path target bytes]
  "Atomically replace TARGET with BYTES through a same-directory temporary."
  (let [parent (.getParent target)
        temporary (Files/createTempFile parent ".workstream-publish-" ".tmp"
                                        (make-array FileAttribute 0))]
    (try
      (write-bytes! temporary bytes)
      (try
        (Files/move temporary target
                    (into-array StandardCopyOption
                                [StandardCopyOption/ATOMIC_MOVE
                                 StandardCopyOption/REPLACE_EXISTING]))
        (catch java.nio.file.AtomicMoveNotSupportedException _
          (Files/move temporary target
                      (into-array StandardCopyOption
                                  [StandardCopyOption/REPLACE_EXISTING]))))
      (finally (Files/deleteIfExists temporary)))))

(defn- atomic-publish-new! [^Path staged ^Path target]
  "Publish STAGED as a new TARGET without replacing an existing path."
  (when (Files/exists target (nofollow))
    (throw (ex-info "immutable terminal destination already exists"
                    {:path (str target)})))
  (try
    (Files/move staged target
                (into-array StandardCopyOption [StandardCopyOption/ATOMIC_MOVE]))
    (catch java.nio.file.AtomicMoveNotSupportedException _
      (Files/move staged target (make-array StandardCopyOption 0)))))

(defn- write-staged! [^Path directory name bytes]
  (let [path (.resolve directory name)]
    (write-bytes! path bytes)
    path))

(defn- stage-adjacent! [^Path target bytes prefix]
  "Write a temporary file beside TARGET for a same-filesystem atomic move."
  (let [temporary (Files/createTempFile (.getParent target) prefix ".tmp"
                                        (make-array FileAttribute 0))]
    (write-bytes! temporary bytes)
    temporary))

(defn- marker-bytes [marker]
  (.getBytes (str (governance/canonical-json marker) "\n") StandardCharsets/UTF_8))

(defn- update-marker! [^Path marker-path marker]
  (atomic-replace! marker-path (marker-bytes marker)))

(defn- transaction-directories []
  (if-not (Files/exists repository-root (nofollow))
    []
    (let [stream (Files/list repository-root)]
      (try
        (->> (iterator-seq (.iterator stream))
             (filter (fn [^Path path]
                       (and (Files/isDirectory path (nofollow))
                            (str/starts-with? (str (.getFileName path))
                                              transaction-prefix))))
             vec)
        (finally (.close stream))))))

(defn- delete-transaction! [^Path directory]
  (doseq [name ["marker.json" "active.backup" "terminal.part" "manifest.part"
                "manifest.old"]]
    (Files/deleteIfExists (.resolve directory name)))
  (Files/deleteIfExists directory))

(defn- restore-active! [^Path backup ^Path active]
  (when (Files/exists backup (nofollow))
    (when (Files/exists active (nofollow))
      (throw (ex-info "cannot restore active reservation over an existing path"
                      {:path (str active)})))
    (try
      (Files/move backup active
                  (into-array StandardCopyOption [StandardCopyOption/ATOMIC_MOVE]))
      (catch java.nio.file.AtomicMoveNotSupportedException _
        (Files/move backup active (make-array StandardCopyOption 0))))))

(defn- remove-recovery-terminal! [^Path terminal expected-sha256]
  "Remove only the exact shard produced by this transaction.

  A noncommitted transaction may have crashed after its terminal rename.  If
  another writer replaced that path, recovery must preserve the data and fail
  closed rather than deleting it."
  (when (Files/exists terminal (nofollow))
    (when (symlink-component? terminal)
      (throw (ex-info "cannot recover through a symbolic-link terminal path"
                      {:path (str terminal)})))
    (let [bytes (Files/readAllBytes terminal)]
      (when-not (= expected-sha256 (sha256-bytes bytes))
        (throw (ex-info "cannot recover a terminal shard with unexpected bytes"
                        {:path (str terminal)
                         :expected expected-sha256
                         :actual (sha256-bytes bytes)})))
      (Files/deleteIfExists terminal))))

(defn- recover-transaction! [^Path directory]
  "Roll back a transaction left by a killed publisher.

  The marker is written before each irreversible step.  Recovery is
  conservative: it restores only the manifest and reservation named by that
  marker and refuses to overwrite an externally changed manifest/reservation."
  (let [marker-path (.resolve directory "marker.json")]
    (if-not (Files/exists marker-path (nofollow))
      (delete-transaction! directory)
      (let [marker (:value (read-json-file marker-path))
            phase (get marker "phase")
            manifest (path-for (get marker "manifest_path"))
            active (path-for (get marker "active_path"))
            terminal (path-for (get marker "terminal_path"))
            staged-terminal-value (get marker "staged_terminal_path")
            staged-terminal (when staged-terminal-value
                             (path-for staged-terminal-value))
            backup (.resolve directory "active.backup")
            current (when (Files/exists manifest (nofollow))
                      (read-json-file manifest))
            current-hash (when current (sha256-bytes (:bytes current)))
            old-hash (get marker "old_manifest_sha256")
            new-hash (get marker "new_manifest_sha256")]
        (when (= phase "committed")
          (delete-transaction! directory))
        (when-not (= phase "committed")
          ;; A process can die between a filesystem operation and its marker
          ;; update.  Use the bytes as the recovery CAS, not merely the phase:
          ;; either the old manifest is still present or our exact new bytes
          ;; are present and can be restored.  Any third value is an external
          ;; write and therefore fails closed.
          (cond
            (= current-hash new-hash)
            (let [old-path (.resolve directory "manifest.old")]
              (atomic-replace! manifest (Files/readAllBytes old-path)))
            (= current-hash old-hash) nil
            :else
            (throw (ex-info "cannot recover transaction after manifest CAS changed"
                            {:transaction (str directory)})))
          ;; The terminal destination did not exist before this transaction.
          ;; Still verify its exact content before removing it: a concurrent or
          ;; external writer must never lose data during recovery.
          (remove-recovery-terminal! terminal (get marker "terminal_sha256"))
          (when staged-terminal
            (Files/deleteIfExists staged-terminal))
          (restore-active! backup active)
          (delete-transaction! directory))))))

(defn- recover-transactions! []
  (doseq [directory (transaction-directories)]
    (recover-transaction! directory)))

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
        (throw (ex-info "another workstream ledger publisher holds the repository lock"
                        {:lock (str repository-lock)})))
      (try
        (thunk)
        (finally (.release held))))))

(defn- parse-options [args]
  (loop [remaining args options {:manifest "contracts/workstream-ledger.json"
                                  :dry-run false}]
    (if-let [argument (first remaining)]
      (case argument
        "--candidate" (if-let [value (second remaining)]
                        (recur (nnext remaining) (assoc options :candidate value))
                        (throw (ex-info "--candidate requires a path" {})))
        "--manifest" (if-let [value (second remaining)]
                       (recur (nnext remaining) (assoc options :manifest value))
                       (throw (ex-info "--manifest requires a path" {})))
        "--dry-run" (recur (next remaining) (assoc options :dry-run true))
        (throw (ex-info (str "unknown argument: " argument) {})))
      (do
        (when-not (:candidate options)
          (throw (ex-info "--candidate is required" {})))
        options))))

(defn- read-current-records [manifest]
  (->> (get manifest "records")
       (map (fn [entry]
              {:ordinal (get entry "ordinal")
               :value (:value (read-json-file (path-for (get entry "path"))))}))
       (sort-by :ordinal)
       (mapv :value)))

(defn- manifest-bytes [manifest]
  (.getBytes (str (governance/canonical-json manifest) "\n")
             StandardCharsets/UTF_8))

(defn- unchanged-snapshot! [^Path path expected label]
  (let [actual (read-json-file path)]
    (when-not (Arrays/equals ^bytes (:bytes expected) ^bytes (:bytes actual))
      (throw (ex-info (str label " changed during publication")
                      {:path (str path) :diagnostic "WG013"})))
    actual))

(defn- transaction-publish!
  [{:keys [^Path manifest-path old-manifest-bytes ^Path active-path
           ^Path terminal-path updated-manifest-bytes terminal-bytes]}]
  (let [directory (Files/createTempDirectory repository-root transaction-prefix
                                              (make-array FileAttribute 0))
        marker-path (.resolve directory "marker.json")
        _old-path (write-staged! directory "manifest.old" old-manifest-bytes)
        staged-terminal (stage-adjacent! terminal-path terminal-bytes
                                          ".workstream-terminal-")
        base-marker {"manifest_path" (str manifest-path)
                     "active_path" (str active-path)
                     "terminal_path" (str terminal-path)
                     "staged_terminal_path" (str staged-terminal)
                     "new_manifest_sha256" (sha256-bytes updated-manifest-bytes)
                     "old_manifest_sha256" (sha256-bytes old-manifest-bytes)
                     "terminal_sha256" (sha256-bytes terminal-bytes)
                     "phase" "prepared"}]
    ;; Marker creation is itself atomic.  A missing marker means the staged
    ;; directory is safe to discard during recovery.
    (atomic-replace! marker-path (marker-bytes base-marker))
    (let [active-moved? (atom false)
          terminal-published? (atom false)
          manifest-published? (atom false)
          committed? (atom false)
          backup (.resolve directory "active.backup")]
      (try
        (try
          (Files/move active-path backup
                      (into-array StandardCopyOption [StandardCopyOption/ATOMIC_MOVE]))
          (catch java.nio.file.AtomicMoveNotSupportedException _
            (Files/move active-path backup (make-array StandardCopyOption 0))))
        (reset! active-moved? true)
        (update-marker! marker-path (assoc base-marker "phase" "active-moved"))
        (atomic-publish-new! staged-terminal terminal-path)
        (reset! terminal-published? true)
        (update-marker! marker-path (assoc base-marker "phase" "terminal-published"))
        ;; atomic-replace! stages in the manifest's own parent, so the swap is
        ;; atomic even when the transaction directory is on another mount.
        (atomic-replace! manifest-path updated-manifest-bytes)
        (reset! manifest-published? true)
        (update-marker! marker-path (assoc base-marker "phase" "manifest-published"))
        ;; Mark committed before removing the backup.  If the process dies
        ;; during cleanup, recovery can safely discard the backup and retain
        ;; the already-atomically-published manifest and terminal.
        (update-marker! marker-path (assoc base-marker "phase" "committed"))
        (reset! committed? true)
        ;; The reservation is now absent from the manifest and can be removed
        ;; without ever exposing a manifest that points at a missing terminal.
        (Files/deleteIfExists backup)
        (delete-transaction! directory)
        true
        (catch Throwable exception
          (when @committed?
            ;; Publication itself succeeded.  Cleanup artifacts are harmless
            ;; and can be recovered by the next invocation; do not roll back a
            ;; committed terminal record.
            (throw exception))
          ;; Roll back in reverse order.  Every restore is conditional on the
          ;; exact transaction state, so no unrelated path is overwritten.
          (when @manifest-published?
            (atomic-replace! manifest-path old-manifest-bytes))
          (when @terminal-published?
            (Files/deleteIfExists terminal-path))
          (Files/deleteIfExists staged-terminal)
          (when @active-moved?
            (restore-active! backup active-path))
          (delete-transaction! directory)
          (throw exception))))))

(defn publish! [{:keys [candidate manifest dry-run]}]
  (with-repository-lock
    (fn []
      (recover-transactions!)
      (let [manifest-path (path-for manifest)
            manifest-snapshot (read-json-file manifest-path)
            manifest-value (:value manifest-snapshot)
            validation (governance/validate-documents
                        "contracts/workstream-governance.json" manifest)]
        (when (seq validation)
          (throw (ex-info "current sharded ledger is not valid"
                          {:diagnostics validation})))
        (let [candidate-path (path-for candidate)
              active-entry (first
                            (filter (fn [entry]
                                      (and (not (true? (get entry "terminal")))
                                           (= candidate-path
                                              (path-for (get entry "path")))))
                                    (get manifest-value "records")))]
          (when-not active-entry
            (throw (ex-info "candidate path has no active reservation"
                            {:candidate candidate :diagnostic "WG013"})))
          (let [active-relative (get active-entry "path")
                active-path (path-for active-relative)]
            ;; Require the caller to name the manifest's canonical relative
            ;; reservation path.  This rejects absolute aliases, traversal,
            ;; symlinks, and substituted files before parsing candidate bytes.
            (when-not (and (= candidate active-relative)
                           (= candidate-path active-path)
                           (= active-relative
                              (str "contracts/workstream-active/"
                                   (get active-entry "id") ".json")))
              (throw (ex-info "candidate path must equal its active reservation"
                              {:candidate candidate
                               :active active-relative
                               :diagnostic "WG013"})))
            (let [candidate-snapshot (read-json-file candidate-path)
                  candidate-value (:value candidate-snapshot)
                  candidate-bytes (:bytes candidate-snapshot)
                  candidate-id (get candidate-value "id")
                  target-state (get candidate-value "state")]
              (when-not (= (get active-entry "sha256")
                           (sha256-bytes candidate-bytes))
                (throw (ex-info "candidate bytes do not match active reservation hash"
                                {:id candidate-id :diagnostic "WG013"})))
              (when-not (and (map? candidate-value)
                             (string? candidate-id)
                             (re-matches #"[a-z0-9][a-z0-9./-]*" candidate-id)
                             (= (get active-entry "id") candidate-id))
                (throw (ex-info "candidate must be a stable workstream record" {})))
              (when-not (contains? terminal-states target-state)
                (throw (ex-info "candidate state is not a governed terminal state"
                                {:state target-state})))
              (when-not (= (get active-entry "invariant_family")
                           (get candidate-value "invariant_family"))
                (throw (ex-info "candidate cannot change its invariant family while closing" {})))
              (when-not (= (get active-entry "dependencies")
                           (get candidate-value "dependencies"))
                (throw (ex-info "candidate cannot change dependencies while closing" {})))
              (let [existing-records (read-current-records manifest-value)
                    replacement (mapv #(if (= candidate-id (get % "id"))
                                        candidate-value
                                        %)
                                      existing-records)
                    aggregate {"schema_version" 1
                               "contract_id" "gravity/workstream-ledger-v1"
                               "governance_contract" "contracts/workstream-governance.json"
                               "workstreams" replacement}
                    semantic-errors (governance/validate-ledger aggregate)]
                (when (seq semantic-errors)
                  (throw (ex-info "terminal candidate fails lifecycle governance"
                                  {:diagnostics semantic-errors})))
                (let [digest (sha256-bytes candidate-bytes)
                      terminal-relative (str "contracts/workstream-records/terminal/"
                                             candidate-id "-" digest ".json")
                      terminal-path (path-for terminal-relative)
                      updated-entries
                      (->> (get manifest-value "records")
                           (map (fn [entry]
                                  (if (= candidate-id (get entry "id"))
                                    (assoc entry "path" terminal-relative
                                                 "sha256" digest
                                                 "state" target-state
                                                 "terminal" true)
                                    entry)))
                           (sort-by #(get % "id"))
                           vec)
                      updated-manifest
                      (assoc manifest-value
                             "records" updated-entries
                             "aggregate_sha256"
                             (sha256-bytes (.getBytes
                                            (governance/canonical-json replacement)
                                            StandardCharsets/UTF_8))
                             "terminal_count"
                             (count (filter #(true? (get % "terminal")) updated-entries))
                             "active_count"
                             (count (remove #(true? (get % "terminal")) updated-entries)))
                      updated-manifest-bytes (manifest-bytes updated-manifest)]
                  (when (Files/exists terminal-path (nofollow))
                    (throw (ex-info "content-addressed terminal destination already exists"
                                    {:path terminal-relative :diagnostic "WG013"})))
                  ;; Re-read both inputs immediately before staging.  This is
                  ;; the compare-and-swap guard against external writers and
                  ;; also closes the candidate TOCTOU window.
                  (unchanged-snapshot! manifest-path manifest-snapshot "manifest")
                  (unchanged-snapshot! active-path candidate-snapshot "candidate")
                  (println (str (if dry-run "would publish " "publishing ") candidate-id
                                " as " target-state " at " terminal-relative))
                  (when-not dry-run
                    (transaction-publish!
                     {:manifest-path manifest-path
                      :old-manifest-bytes (:bytes manifest-snapshot)
                      :active-path active-path
                      :terminal-path terminal-path
                      :updated-manifest-bytes updated-manifest-bytes
                      :terminal-bytes candidate-bytes}))
                  updated-manifest)))))))))

(when-not (= "true" (System/getProperty "gravity.workstream.ledger.library"))
  (publish! (parse-options *command-line-args*)))
