(ns gravity.tools.sh07-b51-attempt32-checker-identity
  "Deterministic checker identities for the governed SH-07 Attempt-32 snapshot."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest])
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io ByteArrayInputStream InputStreamReader]
           [java.nio.charset StandardCharsets]))

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/tools/sh07_b51_attempt32_checker_identity/01-checker-specs"
 "/gravity/tools/sh07_b51_attempt32_checker_identity/02-source-product"
 "/gravity/tools/sh07_b51_attempt32_checker_identity/03-runtime-fingerprint")
