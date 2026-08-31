(ns gravity.c8-effect-checker.catalog
  "Stable C8 diagnostics, effect names, and capability relationships.")

(def diagnostic-ids
  ["C8-UNDECLARED"
   "C8-PROFILE"
   "C8-CAPABILITY"
   "C8-BUILD"
   "C8-REPLAY"
   "C8-ORDER"
   "C8-RUNTIME"
   "C8-UNKNOWN"
   "C8-VERIFY"])

(def governing-document
  "docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md")

(def rejected-designs
  [{:diagnostic "C8-UNDECLARED"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-undeclared.gravity"
    :rejected-design :inferred-effect-outside-declaration}
   {:diagnostic "C8-PROFILE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-profile.gravity"
    :rejected-design :profile-rejects-effect}
   {:diagnostic "C8-CAPABILITY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-capability.gravity"
    :rejected-design :missing-capability-grant}
   {:diagnostic "C8-BUILD"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-build.gravity"
    :rejected-design :ungranted-build-effect}
   {:diagnostic "C8-REPLAY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-replay.gravity"
    :rejected-design :replay-sensitive-effect-without-obligation}
   {:diagnostic "C8-ORDER"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-order.gravity"
    :rejected-design :missing-effect-ordering}
   {:diagnostic "C8-RUNTIME"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-runtime.gravity"
    :rejected-design :no-runtime-provider-support}
   {:diagnostic "C8-UNKNOWN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-unknown.gravity"
    :rejected-design :unregistered-effect-name}
   {:diagnostic "C8-VERIFY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-verify.gravity"
    :rejected-design :malformed-effect-artifact}])

(def override-diagnostics
  {:undeclared "C8-UNDECLARED"
   :profile "C8-PROFILE"
   :capability "C8-CAPABILITY"
   :build "C8-BUILD"
   :replay "C8-REPLAY"
   :order "C8-ORDER"
   :runtime "C8-RUNTIME"
   :unknown "C8-UNKNOWN"
   :verify "C8-VERIFY"})

(def known-effects
  #{:io/write :io/read :filesystem/read :filesystem/write :network/http
    :database/read :database/write :time/read :random/read
    :runtime/dynamic-dispatch :error/throw :memory/raw :ffi/call
    :workflow/event :workflow/replay :ai/model-call :ai/tool-call
    :ai/human-review :build/read-file :build/write-artifact
    :build/network :build/exec :build/model-call :build/tool-call})

(def effect-capability
  {:io/write :io/stdout
   :filesystem/read :fs/read
   :filesystem/write :fs/write
   :network/http :http/client
   :database/read :db/read
   :database/write :db/write
   :memory/raw :memory/raw
   :ffi/call :ffi/call
   :workflow/event :workflow/event
   :ai/model-call :model/call
   :ai/tool-call :tool/invoke
   :ai/human-review :ai/human-review
   :build/read-file :fs/read
   :build/write-artifact :artifact/write
   :build/network :http/client
   :build/exec :process/exec
   :build/model-call :model/call
   :build/tool-call :tool/invoke})

(def replay-sensitive-effects
  #{:time/read :random/read :network/http :database/read :workflow/event
    :workflow/replay :ai/model-call :ai/tool-call :ai/human-review
    :runtime/dynamic-dispatch})

(defn effect-message [id]
  (case id
    "C8-UNDECLARED" "inferred effects exceed the declared effect allowance"
    "C8-PROFILE" "active profile rejects the inferred effect"
    "C8-CAPABILITY" "effect lacks a required capability grant"
    "C8-BUILD" "build effect lacks a build grant"
    "C8-REPLAY" "replay-sensitive effect lacks replay or audit obligation"
    "C8-ORDER" "effect ordering constraints are missing"
    "C8-RUNTIME" "no legal runtime or provider supports the effect"
    "C8-UNKNOWN" "effect name is unregistered"
    "C8-VERIFY" "effect verifier rejected the artifact"
    "Effect checking failed"))
