(ns acceptance-dependencies-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [requirement-sources :as requirement-sources]))

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
    ;; Includes the resumed evolution-workflow package; still navigation only.
    (is (= 32 (count (get-in result [:retained-spec-kit :packages]))))
    (is (= 128 (count (get-in result [:retained-spec-kit :artifact-source-digests]))))
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
    (is (thrown? Exception (dependencies/task-sh-view source task-read)))
    (let [platform-rows (vec (get-in source [:task-authorities "platform"]))
          snapshots (:source-snapshots source)
          transition dependencies/platform-task-source-transition
          platform-path (:path transition)
          current (slurp platform-path)
          layout-read (fn [path]
                        (if (= path platform-path)
                          (str/replace current
                                       "<!-- platform-implementation-tasks:start -->"
                                       "Navigation-only preamble edit.\n<!-- platform-implementation-tasks:start -->")
                          (slurp path)))
          changed-read (fn [path]
                         (if (= path platform-path)
                           (str/replace current
                                        "Adopt clock/reset/HDL target contract"
                                        "Adopt an unauthenticated target contract")
                           (slurp path)))
          duplicate-row (dependencies/platform-task-row current "hardware")
          replay-read (fn [path]
                        (if (= path platform-path)
                          (str/replace current
                                       "<!-- platform-implementation-tasks:end -->"
                                       (str duplicate-row
                                            "\n\n<!-- platform-implementation-tasks:end -->"))
                          (slurp path)))]
      ;; The authenticated row pair survives a navigation-only preamble edit.
      (is (= :git-bytes-verified
             (:snapshot-provenance
              (dependencies/platform-task-transition-view
               transition platform-rows snapshots layout-read))))
      ;; A changed criterion is not a navigation edit and cannot receive SH
      ;; routing credit through the transition.
      (is (thrown? Exception
                   (dependencies/platform-task-transition-view
                    transition platform-rows snapshots changed-read)))
      ;; Baseline/applying identities are a fixed pair, not interchangeable
      ;; labels that can be replayed with a new body.
      (is (thrown? Exception
                   (dependencies/platform-task-transition-view
                    (assoc transition :applying-blob (:baseline-blob transition))
                    platform-rows snapshots slurp)))
      (is (thrown? Exception
                   (dependencies/platform-task-transition-view
                    (assoc transition :baseline-ref (:applying-ref transition))
                    platform-rows snapshots slurp)))
      (is (thrown? Exception
                   (dependencies/platform-task-transition-view
                    transition platform-rows snapshots replay-read))))
    ;; The applying-row exception does not waive the historical locator. Every
    ;; locator component remains bound to the pinned baseline Git source before
    ;; the current transition is considered.
    (doseq [mutated
            [(assoc-in source [:task-authorities "platform" 0 :file-sha256]
                       "0000000000000000000000000000000000000000000000000000000000000000")
             (assoc-in source [:task-authorities "platform" 0 :source-commit]
                       "0000000000000000000000000000000000000000")
             (assoc-in source [:task-authorities "platform" 0 :source :start-byte]
                       (inc (get-in source [:task-authorities "platform" 0 :source :start-byte])))
             (assoc-in source [:task-authorities "platform" 0 :source :end-byte]
                       (dec (get-in source [:task-authorities "platform" 0 :source :end-byte])))
             (assoc-in source [:task-authorities "platform" 0 :source :start-line]
                       (inc (get-in source [:task-authorities "platform" 0 :source :start-line])))
             (assoc-in source [:task-authorities "platform" 0 :source :end-line]
                       (inc (get-in source [:task-authorities "platform" 0 :source :end-line])))
             (assoc-in source [:task-authorities "platform" 0 :source :sha256]
                       "0000000000000000000000000000000000000000000000000000000000000000")
             (assoc-in source [:task-authorities "platform" 0 :source :source-snapshot-ref]
                       "0000000000000000000000000000000000000000000000000000000000000000")
             (assoc-in source [:task-authorities "platform" 0 :source :path]
                       "specs/core-workflow/tasks.md")]]
      (is (thrown? Exception (dependencies/task-sh-view mutated slurp))))))

(deftest current-source-transition-authenticates-file-and-span-identities
  (doseq [contract requirement-sources/current-source-transition-contracts]
    (let [read-ref (fn [ref path]
                     (requirement-sources/git-source "." ref path dependencies/require!))
          validate (fn [transition]
                     (requirement-sources/validate-current-source-transition!
                      transition contract read-ref slurp dependencies/sha256 dependencies/require!))]
      (is (= :current-source-interface-only (:authority (validate contract))))
      (doseq [mutated [(assoc contract :role :migration-authority)
                       (assoc contract :path "specs/core-workflow/tasks.md")
                       (assoc-in contract [:changed-spans 0 :after :sha256] "forged")
                       (assoc-in contract [:changed-spans 0 :before :start-byte]
                                 (inc (get-in contract [:changed-spans 0 :before :start-byte])))]]
        (is (thrown? Exception (validate mutated)))))))

(deftest acceptance-owner-rejects-current-transition-population-drift
  (let [contracts requirement-sources/current-source-transition-contracts
        view (dependencies/current-source-transition-view contracts slurp)]
    (is (= requirement-sources/current-source-transition-kind (:kind view)))
    (is (= (set (map :path contracts)) (set (:paths view))))
    (doseq [mutated [(vec (butlast contracts))
                     (assoc-in contracts [0 :role] :migration-authority)
                     (assoc-in contracts [1 :path] "specs/core-workflow/tasks.md")
                     (conj contracts (first contracts))]]
      (is (thrown? Exception
                   (dependencies/current-source-transition-view mutated slurp))))))

(deftest current-source-transition-history-preserves-auth-predecessor
  (let [history (:current-source-transition-history
                 (edn/read-string
                  (slurp "specs/sh-family-crosswalk/implementation-readiness-current-source-version.edn")))
        read-ref (fn [ref path]
                   (requirement-sources/git-source "." ref path dependencies/require!))
        validate (fn [value]
                   (requirement-sources/validate-current-source-transition-history!
                    value read-ref dependencies/sha256 dependencies/require!))]
    (is (= :authenticated-current-source-transition-history-v1
           (:kind (validate history))))
    (doseq [mutated [(assoc history :authority :migration-authority)
                     (assoc history :record-count 22)
                     (assoc-in history [:predecessor :tree]
                               "0000000000000000000000000000000000000000")
                     (assoc-in history [:predecessor :blob]
                               "0000000000000000000000000000000000000000")
                     (assoc-in history [:superseded-transitions 0 :path]
                               "specs/core-workflow/tasks.md")
                     (assoc-in history [:predecessor-input-fingerprints 0]
                               "forged")]]
      (is (thrown? Exception (validate mutated))))))

(deftest stdlib-task-source-transition-authenticates-current-criteria
  (let [source (edn/read-string (slurp dependencies/sh-crosswalk-path))
        rows (vec (get-in source [:task-authorities "stdlib"]))
        snapshots (:source-snapshots source)
        transition dependencies/stdlib-task-source-transition
        path (:path transition)
        current (slurp path)
        layout-read (fn [read-path]
                      (if (= read-path path)
                        (str/replace current
                                     "<!-- stdlib-implementation-tasks:start -->"
                                     "Navigation-only preamble edit.\n<!-- stdlib-implementation-tasks:start -->")
                        (slurp read-path)))
        changed-read (fn [read-path]
                       (if (= read-path path)
                         (str/replace current
                                      "compiled module manifests agree"
                                      "unauthenticated module manifests agree")
                         (slurp read-path)))
        duplicate-row (dependencies/stdlib-task-row current (:id (first rows)))
        duplicate-read (fn [read-path]
                         (if (= read-path path)
                           (str/replace current
                                        "<!-- stdlib-implementation-tasks:end -->"
                                        (str duplicate-row
                                             "\n\n<!-- stdlib-implementation-tasks:end -->"))
                           (slurp read-path)))]
    (is (= :git-bytes-verified
           (:snapshot-provenance
            (dependencies/stdlib-task-transition-view
             transition rows snapshots layout-read))))
    (is (thrown? Exception
                 (dependencies/stdlib-task-transition-view
                  transition rows snapshots changed-read)))
    (is (thrown? Exception
                 (dependencies/stdlib-task-transition-view
                  (assoc transition :applying-blob (:baseline-blob transition))
                  rows snapshots slurp)))
    (is (thrown? Exception
                 (dependencies/stdlib-task-transition-view
                  transition rows snapshots duplicate-read)))
    (doseq [mutated
            [(assoc-in source [:task-authorities "stdlib" 0 :file-sha256]
                       "0000000000000000000000000000000000000000000000000000000000000000")
             (assoc-in source [:task-authorities "stdlib" 0 :source :start-byte]
                       (inc (get-in source [:task-authorities "stdlib" 0 :source :start-byte])))
             (assoc-in source [:task-authorities "stdlib" 0 :source :path]
                       "specs/core-workflow/tasks.md")]]
      (is (thrown? Exception (dependencies/task-sh-view mutated slurp))))))

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
                     (assoc-in source [:task-authorities "core" 0 :source :path]
                               "specs/platform-workflow/tasks.md")
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

(deftest future-declarations-consume-literal-known-ids
  (let [text (str "<!-- future-composition-dependencies:start -->\n"
                  "| Consumer | Start results | Composition results | Selected output contract |\n"
                  "| --- | --- | --- | --- |\n"
                  "| `consumer` | none | `producer` | Actual selected output. |\n"
                  "<!-- future-composition-dependencies:end -->")
        parse #(dependencies/future-dependency-routes % #{"consumer" "producer"} #{"consumer"})]
    (is (= [{:consumer "consumer" :start-results [] :composition-results ["producer"]
             :selected-output-contract "Actual selected output."
             :source {:path dependencies/future-dependency-path :sha256 (dependencies/sha256 text)
                      :declaration "| `consumer` | none | `producer` | Actual selected output. |"}}]
           (parse text)))
    (doseq [[from to] [["`producer`" "`missing`"] ["`producer`" "producer"]
                       ["`producer`" "`producer`junk"] ["`producer`" "`producer`,"]
                       ["`producer`" "`producer`, `producer`"]
                       ["`producer`" "none, `producer`"] ["`producer`" "`producer`; `consumer`"]
                       ["| `consumer` |" "| `producer` |"]
                       ["| none |" "| `producer` |"]
                       ["Actual selected output." ""]
                       ["Actual selected output." "Split | cell"]
                       ["dependencies:end" "dependencies:missing"]]]
      (is (thrown? Exception (parse (str/replace text from to))) to))
    (is (thrown? Exception (parse (str/replace text "<!-- future-composition-dependencies:end -->"
                                              (str "| `consumer` | none | none | Duplicate. |\n"
                                                   "<!-- future-composition-dependencies:end -->")))))))

(deftest future-task-projection-retains-gaps-and-selected-result-boundaries
  (let [source (edn/read-string (slurp dependencies/sh-crosswalk-path))
        current (into {} (for [rows (vals (:task-authorities source)) row rows] [(:id row) row]))
        future (:future-task-authorities source)
        snapshots (:source-snapshots source)
        result (dependencies/future-task-view future current snapshots slurp)
        review (:dependency-review result)
        edges (:typed-edges result)
        nodes (:nodes result)
        current-nodes (into {} (for [[id _] current facet ["interface" "implementation" "composition"]]
                                 [(str "task:" id "/" facet)
                                  {:state (if (= "interface" facet) :accepted :unassessed)}]))
        frontier (dependencies/advisory-frontier (merge current-nodes nodes) edges {})
        hardware "platform-product/implement-hardware"
        adoption "platform-product/adopt-physical-boundary"]
    (is (= 149 (count current)))
    (is (= 40 (:task-count review)))
    (is (= 40 (count (:declared-consumers review))))
    (is (= 118 (count (:declared-parent-consumers review))))
    (is (= dependencies/selected-parent-consumers (set (:required-parent-consumers review))))
    (is (every? dependencies/selected-parent-consumers
                ["stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations"
                 "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters"
                 "stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests"]))
    (let [text (slurp dependencies/future-dependency-path)
          removed (clojure.string/replace text #"(?m)^\| `domain-ir-boundaries` \|.*\n" "")
          altered (dependencies/future-task-view future current snapshots
                    #(if (= dependencies/future-dependency-path %) removed (slurp %)))
          route (first (filter #(= "domain-ir-boundaries" (:consumer %)) (:routes review)))]
      (is (= ["domain-ir-boundaries"] (get-in altered [:dependency-review :unresolved-consumers])))
      (is (empty? (:start-results route)))
      (is (= 27 (count (:composition-results route))))
      (doseq [producer (:composition-results route)]
        (is (some #(= ["task:domain-ir-boundaries/implementation" (str "task:" producer "/interface") :declared-producer-interface]
                      [(:from %) (:to %) (:kind %)]) edges))
        (is (some #(= ["task:domain-ir-boundaries/composition" (str "result:domain-ir-boundaries:" producer) :actual-selected-producer-result]
                      [(:from %) (:to %) (:kind %)]) edges)))
      (is (not-any? #(and (= "task:domain-ir-boundaries/implementation" (:from %))
                         (clojure.string/starts-with? (:to %) "result:")) edges)))
    (let [c9 (set (filter #(clojure.string/starts-with? % "legality/") dependencies/selected-parent-consumers))
          text (slurp dependencies/future-dependency-path)
          removed (clojure.string/replace text #"(?m)^\| `legality/[^`]+` \|.*\n" "")
          altered (dependencies/future-task-view future current snapshots
                    #(if (= dependencies/future-dependency-path %) removed (slurp %)))
          routes (filter #(c9 (:consumer %)) (:routes review))]
      (is (= 5 (count c9) (count routes)))
      (is (= c9 (set (get-in altered [:dependency-review :unresolved-consumers]))))
      (is (= [29 31 32 43 50] (sort (map (comp count :composition-results) routes))))
      (doseq [{:keys [consumer start-results composition-results]} routes]
        (is (empty? start-results))
        (doseq [producer composition-results]
          (is (some #(= [(str "task:" consumer "/implementation") (str "task:" producer "/interface") :declared-producer-interface]
                        [(:from %) (:to %) (:kind %)]) edges))
          (is (some #(= [(str "task:" consumer "/composition") (str "result:" consumer ":" producer) :actual-selected-producer-result]
                        [(:from %) (:to %) (:kind %)]) edges)))
        (is (not-any? #(and (= (str "task:" consumer "/implementation") (:from %))
                           (clojure.string/starts-with? (:to %) "result:")) edges))))
    (let [id "evolution/security-review"
          text (slurp dependencies/future-dependency-path)
          removed (clojure.string/replace text #"(?m)^\| `evolution/security-review` \|.*\n" "")
          altered (dependencies/future-task-view future current snapshots
                    #(if (= dependencies/future-dependency-path %) removed (slurp %)))
          route (first (filter #(= id (:consumer %)) (:routes review)))]
      (is (= [id] (get-in altered [:dependency-review :unresolved-consumers])))
      (is (empty? (:start-results route)))
      (is (= 7 (count (:composition-results route))))
      (doseq [producer (:composition-results route)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" producer "/interface") :declared-producer-interface]
                      [(:from %) (:to %) (:kind %)]) edges))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" producer) :actual-selected-producer-result]
                      [(:from %) (:to %) (:kind %)]) edges)))
      (is (not-any? #(and (= (str "task:" id "/implementation") (:from %))
                         (str/starts-with? (:to %) "result:")) edges)))
    (is (empty? (:unresolved-consumers review)))
    (is (= :pending (:future-sh-and-full-language-joins review)))
    (is (= :unassessed (:implementation-acceptance review)))
    (is (= [138] (mapv count (:declared-output-cycles review))))
    ;; Fourteen original result routes join the two coarse feedback groups.
    ;; Their older memberships survive; executable typed edges stay acyclic.
    (let [text (requirement-sources/git-source "." "f989a01024d4ef0f595b7bf685e0fbf17648469b"
                                               dependencies/future-dependency-path dependencies/require!)
          prior (dependencies/future-task-view future current snapshots
                  #(if (= dependencies/future-dependency-path %) text (slurp %)))
          old-members (set (mapcat identity (get-in prior [:dependency-review :declared-output-cycles])))
          added #{"release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures" "ui" "release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution" "evolution/stabilization-decision" "backend-mlir" "backend-workflow" "backend-llvm" "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters" "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface" "backend-query" "release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures" "release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations" "backend-emission" "release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures" "release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures" "backend-jvm" "auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions" "backend-ai" "release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures" "release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations" "release/remaining-tasks-and-evidence/execute-formal-evidence-checks" "backend-js" "release/remaining-tasks-and-evidence/execute-language-conformance-fixtures" "evolution/library-admission" "stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations" "backend-c" "backend-mobile" "backend-wasm"}]
      (is (= [99 11] (mapv count (get-in prior [:dependency-review :declared-output-cycles]))))
      (is (= (into old-members added) (set (first (:declared-output-cycles review))))))

    (is (every? (set (first (:declared-output-cycles review)))
                ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
                 "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
                 "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
                 "effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"
                 "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
                 "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
                 "ai" "workflow" "backend-conformance" "query" "backend-interface" "pass-evidence"]))
    (is (contains? (set (first (:declared-output-cycles review)))
                   "effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"))
    (is (every? (set (first (:declared-output-cycles review)))
                ["evolution/security-review" "formal" "math/independent-proof-and-certificate-checking"
                 "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
                 "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"]))
    (is (every? (set (first (:declared-output-cycles review)))
                ["schema/checked-consumer-projection" "schema/compatibility-and-migration"
                 "release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler"
                 "release/remaining-tasks-and-evidence/execute-staged-compiler-validation"]))
    (doseq [id (:unresolved-consumers review)]
      (is (= :dependency-review-pending (get-in frontier [(str "task:" id "/composition") :state])) id)
      (is (= :dependency-review-pending (get-in frontier [(str "task:" id "/implementation") :state])) id))
    (is (some #(= [(str "task:" hardware "/implementation") (str "result:" hardware ":" adoption)
                   :adopted-product-contract] [(:from %) (:to %) (:kind %)]) edges))
    (is (some #(= ["task:automation/composition"
                   "result:automation:build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
                   :actual-selected-producer-result] [(:from %) (:to %) (:kind %)]) edges))
    (is (not-any? #(and (= "task:automation/implementation" (:from %))
                       (str/starts-with? (:to %) "result:")) edges))
    (doseq [row (filter :producer (vals nodes))]
      (is (= :unassessed (:state row)))
      (is (:producer-source row)))
    (is (empty? (dependencies/cycles
                  (dependencies/typed-adjacency
                    (merge current-nodes nodes) edges {}))))
    (let [path "specs/platform-workflow/tasks.md" text (slurp path)
          read-changed #(if (= path %) (str/replace text (str "`" hardware "`") "`platform-product/fabricated`") (slurp %))
          row (first (get future "platform-product-implementation-tasks"))
          body (dependencies/source-raw (:source row) snapshots)
          read-duplicated #(if (= path %) (str text "\n" body) (slurp %))]
      (is (thrown? Exception (dependencies/future-task-view future current snapshots read-changed)))
      (is (thrown? Exception (dependencies/future-task-view future current snapshots read-duplicated)))
      (is (thrown? Exception (dependencies/future-task-view
                              (assoc-in future ["platform-product-implementation-tasks" 0 :component] "domains")
                              current snapshots slurp)))
      (is (thrown? Exception (dependencies/future-task-view
                              (update future "platform-product-implementation-tasks" pop) current snapshots slurp)))
      (is (thrown? Exception (dependencies/future-task-view
                              (assoc-in future ["platform-product-implementation-tasks" 0 :checked] true)
                              current snapshots slurp))))
    (let [doc dependencies/future-dependency-path
          changed (str/replace (slurp doc)
                    "| `platform-product/adopt-hardware-contract` | none | none |"
                    "| `platform-product/adopt-hardware-contract` | none | `platform-product/implement-hardware` |")
          with-backedge (dependencies/future-task-view future current snapshots
                          #(if (= doc %) changed (slurp %)))]
      (is (some #(= ["platform-product/adopt-hardware-contract" "platform-product/implement-hardware"] %)
                (get-in with-backedge [:dependency-review :declared-output-cycles]))))))

(deftest math-output-joins-retain-exact-criterion-spans-and-independent-boundaries
  (let [source (edn/read-string (slurp dependencies/sh-crosswalk-path))
        tasks (into {} (for [rows (concat (vals (:task-authorities source)) (vals (:future-task-authorities source)))
                            row rows] [(:id row) row]))
        snapshots (:source-snapshots source)
        view (dependencies/math-output-view tasks snapshots)
        outputs (into {} (map (juxt :id identity) (:outputs view)))
        rewrite (fn [task-id transform]
                  (let [task (tasks task-id) raw (transform (dependencies/source-raw (:source task) snapshots))]
                    (assoc-in tasks [task-id :source]
                              {:path (:path task) :raw raw :sha256 (dependencies/sha256 raw)})))
        eml "math/eml-normalization-and-search"]
    (is (= 48 (count outputs)))
    (is (= 67 (reduce + (map count (vals (:adjacency view))))))
    (is (= 28 (count (:cross-task-inputs view))))
    (is (empty? (:cycles view)))
    (is (= :unassessed (:implementation-acceptance view)))
    (doseq [row (:outputs view)
            :let [locator (:source row)
                  task-bytes (.getBytes (dependencies/source-raw (:task-source locator) snapshots) "UTF-8")
                  criterion (String. (java.util.Arrays/copyOfRange task-bytes
                                      (int (:task-relative-byte-start locator))
                                      (int (:task-relative-byte-end locator))) "UTF-8")]]
      (is (= (:sha256 locator) (dependencies/sha256 criterion)) (:id row))
      (is (str/starts-with? criterion (str "Criterion `" (:id row) "` (" (:canonical-document row) "; Open).")))
      (is (str/includes? criterion (str "Outputs: " (:output-declaration row))))
      (is (str/includes? criterion (str "Dependencies: " (:dependency-declaration row) ". " (:external-input-contract row))))
      (is (= :unassessed (:state row)))
      (is (tasks (:task-id row))))
    (is (= "math" (:task-id (outputs "math/efir-verification"))))
    (is (= "math/independent-proof-and-certificate-checking"
           (:task-id (outputs "math/eml-independent-trace-replay"))))
    (is (= #{"math/eml-trace-production"} (get-in view [:adjacency "math/eml-independent-trace-replay"])))
    (is (= #{"math/eml-construction"} (get-in view [:adjacency "math/eml-trace-production"])))
    (is (= #{"math/semantic-equality-proof-gate" "math/efir-verification"}
           (get-in view [:adjacency "math/eml-candidate-promotion"])))
    (doseq [[from to] [["Dependencies: math/efir-verification." "Dependencies: math/missing-output."]
                       ["Dependencies: math/eml-construction, math/eml-search-manifest."
                        "Dependencies: math/eml-construction; math/eml-search-manifest."]
                       ["math/semantic-equality-proof-gate, math/efir-verification."
                        "math/semantic-equality-proof-gate, math/semantic-equality-proof-gate."]
                       ["Criterion `math/eml-trace-production`" "Criterion `math/eml-construction`"]
                       ["(MATH4; Open)." "(MATH99; Open)."]
                       ["Outputs: Optional EML expression/DAG, EFIR node correspondence and semantic context."
                        "Outputs:"]
                       ["Outputs: Normalization trace" "Outputs: Duplicate declaration.\nOutputs: Normalization trace"]
                       ["Dependencies: math/eml-construction. Types/Legality"
                        "Dependencies: math/eml-construction. Types/Legality duplicate.\nDependencies: math/eml-construction. Types/Legality"]]]
      (is (thrown? Exception (dependencies/math-output-view (rewrite eml #(str/replace % from to)) snapshots)) from))
    (let [with-cycle (rewrite eml #(str/replace % "Dependencies: math/eml-construction. Types/Legality"
                                               "Dependencies: math/eml-independent-trace-replay. Types/Legality"))]
      (is (= [["math/eml-independent-trace-replay" "math/eml-trace-production"]]
             (:cycles (dependencies/math-output-view with-cycle snapshots)))))
    (let [current (into {} (for [rows (vals (:task-authorities source)) row rows] [(:id row) row]))
          result (dependencies/future-task-view (:future-task-authorities source) current snapshots slurp)
          node (get-in result [:nodes (str "result:" eml ":math")])]
      (is (= ["math/efir-verification" "math/numeric-mode-resolution" "math/semantic-equality-proof-gate"]
             (:named-math-output-inputs node)))
      (is (= :unassessed (:state node)))
      (is (some #(= [(str "result:" eml ":math") "output:math/efir-verification" :named-selected-producer-output]
                    [(:from %) (:to %) (:kind %)]) (:typed-edges result))))))

(deftest release-source-criteria-and-staged-admission-remain-distinct
  (let [source (edn/read-string (slurp dependencies/sh-crosswalk-path))
        snapshots (:source-snapshots source)
        rows (get-in source [:future-task-authorities "release-testing-proposals"])
        view (dependencies/release-source-view rows snapshots)
        criteria (:criteria view)
        rewrite (fn [index f]
                  (let [raw (f (dependencies/source-raw (:source (rows index)) snapshots))]
                    (assoc-in rows [index :source] {:path "fixture" :raw raw :sha256 (dependencies/sha256 raw)})))]
    (is (= 77 (count criteria) (count (set (map :id criteria)))))
    (is (= 73 (count (filter #(= :test-source-clause (:declaration-kind %)) criteria))))
    (is (= 4 (count (filter #(= :bootstrap-criterion (:declaration-kind %)) criteria))))
    (is (= :unassessed (:implementation-acceptance view)))
    (doseq [row criteria :let [s (:source row)
                              bytes (.getBytes (dependencies/source-raw (:task-source s) snapshots) "UTF-8")
                              body (String. (java.util.Arrays/copyOfRange bytes (int (:task-relative-byte-start s))
                                                                         (int (:task-relative-byte-end s))) "UTF-8")]]
      (is (= (:sha256 s) (dependencies/sha256 body)) (:id row))
      (is (str/starts-with? body (str (if (= :test-source-clause (:declaration-kind row)) "Source " "Bootstrap criterion ")
                                    (:id row) ":")))
      (is (= :unassessed (:state row)))
      (is (= :navigation-not-semantic-entailment (:baseline-authority row))))
    (doseq [[from to] [["testing/test8/human-review" "testing/test99/human-review"]
                       ["testing/test8/metric-release" "testing/test8/human-review"]
                       ["Evidence baseline: [TEST8]" "Evidence baseline: [TEST7]"]
                       ["Concrete acceptance: Execute grant" "Missing acceptance: Execute grant"]]]
      (is (thrown? Exception (dependencies/release-source-view (rewrite 0 #(str/replace % from to)) snapshots)) from))
    (is (thrown? Exception (dependencies/release-source-view (pop rows) snapshots)))
    (is (thrown? Exception
                 (dependencies/release-source-view
                   (rewrite 12 #(str/replace % "Bootstrap criterion bootstrap/native-symbol-units:"
                                              "Bootstrap criterion bootstrap/unknown-native:")) snapshots)))
    (let [current (into {} (for [rs (vals (:task-authorities source)) row rs] [(:id row) row]))
          result (dependencies/future-task-view (:future-task-authorities source) current snapshots slurp)
          staged "release/remaining-tasks-and-evidence/execute-staged-compiler-validation"
          comparison "release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler"
          edges (:typed-edges result)
          pair? (fn [from to kind] (some #(= [from to kind] [(:from %) (:to %) (:kind %)]) edges))
          missing-doc (str/replace (slurp dependencies/future-dependency-path)
                        #"(?m)^\| `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures` \|[^\n]*\n" "")
          missing (dependencies/future-task-view (:future-task-authorities source) current snapshots
                    #(if (= dependencies/future-dependency-path %) missing-doc (slurp %)))]
      (is (pair? (str "task:" staged "/implementation") (str "task:" comparison "/interface") :declared-producer-interface))
      (is (not (pair? (str "task:" staged "/implementation") (str "result:" staged ":" comparison) :adopted-product-contract)))
      (is (pair? (str "task:" staged "/composition") (str "result:" staged ":" comparison) :actual-selected-producer-result))
      (is (pair? (str "task:" comparison "/composition") (str "result:" comparison ":" staged) :actual-selected-producer-result))
      (is (pair? (str "task:" staged "/composition") "source-criterion:testing/test13/stage-conformance"
                 :requires-source-criterion-execution-and-refusals))
      (is (= :unassessed (get-in result [:nodes (str "result:" staged ":" comparison) :state])))
      (is (= ["release/remaining-tasks-and-evidence/execute-language-conformance-fixtures"]
             (get-in missing [:dependency-review :unresolved-consumers])))
      (is (= :pending (get-in missing [:nodes "task:release/remaining-tasks-and-evidence/execute-language-conformance-fixtures/implementation"
                                      :dependency-review]))))))

(deftest finish-line-retains-original-stages-proof-roots-and-first-release-scope
  (let [p (edn/read-string (slurp dependencies/sh-crosswalk-path))
        sources (:finish-line-sources p)
        tasks (into {} (for [rs (concat (vals (:task-authorities p)) (vals (:future-task-authorities p))) r rs] [(:id r) r]))
        v (dependencies/finish-line-view sources tasks (:source-snapshots p) slurp)
        base (merge (into {} (for [id (keys tasks)] [(str "task:" id "/interface") {:state :accepted}]))
                    (into {} (for [n (range 30)] [(format "SH-%02d" n) {:state :unassessed}])) )
        nodes (merge base (:nodes v)) edges (:typed-edges v)
        graph (dependencies/typed-adjacency nodes edges (:condition-values v))
        final (dependencies/reachable graph "FL-P18-T02/acceptance")
        original (dependencies/staged-graph (dependencies/full-language-graph (slurp dependencies/roadmap-path)))
        projected (set (for [e edges :when (and (= :original-roadmap-stage-prerequisite (:kind e))
                                               (str/starts-with? (:from e) "FL-"))] [(:from e) (:to e)]))]
    (is (= 56 (count sources)))
    (is (= 55 (count (get-in v [:summary :routes]))))
    (is (= 54 (count (set (map :consumer (get-in v [:summary :routes]))))))
    (is (= (set (for [[from inputs] original to inputs] [from to])) projected))
    (is (empty? (dependencies/cycles graph)))
    (doseq [id dependencies/full-language-task-ids] (is (final (str id "/acceptance")) id))
    (doseq [n (range 7)] (is (final (format "P18-T%02d/acceptance" n))))
    (is (every? final ["P15-S23" "final-phase-18-completion-gate" "SH-29"]))
    (is (not ((dependencies/reachable graph "SH-29") "FL-P18-T02/acceptance")))
    (is (not ((dependencies/reachable graph "SH-29") "final-phase-18-completion-gate")))
    (is (= :unassessed (get-in nodes ["FL-P00-T00/acceptance" :state])))
    (is (= "checked-at-recorded-scope" (get-in nodes ["FL-P00-T00/acceptance" :recorded-status])))
    (is (= :unassessed (get-in nodes ["P18-T02/acceptance" :state])))
    (is (= "complete" (get-in nodes ["P18-T02/acceptance" :recorded-status])))
    (doseq [row sources]
      (is (nil? (dependencies/source-bound! (:source row) (:source-snapshots p) slurp)) (:id row)))
    (let [conditional (filter #(= :platform-product-adopted (:condition %)) edges)
          disabled (dependencies/typed-adjacency nodes edges {:platform-product-adopted false})
          frontier (dependencies/advisory-frontier nodes edges (:condition-values v))]
      (is (= 22 (count conditional)))
      (is (= :unknown-condition (get-in frontier ["FL-P09-T01/implementation" :state])))
      (doseq [e conditional]
        (is ((graph (:from e)) (:to e)))
        (is (not ((disabled (:from e)) (:to e)))))
      (is (seq (disabled "FL-P09-T01/composition"))))
    (is (thrown? Exception (dependencies/finish-line-view (pop sources) tasks (:source-snapshots p) slurp)))
    (is (thrown? Exception (dependencies/finish-line-view
                            (assoc-in sources [0 :recorded-status] "accepted-now") tasks (:source-snapshots p) slurp)))
    (is (thrown? Exception (dependencies/finish-line-view sources tasks (:source-snapshots p)
                            #(if (= % dependencies/roadmap-path) (str (slurp %) "\nChanged contract.\n") (slurp %)))))
    (let [text (slurp dependencies/future-dependency-path)
          ids (set (keys tasks)) consumers (set (map :consumer (get-in v [:summary :routes])))
          parse #(dependencies/finish-line-routes % ids consumers)]
      (doseq [[from to] [["| `FL-P00-T00` |" "| `FL-P99-T99` |"]
                         ["| always | Existing bounded public-surface" "| invented-condition | Existing bounded public-surface"]
                         ["`FL-P00-T00` | `" "`FL-P00-T00` | `missing-task`, `"]]]
        (is (thrown? Exception (parse (str/replace text from to))) to))
      (is (thrown? Exception (parse (str/replace text #"(?m)^\| `SH-29` \|[^\n]*\n" ""))))
      (is (thrown? Exception (parse (str/replace text "<!-- finish-line-dependencies:end -->"
                                 "| `SH-29` | `pipeline` | always | Duplicate. |\n<!-- finish-line-dependencies:end -->")))))))

(deftest original-fourteen-dependency-rows-have-a-bounded-source-successor
  (let [t requirement-sources/original-dependency-row-successor
        prior (:successor-of t)
        path (:path t)
        read-ref #(requirement-sources/git-source "." %1 %2 dependencies/require!)
        before (read-ref (:baseline-ref t) path)
        after (read-ref (:applying-ref t) path)
        source (edn/read-string (slurp dependencies/sh-crosswalk-path))
        original (read-ref "0f4dbda97c69fc67dad7eb8824702ae44959607b" path)
        original-before (read-ref "895dea1ddbce13b8deae461144e57b8245caefc8" path)
        ids ["evolution/library-admission" "evolution/stabilization-decision" "ui"
             "backend-emission" "backend-c" "backend-llvm" "backend-wasm" "backend-jvm"
             "backend-js" "backend-mlir" "backend-mobile" "backend-query" "backend-workflow" "backend-ai"]
        rows #(vec (for [line (str/split-lines %)
                        :when (some (fn [id] (str/starts-with? line (str "| `" id "` |"))) ids)] line))
        inserted (rows after)
        task-ids (set (for [rs (concat (vals (:task-authorities source)) (vals (:future-task-authorities source))) r rs] (:id r)))
        routes (dependencies/future-dependency-routes
                (str "<!-- future-composition-dependencies:start -->\n| Consumer | Start results | Composition results | Selected output contract |\n| --- | --- | --- | --- |\n" (str/join "\n" inserted) "\n<!-- future-composition-dependencies:end -->")
                task-ids (set ids))
        inverse (fn [text rs] (reduce #(str/replace %1 (str %2 "\n") "") text rs))
        validate #(requirement-sources/validate-current-source-transition!
                   % t read-ref slurp dependencies/sha256 dependencies/require!)]
    (is (= prior (first (filter #(= path (:path %)) requirement-sources/current-source-transition-successor-contracts))))
    (is (= 14 (count inserted) (count (rows original))))
    (is (= inserted (rows original)))
    (is (= (set ids) (set (map :consumer routes))))
    (is (= 361 (reduce + (map (comp count :composition-results) routes))))
    (is (every? (comp empty? :start-results) routes))
    (is (empty? (rows before)))
    (is (= before (inverse after inserted)))
    (is (= original-before (inverse original inserted)))
    (is (= before (read-ref "f989a01024d4ef0f595b7bf685e0fbf17648469b" path)))
    (is (= after (slurp path)))
    (is (every? #(str/includes? % " | none | ") inserted))
    (is (= :current-source-interface-only (:authority (validate t))))
    (is (= :unassessed (:implementation-acceptance (validate t))))
    (is (= t (first (filter #(= path (:path %)) (get-in source [:implementation-readiness-source-version :current-source-transitions])))))
    ;; The old census continues to describe its exact prior Git checkpoint.
    (let [binding (get-in source [:implementation-readiness-source-version :source-successor-binding])
          old-source (edn/read-string (read-ref "f216b540663d7c23ba5af646abdcb153654ad9c9" dependencies/sh-crosswalk-path))]
      (is (= binding (get-in old-source [:implementation-readiness-source-version :source-successor-binding])))
      (is (= (:baseline-ref t) (get-in binding [:composition :commit])))
      (is (not-any? #(= path (:path %)) (:source-census binding)))
      (is (= (:baseline-sha256 t) (:live-sha256 prior))))
    (doseq [forged [(assoc t :applying-ref (:baseline-ref t))
                    (assoc t :applying-tree (:baseline-tree t))
                    (assoc t :applying-blob (:baseline-blob t))
                    (assoc t :live-sha256 (:baseline-sha256 t))
                    (update-in t [:changed-spans 0 :after :end-byte] dec)
                    (assoc t :successor-of (assoc prior :authority :implementation-accepted))
                    (assoc t :authority :implementation-accepted)]]
      (is (thrown? Exception (validate forged))))
    (doseq [current [before (str after "\nUnbound change.\n") (str after (first inserted) "\n")]]
      (is (thrown? Exception
                   (requirement-sources/validate-current-source-transition!
                    t t read-ref #(if (= path %) current (slurp %)) dependencies/sha256 dependencies/require!))))))

(let [result (run-tests 'acceptance-dependencies-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
