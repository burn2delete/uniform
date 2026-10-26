(ns acceptance-dependencies-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.string :as str]))

(binding [*command-line-args* nil] (load-file "tools/acceptance_dependencies.clj"))
(require '[acceptance-dependencies :as dependencies])

(defn phase [n deps ids]
  (str "## Phase " n " - Fixture\n\nDependencies: " deps "\n\nTasks:\n\n"
       (apply str (map #(str "- [ ] `" % "` Fixture outcome\n") ids))))

(deftest actual-mutual-reachability
  (let [graph {"a" #{"b"} "b" #{"a"} "c" #{"b"} "d" #{} "e" #{"e"}}]
    (is (= [["a" "b"] ["e"]] (dependencies/cycles graph)))
    (is (= #{"a" "b" "c"} (dependencies/reachable graph "c")))
    (is (empty? (dependencies/cycles {"a" #{} "b" #{"a"}})))
    (is (thrown? Exception (dependencies/cycles {"a" #{"absent"}})))
    (is (thrown? Exception (dependencies/cycles {})))))

(deftest source-graph-and-explicit-wildcards
  (let [text (str (phase "00" "none." ["FL-P00-T01"])
                  (phase "01" "`FL-P00-T01`." ["FL-P01-T01"])
                  (phase "14" "all feature phases whose behavior is under test." ["FL-P14-T01"])
                  (phase "18" "all previous full-language tasks." ["FL-P18-T01"]))
        graph (dependencies/full-language-graph text)]
    (is (= #{"FL-P01-T01"} (get graph "FL-P14-T01")))
    (is (= #{"FL-P00-T01" "FL-P01-T01" "FL-P14-T01"} (get graph "FL-P18-T01")))
    (is (empty? (dependencies/cycles graph)))
    (is (thrown? Exception (dependencies/full-language-graph (str text (phase "19" "none." ["FL-P18-T01"])))))
    (is (thrown? Exception (dependencies/full-language-graph (str/replace text "none." "the next successor."))))
    (is (thrown? Exception (dependencies/full-language-graph (str/replace text "Dependencies:" "Prerequisite:"))))))

(deftest staged-edges-preserve-every-executable-prerequisite
  (let [original {"producer" #{"consumer"} "consumer" #{"producer"}}
        staged (dependencies/staged-graph original)]
    (is (= [["consumer" "producer"]] (dependencies/cycles original)))
    (is (empty? (dependencies/cycles staged)))
    (doseq [[id inputs] original]
      (is (= (set (map #(str % "/implementation") (conj inputs id)))
             (get staged (str id "/composition"))))
      (is (= #{(str id "/composition")} (get staged (str id "/acceptance"))))
      (is (not (contains? (get staged (str id "/composition")) "producer/interface"))))
    (is (= [["consumer/acceptance" "producer/acceptance"]]
           (dependencies/cycles (assoc staged "consumer/acceptance" #{"producer/acceptance"}
                                             "producer/acceptance" #{"consumer/acceptance"}))))))

(deftest migration-history-does-not-become-implementation
  (let [record (str "- [x] T001 Historical migration\n"
                    "<!-- reader-implementation-tasks:start -->\n"
                    "- [ ] `reader/real-outcome` Actual task\n"
                    "<!-- reader-implementation-tasks:end -->\n"
                    "- [x] T005 Historical integration\n")]
    (is (= ["reader/real-outcome"] (dependencies/implementation-ids record "reader")))
    (is (nil? (dependencies/implementation-ids "- [x] T001 Historical only" "reader")))
    (is (thrown? Exception (dependencies/implementation-ids (str record record) "reader")))
    (is (thrown? Exception (dependencies/implementation-ids (str/replace record "reader/real-outcome" "syntax/other") "reader")))
    (is (thrown? Exception (dependencies/implementation-ids (str/replace record "tasks:end" "tasks:missing") "reader")))))

(deftest component-links-are-references
  (let [registry "| Reader / `reader` | boundary | outcome |\n| Syntax / `syntax` | boundary | outcome |\n"
        files {"docs/component-roadmaps/reader.md" "[Syntax](syntax.md#release-outcome)"
               "docs/component-roadmaps/syntax.md" "[Reader](reader.md#release-outcome)"}
        census (dependencies/component-census registry files (constantly false))]
    (is (= ["syntax"] (get-in census ["reader" :reference-links])))
    (is (= "docs/component-roadmaps/reader.md" (get-in census ["reader" :task-source])))
    (is (= :roadmap-remains-authority (get-in census ["syntax" :migration])))
    (is (thrown? Exception (dependencies/component-census (str registry registry) files (constantly false))))))

(deftest real-source-audit-is-read-only-and-nonauthoritative
  (let [paths [dependencies/roadmap-path dependencies/milestone-path]
        before (mapv slurp paths)
        result (dependencies/audit)]
    (is (= :structural-only (:authority result)))
    (is (= before (mapv slurp paths)))
    (is (= 36 (count (get-in result [:full-language :edges]))))
    (is (= 30 (count (get-in result [:self-hosting :edges]))))
    (is (= 240 (count (:canonical-contract-source-digests result))))
    (is (= 31 (count (get-in result [:retained-spec-kit :packages]))))
    (is (= 124 (count (get-in result [:retained-spec-kit :artifact-source-digests]))))
    (is (= 236 (get-in result [:retained-spec-kit :directly-linked-contracts])))
    (is (= ["GOV1" "GOV6" "GOV7" "GOV8"] (get-in result [:retained-spec-kit :without-direct-package-links])))
    (is (= 5 (count (get-in result [:full-language :coarse-acceptance-cycles]))))
    (is (empty? (get-in result [:full-language :staged-cycles])))
    (is (empty? (get-in result [:self-hosting :cycles])))
    (is (contains? (set (:limits result)) :no-semantic-acceptance))
    (is (thrown? Exception (dependencies/run ["--write"])))
    (is (thrown? Exception (dependencies/run ["--check" "outside.edn"])))))

(let [result (run-tests 'acceptance-dependencies-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
