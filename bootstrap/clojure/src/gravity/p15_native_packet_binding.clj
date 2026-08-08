(ns gravity.p15-native-packet-binding
  "Authenticated lowering from the target-neutral stage2 packet to the
  deliberately bounded host-C native runtime provider wire."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path]
           [java.nio.file.attribute BasicFileAttributes]
           [java.security MessageDigest]))

(def ^:private packet-limit 65536)
(def ^:private instruction-limit 128)
(def ^:private stack-limit 128)
(def ^:private value-limit 1024)
(def ^:private output-limit 8192)
(def ^:private identity-file-limit 1048576)
(def ^:private runtime-contract-relative
  "bootstrap/gravity/p15_s23/native_runtime_driver.gravity")
(def ^:private provider-relative
  "bootstrap/native/p15_native_runtime_driver.c")
(def ^:private no-follow-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- fail!
  [id message source-path facts]
  (throw
   (ex-info message
            (merge {:id id
                    :diagnostic id
                    :severity :error
                    :stage :p15-native-packet-binding
                    :source-path (or source-path "packet:unknown")
                    :target :c
                    :profile :native
                    :runtime-provider :gravity.native/libsystem-stdio-v1
                    :fallback-status :rejected
                    :public-command-route? false
                    :self-hosted? false
                    :release-ready? false}
                   facts))))

(defn- auth-fail!
  [source-path message facts]
  (fail! "P15NP001" message source-path
         (merge {:diagnostic-family :packet-authentication
                 :remediation :supply_exact_authenticated_stage2_packet_context}
                facts)))

(defn- plan-fail!
  [source-path message facts]
  (fail! "P15NP002" message source-path
         (merge {:diagnostic-family :unsupported-native-packet-plan
                 :remediation :use_bounded_scalar_str_println_entrypoint}
                facts)))

(defn- bounds-fail!
  [source-path message facts]
  (fail! "P15NP003" message source-path
         (merge {:diagnostic-family :native-packet-wire-or-bound
                 :remediation :reduce_or_repair_native_runtime_packet}
                facts)))

(defn- utf8-bytes
  [value]
  (.getBytes ^String value StandardCharsets/UTF_8))

(defn- sha256-bytes-hex
  [^bytes bytes]
  (let [digest (.digest (doto (MessageDigest/getInstance "SHA-256")
                          (.update bytes)))]
    (apply str (map #(format "%02x" (bit-and (int %) 0xff)) digest))))

(defn- sha256-text
  [text]
  (str "sha256:" (sha256-bytes-hex (utf8-bytes text))))

(defn- hex-encode
  [^bytes bytes]
  (apply str (map #(format "%02x" (bit-and (int %) 0xff)) bytes)))

(defn- repository-root
  []
  (let [resource (io/resource "gravity/p15_native_packet_binding.clj")]
    (when-not resource
      (bounds-fail! nil "native packet binding source is not on the classpath"
                    {:missing-fact :binding-source-resource}))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (bounds-fail! nil "repository root is unavailable"
                      {:missing-fact :repository-root})

        (Files/isRegularFile (.resolve candidate "deps.edn") no-follow-options)
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- file-identity
  [relative]
  (let [path (.resolve ^Path @root relative)
        before (Files/readAttributes path BasicFileAttributes
                                     no-follow-options)]
    (when-not (and (.isRegularFile before)
                   (<= (.size before) identity-file-limit))
      (bounds-fail! relative "native runtime identity file is unavailable"
                    {:maximum-identity-file-bytes identity-file-limit
                     :missing-fact :bounded-regular-runtime-identity-file}))
    (let [bytes (Files/readAllBytes path)
          after (Files/readAttributes path BasicFileAttributes
                                      no-follow-options)]
      (when-not (and (.isRegularFile after)
                     (= (.fileKey before) (.fileKey after))
                     (= (.size before) (.size after))
                     (= (.size after) (alength bytes)))
        (bounds-fail! relative "native runtime identity changed while reading"
                      {:missing-fact :stable-runtime-identity-file}))
      {:path relative
       :content-hash
       (str "sha256:" (sha256-bytes-hex bytes))})))

(defn- exact-context?
  [context]
  (and (map? context)
       (= #{:source-path :source-text :source-content-hash :requested-target}
          (set (keys context)))
       (string? (:source-path context))
       (string? (:source-text context))
       (string? (:source-content-hash context))
       (= :c (:requested-target context))
       (= (:source-content-hash context)
          (sha256-text (:source-text context)))))

(defn- validate-context!
  [context]
  (let [source-path (when (map? context) (:source-path context))]
    (when-not (exact-context? context)
      (auth-fail! source-path "trusted packet context is not exact"
                  {:missing-fact :exact-trusted-packet-context
                   :required-context-keys
                   [:source-path :source-text :source-content-hash
                    :requested-target]}))
    (when-not (or (str/ends-with? source-path ".gravity")
                  (str/ends-with? source-path ".qst"))
      (auth-fail! source-path "trusted source extension is unsupported"
                  {:observed-extension
                   (second (re-find #"(\.[^./]+)$" source-path))
                   :supported-extensions [".gravity" ".qst"]
                   :missing-fact :gravity-source-extension}))
    context))

(defn- validate-envelope!
  [packet context]
  (when-not (and (map? packet)
                 (= :gravity/target-neutral-stage2-runtime-packet
                    (:kind packet))
                 (= :complete (:status packet))
                 (= :c (:requested-target packet))
                 (= :accepted (get-in packet [:target-eligibility :status]))
                 (= :c (get-in packet
                               [:target-eligibility :requested-target])))
    (auth-fail! (:source-path context)
                "stage2 packet envelope is not eligible for native binding"
                {:observed-kind (:kind packet)
                 :observed-status (:status packet)
                 :observed-target (:requested-target packet)
                 :observed-target-eligibility (:target-eligibility packet)
                 :missing-fact :eligible-target-neutral-stage2-packet}))
  ;; This call must remain before any plan traversal or lowering. Packet-local
  ;; hashes are not authority; the trusted source context is the second input.
  (when-not (bootstrap/p15-s23-closed-runtime-packet-authentic? packet context)
    (auth-fail! (:source-path context)
                "stage2 packet does not authenticate against trusted source"
                {:missing-fact :authenticated-stage2-packet-and-context}))
  packet)

(defn- scalar-kind
  [value]
  (cond
    (nil? value) :nil
    (boolean? value) :bool
    (string? value) :string
    (integer? value) :integer
    :else nil))

(defn- checked-rendered-text
  [source-path value]
  (let [kind (scalar-kind value)]
    (when-not kind
      (plan-fail! source-path "native provider supports only scalar values"
                  {:observed-value-type (some-> value class .getName)
                   :missing-fact :bounded-native-scalar}))
    (when (and (= :integer kind)
               (or (< (bigint value) (bigint Long/MIN_VALUE))
                   (> (bigint value) (bigint Long/MAX_VALUE))))
      (bounds-fail! source-path "integer exceeds native signed 64-bit bound"
                    {:observed-value (str value)
                     :maximum Long/MAX_VALUE
                     :minimum Long/MIN_VALUE
                     :missing-fact :signed-64-bit-integer}))
    (if (nil? value) "nil" (str value))))

(defn- push-scalar
  [source-path value]
  (let [kind (scalar-kind value)
        rendered (checked-rendered-text source-path value)
        rendered-bytes (utf8-bytes rendered)]
    (when (> (alength rendered-bytes) value-limit)
      (bounds-fail! source-path "scalar exceeds native value byte bound"
                    {:observed-value-bytes (alength rendered-bytes)
                     :maximum-value-bytes value-limit
                     :missing-fact :bounded-native-value}))
    {:instructions
     [(case kind
        :nil "push-nil"
        :bool (str "push-bool " rendered)
        :integer (str "push-int " rendered)
        :string (str "push-string " (hex-encode rendered-bytes)))]
     :value value
     :rendered rendered
     :stack-delta 1
     :maximum-relative-depth 1
     :stdout ""}))

(declare lower-expression)

(defn- combine-evaluations
  [evaluations]
  (loop [remaining (seq evaluations)
         instructions []
         stdout ""
         depth 0
         maximum-depth 0]
    (if-let [evaluation (first remaining)]
      (let [next-depth (+ depth (:stack-delta evaluation))]
        (recur (next remaining)
               (into instructions (:instructions evaluation))
               (str stdout (:stdout evaluation))
               next-depth
               (max maximum-depth
                    (+ depth (:maximum-relative-depth evaluation)))))
      {:instructions instructions
       :stdout stdout
       :stack-delta depth
       :maximum-relative-depth maximum-depth})))

(defn- lower-call
  [source-path instruction call-kind]
  (let [function (if (= call-kind :println)
                   'println
                   (:function instruction))
        args (:args instruction)]
    (when-not (and (vector? args) (<= 1 (count args) 2))
      (plan-fail! source-path "native builtin arity must be one or two"
                  {:unsupported-op call-kind
                   :observed-function function
                   :observed-arity (when (sequential? args) (count args))
                   :supported-arities [1 2]
                   :missing-fact :bounded-native-builtin-arity}))
    (when-not (or (= 'str function) (= 'println function))
      (plan-fail! source-path "native packet plan contains an unsupported call"
                  {:unsupported-op call-kind
                   :observed-function function
                   :supported-functions ['str 'println]
                   :missing-fact :bounded-native-builtin}))
    (let [lowered-args (mapv #(lower-expression source-path %) args)
          combined (combine-evaluations lowered-args)
          rendered-values (mapv :rendered lowered-args)
          result-text (if (= 'str function)
                        (apply str rendered-values)
                        "nil")
          result-bytes (utf8-bytes result-text)
          emitted (if (= 'println function)
                    (str (str/join " " rendered-values) "\n")
                    "")]
      (when (> (alength result-bytes) value-limit)
        (bounds-fail! source-path "builtin result exceeds native value bound"
                      {:observed-value-bytes (alength result-bytes)
                       :maximum-value-bytes value-limit
                       :unsupported-op call-kind
                       :missing-fact :bounded-native-value}))
      (assoc combined
             :instructions (conj (:instructions combined)
                                 (str (name function) " " (count args)))
             :value (if (= 'str function) result-text nil)
             :rendered result-text
             :stdout (str (:stdout combined) emitted)
             :stack-delta (+ (:stack-delta combined) 1 (- (count args)))
             :maximum-relative-depth
             (max (:maximum-relative-depth combined)
                  (:stack-delta combined))))))

(defn- lower-expression
  [source-path instruction]
  (when-not (map? instruction)
    (plan-fail! source-path "native packet instruction is malformed"
                {:observed-instruction instruction
                 :missing-fact :native-instruction-map}))
  (case (:op instruction)
    :literal
    (push-scalar source-path (:value instruction))

    :quote
    (push-scalar source-path (:value instruction))

    :builtin-call
    (lower-call source-path instruction :builtin-call)

    :println
    (lower-call source-path instruction :println)

    (plan-fail! source-path "native packet plan contains an unsupported operation"
                {:unsupported-op (:op instruction)
                 :supported-operations [:literal :quote :builtin-call
                                        :println]
                 :missing-fact :bounded-native-plan-operation})))

(defn- validate-entrypoint!
  [source-path plan]
  (let [entrypoint (:entrypoint plan)
        functions (:functions plan)
        function (when (map? functions) (get functions entrypoint))]
    (when-not (and (= :gravity/stage2-hosted-core-compiled-plan (:kind plan))
                   (symbol? entrypoint)
                   (map? function)
                   (zero? (:arity function))
                   (empty? (:params function))
                   (vector? (:instructions function))
                   (= 1 (count functions)))
      (plan-fail! source-path "native packet requires one zero-arity entrypoint"
                  {:observed-plan-kind (:kind plan)
                   :observed-entrypoint entrypoint
                   :observed-function-count
                   (when (map? functions) (count functions))
                   :observed-entrypoint-arity (:arity function)
                   :missing-fact :single-zero-arity-native-entrypoint}))
    (let [lowered (combine-evaluations
                   (mapv #(lower-expression source-path %)
                         (:instructions function)))]
      (assoc lowered :entrypoint entrypoint))))

(defn- validate-authority!
  [source-path plan lowered]
  (let [println-count (count (filter #(str/starts-with? % "println ")
                                     (:instructions lowered)))
        str-count (count (filter #(str/starts-with? % "str ")
                                 (:instructions lowered)))
        declared-effects (set (get-in plan [:effect-summary :declared] #{}))
        inferred-effects (set (get-in plan [:effect-summary :inferred] #{}))
        capabilities (set (get-in plan [:module :capabilities] #{}))
        required-effects (cond-> #{}
                           (pos? str-count) (conj :memory/allocate)
                           (pos? println-count) (conj :io/write))
        required-capabilities (cond-> #{}
                                (pos? str-count) (conj :memory/allocator)
                                (pos? println-count) (conj :io/stdout))
        ;; The authenticated stage2 emitter currently reports println in its
        ;; generic inferred summary but does not infer allocation for str.
        ;; Preserve that gap explicitly: str authority is derived here from
        ;; the authenticated operation and must be declared and granted.
        required-inferred-effects (cond-> #{}
                                    (pos? println-count) (conj :io/write))]
    (when-not (and (every? declared-effects required-effects)
                   (every? inferred-effects required-inferred-effects)
                   (every? capabilities required-capabilities))
      (plan-fail! source-path "native lowering lacks effect or capability authority"
                  {:declared-effects declared-effects
                   :inferred-effects inferred-effects
                   :declared-capabilities capabilities
                   :required-effects required-effects
                   :required-inferred-effects required-inferred-effects
                   :required-capabilities required-capabilities
                   :missing-fact :native-effect-capability-authority}))
    {:declared-effects declared-effects
     :inferred-effects inferred-effects
     :declared-capabilities capabilities
     :required-effects required-effects
     :required-inferred-effects required-inferred-effects
     :required-capabilities required-capabilities}))

(defn- build-wire!
  [source-path source-text runtime-rule-sha lowered]
  (let [instructions (conj (:instructions lowered) "halt")
        instruction-count (count instructions)
        payload (str (str/join "\n" instructions) "\n")
        payload-bytes (utf8-bytes payload)
        stdout-bytes (utf8-bytes (:stdout lowered))
        source-path-bytes (utf8-bytes source-path)]
    (when (> instruction-count instruction-limit)
      (bounds-fail! source-path "native instruction count exceeds bound"
                    {:observed-instructions instruction-count
                     :maximum-instructions instruction-limit
                     :missing-fact :bounded-native-instruction-count}))
    (when (> (:maximum-relative-depth lowered) stack-limit)
      (bounds-fail! source-path "native value stack exceeds bound"
                    {:observed-stack-depth (:maximum-relative-depth lowered)
                     :maximum-stack-values stack-limit
                     :missing-fact :bounded-native-value-stack}))
    (when (> (alength stdout-bytes) output-limit)
      (bounds-fail! source-path "native stdout exceeds bound"
                    {:observed-output-bytes (alength stdout-bytes)
                     :maximum-output-bytes output-limit
                     :missing-fact :bounded-native-output}))
    (when (or (zero? (alength source-path-bytes))
              (> (alength source-path-bytes) value-limit)
              (not (re-matches #"[A-Za-z0-9/._-]+" source-path)))
      (bounds-fail! source-path "source path is not representable by native wire"
                    {:observed-source-path-bytes (alength source-path-bytes)
                     :maximum-source-path-bytes value-limit
                     :missing-fact :canonical-native-source-path}))
    (let [source-sha (sha256-bytes-hex (utf8-bytes source-text))
          payload-sha (sha256-bytes-hex payload-bytes)
          text (str "gravity-native-runtime-v1\n"
                    "rule-sha256 " runtime-rule-sha "\n"
                    "source-path-hex " (hex-encode source-path-bytes) "\n"
                    "source-sha256 " source-sha "\n"
                    "payload-sha256 " payload-sha "\n"
                    "instruction-count " instruction-count "\n"
                    "--\n" payload)
          bytes (utf8-bytes text)]
      (when (> (alength bytes) packet-limit)
        (bounds-fail! source-path "native packet wire exceeds bound"
                      {:observed-packet-bytes (alength bytes)
                       :maximum-packet-bytes packet-limit
                       :missing-fact :bounded-native-packet}))
      {:format "gravity-native-runtime-v1"
       :text text
       :bytes bytes
       :content-hash (str "sha256:" (sha256-bytes-hex bytes))
       :rule-sha256 runtime-rule-sha
       :source-path-hex (hex-encode source-path-bytes)
       :source-sha256 source-sha
       :payload payload
       :payload-sha256 payload-sha
       :instruction-count instruction-count
       :packet-bytes (alength bytes)})))

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
                          runtime-rule-sha lowered)
        compiler-record (:stage2-compiler-artifact-record packet)]
    {:artifact :gravity/p15-native-runtime-packet-binding
     :schema-version 1
     :status :complete-for-internal-bounded-native-runtime-provider
     :source {:path source-path
              :content-hash (:source-content-hash context)
              :extension (if (str/ends-with? source-path ".qst") ".qst"
                             ".gravity")
              :content-hash-verified-by-provider? false}
     :plan {:plan-id (:plan-id plan)
            :entrypoint (:entrypoint plan)
            :kind (:kind plan)
            :instruction-summary (:instruction-summary plan)
            :effect-summary (:effect-summary plan)
            :content-hash
            (sha256-text
             (pr-str
              (bootstrap/c-backend-canonical-value
               (select-keys plan [:kind :entrypoint :functions
                                  :binding-table :instruction-summary
                                  :effect-summary]))))}
     :compiler {:artifact (:artifact compiler-record)
                :artifact-hash (:artifact-hash compiler-record)
                :source-content-hash (:source-content-hash compiler-record)
                :semantic-hash (:semantic-hash compiler-record)
                :plan-assembly-artifact-hash
                (:plan-assembly-artifact-hash compiler-record)}
     :emitter {:source-rule-hash
               (get-in packet [:stage2-plan-emitter-rule :source-rule-hash])}
     :driver {:driver-rule-hash
              (get-in packet [:stage2-compiler-driver-rule
                              :driver-rule-hash])
              :record-status
              (get-in packet [:stage2-compiler-driver-record :status])}
     :runtime {:runtime-rule-hash
               (get-in packet [:stage2-runtime-rule :runtime-rule-hash])
               :runtime-artifact-hash
               (get-in packet [:stage2-runtime-rule :runtime-artifact-hash])
               :execution-status
               (get-in packet [:stage2-runtime-execution-record :status])}
     :runtime-contract (assoc runtime-contract
                              :wire-rule-sha256 runtime-rule-sha)
     :provider (assoc provider
                      :implementation :host-authored-c
                      :provider-kind :host-c
                      :runtime-provider
                      :gravity.native/libsystem-stdio-v1)
     :target {:requested :c
              :provider-host-language :c
              :provider-target :arm64-macos
              :profile :native
              :eligibility (:target-eligibility packet)}
     :effects (select-keys authority
                           [:declared-effects :inferred-effects
                            :required-effects :required-inferred-effects])
     :capabilities (select-keys authority
                                [:declared-capabilities
                                 :required-capabilities])
     :provenance
     {:actual-paths (merge
                     {:source source-path
                      :native-runtime-contract runtime-contract-relative
                      :native-runtime-provider provider-relative}
                     (get-in packet [:provenance :actual-paths]))
      :selected-runtime-clojure-seed-boundary? false
      :selected-child-clojure-seed-boundary? false
      :adapter-clojure-seed-boundary? true
      :compiler-clojure-seed-boundary? true
      :verifier-clojure-seed-boundary? true
      :artifact-clojure-seed-boundary? true
      :artifact-construction-clojure-seed-boundary? true
      :process-clojure-seed-boundary? true
      :file-io-clojure-seed-boundary? true
      :process-and-file-io-clojure-seed-boundary? true
      :public-clojure-seed-boundary? true
      :public-wrapper-clojure-seed-boundary? true
      :global-clojure-seed-boundary? true}
     :wire wire
     :expected-stdout expected-stdout
     :expected-stdout-hash (sha256-text expected-stdout)
     :source-content-hash-verified-by-provider? false
     :source-hash-verification :provider-unverified
     :public-command-route? false
     :compiler-authored-in-gravity? false
     :provider-authored-in-gravity? false
     :backend-complete? false
     :full-language? false
     :whole-language? false
     :formal-language-complete? false
     :self-hosted? false
     :release-ready? false
     :seedless-release? false}))
