(defn p15-s23-current-seed-candidate
  [formal-artifact]
  (let [source-evidence (semantic-mid-current-seed-evidence)
        candidate-evidence
        (semantic-mid-current-seed-candidate-evidence
         formal-artifact source-evidence)
        final-proof-input-evidence
        (semantic-mid-current-seed-final-proof-input source-evidence)
        final-seed-retirement-evidence
        (semantic-mid-current-seed-final-evidence)
        final-seed-retirement-complete?
        (semantic-mid-current-seed-final-complete?
         final-seed-retirement-evidence)]
    {:candidate-id
     (if final-seed-retirement-complete?
       :p15-s23-final-seed-retirement
       :stage1-reader-formal-release-governance-seed-retirement)
     :basis-artifact-id (:artifact-id formal-artifact)
     :basis-task (:task formal-artifact)
     :basis-kind (:kind formal-artifact)
     :scope (if final-seed-retirement-complete?
              :whole-language-compiler
              :stage1-reader-claimed-subset)
     :claimed-subset-self-hosted?
     (get-in formal-artifact
             [:self-hosting-evidence :claimed-subset-self-hosted?])
     :full-language-compiler-self-hosted? final-seed-retirement-complete?
     :clojure-seed-retired? final-seed-retirement-complete?
     :clojure-seed-boundary? (not final-seed-retirement-complete?)
     :seed-boundary
     (if final-seed-retirement-complete?
       :retired-by-p15-s23-final-seed-retirement-proof
       :clojure-stage0-bootstrap)
     :evidence
     (cond-> candidate-evidence
       final-seed-retirement-evidence
       (assoc :final-seed-retirement-proof final-seed-retirement-evidence)
       final-seed-retirement-complete?
       (assoc :clojure-seed-retired final-seed-retirement-evidence))}))

(doseq [helper '[semantic-mid-current-seed-evidence
                 semantic-mid-assoc-present-evidence
                 semantic-mid-current-seed-candidate-evidence
                 semantic-mid-current-seed-final-proof-input
                 semantic-mid-current-seed-final-evidence
                 semantic-mid-current-seed-final-complete?]]
  (ns-unmap *ns* helper))
