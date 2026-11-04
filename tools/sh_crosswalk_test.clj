(ns sh-crosswalk-test
  (:require [clojure.test :refer [deftest is run-tests]] [clojure.string :as str] [clojure.edn :as edn]
            [clojure.set :as set]))
(binding [*command-line-args* nil] (load-file "tools/sh_crosswalk.clj"))
(require '[sh-inventory] '[sh-crosswalk] '[acceptance-dependencies] '[requirement-sources])

(deftest l13-library-source-refinement-is-bounded-and-fails-closed
  (let [view (sh-crosswalk/source-view)
        refinement (get-in view [:implementation-readiness-source-version :library-refinement])
        required-routes #{"stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations"
                          "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters"
                          "stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests"}
        plan (slurp "specs/stdlib-workflow/plan.md")
        tasks (slurp "specs/stdlib-workflow/tasks.md")
        dependencies (slurp acceptance-dependencies/future-dependency-path)
        forged (assoc-in sh-crosswalk/readiness-source-version
                         [:library-refinement :criteria] [])
        forged-census (assoc-in sh-crosswalk/readiness-source-version
                                [:library-refinement :source-census :sha256] "forged")
        forged-witness (assoc-in sh-crosswalk/readiness-source-version
                                 [:library-refinement :criteria 0 :current-witness
                                  :source-span :sha256] "forged")
        forged-owner (assoc-in sh-crosswalk/readiness-source-version
                               [:library-refinement :owner-joins
                                "language/library/compile-contract-examples"
                                :whole-attached-owner-predicate]
                               :accepted)]
    (is (= :complete-L13-library-source-proposal (:kind refinement)))
    (is (= {:criteria 26 :source-facets 125 :occurrence-cases 84
            :section-conjunctions 19 :owner-joins 13}
           (into {}
                 (for [k [:criteria :source-facets :occurrence-cases
                          :section-conjunctions :owner-joins]]
                   [k (count (get refinement k))]))))
    (is (= required-routes (set/intersection required-routes
                                             (set (:changed-task-ids refinement)))))
    (is (= :unassessed (:implementation-acceptance refinement)))
    (is (= :unassessed (:global-source-conjunctions refinement)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= 125 (count (set (map :id (:source-facets refinement))))))
    (is (= 84 (count (set (keys (:occurrence-cases refinement))))))
    (is (= 13 (count (set (keys (:owner-joins refinement))))))
    (doseq [heading ["## Complete library-principles source criteria"
                     "L13 complete public library-contract criterion"
                     "L13 API-classification criterion"
                     "L13 complete compiler/domain library API criterion"
                     "L13 library compatibility and release-assembly criterion"]]
      (is (str/includes? plan heading) heading))
    (doseq [marker ["stdlib-library-source-contracts:start"
                    "stdlib-library-source-classification:start"
                    "stdlib-library-source-meta:start"
                    "stdlib-library-source-release:start"]]
      (is (str/includes? tasks marker) marker))
    (doseq [id required-routes]
      (is (str/includes? dependencies (str "| `" id "` |")) id))
    (doseq [forged [forged forged-census forged-witness forged-owner]]
      (with-redefs [sh-crosswalk/readiness-source-version forged]
        (is (thrown? Exception (sh-crosswalk/source-view)))))))

(deftest current-source-transitions-bind-exact-files-and-spans
  (let [view (sh-crosswalk/source-view)
        transitions (get-in view [:implementation-readiness-source-version
                                  :current-source-transitions])]
    (is (= sh-crosswalk/current-source-transition-paths
           (set (map :path transitions))))
    (is (= (count sh-crosswalk/current-source-transition-contracts)
           (count transitions)))
    (is (= :pending-independent-source-version-review
           (:review-state (first transitions))))
    (is (every? #(= :current-source-interface-proposal (:role %)) transitions))
    (is (every? #(= :current-source-interface-only (:authority %)) transitions))
    (doseq [[label mutation]
            [["wrong role" #(assoc-in % [0 :role] :migration-authority)]
             ["foreign path" #(assoc-in % [1 :path] "specs/core-workflow/tasks.md")]
             ["forged after digest" #(assoc-in % [2 :changed-spans 0 :after :sha256] "forged")]
             ["missing transition" #(vec (butlast %))]
             ["duplicate transition" #(conj % (first %))]]]
      (with-redefs [sh-crosswalk/readiness-source-version
                    (assoc sh-crosswalk/readiness-source-version
                           :current-source-transitions
                           (mutation transitions))]
        (is (thrown? Exception
                     (sh-crosswalk/source-view)) label)))))

(deftest current-source-successor-binding-authenticates-composition-census-and-joins
  (let [bridge sh-crosswalk/readiness-source-version
        binding (:source-successor-binding bridge)
        transitions (:current-source-transitions bridge)
        joins (:current-source-library-io-criterion-joins bridge)]
    (is (= :authenticated-source-successor-binding-v1 (:kind binding)))
    (is (= "cc8a79074fc8eeeb65d72333e5d64542b201f0d4"
           (get-in binding [:composition :commit])))
    (is (= "72d15504280f60dd3056bf59cb58293ed734928f"
           (get-in binding [:composition :tree])))
    (is (= 28 (get-in binding [:composition :current-transition-count])))
    (is (= 47 (get-in binding [:composition :source-census-count])))
    (is (= 9 (count (:reviewed-source-successors binding))))
    (is (= 47 (count (:source-census binding))))
    (is (= 31 (count joins)))
    (is (= 23 (count (filter #(= :new-current-source-criterion (:relation %)) joins))))
    (doseq [[label candidate]
            [["forged successor composition tree"
              (assoc-in bridge [:source-successor-binding :composition :tree] "forged")]
             ["duplicate successor census path"
              (update-in bridge [:source-successor-binding :source-census]
                         #(conj % (first %)))]
             ["forged successor join census"
              (assoc-in bridge [:source-successor-binding :source-join-census] [])]]]
      (with-redefs [sh-crosswalk/readiness-source-version candidate]
        (is (thrown? Exception
                     (sh-crosswalk/validate-current-source-transitions! transitions))
            label)))))

(deftest current-main-library-io-joins-bind-exact-criteria-and-frame-history
  (let [bridge sh-crosswalk/readiness-source-version
        view (sh-crosswalk/source-view)
        joins (get-in view [:implementation-readiness-source-version
                            :library-io-criterion-joins])
        frame (get-in view [:implementation-readiness-source-version
                            :current-source-frame-history])]
    (is (= 31 (count joins)))
    (is (= 23 (count (filter #(= :new-current-source-criterion (:relation %)) joins))))
    (is (= requirement-sources/std7-std9-reused-criterion-ids
           (set (for [{:keys [id reused-predicate?]} joins :when reused-predicate?] id))))
    (is (= :authenticated-current-source-frame-history-v1 (:kind frame)))
    (is (= 24 (get-in frame [:predecessor :current-source-transition-count])))
    (is (= 23 (get-in frame [:predecessor :current-source-history-count])))
    (is (= 2185 (get-in frame [:predecessor :current-source-path-count])))
    (is (= 72 (get-in frame [:predecessor :applying-pin-count])))
    (is (= 35 (get-in frame [:predecessor :source-digest-count])))
    (doseq [[label mutation]
            [["missing criterion" #(vec (butlast %))]
             ["duplicate criterion" #(conj % (first %))]
             ["forged span" #(assoc-in % [0 :source-span :sha256] "forged")]
             ["foreign owner" #(assoc-in % [1 :owner] "foreign/task")]
             ["forged frame blob" #(assoc-in % [:predecessor :blob] "forged")]]]
      (let [candidate (if (= label "forged frame blob")
                        (assoc bridge :current-source-frame-history
                               (mutation (get-in bridge [:current-source-frame-history])))
                        (assoc bridge :library-io-criterion-joins
                               (mutation (get-in bridge [:library-io-criterion-joins]))))]
        (with-redefs [sh-crosswalk/readiness-source-version candidate]
          (is (thrown? Exception (sh-crosswalk/source-view)) label))))))

(deftest complete-pattern-source-interface-restores-owner-slices-and-rejects-retired-accounting
  (let [read (fn [path] (slurp path))
        core-plan (read "specs/core-workflow/plan.md")
        core-tasks (read "specs/core-workflow/tasks.md")
        core-spec (read "specs/core-workflow/spec.md")
        types-plan (read "specs/types-workflow/plan.md")
        types-tasks (read "specs/types-workflow/tasks.md")
        owner-plans ["specs/auth-workflow/plan.md"
                     "specs/backends-workflow/plan.md"
                     "specs/build-workflow/plan.md"
                     "specs/compiler-api-workflow/plan.md"
                     "specs/diag-workflow/plan.md"
                     "specs/domains-workflow/plan.md"
                     "specs/effects-workflow/plan.md"
                     "specs/evolution-workflow/plan.md"
                     "specs/expand-workflow/plan.md"
                     "specs/legality-workflow/plan.md"
                     "specs/lower-workflow/plan.md"
                     "specs/mir-workflow/plan.md"
                     "specs/opt-workflow/plan.md"
                     "specs/release-workflow/plan.md"
                     "specs/resolve-workflow/plan.md"
                     "specs/runtime-workflow/plan.md"
                     "specs/stdlib-workflow/plan.md"
                     "specs/syntax-workflow/plan.md"
                     "specs/tools-workflow/plan.md"]
        owner-texts (map read owner-plans)
        pattern-section (fn [text heading]
                          (let [start (.indexOf text heading)
                                rest (if (neg? start) "" (subs text start))
                                end (.indexOf rest "\n## " 1)]
                            (if (neg? end) rest (subs rest 0 end))))
        core-pattern (pattern-section core-plan "## Complete pattern-matching source criteria")
        evolution-pattern (pattern-section (read "specs/evolution-workflow/plan.md")
                                           "## Complete pattern-matching source interface")
        legality-pattern (pattern-section (read "specs/legality-workflow/plan.md")
                                          "## Complete pattern-matching source interface")
        owner-ids ["resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions"
                   "types/current-evidence-and-remaining-work/types1-solve-core-constraints"
                   "core/current-evidence-and-remaining-work/core1-lower-core-forms"
                   "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
                   "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
                   "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
                   "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
                   "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
                   "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
                   "types/current-evidence-and-remaining-work/types2-check-declared-abstractions"
                   "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
                   "evolution/change-review"]
        rows (into {} (map (juxt :id identity)
                           (mapcat val (:task-authorities (sh-crosswalk/source-view)))))]
    (doseq [phrase ["## Complete pattern-matching source criteria"
                    "L7 pattern typing and narrowing criterion"
                    "L7 closed-match coverage criterion"
                    "L7 Boolean guard criterion"
                    "L7 trusted-shape boundary criterion"]]
      (is (str/includes? (str core-plan "\n" types-plan "\n" types-tasks) phrase) phrase))
    (doseq [marker ["core-pattern-source-selection:start"
                    "core-pattern-source-tree:start"
                    "core-pattern-source-artifacts:start"]]
      (is (str/includes? core-tasks marker) marker))
    (is (str/includes? core-spec "complete pattern-matching source criteria"))
    (is (str/includes? types-plan "Complete pattern-matching source interface"))
    (is (str/includes? types-tasks "types-pattern-source-narrowing:start"))
    (is (str/includes? types-tasks "types-pattern-source-coverage:start"))
    (is (str/includes? types-tasks "types-pattern-source-guard:start"))
    (is (str/includes? types-tasks "types-pattern-source-trusted:start"))
    (is (str/includes? types-tasks "types-source-schema-static:start"))
    (is (= 19 (count (filter #(str/includes? % "Complete pattern-matching source interface") owner-texts))))
    (doseq [text [core-pattern evolution-pattern legality-pattern]]
      (is (not (re-find #"977-byte|check-and-bind allocation" text))))
    (doseq [id owner-ids]
      (is (contains? rows id) id))))

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

(deftest current-crosswalk-stores-each-source-payload-once
  (let [source (sh-crosswalk/source-view)
        normalized (sh-crosswalk/normalize-source-payloads source)
        raw-spans (filter #(some (fn [entry] (= :raw (key entry))) %)
                          (filter map? (tree-seq coll? seq source)))
        seeded-snapshots (vals (:source-snapshots source))
        raw-payloads (concat (map :raw raw-spans) seeded-snapshots)
        refs (filter #(some (fn [entry] (= :source-snapshot-ref (key entry))) %)
                     (filter map? (tree-seq coll? seq (dissoc normalized :source-snapshots))))
        snapshots (:source-snapshots normalized)
        recovered (map #(get snapshots (:source-snapshot-ref %)) refs)]
    (is (seq raw-spans))
    (is (= (count (distinct raw-payloads)) (count snapshots)))
    (is (= (sort raw-payloads) (sort recovered)))
    (is (every? #(= (:source-snapshot-ref %)
                   (sh-inventory/sha256 (get snapshots (:source-snapshot-ref %)))) refs))
    (is (empty? (filter #(some (fn [entry] (= :raw (key entry))) %)
                       (filter map? (tree-seq coll? seq (dissoc normalized :source-snapshots))))))))

(deftest source-snapshot-integrity-fails-closed
  (let [source (sh-crosswalk/normalize-source-payloads (sh-crosswalk/source-view))
        reference (get-in source [:requirements 0 :acceptance-source :source-snapshot-ref])
        forged (assoc source :source-snapshots
                      (assoc (:source-snapshots source) reference "forged acceptance"))
        missing (update source :source-snapshots dissoc reference)]
    (is (thrown? Exception (sh-crosswalk/normalize-source-payloads
                            (assoc-in (sh-crosswalk/source-view)
                                      [:requirements 0 :acceptance-source :sha256] "forged"))))
    (is (thrown? Exception (sh-crosswalk/normalize-source-payloads
                            (assoc-in (sh-crosswalk/source-view)
                                      [:requirements 0 :acceptance-source :raw] "forged"))))
    (is (thrown? Exception (acceptance-dependencies/task-sh-view forged slurp)))
    (is (thrown? Exception (acceptance-dependencies/task-sh-view missing slurp)))))

(deftest literal-task-routing-does-not-depend-on-live-ledger-order
  (let [rows (sh-crosswalk/task-records "core")
        expected (sh-crosswalk/task-ref {"core" rows} ["core" 1])]
    (is (= "core/current-evidence-and-remaining-work/core1-lower-core-forms" (:id expected)))
    (is (= expected (sh-crosswalk/task-ref {"core" (vec (reverse rows))} ["core" 1])))
    (is (thrown? Exception (sh-crosswalk/task-ref {"core" (vec (rest rows))} ["core" 1])))))

(deftest regeneration-refuses-to-relabel-changed-bytes-with-an-old-source-commit
  (let [text "original source" digest (sh-inventory/sha256 text)
        record {:nested (sorted-map "string-key" "value")
                :task-authorities {"component" [{:path "tasks.md" :source-commit "pinned" :file-sha256 digest}]}
                :locator {:path "test.clj" :source-commit "pinned" :sha256 digest}}
        reads (atom #{})]
    (is (nil? (sh-crosswalk/validate-pinned-sources! record
                (fn [commit path] (swap! reads conj [commit path]) text))))
    (is (= #{["pinned" "tasks.md"] ["pinned" "test.clj"]} @reads))
    (is (thrown? Exception (sh-crosswalk/validate-pinned-sources! record (fn [_ _] "changed source"))))))

(deftest applying-pins-preserve-current-and-historical-version-digests
  (let [path "plan.md" old-text "historical plan" current-text "current plan"
        old {:path path :source-commit "old" :sha256 (sh-inventory/sha256 old-text)}
        current {:path path :source-commit "current" :sha256 (sh-inventory/sha256 current-text)}
        texts {["old" path] old-text ["current" path] current-text}]
    (doseq [locators [[old current] [current old] [old]]
            :let [seen (atom #{})
                  fixture {:applying-source-pins {path "current"}
                           :source-digests {path (:sha256 current)} :locators locators}]]
      (is (nil? (sh-crosswalk/validate-pinned-sources! fixture
                  (fn [commit p] (swap! seen conj [commit p]) (texts [commit p])))))
      (is (= #{["old" path] ["current" path]} @seen)))
    (doseq [locators [[old current] [current old]]]
      (is (nil? (sh-crosswalk/validate-pinned-sources!
                  {:applying-source-pins {path "current"} :locators locators}
                  (fn [commit p] (texts [commit p]))))))
    (doseq [fixture [{:applying-source-pins {path "current"} :locators [old]}
                    {:applying-source-pins {path "current"} :source-digests {path "stale"} :locators [old]}
                    {:applying-source-pins {path "current"} :source-digests {path (:sha256 current)}
                     :locators [(assoc old :sha256 "tampered")]}
                    {:applying-source-pins {path "current"} :source-digests {path (:sha256 current)}
                     :locators [old (assoc current :sha256 "tampered")]}]]
      (is (thrown? Exception (sh-crosswalk/validate-pinned-sources! fixture
                              (fn [commit p] (texts [commit p]))))))))

(deftest every-explicit-applying-pin-fails-before-either-output-write
  (let [paths (vec (keys sh-crosswalk/applying-source-pins))
        text "pinned source" digest (sh-inventory/sha256 text)
        fixture {:applying-source-pins (zipmap paths (repeat "commit"))
                 :source-digests (zipmap paths (repeat digest))}
        seen (atom #{})]
    (is (nil? (sh-crosswalk/validate-pinned-sources! fixture
                (fn [_ path] (swap! seen conj path) text))))
    (is (= (set paths) @seen))
    (doseq [path paths mode [:mismatch :unavailable :missing-digest]
            :let [writes (atom [])
                  data (if (= mode :missing-digest) (update fixture :source-digests dissoc path) fixture)]]
      (with-redefs [sh-crosswalk/build (constantly data)
                    sh-crosswalk/git-source (fn [_ target]
                                             (if (= path target)
                                               (case mode :mismatch "wrong pinned bytes"
                                                 :unavailable (throw (ex-info "Missing Git object" {})) text)
                                               text))
                    clojure.core/spit (fn [& args] (swap! writes conj (first args)))]
        (is (thrown? Exception (sh-crosswalk/run ["--write"])) [path mode])
        (is (empty? @writes) [path mode])))))

(deftest review-adoption-is-mapping-only-and-bound-to-unchanged-inputs
  (let [source (sh-crosswalk/source-view)
        view (sh-crosswalk/adopt-review source (assoc sh-crosswalk/mapping-review :status :accepted-mapping-only))
        pending (sh-crosswalk/adopt-review source (assoc sh-crosswalk/mapping-review :status :pending-source-version-review))]
    (is (= :accepted-mapping-only (get-in view [:mapping-review :status])))
    (is (every? #(= :owner-and-independent-reviewed (:coverage %)) (:requirements view)))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (is (every? #(= :mapped-owner-review-pending (:coverage %)) (:requirements pending)))
    (is (thrown? Exception (sh-crosswalk/adopt-review
                            (assoc-in source [:requirements 0 :task-references] []) sh-crosswalk/mapping-review)))
    (is (thrown? Exception (sh-crosswalk/adopt-review source (assoc sh-crosswalk/mapping-review :status :pending))))))

(deftest real-derived-view-is-deterministic-and-preserves-gate-boundaries
  (let [view (sh-crosswalk/build) text (sh-crosswalk/rollup view)
        n26 (filter #(= "SH-26" (:milestone %)) (:requirements view))]
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

(deftest current-main-reader-adoption-preserves-predicates-and-history
  (let [view (sh-crosswalk/build)
        bridge (:current-main-successor
                 (edn/read-string (slurp "specs/sh-family-crosswalk/source-version-bridge.edn")))
        rows (:reader-task-comparisons bridge)
        lexical "reader/finite-remaining-work/lexical-products-and-origins-closure"
        changed (remove :raw-unchanged rows)]
    (is (= 7 (count rows)))
    (is (every? :acceptance-predicate-unchanged rows))
    (is (= [lexical] (mapv :id changed)))
    (is (false? (get-in (first changed) [:before :checked])))
    (is (true? (get-in (first changed) [:after :checked])))
    (is (:historical-reader-suffix-unchanged bridge))
    (is (= 3 (count (:accepted-current-scoped-evidence view))))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (let [successor (edn/read-string
                     (slurp "specs/sh-family-crosswalk/reader-dispatch-source-version.edn"))
          historical-source (first (:source-files successor))
          source (first (:source-files (or (:normal-dispatch-seam successor)
                                          (:current-main-composition successor) successor)))]
      (is (= (:applying-main bridge) (get-in historical-source [:before :commit])))
      ;; This bridge is the historical Reader adoption. The readiness successor
      ;; separately verifies the current whole-file version and unchanged body.
      (is (= (get-in source [:after :commit])
             (get sh-crosswalk/predecessor-applying-source-pins
                  "specs/reader-workflow/tasks.md" sh-crosswalk/baseline))))))

(deftest reader-dispatch-successor-retains-exact-source-and-open-acceptance
  (let [view (sh-crosswalk/build)
        historical (edn/read-string
                    (slurp "specs/sh-family-crosswalk/reader-dispatch-source-version.edn"))
        bridge (or (:normal-dispatch-seam historical)
                   (:current-main-composition historical) historical)]
    (is (= :pending-independent-source-version-review (:status bridge)))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= sh-crosswalk/predecessor-mapping-review
           (get-in view [:mapping-review :predecessor-review :predecessor-review])))
    (is (= (:applying-input-fingerprint bridge)
           (get-in view [:mapping-review :reader-normal-source-review :input-fingerprint])))
    (is (not= (:applying-input-fingerprint bridge)
              (sh-crosswalk/review-fingerprint (sh-crosswalk/source-view))))
    (doseq [{:keys [path before after]} (:source-files bridge)]
      (is (= (:commit after)
             (if (= path "docs/self-hosting-slice-ownership.edn")
               (get-in (edn/read-string (slurp "specs/sh-family-crosswalk/reader-assembly-source-version.edn"))
                       [:source-file :before :commit])
               (get sh-crosswalk/predecessor-applying-source-pins path sh-crosswalk/baseline))))
      (is (= (:sha256 before)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
      (when-not (= path "docs/self-hosting-slice-ownership.edn")
        (if-let [successor (first (filter #(= path (:path %))
                                         (:source-files sh-crosswalk/readiness-source-version)))]
          (is (= (:sha256 after) (get-in successor [:before :sha256])))
          (is (= (:sha256 after) (sh-inventory/sha256 (slurp path))))))
      (is (= (:sha256 after)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path)))))
    (let [path "specs/reader-workflow/tasks.md"
          source (first (:source-files bridge))
          before (sh-crosswalk/git-source (get-in source [:before :commit]) path)
          after (slurp path)
          predicates #"(?m)^- \[[ x]\] `reader/[^\n]+|^Done when \(yes/no\): [^\n]+"]
      (is (= (re-seq predicates before) (re-seq predicates after)))
      (is (= (second (str/split before #"<!-- reader-implementation-tasks:end -->" 2))
             (second (str/split after #"<!-- reader-implementation-tasks:end -->" 2))))
      (is (str/includes? after "normal Reader integration and independent acceptance remain open.")))
    (when-let [incoming (:incoming-main bridge)]
      (let [path "docs/self-hosting-slice-ownership.edn"
            own (edn/read-string (sh-crosswalk/git-source
                                 "2e0292c61603aad200df397e397e94e3c7d24818" path))
            main (edn/read-string (sh-crosswalk/git-source incoming path))
            current (edn/read-string (sh-crosswalk/git-source
                                     "4d96671dcd5c649e6be1d8b8d3dcd6e7da8cfeb7" path))]
        (is (= (merge (:standalone-test-owners own) (:standalone-test-owners main)
                      (:new-standalone-owners bridge))
               (:standalone-test-owners current)))))))

(deftest reader-assembly-source-version-conserves-all-predecessor-entries
  (let [bridge (edn/read-string (slurp "specs/sh-family-crosswalk/reader-assembly-source-version.edn"))
        {:keys [path before after]} (:source-file bridge)
        previous (edn/read-string (sh-crosswalk/git-source (:commit before) path))
        current (edn/read-string (sh-crosswalk/git-source (:commit after) path))
        conserved (reduce-kv (fn [record section added-path]
                               (update record section dissoc added-path))
                             current (:added-entries bridge))
        view (sh-crosswalk/build)]
    (is (= previous conserved))
    (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= (:commit after)
           (get-in (edn/read-string (slurp "specs/sh-family-crosswalk/syntax-pipeline-source-version.edn"))
                   [:source-file :before :commit])))
    (is (= :source-conservation-only (:authority bridge)))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= sh-crosswalk/reader-dispatch-mapping-review
           (get-in view [:mapping-review :reader-assembly-predecessor-review])))
    (is (= (get-in view [:reader-assembly-source-version :path])
           (:source-version-bridge sh-crosswalk/syntax-pipeline-predecessor-review)))
    (is (= (sh-crosswalk/review-fingerprint (sh-crosswalk/source-view))
           (get-in view [:mapping-review :input-fingerprint])))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (doseq [wrong [(:commit before) "0000000000000000000000000000000000000000"]]
      (is (thrown? Exception
                   (sh-crosswalk/validate-pinned-sources!
                    {:applying-source-pins {path wrong}
                     :source-digests {path (:sha256 after)}} sh-crosswalk/git-source))))))

(deftest syntax-pipeline-source-version-preserves-predecessor-ownership-and-review
  (let [bridge (edn/read-string (slurp "specs/sh-family-crosswalk/syntax-pipeline-source-version.edn"))
        {:keys [path before after]} (:source-file bridge)
        previous (edn/read-string (sh-crosswalk/git-source (:commit before) path))
        current (edn/read-string (sh-crosswalk/git-source (:commit after) path))
        conserved (reduce-kv (fn [record section added-path]
                               (update record section dissoc added-path))
                             current (:added-entries bridge))
        view (sh-crosswalk/build)]
    (is (= previous conserved))
    (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= (:commit after)
           (get-in (edn/read-string (slurp "specs/sh-family-crosswalk/macro-runtime-source-version.edn"))
                   [:source-file :before :commit])))
    (is (= (:predecessor-input-fingerprint bridge)
           (:input-fingerprint sh-crosswalk/syntax-pipeline-predecessor-review)))
    (is (= (select-keys sh-crosswalk/syntax-pipeline-predecessor-review
                        [:status :input-fingerprint :source-version-bridge])
           (get-in view [:mapping-review :syntax-pipeline-predecessor-review])))
    (is (= (get-in view [:syntax-pipeline-source-version :path])
           (get-in view [:mapping-review :macro-runtime-predecessor-review :source-version-bridge])))
    (is (= (sh-crosswalk/review-fingerprint (sh-crosswalk/source-view))
           (get-in view [:mapping-review :input-fingerprint])))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= :source-conservation-only (:authority bridge)))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (doseq [wrong [(:commit before) "0000000000000000000000000000000000000000"]]
      (is (thrown? Exception
                   (sh-crosswalk/validate-pinned-sources!
                    {:applying-source-pins {path wrong}
                     :source-digests {path (:sha256 after)}} sh-crosswalk/git-source))))))

(deftest macro-runtime-source-version-preserves-predecessor-ownership-and-review
  (let [bridge (edn/read-string (slurp "specs/sh-family-crosswalk/macro-runtime-source-version.edn"))
        {:keys [path before after]} (:source-file bridge)
        previous (edn/read-string (sh-crosswalk/git-source (:commit before) path))
        current (edn/read-string (sh-crosswalk/git-source (:commit after) path))
        conserved (reduce-kv (fn [record section added-path]
                               (update record section dissoc added-path))
                             current (:added-entries bridge))
        view (sh-crosswalk/build)]
    (is (= previous conserved))
    (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= (:commit after) (get-in (edn/read-string (slurp "specs/sh-family-crosswalk/macro-artifacts-source-version.edn")) [:source-file :before :commit])))
    (is (= (:predecessor-input-fingerprint bridge)
           (:input-fingerprint sh-crosswalk/macro-runtime-predecessor-review)))
    (is (= (select-keys sh-crosswalk/macro-runtime-predecessor-review
                        [:status :input-fingerprint :source-version-bridge])
           (get-in view [:mapping-review :macro-runtime-predecessor-review])))
    (is (= (get-in view [:macro-runtime-source-version :path])
           (get-in view [:mapping-review :macro-artifacts-predecessor-review :source-version-bridge])))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= :source-conservation-only (:authority bridge)))
    (is (= 180 (count (:requirements view))))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (doseq [wrong [(:commit before) "0000000000000000000000000000000000000000"]]
      (is (thrown? Exception
                   (sh-crosswalk/validate-pinned-sources!
                    {:applying-source-pins {path wrong}
                     :source-digests {path (:sha256 after)}} sh-crosswalk/git-source))))))

(deftest macro-artifacts-source-version-conserves-only-helper-consumer
  (let [bridge (edn/read-string (slurp "specs/sh-family-crosswalk/macro-artifacts-source-version.edn"))
        {:keys [path before after]} (:source-file bridge)
        change (:changed-entry bridge)
        entry [(:section change) (:path change) (:field change)]
        previous (edn/read-string (sh-crosswalk/git-source (:commit before) path))
        current (edn/read-string (sh-crosswalk/git-source (:commit after) path))
        view (sh-crosswalk/build)]
    (is (= previous (assoc-in current entry (:before change))))
    (is (= (:after change) (get-in current entry)))
    (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= (:sha256 after) (get-in (edn/read-string (slurp "specs/sh-family-crosswalk/pr574-macro-artifacts-composition.edn")) [:source-file :before :sha256])))
    (is (= (:predecessor-input-fingerprint bridge)
           (:input-fingerprint sh-crosswalk/macro-artifacts-predecessor-review)))
    (is (= (select-keys sh-crosswalk/macro-artifacts-predecessor-review
                       [:status :input-fingerprint :source-version-bridge])
           (get-in view [:mapping-review :macro-artifacts-predecessor-review])))
    (is (= (get-in view [:macro-artifacts-source-version :path])
           (get-in view [:mapping-review :macro-artifacts-current-review :source-version-bridge])))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= :source-conservation-only (:authority bridge)))
    (is (= 180 (count (:requirements view))))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (doseq [wrong [(:commit before) "0000000000000000000000000000000000000000"]]
      (is (thrown? Exception
                   (sh-crosswalk/validate-pinned-sources!
                    {:applying-source-pins {path wrong}
                     :source-digests {path (:sha256 after)}} sh-crosswalk/git-source))))))

(deftest pkg5-task-source-successor-preserves-population-and-review-history
  (let [view (sh-crosswalk/build)
        bridge (:pkg5-interface-successor (edn/read-string (slurp "specs/sh-family-crosswalk/source-version-bridge.edn")))
        lineage (get (edn/read-string (slurp "specs/sh-family-crosswalk/task-authority-lineage.edn")) "build")
        rows (:retained-records lineage)]
    (is (= 5 (count rows)))
    (is (empty? (:added-ids lineage)))
    (is (= 4 (count (filter :unchanged rows))))
    (is (= ["build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"] (:changed-records lineage)))
    (is (every? #(= "Open" (get-in % [:after :status])) rows))
    (is (:historical-suffix-unchanged lineage))
    (doseq [path ["specs/build-workflow/tasks.md" "docs/component-roadmaps/build.md"]]
      (is (= (:applying-task-source bridge)
             (get sh-crosswalk/predecessor-applying-source-pins path sh-crosswalk/baseline))))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= :pending-source-version-review (get-in view [:mapping-review :predecessor-review :status])))
    (is (= "c0714e09a510f173d0e69380478beb1a5790b07635283009749612aff9754b57"
           (get-in view [:mapping-review :pkg5-source-review :receipt-sha256])))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))))

(deftest pr574-composition-retains-native-reader-and-syntax-histories
  (let [bridge (edn/read-string (slurp "specs/sh-family-crosswalk/pr574-composition-source-version.edn"))
        {:keys [path before after]} (:source-file bridge)
        previous (edn/read-string (sh-crosswalk/git-source (:commit before) path))
        current (edn/read-string (sh-crosswalk/git-source (:commit after) path))
        conserved (reduce-kv (fn [record section paths]
                               (update record section #(apply dissoc % paths)))
                             current (:added-entries bridge))
        native (edn/read-string
                (sh-crosswalk/git-source (:accepted-pure-candidate bridge) path))
        native-bridge (:native-v2-ownership-successor
                        (edn/read-string (slurp "specs/sh-family-crosswalk/source-version-bridge.edn")))
        view (sh-crosswalk/build)]
    (is (= previous conserved))
    (doseq [[section paths] (:added-entries bridge) p paths]
      (is (not (contains? (get previous section) p)))
      (is (= (get-in native [section p]) (get-in current [section p]))))
    (doseq [record [before after]]
      (is (= (:sha256 record)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:commit record) path)))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= "3d669ff375f173660995b57708934d827a168995e15cf4dfa2541d6ab898c60e" (get-in view [:mapping-review :published-d15-review :input-fingerprint])))
    (is (= native-bridge
           (:native-v2-ownership-successor
             (edn/read-string
               (sh-crosswalk/git-source (:accepted-pure-candidate bridge)
                                       "specs/sh-family-crosswalk/source-version-bridge.edn")))))
    (doseq [p ["specs/sh-family-crosswalk/reader-assembly-source-version.edn"
               "specs/sh-family-crosswalk/syntax-pipeline-source-version.edn"]]
      (is (= (sh-crosswalk/git-source (:main bridge) p) (slurp p))))
    (is (= (:native-pure-source-review bridge)
           (:mapping-review (edn/read-string
                              (sh-crosswalk/git-source (:accepted-pure-candidate bridge)
                                                      "specs/sh-family-crosswalk/crosswalk.edn")))))
    (is (= sh-crosswalk/pr574-predecessor-review
           (get-in view [:mapping-review :pr574-predecessor-review])))
    (is (= :source-conservation-only (:authority bridge)))
    (is (= :pending-independent-affected-composition-review (:status bridge)))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (is (thrown? Exception
                 (sh-crosswalk/validate-pinned-sources!
                   {:applying-source-pins {path (:commit before)}
                    :source-digests {path (:sha256 after)}} sh-crosswalk/git-source)))))

(deftest pr574-current-macro-composition-preserves-exact-parent-inputs
  (let [bridge (edn/read-string (slurp "specs/sh-family-crosswalk/pr574-conflict-composition-source-version.edn"))
        {:keys [path before after]} (:source-file bridge)
        main (edn/read-string (sh-crosswalk/git-source (:main bridge) path))
        published (edn/read-string (sh-crosswalk/git-source (:published-candidate bridge) path))
        current (edn/read-string (sh-crosswalk/git-source (:commit after) path))
        conserved (reduce-kv (fn [record section paths]
                               (update record section #(apply dissoc % paths)))
                             current (:added-entries bridge))
        view (sh-crosswalk/build)]
    (is (= main conserved))
    (doseq [[section paths] (:added-entries bridge) p paths]
      (is (not (contains? (get main section) p)))
      (is (= (get-in published [section p]) (get-in current [section p]))))
    (doseq [record [before after]]
      (is (= (:sha256 record)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:commit record) path)))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= (:commit after) (get-in (edn/read-string (slurp "specs/sh-family-crosswalk/pr574-macro-artifacts-composition.edn")) [:previous-source-ownership :commit])))
    ;; These paths are historical inputs to PR574, while the current checkout
    ;; has a newer source frame.  Verify the old bytes from their immutable
    ;; pins and bind the live files to the current source frame separately;
    ;; requiring old and current bytes to be identical would reject legitimate
    ;; later bridge composition.
    (doseq [p (:preserved-history-paths bridge)
            :let [historical-commit (if (= p "specs/sh-family-crosswalk/macro-runtime-source-version.edn")
                                      (:main bridge) (:published-candidate bridge))
                  historical (sh-crosswalk/git-source historical-commit p)
                  current-commit (:source-input-commit sh-crosswalk/readiness-source-version)
                  current (sh-crosswalk/git-source current-commit p)]]
      (is (= (sh-inventory/sha256 historical)
             (sh-inventory/sha256 (sh-crosswalk/git-source historical-commit p))) p)
      (is (= current (slurp p)) p))
    (doseq [[commit key] [[(:main bridge) :current-main-review]
                         [(:published-candidate bridge) :published-d15-review]]]
      (let [reference (key bridge)
            text (sh-crosswalk/git-source commit (:source-path reference))]
        (is (= (:source-file-sha256 reference) (sh-inventory/sha256 text)))
        (is (= (select-keys reference [:status :input-fingerprint :source-version-bridge])
               (select-keys (:mapping-review (edn/read-string text))
                            [:status :input-fingerprint :source-version-bridge])))))
    (is (= (select-keys (:current-main-review bridge) [:status :input-fingerprint :source-version-bridge])
           (select-keys (get-in view [:mapping-review :current-main-predecessor-review])
                        [:status :input-fingerprint :source-version-bridge])))
    (is (= (:published-d15-review bridge)
           (get-in view [:mapping-review :published-d15-review])))
    (is (= :pending-independent-affected-composition-review (:status bridge)))
    (is (= :source-conservation-only (:authority bridge)))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= 180 (count (:requirements view))))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (is (thrown? Exception
                 (sh-crosswalk/validate-pinned-sources!
                   {:applying-source-pins {path (:commit before)}
                    :source-digests {path (:sha256 after)}} sh-crosswalk/git-source)))))

(deftest pr574-macro-artifacts-composition-keeps-current-main-and-native-union
  (let [bridge (edn/read-string (slurp "specs/sh-family-crosswalk/pr574-macro-artifacts-composition.edn"))
        {:keys [path before after]} (:source-file bridge)
        current (edn/read-string (sh-crosswalk/git-source (:commit after) path))
        main (edn/read-string (sh-crosswalk/git-source (:main bridge) path))
        previous (edn/read-string (sh-crosswalk/git-source (:previous-candidate bridge) path))
        conserved (reduce-kv (fn [record section paths]
                               (update record section #(apply dissoc % paths)))
                             current (:added-entries bridge))
        view (sh-crosswalk/build)]
    (is (= main conserved))
    (doseq [[section paths] (:added-entries bridge) p paths]
      (is (not (contains? (get main section) p)))
      (is (= (get-in previous [section p]) (get-in current [section p]))))
    (doseq [record [before after]]
      (is (= (:sha256 record)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:commit record) path)))))
    (is (= (:commit after) (get-in (edn/read-string (slurp "specs/sh-family-crosswalk/resolution-runtime-composition-source-version.edn")) [:native-source-ownership :commit])))
    (doseq [p ["specs/sh-family-crosswalk/source-version-bridge.edn"
               "specs/sh-family-crosswalk/pr574-composition-source-version.edn"
               "specs/sh-family-crosswalk/pr574-conflict-composition-source-version.edn"]
            :let [historical (sh-crosswalk/git-source (:previous-candidate bridge) p)
                  current (sh-crosswalk/git-source (:source-input-commit sh-crosswalk/readiness-source-version) p)]]
      (is (= (sh-inventory/sha256 historical)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:previous-candidate bridge) p))) p)
      (is (= current (slurp p)) p))
    (doseq [p ["specs/sh-family-crosswalk/reader-assembly-source-version.edn"
               "specs/sh-family-crosswalk/syntax-pipeline-source-version.edn"
               "specs/sh-family-crosswalk/macro-runtime-source-version.edn"
               "specs/sh-family-crosswalk/macro-artifacts-source-version.edn"]]
      (is (= (sh-crosswalk/git-source (:main bridge) p) (slurp p))))
    (doseq [key [:previous-review :current-main-review]
            :let [reference (key bridge)
                  text (sh-crosswalk/git-source (:source-commit reference) (:source-path reference))]]
      (is (= (:source-file-sha256 reference) (sh-inventory/sha256 text)))
      (is (= (select-keys reference [:status :input-fingerprint :source-version-bridge])
             (select-keys (:mapping-review (edn/read-string text))
                          [:status :input-fingerprint :source-version-bridge]))))
    (is (= :pending-independent-affected-final-composition-review (:status bridge)))
    (is (= :source-conservation-only (:authority bridge)))
    (is (= 180 (count (:requirements view))))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (is (thrown? Exception
                 (sh-crosswalk/validate-pinned-sources!
                   {:applying-source-pins {path (:commit before)}
                    :source-digests {path (:sha256 after)}} sh-crosswalk/git-source)))))

(deftest resolution-runtime-source-version-preserves-predecessor-ownership-and-review
  (let [bridge (edn/read-string (slurp "specs/sh-family-crosswalk/resolution-runtime-source-version.edn"))
        {:keys [path before after]} (:source-file bridge)
        previous (edn/read-string (sh-crosswalk/git-source (:commit before) path))
        current (edn/read-string (sh-crosswalk/git-source (:commit after) path))
        conserved (reduce-kv (fn [record section added-path] (update record section dissoc added-path))
                             current (:added-entries bridge))
        view (sh-crosswalk/build)]
    (is (= previous conserved))
    (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= (:commit after) (get-in (edn/read-string (slurp "specs/sh-family-crosswalk/resolution-runtime-composition-source-version.edn")) [:resolution-source-ownership :commit])))
    (is (= (:predecessor-input-fingerprint bridge)
           (:input-fingerprint sh-crosswalk/resolution-runtime-predecessor-review)))
    (is (= (select-keys sh-crosswalk/resolution-runtime-predecessor-review
                        [:status :input-fingerprint :source-version-bridge])
           (get-in view [:mapping-review :resolution-runtime-predecessor-review])))
    (is (= (get-in view [:resolution-runtime-source-version :path])
           (get-in view [:mapping-review :resolution-runtime-current-review :source-version-bridge])))
    (is (= (sh-crosswalk/review-fingerprint (sh-crosswalk/source-view))
           (get-in view [:mapping-review :input-fingerprint])))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= :source-conservation-only (:authority bridge)))
    (is (= 180 (count (:requirements view))))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (doseq [wrong [(:commit before) "0000000000000000000000000000000000000000"]]
      (is (thrown? Exception
                   (sh-crosswalk/validate-pinned-sources!
                    {:applying-source-pins {path wrong}
                     :source-digests {path (:sha256 after)}} sh-crosswalk/git-source))))))

(deftest resolution-native-composition-conserves-both-source-histories
  (let [bridge (edn/read-string (slurp "specs/sh-family-crosswalk/resolution-runtime-composition-source-version.edn"))
        {:keys [path before after]} (:source-file bridge)
        ;; The ownership input is historical; current live drift is inspected
        ;; separately by the readiness successor and dependency report.
        current (edn/read-string (sh-crosswalk/git-source (:commit after) path))
        remove-entries (fn [record entries]
                         (reduce-kv (fn [r section paths]
                                      (update r section #(apply dissoc % (if (string? paths) [paths] paths)))) record entries))
        main (edn/read-string (sh-crosswalk/git-source (:commit before) path))
        resolution (edn/read-string (sh-crosswalk/git-source (get-in bridge [:resolution-source-ownership :commit]) path))
        view (sh-crosswalk/build)
        ignored [:input-fingerprint :source-version-bridge :qualification
                 :predecessor-input-fingerprint
                 :resolution-main-predecessor-review :resolution-runtime-current-review
                 :resolution-runtime-predecessor-review
                 :implementation-readiness-predecessor-review
                 :stdlib-task-source-transition :platform-task-source-transition]]
    (is (= main (remove-entries current (:added-entries bridge))))
    (is (= resolution (remove-entries current (:native-entries bridge))))
    (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (doseq [record [before after (:resolution-source-ownership bridge) (:native-source-ownership bridge)]]
      (is (= (:sha256 record) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit record) path)))))
    (is (= (:commit after) (get-in view [:applying-source-pins path])))
    (doseq [[kind reference] (:predecessors bridge)
            :let [text (sh-crosswalk/git-source (:commit reference) (:path reference))
                  review (:mapping-review (edn/read-string text))]]
      (is (= (:sha256 reference) (sh-inventory/sha256 text)))
      (is (= (:review reference) (select-keys review [:status :input-fingerprint :source-version-bridge])))
      (is (= review (if (= :main kind) sh-crosswalk/resolution-main-predecessor-review
                       sh-crosswalk/resolution-runtime-current-review))))
    (is (= (apply dissoc sh-crosswalk/resolution-main-predecessor-review ignored)
           (apply dissoc (:mapping-review view) ignored)))
    (is (= (get-in bridge [:predecessors :main :review])
           (get-in view [:mapping-review :resolution-main-predecessor-review])))
    (is (= (get-in bridge [:predecessors :resolution :review])
           (get-in view [:mapping-review :resolution-runtime-current-review])))
    (is (= (get-in view [:resolution-runtime-composition-source-version :path])
           (get-in view [:mapping-review :implementation-readiness-predecessor-review
                         :source-version-bridge])))
    (is (= (sh-crosswalk/review-fingerprint (sh-crosswalk/source-view))
           (get-in view [:mapping-review :input-fingerprint])))
    (is (= :source-conservation-only (:authority bridge)))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= 180 (count (:requirements view))))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))
    (doseq [wrong [(:commit before) (get-in bridge [:resolution-source-ownership :commit])
                   "0000000000000000000000000000000000000000"]]
      (is (thrown? Exception
                   (sh-crosswalk/validate-pinned-sources!
                    {:applying-source-pins {path wrong} :source-digests {path (:sha256 after)}}
                    sh-crosswalk/git-source))))))

(deftest readiness-successor-conserves-task-bodies-routes-and-historical-subjects
  (let [bridge sh-crosswalk/readiness-source-version
        predecessor (:predecessor bridge)
        prior-text (sh-crosswalk/git-source (:commit predecessor) (:path predecessor))
        prior (edn/read-string prior-text)
        view (sh-crosswalk/build)
        rows (fn [v] (into {} (for [[_ records] (:task-authorities v) r records] [(:id r) r])))
        before (rows prior) after (rows view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        comparisons (:task-comparisons bridge)
        transition-for (fn [path]
                         (first (filter #(= path (:path %))
                                        sh-crosswalk/current-source-transition-contracts)))]
    (is (= (:sha256 predecessor) (sh-inventory/sha256 prior-text)))
    (is (= (:review predecessor)
           (select-keys (:mapping-review prior) [:status :input-fingerprint :source-version-bridge])))
    (is (= 36 (:unchanged-body-count comparisons)))
    (is (= (set (map :id (:changed-bodies comparisons))) changed))
    (is (= 103 (count changed)))
    (is (= 36 (count (remove changed (keys before)))))
    (let [refinement (:math-parent-refinement bridge)
          predecessor (:predecessor-readiness-successor refinement)
          previous-text (sh-crosswalk/git-source (:commit predecessor) (:path predecessor))
          previous (edn/read-string previous-text)
          previous-projection (sh-crosswalk/git-source (:commit predecessor) (:projection-path predecessor))]
      (is (= (:sha256 predecessor) (sh-inventory/sha256 previous-text)))
      (is (= (:projection-sha256 predecessor) (sh-inventory/sha256 previous-projection)))
      (is (= (:input-fingerprint predecessor) (:input-fingerprint previous)))
      (is (= 135 (get-in previous [:task-comparisons :unchanged-body-count])))
      (is (= 4 (count (get-in previous [:task-comparisons :changed-bodies]))))
      (let [math-predecessor (get-in bridge [:release-testing-refinement :predecessor-readiness-successor])
            math-bridge (edn/read-string (sh-crosswalk/git-source (:commit math-predecessor) (:path math-predecessor)))]
        (is (= (conj (set (map :id (get-in previous [:task-comparisons :changed-bodies]))) "math")
               (set (map :id (get-in math-bridge [:task-comparisons :changed-bodies]))))))
      (is (= 28 (count (:criterion-ids refinement))))
      (is (= (set (:criterion-ids refinement))
             (set (map second (re-seq #"(?m)^(?:Criterion|Decision criterion) `([^`]+)`"
                                     (raw view (after "math")))))))
      (is (= {:profile 3 :domain 2} (:reference-rebindings refinement)))
      (is (= :pending-current-repartition-review (:checker-allocation refinement)))
      (let [contexts (:published-source-contexts refinement)
            text (apply str (map slurp [".specify/implementation-readiness.md"
                                       "specs/build-workflow/plan.md"
                                       "specs/domains-workflow/spec.md"
                                       "specs/domains-workflow/plan.md"
                                       "specs/domains-workflow/tasks.md"
                                       "specs/evolution-workflow/evidence.md"]))]
        (is (= 13 (count contexts)))
        (is (= 12 (count (filter #(= :exact-file-bytes (:relation %)) contexts))))
        (doseq [{:keys [path published-commit published-sha256 preserved-sha256 relation]} contexts]
          (is (str/includes? text (str "https://github.com/burn2delete/uniform/blob/" published-commit "/" path)) path)
          (if (= :exact-file-bytes relation)
            (is (= published-sha256 preserved-sha256) path)
            (do (is (= "specs/domains-workflow/tasks.md" path))
                (is (= :published-predecessor-with-one-qualified-local-amendment relation))
                (is (not= published-sha256 preserved-sha256)))))
        (is (not (str/includes? text "https://github.com/burn2delete/uniform/blob/55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6/")))
        (is (= :not-attempted (get-in refinement [:source-link-verification :aggregate-publication])))
        (is (= "schema/checked-consumer-projection"
               (get-in refinement [:source-link-verification :qualified-local-record])))))
    (is (every? #(= (:checked (before %)) (:checked (after %))) (keys before)))
    (is (= (set (:added-ids comparisons)) (set (remove (set (keys before)) (keys after)))))
    (is (= (set (:added-ids comparisons)) (set (get-in view [:stable-task-routing-keys "evolution"]))))
    (is (= 10 (count (:added-ids comparisons))))
    (is (every? #(false? (:checked (after %))) (:added-ids comparisons)))
    (is (empty? (remove (set (keys after)) (keys before))))
    (doseq [{:keys [id before-sha256 after-sha256 before-checked after-checked]} (:changed-bodies comparisons)]
      (is (= before-sha256 (sh-inventory/sha256 (raw prior (before id)))) id)
      (if-let [transition (transition-for (:path (after id)))]
        (is (= (:live-sha256 transition)
               (sh-inventory/sha256
                (sh-crosswalk/git-source (:applying-ref transition) (:path transition)))) id)
        (is (= after-sha256 (sh-inventory/sha256 (raw view (after id)))) id))
      (is (false? before-checked) id)
      (is (false? after-checked) id))
    (doseq [{:keys [path before after]} (:source-files bridge)]
      (when (:commit before)
        (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))) path))
      (if-let [transition (transition-for path)]
        (do
          (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))) path)
          (is (= (:live-sha256 transition) (sh-inventory/sha256 (slurp path))) path))
        (do
          (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))) path)
          (is (= (:sha256 after) (sh-inventory/sha256 (slurp path))) path))))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (mapv :task-references (:sh07-sequence prior)) (mapv :task-references (:sh07-sequence view))))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (doseq [[path record] (:retained-historical-source-inputs bridge)]
      (is (= (:sha256 record) (get-in prior [:source-digests path]) (get-in view [:source-digests path])))
      (is (= (:commit record) (get-in view [:applying-source-pins path])))
      (is (= (:sha256 record) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit record) path))))
      (is (= (:live-inspected-sha256 record)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:source-input-commit bridge) path)))))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (let [predecessor-review (get-in view [:mapping-review :implementation-readiness-predecessor-review])]
      (is (= (select-keys sh-crosswalk/readiness-predecessor-mapping-review
                          [:status :input-fingerprint :source-version-bridge])
             (select-keys predecessor-review [:status :input-fingerprint :source-version-bridge])))
      (is (= "specs/sh-family-crosswalk/implementation-readiness-source-version.edn"
             (:historical-source-version predecessor-review))))
    (is (= (:input-fingerprint bridge)
           (get-in view [:mapping-review :predecessor-input-fingerprint])))
    (is (= (get-in view [:mapping-review :input-fingerprint])
           (sh-crosswalk/review-fingerprint (sh-crosswalk/source-view))))
    (is (= 180 (count (:requirements view))))
    (is (= 18 (count (:sh07-sequence view))))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))))

(deftest release-refinement-preserves-source-producers-and-aggregate-clauses
  (let [bridge sh-crosswalk/readiness-source-version
        refinement (:release-testing-refinement bridge)
        predecessor (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit predecessor) (:path predecessor))
        previous (edn/read-string previous-text)
        previous-projection (sh-crosswalk/git-source (:commit predecessor) (:projection-path predecessor))
        {:keys [path published-commit published-sha256]} (:published-source refinement)
        published (sh-crosswalk/git-source published-commit path)
        current (slurp path)
        blocks (fn [text]
                 (into {} (for [body (str/split text #"(?m)(?=^- \[ \])")
                                :let [id (second (re-find #"^- \[ \] `([^`]+)`" body))]
                                :when id]
                            [id (str/trim body)])))
        source (blocks (first (str/split published #"<!-- release-implementation-tasks:end -->" 2)))
        future (blocks (second (re-find #"(?s)<!-- release-testing-proposals:start -->(.*?)<!-- release-testing-proposals:end -->" current)))
        aggregates (set (:aggregate-task-ids refinement))
        before-changed (set (map :id (get-in previous [:task-comparisons :changed-bodies])))
        release-checkpoint (get-in bridge [:domain-parent-refinement :predecessor-readiness-successor])
        release-bridge (edn/read-string (sh-crosswalk/git-source (:commit release-checkpoint) (:path release-checkpoint)))
        after-changed (set (map :id (get-in release-bridge [:task-comparisons :changed-bodies])))
        marker-by-suffix {"bind-each-claimed-component-to-exact-implementation-and-conformance" "release-parent-coverage"
                          "complete-stage-compatibilityequivalence-across-the-claimed-compiler" "release-parent-stage-comparison"
                          "produce-actual-self-hosted-compilerruntime-execution" "release-parent-self-hosting"
                          "build-reproducible-signed-distributions-with-provenancesbom" "release-parent-reproducibility"
                          "obtain-independent-acceptance-for-the-exact-release-candidate" "release-parent-acceptance"}]
    (is (= (:sha256 predecessor) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 predecessor) (sh-inventory/sha256 previous-projection)))
    (is (= (:input-fingerprint predecessor) (:input-fingerprint previous)))
    (is (= 134 (get-in previous [:task-comparisons :unchanged-body-count])))
    (is (= 5 (count before-changed)))
    (is (= after-changed (into before-changed aggregates)))
    (is (= published-sha256 (sh-inventory/sha256 published)))
    (is (= 18 (count source)))
    (is (= 13 (count future) (count (:future-records refinement))))
    (is (= 5 (count aggregates) (count (:aggregate-clauses refinement))))
    (is (= (set (keys source)) (into aggregates (keys future))))
    (is (= aggregates (set (sh-crosswalk/stable-task-routing-keys "release"))))
    (is (= (set (keys future)) (set (map :id (:future-records refinement)))))
    (doseq [{:keys [id body-sha256 status]} (:future-records refinement)]
      (is (= (source id) (future id)) id)
      (is (= body-sha256 (sh-inventory/sha256 (future id))) id)
      (is (= :open status))
      (is (str/includes? (future id) "Status: Open") id))
    (doseq [{:keys [id closing-evidence-sha256]} (:aggregate-clauses refinement)
            :let [clause (second (re-find #"(?m)^Closing evidence: ([^\n]+)$" (source id)))
                  marker (marker-by-suffix (last (str/split id #"/")))
                  addition (second (re-find (re-pattern (str "(?s)<!-- " marker ":start -->(.*?)<!-- " marker ":end -->")) current))]]
      (is (contains? aggregates id))
      (is (= closing-evidence-sha256 (sh-inventory/sha256 clause)) id)
      (is (= clause (last (remove str/blank? (str/split-lines addition)))) id))
    (is (= :pending-independent-current-review (:future-sh-composition refinement)))
    (is (= :none (:source-body-changes refinement)))
    (is (= :unassessed (:implementation-acceptance refinement)))
    (is (= refinement (get-in (sh-crosswalk/source-view) [:implementation-readiness-source-version :release-testing-refinement])))))

(deftest domain-parent-refinement-preserves-complete-source-bodies-and-role-boundaries
  (let [bridge sh-crosswalk/readiness-source-version
        refinement (:domain-parent-refinement bridge)
        predecessor (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit predecessor) (:path predecessor))
        previous (edn/read-string previous-text)
        previous-projection (sh-crosswalk/git-source (:commit predecessor) (:projection-path predecessor))
        {:keys [path published-commit published-sha256]} (:published-source refinement)
        published (sh-crosswalk/git-source published-commit path)
        current (slurp path)
        parents (:parent-records refinement)
        body (fn [text id]
               (let [block (first (filter #(str/starts-with? % (str "- [ ] `" id "` "))
                                          (str/split text #"(?m)(?=^- \[ \])")))]
                 (str/trim (subs block (.indexOf block "Projected task block:")))))
        before-changed (set (map :id (get-in previous [:task-comparisons :changed-bodies])))
        domain-checkpoint (get-in bridge [:ai-parent-refinement :predecessor-readiness-successor])
        domain-bridge (edn/read-string (sh-crosswalk/git-source (:commit domain-checkpoint) (:path domain-checkpoint)))
        after-changed (set (map :id (get-in domain-bridge [:task-comparisons :changed-bodies])))]
    (is (= (:sha256 predecessor) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 predecessor) (sh-inventory/sha256 previous-projection)))
    (is (= (:input-fingerprint predecessor) (:input-fingerprint previous)))
    (is (= 130 (get-in previous [:task-comparisons :unchanged-body-count])))
    (is (= 9 (count before-changed)))
    (is (= #{"query" "workflow" "gpu" "ui"} (set (map :id parents))))
    (is (= after-changed (into before-changed (map :id parents))))
    (is (= published-sha256 (sh-inventory/sha256 published)))
    (doseq [{:keys [id marker source-body-sha256 current-body-sha256 criterion-ids profile-mapping-ids]} parents
            :let [raw (body published id)
                  region (second (re-find (re-pattern (str "(?s)<!-- " marker ":start -->(.*?)<!-- " marker ":end -->")) current))
                  carried (second (re-find #"(?s)(Projected task block:\n.*?)\n\nHistorical projected paragraph \(unchanged\):" region))
                  expected (-> raw
                               (str/replace "specs/requirement-reconciliation/profile-system/gap-completion.edn"
                                            "[the preserved profile-interface context](plan.md#query-workflow-gpu-and-ui-source-context)")
                               (str/replace "specs/requirement-reconciliation/domain-gap-completion.edn"
                                            "[the full preserved Domain context](#wider-integration-task-proposals)"))]]
      (is (= source-body-sha256 (sh-inventory/sha256 raw)) id)
      (is (= current-body-sha256 (sh-inventory/sha256 carried)) id)
      (is (= expected carried) id)
      (is (= criterion-ids (mapv second (re-seq #"(?m)^(?:Criterion|Decision criterion) `([^`]+)`" carried))) id)
      (is (= profile-mapping-ids (mapv second (re-seq #"Criterion profile-gap/([^ ]+) \(Open\):" carried))) id))
    (is (= 21 (reduce + (map #(count (:criterion-ids %)) parents))))
    (is (= 5 (reduce + (map #(count (:profile-mapping-ids %)) parents))))
    (is (= :only-declared-context-rebindings (:source-body-changes refinement)))
    (is (= :pending-current-repartition-review (:producer-consumer-allocation refinement)))
    (is (= :detailed-source-resumption-pending (:ai-parent refinement)))
    (is (= :unassessed (:implementation-acceptance refinement)))
    (is (= refinement (get-in (sh-crosswalk/source-view) [:implementation-readiness-source-version :domain-parent-refinement])))))

(deftest ai-parent-refinement-preserves-source-criteria-contexts-and-mixed-facets
  (let [bridge sh-crosswalk/readiness-source-version
        refinement (:ai-parent-refinement bridge)
        predecessor (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit predecessor) (:path predecessor))
        previous (edn/read-string previous-text)
        previous-projection (sh-crosswalk/git-source (:commit predecessor) (:projection-path predecessor))
        {:keys [path published-commit published-sha256]} (:published-source refinement)
        published (sh-crosswalk/git-source published-commit path)
        block (first (filter #(str/starts-with? % "- [ ] `ai` AI\n")
                             (str/split published #"(?m)(?=^- \[ \])")))
        raw (str/trim (subs block (.indexOf block "Projected task block:")))
        current (slurp path)
        region (second (re-find #"(?s)<!-- domains-ai-parent:start -->(.*?)<!-- domains-ai-parent:end -->" current))
        carried (second (re-find #"(?s)(Projected task block:\n.*?)\n\nHistorical projected paragraph \(unchanged\):" region))
        expected (-> raw
                     (str/replace "specs/requirement-reconciliation/profile-system/gap-completion.edn"
                                  "[the preserved profile-interface context](plan.md#ai-parent-source-context)")
                     (str/replace "specs/requirement-reconciliation/domain-gap-completion.edn"
                                  "[the full preserved Domain context](#wider-integration-task-proposals)"))
        contexts (:published-source-contexts refinement)
        proposal-context (first (filter #(= "specs/requirement-reconciliation/phase-11-proposal.edn" (:path %)) contexts))
        proposal (edn/read-string (sh-crosswalk/git-source (:published-commit proposal-context) (:path proposal-context)))
        plan (slurp "specs/domains-workflow/plan.md")
        before-changed (set (map :id (get-in previous [:task-comparisons :changed-bodies])))
        ai-checkpoint (get-in bridge [:platform-refinement :predecessor-readiness-successor])
        ai-bridge (edn/read-string (sh-crosswalk/git-source (:commit ai-checkpoint) (:path ai-checkpoint)))
        after-changed (set (map :id (get-in ai-bridge [:task-comparisons :changed-bodies])))]
    (is (= (:sha256 predecessor) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 predecessor) (sh-inventory/sha256 previous-projection)))
    (is (= (:input-fingerprint predecessor) (:input-fingerprint previous)))
    (is (= 126 (get-in previous [:task-comparisons :unchanged-body-count])))
    (is (= 13 (count before-changed)))
    (is (= after-changed (conj before-changed "ai")))
    (is (= published-sha256 (sh-inventory/sha256 published)))
    (is (= (:source-body-sha256 refinement) (sh-inventory/sha256 raw)))
    (is (= (:current-body-sha256 refinement) (sh-inventory/sha256 carried)))
    (is (= expected carried))
    (is (= (:criterion-ids refinement) (mapv second (re-seq #"(?m)^Criterion `([^`]+)`" carried))))
    (is (= 97 (count (:criterion-ids refinement)) (count (set (:criterion-ids refinement)))))
    (is (= (:profile-mapping-ids refinement) (mapv second (re-seq #"Criterion profile-gap/([^ ]+) \(Open\):" carried))))
    (is (= 3 (count contexts) (count (:profile-mapping-ids refinement))))
    (doseq [{:keys [path published-commit published-sha256 preserved-sha256 relation]} contexts]
      (is (= :exact-file-bytes relation))
      (is (= published-sha256 preserved-sha256
             (sh-inventory/sha256 (sh-crosswalk/git-source published-commit path))) path)
      (is (str/includes? plan (str "https://github.com/burn2delete/uniform/blob/" published-commit "/" path)) path))
    (is (= 21 (count (:mixed-facet-ids refinement))))
    (is (= (set (:mixed-facet-ids refinement)) (set (:unresolved-facet-ids proposal))))
    (doseq [id (:mixed-facet-ids refinement)]
      (is (str/includes? plan (str "`" (str/replace id "facet/ai-build-tools/" "") "`")) id))
    (is (= :retained-qualified-open-context (:historical-mixed-status refinement)))
    (is (= :applies-without-new-permission-or-accounting-gate (:current-accounting-policy refinement)))
    (is (= :pending-current-repartition-review (:producer-consumer-allocation refinement)))
    (is (= :unassessed (:implementation-acceptance refinement)))
    (is (= :not-conferred (get-in refinement [:source-inspection :semantic-entailment])))
    (is (= refinement (get-in (sh-crosswalk/source-view) [:implementation-readiness-source-version :ai-parent-refinement])))))

(deftest platform-refinement-preserves-target-bodies-and-conditional-product-records
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:foundation-tools-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:platform-refinement bridge)
        predecessor (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit predecessor) (:path predecessor))
        previous (edn/read-string previous-text)
        previous-projection (sh-crosswalk/git-source (:commit predecessor) (:projection-path predecessor))
        {:keys [path published-commit published-sha256]} (:published-source refinement)
        published (sh-crosswalk/git-source published-commit path)
        current (slurp path)
        region (fn [text marker]
                 (second (re-find (re-pattern (str "(?s)<!-- " marker ":start -->(.*?)<!-- " marker ":end -->")) text)))
        records (fn [text]
                  (into {} (for [body (str/split text #"(?m)(?=^- \[ \])")
                                 :let [id (second (re-find #"^- \[ \] `([^`]+)`" body))]
                                 :when id]
                             [id (str/trim body)])))
        source-targets (records (region published "platform-implementation-tasks"))
        source-products (records (region published "platform-product-implementation-tasks"))
        current-products (records (region current "platform-product-implementation-tasks"))
        targets (:target-records refinement)
        products (:future-product-records refinement)
        plan (slurp "specs/platform-workflow/plan.md")
        before-changed (set (map :id (get-in previous [:task-comparisons :changed-bodies])))
        after-changed (set (map :id (get-in bridge [:task-comparisons :changed-bodies])))]
    (is (= (:sha256 predecessor) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 predecessor) (sh-inventory/sha256 previous-projection)))
    (is (= (:input-fingerprint predecessor) (:input-fingerprint previous)))
    (is (= 125 (get-in previous [:task-comparisons :unchanged-body-count])))
    (is (= 14 (count before-changed)))
    (is (= #{"hardware" "firmware" "kernel" "devices"} (set (map :id targets))))
    (is (= after-changed (into before-changed (map :id targets))))
    (is (= published-sha256 (sh-inventory/sha256 published)))
    (is (= (set (map :id targets)) (set (sh-crosswalk/stable-task-routing-keys "platform"))))
    (doseq [{:keys [id marker source-body-sha256 current-body-sha256 criterion-ids profile-mapping-ids]} targets
            :let [b (source-targets id)
                  raw (str/trim (subs b (.indexOf b "Declared completion and remaining work:")))
                  carried (second (re-find #"(?s)(Declared completion and remaining work:.*?)\n\nHistorical project paragraph \(unchanged\):" (region current marker)))
                  expected (-> raw
                               (str/replace "specs/requirement-reconciliation/profile-system/gap-completion.edn"
                                            "[the preserved profile-interface context](plan.md#platform-source-context)")
                               (str/replace "specs/requirement-reconciliation/domain-gap-completion.edn"
                                            "[the full preserved Domain context](plan.md#platform-source-context)"))]]
      (is (= source-body-sha256 (sh-inventory/sha256 raw)) id)
      (is (= current-body-sha256 (sh-inventory/sha256 carried)) id)
      (is (= expected carried) id)
      (is (= criterion-ids (mapv second (re-seq #"Criterion `([^`]+)`" carried))) id)
      (is (= profile-mapping-ids (mapv second (re-seq #"Criterion profile-gap/([^ ]+) \(Open\):" carried))) id))
    (is (= 11 (reduce + (map #(count (:criterion-ids %)) targets))))
    (is (= 16 (reduce + (map #(count (:profile-mapping-ids %)) targets))))
    (is (= 11 (count source-products) (count current-products) (count products)))
    (is (= (set (keys source-products)) (set (map :id products))))
    (doseq [{:keys [id body-sha256 status]} products]
      (is (= (source-products id) (current-products id)) id)
      (is (= body-sha256 (sh-inventory/sha256 (current-products id))) id)
      (is (= :open status))
      (is (str/includes? (current-products id) "Status: Open") id))
    (doseq [{:keys [path published-commit published-sha256 preserved-sha256 relation]} (:published-source-contexts refinement)]
      (is (= :exact-file-bytes relation))
      (is (= published-sha256 preserved-sha256
             (sh-inventory/sha256 (sh-crosswalk/git-source published-commit path))) path)
      (is (str/includes? plan (str "https://github.com/burn2delete/uniform/blob/" published-commit "/" path)) path))
    (is (= :not-performed (:architecture-adoption refinement)))
    (is (= :pending-independent-current-review (:future-sh-composition refinement)))
    (is (= :pending-current-repartition-review (:producer-consumer-allocation refinement)))
    (is (= :unassessed (:implementation-acceptance refinement)))
    (is (= refinement (get-in (sh-crosswalk/source-view) [:implementation-readiness-source-version :platform-refinement])))))

(deftest readiness-successor-rejects-stale-review-pins-and-missing-source-routes
  (let [source (sh-crosswalk/source-view)
        bridge sh-crosswalk/readiness-source-version
        domain (first (filter #(= "specs/domains-workflow/tasks.md" (:path %)) (:source-files bridge)))
        path (:path domain)]
    (is (thrown? Exception (sh-crosswalk/adopt-review source sh-crosswalk/readiness-predecessor-mapping-review)))
    (is (thrown? Exception
                 (sh-crosswalk/validate-pinned-sources!
                  (assoc-in source [:applying-source-pins path] (get-in domain [:before :commit]))
                  sh-crosswalk/git-source)))
    (with-redefs [sh-crosswalk/stable-task-routing-keys
                  (dissoc sh-crosswalk/stable-task-routing-keys "evolution")]
      (is (thrown? Exception (sh-crosswalk/source-view))))
    (with-redefs [sh-crosswalk/pinned-evidence-hashes
                  (dissoc sh-crosswalk/pinned-evidence-hashes "docs/phase-02-safety/043-safe14-supply-chain-safety.md")]
      (is (thrown? Exception (sh-crosswalk/source-view))))
    (let [view (sh-crosswalk/build)
          schema (first (filter #(= "schema" (:id %)) (get-in view [:task-authorities "domains"])))
          changed-text (str/replace (slurp path) "Source-specific acceptance for S1-S9 remains open." "Changed source criterion.")]
      (is (thrown? Exception
                   (acceptance-dependencies/current-task-body!
                    (:source schema) (:source-snapshots view)
                    (fn [p] (if (= p path) changed-text (slurp p)))))))))

(deftest future-dependency-successor-keeps-sh-populations-and-exact-task-sources
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version
                           [:math-schema-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:future-dependency-refinement bridge)
        predecessor (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit predecessor) (:path predecessor))
        previous (edn/read-string previous-text)
        previous-projection-text (sh-crosswalk/git-source (:commit predecessor) (:projection-path predecessor))
        previous-projection (edn/read-string previous-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        records (vec (mapcat val (:future-task-authorities view)))
        captured (into {} (map (juxt :id identity) (:task-records refinement)))
        current-map (into {} (for [rows (vals (:task-authorities view)) row rows] [(:id row) row]))
        {:keys [path before after]} (:dependency-source refinement)
        future (acceptance-dependencies/future-task-view (:future-task-authorities view) current-map
                  (:source-snapshots view)
                  #(if (= path %) (sh-crosswalk/git-source (:commit after) path) (slurp %))
                  (:declared-parent-consumers refinement))]
    (is (= (:sha256 predecessor) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 predecessor) (sh-inventory/sha256 previous-projection-text)))
    (is (= (:input-fingerprint predecessor) (:input-fingerprint previous)))
    (is (= (:task-comparisons previous) (:task-comparisons bridge)))
    (is (= (:applying-source-pins previous)
           (dissoc (:applying-source-pins bridge) path)))
    (is (= (:sha256 before) (get-in previous-projection [:source-digests path])
           (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
    (is (nil? (:prior-explicit-applying-pin before)))
    (is (= (:commit after) (get-in view [:applying-source-pins path])))
    (is (= (:sha256 after) (get-in view [:source-digests path])
           (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= 40 (count records) (count captured)))
    (is (= 149 (count current-map)))
    (doseq [row records]
      (is (= (get-in row [:source :sha256]) (get-in captured [(:id row) :body-sha256])))
      (is (= (:source-commit row) (get-in captured [(:id row) :source-commit])))
      (is (false? (:checked row)))
      (is (= :component-future-task-ledger (:authority row))))
    (is (= (:declared-consumers refinement) (get-in future [:dependency-review :declared-consumers])))
    (is (= (:unresolved-consumers refinement) (get-in future [:dependency-review :unresolved-consumers])))
    (is (= :pending-independent-review (:source-entailment refinement)))
    (is (= :unassessed (:implementation-acceptance refinement)))
    (is (= (mapv :task-references (:requirements previous-projection))
           (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence previous-projection) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence previous-projection) (:accepted-current-scoped-evidence view)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :future-dependency-refinement])))))

(deftest math-schema-dependency-successor-preserves-task-versions-and-output-identities
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:release-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:math-schema-dependency-refinement bridge)
        predecessor (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit predecessor) (:path predecessor))
        previous (edn/read-string previous-text)
        previous-projection-text (sh-crosswalk/git-source (:commit predecessor) (:projection-path predecessor))
        previous-projection (edn/read-string previous-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        current (into {} (for [rows (vals (:task-authorities view)) row rows] [(:id row) row]))
        {:keys [path before after]} (:dependency-source refinement)
        dependency-view (acceptance-dependencies/future-task-view (:future-task-authorities view) current
                          (:source-snapshots view) #(if (= path %) (sh-crosswalk/git-source (:commit after) path) (slurp %))
                          (:declared-parent-consumers refinement))
        review (:dependency-review dependency-view)
        math (:math-outputs review)]
    (is (= (:sha256 predecessor) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 predecessor) (sh-inventory/sha256 previous-projection-text)))
    (is (= (:input-fingerprint predecessor) (:input-fingerprint previous)))
    (is (= (:task-comparisons previous) (:task-comparisons bridge)))
    (is (= (:task-authorities previous-projection) (:task-authorities view)))
    (is (= (:future-task-authorities previous-projection) (:future-task-authorities view)))
    (is (= (dissoc (:applying-source-pins previous) path) (dissoc (:applying-source-pins bridge) path)))
    (is (= (:commit before) (get-in previous [:applying-source-pins path])))
    (is (= (:sha256 before) (get-in previous-projection [:source-digests path])
           (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
    (is (= (:commit after) (get-in view [:applying-source-pins path])))
    (is (= (:sha256 after) (get-in view [:source-digests path])
           (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= 27 (count (:declared-consumers refinement))))
    (is (= 13 (count (:unresolved-consumers refinement))))
    (is (= ["math"] (:declared-parent-consumers refinement)))
    (is (= (:declared-consumers refinement) (:declared-consumers review)))
    (is (= (:unresolved-consumers refinement) (:unresolved-consumers review)))
    (is (= (:declared-parent-consumers refinement) (:declared-parent-consumers review)))
    (is (= 48 (count (:math-output-records refinement)) (count (:outputs math))))
    (is (= (:math-output-records refinement)
           (mapv #(-> (select-keys % [:id :task-id :canonical-document :dependency-declaration :output-declaration :source])
                      (update-in [:source :task-source] dissoc :source-snapshot-ref))
                 (:outputs math))))
    (is (= 67 (:literal-output-prerequisites refinement) (reduce + (map count (vals (:adjacency math))))))
    (is (= 28 (:cross-task-output-prerequisites refinement) (count (:cross-task-inputs math))))
    (is (= (:coarse-producer-cycles refinement) (:declared-output-cycles review)))
    (is (= [] (:literal-output-cycles refinement) (:cycles math)))
    (is (= :applicable-accepted-method-not-all-alternatives (:per-claim-proof-method refinement)))
    (is (= :pending-independent-review (:source-entailment refinement)))
    (is (= :unassessed (:implementation-acceptance refinement)))
    (is (= (mapv :task-references (:requirements previous-projection)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence previous-projection) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence previous-projection) (:accepted-current-scoped-evidence view)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :math-schema-dependency-refinement])))))

(deftest release-dependency-successor-preserves-stage-and-task-authorities
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:finish-line-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:release-dependency-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        previous-bridge (edn/read-string previous-text)
        previous-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        previous-projection (edn/read-string previous-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        current (into {} (for [rows (vals (:task-authorities view)) row rows] [(:id row) row]))
        {:keys [path before after]} (:dependency-source refinement)
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) current (:source-snapshots view)
                  #(if (= path %) (sh-crosswalk/git-source (:commit after) path) (slurp %))
                  (:declared-parent-consumers refinement))
        review (:dependency-review result)
        records (get-in review [:release-source-criteria :criteria])]
    (is (= (:sha256 previous) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 previous-projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint previous-bridge)))
    (is (= (:task-comparisons previous-bridge) (:task-comparisons bridge)))
    (is (= (:task-authorities previous-projection) (:task-authorities view)))
    (is (= (:future-task-authorities previous-projection) (:future-task-authorities view)))
    (is (= (dissoc (:applying-source-pins previous-bridge) path) (dissoc (:applying-source-pins bridge) path)))
    (is (= (:sha256 before) (get-in previous-projection [:source-digests path])
           (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
    (is (= (:commit after) (get-in view [:applying-source-pins path])))
    (is (= (:sha256 after) (get-in view [:source-digests path])
           (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))))
    (is (= 40 (count (:declared-consumers review)) (count (:declared-consumers refinement))))
    (is (= 6 (count (:declared-parent-consumers review)) (count (:declared-parent-consumers refinement))))
    (is (= [] (:unresolved-consumers review) (:unresolved-consumers refinement)))
    (is (= 77 (count records) (count (:release-source-records refinement))))
    (is (= (:release-source-records refinement)
           (mapv #(update-in % [:source :task-source] dissoc :source-snapshot-ref) records)))
    (is (= 260 (:selected-result-joins refinement)
           (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= (:coarse-producer-cycles refinement) (:declared-output-cycles review)))
    (is (= [:actual-candidate-and-lineage :raw-comparison :suite-and-rebuild-results
            :independent-intermediate-admission :final-distribution :independent-release]
           (:stage-boundaries refinement)))
    (is (= :pending-independent-review (:source-entailment refinement)))
    (is (= :pending (:future-sh-and-full-language-joins refinement)))
    (is (= :unassessed (:implementation-acceptance refinement)))
    (is (= (mapv :task-references (:requirements previous-projection)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence previous-projection) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence previous-projection) (:accepted-current-scoped-evidence view)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :release-dependency-refinement])))))

(deftest finish-line-successor-binds-new-source-without-rebinding-task-history
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:foundation-tools-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:finish-line-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old (edn/read-string old-text)
        old-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        old-projection (edn/read-string old-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        full "docs/full-language-implementation-gap-map.md"
        doc "docs/acceptance-dependencies.md"
        summarize (fn [row] (assoc (select-keys row [:id :kind :path :source-commit :sha256 :recorded-status])
                                   :source-span (select-keys (:source row) [:start-byte :end-byte :start-line :end-line :sha256])))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (:task-comparisons old) (:task-comparisons bridge)))
    (is (= (:task-authorities old-projection) (:task-authorities view)))
    (is (= (:future-task-authorities old-projection) (:future-task-authorities view)))
    (is (= (dissoc (:applying-source-pins old) doc full) (dissoc (:applying-source-pins bridge) doc full)))
    (is (nil? (get-in old-projection [:source-digests full])))
    (is (= (:source-records refinement) (mapv summarize (:finish-line-sources view))))
    (is (= 56 (count (:finish-line-sources view))))
    (is (= {:full-language-task 36 :phase18-task 7 :self-hosting-milestone 9
            :existing-terminal-gate 2 :shared-proof-contract 1 :shared-stage-contract 1}
           (frequencies (map :kind (:finish-line-sources view)))))
    (is (= 54 (count (:consumers refinement))))
    (is (= 55 (:declaration-rows refinement)))
    (is (= 425 (:selected-results refinement)))
    (is (= :unknown (:platform-product-adoption refinement)))
    (is (= :unchanged-not-240-document-completion (:first-seedless-stop refinement)))
    (is (= :pending (:independent-review refinement)))
    (is (= :unassessed (:source-entailment refinement)))
    (is (= :unassessed (:implementation-acceptance refinement)))
    (doseq [row (:finish-line-sources view)]
      (is (= (:sha256 row) (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit row) (:path row)))) (:id row)))
    (is (= (mapv :task-references (:requirements old-projection)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence old-projection) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence old-projection) (:accepted-current-scoped-evidence view)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :finish-line-refinement])))))

(deftest foundation-tools-successor-restores-criteria-without-changing-task-or-evidence-authority
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:foundation-owner-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:foundation-tools-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old (edn/read-string old-text)
        old-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        path "specs/tools-workflow/tasks.md"
        current (slurp path)
        before (sh-crosswalk/git-source (get-in refinement [:task-source :before :commit]) path)
        restored (str/replace current #"(?s)\n<!-- tools-foundation-(?:lint|inspection):start -->.*?<!-- tools-foundation-(?:lint|inspection):end -->\n\n" "")
        criteria (re-seq #"(?m)^Foundation requirement (gravity/[^:]+):[^\n]+$" current)]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= before restored))
    (is (= 5 (count (:criteria refinement)) (count criteria)))
    (is (= 2 (count (:changed-task-ids refinement))))
    (is (= [] (:new-task-identities refinement)))
    (is (= 12 (count (get-in view [:task-authorities "tools"]))))
    (is (= 121 (get-in old [:task-comparisons :unchanged-body-count])))
    (is (= 119 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= (dissoc (:applying-source-pins old) path) (dissoc (:applying-source-pins bridge) path)))
    (is (= (:task-authorities prior) (assoc (:task-authorities view) "tools" (get-in prior [:task-authorities "tools"]))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= (mapv #(select-keys % [:id :checked :status]) (get-in prior [:task-authorities "tools"]))
           (mapv #(select-keys % [:id :checked :status]) (get-in view [:task-authorities "tools"]))))
    (doseq [{:keys [id criterion-sha256 source-facet-ids]} (:criteria refinement)
            :let [line (first (filter #(str/starts-with? % (str "Foundation requirement " id ":")) (str/split-lines current)))]]
      (is (seq source-facet-ids) id)
      (is (= criterion-sha256 (sh-inventory/sha256 line)) id))
    (doseq [{:keys [path current-sha256]} (:canonical-documents refinement)]
      (is (= current-sha256 (sh-inventory/sha256 (slurp path))) path))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= :pending (:independent-review refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :foundation-tools-refinement])))))

(deftest foundation-owner-successor-keeps-complete-clauses-and-existing-task-authorities
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:c15-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:foundation-owner-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old (edn/read-string old-text)
        old-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        read-current (fn [path] (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) path) (slurp path)))
        changed (set (:changed-task-ids refinement))
        paths (set (map :path (:task-sources refinement)))
        inspection-paths (set (map :path (:inspection-sources refinement)))
        criteria (:criteria refinement)
        original-text (sh-crosswalk/git-source (get-in refinement [:source :published-context-commit])
                                               (get-in refinement [:source :path]))
        original (edn/read-string original-text)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        task-map (fn [v] (into {} (for [rows (vals (:task-authorities v)) r rows] [(:id r) r])))
        before (task-map prior) after (task-map view)]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (get-in refinement [:source :sha256]) (sh-inventory/sha256 original-text)))
    (is (= 13 (count criteria)))
    (is (= 9 (count changed)))
    (is (= 6 (count paths)))
    (is (= 52 (:literal-foundation-context-clauses refinement) (count (:operations original))))
    (is (= [] (:new-task-identities refinement)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= changed (set (for [[id row] before :when (not= (raw prior row) (raw view (after id)))] id))))
    (is (= (apply dissoc (:applying-source-pins old) (concat paths inspection-paths))
           (apply dissoc (:applying-source-pins bridge) (concat paths inspection-paths))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= :pending (:independent-review refinement)))
    (doseq [[id row] before]
      (is (= (select-keys row [:id :checked :status]) (select-keys (after id) [:id :checked :status])) id))
    (doseq [{:keys [path before after]} (:task-sources refinement)
            :let [markers (distinct (map :marker (filter #(= path (:path %)) criteria)))
                  current (read-current path)
                  restored (reduce (fn [text marker]
                                     (str/replace text (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) ""))
                                   current markers)]]
      (is (= restored (sh-crosswalk/git-source (:commit before) path)) path)
      (is (= current (sh-crosswalk/git-source (:commit after) path)) path)
      (is (= (:sha256 after) (get-in view [:source-digests path])) path))
    (doseq [operation (:operations original)
            :let [w (get-in operation [:after :criterion-witness])
                  clause (:criterion-text w) current (slurp (:path w))]]
      ;; Current main may retain the same source criterion in more than one
      ;; owning context.  Preserve the clause and reject only its disappearance.
      (is (pos? (count (re-seq (re-pattern (java.util.regex.Pattern/quote clause)) current))) (:id operation)))
    (doseq [{:keys [id path criterion-sha256 source-facet-ids]} criteria
            :let [clause (first (filter #(str/starts-with? % (str "Foundation requirement " id ":")) (str/split-lines (slurp path))))]]
      (is (seq source-facet-ids) id)
      (is (= criterion-sha256 (sh-inventory/sha256 clause)) id))
    (doseq [{:keys [path current-sha256]} (:canonical-documents refinement)]
      (is (= current-sha256 (sh-inventory/sha256 (slurp path))) path))
    (is (= 6 (count inspection-paths)))
    (doseq [{:keys [path before after kind]} (:inspection-sources refinement)]
      (is (= :source-inspection-not-execution kind))
      (is (= :not-previously-projected-by-crosswalk (:source-version-state before)))
      (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))
                 (get-in bridge [:additional-pinned-evidence-hashes path]))))
    (let [path "specs/lower-workflow/tasks.md" sha (sh-inventory/sha256 (read-current path))]
      (is (= sha (get-in bridge [:inspection-locators path :sha256])
                 (get-in bridge [:additional-pinned-evidence-hashes path]))))
    (is (= refinement (get-in view [:implementation-readiness-source-version :foundation-owner-refinement])))))

(deftest diagnostic-source-successor-preserves-conjunctions-and-owner-boundaries
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:mir-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        read-text (fn [p] (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) p) (slurp p)))
        refinement (:c15-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old (edn/read-string old-text)
        old-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        records (:criteria refinement)
        criterion-keys (set (map :key records))
        task-map (fn [v] (into {} (for [rs (concat (vals (:task-authorities v)) (vals (:future-task-authorities v))) r rs] [(:id r) r])))
        before (task-map prior) after (task-map view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        read-span (fn [text span] (String. (.getBytes text "UTF-8") (int (:start-byte span))
                                          (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        path "specs/diag-workflow/tasks.md"
        text (read-text path)
        restored (reduce (fn [t marker] (str/replace t (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) ""))
                         text (vals (:markers refinement)))
        facets (:source-facets refinement)
        c15 (filterv #(= "C15" (:document-id %)) facets)
        canonical (:canonical-sources refinement)
        source-text (memoize read-text)
        uncovered (fn [rows]
                    (let [p "docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md"
                          bytes (.getBytes (source-text p) "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row rows :let [span (:source-span row)]] (.set covered (int (:start-byte span)) (int (:end-byte span))))
                      (filterv #(and (> (bit-and 0xff (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 3 (count changed)))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= 7 (count records)))
    (is (= 6 (count (filter :restored? records))))
    (is (= #{:structured :identity :record-stream :foundation :views :redaction :tests} criterion-keys))
    (is (= [] (:new-task-identities refinement)))
    (is (= restored (sh-crosswalk/git-source (:commit previous) path)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (doseq [[id r] before]
      (is (= (select-keys r [:id :checked :status]) (select-keys (after id) [:id :checked :status])) id))
    (doseq [{:keys [key task-id historical-witness current-witness]} records
            :let [p (:path current-witness) body (source-text p) span (:source-span current-witness) clause (read-span body span)]]
      (is (= (:raw-sha256 historical-witness) (:sha256 span) (sh-inventory/sha256 clause)) key)
      (is (= (:sha256 current-witness) (sh-inventory/sha256 body)))
      (is (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote clause)) (raw view (after task-id))))) key))
    (is (= 146 (count facets) (count (set (map :id facets)))))
    (is (= 133 (count c15)))
    (is (= {:retained-heading-or-provenance-context 16 :complete-source-predicate-conjunction 117 :foundation-diagnostic-predicate 13}
           (frequencies (map :role facets))))
    (is (= 54 (count (:occurrence-cases refinement))))
    (is (= 38 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals (:occurrence-cases refinement))))))
    (doseq [row (concat facets (vals (:occurrence-cases refinement)))]
      (is (every? criterion-keys (:criterion-keys row))))
    (doseq [f (concat facets (:retained-unjoined-foundation-context refinement))
            :let [body (source-text (:source-path f)) span (:source-span f)]]
      (is (= (:sha256 span) (sh-inventory/sha256 (read-span body span))) (:id f)))
    (is (empty? (uncovered c15)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) c15)))]
      (is (seq (uncovered (remove #(= removed (:id %)) c15)))))
    (is (= 20 (count (:retained-unjoined-foundation-context refinement))))
    (is (= 5 (count (:owner-joins refinement))))
    (doseq [[p root] canonical]
      (is (= (:sha256 root) (sh-inventory/sha256 (source-text p))
                 (get-in refinement [:historical-canonical-sources p :file-sha256]))))
    (is (= :scoped-pass (get-in refinement [:historical-review :verdict])))
    (is (= :not-transferred (get-in refinement [:historical-review :current-acceptance])))
    (is (= :author-assessed-unchanged-predicate-proposal (:source-entailment refinement)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :c15-refinement])))))

(deftest mir-source-successor-binds-all-declared-conditions-without-execution-credit
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:lower-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:mir-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        records (:criteria refinement)
        criterion-keys (set (map :key records))
        task-map (fn [v] (into {} (for [rs (vals (:task-authorities v)) r rs] [(:id r) r])))
        before (task-map prior) after (task-map view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        read-span (fn [text span] (String. (.getBytes text "UTF-8") (int (:start-byte span))
                                          (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        path "specs/mir-workflow/tasks.md" text (slurp path)
        restored (reduce (fn [body marker]
                           (str/replace body (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) ""))
                         text (vals (:markers refinement)))
        source-text (memoize slurp) facets (:source-facets refinement)
        mir-facets (filterv #(= "C11" (:document-id %)) facets)
        uncovered (fn [rows]
                    (let [body (source-text "docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md")
                          bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row rows :let [span (:source-span row)]] (.set covered (int (:start-byte span)) (int (:end-byte span))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint (edn/read-string old-text))))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 5 (count changed)))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= 6 (count records)))
    (is (= #{:construction :facts :ordering :verifier :serialization :consumption} criterion-keys))
    (is (= [] (:new-task-identities refinement)))
    (is (= restored (sh-crosswalk/git-source (:commit previous) path)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (doseq [[id row] before]
      (is (= (select-keys row [:id :checked :status]) (select-keys (after id) [:id :checked :status])) id))
    (doseq [{:keys [key task-id historical-witness current-witness]} records
            :let [body (source-text (:path current-witness)) span (:source-span current-witness) clause (read-span body span)]]
      (is (= (:raw-sha256 historical-witness) (:sha256 span) (sh-inventory/sha256 clause)) key)
      (is (= (:sha256 current-witness) (sh-inventory/sha256 body)))
      (is (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote clause)) (raw view (after task-id))))) key))
    (is (= 148 (count facets) (count (set (map :id facets)))))
    (is (= 134 (count mir-facets)))
    (is (= {:retained-hierarchy-or-provenance-context 17 :complete-source-predicate-conjunction 117 :foundation-MIR-predicate 14}
           (frequencies (map :role facets))))
    (is (= 53 (count (:occurrence-cases refinement))))
    (is (= 36 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals (:occurrence-cases refinement))))))
    (doseq [row (concat facets (vals (:occurrence-cases refinement)))]
      (is (every? criterion-keys (:criterion-keys row))))
    (doseq [row (concat facets (:retained-unjoined-foundation-context refinement))
            :let [body (source-text (:source-path row)) span (:source-span row)]]
      (is (= (:sha256 span) (sh-inventory/sha256 (read-span body span))) (:id row)))
    (is (empty? (uncovered mir-facets)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) mir-facets)))]
      (is (seq (uncovered (remove #(= removed (:id %)) mir-facets)))))
    (is (= 70 (count (:retained-unjoined-foundation-context refinement))))
    (is (= 6 (count (:owner-joins refinement))))
    (doseq [[p root] (:canonical-sources refinement)]
      (is (= (:sha256 root) (sh-inventory/sha256 (source-text p))
                 (get-in refinement [:historical-canonical-sources p :file-sha256]))))
    (is (= :PASS-exact-frozen-source-criterion-conjunctions (get-in refinement [:historical-review :verdict])))
    (is (= :not-transferred (get-in refinement [:historical-review :current-acceptance])))
    (is (some #{:whole-document-entailment} (get-in refinement [:historical-review :qualified-adoption :limits])))
    (is (some #{:held-operation-clearance} (get-in refinement [:historical-review :qualified-adoption :limits])))
    (is (= :author-assessed-unchanged-predicate-proposal (:source-entailment refinement)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :mir-refinement])))))

(deftest lowering-source-successor-preserves-boundary-and-proof-conditions
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:opt-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:lower-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        task-map (fn [v] (into {} (for [rs (vals (:task-authorities v)) r rs] [(:id r) r])))
        before (task-map prior) after (task-map view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        records (:criteria refinement) criterion-keys (set (map :key records))
        source-text (memoize (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp))
        text (source-text "specs/lower-workflow/tasks.md")
        restored (reduce (fn [body marker]
                           (str/replace body (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) ""))
                         text (vals (:markers refinement)))
        read-span (fn [body span] (String. (.getBytes body "UTF-8") (int (:start-byte span))
                                          (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        facets (:source-facets refinement) c14-facets (filterv #(= "C14" (:document-id %)) facets)
        uncovered (fn [rows]
                    (let [body (source-text "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md")
                          bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row rows :let [span (:source-span row)]] (.set covered (int (:start-byte span)) (int (:end-byte span))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint (edn/read-string old-text))))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 5 (count changed) (count records)))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= 3 (count (:newly-amended-original-task-ids refinement))))
    (is (= #{:input :layout :providers :proof :emission} criterion-keys))
    (is (= [] (:new-task-identities refinement)))
    (is (= restored (sh-crosswalk/git-source (:commit previous) "specs/lower-workflow/tasks.md")))
    (let [path "specs/lower-workflow/tasks.md" digest (sh-inventory/sha256 text)]
      (is (= digest (get-in bridge [:inspection-locators path :sha256])
             (get-in bridge [:additional-pinned-evidence-hashes path])
             (get-in refinement [:task-inspection-source :after :sha256]))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (doseq [[id row] before]
      (is (= (select-keys row [:id :checked :status]) (select-keys (after id) [:id :checked :status])) id))
    (doseq [{:keys [key task-id historical-witness current-witness]} records
            :let [body (source-text (:path current-witness)) span (:source-span current-witness) clause (read-span body span)]]
      (is (= (:raw-sha256 historical-witness) (:sha256 span) (sh-inventory/sha256 clause)) key)
      (is (= (:sha256 current-witness) (sh-inventory/sha256 body)))
      (is (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote clause)) (raw view (after task-id))))) key))
    (is (= 145 (count facets) (count (set (map :id facets)))))
    (is (= 122 (count c14-facets)))
    (is (= {:retained-hierarchy-or-provenance-context 16 :complete-source-predicate-conjunction 106 :PERF4-boundary-layout-predicate 23}
           (frequencies (map :role facets))))
    (is (= 49 (count (:occurrence-cases refinement))))
    (is (= 33 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals (:occurrence-cases refinement))))))
    (doseq [row (concat facets (vals (:occurrence-cases refinement)))]
      (is (every? criterion-keys (:criterion-keys row))))
    (doseq [row facets :let [body (source-text (:source-path row)) span (:source-span row)]]
      (is (= (:sha256 span) (sh-inventory/sha256 (read-span body span))) (:id row)))
    (is (empty? (uncovered c14-facets)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) c14-facets)))]
      (is (seq (uncovered (remove #(= removed (:id %)) c14-facets)))))
    (is (= 4 (count (:owner-joins refinement))))
    (doseq [[p root] (:canonical-sources refinement)]
      (is (= (:sha256 root) (sh-inventory/sha256 (source-text p))
             (get-in refinement [:historical-canonical-sources p :file-sha256]))))
    (is (= :pass-exact-source-criterion-scope (get-in refinement [:historical-review :verdict])))
    (is (= :not-inherited (get-in refinement [:historical-review :later-heads])))
    (is (= :not-transferred (get-in refinement [:historical-review :current-acceptance])))
    (is (some #{:complete-PERF4} (get-in refinement [:historical-review :nonclaims])))
    (is (= :not-read-as-review-input-not-cleared-not-retried (get-in refinement [:historical-review :excluded-scope-disposition])))
    (is (= :author-assessed-unchanged-predicate-proposal (:source-entailment refinement)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :lower-refinement])))))

(deftest optimization-source-successor-preserves-pass-ownership-and-evidence-boundaries
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:verification-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:opt-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        read-current (fn [path] (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) path) (slurp path)))
        task-map (fn [v] (into {} (for [rs (vals (:task-authorities v)) r rs] [(:id r) r])))
        before (task-map prior) after (task-map view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        records (:criteria refinement) criterion-keys (set (map :key records))
        criteria (into {} (map (juxt :key identity) records))
        text (read-current "specs/opt-workflow/tasks.md")
        restored (reduce (fn [body marker]
                           (str/replace body (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) ""))
                         text (vals (:markers refinement)))
        source-text (memoize read-current)
        read-span (fn [body span] (String. (.getBytes body "UTF-8") (int (:start-byte span))
                                          (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        facets (:source-facets refinement)
        uncovered (fn [rows]
                    (let [body (source-text "docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md")
                          bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row rows :let [span (:source-span row)]] (.set covered (int (:start-byte span)) (int (:end-byte span))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint (edn/read-string old-text))))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 5 (count changed) (count records)))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= #{:contracts :families :checks :verification :pipeline} criterion-keys))
    (is (= [] (:new-task-identities refinement)))
    (is (= restored (sh-crosswalk/git-source (:commit previous) "specs/opt-workflow/tasks.md")))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (doseq [[id row] before]
      (is (= (select-keys row [:id :checked :status]) (select-keys (after id) [:id :checked :status])) id))
    (doseq [{:keys [key task-id historical-witness current-witness]} records
            :let [body (source-text (:path current-witness)) span (:source-span current-witness) clause (read-span body span)]]
      (is (= (:raw-sha256 historical-witness) (:sha256 span) (sh-inventory/sha256 clause)) key)
      (is (= (:sha256 current-witness) (sh-inventory/sha256 body)))
      (is (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote clause)) (raw view (after task-id))))) key))
    (is (= 120 (count facets) (count (set (map :id facets)))))
    (is (= {"C13" 120} (frequencies (map :document-id facets))))
    (is (= {:retained-hierarchy-or-provenance-context 15 :complete-source-predicate-conjunction 105}
           (frequencies (map :role facets))))
    (is (= 47 (count (:occurrence-cases refinement))))
    (is (= 32 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals (:occurrence-cases refinement))))))
    (doseq [row (concat facets (vals (:occurrence-cases refinement)))]
      (is (every? criterion-keys (:criterion-keys row))))
    (doseq [row facets :let [body (source-text (:source-path row)) span (:source-span row)]]
      (is (= (:sha256 span) (sh-inventory/sha256 (read-span body span))) (:id row)))
    (is (empty? (uncovered facets)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) facets)))]
      (is (seq (uncovered (remove #(= removed (:id %)) facets)))))
    (is (= 4 (count (:owner-joins refinement))))
    (doseq [[p root] (:canonical-sources refinement)]
      (is (= (:sha256 root) (sh-inventory/sha256 (source-text p))
             (get-in refinement [:historical-canonical-sources p :file-sha256]))))
    (is (= (:task-id (criteria :contracts)) (get-in refinement [:owner-boundaries :basic-pass-producer])))
    (is (= (:task-id (criteria :families)) (get-in refinement [:owner-boundaries :family-coverage-consumer])))
    (is (not= (get-in refinement [:owner-boundaries :basic-pass-producer])
              (get-in refinement [:owner-boundaries :family-coverage-consumer])))
    (is (= :consume-actual-basic-pass-products-without-duplicate-implementation (get-in refinement [:owner-boundaries :composition])))
    (is (str/includes? (raw view (after (:task-id (criteria :families)))) "Ownership clarification: consume the actual constant-fold/branch/dead-code/common-expression implementations owned by opt1-execute-pass-contracts."))
    (is (= (:task-id (criteria :verification)) (get-in refinement [:verification-applicability :consumer])))
    (is (= "pass-evidence" (get-in refinement [:verification-applicability :producer])))
    (is (= :per-affected-function-translation-validation-or-stronger-mechanized-pass-proof
           (get-in refinement [:verification-applicability :required])))
    (is (= :affected-function-evidence-not-whole-C18-acceptance (get-in refinement [:verification-applicability :scope])))
    (is (str/includes? (raw view (after (:task-id (criteria :verification)))) "Current C18 application:"))
    (let [source (get-in refinement [:verification-applicability :source])]
      (is (= (:sha256 source) (sh-inventory/sha256 (source-text (:path source))))))
    (let [path "specs/opt-workflow/plan.md"]
      (is (= (sh-inventory/sha256 (source-text path)) (get-in bridge [:inspection-locators path :sha256])
             (get-in bridge [:additional-pinned-evidence-hashes path]))))
    (is (= :PASS-exact-frozen-C13-source-criterion-conjunctions (get-in refinement [:historical-review :verdict])))
    (is (= :none (get-in refinement [:historical-review :later-heads :acceptance])))
    (is (= :not-transferred (get-in refinement [:historical-review :current-acceptance])))
    (is (some #{:whole-task-completion} (get-in refinement [:historical-review :nonclaims])))
    (is (some #{:held-Metadata372-Policy14-fixed-width-OWN-ACC08-clearance} (get-in refinement [:historical-review :nonclaims])))
    (is (= :author-assessed-preserved-predicates-with-explicit-owner-joins (:source-entailment refinement)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :opt-refinement])))))

(deftest verification-source-successor-preserves-existing-witnesses-and-matrix-minima
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:incremental-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
                   sh-crosswalk/readiness-source-version)
        refinement (:verification-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old-projection (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-projection)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint)))
                 (sh-crosswalk/build))
        task-map (fn [v] (into {} (for [rs (vals (:task-authorities v)) r rs] [(:id r) r])))
        before (task-map prior) after (task-map view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        source-text (memoize (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp))
        text (source-text "specs/compiler-api-workflow/tasks.md")
        restored (str/replace text #"(?s)\n<!-- compiler-api-canonical-verification-records:start -->.*?<!-- compiler-api-canonical-verification-records:end -->\n\n" "")
        read-span (fn [body span] (String. (.getBytes body "UTF-8") (int (:start-byte span))
                                          (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        records (:criteria refinement) facets (:source-facets refinement)
        uncovered (fn [rows]
                    (let [body (source-text "docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md")
                          bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row rows :let [span (:source-span row)]] (.set covered (int (:start-byte span)) (int (:end-byte span))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 2 (count changed)))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= ["pass-evidence"] (:newly-amended-original-task-ids refinement)))
    (is (= [] (:new-task-identities refinement)))
    (is (= restored (sh-crosswalk/git-source (:commit previous) "specs/compiler-api-workflow/tasks.md")))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (get-in prior [:task-authorities "backends"]) (get-in view [:task-authorities "backends"])))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (doseq [[id row] before] (is (= (select-keys row [:id :checked :status]) (select-keys (after id) [:id :checked :status])) id))
    (is (= #{:existing :records :backend} (set (map :key records))))
    (is (= {:existing true :records false :backend true} (into {} (map (juxt :key :preserved-current-before?) records))))
    (doseq [{:keys [key historical-witness current-witness]} records
            :let [body (source-text (:path current-witness)) span (:source-span current-witness) clause (read-span body span)]]
      (is (= (:raw-sha256 historical-witness) (:sha256 span) (sh-inventory/sha256 clause)) key)
      (is (= (:sha256 current-witness) (sh-inventory/sha256 body))))
    (is (= 101 (count facets) (count (set (map :id facets)))))
    (is (= {:retained-hierarchy-or-provenance-context 15 :complete-source-predicate-conjunction 86} (frequencies (map :role facets))))
    (is (= 45 (count (:occurrence-cases refinement))))
    (is (= 30 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals (:occurrence-cases refinement))))))
    (doseq [row facets :let [span (:source-span row)]] (is (= (:sha256 span) (sh-inventory/sha256 (read-span (source-text (:source-path row)) span))) (:id row)))
    (is (empty? (uncovered facets)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) facets)))]
      (is (seq (uncovered (remove #(= removed (:id %)) facets)))))
    (is (= 7 (count (:owner-joins refinement))))
    (is (= :task-reference-only-exact-criterion-join-pending (get-in refinement [:owner-joins "backend/conformance-execution-pack" :captured-owner-state])))
    (let [consumer (:opt-consumer refinement) w (:current-witness consumer) clause (read-span (source-text (:path w)) (:source-span w))]
      (is (= :checked-per-affected-function-translation-validation-matrix-minimum (:required consumer)))
      (is (= :optional-unless-applicable-policy-requires-never-a-label-waiver (:stronger-evidence consumer)))
      (is (= (get-in w [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (str/includes? clause "cannot waive checked matrix minimum evidence"))
      (is (not (str/includes? clause "or a stronger mechanized pass proof"))))
    (is (= :scoped-pass (get-in refinement [:historical-review :verdict])))
    (is (= :not-transferred (get-in refinement [:historical-review :current-acceptance])))
    (is (= :not-accepted (get-in refinement [:historical-review :qualified-adoption :whole-current-head])))
    (is (= :no-clearance-or-retry (get-in refinement [:historical-review :qualified-adoption :held-actions])))
    (is (= :excluded (get-in refinement [:historical-review :qualified-applicability :B6-containing-file-drift])))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :verification-refinement])))))

(deftest incremental-source-successor-reconstructs-criteria-without-duplicating-foundation
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:effects-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) refinement (:incremental-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old-projection (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-projection)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        task-map (fn [v] (into {} (for [rs (vals (:task-authorities v)) r rs] [(:id r) r])))
        before (task-map prior) after (task-map view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        source-text (memoize (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp))
        text (source-text "specs/build-workflow/tasks.md")
        restored (reduce (fn [body marker] (str/replace body (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) "")) text (vals (:markers refinement)))
        read-span (fn [body span] (String. (.getBytes body "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        records (:criteria refinement) facets (:source-facets refinement)
        uncovered (fn [rows]
                    (let [body (source-text "docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md")
                          bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row rows :let [span (:source-span row)]] (.set covered (int (:start-byte span)) (int (:end-byte span))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 2 (count changed)))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= 1 (count (:newly-amended-original-task-ids refinement))))
    (is (= [] (:new-task-identities refinement)))
    (is (= restored (sh-crosswalk/git-source (:commit previous) "specs/build-workflow/tasks.md")))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (doseq [[id row] before] (is (= (select-keys row [:id :checked :status]) (select-keys (after id) [:id :checked :status])) id))
    (is (= #{:existing :records :rebuild} (set (map :key records))))
    (is (= {:existing 1 :records 0 :rebuild 2} (into {} (map (juxt :key :preserved-fragment-count) records))))
    (doseq [{:keys [key task-id historical-witness current-fragments join-separator reconstructed-sha256]} records
            :let [parts (mapv #(read-span (source-text (:path %)) (:source-span %)) current-fragments)
                  reconstructed (str/join join-separator parts)]]
      (is (= (:raw-sha256 historical-witness) reconstructed-sha256 (sh-inventory/sha256 reconstructed)) key)
      (is (not= reconstructed-sha256 (sh-inventory/sha256 (str/join join-separator (butlast parts)))))
      (doseq [[w part] (map vector current-fragments parts)]
        (is (= (:sha256 w) (sh-inventory/sha256 (source-text (:path w)))))
        (is (= (get-in w [:source-span :sha256]) (sh-inventory/sha256 part)))
        (is (str/includes? (raw view (after task-id)) part))))
    (is (= 1 (count (re-seq #"Foundation requirement gravity/artifact-graph-reproducibility:" text))))
    (is (= 114 (count facets) (count (set (map :id facets)))))
    (is (= {:retained-hierarchy-or-provenance-context 15 :complete-source-predicate-conjunction 99} (frequencies (map :role facets))))
    (is (= 44 (count (:occurrence-cases refinement))))
    (is (= 29 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals (:occurrence-cases refinement))))))
    (doseq [row facets :let [span (:source-span row)]] (is (= (:sha256 span) (sh-inventory/sha256 (read-span (source-text (:source-path row)) span))) (:id row)))
    (is (empty? (uncovered facets)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) facets)))] (is (seq (uncovered (remove #(= removed (:id %)) facets)))))
    (is (= 6 (count (:owner-joins refinement))))
    (is (= :scoped-pass (get-in refinement [:historical-review :verdict])))
    (is (= :not-transferred (get-in refinement [:historical-review :current-acceptance])))
    (is (= :not-accepted (get-in refinement [:historical-review :qualified-adoption :whole-current-head])))
    (is (= :not-accepted (get-in refinement [:historical-review :qualified-adoption :full-dependency-documents])))
    (is (= :no-clearance-or-retry (get-in refinement [:historical-review :qualified-adoption :held-actions])))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :incremental-refinement])))))

(deftest plugin-plan-reuses-frozen-review-and-preserves-all-current-predicates
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:effects-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        refinement (:plugin-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        review-pin (:retained-source-review refinement)
        review-text (read-text (:path review-pin)) review (edn/read-string review-text)
        canonical (:canonical-source refinement) body (read-text (:path canonical))
        read-span (fn [text span] (String. (.getBytes text "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        uncovered (fn [facets]
                    (let [bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row facets :let [w (:source-witness row)]] (.set covered (int (:start-byte w)) (int (:end-byte w))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))
        facets (:facet-verdicts review)]
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:sha256 review-pin) (sh-inventory/sha256 review-text)))
    (is (= review-text (sh-crosswalk/git-source (:source-commit review-pin) (:path review-pin))))
    (is (= (:file-sha256 canonical) (sh-inventory/sha256 body)))
    (is (= 108 (count facets) (count (set (map :id facets)))))
    (is (= {:retained-hierarchy-or-provenance-context 15 :complete-source-predicate-conjunction 93} (frequencies (map :role facets))))
    (is (= 45 (count (:occurrence-verdicts review))))
    (is (= 30 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals (:occurrence-verdicts review))))))
    (is (= 6 (count (:owner-verdicts review))))
    (doseq [facet facets :let [w (:source-witness facet)]]
      (is (= (:raw-sha256 w) (sh-inventory/sha256 (read-span body w))) (:id facet)))
    (is (empty? (uncovered facets)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) facets)))]
      (is (seq (uncovered (remove #(= removed (:id %)) facets)))))
    (is (= #{:existing :records :pipeline} (set (map :key (:criteria refinement)))))
    (doseq [{:keys [key historical-witness current-witness preserved-current-before?]} (:criteria refinement)
            :let [text (read-text (:path current-witness)) clause (read-span text (:source-span current-witness))]]
      (is preserved-current-before? key)
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (:raw-sha256 historical-witness) (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (= text (sh-crosswalk/git-source (:source-commit current-witness) (:path current-witness))))
      (is (= -1 (.indexOf text clause (inc (.indexOf text clause)))))
      (is (not= (:raw-sha256 historical-witness) (sh-inventory/sha256 (subs clause 1)))))
    (is (= (:task-authorities prior) (:task-authorities view)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= [] (:new-task-identities refinement) (:changed-task-ids refinement)))
    (is (= (:subject review) (get-in refinement [:historical-review :subject])))
    (is (= :scoped-pass (:verdict review)))
    (is (= :not-transferred (get-in refinement [:historical-review :current-acceptance])))
    (is (= :not-accepted (get-in refinement [:historical-review :qualified-adoption :whole-current-head])))
    (is (= :no-clearance-or-retry (get-in refinement [:historical-review :qualified-adoption :held-actions])))
    (is (= :not-conferred (get-in refinement [:historical-review :qualified-applicability :unreviewed-C12 :acceptance])))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :plugin-refinement])))))

(deftest domain-ir-plan-preserves-predicates-and-exposes-actual-family-composition
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:effects-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        refinement (:domain-ir-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        review-pin (:retained-source-review refinement)
        review-text (read-text (:path review-pin)) review (edn/read-string review-text)
        canonical (:canonical-source refinement) body (read-text (:path canonical))
        read-span (fn [text span] (String. (.getBytes text "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        uncovered (fn [facets]
                    (let [bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row facets :let [w (:source-witness row)]] (.set covered (int (:start-byte w)) (int (:end-byte w))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))
        facets (:facet-verdicts review)]
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:sha256 review-pin) (sh-inventory/sha256 review-text)))
    (is (= review-text (sh-crosswalk/git-source (:source-commit review-pin) (:path review-pin))))
    (is (= (:file-sha256 canonical) (sh-inventory/sha256 body)))
    (is (= 115 (count facets) (count (set (map :id facets)))))
    (is (= {:retained-hierarchy-or-provenance-context 16 :complete-source-predicate-conjunction 99} (frequencies (map :role facets))))
    (is (= 50 (count (:occurrence-verdicts review))))
    (is (= 34 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals (:occurrence-verdicts review))))))
    (is (= 6 (count (:owner-verdicts review))))
    (doseq [facet facets :let [w (:source-witness facet)]]
      (is (= (:raw-sha256 w) (sh-inventory/sha256 (read-span body w))) (:id facet)))
    (is (empty? (uncovered facets)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) facets)))]
      (is (seq (uncovered (remove #(= removed (:id %)) facets)))))
    (is (= #{:existing :records :proof} (set (map :key (:criteria refinement)))))
    (doseq [{:keys [key historical-witness current-witness preserved-current-before?]} (:criteria refinement)
            :let [text (read-text (:path current-witness)) clause (read-span text (:source-span current-witness))]]
      (is preserved-current-before? key)
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (:raw-sha256 historical-witness) (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (= text (sh-crosswalk/git-source (:source-commit current-witness) (:path current-witness))))
      (is (= -1 (.indexOf text clause (inc (.indexOf text clause)))))
      (is (not= (:raw-sha256 historical-witness) (sh-inventory/sha256 (subs clause 1)))))
    (is (= (:task-authorities prior) (:task-authorities view)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= [] (:new-task-identities refinement) (:changed-task-ids refinement)))
    (is (= (:subject review) (get-in refinement [:historical-review :subject])))
    (is (= :scoped-pass (:verdict review)))
    (is (= :not-transferred (get-in refinement [:historical-review :current-acceptance])))
    (is (= :not-accepted (get-in refinement [:historical-review :qualified-adoption :whole-current-head])))
    (is (= :no-clearance-or-retry (get-in refinement [:historical-review :qualified-adoption :held-actions])))
    (is (some #{:C18-source-semantics} (get-in refinement [:historical-review :exclusions])))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (let [tasks (into {} (for [rows (vals (:task-authorities view)) row rows] [(:id row) row]))
          dep (:dependency-source refinement)
          read-dependency (fn [path] (if (= (:path dep) path) (sh-crosswalk/git-source (get-in dep [:after :commit]) path) (read-text path)))
          result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-dependency (:required-parent-consumers refinement))
          dependency-review (:dependency-review result)
          route (first (filter #(= "domain-ir-boundaries" (:consumer %)) (:routes dependency-review)))
          route-source (read-dependency (:path dep))]
      (is (= (:sha256 (:after dep)) (sh-inventory/sha256 route-source)))
      (is (= route-source (sh-crosswalk/git-source (:commit (:after dep)) (:path dep))))
      (is (= (:composition-route refinement) (update route :source dissoc :raw)))
      (is (= 13 (count (:composition-results route))))
      (is (empty? (:start-results route)))
      (is (= 9 (count (:baseline-family-owners refinement))))
      (is (= [:efir :schema :workflow :hdl] (:conformance-minimum refinement)))
      (is (= 7 (count (:declared-parent-consumers dependency-review))))
      (is (= (:required-parent-consumers refinement) (:required-parent-consumers dependency-review)))
      (is (empty? (:unresolved-consumers dependency-review)))
      (is (= 273 (:selected-result-joins refinement) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
      (is (= :unassessed (:implementation-acceptance dependency-review))))
    (is (= refinement (get-in view [:implementation-readiness-source-version :domain-ir-refinement])))))

(deftest effects-source-successor-preserves-original-tasks-and-complete-conjunctions
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:ownership-refinement :predecessor-readiness-successor])
        refinement (:effects-refinement sh-crosswalk/readiness-source-version)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        task-map (fn [v] (into {} (for [rs (vals (:task-authorities v)) r rs] [(:id r) r])))
        before (task-map prior) after (task-map view)
        raw (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        changed (set (for [[id row] before :when (not= (raw prior row) (raw view (after id)))] id))
        text (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) "specs/effects-workflow/tasks.md") (slurp "specs/effects-workflow/tasks.md"))
        stripped (reduce (fn [body marker] (str/replace body (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) "")) text (vals (:markers refinement)))
        read-span (fn [body span] (String. (.getBytes body "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        facets (:source-facets refinement)
        uncovered (fn [rows]
                    (let [body (slurp "docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md")
                          bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row rows :let [w (:source-span row)]] (.set covered (int (:start-byte w)) (int (:end-byte w))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= changed (set (:changed-task-ids refinement)) (set (:newly-amended-original-task-ids refinement))))
    (is (= 5 (count changed)))
    (is (= [] (:new-task-identities refinement)))
    (is (= stripped (sh-crosswalk/git-source (:commit previous) "specs/effects-workflow/tasks.md")))
    (doseq [[id row] before] (is (= (:checked row) (:checked (after id))) id))
    (is (= #{:computation :propagation :legality :replay :handoff} (set (map :key (:criteria refinement)))))
    (doseq [{:keys [key task-id historical-witness current-witness]} (:criteria refinement)
            :let [clause (read-span text (:source-span current-witness))
                  old (sh-crosswalk/git-source (:commit historical-witness) (:path historical-witness))]]
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (:file-sha256 historical-witness) (sh-inventory/sha256 old)))
      (is (= clause (read-span old historical-witness)))
      (is (= (:raw-sha256 historical-witness) (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 clause)) key)
      (is (str/includes? (raw view (after task-id)) clause))
      (is (not= (:raw-sha256 historical-witness) (sh-inventory/sha256 (subs clause 1)))))
    (is (= #{:replay :handoff} (set (map :key (:additional-contexts refinement)))))
    (doseq [{:keys [historical-witness current-witness acceptance]} (:additional-contexts refinement)
            :let [clause (read-span text (:source-span current-witness))
                  old (sh-crosswalk/git-source (:commit historical-witness) (:path historical-witness))]]
      (is (= (:file-sha256 historical-witness) (sh-inventory/sha256 old)))
      (is (= clause (read-span old (:source-span historical-witness))))
      (is (= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (= :not-transferred acceptance)))
    (is (= 103 (count facets) (count (set (map :id facets)))))
    (is (= {:retained-hierarchy-or-provenance-context 16 :complete-source-predicate-conjunction 87} (frequencies (map :role facets))))
    (is (= 50 (count (:occurrence-cases refinement))))
    (is (= 34 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals (:occurrence-cases refinement))))))
    (is (= 5 (count (:owner-joins refinement))))
    (doseq [row facets :let [body (slurp (:source-path row)) w (:source-span row)]]
      (is (= (:sha256 w) (sh-inventory/sha256 (read-span body w))) (:id row)))
    (is (empty? (uncovered facets)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) facets)))]
      (is (seq (uncovered (remove #(= removed (:id %)) facets)))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= :pass-qualified-frozen-C8-source-conjunctions-only (get-in refinement [:historical-review :verdict])))
    (is (= :not-transferred (get-in refinement [:historical-review :current-acceptance])))
    (is (= :unchanged-no-equivalent-substitution (get-in refinement [:historical-review :held-actions])))
    (is (some #{:full-L6-entailment} (get-in refinement [:historical-review :nonclaims])))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :effects-refinement])))))

(deftest ownership-source-successor-reuses-census-identities-and-complete-occurrences
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:safety-refinement :predecessor-readiness-successor])
        refinement (:ownership-refinement sh-crosswalk/readiness-source-version)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        task-map (fn [v] (into {} (for [rs (vals (:task-authorities v)) row rs] [(:id row) row])))
        before (task-map prior) after (task-map view)
        raw (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        changed (set (for [[id row] before :when (not= (raw prior row) (raw view (after id)))] id))
        text (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) "specs/legality-workflow/tasks.md") (slurp "specs/legality-workflow/tasks.md"))
        stripped (reduce (fn [body marker] (str/replace body (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) "")) text (vals (:markers refinement)))
        read-span (fn [body span] (String. (.getBytes body "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        body (slurp "docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md")
        facets (:source-facets refinement) cases (:occurrence-cases refinement)
        clause-keys (set (map :key (:criteria refinement)))
        joined? (fn [cases] (and (= (set (map :source-occurrence-id facets)) (set (keys cases)))
                                (every? #(or (= :retained-hierarchy-or-provenance-context (:role %))
                                             (and (seq (:criterion-keys %)) (every? clause-keys (:criterion-keys %)))) (vals cases))))
        uncovered (fn [rows]
                    (let [bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row rows :let [w (:source-span row)]] (.set covered (int (:start-byte w)) (int (:end-byte w))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= 4 (count changed)))
    (is (= 3 (count (:newly-amended-original-task-ids refinement))))
    (is (= [] (:new-task-identities refinement)))
    (is (= stripped (sh-crosswalk/git-source (:commit previous) "specs/legality-workflow/tasks.md")))
    (doseq [[id row] before] (is (= (:checked row) (:checked (after id))) id))
    (is (= #{:analysis :checks :handoff :conformance} clause-keys))
    (doseq [{:keys [task-id current-witness]} (:criteria refinement)
            :let [clause (read-span text (:source-span current-witness))]]
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (str/includes? (raw view (after task-id)) clause))
      (is (not= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 (subs clause 1)))))
    (is (= 130 (count facets) (count (set (map :id facets)))))
    (is (= 39 (count (filter #(= :context (:census-kind %)) facets))))
    (is (= {:retained-hierarchy-or-provenance-context 16 :complete-source-predicate-conjunction 114} (frequencies (map :role facets))))
    (is (= 50 (count cases)))
    (is (= 34 (count (filter #(= :complete-source-predicate-conjunction (:role %)) (vals cases)))))
    (is (joined? cases))
    (is (not (joined? (dissoc cases (:source-occurrence-id (first facets))))))
    (doseq [row facets :let [span (:source-span row) c (cases (:source-occurrence-id row)) cs (:source-span c)]]
      (is (= (:sha256 span) (sh-inventory/sha256 (read-span body span))) (:id row))
      (is (<= (:start-byte cs) (:start-byte span) (:end-byte span) (:end-byte cs)))
      (is (= (:criterion-keys c) (:criterion-keys row))))
    (doseq [[_ c] cases :let [source (read-span body (:source-span c))]]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 source)))
      (when (= :retained-hierarchy-or-provenance-context (:role c))
        (is (or (str/starts-with? source "#") (str/starts-with? source "Sequence:")))))
    (is (empty? (uncovered facets)))
    (let [removed (:id (first (filter #(= :complete-source-predicate-conjunction (:role %)) facets)))]
      (is (seq (uncovered (remove #(= removed (:id %)) facets)))))
    (is (= 7 (count (:owner-joins refinement))))
    (doseq [[_ row] (:owner-joins refinement)]
      (is (contains? after (:task-id row)))
      (is (every? clause-keys (:criterion-keys row)))
      (is (= :unassessed (:whole-attached-owner-predicate row)))
      (is (= :none (:implementation-credit row))))
    (is (= :preserved-identity-and-conservation-only-no-aggregate-adoption (get-in refinement [:source-census :authority])))
    (is (= :not-conferred (get-in refinement [:historical-review :current-acceptance])))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :ownership-refinement])))))

(deftest ownership-dependencies-bind-selected-products-without-changing-task-authority
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:safety-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        refinement (:ownership-dependency-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        previous-bridge (edn/read-string previous-text)
        previous-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string previous-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        tasks (into {} (for [rows (vals (:task-authorities view)) row rows] [(:id row) row]))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                 (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp) (:required-parent-consumers refinement))
        review (:dependency-review result)
        consumers (set (:consumer-task-ids refinement))
        routes (filterv #(consumers (:consumer %)) (:routes review))
        dep (:dependency-source refinement) plan (:plan-source refinement)
        route-meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        all-ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        old-routes (acceptance-dependencies/future-dependency-routes
                     (sh-crosswalk/git-source (:commit previous) (:path dep))
                     all-ids all-ids)]
    (is (= (:sha256 previous) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 previous-projection-text)))
    (is (= (:task-comparisons previous-bridge) (:task-comparisons bridge)))
    (is (= (:ownership-refinement previous-bridge) (:ownership-refinement bridge)))
    (is (= (:task-authorities prior) (:task-authorities view)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= [] (:new-task-identities refinement) (:changed-task-ids refinement) (:changed-prior-route-meanings refinement)))
    (is (= 4 (count consumers) (count routes)))
    (is (= (:routes refinement) (mapv #(update % :source dissoc :raw) routes)))
    (is (= 27 (:selected-producer-result-joins refinement) (reduce + (map (comp count :composition-results) routes))))
    (is (= [4 5 6 12] (sort (map (comp count :composition-results) routes))))
    (is (every? #(empty? (:start-results %)) routes))
    (is (= 47 (:preserved-consumer-route-count refinement) (count old-routes)))
    (is (= (into {} (map (juxt :consumer route-meaning) old-routes))
           (into {} (map (juxt :consumer route-meaning) (remove #(consumers (:consumer %)) (:routes review))))))
    (is (= 300 (:all-selected-result-joins refinement)
           (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 11 (count (:declared-parent-consumers refinement)) (count (:declared-parent-consumers review))))
    (is (= (:required-parent-consumers refinement) (:required-parent-consumers review)))
    (is (empty? (:unresolved-consumers refinement)))
    (is (= [6 8] (mapv count (:coarse-producer-cycles refinement)) (mapv count (:declared-output-cycles review))))
    (doseq [record [dep plan] :let [p (:path record) after (:after record) text (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) p) (slurp p))]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p))))
    (is (= :C9-selected-contracts-only-not-whole-Legality-dependency-entailment (:consumer-route-scope refinement)))
    (is (= :declared-interfaces-no-adopted-result-prerequisites (:ordinary-construction refinement)))
    (is (= :valid-unoptimized-verified-MIR-remains-legal-no-task-completion-gate (:program-legality refinement)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :ownership-dependency-refinement])))))

(deftest safety-source-successor-binds-complete-sections-without-promoting-implementation
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:root-safety-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        refinement (:safety-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        previous-bridge (edn/read-string (sh-crosswalk/git-source (:commit previous) (:path previous)))
        task-map (fn [v] (into {} (for [rows (vals (:task-authorities v)) row rows] [(:id row) row])))
        before (task-map prior) after (task-map view)
        raw (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        changed (set (for [[id row] before :when (not= (raw prior row) (raw view (after id)))] id))
        text (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) "specs/legality-workflow/tasks.md") (slurp "specs/legality-workflow/tasks.md"))
        stripped (reduce (fn [body marker] (str/replace body (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) "")) text (vals (:markers refinement)))
        body (slurp "docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md")
        read-span (fn [text span] (String. (.getBytes text "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        facets (:source-facets refinement) cases (:occurrence-cases refinement)
        sections (:section-conjunctions refinement) clause-keys (set (map :key (:criteria refinement)))
        semantic? #(= :complete-source-predicate-conjunction (:role %))
        section-valid? (fn [sections]
                         (and (= (set (map :source-heading (filter semantic? (vals cases)))) (set (keys sections)))
                              (every? (fn [[heading section]]
                                        (and (= (set (:occurrence-ids section))
                                                (set (for [[id c] cases :when (and (semantic? c) (= heading (:source-heading c)))] id)))
                                             (seq (:criterion-keys section)) (every? clause-keys (:criterion-keys section)))) sections)))
        uncovered (fn [rows]
                    (let [bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [row rows :let [s (:source-span row)]] (.set covered (int (:start-byte s)) (int (:end-byte s))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:sha256 previous) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit previous) (:path previous)))))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 3 (count changed)))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= [] (:new-task-identities refinement) (:newly-amended-original-task-ids refinement)))
    (is (= stripped (sh-crosswalk/git-source (:commit previous) "specs/legality-workflow/tasks.md")))
    (doseq [[id row] before] (is (= (:checked row) (:checked (after id))) id))
    (is (= 84 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 55 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= (:ownership-refinement previous-bridge) (:ownership-refinement bridge)))
    (is (= (:ownership-dependency-refinement previous-bridge) (:ownership-dependency-refinement bridge)))
    (is (= #{:inventory :decisions :preservation :conformance} clause-keys))
    (is (= 3 (count (set (map :task-id (:criteria refinement))))))
    (doseq [{:keys [task-id current-witness]} (:criteria refinement)
            :let [clause (read-span text (:source-span current-witness))]]
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (str/includes? (raw view (after task-id)) clause)))
    (is (= 122 (count facets) (count (set (map :id facets)))))
    (is (= 33 (count (filter #(= :context (:census-kind %)) facets))))
    (is (= {:retained-hierarchy-or-provenance-context 15 :complete-source-predicate-conjunction 107} (frequencies (map :role facets))))
    (is (= 47 (count cases)))
    (is (= 32 (count (filter semantic? (vals cases)))))
    (is (= (set (map :source-occurrence-id facets)) (set (keys cases))))
    (is (= 13 (count sections)))
    (is (section-valid? sections))
    (is (not (section-valid? (update-in sections ["Runtime Check Records" :occurrence-ids] pop))))
    (is (not (section-valid? (assoc-in sections ["Proof Obligations" :criterion-keys] [:unknown]))))
    (doseq [row facets :let [s (:source-span row) c (cases (:source-occurrence-id row)) cs (:source-span c)]]
      (is (= (:sha256 s) (sh-inventory/sha256 (read-span body s))) (:id row))
      (is (<= (:start-byte cs) (:start-byte s) (:end-byte s) (:end-byte cs)))
      (is (= (:criterion-keys c) (:criterion-keys row)))
      (when (semantic? c) (is (every? clause-keys (:criterion-keys c)))))
    (doseq [[_ c] cases :let [source (read-span body (:source-span c))]]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 source)))
      (when-not (semantic? c) (is (or (str/starts-with? source "#") (str/starts-with? source "Sequence:")))))
    (is (empty? (uncovered facets)))
    (let [removed (:id (first (filter semantic? facets)))] (is (seq (uncovered (remove #(= removed (:id %)) facets)))))
    (is (= 6 (count (:owner-joins refinement))))
    (doseq [[_ row] (:owner-joins refinement)]
      (is (contains? after (:task-id row)))
      (is (every? clause-keys (:criterion-keys row)))
      (is (= :unassessed (:whole-attached-owner-predicate row)))
      (is (= :none (:implementation-credit row))))
    (is (= :preserved-identity-and-conservation-only-no-aggregate-adoption (get-in refinement [:source-census :authority])))
    (is (= :not-conferred (get-in refinement [:historical-review :current-acceptance])))
    (is (= :C9-rows-preserved-additional-C10-producer-results-explicitly-pending (:dependency-review refinement)))
    (is (= :static-selected-definitions-and-explicit-policy-nonclaims-not-execution (get-in refinement [:implementation-source-inspection :scope])))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :safety-refinement])))))

(deftest safety-dependencies-extend-three-routes-with-actual-selected-results
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:root-safety-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        refinement (:safety-dependency-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        previous-bridge (edn/read-string previous-text)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        tasks (into {} (for [rows (vals (:task-authorities view)) row rows] [(:id row) row]))
        source-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) source-text (:required-parent-consumers refinement))
        review (:dependency-review result) consumers (set (:consumer-task-ids refinement))
        selected (filterv #(consumers (:consumer %)) (:routes review))
        all-ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        dep (:dependency-source refinement) plan (:plan-source refinement)
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) all-ids all-ids)
        old-selected (into {} (for [r old-routes :when (consumers (:consumer r))] [(:consumer r) r]))
        route-meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        without-selected (str/join "\n" (remove (fn [line] (some #(str/starts-with? line (str "| `" % "` |")) consumers))
                                                  (str/split-lines (source-text (:path dep)))))
        missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                  #(if (= (:path dep) %) without-selected (source-text %)) (:required-parent-consumers refinement))]
    (is (= (:sha256 previous) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:task-authorities prior) (:task-authorities view)))
    (is (= (:task-comparisons previous-bridge) (:task-comparisons bridge)))
    (is (= (:safety-refinement previous-bridge) (:safety-refinement bridge)))
    (is (= (:source-input-commit previous-bridge) (:source-input-commit bridge)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= [] (:changed-task-ids refinement) (:new-task-identities refinement)))
    (is (= 3 (count selected) (count consumers) (count old-selected)))
    (is (= consumers (set (:changed-prior-route-meanings refinement)) (set (get-in missing [:dependency-review :unresolved-consumers]))))
    (is (= (:routes refinement) (mapv #(update % :source dissoc :raw) selected)))
    (is (= (:predecessor-C9-routes refinement) (mapv #(update % :source dissoc :raw) (filter #(consumers (:consumer %)) old-routes))))
    (is (= 48 (:preserved-consumer-route-count refinement) (count (remove #(consumers (:consumer %)) old-routes))))
    (is (= (into {} (map (juxt :consumer route-meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer route-meaning) (remove #(consumers (:consumer %)) (:routes review))))))
    (is (= [4 17 21] (sort (map (comp count :composition-results) selected))))
    (is (= 22 (:preserved-C9-input-joins refinement) (reduce + (map (comp count :composition-results) (vals old-selected)))))
    (is (= 42 (:selected-consumer-result-joins refinement) (reduce + (map (comp count :composition-results) selected))))
    (is (= 20 (:new-selected-producer-result-joins refinement)
           (reduce + (for [r selected]
                       (count (remove (set (:composition-results (old-selected (:consumer r))))
                                      (:composition-results r)))))))
    (doseq [r selected :let [o (old-selected (:consumer r)) retained (first (str/split (:selected-output-contract o) #" This row does not claim"))]]
      (is (= [] (:start-results r) (:start-results o)))
      (is (every? (set (:composition-results r)) (:composition-results o)))
      (is (str/starts-with? (:selected-output-contract r) retained)))
    (is (= 320 (:all-selected-result-joins refinement) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 11 (count (:declared-parent-consumers refinement)) (count (:declared-parent-consumers review))))
    (is (= (:required-parent-consumers refinement) (:required-parent-consumers review)))
    (is (empty? (:unresolved-consumers refinement)))
    (is (= [9 8] (mapv count (:coarse-producer-cycles refinement)) (mapv count (:declared-output-cycles review))))
    (doseq [record [dep plan] :let [p (:path record) after (:after record) text (source-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p))))
    (is (= :selected-C9-and-C10-contracts-not-whole-Legality-or-SAFE-family-entailment (:consumer-route-scope refinement)))
    (is (= :declared-interfaces-no-adopted-result-prerequisites (:ordinary-construction refinement)))
    (is (= :valid-unoptimized-verified-MIR-remains-legal-no-task-completion-gate (:program-legality refinement)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :safety-dependency-refinement])))))

(deftest root-safety-successor-restores-qualified-criteria-and-full-source-conjunctions
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:memory-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        refinement (:root-safety-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        previous-bridge (edn/read-string (sh-crosswalk/git-source (:commit previous) (:path previous)))
        task-map (fn [v] (into {} (for [rows (vals (:task-authorities v)) row rows] [(:id row) row])))
        before (task-map prior) after (task-map view)
        raw (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        changed (set (for [[id row] before :when (not= (raw prior row) (raw view (after id)))] id))
        text (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) "specs/legality-workflow/tasks.md") (slurp "specs/legality-workflow/tasks.md"))
        stripped (reduce (fn [body marker] (str/replace body (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) "")) text (vals (:markers refinement)))
        docs (into {} (for [d (:canonical-sources refinement)] [(:document-id d) (slurp (:path d))]))
        read-span (fn [body span] (String. (.getBytes body "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        facets (:source-facets refinement) cases (:occurrence-cases refinement) sections (:section-conjunctions refinement)
        semantic? #(= :complete-source-predicate-conjunction (:role %))
        criteria (concat (:criteria refinement) (:restored-criteria refinement)) keys (set (map :key criteria))
        sections-valid? (fn [sections]
                          (and (= (set (map :section-key (filter semantic? (vals cases)))) (set (clojure.core/keys sections)))
                               (every? (fn [[k s]] (and (seq (:criterion-keys s)) (every? keys (:criterion-keys s))
                                                       (= (set (:occurrence-ids s)) (set (for [[id c] cases :when (and (semantic? c) (= k (:section-key c)))] id))))) sections)))
        uncovered (fn [id rows]
                    (let [bytes (.getBytes (docs id) "UTF-8") covered (java.util.BitSet. (alength bytes))]
                      (doseq [r rows :when (= id (:document-id r)) :let [s (:source-span r)]] (.set covered (int (:start-byte s)) (int (:end-byte s))))
                      (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:sha256 previous) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit previous) (:path previous)))))
    (is (= (set (clojure.core/keys before)) (set (clojure.core/keys after))))
    (is (= 4 (count changed)))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= [] (:new-task-identities refinement) (:newly-amended-original-task-ids refinement)))
    (is (= stripped (sh-crosswalk/git-source (:commit previous) "specs/legality-workflow/tasks.md")))
    (doseq [[id row] before] (is (= (:checked row) (:checked (after id))) id))
    (is (= 84 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 55 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= (:safety-refinement previous-bridge) (:safety-refinement bridge)))
    (is (= (:safety-dependency-refinement previous-bridge) (:safety-dependency-refinement bridge)))
    (is (= 4 (count (:criteria refinement))))
    (is (= 6 (count (:restored-criteria refinement))))
    (doseq [{:keys [task-id current-witness]} criteria :let [clause (read-span text (:source-span current-witness))]]
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (str/includes? (raw view (after task-id)) clause)))
    (doseq [{:keys [historical-witness current-witness frozen-review-finding current-composition whole-source-acceptance]} (:restored-criteria refinement)
            :let [old (sh-crosswalk/git-source (:source-commit historical-witness) (:path historical-witness))
                  old-clause (read-span old historical-witness) clause (read-span text (:source-span current-witness))]]
      (is (= (:sha256 historical-witness) (:file-sha256 historical-witness) (sh-inventory/sha256 old)))
      (is (= (:raw-sha256 historical-witness) (sh-inventory/sha256 old-clause) (sh-inventory/sha256 clause)))
      (is (= old-clause clause))
      (is (= :pass (:verdict frozen-review-finding)))
      (is (= :pending current-composition))
      (is (= :not-conferred whole-source-acceptance)))
    (is (= 133 (count facets) (count (set (map :id facets)))))
    (is (= {"SAFE1" 71 "SAFE6" 62} (frequencies (map :document-id facets))))
    (is (= {:context 37 :acceptance-rule 96} (frequencies (map :census-kind facets))))
    (is (= {:retained-hierarchy-or-provenance-context 35 :complete-source-predicate-conjunction 98} (frequencies (map :role facets))))
    (is (= 123 (count cases)))
    (is (= 88 (count (filter semantic? (vals cases)))))
    (is (= 31 (count sections)))
    (is (sections-valid? sections))
    (is (not (sections-valid? (update-in sections [["SAFE6" "Review Policy"] :occurrence-ids] pop))))
    (is (not (sections-valid? (assoc-in sections [["SAFE1" "Runtime Checks"] :criterion-keys] [:unknown]))))
    (doseq [r facets :let [s (:source-span r) c (cases (:source-occurrence-id r)) cs (:source-span c)]]
      (is (= (:sha256 s) (sh-inventory/sha256 (read-span (docs (:document-id r)) s))) (:id r))
      (is (<= (:start-byte cs) (:start-byte s) (:end-byte s) (:end-byte cs)))
      (is (= (:criterion-keys c) (:criterion-keys r)))
      (when (semantic? c) (is (and (seq (:criterion-keys c)) (every? keys (:criterion-keys c))))))
    (doseq [[_ c] cases :let [source (read-span (docs (:document-id c)) (:source-span c))]]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 source)))
      (when-not (semantic? c) (is (or (str/starts-with? source "#") (str/starts-with? source "Sequence:")))))
    (doseq [id ["SAFE1" "SAFE6"]]
      (is (empty? (uncovered id facets)))
      (let [removed (:source-occurrence-id (first (filter #(and (= id (:document-id %)) (semantic? %)) facets)))]
        (is (seq (uncovered id (remove #(= removed (:source-occurrence-id %)) facets))))))
    (is (= 13 (count (:owner-joins refinement))))
    (doseq [[_ r] (:owner-joins refinement)]
      (is (contains? after (:task-id r)))
      (is (every? keys (:criterion-keys r)))
      (is (= :unassessed (:whole-attached-owner-predicate r)))
      (is (= :none (:implementation-credit r))))
    (is (= :preserved-identity-and-conservation-only-no-aggregate-adoption (get-in refinement [:source-census :authority])))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :root-safety-refinement])))))

(deftest root-safety-dependencies-bind-descriptor-candidate-and-consumer-subjects
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:memory-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        refinement (:root-safety-dependency-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        previous-bridge (edn/read-string previous-text)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        tasks (into {} (for [rows (vals (:task-authorities view)) r rows] [(:id r) r]))
        source-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) source-text (:required-parent-consumers refinement))
        review (:dependency-review result) consumers (set (:consumer-task-ids refinement))
        selected (filterv #(consumers (:consumer %)) (:routes review))
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        dep (:dependency-source refinement) plan (:plan-source refinement)
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-selected (filterv #(consumers (:consumer %)) old-routes)
        old-map (into {} (map (juxt :consumer identity) old-routes))
        route-meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        new-ids (set (remove (set (map :consumer old-routes)) (map :consumer (:routes review))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:task-authorities prior) (:task-authorities view)))
    (is (= (:task-comparisons previous-bridge) (:task-comparisons bridge)))
    (is (= (:root-safety-refinement previous-bridge) (:root-safety-refinement bridge)))
    (is (= (:source-input-commit previous-bridge) (:source-input-commit bridge)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= [] (:changed-task-ids refinement) (:new-task-identities refinement)))
    (is (= 5 (count selected) (count consumers)))
    (is (= 2 (count new-ids)))
    (is (= new-ids (set (:new-consumer-task-ids refinement))))
    (is (= 3 (count old-selected)))
    (is (= (set (map :consumer old-selected)) (set (:extended-prior-consumers refinement))))
    (is (= (:routes refinement) (mapv #(update % :source dissoc :raw) selected)))
    (is (= (:predecessor-selected-routes refinement) (mapv #(update % :source dissoc :raw) old-selected)))
    (is (= 48 (:preserved-consumer-route-count refinement) (count (remove #(consumers (:consumer %)) old-routes))))
    (is (= (into {} (map (juxt :consumer route-meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer route-meaning) (remove #(consumers (:consumer %)) (:routes review))))))
    (is (= [4 7 8 18 25] (sort (map (comp count :composition-results) selected))))
    (is (= 42 (:preserved-prior-input-joins refinement) (reduce + (map (comp count :composition-results) old-selected))))
    (is (= 62 (:selected-consumer-result-joins refinement) (reduce + (map (comp count :composition-results) selected))))
    (is (= 20 (:new-selected-producer-result-joins refinement)
           (- (reduce + (map (comp count :composition-results) selected)) (reduce + (map (comp count :composition-results) old-selected)))))
    (doseq [r selected :let [o (old-map (:consumer r))]]
      (is (empty? (:start-results r)))
      (when o
        (is (every? (set (:composition-results r)) (:composition-results o)))
        (is (str/starts-with? (:selected-output-contract r) (first (str/split (:selected-output-contract o) #" These selected C9/C10 routes"))))))
    (is (= 340 (:all-selected-result-joins refinement) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 13 (count (:declared-parent-consumers refinement)) (count (:declared-parent-consumers review))))
    (is (= (:required-parent-consumers refinement) (:required-parent-consumers review)))
    (is (empty? (:unresolved-consumers refinement)))
    (is (= [10 8] (mapv count (:coarse-producer-cycles refinement)) (mapv count (:declared-output-cycles review))))
    (doseq [r [dep plan] :let [p (:path r) after (:after r) text (source-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p))))
    (is (= :early-descriptor-not-final-selection-grant-link-or-emission (get-in refinement [:subject-order :providers])))
    (is (= :already-built-dependency-Q-before-current-package-P-classification (get-in refinement [:subject-order :imports])))
    (is (= :local-P-safety-before-P-packaging-never-own-published-certificate-input (get-in refinement [:subject-order :current-package])))
    (is (= :candidate-record-not-fifth-outcome-required-or-supplied-review-not-audit-only-gate (get-in refinement [:subject-order :reviews])))
    (is (= :declared-interfaces-no-adopted-result-prerequisites (:ordinary-construction refinement)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :root-safety-dependency-refinement])))))

(deftest memory-source-successor-reconstructs-thirty-complete-finite-predicates
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:memory-model-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) refinement (:memory-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string prior-text) view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        previous-bridge (edn/read-string (sh-crosswalk/git-source (:commit previous) (:path previous)))
        task-map (fn [v] (into {} (for [rs (vals (:task-authorities v)) r rs] [(:id r) r])))
        before (task-map prior) after (task-map view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        text (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) "specs/legality-workflow/tasks.md") (slurp "specs/legality-workflow/tasks.md"))
        stripped (reduce (fn [b marker] (str/replace b (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) "")) text (vals (:markers refinement)))
        docs (into {} (for [d (:canonical-sources refinement)] [(:document-id d) (slurp (:path d))]))
        read-span (fn [b s] (String. (.getBytes b "UTF-8") (int (:start-byte s)) (int (- (:end-byte s) (:start-byte s))) "UTF-8"))
        fragments (:source-fragments refinement) restored (:restored-criteria refinement)
        fragment-valid? (fn [r] (when r (= (get-in r [:witness :source-span :sha256]) (sh-inventory/sha256 (read-span text (get-in r [:witness :source-span]))))))
        reconstruct (fn [rs fields] (when (every? #(fragment-valid? (fields %)) rs) (str/join " " (map #(read-span text (get-in fields [% :witness :source-span])) rs))))
        facets (:source-facets refinement) cases (:occurrence-cases refinement) sections (:section-conjunctions refinement)
        semantic? #(= :complete-source-predicate-conjunction (:role %))
        clause-keys (set (concat (map :key (:criteria refinement)) (map :key restored)))
        sections-valid? (fn [ss] (and (= (set (map :section-key (filter semantic? (vals cases)))) (set (keys ss)))
                                     (every? (fn [[k s]] (and (seq (:criterion-keys s)) (every? clause-keys (:criterion-keys s))
                                                             (= (set (:occurrence-ids s)) (set (for [[id c] cases :when (and (semantic? c) (= k (:section-key c)))] id))))) ss)))
        uncovered (fn [id rs] (let [bytes (.getBytes (docs id) "UTF-8") covered (java.util.BitSet. (alength bytes))]
                               (doseq [r rs :when (= id (:document-id r)) :let [s (:source-span r)]] (.set covered (int (:start-byte s)) (int (:end-byte s))))
                               (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:sha256 previous) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit previous) (:path previous)))))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 5 (count changed)))
    (is (= changed (set (:changed-task-ids refinement))))
    (is (= [] (:new-task-identities refinement) (:newly-amended-original-task-ids refinement)))
    (is (= stripped (sh-crosswalk/git-source (:commit previous) "specs/legality-workflow/tasks.md")))
    (doseq [[id r] before] (is (= (:checked r) (:checked (after id))) id))
    (is (= 84 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 55 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= (:root-safety-refinement previous-bridge) (:root-safety-refinement bridge)))
    (is (= (:root-safety-dependency-refinement previous-bridge) (:root-safety-dependency-refinement bridge)))
    (is (= 8 (count (:criteria refinement))))
    (is (= 5 (count (set (map :task-id (:criteria refinement))))))
    (doseq [{:keys [task-id current-witness]} (:criteria refinement) :let [clause (read-span text (:source-span current-witness))]]
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (str/includes? (raw view (after task-id)) clause)))
    (is (= 128 (count fragments)))
    (is (= {:shared-document-role 4 :shared-allocation-context 4 :current-allocation-field 120} (frequencies (map :scope (vals fragments)))))
    (doseq [[id r] fragments :let [w (:witness r) s (:source-span w) row (:row-span r)]]
      (is (= (:sha256 w) (sh-inventory/sha256 text)) id)
      (is (fragment-valid? r) id)
      (when row
        (is (= (:sha256 row) (sh-inventory/sha256 (read-span text row))))
        (is (<= (:start-byte row) (:start-byte s) (:end-byte s) (:end-byte row)))
        (is (str/includes? (read-span text row) (:allocation-id r)))))
    (is (= 30 (count restored) (count (set (map :id restored)))))
    (is (= {"SAFE2" 10 "SAFE3" 6 "SAFE4" 8 "SAFE5" 6} (frequencies (map :document-id restored))))
    (doseq [r restored :let [w (:historical-witness r) old (sh-crosswalk/git-source (:source-commit w) (:path w))
                           old-clause (read-span old w) current (reconstruct (:fragment-refs r) fragments)]]
      (is (= (:sha256 w) (:file-sha256 w) (sh-inventory/sha256 old)))
      (is (= 9 (count (:fragment-refs r))))
      (is (= old-clause current))
      (is (= (:raw-sha256 w) (sh-inventory/sha256 current)))
      (is (= :pass (get-in r [:frozen-review-finding :verdict])))
      (is (= :pending (:current-composition r)))
      (is (= :not-conferred (:whole-source-acceptance r))))
    (let [r (first restored) id (nth (:fragment-refs r) 2)]
      (is (nil? (reconstruct (:fragment-refs r) (dissoc fragments id))))
      (is (nil? (reconstruct (:fragment-refs r) (update-in fragments [id :witness :source-span :end-byte] dec)))))
    (is (= 288 (count facets) (count (set (map :id facets)))))
    (is (= {"SAFE2" 74 "SAFE3" 77 "SAFE4" 71 "SAFE5" 66} (frequencies (map :document-id facets))))
    (is (= {:context 79 :acceptance-rule 209} (frequencies (map :census-kind facets))))
    (is (= {:retained-hierarchy-or-provenance-context 75 :complete-source-predicate-conjunction 213} (frequencies (map :role facets))))
    (is (= 264 (count cases)))
    (is (= 189 (count (filter semantic? (vals cases)))))
    (is (= 67 (count sections)))
    (is (sections-valid? sections))
    (is (not (sections-valid? (update-in sections [["SAFE2" "Allocation Failure"] :occurrence-ids] pop))))
    (is (not (sections-valid? (assoc-in sections [["SAFE4" "Runtime Checks"] :criterion-keys] [:unknown]))))
    (doseq [r facets :let [s (:source-span r) c (cases (:source-occurrence-id r)) cs (:source-span c)]]
      (is (= (:sha256 s) (sh-inventory/sha256 (read-span (docs (:document-id r)) s))) (:id r))
      (is (<= (:start-byte cs) (:start-byte s) (:end-byte s) (:end-byte cs)))
      (is (= (:criterion-keys c) (:criterion-keys r)))
      (when (semantic? c) (is (and (seq (:criterion-keys c)) (every? clause-keys (:criterion-keys c))))))
    (doseq [[_ c] cases :let [source (read-span (docs (:document-id c)) (:source-span c))]]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 source)))
      (when-not (semantic? c) (is (or (str/starts-with? source "#") (str/starts-with? source "Sequence:")))))
    (doseq [id ["SAFE2" "SAFE3" "SAFE4" "SAFE5"]]
      (is (empty? (uncovered id facets)))
      (let [removed (:source-occurrence-id (first (filter #(and (= id (:document-id %)) (semantic? %)) facets)))]
        (is (seq (uncovered id (remove #(= removed (:source-occurrence-id %)) facets))))))
    (is (= 33 (count (:owner-joins refinement))))
    (is (= 1 (count (set (map :task-id (vals (:owner-joins refinement)))))))
    (doseq [[_ r] (:owner-joins refinement)]
      (is (contains? after (:task-id r)))
      (is (every? clause-keys (:criterion-keys r)))
      (is (= :unassessed (:whole-attached-owner-predicate r)))
      (is (= :none (:implementation-credit r))))
    (is (= :preserved-identity-and-conservation-only-no-aggregate-adoption (get-in refinement [:source-census :authority])))
    (is (= :retain-concrete-target-capacity-layout-provider-failure-and-profile-semantics-no-project-work-or-input-quotas (:accounting-disposition refinement)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :memory-refinement])))))

(deftest memory-dependencies-bind-early-facts-and-later-actual-consumers
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:memory-model-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) refinement (:memory-dependency-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        previous-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old (edn/read-string previous-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string projection-text) view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        tasks (into {} (for [rs (vals (:task-authorities view)) r rs] [(:id r) r]))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers refinement))
        review (:dependency-review result) consumers (set (:consumer-task-ids refinement))
        selected (filterv #(consumers (:consumer %)) (:routes review))
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        dep (:dependency-source refinement) plan (:plan-source refinement)
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-selected (filterv #(consumers (:consumer %)) old-routes)
        old-map (into {} (map (juxt :consumer identity) old-routes))
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        prefix "legality/remaining-tasks-and-evidence/"
        profile (str prefix "complete-profile-restrictions-and-generated-form-checks")
        analysis (str prefix "complete-ownership-borrow-initialization-and-lifetime-analysis")
        classification (str prefix "complete-safe-code-classification-and-taintunsafe-boundaries")
        handoff (str prefix "preserve-obligations-into-mir-and-runtime-execution")
        conformance (str prefix "close-conformance-for-the-declared-profile-surface")
        added {profile [] classification []
               analysis ["core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
                         "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
                         "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection" "backend-interface"]
               handoff ["lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
                        "lower/current-evidence-and-remaining-work/lower3-select-explicit-providers" "backend-interface"
                        "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"]
               conformance ["stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"]}
        complete? (fn [rs] (and (= consumers (set (map :consumer rs)))
                               (every? (fn [r] (= (set (:composition-results r))
                                                  (set (concat (:composition-results (old-map (:consumer r))) (added (:consumer r)))))) rs)))]
    (is (= (:sha256 previous) (sh-inventory/sha256 previous-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:task-authorities prior) (:task-authorities view)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (:task-comparisons old) (:task-comparisons bridge)))
    (is (= (:source-input-commit old) (:source-input-commit bridge)))
    (doseq [k [:memory-refinement :root-safety-refinement :root-safety-dependency-refinement
               :ownership-refinement :ownership-dependency-refinement :safety-refinement :safety-dependency-refinement]]
      (is (= (k old) (k bridge)) k))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= [] (:changed-task-ids refinement) (:new-task-identities refinement) (:new-consumer-task-ids refinement)))
    (is (= 5 (count selected) (count consumers) (count old-selected)))
    (is (= added (:added-composition-inputs refinement)))
    (is (complete? selected))
    (is (not (complete? (remove #(= analysis (:consumer %)) selected))))
    (doseq [[id inputs] added input inputs]
      (is (not (complete? (mapv #(if (= id (:consumer %)) (update % :composition-results (fn [xs] (vec (remove #{input} xs)))) %) selected)))))
    (is (= (set (map :consumer old-routes)) (set (map :consumer (:routes review)))))
    (is (= (:routes refinement) (mapv #(update % :source dissoc :raw) selected)))
    (is (= (:predecessor-selected-routes refinement) (mapv #(update % :source dissoc :raw) old-selected)))
    (is (= 48 (:preserved-consumer-route-count refinement) (count (remove #(consumers (:consumer %)) old-routes))))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) (:routes review))))))
    (is (= [5 8 9 18 29] (sort (map (comp count :composition-results) selected))))
    (is (= 60 (:preserved-prior-input-joins refinement) (reduce + (map (comp count :composition-results) old-selected))))
    (is (= 69 (:selected-consumer-result-joins refinement) (reduce + (map (comp count :composition-results) selected))))
    (is (= 9 (:new-selected-producer-result-joins refinement) (reduce + (map count (vals added)))))
    (doseq [r selected :let [o (old-map (:consumer r))]]
      (is (empty? (:start-results r)))
      (is (every? (set (:composition-results r)) (:composition-results o)))
      (is (str/starts-with? (:selected-output-contract r) (:selected-output-contract o)))
      (let [text (read-text (:path dep))
            pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote (:consumer r)) "` \\|[^\\n]*\\n"))
            without (str/replace text pattern "")
            missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                      #(if (= % (:path dep)) without (read-text %)) (:required-parent-consumers refinement))]
        (is (= 1 (count (re-seq pattern text))))
        (is (= [(:consumer r)] (get-in missing [:dependency-review :unresolved-consumers]))))
      (doseq [input (:composition-results r)]
        (is (some #(= [(str "task:" (:consumer r) "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                       [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" (:consumer r) "/composition") (str "result:" (:consumer r) ":" input) :actual-selected-producer-result]
                       [(:from %) (:to %) (:kind %)]) (:typed-edges result)))))
    (is (= 349 (:all-selected-result-joins refinement) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 13 (count (:declared-parent-consumers refinement)) (count (:declared-parent-consumers review))))
    (is (= (:required-parent-consumers refinement) (:required-parent-consumers review)))
    (is (empty? (:unresolved-consumers refinement)))
    (is (= [10 8] (mapv count (:coarse-producer-cycles refinement)) (mapv count (:declared-output-cycles review))))
    (doseq [r [dep plan] :let [p (:path r) after (:after r) text (read-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p))))
    (is (= 14 (count (:canonical-sources refinement))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources refinement)]
      (is (= sha256 (sh-inventory/sha256 (slurp path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :C7-before-analysis-C14-after-MIR-revalidate-changed-assumptions (get-in refinement [:subject-order :layout])))
    (is (= :early-declaration-then-compatible-selection-and-grants-not-actual-execution (get-in refinement [:subject-order :providers])))
    (is (= :existing-candidate-specialist-final-outcome-subjects-and-audit-only-policy-preserved (get-in refinement [:subject-order :reviews])))
    (is (= :declared-interfaces-no-adopted-result-prerequisites (:ordinary-construction refinement)))
    (is (= :valid-unoptimized-verified-MIR-remains-legal-no-task-completion-gate (:program-legality refinement)))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :memory-dependency-refinement])))))

(deftest source-memory-model-preserves-cross-component-owners-and-complete-predicates
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:memory-model-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) refinement (:memory-model-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        rows (fn [v] (into {} (for [rs (vals (:task-authorities v)) r rs] [(:id r) r])))
        before (rows prior) after (rows view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        read-span (fn [t s] (String. (.getBytes t "UTF-8") (int (:start-byte s)) (int (- (:end-byte s) (:start-byte s))) "UTF-8"))
        docs (into {} (for [r (:canonical-sources refinement)] [(:document-id r) (read-text (:path r))]))
        criteria (:criteria refinement) finite (:finite-criteria refinement)
        clause-keys (set (concat (map :key criteria) (map :key finite)))
        cases (:occurrence-cases refinement) facets (:source-facets refinement) sections (:section-conjunctions refinement)
        semantic? #(= :complete-source-predicate-conjunction (:role %))
        sections-valid? (fn [ss] (and (= (set (map :section-key (filter semantic? (vals cases)))) (set (keys ss)))
                                     (every? (fn [[k s]] (and (seq (:criterion-keys s)) (every? clause-keys (:criterion-keys s))
                                                             (= (set (:occurrence-ids s)) (set (for [[id c] cases :when (and (semantic? c) (= k (:section-key c)))] id))))) ss)))
        uncovered (fn [id fs] (let [bytes (.getBytes (docs id) "UTF-8") covered (java.util.BitSet. (alength bytes))]
                               (doseq [r fs :when (= id (:document-id r)) :let [s (:source-span r)]] (.set covered (int (:start-byte s)) (int (:end-byte s))))
                               (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 11 (count changed) (count criteria)))
    (is (= changed (set (:changed-task-ids refinement)) (set (map :task-id criteria))))
    (is (= 4 (count (:newly-amended-original-task-ids refinement))))
    (is (= [] (:new-task-identities refinement)))
    (doseq [[id r] before] (is (= (:checked r) (:checked (after id))) id))
    (is (= 80 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 59 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= 32 (count (:source-files bridge))))
    (is (= (:memory-refinement old) (:memory-refinement bridge)))
    (is (= (:memory-dependency-refinement old) (:memory-dependency-refinement bridge)))
    (is (= 6 (count (:task-sources refinement)) (count (:plan-sources refinement))))
    (doseq [r (:task-sources refinement) :let [p (:path r) t (read-text p)
            markers (for [[k m] (:markers refinement) :when (= p (get-in (first (filter #(= k (:key %)) criteria)) [:current-witness :path]))] m)
            stripped (reduce (fn [s marker] (str/replace s (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) "")) t markers)]]
      (is (= stripped (sh-crosswalk/git-source (:commit previous) p)))
      (is (= (get-in r [:after :sha256]) (sh-inventory/sha256 t)))
      (is (= (get-in r [:before :sha256]) (sh-inventory/sha256 (sh-crosswalk/git-source (get-in r [:before :commit]) p)))))
    (doseq [r (:plan-sources refinement) :let [p (:path r) a (:after r) text (read-text p)]]
      (is (= (:sha256 a) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit a) p)))
      (is (= (:commit a) (get-in bridge [:applying-source-pins p])))
      (is (= a (get-in bridge [:inspection-locators p]))))
    (doseq [r (concat criteria finite) :let [w (:current-witness r) t (read-text (:path w)) s (:source-span w) clause (read-span t s)]]
      (is (= (:sha256 w) (sh-inventory/sha256 t)))
      (is (= (:sha256 s) (sh-inventory/sha256 clause)))
      (is (str/includes? (raw view (after (:task-id r))) clause)))
    (is (= 8 (count finite)))
    (is (= {:restored-exact-finite-predicate 7 :preserved-existing-exact-finite-predicate 1} (frequencies (map :disposition finite))))
    (is (= {:profile74 7 :bootstrap25 1} (frequencies (map :frozen-review finite))))
    (doseq [r finite :let [w (:historical-witness r) t (sh-crosswalk/git-source (:source-commit w) (:path w))
                          old-clause (read-span t w) current (read-span (read-text (:path r)) (get-in r [:current-witness :source-span]))]]
      (is (= (:sha256 w) (:file-sha256 w) (sh-inventory/sha256 t)))
      (is (= (:raw-sha256 w) (sh-inventory/sha256 old-clause) (sh-inventory/sha256 current)))
      (is (= :pending (:current-composition r)))
      (is (= :not-conferred (:whole-source-acceptance r))))
    (is (= (before "evolution/change-review") (after "evolution/change-review")))
    (is (= 226 (count facets) (count (set (map :id facets)))))
    (is (= {"L10" 101 "L18" 125} (frequencies (map :document-id facets))))
    (is (= {:context 53 :semantic 173} (frequencies (map :census-kind facets))))
    (is (= {:retained-hierarchy-or-provenance-context 45 :complete-source-predicate-conjunction 181} (frequencies (map :role facets))))
    (is (= 143 (count cases)))
    (is (= 104 (count (filter semantic? (vals cases)))))
    (is (= 35 (count sections)))
    (is (sections-valid? sections))
    (is (not (sections-valid? (update-in sections [["L18" "Memory Provider Declaration"] :occurrence-ids] pop))))
    (is (not (sections-valid? (assoc-in sections [["L10" "Change Control"] :criterion-keys] [:unknown]))))
    (doseq [r facets :let [s (:source-span r) c (cases (:source-occurrence-id r)) cs (:source-span c)]]
      (is (= (:sha256 s) (sh-inventory/sha256 (read-span (docs (:document-id r)) s))) (:id r))
      (is (<= (:start-byte cs) (:start-byte s) (:end-byte s) (:end-byte cs)))
      (is (= (:criterion-keys c) (:criterion-keys r)))
      (when (semantic? c) (is (and (seq (:criterion-keys c)) (every? clause-keys (:criterion-keys c))))))
    (doseq [[_ c] cases :let [raw (read-span (docs (:document-id c)) (:source-span c))]]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (when-not (semantic? c) (is (or (str/starts-with? raw "#") (str/starts-with? raw "Sequence:")))))
    (doseq [id ["L10" "L18"]]
      (is (empty? (uncovered id facets)))
      (let [removed (:source-occurrence-id (first (filter #(and (= id (:document-id %)) (semantic? %)) facets)))]
        (is (seq (uncovered id (remove #(= removed (:source-occurrence-id %)) facets))))))
    (is (= 13 (count (:owner-joins refinement))))
    (doseq [[id r] (:owner-joins refinement)]
      (is (contains? after (:task-id r)) id)
      (is (every? clause-keys (:criterion-keys r)))
      (is (= :unassessed (:whole-attached-owner-predicate r)))
      (is (= :none (:implementation-credit r))))
    (doseq [[id task] {"language/memory/allocate-provider-storage" "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
                       "language/memory/release-provider-storage" "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
                       "language/memory/review-safety-change" "evolution/change-review"
                       "language/memory/check-linear-resource-flow" "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
                       "language/memory/check-raw-and-mmio-boundary" "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"}]
      (is (= task (get-in refinement [:owner-joins id :task-id]))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= :preserved-identity-and-conservation-only-no-aggregate-adoption (get-in refinement [:source-census :authority])))
    (is (= :pending (:independent-current-review refinement)))
    (is (= :unassessed (:implementation-acceptance refinement) (:global-source-conjunctions refinement)))
    (is (= refinement (get-in view [:implementation-readiness-source-version :memory-model-refinement])))))

(deftest source-memory-dependencies-preserve-authorities-and-stage-real-results
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:source-effects-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) r (:memory-model-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        tasks (into {} (for [rs (vals (:task-authorities view)) row rs] [(:id row) row]))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers r))
        review (:dependency-review result) consumers (set (:consumer-task-ids r))
        selected (filterv #(consumers (:consumer %)) (:routes review))
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        dep (:dependency-source r)
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-map (into {} (map (juxt :consumer identity) old-routes))
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        added {"auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "backend-interface"],
 "core/current-evidence-and-remaining-work/core1-lower-core-forms"
 ["syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"],
 "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
 ["core/current-evidence-and-remaining-work/core1-lower-core-forms"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"],
 "devices"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "backend-interface"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"],
 "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
  "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"],
 "evolution/change-review"
 ["types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"
  "pass-evidence"],
 "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"
 [],
 "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
 ["core/current-evidence-and-remaining-work/core1-lower-core-forms"
  "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"],
 "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
 [],
 "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
 [],
 "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
 ["effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"],
 "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "lower/current-evidence-and-remaining-work/lower3-select-explicit-providers"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "lower/current-evidence-and-remaining-work/lower5-integrate-target-emission"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "backend-interface"
  "devices"],
 "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
 ["runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"]}

        expected (into {} (for [[id inputs] added] [id (set (concat (:composition-results (old-map id)) inputs))]))
        complete? (fn [rows] (= expected (into {} (map (juxt :consumer (comp set :composition-results)) rows))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (doseq [k [:task-authorities :future-task-authorities :sh07-sequence :accepted-current-scoped-evidence]] (is (= (k prior) (k view)) k))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (doseq [k [:source-input-commit :task-comparisons :memory-model-refinement :memory-refinement :memory-dependency-refinement
               :root-safety-refinement :root-safety-dependency-refinement :ownership-refinement :ownership-dependency-refinement :safety-refinement :safety-dependency-refinement]]
      (is (= (k old) (k bridge)) k))
    (is (= 32 (count (:source-files bridge))))
    (is (= 149 (count tasks)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 80 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 59 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= [] (:changed-task-ids r) (:new-task-identities r)))
    (is (= 13 (count consumers) (count selected)))
    (is (= 8 (count (:new-consumer-task-ids r))))
    (is (= (set (:new-consumer-task-ids r)) (clojure.set/difference consumers (set (keys old-map)))))
    (is (= added (:added-composition-inputs r)))
    (is (complete? selected))
    (doseq [[id inputs] expected]
      (is (not (complete? (remove #(= id (:consumer %)) selected))))
      (doseq [input inputs]
        (is (not (complete? (mapv #(if (= id (:consumer %)) (update % :composition-results (fn [xs] (vec (remove #{input} xs)))) %) selected))))))
    (is (= 61 (count (:routes review))))
    (is (= (:routes r) (mapv #(update % :source dissoc :raw) selected)))
    (is (= 5 (count (:predecessor-selected-routes r))))
    (is (= 69 (:preserved-prior-input-joins r) (reduce + (map (comp count :composition-results) (:predecessor-selected-routes r)))))
    (is (= 129 (:selected-consumer-result-joins r) (reduce + (map (comp count :composition-results) selected))))
    (is (= 60 (:new-selected-producer-result-joins r) (reduce + (map count (vals added)))))
    (is (= 48 (:preserved-consumer-route-count r)))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) (:routes review))))))
    (doseq [row selected :let [id (:consumer row) before (old-map id)]]
      (is (empty? (:start-results row)))
      (when before
        (is (every? (set (:composition-results row)) (:composition-results before)))
        (is (str/starts-with? (:selected-output-contract row) (:selected-output-contract before))))
      (let [text (read-text (:path dep))
            pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote id) "` \\|[^\\n]*\\n"))
            without (str/replace text pattern "")
            missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                      #(if (= % (:path dep)) without (read-text %)) (:required-parent-consumers r))]
        (is (= 1 (count (re-seq pattern text))))
        (is (= [id] (get-in missing [:dependency-review :unresolved-consumers]))))
      (doseq [input (:composition-results row)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                       [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" input) :actual-selected-producer-result]
                       [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (= :unassessed (get-in result [:nodes (str "result:" id ":" input) :state])))))
    (is (= 409 (:all-selected-result-joins r) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 21 (count (:declared-parent-consumers r)) (count (:required-parent-consumers r)) (count (:declared-parent-consumers review))))
    (is (= 40 (count (:declared-future-consumers r))))
    (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
    (is (= [2 16 8] (mapv count (:coarse-producer-cycles r)) (mapv count (:declared-output-cycles review))))
    (is (= #{"auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
             "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"}
           (set (first (:coarse-producer-cycles r)))))
    (is (= 11 (count (:staged-result-subjects r)) (count (set (map :result (:staged-result-subjects r))))))
    (doseq [s (:staged-result-subjects r)]
      (is (= (:result s) (str "result:" (:consumer s) ":" (:producer s))))
      (is (contains? (expected (:consumer s)) (:producer s))))
    (is (= :independently-supplied-safety-review-receipt-not-compiler-or-self-audit-output (:external-governance-input r)))
    (is (= :GOV9-scope-not-generic-safety-change-approval (:unsafe-audit-decision r)))
    (is (= 8 (count (:plan-sources r))))
    (doseq [record (concat [dep (:dependency-tool-source r)] (:plan-sources r))
            :let [p (:path record) after (:after record) before (:before record) text (read-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p)))
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p)))))
    (is (= 14 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (if (= path "docs/phase-01-core-language/016-l6-effect-system-specification.md")
        (do
          ;; The checkpoint retains the historical L6 pin; the current
          ;; successor binds the live document separately.
          (is (= sha256 (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path))))
          (is (= "529b2cb63e0fe304f628ed63b0e6ef64d86200034f854836399e02e103b41f72"
                 (sh-inventory/sha256 (slurp path)))))
        (is (= sha256 (sh-inventory/sha256 (slurp path))
               (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path))))))
    (is (= :declared-interfaces-no-adopted-result-prerequisites (:ordinary-construction r)))
    (is (= :valid-unoptimized-verified-MIR-remains-legal-no-task-completion-gate (:program-legality r)))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :memory-model-dependency-refinement])))))

(deftest source-effects-bind-complete-predicates-and-six-existing-interpreters
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:source-effects-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) r (:source-effects-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        rows (fn [v] (into {} (for [rs (vals (:task-authorities v)) row rs] [(:id row) row])))
        before (rows prior) after (rows view)
        raw (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        changed (set (for [[id row] before :when (not= (raw prior row) (raw view (after id)))] id))
        read-span (fn [t span] (String. (.getBytes t "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        source (read-text "docs/phase-01-core-language/016-l6-effect-system-specification.md")
        facets (:source-facets r) cases (:occurrence-cases r) sections (:section-conjunctions r)
        criteria (:criteria r) finite (:finite-criteria r)
        criterion-keys (set (concat (map :key criteria) (map :key finite)))
        semantic? #(= :complete-source-predicate-conjunction (:role %))
        sections-valid? (fn [ss] (and (= (set (map :section-key (filter semantic? (vals cases)))) (set (keys ss)))
         (every? (fn [[k s]] (and (seq (:criterion-keys s)) (every? criterion-keys (:criterion-keys s))
          (= (set (:occurrence-ids s)) (set (for [[id c] cases :when (and (semantic? c) (= k (:section-key c)))] id))))) ss)))
        uncovered (fn [fs] (let [bytes (.getBytes source "UTF-8") covered (java.util.BitSet. (alength bytes))]
         (doseq [row fs :let [s (:source-span row)]] (.set covered (int (:start-byte s)) (int (:end-byte s))))
         (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))
        diagnostics #{"L6-EFFECT-UNDECLARED" "L6-EFFECT-PROFILE" "L6-EFFECT-CAPABILITY" "L6-BUILD-EFFECT" "L6-REPLAY-EFFECT"
                      "L6-EFFECT-ORDER" "L6-EFFECT-UNKNOWN" "L6-HANDLER-TYPE" "L6-HANDLER-PROFILE" "L6-HANDLER-CAPABILITY"
                      "L6-HANDLER-CONTINUATION" "L6-HANDLER-REPLAY" "L6-HANDLER-COVERAGE"}]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 149 (count after)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 12 (count changed) (count criteria)))
    (is (= changed (set (:changed-task-ids r)) (set (map :task-id criteria))))
    (is (= 5 (count (:newly-amended-original-task-ids r))))
    (is (= [] (:new-task-identities r)))
    (doseq [[id row] before] (is (= (select-keys row [:checked :status]) (select-keys (after id) [:checked :status])) id))
    (is (= 75 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 64 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= 32 (count (:source-files bridge))))
    (doseq [key [:effects-refinement :memory-model-refinement :memory-model-dependency-refinement :memory-refinement :memory-dependency-refinement]]
      (is (= (key old) (key bridge)) key))
    (is (= 6 (count (:task-sources r))))
    (is (= 8 (count (:plan-sources r))))
    (is (= 1 (count (:specification-sources r))))
    (doseq [record (:task-sources r) :let [p (:path record) text (read-text p)
            markers (for [[k marker] (:markers r) :when (= p (get-in (first (filter #(= k (:key %)) criteria)) [:current-witness :path]))] marker)
            stripped (reduce #(str/replace %1 (re-pattern (str "(?s)\n<!-- " %2 ":start -->.*?<!-- " %2 ":end -->\n\n")) "") text markers)]]
      (is (= stripped (sh-crosswalk/git-source (:commit previous) p)))
      (is (= (get-in record [:after :sha256]) (sh-inventory/sha256 text)))
      (is (= (get-in record [:before :sha256]) (sh-inventory/sha256 (sh-crosswalk/git-source (get-in record [:before :commit]) p)))))
    (doseq [record (concat (:plan-sources r) (:specification-sources r)) :let [p (:path record) a (:after record) text (read-text p)]]
      (is (= (:sha256 a) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit a) p)))
      (is (= (:commit a) (get-in bridge [:applying-source-pins p])))
      (is (= a (get-in bridge [:inspection-locators p]))))
    (doseq [record (concat criteria finite) :let [w (:current-witness record) text (read-text (:path w)) span (:source-span w) clause (read-span text span)]]
      (is (= (:sha256 w) (sh-inventory/sha256 text)))
      (is (= (:sha256 span) (sh-inventory/sha256 clause)))
      (is (str/includes? (raw view (after (:task-id record))) clause)))
    (is (= 9 (count finite)))
    (is (= {:restored-exact-finite-predicate 7 :preserved-existing-exact-finite-predicate 2} (frequencies (map :disposition finite))))
    (is (= #{:profile74} (set (map :frozen-review finite))))
    (doseq [record finite :let [w (:historical-witness record) text (sh-crosswalk/git-source (:source-commit w) (:path w))
                              historical (read-span text w) current (read-span (read-text (:path record)) (get-in record [:current-witness :source-span]))]]
      (is (= (:sha256 w) (:file-sha256 w) (sh-inventory/sha256 text)))
      (is (= (:raw-sha256 w) (sh-inventory/sha256 historical) (sh-inventory/sha256 current)))
      (is (= :pending (:current-composition record)))
      (is (= :not-conferred (:whole-source-acceptance record))))
    (is (= (before "evolution/change-review") (after "evolution/change-review")))
    (is (= 133 (count facets) (count (set (map :id facets)))))
    (is (= {:semantic 104 :context 27 :unresolved 2} (frequencies (map :census-kind facets))))
    (is (= {:complete-source-predicate-conjunction 109 :retained-hierarchy-or-provenance-context 24} (frequencies (map :role facets))))
    (is (= 87 (count cases)))
    (is (= 66 (count (filter semantic? (vals cases)))))
    (is (= 19 (count sections)))
    (is (sections-valid? sections))
    (is (not (sections-valid? (update-in sections [["L6" "Continuation and Replay Safety"] :occurrence-ids] pop))))
    (is (not (sections-valid? (assoc-in sections [["L6" "Typed Effect Handlers"] :criterion-keys] [:unknown]))))
    (doseq [row facets :let [span (:source-span row) c (cases (:source-occurrence-id row)) cs (:source-span c)]]
      (is (= (:sha256 span) (sh-inventory/sha256 (read-span source span))) (:id row))
      (is (<= (:start-byte cs) (:start-byte span) (:end-byte span) (:end-byte cs)))
      (is (= (:criterion-keys c) (:criterion-keys row)))
      (when (semantic? c) (is (and (seq (:criterion-keys c)) (every? criterion-keys (:criterion-keys c))))))
    (doseq [[_ c] cases :let [text (read-span source (:source-span c))]]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 text)))
      (when-not (semantic? c) (is (or (str/starts-with? text "#") (str/starts-with? text "Sequence:")))))
    (is (empty? (uncovered facets)))
    (let [removed (:source-occurrence-id (first (filter semantic? facets)))]
      (is (seq (uncovered (remove #(= removed (:source-occurrence-id %)) facets)))))
    (is (= 16 (count (:owner-joins r))))
    (is (= (set (keys (:owner-joins r))) (set (mapcat :owner-requirement-ids facets))))
    (let [input (:owner-allocation-input r)
          text (sh-crosswalk/git-source (:commit input) (:path input))
          units (into {} (map (juxt :proposed-id identity) (:units (edn/read-string text))))]
      (is (= (:sha256 input) (sh-inventory/sha256 text)))
      (doseq [[id owner] (:owner-joins r)]
        (is (= (:original-obligation owner) (get-in units [id :obligation])))
        (is (= (:historical-task-references owner) (get-in units [id :task-references])))))
    (doseq [[id owner] (:owner-joins r)]
      (is (contains? after (:task-id owner)) id)
      (is (every? criterion-keys (:criterion-keys owner)))
      (is (or (seq (:criterion-keys owner)) (seq (:producer-context owner))))
      (is (= :unassessed (:whole-attached-owner-predicate owner)))
      (is (= :none (:implementation-credit owner))))
    (doseq [[id task] {"language/effects/interpret-async-suspension" "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
                      "language/effects/interpret-generator" "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
                      "language/effects/interpret-resumable-error" "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
                      "language/effects/interpret-workflow-replay" "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
                      "language/effects/interpret-tool-call" "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
                      "language/effects/interpret-test-fixture" "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
                      "language/effects/register-stable-semantic-extension" "evolution/change-review"}]
      (is (= task (get-in r [:owner-joins id :task-id]))))
    (is (= :L6-environment-denial-versus-L12-declared-key-case (get-in r [:hermetic-source-qualification :issue])))
    (is (true? (get-in r [:hermetic-source-qualification :no-generic-grant-exception])))
    (is (= :explicit-source-owner-resolution-before-live-hermetic-environment-claim (get-in r [:hermetic-source-qualification :resolution])))
    (is (= :no-ordinary-development-gate (get-in r [:hermetic-source-qualification :construction])))
    (doseq [{:keys [path source-commit sha256]} (:supporting-canonical-sources r)]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= 55 (count (:baseline-effect-labels r)) (count (set (:baseline-effect-labels r)))))
    (let [family-text (subs source (.indexOf source "## Effect Families\n") (.indexOf source "## Effect Algebra\n"))]
      (is (= (:baseline-effect-labels r) (mapv second (re-seq #"(?m)^- `(:[^`]+)`" family-text)))))
    (is (= diagnostics (set (:required-diagnostic-codes r))))
    (is (= #{"facet/language/be2c3f6e01803bec7d557b47" "facet/language/7e362f8e0eb63c9112a24416"}
           (set (map :facet-id (:accounting-policy-dispositions r)))))
    (doseq [d (:accounting-policy-dispositions r)]
      (is (= :project-work-token-call-fuel-accounting-superseded (:current-policy d)))
      (is (= :preserved (:source-bytes d)))
      (is (= :typed-schema-tool-identity-authority-required-review-result-and-replay-semantics (:surviving-obligations d))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (is (= (:sh07-sequence prior) (:sh07-sequence view)))
    (is (= (:accepted-current-scoped-evidence prior) (:accepted-current-scoped-evidence view)))
    (is (= :existing-409-selected-joins-retained-complete-L6-producer-consumer-detail-pending (:dependency-review r)))
    (is (= :preserved-identity-and-conservation-only-no-aggregate-adoption (get-in r [:source-census :authority])))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :source-effects-refinement])))))


(deftest source-effect-dependencies-preserve-program-and-result-stages
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:concurrency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) r (:source-effects-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        tasks (into {} (for [rs (vals (:task-authorities view)) row rs] [(:id row) row]))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers r))
        review (:dependency-review result) consumers (set (:consumer-task-ids r))
        selected (filterv #(consumers (:consumer %)) (:routes review))
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        dep (:dependency-source r)
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-map (into {} (map (juxt :consumer identity) old-routes))
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        added {"ai"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "schema"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"],
 "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
 [],
 "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
  "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"],
 "effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"],
 "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
 ["effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"],
 "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
 ["types/current-evidence-and-remaining-work/types1-solve-core-constraints"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"],
 "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
 ["effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
  "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"],
 "effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"
 ["effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
  "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
  "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality"
  "opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "workflow"
  "ai"],
 "evolution/change-review"
 ["effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"],
 "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
 ["syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"],
 "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"],
 "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
 ["effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
  "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"],
 "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
 [],
 "release/remaining-tasks-and-evidence/execute-language-conformance-fixtures"
 ["effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"
  "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"],
 "release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures"
 ["effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"
  "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"],
 "release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures"
 ["effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"
  "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"],
 "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "schema"
  "workflow"
  "ai"],
 "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"],
 "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"],
 "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"],
 "workflow"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "schema"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"]}
        expected (into {} (for [[id inputs] added] [id (set (concat (:composition-results (old-map id)) inputs))]))
        complete? (fn [rows] (= expected (into {} (map (juxt :consumer (comp set :composition-results)) rows))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (doseq [k [:task-authorities :future-task-authorities :sh07-sequence :accepted-current-scoped-evidence]] (is (= (k prior) (k view)) k))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (doseq [k [:source-effects-refinement :memory-model-dependency-refinement :source-input-commit :task-comparisons :memory-model-refinement :memory-refinement :memory-dependency-refinement
               :root-safety-refinement :root-safety-dependency-refinement :ownership-refinement :ownership-dependency-refinement :safety-refinement :safety-dependency-refinement]]
      (is (= (k old) (k bridge)) k))
    (is (= 32 (count (:source-files bridge))))
    (is (= 149 (count tasks)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 75 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 64 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= [] (:changed-task-ids r) (:new-task-identities r)))
    (is (= 21 (count consumers) (count selected)))
    (is (= 12 (count (:new-consumer-task-ids r))))
    (is (= (set (:new-consumer-task-ids r)) (clojure.set/difference consumers (set (keys old-map)))))
    (is (= added (:added-composition-inputs r)))
    (is (complete? selected))
    (doseq [[id inputs] expected]
      (is (not (complete? (remove #(= id (:consumer %)) selected))))
      (doseq [input inputs]
        (is (not (complete? (mapv #(if (= id (:consumer %)) (update % :composition-results (fn [xs] (vec (remove #{input} xs)))) %) selected))))))
    (is (= 73 (count (:routes review))))
    (is (= (:routes r) (mapv #(update % :source dissoc :raw) selected)))
    (is (= 9 (count (:predecessor-selected-routes r))))
    (is (= 74 (:preserved-prior-input-joins r) (reduce + (map (comp count :composition-results) (:predecessor-selected-routes r)))))
    (is (= 191 (:selected-consumer-result-joins r) (reduce + (map (comp count :composition-results) selected))))
    (is (= 117 (:new-selected-producer-result-joins r) (reduce + (map count (vals added)))))
    (is (= 52 (:preserved-consumer-route-count r)))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) (:routes review))))))
    (doseq [row selected :let [id (:consumer row) before (old-map id)]]
      (is (empty? (:start-results row)))
      (when before
        (is (every? (set (:composition-results row)) (:composition-results before)))
        (is (str/starts-with? (:selected-output-contract row) (:selected-output-contract before))))
      (let [text (read-text (:path dep))
            pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote id) "` \\|[^\\n]*\\n"))
            without (str/replace text pattern "")
            missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                      #(if (= % (:path dep)) without (read-text %)) (:required-parent-consumers r))]
        (is (= 1 (count (re-seq pattern text))))
        (is (= [id] (get-in missing [:dependency-review :unresolved-consumers]))))
      (doseq [input (:composition-results row)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                       [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" input) :actual-selected-producer-result]
                       [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (= :unassessed (get-in result [:nodes (str "result:" id ":" input) :state])))))
    (is (= 526 (:all-selected-result-joins r) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 33 (count (:declared-parent-consumers r)) (count (:required-parent-consumers r)) (count (:declared-parent-consumers review))))
    (is (= 40 (count (:declared-future-consumers r))))
    (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
    (is (= [29 8] (mapv count (:coarse-producer-cycles r)) (mapv count (:declared-output-cycles review))))
    (let [old-deps (:dependency-review (acceptance-dependencies/future-task-view (:future-task-authorities prior) tasks (:source-snapshots prior)
                      #(sh-crosswalk/git-source (:commit previous) %)
                      (:required-parent-consumers (:memory-model-dependency-refinement old))))
          prior-cycles (:declared-output-cycles old-deps)
          inferred "effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
          expected-main (set (concat (first prior-cycles) (second prior-cycles) (remove #{inferred} (:new-consumer-task-ids r))))]
      (is (= [2 16 8] (mapv count prior-cycles)))
      (is (= expected-main (set (first (:coarse-producer-cycles r)))))
      (is (= (set (nth prior-cycles 2)) (set (second (:coarse-producer-cycles r)))))
      (is (not (contains? expected-main inferred))))
    (is (= 29 (count (:staged-result-subjects r)) (count (set (map :result (:staged-result-subjects r))))))
    (doseq [s (:staged-result-subjects r)]
      (is (= (:result s) (str "result:" (:consumer s) ":" (:producer s))))
      (is (contains? (expected (:consumer s)) (:producer s))))
    (is (= :applicable-action-stable-change-and-memory-safety-reviews-separately-supplied-not-checker-or-service-output (:external-review-input r)))
    (is (= :exact-L6-L12-premises-and-recorded-environment-wording-issue-no-generic-grant-exception (:hermetic-policy r)))
    (is (= :actual-checked-program-P-before-invocation-generated-caller-Q-checked-afterward-no-circular-declaration (:macro-subject-identity r)))
    (is (= [:derived-action-facts-with-legality-pending :checked-Core-after-applicable-checks :later-actual-owner-example-results] (:effect-interface-subjects r)))
    (is (= 11 (count (:plan-sources r))))
    (doseq [record (concat [dep (:dependency-tool-source r)] (:plan-sources r))
            :let [p (:path record) after (:after record) before (:before record) text (read-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p)))
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p)))))
    (is (= 15 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (if (= path "docs/phase-01-core-language/016-l6-effect-system-specification.md")
        (do
          ;; This checkpoint keeps the historical L6 pin; its current
          ;; successor authenticates the live document separately.
          (is (= sha256 (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path))))
          (is (= "529b2cb63e0fe304f628ed63b0e6ef64d86200034f854836399e02e103b41f72"
                 (sh-inventory/sha256 (slurp path)))))
        (is (= sha256 (sh-inventory/sha256 (slurp path))
               (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path))))))
    (is (= :declared-interfaces-no-whole-component-or-final-suite-start-gate (:ordinary-construction r)))
    (is (= :valid-unoptimized-verified-MIR-remains-legal-no-task-completion-gate (:program-legality r)))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :source-effects-dependency-refinement])))))


(deftest concurrency-source-preserves-joint-contracts-and-existing-authorities
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:concurrency-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) r (:concurrency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        rows (fn [v] (into {} (for [rs (vals (:task-authorities v)) row rs] [(:id row) row])))
        before (rows prior) after (rows view)
        raw (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        changed (set (for [[id row] before :when (not= (raw prior row) (raw view (after id)))] id))
        span-text (fn [t s] (String. (.getBytes t "UTF-8") (int (:start-byte s)) (int (- (:end-byte s) (:start-byte s))) "UTF-8"))
        census (edn/read-string (sh-crosswalk/git-source (get-in r [:source-census :commit]) (get-in r [:source-census :path])))
        selected #{"L11" "SAFE8" "R6"}
        original-facets (filterv #(selected (:document-id %)) (:facets census))
        original-cases (filterv #(selected (:document-id %)) (:occurrences census))
        sources (into {} (for [{:keys [document-id path]} (:canonical-sources r)] [document-id (read-text path)]))
        facets (:source-facets r) cases (:occurrence-cases r) sections (:section-conjunctions r)
        criteria (:criteria r) finite (:finite-criteria r)
        criterion-keys (set (concat (map :key criteria) (map :key finite)))
        semantic? #(= :complete-source-predicate-conjunction (:role %))
        sections-valid? (fn [ss] (and (= (set (map :section-key (filter semantic? (vals cases)))) (set (keys ss)))
          (every? (fn [[k s]] (and (seq (:criterion-keys s)) (every? criterion-keys (:criterion-keys s))
            (= (set (:occurrence-ids s)) (set (for [[id c] cases :when (and (semantic? c) (= k (:section-key c)))] id))))) ss)))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= 149 (count before) (count after)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 25 (count (:task-authorities view))))
    (doseq [[id row] before] (is (= (:checked row) (:checked (after id))) id))
    (is (= 18 (count changed) (count criteria)))
    (is (= changed (set (:changed-task-ids r)) (set (map :task-id criteria))))
    (is (= [] (:new-task-identities r)))
    (is (= 4 (count (:newly-amended-original-task-ids r))))
    (is (= [71 68] [(get-in bridge [:task-comparisons :unchanged-body-count]) (count (get-in bridge [:task-comparisons :changed-bodies]))]))
    (is (= 32 (count (:source-files bridge))))
    (is (= 8 (count (:task-sources r))))
    (is (= 9 (count (:plan-sources r))))
    (is (= 3 (count (:specification-sources r))))
    (is (= (raw prior (before "evolution/change-review")) (raw view (after "evolution/change-review"))))
    (doseq [k [:future-task-authorities :sh07-sequence :accepted-current-scoped-evidence]] (is (= (k prior) (k view)) k))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (doseq [k [:source-effects-refinement :source-effects-dependency-refinement :memory-model-refinement :memory-model-dependency-refinement
               :memory-refinement :memory-dependency-refinement :root-safety-refinement :root-safety-dependency-refinement
               :ownership-refinement :ownership-dependency-refinement :safety-refinement :safety-dependency-refinement]]
      (is (= (k old) (k bridge)) k))
    (doseq [record (concat (:task-sources r) (:plan-sources r) (:specification-sources r))
            :let [p (:path record) a (:after record) b (:before record) text (read-text p)]]
      (is (= (:sha256 a) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit a) p)))
      (is (= (:sha256 b) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit b) p)))))
    (doseq [record (concat criteria finite) :let [w (:current-witness record) text (read-text (:path w)) clause (span-text text (:source-span w))]]
      (is (= (:sha256 w) (sh-inventory/sha256 text)))
      (is (= (get-in w [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (str/includes? (raw view (after (:task-id record))) clause)))
    (is (= 32 (count finite) (count (set (map :witness-key finite)))))
    (is (= 27 (count (set (map :id finite)))))
    (is (= {:profile74 10 :safety107 8 :runtime20 12 :R6-task-cancellation 2} (frequencies (map :family finite))))
    (is (= {:preserved-existing-exact-finite-predicate 3 :restored-exact-finite-predicate 29} (frequencies (map :disposition finite))))
    (doseq [record finite :let [w (:historical-witness record) text (sh-crosswalk/git-source (:source-commit w) (:path w))
                              historical (span-text text w) current (span-text (read-text (:path record)) (get-in record [:current-witness :source-span]))]]
      (is (= (:file-sha256 w) (:sha256 w) (sh-inventory/sha256 text)))
      (is (= (:raw-sha256 w) (sh-inventory/sha256 historical) (sh-inventory/sha256 current)))
      (is (= historical current)))
    (is (= 238 (count facets) (count (set (map :id facets)))))
    (is (= {"L11" 94 "SAFE8" 66 "R6" 78} (frequencies (map :document-id facets))))
    (is (= (set (map :id original-facets)) (set (map :id facets))))
    (is (= 160 (count cases)))
    (is (= (set (map :id original-cases)) (set (keys cases))))
    (is (= 44 (count sections)))
    (is (= {:retained-hierarchy-or-provenance-context 53 :complete-source-predicate-conjunction 185} (:current-role-population r)))
    (is (= {:retained-hierarchy-or-provenance-context 50 :complete-source-predicate-conjunction 110} (frequencies (map :role (vals cases)))))
    (is (sections-valid? sections))
    (doseq [k (keys sections)]
      (is (not (sections-valid? (dissoc sections k))))
      (is (not (sections-valid? (assoc-in sections [k :criterion-keys] [])))))
    (doseq [record (concat facets (vals cases)) :let [text (sources (:document-id record)) clause (span-text text (:source-span record))]]
      (is (= (get-in record [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (= :proposal-pending-independent-review (:current-entailment record)))
      (is (every? criterion-keys (:criterion-keys record)))
      (when (semantic? record) (is (seq (:criterion-keys record))))
      (is (not= (get-in record [:source-span :sha256]) (sh-inventory/sha256 (str clause " altered")))))
    (is (= 27 (count (:owner-joins r))))
    (is (= (set (mapcat :owner-requirement-ids original-facets)) (set (keys (:owner-joins r)))))
    (doseq [[id row] (:owner-joins r)]
      (is (contains? after (:task-id row)) id)
      (is (seq (:criterion-keys row)) id)
      (is (every? criterion-keys (:criterion-keys row)) id)
      (is (= :unassessed (:whole-attached-owner-predicate row)))
      (is (= :none (:implementation-credit row))))
    (doseq [{:keys [path source-commit sha256]} (concat (:canonical-sources r) (:supporting-source-interfaces r) (get-in r [:implementation-source-inspection :files]))]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (doseq [{:keys [path source-commit sha256]} (filter :source-commit (:review-source-artifacts r))]
      (is (= sha256 (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :alternative-lifecycle-owner-does-not-waive-SAFE8-non-static-borrow-ban (get-in r [:allocation-reconciliation :detached-capture])))
    (is (= :preventive-synchronization-not-debug-after-race (get-in r [:allocation-reconciliation :runtime-check])))
    (is (= :source-sequential-consistency-unless-explicit-governing-profile-library-weaker-order (get-in r [:allocation-reconciliation :atomic-default])))
    (is (= 8 (count (get-in r [:historical-reviews :safety107 :selected-findings]))))
    (is (= :pending (get-in r [:historical-reviews :runtime20 :actual-composition])))
    (is (= :sealed-and-consumed-once (get-in r [:historical-reviews :R6 :status])))
    (is (= :not-repeated (get-in r [:historical-reviews :R6 :consumption])))
    (is (= :unavailable-422-observed-no-publication-or-handoff-retry (get-in r [:historical-reviews :R6 :github-availability])))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :concurrency-refinement])))))

(deftest concurrency-dependencies-preserve-actual-result-stages
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:authority-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) r (:concurrency-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        tasks (into {} (for [rs (vals (:task-authorities view)) row rs] [(:id row) row]))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers r))
        review (:dependency-review result) consumers (set (:consumer-task-ids r))
        selected (filterv #(consumers (:consumer %)) (:routes review))
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        dep (:dependency-source r)
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-map (into {} (map (juxt :consumer identity) old-routes))
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        added {"ai"
 ["legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
 [],
 "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
 ["runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "backend-gpu"
 ["domain-ir-boundaries"
  "gpu"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "backend-interface"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"],
 "backend-hdl"
 ["domain-ir-boundaries"
  "hardware"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "backend-interface"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "schema"],
 "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
 [],
 "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
 ["core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"],
 "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
 [],
 "evolution/change-review"
 ["runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
  "hardware"
  "gpu"],
 "gpu"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "domain-ir-boundaries"
  "backend-gpu"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "math"],
 "hardware"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "domain-ir-boundaries"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "backend-hdl"
  "schema"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"],
 "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"
 ["core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
  "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "backend-gpu"
  "backend-hdl"
  "hardware"
  "gpu"],
 "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
 ["schema" "gpu" "hardware"],
 "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
 ["hardware" "gpu"],
 "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
 ["effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "schema"],
 "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
 ["core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
  "backend-gpu"
  "backend-hdl"
  "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
 ["legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
  "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality"
  "opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "lower/current-evidence-and-remaining-work/lower3-select-explicit-providers"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "backend-interface"],
 "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality"
 ["mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir"
  "mir/current-evidence-and-remaining-work/mir2-attach-fact-tables"
  "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
  "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "pass-evidence"],
 "release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations"
 ["legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"],
 "release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures"
 ["legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"
  "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality"
  "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "backend-gpu"
  "backend-hdl"],
 "release/remaining-tasks-and-evidence/execute-language-conformance-fixtures"
 ["legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"
  "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
  "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
  "hardware"
  "gpu"],
 "release/remaining-tasks-and-evidence/execute-library-conformance-fixtures"
 ["runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
  "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"],
 "release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures"
 ["backend-gpu" "backend-hdl" "hardware" "gpu"],
 "release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures"
 ["legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"
  "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"],
 "release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures"
 ["runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "backend-gpu"
  "backend-hdl"],
 "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "schema"],
 "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
 ["legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
 ["runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "backend-interface"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
 ["core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
  "schema"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
  "backend-gpu"],
 "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
 ["runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "workflow"
 ["legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"]}
        expected (into {} (for [[id inputs] added] [id (set (concat (:composition-results (old-map id)) inputs))]))
        complete? (fn [rows] (= expected (into {} (map (juxt :consumer (comp set :composition-results)) rows))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (doseq [k [:task-authorities :future-task-authorities :sh07-sequence :accepted-current-scoped-evidence]] (is (= (k prior) (k view)) k))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (doseq [k [:concurrency-refinement :source-effects-dependency-refinement :source-effects-refinement :memory-model-dependency-refinement :source-input-commit :task-comparisons :memory-model-refinement :memory-refinement :memory-dependency-refinement
               :root-safety-refinement :root-safety-dependency-refinement :ownership-refinement :ownership-dependency-refinement :safety-refinement :safety-dependency-refinement]]
      (is (= (k old) (k bridge)) k))
    (is (= 32 (count (:source-files bridge))))
    (is (= 149 (count tasks)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 71 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 68 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= [] (:changed-task-ids r) (:new-task-identities r)))
    (is (= 33 (count consumers) (count selected)))
    (is (= 10 (count (:new-consumer-task-ids r))))
    (is (= (set (:new-consumer-task-ids r)) (clojure.set/difference consumers (set (keys old-map)))))
    (is (= added (:added-composition-inputs r)))
    (is (complete? selected))
    (doseq [[id inputs] expected]
      (is (not (complete? (remove #(= id (:consumer %)) selected))))
      (doseq [input inputs]
        (is (not (complete? (mapv #(if (= id (:consumer %)) (update % :composition-results (fn [xs] (vec (remove #{input} xs)))) %) selected))))))
    (is (= 83 (count (:routes review))))
    (is (= (:routes r) (mapv #(update % :source dissoc :raw) selected)))
    (is (= 23 (count (:predecessor-selected-routes r))))
    (is (= 235 (:preserved-prior-input-joins r) (reduce + (map (comp count :composition-results) (:predecessor-selected-routes r)))))
    (is (= 407 (:selected-consumer-result-joins r) (reduce + (map (comp count :composition-results) selected))))
    (is (= 172 (:new-selected-producer-result-joins r) (reduce + (map count (vals added)))))
    (is (= 50 (:preserved-consumer-route-count r)))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) (:routes review))))))
    (doseq [row selected :let [id (:consumer row) before (old-map id)]]
      (is (empty? (:start-results row)))
      (when before
        (is (every? (set (:composition-results row)) (:composition-results before)))
        (is (str/starts-with? (:selected-output-contract row) (:selected-output-contract before))))
      (let [text (read-text (:path dep))
            pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote id) "` \\|[^\\n]*\\n"))
            without (str/replace text pattern "")
            missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                      #(if (= % (:path dep)) without (read-text %)) (:required-parent-consumers r))]
        (is (= 1 (count (re-seq pattern text))))
        (is (= [id] (get-in missing [:dependency-review :unresolved-consumers]))))
      (doseq [input (:composition-results row)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                       [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" input) :actual-selected-producer-result]
                       [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (= :unassessed (get-in result [:nodes (str "result:" id ":" input) :state])))))
    (is (= 698 (:all-selected-result-joins r) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 43 (count (:declared-parent-consumers r)) (count (:required-parent-consumers r)) (count (:declared-parent-consumers review))))
    (is (= 40 (count (:declared-future-consumers r))))
    (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
    (is (= [40 8] (mapv count (:coarse-producer-cycles r)) (mapv count (:declared-output-cycles review))))
    (let [old-deps (:dependency-review (acceptance-dependencies/future-task-view (:future-task-authorities prior) tasks (:source-snapshots prior)
                      #(sh-crosswalk/git-source (:commit previous) %)
                      (:required-parent-consumers (:source-effects-dependency-refinement old))))
          prior-cycles (:declared-output-cycles old-deps)
          expected-main (set (concat (first prior-cycles) (:new-consumer-task-ids r) ["domain-ir-boundaries"]))]
      (is (= [29 8] (mapv count prior-cycles)))
      (is (= prior-cycles (:predecessor-coarse-cycles r)))
      (is (= expected-main (set (first (:coarse-producer-cycles r)))))
      (is (= (set (second prior-cycles)) (set (second (:coarse-producer-cycles r)))))
      (is (= (set (conj (:new-consumer-task-ids r) "domain-ir-boundaries")) (set (:new-main-cycle-members r)))))
    (is (= 45 (count (:staged-result-subjects r)) (count (set (map :result (:staged-result-subjects r))))))
    (is (= 8 (count (:subject-phases r))))
    (let [all-inputs (into {} (map (juxt :consumer (comp set :composition-results)) (:routes review)))]
      (doseq [s (:staged-result-subjects r)]
        (is (= (:result s) (str "result:" (:consumer s) ":" (:producer s))))
        (is (contains? (all-inputs (:consumer s)) (:producer s)))
        (is (= (if (consumers (:consumer s)) :selected-amended-route :preserved-unchanged-route-context) (:route-scope s)))))
    (is (= (:route (:unchanged-cycle-stage-context r)) (update (old-map "domain-ir-boundaries") :source dissoc :raw)))
    (is (= :actual-applicable-action-unsafe-and-stable-change-review-separately-supplied-not-issued-by-runtime (:external-review-input r)))
    (is (= :all-finite-subjects-frozen-bounded-R6-sealed-consumed-once-not-renewed (:review-conservation r)))
    (is (= :source-definitions-then-checked-domain-payloads-then-emission-then-real-simulation-or-device-reference-result-no-hold-clearance (:hardware-GPU r)))
    (is (= :selected-shapes-before-messages-actual-codecs-where-used-no-full-migration-prerequisite (:schema-boundary r)))
    (is (= 14 (count (:plan-sources r))))
    (doseq [record (concat [dep (:dependency-tool-source r)] (:plan-sources r))
            :let [p (:path record) after (:after record) before (:before record) text (read-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p)))
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p)))))
    (is (= 30 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :declared-interfaces-no-whole-component-or-final-suite-start-gate (:ordinary-construction r)))
    (is (= :valid-unoptimized-verified-MIR-remains-legal-no-task-completion-gate (:program-legality r)))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :concurrency-dependency-refinement])))))


(deftest provider-capability-source-preserves-authorities-and-real-enforcement
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:authority-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) r (:authority-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        rows (fn [v] (into {} (for [rs (vals (:task-authorities v)) row rs] [(:id row) row])))
        before (rows prior) after (rows view)
        raw (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        changed (set (for [[id row] before :when (not= (raw prior row) (raw view (after id)))] id))
        span-text (fn [t s] (String. (.getBytes t "UTF-8") (int (:start-byte s)) (int (- (:end-byte s) (:start-byte s))) "UTF-8"))
        census (edn/read-string (sh-crosswalk/git-source (get-in r [:source-census :commit]) (get-in r [:source-census :path])))
        selected #{"L15" "SAFE10" "R11"}
        original-facets (filterv #(selected (:document-id %)) (:facets census))
        original-cases (filterv #(selected (:document-id %)) (:occurrences census))
        sources (into {} (for [{:keys [document-id path]} (:canonical-sources r)] [document-id (read-text path)]))
        facets (:source-facets r) cases (:occurrence-cases r) sections (:section-conjunctions r)
        criteria (:criteria r) finite (:finite-criteria r)
        criterion-keys (set (concat (map :key criteria) (map :key finite)))
        semantic? #(= :complete-source-predicate-conjunction (:role %))
        sections-valid? (fn [ss] (and (= (set (map :section-key (filter semantic? (vals cases)))) (set (keys ss)))
          (every? (fn [[k s]] (and (seq (:criterion-keys s)) (every? criterion-keys (:criterion-keys s))
            (= (set (:occurrence-ids s)) (set (for [[id c] cases :when (and (semantic? c) (= k (:section-key c)))] id))))) ss)))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= 149 (count before) (count after)))
    (is (= (set (keys before)) (set (keys after))))
    (is (= 25 (count (:task-authorities view))))
    (doseq [[id row] before] (is (= (:checked row) (:checked (after id))) id))
    (is (= 15 (count changed)))
    (is (= 17 (count criteria)))
    (is (= changed (set (:changed-task-ids r)) (set (map :task-id criteria))))
    (is (= [] (:new-task-identities r)))
    (is (= 4 (count (:newly-amended-original-task-ids r))))
    (is (= [67 72] [(get-in bridge [:task-comparisons :unchanged-body-count]) (count (get-in bridge [:task-comparisons :changed-bodies]))]))
    (is (= 32 (count (:source-files bridge))))
    (is (= 6 (count (:task-sources r))))
    (is (= 13 (count (:plan-sources r))))
    (is (= 3 (count (:specification-sources r))))
    (is (= (raw prior (before "evolution/change-review")) (raw view (after "evolution/change-review"))))
    (doseq [k [:future-task-authorities :sh07-sequence :accepted-current-scoped-evidence]] (is (= (k prior) (k view)) k))
    (is (= (mapv :task-references (:requirements prior)) (mapv :task-references (:requirements view))))
    (doseq [k [:concurrency-refinement :concurrency-dependency-refinement :source-effects-refinement :source-effects-dependency-refinement :memory-model-refinement :memory-model-dependency-refinement
               :memory-refinement :memory-dependency-refinement :root-safety-refinement :root-safety-dependency-refinement
               :ownership-refinement :ownership-dependency-refinement :safety-refinement :safety-dependency-refinement]]
      (is (= (k old) (k bridge)) k))
    (doseq [record (concat (:task-sources r) (:plan-sources r) (:specification-sources r))
            :let [p (:path record) a (:after record) b (:before record) text (read-text p)]]
      (is (= (:sha256 a) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit a) p)))
      (is (= (:sha256 b) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit b) p)))))
    (doseq [record (concat criteria finite) :let [w (:current-witness record) text (read-text (:path w)) clause (span-text text (:source-span w))]]
      (is (= (:sha256 w) (sh-inventory/sha256 text)))
      (is (= (get-in w [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (str/includes? (raw view (after (:task-id record))) clause)))
    (is (= 16 (count finite) (count (set (map :witness-key finite)))))
    (is (= 13 (count (set (map :id finite)))))
    (is (= {:profile74 2 :safety107 7 :runtime20 6 :schema25 1} (frequencies (map :family finite))))
    (is (= {:preserved-existing-exact-finite-predicate 6 :restored-exact-finite-predicate 10} (frequencies (map :disposition finite))))
    (doseq [record finite :let [w (:historical-witness record) text (sh-crosswalk/git-source (:source-commit w) (:path w))
                              historical (span-text text w) current (span-text (read-text (:path record)) (get-in record [:current-witness :source-span]))]]
      (is (= (:file-sha256 w) (:sha256 w) (sh-inventory/sha256 text)))
      (is (= (:raw-sha256 w) (sh-inventory/sha256 historical) (sh-inventory/sha256 current)))
      (is (= historical current)))
    (is (= 278 (count facets) (count (set (map :id facets)))))
    (is (= {"L15" 126 "SAFE10" 67 "R11" 85} (frequencies (map :document-id facets))))
    (is (= (set (map :id original-facets)) (set (map :id facets))))
    (is (= 181 (count cases)))
    (is (= (set (map :id original-cases)) (set (keys cases))))
    (is (= 46 (count sections)))
    (is (= {:retained-hierarchy-or-provenance-context 55 :complete-source-predicate-conjunction 223} (:current-role-population r)))
    (is (= {:retained-hierarchy-or-provenance-context 52 :complete-source-predicate-conjunction 129} (frequencies (map :role (vals cases)))))
    (is (sections-valid? sections))
    (doseq [k (keys sections)]
      (is (not (sections-valid? (dissoc sections k))))
      (is (not (sections-valid? (assoc-in sections [k :criterion-keys] [])))))
    (doseq [record (concat facets (vals cases)) :let [text (sources (:document-id record)) clause (span-text text (:source-span record))]]
      (is (= (get-in record [:source-span :sha256]) (sh-inventory/sha256 clause)))
      (is (= :proposal-pending-independent-review (:current-entailment record)))
      (is (every? criterion-keys (:criterion-keys record)))
      (when (semantic? record) (is (seq (:criterion-keys record))))
      (is (not= (get-in record [:source-span :sha256]) (sh-inventory/sha256 (str clause " altered")))))
    (is (= 20 (count (:owner-joins r))))
    (is (= (set (mapcat :owner-requirement-ids original-facets)) (set (keys (:owner-joins r)))))
    (doseq [[id row] (:owner-joins r)]
      (is (contains? after (:task-id row)) id)
      (is (seq (:criterion-keys row)) id)
      (is (every? criterion-keys (:criterion-keys row)) id)
      (is (= :unassessed (:whole-attached-owner-predicate row)))
      (is (= :none (:implementation-credit row))))
    (doseq [{:keys [path source-commit sha256]} (concat (:canonical-sources r) (:supporting-source-interfaces r) (get-in r [:implementation-source-inspection :files]))]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (doseq [{:keys [path source-commit sha256]} (filter :source-commit (:review-source-artifacts r))]
      (is (= sha256 (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= [:explicit-source :package-manifest :workspace-policy :profile-default :target-compiler-default] (get-in r [:allocation-reconciliation :selection-order])))
    (is (= 8 (count (get-in r [:allocation-reconciliation :grant-layers]))))
    (is (= 17 (count (get-in r [:allocation-reconciliation :runtime-actions]))))
    (is (= {:L15 11 :SAFE10 10 :R11 10} (get-in r [:allocation-reconciliation :diagnostics])))
    (is (= :capability-value-transport-not-provider-effect-or-grant-creation (get-in r [:allocation-reconciliation :pure-value])))
    (is (= :recorded-replay-exception-does-not-waive-grants-isolation-or-exact-L6-L12-hermetic-premises (get-in r [:allocation-reconciliation :compile-time-exception])))
    (is (= :selected-existing-Types-interface-no-full-C7-adoption-or-publication-hold-change (get-in r [:allocation-reconciliation :type-owner])))
    (is (= "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation" (get-in r [:allocation-reconciliation :cache-key-producer])))
    (is (= "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix" (get-in r [:allocation-reconciliation :reproducibility-consumer])))
    (is (= (get-in r [:allocation-reconciliation :cache-key-producer]) (:task-id (first (filter #(= :cache-producer (:key %)) criteria)))))
    (is (= 7 (count (get-in r [:historical-reviews :safety107 :selected-findings]))))
    (is (= :pending (get-in r [:historical-reviews :runtime20 :actual-composition])))
    (is (= :existing-completed-finite-pass-frozen (get-in r [:historical-reviews :schema25 :status])))
    (is (= :not-repeated (get-in r [:historical-reviews :schema25 :consumption])))
    (is (= :none (get-in r [:historical-reviews :schema25 :renewal])))
    (is (= :not-conferred (get-in r [:historical-reviews :schema25 :whole-source-current-or-implementation-acceptance])))
    (let [dispositions (:accounting-policy-dispositions r)]
      (is (= #{"facet/language/1442e26d59b802c6bbbb28b6" "safe10/scope-checks/block-2"} (set (map :facet-id dispositions))))
      (doseq [d dispositions]
        (is (= :preserved (:source-bytes d)))
        (is (= :project-work-call-token-fuel-accounting-removed (:current-policy d)))
        (is (= :all-non-accounting-category-identity-input-output-retention-evaluation-and-policy-conditions (:surviving-scope-predicates d)))
        (is (= :actual-provider-account-and-billing-restrictions-separate (:external-boundary d)))
        (is (= :complete-source-predicate-conjunction (:role (first (filter #(= (:id %) (:facet-id d)) facets)))))))
    (let [uncovered (fn [id rows]
                      (let [bytes (.getBytes (sources id) "UTF-8") covered (java.util.BitSet. (alength bytes))]
                        (doseq [f rows :when (= id (:document-id f)) :let [s (:source-span f)]]
                          (.set covered (int (:start-byte s)) (int (:end-byte s))))
                        (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))]
      (doseq [id selected :let [omitted (first (for [[k c] cases :when (and (= id (:document-id c)) (semantic? c))] k))]]
        (is (empty? (uncovered id facets)))
        (is (seq (uncovered id (remove #(= omitted (:source-occurrence-id %)) facets))))))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :authority-refinement])))))

(deftest provider-dependencies-preserve-grants-imports-and-single-cache-owner
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:compile-time-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) r (:authority-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        tasks (into {} (for [rs (vals (:task-authorities view)) row rs] [(:id row) row]))
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers r))
        review (:dependency-review result) consumers (set (:consumer-task-ids r))
        selected (filterv #(consumers (:consumer %)) (:routes review))
        dep (:dependency-source r)
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-map (into {} (map (juxt :consumer identity) old-routes))
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        added {"release/remaining-tasks-and-evidence/execute-library-conformance-fixtures"
 ["auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
  "math"],
 "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
 ["auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"],
 "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
 ["build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "schema"
  "pass-evidence"
  "build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records"],
 "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
 ["effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "schema"],
 "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
 ["auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"],
 "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"],
 "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "schema"],
 "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "schema"],
 "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"
 ["auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
  "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"],
 "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
 ["auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "schema"],
 "release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures"
 ["auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"],
 "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
 ["build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "schema"],
 "release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations"
 ["auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"],
 "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
 ["auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "schema"],
 "math"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
 ["auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"],
 "release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures"
 ["auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"],
 "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "schema"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"],
 "build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records"
 ["build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "schema"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"
  "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"],
 "release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures"
 ["auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"],
 "auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
  "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"
  "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
  "math"],
 "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
 ["build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "schema"],
 "release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures"
 ["auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"],
 "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"
 ["build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
  "build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"],
 "release/remaining-tasks-and-evidence/execute-language-conformance-fixtures"
 ["auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "math"],
 "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
 ["auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"],
 "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
 ["effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "schema"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"],
 "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"],
 "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
 ["auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"],
 "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
 ["auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"],
 "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
  "schema"]}

        expected (into {} (for [id consumers] [id (set (concat (:composition-results (old-map id)) (added id)))]))
        complete? (fn [rows] (= expected (into {} (map (juxt :consumer (comp set :composition-results)) rows))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (let [changed #{:input-fingerprint :applying-source-pins :additional-pinned-evidence-hashes :inspection-locators :source-files :authority-dependency-refinement}]
      (is (= (apply dissoc old changed) (apply dissoc bridge changed))))
    (is (= (:task-authorities prior) (:task-authorities view)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= 149 (count tasks)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 67 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 72 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= "d2812996f4433e2102daacff9c5f7b3194a0b5e9" (:source-input-commit old) (:source-input-commit bridge)))
    (is (= 32 (count (:source-files bridge))))
    (is (= (remove #(= (:path dep) (:path %)) (:source-files old)) (remove #(= (:path dep) (:path %)) (:source-files bridge))))
    (is (= [] (:changed-task-ids r) (:new-task-identities r)))
    (is (= 31 (count consumers) (count selected)))
    (is (= 8 (count (:new-consumer-task-ids r))))
    (is (= (set (:new-consumer-task-ids r)) (clojure.set/difference consumers (set (keys old-map)))))
    (is (= added (:added-composition-inputs r)))
    (is (complete? selected))
    (doseq [[id inputs] expected]
      (is (not (complete? (remove #(= id (:consumer %)) selected))))
      (doseq [input inputs]
        (is (not (complete? (mapv #(if (= id (:consumer %)) (update % :composition-results (fn [xs] (vec (remove #{input} xs)))) %) selected))))))
    (is (= 91 (count (:routes review))))
    (is (= (:routes r) (mapv #(update % :source dissoc :raw) selected)))
    (is (= 23 (count (:predecessor-selected-routes r))))
    (is (= 279 (:preserved-prior-input-joins r) (reduce + (map (comp count :composition-results) (:predecessor-selected-routes r)))))
    (is (= 415 (:selected-consumer-result-joins r) (reduce + (map (comp count :composition-results) selected))))
    (is (= 136 (:new-selected-producer-result-joins r) (reduce + (map count (vals added)))))
    (is (= 60 (:preserved-consumer-route-count r)))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) (:routes review))))))
    (doseq [row selected :let [id (:consumer row) before (old-map id)]]
      (is (empty? (:start-results row)))
      (when before
        (is (every? (set (:composition-results row)) (:composition-results before)))
        (is (str/starts-with? (:selected-output-contract row) (:selected-output-contract before))))
      (let [text (read-text (:path dep))
            pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote id) "` \\|[^\\n]*\\n"))
            missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                      #(if (= % (:path dep)) (str/replace text pattern "") (read-text %)) (:required-parent-consumers r))]
        (is (= 1 (count (re-seq pattern text))))
        (is (= [id] (get-in missing [:dependency-review :unresolved-consumers]))))
      (doseq [input (:composition-results row)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" input) :actual-selected-producer-result]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (= :unassessed (get-in result [:nodes (str "result:" id ":" input) :state])))))
    (is (= 834 (:all-selected-result-joins r) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 51 (count (:declared-parent-consumers r)) (count (:required-parent-consumers r)) (count (:declared-parent-consumers review))))
    (is (= 40 (count (:declared-future-consumers r))))
    (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
    (is (= [48 8] (mapv count (:coarse-producer-cycles r)) (mapv count (:declared-output-cycles review))))
    (let [prior-cycles (get-in old [:concurrency-dependency-refinement :coarse-producer-cycles])
          auth-cases "auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
          infer "effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
          members (conj (disj (set (:new-consumer-task-ids r)) auth-cases) infer)]
      (is (= prior-cycles (:predecessor-coarse-cycles r)))
      (is (= members (set (:new-main-cycle-members r))))
      (is (= (into (set (first prior-cycles)) members) (set (first (:coarse-producer-cycles r)))))
      (is (= (second prior-cycles) (second (:coarse-producer-cycles r)))))
    (is (= 41 (count (:staged-result-subjects r)) (count (set (map :result (:staged-result-subjects r))))))
    (is (= 14 (count (:subject-phases r))))
    (let [inputs (into {} (map (juxt :consumer (comp set :composition-results)) (:routes review)))]
      (doseq [stage (:staged-result-subjects r)]
        (is (= (:result stage) (str "result:" (:consumer stage) ":" (:producer stage))))
        (is (contains? (inputs (:consumer stage)) (:producer stage)))
        (is (= (if (consumers (:consumer stage)) :selected-amended-route :preserved-unchanged-route-context) (:route-scope stage)))))
    (is (= :preexisting-Q-verification-before-use-P-provenance-after-execution (:import-output-boundary r)))
    (is (= :existing-cache-task-alone-owns-keys-invalidation-reuse-reproducibility-consumes-results (:cache-ownership r)))
    (is (= :actual-grant-and-installed-gate-before-action-later-trace-not-prior-authorization (:gate-order r)))
    (is (= :all-finite-subjects-frozen-no-review-renewal-or-new-consumption (:review-conservation r)))
    (is (= :declared-interfaces-no-whole-component-suite-signing-publication-migration-start-gate (:ordinary-construction r)))
    (is (= 11 (count (:plan-sources r))))
    (doseq [record (concat [dep (:dependency-tool-source r)] (:plan-sources r))
            :let [p (:path record) after (:after record) before (:before record) text (read-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p)))
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p)))))
    (is (= 12 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :authority-dependency-refinement])))))

(deftest compile-time-source-preserves-values-phases-and-policy-without-fuel
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:compile-time-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version) r (:compile-time-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        read-text (if checkpoint #(sh-crosswalk/git-source (:commit checkpoint) %) slurp)
        rows (fn [v] (into {} (for [rs (vals (:task-authorities v)) row rs] [(:id row) row])))
        before (rows prior) after (rows view)
        raw-task (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        changed (set (for [[id row] before :when (not= (raw-task prior row) (raw-task view (after id)))] id))
        span (fn [text x] (String. (.getBytes text "UTF-8") (int (:start-byte x)) (int (- (:end-byte x) (:start-byte x))) "UTF-8"))
        census-text (sh-crosswalk/git-source (get-in r [:source-census :commit]) (get-in r [:source-census :path]))
        census (edn/read-string census-text)
        original-facets (filterv #(= "L12" (:document-id %)) (:facets census))
        original-cases (filterv #(= "L12" (:document-id %)) (:occurrences census))
        source (first (:canonical-sources r)) body (read-text (:path source))
        facets (:source-facets r) cases (:occurrence-cases r) sections (:section-conjunctions r)
        criteria (:criteria r) finite (:finite-criteria r) criterion-keys (set (concat (map :key criteria) (map :key finite)))
        semantic? #(= :complete-source-predicate-conjunction (:role %))
        sections-valid? (fn [ss] (and (= (set (map :section-key (filter semantic? (vals cases)))) (set (keys ss)))
          (every? (fn [[k s]] (and (seq (:criterion-keys s)) (every? criterion-keys (:criterion-keys s))
            (= (set (:occurrence-ids s)) (set (for [[id c] cases :when (and (semantic? c) (= k (:section-key c)))] id))))) ss)))
        witness-text (fn [w] (let [t (read-text (:path w))] (is (= t (sh-crosswalk/git-source (:source-commit w) (:path w))))
                              (is (= (:sha256 w) (sh-inventory/sha256 t))) (span t (:source-span w))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (let [modified #{:compile-time-refinement :source-input-commit :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes :task-comparisons :qualification :input-fingerprint}]
      (is (= (apply dissoc old modified) (apply dissoc bridge modified))))
    (is (= 149 (count before) (count after)))
    (is (= 25 (count (:task-authorities view))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= 32 (count (:source-files bridge))))
    (is (= 61 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 78 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= (:source-commit r) (:source-input-commit bridge)))
    (is (= 12 (count changed) (count (set (map :task-id criteria)))))
    (is (= changed (set (:changed-task-ids r))))
    (is (= #{"pipeline" "plugins" "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
             "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
             "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
             "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"}
           (set (:newly-amended-original-task-ids r))))
    (is (= [] (:new-task-identities r)))
    (doseq [[id row] before] (is (= (:checked row) (:checked (after id))) id))
    (is (= (get-in r [:source-census :sha256]) (sh-inventory/sha256 census-text)))
    (is (= (:sha256 source) (sh-inventory/sha256 body) (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit source) (:path source)))))
    (is (= 131 (count facets) (count original-facets)))
    (is (= (set (map :id original-facets)) (set (map :id facets))))
    (is (= 89 (count cases) (count original-cases)))
    (is (= (set (map :id original-cases)) (set (keys cases))))
    (is (= {:complete-source-predicate-conjunction 106 :retained-hierarchy-or-provenance-context 25} (:current-role-population r)))
    (is (= {:complete-source-predicate-conjunction 67 :retained-hierarchy-or-provenance-context 22} (frequencies (map :role (vals cases)))))
    (doseq [f facets :let [c (cases (:source-occurrence-id f))]]
      (is (= (get-in f [:source-span :sha256]) (sh-inventory/sha256 (span body (:source-span f)))))
      (is (= (:role c) (:role f)))
      (is (= (:criterion-keys c) (:criterion-keys f)))
      (is (= (:section-key c) (:section-key f))))
    (doseq [[_id c] cases]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 (span body (:source-span c)))))
      (is (if (semantic? c) (and (seq (:criterion-keys c)) (every? criterion-keys (:criterion-keys c))) (empty? (:criterion-keys c))))
      (is (= :proposal-pending-independent-review (:current-entailment c))))
    (is (= 20 (count sections)))
    (is (sections-valid? sections))
    (doseq [[k _section] sections]
      (is (not (sections-valid? (dissoc sections k)))))
    (let [criterion-map (into {} (map (juxt :key identity) criteria))]
      (is (= 15 (count criterion-map)))
      (doseq [criterion criteria :let [w (:current-witness criterion) text (witness-text w)]]
        (is (str/includes? (raw-task view (after (:task-id criterion))) text))
        (is (= (get-in w [:source-span :sha256]) (sh-inventory/sha256 text)))
        (is (= :author-proposed-whole-source-criterion-no-independent-acceptance (:status criterion))))
      (is (str/includes? (witness-text (:current-witness (criterion-map :evaluate))) "Actually evaluate"))
      (is (str/includes? (witness-text (:current-witness (criterion-map :reuse))) "Build alone constructs keys"))
      (is (str/includes? (witness-text (:current-witness (criterion-map :eligibility))) "replacement mandatory totality admission")))
    (is (= 5 (count finite)))
    (is (= {:restored-exact-finite-predicate 3 :preserved-existing-exact-finite-predicate 2} (frequencies (map :disposition finite))))
    (doseq [f finite :let [w (:historical-witness f) text (sh-crosswalk/git-source (:source-commit w) (:path w)) historical (span text w) actual (witness-text (:current-witness f))]]
      (is (= (:sha256 w) (sh-inventory/sha256 text)))
      (is (= historical actual))
      (is (= (:raw-sha256 w) (sh-inventory/sha256 actual)))
      (is (= :profile74 (:frozen-review f)))
      (is (= :not-conferred (:whole-source-acceptance f)))
      (is (= :pending (:current-composition f))))
    (is (= 8 (count (:owner-joins r))))
    (is (= (set (mapcat :owner-requirement-ids original-facets)) (set (keys (:owner-joins r)))))
    (doseq [[_id owner] (:owner-joins r)]
      (is (contains? after (:task-id owner)))
      (is (every? criterion-keys (:criterion-keys owner)))
      (is (= :unassessed (:whole-attached-owner-predicate owner)))
      (is (= :none (:implementation-credit owner))))
    (let [a (:allocation-reconciliation r)]
      (is (= 13 (count (:build-effects a))))
      (is (= 9 (count (:profiles a)) (count (:phases a))))
      (is (= {:active 9 :source 10 :superseded ["L12-FUEL"]} (:diagnostics a)))
      (is (= :removed-without-renamed-bound-or-mandatory-replacement-totality-proof (:fuel a)))
      (is (= :existing-L6-denial-versus-L12-declared-key-value-policy-source-decision-pending (:hermetic-environment a)))
      (is (= :existing-D1-C1-C5-C6-architecture-owner-resolution-pending (:phase-order-decision a))))
    (is (= 5 (count (:accounting-policy-dispositions r))))
    (is (= {:project-fuel-refusal-and-replacement-mandatory-totality-admission 3 :project-model-tool-cost-accounting 2}
           (frequencies (map :superseded-condition (:accounting-policy-dispositions r)))))
    (doseq [d (:accounting-policy-dispositions r)]
      (is (= :preserved (:source-bytes d)))
      (is (= :project-work-call-token-fuel-accounting-removed (:current-policy d)))
      (is (= (:source-span d) (:source-span (first (filter #(= (:facet-id d) (:id %)) facets))))))
    (doseq [record (concat (:task-sources r) (:plan-sources r) (:specification-sources r)) :let [p (:path record) after (:after record) before (:before record) text (read-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p)))
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p)))))
    (is (= [4 14 3 8] (mapv count [(:task-sources r) (:plan-sources r) (:specification-sources r) (get-in r [:implementation-source-inspection :files])])))
    (doseq [{:keys [path source-commit sha256]} (get-in r [:implementation-source-inspection :files])]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (let [uncovered (fn [rows] (let [bytes (.getBytes body "UTF-8") covered (java.util.BitSet. (alength bytes))]
                               (doseq [f rows :let [s (:source-span f)]] (.set covered (int (:start-byte s)) (int (:end-byte s))))
                               (filterv #(and (> (bit-and 255 (aget bytes %)) 32) (not (.get covered %))) (range (alength bytes)))))
          omitted (first (for [[id c] cases :when (semantic? c)] id))]
      (is (empty? (uncovered facets)))
      (is (seq (uncovered (remove #(= omitted (:source-occurrence-id %)) facets)))))
    (is (= :none (get-in r [:historical-reviews :renewal]) (get-in r [:historical-reviews :new-consumption])))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :compile-time-refinement])))))

(deftest compile-time-dependencies-preserve-P-Q-cache-and-phase-products
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:macro-cache-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (fn [p] (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) p) (slurp p)))
        r (:compile-time-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        tasks (into {} (for [rs (vals (:task-authorities view)) row rs] [(:id row) row]))
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers r))
        review (:dependency-review result) consumers (set (:consumer-task-ids r))
        selected (filterv #(consumers (:consumer %)) (:routes review))
        dep (:dependency-source r)
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-map (into {} (map (juxt :consumer identity) old-routes))
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        added {"release/remaining-tasks-and-evidence/execute-library-conformance-fixtures"
 ["expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "schema"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"],
 "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
 ["resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "schema"
  "plugins"],
 "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"
 ["diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema"
  "diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order"
  "diag/remaining-tasks-and-evidence/close-textjsonredaction-and-explain-contracts"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "plugins"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "schema"],
 "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
 ["resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "plugins"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"],
 "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "plugins"],
 "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
 ["syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "plugins"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "schema"],
 "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
 ["types/current-evidence-and-remaining-work/types1-solve-core-constraints"
  "types/current-evidence-and-remaining-work/types2-check-declared-abstractions"
  "types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit"
  "types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"],
 "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
 ["resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions"
  "resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries"
  "resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality"
  "resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes"
  "syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"],
 "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
 ["expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"],
 "schema"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"],
 "release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures"
 ["expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "plugins"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"],
 "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
 ["pipeline"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "plugins"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"],
 "release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations"
 ["expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "plugins"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"],
 "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
 ["build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "schema"],
 "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "plugins"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"],
 "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"],
 "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "plugins"
  "syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "schema"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"],
 "release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures"
 ["expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"],
 "effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
 ["resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "plugins"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"],
 "build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "plugins"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "pipeline"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"],
 "release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures"
 ["expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"],
 "pipeline"
 ["syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "plugins"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"],
 "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "plugins"
  "pipeline"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"],
 "release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures"
 ["expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "plugins"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"],
 "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "plugins"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "pipeline"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "schema"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"],
 "syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers"
 ["syntax/current-evidence-and-remaining-work/syntax1-construct-canonical-syntax"
  "syntax/current-evidence-and-remaining-work/syntax2-preserve-transformations"
  "syntax/current-evidence-and-remaining-work/syntax3-invalidate-facts"
  "syntax/current-evidence-and-remaining-work/syntax4-serialize-and-verify"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "plugins"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "schema"],
 "release/remaining-tasks-and-evidence/execute-language-conformance-fixtures"
 ["expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"
  "schema"],
 "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
 ["core/current-evidence-and-remaining-work/core1-lower-core-forms"
  "core/current-evidence-and-remaining-work/core2-preserve-binding-structure"
  "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "schema"],
 "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "plugins"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"],
 "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
 ["types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"],
 "plugins"
 ["pipeline"
  "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
  "schema"
  "pass-evidence"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"],
 "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "plugins"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"],
 "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
 ["resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "plugins"
  "pipeline"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"]}

        expected (into {} (for [id consumers] [id (set (concat (:composition-results (old-map id)) (added id)))]))
        complete? (fn [rows] (= expected (into {} (map (juxt :consumer (comp set :composition-results)) rows))))]
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 prior-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (let [changed #{:input-fingerprint :applying-source-pins :additional-pinned-evidence-hashes :inspection-locators :source-files :compile-time-dependency-refinement}]
      (is (= (apply dissoc old changed) (apply dissoc bridge changed))))
    (is (= (:task-authorities prior) (:task-authorities view)))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= 149 (count tasks)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 61 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 78 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= "6dfaf54be773f7eece444c2e73ac08ec4ae4368a" (:source-input-commit old) (:source-input-commit bridge)))
    (is (= 32 (count (:source-files bridge))))
    (is (= (remove #(= (:path dep) (:path %)) (:source-files old)) (remove #(= (:path dep) (:path %)) (:source-files bridge))))
    (is (= [] (:changed-task-ids r) (:new-task-identities r)))
    (is (= 33 (count consumers) (count selected)))
    (is (= 11 (count (:new-consumer-task-ids r))))
    (is (= (set (:new-consumer-task-ids r)) (clojure.set/difference consumers (set (keys old-map)))))
    (is (= added (:added-composition-inputs r)))
    (is (complete? selected))
    (doseq [[id inputs] expected]
      (is (not (complete? (remove #(= id (:consumer %)) selected))))
      (doseq [input inputs]
        (is (not (complete? (mapv #(if (= id (:consumer %)) (update % :composition-results (fn [xs] (vec (remove #{input} xs)))) %) selected))))))
    (is (= 102 (count (:routes review))))
    (is (= (:routes r) (mapv #(update % :source dissoc :raw) selected)))
    (is (= 22 (count (:predecessor-selected-routes r))))
    (is (= 267 (:preserved-prior-input-joins r) (reduce + (map (comp count :composition-results) (:predecessor-selected-routes r)))))
    (is (= 509 (:selected-consumer-result-joins r) (reduce + (map (comp count :composition-results) selected))))
    (is (= 242 (:new-selected-producer-result-joins r) (reduce + (map count (vals added)))))
    (is (= 69 (:preserved-consumer-route-count r)))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) (:routes review))))))
    (doseq [row selected :let [id (:consumer row) before (old-map id)]]
      (is (empty? (:start-results row)))
      (when before
        (is (every? (set (:composition-results row)) (:composition-results before)))
        (is (str/starts-with? (:selected-output-contract row) (:selected-output-contract before))))
      (let [text (read-text (:path dep))
            pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote id) "` \\|[^\\n]*\\n"))
            missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                      #(if (= % (:path dep)) (str/replace text pattern "") (read-text %)) (:required-parent-consumers r))]
        (is (= 1 (count (re-seq pattern text))))
        (is (= [id] (get-in missing [:dependency-review :unresolved-consumers]))))
      (doseq [input (:composition-results row)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" input) :actual-selected-producer-result]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (= :unassessed (get-in result [:nodes (str "result:" id ":" input) :state])))))
    (is (= 1076 (:all-selected-result-joins r) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 62 (count (:declared-parent-consumers r)) (count (:required-parent-consumers r)) (count (:declared-parent-consumers review))))
    (is (= 40 (count (:declared-future-consumers r))))
    (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
    (is (= [60 8] (mapv count (:coarse-producer-cycles r)) (mapv count (:declared-output-cycles review))))
    (let [prior-cycles (get-in old [:authority-dependency-refinement :coarse-producer-cycles])
          core1 "core/current-evidence-and-remaining-work/core1-lower-core-forms"
          members (conj (set (:new-consumer-task-ids r)) core1)]
      (is (= [48 8] (mapv count prior-cycles)))
      (is (= prior-cycles (:predecessor-coarse-cycles r)))
      (is (= members (set (:new-main-cycle-members r))))
      (is (= (into (set (first prior-cycles)) members) (set (first (:coarse-producer-cycles r)))))
      (is (= (second prior-cycles) (second (:coarse-producer-cycles r)))))
    (is (= (:route (:unchanged-cycle-stage-context r)) (update (old-map "core/current-evidence-and-remaining-work/core1-lower-core-forms") :source dissoc :raw)))
    (is (= 58 (count (:staged-result-subjects r)) (count (set (map :result (:staged-result-subjects r))))))
    (is (= 14 (count (:subject-phases r))))
    (let [inputs (into {} (map (juxt :consumer (comp set :composition-results)) (:routes review)))]
      (doseq [stage (:staged-result-subjects r)]
        (is (= (:result stage) (str "result:" (:consumer stage) ":" (:producer stage))))
        (is (contains? (inputs (:consumer stage)) (:producer stage)))
        (is (= (if (consumers (:consumer stage)) :selected-amended-route :preserved-unchanged-route-context) (:route-scope stage)))))
    (is (= :checked-available-P-before-invocation-generated-Q-before-ordinary-Q-checks (:P-Q-boundary r)))
    (is (= :early-fixed-target-rules-before-query-derived-Q-layout-after-real-values-no-canonical-layout-substitution (:target-boundary r)))
    (is (= :Build-alone-constructs-invalidates-revalidates-Expand-consumes-Pipeline-integrates (:cache-ownership r)))
    (is (= :real-evaluation-records-before-persistence-repeat-comparison-and-separately-authorized-archive-sign-publish (:record-order r)))
    (is (= :coarse-task-feedback-not-actual-build-permission-PKG2-acyclic-or-governing-marked-bootstrap-fixed-point (:build-cycle-rule r)))
    (is (= :existing-D1-C1-C5-C6-order-and-L6-L12-environment-wording-remain-pending (:source-decisions r)))
    (is (= :all-finite-subjects-frozen-no-renewal-consumption-or-adoption (:review-conservation r)))
    (is (= :declared-interfaces-no-whole-component-suite-registration-migration-signing-publication-start-gate (:ordinary-construction r)))
    (is (= 16 (count (:plan-sources r))))
    (doseq [record (concat [dep (:dependency-tool-source r)] (:plan-sources r))
            :let [p (:path record) after (:after record) before (:before record) text (read-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 text)))
      (is (= text (sh-crosswalk/git-source (:commit after) p)))
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p)))))
    (is (= 10 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :compile-time-dependency-refinement])))))

(deftest macro-engine-cache-machinery-preserves-shared-contract-and-frozen-predecessor
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:macro-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (fn [p] (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) p) (slurp p)))
        r (:macro-cache-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        tasks (into {} (for [rs (vals (:task-authorities view)) row rs] [(:id row) row]))
        old-tasks (into {} (for [rs (vals (:task-authorities prior)) row rs] [(:id row) row]))
        changed (set (:changed-task-ids r))
        allowed #{:input-fingerprint :source-input-commit :macro-cache-refinement :task-comparisons :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes}
        ownership (:current-ownership r)
        expand (read-text "specs/expand-workflow/tasks.md") build (read-text "specs/build-workflow/tasks.md")
        plan (read-text "specs/expand-workflow/plan.md")]
    (is (= "32bb4f31a5b5bddd23a59ee7672b535ee4af5c46" (:commit previous)))
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
    (is (= 149 (count tasks) (count old-tasks)))
    (is (= 25 (count (:task-authorities view))))
    (is (= (set (keys tasks)) (set (keys old-tasks))))
    (is (= 4 (count changed)))
    (is (= #{} (set (:new-task-identities r))))
    (doseq [[id row] tasks :let [before (old-tasks id)]]
      (is (= (:checked before) (:checked row)))
      (if (changed id)
        (is (not= (get-in before [:source :sha256]) (get-in row [:source :sha256])))
        (is (= (get-in before [:source :sha256]) (get-in row [:source :sha256])))))
    (is (= 61 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 78 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= (get-in old [:task-comparisons :unchanged-body-count]) (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= (set (map :id (get-in old [:task-comparisons :changed-bodies]))) (set (map :id (get-in bridge [:task-comparisons :changed-bodies])))))
    (is (= 23 (count (:replacements r))))
    (doseq [{:keys [path before after old new marker]} (:replacements r)
            :let [a (sh-crosswalk/git-source (:commit before) path) b (read-text path)]]
      (is (= (:sha256 before) (sh-inventory/sha256 old)))
      (is (= (:sha256 after) (sh-inventory/sha256 new)))
      (is (str/includes? a old))
      (is (str/includes? b new))
      (is (not (str/includes? b old)))
      (when marker
        (let [block (second (re-find (re-pattern (str "(?s)<!-- " marker ":start -->(.*?)<!-- " marker ":end -->")) b))]
          (is (string? block))
          (is (str/includes? block new))
          (is (not (str/includes? (str/replace block new "") new))))))
    (is (= 7 (count (:source-files r))))
    (doseq [{:keys [path before after]} (:source-files r)]
      (is (= (:source-commit r) (:commit after) (get-in bridge [:applying-source-pins path])))
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
      (is (= (:sha256 after) (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path)))))
    (doseq [[_ id] ownership] (is (tasks id)))
    (is (= (ownership :shared-default-construction-and-policy-integration) "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"))
    (is (= (ownership :engine-specific-machinery-and-reuse-integration) "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"))
    (is (= :structural-up-to-permitted-alpha-renaming (get-in r [:equivalence :reference-versus-alternative])))
    (is (= :byte-identical (get-in r [:equivalence :same-input-hermetic-typed-Core])))
    (is (= :engine-specific-with-complete-semantic-inputs-and-shared-contracts (get-in r [:equivalence :internal-keys])))
    (is (= (:sha256 (:boundary r)) (sh-inventory/sha256 (:raw (:boundary r)))))
    (is (str/includes? plan (:raw (:boundary r))))
    (doseq [fragment ["selected L16 engine-specific cache machinery" "complete key/input/entry/trace/replay/current-policy results" "Do not require identical internal keys" "without repeating external work on a hit" "looser policy never legalizes hidden effects"]]
      (is (str/includes? expand fragment)))
    (doseq [fragment ["shared/default cache construction" "same contracts" "all ten L12 input groups" "invalidate stricter-policy" "creation-time hidden effects" "secret values"]]
      (is (str/includes? build fragment)))
    (is (= 4 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :superseded-only-for-current-proposal-earlier-subjects-frozen (:historical-ownership-wording r)))
    (let [dep (:dependency-source r)
          ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
          before (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
          after (acceptance-dependencies/future-dependency-routes (read-text (:path dep)) ids ids)
          before-map (into {} (map (juxt :consumer identity) before))
          meaning (fn [rows] (into {} (for [row rows] [(:consumer row) (select-keys row [:start-results :composition-results])])))
          changed-contracts (filterv #(not= (:selected-output-contract %) (:selected-output-contract (before-map (:consumer %)))) after)]
      (is (= 102 (count before) (count after)))
      (is (= (meaning before) (meaning after)))
      (is (= 4 (count changed-contracts) (count (:dependency-route-corrections r))))
      (is (= (set (map :consumer changed-contracts)) (set (map :consumer (:dependency-route-corrections r)))))
      (doseq [row (:dependency-route-corrections r)
              :let [actual (first (filter #(= (:consumer %) (:consumer row)) after))]]
        (is (= (:before-contract row) (:selected-output-contract (before-map (:consumer row)))))
        (is (= (:after-contract row) (:selected-output-contract actual)))
        (is (= (:source-file-sha256 row) (get-in actual [:source :sha256]))))
      (is (= 1076 (get-in r [:dependency-conservation :selected-result-joins]))))
    (is (= (:compile-time-refinement old) (:compile-time-refinement bridge)))
    (is (= (:compile-time-dependency-refinement old) (:compile-time-dependency-refinement bridge)))
    (is (= :complete-L4-L16-C4-SAFE12-predicates-and-selected-dependency-staging (:remaining r)))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (let [actual (get-in view [:implementation-readiness-source-version :macro-cache-refinement])]
      (is (= (dissoc r :boundary) (dissoc actual :boundary)))
      (is (= (get-in r [:boundary :raw]) (acceptance-dependencies/source-raw (:boundary actual) (:source-snapshots view)))))))

(deftest complete-macro-source-preserves-syntax-safety-engine-and-finite-boundaries
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:macro-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (fn [p] (if checkpoint (sh-crosswalk/git-source (:commit checkpoint) p) (slurp p)))
        r (:macro-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        tasks (into {} (for [rows (vals (:task-authorities view)) row rows] [(:id row) row]))
        before (into {} (for [rows (vals (:task-authorities prior)) row rows] [(:id row) row]))
        changed (set (:changed-task-ids r))
        criteria (:criteria r) criterion-map (into {} (map (juxt :key identity) criteria))
        finite (:finite-criteria r) valid-keys (set (concat (keys criterion-map) (map :key finite)))
        facets (:source-facets r) cases (:occurrence-cases r) sections (:section-conjunctions r) owners (:owner-joins r)
        docs (into {} (map (juxt :document-id identity) (:canonical-sources r)))
        texts (into {} (for [[id d] docs] [id (read-text (:path d))]))
        span-text (fn [text span] (String. (.getBytes text "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        witness-text (fn [w] (span-text (read-text (:path w)) (:source-span w)))
        task-text (fn [row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots view)))
        old-task-text (fn [row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots prior)))
        semantic? #(= :complete-source-predicate-conjunction (:role %))
        allowed #{:input-fingerprint :source-input-commit :macro-refinement :task-comparisons :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes :qualification}]
    (is (= "fa8124072057b2ba45ecfa3619a98f114e561f4a" (:commit previous)))
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
    (is (= (:source-commit r) (:source-input-commit bridge)))
    (is (= 149 (count tasks) (count before)))
    (is (= 25 (count (:task-authorities view))))
    (is (= (set (keys tasks)) (set (keys before))))
    (is (= 15 (count changed)))
    (is (= 3 (count (:newly-amended-original-task-ids r))))
    (is (= #{"syntax/current-evidence-and-remaining-work/syntax1-construct-canonical-syntax"
             "syntax/current-evidence-and-remaining-work/syntax2-preserve-transformations"
             "resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes"}
           (set (:newly-amended-original-task-ids r))))
    (is (= [] (:new-task-identities r)))
    (is (= 58 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 81 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= 32 (count (:source-files bridge))))
    (let [successor (:future-task-source-successor r)
          fields [:id :region :component :path :checked :body-sha256]
          old-future (into {} (map (juxt :id identity) (:before successor)))
          actual-future (into {} (for [row (mapcat val (:future-task-authorities view))]
                                 [(:id row) (assoc (select-keys row [:id :region :component :path :checked :source-commit :file-sha256]) :body-sha256 (get-in row [:source :sha256]))]))]
      (is (= (:before successor) (get-in old [:future-dependency-refinement :task-records])))
      (is (= 40 (count (:before successor)) (count (:after successor)) (count actual-future)))
      (is (= actual-future (into {} (map (juxt :id identity) (:after successor)))))
      (is (= 13 (count (:changed-source-binding-task-ids successor))))
      (doseq [row (:after successor)]
        (is (= (select-keys row fields) (select-keys (old-future (:id row)) fields))))
      (doseq [id (:changed-source-binding-task-ids successor)]
        (is (= "specs/release-workflow/tasks.md" (:path (actual-future id))))
        (is (= (:source-commit r) (:source-commit (actual-future id)))))
      (with-redefs [sh-crosswalk/readiness-source-version (update-in bridge [:macro-refinement] dissoc :future-task-source-successor)]
        (is (thrown? Exception (sh-crosswalk/source-view))))
      (with-redefs [sh-crosswalk/readiness-source-version (assoc-in bridge [:macro-refinement :future-task-source-successor :after 0 :body-sha256] "forged")]
        (is (thrown? Exception (sh-crosswalk/source-view)))))
    (doseq [[id row] tasks]
      (is (= (:checked (before id)) (:checked row)))
      (if (changed id)
        (let [restored (reduce (fn [text marker]
                         (if (str/includes? text (str "<!-- " marker ":start -->"))
                           (str/replace text (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->(?:\n\n|\\z)")) "") text))
                       (task-text row) (vals (:markers r)))]
          (is (= (str/trimr (old-task-text (before id))) (str/trimr restored))))
        (is (= (old-task-text (before id)) (task-text row)))))
    (is (= 25 (count criteria) (count criterion-map)))
    (is (= 16 (count (set (map :task-id criteria)))))
    (is (= 24 (count (:markers r))))
    (is (= 10 (count finite) (count (set (map :witness-key finite)))))
    (is (= {:profile74 3 :safety107 7} (frequencies (map :family finite))))
    (is (= {:restored-exact-finite-predicate 7 :preserved-existing-exact-finite-predicate 3} (frequencies (map :disposition finite))))
    (doseq [{:keys [key task-id current-witness]} criteria
            :let [raw (witness-text current-witness) row (tasks task-id) text (read-text (:path current-witness))]]
      (is (some? row))
      (is (= (:source-commit r) (:source-commit current-witness)))
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (is (str/includes? (task-text row) raw))
      (is (nil? (acceptance-dependencies/current-task-body! (:source row) (:source-snapshots view) read-text)))
      (is (thrown? Exception (acceptance-dependencies/current-task-body! (:source row) (:source-snapshots view)
                              #(if (= % (:path current-witness)) (str/replace text raw "") (read-text %)))))
      (if (= key :change)
        (is (= :existing-exact-finite-predicate-no-task-amendment (:status (criterion-map key))))
        (is (= :author-proposed-whole-source-criterion-no-independent-acceptance (:status (criterion-map key))))))
    (doseq [{:keys [family task-id historical-witness current-witness current-composition whole-source-acceptance]} finite
            :let [historical (sh-crosswalk/git-source (:source-commit historical-witness) (:path historical-witness))
                  raw (span-text historical historical-witness) current (witness-text current-witness)]]
      (is (= (:sha256 historical-witness) (sh-inventory/sha256 historical)))
      (is (= (:raw-sha256 historical-witness) (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 raw) (sh-inventory/sha256 current)))
      (is (= raw current))
      (is (str/includes? (task-text (tasks task-id)) current))
      (is (= :pending current-composition))
      (is (= :not-conferred whole-source-acceptance))
      (is (contains? #{:profile74 :safety107} family)))
    (is (= :none (get-in r [:historical-reviews :renewal]) (get-in r [:historical-reviews :new-consumption])))
    (is (= 374 (count facets) (count (set (map :id facets)))))
    (is (= {"L4" 91 "L16" 110 "C4" 121 "SAFE12" 52} (frequencies (map :document-id facets))))
    (is (= {:complete-source-predicate-conjunction 298 :retained-hierarchy-or-provenance-context 76} (frequencies (map :role facets))))
    (is (= 230 (count cases)))
    (is (= {:complete-source-predicate-conjunction 160 :retained-hierarchy-or-provenance-context 70} (frequencies (map :role (vals cases)))))
    (is (= 62 (count sections)))
    (doseq [facet facets
            :let [raw (span-text (texts (:document-id facet)) (:source-span facet)) c (cases (:source-occurrence-id facet))]]
      (is (= (get-in facet [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (is (= (:section-key facet) (:section-key c)))
      (is (= (:role facet) (:role c)))
      (is (= (:criterion-keys facet) (:criterion-keys c)))
      (is (every? valid-keys (:criterion-keys facet)))
      (is (= :proposal-pending-independent-review (:current-entailment facet)))
      (if (semantic? facet) (is (seq (:criterion-keys facet))) (is (empty? (:criterion-keys facet))))
      (is (every? #(contains? owners %) (:owner-requirement-ids facet))))
    (doseq [[id c] cases :let [raw (span-text (texts (:document-id c)) (:source-span c))]]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (is (every? valid-keys (:criterion-keys c)))
      (when (semantic? c)
        (is (some #{id} (get-in sections [(:section-key c) :occurrence-ids])))
        (is (= (:criterion-keys c) (get-in sections [(:section-key c) :criterion-keys])))))
    (doseq [[section row] sections]
      (is (seq (:occurrence-ids row)))
      (is (every? valid-keys (:criterion-keys row)))
      (is (= :whole-section-preamble-list-example-and-following-conditions-required (:qualification row)))
      (doseq [id (:occurrence-ids row)] (is (= section (:section-key (cases id))))))
    (is (= 26 (count owners)))
    (doseq [[id row] owners]
      (is (= id (:id row)))
      (is (tasks (:task-id row)))
      (is (seq (:original-obligation row)))
      (is (every? valid-keys (:criterion-keys row)))
      (is (every? valid-keys (:supporting-producer-consumer-criteria row)))
      (is (= :proposal-pending-independent-review (:current-entailment row)))
      (is (= :unassessed (:whole-attached-owner-predicate row)))
      (is (= :none (:implementation-credit row))))
    (is (= "evolution/change-review" (get-in owners ["language/macro/version-semantics-change" :task-id])))
    (is (= "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety" (get-in owners ["safety/safe12/hygiene" :task-id])))
    (is (= :no-invented-receipt-new-current-source-proposal (get-in r [:allocation-reconciliation :hygiene-finite-review])))
    (is (= 14338 (get-in r [:allocation-reconciliation :preserved-finite-bytes])))
    (is (= 13 (count (get-in r [:allocation-reconciliation :build-effects]))))
    (is (= 11 (count (get-in r [:allocation-reconciliation :syntax-operations]))))
    (is (= 3 (count (get-in r [:allocation-reconciliation :hygiene-modes]))))
    (is (= 7 (count (get-in r [:allocation-reconciliation :observable-phases]))))
    (is (= 37 (get-in r [:allocation-reconciliation :diagnostics :source])))
    (is (= 34 (count (get-in r [:allocation-reconciliation :diagnostics :active]))))
    (is (= #{"L4-EXPANSION-DEPTH" "C4-DEPTH" "C4-SIZE"} (set (get-in r [:allocation-reconciliation :diagnostics :retired]))))
    (is (= 9 (count (:accounting-policy-dispositions r))))
    (doseq [row (:accounting-policy-dispositions r)]
      (is (= :preserved (:source-bytes row)))
      (is (= :not-required (:replacement-proof-admission row)))
      (is (= "specs/accounting-removal/spec.md" (:cleanup-owner row))))
    (is (= 4 (count docs)))
    (doseq [[_ {:keys [path source-commit sha256]}] docs]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= 9 (count (:task-sources r))))
    (is (= 16 (count (:plan-sources r))))
    (is (= 5 (count (:specification-sources r))))
    (doseq [{:keys [path before after]} (concat (:task-sources r) (:plan-sources r) (:specification-sources r))]
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
      (is (= (:sha256 after) (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path)))))
    (is (= 10 (count (get-in r [:implementation-source-inspection :files]))))
    (doseq [{:keys [path source-commit sha256]} (get-in r [:implementation-source-inspection :files])]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :static-selected-source-only-no-execution (get-in r [:implementation-source-inspection :scope])))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= :existing-1076-joins-102-routes-62-current-parents-40-future-records-retained-complete-macro-selected-result-staging-pending (:dependency-review r)))
    (is (= (:macro-cache-refinement old) (:macro-cache-refinement bridge)))
    (is (= (get-in (sh-crosswalk/normalize-source-payloads {:r r}) [:r]) (get-in view [:implementation-readiness-source-version :macro-refinement])))))

(deftest macro-dependencies-preserve-real-phase-tool-cache-and-governance-products
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:syntax-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (if checkpoint (memoize (fn [p] (sh-crosswalk/git-source (:commit checkpoint) p))) slurp)
        r (:macro-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        old-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string old-projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        tasks (into {} (for [rows (vals (:task-authorities view)) row rows] [(:id row) row]))
        before (into {} (for [rows (vals (:task-authorities prior)) row rows] [(:id row) row]))
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers r))
        review (:dependency-review result) routes (:routes review)
        consumers (set (:consumer-task-ids r)) new-consumers (set (:new-consumer-task-ids r))
        selected (filterv #(consumers (:consumer %)) routes)
        dep (:dependency-source r) text (read-text (:path dep))
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-map (into {} (map (juxt :consumer identity) old-routes)) current-map (into {} (map (juxt :consumer identity) routes))
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        allowed #{:input-fingerprint :macro-dependency-refinement :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes}]
    (is (= "de2fceb2816fd1ec45c0b91f1cb79affb5f027db" (:commit previous)))
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
    (is (= (:source-input-commit old) (:source-input-commit bridge)))
    (is (= (:task-comparisons old) (:task-comparisons bridge)))
    (is (= (:macro-refinement old) (:macro-refinement bridge)))
    (is (= 149 (count tasks) (count before)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 58 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 81 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= 32 (count (:source-files bridge))))
    (is (= (set (keys tasks)) (set (keys before))))
    (doseq [[id row] tasks]
      (is (= (:checked row) (:checked (before id))))
      (is (= (:source-commit row) (:source-commit (before id))))
      (is (= (:file-sha256 row) (:file-sha256 (before id))))
      (is (= (get-in row [:source :sha256]) (get-in (before id) [:source :sha256]))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= [] (:changed-task-ids r) (:new-task-identities r)))
    (is (= 47 (count consumers) (count selected) (count (:routes r))))
    (is (= 11 (count new-consumers)))
    (is (= 36 (count (:predecessor-selected-routes r))))
    (is (= 562 (:preserved-prior-input-joins r) (reduce + (map (comp count :composition-results) (:predecessor-selected-routes r)))))
    (is (= 817 (:selected-consumer-result-joins r) (reduce + (map (comp count :composition-results) selected))))
    (is (= 255 (:new-selected-producer-result-joins r) (reduce + (map count (vals (:added-composition-inputs r))))))
    (is (= 1331 (:all-selected-result-joins r) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 113 (count routes)))
    (is (= 73 (count (:declared-parent-consumers r)) (count (:required-parent-consumers r)) (count (:declared-parent-consumers review))))
    (is (= 40 (count (:declared-future-consumers r))))
    (is (= 66 (:preserved-consumer-route-count r)))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) routes)))))
    (is (= (set (map :consumer (:predecessor-selected-routes r))) (set (filter old-map consumers))))
    (is (= (set (remove old-map consumers)) new-consumers))
    (is (= 16 (count (:macro-source-consumer-task-ids r))))
    (is (every? consumers (:macro-source-consumer-task-ids r)))
    (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
    (doseq [row selected :let [id (:consumer row) prior-row (old-map id)]]
      (is (= (meaning row) (meaning (first (filter #(= id (:consumer %)) (:routes r))))))
      (is (empty? (:start-results row)))
      (when prior-row
        (is (every? (set (:composition-results row)) (:composition-results prior-row)))
        (is (str/starts-with? (:selected-output-contract row) (:selected-output-contract prior-row))))
      (let [pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote id) "` \\|[^\\n]*\\n"))
            missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                      #(if (= % (:path dep)) (str/replace text pattern "") (read-text %)) (:required-parent-consumers r))]
        (is (= 1 (count (re-seq pattern text))))
        (is (= [id] (get-in missing [:dependency-review :unresolved-consumers]))))
      (doseq [input (:composition-results row)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" input) :actual-selected-producer-result]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (= :unassessed (get-in result [:nodes (str "result:" id ":" input) :state])))))
    (let [current-cycles (:coarse-producer-cycles r) prior-cycles (:predecessor-coarse-cycles r)
          main (set (first current-cycles)) release (set (second current-cycles))]
      (is (= [71 9] (mapv count current-cycles) (mapv count (:declared-output-cycles review))))
      (is (= [60 8] (mapv count prior-cycles)))
      (is (= (get-in old [:compile-time-dependency-refinement :coarse-producer-cycles]) prior-cycles))
      (is (= new-consumers (set (:new-main-cycle-members r))))
      (is (= main (into (set (first prior-cycles)) new-consumers)))
      (is (= ["evolution/change-review"] (:new-release-cycle-members r)))
      (is (= release (conj (set (second prior-cycles)) "evolution/change-review"))))
    (is (= 115 (count (:staged-result-subjects r)) (count (set (map :result (:staged-result-subjects r))))))
    (doseq [stage (:staged-result-subjects r)]
      (is (= (:result stage) (str "result:" (:consumer stage) ":" (:producer stage))))
      (is (some #{(:producer stage)} (:composition-results (current-map (:consumer stage)))))
      (is (= (if (consumers (:consumer stage)) :selected-amended-route :preserved-unchanged-route-context) (:route-scope stage)))
      (is (keyword? (:role stage))))
    (doseq [row (:preserved-stage-context-routes r)]
      (is (= (meaning row) (meaning (old-map (:consumer row))) (meaning (current-map (:consumer row))))))
    (let [formatter (current-map "tools/finite-service-outcomes-and-remaining-work/format-formatter-maintainer")
          inputs (set (:composition-results formatter))
          expansions #{"expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
                       "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
                       "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
                       "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
                       "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"}]
      (is (not-any? inputs expansions))
      (is (str/includes? (:selected-output-contract formatter) "Formatting does not execute or require macro expansion")))
    (is (= :actual-unexpanded-Reader-or-syntax-without-macro-execution (:formatter-boundary r)))
    (is (= :permitted-L16-engine-machinery-and-shared-Build-completeness-current-policy-integration (:cache-ownership r)))
    (is (= :structural-alpha-equivalence-owner-seed-fixtures-hermetic-typed-Core-bytes-and-independent-claimed-stage-comparison-are-distinct (:equivalence-boundary r)))
    (is (= :raw-candidate-comparison-before-later-stabilization-actual-applicable-GOV-state-before-affected-action-or-exposure-stable-support-claim-after-real-decision (:governance-feedback r)))
    (is (= :declared-interfaces-no-whole-component-tool-suite-registration-migration-signing-publication-start-gate (:ordinary-construction r)))
    (is (= :all-finite-subjects-frozen-no-renewal-consumption-or-adoption (:review-conservation r)))
    (is (= 19 (count (:plan-sources r))))
    (doseq [record (concat [dep (:dependency-tool-source r)] (:plan-sources r))
            :let [p (:path record) after (:after record) before (:before record) body (read-text p)]]
      (is (= (:sha256 after) (sh-inventory/sha256 body)))
      (is (= body (sh-crosswalk/git-source (:commit after) p)))
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p)))))
    (is (= 21 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :macro-dependency-refinement])))))

(deftest complete-syntax-source-preserves-real-lifecycle-and-historical-subjects
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:syntax-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (if checkpoint (memoize (fn [p] (sh-crosswalk/git-source (:commit checkpoint) p))) slurp)
        r (:syntax-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        rows (fn [v] (into {} (for [records (vals (:task-authorities v)) row records] [(:id row) row])))
        tasks (rows view) before (rows prior) changed (set (:changed-task-ids r))
        criteria (:criteria r) valid-keys (set (map :key criteria))
        task-text (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        span-text (fn [text span] (String. (.getBytes text "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        document (first (:canonical-sources r)) document-text (read-text (:path document))
        facets (:source-facets r) cases (:occurrence-cases r) sections (:section-conjunctions r) owners (:owner-joins r)
        allowed [:input-fingerprint :source-input-commit :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes :task-comparisons :qualification :syntax-refinement]]
    (is (= "580798f566ac54767efdd49044b90a889821d34e" (:commit previous)))
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
    (is (= (:macro-refinement old) (:macro-refinement bridge)))
    (is (= (:macro-dependency-refinement old) (:macro-dependency-refinement bridge)))
    (is (= 149 (count tasks) (count before)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 32 (count (:source-files bridge))))
    (is (= 55 (get-in bridge [:task-comparisons :unchanged-body-count])))
    (is (= 84 (count (get-in bridge [:task-comparisons :changed-bodies]))))
    (is (= (set (keys tasks)) (set (keys before))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= [] (:new-task-identities r)))
    (is (= 5 (count changed)))
    (is (= 3 (count (:newly-amended-original-task-ids r))))
    (doseq [[id row] tasks]
      (is (= (:checked row) (:checked (before id))))
      (if (changed id)
        (is (not= (task-text view row) (task-text prior (before id))))
        (is (= (task-text view row) (task-text prior (before id))))))
    (let [p "specs/syntax-workflow/tasks.md" current-text (read-text p)
          restored (reduce (fn [text [_ marker]]
                            (let [pattern (re-pattern (str "(?s)\n\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n"))]
                              (is (= 1 (count (re-seq pattern text))))
                              (str/replace text pattern ""))) current-text (:markers r))]
      (is (= (sh-crosswalk/git-source (:commit previous) p) restored)))
    (is (= 6 (count criteria) (count valid-keys)))
    (doseq [{:keys [key task-id current-witness status]} criteria
            :let [text (read-text (:path current-witness)) raw (span-text text (:source-span current-witness)) row (tasks task-id)]]
      (is (some? row))
      (is (= (:source-commit r) (:source-commit current-witness)))
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (is (str/includes? (task-text view row) raw))
      (is (nil? (acceptance-dependencies/current-task-body! (:source row) (:source-snapshots view) read-text)))
      (is (thrown? Exception (acceptance-dependencies/current-task-body! (:source row) (:source-snapshots view)
                              #(if (= % (:path current-witness)) (str/replace text raw "") (read-text %)))))
      (is (= (if (= key :pipeline) :existing-exact-criterion-no-task-amendment :author-proposed-whole-source-criterion-no-independent-acceptance) status)))
    (is (= 128 (count facets) (count (set (map :id facets)))))
    (is (= 52 (count cases)))
    (is (= 14 (count sections)))
    (let [ref (:source-census r)
          text (sh-crosswalk/git-source (:commit ref) (:path ref))
          census (edn/read-string text)
          original-facets (filter #(= "C3" (:document-id %)) (:facets census))
          original-cases (filter #(= "C3" (:document-id %)) (:occurrences census))]
      (is (= (:sha256 ref) (sh-inventory/sha256 text)))
      (is (= (set (map :id original-facets)) (set (map :id facets))))
      (is (= (set (map :id original-cases)) (set (keys cases))))
      (doseq [facet facets :let [original (first (filter #(= (:id facet) (:id %)) original-facets))]]
        (is (= (:owner-requirement-ids original) (:owner-requirement-ids facet)))
        (is (= [(:start-byte original) (:end-byte original) (:raw-sha256 original)]
               [(get-in facet [:source-span :start-byte]) (get-in facet [:source-span :end-byte]) (get-in facet [:source-span :sha256])]))))
    (doseq [facet facets :let [raw (span-text document-text (:source-span facet)) c (cases (:source-occurrence-id facet))]]
      (is (= (get-in facet [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (is (= (:role c) (:role facet)))
      (is (= (:criterion-keys c) (:criterion-keys facet)))
      (is (= :proposal-pending-independent-review (:current-entailment facet))))
    (doseq [[_ c] cases]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 (span-text document-text (:source-span c)))))
      (is (every? valid-keys (:criterion-keys c)))
      (is (if (= :complete-source-predicate-conjunction (:role c)) (seq (:criterion-keys c)) (empty? (:criterion-keys c)))))
    (doseq [[section row] sections id (:occurrence-ids row)]
      (is (= section (:section-key (cases id))))
      (is (= (:criterion-keys row) (:criterion-keys (cases id)))))
    (is (= #{"compiler/c1/preserve-evidence" "compiler/c3/inspectable-hygiene" "compiler/c3/origin-preservation" "compiler/c3/syntax-construction" "compiler/c3/syntax-roundtrip-gate"} (set (keys owners))))
    (is (= "pipeline" (get-in owners ["compiler/c1/preserve-evidence" :task-id])))
    (is (= (task-text prior (before "pipeline")) (task-text view (tasks "pipeline"))))
    (doseq [[id owner] owners]
      (is (= id (:id owner)))
      (is (tasks (:task-id owner)))
      (is (seq (:original-obligation owner)))
      (is (every? valid-keys (:criterion-keys owner)))
      (is (every? valid-keys (:supporting-producer-consumer-criteria owner)))
      (is (= :unassessed (:whole-attached-owner-predicate owner)))
      (is (= :none (:implementation-credit owner))))
    (is (= [15 10 8 9] (mapv #(count (get-in r [:allocation-reconciliation %])) [:form-families :facts :artifacts :diagnostics])))
    (is (= :stop-expansion-and-analysis (get-in r [:allocation-reconciliation :verifier-failure])))
    (is (= :none (get-in r [:historical-reviews :new-review]) (get-in r [:historical-reviews :renewal]) (get-in r [:historical-reviews :new-consumption])))
    (doseq [record (concat (:task-sources r) (:plan-sources r) (:specification-sources r))
            :let [p (:path record) before (:before record) after (:after record)]]
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p))))
      (is (= (:sha256 after) (sh-inventory/sha256 (read-text p)) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) p)))))
    (is (= [1 7 1] (mapv #(count (% r)) [:task-sources :plan-sources :specification-sources])))
    (is (= (:sha256 document) (sh-inventory/sha256 document-text) (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit document) (:path document)))))
    (is (= 7 (count (get-in r [:implementation-source-inspection :files]))))
    (doseq [{:keys [path source-commit sha256]} (get-in r [:implementation-source-inspection :files])]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :selected-static-definitions-only-no-execution (get-in r [:implementation-source-inspection :scope])))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :syntax-refinement])))))

(deftest syntax-dependencies-preserve-actual-lifecycle-verification-and-source-only-subjects
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:resolution-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (if checkpoint (memoize (fn [p] (sh-crosswalk/git-source (:commit checkpoint) p))) slurp)
        r (:syntax-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        rows (fn [v] (into {} (for [records (vals (:task-authorities v)) row records] [(:id row) row])))
        tasks (rows view) before (rows prior)
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers r))
        review (:dependency-review result) routes (:routes review)
        dep (:dependency-source r) text (read-text (:path dep))
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-map (into {} (map (juxt :consumer identity) old-routes)) current-map (into {} (map (juxt :consumer identity) routes))
        consumers (set (:consumer-task-ids r)) new-consumers (set (:new-consumer-task-ids r))
        selected (filterv #(consumers (:consumer %)) routes)
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        raw (fn [v row]
              (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        changed-task-ids (set (for [[id row] before
                                    :when (not= (raw prior row) (raw view (tasks id)))] id))]
    (is (= "298995887884ab6b4782198250a79b77afaecd2e" (:commit previous)))
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    ;; The immutable route bridge retains its historical source frame.  The
    ;; current-main successor is a distinct reconciled frame, so validate each
    ;; census independently rather than equating old and current metadata.
    (is (= "89959d87edb55f47a66a1f15d530b0e40c27b44c" (:source-input-commit bridge)))
    (is (and (string? (:source-input-commit sh-crosswalk/readiness-source-version))
             (= 40 (count (:source-input-commit sh-crosswalk/readiness-source-version)))))
    (is (not= (:source-input-commit bridge)
              (:source-input-commit sh-crosswalk/readiness-source-version)))
    (is (= :selected-complete-C3-producer-consumer-dependency-proposal (:kind r)))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= 149 (count tasks) (count before)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 32 (count (:source-files bridge))))
    (is (= [36 103] [(get-in sh-crosswalk/readiness-source-version [:task-comparisons :unchanged-body-count])
                      (count (get-in sh-crosswalk/readiness-source-version [:task-comparisons :changed-bodies]))]))
    (is (= [55 84] [(get-in bridge [:task-comparisons :unchanged-body-count])
                     (count (get-in bridge [:task-comparisons :changed-bodies]))]))
    (is (= (set (keys tasks)) (set (keys before))))
    ;; Both immutable route projections contain the same task bodies; the
    ;; current-main source frame is checked separately above.  Do not infer a
    ;; historical body amendment from a source-frame commit transition alone.
    (is (= 0 (count changed-task-ids)))
    (doseq [[id row] tasks]
      (let [prior-row (before id)
            source-text (sh-crosswalk/git-source (:source-commit row) (:path row))
            current-raw (raw view row)
            prior-raw (raw prior prior-row)]
        (is (= (:checked row) (:checked prior-row)))
        (is (= (:file-sha256 row) (sh-inventory/sha256 source-text)))
        (is (= (get-in row [:source :sha256])
               (sh-inventory/sha256 current-raw)))
        (if (changed-task-ids id)
          (is (not= prior-raw current-raw))
          (do
            ;; A changed task file may legitimately move to the current source
            ;; commit while this task body remains byte-identical.  The body
            ;; and its digest are the semantic conservation boundary; the
            ;; whole-file commit/sha are not row identity.
            (is (= prior-raw current-raw))
            (is (= (get-in row [:source :sha256]) (get-in prior-row [:source :sha256])))))))
    (let [future-rows (fn [v] (into {} (for [[_ records] (:future-task-authorities v) row records] [(:id row) row])))
          future-before (future-rows prior)
          future-after (future-rows view)]
      (is (= 40 (count future-after)))
      (is (= (set (keys future-before)) (set (keys future-after))))
      (doseq [[id row] future-before]
        (is (= (:checked row) (:checked (future-after id))))))
    (is (= [] (:changed-task-ids r) (:new-task-identities r)))
    (is (= 35 (count consumers) (count selected) (count (:routes r))))
    (is (= 2 (count new-consumers)))
    (is (= #{"syntax/current-evidence-and-remaining-work/syntax3-invalidate-facts" "syntax/current-evidence-and-remaining-work/syntax4-serialize-and-verify"} new-consumers))
    (is (= 33 (count (:predecessor-selected-routes r))))
    (is (= 536 (:preserved-prior-input-joins r) (reduce + (map (comp count :composition-results) (:predecessor-selected-routes r)))))
    (is (= 610 (:selected-consumer-result-joins r) (reduce + (map (comp count :composition-results) selected))))
    (is (= 74 (:new-selected-producer-result-joins r) (reduce + (map count (vals (:added-composition-inputs r))))))
    (is (= 1405 (:all-selected-result-joins r) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 115 (count routes)))
    (is (= 75 (count (:required-parent-consumers r)) (count (:declared-parent-consumers review))))
    (is (= 40 (count (:declared-future-consumers r))))
    (is (= 80 (:preserved-consumer-route-count r)))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) routes)))))
    (is (= 6 (count (:syntax-source-consumer-task-ids r))))
    (is (every? consumers (:syntax-source-consumer-task-ids r)))
    (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
    (doseq [row selected :let [id (:consumer row) prior-row (old-map id)]]
      (is (= (meaning row) (meaning (first (filter #(= id (:consumer %)) (:routes r))))))
      (is (empty? (:start-results row)))
      (when prior-row
        (is (every? (set (:composition-results row)) (:composition-results prior-row)))
        (is (str/starts-with? (:selected-output-contract row) (:selected-output-contract prior-row))))
      (let [pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote id) "` \\|[^\\n]*\\n"))
            missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                      #(if (= % (:path dep)) (str/replace text pattern "") (read-text %)) (:required-parent-consumers r))]
        (is (= 1 (count (re-seq pattern text))))
        (is (= [id] (get-in missing [:dependency-review :unresolved-consumers]))))
      (doseq [input (:composition-results row)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" input) :actual-selected-producer-result]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (= :unassessed (get-in result [:nodes (str "result:" id ":" input) :state])))))
    (let [s3 "syntax/current-evidence-and-remaining-work/syntax3-invalidate-facts"
          s4 "syntax/current-evidence-and-remaining-work/syntax4-serialize-and-verify"
          pipeline (current-map "pipeline") lifecycle (current-map s3)
          formatter (current-map "tools/finite-service-outcomes-and-remaining-work/format-formatter-maintainer")]
      (is (some #{s3} (:composition-results pipeline)))
      (is (some #{s3} (get-in r [:added-composition-inputs "pipeline"])))
      (is (every? (set (:composition-results lifecycle))
                  ["pipeline" "resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions"
                   "types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts"
                   "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"]))
      (is (not-any? #(str/starts-with? % "expand/") (:composition-results formatter)))
      (is (str/includes? (:selected-output-contract formatter) "source-only verification/round-trip operations"))
      (is (str/includes? (:selected-output-contract formatter) "later type/effect/safety analysis"))
      (is (str/includes? (:selected-output-contract formatter) "whole Syntax4 completion"))
      (is (some #{s4} (:composition-results formatter))))
    (is (= [73 9] (mapv count (:coarse-producer-cycles r)) (mapv count (:declared-output-cycles review))))
    (is (= [71 9] (mapv count (:predecessor-coarse-cycles r))))
    (is (= (get-in old [:macro-dependency-refinement :coarse-producer-cycles]) (:predecessor-coarse-cycles r)))
    (is (= new-consumers (set (:new-main-cycle-members r))))
    (is (= (into (set (first (:predecessor-coarse-cycles r))) new-consumers) (set (first (:coarse-producer-cycles r)))))
    (is (= [] (:new-release-cycle-members r)))
    (is (= (second (:predecessor-coarse-cycles r)) (second (:coarse-producer-cycles r))))
    (is (= 91 (count (:staged-result-subjects r)) (count (set (map :result (:staged-result-subjects r))))))
    (doseq [stage (:staged-result-subjects r)]
      (is (= (:result stage) (str "result:" (:consumer stage) ":" (:producer stage))))
      (is (some #{(:producer stage)} (:composition-results (current-map (:consumer stage)))))
      (is (= :selected-amended-route (:route-scope stage)))
      (is (keyword? (:role stage))))
    (is (= :semantic-stages-own-computation-conditions-and-regeneration-Syntax-owns-carrier-lifecycle-Pipeline-consumes-actual-decision (:fact-ownership r)))
    (is (= :empty-pre-analysis-facts-do-not-require-later-binding-type-effect-safety-macro-or-release-products (:source-only-boundary r)))
    (is (= :complete-source-generated-and-shared-origin-subject-not-summary-payload-or-one-physical-encoding (:serializer-boundary r)))
    (is (= :all-finite-subjects-frozen-no-renewal-consumption-or-adoption (:review-conservation r)))
    (is (= 14 (count (:plan-sources r))))
    (doseq [record (concat [dep (:dependency-tool-source r)] (:plan-sources r))
            :let [p (:path record) before (:before record) after (:after record)]]
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p))))
      (is (= (:sha256 after) (sh-inventory/sha256 (read-text p)) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) p)))))
    (is (= 20 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :syntax-dependency-refinement])))))

(deftest complete-resolution-source-conserves-published-owners-and-qualified-profile-predicates
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:resolution-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (if checkpoint (memoize (fn [p] (sh-crosswalk/git-source (:commit checkpoint) p))) slurp)
        r (:resolution-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        rows (fn [v] (into {} (for [records (vals (:task-authorities v)) row records] [(:id row) row])))
        tasks (rows view) before (rows prior) changed (set (:changed-task-ids r))
        criteria (:criteria r) by-key (into {} (map (juxt :key identity) criteria)) valid-keys (set (keys by-key))
        task-text (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        span-text (fn [text span] (String. (.getBytes text "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        current-raw (fn [k] (let [w (:current-witness (by-key k))] (span-text (read-text (:path w)) (:source-span w))))
        document-text (into {} (for [d (:canonical-sources r)] [(:document-id d) (read-text (:path d))]))
        facets (:source-facets r) cases (:occurrence-cases r) sections (:section-conjunctions r) owners (:owner-joins r)
        allowed [:input-fingerprint :source-input-commit :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes :task-comparisons :resolution-refinement]]
    (is (= "313bb6fd12f639d45774b4de39a672d7d060fe1c" (:commit previous)))
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
    (is (= (:syntax-refinement old) (:syntax-refinement bridge)))
    (is (= (:syntax-dependency-refinement old) (:syntax-dependency-refinement bridge)))
    (is (= 149 (count tasks) (count before)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 32 (count (:source-files bridge))))
    (is (= [51 88] [(get-in bridge [:task-comparisons :unchanged-body-count]) (count (get-in bridge [:task-comparisons :changed-bodies]))]))
    (is (= (set (keys tasks)) (set (keys before))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= [] (:new-task-identities r)))
    (is (= 6 (count changed)))
    (is (= 4 (count (:newly-amended-original-task-ids r))))
    (doseq [[id row] tasks]
      (is (= (:checked row) (:checked (before id))))
      (if (changed id)
        (is (not= (task-text view row) (task-text prior (before id))))
        (is (= (task-text view row) (task-text prior (before id))))))
    (doseq [p ["specs/resolve-workflow/tasks.md" "specs/legality-workflow/tasks.md"]
            :let [markers (for [[k marker] (:markers r) :when (= p (get-in by-key [k :current-witness :path]))] marker)
                  restored (reduce (fn [text marker]
                    (let [pattern (re-pattern (str "(?s)\n\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n"))]
                      (is (= 1 (count (re-seq pattern text)))) (str/replace text pattern ""))) (read-text p) markers)]]
      (is (= (sh-crosswalk/git-source (:commit previous) p) restored)))
    (is (= 12 (count criteria) (count valid-keys)))
    (is (= 10 (count (set (map :task-id criteria)))))
    (doseq [{:keys [key task-id current-witness status]} criteria
            :let [text (read-text (:path current-witness)) raw (span-text text (:source-span current-witness)) row (tasks task-id)]]
      (is (some? row))
      (is (= (:source-commit r) (:source-commit current-witness)))
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (is (str/includes? (task-text view row) raw))
      (is (nil? (acceptance-dependencies/current-task-body! (:source row) (:source-snapshots view) read-text)))
      (is (thrown? Exception (acceptance-dependencies/current-task-body! (:source row) (:source-snapshots view)
                              #(if (= % (:path current-witness)) (str/replace text raw "") (read-text %)))))
      (when (#{:authority :effects :invalidation :compatibility} key)
        (is (= :existing-exact-criterion-no-task-amendment status))
        (is (= (task-text prior (before task-id)) (task-text view row)))))
    (let [published (:published-owner-source r)
          text (sh-crosswalk/git-source (:commit published) "specs/resolve-workflow/tasks.md")]
      (is (= "11350110bc0c0456699a31151b321a7ddfaa6d7e" (:commit published)))
      (is (= "8f52b66b3cd469db5c2ad336013958d378a94533" (:tree published)))
      (is (= true (:verified-published-commit published)))
      (is (= :existing-owner-proposal-not-independent-full-source-acceptance (:qualification published)))
      (is (= 6 (count (:criterion-witnesses published))))
      (doseq [{:keys [key task-id sha256 source-span]} (:criterion-witnesses published)]
        (is (= sha256 (sh-inventory/sha256 text)))
        (is (= (:sha256 source-span) (sh-inventory/sha256 (span-text text source-span))))
        (is (= (current-raw key) (span-text text source-span)))
        (is (= task-id (:task-id (by-key key))))))
    (is (= #{:profile-edge :namespace-contract} (set (keys (:profile74-restorations r)))))
    (is (= 1125 (reduce + (for [[_ row] (:profile74-restorations r)]
                          (- (get-in row [:criterion-witness :end-byte]) (get-in row [:criterion-witness :start-byte]))))))
    (doseq [[k record] (:profile74-restorations r)
            :let [w (:criterion-witness record) text (sh-crosswalk/git-source (:source-commit w) (:path w))
                  span {:start-byte (:start-byte w) :end-byte (:end-byte w)} raw (span-text text span)]]
      (is (= "45fc1420cfe4373e5a20b1fcd80d46f87e4edb63" (:source-commit w)))
      (is (= (:file-sha256 w) (sh-inventory/sha256 text)))
      (is (= (:raw-sha256 w) (sh-inventory/sha256 raw)))
      (is (= raw (current-raw k)))
      (is (= "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks" (:task-id (by-key k))))
      (is (seq (:qualified-review-adoption record))))
    (is (str/includes? (current-raw :namespace-contract) "explicit bootstrap-seed exception"))
    (is (= (get-in old [:authority-refinement :historical-reviews :profile74]) (get-in r [:historical-reviews :profile74])))
    (is (= 195 (count facets) (count (set (map :id facets)))))
    (is (= 107 (count cases)))
    (is (= 27 (count sections)))
    (is (= #{"L3" "C5"} (set (map :document-id facets))))
    (doseq [facet facets :let [raw (span-text (document-text (:document-id facet)) (:source-span facet)) c (cases (:source-occurrence-id facet))]]
      (is (= (get-in facet [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (is (= (:document-id facet) (:document-id c)))
      (is (= (:role c) (:role facet)))
      (is (= (:criterion-keys c) (:criterion-keys facet)))
      (is (every? #(contains? owners %) (:owner-requirement-ids facet)))
      (is (= :proposal-pending-independent-review (:current-entailment facet))))
    (doseq [[_ c] cases]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 (span-text (document-text (:document-id c)) (:source-span c)))))
      (is (every? valid-keys (:criterion-keys c)))
      (is (if (= :complete-source-predicate-conjunction (:role c)) (seq (:criterion-keys c)) (empty? (:criterion-keys c)))))
    (doseq [[section row] sections id (:occurrence-ids row)]
      (is (= section (:section-key (cases id))))
      (is (= (:criterion-keys row) (:criterion-keys (cases id)))))
    (is (= #{"compiler/c16/dependency-invalidation" "compiler/c5/explicit-imports" "language/effects/check-narrowest-allowance" "language/module/check-import-boundary" "language/module/check-initialization-graph" "language/module/emit-reproducible-interface" "language/module/enforce-export-visibility" "language/module/publish-definition-entries" "language/module/resolve-binding" "language/module/review-compatibility-change" "language/module/validate-namespace-contract"} (set (keys owners))))
    (is (= (:task-id (by-key :graph)) (get-in owners ["language/module/check-import-boundary" :task-id])))
    (is (= (:task-id (by-key :namespace-contract)) (get-in owners ["language/module/validate-namespace-contract" :task-id])))
    (is (= (:task-id (by-key :authority)) (get-in owners ["language/effects/check-narrowest-allowance" :task-id])))
    (doseq [[id owner] owners]
      (is (= id (:id owner)))
      (is (tasks (:task-id owner)))
      (is (seq (:original-obligation owner)))
      (is (every? valid-keys (:criterion-keys owner)))
      (is (every? valid-keys (:supporting-producer-consumer-criteria owner)))
      (is (= :unassessed (:whole-attached-owner-predicate owner)))
      (is (= :none (:implementation-credit owner))))
    (is (= [6 11 11 5 10 9 8 10 9]
           (mapv #(count (get-in r [:allocation-reconciliation %])) [:definition-families :entry-fields :namespace-clauses :cross-profile-boundaries :L3-artifacts :C5-artifacts :L3-diagnostics :C5-diagnostics :invalidation-causes])))
    (is (= :actual-module-artifacts-without-source-reread (get-in r [:allocation-reconciliation :package-graph])))
    (is (= :not-executable-symbol-resolution (get-in r [:allocation-reconciliation :quoted-data])))
    (is (= :none (get-in r [:historical-reviews :new-review]) (get-in r [:historical-reviews :renewal]) (get-in r [:historical-reviews :new-consumption])))
    (doseq [record (concat (:task-sources r) (:plan-sources r) (:specification-sources r))
            :let [p (:path record) before (:before record) after (:after record)]]
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p))))
      (is (= (:sha256 after) (sh-inventory/sha256 (read-text p)) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) p)))))
    (is (= [2 14 1] (mapv #(count (% r)) [:task-sources :plan-sources :specification-sources])))
    (doseq [d (:canonical-sources r)]
      (is (= (:sha256 d) (sh-inventory/sha256 (document-text (:document-id d))) (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit d) (:path d))))))
    (is (= 10 (count (get-in r [:implementation-source-inspection :files]))))
    (doseq [{:keys [path source-commit sha256]} (get-in r [:implementation-source-inspection :files])]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (doseq [file ["spec.md" "plan.md" "tasks.md" "evidence.md"]
            :let [p (str "specs/resolve-owner-source-core-verification/" file)]]
      (is (= (sh-crosswalk/git-source (:commit previous) p) (read-text p))))
    (is (= :selected-static-definitions-only-no-execution (get-in r [:implementation-source-inspection :scope])))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :resolution-refinement])))))

(deftest resolution-dependencies-preserve-early-bindings-later-checked-modules-and-real-consumers
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:core-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (if checkpoint (memoize (fn [p] (sh-crosswalk/git-source (:commit checkpoint) p))) slurp)
        r (:resolution-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        rows (fn [v] (into {} (for [records (vals (:task-authorities v)) row records] [(:id row) row])))
        tasks (rows view) before (rows prior)
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers r))
        review (:dependency-review result) routes (:routes review)
        dep (:dependency-source r) text (read-text (:path dep))
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        old-map (into {} (map (juxt :consumer identity) old-routes)) current-map (into {} (map (juxt :consumer identity) routes))
        consumers (set (:consumer-task-ids r)) new-consumers (set (:new-consumer-task-ids r))
        selected (filterv #(consumers (:consumer %)) routes)
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        allowed [:input-fingerprint :resolution-dependency-refinement :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes]]
    (is (= "25a96a0c03536270001449c00676e644ea353521" (:commit previous)))
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
    (is (= (:source-input-commit old) (:source-input-commit bridge)))
    (is (= (:task-comparisons old) (:task-comparisons bridge)))
    (is (= (:resolution-refinement old) (:resolution-refinement bridge)))
    (is (= (:syntax-dependency-refinement old) (:syntax-dependency-refinement bridge)))
    (is (= 149 (count tasks) (count before)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 32 (count (:source-files bridge))))
    (is (= [51 88] [(get-in bridge [:task-comparisons :unchanged-body-count]) (count (get-in bridge [:task-comparisons :changed-bodies]))]))
    (is (= (set (keys tasks)) (set (keys before))))
    (doseq [[id row] tasks]
      (is (= (:checked row) (:checked (before id))))
      (is (= (:source-commit row) (:source-commit (before id))))
      (is (= (:file-sha256 row) (:file-sha256 (before id))))
      (is (= (get-in row [:source :sha256]) (get-in (before id) [:source :sha256]))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= [] (:changed-task-ids r) (:new-task-identities r)))
    (is (= 49 (count consumers) (count selected) (count (:routes r))))
    (is (= 9 (count new-consumers)))
    (is (= #{"types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts" "emit/current-evidence-and-remaining-work/emit2-emit-real-outputs" "core/current-evidence-and-remaining-work/core2-preserve-binding-structure" "resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries" "resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality" "tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer" "types/current-evidence-and-remaining-work/types1-solve-core-constraints" "types/current-evidence-and-remaining-work/types2-check-declared-abstractions" "resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions"} new-consumers))
    (is (= 40 (count (:predecessor-selected-routes r))))
    (is (= 641 (:preserved-prior-input-joins r) (reduce + (map (comp count :composition-results) (:predecessor-selected-routes r)))))
    (is (= 813 (:selected-consumer-result-joins r) (reduce + (map (comp count :composition-results) selected))))
    (is (= 172 (:new-selected-producer-result-joins r) (reduce + (map count (vals (:added-composition-inputs r))))))
    (is (= 1577 (:all-selected-result-joins r) (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= 124 (count routes)))
    (is (= 84 (count (:required-parent-consumers r)) (count (:declared-parent-consumers review))))
    (is (= 40 (count (:declared-future-consumers r))))
    (is (= 75 (:preserved-consumer-route-count r)))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) routes)))))
    (is (= 10 (count (:resolution-source-consumer-task-ids r))))
    (is (every? consumers (:resolution-source-consumer-task-ids r)))
    (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
    (doseq [row selected :let [id (:consumer row) prior-row (old-map id)]]
      (is (= (meaning row) (meaning (first (filter #(= id (:consumer %)) (:routes r))))))
      (is (empty? (:start-results row)))
      (when prior-row
        (is (every? (set (:composition-results row)) (:composition-results prior-row)))
        (is (str/starts-with? (:selected-output-contract row) (:selected-output-contract prior-row))))
      (let [pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote id) "` \\|[^\\n]*\\n"))
            missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                      #(if (= % (:path dep)) (str/replace text pattern "") (read-text %)) (:required-parent-consumers r))]
        (is (= 1 (count (re-seq pattern text))))
        (is (= [id] (get-in missing [:dependency-review :unresolved-consumers]))))
      (doseq [input (:composition-results row)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" input) :actual-selected-producer-result]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (= :unassessed (get-in result [:nodes (str "result:" id ":" input) :state])))))
    (let [n {:compiler-suite "release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures", :conformance "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface", :resolve2 "resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries", :emit2 "emit/current-evidence-and-remaining-work/emit2-emit-real-outputs", :lower5 "lower/current-evidence-and-remaining-work/lower5-integrate-target-emission", :language-suite "release/remaining-tasks-and-evidence/execute-language-conformance-fixtures", :effect3 "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering", :resolve4 "resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes", :resolve3 "resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality", :types2 "types/current-evidence-and-remaining-work/types2-check-declared-abstractions", :core2 "core/current-evidence-and-remaining-work/core2-preserve-binding-structure", :tools-docs "tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer", :profile-suite "release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures", :resolve1 "resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions", :backend "backend-interface", :grant "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime", :profile "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks", :types1 "types/current-evidence-and-remaining-work/types1-solve-core-constraints", :types4 "types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts"}]
      (is (= 12 (get-in r [:resolution-source-binding :criteria])))
      (is (= (get-in bridge [:resolution-refinement :source-commit]) (get-in r [:resolution-source-binding :source-commit])))
      (is (every? (set (:composition-results (current-map (n :core2)))) [(n :resolve1) (n :resolve2) (n :resolve4)]))
      (is (every? (set (:composition-results (current-map (n :types4)))) [(n :types1) (n :types2) (n :resolve1) (n :resolve4)]))
      (is (every? (set (:composition-results (current-map (n :resolve3)))) [(n :effect3) (n :grant) (n :profile)]))
      (is (every? (set (:composition-results (current-map (n :emit2)))) [(n :resolve2) (n :resolve4) (n :lower5) (n :backend)]))
      (is (every? (set (:composition-results (current-map (n :tools-docs)))) [(n :resolve2) (n :resolve3) (n :resolve4)]))
      (is (str/includes? (:selected-output-contract (current-map (n :resolve1))) "Quoted runtime data is not executable lookup"))
      (is (str/includes? (:selected-output-contract (current-map (n :resolve3))) "later results are not prerequisites"))
      (is (str/includes? (:selected-output-contract (current-map (n :resolve4))) "without rereading source"))
      (is (str/includes? (:selected-output-contract (current-map (n :emit2))) "PKG1003"))
      (is (not-any? (set [(n :language-suite) (n :compiler-suite) (n :profile-suite)]) (get-in r [:added-composition-inputs (n :conformance)])))
      (is (str/includes? (:selected-output-contract (current-map (n :conformance))) "These owner cases precede the independent")))
    (is (= [82 9] (mapv count (:coarse-producer-cycles r)) (mapv count (:declared-output-cycles review))))
    (is (= [73 9] (mapv count (:predecessor-coarse-cycles r))))
    (is (= (get-in old [:syntax-dependency-refinement :coarse-producer-cycles]) (:predecessor-coarse-cycles r)))
    (is (= new-consumers (set (:new-main-cycle-members r))))
    (is (= (into (set (first (:predecessor-coarse-cycles r))) new-consumers) (set (first (:coarse-producer-cycles r)))))
    (is (= [] (:new-release-cycle-members r)))
    (is (= (second (:predecessor-coarse-cycles r)) (second (:coarse-producer-cycles r))))
    (is (= 196 (count (:staged-result-subjects r)) (count (set (map :result (:staged-result-subjects r))))))
    (doseq [stage (:staged-result-subjects r)]
      (is (= (:result stage) (str "result:" (:consumer stage) ":" (:producer stage))))
      (is (some #{(:producer stage)} (:composition-results (current-map (:consumer stage)))))
      (is (= :selected-amended-route (:route-scope stage)))
      (is (keyword? (:role stage))))
    (is (= :actual-early-requested-graph-and-available-declaration-index-before-inference-later-checked-module-before-claimed-downstream-use (:construction-order r)))
    (is (= :actual-C5-bindings-before-C6-lowering-later-analysis-facts-empty-until-owner-computation-D1-question-preserved (:semantic-Core-boundary r)))
    (is (= :actual-owner-positive-negative-cases-before-independent-language-compiler-profile-harness-no-own-future-suite-gate (:conformance-order r)))
    (is (= :finite-existing-PKG1-binding-artifact-kind-table-before-actual-Emit2-owning-PKG1003-no-linker-fallback (:entrypoint-boundary r)))
    (is (= :all-finite-subjects-frozen-no-renewal-consumption-or-adoption (:review-conservation r)))
    (is (= 15 (count (:plan-sources r))))
    (doseq [record (concat [dep (:dependency-tool-source r)] (:plan-sources r))
            :let [p (:path record) before (:before record) after (:after record)]]
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p))))
      (is (= (:sha256 after) (sh-inventory/sha256 (read-text p)) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) p)))))
    (is (= 28 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :resolution-dependency-refinement])))))

(deftest complete-core-source-preserves-semantics-verifiers-and-qualified-history
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:core-dependency-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (if checkpoint (memoize (fn [p] (sh-crosswalk/git-source (:commit checkpoint) p))) slurp)
        r (:core-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        rows (fn [v] (into {} (for [records (vals (:task-authorities v)) row records] [(:id row) row])))
        tasks (rows view) before (rows prior) changed (set (:changed-task-ids r))
        criteria (:criteria r) by-key (into {} (map (juxt :key identity) criteria)) criterion-keys (set (keys by-key))
        task-text (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
        span-text (fn [text span] (String. (.getBytes text "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        current-raw (fn [k] (let [w (:current-witness (by-key k))] (span-text (read-text (:path w)) (:source-span w))))
        document-text (into {} (for [d (:canonical-sources r)] [(:document-id d) (read-text (:path d))]))
        facets (:source-facets r) cases (:occurrence-cases r) sections (:section-conjunctions r) owners (:owner-joins r)
        allowed [:input-fingerprint :source-input-commit :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes :task-comparisons :core-refinement]]
    (is (= "f6a56aa387adebc511ff0f414bacbb21edb1f530" (:commit previous)))
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
    (is (= (:resolution-refinement old) (:resolution-refinement bridge)))
    (is (= (:resolution-dependency-refinement old) (:resolution-dependency-refinement bridge)))
    (is (= 149 (count tasks) (count before)))
    (is (= 25 (count (:task-authorities view))))
    (is (= 32 (count (:source-files bridge))))
    (is (= [49 90] [(get-in bridge [:task-comparisons :unchanged-body-count]) (count (get-in bridge [:task-comparisons :changed-bodies]))]))
    (is (= (set (keys tasks)) (set (keys before))))
    (is (= (:future-task-authorities prior) (:future-task-authorities view)))
    (is (= [] (:new-task-identities r)))
    (is (= 5 (count changed)))
    (is (= 2 (count (:newly-amended-original-task-ids r))))
    (doseq [[id row] tasks]
      (is (= (:checked row) (:checked (before id))))
      (if (changed id) (is (not= (task-text view row) (task-text prior (before id))))
        (is (= (task-text view row) (task-text prior (before id))))))
    (let [p "specs/core-workflow/tasks.md"
          restored (reduce (fn [text [_ marker]]
                     (let [pattern (re-pattern (str "(?s)\n\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n"))]
                       (is (= 1 (count (re-seq pattern text)))) (str/replace text pattern ""))) (read-text p) (:markers r))]
      (is (= (sh-crosswalk/git-source (:commit previous) p) restored)))
    (is (= 15 (count criteria) (count criterion-keys)))
    (is (= 9 (count (set (map :task-id criteria)))))
    (doseq [{:keys [key task-id current-witness status]} criteria
            :let [text (read-text (:path current-witness)) raw (span-text text (:source-span current-witness)) row (tasks task-id)]]
      (is (some? row))
      (is (= (:source-commit r) (:source-commit current-witness)))
      (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
      (is (= (get-in current-witness [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (is (str/includes? (task-text view row) raw))
      (is (nil? (acceptance-dependencies/current-task-body! (:source row) (:source-snapshots view) read-text)))
      (is (thrown? Exception (acceptance-dependencies/current-task-body! (:source row) (:source-snapshots view)
                              #(if (= % (:path current-witness)) (str/replace text raw "") (read-text %)))))
      (when (#{:semantic-change :effects :authority :pipeline} key)
        (is (= :existing-exact-criterion-no-task-amendment status))
        (is (= (task-text prior (before task-id)) (task-text view row))))
      (when (= :profile key)
        (is (= :existing-exact-predicate-in-amended-task status))
        (is (str/includes? (task-text prior (before task-id)) raw))))
    (let [published (:published-owner-source r) text (sh-crosswalk/git-source (:commit published) "specs/core-workflow/tasks.md")]
      (is (= "a330051b3c0062cff2fb91df5bd6ff6252676c08" (:commit published)))
      (is (= "c07e591386db870984845f2ecf7a5b89ae2ff512" (:tree published)))
      (is (= true (:verified-published-commit published)))
      (is (= :existing-C6-owner-criteria-not-new-L2-or-current-composition-acceptance (:qualification published)))
      (is (= 5 (count (:criterion-witnesses published))))
      (doseq [{:keys [key task-id sha256 source-span]} (:criterion-witnesses published)]
        (is (= sha256 (sh-inventory/sha256 text)))
        (is (= (:sha256 source-span) (sh-inventory/sha256 (span-text text source-span))))
        (is (= (current-raw key) (span-text text source-span)))
        (is (= task-id (:task-id (by-key key))))))
    (is (= #{:profile :semantic-change} (set (keys (:retained-profile74-predicates r)))))
    (is (= 1637 (reduce + (for [[_ row] (:retained-profile74-predicates r)]
                          (- (get-in row [:criterion-witness :end-byte]) (get-in row [:criterion-witness :start-byte]))))))
    (doseq [[k record] (:retained-profile74-predicates r)
            :let [w (:criterion-witness record) text (sh-crosswalk/git-source (:source-commit w) (:path w))
                  span {:start-byte (:start-byte w) :end-byte (:end-byte w)} raw (span-text text span)]]
      (is (= "45fc1420cfe4373e5a20b1fcd80d46f87e4edb63" (:source-commit w)))
      (is (= (:file-sha256 w) (sh-inventory/sha256 text)))
      (is (= (:raw-sha256 w) (sh-inventory/sha256 raw)))
      (is (= raw (current-raw k)))
      (is (seq (:qualified-review-adoption record))))
    (is (= (get-in old [:authority-refinement :historical-reviews :profile74]) (get-in r [:historical-reviews :profile74])))
    (let [review (get-in r [:historical-reviews :C6])]
      (is (= "753c9e39798bd5df14a1c6e0d39db986aee3c5c710a086375d85438d5d2f8d55" (:sha256 review)))
      (is (= "8834ac0e33f37faebc9bcc983e247d4dc9491501" (:artifact-source-commit review)))
      (is (= "febc1512d38e3365846710083e41b29ae997a84f" (:artifact-tree review)))
      (is (= :verified-published-existing-receipt-no-new-review (:artifact-publication review)))
      (is (= (:sha256 review) (sh-inventory/sha256 (sh-crosswalk/git-source (:artifact-source-commit review) (:path review)))))
      (is (= "103fba824db72589e6afbfc7a5d3573ce3af2d27" (:candidate review)))
      (is (= 103 (get-in review [:scope :source-facets])))
      (is (= 5 (get-in review [:scope :criterion-roles])))
      (is (some #{:full-L2} (:nonclaims review)))
      (is (= :requires-separate-exact-current-conservation (get-in review [:later-subjects :applicability])))
      (is (= (document-text "C6") (sh-crosswalk/git-source (:candidate review) "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"))))
    (is (= 192 (count facets) (count (set (map :id facets)))))
    (is (= 109 (count cases)))
    (is (= 28 (count sections)))
    (is (= #{"L2" "C6"} (set (map :document-id facets))))
    (doseq [facet facets :let [raw (span-text (document-text (:document-id facet)) (:source-span facet)) c (cases (:source-occurrence-id facet))]]
      (is (= (get-in facet [:source-span :sha256]) (sh-inventory/sha256 raw)))
      (is (= (:document-id facet) (:document-id c)))
      (is (= (:role c) (:role facet)))
      (is (= (:criterion-keys c) (:criterion-keys facet)))
      (is (every? #(contains? owners %) (:owner-requirement-ids facet)))
      (is (= :proposal-pending-independent-review (:current-entailment facet))))
    (doseq [[_ c] cases]
      (is (= (get-in c [:source-span :sha256]) (sh-inventory/sha256 (span-text (document-text (:document-id c)) (:source-span c)))))
      (is (every? criterion-keys (:criterion-keys c)))
      (is (if (= :complete-source-predicate-conjunction (:role c)) (seq (:criterion-keys c)) (empty? (:criterion-keys c)))))
    (doseq [[section row] sections id (:occurrence-ids row)]
      (is (= section (:section-key (cases id))))
      (is (= (:criterion-keys row) (:criterion-keys (cases id)))))
    (is (= #{"compiler/c1/preserve-evidence" "compiler/c6/core-verification" "compiler/c6/desugaring-origins" "compiler/c6/domain-boundary" "language/core/apply-checked-call" "language/core/classify-execution-outcome" "language/core/create-closure" "language/core/create-lexical-let" "language/core/destructure-match" "language/core/emit-semantic-core-module" "language/core/enforce-profile-semantics" "language/core/evaluate-do" "language/core/evaluate-quote" "language/core/install-top-level-definition" "language/core/normalize-to-declared-core" "language/core/preserve-observable-evaluation-order" "language/core/reference-var-identity" "language/core/select-if-branch" "language/core/transfer-recur" "language/core/transfer-typed-error" "language/core/update-authorized-location" "language/core/version-semantic-change" "language/effects/check-narrowest-allowance" "language/effects/compose-core-summaries"} (set (keys owners))))
    (is (= "evolution/change-review" (get-in owners ["language/core/version-semantic-change" :task-id])))
    (is (= "core/current-evidence-and-remaining-work/core1-lower-core-forms" (get-in owners ["language/core/apply-checked-call" :task-id])))
    (doseq [[id owner] owners]
      (is (= id (:id owner)))
      (is (tasks (:task-id owner)))
      (is (seq (:original-obligation owner)))
      (is (every? criterion-keys (:criterion-keys owner)))
      (is (every? criterion-keys (:supporting-producer-consumer-criteria owner)))
      (when-let [old-id (get-in owner [:historical-current-task :id])] (is (= old-id (:task-id owner))))
      (is (= :unassessed (:whole-attached-owner-predicate owner)))
      (is (= :none (:implementation-credit owner))))
    (is (= [13 5 4 8 7 7 8 8]
           (mapv #(count (get-in r [:allocation-reconciliation %])) [:core-forms :expression-outcomes :dangerous-operation-outcomes :L2-artifacts :C6-artifacts :L2-diagnostics :C6-diagnostics :evaluation-records])))
    (is (= :no-target-undefined-behavior-or-implicit-fifth-outcome (get-in r [:allocation-reconciliation :safety])))
    (is (= :syntactic-declarations-and-real-resolver-seeds-at-construction-later-inferred-facts-only-from-actual-owners (get-in r [:allocation-reconciliation :facts])))
    (is (str/includes? (current-raw :semantics) "only false and nil"))
    (is (str/includes? (current-raw :lexical) "normally returning guards require Bool"))
    (is (str/includes? (current-raw :artifacts) "pending binding marker"))
    (is (= :none (get-in r [:historical-reviews :new-review]) (get-in r [:historical-reviews :renewal]) (get-in r [:historical-reviews :new-consumption])))
    (doseq [record (concat (:task-sources r) (:plan-sources r) (:specification-sources r))
            :let [p (:path record) before (:before record) after (:after record)]]
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p))))
      (is (= (:sha256 after) (sh-inventory/sha256 (read-text p)) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) p)))))
    (is (= [1 20 1] (mapv #(count (% r)) [:task-sources :plan-sources :specification-sources])))
    (doseq [d (:canonical-sources r)]
      (is (= (:sha256 d) (sh-inventory/sha256 (document-text (:document-id d))) (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit d) (:path d))))))
    (let [d (:selected-SH07-call-order r)]
      (is (= "SH07-C05" (:criterion d)))
      (is (= :operator-before-arguments-once (:order d)))
      (is (= (:sha256 d) (sh-inventory/sha256 (read-text (:path d))) (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit d) (:path d))))))
    (is (= 8 (count (get-in r [:implementation-source-inspection :files]))))
    (doseq [{:keys [path source-commit sha256]} (get-in r [:implementation-source-inspection :files])]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (doseq [file ["spec.md" "plan.md" "tasks.md" "evidence.md"]
            :let [p (str "specs/001-paired-source-core/" file)]]
      (is (= (sh-crosswalk/git-source (:commit previous) p) (read-text p))))
    (is (= :selected-static-definitions-only-no-execution (get-in r [:implementation-source-inspection :scope])))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :core-refinement])))))

(deftest core-dependencies-preserve-real-stage-subjects-and-conditional-optimization
  (let [checkpoint (get-in sh-crosswalk/readiness-source-version [:types-refinement :predecessor-readiness-successor])
        bridge (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint))) sh-crosswalk/readiness-source-version)
        read-text (if checkpoint (memoize (fn [p] (sh-crosswalk/git-source (:commit checkpoint) p))) slurp)
        r (:core-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        rows (fn [v] (into {} (for [records (vals (:task-authorities v)) row records] [(:id row) row])))
        tasks (rows view) before (rows prior)
        ids (set (concat (keys tasks) (map :id (mapcat val (:future-task-authorities view)))))
        result (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view) read-text (:required-parent-consumers r))
        review (:dependency-review result) routes (:routes review)
        dep (:dependency-source r) text (read-text (:path dep))
        old-routes (acceptance-dependencies/future-dependency-routes (sh-crosswalk/git-source (:commit previous) (:path dep)) ids ids)
        consumers (set (:consumer-task-ids r)) new-consumers (set (:new-consumer-task-ids r))
        selected (filterv #(consumers (:consumer %)) routes)
        current-map (into {} (map (juxt :consumer identity) routes)) old-map (into {} (map (juxt :consumer identity) old-routes))
        meaning #(select-keys % [:consumer :start-results :composition-results :selected-output-contract])
        allowed [:input-fingerprint :core-dependency-refinement :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes
                 :source-input-commit :task-comparisons :status :predecessor :kind :retained-historical-source-inputs :qualification
                 :current-source-checkpoint :current-source-paths :future-task-records :historical-source-frame
                 :source-frame-fingerprint :source-frame-scope :source-input-git-tree :source-input-tree]]
    (is (= "a68b5464f95216a8327a46e59d993416dba086ad" (:commit previous)))
    (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
    (is (= (:projection-sha256 previous) (sh-inventory/sha256 projection-text)))
    (is (= (:input-fingerprint previous) (:input-fingerprint old)))
    (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
    ;; The predecessor comparison frame remains authenticated by `old`; the
    ;; current composition has a separate exact census after current-main
    ;; task changes.
    (is (= [49 90] [(get-in bridge [:task-comparisons :unchanged-body-count])
                     (count (get-in bridge [:task-comparisons :changed-bodies]))]))
    (is (= 149 (count tasks) (count before)))
    (is (= 25 (count (:task-authorities view))))
    (is (= (set (keys tasks)) (set (keys before))))
    (doseq [[id row] tasks]
      (is (= (:checked row) (:checked (before id))))
      ;; This projection is an immutable historical checkpoint.  Its row
      ;; source commits therefore need to be authenticated against their own
      ;; historical frame, not compared with the current-main source pin.
      (is (= (:file-sha256 row)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit row) (:path row)))))
      (is (= (get-in row [:source :sha256])
             (sh-inventory/sha256 (acceptance-dependencies/source-raw (:source row) (:source-snapshots view))))))
    (let [future-before (into {} (for [[_region records] (:future-task-authorities prior)
                                       row records] [(:id row) row]))
          future-after (into {} (for [[_region records] (:future-task-authorities view)
                                      row records] [(:id row) row]))]
      (is (= (set (keys future-before)) (set (keys future-after))))
      (doseq [[id row] future-after]
        (is (= (:checked row) (:checked (future-before id))))
        (is (= (:file-sha256 row)
               (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit row) (:path row))))))
      )
    (is (= [] (:changed-task-ids r) (:new-task-identities r)))
    (is (= [59 13 46 778 981 203 1577 1780 137 97 40 78]
           [(count consumers) (count new-consumers) (count (:predecessor-selected-routes r))
            (:preserved-prior-input-joins r) (:selected-consumer-result-joins r) (:new-selected-producer-result-joins r)
            (:predecessor-all-selected-result-joins r) (:all-selected-result-joins r) (count routes)
            (count (:declared-parent-consumers review)) (count (:declared-consumers review)) (:preserved-consumer-route-count r)]))
    (is (= 59 (count selected) (count (:routes r))))
    (is (= 778 (reduce + (map (comp count :composition-results) (:predecessor-selected-routes r)))))
    (is (= 981 (reduce + (map (comp count :composition-results) selected))))
    (is (= 203 (reduce + (map count (vals (:added-composition-inputs r))))))
    (is (= 1780 (count (filter #(= :actual-selected-producer-result (:kind %)) (:typed-edges result)))))
    (is (= (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) old-routes)))
           (into {} (map (juxt :consumer meaning) (remove #(consumers (:consumer %)) routes)))))
    (is (= 9 (count (:core-source-consumer-task-ids r))))
    (is (= 15 (get-in r [:core-source-binding :criteria])))
    (is (= (get-in bridge [:core-refinement :source-commit]) (get-in r [:core-source-binding :source-commit])))
    (is (every? consumers (:core-source-consumer-task-ids r)))
    (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
    (doseq [row selected :let [id (:consumer row) old-row (old-map id)]]
      (is (= (meaning row) (meaning (first (filter #(= id (:consumer %)) (:routes r))))))
      (is (empty? (:start-results row)))
      (when old-row
        (is (every? (set (:composition-results row)) (:composition-results old-row)))
        (is (str/starts-with? (:selected-output-contract row) (:selected-output-contract old-row))))
      (doseq [input (:composition-results row)]
        (is (some #(= [(str "task:" id "/implementation") (str "task:" input "/interface") :declared-producer-interface]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (some #(= [(str "task:" id "/composition") (str "result:" id ":" input) :actual-selected-producer-result]
                      [(:from %) (:to %) (:kind %)]) (:typed-edges result)))
        (is (= :unassessed (get-in result [:nodes (str "result:" id ":" input) :state])))))
    (doseq [id new-consumers
            :let [pattern (re-pattern (str "(?m)^\\| `" (java.util.regex.Pattern/quote id) "` \\|[^\\n]*\\n"))
                  missing (acceptance-dependencies/future-task-view (:future-task-authorities view) tasks (:source-snapshots view)
                            #(if (= % (:path dep)) (str/replace text pattern "") (read-text %)) (:required-parent-consumers r))]]
      (is (= 1 (count (re-seq pattern text))))
      (is (= [id] (get-in missing [:dependency-review :unresolved-consumers]))))
    (let [prefix "lower/current-evidence-and-remaining-work/"
          opt5 "opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline"
          opt4 "opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate"]
      (is (some #{["lower" 5 "opt" 5 :optimization-requested]} acceptance-dependencies/composition-routes))
      (is (not-any? #{opt5} (:composition-results (current-map (str prefix "lower1-validate-lowering-inputs")))))
      (is (not-any? #{opt5} (:composition-results (current-map (str prefix "lower5-integrate-target-emission")))))
      (is (not-any? #{opt4} (:composition-results (current-map (str prefix "lower2-derive-abi-and-layout")))))
      (is (str/includes? (:selected-output-contract (current-map (str prefix "lower1-validate-lowering-inputs"))) "verified unoptimized MIR")))
    (is (= [] (get-in r [:optional-optimization :new-universal-optimizer-joins])))
    (is (= [95 9] (mapv count (:coarse-producer-cycles r)) (mapv count (:declared-output-cycles review))))
    (is (= [82 9] (mapv count (:predecessor-coarse-cycles r))))
    (is (= new-consumers (set (:new-main-cycle-members r))))
    (is (= (into (set (first (:predecessor-coarse-cycles r))) new-consumers) (set (first (:coarse-producer-cycles r)))))
    (is (= [] (:new-release-cycle-members r)))
    (is (= (second (:predecessor-coarse-cycles r)) (second (:coarse-producer-cycles r))))
    (is (= 195 (count (:staged-result-subjects r)) (count (set (map :result (:staged-result-subjects r))))))
    (doseq [stage (:staged-result-subjects r)]
      (is (= (:result stage) (str "result:" (:consumer stage) ":" (:producer stage))))
      (is (some #{(:producer stage)} (:composition-results (current-map (:consumer stage)))))
      (is (= :Core-and-new-pipeline-input-subjects (:route-scope stage)))
      (is (keyword? (:role stage))))
    (doseq [[k record] (:supporting-existing-criteria r)]
      (is (= (:unchanged-record-sha256 record) (sh-inventory/sha256 (pr-str (get bridge k))))))
    (doseq [record (concat [dep (:dependency-tool-source r)] (:plan-sources r))
            :let [p (:path record) before (:before record) after (:after record)]]
      (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) p))))
      ;; The route's :after record is immutable historical evidence.  Current
      ;; composition may have a newer exact source frame for the same path;
      ;; authenticate both rather than conflating the frames.
      (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) p))))
      (when-let [current-commit (sh-crosswalk/current-source-commit p nil)]
        (is (= (sh-inventory/sha256 (slurp p))
               (sh-inventory/sha256 (sh-crosswalk/git-source current-commit p))))))
    (is (= 20 (count (:plan-sources r))))
    (is (= 11 (count (:canonical-sources r))))
    (doseq [{:keys [path source-commit sha256]} (:canonical-sources r)]
      (is (= sha256 (sh-inventory/sha256 (read-text path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :core-dependency-refinement])))))

(deftest
 complete-types-source-preserves-independent-inference-and-original-role-conjunctions
 (let
  [checkpoint
   (get-in
    sh-crosswalk/readiness-source-version
    [:types-dependency-refinement :predecessor-readiness-successor])
   bridge
   (if
    checkpoint
    (edn/read-string
     (sh-crosswalk/git-source (:commit checkpoint) (:path checkpoint)))
    sh-crosswalk/readiness-source-version)
   read-text
   (if
    checkpoint
    (memoize (fn [p] (sh-crosswalk/git-source (:commit checkpoint) p)))
    slurp)
   r
   (:types-refinement bridge)
   previous
   (:predecessor-readiness-successor r)
   old-text
   (sh-crosswalk/git-source (:commit previous) (:path previous))
   old
   (edn/read-string old-text)
   projection-text
   (sh-crosswalk/git-source
    (:commit previous)
    (:projection-path previous))
   prior
   (edn/read-string projection-text)
   view
   (if
    checkpoint
    (edn/read-string
     (sh-crosswalk/git-source
      (:commit checkpoint)
      (:projection-path checkpoint)))
    (sh-crosswalk/build))
   rows
   (fn
    [v]
    (into
     {}
     (for
      [records (vals (:task-authorities v)) row records]
      [(:id row) row])))
   tasks
   (rows view)
   before
   (rows prior)
   changed
   (set (:changed-task-ids r))
   criteria
   (:criteria r)
   by-key
   (into {} (map (juxt :key identity) criteria))
   criterion-keys
   (set (keys by-key))
   task-text
   (fn
    [v row]
    (acceptance-dependencies/source-raw
     (:source row)
     (:source-snapshots v)))
   span-text
   (fn
    [text span]
    (String.
     (.getBytes text "UTF-8")
     (int (:start-byte span))
     (int (- (:end-byte span) (:start-byte span)))
     "UTF-8"))
   current-raw
   (fn
    [k]
    (let
     [w (:current-witness (by-key k))]
     (span-text (read-text (:path w)) (:source-span w))))
   documents
   (into
    {}
    (for
     [d (:canonical-sources r)]
     [(:document-id d) (read-text (:path d))]))
   facets
   (:source-facets r)
   cases
   (:occurrence-cases r)
   sections
   (:section-conjunctions r)
   owners
   (:owner-joins r)
   allowed
   [:input-fingerprint
    :source-input-commit
    :source-files
    :applying-source-pins
    :inspection-locators
    :additional-pinned-evidence-hashes
    :task-comparisons
   :types-refinement
   :macro-cache-refinement]]
  (is
   (= "3adae6fc72518528ee4c43a0f003b0351aa62d21" (:commit previous)))
  (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
  (is
   (=
    (:projection-sha256 previous)
    (sh-inventory/sha256 projection-text)))
  (is (= (:input-fingerprint previous) (:input-fingerprint old)))
  (is (= (apply dissoc
               (assoc-in old [:macro-cache-refinement :boundary :source-snapshot-ref]
                         (get-in bridge [:macro-cache-refinement :boundary :source-snapshot-ref]))
               allowed)
         (apply dissoc bridge allowed)))
  (is (= 149 (count tasks) (count before)))
  (is (= 25 (count (:task-authorities view))))
  (is
   (=
    [44 95]
    [(get-in bridge [:task-comparisons :unchanged-body-count])
     (count (get-in bridge [:task-comparisons :changed-bodies]))]))
  (is (= (set (keys tasks)) (set (keys before))))
  (is
   (=
    (:future-task-authorities prior)
    (:future-task-authorities view)))
  (is (= [] (:new-task-identities r)))
  (is
   (= 5 (count changed) (count (:newly-amended-original-task-ids r))))
  (is (= 11 (count criteria) (count criterion-keys)))
  (is (= 6 (count (set (map :task-id criteria)))))
  (is (= 258 (count facets) (count (set (map :id facets)))))
  (is (= 150 (count cases)))
  (is (= 38 (count sections)))
  (is (= 20 (count owners)))
  (is (= 3 (count (:shared-role-contexts r))))
  (is (= 2 (count (:superseded-accounting-fragments r))))
  (is
   (=
    [14691 3517 3200 1292 975]
    (mapv
     (fn* [p1__146#] (get-in r [:allocation-reconciliation p1__146#]))
     [:source-clause-bytes
      :restored-finite-bytes
      :profile74-bytes
      :schema25-bytes
      :unchanged-Evolution-bytes])))
  (is
   (=
    [9 9 8 10]
    (mapv
     (fn*
      [p1__147#]
      (count (get-in r [:allocation-reconciliation p1__147#])))
     [:L5-artifacts :C7-artifacts :L5-diagnostics :C7-diagnostics])))
  (is
   (=
    [1 20 1]
    (mapv
     (fn* [p1__148#] (count (p1__148# r)))
     [:task-sources :plan-sources :specification-sources])))
  (is
   (= 8 (count (get-in r [:implementation-source-inspection :files]))))
  (is
   (str/includes?
    (current-raw :constraints)
    "A Never argument in any coordinate"))
  (is
   (str/includes?
    (current-raw :constraints)
    "nil-or-Bool is not established Bool"))
  (is
   (str/includes?
    (current-raw :verification)
    "exactly one current type or diagnostic"))
  (is
   (=
    :none
    (get-in r [:historical-reviews :new-review])
    (get-in r [:historical-reviews :renewal])
    (get-in r [:historical-reviews :new-consumption])))
  (is (= :pending (:independent-current-review r)))
  (is
   (=
    :unassessed
    (:implementation-acceptance r)
    (:global-source-conjunctions r)))
  (is
   (=
    r
    (get-in
     view
     [:implementation-readiness-source-version :types-refinement])))
  (doseq
   [[id row] tasks]
   (is (= (:checked row) (:checked (before id))))
   (is
    (if
     (changed id)
     (not= (task-text view row) (task-text prior (before id)))
     (= (task-text view row) (task-text prior (before id))))))
  (doseq
   [{:keys [task-id current-witness]}
    criteria
    :let
    [text
     (read-text (:path current-witness))
     raw
     (span-text text (:source-span current-witness))
     row
     (tasks task-id)]]
   (is (some? row))
   (is (= (:source-commit r) (:source-commit current-witness)))
   (is (= (:sha256 current-witness) (sh-inventory/sha256 text)))
   (is
    (=
     (get-in current-witness [:source-span :sha256])
     (sh-inventory/sha256 raw)))
   (is (str/includes? (task-text view row) raw))
   (is
    (nil?
     (acceptance-dependencies/current-task-body!
      (:source row)
      (:source-snapshots view)
     read-text)))
   (is
    (thrown?
     Exception
     (acceptance-dependencies/current-task-body!
      (:source row)
      (:source-snapshots view)
      (fn*
       [p1__159#]
       (if
        (= p1__159# (:path current-witness))
        (str/replace text raw "")
        (read-text p1__159#)))))))
  (doseq
   [facet
    facets
    :let
    [raw
     (span-text (documents (:document-id facet)) (:source-span facet))
     c
     (cases (:source-occurrence-id facet))]]
   (is
    (=
     (get-in facet [:source-span :sha256])
     (sh-inventory/sha256 raw)))
   (is (= (:document-id facet) (:document-id c)))
   (is (= (:role c) (:role facet)))
   (is (= (:criterion-keys c) (:criterion-keys facet)))
   (is
    (every?
     (fn* [p1__160#] (contains? owners p1__160#))
     (:owner-requirement-ids facet)))
   (is
    (=
     :proposal-pending-independent-review
     (:current-entailment facet))))
  (doseq
   [[_ c] cases]
   (is
    (=
     (get-in c [:source-span :sha256])
     (sh-inventory/sha256
      (span-text (documents (:document-id c)) (:source-span c)))))
   (is (every? criterion-keys (:criterion-keys c)))
   (is
    (if
     (= :complete-source-predicate-conjunction (:role c))
     (seq (:criterion-keys c))
     (empty? (:criterion-keys c)))))
  (doseq
   [[section row] sections id (:occurrence-ids row)]
   (is (= section (:section-key (cases id))))
   (is (= (:criterion-keys row) (:criterion-keys (cases id)))))
  (doseq
   [[id owner] owners]
   (is (= id (:id owner)))
   (is (tasks (:task-id owner)))
   (is (seq (:original-obligation owner)))
   (is (every? criterion-keys (:criterion-keys owner)))
   (is
    (if-let
     [old-id (get-in owner [:historical-current-task :id])]
     (= old-id (:task-id owner))
     true))
   (is (= :unassessed (:whole-attached-owner-predicate owner)))
   (is (= :none (:implementation-credit owner))))
  (doseq
   [{:keys [task-id current-witness]} (:shared-role-contexts r)]
   (is (tasks task-id))
   (is
    (=
     (task-text prior (before task-id))
     (task-text view (tasks task-id))))
   (is
    (=
     (get-in current-witness [:source-span :sha256])
     (sh-inventory/sha256
      (span-text
       (read-text (:path current-witness))
       (:source-span current-witness))))))
  (doseq
   [{:keys [path source-span disposition]}
    (:superseded-accounting-fragments r)]
   (is
    (=
     (:sha256 source-span)
     (sh-inventory/sha256 (span-text (read-text path) source-span))))
   (is (= :superseded-accounting-only-fragment disposition)))
  (doseq
   [record
    (concat
     (:task-sources r)
     (:plan-sources r)
     (:specification-sources r))
    :let
    [p (:path record) before (:before record) after (:after record)]]
   (is
    (=
     (:sha256 before)
     (sh-inventory/sha256
      (sh-crosswalk/git-source (:commit before) p))))
   (is
    (=
     (:sha256 after)
     (sh-inventory/sha256 (read-text p))
     (sh-inventory/sha256
      (sh-crosswalk/git-source (:commit after) p)))))
  (doseq
   [{:keys [path source-commit sha256]}
    (get-in r [:implementation-source-inspection :files])]
   (is
    (=
     sha256
     (sh-inventory/sha256 (read-text path))
     (sh-inventory/sha256
      (sh-crosswalk/git-source source-commit path)))))
  (let
   [p
    "specs/types-workflow/tasks.md"
    restored
    (reduce
     (fn
      [text [_ marker]]
      (let
       [pattern
        (re-pattern
         (str
          "(?s)\n\n<!-- "
          marker
          ":start -->.*?<!-- "
          marker
          ":end -->\n"))]
       (is (= 1 (count (re-seq pattern text))))
       (str/replace text pattern "")))
     (read-text p)
     (:markers r))]
   (is (= (sh-crosswalk/git-source (:commit previous) p) restored)))
  (let
   [published
    (:published-owner-source r)
    text
    (sh-crosswalk/git-source
     (:commit published)
     "specs/types-workflow/tasks.md")]
   (is
    (= "505d8cd3ec02bc9a29ea3444b1665d9d8b7808ea" (:commit published)))
   (is
    (= "8becc8f23ec8b77eb926ff439f98d1d2e55d0ed7" (:tree published)))
   (is (= true (:verified-published-commit published)))
   (is (= 10 (count (:criterion-witnesses published))))
   (doseq
    [{:keys [key task-id sha256 source-span]}
     (:criterion-witnesses published)]
    (is (= sha256 (sh-inventory/sha256 text)))
    (is
     (=
      (:sha256 source-span)
      (sh-inventory/sha256 (span-text text source-span))))
    (is (= (current-raw key) (span-text text source-span)))
    (is (= task-id (:task-id (by-key key))))))
  (doseq
   [[k record]
    (:retained-finite-predicates r)
    :let
    [w
     (:criterion-witness record)
     text
     (sh-crosswalk/git-source (:source-commit w) (:path w))
     raw
     (span-text
      text
      {:start-byte (:start-byte w), :end-byte (:end-byte w)})]]
   (is (= (:file-sha256 w) (sh-inventory/sha256 text)))
   (is (= (:raw-sha256 w) (sh-inventory/sha256 raw)))
   (is (= raw (current-raw k)))
   (is (seq (:qualified-review-adoption record))))
  (doseq
   [record
    [(get-in
      r
      [:retained-finite-predicates
       :schema-static
       :qualified-review-adoption])
     (get-in r [:historical-reviews :schema25])]]
   (is
    (=
     "893bb4b85926a9d7f8953798544b2126baf6ef58"
     (:reviewed-subject-commit record)))
   (is
    (=
     "65e0704bc180521c3393725b39c284995657a065"
     (:source-commit record)))
   (is
    (=
     (:sha256 record)
     (sh-inventory/sha256
      (sh-crosswalk/git-source
       (:source-commit record)
       (:path record))))))
  (let
   [review (get-in r [:historical-reviews :Types])]
   (is
    (=
     "f754c97aaea679fd2bf162ecbbb33d706ec2a753ed9341b93dd64de70dad35dc"
     (:sha256 review)))
   (is
    (=
     (:sha256 review)
     (sh-inventory/sha256
      (sh-crosswalk/git-source
       (:artifact-source-commit review)
       (:path review)))))
   (is
    (=
     "8834ac0e33f37faebc9bcc983e247d4dc9491501"
     (:artifact-source-commit review)))
   (is
    (= "b8108264f9770e1543942f7f524b58868e553fef" (:candidate review)))
   (is
    (=
     [258 150 108 20 5]
     (mapv
      (fn* [p1__145#] (get-in review [:scope p1__145#]))
      [:facets
       :occurrences
       :semantic-conjunctions
       :owner-joins
       :actual-Types-tasks])))
   (is (some #{:no-implementation-credit} (:limits review)))
   (doseq
    [d (:canonical-sources r)]
    (is
     (=
      (documents (:document-id d))
      (sh-crosswalk/git-source (:candidate review) (:path d))))))
  (let
   [task-id (get-in by-key [:type-form-change :task-id])]
   (is
    (=
     :existing-exact-conditional-predicate-no-task-amendment
     (get-in by-key [:type-form-change :status])))
   (is
    (=
     (task-text prior (before task-id))
     (task-text view (tasks task-id)))))
  (let
   [text (read-text "specs/core-workflow/plan.md")]
   (is
    (=
     1
     (count
      (re-seq #"(?m)^## Core producer and consumer order$" text))))
   (is
    (=
     1
     (count
      (re-seq
       #"(?m)^### Historical evidence and current static findings$"
       text)))))))


(deftest
 types-dependencies-preserve-independent-premises-and-real-stage-results
 (let
  [bridge
   sh-crosswalk/readiness-source-version
   read-text
   (memoize
    (fn [p]
      (sh-crosswalk/git-source
       (sh-crosswalk/current-source-commit p (:source-input-commit bridge)) p)))
   r
   (:types-dependency-refinement bridge)
   previous
   (:predecessor-readiness-successor r)
   old-text
   (sh-crosswalk/git-source (:commit previous) (:path previous))
   old
   (edn/read-string old-text)
   projection-text
   (sh-crosswalk/git-source
    (:commit previous)
    (:projection-path previous))
   prior
   (edn/read-string projection-text)
   view
   (sh-crosswalk/build)
   rows
   (fn
    [v]
    (into
     {}
     (for
      [records (vals (:task-authorities v)) row records]
      [(:id row) row])))
   tasks
   (rows view)
   before
   (rows prior)
   ids
   (set
    (concat
     (keys tasks)
     (map :id (mapcat val (:future-task-authorities view)))))
   result
   (acceptance-dependencies/future-task-view
    (:future-task-authorities view)
    tasks
    (:source-snapshots view)
       read-text
    (:required-parent-consumers r))
   review
   (:dependency-review result)
   routes
   (:routes review)
   dep
   (:dependency-source r)
   text
   (read-text (:path dep))
   old-routes
   (acceptance-dependencies/future-dependency-routes
    (sh-crosswalk/git-source (:commit previous) (:path dep))
    ids
    ids)
   consumers
   (set (:consumer-task-ids r))
   new-consumers
   (set (:new-consumer-task-ids r))
   selected
   (filterv (fn* [p1__146#] (consumers (:consumer p1__146#))) routes)
   current-map
   (into {} (map (juxt :consumer identity) routes))
   old-map
   (into {} (map (juxt :consumer identity) old-routes))
   meaning
   (fn*
    [p1__147#]
    (select-keys
     p1__147#
     [:consumer
      :start-results
      :composition-results
      :selected-output-contract]))
   allowed
   [:input-fingerprint
    :types-dependency-refinement
    :types-refinement
    :source-files
    :applying-source-pins
    :inspection-locators
    :additional-pinned-evidence-hashes
    :source-input-commit
    :task-comparisons
    :status
    :predecessor
    :kind
    :retained-historical-source-inputs
    :qualification
    :current-source-checkpoint
    :current-source-paths
    :current-source-transitions
   :current-source-transition-history
   :current-source-transition-successor-history
   :source-successor-binding
   :current-source-library-io-criterion-joins
   :macro-cache-refinement
    :future-task-records
    :library-refinement
   :error-refinement
   :protocol-dependency-refinement
   :protocol-refinement
   :source-digests
   :source-snapshots
    :current-source-frame-history
    :library-io-criterion-joins
    :historical-source-frame
    :source-frame-fingerprint
    :source-frame-scope
    :source-input-git-tree
    :source-input-tree
   :source-effects-refinement
   :source-effects-dependency-refinement]]
  (is
   (= "1ef683ebfa505100ba47b9541d3304f06174bbaa" (:commit previous)))
  (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
  (is
   (=
    (:projection-sha256 previous)
    (sh-inventory/sha256 projection-text)))
  (is (= (:input-fingerprint previous) (:input-fingerprint old)))
  (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
  (is
   (= [36 103 10]
      [(get-in bridge [:task-comparisons :unchanged-body-count])
       (count (get-in bridge [:task-comparisons :changed-bodies]))
       (count (get-in bridge [:task-comparisons :added-ids]))]))
  (is
   (= #{"evolution/change-review"
        "evolution/experiment-lifecycle"
        "evolution/library-admission"
        "evolution/namespace-decision"
        "evolution/registry-yank"
        "evolution/rfc-review"
        "evolution/security-response"
        "evolution/security-review"
        "evolution/stabilization-decision"
        "evolution/target-support-admission"}
      (set (get-in bridge [:task-comparisons :added-ids]))))
  (is (= 149 (count tasks) (count before)))
  (is (= 25 (count (:task-authorities view))))
  (is (= (set (keys tasks)) (set (keys before))))
  (doseq
   [[id row] tasks]
   (let [prior-row (before id)]
     (is (= (:checked row) (:checked prior-row)))
     (is (= (:file-sha256 row)
            (sh-inventory/sha256
             (sh-crosswalk/git-source (:source-commit row) (:path row)))))
     (is (= (get-in row [:source :sha256])
            (sh-inventory/sha256
             (acceptance-dependencies/source-raw
              (:source row) (:source-snapshots view)))))))
  (let [future-rows
        (fn [v]
          (into {}
                (for [[_region records] (:future-task-authorities v)
                      row records]
                  [(:id row) row])))
        future-before (future-rows prior)
        future-after (future-rows view)]
    (is (= (set (keys future-before)) (set (keys future-after))))
    (doseq [[id row] future-after]
      (is (= (:checked row) (:checked (future-before id))))
      (is (= (:file-sha256 row)
             (sh-inventory/sha256
              (sh-crosswalk/git-source (:source-commit row) (:path row)))))
      (is (= (get-in row [:source :sha256])
             (sh-inventory/sha256
              (acceptance-dependencies/source-raw
               (:source row) (:source-snapshots view)))))))
  (is (= [] (:changed-task-ids r) (:new-task-identities r)))
  (is
   (=
    [46 0 46 819 932 113 1780 1893 144 104 40 91]
    [(count consumers)
     (count new-consumers)
     (count (:predecessor-selected-routes r))
     (:preserved-prior-input-joins r)
     (:selected-consumer-result-joins r)
     (:new-selected-producer-result-joins r)
     (:predecessor-all-selected-result-joins r)
     (:all-selected-result-joins r)
     (count routes)
     (count (:declared-parent-consumers review))
     (count (:declared-consumers review))
     (:preserved-consumer-route-count r)]))
  (is (= 46 (count selected) (count (:routes r))))
  (is
   (=
    819
    (reduce
     +
     (map
      (comp count :composition-results)
      (:predecessor-selected-routes r)))))
  (is
   (= 1062 (reduce + (map (comp count :composition-results) selected))))
  (is
   (= 113 (reduce + (map count (vals (:added-composition-inputs r))))))
  (is
   (=
    2156
    (count
     (filter
      (fn*
       [p1__148#]
       (= :actual-selected-producer-result (:kind p1__148#)))
      (:typed-edges result)))))
  ;; The current source transition adds L13 dependency rows.  Existing
  ;; non-consumer routes remain a prefix/subset of the current route graph;
  ;; the new rows may extend their composition and selected contract.
  (doseq [[id old-row]
          (remove (fn [[id _]] (consumers id))
                  (into {} (map (juxt :consumer identity) old-routes)))]
    (let [row (current-map id)]
      (is row)
      (is (= (:start-results old-row) (:start-results row)))
      (is (every? (set (:composition-results row))
                  (:composition-results old-row)))
      (is (str/starts-with? (:selected-output-contract row)
                            (:selected-output-contract old-row)))))
  (is (= 6 (count (:types-source-consumer-task-ids r))))
  (is (= 11 (get-in r [:types-source-binding :criteria])))
  (is
   (=
    (get-in bridge [:types-refinement :source-commit])
    (get-in r [:types-source-binding :source-commit])))
  (is (every? consumers (:types-source-consumer-task-ids r)))
  (is (= [] (:unresolved-consumers r) (:unresolved-consumers review)))
  (doseq
   [row selected :let [id (:consumer row) old-row (old-map id)]]
   (is
    (=
     (meaning row)
     (meaning (current-map id))))
   (is (empty? (:start-results row)))
   (when
    old-row
    (is
     (every?
      (set (:composition-results row))
      (:composition-results old-row)))
    (is
     (str/starts-with?
      (:selected-output-contract row)
      (:selected-output-contract old-row))))
   (doseq
    [input (:composition-results row)]
    (is
     (some
      (fn*
       [p1__152#]
       (=
        [(str "task:" id "/implementation")
         (str "task:" input "/interface")
         :declared-producer-interface]
        [(:from p1__152#) (:to p1__152#) (:kind p1__152#)]))
      (:typed-edges result)))
    (is
     (some
      (fn*
       [p1__153#]
       (=
        [(str "task:" id "/composition")
         (str "result:" id ":" input)
         :actual-selected-producer-result]
        [(:from p1__153#) (:to p1__153#) (:kind p1__153#)]))
      (:typed-edges result)))
    (is
     (=
      :unassessed
      (get-in result [:nodes (str "result:" id ":" input) :state])))))
  (doseq
   [id
    (:types-source-consumer-task-ids r)
    :let
    [pattern
     (re-pattern
      (str
       "(?m)^\\| `"
       (java.util.regex.Pattern/quote id)
       "` \\|[^\\n]*\\n"))
     missing
     (acceptance-dependencies/future-task-view
      (:future-task-authorities view)
      tasks
      (:source-snapshots view)
      (fn*
       [p1__154#]
       (if
        (= p1__154# (:path dep))
        (str/replace text pattern "")
        (slurp p1__154#)))
      (:required-parent-consumers r))]]
   (is (= 1 (count (re-seq pattern text))))
   (is
    (=
     [id]
     (get-in missing [:dependency-review :unresolved-consumers]))))
  (let
   [prefix
    "lower/current-evidence-and-remaining-work/"
    opt5
    "opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline"
    opt4
    "opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate"]
   (is
    (some
     #{["lower" 5 "opt" 5 :optimization-requested]}
     acceptance-dependencies/composition-routes))
   (is
    (not-any?
     #{opt5}
     (:composition-results
      (current-map (str prefix "lower1-validate-lowering-inputs")))))
   (is
    (not-any?
     #{opt5}
     (:composition-results
      (current-map (str prefix "lower5-integrate-target-emission")))))
   (is
    (not-any?
     #{opt4}
     (:composition-results
      (current-map (str prefix "lower2-derive-abi-and-layout")))))
   (is
    (str/includes?
     (:selected-output-contract
      (current-map (str prefix "lower1-validate-lowering-inputs")))
     "verified unoptimized MIR")))
  (is
   (=
    []
    (get-in
     r
     [:optional-optimization :new-universal-optimizer-joins])))
  (is
   (=
    [95 9]
    (mapv count (:coarse-producer-cycles r))))
  (is (= [99 11] (mapv count (:declared-output-cycles review))))
  (is (= (:predecessor-coarse-cycles r) (:coarse-producer-cycles r)))
  (is
   (=
    192
    (count (:staged-result-subjects r))
    (count (set (map :result (:staged-result-subjects r))))))
  (doseq
   [stage (:staged-result-subjects r)]
   (is
    (=
     (:result stage)
     (str "result:" (:consumer stage) ":" (:producer stage))))
   (is
    (some
     #{(:producer stage)}
     (:composition-results (current-map (:consumer stage)))))
   (is
    (=
     :selected-L5-C7-proposal-inputs-not-all-old-route-edges
     (:route-scope stage)))
   (is (keyword? (:role stage))))
  (is (= (:types-refinement old) (:types-refinement bridge)))
  (is
   (=
    (get-in r [:types-source-binding :record-sha256])
    (sh-inventory/sha256 (pr-str (:types-refinement bridge)))))
  (is
   (=
    :all-finite-subjects-frozen-no-renewal-consumption-adoption-or-new-review
    (:review-conservation r)))
  (is
   (=
    [11 6 2 3 9 4 258 150 38 20]
    (mapv
     (fn* [p1__155#] (get-in r [:types-source-binding p1__155#]))
     [:criteria
      :criterion-tasks
      :criterion-authorities
      :inherited-role-contexts
      :joint-tasks
      :joint-authorities
      :source-facets
      :source-occurrences
      :section-conjunctions
      :original-owner-joins])))
  (doseq
   [record
    (concat [dep] (:plan-sources r))
    :let
    [p (:path record) before (:before record) after (:after record)]]
   (is
    (=
     (:sha256 before)
     (sh-inventory/sha256
      (sh-crosswalk/git-source (:commit before) p))))
   (is
    (= (:sha256 after)
       (sh-inventory/sha256
        (sh-crosswalk/git-source (:commit after) p))))
   (is
    (= (sh-inventory/sha256 (slurp p))
       (sh-inventory/sha256
       (sh-crosswalk/git-source
        (sh-crosswalk/current-source-commit p (:source-input-commit bridge)) p)))))
  (is (= 20 (count (:plan-sources r))))
  (is (= 2 (count (:canonical-sources r))))
  (doseq
   [{:keys [path source-commit sha256]} (:canonical-sources r)]
   (is
    (=
     sha256
     (sh-inventory/sha256 (slurp path))
     (sh-inventory/sha256
      (sh-crosswalk/git-source source-commit path)))))
  (is (= :pending (:independent-current-review r)))
  (is
   (=
    :unassessed
    (:implementation-acceptance r)
    (:global-source-conjunctions r)))
  (is
   (=
    r
    (get-in
     view
     [:implementation-readiness-source-version
      :types-dependency-refinement])))
  (let
   [tp
    "types/current-evidence-and-remaining-work/"
    t2
    (str tp "types2-check-declared-abstractions")
    t5
    (str tp "types5-integrate-typed-core")
    t1
    (str tp "types1-solve-core-constraints")
    layout
    "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
    role
    (fn
     [c p]
     (:role
      (first
       (filter
        (fn*
         [p1__166#]
         (= [c p] [(:consumer p1__166#) (:producer p1__166#)]))
        (:staged-result-subjects r)))))]
   (is
    (=
     :early-fixed-target-ABI-rule-not-future-generated-Q-layout-or-emission
     (role t2 layout)))
   (is
    (=
     :actual-required-layout-from-early-typed-premises-before-final-typed-verification
     (role t5 layout)))
   (is
    (=
     :authentic-expanded-versioned-schema-before-actual-static-projection
     (role t2 "schema")))
   (is
    (=
     :later-real-generated-type-verification-not-initial-source-schema-input
     (role "schema" t5)))
   (is
    (=
     :available-already-checked-P-not-own-future-generated-Q-typing
     (role
      "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
      t5)))
   (is
    (str/includes?
     (:selected-output-contract (current-map t1))
     "complete private visibility/export/use/call/escape/mutation/original-target re-entry census"))
   (is
    (str/includes?
     (:selected-output-contract (current-map t1))
     "Expected results, Dynamic defaults, solved flags, circular/vacuous evidence"))
   (is
    (str/includes?
     (:selected-output-contract (current-map t5))
     "exactly one type or diagnostic per node"))
   (is
    (=
     :unassessed
     (get-in result [:nodes (str "result:" t2 ":" layout) :state]))))))


(defn legacy-complete-protocol-source-preserves-method-effects-and-owner-conjunctions []
 (let [bridge sh-crosswalk/readiness-source-version
       r (:protocol-refinement bridge)
       previous (:predecessor-readiness-successor r)
       old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
       old (edn/read-string old-text)
       old-projection (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
       prior (edn/read-string old-projection)
       view (sh-crosswalk/build)
       rows (fn [v] (into {} (for [records (vals (:task-authorities v)) row records] [(:id row) row])))
       tasks (rows view) before (rows prior)
       task-text (fn [v row] (acceptance-dependencies/source-raw (:source row) (:source-snapshots v)))
       changed (set (:changed-task-ids r))
       criteria (:criteria r) by-key (into {} (map (juxt :key identity) criteria))
       source (first (:canonical-sources r)) text (slurp (:path source))
       span-text (fn [body span] (String. (.getBytes body "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
       current-clause (fn [k] (let [w (:current-witness (by-key k))] (span-text (slurp (:path w)) (:source-span w))))
       facets (:source-facets r) cases (:occurrence-cases r) sections (:section-conjunctions r) owners (:owner-joins r)
       allowed [:input-fingerprint :source-input-commit :source-files :applying-source-pins :inspection-locators :additional-pinned-evidence-hashes :task-comparisons :protocol-refinement]]
  (is (= "fa1866623a979758ead8e2bd9e522c01db2d10d4" (:commit previous)))
  (is (= (:sha256 previous) (sh-inventory/sha256 old-text)))
  (is (= (:projection-sha256 previous) (sh-inventory/sha256 old-projection)))
  (is (= (:input-fingerprint previous) (:input-fingerprint old)))
  (is (= (apply dissoc old allowed) (apply dissoc bridge allowed)))
  (is (= 149 (count tasks) (count before)))
  (is (= 25 (count (:task-authorities view))))
  (is (= (set (keys tasks)) (set (keys before))))
  (let [prior-future (into {} (map (juxt :id identity) (mapcat val (:future-task-authorities prior))))
        actual-future (into {} (map (juxt :id identity) (mapcat val (:future-task-authorities view))))
        successor (:future-task-source-successor r)
        metadata (fn [row] (assoc (select-keys row [:id :region :component :path :checked :source-commit :file-sha256]) :body-sha256 (get-in row [:source :sha256])))
        captured (into {} (map (juxt :id identity) (:after successor)))
        stable-fields [:id :region :component :path :checked :body-sha256]]
   (is (= 40 (count prior-future) (count actual-future) (count captured)))
   (is (= (set (keys prior-future)) (set (keys actual-future)) (set (keys captured))))
   (is (= (:before successor) (get-in old [:macro-refinement :future-task-source-successor :after])))
   (is (= [] (:declaration-body-changes successor)))
   (is (= 16 (count (:changed-source-binding-task-ids successor))))
   (doseq [[id row] actual-future]
    (is (= (task-text view row) (task-text prior (prior-future id))))
    (is (= (select-keys (metadata row) stable-fields) (select-keys (metadata (prior-future id)) stable-fields)))
    (is (= (metadata row) (captured id))))
   (doseq [id (:changed-source-binding-task-ids successor)]
    (is (= "specs/domains-workflow/tasks.md" (:path (actual-future id))))
    (is (= (:source-commit r) (:source-commit (actual-future id)))))
   (with-redefs [sh-crosswalk/readiness-source-version (update-in bridge [:protocol-refinement] dissoc :future-task-source-successor)]
    (is (thrown? Exception (sh-crosswalk/source-view))))
   (with-redefs [sh-crosswalk/readiness-source-version (assoc-in bridge [:protocol-refinement :future-task-source-successor :after 0 :body-sha256] "forged")]
    (is (thrown? Exception (sh-crosswalk/source-view)))))
  (is (= [44 95] [(get-in bridge [:task-comparisons :unchanged-body-count]) (count (get-in bridge [:task-comparisons :changed-bodies]))]))
  (is (= 11 (count changed)))
  (is (= [] (:newly-amended-original-task-ids r) (:new-task-identities r)))
  (is (= 16 (count criteria) (count by-key)))
  (is (= 12 (count (set (map :task-id criteria)))))
  (is (= [81 60 14 5] [(count facets) (count cases) (count sections) (count owners)]))
  (is (= 81 (count (set (map :id facets)))))
  (is (= {:semantic 61 :context 20} (:census-kind-population r)))
  (is (= {:complete-source-predicate-conjunction 59 :retained-hierarchy-or-dependency-context 22} (:current-role-population r)))
  (is (= {:complete-source-predicate-conjunction 41 :retained-hierarchy-or-dependency-context 19} (:occurrence-role-population r)))
  (is (= [7 21 1] (mapv #(count (% r)) [:task-sources :plan-sources :specification-sources])))
  (is (= [5 16 14 2 12 8 11 7 1 1011]
         (mapv #(get-in r [:allocation-reconciliation %]) [:original-owners :criteria :new-source-criteria :unchanged-finite-criteria :criterion-tasks :criterion-authorities :changed-tasks :changed-authorities :inherited-cross-role-owners :unchanged-Evolution-bytes])))
  (is (= [9 8 7 9 7] (mapv #(count (get-in r [:allocation-reconciliation %])) [:method-fields :dispatch-modes :interface-artifacts :artifacts :diagnostics])))
  (is (= 2 (count (get-in r [:implementation-source-inspection :files]))))
  (doseq [[id row] tasks]
   (is (= (:checked row) (:checked (before id))))
   (is (if (changed id) (not= (task-text view row) (task-text prior (before id))) (= (task-text view row) (task-text prior (before id))))))
  (is (= (:sha256 source) (sh-inventory/sha256 text) (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit source) (:path source)))))
  (doseq [{:keys [id source-occurrence-id source-span criterion-keys role census-kind]} facets]
   (let [c (cases source-occurrence-id)]
    (is (string? id))
    (is (#{:semantic :context} census-kind))
    (is (= (:sha256 source-span) (sh-inventory/sha256 (span-text text source-span))))
    (is (= criterion-keys (:criterion-keys c)))
    (is (= role (:role c)))
    (is (= :author-proposal-pending-independent-review (:current-entailment c)))))
  (doseq [[id {:keys [source-span section-key criterion-keys role]}] cases]
   (is (string? id))
   (is (= (:sha256 source-span) (sh-inventory/sha256 (span-text text source-span))))
   (if (= :complete-source-predicate-conjunction role)
    (do (is (seq criterion-keys))
        (is (= criterion-keys (get-in sections [section-key :criterion-keys])))
        (is (some #{id} (get-in sections [section-key :occurrence-ids]))))
    (is (= [] criterion-keys))))
  (doseq [[section row] sections]
   (is (= :complete-preamble-list-example-and-following-conditions-required (:qualification row)))
   (is (seq (:occurrence-ids row)))
   (doseq [id (:occurrence-ids row)] (is (= section (:section-key (cases id)))))
   (doseq [k (:criterion-keys row)] (is (contains? by-key k))))
  (doseq [{:keys [key task-id current-witness status]} criteria
          :let [p (:path current-witness) raw (current-clause key) body (slurp p) task (tasks task-id)]]
   (is task)
   (is (= p (:path task)))
   (is (= (:sha256 current-witness) (sh-inventory/sha256 body) (sh-inventory/sha256 (sh-crosswalk/git-source (:source-commit current-witness) p))))
   (is (= (:sha256 (:source-span current-witness)) (sh-inventory/sha256 raw)))
   (is (str/includes? (task-text view task) raw))
   (is (= (.indexOf body raw) (.lastIndexOf body raw)))
   (if (#{:protocol-change :typed-selection} key)
    (is (= :existing-exact-predicate-no-task-amendment status))
    (let [marker (get-in r [:markers key])]
     (is (= :new-complete-L8-source-criterion-pending-independent-review status))
     (is (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote (str "<!-- " marker ":start -->"))) body))))
     (is (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote (str "<!-- " marker ":end -->"))) body)))))))
  (is (= #{"language/errors/normalize-foreign-failure" "language/protocol/publish-method-contract" "language/protocol/select-checked-dispatch" "language/protocol/specialize-dispatch" "language/protocol/review-stable-change"} (set (keys owners))))
  (doseq [[id row] owners]
   (is (= id (:id row))) (is (tasks (:task-id row))) (is (seq (:original-obligation row)))
   (is (= (set (map #(get-in by-key [% :task-id]) (:criterion-keys row))) (set (:criterion-task-ids row))))
   (is (= :unassessed (:whole-attached-owner-predicate row))) (is (= :none (:implementation-credit row))))
  (let [row (owners "language/protocol/select-checked-dispatch")]
   (is (:inherited-cross-role-context? row))
   (is (= "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects" (:task-id row)))
   (is (contains? (set (:criterion-task-ids row)) (get-in by-key [:selection :task-id])))
   (is (contains? (set (:criterion-task-ids row)) (get-in by-key [:allowance :task-id]))))
  (let [row (owners "language/errors/normalize-foreign-failure")]
   (is (= #{(get-in by-key [:dynamic :task-id]) (get-in by-key [:host :task-id])} (set (:criterion-task-ids row))))
   (is (nil? (:historical-current-task row))))
  (let [record (get-in r [:retained-finite-predicates :protocol-change]) w (:criterion-witness record)
        old-text (sh-crosswalk/git-source (:source-commit w) (:path w))
        raw (span-text old-text {:start-byte (:start-byte w) :end-byte (:end-byte w)})
        id "evolution/change-review"]
   (is (= "45fc1420cfe4373e5a20b1fcd80d46f87e4edb63" (:source-commit w)))
   (is (= (:file-sha256 w) (sh-inventory/sha256 old-text)))
   (is (= (:raw-sha256 w) (sh-inventory/sha256 raw)))
   (is (= 1011 (count (.getBytes raw "UTF-8"))))
   (is (= raw (current-clause :protocol-change)))
   (is (= id (:task-id record) (get-in by-key [:protocol-change :task-id])))
   (is (= (task-text view (tasks id)) (task-text prior (before id))))
   (is (seq (:qualified-review-adoption record))))
  (doseq [[k record] (:supporting-prior-source-records r)]
   (is (= (get old k) (get bridge k)))
   (is (= (:record-sha256 record) (sh-inventory/sha256 (pr-str (get bridge k))))))
  (doseq [record (concat (:task-sources r) (:plan-sources r) (:specification-sources r))
          :let [p (:path record) b (:before record) a (:after record)]]
   (is (= (:sha256 b) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit b) p))))
   (is (= (:sha256 a) (sh-inventory/sha256 (slurp p)) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit a) p)))))
  (doseq [{:keys [path source-commit sha256 scope line-ranges]} (get-in r [:implementation-source-inspection :files])]
   (is (= sha256 (sh-inventory/sha256 (slurp path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path))))
   (if (= scope :complete-file) (is (nil? line-ranges)) (is (seq line-ranges))))
  (doseq [code (get-in r [:allocation-reconciliation :diagnostics])] (is (str/includes? text code)))
  (is (str/includes? (current-clause :selection) "lacking a declared priority rule"))
  (is (str/includes? (current-clause :effects) "declared upper bound"))
  (is (str/includes? (current-clause :specialize) "prove that fallback unreachable"))
  (is (str/includes? (current-clause :profiles) "not retired compiler work accounting"))
  (is (str/includes? (current-clause :host) "normalize actual returned nulls, exceptions"))
  (is (str/includes? (current-clause :tools) "separate authentic capability grants"))
  (let [id (get-in by-key [:typed-selection :task-id]) raw (current-clause :typed-selection)]
   (is (str/includes? (task-text prior (before id)) raw))
   (is (= (:current-witness (by-key :typed-selection)) (get-in r [:retained-finite-predicates :typed-selection :current-witness]))))
  (is (= :none (get-in r [:historical-reviews :new-review]) (get-in r [:historical-reviews :renewal]) (get-in r [:historical-reviews :new-consumption]) (get-in r [:implementation-source-inspection :current-execution])))
  (is (= :pending (:independent-current-review r)))
  (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
  (is (= r (get-in view [:implementation-readiness-source-version :protocol-refinement])))))

(deftest current-protocol-source-bridge-is-current-and-tamper-evident
  (let [bridge sh-crosswalk/readiness-source-version
        r (:protocol-refinement bridge)
        checkpoint (:source-input-commit bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old-source (edn/read-string old-text)
        view (sh-crosswalk/source-view)
        sha (fn [text] (sh-inventory/sha256 text))
        span-text (fn [body {:keys [start-byte end-byte]}]
                    (String. (.getBytes body "UTF-8") (int start-byte)
                              (int (- end-byte start-byte)) "UTF-8"))
        file-records (concat (:task-sources r) (:plan-sources r) (:specification-sources r))
        captured (into {} (map (juxt :id identity)
                               (get-in r [:future-task-source-successor :after])))
        actual (into {} (map (juxt :id identity)
                             (mapcat val (:future-task-authorities view))))
        metadata (fn [row]
                   (assoc (select-keys row [:id :region :component :path :checked :source-commit :file-sha256])
                          :body-sha256 (or (:body-sha256 row) (get-in row [:source :sha256]))))
        expected-owners #{"language/errors/normalize-foreign-failure"
                          "language/protocol/publish-method-contract"
                          "language/protocol/review-stable-change"
                          "language/protocol/select-checked-dispatch"
                          "language/protocol/specialize-dispatch"}]
    (is (= checkpoint (:source-commit r)))
    (is (= checkpoint (get-in bridge [:current-source-checkpoint :commit])))
    (is (= :complete-L8-cross-component-protocol-source-proposal (:kind r)))
    (is (str/includes? (:dependency-review r) "2011"))
    (is (str/includes? (:dependency-review r) "139"))
    (is (str/includes? (:dependency-review r) "99"))
    (is (= [50 89]
           [(get-in r [:task-comparisons :unchanged-body-count])
            (count (get-in r [:task-comparisons :changed-bodies]))]))
    (is (= 40 (count captured) (count actual)))
    (is (= [] (get-in r [:future-task-source-successor :changed-source-binding-task-ids])))
    (is (= #{"specs/domains-workflow/tasks.md" "specs/platform-workflow/tasks.md"
             "specs/release-workflow/tasks.md"}
           (set (get-in r [:future-task-source-successor :changed-whole-file-source-paths]))))
    (is (= 5 (count (:owner-joins r))))
    (is (= expected-owners (set (keys (:owner-joins r)))))
    (is (= 16 (count (:criteria r))))
    (is (= 2 (count (get-in r [:implementation-source-inspection :files]))))
    (is (= :none (get-in r [:implementation-source-inspection :current-execution])))
    (is (= :not-claimed (get-in r [:implementation-source-inspection :whole-compiler-inspection])))
    (is (= :none (get-in r [:implementation-source-inspection :implementation-credit])))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= (:sha256 previous) (sha old-text)))
    (is (= (:projection-sha256 previous) (sha (sh-crosswalk/git-source (:commit previous) (:projection-path previous)))))
    (is (= (:input-fingerprint previous) (:input-fingerprint old-source)))
    (doseq [record file-records
            :let [p (:path record) after (:after record)
                  body (slurp p)
                  committed (sh-crosswalk/git-source (:commit after) p)
                  current (sh-crosswalk/git-source
                           (sh-crosswalk/current-source-commit p (:commit after)) p)]]
      (is (= checkpoint (:commit after)))
      ;; The after record is immutable b886 evidence.  A transitioned path
      ;; has a separately authenticated current applying frame.
      (is (= (:sha256 after) (sha committed)))
      (is (= (sha body) (sha current))))
    (doseq [source (:canonical-sources r)]
      (is (= checkpoint (:source-commit source)))
      (is (= (:sha256 source) (sha (slurp (:path source)))
             (sha (sh-crosswalk/git-source (:source-commit source) (:path source))))))
    (doseq [{:keys [path source-commit sha256]} (get-in r [:implementation-source-inspection :files])]
      (is (= checkpoint source-commit))
      (is (= sha256 (sha (slurp path)) (sha (sh-crosswalk/git-source source-commit path)))))
    (doseq [{:keys [current-witness task-id status]} (:criteria r)
            :let [{:keys [path source-commit sha256 source-span]} current-witness
                  body (slurp path)
                  historical-body (sh-crosswalk/git-source source-commit path)
                  raw (span-text historical-body source-span)
                  current (sh-crosswalk/git-source
                           (sh-crosswalk/current-source-commit path source-commit) path)]]
      (is task-id)
      (is (contains? #{:new-complete-L8-source-criterion-pending-independent-review
                       :existing-exact-predicate-no-task-amendment}
                     status)
          (str "unexpected protocol criterion status for " task-id))
      (is (= checkpoint source-commit))
      ;; Keep the witness's historical source pin and span immutable, then
      ;; authenticate the live body against its current transition frame.
      (is (= sha256 (sha historical-body) (sha (sh-crosswalk/git-source source-commit path))))
      (is (= (sha body) (sha current)))
      (is (= (:sha256 source-span) (sha raw)))
      (is (= (.indexOf body raw) (.lastIndexOf body raw))))
    (doseq [[id row] (:owner-joins r)]
      (is (= id (:id row)))
      (is (= :unassessed (:whole-attached-owner-predicate row)))
      (is (= :none (:implementation-credit row)))
      (is (seq (:criterion-task-ids row))))
    (doseq [[id row] captured]
      (is (= (metadata row) (metadata (actual id)))))
    ;; The current protocol successor is the authenticated source for the
    ;; future rows.  Removing or tampering with it must fail closed rather
    ;; than silently falling back to an unrelated historical view.
    (with-redefs [sh-crosswalk/readiness-source-version
                  (update-in bridge [:protocol-refinement] dissoc :future-task-source-successor)]
      (is (thrown? Exception (sh-crosswalk/source-view))))
    (with-redefs [sh-crosswalk/readiness-source-version
                  (assoc-in bridge [:protocol-refinement :future-task-source-successor :after 0 :body-sha256] "forged")]
      (is (thrown? Exception (sh-crosswalk/source-view))))
    (is (= :two-complete-hosted-files-static-only-no-execution
           (get-in r [:implementation-source-inspection :scope])))))

(deftest current-error-source-bridge-preserves-finite-predicates-and-tamper-evidence
  (let [bridge sh-crosswalk/readiness-source-version
        r (:error-refinement bridge)
        view (sh-crosswalk/source-view)
        captured (into {} (map (juxt :id identity)
                               (get-in r [:future-task-source-successor :after])))
        actual (into {} (map (juxt :id identity)
                             (mapcat val (:future-task-authorities view))))
        metadata (fn [row]
                   (assoc (select-keys row [:id :region :component :path :checked
                                             :source-commit :file-sha256])
                          :body-sha256 (or (:body-sha256 row)
                                           (get-in row [:source :sha256]))))
        retained (:retained-finite-predicates r)]
    (is (= (:source-input-commit bridge) (:source-commit r)))
    (is (= :complete-L9-cross-component-error-source-proposal (:kind r)))
    (is (= 23 (count (:criteria r))))
    (is (= 18 (count (:changed-task-ids r))))
    (is (= [] (:new-task-identities r)))
    (is (= 89 (count (:source-facets r))))
    (is (= 56 (count (:occurrence-cases r))))
    (is (= 14 (count (:section-conjunctions r))))
    (is (= 8 (count (:owner-joins r))))
    (is (= {:context 23 :semantic 65 :unresolved 1}
           (:census-kind-population r)))
    (is (= 40 (count captured) (count actual)))
    (is (= #{:finite-ai :finite-workflow :finite-check :finite-panic
             :finite-foreign :finite-outcome :error-change}
           (set (keys retained))))
    (is (= [1125 1111 995 1229 1809 534 1032]
           (mapv :bytes (map retained [:finite-ai :finite-workflow :finite-check
                                       :finite-panic :finite-foreign :finite-outcome
                                       :error-change]))))
    (doseq [[_ row] retained]
      (is (= :none (:implementation-credit row))))
    (is (= :unassessed (:implementation-acceptance r)))
    (is (= :pending (:independent-current-review r)))
    (doseq [[id row] captured]
      (is (= (metadata row) (metadata (actual id)))))
    (with-redefs [sh-crosswalk/readiness-source-version
                  (assoc-in bridge [:error-refinement :future-task-source-successor
                                    :after 0 :body-sha256] "forged")]
      (is (thrown? Exception (sh-crosswalk/source-view))))))

(let [result (run-tests 'sh-crosswalk-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
