(ns gravity.pass-cache-artifact-identity-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.pass-cache :as cache]
            [gravity.pass-cache-test]
            [gravity.pass-cache.receipt-validation :as receipt-validation]))

(defn- private-var
  [namespace symbol]
  (var-get (ns-resolve (find-ns namespace) symbol)))

(defn- warm-hit-result
  [artifact-id-of]
  (let [request-fn (private-var 'gravity.pass-cache-test 'request)
        operations-fn (private-var 'gravity.pass-cache-test 'operations)
        delete-tree-fn (private-var 'gravity.pass-cache-test 'delete-tree!)
        directory (java.nio.file.Files/createTempDirectory
                   "gravity-pass-cache-artifact-identity-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [request (request-fn)
            key (cache/stage-cache-key request)
            operations (assoc (operations-fn (atom {}))
                              :artifact-id-of
                              artifact-id-of)
            store (cache/open-local-store directory)]
        (is (= :stored
               (:status (cache/lookup-or-compute!
                         store key request operations))))
        (:status (cache/lookup! store key operations)))
      (finally
        (delete-tree-fn directory)))))

(deftest warm-hit-retains-known-pure-envelope-identity
  (let [identity-calls (atom 0)]
    (with-redefs [receipt-validation/envelope-artifact-id
                  (fn [artifact]
                    (swap! identity-calls inc)
                    (:artifact-id artifact))]
      (is (= :hit
             (warm-hit-result receipt-validation/envelope-artifact-id)))
      ;; Execution and publication each identity-check the first artifact.
      ;; Only the receipt validation call remains on the warm hit when the
      ;; named operation is recognized.
      (is (= 3 @identity-calls)))))

(deftest warm-hit-preserves-custom-identity-callback
  (let [identity-calls (atom 0)
        callback (fn [artifact]
                   (swap! identity-calls inc)
                   (:artifact-id artifact))]
    (is (= :hit (warm-hit-result callback)))
    ;; A replaced operation is intentionally outside the fast path: both the
    ;; receipt check and the derived-entry check still observe it.
    (is (= 4 @identity-calls)
        "custom identity callbacks retain their existing observability")))
