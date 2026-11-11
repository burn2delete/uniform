(ns seedless.compiler-observation
  "Current-source compiler feedback for the existing Seedless selection loop."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.compiler-probe :as probe]
            [simplify.codex :as codex]
            [simplify.runtime :as rt])
  (:import [java.nio.file Files]))

(def command probe/command)

(defn input-sha256
  "Use the probe's byte identity with the controller's relative-path policy."
  [root path]
  (rt/safe-path! path)
  (probe/input-sha256 root path))

(defn input-id [observation]
  (rt/digest (pr-str (select-keys observation [:inputs :route]))))

(defn current? [cwd observation argv]
  (try
    (and (seq (:inputs observation))
         (= argv (get-in observation [:route :command]))
         (= (:input-id observation) (input-id observation))
         (every? (fn [{:keys [path sha256]}] (= sha256 (input-sha256 cwd path)))
                 (:inputs observation)))
    (catch Exception _ false)))

(defn- read-one [text]
  (let [reader (java.io.PushbackReader. (java.io.StringReader. text)) eof (Object.)
        options {:eof eof :readers {}
                 :default (fn [tag _] (rt/fail! "Unsupported compiler observation tag" {:tag tag}))}
        value (edn/read options reader)]
    (when (or (identical? value eof) (not (identical? eof (edn/read options reader))))
      (rt/fail! "Compiler probe must return exactly one EDN observation" {}))
    value))

(defn- read-result [result]
  ;; The JVM's shared /tmp perfdata instrumentation can warn before -main.
  ;; This exact lock warning changes no compiler result. Every other preamble,
  ;; extra EDN value, trailing warning or error remains a protocol failure.
  (loop [remaining (or (:stdout result) "") warnings []]
    (if-let [[_ warning rest-output]
             (re-matches #"(?s)(\[[0-9]+\.[0-9]+s\]\[warning\]\[perf,memops\] Cannot use file /tmp/hsperfdata_[A-Za-z0-9_.-]+/[0-9]+ because it is locked by another process \(errno = 11\)\r?\n)(.*)"
                         remaining)]
      (recur rest-output (conj warnings warning))
      {:value (read-one remaining) :warnings warnings})))

(defn blocked [reason result]
  {:status :blocked :reason reason
   :terminal {:status (if (#{124 137 143} (:exitCode result)) :timeout :unavailable)
              :exit (:exitCode result)}
   :execution result})

(defn- completed-ids [observation]
  (mapv :component-id (:completed observation)))

(defn- position [observation]
  (let [{:keys [component stage]} (:position observation)
        stages [:current-frontend :canonical-core :core-verification :function-types :type-verification]
        index (.indexOf stages stage)]
    (when (and (nat-int? component) (not (neg? index))) [component index])))

(defn nonregressing? [before after]
  (let [scope [:command :stage :target :triple :abi :catalog :closure
               :frontend-profile :frontend-target :toolchain :scope :build-graph-status]
        completed (completed-ids before)]
    (and (= (select-keys (:route before) scope) (select-keys (:route after) scope))
         (= completed (vec (take (count completed) (completed-ids after))))
         (or (= :scoped-pass (:status after))
             (and (position before) (position after)
                  (not (neg? (compare (position after) (position before)))))))))

(defn advanced? [before after]
  (and (nonregressing? before after)
       (or (= :scoped-pass (:status after))
           (pos? (compare (position after) (position before))))))

(defn- validate-observation! [cwd argv result observation]
  (when-not (and (map? observation)
                 (#{:actionable-failure :blocked :scoped-pass} (:status observation))
                 (string? (:source-revision observation))
                 (vector? (:inputs observation))
                 (vector? (get-in observation [:route :closure]))
                 (seq (get-in observation [:route :closure]))
                 (vector? (:completed observation))
                 (= (completed-ids observation)
                    (vec (take (count (:completed observation))
                               (map :component-id (get-in observation [:route :closure])))))
                 (= :llvm-x86_64-linux (get-in observation [:route :target]))
                 (= "x86_64-unknown-linux-gnu" (get-in observation [:route :triple]))
                 (= (:exitCode result) (get-in observation [:terminal :exit]))
                 (current? cwd observation argv))
    (rt/fail! "Compiler observation does not bind current inputs and declared target" {}))
  (case (:status observation)
    :scoped-pass
    (when-not (and (zero? (:exitCode result)) (= :passed (get-in observation [:terminal :status]))
                   (= (count (:completed observation)) (count (get-in observation [:route :closure]))))
      (rt/fail! "A scoped compiler pass needs successful terminal execution" {}))
    :actionable-failure
    (let [{:keys [id kind source producer consumer owner work-key reason]} (:failure observation)]
      (when-not (and (not (zero? (:exitCode result)))
                     (= :failed (get-in observation [:terminal :status]))
                     (position observation)
                     (= (get-in observation [:position :component]) (count (:completed observation)))
                     (< (get-in observation [:position :component]) (count (get-in observation [:route :closure])))
                     (#{:implementation :language :interface} kind)
                     (every? #(and (string? %) (not (str/blank? %)))
                             [id source producer consumer work-key reason (:task owner)])
                     (re-matches #"SH-(?:[01][0-9]|2[0-9])" (or (:milestone owner) ""))
                     (seq (:paths owner)))
        (rt/fail! "Actionable compiler failure lacks an implementation owner" {}))
      (doseq [path (conj (vec (:paths owner)) source)] (rt/safe-path! path)))
    nil)
  observation)

(def envelope-source "language/compiler/src/gravity/compiler/authenticated_envelope.gravity")
(def c7-source "language/compiler/src/gravity/compiler/c7_type_checker_engine.gravity")
(def envelope-recursion-key "compiler/SH-08/authenticated-envelope-mutual-recursion")

(defn- witnessed-mutual-components [subject]
  (let [functions (into {} (map (juxt :function-syntax-id identity) (:functions subject)))]
    (filterv (fn [component]
               (let [ids (:function-syntax-ids component)]
                 (and (= :mutually-recursive (:kind component))
                      (vector? ids) (< 1 (count ids)) (= (count ids) (count (set ids)))
                      (every? #(and (string? %) (not (str/blank? %)) (contains? functions %)) ids)
                      (vector? (:function-core-node-ids component))
                      (= (count ids) (count (:function-core-node-ids component)))
                      (every? #(and (string? %) (not (str/blank? %))) (:function-core-node-ids component))
                      (= (set (:function-core-node-ids component))
                         (set (map #(get-in functions [% :function-core-node-id]) ids))))))
             (:recursion-components subject))))

(defn classify-observation
  "Interpret only a source-traced compiler boundary. The collector validates
  raw execution and current inputs before invoking this selection policy."
  [cwd observation]
  (let [{:keys [failure subject route position]} observation
        component (get (:closure route) (:component position))
        mutual (witnessed-mutual-components subject)]
    (if (and (= :blocked (:status observation)) (= :unclassified (:kind failure))
             (= :failed (get-in observation [:terminal :status]))
             (integer? (get-in observation [:terminal :exit]))
             (not (zero? (get-in observation [:terminal :exit])))
             (= "C7-ANNOTATION" (:id failure))
             (= :mutual-recursion-unsupported (get-in failure [:diagnostic :reason]))
             (= :rejected (get-in failure [:diagnostic :compiler-result-status]))
             (= :function-types (:stage position))
             (= :current-source-function-types (:stage route))
             (= :complete-six-owner-typed-frontend-prerequisite (:scope route))
             (= :meta (:frontend-profile route)) (= :jvm (:frontend-target route))
             (every? (set (map :component-id (:closure route))) probe/cohort)
             (= :authenticated-envelope (:component-id component))
             (= envelope-source (:source failure) (:source-path subject) (:source-path component))
             (re-matches #"[0-9a-f]{64}" (or (:source-sha256 subject) ""))
             (= (:source-sha256 subject) (try (input-sha256 cwd envelope-source) (catch Exception _ nil)))
             (re-matches #"sha256:[0-9a-f]{64}" (or (:core-artifact-id subject) ""))
             (= :passed (:core-verification subject))
             (seq mutual))
      (-> observation
          (assoc :status :actionable-failure)
          (update :failure merge
                  {:kind :language :work-key envelope-recursion-key
                   :producer "Freshly verified SH07/B47 recursion products for the complete envelope source"
                   :consumer "Current C7 sh08-ft-recursive-proof and function type construction"
                   :owner {:milestone "SH-08" :task "SH-08.2" :paths [c7-source]
                           :record "docs/self-hosting-milestone-completion-plan.md"}
                   :reason "Current C7 rejects this complete compiler module because its verified call graph contains mutual recursion. The diagnostic's first component is not the actual mutual-recursion witness."
                   :witness {:mutual-components mutual :core-artifact-id (:core-artifact-id subject)}
                   :prerequisites
                   [{:milestone "SH-02" :task "SH-02.3" :paths [envelope-source]
                     :record "docs/self-hosting-milestone-completion-plan.md"
                     :reason "Express the envelope's required deterministic identity and traversal operations through existing loop/recur behavior while preserving every complete source function, export, result and rejection contract. This removes the unsupported recursive call-graph prerequisite without reducing the declared compiler closure."
                     :progress {:command command
                                :expected "The same full envelope source reaches C7 inference beyond mutual-recursion rejection; subsequent failures remain explicit."}}]}))
      observation)))

(defn declared-inputs [cwd]
  (mapv (fn [path]
          {:path path :sha256 (try (input-sha256 cwd path) (catch Exception _ :unavailable))})
        probe/input-paths))

(defn- executable-file-identity [file]
  {:path (rt/canonical file) :bytes (.length file)
   :sha256 (probe/sha256 (Files/readAllBytes (.toPath file)))})

(defn- executable-identity [executable]
  (when-let [file (some (fn [directory]
                          (let [file (io/file directory executable)]
                            (when (and (.isFile file) (.canExecute file)) file)))
                        (str/split (or (System/getenv "PATH") "")
                                   (re-pattern (java.util.regex.Pattern/quote java.io.File/pathSeparator))))]
    (executable-file-identity file)))

(defn execution-context [ctx]
  {:timeout-ms (get-in ctx [:config :check-timeout-ms])
   :codex-command (get-in ctx [:config :codex-command])
   :java-version (System/getProperty "java.version")
   :java-home (System/getProperty "java.home")
   :environment-id (rt/digest (pr-str (select-keys (System/getenv)
                                                   ["PATH" "JAVA_HOME" "JAVA_TOOL_OPTIONS" "JDK_JAVA_OPTIONS" "CLJ_CONFIG"])))
   :executables (into {} (map (fn [name] [name (executable-identity name)]) ["clojure" "codex"]))})

(defn observe!
  "Run through the controller's existing sandbox executor and durable effects.
  Nonzero compiler execution is evidence, not a successful check."
  [ctx cwd base execute!]
  (when-let [argv (get-in ctx [:config :compiler-observation-command])]
    (let [previous (:compiler-observation @(:state ctx))
          inputs (declared-inputs cwd)
          execution (execution-context ctx)
          fingerprint (rt/digest (pr-str {:command argv :inputs inputs :execution execution}))
          effect (str "compiler-observation/" fingerprint)
          prior-effect (:observation-effect previous)
          observed
          (try
            ;; A later shared denial also invalidates cached dispatch permission.
            (rt/assert-authorized! ctx :check (codex/check-key ctx argv))
            (when (#{:pending :uncertain :restricted} (get-in @(:state ctx) [:effects prior-effect :status]))
              (rt/fail! "Original compiler execution needs inspection; changed inputs cannot replay it"
                        {:effect prior-effect
                         :restricted (= :restricted (get-in @(:state ctx) [:effects prior-effect :status]))}))
            (if (and (= fingerprint (:execution-fingerprint previous))
                     (or (= :blocked (:status previous)) (current? cwd previous argv)))
              (or (get-in @(:state ctx) [:effects prior-effect :result]) previous)
              (rt/effect! ctx effect
                          #(let [result (try (execute! argv)
                                             (catch Exception e
                                               (if (or (:restricted (ex-data e))
                                                       (false? (:writer-ended? (ex-data e)))
                                                       (not (map? (:result (ex-data e)))))
                                                 (throw e)
                                                 (:result (ex-data e)))))]
                             (cond
                               (:capture-truncated result) (blocked "Compiler observation capture was truncated" result)
                               (not (number? (:exitCode result))) (blocked "Compiler execution has no terminal status" result)
                               :else
                               (try
                                 (let [{observation :value warnings :warnings} (read-result result)]
                                   (when-not (= inputs (:inputs observation))
                                     (rt/fail! "Compiler observation omitted or changed declared source inputs" {}))
                                   (assoc
                                    (if (= "P18T04007" (get-in observation [:failure :id]))
                                      (blocked "Final-release refusal does not identify an implementation repair" result)
                                      (validate-observation! cwd argv result observation))
                                    :execution result :execution-warnings warnings))
                                 (catch Exception e
                                   (blocked (str "Compiler execution prerequisite or observation unavailable: " (.getMessage e)) result)))))))
            (catch Exception e
              (assoc (blocked (.getMessage e) (:result (ex-data e)))
                     :restricted (boolean (:restricted (ex-data e)))
                     :effect (or (:effect (ex-data e)) prior-effect effect))))
          observed (merge {:source-revision base :inputs inputs :route (probe/route [])}
                          observed
                          {:execution-fingerprint fingerprint :execution-context execution
                           :observation-effect (or (:effect observed) effect)})
          observed (if (current? cwd observed argv)
                     (validate-observation! cwd argv {:exitCode (get-in observed [:terminal :exit])}
                                            (classify-observation cwd observed))
                     observed)]
      (rt/update! ctx assoc :compiler-observation observed)
      observed)))

(defn waiting-plan [observation]
  {:coverage ["Existing staged Linux LLVM compiler path"]
   :source-closure (if-let [closure (seq (get-in observation [:route :closure]))]
                     (mapv pr-str closure) ["Execution prerequisite unavailable; declared closure is unchanged"])
   :target "llvm-x86_64-linux / x86_64-unknown-linux-gnu"
   :subset "Current-source development prerequisite; migration and release acceptance remain separate"
   :summary (or (:reason observation) "Compiler execution is blocked")
   :candidate-dispositions []
   :blockers [(or (:reason observation) (get-in observation [:failure :reason]) "Compiler execution is blocked")]
   :slices []})

(defn matching-candidates [observation known open-prs]
  (let [key (get-in observation [:failure :work-key])
        marker (str "<!-- uniform-seedless:" key " -->")]
    (if-not key []
            (vec (concat
                  (filter #(or (= key (:key %))
                               (and (= key (:compiler-failure %)) (= "repair" (:compiler-role %)))) (vals known))
                  (filter #(str/includes? (or (:body %) "") marker) open-prs))))))

(defn candidate-context [known open-prs]
  (vec (concat
        (map #(assoc % :candidate-id (str "slice/" (:key %)))
             (filter #(not (#{:done :not-planned} (:status %))) (vals known)))
        (map #(assoc % :candidate-id (str "pr/" (:number %))) open-prs))))

(defn rebindable? [task slice]
  (and task (#{:ready :repair} (:status task))
       (not-any? #(get task %) [:candidate :pr :implementation :implementation-job :error])
       (= (select-keys task [:paths :dependencies :stack-on :stage])
          (select-keys slice [:paths :dependencies :stack-on :stage]))))

(def compiler-task-keys
  [:compiler-observation :compiler-failure :compiler-role :compiler-task
   :compiler-candidates :compiler-selection-reason])

(defn- implementation-owner? [task owner]
  ;; These source-grounded declarations name concrete files, not directories.
  (and (= (:compiler-task task) (:task owner))
       (= (:stage task) (:milestone owner))
       (boolean (some (set (:paths owner)) (:paths task)))))

(defn- prerequisite-owner [observation task]
  (first (filter #(implementation-owner? task %) (get-in observation [:failure :prerequisites]))))

(defn selected?
  ([task] (selected? task (:compiler-observation task)))
  ([task observation]
   (case (:status observation)
     :actionable-failure
     (and (= (:compiler-failure task) (get-in observation [:failure :work-key]))
          (case (:compiler-role task)
            "repair" (implementation-owner? task (get-in observation [:failure :owner]))
            "prerequisite" (some? (prerequisite-owner observation task))
            false))
     :scoped-pass (and (= "independent" (:compiler-role task)) (= "" (:compiler-failure task)))
     false)))

(defn- prerequisite-key [observation task]
  (let [owner (prerequisite-owner observation task)]
    (str (get-in observation [:failure :work-key]) "/prerequisite/"
         (subs (rt/digest (pr-str [(:task owner) (:milestone owner) (sort (:paths owner))])) 0 16))))

(defn- prerequisite-candidates [observation slice known open-prs]
  (let [key (prerequisite-key observation slice)]
    (concat
     (filter #(or (= key (:key %))
                  (and (= "prerequisite" (:compiler-role %))
                       (= (:compiler-task slice) (:compiler-task %))
                       (selected? % observation))) (vals known))
     (filter #(str/includes? (or (:body %) "") (str "<!-- uniform-seedless:" key " -->")) open-prs))))

(defn bind-selection!
  "Require compiler evidence links and assign stable work identities before admission."
  [cwd argv observation plan known open-prs]
  (if-not observation
    plan
    (do
      (when-not (current? cwd observation argv)
        (rt/fail! "Compiler observation changed before task admission" {:compiler-observation-stale true}))
      (let [failure (:failure observation) key (:work-key failure)
            actionable? (= :actionable-failure (:status observation))
            candidates (matching-candidates observation known open-prs)
            context (candidate-context known open-prs)
            dispositions (into {} (map (juxt :candidate identity) (:candidate-dispositions plan)))
            slices (:slices plan)
            repairs (map :compiler-failure (filter #(= "repair" (:compiler-role %)) slices))
            key-map (into {} (map (fn [slice]
                                    [(:key slice)
                                     (if (rebindable? (get known (:key slice)) slice)
                                       (:key slice)
                                       (case (:compiler-role slice)
                                         "repair" key
                                         "prerequisite" (prerequisite-key observation slice)
                                         (:key slice)))]) slices))]
        (when-not (= (count repairs) (count (set repairs)))
          (rt/fail! "Multiple tasks duplicate the same observed compiler repair" {}))
        (when-not (= (count dispositions) (count (:candidate-dispositions plan)))
          (rt/fail! "Candidate dispositions must have unique identities" {}))
        (doseq [candidate (keys dispositions)]
          (when-not (some #(= candidate (:candidate-id %)) context)
            (rt/fail! "Candidate disposition names an unknown retained candidate" {:candidate candidate})))
        (doseq [slice slices]
          (when (and (contains? known (:key slice))
                     (not (rebindable? (get known (:key slice)) slice)))
            (rt/fail! "Existing task is authored, restricted, uncertain, or has different ownership; do not rename it"
                      {:slice (:key slice)}))
          (when-not (selected? slice observation)
            (rt/fail! "Selected work must address the observed compiler failure with its declared task and implementation ownership"
                      {:slice (:key slice)}))
          (when (and (= "repair" (:compiler-role slice))
                     (seq (remove #(and (= (:key %) (:key slice)) (rebindable? % slice)) candidates)))
            (rt/fail! "Observed repair has an existing candidate or a different implementation owner"
                      {:slice (:key slice) :candidates candidates}))
          (when (and (= "prerequisite" (:compiler-role slice))
                     (seq (remove #(and (= (:key %) (:key slice)) (rebindable? % slice))
                                  (prerequisite-candidates observation slice known open-prs))))
            (rt/fail! "Observed prerequisite has an existing candidate; preserve its task identity"
                      {:slice (:key slice)}))
          (let [new-key (get key-map (:key slice))]
            (when (or (and (contains? known new-key)
                           (not (rebindable? (get known new-key) slice)))
                      (some #(str/includes? (or (:body %) "") (str "<!-- uniform-seedless:" new-key " -->")) open-prs))
              (rt/fail! "Selected compiler work duplicates a retained candidate" {:slice new-key})))
          (doseq [candidate context :when (and (rt/overlap? (:paths slice) (:paths candidate))
                                               (not (and (= (:key candidate) (:key slice))
                                                         (rebindable? candidate slice))))]
            (when-not (= "independent-successor" (:disposition (get dispositions (:candidate-id candidate))))
              (rt/fail! "Overlapping candidate scope needs an explicit independent-successor disposition"
                        {:slice (:key slice) :candidate (:candidate-id candidate)}))))
        (let [bound (mapv (fn [slice]
                            (cond-> (-> slice
                                        (assoc :key (get key-map (:key slice))
                                               :compiler-observation observation
                                               :compiler-selection-reason (:proposal slice)
                                               :compiler-candidates
                                               (vec (keep #(get dispositions (:candidate-id %))
                                                          (filter #(rt/overlap? (:paths slice) (:paths %)) context))))
                                        (update :dependencies #(mapv (fn [dep] (get key-map dep dep)) %))
                                        (update :stack-on #(get key-map % %)))
                              (rebindable? (get known (:key slice)) slice)
                              (merge (select-keys (get known (:key slice))
                                                  [:title :proposal :evidence :seed-paths :uniform-paths :consumer
                                                   :preserve :positive :negative :residual-host-boundary])))) slices)]
          (when-not (= (count bound) (count (set (map :key bound))))
            (rt/fail! "Multiple proposals address the same compiler increment" {}))
          (cond-> (assoc plan :slices bound
                         :source-closure (mapv pr-str (get-in observation [:route :closure]))
                         :target "llvm-x86_64-linux / x86_64-unknown-linux-gnu")
            (and actionable? (empty? slices))
            (update :blockers into [(if (seq candidates)
                                      (str "Waiting for existing compiler candidate: "
                                           (pr-str (mapv #(select-keys % [:key :status :number :html_url]) candidates)))
                                      (str "No permitted repair or prerequisite selected for " key))])))))))

(defn dispatch-order [task]
  [(case (:compiler-role task) "repair" 0 "prerequisite" 1 2)
   (if (= :repair (:status task)) 0 1) (:key task)])
