(ns evolution-task-projection-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.string :as str]))

(load-file "tools/evolution_task_projection.clj")
(require '[evolution-task-projection] '[task-projection])

(def tasks (slurp evolution-task-projection/task-path))
(def roadmap (slurp evolution-task-projection/roadmap-path))
(defn rejects? [f]
  (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest authority-and-projection-boundaries
  (let [rows (evolution-task-projection/records tasks)]
    (is (= evolution-task-projection/semantic-ids (set (map :id rows))))
    (is (= {"Open" 10} (frequencies (map :status rows))))
    (is (evolution-task-projection/check-projection tasks roadmap))
    (is (= roadmap (evolution-task-projection/project
                     (str tasks "\n- [x] Historical work outside the authority\n") roadmap))))
  (let [changed (str/replace-first tasks "\nStatus: Open\n" "\nStatus: Partial; evidence pending\n")
        projected (evolution-task-projection/project changed roadmap)
        before (task-projection/region roadmap "evolution-task-projection")
        after (task-projection/region projected "evolution-task-projection")]
    (is (rejects? #(evolution-task-projection/check-projection changed roadmap)))
    (is (= (select-keys before [:prefix :suffix]) (select-keys after [:prefix :suffix])))
    (is (= projected (evolution-task-projection/project changed projected)))))

(deftest reject-incomplete-or-ambiguous-authority
  (doseq [bad [(str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "`evolution/change-review`" "`evolution/unknown`")
               (str/replace-first tasks "`evolution/rfc-review`" "`evolution/change-review`")
               (str/replace-first tasks "Required rejection evidence:" "Rejections:")
               (str/replace tasks "<!-- evolution-implementation-tasks:end -->" "")
               (str/replace-first tasks "\nStatus: Open\n" "\nStatus: Partial; [x](/tmp/private)\n")]]
    (is (rejects? #(evolution-task-projection/records bad)))))

(let [{:keys [fail error]} (run-tests)]
  (when (pos? (+ fail error)) (System/exit 1)))
