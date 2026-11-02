(ns tooling-requirements-test
  (:require [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "tools/tooling_requirements.clj"))

(def input (tooling-requirements/immutable-inputs))
(def proposal (:proposal input))

(defn refuses? [f]
  (try
    (f)
    false
    (catch clojure.lang.ExceptionInfo _ true)))

(defn current-reference [reference]
  (assoc (first (filter #(= (:path %) (:path reference))
                         (get-in input [:source-view :current-source :task-sources])))
         :id (:id reference)))

(deftest frozen-family-and-proposal-only-result
  (let [result (tooling-requirements/check! proposal input)]
    (is (= [13 631 960 65 200 16]
           (mapv result [:documents :occurrences :facets :requirements
                         :edits :task-references])))
    (is (= :proposal-only (:authority result)))
    (is (= :unassessed (:implementation-acceptance result)))
    (is (= :pending (:independent-review result)))
    (is (= :preserved-source-view-only
           (:historical-task-criteria result)))
    (is (every? #(not= :accepted %)
                (get-in input [:source-view :nonclaims])))))

(deftest current-and-historical-task-boundaries-are-distinct
  (let [reference (first (:task-references (first (:units proposal))))
        historical (tooling-requirements/historical-task!
                    (:source-view input) reference)
        current (tooling-requirements/current-task!
                 (current-reference reference))]
    (is (seq (:title historical)))
    (is (seq (:acceptance historical)))
    (is (= (:title historical) (:title current)))
    (is (not= (:acceptance historical) (:acceptance current)))
    (is (re-find #"T1 acceptance:" (:acceptance historical)))
    (is (not (re-find #"T1 acceptance:" (:acceptance current))))
    (is (refuses? #(tooling-requirements/historical-task!
                    (:source-view input)
                    (assoc reference :acceptance-boundary-sha256 "wrong"))))
    (doseq [[key value] [[:path "specs/missing-workflow/tasks.md"]
                         [:sha256 (apply str (repeat 64 "0"))]
                         [:marker "renamed-implementation-tasks"]
                         [:id "renamed/task"]]]
      (is (refuses? #(tooling-requirements/current-task!
                      (assoc (current-reference reference) key value)))))))

(deftest source-and-negative-conservation-refuses
  (doseq [mutated
          [(assoc-in proposal [:source-documents 0 :current-sha256] "wrong")
           (update proposal :source-occurrences pop)
           (assoc-in proposal [:source-occurrences 0 :id] "invented/source/id")
           (update proposal :preserved-source-facets
                   #(vec (remove (fn [facet]
                                   (= :negative-acceptance (:kind facet))) %)))
           (update proposal :source-dispositions pop)
           (update proposal :units pop)]]
    (is (refuses? #(tooling-requirements/check! mutated input)))))

(deftest stale-duplicate-and-out-of-scope-edits-refuse
  (let [manifest (:manifest input)
        edits (:edits proposal)
        applied (tooling-requirements/apply-edits manifest edits)]
    (is (refuses? #(tooling-requirements/apply-edits applied edits)))
    (is (refuses? #(tooling-requirements/apply-edits
                    manifest (assoc-in edits [0 :before] :stale))))
    (is (refuses? #(tooling-requirements/apply-edits
                    manifest (conj edits (first edits)))))
    (is (refuses? #(tooling-requirements/apply-edits
                    manifest (assoc-in edits [0 :field-path]
                                       [:requirements 0]))))))

(deftest guarded-interfaces-refuse-false-closure
  (let [unit-with-two-refs (first (filter #(= 2 (count (:task-references %)))
                                          (:units proposal)))]
    (doseq [mutated
            [(assoc-in proposal [:units 0 :current-acceptance] :accepted)
             (assoc-in proposal [:units 0 :nonoverlap] :reviewed)
             (assoc-in proposal [:units 0 :task-gaps 0]
                       {:state :accepted :qualification "Complete"})
             (update-in proposal [:units 0 :task-references] pop)
             (assoc-in proposal [:units 0 :task-references 0 :id] "renamed/task")
             (assoc-in proposal [:units 0 :task-references 0]
                       (dissoc (first (:task-references (first (:units proposal))))
                               :acceptance-boundary-sha256))
             (assoc-in proposal [:units 0 :task-gaps]
                       [{:state :acceptance-qualification :qualification "other"}])
             (assoc-in proposal [:input :source-commit] "wrong")
             (assoc-in proposal [:units (.indexOf (vec (:units proposal)) unit-with-two-refs)
                                 :task-references]
                       (vec (butlast (:task-references unit-with-two-refs))))]]
      (is (refuses? #(tooling-requirements/check! mutated input))))))

(deftest source-view-drift-refuses
  (is (refuses? #(tooling-requirements/validate-view!
                  (assoc-in (tooling-requirements/view)
                            [:current-source :manifest :sha256] "wrong"))))
  (is (refuses? #(tooling-requirements/validate-view!
                  (assoc-in (tooling-requirements/view)
                            [:nonclaims 0] :accepted)))))

(let [result (run-tests 'tooling-requirements-test)]
  (shutdown-agents)
  (when (pos? (+ (:fail result) (:error result)))
    (System/exit 1)))
