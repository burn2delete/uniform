

(defn p18-t02-write-packaged-jvm-cli-artifacts!
  []
  (let [artifact (p18-t02-packaged-jvm-cli-artifact!)]
    (p18-t02-write-edn!
     (str p18-t02-artifact-dir "/p18-t02-packaged-jvm-cli-proof.edn")
     artifact)
    (p18-t02-write-edn!
     (str p18-t02-artifact-dir "/p18-t02-package-manifest.edn")
     (:package-manifest artifact))
    (p18-t02-write-edn!
     (str p18-t02-artifact-dir "/p18-t02-dependency-record.edn")
     (:dependency-record artifact))
    (p18-t02-write-edn!
     (str p18-t02-artifact-dir "/p18-t02-artifact-manifest.edn")
     (:artifact-manifest artifact))
    (p18-t02-write-edn!
     (str p18-t02-artifact-dir "/p18-t02-reproducible-build.edn")
     (:reproducible-build-record artifact))
    (p18-t02-write-edn!
     (str p18-t02-artifact-dir "/p18-t02-provenance.edn")
     (:provenance-record artifact))
    (p18-t02-write-edn!
     (str p18-t02-artifact-dir "/p18-t02-sbom.edn")
     (:sbom-record artifact))
    (p18-t02-write-edn!
     (str p18-t02-artifact-dir "/p18-t02-signing-record.edn")
     (:signing-record artifact))
    artifact))

(def p18-t03-build-root "target/phase-18/self-hosted")
(def p18-t03-artifact-dir "docs/artifacts/phase-18/self-hosted")
(def p18-t03-release-artifact-path
  (str p18-t03-build-root "/gravity-release-artifact.edn"))
(def p18-t03-compiler-source "bootstrap/gravity/p15_s23/compiler.gravity")
(def p18-t03-supported-targets #{:gravity-release-manifest})
(def p18-t03-accepted-fixtures
  ["examples/hello.gravity"
   "examples/core-app.gravity"
   "examples/nontrivial-app.gravity"])

(def p18-t03-diagnostic-messages
  {"P18T03001" "release artifact candidate was produced by Clojure packaging"
   "P18T03002" "release artifact candidate is missing self-hosted compiler evidence"
   "P18T03003" "release artifact candidate is missing runtime boundary evidence"
   "P18T03004" "release artifact candidate is missing artifact provenance"
   "P18T03005" "release artifact candidate claims an unsupported target"})

(def p18-t03-diagnostic-ids
  ["P18T03001" "P18T03002" "P18T03003" "P18T03004" "P18T03005"])

(defn p18-t03-diagnostic-record
  [id candidate facts]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p18-t03-self-hosted-release-artifact
   :source-span {:source p18-t03-release-artifact-path}
   :message (get p18-t03-diagnostic-messages id)
   :artifact-producer (:artifact-producer candidate)
   :target (:target candidate)
   :facts facts
   :remediation :repair_p18_t03_self_hosted_release_artifact})

(defn p18-t03-evidence-present?
  [evidence]
  (boolean
   (and (= :verified (:status evidence))
        (re-find #"^sha256:" (str (:artifact-id evidence))))))

(def p18-t03-p15-artifact-files
  {:whole-language-compiler-artifact
   "docs/artifacts/phase-15/bootstrap/p15-s23-whole-language-compiler-artifact.edn"
   :stage3-seedless-compiler-candidate
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage3-seedless-compiler-candidate.edn"
   :stage3-equivalence-bundle
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage3-equivalence-bundle.edn"
   :stage3-self-hosted-application-execution
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage3-self-hosted-application.edn"
   :final-seed-retirement-proof
   "docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn"})

(def p18-t03-p15-summary-keys
  [:seedless-compiler-candidate-present?
   :compiler-path-seedless?
   :accepted-output-equivalent?
   :rejected-diagnostics-equivalent?
   :clojure-stage0-verifier-absent?
   :clojure-stage0-release-compiler-absent?
   :stage3-equivalence-bundle-present?
   :rebuild-equivalence-complete?
   :conformance-evidence-complete?
   :stage3-self-hosted-application-execution-present?
   :accepted-application-run?
   :rejected-application-fails-closed?
   :stage3-toolchain-seedless?
   :runtime-capability-recorded?
   :final-seed-retirement-proof-present?
   :stage3-seedless-boundary-proven?
   :stage3-equivalence-and-application-proven?
   :release-governance-closed?
   :tcb-seed-boundary-retired?
   :provenance-closure-recorded?
   :full-language-compiler-self-hosted?
   :clojure-seed-retired?
   :clojure-seed-boundary?])

(defn p18-t03-read-edn-artifact
  [path]
  (when (.isFile (java.io.File. path))
    (edn/read-string (slurp path))))

(defn p18-t03-p15-evidence-summary
  [path artifact]
  (let [proof (:capability-based-proof artifact)]
    (merge
     {:status :verified
      :artifact (:kind artifact)
      :artifact-id (:artifact-id artifact)
      :proof-id (:proof-id artifact)
      :source-path (:source-path artifact)
      :artifact-path path}
     (select-keys proof p18-t03-p15-summary-keys)
     (select-keys artifact
                  [:full-language-compiler-self-hosted?
                   :clojure-seed-retired?
                   :clojure-seed-boundary?]))))

(defn p18-t03-p15-evidence-from-file
  [key]
  (let [path (get p18-t03-p15-artifact-files key)]
    (when-let [artifact (p18-t03-read-edn-artifact path)]
      (p18-t03-p15-evidence-summary path artifact))))

(defn p18-t03-stage3-evidence
  []
  {:whole-language-compiler-artifact
   (or (p18-t03-p15-evidence-from-file :whole-language-compiler-artifact)
       (p15-s23-whole-language-compiler-artifact-evidence))
   :stage3-seedless-compiler-candidate
   (or (p18-t03-p15-evidence-from-file
        :stage3-seedless-compiler-candidate)
       (p15-s23-stage3-seedless-compiler-candidate-evidence))
   :stage3-equivalence-bundle
   (or (p18-t03-p15-evidence-from-file :stage3-equivalence-bundle)
       (p15-s23-stage3-equivalence-bundle-evidence))
   :stage3-self-hosted-application-execution
   (or (p18-t03-p15-evidence-from-file
        :stage3-self-hosted-application-execution)
       (p15-s23-stage3-self-hosted-application-evidence))
   :final-seed-retirement-proof
   (or (p18-t03-p15-evidence-from-file :final-seed-retirement-proof)
       (p15-s23-final-seed-retirement-evidence))})

(defn p18-t03-compiler-path-record
  [stage3-evidence]
  (let [candidate (:stage3-seedless-compiler-candidate stage3-evidence)
        equivalence (:stage3-equivalence-bundle stage3-evidence)
        final-proof (:final-seed-retirement-proof stage3-evidence)
        complete? (and (p18-t03-evidence-present? candidate)
                       (p18-t03-evidence-present? equivalence)
                       (p18-t03-evidence-present? final-proof)
                       (true? (:compiler-path-seedless? candidate))
                       (true? (:stage3-equivalence-bundle-present?
                               equivalence))
                       (true? (:final-seed-retirement-proof-present?
                               final-proof)))
        base {:artifact :gravity/p18-t03-compiler-path-record
              :schema-version "gravity.release-compiler-path/v1"
              :compiler-stage :stage3-self-hosted
              :compiler-path-id
              (c4-artifact-id
               {:stage :stage3-self-hosted
                :candidate-id (:artifact-id candidate)
                :equivalence-id (:artifact-id equivalence)
                :final-proof-id (:artifact-id final-proof)})
              :release-compiler-id
              (c4-artifact-id
               {:release-compiler :gravity-stage3-release-compiler
                :candidate-id (:artifact-id candidate)
                :final-proof-id (:artifact-id final-proof)})
              :stage3-seedless-compiler-candidate candidate
              :stage3-equivalence-bundle equivalence
              :final-seed-retirement-proof final-proof
              :self-hosted-compiler-evidence-present? complete?
              :compiler-path-seedless?
              (true? (:compiler-path-seedless? candidate))
              :clojure-stage0-verifier-absent?
              (true? (:clojure-stage0-verifier-absent? candidate))
              :clojure-stage0-release-compiler-absent?
              (true? (:clojure-stage0-release-compiler-absent? candidate))
              :status (if complete? :complete :failed)}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t03-runtime-boundary-record
  [stage3-evidence compiler-path]
  (let [stage3-app (:stage3-self-hosted-application-execution stage3-evidence)
        final-proof (:final-seed-retirement-proof stage3-evidence)
        complete? (and (p18-t03-evidence-present? stage3-app)
                       (p18-t03-evidence-present? final-proof)
                       (true? (:stage3-toolchain-seedless? stage3-app))
                       (true? (:runtime-capability-recorded? stage3-app))
                       (true? (:stage3-equivalence-and-application-proven?
                               final-proof)))
        base {:artifact :gravity/p18-t03-runtime-boundary-record
              :schema-version "gravity.release-runtime-boundary/v1"
              :runtime-path-id
              (c4-artifact-id
               {:runtime :stage3-self-hosted-application-runtime
                :stage3-app-id (:artifact-id stage3-app)
                :compiler-path-id (:compiler-path-id compiler-path)})
              :runtime-family :stage3-self-hosted-release-runtime
              :runtime-path-seedless? complete?
              :runtime-capability-recorded?
              (true? (:runtime-capability-recorded? stage3-app))
              :stage3-toolchain-seedless?
              (true? (:stage3-toolchain-seedless? stage3-app))
              :stage3-self-hosted-application-execution stage3-app
              :status (if complete? :complete :failed)}]
    (assoc base :artifact-id (c4-artifact-id base))))