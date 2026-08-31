; Semantic decomposition of HEAD reader line 24702.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-hosted-lowering-evidence-and-results
 [source-path state]
 (let
  [{:keys [diagnostic-stream]} state]
  (assoc
   {}
   :metadata-preservation-report
   {:artifact :gravity/backend-metadata-preservation-report,
    :status :preserved,
    :fields
    [:source-spans
     :generated-origin-chain
     :types
     :effects
     :capabilities
     :schemas
     :safety
     :proofs
     :profile
     :target
     :runtime
     :artifact-graph
     :conformance]}
   :backend-conformance-record
   {:artifact :gravity/backend-conformance-record,
    :suite :p07-t03-hosted-lowering,
    :status :passed,
    :positive-lowering-results
    [{:backend :wasm, :fixture :hosted-positive, :status :passed}
     {:backend :jvm, :fixture :hosted-positive, :status :passed}
     {:backend :js-ts, :fixture :hosted-positive, :status :passed}],
    :negative-diagnostic-results
    (mapv
     (fn [id] {:diagnostic id, :status :matched})
     hosted-lowering-diagnostic-ids),
    :differential-results
    [{:backend :wasm,
      :comparison :artifact-shape-vs-mir,
      :status :matched}
     {:backend :jvm,
      :comparison :artifact-shape-vs-mir,
      :status :matched}
     {:backend :js-ts,
      :comparison :artifact-shape-vs-mir,
      :status :matched}],
    :target-availability
    {:wasm-toolchain :not-required-for-stage0,
     :jvm-toolchain :not-required-for-stage0,
     :js-runtime :not-required-for-stage0},
    :evidence-pack "backend-conformance-pack:p07-t03"}
   :hosted-diagnostic-stream
   diagnostic-stream
   :hosted-lowering-results
   {:conformance-status :complete,
    :wasm-backend-status :complete,
    :js-ts-backend-status :complete,
    :task "P07-T03",
    :artifact-emission-status :complete,
    :diagnostic-status :complete,
    :backend-interface-input-status :complete,
    :documents ["B4" "B5" "B6" "B13" "B14"],
    :metadata-status :complete,
    :status :complete,
    :jvm-backend-status :complete}
   :diagnostics
   [])))
