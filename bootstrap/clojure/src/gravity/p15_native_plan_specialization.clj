(ns gravity.p15-native-plan-specialization
  "Authenticated plan-specialized C artifact for the bounded native child.

  This namespace deliberately stops at an emitted C translation unit.  The
  existing public C backend owns compilation/process supervision; duplicating
  that machinery here would widen the trusted boundary.  Focused tests compile
  and run the returned source in a test-owned private directory instead.
  "
  (:require [gravity.p15-native-plan-specialization.artifact :as artifact]
            [gravity.p15-native-plan-specialization.contract :as contract]
            [gravity.p15-native-plan-specialization.diagnostics :as diagnostics]
            [gravity.p15-native-plan-specialization.emission :as emission]
            [gravity.p15-native-plan-specialization.helper-compilation :as helper]
            [gravity.p15-native-plan-specialization.plan-validation :as plan]
            [gravity.p15-native-plan-specialization.source-snapshot :as source])
  (:import [java.nio.file LinkOption]))

(def ^:private max-plan-instructions contract/max-plan-instructions)
(def ^:private max-generated-source-bytes
  contract/max-generated-source-bytes)
(def ^:private max-reference-output-bytes
  contract/max-reference-output-bytes)
(def ^:private max-scalar-bytes contract/max-scalar-bytes)
(def ^:private max-helper-source-bytes contract/max-helper-source-bytes)
(def ^:private helper-source-relative contract/helper-source-relative)
(def ^:private helper-function contract/helper-function)
(def ^:private helper-contract contract/helper-contract)
(def ^:private helper-function-shape contract/helper-function-shape)
(def ^:private no-follow-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private helper-source-content-hash
  contract/helper-source-content-hash)

(defn- fail! [id message source-path facts]
  (diagnostics/fail! id message source-path facts))
(defn- authentication-fail! [source-path message facts]
  (diagnostics/authentication-fail! source-path message facts))
(defn- unsupported-fail! [source-path message facts]
  (diagnostics/unsupported-fail! source-path message facts))
(defn- bounds-fail! [source-path message facts]
  (diagnostics/bounds-fail! source-path message facts))
(defn- helper-contract-fail! [source-path message facts]
  (diagnostics/helper-contract-fail! source-path message facts))
(defn- helper-rejected-fail! [source-path message facts]
  (diagnostics/helper-rejected-fail! source-path message facts))

(defn- repository-root []
  (source/repository-root helper-contract-fail!))
(def ^:private root (delay (repository-root)))

(defn- helper-source-path! [request-source]
  (source/helper-source-path!
   request-source @root helper-source-relative no-follow-options
   helper-contract-fail!))

(defn- strict-utf8 [request-source source-path bytes]
  (source/strict-utf8
   request-source source-path bytes helper-contract-fail!))

(defn- helper-source-snapshot!
  "Read one bounded, regular-file, NOFOLLOW UTF-8 snapshot of the tracked
  Gravity helper. The open channel and before/after identity checks close the
  replacement window between path validation and hashing."
  [request-source]
  (source/helper-source-snapshot!
   request-source
   {:helper-source-path! helper-source-path!
    :strict-utf8 strict-utf8
    :max-helper-source-bytes max-helper-source-bytes
    :no-follow-options no-follow-options
    :helper-contract-fail! helper-contract-fail!}))

(def ^:dynamic *p15-native-plan-c-emitter-source-loader*
  helper-source-snapshot!)

(defn- utf8-bytes [value]
  (contract/utf8-bytes value))
(defn- printable-ascii-string? [value]
  (contract/printable-ascii-string? value))
(defn- helper-scalar-safe? [instruction]
  (contract/helper-scalar-safe? instruction))
(defn- helper-statement-safe? [instruction]
  (contract/helper-statement-safe? instruction))
(defn- helper-safety-proof [plan]
  (contract/helper-safety-proof plan helper-contract))
(defn- helper-function-semantic-hash [definition]
  (contract/helper-function-semantic-hash definition))
(defn- helper-contract-hash [source-content-hash]
  (contract/helper-contract-hash
   source-content-hash helper-function-shape helper-contract))

(defn- compile-gravity-c-emitter-helper! [request-source]
  (helper/compile-gravity-c-emitter-helper!
   request-source
   {:source-loader *p15-native-plan-c-emitter-source-loader*
    :helper-contract-fail! helper-contract-fail!
    :helper-source-relative helper-source-relative
    :max-helper-source-bytes max-helper-source-bytes
    :helper-source-content-hash helper-source-content-hash
    :helper-function helper-function
    :helper-function-shape helper-function-shape
    :helper-contract helper-contract
    :helper-function-semantic-hash helper-function-semantic-hash
    :helper-contract-hash helper-contract-hash}))

(defn- scalar-value? [value]
  (contract/scalar-value? value))
(defn- scalar-bound! [source-path value]
  (contract/scalar-bound!
   source-path value max-scalar-bytes bounds-fail!))
(defn- instruction-children [source-path instruction]
  (plan/instruction-children source-path instruction unsupported-fail!))

(defn- plan-bounds!
  "Count authenticated plan nodes iteratively before C emission.

  The public backend validator repeats semantic checks, but it intentionally
  does not impose the packet provider's 128-instruction cap.  Keeping this
  check here ensures overbound plans fail before target bytes are generated.
  "
  [source-path plan]
  (plan/plan-bounds!
   source-path plan
   {:maximum-instructions max-plan-instructions
    :unsupported-fail! unsupported-fail!
    :bounds-fail! bounds-fail!
    :scalar-value? scalar-value?
    :scalar-bound! scalar-bound!
    :instruction-children instruction-children}))

(defn- authenticate! [packet context]
  (plan/authenticate! packet context authentication-fail!))

(defn- validate-and-emit! [packet context]
  (emission/validate-and-emit!
   packet context
   {:plan-bounds! plan-bounds!
    :compile-helper! compile-gravity-c-emitter-helper!
    :helper-safety-proof helper-safety-proof
    :helper-contract helper-contract
    :helper-function helper-function
    :utf8-bytes utf8-bytes
    :max-generated-source-bytes max-generated-source-bytes
    :max-reference-output-bytes max-reference-output-bytes
    :unsupported-fail! unsupported-fail!
    :helper-contract-fail! helper-contract-fail!
    :helper-rejected-fail! helper-rejected-fail!
    :bounds-fail! bounds-fail!
    :authentication-fail! authentication-fail!}))

(defn specialize-native-runtime-plan
  "Authenticate PACKET against CONTEXT and emit a bounded plan-specialized C
  artifact.  The packet must be a real target-neutral stage2 runtime packet;
  this function never accepts hand-authored plans or starts a child process.

  Compilation/execution is intentionally test-owned for this slice.  A
  production runner is not exposed until it can reuse the public C backend's
  descriptor-checked staging and cleanup without duplicating process logic.
  "
  [packet context]
  (let [packet (authenticate! packet context)
        emitted (validate-and-emit! packet context)]
    (artifact/build packet context emitted helper-function helper-contract)))
