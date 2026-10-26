(ns requirement-reconciliation-test
  (:require [clojure.test :refer [deftest is run-tests]] [clojure.string :as str]))
(binding [*command-line-args* nil] (load-file "tools/requirement_reconciliation.clj"))
(require '[requirement-reconciliation :as rr])

(defn open-manifest []
  {:scope-version "fixture-1" :scope-state :open :requirements []
   :source-dispositions [] :aliases [] :scope-decisions [] :scope-amendments []})

(deftest byte-conservation-and-distinct-occurrences
  (let [text "# Rules\n\nRepeat.\n\nRepeat.\n\nUnicode: \u03bb\n"
        occurrences (rr/source-occurrences "D0" text)
        blocks (filter #(= :source-block (:structural-kind %)) occurrences)]
    (is (= (alength (.getBytes text "UTF-8")) (:end-byte (last occurrences))))
    (is (= (count occurrences) (count (set (map :id occurrences)))))
    (is (= 2 (count (filter #(= (rr/sha256 "Repeat.\n") (:raw-sha256 %)) blocks))))
    (is (= (map :end-byte (butlast occurrences)) (map :start-byte (rest occurrences))))
    (is (every? #(= (rr/sha256 text) (:source-sha256 %)) occurrences))
    (is (not= (mapv :id occurrences) (mapv :id (rr/source-occurrences "D0" (str text "change")))))))

(deftest all-prose-code-and-table-context-retained
  (let [text "# Rules\n\nA declarative semantic rule with no modal verb.\n\n| Shape | Meaning |\n| nil | literal |\n\n```clojure\n(def x 1)\n\n(def y 2)\n```\n"
        os (rr/source-occurrences "L1" text)
        contexts (mapcat :context os)]
    (is (some #(= ["Rules"] (mapv :text (:headings %))) contexts))
    (is (= 2 (count (filter :table-row? contexts))))
    (is (some #(and (:fence %) (= 11 (:line %))) contexts))
    (is (pos? (count (:unresolved-source-occurrences (rr/project (open-manifest) {"L1" text})))))))

(defn complete-fixture []
  (let [text "Do one thing.\n" occurrence (first (rr/source-occurrences "X" text))]
    [(assoc (open-manifest) :scope-state :reviewed
            :requirements [{:id "fixture/one-thing" :atomicity :reviewed :nonoverlap :reviewed}]
            :source-dispositions [{:occurrence-id (:id occurrence) :kind :requirement
                                   :requirement-ids ["fixture/one-thing"]}])
     {"X" text}]))

(deftest unknowns-and-scope-decisions-stay-visible
  (let [[manifest sources] (complete-fixture)]
    (is (= 1 (:denominator (rr/project manifest sources))))
    (is (nil? (:overall-rate (rr/project manifest sources))))
    (is (false? (:denominator-ready? (rr/project (assoc manifest :scope-state :open) sources))))
    (is (false? (:denominator-ready? (rr/project manifest (assoc sources "new" "New in-scope obligation.\n")))))
    (is (false? (:denominator-ready? (rr/project (assoc manifest :scope-decisions [{:id "pending" :state :unresolved}]) sources))))
    (is (false? (:denominator-ready? (rr/project (assoc-in manifest [:requirements 0 :atomicity] :unresolved) sources))))
    (is (empty? (:proposed-requirement-ids (rr/project (open-manifest) sources))))))

(deftest duplicate-and-dangling-source-dispositions-reject
  (let [[manifest sources] (complete-fixture)]
    (is (thrown? Exception (rr/project (update manifest :requirements conj (first (:requirements manifest))) sources)))
    (is (thrown? Exception (rr/project (update manifest :source-dispositions conj (first (:source-dispositions manifest))) sources)))
    (is (thrown? Exception (rr/project (assoc-in manifest [:source-dispositions 0 :requirement-ids] ["absent"]) sources)))
    (is (thrown? Exception (rr/project (assoc-in manifest [:source-dispositions 0 :occurrence-id] "absent") sources)))
    (is (thrown? Exception (rr/project (assoc-in manifest [:source-dispositions 0 :kind] :silently-excluded) sources)))))

(deftest aliases-and-amendments-cannot-shrink-scope-silently
  (let [[manifest sources] (complete-fixture)]
    (is (thrown? Exception (rr/project (assoc manifest :aliases [{:id "a" :target "b"} {:id "b" :target "a"}]) sources)))
    (is (thrown? Exception (rr/project (assoc manifest :aliases [{:id "a" :target "absent"}]) sources)))
    (is (= 1 (:denominator (rr/project (assoc manifest :aliases [{:id "view-1" :target "fixture/one-thing"}]) sources))))
    (is (thrown? Exception (rr/validate-amendment! {:previous-ids ["a"] :active-ids [] :added [] :removed [] :authority "review" :reason "scope"})))
    (is (thrown? Exception (rr/validate-amendment! {:previous-ids ["a"] :active-ids [] :added [] :removed ["a"]})))
    (is (nil? (rr/validate-amendment! {:previous-ids ["a"] :active-ids ["b"] :added ["b"] :removed ["a"] :authority "review" :reason "versioned split"})))))

(deftest actual-source-census-has-no-requirement-or-dashboard-authority
  (let [report (rr/audit ".")]
    (is (= 245 (:source-documents report)))
    (is (= 240 (count (filter #(re-matches #"[A-Z]+[0-9]+" %) (keys (:source-digests report))))))
    (is (false? (:denominator-ready? report)))
    (is (nil? (:denominator report)))
    (is (nil? (:overall-rate report)))
    (is (seq (:unresolved-source-occurrences report)))
    (is (some #{:no-dashboard-write} (:limits report)))))

(let [{:keys [fail error]} (run-tests)] (System/exit (if (zero? (+ fail error)) 0 1)))
