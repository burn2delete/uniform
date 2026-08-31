

(defn b4-document-feature-requirement
  [id]
  (case id
    "B4-TARGET" :component-model-wasm32
    "B4-COMPONENT" :component-model
    "B4-CANONICAL-ABI" :canonical-abi
    "B4-IMPORT" :declared-import-capability
    "B4-EXPORT" :stable-export-schema
    "B4-MEMORY" :linear-memory-plan
    "B4-BOUNDS" :proof-backed-bounds-elision
    "B4-NONDETERMINISM" :replay-recording
    "B4-ASYNC" :host-async-metadata
    "B4-WASI-ASYNC" :wasi-0.3-async-component
    "B4-SIMD" :simd-feature-record
    "B4-ATOMIC" :shared-memory-atomics
    "B4-HOST-SCHEMA" :host-boundary-schema
    "B4-MANIFEST" :wasm-artifact-manifest
    :wasm-feature-record))

(defn b4-document-fail!
  [id source-path subject extra]
  (fail! id
         "B4 Wasm backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b4-wasm-backend-document
                 :stage (or (:stage subject)
                            :b4-wasm-backend-document-coverage)
                 :backend :gravity.backend/wasm
                 :profile (or (:profile subject) :hosted)
                 :embedding (or (:embedding subject) :wasi-0.3-component)
                 :artifact-id (:artifact-id subject)
                 :mir-op (or (:mir-op subject) :host-call)
                 :domain-anchor (:domain-anchor subject)
                 :import-id (or (:import-id subject) :gravity.host/clock-now)
                 :export-id (or (:export-id subject) :gravity/entry)
                 :interface-item-id (or (:interface-item-id subject)
                                        :gravity-host/clock-now)
                 :world-id (or (:world-id subject) :gravity-stage0-world)
                 :canonical-abi-record-id
                 (or (:canonical-abi-record-id subject)
                     :canonical-abi/entry-v1)
                 :async-item-id (or (:async-item-id subject)
                                    :wasi-async/clock-future)
                 :feature-requirement
                 (or (:feature-requirement subject)
                     (b4-document-feature-requirement id))
                 :missing-evidence (or (:missing-evidence subject)
                                       (b4-document-missing-evidence id))
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Emit Wasm only from verified backend input with pinned target features, component contracts, canonical ABI records, import/export capability schemas, linear-memory and bounds evidence, async/replay records, host boundary schemas, feature fallbacks, and complete artifact manifests."}
                extra)))

(defn b4-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b4-document-override-diagnostics fail-kind)]
      (b4-document-fail!
       id source-path
       {:stage :b4-wasm-backend-document-coverage
        :artifact-id (str "b4-document-" (name fail-kind))
        :missing-evidence fail-kind
        :feature-requirement fail-kind}
       {:missing-fields [fail-kind]}))))

(def b4-document-wat
  (str "(module\n"
       "  (type $entry_t (func (param i64) (result i64)))\n"
       "  (import \"gravity:host/clock\" \"now\" (func $clock_now (result i64)))\n"
       "  (memory $memory 1 2)\n"
       "  (table $table 1 funcref)\n"
       "  (func $gravity_entry (export \"gravity_entry\") (param $x i64) (result i64)\n"
       "    local.get $x)\n"
       ")\n"))

(def b4-document-wit
  (str "package gravity:stage0;\n\n"
       "interface host {\n"
       "  now: async func() -> future<u64>;\n"
       "  stream-events: func() -> stream<u64>;\n"
       "}\n\n"
       "world gravity-stage0 {\n"
       "  import host;\n"
       "  export gravity-entry: func(x: u64) -> u64;\n"
       "}\n"))

(defn b4-document-balanced-parens?
  [text]
  (loop [chars (seq text)
         depth 0]
    (cond
      (neg? depth) false
      (nil? chars) (zero? depth)
      (= \( (first chars)) (recur (next chars) (inc depth))
      (= \) (first chars)) (recur (next chars) (dec depth))
      :else (recur (next chars) depth))))

(defn b4-document-wat-structurally-valid?
  [text]
  (and (b4-document-balanced-parens? text)
       (str/includes? text "(module")
       (str/includes? text "(import")
       (str/includes? text "(memory")
       (str/includes? text "(table")
       (str/includes? text "(export \"gravity_entry\"")))

(defn b4-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b4-wasm-backend-diagnostic-stream
   :stage :b4-wasm-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b4-wasm-backend-document-coverage
            :backend :gravity.backend/wasm
            :message-key (keyword "backend-wasm" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b4-document-syntax-" index)
                      :artifact input-id}
            :mir-op (case id
                      "B4-IMPORT" :wasm-host-import
                      "B4-EXPORT" :host-export
                      "B4-ASYNC" :async-host-call
                      "B4-WASI-ASYNC" :future-stream-lowering
                      "B4-SIMD" :vector-lowering
                      "B4-ATOMIC" :atomic-compare-exchange
                      :host-call)
            :domain-anchor (when (= id "B4-SIMD") :simd-domain)
            :import-id :gravity.host/clock-now
            :export-id :gravity/entry
            :interface-item-id :gravity-host/clock-now
            :world-id :gravity-stage0-world
            :canonical-abi-record-id :canonical-abi/entry-v1
            :async-item-id :wasi-async/clock-future
            :profile :hosted
            :embedding :wasi-0.3-component
            :feature-requirement (b4-document-feature-requirement id)
            :missing-evidence (b4-document-missing-evidence id)
            :fallback-status :rejected
            :facts {:ambient-host-authority :rejected
                    :raw-pointer-escape :rejected
                    :replay-required? true}
            :remediation [{:kind :declare-wasm-component-contract}
                          {:kind :attach-capability-and-host-schema}
                          {:kind :record-replay-and-async-abi}]
            :redactions []
            :ordering-key [id :b4-wasm-backend-document-coverage
                           :wasi-0.3-component]})
         b4-document-diagnostic-ids
         (range))
   :status :complete})