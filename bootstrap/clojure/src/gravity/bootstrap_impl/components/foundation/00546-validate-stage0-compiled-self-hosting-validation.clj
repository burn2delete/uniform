

(defn validate-stage0-compiled-self-hosting-validation!
  [module self-hosting-report]
  (when-not (:provenance-attestation self-hosting-report)
    (compiled-conformance-fail!
     "TEST13002" module self-hosting-report
     {:missing-fields [:provenance-attestation]})))

(defn validate-stage0-compiled-conformance!
  [module]
  (when (stage0-compiled-conformance-suite-present? module)
    (let [suite (stage0-compiled-conformance-suite module)]
      (doseq [fixture-manifest (:fixture-manifests suite)]
        (validate-stage0-compiled-fixture-manifest!
         module fixture-manifest))
      (doseq [compiler-report (:compiler-test-reports suite)]
        (validate-stage0-compiled-compiler-test-report!
         module compiler-report))
      (doseq [runtime-report (:runtime-conformance-reports suite)]
        (validate-stage0-compiled-runtime-conformance!
         module runtime-report))
      (doseq [profile-report (:profile-compliance-reports suite)]
        (validate-stage0-compiled-profile-compliance!
         module profile-report))
      (doseq [safety-report (:safety-conformance-reports suite)]
        (validate-stage0-compiled-safety-conformance!
         module safety-report))
      (doseq [backend-report (:backend-conformance-reports suite)]
        (validate-stage0-compiled-conformance-backend-report!
         module backend-report))
      (doseq [standard-library-report
              (:standard-library-test-reports suite)]
        (validate-stage0-compiled-standard-library-conformance!
         module standard-library-report))
      (doseq [ai-workflow-report (:ai-workflow-eval-reports suite)]
        (validate-stage0-compiled-ai-workflow-conformance!
         module ai-workflow-report))
      (doseq [fuzz-property-suite (:fuzz-property-suites suite)]
        (validate-stage0-compiled-fuzz-property-suite!
         module fuzz-property-suite))
      (doseq [differential-report (:differential-reports suite)]
        (validate-stage0-compiled-differential-report!
         module differential-report))
      (doseq [formal-proof-report (:formal-proof-reports suite)]
        (validate-stage0-compiled-formal-proof-report!
         module formal-proof-report))
      (doseq [performance-report (:performance-regression-reports suite)]
        (validate-stage0-compiled-performance-regression!
         module performance-report))
      (doseq [self-hosting-report
              (:self-hosting-validation-reports suite)]
        (validate-stage0-compiled-self-hosting-validation!
         module self-hosting-report)))))

(defn eval-do
  [module env forms]
  (gravity.runtime/eval-native-stage0-do #'eval-expr module env forms))

(defn bind-let
  [module env bindings]
  (gravity.runtime/bind-native-stage0-let #'fail! #'eval-expr module env bindings))

(defn function-definition-form
  [source-path form]
  (gravity.resolution.stage0-functions/function-definition-form #'defn-form? #'def-function-form? source-path form))

(defn- seed-declared-header? [{:keys [params body]}]
  ((deref #'gravity.resolution.stage0-functions/seed-declared-header?) {:params params :body body}))

(defn- normalize-seed-function-header
  [source-path {:keys [name params body] :as definition}]
  ((deref #'gravity.resolution.stage0-functions/normalize-seed-function-header) #'fail! #'seed-declared-header? source-path definition))

(defn validate-function-definition!
  [source-path {:keys [name params body]}]
  (gravity.resolution.stage0-functions/validate-function-definition! #'fail! source-path {:name name :params params :body body}))

(defn- validate-seed-function-names! [source-path definitions]
  ((deref #'gravity.resolution.stage0-functions/validate-seed-function-names!) #'fail! source-path definitions))

(defn stage0-function-table
  [module]
  (gravity.resolution.stage0-functions/stage0-function-table {:function-definition-form #'function-definition-form :validate-seed-function-names! #'validate-seed-function-names! :normalize-seed-function-header #'normalize-seed-function-header :validate-function-definition! #'validate-function-definition!} module))

(defn- validate-seed-source-headers! [source-path forms]
  ((deref #'gravity.resolution.stage0-functions/validate-seed-source-headers!) {:function-definition-form #'function-definition-form :seed-declared-header? #'seed-declared-header? :validate-seed-function-names! #'validate-seed-function-names! :validate-function-definition! #'validate-function-definition! :normalize-seed-function-header #'normalize-seed-function-header} source-path forms))

(defn fail-call-arity!
  [id module callee args expected]
  (gravity.runtime/fail-native-stage0-call-arity! #'fail! id module callee args expected))

(defn assert-min-arity!
  [module callee args n]
  (gravity.runtime/assert-native-stage0-min-arity! #'fail-call-arity! module callee args n))

(defn assert-exact-arity!
  [module callee args n]
  (gravity.runtime/assert-native-stage0-exact-arity! #'fail-call-arity! module callee args n))

(defn assert-between-arity!
  [module callee args min-n max-n]
  (gravity.runtime/assert-native-stage0-between-arity! #'fail-call-arity! module callee args min-n max-n))

(defn assert-even-arity!
  [module callee args]
  (gravity.runtime/assert-native-stage0-even-arity! #'fail-call-arity! module callee args))
