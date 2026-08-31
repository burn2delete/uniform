; Semantic decomposition of HEAD reader line 10547.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-record-safe-capability-call!-safe14!
 [kind checker record args]
 (case
  kind
  :safe14-package-manifest
  (record-checker!
   checker
   :safe14-package-safety-manifests
   (merge
    record
    {:package-version (dispatch-arg-value args 1),
     :reproducible? (boolean (dispatch-arg-value args 10)),
     :package-id (dispatch-arg-value args 0),
     :denied-effects (or (dispatch-arg-value args 5) #{}),
     :build-effects (or (dispatch-arg-value args 4) #{}),
     :metadata-complete? true,
     :native-deps (or (dispatch-arg-value args 7) []),
     :runtime-capabilities (or (dispatch-arg-value args 3) #{}),
     :generated-artifacts (or (dispatch-arg-value args 9) []),
     :profiles (or (dispatch-arg-value args 2) #{}),
     :compiler-plugins (or (dispatch-arg-value args 8) []),
     :unsafe-summary (dispatch-arg-value args 6)}))
  :safe14-lockfile
  (record-checker!
   checker
   :safe14-lockfile-records
   (merge
    record
    {:package-version (dispatch-arg-value args 1),
     :provider-versions (dispatch-arg-value args 3),
     :package-id (dispatch-arg-value args 0),
     :pinned? (boolean (dispatch-arg-value args 8)),
     :package-digest (dispatch-arg-value args 2),
     :build-grants (or (dispatch-arg-value args 4) #{}),
     :native-digests (or (dispatch-arg-value args 5) {}),
     :generated-artifact-digests (or (dispatch-arg-value args 6) {}),
     :replay-records (or (dispatch-arg-value args 7) #{})}))
  :safe14-build-effect-summary
  (record-checker!
   checker
   :safe14-build-effect-summaries
   (merge
    record
    {:package-id (dispatch-arg-value args 0),
     :build-effects (or (dispatch-arg-value args 1) #{}),
     :grants (or (dispatch-arg-value args 2) #{}),
     :providers (or (dispatch-arg-value args 3) #{}),
     :replayable? (boolean (dispatch-arg-value args 4)),
     :denied-effects (or (dispatch-arg-value args 5) #{})}))
  :safe14-runtime-capability-summary
  (record-checker!
   checker
   :safe14-runtime-capability-summaries
   (merge
    record
    {:package-id (dispatch-arg-value args 0),
     :dependency-path (or (dispatch-arg-value args 1) []),
     :requested-capabilities (or (dispatch-arg-value args 2) #{}),
     :approved-capabilities (or (dispatch-arg-value args 3) #{}),
     :denied-capabilities (or (dispatch-arg-value args 4) #{}),
     :root-approval (dispatch-arg-value args 5)}))
  :safe14-unsafe-summary
  (record-checker!
   checker
   :safe14-unsafe-summaries
   (merge
    record
    {:package-id (dispatch-arg-value args 0),
     :unsafe-island-count (or (dispatch-arg-value args 1) 0),
     :operation-families (or (dispatch-arg-value args 2) #{}),
     :profiles (or (dispatch-arg-value args 3) #{}),
     :safe-wrappers (or (dispatch-arg-value args 4) #{}),
     :review-status (dispatch-arg-value args 5),
     :certificate (dispatch-arg-value args 6),
     :unsafe-dependencies (or (dispatch-arg-value args 7) #{})}))
  :safe14-native-dependency
  (record-checker!
   checker
   :safe14-native-dependency-records
   (merge
    record
    {:required-capabilities (or (dispatch-arg-value args 10) #{}),
     :license (dispatch-arg-value args 8),
     :package-id (dispatch-arg-value args 0),
     :safety-wrapper-package (dispatch-arg-value args 9),
     :advisories (or (dispatch-arg-value args 11) #{}),
     :source (dispatch-arg-value args 3),
     :digest (dispatch-arg-value args 4),
     :targets (or (dispatch-arg-value args 6) #{}),
     :library (dispatch-arg-value args 1),
     :metadata-complete? true,
     :abi (dispatch-arg-value args 5),
     :version (dispatch-arg-value args 2),
     :link-mode (dispatch-arg-value args 7)}))
  :safe14-generated-provenance
  (record-checker!
   checker
   :safe14-generated-artifact-provenance
   (merge
    record
    {:artifact-id (dispatch-arg-value args 0),
     :generator-id (dispatch-arg-value args 1),
     :source-digests (or (dispatch-arg-value args 2) #{}),
     :build-effects (or (dispatch-arg-value args 3) #{}),
     :provider-grants (or (dispatch-arg-value args 4) #{}),
     :output-digest (dispatch-arg-value args 5),
     :reproducible? (boolean (dispatch-arg-value args 6)),
     :safety-checks (or (dispatch-arg-value args 7) #{})}))
  :safe14-signature-attestation
  (record-checker!
   checker
   :safe14-signature-attestation-records
   (merge
    record
    {:package-id (dispatch-arg-value args 0),
     :package-digest (dispatch-arg-value args 1),
     :signature (dispatch-arg-value args 2),
     :attestation (dispatch-arg-value args 3),
     :verified? (boolean (dispatch-arg-value args 4))}))
  :safe14-authority-diff
  (record-checker!
   checker
   :safe14-authority-diff-records
   (merge
    record
    {:package-id (dispatch-arg-value args 0),
     :from-version (dispatch-arg-value args 1),
     :to-version (dispatch-arg-value args 2),
     :added-runtime-capabilities (or (dispatch-arg-value args 3) #{}),
     :added-build-effects (or (dispatch-arg-value args 4) #{}),
     :added-unsafe-families (or (dispatch-arg-value args 5) #{}),
     :approval (dispatch-arg-value args 6),
     :status
     (or (dispatch-arg-value args 7) :no-unapproved-expansion)}))
  :safe14-conformance
  (record-checker!
   checker
   :safe14-conformance-records
   (merge
    record
    {:document :SAFE14,
     :status (or (dispatch-arg-value args 0) :complete),
     :positive-fixtures :passed,
     :negative-fixtures :passed}))
  semantic-early-record-safe-capability-call!-unhandled))
