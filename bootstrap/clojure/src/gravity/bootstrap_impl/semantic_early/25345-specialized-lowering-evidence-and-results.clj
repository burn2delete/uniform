; Semantic decomposition of HEAD reader line 25345.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-specialized-lowering-evidence-and-results
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
     :conformance
     :policy]}
   :backend-conformance-record
   {:artifact :gravity/backend-conformance-record,
    :suite :p07-t04-specialized-lowering,
    :status :passed,
    :positive-lowering-results
    [{:backend :gpu, :fixture :specialized-positive, :status :passed}
     {:backend :hdl, :fixture :specialized-positive, :status :passed}
     {:backend :workflow-graph,
      :fixture :specialized-positive,
      :status :passed}
     {:backend :query-relational,
      :fixture :specialized-positive,
      :status :passed}
     {:backend :mobile,
      :fixture :specialized-positive,
      :status :passed}],
    :negative-diagnostic-results
    (mapv
     (fn [id] {:diagnostic id, :status :matched})
     specialized-lowering-diagnostic-ids),
    :differential-results
    [{:backend :gpu,
      :comparison :artifact-shape-vs-domain-anchor,
      :status :matched}
     {:backend :hdl,
      :comparison :simulation-trace-schema,
      :status :matched}
     {:backend :workflow-graph,
      :comparison :replay-policy,
      :status :matched}
     {:backend :query-relational,
      :comparison :prepared-plan,
      :status :matched}
     {:backend :mobile,
      :comparison :simulator-record-shape,
      :status :matched}],
    :target-availability
    {:gpu-toolchain :not-required-for-stage0,
     :hdl-synthesis :not-required-for-stage0,
     :workflow-runtime :not-required-for-stage0,
     :database :not-required-for-stage0,
     :mobile-simulator :not-required-for-stage0},
    :evidence-pack "backend-conformance-pack:p07-t04"}
   :specialized-diagnostic-stream
   diagnostic-stream
   :specialized-lowering-results
   {:conformance-status :complete,
    :task "P07-T04",
    :artifact-emission-status :complete,
    :diagnostic-status :complete,
    :mobile-backend-status :complete,
    :gpu-backend-status :complete,
    :backend-interface-input-status :complete,
    :hdl-backend-status :complete,
    :documents ["B8" "B9" "B10" "B11" "B12" "B13" "B14"],
    :metadata-status :complete,
    :status :complete,
    :workflow-backend-status :complete,
    :query-backend-status :complete}
   :diagnostics
   [])))
