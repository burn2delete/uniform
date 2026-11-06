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

(defn held-fixture! []
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
                                           :status "completed" :exitCode 0 :aggregatedOutput "{:value 2}"})
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
