

(def p15-s23-stage2-whole-language-compiler-required-emits
  #{:stage2-whole-language-compiler-stage-record
    :stage2-whole-language-accepted-run-record
    :stage2-whole-language-rejected-diagnostic-record
    :stage2-whole-language-evidence-link-record
    :stage2-whole-language-boundary-record
    :stage2-whole-language-lineage-record})

(def p15-s23-stage2-whole-language-compiler-required-links
  #{:whole-language-compiler-artifact
    :compiler-pipeline-manifest
    :stage2-compiler-driver
    :stage2-source-front-end
    :stage2-front-end-executor
    :stage2-plan-emitter
    :stage2-runtime-executor
    :stage2-runtime-kernel
    :accepted-app-execution-proof
    :rejected-app-diagnostic-proof
    :stage-comparison-report
    :self-hosting-conformance-report
    :bootstrap-provenance-attestation
    :trusted-computing-base-delta-record
    :unsafe-audit-report})

(def p15-s23-stage2-whole-language-compiler-diagnostic-messages
  {"P15S23Z001" "P15-S23 stage2 whole-language compiler contract is missing"
   "P15S23Z002" "P15-S23 stage2 whole-language compiler source subset is incomplete"
   "P15S23Z003" "P15-S23 stage2 whole-language compiler is not linked to an executed stage2 driver"
   "P15S23Z004" "P15-S23 stage2 whole-language compiler accepted output does not match the current stage"
   "P15S23Z005" "P15-S23 stage2 whole-language compiler rejected diagnostics are incomplete"
   "P15S23Z006" "P15-S23 stage2 whole-language compiler evidence links, preserves, or emitted records are incomplete"
   "P15S23Z007" "P15-S23 stage2 whole-language compiler trusted boundary record is incomplete"
   "P15S23Z008" "P15-S23 stage2 whole-language compiler makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-stage2-whole-language-compiler-diagnostic-ids
  ["P15S23Z001" "P15S23Z002" "P15S23Z003" "P15S23Z004"
   "P15S23Z005" "P15S23Z006" "P15S23Z007" "P15S23Z008"])

(defn p15-s23-stage2-whole-language-compiler-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage2-whole-language-compiler-diagnostic-messages
              id
              "P15-S23 stage2 whole-language compiler failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage2-whole-language-compiler
                 :diagnostic-family
                 :p15-s23-stage2-whole-language-compiler
                 :value value
                 :remediation "Keep the stage2 whole-language compiler stage authored in Gravity, linked to the stage2 driver, source front-end, runtime kernel, accepted and rejected fixtures, and residual Clojure verifier boundary. Do not claim full self-hosting or Clojure seed retirement until the final evidence bundle exists."}
                data)))

(defn p15-s23-stage2-whole-language-compiler-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage2-whole-language-compiler
   :source-span {:source source-path}
   :message
   (get p15-s23-stage2-whole-language-compiler-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_p15_s23_stage2_whole_language_compiler})

(defn p15-s23-stage2-whole-language-compiler-source-record
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
    {:artifact :gravity/p15-s23-stage2-whole-language-compiler-source-record
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

(defn p15-s23-stage2-whole-language-compiler-stage-record
  [source-path proof-contract source-record]
  (let [stage-contract (:whole-language-stage proof-contract)
        preserves (set (:preserves proof-contract))
        emits (set (:emits proof-contract))
        required-emits
        (set (:emits stage-contract))
        missing-preserves
        (set/difference
         p15-s23-stage2-whole-language-compiler-required-preserves
         preserves)
        missing-emits
        (set/difference
         p15-s23-stage2-whole-language-compiler-required-emits
         emits)
        missing-stage-emits
        (set/difference required-emits emits)]
    {:artifact :gravity/p15-s23-stage2-whole-language-compiler-stage-record
     :source-path source-path
     :stage (:stage proof-contract)
     :whole-language-stage (:stage stage-contract)
     :input (:input proof-contract)
     :output (:output proof-contract)
     :stage2-compiler-driver (:stage2-compiler-driver proof-contract)
     :stage2-runtime-kernel (:stage2-runtime-kernel proof-contract)
     :source-subset-covered? (:source-subset-covered? source-record)
     :missing-preserves (vec (sort missing-preserves))
     :missing-emits (vec (sort (set/union missing-emits
                                          missing-stage-emits)))
     :status
     (if (and (= :gravity/stage2-whole-language-compiler
                 (:artifact proof-contract))
              (= :p15-s23-stage2-whole-language-compiler
                 (:stage proof-contract))
              (= :complete (:status source-record))
              (= :p15-s23-stage2-compiler-driver
                 (:stage2-compiler-driver proof-contract))
              (= :p15-s23-stage2-runtime-kernel
                 (:stage2-runtime-kernel proof-contract))
              (empty? missing-preserves)
              (empty? missing-emits)
              (empty? missing-stage-emits))
       :complete
       :failed)}))

(defn p15-s23-stage2-whole-language-compiler-evidence-link-record
  [whole-compiler-artifact pipeline-artifact driver-artifact source-front-end
   front-end-executor plan-emitter runtime-executor runtime-kernel
   accepted-artifact rejected-artifact stage-comparison-artifact
   conformance-artifact provenance-artifact tcb-artifact unsafe-artifact]
  (let [links
        [{:link :whole-language-compiler-artifact
          :artifact (:kind whole-compiler-artifact)
          :artifact-id (:artifact-id whole-compiler-artifact)}
         {:link :compiler-pipeline-manifest
          :artifact (:kind pipeline-artifact)
          :artifact-id (:artifact-id pipeline-artifact)}
         {:link :stage2-compiler-driver
          :artifact (:kind driver-artifact)
          :artifact-id (:artifact-id driver-artifact)}
         {:link :stage2-source-front-end
          :artifact (:kind source-front-end)
          :artifact-id (:artifact-id source-front-end)}
         {:link :stage2-front-end-executor
          :artifact (:kind front-end-executor)
          :artifact-id (:artifact-id front-end-executor)}
         {:link :stage2-plan-emitter
          :artifact (:kind plan-emitter)
          :artifact-id (:artifact-id plan-emitter)}
         {:link :stage2-runtime-executor
          :artifact (:kind runtime-executor)
          :artifact-id (:artifact-id runtime-executor)}
         {:link :stage2-runtime-kernel
          :artifact (:kind runtime-kernel)
          :artifact-id (:artifact-id runtime-kernel)}
         {:link :accepted-app-execution-proof
          :artifact (:kind accepted-artifact)
          :artifact-id (:artifact-id accepted-artifact)}
         {:link :rejected-app-diagnostic-proof
          :artifact (:kind rejected-artifact)
          :artifact-id (:artifact-id rejected-artifact)}
         {:link :stage-comparison-report
          :artifact (:kind stage-comparison-artifact)
          :artifact-id (:artifact-id stage-comparison-artifact)}
         {:link :self-hosting-conformance-report
          :artifact (:kind conformance-artifact)
          :artifact-id (:artifact-id conformance-artifact)}
         {:link :bootstrap-provenance-attestation
          :artifact (:kind provenance-artifact)
          :artifact-id (:artifact-id provenance-artifact)}
         {:link :trusted-computing-base-delta-record
          :artifact (:kind tcb-artifact)
          :artifact-id (:artifact-id tcb-artifact)}
         {:link :unsafe-audit-report
          :artifact (:kind unsafe-artifact)
          :artifact-id (:artifact-id unsafe-artifact)}]
        links-with-status
        (mapv (fn [link]
                (assoc link
                       :status
                       (if (re-find #"^sha256:"
                                    (str (:artifact-id link)))
                         :verified
                         :missing)))
              links)
        covered (set (map :link links-with-status))]
    {:artifact
     :gravity/p15-s23-stage2-whole-language-compiler-evidence-link-record
     :links links-with-status
     :required-links
     (vec (sort p15-s23-stage2-whole-language-compiler-required-links))
     :required-links-covered?
     (= p15-s23-stage2-whole-language-compiler-required-links covered)
     :all-artifacts-identified?
     (every? #(= :verified (:status %)) links-with-status)
     :status
     (if (and (= p15-s23-stage2-whole-language-compiler-required-links
                 covered)
              (every? #(= :verified (:status %)) links-with-status))
       :complete
       :failed)}))