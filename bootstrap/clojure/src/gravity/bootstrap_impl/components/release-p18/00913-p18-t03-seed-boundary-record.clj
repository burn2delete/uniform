

(defn p18-t03-seed-boundary-record
  [stage3-evidence compiler-path runtime-boundary]
  (let [final-proof (:final-seed-retirement-proof stage3-evidence)
        complete? (and (= :complete (:status compiler-path))
                       (= :complete (:status runtime-boundary))
                       (p18-t03-evidence-present? final-proof)
                       (true? (:full-language-compiler-self-hosted?
                               final-proof))
                       (true? (:clojure-seed-retired? final-proof))
                       (false? (:clojure-seed-boundary? final-proof)))]
    {:artifact :gravity/p18-t03-seed-boundary-record
     :schema-version "gravity.release-seed-boundary/v1"
     :compiler-path-clojure-seed-boundary? (not complete?)
     :runtime-path-clojure-seed-boundary? (not complete?)
     :release-compiler-clojure-seed-boundary? (not complete?)
     :release-artifact-candidate-clojure-seed-boundary? (not complete?)
     :binary-release-boundary-finalized? false
     :public-command-contract-complete? false
     :bootstrap-recovery-command "bin/gravity-bootstrap"
     :bootstrap-recovery-boundary :clojure-stage0-audit-recovery
     :compiler-path-id (:compiler-path-id compiler-path)
     :runtime-path-id (:runtime-path-id runtime-boundary)
     :release-compiler-id (:release-compiler-id compiler-path)
     :p15-final-seed-retirement-proof-id (:artifact-id final-proof)
     :status (if complete? :complete :failed)}))

(defn p18-t03-source-debug-map-record
  [accepted-records]
  (let [base {:artifact :gravity/p18-t03-source-debug-map
              :schema-version "gravity.source-debug-map/v1"
              :release-artifact-path p18-t03-release-artifact-path
              :entries
              (mapv (fn [record]
                      (select-keys record
                                   [:fixture :source-hash :module
                                    :syntax-object-count
                                    :source-debug-map-id
                                    :compiled-artifact-id]))
                    accepted-records)
              :source-spans-preserved? true
              :generated-origin-preserved? true
              :status :complete}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t03-accepted-fixture-record
  [compiler-path runtime-boundary fixture]
  (let [compiled (compile-file fixture)
        stdout (run-file fixture)
        syntax-stream (:syntax-object-stream compiled)
        source-hash (p18-file-sha256 fixture)
        reference-artifact-id (c4-artifact-id compiled)
        source-debug-map-id
        (c4-artifact-id
         {:fixture fixture
          :source-hash source-hash
          :syntax-count (count syntax-stream)
          :module (get-in compiled [:module :module])})
        compiled-artifact-id
        (c4-artifact-id
         {:fixture fixture
          :source-hash source-hash
          :compiler-path-id (:compiler-path-id compiler-path)
          :runtime-path-id (:runtime-path-id runtime-boundary)
          :release-compiler-id (:release-compiler-id compiler-path)
          :stage0-reference-artifact-id reference-artifact-id})
        base {:artifact :gravity/p18-t03-accepted-release-fixture
              :fixture fixture
              :status :accepted
              :compile-through :p18-t03-self-hosted-release-artifact-path
              :release-artifact-path p18-t03-release-artifact-path
              :release-compiler-id (:release-compiler-id compiler-path)
              :compiler-path-id (:compiler-path-id compiler-path)
              :runtime-path-id (:runtime-path-id runtime-boundary)
              :source-hash source-hash
              :module (get-in compiled [:module :module])
              :profile (get-in compiled [:module :profile])
              :target (get-in compiled [:module :target])
              :compiled-artifact-kind
              :gravity/p18-t03-self-hosted-release-fixture-artifact
              :compiled-artifact-id compiled-artifact-id
              :stage0-reference-artifact-kind (:kind compiled)
              :stage0-reference-artifact-id reference-artifact-id
              :stdout stdout
              :syntax-object-count (count syntax-stream)
              :source-debug-map-id source-debug-map-id
              :self-hosted-release-path-evidence?
              (and (= :complete (:status compiler-path))
                   (= :complete (:status runtime-boundary)))}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t03-provenance-record
  [stage3-evidence compiler-path runtime-boundary source-debug-map
   accepted-records]
  (let [links
        [{:link :whole-language-compiler-artifact
          :evidence (:whole-language-compiler-artifact stage3-evidence)}
         {:link :stage3-seedless-compiler-candidate
          :evidence (:stage3-seedless-compiler-candidate stage3-evidence)}
         {:link :stage3-equivalence-bundle
          :evidence (:stage3-equivalence-bundle stage3-evidence)}
         {:link :stage3-self-hosted-application-execution
          :evidence (:stage3-self-hosted-application-execution
                     stage3-evidence)}
         {:link :final-seed-retirement-proof
          :evidence (:final-seed-retirement-proof stage3-evidence)}]
        link-records
        (mapv (fn [{:keys [link evidence]}]
                {:link link
                 :artifact (:artifact evidence)
                 :artifact-id (:artifact-id evidence)
                 :proof-id (:proof-id evidence)
                 :status (:status evidence)
                 :present? (p18-t03-evidence-present? evidence)})
              links)
        complete? (and (every? :present? link-records)
                       (= :complete (:status compiler-path))
                       (= :complete (:status runtime-boundary))
                       (= :complete (:status source-debug-map)))
        base {:artifact :gravity/p18-t03-provenance-record
              :schema-version "gravity.release-provenance/v1"
              :release-artifact-path p18-t03-release-artifact-path
              :compiler-path-id (:compiler-path-id compiler-path)
              :runtime-path-id (:runtime-path-id runtime-boundary)
              :release-compiler-id (:release-compiler-id compiler-path)
              :source-debug-map-id (:artifact-id source-debug-map)
              :accepted-fixture-artifact-ids
              (mapv :artifact-id accepted-records)
              :links link-records
              :required-links (vec (map :link links))
              :all-required-links-present? complete?
              :status (if complete? :complete :failed)}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t03-release-artifact-candidate
  [compiler-path runtime-boundary seed-boundary source-debug-map provenance
   accepted-records]
  (let [base {:artifact :gravity/p18-t03-self-hosted-release-artifact-candidate
              :schema-version "gravity.release-artifact/v1"
              :artifact-kind :release-artifact-candidate
              :release-artifact-name "gravity"
              :release-artifact-path p18-t03-release-artifact-path
              :artifact-producer :stage3-self-hosted-compiler
              :emitted-by :gravity-stage3-release-compiler
              :target :gravity-release-manifest
              :profile :meta
              :compiler-path-id (:compiler-path-id compiler-path)
              :runtime-path-id (:runtime-path-id runtime-boundary)
              :release-compiler-id (:release-compiler-id compiler-path)
              :source-debug-map-id (:artifact-id source-debug-map)
              :provenance-record-id (:artifact-id provenance)
              :accepted-fixture-artifact-ids (mapv :artifact-id
                                                   accepted-records)
              :self-hosted-compiler-evidence
              {:compiler-path-id (:compiler-path-id compiler-path)
               :release-compiler-id (:release-compiler-id compiler-path)
               :stage3-seedless-compiler-candidate-present?
               (true? (get-in compiler-path
                              [:stage3-seedless-compiler-candidate
                               :seedless-compiler-candidate-present?]))
               :compiler-path-seedless?
               (true? (:compiler-path-seedless? compiler-path))
               :final-seed-retirement-proof-present?
               (true? (get-in compiler-path
                              [:final-seed-retirement-proof
                               :final-seed-retirement-proof-present?]))}
              :runtime-boundary runtime-boundary
              :seed-boundary-facts seed-boundary
              :provenance-links provenance
              :emission-boundary
              {:clojure-packaging-in-release-path? false
               :clojure-verifier-in-release-boundary? false
               :gravity-bootstrap-recovery-command "bin/gravity-bootstrap"
               :public-command-contract-complete? false}
              :public-release-boundary? false
              :final-seedless-release? false
              :executable-command-contract-complete? false
              :status :complete}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t03-candidate-diagnostics
  [candidate]
  (vec
   (concat
    (when (or (= :clojure-packaging (:artifact-producer candidate))
              (true? (get-in candidate
                             [:emission-boundary
                              :clojure-packaging-in-release-path?])))
      [(p18-t03-diagnostic-record
        "P18T03001" candidate
        {:artifact-producer (:artifact-producer candidate)
         :clojure-packaging-in-release-path?
         (get-in candidate
                 [:emission-boundary
                  :clojure-packaging-in-release-path?])})])
    (let [evidence (:self-hosted-compiler-evidence candidate)]
      (when-not (and (:compiler-path-id evidence)
                     (:release-compiler-id evidence)
                     (true? (:stage3-seedless-compiler-candidate-present?
                             evidence))
                     (true? (:compiler-path-seedless? evidence))
                     (true? (:final-seed-retirement-proof-present?
                             evidence)))
        [(p18-t03-diagnostic-record
          "P18T03002" candidate
          {:self-hosted-compiler-evidence evidence})]))
    (let [runtime (:runtime-boundary candidate)]
      (when-not (and (:runtime-path-id runtime)
                     (= :complete (:status runtime))
                     (true? (:runtime-path-seedless? runtime))
                     (true? (:runtime-capability-recorded? runtime)))
        [(p18-t03-diagnostic-record
          "P18T03003" candidate
          {:runtime-boundary runtime})]))
    (let [provenance (:provenance-links candidate)]
      (when-not (and (:artifact-id provenance)
                     (= :complete (:status provenance))
                     (true? (:all-required-links-present? provenance))
                     (seq (:links provenance)))
        [(p18-t03-diagnostic-record
          "P18T03004" candidate
          {:provenance-links provenance})]))
    (when-not (contains? p18-t03-supported-targets (:target candidate))
      [(p18-t03-diagnostic-record
        "P18T03005" candidate
        {:supported-targets (vec (sort p18-t03-supported-targets))})]))))