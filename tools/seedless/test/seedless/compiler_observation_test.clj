(ns seedless.compiler-observation-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [gravity.compiler-probe :as probe]
            [seedless.compiler-observation :as compiler]
            [seedless.github :as gh]
            [seedless.workflow :as w]
            [seedless.workflow-test :as fixture]
            [simplify.codex :as codex]
            [simplify.runtime :as rt]
            [simplify.workflow :as shared]))

(use-fixtures :each (fn [test] (with-redefs [rt/event! (fn [& _]) probe/input-paths ["module.clj"]] (test))))

(defn context []
  (assoc-in (fixture/repository! (fixture/context))
            [:config :compiler-observation-command] compiler/command))

(def failure-key "compiler/SH-07/current-source-call")

(defn observation [ctx key]
  (let [cwd (get-in ctx [:config :repo-dir])
        result {:source-revision (rt/git ctx cwd "rev-parse" "HEAD")
                :inputs [{:path "module.clj" :sha256 (compiler/input-sha256 cwd "module.clj")}]
                :route {:command compiler/command :target :llvm-x86_64-linux
                        :triple "x86_64-unknown-linux-gnu" :stage :current-source-canonical-core
                        :closure [{:component-id "compiler" :path "module.clj" :dependencies ["required-runtime"]}
                                  {:component-id "required-runtime" :path "language/runtime"}]}
                :completed [] :position {:component 0 :stage :current-frontend}
                :status :actionable-failure
                :failure {:id "C6-CALL" :kind :implementation :source "module.clj"
                          :producer "current core lowering" :consumer "compiler source consumer"
                          :owner {:milestone "SH-07" :task "specs/bootstrap/tasks.md#SH-07"
                                  :paths ["module.clj"]}
                          :prerequisites [{:milestone "SH-07" :task "specs/bootstrap/tasks.md#SH-07-required-input"
                                           :paths ["module.clj"]}
                                          {:milestone "SH-19" :task "SH-19.1" :paths ["runtime.clj"]}
                                          {:milestone "SH-19" :task "SH-19.2" :paths ["runtime.clj"]}]
                          :work-key key :reason "Required call form is rejected by its implementation"}
                :terminal {:status :failed :exit 1}}]
    (assoc result :input-id (compiler/input-id result))))

(defn plan [slices]
  {:coverage ["required compiler source"] :source-closure ["model navigation"]
   :target "selected Linux LLVM" :subset "current-source prerequisite"
   :summary "Repair the observed source failure" :blockers [] :candidate-dispositions [] :slices slices})

(defn repair []
  (assoc (fixture/slice "model-proposal-name") :kind "implementation"
         :compiler-failure failure-key :compiler-role "repair"))

(defn recorded-envelope-fixture [ctx]
  ;; The real historical SCC/diagnostic witness is retained. Only source/input
  ;; transport identities are rebound to this small controller fixture; this is
  ;; not a compilation or a current acceptance claim for the recorded module.
  (let [cwd (get-in ctx [:config :repo-dir]) file (io/file cwd compiler/envelope-source)
        _ (io/make-parents file)
        _ (spit file "selector input identity fixture\n")
        recorded (:observation (edn/read-string (slurp (io/resource "fixtures/compiler-envelope-mutual.edn"))))
        observation (-> recorded
                        (assoc :source-revision (rt/git ctx cwd "rev-parse" "HEAD")
                               :inputs (compiler/declared-inputs cwd))
                        (assoc-in [:subject :source-sha256] (compiler/input-sha256 cwd compiler/envelope-source)))]
    (assoc observation :input-id (compiler/input-id observation))))

(defn execute-result! [result]
  (let [response {:exitCode (get-in result [:terminal :exit]) :stdout (pr-str result) :stderr ""
                  :capture-truncated false}]
    (if (zero? (:exitCode response)) response
        (throw (ex-info "Sandboxed check failed" {:result response :writer-ended? true :restricted false})))))

(defn with-planner [ctx observed response prs operation]
  (let [cwd (get-in ctx [:config :repo-dir]) base (rt/git ctx cwd "rev-parse" "HEAD")]
    (with-redefs [w/fresh-main! (constantly base)
                  w/worktree! (fn [& _] {:worktree cwd :branch "main"})
                  gh/pages (fn [_ endpoint]
                             (if (str/includes? endpoint "/files") [{:filename "module.clj"}]
                                 prs))
                  gh/issue! (fn [& _] {:number 1 :id 1})
                  gh/milestone! (fn [& _] {:number 7})
                  gh/relation! (fn [& _]) gh/status! (fn [& _]) gh/comment! (fn [& _])
                  w/campaign-issue! (fn [& _] {:number 100 :id 100})
                  w/job! (fn [& args] (response (nth args 3)))
                  codex/command! (fn [_ _ _ argv]
                                   (is (= compiler/command argv))
                                   (execute-result! (observed)))]
      (operation))))

(deftest actual-plan-boundary-binds-the-first-failure-and-stable-work
  (let [ctx (context)]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (fn [prompt]
          (is (str/includes? prompt failure-key))
          (plan [(repair)])) []
        #(let [result (w/plan! ctx) selected (first (:slices result))]
           (is (= failure-key (:key selected)))
           (is (= "specs/bootstrap/tasks.md#SH-07" (:compiler-task selected)))
           (is (= :ready (:status (w/task ctx failure-key))))
           (is (= 2 (count (:source-closure result))))
           (is (= :failed (get-in selected [:compiler-observation :terminal :status])))
           (is (= 1 (get-in selected [:compiler-observation :terminal :exit])))
           (is (w/eligible? ctx (w/task ctx failure-key) []))
           (is (not (w/eligible? ctx (w/task ctx failure-key)
                                 [{:status :implementing :paths ["module.clj"]}])))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest recorded-mutual-recursion-needs-its-complete-current-subject-witness
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir]) raw (recorded-envelope-fixture ctx)]
    (try
      (let [selected (compiler/classify-observation cwd raw)]
        (is (= :actionable-failure (:status selected)))
        (is (= compiler/envelope-recursion-key (get-in selected [:failure :work-key])))
        (is (= "SH-08.2" (get-in selected [:failure :owner :task])))
        (is (= "SH-02.3" (get-in selected [:failure :prerequisites 0 :task])))
        (is (= 3 (count (get-in selected [:failure :witness :mutual-components]))))
        (is (= :self-recursive (get-in raw [:failure :diagnostic :diagnostic :recursive-component :kind])))
        (is (= (get-in raw [:failure :diagnostic]) (get-in selected [:failure :diagnostic]))))
      (doseq [invalid [(assoc-in raw [:subject :source-sha256] (apply str (repeat 64 "0")))
                       (assoc-in raw [:subject :core-artifact-id] nil)
                       (assoc-in raw [:subject :core-verification] :failed)
                       (assoc-in raw [:subject :functions] [])
                       (assoc-in raw [:subject :recursion-components] [])
                       (update-in raw [:subject :recursion-components]
                                  (fn [components] (mapv #(assoc % :function-core-node-ids ["unknown"]) components)))
                       (assoc-in raw [:failure :diagnostic :reason] :another-cause)
                       (assoc-in raw [:failure :id] "P18T04007")
                       (assoc-in raw [:failure :kind] :environment)
                       (assoc-in raw [:failure :source] "different.gravity")
                       (assoc-in raw [:terminal :status] :timeout)
                       (assoc-in raw [:route :closure] [(first (get-in raw [:route :closure]))])]]
        (is (= :blocked (:status (compiler/classify-observation cwd invalid)))))
      (spit (io/file cwd compiler/envelope-source) "changed source bytes\n")
      (is (= :blocked (:status (compiler/classify-observation cwd raw))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest ordinary-planning-reinterprets-raw-current-evidence-without-reexecuting
  (let [ctx (context) executions (atom 0)]
    (try
      (with-redefs [probe/input-paths ["module.clj" compiler/envelope-source]]
        (let [raw (recorded-envelope-fixture ctx)
              prerequisite (assoc (repair) :compiler-failure compiler/envelope-recursion-key
                                  :compiler-role "prerequisite" :compiler-task "SH-02.3" :stage "SH-02"
                                  :paths [compiler/envelope-source] :uniform-paths [compiler/envelope-source])]
          (with-planner ctx #(do (swap! executions inc) raw) (constantly (plan [prerequisite])) []
            #(do
               (with-redefs [compiler/classify-observation (fn [_ observation] observation)]
                 (is (empty? (:slices (w/plan! ctx)))))
               (let [effect (get-in @(:state ctx) [:compiler-observation :observation-effect])
                     selected (first (:slices (w/plan! ctx)))]
                 (is (= "SH-02.3" (:compiler-task selected)))
                 (is (= compiler/envelope-recursion-key (:compiler-failure selected)))
                 (is (w/eligible? ctx (w/task ctx (:key selected)) []))
                 (is (= 1 @executions))
                 (is (= :blocked (get-in @(:state ctx) [:effects effect :result :status])))
                 (is (= :actionable-failure (get-in @(:state ctx) [:compiler-observation :status])))
                 (is (= (pr-str raw) (get-in @(:state ctx) [:compiler-observation :execution :stdout]))))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest the-actual-retained-prerequisite-keeps-its-original-task-key-and-ownership
  (let [ctx (context)
        recorded (:selected-prerequisite (edn/read-string (slurp (io/resource "fixtures/compiler-envelope-mutual.edn"))))]
    (try
      (with-redefs [probe/input-paths ["module.clj" compiler/envelope-source]]
        (let [raw (recorded-envelope-fixture ctx)
              interpreted (compiler/classify-observation (get-in ctx [:config :repo-dir]) raw)
              retained (merge (fixture/slice (:key recorded)) recorded)]
          (is (compiler/selected? retained interpreted))
          (fixture/add-task! ctx (assoc retained :compiler-observation interpreted))
          (with-planner ctx (constantly raw) (constantly (plan [retained])) []
            #(with-redefs [gh/issue! (fn [& _] (throw (ex-info "The retained issue must be preserved" {})))]
               (let [selected (first (:slices (w/plan! ctx)))]
                 (is (= (:key recorded) (:key selected)))
                 (is (w/eligible? ctx (w/task ctx (:key selected)) []))
                 (is (= 1 (count (w/tasks ctx)))))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest only-the-observed-jvm-perfdata-lock-preamble-is-framed-as-a-warning
  (let [warning "[0.001s][warning][perf,memops] Cannot use file /tmp/hsperfdata_agent/6 because it is locked by another process (errno = 11)\n"]
    (doseq [scenario [:perfdata :other-warning :other-errno :trailing-warning :extra-value :truncated :resource-error]]
      (let [ctx (context) planned (atom 0) captured (atom nil)]
        (try
          (with-planner ctx #(observation ctx failure-key)
            (fn [_] (swap! planned inc) (plan [(repair)])) []
            #(with-redefs [codex/command!
                           (fn [& _]
                             (let [edn (pr-str (observation ctx failure-key))
                                   result {:exitCode (if (= :resource-error scenario) 137 1)
                                           :capture-truncated (= :truncated scenario) :stderr ""
                                           :stdout (case scenario
                                                     :perfdata (str warning edn)
                                                     :other-warning (str "[warning] unknown JVM condition\n" edn)
                                                     :other-errno (str (str/replace warning "errno = 11" "errno = 13") edn)
                                                     :trailing-warning (str edn "\n" warning)
                                                     :extra-value (str warning edn "\n{}")
                                                     :truncated (str warning edn)
                                                     :resource-error "java.lang.OutOfMemoryError: Java heap space\n")}]
                               (reset! captured result)
                               (throw (ex-info "Sandboxed command failed" {:result result :writer-ended? true}))))]
               (let [result (w/plan! ctx) observed (:compiler-observation @(:state ctx))]
                 (is (= @captured (:execution observed)))
                 (if (= scenario :perfdata)
                   (do (is (= [warning] (:execution-warnings observed)))
                       (is (= failure-key (:key (first (:slices result)))))
                       (is (= 1 @planned)))
                   (do (is (= :blocked (:status observed)))
                       (is (empty? (:slices result)))
                       (is (zero? @planned)))))))
          (finally (fixture/delete-tree! (:root ctx))))))))

(deftest existing-and-restricted-candidates-are-waited-for-not-renamed
  (doseq [source [:open-pr :retained-restriction]]
    (let [ctx (context) prs (if (= source :open-pr)
                              [{:number 42 :title "Existing call repair"
                                :body (str "<!-- uniform-seedless:" failure-key " -->")}] [])]
      (try
        (when (= source :retained-restriction)
          (fixture/add-task! ctx (assoc (repair) :key failure-key))
          (w/task! ctx failure-key :status :attention :error {:details {:restricted true}}))
        (with-planner ctx #(observation ctx failure-key) (constantly (plan [])) prs
          #(let [result (w/plan! ctx)]
             (is (empty? (:slices result)))
             (is (some (fn [blocker] (str/includes? blocker "existing compiler candidate")) (:blockers result)))
             (is (= (if (= source :open-pr) 0 1) (count (w/tasks ctx))))))
        (with-planner ctx #(observation ctx failure-key) (constantly (plan [(repair)])) prs
          #(is (thrown-with-msg? Exception #"existing candidate" (w/plan! ctx))))
        (when (= source :retained-restriction)
          (is (not (w/eligible? ctx (w/task ctx failure-key) [])))
          (is (true? (get-in (w/task ctx failure-key) [:error :details :restricted]))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest source-change-during-selection-invalidates-evidence-before-admission
  (let [ctx (context)]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (fn [_]
          (spit (io/file (get-in ctx [:config :repo-dir]) "module.clj") "(ns changed)\n")
          (plan [(repair)])) []
        #(is (thrown-with-msg? Exception #"changed before task admission" (w/plan! ctx))))
      (is (empty? (w/tasks ctx)))
      (is (= :planning-validation-error (get-in @(:state ctx) [:planning :validation-error :kind])))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest immutable-overlapping-candidates-permit-inspected-successors-but-not-conflicting-writers
  (let [ctx (context) prs [{:number 42 :title "Retained separate Core behavior" :draft true}]
        disposition {:candidate "pr/42" :disposition "independent-successor"
                     :reason "The immutable PR changes Core provenance; this increment repairs required call evaluation."}]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (constantly (assoc (plan [(repair)]) :candidate-dispositions [disposition])) prs
        #(let [selected (first (:slices (w/plan! ctx)))
               other (rt/claim-paths! ctx "actual-other-writer" ["module.clj"] :implementing)]
           (is (= [disposition] (:compiler-candidates selected)))
           (is (w/eligible? ctx (w/task ctx failure-key) []))
           (is (nil? (rt/claim-paths! ctx failure-key ["module.clj"] :implementing)))
           (rt/release-paths! ctx other)
           (let [lease (rt/claim-paths! ctx failure-key ["module.clj"] :implementing)]
             (is (some? lease))
             (rt/release-paths! ctx lease))))
      (finally (fixture/delete-tree! (:root ctx)))))
  (let [ctx (context)]
    (try
      (with-planner ctx #(observation ctx failure-key) (constantly (plan [(repair)]))
        [{:number 42 :title "Uninspected overlapping candidate"}]
        #(is (thrown-with-msg? Exception #"explicit independent-successor disposition" (w/plan! ctx))))
      (is (empty? (w/tasks ctx)))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest an-upgraded-controller-does-not-author-an-unlinked-old-ready-queue
  (let [ctx (context)]
    (try
      (fixture/add-task! ctx (fixture/slice "retained-unstarted"))
      (is (not (w/eligible? ctx (w/task ctx "retained-unstarted") [])))
      (is (= :ready (:status (w/task ctx "retained-unstarted"))))
      (w/task! ctx "retained-unstarted" :candidate "existing-authored-head")
      (is (w/eligible? ctx (w/task ctx "retained-unstarted") []))
      (is (not (w/eligible? ctx (w/task ctx "retained-unstarted")
                            [{:status :implementing :paths ["module.clj"]}])))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest an-existing-unstarted-task-can-be-bound-without-replacing-its-issue-or-scope
  (let [ctx (context) old-key "retained-required-call"]
    (try
      (fixture/add-task! ctx (fixture/slice old-key))
      (let [before (w/task ctx old-key)]
        (with-planner ctx #(observation ctx failure-key)
          (constantly (plan [(assoc (repair) :key old-key)])) []
          #(with-redefs [gh/issue! (fn [& _] (throw (ex-info "Must reuse the existing issue" {})))]
             (is (= old-key (:key (first (:slices (w/plan! ctx))))))))
        (let [after (w/task ctx old-key)]
          (is (= (select-keys before [:issue :paths :dependencies :proposal :title :status])
                 (select-keys after [:issue :paths :dependencies :proposal :title :status])))
          (is (= failure-key (:compiler-failure after)))
          (is (w/eligible? ctx after []))
          (is (= 1 (count (w/tasks ctx))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest distinct-legacy-task-keys-cannot-admit-the-same-observed-repair-twice
  (let [ctx (context) keys ["retained-first" "retained-second"]]
    (try
      (doseq [key keys] (fixture/add-task! ctx (fixture/slice key)))
      (with-planner ctx #(observation ctx failure-key)
        (constantly
         (assoc (plan (mapv #(assoc (repair) :key %) keys))
                :candidate-dispositions
                (mapv (fn [key] {:candidate (str "slice/" key)
                                 :disposition "independent-successor"
                                 :reason "The planner proposes a separate scope despite the same repair identity."}) keys))) []
        #(is (thrown-with-msg? Exception #"same observed compiler repair" (w/plan! ctx))))
      (is (every? #(nil? (:compiler-observation (w/task ctx %))) keys))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest interrupted-admission-resumes-the-same-bound-selection
  (let [ctx (context) proposals (atom 0) issues (atom 0) relations (atom 0) statuses (atom [])
        prerequisite (assoc (repair) :key "prerequisite-proposal" :compiler-role "prerequisite"
                            :compiler-task "specs/bootstrap/tasks.md#SH-07-required-input")
        response (plan [(assoc (repair) :dependencies ["prerequisite-proposal"]) prerequisite])]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (fn [_] (swap! proposals inc) response) []
        #(with-redefs [gh/issue! (fn [& _] (let [id (swap! issues inc)] {:number id :id id}))
                       gh/relation! (fn [& _]
                                      (when (= 1 (swap! relations inc))
                                        (throw (ex-info "Campaign relationship read unavailable" {}))))
                       gh/status! (fn [_ issue status] (swap! statuses conj [(:number issue) status]))]
           (is (thrown-with-msg? Exception #"Campaign relationship read unavailable" (w/plan! ctx)))
           (is (= "model-proposal-name" (get-in @(:state ctx) [:effects "plan/1" :result :slices 0 :key])))
           (is (= failure-key (:key (w/task ctx failure-key))))
           (is (not (w/eligible? ctx (w/task ctx failure-key) [])))
           (let [selected (:slices (w/plan! ctx))]
             (is (= [failure-key] (mapv :key (filter (fn [slice] (= "repair" (:compiler-role slice))) selected)))))
           (is (= 1 @proposals))
           (is (= 2 @issues))
           (is (= [[1 "Ready"] [2 "Ready"]] @statuses))
           (is (= 2 (count (w/tasks ctx))))
           (is (every? (fn [task] (not (:admission-pending task))) (w/tasks ctx)))
           (is (nil? (:planning @(:state ctx))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest interrupted-admission-keeps-the-original-uncertain-effect-held
  (let [ctx (context) proposals (atom 0) issues (atom 0) mutations (atom 0)
        effect "sub_issues/100/1"]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (fn [_] (swap! proposals inc) (plan [(repair)])) []
        #(with-redefs [gh/issue! (fn [& _] (swap! issues inc) {:number 1 :id 1})
                       gh/relation! (fn [& _]
                                      (rt/effect! ctx effect
                                                  (fn [] (swap! mutations inc)
                                                    (throw (ex-info "Relationship outcome unavailable" {})))))]
           (is (thrown-with-msg? Exception #"Relationship outcome unavailable" (w/plan! ctx)))
           (is (= :uncertain (get-in @(:state ctx) [:effects effect :status])))
           (is (not (w/eligible? ctx (w/task ctx failure-key) [])))
           (is (thrown-with-msg? Exception #"needs reconciliation" (w/plan! ctx)))
           (is (= [1 1 1] [@proposals @issues @mutations]))
           (is (= :uncertain (get-in @(:state ctx) [:effects effect :status])))
           (is (:planning @(:state ctx)))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest retained-bound-selection-still-checks-current-evidence-and-task-ownership
  (doseq [change [:source :restriction]]
    (let [ctx (context) mutations (atom 0)]
      (try
        (with-planner ctx #(observation ctx failure-key) (constantly (plan [(repair)])) []
          #(with-redefs [gh/relation! (fn [& _] (swap! mutations inc)
                                        (throw (ex-info "Relationship read unavailable" {})))]
             (is (thrown-with-msg? Exception #"Relationship read unavailable" (w/plan! ctx)))
             (case change
               :source (spit (io/file (get-in ctx [:config :repo-dir]) "module.clj") "(ns changed)\n")
               :restriction (w/task! ctx failure-key :status :attention :error {:details {:restricted true}}))
             (is (thrown-with-msg? Exception
                                   (if (= change :source) #"changed before admission resumed"
                                       #"changed before evidence binding")
                                   (w/plan! ctx)))
             (is (= 1 @mutations))
             (is (not (w/eligible? ctx (w/task ctx failure-key) [])))
             (is (:admission-pending (w/task ctx failure-key)))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest binding-an-existing-task-does-not-change-ownership-or-erase-a-refusal
  (doseq [change [:paths :restriction :candidate]]
    (let [ctx (context) old-key "retained-required-call"]
      (try
        (fixture/add-task! ctx (fixture/slice old-key))
        (case change
          :restriction (w/task! ctx old-key :error {:details {:restricted true}})
          :candidate (w/task! ctx old-key :candidate "authored-source")
          nil)
        (with-planner ctx #(observation ctx failure-key)
          (constantly (plan [(cond-> (assoc (repair) :key old-key)
                               (= :paths change) (assoc :paths ["other.clj"]))])) []
          #(is (thrown-with-msg? Exception #"do not rename it" (w/plan! ctx))))
        (is (= ["module.clj"] (:paths (w/task ctx old-key))))
        (is (nil? (:compiler-observation (w/task ctx old-key))))
        (is (= 1 (count (w/tasks ctx))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest resolved-failure-reruns-current-inputs-and-selects-the-next-boundary
  (let [ctx (context) next-key "compiler/SH-08/current-source-types" calls (atom 0)]
    (try
      (with-planner ctx #(do (swap! calls inc) (observation ctx failure-key))
        (constantly (plan [(repair)])) [] #(w/plan! ctx))
      (w/task! ctx failure-key :status :done :merged "verified-merge")
      (spit (io/file (get-in ctx [:config :repo-dir]) "module.clj") "(ns fixture)\n(def repaired true)\n")
      (with-planner ctx #(do (swap! calls inc) (observation ctx next-key))
        (constantly (plan [(assoc (repair) :compiler-failure next-key)])) []
        #(is (= next-key (:key (first (:slices (w/plan! ctx)))))))
      (is (= 2 @calls))
      (is (= :done (:status (w/task ctx failure-key))))
      (is (= :ready (:status (w/task ctx next-key))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest independent-prerequisites-keep-real-dependencies-and-stable-identities
  (let [ctx (context)
        prerequisite (assoc (repair) :key "dependency" :compiler-role "prerequisite"
                            :compiler-task "SH-19.1" :stage "SH-19"
                            :paths ["runtime.clj"] :proposal "Supply the runtime operation required by this failed call")]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (constantly (plan [(assoc (repair) :dependencies ["dependency"]) prerequisite])) []
        #(let [slices (:slices (w/plan! ctx)) p (second slices) r (w/task ctx failure-key)]
           (is (str/starts-with? (:key p) (str failure-key "/prerequisite/")))
           (is (= [(:key p)] (:dependencies r)))
           (is (not (w/eligible? ctx r [])))
           (is (w/eligible? ctx (w/task ctx (:key p)) []))
           (w/task! ctx (:key p) :status :done :merged "verified-prerequisite")
           (is (w/eligible? ctx r []))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest distinct-existing-prerequisite-requirements-may-share-owned-files
  (let [ctx (context)
        prerequisite (assoc (repair) :compiler-role "prerequisite" :stage "SH-19" :paths ["runtime.clj"])]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (constantly (plan [(assoc prerequisite :key "first" :compiler-task "SH-19.1")
                           (assoc prerequisite :key "second" :compiler-task "SH-19.2")])) []
        #(let [slices (:slices (w/plan! ctx))]
           (is (= 2 (count (set (map :key slices)))))
           (is (every? (fn [slice] (w/eligible? ctx (w/task ctx (:key slice)) [])) slices))
           (is (not (w/eligible? ctx (w/task ctx (:key (second slices)))
                                 [(assoc (first slices) :status :implementing)])))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest prerequisite-ownership-cannot-be-forged-or-inherited-from-a-retained-task
  (doseq [change [:task :stage :path :parent-scope :file-child] retained? [false true]]
    (let [ctx (context) observed (observation ctx failure-key)
          valid (assoc (repair) :key "retained-prerequisite" :compiler-role "prerequisite"
                       :compiler-task "SH-19.1" :stage "SH-19" :paths ["runtime.clj"])
          forged (case change
                   :task (assoc valid :compiler-task "unrelated-requirement")
                   :stage (assoc valid :stage "SH-02")
                   :path (assoc valid :paths ["unrelated.clj"])
                   :parent-scope (assoc valid :paths ["."])
                   :file-child (assoc valid :paths ["runtime.clj/not-real"]))]
      (try
        (when retained? (fixture/add-task! ctx (assoc forged :compiler-observation observed)))
        (with-planner ctx (constantly observed) (constantly (plan [forged])) []
          #(is (thrown-with-msg? Exception #"declared task and implementation ownership" (w/plan! ctx))))
        (is (not (w/eligible? ctx (assoc forged :status :ready :compiler-observation observed) [])))
        (is (= (if retained? 1 0) (count (w/tasks ctx))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest current-prerequisite-declarations-gate-pending-admission-and-ready-dispatch
  (let [ctx (context) mutations (atom 0)
        prerequisite (assoc (repair) :compiler-role "prerequisite" :compiler-task "SH-19.1"
                            :stage "SH-19" :paths ["runtime.clj"])]
    (try
      (with-planner ctx #(observation ctx failure-key) (constantly (plan [prerequisite])) []
        #(with-redefs [gh/relation! (fn [& _] (swap! mutations inc)
                                      (throw (ex-info "Relationship read unavailable" {})))]
           (is (thrown-with-msg? Exception #"Relationship read unavailable" (w/plan! ctx)))
           (let [key (:key (first (w/tasks ctx)))]
             (with-redefs [compiler/classify-observation
                           (fn [_ observed] (assoc-in observed [:failure :prerequisites] []))]
               (is (thrown-with-msg? Exception #"changed before admission resumed" (w/plan! ctx))))
             (is (= 1 @mutations))
             (is (not (w/eligible? ctx (w/task ctx key) [])))
             ;; Even a ready record from the earlier policy cannot bypass the
             ;; current observation's requirement ownership for the same inputs.
             (w/task! ctx key :admission-pending false)
             (is (compiler/selected? (w/task ctx key)))
             (is (not (w/eligible? ctx (w/task ctx key) []))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest changed-inputs-cannot-refresh-a-prerequisite-into-unowned-authoring
  (doseq [change [:removed :different-file]]
    (let [ctx (context) changed? (atom false) executions (atom 0) authors (atom 0)
          prerequisite (assoc (repair) :compiler-role "prerequisite" :compiler-task "SH-19.1"
                              :stage "SH-19" :paths ["runtime.clj"])
          observed (fn []
                     (swap! executions inc)
                     (cond-> (observation ctx failure-key)
                       @changed?
                       (assoc-in [:failure :prerequisites]
                                 (if (= :removed change) []
                                     [{:milestone "SH-19" :task "SH-19.1" :paths ["different.clj"]}]))))]
      (try
        (with-planner ctx observed (constantly (plan [prerequisite])) []
          #(let [selected (first (:slices (w/plan! ctx))) key (:key selected)]
             (is (w/eligible? ctx (w/task ctx key) []))
             (spit (io/file (get-in ctx [:config :repo-dir]) "module.clj") "(ns changed-before-author)\n")
             (reset! changed? true)
             (with-redefs [w/job! (fn [& _] (swap! authors inc)
                                    (throw (ex-info "Author must not start" {})))]
               (is (thrown-with-msg? Exception #"changed before authoring" (w/prepare! ctx key))))
             (is (= 2 @executions))
             (is (zero? @authors))
             (is (= failure-key (get-in @(:state ctx) [:compiler-observation :failure :work-key])))
             (is (not (compiler/selected? (w/task ctx key) (:compiler-observation @(:state ctx)))))
             (is (empty? (:writers (rt/read-edn (:coordination-file ctx)))))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest ancillary-test-paths-cannot-rename-the-same-prerequisite
  (let [first-ctx (context) second-ctx (context)
        prerequisite (assoc (repair) :compiler-role "prerequisite" :compiler-task "SH-19.1"
                            :stage "SH-19" :paths ["runtime.clj" "first_test.clj"])
        successor (assoc prerequisite :key "another-proposal" :paths ["runtime.clj" "another_test.clj"])]
    (try
      (let [first-selection
            (with-planner first-ctx #(observation first-ctx failure-key) (constantly (plan [prerequisite])) []
              #(first (:slices (w/plan! first-ctx))))
            second-selection
            (with-planner second-ctx #(observation second-ctx failure-key) (constantly (plan [successor])) []
              #(first (:slices (w/plan! second-ctx))))]
        (is (= (:key first-selection) (:key second-selection)))
        (w/task! first-ctx (:key first-selection) :status :ci :candidate "immutable-candidate")
        (with-planner first-ctx #(observation first-ctx failure-key)
          (constantly (assoc (plan [successor]) :candidate-dispositions
                             [{:candidate (str "slice/" (:key first-selection))
                               :disposition "independent-successor"
                               :reason "A different test file still addresses the same declared prerequisite."}])) []
          #(is (thrown-with-msg? Exception #"prerequisite has an existing candidate" (w/plan! first-ctx))))
        (is (= 1 (count (w/tasks first-ctx)))))
      (finally
        (fixture/delete-tree! (:root first-ctx))
        (fixture/delete-tree! (:root second-ctx))))))

(deftest unrelated-selection-and-final-release-refusal-cannot-become-repairs
  (let [ctx (context) models (atom 0)]
    (try
      (with-planner ctx #(assoc-in (observation ctx failure-key) [:failure :id] "P18T04007")
        (fn [_] (swap! models inc) (plan [(repair)])) []
        #(let [result (w/plan! ctx)]
           (is (empty? (:slices result)))
           (is (str/includes? (:summary result) "Final-release refusal"))))
      (is (zero? @models))
      (is (empty? (w/tasks ctx)))
      (finally (fixture/delete-tree! (:root ctx)))))
  (let [ctx (context)]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (constantly (plan [(fixture/slice "unrelated")])) []
        #(is (thrown-with-msg? Exception #"must address the observed compiler failure" (w/plan! ctx))))
      (is (empty? (w/tasks ctx)))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest unavailable-execution-is-an-explicit-blocker-without-a-model-task
  (let [ctx (context) calls (atom 0) models (atom 0)]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (fn [_] (swap! models inc) (plan [(repair)])) []
        #(with-redefs [codex/command! (fn [& _]
                                        (swap! calls inc)
                                        (throw (ex-info "Clojure runtime unavailable"
                                                        {:result {:exitCode 127 :stdout "" :stderr "command not found"}
                                                         :writer-ended? true})))]
           (let [result (w/plan! ctx)]
             (is (seq (:blockers result)))
             (is (empty? (:slices result)))
             (is (= :blocked (:status (:compiler-observation @(:state ctx))))))
           (w/plan! ctx)))
      (is (= 1 @calls))
      (is (zero? @models))
      (is (empty? (w/tasks ctx)))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest a-shared-execution-restriction-is-never-retried-or-converted-to-work
  (let [ctx (context) calls (atom 0) models (atom 0)]
    (try
      (with-planner ctx #(observation ctx failure-key)
        (fn [_] (swap! models inc) (plan [(repair)])) []
        #(with-redefs [codex/command! (fn [context _ _ argv]
                                        (swap! calls inc)
                                        (codex/validate-command-result!
                                         context argv {:exitCode 1 :stdout "" :stderr "Operation not permitted"}))]
           (w/plan! ctx)
           (w/plan! ctx)))
      (is (= 1 @calls))
      (is (zero? @models))
      (is (true? (:restricted (:compiler-observation @(:state ctx)))))
      (is (some? (rt/shared-restriction ctx :checks (codex/check-key ctx compiler/command))))
      (is (empty? (w/tasks ctx)))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest changed-relevant-inputs-or-watchdog-can-reobserve-an-unavailable-prerequisite
  (doseq [change [:source :watchdog]]
    (let [ctx (context) calls (atom 0)]
      (try
        (with-planner ctx #(observation ctx failure-key) (constantly (plan [(repair)])) []
          #(with-redefs [codex/command! (fn [_ _ _ _]
                                          (if (= 1 (swap! calls inc))
                                            (throw (ex-info "Unavailable prerequisite"
                                                            {:result {:exitCode 127 :stdout "" :stderr "not found"}
                                                             :writer-ended? true}))
                                            (execute-result! (observation ctx failure-key))))]
             (is (empty? (:slices (w/plan! ctx))))
             (is (seq (:inputs (:compiler-observation @(:state ctx)))))
             (when (= :source change)
               (spit (io/file (get-in ctx [:config :repo-dir]) "module.clj") "(ns fixture)\n(def dependency true)\n"))
             (let [next-ctx (if (= :watchdog change) (assoc-in ctx [:config :compiler-observation-timeout-ms] 7200000) ctx)]
               (is (= failure-key (:key (first (:slices (w/plan! next-ctx)))))))))
        (is (= 2 @calls))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest changing-watchdog-cannot-replay-an-uncertain-execution
  (let [ctx (context) calls (atom 0)]
    (try
      (with-planner ctx #(observation ctx failure-key) (constantly (plan [(repair)])) []
        #(with-redefs [codex/command! (fn [& _]
                                        (swap! calls inc)
                                        (throw (ex-info "Unknown process termination" {:writer-ended? false})))]
           (w/plan! ctx)
           (let [result (w/plan! (assoc-in ctx [:config :compiler-observation-timeout-ms] 7200000))]
             (is (empty? (:slices result)))
             (is (str/includes? (:summary result) "needs inspection")))))
      (is (= 1 @calls))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest a-new-shared-denial-prevents-reuse-of-cached-actionable-evidence
  (let [ctx (context) calls (atom 0) models (atom 0)]
    (try
      (with-planner ctx #(do (swap! calls inc) (observation ctx failure-key))
        (fn [_] (swap! models inc) (plan [])) []
        #(do
           (w/plan! ctx)
           (rt/record-denial! ctx :check (codex/check-key ctx compiler/command) {:reason "Inspected denied execution"})
           (let [result (w/plan! ctx)]
             (is (empty? (:slices result)))
             (is (true? (:restricted (:compiler-observation @(:state ctx))))))))
      (is (= 1 @calls))
      (is (= 1 @models))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest candidate-checks-require-the-observed-repair-to-advance
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir]) before (observation ctx failure-key)]
    (try
      (fixture/add-task! ctx (assoc (repair) :key failure-key :compiler-observation before
                                    :worktree cwd :candidate (:source-revision before)))
      (with-redefs [w/assert-identity! (fn [& _]) rt/lease-phase! (fn [& _])
                    w/transition! (fn [& _]) shared/checks! (fn [& _])
                    w/assert-clean! (fn [& _])
                    codex/command! (fn [& _] (execute-result! (observation ctx failure-key)))]
        (is (thrown-with-msg? Exception #"still reaches the selected failure"
                              (w/check-candidate! ctx failure-key [] nil))))
      (spit (io/file cwd "module.clj") "(ns fixture)\n(def compiler-advance true)\n")
      (with-redefs [w/assert-identity! (fn [& _]) rt/lease-phase! (fn [& _])
                    w/transition! (fn [& _]) shared/checks! (fn [& _])
                    w/assert-clean! (fn [& _])
                    codex/command! (fn [& _] (execute-result! (assoc (observation ctx "compiler/SH-08/next")
                                                                     :position {:component 0 :stage :canonical-core})))]
        (w/check-candidate! ctx failure-key [] nil))
      (is (= "compiler/SH-08/next" (get-in (w/task ctx failure-key) [:compiler-result :failure :work-key])))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest changed-diagnostics-pruned-closures-and-earlier-stages-are-not-progress
  (let [ctx (context) before (observation ctx failure-key)
        later (assoc before :position {:component 0 :stage :canonical-core})
        next-module (assoc before :completed [{:component-id "compiler"}]
                           :position {:component 1 :stage :current-frontend})]
    (try
      (is (compiler/advanced? before later))
      (is (compiler/advanced? later next-module))
      (is (compiler/nonregressing? before before))
      (is (not (compiler/advanced? before (assoc-in before [:failure :work-key] "different-diagnostic"))))
      (is (not (compiler/nonregressing? later before)))
      (is (not (compiler/nonregressing? next-module later)))
      (is (not (compiler/advanced? before (assoc-in later [:route :closure] [(first (get-in later [:route :closure]))]))))
      (is (not (compiler/advanced? before (assoc-in later [:route :target] :different-target))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest directory-input-identity-includes-added-removed-and-changed-sources
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir]) directory (io/file cwd "sources")]
    (try
      (.mkdirs directory)
      (spit (io/file directory "a.clj") "a")
      (let [before (compiler/input-sha256 cwd "sources")]
        (spit (io/file directory "b.clj") "b")
        (is (not= before (compiler/input-sha256 cwd "sources")))
        (.delete (io/file directory "b.clj"))
        (is (= before (compiler/input-sha256 cwd "sources")))
        (spit (io/file directory "a.clj") "changed")
        (is (not= before (compiler/input-sha256 cwd "sources"))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest root-probe-and-controller-share-byte-identities-and-the-declared-scope
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir]) directory (io/file cwd "sources")]
    (try
      (.mkdirs directory)
      (spit (io/file directory "z.clj") "(ns last)\n" :encoding "UTF-8")
      (spit (io/file directory "a.clj") "(ns first)\n" :encoding "UTF-8")
      (is (thrown? Exception (compiler/input-sha256 cwd "../outside")))
      (is (thrown? Exception (compiler/input-sha256 cwd (.getAbsolutePath (io/file cwd "module.clj")))))
      (let [closure [{:component-id "compiler" :source-path "module.clj" :dependencies ["runtime"]}
                     {:component-id "runtime" :source-path "sources/a.clj"}]
            route (probe/route closure)
            inputs [{:path "module.clj" :sha256 (compiler/input-sha256 cwd "module.clj")}]]
        (is (= closure (:closure route)))
        (is (= compiler/command (:command route)))
        (is (= :llvm-x86_64-linux (:target route)))
        (is (= :complete-six-owner-typed-frontend-prerequisite (:scope route)))
        (is (= :actual-native-compiler-build-recipe-unresolved (:build-graph-status route)))
        (is (= (probe/digest {:inputs inputs :route route})
               (compiler/input-id {:inputs inputs :route route}))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest executable-byte-changes-invalidate-identity-even-with-the-same-size-and-mtime
  (let [ctx (context) file (io/file (:root ctx) "compiler-command")]
    (try
      (spit file "before")
      (let [modified (.lastModified file)
            before (#'compiler/executable-file-identity file)]
        (spit file "after!")
        (is (.setLastModified file modified))
        (is (= modified (.lastModified file)))
        (let [after (#'compiler/executable-file-identity file)]
          (is (= (dissoc before :sha256) (dissoc after :sha256)))
          (is (not= (:sha256 before) (:sha256 after)))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest probe-routes-only-the-selected-owners-and-their-real-imports
  ;; Transport coverage with supplied headers, not compiler acceptance evidence.
  (let [roots (mapv (fn [index id]
                      {:component-id id
                       :source-path (str "language/compiler/src/gravity/cohort_" index ".gravity")
                       :dependencies []})
                    (range) probe/cohort)
        imported {:component-id :required-import
                  :source-path "language/compiler/src/gravity/required.gravity" :dependencies []}
        optional {:component-id :optional-broken
                  :source-path "language/compiler/src/gravity/optional_broken.gravity" :dependencies []}
        headers (into {:required-import {:module 'gravity.required :requires []}}
                      (map-indexed (fn [index component]
                                     [(:component-id component)
                                      {:module (symbol (str "gravity.cohort-" index))
                                       :requires (if (zero? index) [{:module 'gravity.required}] [])}]) roots))
        visited (atom [])]
    (with-redefs-fn {#'probe/module-header
                     (fn [_ component]
                       (swap! visited conj (:component-id component))
                       (or (get headers (:component-id component))
                           (throw (ex-info "Unselected optional source must not be parsed" {}))))}
      #(let [closure (#'probe/source-closure "/transport-fixture" (conj roots imported optional))]
         (is (= (into [:required-import] probe/cohort) (mapv :component-id closure)))
         (is (= ['gravity.required] (:source-imports (second closure))))
         (is (not (some #{:optional-broken} @visited)))))
    (with-redefs-fn {#'probe/module-header
                     (fn [_ component]
                       (cond-> (get headers (:component-id component))
                         (= :required-import (:component-id component))
                         (assoc :module 'gravity.different)))}
      #(is (thrown-with-msg? Exception #"different namespace"
                             (#'probe/source-closure "/transport-fixture" (conj roots imported optional)))))))

(deftest probe-forwards-the-existing-import-products-without-substituting-facts
  ;; Supplied products exercise transport identity; the real probe must produce
  ;; and verify them through the full source route before this handoff.
  (let [component {:component-id :consumer :source-imports ['gravity.required]}
        resolution {:status :accepted :artifact :supplied-c5-product}
        imported-request {:canonical-core-artifact :supplied-verified-producer-wrapper}
        core {:status :accepted :artifact :supplied-consumer-core}
        report {:status :passed :artifact :supplied-fresh-report}
        coordinator {:status :passed :artifact :supplied-coordinator}
        context {:canonical-core-artifact :supplied-authenticated-wrapper}
        calls (atom [])]
    (is (identical? resolution (first (:module-artifacts
                                       (probe/module-inputs component {'gravity.required resolution})))))
    (with-redefs-fn {#'probe/type-api
                     (fn [function & args]
                       (swap! calls conj [function args])
                       (case function coordinator-verification coordinator b47-context context))}
      #(let [request (#'probe/type-request component core report {'gravity.required imported-request})]
         (is (identical? imported-request (first (get-in request [:module-type-inputs :module-requests]))))
         (is (= :supplied-authenticated-wrapper (:canonical-core-artifact request)))
         (is (identical? context (:b47-context request)))
         (is (identical? coordinator (:coordinator-verification request)))
         (is (= [['coordinator-verification [core report]] ['b47-context [core report coordinator]]]
                (mapv (fn [[function args]] [function (vec args)]) @calls)))))))

(deftest probe-blocks-imports-without-their-required-produced-results
  (let [component {:component-id :consumer :source-imports ['gravity.required]}]
    (is (= :missing-module-product
           (try (probe/module-inputs component {}) nil
                (catch Exception exception (:reason (ex-data exception))))))
    (with-redefs-fn {#'probe/type-api (fn [& _] {})}
      #(is (= :missing-module-type-product
              (try (#'probe/type-request component {} {} {}) nil
                   (catch Exception exception (:reason (ex-data exception)))))))))
