(ns gravity.self-hosting.sh01-worktree-preflight-test
  "Focused tests for the read-only Clojure Git worktree preflight.

  These tests use temporary repositories and never mutate the project
  repository.  The preflight namespace is loaded with a nil command-line
  binding so its script guard cannot terminate this test JVM."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files Path Paths]))

(binding [*command-line-args* nil]
  (load-file "tools/check_worktree_preflight.clj"))

(def ^:private preflight-ns (find-ns 'check-worktree-preflight))
(def ^:private run-preflight-var (ns-resolve preflight-ns 'run-preflight))
(def ^:private schema-var (ns-resolve preflight-ns 'schema))

(defn- run-preflight [repository options]
  (@run-preflight-var repository options))

(defn- command-output [directory arguments]
  (let [command (into ["git"] arguments)
        process (doto (ProcessBuilder. ^java.util.List command)
                  (.directory (.toFile directory)))
        running (.start process)
        read-stream
        (fn [stream]
          (let [result (promise)
                thread (Thread.
                        (fn []
                          (deliver result
                                   (try
                                     {:value (with-open [stream stream]
                                               (String. (.readAllBytes stream)
                                                        StandardCharsets/UTF_8))}
                                     (catch Throwable error
                                       {:error error})))))]
            (.setDaemon thread true)
            (.start thread)
            {:thread thread :result result}))
        stdout (read-stream (.getInputStream running))
        stderr (read-stream (.getErrorStream running))
        exit (.waitFor running)
        await-stream
        (fn [{:keys [thread result]}]
          (.join ^Thread thread)
          (let [{:keys [value error]} @result]
            (if error (throw error) value)))
        stdout-text (await-stream stdout)
        stderr-text (await-stream stderr)]
    (when-not (zero? exit)
      (throw (ex-info "git fixture command failed"
                      {:arguments arguments :exit exit :stderr stderr-text})))
    (str/trim stdout-text)))

(defn- git! [directory & arguments]
  (command-output directory arguments))

(defn- delete-tree! [^Path directory]
  (when (Files/exists directory (make-array java.nio.file.LinkOption 0))
    (with-open [paths (Files/walk directory (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (sort-by #(.getNameCount ^Path %)
                                     (iterator-seq (.iterator paths))))]
        (Files/deleteIfExists ^Path path)))))

(defn- with-repository [f]
  (let [directory (Files/createTempDirectory "gravity-worktree-preflight-"
                                             (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (git! directory "init" "-q" "-b" "main")
      (git! directory "config" "user.name" "Gravity Preflight Test")
      (git! directory "config" "user.email" "preflight@example.invalid")
      (spit (str (.resolve directory "tracked.txt")) "base\n")
      (git! directory "add" "tracked.txt")
      (git! directory "commit" "-qm" "base")
      (let [base (git! directory "rev-parse" "HEAD")]
        (git! directory "update-ref" "refs/remotes/origin/main" base)
        (f {:root directory :base base}))
      (finally
        (delete-tree! directory)))))

(defn- expected-options [root base-ref base-commit]
  {:base-ref base-ref
   :candidate-base base-commit
   :candidate-commit (git! root "rev-parse" "HEAD")
   :candidate-tree (git! root "rev-parse" "HEAD^{tree}")})

(defn- canonical-json [value]
  ((ns-resolve 'gravity.workstream-governance 'canonical-json) value))

(defn- text-sha [value]
  ((ns-resolve 'workstream-composition 'sha256) value))

(defn- held-fixture-record []
  (-> (slurp "tools/fixtures/workstream_governance/accepted/minimal-ledger.json")
      ((ns-resolve 'gravity.workstream-governance 'read-strict-json))
      (get "workstreams") first
      (assoc "id" "example-held" "state" "held"
             "disposition" "Synthetic historical held storage fixture; no integration authority.")
      (update "history" conj {"state" "held" "at" "2026-08-23T00:00:01Z"
                              "actor" "synthetic-held-author"
                              "reason" "Synthetic immutable held shard, not an integrated workstream."})))

(defn- write-composition-ledger! [root record migration]
  (let [id (get record "id")
        path (str "contracts/workstream-active/" id ".json")
        text (str (canonical-json record) "\n")
        held (held-fixture-record)
        held-text (str (canonical-json held) "\n")
        held-path (str "contracts/workstream-records/terminal/example-held-" (text-sha held-text) ".json")
        aggregate (text-sha (canonical-json [record held]))
        manifest {"schema_version" 2 "contract_id" "gravity/workstream-ledger-v2"
                  "governance_contract" "contracts/workstream-governance.json"
                  "active_root" "contracts/workstream-active"
                  "record_root" "contracts/workstream-records"
                  "record_count" 2 "active_count" 1 "terminal_count" 1
                  "aggregate_sha256" aggregate "migration" migration
                  "records" [{"id" id "path" path "sha256" (text-sha text)
                              "ordinal" 0 "state" (get record "state") "terminal" false
                              "invariant_family" (get record "invariant_family")
                              "dependencies" []}
                             {"id" "example-held" "path" held-path "sha256" (text-sha held-text)
                              "ordinal" 1 "state" "held" "terminal" true
                              "invariant_family" (get held "invariant_family") "dependencies" []}]}]
    (.mkdirs (.toFile (.resolve root "contracts/workstream-active")))
    (.mkdirs (.toFile (.resolve root "contracts/workstream-records/terminal")))
    (spit (str (.resolve root path)) text)
    (spit (str (.resolve root held-path)) held-text)
    (spit (str (.resolve root "contracts/workstream-ledger.json"))
          (str (canonical-json manifest) "\n"))))

(defn- with-composition [f]
  (with-repository
    (fn [{:keys [root base]}]
      (let [draft (-> (slurp "tools/fixtures/workstream_governance/accepted/minimal-ledger.json")
                      ((ns-resolve 'gravity.workstream-governance 'read-strict-json))
                      (get "workstreams") first
                      (assoc "owned_paths" ["tracked.txt"]))
            migration-source (str (canonical-json {"schema_version" 1 "contract_id" "gravity/workstream-ledger-v1"
                                                   "governance_contract" "contracts/workstream-governance.json"
                                                   "workstreams" [draft (held-fixture-record)]}) "\n")
            migration {"source_schema_version" 1 "source_contract_id" "gravity/workstream-ledger-v1"
                       "source_path" "contracts/workstream-ledger-v1.json"
                       "source_sha256" (text-sha migration-source)
                       "decoded_record_count" 2 "parity" "exact"
                       "decoded_aggregate_sha256" (text-sha (canonical-json [draft (held-fixture-record)]))}]
        (write-composition-ledger! root draft migration)
        (spit (str (.resolve root "contracts/workstream-governance.json"))
              (slurp "contracts/workstream-governance.json"))
        (spit (str (.resolve root "contracts/workstream-ledger-v1.json")) migration-source)
        (git! root "add" "contracts")
        (git! root "commit" "-qm" "immutable fixture source")
        (let [source (git! root "rev-parse" "HEAD")
              source-tree (git! root "rev-parse" "HEAD^{tree}")
              record (-> draft
                         (assoc "base_commit" base "candidate_commit" source
                                "clean_worktree" true "state" "integration-eligible")
                         (assoc "owned_paths" ["tracked.txt"]
                                "evidence" {"accepted_fixtures" ["synthetic positive"]
                                            "rejected_fixtures" ["synthetic negative"]
                                            "stable_diagnostics" ["FIXTURE"]
                                            "validation_commands" [{"command" "synthetic fixture evidence"
                                                                    "exit_code" 0 "result" "passed"}]}
                                "reviews" [{"kind" "independent" "result" "accepted"
                                            "reviewer" "synthetic-fixture-reviewer"}])
                         (update "history" into
                                 (map-indexed (fn [i state]
                                                {"state" state "at" (str "2026-08-23T00:00:0" (inc i) "Z")
                                                 "actor" "synthetic-fixture-author"
                                                 "reason" "Synthetic lifecycle fixture; no project authority."})
                                              ["frozen" "review-pending" "accepted" "integration-eligible"])))]
          (write-composition-ledger! root record migration)
          (git! root "add" "contracts")
          (git! root "commit" "-qm" "later fixture lifecycle metadata")
          (f {:root root :base base :source source :source-tree source-tree
              :record record :migration migration
              :options (assoc (expected-options root "origin/main" base)
                              :source-commit source :source-tree source-tree
                              :workstream-id "example-candidate")}))))))

(deftest governed-source-and-lifecycle-composition-bind-complete-identities
  (with-composition
    (fn [{:keys [root source options]}]
      (let [[document code] (run-preflight root options)
            manifest ((ns-resolve 'gravity.workstream-governance 'read-strict-json)
                      (slurp (str (.resolve root "contracts/workstream-ledger.json"))))]
        (is (true? (get (second (get manifest "records")) "terminal")))
        (is (zero? code) (pr-str (:diagnostics document)))
        (is (= source (get-in document [:source-composition :source-commit])))
        (is (= (:candidate-commit options) (get-in document [:source-composition :integration-commit])))
        (is (true? (get-in document [:source-composition :content-conserved])))
        (is (and (string? (get-in document [:source-composition :manifest-sha256]))
                 (re-matches #"[0-9a-f]{64}" (get-in document [:source-composition :manifest-sha256]))))))))

(deftest governed-composition-rejects-altered-content-and-tuples
  (doseq [scenario [:source :metadata-hash :metadata-authority :metadata-history
                    :metadata-index :metadata-foreign :metadata-held-shard :metadata-utf8 :base :head :tree
                    :source-commit :source-tree :partial :dirty]]
    (testing (name scenario)
      (with-composition
        (fn [{:keys [root record migration options]}]
          (case scenario
            :source (spit (str (.resolve root "tracked.txt")) "altered source\n")
            :metadata-hash (spit (str (.resolve root "contracts/workstream-active/example-candidate.json")) "{}\n")
            :metadata-authority (write-composition-ledger! root (assoc record "owned_paths" ["different.txt"]) migration)
            :metadata-history (write-composition-ledger! root (assoc-in record ["history" 0 "actor"] "changed-history") migration)
            :metadata-index (let [path (str (.resolve root "contracts/workstream-ledger.json"))
                                  manifest ((ns-resolve 'gravity.workstream-governance 'read-strict-json) (slurp path))]
                              (spit path (canonical-json (assoc-in manifest ["records" 0 "state"] "draft"))))
            :metadata-foreign (spit (str (.resolve root "contracts/unrelated.json")) "{}\n")
            :metadata-held-shard (let [held-text (str (canonical-json (held-fixture-record)) "\n")
                                       held-path (str "contracts/workstream-records/terminal/example-held-"
                                                      (text-sha held-text) ".json")]
                                   (spit (str (.resolve root held-path)) "{}\n"))
            :metadata-utf8 (Files/write (.resolve root "contracts/workstream-active/example-candidate.json")
                                        (byte-array [(unchecked-byte 255)])
                                        (make-array java.nio.file.OpenOption 0))
            :dirty (spit (str (.resolve root "tracked.txt")) "uncommitted\n")
            nil)
          (when (#{:source :metadata-hash :metadata-authority :metadata-history
                   :metadata-index :metadata-foreign :metadata-held-shard :metadata-utf8} scenario)
            (git! root "add" ".")
            (git! root "commit" "-qm" "altered composition fixture"))
          (let [current (if (#{:source :metadata-hash :metadata-authority :metadata-history
                              :metadata-index :metadata-foreign :metadata-held-shard :metadata-utf8} scenario)
                          (merge options (expected-options root "origin/main" (:candidate-base options)))
                          options)
                bad (apply str (repeat 40 "0"))
                selected (case scenario
                           :base (assoc current :candidate-base bad)
                           :head (assoc current :candidate-commit bad)
                           :tree (assoc current :candidate-tree bad)
                           :source-commit (assoc current :source-commit (:candidate-commit current)
                                                        :source-tree (:candidate-tree current))
                           :source-tree (assoc current :source-tree bad)
                           :partial (dissoc current :source-tree)
                           current)
                [document code] (run-preflight root selected)]
            (is (= 1 code) (str scenario " " (pr-str document)))
            (is (false? (get-in document [:preconditions :all-pass])))
            (is (seq (:diagnostics document)))))))))

(deftest clean-named-branch-and-identities-are-reported
  (with-repository
    (fn [{:keys [root base]}]
      (let [[document exit-code]
            (run-preflight root (expected-options root "origin/main" base))]
        (is (zero? exit-code))
        (is (= @schema-var (:schema document)))
        (is (true? (:read-only document)))
        (is (= "main" (get-in document [:repository :branch])))
        (is (= base (get-in document [:repository :head])))
        (is (= "identical" (get-in document [:reconciliation :relation])))
        (is (= "no_remerge" (get-in document [:reconciliation :recommendation])))
        (is (= "clean" (get-in document [:working-tree :state])))
        (is (true? (get-in document [:preconditions :all-pass])))))))

(deftest dirty-and-detached-integration-candidates-fail
  (with-repository
    (fn [{:keys [root base]}]
      (spit (str (.resolve root "tracked.txt")) "changed\n")
      (spit (str (.resolve root "new.txt")) "new\n")
      (let [[dirty-document dirty-code]
            (run-preflight root (expected-options root "origin/main" base))]
        (is (= 1 dirty-code))
        (is (= "dirty" (get-in dirty-document [:working-tree :state])))
        (is (= #{"tracked.txt" "new.txt"}
               (set (map :path (get-in dirty-document [:working-tree :blocked-entries])))))
        (is (some #(= "WORKTREE-DIRTY" (:code %)) (:diagnostics dirty-document))))
      (git! root "checkout" "-q" "--detach" base)
      (let [[integration-document integration-code]
            (run-preflight root (expected-options root "origin/main" base))
            [inspect-document inspect-code] (run-preflight root {:mode "inspect"})]
        (is (= 1 integration-code))
        (is (true? (get-in integration-document [:repository :detached])))
        (is (some #(= "WORKTREE-DETACHED" (:code %)) (:diagnostics integration-document)))
        (is (zero? inspect-code))
        (is (true? (get-in inspect-document [:repository :detached])))))))

(deftest output-exclusions-do-not-hide-unrelated-changes
  (with-repository
    (fn [{:keys [root base]}]
      (let [output (.resolve root "target/validation")]
        (Files/createDirectories output (make-array java.nio.file.attribute.FileAttribute 0))
        (spit (str (.resolve output "report.json")) "{}\n")
        (let [[allowed-document allowed-code]
              (run-preflight root (merge (expected-options root "origin/main" base)
                                         {:allow-output ["target/validation"]}))]
          (is (= 1 allowed-code))
          (is (= "dirty" (get-in allowed-document [:working-tree :state])))
          (is (= ["target/validation/report.json"]
                 (mapv :path (get-in allowed-document [:working-tree :allowed-entries])))))
        (spit (str (.resolve root "unrelated.txt")) "no\n")
        (let [[document exit-code]
              (run-preflight root (merge (expected-options root "origin/main" base)
                                         {:allow-output ["target/validation"]}))]
          (is (= 1 exit-code))
          (is (= ["unrelated.txt"]
                 (mapv :path (get-in document [:working-tree :blocked-entries])))))))))

(deftest output-exclusions-have-no-project-count-or-length-quota
  (with-repository
    (fn [{:keys [root base]}]
      (let [long-path (str "target/" (apply str (repeat 4097 "x")))
            paths (conj (mapv #(str "target/output-" %) (range 33)) long-path)
            options (assoc (expected-options root "origin/main" base)
                           :allow-output paths)
            git-var (ns-resolve preflight-ns 'git-result)
            real-git @git-var
            status (str (apply str (map #(str "?? " % "\u0000") paths))
                        " M tracked.txt\u0000")
            [document code]
            (with-redefs-fn
              {git-var (fn [directory arguments]
                         (if (= "status" (first arguments))
                           {:exit 0 :stdout status :stderr ""}
                           (real-git directory arguments)))}
              #(run-preflight root options))]
        (is (= 1 code))
        (is (= (vec (sort paths)) (get-in document [:working-tree :exclusions])))
        (is (= (set paths)
               (set (map :path (get-in document [:working-tree :allowed-entries])))))
        (is (= ["tracked.txt"]
               (mapv :path (get-in document [:working-tree :blocked-entries]))))
        (is (some #(= "WORKTREE-DIRTY" (:code %)) (:diagnostics document)))
        (is (not-any? #(= "WORKTREE-OUTPUT-EXCLUSION-INVALID" (:code %))
                      (:diagnostics document)))))))

(deftest output-exclusions-still-reject-invalid-paths
  (let [normalize (ns-resolve preflight-ns 'normalize-exclusion)]
    (doseq [path ["" " " "/absolute" "C:\\absolute" "../outside" "a/../b"
                  "a/./b" ".git" ".git/config" "target/*" "target/?"
                  "target/[x]" "target/\u0000file"]]
      (let [error (try (normalize path) nil
                       (catch clojure.lang.ExceptionInfo value value))]
        (is (= "WORKTREE-OUTPUT-EXCLUSION-INVALID" (-> error ex-data :code))
            (pr-str path))))
    (is (= "target/validation" (normalize "target//validation/")))
    (is (= "target/validation" (normalize "target\\validation")))))

(deftest tree-equivalent-squash-is-not-replayed
  (with-repository
    (fn [{:keys [root base]}]
      (git! root "checkout" "-q" "-b" "squashed")
      (git! root "commit" "--allow-empty" "-qm" "squash metadata")
      (let [[document exit-code]
            (run-preflight root (expected-options root "main" base))]
        (is (zero? exit-code))
        (is (= "tree_equivalent_squash" (get-in document [:reconciliation :relation])))
        (is (true? (get-in document [:reconciliation :tree-equivalent])))
        (is (= "no_remerge" (get-in document [:reconciliation :recommendation])))))))

(deftest clean-descendant-is-eligible-for-integration
  (with-repository
    (fn [{:keys [root base]}]
      (spit (str (.resolve root "descendant.txt")) "descendant\n")
      (git! root "add" "descendant.txt")
      (git! root "commit" "-qm" "descendant")
      (let [[document exit-code]
            (run-preflight root (expected-options root "origin/main" base))]
        (is (zero? exit-code))
        (is (= "descendant" (get-in document [:reconciliation :relation])))))))

(deftest divergence-is-explicit-and-blocked
  (with-repository
    (fn [{:keys [root base]}]
      (git! root "branch" "candidate" base)
      (spit (str (.resolve root "main.txt")) "main\n")
      (git! root "add" "main.txt")
      (git! root "commit" "-qm" "main advance")
      (let [main-head (git! root "rev-parse" "HEAD")]
        (git! root "checkout" "-q" "candidate")
        (spit (str (.resolve root "candidate.txt")) "candidate\n")
        (git! root "add" "candidate.txt")
        (git! root "commit" "-qm" "candidate change")
        (let [[document exit-code]
              (run-preflight root (expected-options root "main" main-head))]
          (is (= 1 exit-code))
          (is (= main-head (get-in document [:base :commit])))
          (is (= "divergent" (get-in document [:reconciliation :relation])))
          (is (false? (get-in document [:reconciliation :base-is-ancestor])))
          (is (false? (get-in document [:reconciliation :candidate-is-ancestor]))))))))

(deftest missing-base-and-expected-identities-fail-closed
  (with-repository
    (fn [{:keys [root base]}]
      (let [before (git! root "status" "--porcelain=v1")
            [missing-document missing-code]
            (run-preflight root {:base-ref "origin/missing"})
            after (git! root "status" "--porcelain=v1")]
        (is (= 1 missing-code))
        (is (= before after))
        (is (false? (get-in missing-document [:base :resolved])))
        (is (= "unavailable" (get-in missing-document [:reconciliation :relation])))
        (is (some #(= "WORKTREE-REVISION-MISSING" (:code %)) (:diagnostics missing-document))))
      (let [[document exit-code]
            (run-preflight root {:candidate-base (apply str (repeat 40 "0"))
                                 :candidate-commit base
                                 :candidate-tree (apply str (repeat 40 "f"))})
            codes (set (map :code (:diagnostics document)))]
        (is (= 1 exit-code))
        (is (contains? codes "WORKTREE-CANDIDATE-BASE-MISMATCH"))
        (is (contains? codes "WORKTREE-CANDIDATE-TREE-MISMATCH"))))))

(deftest integration-requires-all-three-expected-identities
  (with-repository
    (fn [{:keys [root base]}]
      (let [options (dissoc (expected-options root "origin/main" base)
                            :candidate-tree)
            [document exit-code] (run-preflight root options)
            codes (set (map :code (:diagnostics document)))
            [inspect-document inspect-code] (run-preflight root (assoc options :mode "inspect"))]
        (is (= 1 exit-code))
        (is (contains? codes "WORKTREE-EXPECTED-IDENTITIES-MISSING"))
        (is (false? (get-in document [:preconditions :candidate-identities-match])))
        (is (zero? inspect-code))
        (is (true? (get-in inspect-document [:preconditions :candidate-identities-match])))))))

(deftest candidate-focused-default-omits-peer-inventory
  (with-repository
    (fn [{:keys [root base]}]
      (git! root "branch" "auxiliary" base)
      (let [parent (Files/createTempDirectory "gravity-worktree-preflight-aux-"
                                              (make-array java.nio.file.attribute.FileAttribute 0))
            auxiliary (.resolve parent "auxiliary-worktree")]
        (try
          (git! root "worktree" "add" "-q" (str auxiliary) "auxiliary")
          (spit (str (.resolve auxiliary "tracked.txt")) "auxiliary dirty\n")
          (let [[document exit-code]
                (run-preflight root (expected-options root "origin/main" base))
                records (:worktrees document)
                root-path (str (.toRealPath root (make-array java.nio.file.LinkOption 0)))
                auxiliary-path (str (.toRealPath auxiliary (make-array java.nio.file.LinkOption 0)))]
            (is (zero? exit-code))
            (is (= "candidate" (:worktree-scan document)))
            (is (= {:mode "candidate"
                    :complete? false
                    :candidate-focused? true
                    :count 1}
                   (:worktree-inventory document)))
            (is (= [root-path] (mapv :path records)))
            (is (not (some #(= auxiliary-path (:path %)) records)))
            (is (= "clean" (:state (first records)))))
          (finally
            (git! root "worktree" "remove" "--force" (str auxiliary))
            (delete-tree! parent)))))))

(deftest worktree-scan-mode-is-closed
  (with-repository
    (fn [{:keys [root]}]
      (let [[document exit-code]
            (run-preflight root {:worktree-scan "peer-scan"})]
        (is (= 1 exit-code))
        (is (= "WORKTREE-SCAN-INVALID"
               (-> document :diagnostics first :code)))))))

(deftest registered-worktrees-are-inventory-only
  (with-repository
    (fn [{:keys [root base]}]
      (git! root "branch" "auxiliary" base)
      (let [parent (Files/createTempDirectory "gravity-worktree-preflight-aux-"
                                              (make-array java.nio.file.attribute.FileAttribute 0))
            auxiliary (.resolve parent "auxiliary-worktree")]
        (try
          (git! root "worktree" "add" "-q" (str auxiliary) "auxiliary")
          (spit (str (.resolve auxiliary "tracked.txt")) "auxiliary dirty\n")
          (let [[document exit-code]
                (run-preflight root (assoc (expected-options root "origin/main" base)
                                           :worktree-scan "all"))
                records (into {} (map (juxt :path identity) (:worktrees document)))]
            (is (zero? exit-code))
            (is (= "all" (:worktree-scan document)))
            (is (true? (get-in document [:worktree-inventory :complete?])))
            (is (= "clean" (get-in records [(str (.toRealPath root (make-array java.nio.file.LinkOption 0))) :state])))
            (is (= "dirty" (get-in records [(str (.toRealPath auxiliary (make-array java.nio.file.LinkOption 0))) :state])))
            (is (contains? records (str (.toRealPath auxiliary (make-array java.nio.file.LinkOption 0))))))
          (finally
            (git! root "worktree" "remove" "--force" (str auxiliary))
            (delete-tree! parent)))))))

(deftest full-inventory-has-no-project-record-count-quota
  (with-repository
    (fn [{:keys [root base]}]
      (let [options (expected-options root "origin/main" base)
            git-var (ns-resolve preflight-ns 'git-result)
            real-git @git-var
            inventory-calls (atom 0)
            inventory (apply str
                             (for [index (range 257)]
                               (str "worktree " (.resolve root (str "missing-peer-" index))
                                    "\nHEAD " base "\nbranch refs/heads/peer-" index "\n\n")))
            before (git! root "status" "--porcelain=v1")]
        (with-redefs-fn
          {git-var (fn [directory arguments]
                     (if (= ["worktree" "list" "--porcelain"] arguments)
                       (do (swap! inventory-calls inc)
                           {:exit 0 :stdout inventory :stderr ""})
                       (real-git directory arguments)))}
          (fn []
            (let [[candidate code] (run-preflight root options)]
              (is (zero? code))
              (is (zero? @inventory-calls))
              (is (= 1 (get-in candidate [:worktree-inventory :count]))))
            (let [[document code] (run-preflight root (assoc options :worktree-scan "all"))]
              (is (zero? code))
              (is (= 1 @inventory-calls))
              (is (empty? (:diagnostics document)))
              (is (= 257 (count (:worktrees document))))
              (is (= {:mode "all" :complete? true :candidate-focused? false :count 257}
                     (:worktree-inventory document)))
              (is (every? #(= "missing" (:state %)) (:worktrees document)))
              (is (true? (get-in document [:preconditions :all-pass]))))))
        (is (= before (git! root "status" "--porcelain=v1")))))))

(deftest full-inventory-git-failure-remains-incomplete
  (with-repository
    (fn [{:keys [root base]}]
      (let [options (assoc (expected-options root "origin/main" base)
                           :worktree-scan "all")
            git-var (ns-resolve preflight-ns 'git-result)
            real-git @git-var
            [document code]
            (with-redefs-fn
              {git-var (fn [directory arguments]
                         (if (= ["worktree" "list" "--porcelain"] arguments)
                           {:exit 1 :stdout "" :stderr "Git inventory failed"}
                           (real-git directory arguments)))}
              #(run-preflight root options))]
        (is (= 1 code))
        (is (false? (get-in document [:worktree-inventory :complete?])))
        (is (false? (get-in document [:preconditions :all-pass])))
        (is (some #(= "WORKTREE-GIT-COMMAND" (:code %))
                  (:diagnostics document)))))))
