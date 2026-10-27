(ns compatibility-initialization-audit-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]
            [gravity.self-hosting.sh01-component-test-dependencies :as consumer])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(binding [*command-line-args* nil] (load-file "tools/compatibility_initialization_audit.clj"))
(require '[compatibility-initialization-audit :as initialization])

(defn analyze [text] (initialization/analyze-source "probe.clj" text))
(defn calls [record region] (set (get record region)))

(deftest captured-functions-are-separated-from-current-syntax
  (let [[record] (analyze "(def helper (let [token (Object.)] (fn [x] (slurp x))))")]
    (is (= :def-initializer (:kind record)))
    (is (= '#{let Object. fn} (calls record :current-call-candidates)))
    (is (= '#{slurp} (calls record :body-call-candidates)))
    (is (= '[fn] (:body-spelling-assumptions record)))
    (is (= :unresolved (:call-and-effect-resolution record)))))

(deftest delay-reify-and-nested-definitions-retain-body-and-expansion-boundaries
  (let [[delay-record reify-record nested-record class-record]
        (analyze (str "(def cached (delay (build-source!)))\n"
                      "(def pending (reify clojure.lang.IDeref (deref [_] (read-source!))))\n"
                      "(let [token (Object.)] (clojure.core/defn f [] (start-process!)))\n"
                      "(def byte-array-class (Class/forName \"[B\"))"))]
    (is (= '#{delay} (calls delay-record :current-call-candidates)))
    (is (contains? (calls delay-record :body-call-candidates) 'build-source!))
    (is (= '#{reify} (calls reify-record :current-call-candidates)))
    (is (contains? (calls reify-record :body-call-candidates) 'read-source!))
    (is (not (contains? (calls nested-record :current-call-candidates) 'start-process!)))
    (is (contains? (calls nested-record :body-call-candidates) 'start-process!))
    (is (= '#{Class/forName} (calls class-record :current-call-candidates)))))

(deftest asynchronous-and-dynamic-invocations-never-become-effect-proof
  (let [records (analyze (str "(def started (future (slurp \"input\")))\n"
                              "((fn [] (slurp \"input\")))\n"
                              "(let [delay slurp] (delay \"input\"))"))
        report (initialization/observation-report {} [{:forms records}])]
    (is (contains? (calls (first records) :current-call-candidates) 'slurp))
    (is (contains? (calls (second records) :current-call-candidates) :non-symbol-call-head))
    (is (contains? (set (:body-spelling-assumptions (nth records 2))) 'delay))
    (is (contains? (set (:unresolved report)) :lexical-and-dynamic-call-targets))
    (is (false? (:closure-complete? report)))))

(deftest source-and-macros-are-never-executed
  (let [sentinel (.toFile (Files/createTempFile "initialization-audit-" ".sentinel"
                                               (make-array FileAttribute 0)))]
    (io/delete-file sentinel)
    (try
      (let [records (analyze (str "(defmacro m [] (spit " (pr-str (str sentinel)) " \"ran\"))\n(m)"))
            generated (analyze "(defmacro m [] '(slurp \"generated\")) (m)")]
        (is (not (.exists sentinel)))
        (is (= :macro-definition (:kind (first records))))
        (is (contains? (calls (first records) :body-call-candidates) 'spit))
        (is (contains? (calls (second records) :current-call-candidates) 'm))
        (is (= :unresolved (:call-and-effect-resolution (second generated)))))
      (finally (when (.exists sentinel) (io/delete-file sentinel))))))

(deftest reader-policy-applies-to-all-forms-without-observed-namespace-loading
  (doseq [literal ["#=(System/exit 99)" "#inst \"2026-10-26\""
                   "#uuid \"00000000-0000-0000-0000-000000000000\"" "#custom/tag \"x\""]]
    (is (= :namespace-read
           (try (analyze (str "(ns nonexistent.audit.namespace) " literal)) nil
                (catch clojure.lang.ExceptionInfo exception (:reason (ex-data exception)))))))
  (is (nil? (find-ns 'nonexistent.audit.namespace))))

(deftest observation-representation-cannot-enter-the-existing-cache-contract
  (let [report (initialization/observation-report
                {:authority :authoritative :closure-complete? true :cache-narrowing-authorized? true}
                [{:path "probe.clj" :forms (analyze "(def x 1)")}])]
    (is (= :none (:authority report)))
    (is (false? (:closure-complete? report)))
    (is (false? (:cache-narrowing-authorized? report)))
    (is (false? (consumer/valid-cache-closure? {} {} report)))
    (is (false? (consumer/valid-cache-closure?
                 {} {} (assoc report :schema consumer/cache-closure-schema
                              :closure-complete? true :cache-narrowing-authorized? true))))))

(deftest source-order-and-qualified-core-spellings-remain-visible
  (let [records (analyze "(def unbound)\n(clojure.core/defn f [] (slurp \"x\"))\n(load \"/a\")")]
    (is (= [0 1 2] (mapv :ordinal records)))
    (is (= [1 2 3] (mapv :line records)))
    (is (empty? (:current-call-candidates (first records))))
    (is (= :function-definition (:kind (second records))))
    (is (= '#{load} (calls (nth records 2) :current-call-candidates)))))

(let [result (run-tests 'compatibility-initialization-audit-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
