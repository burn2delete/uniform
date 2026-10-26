(ns reader-task-projection
  "Reader's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/reader-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/reader.md")
(def semantic-ids
  #{"reader/finite-remaining-work/contract-coverage-reconciliation"
    "reader/finite-remaining-work/decode-and-source-identity-closure"
    "reader/finite-remaining-work/lexical-products-and-origins-closure"
    "reader/finite-remaining-work/extension-policy-closure"
    "reader/finite-remaining-work/authentication-and-quota-removal-closure"
    "reader/finite-remaining-work/fresh-syntax-handoff"
    "reader/finite-remaining-work/component-release-evidence"})

(defn fail [message] (throw (ex-info message {})))

(defn region [text marker]
  (let [start (str "<!-- " marker ":start -->")
        end (str "<!-- " marker ":end -->")
        starts (str/split text (re-pattern (java.util.regex.Pattern/quote start)) -1)
        ends (str/split text (re-pattern (java.util.regex.Pattern/quote end)) -1)]
    (when-not (and (= 2 (count starts)) (= 2 (count ends)))
      (fail (str "Missing or duplicate region: " marker)))
    (let [[prefix tail] starts
          [body suffix] (str/split tail (re-pattern (java.util.regex.Pattern/quote end)) -1)]
      (when (nil? suffix) (fail (str "Reversed region: " marker)))
      {:prefix (str prefix start) :body body :suffix (str end suffix)})))

(def record-pattern
  #"(?s)^- \[([ x])\] `(reader/finite-remaining-work/[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn validate-links [field]
  ;; Deliberately small Markdown subset: plain text and inline HTTPS links.
  ;; Copying source-relative links would silently change their target location.
  (let [remaining
        (str/replace field #"\[([^\[\]<>!\\\n]+)\]\(([^\s()<>\"`\\]+)\)"
          (fn [[_ _ target]]
            (let [uri (try (java.net.URI. target)
                           (catch java.net.URISyntaxException _ nil))]
              (when-not (and uri (= "https" (.getScheme uri))
                             (seq (.getHost uri)) (nil? (.getUserInfo uri)))
                (fail "Reader task links must be absolute HTTPS URLs")))
            ""))]
    (when (re-find #"[\[\]<>!\\]" remaining)
      (fail "Unsupported Reader task Markdown; use plain text or inline HTTPS links"))))

(defn records [text]
  (let [body (str/trim (:body (region text "reader-implementation-tasks")))
        blocks (str/split body #"\n\n(?=- \[)")
        rows (mapv (fn [block]
                     (let [[_ checked id title status acceptance] (re-matches record-pattern block)]
                       (when-not id (fail "Malformed Reader task record"))
                       (doseq [field [title status acceptance]] (validate-links field))
                       (when-not (= (= checked "x") (str/starts-with? status "Accepted;"))
                         (fail "Checkbox and explicit acceptance status disagree"))
                       {:id id :title title :status status :acceptance acceptance})) blocks)
        ids (mapv :id rows)]
    (when-not (= (count ids) (count (set ids))) (fail "Duplicate Reader task ID"))
    (when-not (= semantic-ids (set ids)) (fail "Missing or unknown Reader task ID"))
    (when-not (= (count rows) (count (set (map :title rows))))
      (fail "Duplicate Reader task title"))
    rows))

(defn table [rows]
  (str "\n| Task | Current evidence/status | Done when (yes/no) |\n| --- | --- | --- |\n"
       (apply str (for [{:keys [title status acceptance]} rows]
                    (str "| " title " | " status " | " acceptance " |\n")))))

(defn project [task-text roadmap-text]
  (let [rows (records task-text)
        {:keys [prefix suffix]} (region roadmap-text "reader-task-projection")]
    (str prefix (table rows) suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Reader projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/reader_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Reader task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
