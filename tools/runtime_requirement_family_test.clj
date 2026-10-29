(binding [*command-line-args* nil] (load-file "tools/runtime_requirement_family.clj"))
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str]
         '[runtime-requirement-family] '[runtime-task-projection])
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

(deftest replay-task-retains-source-exception-and-rejections
  ;; These are task-interface controls, not a simulated runtime or execution proof.
  (let [id "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
        acceptance (fn [text] (:acceptance (first (filter #(= id (:id %)) (runtime-task-projection/records text)))))
        current (acceptance (slurp runtime-task-projection/task-path))
        rejected (acceptance (runtime-requirement-family/git-source
                              "769050c60259b463556cd5741d905180c98117e9" runtime-task-projection/task-path))
        required ["accept an authorized graph-declared replay-safe repetition where no R7/R8 recorded-output obligation applies"
                  "Reject undeclared repeated external calls"
                  "live nondeterministic calls in R7/R8 recorded-output segments even with a replay-safe graph declaration"
                  "repetitions without required authority"
                  "R7/R8 replay-required segments return recorded model/tool/retrieval/memory/review decisions or reject a missing record"]
        covers? (fn [text] (and (every? #(str/includes? text %) required)
                               (not (str/includes? text "Reject live calls during replay,"))))]
    (is (str/includes? (slurp "docs/phase-08-runtime-architecture/117-r6-concurrency-runtime-design.md")
                       "calls twice unless the graph declares them replay-safe."))
    (is (covers? current))
    (is (not (covers? rejected)) "Original over-restrictive clause must fail")
    (doseq [clause required]
      (is (not (covers? (str/replace current clause ""))) clause))
    (is (not (covers? (str current " Reject live calls during replay,")))
        "A blanket refusal cannot silently override the allowed R6 fixture")))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
