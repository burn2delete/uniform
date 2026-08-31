(ns gravity.p18.t00.parity
  "Pure P18-T00 accepted and rejected command parity projections."
  (:require [clojure.string :as str]
            [gravity.p18.t00.semantics :as semantics]))

(def ^:private path-specific-summary-keys
  [:source-path :source-extension :source-kind :source-unit-path
   :source-unit-kind :reader-source-path :reader-source-map-path])

(defn- semantic-core
  [summary]
  (apply dissoc summary path-specific-summary-keys))

(defn accepted-extension-record
  [{:keys [gravity qst expected-stdout bootstrap-module release-module]}
   {:keys [gravity-summary qst-summary gravity-check qst-check
           gravity-run qst-run gravity-run-compiled qst-run-compiled
           gravity-compile qst-compile gravity-compile-artifact
           qst-compile-artifact gravity-exec qst-exec
           release-gravity-check release-qst-check release-gravity-run
           release-qst-run release-gravity-compile release-qst-compile
           release-gravity-source-path release-qst-source-path
           release-gravity-exec release-qst-exec results]}]
  (let [semantic-equivalent?
        (= (semantic-core gravity-summary) (semantic-core qst-summary))
        provenance-preserved?
        (and (= gravity (get-in gravity-compile-artifact [:source :path]))
             (= qst (get-in qst-compile-artifact [:source :path]))
             (= gravity release-gravity-source-path)
             (= qst release-qst-source-path)
             (= gravity (:source-unit-path gravity-summary))
             (= qst (:source-unit-path qst-summary))
             (= gravity (:reader-source-map-path gravity-summary))
             (= qst (:reader-source-map-path qst-summary)))]
    {:gravity-source gravity
     :qst-source qst
     :expected-stdout expected-stdout
     :bootstrap-module bootstrap-module
     :release-module release-module
     :gravity-summary gravity-summary
     :qst-summary qst-summary
     :bootstrap-check-parity?
     (and (zero? (:exit gravity-check))
          (zero? (:exit qst-check))
          (= (:out gravity-check)
             (str "gravity stage0 check passed: " bootstrap-module "\n"))
          (= (:out qst-check) (:out gravity-check)))
     :bootstrap-run-parity?
     (and (= expected-stdout (:out gravity-run) (:out qst-run))
          (zero? (:exit gravity-run))
          (zero? (:exit qst-run)))
     :bootstrap-compile-parity?
     (and (zero? (:exit gravity-compile))
          (zero? (:exit qst-compile))
          (= expected-stdout (:out gravity-exec) (:out qst-exec)))
     :bootstrap-run-compiled-parity?
     (and (= expected-stdout
             (:out gravity-run-compiled)
             (:out qst-run-compiled))
          (zero? (:exit gravity-run-compiled))
          (zero? (:exit qst-run-compiled)))
     :release-check-parity?
     (and (zero? (:exit release-gravity-check))
          (zero? (:exit release-qst-check))
          (str/includes? (:out release-gravity-check) " check passed: ")
          (= (:out release-qst-check) (:out release-gravity-check)))
     :release-run-parity?
     (and (= expected-stdout
             (:out release-gravity-run)
             (:out release-qst-run))
          (zero? (:exit release-gravity-run))
          (zero? (:exit release-qst-run)))
     :release-compile-parity?
     (and (zero? (:exit release-gravity-compile))
          (zero? (:exit release-qst-compile))
          (= expected-stdout
             (:out release-gravity-exec)
             (:out release-qst-exec)))
     :semantic-equivalent? semantic-equivalent?
     :provenance-preserves-actual-extension? provenance-preserved?
     :no-deprecation-or-compatibility-warning?
     (not-any? semantics/output-has-warning? results)
     :bootstrap-compile-provenance
     {:gravity (get-in gravity-compile-artifact [:source :path])
      :qst (get-in qst-compile-artifact [:source :path])}
     :release-compile-provenance
     {:gravity release-gravity-source-path
      :qst release-qst-source-path}
     :matches-expected?
     (and semantic-equivalent?
          provenance-preserved?
          (not-any? semantics/output-has-warning? results)
          (every? zero? (map :exit results)))}))

(defn rejected-extension-record
  [{:keys [gravity qst expected-diagnostic]}
   {:keys [bootstrap-gravity bootstrap-qst release-gravity release-qst]
    :as results-by-command}]
  (let [results (mapv results-by-command
                      [:bootstrap-gravity :bootstrap-qst
                       :release-gravity :release-qst])]
    {:gravity-source gravity
     :qst-source qst
     :expected-diagnostic expected-diagnostic
     :bootstrap-diagnostic-parity?
     (and (= 1 (:exit bootstrap-gravity) (:exit bootstrap-qst))
          (str/includes? (:err bootstrap-gravity) expected-diagnostic)
          (str/includes? (:err bootstrap-qst) expected-diagnostic))
     :release-diagnostic-parity?
     (and (= 1 (:exit release-gravity) (:exit release-qst))
          (str/includes? (:err release-gravity) expected-diagnostic)
          (str/includes? (:err release-qst) expected-diagnostic))
     :no-deprecation-or-compatibility-warning?
     (not-any? semantics/output-has-warning? results)
     :matches-expected?
     (and (= 1 (:exit bootstrap-gravity) (:exit bootstrap-qst)
             (:exit release-gravity) (:exit release-qst))
          (every? #(str/includes? (:err %) expected-diagnostic) results)
          (not-any? semantics/output-has-warning? results))}))
