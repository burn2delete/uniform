(ns sh-crosswalk-test
  (:require [clojure.test :refer [deftest is run-tests]] [clojure.string :as str] [clojure.edn :as edn]))
(binding [*command-line-args* nil] (load-file "tools/sh_crosswalk.clj"))

(deftest exact-original-inventory-and-negative-population-controls
  (let [source (slurp sh-inventory/plan-path) rows (sh-inventory/inventory source)
        pinned (edn/read-string (slurp "specs/sh-family-crosswalk/original-inventory.edn"))]
    (is (= 180 (count rows)))
    (is (= rows (:requirements pinned)))
    (is (= (:source-file-sha256 pinned) (sh-inventory/sha256 source)))
    (doseq [changed [(str/replace source "**SH-00.1:" "**SH-99.1:")
                     (str/replace source "**SH-00.1:" "**SH-00.2:")
                     (str source "\n1. **SH-003.1:** Malformed extra\n\nChecks:\n")
                     (str/replace source "1. **SH-00.1:" "1. **SH-003.1:")]]
      (is (thrown? Exception (sh-inventory/inventory changed))))))

(deftest source-spans-are-utf8-byte-bound-not-character-offsets
  (let [source "\u03bb\naccepted\n" span (sh-inventory/source-span "fixture" source 2 10)]
    (is (= 3 (:start-byte span)))
    (is (= 11 (:end-byte span)))
    (is (= 2 (:start-line span) (:end-line span)))
    (is (= "accepted" (:raw span)))))

(deftest literal-task-routing-does-not-depend-on-live-ledger-order
  (let [rows (sh-crosswalk/task-records "core")
        expected (sh-crosswalk/task-ref {"core" rows} ["core" 1])]
    (is (= "core/current-evidence-and-remaining-work/core1-lower-core-forms" (:id expected)))
    (is (= expected (sh-crosswalk/task-ref {"core" (vec (reverse rows))} ["core" 1])))
    (is (thrown? Exception (sh-crosswalk/task-ref {"core" (vec (rest rows))} ["core" 1])))))

(deftest regeneration-refuses-to-relabel-changed-bytes-with-an-old-source-commit
  (let [text "original source" digest (sh-inventory/sha256 text)
        record {:task-authorities {"component" [{:path "tasks.md" :source-commit "pinned" :file-sha256 digest}]}
                :locator {:path "test.clj" :source-commit "pinned" :sha256 digest}}
        reads (atom #{})]
    (is (nil? (sh-crosswalk/validate-pinned-sources! record
                (fn [commit path] (swap! reads conj [commit path]) text))))
    (is (= #{["pinned" "tasks.md"] ["pinned" "test.clj"]} @reads))
    (is (thrown? Exception (sh-crosswalk/validate-pinned-sources! record (fn [_ _] "changed source"))))))

(deftest review-adoption-is-mapping-only-and-bound-to-unchanged-inputs
  (let [source (sh-crosswalk/source-view) view (sh-crosswalk/build)]
    (is (= :accepted-mapping-only (get-in view [:mapping-review :status])))
    (is (every? #(= :owner-and-independent-reviewed (:coverage %)) (:requirements view)))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (is (thrown? Exception (sh-crosswalk/adopt-review
                            (assoc-in source [:requirements 0 :task-references] []) sh-crosswalk/mapping-review)))
    (is (thrown? Exception (sh-crosswalk/adopt-review source (assoc sh-crosswalk/mapping-review :status :pending))))))

(deftest real-derived-view-is-deterministic-and-preserves-gate-boundaries
  (let [view (sh-crosswalk/build) text (sh-crosswalk/rollup view)
        n26 (filter #(= "SH-26" (:milestone %)) (:requirements view))]
    (is (nil? (sh-crosswalk/validate-pinned-sources! view (fn [_ path] (slurp path)))))
    (is (= view (edn/read-string (slurp sh-crosswalk/output-path))))
    (is (= text (slurp sh-crosswalk/rollup-path)))
    (is (= 180 (count (re-seq #"(?m)^\| \[SH-\d{2}\.[1-6]\]" text))))
    (is (= 18 (count (re-seq #"(?m)^\| \[SH07-C\d{2}\]" text))))
    (is (= [] (:task-references (last (:sh07-sequence view)))))
    (is (every? #(some (fn [t] (= "release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution" (:id t)))
                      (:task-references %)) n26))
    (is (= :incomplete (get-in view [:source-conflicts 0 :final-seed-retirement-recorded-flags :status])))
    (is (= :complete (get-in view [:source-conflicts 0 :whole-gate-recorded-flags :status])))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (is (= 1 (count (get-in view [:requirements 6 :task-references]))))
    (is (= "development-boundaries" (get-in view [:requirements 6 :task-references 0 :id])))))

(deftest executed-control-and-binding-joins-cannot-be-mir-shape-only
  (let [view (sh-crosswalk/build)
        requirements (into {} (map (juxt :id identity) (:requirements view)))
        sequence (into {} (map (juxt :id identity) (:sh07-sequence view)))]
    (doseq [id ["SH-13.3" "SH-13.5"]]
      (is (some #(= "runtime" (:component %)) (:task-references (requirements id))))
      (is (some #(= "emit/current-evidence-and-remaining-work/emit4-establish-conformance" (:id %))
                (:task-references (requirements id)))))
    (doseq [id ["SH07-C05" "SH07-C06"]]
      (is (some #(= "core/current-evidence-and-remaining-work/core2-preserve-binding-structure" (:id %))
                (:task-references (sequence id)))))))

(let [result (run-tests 'sh-crosswalk-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
