

(def stage0-compiled-safety-rejected-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/core-app-unsafe-forbidden.gravity"
    :diagnostic "SAFE6-UNSAFE-FORBIDDEN"
    :rejected-behavior :unsafe-island-in-safe-executable}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-unsafe-metadata.gravity"
    :diagnostic "SAFE6-MISSING-METADATA"
    :rejected-behavior :unsafe-island-missing-required-metadata}])

(def stage0-compiled-profile-rejected-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/core-app-profile-effect.gravity"
    :diagnostic "P4-HOST-EFFECT"
    :rejected-behavior :hosted-io-effect-missing}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-profile-capability.gravity"
    :diagnostic "P4-HOST-CAPABILITY"
    :rejected-behavior :hosted-stdout-capability-missing}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-profile-runtime.gravity"
    :diagnostic "P1-RUNTIME"
    :rejected-behavior :compiled-executable-non-hosted-profile}])

(def stage0-compiled-performance-rejected-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/core-app-performance-claim.gravity"
    :diagnostic "PERF1-CLAIM"
    :rejected-behavior :incomplete-performance-claim}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-performance-target.gravity"
    :diagnostic "PERF1-TARGET"
    :rejected-behavior :missing-target-fingerprint}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-performance-elision.gravity"
    :diagnostic "PERF10-PROOF-MISSING"
    :rejected-behavior :unproved-check-elision}])

(def stage0-compiled-math-rejected-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/core-app-math-implicit-narrow.gravity"
    :diagnostic "MATH1-NARROW"
    :rejected-behavior :implicit-numeric-narrowing}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-math-mode-missing.gravity"
    :diagnostic "MATH7-MISSING"
    :rejected-behavior :missing-numeric-mode-contract}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-math-float-manifest.gravity"
    :diagnostic "MATH8-MANIFEST"
    :rejected-behavior :floating-operation-without-manifest}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-math-float-reassoc.gravity"
    :diagnostic "MATH8-REASSOC"
    :rejected-behavior :strict-floating-reassociation-without-proof}])