(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-stage2-sh02-authenticated-envelope-verification-report
  [artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-sh02-final-preflight! source-path artifact)
      (let [fresh-c11
            (p15-s23-stage2-c11-mir-artifact checked-core context)
            fresh-packet
            (p15-s23-c13-c14-b1-build-internal!
             p15-s23-c13-c14-b1-authority-token
             fresh-c11 checked-core context)
            expected
            (p15-s23-sh02-build-verified-packet-internal!
             p15-s23-c13-c14-b1-authority-token
             fresh-packet context)]
        (p15-s23-c11-mir-require-strict-structure!
         source-path expected artifact
         :fresh-contextual-sh02-envelope-reconstruction)
        (when-not (= expected artifact)
          (p15-s23-sh02-fail!
           source-path artifact :fresh-context-bound-sh02-envelope {}))
        (let [base
              {:artifact :gravity/sh02-contextual-verification-report
               :schema-version 1 :status :passed
               :artifact-id (:artifact-id artifact)
               :semantic-id (:semantic-id artifact)
               :packet-id (:packet-id artifact)
               :fresh-c11 :passed
               :fresh-c13 :passed
               :fresh-c14 :passed
               :fresh-b1 :passed
               :gravity-envelope-template-replay
               :template-replay-passed
               :host-digest-resolution :passed
               :identity-subject-equality :passed
               :fresh-envelope-reconstruction :passed
               :stages
               (into
                (sorted-map)
                (map
                 (fn [[stage envelope]]
                   [stage
                    {:semantic-envelope-id
                     (:semantic-envelope-id envelope)
                     :provenance-binding-id
                     (:provenance-binding-id envelope)
                     :identity-check-count
                     (count (:identity-checks envelope))
                     :request-count (:request-count envelope)}]))
                (:envelopes artifact))
               :external-tool-execution :not-performed
               :clojure-seed-boundary? true
               :self-hosted? false}]
          (assoc
           base :report-id
           (p15-s23-c6c10-canonical-digest
            source-path
            {:domain :gravity/sh02-contextual-verification-report-v1
             :report base}))))
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch StackOverflowError _
        (p15-s23-sh02-fail!
         source-path {} :bounded-sh02-verifier-host-stack {}))
      (catch AssertionError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-sh02-verifier-assertion error))
      (catch LinkageError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-sh02-verifier-linkage error))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-sh02-verifier-diagnostic exception))
      (catch Exception exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-sh02-verifier-host-failure exception)))))

(defn p15-s23-stage2-sh02-authenticated-envelope-verify!
  [artifact checked-core context]
  (let [report
        (p15-s23-stage2-sh02-authenticated-envelope-verification-report
         artifact checked-core context)]
    (when-not (= :passed (:status report))
      (p15-s23-sh02-fail!
       (p15-s23-c11-ingress-source-path context)
       artifact :sh02-contextual-verification-status {}))
    :passed))

(defn p15-s23-stage2-sh02-authenticated-envelope-authentic?
  ([artifact] false)
  ([artifact checked-core context]
   (try
     (= :passed
        (p15-s23-stage2-sh02-authenticated-envelope-verify!
         artifact checked-core context))
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread))
       (throw interrupted))
     (catch StackOverflowError _ false)
     (catch AssertionError _ false)
     (catch LinkageError _ false)
     (catch Exception _ false))))

(defn p15-s23-stage2-sh02-descriptor-envelope
  "Build one reusable SH-02 envelope directly from a bounded stage descriptor.

  This is the narrow reuse seam for later bootstrap slices.  The caller may
  choose the stage label and artifact kind, but never supplies executable
  Gravity code, a compiled plan, a digest resolver, or construction authority.
  The pinned SH-02 source is reloaded and reauthenticated on every call."
  [stage artifact-kind descriptor source-path]
  (try
    (when-not (and (keyword? stage) (keyword? artifact-kind))
      (p15-s23-sh02-fail!
       source-path descriptor :sh02-descriptor-envelope-stage-contract
       {:stage stage :artifact-kind artifact-kind}))
    (let [binding
          (p15-s23-sh02-source-binding!
           p15-s23-c13-c14-b1-authority-token source-path)
          packet {stage {:artifact artifact-kind}}]
      (p15-s23-sh02-build-stage-envelope!
       p15-s23-c13-c14-b1-authority-token
       stage packet descriptor binding source-path))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError _
      (p15-s23-sh02-fail!
       source-path {} :bounded-sh02-descriptor-envelope-host-stack {}))
    (catch AssertionError error
      (p15-s23-b3-llvm-contain-exception!
       source-path :contained-sh02-descriptor-envelope-assertion error))
    (catch LinkageError error
      (p15-s23-b3-llvm-contain-exception!
       source-path :contained-sh02-descriptor-envelope-linkage error))
    (catch clojure.lang.ExceptionInfo exception
      (p15-s23-b3-llvm-contain-exception!
       source-path :contained-sh02-descriptor-envelope-diagnostic exception))
    (catch Exception exception
      (p15-s23-b3-llvm-contain-exception!
       source-path :contained-sh02-descriptor-envelope-host-failure exception))))

(defn p15-s23-stage2-sh02-descriptor-envelope-verify!
  "Reconstruct a descriptor envelope from current pinned source and inputs."
  [artifact stage artifact-kind descriptor source-path]
  (let [expected
        (p15-s23-stage2-sh02-descriptor-envelope
         stage artifact-kind descriptor source-path)]
    (p15-s23-c11-mir-require-strict-structure!
     source-path expected artifact
     :fresh-sh02-descriptor-envelope-reconstruction)
    (when-not (= expected artifact)
      (p15-s23-sh02-fail!
       source-path artifact :fresh-sh02-descriptor-envelope-equality
       {:stage stage :artifact-kind artifact-kind}))
    :passed))

(def p15-s23-c-backend-diagnostic-rules
  (into
   (set (remove #(str/starts-with? % "B3-")
                p15-s23-b3-llvm-diagnostic-rules))
   ["B2-DIALECT" "B2-UB" "B2-ABI" "B2-POINTER"
    "B2-NUMERIC" "B2-RUNTIME" "B2-FFI" "B2-MMIO"
    "B2-MANIFEST"
    "B13-SCHEMA" "B13-HASH" "B13-PROVENANCE"
    "B13-SOURCEMAP" "B13-EVIDENCE" "B13-TARGET"
    "B13-CONFORMANCE" "B13-REPRODUCIBILITY" "B13-RELEASE"
    "B13-GRAPH"
    "B14-COVERAGE" "B14-TARGET" "B14-POSITIVE" "B14-NEGATIVE"
    "B14-DIFFERENTIAL" "B14-METADATA" "B14-ARTIFACT"
    "B14-NONDETERMINISM" "B14-SKIP" "B14-EVIDENCE"]))

(defn p15-s23-c-backend-diagnostic-stage
  [id]
  (cond
    (str/starts-with? id "C13-") :c13-mir-optimization
    (str/starts-with? id "C14-") :c14-target-lowering
    (str/starts-with? id "B1-") :b1-backend-interface
    (str/starts-with? id "B2-") :b2-c-backend
    (str/starts-with? id "B13-") :b13-artifact-emission
    (str/starts-with? id "B14-") :b14-backend-conformance
    :else :b2-c-backend)))
