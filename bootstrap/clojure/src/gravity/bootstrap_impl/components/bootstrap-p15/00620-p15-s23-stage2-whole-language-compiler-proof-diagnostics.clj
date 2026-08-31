

(defn p15-s23-stage2-whole-language-compiler-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        source-record (:source-record candidate)
        stage-record (:stage-record candidate)
        accepted-record (:accepted-record candidate)
        rejected-record (:rejected-record candidate)
        evidence-link-record (:evidence-link-record candidate)
        boundary-record (:boundary-record candidate)
        lineage-record (:lineage-record candidate)
        claims (:self-hosting-claims proof-contract)]
    (vec
     (concat
      (when-not (= :gravity/stage2-whole-language-compiler
                   (:artifact proof-contract))
        [(p15-s23-stage2-whole-language-compiler-diagnostic-record
          source-path "P15S23Z001" proof-contract
          {:missing-fields [:artifact]})])
      (when-not (= :complete (:status source-record))
        [(p15-s23-stage2-whole-language-compiler-diagnostic-record
          source-path "P15S23Z002" source-record
          {:required-components
           (get-in proof-contract
                   [:claimed-language-subset
                    :compiler-source-components])})])
      (when-not (and (= :complete (:status stage-record))
                     (= :complete (:status lineage-record)))
        [(p15-s23-stage2-whole-language-compiler-diagnostic-record
          source-path "P15S23Z003"
          {:stage-record stage-record
           :lineage-record lineage-record}
          {:required [:stage2-driver
                      :stage2-runtime-kernel
                      :gravity-source-lineage]})])
      (when-not (and (= :complete (:status accepted-record))
                     (true?
                      (:stage2-output-equivalent-to-current-stage?
                       accepted-record))
                     (true? (:stage2-runtime-executed?
                             accepted-record)))
        [(p15-s23-stage2-whole-language-compiler-diagnostic-record
          source-path "P15S23Z004" accepted-record
          {:required [:accepted-output-equivalent
                      :stage2-runtime-executed]})])
      (when-not (and (= :complete (:status rejected-record))
                     (true?
                      (:diagnostics-equivalent-to-current-stage?
                       rejected-record))
                     (true? (:diagnostic-codes-stable?
                             rejected-record)))
        [(p15-s23-stage2-whole-language-compiler-diagnostic-record
          source-path "P15S23Z005" rejected-record
          {:required [:rejected-fixtures-fail-closed
                      :diagnostics-equivalent-to-current-stage]})])
      (when-not (and (= :complete (:status evidence-link-record))
                     (true?
                      (:required-links-covered? evidence-link-record))
                     (true? (:all-artifacts-identified?
                             evidence-link-record)))
        [(p15-s23-stage2-whole-language-compiler-diagnostic-record
          source-path "P15S23Z006" evidence-link-record
          {:required-links
           (vec (sort
                 p15-s23-stage2-whole-language-compiler-required-links))
           :required-emits
           (vec (sort
                 p15-s23-stage2-whole-language-compiler-required-emits))
           :required-preserves
           (vec (sort
                 p15-s23-stage2-whole-language-compiler-required-preserves))})])
      (when-not (and (= :complete (:status boundary-record))
                     (true? (:stage2-compiler-driver-executed?
                             boundary-record))
                     (true? (:clojure-stage0-verifier?
                             boundary-record))
                     (true? (:clojure-stage0-release-compiler?
                             boundary-record))
                     (false? (:clojure-stage0-runtime-host?
                              boundary-record))
                     (false? (:clojure-host-primitive-boundary?
                              boundary-record))
                     (true? (:gravity-runtime-primitives?
                             boundary-record)))
        [(p15-s23-stage2-whole-language-compiler-diagnostic-record
          source-path "P15S23Z007" boundary-record
          {:required [:stage2-driver-executed
                      :stage2-runtime-kernel
                      :clojure-stage0-verifier-recorded
                      :clojure-stage0-release-compiler-recorded]})])
      (when (or (true? (:full-language-compiler-self-hosted?
                       claims))
                (true? (:clojure-seed-retired? claims))
                (true? (:full-language-compiler-self-hosted?
                       boundary-record))
                (true? (:clojure-seed-retired?
                       boundary-record)))
        [(p15-s23-stage2-whole-language-compiler-diagnostic-record
          source-path "P15S23Z008"
          {:claims claims
           :boundary-record boundary-record}
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-stage2-whole-language-compiler-diagnostic-stream
  [source-path proof-id]
  {:artifact
   :gravity/p15-s23-stage2-whole-language-compiler-diagnostic-stream
   :stage :p15-s23-stage2-whole-language-compiler
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-stage2-whole-language-compiler
            :message
            (get p15-s23-stage2-whole-language-compiler-diagnostic-messages
                 id)})
         p15-s23-stage2-whole-language-compiler-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage2-whole-language-compiler-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-stage2-whole-language-compiler-missing-contract
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23Z001"}
   {:fixture :internal-p15-s23-stage2-whole-language-compiler-source-gap
    :candidate
    (assoc-in accepted-candidate [:source-record :status] :failed)
    :expected-diagnostic "P15S23Z002"}
   {:fixture :internal-p15-s23-stage2-whole-language-compiler-driver-gap
    :candidate
    (-> accepted-candidate
        (assoc-in [:stage-record :status] :failed)
        (assoc-in [:lineage-record :status] :failed))
    :expected-diagnostic "P15S23Z003"}
   {:fixture :internal-p15-s23-stage2-whole-language-compiler-output-gap
    :candidate
    (-> accepted-candidate
        (assoc-in [:accepted-record
                   :stage2-output-equivalent-to-current-stage?]
                  false)
        (assoc-in [:accepted-record :status] :failed))
    :expected-diagnostic "P15S23Z004"}
   {:fixture :internal-p15-s23-stage2-whole-language-compiler-rejected-gap
    :candidate
    (-> accepted-candidate
        (assoc-in [:rejected-record
                   :diagnostics-equivalent-to-current-stage?]
                  false)
        (assoc-in [:rejected-record :status] :failed))
    :expected-diagnostic "P15S23Z005"}
   {:fixture :internal-p15-s23-stage2-whole-language-compiler-evidence-gap
    :candidate
    (-> accepted-candidate
        (assoc-in [:evidence-link-record :required-links-covered?]
                  false)
        (assoc-in [:evidence-link-record :status] :failed))
    :expected-diagnostic "P15S23Z006"}
   {:fixture :internal-p15-s23-stage2-whole-language-compiler-boundary-gap
    :candidate
    (-> accepted-candidate
        (assoc-in [:boundary-record :clojure-stage0-verifier?]
                  false)
        (assoc-in [:boundary-record :status] :failed))
    :expected-diagnostic "P15S23Z007"}
   {:fixture :internal-p15-s23-stage2-whole-language-compiler-overclaim
    :candidate
    (-> accepted-candidate
        (assoc-in [:proof-contract :self-hosting-claims
                   :full-language-compiler-self-hosted?]
                  true)
        (assoc-in [:proof-contract :self-hosting-claims
                   :clojure-seed-retired?]
                  true)
        (assoc-in [:boundary-record
                   :full-language-compiler-self-hosted?]
                  true)
        (assoc-in [:boundary-record :clojure-seed-retired?]
                  true))
    :expected-diagnostic "P15S23Z008"}])

(defn p15-s23-stage2-whole-language-compiler-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-stage2-whole-language-compiler-proof-diagnostics
            source-path candidate)})
        (p15-s23-stage2-whole-language-compiler-rejected-candidates
         accepted-candidate)))