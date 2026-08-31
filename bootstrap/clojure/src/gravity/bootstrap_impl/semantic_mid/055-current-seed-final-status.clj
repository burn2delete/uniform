(defn- semantic-mid-current-seed-final-evidence
  []
  {:status :incomplete
   :artifact :gravity/p15-s23-final-seed-retirement-proof-artifact
   :source-path "bootstrap/gravity/p15_s23/compiler.gravity"
   :final-seed-retirement-proof-present? false
   :evidence-links-covered? false
   :stage3-seedless-boundary-proven? false
   :stage3-equivalence-and-application-proven? false
   :release-governance-closed? false
   :tcb-seed-boundary-retired? false
   :provenance-closure-recorded? false
   :full-language-compiler-self-hosted? false
   :clojure-seed-retired? false
   :clojure-seed-boundary? true
   :next-required-capability
   :self_hosted_public_binary_final_verification})

(defn- semantic-mid-current-seed-final-complete?
  [final-seed-retirement-evidence]
  (and (p15-s23-evidence-present? final-seed-retirement-evidence)
       (true? (:final-seed-retirement-proof-present?
               final-seed-retirement-evidence))
       (true? (:evidence-links-covered? final-seed-retirement-evidence))
       (true? (:stage3-seedless-boundary-proven?
               final-seed-retirement-evidence))
       (true? (:stage3-equivalence-and-application-proven?
               final-seed-retirement-evidence))
       (true? (:release-governance-closed?
               final-seed-retirement-evidence))
       (true? (:tcb-seed-boundary-retired?
               final-seed-retirement-evidence))
       (true? (:provenance-closure-recorded?
               final-seed-retirement-evidence))
       (true? (:full-language-compiler-self-hosted?
               final-seed-retirement-evidence))
       (true? (:clojure-seed-retired? final-seed-retirement-evidence))
       (false? (:clojure-seed-boundary?
                final-seed-retirement-evidence))))
