(ns seedless.main
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [seedless.compiler-observation :as compiler]
            [seedless.github :as gh]
            [seedless.workflow :as workflow]
            [simplify.main :as shared]
            [simplify.workflow :as shared-workflow]
            [simplify.runtime :as rt])
  (:import [java.math BigInteger]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files]))

(def completion-check ["clojure" "-M:gravity" "self-host" "verify"])
(def boundary-check ["clojure" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"])

(defn probe! [ctx argv]
  (let [result (rt/command ctx (get-in ctx [:config :repo-dir]) argv)]
    (when-not (zero? (:exit result))
      (let [restricted (rt/authorization-denied? (str (:err result) "\n" (:out result)))]
        (rt/event! ctx :startup-probe-failed {:argv argv :restricted restricted})
        (rt/fail! "Read-only startup probe failed" (assoc result :argv argv :restricted restricted))))
    (str/trim (:out result))))

(defn repository-name [url]
  (second (re-matches #"(?:https://github\.com/|git@github\.com:|ssh://git@github\.com/)([A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+?)(?:\.git)?/?" url)))

(defn target! [ctx]
  (let [config (:config ctx)
        _ (probe! ctx ["git" "check-ref-format" "--branch" (:base-branch config)])
        root (rt/canonical (probe! ctx ["git" "rev-parse" "--show-toplevel"]))
        _ (when-not (= root (:repo-dir config)) (rt/fail! "Repository directory must name its checkout root" {}))
        fetch-url (probe! ctx ["git" "remote" "get-url" (:remote config)])
        push-url (probe! ctx ["git" "remote" "get-url" "--push" (:remote config)])
        _ (doseq [url [fetch-url push-url]]
            (when-not (= (str/lower-case (:repository config))
                         (some-> (repository-name url) str/lower-case))
              (rt/fail! "Git remote and GitHub repository must identify the same target" {})))
        common (io/file (probe! ctx ["git" "rev-parse" "--git-common-dir"]))]
    (assoc (select-keys config [:repository :campaign :objective :contracts :completion-checks
                                :repo-dir :remote :base-branch :worktree-root :checks :project-title :codex-command])
           :git-common-dir (rt/canonical (if (.isAbsolute common) common (io/file root (str common)))))))

(defn config! [path]
  (let [config (merge {:compiler-observation-command compiler/command
                       :compiler-observation-timeout-ms 7200000} (shared/config! path))]
    (when-not (and (= compiler/command (:compiler-observation-command config))
                   (pos-int? (:compiler-observation-timeout-ms config)))
      (rt/fail! "Seedless compiler feedback must use the declared current-source probe" {}))
    (when-not (and (string? (:objective config)) (not (str/blank? (:objective config)))
                   (vector? (:contracts config)) (seq (:contracts config))
                   (some #{completion-check} (:completion-checks config))
                   (some #{boundary-check} (:checks config)))
      (rt/fail! "Seedless needs its objective, contracts, boundary check and canonical self-host verifier" {}))
    (when-not (and (string? (:remote config)) (re-matches #"[A-Za-z0-9][A-Za-z0-9._-]*" (:remote config))
                   (string? (:base-branch config)) (not (str/blank? (:base-branch config)))
                   (pos-int? (:check-timeout-ms config))
                   (vector? (:codex-command config))
                   (= ["codex" "app-server"] (vec (take 2 (:codex-command config))))
                   (every? #(and (string? %) (not (str/blank? %))) (:codex-command config)))
      (rt/fail! "Seedless needs a named Git target, a check watchdog and the Codex app-server command" {}))
    (doseq [path (:contracts config)] (shared-workflow/safe-path! path))
    (doseq [argv (concat (:checks config) (:completion-checks config))]
      (when-not (and (vector? argv) (seq argv) (every? string? argv)
                     (contains? (:check-executables config) (first argv)))
        (rt/fail! "Configured check is outside the toolset" {:command argv})))
    (assoc config :client-info {:name "uniform_seedless" :title "Uniform Seedless" :version "0.1.0"})))

(def ^:private status-identity-keys
  [:repository :campaign :objective :contracts :completion-checks :repo-dir :remote
   :base-branch :worktree-root :checks :project-title :codex-command])

(def ^:private status-restriction-key?
  (fn [key]
    (let [name (when (or (keyword? key) (string? key)) (name key))]
      (and name (or (= name "restrictions") (str/includes? name "restricted"))))))

(defn- nonblank-string? [value]
  (and (string? value) (not (str/blank? value))))

(defn- content-identity [value]
  (let [serialized (pr-str value)]
    {:present true
     :sha256 (rt/digest serialized)
     :serialization :edn-pr-str-utf8}))

(defn- status-identity [config state]
  (let [recorded (:identity state)
        keys (vec (filter #(contains? config %) status-identity-keys))
        configured (select-keys config keys)
        complete? (and (map? recorded) (every? #(contains? recorded %) keys))
        matching? (and (seq keys) complete?
                       (= configured (select-keys recorded keys)))]
    {:status (cond
               (nil? recorded) :missing
               matching? :matching
               :else :stale)
     :configured (rt/redact configured)
     :recorded (rt/redact recorded)
     ;; Git common-directory identity is recorded by target!, but observing
     ;; status must not probe Git to recompute it.
     :git-common-dir :recorded-only}))

(defn- status-record-map? [value]
  (and (map? value)
       (every? (fn [[_ record]]
                 (and (map? record) (keyword? (:status record))))
               value)))

(defn- validate-status-state! [config state path]
  (let [fields [:tasks :jobs :effects]
        restrictions (if (map? state)
                       (->> (keys state) (filter status-restriction-key?))
                       [])
        task-keys-match? (and (map? (:tasks state))
                              (every? (fn [[key task]]
                                        (or (nil? (:key task)) (= key (:key task))))
                                      (:tasks state)))]
    (when-not (and (map? state)
                   (= :seedless/v1 (:schema state))
                   (map? (:identity state))
                   (contains? state :status)
                   (keyword? (:status state))
                   (status-record-map? (:tasks state))
                   (status-record-map? (:jobs state))
                   (status-record-map? (:effects state))
                   task-keys-match?
                   (every? #(or (nil? (get state %)) (map? (get state %))) restrictions))
      (rt/fail! "Seedless status summary found malformed state shape"
                {:path path
                 :fields (if (map? state)
                           (select-keys state (conj fields :schema :identity :status))
                           {:value-type (type state)})}))
    (let [identity (status-identity config state)]
      (when-not (= :matching (:status identity))
        (rt/fail! "Seedless status summary found state for another campaign/configuration"
                  {:path path
                   :expected (rt/redact (select-keys config status-identity-keys))
                   :actual (rt/redact (:identity state))}))
      state)))

(defn- read-status-edn [raw path label]
  (try
    (let [eof (Object.)
          reader (java.io.PushbackReader. (java.io.StringReader. raw))
          options {:readers {}
                   :default (fn [tag _]
                              (rt/fail! (str "Unsupported EDN tag in Seedless " label)
                                        {:tag tag :path path}))
                   :eof eof}
          value (edn/read options reader)
          trailing (edn/read options reader)]
      (when (identical? eof value)
        (rt/fail! (str "Seedless " label " is empty") {:path path}))
      (when-not (identical? eof trailing)
        (rt/fail! (str "Seedless " label " contains multiple EDN forms") {:path path}))
      value)
    (catch Exception e
      (rt/fail! (str "Seedless status summary found malformed " label)
                {:path path :cause (.getMessage e)}))))

(defn- sha256-bytes [bytes]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes))))

(defn- read-status-file [file path label]
  (try
    (let [bytes (Files/readAllBytes (.toPath file))
          decoder (doto (.newDecoder StandardCharsets/UTF_8)
                    (.onMalformedInput CodingErrorAction/REPORT)
                    (.onUnmappableCharacter CodingErrorAction/REPORT))]
      {:raw (.toString (.decode decoder (ByteBuffer/wrap bytes)))
       :digest (sha256-bytes bytes)})
    (catch Exception e
      (rt/fail! (str "Seedless status summary cannot read " label " file")
                {:path path :cause (.getMessage e)}))))

(defn- read-status-state! [config]
  (let [path (str (io/file (:state-dir config) "state.edn"))
        file (io/file path)]
    (when-not (.exists file)
      (rt/fail! "Seedless status summary requires an existing state file"
                {:path path :missing true}))
    (when-not (.isFile file)
      (rt/fail! "Seedless status summary cannot read state file"
                {:path path :not-file true}))
    (let [{:keys [raw digest]} (read-status-file file path "state")
          state (read-status-edn raw path "state")]
      (validate-status-state! config state path)
      {:state state :digest digest :file path})))

(defn- read-status-control [config]
  (let [path (str (io/file (:state-dir config) "control.edn"))
        file (io/file path)]
    (if-not (.exists file)
      {:file path :sha256 nil :available false :status :unavailable}
      (do
        (when-not (.isFile file)
          (rt/fail! "Seedless status summary found malformed control file"
                    {:path path :not-file true}))
        (let [{:keys [raw digest]} (read-status-file file path "control")
              value (read-status-edn raw path "control")]
          (when-not (and (map? value) (contains? value :paused) (boolean? (:paused value)))
            (rt/fail! "Seedless status summary found malformed control shape"
                      {:path path :expected {:paused :boolean} :actual value}))
          {:file path :sha256 digest :available true
           :status (if (:paused value) :paused :active) :paused (:paused value)})))))

(defn- task-error-summary [error]
  (cond
    (nil? error) nil
    (map? error)
    (let [details (:details error)]
      (rt/redact
       (select-keys (merge error (if (map? details) details {}))
                    [:message :effect :effect-id :effect-status :restricted :uncertain
                     :reason :remote-pr-refusal :main-effect :writer-ended? :outcome
                     :job :refusal :path :slice])))
    :else {:message (rt/redact (str error))}))

(defn- effect-reference-values [value]
  (cond
    (map? value)
    (mapcat (fn [[key nested]]
              (let [key-name (when (or (keyword? key) (string? key)) (name key))
                    effect-key? (and key-name
                                     (str/includes? (str/lower-case key-name) "effect"))]
                (concat (if effect-key?
                          (cond
                            (nonblank-string? nested) [nested]
                            (sequential? nested) (filter nonblank-string? nested)
                            :else [])
                          [])
                        (effect-reference-values nested))))
            value)
    (sequential? value) (mapcat effect-reference-values value)
    :else []))

(defn- task-effect-ids [task]
  (->> (effect-reference-values task)
       distinct
       sort
       vec))

(defn- linked-effect-summary [effects id]
  (let [record (get effects id)
        status (if (map? record) (or (:status record) :unknown) :missing)]
    {:id id :status status
     :restricted (= :restricted status)
     :uncertain (= :uncertain status)
     :pending (= :pending status)}))

(defn- effect-history? [record]
  (and (map? record)
       (some #(contains? record %) [:previous :prior :refusal :settlement
                                    :reconciliation :history :refusal-history
                                    :settlement-history])))

(defn- history-facts [value]
  (cond
    (map? value)
    (reduce-kv
     (fn [result key nested]
       (let [projected (history-facts nested)
             key-name (when (or (keyword? key) (string? key)) (name key))
             value (cond
                     (#{:result :output :raw-response} key) (content-identity nested)
                     (= :authorization key)
                     (if (map? nested)
                       {:present true :sha256 (rt/digest (pr-str nested))}
                       (if (and (string? nested) (= projected nested))
                         nested
                         {:present true :sha256 (rt/digest (pr-str nested))}))
                     key-name (if (= "[REDACTED]" (get (rt/redact {key nil}) key))
                                "[REDACTED]"
                                (if (or (map? projected) (sequential? projected))
                                  projected
                                  (rt/redact projected)))
                     :else projected)
             result (assoc result key value)]
         (if (= :reconciliation key)
           (assoc result :reconciliation-identity (content-identity nested))
           result)))
     {}
     value)
    (sequential? value) (mapv history-facts value)
    :else (rt/redact value)))

(defn- effect-history-summary [id record]
  (let [prior (if (map? (:prior record)) (:prior record) {})
        previous (if (map? (:previous record)) (:previous record) {})]
    {:id id
     :current-status (:status record)
     :prior-status (get-in record [:prior :status])
     :previous-status (get-in record [:previous :status])
     :prior (history-facts prior)
     :previous (history-facts previous)
     :reconciliation (if (map? (:reconciliation record))
                       (history-facts (:reconciliation record))
                       (rt/redact (:reconciliation record)))
     :reconciliation-identity (when (contains? record :reconciliation)
                                (content-identity (:reconciliation record)))
     :settlements (->> [(:settlement record)
                        (get-in record [:prior :settlement])
                        (get-in record [:previous :settlement])]
                       (filter map?)
                       (mapv history-facts))}))

(defn- recovery-history-summary [state-file state-digest [id record]]
  {:id id
   :kind (:kind record)
   :stage (:stage record)
   :effect (:effect record)
   :operation (:operation record)
   :outcome (:outcome record)
   :prior-effect-status (get-in record [:prior-effect :status])
   :authorization (history-facts (:authorization record))
   :authorization-present (contains? record :authorization)
   :authorization-sha256 (when (contains? record :authorization)
                           (rt/digest (pr-str (:authorization record))))
   :previous-local-guard (history-facts (:previous-local-guard record))
   :previous-shared-guard (history-facts (:previous-shared-guard record))
   :shared-retirement (history-facts (:shared-retirement record))
   :inspection (history-facts (:inspection record))
   :review (history-facts (:review record))
   :prior-effect (history-facts (:prior-effect record))
   :comment-outcome (history-facts (:comment-outcome record))
   :auth-diagnostic (history-facts (:auth-diagnostic record))
   :receipt-digests (history-facts (:receipt-digests record))
   :record {:state-file state-file :state-sha256 state-digest
            :recovery-key id}})

(defn- effects-summary [state state-file state-digest]
  (let [effects (or (:effects state) {})
        current (->> effects
                     (sort-by (comp str key))
                     (mapv (fn [[id record]]
                             (cond-> {:id id :status (:status record)
                                      :result-present (contains? record :result)
                                      :reconciliation-present (contains? record :reconciliation)
                                      :result-identity (when (contains? record :result)
                                                         (content-identity (:result record)))
                                      :reconciliation-identity
                                      (when (contains? record :reconciliation)
                                        (content-identity (:reconciliation record)))
                                      :record {:state-file state-file :state-sha256 state-digest
                                               :effect-id id}}
                               (some? (:message record))
                               (assoc :message (rt/redact (:message record)))))))
        history (->> effects
                     (filter (fn [[_ record]] (effect-history? record)))
                     (sort-by (comp str key))
                     (mapv (fn [[id record]]
                             (assoc (effect-history-summary id record)
                                    :record {:state-file state-file :state-sha256 state-digest
                                             :effect-id id}))))
        recoveries (->> (or (:operator-api-recoveries state) {})
                        (sort-by (comp str key))
                        (mapv #(recovery-history-summary state-file state-digest %)))
        retained (select-keys state [:effect-history :refusal-history :settlement-history
                                     :refusals :settlements])]
    {:scope :recorded-effects-only
     :current current
     :history history
     :operator-api-recoveries recoveries
     :refusal-settlement-history {:effects history
                                  :operator-api-recoveries recoveries
                                  :recorded (history-facts retained)}}))

(defn- task-status-summary [state effects-view state-file state-digest [map-key task]]
  (let [key map-key
        effects (or (:effects state) {})
        ids (task-effect-ids task)
        linked (mapv #(linked-effect-summary effects %) ids)
        error (task-error-summary (:error task))
        reporting-error (task-error-summary (:reporting-error task))
        statuses (set (concat (keep :status linked)
                              [(:effect-status error) (:effect-status reporting-error)]))]
    {:key key
     :status (:status task)
     :resume-status (:resume-status task)
     :history {:restricted (or (:restricted error) (:restricted reporting-error)
                               (contains? statuses :restricted))
               :uncertain (or (:uncertain error) (:uncertain reporting-error)
                              (contains? statuses :uncertain))
               :pending (contains? statuses :pending)}
     :error error
     :reporting-error reporting-error
     :effects linked
     :effect-history (->> (:history effects-view)
                          (filter #(some #{(:id %)} ids))
                          vec)
     :record {:state-file state-file :state-sha256 state-digest :task-key key}}))

(defn- task-record [state-file state-digest [map-key task]]
  {:key map-key
   :status (:status task)
   :record {:state-file state-file :state-sha256 state-digest
            :task-key map-key}})

(defn- job-refusal-summary [refusal]
  (if-not (map? refusal)
    (rt/redact refusal)
    (let [item (select-keys (:item refusal)
                            [:command :cwd :exitCode :id :status :type])]
      (cond-> (rt/redact (select-keys refusal [:id :status :job :output-sha256 :denial-lines]))
        (seq item) (assoc :item (rt/redact item))))))

(defn- job-status-summary [state-file state-digest [job record]]
  (let [identity (select-keys record [:thread :turn :model :prompt-sha256
                                      :input-sha256 :output-sha256 :turn-terminal?
                                      :active-items :message :restricted :tool-refusals])]
    {:job job
     :status (:status record)
     :mode (:mode record)
     :thread (:thread record)
     :turn (:turn record)
     :model (:model record)
     :turn-terminal? (:turn-terminal? record)
     :active-items (rt/redact (:active-items record))
     :message (some-> (:message record) rt/redact)
     :restricted (:restricted record)
     :tool-refusals (mapv job-refusal-summary (or (:tool-refusals record) []))
     :identity (rt/redact identity)
     :record {:state-file state-file :state-sha256 state-digest :job-key job}}))

(defn- native-restrictions [state]
  (rt/redact (into {}
                   (for [[key value] state
                         :when (status-restriction-key? key)]
                     [key value]))))

(defn- blocker-summary [state state-file state-digest key]
  (let [value (get state key)]
    (if-not (map? value)
      {:present false
       :record {:state-file state-file :state-sha256 state-digest :field key}}
      {:present true
       :status (:status value)
       :result (when (or (keyword? (:result value)) (string? (:result value))
                         (boolean? (:result value)) (number? (:result value)))
                 (rt/redact (:result value)))
       :result-present (contains? value :result)
       :restricted (:restricted value)
       :message (some-> (:message value) rt/redact)
       :missing-evidence (some-> (:missing-evidence value) rt/redact)
       :effect-refs (task-effect-ids value)
       :error (or (task-error-summary (:error value))
                  (task-error-summary (:validation-error value)))
       :record {:state-file state-file :state-sha256 state-digest :field key}})))

(defn status-summary
  "Build a compact, observational projection from one native Seedless state.

  This function reports recorded state only. It does not construct context,
  probe Git or providers, inspect processes, acquire a lock, import shared
  coordination state, or write local state/logs."
  [config state state-digest control]
  (let [state-file (str (io/file (:state-dir config) "state.edn"))
        tasks (->> (:tasks state) (sort-by (comp str key)) vec)
        task-records (mapv #(task-record state-file state-digest %) tasks)
        effects (effects-summary state state-file state-digest)
        attention (->> tasks
                       (filter #(= :attention (:status (second %))))
                       (mapv #(task-status-summary state effects state-file state-digest %)))
        statuses (frequencies (map :status task-records))
        jobs (->> (:jobs state)
                  (sort-by (comp str key))
                  (mapv #(job-status-summary state-file state-digest %)))
        identity (status-identity config state)
        restrictions (native-restrictions state)
        blockers (into {}
                       (map (fn [key] [key (blocker-summary state state-file state-digest key)])
                            [:planning :completion :interruption :diagnosis-blocker]))]
    {:schema :seedless/v1
     :identity identity
     :state {:file state-file :sha256 state-digest
             :recorded-status (:status state)
             :task-count (count task-records)
             :counts statuses
             :record {:state-file state-file :state-sha256 state-digest}}
     :tasks task-records
     :attention attention
     :jobs {:scope :recorded-model-jobs-only :count (count jobs) :records jobs}
     :effects effects
     :recorded-blockers blockers
     :operator-controls {:local-control control
                         :native-restrictions restrictions
                         :github {:status :unavailable
                                  :reason :no-provider-read-in-read-only-summary}}
     :liveness {:processes :unverified
                :service :unverified
                :basis :recorded-local-state-only}}))

(defn status-summary! [config]
  (let [{:keys [state digest]} (read-status-state! config)]
    (status-summary config state digest (read-status-control config))))

(defn context! [config]
  (let [directory (io/file (:state-dir config)) _ (.mkdirs directory)
        path (str (io/file directory "state.edn"))
        startup {:config config :state-dir (str directory) :state-file path
                 :log-file (str (io/file directory "events.jsonl")) :log-lock (Object.) :processes (atom #{})}
        identity (target! startup)
        state (if (.exists (io/file path)) (rt/read-edn path)
                  {:schema :seedless/v1 :identity identity :status :new :tasks {} :effects {} :jobs {}})
        _ (when-not (and (= :seedless/v1 (:schema state)) (= identity (:identity state)))
            (rt/fail! "State belongs to a different Seedless campaign/configuration" {}))
        ctx (assoc startup :identity identity :state (atom state) :owns-lock? (atom false)
                   :control-file (str (io/file directory "control.edn")))
        common (:git-common-dir identity)]
    (merge ctx (rt/coordination common config))))

(defn owned-cleanup! [ctx reason]
  (when (and (:owns-lock? ctx) @(:owns-lock? ctx))
    (let [processes @(:processes ctx)
          observation {:reason reason :time (str (java.time.Instant/now))
                       :known-processes (mapv #(.pid ^Process %) processes)
                       :descendant-coverage :unknown :inspection-required true}]
      ;; No global/descendant discovery: signal only process objects this invocation owns.
      (rt/update! ctx assoc :status :attention :interruption observation)
      (try
        (doseq [process processes] (.destroy ^Process process))
        (rt/event! ctx :owned-cleanup observation)
        (catch Exception e
          (let [restricted (or (instance? SecurityException e) (rt/authorization-denied? (.getMessage e)))]
            (rt/update! ctx update :interruption assoc :restricted (boolean restricted) :message (.getMessage e))
            (rt/event! ctx :owned-cleanup-failed {:restricted (boolean restricted) :message (.getMessage e)})))))))

(defn with-current-lock [ctx operation]
  (rt/with-lock ctx
    #(do
       (when (.exists (io/file (:state-file ctx)))
         (let [state (rt/read-edn (:state-file ctx))]
           (when-not (and (= :seedless/v1 (:schema state)) (= (:identity ctx) (:identity state)))
             (rt/fail! "State identity changed before lock ownership" {}))
           (reset! (:state ctx) state)))
       (rt/import-state-coordination! ctx)
       (when (:owns-lock? ctx) (reset! (:owns-lock? ctx) true))
       (try (operation)
            (finally
              (when (seq @(:processes ctx)) (owned-cleanup! ctx :operation-exit))
              (when (:owns-lock? ctx) (reset! (:owns-lock? ctx) false)))))))

(defn run-campaign! [ctx resume?]
  (with-current-lock ctx
    #(try
       (doseq [t (workflow/tasks ctx)] (workflow/assert-no-pending-main! ctx t))
       (when (:interruption @(:state ctx)) (rt/fail! "Inspect retained controller interruption before recovery" {}))
       (when resume? (rt/write-edn! (:control-file ctx) {:paused false}))
       (if (and (= :complete (:status @(:state ctx)))
                (= (get-in @(:state ctx) [:completion :base]) (workflow/fresh-main! ctx)))
         :complete
         (workflow/campaign! ctx))
       (catch Exception e
         (rt/update! ctx assoc :status :attention)
         (throw e)))))

(defn retry! [ctx key file]
  (let [inspection (rt/read-edn file)]
    (when-not (and (string? (:evidence inspection)) (not (str/blank? (:evidence inspection))))
      (rt/fail! "Retry needs a record of the inspected interruption/failure" {}))
    (doseq [t (workflow/tasks ctx)] (workflow/assert-no-pending-main! ctx t))
    (if (= "audit" key)
      (let [planning (:planning @(:state ctx))
            interruption (:interruption @(:state ctx))
            diagnosis-blocker (:diagnosis-blocker @(:state ctx))
            effect (get-in @(:state ctx) [:effects (:effect planning)])
            diagnosis-effect (get-in @(:state ctx) [:effects (:effect diagnosis-blocker)])]
        (when (or (= :restricted (:status effect)) (get-in @(:state ctx) [:completion :restricted])
                  (:restricted interruption) (:restricted diagnosis-blocker)
                  (= :restricted (:status diagnosis-effect)))
          (rt/fail! "Restricted planning/completion actions cannot be retried" {:restricted true}))
        (when (and diagnosis-blocker (not= :done (:status diagnosis-effect)))
          (rt/fail! "Failed diagnosis effect needs reconciliation before retry"
                    {:effect (:effect diagnosis-blocker)}))
        (when-not (or planning interruption diagnosis-blocker
                      (= :failed (get-in @(:state ctx) [:completion :result])))
          (rt/fail! "No inspected planning/completion failure is pending" {}))
        (rt/update! ctx #(cond-> (-> % (update :inspections (fnil conj [])
                                              {:planning planning :completion (:completion %)
                                               :interruption interruption :diagnosis-blocker diagnosis-blocker
                                               :inspection inspection})
                                     (assoc :round (max (or (:round %) 0) (or (:round planning) 0)) :planning nil)
                                     (dissoc :completion :interruption))
                          diagnosis-blocker (dissoc :diagnosis-blocker :last-discovery-key))))
      (let [task (workflow/task ctx key)]
        (when-not (#{:attention :acceptance-pending} (:status task))
          (rt/fail! "Only Attention or acceptance-pending work needs an inspected retry" {}))
        (when (get-in task [:error :details :restricted])
          (rt/fail! "Restricted work cannot be retried" {:restricted true}))
        (doseq [id (distinct (filter string?
                                     [(get-in task [:error :details :effect])
                                      (get-in task [:error :details :effect-id])
                                      (get-in task [:reporting-error :details :effect])
                                      (get-in task [:reporting-error :details :effect-id])
                                      (:compiler-progress-intent task)
                                      (get-in task [:compiler-result :diagnosis :effect])]))
                :let [effect (get-in @(:state ctx) [:effects id])]
                :when (not= :done (:status effect))]
          (rt/fail! "Failed task effect needs reconciliation before retry"
                    {:effect id :restricted (= :restricted (:status effect))}))
        (when (:merged task)
          (let [tracking-key (str "project-status/" (get-in @(:state ctx) [:project :id]) "/" (get-in task [:issue :node_id]))
                ids [(str "close/" key) (get-in @(:state ctx) [:tracking-intents tracking-key])]]
            (doseq [id ids :let [effect (get-in @(:state ctx) [:effects id])]
                    :when (#{:pending :uncertain :restricted} (:status effect))]
              (rt/fail! "Merged slice finalization needs effect reconciliation" {:effect id :restricted (= :restricted (:status effect))}))))
        (let [id (or (:merge-effect task) (workflow/merge-effect-id task))
              record (:inspection (rt/main-merge-state ctx id))]
          (when (and (get-in task [:error :details :main-effect]) (not (:merged task)) (nil? record))
            (rt/fail! "Main failure needs original-owner journal inspection before retry" {:effect id :main-effect true}))
          (when (= :not-applied (get-in record [:inspection :outcome]))
            (let [effect (get-in @(:state ctx) [:effects id])
                  digest (rt/digest (pr-str record))
                  restriction (rt/digest (pr-str ["gh-pr-merge" (get-in ctx [:config :repository])]))
                  next-id (str id "/inspection/" digest)]
              (when (or (= :restricted (:status effect))
                        (get-in @(:state ctx) [:restricted-commands restriction])
                        (rt/shared-restriction ctx :commands restriction))
                (rt/fail! "Original repository merge restriction forbids a successor intent" {:effect id :restricted true}))
              (when-not (and (= (assoc (select-keys task [:base :candidate :tree]) :id id :method :merge)
                                (select-keys (:intent record) [:id :base :candidate :tree :method]))
                             (= :done (:status effect)) (= digest (:main-inspection-digest effect)))
                (rt/fail! "Not-applied main inspection needs matching local effect reconciliation" {:effect id :main-effect true}))
              (when (get-in @(:state ctx) [:effects next-id])
                (rt/fail! "Inspected successor already has a retained effect; inspect that outcome" {:effect next-id :main-effect true}))
              (workflow/task! ctx key :merge-recovery (assoc (select-keys task [:base :candidate :tree]) :id next-id)))))
        (let [status (cond
                       (or (:merged task) (:merge-recovery (workflow/task ctx key))) :ci
                       (and (true? (get-in task [:error :details :compiler-repair-required]))
                            (= (select-keys task [:base :candidate :tree])
                               (select-keys (get-in task [:error :details]) [:base :candidate :tree]))) :repair
                       (or (= :acceptance-pending (:status task))
                           (= :acceptance-pending (:resume-status task))
                           (true? (:pending-evidence-resume task))) :acceptance-pending
                       (#{:ci :merge} (:resume-status task)) :ci
                       :else :repair)]
          (rt/update! ctx update-in [:tasks key :history] (fnil conj []) {:error (:error task) :inspection inspection})
          (rt/update! ctx update-in [:tasks key]
                      #(cond-> (assoc % :feedback inspection :status status)
                         (= :repair status) (dissoc :pending-evidence-resume :compiler-progress-resume)
                         (= :acceptance-pending status)
                         (assoc :compiler-progress-resume true :compiler-investigation-planned nil
                                :compiler-progress-inspection (rt/digest (pr-str (:history %)))))))))))

(defn dispatch! [ctx command args]
  (case command
    "doctor" (with-current-lock ctx #(shared/doctor! ctx))
    "status" (println (pr-str @(:state ctx)))
    "pause" (rt/write-edn! (:control-file ctx) {:paused true})
    "setup" (with-current-lock ctx #(workflow/with-registration! ctx
                                      (fn [] (gh/setup! ctx)
                                        (gh/status! ctx (workflow/campaign-issue! ctx) "Proposed"))))
    "coordinate" (with-current-lock ctx #(println "Retained state registered; no workers or GitHub writes."))
    "run" (run-campaign! ctx false)
    "resume" (run-campaign! ctx true)
    "retry" (with-current-lock ctx #(retry! ctx (first args) (second args)))
    "reconcile" (with-current-lock ctx #(shared/reconcile! ctx (first args) (second args)))
    "reconcile-main" (with-current-lock ctx #(rt/reconcile-main-merge! ctx (first args) (rt/read-edn (second args))))
    "release-writer" (with-current-lock ctx #(rt/inspect-release-paths! ctx (rt/read-edn (first args))
                                                                        (:evidence (rt/read-edn (second args)))))
    (rt/fail! "Usage: clojure -M:run doctor|setup|coordinate|run|status|status-summary|pause|resume|retry|reconcile|reconcile-main|release-writer CONFIG [KEY FILE]" {})))

(defn -main [& [command path & args]]
  (try
    (when-not path (rt/fail! "Provide a command and configuration file" {}))
    (let [config (config! path)]
      (if (= command "status-summary")
        (do
          (when (seq args) (rt/fail! "status-summary takes no additional arguments" {:args args}))
          (println (pr-str (status-summary! config))))
        (let [ctx (context! config)]
          (.addShutdownHook (Runtime/getRuntime)
                            (Thread. ^Runnable
                             (fn [] (owned-cleanup! ctx :jvm-shutdown))))
          (try
            (rt/event! ctx :startup {:command command :config (dissoc (:config ctx) :model :models)})
            (dispatch! ctx command args)
            (catch Exception e
              (rt/event! ctx :fatal {:message (.getMessage e) :details (ex-data e)})
              (throw e))))))
    (catch Exception e
      (binding [*out* *err*] (println (.getMessage e)))
      (shutdown-agents)
      (System/exit 1)))
  (shutdown-agents))
