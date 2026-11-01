

(defn- p18-t04-incomplete-final-release-proof
  [reason]
  {:kind :gravity/p18-t06-final-release-proof
   :task "P18-T06"
   :status :incomplete
   :final-release? false
   :seedless-release? false
   :clojure-seed-boundary? true
   :next-required-capability
   :self_hosted_public_binary_final_verification
   :verification-failure reason})

(defn- p18-t04-read-final-release-proof
  []
  (try
    (if-let [proof (p18-t03-read-edn-artifact p18-t06-final-proof-path)]
      {:proof proof
       :present? true
       :readable? true}
      {:proof (p18-t04-incomplete-final-release-proof :missing)
       :present? false
       :readable? false
       :failure :missing})
    (catch InterruptedException error
      (.interrupt (Thread/currentThread))
      (throw error))
    (catch Exception error
      {:proof (p18-t04-incomplete-final-release-proof :unreadable)
       :present? true
       :readable? false
       :failure :unreadable
       :error (str error)})))

(defn- p18-t04-artifact-id-matches?
  [artifact]
  (try
    (and (map? artifact)
         (string? (:artifact-id artifact))
         (= (:artifact-id artifact)
            (c4-artifact-id (dissoc artifact :artifact-id))))
    (catch InterruptedException error
      (.interrupt (Thread/currentThread))
      (throw error))
    (catch Exception _
      false)))

(defn- p18-t04-current-binary?
  [release-binary]
  (let [path (:path release-binary)
        content-hash (:content-hash release-binary)]
    (and (string? path)
         (= p18-t06-release-binary-path path)
         (string? content-hash)
         (= content-hash (p18-file-sha256 path)))))

(defn- p18-t04-current-p15-proof?
  [current-p15-proof recorded-p15-proof]
  (and (= p15-s23-compiler-source-path (:source-path current-p15-proof))
       (= (select-keys current-p15-proof
                      [:kind :artifact :artifact-id :proof-id :status
                       :full-language-compiler-self-hosted?
                       :clojure-seed-retired? :clojure-seed-boundary?])
          (select-keys recorded-p15-proof
                       [:kind :artifact :artifact-id :proof-id :status
                        :full-language-compiler-self-hosted?
                        :clojure-seed-retired? :clojure-seed-boundary?]))))

(defn- p18-t04-current-record?
  [record artifact schema-version]
  (and (map? record)
       (= artifact (:artifact record))
       (= schema-version (:schema-version record))
       (= "P18-T06" (:task record))
       (p18-t04-artifact-id-matches? record)))

(defn- p18-t04-current-release-provenance?
  [proof]
  (let [release-binary (:release-binary proof)
        path (:path release-binary)
        content-hash (:content-hash release-binary)
        provenance (:provenance-record proof)
        sbom (:sbom-record proof)
        signing (:signing-record proof)]
    (and (= :gravity/p18-t06-provenance (:artifact provenance))
         (= :gravity/p18-t06-sbom (:artifact sbom))
         (= :gravity/p18-t06-release-signing-record (:artifact signing))
         (p18-t04-artifact-id-matches? provenance)
         (p18-t04-artifact-id-matches? sbom)
         (p18-t04-artifact-id-matches? signing)
         (= :complete (:status provenance))
         (= :complete (:status sbom))
         (= :complete (:status signing))
         (= path (get-in provenance [:subject :path]))
         (= content-hash (get-in provenance [:subject :content-hash]))
         (= path (get-in sbom [:subject :path]))
         (= content-hash (get-in sbom [:subject :content-hash]))
         (= content-hash
            (get-in signing [:payload :release-binary-content-hash])))))

(defn- p18-t04-current-release-evidence?
  [proof]
  (try
    (let [release-binary (:release-binary proof)
          boundary (:release-boundary-record proof)
          recipe (:reproducible-build-recipe proof)
          rebuild (:rebuild-verification-record proof)
          provenance (:provenance-record proof)
          sbom (:sbom-record proof)
          signing (:signing-record proof)
          target-policy (:target-support-policy proof)
          compatibility (:compatibility-record proof)
          security-review (:security-review-record proof)
          release-notes (:release-notes proof)
          governance (:governance-approval-record proof)
          boundary-id (:artifact-id boundary)
          recipe-id (:artifact-id recipe)
          rebuild-id (:artifact-id rebuild)
          provenance-id (:artifact-id provenance)
          sbom-id (:artifact-id sbom)
          signing-id (:artifact-id signing)
          target-policy-id (:artifact-id target-policy)
          compatibility-id (:artifact-id compatibility)
          security-review-id (:artifact-id security-review)
          release-notes-id (:artifact-id release-notes)
          binary-path (:path release-binary)
          binary-hash (:content-hash release-binary)
          rebuilds (:rebuilds rebuild)]
      (and
       (p18-t04-current-record?
        boundary :gravity/p18-t06-final-release-boundary
        "gravity.final-release-boundary/v1")
       (contains? #{:complete :incomplete} (:status boundary))
       (p18-t04-current-record?
        recipe :gravity/p18-t06-reproducible-build-recipe
        "gravity.reproducible-build-recipe/v1")
       (= :complete (:status recipe))
       (p18-t04-current-record?
        rebuild :gravity/p18-t06-rebuild-verification
        "gravity.rebuild-verification/v1")
       (= :complete (:status rebuild))
       (p18-t04-current-record?
        provenance :gravity/p18-t06-provenance
        "gravity.provenance/v1")
       (= :complete (:status provenance))
       (p18-t04-current-record?
        sbom :gravity/p18-t06-sbom
        "gravity.sbom/v1")
       (= :complete (:status sbom))
       (p18-t04-current-record?
        signing :gravity/p18-t06-release-signing-record
        "gravity.signing-record/v1")
       (= :complete (:status signing))
       (p18-t04-current-record?
        target-policy :gravity/p18-t06-target-support-policy
        "gravity.target-support-policy/v1")
       (= :complete (:status target-policy))
       (p18-t04-current-record?
        compatibility :gravity/p18-t06-compatibility-record
        "gravity.release-compatibility/v1")
       (= :complete (:status compatibility))
       (p18-t04-current-record?
        security-review :gravity/p18-t06-security-review
        "gravity.release-security-review/v1")
       (= :complete (:status security-review))
       (p18-t04-current-record?
        release-notes :gravity/p18-t06-release-notes
        "gravity.release-notes/v1")
       (= :complete (:status release-notes))
       (p18-t04-current-record?
        governance :gravity/p18-t06-governance-approval
        "gravity.release-governance/v1")
       (= :complete (:status governance))
       (p18-t04-current-binary? release-binary)
       (= p18-t06-release-binary-path (:release-binary-path recipe))
       (= binary-hash (:release-binary-content-hash recipe))
       (true? (:builder-identity-verified? recipe))
       (= boundary-id (get-in recipe [:inputs 1 :artifact-id]))
       (= recipe-id (:recipe-id rebuild))
       (seq rebuilds)
       (= binary-path (:path (first rebuilds)))
       (every? #(and (= binary-hash (:content-hash %))
                     (true? (:executable? %)))
               rebuilds)
       (every? true?
               (map #(get rebuild %)
                    [:binary-identical? :provenance-identical?
                     :sbom-identical? :signing-record-identical?
                     :command-contract-evidence-identical?]))
       (= boundary-id (get-in provenance [:materials 0 :artifact-id]))
       (= recipe-id (get-in provenance [:materials 1 :artifact-id]))
       (= rebuild-id (get-in provenance [:materials 2 :artifact-id]))
       (map? (:compiler-lineage provenance))
       (= provenance-id (:provenance-record-id sbom))
       (= boundary-id (:release-boundary-id security-review))
       (= provenance-id (:provenance-record-id security-review))
       (= sbom-id (:sbom-id security-review))
       (= target-policy-id (:target-policy-id compatibility))
       (= release-notes-id (:release-notes-id governance))
       (= target-policy-id (:target-policy-id governance))
       (= compatibility-id (:compatibility-record-id governance))
       (= security-review-id (:security-review-id governance))
       (= signing-id (:signing-record-id governance))
       (= p18-t06-release-binary-path (:release-binary release-notes))
       (= binary-hash (:release-binary-content-hash release-notes))
       (= (p18-t06-provenance-record
           release-binary boundary recipe rebuild)
          provenance)
       (= (p18-t06-sbom-record release-binary boundary provenance)
          sbom)
       (= (p18-t06-signing-record
           release-binary boundary recipe rebuild provenance sbom)
          signing)
       (= (p18-t06-release-notes release-binary)
          release-notes)
       (= (p18-t06-target-support-policy)
          target-policy)
       (= (p18-t06-compatibility-record target-policy)
          compatibility)
       (= (p18-t06-security-review-record
           boundary provenance sbom)
          security-review)
       (= (p18-t06-governance-approval-record
           release-notes target-policy compatibility security-review signing)
          governance)
       (= (p18-t06-final-release-diagnostics
           :p18-t06-final-release proof)
          (:diagnostics proof))
       (= (p18-t06-capability-proof proof)
          (:capability-based-proof proof))))
    (catch InterruptedException error
      (.interrupt (Thread/currentThread))
      (throw error))
    (catch Exception _
      false)))

(defn- p18-t04-final-proof-verification
  [p15-final-proof]
  (let [{:keys [proof present? readable? failure error]}
        (p18-t04-read-final-release-proof)
        record-shape?
        (and (= :gravity/p18-t06-final-release-proof (:kind proof))
             (= "P18-T06" (:task proof))
             (contains? p18-t06-supported-targets (:target proof)))
        complete-record?
        (and record-shape?
             (= :complete (:status proof))
             (true? (:final-release? proof))
             (true? (:seedless-release? proof))
             (false? (:clojure-seed-boundary? proof)))
        boundary (:release-boundary-record proof)
        complete-boundary?
        (and (= :gravity/p18-t06-final-release-boundary (:artifact boundary))
             (= "P18-T06" (:task boundary))
             (p18-t04-artifact-id-matches? boundary)
             (= :complete (:status boundary))
             (true? (:final-release? boundary))
             (false? (:clojure-seed-boundary? boundary)))
        release-binary-current?
        (p18-t04-current-binary? (:release-binary proof))
        p15-final-proof-current?
        (p18-t04-current-p15-proof?
         p15-final-proof (:p15-final-seed-retirement-proof proof))
        provenance-current?
        (p18-t04-current-release-provenance? proof)
        release-evidence-current?
        (p18-t04-current-release-evidence? proof)
        authenticated?
        (and present?
             readable?
             record-shape?
             (p18-t04-artifact-id-matches? proof)
             release-binary-current?
             p15-final-proof-current?
             release-evidence-current?
             (or (not complete-record?)
                 (and complete-boundary? provenance-current?)))]
    {:proof proof
     :verification
     {:path p18-t06-final-proof-path
      :present? present?
      :readable? readable?
      :artifact-id-matches? (p18-t04-artifact-id-matches? proof)
      :release-binary-current? release-binary-current?
      :p15-final-proof-current? p15-final-proof-current?
      :complete-boundary-authenticated? complete-boundary?
      :provenance-current? provenance-current?
      :release-evidence-current? release-evidence-current?
      :authenticated? authenticated?
      :failure failure
      :error error}}))

(defn p18-t04-public-self-host-verify-command-artifact!
  []
  (let [p15-final-proof (p18-t06-current-p15-final-seed-proof)
        {:keys [proof verification]}
        (p18-t04-final-proof-verification p15-final-proof)
        p18-final-proof proof
        complete? (and (p18-t04-self-host-verify-complete?
                        p15-final-proof proof)
                       (:authenticated? verification))
        compiler-source (p18-t04-self-host-verify-compiler-source)
        diagnostics
        (if complete?
          []
          [(p18-t04-diagnostic-record
            "P18T04007"
            "gravity self-host verify"
            {:source "gravity self-host verify"
             :p15-final-seed-retirement-proof
             (select-keys p15-final-proof
                          [:kind :artifact-id :proof-id :status
                           :full-language-compiler-self-hosted?
                           :clojure-seed-retired?
                           :clojure-seed-boundary?])
             :p18-final-release-proof
             (select-keys p18-final-proof
                          [:kind :artifact-id :status :final-release?
                           :seedless-release? :clojure-seed-boundary?])
             :p18-final-release-proof-verification verification
             :compiler-source compiler-source
             :proof-artifact-path
             p18-t04-public-self-host-verify-proof-path
             :remediation
             "Complete P15-S23 final seed retirement and P18-T06 final release before `gravity self-host verify` can succeed."})])
        artifact-base
        {:kind :gravity/p18-t04-public-self-host-verify-command-proof
         :task "P18-T04"
         :status (if complete? :complete :incomplete)
         :phase :binary-distribution-and-seedless-release
         :command ["gravity" "self-host" "verify"]
         :scope :current-public-release-surface
         :governing-documents ["TEST13" "BOOT7" "BOOT8" "T1" "D9"]
         :compiler-source compiler-source
         :p15-final-seed-retirement-proof
         (select-keys p15-final-proof
                      [:kind :artifact :artifact-id :proof-id :status
                       :source-path :full-language-compiler-self-hosted?
                       :clojure-seed-retired? :clojure-seed-boundary?
                       :next-required-capability])
         :p18-final-release-proof
         (select-keys p18-final-proof
                      [:kind :artifact-id :status :target :final-release?
                       :seedless-release? :clojure-seed-boundary?
                       :next-required-capability])
         :p18-final-release-proof-verification verification
         :p18-final-release-proof-path p18-t06-final-proof-path
         :p18-release-binary-path p18-t06-release-binary-path
         :bootstrap-hosted? true
         :final-self-host-verification? complete?
         :full-language-conformance? false
         :self-hosted-conformance-runner? false
         :diagnostics diagnostics}
        proof (p18-t04-public-self-host-verify-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p18-t04-write-public-self-host-verify-command-artifacts!
  []
  (let [artifact (p18-t04-public-self-host-verify-command-artifact!)]
    (p18-t02-write-edn! p18-t04-public-self-host-verify-proof-path
                        artifact)
    (p18-t02-write-edn! p18-t04-public-self-host-verify-diagnostics-path
                        (:diagnostics artifact))
    artifact))

(defn p18-t04-public-self-host-verify-usage!
  [args]
  (p18-t04-fail!
   "P18T04008"
   {:source "gravity self-host"
    :command (vec args)
    :expected-command ["gravity" "self-host" "verify"]
    :remediation "Use `gravity self-host verify` exactly."}))

(defn p18-t04-public-self-host-verify-command!
  [args]
  (if-not (and (= "self-host" (first args))
               (= "verify" (second args))
               (= 2 (count args)))
    (p18-t04-public-self-host-verify-usage! args)
    (let [artifact (p18-t04-public-self-host-verify-command-artifact!)]
      (if (= :complete (:status artifact))
        (prn artifact)
        (p18-t04-fail!
         "P18T04007"
         {:source "gravity self-host verify"
          :command (vec args)
          :proof-artifact-path p18-t04-public-self-host-verify-proof-path
          :proof-artifact-id (:artifact-id artifact)
          :p15-final-seed-retirement-proof
          (:p15-final-seed-retirement-proof artifact)
          :p18-final-release-proof (:p18-final-release-proof artifact)
          :compiler-source (:compiler-source artifact)
          :bootstrap-hosted? true
          :full-language-compiler-self-hosted? false
          :clojure-seed-boundary? true
          :remediation
          "Complete P15-S23 final seed retirement and P18-T06 final release before claiming public self-host verification."})))))

(def p18-t00-artifact-dir p18-t00-semantics/artifact-dir)
(def p18-t00-report-path p18-t00-semantics/report-path)
(def p18-t00-accepted-extension-fixtures
  p18-t00-semantics/accepted-extension-fixtures)
(def p18-t00-rejected-extension-fixtures
  p18-t00-semantics/rejected-extension-fixtures)

(defn p18-t00-shell
  [& args]
  (p18-shell-run args))

(defn p18-t00-output-has-warning?
  [result]
  (p18-t00-semantics/output-has-warning? result))

(defn p18-t00-semantic-summary
  [path]
  (let [source-text (slurp path)
        compile-artifact (compile-source path source-text)
        reader-artifact (read-source-artifact path source-text)
        source-unit (c2-source-unit-record
                     path source-text standard-reader-options)]
    (p18-t00-semantics/semantic-summary
     {:path path
      :compile-artifact compile-artifact
      :reader-artifact reader-artifact
      :source-unit source-unit
      :source-extension (gravity-source-extension path)
      :source-kind (gravity-source-kind path)
      :recognized-source? (qst-or-gravity-source? path)})))

(defn p18-t00-compile-artifact-source-path
  [artifact]
  (p18-t00-semantics/compile-artifact-source-path artifact))

(defn p18-t00-accepted-extension-record
  [{:keys [gravity qst expected-stdout bootstrap-module release-module
           bootstrap-output-prefix release-output-prefix]}]
  (p18-t00-orchestration/accepted-extension-record
   {:semantic-summary p18-t00-semantic-summary
    :bootstrap-shell p18-t00-shell
    :release-shell p18-t06-shell
    :read-edn-stdout p18-t04-read-edn-stdout
    :compile-artifact-source-path p18-t00-compile-artifact-source-path}
   {:gravity gravity
    :qst qst
    :expected-stdout expected-stdout
    :bootstrap-module bootstrap-module
    :release-module release-module
    :bootstrap-output-prefix bootstrap-output-prefix
    :release-output-prefix release-output-prefix}))

(defn p18-t00-rejected-extension-record
  [{:keys [gravity qst expected-diagnostic output-prefix]}]
  (p18-t00-orchestration/rejected-extension-record
   {:bootstrap-shell p18-t00-shell
    :release-shell p18-t06-shell}
   {:gravity gravity
    :qst qst
    :expected-diagnostic expected-diagnostic
    :output-prefix output-prefix}))

(defn p18-t00-capability-proof
  [artifact]
  (p18-t00-semantics/capability-proof
   artifact
   {:co-canonical-source-extensions co-canonical-source-extensions
    :accepted-extension-fixtures p18-t00-accepted-extension-fixtures
    :qst-source-kind (gravity-source-kind "examples/core-app.qst")
    :gravity-source-kind
    (gravity-source-kind "examples/core-app.gravity")}))

(defn p18-t00-report-markdown
  [artifact]
  (p18-t00-semantics/report-markdown artifact p18-t00-artifact-dir))

(defn p18-t00-co-canonical-source-extensions-artifact!
  []
  (p18-t00-orchestration/co-canonical-source-extensions-artifact!
   {:write-final-release-artifacts! p18-t06-write-final-release-artifacts!
    :accepted-fixtures p18-t00-accepted-extension-fixtures
    :rejected-fixtures p18-t00-rejected-extension-fixtures
    :accepted-extension-record p18-t00-accepted-extension-record
    :rejected-extension-record p18-t00-rejected-extension-record
    :capability-proof p18-t00-capability-proof
    :artifact-id c4-artifact-id}))

(defn p18-t00-write-co-canonical-source-extension-artifacts!
  []
  (p18-t00-orchestration/write-co-canonical-source-extension-artifacts!
   {:artifact! p18-t00-co-canonical-source-extensions-artifact!
    :artifact-dir p18-t00-artifact-dir
    :report-path p18-t00-report-path
    :report-parent (.getParent (java.io.File. p18-t00-report-path))
    :ensure-dir! p18-ensure-dir!
    :write-edn! p18-t02-write-edn!
    :write-report! spit
    :report-markdown p18-t00-report-markdown}))
