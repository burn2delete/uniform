

(defn p18-t06-accepted-command-record
  [{:keys [fixture output-path expected-stdout module]}]
  (let [check-result (p18-t06-shell "bin/gravity" "check" fixture)
        run-result (p18-t06-shell "bin/gravity" "run" fixture)
        compile-result (p18-t06-shell "bin/gravity" "compile" fixture
                                      "-o" output-path)
        compile-artifact (p18-t04-read-edn-stdout compile-result)
        executable-result (p18-t06-shell output-path)
        inspect-result (p18-t06-shell "bin/gravity" "inspect"
                                      (str output-path ".gravity-artifact.edn"))
        inspected-artifact (p18-t04-read-edn-stdout inspect-result)
        version-result (p18-t06-shell "bin/gravity" "--version")
        executable-file (java.io.File. output-path)
        executable? (and (.isFile executable-file)
                         (.canExecute executable-file))
        stdout-matches? (= expected-stdout (:out executable-result))
        seedless-artifact?
        (and (false? (:clojure-seed-boundary? inspected-artifact))
             (true? (:reproducible-release? inspected-artifact))
             (true? (:signed-release? inspected-artifact))
             (true? (:governance-approved? inspected-artifact)))]
    {:fixture fixture
     :output-path output-path
     :status (if (and (zero? (:exit check-result))
                      (zero? (:exit run-result))
                      (zero? (:exit compile-result))
                      (zero? (:exit executable-result))
                      (zero? (:exit inspect-result))
                      (zero? (:exit version-result))
                      executable?
                      stdout-matches?
                      seedless-artifact?)
               :accepted
               :failed)
     :expected-module module
     :expected-stdout expected-stdout
     :check-result (select-keys check-result [:exit :out :err])
     :run-result (select-keys run-result [:exit :out :err])
     :compile-result (select-keys compile-result [:exit :out :err])
     :compile-artifact
     (select-keys compile-artifact
                  [:kind :task :status :executable-path
                   :release-binary-path :clojure-seed-boundary?
                   :reproducible-release? :signed-release?
                   :governance-approved?])
     :executable-result (select-keys executable-result [:exit :out :err])
     :inspect-result (select-keys inspect-result [:exit :out :err])
     :version-result (select-keys version-result [:exit :out :err])
     :executable? executable?
     :stdout-matches? stdout-matches?
     :artifact-inspection-final-release? seedless-artifact?
     :matches-expected?
     (and (zero? (:exit check-result))
          (zero? (:exit run-result))
          (zero? (:exit compile-result))
          (zero? (:exit executable-result))
          (zero? (:exit inspect-result))
          (zero? (:exit version-result))
          executable?
          stdout-matches?
          seedless-artifact?)}))

(defn p18-t06-rejected-command-record
  [{:keys [fixture output-path expected-diagnostic]}]
  (let [result (p18-t06-shell "bin/gravity" "compile" fixture "-o"
                              output-path)
        diagnostic-present? (str/includes? (:err result)
                                           expected-diagnostic)]
    {:fixture fixture
     :command ["bin/gravity" "compile" fixture "-o" output-path]
     :status :rejected
     :expected-diagnostic expected-diagnostic
     :result (select-keys result [:exit :out :err])
     :stable-diagnostic-through-final-release? diagnostic-present?
     :matches-expected? (and (= 1 (:exit result))
                             diagnostic-present?)}))

(defn p18-t06-final-release-diagnostics
  [fixture candidate]
  (let [boundary (:release-boundary-record candidate)
        rebuild (:rebuild-verification-record candidate)
        provenance (:provenance-record candidate)
        sbom (:sbom-record candidate)
        signing (:signing-record candidate)
        governance (:governance-approval-record candidate)
        target (:target candidate)
        accepted (:accepted-command-proofs candidate)
        rejected (:rejected-command-proofs candidate)]
    (vec
     (concat
      (when-not (and provenance
                     (= :complete (:status provenance))
                     (true? (:builder-identity-verified? provenance))
                     (= :passed (get-in provenance
                                        [:revocation-check :status])))
        [(p18-t06-diagnostic-record
          "P18T06001" fixture candidate
          {:provenance-present? (boolean provenance)})])
      (when-not (and rebuild
                     (true? (:binary-identical? rebuild))
                     (true? (:provenance-identical? rebuild))
                     (true? (:sbom-identical? rebuild))
                     (true? (:signing-record-identical? rebuild))
                     (true? (:command-contract-evidence-identical?
                             rebuild)))
        [(p18-t06-diagnostic-record
          "P18T06002" fixture candidate
          {:rebuild-verification
           (select-keys rebuild
                        [:binary-identical? :provenance-identical?
                         :sbom-identical? :signing-record-identical?
                         :command-contract-evidence-identical?])})])
      (when-not (and boundary (p18-t06-boundary-seedless? boundary))
        [(p18-t06-diagnostic-record
          "P18T06003" fixture candidate
          {:seed-boundary-facts (:seed-boundary-facts boundary)
           :components
           (mapv #(select-keys % [:component :clojure-seed-boundary?])
                 (:release-boundary-components boundary))})])
      (when-not (and (seq accepted)
                     (seq rejected)
                     (every? :matches-expected? accepted)
                     (every? :matches-expected? rejected))
        [(p18-t06-diagnostic-record
          "P18T06004" fixture candidate
          {:accepted-failed
           (mapv :fixture (remove :matches-expected? accepted))
           :rejected-failed
           (mapv :fixture (remove :matches-expected? rejected))})])
      (when-not (and sbom
                     (true? (:complete? sbom))
                     (seq (:components sbom))
                     (= (:artifact-id provenance)
                        (:provenance-record-id sbom)))
        [(p18-t06-diagnostic-record
          "P18T06005" fixture candidate
          {:sbom-present? (boolean sbom)
           :component-count (count (:components sbom))
           :expected-provenance-id (:artifact-id provenance)
           :observed-provenance-id (:provenance-record-id sbom)})])
      (when-not (and signing
                     (true? (:cryptographic-release-signature? signing))
                     (true? (get-in signing
                                    [:verification :signature-valid?]))
                     (= (:artifact-id provenance)
                        (get-in signing
                                [:payload :provenance-record-id]))
                     (= (:artifact-id sbom)
                        (get-in signing [:payload :sbom-id])))
        [(p18-t06-diagnostic-record
          "P18T06006" fixture candidate
          {:signing-present? (boolean signing)
           :verification (:verification signing)})])
      (when-not (contains? p18-t06-supported-targets target)
        [(p18-t06-diagnostic-record
          "P18T06007" fixture candidate
          {:target target
           :supported-targets (vec p18-t06-supported-targets)})])
      (when-not (and governance
                     (= :approved (:approval-status governance))
                     (true? (:rfc-accepted? governance))
                     (true? (:implementation-artifacts-linked?
                             governance))
                     (true? (:conformance-artifacts-linked?
                             governance)))
        [(p18-t06-diagnostic-record
          "P18T06008" fixture candidate
          {:governance-present? (boolean governance)
           :approval-status (:approval-status governance)
           :rfc-accepted? (:rfc-accepted? governance)})])))))

(defn p18-t06-rejected-release-candidates
  [candidate]
  (let [fixtures
        [{:fixture :p18-t06-missing-provenance
          :expected-diagnostic "P18T06001"
          :candidate (assoc candidate :provenance-record nil)}
         {:fixture :p18-t06-unreproducible-binary
          :expected-diagnostic "P18T06002"
          :candidate (assoc-in candidate
                               [:rebuild-verification-record
                                :binary-identical?]
                               false)}
         {:fixture :p18-t06-clojure-in-release-boundary
          :expected-diagnostic "P18T06003"
          :candidate
          (assoc-in candidate
                    [:release-boundary-record
                     :release-boundary-components 0
                     :clojure-seed-boundary?]
                    true)}
         {:fixture :p18-t06-missing-diagnostic-parity
          :expected-diagnostic "P18T06004"
          :candidate (assoc-in candidate
                               [:rejected-command-proofs 0
                                :matches-expected?]
                               false)}
         {:fixture :p18-t06-missing-sbom
          :expected-diagnostic "P18T06005"
          :candidate (assoc candidate :sbom-record nil)}
         {:fixture :p18-t06-invalid-signing-record
          :expected-diagnostic "P18T06006"
          :candidate (assoc-in candidate
                               [:signing-record :verification
                                :signature-valid?]
                               false)}
         {:fixture :p18-t06-unsupported-target-claim
          :expected-diagnostic "P18T06007"
          :candidate (assoc candidate :target :native-unknown)}
         {:fixture :p18-t06-governance-approval-gap
          :expected-diagnostic "P18T06008"
          :candidate (assoc-in candidate
                               [:governance-approval-record
                                :approval-status]
                               :blocked)}]]
    (mapv
     (fn [{:keys [fixture expected-diagnostic candidate]}]
       (let [diagnostics (p18-t06-final-release-diagnostics
                          fixture candidate)
             observed (set (map :diagnostic diagnostics))]
         {:fixture fixture
          :status :rejected
          :expected-diagnostic expected-diagnostic
          :diagnostics diagnostics
          :matches-expected? (contains? observed expected-diagnostic)}))
     fixtures)))