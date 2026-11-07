(ns gravity.c9-ownership-checker.artifact
  "Artifact assembly for the hosted C9 ownership-checker facade."
  (:require [gravity.ownership :as ownership]))

(defn source-artifact [ops source-path source-text]
  (let [{:keys [read-source-form-records validate-ns-syntax! parse-module
                c9-ownership-source-overrides c9-ownership-validate-overrides!
                compiler-c8-effect-source-artifact c9-ownership-graph c9-borrow-graph
                c9-lifetime-interval-map c9-escape-analysis-report
                c9-region-lifetime-graph c9-arena-generation-graph
                c9-linear-resource-flow-graph c9-transfer-records
                c9-runtime-check-records c9-unsafe-audit-references
                c9-ownership-diagnostics c9-ownership-verifier-report
                c9-ownership-validate! c9-ownership-capability-proof c4-artifact-id
                c9-ownership-governing-document c9-ownership-diagnostic-ids]} ops
        records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (c9-ownership-source-overrides module)
        _ (c9-ownership-validate-overrides! source-path module overrides)
        c8-artifact (compiler-c8-effect-source-artifact source-path source-text)
        effect-graph (:effect-graph c8-artifact)
        ownership (c9-ownership-graph module effect-graph)
        borrow (c9-borrow-graph module effect-graph)
        lifetimes (c9-lifetime-interval-map module)
        moves (select-keys ownership [:moves :consumes])
        escape (c9-escape-analysis-report module)
        region (c9-region-lifetime-graph module)
        arena (c9-arena-generation-graph module)
        linear (c9-linear-resource-flow-graph module)
        transfer (c9-transfer-records module)
        runtime (c9-runtime-check-records module)
        unsafe (c9-unsafe-audit-references module)
        diagnostics (c9-ownership-diagnostics source-path ownership)
        verifier (c9-ownership-verifier-report c8-artifact ownership borrow lifetimes moves
                                               escape region arena linear transfer runtime unsafe diagnostics)]
    (:artifact
 (ownership/native-artifact-projection
  {:validate! c9-ownership-validate!,
   :capability-proof c9-ownership-capability-proof,
   :artifact-id c4-artifact-id,
   :governing-document c9-ownership-governing-document,
   :diagnostic-ids c9-ownership-diagnostic-ids}
  source-path
  module
  overrides
  c8-artifact
  ownership
  borrow
  lifetimes
  moves
  escape
  region
  arena
  linear
  transfer
  runtime
  unsafe
  diagnostics
  verifier))))

(defn file-artifact [source-artifact path]
  (source-artifact path (slurp path)))
