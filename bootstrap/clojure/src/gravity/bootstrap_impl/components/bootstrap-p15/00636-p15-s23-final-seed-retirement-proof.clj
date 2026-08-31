

	(defn p15-s23-final-seed-retirement-proof
	  [artifact]
	  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-final-seed-retirement-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat
              #(map :diagnostic (:diagnostics %))
              (:rejected-p15-s23-final-seed-retirement-fixtures
               artifact)))
        boundary (:boundary-record artifact)
        stage3 (:stage3-execution-record artifact)
	        release (:release-decision-record artifact)
	        tcb (:tcb-retirement-record artifact)
	        provenance (:provenance-closure-record artifact)
	        gate (:gate-completion-record artifact)
	        complete? (= :complete (:status gate))]
	    {:final-seed-retirement-proof-authored-in-gravity? true
	     :status (if complete? :complete :incomplete)
	     :task "P15-S23"
	     :final-seed-retirement-proof-present?
	     complete?
     :evidence-links-covered?
     (= :complete (get-in artifact [:evidence-link-record :status]))
     :stage3-seedless-boundary-proven?
     (= :complete (:status boundary))
     :stage3-equivalence-and-application-proven?
     (= :complete (:status stage3))
     :release-governance-closed?
     (= :complete (:status release))
     :tcb-seed-boundary-retired?
     (= :complete (:status tcb))
     :provenance-closure-recorded?
     (= :complete (:status provenance))
     :full-language-compiler-self-hosted?
     (true? (:full-language-compiler-self-hosted? gate))
     :clojure-seed-retired?
     (true? (:clojure-seed-retired? gate))
     :clojure-seed-boundary?
     (:clojure-seed-boundary? gate)
     :verification-host
     (:verification-host provenance)
     :verification-host-in-release-boundary?
     (:verification-host-in-release-boundary? provenance)
     :rejected-candidates-covered?
     (= (set p15-s23-final-seed-retirement-diagnostic-ids)
        rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-final-seed-retirement-diagnostic-ids)
        diagnostics)
	     :limitations
	     {:full-language-compiler-self-hosted? complete?
	      :clojure-seed-retired? complete?
	      :clojure-seed-boundary? (not complete?)
	      :verification-host :clojure-bootstrap-verifier
	      :verification-host-in-release-boundary?
	      (:verification-host-in-release-boundary? provenance)
	      :next-required-capability
	      (or (get-in artifact [:proof-contract :next-required-capability])
	          :self_hosted_public_binary_final_verification)}}))

(defn p15-s23-final-seed-retirement-source-artifact*
  ([source-path]
   (p15-s23-final-seed-retirement-source-artifact*
    source-path
    (p15-s23-final-seed-retirement-base-evidence)))
  ([source-path evidence]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-final-seed-retirement-proof)
        link-record
        (p15-s23-final-seed-retirement-link-record
         proof-contract evidence)
        boundary-record
        (p15-s23-final-seedless-boundary-record
         proof-contract evidence)
        stage3-execution-record
        (p15-s23-final-stage3-execution-record evidence)
        release-decision
        (p15-s23-final-release-decision-record
         proof-contract evidence boundary-record stage3-execution-record)
        tcb-retirement
        (p15-s23-final-tcb-retirement-record
         evidence boundary-record release-decision)
        provenance-closure
        (p15-s23-final-provenance-closure-record
         proof-contract evidence boundary-record stage3-execution-record
         release-decision)
        gate-completion
        (p15-s23-final-gate-completion-record
         proof-contract link-record boundary-record stage3-execution-record
         release-decision tcb-retirement provenance-closure)
        candidate {:proof-contract proof-contract
                   :evidence-link-record link-record
                   :boundary-record boundary-record
                   :stage3-execution-record stage3-execution-record
                   :release-decision-record release-decision
                   :tcb-retirement-record tcb-retirement
                   :provenance-closure-record provenance-closure
                   :gate-completion-record gate-completion}
        diagnostics
        (p15-s23-final-seed-retirement-proof-diagnostics
         source-path candidate)
        complete? (empty? diagnostics)
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :evidence-link-record link-record
                       :boundary-record boundary-record
                       :stage3-execution-record stage3-execution-record
                       :release-decision-record release-decision
                       :tcb-retirement-record tcb-retirement
                       :provenance-closure-record provenance-closure
                       :gate-completion-record gate-completion})))
        rejected-records
        (p15-s23-final-seed-retirement-rejected-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-final-seed-retirement-proof-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-final-seed-retirement-proof
	         :status (if complete? :complete :incomplete)
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :linked-artifacts
         {:stage3-seedless-compiler-candidate
          (select-keys
           (:stage3-seedless-compiler-candidate evidence)
           [:artifact :artifact-id :proof-id])
          :stage3-equivalence-bundle
          (select-keys
           (:stage3-equivalence-bundle evidence)
           [:artifact :artifact-id :proof-id])
          :stage3-self-hosted-application-execution
          (select-keys
           (:stage3-self-hosted-application-execution evidence)
           [:artifact :artifact-id :proof-id])}
         :evidence-link-record link-record
         :boundary-record boundary-record
         :stage3-execution-record stage3-execution-record
         :release-decision-record release-decision
         :tcb-retirement-record tcb-retirement
         :provenance-closure-record provenance-closure
         :gate-completion-record gate-completion
	         :full-language-compiler-self-hosted?
	         (true? (:full-language-compiler-self-hosted? gate-completion))
	         :clojure-seed-retired?
	         (true? (:clojure-seed-retired? gate-completion))
	         :clojure-seed-boundary?
	         (:clojure-seed-boundary? gate-completion)
         :rejected-p15-s23-final-seed-retirement-fixtures
         rejected-records
         :p15-s23-final-seed-retirement-diagnostic-stream
         (p15-s23-final-seed-retirement-diagnostic-stream
          source-path proof-id)
         :p15-s23-final-seed-retirement-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-final-seed-retirement-diagnostic-ids)
	          :full-language-compiler-self-hosted?
	          (true? (:full-language-compiler-self-hosted? gate-completion))
	          :clojure-seed-retired?
	          (true? (:clojure-seed-retired? gate-completion))
	          :clojure-seed-boundary?
	          (:clojure-seed-boundary? gate-completion)
	          :status (if complete? :complete :incomplete)}
	         :diagnostics diagnostics}
        proof (p15-s23-final-seed-retirement-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof))))))

(defn p15-s23-final-seed-retirement-source-artifact
  [source-path]
  (p15-s23-cached-source-artifact
   :p15-s23-final-seed-retirement-proof
   source-path
   #(p15-s23-final-seed-retirement-source-artifact*
     source-path)))

(defn p15-s23-final-seed-retirement-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-final-seed-retirement-fail!
     "P15S23AD001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-final-seed-retirement-source-artifact path))

	(defn p15-s23-final-seed-retirement-evidence-summary
	  [artifact]
	  (let [proof (:capability-based-proof artifact)
	        complete? (= :complete (:status artifact))]
	    {:status (if complete? :verified :incomplete)
	     :artifact (:kind artifact)
     :artifact-id (:artifact-id artifact)
     :proof-id (:proof-id artifact)
     :source-path (:source-path artifact)
     :final-seed-retirement-proof-present?
     (:final-seed-retirement-proof-present? proof)
     :evidence-links-covered? (:evidence-links-covered? proof)
     :stage3-seedless-boundary-proven?
     (:stage3-seedless-boundary-proven? proof)
     :stage3-equivalence-and-application-proven?
     (:stage3-equivalence-and-application-proven? proof)
     :release-governance-closed? (:release-governance-closed? proof)
     :tcb-seed-boundary-retired?
     (:tcb-seed-boundary-retired? proof)
     :provenance-closure-recorded?
     (:provenance-closure-recorded? proof)
     :full-language-compiler-self-hosted?
     (:full-language-compiler-self-hosted? artifact)
     :clojure-seed-retired? (:clojure-seed-retired? artifact)
     :clojure-seed-boundary? (:clojure-seed-boundary? artifact)}))

(defn p15-s23-final-seed-retirement-evidence-from-evidence
  [evidence]
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (p15-s23-final-seed-retirement-evidence-summary
         (p15-s23-final-seed-retirement-source-artifact*
          source-path evidence))
        (catch Exception _
          nil)))))