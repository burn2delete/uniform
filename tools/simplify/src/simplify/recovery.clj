(ns simplify.recovery
  "Inspect a completed author report without overriding an actual refusal."
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.codex :as codex]
            [simplify.runtime :as rt]))

(defn require! [condition message]
  (when-not condition (rt/fail! message {:writer-ended? true})))

(defn file-digest [path]
  (.formatHex (java.util.HexFormat/of)
              (.digest (java.security.MessageDigest/getInstance "SHA-256")
                       (java.nio.file.Files/readAllBytes (.toPath (io/file path))))))

(defn error-digest [task] (rt/digest (pr-str (:error task))))

(defn- path-output! [ctx cwd argv]
  (let [key (rt/digest (pr-str argv))]
    (require! (not (get-in @(:state ctx) [:restricted-commands key])) "Source path inspection is locally refused")
    (rt/assert-authorized! ctx :command key)
    (let [result (rt/command ctx cwd argv)]
      (when (rt/authorization-denied? (:err result))
        (rt/remember-denial! ctx :command key {:argv argv :result result})
        (rt/fail! "Authored inspection encountered a refusal" {:restricted true :writer-ended? true}))
      (require! (zero? (:exit result)) "Cannot inspect the authored delta")
      (:out result))))

(defn changes [ctx cwd]
  (vec (distinct
        (remove str/blank?
                (str/split
                 (apply str
                        (for [argv [["git" "diff" "--name-only" "--no-renames" "-z" "HEAD" "--"]
                                    ["git" "ls-files" "--others" "--exclude-standard" "-z"]]]
                          (path-output! ctx cwd argv))) #"\u0000")))))

(defn owned-file! [task path]
  (rt/safe-path! path)
  (require! (some #(rt/covers? % path) (:paths task)) "Authored delta contains a foreign path")
  (let [root (.toPath (io/file (rt/canonical (:worktree task))))
        file (io/file (:worktree task) path)]
    (require! (.startsWith (.toPath (io/file (rt/canonical file))) root) "Authored path escapes its worktree")
    (require! (not (java.nio.file.Files/isSymbolicLink (.toPath file))) "Authored evidence cannot substitute a symlink")
    (require! (.isFile file) "Authored evidence needs the preserved file bytes")
    file))

(defn native-evidence!
  ([ctx task job-id] (native-evidence! ctx task job-id nil))
  ([ctx task job-id preserved-failures]
  (require! (and (:log-file ctx) (.isFile (io/file (:log-file ctx)))) "Original native action evidence is missing")
  (let [events (with-open [reader (io/reader (:log-file ctx))]
                 (mapv #(json/read-str % :key-fn keyword) (line-seq reader)))
        first-job (first (keep-indexed #(when (= job-id (:job %2)) %1) events))
        _ (require! (some? first-job) "Original job has no native action evidence")
        start (last (keep-indexed
                     (fn [index event]
                       (when (and (< index first-job) (= "command-start" (:event event))
                                  (= (rt/canonical (:worktree task)) (some-> (:cwd event) rt/canonical))
                                  (= ["git" "status" "--porcelain"] (:argv event))) index)) events))
        _ (require! (some? start) "Original author clean-start guard is unproven")
        end (first (for [index (range (inc start) first-job)
                         :let [event (nth events index)]
                         :when (and (= "command-end" (:event event))
                                    (= ["git" "status" "--porcelain"] (:argv event)))] index))
        _ (require! (some? end) "Original clean-start command did not complete")
        _ (require! (and (zero? (:exit (nth events end)))
                         (not-any? #(and (= "command-output" (:event %))
                                         (rt/authorization-denied? (:line %))) (subvec events (inc start) end))
                         (not-any? #(and (= "command-output" (:event %)) (= "stdout" (:stream %))
                                         (not (str/blank? (:line %)))) (subvec events (inc start) end)))
                    "Original worktree was dirty or clean-start evidence is ambiguous")
        job-events (filterv #(and (= job-id (:job %))
                                  (#{"rpc-send" "rpc-receive" "app-server-stderr"} (:event %))) events)
        terminal (get-in @(:state ctx) [:jobs job-id :turn])
        _ (require! (some #(and (= "turn/completed" (get-in % [:message :method]))
                                (= terminal (get-in % [:message :params :turn :id]))
                                (= "completed" (get-in % [:message :params :turn :status]))) job-events)
                    "Original completed turn has no matching terminal event")
        _ (require! (not-any? #(and (= "rpc-receive" (:event %))
                                    (get-in % [:message :id]) (get-in % [:message :method])) job-events)
                    "Original job encountered a privileged or unknown request")
        pending (reduce (fn [active event]
                          (let [method (get-in event [:message :method])
                                item (get-in event [:message :params :item])]
                            (cond
                              (and (= "item/started" method)
                                   (not (#{"agentMessage" "reasoning" "userMessage" "plan"} (:type item))))
                              (assoc active (:id item) item)
                              (= "item/completed" method) (dissoc active (:id item))
                              :else active))) {} job-events)
        _ (require! (empty? pending) "Original native action evidence contains an unfinished action")
        actions (vec (for [event job-events
                           :when (= "item/completed" (get-in event [:message :method]))
                           :let [item (get-in event [:message :params :item])]
                           :when (not (#{"agentMessage" "reasoning" "userMessage" "plan"} (:type item)))] item))]
    (when (some? preserved-failures)
      (let [failures (mapv (fn [action]
                            {:id (:id action) :command (:command action) :exit (:exitCode action)
                             :output-sha256 (when (string? (:aggregatedOutput action))
                                              (rt/digest (:aggregatedOutput action)))})
                          (filter #(and (= "commandExecution" (:type %)) (not= 0 (:exitCode %))) actions))]
        (require! (= failures preserved-failures) "Failed semantic actions do not match the preserved nonpass evidence")))
    (doseq [action actions]
      (case (:type action)
        "commandExecution"
        (do (require! (and (or (= 0 (:exitCode action))
                              (and (some? preserved-failures) (= 1 (:exitCode action))))
                           (= "completed" (:status action))
                           (string? (:command action)) (string? (:aggregatedOutput action)))
                      "Original command has unknown or unfinished execution")
            (require! (not (rt/authorization-denied? (:aggregatedOutput action)))
                      "Original command includes refusal text requiring inspection"))
        "fileChange"
        (do (require! (and (= "completed" (:status action)) (not (:error action)) (seq (:changes action)))
                      "Original file action is failed or unknown")
            (doseq [change (:changes action)]
              (let [path (io/file (:path change))
                    absolute (if (.isAbsolute path) path (io/file (:worktree task) (:path change)))
                    relative (str (.relativize (.toPath (io/file (rt/canonical (:worktree task))))
                                               (.toPath (io/file (rt/canonical absolute)))))]
                (owned-file! task relative))))
        (rt/fail! "Original job includes an unknown action" {:writer-ended? true :type (:type action)})))
    (cond-> {:clean-start (select-keys (nth events start) [:time :cwd :argv])
             :clean-end (select-keys (nth events end) [:time :exit])
             :completed-action-count (count actions) :job-evidence-sha256 (rt/digest (pr-str job-events))}
      (some? preserved-failures) (assoc :preserved-semantic-nonpasses preserved-failures)))))

(defn verified-dependencies! [ctx task receipt]
  (let [dependencies (:verified-source-dependencies receipt)
        _ (require! (seq dependencies) "An actual verified external source merge is required")
        shared (rt/coordinate! ctx (fn [state] [state (select-keys state [:history :main-effects])]))
        _ (require! (not-any? #(= (:state-file ctx) (:state-file %)) (vals (:main-effects shared)))
                    "The original controller has an unresolved Main operation")
        history (:history shared)
        main (rt/fresh-main! ctx)
        repo (get-in ctx [:config :repo-dir])]
    (doseq [{:keys [owner-state-file intent-id commit candidate tree history-sha256 source-paths]} dependencies]
      (require! (and (string? owner-state-file) (string? intent-id)
                     (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %)) [commit candidate tree])
                     (seq source-paths) (every? string? source-paths)) "Dependency merge identity is incomplete")
      (let [entry (some #(when (and (= :main-verified (:kind %))
                                    (= owner-state-file (get-in % [:intent :state-file]))
                                    (= intent-id (get-in % [:intent :id]))
                                    (= commit (get-in % [:actual :commit]))
                                    (= candidate (get-in % [:intent :candidate]))
                                    (= tree (get-in % [:intent :tree]) (get-in % [:actual :tree]))) %) history)]
        (require! (and entry (= history-sha256 (rt/digest (pr-str entry)))) "Dependency has no matching verified Main history")
        (require! (= tree (rt/git ctx repo "rev-parse" (str commit "^{tree}"))) "Dependency Git tree differs")
        (rt/git ctx repo "merge-base" "--is-ancestor" commit main)
        (require! (not= commit (:base task)) "Dependency does not advance the retained source base")
        (rt/git ctx repo "merge-base" "--is-ancestor" (:base task) commit)
        (let [changed (set (remove str/blank?
                                   (str/split (path-output! ctx repo ["git" "diff" "--name-only" "--no-renames" "-z"
                                                                     (get-in entry [:intent :base]) candidate "--"]) #"\u0000")))]
          (doseq [path source-paths]
            (rt/safe-path! path)
            (require! (contains? changed path) "The recorded dependency did not change its claimed source"))
          (require! (some (fn [path] (not-any? #(rt/covers? % path) (:paths task))) source-paths)
                    "The claimed dependency is not outside the retained author scope"))))
    ;; Bind the verified dependency, not a reservation on a moving Main.
    ;; Native preparation independently imports and checks its fresh base.
    {:merges dependencies}))

(defn- inspect-report! [ctx task evidence-file dependency?]
  (let [file (io/file (rt/canonical evidence-file))
        directory (.toPath (io/file (rt/canonical (:state-dir ctx))))
        _ (require! (.startsWith (.toPath file) directory) "Inspection evidence must be retained by this controller")
        receipt (rt/read-edn file)
        job-id (:original-job receipt)
        job (get-in @(:state ctx) [:jobs job-id])
        output (:output job)
        classification (:operator-classification receipt)
        _ (require! (and (= (if dependency? :verified-external-source-dependency :controller-owned-verification-unrun)
                            (:kind classification))
                         (= :controller-operator (:inspector classification))
                         (= (rt/digest (pr-str output)) (:original-output-sha256 classification))
                         (true? (:no-unresolved-authority classification))
                         (true? (:no-unresolved-semantics classification))
                         (string? (:basis classification)) (not (str/blank? (:basis classification))))
                    "An exact-output operator classification for this recovery phase is required")
        _ (when dependency?
            (require! (and (= :source-development-current (:phase classification))
                           (vector? (:preserved-failed-actions receipt))
                           (vector? (:preserved-nonclaims receipt)) (seq (:preserved-nonclaims receipt)))
                      "Dependency recovery must retain semantic nonpasses and declare its development-only phase"))
        prompt-file (io/file (:state-dir ctx) (str job-id ".prompt.edn"))
        _ (require! (.isFile prompt-file) "Original author prompt is missing")
        prompt (rt/read-edn prompt-file)
        text (:input prompt)
        position (when (string? text) (str/index-of text "\n{"))
        _ (require! (some? position) "Original task identity is not present in its prompt")
        prompted (edn/read-string {:readers {} :default (fn [tag _] (rt/fail! "Unsupported prompt tag" {:tag tag}))}
                                 (subs text (inc position)))
        _ (require! (= (select-keys task [:key :task :task-path :paths])
                       (select-keys prompted [:key :task :task-path :paths])) "Original prompt belongs to a different task or scope")
        _ (require! (= (:prompt-sha256 job) (rt/digest text)) "Original prompt identity changed")
        _ (require! (and (= :completed (:status job)) (= :write (:mode job))
                         (true? (:turn-terminal? job)) (:turn job) (:thread job)
                         (or (contains? job :active-items) dependency?) (empty? (:active-items job))
                         (not (:restricted job)) (empty? (:tool-refusals job)))
                    "Original author has a refusal, unfinished action or unknown terminal state")
        _ (require! (= (rt/canonical (:cwd job)) (rt/canonical (:worktree task))) "Original author worktree differs")
        _ (require! (and (= "Implementation requires attention" (get-in task [:error :message]))
                         (= (not dependency?) (get-in task [:error :details :restricted]))
                         (= (if dependency? "needs_input" "restricted") (:outcome output))
                         (= output (get-in task [:error :details :output])))
                    "This is not the original report eligible for the requested recovery phase")
        _ (require! (= (select-keys task [:base :candidate :tree]) (select-keys receipt [:base :candidate :tree])) "Authored evidence has stale identities")
        _ (require! (= (:key task) (:task-key receipt)) "Authored evidence belongs to another task")
        _ (require! (= job (:original-job-state receipt)) "Original terminal job evidence changed")
        _ (require! (= (:error task) (:original-task-error receipt)) "Original task error changed")
        native-proof (when dependency? (native-evidence! ctx task job-id (:preserved-failed-actions receipt)))
        delta (changes ctx (:worktree task))
        _ (require! (seq delta) "No owned authored change remains")
        _ (require! (= (set delta) (set (:observed-changed-paths receipt)) (set (keys (:authored-files receipt))))
                    "Authored delta changed or contains foreign changes")
        files (into (sorted-map)
                    (for [path delta
                          :let [file (owned-file! task path)
                                saved (get-in receipt [:authored-files path])
                                bytes (.decode (java.util.Base64/getDecoder) ^String (:base64 saved))]]
                      (do (require! (= (:sha256 saved) (file-digest file)
                                       (.formatHex (java.util.HexFormat/of) (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes)))
                                    "Preserved authored bytes changed")
                          (when dependency?
                            (require! (= (:executable saved) (.canExecute file)) "Preserved authored file mode changed")
                            (let [provenance (get-in receipt [:source-provenance path])]
                              (require! (and (string? (:basis provenance)) (not (str/blank? (:basis provenance)))
                                             (= (:sha256 saved) (:sha256 provenance)))
                                        "Each retained source file needs its exact authored or supplemental provenance")))
                          [path {:sha256 (:sha256 saved) :executable (.canExecute file)}])))
        commands (vec (distinct (concat (get-in ctx [:config :checks]) (:checks output))))]
    (require! (= (or (:candidate task) (:base task)) (rt/git ctx (:worktree task) "rev-parse" "HEAD")) "Original authored HEAD changed")
    (require! (= (:branch task) (rt/git ctx (:worktree task) "symbolic-ref" "--short" "HEAD")) "Original authored branch changed")
    (require! (= (rt/common-git-directory (:worktree task)) (rt/common-git-directory (get-in ctx [:config :repo-dir])))
              "Original authored repository differs")
    (require! (and (seq commands) (every? #(and (vector? %) (seq %) (every? string? %)
                                               (contains? (get-in ctx [:config :check-executables]) (first %))) commands))
              "Original proposed checks are not in the configured toolset")
    (doseq [argv commands]
      (require! (not (get-in @(:state ctx) [:restricted-checks (codex/check-key ctx argv)])) "Original check is locally refused")
      (rt/assert-authorized! ctx :check (codex/check-key ctx argv))
      (require! (not (get-in @(:state ctx) [:restricted-commands (rt/digest (pr-str argv))])) "Original command is locally refused")
      (rt/assert-authorized! ctx :command (rt/digest (pr-str argv))))
    (require! (not (rt/coordinate! ctx (fn [state]
                                       [state (some #(and (= (:state-file ctx) (:state-file %)) (= job-id (:job %)))
                                                    (mapcat vals (vals (:restrictions state))))])))
              "Original job has a shared retained refusal")
    (cond-> {:kind (if dependency? :inspected-source-dependency-resolution :inspected-reported-implementation)
     :stage :queued :job job-id
     :thread (:thread job) :turn (:turn job) :original-error-sha256 (error-digest task)
     :output-sha256 (rt/digest (pr-str output)) :file-sha256 files :commands commands
     :operator-classification classification
     :evidence-file (str file) :evidence-sha256 (file-digest file)
     :native-evidence (or native-proof (native-evidence! ctx task job-id))}
      dependency? (assoc :verified-source-dependencies (verified-dependencies! ctx task receipt)
                         :preserved-nonclaims (:preserved-nonclaims receipt)))))

(defn inspect! [ctx task evidence-file] (inspect-report! ctx task evidence-file false))

(defn inspect-dependency! [ctx task evidence-file] (inspect-report! ctx task evidence-file true))

(defn validate-queued! [ctx task]
  (let [stored (:inspected-authored task)
        observed (if (= :inspected-source-dependency-resolution (:kind stored))
                   (inspect-dependency! ctx task (:evidence-file stored))
                   (inspect! ctx task (:evidence-file stored)))]
    (require! (= stored observed) "Inspected authored continuation is stale")
    observed))

(defn review-permitted? [task]
  (let [inspection (:inspected-authored task)]
    (and (= :inspected-reported-implementation (:kind inspection)) (= :checked (:stage inspection))
         (= (:original-error-sha256 inspection) (error-digest task))
         (= (:checked-implementation-job inspection) (:implementation-job task))
         (= (select-keys task [:base :candidate :tree]) (:checked-candidate inspection))
         (= :passed (get-in task [:checks :result])))))
