
(def transport-limits
  {:request-max-bytes 4194304
   :response-max-bytes 1073741824
   :stderr-max-bytes 1048576
   :deadline-ms 3600000})

(def closed-environment
  {"LC_ALL" "C"
   "LANG" "C"
   "TZ" "UTC"
   "GIT_CONFIG_NOSYSTEM" "1"
   "GIT_CONFIG_GLOBAL" "/dev/null"
   "GIT_OPTIONAL_LOCKS" "0"
   "GIT_NO_REPLACE_OBJECTS" "1"})

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

(def role-specs
  {:producer-census
   {:cli "producer-census"
    :sequence :a
    :checker-namespace
    "gravity.tools.sh07-b51-attempt30-producer-census"
    :checker-entrypoint "produce-certificate"
    :forbidden-namespace
    "gravity.tools.sh07-b51-attempt30-independent-census"
    :output-name "checker-a-output.edn"
    :algorithm-role :source-first-exhaustive}
   :independent-verifier-census
   {:cli "independent-verifier-census"
    :sequence :b
    :checker-namespace
    "gravity.tools.sh07-b51-attempt30-independent-census"
    :checker-entrypoint "produce-certificate"
    :forbidden-namespace
    "gravity.tools.sh07-b51-attempt30-producer-census"
    :output-name "checker-b-output.edn"
    :algorithm-role :m2-first-independent-exhaustive}})

(def ^:private request-keys
  #{:schema :role :content :eligibility :evidence :limits})
(def ^:private content-keys #{:commit :tree})
(def ^:private evidence-keys #{:architecture-report-sha256 :profile-bounds})
(def ^:private eligibility-keys
  #{:schema :commit :tree :parents :path :mode :type :blob-oid :byte-count
    :raw-sha256 :receipt-id})
(def ^:private product-keys
  #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
    :module-transactions :sh06-resolution-plan-authority
    :source-semantic-carrier :source-read-receipt})
(def ^:private source-receipt-keys
  #{:schema :role :commit :tree :source-descriptors
    :source-descriptors-root :events :event-root :counters :finalized})
(def ^:private source-counter-keys
  #{:acquire-count :decode-count :event-count :finalize-count})
(def ^:private source-descriptor-keys
  #{:ordinal :kind :module-ordinal :path :mode :blob-oid :byte-count
    :raw-sha256})
(def ^:private source-event-keys
  #{:ordinal :domain :source-ordinal :module-ordinal :consumer
    :source-raw-sha256 :product-id})
(def ^:private checker-response-keys
  #{:schema :role :transport :result :counters :tcb :diagnostics})

(def ^:private eligibility-path
  "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn")
(def ^:private evidence-relative-root
  "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9")
(def ^:private completed-at "1970-01-01T00:00:00Z")
(def ^:private predecessor-executable-descriptor-id
  "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234")
(def ^:private codec-namespace "gravity.tools.sh07-b51-v9-codec")
(def ^:private bootstrap-namespace "gravity.bootstrap")
(def ^:private git-evidence-namespace
  "gravity.tools.sh07-b51-git-evidence")
(def ^:private identity-namespace
  "gravity.tools.sh07-b51-attempt32-checker-identity")
(def ^:private child-main-token
  "gravity.tools.sh07-b51-attempt32-checker-child")
(def ^:private classpath-source-prefix "bootstrap/clojure/src/")
(def ^:private empty-raw-sha256
  "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
(def ^:private pre-manifest-row-keys
  #{:ordinal :path :namespace :mode :blob-oid :byte-count :raw-sha256
    :materialized-raw-sha256 :requires-project-namespaces})
(def ^:private codec-tcb-load-order
  '[gravity.c2-artifact-identity.canonical
    gravity.digest
    gravity.tools.sh07-b51-v9-codec])
(def ^:private codec-tcb-specs
  [{:ordinal 0
    :path "bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj"
    :namespace 'gravity.c2-artifact-identity.canonical
    :requires-project-namespaces []}
   {:ordinal 1
    :path "bootstrap/clojure/src/gravity/digest.clj"
    :namespace 'gravity.digest
    :requires-project-namespaces []}
   {:ordinal 2
    :path "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"
    :namespace 'gravity.tools.sh07-b51-v9-codec
    :requires-project-namespaces
    '[gravity.c2-artifact-identity.canonical gravity.digest]}])
(def ^:private external-classpath-specs
  [{:ordinal 1 :logical-coordinate 'org.clojure/clojure
    :version "1.12.5" :required-namespace 'clojure.core}
   {:ordinal 2 :logical-coordinate 'org.clojure/core.specs.alpha
    :version "0.4.74" :required-namespace 'clojure.core.specs.alpha}
   {:ordinal 3 :logical-coordinate 'org.clojure/spec.alpha
   :version "0.5.238" :required-namespace 'clojure.spec.alpha}])
(def ^:private eligibility-receipt-keys
  #{:artifact :schema-version :content :freeze :source-descriptors
    :source-descriptors-root :pre-masked-root :pre-evidence-contract-discharge
    :review :state :self-id-omits :nonclaims :receipt-id})
(def ^:private eligibility-result-keys
  #{:coordinate :receipt :observations :operation-count
    :pre-object-snapshot :pre-discharge-observations
    :pre-discharge-git-operation-count :scope :eligibility-check
    :eligibility-check-root :remaining-gaps :authority})
(def ^:private eligibility-check-keys
  #{:coordinate :observations :pre-discharge-observations :decode-count
    :receipt-id-check-count :pre-discharge-reconstruction-count
    :pre-discharge-git-operation-count
    :pre-discharge-document-descriptor-count
    :pre-discharge-source-descriptor-count
    :pre-discharge-pre-masked-file-count
    :pre-discharge-owned-c-artifact-count
    :pre-discharge-a9-artifact-count
    :pre-discharge-baseline-boot5-count
    :pre-discharge-static-decode-count :content-match
    :source-descriptors-match :source-descriptors-root-match
    :pre-masked-root-match :owned-c-root-match :pre-discharge-match})
(def ^:private eligibility-check-counts
  {:decode-count 1
   :receipt-id-check-count 1
   :pre-discharge-reconstruction-count 1
   :pre-discharge-git-operation-count 209
   :pre-discharge-document-descriptor-count 33
   :pre-discharge-source-descriptor-count 13
   :pre-discharge-pre-masked-file-count 2
   :pre-discharge-owned-c-artifact-count 160
   :pre-discharge-a9-artifact-count 3
   :pre-discharge-baseline-boot5-count 1
   :pre-discharge-static-decode-count 196})

(def semantic-algorithm-source-paths-e3
  ["bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_independent_census.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_producer_census.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"])
(def ^:private eligibility-check-matches
  [:content-match :source-descriptors-match
   :source-descriptors-root-match :pre-masked-root-match
   :owned-c-root-match :pre-discharge-match])
(def ^:private nonclaims
  [:formal-proof :sh07-complete :self-hosting :release :seed-retirement
   :hostile-host])

(def ^:private source-specs
  [{:ordinal 0 :kind :l1-contract :module-ordinal nil
    :path "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"}
   {:ordinal 1 :kind :l2-contract :module-ordinal nil
    :path "docs/phase-01-core-language/012-l2-core-language-semantics.md"}
   {:ordinal 2 :kind :checked-core :module-ordinal nil
    :path "bootstrap/gravity/src/gravity/checked_core.gravity"}
   {:ordinal 3 :kind :module :module-ordinal 0
    :path "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"}
   {:ordinal 4 :kind :module :module-ordinal 1
    :path "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"}
   {:ordinal 5 :kind :module :module-ordinal 2
    :path "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"}
   {:ordinal 6 :kind :module :module-ordinal 3
    :path "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"}
   {:ordinal 7 :kind :module :module-ordinal 4
    :path "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"}
   {:ordinal 8 :kind :module :module-ordinal 5
    :path "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"}
   {:ordinal 9 :kind :module :module-ordinal 6
    :path "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"}
   {:ordinal 10 :kind :compiler :module-ordinal nil
    :path "bootstrap/gravity/p15_s23/compiler.gravity"}
   {:ordinal 11 :kind :emitter :module-ordinal nil
    :path "bootstrap/gravity/p15_s23/emitter.gravity"}
   {:ordinal 12 :kind :sh06-resolution :module-ordinal nil
    :path "bootstrap/gravity/src/gravity/resolution.gravity"}])
