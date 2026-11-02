

(defn js-ts-backend-file-artifact
  ([path] (js-ts-backend-file-artifact path {}))
  ([path options]
   (js-ts-backend-source-artifact
    path (read-gravity-source-text path) options)))

;; ---------------------------------------------------------------------------
;; Hosted JVM target (Java 21 class files and modular executable JAR)
;;
;; This is an opt-in target over the same authenticated stage2 packet as the
;; runtime-derived C and Node targets.  The generated Java executes the closed
;; scalar/control-flow plan; it does not embed stage2 stdout.  javac remains an
;; external target tool and the compiler remains inside the Clojure seed
;; boundary, so this slice deliberately does not claim full B5 conformance.

(def jvm-backend-target :jvm)
(def jvm-backend-target-release 21)
(def jvm-backend-required-classfile-major 65)
(def jvm-backend-module-name "gravity.stage")
(def jvm-backend-main-class "gravity.stage2.Program")
(def ^:dynamic *jvm-backend-javac-command* "javac")
(def ^:dynamic *jvm-backend-java-command* "java")

(defn jvm-backend-fail!
  [id message source-path subject extra]
  (fail! id message
         (merge {:severity :error
                 :stage :jvm-backend-lowering
                 :diagnostic-family
                 (cond
                   (str/starts-with? id "B5") :b5-jvm-backend
                   (str/starts-with? id "B13") :b13-artifact-emission
                   (str/starts-with? id "B14") :b14-backend-conformance
                   :else :c14-target-lowering)
                 :backend :gravity.backend/jvm
                 :target jvm-backend-target
                 :source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :primary {:span (or (:source-span subject)
                                     (source-span source-path 0))}
                 :fallback-status :rejected
                 :remediation
                 "Use --target jvm --lowering runtime-derived with the closed Java 21 hosted subset."}
                extra)))

(defn jvm-backend-validate-plan!
  [source-path plan]
  (try
    ;; Reuse the shared iterative scalar/control-flow validator, but remap its
    ;; historical C diagnostic at this concrete backend boundary.
    (c-backend-validate-runtime-plan! source-path jvm-backend-target plan)
    (catch clojure.lang.ExceptionInfo ex
      (if (= "B2-UNSUPPORTED" (:id (ex-data ex)))
        (jvm-backend-fail!
         "C14-UNSUPPORTED"
         "JVM backend cannot lower this stage2 instruction plan"
         source-path plan
         {:unsupported-op (:unsupported-op (ex-data ex))
          :missing-fact :jvm-stage2-lowering-rule})
        (throw ex))))
  :passed)

(defn jvm-backend-validate-packet!
  [source-path packet trusted-emitter-rule trusted-driver-rule
   trusted-runtime-rule trusted-plan packet-context]
  (let [emitter-hash (get-in packet
                             [:stage2-plan-emitter-rule :source-rule-hash])
        trusted-emitter-hash (:source-rule-hash trusted-emitter-rule)
        driver-hash (get-in packet
                            [:stage2-compiler-driver-rule :driver-rule-hash])
        trusted-driver-hash (:driver-rule-hash trusted-driver-rule)
        runtime-hash (get-in packet
                             [:stage2-runtime-rule :runtime-rule-hash])
        trusted-runtime-hash (:runtime-rule-hash trusted-runtime-rule)
        runtime-artifact-hash
        (get-in packet [:stage2-runtime-rule :runtime-artifact-hash])
        trusted-runtime-artifact-hash
        (:runtime-artifact-hash trusted-runtime-rule)
        compiler-record (:stage2-compiler-artifact-record packet)
        packet-plan (:plan packet)
        driver-plan (:stage2-plan (:stage2-compiler-driver-record packet))
        plan-shape-keys
        [:kind :entrypoint :functions :binding-table
         :instruction-summary :effect-summary]
        plan-compiler (get-in packet [:plan :compiler])
        runtime-record (:stage2-runtime-execution-record packet)
        driver-record (:stage2-compiler-driver-record packet)]
    (when-not
     (and (= :gravity/target-neutral-stage2-runtime-packet (:kind packet))
          (= :complete (:status packet))
          (= :jvm (:requested-target packet))
          (= {:status :accepted
              :source-declared-target :jvm
              :requested-target :jvm
              :selection :source-and-request-agree}
             (:target-eligibility packet))
          (map? packet-plan)
          (= (c-backend-canonical-value
              (select-keys trusted-plan plan-shape-keys))
             (c-backend-canonical-value
              (select-keys packet-plan plan-shape-keys)))
          (= (c-backend-canonical-value
              (select-keys trusted-plan plan-shape-keys))
             (c-backend-canonical-value
              (select-keys driver-plan plan-shape-keys)))
          (= (:plan-id trusted-plan) (:plan-id packet-plan))
          (= (:plan-id driver-plan) (:plan-id runtime-record))
          (= :complete (:status runtime-record))
          (= :complete (:status driver-record))
          (true? (:accepted-output-equivalent? driver-record))
          (= (:reference-output packet) (:stdout runtime-record))
          (string? emitter-hash)
          (boolean (re-matches #"sha256:[0-9a-f]{64}" emitter-hash))
          (= trusted-emitter-hash emitter-hash)
          (= trusted-driver-hash driver-hash)
          (= trusted-runtime-hash runtime-hash)
          (= trusted-runtime-artifact-hash runtime-artifact-hash)
          (p15-s23-closed-runtime-packet-authentic? packet packet-context)
          (p15-s23-stage2-compiler-artifact-record-authentic?
           compiler-record)
          (p15-s23-stage2-compiler-artifact-record-matches-plan?
           compiler-record packet-plan)
          (= (:artifact-hash compiler-record)
             (:expression-lowering-artifact-hash plan-compiler))
          (= (:source-content-hash compiler-record)
             (:expression-lowering-source-content-hash plan-compiler))
          (= (:semantic-hash compiler-record)
             (:expression-lowering-semantic-hash plan-compiler))
          (= (select-keys
              compiler-record
              [:plan-assembly-function :plan-assembly-artifact-hash
               :plan-assembly-source-content-hash
               :plan-assembly-semantic-hash :plan-assembly-invoked?
               :plan-assembly-generic-bridge-residual?])
             (select-keys
              plan-compiler
              [:plan-assembly-function :plan-assembly-artifact-hash
               :plan-assembly-source-content-hash
               :plan-assembly-semantic-hash :plan-assembly-invoked?
               :plan-assembly-generic-bridge-residual?])))
      (jvm-backend-fail!
       "C14-INPUT" "JVM backend received an unauthenticated stage2 packet"
       source-path (:plan packet)
       {:observed-packet-kind (:kind packet)
        :observed-packet-status (:status packet)
        :observed-plan-id (:plan-id packet-plan)
        :expected-plan-id (:plan-id trusted-plan)
        :observed-emitter-source-rule-hash emitter-hash
        :expected-emitter-source-rule-hash trusted-emitter-hash
        :observed-driver-rule-hash driver-hash
        :expected-driver-rule-hash trusted-driver-hash
        :observed-runtime-rule-hash runtime-hash
        :expected-runtime-rule-hash trusted-runtime-hash
        :observed-runtime-artifact-hash runtime-artifact-hash
        :expected-runtime-artifact-hash trusted-runtime-artifact-hash
        :missing-fact :authenticated-target-neutral-stage2-packet})))
  :passed)

(defn jvm-backend-byte-array-source
  [name bytes indent]
  (let [padding (apply str (repeat indent "  "))]
    (str padding "byte[] " name " = new byte[]{"
         (str/join "," (map #(str "(byte)" %) bytes)) "};\n")))

(defn jvm-backend-value-declaration
  [instruction counter indent]
  (let [name (str "gravityValue" (swap! counter inc))
        value (:value instruction)]
    {:source (jvm-backend-byte-array-source
              name (c-backend-runtime-bytes value) indent)
     :descriptor {:name name
                  :truth (not (or (nil? value) (false? value)))}}))

(defn jvm-backend-write-source
  [descriptor indent]
  (let [padding (apply str (repeat indent "  "))
        name (:name descriptor)]
    (str padding "System.out.write(" name ", 0, " name ".length);\n")))

(defn jvm-backend-test-source
  [instruction env]
  (case (:op instruction)
    :literal (if (or (nil? (:value instruction))
                     (false? (:value instruction))) "false" "true")
    :quote (if (or (nil? (:value instruction))
                   (false? (:value instruction))) "false" "true")
    :local (if (get-in env [(:name instruction) :truth]) "true" "false")
    "false"))

(declare jvm-backend-value-source)
(declare jvm-backend-instruction-source)