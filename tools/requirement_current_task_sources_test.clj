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

(let [result (run-tests 'requirement-current-task-sources-test)]
  (shutdown-agents)
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
