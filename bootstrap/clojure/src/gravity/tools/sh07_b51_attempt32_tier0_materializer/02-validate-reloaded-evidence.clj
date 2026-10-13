
(defn validate-reloaded-evidence!
  "Recheck both checkers, the pair, the full carrier, and its exact pin map."
  [materialized values]
  (let [identities (:identities materialized)
        envelopes (:envelopes materialized)
        materializer-dependency-closure
        (:materializer-dependency-closure materialized)
        output-a (:producer-output values)
        output-b (:independent-output values)
        pair (:certificate-pair values)
        carrier (:carrier values)
        expected-tool-closure-a
        (materializer-tool-closure-id
         :producer-census (:producer-identity values)
         materializer-dependency-closure)
        expected-tool-closure-b
        (materializer-tool-closure-id
         :independent-verifier-census (:independent-identity values)
         materializer-dependency-closure)]
    (require! (= (:producer-identity values)
                 (:producer-census identities))
              :reloaded-producer-identity)
    (require! (= (:independent-identity values)
                 (:independent-verifier-census identities))
              :reloaded-independent-identity)
    (require! (= pair [(:certificate output-a) (:certificate output-b)])
              :reloaded-output-certificate-join)
    (require! (= expected-tool-closure-a
                 (get-in envelopes
                         [:producer-census :provenance :tool-closure-id])
                 (get-in output-a
                         [:certificate :provenance :tool-closure-id]))
              :reloaded-producer-tool-closure)
    (require! (= expected-tool-closure-b
                 (get-in envelopes
                         [:independent-verifier-census :provenance
                          :tool-closure-id])
                 (get-in output-b
                         [:certificate :provenance :tool-closure-id]))
              :reloaded-independent-tool-closure)
    (require! (= (:pin-map materialized)
                 (bootstrap/p15-s23-sh07-b51-v4-candidate-pin-map carrier))
              :reloaded-pin-map)
    (require! (= pair
                 (validate-certificate-pair!
                  (:producer-census envelopes) output-a
                  (:independent-verifier-census envelopes) output-b))
              :reloaded-certificate-pair)
    (require! (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid? carrier)
              :reloaded-carrier-invalid)
    (require! (= carrier
                 (bootstrap/p15-s23-sh07-b51-build-linked-carrier-v4-candidate
                  (:products materialized) pair))
              :reloaded-carrier-reconstruction)
    true))

(defn materialize-current
  [{:keys [repository-root snapshot completed-at evidence-relative-root]}]
  (require! (instance? Path repository-root) :repository-root)
  (require! (and (map? snapshot) (= #{:commit :tree} (set (keys snapshot)))
                 (git-id? (:commit snapshot)) (git-id? (:tree snapshot)))
            :snapshot)
  (require! (and (string? completed-at) (not (str/blank? completed-at)))
            :completed-at)
  (require! (safe-relative-path? evidence-relative-root) :evidence-root)
  (let [root (.normalize (.toAbsolutePath ^Path repository-root))
        identities
        {:producer-census
         (identity/checker-identity-manifest root :producer-census)
         :independent-verifier-census
         (identity/checker-identity-manifest
          root :independent-verifier-census)}
        _ (require!
           (every?
            #(not= (get-in identities [:producer-census :checker %])
                   (get-in identities
                           [:independent-verifier-census :checker %]))
            [:source-sha256 :semantic-id :executable-id
             :dependency-closure-id]) :checker-manifest-independence)
        products (bootstrap/p15-s23-sh07-b51-current-bundle-core-products)
        forms (source-forms root products)
        common (common-envelope snapshot products forms)
        materializer-dependency-closure (materializer-dependencies root)
        binding-replay-architecture-report
        (source-record root binding-replay-architecture-report-path)
        envelope-a
        (checker-envelope common :producer-census
                          (:producer-census identities) snapshot completed-at
                          evidence-relative-root
                          materializer-dependency-closure)
        envelope-b
        (checker-envelope common :independent-verifier-census
                          (:independent-verifier-census identities) snapshot
                          completed-at evidence-relative-root
                          materializer-dependency-closure)
        output-a (checker-a/produce-certificate envelope-a)
        output-b (checker-b/produce-certificate envelope-b)
        certificates
        (validate-certificate-pair! envelope-a output-a envelope-b output-b)
        carrier
        (bootstrap/p15-s23-sh07-b51-build-linked-carrier-v4-candidate
         products certificates)]
    (require! (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid? carrier)
              :candidate-carrier-invalid)
    {:snapshot snapshot :completed-at completed-at
     :evidence-relative-root evidence-relative-root
     :identities identities :products products
     :binding-replay-architecture-report binding-replay-architecture-report
     :materializer-dependency-closure materializer-dependency-closure
     :envelopes {:producer-census envelope-a
                 :independent-verifier-census envelope-b}
     :outputs {:producer-census output-a
               :independent-verifier-census output-b}
     :certificates certificates :carrier carrier
     :pin-map (bootstrap/p15-s23-sh07-b51-v4-candidate-pin-map carrier)}))
