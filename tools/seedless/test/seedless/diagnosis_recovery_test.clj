(ns seedless.diagnosis-recovery-test
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [seedless.diagnosis-recovery :as recovery]
            [seedless.main :as main]
            [seedless.main-invariants-test :as fixture]
            [seedless.workflow :as workflow]
            [seedless.workflow-test :as workflow-fixture]
            [simplify.codex :as codex]
            [simplify.main :as shared]
            [simplify.runtime :as rt]))

(use-fixtures :each (fn [test] (with-redefs [rt/event! (fn [& _])] (test))))

(defn write-events! [path events]
  (spit path (apply str (map #(str (json/write-str %) "\n") events))))

(defn failed-context [kind legacy?]
  (let [ctx (fixture/lock-context)
        ctx (assoc-in ctx [:identity :codex-command] ["fixture-codex" "app-server"])
        id (str (name kind) "/fixture")
        job (if (= kind :compiler-diagnosis) "diagnose-1" "review-1")
        observation {:source-revision "source-before" :input-id "complete-inputs"
                     :position {:component 0 :stage :function-types}
                     :failure {:id "FUTURE-COMPILER"} :subject {:source-sha256 "source-bytes"}
                     :terminal {:status :failed :exit 1}}
        observation-key (rt/digest (pr-str (select-keys observation [:input-id :position :failure :subject :terminal])))
        failed {:effect id :observation-key observation-key :status :blocked
                :message "App-server request failed" :restricted false
                :details {:method "initialize" :effect id :writer-ended? false}}
        effect (cond-> {:status :uncertain :message (:message failed)} (not legacy?) (assoc :job job :failure failed))
        task {:key "pending" :status :attention :resume-status :acceptance-pending
              :base "before" :candidate "immutable-candidate" :tree "immutable-tree"
              :checks {:candidate "immutable-candidate" :tree "immutable-tree" :result :passed}
              :compiler-observation observation :compiler-result observation
              :error {:message (:message failed) :details (:details failed)}}
        task (if (= kind :compiler-diagnosis)
               (assoc-in task [:compiler-result :diagnosis] failed)
               (assoc task :compiler-progress-intent id))
        log (str (io/file (:root ctx) "events.jsonl"))
        artifacts (into {} (for [role [:runtime-receipt :driver-exit :driver-stdout :driver-finished]]
                             [role (str (io/file (:root ctx) (name role)))]))
        entry {:event "foreground-production-entrypoint" :time "2026-11-13T21:52:13Z"
               :controller-revision "original-controller" :controller-tree "original-controller-tree"
               :compiler-revision (:source-revision observation)}
        sent {:event "rpc-send" :job job :time "2026-11-13T21:52:16Z" :message {:id 1 :method "initialize"}}
        stderr {:event "app-server-stderr" :job job :time "2026-11-13T21:52:26Z" :line "Fixture startup failure"}
        events (cond-> [entry sent stderr]
                 (not legacy?) (conj {:event "app-server-closed" :job job :time "2026-11-13T21:52:26.5Z"
                                     :owned-process-ended true :exit 1}))
        ;; Same typed shape as the retained production foreground artifacts.
        receipt {:schema :seedless/foreground-runtime-receipt-v1
                 :driver-exit 0 :driver-finished-at "2026-11-13T21:52:27Z"
                 :controller {:revision (:controller-revision entry) :tree (:controller-tree entry)}
                 :diagnosis {:status :blocked :outcome :runtime-blocked-before-model
                             :model-proposal-received false :message (:message failed) :restricted false}
                 :original-effect {:id id :status :uncertain :failure-details (:details failed)}
                 :jobs [{:id job :requested-role :diagnose :requested-mode :read :status :attention
                         :message (:message failed) :restricted false :thread-created false :turn-created false}]
                 :runtime {:command (get-in ctx [:identity :codex-command]) :rpc-send-methods ["initialize"]
                           :initialize-sent-at (:time sent) :rpc-response-count 0
                           :stderr [(select-keys stderr [:time :job :line])]}
                 :compiler {:revision (:source-revision observation) :input-id (:input-id observation)}
                 :model-turn-completed false :campaign-started false :provider-writes false :collector-status :blocked}
        stdout {:controller-revision (:controller-revision entry) :compiler-revision (:source-revision observation)
                :input-id (:input-id observation) :state-directory (:state-dir ctx)
                :model-turn-completed false :campaign-started false :provider-writes false
                :result-status :blocked :collector-status :blocked}]
    (write-events! log events)
    (rt/write-edn! (:runtime-receipt artifacts) receipt)
    (rt/write-edn! (:driver-exit artifacts) 0)
    (rt/write-edn! (:driver-stdout artifacts) stdout)
    (spit (:driver-finished artifacts) (str (:driver-finished-at receipt) "\n"))
    (rt/update! ctx assoc :identity (:identity ctx) :compiler-observation observation :tasks {"pending" task}
                :effects {id effect} :jobs {job {:status :attention :message (:message failed) :restricted false}}
                :diagnosis-intents (if (= kind :compiler-diagnosis) {observation-key id} {})
                :diagnoses (if (= kind :compiler-diagnosis) {"fixture" failed} {})
                :diagnosis-blocker (when (= kind :compiler-diagnosis) failed))
    (assoc ctx :log-file log :test-effect id :test-job job :test-artifacts artifacts
           :test-termination (:driver-exit artifacts))))

(defn inspection [ctx]
  (let [state @(:state ctx) id (:test-effect ctx) job (:test-job ctx)]
    {:outcome :not-applied :result nil
     :evidence "Inspected the original initialization failure, complete native trace and completed foreground cleanup."
     :model-not-started
     {:effect id :job job :state-identity-sha256 (rt/digest (pr-str (:identity state)))
      :effect-sha256 (rt/digest (pr-str (get-in state [:effects id])))
      :job-sha256 (rt/digest (pr-str (get-in state [:jobs job])))
      :context (recovery/operation-context state id)
      :job-events-sha256 (rt/digest (pr-str (recovery/job-events ctx job)))
      :trace-complete true :termination :inspected-foreground-return
      :termination-description "The fixture foreground driver returned after synchronous owned-process cleanup."
      :termination-evidence (mapv (fn [[role path]] {:role role :path path :sha256 (recovery/file-digest path)})
                                  (:test-artifacts ctx))}}))

(defn write-inspection! [ctx value]
  (let [file (str (io/file (:root ctx) "inspection.edn"))]
    (rt/write-edn! file value)
    file))

(deftest inspected-initialization-failure-resumes-audit-with-original-history
  (doseq [legacy? [false true]]
    (let [ctx (failed-context :compiler-diagnosis legacy?)
          id (:test-effect ctx) original @(:state ctx)
          inspected (inspection ctx) file (write-inspection! ctx inspected)]
      (try
        (is (thrown? Exception (main/retry! ctx "audit" file)))
        (main/dispatch! ctx "reconcile" [id file])
        (let [settled (get-in @(:state ctx) [:effects id])]
          (is (= :not-applied (:status settled)))
          (is (= (get-in original [:effects id]) (:prior settled)))
          (is (= inspected (:inspection settled)))
          (main/dispatch! ctx "retry" ["audit" file])
          (is (nil? (:diagnosis-blocker @(:state ctx))))
          (is (= (:diagnosis-blocker original) (get-in @(:state ctx) [:inspections 0 :diagnosis-blocker])))
          (is (= (:jobs original) (:jobs @(:state ctx))))
          (is (= (:compiler-observation original) (:compiler-observation @(:state ctx))))
          (is (= (:diagnoses original) (:diagnoses @(:state ctx))))
          (is (= settled (get-in @(:state ctx) [:effects id])))
          (is (true? (recovery/assert-predecessor! ctx id)))
          (is (= :new-result (rt/effect! ctx id (constantly :new-result))))
          (is (= settled (get-in @(:state ctx) [:effects id :previous])))
          (is (= (get-in original [:effects id]) (get-in @(:state ctx) [:effects id :previous :prior]))))
        (finally (workflow-fixture/delete-tree! (:root ctx)))))))

(deftest inspected-pending-model-recovery-preserves-candidate-and-evidence
  (doseq [kind [:compiler-diagnosis :compiler-progress]]
    (let [ctx (failed-context kind false) id (:test-effect ctx)
          original (get-in @(:state ctx) [:tasks "pending"])
          file (write-inspection! ctx (inspection ctx))]
      (try
        (main/dispatch! ctx "reconcile" [id file])
        (main/dispatch! ctx "retry" ["pending" file])
        (let [resumed (get-in @(:state ctx) [:tasks "pending"])]
          (is (= :acceptance-pending (:status resumed)))
          (is (= (select-keys original [:base :candidate :tree :checks :compiler-observation :compiler-result])
                 (select-keys resumed [:base :candidate :tree :checks :compiler-observation :compiler-result])))
          (is (= (:error original) (get-in resumed [:history 0 :error])))
          (is (true? (:compiler-progress-resume resumed)))
          (is (nil? (:acceptance resumed)))
          (is (true? (recovery/assert-predecessor! ctx id)))
          ;; Ordinary continuation may collect additional checks on this same
          ;; immutable candidate before starting the new progress assessment.
          (rt/update! ctx assoc-in [:tasks "pending" :checks :commands] [["clojure" "new-evidence.clj"]])
          (is (true? (recovery/assert-predecessor! ctx id))))
        (finally (workflow-fixture/delete-tree! (:root ctx)))))))

(deftest unsupported-not-applied-model-effects-cannot-bypass-inspected-recovery
  (let [ctx (failed-context :compiler-diagnosis true) id (:test-effect ctx)
        file (write-inspection! ctx {:outcome :not-applied :result nil :evidence "No supporting inspection"})]
    (try
      (let [before @(:state ctx)]
        (is (thrown? Exception (main/dispatch! ctx "reconcile" [id file])))
        (is (= before @(:state ctx))))
      ;; Even an inherited generic reconciliation cannot bypass the model guard.
      (shared/reconcile! ctx id file)
      (is (thrown? Exception (main/retry! ctx "audit" file)))
      (is (thrown? Exception (main/retry! ctx "pending" file)))
      (is (thrown? Exception (recovery/assert-predecessor! ctx id)))
      (finally (workflow-fixture/delete-tree! (:root ctx))))))

(deftest stale-ambiguous-started-and-restricted-recovery-is-held
  (doseq [change [:effect :job :identity :effect-bytes :job-bytes :context :trace :trace-incomplete
                  :no-termination :changed-termination :thread :turn :output :restricted-job
                  :restricted-effect :restricted-history :restricted-failure :missing-job :missing-observation
                  :other-job :other-effect :later-phase :turn-send :thread-send :command-send :approval :reader-error]]
    (let [ctx (failed-context :compiler-diagnosis true) id (:test-effect ctx) job (:test-job ctx)
          initial (inspection ctx)
          claim (:model-not-started initial)]
      (try
        (case change
          :thread (rt/update! ctx assoc-in [:jobs job :thread] "started-thread")
          :turn (rt/update! ctx assoc-in [:jobs job :turn] "started-turn")
          :output (rt/update! ctx assoc-in [:jobs job :output] {:outcome "unknown"})
          :restricted-job (rt/update! ctx assoc-in [:jobs job :restricted] true)
          :restricted-effect (rt/update! ctx assoc-in [:effects id :status] :restricted)
          :restricted-history (rt/update! ctx assoc-in [:effects id :previous] {:status :restricted})
          :restricted-failure (rt/update! ctx assoc-in [:diagnoses "fixture" :restricted] true)
          :missing-job (rt/update! ctx assoc :jobs {})
          :missing-observation (rt/update! ctx #(-> % (dissoc :compiler-observation) (assoc :tasks {})))
          :other-job (rt/update! ctx assoc-in [:jobs "diagnose-2"] (get-in @(:state ctx) [:jobs job]))
          :other-effect (rt/update! ctx assoc-in [:effects "compiler-diagnosis/other"] (get-in @(:state ctx) [:effects id]))
          :later-phase (rt/update! ctx assoc-in [:diagnoses "fixture" :details :method] "turn/start")
          :changed-termination (spit (:test-termination ctx) "unknown\n")
          (:turn-send :thread-send :command-send :approval :reader-error)
          (spit (:log-file ctx)
                (str (json/write-str
                      (case change
                        :approval {:event "rpc-receive" :job job :message {:id 3 :method "item/commandExecution/requestApproval"}}
                        :reader-error {:event "rpc-reader-error" :job job :message "Malformed response"}
                        {:event "rpc-send" :job job
                         :message {:id 2 :method (case change :turn-send "turn/start" :thread-send "thread/start" "command/exec")}})) "\n")
                :append true)
          nil)
        (let [changed (case change
                        :effect (assoc claim :effect "compiler-diagnosis/other")
                        :job (assoc claim :job "diagnose-2")
                        :identity (assoc claim :state-identity-sha256 "old-identity")
                        :effect-bytes (assoc claim :effect-sha256 "old-effect")
                        :job-bytes (assoc claim :job-sha256 "old-job")
                        :context (assoc claim :context {:kind :different-context})
                        :trace (assoc claim :job-events-sha256 "missing-trace")
                        :trace-incomplete (assoc claim :trace-complete false)
                        :no-termination (dissoc claim :termination-evidence)
                        ;; Refresh digests to prove semantic guards also reject an inspected bad state.
                        (merge (:model-not-started (inspection ctx))
                               (when (= change :changed-termination)
                                 {:termination-evidence (:termination-evidence claim)})))
              file (write-inspection! ctx (assoc initial :model-not-started changed))
              before @(:state ctx)]
          (is (thrown? Exception (main/dispatch! ctx "reconcile" [id file])) (name change))
          (is (= before @(:state ctx)) (name change)))
        (finally (workflow-fixture/delete-tree! (:root ctx)))))))

(deftest foreground-termination-requires-meaningful-bound-artifacts
  (doseq [change [:empty-receipt :unstructured-receipt :extra-edn :unknown-schema
                  :different-effect :different-job :different-source :different-controller
                  :started-turn :nonzero-exit :empty-stdout :different-state-directory
                  :early-finish :different-native-stderr]]
    (let [ctx (failed-context :compiler-diagnosis true) id (:test-effect ctx)
          paths (:test-artifacts ctx)
          edit! (fn [role f] (rt/write-edn! (get paths role) (f (edn/read-string (slurp (get paths role))))))]
      (try
        (case change
          :empty-receipt (spit (:runtime-receipt paths) "")
          :unstructured-receipt (spit (:runtime-receipt paths) "Some arbitrary operator prose")
          :extra-edn (spit (:runtime-receipt paths) "\n{}" :append true)
          :unknown-schema (edit! :runtime-receipt #(assoc % :schema :unknown))
          :different-effect (edit! :runtime-receipt #(assoc-in % [:original-effect :id] "compiler-diagnosis/other"))
          :different-job (edit! :runtime-receipt #(assoc-in % [:jobs 0 :id] "diagnose-2"))
          :different-source (edit! :runtime-receipt #(assoc-in % [:compiler :revision] "other-source"))
          :different-controller (edit! :runtime-receipt #(assoc-in % [:controller :revision] "other-controller"))
          :started-turn (edit! :runtime-receipt #(assoc-in % [:jobs 0 :turn-created] true))
          :nonzero-exit (rt/write-edn! (:driver-exit paths) 1)
          :empty-stdout (spit (:driver-stdout paths) "")
          :different-state-directory (edit! :driver-stdout #(assoc % :state-directory (str (io/file (:root ctx) "other-state"))))
          :early-finish (do (edit! :runtime-receipt #(assoc % :driver-finished-at "2026-11-13T21:52:14Z"))
                            (spit (:driver-finished paths) "2026-11-13T21:52:14Z\n"))
          :different-native-stderr (edit! :runtime-receipt #(assoc-in % [:runtime :stderr 0 :line] "A different run")))
        ;; Recompute every digest: a matching hash cannot make empty, untyped,
        ;; contradictory or unrelated evidence into an original return receipt.
        (let [file (write-inspection! ctx (inspection ctx)) before @(:state ctx)]
          (is (thrown? Exception (main/dispatch! ctx "reconcile" [id file])) (name change))
          (is (= before @(:state ctx)) (name change)))
        (finally (workflow-fixture/delete-tree! (:root ctx)))))))

(deftest owned-process-closure-must-follow-the-complete-job-trace
  (doseq [change [:before-initialize :not-final :duplicate :wrong-job :still-running :missing-exit]]
    (let [ctx (failed-context :compiler-diagnosis false) id (:test-effect ctx) job (:test-job ctx)
          events (mapv #(json/read-str % :key-fn keyword) (str/split-lines (slurp (:log-file ctx))))
          [entry sent stderr closed] events]
      (try
        (write-events! (:log-file ctx)
                       (case change
                         :before-initialize [entry closed sent stderr]
                         :not-final [entry sent closed stderr]
                         :duplicate [entry sent stderr closed closed]
                         :wrong-job [entry sent stderr (assoc closed :job "diagnose-2")]
                         :still-running [entry sent stderr (assoc closed :owned-process-ended false)]
                         :missing-exit [entry sent stderr (dissoc closed :exit)]))
        (let [inspected (inspection ctx)
              ;; A close for another job provides no termination proof here.
              inspected (cond-> inspected (= change :wrong-job) (update :model-not-started dissoc :termination-evidence))
              file (write-inspection! ctx inspected) before @(:state ctx)]
          (is (= ["initialize"] (mapv #(get-in % [:message :method])
                                      (filter #(= "rpc-send" (:event %)) (recovery/job-events ctx job)))))
          (is (thrown? Exception (main/dispatch! ctx "reconcile" [id file])) (name change))
          (is (= before @(:state ctx)) (name change)))
        (finally (workflow-fixture/delete-tree! (:root ctx)))))))

(deftest progress-recovery-needs-both-retained-input-identities
  (doseq [change [:both-absent :before-absent :after-absent :before-input-blank :after-source-blank]]
    (let [ctx (failed-context :compiler-progress false) id (:test-effect ctx)]
      (try
        (rt/update! ctx update-in [:tasks "pending"]
                    (fn [task]
                      (case change
                        :both-absent (dissoc task :compiler-observation :compiler-result)
                        :before-absent (dissoc task :compiler-observation)
                        :after-absent (dissoc task :compiler-result)
                        :before-input-blank (assoc-in task [:compiler-observation :input-id] " ")
                        :after-source-blank (assoc-in task [:compiler-result :source-revision] ""))))
        (let [file (write-inspection! ctx (inspection ctx)) before @(:state ctx)]
          (is (nil? (recovery/operation-context before id)) (name change))
          (is (thrown? Exception (main/dispatch! ctx "reconcile" [id file])) (name change))
          (is (= before @(:state ctx)) (name change)))
        (finally (workflow-fixture/delete-tree! (:root ctx)))))))

(deftest settled-inspection-is-rechecked-against-current-candidate-and-job
  (doseq [change [:candidate :job :trace]]
    (let [ctx (failed-context :compiler-progress false) id (:test-effect ctx)
          file (write-inspection! ctx (inspection ctx))]
      (try
        (main/reconcile! ctx id file)
        (case change
          :candidate (rt/update! ctx assoc-in [:tasks "pending" :candidate] "different-candidate")
          :job (rt/update! ctx assoc-in [:jobs (:test-job ctx) :turn] "late-turn")
          :trace (spit (:log-file ctx) ""))
        (let [before @(:state ctx)]
          (is (thrown? Exception (main/retry! ctx "pending" file)))
          (is (thrown? Exception (recovery/assert-predecessor! ctx id)))
          (is (= before @(:state ctx))))
        (finally (workflow-fixture/delete-tree! (:root ctx)))))))

(deftest model-effect-is-bound-before-startup-and-keeps-its-initialization-failure
  (let [ctx (fixture/lock-context) id "compiler-diagnosis/fixture"]
    (try
      (with-redefs [codex/job! (fn [context job _ _ _ _ _]
                               (is (= job (get-in @(:state context) [:effects id :job])))
                               (throw (ex-info "Initialization fixture failed" {:method "initialize"})))]
        (is (thrown? Exception
                     (rt/effect! ctx id #(workflow/job! (assoc ctx :model-effect id) :diagnose "." "fixture" {})))))
      (is (= "diagnose-1" (get-in @(:state ctx) [:effects id :job])))
      (is (= "initialize" (get-in @(:state ctx) [:effects id :failure :details :method])))
      (is (= :uncertain (get-in @(:state ctx) [:effects id :status])))
      (finally (workflow-fixture/delete-tree! (:root ctx))))))

(deftest initialization-failure-records-owned-process-termination-after-cleanup
  (let [ctx (fixture/lock-context)
        classpath (str/join java.io.File/pathSeparator
                            (map #(.getAbsolutePath (io/file %))
                                 (str/split (System/getProperty "java.class.path")
                                            (re-pattern java.io.File/pathSeparator))))
        program "(.readLine (java.io.BufferedReader. *in*)) (println \"{\\\"id\\\":1,\\\"error\\\":{\\\"code\\\":-32000,\\\"message\\\":\\\"fixture initialization failed\\\"}}\") (flush) (System/exit 7)"
        ctx (assoc-in ctx [:config :codex-command]
                      [(str (io/file (System/getProperty "java.home") "bin/java")) "-cp" classpath
                       "clojure.main" "-e" program])
        events (atom [])]
    (try
      (with-redefs [rt/event! (fn [context kind data]
                               (when (= :app-server-closed kind)
                                 (is (empty? @(:processes context))))
                               (swap! events conj (assoc data :event kind)))]
        (is (thrown? Exception (codex/job! ctx "diagnose-fixture" (:root ctx) :read "fixture" "fixture" {}))))
      (let [closed (filter #(= :app-server-closed (:event %)) @events)]
        (is (= 1 (count closed)))
        (is (= "diagnose-fixture" (:job (first closed))))
        (is (true? (:owned-process-ended (first closed))))
        (is (integer? (:exit (first closed))))
        (is (= ["initialize"] (mapv #(get-in % [:message :method])
                                    (filter #(= :rpc-send (:event %)) @events)))))
      (is (= :attention (get-in @(:state ctx) [:jobs "diagnose-fixture" :status])))
      (is (nil? (get-in @(:state ctx) [:jobs "diagnose-fixture" :turn])))
      (finally (workflow-fixture/delete-tree! (:root ctx))))))
