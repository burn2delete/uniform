(ns gravity.workstream-governance
  "Closed, fail-closed validation for the workstream lifecycle ledger."
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io PushbackReader StringReader)
           (java.nio.charset CharacterCodingException CodingErrorAction StandardCharsets)
           (java.nio.file Files LinkOption Path Paths)
           (java.time Instant DateTimeException)))

(def ^:private maximum-json-bytes (* 2 1024 1024))

(def ^:private contract-keys
  #{"schema_version" "contract_id" "normative_sources" "ledger_schema"
    "lifecycle" "admission_policy" "diagnostics" "nonclaims"})

(def ^:private ledger-schema-keys
  #{"contract_id" "top_level_keys" "workstream_keys" "evidence_keys"
    "validation_command_keys" "review_keys" "history_event_keys"
    "authority_keys"})

(def ^:private lifecycle-keys
  #{"states" "active_candidate_states" "failure_states" "terminal_states"
    "transitions"})

(def ^:private admission-keys
  #{"one_active_candidate_per_invariant_family"
    "architecture_decision_after_failures" "dependency_state_floor"
    "integration_eligible_requires" "review_kinds" "review_results"
    "self_audit_confers_eligibility" "commit_pattern"
    "no_overclaim_authority"})

(def ^:private ledger-keys
  #{"schema_version" "contract_id" "governance_contract" "workstreams"})

(def ^:private workstream-keys
  #{"architecture_decision" "author" "base_commit" "candidate_commit"
    "clean_worktree" "dependencies" "disposition" "evidence"
    "governing_contracts" "history" "id" "invariant_family"
    "no_overclaim_authority" "owned_paths" "owner"
    "residual_host_boundaries" "reviews" "state" "title"})

(def ^:private evidence-keys
  #{"accepted_fixtures" "rejected_fixtures" "stable_diagnostics"
    "validation_commands"})
(def ^:private command-keys #{"command" "exit_code" "result"})
(def ^:private review-keys #{"kind" "result" "reviewer"})
(def ^:private history-keys #{"actor" "at" "reason" "state"})
(def ^:private authority-keys
  #{"integration_only" "release" "seed_retirement" "self_hosting"})

(def ^:private states
  ["draft" "frozen" "review-pending" "accepted" "integration-eligible"
   "integrated" "held" "rejected" "superseded" "abandoned"])
(def ^:private state-set (set states))
(def ^:private active-states
  ["draft" "frozen" "review-pending" "accepted" "integration-eligible"])
(def ^:private active-state-set (set active-states))
(def ^:private failure-states ["rejected"])
(def ^:private terminal-states
  ["integrated" "rejected" "superseded" "abandoned"])
(def ^:private transitions
  {"draft" ["frozen" "held" "rejected" "superseded" "abandoned"]
   "frozen" ["draft" "review-pending" "held" "rejected" "superseded"
             "abandoned"]
   "review-pending" ["draft" "accepted" "held" "rejected" "superseded"
                      "abandoned"]
   "accepted" ["integration-eligible" "held" "rejected" "superseded"
                "abandoned"]
   "integration-eligible" ["integrated" "held" "rejected" "superseded"
                            "abandoned"]
   "held" ["draft" "frozen" "rejected" "superseded" "abandoned"]
   "integrated" []
   "rejected" []
   "superseded" []
   "abandoned" []})
(def ^:private dependency-floor
  {"review-pending" ["accepted" "integration-eligible" "integrated"]
   "accepted" ["accepted" "integration-eligible" "integrated"]
   "integration-eligible" ["integrated"]
   "integrated" ["integrated"]})
(def ^:private review-kinds ["independent" "self-audit"])
(def ^:private review-results ["accepted" "changes-requested" "rejected"])
(def ^:private required-eligibility
  ["exact_base_commit" "exact_candidate_commit" "clean_worktree"
   "owned_paths" "governing_contracts" "accepted_fixtures"
   "rejected_fixtures" "stable_diagnostics"
   "successful_validation_commands" "independent_accepted_review"
   "residual_host_boundaries" "no_overclaim_authority"])
(def ^:private no-overclaim
  {"integration_only" true
   "release" false
   "seed_retirement" false
   "self_hosting" false})
(def ^:private normative-sources
  ["docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md"
   "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
   "docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md"
   "docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md"
   "docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md"
   "docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md"
   "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"
   "docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md"])
(def ^:private canonical-diagnostics
  {"WG001" "strict JSON decoding or top-level schema failed"
   "WG002" "the governance contract is malformed or weakened"
   "WG003" "a workstream record or nested record is not closed"
   "WG004" "a lifecycle history or transition is illegal"
   "WG005" "an identifier, invariant family, owner, or list is invalid or duplicated"
   "WG006" "more than one active candidate occupies an invariant family"
   "WG007" "a dependency is missing, cyclic, or below its required admission state"
   "WG008" "two failed candidates require a nonempty architecture decision before new active work"
   "WG009" "integration evidence is incomplete or malformed"
   "WG010" "an independent accepted review is absent or is not independent"
   "WG011" "a self-audit attempted to confer integration eligibility"
   "WG012" "an authority or product-completion overclaim was attempted"})
(def ^:private nonclaims
  ["Ledger acceptance does not establish implementation correctness."
   "Integration eligibility is authority only to request integration of the exact candidate commit over the exact base commit."
   "A self-audit never confers integration eligibility."
   "Integrated work does not by itself establish release, self-hosting, or seed-retirement authority."
   "Held, rejected, superseded, and abandoned work receives no roadmap completion credit."])

(defn- json-error [message data]
  (throw (ex-info message (assoc data :diagnostic "WG001"))))

(defn- skip-json-whitespace! [^PushbackReader reader]
  (loop [value (.read reader)]
    (if (and (not= -1 value)
             (contains? #{\space \tab \newline \return} (char value)))
      (recur (.read reader))
      value)))

(defn- unread-json! [^PushbackReader reader value]
  (when-not (= -1 value) (.unread reader value)))

(defn- read-json-string! [^PushbackReader reader]
  (let [output (StringBuilder.)]
    (loop []
      (let [value (.read reader)]
        (cond
          (= -1 value) (json-error "Unterminated JSON string" {})
          (= \" (char value)) (str output)
          (= \\ (char value))
          (let [escaped (.read reader)]
            (when (= -1 escaped) (json-error "Unterminated JSON escape" {}))
            (case (char escaped)
              \" (.append output \" )
              \\ (.append output \\)
              \/ (.append output \/)
              \b (.append output \backspace)
              \f (.append output \formfeed)
              \n (.append output \newline)
              \r (.append output \return)
              \t (.append output \tab)
              \u (let [values (repeatedly 4 #(.read reader))]
                   (when (some #(= -1 %) values)
                     (json-error "Unterminated JSON unicode escape" {}))
                   (let [digits (apply str (map char values))]
                     (when-not (re-matches #"[0-9A-Fa-f]{4}" digits)
                       (json-error "Malformed JSON unicode escape"
                                   {:digits digits}))
                     (.append output (char (Integer/parseInt digits 16)))))
              (json-error "Unsupported JSON escape"
                          {:escape (str (char escaped))}))
            (recur))
          (< value 0x20)
          (json-error "JSON string contains an unescaped control character"
                      {:codepoint value})
          :else (do (.append output (char value)) (recur)))))))

(declare read-json-value!)

(defn- read-json-literal! [^PushbackReader reader first-character suffix value]
  (doseq [expected suffix]
    (let [actual (.read reader)]
      (when (or (= -1 actual) (not= expected (char actual)))
        (json-error "Malformed JSON literal"
                    {:literal (str first-character suffix)}))))
  value)

(defn- read-json-number! [^PushbackReader reader first-character]
  (let [token
        (loop [output (StringBuilder. (str first-character))]
          (let [value (.read reader)]
            (if (and (not= -1 value)
                     (re-matches #"[0-9eE+\-.]" (str (char value))))
              (recur (.append output (char value)))
              (do (unread-json! reader value) (str output)))))]
    (when-not (re-matches #"-?(?:0|[1-9][0-9]*)(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?"
                          token)
      (json-error "Malformed JSON number" {:token token}))
    (try
      (if (re-find #"[.eE]" token) (bigdec token) (bigint token))
      (catch NumberFormatException exception
        (json-error "JSON number is out of range"
                    {:token token :cause (.getMessage exception)})))))

(defn- read-json-array! [^PushbackReader reader]
  (let [first-value (skip-json-whitespace! reader)]
    (if (= (int \]) first-value)
      []
      (do
        (unread-json! reader first-value)
        (loop [values []]
          (let [value (read-json-value! reader)
                delimiter (skip-json-whitespace! reader)]
            (cond
              (= (int \,) delimiter) (recur (conj values value))
              (= (int \]) delimiter) (conj values value)
              :else (json-error "JSON array requires comma or closing bracket"
                                {:delimiter delimiter}))))))))

(defn- read-json-object! [^PushbackReader reader]
  (let [first-key (skip-json-whitespace! reader)]
    (if (= (int \}) first-key)
      {}
      (do
        (unread-json! reader first-key)
        (loop [result {}]
          (let [quote-value (skip-json-whitespace! reader)]
            (when-not (= (int \") quote-value)
              (json-error "JSON object key must be a string"
                          {:value quote-value}))
            (let [key (read-json-string! reader)
                  colon (skip-json-whitespace! reader)]
              (when (contains? result key)
                (json-error "JSON object repeats a key" {:key key}))
              (when-not (= (int \:) colon)
                (json-error "JSON object key requires a colon" {:key key}))
              (let [value (read-json-value! reader)
                    delimiter (skip-json-whitespace! reader)
                    updated (assoc result key value)]
                (cond
                  (= (int \,) delimiter) (recur updated)
                  (= (int \}) delimiter) updated
                  :else (json-error "JSON object requires comma or closing brace"
                                    {:key key :delimiter delimiter}))))))))))

(defn- read-json-value! [^PushbackReader reader]
  (let [value (skip-json-whitespace! reader)]
    (when (= -1 value) (json-error "JSON value is missing" {}))
    (let [character (char value)]
      (case character
        \{ (read-json-object! reader)
        \[ (read-json-array! reader)
        \" (read-json-string! reader)
        \t (read-json-literal! reader character "rue" true)
        \f (read-json-literal! reader character "alse" false)
        \n (read-json-literal! reader character "ull" nil)
        (if (or (= character \-) (Character/isDigit character))
          (read-json-number! reader character)
          (json-error "Unsupported JSON value" {:value (str character)}))))))

(defn read-strict-json
  "Reads one strict JSON value. Duplicate object members are rejected."
  [text]
  (with-open [reader (PushbackReader. (StringReader. (str text)))]
    (let [value (read-json-value! reader)
          trailing (skip-json-whitespace! reader)]
      (when-not (= -1 trailing)
        (json-error "Trailing data follows the JSON document"
                    {:value trailing}))
      value)))

(defn- strict-utf8 [bytes]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (str (.decode decoder (java.nio.ByteBuffer/wrap bytes)))
      (catch CharacterCodingException exception
        (json-error "JSON document is not UTF-8"
                    {:cause (.getMessage exception)})))))

(defn load-json
  "Loads a bounded UTF-8 JSON file using strict object semantics."
  [path]
  (let [resolved (.normalize (.toAbsolutePath (Paths/get (str path) (make-array String 0))))
        bytes (Files/readAllBytes resolved)]
    (when (> (alength bytes) maximum-json-bytes)
      (json-error "JSON document exceeds byte limit"
                  {:path (str resolved) :maximum maximum-json-bytes}))
    (read-strict-json (strict-utf8 bytes))))

(defn- issue [code location message]
  (str code " " location ": " message))

(defn- closed-errors [value expected location code]
  (if-not (map? value)
    [(issue code location "must be an object")]
    (let [observed (set (keys value))
          missing (sort (remove observed expected))
          unknown (sort (remove expected observed))]
      (cond-> []
        (seq missing) (conj (issue code location (str "missing keys " missing)))
        (seq unknown) (conj (issue code location (str "unknown keys " unknown)))))))

(defn- nonempty-text? [value]
  (and (string? value) (not (str/blank? value))))

(defn- string-list-errors
  ([value location] (string-list-errors value location false false))
  ([value location require-nonempty? path-like?]
   (if-not (vector? value)
     [(issue "WG005" location "must be a list")]
     (let [base (if (and require-nonempty? (empty? value))
                  [(issue "WG005" location "must not be empty")]
                  [])]
       (loop [remaining value index 0 seen #{} errors base]
         (if (seq remaining)
           (let [item (first remaining)
                 item-location (str location "[" index "]")
                 text-ok? (nonempty-text? item)
                 unsafe? (and text-ok? path-like?
                              (or (str/starts-with? item "/")
                                  (str/starts-with? item "~")
                                  (str/includes? item "\\")
                                  (some #{".."} (str/split item #"/"))))
                 errors (cond-> errors
                          (not text-ok?)
                          (conj (issue "WG005" item-location
                                       "must be a nonempty string"))
                          (and text-ok? (contains? seen item))
                          (conj (issue "WG005" item-location
                                       "must not duplicate an earlier value"))
                          unsafe?
                          (conj (issue "WG005" item-location
                                       "must be a safe repository-relative path")))]
             (recur (next remaining) (inc index)
                    (if text-ok? (conj seen item) seen) errors))
           errors))))))

(defn- exact-error [value expected location]
  (when-not (= value expected)
    [(issue "WG002" location (str "must remain exactly " (pr-str expected)))]))

(defn validate-contract
  "Validates that the governance contract exactly retains the v1 policy."
  [contract]
  (let [shape-errors (closed-errors contract contract-keys "contract" "WG002")]
    (if (seq shape-errors)
      shape-errors
      (let [schema (get contract "ledger_schema")
            lifecycle (get contract "lifecycle")
            admission (get contract "admission_policy")
            diagnostics (get contract "diagnostics")
            expected-schema
            {"top_level_keys" (sort ledger-keys)
             "workstream_keys" (sort workstream-keys)
             "evidence_keys" (sort evidence-keys)
             "validation_command_keys" (sort command-keys)
             "review_keys" (sort review-keys)
             "history_event_keys" (sort history-keys)
             "authority_keys" (sort authority-keys)}]
        (vec
         (concat
          (when-not (and (integer? (get contract "schema_version"))
                         (= 1 (get contract "schema_version")))
            [(issue "WG002" "contract.schema_version" "must be 1")])
          (when-not (= "gravity/workstream-governance-v1"
                       (get contract "contract_id"))
            [(issue "WG002" "contract.contract_id" "unsupported contract")])
          (exact-error (get contract "normative_sources") normative-sources
                       "contract.normative_sources")
          (closed-errors schema ledger-schema-keys "contract.ledger_schema"
                         "WG002")
          (when (map? schema)
            (concat
             (when-not (= "gravity/workstream-ledger-v1"
                          (get schema "contract_id"))
               [(issue "WG002" "contract.ledger_schema.contract_id"
                       "unsupported ledger contract")])
             (mapcat (fn [[key expected]]
                       (exact-error (get schema key) expected
                                    (str "contract.ledger_schema." key)))
                     expected-schema)))
          (closed-errors lifecycle lifecycle-keys "contract.lifecycle" "WG002")
          (when (map? lifecycle)
            (concat
             (exact-error (get lifecycle "states") states
                          "contract.lifecycle.states")
             (exact-error (get lifecycle "active_candidate_states") active-states
                          "contract.lifecycle.active_candidate_states")
             (exact-error (get lifecycle "failure_states") failure-states
                          "contract.lifecycle.failure_states")
             (exact-error (get lifecycle "terminal_states") terminal-states
                          "contract.lifecycle.terminal_states")
             (when-not (= transitions (get lifecycle "transitions"))
               [(issue "WG002" "contract.lifecycle.transitions"
                       "transition graph was changed")])))
          (closed-errors admission admission-keys "contract.admission_policy"
                         "WG002")
          (when (map? admission)
            (concat
             (when-not (true? (get admission
                                   "one_active_candidate_per_invariant_family"))
               [(issue "WG002"
                       "contract.admission_policy.one_active_candidate_per_invariant_family"
                       "must remain true")])
             (when-not (and
                        (integer? (get admission
                                       "architecture_decision_after_failures"))
                        (= 2 (get admission
                                  "architecture_decision_after_failures")))
               [(issue "WG002"
                       "contract.admission_policy.architecture_decision_after_failures"
                       "must remain 2")])
             (when-not (false? (get admission "self_audit_confers_eligibility"))
               [(issue "WG002"
                       "contract.admission_policy.self_audit_confers_eligibility"
                       "must remain false")])
             (when-not (= "^[0-9a-f]{40}$" (get admission "commit_pattern"))
               [(issue "WG002" "contract.admission_policy.commit_pattern"
                       "must remain ^[0-9a-f]{40}$")])
             (when-not (= dependency-floor (get admission "dependency_state_floor"))
               [(issue "WG002" "contract.admission_policy.dependency_state_floor"
                       "dependency floors were changed")])
             (exact-error (get admission "integration_eligible_requires")
                          required-eligibility
                          "contract.admission_policy.integration_eligible_requires")
             (exact-error (get admission "review_kinds") review-kinds
                          "contract.admission_policy.review_kinds")
             (exact-error (get admission "review_results") review-results
                          "contract.admission_policy.review_results")
             (when-not (= no-overclaim (get admission "no_overclaim_authority"))
               [(issue "WG002"
                       "contract.admission_policy.no_overclaim_authority"
                       "authority ceiling was changed")])))
          (when-not (= canonical-diagnostics diagnostics)
            [(issue "WG002" "contract.diagnostics"
                    "diagnostic meanings were changed")])
          (exact-error (get contract "nonclaims") nonclaims
                       "contract.nonclaims")))))))

(defn- timestamp? [value]
  (and (string? value)
       (re-matches #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z" value)
       (try (Instant/parse value) true
            (catch DateTimeException _ false))))

(defn- command-errors [commands location]
  (if-not (vector? commands)
    [(issue "WG003" location "must be a list")]
    (vec
     (mapcat
      (fn [[index command]]
        (let [where (str location "[" index "]")]
          (concat
           (closed-errors command command-keys where "WG003")
           (when (map? command)
             (concat
              (when-not (nonempty-text? (get command "command"))
                [(issue "WG009" (str where ".command")
                        "must be a nonempty string")])
              (when-not (integer? (get command "exit_code"))
                [(issue "WG009" (str where ".exit_code")
                        "must be an integer")])
              (when-not (contains? #{"passed" "failed"} (get command "result"))
                [(issue "WG009" (str where ".result")
                        "must be passed or failed")]))))))
      (map-indexed vector commands)))))

(defn- review-errors [reviews location]
  (if-not (vector? reviews)
    [(issue "WG003" location "must be a list")]
    (vec
     (mapcat
      (fn [[index review]]
        (let [where (str location "[" index "]")]
          (concat
           (closed-errors review review-keys where "WG003")
           (when (map? review)
             (concat
              (when-not (nonempty-text? (get review "reviewer"))
                [(issue "WG003" (str where ".reviewer")
                        "must be a nonempty string")])
              (when-not (contains? (set review-kinds) (get review "kind"))
                [(issue "WG003" (str where ".kind") "unknown review kind")])
              (when-not (contains? (set review-results) (get review "result"))
                [(issue "WG003" (str where ".result")
                        "unknown review result")]))))))
      (map-indexed vector reviews)))))

(defn- history-errors [history current-state location]
  (if-not (and (vector? history) (seq history))
    [(issue "WG004" location "must be a nonempty transition history")]
    (loop [events history index 0 previous-state nil previous-time nil errors []]
      (if (seq events)
        (let [event (first events)
              where (str location "[" index "]")
              state (when (map? event) (get event "state"))
              stamp (when (map? event) (get event "at"))
              parsed (when (timestamp? stamp) (Instant/parse stamp))
              next-errors
              (into errors
                    (concat
                     (closed-errors event history-keys where "WG003")
                     (when-not (contains? state-set state)
                       [(issue "WG004" (str where ".state")
                               "unknown lifecycle state")])
                     (when-not (nonempty-text? (when (map? event)
                                                (get event "actor")))
                       [(issue "WG004" (str where ".actor")
                               "must be a nonempty string")])
                     (when-not (nonempty-text? (when (map? event)
                                                (get event "reason")))
                       [(issue "WG004" (str where ".reason")
                               "must be a nonempty string")])
                     (when-not parsed
                       [(issue "WG004" (str where ".at")
                               "must be canonical UTC YYYY-MM-DDTHH:MM:SSZ")])
                     (when (and parsed previous-time
                                (not (.isAfter parsed previous-time)))
                       [(issue "WG004" (str where ".at")
                               "must increase strictly")])
                     (when (and (zero? index) (not= "draft" state))
                       [(issue "WG004" (str where ".state")
                               "history must begin in draft")])
                     (when (and previous-state
                                (not (contains? (set (get transitions previous-state []))
                                                state)))
                       [(issue "WG004" (str where ".state")
                               (str "illegal transition " previous-state
                                    " -> " state))])))]
          (recur (next events) (inc index) state (or parsed previous-time)
                 next-errors))
        (cond-> errors
          (not= current-state (get (last history) "state"))
          (conj (issue "WG004" location
                       "final history state must match the workstream state")))))))

(defn- record-shape-errors [item location]
  (let [shape (closed-errors item workstream-keys location "WG003")]
    (if (seq shape)
      shape
      (let [evidence (get item "evidence")
            authority (get item "no_overclaim_authority")]
        (vec
         (concat
          (mapcat (fn [field]
                    (when-not (nonempty-text? (get item field))
                      [(issue "WG005" (str location "." field)
                              "must be a nonempty string")]))
                  ["id" "title" "invariant_family" "owner" "author"
                   "disposition"])
          (mapcat (fn [field]
                    (let [value (get item field)]
                      (when (and (string? value)
                                 (not (re-matches #"[a-z0-9][a-z0-9./-]*" value)))
                        [(issue "WG005" (str location "." field)
                                "must use lowercase stable identifier syntax")])))
                  ["id" "invariant_family"])
          (when-not (contains? state-set (get item "state"))
            [(issue "WG004" (str location ".state") "unknown lifecycle state")])
          (string-list-errors (get item "dependencies")
                              (str location ".dependencies"))
          (when-not (string? (get item "architecture_decision"))
            [(issue "WG003" (str location ".architecture_decision")
                    "must be a string")])
          (mapcat (fn [field]
                    (let [value (get item field)]
                      (when-not (or (nil? value) (string? value))
                        [(issue "WG003" (str location "." field)
                                "must be null or a string")])))
                  ["base_commit" "candidate_commit"])
          (when-not (instance? Boolean (get item "clean_worktree"))
            [(issue "WG003" (str location ".clean_worktree")
                    "must be a boolean fact")])
          (string-list-errors (get item "owned_paths")
                              (str location ".owned_paths") false true)
          (string-list-errors (get item "governing_contracts")
                              (str location ".governing_contracts") true false)
          (string-list-errors (get item "residual_host_boundaries")
                              (str location ".residual_host_boundaries"))
          (closed-errors evidence evidence-keys (str location ".evidence") "WG003")
          (when (map? evidence)
            (concat
             (mapcat #(string-list-errors
                       (get evidence %) (str location ".evidence." %))
                     ["accepted_fixtures" "rejected_fixtures"
                      "stable_diagnostics"])
             (command-errors (get evidence "validation_commands")
                             (str location ".evidence.validation_commands"))))
          (review-errors (get item "reviews") (str location ".reviews"))
          (closed-errors authority authority-keys
                         (str location ".no_overclaim_authority") "WG003")
          (when (map? authority)
            (concat
             (when-not (every? #(instance? Boolean %) (vals authority))
               [(issue "WG003" (str location ".no_overclaim_authority")
                       "all authority facts must be booleans")])
             (when-not (= no-overclaim authority)
               [(issue "WG012" (str location ".no_overclaim_authority")
                       "must remain integration-only and deny product authority")])))
          (history-errors (get item "history") (get item "state")
                          (str location ".history"))))))))

(defn- eligibility-errors [item location]
  (if-not (contains? #{"integration-eligible" "integrated"} (get item "state"))
    []
    (let [evidence (get item "evidence")
          reviews (get item "reviews")
          author (get item "author")
          independent? (and (vector? reviews)
                            (some #(and (= "independent" (get % "kind"))
                                        (= "accepted" (get % "result"))
                                        (not= author (get % "reviewer")))
                                  reviews))
          self-accepted? (and (vector? reviews)
                              (some #(and (= "self-audit" (get % "kind"))
                                          (= "accepted" (get % "result")))
                                    reviews))]
      (vec
       (concat
        (mapcat
         (fn [field]
           (when-not (and (string? (get item field))
                          (re-matches #"[0-9a-f]{40}" (get item field)))
             [(issue "WG009" (str location "." field)
                     "must be an exact lowercase 40-hex commit")]))
         ["base_commit" "candidate_commit"])
        (when (= (get item "base_commit") (get item "candidate_commit"))
          [(issue "WG009" location "base and candidate commits must differ")])
        (when-not (true? (get item "clean_worktree"))
          [(issue "WG009" (str location ".clean_worktree")
                  "must be true at evidence capture")])
        (mapcat
         (fn [field]
           (when-not (seq (get item field))
             [(issue "WG009" (str location "." field)
                     "must contain admission evidence")]))
         ["owned_paths" "governing_contracts"])
        (when (map? evidence)
          (concat
           (mapcat
            (fn [field]
              (when-not (seq (get evidence field))
                [(issue "WG009" (str location ".evidence." field)
                        "must contain admission evidence")]))
            ["accepted_fixtures" "rejected_fixtures" "stable_diagnostics"
             "validation_commands"])
           (mapcat
            (fn [[index command]]
              (when-not (and (= 0 (get command "exit_code"))
                             (= "passed" (get command "result")))
                [(issue "WG009"
                        (str location ".evidence.validation_commands[" index "]")
                        "must record exit 0 and passed")]))
            (map-indexed vector (if (vector? (get evidence "validation_commands"))
                                  (get evidence "validation_commands") [])))))
        (when-not independent?
          [(issue (if self-accepted? "WG011" "WG010") (str location ".reviews")
                  (if self-accepted?
                    "a self-audit cannot confer integration eligibility"
                    "requires an independent accepted reviewer distinct from the author"))])
        (when-not (= no-overclaim (get item "no_overclaim_authority"))
          [(issue "WG012" (str location ".no_overclaim_authority")
                  "product authority exceeds integration-only admission")]))))))

(defn- dependency-errors [items by-id]
  (let [basic
        (mapcat
         (fn [[index item]]
           (let [location (str "ledger.workstreams[" index "]")
                 floor (set (get dependency-floor (get item "state") []))]
             (mapcat
              (fn [dependency]
                (if-let [target (get by-id dependency)]
                  (when (and (seq floor)
                             (not (contains? floor (get target "state"))))
                    [(issue "WG007" (str location ".dependencies")
                            (str dependency " is below the required state floor"))])
                  [(issue "WG007" (str location ".dependencies")
                          (str "missing dependency " dependency))]))
              (if (vector? (get item "dependencies"))
                (get item "dependencies") []))))
         (map-indexed vector items))
        graph (into {} (map (fn [item]
                              [(get item "id")
                               (filterv by-id (if (vector? (get item "dependencies"))
                                                (get item "dependencies") []))])
                            items))
        cycle-errors
        (loop [remaining (keys graph) visited #{} errors []]
          (if-let [start (first remaining)]
            (if (contains? visited start)
              (recur (next remaining) visited errors)
              (letfn [(walk [node visiting done]
                        (cond
                          (contains? visiting node) [done true]
                          (contains? done node) [done false]
                          :else
                          (loop [deps (get graph node [])
                                 done done]
                            (if-let [dependency (first deps)]
                              (let [[done cycle?]
                                    (walk dependency (conj visiting node) done)]
                                (if cycle? [done true]
                                    (recur (next deps) done)))
                              [(conj done node) false]))))]
                (let [[done cycle?] (walk start #{} visited)]
                  (recur (next remaining) done
                         (cond-> errors cycle?
                           (conj (issue "WG007" "ledger.workstreams"
                                        "dependency graph contains a cycle")))))))
            errors))]
    (vec (concat basic cycle-errors))))

(defn validate-ledger
  "Validates an already-decoded workstream ledger and returns diagnostics."
  [ledger]
  (let [shape-errors (closed-errors ledger ledger-keys "ledger" "WG001")]
    (if (seq shape-errors)
      shape-errors
      (let [items (get ledger "workstreams")]
        (if-not (vector? items)
          [(issue "WG001" "ledger.workstreams" "must be a list")]
          (let [indexed (map-indexed vector items)
                ids (map #(get % "id") (filter map? items))
                by-id (into {} (keep (fn [item]
                                       (when (and (map? item) (string? (get item "id")))
                                         [(get item "id") item])) items))
                duplicate-ids (for [[id count] (frequencies ids)
                                    :when (> count 1)] id)
                active-by-family (group-by #(get % "invariant_family")
                                           (filter #(and (map? %)
                                                         (contains? active-state-set
                                                                    (get % "state")))
                                                   items))
                rejection-counts (frequencies
                                  (map #(get % "invariant_family")
                                       (filter #(and (map? %)
                                                     (= "rejected" (get % "state")))
                                               items)))]
            (vec
             (concat
              (when-not (and (integer? (get ledger "schema_version"))
                             (= 1 (get ledger "schema_version")))
                [(issue "WG001" "ledger.schema_version" "must be 1")])
              (when-not (= "gravity/workstream-ledger-v1"
                           (get ledger "contract_id"))
                [(issue "WG001" "ledger.contract_id" "unsupported ledger contract")])
              (when-not (= "contracts/workstream-governance.json"
                           (get ledger "governance_contract"))
                [(issue "WG001" "ledger.governance_contract"
                        "must name contracts/workstream-governance.json")])
              (mapcat (fn [[index item]]
                        (record-shape-errors item
                                             (str "ledger.workstreams[" index "]")))
                      indexed)
              (mapcat (fn [[index item]]
                        (if (map? item)
                          (eligibility-errors item
                                              (str "ledger.workstreams[" index "]"))
                          []))
                      indexed)
              (map #(issue "WG005" "ledger.workstreams"
                           (str "duplicate workstream id " %))
                   duplicate-ids)
              (mapcat
               (fn [[family active]]
                 (when (> (count active) 1)
                   [(issue "WG006" "ledger.workstreams"
                           (str "more than one active candidate for " family))]))
               active-by-family)
              (dependency-errors (filterv map? items) by-id)
              (mapcat
               (fn [[index item]]
                 (when (and (map? item)
                            (contains? active-state-set (get item "state"))
                            (>= (get rejection-counts
                                     (get item "invariant_family") 0) 2)
                            (not (nonempty-text? (get item "architecture_decision"))))
                   [(issue "WG008" (str "ledger.workstreams[" index
                                         "].architecture_decision")
                           "two rejected candidates require an architecture decision")]))
               indexed)))))))))

(defn validate-documents
  "Loads and validates a governance contract and ledger."
  [contract-path ledger-path]
  (try
    (let [contract (load-json contract-path)
          ledger (load-json ledger-path)]
      (vec (concat (validate-contract contract) (validate-ledger ledger))))
    (catch Throwable exception
      [(issue "WG001" "json" (.getMessage exception))])))

(defn validate-current
  "Validates the repository's canonical governance contract and ledger."
  ([] (validate-current "contracts/workstream-governance.json"
                        "contracts/workstream-ledger.json"))
  ([contract-path ledger-path]
   (let [errors (validate-documents contract-path ledger-path)]
     (when (seq errors)
       (throw (ex-info "workstream governance validation failed"
                       {:diagnostics errors})))
     nil)))

(defn- parse-options [arguments]
  (loop [remaining arguments
         options {:contract "contracts/workstream-governance.json"
                  :ledger "contracts/workstream-ledger.json"}]
    (if-let [argument (first remaining)]
      (case argument
        "--contract" (if-let [value (second remaining)]
                       (recur (nnext remaining) (assoc options :contract value))
                       (throw (ex-info "--contract requires a path" {})))
        "--ledger" (if-let [value (second remaining)]
                     (recur (nnext remaining) (assoc options :ledger value))
                     (throw (ex-info "--ledger requires a path" {})))
        (throw (ex-info (str "unknown argument: " argument) {})))
      options)))

(defn -main [& arguments]
  (try
    (let [{:keys [contract ledger]} (parse-options arguments)
          errors (validate-documents contract ledger)]
      (if (seq errors)
        (do (binding [*out* *err*] (doseq [error errors] (println error)))
            (System/exit 1))
        (let [document (load-json ledger)
              counts (sort-by key (frequencies (map #(get % "state")
                                                    (get document "workstreams"))))]
          (println (str "validation passed: "
                        (count (get document "workstreams")) " workstreams; "
                        (str/join ", " (map (fn [[state count]]
                                               (str state "=" count)) counts))))
          (System/exit 0))))
    (catch Throwable exception
      (binding [*out* *err*] (println "WG001 cli:" (.getMessage exception)))
      (System/exit 2))))

(when-not (= "true" (System/getProperty
                      "gravity.workstream-governance.library"))
  (apply -main *command-line-args*))
