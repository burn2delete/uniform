(ns simplify.runtime
  "Local execution, atomic state, and verbose, credential-redacted event logs."
  (:refer-clojure :exclude [run!])
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.nio.channels FileChannel OverlappingFileLockException]
           [java.nio.file Files StandardCopyOption StandardOpenOption]
           [java.security MessageDigest]
           [java.time Instant]
           [java.util UUID]
           [java.util.concurrent ConcurrentHashMap]
           [java.util.function Function]))

(defn fail! [message data] (throw (ex-info message data)))
(defn canonical [path] (.getCanonicalPath (io/file path)))
(defn digest [value]
  (format "%064x" (BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256")
                                        (.getBytes (str value) "UTF-8")))))

(defn redact [value]
  (cond
    (map? value) (into {} (map (fn [[k v]]
                                [k (if (re-find #"(?i)(authorization|token|secret|password|api.?key)" (name k))
                                     "[REDACTED]" (redact v))]) value))
    (sequential? value) (mapv redact value)
    (string? value) (-> value
                        (str/replace #"(?:gh[pousr]_[A-Za-z0-9_]+|github_pat_[A-Za-z0-9_]+|sk-[A-Za-z0-9_-]+)" "[REDACTED]")
                        (str/replace #"(?i)(Bearer\s+)[^\s\"']+" "$1[REDACTED]"))
    :else value))

(defn event! [ctx kind data]
  (let [entry (redact (merge {:time (str (Instant/now)) :event (name kind)} data))
        line (json/write-str entry)
        lock (:log-lock ctx)]
    (locking lock
      (binding [*out* *err*] (println line) (flush))
      (when-let [path (:log-file ctx)] (spit path (str line "\n") :append true)))
    entry))

(defn read-edn [path] (edn/read-string {:readers {} :default (fn [tag _] (fail! "Unsupported EDN tag" {:tag tag}))} (slurp path)))

(defn write-edn! [destination value]
    (let [path (.toPath (io/file destination))
          tmp (Files/createTempFile (.getParent path) "state-" ".edn" (make-array java.nio.file.attribute.FileAttribute 0))]
      (try
        (with-open [out (FileChannel/open tmp (into-array StandardOpenOption [StandardOpenOption/WRITE]))]
          (let [buffer (java.nio.ByteBuffer/wrap (.getBytes (pr-str value) "UTF-8"))]
            (while (.hasRemaining buffer) (.write out buffer)))
          (.force out true))
        (Files/move tmp path (into-array StandardCopyOption [StandardCopyOption/ATOMIC_MOVE StandardCopyOption/REPLACE_EXISTING]))
        (finally (Files/deleteIfExists tmp)))))

(defn save! [ctx]
  (let [state (:state ctx)]
    (locking state (write-edn! (:state-file ctx) @state))))

(defn update! [ctx f & args]
  (let [state (:state ctx)]
    (locking state
      (apply swap! state f args)
      (save! ctx)
      @state)))

(defn command
  "Argument vectors only. Both output streams are drained concurrently. No shell."
  ([ctx cwd argv] (command ctx cwd argv nil))
  ([ctx cwd argv input]
   (when-not (and (seq argv) (every? string? argv)) (fail! "Invalid command arguments" {:argv argv}))
   (event! ctx :command-start {:cwd (str cwd) :argv argv})
   (let [started (System/nanoTime)
         process (.start (doto (ProcessBuilder. ^java.util.List (vec argv)) (.directory (io/file cwd))))
         drain (fn [stream label]
                 (future
                   (with-open [reader (io/reader stream :encoding "UTF-8")]
                     (let [out (StringBuilder.)]
                       (doseq [line (line-seq reader)]
                         (.append out (str line "\n"))
                         (event! ctx :command-output {:stream label :line line}))
                       (str out)))))
         stdout (drain (.getInputStream process) "stdout")
         stderr (drain (.getErrorStream process) "stderr")]
     (with-open [writer (io/writer (.getOutputStream process) :encoding "UTF-8")]
       (when input (.write writer (str input))))
     (let [exit (.waitFor process)
           result {:exit exit :out @stdout :err @stderr}]
       (event! ctx :command-end {:argv argv :exit exit :elapsed-ms (quot (- (System/nanoTime) started) 1000000)})
       result))))

(defn authorization-denied? [message]
  (boolean (re-find #"(?i)(HTTP (401|403|429)|requested URL returned error: (401|403|429)|permission denied|permission to .+ denied|write access .+ not granted|authentication failed|resource not accessible|requires authentication|bad credentials|rate limit exceeded|secondary rate limit|not authorized)"
                    (or message ""))))

(declare shared-restriction shared-restrict!)

(defn run! [ctx cwd argv]
  (let [action (cond
                 (= ["git" "push"] (vec (take 2 argv))) ["git-push" (get-in ctx [:config :repository])]
                 (= ["gh" "pr" "merge"] (vec (take 3 argv))) ["gh-pr-merge" (get-in ctx [:config :repository])]
                 :else argv)
        key (digest (pr-str action))
        _ (when (or (get-in @(:state ctx) [:restricted-commands key])
                    (shared-restriction ctx :commands key))
            (fail! "Restricted controller command cannot be retried" {:argv argv :restricted true}))
        result (command ctx cwd argv)]
    (when-not (zero? (:exit result))
      (let [restricted (authorization-denied? (str (:err result) "\n" (:out result)))]
        (when restricted
          (let [refusal {:argv argv :error (:err result)}]
            (update! ctx assoc-in [:restricted-commands key] refusal)
            (shared-restrict! ctx :commands key refusal)))
        (fail! "Command failed" (assoc result :argv argv :restricted restricted))))
    (str/trim (:out result))))

(defn git [ctx cwd & args] (run! ctx cwd (into ["git"] args)))

(defn effect!
  "Record an intent before a remote mutation. An uncertain effect is never replayed automatically."
  [ctx id operation]
  (let [prior (get-in @(:state ctx) [:effects id])]
    (case (:status prior)
      :done (:result prior)
      :restricted (fail! "Restricted effect cannot be retried" {:effect id :effect-status :restricted :restricted true})
      (:pending :uncertain) (fail! "External effect needs reconciliation; not replaying"
                                  {:effect id :effect-status (:status prior) :prior prior})
      (do
        (update! ctx assoc-in [:effects id] {:status :pending})
        (try
          (let [result (operation)]
            (update! ctx assoc-in [:effects id] {:status :done :result result})
            result)
          (catch Exception e
            (update! ctx assoc-in [:effects id] {:status (if (:restricted (ex-data e)) :restricted :uncertain)
                                               :message (.getMessage e)})
            (throw (ex-info (.getMessage e)
                            (assoc (or (ex-data e) {}) :effect id
                                   :effect-status (get-in @(:state ctx) [:effects id :status])) e))))))))

(defn tracking-effect!
  "Status may legitimately cycle. An unresolved earlier write must be inspected first."
  [ctx key operation]
  (let [state (:state ctx)
        id (locking state
             (when-let [prior (get-in @(:state ctx) [:tracking-intents key])]
               (let [status (get-in @(:state ctx) [:effects prior :status])]
                 (when-not (= :done status)
                   (fail! "Tracking effect needs reconciliation; not issuing another write"
                          {:effect prior :effect-status status :restricted (= :restricted status)}))))
             (let [n (inc (or (:tracking-sequence @(:state ctx)) 0))
                   id (str key "/" n)]
               (update! ctx #(-> % (assoc :tracking-sequence n) (assoc-in [:tracking-intents key] id)))
               id))]
    (effect! ctx id operation)))

(defn with-lock [ctx f]
  (with-open [channel (FileChannel/open (.toPath (io/file (:lock-file ctx)))
                                       (into-array StandardOpenOption [StandardOpenOption/CREATE StandardOpenOption/WRITE]))]
    (let [lock (try (.tryLock channel) (catch OverlappingFileLockException _ nil))]
      (when-not lock (fail! "Another controller owns this campaign state" {}))
      (try (f) (finally (.release lock))))))

(defn coordination
  "Separate state ownership from the repository's short integration transaction."
  [common config]
  (let [common (canonical common)
        identity (digest (canonical (:state-dir config)))]
    {:lock-file (str (io/file common (str "uniform-controller-" identity ".lock")))
     :main-lock-file (str (io/file common "uniform-main.lock"))
     :coordination-lock-file (str (io/file common "uniform-harnesses.lock"))
     :coordination-file (str (io/file common "uniform-harnesses.edn"))
     :snapshot-ref (str "refs/uniform/" identity "/main")
     :snapshot-lock (Object.)
     :main-lock-owner (atom nil)}))

(defn fresh-main! [{:keys [snapshot-lock] :as ctx}]
  (let [{:keys [repo-dir remote base-branch]} (:config ctx)]
    (locking snapshot-lock
      ;; Neither FETCH_HEAD nor another campaign's tracking ref is our snapshot.
      (git ctx repo-dir "fetch" "--no-write-fetch-head" "--refmap=" remote
           (str "refs/heads/" base-branch ":" (:snapshot-ref ctx)))
      (git ctx repo-dir "rev-parse" (:snapshot-ref ctx)))))

(defn safe-path! [path]
  (let [normal (str/replace path #"/+$" "")]
    (when (or (str/blank? normal) (str/starts-with? normal "/") (str/includes? normal "\\")
              (some #{".." ".git"} (str/split normal #"/")))
      (fail! "Finding ownership must use safe relative paths" {:path path}))
    normal))

(defn covers? [owner path]
  (or (= owner ".") (= owner path) (str/starts-with? path (str owner "/"))))

(defn overlap? [a b]
  (boolean (some (fn [x] (some #(or (covers? x %) (covers? % x)) b)) a)))

(def ^:private local-coordination-locks (ConcurrentHashMap.))

(defn- coordinate! [ctx f]
  (let [path (:coordination-lock-file ctx)
        monitor (.computeIfAbsent local-coordination-locks (canonical path)
                                  (reify Function (apply [_ _] (Object.))))]
    (locking monitor
      (with-open [channel (FileChannel/open (.toPath (io/file path))
                                           (into-array StandardOpenOption [StandardOpenOption/CREATE StandardOpenOption/WRITE]))
                  _lock (.lock channel)]
        (let [file (io/file (:coordination-file ctx))
              state (if (.exists file) (read-edn file) {:schema 1 :writers {} :main-effects {} :history []})]
          (when-not (= 1 (:schema state)) (fail! "Unknown shared coordination schema" {}))
          (let [[next-state result] (f state)]
            (when-not (= state next-state) (write-edn! file next-state))
            result))))))

(defn- owner [ctx]
  {:state-dir (canonical (:state-dir ctx)) :campaign (get-in ctx [:config :campaign])})

(defn- effect-key [ctx id] (pr-str [(:state-dir (owner ctx)) id]))

(defn shared-restriction [ctx kind key]
  (when (:coordination-file ctx)
    (coordinate! ctx (fn [state] [state (get-in state [:restrictions kind key])]))))

(defn shared-restrict! [ctx kind key refusal]
  (when (:coordination-file ctx)
    (coordinate! ctx (fn [state]
                       (if (get-in state [:restrictions kind key])
                         [state nil]
                         [(assoc-in state [:restrictions kind key]
                                    (merge (owner ctx) refusal {:at (str (Instant/now))})) nil])))))

(defn import-state-coordination!
  "Preserve legacy refusals and explicitly recorded unknown writers during handover."
  [ctx]
  (let [local @(:state ctx)]
    (coordinate!
     ctx
     (fn [state]
       (let [state (reduce (fn [state [kind entries]]
                             (reduce-kv (fn [state key refusal]
                                          (if (get-in state [:restrictions kind key]) state
                                              (assoc-in state [:restrictions kind key] (merge (owner ctx) refusal))))
                                        state (or entries {})))
                           state [[:commands (:restricted-commands local)] [:api (:restricted-actions local)]])
             state (reduce
                    (fn [state [id effect]]
                      (let [migration [(:state-dir (owner ctx)) id :main-effect]]
                        (if (or (not (str/starts-with? id "merge/"))
                                (not (#{:pending :uncertain :restricted} (:status effect)))
                                (get-in state [:imports migration])) state
                            (let [task (first (filter #(= id (str "merge/" (:candidate %))) (vals (:tasks local))))
                                  intent (merge (owner ctx) (select-keys task [:base :candidate :tree])
                                                {:id id :legacy true :status :uncertain
                                                 :method (if (= :seedless/v1 (:schema local)) :merge :squash)
                                                 :original-effect effect})]
                              (-> state (assoc-in [:main-effects (effect-key ctx id)] intent)
                                  (assoc-in [:imports migration] {:at (str (Instant/now))}))))))
                    state (:effects local))
             state (reduce
                    (fn [state [key task]]
                      (let [boundary (:shared-dependency-validation task)
                            migration [(:state-dir (owner ctx)) key (:candidate boundary) (:artifact-dir boundary)]
                            id (pr-str [(:state-dir (owner ctx)) key])]
                        (if (or (not= :unknown (:descendant-coverage boundary))
                                (get-in state [:imports migration])) state
                            (let [lease (or (get-in state [:writers id])
                                            (merge (owner ctx)
                                                   {:id id :token (str (UUID/randomUUID)) :key key
                                                    :paths (mapv safe-path! (:paths task)) :phase :checking
                                                    :status :needs-inspection :boundary boundary
                                                    :at (str (Instant/now))}))]
                              (when (empty? (:paths lease)) (fail! "Unknown legacy writer has no owned paths" {:finding key}))
                              (-> state (assoc-in [:writers id] lease)
                                  (assoc-in [:imports migration] {:lease (:token lease) :at (str (Instant/now))}))))))
                    state (:tasks local))]
         [state nil])))))

(defn claim-paths!
  "Claim only an actual implementing/checking phase. Busy is ordinary waiting."
  [ctx key paths phase]
  (let [paths (mapv safe-path! paths)
        id (pr-str [(canonical (:state-dir ctx)) key])
        lease (merge (owner ctx) {:id id :token (str (UUID/randomUUID)) :key key :paths paths
                                 :phase phase :status :active :at (str (Instant/now))})]
    (when (empty? paths) (fail! "Writer ownership needs paths" {:finding key}))
    (coordinate! ctx
                 (fn [state]
                   (if (or (get-in state [:writers id])
                           (some #(overlap? paths (:paths %)) (vals (:writers state))))
                     [state nil]
                     [(assoc-in state [:writers id] lease) lease])))))

(defn- owned-lease! [ctx state lease]
  (let [current (get-in state [:writers (:id lease)])]
    (when-not (and (= (:token lease) (:token current))
                   (= (:state-dir (owner ctx)) (:state-dir current)))
      (fail! "Writer lease identity differs" {:finding (:key lease)}))
    current))

(defn lease-phase! [ctx lease phase]
  (coordinate! ctx (fn [state]
                     (owned-lease! ctx state lease)
                     [(assoc-in state [:writers (:id lease) :phase] phase) nil])))

(defn release-paths! [ctx lease]
  (coordinate! ctx (fn [state]
                     (let [current (owned-lease! ctx state lease)]
                       (when (= :needs-inspection (:status current))
                         (fail! "Interrupted writer needs inspected release" {:finding (:key lease)}))
                       [(update state :writers dissoc (:id lease)) nil]))))

(defn inspect-release-paths! [ctx lease evidence]
  (when (str/blank? evidence) (fail! "Inspected writer release needs evidence" {}))
  (coordinate! ctx (fn [state]
                     (let [current (owned-lease! ctx state lease)]
                       [(-> state
                            (update :writers dissoc (:id lease))
                            (update :history conj {:kind :writer-inspected :lease current :evidence evidence
                                                   :at (str (Instant/now))})) nil]))))

(defn with-claimed-paths [ctx lease f]
  (try
    (let [result (f)]
      (release-paths! ctx lease)
      result)
    (catch Exception e
      (let [data (ex-data e)]
        (if (or (:writer-ended? data) (contains? data :exit))
          (release-paths! ctx lease)
          (coordinate! ctx (fn [state]
                             (owned-lease! ctx state lease)
                             [(update-in state [:writers (:id lease)] assoc :status :needs-inspection
                                         :error (redact {:message (.getMessage e) :details data})) nil]))))
      (throw e))))

(defn- main-owned! [ctx]
  (when-not (identical? (Thread/currentThread) @(:main-lock-owner ctx))
    (fail! "Main mutation requires the integration lock" {})))

(defn- with-main-lock [ctx inspect? f]
  (with-open [channel (FileChannel/open (.toPath (io/file (:main-lock-file ctx)))
                                       (into-array StandardOpenOption [StandardOpenOption/CREATE StandardOpenOption/WRITE]))]
    (let [lock (try (.tryLock channel) (catch OverlappingFileLockException _ nil))]
      (if-not lock
        (do (event! ctx :integration-waiting {:reason :main-lock-busy}) :busy)
        (try
          (reset! (:main-lock-owner ctx) (Thread/currentThread))
          (let [pending (coordinate! ctx (fn [state] [state (seq (vals (:main-effects state)))]))]
            (if (and pending (not inspect?))
              (do (event! ctx :integration-waiting {:reason :unresolved-main-effect :effect pending}) :fenced)
              (try (f)
                   (catch Exception e
                     (coordinate! ctx
                                  (fn [state]
                                    [(update state :main-effects
                                             (fn [effects]
                                               (into {} (map (fn [[key effect]]
                                                               [key (if (= (:state-dir (owner ctx)) (:state-dir effect))
                                                                      (assoc effect :status :uncertain
                                                                             :error {:message (.getMessage e)
                                                                                     :details (redact (select-keys (ex-data e) [:restricted :effect :effect-status]))})
                                                                      effect)]) effects)))) nil]))
                     (throw e)))))
          (finally (reset! (:main-lock-owner ctx) nil) (.release lock)))))))

(defn try-with-main-lock [ctx f]
  (with-main-lock ctx false f))

(defn- validate-main-intent! [intent]
  (when-not (and (string? (:id intent)) (#{:squash :merge} (:method intent))
                 (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %))
                         (map intent [:base :candidate :tree])))
    (fail! "Incomplete shared main intent" {:intent intent})))

(defn begin-main-merge! [ctx intent]
  (main-owned! ctx)
  (when (shared-restriction ctx :commands
                           (digest (pr-str ["gh-pr-merge" (get-in ctx [:config :repository])])))
    (fail! "Repository merge action remains restricted" {:restricted true}))
  (validate-main-intent! intent)
  (coordinate! ctx (fn [state]
                     (when (seq (:main-effects state)) (fail! "Shared main effect requires reconciliation" {}))
                     [(-> state
                          (assoc-in [:main-effects (effect-key ctx (:id intent))]
                                    (merge intent (owner ctx) {:status :pending :at (str (Instant/now))}))
                          (assoc-in [:imports [(:state-dir (owner ctx)) (:id intent) :main-effect]]
                                    {:kind :journaled :at (str (Instant/now))})) nil])))

(defn- verify-main-result! [intent actual]
  (validate-main-intent! intent)
  (let [parents (if (= :merge (:method intent)) [(:base intent) (:candidate intent)] [(:base intent)])]
    (when-not (and (string? (:commit actual)) (re-matches #"[0-9a-f]{40}" (:commit actual))
                   (= (:tree intent) (:tree actual)) (= parents (:parents actual)))
      (fail! "Inspected main result differs from the exact intent" {:intent intent :actual actual}))))

(defn complete-main-merge!
  "Actual facts must come from the caller's Git/provider verification, never inference."
  [ctx intent actual]
  (main-owned! ctx)
  (coordinate! ctx
               (fn [state]
                 (let [key (effect-key ctx (:id intent)) pending (get-in state [:main-effects key])]
                   (when-not (and (= (:id intent) (:id pending))
                                  (= (:state-dir (owner ctx)) (:state-dir pending))
                                  (= (select-keys intent [:base :candidate :tree :method])
                                     (select-keys pending [:base :candidate :tree :method])))
                     (fail! "Shared main intent identity differs" {}))
                   (verify-main-result! pending actual)
                   [(-> state (update :main-effects dissoc key)
                        (update :history conj {:kind :main-verified :intent pending :actual actual :at (str (Instant/now))})) actual]))))

(defn reconcile-main-merge!
  "Original-owner inspection only. This never retries a mutation or clears its refusal."
  [ctx effect-id inspection]
  (when (str/blank? (:evidence inspection)) (fail! "Shared main reconciliation needs inspected evidence" {}))
  (with-main-lock
    ctx true
    #(coordinate! ctx
                  (fn [state]
                    (let [key (effect-key ctx effect-id) pending (get-in state [:main-effects key])
                          original (merge (:original-intent inspection) (into {} (remove (comp nil? val) pending)))]
                      (when-not (and (= effect-id (:id pending))
                                     (= (:state-dir (owner ctx)) (:state-dir pending)))
                        (fail! "Only the original main-effect owner can reconcile it" {}))
                      (case (:outcome inspection)
                        :not-applied nil
                        :merged (verify-main-result! original (:actual inspection))
                        (fail! "Shared main inspection needs merged or not-applied outcome" {}))
                      [(-> state (update :main-effects dissoc key)
                           (update :history conj {:kind :main-inspected :intent pending
                                                  :inspection inspection :at (str (Instant/now))})) nil])))))
