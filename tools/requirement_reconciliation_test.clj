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
            :source-dispositions [{:occurrence-id (:id occurrence) :kind :requirement
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
    (is (= 1 (:denominator (rr/project (assoc manifest :aliases [{:id "view-1" :target "fixture/one-thing"}]) sources))))
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
                             [{:occurrence-id old-id :kind :superseded :decision-id "removal"}
                              {:occurrence-id authority-id :kind :nonnormative
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
                        :source-dispositions [{:occurrence-id (:id (first os)) :kind :requirement :requirement-ids ["one"] :facet-ids ["fa"]}
                                              {:occurrence-id (:id (second os)) :kind :requirement :requirement-ids ["two"] :facet-ids ["fb"]}])]
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

(let [{:keys [fail error]} (run-tests)] (System/exit (if (zero? (+ fail error)) 0 1)))
