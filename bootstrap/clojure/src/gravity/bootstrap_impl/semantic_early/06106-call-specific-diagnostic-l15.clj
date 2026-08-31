; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l15!
 [operator node]
 (case
  operator
  provider/missing
  (typed-diagnostic!
   "L15-PROVIDER-MISSING"
   "no provider implements a required capability"
   node
   "Install or declare a provider that implements the capability for this profile and target."
   {:requested-capability :provider/missing,
    :selected-or-missing-provider nil,
    :grant-id nil,
    :scope nil,
    :phase :runtime})
  provider/ambiguous
  (typed-diagnostic!
   "L15-PROVIDER-AMBIGUOUS"
   "multiple providers satisfy the same capability without a deterministic rule"
   node
   "Choose a source annotation, manifest entry, workspace policy, profile default, or compiler default."
   {:requested-capability :filesystem/read,
    :selected-or-missing-provider
    #{'gravity.fs/memory 'gravity.fs/posix},
    :grant-id nil,
    :scope :namespace,
    :phase :runtime})
  provider/profile
  (typed-diagnostic!
   "L15-PROFILE"
   "provider is unsupported by the active profile or target"
   node
   "Use a provider declared for the active profile and target."
   {:requested-capability :network/client,
    :selected-or-missing-provider 'gravity.net/http-client,
    :grant-id nil,
    :scope :network,
    :phase :runtime})
  provider/scope
  (typed-diagnostic!
   "L15-SCOPE"
   "requested provider operation exceeds the grant scope"
   node
   "Attenuate the request to the grant scope or declare a narrower provider grant."
   {:requested-capability :filesystem/read,
    :selected-or-missing-provider 'gravity.fs/read-scoped,
    :grant-id :grant/config-read,
    :scope {:paths ["config/*.edn"]},
    :phase :runtime})
  provider/phase
  (typed-diagnostic!
   "L15-PHASE"
   "build authority is used at runtime or runtime authority is used during compilation"
   node
   "Declare separate build and runtime grants and select providers in the matching phase."
   {:requested-capability :filesystem/read,
    :selected-or-missing-provider 'gravity.fs/build-input,
    :grant-id :grant/schema-build-read,
    :scope {:paths ["schemas/*.edn"]},
    :phase :runtime})
  provider/trust
  (typed-diagnostic!
   "L15-TRUST"
   "provider trust level violates policy"
   node
   "Select a trusted provider, sandbox the provider, or update package policy with explicit approval."
   {:requested-capability :ai/tool,
    :selected-or-missing-provider 'third.party/tool,
    :grant-id :grant/tool,
    :scope :tool,
    :phase :runtime,
    :trust-level :untrusted})
  provider/replay
  (typed-diagnostic!
   "L15-REPLAY"
   "compile-time provider cannot satisfy replay requirements"
   node
   "Use a replayable provider or record a policy exception accepted by the build profile."
   {:requested-capability :filesystem/read,
    :selected-or-missing-provider 'gravity.fs/build-input,
    :grant-id :grant/schema-build-read,
    :scope {:paths ["schemas/*.edn"]},
    :phase :build})
  provider/secret
  (typed-diagnostic!
   "L15-SECRET"
   "provider output would leak secret material into a public artifact"
   node
   "Redact secret values and record only secret ids, scopes, and private provenance."
   {:requested-capability :secrets/read,
    :selected-or-missing-provider 'gravity.secrets/scoped,
    :grant-id :grant/secret,
    :scope {:keys ["TOKEN"]},
    :phase :build})
  provider/contract
  (typed-diagnostic!
   "L15-CONTRACT"
   "provider declaration fails its contract suite"
   node
   "Fix provider effects, scopes, failures, artifacts, or conformance evidence before selection."
   {:requested-capability :math/sin,
    :selected-or-missing-provider 'custom.math/fast,
    :grant-id nil,
    :scope :math,
    :phase :runtime})
  provider/revocation
  (typed-diagnostic!
   "L15-REVOCATION"
   "code assumes revocation in a profile that cannot provide revocable handles"
   node
   "Use a profile with revocable capabilities or encode a static lifetime instead."
   {:requested-capability :filesystem/read,
    :selected-or-missing-provider 'gravity.fs/static,
    :grant-id :grant/config-read,
    :scope {:paths ["config/*.edn"]},
    :phase :runtime})
  semantic-early-call-specific-diagnostic-unhandled))
