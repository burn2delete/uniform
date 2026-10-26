(ns requirement-evidence
  "Read-only evidence pointers. Historical support never changes task acceptance."
  (:require [clojure.edn :as edn] [clojure.string :as str]))

(defn require! [condition message]
  (when-not condition (throw (ex-info message {:diagnostic "REQUIREMENT-EVIDENCE"}))))

(defn task-record [text marker id]
  (let [start (str "<!-- " marker ":start -->") end (str "<!-- " marker ":end -->")
        starts (str/split text (re-pattern (java.util.regex.Pattern/quote start)) -1)
        ends (str/split text (re-pattern (java.util.regex.Pattern/quote end)) -1)]
    (require! (and (= 2 (count starts)) (= 2 (count ends))) "Missing or duplicate canonical task region")
    (let [parts (str/split (second starts) (re-pattern (java.util.regex.Pattern/quote end)) -1)
          _ (require! (= 2 (count parts)) "Reversed canonical task region")
          records (str/split (first parts) #"\n\n(?=- \[)")
          matched (filter #(re-find (re-pattern (str "(?m)^- \\[([ x])\\] `"
                                                           (java.util.regex.Pattern/quote id) "` ")) %) records)]
      (require! (= 1 (count matched)) "Missing or duplicate canonical task ID")
      (let [block (first matched)
            [_ checked] (re-find #"(?m)^- \[([ x])\]" block)
            status (second (re-find #"(?m)^Current evidence/status: (.+)$" block))
            acceptance (second (re-find #"(?m)^Done when \(yes/no\): (.+)$" block))]
        (require! (and status acceptance) "Canonical task lacks status or acceptance boundary")
        (require! (= (= checked "x") (str/starts-with? status "Accepted;"))
                  "Canonical checkbox and acceptance status disagree")
        {:id id :checked? (= checked "x") :status status :acceptance acceptance}))))

(defn project [requirement read-pinned sha256]
  (let [{:keys [path marker id] :as task} (:task-reference requirement)
        record (task-record (read-pinned task) marker id)
        historical
        (mapv (fn [{:keys [receipt selector expected ledger] :as support}]
                (let [artifact (edn/read-string (read-pinned receipt))
                      ledger-text (read-pinned ledger)
                      ledger-body (second (re-find #"(?s)## Evidence Ledger\n(.*?)(?:\n## |\z)" ledger-text))
                      row (some #(when (= (:line-sha256 ledger) (sha256 %)) %)
                                (str/split-lines (or ledger-body ""))) ]
                  (require! (and (vector? selector) (seq selector)
                                 (= expected (get-in artifact selector ::absent)))
                            "Historical receipt does not support the selected facet")
                  (require! (and row (str/starts-with? row (str "| " (:date ledger) " |"))
                                 (= (:verification-date support) (:date ledger))
                                 (re-matches #"[0-9a-f]{40}" (or (:source-baseline support) ""))
                                 (str/includes? row (str "`" (:task-id ledger) "`"))
                                 (str/includes? row (:path receipt)))
                            "Historical dated ledger association is absent")
                  (select-keys support [:facet :receipt :ledger :selector :expected :verification-date
                                        :source-baseline :qualification])))
              (:historical-support requirement))]
    {:requirement-id (:id requirement)
     :canonical-task (assoc record :path path :marker marker)
     :historical-support historical
     :current-acceptance :unassessed
     :qualification "Task status is read from its canonical region. Receipt facets preserve historical support only; applicability, consumed closure and independent implementation acceptance remain unassessed."}))
