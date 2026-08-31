; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l19!
 [operator node]
 (case
  operator
  interop/boundary-incomplete
  (typed-diagnostic!
   "L19-BOUNDARY-INCOMPLETE"
   "foreign declaration omits required boundary metadata"
   node
   "Declare ABI or protocol, types, ownership, effects, capabilities, errors, memory, threading, safety, profiles, and version policy."
   {:boundary-id :c/strlen,
    :foreign-source "string.h",
    :active-profile (:profile node),
    :provider-id 'gravity.ffi/c,
    :missing #{:error-map :ownership :version}})
  interop/profile
  (typed-diagnostic!
   "L19-PROFILE"
   "foreign boundary is unsupported by the active profile"
   node
   "Move the boundary behind a supported profile or provide a portable typed artifact boundary."
   {:boundary-id :jvm/String,
    :foreign-source "java.lang.String",
    :active-profile (:profile node),
    :provider-id 'gravity.jvm/bridge})
  interop/type-map-error
  (typed-diagnostic!
   "L19-TYPE-MAP"
   "foreign type mapping is missing, lossy, or unchecked"
   node
   "Use an explicit directional mapping and return Result for conversions that can fail."
   {:boundary-id :c/strlen,
    :foreign-source "char*",
    :type-mapping {:from "String", :to "CString"},
    :required-proof :checked-conversion})
  interop/ownership-error
  (typed-diagnostic!
   "L19-OWNERSHIP"
   "foreign ownership, borrow, release, or allocator facts are missing"
   node
   "Record ownership transfer, nullability, initialization, lifetime, allocator identity, aliasing, and release facts."
   {:boundary-id :ffi/buffer,
    :foreign-source "libexample",
    :ownership-facts nil,
    :required-proof :allocator-release-match})
  interop/error-map-error
  (typed-diagnostic!
   "L19-ERROR-MAP"
   "foreign failure is not translated to Gravity error values"
   node
   "Translate exceptions, panics, error codes, rejected promises, signals, and process exits before crossing into safe Gravity."
   {:boundary-id :db/query,
    :foreign-source "postgres",
    :error-map nil,
    :required-proof :failure-translation})
  interop/capability-error
  (typed-diagnostic!
   "L19-CAPABILITY"
   "foreign boundary lacks required authority"
   node
   "Declare the capability requirement and select a provider grant for the boundary."
   {:boundary-id :process/git,
    :foreign-source "git",
    :capabilities #{:shell/exec},
    :provider-id nil})
  interop/effect-error
  (typed-diagnostic!
   "L19-EFFECT"
   "foreign effects are missing from the caller"
   node
   "Add the declared boundary effects to the caller or move the call behind an effect-policed wrapper."
   {:boundary-id :http/service,
    :foreign-source "https://api.example.test",
    :effects #{:network/http}})
  interop/safe-wrapper-error
  (typed-diagnostic!
   "L19-SAFE-WRAPPER"
   "safe wrapper invariants are unproven"
   node
   "Attach wrapper requirements, ensures clauses, unsafe call provenance, profile support, and audit evidence."
   {:boundary-id :safe-strlen,
    :foreign-source "strlen",
    :safe-wrapper :interop.string/safe-strlen,
    :required-proof :wrapper-invariant})
  interop/schema-drift
  (typed-diagnostic!
   "L19-SCHEMA-DRIFT"
   "generated bindings no longer match source schemas"
   node
   "Regenerate or reject bindings when schema digest, version, nullability, error mapping, or compatibility policy changes."
   {:boundary-id :graphql/github,
    :foreign-source "schema.graphql",
    :schema-version "old",
    :required-proof :schema-digest-match})
  interop/migration-parity
  (typed-diagnostic!
   "L19-MIGRATION-PARITY"
   "migration parity tests fail against incumbent behavior"
   node
   "Record preserved, emulated, narrowed, and rejected behavior with golden parity tests."
   {:boundary-id :python/model-pipeline,
    :foreign-source "legacy_pipeline.py",
    :parity-status :failed,
    :migration-step :fix-or-reject})
  interop/host-leak
  (typed-diagnostic!
   "L19-HOST-LEAK"
   "host runtime behavior leaks into a portable or constrained profile"
   node
   "Represent host behavior as an explicit hosted bridge or replace it with a portable Gravity artifact."
   {:boundary-id :jvm/reflection,
    :foreign-source "java.lang.reflect",
    :active-profile (:profile node),
    :provider-id 'gravity.jvm/reflection})
  semantic-early-call-specific-diagnostic-unhandled))
