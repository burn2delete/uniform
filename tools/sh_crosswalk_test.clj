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
    (is (= 121 (:unchanged-body-count comparisons)))
    (is (= (set (map :id (:changed-bodies comparisons))) changed))
    (is (= 18 (count changed)))
    (is (= 121 (count (remove changed (keys before)))))
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
  (let [bridge sh-crosswalk/readiness-source-version
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
                  #(if (= path %) (sh-crosswalk/git-source (:commit after) path) (slurp %)))]
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
                          (:source-snapshots view) #(if (= path %) (sh-crosswalk/git-source (:commit after) path) (slurp %)))
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
                  #(if (= path %) (sh-crosswalk/git-source (:commit after) path) (slurp %)))
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
  (let [bridge sh-crosswalk/readiness-source-version
        refinement (:finish-line-refinement bridge)
        previous (:predecessor-readiness-successor refinement)
        old-text (sh-crosswalk/git-source (:commit previous) (:path previous))
        old (edn/read-string old-text)
        old-projection-text (sh-crosswalk/git-source (:commit previous) (:projection-path previous))
        old-projection (edn/read-string old-projection-text)
        view (sh-crosswalk/build)
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

(let [result (run-tests 'sh-crosswalk-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
