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
  (let [consumer-prior (sources/validate-source-consumer-file-history! frame read-ref sha require!)
        predecessor (:predecessor sources/source-composition-history-contract)
        before (edn/read-string (read-ref (:commit predecessor) (:path predecessor)))
        inverse (-> consumer-prior
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
    (is (= (:source-snapshots old) (select-keys snapshots (keys (:source-snapshots old)))))
    (let [previous (:predecessor-crosswalk sources/source-consumer-file-history-contract)
          previous-crosswalk (edn/read-string (read-ref (:commit previous) (:path previous)))]
      (is (= (:source-snapshots previous-crosswalk) (select-keys snapshots (keys (:source-snapshots previous-crosswalk))))))
    (let [latest (:predecessor-crosswalk sources/auth-crypto-source-document-history-contract)
          latest-crosswalk (edn/read-string (read-ref (:commit latest) (:path latest)))]
      (is (= 578 (count (:source-snapshots latest-crosswalk))))
      (is (= (:source-snapshots latest-crosswalk) snapshots)))
    (is (every? (fn [[digest raw]] (= digest (sha raw))) snapshots))
    (is (thrown? Exception (sources/source-composition-predecessor-snapshots
                           (fn [commit path] (str (read-ref commit path) "\nUnbound predecessor")) sha require!)))))

(deftest build-consumer-successor-preserves-the-exact-previous-frame
  (let [history sources/source-consumer-file-history-contract
        descriptor (:predecessor-frame history)
        previous (edn/read-string (read-ref (:commit descriptor) (:path descriptor)))
        path "specs/build-workflow/tasks.md"
        old-transitions (:current-source-transitions previous)
        current-transitions (:current-source-transitions
                             (sources/validate-auth-crypto-source-document-history! frame read-ref sha require!))]
    (is (= previous (sources/validate-source-consumer-file-history! frame read-ref sha require!)))
    (is (= 48 (count old-transitions)))
    (is (= 49 (count current-transitions)))
    (is (not-any? #(= path (:path %)) old-transitions))
    (is (= old-transitions (vec (remove #(= path (:path %)) current-transitions))))
    (is (not (contains? sources/source-consumer-file-transition :successor-of)))
    (is (= (:source-composition-future-task-records previous) (:source-composition-future-task-records frame)))
    (doseq [forged [(update frame :source-composition-transition-history dissoc :source-consumer-file-successor)
                    (assoc-in frame [:source-composition-transition-history :source-consumer-file-successor :authority] :implementation)
                    (assoc-in frame [:source-composition-transition-history :source-consumer-file-successor :predecessor-frame :sha256] (sha "Wrong previous frame"))
                    (update frame :current-source-transitions pop)
                    (update frame :current-source-library-io-criterion-joins pop)
                    (assoc frame :source-snapshots {})
                    (assoc frame :source-composition-future-task-records [])]]
      (is (thrown? Exception (sources/validate-source-consumer-file-history! forged read-ref sha require!))))
    ;; A tree:path serves identical bytes; authenticating it as the previous
    ;; commit must fail even when both the expected and supplied labels change.
    (let [forged-history (assoc-in history [:predecessor-frame :commit] (:tree descriptor))
          forged-frame (assoc-in frame [:source-composition-transition-history :source-consumer-file-successor] forged-history)]
      (with-redefs [sources/source-consumer-file-history-contract forged-history]
        (is (thrown? Exception (sources/validate-source-consumer-file-history! forged-frame read-ref sha require!)))))))

(deftest auth-crypto-document-successor-preserves-the-complete-parent
  (let [history sources/auth-crypto-source-document-history-contract
        descriptor (:predecessor-frame history)
        previous (edn/read-string (read-ref (:commit descriptor) (:path descriptor)))
        path "docs/acceptance-dependencies.md"
        old-transitions (:current-source-transitions previous)
        current-transitions (:current-source-transitions frame)
        old-doc (first (filter #(= path (:path %)) old-transitions))
        new-doc (first (filter #(= path (:path %)) current-transitions))]
    (is (= previous (sources/validate-auth-crypto-source-document-history! frame read-ref sha require!)))
    (is (= 49 (count old-transitions) (count current-transitions)))
    (is (= old-doc (:successor-of new-doc)))
    (is (= (remove #(= path (:path %)) old-transitions) (remove #(= path (:path %)) current-transitions)))
    (is (= (:current-source-library-io-criterion-joins previous) (:current-source-library-io-criterion-joins frame)))
    (is (= (:source-composition-future-task-records previous) (:source-composition-future-task-records frame)))
    (doseq [forged [(update frame :source-composition-transition-history dissoc :auth-crypto-source-document-successor)
                    (assoc-in frame [:source-composition-transition-history :auth-crypto-source-document-successor :authority] :implementation)
                    (assoc-in frame [:source-composition-transition-history :auth-crypto-source-document-successor :current-criterion :source-span :sha256] (sha "wrong row"))
                    (assoc-in frame [:source-composition-transition-history :auth-crypto-source-document-successor :predecessor-frame :sha256] (sha "wrong previous frame"))
                    (assoc frame :source-snapshots {})
                    (update frame :current-source-transitions pop)
                    (update frame :current-source-library-io-criterion-joins pop)
                    (assoc frame :source-composition-future-task-records [])]]
      (is (thrown? Exception (sources/validate-auth-crypto-source-document-history! forged read-ref sha require!))))
    (let [forged-history (assoc-in history [:predecessor-frame :commit] (:tree descriptor))
          forged-frame (assoc-in frame [:source-composition-transition-history :auth-crypto-source-document-successor] forged-history)]
      (with-redefs [sources/auth-crypto-source-document-history-contract forged-history]
        (is (thrown? Exception (sources/validate-auth-crypto-source-document-history! forged-frame read-ref sha require!)))))))

(let [result (run-tests 'requirement-sources-test)]
  (shutdown-agents)
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
