(ns gravity.c7-type-checker.catalog
  "Stable diagnostics and governing metadata for hosted Stage0 C7.")

(def diagnostic-ids
  ["C7-TYPE-MISMATCH"
   "C7-ANNOTATION"
   "C7-DYNAMIC"
   "C7-CAST"
   "C7-NULLABILITY"
   "C7-GENERIC"
   "C7-PROTOCOL"
   "C7-LAYOUT"
   "C7-SCHEMA"
   "C7-VERIFY"])

(def governing-document
  "docs/phase-06-compiler-architecture/086-c7-type-checker-design.md")

(def rejected-designs
  [{:diagnostic "C7-TYPE-MISMATCH"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-type-mismatch.gravity"
    :rejected-design :incompatible-inferred-and-expected-types}
   {:diagnostic "C7-ANNOTATION"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-annotation.gravity"
    :rejected-design :profile-required-type-fact-missing}
   {:diagnostic "C7-DYNAMIC"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-dynamic.gravity"
    :rejected-design :dynamic-fallback-in-constrained-profile}
   {:diagnostic "C7-CAST"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-cast.gravity"
    :rejected-design :unchecked-or-illegal-conversion}
   {:diagnostic "C7-NULLABILITY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-nullability.gravity"
    :rejected-design :host-null-without-typed-wrapper}
   {:diagnostic "C7-GENERIC"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-generic.gravity"
    :rejected-design :failed-generic-instantiation}
   {:diagnostic "C7-PROTOCOL"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-protocol.gravity"
    :rejected-design :missing-protocol-implementation}
   {:diagnostic "C7-LAYOUT"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-layout.gravity"
    :rejected-design :missing-profile-required-layout-facts}
   {:diagnostic "C7-SCHEMA"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-schema.gravity"
    :rejected-design :schema-derived-type-weakened}
   {:diagnostic "C7-VERIFY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c7-verify.gravity"
    :rejected-design :typed-core-verifier-failure}])

(def override-diagnostics
  {:type-mismatch "C7-TYPE-MISMATCH"
   :annotation "C7-ANNOTATION"
   :dynamic "C7-DYNAMIC"
   :cast "C7-CAST"
   :nullability "C7-NULLABILITY"
   :generic "C7-GENERIC"
   :protocol "C7-PROTOCOL"
   :layout "C7-LAYOUT"
   :schema "C7-SCHEMA"
   :verify "C7-VERIFY"})

(defn type-message
  [id]
  (case id
    "C7-TYPE-MISMATCH" "inferred type is incompatible with the expected type"
    "C7-ANNOTATION" "active profile requires a type annotation or layout fact"
    "C7-DYNAMIC" "dynamic behavior is forbidden by the active profile"
    "C7-CAST" "cast or conversion lacks a checked or unsafe classification"
    "C7-NULLABILITY" "host null crossed into a non-null Gravity type without a wrapper"
    "C7-GENERIC" "generic instantiation failed or omitted bound evidence"
    "C7-PROTOCOL" "protocol dispatch lacks a matching implementation"
    "C7-LAYOUT" "profile-required layout facts are missing"
    "C7-SCHEMA" "schema-derived type lost source schema identity"
    "C7-VERIFY" "typed-core verifier rejected the artifact"
    "Type checking failed"))
