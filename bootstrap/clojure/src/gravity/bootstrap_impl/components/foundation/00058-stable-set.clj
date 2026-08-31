

(defn stable-set
  [values]
  (set values))

(defn stable-vec
  [values]
  (vec (sort-by pr-str values)))

(def capability-diagnostic-ids
  ["L15-CAPABILITY-MISSING" "L15-PROVIDER-MISSING" "L15-PROFILE"
   "L15-SCOPE" "L15-PHASE" "L15-TRUST"])

(defn- compatibility-diagnostic-record
  [artifact stage id facts]
  {:artifact artifact
   :diagnostic id
   :stage stage
   :facts facts
   :status :rejected})

(declare provider-name
         profile-capabilities
         capability-validation-facts)

(def ^:private ^:dynamic *capability-validation-leaf-call?* false)

(defn- capability-validation-ops
  []
  {:stable-set stable-set
   :stable-vec stable-vec
   :diagnostic-record
   (fn [id facts]
     (compatibility-diagnostic-record
      :gravity/capability-diagnostic :capability-validation id facts))
   :capability-diagnostic-ids capability-diagnostic-ids
   ;; Provider selection and profile capability projection remain bootstrap
   ;; seams.  The leaf may consume them, but it never discovers grants or
   ;; establishes provider trust.
   :provider-name provider-name
   :profile-capabilities profile-capabilities
   :provider-specs provider-specs})

(defn- capability-validation-call
  [operation-key operation & args]
  (if *capability-validation-leaf-call?*
    (capability-validation/call-entrypoint-body operation-key operation args)
    (binding [*capability-validation-leaf-call?* true]
      (capability-validation/with-operations
       (capability-validation-ops)
       #(capability-validation/call-entrypoint-body
         operation-key operation args)))))

(defn provider-name
  [capability]
  (capability-validation-call
   :provider-name capability-validation/provider-name capability))

(defn provider-version
  [capability]
  (get-in provider-specs [capability :version]))

(defn provider-trust-level
  [capability]
  (or (get-in provider-specs [capability :trust-level])
      :trusted-core))

(defn provider-artifact-schema
  [capability]
  (or (get-in provider-specs [capability :artifact-schema])
      (keyword "gravity.provider" (name capability))))

(defn provider-conformance-suite
  [capability]
  (or (get-in provider-specs [capability :conformance])
      (keyword "gravity.conformance" (name capability))))

(defn provider-contracts
  [capability]
  (or (get-in provider-specs [capability :contracts])
      #{'gravity.contracts/Provider}))

(defn provider-scope-kind
  [capability]
  (case capability
    :filesystem/read :filesystem
    :filesystem/write :filesystem
    :network/client :network
    :network/listener :network
    :ai/model :model
    :ai/tool :tool
    :ai/memory :memory-store
    :memory/allocator :memory
    :memory/raw :memory
    :compiler/ir :compiler
    :compiler/plugin :compiler
    :io/stdout :io
    :environment/read :environment
    :provider))