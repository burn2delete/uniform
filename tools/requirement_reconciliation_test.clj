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
            :source-dispositions [{:occurrence-id (:id occurrence) :kind :requirement :review-state :reviewed
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
                               :rationale "Fixture policy authority; implementation scope is represented separately."}]))]
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
        manifest (assoc (open-manifest) :scope-state :reviewed
                        :requirements [{:id "one" :atomicity :reviewed :nonoverlap :reviewed :source-facet-ids ["fa"]}
                                       {:id "two" :atomicity :reviewed :nonoverlap :reviewed :source-facet-ids ["fb"]}]
                        :source-facets [(facet "fa" (first os) "one") (facet "fb" (second os) "two")]
                        :source-dispositions [{:occurrence-id (:id (first os)) :kind :requirement :review-state :reviewed :requirement-ids ["one"] :facet-ids ["fa"]}
                                              {:occurrence-id (:id (second os)) :kind :requirement :review-state :reviewed :requirement-ids ["two"] :facet-ids ["fb"]}])]
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
        result (rr/project m (assoc sources "outside" text))]
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
                                                :rationale "Fixture governing policy, not another product requirement."}))
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
                          :scope-version "v1" :authority-source-occurrence-id (:id authority)
                          :authority-sha256 (:raw-sha256 authority)}
                supersession {:decision-id "remove" :scope-version "v1" :review-state :reviewed
                              :authority-source-occurrence-id (:id authority) :authority-sha256 (:raw-sha256 authority)
                              :source-facet-id "old" :source-fragment-sha256 (:raw-sha256 quota) :reason "Remove the old quota."}]
            [{:scope-version "v1" :scope-state :reviewed :scope-decisions [decision] :aliases []
              :requirements [{:id "u" :atomicity :reviewed :nonoverlap :reviewed :source-facet-ids ["old" "remaining"]}]
              :source-dispositions [{:occurrence-id (:id o) :kind :requirement :review-state :reviewed :requirement-ids ["u"] :facet-ids ["old" "remaining"]}
                                    {:occurrence-id (:id authority) :kind :nonnormative :review-state :reviewed :rationale "Governing policy."}]
              :source-facets [(assoc quota :requirement-role :historical-superseded :supersession supersession) remaining]}
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

(let [{:keys [fail error]} (run-tests)] (System/exit (if (zero? (+ fail error)) 0 1)))
