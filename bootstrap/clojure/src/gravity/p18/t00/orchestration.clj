(ns gravity.p18.t00.orchestration
  "P18-T00 command, artifact, and report orchestration over injected effects."
  (:require [gravity.p18.t00.parity :as parity]))

(defn accepted-extension-record
  [{:keys [semantic-summary bootstrap-shell release-shell read-edn-stdout
           compile-artifact-source-path]}
   {:keys [gravity qst bootstrap-output-prefix release-output-prefix]
    :as fixture}]
  (let [bootstrap-gravity-output (str bootstrap-output-prefix "-gravity")
        bootstrap-qst-output (str bootstrap-output-prefix "-qst")
        release-gravity-output (str release-output-prefix "-gravity")
        release-qst-output (str release-output-prefix "-qst")
        gravity-summary (semantic-summary gravity)
        qst-summary (semantic-summary qst)
        gravity-check (bootstrap-shell "clojure" "-M:gravity" "check" gravity)
        qst-check (bootstrap-shell "clojure" "-M:gravity" "check" qst)
        gravity-run (bootstrap-shell "clojure" "-M:gravity" "run" gravity)
        qst-run (bootstrap-shell "clojure" "-M:gravity" "run" qst)
        gravity-run-compiled (bootstrap-shell "clojure" "-M:gravity"
                                                "run-compiled" gravity)
        qst-run-compiled (bootstrap-shell "clojure" "-M:gravity"
                                            "run-compiled" qst)
        gravity-compile (bootstrap-shell "clojure" "-M:gravity" "compile"
                                         gravity "-o" bootstrap-gravity-output)
        qst-compile (bootstrap-shell "clojure" "-M:gravity" "compile"
                                     qst "-o" bootstrap-qst-output)
        gravity-compile-artifact (read-edn-stdout gravity-compile)
        qst-compile-artifact (read-edn-stdout qst-compile)
        gravity-exec (bootstrap-shell bootstrap-gravity-output)
        qst-exec (bootstrap-shell bootstrap-qst-output)
        release-gravity-check (release-shell "bin/gravity" "check" gravity)
        release-qst-check (release-shell "bin/gravity" "check" qst)
        release-gravity-run (release-shell "bin/gravity" "run" gravity)
        release-qst-run (release-shell "bin/gravity" "run" qst)
        release-gravity-compile (release-shell "bin/gravity" "compile"
                                               gravity "-o"
                                               release-gravity-output)
        release-qst-compile (release-shell "bin/gravity" "compile"
                                           qst "-o" release-qst-output)
        release-gravity-artifact (read-edn-stdout release-gravity-compile)
        release-qst-artifact (read-edn-stdout release-qst-compile)
        release-gravity-source-path
        (compile-artifact-source-path release-gravity-artifact)
        release-qst-source-path
        (compile-artifact-source-path release-qst-artifact)
        release-gravity-exec (release-shell release-gravity-output)
        release-qst-exec (release-shell release-qst-output)
        results [gravity-check qst-check gravity-run qst-run
                 gravity-run-compiled qst-run-compiled gravity-compile
                 qst-compile gravity-exec qst-exec release-gravity-check
                 release-qst-check release-gravity-run release-qst-run
                 release-gravity-compile release-qst-compile
                 release-gravity-exec release-qst-exec]]
    (parity/accepted-extension-record
     fixture
     {:gravity-summary gravity-summary
      :qst-summary qst-summary
      :gravity-check gravity-check
      :qst-check qst-check
      :gravity-run gravity-run
      :qst-run qst-run
      :gravity-run-compiled gravity-run-compiled
      :qst-run-compiled qst-run-compiled
      :gravity-compile gravity-compile
      :qst-compile qst-compile
      :gravity-compile-artifact gravity-compile-artifact
      :qst-compile-artifact qst-compile-artifact
      :gravity-exec gravity-exec
      :qst-exec qst-exec
      :release-gravity-check release-gravity-check
      :release-qst-check release-qst-check
      :release-gravity-run release-gravity-run
      :release-qst-run release-qst-run
      :release-gravity-compile release-gravity-compile
      :release-qst-compile release-qst-compile
      :release-gravity-source-path release-gravity-source-path
      :release-qst-source-path release-qst-source-path
      :release-gravity-exec release-gravity-exec
      :release-qst-exec release-qst-exec
      :results results})))

(defn rejected-extension-record
  [{:keys [bootstrap-shell release-shell]}
   {:keys [gravity qst output-prefix] :as fixture}]
  (let [bootstrap-gravity
        (bootstrap-shell "clojure" "-M:gravity" "run-compiled" gravity)
        bootstrap-qst
        (bootstrap-shell "clojure" "-M:gravity" "run-compiled" qst)
        release-gravity
        (release-shell "bin/gravity" "compile" gravity
                       "-o" (str output-prefix "-gravity"))
        release-qst
        (release-shell "bin/gravity" "compile" qst
                       "-o" (str output-prefix "-qst"))]
    (parity/rejected-extension-record
     fixture
     {:bootstrap-gravity bootstrap-gravity
      :bootstrap-qst bootstrap-qst
      :release-gravity release-gravity
      :release-qst release-qst})))

(defn co-canonical-source-extensions-artifact!
  [{:keys [write-final-release-artifacts! accepted-fixtures
           rejected-fixtures accepted-extension-record
           rejected-extension-record capability-proof artifact-id]}]
  (write-final-release-artifacts!)
  (let [accepted (mapv accepted-extension-record accepted-fixtures)
        rejected (mapv rejected-extension-record rejected-fixtures)
        artifact-base
        {:kind :gravity/p18-t00-co-canonical-source-extensions-proof
         :task "P18-T00"
         :status :complete
         :phase :binary-distribution-and-seedless-release
         :source-extension-contract
         {:co-canonical-extensions [".qst" ".gravity"]
          :qst-source-kind :qst-theory-source
          :gravity-source-kind :gravity-branded-source
          :both-valid-indefinitely? true
          :deprecation-warnings? false
          :compatibility-warnings? false}
         :accepted-extension-parity accepted
         :rejected-extension-parity rejected
         :governing-documents ["C2" "C15" "PKG3" "PKG10" "PKG12"
                               "T1" "BOOT7" "BOOT8" "D9"]
         :diagnostics []}
        proof (capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (artifact-id (assoc artifact-base :capability-based-proof proof)))))

(defn write-co-canonical-source-extension-artifacts!
  [{:keys [artifact! artifact-dir report-path ensure-dir! report-parent
           write-edn! write-report! report-markdown]}]
  (let [artifact (artifact!)]
    (ensure-dir! artifact-dir)
    (ensure-dir! report-parent)
    (write-edn!
     (str artifact-dir "/p18-t00-co-canonical-source-extensions-proof.edn")
     artifact)
    (write-edn!
     (str artifact-dir "/p18-t00-accepted-extension-parity.edn")
     (:accepted-extension-parity artifact))
    (write-edn!
     (str artifact-dir "/p18-t00-rejected-extension-parity.edn")
     (:rejected-extension-parity artifact))
    (write-report! report-path (report-markdown artifact))
    artifact))
