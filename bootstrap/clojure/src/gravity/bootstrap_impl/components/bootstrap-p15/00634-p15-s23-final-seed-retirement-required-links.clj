

(def p15-s23-final-seed-retirement-required-links
  #{:compiler-pipeline-manifest
    :source-unit-and-syntax-serialization-proof
    :core-lowering-and-diagnostic-preservation-report
    :runtime-manifest-and-capability-enforcement-report
    :accepted-app-execution-proof
    :rejected-app-diagnostic-proof
    :reproducible-rebuild-log
    :stage-comparison-report
    :self-hosting-conformance-report
    :bootstrap-provenance-attestation
    :trusted-computing-base-delta-record
    :unsafe-audit-report
    :whole-language-compiler-artifact
    :governance-and-package-release-record
    :stage3-seedless-compiler-candidate
    :stage3-equivalence-bundle
    :stage3-self-hosted-application-execution})

(def p15-s23-final-seed-retirement-diagnostic-messages
  {"P15S23AD001" "P15-S23 final seed-retirement proof contract is missing"
   "P15S23AD002" "P15-S23 final seed-retirement evidence links are incomplete"
   "P15S23AD003" "P15-S23 final seed-retirement seedless compiler boundary is incomplete"
   "P15S23AD004" "P15-S23 final seed-retirement stage3 equivalence or application execution is incomplete"
   "P15S23AD005" "P15-S23 final seed-retirement release governance closure is incomplete"
   "P15S23AD006" "P15-S23 final seed-retirement TCB retirement closure is incomplete"
   "P15S23AD007" "P15-S23 final seed-retirement provenance closure is incomplete"
   "P15S23AD008" "P15-S23 final seed-retirement claim is unsupported"})

(def p15-s23-final-seed-retirement-diagnostic-ids
  ["P15S23AD001" "P15S23AD002" "P15S23AD003" "P15S23AD004"
   "P15S23AD005" "P15S23AD006" "P15S23AD007" "P15S23AD008"])

(defn p15-s23-final-seed-retirement-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-final-seed-retirement-diagnostic-messages
              id
              "P15-S23 final seed-retirement proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-final-seed-retirement-proof
                 :diagnostic-family :p15-s23-final-seed-retirement
                 :value value
                 :remediation "Only emit final self-hosting and seed-retirement claims when every required evidence link is present, the stage3 candidate and application path are seedless, release governance has no remaining seed blocker, TCB closure retires the Clojure seed boundary, and provenance closure records the seedless release boundary."}
                data)))

(defn p15-s23-final-seed-retirement-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-final-seed-retirement-proof
   :source-span {:source source-path}
   :message
   (get p15-s23-final-seed-retirement-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_p15_s23_final_seed_retirement_proof})

	(defn p15-s23-final-seed-retirement-base-evidence
	  []
	  {})

(defn p15-s23-final-seed-retirement-link-record
  [proof-contract evidence]
  (let [required (set (:required-evidence-links proof-contract))
        missing (set/difference required (set (keys evidence)))
        incomplete
        (set (for [[k v] evidence
                   :when (and (contains? required k)
                              (not (p15-s23-evidence-present? v)))]
               k))
        missing-links (set/union missing incomplete)
        links
        (mapv (fn [k]
                (let [v (get evidence k)]
                  {:link k
                   :artifact (:artifact v)
                   :artifact-id (:artifact-id v)
                   :proof-id (:proof-id v)
                   :status (:status v)
                   :present? (p15-s23-evidence-present? v)}))
              (sort required))]
    {:artifact :gravity/p15-s23-final-seed-retirement-link-record
     :required-links (vec (sort required))
     :links links
     :missing-links (vec (sort missing-links))
     :all-required-links-present? (empty? missing-links)
     :status (if (empty? missing-links) :complete :failed)}))

(defn p15-s23-final-seedless-boundary-record
  [proof-contract evidence]
  (let [boundary (:final-boundary proof-contract)
        seedless (:stage3-seedless-compiler-candidate evidence)
        equivalence (:stage3-equivalence-bundle evidence)
        app (:stage3-self-hosted-application-execution evidence)
        complete?
        (and (false? (:compiler-path-uses-clojure-seed? boundary))
             (false? (:runtime-path-uses-clojure-seed? boundary))
             (false? (:release-compiler-uses-clojure-seed? boundary))
             (false? (:verification-host-in-release-boundary? boundary))
             (true? (:compiler-path-seedless? seedless))
             (true? (:clojure-stage0-verifier-absent? seedless))
             (true? (:clojure-stage0-release-compiler-absent?
                     seedless))
             (true? (:stage3-toolchain-seedless? app))
             (true? (:stage3-equivalence-bundle-present?
                     equivalence)))]
    {:artifact :gravity/p15-s23-final-seedless-boundary-record
     :compiler-path-uses-clojure-seed?
     (:compiler-path-uses-clojure-seed? boundary)
     :runtime-path-uses-clojure-seed?
     (:runtime-path-uses-clojure-seed? boundary)
     :release-compiler-uses-clojure-seed?
     (:release-compiler-uses-clojure-seed? boundary)
     :verification-host (:verification-host boundary)
     :verification-host-in-release-boundary?
     (:verification-host-in-release-boundary? boundary)
     :stage3-compiler-path-seedless?
     (true? (:compiler-path-seedless? seedless))
     :stage3-verifier-absent-from-clojure?
     (true? (:clojure-stage0-verifier-absent? seedless))
     :stage3-release-compiler-absent-from-clojure?
     (true? (:clojure-stage0-release-compiler-absent? seedless))
     :stage3-toolchain-seedless?
     (true? (:stage3-toolchain-seedless? app))
     :stage3-equivalence-bundle-present?
     (true? (:stage3-equivalence-bundle-present? equivalence))
     :clojure-seed-boundary? (not complete?)
     :status (if complete? :complete :failed)}))

(defn p15-s23-final-stage3-execution-record
  [evidence]
  (let [equivalence (:stage3-equivalence-bundle evidence)
        app (:stage3-self-hosted-application-execution evidence)
        complete?
        (and (true? (:stage3-equivalence-bundle-present? equivalence))
             (true? (:accepted-output-equivalent? equivalence))
             (true? (:rejected-diagnostics-equivalent? equivalence))
             (true? (:rebuild-equivalence-complete? equivalence))
             (true? (:conformance-evidence-complete? equivalence))
             (true? (:stage3-self-hosted-application-execution-present?
                     app))
             (true? (:accepted-application-run? app))
             (true? (:rejected-application-fails-closed? app))
             (true? (:runtime-capability-recorded? app)))]
    {:artifact :gravity/p15-s23-final-stage3-execution-record
     :stage3-equivalence-bundle-present?
     (true? (:stage3-equivalence-bundle-present? equivalence))
     :accepted-output-equivalent?
     (true? (:accepted-output-equivalent? equivalence))
     :rejected-diagnostics-equivalent?
     (true? (:rejected-diagnostics-equivalent? equivalence))
     :rebuild-equivalence-complete?
     (true? (:rebuild-equivalence-complete? equivalence))
     :conformance-evidence-complete?
     (true? (:conformance-evidence-complete? equivalence))
     :stage3-self-hosted-application-execution-present?
     (true? (:stage3-self-hosted-application-execution-present? app))
     :accepted-application-run?
     (true? (:accepted-application-run? app))
     :rejected-application-fails-closed?
     (true? (:rejected-application-fails-closed? app))
     :runtime-capability-recorded?
     (true? (:runtime-capability-recorded? app))
     :status (if complete? :complete :failed)}))

(defn p15-s23-final-release-decision-record
  [proof-contract evidence boundary-record stage3-record]
  (let [governance (:governance-and-package-release-record evidence)
        previous-blockers (set (:release-blockers governance))
        seed-blocker-retired?
        (and (= #{:clojure-seed-retired} previous-blockers)
             (false? (:clojure-seed-boundary? boundary-record)))
        complete?
        (and (= :complete (:status boundary-record))
             (= :complete (:status stage3-record))
             (p15-s23-evidence-present? governance)
             seed-blocker-retired?
             (true? (get-in proof-contract
                            [:self-hosting-claims
                             :full-language-compiler-self-hosted?]))
             (true? (get-in proof-contract
                            [:self-hosting-claims
                             :clojure-seed-retired?])))
        remaining-blockers
        (if complete? [] (vec (sort previous-blockers)))]
    {:artifact :gravity/p15-s23-final-release-decision-record
     :previous-registry-decision (:registry-decision governance)
     :previous-release-blockers (vec (sort previous-blockers))
     :retired-release-blockers
     (if seed-blocker-retired? [:clojure-seed-retired] [])
     :remaining-release-blockers remaining-blockers
     :seed-blocker-retired? seed-blocker-retired?
     :release-eligible? complete?
     :registry-publication-eligible? complete?
     :governance-and-package-policy-satisfied? complete?
     :status (if complete? :complete :failed)}))

(defn p15-s23-final-tcb-retirement-record
  [evidence boundary-record release-decision]
  (let [tcb (:trusted-computing-base-delta-record evidence)
        complete?
        (and (p15-s23-evidence-present? tcb)
             (= :complete (:status boundary-record))
             (= :complete (:status release-decision)))]
    {:artifact :gravity/p15-s23-final-tcb-retirement-record
     :prior-tcb-delta-record-id (:tcb-delta-record-id tcb)
     :prior-clojure-seed-still-trusted?
     (:clojure-seed-still-trusted? tcb)
     :prior-residual-trusted-count
     (:current-residual-trusted-count tcb)
     :compiler-seed-residual-count (if complete? 0 1)
     :clojure-seed-still-trusted? (not complete?)
     :clojure-seed-retired-from-release-boundary? complete?
     :whole-language-tcb-reduced? complete?
     :status (if complete? :complete :failed)}))