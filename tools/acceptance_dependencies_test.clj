(ns acceptance-dependencies-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.edn :as edn]
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

(deftest missing-terminal-and-malformed-task-rows-reject
  (let [text (slurp dependencies/roadmap-path)
        without-leaf (str/replace text #"(?m)^- \[ \] `FL-P18-T01`[^\n]*\n" "")]
    (is (thrown? Exception
                 (dependencies/require-population! dependencies/full-language-task-ids
                   (keys (dependencies/full-language-graph without-leaf)) "full-language task")))
    (doseq [bad ["- [] `FL-P18-T01`" "- [ ] FL-P18-T01" "- [y] `FL-P18-T01`"]]
      (is (thrown? Exception (dependencies/full-language-graph
                             (str/replace text "- [ ] `FL-P18-T01`" bad)))))
    (is (thrown? Exception
                 (dependencies/require-population! dependencies/full-language-task-ids
                   (conj (disj dependencies/full-language-task-ids "FL-P18-T01") "FL-P18-T99") "full-language task")))))

(deftest full-language-dependencies-consume-whole-tokens
  (let [source (str (phase "00" "none." ["FL-P00-T01"])
                    (phase "06" "`FL-P00-T01`." ["FL-P06-T03"]))]
    (is (= #{"FL-P00-T01"} (get (dependencies/full-language-graph source) "FL-P06-T03")))
    (doseq [bad ["`FL-P06-T030`." "`FL-P06-T03`garbage." "`FL-P06-T99`."
                 "`FL-P00-T01`,." ",`FL-P00-T01`." "`FL-P00-T01` `FL-P06-T03`."
                 "`FL-P00-T01`; `FL-P06-T03`." "FL-P00-T01." "`FL-P00-T01`. trailing"
                 "`FL-P00-T01`.." "`FL-P00-T01`, `FL-P00-T01`." "`FL-P00-T01`"]]
      (is (thrown? Exception
                   (dependencies/full-language-graph
                    (str/replace source "Dependencies: `FL-P00-T01`." (str "Dependencies: " bad)))))))
  (is (= #{"FL-P00-T01" "FL-P06-T03"}
         (dependencies/dependency-list "`FL-P00-T01`,\n`FL-P06-T03`." #"`(FL-P\d{2}-T\d{2})`" true))))

(deftest milestone-dependencies-consume-whole-cells
  (let [source (apply str
                (for [n (range 30)]
                  (format "| [SH-%02d](#sh-%02d) | Outcome | %s | open |\n"
                          n n (if (zero? n) "none" "SH-00"))))]
    (is (= #{"SH-00"} (get (dependencies/milestone-graph source) "SH-01")))
    (doseq [bad ["SH-XX" "SH-003" "SH-00x" "SH-99" "SH-00," ", SH-00"
                 "SH-00 SH-01" "SH-00; SH-01" "none, SH-00" "SH-00 and SH-01"
                 "`SH-00`" "SH-00." "SH-00, SH-00" ""]]
      (is (thrown? Exception (dependencies/milestone-graph
                             (str/replace source "| SH-00 |" (str "| " bad " |"))))))
    (is (thrown? Exception (dependencies/milestone-graph (str/replace source "[SH-00]" "[SH-003]"))))
    (is (thrown? Exception (dependencies/milestone-graph (str/replace source "[SH-00]" "[SH-XX]"))))
    (is (= #{"SH-00" "SH-01"} (dependencies/dependency-list "SH-00, SH-01" #"(SH-\d{2})" false)))))

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
    (is (thrown? Exception (dependencies/implementation-ids (str/replace record "reader/real-outcome" "") "reader")))
    (is (thrown? Exception (dependencies/implementation-ids (str/replace record "tasks:end" "tasks:missing") "reader")))))

(deftest literal-backend-project-keys-are-not-renamed
  (let [ids ["backend-interface" "backend-c" "backend-llvm" "backend-wasm"
             "backend-jvm" "backend-js" "backend-mlir" "backend-gpu"
             "backend-hdl" "backend-workflow" "backend-ai" "backend-query"
             "backend-mobile" "backend-emission" "backend-conformance"]
        region (str "- [x] T001 Historical migration\n"
                    "<!-- backends-implementation-tasks:start -->\n"
                    (apply str (map #(str "- [ ] `" % "` Original family\n") ids))
                    "<!-- backends-implementation-tasks:end -->\n"
                    "- [x] T005 Historical integration\n")]
    (is (= ids (dependencies/implementation-ids region "backends")))
    (is (= [(first ids)]
           (dependencies/implementation-ids
            "<!-- backends-implementation-tasks:start -->\n- [ ] `backend-interface` Original\n<!-- backends-implementation-tasks:end -->"
            "backends")))
    ;; This is a census, not a second component identity/ownership validator.
    (is (= ["existing-project-key"]
           (dependencies/implementation-ids
            "<!-- tools-implementation-tasks:start -->\n- [ ] `existing-project-key` Original\n<!-- tools-implementation-tasks:end -->"
            "tools")))
    (doseq [bad [(str/replace region "`backend-llvm`" "`backend-c`")
                 (str/replace region "`backend-llvm`" "``")
                 (str/replace region "- [ ] `backend-llvm`" "- [y] `backend-llvm`")
                 (str/replace region "- [ ] `backend-llvm`" "- [ ] backend-llvm")]]
      (is (thrown? Exception (dependencies/implementation-ids bad "backends"))))))

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
    (is (= 240 (get-in result [:retained-spec-kit :directly-linked-contracts])))
    (is (empty? (get-in result [:retained-spec-kit :without-direct-package-links])))
    (is (= 5 (count (get-in result [:full-language :coarse-acceptance-cycles]))))
    (is (empty? (get-in result [:full-language :staged-cycles])))
    (is (empty? (get-in result [:self-hosting :cycles])))
    (is (contains? (set (:limits result)) :no-semantic-acceptance))
    (is (thrown? Exception (dependencies/run ["--write"])))
    (is (thrown? Exception (dependencies/run ["--check" "outside.edn"])))))

(deftest conditional-edges-never-turn-unknown-into-eligibility
  (let [nodes {"consumer" {:state :unassessed} "producer" {:state :accepted}}
        edges [{:from "consumer" :to "producer" :kind :selected-output :condition :needed :source {:fixture true}}]]
    (is (= #{"producer"} (get (dependencies/typed-adjacency nodes edges {}) "consumer")))
    (is (= :unknown-condition (get-in (dependencies/advisory-frontier nodes edges {}) ["consumer" :state])))
    (is (= #{} (get (dependencies/typed-adjacency nodes edges {:needed false}) "consumer")))
    (is (= :eligible-for-declared-facet (get-in (dependencies/advisory-frontier nodes edges {:needed false}) ["consumer" :state])))
    (is (= :eligible-for-declared-facet (get-in (dependencies/advisory-frontier nodes edges {:needed true}) ["consumer" :state])))
    (is (thrown? Exception (dependencies/typed-adjacency nodes edges {:needed nil})))
    (is (thrown? Exception (dependencies/typed-adjacency nodes (assoc-in edges [0 :to] "unknown") {})))
    (is (thrown? Exception (dependencies/typed-adjacency nodes (update edges 0 dissoc :source) {})))
    (is (= :prerequisite-not-accepted
           (get-in (dependencies/advisory-frontier (assoc-in nodes ["producer" :state] :unassessed)
                                                   edges {:needed true}) ["consumer" :state])))))

(deftest unassessed-source-inspections-preserve-pins-and-expose-live-drift
  (let [old "inspected source" digest (dependencies/sha256 old)
        record {:path "fixture.clj" :sha256 digest :source-commit "original"
                :current-applicability :unassessed}
        same (dependencies/evidence-source-state record (constantly old))
        changed (dependencies/evidence-source-state record (constantly "new source"))
        missing (dependencies/evidence-source-state record (fn [_] (throw (java.io.FileNotFoundException.))))]
    (is (= :same-bytes-as-inspection (:state same)))
    (is (= :changed-since-inspection (:state changed)))
    (is (= :absent-from-current-checkout (:state missing)))
    (is (= digest (:inspected-sha256 changed)))
    (is (= "original" (:inspected-commit changed)))
    (is (= :unassessed (:current-applicability changed)))
    (is (thrown? Exception (dependencies/evidence-source-state
                            (assoc record :current-applicability :current) (constantly "new source"))))))

(deftest unchanged-captured-spans-follow-layout-only-ledger-edits
  (let [raw "captured task criterion"
        before (str "old preamble\n" raw "\nold suffix")
        locator {:path "fixture.md" :start-byte 13 :end-byte (+ 13 (count raw))
                 :sha256 (dependencies/sha256 raw) :raw raw}
        moved (str "updated preamble with extra navigation text\n" raw "\nold suffix")
        changed (str "updated preamble\nmutated task criterion\nold suffix")
        duplicated (str "prefix\n" raw "\ncopy\n" raw "\nsuffix")]
    (is (nil? (dependencies/source-bound! locator (constantly before))))
    (is (thrown? Exception (dependencies/source-bound! locator (constantly moved))))
    (is (nil? (dependencies/current-task-body! locator (constantly before))))
    (is (nil? (dependencies/current-task-body! locator (constantly moved))))
    (is (thrown? Exception (dependencies/source-bound! locator (constantly changed))))
    (is (thrown? Exception (dependencies/current-task-body! locator (constantly changed))))
    (is (thrown? Exception (dependencies/current-task-body! locator (constantly duplicated))))))

(deftest normalized-source-snapshot-tampering-rejects
  (let [source (edn/read-string (slurp dependencies/sh-crosswalk-path))
        reference (get-in source [:requirements 0 :acceptance-source :source-snapshot-ref])
        forged (assoc source :source-snapshots
                      (assoc (:source-snapshots source) reference "forged acceptance"))
        missing (update source :source-snapshots dissoc reference)]
    (is (string? reference))
    (is (thrown? Exception (dependencies/task-sh-view forged slurp)))
    (is (thrown? Exception (dependencies/task-sh-view missing slurp)))))

(deftest navigation-roadmap-drift-does-not-rebind-sh-history
  (let [source (edn/read-string (slurp dependencies/sh-crosswalk-path))
        roadmap "docs/component-roadmaps/runtime.md"
        task "specs/runtime-workflow/tasks.md"
        roadmap-read (fn [path]
                       (if (= path roadmap)
                         (str (slurp path) "\nNavigation-only edit.\n")
                         (slurp path)))
        task-layout-read (fn [path]
                           (if (= path task)
                             (str/replace (slurp path)
                                          "<!-- runtime-implementation-tasks:start -->"
                                          "Navigation-only preamble edit.\n<!-- runtime-implementation-tasks:start -->")
                             (slurp path)))
        task-read (fn [path]
                    (if (= path task)
                      (str/replace (slurp path)
                                   "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
                                   "runtime/fabricated")
                      (slurp path)))]
    (is (= :structural-only
           (:authority (dependencies/task-sh-view source roadmap-read))))
    (is (= :structural-only
           (:authority (dependencies/task-sh-view source task-layout-read))))
    ;; Frozen task-authority spans still reject mutation; only navigation views
    ;; are exempt from the historical whole-file digest check.
    (is (thrown? Exception (dependencies/task-sh-view source task-read)))))

(deftest historical-sh-routing-drift-stays-unassessed
  (let [source (edn/read-string (slurp dependencies/sh-crosswalk-path))
        path "docs/self-hosting-slice-ownership.edn"
        ;; The live ownership manifest is intentionally newer than the
        ;; crosswalk's historical pin. Keep that pin untouched and use an
        ;; immutable in-memory observation fixture for matching/mismatching
        ;; reads so this test exercises state classification, not receipt
        ;; refresh or live-file history.
        snapshot-bytes (slurp path)
        observation-source (assoc-in source [:source-digests path]
                                     (dependencies/sha256 snapshot-bytes))
        read-snapshot (fn [read-path]
                        (if (= read-path path)
                          snapshot-bytes
                          (slurp read-path)))
        read-drifted (fn [read-path]
                       (if (= read-path path)
                         (str snapshot-bytes "\n")
                         (slurp read-path)))
        same (dependencies/task-sh-view observation-source read-snapshot)
        drifted (dependencies/task-sh-view observation-source read-drifted)]
    (is (= :same-bytes-as-inspection
           (get-in same [:historical-source-applicability path :state])))
    (is (= :changed-since-inspection
           (get-in drifted [:historical-source-applicability path :state])))
    (is (= :unassessed
           (get-in drifted [:historical-source-applicability path :current-applicability])))
    (is (= :structural-only (:authority drifted)))))

(deftest complete-sh-task-view-conserves-sources-and-refuses-credit-forgeries
  (let [source (edn/read-string (slurp dependencies/sh-crosswalk-path))
        view (dependencies/task-sh-view source slurp)
        ids (set (map :id (:requirements source)))
        sh (dependencies/milestone-graph (slurp dependencies/milestone-path))]
    (is (= 180 (count (:incidence view))))
    (is (= 18 (count (:sh07-sequence source))))
    (is (empty? (:cycles view)))
    (is (every? #(or (seq (get (:incidence view) %)) (some #{%} (:coverage-gaps view))) ids))
    (is (= [] (:coverage-gaps view)))
    (is (= (= :accepted-mapping-only (get-in source [:mapping-review :status])) (:mapping-ready? view)))
    (doseq [[milestone deps] sh r (filter #(= milestone (:milestone %)) (:requirements source)) p deps]
      (is (contains? (get-in view [:adjacency (:id r)]) p)))
    (is (= #{"SH07-C01"} (get-in view [:adjacency "SH07-C02"])))
    (is (= 17 (count (get-in view [:adjacency "SH07-C18"]))))
    (is (contains? (get-in view [:adjacency "SH-03"]) "uniform-gate:SH-03"))
    (is (not (contains? (get-in view [:adjacency "SH-02"]) "uniform-gate:SH-02")))
    (is (= :eligible-for-declared-facet (get-in view [:frontier "SH07-C01" :state])))
    (is (= :prerequisite-not-accepted (get-in view [:frontier "SH-29" :state])))
    (is (every? #(= :unassessed (:state %)) (map (:nodes view) ids)))
    (doseq [changed [(update source :requirements pop)
                     (update source :requirements conj (first (:requirements source)))
                     (assoc-in source [:requirements 0 :task-references 0 :id] "fabricated-task")
                     (assoc-in source [:requirements 0 :acceptance-source :raw] "forged acceptance")
                     (assoc-in source [:requirements 6 :completion-prerequisites] [])
                     (assoc-in source [:requirements 0 :context-source :raw] "forged stopping rule")
                     (assoc-in source [:requirements 18 :shared-completion-rule] nil)
                     (assoc-in source [:task-authorities "core" 0 :checked] true)
                     (assoc-in source [:sh07-sequence 1 :dependency-declaration] "none")
                     (update source :sh07-sequence pop)]]
      (is (thrown? Exception (dependencies/task-sh-view changed slurp))))
    (is (thrown? Exception
                 (dependencies/task-sh-view source
                   (fn [path] (if (= path "specs/core-workflow/tasks.md")
                                (str/replace (slurp path) "core1-lower-core-forms" "unknown-task")
                                (slurp path))))))))

(deftest sh07-dependency-ranges-preserve-original-sequence
  (is (= ["SH07-C02" "SH07-C03" "SH07-C04"] (dependencies/sh07-prerequisites "C02-C04")))
  (is (= ["SH07-C03" "SH07-C04"] (dependencies/sh07-prerequisites "C03, C04")))
  (is (= [] (dependencies/sh07-prerequisites "none")))
  (doseq [bad ["C00" "C19" "C09-C02" "C01x" "C01 C02" "C01," "all previous"]]
    (is (thrown? Exception (dependencies/sh07-prerequisites bad)))))

(let [result (run-tests 'acceptance-dependencies-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
