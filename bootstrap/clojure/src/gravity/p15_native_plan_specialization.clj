(ns gravity.p15-native-plan-specialization
  "Authenticated plan-specialized C artifact for the bounded native child.

  This namespace deliberately stops at an emitted C translation unit.  The
  existing public C backend owns compilation/process supervision; duplicating
  that machinery here would widen the trusted boundary.  Focused tests compile
  and run the returned source in a test-owned private directory instead.
  "
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap])
  (:import [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption OpenOption Path Paths]
           [java.nio.file.attribute BasicFileAttributes]))

(def ^:private max-plan-instructions 128)
(def ^:private max-generated-source-bytes 65536)
(def ^:private max-reference-output-bytes 8192)
(def ^:private max-scalar-bytes 1024)
(def ^:private max-helper-source-bytes 65536)
(def ^:private helper-source-relative
  "bootstrap/gravity/p15_s23/native_plan_c_emitter.gravity")
(def ^:private helper-function
  'p15-s23-native-c-emit-plan)
(def ^:private helper-contract
  :p15-s23-native-plan-c-emitter-v1)
(def ^:private helper-function-shape
  {:function helper-function
   :arity 1
   :params ['request]})
(def ^:private no-follow-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

;; Filled after the source is finalized. Keeping this hash pinned prevents a
;; classpath or worktree substitution from silently changing the helper.
(def ^:private helper-source-content-hash
  "sha256:04645a82e66d024c6505ea3ec80c9789a7d0545ef8e7222c5d78cad68fc92adc")

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

(defn- helper-contract-fail!
  [source-path message facts]
  (fail! "P15GCE001" message source-path
         (merge {:diagnostic-family :gravity-c-emitter-helper-contract
                 :remediation :restore_the_pinned_gravity_c_emitter_helper}
                facts)))

(defn- helper-rejected-fail!
  [source-path message facts]
  (fail! "P15GCE002" message source-path
         (merge {:diagnostic-family :gravity-c-emitter-authenticated-subset
                 :remediation :restrict_to_printable_ascii_string_println_and_str}
                facts)))

(defn- repository-root
  []
  (let [resource (io/resource "gravity/p15_native_plan_specialization.clj")]
    (when-not resource
      (helper-contract-fail!
       nil
       "native plan specialization source is not on the classpath"
       {:missing-fact :specialization-source-resource}))
    (loop [candidate
           (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (helper-contract-fail!
         nil
         "repository root is unavailable for the Gravity C emitter helper"
         {:missing-fact :repository-root})

        (Files/isRegularFile (.resolve ^Path candidate "deps.edn")
                             (make-array LinkOption 0))
        candidate

        :else
        (recur (.getParent ^Path candidate))))))

(def ^:private root (delay (repository-root)))

(defn- helper-source-path!
  [request-source]
  (let [repository-root (.normalize (.toAbsolutePath ^Path @root))
        relative-path (Paths/get helper-source-relative
                                 (make-array String 0))
        source-path (.normalize (.resolve repository-root relative-path))]
    (when-not (.startsWith source-path repository-root)
      (helper-contract-fail!
       request-source
       "Gravity C emitter helper escaped the repository root"
       {:missing-fact :bounded-helper-source-location
        :source-path (str source-path)}))
    ;; Check every path component without following links. This keeps the
    ;; source identity rooted even when a parent component is replaced.
    (loop [current repository-root
           components (seq (iterator-seq
                            (.iterator
                             (.relativize repository-root source-path))))]
      (let [attributes
            (try
              (Files/readAttributes current BasicFileAttributes
                                     no-follow-options)
              (catch java.io.IOException error
                (helper-contract-fail!
                 request-source
                 "Gravity C emitter helper path is unreadable"
                 {:missing-fact :bounded-helper-source-location
                  :source-path (str source-path)
                  :observed-component (str current)
                  :cause-message (.getMessage error)})))]
        (when (or (.isSymbolicLink attributes)
                  (and (seq components) (not (.isDirectory attributes)))
                  (and (nil? components) (not (.isRegularFile attributes))))
          (helper-contract-fail!
           request-source
           "Gravity C emitter helper path is not a regular non-symlink file"
           {:missing-fact :bounded-helper-source-location
            :source-path (str source-path)
            :observed-component (str current)
            :symbolic-link? (.isSymbolicLink attributes)
            :directory? (.isDirectory attributes)
            :regular-file? (.isRegularFile attributes)}))
        (if-let [component (first components)]
          (recur (.resolve ^Path current ^Path component)
                 (next components))
          {:source-path source-path
           :attributes attributes})))))

(defn- strict-utf8
  [request-source source-path bytes]
  (try
    (let [decoder
          (doto (.newDecoder StandardCharsets/UTF_8)
            (.onMalformedInput CodingErrorAction/REPORT)
            (.onUnmappableCharacter CodingErrorAction/REPORT))]
      (.toString (.decode decoder (ByteBuffer/wrap ^bytes bytes))))
    (catch java.nio.charset.CharacterCodingException error
      (helper-contract-fail!
       request-source
       "Gravity C emitter helper is not strict UTF-8"
       {:missing-fact :strict-utf8-helper-source
        :source-path (str source-path)
        :cause-message (.getMessage error)}))))

(defn- helper-source-snapshot!
  "Read one bounded, regular-file, NOFOLLOW UTF-8 snapshot of the tracked
  Gravity helper. The open channel and before/after identity checks close the
  replacement window between path validation and hashing."
  [request-source]
  (let [{:keys [source-path attributes]} (helper-source-path! request-source)
        before-size (.size ^BasicFileAttributes attributes)]
    (when (> before-size max-helper-source-bytes)
      (helper-contract-fail!
       request-source
       "Gravity C emitter helper source exceeds its bounded snapshot size"
       {:maximum-helper-source-bytes max-helper-source-bytes
        :observed-helper-source-bytes before-size
        :missing-fact :bounded-helper-source-snapshot}))
    (let [buffer (ByteBuffer/allocate (inc max-helper-source-bytes))]
      (try
        (with-open [channel
                    (java.nio.channels.FileChannel/open
                     source-path
                     (into-array OpenOption
                                 [java.nio.file.StandardOpenOption/READ
                                  LinkOption/NOFOLLOW_LINKS]))]
          (let [channel-size-before (.size channel)
                observed-byte-count
                (loop [zero-reads 0]
                  (if-not (.hasRemaining buffer)
                    (.position buffer)
                    (let [read-count (.read channel buffer)]
                      (cond
                        (neg? read-count) (.position buffer)
                        (zero? read-count)
                        (if (= 8 zero-reads)
                          (helper-contract-fail!
                           request-source
                           "Gravity C emitter helper source did not make progress"
                           {:missing-fact :bounded-helper-source-read
                            :source-path (str source-path)})
                          (recur (inc zero-reads)))
                        :else
                        (recur 0)))))
                channel-size-after (.size channel)
                after
                (try
                  (Files/readAttributes source-path BasicFileAttributes
                                         no-follow-options)
                  (catch java.io.IOException error
                    (helper-contract-fail!
                     request-source
                     "Gravity C emitter helper source disappeared while reading"
                     {:missing-fact :stable-helper-source-snapshot
                      :source-path (str source-path)
                      :cause-message (.getMessage error)})))
                bytes (java.util.Arrays/copyOf (.array buffer)
                                               observed-byte-count)
                content-hash
                (str "sha256:" (bootstrap/sha256-bytes-hex bytes))]
            (when-not
             (and (= channel-size-before channel-size-after
                     (long observed-byte-count))
                  (= (.fileKey ^BasicFileAttributes attributes)
                     (.fileKey ^BasicFileAttributes after))
                  (= (.lastModifiedTime ^BasicFileAttributes attributes)
                     (.lastModifiedTime ^BasicFileAttributes after))
                  (= (.size ^BasicFileAttributes attributes)
                     (.size ^BasicFileAttributes after)
                     (long observed-byte-count))
                  (<= observed-byte-count max-helper-source-bytes))
              (helper-contract-fail!
               request-source
               "Gravity C emitter helper source changed while being read"
               {:missing-fact :stable-helper-source-snapshot
                :source-path (str source-path)
                :observed-helper-source-bytes observed-byte-count}))
            {:source-path (str source-path)
             :source-byte-count observed-byte-count
             :source-content-hash content-hash
             :source-text (strict-utf8 request-source source-path bytes)}))
        (catch java.io.IOException error
          (helper-contract-fail!
           request-source
           "Gravity C emitter helper source cannot be read"
           {:missing-fact :stable-helper-source-snapshot
            :source-path (str source-path)
            :cause-message (.getMessage error)}))))))

(def ^:dynamic *p15-native-plan-c-emitter-source-loader*
  helper-source-snapshot!)

(defn- utf8-bytes
  [value]
  (.getBytes ^String (str value) StandardCharsets/UTF_8))

(defn- printable-ascii-string?
  [value]
  (and (string? value)
       (every? (fn [character]
                 (<= 0x20 (int character) 0x7e))
               value)
       ;; The Gravity helper deliberately delegates literal spelling to the
       ;; stage0 `pr-str` primitive. C11 recognizes trigraphs before parsing
       ;; string escapes, so a raw printable sequence such as `??/` is not a
       ;; safe C literal spelling. Reject every trigraph introducer until a
       ;; Gravity-authored byte encoder owns C escaping.
       (not (re-find #"\?\?[=/'()!<>-]" value))))

(defn- helper-scalar-safe?
  [instruction]
  (case (:op instruction)
    :literal (printable-ascii-string? (:value instruction))
    :quote (printable-ascii-string? (:value instruction))
    :builtin-call
    (and (= 'str (:function instruction))
         (seq (:args instruction))
         (every? helper-scalar-safe? (:args instruction)))
    false))

(defn- helper-statement-safe?
  [instruction]
  (case (:op instruction)
    ;; Literal and quote statements are no-ops in the public C subset. They
    ;; need no C representation and therefore do not weaken the proof.
    :literal true
    :quote true
    :println (every? helper-scalar-safe? (:args instruction))
    :do (every? helper-statement-safe? (:body instruction))
    false))

(defn- helper-safety-proof
  [plan]
  (let [entrypoint (:entrypoint plan)
        entry-function (get-in plan [:functions entrypoint])
        instructions (:instructions entry-function)
        safe? (and (vector? instructions)
                    (every? helper-statement-safe? instructions))]
    {:safe? safe?
     :facts (if safe?
              {:contract helper-contract
               :proof :printable-ascii-string-println-str
               :non-ascii-allowed? false
               :control-allowed? false
               :nul-allowed? false
               :c11-trigraph-sequence-allowed? false}
              {:contract helper-contract
               :proof :printable-ascii-string-println-str
               :non-ascii-allowed? false
               :control-allowed? false
               :nul-allowed? false
               :c11-trigraph-sequence-allowed? false
               :missing-fact :gravity-c-emitter-printable-ascii-subset
               :observed-instructions instructions})}))

(defn- helper-function-semantic-hash
  [definition]
  (str "sha256:"
       (bootstrap/sha256-hex
        (pr-str (bootstrap/c-backend-canonical-value definition)))))

(defn- helper-contract-hash
  [source-content-hash]
  (str "sha256:"
       (bootstrap/sha256-hex
        (pr-str
         (bootstrap/c-backend-canonical-value
          {:source-content-hash source-content-hash
           :function-shape helper-function-shape
           :contract helper-contract})))))

(defn- compile-gravity-c-emitter-helper!
  [request-source]
  (let [snapshot
        (try
          (*p15-native-plan-c-emitter-source-loader* request-source)
          (catch clojure.lang.ExceptionInfo error
            (throw error))
          (catch Throwable error
            (helper-contract-fail!
             request-source
             "Gravity C emitter helper source loader failed"
             {:missing-fact :gravity-c-emitter-source-loader
              :cause-message (.getMessage error)})))
        _ (when-not (and (map? snapshot)
                         (= #{:source-path :source-byte-count
                              :source-content-hash :source-text}
                            (set (keys snapshot)))
                         (string? (:source-path snapshot))
                         (string? (:source-text snapshot))
                         (integer? (:source-byte-count snapshot))
                         (string? (:source-content-hash snapshot)))
            (helper-contract-fail!
             request-source
             "Gravity C emitter helper source snapshot is malformed"
             {:missing-fact :gravity-c-emitter-source-snapshot
              :observed-snapshot snapshot}))
        _ (when-not (str/ends-with? (:source-path snapshot)
                                     helper-source-relative)
            (helper-contract-fail!
             request-source
             "Gravity C emitter helper source path is not the tracked helper"
             {:missing-fact :gravity-c-emitter-source-path
              :expected-relative-path helper-source-relative
              :observed-source-path (:source-path snapshot)}))
        _ (when (> (:source-byte-count snapshot) max-helper-source-bytes)
            (helper-contract-fail!
             request-source
             "Gravity C emitter helper source snapshot exceeds its bound"
             {:maximum-helper-source-bytes max-helper-source-bytes
              :observed-helper-source-bytes (:source-byte-count snapshot)
              :missing-fact :bounded-helper-source-snapshot}))
        actual-source-content-hash
        (str "sha256:" (bootstrap/sha256-hex (:source-text snapshot)))]
    (when-not (= actual-source-content-hash
                (:source-content-hash snapshot)
                helper-source-content-hash)
      (helper-contract-fail!
       request-source
       "Gravity C emitter helper source content hash is not pinned"
       {:expected-source-content-hash helper-source-content-hash
        :observed-source-content-hash actual-source-content-hash
        :snapshot-source-content-hash (:source-content-hash snapshot)
        :missing-fact :pinned-gravity-c-emitter-source-content-hash}))
    (let [emitter-rule
          (try
            (bootstrap/c-backend-stage2-plan-emitter-source-rule!
             request-source :jvm)
            (catch clojure.lang.ExceptionInfo error
              (helper-contract-fail!
               request-source
               "Pinned Gravity plan-emitter rule could not be loaded"
               {:missing-fact :pinned-stage2-plan-emitter-rule
                :cause-diagnostic (:id (ex-data error))
                :cause-facts (ex-data error)}))
            (catch Throwable error
              (helper-contract-fail!
               request-source
               "Pinned Gravity plan-emitter rule could not be loaded"
               {:missing-fact :pinned-stage2-plan-emitter-rule
                :cause-message (.getMessage error)})))
          helper-plan
          (try
            (bootstrap/p15-s23-stage2-plan-emitter-compile-source
             (:emitter emitter-rule)
             (:source-path snapshot)
             (:source-text snapshot))
            (catch clojure.lang.ExceptionInfo error
              (helper-contract-fail!
               request-source
               "Gravity C emitter helper source did not compile"
               {:missing-fact :gravity-c-emitter-source-compilation
                :cause-diagnostic (:id (ex-data error))
                :cause-facts (ex-data error)}))
            (catch Throwable error
              (helper-contract-fail!
               request-source
               "Gravity C emitter helper source did not compile"
               {:missing-fact :gravity-c-emitter-source-compilation
                :cause-message (.getMessage error)})))
          definition (get-in helper-plan [:functions helper-function])
          observed-shape (when (map? definition)
                           {:function helper-function
                            :arity (:arity definition)
                            :params (:params definition)})]
      (when-not (and (map? helper-plan)
                     (= :gravity/stage2-hosted-core-compiled-plan
                        (:kind helper-plan))
                     (= :hosted (get-in helper-plan [:module :profile]))
                     (= :jvm (get-in helper-plan [:module :target]))
                     (= 'main (:entrypoint helper-plan))
                     (map? definition)
                     (= helper-function-shape observed-shape))
        (helper-contract-fail!
         request-source
         "Gravity C emitter helper export shape is not exact"
         {:missing-fact :gravity-c-emitter-export-shape
          :expected-function-shape helper-function-shape
          :observed-function-shape observed-shape
          :observed-plan-kind (:kind helper-plan)
          :observed-plan-entrypoint (:entrypoint helper-plan)}))
      {:snapshot snapshot
       :emitter-rule emitter-rule
       :plan helper-plan
       :function definition
       :function-semantic-hash (helper-function-semantic-hash definition)
       :contract-hash (helper-contract-hash
                       (:source-content-hash snapshot))})))

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
    (let [helper (compile-gravity-c-emitter-helper! source-path)
          safety (helper-safety-proof plan)
          helper-result
          (try
            ;; The flag is an explicit record of the compiler-artifact host
            ;; boundary requested by this helper. It does not relabel the
            ;; ordinary helper plan as a compiler artifact.
            (bootstrap/p15-s23-stage2-runtime-execute-function
             {:engine :gravity-native-plan-c-emitter-host-runner
              :compiler-artifact-plan? true}
             (:plan helper)
             helper-function
             [{:plan plan
               :safe-printable-ascii? (:safe? safety)
               :safety-facts (:facts safety)}])
            (catch clojure.lang.ExceptionInfo error
              (helper-contract-fail!
               source-path
               "Gravity C emitter helper invocation failed"
               {:missing-fact :gravity-c-emitter-runtime-invocation
                :cause-diagnostic (:id (ex-data error))
                :cause-facts (ex-data error)}))
            (catch Throwable error
              (helper-contract-fail!
               source-path
               "Gravity C emitter helper invocation failed"
               {:missing-fact :gravity-c-emitter-runtime-invocation
                :cause-message (.getMessage error)})))
          _ (when-not (map? helper-result)
              (helper-contract-fail!
               source-path
               "Gravity C emitter helper returned a malformed record"
               {:missing-fact :gravity-c-emitter-result-record
                :observed-result helper-result}))
          _ (when (= :rejected (:status helper-result))
              (helper-rejected-fail!
               source-path
               "authenticated plan is outside the Gravity C emitter subset"
               {:helper-diagnostic (:diagnostic helper-result)
                :helper-facts (:facts helper-result)
                :missing-fact
                (or (get-in helper-result [:facts :missing-fact])
                    :gravity-c-emitter-authenticated-subset)}))
          _ (when-not (and (= :complete (:status helper-result))
                           (= helper-contract (:contract helper-result))
                           (= :gravity-source (:implementation helper-result))
                           (string? (:source helper-result)))
              (helper-contract-fail!
               source-path
               "Gravity C emitter helper returned the wrong completion record"
               {:missing-fact :gravity-c-emitter-result-contract
                :expected-contract helper-contract
                :observed-result helper-result}))
          source (:source helper-result)
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
       :expected-output expected-output
       :helper helper
       :helper-safety safety
       :helper-result helper-result})))

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
        {:keys [source source-bytes source-content-hash expected-output bounds
                helper helper-safety helper-result]}
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
     :emitter {:emitter 'gravity.p15-native-plan-specialization/gravity-source-c-emitter
               :source-rule-hash
               (get-in packet [:stage2-plan-emitter-rule :source-rule-hash])
               :status :emitted
               :semantic-owner :gravity-source
               :source-language :gravity
               :helper-source-path
               (get-in helper [:snapshot :source-path])
               :helper-source-content-hash
               (get-in helper [:snapshot :source-content-hash])
               :helper-function helper-function
               :helper-function-semantic-hash
               (:function-semantic-hash helper)
               :helper-contract helper-contract
               :helper-contract-hash (:contract-hash helper)
               :helper-safety-proof (:facts helper-safety)
               :helper-result-contract
               (select-keys helper-result
                            [:status :contract :implementation])}
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
                   :implementation :gravity-source-emitted-plan-specialized-c
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
                  :c-emitter-semantic-owner :gravity-source
                  :c-emitter-source-language :gravity
                  :c-emitter-helper-executed? true
                  :c-emitter-pr-str-primitive-boundary? true
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
