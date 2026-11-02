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
          records (str/split (first parts) #"(?m)(?=^- \[[ x]\] `)")
          matched (filter #(re-find (re-pattern (str "(?m)^- \\[([ x])\\] `"
                                                           (java.util.regex.Pattern/quote id) "` ")) %) records)]
      (require! (= 1 (count matched)) "Missing or duplicate canonical task ID")
      (let [block (first matched)
            [_ checked] (re-find #"(?m)^- \[([ x])\]" block)
            statuses (re-seq #"(?m)^(Current evidence/status|Status): (.+)$" block)
            ordinary (re-seq #"(?m)^(Done when \(yes/no\)|Closing evidence|Concrete exit evidence|Closing evidence and governing contract|Declared completion and remaining work): (.+)$" block)
            projected-headings (re-seq #"(?m)^Projected task block:\n" block)
            projected (re-find #"(?ms)^(Projected task block):\n((?:Remaining tasks|Owner role|Outcome): .+?)\s*\z" block)
            boundaries (concat ordinary (when projected [projected]))
            status (nth (first statuses) 2 nil) acceptance (nth (first boundaries) 2 nil)]
        (require! (and (= 1 (count statuses)) (= 1 (count boundaries))
                       (= (count projected-headings) (if projected 1 0))
                       (or (empty? projected-headings) (empty? ordinary)))
                  (str "Canonical task lacks unique status or acceptance boundary: " id))
        (require! (= (= checked "x") (str/starts-with? status "Accepted;"))
                  "Canonical checkbox and acceptance status disagree")
        {:id id :checked? (= checked "x") :status status :acceptance acceptance}))))

(defn project [requirement read-pinned sha256 source-baseline]
  (let [{:keys [path marker id] :as task} (:task-reference requirement)
        _ (require! (or task (seq (:task-gap requirement))) "Requirement has neither canonical task nor explicit trace gap")
        record (if task (assoc (task-record (read-pinned task) marker id) :path path :marker marker)
                   {:state :missing :gap (:task-gap requirement)})
        interfaces (mapv (fn [{:keys [path marker id] :as ref}]
                           (assoc (task-record (read-pinned ref) marker id) :path path :marker marker))
                         (:additional-task-interfaces requirement))
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
                                 (= source-baseline (:source-baseline support))
                                 (re-matches #"[0-9a-f]{40}" (or source-baseline ""))
                                 (str/includes? row (str "`" (:task-id ledger) "`"))
                                 (str/includes? row (:path receipt)))
                            "Historical dated ledger association is absent")
                  (select-keys support [:facet :receipt :ledger :selector :expected :verification-date
                                        :source-baseline :qualification])))
              (:historical-support requirement))]
    {:requirement-id (:id requirement)
     :canonical-task record
     :additional-task-interfaces interfaces
     :historical-support historical
     :current-acceptance :unassessed
     :qualification "Task status is read from its canonical region. Receipt facets preserve historical support only; applicability, consumed closure and independent implementation acceptance remain unassessed."}))
