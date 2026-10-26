(ns gravity.self-hosting.sh07-c6-native-report-join-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.digest :as digest]))

;; C6/D8/D9: the native producer supplies validation, while the Gravity
;; consumer requires the exact successful report. This is a development
;; source-function check, not pinned pipeline or migration evidence.
(def ^:private source-path
  (delay
    (let [resource (io/resource "gravity/self_hosting/sh07_c6_native_report_join_test.clj")]
      (loop [directory (.getParentFile (io/file (.toURI resource)))]
        (if (.isFile (io/file directory "deps.edn"))
          (.getCanonicalPath
           (io/file directory "bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity"))
          (if-let [parent (.getParentFile directory)]
            (recur parent)
            (throw (ex-info "Repository root absent" {}))))))))

(def ^:private plan
  (delay
    (let [emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! @source-path :jvm))]
      (b/p15-s23-stage2-compiler-artifact-plan
       emitter @source-path (slurp @source-path :encoding "UTF-8")))))

(defn- accepts-report? [report]
  (b/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-c6-native-report-join-test :compiler-artifact-plan? true}
   @plan 'c6c10-carrier-validation-shape? [report]))

(deftest actual-native-success-reports-join-current-gravity-consumer
  (doseq [value [nil {:source [1 :name 'symbol]}
                 (vec (range 129))
                 (nth (iterate vector nil) 65)
                 (apply str (repeat 65537 "x"))
                 (.shiftLeft java.math.BigInteger/ONE 256)]]
    (let [report (b/p15-s23-c6c10-carrier-validation @source-path value)]
      (is (= {:status :passed} report))
      (is (true? (accepts-report? report))))))

(deftest failed-missing-and-extended-reports-remain-rejected
  (doseq [report [nil [] {} {:status :failed} {:status :rejected}
                  {:status :passed :reason :unexpected}
                  {:status :passed :maximum-carrier-nodes 65536}
                  {:status :passed :observed-carrier-nodes 1}]]
    (is (false? (accepts-report? report)))))

(deftest actual-canonical-record-retains-form-text-and-digest-identity
  (let [value {:binding ['name 1] :members #{:a :b}}
        record (b/p15-s23-c6c10-canonical-record @source-path value)]
    (is (= #{:form :text} (set (keys record))))
    (is (= (:form record) (b/p15-s23-c6c10-canonical-form* @source-path value)))
    (is (= (str "sha256:" (digest/sha256-hex (:text record)))
           (b/p15-s23-c6c10-canonical-digest @source-path value)))
    (is (not= (:form record)
              (:form (b/p15-s23-c6c10-canonical-record
                      @source-path (assoc value :binding ['name 2])))))))
