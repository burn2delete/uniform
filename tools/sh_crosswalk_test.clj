(ns sh-crosswalk-test
  (:require [clojure.test :refer [deftest is run-tests]] [clojure.string :as str] [clojure.edn :as edn]))
(binding [*command-line-args* nil] (load-file "tools/sh_crosswalk.clj"))
(require '[sh-inventory] '[sh-crosswalk] '[acceptance-dependencies])

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
        refs (filter #(some (fn [entry] (= :source-snapshot-ref (key entry))) %)
                     (filter map? (tree-seq coll? seq (dissoc normalized :source-snapshots))))
        snapshots (:source-snapshots normalized)
        recovered (map #(get snapshots (:source-snapshot-ref %)) refs)]
    (is (seq raw-spans))
    (is (= (count (distinct (map :raw raw-spans))) (count snapshots)))
    (is (= (sort (map :raw raw-spans)) (sort recovered)))
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
    (doseq [p (:preserved-history-paths bridge)]
      (is (= (sh-crosswalk/git-source
               (if (= p "specs/sh-family-crosswalk/macro-runtime-source-version.edn")
                 (:main bridge) (:published-candidate bridge)) p)
             (slurp p))))
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
               "specs/sh-family-crosswalk/pr574-conflict-composition-source-version.edn"]]
      (is (= (sh-crosswalk/git-source (:previous-candidate bridge) p) (slurp p))))
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
                 :resolution-main-predecessor-review :resolution-runtime-current-review
                 :resolution-runtime-predecessor-review
                 :implementation-readiness-predecessor-review]]
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
        comparisons (:task-comparisons bridge)]
    (is (= (:sha256 predecessor) (sh-inventory/sha256 prior-text)))
    (is (= (:review predecessor)
           (select-keys (:mapping-review prior) [:status :input-fingerprint :source-version-bridge])))
    (is (= 71 (:unchanged-body-count comparisons)))
    (is (= (set (map :id (:changed-bodies comparisons))) changed))
    (is (= 68 (count changed)))
    (is (= 71 (count (remove changed (keys before)))))
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
      (is (= after-sha256 (sh-inventory/sha256 (raw view (after id)))) id)
      (is (false? before-checked) id)
      (is (false? after-checked) id))
    (doseq [{:keys [path before after]} (:source-files bridge)]
      (when (:commit before)
        (is (= (:sha256 before) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))) path))
      (is (= (:sha256 after) (sh-inventory/sha256 (sh-crosswalk/git-source (:commit after) path))) path)
      (is (= (:sha256 after) (sh-inventory/sha256 (slurp path))) path))
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
    (is (= (select-keys sh-crosswalk/readiness-predecessor-mapping-review
                        [:status :input-fingerprint :source-version-bridge])
           (get-in view [:mapping-review :implementation-readiness-predecessor-review])))
    (is (= (:input-fingerprint bridge) (sh-crosswalk/review-fingerprint (sh-crosswalk/source-view))))
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
      (is (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote clause)) current))) (:id operation)))
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
        text (slurp path)
        restored (reduce (fn [t marker] (str/replace t (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) ""))
                         text (vals (:markers refinement)))
        facets (:source-facets refinement)
        c15 (filterv #(= "C15" (:document-id %)) facets)
        canonical (:canonical-sources refinement)
        source-text (memoize slurp)
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
    (doseq [{:keys [key task-id historical-witness current-witness]} records
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
        bridge sh-crosswalk/readiness-source-version refinement (:incremental-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old-projection (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string old-projection)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        task-map (fn [v] (into {} (for [rs (vals (:task-authorities v)) r rs] [(:id r) r])))
        before (task-map prior) after (task-map view)
        raw (fn [v r] (acceptance-dependencies/source-raw (:source r) (:source-snapshots v)))
        changed (set (for [[id r] before :when (not= (raw prior r) (raw view (after id)))] id))
        text (slurp "specs/build-workflow/tasks.md")
        restored (reduce (fn [body marker] (str/replace body (re-pattern (str "(?s)\n<!-- " marker ":start -->.*?<!-- " marker ":end -->\n\n")) "")) text (vals (:markers refinement)))
        read-span (fn [body span] (String. (.getBytes body "UTF-8") (int (:start-byte span)) (int (- (:end-byte span) (:start-byte span))) "UTF-8"))
        source-text (memoize slurp) records (:criteria refinement) facets (:source-facets refinement)
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
        refinement (:plugin-refinement sh-crosswalk/readiness-source-version)
        previous (:predecessor-readiness-successor refinement)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        review-pin (:retained-source-review refinement)
        review-text (slurp (:path review-pin)) review (edn/read-string review-text)
        canonical (:canonical-source refinement) body (slurp (:path canonical))
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
            :let [text (slurp (:path current-witness)) clause (read-span text (:source-span current-witness))]]
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
        refinement (:domain-ir-refinement sh-crosswalk/readiness-source-version)
        previous (:predecessor-readiness-successor refinement)
        prior-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        prior (edn/read-string prior-text)
        view (if checkpoint (edn/read-string (sh-crosswalk/git-source (:commit checkpoint) (:projection-path checkpoint))) (sh-crosswalk/build))
        review-pin (:retained-source-review refinement)
        review-text (slurp (:path review-pin)) review (edn/read-string review-text)
        canonical (:canonical-source refinement) body (slurp (:path canonical))
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
            :let [text (slurp (:path current-witness)) clause (read-span text (:source-span current-witness))]]
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
          read-dependency (fn [path] (if (= (:path dep) path) (sh-crosswalk/git-source (get-in dep [:after :commit]) path) (slurp path)))
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
      (is (= sha256 (sh-inventory/sha256 (slurp path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
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
      (is (= sha256 (sh-inventory/sha256 (slurp path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
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
  (let [bridge sh-crosswalk/readiness-source-version r (:concurrency-dependency-refinement bridge)
        previous (:predecessor-readiness-successor r)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous)) old (edn/read-string old-text)
        projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous)) prior (edn/read-string projection-text)
        view (sh-crosswalk/build)
        read-text slurp
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
      (is (= sha256 (sh-inventory/sha256 (slurp path)) (sh-inventory/sha256 (sh-crosswalk/git-source source-commit path)))))
    (is (= :declared-interfaces-no-whole-component-or-final-suite-start-gate (:ordinary-construction r)))
    (is (= :valid-unoptimized-verified-MIR-remains-legal-no-task-completion-gate (:program-legality r)))
    (is (= :pending (:independent-current-review r)))
    (is (= :unassessed (:implementation-acceptance r) (:global-source-conjunctions r)))
    (is (= r (get-in view [:implementation-readiness-source-version :concurrency-dependency-refinement])))))


(let [result (run-tests 'sh-crosswalk-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
