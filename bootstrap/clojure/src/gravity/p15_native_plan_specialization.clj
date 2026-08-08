(ns gravity.p15-native-plan-specialization
  "Authenticated plan-specialized C artifact for the bounded native child.

  This namespace deliberately stops at an emitted C translation unit.  The
  existing public C backend owns compilation/process supervision; duplicating
  that machinery here would widen the trusted boundary.  Focused tests compile
  and run the returned source in a test-owned private directory instead.
  "
  (:require [gravity.bootstrap :as bootstrap])
  (:import [java.nio.charset StandardCharsets]))

(def ^:private max-plan-instructions 128)
(def ^:private max-generated-source-bytes 65536)
(def ^:private max-reference-output-bytes 8192)
(def ^:private max-scalar-bytes 1024)

(defn- fail!
  [id message source-path facts]
  (throw
   (ex-info message
            (merge {:id id
                    :diagnostic id
                    :severity :error
                    :stage :p15-native-plan-specialization
                    :source-path (or source-path "packet:unknown")
                    :target :c
                    :profile :native
                    :fallback-status :rejected
                    :public-command-route? false
                    :self-hosted? false
                    :release-ready? false
                    :compiler-authored-in-gravity? false
                    :provider-authored-in-gravity? false}
                   facts))))

(defn- authentication-fail!
  [source-path message facts]
  (fail! "P15NS001" message source-path
         (merge {:diagnostic-family :authenticated-packet-context
                 :remediation :supply_exact_authenticated_stage2_packet_context}
                facts)))

(defn- unsupported-fail!
  [source-path message facts]
  (fail! "P15NS002" message source-path
         (merge {:diagnostic-family :unsupported-native-plan-specialization
                 :remediation :restrict_to_public_runtime_derived_c_subset}
                facts)))

(defn- bounds-fail!
  [source-path message facts]
  (fail! "P15NS003" message source-path
         (merge {:diagnostic-family :native-plan-specialization-bound
                 :remediation :reduce_the_authenticated_plan_or_output}
                facts)))

(defn- utf8-bytes
  [value]
  (.getBytes ^String (str value) StandardCharsets/UTF_8))

(defn- scalar-value?
  [value]
  (or (nil? value)
      (boolean? value)
      (string? value)
      (number? value)
      (char? value)
      (keyword? value)
      (symbol? value)))

(defn- scalar-bound!
  [source-path value]
  (when (and (scalar-value? value)
             (> (alength (utf8-bytes (if (nil? value) "nil" value)))
                max-scalar-bytes))
    (bounds-fail! source-path
                  "native plan scalar exceeds the bounded value size"
                  {:maximum-scalar-bytes max-scalar-bytes
                   :observed-scalar-bytes
                   (alength (utf8-bytes (if (nil? value) "nil" value)))
                   :missing-fact :bounded-native-scalar})))

(defn- instruction-children
  [source-path instruction]
  (when-not (map? instruction)
    (unsupported-fail! source-path
                       "native plan contains a malformed instruction"
                       {:observed-instruction instruction
                        :missing-fact :native-instruction-record}))
  (try
    (bootstrap/c-backend-instruction-children instruction)
    (catch Throwable error
      (unsupported-fail! source-path
                         "native plan instruction shape is not supported"
                         {:observed-op (:op instruction)
                          :missing-fact :native-instruction-children
                          :cause-message (.getMessage error)}))))

(defn- plan-bounds!
  "Count authenticated plan nodes iteratively before C emission.

  The public backend validator repeats semantic checks, but it intentionally
  does not impose the packet provider's 128-instruction cap.  Keeping this
  check here ensures overbound plans fail before target bytes are generated.
  "
  [source-path plan]
  (let [functions (:functions plan)]
    (when-not (map? functions)
      (unsupported-fail! source-path
                         "native plan has no function map"
                         {:missing-fact :native-plan-functions}))
    (let [entrypoint (:entrypoint plan)
          entry-function (when (map? functions)
                           (get functions entrypoint))]
      (when-not (and (symbol? entrypoint)
                     (= 1 (count functions))
                     (map? entry-function)
                     (zero? (:arity entry-function))
                     (vector? (:instructions entry-function)))
        (unsupported-fail!
         source-path
         "native plan requires one zero-arity entrypoint"
         {:observed-entrypoint entrypoint
          :observed-function-count (count functions)
          :missing-fact :single-zero-arity-native-entrypoint})))
    (loop [pending
           (vec (mapcat (fn [[_ function]]
                          (when-not (map? function)
                            (unsupported-fail!
                             source-path
                             "native plan function record is malformed"
                             {:missing-fact :native-plan-function-record}))
                          (let [instructions (:instructions function)]
                            (when-not (vector? instructions)
                              (unsupported-fail!
                               source-path
                               "native plan function instructions are malformed"
                               {:missing-fact :native-plan-instructions}))
                            instructions))
                        functions))
           count 0]
      (if-let [instruction (peek pending)]
        (let [pending (pop pending)
              next-count (inc count)]
          (when (> next-count max-plan-instructions)
            (bounds-fail! source-path
                          "native plan exceeds the bounded instruction count"
                          {:maximum-instructions max-plan-instructions
                           :observed-instructions next-count
                           :missing-fact :bounded-native-plan-instructions}))
          (doseq [[key value] instruction]
            (when (and (= key :value) (scalar-value? value))
              (scalar-bound! source-path value)))
          (let [children (instruction-children source-path instruction)]
            (doseq [child children]
              (when (and (map? child) (contains? child :value))
                (scalar-bound! source-path (:value child))))
            (recur (into pending (remove nil? children)) next-count)))
        {:instruction-count count}))))

(defn- authenticate!
  [packet context]
  ;; Do not inspect packet plan/envelope before this call.  Packet-local hashes
  ;; are not authority; the two-argument predicate binds the packet to the
  ;; trusted source text and target context.
  (let [source-path (when (map? context) (:source-path context))
        authenticated?
        (try
          (bootstrap/p15-s23-closed-runtime-packet-authentic?
           packet context)
          (catch Throwable _ false))]
    (when-not authenticated?
      (authentication-fail!
       source-path
       "stage2 runtime packet is not authentic for the trusted context"
       {:missing-fact :authenticated-stage2-runtime-packet-and-context
        :authenticator
        'gravity.bootstrap/p15-s23-closed-runtime-packet-authentic?
        :authenticator-arity 2}))
    (when-not (and (map? context)
                   (= :c (:requested-target context)))
      (authentication-fail!
       source-path
       "native plan specialization requires the :c requested target"
       {:observed-target (when (map? context) (:requested-target context))
        :missing-fact :native-c-target-context}))
    packet))

(defn- validate-and-emit!
  [packet context]
  (let [source-path (:source-path context)
        plan (:plan packet)
        bounds (plan-bounds! source-path plan)]
    (try
      (bootstrap/c-backend-validate-runtime-plan! source-path :c plan)
      (catch clojure.lang.ExceptionInfo error
        (unsupported-fail!
         source-path
         "authenticated plan is outside the runtime-derived C subset"
         {:cause-diagnostic (:id (ex-data error))
          :cause-facts (ex-data error)
          :missing-fact :public-c-backend-runtime-plan-validation}))
      (catch Throwable error
        (unsupported-fail!
         source-path
         "runtime-derived C plan validation failed"
         {:missing-fact :public-c-backend-runtime-plan-validation
          :cause-message (.getMessage error)})))
    (let [source
          (try
            (bootstrap/c-backend-runtime-source plan)
            (catch clojure.lang.ExceptionInfo error
              (unsupported-fail!
               source-path
               "runtime-derived C source emission failed"
               {:cause-diagnostic (:id (ex-data error))
                :cause-facts (ex-data error)
                :missing-fact :public-c-backend-runtime-source}))
            (catch Throwable error
              (unsupported-fail!
               source-path
               "runtime-derived C source emission failed"
               {:missing-fact :public-c-backend-runtime-source
                :cause-message (.getMessage error)})))
          source-bytes (utf8-bytes source)
          expected-output (:reference-output packet)
          expected-output-bytes (when (string? expected-output)
                                  (utf8-bytes expected-output))]
      (when (> (alength source-bytes) max-generated-source-bytes)
        (bounds-fail! source-path
                      "generated C source exceeds the bounded artifact size"
                      {:maximum-generated-source-bytes max-generated-source-bytes
                       :observed-generated-source-bytes (alength source-bytes)
                       :missing-fact :bounded-native-generated-source}))
      (when-not (string? expected-output)
        (authentication-fail! source-path
                              "authenticated packet has no reference output"
                              {:missing-fact :authenticated-reference-output}))
      (when (> (alength expected-output-bytes) max-reference-output-bytes)
        (bounds-fail! source-path
                      "authenticated reference output exceeds the bounded size"
                      {:maximum-reference-output-bytes max-reference-output-bytes
                       :observed-reference-output-bytes
                       (alength expected-output-bytes)
                       :missing-fact :bounded-native-reference-output}))
      {:bounds bounds
       :source source
       :source-bytes source-bytes
       :source-content-hash (str "sha256:" (bootstrap/sha256-hex source))
       :expected-output expected-output})))

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
        {:keys [source source-bytes source-content-hash expected-output bounds]}
        (validate-and-emit! packet context)
        compiler-record (:stage2-compiler-artifact-record packet)]
    {:artifact :gravity/p15-native-plan-specialization
     :schema-version 1
     :status :complete-for-internal-plan-specialized-native-child
     :input {:kind (:kind packet)
             :status (:status packet)
             :requested-target (:requested-target packet)
             :target-eligibility (:target-eligibility packet)
             :source-path (:source-path context)
             :source-content-hash (:source-content-hash context)}
     :authentication {:status :authenticated
                      :authenticator
                      'gravity.bootstrap/p15-s23-closed-runtime-packet-authentic?
                      :authenticator-arity 2
                      :contextual? true}
     :plan {:plan-id (get-in packet [:plan :plan-id])
            :content-hash
            (str "sha256:"
                 (bootstrap/sha256-hex
                  (pr-str
                   (bootstrap/c-backend-canonical-value
                    (:plan packet)))))
            :instruction-count (:instruction-count bounds)
            :validation-status :passed
            :validator 'gravity.bootstrap/c-backend-validate-runtime-plan!}
     :compiler {:artifact (:artifact compiler-record)
                :artifact-hash (:artifact-hash compiler-record)
                :source-content-hash (:source-content-hash compiler-record)
                :semantic-hash (:semantic-hash compiler-record)}
     :emitter {:emitter 'gravity.bootstrap/c-backend-runtime-source
               :source-rule-hash
               (get-in packet [:stage2-plan-emitter-rule :source-rule-hash])
               :status :emitted}
     :driver {:driver-rule-hash
              (get-in packet [:stage2-compiler-driver-rule :driver-rule-hash])
              :record-status
              (get-in packet [:stage2-compiler-driver-record :status])}
     :runtime {:runtime-rule-hash
               (get-in packet [:stage2-runtime-rule :runtime-rule-hash])
               :runtime-artifact-hash
               (get-in packet [:stage2-runtime-rule :runtime-artifact-hash])
               :execution-status
               (get-in packet [:stage2-runtime-execution-record :status])}
     :generated-c {:dialect :c11
                   :source source
                   :bytes source-bytes
                   :content-hash source-content-hash
                   :implementation :clojure-emitted-plan-specialized-c
                   :provider-kind :host-c
                   :execution :not-run
                   :execution-evidence :external-focused-test-only}
     :expected-output expected-output
     :runner {:status :not-exposed
              :reason :public-c-backend-process-staging-not-reused-by-production-wrapper
              :compiler-and-process-boundary :clojure-bootstrap}
     :provenance {:selected-generated-child-clojure-seed-boundary? false
                  :selected-generated-child-jvm-available? false
                  :selected-runtime-clojure-seed-boundary? false
                  :selected-child-clojure-seed-boundary? false
                  :generic-host-c-packet-interpreter-used? false
                  :compiler-clojure-seed-boundary? true
                  :authentication-clojure-seed-boundary? true
                  :validator-clojure-seed-boundary? true
                  :c-emitter-clojure-seed-boundary? true
                  :artifact-clojure-seed-boundary? true
                  :artifact-construction-clojure-seed-boundary? true
                  :process-clojure-seed-boundary? true
                  :file-io-clojure-seed-boundary? true
                  :process-and-file-io-clojure-seed-boundary? true
                  :public-clojure-seed-boundary? true
                  :public-wrapper-clojure-seed-boundary? true
                  :global-clojure-seed-boundary? true}
     :claims {:provider-authored-in-gravity? false
              :compiler-authored-in-gravity? false
              :public-command-route? false
              :self-hosted? false
              :release-ready? false
              :backend-complete? false
              :full-language? false
              :formal-language-complete? false
              :source-content-hash-verified-by-provider? false}}))
