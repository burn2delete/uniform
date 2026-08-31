(defn- p15-s23-b3-llvm-expected-b1-context-evidence
  [c11-artifact checked-core c11-report]
  (let [mir (:mir-module c11-artifact)
        capability-proof-table (:capability-proof-table mir)
        proof-certificate-table (:proof-certificate-table mir)
        safety-proof-table (:safety-proofs proof-certificate-table)
        verifier-record (p15-s23-b3-llvm-c11-verifier-record c11-report)
        verifier-record-id (p15-s23-c11-mir-digest verifier-record)
        dependency-contract
        {:source-core (:artifact-id checked-core)
         :c11-source-rule (:source-rule c11-artifact)
         :c11-pass (get-in mir [:pass-execution-record :record-id])
         :b3-source p15-s23-b3-llvm-expected-source-content-hash}
        input
        {:kind :gravity/mir
         :id (:mir-id c11-artifact)
         :artifact-id (:artifact-id c11-artifact)
         :verified? true
         :verifier-report verifier-record
         :verifier-report-id verifier-record-id}]
    {:b1-input input
     :c14-input (assoc input :optimization-status :not-run
                       :domain-status :not-applicable)
     :proofs
     {:capability
      (assoc (p15-s23-b3-llvm-content-binding capability-proof-table)
             :proof-ids (vec (sort (keys capability-proof-table))))
      :certificates
      (assoc (p15-s23-b3-llvm-content-binding proof-certificate-table)
             :safety-proof-count (count safety-proof-table)
             :safety-proof-ids (vec (sort (keys safety-proof-table))))}
     :fact-table-closure
     {:type (p15-s23-b3-llvm-content-binding (:type-table mir))
      :effect (p15-s23-b3-llvm-content-binding (:effect-table mir))
      :ownership (p15-s23-b3-llvm-content-binding (:ownership-table mir))
      :capability (p15-s23-b3-llvm-content-binding
                   (:capability-table mir))
      :safety (p15-s23-b3-llvm-content-binding (:safety-table mir))
      :source-map (p15-s23-b3-llvm-content-binding (:source-map mir))}
     :b1-preflight (:b1-preflight c11-artifact)
     :mir-id (:mir-id c11-artifact)
     :mir-artifact-id (:artifact-id c11-artifact)
     :source-core-artifact-id (:artifact-id checked-core)
     :c11-verification-status (:status c11-report)
     :c11-semantic-replay-parity (:semantic-replay-parity c11-report)
     :pass-execution-record-id
     (get-in mir [:pass-execution-record :record-id])
     :dependencies dependency-contract
     :b1-source-map {:id (p15-s23-c11-mir-digest (:source-map mir))
                     :complete? true}
     :c14-source-map {:id (p15-s23-c11-mir-digest (:source-map mir))
                      :preserved? true}}))

(defn- p15-s23-b3-llvm-verify-context-bindings!
  [artifact bridge-packet source-path]
  (let [c14-request (get-in bridge-packet [:c14 :request])
        b1-packet
        (dissoc (:b1 bridge-packet)
                :source-rule :actual-path-provenance
                :semantic-id :artifact-id :actual-path-binding-id)
        expected (:contract-bindings c14-request)
        valid?
        (and
         (= bridge-packet (:c13-c14-b1-packet artifact))
         (= c14-request (:c14-request artifact))
         (= b1-packet (:b1-packet artifact))
         (= (:optimized-mir bridge-packet)
            (get-in bridge-packet [:c13 :optimized-mir]))
         (= (:artifact-id (:c13 bridge-packet))
            (get-in c14-request [:input :artifact-id])
            (get-in b1-packet [:input :artifact-id]))
         (= (get-in bridge-packet [:c14 :artifact-id])
            (get-in bridge-packet [:b1 :backend-manifest
                                   :c14-artifact-id]))
         (= (get-in c14-request [:request-id])
            (get-in b1-packet [:backend-manifest :c14-request-id]))
         (every? #(= expected (get-in artifact [% :contract-bindings]))
                 [:c14-request :b1-packet :b3-record
                  :b13-record :c18-record]))]
    (when-not valid?
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path artifact
       {:missing-fact
        :fresh-c11-bound-contract-evidence-before-toolchain}))
    :passed))

(defn- p15-s23-b3-llvm-verify-context-provenance!
  [artifact c11-artifact context binding]
  (when-not
   (= {:source (:source-path context)
       :c11-source (get-in c11-artifact
                           [:provenance :actual-paths :c11-source])
       :c13-source
       (get-in artifact
               [:c13-c14-b1-packet :actual-path-provenance :c13-source])
       :c14-source
       (get-in artifact
               [:c13-c14-b1-packet :actual-path-provenance :c14-source])
       :b1-source
       (get-in artifact
               [:c13-c14-b1-packet :actual-path-provenance :b1-source])
       :c13-c14-b1-packet-binding-id
       (get-in artifact [:c13-c14-b1-packet :actual-path-binding-id])
       :b3-source (:source-path binding)}
      (select-keys (:actual-path-provenance artifact)
                   [:source :c11-source :c13-source :c14-source :b1-source
                    :c13-c14-b1-packet-binding-id :b3-source]))
    (p15-s23-b3-llvm-fail!
     "B3-MANIFEST" (:source-path context) artifact
     {:missing-fact
      :fresh-context-bound-source-provenance-before-toolchain}))
  :passed)
