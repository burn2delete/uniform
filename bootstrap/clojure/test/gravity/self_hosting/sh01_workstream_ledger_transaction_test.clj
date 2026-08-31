(ns gravity.self-hosting.sh01-workstream-ledger-transaction-test
  (:require [clojure.test :refer [deftest is testing]]))

(import '(java.nio.charset StandardCharsets)
        '(java.nio.file Files LinkOption Path)
        '(java.nio.file.attribute FileAttribute)
        '(java.util Arrays))

;; The command tools expose their pure transaction functions when this library
;; flag is set.  The normal command-line entry point remains unchanged.
(System/setProperty "gravity.workstream.ledger.library" "true")
(load-file "tools/publish_workstream_terminal_record.clj")
(load-file "tools/migrate_workstream_ledger.clj")

(def ^:private publisher-ns
  'gravity.tools.publish-workstream-terminal-record)
(def ^:private migration-ns
  'gravity.tools.migrate-workstream-ledger)

(defn- private-var [namespace symbol]
  (or (ns-resolve namespace symbol)
      (throw (ex-info "missing private test hook" {:namespace namespace
                                                    :symbol symbol}))))

(defn- temporary-directory []
  (Files/createTempDirectory "gravity-ledger-test-"
                             (make-array FileAttribute 0)))

(defn- delete-tree! [^Path root]
  (when (Files/exists root (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
    (let [stream (Files/walk root (into-array java.nio.file.FileVisitOption []))]
      (try
        (doseq [path (reverse (vec (iterator-seq (.iterator stream))))]
          (Files/deleteIfExists path))
        (finally (.close stream))))))

(defn- utf8-bytes [text]
  (.getBytes text StandardCharsets/UTF_8))

(deftest publisher-rejects-path-substitution-before-reading-or-writing
  (testing "an external candidate path cannot stand in for a reservation"
    (let [source (Files/createTempFile "gravity-external-candidate-" ".json"
                                       (make-array FileAttribute 0))
          manifest (Files/readAllBytes
                    (java.nio.file.Paths/get "contracts/workstream-ledger.json"
                                              (make-array String 0)))]
      (try
        (Files/write source
                     (Files/readAllBytes
                      (java.nio.file.Paths/get
                       "contracts/workstream-active/native-specialization-evidence.json"
                       (make-array String 0)))
                     (make-array java.nio.file.OpenOption 0))
        (is (thrown? Throwable
                     ((private-var publisher-ns 'publish!)
                      {:candidate (str source)
                       :manifest "contracts/workstream-ledger.json"
                       :dry-run true})))
        (is (Arrays/equals ^bytes manifest
                           ^bytes (Files/readAllBytes
                                   (java.nio.file.Paths/get
                                    "contracts/workstream-ledger.json"
                                    (make-array String 0)))))
        (finally (Files/deleteIfExists source))))))

(deftest held-is-not-a-terminal-publication-target
  (testing "governance terminal states exclude held"
    (let [terminal-states @(private-var publisher-ns 'terminal-states)]
      (is (= #{"integrated" "rejected" "superseded" "abandoned"}
             terminal-states))
      (is (not (contains? terminal-states "held"))))))

(defn- transaction-fixture []
  (let [root (temporary-directory)
        contracts (.resolve root "contracts")
        active-root (.resolve contracts "workstream-active")
        terminal-root (.resolve contracts "workstream-records/terminal")
        manifest (.resolve contracts "workstream-ledger.json")
        active (.resolve active-root "candidate.json")
        terminal (.resolve terminal-root "candidate-terminal.json")]
    (Files/createDirectories active-root (make-array FileAttribute 0))
    (Files/createDirectories terminal-root (make-array FileAttribute 0))
    (let [old-bytes (utf8-bytes "{\"old\":true}\n")
          new-bytes (utf8-bytes "{\"new\":true}\n")
          candidate-bytes (utf8-bytes "{\"candidate\":true}\n")]
      (Files/write manifest old-bytes (make-array java.nio.file.OpenOption 0))
      (Files/write active candidate-bytes (make-array java.nio.file.OpenOption 0))
      {:root root :manifest manifest :active active :terminal terminal
       :old-bytes old-bytes :new-bytes new-bytes
       :candidate-bytes candidate-bytes})))

(defn- transaction-args [fixture]
  (assoc fixture
         :manifest-path (:manifest fixture)
         :old-manifest-bytes (:old-bytes fixture)
         :active-path (:active fixture)
         :terminal-path (:terminal fixture)
         :updated-manifest-bytes (:new-bytes fixture)
         :terminal-bytes (:candidate-bytes fixture)))

(deftest failed-publication-rolls-back-all-three-paths
  (testing "a manifest swap failure removes the terminal and restores reservation"
    (let [fixture (transaction-fixture)
          root-var (private-var publisher-ns 'repository-root)
          replace-var (private-var publisher-ns 'atomic-replace!)
          real-replace @replace-var]
      (try
        (is (thrown? Throwable
                     (with-redefs-fn
                       {root-var (:root fixture)
                        replace-var
                        (fn [target content]
                          (if (= target (:manifest fixture))
                            (throw (ex-info "injected manifest failure" {}))
                            (real-replace target content)))}
                       #((private-var publisher-ns 'transaction-publish!)
                         (transaction-args fixture)))))
        (is (Arrays/equals ^bytes (:old-bytes fixture)
                           ^bytes (Files/readAllBytes (:manifest fixture))))
        (is (Files/isRegularFile (:active fixture)
                                 (make-array LinkOption 0)))
        (is (not (Files/exists (:terminal fixture)
                               (make-array LinkOption 0))))
        (finally (delete-tree! (:root fixture)))))))

(deftest repository-lock-rejects-overlapping-publisher
  (testing "a second operation cannot enter while the first owns the lock"
    (let [root (temporary-directory)
          lock (.resolve root ".workstream-ledger.lock")
          root-var (private-var publisher-ns 'repository-root)
          lock-var (private-var publisher-ns 'repository-lock)
          entered (promise)
          release (promise)
          first-result (promise)
          second-result (promise)]
      (try
        (with-redefs-fn
          {root-var root lock-var lock}
          (fn []
            (let [first-thread (doto (Thread.
                                      #(try
                                         (deliver first-result
                                                  ((private-var publisher-ns
                                                                'with-repository-lock)
                                                   (fn []
                                                     (deliver entered true)
                                                     @release
                                                     :first)))
                                         (catch Throwable exception
                                           (deliver first-result
                                                    [:error exception]))))
                                 (.setDaemon true))
                  _ (.start first-thread)
                  _ @entered
                  second-thread (doto (Thread.
                                       #(try
                                          (deliver second-result
                                                   ((private-var publisher-ns
                                                                 'with-repository-lock)
                                                    (fn [] :second)))
                                          (catch Throwable _
                                            (deliver second-result :locked))))
                                  (.setDaemon true))]
              (.start second-thread)
              (is (= :locked @second-result))
              (deliver release true)
              (.join first-thread 5000)
              (is (= :first @first-result)))))
        ;; The lock inode remains in place after release.  This prevents a
        ;; second process from locking a newly created split inode while the
        ;; first process still owns the old one.
        (is (Files/exists lock (make-array LinkOption 0)))
        (finally (delete-tree! root))))))

(deftest successful-publication-uses-atomic-terminal-and-manifest
  (testing "success leaves an immutable terminal, new manifest, and no active file"
    (let [fixture (transaction-fixture)
          root-var (private-var publisher-ns 'repository-root)]
      (try
        (is (true?
             (with-redefs-fn {root-var (:root fixture)}
               #((private-var publisher-ns 'transaction-publish!)
                 (transaction-args fixture)))))
        (is (Arrays/equals ^bytes (:new-bytes fixture)
                           ^bytes (Files/readAllBytes (:manifest fixture))))
        (is (Arrays/equals ^bytes (:candidate-bytes fixture)
                           ^bytes (Files/readAllBytes (:terminal fixture))))
        (is (not (Files/exists (:active fixture)
                               (make-array LinkOption 0))))
        (finally (delete-tree! (:root fixture)))))))

(deftest compare-and-swap-revalidation-rejects-a-changed-input
  (testing "a changed manifest snapshot cannot be committed"
    (let [path (Files/createTempFile "gravity-ledger-cas-" ".json"
                                     (make-array FileAttribute 0))
          old-bytes (utf8-bytes "{\"version\":1}\n")]
      (try
        (Files/write path old-bytes (make-array java.nio.file.OpenOption 0))
        (Files/write path (utf8-bytes "{\"version\":2}\n")
                     (make-array java.nio.file.OpenOption 0))
        (is (thrown? Throwable
                     ((private-var publisher-ns 'unchanged-snapshot!)
                      path {:bytes old-bytes} "manifest")))
        (finally (Files/deleteIfExists path))))))

(deftest recovery-preserves-an-unexpected-terminal-shard
  (testing "recovery fails closed instead of deleting another writer's bytes"
    (let [root (temporary-directory)
          transaction (.resolve root ".workstream-ledger-txn-test")
          marker-path (.resolve transaction "marker.json")
          manifest (.resolve root "manifest.json")
          active (.resolve root "active.json")
          backup (.resolve transaction "active.backup")
          terminal (.resolve root "terminal.json")
          old-bytes (utf8-bytes "{\"old\":true}\n")
          expected-bytes (utf8-bytes "{\"expected\":true}\n")
          unexpected-bytes (utf8-bytes "{\"external\":true}\n")
          hash-fn (private-var publisher-ns 'sha256-bytes)
          marker-bytes-fn (private-var publisher-ns 'marker-bytes)
          recover (private-var publisher-ns 'recover-transaction!)]
      (try
        (Files/createDirectories transaction (make-array FileAttribute 0))
        (Files/write manifest old-bytes (make-array java.nio.file.OpenOption 0))
        (Files/write backup expected-bytes (make-array java.nio.file.OpenOption 0))
        (Files/write terminal unexpected-bytes (make-array java.nio.file.OpenOption 0))
        (Files/write marker-path
                     (marker-bytes-fn
                      {"manifest_path" (str manifest)
                       "active_path" (str active)
                       "terminal_path" (str terminal)
                       "staged_terminal_path" (str terminal)
                       "old_manifest_sha256" (hash-fn old-bytes)
                       "new_manifest_sha256" (hash-fn (utf8-bytes "{\"new\":true}\n"))
                       "terminal_sha256" (hash-fn expected-bytes)
                       "phase" "terminal-published"})
                     (make-array java.nio.file.OpenOption 0))
        (is (thrown? Throwable (recover transaction)))
        (is (Arrays/equals ^bytes unexpected-bytes
                           ^bytes (Files/readAllBytes terminal)))
        (is (Arrays/equals ^bytes old-bytes
                           ^bytes (Files/readAllBytes manifest)))
        (is (Files/exists backup (make-array LinkOption 0)))
        (finally (delete-tree! root))))))

(deftest migration-refuses-existing-destinations-before-any-write
  (testing "the canonical migration is read-only once outputs already exist"
    (let [manifest (java.nio.file.Paths/get "contracts/workstream-ledger.json"
                                            (make-array String 0))
          before (Files/readAllBytes manifest)
          migrate (private-var migration-ns 'migrate!)]
      (is (thrown? Throwable
                   (migrate {:source "contracts/workstream-ledger-v1.json"
                             :manifest "contracts/workstream-ledger.json"})))
      (is (Arrays/equals ^bytes before ^bytes (Files/readAllBytes manifest))))))
