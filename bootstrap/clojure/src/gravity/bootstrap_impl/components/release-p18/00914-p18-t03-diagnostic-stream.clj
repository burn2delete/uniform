

(defn p18-t03-diagnostic-stream
  [proof-id]
  {:artifact :gravity/p18-t03-diagnostic-stream
   :stage :p18-t03-self-hosted-release-artifact
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p18-t03-self-hosted-release-artifact
            :message (get p18-t03-diagnostic-messages id)
            :stable? true})
         p18-t03-diagnostic-ids)
   :status :complete})

(defn p18-t03-rejected-fixture-records
  [candidate]
  (let [records
        [{:fixture :internal-p18-t03-clojure-packaging-release-path
          :expected-diagnostic "P18T03001"
          :candidate
          (-> candidate
              (assoc :artifact-producer :clojure-packaging)
              (assoc-in [:emission-boundary
                         :clojure-packaging-in-release-path?]
                        true))}
         {:fixture :internal-p18-t03-missing-self-hosted-compiler-evidence
          :expected-diagnostic "P18T03002"
          :candidate (dissoc candidate :self-hosted-compiler-evidence)}
         {:fixture :internal-p18-t03-missing-runtime-boundary-evidence
          :expected-diagnostic "P18T03003"
          :candidate
          (-> candidate
              (assoc-in [:runtime-boundary :status] :failed)
              (assoc-in [:runtime-boundary :runtime-path-seedless?]
                        false))}
         {:fixture :internal-p18-t03-missing-artifact-provenance
          :expected-diagnostic "P18T03004"
          :candidate
          (-> candidate
              (assoc-in [:provenance-links :status] :failed)
              (assoc-in [:provenance-links
                         :all-required-links-present?]
                        false))}
         {:fixture :internal-p18-t03-unsupported-target-claim
          :expected-diagnostic "P18T03005"
          :candidate (assoc candidate :target :native-linux-x86_64)}]]
    (mapv
     (fn [{:keys [fixture expected-diagnostic candidate]}]
       (let [diagnostics (p18-t03-candidate-diagnostics candidate)
             observed (set (map :diagnostic diagnostics))]
         {:fixture fixture
          :status :rejected
          :expected-diagnostic expected-diagnostic
          :diagnostics diagnostics
          :matches-expected? (contains? observed expected-diagnostic)}))
     records)))

(defn p18-t03-self-hosted-release-artifact-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (:diagnostics artifact)))
        full-diagnostic-stream
        (set (map :diagnostic
                  (get-in artifact
                          [:p18-t03-diagnostic-stream :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-fixtures artifact)))
        accepted-paths (set (map :fixture (:accepted-fixtures artifact)))]
    {:task "P18-T03"
     :status (:status artifact)
     :release-artifact-candidate-emitted?
     (.isFile (java.io.File. p18-t03-release-artifact-path))
     :release-artifact-id (:artifact-id (:release-artifact-candidate
                                         artifact))
     :release-artifact-candidate-complete?
     (= :complete (get-in artifact [:release-artifact-candidate :status]))
     :candidate-diagnostics-covered?
     (set/subset? diagnostics (set p18-t03-diagnostic-ids))
     :final-seed-retirement-proof-present?
     (true? (get-in artifact
                    [:release-artifact-candidate
                     :self-hosted-compiler-evidence
                     :final-seed-retirement-proof-present?]))
     :compiler-path-id (get-in artifact
                               [:compiler-path-record :compiler-path-id])
     :runtime-path-id (get-in artifact
                              [:runtime-boundary-record :runtime-path-id])
     :release-compiler-id
     (get-in artifact [:compiler-path-record :release-compiler-id])
     :self-hosted-compiler-path-linked?
     (= :complete (get-in artifact [:compiler-path-record :status]))
     :runtime-boundary-recorded?
     (= :complete (get-in artifact [:runtime-boundary-record :status]))
     :provenance-recorded?
     (= :complete (get-in artifact [:provenance-record :status]))
     :source-debug-map-recorded?
     (= :complete (get-in artifact [:source-debug-map :status]))
     :seed-boundary-recorded?
     (= :complete (get-in artifact [:seed-boundary-record :status]))
     :clojure-packaging-excluded?
     (false? (get-in artifact
                     [:release-artifact-candidate :emission-boundary
                      :clojure-packaging-in-release-path?]))
     :accepted-fixtures-covered?
     (= (set p18-t03-accepted-fixtures) accepted-paths)
     :accepted-fixtures-self-hosted-path?
     (every? true?
             (map :self-hosted-release-path-evidence?
                  (:accepted-fixtures artifact)))
     :rejected-candidates-covered?
     (= (set p18-t03-diagnostic-ids) rejected-diagnostics)
     :diagnostics-covered?
     (= (set p18-t03-diagnostic-ids) full-diagnostic-stream)
     :does-not-complete-final-command-contract?
     (false? (get-in artifact
                     [:release-artifact-candidate
                      :executable-command-contract-complete?]))
     :does-not-claim-final-seedless-release?
     (false? (get-in artifact
                     [:release-artifact-candidate
                      :final-seedless-release?]))
     :next-required-capability
     (if (= :complete (:status artifact))
       :p18-t04-executable-command-contract
       :p15-s23-final-seed-retirement)}))

(defn p18-t03-self-hosted-release-artifact!
  ([]
   (p18-t03-self-hosted-release-artifact! p18-t03-compiler-source))
  ([compiler-source]
   (let [stage3-evidence (p18-t03-stage3-evidence)
         compiler-path (p18-t03-compiler-path-record stage3-evidence)
         runtime-boundary
         (p18-t03-runtime-boundary-record stage3-evidence compiler-path)
         seed-boundary
         (p18-t03-seed-boundary-record stage3-evidence compiler-path
                                       runtime-boundary)
         accepted-records
         (mapv #(p18-t03-accepted-fixture-record compiler-path
                                                 runtime-boundary %)
               p18-t03-accepted-fixtures)
         source-debug-map
         (p18-t03-source-debug-map-record accepted-records)
         provenance
         (p18-t03-provenance-record stage3-evidence compiler-path
                                    runtime-boundary source-debug-map
                                    accepted-records)
         candidate-base
         (p18-t03-release-artifact-candidate
          compiler-path runtime-boundary seed-boundary source-debug-map
          provenance accepted-records)
         diagnostics (p18-t03-candidate-diagnostics candidate-base)
         candidate-status (if (seq diagnostics) :incomplete :complete)
         candidate-with-status
         (assoc candidate-base
                :status candidate-status
                :diagnostics diagnostics)
         candidate
         (assoc candidate-with-status
                :artifact-id
                (c4-artifact-id
                 (dissoc candidate-with-status :artifact-id)))
         _ (p18-t02-write-edn! p18-t03-release-artifact-path candidate)
         proof-id
         (c4-artifact-id
          {:compiler-source compiler-source
           :candidate-id (:artifact-id candidate)
           :compiler-path-id (:compiler-path-id compiler-path)
           :runtime-path-id (:runtime-path-id runtime-boundary)
           :accepted-fixture-ids (mapv :artifact-id accepted-records)})
         rejected-records (p18-t03-rejected-fixture-records candidate)
         artifact-base
         {:kind :gravity/p18-t03-self-hosted-release-artifact-proof
          :task "P18-T03"
          :status candidate-status
          :phase :binary-distribution-and-seedless-release
          :compiler-source compiler-source
          :release-artifact-candidate candidate
          :release-artifact-path p18-t03-release-artifact-path
          :release-artifact-id (:artifact-id candidate)
          :compiler-path-record compiler-path
          :runtime-boundary-record runtime-boundary
          :seed-boundary-record seed-boundary
          :source-debug-map source-debug-map
          :provenance-record provenance
          :accepted-fixtures accepted-records
          :rejected-fixtures rejected-records
          :p18-t03-diagnostic-stream
          (p18-t03-diagnostic-stream proof-id)
          :p18-t03-results
          {:accepted-fixtures (count accepted-records)
           :rejected-fixtures (count rejected-records)
           :diagnostic-count (count p18-t03-diagnostic-ids)
           :release-artifact-candidate-emitted? true
           :self-hosted-compiler-path-linked?
           (= :complete (:status compiler-path))
           :runtime-boundary-recorded?
           (= :complete (:status runtime-boundary))
           :final-executable-command-contract-complete? false
           :status candidate-status}
          :diagnostics diagnostics}
         proof (p18-t03-self-hosted-release-artifact-proof artifact-base)]
     (assoc artifact-base
            :capability-based-proof proof
            :artifact-id
            (c4-artifact-id
             (assoc artifact-base :capability-based-proof proof))))))