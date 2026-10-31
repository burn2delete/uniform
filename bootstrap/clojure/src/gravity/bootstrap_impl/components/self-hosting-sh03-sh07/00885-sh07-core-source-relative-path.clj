;; SH-07-A/B1/B2/B3/B4/B5/B6/B7/B8/B9/B10/B11/B12/B13/B15/B47 is the checked-core projection
;; owned by Gravity source.
;; The coordinator authenticates the verified SH-06 carrier, projects the
;; literal/function/control-flow subset, resolves declared digest
;; requests, and keeps physical paths out of semantic identity.
(def sh07-core-source-relative-path "bootstrap/gravity/src/gravity/checked_core.gravity")

(def sh07-core-adapter-contract :gravity/sh07-to-c6-core-products-v16)
(def sh07-core-governing-document
  "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md")

(def sh07-core-public-function-shapes
  {'sh07-build-core-template {:arity 1 :params '[request]}
   'sh07-verify-core-template {:arity 3 :params '[request template digest-requests]}
   'sh07-verify-core-resolved
   {:arity 4 :params '[request resolved-core digest-requests resolved-digests]}
   'sh07-resolve-source-core-map-preimage {:arity 2 :params '[preimage resolved-digests]}
   'sh07-verify-source-core-map
   {:arity 4 :params '[request resolved-core digest-requests resolved-digests]}})

(def sh07-core-expected-source-byte-count 498219)
(def sh07-core-expected-source-content-hash
  "sha256:34371eb78cbe9d1feabf421c70ced3b9a89bbb6879fab671203ca29a70672ebe")
(def sh07-core-expected-plan-semantic-hash
  "sha256:bf16573f21fd7efb26549ce958fc3e6b672e1d49d5f8c9a2c72a7b60fb986829")
(def sh07-core-expected-functions-semantic-hash
  "sha256:df91ea3bc30dee03968640777a62c6ef210748921649c291caa328da0af94d63")
(def sh07-core-expected-function-count 360)
(def sh07-core-expected-function-names-hash
  "sha256:0d70635e77175d450e0d6d0505d70f95ecb8ada5fb9ca242a3648ccabbbec85c")
(def sh07-core-expected-function-shapes-hash
  "sha256:9af96f01a73b2f1c61c1e8dcea71b5ae49cfcfdd2934d932e333e4c91462b68a")
(def sh07-core-public-function-hashes
  '{sh07-build-core-template "sha256:3c986f70123a51afb4e788199f559b1d571afd825c2ba72c0a53675eb5c34948", sh07-resolve-source-core-map-preimage "sha256:667f3461e8566c1a0ec854f156f1aa56baf8b9d34ffc1f31e7a06a35e7c0048d", sh07-verify-core-resolved "sha256:d0aa83b35de51eb7fdbbdef6133aa5b20ec825340bf45d8c3833fb6801ffa8ba", sh07-verify-core-template "sha256:4bc863464168971648f1c3e7ee17df32155e6c3d77b6c3d69d138566cf3b1791", sh07-verify-source-core-map "sha256:30b77009004032faa6306970676d9bf7e3a01c33d51006c9b69811a20cae1637"})

(declare
 ^{:private true} checked-core-stage-input
 ^{:private true, :dynamic true} *sh07-core-execution-binding*
 ^{:private true} sh07-core-cached-binding
 ^{:private true} sh07-core-development-binding
 sh07-core-public-function-shapes
 sh07-core-source-relative-path
 sh07-core-build-binding!
 ^{:private true} sh07-core-current-source-key
 sh07-core-plan-identities
 sh07-core-source-path)

(def
 ^{:private true} checked-core-stage-defaults
 {:p15-s23-stage2-compiler-artifact-plan
  #'p15-s23-stage2-compiler-artifact-plan,
  :sh07-core-source-path #'sh07-core-source-path,
  :sh07-core-current-source-key #'sh07-core-current-source-key,
  :p15-s23-stage2-runtime-execute-function
  #'p15-s23-stage2-runtime-execute-function,
  :p15-s23-stage2-compiler-artifact-semantic-input
  #'p15-s23-stage2-compiler-artifact-semantic-input,
  :read-forms #'read-forms,
  :sh07-core-plan-identities #'sh07-core-plan-identities,
  :c-backend-resolve-p15-s23-compiler-source-path
  #'c-backend-resolve-p15-s23-compiler-source-path,
  :sha256-bytes-hex #'sha256-bytes-hex,
  :input #'checked-core-stage-input,
  :p15-s23-c6c10-carrier-validation #'p15-s23-c6c10-carrier-validation,
  :sh07-core-build-binding! #'sh07-core-build-binding!,
  :c-backend-stage2-plan-emitter-source-rule!
  #'c-backend-stage2-plan-emitter-source-rule!,
  :p15-s23-stage2-compiler-artifact-source-path
  #'p15-s23-stage2-compiler-artifact-source-path,
  :p15-s23-c11-mir-digest #'p15-s23-c11-mir-digest,
  :sh04-syntax-strip-host-metadata #'sh04-syntax-strip-host-metadata})

(defn- checked-core-stage-operations [] checked-core-stage-defaults)

(defn-
 checked-core-stage-input
 [field]
 (case
  field
  :*sh07-core-execution-binding*
  *sh07-core-execution-binding*
  :sh07-core-cached-binding
  sh07-core-cached-binding
  :sh07-core-development-binding
  sh07-core-development-binding
  :sh07-core-public-function-shapes
  sh07-core-public-function-shapes
  :sh07-core-source-relative-path
  sh07-core-source-relative-path
  (throw (ex-info "Unknown checked-core stage input" {:input field}))))

(defn
 sh07-core-source-path
 []
 (gravity.checked-core/source-path (checked-core-stage-operations)))

(defn
 sh07-core-plan-identities
 [plan]
 (gravity.checked-core/plan-identities
  plan
  (checked-core-stage-operations)))

(defn
 sh07-core-build-binding!
 []
 (gravity.checked-core/build-plan-binding!
  (checked-core-stage-operations)))

(defn-
 sh07-core-current-source-key
 []
 (gravity.checked-core/source-key (checked-core-stage-operations)))

(def ^:private sh07-core-development-binding (atom nil))

(defn
 sh07-core-current-binding!
 "Compile the current source; reuse only a binding for unchanged input bytes."
 []
 (gravity.checked-core/current-plan-binding!
  (checked-core-stage-operations)))

(def ^:private sh07-core-cached-binding
  (reify clojure.lang.IDeref
    (deref [_] (sh07-core-current-binding!))
    clojure.lang.IPending
    (isRealized [_]
      (= (sh07-core-current-source-key)
         (:key @sh07-core-development-binding)))))

(defn sh07-core-assert-pinned-revision!
  "Require the complete historical revision at an explicit migration boundary."
  ([] (sh07-core-assert-pinned-revision! @sh07-core-cached-binding))
  ([binding]
   (let [expected {:source-byte-count sh07-core-expected-source-byte-count
                   :source-content-hash sh07-core-expected-source-content-hash
                   :plan-semantic-hash sh07-core-expected-plan-semantic-hash
                   :functions-semantic-hash sh07-core-expected-functions-semantic-hash
                   :function-count sh07-core-expected-function-count
                   :function-names-hash sh07-core-expected-function-names-hash
                   :function-shapes-hash sh07-core-expected-function-shapes-hash
                   :public-function-hashes sh07-core-public-function-hashes
                   :public-function-shapes sh07-core-public-function-shapes}]
     (when-not (= expected (select-keys binding (keys expected)))
       (throw (ex-info "Historical SH-07 migration pins differ from current code"
                       {:id "C6-VERIFY" :stage :core-lowering
                        :missing-fields [:pinned-sh07-core-plan]
                        :reason :unadmitted-checked-core-revision
                        :expected expected
                        :observed (select-keys binding (keys expected))})))
     binding)))

(def ^:private ^:dynamic *sh07-core-execution-binding* nil)

(defn
 sh07-core-execute!
 [source-path function arguments]
 (gravity.checked-core/execute!
  source-path
  function
  arguments
  (checked-core-stage-operations)))

(defn sh07-core-source-path-from-resolution [resolution-artifact]
  (or (get-in resolution-artifact [:provenance :source-path])
      (get-in resolution-artifact
              [:gravity-resolution-boundary
               :authenticated-resolution-request
               :provenance :actual-source-path])
      "<sh07-core-input>"))

(defn sh07-core-semantic-span [span]
  (select-keys (or (:primary span) span {})
   [:byte-start :byte-end :line-start :column-start
    :line-end :column-end :scalar-start :scalar-end]))

(defn sh07-core-value-kind [value]
  (cond
    (nil? value) :nil (boolean? value) :boolean (integer? value) :integer
    (or (instance? java.math.BigDecimal value)
        (instance? Double value) (instance? Float value)) :decimal
    (ratio? value) :ratio (char? value) :character
    (string? value) :string
    (keyword? value) :keyword (symbol? value) :symbol
    (vector? value) :vector (map? value) :map
    (set? value) :set (seq? value) :list
    :else :unsupported))
