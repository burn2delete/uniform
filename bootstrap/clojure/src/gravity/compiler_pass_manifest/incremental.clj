(ns gravity.compiler-pass-manifest.incremental
  "Incremental cache, proof reuse, and speculative reuse defaults.")

(def compiler-pass-default-cache-key-schema
  {:artifact :gravity/cache-key-schema
   :required-fields [:stage :source :syntax :profile :target :compiler
                     :pass-contract :dependencies :build-effects
                     :capabilities :language-facets]})

(def compiler-pass-default-cache-keys
  [{:stage :type-check
    :source "sha256:stage0-source"
    :syntax "sha256:stage0-syntax"
    :profile "sha256:stage0-profile"
    :target "sha256:stage0-target"
    :compiler "sha256:stage0-clojure-bootstrap"
    :pass-contract "sha256:stage0-type-check-contract"
    :dependencies "sha256:stage0-dependency-graph"
    :build-effects "sha256:stage0-build-replay"
    :capabilities "sha256:stage0-capability-policy"
    :language-facets "sha256:stage0-facets"}])

(def compiler-pass-default-cache-entries
  [{:stage :type-check
    :cache-key "sha256:stage0-type-check-cache-key"
    :artifact-id "sha256:stage0-typed-core"
    :producer {:stage :type-check :pass-version "stage0"}
    :inputs ["sha256:stage0-core"]
    :preserved-facts [:source-spans :resolved-bindings]
    :invalidated-by [:source-change :type-rule-change :profile-change]
    :diagnostics "sha256:stage0-type-diagnostics"
    :trust :local-build
    :revalidation :required-before-release}])

(def compiler-pass-default-proof-reuse-records
  [{:proof-id :proof/stage0-bounds
    :claim :bounds-preserved
    :inputs ["sha256:stage0-mir-op"]
    :profile :native
    :target :jvm
    :status :fresh
    :reuse :accepted
    :invalidation-conditions [:source-change :safety-rule-change
                              :target-change]}])

(def compiler-pass-default-speculative-reuse-records
  [{:artifact-id "sha256:stage0-speculative-expansion"
    :stage :macro-expand
    :reuse :speculative
    :publishable? false
    :revalidation :required-before-release}])
