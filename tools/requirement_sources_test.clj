(ns requirement-sources-test
  (:require [clojure.test :refer [deftest is run-tests]] [clojure.edn :as edn]))

(binding [*command-line-args* nil] (load-file "tools/requirement_sources.clj"))
(require '[requirement-sources :as sources])

(defn require! [condition message] (when-not condition (throw (ex-info message {}))))
(defn sha [text]
  (apply str (map #(format "%02x" (bit-and 255 %))
                 (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes text "UTF-8")))))
(def read-ref (memoize #(sources/git-source "." %1 %2 require!)))
(def frame (edn/read-string (slurp "specs/sh-family-crosswalk/implementation-readiness-current-source-version.edn")))

(deftest current-composition-preserves-the-complete-previous-frame
  (is (= sources/source-composition-history-contract
         (sources/validate-source-composition-history! frame read-ref sha require!)))
  (let [predecessor (:predecessor sources/source-composition-history-contract)
        before (edn/read-string (read-ref (:commit predecessor) (:path predecessor)))
        inverse (-> frame
                    (assoc :current-source-transitions (:current-source-transitions before)
                           :current-source-library-io-criterion-joins (:current-source-library-io-criterion-joins before))
                    (dissoc :source-composition-transition-history :source-composition-future-task-records))]
    (is (= before inverse))
    (is (= (:source-snapshots before) (:source-snapshots frame)))
    (is (= (:future-task-records before) (:future-task-records frame)))))

(deftest current-composition-rejects-history-and-scope-forgeries
  (doseq [forged [(dissoc frame :source-composition-transition-history)
                  (assoc-in frame [:source-composition-transition-history :authority] :implementation)
                  (assoc-in frame [:source-composition-transition-history :implementation-acceptance] :accepted)
                  (assoc-in frame [:source-composition-transition-history :predecessor :commit]
                            (get-in frame [:source-composition-transition-history :predecessor :tree]))
                  (assoc-in frame [:source-composition-transition-history :predecessor :sha256] (sha "other source"))
                  (assoc frame :source-snapshots {})
                  (assoc frame :future-task-records [])
                  (assoc frame :source-successor-binding {})
                  (assoc frame :source-composition-future-task-records [])
                  (update-in frame [:source-composition-future-task-records 0 :checked] not)
                  (update frame :current-source-transitions pop)
                  (update frame :current-source-library-io-criterion-joins pop)]]
    (is (thrown? Exception (sources/validate-source-composition-history! forged read-ref sha require!)))))

(deftest current-files-require-exact-commits-spans-and-live-source
  (doseq [contract sources/current-source-transition-contracts]
    (is (= (:path contract)
           (:path (sources/validate-current-source-transition! contract contract read-ref slurp sha require!)))))
  (let [contract (first sources/source-composition-transition-contracts)
        forged (assoc contract :applying-ref (:applying-tree contract))]
    ;; The identical tree:path can serve the bytes; commit type must still fail.
    (is (= (read-ref (:applying-ref contract) (:path contract))
           (read-ref (:applying-tree contract) (:path contract))))
    (is (thrown? Exception (sources/validate-current-source-transition! forged forged read-ref slurp sha require!)))
    (is (thrown? Exception (sources/validate-current-source-transition! contract contract read-ref #(str (slurp %) "\nUnbound source") sha require!)))
    (is (thrown? Exception (sources/validate-current-source-transition!
                           (assoc contract :authority :implementation) contract read-ref slurp sha require!)))))

(deftest current-library-io-keeps-exact-predicates-and-owners
  (let [joins (:current-source-library-io-criterion-joins frame)]
    (is (= joins (sources/validate-current-source-library-io-criterion-joins! joins read-ref slurp sha require!)))
    (is (= (mapv :id sources/current-source-library-io-criterion-contracts) (mapv :id joins)))
    (is (= sources/current-source-library-io-criterion-contracts (mapv :successor-of joins)))
    (doseq [forged [(update joins 0 assoc :owner "foreign/task")
                    (update joins 0 dissoc :successor-of)
                    (assoc-in joins [0 :source-span :sha256] (sha "different predicate"))
                    (assoc-in joins [0 :source-commit] (:source-tree (first joins)))]]
      (is (thrown? Exception (sources/validate-current-source-library-io-criterion-joins! forged read-ref slurp sha require!))))
    (is (thrown? Exception (sources/validate-current-source-library-io-criterion-joins!
                           joins read-ref #(str (slurp %) "\nDifferent current file") sha require!)))))

(deftest predecessor-payloads-retain-their-authenticated-subjects
  (let [descriptor (:predecessor-crosswalk sources/source-composition-history-contract)
        old (edn/read-string (read-ref (:commit descriptor) (:path descriptor)))
        snapshots (sources/source-composition-predecessor-snapshots read-ref sha require!)]
    (is (= (:source-snapshots old) snapshots))
    (is (every? (fn [[digest raw]] (= digest (sha raw))) snapshots))
    (is (thrown? Exception (sources/source-composition-predecessor-snapshots
                           (fn [commit path] (str (read-ref commit path) "\nUnbound predecessor")) sha require!)))))

(let [result (run-tests 'requirement-sources-test)]
  (shutdown-agents)
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
