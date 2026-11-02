(require '[clojure.string :as str] '[clojure.test :as test])
(import '[java.nio.file Files] '[java.nio.file.attribute FileAttribute])
(System/setProperty "gravity.ports.controls.load-only" "true")
(def originals ["bootstrap/clojure/test/gravity/development_test_runner.clj"
                "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj"
                "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj"])
(def pristine (mapv slurp originals))
(def props ["gravity.ports.dev-source" "gravity.ports.broker-source" "gravity.ports.runner-source"])
(def mutation-root (Files/createTempDirectory "current-main-ports-mutants-" (make-array FileAttribute 0)))
(def cases
  [{:id :ordinary-guard-bypass :file 2 :from "   (require-supported-namespace! (:namespace job))\n   (run-namespace-process-owned job options)"
    :to "   (run-namespace-process-owned job options)" :control 'current-defaults-and-ordinary-guard}
   {:id :omit-canonical-L :file 2 :from "(fn [] ((:acquire! guards)) (operation))" :to "(fn [] (operation))"
    :control 'selected-order-success-and-correlated-disposition}
   {:id :skip-completion-read :file 2 :from "(read-development-result delivery result)" :to "nil"
    :control 'selected-order-success-and-correlated-disposition}
   {:id :overwrite-completion :file 0 :from "java.nio.file.StandardOpenOption/CREATE_NEW" :to "java.nio.file.StandardOpenOption/CREATE"
    :control 'actual-thin-writer-private-DATAIO-and-refusals}
   {:id :start-without-pending-custody :file 2 :from "custody (broker/retain-process-start-custody!)" :to "custody nil"
    :control 'startup-custody-and-default-interrupt-conservation}
   {:id :release-C-despite-L-failure :file 1
    :from "(and (:complete? terminal) (nil? (:error resources))\n                        (contains? #{:not-acquired :released} (:state resources)))"
    :to "(:complete? terminal)" :control 'selected-busy-unresolved-release-and-raw-error-paths}
   {:id :mask-completion-read-with-close :file 2
    :from "(when-not (identical? original close-error)\n                   (try (.addSuppressed ^Throwable original close-error) (catch Throwable _ nil)))"
    :to "(throw close-error)" :control 'completion-read-primary-and-close-errors-are-not-confused}])
(def results
  (mapv
   (fn [{:keys [id file from to control]}]
     (doseq [p props] (System/clearProperty p))
     ;; Source definitions only; no operation/native C/L/process port executes.
     (load-file (originals 0)) (load-file (originals 1))
     (let [text (pristine file)
           _ (assert (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote from)) text))) (str id))
           path (.resolve mutation-root (str (name id) ".clj"))
           _ (spit (str path) (str/replace text from to))
           _ (System/setProperty (props file) (str path))
           _ (when (< file 2) (load-file (str path)))
           _ (load-file "specs/accounting-removal/current-main-selected-ports/check.clj")
           counts (ref test/*initial-report-counters*)]
       (binding [test/*report-counters* counts] (test/test-vars [(ns-resolve 'user control)]))
       (assert (pos? (+ (:fail @counts) (:error @counts))) (str "Undetected mutation " id))
       {:id id :detected true :counts @counts})) cases))
(prn {:scope :current-main-affected-behavioral-MODEL-sensitivity :results results
      :fixture-root (str mutation-root) :actual-C-L-process-lease-probes 0 :runtime-admission false})
