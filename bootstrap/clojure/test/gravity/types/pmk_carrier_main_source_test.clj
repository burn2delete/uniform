(ns gravity.types.pmk-carrier-main-source-test
  "Inert current-main source equations. No Gravity definition or project model is evaluated."
  (:require [clojure.test :refer [deftest is]] [clojure.edn :as edn]
            [gravity.types.ordinary-test-support :refer [before correspondence definitions erase
                                                         expected-for failure nodes owners replace-data
                                                         width without-accounting]]))
;; Historical source pin; this inert test does not renew or promote it.
(def base "ae0cd80165b2085e7ce362d0c7bfc5eeec79b2de")

(deftest main-defect-and-complete-correctness-equation
  (doseq [[prefix path] owners
          :let [name (symbol (str prefix "-pmk-parameter")) a (before base path) b (slurp path)
                old (definitions a) new (definitions b) of (get old name) nf (get new name)]]
    (is (= 7 (count (nth of 2))))
    (is (= 6 (count (nth nf 2))))
    (is (not= of nf))
    (is (= nf (expected-for of)) "Preserve semantic carrier and failure priority in the complete equation")
    (is (= (without-accounting of) (erase nf)))
    ;; This historical equation owns the complete parameter guard; accounting
    ;; is removed from every live owner independently of that semantic guard.
    (is (= 1 (count (filter #{width} (nodes nf)))))
    (is (= 1 (count (filter #{correspondence} (nodes nf))))))
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.types.private-map-parameter-test))))

(deftest effective-main-source-corruptions
  (doseq [[prefix path] owners
          :let [name (symbol (str prefix "-pmk-parameter"))
                old (get (definitions (before base path)) name) good (get (definitions (slurp path)) name)
                target (expected-for old)]]
    (doseq [[a b] [['(vector? values) true] ['(= (count names) (count values)) true]
                   ['(map? form) true] ['(symbol? original) true] ['(symbol? value) true]
                   ['(keyword? original) true] ['(keyword? value) true] ['(= original value) true]
                   ['(get values i) '(get values 0)] ['(get forms (get names i)) '(get forms (first names))]
                   ['(recur (+ i 1)) true] ['(= i (count names)) '(= i 1)]]]
      (let [bad (replace-data good a b)]
        (is (not= good bad))
        (is (not= target bad))))
    (let [bad (assoc (vec good) 2 (vec (butlast (nth good 2))))]
      (is (= 5 (count (nth bad 2))))
      (is (not= target bad)))
    (let [live (without-accounting old)
          accepted (first (filter #(and (map? %) (= :accepted (:status %))) (nodes live)))
          live-failure failure
          without (replace-data good (list 'if correspondence accepted live-failure) accepted)
          lifted (list 'defn name (nth good 2) (list 'if correspondence (nth without 3) live-failure))]
      (is (not= good lifted))
      (is (not= target lifted) "Lifting the walk changes existing failure priority"))))

(deftest data-expectations-are-not-semantic-results
  (let [p (edn/read-string (slurp "specs/accounting-removal/pmk-carrier-main-data.edn"))]
    (is (= :inert-data-only (:authority p)))
    (is (= :unrun (:semantic-execution p)))
    (is (= 7 (:parameter-ABI p)))
    (is (= :preserved (:remaining-accounting p)))
    (is (every? #(= :unrun (:observed %)) (:cases p)))
    (is (= #{:valid :carrier :width :late-name :late-marker :late-type :opaque :children :metadata}
           (set (map :kind (:cases p)))))))
