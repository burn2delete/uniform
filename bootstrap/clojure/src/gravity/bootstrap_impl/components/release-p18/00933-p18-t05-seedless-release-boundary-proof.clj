

(defn p18-t05-seedless-release-boundary-proof
  [artifact]
  (let [boundary (:release-boundary-record artifact)
        components (p18-t05-components-by-key boundary)
        accepted (:accepted-boundary-proofs artifact)
        rejected (:rejected-boundary-fixtures artifact)
        diagnostic-stream (set (map :diagnostic
                                    (get-in artifact
                                            [:p18-t05-diagnostic-stream
                                             :diagnostics])))
        diagnostics (set (map :diagnostic (:diagnostics artifact)))]
    {:task "P18-T05"
     :status (:status artifact)
     :boundary-diagnostics-covered?
     (set/subset? diagnostics (set p18-t05-diagnostic-ids))
     :gravity-binary-clojure-seed-boundary?
     (:clojure-seed-boundary? (get components :gravity-binary))
     :compiler-path-clojure-seed-boundary?
     (:clojure-seed-boundary? (get components :compiler-path))
     :runtime-path-clojure-seed-boundary?
     (:clojure-seed-boundary? (get components :runtime-path))
     :release-compiler-path-clojure-seed-boundary?
     (:clojure-seed-boundary? (get components :release-compiler-path))
     :all-required-seed-boundary-facts-false?
     (p18-t05-seed-boundary-retired? boundary)
     :accepted-fixtures-covered?
     (= (set (map :fixture p18-t05-accepted-fixtures))
        (set (map :fixture accepted)))
     :accepted-check-run-compile-survived?
     (every? :command-survived? accepted)
     :artifact-inspection-seedless?
     (every? :artifact-inspection-seedless? accepted)
     :release-proof-command-seedless?
     (every? :release-proof-command-seedless? accepted)
     :rejected-command-fixtures-covered?
     (every? :matches-expected? (:rejected-command-proofs artifact))
     :rejected-boundary-fixtures-covered?
     (= (set p18-t05-diagnostic-ids)
        (set (map :expected-diagnostic rejected)))
     :diagnostics-covered?
     (= (set p18-t05-diagnostic-ids)
        (set (concat (map :expected-diagnostic rejected)
                     diagnostic-stream)))
     :bootstrap-recovery-explicit?
     (true? (get-in artifact
                    [:bootstrap-audit-record
                     :bootstrap-recovery-explicit?]))
     :bootstrap-excluded-from-public-release-boundary?
     (true? (get-in artifact
                    [:bootstrap-audit-record
                     :bootstrap-excluded-from-public-release-boundary?]))
     :seedless-release-boundary-eligible?
     (true? (get-in artifact
                    [:release-eligibility-report
                     :seedless-release-boundary-eligible?]))
     :final-release-eligible?
     (get-in artifact [:release-eligibility-report
                       :final-release-eligible?])
     :next-required-capability
     (if (= :complete (:status artifact))
       :p18-t06-reproducibility-provenance-sbom-signing-governance
       :p15-s23-final-seed-retirement)}))

(defn p18-t05-existing-p18-t03-artifact
  []
  (or (p18-t03-read-edn-artifact
       (str p18-t03-artifact-dir
            "/p18-t03-self-hosted-release-artifact-proof.edn"))
      (p18-t03-write-self-hosted-release-artifacts!)))

(defn p18-t05-existing-p18-t04-artifact
  []
  (or (p18-t03-read-edn-artifact
       (str p18-t04-artifact-dir
            "/p18-t04-executable-command-contract-proof.edn"))
      (p18-t04-write-executable-command-artifacts!)))

(defn p18-t05-seedless-release-boundary-artifact!
  []
  (let [p18-t04-artifact (p18-t05-existing-p18-t04-artifact)
        p18-t03-artifact (p18-t05-existing-p18-t03-artifact)
        release-binary (p18-t05-write-release-binary!)
        boundary-base (p18-t05-release-boundary-record release-binary
                                                       p18-t03-artifact
                                                       p18-t04-artifact)
        diagnostics (p18-t05-boundary-diagnostics boundary-base)
        boundary-status (if (seq diagnostics) :incomplete :complete)
        boundary (assoc boundary-base
                        :status boundary-status
                        :diagnostics diagnostics
                        :artifact-id
                        (c4-artifact-id
                         (dissoc (assoc boundary-base
                                        :status boundary-status
                                        :diagnostics diagnostics)
                                 :artifact-id)))
        _ (p18-t02-write-edn! p18-t05-release-boundary-path boundary)
        accepted-records
        (mapv p18-t05-accepted-boundary-record p18-t05-accepted-fixtures)
        rejected-command-records
        (mapv p18-t05-rejected-command-record
              p18-t05-rejected-command-fixtures)
        rejected-boundary-records
        (p18-t05-rejected-boundary-records boundary)
        tcb-delta (p18-t05-tcb-delta-record boundary)
        provenance (p18-t05-provenance-attestation-record
                    boundary p18-t03-artifact p18-t04-artifact)
        bootstrap-audit (p18-t05-bootstrap-audit-record boundary)
        eligibility (p18-t05-release-eligibility-report boundary)
        proof-id
        (c4-artifact-id
         {:boundary-id (:artifact-id boundary)
          :accepted (mapv :output-path accepted-records)
          :rejected (mapv :expected-diagnostic rejected-boundary-records)
          :release-binary (:content-hash release-binary)})
        artifact-base
        {:kind :gravity/p18-t05-seedless-release-boundary-proof
         :task "P18-T05"
         :status boundary-status
         :phase :binary-distribution-and-seedless-release
         :release-binary release-binary
         :release-boundary-record boundary
         :tcb-delta-record tcb-delta
         :provenance-attestation provenance
         :bootstrap-audit-record bootstrap-audit
         :release-eligibility-report eligibility
         :accepted-boundary-proofs accepted-records
         :rejected-command-proofs rejected-command-records
         :rejected-boundary-fixtures rejected-boundary-records
         :p18-t05-diagnostic-stream
         (p18-t05-diagnostic-stream proof-id)
         :seedless-release-boundary? (= :complete boundary-status)
         :final-release-eligible? false
         :diagnostics diagnostics}
        proof (p18-t05-seedless-release-boundary-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p18-t05-write-seedless-release-artifacts!
  []
  (let [artifact (p18-t05-seedless-release-boundary-artifact!)]
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir
          "/p18-t05-seedless-release-boundary-proof.edn")
     artifact)
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir "/p18-t05-release-boundary.edn")
     (:release-boundary-record artifact))
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir "/p18-t05-tcb-delta.edn")
     (:tcb-delta-record artifact))
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir "/p18-t05-provenance-attestation.edn")
     (:provenance-attestation artifact))
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir "/p18-t05-bootstrap-audit-record.edn")
     (:bootstrap-audit-record artifact))
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir "/p18-t05-release-eligibility-report.edn")
     (:release-eligibility-report artifact))
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir "/p18-t05-accepted-boundary-proofs.edn")
     (:accepted-boundary-proofs artifact))
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir "/p18-t05-rejected-command-proofs.edn")
     (:rejected-command-proofs artifact))
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir "/p18-t05-rejected-boundary-fixtures.edn")
     (:rejected-boundary-fixtures artifact))
    (p18-t02-write-edn!
     (str p18-t05-artifact-dir "/p18-t05-diagnostic-stream.edn")
     (:p18-t05-diagnostic-stream artifact))
    artifact))

(def p18-t06-build-root "target/phase-18/release")
(def p18-t06-artifact-dir "docs/artifacts/phase-18/release")
(def p18-t06-release-binary-path (str p18-t06-build-root "/gravity"))
(def p18-t06-final-proof-path
  (str p18-t06-artifact-dir "/p18-t06-final-release-proof.edn"))
(def p18-t06-release-boundary-path
  (str p18-t06-artifact-dir "/p18-t06-final-release-boundary.edn"))
(def p18-t06-report-path
  "docs/artifacts/phase-18/reports/p18-t06-reproducibility-provenance-sbom-signing-governance-report.md")

(def p18-t06-supported-targets
  #{:portable-posix-shell :macos-arm64-posix-shell})

(def p18-t06-accepted-fixtures
  [{:fixture "examples/hello.gravity"
    :output-path (str p18-t06-build-root "/hello-app")
    :expected-stdout "Hello Gravity\n"
    :module "hello"}
   {:fixture "examples/core-app.gravity"
    :output-path "target/core-app"
    :expected-stdout "core-app\ngravity:19:2\n(:ok 19)\n"
    :module "core.app"}
   {:fixture "examples/nontrivial-app.gravity"
    :output-path (str p18-t06-build-root "/nontrivial-app")
    :expected-stdout "nontrivial-app\ngravity:ready:2\n(:release 24)\n"
    :module "nontrivial.app"}])

(def p18-t06-rejected-command-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-release.gravity"
    :output-path (str p18-t06-build-root "/rejected-final-release")
    :expected-diagnostic "B13-RELEASE"}])

(def p18-t06-diagnostic-messages
  {"P18T06001" "final release candidate is missing complete provenance"
   "P18T06002" "final release candidate is not reproducible"
   "P18T06003" "Clojure appears inside the final release boundary"
   "P18T06004" "final release candidate is missing stable command diagnostic parity"
   "P18T06005" "final release candidate is missing a complete SBOM"
   "P18T06006" "final release candidate has an invalid release signing record"
   "P18T06007" "final release candidate claims an unsupported target"
   "P18T06008" "final release candidate is missing governance approval"})

(def p18-t06-diagnostic-ids
  ["P18T06001" "P18T06002" "P18T06003" "P18T06004"
   "P18T06005" "P18T06006" "P18T06007" "P18T06008"])