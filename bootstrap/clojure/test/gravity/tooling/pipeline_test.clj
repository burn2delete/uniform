(ns gravity.tooling.pipeline-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.tooling :as tooling]
            [gravity.self-hosting.w5-tooling-conformance-verifier-test]))

(defn- helper [name]
  (var-get (ns-resolve 'gravity.self-hosting.w5-tooling-conformance-verifier-test name)))

(defn- invoke [function arguments]
  ((helper 'invoke) (helper 'engine-plan) function arguments))

(defn- original-request [extension]
  ((helper 'request-at)
   (helper (if (= extension ".gravity") 'accepted-gravity-plan 'accepted-qst-plan))
   ((helper 'fixture-path) "accepted" "tooling-conformance" extension)
   extension (if (= extension ".gravity") :gravity :qst)))

(deftest explicit-contract-does-not-promote-coverage-labels
  (doseq [operation [#'tooling/coverage-record #'tooling/check-evidence
                     #'tooling/execute-evidence #'tooling/verify-evidence]]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.tooling (:namespace contract)))
      (is (= :none (get-in contract [:public-api (:name (meta operation)) :admission])))
      (is (= (:arglists (meta operation))
             (get-in contract [:public-api (:name (meta operation)) :arglists])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-certificates? contract)))
      (is (false? (:performs-tool-services? contract)))
      (is (= '[clojure.set] (get-in contract [:dependency-direction :requires])))))
  (is (= #{'set} (set (keys (ns-aliases 'gravity.tooling)))))
  (is (= '([artifact]) (:arglists (meta #'bootstrap/p13-tooling-proof))))
  (let [record (bootstrap/p13-tooling-proof {})]
    (is (= :complete (:status record)))
    (is (false? (:document-coverage-complete? record)))
    (is (false? (:cli-and-project-workflow-covered? record)))
    (is (= "W5-TC-SCHEMA"
           (get-in (tooling/check-evidence invoke record) [:diagnostics 0 :rule])))))

(deftest complete-source-execution-preserves-incomplete-gate-and-refusals
  (doseq [extension [".gravity" ".qst"]]
    (let [original (original-request extension)
          bound (tooling/execute-evidence invoke original)
          result (:result bound)
          rejected-plan (helper (if (= extension ".gravity") 'rejected-gravity-plan 'rejected-qst-plan))]
      (is (= :accepted (:status result)))
      (is (= :incomplete (:completion-status result)))
      (is (= :blocked (get-in result [:verifier-gate :decision])))
      (is (false? (get-in result [:verifier-gate :release-eligible?])))
      (is (= :non-authority (:authority result)))
      (is (= :passed (get-in bound [:verification :status])))
      (is (= result (tooling/check-evidence invoke original)))
      (doseq [[function rule] [['w5-tooling-conformance-invalid-source-request "W5-TC-SOURCE"]
                              ['w5-tooling-conformance-invalid-narrative-request "W5-TC-NARRATIVE"]
                              ['w5-tooling-conformance-invalid-source-span-missing-request "W5-TC-SOURCE"]]]
        (let [request ((helper 'invoke) rejected-plan function [original])
              rejected (tooling/execute-evidence invoke request)]
          (is (= :rejected (get-in rejected [:result :status])))
          (is (= rule (get-in rejected [:result :diagnostics 0 :rule])))
          (is (= :passed (get-in rejected [:verification :status]))))))))

(deftest replay-keeps-the-original-request-and-rejects-substitution
  (let [original (original-request ".gravity")
        result (tooling/check-evidence invoke original)
        forged (assoc result :completion-status :complete)
        refusal (tooling/verify-evidence invoke original forged)
        alternate ((helper 'request-at) (helper 'accepted-gravity-plan)
                   "fixture/unbound-substitution.gravity" ".gravity" :gravity)
        alternate-result (tooling/check-evidence invoke alternate)]
    (is (= :rejected (:status refusal)))
    (is (= "W5-TC-SUBSTITUTION" (get-in refusal [:diagnostics 0 :rule])))
    (is (= :non-authority (:authority refusal)))
    (is (= :accepted (:status alternate-result)))
    (is (= (:identity-input result) (:identity-input alternate-result)))
    (is (not= (:provenance result) (:provenance alternate-result)))
    (is (= :rejected (:status (tooling/verify-evidence invoke original alternate-result))))))

(deftest failed-fresh-verification-withholds-the-private-result
  (let [original (original-request ".gravity") calls (atom [])
        alternate ((helper 'request-at) (helper 'accepted-gravity-plan)
                   "fixture/unbound-substitution.gravity" ".gravity" :gravity)
        candidate (tooling/check-evidence invoke alternate)
        refusal (tooling/verify-evidence invoke original candidate)
        operation (fn [function arguments]
                    (swap! calls conj [function arguments])
                    (if (= function 'w5-tooling-conformance-verify)
                      candidate
                      (invoke function arguments)))
        result (tooling/execute-evidence operation original)]
    (is (= refusal result))
    (is (= :rejected (:status result)))
    (is (= "W5-TC-SUBSTITUTION" (get-in result [:diagnostics 0 :rule])))
    (is (not (contains? result :result)))
    (is (= ['w5-tooling-conformance-verify 'w5-tooling-conformance-verify-result]
           (mapv first @calls)))
    (is (identical? original (get-in @calls [0 1 0])))
    (is (identical? original (get-in @calls [1 1 0])))))

(def ^:private document-ids [:fixture/before])
(def ^:private diagnostic-ids [])
(defn- present [_value] true)
(defn- task-statuses [] {:fixture :supplied-data})

(defn- observed-artifact [data observe]
  (reify clojure.lang.ILookup
    (valAt [_ key] (observe key) (get data key))
    (valAt [_ key not-found] (observe key) (get data key not-found))))

(deftest document-vars-are-read-at-the-original-body-position
  (with-redefs [document-ids [:fixture/before]]
    (let [artifact
          (observed-artifact
           {:document-contracts {:fixture/after :unbound}}
           (fn [key]
             (when (= key :document-contracts)
               (alter-var-root #'document-ids (constantly [:fixture/after])))))
          result (tooling/coverage-record #'document-ids #'diagnostic-ids #'present #'task-statuses artifact)]
      (is (true? (:document-coverage-complete? result)))
      (is (= {:fixture :supplied-data} (:task-statuses result))))))

(deftest callback-root-is-read-before-its-argument-lookup
  (let [calls (atom [])]
    (with-redefs [present (fn [_value] (swap! calls conj :old) true)]
      (let [artifact
            (observed-artifact
             {:registry-ux-record {:update-diff {:capability-diff-visible true}}
              :ir-inspector-bundle {:source-span-maps [:fixture/unbound]}
              :profiler-report {:check-elision-report {:evidence [:fixture/unbound]}}
              :safety-audit-report {:proof-index [:fixture/unbound]}}
             (fn [key]
               (when (= key :ir-inspector-bundle)
                 (alter-var-root #'present
                                 (constantly (fn [_value] (swap! calls conj :new) true))))))
            result (tooling/coverage-record #'document-ids #'diagnostic-ids #'present #'task-statuses artifact)]
        (is (true? (:dev-registry-and-inspectors-covered? result)))
        (is (= [:old :new :new] @calls))))))

(deftest missing-line-fixture-retains-every-other-map-entry
  (doseq [plan [(helper 'rejected-gravity-plan) (helper 'rejected-qst-plan)]]
    (let [span {:source-id :fixture/unbound :line 1 :column 1 :start-byte 0 :end-byte 8
                :extra {:ai/human-review :required} :nil-value nil}
          result ((helper 'invoke) plan 'tc-span-without-line [span])]
      (is (= (dissoc span :line) result))
      (is (contains? result :nil-value))
      (is (= :required (get-in result [:extra :ai/human-review]))))))
