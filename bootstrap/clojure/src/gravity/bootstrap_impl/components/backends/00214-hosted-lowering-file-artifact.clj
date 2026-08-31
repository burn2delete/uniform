

(defn hosted-lowering-file-artifact
  [path]
  (hosted-lowering-source-artifact path (slurp path)))

(def specialized-lowering-governing-documents
  ["docs/phase-07-backend-architecture/105-b8-gpu-backend-design.md"
   "docs/phase-07-backend-architecture/106-b9-hdl-backend-design.md"
   "docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md"
   "docs/phase-07-backend-architecture/108-b11-query-relational-backend-design.md"
   "docs/phase-07-backend-architecture/109-b12-mobile-backend-design.md"
   "docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md"
   "docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md"
   "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"])

(def specialized-lowering-diagnostic-ids
  ["B8-TARGET"
   "B8-KERNEL"
   "B8-HOST-EFFECT"
   "B8-MEMORY"
   "B8-TRANSFER"
   "B8-SYNC"
   "B8-ATOMIC"
   "B8-LAUNCH"
   "B8-MATH"
   "B8-MANIFEST"
   "B9-TARGET"
   "B9-WIDTH"
   "B9-CLOCK"
   "B9-RESET"
   "B9-CDC"
   "B9-RUNTIME"
   "B9-UNBOUNDED"
   "B9-INTERFACE"
   "B9-TIMING"
   "B9-MANIFEST"
   "B10-SCHEMA"
   "B10-REPLAY"
   "B10-IDEMPOTENCY"
   "B10-RETRY"
   "B10-COMPENSATION"
   "B10-CAPABILITY"
   "B10-POLICY"
   "B10-TAINT"
   "B10-GRAPH"
   "B10-MANIFEST"
   "B11-DIALECT"
   "B11-SCHEMA"
   "B11-TAINT"
   "B11-PARAMETER"
   "B11-CAPABILITY"
   "B11-TRANSACTION"
   "B11-NULL"
   "B11-MIGRATION"
   "B11-RESULT"
   "B11-PLAN"
   "B11-MANIFEST"
   "B12-TARGET"
   "B12-PERMISSION"
   "B12-LIFECYCLE"
   "B12-THREAD"
   "B12-NULL"
   "B12-ERROR"
   "B12-BACKGROUND"
   "B12-STORAGE"
   "B12-RESOURCE"
   "B12-MANIFEST"])

(def specialized-lowering-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             specialized-lowering-diagnostic-ids)))

(defn specialized-lowering-backend-for-diagnostic
  [id]
  (cond
    (str/starts-with? id "B8-") :gravity.backend/gpu
    (str/starts-with? id "B9-") :gravity.backend/hdl
    (str/starts-with? id "B10-") :gravity.backend/workflow-graph
    (str/starts-with? id "B11-") :gravity.backend/query-relational
    :else :gravity.backend/mobile))

(defn specialized-lowering-stage-for-diagnostic
  [id]
  (cond
    (str/starts-with? id "B8-") :gpu-backend
    (str/starts-with? id "B9-") :hdl-backend
    (str/starts-with? id "B10-") :workflow-graph-backend
    (str/starts-with? id "B11-") :query-relational-backend
    :else :mobile-backend))

(defn specialized-lowering-diagnostic-message
  [id]
  (cond
    (str/starts-with? id "B8-") "GPU specialized lowering contract failed"
    (str/starts-with? id "B9-") "HDL specialized lowering contract failed"
    (str/starts-with? id "B10-") "workflow graph specialized lowering contract failed"
    (str/starts-with? id "B11-") "query/relational specialized lowering contract failed"
    :else "mobile specialized lowering contract failed"))

(defn specialized-lowering-source-overrides
  [module]
  (or (get-in module [:metadata :backend :specialized-lowering])
      (get-in module [:metadata :backend :specialized])
      {}))

(defn specialized-lowering-fail!
  [id source-path subject extra]
  (fail! id
         (specialized-lowering-diagnostic-message id)
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :specialized-backend-lowering
                 :stage (or (:stage subject)
                            (specialized-lowering-stage-for-diagnostic id))
                 :backend (or (:backend subject)
                              (specialized-lowering-backend-for-diagnostic id))
                 :profile (or (:profile subject) :hosted)
                 :target (or (:target subject) :specialized-stage0)
                 :artifact-id (:artifact-id subject)
                 :mir-op (or (:mir-op subject) :domain-lowering)
                 :domain-anchor (or (:domain-anchor subject) :domain)
                 :missing-evidence (:missing-evidence subject)
                 :target-construct (:target-construct subject)
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Specialized backends must preserve domain anchors, schemas, effects, capabilities, proof records, source maps, provider/runtime policies, and conformance evidence instead of hiding target behavior in backend-specific artifacts."}
                extra)))

(defn specialized-lowering-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get specialized-lowering-override-diagnostics fail-kind)]
      (specialized-lowering-fail!
       id source-path
       {:stage (specialized-lowering-stage-for-diagnostic id)
        :backend (specialized-lowering-backend-for-diagnostic id)
        :artifact-id (str "specialized-lowering-" (name fail-kind))
        :missing-evidence [fail-kind]
        :target-construct fail-kind}
       {:missing-fields [fail-kind]}))))

(defn specialized-lowering-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/backend-diagnostic-stream
   :stage :specialized-backend-lowering
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage (specialized-lowering-stage-for-diagnostic id)
            :backend (specialized-lowering-backend-for-diagnostic id)
            :message-key (keyword "backend-specialized" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "specialized-backend-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :specialized-stage0
            :mir-op :domain-lowering
            :domain-anchor (case (subs id 0 (.indexOf id "-"))
                             "B8" :gpu-kernel
                             "B9" :hardware-circuit
                             "B10" :workflow-graph
                             "B11" :relational-query
                             "B12" :mobile-app)
            :missing-evidence #{:schema :capability :proof :source-map
                                :conformance}
            :target-construct id
            :fallback-status :rejected
            :facts {:domain-anchor-required? true
                    :schema-required? true
                    :capability-required? true}
            :remediation [{:kind :provide-domain-anchor}
                          {:kind :preserve-schema-and-capability}
                          {:kind :emit-specialized-conformance-evidence}]
            :redactions []
            :ordering-key [id :specialized-backend-lowering
                           :specialized-stage0]})
         specialized-lowering-diagnostic-ids
         (range))
   :status :complete})

(defn specialized-lowering-artifact-manifest
  [backend kind target content input-id evidence-id]
  {:schema-version 1
   :kind kind
   :backend backend
   :profile :hosted
   :target target
   :content-hash (c4-artifact-id content)
   :inputs {:source input-id
            :mir input-id
            :backend-interface input-id}
   :evidence {:safety "safety-bundle:stage0"
              :proofs evidence-id
              :capabilities "capability-summary:stage0"
              :effects "effect-summary:stage0"
              :conformance "backend-conformance-pack:p07-t04"}
   :provenance {:compiler "gravity-stage0-clojure"
                :passes ["C14" "B1" "B8/B9/B10/B11/B12" "B13" "B14"]
                :dependencies "dependency-graph:stage0"}
   :reproducibility {:timestamp-policy :none
                     :nondeterminism []
                     :status :recorded}})