(ns gravity.tools.sh07-b51-attempt32-tier0-materializer
  "Parent-safe validation and joining for already-produced Attempt-32 output.

  This namespace deliberately has no project dependency. The coordinator owns
  process creation; this layer accepts inert child values plus neutral
  validation/build functions from M's already-loaded authority.")

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/tools/sh07_b51_attempt32_tier0_materializer/01-predecessor-executable-descriptor-id"
 "/gravity/tools/sh07_b51_attempt32_tier0_materializer/02-construct-negative-edge-evidence")
