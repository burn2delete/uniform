(ns domain-gap-patch-test
  (:require [clojure.edn :as edn] [clojure.test :refer [deftest is run-tests]]
            [domain-gap-patch :as gap] [domain-requirement-patch :as source]))

(def proposal (edn/read-string (slurp gap/patch-path)))
(def partition-input (edn/read-string (slurp (first *command-line-args*))))
(def review (edn/read-string (slurp (second *command-line-args*))))
(def published (edn/read-string (source/git-text "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30" "contracts/requirement-reconciliation.edn")))
(def files (gap/task-files proposal))
(defn validates [p] (gap/validate! p partition-input review published files))
(defn refuses? [f] (try (f) false (catch Exception _ true)))

(deftest assigned-criteria-and-task-proposals-are-concrete
  (let [result (validates proposal)
        composed (gap/compose-tasks proposal files)
        after (gap/apply-coverage review (:coverage-operations proposal))]
    (is (= 70 (:prepared-criterion-mappings result)))
    (is (= 0 (:independently-accepted result) (:actual-task-count-delta result)))
    (is (= 9 (:proposed-task-count-delta result)))
    (is (= 577 (count (:historical-gap-traceability after))))
    (is (= 57 (:inherited-unresolved-source-ids result)))
    (is (= (set (keys files)) (set (keys composed))))
    (is (refuses? #(gap/compose-tasks proposal composed)))
    (is (refuses? #(gap/apply-coverage after (:coverage-operations proposal))))
    (is (= (:retained-terminal-history partition-input)
           (get-in proposal [:history-conservation :retained-terminal-history])))))

(deftest stale-duplicate-and-outside-assignment-refuse
  (is (refuses? #(gap/compose-tasks proposal (update files "specs/domains-workflow/tasks.md" str "\nUnexpected later change"))))
  (is (refuses? #(gap/apply-coverage review (conj (:coverage-operations proposal) (first (:coverage-operations proposal))))))
  (doseq [bad [(update proposal :records pop)
               (update proposal :assigned-ids pop)
               (update proposal :coverage-operations pop)
               (assoc-in proposal [:coverage-operations 0 :id] "unassigned/other-family")
               (assoc-in proposal [:records 0 :before :coverage-disposition] :concrete-task-criterion-witness)]]
    (is (refuses? #(validates bad)))))

(deftest forged-coverage-source-task-and-negative-conditions-refuse
  (doseq [bad [(assoc-in proposal [:records 0 :after :whole-facet-entailment-acceptance] :accepted)
               (assoc-in proposal [:records 0 :after :implementation-acceptance] :accepted)
               (assoc-in proposal [:records 0 :criterion-sha256] "forged")
               (assoc-in proposal [:records 0 :source-evidence 0 :raw-sha256] "wrong")
               (assoc-in proposal [:records 0 :source-evidence 0 :source-occurrence-id] "other/source/context")
               (assoc-in proposal [:records 0 :source-evidence 0 :original-source-sha256] "wrong")
               (assoc-in proposal [:records 0 :negative-fixture] "")
               (assoc-in proposal [:records 0 :dependencies] [])
               (assoc-in proposal [:records 0 :task-reference :id] "missing-task")
               (assoc proposal :unresolved-semantic-ids [])
               (assoc-in proposal [:task-amendments 0 :marker] "wrong-marker")
               (assoc-in proposal [:task-amendments 0 :before-present?] false)
               (assoc-in proposal [:new-task-proposals 0 :sha256] "wrong")
               (assoc-in proposal [:new-task-proposals 0 :raw] "- [x] `automation` Invented acceptance")]]
    (is (refuses? #(validates bad)))))

(deftest coverage-write-payload-must-match-validated-record
  ;; Mutate the actual write payload only, preserving the validated records.
  (doseq [[field value] [[[:after :implementation-acceptance] :accepted]
                        [[:after :whole-facet-entailment-acceptance] :accepted]
                        [[:after :criterion-text] "Forged coverage with no source-entailing criterion"]
                        [[:after :criterion-task-id] "wrong-task"]
                        [[:after :source-facet-ids] []]
                        [[:after :historical-coverage-history] []]
                        [[:after :unexpected-field] :unvalidated]]]
    (is (refuses? #(validates (assoc-in proposal (into [:coverage-operations 0] field) value)))
        (str "Actual coverage write mutation must reject: " field)))
  (is (refuses? #(validates (-> proposal
                               (assoc-in [:records 0 :after :criterion-task-id] "wrong-task")
                               (assoc-in [:coverage-operations 0 :after :criterion-task-id] "wrong-task"))))))

(let [result (run-tests)] (shutdown-agents)
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
