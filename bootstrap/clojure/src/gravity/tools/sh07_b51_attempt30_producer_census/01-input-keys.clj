
(def input-keys
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0s :m1 :m2s :bundle-core :source-forms :module-transactions
    :sh06-resolution-plan-authority :source-semantic-carrier
    :semantic-algorithm-source-descriptors :semantic-algorithm-source-root
    :checker :provenance})

(def checker-keys
  #{:artifact-id :source-sha256 :semantic-id :executable-id
    :dependency-closure-id :algorithm-role})

(def input-provenance-keys
  #{:invocation-id :tool-closure-id :reproduction-command-id
    :evidence-relative-path :completed-at})

(def certificate-provenance-keys
  (conj input-provenance-keys
        :raw-transcript-artifact-id :m2-transcript-artifact-id))

(def certificate-input-keys
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0-ids :m1-id :m2-ids :bundle-core-id :lowering-product-id})

(def certificate-keys
  #{:artifact :domain :schema-version :status :role
    :architecture-report-sha256 :claim-id :claim :assumptions :checker
    :inputs :result :semantic-witness :provenance :raw-source-transcript :m2-transcript
    :profile-bounds :evidence-root :certificate-id})

(def claim-keys
  #{:domain :normalized-census-id :external-bijection-commitment
    :builtin-parity-commitments :provenance-completeness-commitment
    :physical-census :category-schedule :category-commitments})

(def assumptions-keys
  #{:sha256-collision-resistance :canonical-reader-hash-id
    :source-emitter-bytes-immutable :checker-closures-independent
    :host-pin-boundary-id :runtime-token-boundary-id})

(def result-keys
  #{:status :normalized-census-id :definition-count :local-count
    :external-count :builtin-count :semantic-row-count
    :external-bijection-complete :builtin-parity-complete
    :provenance-complete :ordering-complete :reachability-complete})

(def transcript-descriptor-keys
  #{:artifact :schema-version :node-count :row-count :artifact-id
    :record-root})

(def profile-bounds
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

(def category-schedule
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

(def categories
  [:roots :members :functions :local-runtime-call-edges
   :external-runtime-call-edges :referenced-builtins :module-contracts])

(def internal-builtins
  '[sh07-canonical-text-compare
    sh07-declared-digest-hash
    sh07-observe-utf8-byte-count
    sh07-b51-admitted-executable-carrier-validation?])

(def internal-builtin-arities
  {'sh07-canonical-text-compare 2
   'sh07-declared-digest-hash 1
   'sh07-observe-utf8-byte-count 3
   'sh07-b51-admitted-executable-carrier-validation? 1})

(def special-forms '#{defn ns if let loop recur do quote})

(def m0-keys
  #{:artifact :schema-version :module-source-id :module-ordinal :module
    :logical-source-path :owner :source-byte-count :source-sha256 :profile
    :target :effects :capabilities :safety :definitions :exports
    :core-binding-count :lexical-binding-count :external-bindings
    :aggregate-binding-count :builtin-calls})

(def definition-keys #{:name :arity :visibility :source-hash})
(def builtin-call-keys
  #{:caller :caller-function-source-hash :builtin :arity
    :compiler-internal?})
(def external-binding-keys
  #{:symbol :callee-module-ordinal :callee-module :callee-source-sha256
    :callee-definition-source-hash :arity :owner-edge})
(def m1-keys #{:artifact :schema-version :manifest-id :module-source-ids :edges})
(def external-row-keys
  #{:ordinal :caller-module-source-id :caller-function
    :caller-function-source-hash :callsite-path :callee-module-source-id
    :callee-function :callee-function-source-hash :arity :owner-edge
    :authorization})
(def bundle-core-keys
  #{:domain :schema-version :manifest-id :module-plans :routes
    :public-roots})
(def module-plan-coordinate-keys
  #{:module-ordinal :module-source-id :plan-id})
(def route-keys #{:edge-ordinal :caller-plan-id :callee-plan-id})
(def public-root-keys #{:root-ordinal :function :arity :plan-id})

(def physical-census-keys
  #{:module-count :module-censuses :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :semantic-row-count
    :module-binding-occupancies :maximum-binding-occupancy :binding-ceiling})

(def module-census-keys
  #{:module-ordinal :module :logical-source-path :owner :source-byte-count
    :source-sha256 :module-source-id :plan-id :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :core-binding-count
    :lexical-binding-count :external-binding-count :aggregate-binding-count})

(def raw-record-keys
  #{:node-ordinal :module-ordinal :caller :caller-function-source-hash
    :source-path :callsite-path :node-kind :quoted-subtree-stop
    :possible-call-head :classification :callee :arity
    :physical-ordinal :semantic-row})

(def m2-record-keys
  #{:node-ordinal :module-ordinal :instruction-ordinal :caller
    :caller-function-source-hash :path :op :call-classification :callee
    :arity :external-link :provenance-present :provenance
    :occurrence-ordinal :semantic-row})

(defn canonical-value [value]
  (canonical/reader-canonical-value canonical-value value))

(defn canonical-hash [value]
  (canonical/reader-canonical-hash digest/sha256-hex canonical-value value))

(defn- exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn- sha256-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- nonnegative-integer? [value]
  (and (integer? value) (<= 0 value)))

(defn- fail! [reason & [context]]
  (throw (ex-info "Attempt30 producer census rejected"
                  {:reason reason :context (or context {})})))

(defn- require! [condition reason & [context]]
  (when-not condition (fail! reason context)))
