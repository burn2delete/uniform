(ns gravity.p15-native-packet-binding
  "Authenticated lowering from the target-neutral stage2 packet to the
  deliberately bounded host-C native runtime provider wire."
  (:require [gravity.p15-native-packet-binding.artifact :as artifact]
            [gravity.p15-native-packet-binding.authentication :as authentication]
            [gravity.p15-native-packet-binding.contract :as contract]
            [gravity.p15-native-packet-binding.diagnostics :as diagnostics]
            [gravity.p15-native-packet-binding.lowering :as lowering]
            [gravity.p15-native-packet-binding.source-identity :as source-identity]
            [gravity.p15-native-packet-binding.wire :as wire])
  (:import [java.nio.file LinkOption]))

(def ^:private packet-limit contract/packet-limit)
(def ^:private instruction-limit contract/instruction-limit)
(def ^:private stack-limit contract/stack-limit)
(def ^:private value-limit contract/value-limit)
(def ^:private output-limit contract/output-limit)
(def ^:private identity-file-limit contract/identity-file-limit)
(def ^:private runtime-contract-relative contract/runtime-contract-relative)
(def ^:private provider-relative contract/provider-relative)
(def ^:private no-follow-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- fail! [id message source-path facts]
  (diagnostics/fail! id message source-path facts))
(defn- auth-fail! [source-path message facts]
  (diagnostics/auth-fail! source-path message facts))
(defn- plan-fail! [source-path message facts]
  (diagnostics/plan-fail! source-path message facts))
(defn- bounds-fail! [source-path message facts]
  (diagnostics/bounds-fail! source-path message facts))

(defn- utf8-bytes [value] (contract/utf8-bytes value))
(defn- sha256-bytes-hex [bytes] (contract/sha256-bytes-hex bytes))
(defn- sha256-text [text] (contract/sha256-text text))
(defn- hex-encode [bytes] (contract/hex-encode bytes))

(defn- repository-root []
  (source-identity/repository-root no-follow-options bounds-fail!))
(def ^:private root (delay (repository-root)))
(defn- file-identity [relative]
  (source-identity/file-identity
   relative @root no-follow-options identity-file-limit bounds-fail!
   sha256-bytes-hex))

(defn- exact-context? [context]
  (authentication/exact-context? context sha256-text))
(defn- validate-context! [context]
  (authentication/validate-context! context exact-context? auth-fail!))
(defn- validate-envelope! [packet context]
  (authentication/validate-envelope! packet context auth-fail!))

(defn- scalar-kind [value] (contract/scalar-kind value))
(defn- checked-rendered-text [source-path value]
  (lowering/checked-rendered-text
   source-path value scalar-kind plan-fail! bounds-fail!))
(defn- push-scalar [source-path value]
  (lowering/push-scalar
   source-path value
   {:scalar-kind scalar-kind
    :checked-rendered-text checked-rendered-text
    :utf8-bytes utf8-bytes
    :hex-encode hex-encode
    :value-limit value-limit
    :bounds-fail! bounds-fail!}))

(defn- combine-evaluations [evaluations]
  (lowering/combine-evaluations evaluations))

(declare lower-expression)

(defn- lower-call [source-path instruction call-kind]
  (lowering/lower-call
   source-path instruction call-kind
   {:lower-expression lower-expression
    :combine-evaluations combine-evaluations
    :utf8-bytes utf8-bytes
    :value-limit value-limit
    :plan-fail! plan-fail!
    :bounds-fail! bounds-fail!}))

(defn- lower-expression [source-path instruction]
  (lowering/lower-expression
   source-path instruction
   {:push-scalar push-scalar
    :lower-call lower-call
    :plan-fail! plan-fail!}))

(defn- validate-entrypoint! [source-path plan]
  (lowering/validate-entrypoint!
   source-path plan lower-expression combine-evaluations plan-fail!))
(defn- validate-authority! [source-path plan lowered]
  (lowering/validate-authority! source-path plan lowered plan-fail!))

(defn- build-wire! [source-path source-text runtime-rule-sha lowered]
  (wire/build-wire!
   source-path source-text runtime-rule-sha lowered
   {:instruction-limit instruction-limit
    :stack-limit stack-limit
    :output-limit output-limit
    :value-limit value-limit
    :packet-limit packet-limit
    :utf8-bytes utf8-bytes
    :hex-encode hex-encode
    :sha256-bytes-hex sha256-bytes-hex
    :bounds-fail! bounds-fail!}))

(defn bind-native-runtime-packet
  "Authenticate and lower one real target-neutral stage2 packet to the
  canonical bounded native provider wire. No provider process is started."
  [packet context]
  (let [context (validate-context! context)
        _ (validate-envelope! packet context)
        source-path (:source-path context)
        plan (:plan packet)
        lowered (validate-entrypoint! source-path plan)
        authority (validate-authority! source-path plan lowered)
        expected-stdout (get-in packet [:stage2-runtime-execution-record
                                        :stdout])
        _ (when-not (and (string? expected-stdout)
                         (= expected-stdout (:reference-output packet))
                         (= expected-stdout (:stdout lowered)))
            (auth-fail! source-path
                        "lowered output does not match authenticated runtime output"
                        {:expected-stdout-hash
                         (when (string? expected-stdout)
                           (sha256-text expected-stdout))
                         :lowered-stdout-hash (sha256-text (:stdout lowered))
                         :missing-fact :authenticated-native-output-equivalence}))
        runtime-contract (file-identity runtime-contract-relative)
        provider (file-identity provider-relative)
        runtime-rule-sha (subs (:content-hash runtime-contract) 7)
        wire (build-wire! source-path (:source-text context)
                          runtime-rule-sha lowered)]
    (artifact/build packet context authority runtime-contract provider
                    runtime-rule-sha wire expected-stdout sha256-text
                    runtime-contract-relative provider-relative)))
