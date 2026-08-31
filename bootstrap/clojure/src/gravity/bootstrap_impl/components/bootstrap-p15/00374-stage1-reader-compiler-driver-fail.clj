

(defn stage1-reader-compiler-driver-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-compiler-driver-diagnostic-messages
              id
              "stage1 reader compiler-driver execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-compiler-driver
                 :diagnostic-family :stage1-reader-compiler-driver
                 :value value
                 :remediation "Keep compiler-driver orchestration in Gravity-owned source, reject seed orchestration fallback, preserve diagnostics and artifact routing, and keep remaining host command/file boundaries explicit until they are replaced."}
                data)))

(defn stage1-reader-runtime-entrypoint-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-runtime-entrypoint-diagnostic-messages
              id
              "stage1 reader runtime-entrypoint execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-runtime-entrypoint
                 :diagnostic-family :stage1-reader-runtime-entrypoint
                 :value value
                 :remediation "Keep runtime entrypoint command, file, artifact, and exit routing in Gravity-owned source, reject Clojure runner fallback, and keep any remaining OS process or filesystem boundary explicit until it is replaced."}
                data)))

(defn stage1-reader-runtime-image-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-runtime-image-diagnostic-messages
              id
              "stage1 reader runtime-image execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-runtime-image
                 :diagnostic-family :stage1-reader-runtime-image
                 :value value
                 :remediation "Keep runtime image loading, source mounting, stdout routing, artifact emission, and machine-boundary recording in Gravity-owned source; reject OS boundary fallback until the next boot-chain proof replaces it."}
                data)))

(defn stage1-reader-verified-boot-chain-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-verified-boot-chain-diagnostic-messages
              id
              "stage1 reader verified boot-chain execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-verified-boot-chain
                 :diagnostic-family :stage1-reader-verified-boot-chain
                 :value value
                 :remediation "Keep verified boot-chain, runtime-image activation, artifact loading, scheduler authority, reproducible provenance, and trust-anchor records in Gravity-owned source; reject machine/kernel fallback until the next diverse bootstrap proof replaces it."}
                data)))

(defn stage1-reader-diverse-bootstrap-verification-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-diverse-bootstrap-verification-diagnostic-messages
              id
              "stage1 reader diverse bootstrap verification execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-diverse-bootstrap-verification
                 :diagnostic-family
                 :stage1-reader-diverse-bootstrap-verification
                 :value value
                 :remediation "Keep diverse bootstrap rebuilds, trace comparison, reproducible provenance, independent audit metadata, and residual trust assumptions in Gravity-owned source; reject hardware, firmware, or external-auditor fallback."}
                data)))

(defn stage1-reader-release-attestation-seed-retirement-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-release-attestation-seed-retirement-diagnostic-messages
              id
              "stage1 reader release attestation seed-retirement execution failed")
         (merge {:source-span {:source source-path}
                 :stage
                 :stage1-reader-release-attestation-seed-retirement
                 :diagnostic-family
                 :stage1-reader-release-attestation-seed-retirement
                 :value value
                 :remediation "Keep release attestation, seed-retirement evidence, reproducible custody, supply-chain manifests, governance approval, revocation checks, and residual release-governance assumptions in Gravity-owned source; reject physical, supply-chain, or independent-review fallback."}
                data)))

(defn stage1-reader-formal-release-governance-seed-retirement-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-formal-release-governance-seed-retirement-diagnostic-messages
              id
              "stage1 reader formal release governance seed-retirement execution failed")
         (merge {:source-span {:source source-path}
                 :stage
                 :stage1-reader-formal-release-governance-seed-retirement
                 :diagnostic-family
                 :stage1-reader-formal-release-governance-seed-retirement
                 :value value
                 :remediation "Keep formal release governance, deployment custody, self-hosting evidence, TCB deltas, unsafe-audit records, and residual full-compiler self-hosting assumptions in Gravity-owned source; reject human governance or deployment custody fallback."}
                data)))

(defn- stage1-reader-token-index
  [tokens]
  (into {}
        (map (juxt #(or (:token-id %)
                        (keyword (str "tok-" (:index %))))
                   identity)
             tokens)))