(ns requirement-current-task-sources-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.string :as str]))

(binding [*command-line-args* nil] (load-file "tools/requirement_reconciliation.clj"))
(require '[requirement-current-task-sources :as current]
         '[requirement-reconciliation :as rr]
         '[requirement-evidence :as evidence]
         '[requirement-sources :as sources])

(def checkpoint "372c32196e8b54dea9e71c8d1d99415b3e67d063")
(def path "specs/evolution-workflow/tasks.md")
(def marker "evolution-implementation-tasks")
(def task-id "evolution/change-review")
(def read-ref (memoize #(sources/git-source "." %1 %2 rr/require!)))
(def snapshot (read-ref checkpoint path))
(def record (evidence/task-record snapshot marker task-id))
(def tree (rr/git-tree "." checkpoint rr/require!))
(def token "Criterion governance/compatibility-assessment:")
(def start (str/index-of snapshot token))
(def end (str/index-of snapshot "\n\n" (+ start (count token))))
(def criterion {:start-byte (alength (.getBytes (subs snapshot 0 start) "UTF-8"))
                :end-byte (alength (.getBytes (subs snapshot 0 end) "UTF-8"))
                :sha256 (rr/sha256 (subs snapshot start end))})
(def reference {:path path :marker marker :id task-id :source-view current/current-view
                :source-commit checkpoint :source-tree tree :sha256 (rr/sha256 snapshot)
                :source-record-sha256 (rr/task-record-digest record rr/sha256)
                :observed-status (:status record) :observed-checked? (:checked? record)
                :criterion-source criterion :detailed-acceptance :unassessed
                :qualification "Pinned source fixture only; no implementation or current evidence acceptance."})
(def readers {:read-ref read-ref :read-commit-tree #(sources/git-commit-tree "." % rr/require!)
              :read-current #(if (= path %) snapshot (throw (ex-info "Unexpected fixture path" {})))
              :task-record evidence/task-record :sha256 rr/sha256 :require! rr/require!})
(def bridge {:path path :marker marker :current-ref checkpoint :current-tree tree
             :current-sha256 (rr/sha256 snapshot) :historical-sha256 (rr/sha256 "historical fixture")})

(deftest actual-git-criterion-is-current-source-only
  (is (some? start))
  (is (str/includes? (subs snapshot start end) "rather than only consuming another record"))
  (is (str/includes? (subs snapshot start end) "GOV2001-GOV2008"))
  (doseq [qualified-bridge [nil bridge]]
    (let [result (current/validate! reference qualified-bridge readers)]
      (is (= current/current-view (:source-view result)))
      (is (= :current-task-source-interface-only (:authority result)))
      (is (= :unassessed (:implementation-acceptance result)))
      (is (= checkpoint (:source-commit result)))
      (is (= criterion (:criterion-source result)))
      (is (= ["Open" false] ((juxt :observed-status :observed-checked?) result))))))

(deftest current-source-fields-and-scope-forgeries-fail
  (doseq [forged [(dissoc reference :source-view)
                  (assoc reference :source-view :current)
                  (assoc reference :path "../outside.md")
                  (assoc reference :path "/outside.md")
                  (assoc reference :source-tree (apply str (repeat 40 "0")))
                  (assoc reference :source-commit (apply str (repeat 40 "0")))
                  (assoc reference :sha256 (rr/sha256 "other file"))
                  (assoc reference :source-record-sha256 (rr/sha256 "other record"))
                  (assoc reference :observed-status "Complete")
                  (assoc reference :observed-checked? true)
                  (assoc reference :detailed-acceptance :accepted)
                  (assoc reference :qualification "")
                  (dissoc reference :criterion-source)
                  (assoc-in reference [:criterion-source :sha256] (rr/sha256 "other criterion"))
                  (assoc-in reference [:criterion-source :end-byte] Long/MAX_VALUE)
                  (assoc reference :criterion-source {:start-byte 0 :end-byte 1 :sha256 (rr/sha256 (subs snapshot 0 1))})
                  (assoc reference :id "evolution/experiment-lifecycle")
                  (assoc reference :marker "evolution-absent-region")]]
    (is (thrown? Exception (current/validate! forged bridge readers))))
  (doseq [forged-bridge [(assoc bridge :current-ref (apply str (repeat 40 "1")))
                         (assoc bridge :current-tree (apply str (repeat 40 "1")))
                         (assoc bridge :current-sha256 (:historical-sha256 bridge))
                         (assoc bridge :marker "other-authority")]]
    (is (thrown? Exception (current/validate! reference forged-bridge readers))))
  (is (thrown? Exception (current/validate! reference bridge (assoc readers :read-current (constantly (str snapshot "\nChanged.")))))))

(deftest actual-tree-object-cannot-stand-in-for-the-source-commit
  ;; A tree resolves through ^{tree} and serves the identical file, so checking
  ;; those values alone cannot authenticate the claimed commit identity.
  (is (= tree (rr/git-tree "." tree rr/require!)))
  (is (= snapshot (read-ref tree path)))
  (let [production-readers (rr/current-task-reference-readers ".")
        forged (assoc reference :source-commit tree)]
    (is (= tree ((:read-commit-tree production-readers) checkpoint)))
    (is (thrown? Exception ((:read-commit-tree production-readers) tree)))
    (is (thrown? Exception (current/validate! forged nil production-readers)))
    (is (thrown? Exception (current/validate! forged nil readers)))
    (is (thrown? Exception (current/validate! reference nil (dissoc readers :read-commit-tree))))))

(deftest legacy-historical-identity-is-not-relabeled
  (with-redefs [rr/current-task-reference-readers (constantly readers)
                rr/checked-path (fn [_ p] p)
                rr/read-source (constantly snapshot)]
    (is (= snapshot (rr/task-source-text "." reference {path bridge})))
    (is (= snapshot (rr/task-source-text "." reference {})))
    (with-redefs [rr/read-source (fn [_] (throw (ex-info "Unvalidated second live read" {})))]
      (is (= snapshot (rr/task-source-text "." reference {path bridge}))))
    (is (thrown? Exception (rr/task-source-text "." (dissoc reference :source-view) {path bridge})))
    (is (thrown? Exception (rr/task-source-text "." (assoc reference :source-view :unknown) {})))
    (is (= snapshot (rr/task-source-text "." {:path path :sha256 (:historical-sha256 bridge)} {path bridge})))
    (is (= snapshot (rr/task-source-text "." {:path path :sha256 (rr/sha256 snapshot)} {})))
    (is (thrown? Exception (rr/task-source-text "." {:path path :sha256 (rr/sha256 "stale")} {})))))

(deftest source-version-validator-selects-the-explicit-reference-view
  (let [historical-ref (apply str (repeat 40 "1"))
        historical (str snapshot "\nHistorical source fixture.\n")
        b (assoc bridge :historical-ref historical-ref :historical-sha256 (rr/sha256 historical)
                 :changed-records [] :current-only-records [] :change-class :source-fixture
                 :qualification "Separate historical/current source fixture; no acceptance.")
        manifest {:task-source-version-bridges [b] :requirements [{:id "fixture" :task-reference reference}]}]
    (with-redefs [sources/git-source (fn [_ ref p _]
                                      (rr/require! (= path p) "Unexpected path")
                                      (if (= historical-ref ref) historical (read-ref ref p)))
                  rr/current-task-reference-readers (constantly readers)]
      (is (= [b] (rr/validate-task-source-versions! "." manifest)))
      (is (thrown? Exception (rr/validate-task-source-versions! "." (update-in manifest [:requirements 0 :task-reference] dissoc :source-view))))
      (is (thrown? Exception (rr/validate-task-source-versions! "." (assoc-in manifest [:requirements 0 :task-reference :source-view] :unknown))))
      (is (= [b] (rr/validate-task-source-versions! "." (assoc-in manifest [:requirements 0 :task-reference] {:path path :sha256 (:historical-sha256 b)})))))))

(deftest evidence-projection-keeps-the-reference-view-and-unassessed-acceptance
  (with-redefs [rr/current-task-reference-readers (constantly readers)
                rr/checked-path (fn [_ p] p)
                rr/read-source (constantly snapshot)]
    (let [requirement {:id "governance/compatibility-assessment" :task-reference reference
                       :additional-task-interfaces [reference] :historical-support []}
          read-pinned #(rr/task-source-text "." % {path bridge})
          result (evidence/project requirement read-pinned rr/sha256 checkpoint)]
      (is (= :unassessed (:current-acceptance result)))
      (is (= task-id (get-in result [:canonical-task :id])))
      (is (= "Open" (get-in result [:canonical-task :status])))
      (is (= 1 (count (:additional-task-interfaces result))))
      (is (empty? (:historical-support result)))
      (is (thrown? Exception (evidence/project (update requirement :task-reference dissoc :source-view)
                                               read-pinned rr/sha256 checkpoint))))))

(deftest utf8-criterion-boundaries-do-not-accept-partial-code-points
  (let [text "<!-- example:start -->\n- [ ] `example/task` Example\nStatus: Open\nClosing evidence: \u03c0\n<!-- example:end -->\n"
        a (str/index-of text "\u03c0") offset (alength (.getBytes (subs text 0 a) "UTF-8"))
        r (evidence/task-record text "example" "example/task")
        ref (assoc reference :path "fixture.md" :marker "example" :id "example/task"
                   :sha256 (rr/sha256 text) :source-record-sha256 (rr/task-record-digest r rr/sha256)
                   :criterion-source {:start-byte offset :end-byte (+ offset 2) :sha256 (rr/sha256 "\u03c0")})
        fixture-readers (assoc readers :read-ref (fn [_ _] text) :read-current (constantly text))]
    (is (= :unassessed (:implementation-acceptance (current/validate! ref nil fixture-readers))))
    (is (thrown? Exception (current/validate! (update-in ref [:criterion-source :start-byte] inc) nil fixture-readers)))
    (is (thrown? Exception (current/validate! (update-in ref [:criterion-source :end-byte] dec) nil fixture-readers)))))

(defn transition-fixture []
  (let [path "fixture/tasks.md" marker "fixture" id "fixture/task"
        historical-ref (apply str (repeat 40 "1")) prior-ref (apply str (repeat 40 "2"))
        current-ref (apply str (repeat 40 "3")) model-ref (apply str (repeat 40 "4"))
        tree-of #(apply str (repeat 40 (str ({historical-ref 5 prior-ref 6 current-ref 7 model-ref 8} %))))
        text "<!-- fixture:start -->\n- [ ] `fixture/task` Task\nStatus: Open\nClosing evidence: Original predicate.\n<!-- fixture:end -->\n"
        current-text (str/replace text "Original predicate." "Original predicate. Current source clause.")
        digest #(rr/task-record-digest (evidence/task-record % marker id) rr/sha256)
        old {:path path :marker marker :historical-ref historical-ref :historical-sha256 (rr/sha256 text)
             :current-ref prior-ref :current-tree (tree-of prior-ref) :current-sha256 (rr/sha256 text)
             :changed-records [{:id id :historical-record-sha256 (digest text) :current-record-sha256 (digest text)}]
             :current-only-records [] :change-class :fixture :qualification "Prior source only."}
        other (assoc old :path "fixture/reader.md")
        prior-table [old other]
        model-text (pr-str {:task-source-version-bridges prior-table :source-view-review-history [{:kind :older-unrelated-source-history}]})
        observation (fn [t] (let [span (current/task-body-span t marker id rr/require!)
                                  bytes (.getBytes t "UTF-8")]
                              (assoc span :sha256 (rr/sha256 (String. (java.util.Arrays/copyOfRange bytes (:start-byte span) (:end-byte span)) "UTF-8")))))
        successor (assoc old :change-class current/bridge-successor-class :predecessor-bridge old
                         :current-ref current-ref :current-tree (tree-of current-ref) :current-sha256 (rr/sha256 current-text)
                         :changed-records [{:id id :historical-record-sha256 (digest text) :current-record-sha256 (digest current-text)}]
                         :previously-declared-record-observations [{:id id :prior-captured-record-sha256 (digest text) :current-captured-record-sha256 (digest current-text)
                                                                   :prior-whole-body (observation text) :current-whole-body (observation current-text)}])
        frame {:kind current/bridge-transition-kind :source-commit model-ref :source-tree (tree-of model-ref)
               :manifest-sha256 (rr/sha256 model-text) :prior-task-source-version-bridges prior-table
               :qualification "Preserve complete predecessor source without evidence acceptance."}
        manifest {:task-source-version-bridges [successor other] :source-view-review-history [{:kind :older-unrelated-source-history} frame]}
        source-readers {:read-commit-tree (fn [ref] (rr/require! (contains? #{historical-ref prior-ref current-ref model-ref} ref) "Actual commit required") (tree-of ref))
                        :read-ref (fn [ref p] (cond (= p "contracts/requirement-reconciliation.edn") model-text
                                                   (= current-ref ref) current-text :else text))
                        :task-record evidence/task-record :sha256 rr/sha256 :require! rr/require!}]
    {:manifest manifest :readers source-readers :current-text current-text}))

(deftest task-source-transition-preserves-complete-source-history
  (let [{:keys [manifest readers]} (transition-fixture)
        result (current/validate-bridge-transition! manifest readers)]
    (is (= current/bridge-transition-kind (:kind result)))
    (is (= 2 (:prior-bridge-count result)))
    (is (= :unassessed (:implementation-acceptance result)))
    (is (nil? (current/validate-bridge-transition! {:task-source-version-bridges []} readers)))
    (is (thrown? Exception (current/validate-bridge-transition! (update manifest :source-view-review-history #(into [{:kind :historical-unrelated}] %)) readers)))))

(deftest task-source-transition-rejects-history-and-body-forgeries
  (let [{:keys [manifest readers current-text]} (transition-fixture)
        b [:task-source-version-bridges 0]
        h [:source-view-review-history 1]
        o (into b [:previously-declared-record-observations 0])]
    (doseq [forged [(update manifest :source-view-review-history pop)
                    (update-in manifest (conj h :prior-task-source-version-bridges) pop)
                    (assoc-in manifest (conj h :source-commit) (apply str (repeat 40 "9")))
                    (assoc-in manifest (conj h :source-tree) (apply str (repeat 40 "9")))
                    (assoc-in manifest (conj h :manifest-sha256) (rr/sha256 "other model"))
                    (update manifest :task-source-version-bridges pop)
                    (update-in manifest b dissoc :predecessor-bridge)
                    (assoc-in manifest (conj b :change-class) :unknown)
                    (assoc-in manifest (conj b :historical-ref) (apply str (repeat 40 "9")))
                    (assoc-in manifest (conj b :predecessor-bridge :qualification) "Rewritten prior source")
                    (assoc-in manifest (conj b :changed-records 0 :historical-record-sha256) (rr/sha256 "other record"))
                    (assoc-in manifest (conj b :previously-declared-record-observations) [])
                    (update-in manifest (conj b :previously-declared-record-observations) #(conj % (first %)))
                    (assoc-in manifest (conj o :current-captured-record-sha256) (rr/sha256 "other scalar"))
                    (update-in manifest (into o [:current-whole-body :start-byte]) inc)
                    (assoc-in manifest (into o [:current-whole-body :sha256]) (rr/sha256 "other body"))]]
      (is (thrown? Exception (current/validate-bridge-transition! forged readers))))
    (is (thrown? Exception (current/validate-bridge-transition! manifest (dissoc readers :read-commit-tree))))
    (let [accepted (-> current-text
                       (str/replace "- [ ]" "- [x]")
                       (str/replace "Status: Open" "Status: Accepted; forged"))
          forged (assoc-in manifest (conj b :current-sha256) (rr/sha256 accepted))]
      (is (thrown-with-msg? Exception #"relabels task acceptance"
             (current/validate-bridge-transition! forged
               (assoc readers :read-ref (fn [ref p]
                                         (if (= (get-in manifest (conj b :current-ref)) ref)
                                           accepted ((:read-ref readers) ref p))))))))
    (is (thrown? Exception (current/validate-bridge-transition! manifest
                             (assoc readers :read-ref (fn [ref p]
                                                       (if (= (get-in manifest (conj b :current-ref)) ref)
                                                         (str/replace current-text "Status: Open" "Status: Accepted; forged")
                                                         ((:read-ref readers) ref p)))))))))

(let [result (run-tests 'requirement-current-task-sources-test)]
  (shutdown-agents)
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
