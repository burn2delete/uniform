(ns domain-gap-patch
  "Read-only guarded composition of the assigned Domain historical-gap proposals."
  (:require [clojure.edn :as edn] [clojure.string :as str] [clojure.set :as set]
            [domain-requirement-patch :as source]))

(binding [*command-line-args* nil]
  (require '[domains-task-projection :as domains]))

(def patch-path "specs/requirement-reconciliation/domain-gap-completion.edn")
(defn require! [p message] (source/require! p message))
(defn sha [s] (source/sha s))
(defn replace-once [text before after]
  (let [i (.indexOf ^String text ^String before)]
    (require! (and (<= 0 i) (= i (.lastIndexOf ^String text ^String before))) "Stale, duplicate or ambiguous task predecessor")
    (str (subs text 0 i) after (subs text (+ i (count before))))))
(defn task-files [p]
  (into {} (for [[path pin] (:task-file-predecessors p)]
             (let [text (source/git-text (:source-commit pin) path)]
               (require! (= (:sha256 pin) (sha text)) "Task source-version pin differs")
               [path text]))))
(defn compose-tasks [p files]
  (doseq [[path pin] (:task-file-predecessors p)]
    (require! (= (:sha256 pin) (sha (get files path))) "Stale whole task-file predecessor"))
  (let [amendments (:task-amendments p)
        _ (require! (= (count amendments) (count (set (map (juxt :path :id) amendments)))) "Duplicate task amendment")
        changed (reduce (fn [m {:keys [path id before after before-sha256 after-sha256]}]
                          (require! (= before-sha256 (sha before)) "Forged task before hash")
                          (require! (= after-sha256 (sha after)) "Forged task after hash")
                          (require! (and (str/includes? before (str "`" id "`"))
                                         (= (re-seq #"(?m)^Status: .+$" before) (re-seq #"(?m)^Status: .+$" after)))
                                    "Task identity/status mutation")
                          (update m path replace-once before after)) files amendments)
        new-tasks (:new-task-proposals p)
        ids (mapv :id new-tasks)
        _ (require! (= (count ids) (count (set ids))) "Duplicate new task ID")
        path "specs/domains-workflow/tasks.md"
        text (get changed path)
        _ (doseq [id ids] (require! (not (str/includes? text (str "- [ ] `" id "`"))) "New task already exists"))
        result (update changed path replace-once "<!-- domains-implementation-tasks:end -->"
                       (str "\n" (str/join "\n" (map :raw new-tasks)) "\n<!-- domains-implementation-tasks:end -->"))
        headings (into domains/outcome-headings (map (juxt :id :title) new-tasks))]
    ;; Verify the actual proposed records under the necessary proposed schema
    ;; extension. No production parser or canonical task source is modified.
    (with-redefs [domains/outcome-order (into domains/outcome-order ids)
                  domains/outcome-headings headings]
      (domains/records (get result path)))
    result))
(defn apply-coverage [review operations]
  (require! (= (count operations) (count (set (map :id operations)))) "Duplicate coverage operation")
  (reduce (fn [m {:keys [collection id field-path before-present? before after]}]
            (require! (and (= :historical-gap-traceability collection) (= [] field-path) (true? before-present?)) "Wrong coverage operation")
            (let [i (.indexOf (mapv :id (get m collection)) id)]
              (require! (<= 0 i) "Unknown historical gap")
              (require! (= before (get-in m [collection i])) "Stale or repeated coverage operation")
              (assoc-in m [collection i] (assoc after :id id)))) review operations))
(defn validate! [p partition review published files]
  (let [assigned (:ids (first (filter #(= "phase-09" (:family %)) (:partitions partition))))
        rs (:records p) by-id (source/index! rs :historical-id)
        history (source/index! (:historical-gap-traceability review) :id)
        original (source/index! (:requirements published) :id)
        facets (source/index! (:source-facets published) :id)
        documents (source/index! (concat (:original-documents published) (:extension-sources published)) :id)
        taskids (set (concat (map :id (:task-amendments p)) (map :id (:new-task-proposals p))))
        operations (:coverage-operations p)]
    (require! (= assigned (:assigned-ids p)) "Wrong canonical assignment")
    (require! (= (set assigned) (set (keys by-id)) (set (map :id operations))) "Missing, duplicated or cross-family residual")
    (require! (= 70 (count assigned) (count rs) (count operations)) "Assigned population differs")
    (require! (= :proposal-only (:authority p)) "Unsupported authority")
    (doseq [r rs :let [id (:historical-id r) h (history id) o (original id) after (:after r)]]
      (require! (= h (:before r)) "Historical before record changed")
      (require! (= [h] (:historical-coverage-history after)) "Historical coverage lineage lost")
      (require! (= (:before-sha256 r) (sha (pr-str h))) "Wrong historical hash")
      (require! (#{:explicit-open-source-interface-work :no-same-id-open-row-and-no-full-coverage-witness} (:coverage-disposition h)) "Reused witness or retired history")
      (require! (= (:historical-record-sha256 r) (sha (pr-str o))) "Historical semantic record changed")
      (require! (= (:historical-obligation r) (:obligation o)) "Lost historical predicate")
      (require! (= (:source-facet-ids h) (mapv :id (:source-evidence r)) (:source-facet-ids after)) "Lost or invented facet")
      (doseq [f (:source-evidence r) :let [old (facets (:id f)) doc (documents (:document-id old))]]
        (require! (= [(:path f) (:original-source-sha256 f)] [(:path doc) (:sha256 doc)]) "Wrong source document/hash")
        (require! (= (select-keys old [:id :document-id :source-occurrence-id :start-byte :end-byte :start-line :end-line :raw-sha256 :kind :unresolved :unit-ids])
                     (select-keys f [:id :document-id :source-occurrence-id :start-byte :end-byte :start-line :end-line :raw-sha256 :kind :unresolved :unit-ids])) "Wrong source hash/ID/span or lost qualification"))
      (require! (and (= :open (:status r)) (= :not-yet-produced (:evidence r))
                    (= :finite-actionable-task-interface-proposed (:coverage-disposition after))
                    (= :unassessed (:implementation-acceptance after))
                    (= :pending-independent-source-review (:whole-facet-entailment-acceptance after))) "Forged coverage/implementation acceptance")
      (require! (= (:criterion-sha256 r) (sha (:criterion-text r)) (:criterion-sha256 after)) "Wrong criterion hash")
      (require! (= (:criterion-text r) (:criterion-text after)) "Criterion/coverage mismatch")
      (doseq [field [:positive-fixture :negative-fixture :required-output]]
        (require! (and (seq (get r field)) (str/includes? (:criterion-text r) (get r field))) "Lost acceptance, negative or output condition"))
      (require! (and (seq (:dependencies r)) (every? #(str/includes? (:criterion-text r) %) (:dependencies r))) "Missing dependencies")
      (require! (taskids (get-in r [:task-reference :id])) "Missing canonical task/interface proposal"))
    (doseq [r rs :let [task (get-in r [:task-reference :id])
                       target (first (filter #(= task (:id %)) (concat (:task-amendments p) (:new-task-proposals p))))]]
      (require! (str/includes? (or (:after target) (:raw target)) (:criterion-text r)) "Task lacks concrete criterion"))
    (require! (= (:unresolved-semantic-ids p) (mapv :historical-id (filter #(some :unresolved (:source-evidence %)) rs))) "Unresolved source histories silently cleared")
    (let [next-review (apply-coverage review operations)
          assigned-set (set assigned)]
      (require! (= (remove #(assigned-set (:id %)) (:historical-gap-traceability review))
                   (remove #(assigned-set (:id %)) (:historical-gap-traceability next-review))) "Unassigned history changed")
      (require! (= 577 (count (:historical-gap-traceability next-review))) "Original history lost"))
    (compose-tasks p files)
    {:assigned 70 :prepared-criterion-mappings 70 :independently-accepted 0
     :actual-task-count-delta 0 :proposed-task-count-delta (count (:new-task-proposals p))
     :inherited-unresolved-source-ids (count (:unresolved-semantic-ids p))
     :adoption :pending :implementation :unassessed}))
(defn -main [& args]
  (try
    (require! (= 2 (count args)) "Usage: domain-gap-patch PARTITION.edn COVERAGE.edn")
    (let [p (edn/read-string (slurp patch-path)) [partition-text review-text] (map slurp args)
          published-text (source/git-text "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30" "contracts/requirement-reconciliation.edn")]
      (require! (= (:partition-sha256 p) (sha partition-text)) "Partition drift")
      (require! (= (:coverage-receipt-sha256 p) (sha review-text)) "Coverage receipt drift")
      (require! (= (:published-manifest-sha256 p) (sha published-text)) "Published history drift")
      (prn (validate! p (edn/read-string partition-text) (edn/read-string review-text)
                      (edn/read-string published-text) (task-files p))))
    (finally (shutdown-agents))))
