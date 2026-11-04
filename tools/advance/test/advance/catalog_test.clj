(ns advance.catalog-test
  (:require [advance.catalog :as catalog]
            [clojure.test :refer [deftest is]]))

(def task-text
  "# Tasks\n<!-- reader-implementation-tasks:start -->\n- [ ] `reader/one` One\n\nDone when: actual source and rejection evidence.\n- [x] `reader/two` Two\nAccepted; immutable receipt.\n<!-- reader-implementation-tasks:end -->\n## Historical migration\n- [ ] T001 Old planning\n<!-- reader-product-implementation-tasks:start -->\n- [ ] `reader/three` Three\n<!-- reader-product-implementation-tasks:end -->\n")

(deftest discovers-authorities-without-historical-checkboxes
  (let [rows (catalog/parse "specs/reader/tasks.md" task-text)]
    (is (= ["reader/one" "reader/two" "reader/three"] (mapv :id rows)))
    (is (= [false true false] (mapv :checked rows)))
    (is (= 3 (:line (first rows))))
    (is (= :implementation-region (:authority (first rows))))
    (is (re-find #"rejection evidence" (:body (first rows))))
    (is (not-any? #(re-find #"Old planning" (:body %)) rows))))

(deftest unmarked-current-tasks-remain-visible
  (let [text "# Current tasks\n- [ ] ACC-01 Remove accounting.\n- [ ] T001 Implement reader.\n- [x] Preserve metadata.\n## Historical attempts\n- [ ] ACC-02 Retired attempt.\n"
        rows (catalog/parse "specs/current/tasks.md" text)]
    (is (= 3 (count rows)))
    (is (= ["ACC-01" "T001"] (mapv :id (take 2 rows))))
    (is (every? #(= :unmarked (:authority %)) rows))
    (is (= (mapv :key rows) (mapv :key (catalog/parse "specs/current/tasks.md" (str "\n" text)))))))

(deftest rejects-corrupt-authorities-and-invented-or-repeated-selections
  (doseq [text ["<!-- reader-implementation-tasks:start -->\n- [ ] `one` One"
                "<!-- reader-implementation-tasks:end -->"
                "- [ ] T001 One\n- [ ] T001 Duplicate"]]
    (is (thrown? Exception (catalog/parse "specs/test/tasks.md" text))))
  (let [rows (catalog/parse "specs/reader/tasks.md" task-text)
        source {:tasks rows}
        increment {:key "implement-one" :task (:key (first rows)) :paths ["specs/reader/tasks.md" "src"]}]
    (is (= [increment] (catalog/validate-selection! source [increment] {})))
    (doseq [bad [(assoc increment :task "invented")
                 (assoc increment :task (:key (second rows)))
                 (assoc increment :paths ["src"])]]
      (is (thrown? Exception (catalog/validate-selection! source [bad] {}))))
    (is (thrown? Exception (catalog/validate-selection! source [increment] {"implement-one" {:status :done}})))))
