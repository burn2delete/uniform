

(defn native-lowering-file-artifact
  [path]
  (native-lowering-source-artifact path (slurp path)))

(def hosted-lowering-governing-documents
  ["docs/phase-07-backend-architecture/101-b4-wasm-backend-design.md"
   "docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md"
   "docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md"
   "docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md"
   "docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md"
   "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"])

(def hosted-lowering-diagnostic-ids
  ["B4-TARGET"
   "B4-COMPONENT"
   "B4-CANONICAL-ABI"
   "B4-IMPORT"
   "B4-EXPORT"
   "B4-MEMORY"
   "B4-BOUNDS"
   "B4-NONDETERMINISM"
   "B4-ASYNC"
   "B4-WASI-ASYNC"
   "B4-SIMD"
   "B4-ATOMIC"
   "B4-HOST-SCHEMA"
   "B4-MANIFEST"
   "B5-TARGET"
   "B5-NULL"
   "B5-EXCEPTION"
   "B5-REFLECTION"
   "B5-CLASSLOADING"
   "B5-INTEROP"
   "B5-RESOURCE"
   "B5-THREAD"
   "B5-NATIVE-IMAGE"
   "B5-PROFILE"
   "B5-MANIFEST"
   "B6-TARGET"
   "B6-GLOBAL"
   "B6-IMPORT"
   "B6-NULLISH"
   "B6-EXCEPTION"
   "B6-NUMERIC"
   "B6-EVAL"
   "B6-PROTOTYPE"
   "B6-ASYNC"
   "B6-UI"
   "B6-MANIFEST"])

(def hosted-lowering-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             hosted-lowering-diagnostic-ids)))

(defn hosted-lowering-backend-for-diagnostic
  [id]
  (cond
    (str/starts-with? id "B4-") :gravity.backend/wasm
    (str/starts-with? id "B5-") :gravity.backend/jvm
    :else :gravity.backend/js-ts))

(defn hosted-lowering-stage-for-diagnostic
  [id]
  (cond
    (str/starts-with? id "B4-") :wasm-backend
    (str/starts-with? id "B5-") :jvm-backend
    :else :js-ts-backend))

(defn hosted-lowering-diagnostic-message
  [id]
  (cond
    (str/starts-with? id "B4-") "Wasm hosted boundary lowering contract failed"
    (str/starts-with? id "B5-") "JVM hosted boundary lowering contract failed"
    :else "JavaScript/TypeScript hosted boundary lowering contract failed"))

(defn hosted-lowering-source-overrides
  [module]
  (or (get-in module [:metadata :backend :hosted-lowering])
      (get-in module [:metadata :backend :hosted])
      {}))

(defn hosted-lowering-fail!
  [id source-path subject extra]
  (fail! id
         (hosted-lowering-diagnostic-message id)
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :hosted-backend-lowering
                 :stage (or (:stage subject)
                            (hosted-lowering-stage-for-diagnostic id))
                 :backend (or (:backend subject)
                              (hosted-lowering-backend-for-diagnostic id))
                 :profile (or (:profile subject) :hosted)
                 :target (or (:target subject) :hosted-stage0)
                 :artifact-id (:artifact-id subject)
                 :mir-op (or (:mir-op subject) :host-call)
                 :domain-anchor (:domain-anchor subject)
                 :host-symbol (:host-symbol subject)
                 :missing-evidence (:missing-evidence subject)
                 :target-construct (:target-construct subject)
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Hosted backend lowerings must declare host authority, nullish/null and exception translation, dynamic loading, async/replay, schemas, runtime providers, source maps, artifact manifests, and capability evidence."}
                extra)))

(defn hosted-lowering-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get hosted-lowering-override-diagnostics fail-kind)]
      (hosted-lowering-fail!
       id source-path
       {:stage (hosted-lowering-stage-for-diagnostic id)
        :backend (hosted-lowering-backend-for-diagnostic id)
        :artifact-id (str "hosted-lowering-" (name fail-kind))
        :host-symbol fail-kind
        :missing-evidence [fail-kind]
        :target-construct fail-kind}
       {:missing-fields [fail-kind]}))))

(defn hosted-lowering-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/backend-diagnostic-stream
   :stage :hosted-backend-lowering
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage (hosted-lowering-stage-for-diagnostic id)
            :backend (hosted-lowering-backend-for-diagnostic id)
            :message-key (keyword "backend-hosted" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "hosted-backend-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :hosted-stage0
            :mir-op (case id
                      "B5-NULL" :host-null-boundary
                      "B6-GLOBAL" :host-global-access
                      "B4-IMPORT" :wasm-host-import
                      :host-call)
            :domain-anchor nil
            :host-symbol id
            :missing-evidence #{:capability :schema :source-map
                                :runtime-provider}
            :target-construct id
            :fallback-status :rejected
            :facts {:ambient-authority-policy :reject
                    :nullish-policy :checked-boundary
                    :host-exception-policy :translate}
            :remediation [{:kind :declare-host-boundary}
                          {:kind :preserve-capability-and-schema}
                          {:kind :translate-host-failure}]
            :redactions []
            :ordering-key [id :hosted-backend-lowering :hosted-stage0]})
         hosted-lowering-diagnostic-ids
         (range))
   :status :complete})

(defn hosted-lowering-artifact-manifest
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
              :conformance "backend-conformance-pack:p07-t03"}
   :provenance {:compiler "gravity-stage0-clojure"
                :passes ["C14" "B1" "B4/B5/B6" "B13" "B14"]
                :dependencies "dependency-graph:stage0"}
   :reproducibility {:timestamp-policy :none
                     :nondeterminism []
                     :status :recorded}})