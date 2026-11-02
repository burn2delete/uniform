(binding [*command-line-args* nil] (load-file "tools/runtime_decision_allocation.clj"))
(require '[clojure.edn :as edn] '[clojure.string :as str] '[clojure.test :refer [deftest is run-tests]]
         '[runtime-decision-allocation :as allocation] '[runtime-requirement-family :as f] '[runtime-task-projection :as tasks])
(def expected (allocation/build (first *command-line-args*)))
(def proposal (f/read-edn allocation/output))
(def ds (edn/read-string (f/git-source allocation/base allocation/decision-path)))
(def task-source (f/git-source allocation/base tasks/task-path))
(defn refuses? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))
(deftest exact-dispatch-source-and-qualified-dispositions
  (is (allocation/validate proposal expected))
  (is (= 20 (count (:mappings proposal)) (count (:decision-field-edits proposal))))
  (is (= {:proposed-exact-current-criterion-witness 6 :proposed-missing-criterion 11 :source-metadata-adjudication-required 3}
         (frequencies (map :disposition (:mappings proposal)))))
  (doseq [m (:mappings proposal)]
    (is (= (get-in m [:dispatch-record :source-facet-ids]) (mapv :id (:source-evidence m))))
    (is (= (:required-output m) (get-in m [:before :required-output])))
    (doseq [w (:witnesses m)]
      (let [b (.getBytes task-source "UTF-8") raw (String. b (int (:start-byte w)) (int (- (:end-byte w) (:start-byte w))) "UTF-8")]
        (is (= raw (:raw w))) (is (= (:raw-sha256 w) (f/sha256 raw)))))))
(deftest actual-field-and-task-guards-preserve-open-state
  (let [after (allocation/apply-fields ds (:decision-field-edits proposal))
        text (allocation/apply-task-edits task-source (:task-criterion-edits proposal))]
    (is (= (mapv (juxt :id :status) (:remaining-decisions ds)) (mapv (juxt :id :status) (:remaining-decisions after))))
    (is (refuses? #(allocation/apply-fields after (:decision-field-edits proposal))))
    (is (refuses? #(allocation/apply-fields ds (assoc-in (:decision-field-edits proposal) [0 :before] :wrong))))
    (is (refuses? #(allocation/apply-fields ds (assoc-in (:decision-field-edits proposal) [0 :field-path] [:status]))))
    (is (refuses? #(allocation/apply-task-edits text (:task-criterion-edits proposal))))
    (is (refuses? #(allocation/apply-task-edits task-source (assoc-in (:task-criterion-edits proposal) [0 :record-sha256] "wrong"))))
    (is (= (mapv :id (tasks/records task-source)) (mapv :id (tasks/records text))))
    (is (every? #(= "Open" (:status %)) (tasks/records text)))
    (doseq [[a b] (map vector (tasks/records task-source) (tasks/records text))]
      (is (str/starts-with? (:acceptance b) (:acceptance a)))
      (is (= (dissoc a :acceptance) (dissoc b :acceptance))))))
(deftest no-forged-coverage-or-hidden-metadata-gaps
  (doseq [bad [(update proposal :mappings pop)
               (assoc-in proposal [:mappings 0 :id] "other-family")
               (assoc-in proposal [:mappings 0 :source-evidence 0 :raw-sha256] "wrong")
               (assoc-in proposal [:mappings 0 :dependencies] [])
               (assoc-in proposal [:mappings 0 :outputs] [])
               (assoc-in proposal [:mappings 0 :disposition] :accepted)
               (assoc-in proposal [:preservation :accepted-historical-mappings] 450)
               (assoc proposal :unresolved-ids [])
               (assoc-in proposal [:decision-field-edits 0 :after :implementation-acceptance] :accepted)]]
    (is (refuses? #(allocation/validate bad expected))))
  (is (= 3 (count (:unresolved-ids proposal))))
  (is (= 10 (count (set (mapcat :unresolved-facet-metadata-ids (:mappings proposal)))))))
(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
