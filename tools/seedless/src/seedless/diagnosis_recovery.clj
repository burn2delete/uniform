(ns seedless.diagnosis-recovery
  "Inspected recovery for read-only compiler model jobs that never initialized."
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.runtime :as rt])
  (:import [java.nio.file Files]
           [java.security MessageDigest]
           [java.time Instant]))

(defn model-effect? [id]
  (and (string? id)
       (or (str/starts-with? id "compiler-diagnosis/")
           (str/starts-with? id "compiler-progress/"))))

(defn- observation-key [observation]
  (rt/digest (pr-str (select-keys observation [:input-id :position :failure :subject :terminal]))))

(defn- present-text? [value]
  (and (string? value) (not (str/blank? value))))

(defn- observed-input? [observation]
  (and (map? observation)
       (every? #(present-text? (get observation %)) [:input-id :source-revision])))

(defn- failure [state id]
  (or (get-in state [:effects id :failure])
      (get-in state [:effects id :prior :failure])
      (when (str/starts-with? id "compiler-diagnosis/")
        (get-in state [:diagnoses (subs id (count "compiler-diagnosis/"))]))
      (when (= id (get-in state [:diagnosis-blocker :effect])) (:diagnosis-blocker state))
      (some #(when (= id (:effect %)) %) (vals (:diagnoses state)))
      (some (fn [task]
              (cond (= id (get-in task [:compiler-result :diagnosis :effect]))
                    (get-in task [:compiler-result :diagnosis])
                    (= id (:compiler-progress-intent task)) (:error task)))
            (vals (:tasks state)))))

(defn operation-context
  "Return the retained source/candidate binding. Missing or ambiguous bindings fail closed."
  [state id]
  (let [pending (filter #(= id (get-in % [:compiler-result :diagnosis :effect]))
                        (vals (:tasks state)))]
    (if (str/starts-with? id "compiler-diagnosis/")
      (let [keys* (vec (for [[key intent] (:diagnosis-intents state) :when (= id intent)] key))
            observations (->> (cons (:compiler-observation state) (map :compiler-result pending))
                              (filter #(and (observed-input? %) (= (first keys*) (observation-key %))))
                              (map #(select-keys % [:input-id :source-revision])) distinct)]
        (when (and (= 1 (count keys*)) (= 1 (count observations)))
          {:kind :compiler-diagnosis :observation-key (first keys*)
           :input-id (:input-id (first observations))
           :source-revision (:source-revision (first observations))
           :candidates (mapv #(select-keys % [:key :base :candidate :tree]) (sort-by :key pending))}))
      (let [tasks (filter #(= id (:compiler-progress-intent %)) (vals (:tasks state)))]
        (when (= 1 (count tasks))
          (let [task (first tasks)]
            (when (and (every? #(and (string? (get task %)) (not (str/blank? (get task %)))) [:key :base :candidate :tree])
                       (every? observed-input? [(:compiler-observation task) (:compiler-result task)]))
              {:kind :compiler-progress :slice (:key task)
               :candidate (select-keys task [:base :candidate :tree])
               :observations (mapv #(assoc (select-keys % [:input-id :source-revision])
                                          :observation-key (observation-key %))
                                   [(:compiler-observation task) (:compiler-result task)])})))))))

(defn file-digest [path]
  (format "%064x" (BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256")
                                         (Files/readAllBytes (.toPath (io/file path)))))))

(defn- log-events [ctx predicate]
  (when-not (and (string? (:log-file ctx)) (.isFile (io/file (:log-file ctx))))
    (rt/fail! "Model recovery needs the original native event log" {}))
  (with-open [reader (io/reader (:log-file ctx) :encoding "UTF-8")]
    (->> (line-seq reader)
         (map #(json/read-str % :key-fn keyword))
         (filter predicate)
         vec)))

(defn job-events [ctx job]
  (log-events ctx #(= job (:job %))))

(defn- read-receipt [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path :encoding "UTF-8"))]
    (let [eof (Object.)
          options {:eof eof :readers {} :default (fn [tag _] (rt/fail! "Unsupported termination receipt tag" {:tag tag}))}
          value (edn/read options reader)]
      (when (or (identical? eof value) (not (identical? eof (edn/read options reader))))
        (rt/fail! "Termination receipt must contain exactly one EDN value" {:path path}))
      value)))

(defn- foreground-return? [ctx id job context events claim]
  (let [artifacts (:termination-evidence claim)
        roles #{:runtime-receipt :driver-exit :driver-stdout :driver-finished}
        by-role (into {} (map (juxt :role identity) artifacts))]
    (and (= :compiler-diagnosis (:kind context))
         (= :inspected-foreground-return (:termination claim))
         (string? (:termination-description claim)) (not (str/blank? (:termination-description claim)))
         (vector? artifacts) (= (count roles) (count artifacts)) (= roles (set (keys by-role)))
         (every? (fn [{:keys [path sha256]}]
                   (and (string? path) (.isAbsolute (io/file path)) (.isFile (io/file path))
                        (string? sha256) (= sha256 (file-digest path)))) artifacts)
         (let [receipt (read-receipt (get-in by-role [:runtime-receipt :path]))
               exit (read-receipt (get-in by-role [:driver-exit :path]))
               stdout (read-receipt (get-in by-role [:driver-stdout :path]))
               finished (str/trim (slurp (get-in by-role [:driver-finished :path])))
               entry (last (log-events ctx #(= "foreground-production-entrypoint" (:event %))))
               sent (first (filter #(= "rpc-send" (:event %)) events))
               original-job (get-in @(:state ctx) [:jobs job])
               receipt-job (first (:jobs receipt))
               stderr (mapv #(select-keys % [:time :job :line]) (filter #(= "app-server-stderr" (:event %)) events))]
           (and (map? receipt) (map? stdout) entry
                (= :seedless/foreground-runtime-receipt-v1 (:schema receipt))
                (= 0 exit (:driver-exit receipt))
                (= finished (:driver-finished-at receipt))
                (false? (:model-turn-completed receipt)) (false? (:model-turn-completed stdout))
                (false? (:campaign-started receipt)) (false? (:campaign-started stdout))
                (false? (:provider-writes receipt)) (false? (:provider-writes stdout))
                (= :blocked (get-in receipt [:diagnosis :status]) (:collector-status receipt)
                   (:result-status stdout) (:collector-status stdout))
                (= :runtime-blocked-before-model (get-in receipt [:diagnosis :outcome]))
                (false? (get-in receipt [:diagnosis :model-proposal-received]))
                (false? (get-in receipt [:diagnosis :restricted]))
                (= id (get-in receipt [:original-effect :id]))
                (= :uncertain (get-in receipt [:original-effect :status]))
                (= "initialize" (get-in receipt [:original-effect :failure-details :method]))
                (= 1 (count (:jobs receipt))) (= job (:id receipt-job))
                (= :diagnose (:requested-role receipt-job)) (= :read (:requested-mode receipt-job))
                (= :attention (:status receipt-job) (:status original-job))
                (= (:message original-job) (:message receipt-job) (get-in receipt [:diagnosis :message]))
                (false? (:restricted receipt-job)) (false? (:thread-created receipt-job)) (false? (:turn-created receipt-job))
                (= ["initialize"] (get-in receipt [:runtime :rpc-send-methods]))
                (= 0 (get-in receipt [:runtime :rpc-response-count]) (count (filter #(= "rpc-receive" (:event %)) events)))
                (= (:time sent) (get-in receipt [:runtime :initialize-sent-at]))
                (= stderr (get-in receipt [:runtime :stderr]))
                (vector? (get-in receipt [:runtime :command])) (seq (get-in receipt [:runtime :command]))
                (= (get-in @(:state ctx) [:identity :codex-command]) (get-in receipt [:runtime :command]))
                (= (:input-id context) (get-in receipt [:compiler :input-id]) (:input-id stdout))
                (= (:source-revision context) (get-in receipt [:compiler :revision]) (:compiler-revision stdout) (:compiler-revision entry))
                (every? #(present-text? (get-in receipt [:controller %])) [:revision :tree])
                (= (get-in receipt [:controller :revision]) (:controller-revision stdout) (:controller-revision entry))
                (= (get-in receipt [:controller :tree]) (:controller-tree entry))
                (string? (:state-directory stdout)) (= (rt/canonical (:state-dir ctx)) (rt/canonical (:state-directory stdout)))
                (.isBefore (Instant/parse (:time entry)) (Instant/parse (:time sent)))
                (.isAfter (Instant/parse finished) (Instant/parse (:time (last events)))))))))

(defn- restricted-history? [record]
  (and (map? record)
       (or (= :restricted (:status record)) (true? (:restricted record))
           (true? (get-in record [:details :restricted]))
           (restricted-history? (:prior record)) (restricted-history? (:previous record)))))

(defn- never-started-job? [record]
  (and (= :attention (:status record)) (false? (:restricted record))
       (not (restricted-history? record))
       (not-any? #(some? (get record %)) [:thread :turn :output])))

(defn validate-not-started!
  "Validate an operator inspection against native retained state and log bytes.
  Termination needs owned-process evidence or inspected original foreground
  completion artifacts; absence of a PID or a bare boolean is not evidence."
  [ctx id original inspection]
  (let [state @(:state ctx)
        claim (:model-not-started inspection)
        job (:job claim)
        job-record (get-in state [:jobs job])
        context (when (model-effect? id) (operation-context state id))
        failed (failure state id)
        prefix (if (= :compiler-diagnosis (:kind context)) "diagnose-" "review-")
        legacy-jobs (set (for [[key record] (:jobs state)
                              :when (and (str/starts-with? key prefix)
                                         (never-started-job? record)
                                         (= (:message original) (:message record)))] key))
        legacy-effects (set (for [[key record] (:effects state)
                                 :let [record (if (= :not-applied (:status record)) (:prior record) record)]
                                 :when (and (model-effect? key)
                                            (#{:pending :uncertain} (:status record))
                                            (= (:message original) (:message record)))] key))]
    (when-not (and (map? claim) context
                   (map? (:identity state)) (= (:identity ctx) (:identity state))
                   (= :not-applied (:outcome inspection)) (nil? (:result inspection))
                   (#{:pending :uncertain} (:status original))
                   (not (restricted-history? original))
                   (not (or (:restricted failed) (get-in failed [:details :restricted])))
                   (= "initialize" (get-in failed [:details :method]))
                   (= id (:effect claim))
                   (= (rt/digest (pr-str (:identity state))) (:state-identity-sha256 claim))
                   (= (rt/digest (pr-str original)) (:effect-sha256 claim))
                   (= context (:context claim))
                   (string? job) (str/starts-with? job prefix) (never-started-job? job-record)
                   (= (rt/digest (pr-str job-record)) (:job-sha256 claim))
                   (if-let [bound-job (:job original)]
                     (= job bound-job)
                     (and (= #{job} legacy-jobs) (= #{id} legacy-effects)))
                   (true? (:trace-complete claim)))
      (rt/fail! "Not-applied model recovery needs a matching inspected initialization-only failure"
                {:effect id :job job :restricted (or (restricted-history? original) (:restricted failed))}))
    (let [events (job-events ctx job)
          sends (filter #(= "rpc-send" (:event %)) events)
          methods (mapv #(get-in % [:message :method]) sends)
          closed (filter #(= "app-server-closed" (:event %)) events)]
      (when-not (and (= ["initialize"] methods)
                     (= (rt/digest (pr-str events)) (:job-events-sha256 claim))
                     (not-any? #(or (= "rpc-reader-error" (:event %))
                                    (and (= "rpc-receive" (:event %)) (get-in % [:message :method]))) events)
                     (string? (:evidence inspection)) (not (str/blank? (:evidence inspection)))
                     (if (seq closed)
                       (and (= 1 (count closed)) (= (first closed) (last events))
                            (not= (first events) (first closed))
                            (true? (:owned-process-ended (first closed))) (integer? (:exit (first closed))))
                       (foreground-return? ctx id job context events claim)))
        (rt/fail! "Model recovery needs the complete initialization trace and inspected termination evidence"
                  {:effect id :job job})))
    inspection))

(defn assert-settled! [ctx id]
  (let [effect (get-in @(:state ctx) [:effects id])]
    (case (:status effect)
      :done true
      :not-applied (do
                     (validate-not-started! ctx id (:prior effect) (:inspection effect))
                     true)
      (rt/fail! "Failed model effect needs reconciliation before retry"
                {:effect id :restricted (= :restricted (:status effect))}))))

(defn assert-predecessor! [ctx id]
  (when id (assert-settled! ctx id)))
