(ns sh-crosswalk-test
  (:require [clojure.test :refer [deftest is run-tests]] [clojure.string :as str] [clojure.edn :as edn]))
(binding [*command-line-args* nil] (load-file "tools/sh_crosswalk.clj"))
(require '[sh-inventory] '[sh-crosswalk])

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
      (is (= (get-in source [:after :commit])
             (get-in view [:applying-source-pins "specs/reader-workflow/tasks.md"]))))))

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
               (get-in view [:applying-source-pins path]))))
      (is (= (:sha256 before)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
      (when-not (= path "docs/self-hosting-slice-ownership.edn")
        (is (= (:sha256 after) (sh-inventory/sha256 (slurp path)))))
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
      (is (= (:applying-task-source bridge) (get-in view [:applying-source-pins path]))))
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
        current (edn/read-string (slurp path))
        remove-entries (fn [record entries]
                         (reduce-kv (fn [r section paths]
                                      (update r section #(apply dissoc % (if (string? paths) [paths] paths)))) record entries))
        main (edn/read-string (sh-crosswalk/git-source (:commit before) path))
        resolution (edn/read-string (sh-crosswalk/git-source (get-in bridge [:resolution-source-ownership :commit]) path))
        view (sh-crosswalk/build)
        ignored [:input-fingerprint :source-version-bridge :qualification
                 :resolution-main-predecessor-review :resolution-runtime-current-review
                 :resolution-runtime-predecessor-review]]
    (is (= main (remove-entries current (:added-entries bridge))))
    (is (= resolution (remove-entries current (:native-entries bridge))))
    (is (= (:sha256 after) (sh-inventory/sha256 (slurp path))))
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
           (get-in view [:mapping-review :source-version-bridge])))
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

(let [result (run-tests 'sh-crosswalk-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
