

(defn p15-s23-stage3-equivalence-bundle-source-artifact
  [source-path]
  (p15-s23-cached-source-artifact
   :p15-s23-stage3-equivalence-bundle
   source-path
   #(p15-s23-stage3-equivalence-bundle-source-artifact*
     source-path)))

(defn p15-s23-stage3-equivalence-bundle-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage3-equivalence-bundle-fail!
     "P15S23AB001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage3-equivalence-bundle-source-artifact path))

(defn p15-s23-stage3-equivalence-bundle-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage3-equivalence-bundle-file-artifact
               source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :stage3-equivalence-bundle-present?
           (:stage3-equivalence-bundle-present? proof)
           :accepted-output-equivalent?
           (:accepted-output-equivalent? proof)
           :rejected-diagnostics-equivalent?
           (:rejected-diagnostics-equivalent? proof)
           :rebuild-equivalence-complete?
           (:rebuild-equivalence-complete? proof)
           :conformance-evidence-complete?
           (:conformance-evidence-complete? proof)
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired?
           (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(def p15-s23-stage3-self-hosted-application-required-preserves
  #{:accepted-app-output :rejected-app-diagnostic-trace
    :compiler-lineage :runtime-capability-manifest
    :artifact-provenance})

(def p15-s23-stage3-self-hosted-application-required-emits
  #{:stage3-self-hosted-application-run-record
    :stage3-self-hosted-application-rejected-record
    :stage3-self-hosted-application-toolchain-record
    :stage3-self-hosted-application-runtime-record
    :stage3-self-hosted-application-boundary-record})

(def p15-s23-stage3-self-hosted-application-required-links
  #{:stage3-equivalence-bundle
    :stage3-seedless-compiler-candidate
    :stage2-compiler-driver
    :stage2-runtime-kernel
    :accepted-app-execution-proof
    :rejected-app-diagnostic-proof})

(def p15-s23-stage3-self-hosted-application-diagnostic-messages
  {"P15S23AC001" "P15-S23 stage3 self-hosted application execution contract is missing"
   "P15S23AC002" "P15-S23 stage3 equivalence bundle evidence is incomplete"
   "P15S23AC003" "P15-S23 stage3 self-hosted accepted application output does not match"
   "P15S23AC004" "P15-S23 stage3 self-hosted rejected application diagnostics do not match"
   "P15S23AC005" "P15-S23 stage3 self-hosted toolchain boundary is incomplete"
   "P15S23AC006" "P15-S23 stage3 self-hosted runtime capability record is incomplete"
   "P15S23AC007" "P15-S23 stage3 self-hosted application evidence links are incomplete"
   "P15S23AC008" "P15-S23 stage3 self-hosted application execution makes an unsupported final seed-retirement claim"})

(def p15-s23-stage3-self-hosted-application-diagnostic-ids
  ["P15S23AC001" "P15S23AC002" "P15S23AC003" "P15S23AC004"
   "P15S23AC005" "P15S23AC006" "P15S23AC007" "P15S23AC008"])

(defn p15-s23-stage3-self-hosted-application-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage3-self-hosted-application-diagnostic-messages
              id
              "P15-S23 stage3 self-hosted application execution failed")
         (merge {:source-span {:source source-path}
                 :stage
                 :p15-s23-stage3-self-hosted-application-execution
                 :diagnostic-family
                 :p15-s23-stage3-self-hosted-application-execution
                 :value value
                 :remediation "Run the accepted and rejected application fixtures through the stage3 seedless compiler candidate, preserve diagnostics, keep runtime capability evidence linked, and do not claim Clojure seed retirement until the final proof exists."}
                data)))

(defn p15-s23-stage3-self-hosted-application-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage3-self-hosted-application-execution
   :source-span {:source source-path}
   :message
   (get p15-s23-stage3-self-hosted-application-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_p15_s23_stage3_self_hosted_application_execution})

(defn p15-s23-stage3-self-hosted-application-record
  [source-path proof-contract]
  (let [application-stage (:application-stage proof-contract)
        preserves (set (:preserves proof-contract))
        emits (set (:emits proof-contract))
        requires (set (:requires application-stage))
        missing-preserves
        (set/difference
         p15-s23-stage3-self-hosted-application-required-preserves
         preserves)
        missing-emits
        (set/difference
         p15-s23-stage3-self-hosted-application-required-emits
         emits)
        missing-requires
        (set/difference
         p15-s23-stage3-self-hosted-application-required-links
         requires)]
    {:artifact :gravity/p15-s23-stage3-self-hosted-application-execution-record
     :source-path source-path
     :stage (:stage proof-contract)
     :application-stage (:stage application-stage)
     :toolchain (:toolchain proof-contract)
     :verified-by (:verified-by proof-contract)
     :accepted-fixture (:accepted-fixture application-stage)
     :rejected-fixtures (:rejected-fixtures application-stage)
     :missing-preserves (vec (sort missing-preserves))
     :missing-emits (vec (sort missing-emits))
     :missing-requires (vec (sort missing-requires))
     :status
     (if (and (= :gravity/stage3-self-hosted-application-execution
                 (:artifact proof-contract))
              (= :p15-s23-stage3-self-hosted-application-execution
                 (:stage proof-contract))
              (= :p15-s23-stage3-seedless-compiler-candidate
                 (:toolchain proof-contract))
              (= :emit-and-run-stage3-self-hosted-application
                 (:stage application-stage))
              (= :gravity-stage3-verifier (:verified-by proof-contract))
              (empty? missing-preserves)
              (empty? missing-emits)
              (empty? missing-requires))
       :complete
       :failed)}))

(defn p15-s23-stage3-self-hosted-application-link-record
  [equivalence candidate driver runtime accepted rejected]
  (let [links
        [{:link :stage3-equivalence-bundle
          :artifact (:kind equivalence)
          :artifact-id (:artifact-id equivalence)}
         {:link :stage3-seedless-compiler-candidate
          :artifact (:kind candidate)
          :artifact-id (:artifact-id candidate)}
         {:link :stage2-compiler-driver
          :artifact (:kind driver)
          :artifact-id (:artifact-id driver)}
         {:link :stage2-runtime-kernel
          :artifact (:kind runtime)
          :artifact-id (:artifact-id runtime)}
         {:link :accepted-app-execution-proof
          :artifact (:kind accepted)
          :artifact-id (:artifact-id accepted)}
         {:link :rejected-app-diagnostic-proof
          :artifact (:kind rejected)
          :artifact-id (:artifact-id rejected)}]
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
    {:artifact :gravity/p15-s23-stage3-self-hosted-application-link-record
     :links links-with-status
     :required-links
     (vec (sort p15-s23-stage3-self-hosted-application-required-links))
     :required-links-covered?
     (= p15-s23-stage3-self-hosted-application-required-links covered)
     :all-artifacts-identified?
     (every? #(= :verified (:status %)) links-with-status)
     :status
     (if (and (= p15-s23-stage3-self-hosted-application-required-links
                 covered)
              (every? #(= :verified (:status %)) links-with-status))
       :complete
       :failed)}))