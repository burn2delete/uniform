(ns core-language-reconciliation-test
  (:require [clojure.test :refer [deftest is run-tests]] [clojure.edn :as edn] [clojure.string :as str]))
(binding [*command-line-args* nil] (load-file "tools/core_language_reconciliation.clj")
                                (load-file "tools/runtime_task_projection.clj"))
(require '[core-language-reconciliation :as family] '[runtime-task-projection :as runtime])
(def proposal (edn/read-string (slurp family/output-path)))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest guarded-field-application
  (let [rows (mapv :preserved-record (:units proposal)) m {:requirements rows}
        edits (:edits proposal) changed (family/apply-edits m edits)]
    (is (= (mapv :id rows) (mapv :id (:requirements changed))))
    (doseq [[before after] (map vector rows (:requirements changed))]
      (is (= (dissoc before :task-reference :additional-task-interfaces :task-gap)
             (dissoc after :task-reference :additional-task-interfaces :task-gap))))
    (is (rejects? #(family/apply-edits changed edits)))
    (is (rejects? #(family/apply-edits m (conj edits (first edits)))))
    (is (rejects? #(family/apply-edits m [(assoc (first edits) :id "missing")])) )
    (is (rejects? #(family/apply-edits m [(assoc (first edits) :before "wrong")])) )
    ;; Neither aliases with different context nor lost rejection predicates can
    ;; be smuggled into this task-interface-only patch.
    (is (rejects? #(family/apply-edits m [(assoc (first edits) :collection :aliases)])))
    (is (rejects? #(family/apply-edits m [(assoc (first edits) :field-path [:obligation] :after "allow everything")])))))

(deftest task-source-and-startup-boundary
  (let [{:keys [before after before-sha256 after-sha256]} (first (:task-source-amendments proposal))
        reference {:id family/runtime-id :marker "runtime-implementation-tasks" :sha256 after-sha256}
        before-rows (runtime/records before) after-rows (runtime/records after)
        row (first (filter #(= family/runtime-id (:id %)) after-rows))]
    (is (= before-sha256 (family/sha256 before)))
    (is (nil? (family/validate-task! reference after)))
    (is (= (mapv :id before-rows) (mapv :id after-rows)))
    (is (= (mapv :status before-rows) (mapv :status after-rows)))
    (doseq [condition ["before user code" "existence" "artifact versions" "grant scopes" "compiled contracts"
                       "profile/target" "safety/audit" "unless a declared fallback" "negative mismatch"]]
      (is (str/includes? (:acceptance row) condition)))
    (doseq [bad [(assoc reference :sha256 "wrong") (assoc reference :marker "missing") (assoc reference :id "renamed")]]
      (is (rejects? #(family/validate-task! bad after))))
    (is (rejects? #(family/validate-task! reference (str after "tampered"))))
    (is (string? (runtime/project after (slurp runtime/roadmap-path))))))

(deftest exact-source-facet-controls
  (let [text "reject unsafe\n" o (first (family/source-occurrences "L15" text))
        f {:document-id "L15" :source-occurrence-id (:id o) :start-byte 0 :end-byte 13
           :raw-sha256 (family/sha256 "reject unsafe")}
        texts {"L15" text} occurrences {(:id o) o}]
    (is (nil? (family/validate-facet! f texts occurrences)))
    (doseq [bad [(assoc f :raw-sha256 "wrong") (assoc f :source-occurrence-id "invented")
                 (assoc f :document-id "L16") (assoc f :end-byte 30)]]
      (is (rejects? #(family/validate-facet! bad texts occurrences))))))

(let [r (run-tests)] (shutdown-agents) (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
