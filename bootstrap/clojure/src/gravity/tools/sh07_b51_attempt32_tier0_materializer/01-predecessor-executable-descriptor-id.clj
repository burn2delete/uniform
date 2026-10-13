
(def predecessor-executable-descriptor-id
  "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234")

(def materializer-source-paths
  ["bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj"])

(def binding-replay-architecture-report-path
  "docs/artifacts/phase-15/reports/sh07-b51-binding-replay-index-architecture-v1.md")

(def output-file-names
  {:producer-identity "checker-a-identity.edn"
   :independent-identity "checker-b-identity.edn"
   :producer-output "checker-a-output.edn"
   :independent-output "checker-b-output.edn"
   :certificate-pair "certificate-pair.edn"
   :carrier "linked-carrier-v4-candidate.edn"
   :receipt "identity-receipt.edn"})

(def certificate-keys-v9
  #{:artifact :domain :schema-version :status :role
    :architecture-report-sha256 :claim-id :claim :assumptions :checker
    :inputs :result :semantic-witness :provenance :raw-source-transcript
    :m2-transcript :profile-bounds :evidence-root :certificate-id})

(def certificate-input-keys-v9
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :lowering-product-id :predecessor-executable-descriptor-id
    :module-source-identities :module-source-byte-counts :emitter-source-id
    :emitter-semantic-id :m0-ids :m1-id :m2-ids :bundle-core-id})

(def semantic-witness-keys
  #{:schema :content :operation-manifest :operation-schedule-id
    :fuel-receipts :l1 :l2 :c2 :c6 :boot2 :boot3 :boot4 :witness-id})

(def lowering-product-keys
  #{:schema :content :source-authority :module-products :pass-records
    :fact-records :provenance-records :fuel-subjects :semantic-roots
    :product-id})

(def bad-actual-keys
  #{:status :code :rule :reason :input-id :mutated-id :expected :actual
    :committed-row :output})

(def ^:private roles
  [:producer-census :independent-verifier-census])
(def ^:private algorithm-roles
  [:source-first-exhaustive :m2-first-independent-exhaustive])
(def ^:private identity-coordinates
  [:source-sha256 :semantic-id :executable-id :dependency-closure-id])
(def ^:private common-certificate-keys
  [:architecture-report-sha256 :claim-id :claim :assumptions :inputs
   :profile-bounds])

(defn- reject! [reason & [context]]
  (throw (ex-info "SH-07 Attempt32 parent-safe validation rejected"
                  {:reason reason :context (or context {})})))
(defn- require! [condition reason & [context]]
  (when-not condition (reject! reason context)))
(defn- exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))
(defn- digest-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))
(defn- git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))
(defn- dense-ordered? [rows]
  (and (vector? rows) (= (vec (range (count rows))) (mapv :ordinal rows))))

(defn certificate-v9-shell-valid?
  "Closed, role-neutral validation of the V9 certificate boundary.

  Deep census and canonical-hash equations remain the responsibility of the
  neutral bootstrap predicate passed to `validate-produced-child-pair!`."
  [certificate]
  (let [inputs (:inputs certificate)
        witness (:semantic-witness certificate)]
    (and (exact-keys? certificate certificate-keys-v9)
         (= :gravity/sh07-b51-compile-census-certificate
            (:artifact certificate))
         (= 1 (:schema-version certificate))
         (= :accepted (:status certificate))
         (some #{(:role certificate)} roles)
         (digest-id? (:architecture-report-sha256 certificate))
         (digest-id? (:claim-id certificate))
         (digest-id? (:evidence-root certificate))
         (digest-id? (:certificate-id certificate))
         (exact-keys? inputs certificate-input-keys-v9)
         (= (:architecture-report-sha256 certificate)
            (:architecture-report-sha256 inputs))
         (git-id? (:snapshot-commit inputs))
         (git-id? (:snapshot-tree inputs))
         (= predecessor-executable-descriptor-id
            (:predecessor-executable-descriptor-id inputs))
         (digest-id? (:lowering-product-id inputs))
         (exact-keys? witness semantic-witness-keys)
         (= :gravity/sh07-b51-authentic-lowering-witness-v1
            (:schema witness))
         (= {:commit (:snapshot-commit inputs)
             :tree (:snapshot-tree inputs)}
            (:content witness))
         (digest-id? (:operation-schedule-id witness))
         (digest-id? (:witness-id witness))
         (dense-ordered? (:operation-manifest witness))
         (= 7 (count (:operation-manifest witness)))
         (vector? (:fuel-receipts witness))
         (= 7 (count (:fuel-receipts witness)))
         (= (mapv :operation-id (:operation-manifest witness))
            (mapv :operation-id (:fuel-receipts witness)))
         (= (mapv :lowering-product-id (:operation-manifest witness))
            (vec (repeat 7 (:lowering-product-id inputs))))
         (every? true? (map :within-bounds (:fuel-receipts witness))))))

(defn validate-bad-actual!
  "Validate the exact receipt-free rejection result, including both nils."
  ([actual] (validate-bad-actual! actual nil))
  ([actual expected]
   (require! (exact-keys? actual bad-actual-keys) :bad-actual-keyset)
   (require! (= :rejected (:status actual)) :bad-actual-status)
   (require! (= "SH07-B51-EDGE-AUTHORITY" (:code actual)) :bad-actual-code)
   (require! (= "L2-UNKNOWN-CORE-FORM" (:rule actual)) :bad-actual-rule)
   (require! (= :unauthenticated-b51-external-link (:reason actual))
             :bad-actual-reason)
   (require! (nil? (:committed-row actual)) :bad-actual-committed-row)
   (require! (nil? (:output actual)) :bad-actual-output)
   (when expected (require! (= expected actual) :bad-actual-exact-value))
   actual))

(defn validate-certificate-pair!
  "Validate an already-produced A/B pair without loading either role."
  [certificates]
  (require! (and (vector? certificates) (= 2 (count certificates)))
            :certificate-pair-shape)
  (let [[a b] certificates ca (:checker a) cb (:checker b)]
    (require! (every? certificate-v9-shell-valid? certificates)
              :certificate-invalid)
    (require! (= roles (mapv :role certificates)) :certificate-role-order)
    (require! (= algorithm-roles
                 (mapv #(get-in % [:checker :algorithm-role]) certificates))
              :certificate-algorithm-role-order)
    (require! (= (select-keys a common-certificate-keys)
                 (select-keys b common-certificate-keys))
              :certificate-common-claim)
    (require! (= (:semantic-witness a) (:semantic-witness b))
              :certificate-common-witness)
    (require! (= (get-in a [:result :normalized-census-id])
                 (get-in b [:result :normalized-census-id])
                 (get-in a [:claim :normalized-census-id]))
              :certificate-normalized-census)
    (require! (= (get-in a [:claim :physical-census])
                 (get-in b [:claim :physical-census]))
              :certificate-physical-census)
    (require! (= (get-in a [:claim :category-schedule])
                 (get-in b [:claim :category-schedule]))
              :certificate-category-schedule)
    (require! (= (get-in a [:claim :category-commitments])
                 (get-in b [:claim :category-commitments]))
              :certificate-category-commitments)
    (require! (not= (:certificate-id a) (:certificate-id b))
              :certificate-identities-distinct)
    (require! (every? #(and (digest-id? (get ca %))
                            (digest-id? (get cb %))
                            (not= (get ca %) (get cb %)))
                     identity-coordinates)
              :certificate-role-identities-distinct)
    certificates))

(defn build-semantic-witness-runtime
  "Build the neutral runtime wrapper after exact common-witness validation."
  [H content checker-ids witness]
  (require! (fn? H) :canonical-id-function)
  (require! (= 2 (count checker-ids)) :checker-id-count)
  (require! (every? digest-id? checker-ids) :checker-id-shape)
  (require! (exact-keys? witness semantic-witness-keys) :witness-keyset)
  (let [base {:schema :gravity/sh07-b51-semantic-witness-runtime-v1
              :content content :checker-ids checker-ids :witness witness
              :equal true}]
    (assoc base :runtime-witness-id
           (H [:gravity/sh07-b51-semantic-witness-runtime-v1 base]))))

(defn validate-produced-child-pair!
  "Validate inert child output and exact M product/witness joins.

  `authority` supplies only neutral bootstrap predicates and H."
  [authority {:keys [producer-output independent-output
                     producer-identity independent-identity
                     producer-lowering-product independent-lowering-product
                     reconstructed-lowering-product reconstructed-witness]}]
  (let [{:keys [H certificate-valid? certificate-pair-valid?]} authority
        a (:certificate producer-output)
        b (:certificate independent-output)]
    (require! (and (fn? H) (fn? certificate-valid?)
                   (fn? certificate-pair-valid?))
              :neutral-authority-functions)
    (require! (= [:accepted :accepted]
                 [(:status producer-output) (:status independent-output)])
              :child-output-status)
    (require! (= [(:checker producer-identity) (:checker independent-identity)]
                 [(:checker a) (:checker b)])
              :child-identity-join)
    (let [pair (validate-certificate-pair! [a b])
          products [producer-lowering-product independent-lowering-product]
          product-id (:product-id reconstructed-lowering-product)
          witness (:semantic-witness a)
          checker-ids [(:identity-manifest-id producer-identity)
                       (:identity-manifest-id independent-identity)]]
      (require! (every? certificate-valid? pair)
                :neutral-certificate-validation)
      (require! (certificate-pair-valid? pair)
                :neutral-certificate-pair-validation)
      (require! (every? #(exact-keys? % lowering-product-keys) products)
                :lowering-product-keyset)
      (require! (= producer-lowering-product independent-lowering-product
                   reconstructed-lowering-product)
                :lowering-product-exact-join)
      (require! (= product-id (get-in a [:inputs :lowering-product-id])
                   (get-in b [:inputs :lowering-product-id]))
                :lowering-product-id-join)
      (require! (= witness (:semantic-witness b) reconstructed-witness)
                :semantic-witness-exact-join)
      {:certificates pair
       :semantic-witness-runtime
       (build-semantic-witness-runtime H (:content witness)
                                       checker-ids witness)})))

(defn reconstruct-parent-safe-v9-inputs!
  "Invoke passed receipt-free owner/fuel constructors without a reverse require."
  [materialize-api {:keys [module-transactions root-specs bundle-core
                           fuel-source-roots pre-stages collections-by-owner]}]
  (let [{:keys [build-owner-registry build-owner-selection
                build-fuel-subjects build-fixed-fuel-operation-specs]}
        materialize-api]
    (require! (every? fn? [build-owner-registry build-owner-selection
                           build-fuel-subjects
                           build-fixed-fuel-operation-specs])
              :materialize-api)
    (let [owner-registry (build-owner-registry module-transactions)
          owner-selection
          (build-owner-selection root-specs bundle-core owner-registry)
          fuel-subjects
          (build-fuel-subjects fuel-source-roots pre-stages
                               collections-by-owner)]
      {:owner-registry owner-registry :owner-selection owner-selection
       :fuel-subjects fuel-subjects
       :operation-specs (build-fixed-fuel-operation-specs fuel-subjects)})))
