(ns simplify.recovery-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [simplify.codex :as codex]
            [simplify.core-test :as fixture]
            [simplify.github :as gh]
            [simplify.recovery :as recovery]
            [simplify.runtime :as rt]
            [simplify.workflow :as flow]
            [simplify.workflow-test :as git-fixture]))

(use-fixtures :each fixture/no-github-network)

(def check-command ["clojure" "-M" "fixture-check.clj"])
(defn action! [ctx item]
  (rt/event! ctx :rpc-receive {:job "implement-1" :message {:method "item/completed" :params {:item item}}}))

(defn held-fixture! [& [options]]
  (let [initial (fixture/context)
        ctx (assoc initial :log-file (str (io/file (:state-dir initial) "events.jsonl")))
        {:keys [repo]} (git-fixture/repository! ctx)
        _ (rt/git ctx repo "checkout" "-b" "codex/recovery-fixture")
        base (rt/git ctx repo "rev-parse" "HEAD")
        tree (rt/git ctx repo "rev-parse" "HEAD^{tree}")
        ctx (assoc ctx :config {:repo-dir repo :state-dir (:state-dir ctx) :repository "fixture/repo"
                                :remote "origin" :base-branch "main" :campaign "fixture"
                                :checks [check-command] :check-executables #{"clojure"} :early-draft true})
        task {:key "authored" :title "Preserve useful authored work" :paths ["work.edn"] :dependencies []
              :base base :candidate base :tree tree :branch "codex/recovery-fixture" :worktree repo
              :status :ready :issue {:number 1}}
        output {:outcome "restricted" :summary "Implementation authored; controller-owned checks remain unrun"
                :checks [check-command]}
        _ (rt/update! ctx assoc :tasks {"authored" task})]
    (with-redefs [gh/status! (fn [& _] nil) gh/comment! (fn [& _] nil)
                  codex/job! (fn [c job cwd mode instructions prompt schema]
                               (is (= :write mode))
                               (spit (io/file (:state-dir c) (str job ".prompt.edn"))
                                     (pr-str {:instructions instructions :input prompt :schema schema}))
                               (rt/update! c assoc-in [:jobs job]
                                           {:cwd cwd :mode :write :status :completed :thread "fixture-thread" :turn "fixture-turn"
                                            :turn-terminal? true :active-items {} :prompt-sha256 (rt/digest prompt) :output output})
                               (rt/event! c :rpc-send {:job job :message {:method "initialize"}})
                               (spit (io/file cwd "work.edn") "{:value 2}\n")
                               (action! c {:type "fileChange" :id "fixture-write" :status "completed"
                                           :changes [{:path (str (io/file cwd "work.edn")) :kind {:type "update"} :diff "fixture authored change"}]})
                               (action! c {:type "commandExecution" :id "fixture-read" :command "cat work.edn"
                                           :status "completed" :exitCode 0
                                           :aggregatedOutput (if (:quoted-refusal-data options)
                                                               "{:prior-error \"HTTP 401: Bad credentials\"}"
                                                               "{:value 2}")})
                               (when (:semantic-failure options)
                                 (action! c {:type "commandExecution" :id "fixture-semantic-check"
                                             :command "clojure -M fixture-semantic-check.clj" :status "failed"
                                             :exitCode 1 :aggregatedOutput "C2-HASH pinned input is stale"}))
                               (rt/event! c :rpc-receive {:job job :message {:method "turn/completed"
                                                                           :params {:turn {:id "fixture-turn" :status "completed"}}}})
                               output)]
      (let [error (try (flow/prepare! ctx "authored") (catch Exception e e))]
        (is (= "Implementation requires attention" (.getMessage error)))
        (flow/failure! ctx "authored" error)))
    (let [t (flow/task ctx "authored")
          bytes (java.nio.file.Files/readAllBytes (.toPath (io/file repo "work.edn")))
          receipt {:kind :authored-implementation-evidence :task-key "authored"
                   :operator-classification {:kind :controller-owned-verification-unrun :inspector :controller-operator
                                             :original-output-sha256 (rt/digest (pr-str output))
                                             :no-unresolved-authority true :no-unresolved-semantics true
                                             :basis "Fixture operator inspected the original report: only controller-owned verification is unrun."}
                   :base (:base t) :candidate (:candidate t) :tree (:tree t)
                   :original-job "implement-1" :original-job-state (get-in @(:state ctx) [:jobs "implement-1"])
                   :original-task-error (:error t) :observed-changed-paths ["work.edn"]
                   :authored-files {"work.edn" {:sha256 (recovery/file-digest (io/file repo "work.edn"))
                                              :base64 (.encodeToString (java.util.Base64/getEncoder) bytes)}}}
          path (str (io/file (:state-dir ctx) "authored-evidence.edn"))]
      (rt/write-edn! path receipt)
      {:ctx ctx :repo repo :evidence path :task t :output output})))

(deftest inspected-report-reuses-authored-work-through-real-check-and-review-stages
  (let [{:keys [ctx repo evidence task output]} (held-fixture!)
        original-job (get-in @(:state ctx) [:jobs "implement-1"])
        stages (atom [])]
    (flow/recover-authored! ctx "authored" evidence)
    (is (= :repair (:status (flow/task ctx "authored"))))
    (is (= (:error task) (:error (flow/task ctx "authored"))))
    (is (= original-job (get-in @(:state ctx) [:jobs "implement-1"])))
    (with-redefs [gh/status! (fn [& _] nil)
                  flow/publish! (fn [c t]
                                  (swap! stages conj (if (:review t) :ready :draft))
                                  (flow/task! c (:key t) :status :ci))
                  codex/command! (fn [_ _ _ argv] (swap! stages conj [:check argv]) {:exitCode 0})
                  codex/job! (fn [c _ _ mode _ _ _]
                               (is (= :read mode) "Recovery never reauthors the held work")
                               (swap! stages conj :review)
                               (assoc (select-keys (flow/task c "authored") [:base :candidate :tree])
                                      :verdict "approved" :summary "Exact fixture review" :findings []))]
      (flow/prepare! ctx "authored"))
    (let [current (flow/task ctx "authored")]
      (is (= [:draft [:check check-command] :review :ready] @stages))
      (is (= :passed (get-in current [:checks :result])))
      (is (= "approved" (get-in current [:review :verdict])))
      (is (= :checked (get-in current [:inspected-authored :stage])))
      (is (= (:error task) (:error current)) "The original report is preserved, not cleared")
      (is (= output (get-in @(:state ctx) [:jobs "implement-1" :output])))
      (is (= "{:value 2}\n" (slurp (io/file repo "work.edn")))))
    (is (str/blank? (rt/git ctx repo "status" "--porcelain")))))

(deftest actual-refusals-unknown-actions-and-stale-authorship-remain-held
  (doseq [mutation [:command-refusal :approval :unfinished :unfinished-native :unknown-action :failed-command :shared-check
                    :stale-file :foreign-dirty :no-delta :wrong-job :missing-clean-proof]]
    (let [{:keys [ctx repo evidence]} (held-fixture!)]
      (case mutation
        :command-refusal (action! ctx {:type "commandExecution" :id "denied" :command "fixture denied action"
                                       :status "completed" :exitCode 0
                                       :aggregatedOutput "git: error: couldn't create cache file '/tmp/fixture' (errno=Operation not permitted)"})
        :approval (rt/event! ctx :rpc-receive {:job "implement-1" :message {:id 99 :method "item/commandExecution/requestApproval"}})
        :unfinished (rt/update! ctx assoc-in [:jobs "implement-1" :active-items] {"unknown" "commandExecution"})
        :unfinished-native (rt/event! ctx :rpc-receive {:job "implement-1" :message {:method "item/started"
                                                                                    :params {:item {:id "unfinished" :type "commandExecution"}}}})
        :unknown-action (action! ctx {:type "unrecognizedTool" :id "unknown" :status "completed"})
        :failed-command (action! ctx {:type "commandExecution" :id "failed" :command "fixture failing action"
                                     :status "completed" :exitCode 1 :aggregatedOutput "Unknown failure"})
        :shared-check (rt/record-denial! ctx :check (codex/check-key ctx check-command) {:command check-command :source :fixture})
        :stale-file (spit (io/file repo "work.edn") "{:value 3}\n")
        :foreign-dirty (spit (io/file repo "foreign.edn") "{:foreign true}\n")
        :no-delta (spit (io/file repo "work.edn") "{:value 1 :duplicate 1}\n")
        :wrong-job (rt/update! ctx assoc-in [:jobs "implement-1" :thread] "other-thread")
        :missing-clean-proof (spit (:log-file ctx) ""))
      (is (thrown? Exception (flow/recover-authored! ctx "authored" evidence)) (name mutation))
      (is (= :attention (:status (flow/task ctx "authored"))))))
  (let [{:keys [ctx evidence task]} (held-fixture!)]
    (flow/task! ctx "authored" :error {:message "Actual command refusal" :details {:restricted true}})
    (is (thrown? Exception (flow/recover-authored! ctx "authored" evidence)))
    (is (not= (:error task) (:error (flow/task ctx "authored"))))))

(deftest queued-inspection-does-not-authorize-later-edits-or-actual-check-denials
  (doseq [failure [:edit :denied-check]]
    (let [{:keys [ctx repo evidence]} (held-fixture!)
          authors (atom 0)]
      (flow/recover-authored! ctx "authored" evidence)
      (case failure
        :edit (spit (io/file repo "work.edn") "{:changed-after-inspection true}\n")
        :denied-check (rt/record-denial! ctx :check (codex/check-key ctx check-command) {:command check-command :source :fixture}))
      (with-redefs [codex/job! (fn [& _] (swap! authors inc)) gh/status! (fn [& _] nil)]
        (is (thrown? Exception (flow/prepare! ctx "authored"))))
      (is (zero? @authors)))))

(deftest a-model-report-alone-does-not-authorize-reinterpretation
  (doseq [mutation [:missing :different-output :unresolved-authority :unresolved-semantics :no-basis]]
    (let [{:keys [ctx evidence task]} (held-fixture!)
          receipt (rt/read-edn evidence)
          altered (case mutation
                    :missing (dissoc receipt :operator-classification)
                    :different-output (assoc-in receipt [:operator-classification :original-output-sha256] "different")
                    :unresolved-authority (assoc-in receipt [:operator-classification :no-unresolved-authority] false)
                    :unresolved-semantics (assoc-in receipt [:operator-classification :no-unresolved-semantics] false)
                    :no-basis (assoc-in receipt [:operator-classification :basis] ""))]
      (rt/write-edn! evidence altered)
      (is (thrown? Exception (flow/recover-authored! ctx "authored" evidence)) (name mutation))
      (is (= :attention (:status (flow/task ctx "authored"))))
      (is (= (:error task) (:error (flow/task ctx "authored")))))))

(defn dependency-fixture! [& [options]]
  (let [{:keys [ctx repo evidence output]} (held-fixture! options)
        base (rt/git ctx repo "rev-parse" "HEAD")
        dependency (str (io/file (:state-dir ctx) "dependency"))
        dependency-path (or (:dependency-path options) "dependency.edn")
        _ (rt/git ctx repo "worktree" "add" "--detach" dependency base)
        _ (spit (io/file dependency dependency-path) "{:semantic-dependency :fixed}\n")
        _ (rt/git ctx dependency "add" dependency-path)
        _ (rt/git ctx dependency "commit" "-m" "Fix external semantic dependency")
        candidate (rt/git ctx dependency "rev-parse" "HEAD")
        tree (rt/git ctx dependency "rev-parse" "HEAD^{tree}")
        merge (rt/git ctx dependency "commit-tree" tree "-p" base "-p" candidate "-m" "Verified dependency fixture merge")
        _ (rt/git ctx dependency "push" "origin" (str merge ":main"))
        entry {:kind :main-verified
               :intent {:id "fixture-dependency" :state-file "fixture-original-owner/state.edn"
                        :base base :candidate candidate :tree tree :method :merge}
               :actual {:commit merge :main merge :tree tree :parents [base candidate]}}
        _ (rt/coordinate! ctx (fn [state] [(update state :history conj entry) nil]))
        output (assoc output :outcome "needs_input" :summary "External semantic source dependency is missing")
        error {:message "Implementation requires attention" :details {:restricted false :output output :writer-ended? true}}
        _ (rt/update! ctx assoc-in [:jobs "implement-1" :output] output)
        ;; A legacy job can lack this metadata: original terminal native events
        ;; still have to prove all started actions ended.
        _ (rt/update! ctx update-in [:jobs "implement-1"] dissoc :active-items)
        _ (flow/task! ctx "authored" :error error)
        prior (rt/read-edn evidence)
        receipt (-> prior
                    (assoc :original-job-state (get-in @(:state ctx) [:jobs "implement-1"])
                           :original-task-error error
                           :preserved-failed-actions
                           (if (:semantic-failure options)
                             [{:id "fixture-semantic-check" :command "clojure -M fixture-semantic-check.clj" :exit 1
                               :output-sha256 (rt/digest "C2-HASH pinned input is stale")}]
                             [])
                           :preserved-nonclaims [:historical-nonpass-is-not-a-pass :no-migration-authority]
                           :source-provenance {"work.edn" {:sha256 (recovery/file-digest (io/file repo "work.edn"))
                                                          :basis "Retained native file action; independently inspected source bytes."}}
                           :verified-source-dependencies
                           [{:owner-state-file "fixture-original-owner/state.edn" :intent-id "fixture-dependency"
                             :commit merge :candidate candidate :tree tree :history-sha256 (rt/digest (pr-str entry))
                             :source-paths [dependency-path]}])
                    (assoc-in [:operator-classification :kind] :verified-external-source-dependency)
                    (assoc-in [:operator-classification :phase] :source-development-current)
                    (assoc-in [:operator-classification :original-output-sha256] (rt/digest (pr-str output)))
                    (assoc-in [:operator-classification :basis] "Original operator inspected the now-merged external fix; old semantic nonpasses remain history.")
                    (assoc-in [:authored-files "work.edn" :executable] false))]
    (rt/write-edn! evidence receipt)
    {:ctx ctx :repo repo :dependency-worktree dependency :dependency-path dependency-path :evidence evidence :merge merge
     :error error :output output :receipt receipt}))

(deftest resolved-dependency-commits-retained-work-and-composes-verified-main-without-reauthoring
  (doseq [options [{} {:semantic-failure true :quoted-refusal-data true
                      :dependency-path " dependency\nsource.edn"}]]
  (let [{:keys [ctx repo dependency-worktree dependency-path evidence merge error output]} (dependency-fixture! options)
        stages (atom [])
        job (get-in @(:state ctx) [:jobs "implement-1"])]
    (flow/recover-dependency! ctx "authored" evidence)
    (is (= :inspected-source-dependency-resolution (get-in (flow/task ctx "authored") [:inspected-authored :kind])))
    (is (= error (:error (flow/task ctx "authored"))))
    (is (= job (get-in @(:state ctx) [:jobs "implement-1"])))
    ;; Main may advance while the retained work awaits native preparation.
    (rt/git ctx dependency-worktree "checkout" "--detach" merge)
    (spit (io/file dependency-worktree "later.edn") "{:unrelated-main :advanced}\n")
    (rt/git ctx dependency-worktree "add" "later.edn")
    (rt/git ctx dependency-worktree "commit" "-m" "Unrelated later Main")
    (rt/git ctx dependency-worktree "push" "origin" "HEAD:main")
    (with-redefs [gh/status! (fn [& _] nil)
                  flow/publish! (fn [c t] (swap! stages conj (if (:review t) :ready :draft))
                                  (flow/task! c (:key t) :status :ci))
                  codex/command! (fn [_ _ _ argv] (swap! stages conj [:check argv]) {:exitCode 0})
                  codex/job! (fn [c _ _ mode _ _ _]
                               (is (= :read mode) "Retained work is checked and reviewed without another author turn")
                               (swap! stages conj :review)
                               (assoc (select-keys (flow/task c "authored") [:base :candidate :tree])
                                      :verdict "approved" :summary "Exact dependency composition" :findings []))]
      (flow/prepare! ctx "authored"))
    (let [t (flow/task ctx "authored")]
      (is (= [:draft [:check check-command] :review :ready] @stages))
      (is (= (rt/git ctx dependency-worktree "rev-parse" "HEAD") (:base t)))
      (is (= :passed (get-in t [:checks :result])))
      (is (= :checked (get-in t [:inspected-authored :stage])))
      (is (= error (:error t)))
      (is (= output (get-in @(:state ctx) [:jobs "implement-1" :output])))
      (is (= "{:value 2}\n" (slurp (io/file repo "work.edn"))))
      (is (= "{:semantic-dependency :fixed}\n" (slurp (io/file repo dependency-path))))
      (is (= "{:unrelated-main :advanced}\n" (slurp (io/file repo "later.edn"))))
      (is (str/blank? (rt/git ctx repo "status" "--porcelain")))))))

(deftest source-dependency-recovery-cannot-bypass-real-refusals-or-stale-evidence
  (doseq [mutation [:real-restriction :unresolved-semantics :no-dependency :forged-history :false-source
                    :stale-file :file-mode :unproven-source :unknown-failure :unfinished-action :refusal-action
                    :own-main-pending :watchdog-failure :xcrun-refusal :failed-refusal]]
    (let [{:keys [ctx repo evidence receipt]} (dependency-fixture!)]
      (case mutation
        :real-restriction (flow/task! ctx "authored" :error {:message "Actual denied action" :details {:restricted true}})
        :unresolved-semantics (rt/write-edn! evidence (assoc-in receipt [:operator-classification :no-unresolved-semantics] false))
        :no-dependency (rt/write-edn! evidence (assoc receipt :verified-source-dependencies []))
        :forged-history (rt/write-edn! evidence (assoc-in receipt [:verified-source-dependencies 0 :history-sha256] "forged"))
        :false-source (rt/write-edn! evidence (assoc-in receipt [:verified-source-dependencies 0 :source-paths] ["work.edn"]))
        :stale-file (spit (io/file repo "work.edn") "{:stale true}\n")
        :file-mode (.setExecutable (io/file repo "work.edn") true)
        :unproven-source (rt/write-edn! evidence (assoc receipt :source-provenance {}))
        :own-main-pending (rt/coordinate! ctx (fn [s] [(assoc s :main-effects {"pending" {:state-file (:state-file ctx)}}) nil]))
        :watchdog-failure (do
                            (action! ctx {:type "commandExecution" :id "watchdog" :command "fixture watchdog"
                                          :status "completed" :exitCode 125 :aggregatedOutput "watchdog failure"})
                            (rt/write-edn! evidence
                              (assoc receipt :preserved-failed-actions
                                     [{:id "watchdog" :command "fixture watchdog" :exit 125
                                       :output-sha256 (rt/digest "watchdog failure")}])) )
        :unknown-failure (action! ctx {:type "commandExecution" :id "unknown-failure" :command "unknown"
                                      :status "completed" :exitCode 1 :aggregatedOutput "Unknown failure"})
        :unfinished-action (rt/event! ctx :rpc-receive {:job "implement-1" :message {:method "item/started"
                                                                                      :params {:item {:id "pending" :type "commandExecution"}}}})
        :refusal-action (action! ctx {:type "commandExecution" :id "denied" :command "retained denied action"
                                     :status "completed" :exitCode 0
                                     :aggregatedOutput "git: error: couldn't create cache file '/tmp/fixture' (errno=Operation not permitted)"})
        :xcrun-refusal (action! ctx {:type "commandExecution" :id "denied-xcrun" :command "retained denied action"
                                    :status "completed" :exitCode 0
                                    :aggregatedOutput "xcrun: error: couldn't create cache file '/tmp/fixture' (errno=Operation not permitted)"})
        :failed-refusal (let [output "Permission denied"]
                          (action! ctx {:type "commandExecution" :id "denied-failed" :command "retained denied action"
                                        :status "completed" :exitCode 1 :aggregatedOutput output})
                          (rt/write-edn! evidence (assoc receipt :preserved-failed-actions
                                                        [{:id "denied-failed" :command "retained denied action" :exit 1
                                                          :output-sha256 (rt/digest output)}]))))
      (is (thrown? Exception (flow/recover-dependency! ctx "authored" evidence)) (name mutation))
      (is (= :attention (:status (flow/task ctx "authored")))))))
