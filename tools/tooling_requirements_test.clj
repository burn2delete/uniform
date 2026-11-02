(ns tooling-requirements-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "tools/tooling_requirements.clj"))
(require '[tooling-requirements])

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
    (let [current-reference (current-reference reference)
          current-text (slurp (:path current-reference))
          closed-text (str/replace current-text
                                  "Status: Open"
                                  "Status: Closed")]
      (is (refuses? #(tooling-requirements/task-details
                      closed-text
                      (assoc current-reference
                             :sha256 (tooling-requirements/sha256 closed-text))))))
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

(deftest current-document-pairs-refuse-valid-but-wrong-source
  (let [source-view (tooling-requirements/view)
        documents (vec (get-in source-view [:current-source :documents]))
        replacement (second documents)
        wrong-pair-view (assoc-in source-view [:current-source :documents 0]
                                   replacement)]
    (is (= source-view
           (tooling-requirements/validate-document-pairs!
            source-view proposal)))
    (is (refuses? #(tooling-requirements/validate-document-pairs!
                    wrong-pair-view proposal)))))

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
             (update proposal :unresolved-requirement-ids #(vec (reverse %)))
             (assoc-in proposal [:unresolved-requirement-ids 1]
                       (first (:unresolved-requirement-ids proposal)))
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

(deftest current-manifest-transition-is-versioned-and-exact
  (let [source-view (tooling-requirements/view)
        transition tooling-requirements/current-manifest-source-transition
        baseline (:baseline transition)
        replay (assoc transition :applying baseline)
        wrong-pair (assoc-in transition [:applying :tree] (:tree baseline))
        wrong-body (assoc-in transition [:applying :sha256] (:sha256 baseline))
        wrong-task-extension (assoc-in transition
                                        [:task-source-bridge-extension :added-paths]
                                        [])]
    (is (= {:snapshot-provenance :git-commit-tree-blob-and-bytes-verified
            :population :conserved
            :interpretation :platform-generated-view-v2-plus-pr560-task-source-bridge-extension}
           (select-keys
            (tooling-requirements/validate-current-manifest-transition! source-view)
            [:snapshot-provenance :population :interpretation])))
    ;; A changed applying body, a mismatched commit/tree pair, and replay of the
    ;; baseline identity must all fail closed even when the source-view digest
    ;; remains frozen.
    (doseq [mutated [wrong-body wrong-pair replay wrong-task-extension]]
      (with-redefs [tooling-requirements/current-manifest-source-transition mutated]
        (is (refuses? #(tooling-requirements/validate-current-manifest-transition!
                        source-view)))))))

(let [result (run-tests 'tooling-requirements-test)]
  (shutdown-agents)
  (when (pos? (+ (:fail result) (:error result)))
    (System/exit 1)))
