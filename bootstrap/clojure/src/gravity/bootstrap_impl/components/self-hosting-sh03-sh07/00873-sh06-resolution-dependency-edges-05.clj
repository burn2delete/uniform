
(declare sh06-resolution-module-request)
(declare ^:private resolution-module-request-from-base)
(declare ^:private sh06-resolution-verify-sh05)

(defn- resolution-base-request-from-authenticated-parent
  [source-path sh05-artifact overrides]
   (let [forms (mapv :form (:expanded-syntax-stream sh05-artifact))
        module (parse-module source-path forms)
        imports (sh06-resolution-import-records
                 source-path module overrides)
        explicit-definitions
        (sh06-resolution-explicit-candidate-records
         module sh05-artifact overrides)
        analysis-inputs
        (sh06-resolution-analysis-inputs
         module sh05-artifact overrides)
        source-revision-id
        (sh06-resolution-source-revision-id sh05-artifact)]
    {:artifact :gravity/sh06-authenticated-c4-resolution-request
     :schema-version 1
     :module
     {:namespace (:module module)
      :package (sh06-resolution-package (:module module))
      :profile (:profile module)
      :target (:target module)
      :safety (:safety module)
      :effects (vec (sort (:effects module)))
      :capabilities (vec (sort (:capabilities module)))
      :exports (vec (:exports module))
      :source-revision-id source-revision-id
      :c4-artifact-id (:artifact-id sh05-artifact)}
     :definitions
     (vec (concat
           (sh06-resolution-definition-records module sh05-artifact)
           explicit-definitions))
     :imports imports
     :import-bindings
     (sh06-resolution-import-binding-records module imports overrides)
     :lexical-scopes (:lexical-scopes analysis-inputs)
     :references (:references analysis-inputs)
     :core-bindings (sh06-resolution-core-records)
     :dependency-edges
     (sh06-resolution-dependency-edges source-path module imports overrides)
     :macro-expansion-binding
     {:artifact :gravity/sh05-macro-expansion-binding
      :schema-version 1
      :c4-artifact-id (:artifact-id sh05-artifact)
      :source-revision-id source-revision-id
      :macro-result-id (:expanded-syntax-stream-id sh05-artifact)
      :authenticated-envelope-id
      (sh06-resolution-envelope-id sh05-artifact)}
     :provenance {:actual-source-path source-path}}))

(defn sh06-resolution-base-request-data
  [source-path sh05-artifact overrides]
  (let [verification (sh06-resolution-verify-sh05 sh05-artifact)]
    (when-not (= :passed (:status verification))
      (c5-resolution-fail!
       "C5-UNRESOLVED" source-path
       {:stage :name-resolution}
       {:missing-fields [:verified-sh05-macro-artifact]}))
    (resolution-base-request-from-authenticated-parent source-path sh05-artifact overrides)))

(defn sh06-resolution-base-request
  ([source-path sh05-artifact]
   (let [module (parse-module source-path (mapv :form (:expanded-syntax-stream sh05-artifact)))]
     (sh06-resolution-base-request-data source-path sh05-artifact (sh06-resolution-request-overrides module))))
  ([source-path sh05-artifact context]
   (when (nil? context)
     (sh06-resolution-c5-fail! source-path :missing-context {}))
   (let [base (sh06-resolution-base-request source-path sh05-artifact)]
     (sh06-resolution-c5-authority-request!
      source-path sh05-artifact base context))))

(defn sh06-resolution-request
  ([source-path sh05-artifact]
   (sh06-resolution-module-request source-path sh05-artifact
    {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}))
  ([source-path sh05-artifact context]
   (sh06-resolution-base-request source-path sh05-artifact context)))

(def sh06-resolution-source-relative-path
  "bootstrap/gravity/src/gravity/resolution.gravity")
(def sh06-resolution-facade-relative-path
  "bootstrap/gravity/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity")
(def sh06-resolution-adapter-contract
  :gravity/sh06-to-c5-resolution-products-v1)
(def sh06-resolution-envelope-stage :c5-resolution)
(def sh06-resolution-sealed-artifact-kind
  :gravity/sh06-resolution-products)

;; Final source/plan pins are filled only after the leaf's semantic matrix is
;; stable.  A nil pin is rejected, so an in-progress leaf cannot accidentally
;; receive integration credit.
(def sh06-resolution-expected-source-byte-count 77209)
(def sh06-resolution-expected-source-content-hash
  "sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56")
(def sh06-resolution-expected-plan-semantic-hash
  "sha256:ba8c292ffe3c703bbc220af0a7121496c134793d1e80a012f6fb0a0d9dc6b6fa")
(def sh06-resolution-expected-plan-id
  "sha256:a85dd6c53929b8a9f9b4fdf9d7b8b3b5c92fd06bacccf282ea036f9738cc13ee")
(def sh06-resolution-expected-functions-semantic-hash
  "sha256:a1445872cda26a9aa0fb8959165eada593619de7a03f527a8ea8a8c216237b91")
(def sh06-resolution-expected-function-count 94)
(def sh06-resolution-expected-function-names-hash
  "sha256:1a12e29aea416d4df7c2904d366975ce240f5ecaee0b2b9c5aa95b1aa83d1ba4")
(def sh06-resolution-expected-function-shapes-hash
  "sha256:62aec401ab663019a10b63c1b91487f70b9756503d59e43879283afae8c02f57")
(def sh06-resolution-public-function-hashes
  {'sh06-build-resolution-template
   "sha256:154d52c95bc0488e5ec302ce9219b57d72995f5f54f137816ca980d97bf71f38"
   'sh06-verify-resolution-template
   "sha256:de579ce14c12644979fe636d5f7b32d02c505753b85463b8a1994eb9de7a03ec"
   'sh06-verify-resolution-resolved
   "sha256:80e3c1e5313ffb38805eba60110dc80edccb50218eaabd5edba98b2025e99342"})
(def sh06-resolution-public-function-shapes
  {'sh06-build-resolution-template {:arity 1 :params ['request]}
   'sh06-verify-resolution-template
   {:arity 2 :params ['analysis 'digest-requests]}
   'sh06-verify-resolution-resolved
   {:arity 3
    :params ['analysis 'digest-requests 'resolved-digests]}})

(declare
 ^{:private true} resolution-stage-input
 ^{:private true, :dynamic true} *sh06-resolution-operation-binding*
 c-backend-canonical-value
 c-backend-resolve-p15-s23-compiler-source-path
 c-backend-stage2-plan-emitter-source-rule!
 c5-resolution-fail!
 p15-s23-c11-mir-digest
 p15-s23-stage2-compiler-artifact-plan
 p15-s23-stage2-compiler-artifact-semantic-input
 p15-s23-stage2-compiler-artifact-source-path
 p15-s23-stage2-runtime-execute-function
 sh04-syntax-strip-host-metadata
 sh06-resolution-binding-current!
 sh06-resolution-boundary-fail!
 sh06-resolution-build-binding!
 ^{:private true} sh06-resolution-cached-binding
 sh06-resolution-current-source-key
 sh06-resolution-expected-source-byte-count
 sh06-resolution-expected-source-content-hash
 sh06-resolution-plan-identities
 sh06-resolution-public-function-shapes
 sh06-resolution-read-current-source!
 sh06-resolution-resolve-source-path
 sh06-resolution-source-relative-path
 sha256-bytes-hex
 sha256-hex
 source-span)

(declare sh06-resolution-artifact-identity-input
         sh06-resolution-identity-chunks
         sh06-resolution-order-compatibility
         ^:private sh06-resolution-product-identity-chunks
         ^:private sh06-resolution-artifact-identity-input-with-chunks
         ^:private sh06-resolution-envelope-summary-with-chunks)

(def
 ^{:private true} resolution-stage-defaults
 {:c5-resolution-fail! #'c5-resolution-fail!,
  :p15-s23-stage2-compiler-artifact-plan
  #'p15-s23-stage2-compiler-artifact-plan,
  :source-span #'source-span,
  :sh06-resolution-envelope-summary-with-chunks
  #'sh06-resolution-envelope-summary-with-chunks,
  :sh06-resolution-order-compatibility
  #'sh06-resolution-order-compatibility,
  :p15-s23-stage2-runtime-execute-function
  #'p15-s23-stage2-runtime-execute-function,
  :sh06-resolution-resolve-source-path
  #'sh06-resolution-resolve-source-path,
  :p15-s23-stage2-compiler-artifact-semantic-input
  #'p15-s23-stage2-compiler-artifact-semantic-input,
  :sh06-resolution-artifact-identity-input-with-chunks
  #'sh06-resolution-artifact-identity-input-with-chunks,
  :sh06-fixed-vector-pattern? #'sh06-fixed-vector-pattern?,
  :sh06-resolution-definition-kind #'sh06-resolution-definition-kind,
  :sha256-hex #'sha256-hex,
  :sh06-resolution-core-record #'sh06-resolution-core-record,
  :c-backend-canonical-value #'c-backend-canonical-value,
  :reader-canonical-hash #'reader-canonical-hash,
  :sh06-resolution-artifact-identity-input
  #'sh06-resolution-artifact-identity-input,
  :sh06-resolution-read-current-source!
  #'sh06-resolution-read-current-source!,
  :c-backend-resolve-p15-s23-compiler-source-path
  #'c-backend-resolve-p15-s23-compiler-source-path,
  :sh06-resolution-product-identity-chunks
  #'sh06-resolution-product-identity-chunks,
  :sh06-resolution-binding-current! #'sh06-resolution-binding-current!,
  :sh06-resolution-package #'sh06-resolution-package,
  :sh06-resolution-current-source-key
  #'sh06-resolution-current-source-key,
  :sha256-bytes-hex #'sha256-bytes-hex,
  :input #'resolution-stage-input,
  :sh06-resolution-plan-identities #'sh06-resolution-plan-identities,
  :sh06-let-loop-binding-paths #'sh06-let-loop-binding-paths,
  :sh06-resolution-identity-chunks #'sh06-resolution-identity-chunks,
  :sh06-resolution-build-binding! #'sh06-resolution-build-binding!,
  :sh06-resolution-boundary-fail! #'sh06-resolution-boundary-fail!,
  :sh06-unique-match-binding-paths #'sh06-unique-match-binding-paths,
  :c-backend-stage2-plan-emitter-source-rule!
  #'c-backend-stage2-plan-emitter-source-rule!,
  :sh06-resolution-dependency-record
  #'sh06-resolution-dependency-record,
  :p15-s23-stage2-compiler-artifact-source-path
  #'p15-s23-stage2-compiler-artifact-source-path,
  :sh06-match-pattern-binding-paths #'sh06-match-pattern-binding-paths,
  :p15-s23-c11-mir-digest #'p15-s23-c11-mir-digest,
  :sh06-resolution-semantic-span #'sh06-resolution-semantic-span,
  :sh04-syntax-strip-host-metadata #'sh04-syntax-strip-host-metadata})

^{:line 100, :column 18}
(defn- resolution-stage-operations [] resolution-stage-defaults)

(defn-
 resolution-stage-input
 [field]
 (case
  field
  :*sh06-resolution-operation-binding*
  *sh06-resolution-operation-binding*
  :sh06-resolution-cached-binding
  sh06-resolution-cached-binding
  :sh06-resolution-expected-source-byte-count
  sh06-resolution-expected-source-byte-count
  :sh06-resolution-expected-source-content-hash
  sh06-resolution-expected-source-content-hash
  :sh06-resolution-public-function-shapes
  sh06-resolution-public-function-shapes
  :sh06-resolution-source-relative-path
  sh06-resolution-source-relative-path
  :known-source-profiles
  known-source-profiles
  :sh06-resolution-all-core-symbols
  sh06-resolution-all-core-symbols
  :sh06-resolution-special-symbols
  sh06-resolution-special-symbols
  :sh06-resolution-type-symbols
  sh06-resolution-type-symbols
  (throw (ex-info "Unknown resolution stage input" {:input field}))))

(defn
 sh06-resolution-boundary-fail!
 [rule source-path missing-field observed facts]
 (gravity.resolution/boundary-fail!
  rule
  source-path
  missing-field
  observed
  facts
  ^{:line 117, :column 65}
  (resolution-stage-operations)))

(defn
 sh06-resolution-resolve-source-path
 []
 (gravity.resolution/resolve-source-path
  ^{:line 117, :column 65}
  (resolution-stage-operations)))

(defn
 sh06-resolution-read-current-source!
 [request-source]
 (gravity.resolution/read-current-source!
  request-source
  ^{:line 117, :column 65}
  (resolution-stage-operations)))

(defn
 sh06-resolution-read-pinned-source!
 [request-source]
 (gravity.resolution/read-pinned-source!
  request-source
  ^{:line 117, :column 65}
  (resolution-stage-operations)))

(defn
 sh06-resolution-plan-identities
 [plan]
 (gravity.resolution/plan-identities
  plan
  ^{:line 117, :column 65}
  (resolution-stage-operations)))
