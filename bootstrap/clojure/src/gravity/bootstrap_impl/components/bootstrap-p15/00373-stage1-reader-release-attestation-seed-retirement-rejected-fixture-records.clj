

(def stage1-reader-release-attestation-seed-retirement-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1REL001"
      :rejected-behavior :missing-gravity-reader-release-attestation-seed-retirement}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REL002"
      :rejected-behavior :unsupported-release-attestation-operation}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REL003"
      :rejected-behavior :missing-reader-release-attestation-record}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REL004"
      :rejected-behavior :missing-reader-seed-retirement-evidence}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1REL005"
      :rejected-behavior :release-custody-nonreproducible}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REL006"
      :rejected-behavior :release-supply-chain-manifest-unverifiable}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REL007"
      :rejected-behavior :release-governance-approval-missing}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REL008"
      :rejected-behavior :release-attestation-physical-supply-chain-fallback}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REL009"
      :rejected-behavior :release-input-revoked}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REL010"
      :rejected-behavior :invalid-reader-release-attestation-seed-retirement}])))

(def stage1-reader-formal-release-governance-seed-retirement-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1GOV001"
      :rejected-behavior :missing-gravity-reader-formal-release-governance}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1GOV002"
      :rejected-behavior :unsupported-formal-release-governance-operation}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1GOV003"
      :rejected-behavior :missing-formal-release-governance-record}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1GOV004"
      :rejected-behavior :deployment-custody-unverifiable}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1GOV005"
      :rejected-behavior :self-hosting-evidence-missing}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1GOV006"
      :rejected-behavior :full-compiler-rebuild-unreproducible}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1GOV007"
      :rejected-behavior :stage-compiler-equivalence-missing}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1GOV008"
      :rejected-behavior :tcb-delta-missing}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1GOV009"
      :rejected-behavior :formal-governance-deployment-fallback}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1GOV010"
      :rejected-behavior :invalid-reader-formal-release-governance}])))

(defn stage1-reader-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-pipeline-diagnostic-messages
              id
              "stage1 reader pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-pipeline
                 :diagnostic-family :stage1-reader-pipeline
                 :value value
                 :remediation "Keep the Gravity reader pipeline explicit, preserve token-stream provenance, and prove form parity before replacing the remaining Clojure seed primitives."}
                data)))

(defn stage1-reader-character-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-character-pipeline-diagnostic-messages
              id
              "stage1 reader character pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-character-pipeline
                 :diagnostic-family :stage1-reader-character-pipeline
                 :value value
                 :remediation "Keep the Gravity reader character pipeline explicit, preserve character and token provenance, and prove form parity before replacing the remaining Clojure seed primitives."}
                data)))

(defn stage1-reader-token-classifier-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-token-classifier-pipeline-diagnostic-messages
              id
              "stage1 reader token-classifier pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-token-classifier-pipeline
                 :diagnostic-family :stage1-reader-token-classifier-pipeline
                 :value value
                 :remediation "Keep the Gravity reader token classifier explicit, preserve character/token provenance, and prove form parity before replacing the remaining Clojure seed primitives."}
                data)))

(defn stage1-reader-token-realizer-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-token-realizer-pipeline-diagnostic-messages
              id
              "stage1 reader token-realizer pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-token-realizer-pipeline
                 :diagnostic-family :stage1-reader-token-realizer-pipeline
                 :value value
                 :remediation "Keep the Gravity reader token realizer explicit, preserve character/token provenance, and prove form parity before replacing the remaining Clojure seed primitives."}
                data)))

(defn stage1-reader-token-automaton-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-token-automaton-pipeline-diagnostic-messages
              id
              "stage1 reader token-automaton pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-token-automaton-pipeline
                 :diagnostic-family :stage1-reader-token-automaton-pipeline
                 :value value
                 :remediation "Keep the Gravity reader token automaton explicit, preserve character/token provenance, and prove form parity before replacing the remaining Clojure seed primitives."}
                data)))

(defn stage1-reader-form-builder-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-form-builder-pipeline-diagnostic-messages
              id
              "stage1 reader form-builder pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-form-builder-pipeline
                 :diagnostic-family :stage1-reader-form-builder-pipeline
                 :value value
                 :remediation "Keep the Gravity reader form builder explicit, preserve token/form provenance, and prove form parity before replacing the remaining Clojure seed primitives."}
                data)))

(defn stage1-reader-executor-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-executor-pipeline-diagnostic-messages
              id
              "stage1 reader executor pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-executor-pipeline
                 :diagnostic-family :stage1-reader-executor-pipeline
                 :value value
                 :remediation "Keep reader-specific token automaton and form-builder executor logic in Gravity-owned source, preserve token/form provenance, and prove form parity before retiring the Clojure seed evaluator."}
                data)))

(defn stage1-reader-runtime-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-runtime-pipeline-diagnostic-messages
              id
              "stage1 reader runtime pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-runtime-pipeline
                 :diagnostic-family :stage1-reader-runtime-pipeline
                 :value value
                 :remediation "Keep source-character runtime ownership in Gravity source, preserve reader provenance and stage0 parity, and keep the remaining Clojure interpreter boundary explicit until it is replaced."}
                data)))

(defn stage1-reader-compiled-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-compiled-pipeline-diagnostic-messages
              id
              "stage1 reader compiled pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-compiled-pipeline
                 :diagnostic-family :stage1-reader-compiled-pipeline
                 :value value
                 :remediation "Keep the reader pipeline compiled into a Gravity-owned program, preserve provenance and stage0 parity, and keep the remaining Clojure instruction executor explicit until it is replaced."}
                data)))

(defn stage1-reader-binary-pipeline-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-binary-pipeline-diagnostic-messages
              id
              "stage1 reader binary pipeline execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-binary-pipeline
                 :diagnostic-family :stage1-reader-binary-pipeline
                 :value value
                 :remediation "Keep the reader emitted binary in Gravity-owned source, preserve provenance and stage0 parity, and keep the remaining Clojure binary runner boundary explicit until it is replaced."}
                data)))

(defn stage1-reader-self-hosted-runtime-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-self-hosted-runtime-diagnostic-messages
              id
              "stage1 reader self-hosted runtime execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-self-hosted-runtime
                 :diagnostic-family :stage1-reader-self-hosted-runtime
                 :value value
                 :remediation "Keep the self-hosted reader runtime in Gravity-owned source, preserve provenance and stage0 parity, and keep any remaining Clojure seed builtin boundary explicit until it is replaced."}
                data)))

(defn stage1-reader-core-bootstrap-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-core-bootstrap-diagnostic-messages
              id
              "stage1 reader core-bootstrap execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-core-bootstrap
                 :diagnostic-family :stage1-reader-core-bootstrap
                 :value value
                 :remediation "Keep core bootstrap builtin semantics in Gravity-owned source, reject host fallback, preserve provenance and stage0 parity, and keep remaining Clojure seed orchestration explicit until it is replaced."}
                data)))