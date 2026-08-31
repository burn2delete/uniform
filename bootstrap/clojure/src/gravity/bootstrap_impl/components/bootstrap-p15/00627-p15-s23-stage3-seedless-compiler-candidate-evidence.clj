

(defn p15-s23-stage3-seedless-compiler-candidate-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage3-seedless-compiler-candidate-file-artifact
               source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :seedless-compiler-candidate-present?
           (:seedless-compiler-candidate-present? proof)
           :compiler-path-seedless? (:compiler-path-seedless? proof)
           :accepted-output-equivalent?
           (:accepted-output-equivalent? proof)
           :rejected-diagnostics-equivalent?
           (:rejected-diagnostics-equivalent? proof)
           :clojure-stage0-verifier-absent?
           (:clojure-stage0-verifier-absent? proof)
           :clojure-stage0-release-compiler-absent?
           (:clojure-stage0-release-compiler-absent? proof)
           :final-equivalence-bundle-complete? false
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired?
           (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(def p15-s23-stage3-equivalence-bundle-required-preserves
  #{:accepted-app-output :rejected-app-diagnostic-trace
    :rebuild-artifact-identity :stage-equivalence-record
    :conformance-suite-status :compiler-lineage
    :tcb-component-inventory :unsafe-island-index})

(def p15-s23-stage3-equivalence-bundle-required-emits
  #{:stage3-equivalence-bundle-record
    :stage3-equivalence-accepted-record
    :stage3-equivalence-rejected-record
    :stage3-equivalence-rebuild-record
    :stage3-equivalence-conformance-record
    :stage3-equivalence-boundary-record})

(def p15-s23-stage3-equivalence-bundle-required-links
  #{:stage3-seedless-compiler-candidate
    :accepted-app-execution-proof
    :rejected-app-diagnostic-proof
    :reproducible-rebuild-log
    :stage-comparison-report
    :self-hosting-conformance-report
    :bootstrap-provenance-attestation
    :trusted-computing-base-delta-record
    :unsafe-audit-report})

(def p15-s23-stage3-equivalence-bundle-diagnostic-messages
  {"P15S23AB001" "P15-S23 stage3 equivalence bundle contract is missing"
   "P15S23AB002" "P15-S23 stage3 equivalence candidate evidence is incomplete"
   "P15S23AB003" "P15-S23 stage3 equivalence accepted output does not match"
   "P15S23AB004" "P15-S23 stage3 equivalence rejected diagnostics do not match"
   "P15S23AB005" "P15-S23 stage3 equivalence reproducible rebuild evidence is incomplete"
   "P15S23AB006" "P15-S23 stage3 equivalence conformance, provenance, TCB, or unsafe-audit evidence is incomplete"
   "P15S23AB007" "P15-S23 stage3 equivalence evidence links are incomplete"
   "P15S23AB008" "P15-S23 stage3 equivalence bundle makes an unsupported final release claim"})

(def p15-s23-stage3-equivalence-bundle-diagnostic-ids
  ["P15S23AB001" "P15S23AB002" "P15S23AB003" "P15S23AB004"
   "P15S23AB005" "P15S23AB006" "P15S23AB007" "P15S23AB008"])

(defn p15-s23-stage3-equivalence-bundle-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage3-equivalence-bundle-diagnostic-messages
              id
              "P15-S23 stage3 equivalence bundle failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage3-equivalence-bundle
                 :diagnostic-family :p15-s23-stage3-equivalence-bundle
                 :value value
                 :remediation "Keep the equivalence bundle linked to the stage3 candidate, accepted and rejected fixtures, rebuild, stage comparison, conformance, provenance, TCB, and unsafe-audit evidence. Do not claim final self-hosting or seed retirement until the release bundle exists."}
                data)))

(defn p15-s23-stage3-equivalence-bundle-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage3-equivalence-bundle
   :source-span {:source source-path}
   :message
   (get p15-s23-stage3-equivalence-bundle-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_p15_s23_stage3_equivalence_bundle})

(defn p15-s23-stage3-equivalence-bundle-record
  [source-path proof-contract]
  (let [stage (:equivalence-stage proof-contract)
        preserves (set (:preserves proof-contract))
        emits (set (:emits proof-contract))
        requires (set (:requires stage))
        missing-preserves
        (set/difference p15-s23-stage3-equivalence-bundle-required-preserves
                        preserves)
        missing-emits
        (set/difference p15-s23-stage3-equivalence-bundle-required-emits
                        emits)
        missing-requires
        (set/difference p15-s23-stage3-equivalence-bundle-required-links
                        requires)]
    {:artifact :gravity/p15-s23-stage3-equivalence-bundle-record
     :source-path source-path
     :stage (:stage proof-contract)
     :equivalence-stage (:stage stage)
     :candidate (:candidate proof-contract)
     :verified-by (:verified-by proof-contract)
     :missing-preserves (vec (sort missing-preserves))
     :missing-emits (vec (sort missing-emits))
     :missing-requires (vec (sort missing-requires))
     :status
     (if (and (= :gravity/stage3-equivalence-bundle
                 (:artifact proof-contract))
              (= :p15-s23-stage3-equivalence-bundle
                 (:stage proof-contract))
              (= :p15-s23-stage3-seedless-compiler-candidate
                 (:candidate proof-contract))
              (= :prove-stage3-candidate-equivalent-to-current-stage
                 (:stage stage))
              (= :gravity-stage3-verifier (:verified-by proof-contract))
              (empty? missing-preserves)
              (empty? missing-emits)
              (empty? missing-requires))
       :complete
       :failed)}))

(defn p15-s23-stage3-equivalence-bundle-link-record
  [candidate accepted rejected rebuild comparison conformance provenance tcb
   unsafe]
  (let [links
        [{:link :stage3-seedless-compiler-candidate
          :artifact (:kind candidate)
          :artifact-id (:artifact-id candidate)}
         {:link :accepted-app-execution-proof
          :artifact (:kind accepted)
          :artifact-id (:artifact-id accepted)}
         {:link :rejected-app-diagnostic-proof
          :artifact (:kind rejected)
          :artifact-id (:artifact-id rejected)}
         {:link :reproducible-rebuild-log
          :artifact (:kind rebuild)
          :artifact-id (:artifact-id rebuild)}
         {:link :stage-comparison-report
          :artifact (:kind comparison)
          :artifact-id (:artifact-id comparison)}
         {:link :self-hosting-conformance-report
          :artifact (:kind conformance)
          :artifact-id (:artifact-id conformance)}
         {:link :bootstrap-provenance-attestation
          :artifact (:kind provenance)
          :artifact-id (:artifact-id provenance)}
         {:link :trusted-computing-base-delta-record
          :artifact (:kind tcb)
          :artifact-id (:artifact-id tcb)}
         {:link :unsafe-audit-report
          :artifact (:kind unsafe)
          :artifact-id (:artifact-id unsafe)}]
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
    {:artifact :gravity/p15-s23-stage3-equivalence-bundle-link-record
     :links links-with-status
     :required-links
     (vec (sort p15-s23-stage3-equivalence-bundle-required-links))
     :required-links-covered?
     (= p15-s23-stage3-equivalence-bundle-required-links covered)
     :all-artifacts-identified?
     (every? #(= :verified (:status %)) links-with-status)
     :status
     (if (and (= p15-s23-stage3-equivalence-bundle-required-links
                 covered)
              (every? #(= :verified (:status %)) links-with-status))
       :complete
       :failed)}))

(defn p15-s23-stage3-equivalence-accepted-record
  [candidate accepted]
  (let [candidate-output
        (get-in candidate [:accepted-record :seedless-candidate-output])
        accepted-output
        (get-in accepted [:accepted-output-comparison :accepted-stdout])]
    {:artifact :gravity/p15-s23-stage3-equivalence-accepted-record
     :fixture p15-s23-accepted-app-source-path
     :candidate-output candidate-output
     :accepted-proof-output accepted-output
     :expected-output p15-s23-accepted-app-expected-stdout
     :accepted-output-equivalent?
     (= candidate-output accepted-output
        p15-s23-accepted-app-expected-stdout)
     :status
     (if (= candidate-output accepted-output
            p15-s23-accepted-app-expected-stdout)
       :complete
       :failed)}))

(defn p15-s23-stage3-equivalence-rejected-record
  [candidate rejected]
  (let [candidate-diagnostics
        (set (get-in candidate
                     [:rejected-record
                      :seedless-candidate-diagnostics]))
        rejected-diagnostics
        (set (map :diagnostic
                  (:rejected-app-diagnostic-records rejected)))
        expected #{"L2-FUNCTION-ARITY" "L2-BUILTIN-ARITY"}]
    {:artifact :gravity/p15-s23-stage3-equivalence-rejected-record
     :candidate-diagnostics (vec (sort candidate-diagnostics))
     :rejected-proof-diagnostics (vec (sort rejected-diagnostics))
     :expected-diagnostics (vec (sort expected))
     :rejected-diagnostics-equivalent?
     (= candidate-diagnostics rejected-diagnostics expected)
     :status
     (if (= candidate-diagnostics rejected-diagnostics expected)
       :complete
       :failed)}))