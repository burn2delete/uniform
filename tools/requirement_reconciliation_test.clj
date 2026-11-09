(ns requirement-reconciliation-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is run-tests]] [clojure.string :as str]))
(binding [*command-line-args* nil] (load-file "tools/requirement_reconciliation.clj"))
(require '[requirement-reconciliation :as rr])
(require 'requirement-evidence 'requirement-sources)

(defn open-manifest []
  {:scope-version "fixture-1" :scope-state :open :requirements []
   :source-dispositions [] :aliases [] :scope-decisions [] :scope-amendments []})

(defn declare-sources [manifest sources]
  (assoc manifest :original-documents
         (mapv (fn [[id text]] {:id id :sha256 (rr/sha256 text)}) (sort-by key sources))))

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
    (is (pos? (count (:unresolved-source-occurrences
                     (rr/project (declare-sources (open-manifest) {"L1" text}) {"L1" text})))))))

(defn complete-fixture []
  (let [text "Do one thing.\n" occurrence (first (rr/source-occurrences "X" text))]
    [(declare-sources (assoc (open-manifest) :scope-state :reviewed
            :requirements [{:id "fixture/one-thing" :atomicity :reviewed :nonoverlap :reviewed}]
            :source-dispositions [{:occurrence-id (:id occurrence) :kind :requirement :review-state :reviewed
                                   :requirement-ids ["fixture/one-thing"]}]) {"X" text})
     {"X" text}]))

(deftest unknowns-and-scope-decisions-stay-visible
  (let [[manifest sources] (complete-fixture)]
    (is (= 1 (:denominator (rr/project manifest sources))))
    (is (nil? (:overall-rate (rr/project manifest sources))))
    (is (false? (:denominator-ready? (rr/project (assoc manifest :scope-state :open) sources))))
    (is (thrown? Exception (rr/project manifest (assoc sources "new" "New in-scope obligation.\n"))))
    (is (false? (:denominator-ready? (rr/project (assoc manifest :scope-decisions [{:id "pending" :state :unresolved}]) sources))))
    (is (false? (:denominator-ready? (rr/project (assoc-in manifest [:requirements 0 :atomicity] :unresolved) sources))))
    (is (empty? (:proposed-requirement-ids (rr/project (declare-sources (open-manifest) sources) sources))))))

(deftest duplicate-and-dangling-source-dispositions-reject
  (let [[manifest sources] (complete-fixture)]
    (is (thrown? Exception (rr/project (update manifest :requirements conj (first (:requirements manifest))) sources)))
    (is (thrown? Exception (rr/project (update manifest :source-dispositions conj (first (:source-dispositions manifest))) sources)))
    (is (thrown? Exception (rr/project (assoc-in manifest [:source-dispositions 0 :requirement-ids] ["absent"]) sources)))
    (is (thrown? Exception (rr/project (assoc-in manifest [:source-dispositions 0 :occurrence-id] "absent") sources)))
    (is (thrown? Exception (rr/project (assoc-in manifest [:source-dispositions 0 :kind] :silently-excluded) sources)))))

(deftest declared-source-population-cannot-be-omitted-or-expanded
  (let [[manifest sources] (complete-fixture)
        text "Reviewed context, not another requirement.\n"
        occurrence (first (rr/source-occurrences "Y" text))
        all-sources (assoc sources "Y" text)
        disposition {:occurrence-id (:id occurrence) :kind :nonnormative :review-state :reviewed
                     :rationale "Fixture context has no additional obligation."}
        complete (-> manifest (declare-sources all-sources)
                     (update :source-dispositions conj disposition))]
    (is (= 1 (:denominator (rr/project complete all-sources))))
    ;; Removing both text and its disposition must not hide a declared source.
    (is (thrown? Exception (rr/project (assoc complete :source-dispositions (:source-dispositions manifest)) sources)))
    ;; A fully reviewed extra disposition cannot silently expand declared scope.
    (is (thrown? Exception (rr/project (update manifest :source-dispositions conj disposition) all-sources)))
    (is (thrown? Exception (rr/project (dissoc manifest :original-documents) sources)))
    (is (thrown? Exception (rr/project (update manifest :original-documents conj (first (:original-documents manifest))) sources)))))

(deftest canonical-requirement-ids-must-be-nonblank-strings
  (let [[manifest sources] (complete-fixture)]
    (doseq [id [nil "" " " 17 :not-a-semantic-id]]
      (let [malformed (-> manifest (assoc-in [:requirements 0 :id] id)
                          (assoc-in [:source-dispositions 0 :requirement-ids] [id]))]
        (is (thrown? Exception (rr/project malformed sources)))))))

(deftest accepted-scope-decisions-require-unique-source-bound-authority
  (let [[manifest sources] (complete-fixture)
        occurrence (first (rr/source-occurrences "X" (get sources "X")))
        decision {:id "scope/fixture" :state :accepted :scope-version "fixture-1"
                  :authority "Fixture scope review" :reason "Retain the fixture requirement."
                  :source-id "X" :authority-source-occurrence-id (:id occurrence)
                  :authority-sha256 (:raw-sha256 occurrence)}
        approved (assoc manifest :scope-decisions [decision])]
    (is (= 1 (:denominator (rr/project approved sources))))
    (is (thrown? Exception (rr/project (assoc manifest :scope-decisions [{:id "scope/fixture" :state :accepted}]) sources)))
    (is (thrown? Exception (rr/project (update approved :scope-decisions conj decision) sources)))
    (is (thrown? Exception (rr/project (assoc manifest :scope-decisions [{:id "pending" :state :unresolved}
                                                                      {:id "pending" :state :unresolved}]) sources)))
    (is (thrown? Exception (rr/project (dissoc approved :scope-version) sources)))
    (doseq [field [:id :scope-version :authority :reason :authority-source-occurrence-id]]
      (is (thrown? Exception (rr/project (update-in approved [:scope-decisions 0] dissoc field) sources))))
    (doseq [[field value] [[:scope-version "other"] [:authority ""] [:authority 17] [:reason ""]
                          [:authority-source-occurrence-id "missing"] [:source-id "other"]
                          [:authority-sha256 "wrong"]]]
      (is (thrown? Exception (rr/project (assoc-in approved [:scope-decisions 0 field] value) sources))))))

(deftest aliases-and-amendments-cannot-shrink-scope-silently
  (let [[manifest sources] (complete-fixture)]
    (is (thrown? Exception (rr/project (assoc manifest :aliases [{:id "a" :target "b"} {:id "b" :target "a"}]) sources)))
    (is (thrown? Exception (rr/project (assoc manifest :aliases [{:id "a" :target "absent"}]) sources)))
    (is (= 1 (:denominator (rr/project (assoc manifest :aliases [{:id "view-1" :target "fixture/one-thing" :review-state :reviewed}]) sources))))
    (is (false? (:denominator-ready? (rr/project (assoc manifest :aliases [{:id "view-1" :target "fixture/one-thing"}]) sources))))
    (is (false? (:denominator-ready? (rr/project (assoc manifest :aliases [{:id "view-1" :target "fixture/one-thing" :review-state :proposed}]) sources))))
    (is (thrown? Exception (rr/validate-amendment! {:previous-ids ["a"] :active-ids [] :added [] :removed [] :authority "review" :reason "scope"})))
    (is (thrown? Exception (rr/validate-amendment! {:previous-ids ["a"] :active-ids [] :added [] :removed ["a"]})))
    (is (nil? (rr/validate-amendment! {:previous-ids ["a"] :active-ids ["b"] :added ["b"] :removed ["a"] :authority "review" :reason "versioned split"})))))

(deftest orphan-and-detached-amendment-regressions
  (let [[manifest sources] (complete-fixture)
        amendment {:from-version "fixture-0" :to-version "fixture-1"
                   :previous-ids ["old"] :active-ids ["fixture/one-thing"]
                   :added ["fixture/one-thing"] :removed ["old"]
                   :authority "fixture review" :reason "explicit replacement"}
        amended (assoc manifest :prior-scope {:version "fixture-0" :active-ids ["old"]
                                             :review-reference "fixture-reviewed-scope"}
                                :scope-amendments [amendment])]
    (is (thrown? Exception (rr/project (update manifest :requirements conj
                                             {:id "no-source" :atomicity :reviewed :nonoverlap :reviewed}) sources)))
    (is (thrown? Exception (rr/project (assoc manifest :scope-amendments [amendment]) sources)))
    (is (= 1 (:denominator (rr/project amended sources))))
    (is (thrown? Exception (rr/project (assoc-in amended [:scope-amendments 0 :to-version] "unrelated") sources)))
    (is (thrown? Exception (rr/project (assoc-in amended [:prior-scope :active-ids] ["other"]) sources)))
    (is (thrown? Exception (rr/project (-> amended
                                         (assoc-in [:scope-amendments 0 :active-ids] ["unrelated"])
                                         (assoc-in [:scope-amendments 0 :added] ["unrelated"])) sources)))))

(deftest supersession-requires-bound-authority-and-lineage
  (let [[manifest sources] (complete-fixture)
        sources (assoc sources "legacy" "Old project quota.\n" "policy" "Remove project quotas.\n")
        old-id (:id (first (rr/source-occurrences "legacy" (get sources "legacy"))))
        authority-id (:id (first (rr/source-occurrences "policy" (get sources "policy"))))
        decision {:id "removal" :state :accepted :scope-version "fixture-1"
                  :authority "fixture standing policy" :reason "obsolete work quota"
                  :authority-source-occurrence-id authority-id :source-occurrence-ids [old-id]
                  :lineage {:superseded-source-occurrence-id old-id
                            :authority-source-occurrence-id authority-id :scope-version "fixture-1"}}
        proposed (-> manifest
                     (assoc :scope-decisions [decision])
                     (update :source-dispositions into
                             [{:occurrence-id old-id :kind :superseded :review-state :reviewed :decision-id "removal"}
                              {:occurrence-id authority-id :kind :nonnormative :review-state :reviewed
                               :rationale "Fixture policy authority; implementation scope is represented separately."}])
                     (declare-sources sources))]
    (is (= 1 (:denominator (rr/project proposed sources))))
    (doseq [field [:authority :reason :scope-version :lineage :authority-source-occurrence-id :source-occurrence-ids]]
      (is (thrown? Exception (rr/project (update-in proposed [:scope-decisions 0] dissoc field) sources))))
    (is (thrown? Exception (rr/project (assoc proposed :scope-decisions [{:id "removal" :state :accepted}]) sources)))))

(deftest actual-source-census-has-no-requirement-or-dashboard-authority
  (let [report (rr/audit ".")]
    (is (= 245 (:source-documents report)))
    (is (= 240 (count (filter #(re-matches #"[A-Z]+[0-9]+" %) (keys (:source-digests report))))))
    (is (false? (:denominator-ready? report)))
    (is (nil? (:denominator report)))
    (is (nil? (:overall-rate report)))
    (is (seq (:unresolved-source-occurrences report)))
    (is (some #{:no-dashboard-write} (:limits report)))))

(deftest task-source-bridges-bind-history-current-main-and-changed-records
  (let [manifest (edn/read-string (slurp "contracts/requirement-reconciliation.edn"))
        predecessor-text (requirement-sources/git-source
                          "." "1e683892ceb55126cc991b54f8f84981ed541d4d"
                          rr/manifest-path rr/require!)
        predecessor (edn/read-string predecessor-text)
        bridges (vec (rr/validate-task-source-versions! "." manifest))
        predecessor-bridges (vec (:task-source-version-bridges predecessor))
        current-bridges (vec (:task-source-version-bridges manifest))
        predecessor-history (vec (:source-view-review-history predecessor))
        current-history (vec (:source-view-review-history manifest))
        added-bridges (subvec current-bridges (count predecessor-bridges))
        successor (first added-bridges)
        predecessor-by-path (into {} (map (juxt :path identity) predecessor-bridges))
        by-path (into {} (map (juxt :path identity) bridges))
        path-index (fn [path] (.indexOf (mapv :path bridges) path))
        refs (mapcat (fn [requirement]
                       (concat (when-let [task (:task-reference requirement)] [task])
                               (:additional-task-interfaces requirement)))
                     (:requirements manifest))
        predecessor-refs (mapcat (fn [requirement]
                                   (concat (when-let [task (:task-reference requirement)] [task])
                                           (:additional-task-interfaces requirement)))
                                 (:requirements predecessor))
        historical-match-count
        (fn [bridge-rows reference-rows]
          (let [rows-by-path (into {} (map (juxt :path identity) bridge-rows))]
            (count (filter #(= (:sha256 %)
                               (:historical-sha256 (get rows-by-path (:path %))))
                           reference-rows))))]
    (is (= "5dfe5011bc8d73a37b55986628741ecf6b9219705b45ce8e0da32a05af86b359"
           (rr/sha256 predecessor-text)))
    (is (= (inc (count predecessor-bridges)) (count bridges)))
    (is (= predecessor-bridges
           (subvec current-bridges 0 (count predecessor-bridges))))
    (is (= 1 (count added-bridges)))
    (is (= (set (map :change-class predecessor-bridges))
           (set (map :change-class bridges))))
    (is (= (historical-match-count predecessor-bridges predecessor-refs)
           (historical-match-count bridges refs)))
    (is (= {:path "specs/evolution-workflow/tasks.md"
            :marker "evolution-implementation-tasks"
            :historical-ref "16c0f407a4b7923289808bea65d4f78d5a808b54"
            :historical-tree "99cb19ded0ec128a3b44a72316261bb112ed5329"
            :historical-sha256 "4fd5e1a6f080ab51fea0a4c1943a4089c233981a91f02c896103d6eba69ada31"
            :change-class :qualified-current-task-source-successor
            :current-ref "89849c8617f3215c49b39d63abe29e61f8a26b46"
            :current-tree "d54d92aba27156a921e388eaed6a5d1a7b1d8df7"
            :current-sha256 "65f9d349c92bf8fda7666c8d50d4be3e3b34c5d1e7a60297ae1eff5d806e37dd"
            :predecessor-bridge nil}
           (select-keys successor [:path :marker :historical-ref :historical-tree
                                   :historical-sha256 :change-class :current-ref
                                   :current-tree :current-sha256 :predecessor-bridge])))
    (is (= [] (:current-only-records successor)))
    (is (= 10 (count (:changed-records successor))))
    (is (= #{"evolution/change-review"
             "evolution/experiment-lifecycle"
             "evolution/library-admission"
             "evolution/namespace-decision"
             "evolution/registry-yank"
             "evolution/rfc-review"
             "evolution/security-response"
             "evolution/security-review"
             "evolution/stabilization-decision"
             "evolution/target-support-admission"}
           (set (map :id (:changed-records successor)))))
    (is (= (set (map :id (:changed-records successor)))
           (set (map :id (:previously-declared-record-observations successor)))))
    (is (= (inc (count predecessor-history)) (count current-history)))
    (is (= predecessor-history
           (subvec current-history 0 (count predecessor-history))))
    (let [frame (last current-history)]
      (is (= {:kind :qualified-task-source-version-transition-v1
              :source-commit "1e683892ceb55126cc991b54f8f84981ed541d4d"
              :source-tree "1725310700c6f89c4e574961ad9cdab23a14ce0e"
              :manifest-sha256 "5dfe5011bc8d73a37b55986628741ecf6b9219705b45ce8e0da32a05af86b359"
              :prior-task-source-version-bridges predecessor-bridges}
             (select-keys frame [:kind :source-commit :source-tree :manifest-sha256
                                 :prior-task-source-version-bridges])))
      (is (= "Append-only successor from the authenticated Source1e model to the immutable Main898 current task snapshot. All prior bridges, source-view history, requirement records, statuses, checkboxes and nonclaims remain historical and unchanged."
             (:qualification frame))))
    (doseq [path ["specs/stdlib-workflow/tasks.md"
                  "specs/types-workflow/tasks.md"
                  "specs/runtime-workflow/tasks.md"
                  "specs/domains-workflow/tasks.md"]]
      (is (= (select-keys (get predecessor-by-path path)
                          [:change-class :current-ref :current-tree :current-sha256
                           :known-current-main])
             (select-keys (get by-path path)
                          [:change-class :current-ref :current-tree :current-sha256
                           :known-current-main]))))
    (doseq [path ["specs/compiler-api-workflow/tasks.md"
                  "specs/reader-workflow/tasks.md"]]
      (is (= (select-keys (get predecessor-by-path path)
                          [:changed-records :current-only-records])
             (select-keys (get by-path path)
                          [:changed-records :current-only-records]))))
    ;; Keep the three existing corruption negatives unchanged.
    (is (thrown? Exception
                 (rr/validate-task-source-versions!
                  "." (assoc-in manifest [:task-source-version-bridges
                                           (path-index "specs/stdlib-workflow/tasks.md")
                                           :current-sha256]
                                (apply str (repeat 64 "0"))))))
    (is (thrown? Exception
                 (rr/validate-task-source-versions!
                  "." (assoc-in manifest [:task-source-version-bridges
                                           (path-index "specs/compiler-api-workflow/tasks.md")
                                           :changed-records 0
                                           :current-record-sha256] "wrong"))))
    (is (thrown? Exception
                 (rr/validate-task-source-versions!
                  "." (assoc-in manifest [:task-source-version-bridges
                                           (path-index "specs/compiler-api-workflow/tasks.md")
                                           :current-only-records 0
                                           :current-record-sha256] "wrong"))))))

(deftest proposed-source-disposition-is-not-reviewed-closure
  (let [[manifest sources] (complete-fixture)
        proposed (assoc-in manifest [:source-dispositions 0 :review-state] :proposed)]
    (is (false? (:denominator-ready? (rr/project proposed sources))))
    (is (= 1 (count (:unresolved-source-occurrences (rr/project proposed sources)))))))

(deftest only-explicit-reviewed-source-dispositions-close-scope
  (let [[manifest sources] (complete-fixture)
        occurrence-id (get-in manifest [:source-dispositions 0 :occurrence-id])]
    (doseq [state [nil :unreviewed :rejected :unknown :accepted :proposed]]
      (let [candidate (if (nil? state)
                        (update-in manifest [:source-dispositions 0] dissoc :review-state)
                        (assoc-in manifest [:source-dispositions 0 :review-state] state))
            report (rr/project candidate sources)]
        (is (false? (:denominator-ready? report)))
        (is (nil? (:denominator report)))
        (is (= 0 (get-in report [:source-dispositions :reviewed])))
        (is (= [occurrence-id] (:unresolved-source-occurrences report)))))))

(deftest canonical-task-region-controls-the-projection
  (let [record "- [ ] `reader/identity` Identity\n\nCurrent evidence/status: Partial; fixture only.\n\nDone when (yes/no): Declared roots determine identity."
        text (str "- [x] `reader/identity` historical checkbox\n<!-- active:start -->\n\n" record "\n<!-- active:end -->")]
    (is (= "Partial; fixture only." (:status (requirement-evidence/task-record text "active" "reader/identity"))))
    (is (false? (:checked? (requirement-evidence/task-record text "active" "reader/identity"))))
    (is (thrown? Exception (requirement-evidence/task-record text "missing" "reader/identity")))
    (is (thrown? Exception (requirement-evidence/task-record (str/replace text record (str record "\n\n" record)) "active" "reader/identity")))
    (is (thrown? Exception (requirement-evidence/task-record (str/replace text "Current evidence/status: Partial;" "Current evidence/status: Accepted;") "active" "reader/identity")))))

(deftest adjacent-task-records-have-independent-status-and-uniqueness
  (let [accepted "- [x] `first` First\nCurrent evidence/status: Accepted; exact first receipt.\nDone when (yes/no): First boundary."
        partial "- [ ] `second` Second\nCurrent evidence/status: Partial; second fixture only.\nDone when (yes/no): Second boundary."
        wrap #(str "<!-- active:start -->\n" % "\n<!-- active:end -->")
        second-task (requirement-evidence/task-record (wrap (str accepted "\n" partial)) "active" "second")]
    (is (false? (:checked? second-task)))
    (is (= "Partial; second fixture only." (:status second-task)))
    (is (= "Second boundary." (:acceptance second-task)))
    (is (thrown? Exception (requirement-evidence/task-record (wrap (str partial "\n" partial)) "active" "second")))))

(deftest historical-facets-never-upgrade-canonical-acceptance
  (let [task "<!-- active:start -->\n\n- [ ] `reader/identity` Identity\n\nCurrent evidence/status: Partial; fixture only.\n\nDone when (yes/no): All supplied paths.\n<!-- active:end -->"
        row "| 2026-07-02 | Recorder | `P18-T00` | proof.edn | fixture only |"
        ledger (str "## Evidence Ledger\n" row "\n")
        files {"task.md" task "proof.edn" "{:fixture {:path-preserved? true}}" "ledger.md" ledger}
        support {:facet "paired paths" :receipt {:path "proof.edn"}
                 :selector [:fixture :path-preserved?] :expected true
                 :ledger {:path "ledger.md" :date "2026-07-02" :task-id "P18-T00" :line-sha256 (rr/sha256 row)}
                 :verification-date "2026-07-02" :source-baseline (apply str (repeat 40 "a"))}
        requirement {:id "source/provenance" :task-reference {:path "task.md" :marker "active" :id "reader/identity"}
                     :historical-support [support]}
        baseline (apply str (repeat 40 "a"))
        project #(requirement-evidence/project % (fn [pin] (get files (:path pin))) rr/sha256 baseline)
        report (project requirement)]
    (is (= :unassessed (:current-acceptance report)))
    (is (= 1 (count (:historical-support report))))
    (is (false? (get-in report [:canonical-task :checked?])))
    (is (thrown? Exception (project (assoc-in requirement [:historical-support 0 :selector] [:unsupported]))))
    (is (thrown? Exception (project (assoc-in requirement [:historical-support 0 :verification-date] "2026-07-03"))))
    (is (thrown? Exception (project (assoc-in requirement [:historical-support 0 :source-baseline] (apply str (repeat 40 "0"))))))
    (is (thrown? Exception (requirement-evidence/project requirement
                            (fn [pin] (if (= "ledger.md" (:path pin)) (str row "\n## Evidence Ledger\n")
                                          (get files (:path pin)))) rr/sha256 baseline)))))

(deftest existing-canonical-task-labels-remain-source-authority
  (doseq [boundary ["Closing evidence" "Concrete exit evidence"]]
    (let [text (str "<!-- active:start -->\n- [ ] `task` Task\nStatus: Open\n" boundary ": Actual boundary\n<!-- active:end -->")]
      (is (= "Open" (:status (requirement-evidence/task-record text "active" "task"))))
      (is (= "Actual boundary" (:acceptance (requirement-evidence/task-record text "active" "task"))))
      (is (thrown? Exception (requirement-evidence/task-record
                              (str/replace text "Status: Open" "Status: Open\nCurrent evidence/status: Partial") "active" "task"))))))

(deftest missing-task-interface-is-explicit-and-not-accepted
  (let [requirement {:id "unmapped/behavior" :task-gap "No stable implementation ID located."}
        result (requirement-evidence/project requirement (fn [_] (throw (Exception. "Must not read absent task"))) rr/sha256 (apply str (repeat 40 "a")))]
    (is (= :missing (get-in result [:canonical-task :state])))
    (is (= :unassessed (:current-acceptance result)))
    (is (thrown? Exception (requirement-evidence/project (dissoc requirement :task-gap) identity rr/sha256 (apply str (repeat 40 "a")))))))

(deftest exact-facet-partitions-and-pending-semantics
  (let [[manifest sources] (complete-fixture)
        text (get sources "X") n (count text)
        o (first (rr/source-occurrences "X" text))
        facet (fn [id start end]
                {:id id :source-occurrence-id (:id o) :start-byte start :end-byte end
                 :start-line 1 :end-line 1 :raw-sha256 (rr/sha256 (subs text start end))
                 :unit-ids ["fixture/one-thing"]})
        m (-> manifest
              (assoc :source-facets [(facet "f1" 0 3) (facet "f2" 3 n)])
              (assoc-in [:requirements 0 :source-facet-ids] ["f1" "f2"])
              (assoc-in [:source-dispositions 0 :facet-ids] ["f1" "f2"]))]
    (is (= 1 (:denominator (rr/project m sources))))
    (is (= 2 (:source-facet-count (rr/project m sources))))
    (is (false? (:denominator-ready? (rr/project (assoc-in m [:source-facets 1 :unresolved] "Mixed source policy") sources))))
    (doseq [bad [(assoc-in m [:source-facets 0 :raw-sha256] "wrong")
                 (assoc-in m [:source-facets 0 :unit-ids] ["absent"])
                 (assoc-in m [:source-facets 0] (facet "f1" 0 2))
                 (assoc-in m [:source-facets 0] (facet "f1" 0 4))
                 (assoc-in m [:source-facets 1 :start-line] 2)
                 (assoc-in m [:source-facets 1 :id] "f1")
                 (assoc-in m [:source-dispositions 0 :facet-ids] ["f1"])
                 (assoc-in m [:requirements 0 :source-facet-ids] ["absent"])]]
      (is (thrown? Exception (rr/project bad sources))))))

(deftest additional-task-interfaces-are-checked-not-silently-dropped
  (let [text "<!-- active:start -->\n- [ ] `one` One\nStatus: Open\nClosing evidence: First boundary.\n- [ ] `two` Two\nStatus: Open\nClosing evidence: Second boundary.\n<!-- active:end -->"
        requirement {:id "two-consumers" :task-reference {:path "tasks" :marker "active" :id "one"}
                     :additional-task-interfaces [{:path "tasks" :marker "active" :id "two"}]}
        project #(requirement-evidence/project % (fn [_] text) rr/sha256 (apply str (repeat 40 "a")))
        result (project requirement)]
    (is (= "one" (get-in result [:canonical-task :id])))
    (is (= "two" (get-in result [:additional-task-interfaces 0 :id])))
    (is (thrown? Exception (project (assoc-in requirement [:additional-task-interfaces 0 :id] "missing"))))))

(deftest source-and-facet-relations-must-agree-in-both-directions
  (let [sources {"A" "One.\n" "B" "Two.\n"}
        os (mapv #(first (rr/source-occurrences % (get sources %))) ["A" "B"])
        facet (fn [id o unit] {:id id :source-occurrence-id (:id o) :start-byte 0 :end-byte (:end-byte o)
                               :start-line 1 :end-line 1 :raw-sha256 (:raw-sha256 o) :unit-ids [unit]})
        manifest (declare-sources (assoc (open-manifest) :scope-state :reviewed
                        :requirements [{:id "one" :atomicity :reviewed :nonoverlap :reviewed :source-facet-ids ["fa"]}
                                       {:id "two" :atomicity :reviewed :nonoverlap :reviewed :source-facet-ids ["fb"]}]
                        :source-facets [(facet "fa" (first os) "one") (facet "fb" (second os) "two")]
                        :source-dispositions [{:occurrence-id (:id (first os)) :kind :requirement :review-state :reviewed :requirement-ids ["one"] :facet-ids ["fa"]}
                                              {:occurrence-id (:id (second os)) :kind :requirement :review-state :reviewed :requirement-ids ["two"] :facet-ids ["fb"]}]) sources)]
    (is (= 2 (:denominator (rr/project manifest sources))))
    (is (thrown? Exception (rr/project (update manifest :requirements #(mapv (fn [r] (dissoc r :source-facet-ids)) %)) sources)))
    (is (thrown? Exception (rr/project (-> manifest
                                           (assoc-in [:source-facets 0 :unit-ids] ["two"])
                                           (assoc-in [:source-facets 1 :unit-ids] ["one"])
                                           (assoc-in [:requirements 0 :source-facet-ids] ["fb"])
                                           (assoc-in [:requirements 1 :source-facet-ids] ["fa"])) sources)))))

(deftest grouped-backend-task-preserves-its-entire-boundary
  (let [text (slurp "specs/backends-workflow/tasks.md")
        record (requirement-evidence/task-record text "backends-implementation-tasks" "backend-conformance")]
    (is (= "Open" (:status record)))
    (is (false? (:checked? record)))
    (is (str/includes? (:acceptance record) "unavailable targets honestly open"))
    (is (str/includes? (:acceptance record) "No automatic release closure follows."))
    (is (thrown? Exception (requirement-evidence/task-record
                            (str/replace text "Projected task block:" "Projected task block:\nProjected task block:")
                            "backends-implementation-tasks" "backend-conformance")))
    (is (thrown? Exception (requirement-evidence/task-record
                            (str/replace text "Status: Open" "Status: Open\nClosing evidence: Unrelated boundary")
                            "backends-implementation-tasks" "backend-conformance")))
    (is (thrown? Exception (requirement-evidence/task-record
                            "<!-- m:start -->\n- [ ] `t` Test\nStatus: Open\nProjected task block:\nMalformed remaining boundary\nClosing evidence: Competing replacement\n<!-- m:end -->"
                            "m" "t")))))

(deftest explicit-unresolved-source-is-not-nonnormative-or-ready
  (let [[m sources] (complete-fixture)
        text "An externally owned behavior needs an exact join.\n"
        o (first (rr/source-occurrences "outside" text))
        m (update m :source-dispositions conj {:occurrence-id (:id o) :kind :unresolved
                                               :review-state :reviewed :rationale "Owner interface is not adjudicated."})
        all-sources (assoc sources "outside" text)
        result (rr/project (declare-sources m all-sources) all-sources)]
    (is (false? (:denominator-ready? result)))
    (is (= [(:id o)] (:unresolved-source-occurrences result)))))

(deftest known-obligation-acceptance-gaps-do-not-change-scope
  (let [[m sources] (complete-fixture)
        o (first (rr/source-occurrences "X" (get sources "X")))
        facet {:id "f" :source-occurrence-id (:id o) :start-byte 0 :end-byte (:end-byte o)
               :start-line 1 :end-line 1 :raw-sha256 (:raw-sha256 o) :unit-ids ["fixture/one-thing"]
               :unresolved true :unresolved-category :acceptance-qualification
               :qualification-reason "The known outcome lacks applicable implementation evidence."}
        m (-> m (assoc :source-facets [facet])
              (assoc-in [:requirements 0 :source-facet-ids] ["f"])
              (assoc-in [:source-dispositions 0 :facet-ids] ["f"]))
        result (rr/project m sources)]
    (is (= 1 (:denominator result)))
    (is (= 1 (:acceptance-qualification-facet-count result)))
    (is (nil? (:overall-rate result)))
    (doseq [category [:scope-identity :nonoverlap nil]]
      (is (false? (:denominator-ready? (rr/project (assoc-in m [:source-facets 0 :unresolved-category] category) sources)))))
    (is (thrown? Exception (rr/project (assoc-in m [:source-facets 0 :unresolved-category] :ignore) sources)))
    (is (thrown? Exception (rr/project (update-in m [:source-facets 0] dissoc :qualification-reason) sources)))
    (is (thrown? Exception (rr/project (assoc-in m [:source-facets 0 :unresolved] false) sources)))))

(deftest integrated-api-grouped-records-retain-tables-and-acceptance
  (let [text (slurp "specs/compiler-api-workflow/tasks.md")]
    (doseq [id ["pipeline" "plugins" "pass-evidence"]
            :let [r (requirement-evidence/task-record text "compiler-api-implementation-tasks" id)]]
      (is (= "Open" (:status r)))
      (is (false? (:checked? r)))
      (is (str/includes? (:acceptance r) "| Task | Concrete closing evidence |"))
      (is (str/includes? (:acceptance r) "Yes/no acceptance:")))
    (is (thrown? Exception (requirement-evidence/task-record
                            (str/replace text "Owner role:" "Malformed role:")
                            "compiler-api-implementation-tasks" "pipeline")))))

(deftest exact-facet-supersession-needs-authority-and-review
  (let [[m sources] (complete-fixture) text (get sources "X")
        o (first (rr/source-occurrences "X" text))
        authority-text "Remove project work quotas.\n"
        authority (first (rr/source-occurrences "policy" authority-text))
        sources (assoc sources "policy" authority-text)
        facet (fn [id start end]
                {:id id :source-occurrence-id (:id o) :start-byte start :end-byte end
                 :start-line 1 :end-line 1 :raw-sha256 (rr/sha256 (subs text start end))
                 :unit-ids ["fixture/one-thing"]})
        first-facet (facet "old" 0 3) second-facet (facet "current" 3 (count text))
        decision {:id "removal" :state :accepted :authority "Fixture policy" :source-id "policy"
                  :reason "Retire the source-bound obsolete quota."
                  :scope-version "fixture-1" :authority-source-occurrence-id (:id authority)
                  :authority-sha256 (:raw-sha256 authority)}
        supersession {:decision-id "removal" :scope-version "fixture-1" :review-state :proposed
                      :authority-source-occurrence-id (:id authority) :authority-sha256 (:raw-sha256 authority)
                      :source-facet-id "old" :source-fragment-sha256 (:raw-sha256 first-facet)
                      :reason "Remove only the old quota fragment; retain the other predicate."}
        m (-> m (assoc :scope-decisions [decision]
                       :source-facets [(assoc first-facet :requirement-role :historical-superseded :supersession supersession) second-facet])
              (assoc-in [:requirements 0 :source-facet-ids] ["old" "current"])
              (assoc-in [:source-dispositions 0 :facet-ids] ["old" "current"])
              (update :source-dispositions conj {:occurrence-id (:id authority) :kind :nonnormative :review-state :reviewed
                                                :rationale "Fixture governing policy, not another product requirement."})
              (declare-sources sources))
        reviewed (assoc-in m [:source-facets 0 :supersession :review-state] :reviewed)]
    (is (false? (:denominator-ready? (rr/project m sources))))
    (is (= 1 (:historical-superseded-facet-count (rr/project reviewed sources))))
    (is (= 1 (:denominator (rr/project reviewed sources))))
    (doseq [field [:decision-id :scope-version :authority-source-occurrence-id :authority-sha256
                  :source-facet-id :source-fragment-sha256 :review-state :reason]]
      (is (thrown? Exception (rr/project (update-in reviewed [:source-facets 0 :supersession] dissoc field) sources))))
    (is (thrown? Exception (rr/project (assoc-in reviewed [:scope-decisions 0 :state] :unresolved) sources)))
    (is (thrown? Exception (rr/project (assoc-in reviewed [:source-facets 0 :requirement-role] :ignored) sources)))
    (is (thrown? Exception (rr/project
                            (assoc-in reviewed [:source-facets 1]
                                      (assoc second-facet :requirement-role :historical-superseded
                                             :supersession (assoc supersession :review-state :reviewed
                                                                  :source-facet-id "current"
                                                                  :source-fragment-sha256 (:raw-sha256 second-facet)))) sources)))))

(deftest supersession-cannot-retain-only-layout-or-nonnormative-provenance
  (let [fixture
        (fn [tail kind]
          (let [text (str "Quota." tail) authority-text "Remove project work quotas.\n"
                o (first (rr/source-occurrences "X" text))
                authority (first (rr/source-occurrences "policy" authority-text))
                facet (fn [id start end]
                        {:id id :source-occurrence-id (:id o) :start-byte start :end-byte end
                         :start-line 1 :end-line 1 :raw-sha256 (rr/sha256 (subs text start end)) :unit-ids ["u"]})
                quota (facet "old" 0 6) remaining (assoc (facet "remaining" 6 (count text)) :kind kind)
                decision {:id "remove" :state :accepted :authority "Fixture accounting removal" :source-id "policy"
                          :reason "Retire only the obsolete quota facet."
                          :scope-version "v1" :authority-source-occurrence-id (:id authority)
                          :authority-sha256 (:raw-sha256 authority)}
                supersession {:decision-id "remove" :scope-version "v1" :review-state :reviewed
                              :authority-source-occurrence-id (:id authority) :authority-sha256 (:raw-sha256 authority)
                              :source-facet-id "old" :source-fragment-sha256 (:raw-sha256 quota) :reason "Remove the old quota."}]
            [(declare-sources {:scope-version "v1" :scope-state :reviewed :scope-decisions [decision] :aliases []
              :requirements [{:id "u" :atomicity :reviewed :nonoverlap :reviewed :source-facet-ids ["old" "remaining"]}]
              :source-dispositions [{:occurrence-id (:id o) :kind :requirement :review-state :reviewed :requirement-ids ["u"] :facet-ids ["old" "remaining"]}
                                    {:occurrence-id (:id authority) :kind :nonnormative :review-state :reviewed :rationale "Governing policy."}]
              :source-facets [(assoc quota :requirement-role :historical-superseded :supersession supersession) remaining]}
                              {"X" text "policy" authority-text})
             {"X" text "policy" authority-text}]))]
    (doseq [[tail kind] [["\n" :layout] ["\n" nil] ["\n" :requirement]
                         [" Heading.\n" :layout] [" Commentary.\n" :nonnormative]
                         [" Contents.\n" :navigation] [" Title.\n" :heading]]]
      (let [[m sources] (fixture tail kind)]
        (is (thrown-with-msg? Exception #"no substantive current provenance" (rr/project m sources)))))
    (let [[m sources] (fixture " Validate actual member types.\n" :requirement)]
      (is (= 1 (:denominator (rr/project m sources))))
      (is (= 1 (:historical-superseded-facet-count (rr/project m sources))))
      (is (thrown-with-msg? Exception #"no substantive current provenance"
                           (rr/project (assoc-in m [:source-facets 1 :normative?] false) sources))))))

(deftest layout-transfer-is-exact-and-snapshot-bound
  (let [base "# Platform\n\nProjects.\n\nProduct.\n"
        live (str "# Platform\n\n" requirement-sources/platform-authority
                  "<!-- platform-task-projection:start -->\nProjects.\n<!-- platform-task-projection:end -->\n\n"
                  "<!-- platform-product-projection:start -->\nProduct.\n<!-- platform-product-projection:end -->\n")
        baseline (apply str (repeat 40 "a")) applying (apply str (repeat 40 "b"))
        doc {:id "platform-product" :path "docs/component-roadmaps/platform.md" :sha256 (rr/sha256 base)}
        b {:document-id "platform-product" :kind :platform-task-authority-layout-only
           :baseline-ref baseline :applying-ref applying :baseline-sha256 (rr/sha256 base)
           :live-sha256 (rr/sha256 live) :review-state :proposed}
        m {:source-baseline baseline :extension-sources [doc] :source-version-bridges [b]}
        view #(requirement-sources/view %1 doc %2 rr/sha256 rr/require!)
        v (view m live) receipt (:bridge v)
        snapshots {baseline base applying live}
        read-ref (fn [ref _] (get snapshots ref))]
    (is (= base (:text v)))
    (is (= :unverified (:snapshot-provenance receipt)))
    (is (= :git-bytes-verified (:snapshot-provenance (requirement-sources/validate-snapshots! receipt read-ref rr/sha256 rr/require!))))
    (is (thrown? Exception (requirement-sources/validate-snapshots! (assoc receipt :applying-ref (apply str (repeat 40 "0"))) read-ref rr/sha256 rr/require!)))
    (doseq [changed [(str/replace live "Product." "Changed product.")
                     (str/replace live "<!-- platform-product-projection:end -->\n" "")
                     (str live requirement-sources/platform-authority)]]
      (is (thrown? Exception (view (assoc-in m [:source-version-bridges 0 :live-sha256] (rr/sha256 changed)) changed))))
    (is (thrown? Exception (view (assoc m :source-version-bridges []) live)))
    (is (thrown? Exception (view (assoc-in m [:source-version-bridges 0 :baseline-ref] applying) live)))
    (is (= base (:text (view m base))))
    (let [[one one-sources] (complete-fixture)
          dispositions (for [o (rr/source-occurrences "platform-product" base)
                             :when (= :source-block (:structural-kind o))]
                         {:occurrence-id (:id o) :kind :nonnormative :review-state :reviewed :rationale "Fixture source context."})
          projected (-> (merge one m) (update :source-dispositions into dispositions))
          sources (assoc one-sources "platform-product" live)]
      (is (false? (:denominator-ready? (rr/project projected sources))))
      (is (= 1 (:denominator (rr/project (assoc-in projected [:source-version-bridges 0 :review-state] :reviewed) sources)))))))

(defn std3-accounting-fixture []
  (let [manifest (edn/read-string (slurp "contracts/requirement-reconciliation.edn"))
        bridge (first (filter #(= requirement-sources/std3-accounting-kind (:kind %))
                              (:source-version-bridges manifest)))
        document (first (filter #(= "STD3" (:id %)) (:original-documents manifest)))
        lineage (get-in manifest [:stdlib-reconciliation-context :accounting-lineage
                                  :mixed-std3-paragraphs])]
    {:scope-version (:scope-version manifest)
     :source-baseline (:source-baseline manifest)
     :original-documents [document]
     :source-version-bridges [bridge]
     :stdlib-reconciliation-context {:accounting-lineage {:mixed-std3-paragraphs lineage}}}))

(defn std3-lineage-digest [manifest]
  (rr/sha256 (pr-str ((var requirement-sources/canonical-value)
                      (get-in manifest [:stdlib-reconciliation-context :accounting-lineage
                                        :mixed-std3-paragraphs])))))

(deftest std3-accounting-projection-is-exact-and-snapshot-bound
  (let [manifest (std3-accounting-fixture)
        document (first (:original-documents manifest))
        live (slurp (:path document))
        baseline (requirement-sources/git-source "."
                                                requirement-sources/std3-accounting-baseline-ref
                                                (:path document) rr/require!)
        view #(requirement-sources/view %1 document %2 rr/sha256 rr/require!
                                        {:baseline-texts {"STD3" %3}})
        projected (view manifest live baseline)
        snapshots {requirement-sources/std3-accounting-baseline-ref baseline
                   requirement-sources/std3-accounting-applying-ref live}
        read-ref (fn [ref path] (when (= path (:path document)) (get snapshots ref)))]
    (is (= baseline (:text projected)))
    (is (= :unverified (get-in projected [:bridge :snapshot-provenance])))
    (is (= :git-bytes-verified
           (:snapshot-provenance
            (requirement-sources/validate-snapshots!
             (get-in projected [:bridge]) read-ref rr/sha256 rr/require!))))
    (is (thrown? Exception
                 (view (assoc-in manifest [:source-version-bridges 0 :applying-ref]
                                 (apply str (repeat 40 "0"))) live baseline)))
    (is (thrown? Exception
                 (view (assoc-in manifest [:source-version-bridges 0 :baseline-sha256]
                                 (apply str (repeat 64 "0"))) live baseline)))
    (is (thrown? Exception
                 (view (assoc-in manifest [:source-version-bridges 0 :review-state] :rejected)
                       live baseline)))
    (is (thrown? Exception
                 (view (assoc-in manifest [:source-version-bridges 0 :authority]
                                 "untrusted authority") live baseline)))
    (is (thrown? Exception
                 (view (assoc-in manifest [:source-version-bridges 0 :qualification]
                                 "blanket quota equivalence") live baseline)))
    (is (thrown? Exception
                 (view (assoc-in manifest [:stdlib-reconciliation-context :accounting-lineage
                                           :mixed-std3-paragraphs 0 :authority-source-occurrence-ids]
                                  ["forged/authority"])
                       live baseline)))
    (is (thrown? Exception
                 (view (assoc-in manifest [:stdlib-reconciliation-context :accounting-lineage
                                           :mixed-std3-paragraphs 1 :rationale]
                                  "forged rationale")
                       live baseline)))
    (is (thrown? Exception
                 (view (assoc-in manifest [:stdlib-reconciliation-context :accounting-lineage
                                           :mixed-std3-paragraphs 2 :specific-fragment-proposal]
                                  {:remove ["untrusted"] :retain []})
                       live baseline)))
    (doseq [[field value]
            [[:authority-source-occurrence-ids ["forged/authority"]]
             [:rationale "forged rationale"]
             [:specific-fragment-proposal {:remove ["untrusted"] :retain []}]]]
      (let [tampered (assoc-in manifest [:stdlib-reconciliation-context :accounting-lineage
                                         :mixed-std3-paragraphs 0 field] value)
            recomputed (assoc-in tampered [:source-version-bridges 0 :lineage-record-digest]
                                 (std3-lineage-digest tampered))]
        (is (thrown? Exception (view recomputed live baseline)))))
    (is (thrown? Exception
                 (view (assoc-in manifest [:source-version-bridges 0 :path]
                                 "docs/phase-16-standard-library/212-std2-core-library-specification.md")
                       live baseline)))
    (is (thrown? Exception
                 (view manifest live (str/replace baseline "account for construction work"
                                                 "account for changed work"))))
    ;; A retained semantic edit in a reviewed region is not an allowed projection.
    (is (thrown? Exception
                 (view manifest (str/replace live "validate the input before"
                                             "skip validation before") baseline)))
    ;; Removing a reviewed region or changing an unlisted region cannot be hidden.
    (is (thrown? Exception
                 (view (update-in manifest [:source-version-bridges 0 :projection-hunks] pop)
                       live baseline)))
    (is (thrown? Exception
                 (view (update-in manifest [:stdlib-reconciliation-context :accounting-lineage
                                            :mixed-std3-paragraphs]
                                   conj {:id "STD3/source/unprojected"})
                       live baseline)))
    (is (thrown? Exception
                 (view manifest (str/replace live "## Map Lookup With An Explicit Default"
                                             "## Changed Map Lookup") baseline)))
    (is (thrown? Exception
                 (requirement-sources/validate-snapshots!
                  (assoc-in (get-in projected [:bridge]) [:live-sha256] (apply str (repeat 64 "0")))
                  read-ref rr/sha256 rr/require!)))
    (is (thrown? Exception
                 (requirement-sources/validate-snapshots!
                  (assoc-in (get-in projected [:bridge]) [:baseline-blob] (apply str (repeat 40 "0")))
                  read-ref rr/sha256 rr/require!)))
    (is (thrown? Exception
                 (requirement-sources/validate-snapshots!
                  (assoc-in (get-in projected [:bridge]) [:applying-blob] (apply str (repeat 40 "0")))
                  read-ref rr/sha256 rr/require!)))
    (is (thrown? Exception
                 (requirement-sources/validate-snapshots!
                  (assoc-in (get-in projected [:bridge]) [:path]
                            "docs/phase-16-standard-library/212-std2-core-library-specification.md")
                  read-ref rr/sha256 rr/require!)))))

(deftest final-transferred-domain-tool-and-platform-records-retain-boundaries
  (let [domains (slurp "specs/domains-workflow/tasks.md")
        tools (slurp "specs/tools-workflow/tasks.md")
        platform (slurp "specs/platform-workflow/tasks.md")]
    (doseq [id ["math" "schema" "query" "workflow" "ai" "gpu" "ui"]
            :let [r (requirement-evidence/task-record domains "domains-implementation-tasks" id)]]
      (is (= "Open" (:status r)))
      (is (false? (:checked? r)))
      (is (str/includes? (:acceptance r) "Yes/no acceptance:")))
    (is (str/includes? (:acceptance (requirement-evidence/task-record tools "tools-implementation-tasks"
                               "tools/finite-service-outcomes-and-remaining-work/format-formatter-maintainer")) "Comments retained?"))
    (is (str/includes? (:acceptance (requirement-evidence/task-record platform "platform-implementation-tasks" "hardware")) "state-machine lowering and simulation"))
    (is (thrown? Exception (requirement-evidence/task-record (str/replace domains "Outcome:" "Malformed outcome:") "domains-implementation-tasks" "math")))))

(defn canonical-policy-fixture [document-id]
  (let [manifest (edn/read-string (slurp rr/manifest-path))
        document (first (filter #(= document-id (:id %)) (:original-documents manifest)))
        bridge (first (filter #(= document-id (:document-id %)) (:source-version-bridges manifest)))
        read-ref #(requirement-sources/git-source "." %1 %2 rr/require!)
        baseline (read-ref (:baseline-ref bridge) (:path bridge))
        live (slurp (:path bridge))
        declared {:source-baseline (:source-baseline manifest)
                  :original-documents [document]
                  :source-facets (vec (filter #(= document-id (:document-id %)) (:source-facets manifest)))
                  :source-version-bridges [bridge]}
        options {:baseline-texts {document-id baseline} :verified-baselines? #{document-id}}]
    {:bridge bridge :baseline baseline :live live :document document :read-ref read-ref :declared declared
     :view (fn [b text]
             (requirement-sources/view (assoc declared :source-version-bridges [b]) document text rr/sha256 rr/require! options))}))

(deftest canonical-policy-current-sources-preserve-baseline-and-explicit-pending-scope
  (doseq [id ["L6" "STD13" "SAFE9"]]
    (let [{:keys [bridge baseline live read-ref view]} (canonical-policy-fixture id)
          authenticated (requirement-sources/validate-snapshots! bridge read-ref rr/sha256 rr/require!)
          result (view bridge live)]
      (is (= :git-bytes-verified (:snapshot-provenance authenticated)))
      (is (= baseline (:text result)))
      (is (= :proposed (get-in result [:bridge :review-state])))
      (is (= (:human-decision bridge) (get-in result [:bridge :human-decision])))
      (is (seq (:semantic-deltas bridge)))
      (is (seq (:current-criteria bridge)))
      (is (every? #(= :open (:recorded-status %)) (:current-criteria bridge)))
      (is (every? #(and (:owning-task %) (:source-commit %) (:span-sha256 %)) (:current-criteria bridge))))))

(deftest canonical-policy-source-authority-hunks-and-current-criteria-reject-forgeries
  (doseq [id ["L6" "STD13" "SAFE9"]]
    (let [{:keys [bridge live read-ref view]} (canonical-policy-fixture id)]
      (doseq [altered [(dissoc bridge :human-decision)
                       (assoc bridge :human-decision :unrestricted-provider-access)
                       (assoc bridge :review-state :reviewed)
                       (assoc bridge :authority "")
                       (assoc bridge :qualification "")
                       (assoc bridge :semantic-deltas [])
                       (assoc-in bridge [:source-review :sha256] (apply str (repeat 64 "0")))
                       (assoc bridge :baseline-ref (:applying-ref bridge))
                       (assoc bridge :baseline-tree (:applying-tree bridge))
                       (assoc bridge :applying-blob (:baseline-blob bridge))
                       (update bridge :projection-hunks pop)
                       (assoc-in bridge [:projection-hunks 0 :facet-ids] ["absent/facet"])
                       (update bridge :current-criteria pop)
                       (assoc-in bridge [:current-criteria 0 :owning-task] "absent/task")
                       (assoc-in bridge [:current-criteria 0 :recorded-status] :checked)
                       (update-in bridge [:current-criteria 0 :start-byte] inc)
                       (assoc-in bridge [:current-criteria 0 :whole-file-sha256] "forged")]]
        (is (thrown? Exception (view altered live)))
        (is (thrown? Exception (requirement-sources/validate-snapshots! altered read-ref rr/sha256 rr/require!))))
      (let [drifted (str live "\nUnlisted additional policy.\n")]
        (is (thrown? Exception (view bridge drifted)))
        (is (thrown? Exception (view (assoc bridge :live-sha256 (rr/sha256 drifted)) drifted))))
      (let [criterion (first (:current-criteria bridge))
            forged-read (fn [ref path]
                          (let [text (read-ref ref path)]
                            (if (and (= ref (:source-commit criterion)) (= path (:path criterion)))
                              (str text "\nChanged after the criterion source freeze.\n") text)))]
        (is (thrown? Exception (requirement-sources/validate-snapshots! bridge forged-read rr/sha256 rr/require!)))))))

(deftest canonical-policy-records-preserve-every-existing-model-value
  (letfn [(records-by-id [model]
            (into {} (map (juxt :id identity) (:requirements model))))
          (changed-fields [before-record current-record]
            (set (for [key (set (concat (keys before-record) (keys current-record)))
                       :when (not= (get before-record key) (get current-record key))]
                   key)))
          (restore-field [record before-record key]
            (if (contains? before-record key)
              (assoc record key (get before-record key))
              (dissoc record key)))
          (restore-reference-fields [record before-record]
            (reduce #(restore-field %1 before-record %2)
                    record [:task-reference :additional-task-interfaces]))]
    (let [legacy (edn/read-string
                  (requirement-sources/git-source
                   "." "67d05c761cad92d5e09f0f8cd2c6e692853887cb"
                   rr/manifest-path rr/require!))
          before-text (requirement-sources/git-source
                       "." "1e683892ceb55126cc991b54f8f84981ed541d4d"
                       rr/manifest-path rr/require!)
          before (edn/read-string before-text)
          current (edn/read-string (slurp rr/manifest-path))
          legacy-policy (vec (:source-version-bridges legacy))
          predecessor-policy (vec (:source-version-bridges before))
          current-policy (vec (:source-version-bridges current))
          before-records (records-by-id before)
          current-records (records-by-id current)
          record-ids (vec (map :id (:requirements before)))
          current-record-ids (vec (map :id (:requirements current)))
          deltas (into {}
                       (for [id record-ids
                             :let [fields (changed-fields (get before-records id)
                                                          (get current-records id))]
                             :when (seq fields)]
                         [id fields]))
          task-reference-changes
          (count (filter #(not= (:task-reference (get before-records %))
                                (:task-reference (get current-records %)))
                         record-ids))
          additional-reference-pairs
          (mapcat (fn [id]
                    (map vector
                         (vec (or (:additional-task-interfaces (get before-records id)) []))
                         (vec (or (:additional-task-interfaces (get current-records id)) []))))
                  record-ids)
          additional-length-pairs
          (map (fn [id]
                 [(count (or (:additional-task-interfaces (get before-records id)) []))
                  (count (or (:additional-task-interfaces (get current-records id)) []))])
               record-ids)
          additional-reference-changes
          (count (filter (fn [[a b]] (not= a b)) additional-reference-pairs))
          predecessor-task-bridges (vec (:task-source-version-bridges before))
          current-task-bridges (vec (:task-source-version-bridges current))
          predecessor-history (vec (:source-view-review-history before))
          current-history (vec (:source-view-review-history current))
          restored-requirements
          (mapv (fn [record]
                  (restore-reference-fields record (get before-records (:id record))))
                (:requirements current))
          restored
          (-> current
              (assoc :requirements restored-requirements)
              (assoc :task-source-version-bridges
                     (subvec current-task-bridges 0 (count predecessor-task-bridges)))
              (assoc :source-view-review-history
                     (subvec current-history 0 (count predecessor-history))))]
      (is (= "5dfe5011bc8d73a37b55986628741ecf6b9219705b45ce8e0da32a05af86b359"
             (rr/sha256 before-text)))
      (is (= (vec (map :id (:requirements before))) current-record-ids))
      (is (= (set (map :id (:requirements before))) (set (keys current-records))))
      (is (= 25 (count deltas)))
      (is (every? #(every? #{:task-reference :additional-task-interfaces} (val %)) deltas))
      (is (every? (fn [[a b]] (= a b)) additional-length-pairs))
      (is (= 25 task-reference-changes))
      (is (= 1 additional-reference-changes))
      (is (= 26 (+ task-reference-changes additional-reference-changes)))
      (is (= (inc (count predecessor-task-bridges)) (count current-task-bridges)))
      (is (= predecessor-task-bridges
             (subvec current-task-bridges 0 (count predecessor-task-bridges))))
      (is (= (inc (count predecessor-history)) (count current-history)))
      (is (= predecessor-history
             (subvec current-history 0 (count predecessor-history))))
      (is (= legacy-policy
             (subvec predecessor-policy 0 (count legacy-policy))))
      (is (= #{"L6" "STD13" "SAFE9"}
             (set (map :document-id
                       (subvec predecessor-policy (count legacy-policy))))))
      ;; SAFE9 is a real Source1e policy addition and remains unchanged in Root512.
      (is (= predecessor-policy current-policy))
      (doseq [key [:source-baseline :original-documents :source-dispositions
                   :bounded-evidence-assessments]]
        (is (= (get before key) (get current key))))
      (is (= :open (:scope-state current)))
      ;; Invert only the authenticated 26-reference transition and the two
      ;; append-only task-source records; then require complete model equality.
      (is (= before restored)))))

(deftest canonical-policy-cli-source-maps-load-declared-baselines
  ;; The global task/evidence audit is a separate current-binding gate. Exercise
  ;; the real CLI branch and actual document/Git inputs while isolating that gate.
  (let [manifest (edn/read-string (slurp rr/manifest-path))
        read-ref #(requirement-sources/git-source "." %1 %2 rr/require!)]
    (doseq [id ["L6" "STD13" "SAFE9" "STD3" "L4" "C4" "MATH4" "MATH9"]]
      (let [source (first (filter #(= id (:id %)) (:original-documents manifest)))
            bridge (first (filter #(= id (:document-id %)) (:source-version-bridges manifest)))
            baseline (read-ref (:baseline-ref bridge) (:path source))
            audit-roots (atom [])
            output (with-out-str
                     (with-redefs [rr/audit (fn [root] (swap! audit-roots conj root) {:scope :source-map-branch-fixture})]
                       (rr/run ["--source-map" id])))
            result (edn/read-string output)]
        (is (= ["."] @audit-roots))
        (is (= id (:document-id result)))
        (is (= (:path source) (:path result)))
        (is (= (:baseline-ref bridge) (:source-ref result)))
        (is (= (rr/source-occurrences id baseline) (:source-occurrences result)))))
    (with-redefs [rr/audit (fn [_] {:scope :source-map-branch-fixture})]
      (is (thrown? Exception (rr/run ["--source-map" "missing-document"])))
      (is (thrown? Exception (rr/run ["--source-map" "L6" "unexpected-argument"]))))))

(def task-metadata-clarification-cases
  (vec (concat
        (for [id ["math" "query" "workflow" "ai" "gpu" "ui"]]
          ["specs/domains-workflow/tasks.md" "domains-implementation-tasks" id])
        (for [id ["hardware" "firmware" "kernel" "devices"]]
          ["specs/platform-workflow/tasks.md" "platform-implementation-tasks" id])
        (for [id ["legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
                  "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
                  "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"]]
          ["specs/legality-workflow/tasks.md" "legality-implementation-tasks" id])
        [["specs/runtime-workflow/tasks.md" "runtime-implementation-tasks"
          "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"]])))

(deftest current-task-metadata-clarification-preserves-every-clause-and-task-state
  ;; The current 29-file source overlay is the authenticated Source1e snapshot.
  ;; Do not normalize headings against the older 09bdb predecessor: that would
  ;; erase legitimate intervening clause evolution.
  (doseq [path (distinct (map first task-metadata-clarification-cases))]
    (let [before (requirement-sources/git-source
                  "." "1e683892ceb55126cc991b54f8f84981ed541d4d"
                  path rr/require!)
          current (slurp path)]
      (is (= before current))))
  (doseq [[path marker id] task-metadata-clarification-cases]
    (let [record (requirement-evidence/task-record (slurp path) marker id)]
      (is (= id (:id record)))
      (is (= "Open" (:status record)))
      (is (false? (:checked? record)))
      (is (not (str/blank? (:acceptance record)))))))

(deftest current-task-metadata-clarification-keeps-authority-ambiguity-rejection
  (doseq [[path marker id] task-metadata-clarification-cases]
    (let [text (slurp path)
          matcher (re-matcher (re-pattern (str "(?m)^- \\[([ x])\\] `" (java.util.regex.Pattern/quote id) "` [^\n]*")) text)]
      (is (.find matcher))
      (let [heading-end (.end matcher) checked-at (.start matcher 1)
            inserted (fn [field] (str (subs text 0 heading-end) "\n" field (subs text heading-end)))
            forged-check (str (subs text 0 checked-at) "x" (subs text (inc checked-at)))]
        (is (thrown? Exception (requirement-evidence/task-record (inserted "Status: Accepted; forged") marker id)))
        (is (thrown? Exception (requirement-evidence/task-record (inserted "Closing evidence: supplied success flag") marker id)))
        (is (thrown? Exception (requirement-evidence/task-record forged-check marker id)))))))


(let [{:keys [fail error]} (run-tests)] (System/exit (if (zero? (+ fail error)) 0 1)))
