;; Attempt 32 host scaffold.  Production pins and certificate values remain
;; deliberately unavailable until one immutable implementation snapshot is
;; independently reviewed.  Candidate constructors establish schemas and
;; joins; they do not confer authority.
(def p15-s23-sh07-b51-attempt32-architecture-report-sha256
  "sha256:34ee9b302f78d88743c3c8ba91cd9bd47c31bc1b4d814afc9e0a892f70e3b0dd")

;; Compatibility names retained for the Attempt-30 evidence harness.  Their
;; values are the superseding Attempt-32 authorities, never stale Attempt-30
;; identities.
(def p15-s23-sh07-b51-attempt30-architecture-report-sha256
  p15-s23-sh07-b51-attempt32-architecture-report-sha256)

(def p15-s23-sh07-b51-v4-schedule
  (array-map
   :rows-per-leaf 16
   :fold-fanout 16
   :maximum-fold-levels 3
   :category-count 7
   :maximum-total-rows 65536
   :maximum-row-nodes 48
   :maximum-row-depth 6
   :maximum-row-width 8
   :maximum-row-scalar-utf8 4096
   :maximum-row-c11-bytes 8192
   :maximum-row-canonical-reader-bytes 9216
   :maximum-leaf-nodes 1024
   :maximum-leaf-depth 12
   :maximum-leaf-width 16
   :maximum-leaf-scalar-utf8 4096
   :maximum-leaf-c11-bytes 196608
   :maximum-leaf-canonical-reader-bytes 212992
   :maximum-fold-nodes 512
   :maximum-fold-depth 8
   :maximum-fold-width 16
   :maximum-fold-scalar-utf8 4096
   :maximum-fold-c11-bytes 65536
   :maximum-fold-canonical-reader-bytes 73728
   :maximum-commitment-nodes 64
   :maximum-commitment-depth 6
   :maximum-commitment-width 16
   :maximum-commitment-scalar-utf8 4096
   :maximum-commitment-c11-bytes 16384
   :maximum-commitment-canonical-reader-bytes 18432
   :maximum-root-nodes 512
   :maximum-root-depth 8
   :maximum-root-width 64
   :maximum-root-scalar-utf8 4096
   :maximum-root-c11-bytes 65536
   :maximum-root-canonical-reader-bytes 73728))

(def p15-s23-sh07-b51-attempt32-profile-bounds
  {:certificate-maximum-nodes 2048
   :certificate-maximum-depth 12
   :certificate-maximum-width 35
   :certificate-maximum-collection-count 64
   :certificate-maximum-scalar-utf8 4096
   :certificate-maximum-canonical-reader-bytes 262144
   :outer-request-maximum-collection-count 65536
   :carrier-maximum-nodes 8388608
   :carrier-maximum-depth 256
   :carrier-maximum-width 65536
   :carrier-maximum-collection-count 524288
   :carrier-partition-maximum-collection-count 65536
   :carrier-maximum-scalar-utf8 65536
   :offline-transcript-maximum-nodes 262144
   :offline-transcript-maximum-records 262144
   :offline-transcript-maximum-call-rows 65536
   :offline-transcript-maximum-bytes 268435456})

(def p15-s23-sh07-b51-attempt30-profile-bounds
  p15-s23-sh07-b51-attempt32-profile-bounds)

(def p15-s23-sh07-b51-v4-categories
  [:roots :members :functions :local-runtime-call-edges
   :external-runtime-call-edges :referenced-builtins :module-contracts])

(def ^:private p15-s23-sh07-b51-v4-predecessor-id
  "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234")

(def ^:private p15-s23-sh07-b51-v4-inherited-contract-hashes
  (array-map
   :sh07-core-bounds
   "sha256:293bb59806e517d29988dd28a78f0005f0f5b8665ab02f70a82c2b8d91ce77b9"
   :sh07-core-contract
   "sha256:f20c2e57d820edef6461f83de84fe1973a58c02a2a2e9dd702b0a235364e607f"
   :sh07-core-diagnostic-catalog
   "sha256:4a3b03150d3f7e016b5c1e5a17fbea22ae2d73d63b71b1b6c6738630767562c3"))

(def ^:private p15-s23-sh07-b51-v4-logical-public-surface
  '[sh07-core-contract
    sh07-core-bounds
    sh07-core-diagnostic-catalog
    sh07-build-core-template
    sh07-verify-core-template
    sh07-verify-core-resolved
    sh07-b51-build-template
    sh07-b51-verify-template
    sh07-b51-resolve-digest-preimage
    sh07-b51-resolve-template
    sh07-b51-verify-resolved
    sh07-b51-build-independent-verifier-binding
    sh07-b51-build-final-artifact
    sh07-b51-finalize-rejection])

(def ^:private p15-s23-sh07-b51-v4-carrier-keys
  #{:domain :schema-version :predecessor-executable-descriptor-id
    :semantic-manifest :semantic-root-hash :module-source-descriptors
    :link-manifest :module-plan-carriers :execution-bundle :physical-census
    :compile-census-certificates})

(def ^:private p15-s23-sh07-b51-v4-raw-keys
  #{:artifact :schema-version :authenticated-sh06-request
    :authenticated-sh06-verification-report :sh06-resolution-artifact
    :predecessor-executable-descriptor :v18-executable-descriptor
    :predecessor-contract-carrier :predecessor-raw-outcome
    :physical-invocation :scope})

(def ^:private p15-s23-sh07-b51-v4-semantic-keys
  #{:domain :schema-version :predecessor-executable-descriptor-id
    :module-source-ids :link-manifest-id :module-plan-ids
    :execution-bundle-id :compile-census-certificate-ids :schedule
    :category-commitments :logical-public-surface
    :inherited-contract-value-hashes :total-row-count})

(def ^:private p15-s23-sh07-b51-v4-certificate-keys
  #{:artifact :domain :schema-version :status :role
    :architecture-report-sha256 :claim-id :claim :assumptions :checker
    :inputs :result :provenance :raw-source-transcript :m2-transcript
    :profile-bounds :evidence-root :certificate-id})

(def ^:private p15-s23-sh07-b51-v4-claim-keys
  #{:domain :normalized-census-id :external-bijection-commitment
    :builtin-parity-commitments :provenance-completeness-commitment
    :physical-census :category-schedule :category-commitments})

(def ^:private p15-s23-sh07-b51-v4-assumption-keys
  #{:sha256-collision-resistance :canonical-reader-hash-id
    :source-emitter-bytes-immutable :checker-closures-independent
    :host-pin-boundary-id :runtime-token-boundary-id})

(def ^:private p15-s23-sh07-b51-v4-checker-keys
  #{:artifact-id :source-sha256 :semantic-id :executable-id
    :dependency-closure-id :algorithm-role})

(def ^:private p15-s23-sh07-b51-v4-input-keys
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0-ids :m1-id :m2-ids :bundle-core-id})

(def ^:private p15-s23-sh07-b51-v4-result-keys
  #{:status :normalized-census-id :definition-count :local-count
    :external-count :builtin-count :semantic-row-count
    :external-bijection-complete :builtin-parity-complete
    :provenance-complete :ordering-complete :reachability-complete})

(def ^:private p15-s23-sh07-b51-v4-provenance-keys
  #{:invocation-id :tool-closure-id :raw-transcript-artifact-id
    :m2-transcript-artifact-id :reproduction-command-id
    :evidence-relative-path :completed-at})

(def ^:private p15-s23-sh07-b51-v4-transcript-keys
  #{:artifact :schema-version :node-count :row-count
    :artifact-id :record-root})

(def ^:private p15-s23-sh07-b51-v4-category-commitment-keys
  #{:category :row-count :leaf-count :fold-level-count :root-digest})

(def ^:private p15-s23-sh07-b51-v4-physical-census-keys
  #{:module-count :module-censuses :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :semantic-row-count
    :module-binding-occupancies :maximum-binding-occupancy :binding-ceiling})

(def ^:private p15-s23-sh07-b51-v4-module-census-keys
  #{:module-ordinal :module :logical-source-path :owner :source-byte-count
    :source-sha256 :module-source-id :plan-id :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :core-binding-count
    :lexical-binding-count :external-binding-count :aggregate-binding-count})

(defn- p15-s23-sh07-b51-v4-bounded-utf8-count
  [^String value limit]
  (loop [index 0 bytes 0]
    (if (or (> bytes limit) (>= index (.length value)))
      bytes
      (let [code-point (.codePointAt value index)
            width (cond
                    (<= code-point 0x7f) 1
                    (<= code-point 0x7ff) 2
                    (<= code-point 0xffff) 3
                    :else 4)]
        (recur (+ index (Character/charCount code-point))
               (+ bytes width))))))

(defn- p15-s23-sh07-b51-v4-scalar-text [value limit]
  (when (or (nil? value) (boolean? value) (char? value) (string? value)
            (keyword? value) (symbol? value) (number? value))
    (let [raw (cond
                (string? value) value
                (or (keyword? value) (symbol? value)) (str value)
                :else nil)]
      (when (or (nil? raw)
                (<= (p15-s23-sh07-b51-v4-bounded-utf8-count raw limit)
                    limit))
        (pr-str value)))))

(defn- p15-s23-sh07-b51-v4-bounded-collection-children
  [value maximum-width]
  ;; Some authenticated plan syntax is a PersistentList/Cons and therefore
  ;; does not implement Counted.  Walk at most width+1 elements instead of
  ;; calling count or rejecting a finite canonical list by representation.
  (loop [remaining (seq value) width 0 children []]
    (if (nil? remaining)
      {:width width :children children}
      (do
        (when (>= width maximum-width)
          (throw (ex-info "supplied collection exceeds width"
                          {:reason :maximum-width
                           :maximum maximum-width
                           :observed (inc width)})))
        (let [item (first remaining)]
          (if (map? value)
            (recur (next remaining) (inc width)
                   (conj children (key item) (val item)))
            (recur (next remaining) (inc width) (conj children item))))))))
