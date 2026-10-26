(ns effects-task-projection
  "Effects's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/effects-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/effects.md")
(def semantic-ids
  #{"effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
    "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
    "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
    "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
    "effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"})

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
  #"(?s)^- \[([ x])\] `(effects/current-evidence-and-remaining-work/effects[1-5]-[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

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
                (fail "Effects task links must be absolute HTTPS URLs")))
            ""))]
    (when (re-find #"[\[\]<>!\\]" remaining)
      (fail "Unsupported Effects task Markdown; use plain text or inline HTTPS links"))))

(defn records [text]
  (let [body (str/trim (:body (region text "effects-implementation-tasks")))
        blocks (str/split body #"\n\n(?=- \[)")
        rows (mapv (fn [block]
                     (let [[_ checked id title status acceptance] (re-matches record-pattern block)]
                       (when-not id (fail "Malformed Effects task record"))
                       (when-not (= id (str "effects/current-evidence-and-remaining-work/"
                                         (-> title str/lower-case
                                             (str/replace #"[^a-z0-9 -]" "")
                                             (str/replace #" +" "-"))))
                         (fail "Effects task ID and preserved title disagree"))
                       (doseq [field [title status acceptance]] (validate-links field))
                       (when-not (= (= checked "x") (str/starts-with? status "Accepted;"))
                         (fail "Checkbox and explicit acceptance status disagree"))
                       {:id id :title title :status status :acceptance acceptance})) blocks)
        ids (mapv :id rows)]
    (when-not (= (count ids) (count (set ids))) (fail "Duplicate Effects task ID"))
    (when-not (= semantic-ids (set ids)) (fail "Missing or unknown Effects task ID"))
    (when-not (= (count rows) (count (set (map :title rows))))
      (fail "Duplicate Effects task title"))
    rows))

(defn table [rows]
  (str "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
       (apply str (for [{:keys [title status acceptance]} rows]
                    (str "| " title " | " status " | " acceptance " |\n")))))

(defn project [task-text roadmap-text]
  (let [rows (records task-text)
        {:keys [prefix suffix]} (region roadmap-text "effects-task-projection")]
    (str prefix (table rows) suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Effects projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/effects_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Effects task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
