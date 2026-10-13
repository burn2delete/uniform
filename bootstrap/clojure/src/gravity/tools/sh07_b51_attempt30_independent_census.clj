(ns gravity.tools.sh07-b51-attempt30-independent-census
  (:require [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest]))

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/tools/sh07_b51_attempt30_independent_census/01-input-keys"
 "/gravity/tools/sh07_b51_attempt30_independent_census/02-transcript-counts-within"
 "/gravity/tools/sh07_b51_attempt30_independent_census/03-instruction-children"
 "/gravity/tools/sh07_b51_attempt30_independent_census/04-check-and-derive-inputs"
 "/gravity/tools/sh07_b51_attempt30_independent_census/05-normalized-external-source-rows"
 "/gravity/tools/sh07_b51_attempt30_independent_census/06-category-rows"
 "/gravity/tools/sh07_b51_attempt30_independent_census/07-transcript-within"
 "/gravity/tools/sh07_b51_attempt30_independent_census/08-certificate-valid"
 "/gravity/tools/sh07_b51_attempt30_independent_census/09-valid-rejection-output")
