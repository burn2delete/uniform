(binding [*command-line-args* nil] (load-file "tools/runtime_requirement_family.clj"))
(require '[clojure.test :refer [deftest is run-tests]])
(def p (runtime-requirement-family/read-edn runtime-requirement-family/proposal-path))
(def manifest-path (first *command-line-args*))
(runtime-requirement-family/require! manifest-path "Pass the preserved applying manifest path")
(def m (runtime-requirement-family/read-edn manifest-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))
(defn change-task-ref [p k value]
  (-> p (assoc-in [:units 0 :task-references 0 k] value)
      (assoc-in [:edits 0 :after :task-reference k] value)))

(deftest complete-family-and-exact-guards
  (is (= {:documents 12 :occurrences 876 :facets 1021 :units 43 :task-amendments 5
          :unresolved-facets 44 :implementation-acceptance :unassessed :adoption :central-owner-only}
         (runtime-requirement-family/check p m)))
  (let [records (into {} (map (juxt :id identity) (:requirements m)))
        after (runtime-requirement-family/guarded-apply records (:edits p))]
    (is (rejects? #(runtime-requirement-family/guarded-apply after (:edits p))))
    (is (rejects? #(runtime-requirement-family/guarded-apply
                    (update-in records [(:id (first (:edits p))) :obligation] str " different context") (:edits p))))
    (is (rejects? #(runtime-requirement-family/guarded-apply (dissoc records (:id (first (:edits p)))) (:edits p))))))

(deftest source-task-and-context-negative-controls
  (doseq [bad [(update p :source-documents pop)
               (assoc-in p [:source-documents 0 :current-sha256] "wrong")
               (assoc-in p [:source-occurrences 0 :id] "R1/source/invented")
               (update p :source-facets #(vec (remove (fn [f] (= :negative-case (:kind f))) %)))
               (update p :source-dispositions pop)
               (assoc-in p [:preserved-lineage :aliases 0 :qualification] "Different context")
               (assoc-in p [:units 0 :acceptance-facets] [])
               (assoc-in p [:units 0 :current-acceptance] :accepted)
               (assoc-in p [:edits 0 :after :source-facet-ids] [])
               (change-task-ref p :path "missing/tasks.md")
               (change-task-ref p :sha256 "wrong")
               (change-task-ref p :marker "migration-tasks")
               (change-task-ref p :id "runtime/renamed")
               (update p :edits pop)
               (update p :edits conj (first (:edits p)))
               (assoc-in p [:task-amendments 0 :before] "Stale predecessor")]]
    (is (rejects? #(runtime-requirement-family/check bad m)))))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
