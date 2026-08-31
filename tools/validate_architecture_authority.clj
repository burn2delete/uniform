(ns gravity.architecture-authority
  "Fail-closed pre-freeze checks for architecture-report authority claims.

  Architecture reports are intentionally Markdown for human review, but the
  authority and dependency tuple is a closed JSON block.  The linter never
  treats prose, including terminal history, as a source of authority.  A
  legacy report can be checked with :require-block? false; that mode is
  explicitly non-authoritative and is intended only for retained history."
  (:require [clojure.string :as str])
  (:import (java.io PushbackReader StringReader)
           (java.nio ByteBuffer)
           (java.nio.charset CharacterCodingException CodingErrorAction
                              StandardCharsets)
           (java.nio.file Files Paths)))

(def ^:private authority-schema "gravity/architecture-authority-v1")
(def ^:private authority-keys
  #{"schema" "workstream_id" "invariant_family" "report_path" "status"
    "base_commit" "dependencies" "historical_references" "authority"})
(def ^:private dependency-keys #{"id" "required_state"})
(def ^:private history-keys #{"id" "terminal_state" "role"})
(def ^:private authority-fact-keys
  #{"integration_only" "release" "self_hosting" "seed_retirement"})
(def ^:private lifecycle-states
  #{"draft" "frozen" "review-pending" "accepted" "integration-eligible"
    "integrated" "held" "rejected" "superseded" "abandoned"})
(def ^:private terminal-states #{"rejected" "superseded" "abandoned"})
(def ^:private exact-authority
  {"integration_only" true
   "release" false
   "self_hosting" false
   "seed_retirement" false})
(def ^:private oid-pattern #"[0-9a-f]{40}")
(def ^:private identifier-pattern #"[a-z0-9][a-z0-9./-]*")
(def ^:private block-opening
  #"^```json gravity-architecture-authority-v1[ \t]*$")
(def ^:private block-closing #"^```[ \t]*$")

(defn- issue [code location message]
  (str code " " location ": " message))

(defn- json-error [message data]
  (throw (ex-info message (assoc data :code "AA001"))))

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
          (= -1 value) (json-error "unterminated JSON string" {})
          (= \" (char value)) (str output)
          (= \\ (char value))
          (let [escaped (.read reader)]
            (when (= -1 escaped) (json-error "unterminated JSON escape" {}))
            (case (char escaped)
              \" (.append output \" )
              \\ (.append output \\)
              \/ (.append output \/)
              \b (.append output \backspace)
              \f (.append output \formfeed)
              \n (.append output \newline)
              \r (.append output \return)
              \t (.append output \tab)
              \u (let [values (repeatedly 4 #(.read reader))
                       digits (apply str (map char values))]
                   (when (or (some #(= -1 %) values)
                             (not (re-matches #"[0-9A-Fa-f]{4}" digits)))
                     (json-error "malformed JSON unicode escape" {:digits digits}))
                   (.append output (char (Integer/parseInt digits 16))))
              (json-error "unsupported JSON escape"
                          {:escape (str (char escaped))}))
            (recur))
          (< value 0x20) (json-error "JSON string contains a control character" {})
          :else (do (.append output (char value)) (recur)))))))

(declare read-json-value!)

(defn- read-json-literal! [^PushbackReader reader first-character suffix value]
  (doseq [expected suffix]
    (let [actual (.read reader)]
      (when (or (= -1 actual) (not= expected (char actual)))
        (json-error "malformed JSON literal" {}))))
  value)

(defn- read-json-number! [^PushbackReader reader first-character]
  (let [token (loop [output (StringBuilder. (str first-character))]
                (let [value (.read reader)]
                  (if (and (not= -1 value)
                           (re-matches #"[0-9eE+\-.]" (str (char value))))
                    (recur (.append output (char value)))
                    (do (unread-json! reader value) (str output)))))]
    (when-not (re-matches #"-?(?:0|[1-9][0-9]*)(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?"
                          token)
      (json-error "malformed JSON number" {:token token}))
    (try
      (if (re-find #"[.eE]" token) (bigdec token) (bigint token))
      (catch NumberFormatException _
        (json-error "JSON number is out of range" {:token token})))))

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
              :else (json-error "JSON array requires comma or closing bracket" {}))))))))

(defn- read-json-object! [^PushbackReader reader]
  (let [first-key (skip-json-whitespace! reader)]
    (if (= (int \}) first-key)
      {}
      (do
        (unread-json! reader first-key)
        (loop [result {}]
          (let [quote-value (skip-json-whitespace! reader)]
            (when-not (= (int \" ) quote-value)
              (json-error "JSON object key must be a string" {}))
            (let [key (read-json-string! reader)
                  colon (skip-json-whitespace! reader)]
              (when (contains? result key)
                (json-error "JSON object repeats a key" {:key key}))
              (when-not (= (int \:) colon)
                (json-error "JSON object key requires a colon" {}))
              (let [value (read-json-value! reader)
                    delimiter (skip-json-whitespace! reader)
                    updated (assoc result key value)]
                (cond
                  (= (int \,) delimiter) (recur updated)
                  (= (int \}) delimiter) updated
                  :else (json-error "JSON object requires comma or closing brace" {}))))))))))

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
          (json-error "unsupported JSON value" {:value (str character)}))))))

(defn- strict-json [text]
  (with-open [reader (PushbackReader. (StringReader. (str text)))]
    (let [value (read-json-value! reader)
          trailing (skip-json-whitespace! reader)]
      (when-not (= -1 trailing)
        (json-error "trailing data follows JSON document" {}))
      value)))

(declare strict-utf8)

(defn- load-json-file [path]
  (let [resolved (.normalize (.toAbsolutePath (Paths/get (str path)
                                                         (make-array String 0))))]
    (strict-json (strict-utf8 (Files/readAllBytes resolved) (str resolved)))))

(defn- strict-utf8 [bytes path]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (str (.decode decoder (ByteBuffer/wrap bytes)))
      (catch CharacterCodingException exception
        (throw (ex-info "architecture report is not UTF-8"
                        {:code "AA001" :path path
                         :cause (.getMessage exception)}))))))

(defn read-report
  "Reads a bounded UTF-8 report as text.  The report parser itself is pure."
  [path]
  (let [resolved (.normalize (.toAbsolutePath (Paths/get (str path)
                                                         (make-array String 0))))]
    (strict-utf8 (Files/readAllBytes resolved) (str resolved))))

(defn- safe-relative-path? [value]
  (and (string? value)
       (not (str/blank? value))
       (not (str/starts-with? value "/"))
       (not (str/starts-with? value "~"))
       (not (str/includes? value "\\"))
       (not (some #{".."} (str/split value #"/")))
       (= value (str/replace value #"/+" "/"))))

(defn- normalized-report-path [path]
  (-> (str path)
      (str/replace "\\" "/")
      (str/replace-first #"^\./" "")))

(defn- closed-errors [value expected location]
  (if-not (map? value)
    [(issue "AA002" location "must be an object")]
    (let [observed (set (keys value))
          missing (sort (remove observed expected))
          unknown (sort (remove expected observed))]
      (cond-> []
        (seq missing) (conj (issue "AA002" location
                                   (str "missing keys " missing)))
        (seq unknown) (conj (issue "AA002" location
                                   (str "unknown keys " unknown)))))))

(defn- strict-list-errors [value location]
  (when-not (vector? value)
    [(issue "AA002" location "must be a list")]))

(defn- extract-authority-block
  "Returns {:block map :errors [...]} for exactly one authority block."
  [content]
  (let [lines (str/split (str/replace (str content) "\r\n" "\n") #"\n" -1)
        openings (keep-indexed (fn [index line]
                                 (when (re-matches block-opening line) index))
                               lines)]
    (cond
      (empty? openings)
      {:block nil
       :errors [(issue "AA001" "report.authority"
                       "requires one fenced gravity-architecture-authority-v1 JSON block")]}

      (> (count openings) 1)
      {:block nil
       :errors [(issue "AA001" "report.authority"
                       "must contain exactly one authority block")]}

      :else
      (let [opening (first openings)
            after (subvec (vec lines) (inc opening))
            closing-offset (first (keep-indexed
                                   (fn [index line]
                                     (when (re-matches block-closing line) index))
                                   after))]
        (if (nil? closing-offset)
          {:block nil
           :errors [(issue "AA001" "report.authority"
                           "authority block is not closed")]}
          (let [body (str/join "\n" (subvec after 0 closing-offset))]
            (try
              {:block (strict-json body) :errors []}
              (catch Throwable _
                {:block nil
                 :errors [(issue "AA001" "report.authority"
                                 "authority block is not strict JSON")]}))))))))

(defn- ledger-items [ledger]
  (if (and (map? ledger) (vector? (get ledger "workstreams")))
    (get ledger "workstreams")
    ;; The sharded v2 manifest carries the closed identity/state/dependency
    ;; projection in `records`.  It is sufficient for authority checks and
    ;; avoids re-reading every terminal record during a pre-freeze invocation.
    (if (and (map? ledger) (vector? (get ledger "records")))
      (get ledger "records")
      [])))

(defn- ledger-by-id [ledger]
  (into {} (keep (fn [item]
                   (when (and (map? item) (string? (get item "id")))
                     [(get item "id") item]))
                 (ledger-items ledger))))

(defn- dependency-shape-errors [dependencies location]
  (if-let [errors (strict-list-errors dependencies location)]
    errors
    (vec
     (mapcat
      (fn [[index dependency]]
        (let [where (str location "[" index "]")]
          (concat
           (closed-errors dependency dependency-keys where)
           (when (map? dependency)
             (concat
              (when-not (and (string? (get dependency "id"))
                             (re-matches identifier-pattern
                                         (get dependency "id")))
                [(issue "AA002" (str where ".id")
                        "must be a lowercase stable identifier")])
              (when-not (= "integrated" (get dependency "required_state"))
                [(issue "AA002" (str where ".required_state")
                        "must be exactly integrated for architecture authority")]))))))
      (map-indexed vector dependencies)))))

(defn- history-shape-errors [history location]
  (if-let [errors (strict-list-errors history location)]
    errors
    (vec
     (mapcat
      (fn [[index reference]]
        (let [where (str location "[" index "]")]
          (concat
           (closed-errors reference history-keys where)
           (when (map? reference)
             (concat
              (when-not (and (string? (get reference "id"))
                             (re-matches identifier-pattern (get reference "id")))
                [(issue "AA002" (str where ".id")
                        "must be a lowercase stable identifier")])
              (when-not (contains? terminal-states (get reference "terminal_state"))
                [(issue "AA002" (str where ".terminal_state")
                        "must be a terminal rejection state")])
              (when-not (= "history" (get reference "role"))
                [(issue "AA002" (str where ".role")
                        "must be exactly history")]))))))
      (map-indexed vector history)))))

(defn- authority-shape-errors [block report-path]
  (let [location "report.authority"
        shape (closed-errors block authority-keys location)
        authority (when (map? block) (get block "authority"))]
    (if (seq shape)
      shape
      (vec
       (concat
        (when-not (= authority-schema (get block "schema"))
          [(issue "AA002" (str location ".schema")
                  "unsupported authority schema")])
        (when-not (and (string? (get block "workstream_id"))
                       (re-matches identifier-pattern (get block "workstream_id")))
          [(issue "AA002" (str location ".workstream_id")
                  "must be a lowercase stable identifier")])
        (when-not (and (string? (get block "invariant_family"))
                       (re-matches identifier-pattern (get block "invariant_family")))
          [(issue "AA002" (str location ".invariant_family")
                  "must be a lowercase stable identifier")])
        (when-not (= (normalized-report-path report-path)
                     (get block "report_path"))
          [(issue "AA003" (str location ".report_path")
                  "must identify the exact repository-relative report path")])
        (when-not (safe-relative-path? (get block "report_path"))
          [(issue "AA003" (str location ".report_path")
                  "must be a safe repository-relative path")])
        (when-not (contains? lifecycle-states (get block "status"))
          [(issue "AA002" (str location ".status")
                  "unknown lifecycle state")])
        (when-not (and (string? (get block "base_commit"))
                       (re-matches oid-pattern (get block "base_commit")))
          [(issue "AA003" (str location ".base_commit")
                  "must be an exact lowercase 40-hex commit")])
        (dependency-shape-errors (get block "dependencies")
                                 (str location ".dependencies"))
        (history-shape-errors (get block "historical_references")
                              (str location ".historical_references"))
        (closed-errors authority authority-fact-keys (str location ".authority"))
        (when (map? authority)
          (concat
           (when-not (every? #(instance? Boolean %) (vals authority))
             [(issue "AA002" (str location ".authority")
                     "all authority facts must be booleans")])
           (when-not (= exact-authority authority)
             [(issue "AA008" (str location ".authority")
                     "architecture reports may grant integration-only authority")]))))))))

(defn- relation-errors [block ledger]
  (let [by-id (ledger-by-id ledger)
        own-id (get block "workstream_id")
        dependencies (if (vector? (get block "dependencies"))
                       (get block "dependencies") [])
        history (if (vector? (get block "historical_references"))
                  (get block "historical_references") [])
        dependency-ids (mapv #(get % "id") dependencies)
        history-ids (mapv #(get % "id") history)
        duplicate-dependencies (for [[id count] (frequencies dependency-ids)
                                     :when (and id (> count 1))] id)
        duplicate-history (for [[id count] (frequencies history-ids)
                                :when (and id (> count 1))] id)
        cycle? (fn [start]
                 (letfn [(walk [node visiting]
                           (cond
                             (contains? visiting node) true
                             (not (contains? by-id node)) false
                             :else (some #(walk % (conj visiting node))
                                         (get (get by-id node) "dependencies" []))))]
                   (boolean (walk start #{}))))]
    (vec
     (concat
      (when (some nil? dependency-ids) [])
      (map #(issue "AA004" "report.authority.dependencies"
                   (str "duplicate dependency " %)) duplicate-dependencies)
      (map #(issue "AA004" "report.authority.historical_references"
                   (str "duplicate historical reference " %)) duplicate-history)
      (for [dependency dependencies
            :let [id (get dependency "id")
                  required (get dependency "required_state")
                  target (get by-id id)]
            :when (and target (not= required (get target "state")))]
        (issue "AA005" "report.authority.dependencies"
               (str id " is " (get target "state") "; required state is " required)))
      (for [dependency dependencies
            :let [id (get dependency "id")]
            :when (and id (not (contains? by-id id)))]
        (issue "AA004" "report.authority.dependencies"
               (str "missing dependency " id)))
      (for [dependency dependencies
            :let [id (get dependency "id")]
            :when (and id (= id own-id))]
        (issue "AA007" "report.authority.dependencies"
               "dependency graph contains a cycle through the report workstream"))
      (for [id (sort (set dependency-ids))
            :when (cycle? id)]
        (issue "AA007" "report.authority.dependencies"
               (str "dependency graph contains a cycle through " id)))
      (for [reference history
            :let [id (get reference "id")
                  expected (get reference "terminal_state")
                  target (get by-id id)]
            :when (and target (not= expected (get target "state")))]
        (issue "AA005" "report.authority.historical_references"
               (str id " is " (get target "state")
                    "; historical reference requires " expected)))
      (for [reference history
            :let [id (get reference "id")]
            :when (and id (not (contains? by-id id)))]
        (issue "AA004" "report.authority.historical_references"
               (str "missing historical workstream " id)))
      (for [id (sort (filter #(contains? (set dependency-ids) %)
                            (set history-ids)))]
        (issue "AA006" "report.authority"
               (str id " cannot be both an authority dependency and terminal history")))))))

(defn- own-record-errors [block ledger]
  (when-let [record (get (ledger-by-id ledger) (get block "workstream_id"))]
    (vec
     (concat
      (when-not (= (get block "invariant_family") (get record "invariant_family"))
        [(issue "AA003" "report.authority.invariant_family"
                "does not match the ledger workstream")])
      (when (and (string? (get record "base_commit"))
                 (not= (get block "base_commit") (get record "base_commit")))
        [(issue "AA003" "report.authority.base_commit"
                "does not match the ledger workstream")])
      (when (and (string? (get record "architecture_decision"))
                 (not= (normalized-report-path (get record "architecture_decision"))
                       (get block "report_path")))
        [(issue "AA003" "report.authority.report_path"
                "does not match the ledger architecture report")])
      (when-not (= (get block "status") (get record "state"))
        [(issue "AA003" "report.authority.status"
                "does not match the ledger lifecycle state")])))))

(defn- rejected-authority-phrase? [content phrase]
  (let [matcher (re-matcher (re-pattern (str "(?is)\\bintegrated\\s+" phrase))
                            (str content))]
    (loop [found nil]
      (if (.find matcher)
        (let [start (max 0 (- (.start matcher) 96))
              context (subs (str content) start (.end matcher))]
          (if (re-find #"(?i)(not|never|terminal(?:ly)?|rejected)" context)
            (recur found)
            true))
        found))))

(defn- prose-authority-errors [content ledger]
  (let [rejected (filter #(and (map? %) (= "rejected" (get % "state")))
                         (ledger-items ledger))
        full-id-errors
        (for [record rejected
              :let [id (get record "id")
                    escaped (java.util.regex.Pattern/quote id)]
              :when (rejected-authority-phrase? content
                                                 (str "`?" escaped "`?(?![a-z0-9./-])"))]
          (issue "AA006" "report.prose"
                 (str "terminally rejected workstream " id
                      " is described as integrated")))
        attempt-errors
        (when (rejected-authority-phrase? content "attempt[- ]?15\\b")
          [(issue "AA006" "report.prose"
                  "terminally rejected Attempt 15 cannot be described as integrated")])]
    (vec (concat full-id-errors attempt-errors))))

(defn- load-ledger-value [ledger]
  (if (string? ledger)
    (load-json-file ledger)
    ledger))

(defn validate-report-content
  "Validates one architecture report against an already decoded ledger.

  Options:
  :require-block? (default true) requires the closed machine-readable block.
  Set it false only for legacy history; no authority is inferred in that mode."
  ([report-path content ledger]
   (validate-report-content report-path content ledger {:require-block? true}))
  ([report-path content ledger {:keys [require-block?] :or {require-block? true}}]
   (let [ledger (load-ledger-value ledger)
         extracted (extract-authority-block content)
         block-errors (:errors extracted)
         missing-only? (and (= 1 (count block-errors))
                            (str/includes? (first block-errors)
                                           "requires one fenced"))
         report-errors (if (and missing-only? (not require-block?))
                         (prose-authority-errors content ledger)
                         block-errors)
         block (:block extracted)]
     (if (and missing-only? (not require-block?))
       (vec report-errors)
       (if (seq report-errors)
         (vec report-errors)
         (let [shape-errors (authority-shape-errors block report-path)]
           (vec (concat
                 shape-errors
                 (when (empty? shape-errors)
                   (relation-errors block ledger))
                 (when (empty? shape-errors)
                   (own-record-errors block ledger))
                 (prose-authority-errors content ledger)))))))))

(defn validate-report
  "Validates a report path against a ledger path or decoded ledger map."
  ([report-path ledger] (validate-report report-path ledger {}))
  ([report-path ledger options]
   (validate-report-content report-path (read-report report-path)
                            (load-ledger-value ledger) options)))

(defn- load-governance-namespace []
  (or (find-ns 'gravity.workstream-governance)
      (do
        ;; Governance remains the outer pre-freeze gate.  Loading it as a
        ;; library avoids running its command-line entrypoint.
        (System/setProperty "gravity.workstream-governance.library" "true")
        (load-file "tools/validate_workstream_governance.clj")
        (find-ns 'gravity.workstream-governance))))

(defn validate-pre-freeze
  "Runs the governance contract/ledger checks and the strict report check."
  ([report-path] (validate-pre-freeze report-path
                                      "contracts/workstream-governance.json"
                                      "contracts/workstream-ledger.json"))
  ([report-path contract-path ledger-path]
   (let [namespace (load-governance-namespace)
         validate-documents (ns-resolve namespace 'validate-documents)
         governance-errors (validate-documents contract-path ledger-path)
         report-errors (validate-report report-path ledger-path)]
     (vec (concat governance-errors report-errors)))))

(defn- parse-options [arguments]
  (loop [remaining arguments
         options {:contract "contracts/workstream-governance.json"
                  :ledger "contracts/workstream-ledger.json"
                  :require-block? true}]
    (if-let [argument (first remaining)]
      (case argument
        "--report" (if-let [value (second remaining)]
                     (recur (nnext remaining) (assoc options :report value))
                     (throw (ex-info "--report requires a path" {})))
        "--contract" (if-let [value (second remaining)]
                       (recur (nnext remaining) (assoc options :contract value))
                       (throw (ex-info "--contract requires a path" {})))
        "--ledger" (if-let [value (second remaining)]
                     (recur (nnext remaining) (assoc options :ledger value))
                     (throw (ex-info "--ledger requires a path" {})))
        "--legacy" (recur (next remaining) (assoc options :require-block? false))
        (throw (ex-info (str "unknown argument: " argument) {})))
      options)))

(defn -main [& arguments]
  (try
    (let [{:keys [report contract ledger require-block?] :as options}
          (parse-options arguments)]
      (when-not report
        (throw (ex-info "usage: clojure -M tools/validate_architecture_authority.clj --report REPORT [--legacy]"
                        {:code "AA001"})))
      (let [errors (if require-block?
                     (validate-pre-freeze report contract ledger)
                     (let [namespace (load-governance-namespace)
                           validate-documents (ns-resolve namespace 'validate-documents)]
                       (vec (concat (validate-documents contract ledger)
                                    (validate-report report ledger options)))))]
        (if (seq errors)
          (do (binding [*out* *err*] (doseq [error errors] (println error)))
              (System/exit 1))
          (do (println (str "architecture authority validation passed: " report
                            (when-not require-block? " (legacy, non-authoritative)")))
              (System/exit 0)))))
    (catch Throwable exception
      (binding [*out* *err*]
        (println (str "AA001 cli: " (.getMessage exception))))
      (System/exit 2))))

(when-not (= "true" (System/getProperty
                      "gravity.architecture-authority.library"))
  (apply -main *command-line-args*))
