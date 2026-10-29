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

(deftest compiler-domain-record-keeps-existing-routing-positions
  (let [rows (sh-crosswalk/task-records "compiler-api")
        tasks {"compiler-api" rows}
        reordered {"compiler-api" (vec (reverse rows))}]
    (is (= "development-boundaries" (:id (sh-crosswalk/task-ref tasks ["compiler-api" 4]))))
    (is (= "domain-ir-boundaries" (:id (sh-crosswalk/task-ref tasks ["compiler-api" 5]))))
    (is (= (sh-crosswalk/task-ref tasks ["compiler-api" 4])
           (sh-crosswalk/task-ref reordered ["compiler-api" 4])))))

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
      (is (= (:commit after) (get-in view [:applying-source-pins path])))
      (is (= (:sha256 before)
             (sh-inventory/sha256 (sh-crosswalk/git-source (:commit before) path))))
      (is (= (:sha256 after) (sh-inventory/sha256 (slurp path))))
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
            current (edn/read-string (slurp path))]
        (is (= (merge (:standalone-test-owners own) (:standalone-test-owners main)
                      (:new-standalone-owners bridge))
               (:standalone-test-owners current)))))))

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
    (let [domain-successor (get-in view [:mapping-review :domains70-current-composition])
          compiler-successor (get-in view [:mapping-review :compiler-testing85-current-composition])
          successor (or (:source-commit compiler-successor)
                        (:source-commit domain-successor)
                        (get-in view [:mapping-review :historical-criterion-source-composition :source-commit])
                        (get-in view [:mapping-review :family-task-source-composition :source-commit]))
          historical (edn/read-string (slurp "specs/build-workflow/fixtures/yank-task-source.edn"))]
      (is (= (:applying-task-source bridge) (:source-commit historical)))
      (doseq [path ["specs/build-workflow/tasks.md" "docs/component-roadmaps/build.md"]]
        (is (= (or successor (:applying-task-source bridge))
               (get-in view [:applying-source-pins path])))
        (when successor
          (is (= (slurp path) (sh-crosswalk/git-source successor path)))
          (let [old (if (= path (:path historical)) historical (:roadmap-source historical))]
            (is (= path (:path old)))
            (is (= (get-in (first (filter #(= path (:path %)) (:source-files bridge))) [:after :sha256])
                   (:sha256 old) (sh-inventory/sha256 (:text old)))))))
      (doseq [declared-successor (remove nil? [domain-successor compiler-successor])]
        (let [record (edn/read-string (slurp (:record declared-successor)))
              path "specs/build-workflow/tasks.md"
              before (sh-crosswalk/git-source (:applying-source record) path)
              operations (filter #(= path (:path %)) (:operations record))]
          (is (= 1 (count operations)))
          (is (= (get {:guarded-domains70-current-task-composition "19e1120301643581e6a8b55b933ec12643d0ba2f"
                       :guarded-compiler-testing85-current-task-composition "d7ff9de2d916918acb5aeabfa3f2409784a33f2a"}
                      (:kind record))
                 (:source-commit declared-successor)))
          (doseq [op operations]
            (is (= (:applying-before-sha256 op) (sh-inventory/sha256 (:before op))))
            (is (= (:applying-after-sha256 op) (sh-inventory/sha256 (:after op))))
            (is (str/starts-with? (:after op) (str/trimr (:before op))))
            (is (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote (:before op))) before)))))
          (is (= (sh-crosswalk/git-source (:source-commit declared-successor) path)
                 (reduce (fn [text op] (str/replace text (:before op) (:after op))) before operations))))))
    (is (= :pending-source-version-review (get-in view [:mapping-review :status])))
    (is (= :pending-source-version-review (get-in view [:mapping-review :predecessor-review :status])))
    (is (= "c0714e09a510f173d0e69380478beb1a5790b07635283009749612aff9754b57"
           (get-in view [:mapping-review :pkg5-source-review :receipt-sha256])))
    (is (every? #(= :unassessed (:accepted-current-requirement %)) (:requirements view)))))

(let [result (run-tests 'sh-crosswalk-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
