(ns ci.progress-isolation-test
  (:require [clojure.test :as test :refer [deftest is run-tests]]
            [clojure.edn :as edn] [clojure.string :as str]))

;; Reuse actual synthetic runner helpers without replaying the earlier suite.
(with-redefs [test/run-tests (fn [& _] {:fail 0 :error 0})]
  (load-file "tools/ci/progress_test.clj"))
(require '[ci.progress-test :as fixture] '[gravity.development-test-runner :as runner])

(deftest disabled-nested-public-invocation-isolates-active-and-failed-outer-state
  (doseq [failed? [false true] timing? [false true]]
    (let [outer (#'runner/progress-state) error (Exception. "outer-only")
          sink (fixture/writer-fixture nil nil nil) formatting (atom 0)
          original-pr pr-str]
      (when failed? (.add ^java.util.ArrayList (:errors outer) error))
      (let [r (with-bindings {#'runner/*progress-state* outer}
                (with-redefs [clojure.core/pr-str (fn [& args] (swap! formatting inc) (apply original-pr args))]
                  (fixture/exercise {:timing? timing? :writer (:writer sink)})))]
        (is (= 0 (:result r))) (is (= [:load 'a 'b] (:events r)))
        (is (= (if timing? 1 0) (:timings r)))
        (is (= 1 (count (:records r))))
        (is (= :passed (:outcome (first (:records r)))))
        (is (empty? @(:calls sink))) (is (empty? (str (:text sink))))
        (is (zero? @formatting))
        (is (= (if failed? 1 0) (.size ^java.util.ArrayList (:errors outer))))
        (when failed? (is (identical? error (.get ^java.util.ArrayList (:errors outer) 0))))))))

(deftest enabled-inner-invocation-has-own-state
  (doseq [failed? [false true]]
    (let [outer (#'runner/progress-state) original (Exception. "outer-only")
          sink (fixture/writer-fixture nil nil nil)]
      (when failed? (.add ^java.util.ArrayList (:errors outer) original))
      (let [r (with-bindings {#'runner/*progress-state* outer}
                (fixture/exercise {:progress? true :timing? true :writer (:writer sink)}))
            rows (mapv edn/read-string (str/split-lines (str (:text sink))))]
        (is (= 0 (:result r))) (is (= 12 (count rows)))
        (is (= 1 (:timings r) (count (:records r))))
        (is (= (if failed? 1 0) (.size ^java.util.ArrayList (:errors outer))))
        (when failed? (is (identical? original (.get ^java.util.ArrayList (:errors outer) 0))))))))

(deftest failed-enabled-inner-does-not-poison-outer
  (let [outer (#'runner/progress-state) original (Exception. "inner-only")
        sink (fixture/writer-fixture :enter :write original)
        r (with-bindings {#'runner/*progress-state* outer}
            (fixture/exercise {:progress? true :writer (:writer sink)}))]
    (is (identical? original (:result r)))
    (is (= [:load 'a 'b] (:events r)))
    (is (empty? (:records r)))
    (is (.isEmpty ^java.util.ArrayList (:errors outer)))))

(defn check! []
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (let [r (run-tests 'ci.progress-isolation-test)]
    (assert (nil? (find-ns 'gravity.bootstrap)))
    (when (pos? (+ (:fail r) (:error r))) (System/exit 1))))

(if (= ["--old-branch"] *command-line-args*)
  (with-redefs-fn {#'runner/run-cli!
                   (fn [args]
                     (let [options (#'runner/parse-args args)]
                       (if (:progress? options)
                         (#'runner/run-with-progress! (#'runner/progress-state)
                                                      #(#'runner/run-cli-with-options! options))
                         (#'runner/run-cli-with-options! options))))}
    check!)
  (check!))
