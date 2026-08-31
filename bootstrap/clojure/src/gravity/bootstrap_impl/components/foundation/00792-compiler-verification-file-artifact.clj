

(defn compiler-verification-file-artifact
  [path]
  (compiler-verification-source-artifact path (slurp path)))

(def c1-architecture-governing-document
  "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md")

(def c1-architecture-rejected-designs
  [{:diagnostic "C1-PIPELINE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-pipeline-order.gravity"
    :rejected-design :hidden-or-noncanonical-stage-order}
   {:diagnostic "C1-PASS-CONTRACT"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-pass-contract.gravity"
    :rejected-design :incomplete-pass-contract}
   {:diagnostic "C1-EVIDENCE-DROP"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-evidence-drop.gravity"
    :rejected-design :silent-metadata-loss}
   {:diagnostic "C1-UNCHECKED-BACKEND"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-unchecked-backend.gravity"
    :rejected-design :backend-from-unchecked-input}
   {:diagnostic "C1-DOMAIN-ANCHOR"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c1-domain-anchor.gravity"
    :rejected-design :unanchored-domain-ir}
   {:diagnostic "C1-MANIFEST"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-manifest-gap.gravity"
    :rejected-design :missing-pipeline-manifest}
   {:diagnostic "C1-SELF-HOST"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c1-self-host.gravity"
    :rejected-design :uncomparable-self-hosting-artifact}])

(defn c1-architecture-source-overrides
  [module]
  (get-in module [:metadata :compiler :c1-architecture] {}))

(defn c1-architecture-fail!
  [id source-path subject extra]
  (fail! id
         (case id
           "C1-DOMAIN-ANCHOR" "domain IR lacks a semantic anchor to typed core or MIR"
           "C1-SELF-HOST" "compiler artifact cannot participate in bootstrap comparison"
           "compiler architecture document coverage failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :c1-compiler-architecture
                 :stage (or (:stage subject) :compiler-architecture)
                 :document-id "C1"
                 :expected-document c1-architecture-governing-document
                 :artifact-id (:artifact-id subject)
                 :domain (:domain subject)
                 :semantic-anchor (:semantic-anchor subject)
                 :remediation "Preserve the C1 pipeline manifest, semantic anchors, verifier gates, provenance graph, diagnostics, and bootstrap comparison inputs as public compiler artifacts."}
                extra)))

(defn c1-architecture-validate-overrides!
  [source-path overrides]
  (case (:fail overrides)
    :domain-anchor
    (c1-architecture-fail!
     "C1-DOMAIN-ANCHOR" source-path
     {:stage :lower-domain-ir
      :artifact-id :gravity/domain-ir
      :domain :efir
      :semantic-anchor nil}
     {:missing-fields [:semantic-anchor]})
    :self-host
    (c1-architecture-fail!
     "C1-SELF-HOST" source-path
     {:stage :record-package-provenance
      :artifact-id :gravity/self-hosting-comparison-inputs}
     {:missing-fields [:stage-outputs-comparable?
                       :compiler-data-gravity-values?]})
    nil))

(defn c1-architecture-artifact-id
  [artifact]
  (str "sha256:"
       (sha256-hex
        (binding [*print-length* 256
                  *print-level* 16]
          (pr-str artifact)))))

(defn c1-architecture-stage-record
  [stage artifact owner-doc]
  {:stage stage
   :owner-doc owner-doc
   :artifact-kind (:kind artifact)
   :artifact-id (c1-architecture-artifact-id artifact)
   :document-set (:document-set artifact)
   :diagnostics (:diagnostics artifact)
   :verifier-result :passed})

(defn c1-architecture-capability-proof
  [artifact]
  (let [contracts (:pass-contract-registry artifact)
        rejected (set (map :diagnostic (:rejected-design-coverage artifact)))
        conformance (:document-conformance artifact)]
    {:pipeline-manifest-emitted?
     (= :gravity/compiler-pipeline (get-in artifact [:pipeline-manifest
                                                     :artifact]))
     :pass-contracts-complete?
     (and (= (count (:canonical-pipeline-order artifact))
             (count contracts))
          (every? #(empty? (compiler-pass-missing-fields
                            % compiler-pass-contract-required-fields))
                  contracts))
     :verifier-gates-executed?
     (every? #(= :passed (:status %)) (:verifier-gate-reports artifact))
     :metadata-preserved?
     (true? (:metadata-preservation-demonstrated? conformance))
     :unchecked-backend-rejected?
     (contains? rejected "C1-UNCHECKED-BACKEND")
     :domain-anchors-linked?
     (true? (:domain-ir-anchors-demonstrated? conformance))
     :invalidation-regeneration-demonstrated?
     (true? (:invalidation-regeneration-demonstrated? conformance))
     :diagnostics-origin-linked?
     (true? (:diagnostics-origin-linked? conformance))
     :bootstrap-comparison-ready?
     (= :ready (get-in artifact [:self-hosting-comparison-inputs :status]))
     :status :complete}))

(defn c1-architecture-validate!
  [source-path artifact]
  (let [proof (c1-architecture-capability-proof artifact)]
    (when-not (:pipeline-manifest-emitted? proof)
      (c1-architecture-fail! "C1-MANIFEST" source-path
                             (:pipeline-manifest artifact)
                             {:missing-fields [:pipeline-manifest]}))
    (when-not (:pass-contracts-complete? proof)
      (c1-architecture-fail! "C1-PASS-CONTRACT" source-path
                             {:stage :pass-contract-registry}
                             {:missing-fields [:pass-contract-registry]}))
    (when-not (:verifier-gates-executed? proof)
      (c1-architecture-fail! "C1-PIPELINE" source-path
                             {:stage :verifier-gates}
                             {:missing-fields [:verifier-gate-reports]}))
    (when-not (:metadata-preserved? proof)
      (c1-architecture-fail! "C1-EVIDENCE-DROP" source-path
                             {:stage :metadata-preservation}
                             {:missing-fields [:metadata-preservation]}))
    (when-not (:unchecked-backend-rejected? proof)
      (c1-architecture-fail! "C1-UNCHECKED-BACKEND" source-path
                             {:stage :lower-target}
                             {:missing-fields [:unchecked-backend-fixture]}))
    (when-not (:domain-anchors-linked? proof)
      (c1-architecture-fail! "C1-DOMAIN-ANCHOR" source-path
                             {:stage :lower-domain-ir}
                             {:missing-fields [:semantic-anchor]}))
    (when-not (:bootstrap-comparison-ready? proof)
      (c1-architecture-fail! "C1-SELF-HOST" source-path
                             {:stage :record-package-provenance}
                             {:missing-fields [:self-hosting-comparison-inputs]})))
  :complete)