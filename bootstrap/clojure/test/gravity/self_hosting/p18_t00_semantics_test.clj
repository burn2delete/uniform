(ns gravity.self-hosting.p18-t00-semantics-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.p18.t00.parity :as parity]
            [gravity.p18.t00.semantics :as semantics]
            [gravity.p18.t04.semantics :as t04-semantics]))

(def fixture
  {:gravity "module.gravity"
   :qst "module.qst"
   :expected-stdout "ok\n"
   :bootstrap-module 'sample.main
   :release-module "sample"})

(defn- summary
  [path extension kind]
  {:source-path path
   :source-extension extension
   :source-kind kind
   :recognized-source? true
   :source-unit-path path
   :source-unit-kind kind
   :reader-source-path path
   :reader-source-map-path path
   :module {:name 'sample.main}
   :syntax-count 2
   :compiled-kind :gravity/compiled-module})

(def gravity-summary
  (summary "module.gravity" ".gravity" :gravity-branded-source))

(def qst-summary
  (summary "module.qst" ".qst" :qst-theory-source))

(defn- successful-inputs
  []
  (let [check {:exit 0
               :out "gravity stage0 check passed: sample.main\n"
               :err ""}
        execution {:exit 0 :out "ok\n" :err ""}
        compile-result {:exit 0 :out "artifact\n" :err ""}
        results [check check execution execution execution execution
                 compile-result compile-result execution execution
                 check check execution execution compile-result
                 compile-result execution execution]]
    {:gravity-summary gravity-summary
     :qst-summary qst-summary
     :gravity-check check
     :qst-check check
     :gravity-run execution
     :qst-run execution
     :gravity-run-compiled execution
     :qst-run-compiled execution
     :gravity-compile compile-result
     :qst-compile compile-result
     :gravity-compile-artifact {:source {:path "module.gravity"}}
     :qst-compile-artifact {:source {:path "module.qst"}}
     :gravity-exec execution
     :qst-exec execution
     :release-gravity-check check
     :release-qst-check check
     :release-gravity-run execution
     :release-qst-run execution
     :release-gravity-compile compile-result
     :release-qst-compile compile-result
     :release-gravity-source-path "module.gravity"
     :release-qst-source-path "module.qst"
     :release-gravity-exec execution
     :release-qst-exec execution
     :results results}))

(deftest constants-and-pure-bootstrap-wrappers-retain-exact-values
  (is (= semantics/artifact-dir bootstrap/p18-t00-artifact-dir))
  (is (= semantics/report-path bootstrap/p18-t00-report-path))
  (is (= semantics/accepted-extension-fixtures
         bootstrap/p18-t00-accepted-extension-fixtures))
  (is (= semantics/rejected-extension-fixtures
         bootstrap/p18-t00-rejected-extension-fixtures))
  (doseq [result [{:out "clean" :err ""}
                  {:out "compatibility warning" :err ""}
                  {:out "" :err "deprecated route"}]]
    (is (= (semantics/output-has-warning? result)
           (bootstrap/p18-t00-output-has-warning? result))))
  (doseq [artifact [{:source-path "direct.gravity"}
                    {:source {:path "nested.qst"}}]]
    (is (= (semantics/compile-artifact-source-path artifact)
           (bootstrap/p18-t00-compile-artifact-source-path artifact))))
  (is (= '([result])
         (:arglists (meta #'bootstrap/p18-t00-output-has-warning?))))
  (is (= '([artifact])
         (:arglists
          (meta #'bootstrap/p18-t00-compile-artifact-source-path)))))

(deftest semantic-summary-wrapper-preserves-effect-boundary-and-value
  (let [compile-artifact
        {:kind :gravity/compiled-module
         :module {:name 'sample.main :source-path "module.gravity"}
         :syntax-object-stream [{:id 1} {:id 2}]}
        reader-artifact
        {:source {:path "module.gravity"}
         :syntax-object-stream
         [{:span {:source "module.gravity"}}]}
        source-unit {:path "module.gravity"
                     :source-kind :gravity-branded-source}
        expected
        (semantics/semantic-summary
         {:path "module.gravity"
          :compile-artifact compile-artifact
          :reader-artifact reader-artifact
          :source-unit source-unit
          :source-extension ".gravity"
          :source-kind :gravity-branded-source
          :recognized-source? true})]
    (with-redefs [clojure.core/slurp (fn [_] "source")
                  bootstrap/compile-source
                  (fn [_ _] compile-artifact)
                  bootstrap/read-source-artifact
                  (fn [_ _] reader-artifact)
                  bootstrap/c2-source-unit-record
                  (fn [_ _ _] source-unit)
                  bootstrap/gravity-source-extension (constantly ".gravity")
                  bootstrap/gravity-source-kind
                  (constantly :gravity-branded-source)
                  bootstrap/qst-or-gravity-source? (constantly true)]
      (is (= expected
             (bootstrap/p18-t00-semantic-summary "module.gravity"))))))

(deftest accepted-and-rejected-parity-projections-are-pure
  (let [accepted (parity/accepted-extension-record
                  fixture (successful-inputs))]
    (doseq [key [:bootstrap-check-parity? :bootstrap-run-parity?
                 :bootstrap-compile-parity?
                 :bootstrap-run-compiled-parity? :release-check-parity?
                 :release-run-parity? :release-compile-parity?
                 :semantic-equivalent?
                 :provenance-preserves-actual-extension?
                 :no-deprecation-or-compatibility-warning?
                 :matches-expected?]]
      (is (true? (get accepted key)) key))
    (is (= {:gravity "module.gravity" :qst "module.qst"}
           (:bootstrap-compile-provenance accepted))))
  (let [rejection {:exit 1 :out "" :err "RULE rejected"}
        rejected (parity/rejected-extension-record
                  {:gravity "bad.gravity"
                   :qst "bad.qst"
                   :expected-diagnostic "RULE"}
                  {:bootstrap-gravity rejection
                   :bootstrap-qst rejection
                   :release-gravity rejection
                   :release-qst rejection})]
    (is (true? (:bootstrap-diagnostic-parity? rejected)))
    (is (true? (:release-diagnostic-parity? rejected)))
    (is (true? (:matches-expected? rejected)))))

(deftest proof-and-report-wrappers-match-pure-leaf
  (let [accepted (parity/accepted-extension-record fixture
                                                   (successful-inputs))
        rejected (assoc accepted :matches-expected? true)
        artifact {:accepted-extension-parity [accepted]
                  :rejected-extension-parity [rejected]}
        context {:co-canonical-source-extensions #{".qst" ".gravity"}
                 :accepted-extension-fixtures [fixture]
                 :qst-source-kind :qst-theory-source
                 :gravity-source-kind :gravity-branded-source}
        expected (semantics/capability-proof artifact context)
        bootstrap-proof
        (with-redefs [bootstrap/co-canonical-source-extensions
                      #{".qst" ".gravity"}
                      bootstrap/p18-t00-accepted-extension-fixtures [fixture]
                      bootstrap/gravity-source-kind
                      #(if (.endsWith ^String % ".qst")
                         :qst-theory-source
                         :gravity-branded-source)]
          (bootstrap/p18-t00-capability-proof artifact))
        report-artifact {:capability-based-proof expected}]
    (is (= expected bootstrap-proof))
    (is (= (semantics/report-markdown report-artifact
                                      semantics/artifact-dir)
           (bootstrap/p18-t00-report-markdown report-artifact))))
  (doseq [namespace ['gravity.p18.t00.semantics
                     'gravity.p18.t00.parity]]
    (is (not-any? #{'gravity.bootstrap}
                  (map ns-name (vals (ns-aliases namespace)))))))

(deftest p18-t04-completeness-and-proof-wrappers-match-pure-leaf
  (let [p15 {:status :complete
             :artifact-id "sha256:p15"
             :full-language-compiler-self-hosted? true
             :clojure-seed-retired? true
             :clojure-seed-boundary? false}
        p18 {:status :complete
             :artifact-id "sha256:p18"
             :final-release? true
             :seedless-release? true
             :clojure-seed-boundary? false
             :capability-based-proof {:clojure-seed-boundary? false}}
        artifact {:status :complete
                  :compiler-source {:path "compiler.gravity"
                                    :extension ".gravity"
                                    :deprecation-warning? false}
                  :p15-final-seed-retirement-proof p15
                  :p18-final-release-proof p18
                  :diagnostics []}
        context {:artifact artifact
                 :complete? true
                 :source-path "compiler.gravity"
                 :source-extension (constantly ".gravity")
                 :p15-final-proof p15
                 :p18-final-proof p18
                 :diagnostics []}]
    (is (true? (t04-semantics/complete? p15 p18)))
    (is (= (t04-semantics/complete? p15 p18)
           (bootstrap/p18-t04-self-host-verify-complete? p15 p18)))
    (with-redefs [bootstrap/p15-s23-compiler-source-path "compiler.gravity"
                  bootstrap/gravity-source-extension (constantly ".gravity")]
      (is (= (t04-semantics/proof context)
             (bootstrap/p18-t04-public-self-host-verify-proof artifact))))))
