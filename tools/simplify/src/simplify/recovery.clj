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

(defn- source-command! [ctx cwd argv exits]
  (let [key (rt/digest (pr-str argv))]
    (require! (not (get-in @(:state ctx) [:restricted-commands key])) "Source path inspection is locally refused")
    (rt/assert-authorized! ctx :command key)
    (let [result (rt/command ctx cwd argv)]
      (when (rt/authorization-denied? (:err result))
        (rt/remember-denial! ctx :command key {:argv argv :result result})
        (rt/fail! "Authored inspection encountered a refusal" {:restricted true :writer-ended? true}))
      (require! (contains? exits (:exit result)) "Cannot inspect the authored delta")
      result)))

(defn- path-output! [ctx cwd argv]
  (:out (source-command! ctx cwd argv #{0})))

(defn changes [ctx cwd]
  (vec (distinct
        (remove empty?
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
                           (if (and (some? preserved-failures) (= 1 (:exitCode action)))
                             (#{"completed" "failed"} (:status action))
                             (= "completed" (:status action)))
                           (string? (:command action)) (string? (:aggregatedOutput action)))
                      "Original command has unknown or unfinished execution")
            (require! (not (if (some? preserved-failures)
                             (seq (codex/model-command-denial-lines action))
                             (rt/authorization-denied? (:aggregatedOutput action))))
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

(defn- referenced-evidence! [{:keys [path sha256]}]
  (require! (and (string? path) (.isFile (io/file path))
                 (= sha256 (file-digest path))) "Dependency source evidence is missing or changed")
  (rt/read-edn path))

(defn- source-status [shared owners paths]
  {:writers (into {} (filter (fn [[_ writer]] (and (contains? owners (:state-file writer))
                                                    (rt/overlap? paths (:paths writer)))) (:writers shared)))
   :refusals (into {} (for [[kind records] (:restrictions shared)
                            [key record] records :when (contains? owners (:state-file record))]
                        [[kind key] record]))})

(defn- admitted-source! [ctx shared candidate tree source-paths phase]
  (let [admission (referenced-evidence! (:acceptance-evidence phase))
        subject (:subject admission) paths (:paths phase) owners (set (:owner-state-files subject))
        snapshot (referenced-evidence! (:status-evidence phase))
        required-nonclaims #{:no-retroactive-source-ownership :no-standalone-held-source-admission
                             :no-release :no-migration-authority}
        live (source-status shared owners paths)]
    (require! (and (= :source-only-admission (:kind admission))
                   (= :accepted-source-only (:disposition admission))
                   (= candidate (:candidate subject)) (= tree (:tree subject))
                   (= (set paths) (set (:paths subject)) (set (keys (:entries subject))))
                   (seq paths) (every? string? paths) (seq owners)
                   (every? #(and (string? %) (= % (rt/canonical %))) owners)
                   (every? (set (:nonclaims admission)) required-nonclaims)
                   (seq (:evidence admission))
                   (every? (fn [p] (some #(rt/covers? % p) paths)) source-paths))
              "Dependency admission does not bind the exact source-only value, scope and nonclaims")
    (doseq [reference (:evidence admission)] (referenced-evidence! reference))
    (doseq [[path {:keys [mode blob]}] (:entries subject)]
      (rt/safe-path! path)
      (require! (and (= "100644" mode) (string? blob) (re-matches #"[0-9a-f]{40}" blob)
                     (= (str mode " blob " blob "\t" path "\u0000")
                        (path-output! ctx (get-in ctx [:config :repo-dir]) ["git" "ls-tree" "-z" candidate "--" path])))
                "Dependency source blob or mode differs from its admission"))
    (when (= :accepted-immutable-lineage (:kind phase))
      (let [repo (get-in ctx [:config :repo-dir]) parent (:source-parent subject)]
        (require! (and (string? parent) (re-matches #"[0-9a-f]{40}" parent)
                       (= parent (first (str/split (rt/git ctx repo "show" "-s" "--format=%P" candidate) #" ")))
                       (= (:source-parent-tree subject) (rt/git ctx repo "rev-parse" (str parent "^{tree}"))))
                  "Immutable dependency source-parent identity changed")
        (doseq [path paths]
          (require! (= (path-output! ctx repo ["git" "ls-tree" "-z" parent "--" path])
                       (path-output! ctx repo ["git" "ls-tree" "-z" candidate "--" path]))
                    "Immutable dependency source value differs from its admitted lineage"))))
    (require! (and (= :dependency-source-status (:kind snapshot)) (= subject (:subject snapshot))
                   (true? (:known-end snapshot)) (= live (:current-status snapshot))
                   (empty? (:writers live)) (empty? (:refusals live)))
              "Dependency source status is stale, has a current writer, or retains a relevant refusal")
    subject))

(defn- ended-source-phase! [ctx shared candidate tree source-paths phase]
  (admitted-source! ctx shared candidate tree source-paths phase)
  (if (= :accepted-immutable-lineage (:kind phase))
    ;; The admitted immutable value can have joined source history. A missing
    ;; creation lease is not permission to fabricate one or erase old holds.
    (require! (and (= candidate (:candidate phase)) (= tree (:tree phase))
                   (true? (:writer-ended? phase)) (true? (:no-unresolved-authority phase)))
              "Immutable dependency lineage is not bound to its inspected current value")
  (let [{:keys [owner-state-file lease-id lease-token paths]} phase
        release (referenced-evidence! (:release-evidence phase))
        lease (or (:lease release) release)]
    (require! (and (string? owner-state-file) (= owner-state-file (rt/canonical owner-state-file))
                   (string? lease-id) (not (str/blank? lease-id))
                   (string? lease-token) (not (str/blank? lease-token))
                   (= candidate (:candidate phase)) (= tree (:tree phase))
                   (true? (:writer-ended? phase)) (true? (:no-unresolved-authority phase))
                   (seq paths) (every? string? paths)
                   (every? (fn [p] (some #(rt/covers? % p) paths)) source-paths)
                   (= owner-state-file (:state-file lease)) (= lease-token (:token lease))
                   (= (set paths) (set (:paths lease)))
                   (= lease-id (or (:id lease) (pr-str [(:state-dir lease) (:key lease)])))
                   (or (true? (:released release)) (true? (:row-absent release))
                       (true? (get-in release [:result :row-absent]))))
              "Dependency is not bound to its genuine ended source phase")
    ;; A Main publisher's identity is not the dependency's Source author.
    ;; Scope these guards to that exact Source phase, preserving unrelated holds.
    (require! (not-any? #(or (= lease-token (:token %))
                             (and (= owner-state-file (:state-file %))
                                  (rt/overlap? paths (:paths %)))) (vals (:writers shared)))
              "The dependency source phase still has a writer requiring inspection")
    (require! (not-any? #(= owner-state-file (:state-file %))
                        (mapcat vals (vals (:restrictions shared))))
              "The dependency source owner retains an unresolved refusal"))))

(defn verified-dependencies! [ctx task receipt]
  (let [dependencies (:verified-source-dependencies receipt)
        _ (require! (seq dependencies) "An actual verified external source merge is required")
        shared (rt/coordinate! ctx (fn [state] [state (select-keys state [:history :main-effects :writers :restrictions])]))
        _ (require! (not-any? #(= (:state-file ctx) (:state-file %)) (vals (:main-effects shared)))
                    "The original controller has an unresolved Main operation")
        history (:history shared)
        main (rt/fresh-main! ctx)
        repo (get-in ctx [:config :repo-dir])]
    (doseq [{:keys [owner-state-file intent-id commit candidate tree history-sha256 source-paths source-phase]} dependencies]
      (require! (and (string? owner-state-file) (string? intent-id)
                     (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %)) [commit candidate tree])
                     (seq source-paths) (every? string? source-paths)) "Dependency merge identity is incomplete")
      (ended-source-phase! ctx shared candidate tree source-paths source-phase)
      (let [entry (some #(when (and (= :main-verified (:kind %))
                                    (= owner-state-file (get-in % [:intent :state-file]))
                                    (= intent-id (get-in % [:intent :id]))
                                    (= commit (get-in % [:actual :commit]))
                                    (= candidate (get-in % [:intent :candidate]))
                                    (= tree (get-in % [:intent :tree]) (get-in % [:actual :tree]))) %) history)]
        (require! (and entry (= history-sha256 (rt/digest (pr-str entry)))) "Dependency has no matching verified Main history")
        (let [intent (:intent entry) actual (:actual entry)
              expected-parents (case (:method intent)
                                 :merge [(:base intent) candidate]
                                 :squash [(:base intent)]
                                 nil)
              parents (str/split (rt/git ctx repo "show" "-s" "--format=%P" commit) #" ")]
          (require! (and expected-parents (= expected-parents (:parents actual) parents)
                         (= tree (rt/git ctx repo "rev-parse" (str candidate "^{tree}"))))
                    "Dependency commit does not integrate the exact source candidate")
          (require! (and (string? (:main actual)) (re-matches #"[0-9a-f]{40}" (:main actual)))
                    "Dependency Main identity is incomplete")
          (rt/git ctx repo "merge-base" "--is-ancestor" commit (:main actual))
          (rt/git ctx repo "merge-base" "--is-ancestor" (:main actual) main))
        (require! (= tree (rt/git ctx repo "rev-parse" (str commit "^{tree}"))) "Dependency Git tree differs")
        (rt/git ctx repo "merge-base" "--is-ancestor" commit main)
        (require! (not= commit (:base task)) "Dependency does not advance the retained source base")
        (rt/git ctx repo "merge-base" "--is-ancestor" (:base task) commit)
        (let [changed (set (remove empty?
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

(defn inspect-fresh-source! [ctx task evidence-file]
  (let [file (io/file (rt/canonical evidence-file))
        _ (require! (.startsWith (.toPath file) (.toPath (io/file (rt/canonical (:state-dir ctx)))))
                    "Fresh validation evidence must be retained by this controller")
        receipt (rt/read-edn file)
        classification (:operator-classification receipt)
        authorization (referenced-evidence! (:authorization receipt))
        [_ authorized-pr authorized-step] (when (keyword? (:authorization-scope receipt))
                                           (re-matches #"fresh-pr([0-9]+)-(status|whitespace)-validation"
                                                       (name (:authorization-scope receipt))))
        proof (referenced-evidence! (:source-acceptance receipt))
        validation (:validation receipt)
        {:keys [base candidate tree worktree branch]} validation
        old-error (:error task)
        status-read? (and (= "Command failed" (:message old-error))
                          (true? (get-in old-error [:details :restricted]))
                          (= ["gh" "pr" "view"] (take 3 (get-in old-error [:details :argv]))))
        failed-check? (and (= "Sandboxed check failed" (:message old-error))
                           (not (get-in old-error [:details :restricted]))
                           (not= false (get-in old-error [:details :writer-ended?]))
                           (= 1 (get-in old-error [:details :result :exitCode]))
                           (not (rt/authorization-denied?
                                 (str (get-in old-error [:details :result :stdout]) "\n"
                                      (get-in old-error [:details :result :stderr])))))
        commands (vec (distinct (concat (get-in ctx [:config :checks]) (:checks receipt))))
        repo (get-in ctx [:config :repo-dir])]
    (require! (and (= :fresh-source-validation (:kind receipt)) (= (:key task) (:task-key receipt))
                   (= (:pr task) (:original-pr receipt))
                   (= (rt/digest (pr-str task)) (:original-task-sha256 receipt))
                   (= :controller-operator (:inspector classification))
                   (= :fresh-source-validation (:kind classification))
                   (= (error-digest task) (:original-error-sha256 classification))
                   (true? (:no-unresolved-source-semantics classification))
                   (true? (:old-actions-remain-prohibited classification))
                   (string? (:basis classification)) (not (str/blank? (:basis classification)))
                   (vector? (:preserved-nonclaims receipt)) (seq (:preserved-nonclaims receipt))
                   (or status-read? failed-check?))
              "This is not an inspected fresh validation of a retained failed source candidate")
    (require! (and (= :direct-human-question-reply (:source authorization))
                   (contains? (set (:scope authorization)) (:authorization-scope receipt))
                   (= (str (get-in task [:pr :number])) authorized-pr)
                   (= (if status-read? "status" "whitespace") authorized-step))
              "Fresh validation needs the specific human authorization; old refusals are not cleared")
    (when status-read?
      (let [status (referenced-evidence! (:status-validation receipt))]
        (require! (and (= (get-in receipt [:authorization :sha256]) (:authorization-sha256 status))
                       (= (:argv status) (get-in old-error [:details :argv]))
                       (= (get-in task [:pr :number]) (:pr-number status))
                       (= (:candidate task) (:head status) (get-in status [:provider-result :headRefOid]))
                       (= "OPEN" (get-in status [:provider-result :state]))
                       (= (get-in ctx [:config :base-branch]) (get-in status [:provider-result :baseRefName]))
                       (= 0 (:exit status)))
                  "The newly authorized original PR status read has not been verified")))
    (require! (and (= :accepted-scoped-source-only (get-in proof [:source-review :disposition]))
                   (zero? (get-in proof [:source-review :finding-count] -1))
                   (= (:candidate task) (get-in proof [:source-review :source-input]))
                   (= (set (:paths task)) (set (get-in proof [:source-review :owned-paths])))
                   (= (select-keys validation [:base :candidate])
                      (select-keys (:composition proof) [:base :candidate]))
                   (= tree (get-in proof [:composition :candidate-tree]))
                   (= [(:candidate task) base] (get-in proof [:composition :parents])))
              "Fresh candidate has no matching independent source and composition acceptance")
    (require! (and (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %)) [base candidate tree])
                   (string? worktree) (string? branch) (str/starts-with? branch "codex/")
                   (not= branch (:branch task))
                   (not= (rt/canonical worktree) (rt/canonical (:worktree task)))
                   (= (rt/common-git-directory worktree) (rt/common-git-directory repo))
                   (= (rt/canonical worktree) (rt/canonical (rt/git ctx worktree "rev-parse" "--show-toplevel")))
                   (= branch (rt/git ctx worktree "symbolic-ref" "--short" "HEAD"))
                   (= candidate (rt/git ctx worktree "rev-parse" "HEAD"))
                   (= tree (rt/git ctx worktree "rev-parse" "HEAD^{tree}"))
                   (empty? (changes ctx worktree)))
              "Fresh validation needs a distinct clean named worktree in the original repository")
    (require! (= [(:candidate task) base]
                 (str/split (rt/git ctx repo "show" "-s" "--format=%P" candidate) #" "))
              "Fresh candidate is not the recorded normal source/Main composition")
    (require! (= tree (rt/git ctx repo "merge-tree" "--write-tree" (:candidate task) base))
              "Fresh candidate differs from the original committed source composition")
    (let [changed (remove empty? (str/split (path-output! ctx worktree
                                                ["git" "diff" "--name-only" "--no-renames" "-z" base candidate "--"]) #"\u0000"))]
      (require! (seq changed) "Fresh validation has no remaining source change")
      (doseq [path changed] (owned-file! (assoc task :worktree worktree) path)))
    (require! (and (seq commands) (every? #(and (vector? %) (seq %) (every? string? %)
                                               (contains? (get-in ctx [:config :check-executables]) (first %))) commands))
              "Fresh checks are outside the configured toolset")
    (doseq [argv commands]
      (require! (and (not (get-in @(:state ctx) [:restricted-checks (codex/check-key ctx argv)]))
                     (not (get-in @(:state ctx) [:restricted-commands (rt/digest (pr-str argv))])))
                "A fresh check is still locally refused")
      (rt/assert-authorized! ctx :check (codex/check-key ctx argv))
      (rt/assert-authorized! ctx :command (rt/digest (pr-str argv))))
    (let [shared (rt/coordinate! ctx (fn [s] [s (select-keys s [:main-effects :restrictions])]))]
      (require! (not-any? #(= (:state-file ctx) (:state-file %)) (vals (:main-effects shared)))
                "An original Main operation still needs inspection")
      (require! (not (get-in shared [:restrictions :api (rt/digest (pr-str ["POST" "graphql"]))]))
                "The separate shared API refusal still blocks productive continuation"))
    (rt/git ctx repo "merge-base" "--is-ancestor" base (rt/fresh-main! ctx))
    {:kind :fresh-source-validation :stage :queued :validation validation :commands commands
     :original-error-sha256 (error-digest task) :evidence-file (str file) :evidence-sha256 (file-digest file)
     :authorization (:authorization receipt) :source-acceptance (:source-acceptance receipt)
     :preserved-nonclaims (:preserved-nonclaims receipt)}))

(defn validate-fresh-queued! [ctx task]
  (let [inspection (:fresh-source-validation task)
        original (get-in task [:fresh-source-validation-history (dec (count (:fresh-source-validation-history task))) :original-task])]
    (require! (= inspection (inspect-fresh-source! ctx original (:evidence-file inspection)))
              "The fresh validation handoff changed before native checking")
    inspection))

(defn composition-conflict-snapshot! [ctx task]
  (let [cwd (:worktree task)
        conflicts (vec (remove empty? (str/split (path-output! ctx cwd
                                  ["git" "diff" "--name-only" "--diff-filter=U" "-z" "--"]) #"\u0000")))]
    (require! (seq conflicts) "The original worktree has no unresolved merge conflict")
    (doseq [path conflicts] (owned-file! task path))
    {:head (rt/git ctx cwd "rev-parse" "HEAD")
     :merge-head (rt/git ctx cwd "rev-parse" "MERGE_HEAD")
     :conflict-paths conflicts
     :index-sha256 (rt/digest (path-output! ctx cwd ["git" "ls-files" "--stage" "-z"]))
     :files (into (sorted-map)
                  (for [path (changes ctx cwd)
                        :let [file (owned-file! (assoc task :paths [path]) path)]]
                    [path {:sha256 (file-digest file) :executable (.canExecute file)}]))}))

(defn inspect-composition! [ctx task evidence-file]
  (let [file (io/file (rt/canonical evidence-file))
        _ (require! (.startsWith (.toPath file) (.toPath (io/file (rt/canonical (:state-dir ctx)))))
                    "Composition evidence must be retained by this controller")
        receipt (rt/read-edn file) validation (:validation receipt)
        {:keys [base candidate tree worktree branch]} validation
        details (get-in task [:error :details])
        proof (referenced-evidence! (:resolution-acceptance receipt))
        snapshot (composition-conflict-snapshot! ctx task)
        repo (get-in ctx [:config :repo-dir])
        commands (vec (distinct (concat (get-in ctx [:config :checks]) (:checks receipt))))]
    (require! (and (= :attention (:status task)) (= :ci (:resume-status task)) (:pr task)
                   (= :owned-composition-conflict (:kind receipt)) (= (:key task) (:task-key receipt))
                   (= (:pr task) (:original-pr receipt))
                   (= (rt/digest (pr-str task)) (:original-task-sha256 receipt))
                   (= "Command failed" (get-in task [:error :message])) (= 1 (:exit details))
                   (false? (:restricted details)) (= ["git" "merge" "--no-edit" base] (:argv details))
                   (not (rt/authorization-denied? (str (:out details) "\n" (:err details))))
                   (= snapshot (:original-conflict receipt))
                   (= [(:candidate task) base] [(:head snapshot) (:merge-head snapshot)]))
              "This is not the exact ordinary owned composition conflict")
    (require! (not-any? #(#{:pending :uncertain :restricted} (:status %)) (vals (:effects @(:state ctx))))
              "Unsettled effects must be inspected before composition recovery")
    (require! (and (= :accepted-owned-composition-resolution (:disposition proof))
                   (= [] (:findings proof)) (= (:key task) (:task-key proof))
                   (= (select-keys task [:base :candidate :tree]) (:source proof))
                   (= (select-keys validation [:base :candidate :tree]) (:resolution proof))
                   (= [(:candidate task) base] (:parents proof))
                   (= (:conflict-paths snapshot) (:conflict-paths proof)))
              "The exact conflict resolution needs independent acceptance")
    (require! (and (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %)) [base candidate tree])
                   (string? branch) (str/starts-with? branch "codex/") (not= branch (:branch task))
                   (string? worktree) (not= (rt/canonical worktree) (rt/canonical (:worktree task)))
                   (= (rt/common-git-directory worktree) (rt/common-git-directory repo))
                   (= (rt/canonical worktree) (rt/canonical (rt/git ctx worktree "rev-parse" "--show-toplevel")))
                   (= branch (rt/git ctx worktree "symbolic-ref" "--short" "HEAD"))
                   (= candidate (rt/git ctx worktree "rev-parse" "HEAD"))
                   (= tree (rt/git ctx worktree "rev-parse" "HEAD^{tree}")) (empty? (changes ctx worktree))
                   (= [(:candidate task) base] (str/split (rt/git ctx worktree "show" "-s" "--format=%P" candidate) #" ")))
              "The resolved candidate needs a distinct clean named normal composition")
    (let [automatic (source-command! ctx repo ["git" "merge-tree" "--write-tree" (:candidate task) base] #{1})
          automatic-tree (first (str/split-lines (:out automatic)))
          _ (require! (re-matches #"[0-9a-f]{40}" (or automatic-tree "")) "Automatic conflict tree is unavailable")
          resolved (vec (remove empty? (str/split (path-output! ctx worktree
                                     ["git" "diff" "--name-only" "--no-renames" "-z" automatic-tree candidate "--"]) #"\u0000")))]
      (require! (= (set (:conflict-paths snapshot)) (set resolved))
                "Resolution changed paths beyond the recorded conflicts"))
    (require! (and (seq commands) (every? #(and (vector? %) (seq %) (every? string? %)
                                               (contains? (get-in ctx [:config :check-executables]) (first %))) commands))
              "Composition checks are outside the configured toolset")
    (doseq [argv commands]
      (require! (and (not (get-in @(:state ctx) [:restricted-checks (codex/check-key ctx argv)]))
                     (not (get-in @(:state ctx) [:restricted-commands (rt/digest (pr-str argv))])))
                "A composition check remains refused")
      (rt/assert-authorized! ctx :check (codex/check-key ctx argv))
      (rt/assert-authorized! ctx :command (rt/digest (pr-str argv))))
    (let [shared (rt/coordinate! ctx (fn [s] [s (select-keys s [:main-effects :restrictions])]))]
      (require! (not-any? #(= (:state-file ctx) (:state-file %)) (vals (:main-effects shared)))
                "An original Main operation still needs inspection")
      (require! (not (get-in shared [:restrictions :api (rt/digest (pr-str ["POST" "graphql"]))]))
                "The shared API refusal still blocks continuation"))
    (rt/git ctx repo "merge-base" "--is-ancestor" base (rt/fresh-main! ctx))
    {:kind :owned-composition-conflict :stage :queued :validation validation :commands commands
     :original-error-sha256 (error-digest task) :evidence-file (str file) :evidence-sha256 (file-digest file)
     :resolution-acceptance (:resolution-acceptance receipt) :original-conflict snapshot}))

(defn validate-composition-queued! [ctx task]
  (let [inspection (:composition-recovery task)
        original (:original-task (peek (:composition-recovery-history task))) ]
    (require! (= inspection (inspect-composition! ctx original (:evidence-file inspection)))
              "The composition resolution changed before native checking")
    inspection))

(defn review-permitted? [task]
  (let [inspection (:inspected-authored task) fresh (:fresh-source-validation task)]
    (or (and (= :inspected-reported-implementation (:kind inspection)) (= :checked (:stage inspection))
         (= (:original-error-sha256 inspection) (error-digest task))
         (= (:checked-implementation-job inspection) (:implementation-job task))
         (= (select-keys task [:base :candidate :tree]) (:checked-candidate inspection))
         (= :passed (get-in task [:checks :result])))
        (and (= :fresh-source-validation (:kind fresh)) (= :checked (:stage fresh))
             (= (:original-error-sha256 fresh) (error-digest task))
             (= (select-keys task [:base :candidate :tree]) (:checked-candidate fresh))
             (= :passed (get-in task [:checks :result]))))))
