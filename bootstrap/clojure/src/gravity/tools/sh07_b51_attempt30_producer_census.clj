(ns gravity.tools.sh07-b51-attempt30-producer-census
  "Offline source-first exhaustive census checker for SH-07 B51 Attempt 30.

  This namespace is intentionally self-contained.  It shares only the
  repository's canonical reader encoding and SHA-256 scalar primitive.  In
  particular it does not call the Attempt-28 census, carrier traversal,
  reachability, row, normalization, or digest-schedule implementation."
  (:require [clojure.set :as set]
            [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest]
            [gravity.tools.sh07-b51-attempt32-materialize :as materialize]))

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/tools/sh07_b51_attempt30_producer_census/01-input-keys"
 "/gravity/tools/sh07_b51_attempt30_producer_census/02-value-profile"
 "/gravity/tools/sh07_b51_attempt30_producer_census/03-source-indexes"
 "/gravity/tools/sh07_b51_attempt30_producer_census/04-scan-m2"
 "/gravity/tools/sh07_b51_attempt30_producer_census/05-validate-envelope"
 "/gravity/tools/sh07_b51_attempt30_producer_census/06-validate-builtin-parity"
 "/gravity/tools/sh07_b51_attempt30_producer_census/07-category-product"
 "/gravity/tools/sh07_b51_attempt30_producer_census/08-builtin-commitments"
 "/gravity/tools/sh07_b51_attempt30_producer_census/09-certificate-valid"
 "/gravity/tools/sh07_b51_attempt30_producer_census/10-produce-accepted")
