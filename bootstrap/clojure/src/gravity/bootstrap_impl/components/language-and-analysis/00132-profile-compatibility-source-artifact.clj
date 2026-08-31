

(defn profile-compatibility-source-artifact
  [source-path source-text]
  (let [{:keys [module dependencies]} (p13-source-context source-path source-text)
        dependency-records (vec (keep #(p13-validate-dependency!
                                        source-path module %)
                                      dependencies))
        artifact-records (mapv #(p13-artifact-record source-path module %)
                               (p13-artifact-boundaries module))
        standard-library-facades (p13-standard-library-facades module)
        manifest-artifact (profile-manifest-source-artifact source-path
                                                            source-text)
        conformance (p13-conformance-results dependency-records
                                             artifact-records
                                             standard-library-facades)
        graph {:consumer {:namespace (:module module)
                          :profile (:profile module)}
               :edges (vec (concat dependency-records artifact-records))
               :status :complete}]
    {:kind :gravity/stage0-profile-compatibility-artifact
     :document "P13"
     :pass {:name :profile-compatibility-validation
            :input :profile-manifest
            :output :cross-profile-dependency-graph
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check
                       :profile-manifest-validation]
            :preserves [:source-spans :generated-origin :profile :target
                        :effects :capabilities :facade-evidence
                        :artifact-boundaries]
            :emits [:profile-compatibility-matrix
                    :cross-profile-dependency-graph :facade-manifest
                    :artifact-boundary-manifest :evidence-records
                    :compatibility-conformance-results]
            :rejects p13-diagnostic-ids}
     :profile-manifest-artifact-hash (str "sha256:"
                                          (sha256-hex
                                           (pr-str manifest-artifact)))
     :profile-manifest (:profile-manifest manifest-artifact)
     :profile-compatibility-matrix (p13-compatibility-matrix)
     :cross-profile-dependency-graph graph
     :facade-manifest (filterv #(= :facade-required (:edge %))
                               dependency-records)
     :artifact-boundary-manifest artifact-records
     :evidence-records (mapv #(select-keys % [:producer :edge :evidence
                                              :facade :artifact :status])
                             (concat dependency-records artifact-records))
     :capability-based-proof (p13-capability-proof dependency-records
                                                   artifact-records)
     :compatibility-conformance-results conformance
     :diagnostics []}))

(def profile-compliance-fixture-root
  "bootstrap/clojure/fixtures")

(def profile-compliance-required-documents
  ["P1" "P2" "P3" "P4" "P5" "P6" "P7" "P8" "P9" "P10" "P11" "P12" "P13"])

(def profile-compliance-required-diagnostic-ids
  (vec (concat p1-diagnostic-ids
               profile-set-diagnostic-ids
               constrained-profile-diagnostic-ids
               distributed-ai-diagnostic-ids
               p13-diagnostic-ids)))

(def profile-compliance-accepted-fixtures
  [{:fixture "profile-accepted-core.gravity"
    :profile :core
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-meta.gravity"
    :profile :meta
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-hosted.gravity"
    :profile :hosted
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-native.gravity"
    :profile :native
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-firmware.gravity"
    :profile :firmware
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-kernel.gravity"
    :profile :kernel
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-hardware.gravity"
    :profile :hardware
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-distributed.gravity"
    :profile :distributed
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-ai.gravity"
    :profile :ai
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-gpu.gravity"
    :profile :gpu
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-accepted-formal.gravity"
    :profile :formal
    :document "P1"
    :stage :profile-manifest}
   {:fixture "profile-set-core.gravity"
    :profile :core
    :document "P2"
    :stage :profile-set}
   {:fixture "profile-set-meta.gravity"
    :profile :meta
    :document "P3"
    :stage :profile-set}
   {:fixture "profile-set-hosted.gravity"
    :profile :hosted
    :document "P4"
    :stage :profile-set}
   {:fixture "profile-set-native.gravity"
    :profile :native
    :document "P5"
    :stage :profile-set}
   {:fixture "profile-validation-firmware.gravity"
    :profile :firmware
    :document "P6"
    :stage :profile-validation}
   {:fixture "profile-validation-kernel.gravity"
    :profile :kernel
    :document "P7"
    :stage :profile-validation}
   {:fixture "profile-validation-hardware.gravity"
    :profile :hardware
    :document "P8"
    :stage :profile-validation}
   {:fixture "profile-distributed-ai-distributed.gravity"
    :profile :distributed
    :document "P9"
    :stage :profile-distributed-ai}
   {:fixture "profile-distributed-ai-ai.gravity"
    :profile :ai
    :document "P10"
    :stage :profile-distributed-ai}
   {:fixture "profile-validation-gpu.gravity"
    :profile :gpu
    :document "P11"
    :stage :profile-validation}
   {:fixture "profile-validation-formal.gravity"
    :profile :formal
    :document "P12"
    :stage :profile-validation}
   {:fixture "profile-compatibility-matrix.gravity"
    :profile :kernel
    :document "P13"
    :stage :profile-compatibility}])

(defn profile-compliance-fixture-dir
  [category]
  (str profile-compliance-fixture-root "/" (name category)))

(defn profile-compliance-fixture-path
  [category fixture-name]
  (str (profile-compliance-fixture-dir category) "/" fixture-name))

(defn profile-compliance-run-artifact
  [stage source-path]
  (let [source-text (slurp source-path)]
    (case stage
      :profile-manifest (profile-manifest-source-artifact source-path source-text)
      :profile-set (profile-set-source-artifact source-path source-text)
      :profile-validation (constrained-profile-source-artifact source-path
                                                               source-text)
      :profile-distributed-ai (distributed-ai-profile-source-artifact
                               source-path source-text)
      :profile-compatibility (profile-compatibility-source-artifact
                              source-path source-text))))

(defn profile-compliance-rejected-stage
  [fixture-name]
  (cond
    (str/starts-with? fixture-name "profile-compatibility-")
    :profile-compatibility

    (or (str/starts-with? fixture-name "profile-distributed-")
        (str/starts-with? fixture-name "profile-ai-"))
    :profile-distributed-ai

    (or (str/starts-with? fixture-name "profile-firmware-")
        (str/starts-with? fixture-name "profile-kernel-")
        (str/starts-with? fixture-name "profile-hardware-")
        (str/starts-with? fixture-name "profile-gpu-")
        (str/starts-with? fixture-name "profile-formal-"))
    :profile-validation

    (or (str/starts-with? fixture-name "profile-core-")
        (str/starts-with? fixture-name "profile-meta-")
        (str/starts-with? fixture-name "profile-hosted-")
        (str/starts-with? fixture-name "profile-native-"))
    :profile-set

    :else
    :profile-manifest))

(defn profile-compliance-rejected-fixture-paths
  []
  (let [dir (java.io.File. (profile-compliance-fixture-dir :rejected))]
    (->> (file-seq dir)
         (filter #(.isFile %))
         (map #(.getPath %))
         (filter #(str/ends-with? % ".gravity"))
         (filter #(str/starts-with? (.getName (java.io.File. %)) "profile-"))
         sort
         vec)))

(defn profile-compliance-artifact-profile
  [artifact]
  (or (get-in artifact [:profile-manifest :profile])
      (get-in artifact [:module :profile])
      (get-in artifact [:profile-validation-report :profile])))

(defn profile-compliance-active-document
  [artifact]
  (or (:document artifact)
      (get-in artifact [:profile-specific-report :document])
      (get-in artifact [:profile-validation-report :document])))