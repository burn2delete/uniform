

(defn p13-validate-dependency!
  [source-path module dependency]
  (let [consumer (:profile module)
        producer (:profile dependency)
        edge (or (:edge dependency)
                 (cond
                   (profile-direct-import-allowed? consumer producer) :direct
                   (contains? p13-artifact-boundary-kinds
                              (:boundary dependency)) :artifact-only
                   (:boundary dependency) :facade-required
                   :else :direct))]
    (when producer
      (cond
        (and (= :direct edge)
             (not (profile-direct-import-allowed? consumer producer))
             (:boundary dependency))
        (p13-fail! "P13-MATRIX" source-path module dependency
                   {:declared-edge edge
                    :standard-direct-imports (get profile-direct-imports
                                                  consumer)
                    :suggested-boundary :facade-required})

        (and (:generated? dependency)
             (not (profile-direct-import-allowed? consumer producer))
             (nil? (:boundary dependency)))
        (p13-fail! "P13-GENERATED" source-path module dependency
                   {:declared-edge edge
                    :missing-fact :generated-profile-boundary
                    :suggested-boundary :generated-facade-or-artifact})

        (profile-direct-import-allowed? consumer producer)
        (p13-direct-record module dependency)

        (nil? (:boundary dependency))
        (p13-fail! "P13-DIRECT" source-path module dependency
                   {:declared-edge edge
                    :standard-direct-imports (get profile-direct-imports
                                                  consumer)
                    :suggested-boundary :facade-required})

        (= :artifact-only edge)
        (p13-fail! "P13-ARTIFACT" source-path module dependency
                   {:declared-edge edge
                    :artifact (:artifact dependency)
                    :artifact-schema (:artifact-schema dependency)
                    :suggested-boundary :artifact-manifest})

        :else
        (p13-validate-facade! source-path module dependency)))))

(defn p13-artifact-record
  [source-path module artifact]
  (let [consumer (:profile module)
        producer (:producer-profile artifact)
        effect-denials (p13-effect-denials consumer (:effects artifact))
        capability-denials (p13-capability-denials consumer
                                                   (:capabilities artifact))]
    (when-not (contains? p13-artifact-boundary-kinds
                         (:artifact-kind artifact))
      (p13-fail! "P13-ARTIFACT" source-path module
                 {:module (:producer-namespace artifact)
                  :profile producer
                  :edge :artifact-only
                  :boundary (:artifact-kind artifact)}
                 {:artifact (:artifact artifact)
                  :artifact-kind (:artifact-kind artifact)
                  :suggested-boundary :declared-artifact-boundary}))
    (when (empty? (:evidence artifact))
      (p13-fail! "P13-EVIDENCE" source-path module
                 {:module (:producer-namespace artifact)
                  :profile producer
                  :edge :artifact-only
                  :boundary (:artifact-kind artifact)}
                 {:missing-evidence #{:schema :provenance}
                  :suggested-boundary :declared-artifact-boundary}))
    (when (seq effect-denials)
      (p13-fail! "P13-EFFECT" source-path module
                 {:module (:producer-namespace artifact)
                  :profile producer
                  :edge :artifact-only
                  :boundary (:artifact-kind artifact)}
                 {:denied-effects effect-denials
                  :suggested-boundary :effect-free-artifact}))
    (when (seq capability-denials)
      (p13-fail! "P13-CAPABILITY" source-path module
                 {:module (:producer-namespace artifact)
                  :profile producer
                  :edge :artifact-only
                  :boundary (:artifact-kind artifact)}
                 {:denied-capabilities capability-denials
                  :suggested-boundary :capability-free-artifact}))
    {:consumer {:namespace (:module module)
                :profile consumer}
     :producer {:namespace (:producer-namespace artifact)
                :profile producer}
     :edge :artifact-only
     :artifact (:artifact artifact)
     :artifact-kind (:artifact-kind artifact)
     :artifact-schema (:artifact-schema artifact)
     :evidence (:evidence artifact)
     :status :accepted}))

(defn p13-artifact-boundaries
  [module]
  (vec (get-in module [:metadata :profile-compatibility
                       :artifact-boundaries] [])))

(defn p13-standard-library-facades
  [module]
  (vec (get-in module [:metadata :profile-compatibility
                       :standard-library-facades] [])))

(defn p13-capability-proof
  [dependency-records artifact-records]
  {:dependency-edges (count dependency-records)
   :artifact-edges (count artifact-records)
   :effect-authority-preserved?
   (every? #(empty? (:effects %)) dependency-records)
   :capability-authority-preserved?
   (every? #(empty? (:capabilities %)) dependency-records)
   :artifact-authority-preserved?
   (every? #(and (empty? (:effects %))
                 (empty? (:capabilities %)))
           artifact-records)
   :status :complete})

(defn p13-conformance-results
  [dependency-records artifact-records standard-library-facades]
  (let [edge-kinds (set (concat (map :edge dependency-records)
                                (map :edge artifact-records)))
        required #{:direct :facade-required :artifact-only}
        missing (set/difference required edge-kinds)]
    {:document "P13"
     :required-edge-kinds required
     :covered-edge-kinds edge-kinds
     :missing-edge-kinds missing
     :diagnostic-ids p13-diagnostic-ids
     :dependency-graph-status :complete
     :standard-library-facades standard-library-facades
     :standard-library-facade-status (if (seq standard-library-facades)
                                       :complete
                                       :not-present)
     :status (if (empty? missing) :complete :incomplete)}))

(defn p13-compatibility-matrix
  []
  (mapv (fn [profile]
          {:consumer profile
           :direct-imports (get profile-direct-imports profile)
           :facade-required :matrix-specific
           :artifact-only :richer-profile-artifacts})
        standard-profile-order))