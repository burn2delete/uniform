(binding [*command-line-args* nil] (load-file "tools/domain_requirement_patch.clj"))
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.edn :as edn])
(def p (edn/read-string (slurp domain-requirement-patch/proposal-path)))
(def m (edn/read-string (slurp (first *command-line-args*))))
(defn refuses? [f] (try (f) false (catch Exception _ true)))
(def git-source (memoize domain-requirement-patch/git-text))
(defn validate [proposal model]
  (with-redefs [domain-requirement-patch/git-text git-source]
    (domain-requirement-patch/validate! proposal model)))

(deftest exact-family-and-guarded-application
  (let [result (validate p m)
        changed (domain-requirement-patch/apply-edits m (:edits p))]
    (is (= 21 (:documents result)))
    (is (= :unassessed (:implementation-acceptance result)))
    (is (= :not-performed (:canonical-adoption result)))
    (is (= (dissoc m :requirements) (dissoc changed :requirements)))
    (is (= (mapv :id (:requirements m)) (mapv :id (:requirements changed))))
    (is (= (mapv #(dissoc % :task-reference :task-gap) (:requirements m))
           (mapv #(dissoc % :task-reference :task-gap) (:requirements changed))))
    (is (refuses? #(domain-requirement-patch/apply-edits changed (:edits p))))
    (is (refuses? #(domain-requirement-patch/apply-edits m (conj (:edits p) (first (:edits p))))))
    (is (refuses? #(domain-requirement-patch/apply-edits m [(assoc (first (:edits p)) :before-present? true :before :wrong)])))))

(deftest source-and-semantic-loss-refuse
  (doseq [bad [(update p :source-documents pop)
               (assoc-in p [:source-documents 0 :current-sha256] "wrong")
               (assoc-in p [:source-facets 0 :raw-sha256] "wrong")
               (update p :source-facets pop)
               (update p :source-dispositions pop)
               (assoc-in p [:source-occurrences 0 :id] "DOM1/source/invented")
               (assoc-in p [:units 0 :obligation] "Negative source condition omitted")
               (assoc-in p [:units 0 :equivalence-proposals] [{:target "different-context" :kind :alias}])
               (assoc-in p [:units 0 :current-acceptance] :accepted)]]
    (is (refuses? #(validate bad m)))))

(deftest task-identity-and-source-refuse
  (let [reference (:after (first (filter #(= [:task-reference] (:field-path %)) (:edits p))))]
    (doseq [bad [(assoc reference :sha256 "wrong")
                 (assoc reference :marker "missing-implementation-tasks")
                 (assoc reference :id "renamed-task")
                 (assoc reference :path "specs/missing-workflow/tasks.md")]]
      (is (refuses? #(domain-requirement-patch/task! bad))))))

(let [r (run-tests)] (shutdown-agents) (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))
