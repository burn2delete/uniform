

(defn stage1-reader-diverse-bootstrap-verification-from-definitions
  [reader-source-path definitions]
  (let [diverse-verification
        (stage1-reader-diverse-bootstrap-verification-literal-definition-value
         reader-source-path definitions
         'stage1-reader-diverse-bootstrap-verification)
        diagnostics (:diagnostics diverse-verification)
        missing-diagnostics
        (remove #(contains? diagnostics %)
                [:missing-entrypoint
                 :unsupported-verification-operation
                 :missing-diverse-verification-record
                 :single-implementation-self-certification
                 :bootstrap-trace-divergence
                 :unreproducible-diverse-build-provenance
                 :missing-independent-audit-metadata
                 :illegal-trust-anchor-fallback
                 :invalid-diverse-bootstrap-verification])
        required-stages
        [:stage1-diverse-bootstrap-seed-built-rebuild
         :stage1-diverse-bootstrap-self-built-rebuild
         :stage1-diverse-bootstrap-clean-environment-rebuild
         :stage1-diverse-bootstrap-diverse-toolchain-rebuild
         :stage1-diverse-bootstrap-compare-traces
         :stage1-diverse-bootstrap-verify-provenance
         :stage1-diverse-bootstrap-record-independent-audit]
        direct-stages (:direct-stages diverse-verification)
        independent-toolchains (:independent-toolchains diverse-verification)
        toolchain-identities (set (map :identity independent-toolchains))
        trace-comparisons (:bootstrap-trace-comparisons diverse-verification)
        trace-modes (set (map :mode trace-comparisons))
        reproducible-evidence
        (:reproducible-build-evidence diverse-verification)
        audit-record (:independent-audit-record diverse-verification)]
    (when-not (map? diverse-verification)
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV003" reader-source-path diverse-verification
       {:missing-fields [:stage1-reader-diverse-bootstrap-verification]}))
    (doseq [field [:engine :entrypoint :replaces :verified-boot-chain
                   :input :output :artifact :diagnostic-stream
                   :proof-kind :trust-anchor-boundaries
                   :replaced-trust-anchor-boundaries
                   :residual-trust-boundaries
                   :diverse-verification-fallbacks
                   :diverse-verification-operations
                   :independent-toolchains
                   :bootstrap-trace-comparisons
                   :reproducible-build-evidence
                   :independent-audit-record
                   :direct-stages :uses-runtimes :uses-builtins
                   :uses-executors :preserves :diagnostics :provenance]]
      (when-not (contains? diverse-verification field)
        (stage1-reader-diverse-bootstrap-verification-fail!
         "STAGE1DIV009" reader-source-path diverse-verification
         {:missing-fields [field]})))
    (when-not (= :gravity-reader-diverse-bootstrap-verification-v1
                 (:engine diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:engine]}))
    (when-not (= :stage1-read-source-diverse-bootstrap-verification
                 (:entrypoint diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:entrypoint]}))
    (when-not (set/subset? #{:hardware-reset-vector
                             :firmware-root-of-trust
                             :external-auditor-key}
                           (set (:replaces diverse-verification)))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:replaces]}))
    (when-not (= :stage1-reader-verified-boot-chain
                 (:verified-boot-chain diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:verified-boot-chain]}))
    (when-not (= [:diverse-bootstrap-verification
                  :verified-boot-chain
                  :source-path
                  :source-text]
                 (:input diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records
                 (:output diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:output]}))
    (when-not (= :gravity/stage1-reader-diverse-bootstrap-verification-artifact
                 (:artifact diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV005" reader-source-path diverse-verification
       {:missing-fields [:artifact]}))
    (when-not (= :gravity/stage1-reader-diverse-bootstrap-verification-diagnostic-stream
                 (:diagnostic-stream diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:diagnostic-stream]}))
    (when-not (= :gravity/stage1-reader-diverse-bootstrap-verification-proof
                 (:proof-kind diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV006" reader-source-path diverse-verification
       {:missing-fields [:proof-kind]}))
    (when-not (= [] (:trust-anchor-boundaries diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV008" reader-source-path diverse-verification
       {:trust-anchor-boundaries
        (:trust-anchor-boundaries diverse-verification)}))
    (when-not (set/subset? #{:hardware-reset-vector
                             :firmware-root-of-trust
                             :external-auditor-key}
                           (set (:replaced-trust-anchor-boundaries
                                 diverse-verification)))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:replaced-trust-anchor-boundaries]}))
    (when-not (set/subset? #{:physical-device-manufacturing
                             :supply-chain-custody
                             :independent-diversity-review}
                           (set (:residual-trust-boundaries
                                 diverse-verification)))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV006" reader-source-path diverse-verification
       {:missing-fields [:residual-trust-boundaries]}))
    (when-not (= [] (:diverse-verification-fallbacks diverse-verification))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV008" reader-source-path diverse-verification
       {:diverse-verification-fallbacks
        (:diverse-verification-fallbacks diverse-verification)}))
    (let [diverse-operations
          (:diverse-verification-operations diverse-verification)
          operation-names (set diverse-operations)
          required-operation-set
          (set stage1-reader-diverse-bootstrap-verification-required-operations)]
      (when-not (vector? diverse-operations)
        (stage1-reader-diverse-bootstrap-verification-fail!
         "STAGE1DIV003" reader-source-path diverse-verification
         {:missing-fields [:diverse-verification-operations]}))
      (when-not (set/subset? required-operation-set operation-names)
        (stage1-reader-diverse-bootstrap-verification-fail!
         "STAGE1DIV002" reader-source-path diverse-operations
         {:missing-operations
          (vec (remove operation-names
                       stage1-reader-diverse-bootstrap-verification-required-operations))})))
    (when-not (and (vector? direct-stages)
                   (= required-stages (mapv :op direct-stages)))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:direct-stages]}))
    (when-not (and (vector? independent-toolchains)
                   (>= (count independent-toolchains) 2)
                   (>= (count toolchain-identities) 2))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV004" reader-source-path independent-toolchains
       {:missing-fields [:independent-toolchains]}))
    (when-not (and (set/subset? #{:manifest-equivalence
                                  :diagnostic-equivalence
                                  :stage0-form-parity}
                                trace-modes)
                   (every? #(= :accepted (:status %)) trace-comparisons))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV005" reader-source-path trace-comparisons
       {:missing-fields [:bootstrap-trace-comparisons]}))
    (when-not (and (= true (:locked-dependencies reproducible-evidence))
                   (= true (:fixed-time reproducible-evidence))
                   (= "C" (:locale reproducible-evidence))
                   (= :canonical (:filesystem-order reproducible-evidence))
                   (= :disabled (:network reproducible-evidence)))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV006" reader-source-path reproducible-evidence
       {:missing-fields [:reproducible-build-evidence]}))
    (when-not (and (= :recorded (:status audit-record))
                   (= :independent-diversity-review
                      (:review-kind audit-record))
                   (:reviewer audit-record))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV007" reader-source-path audit-record
       {:missing-fields [:independent-audit-record]}))
    (when-not (set/subset? #{:stage1-reader-diverse-bootstrap-verification
                             :stage1-reader-verified-boot-chain
                             :stage1-reader-runtime-image
                             :stage1-reader-runtime-entrypoint
                             :stage1-reader-compiler-driver
                             :stage1-reader-core-bootstrap-runtime
                             :stage1-reader-self-hosted-runtime
                             :stage1-reader-source-runtime}
                           (set (:uses-runtimes diverse-verification)))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:uses-runtimes]}))
    (when-not (set/subset? #{:stage1-reader-core-bootstrap-builtins}
                           (set (:uses-builtins diverse-verification)))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:uses-builtins]}))
    (when-not (set/subset? #{:stage1-reader-token-automaton-executor
                             :stage1-reader-form-builder-executor}
                           (set (:uses-executors diverse-verification)))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diverse-verification
       {:missing-fields [:uses-executors]}))
    (when-not (= :gravity-source
                 (get-in diverse-verification [:provenance :owner]))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV006" reader-source-path diverse-verification
       {:missing-fields [:provenance :owner]}))
    (when-not (= :reader-diverse-bootstrap-trust-anchor-replacement
                 (get-in diverse-verification [:provenance :purpose]))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV006" reader-source-path diverse-verification
       {:missing-fields [:provenance :purpose]}))
    (when-not (= :replace-hardware-firmware-and-external-trust-anchors-with-diverse-self-hosted-bootstrap-verification
                 (get-in diverse-verification
                         [:provenance :retirement-objective]))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV006" reader-source-path diverse-verification
       {:missing-fields [:provenance :retirement-objective]}))
    (when (seq missing-diagnostics)
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV009" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc diverse-verification
           :diverse-bootstrap-verification-id
           (str "sha256:" (sha256-hex (pr-str diverse-verification))))))

(defn stage1-reader-diverse-bootstrap-verification-entrypoint-valid?
  [definitions]
  (let [definition (get definitions
                        stage1-reader-diverse-bootstrap-verification-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-diverse-bootstrap-verification
               source-path
               source-text
               stage1-reader-diverse-bootstrap-verification
               stage1-reader-verified-boot-chain))
            (:body definition)))))