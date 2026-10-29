(ns tooling-requirements-test
  (:require [clojure.edn :as edn] [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))
(binding [*command-line-args* nil] (load-file "tools/tooling_requirements.clj"))
(require '[tooling-requirements])
(def input (tooling-requirements/inputs))
(def proposal (edn/read-string (slurp tooling-requirements/proposal-path)))
(defn refuses? [p]
  (try (tooling-requirements/check! p input) false
       (catch clojure.lang.ExceptionInfo _ true)))

(deftest pinned-family-and-open-task-interfaces
  (let [r (tooling-requirements/check! proposal input)]
    (is (= [13 631 960 65 200 16]
           (mapv r [:documents :occurrences :facets :requirements :edits :task-references])))
    (is (= :unassessed (:implementation-acceptance r)))
    (is (= :pending (:independent-review r)))))

(deftest task-population-status-and-history-are-preserved
  (doseq [path ["specs/tools-workflow/tasks.md" "specs/cli-workflow/tasks.md"]]
    (let [before (tooling-requirements/git-text "6276c94b0b753c888ef8fecc794bcc81245ea3b3" path)
          after (slurp path)
          strip #(str/replace % #"(?m)^Closing evidence(?: and governing contract)?: .+$" "")]
      (is (= (strip before) (strip after))))))

(deftest wrong-task-identities-refuse
  (let [ref (get-in proposal [:units 0 :task-references 0])]
    (doseq [[k value] [[:path "specs/missing-workflow/tasks.md"]
                      [:sha256 (apply str (repeat 64 "0"))]
                      [:marker "renamed-implementation-tasks"]
                      [:id "renamed/task"]
                      [:acceptance-boundary-sha256 "wrong"]]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (tooling-requirements/task! (assoc ref k value)))))))

(deftest source-and-negative-conservation-refuses
  (doseq [p [(assoc-in proposal [:source-documents 0 :current-sha256] "wrong")
             (update proposal :source-occurrences pop)
             (assoc-in proposal [:source-occurrences 0 :id] "invented/source/id")
             (update proposal :preserved-source-facets
                     #(vec (remove (fn [f] (= :negative-acceptance (:kind f))) %)))
             (update-in proposal [:units 0 :acceptance-facets] pop)
             (update proposal :source-dispositions pop)
             (update proposal :units pop)]]
    (is (refuses? p))))

(deftest stale-and-duplicate-field-operations-refuse
  (let [manifest (:manifest input) edits (:edits proposal)
        applied (tooling-requirements/apply-edits manifest edits)]
    (is (thrown? clojure.lang.ExceptionInfo
                 (tooling-requirements/apply-edits applied edits)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (tooling-requirements/apply-edits manifest
                   (assoc-in edits [0 :before] :stale))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (tooling-requirements/apply-edits manifest
                   (conj edits (first edits)))))))

(deftest contextual-equivalence-and-acceptance-refuse
  (let [n (first (keep-indexed #(when (seq (:equivalence-proposals %2)) %1) (:units proposal)))]
    (is (refuses? (assoc-in proposal [:units n :equivalence-proposals 0 :kind] :alias)))
    (is (refuses? (assoc-in proposal [:units n :equivalence-proposals 0 :target] "other/context")))
    (is (refuses? (assoc-in proposal [:units 0 :current-acceptance] :accepted)))
    (is (refuses? (assoc-in proposal [:units 0 :nonoverlap] :reviewed)))))

(deftest independent-review-lineage-and-false-closure-regressions
  (let [n (first (keep-indexed #(when (= [:task-interface-qualification] (:field-path %2)) %1)
                               (:edits proposal)))
        mutations
        [[:drop-input (dissoc proposal :input)]
         [:wrong-input (assoc proposal :input {:source-commit "wrong" :sha256 "wrong"})]
         [:drop-dependency-history (dissoc proposal :dependency-history)]
         [:drop-inherited-lineage (dissoc proposal :inherited-proposal-batch)]
         [:hide-unresolved (assoc proposal :unresolved-requirement-ids [])]
         [:false-unit-atomicity (assoc-in proposal [:units 0 :atomicity] :reviewed)]
         [:false-unit-history (assoc-in proposal [:units 0 :historical-support] [{:state :accepted}])]
         [:empty-dependency-edit (assoc-in proposal [:edits n :after] {:state :accepted})]
         [:lose-negative-unit-gap (assoc-in proposal [:units 0 :task-gaps 0]
                                          {:state :accepted :qualification "Complete"})]]]
    (doseq [[id mutated] mutations]
      (is (refuses? mutated) (name id)))))

(let [r (run-tests 'tooling-requirements-test)]
  (shutdown-agents)
  (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
