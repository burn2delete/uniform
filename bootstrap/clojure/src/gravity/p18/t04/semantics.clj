(ns gravity.p18.t04.semantics
  "Pure P18-T04 self-host verification projections.

  Proof acquisition, diagnostics, and publication remain bootstrap-owned.")

(defn compiler-source
  [{:keys [source-path source-extension source-kind source-exists?
           source-extensions]}]
  {:path source-path
   :extension (source-extension source-path)
   :source-kind (source-kind source-path)
   :exists? (source-exists? source-path)
   :co-canonical-source-extensions (vec (sort source-extensions))
   :deprecation-warning? false
   :legacy-alias? false})

(defn complete?
  [p15-final-proof p18-final-proof]
  (and (= :complete (:status p15-final-proof))
       (true? (:full-language-compiler-self-hosted? p15-final-proof))
       (true? (:clojure-seed-retired? p15-final-proof))
       (false? (:clojure-seed-boundary? p15-final-proof))
       (= :complete (:status p18-final-proof))
       (true? (:final-release? p18-final-proof))
       (true? (:seedless-release? p18-final-proof))
       (false? (:clojure-seed-boundary? p18-final-proof))
       (false? (get-in p18-final-proof
                       [:capability-based-proof :clojure-seed-boundary?]))))

(defn proof
  [{:keys [artifact complete? source-path source-extension
           p15-final-proof p18-final-proof diagnostics]}]
  (let [diagnostic-ids (set (map :diagnostic diagnostics))]
    {:task "P18-T04"
     :status (:status artifact)
     :public-self-host-verify-command-present? true
     :final-self-host-verification? complete?
     :p15-final-seed-retirement-proof-linked? (boolean (:artifact-id p15-final-proof))
     :p18-final-release-proof-linked? (boolean (:artifact-id p18-final-proof))
     :full-language-compiler-self-hosted? (true? (:full-language-compiler-self-hosted? p15-final-proof))
     :clojure-seed-retired? (true? (:clojure-seed-retired? p15-final-proof))
     :clojure-seed-boundary? (or (true? (:clojure-seed-boundary? p15-final-proof))
                                 (true? (:clojure-seed-boundary? p18-final-proof)))
     :fails-closed-while-seed-boundary-active?
     (if complete? false (contains? diagnostic-ids "P18T04007"))
     :source-path-preserved? (= source-path (get-in artifact [:compiler-source :path]))
     :source-extension-preserved? (= (source-extension source-path)
                                     (get-in artifact [:compiler-source :extension]))
     :no-extension-deprecation-warning?
     (false? (get-in artifact [:compiler-source :deprecation-warning?]))
     :bootstrap-hosted? true
     :full-language-conformance? false
     :self-hosted-conformance-runner? false
     :next-required-capability (if complete? :none :self_hosted_public_binary_final_verification)}))
