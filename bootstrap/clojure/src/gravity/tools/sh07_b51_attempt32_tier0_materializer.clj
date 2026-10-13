(ns gravity.tools.sh07-b51-attempt32-tier0-materializer
  "Construct and verify authentic Attempt-32 Tier-0 census evidence."
  (:require [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]
            [gravity.tools.sh07-b51-attempt30-independent-census :as checker-b]
            [gravity.tools.sh07-b51-attempt30-producer-census :as checker-a]
            [gravity.tools.sh07-b51-attempt32-checker-identity :as identity])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path]))

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/tools/sh07_b51_attempt32_tier0_materializer/01-predecessor-executable-descriptor-id"
 "/gravity/tools/sh07_b51_attempt32_tier0_materializer/02-validate-reloaded-evidence")
