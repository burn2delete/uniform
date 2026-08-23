(ns gravity.worktree-preflight
  "Read-only Git worktree and integration preflight.

  This is a tooling boundary, not compiler authority.  It invokes Git without
  a shell, disables optional index locking, and only observes repository state.
  Its JSON report is deterministic and carries enough immutable identities for
  a separate admission or integration process to make a decision."
  (:require [clojure.string :as str])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path Paths]
           [java.util.regex Pattern]))

(def schema "gravity/worktree-preflight-v1")
(def default-base-ref "origin/main")
(def max-output-exclusions 32)
(def max-output-path-length 4096)
(def ^:private oid-pattern (Pattern/compile "^[0-9a-fA-F]{40,64}$"))

(defn- failure
  ([code message] (failure code message {}))
  ([code message details]
   (ex-info message {:code code :details details})))

(defn- error-code [error]
  (or (-> error ex-data :code) "WORKTREE-UNEXPECTED"))

(defn- error-details [error]
  (or (-> error ex-data :details) {}))

(defn- text [value]
  (str/trim (or value "")))

(defn- canonical-path [value]
  (.toPath (.getCanonicalFile (.toFile (if (instance? Path value)
                                         value
                                         (Paths/get (str value) (make-array String 0)))))))

(defn- git-result
  "Run one Git command without a shell.  All commands used by this namespace
  are read-only; GIT_OPTIONAL_LOCKS=0 prevents status from refreshing state."
  [^Path directory arguments]
  (try
    (let [command (into ["git" "-c" "core.fsmonitor=false"] arguments)
          builder (doto (ProcessBuilder. ^java.util.List command)
                    (.directory (.toFile directory)))
          environment (.environment builder)]
      (.put environment "GIT_OPTIONAL_LOCKS" "0")
      (let [process (.start builder)
            read-stream
            (fn [stream]
              (let [result (promise)
                    thread (Thread.
                            (fn []
                              (deliver result
                                       (try
                                         {:value (with-open [stream stream]
                                                   (String. (.readAllBytes stream)
                                                            StandardCharsets/UTF_8))}
                                         (catch Throwable error
                                           {:error error})))))]
                (.setDaemon thread true)
                (.start thread)
                {:thread thread :result result}))
            stdout (read-stream (.getInputStream process))
            stderr (read-stream (.getErrorStream process))
            exit (.waitFor process)
            await-stream
            (fn [{:keys [thread result]}]
              (.join ^Thread thread)
              (let [{:keys [value error]} @result]
                (if error (throw error) value)))]
        {:exit exit :stdout (await-stream stdout) :stderr (await-stream stderr)}))
    (catch java.io.IOException error
      (throw (failure "WORKTREE-GIT-UNAVAILABLE"
                      (str "cannot execute git: " (.getMessage error)))))))

(defn- git-failure [result arguments]
  (failure "WORKTREE-GIT-COMMAND"
           (if (seq (text (:stderr result)))
             (text (:stderr result))
             (str "git command failed with exit code " (:exit result)))
           {:arguments (vec arguments) :exit-code (:exit result)}))

(defn- require-git [directory arguments]
  (let [result (git-result directory arguments)]
    (if (zero? (:exit result)) result (throw (git-failure result arguments)))))

(defn- resolve-root [repository]
  (let [requested (canonical-path repository)
        result (git-result requested ["rev-parse" "--show-toplevel"])]
    (if (zero? (:exit result))
      (canonical-path (text (:stdout result)))
      (throw (failure "WORKTREE-NOT-REPOSITORY"
                      (if (seq (text (:stderr result)))
                        (text (:stderr result))
                        "path is not inside a Git repository")
                      {:path (str requested)})))))

(defn- valid-oid? [value]
  (and (string? value) (boolean (re-matches oid-pattern value))))

(defn- revision [root expression label]
  (let [result (git-result root ["rev-parse" "--verify" "--end-of-options" expression])]
    (if-not (zero? (:exit result))
      (throw (failure "WORKTREE-REVISION-MISSING"
                      (str "cannot resolve " label ": " expression)
                      {:label label :ref expression :git-error (text (:stderr result))}))
      (let [value (str/lower-case (text (:stdout result)))]
        (if (valid-oid? value)
          value
          (throw (failure "WORKTREE-REVISION-INVALID"
                          (str "Git returned an invalid " label " identity")
                          {:label label :value value})))))))

(defn- branch-info [root]
  (let [result (git-result root ["symbolic-ref" "-q" "HEAD"])]
    (if (zero? (:exit result))
      (let [ref (text (:stdout result))]
        (if (str/starts-with? ref "refs/heads/")
          {:ref ref :branch (subs ref (count "refs/heads/")) :detached false}
          {:ref (when (seq ref) ref) :branch nil :detached true}))
      {:ref nil :branch nil :detached true})))

(defn- parse-status [raw]
  (->> (str/split raw #"\u0000" -1)
       (remove str/blank?)
       (map (fn [token]
              (if (and (>= (count token) 3) (= \space (nth token 2)))
                {:status (subs token 0 2) :path (subs token 3)}
                (throw (failure "WORKTREE-STATUS-MALFORMED"
                                "Git returned malformed porcelain status")))))
       (sort-by (juxt :path :status))
       vec))

(defn- status-entries [root]
  (let [arguments ["status" "--porcelain=v1" "-z" "--untracked-files=all" "--no-renames"]
        result (git-result root arguments)]
    (if (zero? (:exit result))
      (parse-status (:stdout result))
      (throw (git-failure result arguments)))))

(defn- normalize-exclusion [value]
  (let [raw (str value)]
    (when (or (str/blank? raw)
              (> (count raw) max-output-path-length)
              (str/includes? raw "\u0000")
              (some #(str/includes? raw %) ["*" "?" "[" "]"]))
      (throw (failure "WORKTREE-OUTPUT-EXCLUSION-INVALID"
                      "output exclusion must be a non-empty bounded relative path"
                      {:path raw})))
    (when (or (str/starts-with? raw "/")
              (re-find #"^[A-Za-z]:[\\/]" raw))
      (throw (failure "WORKTREE-OUTPUT-EXCLUSION-INVALID"
                      "output exclusion must be repository-relative"
                      {:path raw})))
    (let [parts (->> (str/split (str/replace raw #"\\" "/") #"/" -1)
                     (remove str/blank?)
                     vec)]
      (when (or (empty? parts) (some #{"." ".."} parts))
        (throw (failure "WORKTREE-OUTPUT-EXCLUSION-INVALID"
                        "output exclusion cannot traverse"
                        {:path raw})))
      (let [normalized (str/join "/" parts)]
        (when (or (= normalized ".git") (str/starts-with? normalized ".git/"))
          (throw (failure "WORKTREE-OUTPUT-EXCLUSION-INVALID"
                          "output exclusion cannot name Git metadata"
                          {:path raw})))
        normalized))))

(defn- normalize-exclusions [values]
  (let [values (vec (or values []))]
    (when (> (count values) max-output-exclusions)
      (throw (failure "WORKTREE-OUTPUT-EXCLUSION-INVALID"
                      (str "at most " max-output-exclusions " output exclusions are allowed")
                      {:count (count values)})))
    (->> values (map normalize-exclusion) distinct sort vec)))

(defn- path-allowed? [path exclusions]
  (let [normalized (str/join "/"
                              (remove str/blank?
                                      (str/split (str/replace path #"\\" "/") #"/")))]
    (boolean (some #(or (= normalized %)
                        (str/starts-with? normalized (str % "/"))) exclusions))))

(defn- working-tree [root exclusions]
  (let [entries (status-entries root)
        allowed (filterv #(path-allowed? (:path %) exclusions) entries)
        blocked (filterv #(not (path-allowed? (:path %) exclusions)) entries)]
    {:state (if (seq entries) "dirty" "clean")
     :entries entries
     :allowed-entries allowed
     :blocked-entries blocked
     :tracked-changes (filterv #(not= "??" (:status %)) entries)
     :untracked-changes (filterv #(= "??" (:status %)) entries)
     :exclusions exclusions}))

(defn- parse-worktree-list [raw]
  (let [records (atom [])
        current (atom nil)
        flush! (fn [] (when @current (swap! records conj @current)))]
    (doseq [line (str/split-lines raw)]
      (cond
        (str/starts-with? line "worktree ")
        (do (flush!) (reset! current {:path (subs line (count "worktree "))}))
        (str/starts-with? line "HEAD ")
        (swap! current assoc :head (str/lower-case (subs line (count "HEAD "))))
        (str/starts-with? line "branch ")
        (swap! current assoc :branch-ref (subs line (count "branch ")))
        (= line "detached") (swap! current assoc :detached true)
        (= line "bare") (swap! current assoc :bare true)
        (str/starts-with? line "locked")
        (swap! current assoc :locked true :lock-reason (str/trim (subs line (count "locked"))))
        (str/starts-with? line "prunable")
        (swap! current assoc :prunable true :prune-reason (str/trim (subs line (count "prunable"))))
        :else nil))
    (flush!)
    @records))

(defn- worktrees [root exclusions]
  (let [arguments ["worktree" "list" "--porcelain"]
        result (git-result root arguments)]
    (if-not (zero? (:exit result))
      (throw (git-failure result arguments))
      (->> (parse-worktree-list (:stdout result))
           (map (fn [record]
                  (let [path (canonical-path (:path record))
                        state (cond
                                (:bare record) "bare"
                                (:prunable record) "prunable"
                                (not (Files/isDirectory path (make-array LinkOption 0))) "missing"
                                :else (try
                                        (:state (working-tree path exclusions))
                                        (catch clojure.lang.ExceptionInfo _ "unavailable")))]
                    (assoc record :path (str path) :state state))))
           (sort-by :path)
           vec))))

(defn- ancestor? [root ancestor descendant]
  (let [result (git-result root ["merge-base" "--is-ancestor" ancestor descendant])]
    (cond (zero? (:exit result)) true
          (= 1 (:exit result)) false
          :else nil)))

(defn- reconcile [root base candidate]
  (let [{base-commit :commit base-tree :tree} base
        {candidate-commit :commit candidate-tree :tree} candidate]
    (if (or (nil? base-commit) (nil? base-tree)
            (nil? candidate-commit) (nil? candidate-tree))
      {:relation "unavailable" :base-is-ancestor nil :candidate-is-ancestor nil
       :tree-equivalent false :recommendation "resolve_base_and_candidate_identities"}
      (let [base-ancestor (ancestor? root base-commit candidate-commit)
            candidate-ancestor (ancestor? root candidate-commit base-commit)
            same-commit (= base-commit candidate-commit)
            same-tree (= base-tree candidate-tree)
            relation (cond same-commit "identical"
                           same-tree "tree_equivalent_squash"
                           (true? base-ancestor) "descendant"
                           (true? candidate-ancestor) "behind"
                           (and (false? base-ancestor) (false? candidate-ancestor)) "divergent"
                           :else "unavailable")]
        {:relation relation
         :base-is-ancestor base-ancestor
         :candidate-is-ancestor candidate-ancestor
         :tree-equivalent same-tree
         :recommendation (case relation
                           ("identical" "tree_equivalent_squash") "no_remerge"
                           "descendant" "integrate_candidate"
                           "behind" "rebase_or_supersede_candidate"
                           "divergent" "resolve_divergence_before_integration"
                           "resolve_base_and_candidate_identities")}))))

(defn- identity-match? [expected actual]
  (or (nil? expected) (= (str/lower-case (str expected)) actual)))

(defn- diagnostic [code message details]
  {:code code :message message :details details})

(defn- preflight-document [repository options]
  (let [{:keys [mode base-ref candidate-base candidate-commit candidate-tree allow-output]
         :or {mode "integration" base-ref default-base-ref}} options
        exclusions (normalize-exclusions allow-output)
        diagnostics (atom [])
        root (try
               (resolve-root repository)
               (catch clojure.lang.ExceptionInfo error
                 (swap! diagnostics conj (diagnostic (error-code error) (ex-message error)
                                                      (error-details error)))
                 nil))
        path (str (or root (canonical-path repository)))
        branch (if root (branch-info root) {:ref nil :branch nil :detached nil})
        candidate (if root
                    (try {:commit (revision root "HEAD^{commit}" "HEAD commit")
                          :tree (revision root "HEAD^{tree}" "HEAD tree")}
                         (catch clojure.lang.ExceptionInfo error
                           (swap! diagnostics conj (diagnostic (error-code error) (ex-message error)
                                                                (error-details error)))
                           {:commit nil :tree nil}))
                    {:commit nil :tree nil})
        base (if root
               (try (let [commit (revision root base-ref "base commit")]
                      {:ref base-ref :resolved true :commit commit
                       :tree (revision root (str commit "^{tree}") "base tree")})
                    (catch clojure.lang.ExceptionInfo error
                      (swap! diagnostics conj (diagnostic (error-code error) (ex-message error)
                                                           (error-details error)))
                      {:ref base-ref :resolved false :commit nil :tree nil}))
               {:ref base-ref :resolved false :commit nil :tree nil})
        base-match (identity-match? candidate-base (:commit base))
        commit-match (identity-match? candidate-commit (:commit candidate))
        tree-match (identity-match? candidate-tree (:tree candidate))
        identity-match (and base-match commit-match tree-match)
        expected-identities-complete?
        (every? #(and (some? %) (not (str/blank? (str %))))
                [candidate-base candidate-commit candidate-tree])
        candidate-identities-match
        (and identity-match
             (or (= mode "inspect") expected-identities-complete?))
        tree (if root
               (try (working-tree root exclusions)
                    (catch clojure.lang.ExceptionInfo error
                      (swap! diagnostics conj (diagnostic (error-code error) (ex-message error)
                                                           (error-details error)))
                      {:state "unavailable" :entries [] :allowed-entries [] :blocked-entries []
                       :tracked-changes [] :untracked-changes [] :exclusions exclusions}))
               {:state "unavailable" :entries [] :allowed-entries [] :blocked-entries []
                :tracked-changes [] :untracked-changes [] :exclusions exclusions})
        reconciliation (reconcile root base candidate)
        relation-pass (contains? #{"identical" "tree_equivalent_squash" "descendant"}
                                 (:relation reconciliation))
        _ (when (and root (= mode "integration") (= "dirty" (:state tree)))
            (swap! diagnostics conj (diagnostic "WORKTREE-DIRTY"
                                                 "integration preflight requires a clean worktree"
                                                 {:entries (:entries tree)})))
        _ (when (and root (= mode "integration") (:detached branch))
            (swap! diagnostics conj (diagnostic "WORKTREE-DETACHED"
                                                 "integration preflight rejects detached HEAD" {})))
        _ (when (and root (= mode "integration") (not expected-identities-complete?))
            (swap! diagnostics conj
                   (diagnostic "WORKTREE-EXPECTED-IDENTITIES-MISSING"
                               "integration preflight requires candidate base, commit, and tree identities"
                               {:missing (vec (remove nil?
                                                       [(when-not (and (some? candidate-base)
                                                                        (not (str/blank? (str candidate-base))))
                                                          "candidate-base")
                                                        (when-not (and (some? candidate-commit)
                                                                        (not (str/blank? (str candidate-commit))))
                                                          "candidate-commit")
                                                        (when-not (and (some? candidate-tree)
                                                                        (not (str/blank? (str candidate-tree))))
                                                          "candidate-tree")]))})))
        _ (when (and candidate-base (not base-match))
            (swap! diagnostics conj (diagnostic "WORKTREE-CANDIDATE-BASE-MISMATCH"
                                                 "candidate base identity does not match the resolved base"
                                                 {:expected candidate-base :actual (:commit base)})))
        _ (when (and candidate-commit (not commit-match))
            (swap! diagnostics conj (diagnostic "WORKTREE-CANDIDATE-COMMIT-MISMATCH"
                                                 "candidate commit identity does not match HEAD"
                                                 {:expected candidate-commit :actual (:commit candidate)})))
        _ (when (and candidate-tree (not tree-match))
            (swap! diagnostics conj (diagnostic "WORKTREE-CANDIDATE-TREE-MISMATCH"
                                                 "candidate tree identity does not match HEAD"
                                                 {:expected candidate-tree :actual (:tree candidate)})))
        worktree-inventory (if root
                             (try (worktrees root exclusions)
                                  (catch clojure.lang.ExceptionInfo error
                                    (swap! diagnostics conj (diagnostic (error-code error) (ex-message error)
                                                                         (error-details error)))
                                    []))
                             [])
        all-pass (and root (:resolved base) (= "clean" (:state tree))
                      (not (:detached branch)) candidate-identities-match relation-pass)
        diagnostics (->> @diagnostics (sort-by (juxt :code :message)) vec)]
    {:schema schema
     :read-only true
     :mode mode
     :repository {:path path :branch (:branch branch) :ref (:ref branch)
                  :detached (:detached branch) :head (:commit candidate) :tree (:tree candidate)}
     :base {:ref (:ref base) :resolved (:resolved base) :commit (:commit base) :tree (:tree base)}
     :candidate {:commit (:commit candidate) :tree (:tree candidate)}
     :reconciliation reconciliation
     :working-tree tree
     :worktrees worktree-inventory
     :preconditions {:base-resolved (:resolved base)
                     :candidate-identities-match candidate-identities-match
                     :clean-worktree (= "clean" (:state tree))
                     :named-branch (not (:detached branch))
                     :reconciliation-allowed relation-pass
                     :all-pass (and all-pass (empty? diagnostics))}
     :recommendations (vec (distinct (remove nil?
                                            [(when (= "no_remerge" (:recommendation reconciliation))
                                               "no_remerge")
                                             (:recommendation reconciliation)])))
     :diagnostics diagnostics
     :observed-identities {:expected-base candidate-base
                           :expected-commit candidate-commit
                           :expected-tree candidate-tree}}))

(defn run-preflight
  "Return `[document exit-code]` for a repository.

  Options are `:mode` (`\"integration\"` or `\"inspect\"`), `:base-ref`,
  `:candidate-base`, `:candidate-commit`, `:candidate-tree`, and repeated
  `:allow-output` repository-relative paths.  Integration requires all three
  expected identities and a truly clean worktree; inspect is discovery-only
  and returns zero for a report even when integration gates fail."
  ([repository] (run-preflight repository {}))
  ([repository options]
   (let [options (assoc options :mode (if (= "inspect" (:mode options))
                                        "inspect"
                                        "integration"))]
     (try
       (let [document (preflight-document repository options)]
         [document (if (or (= "inspect" (:mode document))
                          (get-in document [:preconditions :all-pass])) 0 1)])
       (catch clojure.lang.ExceptionInfo error
         [{:schema schema :read-only true :mode (:mode options)
           :repository {:path (str repository) :resolved false}
           :base {:ref (or (:base-ref options) default-base-ref) :resolved false}
           :candidate {:commit nil :tree nil}
           :reconciliation {:relation "unavailable" :base-is-ancestor nil
                            :candidate-is-ancestor nil :tree-equivalent false
                            :recommendation "resolve_base_and_candidate_identities"}
           :working-tree {:state "unavailable" :entries [] :allowed-entries []
                          :blocked-entries [] :tracked-changes [] :untracked-changes []
                          :exclusions []}
           :worktrees []
           :preconditions {:base-resolved false :candidate-identities-match false
                           :clean-worktree false :named-branch false
                           :reconciliation-allowed false :all-pass false}
           :recommendations []
           :diagnostics [(diagnostic (error-code error) (ex-message error)
                                     (error-details error))]}
          1])))))

(defn- json-key [key]
  (cond (keyword? key) (name key)
        (symbol? key) (name key)
        :else (str key)))

(defn- json-escape [value]
  (apply str
         (map (fn [character]
                (case character
                  \" "\\\""
                  \\ "\\\\"
                  \backspace "\\b"
                  \formfeed "\\f"
                  \newline "\\n"
                  \return "\\r"
                  \tab "\\t"
                  (if (< (int character) 0x20)
                    (format "\\u%04x" (int character))
                    (str character))))
              (str value))))

(declare json-write)

(defn- json-write [value]
  (cond
    (nil? value) "null"
    (true? value) "true"
    (false? value) "false"
    (string? value) (str "\"" (json-escape value) "\"")
    (number? value) (str value)
    (map? value) (str "{" (str/join ","
                                         (map (fn [[key item]]
                                                (str (json-write (json-key key)) ":"
                                                     (json-write item)))
                                              (sort-by (comp str key) value))) "}")
    (or (vector? value) (list? value) (seq? value) (set? value))
    (str "[" (str/join "," (map json-write value)) "]")
    :else (json-write (str value))))

(defn- parse-args [arguments]
  (loop [arguments (seq arguments)
         options {:repo "." :mode "integration" :base-ref default-base-ref :allow-output []}]
    (if-not arguments
      options
      (let [option (first arguments)
            remainder (next arguments)
            value (fn [label]
                    (if (seq remainder)
                      (first remainder)
                      (throw (failure "WORKTREE-CLI-ARGUMENT"
                                      (str label " requires a value")))))]
        (case option
          "--repo" (recur (next remainder) (assoc options :repo (value option)))
          "--mode" (let [mode (value option)]
                      (when-not #{"inspect" "integration"} mode
                        (throw (failure "WORKTREE-CLI-ARGUMENT"
                                        "--mode must be inspect or integration"
                                        {:mode mode})))
                      (recur (next remainder) (assoc options :mode mode)))
          "--base-ref" (recur (next remainder) (assoc options :base-ref (value option)))
          "--candidate-base" (recur (next remainder) (assoc options :candidate-base (value option)))
          "--expected-base" (recur (next remainder) (assoc options :candidate-base (value option)))
          "--candidate-commit" (recur (next remainder) (assoc options :candidate-commit (value option)))
          "--expected-commit" (recur (next remainder) (assoc options :candidate-commit (value option)))
          "--candidate-tree" (recur (next remainder) (assoc options :candidate-tree (value option)))
          "--expected-tree" (recur (next remainder) (assoc options :candidate-tree (value option)))
          "--allow-output" (recur (next remainder)
                                  (update options :allow-output conj (value option)))
          "--help" (assoc options :help true)
          (throw (failure "WORKTREE-CLI-ARGUMENT"
                          (str "unknown option: " option)
                          {:option option})))))))

(defn -main [& arguments]
  (try
    (let [options (parse-args arguments)]
      (if (:help options)
        (do
          (println "Usage: clojure -M tools/check_worktree_preflight.clj [options]")
          (println "  --repo PATH --mode inspect|integration --base-ref REF")
          (println "  integration requires --candidate-base OID --candidate-commit OID --candidate-tree OID")
          (println "  --allow-output RELATIVE-PATH (inspection classification only; repeatable)")
          0)
        (let [[document exit-code] (run-preflight (:repo options) options)]
          (println (json-write document))
          (flush)
          exit-code)))
    (catch clojure.lang.ExceptionInfo error
      (println (json-write {:schema schema :read-only true :mode "integration"
                            :diagnostics [(diagnostic (error-code error)
                                                       (ex-message error)
                                                       (error-details error))]}))
      2)))

;; `clojure tools/check_worktree_preflight.clj ...` loads a file rather than
;; calling a namespace's `-main`.  Clojure binds *command-line-args* for that
;; script path, while callers that load this file as a library normally leave
;; it nil.  Keep the guard small so focused tests can load the namespace with a
;; nil binding and never terminate their test JVM.
(when (some? *command-line-args*)
  (System/exit (int (apply -main *command-line-args*))))
