

(defn p15-s23-source-syntax-serialization-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-source-syntax-serialization-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-source-syntax-fixtures artifact)))
        serialization (:serialization-roundtrip-record artifact)]
    {:source-syntax-proof-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :compiler-source-inventory-linked?
     (= :gravity/p15-s23-compiler-source-inventory-artifact
        (get-in artifact [:compiler-source-inventory-artifact :kind]))
     :c2-reader-artifact-linked?
     (= :gravity/stage0-c2-reader-document-artifact
        (get-in artifact [:c2-reader-artifact :kind]))
     :c3-syntax-artifact-linked?
     (= :gravity/stage0-c3-syntax-object-artifact
        (get-in artifact [:c3-syntax-artifact :kind]))
     :source-unit-roundtrips?
     (true? (:source-unit-roundtrip? serialization))
     :syntax-objects-roundtrip?
     (true? (:syntax-object-roundtrip? serialization))
     :source-spans-preserved?
     (every? #(p15-s23-source-syntax-span-resolves? (:span %))
             (:syntax-object-stream artifact))
     :syntax-identities-preserved?
     (true? (:stable-syntax-ids? serialization))
     :origin-chains-preserved?
     (true? (:origin-links-preserved? serialization))
     :syntax-verifier-passed?
     (= :passed (get-in artifact [:syntax-verification-report :status]))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-source-syntax-serialization-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-source-syntax-serialization-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :compiles-whole-claimed-subset? false
      :next-required-capability
      :implement_core_lowering_and_diagnostic_preservation_report}}))

(defn p15-s23-source-syntax-serialization-proof-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :source-syntax-serialization-proof source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-source-syntax-serialization-proof)
        c2-artifact
        (p15-s23-source-syntax-c2-artifact source-path
                                           (:source-text source-data))
        c3-artifact
        (p15-s23-source-syntax-c3-artifact source-path c2-artifact)
        serialization
        (p15-s23-source-syntax-serialization-roundtrip-record
         c2-artifact c3-artifact)
        sh04-identity-record
        (p15-s23-source-syntax-sh04-identity-record
         source-path (:source-unit-record c2-artifact)
         (:syntax-object-stream c3-artifact))
        identity-record
        (merge
         sh04-identity-record
         (p15-s23-source-syntax-c2-identity-record
          (:source-unit-record c2-artifact)
          (:expected-sh04-semantic-source-id sh04-identity-record)
          c3-artifact)
         (p15-s23-source-syntax-c2-context-record
          source-path (:source-unit-record c2-artifact) c3-artifact))
        candidate {:proof-contract proof-contract
                   :source-unit-record (:source-unit-record c2-artifact)
                   :syntax-object-stream (:syntax-object-stream c3-artifact)
                   :serialization-roundtrip-record serialization
                   :source-syntax-identity-record identity-record
                   :c3-syntax-artifact c3-artifact
                   :syntax-verification-report
                   (:syntax-verification-report c3-artifact)}
        diagnostics
        (p15-s23-source-syntax-proof-diagnostics source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-source-syntax-serialization-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        compiler-source-inventory
        (p15-s23-compiler-source-inventory-source-artifact source-path)
        proof-id
        (reader-canonical-hash
         {:source-id
          (get-in c2-artifact [:source-unit-record :source-id])
          :c2-artifact (:artifact-id c2-artifact)
          :c3-artifact (:artifact-id c3-artifact)
          :serialization (:serialization-id serialization)
          :proof-contract proof-contract})
        rejected-records
        (p15-s23-source-syntax-rejected-records source-path)
        artifact-base
        {:kind :gravity/p15-s23-source-syntax-serialization-proof-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-source-syntax-serialization-proof
         :source-path source-path
         :proof-id proof-id
         :compiler-source-inventory-artifact compiler-source-inventory
         :proof-contract proof-contract
         :c2-reader-artifact
         (select-keys c2-artifact
                      [:kind :artifact-id :status :source-unit-record
                       :reader-source-map :incremental-reader-hashes
                       :representation-boundary
                       :gravity-reader-boundary
                       :p15-s23-source-syntax-reader-results
                       :capability-based-proof
                       :p15-s23-capability-based-proof])
         :c3-syntax-artifact
         (select-keys c3-artifact
                      [:kind :artifact-id :syntax-serialization-fixture
                       :syntax-verification-report :capability-based-proof
                       :gravity-syntax-boundary
                       :gravity-hygiene-context-map
                       :gravity-metadata-ledger
                       :gravity-fact-invalidation-ledger
                       :gravity-origin-chain-graph
                       :gravity-syntax-ownership-product
                       :p15-compatibility-boundary])
         :source-unit-record (:source-unit-record c2-artifact)
         :source-syntax-identity-record identity-record
         :syntax-object-stream (:syntax-object-stream c3-artifact)
         :syntax-object-summary
         (p15-s23-source-syntax-summary (:syntax-object-stream c3-artifact))
         :syntax-verification-report (:syntax-verification-report c3-artifact)
         :serialization-roundtrip-record serialization
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-source-syntax-fixtures
         [{:fixture source-path
           :status :accepted
           :source-id
           (get-in c2-artifact [:source-unit-record :source-id])
           :semantic-source-id
           (:expected-sh04-semantic-source-id identity-record)
           :adapted-source-unit-id
           (:observed-adapted-source-unit-id identity-record)
           :syntax-object-count (count (:syntax-object-stream c3-artifact))
           :serialization-id (:serialization-id serialization)}]
         :rejected-p15-s23-source-syntax-fixtures rejected-records
         :p15-s23-source-syntax-serialization-diagnostic-stream
         (p15-s23-source-syntax-diagnostic-stream source-path proof-id)
         :p15-s23-source-syntax-serialization-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-source-syntax-serialization-diagnostic-ids)
          :syntax-object-count (count (:syntax-object-stream c3-artifact))
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-source-syntax-serialization-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-source-syntax-serialization-proof-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-source-syntax-serialization-fail!
     "P15S23S001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-source-syntax-serialization-proof-source-artifact path)))

(def p15-s23-core-diagnostic-required-preserves
  #{:source-spans :syntax-identity :origin-chain :diagnostic-codes
    :artifact-provenance :remediation})

(def p15-s23-core-diagnostic-messages
  {"P15S23D001" "P15-S23 core lowering and diagnostic preservation report is missing"
   "P15S23D002" "P15-S23 core lowering evidence does not preserve source, syntax, origin, or evaluation-order facts"
   "P15S23D003" "P15-S23 diagnostic preservation evidence is incomplete or unstable"
   "P15S23D004" "P15-S23 core lowering or diagnostic evidence is not linked to the required artifacts"
   "P15S23D005" "P15-S23 core lowering and diagnostic preservation report makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-core-diagnostic-ids
  ["P15S23D001" "P15S23D002" "P15S23D003" "P15S23D004"
   "P15S23D005"])

(def p15-s23-core-diagnostic-forms
  #{:quote :if :do :let :fn :loop :recur :def :var :set! :try :throw :match
    :call :literal :symbol :generated-form})

(defn p15-s23-core-diagnostic-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-core-diagnostic-messages
              id
              "P15-S23 core lowering and diagnostic preservation failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-core-lowering-diagnostic-preservation-report
                 :diagnostic-family
                 :p15-s23-core-lowering-diagnostic-preservation-report
                 :value value
                 :remediation "Keep P15-S23 core lowering and diagnostic preservation evidence in Gravity-owned source, preserve source spans, syntax ids, origin chains, diagnostic ids, artifact provenance, and remediation, and keep self-hosting claims false until the complete evidence bundle exists."}
                data)))

(defn p15-s23-core-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-core-lowering-diagnostic-preservation-report
   :source-span {:source source-path}
   :message (get p15-s23-core-diagnostic-messages id)
   :facts data
   :observed value
   :remediation
   :repair_gravity_core_lowering_and_diagnostic_preservation_report})