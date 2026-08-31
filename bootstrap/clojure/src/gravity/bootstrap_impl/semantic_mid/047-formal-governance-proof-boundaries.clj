(defn- semantic-mid-formal-governance-proof-boundaries
  [{:keys [artifact gravity-runtimes gravity-executors character-stream
           token-stream records diagnostics]}]
  {:host-primitive-boundary-empty? (= [] (:host-primitives artifact))
   :host-fallbacks-empty?
   (= [] (get-in artifact
                 [:stage1-reader-core-bootstrap-builtins :host-fallbacks]))
   :seed-builtin-fallbacks-empty?
   (= [] (:seed-builtin-fallbacks artifact))
   :seed-orchestration-fallbacks-empty?
   (= [] (:seed-orchestration-fallbacks artifact))
   :runner-fallbacks-empty? (= [] (:runner-fallbacks artifact))
   :os-boundaries-empty? (= [] (:os-boundaries artifact))
   :image-fallbacks-empty? (= [] (:image-fallbacks artifact))
   :machine-boundaries-empty? (= [] (:machine-boundaries artifact))
   :boot-chain-fallbacks-empty? (= [] (:boot-chain-fallbacks artifact))
   :trust-anchor-boundaries-empty?
   (= [] (:trust-anchor-boundaries artifact))
   :physical-release-boundaries-empty?
   (= [] (:physical-release-boundaries artifact))
   :diverse-verification-fallbacks-empty?
   (= [] (:diverse-verification-fallbacks artifact))
   :release-attestation-fallbacks-empty?
   (= [] (:release-attestation-fallbacks artifact))
   :formal-release-governance-fallbacks-empty?
   (= [] (:formal-release-governance-fallbacks artifact))
   :replaced-release-governance-boundaries-recorded?
   (= #{:human-release-governance :legal-custody-record-retention
        :deployment-environment-custody}
      (set (:replaced-release-governance-boundaries artifact)))
   :gravity-runtimes-covered?
   (set/subset?
    #{:stage1-reader-formal-release-governance-seed-retirement
      :stage1-reader-release-attestation-seed-retirement
      :stage1-reader-diverse-bootstrap-verification
      :stage1-reader-verified-boot-chain :stage1-reader-runtime-image
      :stage1-reader-runtime-entrypoint :stage1-reader-compiler-driver
      :stage1-reader-core-bootstrap-runtime
      :stage1-reader-self-hosted-runtime :stage1-reader-source-runtime}
    gravity-runtimes)
   :gravity-executors-covered?
   (set/subset? #{:stage1-reader-token-automaton-executor
                  :stage1-reader-form-builder-executor}
                gravity-executors)
   :character-stream-covered?
   (and (= :gravity/stage1-reader-character-stream (:kind character-stream))
        (= :gravity-reader-formal-release-governance-v1
           (:formal-release-governance-engine character-stream))
        (pos? (:character-count character-stream))
        (= (:character-count character-stream)
           (count (:characters character-stream))))
   :token-stream-covered?
   (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
        (= :gravity-reader-formal-release-governance-v1
           (:formal-release-governance-engine token-stream))
        (pos? (:token-count token-stream))
        (= (:token-count token-stream) (count (:tokens token-stream))))
   :form-records-covered?
   (and (seq records)
        (every? #(and
                  (= :gravity-reader-release-attestation-seed-retirement-v1
                     (:release-attestation-seed-retirement-engine %))
                  (= :gravity-reader-formal-release-governance-v1
                     (:formal-release-governance-engine %)))
                records))
   :forms-match-stage0?
   (true? (get-in artifact [:stage0-comparison :forms-equal?]))
   :source-spans-covered? (every? #(get-in % [:span :source]) records)
   :diagnostics-covered?
   (set/subset?
    (set (concat
          stage1-reader-formal-release-governance-seed-retirement-diagnostic-ids
          (butlast stage1-reader-execution-diagnostic-ids)))
    diagnostics)
   :limitations
   {:clojure-runtime-interpreter? false
    :clojure-instruction-executor? false
    :clojure-binary-runner? false
    :clojure-character-stream-implementation? false
    :clojure-seed-builtins? false
    :clojure-seed-orchestration? false
    :clojure-driver-runner? false
    :host-command-invocation? false
    :host-file-read? false
    :os-process-boundary? false
    :os-filesystem-read-boundary? false
    :stdout-boundary? false
    :machine-boundary? false
    :kernel-process-scheduler-boundary? false
    :artifact-loader-boundary? false
    :hardware-reset-vector-boundary? false
    :firmware-root-of-trust-boundary? false
    :external-auditor-key-boundary? false
    :physical-device-manufacturing-boundary? false
    :supply-chain-custody-boundary? false
    :independent-diversity-review-boundary? false
    :human-release-governance-boundary? false
    :legal-custody-record-retention-boundary? false
    :deployment-environment-custody-boundary? false
    :claimed-subset-self-hosted? true
    :full-language-compiler-self-hosted? false
    :clojure-seed-retired? false
    :next-required-capability
    :implement-full-language-compiler-self-hosting-before-retiring-clojure-seed}
   :status :complete})
