(binding [*command-line-args* nil] (load-file "tools/runtime_residual_interfaces.clj"))
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str]
         '[runtime-residual-interfaces :as residual] '[runtime-requirement-family :as family]
         '[runtime-task-projection :as tasks])
(def expected (residual/build (first *command-line-args*)))
(def patch (family/read-edn residual/output))
(def base (family/git-source residual/predecessor tasks/task-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest exact-eight-interfaces-and-preserved-predecessor
  (is (residual/validate-patch patch expected))
  (let [after (residual/apply-edits base (:task-edits patch))
        before-rows (tasks/records base) after-rows (tasks/records after)]
    (is (= 8 (count (:mappings patch))))
    (is (= (mapv :id before-rows) (mapv :id after-rows)))
    (is (every? #(= "Open" (:status %)) after-rows))
    (doseq [[before row] (map vector before-rows after-rows)]
      (is (= (dissoc before :acceptance) (dissoc row :acceptance)))
      (is (str/starts-with? (:acceptance row) (:acceptance before))))
    (is (= (:task-successor-sha256 patch) (family/sha256 after)))
    (is (str/includes? after "before user code, validate selected provider existence"))
    (is (str/includes? after "P4: execute deterministic cleanup"))
    (is (str/includes? after "Execute the [SAFE2-SAFE8 contracts]"))
    (is (rejects? #(residual/apply-edits after (:task-edits patch))))
    (is (rejects? #(residual/apply-edits (str base "\nLater source change") (:task-edits patch))))))

(deftest forged-coverage-missing-source-and-assignment-refuse
  (doseq [bad [(update patch :mappings pop)
               (assoc-in patch [:mappings 0 :id] "runtime/not-assigned")
               (assoc-in patch [:mappings 0 :source-facets 0 :raw-sha256] "forged")
               (assoc-in patch [:mappings 0 :source-facets] [])
               (assoc-in patch [:mappings 0 :criterion] "See runtime heading")
               (assoc-in patch [:mappings 0 :acceptance-and-rejection-outputs] [])
               (assoc-in patch [:mappings 0 :implementation-acceptance] :accepted)
               (assoc-in patch [:counts :independently-adopted-mappings] 8)
               (assoc-in patch [:historical-conservation :retirements] 0)
               (assoc-in patch [:task-edits 0 :before] "stale")]]
    (is (rejects? #(residual/validate-patch bad expected)))))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
