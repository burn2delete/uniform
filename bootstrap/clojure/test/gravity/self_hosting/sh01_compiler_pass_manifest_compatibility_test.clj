(ns gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.compiler-pass-manifest :as manifest]))

(def upstream-artifact
  {:kind :gravity/stage0-math-conformance-artifact
   :profile-manifest {:metadata {}}})

(def facade-functions
  '[compiler-pass-contract
    compiler-pass-default-risk-classification
    compiler-pass-default-trust-report
    compiler-pass-merge-record-overrides
    compiler-pass-suite
    compiler-pass-fail!
    compiler-pass-missing-fields
    compiler-pass-validate-pipeline!
    compiler-pass-validate-diagnostics!
    compiler-pass-validate-incremental!
    compiler-pass-validate-plugins!
    compiler-pass-validate-verification!
    compiler-pass-capability-proof])

(defn diagnostic-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(deftest gravity-bootstrap-preserves-the-compiler-pass-manifest-api
  (is (= manifest/compiler-pass-default-stage-order
         bootstrap/compiler-pass-default-stage-order))
  (is (= manifest/compiler-pass-default-contracts
         bootstrap/compiler-pass-default-contracts))
  (is (= manifest/compiler-pass-diagnostic-ids
         bootstrap/compiler-pass-diagnostic-ids))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-pass-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-pass-file-artifact))))
  (doseq [function-name facade-functions]
    (let [leaf-var (ns-resolve 'gravity.compiler-pass-manifest function-name)
          facade-var (ns-resolve 'gravity.bootstrap function-name)]
      (is (some? leaf-var) function-name)
      (is (some? facade-var) function-name)
      (is (= (:arglists (meta leaf-var))
             (:arglists (meta facade-var)))
          function-name)))
  (is (= (manifest/compiler-pass-suite {})
         (bootstrap/compiler-pass-suite {})))
  (let [direct (manifest/compiler-pass-source-artifact-from-upstream
                "compiler-passes.gravity" upstream-artifact)
        facade (with-redefs [bootstrap/math-conformance-source-artifact
                             (fn [_ _] upstream-artifact)]
                 (bootstrap/compiler-pass-source-artifact
                  "compiler-passes.gravity" "source"))]
    (is (= direct facade)))
  (let [sentinel {:kind :compiler-pass-file-sentinel}]
    (is (= sentinel
           (with-redefs [bootstrap/compiler-pass-source-artifact
                         (fn [_ _] sentinel)]
             (bootstrap/compiler-pass-file-artifact
              "bootstrap/clojure/fixtures/accepted/compiler-passes.gravity")))))
  (let [suite (assoc (manifest/compiler-pass-suite {})
                     :proof-reuse-records
                     [{:proof-id :stale :status :stale :reuse :accepted}])
        args ["compiler-passes.gravity" {} suite]]
    (is (= (select-keys
            (diagnostic-data
             #(apply manifest/compiler-pass-validate-incremental! args))
            [:id :message :bootstrap-stage :stage :diagnostic-family])
           (select-keys
            (diagnostic-data
             #(apply bootstrap/compiler-pass-validate-incremental! args))
            [:id :message :bootstrap-stage :stage :diagnostic-family])))))
