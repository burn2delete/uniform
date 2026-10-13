
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

(def child-role-specs
  {:producer-census
   {:cli "producer-census" :sequence :a
    :identity-name "checker-a-identity.edn"
    :output-name "checker-a-output.edn"}
   :independent-verifier-census
   {:cli "independent-verifier-census" :sequence :b
    :identity-name "checker-b-identity.edn"
    :output-name "checker-b-output.edn"}})

(def coordinator-event-plan
  [[:m-process-start :m]
   [:a-builder :a]
   [:a-start :a]
   [:a-exit-observed :a]
   [:a-response-validated :a]
   [:b-builder :b]
   [:b-start :b]
   [:b-exit-observed :b]
   [:b-response-validated :b]
   [:m-session-start :m]
   [:m-session-finalize :m]])

(def ^:private coordinator-option-names
  ["--snapshot-commit"
   "--snapshot-tree"
   "--completed-at"
   "--evidence-relative-root"
   "--staging-root"
   "--eligibility-review-commit"
   "--eligibility-review-tree"
   "--eligibility-receipt-path"
   "--eligibility-receipt-blob"
   "--eligibility-receipt-byte-count"
   "--eligibility-receipt-raw-sha256"
   "--eligibility-receipt-id"])

(def ^:private evidence-relative-root
  "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9")
(def ^:private eligibility-path
  "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn")
(def ^:private completed-at "1970-01-01T00:00:00Z")
(def ^:private child-main-token
  "gravity.tools.sh07-b51-attempt32-checker-child")
(def ^:private coordinator-main-token
  "gravity.tools.sh07-b51-attempt32-coordinator-child")
(def ^:private codec-namespace "gravity.tools.sh07-b51-v9-codec")
(def ^:private bootstrap-namespace "gravity.bootstrap")
(def ^:private git-evidence-namespace
  "gravity.tools.sh07-b51-git-evidence")
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
(def ^:private eligibility-check-matches
  [:content-match :source-descriptors-match
   :source-descriptors-root-match :pre-masked-root-match
   :owned-c-root-match :pre-discharge-match])

(def eligibility-participant-order [:l :m :a :b])

(def ^:private aggregate-eligibility-counter-values
  {:q-coordinate-git-operation-count 20
   :pre-discharge-git-operation-count 836
   :git-operation-count 856
   :q-coordinate-decode-count 4
   :receipt-id-check-count 4
   :pre-discharge-reconstruction-count 4
   :pre-discharge-static-decode-count 784
   :pre-discharge-document-descriptor-count 132
   :pre-discharge-source-descriptor-count 52
   :pre-discharge-pre-masked-file-count 8
   :pre-discharge-owned-c-artifact-count 640
   :pre-discharge-a9-artifact-count 12
   :pre-discharge-baseline-boot5-count 4
   :content-match-count 4
   :source-descriptors-match-count 4
   :source-descriptors-root-match-count 4
   :pre-masked-root-match-count 4
   :owned-c-root-match-count 4
   :pre-discharge-match-count 4})
(def ^:private nonclaims
  [:formal-proof :sh07-complete :self-hosting :release :seed-retirement
   :hostile-host])

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
    :version "0.5.238"
    :required-namespace 'clojure.spec.alpha}])

(def ^:private event-keys
  #{:ordinal :event :role :process-handle-id :argv-id :request-id
    :response-preimage-id :source-session-id :prior-event-root :event-id})
(def ^:private response-keys
  #{:schema :role :transport :result :counters :tcb :diagnostics})
(def ^:private response-transport-keys
  #{:argv-id :request-id :response-preimage-id :self-id-omits})
(def ^:private result-keys
  #{:checker-identity :checker-output :source-read-receipt :production
    :sentinel})
(def ^:private source-receipt-keys
  #{:schema :role :commit :tree :source-descriptors
    :source-descriptors-root :events :event-root :counters :finalized})
(def ^:private product-keys
  #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
    :module-transactions :sh06-resolution-plan-authority
    :source-semantic-carrier :source-read-receipt})
(def ^:private source-counter-keys
  #{:acquire-count :decode-count :event-count :finalize-count})
(def ^:private source-descriptor-keys
  #{:ordinal :kind :module-ordinal :path :mode :blob-oid :byte-count
    :raw-sha256})
(def ^:private source-event-keys
  #{:ordinal :domain :source-ordinal :module-ordinal :consumer
    :source-raw-sha256 :product-id})
(def ^:private result-record-keys
  #{:schema :role :status :artifact-id :output-id :authority})
(def ^:private response-counter-keys #{:work :source-session :process :ipc})
(def ^:private work-counter-keys #{:vector :total})
(def ^:private child-process-counter-keys
  #{:child-count :start-count :exit-count :overlap-count})
(def ^:private ipc-counter-keys
  #{:request-count :response-count :request-max-bytes :response-max-bytes
    :stderr-max-bytes :deadline-ms})
(def ^:private tcb-keys
  #{:manifest-id :role :process :runtime-mxbean :java :git :classpath
    :pre-manifest-tcb :eligibility-check :environment :source-closure
    :loaded-namespace-receipt :host-surface})

(defn- reject!
  ([reason] (reject! reason {}))
  ([reason details]
   (throw (ex-info "SH-07 B51 coordinator child rejected"
                   {:id "SH07-B51-CHECKER-PROCESS"
                    :reason reason
                    :details details}))))

(defn- exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn- git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- sha256-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- dense-observations? [expected-count observations]
  (and (vector? observations)
       (= expected-count (count observations))
       (= (vec (range expected-count)) (mapv :ordinal observations))))
