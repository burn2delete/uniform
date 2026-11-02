(binding [*command-line-args* nil]
  (load-file "tools/runtime_current_source_view.clj"))
(require '[clojure.test :refer [deftest is run-tests]])
(require '[runtime-current-source-view :as view])

(def source-view (view/view))

(deftest current-runtime-proposal-is-scoped-and-authenticated
  (let [result (view/validate-view! source-view)]
    (is (= :proposal-only (:authority result)))
    (is (= 5 (:task-count result)))
    (is (= (set view/expected-task-ids) (set (:task-ids result))))
    (is (every? #(= "Open" %) (:task-statuses result)))
    (is (= {:mappings 20
            :decision-field-edits 20
            :task-criterion-edits 3}
           (select-keys (:proposal result)
                        [:mappings :decision-field-edits :task-criterion-edits])))
    (is (= :unassessed (:implementation-acceptance result)))
    (is (= :pending-independent-review (:canonical-adoption result)))))

(deftest current-source-view-refuses-status-order-id-and-tamper-drift
  (is (= {:status true :order true :id true :tamper true}
         (view/negative-controls source-view))))

(deftest historical-and-current-task-identities-remain-distinct
  (let [historical (view/git-text
                    "86747916c4c876d707a1548113409fb939d854c9"
                    "specs/runtime-workflow/tasks.md")
        current (slurp "specs/runtime-workflow/tasks.md")]
    (is (= "da3305176c678e513b9c41a85138099f84d9464d4a37ce03e6ac14c2cb3693e9"
           (view/sha256 historical)))
    (is (= "64063a3fc0c38b2903a42e62f80fc1e2bb7ee820f8cefcad0256c9ce53b2db06"
           (view/sha256 current)))
    (is (not= historical current))
    (is (= view/expected-task-ids
           (mapv :id (view/task-rows historical))))
    (is (= view/expected-task-ids
           (mapv :id (view/task-rows current))))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result)))
    (System/exit 1)))
