

(defn p15-s23-stage2-whole-language-compiler-source-artifact
  [source-path]
  (p15-s23-cached-source-artifact
   :p15-s23-stage2-whole-language-compiler
   source-path
   #(p15-s23-stage2-whole-language-compiler-source-artifact*
     source-path)))

(defn p15-s23-stage2-whole-language-compiler-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage2-whole-language-compiler-fail!
     "P15S23Z001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage2-whole-language-compiler-source-artifact path))

(defn p15-s23-stage2-whole-language-compiler-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage2-whole-language-compiler-file-artifact
               source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :stage2-whole-language-compiler-present?
           (:stage2-whole-language-compiler-present? proof)
           :source-subset-covered? (:source-subset-covered? proof)
           :stage2-compiler-driver-executed?
           (:stage2-compiler-driver-executed? proof)
           :stage2-runtime-kernel-used?
           (:stage2-runtime-kernel-used? proof)
           :accepted-output-equivalent?
           (:accepted-output-equivalent? proof)
           :rejected-diagnostics-equivalent?
           (:rejected-diagnostics-equivalent? proof)
           :residual-clojure-verifier-recorded?
           (:residual-clojure-verifier-recorded? proof)
           :residual-clojure-release-compiler-recorded?
           (:residual-clojure-release-compiler-recorded? proof)
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired?
           (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(def p15-s23-stage3-seedless-compiler-candidate-required-preserves
  #{:source-spans :source-unit-identity :syntax-identity :diagnostic-codes
    :artifact-provenance :pipeline-stage-contracts
    :runtime-capability-manifest :accepted-app-output
    :rejected-app-diagnostic-trace :compiler-lineage})

(def p15-s23-stage3-seedless-compiler-candidate-required-emits
  #{:stage3-seedless-compiler-candidate-record
    :stage3-seedless-accepted-run-record
    :stage3-seedless-rejected-diagnostic-record
    :stage3-seedless-boundary-record
    :stage3-seedless-lineage-record})

(def p15-s23-stage3-seedless-compiler-candidate-required-links
  #{:stage2-whole-language-compiler
    :whole-language-compiler-artifact
    :stage2-compiler-driver
    :stage2-source-front-end
    :stage2-front-end-executor
    :stage2-plan-emitter
    :stage2-runtime-executor
    :stage2-runtime-kernel
    :accepted-app-execution-proof
    :rejected-app-diagnostic-proof})

(def p15-s23-stage3-seedless-compiler-candidate-diagnostic-messages
  {"P15S23AA001" "P15-S23 stage3 seedless compiler candidate contract is missing"
   "P15S23AA002" "P15-S23 stage3 seedless compiler source subset is incomplete"
   "P15S23AA003" "P15-S23 stage3 seedless compiler path is incomplete"
   "P15S23AA004" "P15-S23 stage3 seedless compiler accepted output does not match"
   "P15S23AA005" "P15-S23 stage3 seedless compiler rejected diagnostics are incomplete"
   "P15S23AA006" "P15-S23 stage3 seedless compiler still records a Clojure seed boundary"
   "P15S23AA007" "P15-S23 stage3 seedless compiler evidence links are incomplete"
   "P15S23AA008" "P15-S23 stage3 seedless compiler makes an unsupported final release claim"})

(def p15-s23-stage3-seedless-compiler-candidate-diagnostic-ids
  ["P15S23AA001" "P15S23AA002" "P15S23AA003" "P15S23AA004"
   "P15S23AA005" "P15S23AA006" "P15S23AA007" "P15S23AA008"])

(defn p15-s23-stage3-seedless-compiler-candidate-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage3-seedless-compiler-candidate-diagnostic-messages
              id
              "P15-S23 stage3 seedless compiler candidate failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage3-seedless-compiler-candidate
                 :diagnostic-family
                 :p15-s23-stage3-seedless-compiler-candidate
                 :value value
                 :remediation "Keep the candidate authored in Gravity, compile through the stage2 compiler driver and runtime kernel, replace Clojure verifier and release-compiler boundaries in the candidate record, and keep final self-hosting claims false until the full equivalence bundle is present."}
                data)))

(defn p15-s23-stage3-seedless-compiler-candidate-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage3-seedless-compiler-candidate
   :source-span {:source source-path}
   :message
   (get p15-s23-stage3-seedless-compiler-candidate-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_p15_s23_stage3_seedless_compiler_candidate})

(defn p15-s23-stage3-seedless-compiler-candidate-source-record
  [source-path proof-contract inventory-artifact]
  (let [modules (:source-inventory inventory-artifact)
        required-components
        (set (get-in proof-contract
                     [:claimed-language-subset
                      :compiler-source-components]))
        observed-components (set (map :component modules))
        missing-components
        (set/difference required-components observed-components)
        missing-files
        (->> modules
             (remove #(= :present (:status %)))
             (map :path)
             sort
             vec)]
    {:artifact
     :gravity/p15-s23-stage3-seedless-compiler-candidate-source-record
     :source-path source-path
     :required-components (vec (sort required-components))
     :observed-components (vec (sort observed-components))
     :source-modules modules
     :missing-components (vec (sort missing-components))
     :missing-files missing-files
     :source-subset-covered?
     (and (empty? missing-components) (empty? missing-files))
     :status
     (if (and (= :gravity/p15-s23-compiler-source-inventory-artifact
                 (:kind inventory-artifact))
              (empty? missing-components)
              (empty? missing-files))
       :complete
       :failed)}))

(defn p15-s23-stage3-seedless-compiler-candidate-record
  [source-path proof-contract source-record]
  (let [candidate-stage (:candidate-stage proof-contract)
        preserves (set (:preserves proof-contract))
        emits (set (:emits proof-contract))
        requires (set (:requires candidate-stage))
        missing-preserves
        (set/difference
         p15-s23-stage3-seedless-compiler-candidate-required-preserves
         preserves)
        missing-emits
        (set/difference
         p15-s23-stage3-seedless-compiler-candidate-required-emits
         emits)
        missing-requires
        (set/difference
         p15-s23-stage3-seedless-compiler-candidate-required-links
         requires)]
    {:artifact
     :gravity/p15-s23-stage3-seedless-compiler-candidate-record
     :source-path source-path
     :stage (:stage proof-contract)
     :candidate-stage (:stage candidate-stage)
     :implemented-by (:implemented-by proof-contract)
     :compiled-by (:compiled-by proof-contract)
     :verified-by (:verified-by proof-contract)
     :release-compiler (:release-compiler proof-contract)
     :executed-by (:executed-by proof-contract)
     :source-subset-covered? (:source-subset-covered? source-record)
     :missing-preserves (vec (sort missing-preserves))
     :missing-emits (vec (sort missing-emits))
     :missing-requires (vec (sort missing-requires))
     :compiler-path-seedless?
     (and (= :gravity-stage2-compiler-driver
             (:compiled-by proof-contract))
          (= :gravity-stage3-verifier (:verified-by proof-contract))
          (= :gravity-stage3-release-compiler
             (:release-compiler proof-contract))
          (= :gravity-stage2-runtime-kernel
             (:executed-by proof-contract)))
     :status
     (if (and (= :gravity/stage3-seedless-compiler-candidate
                 (:artifact proof-contract))
              (= :p15-s23-stage3-seedless-compiler-candidate
                 (:stage proof-contract))
              (= :complete (:status source-record))
              (= :compile-current-implementation-subset-without-clojure-seed
                 (:stage candidate-stage))
              (empty? missing-preserves)
              (empty? missing-emits)
              (empty? missing-requires)
              (= :gravity-stage2-compiler-driver
                 (:compiled-by proof-contract))
              (= :gravity-stage3-verifier (:verified-by proof-contract))
              (= :gravity-stage3-release-compiler
                 (:release-compiler proof-contract))
              (= :gravity-stage2-runtime-kernel
                 (:executed-by proof-contract)))
       :complete
       :failed)}))