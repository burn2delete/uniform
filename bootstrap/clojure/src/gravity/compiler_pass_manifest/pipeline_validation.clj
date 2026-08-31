(ns gravity.compiler-pass-manifest.pipeline-validation
  "Canonical pipeline and pass contract validation."
  (:require [clojure.set :as set]
            [gravity.compiler-pass-manifest.contracts :as contracts]
            [gravity.compiler-pass-manifest.failures :as failures]))

(defn compiler-pass-validate-pipeline!
  [source-path manifest suite]
  (let [stage-order (:stage-order suite)
        contracts (:contracts suite)
        contracts-by-pass (into {} (map (juxt :pass identity) contracts))]
    (when-not (= contracts/compiler-pass-default-stage-order stage-order)
      (failures/compiler-pass-fail! "C1-PIPELINE" source-path manifest
                           {:stage :pipeline-order}
                           {:expected-outcome contracts/compiler-pass-default-stage-order
                            :actual-outcome stage-order
                            :remediation "Expose the D1/C1 canonical pipeline order as pass manifest data."}))
    (doseq [stage stage-order]
      (when-not (contains? contracts-by-pass stage)
        (failures/compiler-pass-fail! "C1-PASS-CONTRACT" source-path manifest
                             {:pass stage}
                             {:missing-fields [:contract]
                              :remediation "Add a pass contract for every exposed pipeline stage."})))
    (doseq [contract contracts]
      (let [missing-fields (failures/compiler-pass-missing-fields
                            contract contracts/compiler-pass-contract-required-fields)]
        (when (seq missing-fields)
          (failures/compiler-pass-fail! "C1-PASS-CONTRACT" source-path manifest contract
                               {:missing-fields missing-fields
                                :remediation "Every compiler pass must declare input, output, facts, capabilities, artifacts, verifier gate, risk, and evidence class."})))
      (let [durable-drops (set/intersection contracts/compiler-pass-durable-facts
                                            (set (:invalidates contract)))
            replacements (set (concat (:regenerates contract)
                                      (:replacement-evidence contract)
                                      (:emits contract)))
            missing-replacements (set/difference durable-drops replacements)]
        (when (seq missing-replacements)
          (failures/compiler-pass-fail! "C1-EVIDENCE-DROP" source-path manifest
                               contract
                               {:missing-fields (vec missing-replacements)
                                :remediation "Regenerate durable facts, emit replacement proof, keep runtime checks, or reject the transformation."}))))
    (let [lower-target (get contracts-by-pass :lower-target)]
      (when (contains? #{:raw-source :source-forms :syntax-objects
                        :expanded-syntax :unchecked-core :gravity/mir}
                      (:input lower-target))
        (failures/compiler-pass-fail! "C1-UNCHECKED-BACKEND" source-path manifest
                             lower-target
                             {:remediation "Target lowering must consume verified MIR or verified domain IR."})))
    (let [pipeline (:pipeline-manifest suite)
          missing-fields (failures/compiler-pass-missing-fields
                          pipeline
                          [:artifact :pipeline-id :compiler :source-root
                           :profile :target :stages :pass-contracts
                           :evidence :diagnostics :artifact-graph])]
      (when (seq missing-fields)
        (failures/compiler-pass-fail! "C1-MANIFEST" source-path manifest pipeline
                             {:missing-fields missing-fields
                              :remediation "Emit a complete compiler pipeline manifest for diagnostics, caches, packages, and bootstrap comparison."}))))
  :complete)
