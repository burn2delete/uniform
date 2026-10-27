(ns task-projection
  "Pure implementation-task parsing and roadmap projection; no CLI or file IO."
  (:require [clojure.string :as str]))

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

(defn validate-links [{:keys [component allow-bang?]} field]
  ;; Core permits punctuation in set!, but no component permits images.
  (when (and allow-bang? (str/includes? field "!["))
    (fail (str component " task images are unsupported; use plain text or inline HTTPS links")))
  (let [remaining
        (str/replace field #"\[([^\[\]<>!\\\n]+)\]\(([^\s()<>\"`\\]+)\)"
          (fn [[_ _ target]]
            (let [uri (try (java.net.URI. target)
                           (catch java.net.URISyntaxException _ nil))]
              (when-not (and uri (= "https" (.getScheme uri))
                             (seq (.getHost uri)) (nil? (.getUserInfo uri)))
                (fail (str component " task links must be absolute HTTPS URLs"))))
            ""))]
    (when (re-find (if allow-bang? #"[\[\]<>\\]" #"[\[\]<>!\\]") remaining)
      (fail (str "Unsupported " component " task Markdown; use plain text or inline HTTPS links")))))

(defn records [{:keys [component task-marker record-pattern semantic-ids
                      title-id-prefix title-id-error record-keys validate-field] :as options} text]
  (let [body (str/trim (:body (region text task-marker)))
        blocks (str/split body #"\n\n(?=- \[)")
        rows (mapv (fn [block]
                     (let [[_ checked & values] (re-matches record-pattern block)
                           row (zipmap (or record-keys [:id :title :status :acceptance]) values)
                           {:keys [id title status]} row]
                       (when-not id (fail (str "Malformed " component " task record")))
                       (when (and title-id-prefix
                                  (not= id (str title-id-prefix
                                                (-> title str/lower-case
                                                    (str/replace #"[^a-z0-9 -]" "")
                                                    (str/replace #" +" "-")))))
                         (fail title-id-error))
                       (doseq [field (vals (dissoc row :id))]
                         (if validate-field (validate-field field) (validate-links options field)))
                       (when-not (= (= checked "x") (str/starts-with? status "Accepted;"))
                         (fail "Checkbox and explicit acceptance status disagree"))
                       row)) blocks)
        ids (mapv :id rows)]
    (when-not (= (count ids) (count (set ids))) (fail (str "Duplicate " component " task ID")))
    (when-not (= semantic-ids (set ids)) (fail (str "Missing or unknown " component " task ID")))
    (when-not (= (count rows) (count (set (map :title rows))))
      (fail (str "Duplicate " component " task title")))
    rows))

(defn table [header rows]
  (str header
       (apply str (for [{:keys [title status acceptance]} rows]
                    (str "| " title " | " status " | " acceptance " |\n")))))

(defn project [{:keys [projection-marker table-header] :as options} task-text roadmap-text]
  (let [rows (records options task-text)
        {:keys [prefix suffix]} (region roadmap-text projection-marker)]
    (str prefix (table table-header rows) suffix)))

(defn check-projection [{:keys [component] :as options} task-text roadmap-text]
  (when-not (= roadmap-text (project options task-text roadmap-text))
    (fail (str "Stale " component " projection; edit tasks.md, then run --write")))
  true)
