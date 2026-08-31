

(defn validate-stage0-compiled-ai-provider!
  [module provider]
  (when (or (nil? (:capability provider))
            (= :ambient (:capability provider)))
    (compiled-ai-fail!
     "A2001" module provider
     {:missing-fields [:provider-capability]})))

(defn validate-stage0-compiled-ai-prompt!
  [module prompt]
  (when-not (= :partitioned (:authority-partition prompt))
    (compiled-ai-fail!
     "A3003" module prompt
     {:missing-fields [:prompt-authority-partition]})))

(defn validate-stage0-compiled-ai-tool!
  [module tool]
  (when-not (= :required-for-high-priority (:human-review tool))
    (compiled-ai-fail!
     "A4005" module tool
     {:missing-fields [:write-tool-human-review]})))

(defn validate-stage0-compiled-ai-agent!
  [module agent]
  (when-not (seq (:eval-gates agent))
    (compiled-ai-fail!
     "A5005" module agent
     {:missing-fields [:agent-eval-gate]})))

(defn validate-stage0-compiled-ai-workflow!
  [module workflow]
  (when-not (= :recorded-effects (:replay-mode workflow))
    (compiled-ai-fail!
     "A6001" module workflow
     {:missing-fields [:workflow-replay-mode]})))

(defn validate-stage0-compiled-ai-memory!
  [module memory]
  (when-not (= :deny-by-default (:cross-tenant memory))
    (compiled-ai-fail!
     "A7004" module memory
     {:missing-fields [:tenant-partition]})))

(defn validate-stage0-compiled-ai-policy!
  [module policy]
  (when-not (= :untrusted-until-schema-validated
               (get-in policy [:taint :ai-output]))
    (compiled-ai-fail!
     "A8004" module policy
     {:missing-fields [:taint-validation-required]})))

(defn validate-stage0-compiled-ai-evaluation!
  [module evaluation]
  (when-not (= :passed (:release-gate evaluation))
    (compiled-ai-fail!
     "A9001" module evaluation
     {:missing-fields [:eval-gate]})))

(defn validate-stage0-compiled-ai-human-review!
  [module human-review]
  (when-not (= :canonical-action-payload (:payload-hash-rule human-review))
    (compiled-ai-fail!
     "A10005" module human-review
     {:missing-fields [:payload-hash-match]})))

(defn validate-stage0-compiled-ai-injection-defense!
  [module defense]
  (when-not (contains? (set (:runtime-monitors defense))
                       :denied-tool-escalation)
    (compiled-ai-fail!
     "A11002" module defense
     {:missing-fields [:denied-tool-escalation]})))

(defn validate-stage0-compiled-ai!
  [module]
  (when (stage0-compiled-ai-suite-present? module)
    (let [suite (stage0-compiled-ai-suite module)]
      (doseq [program (:programs suite)]
        (validate-stage0-compiled-ai-program! module program))
      (doseq [provider (:providers suite)]
        (validate-stage0-compiled-ai-provider! module provider))
      (doseq [prompt (:prompts suite)]
        (validate-stage0-compiled-ai-prompt! module prompt))
      (doseq [tool (:tools suite)]
        (validate-stage0-compiled-ai-tool! module tool))
      (doseq [agent (:agents suite)]
        (validate-stage0-compiled-ai-agent! module agent))
      (doseq [workflow (:workflows suite)]
        (validate-stage0-compiled-ai-workflow! module workflow))
      (doseq [memory (:memories suite)]
        (validate-stage0-compiled-ai-memory! module memory))
      (doseq [policy (:policies suite)]
        (validate-stage0-compiled-ai-policy! module policy))
      (doseq [evaluation (:evaluations suite)]
        (validate-stage0-compiled-ai-evaluation! module evaluation))
      (doseq [human-review (:human-reviews suite)]
        (validate-stage0-compiled-ai-human-review! module human-review))
      (doseq [defense (:injection-defenses suite)]
        (validate-stage0-compiled-ai-injection-defense! module defense)))))

(defn stage0-compiled-package-suite
  [module]
  (get-in module [:metadata :package :compiled-gate] {}))

(defn stage0-compiled-package-suite-present?
  [module]
  (contains? (get-in module [:metadata :package] {}) :compiled-gate))

(defn compiled-package-fail!
  [id module subject extra]
  (p12-package-fail!
   id
   (:source-path module)
   subject
   (merge {:stage :stage0-compiled-package-gate
           :diagnostic-family :phase12-compiled-package-artifacts
           :compiled-gate :package-build-artifact
           :remediation
           "Compiled package/build/artifact metadata must preserve project manifests, lockfiles, build effects, artifact evidence, verified package operations, dependency capability policy, reproducible build inputs, safety audit records, private registry grants, provenance, target matrices, signatures, and SBOM evidence before instruction-plan execution."}
          extra)))

(defn validate-stage0-compiled-project-manifest!
  [module project-manifest]
  (when-not (true? (:lockfile-complete project-manifest))
    (compiled-package-fail!
     "PKG1006" module project-manifest
     {:missing-fields [:lockfile-complete]})))

(defn validate-stage0-compiled-build-graph!
  [module build-graph]
  (when-not (every? #(set/subset? (:effects %)
                                  (:declared-effects build-graph))
                    (:nodes build-graph))
    (compiled-package-fail!
     "PKG2001" module build-graph
     {:missing-fields [:declared-effects]})))

(defn validate-stage0-compiled-artifact-manifest!
  [module artifact-manifest]
  (when-not (get-in artifact-manifest [:evidence :safety])
    (compiled-package-fail!
     "PKG3005" module artifact-manifest
     {:missing-fields [:safety-evidence]})))

(defn validate-stage0-compiled-package-operation!
  [module package-operation]
  (when-not (true? (:download-verified package-operation))
    (compiled-package-fail!
     "PKG4001" module package-operation
     {:missing-fields [:download-verified]})))

(defn validate-stage0-compiled-resolution-report!
  [module resolution-report]
  (when-not (true? (:capability-compatible resolution-report))
    (compiled-package-fail!
     "PKG5002" module resolution-report
     {:missing-fields [:capability-compatible]})))

(defn validate-stage0-compiled-capability-manifest!
  [module capability-manifest]
  (when (seq (set/intersection
              (get-in capability-manifest [:capabilities :denies])
              (get-in capability-manifest [:capabilities :requests])))
    (compiled-package-fail!
     "PKG6004" module capability-manifest
     {:missing-fields [:denied-authority]})))

(defn validate-stage0-compiled-reproducible-build!
  [module reproducible-build-recipe]
  (when-not (= :disabled
               (get-in reproducible-build-recipe
                       [:environment :network]))
    (compiled-package-fail!
     "PKG7003" module reproducible-build-recipe
     {:missing-fields [:controlled-network]})))