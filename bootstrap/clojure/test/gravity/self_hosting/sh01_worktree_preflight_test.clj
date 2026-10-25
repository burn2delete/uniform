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

(def ^:private preflight-ns (find-ns 'gravity.worktree-preflight))
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
