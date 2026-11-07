(ns simplify.recovery-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [simplify.codex :as codex]
            [simplify.core-test :as fixture]
            [simplify.github :as gh]
            [simplify.main :as main]
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
        source-parent (rt/git ctx dependency "rev-parse" "HEAD")
        source-parent-tree (rt/git ctx dependency "rev-parse" "HEAD^{tree}")
        merge-base (if (:immutable-lineage options)
                     (do (rt/git ctx dependency "checkout" "--detach" base)
                         (spit (io/file dependency "published-main.edn") "{:published-main :unchanged-import}\n")
                         (rt/git ctx dependency "add" "published-main.edn")
                         (rt/git ctx dependency "commit" "-m" "Published Main fixture")
                         (let [main (rt/git ctx dependency "rev-parse" "HEAD")]
                           (rt/git ctx dependency "push" "origin" "HEAD:main")
                           (rt/git ctx dependency "checkout" "--detach" source-parent)
                           (rt/git ctx dependency "merge" "--no-ff" "--no-edit" main)
                           main))
                     base)
        candidate (rt/git ctx dependency "rev-parse" "HEAD")
        tree (rt/git ctx dependency "rev-parse" "HEAD^{tree}")
        merge (rt/git ctx dependency "commit-tree" tree "-p" merge-base "-p" candidate "-m" "Verified dependency fixture merge")
        _ (rt/git ctx dependency "push" "origin" (str merge ":main"))
        entry {:kind :main-verified
               :intent {:id "fixture-dependency" :state-file "fixture-original-owner/state.edn"
                        :base merge-base :candidate candidate :tree tree :method :merge}
               :actual {:commit merge :main merge :tree tree :parents [merge-base candidate]}}
        _ (rt/coordinate! ctx (fn [state] [(update state :history conj entry) nil]))
        source-owner (rt/canonical (io/file (:state-dir ctx) "original-dependency-source.edn"))
        source-lease {:id "fixture-source-phase" :token "fixture-original-token"
                      :state-file source-owner :paths [dependency-path]}
        release-file (str (io/file (:state-dir ctx) "dependency-source-release.edn"))
        acceptance-file (str (io/file (:state-dir ctx) "dependency-source-acceptance.edn"))
        review-file (str (io/file (:state-dir ctx) "dependency-original-source-review.edn"))
        status-file (str (io/file (:state-dir ctx) "dependency-current-source-status.edn"))
        entry-text (rt/git ctx dependency "ls-tree" candidate "--" dependency-path)
        [_ mode blob] (re-find #"^([0-9]+) blob ([0-9a-f]{40})\t" entry-text)
        subject (cond-> {:candidate candidate :tree tree :paths [dependency-path] :owner-state-files [source-owner]
                         :entries {dependency-path {:mode mode :blob blob}}}
                  (:immutable-lineage options) (assoc :source-parent source-parent :source-parent-tree source-parent-tree))
        _ (rt/write-edn! release-file {:lease source-lease :released true :checks-ended true})
        _ (rt/write-edn! review-file {:candidate candidate :tree tree :disposition :accepted-scoped-source})
        _ (rt/write-edn! acceptance-file {:kind :source-only-admission :subject subject :disposition :accepted-source-only
                                         :nonclaims [:no-retroactive-source-ownership :no-standalone-held-source-admission
                                                     :no-release :no-migration-authority]
                                         :evidence [{:path review-file :sha256 (recovery/file-digest review-file)}]})
        _ (rt/write-edn! status-file {:kind :dependency-source-status :subject subject :known-end true
                                     :current-status {:writers {} :refusals {}}})
        source-phase (cond-> {:owner-state-file source-owner :lease-id (:id source-lease) :lease-token (:token source-lease)
                      :paths [dependency-path] :candidate candidate :tree tree
                      :writer-ended? true :no-unresolved-authority true
                      :release-evidence {:path release-file :sha256 (recovery/file-digest release-file)}
                      :acceptance-evidence {:path acceptance-file :sha256 (recovery/file-digest acceptance-file)}
                      :status-evidence {:path status-file :sha256 (recovery/file-digest status-file)}}
                       (:immutable-lineage options) (assoc :kind :accepted-immutable-lineage)
                       (:immutable-lineage options) (dissoc :release-evidence :lease-id :lease-token :owner-state-file))
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
                             :source-paths [dependency-path] :source-phase source-phase}])
                    (assoc-in [:operator-classification :kind] :verified-external-source-dependency)
                    (assoc-in [:operator-classification :phase] :source-development-current)
                    (assoc-in [:operator-classification :original-output-sha256] (rt/digest (pr-str output)))
                    (assoc-in [:operator-classification :basis] "Original operator inspected the now-merged external fix; old semantic nonpasses remain history.")
                    (assoc-in [:authored-files "work.edn" :executable] false))]
    (rt/write-edn! evidence receipt)
    {:ctx ctx :repo repo :dependency-worktree dependency :dependency-path dependency-path :evidence evidence :merge merge
     :source-phase source-phase :source-lease source-lease
     :error error :output output :receipt receipt}))

(deftest resolved-dependency-commits-retained-work-and-composes-verified-main-without-reauthoring
  (doseq [options [{} {:immutable-lineage true} {:semantic-failure true :quoted-refusal-data true
                      :dependency-path " dependency\nsource.edn"}]]
  (let [{:keys [ctx repo dependency-worktree dependency-path evidence merge error output]} (dependency-fixture! options)
        stages (atom [])
        job (get-in @(:state ctx) [:jobs "implement-1"])]
    (rt/coordinate! ctx (fn [s] [(assoc-in s [:restrictions :commands "unrelated-historical-owner"]
                                         {:state-file "another-original-source-owner.edn" :message "Historical refusal stays held"}) nil]))
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
                    :own-main-pending :watchdog-failure :xcrun-refusal :failed-refusal
                    :forged-parentage :wrong-candidate :live-dependency-writer :dependency-refusal
                    :stale-source-release :wrong-source-phase :admission-candidate :admission-scope
                    :admission-disposition :admission-blob :admission-nonclaims :status-ended :status-subject]]
    (let [{:keys [ctx repo evidence receipt source-phase source-lease]} (dependency-fixture!)
          change-reference! (fn [key f]
                                (let [position [:verified-source-dependencies 0 :source-phase key]
                                      path (:path (get-in receipt position))]
                                  (rt/write-edn! path (f (rt/read-edn path)))
                                  (rt/write-edn! evidence (assoc-in receipt (conj position :sha256) (recovery/file-digest path)))))]
      (case mutation
        :real-restriction (flow/task! ctx "authored" :error {:message "Actual denied action" :details {:restricted true}})
        :unresolved-semantics (rt/write-edn! evidence (assoc-in receipt [:operator-classification :no-unresolved-semantics] false))
        :no-dependency (rt/write-edn! evidence (assoc receipt :verified-source-dependencies []))
        :forged-history (rt/write-edn! evidence (assoc-in receipt [:verified-source-dependencies 0 :history-sha256] "forged"))
        :live-dependency-writer (rt/coordinate! ctx (fn [s] [(assoc-in s [:writers (:id source-lease)]
                                                                 (assoc source-lease :status :needs-inspection)) nil]))
        :dependency-refusal (rt/coordinate! ctx (fn [s] [(assoc-in s [:restrictions :commands "source-refusal"]
                                                              {:state-file (:owner-state-file source-phase)
                                                               :message "Original source refusal remains"}) nil]))
        :stale-source-release (spit (get-in source-phase [:release-evidence :path]) "{:changed true}\n")
        :wrong-source-phase (rt/write-edn! evidence (assoc-in receipt [:verified-source-dependencies 0 :source-phase :lease-token] "foreign-token"))
        :admission-candidate (change-reference! :acceptance-evidence #(assoc-in % [:subject :candidate] (apply str (repeat 40 "f"))))
        :admission-scope (change-reference! :acceptance-evidence #(assoc-in % [:subject :paths] []))
        :admission-disposition (change-reference! :acceptance-evidence #(assoc % :disposition :held))
        :admission-blob (change-reference! :acceptance-evidence #(assoc-in % [:subject :entries "dependency.edn" :blob] (apply str (repeat 40 "f"))))
        :admission-nonclaims (change-reference! :acceptance-evidence #(assoc % :nonclaims []))
        :status-ended (change-reference! :status-evidence #(assoc % :known-end false))
        :status-subject (change-reference! :status-evidence #(assoc-in % [:subject :tree] (apply str (repeat 40 "f"))))
        :forged-parentage (let [entry (last (:history (rt/coordinate! ctx (fn [s] [s s]))))
                               changed (assoc-in entry [:actual :parents] [(:base (flow/task ctx "authored"))])]
                           (rt/coordinate! ctx (fn [s] [(update s :history #(conj (vec (butlast %)) changed)) nil]))
                           (rt/write-edn! evidence (assoc-in receipt [:verified-source-dependencies 0 :history-sha256]
                                                            (rt/digest (pr-str changed)))))
        :wrong-candidate (let [entry (last (:history (rt/coordinate! ctx (fn [s] [s s]))))
                              changed (assoc-in entry [:intent :candidate] (get-in entry [:actual :commit]))]
                          (rt/coordinate! ctx (fn [s] [(update s :history #(conj (vec (butlast %)) changed)) nil]))
                          (rt/write-edn! evidence (-> receipt
                                                     (assoc-in [:verified-source-dependencies 0 :candidate] (get-in changed [:intent :candidate]))
                                                     (assoc-in [:verified-source-dependencies 0 :source-phase :candidate] (get-in changed [:intent :candidate]))
                                                     (assoc-in [:verified-source-dependencies 0 :history-sha256] (rt/digest (pr-str changed))))))
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

(deftest raw-path-collectors-retain-disallowed-whitespace-only-paths-for-rejection
  (let [{:keys [ctx repo evidence]} (dependency-fixture!)
        path " \n "]
    (spit (io/file repo path) "not an owned path\n")
    (is (some #{path} (flow/changed-paths ctx repo)))
    (is (some #{path} (recovery/changes ctx repo)))
    (is (thrown? Exception (flow/recover-dependency! ctx "authored" evidence)))
    (is (= :attention (:status (flow/task ctx "authored"))))))

(defn fresh-source-fixture! [& [status-read?]]
  (let [{:keys [ctx repo]} (held-fixture!)
        old-task (flow/task ctx "authored")
        base (:base old-task)
        _ (rt/git ctx repo "add" "work.edn")
        _ (rt/git ctx repo "commit" "-m" "Original committed source")
        source (rt/git ctx repo "rev-parse" "HEAD")
        source-tree (rt/git ctx repo "rev-parse" "HEAD^{tree}")
        main-worktree (str (io/file (:state-dir ctx) "published-main"))
        fresh (str (io/file (:state-dir ctx) "fresh-validation"))
        _ (rt/git ctx repo "worktree" "add" "--detach" main-worktree base)
        _ (spit (io/file main-worktree "main-only.edn") "{:main :advanced}\n")
        _ (rt/git ctx main-worktree "add" "main-only.edn")
        _ (rt/git ctx main-worktree "commit" "-m" "Fresh Main")
        main (rt/git ctx main-worktree "rev-parse" "HEAD")
        _ (rt/git ctx main-worktree "push" "origin" "HEAD:main")
        _ (rt/git ctx repo "worktree" "add" "-b" "codex/fresh-validation-fixture" fresh source)
        _ (rt/git ctx fresh "merge" "--no-ff" "--no-edit" main)
        candidate (rt/git ctx fresh "rev-parse" "HEAD") tree (rt/git ctx fresh "rev-parse" "HEAD^{tree}")
        argv ["gh" "pr" "view" "fixture-original-pr" "--json" "headRefOid,state"]
        error (if status-read?
                {:message "Command failed" :details {:argv argv :exit 1 :restricted true :err "HTTP 401"}}
                {:message "Sandboxed check failed"
                 :details {:command ["clojure" "-M" "old-macro.clj"]
                           :result {:exitCode 1 :stdout "Old semantic nonpass"} :old-cleanup :unknown}})
        original (assoc old-task :candidate source :tree source-tree :error error :pr {:number (if status-read? 913 912)})
        _ (rt/update! ctx assoc-in [:tasks "authored"] original)
        old-pr-effect {:status :done :result {:number (get-in original [:pr :number]) :html_url "fixture-original-pr"}}
        _ (rt/update! ctx assoc-in [:effects "pr/authored"] old-pr-effect)
        authorization (str (io/file (:state-dir ctx) "fresh-human-authorization.edn"))
        _ (rt/write-edn! authorization {:source :direct-human-question-reply :answer "Allow fresh A1 validation"
                                        :scope [:fresh-pr913-status-validation :fresh-pr912-whitespace-validation]})
        proof (str (io/file (:state-dir ctx) "fresh-source-peer-binding.edn"))
        validation {:base main :candidate candidate :tree tree :worktree fresh :branch "codex/fresh-validation-fixture"}
        _ (rt/write-edn! proof {:source-review {:disposition :accepted-scoped-source-only :finding-count 0
                                               :source-input source :owned-paths (:paths original)}
                               :composition {:base main :candidate candidate :candidate-tree tree :parents [source main]}})
        status (str (io/file (:state-dir ctx) "fresh-approved-status-result.edn"))
        _ (rt/write-edn! status {:authorization-sha256 (recovery/file-digest authorization) :argv argv
                                :head source :pr-number (get-in original [:pr :number]) :exit 0
                                :provider-result {:headRefOid source :state "OPEN" :baseRefName "main"}})
        evidence (str (io/file (:state-dir ctx) "fresh-source-inspection.edn"))
        receipt {:kind :fresh-source-validation :task-key "authored" :original-pr (:pr original)
                 :original-task-sha256 (rt/digest (pr-str original)) :validation validation :checks [check-command]
                 :operator-classification {:kind :fresh-source-validation :inspector :controller-operator
                                           :original-error-sha256 (recovery/error-digest original)
                                           :no-unresolved-source-semantics true :old-actions-remain-prohibited true
                                           :basis "Original operator inspected the distinct fresh candidate; old failures and unknown cleanup stay history."}
                 :authorization {:path authorization :sha256 (recovery/file-digest authorization)}
                 :authorization-scope (if status-read? :fresh-pr913-status-validation :fresh-pr912-whitespace-validation)
                 :source-acceptance {:path proof :sha256 (recovery/file-digest proof)}
                 :status-validation {:path status :sha256 (recovery/file-digest status)}
                 :preserved-nonclaims [:old-failure-is-not-a-pass :old-process-inspection-prohibited :old-cleanup-unknown]}
        _ (rt/write-edn! evidence receipt)]
    {:ctx ctx :repo repo :fresh fresh :main-worktree main-worktree :original original :evidence evidence :receipt receipt
     :old-pr-effect old-pr-effect :authorization authorization :proof proof :status-proof status}))

(deftest fresh-source-validation-keeps-original-records-and-runs-native-check-review-publication
  (doseq [status-read? [false true]]
    (let [{:keys [ctx repo fresh main-worktree original evidence old-pr-effect]} (fresh-source-fixture! status-read?)
          jobs (:jobs @(:state ctx)) original-bytes (slurp (io/file repo "work.edn")) calls (atom [])]
      (flow/recover-fresh-source! ctx "authored" evidence)
      (is (= original (get-in (flow/task ctx "authored") [:fresh-source-validation-history 0 :original-task])))
      (is (= (:error original) (:error (flow/task ctx "authored"))))
      (is (nil? (:pr (flow/task ctx "authored"))))
      (is (false? (boolean (recovery/review-permitted? (flow/task ctx "authored")))) "Fresh checks are required before review")
      ;; Native preparation, rather than the old validation receipt, owns later Main composition.
      (spit (io/file main-worktree "later.edn") "{:later-main :preserved}\n")
      (rt/git ctx main-worktree "add" "later.edn")
      (rt/git ctx main-worktree "commit" "-m" "Later unrelated Main")
      (rt/git ctx main-worktree "push" "origin" "HEAD:main")
      (with-redefs [gh/status! (fn [& _] nil) gh/comment! (fn [& _] nil) gh/draft! (fn [& _] nil)
                    gh/api (fn ([_ _] [])
                             ([_ method endpoint body]
                              (is (= "POST" method)) (is (.endsWith endpoint "/pulls"))
                              (swap! calls conj :create-new-pr)
                              {:number 2000 :node_id "fresh-pr" :html_url "fixture-fresh-pr" :draft (:draft body)}))
                    codex/command! (fn [_ _ cwd argv]
                                     (is (= fresh cwd)) (is (= check-command argv))
                                     (swap! calls conj :fresh-check) {:exitCode 0})
                    codex/job! (fn [c _ _ mode _ _ _]
                                 (is (= :read mode) "No author turn or denied old action is replayed")
                                 (swap! calls conj :fresh-review)
                                 (assoc (select-keys (flow/task c "authored") [:base :candidate :tree])
                                        :verdict "approved" :findings []))]
        (flow/prepare! ctx "authored"))
      (is (= [:fresh-check :fresh-review :create-new-pr] @calls))
      (is (= 2000 (get-in (flow/task ctx "authored") [:pr :number])))
      (is (= old-pr-effect (get-in @(:state ctx) [:effects "pr/authored"])) "Original creation effect is never reused")
      (is (= jobs (:jobs @(:state ctx))))
      (is (= original-bytes (slurp (io/file repo "work.edn")))))
    ))

(defn composition-conflict-fixture! []
  (let [{:keys [ctx repo original main-worktree]} (fresh-source-fixture!)
        _ (spit (io/file main-worktree "work.edn") "{:value :main}\n")
        _ (rt/git ctx main-worktree "add" "work.edn")
        _ (rt/git ctx main-worktree "commit" "-m" "Main changes the same owned file")
        base (rt/git ctx main-worktree "rev-parse" "HEAD")
        _ (rt/git ctx main-worktree "push" "origin" "HEAD:main")
        result (rt/command ctx repo ["git" "merge" "--no-edit" base])
        _ (is (= 1 (:exit result)))
        original (assoc original :resume-status :ci :error {:message "Command failed" :details (assoc result :argv ["git" "merge" "--no-edit" base] :restricted false)})
        _ (rt/update! ctx assoc-in [:tasks "authored"] original)
        fresh (str (io/file (:state-dir ctx) "resolved-composition"))
        branch "codex/resolved-composition"
        _ (rt/git ctx repo "worktree" "add" "-b" branch fresh (:candidate original))
        result (rt/command ctx fresh ["git" "merge" "--no-edit" base])
        _ (is (= 1 (:exit result)))
        _ (spit (io/file fresh "work.edn") "{:source 2 :main :main}\n")
        _ (rt/git ctx fresh "add" "work.edn")
        _ (rt/git ctx fresh "commit" "-m" "Preserve both changes in the owned conflict")
        validation {:base base :candidate (rt/git ctx fresh "rev-parse" "HEAD")
                    :tree (rt/git ctx fresh "rev-parse" "HEAD^{tree}") :worktree fresh :branch branch}
        conflict (recovery/composition-conflict-snapshot! ctx original)
        proof (str (io/file (:state-dir ctx) "resolution-peer.edn"))
        _ (rt/write-edn! proof {:disposition :accepted-owned-composition-resolution :findings [] :task-key "authored"
                                :source (select-keys original [:base :candidate :tree])
                                :resolution (select-keys validation [:base :candidate :tree])
                                :parents [(:candidate original) base] :conflict-paths (:conflict-paths conflict)})
        evidence (str (io/file (:state-dir ctx) "composition-evidence.edn"))
        receipt {:kind :owned-composition-conflict :task-key "authored" :original-pr (:pr original)
                 :original-task-sha256 (rt/digest (pr-str original)) :original-conflict conflict
                 :validation validation :checks [check-command]
                 :resolution-acceptance {:path proof :sha256 (recovery/file-digest proof)}}
        _ (rt/write-edn! evidence receipt)]
    {:ctx ctx :repo repo :original original :fresh fresh :proof proof :evidence evidence :receipt receipt :conflict conflict}))

(deftest owned-composition-recovery-preserves-the-conflict-and-original-pr
  (let [{:keys [ctx repo original fresh evidence conflict]} (composition-conflict-fixture!)
        jobs (:jobs @(:state ctx)) stages (atom [])
        request (flow/queue-recovery! ctx :recover-composition "authored" evidence)]
    (flow/process-recovery-requests! ctx #{"authored"})
    (is (= original (flow/task ctx "authored")) "An active native writer blocks the request")
    (flow/process-recovery-requests! ctx #{})
    (let [queued (flow/task ctx "authored")]
      (is (= :applied (get-in @(:state ctx) [:operator-recovery-requests (:request request) :status])))
      (is (= original (get-in queued [:composition-recovery-history 0 :original-task])))
      (is (= (:pr original) (:pr queued)))
      (is (= (:branch original) (:publication-branch queued)))
      (is (= (:error original) (:error queued)))
      (with-redefs [gh/status! (fn [& _] nil) gh/comment! (fn [& _] nil) gh/draft! (fn [& _] nil)
                    gh/api (fn ([_ endpoint]
                                (is (str/includes? endpoint (str "head=fixture:" (:branch original))))
                                [(assoc (:pr original) :state "open" :draft true)])
                             ([_ _ _ _] (throw (ex-info "Must not recreate the existing PR" {}))))
                    codex/command! (fn [_ _ cwd argv]
                                     (is (= fresh cwd)) (is (= check-command argv))
                                     (swap! stages conj :check) {:exitCode 0})
                    codex/job! (fn [c _ _ mode _ _ _]
                                 (is (= :read mode)) (swap! stages conj :review)
                                 (assoc (select-keys (flow/task c "authored") [:base :candidate :tree])
                                        :verdict "approved" :findings []))]
        (flow/prepare! ctx "authored"))
      (is (= [:check :review] @stages))
      (is (= :ci (:status (flow/task ctx "authored")))))
    (is (= jobs (:jobs @(:state ctx))))
    (is (= conflict (recovery/composition-conflict-snapshot! ctx original)))
    (is (= (:candidate (flow/task ctx "authored"))
           (first (str/split (rt/git ctx repo "ls-remote" "origin" (str "refs/heads/" (:branch original))) #"\s+"))))))

(deftest owned-composition-recovery-rejects-stale-refused-or-unaccepted-inputs
  (doseq [mutation [:stale-task :conflict-bytes :refusal :uncertain :foreign-resolution :wrong-proof :stale-proof :dirty-resolution :shared-api]]
    (let [{:keys [ctx repo original fresh evidence receipt proof]} (composition-conflict-fixture!)
          changed (case mutation
                    :stale-task (assoc receipt :original-task-sha256 "stale")
                    :wrong-proof (do (rt/write-edn! proof (assoc (rt/read-edn proof) :findings ["unresolved"]))
                                     (assoc-in receipt [:resolution-acceptance :sha256] (recovery/file-digest proof)))
                    receipt)]
      (case mutation
        :conflict-bytes (spit (io/file repo "work.edn") "changed after inspection")
        :refusal (rt/update! ctx assoc-in [:tasks "authored" :error :details :restricted] true)
        :uncertain (rt/update! ctx assoc-in [:effects "unknown"] {:status :uncertain})
        :stale-proof (spit proof "{:changed true}")
        :dirty-resolution (spit (io/file fresh "work.edn") "dirty")
        :foreign-resolution (do (spit (io/file fresh "foreign.edn") "foreign")
                                (rt/git ctx fresh "add" "foreign.edn")
                                (rt/git ctx fresh "commit" "--amend" "--no-edit"))
        :shared-api (rt/shared-restrict! ctx :api (rt/digest (pr-str ["POST" "graphql"])) {:message "held"})
        nil)
      (rt/write-edn! evidence changed)
      (let [before (flow/task ctx "authored")]
        (is (thrown? Exception (flow/recover-composition! ctx "authored" evidence)) (name mutation))
        (is (= before (flow/task ctx "authored")))
        (is (nil? (:composition-recovery (flow/task ctx "authored"))))))))

(deftest fresh-source-validation-rejects-unflagged-check-refusals
  (doseq [stream [:stdout :stderr]]
    (let [{:keys [ctx evidence receipt original]} (fresh-source-fixture! false)
          denied (assoc-in original [:error :details :result stream]
                           "Error building classpath. Permission denied")
          updated (-> receipt
                      (assoc :original-task-sha256 (rt/digest (pr-str denied)))
                      (assoc-in [:operator-classification :original-error-sha256]
                                (recovery/error-digest denied)))]
      (rt/update! ctx assoc-in [:tasks "authored"] denied)
      (rt/write-edn! evidence updated)
      (is (thrown? Exception (flow/recover-fresh-source! ctx "authored" evidence)))
      (is (= denied (flow/task ctx "authored")))
      (is (nil? (:fresh-source-validation (flow/task ctx "authored")))))))

(deftest fresh-source-validation-rejects-stale-or-unapproved-handoffs
  (doseq [mutation [:no-authority :wrong-scope :stale-task :stale-status :different-tree :foreign-scope :dirty :same-worktree :shared-api]]
    (let [{:keys [ctx repo fresh evidence receipt authorization proof status-proof original]} (fresh-source-fixture! true)]
      (case mutation
        :no-authority (spit authorization "{:source :model-report}\n")
        :wrong-scope (rt/write-edn! evidence (assoc receipt :authorization-scope :fresh-pr912-whitespace-validation))
        :stale-task (flow/task! ctx "authored" :error {:message "A new actual failure" :details {:restricted true}})
        :stale-status (rt/write-edn! status-proof (assoc (rt/read-edn status-proof) :head "different"))
        :different-tree (rt/write-edn! evidence (assoc-in receipt [:validation :tree] (:tree original)))
        :foreign-scope (rt/write-edn! proof (assoc-in (rt/read-edn proof) [:source-review :owned-paths] ["foreign.edn"]))
        :dirty (spit (io/file fresh "foreign.edn") "dirty\n")
        :same-worktree (rt/write-edn! evidence (assoc-in receipt [:validation :worktree] repo))
        :shared-api (rt/shared-restrict! ctx :api (rt/digest (pr-str ["POST" "graphql"])) {:message "The separate API refusal stays held"}))
      (is (thrown? Exception (flow/recover-fresh-source! ctx "authored" evidence)) (name mutation))
      (is (= :attention (:status (flow/task ctx "authored")))))))

(deftest recovery-requests-reach-the-original-live-controller-once
  (let [{:keys [ctx evidence original]} (fresh-source-fixture! true)
        before @(:state ctx) raw (slurp (:state-file ctx))]
    (rt/with-lock ctx
      (fn []
        ;; A separate CLI context cannot acquire the held state lock. Submission
        ;; only publishes a complete immutable input for its original owner.
        (let [client (assoc ctx :state (atom {:stale-client true}))]
          (main/dispatch! client "queue-recovery" ["recover-fresh-source" "authored" evidence])
          (is (= {:stale-client true} @(:state client))))
        (is (= raw (slurp (:state-file ctx))))
        (is (= before @(:state ctx)))
        (flow/process-recovery-requests! ctx #{"authored"})
        (is (= before @(:state ctx)))
        (flow/process-recovery-requests! ctx #{})
        (is (= :repair (:status (flow/task ctx "authored"))))
        (is (= original (get-in (flow/task ctx "authored") [:fresh-source-validation-history 0 :original-task])))
        (is (= [:applied] (mapv :status (vals (:operator-recovery-requests @(:state ctx))))))
        (let [after @(:state ctx)]
          (flow/process-recovery-requests! ctx #{})
          (is (= after @(:state ctx))))
        (is (= (dissoc before :tasks) (dissoc @(:state ctx) :tasks :operator-recovery-requests)))))))

(deftest recovery-requests-preserve-rejections-and-reconcile-a-completed-queue
  (doseq [completed? [false true]]
    (let [{:keys [ctx evidence original]} (fresh-source-fixture! true)
          request (flow/queue-recovery! ctx :recover-fresh-source "authored" evidence)]
      (is (= request (flow/queue-recovery! ctx :recover-fresh-source "authored" evidence)))
      (if completed?
        (flow/recover-fresh-source! ctx "authored" evidence)
        (spit evidence "{:changed true}\n"))
      (let [before @(:state ctx)]
        (flow/process-recovery-requests! ctx #{})
        (is (= (if completed? :applied :rejected)
               (get-in @(:state ctx) [:operator-recovery-requests (:request request) :status])))
        (is (= (:tasks before) (:tasks @(:state ctx))))
        (when-not completed? (is (= original (flow/task ctx "authored")))))
      (let [before @(:state ctx)]
        (flow/process-recovery-requests! ctx #{})
        (is (= before @(:state ctx)))))))

(deftest dependency-recovery-requests-use-the-same-inspected-native-phase
  (let [{:keys [ctx evidence]} (dependency-fixture! {:immutable-lineage true})]
    (flow/queue-recovery! ctx :recover-dependency "authored" evidence)
    (flow/process-recovery-requests! ctx #{})
    (is (= :repair (:status (flow/task ctx "authored")))))
  (let [{:keys [ctx evidence]} (fresh-source-fixture! true)
        request (flow/queue-recovery! ctx :recover-fresh-source "authored" evidence)
        before @(:state ctx)]
    (rt/shared-restrict! ctx :api (rt/digest (pr-str ["POST" "graphql"])) {:message "Actual held API episode"})
    (flow/process-recovery-requests! ctx #{})
    (is (= before @(:state ctx)))
    (is (nil? (get-in @(:state ctx) [:operator-recovery-requests (:request request)])))
    ;; Simulate the fixture's original owner completing its authorized
    ;; settlement. No real API request or production guard is involved.
    (rt/coordinate! ctx (fn [s] [(update-in s [:restrictions :api] dissoc (rt/digest (pr-str ["POST" "graphql"]))) nil]))
    (flow/process-recovery-requests! ctx #{})
    (is (= :repair (:status (flow/task ctx "authored"))))))

(deftest recovery-request-files-cannot-dispatch-an-unapproved-operation
  (doseq [[operation correct-id?] [[:retry true] [:recover-fresh-source false]]]
    (let [{:keys [ctx evidence]} (fresh-source-fixture! true)
        _ (flow/recover-fresh-source! ctx "authored" evidence)
        queued (flow/task ctx "authored")
        request {:operation operation :task-key "authored" :evidence-file evidence
                 :evidence-sha256 (recovery/file-digest evidence)}
        id (if correct-id? (rt/digest (pr-str request)) (apply str (repeat 64 "a")))
        directory (io/file (:state-dir ctx) "operator-recovery-requests")]
    (.mkdirs directory)
    (rt/write-edn! (str (io/file directory (str id ".edn"))) request)
    (flow/process-recovery-requests! ctx #{})
    (is (= :rejected (get-in @(:state ctx) [:operator-recovery-requests id :status])))
    (is (= queued (flow/task ctx "authored"))))))

(deftest recovery-submission-cli-preserves-the-live-native-event-stream
  (let [{:keys [ctx repo evidence]} (fresh-source-fixture! true)
        cfg (assoc (:config ctx) :worktree-root (str (io/file (:state-dir ctx) "workers")))
        ctx (merge ctx (rt/coordination (rt/common-git-directory repo) cfg))
        _ (rt/update! ctx assoc :repository "fixture/repo" :campaign "fixture")
        config-file (str (io/file (:state-dir ctx) "queue-config.edn"))
        _ (rt/write-edn! config-file cfg)
        source-root (str (.getParentFile (.getParentFile (io/file (.toURI (io/resource "simplify/main.clj"))))))
        deps (pr-str {:paths [source-root] :deps {'org.clojure/clojure {:mvn/version "1.12.0"}
                                                 'org.clojure/data.json {:mvn/version "2.5.2"}}})
        before-state (slurp (:state-file ctx)) before-events (recovery/file-digest (:log-file ctx))]
    (rt/with-lock ctx
      (fn []
        (let [result (rt/command {:log-lock (Object.)} repo
                                ["clojure" "-Sdeps" deps "-M" "-m" "simplify.main" "queue-recovery" config-file
                                 "recover-fresh-source" "authored" evidence])]
          (is (zero? (:exit result)))
          (is (= before-state (slurp (:state-file ctx))))
          (is (= before-events (recovery/file-digest (:log-file ctx)))))))))
