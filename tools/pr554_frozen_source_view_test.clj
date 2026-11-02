(ns pr554-frozen-source-view-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "tools/pr554_frozen_source_view.clj"))
(require '[pr554-frozen-source-view :as view])

(def frozen (edn/read-string (slurp view/output-path)))

(deftest frozen-source-identities-and-family-accounting
  (is (view/check-static! frozen))
  (is (= 82 (:selected-entry-count frozen)))
  (is (= {:source-decisions-and-historical-mappings 54
          :conservation-inventory 1
          :qualified-b6-receipts 7
          :qualified-c18-receipts 13
          :domain-gap-context 2
          :read-only-census-and-manifest-input 1
          :read-only-census-tooling 4}
         (:families frozen)))
  (is (= 82 (:verified-count (view/verify-source! frozen))))
  (is (= 28785 (get-in (first (filter #(= :conservation-inventory (:family %))
                                     (:entries frozen)))
                       [:summary :counts :facets])))
  (is (= 70 (get-in (first (filter #(= "specs/requirement-reconciliation/domain-gap-completion.edn"
                                      (:path %)) (:entries frozen)))
                    [:summary :counts :records]))))

(deftest historical-view-has-explicit-boundaries
  (is (contains? (set (:nonclaims frozen)) :not-current-canonical-source))
  (is (contains? (set (:nonclaims frozen)) :not-requirement-denominator))
  (is (contains? (set (:nonclaims frozen)) :no-blind-hash-refresh))
  (is (contains? (set (:nonclaims frozen)) :no-denied-action-retry))
  (is (every? #(not= :same (get-in % [:current-main :state]))
              (:entries frozen))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result)))
    (System/exit 1)))
